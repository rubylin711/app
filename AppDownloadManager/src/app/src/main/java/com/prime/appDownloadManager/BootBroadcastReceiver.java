package com.prime.appDownloadManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.appDownloadManager.ServerCommunicate.BackendManagementService;

import java.io.File;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

public class BootBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "BootBroadcastReceiver";
    private static final boolean DEBUG = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d( TAG, "Broadcast Action : " + action );
        if (action.equals(ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED))
        {
            String downloadPath = context.getExternalFilesDir(null).getAbsolutePath() + "/downloadAppFile";
            //deleteDir("/storage/emulated/0/Android/data/com.prime.appDownloadManager/files");
            deleteDir(downloadPath);
            Intent intentRun = new Intent(Intent.ACTION_RUN);
            intentRun.setClass(context, BackendManagementService.class);
            intentRun.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intentRun);
        }
    }

    private void deleteDir(String path)
    {
        File file = new File(path);
        if(!file.exists())//判斷是否待刪除目錄是否存在
        {
            if (DEBUG)
                Log.d(TAG, "deleteDir: The dir are not exists! path = " + path);
            return;
        }

        String[] content = file.list();//取得目前的目錄下所有檔和資料夾
        if (content == null || content.length == 0)
        {
            if (DEBUG)
                Log.d(TAG, "deleteDir: The dir is empty folder. path = " + path);
            return;
        }
        for(String name : content)
        {
            File temp = new File(path, name);
            if(temp.isDirectory())//判斷是否是目錄
            {
                deleteDir(temp.getAbsolutePath());//遞迴呼叫，刪除目錄裡的內容
                boolean deleted = temp.delete();//刪除空目錄
            }
            else
            {
                if(!temp.delete())//直接删除文件
                {
                    Log.d(TAG, "deleteDir: Failed to delete " + name + ", path = " + path);
                }
            }
        }
    }
}
