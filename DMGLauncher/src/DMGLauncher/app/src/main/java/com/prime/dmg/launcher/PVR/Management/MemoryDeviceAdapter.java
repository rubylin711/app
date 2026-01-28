package com.prime.dmg.launcher.PVR.Management;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.CustomView.MessageDialog;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.Utils;

import java.lang.ref.WeakReference;
import java.util.List;

public class MemoryDeviceAdapter extends RecyclerView.Adapter<MemoryDeviceAdapter.Holder>{
    String TAG = MemoryDeviceAdapter.class.getSimpleName();

    private final WeakReference<AppCompatActivity> g_ref;
    private List<MemoryDeviceInfo> g_memory_device_Info_list;

    public MemoryDeviceAdapter(AppCompatActivity activity, List<MemoryDeviceInfo> memoryDeviceInfoList) {
        g_ref = new WeakReference<>(activity);
        g_memory_device_Info_list = memoryDeviceInfoList;
    }

    @NonNull
    @Override
    public MemoryDeviceAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_memory_device_list, parent, false);
        return new MemoryDeviceAdapter.Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryDeviceAdapter.Holder holder, int position) {
        //Log.d(TAG, "onBindViewHolder: position = " + position);
        on_key_listener(holder, position);
        setup_item_view(holder, position);
    }

    @Override
    public int getItemCount() {
        return g_memory_device_Info_list.size();
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    private void setup_item_view(MemoryDeviceAdapter.Holder holder, int position) {

        TextView usbDeviceUsed = holder.itemView.findViewById(R.id.lo_memory_device_usb_device_used);
        TextView usbDevice = holder.itemView.findViewById(R.id.lo_memory_device_usb_device);
        TextView totalSpace = holder.itemView.findViewById(R.id.lo_memory_device_total_space);
        TextView freeSpace = holder.itemView.findViewById(R.id.lo_memory_device_free_space);
        TextView usedSpace = holder.itemView.findViewById(R.id.lo_memory_device_used_space);
        //TextView type = holder.itemView.findViewById(R.id.lo_memory_device_type);

        MemoryDeviceInfo memoryDeviceInfo = g_memory_device_Info_list.get(position);
        boolean isEmpty = memoryDeviceInfo.get_usb_device() == null || memoryDeviceInfo.get_usb_device().isEmpty();

        if (isEmpty) {
            usbDevice.setText("");
            totalSpace.setText("");
            freeSpace.setText("");
            usedSpace.setText("");
            //type.setText("");
            holder.itemView.setFocusable(false);
            usbDeviceUsed.setBackground(null);
            usbDeviceUsed.setText("");
            return;
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) usbDevice.getLayoutParams();
        //Log.d(TAG, "setup_item_view: get_mount_usb_path() = " + Utils.get_mount_usb_path());
        //Log.d(TAG, "setup_item_view: get_usb_device() = " + memoryDeviceInfo.get_usb_device());
        if (Utils.get_mount_usb_path().equals(memoryDeviceInfo.get_usb_path())) {
            usbDeviceUsed.setBackground(get_activity().getResources().getDrawable(R.drawable.orange_gradient_btn2, null));
            usbDeviceUsed.setText(get_activity().getResources().getString(R.string.used));
        }
        else {
            usbDeviceUsed.setBackground(null);
            usbDeviceUsed.setText("");
        }

        usbDevice.setText(memoryDeviceInfo.get_usb_device());
        totalSpace.setText(memoryDeviceInfo.get_total_space() + " GB");
        freeSpace.setText(memoryDeviceInfo.get_free_space() + " GB");
        usedSpace.setText(memoryDeviceInfo.get_used_space() + " GB");
        //type.setText(memoryDeviceInfo.get_type());

    }

    private void on_key_listener(MemoryDeviceAdapter.Holder holder, int position) {
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.ACTION_DOWN != event.getAction())
                return false;

            if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                Log.i(TAG, "on_key_listener: eject usb device = " + g_memory_device_Info_list.get(position).get_usb_path());
                Utils.eject_usb_device(get_activity(), g_memory_device_Info_list.get(position).get_usb_path());
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
                Log.i(TAG, "on_key_listener: modify name = " + g_memory_device_Info_list.get(position).get_usb_path());
                show_modify_name_dialog(g_memory_device_Info_list.get(position));
                //Utils.modify_usb_name(get_activity(), g_memory_device_Info_list.get(position).get_usb_path());
                return true;
            }

            return false;
        });
    }

    private void show_modify_name_dialog(MemoryDeviceInfo memoryDeviceInfo) {
        MessageDialog messageDialog = new MessageDialog(get_activity());
        messageDialog.show_edit_panel(memoryDeviceInfo.get_usb_device());
        messageDialog.set_confirm_action(() -> {
            String name = messageDialog.get_input_text();
            Log.d(TAG, "show_modify_name_dialog: name = " + name);
            Utils.modify_usb_name(get_activity(), memoryDeviceInfo.get_usb_path(), name);
            ((MemoryDeviceSettingActivity)get_activity()).update_ui();
        });
    }

    private AppCompatActivity get_activity() {
        return g_ref.get();
    }

    public void update(List<MemoryDeviceInfo> memoryDeviceInfoList) {
        g_memory_device_Info_list = memoryDeviceInfoList;
        notifyDataSetChanged();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
