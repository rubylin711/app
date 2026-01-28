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

import android.media.MediaCas;
import android.media.MediaCasException;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Used to obtain keys for decrypting protected media streams. See {@link android.media.MediaCas}.
 */
public interface ExoMediaCas<T> {

  interface OnEventListener {
    /**
     * Called when an event occurs that requires the app to be notified
     *
     * @param mediaCas The {@link ExoMediaCas} object on which the event occurred.
     * @param sessionId The CAS session ID on which the event occurred.
     * @param event Indicates the event type.
     * @param extra A secondary error code.
     * @param data Optional byte array of data that may be associated with the event.
     */
    void onEvent(
        ExoMediaCas mediaCas,
        int event,
        int extra,
        @Nullable byte[] data);
  }

  interface OnKeyStatusChangeListener {
    /**
     * Called when the keys in a session change status, such as when the license is renewed or
     * expires.
     *
     * @param mediaCas The {@link ExoMediaCas} object on which the event occurred.
     * @param sessionId The CAS session ID on which the event occurred.
     * @param exoKeyInformation A list of {@link KeyStatus} that contains key ID and status.
     * @param hasNewUsableKey Whether a new key became usable.
     */
    void onKeyStatusChange(
        ExoMediaCas mediaCas,
        byte[] sessionId,
        List<KeyStatus> exoKeyInformation,
        boolean hasNewUsableKey);
  }

  final class KeyStatus {

    private final int statusCode;
    private final byte[] keyId;

    public KeyStatus(int statusCode, byte[] keyId) {
      this.statusCode = statusCode;
      this.keyId = keyId;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public byte[] getKeyId() {
      return keyId;
    }

  }

  final class KeyRequest {

    private final byte[] data;
    private final String licenseServerUrl;

    public KeyRequest(byte[] data, String licenseServerUrl) {
      this.data = data;
      this.licenseServerUrl = licenseServerUrl;
    }

    public byte[] getData() {
      return data;
    }

    public String getLicenseServerUrl() {
      return licenseServerUrl;
    }

  }

  final class ProvisionRequest {

    private final byte[] data;
    private final String defaultUrl;

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
    public ProvisionRequest(byte[] data, String defaultUrl) {
      //int j=0;
      //Log.d("WidevineCasSessionMgr", "ProvisionRequest, defaultUrl="+defaultUrl);//eric lin test
      //Log.d("WidevineCasSessionMgr", "ProvisionRequest, data length="+data.length);//eric lin test
      //char testData[216];

      //for (int i = 0; i < data.length; ) {
      //  Log.d("WidevineCasSessionMgr", toHexString(data));//String.valueOf(data[i]));//eric lin test
      //}

      this.data = data;
      this.defaultUrl = defaultUrl;
    }

    public byte[] getData() {
      return data;
    }

    public String getDefaultUrl() {
      return defaultUrl;
    }

  }

  /**
   * @see MediaCas#openSession()
   */
  public T openSession() throws MediaCasException;

  public void provision(@NonNull String provisionString) throws MediaCasException;

  public void processEmm(@NonNull byte[] ecm) throws MediaCasException;
  public void setPrivateData(byte[] data) throws MediaCasException;
  public void sendEvent(int event, int arg, byte[] data) throws MediaCasException;

  public void setOnEventListener(OnEventListener listener, Handler handler);//eric lin 20201217 cas listener workaround, add parameter handler

  /**
   * @see android.media.MediaDescrambler#MediaDescrambler(int))
   *
   * @param initData Opaque initialization data specific to the descrambler scheme.
   * @return An object extends {@link ExoMediaDescrambler}, using opaque descrambler scheme specific data.
   * @throws MediaCasException If the instance can't be created.
   */
  ExoMediaDescrambler createMediaDescrambler() throws MediaCasException;

}
