package com.prime.btpair;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemProperties;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;

public class StbInfoDialog extends Dialog {
    private static final String TAG = "StbInfoDialog";
    private HookBeginActivity parent;

    public StbInfoDialog(HookBeginActivity activity)
    {
        super(activity);
        parent = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stb_info_constraint_layout);
    }

    @Override
    public void show()
    {
        super.show();
        init_data();
    }

    @SuppressLint("DefaultLocale")
    protected void init_data() {
        TextView firmware_Version = findViewById(R.id.stb_info_firmware_version_content);
        TextView slot_Index = findViewById(R.id.stb_info_slot_index_content);
        TextView number_SN = findViewById(R.id.stb_info_sn_number_content);
        TextView number_CASN = findViewById(R.id.stb_info_casn_number_content);

        String sn = SystemProperties.get("persist.sys.prime.device_sn", "unknown");        
        String firmwareVer = SystemProperties.get("ro.firmwareVersion", "unknown");
        String slotSuffix = SystemProperties.get("ro.boot.slot_suffix", "_unknown");
        String casn = SystemProperties.get("ro.boot.cstmsnno", "unknown");

        number_SN.setText(sn);
        firmware_Version.setText(firmwareVer);
        slot_Index.setText(slotSuffix.substring(1));
        number_CASN.setText(casn);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        parent.reset_show_info_flag();
    }

}
