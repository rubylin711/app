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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class EcmHolder {

  public final byte[] currentEcm;
  public final byte[] previousEcm;

  private final byte[] oddEcm;
  private final byte[] evenEcm;
  private final int oddEcmHash;
  private final int evenEcmHash;

  private final static int TID_EVEN = 0x80;
  private final static int TID_ODD = 0x81;

  public byte[] getcurrentEcm()//eric lin test
  {
    return currentEcm;
  }
  public byte[] getpreviousEcm()//eric lin test
  {
    return previousEcm;
  }
  public byte[] getOddEcm()//eric lin test
  {
    return oddEcm;
  }
  public byte[] getEvenEcm()//eric lin test
  {
    return evenEcm;
  }
  public EcmHolder UpdateEcm(@NonNull byte[] data, int offset, int length) {
    int newEcmTid = data[0] & 0xFF;
    int newEcmHash = hashCode(data, offset, length);
    byte[] newEcm;

    switch (newEcmTid) {
      case TID_ODD:
        if (newEcmHash == oddEcmHash) {
          return this;
        }

        newEcm = new byte[length];
        System.arraycopy(data, offset, newEcm, 0, length);
        return new EcmHolder(newEcm, evenEcm, newEcm, evenEcm, newEcmHash, evenEcmHash);

      case TID_EVEN:
        if (newEcmHash == evenEcmHash) {
          return this;
        }

        newEcm = new byte[length];
        System.arraycopy(data, offset, newEcm, 0, length);
        return new EcmHolder(oddEcm, newEcm, newEcm, oddEcm, oddEcmHash, newEcmHash);

      default:
        return this;
    }
  }

  public EcmHolder() {
    this.oddEcm = null;
    this.evenEcm = null;
    this.previousEcm = null;
    this.currentEcm = null;
    this.oddEcmHash = 0;
    this.evenEcmHash = 0;
  }

  public EcmHolder(@Nullable byte[] oddEcm, @Nullable byte[] evenEcm,
      byte[] currentEcm, @Nullable byte[] previousEcm, int oddEcmHash, int evenEcmHash) {
    this.oddEcm = oddEcm;
    this.evenEcm = evenEcm;
    this.currentEcm = currentEcm;
    this.previousEcm = previousEcm;
    this.oddEcmHash = oddEcmHash;
    this.evenEcmHash = evenEcmHash;
  }

  private static int hashCode(byte a[], int offset, int length) {

    int result = 1;
    int end = offset + length;
    for (int i = offset; i < end; i++)
      result = 31 * result + a[i];

    return result;
  }
}
