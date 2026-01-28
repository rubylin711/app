package com.prime.android.SystemAPP;


import android.app.LocaleManager;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.LocaleList;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.UserHandle;
import android.service.oemlock.OemLockManager;
import android.service.persistentdata.PersistentDataBlockManager;


import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

import com.realtek.hardware.RtkHDMIManager3;

public class PrimeSystemApp{
	static final String TAG = "PrimeSystemApp";

	public static void trigger_ota_to_recovery(Context context , String filePath){
		File updateFile = new File(filePath);
		try {
			// RecoverySystem.installPackage(mContext, updateFile);  // will reboot
			//RecoverySystem RecoverySystem = null;
			RecoverySystem.scheduleUpdateOnBoot(context, updateFile);
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			pm.reboot(null) ;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			//Toast.makeText(mContext, "------failed-------", 1);
		}
	}

	public static void format_hdd(Context context){
		StorageManager mstorageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
		for(VolumeInfo volume : mstorageManager.getVolumes()){
			if(volume.type == VolumeInfo.TYPE_PUBLIC){
				Log.d(TAG, volume.toString());
				mstorageManager.partitionPublic(volume.getDiskId());
				//mstorageManager.mount(volume.id);
			}
		}
	}

	public static void set_hdcp_level(int level){
		getHDMIManager().setHDCPEnable(level);
	}

	public static void set_wake_lock(int value){
		Log.d(TAG, "set_wake_lock ["+value+"]");
		getHDMIManager().setWakelock(value);
	}

	private static RtkHDMIManager3 getHDMIManager() {
        return RtkHDMIManager3.getHDMIManager();
    }


	public static void performFactoryReset(Context context) {
		final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager)
				context.getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);

		if (shouldWipePersistentDataBlock(context, pdbManager)) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					pdbManager.wipe();
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					doMainClear(context);
				}
			}.execute();
		} else {
			doMainClear(context);
		}
	}

	private static boolean shouldWipePersistentDataBlock(Context context, PersistentDataBlockManager pdbManager) {
		if (pdbManager == null) {
			return false;
		}
		// If OEM unlock is allowed, the persistent data block will be wiped during FR.
		// If disabled, it will be wiped here instead.
		if (((OemLockManager) context.getSystemService(Context.OEM_LOCK_SERVICE))
				.isOemUnlockAllowed()) {
			return false;
		}
		return true;
	}

	private static void doMainClear(Context context) {
		if (context == null) {
			return;
		}
		Intent resetIntent = new Intent(Intent.ACTION_FACTORY_RESET);
		resetIntent.setPackage("android");
		resetIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		resetIntent.putExtra(Intent.EXTRA_REASON, "ResetConfirmFragment");
		context.sendBroadcastAsUser(resetIntent, UserHandle.SYSTEM);
	}		

    public static void set_Ethernet_static_ip(Context context, String ip, int netMask_prefixLength, String gateway, String dns){
		final String ETH_INTERFACE_NAME = "eth0"; 

		try {
				// 1. 準備 IP/網路資訊
				InetAddress ipAddr = InetAddress.getByName(ip);
				LinkAddress linkAddress = new LinkAddress(ipAddr, netMask_prefixLength);
				InetAddress gatewayAddr = InetAddress.getByName(gateway);
				InetAddress dnsAddr = InetAddress.getByName(dns);

	// 2. 使用反射獲取 EthernetManager
			Object ethernetManager = context.getSystemService(Context.ETHERNET_SERVICE);

			if (ethernetManager == null) {
				Log.e(TAG, "EthernetManager is null or service unavailable.");
				return;
			}

			// --- 3. 建立 StaticIpConfiguration 並設定公共欄位 ---
			Class<?> staticIpConfigClass = Class.forName("android.net.StaticIpConfiguration");
			Object staticIpConfig = staticIpConfigClass.newInstance();

			// 🚨 修正點 1: 使用反射直接設定公共欄位 'ipAddress'
			// 嘗試設定 ipAddress 欄位
			java.lang.reflect.Field ipAddressField = staticIpConfigClass.getField("ipAddress");
			ipAddressField.set(staticIpConfig, linkAddress);

			// 🚨 修正點 2: 使用反射直接設定公共欄位 'gateway'
			// 嘗試設定 gateway 欄位
			java.lang.reflect.Field gatewayField = staticIpConfigClass.getField("gateway");
			gatewayField.set(staticIpConfig, gatewayAddr);

			// 🚨 修正點 3: 設置 DNS (通常為 List<InetAddress> 欄位 'dnsServers')
			java.lang.reflect.Field dnsServersField = staticIpConfigClass.getField("dnsServers");
			@SuppressWarnings("unchecked")
			List<InetAddress> dnsServers = (List<InetAddress>) dnsServersField.get(staticIpConfig);
			dnsServers.add(dnsAddr);

			// --- 4. 建立 IpConfiguration (反射呼叫建構子) ---
			Class<?> ipAssignmentClass = Class.forName("android.net.IpConfiguration$IpAssignment");
			Object ipAssignmentStatic = Enum.valueOf((Class<Enum>) ipAssignmentClass, "STATIC");

			Class<?> proxySettingsClass = Class.forName("android.net.IpConfiguration$ProxySettings");
			Object proxySettingsNone = Enum.valueOf((Class<Enum>) proxySettingsClass, "NONE");

			// 尋找符合參數列表的建構子
			Class<?> ipConfigurationClass = Class.forName("android.net.IpConfiguration");
			Constructor<?> constructor = ipConfigurationClass.getConstructor(
				ipAssignmentClass,
				proxySettingsClass,
				staticIpConfigClass,
				Class.forName("android.net.ProxyInfo")
			);
			
			// 實例化 IpConfiguration
			Object ipConfiguration = constructor.newInstance(
				ipAssignmentStatic,
				proxySettingsNone,
				staticIpConfig,
				null // ProxyInfo 參數為 null
			);

			// --- 5. 應用配置 (setConfiguration) ---
			Class<?> ethernetManagerClass = Class.forName("android.net.EthernetManager");
			// 🚨 修正點 4: 尋找接受 (String, IpConfiguration) 參數的方法
        	Method setConfigurationMethod = ethernetManagerClass.getMethod("setConfiguration", String.class, ipConfigurationClass);

			// 執行設定！
			// 傳入介面名稱 (String) 和配置物件 (IpConfiguration)
			setConfigurationMethod.invoke(ethernetManager, ETH_INTERFACE_NAME, ipConfiguration);

			Log.d(TAG, "Static IP saved successfully: " + ip + "/" + netMask_prefixLength);

		} catch (UnknownHostException e) {
			Log.e(TAG, "Invalid IP address format", e);
		} catch (Exception e) {
			// 捕獲所有反射異常 (NoSuchMethodException, IllegalAccessException, etc.)
			Log.e(TAG, "Error setting static IP using reflection: " + e.getMessage(), e);
		}   
    }	

	public static void set_Ethernet_dhcp(Context context){
		
		// ⚠️ 假定 Ethernet 介面名稱與靜態 IP 設定一致
		final String ETH_INTERFACE_NAME = "eth0"; 

		try {
			// 1. 使用反射獲取 EthernetManager
			Object ethernetManager = context.getSystemService(Context.ETHERNET_SERVICE);

			if (ethernetManager == null) {
				Log.e(TAG, "EthernetManager is null or service unavailable.");
				return;
			}
			
			// --- 2. 建立 IpConfiguration (DHCP 模式) ---
			Class<?> ipAssignmentClass = Class.forName("android.net.IpConfiguration$IpAssignment");
			// 獲取 DHCP 列舉值
			Object ipAssignmentDhcp = Enum.valueOf((Class<Enum>) ipAssignmentClass, "DHCP");

			Class<?> proxySettingsClass = Class.forName("android.net.IpConfiguration$ProxySettings");
			Object proxySettingsNone = Enum.valueOf((Class<Enum>) proxySettingsClass, "NONE");

			// 尋找符合參數列表的建構子
			Class<?> ipConfigurationClass = Class.forName("android.net.IpConfiguration");
			// 參數列表：(IpAssignment, ProxySettings, StaticIpConfiguration, ProxyInfo)
			Constructor<?> constructor = ipConfigurationClass.getConstructor(
				ipAssignmentClass,
				proxySettingsClass,
				Class.forName("android.net.StaticIpConfiguration"), // 即使是 DHCP，建構子仍需要這個類型
				Class.forName("android.net.ProxyInfo")
			);
			
			// 實例化 IpConfiguration
			// 關鍵：StaticIpConfiguration 參數傳入 null
			Object ipConfiguration = constructor.newInstance(
				ipAssignmentDhcp,
				proxySettingsNone,
				null, // StaticIpConfiguration 傳 null
				null // ProxyInfo 參數為 null
			);

			// --- 3. 應用配置 ( setConfiguration(String, IpConfiguration) ) ---
			Class<?> ethernetManagerClass = Class.forName("android.net.EthernetManager");
			
			// 尋找接受 (String, IpConfiguration) 參數的方法
			Method setConfigurationMethod = ethernetManagerClass.getMethod("setConfiguration", String.class, ipConfigurationClass);
			
			// 執行設定！
			setConfigurationMethod.invoke(ethernetManager, ETH_INTERFACE_NAME, ipConfiguration);

			Log.d(TAG, "DHCP configuration saved successfully for " + ETH_INTERFACE_NAME);

		} catch (java.lang.reflect.InvocationTargetException e) {
			// 如果底層方法執行失敗，打印底層異常
			Log.e(TAG, "Configuration invocation failed: " + e.getTargetException().getMessage(), e.getTargetException());
		} catch (Exception e) {
			// 捕獲所有反射異常
			Log.e(TAG, "Error setting DHCP using reflection: " + e.getMessage(), e);
		}
	}	

	public static List<TvContentRating> getRatings(Context context){
		TvInputManager mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
		return mTvInputManager.getBlockedRatings();
	}

	public static void set_enable(Context context ,boolean enable){
		TvInputManager mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
		mTvInputManager.setParentalControlsEnabled(enable);
	}

	public static void remove_all_rattings(Context context){
		TvInputManager mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
		List<TvContentRating> ratings = mTvInputManager.getBlockedRatings();

		for(TvContentRating r : ratings){
			mTvInputManager.removeBlockedRating(r);
		}

	}

	public static void add_rating(Context context, TvContentRating rating){
		TvInputManager mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
		mTvInputManager.addBlockedRating(rating);
	}

	public static void SetSystemLanguage(Context context,String langCode){
			LocaleManager localeManager = context.getSystemService(LocaleManager.class);
            
			Log.d(TAG, "SetSystemLanguage "+langCode);
            // 創建只包含新語言的 LocaleList
			Locale locale;
			if(langCode.equals("en"))
				locale = Locale.US;
			else
				locale = Locale.TRADITIONAL_CHINESE;

            LocaleList localeList = new LocaleList(locale);

            // 呼叫 setSystemLocales 設置系統語言
            // 這是需要系統權限 (CHANGE_CONFIGURATION) 的方法
            localeManager.setSystemLocales(localeList);		
	}
}
