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
import android.media.MediaDescrambler;
import android.os.Handler;//eric lin 20201217 cas listener workaround
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.exoplayer2.util.Assertions;


/**
 * An {@link ExoMediaCas} implementation that wraps the framework {@link MediaCas}.
 */
@TargetApi(23)
public class FrameworkMediaCas implements ExoMediaCas<MediaCas.Session> {

  protected final int casSchemeId;
  protected final MediaCas mediaCas;

  /**
   * Creates an instance for the specified scheme id.
   *
   * @param casSchemeId The scheme id.
   * @return The created instance.
   * @throws UnsupportedCasException If the CAS scheme is unsupported or cannot be instantiated.
   */
  public static ExoMediaCas<MediaCas.Session> newInstance(int casSchemeId) throws UnsupportedCasException {
    try {
      android.util.Log.d("WidevicecasManager", "fmc new FrameworkMediaCas casSchemeId=" + casSchemeId);//eric lin test
      return new FrameworkMediaCas(casSchemeId);
    } catch (UnsupportedCasException e) {
      throw new UnsupportedCasException(UnsupportedCasException.REASON_UNSUPPORTED_SCHEME, e);
    } catch (Exception e) {
      throw new UnsupportedCasException(UnsupportedCasException.REASON_INSTANTIATION_ERROR, e);
    }
  }

  protected FrameworkMediaCas(int casSchemeId) throws UnsupportedCasException {
    Assertions.checkArgument(!(casSchemeId == 0), "CAS ID 0 invalid");
    this.casSchemeId = casSchemeId;
    try {
      android.util.Log.d("WidevineCasSessionMgr", "fmc FrameworkMediaCas() want to new MediaCas, casSchemeId=" + casSchemeId);//eric lin test
      mediaCas = new MediaCas(this.casSchemeId);
    } catch(Exception e) {
      android.util.Log.d("WidevineCasSessionMgr", "fmc new MediaCas ERROR!!! xxx");//eric lin test
      throw new UnsupportedCasException(UnsupportedCasException.REASON_INSTANTIATION_ERROR, e);
    }
  }

  @Override
  public MediaCas.Session openSession() throws MediaCasException {
    android.util.Log.d("WidevineCasSessionMgr", "fmc opensession() return mediaCas.openSession()");//eric lin test
    return mediaCas.openSession();
  }

  @Override
  public void setOnEventListener(
      final ExoMediaCas.OnEventListener listener, Handler handler) {//eric lin 20201217 cas listener workaround, add parameter handler
    if(listener == null)
    android.util.Log.d("WidevineCasSessionMgr", "fmc setOnEventListener(),  listener == null");//eric lin test
    else
      android.util.Log.d("WidevineCasSessionMgr", "fmc setOnEventListener(), listener != null");//eric lin test
    mediaCas.setEventListener(
        listener == null
            ? null
            : (mediaCas, event, extra, data) ->
                listener.onEvent(FrameworkMediaCas.this, event, extra, data), handler);//null);//eric lin 20201217 cas listener workaround, add parameter handler
  }

  @Override
  public void provision(@NonNull String provisionString) throws MediaCasException {
    android.util.Log.d("WidevineCasSessionMgr", "FMC provision, provisionString="+provisionString);//eric lin test
    mediaCas.provision(provisionString);
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
  @Override
  public void setPrivateData(byte[] data) throws MediaCasException {
    android.util.Log.d("WidevineCasSessionMgr", "fmc setPrivateData() data.length="+data.length);//eric lin test
    Log.d("WidevineCasSessionMgr", toHexString(data));//String.valueOf(data[i]));//eric lin test
    mediaCas.setPrivateData(data);
  }

  @Override
  public void sendEvent(int event, int arg, byte[] data) throws MediaCasException {
    android.util.Log.d("afaaexo", "framework sendEvent");//eric lin test
    mediaCas.sendEvent(event, arg, data);
  }

  @Override
  public void processEmm(@NonNull byte[] emm) throws MediaCasException {
    android.util.Log.d("WidevineCasSessionMgr", "fmc processEmm");//eric lin test
    mediaCas.processEmm(emm);
  }

  @Override
  public FrameworkMediaDescrambler createMediaDescrambler() throws MediaCasException {
    return new FrameworkMediaDescrambler(new MediaDescrambler(casSchemeId), false);
  }
}
