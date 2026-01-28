package com.prime.android.tv.otaservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.SystemProperties;
import android.os.Message;
import android.os.Handler;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.json.JSONObject;
import org.json.JSONArray;

public class ABUpdateReceiver extends BroadcastReceiver {
    private final String TAG = "ABUpdateReceiver";

    private boolean OTA_DEBUG = true ;
    //private DownloadService mDownloadService = DownloadService.getInstance();// new DownloadService();
    private ConnectivityManager mConnectivityManager = null;
    private int mOriginRedLed = 0;
    private int mOriginGreenLed = 1;

    //action
    public static final String ABUPDATE_BROADCAST_START = "com.prime.android.tv.otaservice.abupdate.start";
    public static final String ABUPDATE_BROADCAST_STOP = "com.prime.android.tv.otaservice.abupdate.stop";
    public static final String ABUPDATE_BROADCAST_PAUSE = "com.prime.android.tv.otaservice.abupdate.pause";
    public static final String ABUPDATE_BROADCAST_RESUME = "com.prime.android.tv.otaservice.abupdate.resume";
    public static final String ABUPDATE_BROADCAST_REGISTER_CALLER = "com.prime.android.tv.otaservice.abupdate.register.caller";
    public static final String ABUPDATE_BROADCAST_ERROR = "com.prime.android.tv.otaservice.abupdate.error";//send error code and error msg
    public static final String ABUPDATE_BROADCAST_STATUS = "com.prime.android.tv.otaservice.abupdate.status";//send error code and error msg
    public static final String ABUPDATE_BROADCAST_COMPLETE = "com.prime.android.tv.otaservice.abupdate.complete";//send update complete
    public static final String NETWORK_BROADCAST_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";//Network Changed
    public static final String ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE = "com.prime.android.tv.otaservice.download.zip.file";

    //parameter
    private final String ABUPDATE_BROADCAST_ERROR_CODE = "prime.abupdate.errCode";
    private final String ABUPDATE_BROADCAST_ERROR_MSG = "prime.abupdate.errStr";
    private final String ABUPDATE_BROADCAST_UPDATE_PROGRESS = "prime.abupdate.progress";
    private final String ABUPDATE_BROADCAST_UPDATE_CALLER = "prime.abupdate.caller";
	private final String ABUPDATE_BROADCAST_UPDATE_BIN_URL = "prime.abupdate.url.bin";
    private final String ABUPDATE_BROADCAST_UPDATE_PARAM_URL = "prime.abupdate.url.txt";
    private final String ABUPDATE_BROADCAST_UPDATE_IMAGE_VERSION = "prime.abupdate.image.version";
    private final String ABUPDATE_BROADCAST_UPDATE_MODE = "prime.abupdate.MODE";
    private final String ABUPDATE_BROADCAST_UPDATE_ZIP_URL = "prime.abupdate.url.zip";
    private final String ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE = "prime.abupdate.size.zip";
    private final String ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET = "prime.abupdate.offset.zip";
    private final String ABUPDATE_BROADCAST_UPDATE_PROPERTIES = "prime.abupdate.properties.zip";

    public static final String TEST_ABUPDATE_BROADCAST_START = "test.com.prime.android.tv.otaservice.abupdate.start";
    public static final String TEST_ABUPDATE_BROADCAST_STOP = "test.com.prime.android.tv.otaservice.abupdate.stop";
    public static final String TEST_ABUPDATE_BROADCAST_PAUSE = "test.com.prime.android.tv.otaservice.abupdate.pause";
    public static final String TEST_ABUPDATE_BROADCAST_RESUME = "test.com.prime.android.tv.otaservice.abupdate.resume";

    private final String SET_ORIGIN_RED_LED = "persist.sys.abupdate.led.red";
    private final String SET_ORIGIN_GREEN_LED = "persist.sys.abupdate.led.green";
    private final String NETWORK_ENABLE = "persist.sys.network.enable";
    private final String ALREADY_UPDATE_COMPLETE_VERSION = "persist.sys.abupdate.complete.version";

    private final String BROADCAST_DOWNLOAD_ZIP_URL = "prime.download.zip.url";
    private final String BROADCAST_DOWNLOAD_ZIP_MD5 = "prime.download.zip.md5";

    private final int ABUPDATE_STREAM_MODE = 0;
    private final int ABUPDATE_STREAM_ZIP_MODE = 1;
    private final int ABUPDATE_USB_MODE = 2;
/* adb send broadcast for test
url:shall check the newest download url, at lease newer than test version; otherwise will show update error => Install Device Open Error
adb shell am broadcast -a test.com.prime.android.tv.otaservice.abupdate.start --es prime.abupdate.url.bin https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.bin" --es prime.abupdate.url.txt https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.txt" -f 0x01000000
adb shell am broadcast -a test.com.prime.android.tv.otaservice.abupdate.stop -f 0x01000000
adb shell am broadcast -a test.com.prime.android.tv.otaservice.abupdate.pause -f 0x01000000
adb shell am broadcast -a test.com.prime.android.tv.otaservice.abupdate.resume -f 0x01000000
*/

    public Handler mHandler = null;
    public void registerHandler(Handler h) {
        mHandler = h;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if(OTA_DEBUG)
            Log.d(TAG, "ABUpdateReceiver: IN ===>>> action = ["+action+"]");
        if(action.equals(ABUPDATE_BROADCAST_START))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_START: IN");

			String UpdateBinUrl = intent.getStringExtra(ABUPDATE_BROADCAST_UPDATE_BIN_URL);//N97_A1B_72115.bin
			String UpdateParamUrl = intent.getStringExtra(ABUPDATE_BROADCAST_UPDATE_PARAM_URL);//N97_A1B_72115.txt
            String UpdateImageVersion = intent.getStringExtra(ABUPDATE_BROADCAST_UPDATE_IMAGE_VERSION);//VERSION
			int updateMode = intent.getIntExtra(ABUPDATE_BROADCAST_UPDATE_MODE, ABUPDATE_STREAM_MODE);
			if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_START: ABupdate Start UpdateBinUrl = ["+UpdateBinUrl+"] UpdateParamUrl = ["+UpdateParamUrl+
                "] UpdateImageVersion = [" + UpdateImageVersion +"] updateMode = [" + updateMode + "]");
            
            String UpdateZipFile = intent.getStringExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_URL);
            long UpdatePayloadOffset = intent.getLongExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET, 0);
            long UpdatePayloadSize = intent.getLongExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE, 0);
            String [] UpdateProperties = intent.getStringArrayExtra(ABUPDATE_BROADCAST_UPDATE_PROPERTIES);

            Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_START: ABupdate Start UpdateZipFile= ["+UpdateZipFile+"] UpdatePayloadOffset = ["+UpdatePayloadOffset+
                "] UpdatePayloadSize = ["+ UpdatePayloadSize + "] UpdateProperties = ["+ UpdateProperties +
                "] UpdateImageVersion = [" + UpdateImageVersion +"] updateMode = [" + updateMode + "]");

            /*if( UpdateBinUrl == null || UpdateParamUrl == null)//Add protect because prime not return url string when not get correct url, result in crash!!!
            {
                Log.e(TAG, "Please check the correct url before starting OTA!!");
                return;
                //UpdateBinUrl = "null";
                //UpdateParamUrl = "null";
                //UpdateBinUrl = "https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.bin";
                //UpdateParamUrl = "https://atvgwlab.kbro.tv/OTA_file/CDN97_A1B_72115_1.14/N97_A1B_72115.txt";
            }*/
            // if(mDownloadService != null)
            //     Log.d(TAG,"mDownloadService => Instance ==>> ["+ mDownloadService + "]");
            if(UpdateImageVersion == null)
            {
                Log.d(TAG,"for compatibility set UpdateImageVersion to null to start update with old trigger approach");
                UpdateImageVersion = "null";
            }

            // mDownloadService.abHandlerStart(UpdateBinUrl,UpdateParamUrl,UpdateImageVersion, updateMode, context);
            try {
                JSONObject obj = new JSONObject();
                if (updateMode == ABUPDATE_USB_MODE || updateMode == ABUPDATE_STREAM_MODE) {
                    obj.put("UpdateBinUrl", UpdateBinUrl);
                    obj.put("UpdateParamUrl", UpdateParamUrl);
                    obj.put("UpdatePayloadOffset", UpdatePayloadOffset);
                    obj.put("UpdatePayloadSize", UpdatePayloadSize);
                    obj.put("UpdateProperties", new JSONArray(UpdateProperties));
                } else if (updateMode == ABUPDATE_STREAM_ZIP_MODE) {
                    obj.put("UpdatePayloadOffset", UpdatePayloadOffset);
                    obj.put("UpdatePayloadSize", UpdatePayloadSize);
                    obj.put("UpdateProperties", new JSONArray(UpdateProperties));
                }
                obj.put("UpdateZipFile", UpdateZipFile);
                obj.put("UpdateImageVersion", UpdateImageVersion);
                obj.put("updateMode", updateMode);
                Message msg = new Message();
                msg.what = DownloadService.MSG_AB_HANDLER_START;
                msg.obj = obj;
                mHandler.sendMessage(msg);
            }
            catch (Exception e) {
                Log.d(TAG, "ABUPDATE_BROADCAST_START , error = " + e.getMessage());
            }
        }
        else if(action.equals(ABUPDATE_BROADCAST_STOP))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_STOP:");

            // mDownloadService.setLogEnable(false);
            // mDownloadService.ABUpdateStop(context);
            Message msg = new Message();
            msg.what = DownloadService.MSG_AB_UPDATE_STOP;
            mHandler.sendMessage(msg);
        }
        else if(action.equals(ABUPDATE_BROADCAST_PAUSE))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_PAUSE:");

            // mDownloadService.ABUpdatePause(context);
            Message msg = new Message();
            msg.what = DownloadService.MSG_AB_UPDATE_PAUSE;
            mHandler.sendMessage(msg);
        }
        else if(action.equals(ABUPDATE_BROADCAST_RESUME))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_RESUME:");

            // mDownloadService.ABUpdateResume(context);
            Message msg = new Message();
            msg.what = DownloadService.MSG_AB_UPDATE_RESUME;
            mHandler.sendMessage(msg);
        }
        else if (action.equals(ABUPDATE_BROADCAST_REGISTER_CALLER))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "11111 ABUpdateReceiver ABUPDATE_BROADCAST_REGISTER_CALLER:");

            String callerPackageName = intent.getStringExtra(ABUPDATE_BROADCAST_UPDATE_CALLER);

            Log.d(TAG, "11111 ABUpdateReceiver ABUPDATE_BROADCAST_REGISTER_CALLER: Caller Package Name = " +
                    "[" + callerPackageName + "]");
            // mDownloadService.registerCallerPackageName(callerPackageName, context);
            Message msg = new Message();
            msg.what = DownloadService.MSG_REGISTER_CALLER;
            msg.obj = callerPackageName;
            mHandler.sendMessage(msg);
        }
        else if ( action.equals(ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE:");

            String DownloadZipFileUrl = intent.getStringExtra(BROADCAST_DOWNLOAD_ZIP_URL);
            String DownloadZipFileUMd5 = intent.getStringExtra(BROADCAST_DOWNLOAD_ZIP_MD5);
            Log.d(TAG, "ABUpdateReceiver DownloadZipFile Url: " + DownloadZipFileUrl + " Md5: " + DownloadZipFileUMd5);

            try {
                JSONObject obj = new JSONObject();
                obj.put("DownloadZipUrl", DownloadZipFileUrl);
                obj.put("DownloadZipMd5", DownloadZipFileUMd5);

                Message msg = new Message();
                msg.what = DownloadService.MSG_DOWNLOAD_ZIP;
                msg.obj = obj;
                mHandler.sendMessage(msg);
            }
            catch (Exception e) {
                Log.d(TAG, "ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE , error = " + e.getMessage());
            }
        }
		else if( action.toLowerCase().equals(Intent.ACTION_SCREEN_ON.toLowerCase()) )//Screen On Set Green Led
		{
			if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ACTION_SCREEN_ON:");
			
			//mDownloadService.SetDefaultLedValues(0,1);
			//SystemProperties.set(SET_ORIGIN_RED_LED,"0");
			//SystemProperties.set(SET_ORIGIN_GREEN_LED,"1");
			mOriginRedLed = 0;
			mOriginGreenLed = 1;
            Message msg = new Message();
            msg.what = DownloadService.MSG_SET_GREEN_LED;
            //msg.obj = callerPackageName;
            mHandler.sendMessage(msg);			
			//mDownloadService.setGreenLed(1);
			//mDownloadService.setRedLed(0);
		}
		else if( action.toLowerCase().equals(Intent.ACTION_SCREEN_OFF.toLowerCase()) )//Screen Off Set Red Led
		{
			if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ACTION_SCREEN_OFF:");
			
			//mDownloadService.SetDefaultLedValues(1,0);
			//SystemProperties.set(SET_ORIGIN_RED_LED,"1");
			//SystemProperties.set(SET_ORIGIN_GREEN_LED,"0");
			mOriginRedLed = 1;
			mOriginGreenLed = 0;
            Message msg = new Message();
            msg.what = DownloadService.MSG_SET_RED_LED;
            //msg.obj = callerPackageName;
            mHandler.sendMessage(msg);				
			//mDownloadService.setGreenLed(0);
			//mDownloadService.setRedLed(1);			
		}/*else if(action.toLowerCase().equals(Intent.NETWORK_BROADCAST_CHANGED.toLowerCase()))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver network changed:");

            if(mConnectivityManager == null)
            {
                mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                mConnectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        Log.i(TAG, "scoty test onAvailable: "+network);
                    }

                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        Log.i(TAG, "scoty test oonLosing: "+network);
                    }

                    @Override
                    public void onLost(Network network) {
                        super.onLost(network);
                        Log.i(TAG, "scoty test oonLost: "+network);
                    }

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                        Log.i(TAG, "scoty test oonUnavailable: ");
                    }

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
                        Log.i(TAG, "scoty test oonCapabilitiesChanged: "+network);
                    }

                    @Override
                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                        super.onLinkPropertiesChanged(network, linkProperties);
                        Log.i(TAG, "scoty test oonLinkPropertiesChanged: "+network);
                    }
                });
            }
        }*/
       /* else if(action.equals(ABUPDATE_BROADCAST_ERROR))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_ERROR: close Log");

            mDownloadService.setLogEnable(false);
        }
        else if(action.equals(ABUPDATE_BROADCAST_COMPLETE))
        {
            if(OTA_DEBUG)
                Log.d(TAG, "ABUpdateReceiver ABUPDATE_BROADCAST_COMPLETE: close Log");

            mDownloadService.setLogEnable(false);
        }*/

    }

    public int getOriginRedLed() {
        return mOriginRedLed;
    }

    public int getOriginGreenLed() {
        return mOriginGreenLed;
    }
}

