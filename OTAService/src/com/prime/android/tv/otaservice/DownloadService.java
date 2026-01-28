package com.prime.android.tv.otaservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.IOException;
import android.os.RemoteException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;

import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import vendor.prime.hardware.misc.V1_0.IMisc;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class DownloadService extends Service {
    private final String TAG = "DownloadService";
    private static DownloadService mDs = new DownloadService();
    private boolean OTA_DEBUG = true ;
    private boolean running = false;

    //Update_Engine 
    private HandlerThread mABHandlerThread = null;
    private Handler mABHandler = null;
    private UpdateEngine mABEngine = null;
    private ABUpdateReceiver mABReceiver = null;
    private UpdateEngineCallback mUpdateEngineCallback = null;
    private Runnable mABUpdateRunnable = null;
    private String mUpdatePath = "";//"https://atvgwlab.kbro.tv/OTA_file/A1B_72115_0.1/N97_A1B_72115.bin";
    private String mUpdatePropPath = "";//"https://atvgwlab.kbro.tv/OTA_file/A1B_72115_0.1/N97_A1B_72115.txt";
    private String mUpdateImageVersion = "";
    private int mUpdateMode = 0;
    public long mPayloadOffset = 0;
    public long mPayloadSize = 0;
    public String[] mProperties;

    //download manager
    private ZipFileDownload mZipFileDownload = null;

    //usb mode
    private String mBinDest = "/data/ota_package/UpdateBin.bin";
    //private String mTxtDest = "/data/media/0/UpdateTxt.txt";
    private String mTxtDest = "/sdcard/UpdateTxt.txt";

    private String[] mUpdateStatusMsgArray={"idle",
											"checking for update",
											"update available",
											"downloading",
											"verifying",
											"finalizing",
											"updated need reboot",
											"reporting error evnet",
											"attempting rollback",
											"disabled",
											"need permission to update"};
    private String[] mErrMsgArray={"Success",
									"Error",
									"Omaha Request Error",
									"Omaha Response Handler Error",
									"Filesystem Copier Error",
									"Postinstall Runner Error",
									"Payload Mismatched Type",
									"Install Device Open Error",
									"Kernel Device Ope nError",
									"Download Transfer Error",
									"Payload Hash Mismatch Error",
									"Payload Size Mismatch Error",
									"Download Payload Verification Error",
									"Download New Partition Info Error",
									"Download Write Error",
									"New Rootfs Verification Error",
									"New Kernel Verification Error",
									"Signed Delta Payload Expected Error",
									"Download Payload PubKey Verification Error",
									"Postinstall Booted From Firmware B",
									"Download State Initialization Error",
									"Download Invalid Metadata Magic String",
									"Download Signature Missing In Manifest",
									"Download Manifest Parse Error",
									"Download Metadata Signature Error",
									"Download Metadata Signature Verification Error",
									"Download Metadata Signature Mismatch",
									"Download Operation Hash Verification Error",
									"Download Operation Execution Error",
									"Download Operation Hash Mismatch",
									"Omaha Request Empty Response Error",
									"Omaha Request XML Parse Error",
									"Download Invalid Metadata Size",
									"Download Invalid Metadata Signature",
									"Omaha Response Invalid",
									"Omaha Update Ignored Per Policy",
									"Omaha Update Deferred Per Policy",
									"Omaha Error In HTTP Response",
									"Download Operation Hash Missing Error",
									"Download Metadata Signature Missing Error",
									"Omaha Update Deferred For Back off",
									"Postinstall Power wash Error",
									"Update Canceled By Channel Change",
									"Postinstall Firmware RO Not Updatable",
									"Unsupported Major Payload Version",
									"Unsupported Minor Payload Version",
									"Omaha Request XML Has Entity Decl",
									"Filesystem Verifier Error",
									"User Canceled",
									"Non Critical Update In OOBE",
									"Omaha Update Ignored Over Cellular",
									"Payload Timestamp Error",
									"Updated But Not Active",
									"No Update",
									"Rollback Not Possible",
									"First Active Omaha Ping Sent Persistence Error",
									"Verity Calculation Error",
                                    "Please Check Correct Url",
                                    "Please Check Network Connection",
                                    "Sdcard Size is less than 100KB"};
    private String[] mUpdateModeArray = {"Stream payload",
                                         "Stream zip",
                                         "USB",
                                         "Download"};
    /*
    private String OTA_SR_TRIGGER_STATUS = "persist.vendor.nx.otastart";
    private String OTA_SR_UPDATE_PATH = "persist.vendor.nx.mUpdatePath";
    private String OTA_SR_UPDATEPROPER_PATH = "persist.vendor.nx.mUpdatePropPath";
    private String OTA_SR_PAYLOAD_SIZE = "persist.vendor.nx.payloadsize";
    private String OTA_SR_ABUPDATE_TRIGGER = "persist.vendor.nx.abupdate.trigger";
    private String OTA_SR_ABUPFATE_START_FLAG = "persist.vendor.nx.abupdate.flag";
*/

    //OTA Broadcast
    private final String ABUPDATE_BROADCAST_START = "com.prime.android.tv.otaservice.abupdate.start";
    private final String ABUPDATE_BROADCAST_STOP = "com.prime.android.tv.otaservice.abupdate.stop";
    private final String ABUPDATE_BROADCAST_PAUSE = "com.prime.android.tv.otaservice.abupdate.pause";
    private final String ABUPDATE_BROADCAST_RESUME = "com.prime.android.tv.otaservice.abupdate.resume";
    private final String ABUPDATE_BROADCAST_ERROR = "com.prime.android.tv.otaservice.abupdate.error";//send error code and error msg
    private final String ABUPDATE_BROADCAST_STATUS = "com.prime.android.tv.otaservice.abupdate.status";//send error code and error msg
    private final String ABUPDATE_BROADCAST_COMPLETE = "com.prime.android.tv.otaservice.abupdate.complete";//send update complete
    private final String ABUPDATE_BROADCAST_CALLER = "com.prime.android.tv.otaservice.abupdate.caller";

    //OTA Broadcast delivery params
    private final String ABUPDATE_BROADCAST_ERROR_CODE = "prime.abupdate.errCode";
    private final String ABUPDATE_BROADCAST_ERROR_MSG = "prime.abupdate.errStr";
    private final String ABUPDATE_BROADCAST_UPDATE_PROGRESS = "prime.abupdate.progress";
    private final String ABUPDATE_BROADCAST_STATUS_MESSAGE = "prime.abupdate.statusMsg";
	private final String ABUPDATE_BROADCAST_UPDATE_BIN_URL = "prime.abupdate.url.bin";
    private final String ABUPDATE_BROADCAST_UPDATE_PARAM_URL = "prime.abupdate.url.txt";
    private final String ABUPDATE_BROADCAST_UPDATE_CALLER = "prime.abupdate.caller";
    private final String ABUPDATE_BROADCAST_EXECUTION_COMPONENT = "prime.execution.component";
    private final String ABUPDATE_BROADCAST_EXECUTION_RESULT = "prime.execution.result";

    //OTA Check Current Status property
    private final String TEMP_ABUPDATE_PERCENT = "persist.sys.abupdate.percent";
    private final String TEMP_ABUPDATE_STATUS = "persist.sys.abupdate.status";
    private final String TEMP_ABUPDATE_COMPLETE = "persist.sys.abupdate.complete";
    private final String TEMP_ABUPDATE_ERRORFLAG = "persist.sys.abupdate.errorflag";
    private final String TEMP_ABUPDATE_ERRORCODE = "persist.sys.abupdate.errorcode";
    private final String SET_ORIGIN_RED_LED = "persist.sys.abupdate.led.red";
    private final String SET_ORIGIN_GREEN_LED = "persist.sys.abupdate.led.green";
    private final String SET_OTASERVICE_TEST_ENABLE = "persist.sys.abupdate.otaservicetest.enable";
    private final String SET_NETWORK_IS_READY = "persist.sys.abupdate.internet.ready";
    private final String ALREADY_UPDATE_COMPLETE_VERSION = "persist.sys.abupdate.complete.version";

    //Home Launcher package name
	private final String HOME_LAUNCHER_PACKAGE_NAME = "com.prime.android.tv.otaservicetest";
	private String mCallerPackageName = "com.prime.otaservicetestapp";

	//update mode
    private final int ABUPDATE_STREAM_MODE = 0;
    private final int ABUPDATE_STREAM_ZIP_MODE = 1;
    private final int ABUPDATE_USB_MODE = 2;
    private final int ABUPDATE_DOWNLOAD_MODE = 3;

    //SET LED
	private IMisc mPesiService;
    private static final int GREEN_LED      = 1;
    private static final int RED_LED        = 2;
    private static final int ORANGE_LED     = 3;	
    private static final int LED_BRIGHTNESS_100 = 100;
    private static final int LED_BRIGHTNESS_50  =  50;
    private static final int LED_BRIGHTNESS_25  =  25;
    private static final int LED_BRIGHTNESS_0   =   0;
    private static final int SETLED_BRIGHT      =   0x1003;
    private static final int SETLED_BRIGHT_PROP =   0x1004;

    //OTA Current Status, not repeat
    private int ABPERCENT = -1;
    private int mOTAStatus = 0;
    private final String FALSE = "false", TRUE = "true";
    private int red_led_value = 1, green_led_value = 0;
	private int origin_red_led_value = 0, origin_green_led_value = 0;

    //Add new Error not from Update_Engine
    private final int mErrUrl = 57;//add for start OTA with null url
    private final int mErrNetwork = 58;//add for start OTA without network
    private final int ErrStorage = 59;//add for start OTA sdcard without 100KB space
    private final int mErrCopyFile = 4;

    //Storage Check
    private final int MB = 1024*1024 ;
    private final int KB = 1024 ;
    private final int ABUPDATE_MIN_SPACE = 100;//KB
    private final String SDCARD_PATH = "/sdcard";//Check Sdcard space at lease 100KB

    //Network Connection
    private ConnectivityManager mConnectivityManager = null;
    private ConnectivityManager.NetworkCallback mNetworkCallback = null;
    private NetworkRequest mNetworkRequest = null;
    private final String INTERNET_URL = "https://www.google.com/";//for checking internet

    //Write 200K files for Android OTA Update rules
    private final String mSaveSpaceFile = "SaveSpace";
    private final int File_KB = 1024;
    private final int Min_Save_Size = 200*1024;
    private final String mSaveOtaPath = Environment.getExternalStorageDirectory()+"/"+mSaveSpaceFile;
/*
    public DownloadService() {
            Log.d(TAG, "DownloadService: IN");
    }

    private static DownloadService mInstance;
    public static DownloadService getInstance() {
        if(mInstance == null) {
            mInstance = mDs;
            Log.d("TAG","Instance ==>> ["+ mInstance + "]");
        }
        return mInstance;
    } 
*/
    public Context mContext = null;
    public static final int MSG_AB_HANDLER_START    = 1000;
    public static final int MSG_AB_UPDATE_STOP      = 1001;
    public static final int MSG_AB_UPDATE_PAUSE     = 1002;
    public static final int MSG_AB_UPDATE_RESUME    = 1003;
    public static final int MSG_REGISTER_CALLER     = 1004;
	public static final int MSG_SET_GREEN_LED       = 1005;
	public static final int MSG_SET_RED_LED         = 1006;	
    public static final int MSG_DOWNLOAD_ZIP        = 1007;
    public static final int MSG_DOWNLOAD_STATUS     = 1008;
    public Handler mHandler = new_Handler();
    public Handler new_Handler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    switch (msg.what) {
                        case MSG_AB_HANDLER_START:
                            if (mOTAStatus == OTA_Cur_Status.ZipFileDownloading.getValue()) {
                                Log.d(TAG, "Zip file downloading, is not able to start ota update");
                                break;
                            }
                                
                            JSONObject obj = (JSONObject) msg.obj;
                            int updateMode = obj.getInt("updateMode");
                            String UpdateImageVersion = obj.getString("UpdateImageVersion");
                            if (updateMode == ABUPDATE_USB_MODE) {
                                String UpdateZipFile = obj.getString("UpdateZipFile");
                                long UpdatePayloadOffset = obj.getLong("UpdatePayloadOffset");
                                long UpdatePayloadSize = obj.getLong("UpdatePayloadSize");
                                JSONArray UpdateProperties = obj.getJSONArray("UpdateProperties");
                                abHandlerStart(UpdateZipFile, UpdatePayloadOffset, UpdatePayloadSize, UpdateProperties, UpdateImageVersion, updateMode, mContext);
                            }
                            else if (updateMode == ABUPDATE_DOWNLOAD_MODE) {
                                setDataFromZip(mZipFileDownload.updateFileSavePath);
                                String downloadFilePath = "file://" + mZipFileDownload.updateFileSavePath;
                                abHandlerStart(downloadFilePath, mPayloadOffset, mPayloadSize, null, UpdateImageVersion, updateMode, mContext);
                            }
                            else if (updateMode == ABUPDATE_STREAM_ZIP_MODE) {
                                String UpdateZipUrl = obj.getString("UpdateZipFile");
                                long UpdatePayloadOffset = obj.getLong("UpdatePayloadOffset");
                                long UpdatePayloadSize = obj.getLong("UpdatePayloadSize");
                                JSONArray UpdateProperties = obj.getJSONArray("UpdateProperties");
                                abHandlerStart(UpdateZipUrl, UpdatePayloadOffset, UpdatePayloadSize, UpdateProperties, UpdateImageVersion, updateMode, mContext);
                            }
                            else {
                                String UpdateBinUrl = obj.getString("UpdateBinUrl");
                                String UpdateParamUrl = obj.getString("UpdateParamUrl");
                                abHandlerStart(UpdateBinUrl,UpdateParamUrl,UpdateImageVersion, updateMode, mContext);
                            }
                            break;
                        case MSG_AB_UPDATE_STOP:
                            ABUpdateStop(mContext);
                            break;
                        case MSG_AB_UPDATE_PAUSE:
                            ABUpdatePause(mContext);
                            break;
                        case MSG_AB_UPDATE_RESUME:
                            ABUpdateResume(mContext);
                            break;
                        case MSG_REGISTER_CALLER:
                            String pkgName = (String) msg.obj;
                            registerCallerPackageName(pkgName, mContext);
                            break;
                        case MSG_SET_GREEN_LED:
							setGreenLed(1);
							setRedLed(0);
                            break;
                        case MSG_SET_RED_LED:
							setGreenLed(0);
							setRedLed(1);
                            break;														
                        case MSG_DOWNLOAD_ZIP:
                            JSONObject jsObj = (JSONObject) msg.obj;
                            String downloadUrl = jsObj.getString("DownloadZipUrl");
                            String downloadMd5 = jsObj.getString("DownloadZipMd5");
                            //mZipFileDownload.StartDownloadUpdateFile(downloadUrl);
                            ABUpdateStop(mContext);
                            setOTAStatus(OTA_Cur_Status.ZipFileDownloading.getValue());
                            Log.d(TAG, "mOTAStatus = " + mOTAStatus);
                            mZipFileDownload.downloadFile(downloadUrl, downloadMd5);
                            break;
                        case MSG_DOWNLOAD_STATUS:
                            setOTAStatus((int) msg.obj);
                            Log.d(TAG, "mOTAStatus = " + mOTAStatus);
                            break;
                        default:
                            Log.d(TAG, "handle default message !");
                            break;
                    } // switch END
                }
                catch (Exception e) {
                    Log.e(TAG, "handleMessage Exception = " + e.getMessage());
                }
            } // handleMessage END
        };
    }
    
    private ConnectivityManager.NetworkCallback mWifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            Log.d(TAG, "wifi is available");
        }
        @Override
        public void onLost(Network network){
            Log.d(TAG, "wifi is lost");
            setWifiLed(0);
        }
        @Override
        public void onCapabilitiesChanged (Network network,
                                           NetworkCapabilities capabilities){
            super.onCapabilitiesChanged(network, capabilities);
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.d(TAG, "Wi-Fi is connected");
                setWifiLed(1);
            }
            else {
                Log.d(TAG, "Other network type is connected");
                //setWifiLed(0);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: In Version [" + getString(R.string.app_name) + "]");
        Log.d(TAG, "onCreate: Tony 20200518 11:00");
        NotificationChannel channel = new NotificationChannel("1","DownloadService",
                NotificationManager.IMPORTANCE_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(getApplicationContext(),"1").build();
        startForeground(1, notification);
        mContext = getApplicationContext();
        mABEngine = new UpdateEngine();
        mABReceiver = new ABUpdateReceiver();
        mABReceiver.registerHandler(mHandler);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_START);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_STOP);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_PAUSE);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_RESUME);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_ERROR);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_STATUS);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_COMPLETE);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_REGISTER_CALLER);
        intentFilter.addAction(ABUpdateReceiver.ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE);
        intentFilter.addAction(ABUpdateReceiver.TEST_ABUPDATE_BROADCAST_START);
        intentFilter.addAction(ABUpdateReceiver.TEST_ABUPDATE_BROADCAST_STOP);
        intentFilter.addAction(ABUpdateReceiver.TEST_ABUPDATE_BROADCAST_PAUSE);
        intentFilter.addAction(ABUpdateReceiver.TEST_ABUPDATE_BROADCAST_RESUME);
        getApplicationContext().registerReceiver(mABReceiver, intentFilter, Context.RECEIVER_EXPORTED);

        SystemProperties.set(TEMP_ABUPDATE_PERCENT,"-1");
        SystemProperties.set(TEMP_ABUPDATE_COMPLETE,"0");
        SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"0");
        SystemProperties.set(TEMP_ABUPDATE_STATUS, "...");
		
		try {
			mPesiService = IMisc.getService(true);
			Log.d(TAG," Get mPesiService => [" + mPesiService + "]");			
		} catch (RemoteException e) {
			Log.e(TAG, "RemoteException : IMisc getService fail", e);
		}		

        //SystemProperties.set(SET_ORIGIN_RED_LED,"0");
        //SystemProperties.set(SET_ORIGIN_GREEN_LED,"1");

        SystemProperties.set(ALREADY_UPDATE_COMPLETE_VERSION,"-1");

        Log.d(TAG, "onCreate: ALREADY_UPDATE_COMPLETE_VERSION [" + SystemProperties.get(ALREADY_UPDATE_COMPLETE_VERSION,"0") + "]");

        setOTAStatus(OTA_Cur_Status.Idle.getValue());//Set mOTAStatus Idle
        initNetworkInfoLis();//Init Network callback to check internet before OTA
        setLogEnable(false);

        mZipFileDownload = new ZipFileDownload(this);
        mZipFileDownload.registerHandler(mHandler);
		mZipFileDownload.check_download_failed_retry();
    }

    public void registerCallerPackageName(String packageName, Context context)
    {
        mCallerPackageName = packageName;
        Log.d(TAG, "11111 registerCallerPackageName: CallerPackageName = [" +mCallerPackageName+"]");
        SendUpdateCallerPackageName(context);
    }

    public void SendUpdateCallerPackageName(Context context)
    {
        //if(getLogEnable())
        Log.d(TAG, "SendUpdateCallerPackageName: CallerPackageName = [" +mCallerPackageName+"]");

        //Send progress Broadcast
        Intent intent = new Intent();

        intent.setPackage(mCallerPackageName);

        //intent.setPackage("com.prime.otaservicetestapp");
        intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.setAction(ABUPDATE_BROADCAST_CALLER);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_CALLER, mCallerPackageName);
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void SendUpdateProgress(int Percent, String statusMsg)
    {
        //if(getLogEnable())
        Log.d(TAG, "SendUpdateProgress: ===>>> IN Percent = [" +Percent+"] , statusMsg = [" + statusMsg + "]" + ", mCallerPackageName = " + mCallerPackageName);
        setBlinkLed();//when start OTA, always slash led before OTA complete callback
        if(Percent <= 0)
            return;
		if(Percent <= 100)
		{
			//Send progress Broadcast
			Intent intent = new Intent();

			intent.setPackage(mCallerPackageName);
            //intent.setPackage("com.prime.otaservicetestapp");
			intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
			intent.setAction(ABUPDATE_BROADCAST_STATUS);
			intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PROGRESS,Percent);
            intent.putExtra(ABUPDATE_BROADCAST_STATUS_MESSAGE, statusMsg);
			sendBroadcastAsUser(intent, UserHandle.ALL);
		}
    }

    public void SendErrBroadCast(int errCode)
    {
        Log.d(TAG, "SendErrBroadCast: ===>>> IN errCode = [" + errCode +"] ");
        //int RedLed = Integer.valueOf(SystemProperties.get(SET_ORIGIN_RED_LED,"0"));
        //int GreenLed = Integer.valueOf(SystemProperties.get(SET_ORIGIN_GREEN_LED,"0"));
        int RedLed = mABReceiver.getOriginRedLed();
        int GreenLed = mABReceiver.getOriginGreenLed();

        setOTAStatus(OTA_Cur_Status.Idle.getValue());//Set mOTAStatus Idle
        setRedLed(RedLed);
        setGreenLed(GreenLed);
        Intent intent = new Intent();

        intent.setPackage(mCallerPackageName);
        //intent.setPackage("com.prime.otaservicetestapp");
		intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.setAction(ABUPDATE_BROADCAST_ERROR);
        intent.putExtra(ABUPDATE_BROADCAST_ERROR_CODE,errCode);
        intent.putExtra(ABUPDATE_BROADCAST_ERROR_MSG,mErrMsgArray[errCode]);
        sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void SendUpdateComplete()
    {
        Log.d(TAG, "SendUpdateComplete: ===>>> IN , mCallerPackageName = " + mCallerPackageName);
        //int RedLed = Integer.valueOf(SystemProperties.get(SET_ORIGIN_RED_LED,"0"));
        //int GreenLed = Integer.valueOf(SystemProperties.get(SET_ORIGIN_GREEN_LED,"0"));
        int RedLed = mABReceiver.getOriginRedLed();
        int GreenLed = mABReceiver.getOriginGreenLed();

        setOTAStatus(OTA_Cur_Status.Idle.getValue());//Set mOTAStatus Idle
		setRedLed(RedLed);
		setGreenLed(GreenLed);
        Intent intent = new Intent();

        intent.setPackage(mCallerPackageName);
        //intent.setPackage("com.prime.otaservicetestapp");
		intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.setAction(ABUPDATE_BROADCAST_COMPLETE);
        sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void SendStatusExecution(String ExecutionName, boolean ExecutionResult, Context context)
    {
        Log.d(TAG, "SendStatusExecution: ===>>> IN ");
        Intent intent = new Intent();

        intent.setPackage(mCallerPackageName);
        intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.setAction(ABUPDATE_BROADCAST_STATUS);
        intent.putExtra(ABUPDATE_BROADCAST_EXECUTION_COMPONENT,ExecutionName);
        intent.putExtra(ABUPDATE_BROADCAST_EXECUTION_RESULT,ExecutionResult);
        if (context != null)
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
        else
            sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void CreateABUpdateThread()
    {
        if(getLogEnable())
            Log.d(TAG, "CreateABUpdateThread start");

        if(mABEngine == null)
        {
            Log.d(TAG, "CreateABUpdateThread ==>> create mABEngine");
            mABEngine = new UpdateEngine();
        }

        if(mABUpdateRunnable == null)
        {
            Log.d(TAG, "CreateABUpdateThread ==>> create mABUpdateRunnable");
            SetABUpdateRunnable();
        }

        if(mABHandlerThread == null)
        {
            Log.d(TAG, "CreateABUpdateThread ==>> create mABHandlerThread");
            mABHandlerThread = new HandlerThread("abthread");
            mABHandlerThread.start();
        }

        if(mABHandler == null)
        {
            Log.d(TAG, "CreateABUpdateThread ==>> create mABHandler");
            mABHandler = new Handler(mABHandlerThread.getLooper());
        }

        if(getLogEnable())
            Log.d(TAG, "CreateABUpdateThread finish");

    }

    public void CloseAbHadleThread()
    {
        if(getLogEnable())
            Log.d(TAG, "CloseAbHadleThread start");

        if(mABUpdateRunnable != null)
        {
            Log.d(TAG, "CloseAbHadleThread ===>> Remove mABUpdateRunnable callback");
            mABHandler.removeCallbacks(mABUpdateRunnable);
            mABUpdateRunnable = null;
        }

        if(mABHandlerThread != null)
        {
            Log.d(TAG, "CloseAbHadleThread: ===>>> close mABHandlerThread");
            mABHandlerThread.quit();
            mABHandlerThread = null;
        }

        if(mABHandler != null)
        {
            Log.d(TAG, "CloseAbHadleThread: ===>>> close mABHandler");
            mABHandler = null;
        }

        if(getLogEnable())
            Log.d(TAG, "CloseAbHadleThread finsih");
    }

    public void ABUpdateStop(Context context)
    {
        if(getLogEnable())
            Log.d(TAG, "ABUpdateStop ===>> mOTAStatus = [" + mOTAStatus + "]");

        boolean checkFlag = false;

        if(mABEngine != null)
        {
            if(mOTAStatus == OTA_Cur_Status.OnGoing.getValue() || mOTAStatus == OTA_Cur_Status.Pause.getValue())
            {
                mABEngine.cancel();//After stop finshided, it will receive error callback, shall set "Idle" there
                checkFlag = true;
            }else{
                    Log.e(TAG, "Now is not on OTA Stop condition"); 
            }
        }else{
                Log.e(TAG, "Please Start OTA Update before Set Stop"); 
        }

        SendStatusExecution("Stop", checkFlag, context);
    }

    public void ABUpdatePause(Context context)
    {
        if(getLogEnable())
            Log.d(TAG, "ABUpdatePause ===>> mOTAStatus = [" + mOTAStatus + "]");

        boolean checkFlag = false;

        if(mABEngine != null)
        {
            if(mOTAStatus == OTA_Cur_Status.OnGoing.getValue())
            {
                mABEngine.suspend();
                setOTAStatus(OTA_Cur_Status.Pause.getValue());//Set mOTAStatus Pause
                checkFlag = true;
            }else{
                    Log.e(TAG, "Now is not on OTA Pause condition"); 
            }
        }else{
                Log.e(TAG, "Please Start OTA Update before Set Pause"); 
        }

        SendStatusExecution("Pause", checkFlag, context);
    }

    public void ABUpdateResume(Context context)
    {
        if(getLogEnable())
            Log.d(TAG, "ABUpdateResume ===>> mOTAStatus = [" + mOTAStatus + "]");

        boolean checkFlag = false;

        if(mABEngine != null)
        {
            if(mOTAStatus == OTA_Cur_Status.Pause.getValue())
            {
                mABEngine.resume();
                setOTAStatus(OTA_Cur_Status.OnGoing.getValue());//Set mOTAStatus OnGoing
                checkFlag = true;
            }else{
                    Log.e(TAG, "Now is not on OTA Resume condifion"); 
            }
        }else{
                Log.e(TAG, "Please Start OTA Update before Set Resume"); 
        }

        SendStatusExecution("Resume", checkFlag, context);
    }

    public void setLogEnable(boolean enable)
    {
        if(enable)
            Log.d(TAG, "Enable ABUpdate logcat");
        else
            Log.d(TAG, "Disable ABUpdate logcat");

		OTA_DEBUG = enable;
    }

    public boolean getLogEnable()
    {
        return OTA_DEBUG;
    }

    public int getOTAStatus()
    {
        return mOTAStatus;
    }

    public void setOTAStatus(int status)
    {
        mOTAStatus = status;
    }

	private void setBlinkLed()
	{
		if(red_led_value == 1)
			green_led_value = 0;
		else
			green_led_value = 1;
		
		setRedLed(red_led_value);
		setGreenLed(green_led_value);	
		
		if(red_led_value == 1)
			red_led_value = 0;
		else
			red_led_value = 1;

	}

	private void setUpdateTime()
	{
		int rc = 0;
		String time="";
		Calendar c = Calendar.getInstance(); 
		c.setTimeZone(TimeZone.getTimeZone("GMT+08"));
		int year = c.get(Calendar.YEAR) ;
		int month = c.get(Calendar.MONTH)+1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY); 
		int minute = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);
		time = year + " " + month + " " + day + " " + hour + " " + minute + " " + sec + " ";
		Log.d(TAG," Calendar time => [" + time + "]");
		
		// try{
		// 	mCommand.tag = SET_UPDATE_TIME;
		// 	mCommand.value = 1;
		// 	mCommand.str = time;
		// 	rc = mPesiService.send_command(mCommand);
		// 	Log.d(TAG, "setUpdateTime: ===>>> rc = [" +rc+"]");
		// }
		// catch (RemoteException e) {
		// 	Log.e(TAG, "RemoteException : IMisc send_command setUpdateTime fail", e);
		// }
	}

	private void setRedLed(int enable)
	{
		//int brightness_value = (enable==1)?LED_BRIGHTNESS_100:LED_BRIGHTNESS_0;
        try {
            //mPesiService.invoke_cmd(SETLED_BRIGHT, RED_LED, brightness_value);
			mPesiService.SetGpio(26, 1, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException : IMisc send_command fail", e);
        }
	}

	private void setGreenLed(int enable)
	{
		//int brightness_value = (enable==1)?LED_BRIGHTNESS_100:LED_BRIGHTNESS_0;
        try {

            //mPesiService.invoke_cmd(SETLED_BRIGHT, GREEN_LED, brightness_value);
			mPesiService.SetGpio(27, 1, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException : IMisc send_command fail", e);
        }
	}
    
    private void setWifiLed(int enable)
	{
        try {
            mPesiService.SetGpio(53, 1, enable);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException : IMisc send_command fail", e);
        }
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: IN");
        Log.d(TAG, "onStartCommand: mCallerPackageName = "  + mCallerPackageName);
        running = true;

        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.i(TAG, "running "+running);
				int start_count = 0;

                while (running) {
                    int tmppercent = Integer.valueOf(SystemProperties.get(TEMP_ABUPDATE_PERCENT,"-1"));
                    int completeflag = Integer.valueOf(SystemProperties.get(TEMP_ABUPDATE_COMPLETE,"0"));
                    int errorflag = Integer.valueOf(SystemProperties.get(TEMP_ABUPDATE_ERRORFLAG,"0"));
                    int errorcode = Integer.valueOf(SystemProperties.get(TEMP_ABUPDATE_ERRORCODE,"0"));
                    String statusMsg = SystemProperties.get(TEMP_ABUPDATE_STATUS, "...");

                    //if(getLogEnable())
                    /*    Log.d(TAG, "Service running!! tmp tmppercent = [" +  tmppercent
                                + "] completeflag = [" + completeflag
                                + "] errorflag =[" +errorflag +"]");*/
                   try {
                        if(tmppercent >= 0)//update processing, send progress
                        {
                            SendUpdateProgress(tmppercent, statusMsg);
                        }
                        if(completeflag != 0) //update complete, send broadcast
                        {
							SendUpdateProgress(100, statusMsg);
                            SendUpdateComplete();
							SystemProperties.set(TEMP_ABUPDATE_COMPLETE,"0");
                            if(completeflag != 2)
							    setUpdateTime();
                            setLogEnable(false);
							
                        }
                        if(errorflag != 0)//error, send broadcast
                        {
                            SendStatusExecution("Start", false, null);
                            SendErrBroadCast(errorcode);
                            SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"0");
                            SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,"0");
                            setLogEnable(false);
                        }

                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(getLogEnable())
                    Log.i(TAG, "Service stop!!");
            }
        }.start();

        if(getLogEnable())
            Log.d(TAG, "onStartCommand: OUT");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: IN");

        mABHandler.removeCallbacks(mABUpdateRunnable);
        mABHandlerThread.quit();
        stopForeground(true);
        running = false;
        unregisterReceiver(mABReceiver);
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        mConnectivityManager.unregisterNetworkCallback(mWifiNetworkCallback);

        Log.d(TAG, "onDestroy: OUT");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: IN");
        return null;
    }

    public class PayloadInfo {
        public List<String> content = null;
        public long payloadSize;
        public String payloadPath;

        public PayloadInfo() {
            content = new ArrayList<>();
            payloadSize = 0;
            payloadPath = "";
        }
    }

    public void abHandlerStart(String UpdateBinUrl, String UpdateParamUrl, String ImageVersion, int updateMode, Context context)
    {
        Log.d(TAG, "abHandlerStart ===>> mOTAStatus = [" + mOTAStatus + "] UpdateBinUrl = [" +UpdateBinUrl+"] UpdateParamUrl = ["+ UpdateParamUrl +
         "] ImageVersion =[" + ImageVersion + "] saveImageVersion = [" + SystemProperties.get(ALREADY_UPDATE_COMPLETE_VERSION,"0") + "]");
        mUpdateImageVersion = ImageVersion;

        if(ImageVersion.equals(SystemProperties.get(ALREADY_UPDATE_COMPLETE_VERSION,"0")))
        {
            SystemProperties.set(TEMP_ABUPDATE_COMPLETE,"2");//Set Complete to 2 not to set update time with UpdateSame Update Image Version
            Log.e(TAG, "This OTA Image is already upadte complete version = [" + mUpdateImageVersion +"] no set time again");
            return;
        }else{
            if(ImageVersion.equals("null"))
            {
                Log.d(TAG,"for compatibility set ImageVersion to 0 to start update with old trigger approach");
                mUpdateImageVersion = "0";
            }
        }

        if(mOTAStatus == OTA_Cur_Status.OnGoing.getValue() || mOTAStatus == OTA_Cur_Status.Pause.getValue())
        {
            Log.e(TAG, "OTA Update is Already OnGoing! No need to start again");
            SendStatusExecution("Start", false, context);
            return;
        }
/*
        if(!checkFreeSpace(SDCARD_PATH,ABUPDATE_MIN_SPACE))//Sdcard Space need larger than 100KB
        {
            Log.e(TAG, "Please clear the sdcard space befor starting OTA!!");
            SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
            SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(ErrStorage));
            return;
        }
*/
        if( UpdateBinUrl == null || UpdateParamUrl == null)//Add protect because Alticast not return url string when not get correct url, result in crash!!!
        {
            Log.e(TAG, "Please check the correct url before starting OTA!!");
            SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
            SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(mErrUrl));
            return;
            //UpdateBinUrl = "null";
            //UpdateParamUrl = "null";
            //UpdateBinUrl = "https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.bin";
            //UpdateParamUrl = "https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.txt";
        }

        //Check Network connection
        if(!NetworkIsReady() && updateMode == ABUPDATE_STREAM_MODE)
        {
            Log.e(TAG, "Please Check Network is Ready before OTA");
            SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
            SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(mErrNetwork));
            return;
        } 

        //Check OTA 200K File exist and delete before OTA Update
        File file = new File(mSaveOtaPath);
        if(isOTASavedFileExist(file))
        {
            try {
                file.delete();
            } catch (Exception e) {
                throw new RuntimeException("delete file wrong : " + e.getMessage(), e);
            } 
        }

        setLogEnable(true); 
        setOTAStatus(OTA_Cur_Status.OnGoing.getValue());//Set mOTAStatus OnGoing
        mUpdatePath = UpdateBinUrl;//N97_A1B_72115.bin
		mUpdatePropPath = UpdateParamUrl;//N97_A1B_72115.txt
        mUpdateMode = updateMode;
        Log.d(TAG, "abHandlerStart: IN UpdateBinUrl = [" + UpdateBinUrl + "] UpdateParamUrl = [" + UpdateParamUrl + "] mUpdateImageVersion = [" + mUpdateImageVersion +"] updateMode = [" + mUpdateModeArray[mUpdateMode] + "]");

        CreateABUpdateThread();
        mABEngine.resetStatus();//reset update_engine status for newer OTA Imgaes
        mABHandler.post(mABUpdateRunnable);

        SendStatusExecution("Start", true, context);
    }

    public void abHandlerStart(String UpdateZipFile, long UpdatePayloadOffset, long UpdatePayloadSize, JSONArray UpdateProperties, String ImageVersion, int updateMode, Context context)
    {
        Log.d(TAG, "abHandlerStart ===>> mOTAStatus = [" + mOTAStatus + "] UpdateZipFile = [" +UpdateZipFile+"] UpdatePayloadOffset = ["+ UpdatePayloadOffset +
        "] UpdatePayloadSize =[" + UpdatePayloadSize + "] UpdateProperties =[" + UpdateProperties + "] ImageVersion =[" + ImageVersion + "] saveImageVersion = [" + SystemProperties.get(ALREADY_UPDATE_COMPLETE_VERSION,"0") + "]");
        mUpdateImageVersion = ImageVersion;

        if(ImageVersion.equals(SystemProperties.get(ALREADY_UPDATE_COMPLETE_VERSION,"0")))
        {
            SystemProperties.set(TEMP_ABUPDATE_COMPLETE,"2");//Set Complete to 2 not to set update time with UpdateSame Update Image Version
            Log.e(TAG, "This OTA Image is already upadte complete version = [" + mUpdateImageVersion +"] no set time again");
            return;
        }else{
            if(ImageVersion.equals("null"))
            {
                Log.d(TAG,"for compatibility set ImageVersion to 0 to start update with old trigger approach");
                mUpdateImageVersion = "0";
            }
        }

        if(mOTAStatus == OTA_Cur_Status.OnGoing.getValue() || mOTAStatus == OTA_Cur_Status.Pause.getValue())
        {
            Log.e(TAG, "OTA Update is Already OnGoin! No need to start again");
            SendStatusExecution("Start", false, context);
            return;
        }
/*
        if(!checkFreeSpace(SDCARD_PATH,ABUPDATE_MIN_SPACE))//Sdcard Space need larger than 100KB
        {
            Log.e(TAG, "Please clear the sdcard space befor starting OTA!!");
            SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
            SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(ErrStorage));
            return;
        }
*/
        if(UpdateZipFile == null)//Add protect because Alticast not return url string when not get correct url, result in crash!!!
        {
            Log.e(TAG, "Please check the correct url before starting OTA!!");
            SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
            SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(mErrUrl));
            return;
            //UpdateBinUrl = "null";
            //UpdateParamUrl = "null";
            //UpdateBinUrl = "https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.bin";
            //UpdateParamUrl = "https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.txt";
        }

        //Check Network connection
        if(!NetworkIsReady() && (updateMode == ABUPDATE_STREAM_MODE || updateMode == ABUPDATE_STREAM_ZIP_MODE))
        {
            Log.e(TAG, "Please Check Network is Ready before OTA");
            SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
            SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(mErrNetwork));
            return;
        } 

        //Check OTA 200K File exist and delete before OTA Update
        File file = new File(mSaveOtaPath);
        if(isOTASavedFileExist(file))
        {
            try {
                file.delete();
            } catch (Exception e) {
                throw new RuntimeException("delete file wrong : " + e.getMessage(), e);
            } 
        }

        setLogEnable(true); 
        setOTAStatus(OTA_Cur_Status.OnGoing.getValue());//Set mOTAStatus OnGoing
        if (updateMode == ABUPDATE_USB_MODE || updateMode == ABUPDATE_STREAM_ZIP_MODE) {     
            mPayloadOffset = UpdatePayloadOffset;
            mPayloadSize = UpdatePayloadSize;
            mProperties = parsejsonArrayToStringArray(UpdateProperties);
        }
        mUpdatePath = UpdateZipFile;
        mUpdateMode = updateMode;
        Log.d(TAG, "abHandlerStart: IN mUpdatePath = [" + mUpdatePath + "] mPayloadOffset = [" + mPayloadOffset +
         "] mPayloadSize = [" + mPayloadSize + "] mProperties = [" + mProperties.toString() + 
         "] mUpdateImageVersion = [" + mUpdateImageVersion +"] updateMode = [" + mUpdateModeArray[mUpdateMode] + "]");
        /*
        if (mUpdateMode == ABUPDATE_USB_MODE)
        {
            Log.d(TAG, "abHandlerStart: usb mode");
            if(!copyUSBFileToLocal(mUpdatePropPath, mTxtDest))
            {
                SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
                SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(mErrCopyFile));
                setOTAStatus(OTA_Cur_Status.Idle.getValue());
                return;
            }
            if (!copyUSBFileToLocal(mUpdatePath, mBinDest))
            {
                SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
                SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(mErrCopyFile));
                setOTAStatus(OTA_Cur_Status.Idle.getValue());
                return;
            }
            mUpdatePath = "file://"+mBinDest;
            mUpdatePropPath = mTxtDest;
            Log.d(TAG, "abHandlerStart: usb mode mUpdatePath = [" + mUpdatePath + "] mUpdatePropPath = [" + mUpdatePropPath + "]");
        }*/
        CreateABUpdateThread();
        mABEngine.resetStatus();//reset update_engine status for newer OTA Imgaes
        mABHandler.post(mABUpdateRunnable);

        SendStatusExecution("Start", true, context);
    }

    private void SetUpdateEngineCallback()
    {
        mUpdateEngineCallback = new UpdateEngineCallback() {
            @Override
            public void onStatusUpdate(final int status, final float percent) {
                if(getLogEnable())
                    Log.d(TAG, "onStatusUpdate: IN percent = [" + percent + "] status = [" + status + "]");

                int abpercent =(int)(percent *100+0.5f);

                String percentStr = String.valueOf(abpercent);
                SystemProperties.set(TEMP_ABUPDATE_PERCENT,percentStr);

                int maxValue = UpdateStatus.status.NEED_PERMISSION_TO_UPDATE.getValue();
                String statusMsg = (status >= 0 && status <= maxValue) ? mUpdateStatusMsgArray[status] : "...";
                SystemProperties.set(TEMP_ABUPDATE_STATUS, statusMsg);

                if(status ==  UpdateStatus.status.UPDATED_NEED_REBOOT.getValue())
                {
                    if(getLogEnable())
                        Log.d(TAG, "onStatusUpdate stats value == [" + status +"] str =[" + mUpdateStatusMsgArray[status] + "]");

                    SystemProperties.set(TEMP_ABUPDATE_PERCENT,"-1");
                    return;
                }
                else
                {
                    if(getLogEnable())
                        Log.d(TAG, "onStatusUpdate stats value == [" + status +"] str =[" + mUpdateStatusMsgArray[status] + "]");
                }
            }

            @Override
            public void onPayloadApplicationComplete(final int errorCode) {
                if(getLogEnable())
                    Log.d(TAG, "onPayloadApplicationComplete: IN");

                if (0 != errorCode) {// ErrorCode == 0 success
                    if(getLogEnable())
                        Log.d(TAG, "onPayloadApplicationComplete errorcde = [" + errorCode + "] errMsg = [" + mErrMsgArray[errorCode] + "]" );

                    SystemProperties.set(TEMP_ABUPDATE_ERRORFLAG,"1");
                    SystemProperties.set(TEMP_ABUPDATE_ERRORCODE,String.valueOf(errorCode));
                    if(mOTAStatus == OTA_Cur_Status.OnGoing.getValue() || mOTAStatus == OTA_Cur_Status.Pause.getValue())//if not set idle when finish abupdate (protect)
                        setOTAStatus(OTA_Cur_Status.Idle.getValue());//Set mOTAStatus Idle

                } else {
                    if(getLogEnable())
                        Log.d(TAG, "onPayloadApplicationComplete Success errorcde = " + errorCode);

                    SystemProperties.set(TEMP_ABUPDATE_COMPLETE,"1");
                    SystemProperties.set(ALREADY_UPDATE_COMPLETE_VERSION,mUpdateImageVersion);//Save Update Image Version Propery
                    if(mOTAStatus == OTA_Cur_Status.OnGoing.getValue() || mOTAStatus == OTA_Cur_Status.Pause.getValue())//if not set idle when finish abupdate (protect)
                        setOTAStatus(OTA_Cur_Status.Idle.getValue());//Set mOTAStatus Idle
                }

                SystemProperties.set(TEMP_ABUPDATE_PERCENT,"-1");
                WriteFiles(Min_Save_Size,mSaveOtaPath);//After Update complete or Error, create new 200K files for Android OTA Update rules
                CloseAbHadleThread();
            }
        };
    }

    private boolean checkFreeSpace(String statFsPath, final int checkSize)//Sdcard space must larger than 100 KB
    {
        try {
            StatFs stat = new StatFs(statFsPath);
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            //if( getLogEnable() )
            {
                Log.d(TAG, "checkFreeSpace: blockSize = " + blockSize + " availableBlocks = " + availableBlocks);
                Log.i(TAG, "checkFreeSpace statFsPath = " + statFsPath + " => available = " + ((availableBlocks * blockSize) / KB)
                        + " checkSize = " + checkSize);
            }
            return ((availableBlocks * blockSize)/KB) >= checkSize;
        }catch (Exception e){
            Log.d(TAG,"checkFreeSpace Exception return false");
        }
        return false;
    }


    private void SetNetworkCallback()
    {
        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            
            @Override
            public void onAvailable(Network network) {//Connect Network, but not check internet ready
                super.onAvailable(network);
                Log.d(TAG, "onAvailable Network Connect  ===>> ["+ network+"]");
                try {
                    URLConnection urlConnection = new URL(INTERNET_URL).openConnection();//Check connect to Goolge OK
                    urlConnection.setConnectTimeout(500);
                    urlConnection.connect();
                    SystemProperties.set(SET_NETWORK_IS_READY,TRUE);
                    Log.d(TAG, "onAvailable Network Connection success NetworkReady = [" + SystemProperties.get(SET_NETWORK_IS_READY,FALSE) +"]");
					if(mZipFileDownload != null)
						mZipFileDownload.check_download_failed_retry();
                } catch (Exception e) {
                    SystemProperties.set(SET_NETWORK_IS_READY,FALSE);
                    Log.d(TAG, "onAvailable Network Connection fail NetworkReady = [" + SystemProperties.get(SET_NETWORK_IS_READY,FALSE) +"]");
                }
            }

            @Override
            public void onLost(Network network) {//Disconnect Network
                super.onLost(network);
                Log.d(TAG, "onLost Network DisConnect  ===>> ["+ network+"]");
                SystemProperties.set(SET_NETWORK_IS_READY,FALSE);
                Log.d(TAG, "onLost Network DisConnect  ===>> NetworkReady = [" + SystemProperties.get(SET_NETWORK_IS_READY,FALSE) +"]");
            }
        };
    }

    private void initNetworkInfoLis(){
         if(mConnectivityManager == null)
         {
            mConnectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
         }

         if(mNetworkRequest == null)
         {
             NetworkRequest.Builder builder = new NetworkRequest.Builder(); 
             mNetworkRequest = builder.build(); 
         }

         if( mNetworkCallback == null)
         {
             SystemProperties.set(SET_NETWORK_IS_READY,FALSE);
             SetNetworkCallback();
             mConnectivityManager.requestNetwork(mNetworkRequest, mNetworkCallback);
         }
         
         // wifi led
         NetworkRequest wifiNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                //.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();
         mConnectivityManager.registerNetworkCallback(wifiNetworkRequest, mWifiNetworkCallback);
    }

    private boolean NetworkIsReady()
    {

        Log.d(TAG, "NetworkIsReady => ["+SystemProperties.get(SET_NETWORK_IS_READY,FALSE)+"]");
        if(SystemProperties.get(SET_NETWORK_IS_READY,FALSE).equals(FALSE))
        {
            Log.e(TAG, "Network Connect Fail, Please Check Internet Ready before OTA!");
            return false;
        }

        return true;
    }

    private Boolean copyUSBFileToLocal(String source,String dest)
    {
        boolean checkflag = true;
        Log.d(TAG, "copyUSBFileToLocal: source = [" + source + "] dest = [" + dest + "]");

        /**/
        File sourceFile = new File(source);
        File destFile = new File(dest);

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
            Files.copy(sourceFile.toPath(),destFile.toPath());
            Log.d(TAG, "copyUSBFileToLocal: copy file complete");
        }
        catch (Exception exception)
        {
            checkflag = false;
            Log.d(TAG, "copyUSBFileToLocal: copy file Exception = " + exception);
        }

        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(perm);
            Files.setPosixFilePermissions(destFile.toPath(), permissions);
        } catch (IOException exception) {
            // logger.warning("Can't set permission '" + perm + "' to " +   dir.getPath());
            checkflag = false;
            Log.d(TAG, "copyUSBFileToLocal: change permission Exception = " + exception);
        }

        Log.d(TAG, "copyUSBFileToLocal: checkflag = [" + checkflag + "]");

        return checkflag;

        /*
        try {
            File oldFile = new File(source);

            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                Log.e(TAG, "copyFile:  oldFile not exist, not file or cannot read.");
                return false;
            }

            FileInputStream fileInputStream = new FileInputStream(source);
            FileOutputStream fileOutputStream = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            long size = new File(source).length();
            int byteRead;
            long transferSize = 0;
            int progress = 0;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
                transferSize += byteRead;
                progress = (int) (transferSize * 100 / size);
                Log.d(TAG, "copyFile --- progress=" + progress);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            Log.d(TAG, "copyFile package complete.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
*/
    }

    //Start AB Update
	private void ApplyABUpdate()
    {
		long fileLength = -1;
		/*
        //boolean isComplete = false;
        //get http N97_A1B_72115.bin file size from url
        try {
            URL url = new URL(mUpdatePath);//get payload file
            fileLength = url.openConnection().getContentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "ApplyABUpdate  ===>> fileLength = [" + fileLength + "]");

        PayloadInfo Payinfo = new PayloadInfo();
        //get http N97_A1B_72115.txt file content from url
        try {
            URL url = new URL(mUpdatePropPath);//get payload parameter
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                Payinfo.content.add(inputLine);
            if (in != null) {
                in.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        PayloadInfo Payinfo = new PayloadInfo();
        if (mUpdateMode == 0)
            Payinfo = getPayinfoByInternetMode();
        else if (mUpdateMode == 1)
        {
            Payinfo = getPayinfoByUSBMode();
        }

        String[] keyPairsProp = Payinfo.content.toArray(new String[Payinfo.content.size()]);
        for (int i = 0; i < keyPairsProp.length; i++)
        {
            if ( i == 1)
            {
                String a= keyPairsProp[i].substring(10);
                fileLength = Integer.parseInt(a);
            }
            Log.d(TAG, "get content from url ===>> i = [" + i + "] " + " strings[" + "] = [" + keyPairsProp[i] + "]");
        }
        //Apply AB Update params to Update_Engine

        try
        {
            if (getLogEnable()) Log.d(TAG, "applyPayload enter");
            if (mUpdateMode == ABUPDATE_STREAM_MODE) 
                mABEngine.applyPayload(mUpdatePath, 0, fileLength, keyPairsProp);
            else
                mABEngine.applyPayload(mUpdatePath, mPayloadOffset, mPayloadSize, mProperties);
                
        } catch (android.os.ServiceSpecificException e)
        {
            Log.d(TAG, "update is already success , need to reboot");
        }

        // isComplete = IsApplyPayloadComplete(fileLength,keyPairsProp);
        // if(isComplete)
        // {
        //     Log.d(TAG, "Already complete");
        //     SystemProperties.set(TEMP_ABUPDATE_COMPLETE,"1");
        //     if(mOTAStatus != OTA_Cur_Status.Idle.getValue())//if not set idle when finish abupdate (protect)
        //         setOTAStatus(OTA_Cur_Status.Idle.getValue());//Set mOTAStatus Idle
        // }
    }

    private PayloadInfo getPayinfoByInternetMode()
    {
        PayloadInfo Payinfo = new PayloadInfo();
        //get http N97_A1B_72115.txt file content from url
        try {
            URL url = new URL(mUpdatePropPath);//get payload parameter
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                Payinfo.content.add(inputLine);
            if (in != null) {
                in.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Payinfo;
    }

    private PayloadInfo getPayinfoByUSBMode()
    {
        PayloadInfo Payinfo = new PayloadInfo();
        //get http N97_A1B_72115.txt file content from url
        try {
            FileInputStream fis = new FileInputStream(mUpdatePropPath);
            //URL url = new URL(mUpdatePropPath);//get payload parameter
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                Payinfo.content.add(inputLine);
            if (in != null) {
                in.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Payinfo;
    }
    // private boolean IsApplyPayloadComplete(long fileLength,String[] keyPairsProp)
    // {
    //     try {
    //         if(getLogEnable())
    //             Log.d(TAG, "applyPayload enter");
    //         mABEngine.applyPayload(mUpdatePath, 0, fileLength, keyPairsProp);
    //     } catch (android.os.ServiceSpecificException e) {
    //         Log.d(TAG, "update is already success , need to reboot");
    //         return true;
    //     }
    //     Log.d(TAG, "IsApplyPayloadComplete false");
    //     return false;
    // }

    //protect abupdate status
    private enum OTA_Cur_Status{
       Idle(0),//Stop, Complete, Error
       OnGoing(1),//Start, resume
       Pause(2),//Pause
       ZipFileDownloading(3);

       private int value;
       private OTA_Cur_Status(int value){
           this.value = value;
       }
       private int getValue() {
           return this.value;
       }
    }

    //New Abupdate use new Runnable, close runnable when finsih abupdate
    private void SetABUpdateRunnable()
    {
        mABUpdateRunnable = new Runnable() {
        public void run() {
                if(mUpdateEngineCallback == null)
                {
                    Log.d(TAG, "SetABUpdateRunnable create UpdateEngineCallback");
                    SetUpdateEngineCallback();
                    mABEngine.bind(mUpdateEngineCallback);
                }
                ApplyABUpdate();
            }
        };
    }  

    //Write 200K files for Android OTA Update rules
    private boolean isOTASavedFileExist(File file)
    {
        if (!file.exists()) { 
            return false;
        }
        return true;
    }

    private void WriteFiles(int filesize, String path)
    {
        Log.d(TAG, "writeFile: ====>> Running path =  ["+path+"]");
        try {
            Log.d(TAG, "writeFile: ====>> Running 0 ");

            File file = new File(path);
            if(!isOTASavedFileExist(file))
            {
                file.createNewFile();
            }else{
                return;//if file exist, no need to create new
            }

            FileOutputStream fops = new FileOutputStream(file);
            FileChannel channel = fops.getChannel();
           /* int count = filesize/File_KB;
            for(int i = 0 ; i < count ; i++)
            {
                ByteBuffer buffer = ByteBuffer.allocate(File_KB);
                channel.write(buffer);

            }*/

            ByteBuffer buffer = ByteBuffer.allocate(filesize);
            channel.write(buffer);
            fops.close();

        } catch (IOException e) {
            throw new RuntimeException("Write file wrong : " + e.getMessage(), e);
        } 
        
    }

    private String [] parsejsonArrayToStringArray(JSONArray jsonArray) {
        String[] stringArray =  new String[jsonArray.length()];
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                stringArray[i] = jsonArray.getString(i);
                Log.d(TAG, "parsejsonArrayToStringArray stringArray = [" + i + "] = [" + stringArray[i] + "]");
            }
        
        }   catch (JSONException e) {
            e.printStackTrace();  // 打印異常信息，或進行其他處理
        }
        return stringArray;
    }

    private void setDataFromZip(String otaZipPath) {
        File otaZipFile = new File(otaZipPath);
        boolean payloadFound = false;

        if (!otaZipFile.exists()) {
            Log.e(TAG, "setDataFromZip: otaZipFile not exit");
            return;
        }

        List<String> properties = new ArrayList<>();

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

                if ("payload.bin".equals(name)) {
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
                            properties.add(line);
                        }
                    }
                    Log.d(TAG, "setDataFromZip: properties = " + properties);
                }
                offset += length;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "sameTimestamp: " + e);
        }

        mProperties = properties.toArray(new String[0]);
        if (!payloadFound) {
            Log.e(TAG, "setDataFromZip: Failed to find payload entry in the given package.");
        }
    }
}
