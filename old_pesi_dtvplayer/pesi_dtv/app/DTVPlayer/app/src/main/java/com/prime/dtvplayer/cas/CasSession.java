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
import androidx.annotation.IntDef;

import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A CAS session.
 */
@TargetApi(16)
public interface CasSession {

  /**
   * Wraps the throwable which is the cause of the error state.
   */
  class CasSessionException extends Exception {

    public CasSessionException(Throwable cause) {
      super(cause);
    }

  }

  /**
   * The state of the CAS session.
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({STATE_RELEASED, STATE_ERROR, STATE_OPENING, STATE_OPENED, STATE_OPENED_WITH_KEYS})
  public @interface State {}
  /**
   * The session has been released.
   */
  int STATE_RELEASED = 0;
  /**
   * The session has encountered an error. {@link #getError()} can be used to retrieve the cause.
   */
  int STATE_ERROR = 1;
  /**
   * The session is being opened.
   */
  int STATE_OPENING = 2;
  /**
   * The session is open, but does not yet have the keys required for decryption.
   */
  int STATE_OPENED = 3;
  /**
   * The session is open and has the keys required for decryption.
   */
  int STATE_OPENED_WITH_KEYS = 4;

  /**
   * Returns the current state of the session, which is one of {@link #STATE_ERROR},
   * {@link #STATE_RELEASED}, {@link #STATE_OPENING}, {@link #STATE_OPENED} and
   * {@link #STATE_OPENED_WITH_KEYS}.
   */
  @State int getState();

  /**
   * Returns the cause of the error state.
   */
  CasSessionException getError();

  /**
   * Returns a {@link ExoMediaDescrambler} for the open session, or null if called before the session has
   * been opened or after it's been released.
   */
  ExoMediaDescrambler getMediaDescrambler();

  byte[] descramble(@NonNull byte[] data, int offset, int length, int polarity);

  void processEcm(@NonNull EcmHolder ecmHolder);

  void processDtvEcm(@NonNull byte[] ecmData);//eric lin 20210107 widevine cas

}
