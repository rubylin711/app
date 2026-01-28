package com.prime.launcher.teletextservice;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tracking Teletext service status
 *
 * Used for consistency in case of unexpected Teletext service crash
 */
public class ServiceStatus {
    private static final String SERVICE_STATUS_SHARED_PREF = "service_state";
    private static final String RENDERING_STATE_SHARED_PREF = "rendering_state";
    private static final String REBIND_SHARED_PREF = "rebind";
    public static final int RENDERING_STATE_UNKNOWN = 0;
    public static final int RENDERING_STATE_RUNNING = 1;
    public static final int RENDERING_STATE_STOPPED = 2;

    private int mRenderingState;
    private boolean mRebind;
    private Context mContext;

    public ServiceStatus(Context context) {
        mContext = context;
    }

    public void readStatus() {
        SharedPreferences serviceState = mContext.getSharedPreferences(SERVICE_STATUS_SHARED_PREF,
                Context.MODE_PRIVATE);
        mRenderingState = serviceState.getInt(RENDERING_STATE_SHARED_PREF, RENDERING_STATE_UNKNOWN);
        mRebind = serviceState.getBoolean(REBIND_SHARED_PREF, false);
    }

    public void saveStatus(int renderingState) {
        SharedPreferences serviceState = mContext.getSharedPreferences(SERVICE_STATUS_SHARED_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = serviceState.edit();
        mRenderingState = renderingState;
        editor.putInt(RENDERING_STATE_SHARED_PREF, renderingState);
        editor.apply();
    }

    public void saveStatus(boolean rebind) {
        SharedPreferences serviceState = mContext.getSharedPreferences(SERVICE_STATUS_SHARED_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = serviceState.edit();
        mRebind = rebind;
        editor.putBoolean(REBIND_SHARED_PREF, rebind);
        editor.apply();
    }

    public String getRenderingStateString() {
        switch (mRenderingState) {
            case RENDERING_STATE_RUNNING:
                return "running";
            case RENDERING_STATE_STOPPED:
                return "stopped";
            case RENDERING_STATE_UNKNOWN:
            default:
                return "unknown";
        }
    }

    public int getRenderingState() {
        return mRenderingState;
    }

    public boolean getRebindState() {
        return mRebind;
    }
}
