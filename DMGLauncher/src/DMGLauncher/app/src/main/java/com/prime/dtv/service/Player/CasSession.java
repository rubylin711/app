package com.prime.dtv.service.Player;

import android.media.MediaCas;
import android.media.MediaCasException;
import android.media.MediaCasStateException;
import android.media.tv.tuner.Descrambler;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.SectionEvent;
import android.media.tv.tuner.filter.SectionSettingsWithSectionBits;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.dtv.CasRefreshHelper;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Util.HttpURLConnectionUtil;
import com.prime.dtv.service.Util.Utils;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CasSession implements MediaCas.EventListener {
    private static final String TAG = "CasSession";
    public static final String DEFAULT_PROVISIONING_URL =
            "https://www.googleapis.com/certificateprovisioning/v1/devicecertificates/";
    public static final String DEFAULT_PROVISION_KEY =
            "create?key=AIzaSyB-5OLKTx2iU5mko18DfdwK5611JIjbUhE&signedRequest=";
    public static final String DEFAULT_LICENSE_URL = Pvcfg.getWvcasLicenseUrl();
    private static final int INDIVIDUALIZATION_REQUEST = 1000;
    private static final int INDIVIDUALIZATION_RESPONSE = 1001;
    private static final int INDIVIDUALIZATION_COMPLETE = 1002;

    private static final int LICENSE_REQUEST = 2000;
    private static final int LICENSE_RESPONSE = 2001;
    private static final int CAS_ERROR_DEPRECATED = 2002; // DEPRECATED
    private static final int LICENSE_RENEWAL_REQUEST = 2003;
    private static final int LICENSE_RENEWAL_RESPONSE = 2004;
    private static final int LICENSE_RENEWAL_URL = 2005;
    private static final int LICENSE_CAS_READY = 2006;
    private static final int LICENSE_CAS_RENEWAL_READY = 2007;
    private static final int LICENSE_REMOVAL = 2008;
    private static final int LICENSE_REMOVED = 2009;

    private static final int CAS_SESSION_ID = 3000;
    private static final int SET_CAS_SOC_ID = 3001; // DEPRECATED
    private static final int SET_CAS_SOC_DATA = 3002; // DEPRECATED

    private static final int CAS_ERROR = 5000;

    private static final int LICENSE_EXPIRED = -4002;

    private HandlerThread mMediaCasHandlerThread;
    private Handler mMediaCasHandler;

    private HandlerThread mECMHandlerThread;
    private Handler mECMHandler;
    private static final int ECM_INIT = 0;
    private static final int ECM_STOP = 1;
    private static final int ECM_SWITCH = 2;

    private final Tuner mTuner;
    private MediaCas mMediaCas;
    private final int mEcmPid;
    private final int mCaSystemId;
    private MediaCas.Session mMediaCasSession;
    private Filter mEcmFilter;
    private Descrambler mDescrambler;
    private final Object mLockProvisioning;
    private final Object mLockCasReady;
//    private int mStartCount;
//    private int mEcmCount;
    private boolean mIsProvisioned;
    private boolean mIsLicenseReady;
    private boolean mIsRefresh = false;
    private boolean mIsClosing = false;
    private boolean mIsOpening = false;
    private byte[] mPrivateData;
    private byte[] mPreEcm = null;
    private final List<Integer> mPidList = new ArrayList<>();
    private String mContentId = "";
    private final CasRefreshHelper mCasRefreshHelper;
    private Callback mCallback;
    private DataManager mDatamanager = null;
    private boolean mIsReOpening = false;
    private int ecm_error_count = 0;

    private Executor getExecutor() {
        return Runnable::run;
//        return Executors.newFixedThreadPool(1);
    }

    private HttpURLConnectionUtil.Callback getHttpConnectionCallback() {
        return new HttpURLConnectionUtil.Callback() {
            @Override
            public void onHttpResponseError(int responseCode) {
                if (mCallback != null) {
                    mCallback.onHttpError(responseCode, mContentId);
                }
            }

            @Override
            public void onConnectTimeout(String msg) {
                if (mCallback != null) {
                    mCallback.onConnectTimeout(msg);
                }
            }
        };
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private FilterCallback getEcmFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent e : events) {
                    if (e instanceof SectionEvent) {
                        int dataLen = ((SectionEvent) e).getDataLength();
                        byte[] ecm = new byte[dataLen];

//                        Log.d(TAG, "onFilterEvent, length = " + dataLen);
                        try {
                            filter.read(ecm, 0, dataLen);
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                        int table_id = Utils.getInt(ecm, 0, 1, Utils.MASK_8BITS);
                        //if(openDebug()) {
                        //    LogUtils.d("Read ECM mEcmPid = " + mEcmPid + " table_id = " + table_id);
                        //    LogUtils.d("mIsLicenseReady = "+mIsLicenseReady+" mMediaCasSession = "+mMediaCasSession);
                        //}
                        try {
                                if (mIsLicenseReady
                                        && mMediaCasSession != null
                                        && !Arrays.equals(mPreEcm, ecm)) {
                                    String ecmStr = bytesToHex(ecm);
                                    //if(mEcmPid == 7726) {
                                        Log.e(TAG, "Read ECM mEcmPid = " + mEcmPid + " table_id = " + table_id+" mContentId "+ mContentId);
                                        Log.e(TAG, "onFilterEvent, processEcm = " + ecmStr);
                                    //}
                                    mMediaCasSession.processEcm(ecm);
                                    mPreEcm = ecm;
                                    //switchECMFilter(table_id);cd d:
                                    //handler_stop_ecm();
                                    //handler_start_ecm(table_id);
                                    //handler_swtich_ecm(table_id);
                                    ecm_error_count = 0;
                                }
                            } catch (Exception ex) {
                                LogUtils.d("ECM pid "+mEcmPid);
                                ecm_error_count++;
                                if(ecm_error_count >10){
                                    mPreEcm = ecm;
                                    if (mCallback != null) {
                                       mCallback.onCasError(ex.toString());
                                    }
                                }

                                //handler_start_ecm(0);
                                ex.printStackTrace();
                                //handler_swtich_ecm(table_id);
                            }
                    }
                }
            }
            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
                Log.d(TAG, "onFilterStatusChanged: status = " + status);
            }
        };
    }

    private boolean openDebug() {
        int value = SystemProperties.getInt("persist.sys.prime.debug.ecm_pid", 0);
        //LogUtils.d("value = "+ value+ " mEcmPid = "+mEcmPid);
        if(value == mEcmPid)
            return true;
        else
            return false;
    }

    public CasSession(int caSystemId) {
        mCaSystemId = caSystemId;

        mLockProvisioning = new Object();
        mLockCasReady = new Object();

        mCasRefreshHelper = CasRefreshHelper.get_instance();
        mEcmPid = 0;
        mTuner = null;
        mDatamanager = DataManager.getDataManager();
    }

    private String getWVLicenseURL(){
        String url;
        if(mDatamanager != null) {
            url = mDatamanager.getGposInfo().getWVCasLicenseURL();
            if(url.length() == 0){
                url = Pvcfg.getWvcasLicenseUrl();
            }
            else{
                url += "/fortress/wvcas/license";
            }
        }else {
            url = Pvcfg.getWvcasLicenseUrl();
        }
        return url;
    }

    public CasSession(Tuner tuner, int caSystemId, int ecmPid) {
        mTuner = tuner;
        mEcmPid = ecmPid;
        mCaSystemId = caSystemId;

        mDatamanager = DataManager.getDataManager();
        mLockProvisioning = new Object();
        mLockCasReady = new Object();

        mCasRefreshHelper = CasRefreshHelper.get_instance();

    }

    private void handler_start_ecm(int table_id) {
        if (mECMHandler != null) {
            Message message = new Message();
            message.what = ECM_INIT;
            message.arg1 = table_id;
            mECMHandler.handleMessage(message);
        }
    }
    private void handler_stop_ecm(){
        if (mECMHandler != null) {
            Message message = new Message();
            message.what = ECM_STOP;
            mECMHandler.handleMessage(message);
        }
    }
    private void handler_swtich_ecm(int table_id){
        if (mECMHandler != null) {
            Message message = new Message();
            message.what = ECM_SWITCH;
            message.arg1 = table_id;
            mECMHandler.handleMessage(message);
        }
    }
    public void run(){
        Thread thread = open_cas_thread();
        thread.start();
    }
    private Thread open_cas_thread(){
        Runnable runnable = () -> {
            open();
        };
        Thread thread = new Thread(runnable,"open_cas_thread");
        return thread;
    }
    private void create_ecm_handler_thread(){
        if(mECMHandlerThread == null) {
            mECMHandlerThread = new HandlerThread("pesi-ecm-handler-thread");
            mECMHandlerThread.start();
            mECMHandler = new Handler(mECMHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case ECM_INIT: {
                            LogUtils.d("ECM_INIT = >startEcmFilter");
                            startEcmFilter(msg.arg1);
                        }
                        break;
                        case ECM_STOP: {
                            stopEcmFilter();
                        }
                        break;
                        case ECM_SWITCH: {
                            switchEcmFilter(msg.arg1);
                        }
                        break;
                    }

                }
            };
        }
    }
    public void open() {
        Log.d(TAG, "open --start");
        mIsClosing = false;
        mIsOpening = true;
        try {
            mMediaCas = new MediaCas(mCaSystemId);
            mMediaCasHandlerThread = new HandlerThread("pesi-mediacas-handler-thread");
            mMediaCasHandlerThread.start();
            mMediaCasHandler = new Handler(mMediaCasHandlerThread.getLooper());
            mMediaCas.setEventListener(this, mMediaCasHandler);


            provision();
            openSession();
            mDescrambler = mTuner.openDescrambler();
            //Log.d(TAG, "mTuner = "+mTuner+"mDescrambler = "+mDescrambler);
            Log.d(TAG, "mEcmPid "+mEcmPid+" open: session id = " + Arrays.toString(mMediaCasSession.getSessionId())+" mTuner = "+mTuner+" mDescrambler = "+mDescrambler);
            mDescrambler.setKeyToken(getDescrambleKeyToken(mMediaCasSession.getSessionId()));

            LogUtils.d("mPrivateData = "+Utils.bytesToHex(mPrivateData));
            mMediaCasSession.setPrivateData(mPrivateData);
            //mMediaCas.setPrivateData(mPrivateData);

            //startEcmFilter();
            mPreEcm = null;
            //if(mIsReOpening == false) 
			{
                create_ecm_handler_thread();
                handler_start_ecm(0);
            }
            mIsOpening = false;
            Log.d(TAG, "open --end");
        }catch (MediaCasStateException e) {
            LogUtils.d("MediaCasStateException");
            e.printStackTrace();
            if(mCallback != null) {
                String licenseId = mCasRefreshHelper.get_license_id(mContentId);
                mCallback.onRemoveLicense(licenseId);
            }
        }catch (Exception e) {
            if (mCallback != null) {
                //mCallback.onCasError(e.toString());
            }
            e.printStackTrace();
        }
    }

    public void openForRefresh() {
        mIsRefresh = true;
        mIsClosing = false;
        try {
            LogUtils.d("mContentId "+ mContentId);
            mMediaCas = new MediaCas(mCaSystemId);
            mMediaCasHandlerThread = new HandlerThread("pesi-mediacas-handler-thread");
            mMediaCasHandlerThread.start();
            mMediaCasHandler = new Handler(mMediaCasHandlerThread.getLooper());
            mMediaCas.setEventListener(this, mMediaCasHandler);

            provision();
            openSession();

            mMediaCasSession.setPrivateData(mPrivateData);
            //mMediaCas.setPrivateData(mPrivateData);
        } catch (Exception e) {
            if (mCallback != null) {
                mCallback.onCasError(e.toString());
            }
			close();
            e.printStackTrace();
        }
    }

    private byte[] getDescrambleKeyToken(byte[] sessionId) {
        int length = sessionId.length;
        byte[] keyToken = new byte[length];

        // reverse sessionId
        for (int i = 0, j = length - 1 ; i < length ; i++, j--) {
            keyToken[j] = sessionId[i];
        }

        return keyToken;
    }

    public void internal_close() {
        LogUtils.d("close: mEcmPid = "+mEcmPid+" mContentId "+ mContentId);
        mIsClosing = true;

        if (mECMHandlerThread != null) {
            Log.d(TAG, "close: mECMHandlerThread");
            mECMHandlerThread.quitSafely();
            mECMHandlerThread = null;

            // remove all pending callback and msg
            mECMHandler.removeCallbacksAndMessages(null);
            mECMHandler = null;
        }
        if (mEcmFilter != null) {
            Log.d(TAG, "close: mEcmFilter");
            mEcmFilter.stop();
            mEcmFilter.close();
            mEcmFilter = null;
        }
        if (mDescrambler != null) {
            Log.d(TAG, "close: mDescrambler");
            mDescrambler.close();
            mDescrambler = null;
        }

        if (mMediaCasSession != null) {
            Log.d(TAG, "close: mMediaCasSession");
            mMediaCasSession.close();
            mMediaCasSession = null;
        }

        if (mMediaCas != null) {
            Log.d(TAG, "close: mMediaCas");
            mMediaCas.close();
            mMediaCas = null;
        }
        if (mMediaCasHandler != null) {
            Log.d(TAG, "close: mMediaCasHandler");
            mMediaCasHandler.removeCallbacksAndMessages(null);
            mMediaCasHandler = null;
        }
        if (mMediaCasHandlerThread != null) {
            Log.d(TAG, "close: mMediaCasHandlerThread");
            mMediaCasHandlerThread.quit();
            mMediaCasHandlerThread = null;
        }
    }

    public synchronized void close() {
        Log.d(TAG, "close: mEcmPid = "+mEcmPid+" mContentId "+ mContentId);
        int count = 0;
        while(mIsReOpening){
            try {
                if(count == 30)
                    break;
                Thread.sleep(50);
                count++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        mIsClosing = true;

        if (mECMHandlerThread != null) {
            Log.d(TAG, "close: mECMHandlerThread");
            mECMHandlerThread.quitSafely();
            mECMHandlerThread = null;

            // remove all pending callback and msg
            mECMHandler.removeCallbacksAndMessages(null);
            mECMHandler = null;
        }
        if (mEcmFilter != null) {
            Log.d(TAG, "close: mEcmFilter filterId = "+mEcmFilter.getId());
            mEcmFilter.stop();
            mEcmFilter.close();
            mEcmFilter = null;
        }
        if (mDescrambler != null) {
            Log.d(TAG, "close: mDescrambler");
            mDescrambler.close();
            mDescrambler = null;
        }

        if (mMediaCasSession != null) {
            Log.d(TAG, "close: mMediaCasSession");
            mMediaCasSession.close();
            mMediaCasSession = null;
        }

        if (mMediaCas != null) {
            Log.d(TAG, "close: mMediaCas");
            mMediaCas.close();
            mMediaCas = null;
        }
        if (mMediaCasHandler != null) {
            Log.d(TAG, "close: mMediaCasHandler");
            mMediaCasHandler.removeCallbacksAndMessages(null);
            mMediaCasHandler = null;
        }
        if (mMediaCasHandlerThread != null) {
            Log.d(TAG, "close: mMediaCasHandlerThread");
            mMediaCasHandlerThread.quit();
            mMediaCasHandlerThread = null;
        }
    }

//    public int getEcmCount() {
//        return mEcmCount;
//    }

    private void provision() throws MediaCasException, InterruptedException {
        synchronized (mLockProvisioning) {
//            try {
                if (!mIsProvisioned) {
                    mMediaCas.provision("");
                    mLockProvisioning.wait();
                }
//            } catch (MediaCasException | InterruptedException e) {
//                Log.d(TAG, "provision: exception = " + e);
//                if (mCallback != null) {
//                    mCallback.onCasError(e.toString());
//                }
//                e.printStackTrace();
//            }
        }
    }

    private void openSession() throws MediaCasException {
//        try {
            LogUtils.d("openSession:");
            mMediaCasSession = mMediaCas.openSession(MediaCas.SESSION_USAGE_LIVE,
                    MediaCas.SCRAMBLING_MODE_RESERVED);
//        } catch (Exception ex) {
//            Log.d(TAG, "openSession: exception = " + ex);
//            if (mCallback != null) {
//                mCallback.onCasError(ex.toString());
//            }
//            ex.printStackTrace();
//        }
    }

    public void setPrivateData(byte[] privateData) {
        mPrivateData = privateData;

        setContentId(privateData);
    }

    private void startEcmFilter(int table_id) {
        LogUtils.d("ecmpid = "+mEcmPid+" table_id "+ table_id);
        synchronized (mLockCasReady) {
            try {
                if (!mIsLicenseReady) {

                    mLockCasReady.wait(2000);
                }

            } catch (InterruptedException e) {
                Log.d(TAG, "startEcmFilter: exception = " + e);
                if (mCallback != null) {
                    mCallback.onCasError(e.toString());
                }
                e.printStackTrace();
            }
        }

        if (!mIsLicenseReady) {
            Log.e(TAG, "startEcmFilter: license not ready, something wrong");
            return;
        }

        if (mIsClosing) {
            Log.w(TAG, "startEcmFilter: is closing, return");
            return;
        }

        int bufferSize = 32 * 1024; // from rtk demo
        mEcmFilter = mTuner.openFilter(Filter.TYPE_TS, Filter.SUBTYPE_SECTION, bufferSize,
                getExecutor(), getEcmFilterCallback());

        if (mEcmFilter != null) {
            byte[] filter , mask, mode;
            if(table_id == 0x80) {
                filter = new byte[]{(byte) 0x81};
                mask = new byte[]{(byte) 0xFF};
            }else if(table_id == 0x81){
                filter = new byte[]{(byte) 0x80};
                mask = new byte[]{(byte) 0xFF};
            }else{
                filter = new byte[]{(byte) 0x80};
                mask = new byte[]{(byte) 0xFE};
            }
            mode = new byte[]{(byte) 0x00};
            SectionSettingsWithSectionBits settings = SectionSettingsWithSectionBits
                    .builder(Filter.TYPE_TS)
                    .setCrcEnabled(false)
                    .setRepeat(true)
                    .setRaw(false)
                    .setFilter(filter)
                    .setMask(mask)
                    .setMode(mode)
                    .build();
            FilterConfiguration config = TsFilterConfiguration
                    .builder()
                    .setTpid(mEcmPid)
                    .setSettings(settings)
                    .build();
            mEcmFilter.configure(config);
            mEcmFilter.start();
            LogUtils.d("ECM openFilter filterid = "+mEcmFilter.getId()+" mEcmPid = "+mEcmPid+" mPidList = "+mPidList);
            for (int pid : mPidList) {
                mDescrambler.addPid(Descrambler.PID_TYPE_T, pid, mEcmFilter);
            }
        }
    }

    private void stopEcmFilter(){
        if(mEcmFilter != null){
            LogUtils.d("IN");
            mEcmFilter.stop();
            mEcmFilter.close();
            mEcmFilter = null;
        }
    }

    private void switchEcmFilter(int table_id){
        LogUtils.d("ecmpid = "+mEcmPid+" table_id "+ table_id);
        synchronized (mLockCasReady) {
            try {
                if (!mIsLicenseReady) {
                    LogUtils.e("License not ready, waiting");
                    mLockCasReady.wait();
                }

            } catch (InterruptedException e) {
                Log.d(TAG, "startEcmFilter: exception = " + e);
                if (mCallback != null) {
                    mCallback.onCasError(e.toString());
                }
                e.printStackTrace();
            }
        }

        if (!mIsLicenseReady) {
            Log.e(TAG, "startEcmFilter: license not ready, something wrong");
            return;
        }

        if (mIsClosing) {
            Log.w(TAG, "startEcmFilter: is closing, return");
        }
        if(mEcmFilter != null) {
            mEcmFilter.stop();
            byte[] filter , mask, mode;
            if(table_id == 0x80) {
                filter = new byte[]{(byte) 0x81};
                mask = new byte[]{(byte) 0xFF};
            }else if(table_id == 0x81){
                filter = new byte[]{(byte) 0x80};
                mask = new byte[]{(byte) 0xFF};
            }else{
                filter = new byte[]{(byte) 0x80};
                mask = new byte[]{(byte) 0xFE};
            }
            mode = new byte[]{(byte) 0x00};
            SectionSettingsWithSectionBits settings = SectionSettingsWithSectionBits
                    .builder(Filter.TYPE_TS)
                    .setCrcEnabled(false)
                    .setRepeat(true)
                    .setRaw(false)
                    .setFilter(filter)
                    .setMask(mask)
                    .setMode(mode)
                    .build();
            FilterConfiguration config = TsFilterConfiguration
                    .builder()
                    .setTpid(mEcmPid)
                    .setSettings(settings)
                    .build();
            mEcmFilter.configure(config);
            mEcmFilter.start();
            LogUtils.d("mEcmFilter.start");

        }
    }
    public void addPid(int pid) {
        Log.d(TAG, "addPid: pid = " + pid);
        mPidList.add(pid);
    }

    public void removePid(int pid) {
        Log.d(TAG, "removePid: pid = " + pid);
        mPidList.remove((Integer) pid);
    }

    private void setContentId(byte[] privateData) {

        mContentId = CasRefreshHelper.parse_content_id(privateData);
        Log.d(TAG, "setContentId: content id = " + mContentId);
    }

    @Override
    public void onEvent(@NonNull MediaCas mediaCas, int event, int arg, @Nullable byte[] data) {
        Log.d(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" Widevine onEvent() Event: " + event + " arg = " + arg);
        String strData = new String(data);
        //Log.d(TAG, "Widevine onEvent() data, strData= " + strData);
        if(mIsClosing){
            LogUtils.e("The CasSessing is closed, do nothing !!!!!!!!!!!!!!");
            close();
            return;
        }
        switch (event) {
            case INDIVIDUALIZATION_REQUEST: {
                Log.d(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" Individualization request event from plugin");

                String url = DEFAULT_PROVISIONING_URL + DEFAULT_PROVISION_KEY + strData;
                Log.d(TAG, "Widevine provision url = " + url);

                //add http request...
                String string = HttpURLConnectionUtil.doPost(url, null);
                Log.d(TAG, "Widevine provision Response = " + string);

                byte[] provisionResponse = string.getBytes();
                try {
                    mediaCas.sendEvent(INDIVIDUALIZATION_RESPONSE, 0, provisionResponse);
                } catch (Exception e) {
                    Log.d(TAG, "exception = " + e);
                    e.printStackTrace();
                    synchronized (mLockProvisioning) {
                        mLockProvisioning.notifyAll();
                    }
                }
                break;
            }

            case INDIVIDUALIZATION_COMPLETE: {
                Log.d(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" Individualization complete");
                synchronized (mLockProvisioning) {
                    mIsProvisioned = true;
                    mLockProvisioning.notifyAll();
                }

                break;
            }

            case CAS_SESSION_ID: {
                Log.d(TAG, "open session complete");
                Log.d(TAG, "Plugin session ID event. ID: " + Arrays.toString(data));
                break;
            }

            case LICENSE_REQUEST: {
                Log.d(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" receive license request ... data length = " + (data != null ? data.length : 0));

                try {
                    byte[] licenseResponse;
                    licenseResponse = HttpURLConnectionUtil.doPostByte(getWVLicenseURL(), data, getHttpConnectionCallback(), false);
                    Log.d(TAG, "Widevine Response length = " + licenseResponse.length);
                    mediaCas.sendEvent(LICENSE_RESPONSE, 0, licenseResponse);
                } catch (Exception e) {
                    Log.d(TAG, "mEcmPid "+mEcmPid+" e = " + e);
                    synchronized (mLockCasReady) {
                        mLockCasReady.notifyAll();
                    }

                    if (mCallback != null) {
                        mCallback.onCasError(e.toString());
                    }

                    Log.e(TAG, "onEvent: LICENSE_REQUEST ", e);
                }

                break;
            }

            case LICENSE_RENEWAL_REQUEST: {
                Log.d(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" receive license renewal request ... data length = " + (data != null ? data.length : 0));
                try {
                    byte[] licenseResponse;
                    licenseResponse = HttpURLConnectionUtil.doPostByte(getWVLicenseURL(), data, getHttpConnectionCallback(), true);
                    Log.d(TAG, "Widevine Response length =" + licenseResponse.length);
                    mediaCas.sendEvent(LICENSE_RENEWAL_RESPONSE, 0, licenseResponse);
                } catch (Exception e) {
                    Log.d(TAG, "mEcmPid "+mEcmPid+" e = " + e);
                    synchronized (mLockCasReady) {
                        mLockCasReady.notifyAll();
                    }

                    if (mCallback != null) {
                        mCallback.onCasError(e.toString());
                    }

                    Log.e(TAG, "onEvent: LICENSE_RENEWAL_REQUEST ", e);
                }

                break;
            }

            case LICENSE_CAS_READY:
            case LICENSE_CAS_RENEWAL_READY: {
                Log.d(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" license ready complete");
                synchronized (mLockCasReady) {
                    mIsLicenseReady = true;
                    mLockCasReady.notifyAll();
                }

                mCasRefreshHelper.update_license_mapping(mContentId, strData);
                mPreEcm = null;
                if (mIsRefresh) {
                    close();
                }
                break;
            }

            case LICENSE_EXPIRED: {
                Log.e(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" onEvent: license expired");
                mIsLicenseReady = false;
                // redo wvcas if license expire
                handleLicenseExpire();
            }break;

            case CAS_ERROR: {
                Log.e(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" onEvent: cas error ECM Pid = "+mEcmPid);
                mIsLicenseReady = false;
                if (mIsRefresh) {
                    close();
                }

                if (mCallback != null) {
                    mCallback.onCasError(strData);
                }

                break;
            }

            case LICENSE_REMOVED: {
                Log.d(TAG, "mEcmPid = "+mEcmPid+" mContentId "+ mContentId+" onEvent: license removed");
                mCasRefreshHelper.remove_license_mapping(mContentId, strData);
            }
        }
    }

    private void handleLicenseExpire(){
        int count = 0;
        while(mIsOpening) {
            try {
                if(count == 30)
                    break;
                Thread.sleep(50);
                count ++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mIsReOpening = true;
        if (!mIsClosing) {
            internal_close();
            LogUtils.d("mIsRefresh "+mIsRefresh);
            if (mIsRefresh) {
                openForRefresh();
            }
            else {
                open();
            }
        }else{
            close();
        }
        mIsReOpening = false;
    }

    public interface Callback {
        void onHttpError(int responseCode, String contentId);
        void onCasError(String msg);
        void onConnectTimeout(String msg);
        void onRemoveLicense(String licenseId);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

}
