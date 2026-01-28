// ITVService.aidl
package com.prime.tvservice;

// Declare any non-default types here with import statements
import com.prime.utils.TVScanParams;
import com.prime.utils.TVTunerParams;
import com.prime.utils.ITVCallback;
interface ITVService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void registerCallback(ITVCallback cb);
    void unregisterCallback(ITVCallback cb);

    void StartScan(in TVScanParams param);
    void StopScan(boolean store);

    int GetStrength(int tuner_id);
    int GetQuality(int tuner_id);
    int GetLockStatus(int tuner_id);
    int TuneFrontEnd(in TVTunerParams tp);

    void SendEPGUpdateMsg(int serviceType, int serviceChNum);   // johnny test send epgupdate msg 20171211
    int GetBER(int tunerId);
    int GetSNR(int tunerId);
}
