/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.prime.dtvplayer.cas;  
//package com.google.android.exoplayer2.cas;

import android.annotation.TargetApi;
import android.media.MediaCas;
import android.media.MediaCasException;
import android.os.HandlerThread;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import com.prime.dtvplayer.cas.CasInitData.SchemeData;
//import com.google.android.exoplayer2.cas.CasInitData.SchemeData;
import com.google.android.exoplayer2.util.EventDispatcher;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A {@link CasSession} that supports playbacks using {@link ExoMediaCas}.
 */
@TargetApi(18)
public class DefaultCasSession implements CasSession {

  /**
   * Manages provisioning requests.
   */
  public interface ProvisioningManager {

    /**
     * Called when a session requires provisioning.
     *
     * @param session The session.
     */
    void provisionRequired(DefaultCasSession session) throws MediaCasException;

  }

  public final SchemeData schemeData;

  private static final String TAG = "DefaultCasSession";

  private final ExoMediaCas<MediaCas.Session> mediaCas;
  private final ProvisioningManager provisioningManager;
  private final @DefaultCasSessionManager.Mode int mode;
  private final HashMap<String, String> optionalKeyRequestParameters;
  private final EventDispatcher<DefaultCasSessionEventListener> eventDispatcher;
  private final int initialCasRequestRetryCount;
  private final CasScheme casScheme;

  /* package */ final int caSystemId;
  public final @CasSessionManager.sessionMode int sessionMode;

  private @CasSession.State int state;
  private int openCount;
  private HandlerThread requestHandlerThread;
  private ExoMediaDescrambler mediaDescrambler;
  private CasSessionException lastException;
  MediaCas.Session session;

  private Object currentKeyRequest;
  private Object currentProvisionRequest;

  private EcmHolder ecmHolder;

  private static final int MEDIAPLAYER_COMMAND_SESSION_MODE = 0xC0;

  /**
   * Instantiates a new CAS session.
   *
   * @param caSystemId The id of the cas scheme.
   * @param mediaCas The media CAS.
   * @param provisioningManager The manager for provisioning.
   * @param schemeData The CAS data for this session, or null if a {@code offlineLicenseKeySetId} is
   *     provided.
   * @param mode The CAS mode.
   * @param optionalKeyRequestParameters The optional key request parameters.
   * @param eventDispatcher The dispatcher for CAS session manager events.
   * @param initialCasRequestRetryCount The number of times to retry for initial provisioning and
   *     key request before reporting error.
   */
  public DefaultCasSession(
      int caSystemId,
      @CasSessionManager.sessionMode int sessionMode,
      ExoMediaCas<MediaCas.Session> mediaCas,
      ProvisioningManager provisioningManager,
      @Nullable SchemeData schemeData,
      @DefaultCasSessionManager.Mode int mode,
      HashMap<String, String> optionalKeyRequestParameters,
      EventDispatcher<DefaultCasSessionEventListener> eventDispatcher,
      int initialCasRequestRetryCount) {
    this.caSystemId = caSystemId;
    this.sessionMode = sessionMode;
    this.provisioningManager = provisioningManager;
    this.mediaCas = mediaCas;
    this.mode = mode;
    this.schemeData = schemeData;
    this.optionalKeyRequestParameters = optionalKeyRequestParameters;
    this.initialCasRequestRetryCount = initialCasRequestRetryCount;
    this.eventDispatcher = eventDispatcher;
    state = STATE_OPENING;

    requestHandlerThread = new HandlerThread("CasRequestHandler");
    requestHandlerThread.start();

    casScheme = new CasScheme(caSystemId, schemeData.data);
  }

  // Life cycle.

  public void acquire() {
    if (++openCount == 1) {
      if (state == STATE_ERROR) {
        return;
      }
      openInternal(true);
    }
  }

  /**
   * @return True if the session is closed and cleaned up, false otherwise.
   */
  public boolean release() {
    if (--openCount == 0) {
      state = STATE_RELEASED;
      requestHandlerThread.quit();
      requestHandlerThread = null;
      mediaDescrambler = null;
      lastException = null;
      currentKeyRequest = null;
      currentProvisionRequest = null;
      if (session != null) {
        session.close();
        session = null;
      }
      return true;
    }
    return false;
  }

  public boolean hasInitData(byte[] initData) {
    return Arrays.equals(schemeData != null ? schemeData.data : null, initData);
  }

  // Provisioning implementation.
  public void provision() throws MediaCasException {
    final String provisionString = casScheme.CasSchemeGetProvisionString();
    mediaCas.provision(provisionString);
  }

  // CasSession implementation.

  @Override
  @CasSession.State
  public final int getState() {
    return state;
  }

  @Override
  public final CasSessionException getError() {
    return state == STATE_ERROR ? lastException : null;
  }

  @Override
  public final ExoMediaDescrambler getMediaDescrambler() {
    return mediaDescrambler;
  }

  @Override
  public byte[] descramble(@NonNull byte[] data, int offset, int length, int polarity) {
    return mediaDescrambler.descramble(sessionMode, data, offset, length, polarity);
  }

  @Override
  public void processEcm(@NonNull EcmHolder newEcmHolder) {
    try {

      /* Check the new ECM hasn't already been set by a different render thread */
      if (newEcmHolder != ecmHolder) {
        byte[] currentEcm = newEcmHolder.currentEcm;
        byte[] previousEcm = newEcmHolder.previousEcm;

        /* Check for first inject, skip condition or when both keys changed */
        if (previousEcm != null && (ecmHolder == null || ecmHolder.currentEcm != previousEcm)) {
          session.processEcm(previousEcm, 0, previousEcm.length);
        }

        session.processEcm(currentEcm, 0, currentEcm.length);

        /* Update our local ECM holder */
        ecmHolder = newEcmHolder;
      }

    } catch (Exception e) {
      onError(e);
    }
  }

  @Override
  public void processDtvEcm(@NonNull byte[] ecmData)//eric lin 20210107 widevine cas
  {

  }
  // Internal methods.

  /**
   *  Try to open a session, do provisioning if necessary.
   *  @param allowProvisioning if provisioning is allowed, set this to false when calling from
   *      processing provision response.
   *  @return true on success, false otherwise.
   */
  private boolean openInternal(boolean allowProvisioning) {
    if (isOpen()) {
      // Already opened
      return true;
    }

    try {
      android.util.Log.d("WidevineCasSessionMgr", "dcs opensession");//eric lin test
      session = mediaCas.openSession();
      android.util.Log.d("WidevineCasSessionMgr", "Default setPrivateData");//eric lin test
      session.setPrivateData(new byte[] {(byte) MEDIAPLAYER_COMMAND_SESSION_MODE, (byte) sessionMode });
      mediaDescrambler = mediaCas.createMediaDescrambler();
      mediaDescrambler.setMediaCasSession(session);
      state = STATE_OPENED;

      if (allowProvisioning) {
        provisioningManager.provisionRequired(this);
      }

      return true;
    } catch (Exception e) {
      onError(e);
    }

    return false;
  }

  private void onError(final Exception e) {
    lastException = new CasSessionException(e);
    eventDispatcher.dispatch(listener -> listener.onCasSessionManagerError(e));
    if (state != STATE_OPENED_WITH_KEYS) {
      state = STATE_ERROR;
    }
  }

  private boolean isOpen() {
    return state == STATE_OPENED || state == STATE_OPENED_WITH_KEYS;
  }

}
