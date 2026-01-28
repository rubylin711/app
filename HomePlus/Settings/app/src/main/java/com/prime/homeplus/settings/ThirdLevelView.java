package com.prime.homeplus.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public abstract class ThirdLevelView extends LinearLayout {
    protected SettingsRecyclerView settingsRecyclerView;
    protected int mViewIndex;
    private String TAG = "HomePlus-ThirdLevelView";

    public abstract int loadLayoutResId();

    public abstract void onViewCreated();

    public abstract void onFocus();

    public void onHandleAction(String action) {
        Log.d(TAG, "onHandleAction() action:" + action);
    }

    public void onViewPaused() {
        Log.d(TAG, "onViewPaused()");
    }

    public void onViewResumed() {
        Log.d(TAG, "onViewResumed()");
    }

    public ThirdLevelView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(context);
        this.mViewIndex = i;
        this.settingsRecyclerView = secondDepthView;
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(loadLayoutResId(), this);
        setFocusable(false);
        onViewCreated();
        //onFocus();
    }

    public View.OnFocusChangeListener NoAnimationOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.BOLD);
                    rbBtn.setTextSize(17);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.BOLD);
                    btnBtn.setTextSize(17);
                } else if (view instanceof EditText) {
                    EditText etText = (EditText) view;
                    etText.setTypeface(null, Typeface.BOLD);
                    etText.setTextSize(17);
                }
            } else {
                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.NORMAL);
                    rbBtn.setTextSize(16);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.NORMAL);
                    btnBtn.setTextSize(16);
                } else if (view instanceof EditText) {
                    EditText etText = (EditText) view;
                    etText.setTypeface(null, Typeface.NORMAL);
                    etText.setTextSize(16);
                }
            }
        }
    };

    public View.OnFocusChangeListener OnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                view.bringToFront();
                ScaleAnimation am = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                am.setDuration(200);
                am.setFillAfter(true);
                view.setAnimation(am);
                am.startNow();

                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.BOLD);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.BOLD);
                }
            } else {
                view.clearAnimation();

                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.NORMAL);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    };
}
