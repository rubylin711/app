package com.prime.appDownloadManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prime.appDownloadManager.ServerCommunicate.InterfaceString;

import java.util.ArrayList;

import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.INSTRUCTION_ID_DEFAULT_VALUE;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.KEY_NAME_INSTRUCTION_ID;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.RESULT_STATUS.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST_ONOFF.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST.*;
import static com.prime.appDownloadManager.ServerCommunicate.InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.*;

public class ResultReceiver extends BroadcastReceiver
{
    private static final String TAG = "ResultReceiver";
    private static final boolean DEBUG = true;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: get Action = " + action);
        int instructionID = intent.getIntExtra(KEY_NAME_INSTRUCTION_ID,INSTRUCTION_ID_DEFAULT_VALUE);
        Log.d(TAG, "onReceive: instruction ID = " + instructionID);
        if (instructionID != InterfaceString.INSTRUCTION_LIST.UI_Management.CMD_RESULT.ID)
            return;
        int executeInstructionID = intent.getIntExtra(KEY_NAME_EXECUTE_INSTRUCTION_ID, EXECUTE_INSTRUCTION_ID_DEFAULT_VALUE);
        Log.d(TAG, "onReceive: execute Instruction ID = " + executeInstructionID);
        if (executeInstructionID == EXECUTE_INSTRUCTION_ID_DEFAULT_VALUE)
            return;
        switch (executeInstructionID)
        {
            case InterfaceString.INSTRUCTION_LIST.UI_Management.INSTALL_APK.ID:
            {
                String pkgName = intent.getStringExtra(KEY_NAME_INSTALL_PKGNAME);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, RESULT_STATUS_FAILURE);
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);
                String downloadFilePath = "";
                long downloadID = 0;
                if (resultStatus >= DOWNLOAD_RESULT_STATUS_PENDING && resultStatus <= DOWNLOAD_PROGRESS)
                {
                    downloadFilePath = intent.getStringExtra(KEY_NAME_DOWNLOAD_FILE_PATH);
                    Log.d(TAG, "onReceive: download File Path = " + downloadFilePath);
                    if (resultStatus == DOWNLOAD_PROGRESS)
                    {
                        downloadID = intent.getLongExtra(KEY_NAME_DOWNLOAD_ID, DOWNLOAD_ID_DEFAULT_VALUE);
                        Log.d(TAG, "onReceive: download ID = " + downloadID);
                    }
                }

                Log.d(TAG, "onReceive: pkg Name = " + pkgName);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            case InterfaceString.INSTRUCTION_LIST.UI_Management.UNINSTALL_APK.ID:
            {
                String pkgName = intent.getStringExtra(KEY_NAME_UNINSTALL_PKGNAME);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, RESULT_STATUS_FAILURE);
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);

                Log.d(TAG, "onReceive: pkg Name = " + pkgName);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            case InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST_ONOFF.ID:
            {
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);
                boolean whitelistOnOff = intent.getBooleanExtra(KEY_NAME_WHITELIST_ONOFF, false);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, RESULT_STATUS_FAILURE);

                Log.d(TAG, "onReceive: whitelist On Off = " + whitelistOnOff);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            case InterfaceString.INSTRUCTION_LIST.UI_Management.SET_WHITELIST.ID:
            {
                ArrayList<String> currentWhitelist = intent.getStringArrayListExtra(KEY_NAME_CURRENT_WHITELIST);
                ArrayList<String> appendSamePkgNameList= intent.getStringArrayListExtra(KEY_NAME_APPEND_SAME_PKGNAME_LIST);
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);
                boolean append = intent.getBooleanExtra(KEY_NAME_APPEND, false);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, RESULT_STATUS_FAILURE);

                Log.d(TAG, "onReceive: current Whitelist = " + currentWhitelist);
                Log.d(TAG, "onReceive: append Same Pkg Name List = " + appendSamePkgNameList);
                Log.d(TAG, "onReceive: append = " + append);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            case InterfaceString.INSTRUCTION_LIST.UI_Management.DELETE_WHITELIST.ID:
            {
                ArrayList<String> deletedWhitelist = intent.getStringArrayListExtra(KEY_NAME_DELETED_WHITELIST);
                ArrayList<String> currentWhitelist = intent.getStringArrayListExtra(KEY_NAME_CURRENT_WHITELIST);
                ArrayList<String> pkgNameNotFoundList = intent.getStringArrayListExtra(KEY_NAME_PKGNAMES_NOT_FOUND_LIST);
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, RESULT_STATUS_FAILURE);

                Log.d(TAG, "onReceive: deleted Whitelist = " + deletedWhitelist);
                Log.d(TAG, "onReceive: current Whitelist = " + currentWhitelist);
                Log.d(TAG, "onReceive: pkg Name Not Found List = " + pkgNameNotFoundList);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            case InterfaceString.INSTRUCTION_LIST.UI_Management.DELETE_ALL_WHITELIST.ID:
            {
                ArrayList<String> deletedWhitelist = intent.getStringArrayListExtra(KEY_NAME_DELETED_WHITELIST);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, RESULT_STATUS_FAILURE);
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);

                Log.d(TAG, "onReceive: deleted Whitelist = " + deletedWhitelist);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            case InterfaceString.INSTRUCTION_LIST.UI_Management.GET_WHITELIST.ID:
            {
                ArrayList<String> currentWhitelist = intent.getStringArrayListExtra(KEY_NAME_CURRENT_WHITELIST);
                boolean whitelistOnOff = intent.getBooleanExtra(KEY_NAME_WHITELIST_ONOFF, false);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, RESULT_STATUS_FAILURE);
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);

                Log.d(TAG, "onReceive: current Whitelist = " + currentWhitelist);
                Log.d(TAG, "onReceive: whitelist On Off = " + whitelistOnOff);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            case InterfaceString.INSTRUCTION_LIST.UI_Management.SET_RETURN_TARGET.ID:
            {
                String returnTargetAction = intent.getStringExtra(KEY_NAME_RETURN_TARGET_ACTION);
                String retrunTargetPkgName = intent.getStringExtra(KEY_NAME_RETURN_TARGET_PKGNAME);
                String resultDetail = intent.getStringExtra(KEY_NAME_RESULT_DETAIL);
                int resultStatus = intent.getIntExtra(KEY_NAME_RESULT_STATUS, 0);

                Log.d(TAG, "onReceive: return Target Action = " + returnTargetAction);
                Log.d(TAG, "onReceive: retrun Target Pkg Name = " + retrunTargetPkgName);
                Log.d(TAG, "onReceive: result Status = " + resultStatus);
                Log.d(TAG, "onReceive: result Detail = " + resultDetail);
                break;
            }
            default:
            {
                Log.d(TAG, "onReceive: type is not defined executeInstructionID, executeInstructionID = " + executeInstructionID);
                break;
            }
        }
    }
}
