package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.service.Scan.AutoUpdateManager;
import com.prime.dtv.service.Scan.Scan;
import com.prime.dtv.utils.TVScanParams;

public class ScanCmdManager extends BaseManager {
    private static final String TAG = "ScanCmdManager" ;
    public static final int HANDLER_SCAN = 1;
    public static final int HANDLER_UPDATE_PMT = 11;
    public static final int HANDLER_START_MONITOR_TABLE = 12;
    public static final int HANDLER_STOP_MONITOR_TABLE = 13;
    private Scan scan=null;
    private AutoUpdateManager mAutoUpdateManager=null;
    private static int mStartMonitorFlag = 0;
    public ScanCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, ScanCmdManager.class);

        mAutoUpdateManager = new AutoUpdateManager(context, getPesiDtvFrameworkInterfaceCallback());
    }

    public static int getStartMonitorFlag() {
        return mStartMonitorFlag;
    }

    /*
    scan
     */
    public void startScan(TVScanParams sp) {
        stopMonitorTable(-1, -1);
        Message msg = Message.obtain();
        msg.what = HANDLER_SCAN;
        msg.obj = sp;
        DoCommand(msg);
    }

    public void startMonitorTable(long channelId, int isFcc, int tuner_id) {
        Message msg = Message.obtain();
        msg.what = HANDLER_START_MONITOR_TABLE;
        msg.arg1 = isFcc;
        msg.arg2 = tuner_id;
        msg.obj = channelId;
        DoCommand(msg);
    }

    public void stopMonitorTable(long channelId, int tuner_id) {
        Message msg = Message.obtain();
        msg.what = HANDLER_STOP_MONITOR_TABLE;
        msg.obj = channelId;
        msg.arg2 = tuner_id;
        DoCommand(msg);
        mStartMonitorFlag = 0;
    }

    public void UpdatePMT(long channelId) {
        Message msg = Message.obtain();
        msg.what = HANDLER_UPDATE_PMT;
        msg.obj = channelId;
        DoCommand(msg);
    }

    public void VMXstartScan(TVScanParams sp, int startTPID, int endTPID) {

    }

    public void stopScan(boolean store) {
        if(store==true)
            scan.AddToDataManager(true);
        if(scan!=null)
            scan.abort();
    }


    @Override
    public void BaseHandleMessage(Message msg) {
        switch(msg.what) {
            case HANDLER_SCAN: {
                TVScanParams sp=(TVScanParams)msg.obj;
                scan = new Scan(getApplicationContext(), sp, getPesiDtvFrameworkInterfaceCallback());
                Log.d(TAG,"HANDLER_SCAN obj="+msg.obj.toString()+" --start");
                scan.startScan();
                Log.d(TAG,"HANDLER_SCAN obj="+msg.obj.toString()+ "--end");
            }break;
            case HANDLER_START_MONITOR_TABLE:{
                long channelId = (long)msg.obj;
                int isFcc = msg.arg1;
                mStartMonitorFlag = 1;
                mAutoUpdateManager.StartTableMonitor(channelId, isFcc, msg.arg2);
            }break;
            case HANDLER_STOP_MONITOR_TABLE:{
                long channelId = (long)msg.obj;
                mAutoUpdateManager.StopTableMonitor(channelId, msg.arg2);
            }break;
            default: {
                Log.d(TAG,"unknow msg what = "+msg.what);
            }
        }
    }
}
