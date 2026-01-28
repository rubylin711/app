package com.loader;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.loader.structure.OTACableParameters;
import com.loader.structure.OTATerrParameters;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

public class OTAUpgrade  extends DTVActivity {

    private final String TAG = getClass().getSimpleName();
    private Button bnUpdate;
    private Button bnExit;
    private TextView ota_msg;
    private OTATerrParameters dvbt_parameters;
    private OTACableParameters cable_parameters;
    private int dvb_type;
    private int is_service_available;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otaupgrade);

        ota_msg=(TextView) findViewById(R.id.ota_msg);

        bnUpdate = (Button)findViewById(R.id.ota_ok_btn);
        bnUpdate.setOnClickListener(mDownloadListener);

        bnExit = (Button)findViewById(R.id.ota_exit_btn);
        bnExit.setOnClickListener(mExitListener);

        Bundle b=this.getIntent().getExtras();
        is_service_available = b.getInt("service",-1);
        dvb_type = b.getInt("dvb_type",0);
        Log.d(TAG, "onCreate: dvb_type " + dvb_type + " is_service_available "+ is_service_available);
        if(dvb_type == 1) { //cable
            cable_parameters = new OTACableParameters();
            cable_parameters.pid = b.getInt("pid", 0);
            cable_parameters.frequency = b.getInt("frequency", 0);
            cable_parameters.symbolRate = b.getInt("symbolRate", 0);
            cable_parameters.modulation =b.getInt("modulation", 0);
            Log.d(TAG,"onCreate: pid " +  cable_parameters.pid);
        }
        else if((dvb_type == 4) || (dvb_type == 8) || (dvb_type == 16)){//DVB T or T2 or isdbt
            dvbt_parameters = new OTATerrParameters();
            dvbt_parameters.pid = b.getInt("pid", 0);
            dvbt_parameters.frequency = b.getInt("frequency", 0);
            dvbt_parameters.bandWidth = b.getInt("bandWidth", 0);
            dvbt_parameters.modulation = b.getInt("modulation", 0);
            dvbt_parameters.enDVBTPrio = b.getInt("dvbprio", 0);
            dvbt_parameters.enChannelMode = b.getInt("channelMode", 0);
            Log.d(TAG, "onCreate: pid " + dvbt_parameters.pid);
        }
        if(is_service_available == 0){
            ota_msg.setText(getResources().getString(R.string.STR_LOADER_DTV_OTA_MSG));
            bnUpdate.setVisibility(View.VISIBLE);
        }
        else
        {
            ota_msg.setText(getResources().getString(R.string.STR_LOADER_DTV_OTA_REJECT));
            bnUpdate.setVisibility(View.INVISIBLE);
        }

    }

    private View.OnClickListener mDownloadListener = new OnClickListener() {
        public void onClick(View v) {
            int ret = 0;
            if (dvb_type == 1) //cable
                ret = UpdateOTADVBCSoftWare(cable_parameters.pid,cable_parameters.frequency, cable_parameters.symbolRate, cable_parameters.modulation);
            else if(dvb_type == 4) //DVB T
                ret = UpdateOTADVBTSoftWare(dvbt_parameters.pid,dvbt_parameters.frequency, dvbt_parameters.bandWidth, dvbt_parameters.modulation,dvbt_parameters.enDVBTPrio);
            else if(dvb_type == 8) //DVB T2
                ret = UpdateOTADVBT2SoftWare(dvbt_parameters.pid,dvbt_parameters.frequency, dvbt_parameters.bandWidth, dvbt_parameters.modulation,dvbt_parameters.enChannelMode);
            else if(dvb_type == 16) //isdbt
                ret = UpdateOTAISDBTSoftWare(dvbt_parameters.pid,dvbt_parameters.frequency, dvbt_parameters.bandWidth, dvbt_parameters.modulation,dvbt_parameters.enDVBTPrio);
            else
                Log.d(TAG, "dvb type : "+dvb_type+ "not support" );
            if(ret != 0 )
                show_err_toast(getResources().getString(R.string.STR_LOADER_DTV_OTA_UPGRADE_FAILED));
            finish();
        }
    };

    private View.OnClickListener mExitListener = new OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

    private void show_err_toast(String msg)
    {
        TextView title = (TextView)findViewById(R.id.t_usb_title);
        Toast err = Toast.makeText(OTAUpgrade.this, msg, Toast.LENGTH_SHORT);
        err.setGravity(Gravity.TOP, 0, title.getTop());
        err.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            {
                finish();
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
}
