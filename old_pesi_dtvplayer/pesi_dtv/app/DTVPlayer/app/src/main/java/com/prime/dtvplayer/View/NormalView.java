package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prime.dtvplayer.R;

/**
 * Created by connie_sung on 2017/12/13.
 */

public class NormalView extends ConstraintLayout {
    private final String TAG = getClass().getSimpleName();
    private TextView subtitle ;
    private TextView errMsg ;
    private TextView chNum ;
    private ImageView mailIcon;

    public NormalView(Context context) {
        super(context);
    }
    public NormalView(Context context, AttributeSet attrs) {
        this(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        errMsg = (TextView)findViewById(R.id.errorMsg) ;
        mailIcon = (ImageView) findViewById(R.id.mailIGV);
    }

    public void SetVisibility( int visible)
    {
        ViewErrMsg( visible, null);
    }

    public void ViewErrMsg(int visible, String msg)
    {
        Log.d(TAG, "View:  AAAAA   visible = " + visible + "      " + msg);
        if (visible == View.VISIBLE)
            errMsg.setText(msg);
        errMsg.setVisibility(visible);
    }

    public int getMailIconVisibility()
    {
        return mailIcon.getVisibility();
    }

    public void ViewMailIcon(int visible)
    {
        if (visible == View.VISIBLE) {
            mailIcon.setVisibility(View.VISIBLE);
        }
        else
            mailIcon.setVisibility(View.INVISIBLE);
    }

}
