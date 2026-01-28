// Copyright 2021 Google Inc. All Rights Reserved.

package com.prime.globalkeyhandler;

import android.app.DreamManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Looper;
import android.widget.Toast;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.statusbar.IStatusBarService;
import java.util.List;

import android.os.PowerManager;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import java.util.Set;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.os.SystemProperties;
public class GlobalKeyReceiver extends BroadcastReceiver {
    private static final String TAG = "PrimeGlobalKeyReceiver";
    private static final boolean DEBUG = false;

    private static final String EXTRA_BEGAN_FROM_NON_INTERACTIVE =
            "EXTRA_BEGAN_FROM_NON_INTERACTIVE";
    private static final String EXTRA_LAUNCH_INTENT = "launchIntent";
    private static final String INTENT_EXTRA_NO_INPUT_MODE = "no_input_mode";
    private static final String EXTRA_PACKAGE_NAME = "launchPackageName";
    private static final String PACKAGE_NAME_NETFLIX = "com.netflix.ninja";
    private static final String PACKAGE_NAME_YOUTUBE = "com.google.android.youtube.tv";
    private static final String PACKAGE_NAME_DISNEY_PLUS = "com.disney.disneyplus";
	private static final String PACKAGE_NAME_DMG_LAUNCHER = "com.prime.dmg.launcher";
	private static final String DMG_LAUNCHER_PACKAGE = "com.prime.dmg.launcher";
	private static final String DMG_LAUNCHER_HOME_ACTIVITY = "com.prime.dmg.launcher.HomeActivity";
	private static final String DMG_LAUNCHER_EPG_ACTIVITY = "com.prime.dmg.launcher.EPG.EpgActivity";
	private static final String PRIME_BTPAIR_PACKAGE = "com.prime.btpair";
	private static final String PRIME_BTPAIR_HOOKBEGINACTIVITY = "com.prime.btpair.HookBeginActivity";
    private static final String CMP_DISNEY_PLLUS = "com.bamtechmedia.dominguez.main.MainActivity";
    private static final String URI_DISNEY_PLLUS = "market://details?id=com.disney.disneyplus&url=https://www.disneyplus.com/series/obi-wan-kenobi/2JYKcHv9fRJb?distributionPartner=google&external_client_id=205099748685832028";
    private static final String PACKAGE_NAME_HBO_NOW = "com.wbd.stream";
    private static final String CMP_HBO_NOW = "com.hbo.max.HboMaxActivity";
    private static final String URI_HBO_NOW = "market://details?id=com.hbo.hbonow&referrer=atv_launcher&ah=DRqyU2hgS35x7UflL99t/Kijr9o&external_client_id=205099748685832363";
    private static final String PACKAGE_NAME_PRIME_VIDEO = "com.amazon.amazonvideo.livingroom";
    private static final String CMP_PRIME_VIDEO = "com.amazon.ignition.IgnitionActivity";
    private static final String URI_PRIME_VIDEO = "market://details?id=com.amazon.amazonvideo.livingroom";
    private static final String NETFLIX_INTENT = "com.netflix.action.NETFLIX_KEY_START";
    private static final String NETFLIX_PERMISSION = "com.netflix.ninja.permission.NETFLIX_KEY";
    private static final String NETFLIX_KEY_POWER_MODE = "power_on";
	private static final String PACKAGE_NAME_LITV = "com.js.litv.home";
	private static final String LITV_HOME_ACTIVITY = "com.js.litv.home.LiTVHomeActivityV2";
    private static final String PACKAGE_NAME_LITV_CABLE = "com.litv.cable.home";
    private static final String LITV_CABLE_HOME_ACTIVITY = "com.litv.home.LiTVHomeActivityV2";
	private static final String PACKAGE_NAME_FRIDAY = "net.fetnet.fetvod.tv";
	private static final String CMP_FRIDAY = "net.fetnet.fetvod.tv.MainActivity";
	private static final String URI_FRIDAY = "market://details?id=net.fetnet.fetvod.tv";
	private static final String PACKAGE_NAME_CATCHPLAY = "com.catchplay.asiaplay.common.tv";
	private static final String CMP_CATCHPLAY = "com.catchplay.asiaplay.common.tv.MainActivity";
	private static final String URI_CATCHPLAY = "market://details?id=com.catchplay.asiaplay.common.tv";
	

	private static final String ACTION_GLOBAL_BUTTON_DMG = "com.prime.dmg.launcher.GLOBAL_BUTTON";
	private static final String EXTRA_KEY_EVENT = "android.intent.extra.KEY_EVENT";
    private static final String PACKAGE_NAME_HOMEPLUS_TV = "com.prime.homeplus.tv";
    private static final String HOMEPLUS_MAIN_ACTIVITY =
        "com.prime.homeplus.tv.ui.activity.MainActivity";
    private static final String PROP_LAUNCHER_MODULE = "persist.sys.prime.launcher_module";
    private static final String LAUNCHER_MODULE_CNS  = "CNS";
    private static final String CNS_OVERDUE_PAYMENT  = "persist.sys.inspur.cns_overdue_payment";

    private static PowerManager mPowerManager;
    private static PowerManager.WakeLock mWakeLock;
    private static WifiManager mWifiManager;
    private static NetworkInfo mNewNetworkInfo;
    private static NetworkInfo mNetworkInfo;
    private static ConnectivityManager mConnectivityManager;
    private static Handler mHandler = new Handler();
    public static final int MINOR_DEVICE_CLASS_POINTING =
            Integer.parseInt("0000010000000", 2);
    public static final int MINOR_DEVICE_CLASS_JOYSTICK =
            Integer.parseInt("0000000000100", 2);
    public static final int MINOR_DEVICE_CLASS_GAMEPAD =
            Integer.parseInt("0000000001000", 2);
    public static final int MINOR_DEVICE_CLASS_KEYBOARD =
            Integer.parseInt("0000001000000", 2);
    public static final int MINOR_DEVICE_CLASS_REMOTE =
            Integer.parseInt("0000000001100", 2);


    private boolean isActivityVisible(Context context, String packageName, String activityClassName) {
        android.app.ActivityManager am = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<android.app.ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity != null &&
                topActivity.getPackageName().equals(packageName) &&
                topActivity.getClassName().equals(activityClassName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_GLOBAL_BUTTON.equals(intent.getAction())) {
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            final String hookPackage = "com.prime.btpair";
            final String hookActivity = "com.prime.btpair.HookBeginActivity";
            String packageName = DMG_LAUNCHER_PACKAGE;
            boolean isBtPairExist = isActivityVisible(context, hookPackage, hookActivity);

            mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock =  mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmSuspend");
            mWifiManager = context.getSystemService(WifiManager.class);
            mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            mNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            final boolean fromNonInteractive =
                intent.getBooleanExtra(EXTRA_BEGAN_FROM_NON_INTERACTIVE, false);
            Log.d(TAG, "Received KeyEvent: " + event.toString()
                  + ", isBtPairExist: "+isBtPairExist
                  + ", fromNonInteractive: " + fromNonInteractive);
            if (!isTvSetupComplete(context)) {
                if(isBtPairExist && (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_6))
                    ;
                else{
                    Log.i(TAG, "Setup isn't completed. Ignoring KeyEvent: " + event.getKeyCode());
                    return;
                }
            }

            if (event.getAction() != KeyEvent.ACTION_UP) {
                return;
            }

            int overduePayment = SystemProperties.getInt(CNS_OVERDUE_PAYMENT,0);
            if(overduePayment == 1)
                return ;

            SystemProperties.set("persist.sys.prime.global_key", String.valueOf(event.getKeyCode()));

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BUTTON_2:
                    handleKeycode_2(context);
                    break;
                case KeyEvent.KEYCODE_BUTTON_3:
                    launchYoutube(context, fromNonInteractive);
                    break;
                case KeyEvent.KEYCODE_BUTTON_4:
                    launchNetflix(context, fromNonInteractive);
                    break;
                case KeyEvent.KEYCODE_BUTTON_5:
                    launchAppByPackageName(context, getPackageName(context,
                          R.array.disney_plus_package_name, PACKAGE_NAME_DISNEY_PLUS),
                        fromNonInteractive, CMP_DISNEY_PLLUS, URI_DISNEY_PLLUS);					
                    break;
                case KeyEvent.KEYCODE_BUTTON_6:
					launchAppByPackageName(context, getPackageName(context,
						R.array.Friday_package_name, PACKAGE_NAME_FRIDAY),
						fromNonInteractive, CMP_FRIDAY, URI_FRIDAY);
  
                    break;
                case KeyEvent.KEYCODE_BUTTON_7:
                    launchAppByPackageName(context,
                        "com.android.vending",
                        fromNonInteractive, null, null);
 
                    break;
                case KeyEvent.KEYCODE_BUTTON_8:
					launchAppByPackageName(context, getPackageName(context,
						R.array.Friday_package_name, PACKAGE_NAME_CATCHPLAY),
						fromNonInteractive, CMP_CATCHPLAY, URI_CATCHPLAY);					
                    //handleWifiConnected(context);
                    break;
                case KeyEvent.KEYCODE_BUTTON_9:
                    /* New ohsung G20 Karaoke */
                    //toastView(context);
                    break;
                case KeyEvent.KEYCODE_BUTTON_10:
                    //launchAppByPackageName(context, getPackageName(context,
                    //      R.array.prime_video_package_name, PACKAGE_NAME_PRIME_VIDEO),
                    //    fromNonInteractive, CMP_PRIME_VIDEO, URI_PRIME_VIDEO);
                    break;
                case KeyEvent.KEYCODE_BUTTON_11:
                    /* RTK G20 app5 */
                    //toastView(context);
                    break;
                case KeyEvent.KEYCODE_BUTTON_12:
                    /* RTK G20 app6 */
                    //toastView(context);
                    break;
                case KeyEvent.KEYCODE_SETTINGS:
                    //toggleDashboardPanel(context);
                    break;
                case KeyEvent.KEYCODE_GUIDE :
                    if (isCnsLauncher()) {
                        
                        // CNS：走 HomePlus TV 的 EPG
                        Log.d(TAG, "Open HomePlus EPG (CNS)");
                        launchHomeplusEpg(context, fromNonInteractive);
                        packageName = PACKAGE_NAME_HOMEPLUS_TV;
                        
                    } else {
                        //  其他機種：走 DMG Launcher 的 EPG
                        Log.d(TAG, "Open DMG_LAUNCHER_EPG_ACTIVITY ");
                        launchDMG_EPG(context, fromNonInteractive);
                        packageName = DMG_LAUNCHER_PACKAGE;
                        
                    }
                    //setAISREnable();
                    break;
                case KeyEvent.KEYCODE_TV :
					 if (isCnsLauncher()) {
						launchHomeplusTv(context, fromNonInteractive);
					 }else{
						launchDMGLauncher(context, 0);
					 }

					Log.d(TAG, "Open DMG_LAUNCHER_HOME_ACTIVITY");
                    //setAISREnable();
                    break;
                default:
                    break;
            }
			sendKey(context, packageName, event);
            Log.i(TAG, "Unhandled KeyEvent: " + event.getKeyCode());
        }
    }

	private static void sendKey(Context context, String packageName,KeyEvent event){
		Intent launchIntent  = new Intent();
		launchIntent.setPackage(packageName);
		launchIntent.setAction(ACTION_GLOBAL_BUTTON_DMG);
		launchIntent.putExtra(EXTRA_KEY_EVENT, new KeyEvent(event));
		Log.d(TAG, "Send Key event to "+packageName);
		context.sendBroadcastAsUser(launchIntent, UserHandle.ALL);

	}
    private static void toastView(Context context) {
        Toast.makeText(context, "The feature is not yet supported.", Toast.LENGTH_SHORT).show();
    }

    private static ApplicationInfo info = null;
    private static Boolean isPkgInstalled(Context context, String pkgName) {
        try {
            info = context.getPackageManager().getApplicationInfo(pkgName, 0);
            return info != null;
        } catch (NameNotFoundException e) {
            Log.d(TAG, "WARNINGS: APK "+ pkgName + " is not installed, try go to GooglePlay");
            return false;
        }
    }

    private static boolean isTvSetupComplete(Context context) {
        return Settings.Secure.getInt(
            context.getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
    }

    private static boolean isAppExisted(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLeanbackLaunchIntentForPackage(pkgName);

        if (null == intent)
            intent = pm.getLaunchIntentForPackage(pkgName);

        return intent != null;
    }

    private static void launchDMG_EPG(Context context, boolean fromNonInteractive) {
        Intent launchIntent  = new Intent();
		launchIntent.setComponent(new ComponentName(DMG_LAUNCHER_PACKAGE, DMG_LAUNCHER_EPG_ACTIVITY));
        //launchIntent.putExtra("power_on", fromNonInteractive);
        //launchIntent.putExtra("yt_remote_button", true);
		//Log.d(TAG, "launchDMG_EPG package:"+DMG_LAUNCHER_PACKAGE+ " DMG_LAUNCHER_EPG_ACTIVITY:"+DMG_LAUNCHER_EPG_ACTIVITY);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        DreamManager dreamer = context.getSystemService(DreamManager.class);
        if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
            }
        if(dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }
        context.startActivity(launchIntent);
    }
    private static void launchHomeplusEpg(Context context, boolean fromNonInteractive) {
        Intent intent = new Intent();
        intent.setClassName(PACKAGE_NAME_HOMEPLUS_TV, HOMEPLUS_MAIN_ACTIVITY);
    
        // 告訴 MainActivity：這次是 EPG 模式
        intent.putExtra("Action", "EPG");
    
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    
        DreamManager dreamer = context.getSystemService(DreamManager.class);
        if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
        }
        if (dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }
    
        Log.d(TAG, "launchHomeplusEpg: class=" + HOMEPLUS_MAIN_ACTIVITY + " extra Action=EPG");
        context.startActivity(intent);
    }

	private static void launchHomeplusTv(Context context, boolean fromNonInteractive) {
        Intent intent = new Intent();
        intent.setClassName(PACKAGE_NAME_HOMEPLUS_TV, HOMEPLUS_MAIN_ACTIVITY);
    
        // 告訴 MainActivity：這次是 EPG 模式
    
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    
        DreamManager dreamer = context.getSystemService(DreamManager.class);
        if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
        }
        if (dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }
    
        Log.d(TAG, "launchHomeplusEpg: class=" + HOMEPLUS_MAIN_ACTIVITY + " extra Action=EPG");
        context.startActivity(intent);
    }
    

    
    private static void launchLiTV(Context context, boolean fromNonInteractive) {
        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLeanbackLaunchIntentForPackage(PACKAGE_NAME_LITV_CABLE);

        if (null == launchIntent)
            launchIntent = pm.getLaunchIntentForPackage(PACKAGE_NAME_LITV_CABLE);

        // if (null == launchIntent) {
        //     launchIntent = new Intent();
        //     launchIntent.setPackage(PACKAGE_NAME_LITV_CABLE);
        //     //launchIntent.setPackage(PACKAGE_NAME_LITV);
        //     //launchIntent.setComponent(new ComponentName(PACKAGE_NAME_LITV, LITV_HOME_ACTIVITY));
        //     //launchIntent.putExtra("power_on", fromNonInteractive);
        //     //launchIntent.putExtra("yt_remote_button", true);
        //     launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // }

        DreamManager dreamer = context.getSystemService(DreamManager.class);
        if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
        }
        if(dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }

        if (isAppExisted(context, PACKAGE_NAME_LITV_CABLE))
            context.startActivity(launchIntent);
        else
            installApp(context, PACKAGE_NAME_LITV_CABLE);
    }

	//screen_type = 0 , go to Live TV full screen
	//screen_type = 1 , go to 熱門 screen
    private static void launchDMGLauncher(Context context, int screen_type) {
        Intent launchIntent  = new Intent();
		launchIntent.setComponent(new ComponentName(PACKAGE_NAME_DMG_LAUNCHER, DMG_LAUNCHER_HOME_ACTIVITY));
        launchIntent.putExtra("screen_type", screen_type);
        //launchIntent.putExtra("yt_remote_button", true);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        DreamManager dreamer = context.getSystemService(DreamManager.class);
        if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
            }
        if(dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }
        context.startActivity(launchIntent);
    }


    private static void launchYoutube(Context context, boolean fromNonInteractive) {
        Intent launchIntent  = new Intent();
        ComponentName comp = new ComponentName("com.google.android.youtube.tv", "com.google.android.apps.youtube.tv.activity.MainActivity");
        launchIntent.setPackage(PACKAGE_NAME_YOUTUBE);
        launchIntent.putExtra("power_on", fromNonInteractive);
        launchIntent.putExtra("yt_remote_button", true);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            | Intent.FLAG_RECEIVER_FOREGROUND);

        DreamManager dreamer = context.getSystemService(DreamManager.class);
            if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
            }
        if(dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }
        if (isAppExisted(context, launchIntent.getPackage()))
            context.startActivity(launchIntent);
        else
            installApp(context, launchIntent.getPackage());
    }

    private static void launchNetflix(Context context, boolean fromNonInteractive) {
        Intent launchIntent  = new Intent(NETFLIX_INTENT);
        launchIntent.setPackage(PACKAGE_NAME_NETFLIX);
        launchIntent.putExtra(NETFLIX_KEY_POWER_MODE, fromNonInteractive);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            | Intent.FLAG_RECEIVER_FOREGROUND);

        DreamManager dreamer = context.getSystemService(DreamManager.class);
            if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
            }
        if(dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }
        //if (isPkgInstalled(context, launchIntent.getPackage()))
        if (isAppExisted(context, launchIntent.getPackage()))
            context.startActivity(launchIntent);
        else
            installApp(context, launchIntent.getPackage());
    }

    private static void launchAppByPackageName(Context context, String packageName,
        boolean fromNonInteractive, String cmpName, String uriName) {
        Intent newIntent = new Intent();
        if (isPkgInstalled(context, packageName)) {
            ComponentName comp = new ComponentName(packageName, cmpName);
            newIntent.setAction("android.intent.action.VIEW");
            if (packageName.equals(PACKAGE_NAME_PRIME_VIDEO)) {
                newIntent.setAction("com.amazon.amazonvideo.livingroom.AMAZON_BUTTON");
            }
            newIntent.setComponent(comp);
            newIntent.putExtra("power_on", fromNonInteractive);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        } else {
            ComponentName comp = new ComponentName("com.android.vending", "com.google.android.finsky.inlinedetails.activities.tv.TvMarketDeepLinkHandlerActivity");
            newIntent.setAction("android.intent.action.VIEW");
            newIntent.setData(Uri.parse(uriName));
            newIntent.setComponent(comp);
            newIntent.putExtra("power_on", fromNonInteractive);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }

        DreamManager dreamer = context.getSystemService(DreamManager.class);
        if (DEBUG) {
            Log.d(TAG, "Show the isInScreenSaver: " + dreamer.isDreaming());
        }
        if(dreamer.isDreaming()) {
            Log.d(TAG,"[NTSDREAM] stop dream");
            dreamer.stopDream();
        }
        context.startActivity(newIntent);
    }

    private static void installApp(Context context, String pkgName) {
        Intent intent = getPlayStoreIntent(pkgName);
        Log.i(TAG, "installApp: [pkgName] " + pkgName);
        context.startActivity(intent);
    }

    private static Intent getPlayStoreIntent(String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        intent.setPackage("com.android.vending");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static void toggleDashboardPanel(Context context) {
        IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(
            ServiceManager.checkService(Context.STATUS_BAR_SERVICE));
        try {
            if (statusBarService != null) {
                statusBarService.togglePanel();
            } else {
                Log.e(TAG, "Failed to get statusBarService");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get statusBarService: ", e);
        }
    }

    private static final Runnable reSetWifiRunnable = new Runnable(){
        public void run (){
            mNewNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mNewNetworkInfo.isConnected()) {
                mWakeLock.release();
            } else {
                mWifiManager.setWifiEnabled(false);
                mWifiManager.setWifiEnabled(true);
                Log.d(TAG, "Reconnect...");
                mHandler.postDelayed(this,10000);
            }
        }
    };

    private static void handleWifiConnected(Context context) {
        if (!mNetworkInfo.isConnected() && !mWifiManager.getConfiguredNetworks().isEmpty()) {
            Log.d(TAG, "Check network connection.");
            mWakeLock.acquire();
            mWifiManager.reconnect();
            mHandler.postDelayed(reSetWifiRunnable, 4000);
        }
    }

    private static String getPackageName(Context context, int res, String defaultPackageName) {
        String[] packageNames = context.getResources().getStringArray(res);
        for (int i = 0; i < packageNames.length; i++) {
            if (context.getPackageManager()
                    .getLeanbackLaunchIntentForPackage(packageNames[i]) != null) {
                return packageNames[i];
            }
        }
        return defaultPackageName;
    }
    public static void handleKeycode_2(Context context) {
        if (hasRemoteControl())
            return;
        //PackageManager packageManager = context.getPackageManager();
        //Intent intent = new Intent("com.google.android.tvsetup.app.REPAIR_REMOTE");
        //List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        //boolean isIntentSafe = activities.size() > 0;
        //if (!isIntentSafe)
        //{
        //    intent = new Intent("com.rtk.partnerinterface.action.RCU_WARNNING");
        //}
		Intent newIntent = new Intent();
		newIntent.setComponent(new ComponentName(PRIME_BTPAIR_PACKAGE, PRIME_BTPAIR_HOOKBEGINACTIVITY));
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }
    public static boolean isRemoteControl (BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (DEBUG) Log.d(TAG,"isRemoteControl device="+device+" name="+device.getName()
            +" getDeviceClass="+bluetoothClass.getDeviceClass()
            +" devicetype="+device.getType());
        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL &&
            BluetoothDevice.DEVICE_TYPE_LE == device.getType() &&
            (((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_REMOTE)!= 0)
                || ((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_KEYBOARD)!= 0))) {
            Log.d(TAG,"isRemoteControl device="+device+" is remote control");
            return true;
        }
        return false;
    }
    public static boolean hasRemoteControl () {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            final Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
            if (bondedDevices != null) {
                for (BluetoothDevice device : bondedDevices) {
                    if (isRemoteControl(device)) {
                        Log.d(TAG, "Still has remote control device="+device);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private static boolean isCnsLauncher() {
        String module = SystemProperties.get(PROP_LAUNCHER_MODULE, "");
        Log.d(TAG, "launcher_module = " + module);
        return LAUNCHER_MODULE_CNS.equals(module);
    }
    
}
