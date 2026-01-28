package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

//import android.view.View;
//import android.widget.ImageView;

/**
 * Created by eric_lin on 2018/5/7.
 */

public class ActivityHelpViewWoColorIcon extends ConstraintLayout {
    private static final String TAG = "ActivityHelpViewWoColorIcon";
    //private ImageView helpRedIcon,helpGreenIcon,helpYellowIcon,helpBlueIcon;
    private TextView helpInfoView;//,helpRedView,helpGreenView,helpYellowView,helpBlueView;
    private Context mContext;
    DTVActivity mDtv;
    public ActivityHelpViewWoColorIcon(Context context) {
        super(context);
        mContext = context;
        mDtv = (DTVActivity)context;
    }
    public ActivityHelpViewWoColorIcon(Context context, AttributeSet attrs) {
        this(context);
        mContext = context;
        mDtv = (DTVActivity)context;
    }
    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "onFinishInflate");
        super.onFinishInflate();
        helpInfoView = (TextView) findViewById(R.id.infohelpTXV);
    }
    public void setHelpInfoTextBySplit(String fullText) {
        if (fullText != null) {
            helpInfoView.setText(mDtv.SetSplitText(fullText), TextView.BufferType.SPANNABLE);
        }
        else
            helpInfoView.setText("");
    }
    public void setHelpInfoText(SpannableStringBuilder builder, String string)
    {
        Log.d(TAG, "setHelpInfoText");
        if(builder == null)
            helpInfoView.setText(string);
        else
            helpInfoView.setText(builder);
    }
}
