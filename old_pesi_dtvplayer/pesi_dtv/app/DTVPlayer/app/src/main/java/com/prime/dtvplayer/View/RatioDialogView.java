package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.R;

import java.util.logging.Handler;

/**
 * Created by scoty on 2017/12/13.
 */

public abstract class RatioDialogView extends Dialog {
    private static final String TAG="RatioDialogView";
    private Context mContext = null;
    private TextView ratio_text,conversion_text,resolution_text,audoutput_text;
    private Spinner spinner_ratio,spinner_conversion,spinner_resolution,spinner_audoutput;
    private SelectBoxView sel_ratio,sel_conversion,sel_resolution,sel_audoutput;
    private Button okbtn,cancelbtn;
    private int ratio,conversion,resolution,audOutput;

    public RatioDialogView(Context context,int gpos_ratio, int gpos_conversion, int gpos_resolution, int gpos_audioOutput)
    {
        super(context);
        mContext = context;
        ratio = gpos_ratio;
        conversion = gpos_conversion;
        resolution = gpos_resolution;
        audOutput = gpos_audioOutput;

//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        setContentView(R.layout.ratio_dialog);
        Window window = getWindow();//get dialog widow size
        WindowManager.LayoutParams lp=getWindow().getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        getWindow().setGravity(Gravity.START|Gravity.TOP);
        getWindow().setAttributes(lp);//set dialog parameter
        onWindowAttributesChanged(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
            @Override
            public void run () {
                show();
            }
        }, 150);
//        show();//show dialog
        ratio_dialog_init();

    }
    public void ratio_dialog_init()
    {
        ratio_text = (TextView) getWindow().findViewById(R.id.ratiodialogtvratioTXV);
        conversion_text = (TextView) getWindow().findViewById(R.id.ratiodialogconversionTXV);
        audoutput_text = (TextView) getWindow().findViewById(R.id.audiooutputTXV);
        //resolution_text = (TextView) mDialog.getWindow().findViewById(R.id.ratiodialogresolutionTXV);//resolution

        spinner_ratio = (Spinner) getWindow().findViewById(R.id.ratiodialogtvrationSPINNER);
        sel_ratio = new SelectBoxView(mContext,
                spinner_ratio, mContext.getResources().getStringArray(R.array.STR_TV_RATIO_OPTION));
        spinner_ratio.setSelection(ratio);

        spinner_conversion = (Spinner) getWindow().findViewById(R.id.ratiodialogconversionSPINNER);
        sel_conversion = new SelectBoxView(mContext,
                spinner_conversion, mContext.getResources().getStringArray(R.array.STR_CONVERSION_OPTION));
        spinner_conversion.setSelection(conversion);

        spinner_audoutput = (Spinner) getWindow().findViewById(R.id.audoutputSPINNER);
        sel_audoutput = new SelectBoxView(mContext,
                spinner_audoutput, mContext.getResources().getStringArray(R.array.STR_ARRAY_AUDIO_OUTPUT));

        /*//resolution
        spinner_resolution = (Spinner) mDialog.getWindow().findViewById(R.id.ratiodialogresolutionSPINNER);
        sel_resolution = new SelectBoxView(mContext,
                spinner_resolution, mContext.getResources().getStringArray(R.array.STR_HDMI_RESOLUTION_OPTION));
        spinner_resolution.setSelection(resolution);
        */

        okbtn = (Button) getWindow().findViewById(R.id.ratiodialogokBTN);
        okbtn.setOnClickListener(new onOkclickitem());
        cancelbtn = (Button) getWindow().findViewById(R.id.ratiodialogcancelBTN);
        cancelbtn.setOnClickListener(new onCancleclickitem());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//key event
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
            /*case KeyEvent.KEYCODE_3:{
                mContext.getVolumebar
            }break;*/
        }
        return super.onKeyDown(keyCode, event);
    }

    class onOkclickitem implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onOkclickitem : onclick");
            ratio = sel_ratio.GetSelectedItemIndex();
            conversion = sel_conversion.GetSelectedItemIndex();
            audOutput = sel_audoutput.GetSelectedItemIndex();
            //resolution = sel_resolution.GetSelectedItemIndex();
            //Log.d(TAG,"ratio ==> " + ratio +"conversion ==>> " + conversion);
            onSetPositiveButton(ratio,conversion,audOutput/*,resolution*/);
            dismiss();
        }
    }

    class onCancleclickitem implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onCancleclickitem : onclick");
            dismiss();
        }
    }

    //    public void dismissDialog(){
//        Log.d(TAG, "dismissDialog");
//        if(mDialog!=null&& mDialog.isShowing()){
//            mDialog.dismiss();//close dialog
//        }
//    }
//
//    public boolean isShowing(){
//        Log.d(TAG, "isShowing");
//        if(mDialog!=null&&mDialog.isShowing()){
//            return mDialog.isShowing();//check dialog is exist
//        }
//        return false;
//    }
    abstract public void onSetPositiveButton(int gpos_ratio, int gpos_conversion, int audOutput/*, int gpos_resolution*/);
}
