package com.loader;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.loader.structure.OTACableParameters;
import com.loader.structure.OTATerrParameters;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
//import com.pesi.dvttestapp.LoaderParameters;
//import com.pesi.dvttestapp.LoaderStatus;
//import com.pesi.dvttestapp.OTAUpgrade;
//import com.pesi.dvttestapp.SecureSetting;
//import com.pesi.dvttestapp.USBUpgrade;


public class MainActivity extends DTVActivity implements OnClickListener{

    private final String TAG = getClass().getSimpleName();
    private Button btn_status;
    private Button btn_param;
    private Button btn_usb;
    private Button btn_ota;
    private Button btn_secure;
    private int is_service_available;
    private int dvb_type;
    private OTATerrParameters dvbt_parameters;
    private OTACableParameters cable_parameters;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_loader);
        
        btn_status = (Button) findViewById(R.id.bn_ldr_status);
        btn_param  = (Button) findViewById(R.id.bn_ldr_params);
        btn_usb    = (Button) findViewById(R.id.bn_usb_pg);
        btn_ota    = (Button) findViewById(R.id.bn_ota_pg);
        btn_secure = (Button) findViewById(R.id.bn_secure_pg);

        btn_status.setOnClickListener(this);
        btn_param.setOnClickListener(this);
        btn_usb.setOnClickListener(this);
        btn_ota.setOnClickListener(this);
        btn_secure.setOnClickListener(this);

        is_service_available = -1;
        dvb_type = 16; //1 is cable, 2 is sat, 4 is Ter, 8 is T2, 16 is ISDBT
        if(dvb_type == 1) //cable
            cable_parameters =  DVBGetOTACableParas();
        else if(dvb_type == 4) //dvb t
            dvbt_parameters = DVBGetOTATerrestrialParas();
        else if(dvb_type == 8) //dvb t2
            dvbt_parameters = DVBGetOTADVBT2Paras();
        else if(dvb_type == 16) //isdbt
            dvbt_parameters =  DVBGetOTAIsdbtParas();
        checkDownloadParameters();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: = " + requestCode + " => resultCode " + resultCode);
        is_service_available = data.getExtras().getInt("service",-1);
        Log.d(TAG, "is_service_available: = " + is_service_available);
        if(is_service_available == 0) {
            if(dvb_type == 1) { //cable
                cable_parameters.pid = data.getExtras().getInt("pid", 0);
                cable_parameters.frequency = data.getExtras().getInt("frequency", 0);
                cable_parameters.symbolRate = data.getExtras().getInt("symbolRate", 0);
                cable_parameters.modulation = data.getExtras().getInt("modulation", 0);
                Log.d("MainActivity", "onActivityResult: frequency = " + cable_parameters.frequency
                        + " modulation = " + cable_parameters.modulation + " symbolRate = " + cable_parameters.symbolRate
                        + " pid = " + cable_parameters.pid);
            }
            else if((dvb_type == 4) || (dvb_type == 8) ||(dvb_type == 16)) { //DVB T or T2 or dvbt
                dvbt_parameters.pid = data.getExtras().getInt("pid", 0);
                dvbt_parameters.frequency = data.getExtras().getInt("frequency", 0);
                dvbt_parameters.bandWidth = data.getExtras().getInt("bandWidth", 0);
                dvbt_parameters.modulation = data.getExtras().getInt("modulation", 0);
                dvbt_parameters.enDVBTPrio = data.getExtras().getInt("dvbprio", 0);
                dvbt_parameters.enChannelMode= data.getExtras().getInt("channelMode", 0);
                Log.d("MainActivity", "onActivityResult: frequency = " + dvbt_parameters.frequency
                        + " modulation = " + dvbt_parameters.modulation + " bandWidth = " + dvbt_parameters.bandWidth
                        + " pid = " + dvbt_parameters.pid);
            }
        }
    }

    private void checkDownloadParameters()
    {
        if(dvb_type == 1) {
            if (cable_parameters.pid < 32 || cable_parameters.pid > 8190) {
                Log.d(TAG, "Error: cable_parameters.pid " + cable_parameters.pid);
                cable_parameters.pid = 7000;
            }
            if (cable_parameters.frequency < 100 || cable_parameters.frequency > 900) {
                Log.d(TAG, "Error: cable_parameters.frequency " + cable_parameters.frequency);
                cable_parameters.frequency = 605;
            }
            if (cable_parameters.symbolRate < 100 || cable_parameters.symbolRate > 9000) {
                Log.d(TAG, "Error: cable_parameters.symbolRate" + cable_parameters.symbolRate);
                cable_parameters.symbolRate = 6875;
            }
            if (cable_parameters.modulation < 0 || cable_parameters.modulation > 4) {
                Log.d(TAG, "Error: cable_parameters.modulation " + cable_parameters.modulation);
                cable_parameters.modulation = 2;
            }
        }
        else if((dvb_type == 4) || (dvb_type == 8) || (dvb_type == 16)) {//dvb t to isdbt
            if (dvbt_parameters.pid < 32 || dvbt_parameters.pid > 8190) {
                Log.d(TAG, "Error: dvbt_parameters.pid " + dvbt_parameters.pid);
                dvbt_parameters.pid = 7000;
            }
            if (dvbt_parameters.frequency < 100 || dvbt_parameters.frequency > 900) {
                Log.d(TAG, "Error: dvbt_parameters.frequency " + dvbt_parameters.frequency);
                dvbt_parameters.frequency = 605;
            }
            if (dvbt_parameters.bandWidth < 6 || dvbt_parameters.bandWidth > 9) {
                Log.d(TAG, "Error: dvbt_parameters.bandWidth " + dvbt_parameters.bandWidth);
                dvbt_parameters.bandWidth = 8;
            }
            if (dvbt_parameters.modulation < 0 || dvbt_parameters.modulation > 4) {
                Log.d(TAG, "Error: dvbt_parameters.modulation " + dvbt_parameters.modulation);
                dvbt_parameters.modulation = 3;
            }
            if (dvbt_parameters.enChannelMode < 0 || dvbt_parameters.enChannelMode> 1) {
                Log.d(TAG, "Error: dvbt_parameters.enChannelMode " + dvbt_parameters.enChannelMode);
                dvbt_parameters.enChannelMode = 0;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        case R.id.bn_ldr_status:
            Intent intent = new Intent(MainActivity.this, LoaderStatus.class);
            startActivity(intent);
            break;

        case R.id.bn_ldr_params:
            Bundle bundle1 = new Bundle();
            Intent intent1 = new Intent();
            if(dvb_type == 1) //cable
            {
                intent1.setClass(MainActivity.this, LoaderCableParameters.class);
                bundle1.putInt("service", is_service_available);
                bundle1.putInt("pid", cable_parameters.pid);
                bundle1.putInt("frequency", cable_parameters.frequency);
                bundle1.putInt("symbolRate", cable_parameters.symbolRate);
                bundle1.putInt("modulation", cable_parameters.modulation);
                Log.d(TAG, "bn_ldr_params: pid = " + cable_parameters.pid + " frequency = " + cable_parameters.frequency
                        + " symbolRate = " + cable_parameters.symbolRate + " modulation = " + cable_parameters.modulation );
                intent1.putExtras(bundle1);
                intent1.putExtras(bundle1);
                startActivityForResult(intent1, 0);
            }
            else if((dvb_type == 4) || (dvb_type == 8) ||(dvb_type == 16)) { //dvb t or t2 or isdbt
                if((dvb_type == 4) || (dvb_type == 8)) //DVB T or T2
                    intent1.setClass(MainActivity.this, LoaderTerrestrialParameters.class);
                else //ISDBT
                    intent1.setClass(MainActivity.this, LoaderISDBTParameters.class);
                bundle1.putInt("service", is_service_available);
                bundle1.putInt("pid", dvbt_parameters.pid);
                bundle1.putInt("frequency", dvbt_parameters.frequency);
                bundle1.putInt("bandWidth", dvbt_parameters.bandWidth);
                bundle1.putInt("modulation", dvbt_parameters.modulation);
                bundle1.putInt("dvbprio", dvbt_parameters.enDVBTPrio);
                bundle1.putInt("channelMode", dvbt_parameters.enChannelMode);
                Log.d(TAG, "bn_ldr_params: pid = " + dvbt_parameters.pid + " frequency = " + dvbt_parameters.frequency
                        + " bandwidth = " + dvbt_parameters.bandWidth + " modulation = " + dvbt_parameters.modulation );
                intent1.putExtras(bundle1);
                startActivityForResult(intent1, 0);
            }
            break;

        case R.id.bn_usb_pg:
            Intent intent2 = new Intent(MainActivity.this, USBUpgrade.class);
            startActivity(intent2);
            break;
        
        case R.id.bn_ota_pg:
            Intent intent3 = new Intent(MainActivity.this, OTAUpgrade.class);
            Bundle bundle3 = new Bundle();
            bundle3.putInt("service", is_service_available);
            bundle3.putInt("dvb_type", dvb_type);
            if(dvb_type == 1) { //cable
                bundle3.putInt("pid", cable_parameters.pid);
                bundle3.putInt("frequency", cable_parameters.frequency);
                bundle3.putInt("symbolRate", cable_parameters.symbolRate);
                bundle3.putInt("modulation", cable_parameters.modulation);
            }
            else if((dvb_type == 4) || (dvb_type == 8) || (dvb_type == 16)){ //dvb t or t2 or isdbt
                bundle3.putInt("pid", dvbt_parameters.pid);
                bundle3.putInt("frequency", dvbt_parameters.frequency);
                bundle3.putInt("bandWidth", dvbt_parameters.bandWidth);
                bundle3.putInt("modulation", dvbt_parameters.modulation);
                bundle3.putInt("dvbprio", dvbt_parameters.enDVBTPrio);
                bundle3.putInt("channelMode", dvbt_parameters.enChannelMode);
            }
            intent3.putExtras(bundle3);
            startActivity(intent3);
            break;
            
        case R.id.bn_secure_pg:
            Intent intent4 = new Intent(MainActivity.this, SecureSetting.class);
            startActivity(intent4);
            break;

        default:
            break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d("LoaderISDBTParameters", "onKeyDown: IN");
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            {
                finish();
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
