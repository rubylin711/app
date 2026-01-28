package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.TeletextInfo;

import java.util.List;

public class TeletextModule {
    private static final String TAG = "TeletextModule";

    private static final int CMD_TTX_BASE = PrimeDtvMediaPlayer.CMD_Base + 0xD00;

    //ttx
    private static final int CMD_TTX_Show = CMD_TTX_BASE + 0x01;
    private static final int CMD_TTX_Hide = CMD_TTX_BASE + 0x02;
    private static final int CMD_TTX_IsShow = CMD_TTX_BASE + 0x03;
    private static final int CMD_TTX_IsAvailable = CMD_TTX_BASE + 0x04;
    private static final int CMD_TTX_SetLanguage = CMD_TTX_BASE + 0x05;
    private static final int CMD_TTX_GetLanguage = CMD_TTX_BASE + 0x06;
    private static final int CMD_TTX_GetCurrentPage = CMD_TTX_BASE + 0x07;
    private static final int CMD_TTX_SetInitPage = CMD_TTX_BASE + 0x08;
    private static final int CMD_TTX_SetCommand = CMD_TTX_BASE + 0x09;
    private static final int CMD_TTX_SetRegion = CMD_TTX_BASE + 0x0A;
    private static final int CMD_TTX_GetRegion = CMD_TTX_BASE + 0x0B;
    private static final int CMD_TTX_GetLangInfo = CMD_TTX_BASE + 0x0C;

    // JAVA CMD

    //not exist
    private static final int CMD_AV_GetTeletextList = PrimeDtvMediaPlayer.CMD_JAVA_Base + CMD_TTX_BASE + 0x01;
    //not exist end


    public TeletextInfo av_control_get_current_teletext(int playId) {
        Log.d(TAG, "av_control_get_current_teletext: ");
        return get_current_teletext(playId);
    }

    public List<TeletextInfo> av_control_get_teletext_list(int playId) {
        Log.d(TAG, "av_control_get_teletext_list: ");
        return get_teletext_components(playId);
    }

    public int av_control_show_teletext(int playId, boolean enable) {
        Log.d(TAG, "av_control_show_teletext: ");
        return show_teletext(playId, enable);
    }

    public boolean av_control_is_teletext_visible(int playId) {
        Log.d(TAG, "av_control_is_teletext_visible: ");
        return is_teletext_visible(playId);
    }

    public boolean av_control_is_teletext_available(int playId) {
        Log.d(TAG, "av_control_is_teletext_available: ");
        return is_teletext_available(playId);
    }

    public int av_control_set_teletext_language(int playId, String primeLang) {
        Log.d(TAG, "av_control_set_teletext_language: ");
        return set_teletext_language(playId, primeLang);
    }

    public String av_control_get_teletext_language(int playId) {//eric lin 20180705 get ttx lang
        Log.d(TAG, "av_control_get_teletext_language: ");
        return get_teletext_language(playId);
    }

    public int av_control_set_command(int playId, int keyCode) {
        Log.d(TAG, "av_control_set_command: ");
        return setCommand(playId, keyCode);
    }

    private TeletextInfo get_current_teletext(int playID) {
        Log.d(TAG, "get_current_teletext()");

        TeletextInfo ttxComponent = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TTX_GetLangInfo);//get info

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int magazineNum = reply.readInt();
            int pageNum = reply.readInt();
            reply.readInt();//pageSubCode

            ttxComponent = new TeletextInfo();
            ttxComponent.setMagazingNum(magazineNum);
            ttxComponent.setPageNum(pageNum);

            Log.d(TAG, "get_current_teletext() magazineNum = "
                    + magazineNum + ",pageNum = " + pageNum);
        }

        request.recycle();
        reply.recycle();

        return ttxComponent;
    }

    private List<TeletextInfo> get_teletext_components(int playID) {
        Log.d(TAG, "get_teletext_components() not used.");
        return null;

        /*Log.d(TAG, "getTeletextList()");

        List<TeletextInfo> ttxList = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetTeletextList);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int cnt = reply.readInt();
            if (cnt > 0) {
                ttxList = new ArrayList<TeletextInfo>();
                for (int i = 0; i < cnt; i++) {
                    String languageCode = reply.readString();
                    int magazineNum = reply.readInt();
                    int pageNum = reply.readInt();

                    TeletextInfo ttxComponent = new TeletextInfo();
                    ttxComponent.setLangCode(languageCode);
                    ttxComponent.setMagazingNum(magazineNum);
                    ttxComponent.setPageNum(pageNum);

                    ttxList.add(ttxComponent);
                    Log.d(TAG, "getTeletextList:languageCode = " + languageCode + "magazineNum = "
                            + magazineNum + "pageNum = " + pageNum);
                }
            }
        }

        request.recycle();
        reply.recycle();
        return ttxList;*/
    }

    private int show_teletext(int playID, boolean bShow) {
        Log.d(TAG, "show_teletext(" + bShow + ")");

        int tmpFlag = (bShow) ? 1 : 0;

        if (bShow) {
            return PrimeDtvMediaPlayer.excute_command(CMD_TTX_Show);
        } else {
            return PrimeDtvMediaPlayer.excute_command(CMD_TTX_Hide);
        }
    }

    private boolean is_teletext_visible(int playID) {
        Log.d(TAG, "is_teletext_visible()");
        return 1 == PrimeDtvMediaPlayer.excute_command_getII(CMD_TTX_IsShow);
    }

    private boolean is_teletext_available(int playID) {
        Log.d(TAG, "is_teletext_available()");
        return 1 == PrimeDtvMediaPlayer.excute_command_getII(CMD_TTX_IsAvailable);
    }

    private int set_teletext_language(int playID, String primaryTTXLang) {
        Log.d(TAG, "set_teletext_language(" + primaryTTXLang + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TTX_SetLanguage);

        request.writeString(primaryTTXLang);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "set_teletext_language fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public String get_teletext_language(int playID)//eric lin 20180705 get ttx lang
    {
        String ttxLang = "";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TTX_GetLanguage);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            ttxLang = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return ttxLang;
    }

    private int setCommand(int playID, /*EnCMDCode*/int code) {
        return PrimeDtvMediaPlayer.excute_command(CMD_TTX_SetCommand, /*code.getValue()*/code);
    }
}
