package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.ScanModule;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.PrimeDtv;

public class ScanModuleCommand {
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle,Bundle replyBundle,PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = "+command_id);
        switch(command_id) {
            case ScanModule.CMD_ServicePlayer_SCAN_StartScan:
                replyBundle = start_scan(requestBundle,replyBundle);
                break;
            case ScanModule.CMD_ServicePlayer_SCAN_StopScan:
                replyBundle = stop_scan(requestBundle,replyBundle);
                break;
//            case ScanModule.CMD_ServicePlayer_SCAN_CALLBACK_TEST:
//                replyBundle = callback_test(requestBundle,replyBundle);
//                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    public Bundle start_scan(Bundle requestBundle,Bundle replyBundle) {
        TVScanParams tvScanParams = requestBundle.getParcelable(TVScanParams.TAG,TVScanParams.class);
        long start = System.currentTimeMillis();
        primeDtv.start_scan(tvScanParams);
        long end = System.currentTimeMillis();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle stop_scan(Bundle requestBundle,Bundle replyBundle) {
        boolean store = requestBundle.getBoolean(ScanModule.Store_string,false);
        primeDtv.stop_scan(store);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);;
        return replyBundle;
    }
}
