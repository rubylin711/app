package com.prime.dtv.service.Test;

import android.content.Context;

import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.service.Scan.Scan;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.TVScanParams;

public class ScanTest {
    public ScanTest (Context context){
        TpInfo tpInfo = new TpInfo(TpInfo.DVBC);
        tpInfo.setTpId(0);
        tpInfo.CableTp.setFreq(405000);
        tpInfo.CableTp.setSymbol(5217);
        tpInfo.CableTp.setQam(TpInfo.Cable.QAM_256);

        TVScanParams scanData = new TVScanParams(0, tpInfo, 0, 1, 0, 0, 0, 0);
        Scan scan = new Scan(context, scanData, BaseManager.getPesiDtvFrameworkInterfaceCallback());
        scan.startScan();
    }
}
