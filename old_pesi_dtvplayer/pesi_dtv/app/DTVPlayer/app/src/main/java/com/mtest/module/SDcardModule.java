package com.mtest.module;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import com.mtest.config.MtestConfig;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Activity.DTVActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class SDcardModule {
    private static final String TAG = "SDcardModule";
    private static final int PUBLIC = /*VolumeInfo.TYPE_PUBLIC*/ 0; // @hide, find another method
    private final WeakReference<Context> mContRef;
    private boolean mSDcardWaitCardOut = false;

    public SDcardModule(Context context) {
        mContRef = new WeakReference<>(context);
    }

    public void reset() // edwin 20201216 fix wrong return value
    {
        mSDcardWaitCardOut = false;
    }

    public List<Object> getVolumes() {

        Log.d(TAG, "getVolumes: ");
        return ((DTVActivity)mContRef.get()).getVolumes();
    }

    public int checkStatus() // edwin 20201216 fix wrong return value
    {
        StorageManager storageManager = (StorageManager)mContRef.get().getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        boolean cardIN = false;

        for (Object vol : getVolumes())
        {
            if (pesiStorageHelper.isSd(vol)) // edwin 20200522 fix sd card check
            {
                cardIN = true;
                break;
            }
        }

        Log.d(TAG, "checkStatus: WaitCardOut = " + mSDcardWaitCardOut+" , cardIN = " + cardIN);
        if (!mSDcardWaitCardOut)
        {
            if (cardIN)
            {
                mSDcardWaitCardOut = true;
                return MtestConfig.TEST_RESULT_WAIT_CARD_OUT;
            }
            else
                return MtestConfig.TEST_RESULT_FAIL;
        }
        else
        {
            if (cardIN)
                return MtestConfig.TEST_RESULT_WAIT_CARD_OUT;
            else
                return MtestConfig.TEST_RESULT_PASS;
        }
    }

    public int checkSdCardRW() // edwin 20201216 fix wrong return value
    {
        boolean sdCardPass = false;

        StorageManager storageManager = (StorageManager)mContRef.get().getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        for (Object vol : getVolumes())
        {
            if (pesiStorageHelper.isSd(vol))
            {
                sdCardPass = testReadWrite(pesiStorageHelper.getInternalPath(vol)); // edwin 20200519 replace service read write test
                break;
            }
        }

        Log.d(TAG, "checkSdCardRW: WaitCardOut = " + mSDcardWaitCardOut+" , sdCardPass = " + sdCardPass);
        if (!mSDcardWaitCardOut)
        {
            if (sdCardPass)
            {
                mSDcardWaitCardOut = true;
                return MtestConfig.TEST_RESULT_WAIT_CARD_OUT;
            }
            else
                return MtestConfig.TEST_RESULT_FAIL;
        }
        else
        {
            if (sdCardPass)
                return MtestConfig.TEST_RESULT_WAIT_CARD_OUT;
            else
                return MtestConfig.TEST_RESULT_PASS;
        }
    }

    private boolean testReadWrite (String internalPath) {
        String testStr = "test read / test write", readStr = null;
        try {
            File file = new File(internalPath, "test_read_write.txt");
            testWrite(file, testStr);
            readStr = testRead(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "testReadWrite:"
                + "\n testStr = " + testStr
                + "\n readStr = " + readStr);
        if (readStr == null)
            return false;
        return readStr.equals(testStr);
    }

    private void testWrite (File file, String str) {
        OutputStream os;
        try {
            os = new FileOutputStream(file, false);
            os.write(str.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String testRead (File file) {
        FileReader fr;
        String str = null;
        try {
            if (file.exists()) {
                fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                str = br.readLine();
                br.close();
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
}
