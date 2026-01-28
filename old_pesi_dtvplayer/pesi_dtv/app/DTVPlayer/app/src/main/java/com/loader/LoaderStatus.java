package com.loader;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.LoaderInfo;


public class LoaderStatus extends DTVActivity {

    private TextView chipset_value;
    private TextView seq_value;
    private TextView sw_version_value;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader_status);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        show_status();

    }
    
    private void show_status(){
       chipset_value = (TextView)findViewById(R.id.chipset_value);
        seq_value = (TextView)findViewById(R.id.seqNum_value);
        sw_version_value = (TextView)findViewById(R.id.sw_ver_value);

        String chipid_text = String.format("0x%08X", LoaderDtvGetChipSetId());
        chipset_value.setText(chipid_text);
        String seq_text = String.format("0x%08X", LoaderDtvGetSTBSN());
        seq_value.setText(seq_text);
        String sw_text= String.format("0x%08X",LoaderDtvGetSWVersion());
        sw_version_value.setText(sw_text);
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
