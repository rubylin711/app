package com.prime.dmg.launcher.PVR.Management;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.CustomView.MessageDialog;
import com.prime.dmg.launcher.CustomView.ProgressDialog;
import com.prime.dmg.launcher.CustomView.Snakebar;
import com.prime.dmg.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.dmg.launcher.EPG.MyLinearLayoutManager;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.PrimeTimerReceiver;
import com.prime.dtv.PrimeUsbReceiver;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.utils.TVMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoryDeviceSettingActivity  extends BaseActivity {
    String TAG = MemoryDeviceSettingActivity.class.getSimpleName();

    int MENU= 0, MANAGEMENT_LIMIT = 1, HDD_REMOVE = 2, HDD_FORMAT = 3, HDD_MANAGEMENT = 4;

    private PrimeDtv g_PrimeDtv;
    private int g_view_status;
    private boolean g_MgrLimitFlag = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_device_setting);
    }

    @Override
    protected void onStart() {
        super.onStart();
        g_PrimeDtv = HomeApplication.get_prime_dtv();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        init_ui();
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "onBroadcastMessage: action == null");
            return;
        }

        switch (action) {
            case PrimeTimerReceiver.ACTION_TIMER_RECORD:
                BookInfo bookInfo = new BookInfo(intent.getExtras());
                Log.d(TAG, "onBroadcastMessage: ACTION_TIMER_RECORD " + bookInfo.getEventName());
                break;
            case PrimeUsbReceiver.ACTION_MEDIA_MOUNTED:
                Log.d(TAG, "onBroadcastMessage: ACTION_MEDIA_MOUNTED");
                update_ui();
                break;
            case PrimeUsbReceiver.ACTION_MEDIA_UNMOUNTED:
                Log.d(TAG, "onBroadcastMessage: ACTION_MEDIA_UNMOUNTED");
                update_ui();
                break;
        }
    }

    private void init_ui() {
        setup_space_info();
        //setup_usb_list();
        set_dvr_mgr_limit();
        set_listener();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (g_view_status == MENU) {
                finish();
                return true;
            }
            else {
                hide_view();
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    private void setup_space_info() {
        String usbPath = Utils.get_mount_usb_path();
        TextView spaceMessage = findViewById(R.id.memory_device_hdd_space_message);
        ProgressBar progressBar = findViewById(R.id.memory_device_hdd_space_progress);
        if (usbPath == null || usbPath.isEmpty() ) {
            spaceMessage.setText(getString(R.string.dvr_mgr_hdd_not_ready));
            progressBar.setProgress(0);

        } else {
            int recordCount = 0;
            List<Long> usbSize = Utils.get_usb_space_info();
            long totalSize = usbSize.get(0);
            long availableSize = usbSize.get(1);
            long usedSize = totalSize - availableSize;
            int percent = (int) (usedSize * 100 / totalSize);

            Log.d(TAG, "setup_space_info: Total Size     : " + totalSize + " MB");
            Log.d(TAG, "setup_space_info: Available Size : " + availableSize + " MB");

            progressBar.setMax(100);
            progressBar.setProgress(percent);

            if (g_PrimeDtv != null) {
                g_PrimeDtv.pvr_init(usbPath);
                recordCount = g_PrimeDtv.pvr_GetRecCount();
            }

            spaceMessage.setText(getString(R.string.dvr_quota_space_hint, percent +"%", recordCount));
        }
    }

    private void setup_usb_list() {
        MiddleFocusRecyclerView memoryDeviceListView = findViewById(R.id.lo_memory_device_settings_usb_list);
        memoryDeviceListView.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(memoryDeviceListView.getItemAnimator())).setSupportsChangeAnimations(false);
        memoryDeviceListView.setLayoutManager(new MyLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        List<MemoryDeviceInfo> memoryDeviceInfoList = get_memory_device_list();
        //fill_list_size_to_9(memoryDeviceInfoList);
        memoryDeviceListView.setAdapter(new MemoryDeviceAdapter(this, memoryDeviceInfoList));
    }

    private List<MemoryDeviceInfo> get_memory_device_list() {
        List<MemoryDeviceInfo> memoryDeviceInfoList = new ArrayList<MemoryDeviceInfo>();
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        Uri currentUri;
        String currentPath;
        long totalSize = -1;
        long availableSize = 0;
        long usedSize = 0;
        boolean need_set_usb_path = false;

        if (Utils.get_mount_usb_path() == null) {
            Log.i(TAG, "get_memory_device_list: get_mount_usb_path() == null");
            need_set_usb_path = true;
        }

        for (StorageVolume currentVol : storageManager.getStorageVolumes()) {
            if (currentVol.getDirectory() == null)
                continue;

            currentUri = Uri.fromFile(currentVol.getDirectory());
            currentPath = null == currentVol.getDirectory() ? null : currentVol.getDirectory().getPath();
            Log.d(TAG, "get_memory_device_list: [currentUri] " + currentUri + ", [currentPath] " + currentPath);

            if (null == currentPath || currentPath.startsWith("/storage/emulated"))
                continue;

            if (need_set_usb_path) {
                Utils.set_mount_usb_path(currentPath);
                need_set_usb_path = false;
                g_PrimeDtv.hdd_monitor_start(Pvcfg.getPvrHddLimitSize());
                g_PrimeDtv.pvr_init(currentPath);
                Log.i(TAG, "get_memory_device_list: mount_usb_path() = " + Utils.get_mount_usb_path());
            }

            File usbFile    = new File(currentVol.getDirectory().getPath());
            StatFs statFs   = new StatFs(usbFile.getPath());
            totalSize       = (statFs.getBlockCountLong() * statFs.getBlockSizeLong()) / (1024 * 1024 * 1024);
            availableSize   = (statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong()) / (1024 * 1024 * 1024);
            usedSize = totalSize - availableSize;
            Log.d(TAG, "get_memory_device_list: Total Size: " + totalSize + " GB");
            Log.d(TAG, "get_memory_device_list: Available Size: " + availableSize + " GB");
            Log.d(TAG, "get_memory_device_list: Used Size: " + usedSize + " GB");

            String uuid = Utils.get_usb_uuid(currentPath);
            MemoryDeviceInfo memoryDeviceInfo = new MemoryDeviceInfo(Utils.get_usb_name(this, uuid), currentPath, totalSize, availableSize, usedSize, "fileSystemType");
            memoryDeviceInfoList.add(memoryDeviceInfo);
        }

        return memoryDeviceInfoList;
    }

    public void fill_list_size_to_9(List<MemoryDeviceInfo> memoryDeviceInfoList) {
        int size = memoryDeviceInfoList.size();
        for (int i = 0; i < 9 - size; i++)
            memoryDeviceInfoList.add(new MemoryDeviceInfo());
    }

    public void update_ui() {
        List<MemoryDeviceInfo> memoryDeviceInfoList = get_memory_device_list();
        Log.d(TAG, "update_ui: " + memoryDeviceInfoList.size());

        runOnUiThread(() ->{
            setup_space_info();

            if (g_view_status == HDD_MANAGEMENT) {
                //fill_list_size_to_9(memoryDeviceInfoList);
                MiddleFocusRecyclerView usbListView = findViewById(R.id.lo_memory_device_settings_usb_list);
                ((MemoryDeviceAdapter) Objects.requireNonNull(usbListView.getAdapter())).update(memoryDeviceInfoList);
                usbListView.requestFocus();
            }
        });
    }

    private void set_listener() {
        TextView dvrMgrLimit = findViewById(R.id.dvr_mgr_limit);
        dvrMgrLimit.setOnClickListener((View) -> on_click_DvrMgrLimit());

        TextView dvrHddRemove = findViewById(R.id.dvr_mgr_hdd_remove);
        dvrHddRemove.setOnClickListener((View) -> on_click_DvrHddRemove());

        TextView dvrHddFormatting = findViewById(R.id.dvr_mgr_hdd_formatting);
        dvrHddFormatting.setOnClickListener((View) -> on_click_DvrHddFormatting());

        TextView dvrHddManagement = findViewById(R.id.dvr_mgr_hdd_management);
        dvrHddManagement.setOnClickListener((View) -> on_click_DvrHddManagement());

    }

    private void on_click_DvrMgrLimit() {
        TextView dvrMgrLimitHint = findViewById(R.id. dvr_mgr_limit_hint);

        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        GposInfo gposInfo = primeDtv.gpos_info_get();
        int dvrManagementLimit = gposInfo.getDVRManagementLimit();
        Log.d(TAG, "on_click_DvrMgrLimit: dvrManagementLimit = " + dvrManagementLimit);
        if (dvrManagementLimit == GposInfo.MANAGEMENT_AUTO_CLEAR) {
            dvrMgrLimitHint.setText(R.string.dvr_mgr_manual_clear);
            dvrManagementLimit = GposInfo.MANAGEMENT_MANUAL_CLEAR;
        }
        else {
            dvrMgrLimitHint.setText(R.string.dvr_mgr_auto_clear);
            dvrManagementLimit = GposInfo.MANAGEMENT_AUTO_CLEAR;
        }
        primeDtv.gpos_info_update_by_key_string(GposInfo.GPOS_DVR_MANAGEMENT_LIMIT, dvrManagementLimit);
        primeDtv.saveGposKeyValue(GposInfo.GPOS_DVR_MANAGEMENT_LIMIT, dvrManagementLimit);
    }

    private void on_click_DvrHddRemove() {
        String usbPath = Utils.get_mount_usb_path();
        if (null == usbPath || usbPath.isEmpty()) {
            Log.w(TAG, "set_on_click_listener: do not start " + RecordProgramsActivity.class.getSimpleName() + " because USB path is null");
            HotkeyRecord hotkeyRecord = new HotkeyRecord(this);
            hotkeyRecord.show_panel(R.string.pvr_message_hdd_not_ready);
            return;
        }

        MessageDialog messageDialog = new MessageDialog(this);
        messageDialog.show_panel(R.string.dvr_mgr_hdd_remove, true);
        messageDialog.set_confirm_action(this::remove_hdd);
    }

    private void on_click_DvrHddFormatting() {
        String usbPath = Utils.get_mount_usb_path();
        if (null == usbPath || usbPath.isEmpty()) {
            Log.w(TAG, "set_on_click_listener: do not start " + RecordProgramsActivity.class.getSimpleName() + " because USB path is null");
            HotkeyRecord hotkeyRecord = new HotkeyRecord(this);
            hotkeyRecord.show_panel(R.string.pvr_message_hdd_not_ready);
            return;
        }

        MessageDialog messageDialog = new MessageDialog(this);
        messageDialog.show_panel(R.string.pvr_message_hdd_format_check, true);
        messageDialog.set_confirm_action(this::format_hdd);
    }

    private void on_click_DvrHddManagement() {

        ConstraintLayout hddManagementView = findViewById(R.id.memory_device_hdd_management);
        hddManagementView.setVisibility(View.VISIBLE);

        MiddleFocusRecyclerView memoryDeviceListView = findViewById(R.id.lo_memory_device_settings_usb_list);
        memoryDeviceListView.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(memoryDeviceListView.getItemAnimator())).setSupportsChangeAnimations(false);
        memoryDeviceListView.setLayoutManager(new MyLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        List<MemoryDeviceInfo> memoryDeviceInfoList = get_memory_device_list();
        //fill_list_size_to_9(memoryDeviceInfoList);
        memoryDeviceListView.setAdapter(new MemoryDeviceAdapter(this, memoryDeviceInfoList));

        //Log.d(TAG, "on_click_DvrHddManagement: size = " + memoryDeviceInfoList.size());
        //if(!memoryDeviceInfoList.isEmpty())
        //    memoryDeviceListView.getChildAt(0).requestFocus();

        g_view_status = HDD_MANAGEMENT;
        ConstraintLayout memoryDeviceMenu = findViewById(R.id.memory_device_menu);
        memoryDeviceMenu.setVisibility(View.GONE);

        hide_hint_key_menu();
        show_hint_key_hdd_mgr();
    }

    private void hide_view() {
        if (g_view_status == HDD_MANAGEMENT) {
            ConstraintLayout hddManagementView = findViewById(R.id.memory_device_hdd_management);
            hddManagementView.setVisibility(View.GONE);

            ConstraintLayout memoryDeviceMenu = findViewById(R.id.memory_device_menu);
            memoryDeviceMenu.setVisibility(View.VISIBLE);

            TextView dvrHddManagement = findViewById(R.id.dvr_mgr_hdd_management);
            dvrHddManagement.requestFocus();

            show_hint_key_menu();
            hide_hint_key_hdd_mgr();
        }

        g_view_status = MENU;
    }
    
    private void format_hdd() {
        Log.d(TAG, "format_hdd: ");
        String usb_name = Utils.get_usb_uuid();

        String message = String.format(getString(R.string.storage_format_success), usb_name);
        ProgressDialog ejecting = new ProgressDialog(this, getString(R.string.storage_wizard_format_progress_title));
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();

        Runnable ejectUsbDisk = () -> {
            // close database
            primeDtv.pvr_deinit();

            // eject usb device
            boolean success = Utils.format_usb_device(this);
            Log.d(TAG, "format_hdd: [ejecting success] " + success);

            // dismiss & show message
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                ejecting.dismiss();
                Snakebar.show(this, message, Snakebar.LENGTH_LONG);
            }, 3000);
        };

        ejecting.set_title(message);
        ejecting.set_start_action(ejectUsbDisk);
        ejecting.show();
    }

    private void remove_hdd() {
        Log.d(TAG, "remove_hdd: ");
        String usb_name = Utils.get_usb_uuid();
        Log.d(TAG, "remove_hdd: usb_path = " + usb_name);
        show_ejecting(usb_name);
    }

    private void show_hint_key_hdd_mgr() {
        LinearLayout hintRed = findViewById(R.id.lo_memory_device_settings_hint_red);
        hintRed.setVisibility(View.VISIBLE);

        LinearLayout hintYellow = findViewById(R.id.lo_memory_device_settings_hint_yellow);
        hintYellow.setVisibility(View.VISIBLE);
    }

    private void hide_hint_key_hdd_mgr() {
        LinearLayout hintRed = findViewById(R.id.lo_memory_device_settings_hint_red);
        hintRed.setVisibility(View.GONE);

        LinearLayout hintYellow = findViewById(R.id.lo_memory_device_settings_hint_yellow);
        hintYellow.setVisibility(View.GONE);
    }

    private void show_hint_key_menu() {
        LinearLayout hintOk = findViewById(R.id.lo_memory_device_settings_hint_ok);
        hintOk.setVisibility(View.VISIBLE);
    }

    private void hide_hint_key_menu() {
        LinearLayout hintOk = findViewById(R.id.lo_memory_device_settings_hint_ok);
        hintOk.setVisibility(View.GONE);
    }

    private void set_dvr_mgr_limit() {
        TextView dvrMgrLimitHint = findViewById(R.id. dvr_mgr_limit_hint);
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        GposInfo gposInfo = primeDtv.gpos_info_get();
        int dvrManagementLimit = gposInfo.getDVRManagementLimit();
        if (dvrManagementLimit == GposInfo.MANAGEMENT_AUTO_CLEAR) {
            dvrMgrLimitHint.setText(R.string.dvr_mgr_auto_clear);
        }
        else {
            dvrMgrLimitHint.setText(R.string.dvr_mgr_manual_clear);
        }
    }
}
