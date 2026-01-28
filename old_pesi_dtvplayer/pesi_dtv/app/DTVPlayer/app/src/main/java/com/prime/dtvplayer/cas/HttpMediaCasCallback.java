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
import android.net.Uri;
import android.text.TextUtils;
import android.util.JsonReader;
import com.google.android.exoplayer2.C;
import com.prime.dtvplayer.cas.ExoMediaCas.KeyRequest;
import com.prime.dtvplayer.cas.ExoMediaCas.ProvisionRequest;
/*
import com.google.android.exoplayer2.cas.ExoMediaCas.KeyRequest;
import com.google.android.exoplayer2.cas.ExoMediaCas.ProvisionRequest;
 */
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.util.Log;

/**
 * A {@link MediaCasCallback} that makes requests using {@link HttpDataSource} instances.
 */
@TargetApi(18)
public final class HttpMediaCasCallback implements MediaCasCallback {

  private static final int MAX_MANUAL_REDIRECTS = 5;

  private final HttpDataSource.Factory dataSourceFactory;
  private final String defaultLicenseUrl;
  private final boolean forceDefaultLicenseUrl;
  private final Map<String, String> keyRequestProperties;
  private final static String TAG = "HttpMediaCasCallback";

  /**
   * @param defaultLicenseUrl The default license URL. Used for key requests that do not specify
   *     their own license URL.
   * @param dataSourceFactory A factory from which to obtain {@link HttpDataSource} instances.
   */
  public HttpMediaCasCallback(String defaultLicenseUrl, HttpDataSource.Factory dataSourceFactory) {
    this(defaultLicenseUrl, false, dataSourceFactory);
  }

  /**
   * @param defaultLicenseUrl The default license URL. Used for key requests that do not specify
   *     their own license URL, or for all key requests if {@code forceDefaultLicenseUrl} is
   *     set to true.
   * @param forceDefaultLicenseUrl Whether to use {@code defaultLicenseUrl} for key requests that
   *     include their own license URL.
   * @param dataSourceFactory A factory from which to obtain {@link HttpDataSource} instances.
   */
  public HttpMediaCasCallback(String defaultLicenseUrl, boolean forceDefaultLicenseUrl,
      HttpDataSource.Factory dataSourceFactory) {
    this.dataSourceFactory = dataSourceFactory;
    this.defaultLicenseUrl = defaultLicenseUrl;
    this.forceDefaultLicenseUrl = forceDefaultLicenseUrl;
    this.keyRequestProperties = new HashMap<>();
  }

  /**
   * Sets a header for key requests made by the callback.
   *
   * @param name The name of the header field.
   * @param value The value of the field.
   */
  public void setKeyRequestProperty(String name, String value) {
    Assertions.checkNotNull(name);
    Assertions.checkNotNull(value);
    synchronized (keyRequestProperties) {
      keyRequestProperties.put(name, value);
    }
  }

  /**
   * Clears a header for key requests made by the callback.
   *
   * @param name The name of the header field.
   */
  public void clearKeyRequestProperty(String name) {
    Assertions.checkNotNull(name);
    synchronized (keyRequestProperties) {
      keyRequestProperties.remove(name);
    }
  }

  /**
   * Clears all headers for key requests made by the callback.
   */
  public void clearAllKeyRequestProperties() {
    synchronized (keyRequestProperties) {
      keyRequestProperties.clear();
    }
  }

  private byte[] getLicence(byte[] keyResponse) {
    String licenceStr = null;
    String statusStr = null;
    int statusCode = -1;
    InputStream inputStream = new ByteArrayInputStream(keyResponse);
    try {
      JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "Response":
            reader.beginObject();
            while (reader.hasNext()) {
              switch (reader.nextName()) {
                case "license":
                  licenceStr = reader.nextString();
                  break;
                case "status":
                  statusStr = reader.nextString();
                  break;
                default:
                  throw new Exception("Unhandled string");
              }
            }
            reader.endObject();
            break;
          case "Status Code":
            statusCode = reader.nextInt();
            break;
          default:
            throw new Exception("Unhandled string");
        }
      }
      reader.endObject();
    } catch (Exception ex) {
      Log.e(TAG, "Failed to parse licence string: " + ex.toString());
      return null;
    }

    if (licenceStr == null || !statusStr.equals("OK") || statusCode != 0) {
      return null;
    }

    return Base64.getDecoder().decode(licenceStr);
  }

  @Override
  public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws IOException {
    //android.util.Log.d("WidevineCasSessionMgr", "hmcc executeProvisionRequest");//eric lin test
    //android.util.Log.d("WidevineCasSessionMgr", "request.getDefaultUrl()="+request.getDefaultUrl());//eric lin test
    //android.util.Log.d("WidevineCasSessionMgr", "getData()="+Util.fromUtf8Bytes(request.getData()));//eric lin test
    String url =
        request.getDefaultUrl() + "&signedRequest=" + Util.fromUtf8Bytes(request.getData());
    //Log.d("WidevineCasSessionMgr", "Send provisioning request, uri:" + url);
    return executePost(dataSourceFactory, url, Util.EMPTY_BYTE_ARRAY, null);
  }

  @Override
  public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws Exception {
    String url = request.getLicenseServerUrl();
    android.util.Log.d("WidevineCasSessionMgr", "hmcc executeKeyRequest(), url="+url);//eric lin test
    if (forceDefaultLicenseUrl || TextUtils.isEmpty(url)) {
      android.util.Log.d("WidevineCasSessionMgr", "hmcc executeKeyRequest(), set url be defaultLicenseUrl="+defaultLicenseUrl);//eric lin test
      url = defaultLicenseUrl;
    }
    Map<String, String> requestProperties = new HashMap<>();
    // Add standard request properties for supported schemes.
    String contentType = C.PLAYREADY_UUID.equals(uuid) ? "text/xml"
        : (C.CLEARKEY_UUID.equals(uuid) ? "application/json" : "application/octet-stream");
    requestProperties.put("Content-Type", contentType);
    if (C.PLAYREADY_UUID.equals(uuid)) {
      requestProperties.put("SOAPAction",
          "http://schemas.microsoft.com/DRM/2007/03/protocols/AcquireLicense");
    }
    //requestProperties.put("User-Agent", "Widevine CDM v1.0");
    // Add additional request properties.
    synchronized (keyRequestProperties) {
      requestProperties.putAll(keyRequestProperties);
    }
    String urlFinal = url + Util.fromUtf8Bytes(request.getData());
    return executePost(dataSourceFactory, url, request.getData(), requestProperties);
    //return executePost(dataSourceFactory, urlFinal, Util.EMPTY_BYTE_ARRAY, requestProperties);
  }

  private static byte[] executePost(HttpDataSource.Factory dataSourceFactory, String url,
      byte[] data, Map<String, String> requestProperties) throws IOException {
    HttpDataSource dataSource = dataSourceFactory.createDataSource();
    if (requestProperties != null) {
      for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
        dataSource.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
      }
    }

    int manualRedirectCount = 0;
    while (true) {
      DataSpec dataSpec =
          new DataSpec(
              Uri.parse(url),
              data,
              /* absoluteStreamPosition= */ 0,
              /* position= */ 0,
              /* length= */ C.LENGTH_UNSET,
              /* key= */ null,
              DataSpec.FLAG_ALLOW_GZIP);
      Log.d(TAG, dataSpec.toString());
      DataSourceInputStream inputStream = new DataSourceInputStream(dataSource, dataSpec);
      try {
        return Util.toByteArray(inputStream);
      } catch (InvalidResponseCodeException e) {
        // For POST requests, the underlying network stack will not normally follow 307 or 308
        // redirects automatically. Do so manually here.
        boolean manuallyRedirect =
            (e.responseCode == 307 || e.responseCode == 308)
                && manualRedirectCount++ < MAX_MANUAL_REDIRECTS;
        url = manuallyRedirect ? getRedirectUrl(e) : null;
        if (url == null) {
          throw e;
        }
      } finally {
        Util.closeQuietly(inputStream);
      }
    }
  }

  private static String getRedirectUrl(InvalidResponseCodeException exception) {
    Map<String, List<String>> headerFields = exception.headerFields;
    if (headerFields != null) {
      List<String> locationHeaders = headerFields.get("Location");
      if (locationHeaders != null && !locationHeaders.isEmpty()) {
        return locationHeaders.get(0);
      }
    }
    return null;
  }

}
