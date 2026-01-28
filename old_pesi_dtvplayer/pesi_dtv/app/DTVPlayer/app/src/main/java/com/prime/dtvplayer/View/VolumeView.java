package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.prime.dtvplayer.R;

/**
 * Created by scoty on 2017/12/13.
 */

public class VolumeView extends ConstraintLayout {
    private static final String TAG = "VolumeView";
    public VolumeView(Context context) {
        super(context);
    }
    public VolumeView(Context context, AttributeSet attrs) {
        this(context);
    }
    private SeekBar volumebar;
    private TextView volume_text,volume_value;
    private ImageView volumebg,volume_mute;
    private int visibility = View.INVISIBLE;

    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "onFinishInflate");
        super.onFinishInflate();
        volumebar = (SeekBar) findViewById(R.id.volumebarSEEKBAR);
        volumebg = (ImageView) findViewById(R.id.volumebgIGV);
        volume_text = (TextView) findViewById(R.id.volumeTXV);
        volume_value = (TextView) findViewById(R.id.volumevalueTXV);
        volume_mute = (ImageView) findViewById(R.id.volumebarmuteIGV);
    }

    public SeekBar getVolumebar()
    {
        return volumebar;
    }

    public ImageView getVolumeMute()
    {
        return volume_mute;
    }
    public TextView getVolumeValue()
    {
        return volume_value;
    }
    public void setVisibility(int visible)
    {
        volumebar.setVisibility(visible);
        volumebg.setVisibility(visible);
        volume_text.setVisibility(visible);
        volume_value.setVisibility(visible);
        volume_mute.setVisibility(visible);
        visibility = visible;
    }

    public void setProgressValue(int progress)
    {
        if(progress == 0)//mute show icon
        {
            volume_mute.setVisibility(View.VISIBLE);
            volume_value.setVisibility(View.INVISIBLE);
        }
        else
        {
            volume_mute.setVisibility(View.INVISIBLE);
            volume_value.setVisibility(View.VISIBLE);
        }
    }

    public int getVisibility()
    {
        return visibility == View.VISIBLE ? View.VISIBLE:View.INVISIBLE ;
    }


}
