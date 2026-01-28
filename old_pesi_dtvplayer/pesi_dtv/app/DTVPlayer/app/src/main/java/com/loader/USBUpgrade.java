package com.loader;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

public class USBUpgrade extends DTVActivity {

    private Button bnUpdate;
    private Button bnExit;
    private int status;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbupgrade);
        
        bnUpdate = (Button)findViewById(R.id.usb_ok_btn);
        bnExit   = (Button)findViewById(R.id.usb_exit_btn);
        
        bnUpdate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int ret;
                ret = UpdateUsbSoftWare(getResources().getString(R.string.STR_LOADER_DTV_UPDATE_FILE));
                if(ret != 0)
                    show_err_toast(getResources().getString(R.string.STR_LOADER_DTV_USB_UPGRADE_FAILED));
            }
        });
        
        bnExit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {finish();
            }
        });
    }

    private void show_err_toast(String msg)
    {
        TextView title = (TextView)findViewById(R.id.t_usb_title);
        Toast err = Toast.makeText(USBUpgrade.this, msg, Toast.LENGTH_SHORT);
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
