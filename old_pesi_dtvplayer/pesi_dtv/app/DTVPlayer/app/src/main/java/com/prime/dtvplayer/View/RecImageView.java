package com.prime.dtvplayer.View;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.prime.dtvplayer.R;

public class RecImageView extends androidx.appcompat.widget.AppCompatImageView {
    private final String TAG = getClass().getSimpleName();
    private WindowManager mWindowManager = null;
    private ImageView mRecImage = null;

    public RecImageView(Context context) {
        super(context);

        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.x = 80;
        params.y = 80;
        params.width = 50;
        params.height = 50;
        params.gravity = Gravity.START | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;

        mRecImage = new ImageView(context);
        mRecImage.setImageDrawable(context.getResources().getDrawable(R.drawable.rec,null));

        mWindowManager.addView(mRecImage, params);
    }

    public void SetVisibility(boolean enable)
    {
        if(mRecImage != null) {
            if (enable)
                mRecImage.setVisibility(View.VISIBLE);
            else
                mRecImage.setVisibility(View.INVISIBLE);
        }
    }

    public void remove()
    {
        if(mRecImage != null) {
            mWindowManager.removeView(mRecImage);
            mRecImage = null;
        }
    }
}
