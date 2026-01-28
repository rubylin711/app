package com.prime.appDownloadManager.UpdateApp;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.appDownloadManager.ServerCommunicate.BackendManagementService;
import com.prime.appDownloadManager.ServerCommunicate.InterfaceString;
import com.prime.appDownloadManager.Utility;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.KEY_NAME_INSTRUCTION_ID;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.RESULT_DETAIL.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.RESULT_STATUS.*;

public class DownloadFile
{
    private static final String TAG = "DownloadFile";
    private static final boolean DEBUG = false;
    public static final int MSG_DOWNLOAD_COMPLETE = 111;
    private static final int TIMER_CHECK_DOWNLOAD_PROGRESS_INTERVAL = 1000;
    private static final int TIMER_CHECK_DOWNLOAD_CANCEL_INTERVAL = 1000;
    private List<Long> mNowDownloadIds = new ArrayList<>();
    private List<String> mInstallPkgNames = new ArrayList<>();
    private List<String> mDownloadFilePath = new ArrayList<>();
    private List<Long> mDownloadCancelIds = new ArrayList<>();
    private List<Integer> mDownloadNoNetworkCounts = new ArrayList<>();
    private WeakReference<Context> mContRef = null;
    private DownloadManager mDownloadManager = null;
    private Handler mHandler = null;
    private CountDownTimer mDownloadProgressTimer = null;
    private CountDownTimer mDownloadCancelTimer = null;

    public DownloadFile(Context context, Handler handler)
    {
        mContRef = new WeakReference<>(context);
        mHandler = handler;
        mDownloadManager = (DownloadManager) mContRef.get().getSystemService(Context.DOWNLOAD_SERVICE);
        initDownloadProgressTimer();
        initDownloadCancelTimer();
        IntentFilter downloadIntentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContRef.get().registerReceiver(mDownloadReceiver, downloadIntentFilter, Context.RECEIVER_EXPORTED);
        mDownloadProgressTimer.start();
        mDownloadCancelTimer.start();
    }

    public void deinit()
    {
        mContRef.get().unregisterReceiver(mDownloadReceiver);
        mDownloadProgressTimer.cancel();
        mDownloadCancelTimer.cancel();
    }

    public void startDownload(String pkgName, String downloadUri)
    {
        Context context = mContRef.get();
        if (context == null) {
            Log.e(TAG, "startDownload: Context reference is null, cannot proceed");
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUri));

        File dir = new File(context.getExternalFilesDir(null), "downloadAppFile");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String downloadPath = mContRef.get().getExternalFilesDir(null).getAbsolutePath() + "/downloadAppFile";
        File updatefile = new File(downloadPath + "/" + pkgName);

        Uri fileUri = Uri.fromFile(updatefile);
        if (fileUri == null) {
            Log.e(TAG, "startDownload: Failed to create Uri from file");
            return;
        }

        request.setDestinationUri(Uri.fromFile(updatefile)); // open ab_update.zip or update.zip in /sdcard
        if (DEBUG)
        {
            Log.d(TAG, "startDownload: download loacation = " + String.valueOf(Uri.fromFile(updatefile))) ;
            Log.d(TAG, "startDownload: updatefile getPath = " + updatefile.getPath());
        }
        long requestID = mDownloadManager.enqueue(request);
        if(requestID == -1){
            Log.e(TAG, "startDownload: Failed to get requestID");
            return;
        }
        mNowDownloadIds.add(requestID);
        mInstallPkgNames.add(pkgName);
        mDownloadFilePath.add(updatefile.getPath());
        mDownloadNoNetworkCounts.add(0);
        if (DEBUG)
        {
            Log.d(TAG, "startDownload: path name = " + downloadPath + "/" + pkgName);
            Log.d(TAG, "startDownload: last NowDownloadId = " + mNowDownloadIds.get(mNowDownloadIds.size() - 1));
            Log.d(TAG, "startDownload: mNowDownloadIds size = " + mNowDownloadIds.size());
        }
    }

    private void initDownloadProgressTimer()
    {
        if (mDownloadProgressTimer != null)
            return;

        mDownloadProgressTimer = new CountDownTimer(Long.MAX_VALUE, TIMER_CHECK_DOWNLOAD_PROGRESS_INTERVAL)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                if (mNowDownloadIds.size() == 0)
                    return;

                for (long id : mNowDownloadIds)
                {
                    int pos = mNowDownloadIds.indexOf(id);
                    String installPkgName = mInstallPkgNames.get(pos);
                    double progress = getDownloadProgress(id);
                    double total = getDownloadTotal(id);
                    int status = getDownloadStatus(id);
                    int percent = (int) ((progress/total)*100);
                    if (DEBUG)
                    {
                        Log.d(TAG, "mDownloadProgressTimer onTick: status = " + status);
                        Log.d(TAG, "mDownloadProgressTimer onTick: id = " + id + ", progress = " + progress + ", total = " + total);
                        Log.d(TAG, "mDownloadProgressTimer onTick: installPkgName = " + installPkgName + ", percent = " + percent + "%");
                        Log.d(TAG, "mDownloadProgressTimer onTick: =========================");
                    }
                    if (Utility.canSendBroadcast(mContRef.get()))
                    {
                        Intent progressIntent;
                        progressIntent = new Intent(BackendManagementService.mTargetAction); //設定廣播 action 識別碼
                        progressIntent.setPackage(BackendManagementService.mTargetPkgName);
                        progressIntent.putExtra(
                                KEY_NAME_INSTRUCTION_ID,
                                InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                        progressIntent.putExtra(
                                KEY_NAME_EXECUTE_INSTRUCTION_ID,
                                InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID);//fix to execute ID
                        progressIntent.putExtra(
                                KEY_NAME_INSTALL_PKGNAME,
                                mInstallPkgNames.get(pos));
                        progressIntent.putExtra(KEY_NAME_DOWNLOAD_ID, id); //設定廣播夾帶參數
                        progressIntent.putExtra(KEY_NAME_RESULT_STATUS, DOWNLOAD_PROGRESS);
                        progressIntent.putExtra(KEY_NAME_RESULT_DETAIL, String.valueOf(percent));
                        progressIntent.putExtra(KEY_NAME_DOWNLOAD_FILE_PATH, mDownloadFilePath.get(pos));
                        mContRef.get().sendBroadcast(progressIntent); //發送廣播訊息
                    }
                }
            }

            @Override
            public void onFinish()
            {
                if (DEBUG)
                    Log.d(TAG, "mDownloadProgressTimer onFinish");
            }
        };
    }

    private void initDownloadCancelTimer()
    {
        if (mDownloadCancelTimer != null)
            return;
        mDownloadCancelTimer = new CountDownTimer(Long.MAX_VALUE, TIMER_CHECK_DOWNLOAD_CANCEL_INTERVAL)
        {
            int count = 1;
            int countMax = 120;
            @Override
            public void onTick(long millisUntilFinished)
            {
                if (DEBUG)
                    //Log.d(TAG, "onTick: mNowDownloadIds.size = " + mNowDownloadIds.size());
                if (mNowDownloadIds.size() == 0)
                {
                    if (count != 1)
                        count = 1;
                    if (DEBUG)
                        //Log.d(TAG, "onTick: mNowDownloadIds.size() = 0, num = " + num);
                    return;
                }

                for (long id : mNowDownloadIds)
                {
                    int status = getDownloadStatus(id);
                    int pos = mNowDownloadIds.indexOf(id);
                    int noNetworkCount = mDownloadNoNetworkCounts.get(pos);
                    Log.d(TAG, "mDownloadCancelTimer onTick: count = " + count);
                    Log.d(TAG, "mDownloadCancelTimer onTick: id = " + id);
                    Log.d(TAG, "mDownloadCancelTimer onTick: status = " + status);

                    if (status == DOWNLOAD_RESULT_STATUS_PENDING || status == DOWNLOAD_RESULT_STATUS_PAUSED)
                    {
                        mDownloadNoNetworkCounts.set(pos, ++noNetworkCount);
                        Log.d(TAG, "onTick: mDownloadNoNetworkCounts get(" + pos + ") = " + mDownloadNoNetworkCounts.get(pos));
                    }


                    if (count == countMax)
                    {
                        if (noNetworkCount == countMax)
                        {
                            mDownloadCancelIds.add(id);
                            cancelDownload(id);
                        }
                        else
                            mDownloadNoNetworkCounts.set(pos, 0);
                    }
                }

                if (count == countMax)
                    count = 1;
                else
                    count++;
            }

            @Override
            public void onFinish()
            {
                if (DEBUG)
                    Log.d(TAG, "mDownloadCancelTimer onFinish");
            }
        };
    }

    private void downloadComplete(String downloadFilePath, String pkgName)
    {
        Message msg = new Message();
        msg.what = MSG_DOWNLOAD_COMPLETE;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME_DOWNLOAD_FILE_PATH, downloadFilePath);
        bundle.putString(KEY_NAME_INSTALL_PKGNAME, pkgName);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private final BroadcastReceiver mDownloadReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.d(TAG, "DownloadReceiver onReceive: action = " + action);
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            {
                long loadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                int status = getDownloadStatus(loadId);
                String reason = getDownloadReason(loadId);
                double progress = getDownloadProgress(loadId);
                double total = getDownloadTotal(loadId);

                if (DEBUG)
                {
                    Log.d(TAG, "DownloadReceiver onReceive: loadId = " + loadId);
                    for (long id : mNowDownloadIds)
                        Log.d(TAG, "DownloadReceiver onReceive: id_" + mNowDownloadIds.indexOf(id) + " = " + id);
                    Log.d(TAG, "onReceive: id = " + loadId);
                    Log.d(TAG, "DownloadReceiver onReceive: status = " + status);//if STATUS_SUCCESSFUL, progress value = total value
                    Log.d(TAG, "DownloadReceiver onReceive: reason = " + reason);
                    Log.d(TAG, "DownloadReceiver onReceive: progress = " + progress);
                    Log.d(TAG, "DownloadReceiver onReceive: total = " + total);
                    Log.d(TAG, "DownloadReceiver onReceive: percent = " + (int) ((progress/total)*100) + "%");
                }

                if (!mNowDownloadIds.contains(loadId))
                {
                    if (DEBUG)
                    {
                        Log.d(TAG, "DownloadReceiver onReceive: loadId is not found in mNowDownloadIds");
                        Log.d(TAG, "DownloadReceiver onReceive: loadId = " + loadId);
                    }
                    return;
                }

                int pos = mNowDownloadIds.indexOf(loadId);
                if (status == DOWNLOAD_RESULT_STATUS_SUCCESSFUL)
                {
                    Uri fileUri = mDownloadManager.getUriForDownloadedFile(loadId);
                    String downloadFilePath = getDataColumn(fileUri, null, null);
                    String pkgName = mInstallPkgNames.get(pos);
                    if (DEBUG)
                    {
                        Log.d(TAG, "DownloadReceiver onReceive: file uri = " + fileUri);
                        Log.d(TAG, "DownloadReceiver onReceive: download file path = " + downloadFilePath);
                    }
                    downloadComplete(downloadFilePath, pkgName);
                }
                else
                {
                    if (Utility.canSendBroadcast(context))
                    {
                        if (mDownloadCancelIds.contains(loadId))
                        {
                            reason = DOWNLOAD_RESULT_DETAIL_ERROR_NO_NETWORK;
                            mDownloadCancelIds.remove(loadId);
                        }
                        Intent resultIntent = null;
                        resultIntent = new Intent(BackendManagementService.mTargetAction); //設定廣播 action 識別碼
                        resultIntent.setPackage(BackendManagementService.mTargetPkgName);
                        resultIntent.putExtra(
                                KEY_NAME_INSTRUCTION_ID,
                                InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID);
                        resultIntent.putExtra(
                                KEY_NAME_EXECUTE_INSTRUCTION_ID,
                                InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID);
                        resultIntent.putExtra(KEY_NAME_INSTALL_PKGNAME, mInstallPkgNames.get(pos));
                        resultIntent.putExtra(KEY_NAME_RESULT_STATUS, status);
                        resultIntent.putExtra(KEY_NAME_RESULT_DETAIL, reason); //設定廣播夾帶參數
                        resultIntent.putExtra(KEY_NAME_DOWNLOAD_FILE_PATH,mDownloadFilePath.get(pos));
                        mContRef.get().sendBroadcast(resultIntent); //發送廣播訊息
                        if (DEBUG)
                            Log.d(TAG, "DownloadReceiver onReceive: sendBroadcast finish");
                    }
                }

                mNowDownloadIds.remove(loadId);
                mInstallPkgNames.remove(pos);
                mDownloadFilePath.remove(pos);
                mDownloadNoNetworkCounts.remove(pos);

                if (mNowDownloadIds.size() == 0)
                {
                    if (DEBUG)
                    {
                        Log.d(TAG, "DownloadReceiver onReceive: unregisterReceiver mReceiver");
                        Log.d(TAG, "DownloadReceiver onReceive: mPkgNames size = " + mInstallPkgNames.size());
                    }
                }
                /*else
                {
                    Log.d(TAG, "onReceive: leftover id :");
                    for (long id : mNowDownloadIds)
                        Log.d(TAG, "onReceive: id_" + mNowDownloadIds.indexOf(id) + " = " + id);
                }*/
            }
        }
    };

    /**
     * 獲取下載狀態
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return int
     * @see DownloadManager#STATUS_PENDING  　　 下載等待開始時
     * @see DownloadManager#STATUS_PAUSED   　　 下載暫停
     * @see DownloadManager#STATUS_RUNNING　     正在下載中　
     * @see DownloadManager#STATUS_SUCCESSFUL   下載成功
     * @see DownloadManager#STATUS_FAILED       下載失敗
     */
    private int getDownloadStatus(long downloadId)
    {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        int status = -1;
        if (cursor != null)
        {
            try
            {
                if (cursor.moveToFirst())
                    status =  cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            }
            finally
            {
                cursor.close();
            }
        }

        if (status == DownloadManager.STATUS_PENDING)
            return DOWNLOAD_RESULT_STATUS_PENDING;
        else if (status == DownloadManager.STATUS_RUNNING)
            return DOWNLOAD_RESULT_STATUS_RUNNING;
        else if (status == DownloadManager.STATUS_PAUSED)
            return DOWNLOAD_RESULT_STATUS_PAUSED;
        else if (status == DownloadManager.STATUS_SUCCESSFUL)
            return DOWNLOAD_RESULT_STATUS_SUCCESSFUL;
        else if (status == DownloadManager.STATUS_FAILED)
            return DOWNLOAD_RESULT_STATUS_FAILED;
        return DOWNLOAD_RESULT_STATUS_FAILED;
    }

    private String getDownloadReason(long downloadId)
    {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        int intReason = -1;
        if (cursor != null)
        {
            try
            {
                if (cursor.moveToFirst())
                    intReason =  cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
            }
            finally
            {
                cursor.close();
            }
        }

        if (intReason == DownloadManager.ERROR_UNKNOWN)
            return DOWNLOAD_RESULT_DETAIL_ERROR_UNKNOWN;
        else if (intReason == DownloadManager.ERROR_FILE_ERROR)
            return DOWNLOAD_RESULT_DETAIL_ERROR_FILE_ERROR;
        else if (intReason == DownloadManager.ERROR_UNHANDLED_HTTP_CODE)
            return DOWNLOAD_RESULT_DETAIL_ERROR_UNHANDLED_HTTP_CODE;
        else if (intReason == DownloadManager.ERROR_HTTP_DATA_ERROR)
            return DOWNLOAD_RESULT_DETAIL_ERROR_HTTP_DATA_ERROR;
        else if (intReason == DownloadManager.ERROR_TOO_MANY_REDIRECTS)
            return DOWNLOAD_RESULT_DETAIL_ERROR_TOO_MANY_REDIRECTS;
        else if (intReason == DownloadManager.ERROR_INSUFFICIENT_SPACE)
            return DOWNLOAD_RESULT_DETAIL_ERROR_INSUFFICIENT_SPACE;
        else if (intReason == DownloadManager.ERROR_DEVICE_NOT_FOUND)
            return DOWNLOAD_RESULT_DETAIL_ERROR_DEVICE_NOT_FOUND;
        else if (intReason == DownloadManager.ERROR_CANNOT_RESUME)
            return DOWNLOAD_RESULT_DETAIL_ERROR_CANNOT_RESUME;
        else if (intReason == DownloadManager.ERROR_FILE_ALREADY_EXISTS)
            return DOWNLOAD_RESULT_DETAIL_ERROR_FILE_ALREADY_EXISTS;
        else if (intReason == ERROR_BLOCKED)
            return DOWNLOAD_RESULT_DETAIL_ERROR_BLOCKED;
        else if (intReason == DownloadManager.PAUSED_WAITING_TO_RETRY)
            return DOWNLOAD_RESULT_DETAIL_PAUSED_WAITING_TO_RETRY;
        else if (intReason == DownloadManager.PAUSED_WAITING_FOR_NETWORK)
            return DOWNLOAD_RESULT_DETAIL_PAUSED_WAITING_FOR_NETWORK;
        else if (intReason == DownloadManager.PAUSED_QUEUED_FOR_WIFI)
            return DOWNLOAD_RESULT_DETAIL_PAUSED_QUEUED_FOR_WIFI;
        else if (intReason == DownloadManager.PAUSED_UNKNOWN)
            return DOWNLOAD_RESULT_DETAIL_PAUSED_UNKNOWN;

        return "the Reason not defined! Reason (int) = " + intReason;
    }

    private String getDataColumn(Uri uri, String selection, String[] selectionArgs)
    {
        Cursor cursor = null;
        final String column = "_data"; // 路徑儲存在 downloads 表中的 _data 欄位
        final String[] projection = {column};
        try
        {
            cursor = mContRef.get().getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst())
            {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "getDataColumn Exception : " + Log.getStackTraceString(e));
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * 獲取當前下載進度
     *
     * @return int
     */
    public int getDownloadProgress(long downloadId)
    {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor != null)
        {
            try
            {
                if (cursor.moveToFirst())
                    return cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            }
            finally
            {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * 獲取下載總大小
     *
     * @return int
     */
    public int getDownloadTotal(long downloadId)
    {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor != null)
        {
            try
            {
                if (cursor.moveToFirst())
                    return cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            }
            finally
            {
                cursor.close();
            }
        }
        return -1;
    }

    private void cancelDownload(long downloadId)
    {
        mDownloadManager.remove(downloadId);
    }
}
