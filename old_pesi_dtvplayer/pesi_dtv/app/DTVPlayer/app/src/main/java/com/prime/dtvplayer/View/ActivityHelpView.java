package com.prime.dtvplayer.View;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

/**
 * Created by scoty on 2017/11/13.
 */

public class ActivityHelpView extends ConstraintLayout{
    private static final String TAG = "ActivityHelpView";
    private ImageView helpRedIcon,helpGreenIcon,helpYellowIcon,helpBlueIcon;
    private TextView helpInfoView,helpRedView,helpGreenView,helpYellowView,helpBlueView;
    private Context mContext;
    DTVActivity mDtv;
    public ActivityHelpView(Context context) {
        super(context);
        mContext = context;
        mDtv = (DTVActivity)context;
    }
    public ActivityHelpView(Context context, AttributeSet attrs) {
        this(context);
        mContext = context;
        mDtv = (DTVActivity)context;
    }
    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "onFinishInflate");
        super.onFinishInflate();
        helpInfoView = (TextView) findViewById(R.id.infohelpTXV);
        helpRedView = (TextView)findViewById(R.id.redhelpTXV);
        helpRedIcon = (ImageView) findViewById(R.id.redhelpIGV);
        helpGreenView = (TextView)findViewById(R.id.greenhelpTXV);
        helpGreenIcon = (ImageView) findViewById(R.id.greenhelpIGV);
        helpYellowView = (TextView)findViewById(R.id.yellowhelpTXV);
        helpYellowIcon = (ImageView) findViewById(R.id.yellowhelpIGV);
        helpBlueView = (TextView)findViewById(R.id.bluehelpTXV);
        helpBlueIcon = (ImageView) findViewById(R.id.bluehelpIGV);
    }
    public void setHelpInfoTextBySplit(String fullText) {

        if (fullText != null)
            helpInfoView.setText(mDtv.SetSplitText(fullText), TextView.BufferType.SPANNABLE);
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
    public void setHelpRedText(String string)
    {
        Log.d(TAG, "setHelpRedText");
        if(string == null) {
            Log.d(TAG, "setHelpGreenText ===>>> IN");
            helpRedIcon.setVisibility(View.INVISIBLE);
        }
        else
            helpRedIcon.setVisibility(View.VISIBLE);
        helpRedView.setText(string);
    }
    public void setHelpGreenText(String string)
    {
        Log.d(TAG, "setHelpGreenText");
        if(string == null) {
            Log.d(TAG, "setHelpGreenText ===>>> IN");
            helpGreenIcon.setVisibility(View.INVISIBLE);
        }
        else
            helpGreenIcon.setVisibility(View.VISIBLE);
        helpGreenView.setText(string);
    }
    public void setHelpYellowText(String string)
    {
        Log.d(TAG, "setHelpYellowText");
        if(string == null) {
            Log.d(TAG, "setHelpYellowText ===>>> IN");
            helpYellowIcon.setVisibility(View.INVISIBLE);
        }
        else
            helpYellowIcon.setVisibility(View.VISIBLE);
        helpYellowView.setText(string);
    }
    public void setHelpBlueText(String string)
    {
        Log.d(TAG, "setHelpBlueText");
        if(string == null) {
            Log.d(TAG, "setHelpBlueText ===>>> IN");
            helpBlueIcon.setVisibility(View.INVISIBLE);
        }
        else
            helpBlueIcon.setVisibility(View.VISIBLE);
        helpBlueView.setText(string);
    }

    public void resetHelpRed(int Icon,String string)
    {
        Log.d(TAG, "resetHelpRed");
        if(string == null || Icon == 0) {
            helpRedIcon.setVisibility(View.INVISIBLE);
            helpRedView.setVisibility(View.INVISIBLE);
        }
        else{
            helpRedIcon.setVisibility(View.VISIBLE);
            helpRedView.setVisibility(View.VISIBLE);
            helpRedIcon.setImageDrawable(getResources().getDrawable(Icon, null));
            helpRedView.setText(string);
        }
    }

    public void resetHelpGreen(int Icon,String string)
    {
        Log.d(TAG, "resetHelpGreen");
        if(string == null || Icon == 0) {
            helpGreenIcon.setVisibility(View.INVISIBLE);
            helpGreenView.setVisibility(View.INVISIBLE);
        }
        else{
            helpGreenIcon.setVisibility(View.VISIBLE);
            helpGreenView.setVisibility(View.VISIBLE);
            helpGreenIcon.setImageDrawable(getResources().getDrawable(Icon, null));
            helpGreenView.setText(string);
        }
    }

    public void resetHelpYellow(int Icon,String string)
    {
        Log.d(TAG, "resetHelpYellow");
        if(string == null || Icon == 0) {
            helpYellowIcon.setVisibility(View.INVISIBLE);
            helpYellowView.setVisibility(View.INVISIBLE);
        }
        else {
            helpYellowIcon.setVisibility(View.VISIBLE);
            helpYellowView.setVisibility(View.VISIBLE);
            helpYellowIcon.setImageDrawable(getResources().getDrawable(Icon, null));
            helpYellowView.setText(string);
        }
    }

    public void resetHelpBlue(int Icon,String string)
    {
        Log.d(TAG, "resetHelpBlue");
        if(string == null || Icon == 0) {
            helpBlueIcon.setVisibility(View.INVISIBLE);
            helpBlueView.setVisibility(View.INVISIBLE);
        }
        else {
            helpBlueIcon.setVisibility(View.VISIBLE);
            helpBlueView.setVisibility(View.VISIBLE);
            helpBlueIcon.setImageDrawable(getResources().getDrawable(Icon, null));
            helpBlueView.setText(string);
        }
    }

    public void resetHelp(int index, int Icon,String string)
    {
        Log.d(TAG, "resetHelp");
        if( index == 1)
            resetHelpRed(Icon, string);
        else if (index == 2)
            resetHelpGreen(Icon, string);
        else if (index == 3)
            resetHelpYellow(Icon, string);
        else if (index == 4)
            resetHelpBlue(Icon, string);
    }

    // Johnny 20181228 for mouse control -s
    public void setHelpIconClickListener(int index, View.OnClickListener listener) {
        Log.d(TAG, "setHelpIconClickListener");
        if( index == 1) {
            helpRedIcon.setOnClickListener(listener);
        }
        else if (index == 2) {
            helpGreenIcon.setOnClickListener(listener);
        }
        else if (index == 3) {
            helpYellowIcon.setOnClickListener(listener);
        }
        else if (index == 4) {
            helpBlueIcon.setOnClickListener(listener);
        }
    }
    // Johnny 20181228 for mouse control -e
}
