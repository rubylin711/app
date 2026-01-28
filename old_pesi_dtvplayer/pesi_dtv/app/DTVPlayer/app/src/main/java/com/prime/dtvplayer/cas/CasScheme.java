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

import androidx.annotation.Nullable;

public final class CasScheme {

  int id;
  byte[] data;
  public static final int GOOGLE_CASID = 0xF6D8;//eric lin copy from exoplayer\exoplayer-cas\library\core\src\main\java\com\google\android\exoplayer2\C.java
  public static final int MOCK_CASID = 0xFFFF;//eric lin copy from exoplayer\exoplayer-cas\library\core\src\main\java\com\google\android\exoplayer2\C.java
  public static final int WIDEVINE_CASID = 0x4AD4;//eric lin copy from exoplayer\exoplayer-cas\library\core\src\main\java\com\google\android\exoplayer2\C.java

  public CasScheme(int id, byte[] data) {
    this.id = id;
    this.data = data;
  }

  public String CasSchemeGetProvisionString() {
    String sProvisionStr = null;

    switch (id) {
      case GOOGLE_CASID: //C.GOOGLE_CASID://eric lin modify
        sProvisionStr =
                "{                                                           " +
                        "  \"id\": 21140844,                                 " +
                        "  \"name\": \"Test Title\",                         " +
                        "  \"lowercase_organization_name\": \"Android\",     " +
                        "  \"asset_key\": {                                  " +
                        "  \"encryption_key\": \"nezAr3CHFrmBR9R8Tedotw==\"  " +
                        "  },                                                " +
                        "  \"cas_type\": 1,                                  " +
                        "  \"track_types\": [ ]                              " +
                        "}                                                   ";
        break;
      default:
        sProvisionStr = null;
    }

    return sProvisionStr;
  }

}
