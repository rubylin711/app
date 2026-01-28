package com.prime.dtv.module;

import android.graphics.Rect;
import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.AudioInfo;
import com.prime.dtv.sysdata.EnAudioTrackMode;
import com.prime.dtv.sysdata.EnPlayStatus;

public class AvModule {
    private static final String TAG = "AvModule";

    private static final int CMD_AV_Base = PrimeDtvMediaPlayer.CMD_Base + 0x200;

    // AV Command
    private static final int CMD_AV_Open = CMD_AV_Base + 0x01;
    private static final int CMD_AV_Close = CMD_AV_Base + 0x02;
    private static final int CMD_AV_Start = CMD_AV_Base + 0x03;
    private static final int CMD_AV_Stop = CMD_AV_Base + 0x04;
    private static final int CMD_AV_SetAudioLanguage = CMD_AV_Base + 0x05;
    private static final int CMD_AV_GetAudioLanguage = CMD_AV_Base + 0x06;
    private static final int CMD_AV_GetMutiAudioInfo = CMD_AV_Base + 0x07;
    private static final int CMD_AV_ChangeAudioTrack = CMD_AV_Base + 0x08;
    private static final int CMD_AV_SetAudioTrackMode = CMD_AV_Base + 0x09;
    private static final int CMD_AV_GetAudioTrackMode = CMD_AV_Base + 0x0A;
    private static final int CMD_AV_SetWindowSize = CMD_AV_Base + 0x0B;
    private static final int CMD_AV_ShowVideo = CMD_AV_Base + 0x0C;
    private static final int CMD_AV_FreezeVideo = CMD_AV_Base + 0x0D;
    private static final int CMD_AV_SetMute = CMD_AV_Base + 0x0E;
    private static final int CMD_AV_GetMute = CMD_AV_Base + 0x0F;
    private static final int CMD_AV_SetStopMode = CMD_AV_Base + 0x10;
    private static final int CMD_AV_GetStopMode = CMD_AV_Base + 0x11;
    private static final int CMD_AV_GetAdAttr = CMD_AV_Base + 0x12;
    private static final int CMD_AV_SetAdAttr = CMD_AV_Base + 0x13;
    private static final int CMD_AV_GetAVStatus = CMD_AV_Base + 0x14;
    private static final int CMD_AV_EwsActionControl = CMD_AV_Base + 0x15;
    private static final int CMD_AV_GetDRARawChannel = CMD_AV_Base + 0x16;
    private static final int CMD_AV_GetWindowSize = CMD_AV_Base + 0x17;
    private static final int CMD_AV_GetAspectRatio = CMD_AV_Base + 0x64;
    private static final int CMD_AV_SetAudioOuputMode = CMD_AV_Base + 0x65;
    private static final int CMD_AV_SetDispRatio = CMD_AV_Base + 0x66;
    private static final int CMD_AV_PreStart = CMD_AV_Base + 0x67;//Scoty 20180816 add fast change channel
    private static final int CMD_AV_MIC_SetInputGain = CMD_AV_Base + 0x68;
    private static final int CMD_AV_MIC_SetAlcGain = CMD_AV_Base + 0x69;
    private static final int CMD_AV_MIC_SetLRInputGain = CMD_AV_Base + 0x6A;
    private static final int CMD_AV_GetVideoErrorFrameCount = CMD_AV_Base + 0x6B;


    public int av_control_open(int playerID) {
        Log.d(TAG, "av_control_open()");
        if (0 != playerID) {
            Log.d(TAG, "av_control_open() only support one instance.");
            return 0;
        }

        return PrimeDtvMediaPlayer.excute_command(CMD_AV_Open);
    }

    public int av_control_close(int playerID, int resourceType) {
        Log.d(TAG, "av_control_close(" + playerID + "),type(" + resourceType + ")");
        if (0 != playerID) {
            Log.d(TAG, "av_control_close() only support one instance.");
            return 0;
        }

        return PrimeDtvMediaPlayer.excute_command(CMD_AV_Close);
    }

    public int av_control_start(int playerID, long channelID, int show) {
        Log.d(TAG, "av_control_start(" + channelID + ")");

        //  show = 1 means true play, 0 means send program in play, but not play at once
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_Start, channelID, show);
    }

    public int av_control_stop(int playerID) {
        Log.d(TAG, "av_control_stop: ");

        int stopType = av_get_stop_mode(playerID);
        Log.d(TAG, "av_control_stop: stopType=" + stopType);

        return PrimeDtvMediaPlayer.excute_command(CMD_AV_Stop);
    }

    public AudioInfo get_current_audio(int playerID) {
        Log.d(TAG, "get_current_audio()");

        AudioInfo audioInfo = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetMutiAudioInfo);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int audioCount = reply.readInt();
            int curIndex = reply.readInt();

            audioInfo = new AudioInfo();
            audioInfo.setCurPos(curIndex);
            for (int i = 0; i < audioCount; i++) {
                int pid = reply.readInt();
                int audioType = reply.readInt();
                int adType = reply.readInt(); //u32Reserved
                int compTag = reply.readInt();
                int compType = reply.readInt();
                int streamContent = reply.readInt();
                int trackMode = reply.readInt(); // u8Reserved2
                int reserved3 = reply.readInt(); // u8Reserved3
                String languageCode = reply.readString();

                AudioInfo.AudioComponent audioComponent = new AudioInfo.AudioComponent();
                audioComponent.setLangCode(languageCode);
                audioComponent.setPid(pid);
                audioComponent.setAudioType(/*EnStreamType.valueOf(audioType)*/audioType);
                audioComponent.setAdType(/*EnAudioType.valueOf(adType)*/adType);
                audioComponent.setTrackMode(/*EnAudioTrackMode.valueOf(trackMode)*/EnAudioTrackMode.valueOf(trackMode).getValue());
                audioComponent.setPos(i);   // need test
                audioInfo.ComponentList.add(audioComponent);

                /*if (curIndex == i)
                {
                    Log.d(TAG, "getCurrentAudio:languageCode = " + languageCode + ",pid = " + pid + ",audioType = " + audioType);
                }*/
            }
        }

        request.recycle();
        reply.recycle();

        return audioInfo;
    }

    public int select_audio(int playerID, AudioInfo.AudioComponent audio) {
        int audioPid = audio.getPid();
        int index = 0;
        boolean bGet = false;
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request1.writeInt(CMD_AV_GetMutiAudioInfo);

        PrimeDtvMediaPlayer.invokeex(request1, reply1);
        int ret1 = reply1.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret1) {
            int audioCount = reply1.readInt();
            int curIndex = reply1.readInt();
            for (int i = 0; i < audioCount; i++) {
                int pid = reply1.readInt();
                int audioType = reply1.readInt();
                int adType = reply1.readInt(); //u32Reserved
                int compTag = reply1.readInt();
                int compType = reply1.readInt();
                int streamContent = reply1.readInt();
                int trackMode = reply1.readInt(); // u8Reserved2
                int reserved3 = reply1.readInt(); // u8Reserved3
                String languageCode = reply1.readString();

                if (pid == audioPid) {
                    index = i;
                    bGet = true;
                    break;
                }
            }
        }

        request1.recycle();
        reply1.recycle();

        if (!bGet) {
            Log.e(TAG, "not find this audio track(languageCode = " + audio.getLangCode() + ",pid =" + audio.getPid()
                    + ",type =" + audio.getAudioType() + ")");
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        Log.d(TAG, "selectAudio(languageCode = " + audio.getLangCode() + ",pid =" + audio.getPid()
                + ",type =" + audio.getAudioType() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_ChangeAudioTrack);
        request.writeInt(index);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        Log.d(TAG, "ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_track_mode(int playerID, EnAudioTrackMode enTrackMode) {
        Log.d(TAG, "set_track_mode(" + enTrackMode + ")");
        int track = enTrackMode.getValue();
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_SetAudioTrackMode, track);
    }

    public EnAudioTrackMode get_track_mode(int playerID) {
        Log.d(TAG, "get_track_mode()");
        EnAudioTrackMode trackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_BUTT;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAudioTrackMode);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int mode = reply.readInt();
            trackMode = EnAudioTrackMode.valueOf(mode);
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "get_track_mode(),trackMode = " + trackMode);
        return trackMode;
    }

    public int set_window_rect(int playerID, Rect rect) {
        Log.d(TAG, "set_window_rect(" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_SetWindowSize);
        request.writeInt(rect.left);
        request.writeInt(rect.top);
        request.writeInt(rect.width());
        request.writeInt(rect.height());

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int show_video(int playerID, boolean bShow) {
        Log.d(TAG, "show_video(" + bShow + ")");
        int tmpFlag = (bShow) ? 1 : 0;
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_ShowVideo, tmpFlag);
    }

    public int freeze_video(int playerID, boolean bFreeze) {
        Log.d(TAG, "freeze_video(" + bFreeze + ")");
        int tmpFlag = (bFreeze) ? 1 : 0;
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_FreezeVideo, tmpFlag);
    }

    public int set_mute_status(int playerID, boolean bMuteFlag) {
        Log.d(TAG, "set_mute_status(" + bMuteFlag + "),(id=" + playerID + ")");
        int tmp = (bMuteFlag) ? 1 : 0;
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_SetMute, tmp);
    }

    public boolean get_mute_status(int playerID) {
        Log.d(TAG, "get_mute_status");
        return 1 == PrimeDtvMediaPlayer.excute_command_getII(CMD_AV_GetMute);
    }

    public int set_stop_mode(int playerID, /*EnStopType*/int enStopMode) {
        Log.d(TAG, "set_stop_mode(" + enStopMode + ")");
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_SetStopMode, /*enStopMode.getValue()*/enStopMode);
    }

    public /*EnStopType*/int av_get_stop_mode(int playerID) {
        Log.d(TAG, "av_get_stop_mode()");
        /*EnStopType*/
        int enStopMode = /*EnStopType.FREEZE*/0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetStopMode);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int index = reply.readInt();
            enStopMode = /*EnStopType.valueOf(index)*/index;
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "av_get_stop_mode(),avGetStopMode = " + enStopMode);

        return enStopMode;
    }

    public EnPlayStatus get_play_status(int playerID) {
        Log.d(TAG, "get_play_status()");

        EnPlayStatus playStatus = EnPlayStatus.IDLE;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(0); // 0 means get play satus SVR_AV_GET_PLAY_STATUS

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int status = reply.readInt();
            playStatus = EnPlayStatus.valueOf(status);
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "get_play_status(), EnPlayStatus status = " + playStatus);

        return playStatus;
    }

    public int get_fps(int playerID) {
        Log.d(TAG, "get_fps(" + playerID + ")");
        int fps = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(1); // 1 means get play satus SVR_AV_GET_STATUS_INFO

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            fps = reply.readInt(); // fps
        }
        request.recycle();
        reply.recycle();
        return fps;
    }

    public int get_video_resolution_height(int playerID) {
        Log.d(TAG, "get_video_resolution_height(" + playerID + ")");
        int height = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(1); // 1 means get play satus SVR_AV_GET_STATUS_INFO

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "get_video_resolution_height" + ret);

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt(); // fps
            reply.readInt(); // width
            height = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return height;
    }

    public int get_video_resolution_width(int playerID) {
        Log.d(TAG, "get_video_resolution_width(" + playerID + ")");
        int width = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(1); // 1 means get play satus SVR_AV_GET_STATUS_INFO

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt(); // fps
            width = reply.readInt(); // width
        }
        request.recycle();
        reply.recycle();
        return width;
    }

    public int get_dolby_info_stream_type(int playerID) {
        Log.d(TAG, "get_dolby_info_stream_type(" + playerID + ")");
        int streamType = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(3); // 3 means get play satus SVR_AV_GET_DOLBYPLUS_STREAM_INFO

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            streamType = reply.readInt();  // streamType
        }
        request.recycle();
        reply.recycle();
        return streamType;
    }

    public int get_dolby_info_acmod(int playerID) {
        Log.d(TAG, "get_dolby_info_acmod(" + playerID + ")");
        int acmod = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(3); // 3 means get play satus SVR_AV_GET_DOLBYPLUS_STREAM_INFO

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt(); // streamType
            acmod = reply.readInt(); // acmod
        }
        request.recycle();
        reply.recycle();
        return acmod;
    }

    public int ews_action_control(int playerID, boolean bEnable) {
        Log.d(TAG, "ews_action_control(" + playerID + "," + bEnable + ")");

        int enableFlag = (bEnable) ? 1 : 0;
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_EwsActionControl, enableFlag);
    }

    public Rect get_window_size(int playerID) {
        Log.d(TAG, "get_window_size()");
        Rect rect = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetWindowSize);
        request.writeInt(playerID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int x = reply.readInt();
            int y = reply.readInt();
            int width = reply.readInt();
            int height = reply.readInt();

            rect = new Rect(x, y, x + width, y + height);
        }

        request.recycle();
        reply.recycle();

        return rect;
    }

    public int get_ratio(int playId) {
        Log.d(TAG, "get_ratio");
        int ratio = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAspectRatio);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            ratio = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "getRatio(),index = " + ratio);

        return ratio;
    }

    public int set_audio_output_mode(int playerID, int mode) {
        Log.d(TAG, "set_audio_output_mode(" + mode + ")");
        return PrimeDtvMediaPlayer.excute_command(CMD_AV_SetAudioOuputMode, mode);
    }

    public int set_ratio_conversion(int playId, int ratio, int conversion) {
        Log.d(TAG, "set_ratio_conversion,playId = " + playId + ",ratio = " + ratio + ",conversion = " + conversion);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_SetDispRatio);
        request.writeInt(ratio);
        request.writeInt(conversion);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "set_ratio_conversion(),ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int av_control_pre_play_stop() {
        Log.d(TAG, "av_control_pre_play_stop: ");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_PreStart);
        request.writeInt(0);
        request.writeInt(0xFFFFFFFF);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "av_control_pre_play_stop(),ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_fast_change_channel(long PreChannelId, long NextChannelId)//Scoty 20180816 add fast change channel
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_PreStart);
        request.writeInt((int) PreChannelId);
        request.writeInt((int) NextChannelId);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "set_fast_change_channel(),ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int av_mic_set_input_gain(int value) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_MIC_SetInputGain);

        request.writeInt(value); // value 0 ~ 7

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int av_mic_set_alc_gain(int value) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_MIC_SetAlcGain);

        request.writeInt(value); // value 0 ~ 241

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int av_mic_set_lr_input_gain(int l_r, int value) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_MIC_SetLRInputGain);

        request.writeInt(l_r); // // l_r: 0 = L, 1 = R
        request.writeInt(value); // value 0 ~ 8

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int av_get_error_frame_count(int tunerID) {
        int errorFrameCount = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetVideoErrorFrameCount);

        request.writeInt(tunerID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
//            Log.d(TAG, "av_get_error_frame_count: success");
            errorFrameCount = reply.readInt();
        }
        request.recycle();
        reply.recycle();
//        Log.d(TAG, "av_get_error_frame_count: Count = "+ errorFrameCount);
        return errorFrameCount;
    }

    public int av_get_frame_drop_count(int tunerID) {
        int frameDropCount = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetVideoErrorFrameCount);

        request.writeInt(tunerID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
//            Log.d(TAG, "av_get_frame_drop_count: success");
            reply.readInt(); // error frame count
            frameDropCount = reply.readInt();
        }
        request.recycle();
        reply.recycle();
//        Log.d(TAG, "av_get_frame_drop_count: Count = "+ frameDropCount);
        return frameDropCount;
    }
}
