package com.prime.homeplus.settings;

import android.content.Context;
import android.media.tv.TvContentRating;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.datastructure.sysdata.SystemInfo;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PrimeUtils {
    public static PrimeDtvServiceInterface g_prime_dtv = PrimeApplication.get_prime_dtv_service();

    private static TpInfo get_tp_info_by_param(int freq, int symbol, int modulation){
        List<TpInfo> tpList =
                g_prime_dtv.tp_info_get_list_by_satId(
                        g_prime_dtv.get_tuner_type(),
                        MiscDefine.TpInfo.NONE_SAT_ID,
                        MiscDefine.TpInfo.POS_ALL,
                        MiscDefine.TpInfo.NUM_ALL);
        //LogUtils.d("start_scan tpList[0] = "+tpList.get(0).ToString());
        TpInfo tpInfo = null;
        int tp_id = -1;
        //LogUtils.d("tpList size = "+tpList.size());
        for(int i=0 ; i<tpList.size() ; i++)
        {
            tpInfo = tpList.get(i);
            //LogUtils.d("freq = "+tpInfo.CableTp.getFreq());
            if ( tpInfo.CableTp.getFreq() == freq) {
                tp_id = i;
                break;
            }
        }

        if (tpInfo == null)
            tpInfo = new TpInfo();

        tpInfo.CableTp.setFreq(freq);
        tpInfo.CableTp.setSymbol(symbol);
        tpInfo.CableTp.setQam(modulation);

        LogUtils.d(" tp_id = "+tp_id);

        if(tp_id == -1) {
            tpInfo.setTunerType(TpInfo.DVBC);
            tp_id = g_prime_dtv.tp_info_add(tpInfo);// add tp
            LogUtils.d("new tp_id = "+tp_id);
        }
        else
            g_prime_dtv.tp_info_update(tpInfo); // update tp
        g_prime_dtv.save_table(EnTableType.TP); // save tp to database
        return tpInfo;
    }

    public static int tuner_get_strength(int tuner_id){
        int strength = g_prime_dtv.get_signal_strength(tuner_id)/ 1000 + 107;//dBuV;
        LogUtils.d("tuner_id "+tuner_id+" strength "+strength);
        return strength;
    }

    public static int tuner_get_snr(int tuner_id){
        int snr = g_prime_dtv.get_signal_snr(tuner_id)/ 1000;//dB
        LogUtils.d("tuner_id "+tuner_id+" snr "+snr);
        return snr;
    }

    public static double tuner_get_ber(int tuner_id){
        double ber = g_prime_dtv.get_signal_ber(tuner_id)/ 1000000d;
        LogUtils.d("tuner_id "+tuner_id+" ber "+ber);
        return ber;
    }

    public static boolean tuner_get_lock(int tuner_id){
        boolean islock = g_prime_dtv.get_tuner_status(tuner_id);
        LogUtils.d("tuner_id "+tuner_id+" islock "+islock);
        return islock;
    }

    public static void tune_lock(int tunerId, int freq, int symbol, int modulation){;
        LogUtils.d("tunerId = "+tunerId+" freq = "+freq+" sym = "+symbol);

        int tp_id = get_tp_info_by_param(freq, symbol, modulation).getTpId();

        if(tp_id != -1) {
            TVTunerParams tvTunerParams = TVTunerParams.CreateTunerParamDVBC(tunerId, MiscDefine.TpInfo.NONE_SAT_ID, tp_id, freq, symbol, modulation);
            g_prime_dtv.tuner_lock(tvTunerParams);
        }

    }

    public static void save_scan_result(Context context){
        boolean isLock = g_prime_dtv.get_tuner_status(0);
        com.prime.homeplus.settings.LogUtils.d( "isLock = "+isLock);
        if (isLock) {

            g_prime_dtv.stop_scan(true);
            g_prime_dtv.save_table(EnTableType.PROGRAME); // save program to database
            g_prime_dtv.save_table(EnTableType.TP);
            g_prime_dtv.save_table(EnTableType.GROUP);

            GposInfo gposInfo = g_prime_dtv.gpos_info_get();
            GposInfo.setChannelLockCount(context, 0); // reset locked channels to 0
            GposInfo.resetTimeLockPeriods(context); // reset all time lock periods to -1
            //g_prime_dtv.save_table(EnTableType.GPOS);
            g_prime_dtv.av_control_change_channel_manager_list_update(FavGroup.ALL_TV_TYPE);
            //g_ch_change_manager.update_all_channel();
//            int tunerType = g_prime_dtv.get_tuner_type();
//                        TIFChannelData.insertChannels(this,g_ch_change_manager.get_all_channel_list(),
//                                PrimeTvInputAppApplication.getTvInputId(),tunerType);
            //g_ch_change_manager.reset_fcc();
            //g_ch_change_manager.change_channel_to_1st();
            g_prime_dtv.setup_epg_channel(); // send updated channels for epg
            g_prime_dtv.backupDatabase(true);
            //scan_complete(msg);
        }
        else {
            //g_channel_scan_view.update_scan_no_signal();
        }

        //g_scan_complete = true;
    }

    public static void start_scan(final int freq, final int sr, int bw, final int modulation, int tunerId, boolean isSingleFreq) {
        TpInfo tpInfo = get_tp_info_by_param(freq, sr, modulation);

        int scanMode ;//= (isSingleFreq)?TVScanParams.SCAN_MODE_MANUAL:TVScanParams.SCAN_DMG_SEARCH;
        if(isSingleFreq){
            scanMode = TVScanParams.SCAN_MODE_MANUAL;
        }else{
            scanMode = TVScanParams.SCAN_CNS_SEARCH;
        }
        TVScanParams params = new TVScanParams(
                tunerId, tpInfo, 0,
                scanMode, TVScanParams.SEARCH_OPTION_ALL, TVScanParams.SEARCH_OPTION_ALL,
                0, 0);
        g_prime_dtv.stopMonitorTable(-1, -1);
        com.prime.homeplus.settings.LogUtils.d("start_scan tpList = "+tpInfo.ToString());
        g_prime_dtv.start_scan(params);
    }

    public static void do_factory_reset(){
        LogUtils.d(" ");
        g_prime_dtv.do_factory_reset();
    }

    public static void set_Ethernet_static_ip(String ipStr, int prefixLength, String gatewayStr, String dnsStr) {
        g_prime_dtv.set_Ethernet_static_ip(ipStr, prefixLength, gatewayStr, dnsStr);
    }

    public static void set_Ethernet_dhcp(){
        g_prime_dtv.set_Ethernet_dhcp();
    }

    public static GposInfo get_gpos_info(){
        if(g_prime_dtv != null){
            return g_prime_dtv.gpos_info_get();
        }
        return null;
    }

    public static SystemInfo get_system_info(Context context){
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        String macs[] = g_prime_dtv.get_mac();
        LogUtils.d("macs[0] = "+macs[0]+" macs[1] = "+macs[1]);
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.device_sn = Pvcfg.get_device_sn();
        if(macs[0] != null && !macs[0].isEmpty())
            systemInfo.Ethernet_mac = macs[0].replaceAll("..(?!$)", "$0:");
        if(macs[1] != null && !macs[1].isEmpty())
            systemInfo.Wifi_mac = macs[1].replaceAll("..(?!$)", "$0:");
        systemInfo.SW_version = Pvcfg.get_firmware_version();
        systemInfo.Loader_version = Pvcfg.get_loader_firmware_version();
        if(gposInfo != null) {
            systemInfo.AreaCode = GposInfo.getAreadCode(context);
            systemInfo.CmMode = GposInfo.getCmMode(context);
            systemInfo.BouquetId = GposInfo.getBatId(context);
            systemInfo.Zip_code = GposInfo.getZipCode(context);
        }
        systemInfo.CA_sn = Pvcfg.get_ca_sn();
        systemInfo.HDD_sn = g_prime_dtv.get_hdd_serial().replaceAll("\\R+$", "");
        LogUtils.d("HDD_sn = "+systemInfo.HDD_sn);
        return systemInfo;
    }

    public static String get_hdd_serial(){
        return g_prime_dtv.get_hdd_serial().replaceAll("\\R+$", "");
    }

    public static Set<TvContentRating> getRatings(){
        return g_prime_dtv.tv_input_manager_get_ratings();
    }

    public static void setParentalRatingEnable(boolean enable){
        g_prime_dtv.tv_input_manager_set_parental_rating_enable(enable);
    }

    public static void removeAllRatings(){
        g_prime_dtv.tv_input_manager_remove_all_ratings();
    }

    public static void addRatings(TvContentRating inputContentRating){
        g_prime_dtv.tv_input_manager_add_ratings(inputContentRating);
    }

    public static void set_system_language(Context context, String language) {
        String osdLang;
        if(language.equals("en")) {
            osdLang = "eng";
        }else{
            osdLang = "chi";
        }
        get_gpos_info().setOSDLanguage(context, osdLang);
        //g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_OSD_LANGUAGE, osdLang);
        g_prime_dtv.set_system_language(language);

    }
	
    public static void set_aspect_ratio(int ratio){
        g_prime_dtv.av_control_set_aspect_ratio(ratio);
    }

    public static void set_hdcp_level(int level){
        g_prime_dtv.set_dcp_level(level);
    }

    public static void set_hdmicec_enable(boolean enable){
        g_prime_dtv.set_hdmi_cec(enable?1:0);
    }

    public static String[] get_supported_resolution_list(Context context){
        int list_cnt = g_prime_dtv.get_hdmi_output_format();
        if (list_cnt <= 0) {
            // fix crash if get_hdmi_output_format() return <= 0
            list_cnt = 1;
        }

        String[] resolutionList = new String[list_cnt];

        resolutionList[0] = context.getString(R.string.settings_av_setting_auto);
        if(list_cnt >5)  resolutionList[5] = "4K P(60Hz)";
        if(list_cnt >4)  resolutionList[4] = "4K P(30Hz)";
        if(list_cnt >3)  resolutionList[3] = "1080P";
        if(list_cnt >2)  resolutionList[2] = "720P";
        if(list_cnt >1)  resolutionList[1] = "480P";

        return resolutionList;
    }

    public static void set_hdmi_output_format(int output_format){
        g_prime_dtv.set_hdmi_output_format(output_format);
    }

    public static void start_ota_update() {
        g_prime_dtv.start_ota_update();
    }

    public static List<String> get_entitlement_list(){
        return g_prime_dtv.get_entitled_channel_ids();
    }

    public static List<String> get_promotion_list(){
        return g_prime_dtv.get_promotion_channel_ids();
    }

    public static CasData get_Widevine_CasData(){
        LogUtils.d("get_cas_data");
        return g_prime_dtv.get_cas_data();
    }
}
