package com.prime.dtvplayer.View;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.prime.dtvplayer.R;
/**
 * Created by jim_huang on 2017/10/27.
 */

public class InfoDetailView extends ConstraintLayout {
    private ImageView detailBg ;
    private TextView eventDetail ;
    private ProgressBar strengthBar ;
    private ProgressBar qualityBar ;
    private TextView strengthValue ;
    private TextView qualityValue ;
    private TextView berValue ;
    private static int lock =0;
//    private int visibility = View.INVISIBLE ;
    public InfoDetailView(Context context) {
        super(context);
    }
    public InfoDetailView(Context context, AttributeSet attrs) {
        this(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        detailBg = (ImageView)findViewById(R.id.detailBgIGV) ;
        eventDetail = (TextView)findViewById(R.id.eventDetailTXV) ;
        strengthBar = (ProgressBar)findViewById(R.id.strengthProgBar) ;
        qualityBar = (ProgressBar)findViewById(R.id.qualityProgBar) ;
        strengthValue = (TextView)findViewById(R.id.strengthValueTXV) ;
        qualityValue = (TextView)findViewById(R.id.qualityValueTXV) ;
        berValue = (TextView)findViewById(R.id.berValueTXV) ;
    }

    public void SetVisibility(int visible, String info, int lock, int str, int qua, String ber) {
        this.setVisibility(visible);
        if ( visible == View.VISIBLE ) {
            ShowDetail(info, str, qua, ber);
            UpdateLockStatus(lock);
            eventDetail.requestFocus();
            this.requestLayout();
        }
    }

    public int GetVisibility()
    {
        return this.getVisibility();
    }

    public void ShowDetail(String info, int str, int qua, String ber )
    {
        int barcolor;
        String string;
        eventDetail.setText(info);

        if(lock == 1 )
            barcolor = Color.GREEN;
        else
            barcolor =Color.RED;
        strengthBar.setProgressTintList(ColorStateList.valueOf(barcolor));
        qualityBar.setProgressTintList(ColorStateList.valueOf(barcolor));

        strengthBar.setProgress(str);
        qualityBar.setProgress(qua);
        string = Integer.toString(str) + " %";
        strengthValue.setText(string);
        string = Integer.toString(qua)+ " %";
        qualityValue.setText(string);
        //berValue.setText(ber);
    }

    public void UpdateDetailInfo( String info )
    {
        if(detailBg.getVisibility() == VISIBLE )
            eventDetail.setText(info);
    }

    public void UpdateLockStatus( int lockvalue)
    {
        lock = lockvalue;
    }

    public void UpdateTunerStatus( int str, int qua, String ber )
    {
        int barcolor;
        String string;
        //Log.d("UpdateTunerStatus", "UpdateTunerStatus:  lockValue = " + lock);
        if(detailBg.getVisibility() == VISIBLE ) {
            if(lock == 1 )
                barcolor = Color.GREEN;
            else
                barcolor =Color.RED;
            strengthBar.setProgressTintList(ColorStateList.valueOf(barcolor));
            qualityBar.setProgressTintList(ColorStateList.valueOf(barcolor));

            strengthBar.setProgress(str);
            qualityBar.setProgress(qua);
            string = Integer.toString(str) + " %";
            strengthValue.setText(string);
            string = Integer.toString(qua)+ " %";
            qualityValue.setText(string);
            //berValue.setText(ber);
        }
    }
}
