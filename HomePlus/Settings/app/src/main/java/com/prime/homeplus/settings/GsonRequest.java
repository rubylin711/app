package com.prime.homeplus.settings;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.prime.homeplus.settings.data.MD5;

import android.util.Log;

public class GsonRequest<T> extends Request<T> {
	private static final String TAG = "GsonRequest";
	
	private final Listener<T> mListener;
	private Gson mGson;
	private Class<T> mClass;
	private String system_id = "cossmsnt";
	private String system_key = "nt888";

	public GsonRequest(int method, String url, Class<T> clazz, Listener<T> listener,
			ErrorListener errorListener) {
		super(method, url, errorListener);
		mGson = new Gson();
		mClass = clazz;
		mListener = listener;
		setRetryPolicy(new DefaultRetryPolicy( 8 * 1000, 0, 0)); // set default timeout to 8 seconds

	}

	public GsonRequest(String url, Class<T> clazz, Listener<T> listener,
			ErrorListener errorListener) {
		this(Method.GET, url, clazz, listener, errorListener);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
			return Response.success(mGson.fromJson(jsonString, mClass),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		}
	}

	@Override
	protected void deliverResponse(T response) {
		mListener.onResponse(response);
	}

	@Override
	public String getBodyContentType() {
	    return "application/json; charset=utf-8";
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = new HashMap<>();

		if (getUrl().endsWith("activation_status")) {
			String timeStampSec = String.valueOf(System.currentTimeMillis() / 1000); // Unix timestamp (second)
			MD5 mMD5 = new MD5();
			String sign = mMD5.md5sumStr(system_id + system_key + timeStampSec).toLowerCase(); // need toLowerCase() ?

			Log.d(TAG, "getHeaders() X-System-ID:" + system_id + ", X-Sign:" + sign);

			headers.put("X-System-ID", system_id);
			headers.put("X-Sign", sign);
		}

		return headers;
	}
}
