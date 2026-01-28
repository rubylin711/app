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



//import static com.google.android.exoplayer2.extractor.ts.TsPayloadReader.FLAG_SCRAMBLING_CONTROL_EVEN_KEY; //eric lin mark
//import static com.google.android.exoplayer2.extractor.ts.TsPayloadReader.FLAG_SCRAMBLING_CONTROL_ODD_KEY; //eric lin mark

import java.nio.ByteBuffer;
import android.annotation.TargetApi;
import androidx.annotation.NonNull;
import android.media.MediaCas;
import android.media.MediaCodec;
import android.media.MediaDescrambler;
import com.google.android.exoplayer2.util.Assertions;

/**
 * An {@link ExoMediaDescrambler} implementation that wraps the framework {@link MediaDescrambler}.
 */
@TargetApi(16)
public class FrameworkMediaDescrambler implements ExoMediaDescrambler {

  private final MediaDescrambler mediaDescrambler;
  private final boolean forceAllowInsecureDecoderComponents;

  /** Indicates the TS packet is scrambled with EVEN key. */
  private final int FLAG_SCRAMBLING_CONTROL_EVEN_KEY = 1 << 3; //eric lin add from exoplayer-cas\library\core\src\main\java\com\google\android\exoplayer2\extractor\ts\TsPayloadReader.java
  /** Indicates the TS packet is scrambled with ODD key */
  private final int FLAG_SCRAMBLING_CONTROL_ODD_KEY = 1 << 4; //eric lin add from exoplayer-cas\library\core\src\main\java\com\google\android\exoplayer2\extractor\ts\TsPayloadReader.java

  /**
   * @param mediaDescrambler The {@link MediaDescrambler} to wrap.
   */
  public FrameworkMediaDescrambler(MediaDescrambler mediaDescrambler) {
    this(mediaDescrambler, false);
  }

  /**
   * @param mediaDescrambler The {@link MediaDescrambler} to wrap.
   * @param forceAllowInsecureDecoderComponents Whether to force
   *     {@link #requiresSecureDecoderComponent(String)} to return {@code false}, rather than
   *     {@link MediaDescrambler#requiresSecureDecoderComponent(String)} of the wrapped
   *     {@link MediaDescrambler}.
   */
  public FrameworkMediaDescrambler(MediaDescrambler mediaDescrambler,
                                   boolean forceAllowInsecureDecoderComponents) {
    this.mediaDescrambler = Assertions.checkNotNull(mediaDescrambler);
    this.forceAllowInsecureDecoderComponents = forceAllowInsecureDecoderComponents;
  }

  /**
   * Returns the wrapped {@link MediaDescrambler}.
   */
  @Override
  public MediaDescrambler getWrappedMediaDescrambler() {
    return mediaDescrambler;
  }

  @Override
  public boolean requiresSecureDecoderComponent(String mimeType) {
    return !forceAllowInsecureDecoderComponents
        && mediaDescrambler.requiresSecureDecoderComponent(mimeType);
  }

  @Override
  public void setMediaCasSession(MediaCas.Session session) {
    mediaDescrambler.setMediaCasSession(session);
  }

  @Override
  public byte[] descramble(@CasSessionManager.sessionMode int sessionMode, @NonNull byte[] data, int offset, int length, int polarity) {
    MediaCodec.CryptoInfo cryptoInfo = new MediaCodec.CryptoInfo();
    int[] numBytesOfClearData     = new int[] { 0 };
    int[] numBytesOfEncryptedData = new int[] { length };
    byte[] iv = new byte[16]; // not used
    byte[] key = new byte[16];
    // Indicate that the content is scrambled
    switch(polarity) {
      case FLAG_SCRAMBLING_CONTROL_EVEN_KEY:
        key[0] = MediaDescrambler.SCRAMBLE_CONTROL_EVEN_KEY;
        break;
      case FLAG_SCRAMBLING_CONTROL_ODD_KEY:
        key[0] = MediaDescrambler.SCRAMBLE_CONTROL_ODD_KEY;
        break;
      default:
        break;
    }

    if (sessionMode == CasSessionManager.SESSION_MODE_EXTRACTOR) {
      key[1] = MediaDescrambler.SCRAMBLE_FLAG_PES_HEADER;
    }
    cryptoInfo.set(1, numBytesOfClearData, numBytesOfEncryptedData,
            key, iv, MediaCodec.CRYPTO_MODE_AES_CBC);
    ByteBuffer inputBuf = ByteBuffer.wrap(data, offset, length);
    ByteBuffer outputBuf = ByteBuffer.allocate(length);
    int descrambled;
    try {
      descrambled = mediaDescrambler.descramble(inputBuf, outputBuf, cryptoInfo);
    } catch (Exception e) {
      descrambled = 0;
    }
    return (descrambled != 0) ? outputBuf.array() : null;
  }
}
