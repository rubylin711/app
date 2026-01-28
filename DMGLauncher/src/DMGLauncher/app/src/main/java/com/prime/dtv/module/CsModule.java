package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.EnNetworkType;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.TVScanParams;

import java.util.Locale;

public class CsModule {
    private static final String TAG = "CsModule";

    private static final int CMD_CS_Base = PrimeDtvMediaPlayer.CMD_Base + 0x1000;

    //CS
    private static final int CMD_CS_SingleFreqSearch = CMD_CS_Base + 0x01;
    private static final int CMD_CS_SegmentSearch = CMD_CS_Base + 0x02;
    private static final int CMD_CS_AutoSearch = CMD_CS_Base + 0x03;
    private static final int CMD_CS_NITSearch = CMD_CS_Base + 0x04;
    private static final int CMD_CS_MultiTransponderSearch = CMD_CS_Base + 0x05;
    private static final int CMD_CS_SingleSatBlindSearch = CMD_CS_Base + 0x06;
    private static final int CMD_CS_SteppingSearch = CMD_CS_Base + 0x07;
    private static final int CMD_CS_StopSearch = CMD_CS_Base + 0x08;
    private static final int CMD_CS_PauseSearch = CMD_CS_Base + 0x09;
    private static final int CMD_CS_ResumeSearch = CMD_CS_Base + 0x0A;
    private static final int CMD_CS_GetSearchProgress = CMD_CS_Base + 0x0B;
    private static final int CMD_CS_GetSearchStatisticalInfo = CMD_CS_Base + 0x0C;
    private static final int CMD_CS_AllSatSearch = CMD_CS_Base + 0x0D;
    private static final int CMD_CS_BlockSearch = CMD_CS_Base + 0x0E;

    public void start_scan(TVScanParams sp) {
        Log.d(TAG, "start_scan");
        int ret;
        if (sp.getScanMode() == TVScanParams.SCAN_MODE_AUTO) {
            if (sp.getTpInfo().getTunerType() == TpInfo.DVBC) {
                ret = dvbc_auto_scan(sp);
                Log.d(TAG, "start_scan dvbcAutoScan. Ret = " + ret);
            } else if (sp.getTpInfo().getTunerType() == TpInfo.DVBS) {
                ret = dvbs_scan_selected_sat(sp);
                Log.d(TAG, "start_scan dvbsScanSelectedSat. Ret = " + ret);
            } else if (sp.getTpInfo().getTunerType() == TpInfo.DVBT) {
                ret = dvbt_auto_scan(sp);
                Log.d(TAG, "start_scan dvbtAutoScan. Ret = " + ret);
            } else if (sp.getTpInfo().getTunerType() == TpInfo.ISDBT) {
                ret = isdbt_auto_scan(sp);
                Log.d(TAG, "start_scan isdbtAutoScan. Ret = " + ret);
            }
        } else if (sp.getScanMode() == TVScanParams.SCAN_MODE_MANUAL) {
            if (sp.getTpInfo().getTunerType() == TpInfo.DVBC) {
                ret = dvbc_single_freq_scan(sp);
                Log.d(TAG, "start_scan dvbcSingleFreqScan. Ret = " + ret);
            } else if (sp.getTpInfo().getTunerType() == TpInfo.DVBS) {
                ret = dvbs_search_tp(sp);
                Log.d(TAG, "start_scan dvbsSearchTp. Ret = " + ret);
            } else if (sp.getTpInfo().getTunerType() == TpInfo.DVBT) {
                ret = dvbt_single_freq_scan(sp);
                Log.d(TAG, "start_scan dvbtSingleScan. Ret = " + ret);
            } else if (sp.getTpInfo().getTunerType() == TpInfo.ISDBT) {
                ret = isdbt_single_freq_scan(sp);
                Log.d(TAG, "start_scan isdbtSingleScan. Ret = " + ret);
            }
        } else if (sp.getScanMode() == TVScanParams.SCAN_MODE_NETWORK) {
            if (sp.getTpInfo().getTunerType() == TpInfo.DVBS) {
                ret = dvbs_nit_search(sp);
                Log.d(TAG, "start_scan dvbsNitSearch. Ret = " + ret);
            }
        } else if (sp.getScanMode() == TVScanParams.SCAN_MODE_ALL_SAT) {
            if (sp.getTpInfo().getTunerType() == TpInfo.DVBS) {
                ret = dvbs_search_all_sat(sp); // Johnny 20180813 modify to search all sat in all cases
                Log.d(TAG, "start_scan dvbsSearchAllSat. Ret = " + ret);
            }
        } else {
            Log.d(TAG, "start_scan: unknown scanmode");
        }
    }

    public void stop_scan(boolean store) {
        Log.d(TAG, "stop_scan: store = " + store);
        PrimeDtvMediaPlayer.excute_command(CMD_CS_StopSearch, store ? 1 : 0);
    }

    private int dvbc_single_freq_scan(TVScanParams sp) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        if (sp.getNitSearch() == 1)  // enable nitSearch
        {
            request.writeInt(CMD_CS_NITSearch);
        } else {
            Log.d(TAG, "dvbc_single_freq_scan: CMD_CS_SingleFreqSearch " + CMD_CS_SingleFreqSearch);
            Log.d(TAG, "sp.getSatId() : " + sp.getSatId() + " sp.getTunerId() : " + sp.getTunerId()
                    + " EnNetworkType.CABLE.getValue() : " + EnNetworkType.CABLE.getValue());
            Log.d(TAG, /*tp.getID()*/"sp.getTpInfo().getTpId() : " + sp.getTpInfo().getTpId()
                    + " sp.getTpInfo().CableTp.getSymbol() : " + sp.getTpInfo().CableTp.getSymbol()
                    + " sp.getTpInfo().CableTp.getQam() : " + sp.getTpInfo().CableTp.getQam());
            request.writeInt(CMD_CS_SingleFreqSearch);
        }

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.CABLE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().CableTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().CableTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/sp.getTpInfo().CableTp.getQam());
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int dvbc_auto_scan(TVScanParams sp) {

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        if (sp.getNitSearch() == 1)  // enable nitSearch
        {
            request.writeInt(CMD_CS_NITSearch);
        } else {
            request.writeInt(CMD_CS_AutoSearch);
        }

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.CABLE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().CableTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().CableTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/sp.getTpInfo().CableTp.getQam());
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int isdbt_single_freq_scan(TVScanParams sp) {
        /*List<Multiplex> lstTPs = terObj.getScanMultiplexes();

        if ((null == lstTPs) || (1 != lstTPs.size()))
        {
            Log.e(TAG, "error parameters,the scan tiplist param errror,lstTPs=" + lstTPs);
            return -2;
        }*/

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        // johnny modify 2080418 for nit search
        if (/*type.isEnableNit()*/sp.getNitSearch() == 1) {
            request.writeInt(CMD_CS_NITSearch);
        } else {
            request.writeInt(CMD_CS_SingleFreqSearch);
        }

        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.ISDB_TER.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(/*type.getTransmissionTypeFilter().getValue()*/sp.getOneSegment());
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(/*tp.getBandWidth()*/sp.getTpInfo().TerrTp.getBand());
        request.writeInt(/*tp.getModulation().getValue()*/0);   // ignore
        request.writeInt(/*tp.getVersion().ordinal()*/0);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int isdbt_auto_scan(TVScanParams sp) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        request.writeInt(CMD_CS_AutoSearch);

        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.ISDB_TER.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(/*type.getTransmissionTypeFilter().getValue()*/sp.getOneSegment());
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int dvbt_single_freq_scan(TVScanParams sp) {
        /*List<Multiplex> lstTPs = terObj.getScanMultiplexes();

        if ((null == lstTPs) || (1 != lstTPs.size()))
        {
            Log.e(TAG, "error parameters,the scan tiplist param errror,lstTPs=" + lstTPs);
            return -2;
        }*/

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        if (/*type.isEnableNit()*/sp.getNitSearch() == 1) {
            request.writeInt(CMD_CS_NITSearch);
            //input freq flag
            //request.writeInt(1);
        } else {
            request.writeInt(CMD_CS_SingleFreqSearch);
        }

        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.TERRESTRIAL.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(/*tp.getBandWidth()*/sp.getTpInfo().TerrTp.getBand());
        request.writeInt(/*tp.getModulation().getValue()*/0);   // ignore
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int dvbt_auto_scan(TVScanParams sp) {
        /*List<Multiplex> lstTPs = terObj.getScanMultiplexes();
        int inputFlag = 0;
        if ((null != lstTPs) && (0 < lstTPs.size()))
        {
            inputFlag = 1;
        }*/

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        if (/*type.isEnableNit()*/sp.getNitSearch() == 1) {
            request.writeInt(CMD_CS_NITSearch);
            //input freq flag
            //request.writeInt(inputFlag);
        } else {
            request.writeInt(CMD_CS_AutoSearch);
        }
        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.TERRESTRIAL.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

//        if (1 == inputFlag)
//        {
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(/*tp.getBandWidth()*/sp.getTpInfo().TerrTp.getBand());
        request.writeInt(/*tp.getModulation().getValue()*/0);   // ignore
        request.writeInt(/*tp.getVersion().ordinal()*/0);
//        }


        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    // Pesi uses CMD_CS_AutoSearch to do dvbs single Sat search
    private int dvbs_scan_selected_sat(TVScanParams sp) {
        Log.d(TAG, "dvbs_scan_selected_sat(" + sp.getSatId() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(/*CMD_CS_SingleSatBlindSearch*/CMD_CS_AutoSearch);    // Johnny 20180813 modify to use CMD_CS_AutoSearch in singleSatelliteScan

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "dvbsScanSelectedSat: " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    // Pesi uses CMD_CS_SingleFreqSearch to do dvbs single Tp search
    private int dvbs_search_tp(TVScanParams sp) {
        Log.d(TAG, "dvbs_search_tp: ");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(/*CMD_CS_MultiTransponderSearch*/CMD_CS_SingleFreqSearch);  // Johnny 20180813 modify to use CMD_CS_SingleFreqSearch in satelliteTPScan

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().SatTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().SatTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/sp.getTpInfo().SatTp.getPolar()); // Johnny 20180813 modify DVBS Qam to Polar
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "dvbs_search_tp: ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int dvbs_search_all_sat(TVScanParams sp) {
        Log.d(TAG, "dvbs_search_all_sat(" + sp.getSatId() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CS_AllSatSearch);

        request.writeInt(/*satellite.getID()*/sp.getSatId());
        request.writeInt(/*satellite.getTunerID()*/sp.getTunerId());

        request.writeInt(/*satellite.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "dvbs_search_all_sat: " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int dvbs_nit_search(TVScanParams sp) {
        Log.d(TAG, "dvbs_nit_search(" + sp.getSatId() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CS_NITSearch);

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().SatTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().SatTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/sp.getTpInfo().SatTp.getPolar()); // Johnny 20180813 modify DVBS Qam to Polar
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "dvbs_nit_search: " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }


    public void vmx_start_scan(TVScanParams sp, int startTPID, int endTPID) // connie 20180919 add for vmx search
    {
        Log.d(TAG, "vmx_start_scan");
        int ret;
        if (sp.getTpInfo().getTunerType() == TpInfo.DVBC) {
            ret = dvbc_vmx_scan(sp, startTPID, endTPID);
            Log.d(TAG, "vmx_start_scan dvbcVMXScan. Ret = " + ret);
        } else if (sp.getTpInfo().getTunerType() == TpInfo.DVBS) {
            ret = dvbs_vmx_scan(sp, startTPID, endTPID);
            Log.d(TAG, "vmx_start_scan dvbsVMXScan. Ret = " + ret);
        } else if (sp.getTpInfo().getTunerType() == TpInfo.DVBT) {
            ret = dvbt_vmx_scan(sp, startTPID, endTPID);
            Log.d(TAG, "vmx_start_scan dvbtVMXScan. Ret = " + ret);
        } else if (sp.getTpInfo().getTunerType() == TpInfo.ISDBT) {
            ret = isdbt_vmx_scan(sp, startTPID, endTPID);
            Log.d(TAG, "vmx_start_scan isdbtVMXScan. Ret = " + ret);
        }
    }

    private int dvbc_vmx_scan(TVScanParams sp, int startTPID, int endTPID) // connie 20180919 add for vmx search -s
    {
        Log.d(TAG, "dvbc_vmx_scan:    startTPID = " + startTPID + "     endTPID =" + endTPID);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        request.writeInt(CMD_CS_BlockSearch);

        // scan params
        request.writeInt(sp.getSatId());
        request.writeInt(sp.getTunerId());
        request.writeInt(EnNetworkType.CABLE.getValue());
        request.writeInt(sp.getSearchOptionCaFta());
        request.writeInt(sp.getSearchOptionTVRadio());

        Log.d(TAG, "dvbc_vmx_scan:   getSatId() = " + sp.getSatId() + "     getTunerId = " + sp.getTunerId() + "   NetworkType = " + EnNetworkType.CABLE.getValue()
                + "   getSearchOptionCaFta = " + sp.getSearchOptionCaFta() + "    getSearchOptionTVRadio=" + sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(sp.getTpInfo().getTpId());
        request.writeInt(sp.getTpInfo().CableTp.getFreq());
        request.writeInt(sp.getTpInfo().CableTp.getSymbol());
        request.writeInt(sp.getTpInfo().CableTp.getQam());
        request.writeInt(0);
        request.writeInt(startTPID);
        request.writeInt(endTPID);
        Log.d(TAG, "dvbc_vmx_scan:  getTpId() = " + sp.getTpInfo().getTpId() + "    getFreq() = " + sp.getTpInfo().CableTp.getFreq() + "   Qam = " + sp.getTpInfo().CableTp.getQam());

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int dvbs_vmx_scan(TVScanParams sp, int startTPID, int endTPID) {
        return 0;
    }

    private int dvbt_vmx_scan(TVScanParams sp, int startTPID, int endTPID) {
        return 0;
    }

    private int isdbt_vmx_scan(TVScanParams sp, int startTPID, int endTPID) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);

        // johnny modify 20180418 for nit search
        request.writeInt(CMD_CS_BlockSearch);
        request.writeInt(sp.getSatId());
        request.writeInt(sp.getTunerId());
        request.writeInt(EnNetworkType.ISDB_TER.getValue());
        request.writeInt(sp.getSearchOptionCaFta());
        request.writeInt(sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "language :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(sp.getOneSegment());
        request.writeInt(sp.getTpInfo().getTpId());
        request.writeInt(sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(sp.getTpInfo().TerrTp.getBand());
        request.writeInt(0);   // ignore
        request.writeInt(0);
        request.writeInt(startTPID);
        request.writeInt(endTPID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);

    }
}
