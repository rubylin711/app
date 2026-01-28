package com.prime.homeplus.tv.utils;

import static com.prime.homeplus.tv.error.ErrorMessageResolver.getErrorMessage;

import android.content.Context;
import android.os.SystemProperties;
import android.view.View;
import android.widget.TextView;

import androidx.tvprovider.media.tv.Channel;

import com.google.android.material.snackbar.Snackbar;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.ErrorCode;
import com.prime.datastructure.utils.LogUtils;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;

public class PrimeUtils {
    public static final String TAG = "PrimeUtils";

    public static String SO_ID_NEW = "persist.sys.inspur.cns.soid";
    public static PrimeDtvServiceInterface g_prime_dtv = PrimeHomeplusTvApplication.get_prime_dtv_service();

    public static ProgramInfo getProgramInfo(long channelId) {
        return g_prime_dtv.get_program_by_channel_id(channelId);
    }

    public static ProgramInfo getProgramInfo(Channel channel) {
        if (channel == null)
            return null;
        int sid = channel.getServiceId();
        int onid = channel.getOriginalNetworkId();
        int tsid = channel.getTransportStreamId();
        return g_prime_dtv.get_program_by_SId_OnId_TsId(sid,onid,tsid);
    }

    public static TpInfo getTpInfo(int tpID) {
        return g_prime_dtv.tp_info_get(tpID);
    }

    public static int getECMPid(ProgramInfo programInfo) {
        if (programInfo == null)
            return 0;
        return programInfo.pVideo.getEcmPid(Pvcfg.ALTIMEDIA_CA_SYSTEM_ID);
    }

    public static int getVideoPid(ProgramInfo programInfo) {
        if (programInfo == null)
            return 0;
        return programInfo.pVideo.getPID();
    }

    public static int getAudioPid(ProgramInfo programInfo) {
        if (programInfo == null)
            return 0;
        return programInfo.pAudios.get(programInfo.getAudioSelected()).getPid();
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

    public static String getSoId(Context context) {
//        String soId = SystemProperties.get(SO_ID_NEW, "00");
        String soId = GposInfo.getSo(context);
        return soId;
    }

    public static String getAreaCode(Context context) {
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        String areaCode = "";
        if(gposInfo != null)
            areaCode = GposInfo.getAreadCode(context);
        return areaCode;
    }

    public static String getDefaultSubtitleLang(Context context) {
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        String lang = "";
        if(gposInfo != null)
            lang = GposInfo.getSubtitleLanguageSelection(context, 0);
        return lang;
    }

    public static int getSubtitleOnOff(Context context) {
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        int onoff = 0;
        if(gposInfo != null)
            onoff = GposInfo.getSubtitleOnOff(context);
        return onoff;
    }

    public static String convertErrorCode(Context context, int errorCode){
        String errorMsg = "";
        switch (errorCode) {
            case ErrorCode.ERROR_E010:
                errorMsg = "E025";
                break;
            case ErrorCode.ERROR_E213:
                errorMsg = "E029";
                break;
            case ErrorCode.ERROR_E507:
                errorMsg = "E006";
            break;
            case ErrorCode.ERROR_E511:
                errorMsg = "E014";
                break;
            default:
                LogUtils.d("unhandle error code "+errorCode);
        }
        return errorMsg;
    }

    public static GposInfo get_gpos_info() {
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        return gposInfo;
    }

    public static void showCenterSnackBar(View view, int resId) {
        showCenterSnackBar(view, view.getResources().getText(resId));
    }

    public static void showCenterSnackBar(View view, CharSequence message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }

        snackbar.show();
    }
}
