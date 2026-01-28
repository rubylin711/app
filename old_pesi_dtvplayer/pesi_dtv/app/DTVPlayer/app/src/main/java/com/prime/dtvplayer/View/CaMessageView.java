package com.prime.dtvplayer.View;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.prime.dtvplayer.R;

public class CaMessageView extends androidx.appcompat.widget.AppCompatTextView{
    private final String TAG = getClass().getSimpleName();
    private WindowManager mWindowManager = null;
    private TextView mErrorMsg = null;

    public CaMessageView(Context context, String text){
        super(context);

        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);

        mErrorMsg = new TextView(context);
        mErrorMsg.setBackgroundResource(R.drawable.full_black_bg_with_frame);//Scoty 20180912 modify caMessage
        mErrorMsg.setText(text);
        mErrorMsg.setGravity(Gravity.CENTER);
        mErrorMsg.setTextSize(25);//Scoty 20180912 modify caMessage
        mErrorMsg.setTextColor(Color.WHITE);//Scoty 20180912 modify caMessage

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.x = 0;
        params.y = 0;
        params.width = 700;
        params.height = 200;
        params.gravity = Gravity.CENTER;

        mWindowManager.addView(mErrorMsg, params);

    }

    public void remove()
    {
        if(mErrorMsg != null) {
            mWindowManager.removeView(mErrorMsg);
            mErrorMsg = null;
        }
    }

    public void SetVisibility(int visible)
    {
        mErrorMsg.setVisibility(visible);
    }
}
