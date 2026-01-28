/*
 * Copyright (C) 2017 The Android Open Source Project
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

import androidx.annotation.NonNull;
import com.google.android.exoplayer2.util.Assertions;

/** A {@link CasSession} that's in a terminal error state. */
public final class ErrorStateCasSession implements CasSession {

  private final CasSessionException error;

  public ErrorStateCasSession(CasSessionException error) {
    this.error = Assertions.checkNotNull(error);
  }

  @Override
  public int getState() {
    return STATE_ERROR;
  }

  @Override
  public CasSessionException getError() {
    return error;
  }

  @Override
  public ExoMediaDescrambler getMediaDescrambler() {
    return null;
  }

  @Override
  public byte[] descramble(@NonNull byte[] data, int offset, int length, int polarity) { return null; }

  @Override
  public void processEcm(@NonNull EcmHolder ecmHolder) { }

  @Override
  public void processDtvEcm(@NonNull byte[] ecmData)//eric lin 20210107 widevine cas
  {

  }

}
