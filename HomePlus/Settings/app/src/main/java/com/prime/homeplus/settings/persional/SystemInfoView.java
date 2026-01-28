package com.prime.homeplus.settings.persional;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.SystemInfo;
import com.prime.homeplus.settings.InfoUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class SystemInfoView extends ThirdLevelView {
    private String TAG = "HomePlus-SystemInfoView";

    public SystemInfoView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_system_info;
    }

    public void onFocus() {
        Log.d(TAG, "Refresh info ...");
        getInfo();
    }

    private Button btnFocus;
    private TextView tvSTBNumber,tvMAC, tvSmartCardNumber, tvBouquetID,tvCAVersion;
    private TextView tvHardwareVersion, tvSoftwareVersion,tvSystemVersion,tvLoaderVersion,tvIP;
    private TextView tvPVR, tvDiskSerialNumber, tvWVID, tvAreaCode, tvCmMode, tvZipCode;
    private LinearLayout llIP;

    @Override
    public void onViewCreated() {
        tvSTBNumber = (TextView) findViewById(R.id.tvSTBNumber);
        tvSmartCardNumber = (TextView) findViewById(R.id.tvSmartCardNumber);
        tvBouquetID = (TextView) findViewById(R.id.tvBouquetID);
        tvCAVersion = (TextView) findViewById(R.id.tvCAVersion);

        tvMAC = (TextView) findViewById(R.id.tvMAC);
        tvHardwareVersion = (TextView) findViewById(R.id.tvHardwareVersion);
        tvSoftwareVersion = (TextView) findViewById(R.id.tvSoftwareVersion);
        tvSystemVersion = (TextView) findViewById(R.id.tvSystemVersion);
        tvLoaderVersion = (TextView) findViewById(R.id.tvLoaderVersion);
        tvIP = (TextView) findViewById(R.id.tvIP);

        tvPVR = (TextView) findViewById(R.id.tvPVR);
        tvDiskSerialNumber = (TextView) findViewById(R.id.tvDiskSerialNumber);
        tvWVID = (TextView) findViewById(R.id.tvWVID);
        tvAreaCode = (TextView) findViewById(R.id.tvAreaCode);
        tvCmMode = (TextView) findViewById(R.id.tvCmMode);
        tvZipCode = (TextView) findViewById(R.id.tvZipCode);

        llIP = (LinearLayout) findViewById(R.id.llIP);

        btnFocus = (Button) findViewById(R.id.btnFocus);

        btnFocus.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });
    }

    private void getPvrInfo() {
        String pvrFeature = getContext().getString(R.string.settings_system_info_devnotready);
        String pvrSN = "";
        pvrSN = PrimeUtils.get_hdd_serial();
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // get pvr related information
        if(Pvcfg.getPVR_PJ())
            pvrFeature = getContext().getString(R.string.settings_system_info_devready);

        tvPVR.setText(pvrFeature);
        tvDiskSerialNumber.setText(pvrSN);
    }

    @SuppressLint("WrongConstant")
    private void getWVInfo() {
        String wvSystemId = "12345678";

        SystemInfo systemInfo = PrimeUtils.get_system_info(getContext());
        if(systemInfo != null)
            wvSystemId = systemInfo.WV_id;

        tvWVID.setText(wvSystemId);
    }

    private void getInfo() {
        SystemInfo systemInfo = PrimeUtils.get_system_info(getContext());

        tvSTBNumber.setText(InfoUtils.getSN());
        tvMAC.setText(systemInfo.Ethernet_mac);
        tvHardwareVersion.setText(systemInfo.HW_version);
        tvSoftwareVersion.setText(systemInfo.SW_version);
        tvSystemVersion.setText(Build.VERSION.RELEASE);
        tvLoaderVersion.setText(systemInfo.Loader_version);
        tvIP.setText("0.0.0.0");
        tvSmartCardNumber.setText(InfoUtils.getCardSN());
        tvCAVersion.setText(systemInfo.CA_version);
        getPvrInfo();
        getWVInfo();

        int cmMode = systemInfo.CmMode;
        tvCmMode.setText("" + cmMode);
//        if (cmMode == 2) {
//            String bid = "0";
//            String areaCode = "0";
//            String zipCode = "0";
//            tvBouquetID.setText(bid);
//            tvAreaCode.setText(areaCode);
//            tvZipCode.setText(zipCode);
//        } else {

            tvBouquetID.setText(""+systemInfo.BouquetId);
            tvAreaCode.setText(systemInfo.AreaCode);
            tvZipCode.setText(systemInfo.Zip_code);
//        }
    }

}
