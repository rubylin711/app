package com.mtest.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.mtest.module.TunerModule;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import java.io.File;
import java.util.List;

import static android.view.KeyEvent.*;

public class HDDTestActivity extends DTVActivity
{
    private final static String TAG = "HDDTestActivity";
    private final static String MountOK = "Mount OK";
    private final static String MountFail = "Mount Fail";
    private String usb1Path = null;
    private String usb2Path = null;
    private String selectPath = null;
    Handler handler;
    TextView txvStatus;
    TextView txvTuner;
    TextView txvStorage;
    TextView txvHDD;
    TextView txvUSB1;
    TextView txvUSB2;
    TextView txv1;
    TextView txv2;
    TextView txv3;

    @Override
    public void onCreate ( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hddtest);
        txvStatus = (TextView) findViewById( R.id.tv_item19_status );
        txvTuner = (TextView) findViewById( R.id.tv_item19_tuner_no );
        txvStorage = (TextView) findViewById( R.id.tv_item19_storage_name );
        txvHDD = (TextView) findViewById( R.id.tv_item19_storage_1_status );
        txvUSB1 = (TextView) findViewById( R.id.tv_item19_storage_2_status );
        txvUSB2 = (TextView) findViewById( R.id.tv_item19_storage_3_status );
        txv1 = (TextView) findViewById(R.id.tv_item19_storage_1_idx);
        txv2 = (TextView) findViewById(R.id.tv_item19_storage_2_idx);
        txv3 = (TextView) findViewById(R.id.tv_item19_storage_3_idx);

        txv1.setBackgroundResource(R.drawable.shape_rectangle_focus);
        txvStatus.setText( "Idle" );

        TunerModule tunerModule = new TunerModule(this);
        if (tunerModule.isLock(0))//if ( TunerGetLockStatus(0) == 1 )
            txvTuner.setBackgroundResource( R.drawable.shape_rectangle_pass );
        else
            txvTuner.setBackgroundResource( R.drawable.shape_rectangle_fail );

        txvStorage.setText( "Storage: HDD" );
        selectPath = Environment.getExternalStorageDirectory().getPath();
    }

    @Override
    protected void onStart ()
    {
        super.onStart();
        handler = new Handler();
        handler.post( new Runnable()
        {
            @Override
            public void run ()
            {
                Mount();
                handler.postDelayed( this, 2000 );
            }
        } );
    }

    @Override
    public boolean onKeyDown ( int keyCode, KeyEvent event )
    {
        String HDD = txvHDD.getText().toString();
        String USB1 = txvUSB1.getText().toString();
        String USB2 = txvUSB2.getText().toString();

        switch ( keyCode )
        {
            case KEYCODE_1:
                if ( HDD.equals( MountOK ) )
                {
                    txv1.setBackgroundResource(R.drawable.shape_rectangle_focus);
                    txv2.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
                    txv3.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
                    txvStorage.setText( "Storage: HDD" );
                    selectPath = Environment.getExternalStorageDirectory().getPath();
                    Log.d( TAG, "onKeyDown: HDD path = "+selectPath );
                }
                break;

            case KEYCODE_2:
                if ( USB1.equals( MountOK ) )
                {
                    txv1.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
                    txv2.setBackgroundResource(R.drawable.shape_rectangle_focus);
                    txv3.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
                    txvStorage.setText( "Storage: USB1" );
                    selectPath = usb1Path;
                    Log.d( TAG, "onKeyDown: USB1 path = "+selectPath );
                }
                break;

            case KEYCODE_3:
                if ( USB2.equals( MountOK ) )
                {
                    txv1.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
                    txv2.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
                    txv3.setBackgroundResource(R.drawable.shape_rectangle_focus);
                    txvStorage.setText( "Storage: USB2" );
                    selectPath = usb2Path;
                    Log.d( TAG, "onKeyDown: USB2 path = "+selectPath );
                }
                break;

            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                txvStatus.setText( "Recording" );
                chId = ProgramInfoGetList( ProgramInfo.ALL_TV_TYPE ).get(0).getChannelId();
                selectPath += "/test.ts";
                Log.d( TAG, "onKeyDown: chId = "+chId + " selectPath = "+selectPath );
                recId = PvrRecordStart(0, chId, selectPath, 60);
                handler.postDelayed( playRec, 6000 );
                Log.d( TAG, "onKeyDown: PvrRecordStart = "+recId );
        }
        return super.onKeyDown( keyCode, event );
    }
    long chId;
    int recId;
    Runnable playRec = new Runnable()
    {
        @Override
        public void run ()
        {
            PvrRecordStop( 0, recId );
            AvControlPlayStop(0);
            txvStatus.setText( "Playing" );
            int ret = PvrPlayStart(selectPath);
            Log.d( TAG, "onKeyDown: PvrPlayStart = "+ret );
            handler.postDelayed( idle, 5000 );
        }
    };
    Runnable idle = new Runnable()
    {
        @Override
        public void run ()
        {
            txvStatus.setText( "Idle" );
            PvrPlayStop();
            AvControlOpen( 0 );
            AvControlPlayByChannelId( 0, chId, ProgramInfo.ALL_TV_TYPE, 1 );
            selectPath = selectPath.replace("/test.ts", "");
        }
    };

    private void Mount()
    {
        txvHDD.setBackgroundResource( R.drawable.shape_rectangle_fail );
        txvUSB1.setBackgroundResource( R.drawable.shape_rectangle_fail );
        txvUSB2.setBackgroundResource( R.drawable.shape_rectangle_fail );
        txvHDD.setText( MountFail );
        txvUSB1.setText( MountFail );
        txvUSB2.setText( MountFail );

        if ( new File(getDefaultRecPath()).exists() )
        {
            txvHDD.setText( MountOK );
            txvHDD.setBackgroundResource( R.drawable.shape_rectangle_pass );
        }

        // edwin 20201217 check port index -s
        List<Integer> usbPortList = GetUsbPortList();
        StorageManager storageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper storageHelper = new PesiStorageHelper(storageManager);
        for (Object vol : getVolumes())
        {

            if (storageHelper.isUsb(vol))
            {
                //int portIndex = usbPortList.indexOf(storageHelper.getUsbPortNum(vol));
                if (storageHelper.isPort1(vol)) //if (portIndex == 0) //if(vol.devType.equals(getString(R.string.STR_USB_2_0))) // USB 2.0
                {
                    usb1Path = storageHelper.getPath(vol);
                    txvUSB1.setText( MountOK );
                    txvUSB1.setBackgroundResource( R.drawable.shape_rectangle_pass );
                }
                else if (storageHelper.isPort2(vol))
                {
                    usb2Path = storageHelper.getPath(vol);
                    txvUSB2.setText( MountOK );
                    txvUSB2.setBackgroundResource( R.drawable.shape_rectangle_pass );
                }
            }
        }
        // edwin 20201217 check port index -e
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

