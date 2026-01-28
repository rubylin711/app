package com.prime.launcher.OTA;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OtaNewVersionCheckActivity extends BaseActivity {
    private static final String TAG = "OtaNewVersionCheckActivity" ;
    private Context g_context = null ;
    private String mKeyStr = "";
    public static final String ACS_SERVER_TYPE_PROPERTY = "persist.sys.prime.acs.server_type" ;
    public static final String ACS_SERVER_TYPE_PRODUCTION = "Production" ;
    public static final String ACS_SERVER_TYPE_Lab = "Lab" ;
    private final String OTA_DOWNLOAD_STATUS 				= "persist.sys.prime.ota_download_status";
    private final String OTA_DOWNLOAD_PERCENT 				= "persist.sys.prime.ota_download_percent";
    private final String AB_UPDATE_STATUS 				= "persist.sys.abupdate.status";
    private final String AB_UPDATE_PERCENT 				= "persist.sys.abupdate.percent";
    private TextView download_status_tv;
    private ProgressBar download_press_bar;
    private Handler handler;
    private int[] StatusString = {R.string.ota_status_idle,
                                        R.string.ota_status_downloading,
                                        R.string.ota_status_download_completed,
                                        R.string.ota_status_download_fail};
    private final Runnable BlockDownloadStatusRunnable = new Runnable() {
        private int dot = 0;
        @Override
        public void run() {
            int status = SystemProperties.getInt(OTA_DOWNLOAD_STATUS, 0);
            if(status>3)
                status = 0;
            String txt;
            if(status == 1) {
                int progress = SystemProperties.getInt(OTA_DOWNLOAD_PERCENT, 0);
                txt = g_context.getString(R.string.ota_status)+" : "+progress+"%";
                download_press_bar.setProgress(progress);
            }else{
                txt = g_context.getString(R.string.ota_status)+" : "+g_context.getString(StatusString[status]);
            }
            String upgrade_status = SystemProperties.get(AB_UPDATE_STATUS);
            LogUtils.d("upgrade_status = "+upgrade_status);
            if(upgrade_status.equals("downloading")){
                int progress = SystemProperties.getInt(AB_UPDATE_PERCENT, 0);
                txt = g_context.getString(R.string.ota_upgrade_status)+" : "+progress+"%";
                download_press_bar.setProgress(progress);
            }
            else if(upgrade_status.equals("verifying")){
                int progress = SystemProperties.getInt(AB_UPDATE_PERCENT, 0);
                txt = g_context.getString(R.string.ota_verify_status)+" : "+progress+"%";
                download_press_bar.setProgress(progress);
            }
            download_status_tv.setText(txt);

            handler.postDelayed(this, 1000);
        }

    };

    private  View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d( TAG, "v : " + v.getId() );
            Intent intent = new Intent();
            intent.setPackage("com.prime.acsclient");
            intent.setAction("com.prime.acsclient.renew_fw_info");
            g_context.sendBroadcast(intent);
            HomeApplication.get_prime_dtv().gpos_info_update_by_key_string(GposInfo.GPOS_OTA_MD5, "0");
            HomeApplication.get_prime_dtv().saveGposKeyValue(GposInfo.GPOS_OTA_MD5, "0");
        }
    };

    @SuppressLint("MissingInflatedId")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_new_ver_check);
        g_context = this ;
        LinearLayout linearLayout = findViewById(R.id.lo_last_check_linearlayout);
        linearLayout.setOnClickListener(onClickListener);

        Button button = findViewById(R.id.lo_check_ota_btn);
        button.setOnClickListener(onClickListener);


        TextView current_fw_ver = findViewById(R.id.lo_current_ver_txv);
        current_fw_ver.setText(current_fw_ver.getText() + "V" + SystemProperties.get("ro.firmwareVersion"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime());
        TextView last_check_time = findViewById(R.id.lo_last_check_time_date);
        last_check_time.setText(currentDate);

        download_status_tv = findViewById(R.id.lo_download_status);
        handler = new Handler(Looper.getMainLooper());

        download_press_bar = findViewById(R.id.lo_download_progressBar);
        download_press_bar.setMax(100);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_7) {
                this.mKeyStr += "7";
            } else if (keyCode == KeyEvent.KEYCODE_6) {
                this.mKeyStr += "6";
                Log.d(TAG,"mKeyStr = "+this.mKeyStr);
                if (this.mKeyStr.contentEquals("13197776")) {
                    showCustomDialog() ;
                }
            } else {
                this.mKeyStr = "";
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        handler.post(BlockDownloadStatusRunnable);
    }
    @Override
    public void onPause()
    {
        super.onPause();
        handler.removeCallbacks(BlockDownloadStatusRunnable);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    private void showCustomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_acs_server_select);
        dialog.setCancelable(true);

        RadioGroup radioGroup = dialog.findViewById(R.id.lo_radio_group);
        RadioButton radioOption1 = dialog.findViewById(R.id.lo_radio_option_1);
        RadioButton radioOption2 = dialog.findViewById(R.id.lo_radio_option_2);
        Button btnCancel = dialog.findViewById(R.id.lo_cancel_btn);
        Button btnSave = dialog.findViewById(R.id.lo_save_btn);
        String server_type = SystemProperties.get(ACS_SERVER_TYPE_PROPERTY) ;

        radioOption1.setText("Set " + ACS_SERVER_TYPE_PRODUCTION + " Environment : \n https://acs-portal.tbcnet.net.tw/provision") ;
        radioOption2.setText("Set " + ACS_SERVER_TYPE_Lab + " Environment : \n https://acs-sit.tbcnet.net.tw/provision");
        radioOption1.setChecked(true);
        radioOption2.setChecked(false);
        if ( server_type != null && server_type.equalsIgnoreCase(ACS_SERVER_TYPE_Lab) )
        {
            radioOption1.setChecked(false);
            radioOption2.setChecked(true);
        }

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if ( selectedId == R.id.lo_radio_option_1 )
            {
                SystemProperties.set(ACS_SERVER_TYPE_PROPERTY, ACS_SERVER_TYPE_PRODUCTION);
                Toast.makeText(this, "Used " + radioOption1.getText(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
            else if ( selectedId == R.id.lo_radio_option_2 )
            {
                SystemProperties.set(ACS_SERVER_TYPE_PROPERTY, ACS_SERVER_TYPE_Lab);
                Toast.makeText(this, "Used " + radioOption2.getText(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
            else
            {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
