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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.google.android.exoplayer2.cas.CaDescriptorPrivateData;
import com.prime.dtvplayer.cas.CasInitData.SchemeData;
//import com.google.android.exoplayer2.cas.CasInitData.SchemeData;
import com.google.android.exoplayer2.util.EventDispatcher;
import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A {@link CasSession} that supports playbacks using {@link ExoMediaCas}.
 */
@TargetApi(18)
public class WidevineCasSession implements CasSession {

  /**
   * Manages provisioning requests.
   */
  public interface ProvisioningManager {

    /**
     * Called when a session requires provisioning.
     *
     * @param session The session.
     */
    void provisionRequired(WidevineCasSession session) throws MediaCasException;

  }

  public final SchemeData schemeData;

  private static final String TAG = "WidevineCasSession";

  private final ExoMediaCas<MediaCas.Session> mediaCas;
  private final ProvisioningManager provisioningManager;
  private final @WidevineCasSessionManager.Mode int mode;
  private final HashMap<String, String> optionalKeyRequestParameters;
  private final EventDispatcher<DefaultCasSessionEventListener> eventDispatcher;
  private final int initialCasRequestRetryCount;
  private final CasScheme casScheme;
  private final Object lockCasReady;
  private final Object lockSessionReady;
  private String casProvisioning = null;
  private String casContentId = null;
  private String casProvider = null;

  /* package */ final int caSystemId;
  public final @CasSessionManager.sessionMode int sessionMode;

  private @State int state;
  private int openCount;
  private HandlerThread requestHandlerThread;
  private ExoMediaDescrambler mediaDescrambler;
  private CasSessionException lastException;
  MediaCas.Session session;
  private Boolean casReady;
  int sessionId;
  public int sessionIndex;//eric lin 20210107 widevine cas

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
  public WidevineCasSession(
      int caSystemId,
      @CasSessionManager.sessionMode int sessionMode,
      ExoMediaCas<MediaCas.Session> mediaCas,
      String casProvisioning,
      String casContentId,
      String casProvider,
      ProvisioningManager provisioningManager,
      @Nullable SchemeData schemeData,
      @WidevineCasSessionManager.Mode int mode,
      HashMap<String, String> optionalKeyRequestParameters,
      EventDispatcher<DefaultCasSessionEventListener> eventDispatcher,
      int initialCasRequestRetryCount) {
    this.caSystemId = caSystemId;
    this.sessionMode = sessionMode;
    this.provisioningManager = provisioningManager;
    this.mediaCas = mediaCas;
    this.casContentId = casContentId;
    this.casProvider = casProvider;
    this.mode = mode;
    this.schemeData = schemeData;
    this.optionalKeyRequestParameters = optionalKeyRequestParameters;
    this.initialCasRequestRetryCount = initialCasRequestRetryCount;
    this.eventDispatcher = eventDispatcher;
    lockCasReady = new Object();
    lockSessionReady = new Object();
    casReady = false; // TODO: Use state instead ?
    sessionId = -1;
    state = STATE_OPENING;
    Log.d("WidevineCasSessionMgr", "wcs WidevineCasSession()--start");//eric lin test
    requestHandlerThread = new HandlerThread("CasRequestHandler");
    Log.d("WidevineCasSessionMgr", "wcs WidevineCasSession() requestHandlerThread.start()");//eric lin test
    requestHandlerThread.start();
    //postRequestHandler = new PostRequestHandler(requestHandlerThread.getLooper());
    casScheme = new CasScheme(caSystemId, schemeData.data);
    Log.d("WidevineCasSessionMgr", "wcs WidevineCasSession()--end");//eric lin test
  }

  // Life cycle.

  public void acquire() {
    Log.d("WidevineCasSessionMgr", "wcs acquire()--start");//eric lin test
    if (++openCount == 1) {
      if (state == STATE_ERROR) {
        Log.d("WidevineCasSessionMgr", "wcs acquire()--end return");//eric lin test
        return;
      }
      Log.d("WidevineCasSessionMgr", "wcs acquire() call openInternal(true)");//eric lin test
      openInternal(true);
    }
    Log.d("WidevineCasSessionMgr", "wcs acquire()--end");//eric lin test
  }

  /**
   * @return True if the session is closed and cleaned up, false otherwise.
   */
  public boolean release() {
    Log.d("WidevineCasSessionMgr", "wcs release()--start");//eric lin test
    if (--openCount == 0) {
      state = STATE_RELEASED;
      //postRequestHandler.removeCallbacksAndMessages(null);
      //postRequestHandler = null;
      requestHandlerThread.quit();
      requestHandlerThread = null;
      mediaDescrambler = null;
      lastException = null;
      currentProvisionRequest = null;
      if (session != null) {
        session.close();
        session = null;
      }
      Log.d("WidevineCasSessionMgr", "wcs release()--end return true");//eric lin test
      return true;
    }
    Log.d("WidevineCasSessionMgr", "wcs release()--end return false");//eric lin test
    return false;
  }

  public boolean hasInitData(byte[] initData) {
    Log.d("WidevineCasSessionMgr", "wcs hasInitData()--start");//eric lin test
    return Arrays.equals(schemeData != null ? schemeData.data : null, initData);
  }

  // Provisioning implementation.
  public void provision() throws MediaCasException {
  }

  public void onMediaCasEvent(int what) {
  }

  // CasSession implementation.

  @Override
  @State
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
/*
    Log.d("WidevineCasSessionMgr", "processEcm currentEcm="+ newEcmHolder.getcurrentEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getcurrentEcm()));//eric lin test
    Log.d("WidevineCasSessionMgr", "processEcm previousEcm="+ newEcmHolder.getpreviousEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getpreviousEcm()));//eric lin test

    Log.d(TAG, "processEcm oddEcm="+ newEcmHolder.getOddEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getOddEcm()));//eric lin test
    Log.d(TAG, "processEcm evenEcm="+ newEcmHolder.getEvenEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getEvenEcm()));//eric lin test
*/
    Log.d("WidevineCasSessionMgr", "wcs processEcm()--start");//eric lin test
    blockUntilReady();

    try {

      /* Check the new ECM hasn't already been set by a different render thread */
      if (newEcmHolder != ecmHolder) {
        Log.d("WidevineCasSessionMgr", "wcs processEcm() newEcmHolder != ecmHolder");//eric lin test
        Log.d("WidevineCasSessionMgr", "wcs processEcm() currentEcm=");//eric lin test
        Log.d("WidevineCasSessionMgr", toHexString(newEcmHolder.getcurrentEcm()));//eric lin test

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
    Log.d("WidevineCasSessionMgr", "wcs processEcm()--end");//eric lin test
  }

  @Override
  public void processDtvEcm(@NonNull byte[] ecmData) {//eric lin 20210107 widevine cas
/*
    Log.d("WidevineCasSessionMgr", "processEcm currentEcm="+ newEcmHolder.getcurrentEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getcurrentEcm()));//eric lin test
    Log.d("WidevineCasSessionMgr", "processEcm previousEcm="+ newEcmHolder.getpreviousEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getpreviousEcm()));//eric lin test

    Log.d(TAG, "processEcm oddEcm="+ newEcmHolder.getOddEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getOddEcm()));//eric lin test
    Log.d(TAG, "processEcm evenEcm="+ newEcmHolder.getEvenEcm().length);//eric lin test
    //Log.d(TAG, toHexString(newEcmHolder.getEvenEcm()));//eric lin test
*/
    //Log.d("WidevineCasSessionMgr", "wcs processDtvEcm()--start");//eric lin test
    blockUntilReady();
    //Log.d("WidevineCasSessionMgr", "wcs processDtvEcm()--pass blockUntilReady()");//eric lin test

    try {

      /* Check the new ECM hasn't already been set by a different render thread */
       {//if (newEcmHolder != ecmHolder) { //eric lin mark
        //Log.d(TAG, "processDtvEcm newEcmHolder != ecmHolder");//eric lin test
        //Log.d("WidevineCasSessionMgr", "processEcm currentEcm=");//eric lin test
        //Log.d("WidevineCasSessionMgr", toHexString(newEcmHolder.getcurrentEcm()));//eric lin test

        //byte[] currentEcm = newEcmHolder.currentEcm;//eric lin mark
        //byte[] previousEcm = newEcmHolder.previousEcm;//eric lin mark

        /* Check for first inject, skip condition or when both keys changed */
         /* //eric lin mark
        if (previousEcm != null && (ecmHolder == null || ecmHolder.currentEcm != previousEcm)) {
          session.processEcm(previousEcm, 0, previousEcm.length);
        }
         */
         Log.d("WidevineCasSessionMgr", "wcs processDtvEcm()--processEcm ZZZ, start");//eric lin test
        session.processEcm(ecmData, 0, ecmData.length);
         Log.d("WidevineCasSessionMgr", "wcs processDtvEcm()--processEcm ZZZ, end");//eric lin test

        /* Update our local ECM holder */
        //ecmHolder = newEcmHolder;//eric lin mark
      }

    } catch (Exception e) {
      onError(e);
    }
    Log.d("WidevineCasSessionMgr", "wcs processDtvEcm()--end");//eric lin test
  }

  public void setSessionId(int sessionId) {
    Log.d("WidevineCasSessionMgr", "wcs setSessionId()--start, sessionId="+sessionId);//eric lin test
    synchronized (lockSessionReady) {
      this.sessionId = sessionId;
      lockSessionReady.notifyAll();
    }
    Log.d("WidevineCasSessionMgr", "wcs setSessionId()--end");//eric lin test
  }

  public void notifyCasReady(boolean casReady) {
    Log.d("WidevineCasSessionMgr", "wcs notifyCasReady()--start");//eric lin test
    synchronized (lockCasReady) {
      this.casReady = casReady;
      lockCasReady.notifyAll();
    }
    Log.d("WidevineCasSessionMgr", "wcs notifyCasReady()--end");//eric lin test
  }

  // Internal methods.

  public static String toHexString(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
      String hex = Integer.toHexString(0xFF & bytes[i]);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }

    return hexString.toString();
  }

  public MediaCas.Session getSession()//eric lin 20210107 widevine cas
  {
    return session;
  }

  /**
   *  Try to open a session, do provisioning if necessary.
   *  @param allowProvisioning if provisioning is allowed, set this to false when calling from
   *      processing provision response.
   *  @return true on success, false otherwise.
   */
  private boolean openInternal(boolean allowProvisioning) {
    //Log.d("WidevineCasSessionMgr", "wcs openInternal()--start");//eric lin test
    if (isOpen()) {
      // Already opened
      return true;
    }

    try {
      android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() want to call mediaCas.openSession()");//eric lin test
      session = mediaCas.openSession();
      if (casContentId != null && casProvider != null) {
        CaDescriptorPrivateData casPrivateData = CaDescriptorPrivateData.newBuilder()
                .setContentId(ByteString.copyFrom(casContentId.getBytes()))
                .setProvider(casProvider)
                .build();
        //android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() casContentId.length="+casContentId.length());//eric lin test
        //android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() string casContentId="+casContentId);//eric lin test
        //android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() casContentId="+toHexString(casContentId.getBytes()));//eric lin test
        //android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() casProvider.length="+casProvider.length());//eric lin test
        //android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() string casProvider="+casProvider);//eric lin test
        //android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() casContentId="+toHexString(casProvider.getBytes()));//eric lin test
        //android.util.Log.d("WidevineCasSessionMgr", "wcs openInternal() setPrivateData, length="+casPrivateData.toByteArray().length);//eric lin test
        //Log.d("WidevineCasSessionMgr", toHexString(casPrivateData.toByteArray()));//String.valueOf(data[i]));//eric lin test
        mediaCas.setPrivateData(HiDtvMediaPlayer.getInstance().privateData);//mediaCas.setPrivateData(casPrivateData.toByteArray());//eric lin 20210312 set private data from service
      }

      mediaDescrambler = mediaCas.createMediaDescrambler();
      mediaDescrambler.setMediaCasSession(session);
      state = STATE_OPENED;
      //Log.d("WidevineCasSessionMgr", "wcs openInternal()--end return true");//eric lin test
      return true;
    } catch (Exception e) {
      onError(e);
    }
    //Log.d("WidevineCasSessionMgr", "wcs openInternal()--end return false");//eric lin test
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
    Log.d("WidevineCasSessionMgr", "wcs isOpen()");//eric lin test
    return state == STATE_OPENED || state == STATE_OPENED_WITH_KEYS;
  }

  private void blockUntilReady() {
    //Log.d("WidevineCasSessionMgr", "wcs blockUntilReady()--start");//eric lin test
    try {
      synchronized (lockCasReady) {
        if (casReady == false) {
          lockCasReady.wait();
          if (casReady == false) {
            Log.e(TAG, "Failed waiting for session to become ready");
          }
        }
      }
      synchronized (lockSessionReady) {
        if (sessionId == -1) {
          lockSessionReady.wait();
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed waiting for session to become ready");
      onError(e);
    }
    //Log.d("WidevineCasSessionMgr", "wcs blockUntilReady()--end");//eric lin test
  }
}
