package com.prime.dmg.launcher.PVR.Management;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;

public class RecordManagementDialog extends BaseDialog {
    String TAG = RecordManagementDialog.class.getSimpleName();

    WeakReference<AppCompatActivity> g_ref;

    public RecordManagementDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>((AppCompatActivity) context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            //getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.dialog_pvr);
        init_ui();
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    private void init_ui() {
        TextView bookRecordManagement = findViewById(R.id.lo_pvr_dialog_book_record_management);
        TextView timerManagement = findViewById(R.id.lo_pvr_dialog_timer_management);
        TextView recordPrograms = findViewById(R.id.lo_pvr_dialog_record_programs);
        TextView memoryDeviceSetting= findViewById(R.id.lo_pvr_dialog_memory_device_setting);

        set_on_click_listener(bookRecordManagement, timerManagement, recordPrograms, memoryDeviceSetting);
    }

    private void set_on_click_listener(TextView bookRecordManagement, TextView timerManagement, TextView recordPrograms, TextView memoryDeviceSetting) {
        bookRecordManagement.setOnClickListener((view) ->{
            check_usb_path();
            String usbPath = Utils.get_mount_usb_path();
            Log.d(TAG, "set_on_click_listener: bookRecordManagement, [USB PATH] " + usbPath);

            Intent intent = new Intent(get_context(), BookRecordManagementActivity.class);
            get_context().startActivity(intent);
            dismiss();
        });

        timerManagement.setOnClickListener((view) ->{
            String usbPath = Utils.get_mount_usb_path();
            Log.d(TAG, "set_on_click_listener: timerManagement, [USB PATH] " + usbPath);

            if (null == usbPath || usbPath.isEmpty()) {
                Log.w(TAG, "set_on_click_listener: do not start " + TimerManagementActivity.class.getSimpleName() + " because USB path is null");
                HotkeyRecord hotkeyRecord = new HotkeyRecord((AppCompatActivity) get_context());
                hotkeyRecord.show_panel(R.string.pvr_usb_disk_is_extracted);
            }
            else {
                Intent intent = new Intent(get_context(), TimerManagementActivity.class);
                get_context().startActivity(intent);
            }
            dismiss();
        });

        recordPrograms.setOnClickListener((view) ->{
            String usbPath = Utils.get_mount_usb_path();
            Log.d(TAG, "set_on_click_listener: recordPrograms, [USB PATH] " + usbPath);

            if (null == usbPath || usbPath.isEmpty()) {
                Log.w(TAG, "set_on_click_listener: do not start " + RecordProgramsActivity.class.getSimpleName() + " because USB path is null");
                HotkeyRecord hotkeyRecord = new HotkeyRecord((AppCompatActivity) get_context());
                hotkeyRecord.show_panel(R.string.pvr_usb_disk_is_extracted);
            }
            else {
                Intent intent = new Intent(get_context(), RecordProgramsActivity.class);
                get_context().startActivity(intent);
            }
            dismiss();
        });

        memoryDeviceSetting.setOnClickListener((view) ->{
            String usbPath = Utils.get_mount_usb_path();
            Log.d(TAG, "set_on_click_listener: Memory Device Settings, [USB PATH] " + usbPath);

//            if (null == usbPath || usbPath.isEmpty()) {
//                Log.w(TAG, "set_on_click_listener: do not start " + MemoryDeviceSettingActivity.class.getSimpleName() + " because USB path is null");
//                HotkeyRecord hotkeyRecord = new HotkeyRecord((AppCompatActivity) get_context());
//                hotkeyRecord.show_panel(R.string.pvr_usb_disk_is_extracted);
//            }
//            else {
                Intent intent = new Intent(get_context(), MemoryDeviceSettingActivity.class);
                get_context().startActivity(intent);
//            }
            dismiss();
        });
    }

    private Context get_context() {
        return g_ref.get();
    }

    private void check_usb_path() {
        Context context = getContext();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Uri currentUri;
        String currentPath;

        Utils.set_mount_usb_path(null);
        for (StorageVolume currentVol : storageManager.getStorageVolumes()) {
            if (currentVol.getDirectory() == null)
                continue;
            currentUri = Uri.fromFile(currentVol.getDirectory());
            currentPath = null == currentVol.getDirectory() ? null : currentVol.getDirectory().getPath();
            Log.d(TAG, "check_usb_path: [currentUri] " + currentUri + ", [currentPath] " + currentPath);

            if (null == currentPath || currentPath.startsWith("/storage/emulated"))
                continue;

            Utils.set_mount_usb_path(currentPath);
            break;
        }
    }
}
