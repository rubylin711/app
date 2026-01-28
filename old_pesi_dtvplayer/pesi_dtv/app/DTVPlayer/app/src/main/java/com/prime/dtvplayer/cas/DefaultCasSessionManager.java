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
import android.os.Handler;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.prime.dtvplayer.Activity.DTVActivity;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.DefaultCasSession.ProvisioningManager;
import com.prime.dtvplayer.cas.CasInitData.SchemeData;
import com.prime.dtvplayer.cas.CasSession.CasSessionException;
/*
import com.google.android.exoplayer2.cas.DefaultCasSession.ProvisioningManager;
import com.google.android.exoplayer2.cas.CasInitData.SchemeData;
import com.google.android.exoplayer2.cas.CasSession.CasSessionException;
 */
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.EventDispatcher;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * A {@link CasSessionManager} that supports playbacks using {@link ExoMediaCas}.
 */
@TargetApi(18)
public class DefaultCasSessionManager implements CasSessionManager, ProvisioningManager {

  /** @deprecated Use {@link DefaultCasSessionEventListener}. */
  @Deprecated
  public interface EventListener extends DefaultCasSessionEventListener {}

  /**
   * Signals that the {@link CasInitData} passed to {@link #acquireSession} does not contain does
   * not contain scheme data for the required ID.
   */
  public static final class MissingSchemeDataException extends Exception {

    private MissingSchemeDataException(int caSystemId) {
      super("Media does not support caSystemId: " + caSystemId);
    }
  }

  /** Determines the action to be done after a session acquired. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({MODE_PLAYBACK, MODE_QUERY, MODE_DOWNLOAD, MODE_RELEASE})
  public @interface Mode {}
  /**
   * Loads and refreshes (if necessary) a license for playback. Supports streaming and offline
   * licenses.
   */
  public static final int MODE_PLAYBACK = 0;
  /**
   * Restores an offline license to allow its status to be queried.
   */
  public static final int MODE_QUERY = 1;
  /** Downloads an offline license or renews an existing one. */
  public static final int MODE_DOWNLOAD = 2;
  /** Releases an existing offline license. */
  public static final int MODE_RELEASE = 3;

  private static final String TAG = "DefaultCasSessionMgr";

  private final int caSystemId;
  private final ExoMediaCas<MediaCas.Session> mediaCas;
  private final HashMap<String, String> optionalKeyRequestParameters;
  private final EventDispatcher<DefaultCasSessionEventListener> eventDispatcher;
  private final boolean multiSession;
  private final int initialCasRequestRetryCount;

  private final List<DefaultCasSession> sessions;
  private final List<DefaultCasSession> provisioningSessions;

  private int mode;

  /**
   * @param caSystemId The id of the cas scheme.
   * @param mediaCas An underlying {@link ExoMediaCas} for use by the manager.
   */
  public DefaultCasSessionManager(
      int caSystemId,
      ExoMediaCas<MediaCas.Session> mediaCas) {
    Assertions.checkArgument(caSystemId != -1);
    Assertions.checkNotNull(mediaCas);
    this.caSystemId = caSystemId;
    this.mediaCas = mediaCas;
    this.optionalKeyRequestParameters = null;
    this.eventDispatcher = new EventDispatcher<>();
    this.multiSession = true;
    this.initialCasRequestRetryCount = 0;
    mode = MODE_PLAYBACK;
    sessions = new ArrayList<>();
    provisioningSessions = new ArrayList<>();
  }

  /**
   * Adds a {@link DefaultCasSessionEventListener} to listen to cas session events.
   *
   * @param handler A handler to use when delivering events to {@code eventListener}.
   * @param eventListener A listener of events.
   */
  public final void addListener(Handler handler, DefaultCasSessionEventListener eventListener) {
    eventDispatcher.addListener(handler, eventListener);
  }

  /**
   * Removes a {@link DefaultCasSessionEventListener} from the list of cas session event listeners.
   *
   * @param eventListener The listener to remove.
   */
  public final void removeListener(DefaultCasSessionEventListener eventListener) {
    eventDispatcher.removeListener(eventListener);
  }

  // CasSessionManager implementation.

  @Override
  public void processEmm(byte[] emm) {
    android.util.Log.d("WidevineCasSessionMgr", "dcsm processEmm111");//eric lin test
    try {
      mediaCas.processEmm(emm);
    }  catch (Exception e) {
      eventDispatcher.dispatch(listener -> listener.onCasSessionManagerError(e));
    }
  }

  @Override
  public boolean canAcquireSession(int caSystemId) {
    return (caSystemId == this.caSystemId);
  }

  @Override
  public CasSession acquireSession(CasInitData casInitData, int sessionMode, int sessionIndex) {//eric lin 20210107 widevine cas, add parameter sessionIndex
    SchemeData schemeData = getSchemeData(casInitData, caSystemId, true);
    if (schemeData == null) {
      final MissingSchemeDataException error = new MissingSchemeDataException(caSystemId);
      eventDispatcher.dispatch(listener -> listener.onCasSessionManagerError(error));
      return new ErrorStateCasSession(new CasSessionException(error));
    }

    DefaultCasSession session;
    if (!multiSession) {
      session = sessions.isEmpty() ? null : sessions.get(0);
    } else {
      // Only use an existing session if it has matching init data and we are indexing
      session = null;
      byte[] initData = schemeData != null ? schemeData.data : null;
      for (DefaultCasSession existingSession : sessions) {
        if (existingSession.sessionMode == sessionMode && sessionMode == SESSION_MODE_EXTRACTOR) {
          if (existingSession.hasInitData(initData)) {
            if (existingSession.schemeData.equals(schemeData))
              session = existingSession;
            break;
          }
        }
      }
    }

    if (session == null) {
      // Create a new session.
      session =
          new DefaultCasSession(
              caSystemId,
              sessionMode,
              mediaCas,
              this,
              schemeData,
              mode,
              optionalKeyRequestParameters,
              eventDispatcher,
                  initialCasRequestRetryCount);
      sessions.add(session);
    }
    session.acquire();
    return session;
  }
  @Override
  public boolean getReadyState()//eric lin 20210107 widevine cas
  {
    return false;
  }

  @Override
  public void setHandler(DTVActivity.MainHandler mHandler)//eric lin 20210107 widevine cas
  {

  }

  @Override
  public void releaseSession(CasSession session) {
    if (session instanceof ErrorStateCasSession) {
      // Do nothing.
      return;
    }

    DefaultCasSession casSession = (DefaultCasSession) session;
    if (casSession.release()) {
      sessions.remove(casSession);
      if (provisioningSessions.size() > 1 && provisioningSessions.get(0) == casSession) {
        // Other sessions were waiting for the released session to complete a provision operation.
        // We need to have one of those sessions perform the provision operation instead.
        // provisioningSessions.get(1).provision("provision_string");
      }
      provisioningSessions.remove(casSession);
    }
  }

  // ProvisioningManager implementation.

  @Override
  public void provisionRequired(DefaultCasSession session) throws MediaCasException {
    provisioningSessions.add(session);
    if (provisioningSessions.size() == 1) {
      // This is the first session requesting provisioning, so have it perform the operation.
      session.provision();
    }
  }

  // Internal methods.

  /**
   * Extracts {@link SchemeData} suitable for the given CAS scheme {@link UUID}.
   *
   * @param casInitData The {@link CasInitData} from which to extract the {@link SchemeData}.
   * @param caSystemId The id.
   * @param allowMissingData Whether a {@link SchemeData} with null {@link SchemeData#data} may be
   *     returned.
   * @return The extracted {@link SchemeData}, or null if no suitable data is present.
   */
  private static SchemeData getSchemeData(CasInitData casInitData, int caSystemId,
                                          boolean allowMissingData) {

    SchemeData schemeData = casInitData.get();
    if (schemeData.matches(caSystemId) && (schemeData.data != null || allowMissingData)) {
      return schemeData;
    }

    return null;
  }
}
