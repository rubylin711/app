package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.SubtitleInfo;

public class SubtitleModule {
    private static final String TAG = "SubtitleModule";

    private static final int CMD_SUBT_Base = PrimeDtvMediaPlayer.CMD_Base + 0xC00;

    //subt
    private static final int CMD_SUBT_SetMode = CMD_SUBT_Base + 0x01;
    private static final int CMD_SUBT_GetMode = CMD_SUBT_Base + 0x02;
    private static final int CMD_SUBT_GetList = CMD_SUBT_Base + 0x03;
    private static final int CMD_SUBT_Switch = CMD_SUBT_Base + 0x04;
    private static final int CMD_SUBT_GetLang = CMD_SUBT_Base + 0x05;
    private static final int CMD_SUBT_SetLang = CMD_SUBT_Base + 0x06;
    private static final int CMD_SUBT_SetHohPreferred = CMD_SUBT_Base + 0x07;
    private static final int CMD_SUBT_GetHohPreferred = CMD_SUBT_Base + 0x08;


    public SubtitleInfo.SubtitleComponent av_control_get_current_subtitle(int playId) {
        Log.d(TAG, "av_control_get_current_subtitle: ");
        return get_current_subtitle(playId);
    }

    public SubtitleInfo av_control_get_subtitle_list(int playId) {
        Log.d(TAG, "av_control_get_subtitle_list: ");
        return get_subtitle_components(playId);
    }

    public int av_control_select_subtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent) {
        Log.d(TAG, "av_control_select_subtitle: ");
        return select_subtitle(playId, subtitleComponent);
    }

    public int av_control_show_subtitle(int playId, boolean enable) {
        Log.d(TAG, "av_control_show_subtitle: ");
        return show_subtitle(playId, enable);
    }

    public boolean av_control_is_subtitleVisible(int playId) {
        Log.d(TAG, "AvControlIsSubtitleVisible: ");
        return is_subtitle_visible(playId);
    }

    public int av_control_set_subt_hoh_preferred(int playId, boolean on) {
        Log.d(TAG, "av_control_set_subt_hoh_preferred: ");
        return set_subtitle_hoh_preferred(playId, on);
    }

    public int av_control_set_subtitle_language(int playId, int index, String lang) {
        Log.d(TAG, "av_control_set_subtitle_language: ");
        return set_subtitle_language(playId, index, lang);
    }

    private SubtitleInfo.SubtitleComponent get_current_subtitle(int playerID) {
        Log.d(TAG, "get_current_subtitle()");

        SubtitleInfo.SubtitleComponent subtitleComponent = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_GetList);
        int curIndex = 0;

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int cnt = reply.readInt();
            curIndex = reply.readInt();
            if (cnt > 0) {
                for (int i = 0; i < cnt; i++) {
                    int type = reply.readInt();
                    String languageCode = null;
                    int langCode0 = reply.readInt();
                    int langCode1 = reply.readInt();
                    int langCode2 = reply.readInt();
                    languageCode = String.format("%c%c%c", langCode0, langCode1, langCode2);

                    subtitleComponent = new SubtitleInfo.SubtitleComponent();
                    subtitleComponent.setLangCode(languageCode);
                    subtitleComponent.setPos(curIndex + 1);

                    switch (type) {
                        case 2:
                            subtitleComponent.setType(/*EnSubtitleType.TELETEXT*/2);
                            break;
                        case 0:
                        default:
                            subtitleComponent.setType(/*EnSubtitleType.SUBTITLE*/0);
                            break;
                    }


                    if (i == curIndex) {
                        return subtitleComponent;
                    }
                    Log.d(TAG, "getSubtitleComponents:languageCode = " + languageCode
                            + " type = " + type);
                }
            }
        }
        request.recycle();
        reply.recycle();

        return subtitleComponent;

    }

    private SubtitleInfo.SubtitleComponent get_subt_off_item() {
        final String OFF_STATUS = "off";

        SubtitleInfo.SubtitleComponent subtOffItem = new SubtitleInfo.SubtitleComponent();
        subtOffItem.setLangCode(OFF_STATUS);
        subtOffItem.setPid(0);
        subtOffItem.setType(/*EnSubtitleType.SUBTITLE*/0);
//        subtOffItem.setSubtComponentType(EnSubtComponentType.NORMAL);
        subtOffItem.setPos(0);

        return subtOffItem;
    }

    private SubtitleInfo get_subtitle_components(int playID) {
        SubtitleInfo subtitleInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_GetList);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int cnt = reply.readInt();
            reply.readInt();
            if (cnt > 0) {
                subtitleInfo = new SubtitleInfo();

                /*add off item*/
                subtitleInfo.Component.add(get_subt_off_item());

                /*add subt item*/
                for (int i = 0; i < cnt; i++) {
                    int type = reply.readInt();
                    String languageCode = null;
                    int langCode0 = reply.readInt();
                    int langCode1 = reply.readInt();
                    int langCode2 = reply.readInt();
                    languageCode = String.format("%c%c%c", langCode0, langCode1, langCode2);

                    SubtitleInfo.SubtitleComponent subtComponent = new SubtitleInfo.SubtitleComponent();
                    subtComponent.setLangCode(languageCode);
                    subtComponent.setPos(i + 1);

                    switch (type) {
                        case 2:
                            subtComponent.setType(/*EnSubtitleType.TELETEXT*/2);
                            break;
                        case 0:
                        default:
                            subtComponent.setType(/*EnSubtitleType.SUBTITLE*/0);
                            break;
                    }


                    subtitleInfo.Component.add(subtComponent);
                    Log.d(TAG, "getSubtitleComponents:languageCode = " + languageCode
                            + " type = " + type);
                }

                SubtitleInfo.SubtitleComponent currentSubtitle = get_current_subtitle(playID);    // Johnny add 20180214 to set curPos of SubInfo
                if (currentSubtitle != null) {
                    subtitleInfo.setCurPos(currentSubtitle.getPos());
                } else {
                    subtitleInfo.setCurPos(0);
                }
            }
        }
        request.recycle();
        reply.recycle();
        return subtitleInfo;
    }

    private int select_subtitle(int playerID, SubtitleInfo.SubtitleComponent subtitleComponent) {
        if (null == subtitleComponent) {
            Log.e(TAG, "the param of subitleComponent is null");

            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        if (0 == subtitleComponent.getPos()) {
            return show_subtitle(playerID, false);
        }

        Log.d(TAG, "selectSubtitle(position = " + subtitleComponent.getPos() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_Switch);

        request.writeInt(subtitleComponent.getPos() - 1);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        Log.d(TAG, "ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int show_subtitle(int playerID, boolean bShow) {
        Log.d(TAG, "show_subtitle(" + bShow + ")");

        int tmpFlag = (bShow) ? 1 : 0;

        return PrimeDtvMediaPlayer.excute_command(CMD_SUBT_SetMode, tmpFlag);
    }

    private boolean is_subtitle_visible(int playerID) {
        Log.d(TAG, "is_subtitle_visible()");
        return 1 == PrimeDtvMediaPlayer.excute_command_getII(CMD_SUBT_GetMode);
    }

    private int set_subtitle_hoh_preferred(int playerID, boolean bOn) {
        Log.d(TAG, "set_subtitle_hoh_preferred()");

        int tmpFlag = (bOn) ? 1 : 0;

        return PrimeDtvMediaPlayer.excute_command(CMD_SUBT_SetHohPreferred, tmpFlag);
    }

    private int set_subtitle_language(int playerID, int index, String lang) {
        Log.d(TAG, "set_subtitle_language(" + lang + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_SetLang);
        request.writeInt(index);
        request.writeString(lang);


        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "set_subtitle_language(" + lang + ")" + ", ret=" + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
}
