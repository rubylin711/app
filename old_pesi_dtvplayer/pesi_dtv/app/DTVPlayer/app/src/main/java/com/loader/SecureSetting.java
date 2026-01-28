package com.loader;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.R;

public class SecureSetting extends DTVActivity {

    private final String TAG = getClass().getSimpleName();
    private TextView fuse_value;
    private Button bnBLOW;
    private int status;
    public static SecureSetting handle=null;
    
   @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_setting);
        handle= this;
        
        fuse_value = (TextView)findViewById(R.id.fuse_status);
        bnBLOW = (Button)findViewById(R.id.blow_btn);
        bnBLOW.setOnClickListener(mBlowListener);
    }

    protected void onStart()
    {
        show_status();
        super.onStart();
    }

    public void show_status()
    {
        status = LoaderDtvGetJTAG();
        if(status == 0)
            fuse_value.setText(getResources().getString(R.string.STR_LOADER_DTV_OPEN));
        else if(status == 1)
            fuse_value.setText(getResources().getString(R.string.STR_LOADER_DTV_PROTECTED));
        else
            fuse_value.setText(getResources().getString(R.string.STR_LOADER_DTV_CLOSE));
    }

    private View.OnClickListener mBlowListener = new View.OnClickListener(){
                @Override
                public void onClick(View v){
            new SureDialog( SecureSetting.this )
            {
                public void onSetMessage ( View v )
                {
                    ( (TextView) v ).setText( getString( R.string.STR_LOADER_DTV_BLOW_MSG ) );
                }

                public void onSetNegativeButton ()
                {
                    Log.d(TAG, "onKeyDown: onSetNegativeButton");
                }

                public void onSetPositiveButton ()
                {
                   Log.d(TAG, "onKeyDown: onSetPositiveButton");
                   int result = LoaderDtvSetJTAG(1); // 0: open 1:protected 2:close
                    Log.d(TAG, "LoaderDtvSetJTAG result = " + result);
                   if(result == 0){
                       show_status();
                   }

                }
            };
        };
    };
}
