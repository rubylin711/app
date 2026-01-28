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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.prime.dtvplayer.cas.CasInitData.SchemeData;
//import com.google.android.exoplayer2.cas.CasInitData.SchemeData;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Initialization data for one or more CAS schemes.
 */
public final class CasInitData implements Comparator<SchemeData>, Parcelable {

  private final SchemeData schemeData;

  // Lazily initialized hashcode.
  private int hashCode;

  public CasInitData(SchemeData schemeData) {
    this.schemeData = schemeData;
  }

  /* package */ CasInitData(Parcel in) {
    schemeData = in.readParcelable(SchemeData.class.getClassLoader());
  }

  /**
   * Retrieves the {@link SchemeData} at a given index.
   *
   * @return The {@link SchemeData} at the specified index.
   */
  public SchemeData get() {
    return schemeData;
  }


  @Override
  public int hashCode() {
    if (hashCode == 0) {
      int result = schemeData.hashCode;
      hashCode = result;
    }
    return hashCode;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CasInitData other = (CasInitData) obj;
    return schemeData.equals(other.schemeData);
  }

  @Override
  public int compare(SchemeData first, SchemeData second) {
    return (first.caSystemId == second.caSystemId) ? 1 : 0;
  }

  // Parcelable implementation.

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    schemeData.writeToParcel(dest, 0);
  }

  public static final Parcelable.Creator<CasInitData> CREATOR =
      new Parcelable.Creator<CasInitData>() {

    @Override
    public CasInitData createFromParcel(Parcel in) {
      return new CasInitData(in);
    }

    @Override
    public CasInitData[] newArray(int size) {
      return new CasInitData[size];
    }

  };

  // Internal methods.

  private static boolean containsSchemeDataWithId(
    SchemeData data, int caSystemId) {
    if (data.caSystemId == caSystemId) {
      return true;
    }
    return false;
  }

  /**
   * Scheme initialization data.
   */
  public static final class SchemeData implements Parcelable {

    // Lazily initialized hashcode.
    private int hashCode;

    /**
     * The id of the CAS scheme.
     */
    private final int caSystemId;
    /**
     * The initialization data. May be null for scheme support checks only.
     */
    public final byte[] data;
    /**
     * Whether secure decryption is required.
     */
    public final boolean requiresSecureDecryption;

    /**
     * @param caSystemId The id of the CAS scheme.
     * @param data See {@link #data}.
     */
    public SchemeData(int caSystemId, byte[] data) {
      this(caSystemId, data, false);
    }

    /**
     * @param caSystemId The id of the CAS scheme
     * @param data See {@link #data}.
     * @param requiresSecureDecryption See {@link #requiresSecureDecryption}.
     */
    public SchemeData(
        int caSystemId,
        byte[] data,
        boolean requiresSecureDecryption) {
      Assertions.checkArgument(caSystemId != 0);
      this.caSystemId = caSystemId;
      this.data = data;
      this.requiresSecureDecryption = requiresSecureDecryption;
    }

    /* package */ SchemeData(Parcel in) {
      caSystemId = in.readInt();
      data = in.createByteArray();
      requiresSecureDecryption = in.readByte() != 0;
    }

    /**
     * Returns whether this initialization data applies to the specified scheme.
     *
     * @param caSystemId The id of the CAS scheme
     * @return Whether this initialization data applies to the specified scheme.
     */
    public boolean matches(int caSystemId) {
      return (this.caSystemId == caSystemId);
    }

    /**
     * Returns whether this {@link SchemeData} can be used to replace {@code other}.
     *
     * @param other A {@link SchemeData}.
     * @return Whether this {@link SchemeData} can be used to replace {@code other}.
     */
    public boolean canReplace(SchemeData other) {
      return hasData() && !other.hasData() && matches(other.caSystemId);
    }

    /**
     * Returns whether {@link #data} is non-null.
     */
    public boolean hasData() {
      return data != null;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (!(obj instanceof SchemeData)) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      SchemeData other = (SchemeData) obj;
      return Util.areEqual(caSystemId, other.caSystemId)
          && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
      if (hashCode == 0) {
        int result = 17;
        result = 31 * result + caSystemId;
        result = 31 * result + Arrays.hashCode(data);
        hashCode = result;
      }
      return hashCode;
    }

    // Parcelable implementation.

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(caSystemId);
      dest.writeByteArray(data);
      dest.writeByte((byte) (requiresSecureDecryption ? 1 : 0));
    }

    @SuppressWarnings("hiding")
    public static final Parcelable.Creator<SchemeData> CREATOR =
        new Parcelable.Creator<SchemeData>() {

      @Override
      public SchemeData createFromParcel(Parcel in) {
        return new SchemeData(in);
      }

      @Override
      public SchemeData[] newArray(int size) {
        return new SchemeData[size];
      }

    };

  }

}
