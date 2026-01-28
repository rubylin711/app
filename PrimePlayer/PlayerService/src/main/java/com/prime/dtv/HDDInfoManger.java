package com.prime.dtv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.utils.UsbUtils;

import java.util.List;

public class HDDInfoManger {
    private static final String TAG = "HDDInfoManger";
    private final Context mContext;
    private final Handler handler;
    private final PrimeDtvInterface.DTVCallback callback; // 新增回調
    private long mAlert_SizeMb;
    private final long mCritical_SizeMb;
    private Thread mThread = null;
    private boolean mIsRunning = false;


    public HDDInfoManger(Context context,PrimeDtvInterface.DTVCallback cb) {
        mContext = context;
        handler = new Handler(Looper.getMainLooper());
        callback = cb; // 設置回調
        mAlert_SizeMb = Pvcfg.getPvrHddLimitSize(); //預設警告大小為config size
        mCritical_SizeMb = Pvcfg.getPvrHddCriticalSize();
    }
    public void startMonitoring(long size_Mb) {
        mAlert_SizeMb = size_Mb;
        if (mThread != null && mThread.isAlive()) {
            Log.d(TAG, "監測執行緒已經運行中");
            return;
        }
        long totalSize = UsbUtils.get_usb_space_info().get(0);
        if (totalSize < 0) {
            Log.d(TAG, "沒有mount USB storage");
            return;
        }
        mIsRunning = true;

        mThread = new Thread(() -> {
            while (mIsRunning) {
                try {
                    List<Long> usbSize = UsbUtils.get_usb_space_info();
//                    Log.d(TAG, "startMonitoring: " + usbSize);
                    if(usbSize.get(0) < 0) {
                        Log.w(TAG, "⚠️ USB 總空間低於 0 MB！ USB Storage 已經被移除了!");
                    } else if (usbSize.get(1) < mCritical_SizeMb) {
                        handler.post(() -> {
                            Log.w(TAG, "⚠️ USB 剩餘空間低於 "+mCritical_SizeMb+"MB！");
                            if (callback != null) {
                                TVMessage msg = TVMessage.SetPvrDISKFULL();
                                callback.onMessage(msg);
                            }
                        });
                    } else if (usbSize.get(1) < mAlert_SizeMb) {
                        handler.post(() -> {
                            Log.w(TAG, "⚠️ USB 剩餘空間低於 "+mAlert_SizeMb+"MB！");
                            if (callback != null) {
                                TVMessage msg = TVMessage.NoHDDSpace();
                                callback.onMessage(msg);
                            }
                        });
                    }
                    Thread.sleep(3000); // 每 3 秒檢查一次
                } catch (InterruptedException e) {
                    Log.w(TAG, "監測執行緒中斷", e);
                    mIsRunning = false;

                }
            }
        });

        mThread.start();
        Log.d(TAG, "USB 監測執行緒已啟動");
    }

    public void stopMonitoring() {
        mIsRunning = false;
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        Log.d(TAG, "USB 監測執行緒已停止");
    }
}
