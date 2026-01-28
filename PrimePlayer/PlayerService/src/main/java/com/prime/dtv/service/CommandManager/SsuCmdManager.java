package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.prime.datastructure.sysdata.OTACableParameters;
import com.prime.datastructure.sysdata.OTATerrParameters;
import com.prime.dtv.Interface.BaseManager;

public class SsuCmdManager extends BaseManager {
    private static final String TAG = "SsuCmdManager" ;
    public SsuCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, SsuCmdManager.class);
    }

    public int LoaderDtvGetJTAG() {
        return 0;
    }

    public int LoaderDtvSetJTAG(int value) {
        return 0;
    }

    public int LoaderDtvCheckISDBTService(OTATerrParameters ota) {
        return 0;
    }

    public int LoaderDtvCheckTerrestrialService(OTATerrParameters ota) {
        return 0;
    }

    public int LoaderDtvCheckCableService(OTACableParameters ota) {
        return 0;
    }

    public int LoaderDtvGetSTBSN() {
        return 0;
    }

    public int LoaderDtvGetChipSetId() {
        return 0;
    }

    public int LoaderDtvGetSWVersion() {
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }
}
