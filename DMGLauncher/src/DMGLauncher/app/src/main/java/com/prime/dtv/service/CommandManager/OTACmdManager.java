package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.prime.dtv.Interface.BaseManager;

public class OTACmdManager extends BaseManager {
    private static final String TAG = "OTACmdManager" ;
    public OTACmdManager(Context context, Handler handler) {
        super(context, TAG, handler, OTACmdManager.class);
    }

    /*
    OTA
     */
    public int UpdateUsbSoftWare(String filename) {
        return 0;
    }

    public int UpdateFileSystemSoftWare(String pathAndFileName, String partitionName) {
        return 0;
    }

    public int UpdateOTADVBCSoftWare(int tpId, int freq, int symbol, int qam) {
        return 0;
    }

    public int UpdateOTADVBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority) {
        return 0;
    }

    public int UpdateOTADVBT2SoftWare(int tpId, int freq, int symbol, int qam, int channelmode) {
        return 0;
    }

    public int UpdateOTAISDBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority) {
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }
}
