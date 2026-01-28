package com.prime.app.control.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class AppMgrReceiver extends BroadcastReceiver
{
    public final String TAG = getClass().getSimpleName();
    public static final String  BROADCAST_ACTION     = "pesi.broadcast.action.device.manager";
    public static final int     INSTALL_PACKAGE      = 201;
    public static final int     UNINSTALL_PACKAGE    = 202;
    public static final int     SET_WHITELIST_ON_OFF = 203;
    public static final int     SET_WHITELIST        = 204;
    public static final int     DELETE_WHITELIST     = 205;
    public static final int     DELETE_ALL_WHITELIST = 206;
    public static final int     GET_WHITELIST        = 207;
    public static final int     SET_RETURN_TARGET    = 208;
    public static final int     INSTRUCTION_ID       = 209;

    public int      DEBUG_NORMAL            = AppControlService.DEBUG_NORMAL;
    public int      DEBUG_VERBOSE           = AppControlService.DEBUG_VERBOSE;
    public int      DEBUG                   = AppControlService.DEBUG;
    public int      INSTALL_UNINSTALL_COUNT = 0;
    public Handler  mHandler                = null;

    public AppMgrReceiver(Handler handler) {
        Log.d(TAG, "Create " + TAG);
        mHandler = handler;
    }

    public void resetInstallCount() {
        INSTALL_UNINSTALL_COUNT = 0;
        Log.d(TAG, "resetInstallCount: INSTALL_COUNT: " + INSTALL_UNINSTALL_COUNT);
    }

    public void resetUninstallCount() {
        INSTALL_UNINSTALL_COUNT = 0;
        Log.d(TAG, "resetUninstallCount: UNINSTALL_COUNT: " + INSTALL_UNINSTALL_COUNT);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String  ACTION          = intent.getAction();
        int     instructionID   = intent.getIntExtra("INSTRUCTION_ID", 0);
        int     result          = intent.getIntExtra("EXECUTE_INSTRUCTION_ID", 0);
        int     resultStatus    = intent.getIntExtra("RESULT_STATUS", 401);
        String  resultDetail    = intent.getStringExtra("RESULT_DETAIL");

        if ( BROADCAST_ACTION.equals(ACTION) && instructionID == INSTRUCTION_ID )
        {
            ArrayList<String> currentWhitelist, appendSamePkgNameList, deletedWhitelist, pkgNameNotFoundList;
            boolean whitelistOnOff;

            switch (result) {
                case SET_WHITELIST_ON_OFF: {
                    whitelistOnOff = intent.getBooleanExtra("WHITELIST_ONOFF", false);
                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.d(TAG, "onReceive: -- SET_WHITELIST_ON_OFF"
                            + "\n    Whitelist On/Off: " + whitelistOnOff
                            + "\n    Result Status:    " + resultStatus
                            + "\n    Result Detail:    " + resultDetail);
                    sendMessage(MSG.WHITELIST_APPEND);
                    break;
                }
                case SET_WHITELIST: {
                    currentWhitelist      = intent.getStringArrayListExtra("CURRENT_WHITELIST");
                    appendSamePkgNameList = intent.getStringArrayListExtra("APPEND_SAME_PKGNAME_LIST");
                    boolean append        = intent.getBooleanExtra("APPEND", false);
                    Config.WHITELIST      = new ArrayList<>(currentWhitelist);

                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.d(TAG, "onReceive: " + (append ? "-- APPEND WHITELIST" : "-- REPLACE WHITELIST")
                            + "\n    Current Whitelist:   " + currentWhitelist
                            + "\n    Append Same PkgName: " + appendSamePkgNameList
                            + "\n    Append:              " + append
                            + "\n    Result Status:       " + resultStatus
                            + "\n    Result Detail:       " + resultDetail);

                    if (append)
                        sendMessage(MSG.WHITELIST_REPLACE);
                    else
                        sendMessage(MSG.WHITELIST_REMOVE);
                    break;
                }
                case DELETE_WHITELIST: {
                    deletedWhitelist = intent.getStringArrayListExtra("DELETED_WHITELIST");
                    currentWhitelist = intent.getStringArrayListExtra("CURRENT_WHITELIST");
                    pkgNameNotFoundList = intent.getStringArrayListExtra("PKGNAMES_NOT_FOUND_LIST");
                    Config.WHITELIST = new ArrayList<>(currentWhitelist);

                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.d(TAG, "onReceive: -- DELETE_WHITELIST"
                            + "\n    Current Whitelist: " + currentWhitelist
                            + "\n    Remove PkgName:    " + deletedWhitelist
                            + "\n    Not Found PkgName: " + pkgNameNotFoundList
                            + "\n    Result Status:     " + resultStatus
                            + "\n    Result Detail:     " + resultDetail);

                    sendMessage(MSG.WHITELIST_REMOVE_ALL);
                    break;
                }
                case DELETE_ALL_WHITELIST: {
                    deletedWhitelist = intent.getStringArrayListExtra("DELETED_WHITELIST");
                    Config.WHITELIST = new ArrayList<>();
                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.d(TAG, "onReceive: -- DELETE_ALL_WHITELIST"
                            + "\n    Remove PkgName: " + deletedWhitelist
                            + "\n    Result Status:  " + resultStatus
                            + "\n    Result Detail:  " + resultDetail);
                    sendMessage(MSG.APP_INSTALL);
                    break;
                }
                case INSTALL_PACKAGE: {
                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.d(TAG, "onReceive: -- INSTALL_PACKAGE"
                            + "\n    Package Name:  " + intent.getStringExtra("INSTALL_PKGNAME")
                            + "\n    Result Status: " + resultStatus
                            + "\n    Result Detail: " + resultDetail);

                    // if (resultStatus >= 100 && resultStatus <= 200)
                    //     if (DEBUG >= DEBUG_VERBOSE) Log.d(TAG, "onReceive: File Path: " + intent.getStringExtra("DOWNLOAD_FILE_PATH"));

                    // TODO: Count installed apps
                    if (ResultStatus.INSTALL_UNINSTALL_RESULT_STATUS_SUCCESS == resultStatus ||
                        ResultStatus.INSTALL_UNINSTALL_RESULT_STATUS_FAILURE == resultStatus ||
                        ResultStatus.DOWNLOAD_RESULT_STATUS_FAILED           == resultStatus) {
                        INSTALL_UNINSTALL_COUNT++;

                        Log.d(TAG, "    Install Count:        " + INSTALL_UNINSTALL_COUNT);
                        Log.d(TAG, "    APP_INSTALL_PKG size: " + Config.APP_INSTALL_PKG.size());

                        if (INSTALL_UNINSTALL_COUNT == Config.APP_INSTALL_PKG.size()) {
                            INSTALL_UNINSTALL_COUNT = -1000;
                            sendMessage(MSG.APP_UNINSTALL);
                        }
                    }
                    break;
                }
                case UNINSTALL_PACKAGE: {
                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.d(TAG, "onReceive: -- UNINSTALL_PACKAGE"
                            + "\n    Package Name:  " + intent.getStringExtra("UNINSTALL_PKGNAME")
                            + "\n    Result Status: " + resultStatus
                            + "\n    Result Detail: " + resultDetail);

                    // TODO: Count uninstalled apps
                    if (ResultStatus.INSTALL_UNINSTALL_RESULT_STATUS_SUCCESS == resultStatus ||
                        ResultStatus.INSTALL_UNINSTALL_RESULT_STATUS_FAILURE == resultStatus ) {
                        INSTALL_UNINSTALL_COUNT++;

                        Log.d(TAG, "    Uninstall Count:        " + INSTALL_UNINSTALL_COUNT);
                        Log.d(TAG, "    APP_UNINSTALL_PKG size: " + Config.APP_UNINSTALL_PKG.size());

                        if (INSTALL_UNINSTALL_COUNT == Config.APP_UNINSTALL_PKG.size()) {
                            INSTALL_UNINSTALL_COUNT = -1000;
                            sendMessage(MSG.DOWNLOAD_CONFIG);
                        }
                    }
                    break;
                }
                case GET_WHITELIST: {
                    currentWhitelist = intent.getStringArrayListExtra("CURRENT_WHITELIST");
                    whitelistOnOff = intent.getBooleanExtra("WHITELIST_ONOFF", false);

                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.d(TAG, "onReceive: currentWhitelist: " + currentWhitelist +
                            "\n\nwhitelistOnOff: " + whitelistOnOff +
                            "\n\nresultStatus: " + resultStatus +
                            "\n\nresultDetail: " + resultDetail + "\n\n");
                    break;
                }
                case SET_RETURN_TARGET: {
                    String returnTargetAction = intent.getStringExtra("RETURN_TARGET_ACTION");
                    String returnTargetPkgName = intent.getStringExtra("RETURN_TARGET_PKGNAME");

                    if (DEBUG >= DEBUG_VERBOSE)
                        Log.i(TAG, "onReceive: returnTargetAction: " + returnTargetAction +
                            "\n\nreturnTargetPkgName: " + returnTargetPkgName +
                            "\n\nresultStatus: " + resultStatus +
                            "\n\nresultDetail: " + resultDetail + "\n\n");
                    break;
                }
            } // switch
        } // if
    } // onReceive

    public void sendMessage(int what) {
        Message msg = new Message();
        msg.what    = what;
        mHandler.sendMessage(msg);
    }
}
