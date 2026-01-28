package com.prime.dtv.service.CNS;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.prime.datastructure.config.PropertyDefine;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.Util.Utils;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * IRD Command handler (Homeplus-TG-IRD Command v1.34)
 *
 * commandData (payload) format is a comma-separated string:
 * [mainCmd],[paraLength],[para1],[para2],...,[paraN]
 *
 * Examples:
 * - Reset STB PIN1: "7,0"
 * - Set STB PIN: "9,2,3,1234" (set PIN index=3 to 1234)
 * - Set static IP:
 * "16,4,192.168.100.100,255.255.0.0,192.168.100.254,192.168.100.1"
 *
 * NOTE:
 * - MPS decrypt output often contains trailing '\0' (NUL) bytes.
 * We MUST strip NULs before split/parse, otherwise args may contain hidden
 * characters.
 */
public class IrdCommand {
    public String TAG = "IrdCommand";
    byte[] mCommandData;
    long mChannelId;
    private Context mContext;

    public final int IrdCommand_7 = 7; // Reset STB PIN1
    public final int IrdCommand_8 = 8; // Reset STB PIN2
    public final int IrdCommand_9 = 9; // Set STB PIN
    public final int IrdCommand_10 = 10; // Disable STB Auto-standby
    public final int IrdCommand_11 = 11; // Enable STB Auto-standby
    public final int IrdCommand_12 = 12; // Reboot STB
    public final int IrdCommand_13 = 13; // Enable dynamic IP in Physical network
    public final int IrdCommand_14 = 14; // Enable static IP in Physical network for Master HMC-3000
    public final int IrdCommand_15 = 15; // Enable static IP in Physical network for Slave HMC-3000
    public final int IrdCommand_16 = 16; // Set static IP in Physical network for specific STB
    public final int IrdCommand_17 = 17; // Factory reset with FTI skip
    public final int IrdCommand_18 = 18; // Reset the download applications from App Store
    public final int IrdCommand_19 = 19; // Disable AD (advertisement) in Live TV
    public final int IrdCommand_20 = 20; // Enable AD (advertisement) in Live TV
    public final int IrdCommand_21 = 21; // Reset App Store URL to default
    public final int IrdCommand_22 = 22; // Change App Store URL
    public final int IrdCommand_23 = 23; // Set AP Router – Home Gateway
    public final int IrdCommand_24 = 24; // Update Entitled List by default URL – Home Gateway
    public final int IrdCommand_25 = 25; // Update Entitled List by specific URL – Home Gateway
    public final int IrdCommand_26 = 26; // Disable First Time Installation/FTI
    public final int IrdCommand_27 = 27; // Change VBM Report URL/IP
    public final int IrdCommand_28 = 28; // Enable Anti-pirate function
    public final int IrdCommand_29 = 29; // Disable Anti-pirate function
    public final int IrdCommand_30 = 30; // HDD Pairing and Provision – PVR
    public final int IrdCommand_31 = 31; // HDD Provision – PVR
    public final int IrdCommand_32 = 32; // HDD Formatting – PVR
    public final int IrdCommand_33 = 33; // Reset STB Download Mode
    public final int IrdCommand_34 = 34; // HDMI CEC Function
    public final int IrdCommand_35 = 35; // TV Fee Reminder STB Locked Function
    public final int IrdCommand_36 = 36; // HDCP Function
    public final int IrdCommand_37 = 37; // Area Limitation Function
    public final int IrdCommand_38 = 38; // TR069 Function
    public final int IrdCommand_39 = 39; // Rescan Channel
    public final int IrdCommand_40 = 40; // Set QR Error Code Reporting URL
    public final int IrdCommand_41 = 41; // ATV Group/Mode Setting
    public final int IrdCommand_42 = 42; // ATV AD Function Setting
    public final int IrdCommand_43 = 43; // STB Log Reporting Trigger
    public final int IrdCommand_44 = 44; // ATV debug Mode Setting
    public final int IrdCommand_45 = 45; // Tuner Signal Status Report
    public final int IrdCommand_46 = 46; // STB Volume Restore Cache Cleaning Reload Launcher Layout
    public final int IrdCommand_47 = 47; // STB Volume Restore Cache Cleaning Reload Launcher Layout
    public final int IrdCommand_48 = 48; // STB Volume Restore Cache Cleaning Reload Launcher Layout

    public IrdCommand(Context context, byte[] data, long channelId) {
        mCommandData = data;
        mChannelId = channelId;
        mContext = context;
    }

    public void doIrdCommand(PrimeDtv primeDtv) {
        if (primeDtv == null) {
            Log.w(TAG, "doIrdCommand: primeDtv is null");
            return;
        }

        GposInfo gposInfo = primeDtv.gpos_info_get();
        if (mCommandData == null || mCommandData.length == 0) {
            Log.w(TAG, "doIrdCommand: mCommandData is empty");
            return;
        }

        // 1) Convert bytes -> string, remove NULs, trim.
        // IMPORTANT: decrypt output often has trailing '\0', which breaks parsing if
        // not removed.
        String dataStr = stripNulAndTrim(new String(mCommandData, StandardCharsets.UTF_8));
        Log.d(TAG, "doIrdCommand: raw payload = [" + dataStr + "]");

        if (TextUtils.isEmpty(dataStr)) {
            Log.w(TAG, "doIrdCommand: payload is empty after stripNulAndTrim");
            return;
        }

        // 2) Split by comma, allowing spaces around comma.
        // Example: "16, 4, 192.168..., 255.255..." is supported.
        String[] args = dataStr.split("\\s*,\\s*");
        if (args.length < 2) {
            Log.w(TAG, "doIrdCommand: invalid format, need at least [mainCmd],[paraLength]");
            return;
        }

        // 3) Parse mainCmd and paraLength.
        // HDD Pairing and Provision – PVR 的 args[0] 過濾掉開頭的 '&' 符號
        if (args[0] != null && args[0].startsWith("&")) {
            Log.w(TAG, "doIrdCommand: Found leading '&' in commandId, removing it. Raw: " + args[0]);
            args[0] = args[0].substring(1).trim(); // 移除第一個字元並去除可能產生的空白
        }

        Integer commandId = tryParseInt(args[0]);
        Integer paraLength = tryParseInt(args[1]);
        if (commandId == null || paraLength == null) {
            Log.e(TAG, "doIrdCommand: mainCmd/paraLength parse failed, args[0]=" + args[0] + ", args[1]=" + args[1]);
            return;
        }

        // 4) Extract parameters based on paraLength (not based on args.length blindly).
        // If args has extra tokens, we join them into the last parameter (defensive).
        // This prevents accidental "extra commas" from shifting parameters.
        String[] params = extractParams(args, paraLength);

        if (params == null) {
            // extractParams already logged reason
            return;
        }

        Log.i(TAG, "doIrdCommand: cmd=" + commandId + ", paraLength=" + paraLength + ", paramsCount=" + params.length);

        // 5) Dispatch
        switch (commandId) {
            case IrdCommand_7:
                Log.d(TAG, "Reset STB PIN1");
                GposInfo.setPasswordValue(mContext, 0);
                // primeDtv.gpos_info_update_by_key_string("PasswordValue","0");
                break;
            case IrdCommand_8:
                Log.d(TAG, "Reset STB PIN2");
                GposInfo.setPurchasePasswordValue(mContext, 0);
                // primeDtv.gpos_info_update_by_key_string("PurchasePasswordValue","0");
                break;
            case IrdCommand_9:
                Log.d(TAG, "Set STB PIN");
                // 規格定義參數長度為 2 (9, 2, [PIN], [password])
                if (params.length >= 2 && primeDtv != null) {
                    Integer pinIndex = tryParseInt(params[0]); // 取得 [PIN] (1, 2, 或 3)
                    Integer pinValue = tryParseInt(params[1]); // 取得 [password]

                    if (pinIndex != null && pinValue != null) {
                        if (pinIndex == 1) {
                            // PIN = 1 => Parental PIN (親子鎖)
                            GposInfo.setPasswordValue(mContext, 0);
                            // primeDtv.gpos_info_update_by_key_string("PasswordValue","0");
                            Log.i(TAG, "Set Parental PIN to: " + pinValue);
                        } else if (pinIndex == 2) {
                            // PIN = 2 => Purchase PIN (購買鎖)
                            GposInfo.setPurchasePasswordValue(mContext, 0);
                            // primeDtv.gpos_info_update_by_key_string("PurchasePasswordValue","0");
                            Log.i(TAG, "Set Purchase PIN to: " + pinValue);
                        } else {
                            // PIN = 3 or others => Reserve (預留)
                            Log.w(TAG, "PIN index " + pinIndex + " is reserved or unsupported.");
                        }
                    } else {
                        Log.e(TAG, "Set STB PIN: Failed to parse index or value. Raw: " + params[0] + ", " + params[1]);
                    }
                }
                break;
            case IrdCommand_10:
                Log.d(TAG, "Disable STB Auto-standby");
                if (gposInfo != null) {
                    GposInfo.setAutoStandbyTime(mContext, 0);
                }
                break;
            case IrdCommand_11:
                Log.d(TAG, "Enable STB Auto-standby");
                if (gposInfo != null) {
                    GposInfo.setAutoStandbyTime(mContext, 5);
                }
                break;
            case IrdCommand_12:
                Log.d(TAG, "Reboot STB");
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    // "IrdCommand" 是重啟的原因 (Reason)，會記錄在系統日誌中
                    pm.reboot("IrdCommand");
                }
                break;
            case IrdCommand_13:
                Log.d(TAG, "Enable dynamic IP in Physical network");
                if (primeDtv != null) {
                    primeDtv.set_Ethernet_dhcp();
                }
                break;
            case IrdCommand_14:
                Log.d(TAG, "Enable static IP in Physical network for Master HMC-3000");
                // 確保參數數量足夠 (IP, Mask, Gateway, DNS)
                if (params.length >= 4 && primeDtv != null) {
                    String ip = params[0];
                    String mask = params[1];
                    String gateway = params[2];
                    String dns = params[3];

                    // 將子網路遮罩 (如 255.255.255.0) 轉換為前綴長度 (如 24)
                    int prefixLength = maskToPrefixLength(mask);

                    Log.i(TAG, "Setting Static IP: IP=" + ip + ", Mask=" + mask +
                            " (/" + prefixLength + "), GW=" + gateway + ", DNS=" + dns);

                    primeDtv.set_Ethernet_static_ip(ip, prefixLength, gateway, dns);
                } else {
                    Log.e(TAG, "IrdCommand_14: Missing parameters or dtv is null. Count=" + params.length);
                }
                break;
            case IrdCommand_15:
                Log.d(TAG, "Enable static IP in Physical network for Slave HMC-3000");
                break;
            case IrdCommand_16:
                Log.d(TAG, "Set static IP in Physical network for specific STB");
                break;
            case IrdCommand_17:
                Log.d(TAG, "Factory reset with FTI skip");
                if (primeDtv != null) {
                    primeDtv.do_factory_reset();
                }
                break;
            case IrdCommand_18:
                Log.d(TAG, "Reset the download applications from App Store");
                break;
            case IrdCommand_19:
                Log.d(TAG, "Disable AD (advertisement) in Live TV");
                SystemProperties.set("persist.sys.inspur.ca.ad", "close");
                break;
            case IrdCommand_20:
                Log.d(TAG, "Enable AD (advertisement) in Live TV");
                SystemProperties.set("persist.sys.inspur.ca.ad", "open");
                break;
            case IrdCommand_21:
                Log.d(TAG, "Reset App Store URL to default");
                break;
            case IrdCommand_22:
                Log.d(TAG, "Change App Store URL");
                break;
            case IrdCommand_23:
                Log.d(TAG, "Set AP Router – Home Gateway");
                break;
            case IrdCommand_24:
                Log.d(TAG, "Update Entitled List by default URL – Home Gateway");
                break;
            case IrdCommand_25:
                Log.d(TAG, "Update Entitled List by specific URL – Home Gateway");
                break;
            case IrdCommand_26:
                Log.d(TAG, "Disable First Time Installation/FTI");
                disableFtiAndTutorial();
                break;
            case IrdCommand_27:
                Log.d(TAG, "Change VBM Report URL/IP");
                handleChangeVbmReportUrl(params);
                break;
            case IrdCommand_28:
                // Remarks: This command is not available for Android TV STB
                Log.d(TAG, "Enable Anti-pirate function");
                break;
            case IrdCommand_29:
                // Remarks: This command is not available for Android TV STB
                Log.d(TAG, "Disable Anti-pirate function");
                break;
            case IrdCommand_30:
                handlePvrPairingAndProvision(params);
                Log.d(TAG, "HDD Pairing and Provision – PVR");
                break;
            case IrdCommand_31:
                handlePvrProvision(params);
                Log.d(TAG, "HDD Provision – PVR");
                break;
            case IrdCommand_32:
                Log.d(TAG, "HDD Formatting – PVR");
                primeDtv.format_hdd();
                break;
            case IrdCommand_33:
                // Remarks: This command is not available for Android TV STB
                Log.d(TAG, "Reset STB Download Mode");
                break;
            case IrdCommand_34:
                Log.d(TAG, "HDMI CEC Function");
                Integer cec = parseRequiredInt(params, 0, "cec");
                if (cec != null && primeDtv != null) {
                    primeDtv.set_hdmi_cec(cec);
                }
                break;
            case IrdCommand_35:
                Log.d(TAG, "TV Fee Reminder STB Locked Function");
                Integer lockStbOnoff = parseRequiredInt(params, 0, "lock_stb_onoff"); // 0-UnLock STB, 1-Lock STB
                String lockStbText = safeParam(params, 1);
                Pvcfg.set_cns_overdue_payment(lockStbOnoff);
                if (lockStbOnoff != null) {
                    Log.i(TAG, "STB lock params: onoff=" + lockStbOnoff + ", text=" + lockStbText);
                    if (lockStbText.toUpperCase().contains("URL")) {
                        String url = lockStbText;
                        if (lockStbText.toUpperCase().startsWith("URL:")) {
                            url = lockStbText.substring(4).trim();
                        } else if (lockStbText.toUpperCase().startsWith("URL=")) {
                            url = lockStbText.substring(4).trim();
                        }
                        Log.i(TAG, "Extracted URL: " + url);
                        GposInfo.setCNSOverduePaymentURL(mContext, url);
                    } else {
                        GposInfo.setCNSOverduePaymentURL(mContext, "");
                    }

                    // 收到命令後重新啟動 Launcher 以套用狀態
                    try {
                        Log.i(TAG, "Restarting Home Launcher via ACTION_MAIN/CATEGORY_HOME");
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(homeIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to restart Home Launcher", e);
                    }
                }
                // primeDtv.set_stb_lock(lock_stb_onoff, lock_stb_text);
                break;
            case IrdCommand_36:
                Log.d(TAG, "HDCP Function");
                Integer hdcp = parseRequiredInt(params, 0, "hdcp");
                if (hdcp != null && primeDtv != null) {
                    primeDtv.set_dcp_level(hdcp);
                }
                break;
            case IrdCommand_37:
                Log.d(TAG, "Area Limitation Function");
                Integer alOnoff = parseRequiredInt(params, 0, "al_onoff");
                if (alOnoff != null) {
                    Log.i(TAG, "Area limitation on/off=" + alOnoff);
                }
                break;
            case IrdCommand_38:
                // Remarks: This command is not available for Android TV STB
                Log.d(TAG, "TR069 Function");
                break;
            case IrdCommand_39:
                if (mChannelId != 0) {
                    ProgramInfo programInfo = primeDtv.get_program_by_channel_id(mChannelId);
                    TpInfo tpInfo = primeDtv.tp_info_get(programInfo.getTpId());
                    Integer frequency = parseRequiredInt(params, 0, "frequency");
                    Integer symbolrate = parseRequiredInt(params, 1, "symbolrate");
                    Integer qam = parseRequiredInt(params, 2, "qam");
                    Integer bid = parseRequiredInt(params, 3, "bid");
                    if (frequency == null || symbolrate == null || qam == null || bid == null) {
                        Log.e(TAG, "Rescan Channel: missing/invalid parameters");
                        break;
                    }
                    if (frequency != 0)// 0-Keep current parameter
                    {
                        tpInfo.CableTp.setFreq(frequency);
                    }
                    if (symbolrate != 0)// 0-Keep current parameter
                    {
                        tpInfo.CableTp.setSymbol(symbolrate);
                    }
                    if (qam != 0)// 0-Keep current parameter
                    {
                        tpInfo.CableTp.setQam(qam);
                    }
                    if (bid != 0)// 0-Keep current parameter
                    {
                        Pvcfg.setBatId(bid);
                    }
                    //primeDtv.stopMonitorTable(-1, -1);
                    TVScanParams tvScanParams = new TVScanParams(programInfo.getTunerId(), tpInfo, tpInfo.getSatId(),
                            TVScanParams.SCAN_CNS_SEARCH, TVScanParams.SEARCH_OPTION_ALL,
                            TVScanParams.SEARCH_OPTION_ALL,
                            0, 0);
                    primeDtv.start_scan(tvScanParams);
                }
                Log.d(TAG, "Rescan Channel");
                break;
            case IrdCommand_40:
                Log.d(TAG, "Set QR Error Code Reporting URL");
                handleChangeQRErrorCodeUrl(params);
                break;
            case IrdCommand_41:
                Log.d(TAG, "ATV Group/Mode Setting");
                handleATVGroupModeSetting(params);
                break;
            case IrdCommand_42:
                Log.d(TAG, "ATV AD Function Setting");
                handleATVADFunctionSetting(params);
                break;
            case IrdCommand_43:
                Log.d(TAG, "STB Log Reporting Trigger");

                break;
            case IrdCommand_44:
                Log.d(TAG, "ATV debug Mode Setting");
                break;
            case IrdCommand_45:
                Log.d(TAG, "Tuner Signal Status Report");
                primeDtv.handleTunerSignalStatusReport(params);
                break;
            case IrdCommand_46:
                Log.d(TAG, "STB Volume Restore Cache Cleaning Reload Launcher Layout");
                break;
            case IrdCommand_47:// 00 08 34 37 2C 32 2C 30 2C 30 => ex:47,2,0,0
                Log.d(TAG, "Refresh Widevine CAS Data Trigger");
                int delete_flag = parseRequiredInt(params, 0, "delete_flag");
                int delayS = parseRequiredInt(params, 1, "delayS");
                primeDtv.handleCasRefresh(delete_flag, delayS, mChannelId);
                break;
            case IrdCommand_48:
                String url = params[0];
                Log.d(TAG, "Change Widevine CAS Proxy Server IP to " + url);
                GposInfo.setWVCasLicenseURL(mContext, url);
                break;
            default:
                Log.d(TAG, "Unknown Ird Command [" + commandId + "]");
                break;
        }
    }

    /**
     * Command 26: Disable First Time Installation/FTI
     *
     * CNSLauncher checks:
     * Settings.Secure "user_setup_complete" (if 1 => not in FTI)
     */

    private void disableFtiAndTutorial() {
        try {
//            Settings.System.putInt(mContext.getContentResolver(), "FTI_CREATE", 0);
            GposInfo.setFTI_CREATE(mContext,0);
//            Settings.System.putInt(mContext.getContentResolver(), "cns_show_tutorial_page", 0);
            GposInfo.setTutorialSetting(mContext, 0);
            Log.i(TAG, "disableFtiAndTutorial !! GposInfo.setFTI_CREATE(mContext,0) , GposInfo.setTutorialSetting(mContext, 0)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to write Settings.System.FTI_CREATE", e);
        }
    }

    private void handleChangeQRErrorCodeUrl(String[] params) {
        Log.i(TAG, "Change QR Error Code URL");

        if (params.length < 1 || TextUtils.isEmpty(params[0])) {
            Log.e(TAG, "Change QR Error Code URL: missing param[0]=url");
            return;
        }
        String url = params[0].trim();
        Pvcfg.set_error_code_report_url(url);
    }

    private void handleATVGroupModeSetting(String[] params) {
        Log.i(TAG, "Change ATV Group/Mode Setting");

        if (params == null || params.length < 2) {
            Log.e(TAG, "Change ATV Group/Mode Setting error");
            return;
        }
        String groupId = params[0].trim();
        String mode = params[1].trim();
        // 第三參數 properties：保留欄位，可能不存在
        String properties = "";
        if (params.length >= 3 && params[2] != null) {
            properties = params[2].trim();
        }
        Pvcfg.setATV_GroupId(groupId);
        Pvcfg.setATV_Mode(mode);
        if (!TextUtils.isEmpty(properties)) {
            Pvcfg.setATV_Properties(properties);
        }

    }

    private void handleChangeVbmReportUrl(String[] params) {
        Log.i(TAG, "Change VBM Report URL/IP");

        if (params.length < 1 || TextUtils.isEmpty(params[0])) {
            Log.e(TAG, "Change VBM Report URL/IP: missing param[0]=url");
            return;
        }
        String url = params[0].trim();
        Pvcfg.set_Vbm_Url(url);
    }

    private boolean parseBoolean(String s) {
        if (s == null)
            return false;
        String val = s.trim();
        return "1".equals(val) || "true".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val);
    }

    private void handlePvrPairingAndProvision(String[] params) {
        Log.i(TAG, "HDD Pairing and Provision – PVR");
        // Spec example:
        // 30,5,WD-WX1234567890,1,1000,2,12345678

        if (params.length < 5) {
            Log.e(TAG, "Command 30 needs 5 params: hdd_sn, pvr_on_off, size, tuner_resource, encrypt_key. params="
                    + Arrays.toString(params));
            return;
        }

        final String hddSn = params[0].trim();
        final boolean onoff = parseBoolean(params[1]); // 0-Disable PVR, 1-Enable PVR
        final int logicalCapacity = Integer.parseInt(params[2]);// 限制可錄的大小 Unit-GB
        final String tunerResource = params[3].trim();// 可做幾路的錄影
        final String encryptKey = params[4].trim();

        if (TextUtils.isEmpty(hddSn) || TextUtils.isEmpty(tunerResource) || TextUtils.isEmpty(encryptKey)) {
            Log.e(TAG, "Command 30 param empty, hddSn=" + hddSn
                    + ", tunerResource=" + tunerResource
                    + ", encryptKey=" + encryptKey);
            return;
        }

        // 1) 寫入 System Property (persist.* 會落盤保存)
        Pvcfg.setHDD_SN(hddSn);
        Pvcfg.setPVR_PJ(onoff);
        Pvcfg.setPvrHddAuthorizedSize(logicalCapacity * 1024);
        Pvcfg.setPVR_Tuner_Resource(tunerResource);
        Pvcfg.setPVR_Encrypt_Key(encryptKey);

        // 2) 讀回做 log
        Log.i(TAG, "Persisted props:"
                + " hdd_pair_sn=" + Pvcfg.getHDD_SN()
                + " pvr_on_off=" + Pvcfg.getPVR_PJ()
                + ", pvr_logical_capacity=" + Pvcfg.getPvrHddAuthorizedSize()
                + ", pvr_tuner_resource=" + Pvcfg.getPVR_Tuner_Resource()
                + ", pvr_encrypt_key=" + Pvcfg.getPVR_Encrypt_Key());

    }

    private void handlePvrProvision(String[] params) {
        Log.i(TAG, "HDDProvision – PVR");
        // Spec example:
        // 30,5, 1,1000,2,12345678

        if (params.length < 4) {
            Log.e(TAG, "Command 31 needs 4 params:,pvr_on_off, size, tuner_resource, encrypt_key. params="
                    + Arrays.toString(params));
            return;
        }

        final boolean onoff = parseBoolean(params[0]); // 0-Disable PVR, 1-Enable PVR
        final String tunerResource = params[2].trim();
        final String encryptKey = params[3].trim();

        if (TextUtils.isEmpty(tunerResource) || TextUtils.isEmpty(encryptKey)) {
            Log.e(TAG, "Command 30 param empty, on_off=" + onoff
                    + ", tunerResource=" + tunerResource
                    + ", encryptKey=" + encryptKey);
            return;
        }

        // 1) 寫入 System Property (persist.* 會落盤保存)
        Pvcfg.setPVR_PJ(onoff);
        Pvcfg.setPVR_Tuner_Resource(tunerResource);
        Pvcfg.setPVR_Encrypt_Key(encryptKey);

        // 2) 讀回做 log，方便現場驗證（可選，但建議保留）
        Log.i(TAG, "Persisted props:"
                + " pvr_on_off=" + SystemProperties.get(PropertyDefine.PROPERTY_PVRPJ, "")
                + ", pvr_tuner_resource=" + SystemProperties.get(PropertyDefine.PROP_PVR_TUNER_RESOURCE, "")
                + ", pvr_encrypt_key=" + SystemProperties.get(PropertyDefine.PROP_PVR_ENCRYPT_KEY, ""));

    }

    private void handleATVADFunctionSetting(String[] params) {
        Log.i(TAG, "ATV AD Function Setting");

        // params: [ad vender], [onoff], [properties]
        if (params == null || params.length < 2) {
            Log.e(TAG, "ATV AD Setting error: need at least [vendor],[onoff], paramsLen="
                    + (params == null ? -1 : params.length));
            return;
        }

        // 1) vendor: 0~4
        String vendorRaw = (params[0] == null) ? "" : params[0].trim();
        Integer vendor = tryParseInt(vendorRaw);
        if (vendor == null || vendor < 0 || vendor > 4) {
            Log.e(TAG, "ATV AD Setting error: vendor must be 0~4, vendor=" + vendorRaw);
            return;
        }

        // 2) onoff: 0/1 only
        String onoffRaw = (params[1] == null) ? "" : params[1].trim();
        if (!"0".equals(onoffRaw) && !"1".equals(onoffRaw)) {
            Log.e(TAG, "ATV AD Setting error: onoff must be 0/1, onoff=" + onoffRaw);
            return;
        }

        // 3) properties: reserved text
        String properties = "";
        if (params.length >= 3 && params[2] != null) {
            properties = params[2].trim();
        }

        // 4) Apply via your wrapper functions
        // vendor=0 is "All vendors" (global), store as-is for downstream
        // interpretation.
        Pvcfg.setATV_AD_Vendor(String.valueOf(vendor));
        Pvcfg.setATV_AD_Enable(onoffRaw); // keep 0/1 string per your API
        Pvcfg.setATV_AD_Properties(properties); // reserved

        Log.i(TAG, "ATV AD Setting applied:"
                + " vendor=" + vendor
                + ", enable=" + onoffRaw
                + ", properties=" + properties);
    }

    // =============================================================================================
    // Parsing helpers
    // =============================================================================================

    /**
     * Remove all NUL characters and then trim.
     * Why:
     * - AES decrypt output / fixed-size buffers commonly contain trailing '\0'.
     * - If we don't remove them, parseInt() may fail, or params may carry hidden
     * characters.
     */
    private static String stripNulAndTrim(String s) {
        if (s == null)
            return "";
        return s.replace("\u0000", "").trim();
    }

    private static Integer tryParseInt(String s) {
        if (s == null)
            return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Extract parameters according to paraLength.
     *
     * args layout:
     * args[0]=mainCmd, args[1]=paraLength, args[2..]=para1..
     *
     * Defensive rule:
     * - If provided param tokens > paraLength, join the overflow tokens into the
     * LAST param.
     */
    private String[] extractParams(String[] args, int paraLength) {
        if (paraLength < 0) {
            Log.e(TAG, "extractParams: paraLength < 0");
            return null;
        }

        int available = Math.max(0, args.length - 2);

        if (available < paraLength) {
            Log.e(TAG, "extractParams: params not enough, expected=" + paraLength + ", available=" + available);
            return null;
        }

        if (paraLength == 0) {
            return new String[0];
        }

        String[] params = new String[paraLength];

        // Copy first paraLength-1
        for (int i = 0; i < paraLength - 1; i++) {
            params[i] = safeParam(args[2 + i]);
        }

        // Last param: join remaining tokens if any
        StringBuilder last = new StringBuilder();
        last.append(safeParam(args[2 + (paraLength - 1)]));
        for (int i = 2 + paraLength; i < args.length; i++) {
            last.append(",").append(safeParam(args[i]));
        }
        params[paraLength - 1] = last.toString();

        return params;
    }

    private Integer parseRequiredInt(String[] params, int index, String name) {
        String raw = safeParam(params, index);
        if (TextUtils.isEmpty(raw)) {
            Log.e(TAG, "Missing integer param [" + name + "] at index=" + index);
            return null;
        }
        Integer value = tryParseInt(raw);
        if (value == null) {
            Log.e(TAG, "Invalid integer param [" + name + "]=" + raw);
        }
        return value;
    }

    private static String safeParam(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static String safeParam(String[] params, int index) {
        if (params == null || index < 0 || index >= params.length) {
            return "";
        }
        return safeParam(params[index]);
    }

    // 將子網路遮罩字串 (如 "255.255.255.0") 轉換為 Prefix Length (如 24)
    private int maskToPrefixLength(String subnetMask) {
        try {
            InetAddress maskAddr = InetAddress.getByName(subnetMask);
            byte[] maskBytes = maskAddr.getAddress();
            int prefixLength = 0;
            for (byte b : maskBytes) {
                for (int i = 7; i >= 0; i--) {
                    if (((b >> i) & 1) == 1) {
                        prefixLength++;
                    } else {
                        // 假設是連續的遮罩 (如 255.255.255.0)
                        return prefixLength;
                    }
                }
            }
            return prefixLength;
        } catch (Exception e) {
            Log.e(TAG, "Error converting subnet mask to prefix length", e);
            return 0; // 錯誤處理
        }
    }

}
