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

import android.media.MediaDescrambler;
import androidx.annotation.NonNull;
import android.media.MediaCas;

/**
 * An opaque {@link android.media.MediaDescrambler} equivalent.
 */
public interface ExoMediaDescrambler {

  MediaDescrambler getWrappedMediaDescrambler();

  /**
   * @see android.media.MediaDescrambler#requiresSecureDecoderComponent(String)
   */
  boolean requiresSecureDecoderComponent(String mimeType);

  /**
   * @see android.media.MediaDescrambler#setMediaCasSession(MediaCas.Session)
   */
  void setMediaCasSession(MediaCas.Session session);

  byte[] descramble(@CasSessionManager.sessionMode int sessionMode, @NonNull byte[] data, int offset, int length, int polarity);
}
