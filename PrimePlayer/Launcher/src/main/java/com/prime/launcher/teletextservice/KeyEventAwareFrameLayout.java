package com.prime.launcher.teletextservice;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class KeyEventAwareFrameLayout extends FrameLayout {
    private static final String TAG = "KeyEventAwareLayout";

    public interface KeyEventListener {
        void onTeletextKeyPressed(KeyEvent event);
    }

    @Nullable
    private KeyEventListener mListener;

    public KeyEventAwareFrameLayout(@NonNull Context context) {
        super(context);
    }

    public KeyEventAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyEventAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KeyEventAwareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setKeyEventListener(@Nullable KeyEventListener listener) {
        mListener = listener;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "*****dispatchKeyEvent Event : " + event);
        if (event != null && (event.getAction() == KeyEvent.ACTION_UP) && mListener != null) {
            mListener.onTeletextKeyPressed(event);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
