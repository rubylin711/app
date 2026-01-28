package com.prime.homeplus.settings.system;

import android.app.ActionBar;
import android.content.Context;
import android.os.Handler;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormatDiskView extends ThirdLevelView {
    private String TAG = "HomePlus-FormatDiskView";
    private SettingsRecyclerView settingsRecyclerView;


    public FormatDiskView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_signal;
    }

    private Button btnSignal;
    private TextView tvSignal, tvSignalHint;


    public void onFocus() {
        btnSignal.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        tvSignal = (TextView) findViewById(R.id.tvSignal);
        tvSignalHint = (TextView) findViewById(R.id.tvSignalHint);

        tvSignal.setText(getContext().getString(R.string.settings_format_diskformat));
        tvSignalHint.setText(getContext().getString(R.string.settings_format_description));

        btnSignal = (Button) findViewById(R.id.btnSignal);

        btnSignal.setText(getContext().getString(R.string.settings_format_diskformat));

        btnSignal.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
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

        btnSignal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasDisk = true;
                if (hasDisk) {
                    showPopWindow(popupViewFormatList);
                } else {
                    showPopWindow(popupViewNoDisk);
                }
            }
        });
    }

    private View popupViewNoDisk, popupViewFormatList, popupViewFormatProgress, popupViewFormatResult;
    private static PopupWindow popupWindow;

    private Button btnOK, btnConfirm;
    private ListView lvFormatDisks;
    private TextView textView_format_result;

    private void initPopWindow() {
        popupViewNoDisk = LayoutInflater.from(getContext()).inflate(R.layout.dialog_format_disk, null);
        popupViewFormatList = LayoutInflater.from(getContext()).inflate(R.layout.dialog_format_list, null);
        popupViewFormatProgress = LayoutInflater.from(getContext()).inflate(R.layout.dialog_format_progress, null);
        popupViewFormatResult = LayoutInflater.from(getContext()).inflate(R.layout.dialog_format_result, null);

        btnOK  = (Button) popupViewNoDisk.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        lvFormatDisks = (ListView) popupViewFormatList.findViewById(R.id.lvFormatDisks);
        //lvFormatDisks.setAdapter(getListAdapter());
        lvFormatDisks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                LinearLayout itemLayout = (LinearLayout) lvFormatDisks.getChildAt(position);
//                TextView tvFormatUserLabel = (TextView) itemLayout.findViewById(R.id.formatUserLabel);
//                final String mCurrentSelectedUserLabel = tvFormatUserLabel.getText().toString();
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                // format as Android internal storage
// 獲取選中的磁碟 ID
                Map<String, Object> item = (Map<String, Object>) parent.getItemAtPosition(position);
                final String mCurrentSelectedUserLabel = (String) item.get("item_user_label");
                final String diskId = (String) item.get("item_disk_id"); // 這是上面 getListData 存入的
                LogUtils.d("diskId = "+diskId);
                showPopWindow(popupViewFormatProgress);
// 在線程中執行格式化
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            StorageManager sm = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);

                            // 【反射核心】調用 partitionPrivate(String diskId)
                            // ATV14 上，這個方法可能需要一些時間，且會觸發系統廣播
                            Method partitionPrivateMethod = sm.getClass().getMethod("partitionPublic", String.class);
                            partitionPrivateMethod.invoke(sm, diskId);

                            // 模擬等待系統處理 (因為上面的調用通常是異步觸發的)
                            Thread.sleep(3000);

                            // 回到主線程更新 UI
                            new Handler(getContext().getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if(textView_format_result != null) {
                                        textView_format_result.setText(String.format(getContext().getString(R.string.format_result), mCurrentSelectedUserLabel));
                                    }
                                    showPopWindow(popupViewFormatResult);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            // 錯誤處理
                        }
                    }
                }).start();
            }
        });

        textView_format_result = (TextView) popupViewFormatResult.findViewById(R.id.textView_format_result);
        btnConfirm = (Button) popupViewFormatResult.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private void showPopWindow(View view) {
        if (view != null) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            popupWindow = new PopupWindow(view, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
            popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
            lvFormatDisks.setAdapter(getListAdapter());
        }
    }

    private SimpleAdapter getListAdapter() {
        return new SimpleAdapter(getContext(), getListData(), R.layout.dialog_format_list_item,
                new String[]{"item_user_label", "item_size", "item_disk_id"},
                new int[]{R.id.formatUserLabel, R.id.formatSize, R.id.formatDiskId});
    }

    private List<Map<String, Object>> getListData() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        StorageManager sm = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);

        try {
            // 1. 反射獲取 getDisks()
            Method getDisksMethod = sm.getClass().getMethod("getDisks");
            List<?> disks = (List<?>) getDisksMethod.invoke(sm);

            for (Object disk : disks) {
                Class<?> diskInfoClass = disk.getClass();

                // 2. 獲取大小 (size 是 public field)
                Field sizeField = diskInfoClass.getField("size");
                long size = sizeField.getLong(disk);

                // 3. 檢查是否為 Stub (佔位符)
                boolean isStub = false;
                try {
                    // 嘗試反射調用 isStub() 方法 (舊版 API)
                    Method isStubMethod = diskInfoClass.getMethod("isStub");
                    isStub = (boolean) isStubMethod.invoke(disk);
                } catch (NoSuchMethodException e) {
                    // 這是您遇到的錯誤！如果找不到 isStub()，我們就忽略這個檢查。
                    // 在較新版本的 AOSP 中，如果它是有效的磁碟，通常它已經被過濾到 getDisks() 中了。
                }

                // 4. 過濾條件：非 stub 且 有容量
                if (!isStub && size > 0) {
                    Map<String, Object> map = new HashMap<String, Object>();

                    // 獲取 ID (格式化需要)
                    Method getIdMethod = diskInfoClass.getMethod("getId");
                    String id = (String) getIdMethod.invoke(disk);

                    // 獲取 Label (顯示名稱)
                    Method getDescriptionMethod = diskInfoClass.getMethod("getDescription");
                    String label = (String) getDescriptionMethod.invoke(disk);
                    if (label == null) {
                        label = "External Drive"; // 默認名稱
                    }

                    String sizeStr = Formatter.formatFileSize(getContext(), size);

                    map.put("item_user_label", label);
                    map.put("item_size", getContext().getString(R.string.format_disktotalspace) + ": " + sizeStr);
                    map.put("item_disk_id", id);

                    mList.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 處理其他反射異常
        }

        // 如果沒有檢測到磁盤，可以添加一個提示或者保持列表為空
        // 現有的邏輯會在 ViewCreated 中處理 empty 情況 (popupViewNoDisk)
//        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
//        Map<String, Object> map = new HashMap<String, Object>();
//        map = new HashMap<String, Object>();
//        map.put("item_user_label", "Disk A");
//        map.put("item_size", getContext().getString(R.string.format_disktotalspace) + ": 500 GB" );
//        map.put("item_disk_id", "DISK-TEST-001");
//        mList.add(map);
        LogUtils.d("mList = "+mList);
        return mList;
    }
}
