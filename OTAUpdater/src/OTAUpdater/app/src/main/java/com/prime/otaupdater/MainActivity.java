package com.prime.otaupdater;

import android.os.SystemProperties;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.android.SystemAPP.PrimeSystemApp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity" ;
    public static final String PROPERTY_IMAGE_NAME = "persist.sys.prime.ota_image_name";
    public static final String IMAGE_NAME = "DADA_1319D.zip";
    public static final String ACTION_CLOSE_OTA_UI = "com.prime.otaupdater.action.close_ota_ui" ;
    public static final String ACTION_OTA_PROGRESS = "com.prime.otaupdater.action.progress" ;
    public static final String OTA_SERVICE_PACKAGE_NAME = "com.prime.android.tv.otaservice";
    public static final String SERVER_CONTROL_SERVICE_PACKAGE_NAME = "com.prime.server.control.service";

    // property
    public static final String PROPERTY_OTA_UPDATE_NAME = "ro.ota_update_name";
    public static final String PROPERTY_FIRMWARE_VERSION = "ro.firmwareVersion";

    // broadcast to OTA Service
    public static final String ABUPDATE_BROADCAST_START             = "com.prime.android.tv.otaservice.abupdate.start";
    public static final String ABUPDATE_BROADCAST_STOP              = "com.prime.android.tv.otaservice.abupdate.stop";
    public static final String ABUPDATE_BROADCAST_PAUSE             = "com.prime.android.tv.otaservice.abupdate.pause";
    public static final String ABUPDATE_BROADCAST_RESUME            = "com.prime.android.tv.otaservice.abupdate.resume";
    public static final String ABUPDATE_BROADCAST_REGISTER_CALLER   = "com.prime.android.tv.otaservice.abupdate.register.caller";

    // broadcast from OTA Service
    public static final String ABUPDATE_BROADCAST_ERROR             = "com.prime.android.tv.otaservice.abupdate.error";//send error code and error msg
    public static final String ABUPDATE_BROADCAST_STATUS            = "com.prime.android.tv.otaservice.abupdate.status";//send error code and error msg
    public static final String ABUPDATE_BROADCAST_COMPLETE          = "com.prime.android.tv.otaservice.abupdate.complete";//send update complete
    public static final String ABUPDATE_BROADCAST_CALLER            = "com.prime.android.tv.otaservice.abupdate.caller";

    // Notify HTTP OTA update from other service
    public static final String ACTION_HTTP_OTA_UPDATE               = "com.prime.otaupdater.ACTION_HTTP_OTA_UPDATE";
    public static final String EXTRA_HTTP_PAYLOAD_BIN               = "com.prime.otaupdater.EXTRA_HTTP_OTA_PAYLOAD";
    public static final String EXTRA_HTTP_PAYLOAD_PROP              = "com.prime.otaupdater.EXTRA_HTTP_OTA_PROPERTY";
    public static final String EXTRA_HTTP_METADATA                  = "com.prime.otaupdater.EXTRA_HTTP_OTA_METADATA";
    public static final String EXTRA_HTTP_FORCE                     = "com.prime.otaupdater.EXTRA_HTTP_OTA_FORCE";
    public static final String EXTRA_CALLER                         = "com.prime.otaupdater.EXTRA_CALLER";
    public static final String EXTRA_UPDATE_MODE                    = "com.prime.otaupdater.EXTRA_UPDATE_MODE";
    public static final String EXTRA_UPDATE_ZIP_URL                 = "com.prime.otaupdater.EXTRA_UPDATE_ZIP_URL";
    public static final String EXTRA_UPDATE_ZIP_OFFSET              = "com.prime.otaupdater.EXTRA_ZIP_OFFSET";
    public static final String EXTRA_UPDATE_ZIP_SIZE                = "com.prime.otaupdater.EXTRA_ZIP_SIZE";
    public static final String EXTRA_UPDATE_PROPERTIES              = "com.prime.otaupdater.EXTRA_PROPERTIES";

    // param of ABUPDATE_BROADCAST_CALLER
    public static final String ABUPDATE_BROADCAST_UPDATE_CALLER         = "prime.abupdate.caller";

    // param of ABUPDATE_BROADCAST_ERROR
    public static final String ABUPDATE_BROADCAST_ERROR_CODE            = "prime.abupdate.errCode";
    public static final String ABUPDATE_BROADCAST_ERROR_MSG             = "prime.abupdate.errStr";

    // param of ABUPDATE_BROADCAST_STATUS
    public static final String ABUPDATE_BROADCAST_UPDATE_PROGRESS       = "prime.abupdate.progress";
    public static final String ABUPDATE_BROADCAST_STATUS_MESSAGE        = "prime.abupdate.statusMsg";

    // param of ABUPDATE_BROADCAST_START
    public static final String ABUPDATE_BROADCAST_UPDATE_ZIP_URL        = "prime.abupdate.url.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_BIN_URL        = "prime.abupdate.url.bin";
    public static final String ABUPDATE_BROADCAST_UPDATE_PARAM_URL      = "prime.abupdate.url.txt";
    public static final String ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE        = "prime.abupdate.size.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET        = "prime.abupdate.offset.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_PROPERTIES        = "prime.abupdate.properties.zip";
    public static final String ABUPDATE_BROADCAST_UPDATE_IMAGE_VERSION  = "prime.abupdate.image.version";
    public static final String ABUPDATE_BROADCAST_UPDATE_MODE           = "prime.abupdate.MODE";
    public static final int ABUPDATE_STREAM_PAYLOAD_MODE = 0;
    public static final int ABUPDATE_STREAM_ZIP_MODE = 1;
    public static final int ABUPDATE_USB_MODE = 2;

    // param from MountReceiver, Http OTA Receiver
    public static final String EXTRA_OTA_PATH           = "EXTRA_OTA_PATH" ;
    public static final String EXTRA_PAYLOAD_BIN        = "EXTRA_PAYLOAD_BIN" ;
    public static final String EXTRA_PAYLOAD_PROP       = "EXTRA_PAYLOAD_PROP" ;
    public static final String EXTRA_METADATA           = "EXTRA_METADATE" ;
    public static final String EXTRA_IMAGE_VERSION      = "EXTRA_IMAGE_VERSION" ;
    public static final String EXTRA_ZIP_FILE_OTA       = "EXTRA_ZIP_FILE_OTA" ;
    public static final String EXTRA_ZIP_FILE_RECOVERY  = "EXTRA_ZIP_FILE_RECOVERY" ;
    public static final String EXTRA_TIMESTAMP          = "EXTRA_TIMESTAMP" ;
    public static final String EXTRA_FORCE              = "EXTRA_FORCE" ;
    public static final String EXTRA_IS_HTTP_OTA        = "EXTRA_IS_HTTP_OTA" ;

    // param from settings ui
    private static final String EXTRA_CALLER_FROM_SETTINGS = "EXTRA_CALLER_FROM_SETTINGS";
    private boolean mCallerFromSettingsUiFlag;

    // param of mHomeReceiver
    public static final String EXTRA_POPUP_FINISH_DIALOG = "EXTRA_POPUP_FINISH_DIALOG" ;

    // action stop ota updater
    public static final String STOP_OTA_UPDATER = "com.prime.otaupdater.stop_otaupdater";

    // test flag
    public static final boolean BURN_TEST = false;

    // size
    public static final int MB = 1024*1024 ;

    public Handler mHandler = new Handler(Looper.getMainLooper());
    public Handler mCopyHandler;
    public Context mContext;
    public View mMainLayout;
    public ProgressBar mProgressBar;
    public TextView mProgressVal;
    public TextView mUpdateStatus;
    //public TextView mUpdateResult;
    public TextView mVersionCurrent;
    public TextView mVersionNext;
    public TextView mReadmeTxt;
    public View mReadmeAll;
    public SharedPreferences mSharedPreference;
    public SharedPreferences.Editor mEditor;
    public String mCallerPackageName;

    //public static String mPayloadBin;
    //public static String mPayloadProp;
    public SureDialog mDialogStartUpdate = null;
    public SureDialog mDialogFinishActivity = null;
    public SureDialog mDialogInstallPkg = null;

    public UpdateEngine mEngine = new UpdateEngine();
    public static int mEngineStatus = -1;
    public Runnable mWait_UpdateEngine_Idle_to_handleFile;

    // 強制 OTA update
    public boolean mForceUpdate = false;
    private boolean mHttpOtaUpdateAgain = false;

    // 避免 trigger 第 2 次 OTA update
    public boolean mHandleFolderStart = false;

    // ota 參數
    public static final String TARGET_PATH = "/data/ota_package/update.zip";
    public long mPostTimeStamp = -1;
    public long mPayloadOffset = 0;
    public long mPayloadSize = 0;
    public String mNextVersion;
    public List<String> mProperties;
    public String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        mProgressBar = findViewById(R.id.progressBar);
        mProgressVal = findViewById(R.id.progress_value);
        mUpdateStatus = findViewById(R.id.status_value);
        //mUpdateResult = findViewById(R.id.result_value);
        mVersionCurrent = findViewById(R.id.version1_val);
        mVersionNext = findViewById(R.id.version2_val);
        mReadmeTxt = findViewById(R.id.readme);
        mMainLayout = findViewById(R.id.activity_background);
        mReadmeAll = findViewById(R.id.readmeBackground);

        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mSharedPreference.edit();
        mCallerFromSettingsUiFlag = getIntent().getBooleanExtra(EXTRA_CALLER_FROM_SETTINGS, false);
        mCallerPackageName = getIntent().getStringExtra(EXTRA_CALLER);

        registerOtaReceiver();
        registerHomeReceiver();
        initReadme();
        bindUpdateEngineCallback();
        handleFolder(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean popupFinishDialog = intent.getBooleanExtra(EXTRA_POPUP_FINISH_DIALOG, false);
        Log.d(TAG, "onNewIntent: popup Finish Dialog = " + popupFinishDialog);

        if (isShowingDialog()) {
            Log.w(TAG, "onNewIntent: already has a dialog on screen");
            return;
        }

        if (checkRestartOTA(intent)) {
            Log.w(TAG, "onNewIntent: check restart ota");
            mForceUpdate = false;
            mHandleFolderStart = false;
            mProgressBar.setProgress(0);
            mProgressVal.setText("0 %");
            mUpdateStatus.setText("wait for new update");
        }

        if (mForceUpdate) {
            Log.w(TAG, "onNewIntent: forbid home key cause window disappear");
            return;
        }
        
        if (popupFinishDialog)
            dialogFinishActivity();
        else
            handleFolder(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mEngine.unbind();
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mOtaReceiver);
        unregisterReceiver(mHomeReceiver);
        broadcastStopOTA(getApplicationContext());
        sendOTAErrorToCaller();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (View.VISIBLE != mMainLayout.getVisibility())
            return true;

        if (mForceUpdate) {
            Log.w(TAG, "onKeyDown: forbid key cause window disappear");
            return true;
        }

        if (KeyEvent.KEYCODE_BACK == keyCode) {
            //pressBackToFinish();
            dialogFinishActivity();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /*public void pressBackToFinish() {
        mTimeLength = System.currentTimeMillis() - mTimeLength;
        Log.d(TAG, "pressBackToFinish: mTimeLength = " + mTimeLength);

        if (mTimeLength < 3000)
            finish();
        else
            Toast.makeText(this, "Press again to finish OTA UI", Toast.LENGTH_SHORT).show();
        mTimeLength = System.currentTimeMillis();
    }*/

    public void initReadme() {
        String otaPath = getIntent().getStringExtra(EXTRA_OTA_PATH);
        if (otaPath != null) {
            return;
        }

        if (mCallerFromSettingsUiFlag)
            return;

        mReadmeAll.setVisibility(View.VISIBLE);
        StringBuilder readme = new StringBuilder();
        readme.append("請在 USB 隨身碟建立 Pesi_Ota 或 Pesi_Force_Ota，並選擇其中一種方法\n")
                .append("　　- Pesi_Ota 目錄會以 Dialog 詢問是否啟動 OTA 更新\n")
                .append("　　- Pesi_Force_Ota 目錄會直接啟動 OTA 更新\n\n")
                .append("　　方法 1 : 透過 OTA Service 更新系統，必須在目錄置入：\n")
                .append("　　　　- " +getImageName() +"\n")
                //.append("　　　　- payload.bin\n")
                //.append("　　　　- payload_properties.txt\n")
                //.append("　　　　- metadata\n\n")
                .append("　　方法 2 : 透過 Recovery System 更新系統，必須在目錄置入：\n")
                //.append("　　　　- 更新用的 zip 檔 ( 例如 RealtekHank-ota-202301082151.zip )\n\n")
                .append("　　　　- 更新用的 zip 檔，檔名必須為 " + SystemProperties.get(PROPERTY_IMAGE_NAME, "recovery_package.zip") + "\n\n")
                .append("準備好後，請依照下列步驟進行更新\n")
                .append("　　1. 啟動 Ota Updater 開啟背景 Service，再關閉 Ota Updater ( 只需要啟動一次 )\n")
                .append("　　2. 插入 USB 隨身碟後，等待 OTA 更新開始，或根據 Dialog 指示進行 OTA 更新\n")
                .append("　　3. 更新完後請移除 USB 隨身碟並重新開機使系統完成更新\n");
        mReadmeTxt.setText(readme);
    }

    public boolean isShowingDialog() {
        boolean isShow = false;

        if (mDialogStartUpdate != null && mDialogStartUpdate.isShowing())
            isShow = true;
        if (mDialogFinishActivity != null && mDialogFinishActivity.isShowing())
            isShow = true;
        if (mDialogInstallPkg != null && mDialogInstallPkg.isShowing())
            isShow = true;

        return isShow;
    }

    public void dialogFinishActivity() {
        Log.d(TAG, "dialogFinishActivity: ");
        mMainLayout.setVisibility(View.INVISIBLE);
        mDialogFinishActivity = new SureDialog(this, "Do you want to finish OTA Updater ?") {
            public void onClickNo () {
                mMainLayout.setVisibility(View.VISIBLE);
            }
            public void onClickYes () {
                finish();
            }
        };
    }

    public void registerOtaReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CLOSE_OTA_UI);
        intentFilter.addAction(ACTION_OTA_PROGRESS);
        intentFilter.addAction(ABUPDATE_BROADCAST_ERROR);
        intentFilter.addAction(ABUPDATE_BROADCAST_STATUS);
        intentFilter.addAction(ABUPDATE_BROADCAST_COMPLETE);
        intentFilter.addAction(ABUPDATE_BROADCAST_CALLER);
        intentFilter.addAction(STOP_OTA_UPDATER);
        registerReceiver(mOtaReceiver, intentFilter, Context.RECEIVER_EXPORTED);
    }

    public void registerHomeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeReceiver, intentFilter, Context.RECEIVER_EXPORTED);
    }

    public void bindUpdateEngineCallback() {
        mEngine.bind(new UpdateEngineCallback() {
            @Override
            public void onStatusUpdate(int status, float percent) {
                Log.d(TAG, "onStatusUpdate: status = " + status + ", percent = " + percent);
                mEngineStatus = status;
            }
            @Override
            public void onPayloadApplicationComplete(int errorCode) {
                Log.d(TAG, "onPayloadApplicationComplete: errorCode = " + errorCode);
            }
        });
    }

    public void handleFolder(Intent intent) {
        boolean isHttpOta = intent.getBooleanExtra(EXTRA_IS_HTTP_OTA, false);
        String folderPath;
        if (mCallerFromSettingsUiFlag)
            folderPath = getUSBFilePath(intent);
        else {
            folderPath = intent.getStringExtra(EXTRA_OTA_PATH);
            mFilePath = folderPath + "/" +getImageName();
        }
        Log.d(TAG, "handleFolder: OTA folder path = " + folderPath);

        if (isHttpOta)
            Log.w(TAG, "handleFolder: do not check OTA folder");
        else if (isNullPath(mFilePath) || isNullPath(folderPath) ) {
            return;
        } else {
            File file = new File(mFilePath);
            if (!file.exists()) {
                mUpdateStatus.setText("Error: not found \""+getImageName()+"\"");
                Log.e(TAG, "handleFolder: not found \""+getImageName()+"\"");
                return;
            }
        }

        if (mHttpOtaUpdateAgain) {
            Log.w(TAG, "handleFolder: Http Ota Update Again");
            mHttpOtaUpdateAgain = false;
        }
        else if (mHandleFolderStart) {
            Log.w(TAG, "handleFolder: do not OTA again");
            return;
        }
        else {
            Log.w(TAG, "handleFolder: ok to start OTA");
            mHandleFolderStart = true;
        }

        mReadmeAll.setVisibility(View.INVISIBLE);

        // wait Update Engine IDLE to handleFile()
        mWait_UpdateEngine_Idle_to_handleFile = () -> {
            boolean needConfirm = true;

            if (!isHttpOta)
                needConfirm = !folderPath.toLowerCase().contains("force");

            Log.d(TAG, "handleFolder: isHttpOta     = " + isHttpOta);
            Log.d(TAG, "handleFolder: needConfirm   = " + needConfirm);
            Log.d(TAG, "handleFolder: mEngineStatus = " + mEngineStatus);

            if (UpdateEngine.UpdateStatusConstants.IDLE == mEngineStatus) {
                mHandler.removeCallbacksAndMessages(null);
                handleFile(intent, needConfirm);
            }
            else
                mHandler.postDelayed(mWait_UpdateEngine_Idle_to_handleFile, 3000);
        };
        mMainLayout.setVisibility(View.INVISIBLE);
        mHandler.postDelayed(mWait_UpdateEngine_Idle_to_handleFile, 3000);
    }

    public void handleFile(Intent intent, boolean needConfirm) {
        if (hasZipUrl(intent))
            dialogInstallByZipStreaming(intent, needConfirm);
        else if (hasOtaZip(intent))
            dialogInstallOta(intent, needConfirm);
        else if (hasPayload(intent))
            dialogInstallPayload(intent, needConfirm);
        else if (hasRecoveryZip(intent))
            dialogInstallRecovery(intent, needConfirm);
    }

    public void dialogInstallPayload(Intent intent, boolean needConfirm) {
        mForceUpdate = intent.getBooleanExtra(EXTRA_FORCE, false);

        Log.d(TAG, "dialogInstallPayload: need Confirm = " + needConfirm);
        Log.d(TAG, "dialogInstallPayload: Force Update = " + mForceUpdate);

        if (needConfirm) {
            /*boolean isHttpOta = intent.getBooleanExtra(EXTRA_IS_HTTP_OTA, false);
            final String[] version = {""};
            new Thread(() -> {
                if (isHttpOta) {
                    String metadata = intent.getStringExtra(EXTRA_METADATA);
                    HashMap<String, String> metaMap = readMetadata(metadata);
                    version[0] = "New version: " + metaMap.get("version") + "\n\n";
                }
                runOnUiThread(() -> {
                });
            }).start();*/
            mMainLayout.setVisibility(View.INVISIBLE);
            if (mForceUpdate) {
                mDialogStartUpdate = new SureDialog(MainActivity.this,
                        "Force upgrade your system after 10 seconds.",
                        10000,
                        false)
                {
                    @Override
                    public void dismissDialog() {
                        super.dismissDialog();
                        installPayload(intent);
                    }
                };
                //mDialogStartUpdate.content.setGravity(Gravity.TOP|Gravity.START);
            }
            else {
                mDialogStartUpdate = new SureDialog(this, "Start upgrade with payload files?")
                {
                    public void onClickNo() {
                        finish();
                    }
                    public void onClickYes() {
                        installPayload(intent);
                    }
                };
            }
        }
        else {
            installPayload(intent);
        }
    }

    public void dialogInstallByZipStreaming(Intent intent, boolean needConfirm) {
        mForceUpdate = intent.getBooleanExtra(EXTRA_FORCE, false);

        Log.d(TAG, "dialogInstallZipStreaming: need Confirm = " + needConfirm);
        Log.d(TAG, "dialogInstallZipStreaming: Force Update = " + mForceUpdate);

        if (needConfirm) {
            /*boolean isHttpOta = intent.getBooleanExtra(EXTRA_IS_HTTP_OTA, false);
            final String[] version = {""};
            new Thread(() -> {
                if (isHttpOta) {
                    String metadata = intent.getStringExtra(EXTRA_METADATA);
                    HashMap<String, String> metaMap = readMetadata(metadata);
                    version[0] = "New version: " + metaMap.get("version") + "\n\n";
                }
                runOnUiThread(() -> {
                });
            }).start();*/
            mMainLayout.setVisibility(View.INVISIBLE);
            if (mForceUpdate) {
                mDialogStartUpdate = new SureDialog(MainActivity.this,
                        "Force upgrade your system after 10 seconds.",
                        10000,
                        false)
                {
                    @Override
                    public void dismissDialog() {
                        super.dismissDialog();
                        installByZipStream(intent);
                    }
                };
                //mDialogStartUpdate.content.setGravity(Gravity.TOP|Gravity.START);
            }
            else {
                mDialogStartUpdate = new SureDialog(this, "Start upgrade with zip files?")
                {
                    public void onClickNo() {
                        finish();
                    }
                    public void onClickYes() {
                        installPayload(intent);
                    }
                };
            }
        }
        else {
            installByZipStream(intent);
        }
    }

    public void dialogInstallOta(Intent intent, boolean needConfirm) {
        mForceUpdate = intent.getBooleanExtra(EXTRA_FORCE, false);

        Log.d(TAG, "dialogInstallOta: need Confirm = " + needConfirm);
        Log.d(TAG, "dialogInstallOta: Force Update = " + mForceUpdate);

        mDialogStartUpdate = new SureDialog(this, "Start upgrade with "+getImageName()+" files?")
        {
            public void onClickNo() {
                finish();
            }
            public void onClickYes() {
                if(!isNullPath(mFilePath))
                    PrimeSystemApp.trigger_ota_to_recovery(MainActivity.this,"/mnt/usb/"+SystemProperties.get(PROPERTY_IMAGE_NAME, "recovery_package.zip"));
//                new SureDialog(MainActivity.this) {
//                    @Override
//                    public void onShowEvent() {
//                        super.onShowEvent();
//                        if (isNullPath(mFilePath)) {
//                            dismissDialog();
//                            return;
//                        }
//                        HandlerThread handlerThread = new HandlerThread("CopyHandlerThread");
//                        handlerThread.start();
//                        mCopyHandler = new Handler(handlerThread.getLooper());
//                        mCopyHandler.post(()->{
//                            copyFile(mFilePath, TARGET_PATH);
//                            dismissDialog();
//                            mHandler.post(()->{
//                                installOta(intent);
//                            });
//                        });
//                    }
 //               };
            };
        };
    }

    public void dialogInstallRecovery(Intent intent, boolean needConfirm) {
        String otaZipPath = "";
        File udiskZip = null;
        File otaZip = null;
        String msg = "";

        otaZipPath = intent.getStringExtra(EXTRA_ZIP_FILE_RECOVERY);
        udiskZip   = getUdiskZip(otaZipPath, needConfirm);

        Log.d(TAG, "dialogInstallPkg: OTA zip path  = " + otaZipPath);
        Log.d(TAG, "dialogInstallPkg: OTA zip udisk = " + udiskZip.getPath());
        Log.d(TAG, "dialogInstallPkg: need confirm  = " + needConfirm);

        if (BURN_TEST)
            msg = "Reboot Count: " + BootUpReceiver.reboot_counter + "\n\n";

        mMainLayout.setVisibility(View.INVISIBLE);

        if (otaZipPath == null) {
            Toast.makeText(this, "File not found: " + udiskZip.getName(), Toast.LENGTH_LONG).show();
            runOnUiThread(this::finish);
            return;
        }

        otaZip = new File(otaZipPath);
        if (sameTimestamp(otaZip)) {
            Toast.makeText(this, "Timestamp is same, no need to update system", Toast.LENGTH_LONG).show();
            runOnUiThread(this::finish);
            return;
        }

        File final_udiskZip = udiskZip;
        File final_otaZip = otaZip;

        if (needConfirm) {
            mDialogInstallPkg = new SureDialog(MainActivity.this,
                    msg + "Select YES to upgrade your system ( Reboot Required ), please do not power off the device.")
            {
                @Override
                public void onClickNo() {
                    finish();
                }
                @Override
                public void onClickYes() {
                    installPackage(final_udiskZip, final_otaZip);
                }
            };
        }
        else {
            mDialogInstallPkg = new SureDialog(MainActivity.this,
                    msg + "Reboot to upgrade your system after 10 seconds, please do not power off the device.",
                    10000,
                    true)
            {
                @Override
                public void dismissDialog() {
                    super.dismissDialog();
                    installPackage(final_udiskZip, final_otaZip);
                }
            };
        }
    }

    public void installByZipStream(Intent intent) {
        mMainLayout.setVisibility(View.VISIBLE);
        String url_zip_file = intent.getStringExtra(EXTRA_UPDATE_ZIP_URL);

        if (url_zip_file == null) {
            Log.e(TAG, "installByZipStream: [Error] file not found");
            sendOTAErrorToCaller();
            return;
        }

        setupVersionCurrent();
        setupVersionFirmware();

        broadcastPackageName();
        broadcastStartOTA(intent);
    }

    public void installPayload(Intent intent) {
        mMainLayout.setVisibility(View.VISIBLE);
        String payloadBin     = intent.getStringExtra(EXTRA_PAYLOAD_BIN);
        String payloadProp    = intent.getStringExtra(EXTRA_PAYLOAD_PROP);
        String metadata       = intent.getStringExtra(EXTRA_METADATA);
        String image_vesrion  = intent.getStringExtra(EXTRA_IMAGE_VERSION);
        Boolean isHttpOta     = intent.getBooleanExtra(EXTRA_IS_HTTP_OTA, false);

        if (isNullPath(payloadBin, payloadProp, metadata)) {
            sendOTAErrorToCaller();
            return;
        }

        if (isHttpOta) {
            Log.d(TAG, "installPayload: from HTTP");
            installPayloadHTTP(payloadBin, payloadProp, metadata, image_vesrion);
            return;
        }

        // show version
        setupVersion(metadata);

        // check timestamp
        if (!timestampOK(metadata) && !BURN_TEST) {
            mUpdateStatus.setText("Error: Timestamp expired");
            return;
        }

        if (!freeSpaceEnough(payloadBin, payloadProp, "/data")) {
            mUpdateStatus.setText("Error: Space not enough");
            return;
        }

        // register & start OTA update
        broadcastPackageName();
        broadcastStartOTA(payloadBin, payloadProp, isHttpOta, image_vesrion);
    }

    public void installOta(Intent intent) {
        mMainLayout.setVisibility(View.VISIBLE);
        String otaZip         = intent.getStringExtra(EXTRA_ZIP_FILE_OTA);
        Boolean isHttpOta     = intent.getBooleanExtra(EXTRA_IS_HTTP_OTA, false);

        if (isNullPath(TARGET_PATH)) {
            return;
        }

        setDataFromZip(TARGET_PATH);

        // show version
        setupVersion();

        // check timestamp
//        if (!timestampOK() && !BURN_TEST) {
//            mUpdateStatus.setText("Error: Timestamp expired");
//            return;
//        }

        if (!freeSpaceEnough(otaZip, "/data")) {
            mUpdateStatus.setText("Error: Space not enough");
            return;
        }

        // register & start OTA update
        broadcastPackageName();
        broadcastStartOTA(isHttpOta);
    }

    public void installPayloadHTTP(String payloadBin, String payloadProp, String metadata, String image_vesrion) {
        new Thread(() -> {
            // metadata for setup version
            HashMap<String, String> metaMap = readMetadata(TAG, metadata);
            runOnUiThread(() -> {
                // show version
                setupVersion(metaMap);

                if (metaMap.isEmpty()) {
                    mUpdateStatus.setText("Error: Unable to read data from the server");
                    mForceUpdate = false;
                    sendOTAErrorToCaller();
                    return;
                }

                if (mForceUpdate)
                    Log.d(TAG, "installPayload: skip timestamp check");
                else if (!timestampOK(TAG, metaMap) && !BURN_TEST) {
                    mUpdateStatus.setText("Error: Timestamp expired");
                    mForceUpdate = false;
                    sendOTAErrorToCaller();
                    return;
                }

                if (!freeSpaceEnough(payloadBin, payloadProp, "/data")) {
                    mUpdateStatus.setText("Error: Space not enough");
                    mForceUpdate = false;
                    sendOTAErrorToCaller();
                    return;
                }

                // register & start OTA update
                broadcastPackageName();
                broadcastStartOTA(payloadBin, payloadProp, true, image_vesrion);
            });
        }).start();
    }

    public void installPackage(File udiskZip, File otaZip) {
        if (otaZip.exists()) {
            try {
                RecoverySystem.installPackage(MainActivity.this, udiskZip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "File not found: " + udiskZip.getName(), Toast.LENGTH_LONG).show();
        }
        finish();
    }

    public boolean hasPayload(Intent intent) {
        String payloadBin = intent.getStringExtra(EXTRA_PAYLOAD_BIN);
        String payloadProp = intent.getStringExtra(EXTRA_PAYLOAD_PROP);
        String metadata = intent.getStringExtra(EXTRA_METADATA);
        return ((payloadBin != null) || (payloadProp != null) || (metadata != null));
    }

    public boolean hasOtaZip(Intent intent) {
        String otaZipName = intent.getStringExtra(EXTRA_ZIP_FILE_OTA);
        return otaZipName != null;
    }

    public boolean hasRecoveryZip(Intent intent) {
        String otaZipName = intent.getStringExtra(EXTRA_ZIP_FILE_RECOVERY);
        return otaZipName != null;
    }

    public boolean hasZipUrl(Intent intent) {
        String url_zip_file =  intent.getStringExtra(EXTRA_UPDATE_ZIP_URL);
        return url_zip_file != null;
    }

    public boolean isNullPath(String bin, String prop, String metadata) {
        boolean isNull = false;
        if (bin == null) {
            mUpdateStatus.setText("Error: not found \"payload.bin\"");
            Log.e(TAG, "isNullPath: not found \"payload.bin\"");
            mForceUpdate = false;
            isNull = true;
        }
        if (prop == null) {
            mUpdateStatus.setText("Error: not found \"payload_properties.txt\"");
            Log.e(TAG, "isNullPath: not found \"payload_properties.txt\"");
            mForceUpdate = false;
            isNull = true;
        }
        if (metadata == null) {
            mUpdateStatus.setText("Error: not found \"metadata\"");
            Log.e(TAG, "isNullPath: not found \"metadata\"");
            mForceUpdate = false;
            isNull = true;
        }
        return isNull;
    }

    public boolean isNullPath(String otaZip) {
        boolean isNull = false;
        if (otaZip == null) {
            mUpdateStatus.setText("Error: not found \""+getImageName()+"\"");
            Log.e(TAG, "isNullPath: not found \""+getImageName()+"\"");
            mForceUpdate = false;
            isNull = true;
        }
        return isNull;
    }

    public File getUdiskZip(String path, boolean needConfirm) {
        String otaZipPath = "/udisk/";
        File otaZip = new File(path);

        if (needConfirm)
            otaZipPath += "Pesi_Ota/" + otaZip.getName();
        else
            otaZipPath += "Pesi_Force_Ota/" + otaZip.getName();

        return new File(otaZipPath);
    }

    public static HashMap<String, String> readMetadata(String TAG, String metaPath) {
        HashMap<String, String> map = new HashMap<>();
        try {
            URL url = new URL(metaPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("post-build-incremental=")) {
                        line = line.replace("post-build-incremental=", "");
                        map.put("version", line);
                        Log.d(TAG, "readMetadata: current version   = " + line);
                    }
                    else
                    if (line.startsWith("post-timestamp=")) {
                        line = line.replace("post-timestamp=", "");
                        map.put("timestamp", line);
                        Log.d(TAG, "readMetadata: current timestamp = " + line);
                    }
                }
                reader.close();
            }
        }
        catch (Exception e) {
            Log.e(TAG, "readMetadata: " + e);
            map = new HashMap<>();
        }

        return map;
    }

    public void setupVersion(String metadata) {
        // current version
        setupVersionCurrent();

        // next version
        String next_version = versionOf(metadata);
        mVersionNext.setText(next_version);

        // firmware version
        setupVersionFirmware();
    }

    public void setupVersion() {
        // current version
        setupVersionCurrent();

        // next version
        //String next_version = versionOf(metadata);
        mVersionNext.setText(mNextVersion);

        // firmware version
        setupVersionFirmware();
    }

    public void setupVersion(HashMap<String, String> metaMap) {
        // current version
        setupVersionCurrent();

        // next version
        String next_version = metaMap.get("version");
        mVersionNext.setText(next_version);

        // firmware version
        setupVersionFirmware();
    }

    public void setupVersionCurrent() {
        String current_version = SystemProperties.get("ro.build.version.incremental", "-1");
        if (BURN_TEST)
            current_version = current_version + " , reboot counter: " + BootUpReceiver.reboot_counter;
        mVersionCurrent.setText(current_version);
    }

    public void setupVersionFirmware() {
        TextView firmwareVersion = findViewById(R.id.firmware_version_1);
        firmwareVersion.setText("v" + SystemProperties.get(MainActivity.PROPERTY_FIRMWARE_VERSION, "0.0.0"));
    }

    /*public String getLogIndex() {
        int tmpIndex = 0;
        String logIndex = "";

        // length to 4
        tmpIndex = mSharedPreference.getInt("key_log_index", 0);
        logIndex = String.valueOf(tmpIndex);
        while (logIndex.length() < 4) {
            logIndex = "0" + logIndex;
        }

        return logIndex;
    }*/

    /*public void increaseLogIndex() {
        int tmpIndex = 0;

        tmpIndex = mSharedPreference.getInt("key_log_index", 0);

        // hold index
        mEditor.putInt("key_log_index", ++tmpIndex);
        if (mEditor.commit()) {
            try {
                Process process = Runtime.getRuntime().exec("/system/bin/sync");
                process.waitFor();
            }
            catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }*/

    public boolean sameTimestamp(File otaZip) {
        boolean isSameTime = true;
        long post_timestamp = -1;
        long system_timestamp = -1;
        List<String> properties = new ArrayList<>();

        try (ZipFile zip = new ZipFile(otaZip)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            long offset = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                // Zip local file header has 30 bytes + filename + sizeof extra field.
                // https://en.wikipedia.org/wiki/Zip_(file_format)
                long extraSize = entry.getExtra() == null ? 0 : entry.getExtra().length;
                offset += 30 + name.length() + extraSize;

                if (entry.isDirectory()) {
                    continue;
                }

                //Log.d(TAG, "sameTimestamp: " + name);

                long length = entry.getCompressedSize();

                if ("META-INF/com/android/metadata".equals(name)) {
                    InputStream inputStream = zip.getInputStream(entry);
                    if (inputStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = br.readLine()) != null)
                            properties.add(line);
                    }
                }
                offset += length;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "sameTimestamp: " + e);
        }

        system_timestamp = Long.parseLong(SystemProperties.get("ro.build.date.utc", "-1"));
        for (String property : properties) {
            if (property.startsWith("post-timestamp=")) {
                post_timestamp = Long.parseLong(property.replace("post-timestamp=", ""));
                break;
            }
        }

        Log.d(TAG, "sameTimestamp: system_timestamp = " + system_timestamp);
        Log.d(TAG, "sameTimestamp: post_timestamp   = " + post_timestamp);

        isSameTime = (system_timestamp == post_timestamp);

        return false;//isSameTime;
    }

    public boolean timestampOK(String metaPath) {
        long sysTime = Long.parseLong(SystemProperties.get("ro.build.date.utc", "-1"));
        long metaTime = timestampOf(metaPath);
        Log.d(TAG, "timestampOK: metaTime = "+ metaTime);
        Log.d(TAG, "timestampOK: sysTime  = "+ sysTime);
        Log.d(TAG, "timestampOK: metaTime > sysTime , result: " + (metaTime > sysTime));
        return true;//metaTime > sysTime;
    }

    public boolean timestampOK() {
        long sysTime = Long.parseLong(SystemProperties.get("ro.build.date.utc", "-1"));
        long metaTime = mPostTimeStamp;
        Log.d(TAG, "timestampOK: metaTime = "+ metaTime);
        Log.d(TAG, "timestampOK: sysTime  = "+ sysTime);
        Log.d(TAG, "timestampOK: metaTime > sysTime , result: " + (metaTime > sysTime));
        return true;//metaTime > sysTime;
    }

    public static boolean timestampOK(String TAG, HashMap<String, String> metaMap) {
        String timestampStr = metaMap.get("timestamp");

        if (timestampStr == null)
            return false;

        long sysTime = Long.parseLong(SystemProperties.get("ro.build.date.utc", "-1"));
        long metaTime = Long.parseLong(timestampStr);

        Log.d(TAG, "timestampOK: metaTime = "+ metaTime);
        Log.d(TAG, "timestampOK: sysTime  = "+ sysTime);
        Log.d(TAG, "timestampOK: metaTime > sysTime , result: " + (metaTime > sysTime));

        return true;//metaTime > sysTime;
    }

    public boolean freeSpaceEnough(String payloadBin, String payloadProp, String statFsPath)
    {
        StatFs stat;
        long blockSize;
        long availableBlocks;
        long freeSpace;
        long otaFileSize;

        // get free space
        stat = new StatFs(statFsPath);
        blockSize = stat.getBlockSize();
        availableBlocks = stat.getAvailableBlocks();
        freeSpace = (availableBlocks * blockSize) / MB;
        Log.d(TAG, "freeSpaceEnough: free space = " + freeSpace);

        // get OTA file size
        otaFileSize = (new File(payloadBin).length() / MB) + (new File(payloadProp).length() / MB);
        Log.d(TAG, "freeSpaceEnough: OTA file size = " + otaFileSize ) ;

        return freeSpace >= otaFileSize;
    }

    public boolean freeSpaceEnough(String otaZip, String statFsPath)
    {
        StatFs stat;
        long blockSize;
        long availableBlocks;
        long freeSpace;
        long otaFileSize;

        // get free space
        stat = new StatFs(statFsPath);
        blockSize = stat.getBlockSize();
        availableBlocks = stat.getAvailableBlocks();
        freeSpace = (availableBlocks * blockSize) / MB;
        Log.d(TAG, "freeSpaceEnough: free space = " + freeSpace);

        // get OTA file size
        otaFileSize = (new File(otaZip).length() / MB);
        Log.d(TAG, "freeSpaceEnough: OTA file size = " + otaFileSize ) ;

        return freeSpace >= otaFileSize;
    }

    public long timestampOf(String filePath) {
        String line = null;
        //Log.d(TAG, "timestampOf: filePath = " +filePath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("post-timestamp")) {
                    line = line.replace("post-timestamp=", "");
                    break;
                }
            }
            reader.close();
        }
        catch(Exception e) {
            Log.d(TAG, "" + e);
        }

        if (line == null)
            line = "0";

        return Long.parseLong(line);
    }

    public String versionOf(String filePath) {
        String line = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("post-build-incremental=")) {
                    line = line.replace("post-build-incremental=", "");
                    break;
                }
            }
            reader.close();
        }
        catch(Exception e) {
            Log.d(TAG, "" + e);
        }

        if (line == null)
            line = "-1";

        return line;
    }

    public void broadcastPackageName() {
        Log.d(TAG, "broadcastPackageName: package name = " + getPackageName());
        Intent intent = new Intent(ABUPDATE_BROADCAST_REGISTER_CALLER);
        intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
        //intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_CALLER, getPackageName());
        sendBroadcast(intent);
    }

    @SuppressLint("SetTextI18n")
    public void broadcastStartOTA(String payloadBin, String payloadProp, Boolean isHttpOta, String image_vesrion) {
        double MIN = 0;

        if (BURN_TEST) {
            MIN = 3;
            showDelayMsg(MIN);
        }

        mProgressBar.setProgress(0);
        mProgressVal.setText("0 %");
        mUpdateStatus.setText("Start system update");

        Log.d(TAG, "broadcastStartOTA: START , delay " + MIN + " min");
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(()->{
            Log.d(TAG, "broadcastStartOTA: START , bin file    = " + payloadBin);
            Log.d(TAG, "broadcastStartOTA: START , property    = " + payloadProp);
            Log.d(TAG, "broadcastStartOTA: START , is http OTA = " + isHttpOta);
            Intent intent = new Intent(ABUPDATE_BROADCAST_START);
            intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_BIN_URL,     payloadBin);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PARAM_URL,   payloadProp);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_IMAGE_VERSION,   image_vesrion);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PROPERTIES, new String[0]); // to avoid http update cause otaservice crash
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_MODE,        isHttpOta ?
                    ABUPDATE_STREAM_PAYLOAD_MODE : ABUPDATE_USB_MODE);
            sendBroadcast(intent);
        }, (long) (MIN * 60000));
    }

    @SuppressLint("SetTextI18n")
    public void broadcastStartOTA(Boolean isHttpOta) {
        double MIN = 0;

        if (BURN_TEST) {
            MIN = 3;
            showDelayMsg(MIN);
        }

        mProgressBar.setProgress(0);
        mProgressVal.setText("0 %");
        mUpdateStatus.setText("Start system update");

        Log.d(TAG, "broadcastStartOTA: START , delay " + MIN + " min");
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(()->{
            Log.d(TAG, "broadcastStartOTA: START , zip file         = " + TARGET_PATH);
            Log.d(TAG, "broadcastStartOTA: START , payload offset   = " + mPayloadOffset);
            Log.d(TAG, "broadcastStartOTA: START , payload size     = " + mPayloadSize);
            Log.d(TAG, "broadcastStartOTA: START , properties     = " + mProperties );
            Log.d(TAG, "broadcastStartOTA: START , is http OTA = " + isHttpOta);
            Intent intent = new Intent(ABUPDATE_BROADCAST_START);
            intent.setPackage(OTA_SERVICE_PACKAGE_NAME);;
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_URL,     "file://"+TARGET_PATH);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET,     mPayloadOffset);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE,     mPayloadSize);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PROPERTIES, mProperties.toArray(new String[0]));
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_MODE,        isHttpOta ?
                    ABUPDATE_STREAM_PAYLOAD_MODE : ABUPDATE_USB_MODE);

            sendBroadcast(intent);
        }, (long) (MIN * 60000));
    }

    @SuppressLint("SetTextI18n")
    public void broadcastStartOTA(Intent updateInfo) {
        double MIN = 0;

        if (BURN_TEST) {
            MIN = 3;
            showDelayMsg(MIN);
        }

        mProgressBar.setProgress(0);
        mProgressVal.setText("0 %");
        mUpdateStatus.setText("Start system update");

        Log.d(TAG, "broadcastStartOTA: START , delay " + MIN + " min");
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(()->{
            Log.d(TAG, "broadcastStartOTA: START , zip file         = " + updateInfo.getStringExtra(MainActivity.EXTRA_UPDATE_ZIP_URL));
            Log.d(TAG, "broadcastStartOTA: START , payload offset   = " + updateInfo.getLongExtra(MainActivity.EXTRA_UPDATE_ZIP_OFFSET, 0));
            Log.d(TAG, "broadcastStartOTA: START , payload size     = " + updateInfo.getLongExtra(MainActivity.EXTRA_UPDATE_ZIP_SIZE, 0));
            Log.d(TAG, "broadcastStartOTA: START , properties       = " + Arrays.toString(updateInfo.getStringArrayExtra(MainActivity.EXTRA_UPDATE_PROPERTIES)));
            Log.d(TAG, "broadcastStartOTA: START , update mode      = " + updateInfo.getIntExtra(MainActivity.EXTRA_UPDATE_MODE, 0));
            Intent intent = new Intent(ABUPDATE_BROADCAST_START);
            intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_URL,     updateInfo.getStringExtra(MainActivity.EXTRA_UPDATE_ZIP_URL));
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET,     updateInfo.getLongExtra(MainActivity.EXTRA_UPDATE_ZIP_OFFSET, 0));
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE,     updateInfo.getLongExtra(MainActivity.EXTRA_UPDATE_ZIP_SIZE, 0));
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PROPERTIES, updateInfo.getStringArrayExtra(MainActivity.EXTRA_UPDATE_PROPERTIES));
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_MODE,        updateInfo.getIntExtra(MainActivity.EXTRA_UPDATE_MODE, ABUPDATE_STREAM_ZIP_MODE));

            sendBroadcast(intent);
        }, (long) (MIN * 60000));
    }

    public void broadcastStopOTA(Context context) {
        Log.d(TAG, "broadcastStopOTA: stop Update Engine");
        Intent i = new Intent(MainActivity.ABUPDATE_BROADCAST_STOP);
        i.setPackage(MainActivity.OTA_SERVICE_PACKAGE_NAME);
        //i.setFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        context.sendBroadcast(i);
    }

    /*public void obtainCallerName() {
        Intent intent = new Intent(ABUPDATE_BROADCAST_CALLER);
        intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
        sendBroadcast(intent);
    }*/

    public void showDelayMsg(final double min) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Delay " + min + " minutes", Toast.LENGTH_LONG).show();
                //new Handler().postDelayed(this, 5000);
            }
        });
    }

    /*public void reboot() {
        Log.d(TAG, "ro.boot.slot_suffix = " + SystemProperties.get("ro.boot.slot_suffix", "-1"));
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        powerManager.reboot(null);
    }*/

    public void reboot() {
        Log.d(TAG, "reboot: START");
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.d(TAG, "reboot: error = " + e.getMessage());
        }
        Log.d(TAG, "reboot: END");
    }

    /* public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Log.d(TAG, "Thread sleep error = " + e.getMessage());
        }
    } */

    /* public Handler setupHandler() {
        return new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg)
            {
                if (MSG_UPDATE_STATUS_PROGRESS == msg.what) {
                    mUI_PROGRESS = msg.arg1;
                    mUI_STATUS = msg.arg2;
                    return;
                }
                if (MSG_UPDATE_ERROR_CODE == msg.what) {
                    mUI_ERR_CODE = msg.arg1;
                    return;
                }
                if (MSG_RESET_STATUS_EXCEPTION == msg.what) {
                    //Context ctx = (Context) msg.obj;
                    Intent intent = new Intent(ABUpdateReceiver.ABUPDATE_BROADCAST_START);
                    intent.setPackage(mContext.getPackageName());
                    intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
                    intent.putExtra(ABUpdateReceiver.ABUPDATE_BROADCAST_UPDATE_BIN_URL,     mPayloadBin);
                    intent.putExtra(ABUpdateReceiver.ABUPDATE_BROADCAST_UPDATE_PARAM_URL,   mPayloadProp);
                    intent.putExtra(ABUpdateReceiver.ABUPDATE_BROADCAST_UPDATE_MODE,        ABUpdateReceiver.ABUPDATE_USB_MODE);
                    mContext.sendBroadcast(intent);
                    return;
                }
            }
        }
    }; */

    public BroadcastReceiver mOtaReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);

            if (ABUPDATE_BROADCAST_CALLER.equals(action)) {
                String packageName = intent.getStringExtra(ABUPDATE_BROADCAST_UPDATE_CALLER);
                Log.d(TAG, "onReceive: [OTA service] caller package name = " + packageName);
            }
            else
            if (ABUPDATE_BROADCAST_ERROR.equals(action)) {
                int errorCode = intent.getIntExtra(ABUPDATE_BROADCAST_ERROR_CODE, -1);
                String errorMsg = intent.getStringExtra(ABUPDATE_BROADCAST_ERROR_MSG);
                Log.d(TAG, "onReceive: [OTA service] errorCode = " + errorCode + " , errorMsg = " + errorMsg);
                //if (errorMsg == null)
                //    errorMsg = "...";
                //mUpdateResult.setText(errorMsg);
                mUpdateStatus.setText("Error code : " + errorCode + " "+errorMsg);
                mForceUpdate = false;
                if (errorMsg.equals("Download Transfer Error"))
                    sendOTAErrorToServerControlService(errorCode, errorMsg);
                sendOTAErrorToCaller();
            }
            else
            if (ABUPDATE_BROADCAST_STATUS.equals(action)) {
                int progress = intent.getIntExtra(ABUPDATE_BROADCAST_UPDATE_PROGRESS, 0);
                String status = intent.getStringExtra(ABUPDATE_BROADCAST_STATUS_MESSAGE);

                Log.d(TAG, "onReceive: [OTA service] progress = " + progress + ", status = " + status);
                if (status == null)
                    return; //status = "...";

                mProgressBar.setProgress(progress);
                mProgressVal.setText(progress + " %");
                //mUpdateStatus.setText(status);
                if(status.equals("downloading"))
                    mUpdateStatus.setText("Update is in progress");
                else
                    mUpdateStatus.setText(status);
            }
            else
            if (ABUPDATE_BROADCAST_COMPLETE.equals(action)) {
                Log.d(TAG, "onReceive: [OTA service] Success");
                Log.d(TAG, "onReceive: BURN_TEST = " + BURN_TEST + ", mForceUpdate = " + mForceUpdate + "\n");
                //mUpdateResult.setText("Success");
                mUpdateStatus.setText("Success (need reboot)");

                if (BURN_TEST || mForceUpdate) {
                    Log.d(TAG, "onReceive: before reboot , ro.boot.slot_suffix = " + SystemProperties.get("ro.boot.slot_suffix", "-1"));
                    int forceUpdateRebootCountdown = 10000;//ms
                    mDialogStartUpdate = new SureDialog(context,
                            "Force reboot after " + String.valueOf(forceUpdateRebootCountdown/1000) + " seconds.",
                            forceUpdateRebootCountdown,
                            false)
                    {
                        @Override
                        public void dismissDialog() {
                            super.dismissDialog();
                            reboot();
                        }
                    };
                } else {
                    mDialogStartUpdate = new SureDialog(context, "Do you want to reboot now?")
                    {
                        public void onClickNo() {
                            finish();
                        }
                        public void onClickYes() {
                            reboot();
                        }
                    };
                }
            }
            else
            if (STOP_OTA_UPDATER.equals(action)) {
                Log.d(TAG, "onReceive: stop ota updater");

                if (mDialogStartUpdate != null && mDialogStartUpdate.isShowing())
                    mDialogStartUpdate.dismissDialog();
                if (mDialogFinishActivity != null && mDialogFinishActivity.isShowing())
                    mDialogFinishActivity.dismissDialog();
                if (mDialogInstallPkg != null && mDialogInstallPkg.isShowing())
                    mDialogInstallPkg.dismissDialog();

                finish();
            }
            /*else
            if (ACTION_CLOSE_OTA_UI.equals(action)) {
                Log.d(TAG, "onReceive: close MainActivity");
                finish();
            }*/

            /*else // update UI
            if (ACTION_OTA_PROGRESS == action) {
                mUI_PROGRESS    = Integer.parseInt(SystemProperties.get("persist.sys.progress", "0"));
                mUI_STATUS      = Integer.parseInt(SystemProperties.get("persist.sys.status", "0"));
                mUI_ERR_CODE    = Integer.parseInt(SystemProperties.get("persist.sys.errorCode", "-1"));
                mUI_STATUS_MSG  = SystemProperties.get("persist.sys.statusMsg", "...");
                mUI_ERR_MSG     = SystemProperties.get("persist.sys.errorMsg", "...");

                Log.d(TAG, "mOtaReceiver: progress [" + mUI_PROGRESS + "] , status [" + mUI_STATUS + "] , statusMsg [" + mUI_STATUS_MSG + "] , errorCode [" + mUI_ERR_CODE + "] , errorMsg [" + mUI_ERR_MSG + "]");
                mProgressBar.setProgress(mUI_PROGRESS);
                mProgressVal.setText(String.valueOf(mUI_PROGRESS) + " %");
                mUpdateStatus.setText(mUI_STATUS_MSG);
                mUpdateResult.setText(mUI_ERR_MSG);

                if (UpdateStatus.UPDATED_NEED_REBOOT == mUI_STATUS && BURN_TEST) {
                    Log.d(TAG, "reboot after 30 secs");
                    // reboot after 30 secs
                    mHandler.postDelayed(()->{
                        reboot();
                    }, 30000);
                }
            }*/
        }
    };

    public BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = null;
            String reason = null;

            action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);

            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {

                reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.d(TAG, "onReceive: reason = " + reason);

                if (reason == null) {
                    return;
                }

                // forbid home key cause window disappear
                if (mForceUpdate) {
                    Log.w(TAG, "onReceive: forbid home key cause window disappear");
                    Intent i = new Intent(context, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(i);
                    return;
                }

                // show finish dialog
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    //Toast.makeText(context, "HOME_KEY", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(context, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    i.putExtra(EXTRA_POPUP_FINISH_DIALOG, true);
                    startActivity(i);
                }
            }
        }
    };

    private void sendOTAErrorToServerControlService(int errorCode, String errorMsg) {
        mHttpOtaUpdateAgain = true;
        Intent intent = new Intent();
        intent.setPackage(SERVER_CONTROL_SERVICE_PACKAGE_NAME);
        intent.setAction(ABUPDATE_BROADCAST_ERROR);
        intent.putExtra(ABUPDATE_BROADCAST_ERROR_CODE, errorCode);
        intent.putExtra(ABUPDATE_BROADCAST_ERROR_MSG, errorMsg);
        sendBroadcast(intent);
    }

    private void sendOTAErrorToCaller() {
        Intent intent = new Intent();
        intent.setPackage(mCallerPackageName);
        intent.setAction(ABUPDATE_BROADCAST_ERROR);
        sendBroadcast(intent);
    }

    private boolean copyFile(String fileFromPath, String fileToPath) {
        Log.d(TAG, "copyFile: source = [" + fileFromPath + "] dest = [" + fileToPath + "]");
        File sourceFile = new File(fileFromPath);
        File destFile = new File(fileToPath);

        boolean checkflag = true;

        if(destFile.exists())
        {
            try {
                destFile.delete();
            } catch (Exception e) {
                throw new RuntimeException("delete file wrong : " + e.getMessage(), e);
            }
        }

        String perm = "rwxrwxrwx";// in octal = 770

        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.copy(sourceFile.toPath(),destFile.toPath());
            }
            Log.d(TAG, "copyFile: copy file complete");
        }
        catch (Exception exception)
        {
            checkflag = false;
            Log.e(TAG, "copyFile: copy file Exception = " + exception);
        }

        try {
            Set<PosixFilePermission> permissions = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                permissions = PosixFilePermissions.fromString(perm);
            } if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.setPosixFilePermissions(destFile.toPath(), permissions);
            }
        } catch (IOException exception) {
            // logger.warning("Can't set permission '" + perm + "' to " +   dir.getPath());
            checkflag = false;
            Log.e(TAG, "copyUSBFileToLocal: change permission Exception = " + exception);
        }

        return checkflag;
    }

    private void setDataFromZip(String otaZipPath) {
        File otaZipFile = new File(otaZipPath);
        boolean payloadFound = false;

        if (!otaZipFile.exists()) {
            Log.e(TAG, "setDataFromZip: otaZipFile not exit");
            return;
        }

        List<String> metadataProperties = new ArrayList<>();
        mProperties = new ArrayList<>();

        try (ZipFile zip = new ZipFile(otaZipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            long offset = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                // Zip local file header has 30 bytes + filename + sizeof extra field.
                // https://en.wikipedia.org/wiki/Zip_(file_format)
                long extraSize = entry.getExtra() == null ? 0 : entry.getExtra().length;
                offset += 30 + name.length() + extraSize;

                if (entry.isDirectory()) {
                    continue;
                }

                long length = entry.getCompressedSize();

                if ("META-INF/com/android/metadata".equals(name)) {
                    InputStream inputStream = zip.getInputStream(entry);
                    if (inputStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = br.readLine()) != null)
                            metadataProperties.add(line);
                    }
                    for (String property : metadataProperties) {
                        if (property.startsWith("post-timestamp=")) {
                            mPostTimeStamp = Long.parseLong(property.replace("post-timestamp=", ""));
                        }
                        else if (property.startsWith("post-build-incremental=")) {
                            mNextVersion = property.replace("post-build-incremental=", "");
                        }
                    }
                    Log.d(TAG, "sameTimestamp: post_timestamp   = " + mPostTimeStamp + " mNextVersion = " + mNextVersion);
                }
                else if ("payload.bin".equals(name)) {
                    if (entry.getMethod() != ZipEntry.STORED) {
                        throw new IOException("Invalid compression method.");
                    }
                    payloadFound = true;
                    mPayloadOffset = offset;
                    mPayloadSize = length;
                    Log.d(TAG, "setDataFromZip: mPayloadOffset = " + mPayloadOffset + " mPayloadSize = " + mPayloadSize);
                }
                else if ("payload_properties.txt".equals(name)) {
                    InputStream inputStream = zip.getInputStream(entry);
                    if (inputStream != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = br.readLine()) != null) {
                            mProperties.add(line);
                        }
                    }
                    Log.d(TAG, "setDataFromZip: properties = " + mProperties);
                }
                offset += length;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "sameTimestamp: " + e);
        }

        if (!payloadFound) {
            Log.e(TAG, "setDataFromZip: Failed to find payload entry in the given package.");
        }
    }

    private String getUSBFilePath(Intent intent) {
        Log.d(TAG, "getUSBFilePath: ");
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();

        for (StorageVolume volume : storageVolumes) {
            if (volume.isRemovable()) {
                // 打印存儲卷的描述
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Log.d("USBStorage", "Storage getDirectory(): " + volume.getDirectory());
                    mFilePath = volume.getDirectory() + "/"+getImageName();
                    intent.putExtra(EXTRA_ZIP_FILE_OTA, volume.getDirectory() + "/"+getImageName());
                    return volume.getDirectory() + "/"+getImageName();
                }
            }
        }
        return null;
    }

    private boolean checkRestartOTA(Intent intent) {
        String payloadBin     = intent.getStringExtra(EXTRA_PAYLOAD_BIN);
        String payloadProp    = intent.getStringExtra(EXTRA_PAYLOAD_PROP);
        String metadata       = intent.getStringExtra(EXTRA_METADATA);

        return !isNullPath(payloadBin, payloadProp, metadata);
    }

    public static String getImageName() {
        return SystemProperties.get(PROPERTY_IMAGE_NAME, IMAGE_NAME);
    }
}