package com.prime.dtvplayer.View;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PipFrameView extends ConstraintLayout {
    private static final String TAG = "PipFrameView";
    private Context mContext;
    private TextView frameView;
    final int width = 5;
    private GradientDrawable border = new GradientDrawable();

    public PipFrameView(Context context) {
        super(context);
        mContext = context;
    }
    public PipFrameView(Context context, AttributeSet attrs) {
        this(context);
        mContext = context;
    }
    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "onFinishInflate");
        super.onFinishInflate();

        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(width, Color.argb(255, 128, 255, 255));
        border.setCornerRadius(0);
        setBackground(border);
        setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        setFocusable(false);

    }
    public void SetColor(int color)
    {
        Log.d(TAG, "setColor: ");

        border.setStroke(width, color);
        setBackground(border);
    }

    public void setSwitch()
    {
        Log.d(TAG, "setSwitch: ");
        if(getVisibility() == View.INVISIBLE)
            setVisibility(View.VISIBLE);
        else
            setVisibility(View.INVISIBLE);
    }

    public void SetVisibility(int visible)
    {
        Log.d(TAG, "SetVisibility: visible = " + visible);
        setVisibility(visible);
    }

    public void setPosition(int x, int y, int width, int height)
    {
        Log.d(TAG, "setPosition: x = " + x + " y = " + y + " width = " + width + " height = " + height);
        setX(x);
        setY(y);
        setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
    }
}
