package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.prime.dtvplayer.R;

/**
 * Created by scoty on 2017/11/13.
 */

public class ActivityTitleView extends ConstraintLayout {
    private static final String TAG = "ActivityTitleView";
    private TextView titleView;
    private TextView timeView;
    public ActivityTitleView(Context context) {
        super(context);
    }
    public ActivityTitleView(Context context, AttributeSet attrs) {
        this(context);
    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "onFinishInflate");
        super.onFinishInflate();
        titleView = (TextView)findViewById(R.id.activitytitleTXV);
        timeView= (TextView)findViewById(R.id.titleTimeTXV);
    }
    public void setTitleView(String string)
    {
        Log.d(TAG, "setTitleView");
        titleView.setText(string);
    }
    public void setCurTimeVisible()
    {
        timeView.setVisibility(View.VISIBLE);
    }
    public void setCurrentTime(String string)
    {
        timeView.setText(string);
    }
    public String getCurrentTime()
    {
            return (String) timeView.getText();
    }
}