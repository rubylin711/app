package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.os.Bundle;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.CommonModule;
import com.prime.datastructure.sysdata.SystemInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;
import com.prime.dtvservice.PrimeDtvServiceApplication;

public class CommonModuleCommand {
    private static final String TAG = "CommonModuleCommand";
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle,Bundle replyBundle,PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = "+command_id);
        switch(command_id) {
            case CommonModule.CMD_ServicePlayer_COMMON_SetModuleType:
                Log.e(TAG,"CommonModule.CMD_ServicePlayer_COMMON_SetModuleType not porting");
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_GetModuleType:
                Log.e(TAG,"CommonModule.CMD_ServicePlayer_COMMON_GetModuleType not porting");
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_GetLibVer:
                replyBundle = getLibVer(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_FactoryReset:
                replyBundle = factory_reset(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_SetEthernetStaticIP:
                replyBundle = set_ethernet_static_ip(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_SetEthernetDHCP:
                replyBundle = set_ethernet_dhcp(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_InitService:
                replyBundle = init_service(requestBundle,replyBundle);
				break;
            case CommonModule.CMD_ServicePlayer_COMMON_GetMac:
                replyBundle = get_mac(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_GetHddSeries:
                replyBundle = get_hdd_series(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_SetSystemLanguage:
                replyBundle = set_system_lanaguage(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_SetHdmiCec:
                replyBundle = set_hdmi_cec(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_SetHDCPLevel:
                replyBundle = set_dcp_level(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_GetHdmiOutputFormat:
                replyBundle = get_hdmi_output_format_count(requestBundle,replyBundle);
                break;
            case CommonModule.CMD_ServicePlayer_COMMON_SetHdmiOutputFormat:
                replyBundle = set_hdmi_output_format(requestBundle,replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    private Bundle set_hdmi_cec(Bundle requestBundle, Bundle replyBundle) {
        int cec = requestBundle.getInt("HDMICEC",0);
        primeDtv.set_hdmi_cec(cec);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle set_dcp_level(Bundle requestBundle, Bundle replyBundle) {
        int level = requestBundle.getInt("HDCPLEVEL",0);
        primeDtv.set_dcp_level(level);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle set_hdmi_output_format(Bundle requestBundle, Bundle replyBundle) {
        int format = requestBundle.getInt("HDMIOUTPUTFORMAT",0);
        primeDtv.set_hdmi_output_format(format);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle get_hdmi_output_format_count(Bundle requestBundle, Bundle replyBundle) {
        int output_count = primeDtv.get_hdmi_output_format_count();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt("HDMIOUTPUTFORMATCOUNT", output_count);
        return replyBundle;
    }

    private Bundle set_system_lanaguage(Bundle requestBundle, Bundle replyBundle) {
        String language = requestBundle.getString("LanguageCode","en");
        primeDtv.set_system_language(language);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle init_service(Bundle requestBundle, Bundle replyBundle) {
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        PrimeDtvServiceApplication.primeDtvInitDone();
        return replyBundle;
    }

    private Bundle set_ethernet_static_ip(Bundle requestBundle, Bundle replyBundle) {
        String ip = requestBundle.getString("static_ip", "0.0.0.0");
        int netMask_prefixLength = requestBundle.getInt("static_ip_prefixLength", 24);
        String gateway = requestBundle.getString("static_ip_geteway", "0.0.0.0");
        String dns = requestBundle.getString("static_ip_dns", "0.0.0.0");
        primeDtv.set_Ethernet_static_ip(ip, netMask_prefixLength, gateway,dns);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle get_hdd_series(Bundle requestBundle, Bundle replyBundle){
        String hdd_serial = primeDtv.get_hdd_serial();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putString("hdd_series",hdd_serial);
        return replyBundle;
    }

    private Bundle get_mac(Bundle requestBundle, Bundle replyBundle){
        String mac1 = primeDtv.get_Ethernet_mac();
        String mac2 = primeDtv.get_Wifi_mac();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putString("Ethernet_mac",mac1);
        replyBundle.putString("Wifi_mac",mac2);
        return replyBundle;
    }

    private Bundle set_ethernet_dhcp(Bundle requestBundle, Bundle replyBundle){
        primeDtv.set_Ethernet_dhcp();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    public Bundle getLibVer(Bundle requestBundle,Bundle replyBundle) {
        // 直接存取剛剛定義的欄位
        String playerServiceVersionName = com.prime.dtv.BuildConfig.VERSION_NAME;
        int playerServiceVersionCode = com.prime.dtv.BuildConfig.VERSION_CODE;

        String dataStructureVersionName = com.prime.datastructure.BuildConfig.VERSION_NAME;
        int dataStructureVersionCode = com.prime.datastructure.BuildConfig.VERSION_CODE;

        Log.e(TAG, "PrimeDtvService PlayerService 的版本是: " + playerServiceVersionName);
        Log.e(TAG, "PrimeDtvService DataStructure 的版本是: " + dataStructureVersionName);

        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putString(CommonModule.DATASTRUCTURE_PACKAGE_NAME,com.prime.datastructure.BuildConfig.VERSION_NAME);
        replyBundle.putString(CommonModule.PLAYERSERVICE_PACKAGE_NAME,com.prime.dtv.BuildConfig.VERSION_NAME);
        return replyBundle;
    }

    public Bundle factory_reset(Bundle requestBundle,Bundle replyBundle){
        LogUtils.d(" ");
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        primeDtv.do_factory_reset();
        return replyBundle;
    }
}
