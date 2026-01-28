package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.POS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.VALUE;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GetProgramByChannelId;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GetProgramInfoList;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GposInfoGet;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GposInfoUpdate;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GposSaveKeyValueInteger;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GposSaveKeyValueString;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_TpInfoGet;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_TpInfoGetListBySatId;
import static com.prime.datastructure.ServiceDefine.AvCmdMiddle.PESI_SVR_AV_STOP_STATE;

import android.content.Context;
import android.media.tv.TvContentRating;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.os.SystemProperties;
import android.view.Surface;
import android.view.SurfaceView;

import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.SubtitleInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.JsonParser;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.utils.TVTunerParams;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrimeDtvServiceInterface implements PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback {
    public static String TAG = "PrimeDtvServiceInterface";
    private static Context gContext;
    private IPrimeDtvService gPrimeDtvService = null;
    private static PrimeDtvServiceInterface g_primeDtvServiceInterface;
    private static IPrimeDtvServiceCallback gPrimeDtvServiceCallback = null;
    private PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback gCallback = null;
    private boolean mPrimeDtvServiceReady = false;

    public static PrimeDtvServiceInterface getInstance(Context context) {
        gContext = context;
        if (g_primeDtvServiceInterface == null)
            g_primeDtvServiceInterface = new PrimeDtvServiceInterface(context);
        return g_primeDtvServiceInterface;
    }

    public boolean isPrimeDtvServiceReady() {
        return mPrimeDtvServiceReady;
    }

    public void register_callbacks(PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback callback) {
        gCallback = callback;
    }

    public void unregister_callbacks(PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback callback) {
        gCallback = null;
    }

    @Override
    public void onServiceConnected() {
        if (gCallback != null)
            gCallback.onServiceConnected();
    }

    @Override
    public void onServiceDisconnected() {
        if (gCallback != null)
            gCallback.onServiceDisconnected();
    }

    public interface onMessageListener {
        void onMessage(TVMessage msg);
    }

    public PrimeDtvServiceInterface(Context context) {
        gContext = context;
    }

    public void SetPrimeDtvService(IPrimeDtvService service, onMessageListener listener, String connectedFrom) {
        try {
            if (service == null && gPrimeDtvService != null) {
                Log.d(TAG, "[PrimeDtvServiceAIDL] Unregistering callback because service is being set to null");
                gPrimeDtvService.unregisterCallback(gPrimeDtvServiceCallback);
            }
            gPrimeDtvService = service;
            Log.d(TAG, "[PrimeDtvServiceAIDL] SetPrimeDtvService: service = " + gPrimeDtvService + ", connectedFrom = "
                    + connectedFrom);
            if (gPrimeDtvService != null) {
                gPrimeDtvServiceCallback = new IPrimeDtvServiceCallback.Stub() {
                    @Override
                    public void onMessage(TVMessage msg) throws RemoteException {
                        Log.d(TAG,
                                "[PrimeDtvServiceAIDL] PrimeDtvServiceInterface onMessage msg = " + msg.getMessage()
                                        + " type "
                                        + msg.getMsgType() + " flag " + msg.getMsgFlag());
                        if (msg.getMsgFlag() == TVMessage.FLAG_SYSTEM && msg.getMsgType() == TVMessage.TYPE_SYSTEM_INIT){

                            mPrimeDtvServiceReady = true;
                            Log.d(TAG, "PrimeDtvService ok: TYPE_SYSTEM_INIT");
                        }
                        if (listener != null)
                            listener.onMessage(msg);
                    }
                };
                Log.d(TAG, "[PrimeDtvServiceAIDL] Registering callback: " + gPrimeDtvServiceCallback);
                gPrimeDtvService.registerCallback(gPrimeDtvServiceCallback, connectedFrom);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "[PrimeDtvServiceAIDL] SetPrimeDtvService: RemoteException ignored (service likely dead)", e);
        }
    }

    public static IPrimeDtvServiceCallback getgPrimeDtvServiceCallback() {
        return gPrimeDtvServiceCallback;
    }

    public Bundle invokeBundle(Bundle requestBundle) {
        if (gPrimeDtvService != null) {
            try {
                Log.d(TAG, "invokeBundle gPrimeDtvServiceCallback = " + gPrimeDtvServiceCallback);
                return gPrimeDtvService.invokeBundle(requestBundle);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.w(TAG, "PrimeDtvService not ready !!");
        }
        return new Bundle();
    }

    public boolean isServiceReady() {
        return gPrimeDtvService != null;
    }

    public List<ProgramInfo> get_program_info_list(int type, int pos, int num) {
        List<ProgramInfo> programInfoList = new ArrayList<>();
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_GetProgramInfoList);
        requestBundle.putInt(FavInfo.FAV_MODE, type);
        requestBundle.putInt(MiscDefine.POS, pos);
        requestBundle.putInt(MiscDefine.Num, num);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            programInfoList = replyBundle.getParcelableArrayList(COMMAND_REPLY_OBJ, ProgramInfo.class);
        return programInfoList;
    }

    public ProgramInfo get_program_by_channel_id(long channelId) {
        ProgramInfo programInfo = null;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_GetProgramByChannelId);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelId);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            programInfo = replyBundle.getParcelable(COMMAND_REPLY_OBJ, ProgramInfo.class);
        return programInfo;
    }

    public ProgramInfo get_program_by_SId_OnId_TsId(int SId, int OnId, int TsId) {
        ProgramInfo programInfo = null;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_GetProgramBySIdOnIdTsId);
        requestBundle.putInt(ProgramInfo.SERVICE_ID, SId);
        requestBundle.putInt(ProgramInfo.ORIGINAL_NETWORK_ID, OnId);
        requestBundle.putInt(ProgramInfo.TRANSPORT_STREAM_ID, TsId);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        Log.d(TAG, "get_program_by_SId_OnId_TsId SId = " + SId + " OnId = " + OnId + " TsId = " + TsId);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            programInfo = replyBundle.getParcelable(COMMAND_REPLY_OBJ, ProgramInfo.class);
            if (programInfo != null)
                Log.d(TAG, "get_program_by_SId_OnId_TsId programInfo = [" + programInfo.getDisplayNum()
                        + "] " + programInfo.getDisplayName());
        } else {
            Log.d(TAG, "get_program_by_SId_OnId_TsId fail");
        }
        return programInfo;
    }

    public ProgramInfo get_program_by_ch_num(int chnum, int type) {
        ProgramInfo programInfo = null;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_GetProgramByChannelId);
        requestBundle.putInt(ProgramInfo.DISPLAY_NUM, chnum);
        requestBundle.putInt(ProgramInfo.TYPE, type);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            programInfo = replyBundle.getParcelable(COMMAND_REPLY_OBJ, ProgramInfo.class);
        // ParcelableClass parcel =
        // gPrimeDtvService.invokeParcel(CMD_ServicePlayer_PM_GetProgramByChannelId,null);
        // programInfo = parcel.getData(ProgramInfo.class);
        return programInfo;
    }

    public int gpos_info_update(GposInfo gposInfo) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_GposInfoUpdate);
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putParcelable(GposInfo.TAG, gposInfo);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int gpos_info_update_by_key_string(String key, int value) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_GposSaveKeyValueInteger);
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putString(PmModule.KeyName_String, key);
        requestBundle.putInt(key, value);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int gpos_info_update_by_key_string(String key, String value) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_GposSaveKeyValueString);
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putString(PmModule.KeyName_String, key);
        requestBundle.putString(key, value);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public GposInfo gpos_info_get() {
        GposInfo gposInfo = null;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_GposInfoGet);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            gposInfo = replyBundle.getParcelable(COMMAND_REPLY_OBJ, GposInfo.class);
        }
        // ParcelableClass parcel =
        // gPrimeDtvService.invokeParcel(CMD_ServicePlayer_PM_GetProgramByChannelId,null);
        // programInfo = parcel.getData(ProgramInfo.class);
        return gposInfo;
    }

    public int tp_info_add(TpInfo pTp) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_TpInfoAdd);
        requestBundle.putParcelable(TpInfo.TAG, pTp);
        TpInfo tpInfo = requestBundle.getParcelable(TpInfo.TAG, TpInfo.class);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getInt(TpInfo.TP_ID, -1);
        }
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int tp_info_update(TpInfo pTp) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_TpInfoUpdate);
        requestBundle.putParcelable(TpInfo.TAG, pTp);
        TpInfo tpInfo = requestBundle.getParcelable(TpInfo.TAG, TpInfo.class);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public List<TpInfo> tp_info_get_list_by_satId(int tunerType, int satId, int pos, int num) {
        List<TpInfo> tpInfoList = new ArrayList<>();
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_TpInfoGetListBySatId);
        requestBundle.putInt(TpInfo.TUNER_TYPE, tunerType);
        requestBundle.putInt(TpInfo.SAT_ID, satId);
        requestBundle.putInt(MiscDefine.POS, pos);
        requestBundle.putInt(MiscDefine.Num, num);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            tpInfoList = replyBundle.getParcelableArrayList(COMMAND_REPLY_OBJ, TpInfo.class);
        return tpInfoList;
    }

    public TpInfo tp_info_get(int tpId) {
        TpInfo tpInfo = null;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CMD_ServicePlayer_PM_TpInfoGet);
        requestBundle.putInt(TpInfo.TP_ID, tpId);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            tpInfo = replyBundle.getParcelable(COMMAND_REPLY_OBJ, TpInfo.class);
        return tpInfo;
    }

    public int setSetVisibleCompleted(boolean isVisibleCompleted) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_SetVisibleCompiled);
        requestBundle.putBoolean(AvModule.SetVisibleCompiled_string, isVisibleCompleted);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_get_play_status(int playId) {
        int playStatus = PESI_SVR_AV_STOP_STATE;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_GetPlayStatus);
        requestBundle.putInt(AvModule.PlayId_string, playId);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            playStatus = replyBundle.getInt(AvModule.PlayStatus_string, PESI_SVR_AV_STOP_STATE);
        return playStatus;
    }

    public AudioInfo av_control_get_audio_list_info(int playId) {
        AudioInfo audioInfo = null;

        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_GetAudioListInfo);
        requestBundle.putInt(AvModule.PlayId_string, playId);

        replyBundle = invokeBundle(requestBundle);

        // 很重要：設好 classLoader，讓 Bundle 知道去哪裡找 AudioInfo 這個 class
        replyBundle.setClassLoader(gContext.getClassLoader());

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            // ★★★ 真正從 reply 取出 AudioInfo ★★★
            audioInfo = replyBundle.getParcelable(COMMAND_REPLY_OBJ, AudioInfo.class);
        }

        return audioInfo;
    }

    public int av_control_change_audio(int playId, AudioInfo.AudioComponent component) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_ChangeAudio);
        requestBundle.putInt(AvModule.PlayId_string, playId);
        requestBundle.putParcelable(AvModule.AudioComponent_string, component);

        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }
    // =========================
    // Subtitle control
    // =========================

    /**
     * 從 DtvService 拿到目前 playId 底下的 DVB Subtitle 清單
     * 對應 Service 端回傳一個 SubtitleInfo（裡面有 component list）
     */
    public SubtitleInfo av_control_get_subtitle_list_info(int playId) {
        SubtitleInfo subtitleInfo = null;

        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_SUBTITLE_GetList); // TODO: 常數名照實際定義
        requestBundle.putInt(AvModule.PlayId_string, playId);

        replyBundle = invokeBundle(requestBundle);

        // 一樣：一定要設 classLoader，不然 Parcelable 會找不到 SubtitleInfo
        replyBundle.setClassLoader(gContext.getClassLoader());

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            subtitleInfo = replyBundle.getParcelable(COMMAND_REPLY_OBJ, SubtitleInfo.class);
        }

        return subtitleInfo;
    }

    /**
     * 切換 Subtitle track（選擇哪一條 DVB subtitle）
     * 通常在 TIF 的 onSelectTrack() 被叫到時使用
     */
    public int av_control_change_subtitle(int playId, SubtitleInfo.SubtitleComponent component) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_SUBTITLE_Select); // TODO: 確認常數名
        requestBundle.putInt(AvModule.PlayId_string, playId);
        requestBundle.putParcelable(AvModule.SubtitleComponent_string, component); // TODO: 確認 key 名稱

        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    /**
     * 開啟 DVB Subtitle 解碼 / 顯示
     * 一般可以在 TIF 的 onSetCaptionEnabled(true) 時呼叫：
     * 1. 先 av_control_get_subtitle_list_info()
     * 2. 選擇預設 component
     * 3. 再呼叫這支 start_subtitle
     */

    /**
     * 關閉 DVB Subtitle 解碼 / 顯示
     * 一般在 onSetCaptionEnabled(false) 時呼叫
     */
    public int av_control_stop_subtitle(int playId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_SUBTITLE_Stop); // TODO: 確認常數名
        requestBundle.putInt(AvModule.PlayId_string, playId);

        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_set_play_status(int status) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_SetPlayStatus);
        requestBundle.putInt(AvModule.PlayStatus_string, status);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_play_stop_all() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_PlayStopAll);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_reset_audio_default_language(String lang) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_ResetAudioDefaultLanguage);
        requestBundle.putString(GposInfo.GPOS_AUDIO_LANG_SELECT_1, lang);

        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public void AvCmdMiddle_WaitAVPlayReady() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_WaitAVPlayReady);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public void set_tv_input_id(String id) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_SetTvInputId);
        requestBundle.putString(PmModule.TvInputId_String, id);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        Log.d(TAG, "set_tv_input_id inputId = " + id);
    }

    public void do_factory_reset() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_FactoryReset);
        replyBundle = invokeBundle(requestBundle);
    }

    public void set_Ethernet_static_ip(String ip, int prefixLength, String gateway, String dns) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_SetEthernetStaticIP);
        requestBundle.putString("static_ip", ip);
        requestBundle.putInt("static_ip_prefixLength", prefixLength);
        requestBundle.putString("static_ip_gateway", gateway);
        requestBundle.putString("static_ip_dns", dns);
        replyBundle = invokeBundle(requestBundle);
    }

    public String get_hdd_serial() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_GetHddSeries);
        replyBundle = invokeBundle(requestBundle);
        int ret = replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        if (ret == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getString("hdd_series", "000000000");
        }
        return "000000000";
    }

    public String[] get_mac() {
        String[] macs = new String[2];
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_GetMac);
        replyBundle = invokeBundle(requestBundle);
        int ret = replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        if (ret == COMMAND_REPLY_SUCCESS) {
            macs[0] = replyBundle.getString("Ethernet_mac", "001122334455");
            macs[1] = replyBundle.getString("Wifi_mac", "554433221100");
        }
        return macs;
    }

    public void set_Ethernet_dhcp() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_SetEthernetDHCP);
        replyBundle = invokeBundle(requestBundle);
    }

    public void init_service() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_InitService);
        replyBundle = invokeBundle(requestBundle);
    }

    public void check_lib_correct(String dataStructureVerName, String playerServiceVerName) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_GetLibVer);
        replyBundle = invokeBundle(requestBundle);
        int ret = replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        if (ret == COMMAND_REPLY_SUCCESS) {
            String dataStructure = replyBundle.getString(CommonModule.DATASTRUCTURE_PACKAGE_NAME, "");
            String playerService = replyBundle.getString(CommonModule.PLAYERSERVICE_PACKAGE_NAME, "");

            // Log.d(TAG,"check_lib_correct app dataStructure ver : " + dataStructureVerName
            // +
            // " PrimeDtvService dataStructure ver : " + dataStructure);
            // Log.d(TAG,"check_lib_correct app playerService ver : " + playerServiceVerName
            // +
            // " PrimeDtvService playerService ver : " + playerService);
            if (dataStructure.equals(dataStructureVerName) && playerService.equals(playerServiceVerName)) {
                Log.d(TAG, "check prime library version success");
            } else {
                String exception_string = "check prime library version fail ! ";
                if (!dataStructure.equals(dataStructureVerName))
                    exception_string += ("dataStructure not equals, app ver : " + dataStructureVerName +
                            " PrimeDtvService ver : " + dataStructure);
                if (!playerService.equals(playerServiceVerName))
                    exception_string += ("playerServiceVerName not equals, app ver : " + playerServiceVerName +
                            " PrimeDtvService ver : " + playerService);
                throw new RuntimeException(exception_string);
            }
        }
    }

    public void StopDVBStubtitle() {
        Log.d(TAG, "StopDVBStubtitle() not porting");
    }

    public int update_program_info(ProgramInfo pProgram) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_UpdateProgramInfo);
        requestBundle.putParcelable(ProgramInfo.TAG, pProgram);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_play_stop(int tunerId, int mode, int stop_monitor_table) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_PlayStop);
        requestBundle.putInt(ProgramInfo.TUNER_ID, tunerId);
        requestBundle.putInt(AvModule.Mode_string, mode);
        requestBundle.putInt(AvModule.StopMonitorTable_string, stop_monitor_table);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_play_by_channel_id(int playId, long channelId, int groupType, int show) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_PlayByChannelId);
        requestBundle.putInt(AvModule.PlayId_string, playId);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelId);
        requestBundle.putInt(ProgramInfo.TYPE, groupType);
        requestBundle.putInt(AvModule.Show_string, show);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_tif_play_by_channel_id(int playId, long channelId, int groupType, boolean force) {
        Bundle requestBundle = new Bundle(), replyBundle;
        int tunerId = 0;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_ChangeChannelManager_TIF_PlayByChannelId);
        requestBundle.putInt(AvModule.PlayId_string, playId);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelId);
        requestBundle.putInt(ProgramInfo.TYPE, groupType);
        requestBundle.putBoolean(AvModule.Force_string, force);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            tunerId = replyBundle.getInt(AvModule.PlayId_string, 0);
        return tunerId;
    }

    public int av_control_set_aspect_ratio(int aspectRatio) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_SetAspectRatio);
        requestBundle.putInt("AspectRatio", aspectRatio);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int av_control_change_channel_manager_list_update(int groupType) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_ChangeChannelManager_ListUpdate);
        requestBundle.putInt(ProgramInfo.TYPE, groupType);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int set_default_open_channel(long channelId, int groupType) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_SetDefaultOpenChannel);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelId);
        requestBundle.putInt(ProgramInfo.TYPE, groupType);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list,
            boolean channelBlocked) {
        Log.d(TAG, "AvControlPlayByChannelIdFCC() not porting");
        return 0;
    }

    public int av_control_clear_fast_change_channel(int tunerId, long chId) {
        LogUtils.d("[FCC2] ");
        Log.d(TAG, "AvControlPlayByChannelIdFCC() not porting");
        return 0;
    }

    public void stopMonitorTable(long channel_id, int tuner_id) {
        LogUtils.d("AutoUpdateManager channel_id = " + channel_id + " tuner id = " + tuner_id);
        Log.d(TAG, "AvControlPlayByChannelIdFCC() not porting");
    }

    public int av_control_set_fast_change_channel(int tunerId, long chId) {
        Log.d(TAG, "av_control_set_fast_change_channel() not porting");
        return 0;
    }

    public void set_surface(Context context, Surface surface, int index) {
        Bundle requestBundle = new Bundle(), replyBundle;
        LogUtils.d("FCC_LOG PrimeDtvServiceInterface set_surface: index=" + index
                + " surface=" + surface
                + " surfaceId=" + (surface == null ? 0 : System.identityHashCode(surface))
                + " pid=" + android.os.Process.myPid()
                + " uid=" + android.os.Process.myUid());
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_SetSurface);
        requestBundle.putParcelable(AvModule.Surface_string, surface);
        requestBundle.putInt(MiscDefine.Index, index);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle != null) {
            LogUtils.d("FCC_LOG PrimeDtvServiceInterface set_surface: reply status="
                    + replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL));
        } else {
            LogUtils.d("FCC_LOG PrimeDtvServiceInterface set_surface: reply null");
        }
    }

    public void set_surface_view(Context context, SurfaceView surfaceView, int index) {
        Surface surface = null;
        if (surfaceView != null && surfaceView.getHolder() != null) {
            surface = surfaceView.getHolder().getSurface();
        }
        LogUtils.d("FCC_LOG PrimeDtvServiceInterface set_surface_view: index=" + index
                + " surfaceView=" + surfaceView + " surface=" + surface
                + " surfaceId=" + (surface == null ? 0 : System.identityHashCode(surface)));
        set_surface(context, surface, index);
    }

    public boolean get_tuner_status(int tunerId) {
        Boolean isLock = false;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_GetTunerStatus);
        requestBundle.putInt(TpInfo.TUNER_ID, tunerId);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            isLock = replyBundle.getBoolean(TVTunerParams.IsLock, false);
        return isLock;
    }

    public void stop_scan(boolean store) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, ScanModule.CMD_ServicePlayer_SCAN_StopScan);
        requestBundle.putBoolean(ScanModule.Store_string, store);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public void start_schedule_eit(int tp_id, int tuner_id) {
        // use tuner framework to get epg raw data
        LogUtils.d("[Ethan] start_schedule_eit");
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, EpgModule.CMD_ServicePlayer_EPG_StartScheduleEit);
        requestBundle.putInt(TpInfo.TP_ID, tp_id);
        requestBundle.putInt(TpInfo.TUNER_ID, tuner_id);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public void stop_schedule_eit() {
        LogUtils.d("[Ethan] stop_schedule_eit");
        // gDtvFramework.stopScheduleEit();
        // try {
        // Bundle requestBundle = new Bundle(),replyBundle;
        // requestBundle.putInt(COMMAND_ID,
        // EpgModule.CMD_ServicePlayer_EPG_StopScheduleEit);
        // replyBundle = invokeBundle(requestBundle);
        // replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        // } catch (RemoteException e) {
        // throw new RuntimeException(e);
        // }
    }

    public void setup_epg_channel() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, EpgModule.CMD_ServicePlayer_EPG_SetupEpgChannel);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public void save_table(EnTableType tableType) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_SaveTable);
        requestBundle.putString(MiscDefine.EnTableType_string, tableType.name());
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public void backupDatabase(boolean force) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_BackupDatabase);
        requestBundle.putBoolean(MiscDefine.Force_string, force);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int tuner_lock(TVTunerParams tvTunerParams) {
        Bundle requestBundle = new Bundle(), replyBundle;
        // requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_TunerTune);
        requestBundle.putParcelable(TVTunerParams.TAG, tvTunerParams);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int get_tuner_type() {
        int tunerType = 0;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_GetTunerType);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            tunerType = replyBundle.getInt(TpInfo.TUNER_TYPE, 0);
        return tunerType;
    }

    public void start_scan(TVScanParams sp) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, ScanModule.CMD_ServicePlayer_SCAN_StartScan);
        requestBundle.putParcelable(TVScanParams.TAG, sp);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int get_signal_strength(int nTunerID) {
        int value = 0;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_GetSignalStrength);
        requestBundle.putInt(TpInfo.TUNER_ID, nTunerID);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            value = replyBundle.getInt(FeModule.Strength_string, 0);
        return value;
    }

    public int get_signal_quality(int nTunerID) {
        int value = 0;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_GetSignalQuality);
        requestBundle.putInt(TpInfo.TUNER_ID, nTunerID);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            value = replyBundle.getInt(FeModule.Quality_string, 0);
        return value;
    }

    public int get_signal_snr(int nTunerID) {
        int value = 0;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_GetSignalSNR);
        requestBundle.putInt(TpInfo.TUNER_ID, nTunerID);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            value = replyBundle.getInt(FeModule.SNR_string, 0);
        return value;
    }

    public int get_signal_ber(int nTunerID) {
        int value = 0;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_GetSignalBER);
        requestBundle.putInt(TpInfo.TUNER_ID, nTunerID);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            value = replyBundle.getInt(FeModule.BER_string, 0);
        return value;
    }

    public void category_update_to_fav(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList) {
        int value = 0;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PmModule.CMD_ServicePlayer_PM_CategoryUpdateToFav);
        ArrayList<ProgramInfo> programInfoArrayList = new ArrayList<>(ProgramInfoList);
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putParcelableArrayList(MiscDefine.ProgramInfoList_string, programInfoArrayList);
        ArrayList<MusicInfo> musicInfoArrayList = new ArrayList<>(musicInfoList);
        requestBundle.putParcelableArrayList(MiscDefine.MusicInfoList_string, musicInfoArrayList);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public List<MusicInfo> get_current_category(Context context) {
        String musicList = null;
        // String musicList = ACSDataProviderHelper.get_acs_provider_data(context,
        // "music_category");
        // String musicList = "[\n" + " {\n" + " \"name\": \"華語音樂\",\n" + " \"icon\":
        // \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330858497.png\",\n" + "
        // \"sort\": 1,\n" + " \"servicelists\": [\n" + " \"1534\",\n" + " \"1535\",\n"
        // + " \"1536\",\n" + " \"1543\"\n" + " ]\n" + " },\n" + " {\n" + " \"name\":
        // \"西洋音樂\",\n" + " \"icon\":
        // \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330890167.png\",\n" + "
        // \"sort\": 2,\n" + " \"servicelists\": [\n" + " \"1514\",\n" + " \"1515\",\n"
        // + " \"1516\",\n" + " \"1517\",\n" + " \"1518\",\n" + " \"1519\",\n" + "
        // \"1521\"\n" + " ]\n" + " },\n" + " {\n" + " \"name\": \"東洋音樂\",\n" + "
        // \"icon\":
        // \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330824653.png\",\n" + "
        // \"sort\": 3,\n" + " \"servicelists\": [\n" + " \"1525\",\n" + " \"1526\",\n"
        // + " \"1527\"\n" + " ]\n" + " },\n" + " {\n" + " \"name\": \"沙發音樂\",\n" + "
        // \"icon\":
        // \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330938131.png\",\n" + "
        // \"sort\": 4,\n" + " \"servicelists\": [\n" + " \"1522\",\n" + " \"1523\",\n"
        // + " \"1524\",\n" + " \"1528\",\n" + " \"1531\",\n" + " \"1532\"\n" + " ]\n" +
        // " },\n" + " {\n" + " \"name\": \"古典音樂\",\n" + " \"icon\":
        // \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330777070.png\",\n" + "
        // \"sort\": 5,\n" + " \"servicelists\": [\n" + " \"1529\",\n" + " \"1530\",\n"
        // + " \"1537\"\n" + " ]\n" + " },\n" + " {\n" + " \"name\": \"其他音樂\",\n" + "
        // \"icon\":
        // \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586331013427.png\",\n" + "
        // \"sort\": 6,\n" + " \"servicelists\": [\n" + " \"1520\",\n" + " \"1533\",\n"
        // + " \"1538\",\n" + " \"1539\",\n" + " \"1540\",\n" + " \"1541\",\n" + "
        // \"1542\"\n" + " ]\n" + " }\n" + " ]";
        Log.d(TAG, "get_current_category: musicList = " + musicList);

        if (musicList == null)
            return new ArrayList<>();

        return JsonParser.parse_music_info(musicList);
    }

    // public void testCallback(String s) {
    // Bundle requestBundle = new Bundle(),replyBundle;
    // requestBundle.setClassLoader(gContext.getClassLoader());
    // requestBundle.putInt(COMMAND_ID,
    // ScanModule.CMD_ServicePlayer_SCAN_CALLBACK_TEST);
    // requestBundle.putString("test", s);
    // replyBundle = invokeBundle(requestBundle);
    // replyBundle.getInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
    // }

    public int init_tuner() {
        Log.d("zzzz", "init_tuner " + System.currentTimeMillis() + " ms");
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, FeModule.CMD_ServicePlayer_FE_TunerInit);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        return 0;
    }

    public int start_epg(long channelID) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putInt(COMMAND_ID, EpgModule.CMD_ServicePlayer_EPG_StartEpg);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelID);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        return 0;
        // return mServiceInterface.get_prime_dtv().start_epg(channelID);
    }

    // public void setup_epg_channel() {
    // Bundle requestBundle = new Bundle(),replyBundle;
    // List<ProgramInfo> tvChList = get_program_info_list(
    // FavGroup.ALL_TV_TYPE,
    // com.prime.datastructure.sysdata.MiscDefine.ProgramInfo.POS_ALL,
    // com.prime.datastructure.sysdata.MiscDefine.ProgramInfo.NUM_ALL);
    // List<ProgramInfo> radioChList = get_program_info_list(
    // FavGroup.ALL_RADIO_TYPE,
    // com.prime.datastructure.sysdata.MiscDefine.ProgramInfo.POS_ALL,
    // com.prime.datastructure.sysdata.MiscDefine.ProgramInfo.NUM_ALL);
    //
    // List<ProgramInfo> allChList = new ArrayList<>();
    // allChList.addAll(tvChList);
    // allChList.addAll(radioChList);
    // ArrayList<EpgModule.EpgSendDataIdParam> channels = new ArrayList<>();
    //// ArrayList<Long> channels = new ArrayList<>();
    //// ArrayList<Integer> sids = new ArrayList<>();
    //// ArrayList<Integer> tsids = new ArrayList<>();
    //// ArrayList<Integer> onids = new ArrayList<>();
    // if(allChList.size() > 0) {
    // for (ProgramInfo p : allChList) {
    // EpgModule.EpgSendDataIdParam channel = new EpgModule.EpgSendDataIdParam();
    // channel.setChannelId(p.getChannelId());
    // channel.setSid(p.getServiceId());
    // channel.setTsid(p.getTransportStreamId());
    // channel.setOnid(p.getOriginalNetworkId());
    // channels.add(channel);
    // }
    // requestBundle.setClassLoader(gContext.getClassLoader());
    // requestBundle.putInt(COMMAND_ID,
    // EpgModule.CMD_ServicePlayer_EPG_SendEpgDataId);
    // requestBundle.putParcelableArrayList(ProgramInfo.CHANNEL_ID, channels);
    // replyBundle = invokeBundle(requestBundle);
    // replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    //// return 0;
    //// CMD_ServicePlayer_EPG_SendEpgDataId
    //// mServiceInterface.get_prime_dtv().setup_epg_channel();
    // }
    // }

    public int pvr_init(String usbMountPath) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_Init);
        requestBundle.putString(PvrModule.KEY_USB_MOUNT_PATH, usbMountPath);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int pvr_deinit() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_DeInit);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public boolean pvr_change_channel_manager_is_full_fecording() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_IsFullRecording);
        replyBundle = invokeBundle(requestBundle);
        boolean ret = false;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            ret = replyBundle.getBoolean(VALUE, false);
        return ret;
    }

    public boolean pvr_change_channel_manager_is_timeshift_start() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_IsTimeshiftStart);
        replyBundle = invokeBundle(requestBundle);
        boolean ret = false;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            ret = replyBundle.getBoolean(VALUE, false);
        return ret;
    }

    public boolean pvr_change_channel_manager_is_channel_recording(long channelId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_IsChannelRecording);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelId);
        replyBundle = invokeBundle(requestBundle);
        boolean ret = false;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            ret = replyBundle.getBoolean(VALUE, false);
        return ret;
    }

    public boolean pvr_change_channel_manager_record_start(long channelId, int eventId, int duration,
            boolean isSeries) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_RecordStart);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelId);
        requestBundle.putInt(BookInfo.EPG_EVENT_ID, eventId);
        requestBundle.putInt(BookInfo.DURATION, duration);
        requestBundle.putBoolean(BookInfo.IS_SERIES, isSeries);
        replyBundle = invokeBundle(requestBundle);
        boolean ret = false;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            ret = replyBundle.getBoolean(VALUE, false);
        return ret;
    }

    public int pvr_change_channel_manager_record_stop(long channelId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_RecordStop);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID, channelId);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        return 0;
    }

    public int pvr_change_channel_manager_get_rec_num() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_GetRecNum);
        replyBundle = invokeBundle(requestBundle);
        int value = 0;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            value = replyBundle.getInt(VALUE, 0);
        return value;
    }

    public int pvr_change_channel_manager_get_rec_tuner_id() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_GetRecTunerId);
        replyBundle = invokeBundle(requestBundle);
        int value = 0;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS)
            value = replyBundle.getInt(VALUE, 0);
        return value;
    }

    public int pvr_change_channel_manager_playback_start(PvrRecIdx pvrRecIdx, boolean fromLastPos) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_PlaybackStart);
        requestBundle.putInt(PvrModule.KEY_MASTER_REC_INDEX, pvrRecIdx.getMasterIdx());
        requestBundle.putInt(PvrModule.KEY_SERIES_REC_INDEX, pvrRecIdx.getSeriesIdx());
        requestBundle.putBoolean(PvrModule.KEY_FROM_LAST_POS, fromLastPos);
        replyBundle = invokeBundle(requestBundle);

        int playbackTunerId = -1;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            playbackTunerId = replyBundle.getInt(AvModule.PlayId_string, 0);
        }

        return playbackTunerId;
    }

    public int pvr_change_channel_manager_playback_stop() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_ChangeChannelManager_PlaybackStop);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int pvr_playback_resume(int playbackTunerId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_PlayPlay);
        requestBundle.putInt(AvModule.PlayId_string, playbackTunerId);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int pvr_playback_pause(int playbackTunerId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_PlayPause);
        requestBundle.putInt(AvModule.PlayId_string, playbackTunerId);
        replyBundle = invokeBundle(requestBundle);
        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int pvr_playback_get_play_time(int playbackTunerId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_PlayGetPlayTime);
        requestBundle.putInt(AvModule.PlayId_string, playbackTunerId);
        replyBundle = invokeBundle(requestBundle);

        int playbackTimeSecs = 0;
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            playbackTimeSecs = replyBundle.getInt(PvrModule.KEY_PLAY_TIME, 0);
        }

        return playbackTimeSecs;
    }

    public int pvr_playback_seek(int playbackTunerId, PvrInfo.EnSeekMode seekMode, int offsetSecs) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_PlaySeek);
        requestBundle.putInt(AvModule.PlayId_string, playbackTunerId);
        requestBundle.putInt(PvrModule.KEY_SEEK_MODE, seekMode.getValue());
        requestBundle.putInt(PvrModule.KEY_SEEK_OFFSET_SECS, offsetSecs);
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int pvr_playback_set_speed(int playbackTunerId, PvrInfo.EnPlaySpeed playSpeed) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_PlaySetSpeed);
        requestBundle.putInt(AvModule.PlayId_string, playbackTunerId);
        requestBundle.putInt(PvrModule.KEY_SPEED, playSpeed.getValue());
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int pvr_playback_change_audio_track(int playbackTunerId, AudioInfo.AudioComponent audioComponent) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_PlayChangeAudioTrack);
        requestBundle.putInt(AvModule.PlayId_string, playbackTunerId);
        requestBundle.putParcelable(AvModule.AudioComponent_string, audioComponent);
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public String pvr_get_usb_mount_path() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_GetMountUsbPath);
        replyBundle = invokeBundle(requestBundle);

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getString(PvrModule.KEY_USB_MOUNT_PATH, "");
        } else {
            return "";
        }
    }

    public int pvr_delete_one_rec(PvrRecIdx pvrRecIdx) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_DelOneRec);
        requestBundle.putInt(PvrModule.KEY_MASTER_REC_INDEX, pvrRecIdx.getMasterIdx());
        requestBundle.putInt(PvrModule.KEY_SERIES_REC_INDEX, pvrRecIdx.getSeriesIdx());
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getInt(PvrModule.KEY_COUNT, 0);
        } else {
            return -1;
        }
    }

    public int pvr_delete_all_recs() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, PvrModule.CMD_ServicePlayer_PVR_DelAllRecs);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getInt(PvrModule.KEY_COUNT, 0);
        } else {
            return -1;
        }
    }

    public Set<TvContentRating> tv_input_manager_get_ratings() {
        Set<TvContentRating> ratings = new HashSet<>();
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, TvInputManagerModule.CMD_ServicePlayer_TvInputManager_GetRatings);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            int num = replyBundle.getInt("RatingNum");
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    String domain = replyBundle.getString("Domain");
                    String ratingSystem = replyBundle.getString("RatingSystem");
                    String ratingValue = replyBundle.getString("RatingValue");
                    ratings.add(TvContentRating.createRating(domain, ratingSystem, ratingValue));
                }
            }
        }
        return ratings;
    }

    public void tv_input_manager_set_parental_rating_enable(boolean enable) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, TvInputManagerModule.CMD_ServicePlayer_TvInputManager_SetParentalRatingEnable);
        requestBundle.putBoolean("RatingEnable", enable);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public void tv_input_manager_remove_all_ratings() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, TvInputManagerModule.CMD_ServicePlayer_TvInputManager_RemoveAllRatings);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public void tv_input_manager_add_ratings(TvContentRating inputContentRating) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, TvInputManagerModule.CMD_ServicePlayer_TvInputManager_AddRatings);
        requestBundle.putString("Domain", inputContentRating.getDomain());
        requestBundle.putString("RatingSystem", inputContentRating.getRatingSystem());
        requestBundle.putString("RatingValue", inputContentRating.getMainRating());
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public void set_hdmi_cec(int cec) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_SetHdmiCec);
        requestBundle.putInt("HDMICEC", cec);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public void set_dcp_level(int hdcplevel) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_SetHDCPLevel);
        requestBundle.putInt("HDCPLEVEL", hdcplevel);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public void set_hdmi_output_format(int output_format) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_SetHdmiOutputFormat);
        requestBundle.putInt("HDMIOUTPUTFORMAT", output_format);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public int get_hdmi_output_format() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_GetHdmiOutputFormat);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getInt("HDMIOUTPUTFORMATCOUNT", 6);
        }
        return 1;
    }

    public void set_system_language(String language) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CommonModule.CMD_ServicePlayer_COMMON_SetSystemLanguage);
        requestBundle.putString("LanguageCode", language);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public String get_cns_ota_download_info() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, OtaModule.CMD_ServicePlayer_OTA_GetUpdateInfo);
        replyBundle = invokeBundle(requestBundle);

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getString(OtaModule.KEY_OTA_UPDATE_REPLY, "");
        } else {
            return "";
        }
    }

    public String upload_first_boot_info() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, OtaModule.CMD_ServicePlayer_OTA_UploadFirstBootInfo);
        replyBundle = invokeBundle(requestBundle);

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getString(OtaModule.KEY_OTA_UPLOAD_FIRST_BOOT_REPLY, "");
        } else {
            return "";
        }
    }

    public String boot_login() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, OtaModule.CMD_ServicePlayer_OTA_BootLogin);
        replyBundle = invokeBundle(requestBundle);

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getString(OtaModule.KEY_OTA_BOOT_LOGIN_REPLY, "");
        } else {
            return "";
        }
    }

    public String upload_ota_update_status() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, OtaModule.CMD_ServicePlayer_OTA_UploadUpdateStatus);
        replyBundle = invokeBundle(requestBundle);

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getString(OtaModule.KEY_OTA_UPLOAD_UPDATE_STATUS_REPLY, "");
        } else {
            return "";
        }
    }

    public void start_ota_update() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, OtaModule.CMD_ServicePlayer_OTA_StartUpdate);
        replyBundle = invokeBundle(requestBundle);

        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
        }
    }

    public List<BookInfo> book_info_get_list() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_BookInfoGetList);
        replyBundle = invokeBundle(requestBundle);
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            return replyBundle.getParcelableArrayList(COMMAND_REPLY_OBJ, BookInfo.class);
        }

        return new ArrayList<>();
    }

    public BookInfo book_info_get(int bookId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_BookInfoGet);
        requestBundle.putInt(BookInfo.BOOK_ID, bookId);
        replyBundle = invokeBundle(requestBundle);

        BookInfo bookInfo = null;
        replyBundle.setClassLoader(gContext.getClassLoader());
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            bookInfo = replyBundle.getParcelable(BookInfo.TAG, BookInfo.class);
        }

        return bookInfo;
    }

    public int book_info_add(BookInfo bookInfo) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_BookInfoAdd);
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putParcelable(BookInfo.TAG, bookInfo);
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int book_info_delete(int bookId) {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_BookInfoDelete);
        requestBundle.putInt(BookInfo.BOOK_ID, bookId);
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int book_info_delete_all() {
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_BookInfoDeleteAll);
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int set_alarm(BookInfo bookInfo) {
        Bundle requestBundle = new Bundle(), replyBundle;

        Calendar calendar = Calendar.getInstance();
        calendar.set(bookInfo.getYear(),
                bookInfo.getMonth() - 1, // bookinfo month = 1 ~ 12, -1 for Calendar
                bookInfo.getDate(),
                bookInfo.getStartTime() / 100,
                bookInfo.getStartTime() % 100,
                0);

        // adjust record/remind alarm time
        // e.g. ADVANCE_REMINDER_TIME = -1 if we need the alarm to be triggered 1 min
        // earlier
        if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD
                || bookInfo.getBookType() == BookInfo.BOOK_TYPE_CHANGE_CHANNEL) {
            calendar.add(Calendar.MINUTE, BookInfo.ADVANCE_REMINDER_TIME);
        }

        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_SetAlarm);
        requestBundle.putInt(BookInfo.BOOK_ID, bookInfo.getBookId());
        requestBundle.putParcelable(BookModule.KEY_BOOK_INTENT, bookInfo.getIntent());
        requestBundle.putLong(BookInfo.START_TIME_MS, calendar.getTimeInMillis());
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int cancel_alarm(BookInfo bookInfo) {
        Bundle requestBundle = new Bundle(), replyBundle;

        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_CancelAlarm);
        requestBundle.putInt(BookInfo.BOOK_ID, bookInfo.getBookId());
        requestBundle.putParcelable(BookModule.KEY_BOOK_INTENT, bookInfo.getIntent());
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public int schedule_next_timer(BookInfo bookInfo) {
        Bundle requestBundle = new Bundle(), replyBundle;

        requestBundle.putInt(COMMAND_ID, BookModule.CMD_ServicePlayer_BOOK_ScheduleNextTimer);
        requestBundle.setClassLoader(gContext.getClassLoader());
        requestBundle.putParcelable(BookInfo.TAG, bookInfo);
        replyBundle = invokeBundle(requestBundle);

        return replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
    }

    public List<String> get_entitled_channel_ids() {
        List<String> entitlement_list = new ArrayList<>();
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CaModule.CMD_ServicePlayer_CA_GetEntitlementList);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            int count = replyBundle.getInt("NumOfIds", 0);
            for (int i = 0; i < count; i++) {
                String entitlement_id = replyBundle.getString("EntitlementId");
                entitlement_list.add(entitlement_id);
            }
        }
        return entitlement_list;
    }

    public List<String> get_promotion_channel_ids() {
        List<String> promotion_list = new ArrayList<>();
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CaModule.CMD_ServicePlayer_CA_GetPromotionList);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            int count = replyBundle.getInt("NumOfIds", 0);
            for (int i = 0; i < count; i++) {
                String promotion_id = replyBundle.getString("PromotionId");
                promotion_list.add(promotion_id);
            }
        }
        return promotion_list;
    }

    public CasData get_cas_data() {
        CasData casData = null;
        Bundle requestBundle = new Bundle(), replyBundle;
        requestBundle.putInt(COMMAND_ID, CaModule.CMD_ServicePlayer_CA_GetCasData);
        replyBundle = invokeBundle(requestBundle);
        if (replyBundle.getInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL) == COMMAND_REPLY_SUCCESS) {
            String rawJsonString = replyBundle.getString("mRawJsonString", "");
            LogUtils.d("rawJsonString = " + rawJsonString);
            if (rawJsonString.length() != 0) {
                casData = new CasData(rawJsonString);
                LogUtils.d("get_cas_data :" + casData.toString());
            }
        }
        return casData;

    }
}
