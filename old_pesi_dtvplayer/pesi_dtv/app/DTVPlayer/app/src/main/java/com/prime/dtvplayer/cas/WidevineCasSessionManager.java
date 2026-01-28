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
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.IntDef;
import android.util.Log;

import com.prime.dtvplayer.Activity.DTVActivity;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.CasInitData.SchemeData;
import com.prime.dtvplayer.cas.CasSession.CasSessionException;
import com.prime.dtvplayer.cas.WidevineCasSession.ProvisioningManager;
import com.prime.dtvplayer.cas.ExoMediaCas.OnEventListener;
import com.prime.dtvplayer.cas.ExoMediaCas.KeyRequest;
import com.prime.dtvplayer.cas.ExoMediaCas.ProvisionRequest;
/*
import com.google.android.exoplayer2.cas.CasInitData.SchemeData;
import com.google.android.exoplayer2.cas.CasSession.CasSessionException;
import com.google.android.exoplayer2.cas.WidevineCasSession.ProvisioningManager;
import com.google.android.exoplayer2.cas.ExoMediaCas.OnEventListener;
import com.google.android.exoplayer2.cas.ExoMediaCas.KeyRequest;
import com.google.android.exoplayer2.cas.ExoMediaCas.ProvisionRequest;
 */
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.EventDispatcher;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.prime.dtvplayer.Activity.DTVActivity.WV_TEST_CAS_READY;//eric lin 20210107 widevine cas
import static com.prime.dtvplayer.Activity.DTVActivity.WV_TEST_SET_SESSION_ID;//eric lin 20210107 widevine cas

/**
 * A {@link CasSessionManager} that supports playbacks using {@link ExoMediaCas}.
 */
@TargetApi(18)
public class WidevineCasSessionManager implements CasSessionManager, ProvisioningManager {

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

  public static final int INDIVIDUALIZATION_REQUEST = 1000;
  public static final int INDIVIDUALIZATION_RESPONSE = 1001;
  public static final int INDIVIDUALIZATION_COMPLETE = 1002;

  public static final int LICENSE_REQUEST = 2000;
  public static final int LICENSE_RESPONSE = 2001;
  public static final int CAS_ERROR_DEPRECATED = 2002;
  public static final int LICENSE_CAS_READY = 2006;

  public static final int CAS_SESSION_ID = 3000;
  public static final int SET_CAS_SOC_ID = 3001;

  public static final int CAS_ERROR = 5000;

  private static final String TAG = "WidevineCasSessionMgr";

  private final int caSystemId;
  private String casProvisioning;
  private String casContentId;
  private String casProvider;
  private MediaCasCallback casCallback;
  private final ExoMediaCas<MediaCas.Session> mediaCas;
  private final HashMap<String, String> optionalKeyRequestParameters;
  private final EventDispatcher<DefaultCasSessionEventListener> eventDispatcher;
  private final boolean multiSession;
  private final int initialCasRequestRetryCount;

  private final List<WidevineCasSession> sessions;
  private WidevineCasSession openingSession;

  private int mode;
  private HandlerThread provisionHandlerThread;
  private boolean provisioned = false;
  private boolean ready = false;
  private final Object lockProvisioning;
  private final Object lockOpening;
  public DTVActivity.MainHandler mHandler;//eric lin 20210107 widevine cas

  /* package */ volatile MediaCasHandler mediaCasHandler;
  /**
   * @param caSystemId The id of the cas scheme.
   * @param mediaCas An underlying {@link ExoMediaCas} for use by the manager.
   */

  @Override
  public void setHandler(DTVActivity.MainHandler handler)//eric lin 20210107 widevine cas
  {
    mHandler = handler;
  }

  public WidevineCasSessionManager(
      int caSystemId,
      String casProvisioning,
      String casContentId,
      String casProvider,
      MediaCasCallback casCallback,
      ExoMediaCas<MediaCas.Session> mediaCas) {
    Log.d("WidevineCasSessionMgr", "wcsm WidevineCasSessionManager()--start");//eric lin test
    Assertions.checkArgument(caSystemId != -1);
    Assertions.checkNotNull(mediaCas);
    this.caSystemId = caSystemId;
    this.casProvisioning = casProvisioning;
    this.casCallback = casCallback;
    this.casContentId = casContentId;
    this.casProvider = casProvider;
    this.mediaCas = mediaCas;
    this.optionalKeyRequestParameters = null;
    this.eventDispatcher = new EventDispatcher<>();
    this.multiSession = true;
    this.initialCasRequestRetryCount = 0;
    mode = MODE_PLAYBACK;
    sessions = new ArrayList<>();
    //mediaCas.setOnEventListener(new MediaCasEventListener());//eric lin 20201217 cas listener workaround, move
    lockProvisioning = new Object();
    lockOpening = new Object();
    Log.d("WidevineCasSessionMgr", "wcsm WidevineCasSessionManager() new HandlerThread");//eric lin test
    provisionHandlerThread = new HandlerThread("CasProvisionHandler");
    Log.d("WidevineCasSessionMgr", "wcsm WidevineCasSessionManager() provisionHandlerThread.start()");//eric lin test
    provisionHandlerThread.start();
    if (mediaCasHandler == null) {//eric lin 20201217 cas listener workaround, add 
      Log.d("WidevineCasSessionMgr", "wcsm mediaCasHandler == null, so new");
      mediaCasHandler = new MediaCasHandler(provisionHandlerThread.getLooper());
    }
    mediaCas.setOnEventListener(new MediaCasEventListener(), mediaCasHandler);//eric lin 20201217 cas listener workaround, move

    Log.d("WidevineCasSessionMgr", "wcsm WidevineCasSessionManager()--end");//eric lin test
  }

  /**
   * Adds a {@link DefaultCasSessionEventListener} to listen to cas session events.
   *
   * @param handler A handler to use when delivering events to {@code eventListener}.
   * @param eventListener A listener of events.
   */
  public final void addListener(Handler handler, DefaultCasSessionEventListener eventListener) {
    Log.d("WidevineCasSessionMgr", "wcsm addListener--start");//eric lin test
    eventDispatcher.addListener(handler, eventListener);
  }

  /**
   * Removes a {@link DefaultCasSessionEventListener} from the list of cas session event listeners.
   *
   * @param eventListener The listener to remove.
   */
  public final void removeListener(DefaultCasSessionEventListener eventListener) {
    Log.d("WidevineCasSessionMgr", "wcsm removeListener--start");//eric lin test
    eventDispatcher.removeListener(eventListener);
  }

  // CasSessionManager implementation.

  @Override
  public void processEmm(byte[] emm) {
    Log.d("WidevineCasSessionMgr", "wcsm processEmm()--start");//eric lin test
    try {
      mediaCas.processEmm(emm);
    }  catch (Exception e) {
      eventDispatcher.dispatch(listener -> listener.onCasSessionManagerError(e));
    }
    Log.d("WidevineCasSessionMgr", "wcsm processEmm()--end");//eric lin test
  }

  @Override
  public boolean canAcquireSession(int caSystemId) {
    Log.d("WidevineCasSessionMgr", "wcsm canAcquireSession--start");//eric lin test
    return (caSystemId == this.caSystemId);
  }

  @Override
  public CasSession acquireSession(CasInitData casInitData, int sessionMode, int sessionIndex) {//eric lin 20210107 widevine cas
    Log.d("WidevineCasSessionMgr", "wcsm acquireSession()--start");//eric lin test
    if (sessions.isEmpty()) {
      if (mediaCasHandler == null) {
        Log.d("WidevineCasSessionMgr", "wcsm acquireSession() mediaCasHandler == null, so new");
        mediaCasHandler = new MediaCasHandler(provisionHandlerThread.getLooper());
      }
    }
    android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() want to call is_provisioned()--1");//eric lin test
    is_provisioned();
    android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() want to call getSchemeData()");//eric lin test
    SchemeData schemeData = getSchemeData(casInitData, caSystemId, true);
    if (schemeData == null) {
      Log.d("WidevineCasSessionMgr", "wcsm acquireSession() schemeData == NULL, error");
      final MissingSchemeDataException error = new MissingSchemeDataException(caSystemId);
      eventDispatcher.dispatch(listener -> listener.onCasSessionManagerError(error));
      Log.d("WidevineCasSessionMgr", "wcsm acquireSession()--end return error1");//eric lin test
      return new ErrorStateCasSession(new CasSessionException(error));
    }

    WidevineCasSession session;
    if (!multiSession) {
      session = sessions.isEmpty() ? null : sessions.get(0);
      //if(session == null)
      //  android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() !multiSession=TRUE session == null");//eric lin test
      //else
      //  android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() !multiSession=TRUE session != null");//eric lin test
    } else {
      //android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() !multiSession=FALSE");//eric lin test
      // Only use an existing session if it has matching init data and we are indexing
      session = null;
      byte[] initData = schemeData != null ? schemeData.data : null;
      for (WidevineCasSession existingSession : sessions) {
        android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() for 1");//eric lin test
        if (existingSession.sessionMode == sessionMode && sessionMode == SESSION_MODE_EXTRACTOR) {
          android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() for 2");//eric lin test
          if (existingSession.hasInitData(initData)) {
            android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() for 3");//eric lin test
            if (existingSession.schemeData.equals(schemeData)) {
              session = existingSession;
              if (session == null)
                android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() session = existingSession, session == null");//eric lin test
              else
                android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() session = existingSession, session != null");//eric lin test
            break;
          }
        }
      }
    }
    }

    if (session == null) {
      android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() session==null");//eric lin test
      synchronized (lockOpening) {
        if (openingSession != null) {
          try {
            lockOpening.wait();
          }
          catch (Exception ex) {
            Log.e(TAG, "error waiting for cas session to open: " + ex.toString());
          }
        }
        android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() want to new WidevineCasSession()");//eric lin test
        // Create a new session.
        session =
            new WidevineCasSession(
                caSystemId,
                sessionMode,
                mediaCas,
                casProvisioning,
                casContentId,
                casProvider,
                this,
                schemeData,
                mode,
                optionalKeyRequestParameters,
                eventDispatcher,
                initialCasRequestRetryCount);
        sessions.add(session);
        session.sessionIndex = sessionIndex;//eric lin 20210107 widevine cas
        android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() AAA session.sessionIndex="+session.sessionIndex);//eric lin test
        openingSession = session;
        android.util.Log.d("WidevineCasSessionMgr", "wcsm acquireSession() AAA openingSession.sessionIndex="+openingSession.sessionIndex);//eric lin test

        if (ready) {
          session.notifyCasReady(true);
        }
      }
    }

    Log.d("WidevineCasSessionMgr", "wcsm acquireSession() want to call session.acquire()");//eric lin test
    session.acquire();
    Log.d("WidevineCasSessionMgr", "wcsm acquireSession()--end");//eric lin test
    return session;
  }

  @Override
  public void releaseSession(CasSession session) {
    Log.d("WidevineCasSessionMgr", "wcsm releaseSession()--start");//eric lin test

    if(session == null) {//eric lin 20210107 widevine cas, add
      Log.d("WidevineCasSessionMgr", "wcsm releaseSession() session == null, return");
      return;
    }
    if (session instanceof ErrorStateCasSession) {
      // Do nothing.
      Log.d("WidevineCasSessionMgr", "wcsm releaseSession()--end do nothing");//eric lin test
      return;
    }

    WidevineCasSession casSession = (WidevineCasSession) session;
    if (casSession.release()) {
      sessions.remove(casSession);
    }

    if (sessions.isEmpty()) {
      provisionHandlerThread.quit();
      provisionHandlerThread = null;
    }
    Log.d("WidevineCasSessionMgr", "wcsm releaseSession()--end");//eric lin test
  }

  // ProvisioningManager implementation.

  @Override
  public void provisionRequired(WidevineCasSession session) throws MediaCasException {
    Log.d("WidevineCasSessionMgr", "wcsm provisionRequired--start");//eric lin test
    provisioned = false;
    android.util.Log.d("WidevineCasSessionMgr", "wcsm want to call is_provisioned()--2");//eric lin test
    is_provisioned();
    Log.d("WidevineCasSessionMgr", "wcsm provisionRequired--end");//eric lin test
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
    //Log.d("WidevineCasSessionMgr", "wcsm getSchemeData()--start");//eric lin test
    SchemeData schemeData = casInitData.get();
    if (schemeData.matches(caSystemId) && (schemeData.data != null || allowMissingData)) {
      //Log.d("WidevineCasSessionMgr", "wcsm getSchemeData() provisionRequired--end return schemeData");//eric lin test
      return schemeData;
    }
    //Log.d("WidevineCasSessionMgr", "wcsm getSchemeData() provisionRequired--end return null");//eric lin test
    return null;
  }

  private boolean is_provisioned() {
    //Log.d("WidevineCasSessionMgr", "wcsm is_provisioned()--start");//eric lin test
    synchronized (lockProvisioning) {

      if (provisioned) {
        //Log.d("WidevineCasSessionMgr", "wcsm is_provisioned()--end return true");//eric lin test
        return true;
      }

      Log.d(TAG, "wcsm is_provisioned() Start provision");
      try {
        mediaCas.provision("");
      }
      catch (Exception ex) {
        Log.e(TAG, "Exception occurred calling provision: " + ex.toString());
        Log.d("WidevineCasSessionMgr", "wcsm is_provisioned()--end return false1");//eric lin test
        return false;
      }

      try {
        lockProvisioning.wait();
      } catch (Exception ex) {
        Log.e(TAG, "Exception occurred waiting for provision: " + ex.toString());
        Log.d("WidevineCasSessionMgr", "wcsm is_provisioned()--end return false2");//eric lin test
        return false;
      }
    }

    if (provisioned) {
      Log.d(TAG, "wcsm is_provisioned() Provision complete");
    } else {
      Log.d(TAG, "wcsm is_provisioned() Failed to provision");
    }
    //Log.d("WidevineCasSessionMgr", "wcsm is_provisioned()--end return provisioned, last line");//eric lin test
    return provisioned;
  }

  private void sendMessage(Handler handler, int what, int arg1, int arg2, Object obj)//eric lin 20210107 widevine cas
  {
    Message msg = new Message();
    msg.what = what;
    msg.arg1 = arg1;
    msg.arg2 = arg2;
    msg.obj = obj;
    handler.sendMessage(msg);
  }

  private void setReadyState(boolean ready) {
    Log.d("WidevineCasSessionMgr", "wcsm setReadyState()--start, ready="+ready);//eric lin test
    this.ready = ready;
    for (WidevineCasSession session : sessions) {
      session.notifyCasReady(ready);
    }
    if(mHandler != null) {//eric lin 20210107 widevine cas
      sendMessage(mHandler, WV_TEST_CAS_READY, 0, 0, 0);
    }
    Log.d("WidevineCasSessionMgr", "wcsm setReadyState()--end");//eric lin test
  }

  public boolean getReadyState() {//eric lin add
    return this.ready;
  }

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
  public void handleCasEvent(Message msg) {
    Log.d(TAG, "wcsm handleCasEvent()--start");
    Log.d(TAG, "CAS event received: " + msg.arg1+"--start");
    //android.util.Log.d("afaaexo", "CAS event received: " + msg.arg1);//eric lin test
    switch(msg.arg1) {

      case CAS_SESSION_ID: {
        int sessionId = msg.arg2;
        Log.d(TAG, "Plugin session ID(msg.arg2) event. ID: " + sessionId);

        byte[] casId = ByteBuffer.allocate(4).putInt(sessionId).array();
        Log.d(TAG, "Setting " + sessionId + " as CAS SOC ID");
        //Log.d(TAG, "casId length="+casId.length);
        try {
          android.util.Log.d(TAG, "sendEvent SET_CAS_SOC_ID(3001)," + "arg(sessionId)="+sessionId+", data(casId)=");//eric lin test
          Log.d(TAG, toHexString(casId));
          mediaCas.sendEvent(SET_CAS_SOC_ID, sessionId, casId);
          if(mHandler != null) {//eric lin 20210107 widevine cas
            sendMessage(mHandler, WV_TEST_SET_SESSION_ID, openingSession.sessionIndex, sessionId, 0);
          }
        } catch (Exception ex) {
          Log.e(TAG, "Exception on sending SET_CAS_SOC_ID event: " + ex.toString());
        }
        android.util.Log.d("WidevineCasSessionMgr", "wcsm handleCasEvent() CAS_SESSION_ID: openingSession.sessionIndex="+openingSession.sessionIndex);//eric lin test
        openingSession.setSessionId(sessionId);
        synchronized (lockOpening) {
          openingSession = null;
          lockOpening.notifyAll();
        }
        break;
      }

      case INDIVIDUALIZATION_REQUEST: {
        //Log.d(TAG, "Individualization request event from plugin, msg.obj length=" + ((byte[])msg.obj).length +", msg.obj=");
        //Log.d(TAG, toHexString((byte[]) msg.obj));//String.valueOf(data[i]));//eric lin test
        //Log.d(TAG, "casProvisioning=" + casProvisioning);//eric lin test
        ProvisionRequest request = new ProvisionRequest((byte[]) msg.obj, casProvisioning);
        //Log.d(TAG, "defaultUrl=");
        try {
          byte[] provisionResponse = casCallback.executeProvisionRequest(new UUID(0, 0), request);
          //android.util.Log.d(TAG, "WidevicecasManager sendEvent INDIVIDUALIZATION_RESPONSE(1001), arg=0, data(provisionResponse)=");//eric lin test
          //android.util.Log.d(TAG, toHexString(provisionResponse));//eric lin test
          mediaCas.sendEvent(INDIVIDUALIZATION_RESPONSE, 0, provisionResponse);
        } catch (Exception ex) {
          Log.e(TAG, "Exception on sending INDIVIDUALIZATION_RESPONSE event: " + ex.toString());
          synchronized (lockProvisioning) {
            lockProvisioning.notifyAll();
          }
        }
        break;
      }

      case INDIVIDUALIZATION_COMPLETE:
        Log.d(TAG, "Individualization complete event");
        try {
          synchronized (lockProvisioning) {
            provisioned = true;
            lockProvisioning.notifyAll();
          }
        } catch (Exception ex) {
          Log.e(TAG, "Exception sending notify: " + ex.toString());
        }
        break;

      case LICENSE_REQUEST:
        Log.d(TAG, "Licence request event");
        //Log.d(TAG, "LICENSE_REQUEST event, msg.obj.length=" + ((byte[])msg.obj).length + ", msg.obj=");
        //Log.d(TAG, toHexString((byte[])msg.obj));

        KeyRequest request = new KeyRequest((byte[])msg.obj, "");
        byte[] licence = null;
        try {
          licence = casCallback.executeKeyRequest(new UUID(0,0), request);
        } catch (Exception ex) {
          Log.e(TAG, "Failed to parse licence string: " + ex.toString());
        }

        if (licence == null) {
          setReadyState(false);
          return;
        }

        Log.d(TAG, "Sending licence response to plugin");
        try {
          //android.util.Log.d(TAG, "WidevicecasManager sendEvent LICENSE_RESPONSE(2001) arg=0, data(licence)=");//eric lin test
          //Log.d(TAG, toHexString(licence));
          mediaCas.sendEvent(LICENSE_RESPONSE, 0, licence);
        } catch (Exception ex) {
          Log.e(TAG, "Exception on sending LICENSE_RESPONSE event: " + ex.toString());
          setReadyState(false);
        }
        break;

      case LICENSE_CAS_READY:
        Log.d(TAG, "CAS ready event received from plugin");
        setReadyState(true);
        break;

      case CAS_ERROR:
      case CAS_ERROR_DEPRECATED:
        Log.d(TAG, "CAS ERROR event received from plugin");
        setReadyState(false);
        break;

      default:
        Log.d(TAG, "Unhandled event: " + msg.arg1);
    }
    Log.d(TAG, "wcsm handleCasEvent()--end");
  }

//  @SuppressLint("HandlerLeak")
  private class MediaCasHandler extends Handler {

    public MediaCasHandler(Looper looper) {
      super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        handleCasEvent(msg);
    }

  }

  private class MediaCasEventListener implements OnEventListener {

    @Override
    public void onEvent(ExoMediaCas mc, int event, int extra,
        byte[] data) {
        //android.util.Log.d(TAG, "wcsm onEvent(), " + "event="+event+", extra="+extra);//eric lin test
        mediaCasHandler.obtainMessage(0, event, extra, data).sendToTarget();
    }

  }
}
