package com.prime.dtvplayer.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.StatFs;
import android.os.storage.StorageManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolphin.dtv.EnTableType;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Storage_device_setup extends DTVActivity
{
    private final String TAG = getClass().getSimpleName();

    private final static int BYTES_IN_1GB = 1073741824; // Edwin 20181211 define size in 1gb

    private final int TOTAL = 0;
    private final int FREE  = 1;
    private final int USE   = 2;

    private int curDeviceIndex;
    private RecyclerView rvUsbList;

    public class UsbInfo
    {
        boolean select = false;
        String path;
        String port;
        String strTotal;
        String strFree;
        String strUse;
        String type;
    }

    // Edwin 20181211 add detect of usb in Storage Settings -s
    private BroadcastReceiver mUnmountedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive ( Context context, Intent intent ) {
            String action = intent.getAction();

            Log.d(TAG, "onReceive: action = "+action);
            if ( action == null )
            {
                return;
            }
            else if ( action.equals(Intent.ACTION_MEDIA_MOUNTED) )
            {
                InitRecyclerView();
            }
            else if ( action.equals(Intent.ACTION_MEDIA_UNMOUNTED) )
            {
                InitRecyclerView();
            }

            Log.d(TAG, "onReceive: RecordPath = "+GetRecordPath());
            if ( !CheckUsbPathAvailable(GetRecordPath()) )
            {
                MessageDialogView.GetMessageDialogView().dismiss();
                finish();
            }
        }
    };
    // Edwin 20181211 add detect of usb in Storage Settings -e

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        Log.d(TAG, "onWindowFocusChanged: ");

        if (!hasFocus)
            return;

        int windowHeight;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        windowHeight = size.y;

        // edwin 20180802 check item null -s
        int itemHeight = (int) getResources().getDimension( R.dimen.LIST_VIEW_HEIGHT );
        if ( rvUsbList.getChildAt(0) != null )
        {
            itemHeight = rvUsbList.getChildAt(0).getMeasuredHeight();
        }
        // edwin 20180802 check item null -e
        int totalHeight     = (int)(windowHeight * 0.7) - itemHeight;
        int displayedCount  = totalHeight / itemHeight;

        rvUsbList.getLayoutParams().height = displayedCount * itemHeight;
        rvUsbList.setLayoutParams(rvUsbList.getLayoutParams());
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_device_setup);

        Log.d(TAG, "onCreate: ");

        SetTitle();
        InitRecyclerView();
        SubscribeBroadcast(); // Edwin 20181211 add detect of usb in Storage Settings
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        UnsubscribeBroadcast(); // Edwin 20181211 add detect of usb in Storage Settings
    }

    private void SubscribeBroadcast() // Edwin 20181211 add detect of usb in Storage Settings
    {
        Log.d(TAG, "SubscribeBroadcast: ");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mUnmountedReceiver, filter);
    }

    private void UnsubscribeBroadcast() // Edwin 20181211 add detect of usb in Storage Settings
    {
        Log.d(TAG, "UnsubscribeBroadcast: ");

        if ( mUnmountedReceiver == null )
        {
            return;
        }

        unregisterReceiver(mUnmountedReceiver);
        mUnmountedReceiver = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(TAG, "onKeyDown: ");

        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
            {
                SaveTable(EnTableType.GPOS); // connie 20180530 for save record path
            }
            break;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void InitRecyclerView()
    {
        int deviceCount = 0;
        String curRecPath = GetRecordPath();
        ArrayList<UsbInfo> usbInfoList = new ArrayList<>();
        //boolean isSelectUSB = false;
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper helper = new PesiStorageHelper(storageManager);

        // Add SD Card info
        //UsbInfo sdCard = GetSDCard();
        //usbInfoList.add(sdCard);

        // Add Other USB info
        for (Object vol : getVolumes()) // Edwin 20181207 simplify get volume list
        {
            if (helper.isUsb(vol))
            {
                deviceCount++;
                UsbInfo usbInfo = GetUsbInfo(vol);

                Log.d(TAG, "InitRecyclerView: curRecPath = " + curRecPath
                        + " , usbInfo.path = " + usbInfo.path);
                if(usbInfo.path.equals(curRecPath))
                {
                    usbInfo.select = true;
                    curDeviceIndex = deviceCount-1; // Johnny 20180802 fix crash if have two usb
                    //isSelectUSB = true;
                }

                usbInfoList.add(usbInfo);
            }
        }

        // not select USB
        //if( ! isSelectUSB )
        //{
        //    SelectSDCard(sdCard);
        //}

        // init RecyclerView setAdapter
        StorageAdapter storageAdapter = new StorageAdapter(usbInfoList);
        rvUsbList = (RecyclerView) findViewById(R.id.storageRecyclerV);
        rvUsbList.setAdapter(storageAdapter);
        storageAdapter.notifyDataSetChanged();
    }

//    private void SelectSDCard(UsbInfo sdCard)
//    {
//        Log.d(TAG, "SelectSDCard: ");
//
//        curDeviceIndex = 0;
//        String curRecPath = GetRecordPath();
//        sdCard.select = true;
//
//        if(!sdCard.path.equals(curRecPath))
//        {
//            String defaultPath = getString(R.string.STR_SDCARD_PATH);
//            SetRecordPath(defaultPath);
//            SaveTable(EnTableType.GPOS); // connie 20180530 for save record path
//        }
//    }

    private void SetTitle()
    {
        Log.d(TAG, "SetTitle: ");

        ActivityTitleView titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        titleView.setTitleView(getString(R.string.STR_STORAGE_SETTING_TITLE));
    }

//    private UsbInfo GetSDCard()
//    {
//        Log.d(TAG, "GetSDCard: ");
//
//        UsbInfo sdCard = new UsbInfo();
//        sdCard.path = getString(R.string.STR_SDCARD_PATH);
//        sdCard.type = getString(R.string.STR_SDCARD_TYPE);
//
//        String space[]  = GetMemorySpace(sdCard.path);
//        sdCard.strTotal = space[TOTAL];
//        sdCard.strFree  = space[FREE];
//        sdCard.strUse   = space[USE];
//        sdCard.port     = getString(R.string.STR_BUILD_IN);
//
//        return sdCard;
//    }

    private UsbInfo GetUsbInfo(Object vol)
    {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper helper = new PesiStorageHelper(storageManager);

        UsbInfo usbInfo = new UsbInfo();
        String space[];
        usbInfo.path = helper.getInternalPath(vol); // edwin 20210113 fix USB path, port
        usbInfo.type = helper.getFsType(vol);
        usbInfo.port = helper.isPort1(vol) ? getString(R.string.STR_USB_PORT1) : getString(R.string.STR_USB_PORT2);
        Log.d(TAG, "GetUsbInfo: path = " + usbInfo.path
                + " , type = " + usbInfo.type
                + " , port = " + usbInfo.port);

        //Scoty 20181025 change port to usb port1/port2 -e//Scoty 20181024 modfy new front/back usb port rule -e
        space = GetMemorySpace(usbInfo.path);
        usbInfo.strTotal    = space[TOTAL];
        usbInfo.strFree     = space[FREE];
        usbInfo.strUse      = space[USE];

        return usbInfo;
    }

    private String[] GetMemorySpace(String path)
    {
        Log.d(TAG, "GetMemorySpace: ");

        String space[]  = new String[3];
        StatFs sfIn     = new StatFs(path);
        long blockSize  = sfIn.getBlockSizeLong();
        long totalCount = sfIn.getBlockCountLong();
        long availCount = sfIn.getAvailableBlocksLong();
        double totalGSize = (double) blockSize * (double) totalCount / BYTES_IN_1GB; // Edwin 20181211 define size in 1gb
        double freeGSize  = (double) blockSize * (double) availCount / BYTES_IN_1GB;
        double usedGSize  = totalGSize - freeGSize;

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern("#0.00");

        space[TOTAL] = decimalFormat.format(totalGSize) + "G";
        space[FREE]  = decimalFormat.format(freeGSize)  + "G";
        space[USE]   = decimalFormat.format(usedGSize)  + "G";

        return space;
    }

    private class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> {

        ArrayList<UsbInfo> usbList;

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView wrokText;
            TextView portText;
            TextView deviceText;
            TextView totalText;
            TextView freeText;
            TextView useText;
            TextView typeText;
            public ViewHolder(View itemView)
            {
                super(itemView);
                wrokText    = (TextView) itemView.findViewById(R.id.workTXV);
                portText    = (TextView) itemView.findViewById(R.id.portTXV);
                deviceText  = (TextView) itemView.findViewById(R.id.deviceTXV);
                totalText   = (TextView) itemView.findViewById(R.id.totalTXV);
                freeText    = (TextView) itemView.findViewById(R.id.freeTXV);
                useText     = (TextView) itemView.findViewById(R.id.useTXV);
                typeText    = (TextView) itemView.findViewById(R.id.typeTXV);
                itemView.setFocusable(true);
            }
        }

        StorageAdapter(ArrayList<UsbInfo> storageList) {
            usbList = storageList;
        }

        @Override
        public StorageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.storage_list, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final StorageAdapter.ViewHolder holder, int position) {

            if (usbList == null) {
                return;
            }

            if(usbList.get(position).select)
                holder.wrokText.setText(getString(R.string.STR_V));
            else
                holder.wrokText.setText(null);

            // set Device Name
            String path = usbList.get(position).path;
            String[] tokens = path.split("/");
            holder.deviceText.setText(tokens[tokens.length-1]);

            holder.portText.setText(usbList.get(position).port);
            holder.totalText.setText(usbList.get(position).strTotal);
            holder.freeText.setText(usbList.get(position).strFree);
            holder.useText.setText(usbList.get(position).strUse);
            holder.typeText.setText(usbList.get(position).type);

            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.d(TAG, "onClick: Device");

                    // disable other device
                    usbList.get(curDeviceIndex).select = false;
                    notifyItemChanged(curDeviceIndex);

                    // enable current device
                    curDeviceIndex = holder.getAdapterPosition();
                    usbList.get(curDeviceIndex).select = true;
                    notifyItemChanged(curDeviceIndex);

                    // Reset Path by DTV SetRecordPath()
                    SetRecordPath(usbList.get(curDeviceIndex).path);
                }
            });

            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View itemView, boolean hasFocus)
                {
                    Log.d(TAG, "onFocusChange: Device");

                    if (hasFocus) {
                        holder.wrokText.setSelected(true);
                        holder.portText.setSelected(true);
                        holder.deviceText.setSelected(true);
                        holder.totalText.setSelected(true);
                        holder.freeText.setSelected(true);
                        holder.useText.setSelected(true);
                        holder.typeText.setSelected(true);
                    }
                    else {
                        holder.wrokText.setSelected(false);
                        holder.portText.setSelected(false);
                        holder.deviceText.setSelected(false);
                        holder.totalText.setSelected(false);
                        holder.freeText.setSelected(false);
                        holder.useText.setSelected(false);
                        holder.typeText.setSelected(false);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return usbList.size();
        }
    }
}
