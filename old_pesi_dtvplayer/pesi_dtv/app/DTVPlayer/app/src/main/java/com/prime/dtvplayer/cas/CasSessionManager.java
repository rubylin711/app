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

import com.prime.dtvplayer.Activity.DTVActivity;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.CasInitData.SchemeData;
//import com.google.android.exoplayer2.cas.CasInitData.SchemeData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Manages a CAS session.
 */
@TargetApi(16)
public interface CasSessionManager {

  void setHandler(DTVActivity.MainHandler mHandler);//eric lin 20210107 widevine cas
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({SESSION_MODE_EXTRACTOR, SESSION_MODE_RENDERER})
  public @interface sessionMode {}
  int SESSION_MODE_EXTRACTOR = 0;
  int SESSION_MODE_RENDERER = 1;

  /**
   * Returns whether the manager is capable of acquiring a session for the given
   * {@link CasInitData}.
   *
   * @param caSystemId CAS system ID.
   * @return Whether the manager is capable of acquiring a session for the given
   *     {@link CasInitData}.
   */
  boolean canAcquireSession(int caSystemId);

  /**
   * Process EMM section.
   *
   * @param emm EMM section.
   */
  void processEmm(byte[] emm);

  /**
   * Acquires a {@link CasSession} for the specified {@link CasInitData}. The {@link CasSession}
   * must be returned to {@link #releaseSession(CasSession)} when it is no longer required.
   *
   * @param casInitData CAS initialization data. All contained {@link SchemeData}s must contain
   *     non-null {@link SchemeData#data}.
   * @return The CAS session.
   */
  //eric lin 20210107 widevine cas, add parameter sessionIndex
  CasSession acquireSession(CasInitData casInitData, @sessionMode int sessionMode, int sessionIndex);

  boolean getReadyState();//eric lin 20210107 widevine cas
  /**
   * Releases a {@link CasSession}.
   */
  void releaseSession(CasSession casSession);

}
