package com.prime.android.tv.otaservice;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.*;

import org.json.JSONObject;

//import org.json.JSONArray;

public class ZipFileDownload {
    private static final String TAG = "ZipFileDownload";
    private boolean OTA_DEBUG = true ;

    public static final String ACS_CLIENT_PACKAGE_NAME = "com.prime.acsclient";
    public static final String DTV_SERVICE_PACKAGE_NAME = "com.prime.dtvservice";

    private final String ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE   = "com.prime.android.tv.otaservice.abupdate.download.complete";
    private final String ABUPDATE_BROADCAST_DOWNLOAD_ERROR      = "com.prime.android.tv.otaservice.abupdate.download.error";
	private final String ZIPFILE_DOWNLOAD_STATUS 				= "persist.sys.prime.ota_download_status";
	private final String ZIPFILE_DOWNLOAD_PERCENT 				= "persist.sys.prime.ota_download_percent";
	private final String ZIPFILE_DOWNLOAD_URL 					= "persist.sys.prime.ota_download_url";
	private final String ZIPFILE_DOWNLOAD_MD5 					= "persist.sys.prime.ota_download_md5";	
	private final String ZIPFILE_DOWNLOAD_RETRY_COUNT    		= "persist.sys.prime.ota_download_retry";	

    private String updateFileSaveDir = "/data/ota_package" ;
    private String updateFileSaveFile = "DADA_1319D.zip" ;
    public String updateFileSavePath = updateFileSaveDir + "/" + updateFileSaveFile ;
    private String downloadFileSavePath = "/sdcard/Download/DADA_1319D.zip";
    private String download_url = "http://10.1.4.81/Kbro_A1_N97.zip";
    private final int TRUE = 1 ;
    private final int FALSE = 0;
    private final int DEFAULT_MAX_WAIT_TIME = 2 * 60 * 1000;  // 2 minutes
    private final int MB = 1024*1024 ;
    private final int MAX_File_Size_MB = 1000 ;//Mb
    private DownloadManager mDownloadManager = null;
    private DownloadReceiver receiver = null;
    private long nowDownloadId = -1;
    private long downloadLastRunningTime = 0;
    private Context mContext;
    private Handler mHandler = null;
    private HttpsURLConnection mHttpsConnection = null;
    private HttpURLConnection mHttpConnection = null;
    public SharedPreferences mSharedPreference;
    public SharedPreferences.Editor mEditor;	

    public ZipFileDownload(Context context){
        init_data(context);
       	mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mSharedPreference.edit();
        //StartDownloadUpdateFile();
    }

    public void registerHandler(Handler h) {
        mHandler = h;
    }

    public void init_data(Context context){
        Log.d(TAG, "init_data: ");
        //mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        mContext = context;
        receiver = new DownloadReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContext.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED);
    }


    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                if( OTA_DEBUG )
                    Log.i(TAG, "ACTION_DOWNLOAD_COMPLETE");
                //verify_otapackage_signature();
                copyfile(downloadFileSavePath, updateFileSavePath);
                Intent intentToAcsClient = new Intent(ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE);
                intentToAcsClient.setPackage(ACS_CLIENT_PACKAGE_NAME);
                Log.d(TAG,"ACTION_DOWNLOAD_COMPLETE = " + intentToAcsClient);
                context.sendBroadcast(intentToAcsClient);
            }
        }
    }

    public void StartDownloadUpdateFile(String url){
        deleteUpdateFile();

        if ( ! checkFreeSpace(updateFileSaveDir) )
        {
            Log.e(TAG, "Space not enough") ;
            return ;
        }
        try {
            if (OTA_DEBUG)
                Log.i(TAG, "StartDownloadUpdateFile");

            if (mContext == null)
                Log.d(TAG, "StartDownloadUpdateFile: mContext is null");
            else
                Log.d(TAG, "StartDownloadUpdateFile: mContext = " + mContext.toString());


            mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

            if (OTA_DEBUG)
                Log.i(TAG, "StartDownloadUpdateFile AAAAAAAAAAA");

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            if (OTA_DEBUG)
                Log.i(TAG, "StartDownloadUpdateFile BBBBBBBBBBB");

            //request.setDestinationUri(Uri.fromFile(new File(updateFileSaveDir, updateFileSaveFile)));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, updateFileSaveFile);
            //request.setDestinationToSystemCache();
            nowDownloadId = mDownloadManager.enqueue(request);
            downloadLastRunningTime = 0;
        }catch (Exception e){
            if (OTA_DEBUG)
                Log.i(TAG, "StartDownloadUpdateFile EXCEPTION");
            e.printStackTrace();
        }
    }

    public void StopDownliadUpdateFile()
    {
        mDownloadManager.remove(nowDownloadId);
        nowDownloadId = -1 ;
    }

    private void deleteUpdateFile()
    {
        if( OTA_DEBUG )
            Log.i(TAG, "deleteUpdateFile");
        File cacheFile = new File(updateFileSavePath);
        if (cacheFile.exists())
        {
            cacheFile.delete();
        }
    }

    private boolean checkFreeSpace(String statFsPath)
    {
        StatFs stat = new StatFs(statFsPath);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        if( OTA_DEBUG )
            Log.i( TAG, "avalible = " + ((availableBlocks * blockSize)/MB)) ;
        return ((availableBlocks * blockSize)/MB) >= MAX_File_Size_MB;
    }

    private void copyfile(String fileFromPath, String fileToPath) {
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
            Log.e(TAG, "copyFile: change permission Exception = " + exception);
        }
    }

    private boolean startDownloadFileFromHttp(String fileUrl) {
        Log.d(TAG, "startDownloadFileFromHttp: " + fileUrl);

        mHttpConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
		//RandomAccessFile randomAccessFile = null;
		int fileLength = 0;
		int retryCount = 0;
		final int maxRetries = SystemProperties.getInt(ZIPFILE_DOWNLOAD_RETRY_COUNT, 60);	
		File outputFile = new File(updateFileSaveDir, "DADA_1319D.zip");	

		SystemProperties.set(ZIPFILE_DOWNLOAD_STATUS, "1");
		while(retryCount < maxRetries) {
			try {
				// 創建 URL 對象
				URL url = new URL(fileUrl);
		
				// 打開 HTTP 連接
				mHttpConnection = (HttpURLConnection) url.openConnection();
		
				mHttpConnection.setRequestMethod("GET");
				mHttpConnection.setConnectTimeout(10000);
				mHttpConnection.setReadTimeout(10000);			
		
				// 獲取輸入流
				inputStream = mHttpConnection.getInputStream();
				fileLength = mHttpConnection.getContentLength();
		
				// 創建保存文件的 File 對象
				//File outputFile = new File(updateFileSaveDir, "DADA_1319D.zip");
		
				// 寫入數據到文件
				fileOutputStream = new FileOutputStream(outputFile);
	
				byte[] buffer = new byte[4096];
				int bytesRead;
				long totalBytesRead = 0;
				int progress = 0;
				SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, Integer.toString(progress));
		
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;
					// 計算進度
					int temp = (int) (totalBytesRead * 100 / fileLength);
					if(temp != progress){
						progress = temp;
						Log.d(TAG, "下載進度: " + progress + "%");	
						SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, Integer.toString(progress));
					}

				}
		
				fileOutputStream.flush();
		
				// 下載完成
				progress = 100;
				SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, Integer.toString(progress));
				store_download_url("");
				store_download_md5("");
				//SystemProperties.set(ZIPFILE_DOWNLOAD_URL, "");
				//SystemProperties.set(ZIPFILE_DOWNLOAD_MD5, "");	
				Log.i(TAG, "startDownloadFileFromHttp: " + outputFile.getAbsolutePath());
				break;
			} catch (Exception e) {
				e.printStackTrace();
				retryCount++;
				if (retryCount < maxRetries) {
					Log.d(TAG, "重試次數: " + retryCount + "，等待5秒後重試...");
					try {
						Thread.sleep(5000); // 等待5秒後重試
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt(); // 恢復中斷狀態
					}
				} else {
					Log.e(TAG, "達到最大重試次數，下載失敗");
					SystemProperties.set(ZIPFILE_DOWNLOAD_STATUS, "3");
					SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, "0");
					return false;
				}
			} finally {
				// 關閉流
				try {
					if (inputStream != null) inputStream.close();
					if (fileOutputStream != null) fileOutputStream.close();
					if (mHttpConnection != null) mHttpConnection.disconnect();
					mHttpConnection = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
    }

    private boolean startDownloadFileFromHttps(String fileUrl) {
        Log.d(TAG, "startDownloadFileFromHttps: " + fileUrl);

        mHttpsConnection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        int fileLength = 0;
		int retryCount = 0;
        final int maxRetries = SystemProperties.getInt(ZIPFILE_DOWNLOAD_RETRY_COUNT, 60);	
        File outputFile = new File(updateFileSaveDir, "DADA_1319D.zip");	

        SystemProperties.set(ZIPFILE_DOWNLOAD_STATUS, "1");
        while(retryCount < maxRetries) {
            try {
                // 創建 URL 對象
                URL url = new URL(fileUrl);

                // 打開 HTTP 連接
                mHttpsConnection = (HttpsURLConnection) url.openConnection();
                // 配置超时
                mHttpsConnection.setConnectTimeout(5000); // 連接超時（毫秒）
                mHttpsConnection.setReadTimeout(5000);    // 讀取超時（毫秒）

                // 配置請求方法
                mHttpsConnection.setRequestMethod("GET");
                // 配置自定義 SSL（如果需要忽略自簽名證書）
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        Log.i(TAG, "checkClientTrusted: ");
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        Log.i(TAG, "checkServerTrusted: ");
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        Log.i(TAG, "getAcceptedIssuers: ");
                        return new X509Certificate[0];
                    }
                }}, new SecureRandom());
                mHttpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());

                // 檢查響應碼
                int responseCode = mHttpsConnection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) { // HTTP 200
                    Log.i(TAG,"Connection established. Downloading file...");

                    // 獲取輸入流
                    inputStream = mHttpsConnection.getInputStream();
                    fileLength = mHttpsConnection.getContentLength();

                    // 創建保存文件的 File 對象
                    //File outputFile = new File(updateFileSaveDir, "DADA_1319D.zip");

                    // 創建本地文件輸出流
                    outputStream = new FileOutputStream(outputFile);

                    // 讀取文件數據並寫入到本地
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;
				    int progress = 0;
                    SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, Integer.toString(progress));

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        // 計算進度
                        int temp = (int) (totalBytesRead * 100 / fileLength);
					    if(temp != progress){
                            progress = temp;
                            Log.d(TAG, "下載進度: " + progress + "%");	
                            SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, Integer.toString(progress));
                        }
                    }

                    outputStream.flush();

                    // 下載完成
                    progress = 100;
				    SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, Integer.toString(progress));
				    store_download_url("");
				    store_download_md5("");
                    // 關閉流
                    Log.i(TAG, "startDownloadFileFromHttps: " + outputFile.getAbsolutePath());

                    break;
                } else {
                    Log.e(TAG, "Failed to download file. HTTPS response code: " + responseCode);

                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to download file. Exception: " + e);
                retryCount++;
                if (retryCount < maxRetries) {
					Log.d(TAG, "重試次數: " + retryCount + "，等待5秒後重試...");
					try {
						Thread.sleep(5000); // 等待5秒後重試
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt(); // 恢復中斷狀態
					}
				} else {
					Log.e(TAG, "達到最大重試次數，下載失敗");
					SystemProperties.set(ZIPFILE_DOWNLOAD_STATUS, "3");
					SystemProperties.set(ZIPFILE_DOWNLOAD_PERCENT, "0");
					return false;
				}
            }  finally {
                // 關閉流
                try {
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                    if (mHttpsConnection != null) mHttpsConnection.disconnect();
                    mHttpsConnection = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public boolean startDownloadFile(String fileUrl) {
        
        if (fileUrl.contains("https")) {
            return startDownloadFileFromHttps(fileUrl);
        }
        else {
            return startDownloadFileFromHttp(fileUrl);
        }

    }

    public boolean checkDownloading(String fileUrl) {
        if (fileUrl.contains("https")) {
            if (mHttpsConnection == null) 
                return false;
            
            mHttpsConnection.disconnect();
            return true;
        }
        else {
            if (mHttpConnection == null) 
                return false;
        
            mHttpConnection.disconnect();
            return true;
        }
    }

    public void downloadFile(String fileUrl, String fileMd5) {
		store_download_url(fileUrl);
		store_download_md5(fileMd5);

        new Thread(() -> {
            deleteUpdateFile();
            
            if (!checkFreeSpace(updateFileSaveDir) )
            {
                Log.e(TAG, "Space not enough") ;
                sendDonwloadError();
                return ;
            }

            if (checkDownloading(fileUrl)) {
                Log.e(TAG, "still downloading, stop download") ;
                sendDownloadStatus();
                return ;
            }

            if (!startDownloadFile(fileUrl)) {
                Log.e(TAG, "download not complete") ;
                sendDonwloadError();
                return ;
            }

            if (!checkMd5(fileMd5)) {
                Log.e(TAG, "download file Md5 not the same") ;
                sendDonwloadError();
                return ;
            }

            if (!setFilepermissions()) {
                Log.e(TAG, "set permissions fail") ;
                sendDonwloadError();
                return ;
            }

            sendDonwloadComplete();
        }).start();
    }

    private Boolean checkMd5(String fileMd5) {
        File downloadFile = new File(updateFileSavePath);
        String md5String;
        try {
            md5String = Md5Units.getFileMD5String(downloadFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Log.d(TAG, "checkMd5 md5String = " + md5String) ;
        if (fileMd5.equals(md5String))
            return true;
        
        return false;
    }

    private Boolean setFilepermissions() {
        File downloadFile = new File(updateFileSavePath);
        if (!downloadFile.exists()) {
            return false;
        }

        try {
            String perm = "rwxrwxrwx";// in octal = 770
            Set<PosixFilePermission> permissions = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                permissions = PosixFilePermissions.fromString(perm);
            } if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.setPosixFilePermissions(downloadFile.toPath(), permissions);
            }
            return true;
        } catch (IOException exception) {
            Log.e(TAG, "setFilepermissions change permission Exception = " + exception);
            return false;
        }
    }

    private void sendDonwloadComplete() {
        sendDownloadStatus();

        Intent intentToClient = new Intent(ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE);
        intentToClient.setPackage(DTV_SERVICE_PACKAGE_NAME);
        Log.d(TAG,"ACTION_DOWNLOAD_COMPLETE = " + intentToClient);
        mContext.sendBroadcast(intentToClient);
    }

    private void sendDonwloadError() {
        sendDownloadStatus();

        Intent intentToClient = new Intent(ABUPDATE_BROADCAST_DOWNLOAD_ERROR);
        intentToClient.setPackage(DTV_SERVICE_PACKAGE_NAME);
        Log.d(TAG,"ACTION_DOWNLOAD_ERROR = " + intentToClient);
        mContext.sendBroadcast(intentToClient);
    }

    private void sendDownloadStatus() {
        Message msg = new Message();
        msg.what = DownloadService.MSG_DOWNLOAD_STATUS;
        msg.obj = 0; // OTA_Cur_Status.Idle
        mHandler.sendMessage(msg);
    }

	public void check_download_failed_retry(){
		int download_status = SystemProperties.getInt(ZIPFILE_DOWNLOAD_STATUS, 0);
		Log.d(TAG, "check_download_failed_retry = > download_status = "+download_status);
		if(mSharedPreference == null){
			Log.e(TAG, "check_download_failed_retry fail");
			return;
		}
		if(download_status == 3 || download_status == 1){//
			try{
				Message msg = new Message();
				JSONObject obj = new JSONObject();
				String DownloadZipFileUrl = mSharedPreference.getString(ZIPFILE_DOWNLOAD_URL, "");//SystemProperties.get(ZIPFILE_DOWNLOAD_URL, "");
				String DownloadZipFileUMd5 = mSharedPreference.getString(ZIPFILE_DOWNLOAD_MD5, "");//SystemProperties.get(ZIPFILE_DOWNLOAD_MD5, "");	

				Log.d(TAG, "check_download_failed_retry = > DownloadZipFileUrl = "+DownloadZipFileUrl);
				Log.d(TAG, "check_download_failed_retry = > DownloadZipFileUMd5 = "+DownloadZipFileUMd5);
				obj.put("DownloadZipUrl", DownloadZipFileUrl);
				obj.put("DownloadZipMd5", DownloadZipFileUMd5);	
				msg.what = DownloadService.MSG_DOWNLOAD_ZIP;
				msg.obj = obj; // OTA_Cur_Status.Idle
				mHandler.sendMessage(msg);		
			}catch (Exception e) {
                Log.e(TAG, "check_download_failed_retry , error = " + e.getMessage());
            }	
		}
	}	
	
	private void store_download_url(String url){
		if(mEditor == null){
			Log.e(TAG, "store_download_url fail");
			return;
		}
		mEditor.putString(ZIPFILE_DOWNLOAD_URL, url);
		mEditor.apply();
	}

	private void store_download_md5(String md5){
		if(mEditor == null){
			Log.e(TAG, "store_download_md5 fail");
			return;
		}
		mEditor.putString(ZIPFILE_DOWNLOAD_MD5, md5);
		mEditor.apply();		
	}	
}