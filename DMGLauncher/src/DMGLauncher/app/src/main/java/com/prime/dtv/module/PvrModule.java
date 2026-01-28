package com.prime.dtv.module;

import android.graphics.Rect;
import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.sysdata.EnServiceType;
import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.AudioInfo;
import com.prime.dtv.sysdata.EnAudioTrackMode;
import com.prime.dtv.sysdata.EnPVRTimeShiftStatus;
import com.prime.dtv.sysdata.EnTrickMode;
import com.prime.dtv.sysdata.PVREncryption;
import com.prime.dtv.sysdata.PvrFileInfo;
import com.prime.dtv.sysdata.PvrInfo;
import com.prime.dtv.sysdata.Resolution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PvrModule {
    private static final String TAG = "PvrModule";

    private static final int CMD_PVR_Base = PrimeDtvMediaPlayer.CMD_Base + 0x300;

    // PVR command
    private static final int CMD_PVR_Record_Start = CMD_PVR_Base + 0x01;
    private static final int CMD_PVR_Record_Stop = CMD_PVR_Base + 0x02;
    private static final int CMD_PVR_Record_Pause = CMD_PVR_Base + 0x03;
    private static final int CMD_PVR_Record_Resume = CMD_PVR_Base + 0x04;
    private static final int CMD_PVR_Record_GetRecTime = CMD_PVR_Base + 0x05;
    private static final int CMD_PVR_Record_GetInfo = CMD_PVR_Base + 0x06;
    private static final int CMD_PVR_Record_Start_V2 = CMD_PVR_Base + 0x07;

    private static final int CMD_PVR_Play_Start = CMD_PVR_Base + 0x10;
    private static final int CMD_PVR_Play_Stop = CMD_PVR_Base + 0x11;
    private static final int CMD_PVR_Play_Pause = CMD_PVR_Base + 0x12;
    private static final int CMD_PVR_Play_Resume = CMD_PVR_Base + 0x13;
    private static final int CMD_PVR_Play_Trick = CMD_PVR_Base + 0x14;
    private static final int CMD_PVR_Play_Seek = CMD_PVR_Base + 0x15;
    private static final int CMD_PVR_Play_GetPlayTime = CMD_PVR_Base + 0x16;
    private static final int CMD_PVR_Play_GetInfo = CMD_PVR_Base + 0x17;
    private static final int CMD_PVR_Play_GetMutiAudioInfo = CMD_PVR_Base + 0x18;
    private static final int CMD_PVR_Play_ChangeAudioTrack = CMD_PVR_Base + 0x19;
    private static final int CMD_PVR_Play_SetWindowSize = CMD_PVR_Base + 0x1A;
    private static final int CMD_PVR_Play_SetAudioTrackMode = CMD_PVR_Base + 0x1B;
    private static final int CMD_PVR_Play_GetAudioTrackMode = CMD_PVR_Base + 0x1C;

    private static final int CMD_PVR_File_Remove = CMD_PVR_Base + 0x20;
    private static final int CMD_PVR_File_Rename = CMD_PVR_Base + 0x21;
    private static final int CMD_PVR_File_GetInfo = CMD_PVR_Base + 0x22;
    private static final int CMD_PVR_File_GetExtraInfo = CMD_PVR_Base + 0x5F;

    private static final int CMD_PVR_TimeShift_Start = CMD_PVR_Base + 0x30;
    private static final int CMD_PVR_TimeShift_Stop = CMD_PVR_Base + 0x31;
    private static final int CMD_PVR_TimeShift_Play = CMD_PVR_Base + 0x32;
    private static final int CMD_PVR_TimeShift_Pause = CMD_PVR_Base + 0x33;
    private static final int CMD_PVR_TimeShift_Resume = CMD_PVR_Base + 0x34;
    private static final int CMD_PVR_TimeShift_Trick = CMD_PVR_Base + 0x35;
    private static final int CMD_PVR_TimeShift_Seek = CMD_PVR_Base + 0x36;
    private static final int CMD_PVR_TimeShift_GetPlayTime = CMD_PVR_Base + 0x37;
    private static final int CMD_PVR_TimeShift_GetStartTime = CMD_PVR_Base + 0x38;
    private static final int CMD_PVR_TimeShift_GetRecTime = CMD_PVR_Base + 0x39;
    private static final int CMD_PVR_TimeShift_GetInfo = CMD_PVR_Base + 0x3A;
    private static final int CMD_PVR_TimeShift_Start_V2 = CMD_PVR_Base + 0x3B;

    private static final int CMD_PVR_Record_GetInfo_TypeStatus = CMD_PVR_Base + 0x50;
    private static final int CMD_PVR_Record_GetInfo_TypeFileFullpath = CMD_PVR_Base + 0x51;
    private static final int CMD_PVR_Record_GetInfo_TypeProginfo = CMD_PVR_Base + 0x52;
    private static final int CMD_PVR_Play_GetInfo_TypeFileAttr = CMD_PVR_Base + 0x53;
    private static final int CMD_PVR_Play_GetInfo_TypeStatus = CMD_PVR_Base + 0x54;
    private static final int CMD_PVR_Play_GetInfo_TypeFileFullpath = CMD_PVR_Base + 0x55;
    private static final int CMD_PVR_Play_GetInfo_TypeAvplayStatus = CMD_PVR_Base + 0x56;
    private static final int CMD_PVR_TimeShift_GetInfo_TypeStatus = CMD_PVR_Base + 0x57;
    private static final int CMD_PVR_GetPesiPVRmode = CMD_PVR_Base + 0x58;
    private static final int CMD_PVR_GetAspectRatio = CMD_PVR_Base + 0x59;
    private static final int CMD_PVR_Record_Check = CMD_PVR_Base + 0x5A;
    private static final int CMD_PVR_Record_GetAllInfo = CMD_PVR_Base + 0x5B;
    private static final int CMD_PVR_Record_GetMaxRecNum = CMD_PVR_Base + 0x5C;
    private static final int CMD_PVR_SetStartPositionFlag = CMD_PVR_Base + 0x5D;
    private static final int CMD_PVR_PlayFile_CheckLastViewPoint = CMD_PVR_Base + 0x5E;
    private static final int CMD_PVR_Record_AllTs_Start = CMD_PVR_Base + 0x60;
    private static final int CMD_PVR_Record_AllTs_Stop = CMD_PVR_Base + 0x61;
    private static final int CMD_PVR_SetParentLockOK = CMD_PVR_Base + 0x62;
    private static final int CMD_PVR_Total_Record_File_Open = CMD_PVR_Base + 0x63;
    private static final int CMD_PVR_Total_Record_File_Close = CMD_PVR_Base + 0x64;
    private static final int CMD_PVR_Total_Record_File_Sort = CMD_PVR_Base + 0x65;
    private static final int CMD_PVR_Total_Record_File_Get = CMD_PVR_Base + 0x66;
    private static final int CMD_PVR_Open_Hard_Disk = CMD_PVR_Base + 0x67;//Scoty 20180827 add HDD Ready command and callback
    private static final int CMD_PVR_TimeShift_PlayStop = CMD_PVR_Base + 0x68;//Scoty 20180827 add and modify TimeShift Live Mode
    private static final int CMD_PVR_TimeShift_GetLivePauseTime = CMD_PVR_Base + 0x69;//Scoty 20180827 add and modify TimeShift Live Mode
    private static final int CMD_PVR_File_GetEPGInfo = CMD_PVR_Base + 0x6A;
    private static final int CMD_PVR_Get_Total_Rec_Num = CMD_PVR_Base + 0x6B;
    private static final int CMD_PVR_Get_Records_File = CMD_PVR_Base + 0x6C;
    private static final int CMD_PVR_Get_Total_One_Series_Rec_Num = CMD_PVR_Base + 0x6D;
    private static final int CMD_PVR_Get_One_Series_Records_File = CMD_PVR_Base + 0x6E;
    private static final int CMD_PVR_Delete_Total_Records_File = CMD_PVR_Base + 0x6F;
    private static final int CMD_PVR_Delete_One_Series_Folder = CMD_PVR_Base + 0x70;
    private static final int CMD_PVR_Delete_Record_File_By_Ch_Id = CMD_PVR_Base + 0x71;
    private static final int CMD_PVR_Delete_Record_File = CMD_PVR_Base + 0x72;

    private static final int PESI_SVR_PVR_MAX_CIPHER_KEY_LEN = 128;


    public int pvr_record_start(int pvrPlayerID, long channelID, String recordPath, int duration) {
        Log.d(TAG, "pvr_record_start(" + channelID + "," + recordPath + "," + duration + "," + ")");
        int recid = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start);
        //request.writeInt(pvrPlayerID);
        request.writeInt((int) channelID);
        request.writeInt(duration);
        request.writeInt(0);// file length
        request.writeString(recordPath);
        request.writeInt(0);// no cipher
        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            recid = reply.readInt();
            Log.d(TAG, "pvr_record_start:  recid = " + recid);
        }
        request.recycle();
        reply.recycle();
        return recid;
    }

    public int pvr_record_start(int pvrPlayerID, long channelID, String recordPath, int duration, PVREncryption pvrEncryption) {
        Log.d(TAG, "pvr_record_start(" + channelID + "," + recordPath + "," + duration + "," + ")");
        int recid = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int length = pvrEncryption.getPvrKey().length();
        String strPvrKey = pvrEncryption.getPvrKey();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start);
        //request.writeInt(pvrPlayerID);
        request.writeInt((int) channelID);
        request.writeInt(duration);
        request.writeInt(256);// file length
        request.writeString(recordPath);
        request.writeInt(1);//  cipher
        //request.writeInt(1);//  andorid
        request.writeInt(pvrEncryption.getPvrEncryptionType());
        if (length > PESI_SVR_PVR_MAX_CIPHER_KEY_LEN) {
            length = PESI_SVR_PVR_MAX_CIPHER_KEY_LEN;
        }
        request.writeInt(length);

        for (int i = 0; i < length; i++) {
            char cKey = strPvrKey.charAt(i);
            request.writeInt((int) cKey);
        }
        //request.writeString(pvrEncryption.getPvrKey());

        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            recid = reply.readInt();
            Log.d(TAG, "pvrRecordStart:  recid = " + recid);
        }
        request.recycle();
        reply.recycle();
        return recid;
    }

    public int pvr_record_stop(int pvrPlayerID, int recId) {
        Log.d(TAG, "pvr_record_stop() recId=" + recId);
        //return excuteCommand(CMD_PVR_Record_Stop,pvrPlayerID);
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_Record_Stop, recId);
    }

    public int pvr_record_get_already_rec_time(int pvrPlayerID, int recId) {
        Log.d(TAG, "pvr_record_get_already_rec_time() recId=" + recId);
        //return excuteCommand(CMD_PVR_Record_GetRecTime,pvrPlayerID);
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_PVR_Record_GetRecTime, recId) / 1000;
    }

    public int record_start_v2_with_duration(long channelId, int durationSec, boolean doCipher, PVREncryption pvrEncryption) {
        Log.d(TAG, "record_start_v2_with_duration(" + channelId + "," + durationSec + "," + doCipher + ")");
        int recID = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start_V2);
        request.writeInt((int) channelId);
        request.writeInt(durationSec); // duration (second)
        request.writeInt(0); // file size (MByte)

        if (doCipher && pvrEncryption != null) {
            int keyLength = pvrEncryption.getPvrKey().length();
            int keyType = pvrEncryption.getPvrEncryptionType();
            String key = pvrEncryption.getPvrKey();

            if (keyLength > PESI_SVR_PVR_MAX_CIPHER_KEY_LEN)
                keyLength = PESI_SVR_PVR_MAX_CIPHER_KEY_LEN;

            request.writeInt(1); // do cipher
            request.writeInt(keyType);
            request.writeInt(keyLength);

            for (int i = 0; i < keyLength; i++) {
                char cKey = key.charAt(i);
                request.writeInt((int) cKey);
            }
        } else {
            request.writeInt(0); // no cipher
        }

        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            recID = reply.readInt();
        }
        Log.d(TAG, "record_start_v2_with_duration: ret = " + ret + " , recID = " + recID);

        request.recycle();
        reply.recycle();
        return recID;
    }

    public int record_start_v2_with_file_size(long channelId, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption) {
        Log.d(TAG, "record_start_v2_with_file_size(" + channelId + "," + fileSizeMB + "," + doCipher + ")");
        int recID = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start_V2);
        request.writeInt((int) channelId);
        request.writeInt(0); // duration (second)
        request.writeInt(fileSizeMB); // file size (MByte)

        if (doCipher && pvrEncryption != null) {
            int keyLength = pvrEncryption.getPvrKey().length();
            int keyType = pvrEncryption.getPvrEncryptionType();
            String key = pvrEncryption.getPvrKey();

            if (keyLength > PESI_SVR_PVR_MAX_CIPHER_KEY_LEN)
                keyLength = PESI_SVR_PVR_MAX_CIPHER_KEY_LEN;

            request.writeInt(1); // do cipher
            request.writeInt(keyType);
            request.writeInt(keyLength);

            for (int i = 0; i < keyLength; i++) {
                char cKey = key.charAt(i);
                request.writeInt((int) cKey);
            }
        } else {
            request.writeInt(0); // no cipher
        }

        PrimeDtvMediaPlayer.invokeex(request, reply);

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            recID = reply.readInt();
        }
        Log.d(TAG, "record_start_v2_with_file_size: retID = " + recID);

        request.recycle();
        reply.recycle();
        return recID;
    }

    public int pvr_play_start(String filePath) {
        Log.d(TAG, "pvr_play_start");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_Start);
        //Log.i(TAG,"the filepath is:"+filePath);
        request.writeString(filePath);
        request.writeInt(0);//not use cipher
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_play_start(String filePath, PVREncryption pvrEncryption) {
        Log.d(TAG, "pvrPlayStart");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int length = pvrEncryption.getPvrKey().length();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_Start);
        Log.i(TAG, "the filepath is:" + filePath);
        request.writeString(filePath);
        request.writeInt(pvrEncryption.getPvrEncryptionType());
        request.writeString(pvrEncryption.getPvrKey());
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_play_stop() {
        Log.d(TAG, "pvr_play_stop");
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_Play_Stop);
    }

    public int pvr_play_pause() {
        Log.d(TAG, "pvr_play_pause");
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_Play_Pause);
    }

    public int pvr_play_resume() {
        Log.d(TAG, "pvr_play_resume");
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_Play_Resume);
    }

    public int pvr_play_trick_play(EnTrickMode enSpeed) {
        Log.d(TAG, "pvr_play_trick_play");
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_Play_Trick, enSpeed.getValue());
    }

    public int pvr_play_seek_to(int sec) {
        long msec = sec * 1000L;

        Log.d(TAG, "pvr_play_seek_to");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_Seek);

        request.writeInt(0);
        request.writeLong(msec);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_play_get_play_time() {
        Log.d(TAG, "pvr_play_get_play_time");
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_PVR_Play_GetPlayTime) / 1000;
    }

    public int pvr_play_get_play_time_ms()//eric lin 20181026 get play time(ms) for live channel
    {
        Log.d(TAG, "pvr_play_get_play_time_ms");
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_PVR_Play_GetPlayTime);
    }

    public AudioInfo.AudioComponent pvr_play_get_current_audio() {
        Log.d(TAG, "pvr_play_get_current_audio");

        AudioInfo.AudioComponent localAudioInfo = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetMutiAudioInfo);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int audioCount = reply.readInt();
            int curIndex = reply.readInt();
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

                if (curIndex == i) {
                    localAudioInfo = new AudioInfo.AudioComponent();
                    localAudioInfo.setLangCode(languageCode);
                    localAudioInfo.setPid(pid);
                    localAudioInfo.setAudioType(/*EnStreamType.valueOf(audioType)*/audioType);
                    localAudioInfo.setAdType(/*EnAudioType.valueOf(adType)*/adType);
                    localAudioInfo.setTrackMode(/*ConverToJavaTrack(trackMode)*/EnAudioTrackMode.valueOf(trackMode).getValue());
                    Log.d(TAG, "pvr_play_get_current_audio:languageCode = " + languageCode + ",pid = " + pid + ",audioType = " + audioType);
                }
            }
        }

        request.recycle();
        reply.recycle();
        return localAudioInfo;
    }

    public AudioInfo pvr_play_get_audio_components() {
        AudioInfo audioList = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetMutiAudioInfo);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int cnt = reply.readInt();
            int curIndex = reply.readInt();
            if (cnt > 0) {
                audioList = new AudioInfo();
                audioList.setCurPos(curIndex);//eric lin 20180720 add pvr play audio's CurPos and component pos
                for (int i = 0; i < cnt; i++) {
                    AudioInfo.AudioComponent localAudioInfo = new AudioInfo.AudioComponent();

                    int pid = reply.readInt();
                    int audioType = reply.readInt();
                    int adType = reply.readInt(); //u32Reserved
                    int compTag = reply.readInt();
                    int compType = reply.readInt();
                    int streamContent = reply.readInt();
                    int trackMode = reply.readInt(); // u8Reserved2
                    int reserved3 = reply.readInt(); // u8Reserved3
                    String languageCode = reply.readString();

                    localAudioInfo.setLangCode(languageCode);
                    localAudioInfo.setPid(pid);
                    localAudioInfo.setAudioType(/*EnStreamType.valueOf(audioType)*/audioType);
                    localAudioInfo.setAdType(/*EnAudioType.valueOf(adType)*/adType);
                    localAudioInfo.setTrackMode(/*ConverToJavaTrack(trackMode)*/EnAudioTrackMode.valueOf(trackMode).getValue());
                    localAudioInfo.setPos(i);//eric lin 20180720 add pvr play audio's CurPos and component pos
                    audioList.ComponentList.add(localAudioInfo);
                    Log.d(TAG, "getAudioComponents:languageCode = " + languageCode + "pid = " + pid
                            + "audioType = " + audioType);
                }
            }
        }

        request.recycle();
        reply.recycle();
        return audioList;
    }

    public int pvr_play_select_audio(AudioInfo.AudioComponent audio) {
        if (null == audio) {
            Log.e(TAG, "the param of audio is null");
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        int audioPid = audio.getPid();
        int index = 0;
        boolean bGet = false;

        Log.d(TAG, "pvr_play_select_audio(languageCode = " + audio.getLangCode() + ",pid =" + audio.getPid()
                + ",type =" + audio.getAudioType() + ")");

        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_Play_GetMutiAudioInfo);

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

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_ChangeAudioTrack);
        request.writeInt(index);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        Log.d(TAG, "ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_play_set_window_rect(Rect rect) {
        if (null == rect) {
            Log.e(TAG, "the param of rect is null");
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }
        Log.d(TAG, "setWindowRect(" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_SetWindowSize);
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

    public int pvr_play_set_track_mode(EnAudioTrackMode enTrackMode) {
        Log.d(TAG, "pvrplay setTrackMode(" + enTrackMode + ")");
        int svrTrackMode = 0;
        switch (enTrackMode) {
            case /*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO:
                svrTrackMode = 0;
                break;
            /*case AUDIO_TRACK_DOUBLE_MONO:
                svrTrackMode = 1;
                break;*/
            case /*AUDIO_TRACK_DOUBLE_LEFT*/MPEG_AUDIO_TRACK_LEFT:
                svrTrackMode = 4;
                break;
            case /*AUDIO_TRACK_DOUBLE_RIGHT*/MPEG_AUDIO_TRACK_RIGHT:
                svrTrackMode = 5;
                break;
            /*case AUDIO_TRACK_EXCHANGE:
                break;
            case AUDIO_TRACK_ONLY_RIGHT:
                svrTrackMode = 5;
                break;
            case AUDIO_TRACK_ONLY_LEFT:
                svrTrackMode = 4;
                break;*/
            /*case AUDIO_TRACK_MUTED:
                break;*/
            default:
                break;
        }

        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_Play_SetAudioTrackMode, svrTrackMode);
    }

    public EnAudioTrackMode pvr_play_get_track_mode() {
        Log.d(TAG, "pvr_play_get_track_mode()");
        EnAudioTrackMode enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_BUTT*/MPEG_AUDIO_TRACK_BUTT;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetAudioTrackMode);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int index = reply.readInt();
            switch (index) {
                case 0:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO;
                    break;
               /* case 1:
                    enTrackMode = EnAudioTrackMode.AUDIO_TRACK_DOUBLE_MONO;
                    break;*/
                case 2:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO;
                    break;
                case 3:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO;
                    break;
                case 4:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_DOUBLE_LEFT*/MPEG_AUDIO_TRACK_LEFT;
                    break;
                case 5:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_DOUBLE_RIGHT*/MPEG_AUDIO_TRACK_RIGHT;
                    break;
                default:
                    break;
            }
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "getTrackMode(),enTrackMode = " + enTrackMode);

        return enTrackMode;
    }

    public int pvr_file_remove(String filePath) {
        Log.d(TAG, "pvr_file_remove() filePath = " + filePath);
        if (null == filePath) {
            Log.e(TAG, "the filePath is null");
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_Remove);
        request.writeString(filePath);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_file_rename(String oldName, String newName) {
        Log.d(TAG, "pvr_file_rename()");
        if (null == oldName || null == newName) {
            Log.e(TAG, "oldName or newName is null");
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_Rename);
        request.writeString(oldName);
        request.writeString(newName);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public boolean pvr_play_is_radio(String fullName) {
        Log.d(TAG, "pvr_play_is_radio fullName=" + fullName);
        if (null == fullName) {
            Log.e(TAG, "fullName is null");
            return false;
        }
        int serviceType = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo); //CMD_PVR_Play_GetInfo_TypeFileAttr
        request.writeString(fullName);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        boolean bRadio = false;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt();// start time ms
            reply.readInt();// end time ms
            reply.readInt();// duration in ms
            reply.readLong();// fileSize in byte
            serviceType = reply.readInt();
        }

        EnServiceType tmpServiceType = EnServiceType.valueOf(serviceType);
        if ((tmpServiceType == EnServiceType.RADIO)
                || (tmpServiceType == EnServiceType.FM_RADIO)) {
            bRadio = true;
        }
        request.recycle();
        reply.recycle();
        return bRadio;
    }

    public int pvr_file_get_duration(String fullName) {
        Log.d(TAG, "pvr_file_get_duration()");
        if (null == fullName) {
            Log.e(TAG, "fullName is null");
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo);
        request.writeString(fullName);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int durationSec = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt();  //startTime in ms
            reply.readInt();  //endTime in ms
            durationSec = reply.readInt() / 1000;
        }

        request.recycle();
        reply.recycle();

        return durationSec;
    }

    public long pvr_file_get_size(String fullName) {
        Log.d(TAG, "pvr_file_get_size()");
        if (null == fullName) {
            Log.e(TAG, "fullName is null");
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo);
        request.writeString(fullName);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        long fileSize = PrimeDtvMediaPlayer.CMD_RETURN_LONG_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt();  //startTime in ms
            reply.readInt();  //endTime in ms
            reply.readInt();  //duration in ms
            fileSize = reply.readLong();
        }

        request.recycle();
        reply.recycle();

        return fileSize;
    }

    public PvrFileInfo pvr_file_get_all_info(String fullName) {
        Log.d(TAG, "pvr_file_get_all_info()");
        if (null == fullName) {
            Log.e(TAG, "fullName is null");
            //return CMD_RETURN_VALUE_FAILAR;
            return null;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo);
        request.writeString(fullName);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        PvrFileInfo pvrFileInfo = new PvrFileInfo();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            pvrFileInfo.startTimeInMs = reply.readInt();    // startTime in ms
            pvrFileInfo.endTimeInMs = reply.readInt();      // endTime in ms
            pvrFileInfo.durationInMs = reply.readInt();     // duration in ms
            pvrFileInfo.fileSize = reply.readLong();        // file size
            pvrFileInfo.serviceType = reply.readInt();      // service type
            pvrFileInfo.channelLock = reply.readInt();      // channel lock
            pvrFileInfo.parentalRate = reply.readInt();      // parental lock
        } else {
            pvrFileInfo.startTimeInMs = 0;    // startTime in ms
            pvrFileInfo.endTimeInMs = 0;      // endTime in ms
            pvrFileInfo.durationInMs = 0;     // duration in ms
            pvrFileInfo.fileSize = 0;        // file size
            pvrFileInfo.serviceType = 0;      // service type
            pvrFileInfo.channelLock = 0;      // channel lock
            pvrFileInfo.parentalRate = 0;      // parental lock
        }

        request.recycle();
        reply.recycle();

        return pvrFileInfo;
    }

    public PvrFileInfo pvr_file_get_extra_info(String fullName) {
        Log.d(TAG, "pvr_file_get_extra_info()");
        if (null == fullName) {
            Log.e(TAG, "fullName is null");
            //return CMD_RETURN_VALUE_FAIL;
            return null;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetExtraInfo);
        request.writeString(fullName);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        PvrFileInfo pvrFileInfo = new PvrFileInfo();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            pvrFileInfo.channelName = reply.readString();
            pvrFileInfo.year = reply.readInt();
            pvrFileInfo.month = reply.readInt();
            pvrFileInfo.date = reply.readInt();
            pvrFileInfo.week = reply.readInt();
            pvrFileInfo.hour = reply.readInt();
            pvrFileInfo.minute = reply.readInt();
            pvrFileInfo.second = reply.readInt();
        } else {
            pvrFileInfo.channelName = "";
            pvrFileInfo.year = 0;
            pvrFileInfo.month = 0;
            pvrFileInfo.date = 0;
            pvrFileInfo.week = 0;
            pvrFileInfo.hour = 0;
            pvrFileInfo.minute = 0;
            pvrFileInfo.second = 0;
        }

        request.recycle();
        reply.recycle();

        return pvrFileInfo;
    }

    public int pvr_timeshift_start(int playerID, int time, int filesize, String filePath) {
        Log.d(TAG, "pvr_timeshift_start time = " + time + "filesize = " + filesize + "filePath = " + filePath);
        //return excuteCommand(CMD_PVR_TimeShift_Start, time, filesize, 0);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_Start);
        request.writeInt(time);
        request.writeInt(filesize);
        request.writeInt(0);
        request.writeString(filePath);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_timeshift_start_v2(int durationSec, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption) {
        Log.d(TAG, "pvr_timeshift_start_v2(" + durationSec + "," + fileSizeMB + "," + doCipher + ")");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_Start_V2);
        request.writeInt(durationSec); // duration (second)
        request.writeInt(fileSizeMB); // file size (MByte)

        if (doCipher && pvrEncryption != null) {
            int keyLength = pvrEncryption.getPvrKey().length();
            int keyType = pvrEncryption.getPvrEncryptionType();
            String key = pvrEncryption.getPvrKey();

            if (keyLength > PESI_SVR_PVR_MAX_CIPHER_KEY_LEN)
                keyLength = PESI_SVR_PVR_MAX_CIPHER_KEY_LEN;

            request.writeInt(1); // do cipher
            request.writeInt(keyType);
            request.writeInt(keyLength);

            for (int i = 0; i < keyLength; i++) {
                char cKey = key.charAt(i);
                request.writeInt((int) cKey);
            }
        } else {
            request.writeInt(0); // no cipher
        }

        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        Log.d(TAG, "time_shift_start_v2: ret = " + ret);

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }


    public int pvr_timeshift_stop(int playerID) {
        Log.d(TAG, "pvr_timeshift_stop");
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Stop, 1);
    }

    public int pvr_time_shift_play_for_live_channel(int pvrMode) //Edwin 20181022 TimeShift for Live Channel
    {
        Log.d(TAG, "pvrTimeShiftPlay");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetLivePauseTime);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            playTime = reply.readInt();
        }
        Log.d(TAG, "pvrTimeShiftPlay: playTime = " + playTime + " pvrMode = " + pvrMode);

        if (pvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE) {
            ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Play, 0);
        } else {
            ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Resume, 0);
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_timeshift_play(int playerID) {
        Log.d(TAG, "pvrTimeShiftPlay");
        //Scoty 20180827 add and modify TimeShift Live Mode -s
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetLivePauseTime);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            playTime = reply.readInt();
        }
        Log.d(TAG, "pvrTimeShiftPlay: playTime = " + playTime);
        if (playTime == 0) {
            ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Play, 0);
        } else {
            ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Resume, 0);
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
        //Scoty 20180827 add and modify TimeShift Live Mode -e
    }

    public int pvr_timeshift_resume(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);

        PrimeDtvMediaPlayer.invokeex(request1, reply1);
        int ret = reply1.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int status = reply1.readInt();
            Log.d(TAG, "pvrTimeShiftResume GetPlayStatus(), status = " + status);
            if (status != EnPVRTimeShiftStatus.PLAY &&
                    status != EnPVRTimeShiftStatus.STOP)
                ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Resume, 0);

        }
        request1.recycle();
        reply1.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_timeshift_pause(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);

        PrimeDtvMediaPlayer.invokeex(request1, reply1);
        int ret = reply1.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int status = reply1.readInt();
            Log.d(TAG, "pvr_timeshift_pause GetPlayStatus(), status = " + status);
            if (status != EnPVRTimeShiftStatus.PAUSE &&
                    status != EnPVRTimeShiftStatus.STOP)
                ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Pause, 0);
        }
        request1.recycle();
        reply1.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_timeshift_live_pause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvr_timeshift_live_pause");
        int ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Pause, 0);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_timeshift_file_pause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvr_timeshift_file_pause");

        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);

        PrimeDtvMediaPlayer.invokeex(request1, reply1);
        int ret = reply1.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int status = reply1.readInt();
            Log.d(TAG, "GetPlayStatus(), status = " + status);

            //Scoty 20180622 modify play/pause status -s
            if (status == EnPVRTimeShiftStatus.PLAY) {
                ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Pause, 0);
            } else if (status == EnPVRTimeShiftStatus.PAUSE ||
                    status == EnPVRTimeShiftStatus.FAST_FORWARD ||
                    status == EnPVRTimeShiftStatus.FAST_BACKWARD) {
                ret = PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Resume, 0);
            } else if (status == EnPVRTimeShiftStatus.STOP) {
                //ret = excuteCommand(CMD_PVR_TimeShift_Start, 900, 0, 0);
                ret = pvr_timeshift_start_v2(900, 0, false, null);
            } else {
                ret = 0;
            }
            //Scoty 20180622 modify play/pause status -e
        }
        request1.recycle();
        reply1.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_timeshift_trick_play(int playerID, EnTrickMode mode) {
        Log.d(TAG, "pvr_timeshift_trick_play(" + mode + ") value = " + mode.getValue());
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Trick, mode.getValue());
    }

    public int pvr_timeshift_seek_play(int playerID, int seekSec) {
        Log.d(TAG, "seekPlay(" + seekSec + ")");
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_TimeShift_Seek, seekSec);
    }

    public Date pvr_timeshift_get_played_time(int playerID) {
        Log.d(TAG, "pvr_timeshift_get_played_time()");
        Date retDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetPlayTime);
        request.writeInt(playerID);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int playTime = reply.readInt();

            Calendar ca = Calendar.getInstance();
            PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();

            int dst = 0, zone = 0, dtvZoneSecond = 0, dtvDaylightSecond = 0;
            if (PrimeDtvMediaPlayer.ADD_SYSTEM_OFFSET) { // connie 20181106 for not add system offset
                dst = ca.get(Calendar.DST_OFFSET);
                zone = ca.get(Calendar.ZONE_OFFSET);
                dtvZoneSecond = dtv.get_dtv_timezone();
                dtvDaylightSecond = dtv.get_dtv_daylight();
            }

            long timeMs = playTime;

            if (PrimeDtvMediaPlayer.ADD_SYSTEM_OFFSET && 0 != dtvZoneSecond) {
                timeMs -= (zone + dst) / 1000;
                timeMs += (dtvZoneSecond + dtvDaylightSecond);
            }

            timeMs *= 1000;

            retDate = new Date(timeMs);
        }

        request.recycle();
        reply.recycle();

        return retDate;
    }

    public int pvr_timeshift_get_play_second(int playerID) {
        Log.d(TAG, "pvr_timeshift_get_play_second()");


        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetPlayTime);
        request.writeInt(playerID);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            playTime = reply.readInt();
            playTime /= 1000;
        }

        request.recycle();
        reply.recycle();

        return playTime;
    }

    public Date pvr_timeshift_get_begin_time(int playerID) {
        Log.d(TAG, "pvr_timeshift_get_begin_time()");
        // TODO: need fix this
        Date retDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetStartTime);
        request.writeInt(playerID);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int beginTime = reply.readInt();

            Calendar ca = Calendar.getInstance();
            PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);
            int dtvZoneSecond = dtv.get_dtv_timezone();
            int dtvDaylightSecond = dtv.get_dtv_daylight();

            long timeMs = beginTime;

            if (0 != dtvZoneSecond) {
                timeMs -= (zone + dst) / 1000;
                timeMs += (dtvZoneSecond + dtvDaylightSecond);
            }

            timeMs *= 1000;
            retDate = new Date(timeMs);
        }

        request.recycle();
        reply.recycle();

        return retDate;
    }

    public int pvr_timeshift_get_begin_second(int playerID) {
        Log.d(TAG, "pvr_timeshift_get_begin_second()");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetStartTime);
        request.writeInt(playerID);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int beginTime = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            beginTime = reply.readInt();
            beginTime /= 1000;
        }

        request.recycle();
        reply.recycle();
        return beginTime;
    }

    public int pvr_timeshift_get_record_time(int playerID) {
        Log.d(TAG, "pvr_timeshift_get_record_time()");
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_PVR_TimeShift_GetRecTime) / 1000;
    }

    public int pvr_timeshift_get_status(int playerID) {
        Log.d(TAG, "pvr_timeshift_get_status()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int pvrTimeShiftStatus = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            pvrTimeShiftStatus = reply.readInt();
            Log.d(TAG, "pvr_timeshift_get_status(), status = " + pvrTimeShiftStatus);
        }

        request.recycle();
        reply.recycle();

        return pvrTimeShiftStatus;
    }

    public EnTrickMode pvr_timeshift_get_current_trick_mode(int playerID) {
        Log.d(TAG, "pvr_timeshift_get_current_trick_mode");
        EnTrickMode enTrickMode = EnTrickMode.INVALID_TRICK_MODE;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt();
            int index = reply.readInt();
            enTrickMode = EnTrickMode.valueOf(index);
        }
        request.recycle();
        reply.recycle();
        return enTrickMode;
    }

    public long pvr_play_get_size() {
        Log.d(TAG, "pvr_play_get_size");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetInfo_TypeFileAttr);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        long fileSize = PrimeDtvMediaPlayer.CMD_RETURN_LONG_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt();// start time ms
            reply.readInt();// end time ms
            reply.readInt();// duration in ms
            fileSize = reply.readLong();// fileSize in byte
        }
        request.recycle();
        reply.recycle();
        return fileSize;
    }

    public int pvr_play_get_duration() {
        Log.d(TAG, "pvr_play_get_duration");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetInfo_TypeFileAttr);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int duration = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt();// start time ms
            reply.readInt();// end time ms
            duration = reply.readInt() / 1000;// duration in ms / 1000
        }
        request.recycle();
        reply.recycle();
        return duration;
    }

    public int pvr_play_get_current_status() {
        Log.d(TAG, "pvr_play_get_current_status");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetInfo_TypeStatus);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int pvrStatus = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            pvrStatus = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return pvrStatus;
    }

    public EnTrickMode pvr_play_get_current_trick_mode() {
        Log.d(TAG, "pvr_play_get_current_trick_mode");
        EnTrickMode enTrickMode = EnTrickMode.INVALID_TRICK_MODE;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetInfo_TypeStatus);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            reply.readInt();
            int index = reply.readInt();
            enTrickMode = EnTrickMode.valueOf(index);
        }
        request.recycle();
        reply.recycle();
        return enTrickMode;
    }

    public String pvr_play_get_file_full_path(int pvrPlayerID) {
        Log.d(TAG, "pvr_play_get_file_full_path()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetInfo_TypeFileFullpath);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String fullPath = null;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            fullPath = reply.readString();
        }

        request.recycle();
        reply.recycle();
        return fullPath;
    }

    public Resolution pvr_play_get_video_resolution() {
        Log.d(TAG, "pvr_play_get_video_resolution");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetInfo_TypeAvplayStatus);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Resolution resolution = null;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            resolution = new Resolution();

            reply.readInt();  //fps
            resolution.width = reply.readInt();
            resolution.height = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return resolution;
    }

    public int pvr_record_get_status(int pvrPlayerID, int recId) {
        Log.d(TAG, "pvr_record_get_status() recId=" + recId);
        //return excuteCommand(CMD_PVR_Record_GetInfo, pvrPlayerID);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_Record_GetInfo);
//        request.writeInt(0);
        request.writeInt(CMD_PVR_Record_GetInfo_TypeStatus);
        request.writeInt(recId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        int status = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            status = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return status;
    }

    public String pvr_record_get_file_full_path(int pvrPlayerID, int recId) {
        Log.d(TAG, "pvr_record_get_file_full_path() recId=" + recId);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_GetInfo_TypeFileFullpath);
        request.writeInt(recId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        String fullPath = null;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            fullPath = reply.readString();
        }

        request.recycle();
        reply.recycle();
        return fullPath;
    }

    public int pvr_record_get_program_id(int pvrPlayerID, int recId) {
        Log.d(TAG, "pvr_record_get_program_id() recId=" + recId);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_GetInfo_TypeProginfo);
        request.writeInt(recId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        int progId = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            progId = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return progId;
    }

    public int pvr_get_current_pvr_mode(long channelId) {
        Log.d(TAG, "pvr_get_current_pvr_mode()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_GetPesiPVRmode);
        request.writeInt((int) channelId);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int pvrMode = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            pvrMode = reply.readInt();
        }

        request.recycle();
        reply.recycle();

        return pvrMode;
    }

    public int pvr_get_ratio() {
        Log.d(TAG, "pvr_get_ratio()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_GetAspectRatio);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int ratio = 0;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            ratio = reply.readInt();
        }
        Log.d(TAG, "pvr_get_ratio: ratio = " + ratio);
        request.recycle();
        reply.recycle();

        return ratio;
    }

    public int pvr_record_check(long channelID) {
        Log.d(TAG, "pvr_record_check: channelID=" + channelID);
        //return recId
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_PVR_Record_Check, channelID);
    }

    public List<PvrInfo> pvr_record_get_all_info() {
        Log.d(TAG, "pvr_record_get_all_info: ");

        List<PvrInfo> pvrList = new ArrayList<PvrInfo>();

        int i = 0;
        int ret = 0;
        int retNum = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_GetAllInfo);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            retNum = reply.readInt();
            Log.d(TAG, "pvr Num =" + retNum);
            int rec_id = 0;
            int channel_id = 0;
            int pvr_mode = 0;
            for (i = 0; i < retNum; i++) {
                rec_id = reply.readInt();
                channel_id = reply.readInt();
                pvr_mode = reply.readInt();
                PvrInfo pvr = new PvrInfo(rec_id, channel_id, pvr_mode);
                Log.d(TAG, "pvr Num =" + retNum);
                pvrList.add(pvr);
            }
            Log.d(TAG, "getProgramList end");
        }
        request.recycle();
        reply.recycle();

        return pvrList;
    }

    public int pvr_record_get_max_rec_num() {
        Log.d(TAG, "pvrRecordGetMaxRecNum: ");
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_PVR_Record_GetMaxRecNum);
    }

    public int pvr_set_start_position_flag(int startPositionFlag) {
        return PrimeDtvMediaPlayer.excute_command(CMD_PVR_SetStartPositionFlag, startPositionFlag);
    }

    public int pvr_play_file_check_last_view_point(String fullName) {
        Log.d(TAG, "pvr_play_file_check_last_view_point fullName=" + fullName);
        if (null == fullName) {
            Log.e(TAG, "fullName is null");
            return 0;
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_PlayFile_CheckLastViewPoint);
        request.writeString(fullName);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int CheckOk = 0;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            CheckOk = reply.readInt();//CheckOk
        }
        request.recycle();
        reply.recycle();
        return CheckOk;
    }

    public int record_ts_start(int TunerId, String FullName) // connie 20180803 add record ts -s
    {
        if (FullName == null) {
            Log.d(TAG, "record_ts_start: FullName is null !!!!!");
            return -1;
        }

        Log.d(TAG, "record_ts_start:  TunerId =" + TunerId + "     FullName =" + FullName);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_AllTs_Start);
        request.writeInt(TunerId);
        request.writeString(FullName);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "record_ts_start:  recordTS_start Sucess !!!!!!");
        }
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int record_ts_stop() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_AllTs_Stop);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "record_ts_stop:  record_ts_stop Success !!!!!!");
        }
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }// connie 20180803 add record ts -e

    public int pvr_set_parent_lock_ok()  //connie 20180806 for pvr parentalRate
    {
        Log.d(TAG, "pvr_set_parent_lock_ok()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_SetParentLockOK);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    // edwin 20180809 add PvrTotalRecordFileXXX -s
    public int pvr_total_record_file_open(String dirPath) {
        Log.d(TAG, "pvr_total_record_file_open: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Open);
        request.writeString(dirPath);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int totalFileNumber = 0; // edwin 20180816 return normal value
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            totalFileNumber = reply.readInt();
        }
        request.recycle();
        reply.recycle();

        return totalFileNumber;
    }

    public int pvr_total_record_file_close() {
        Log.d(TAG, "pvr_total_record_file_close: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Close);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_total_record_file_sort(int sortType) {
        Log.d(TAG, "pvr_total_record_file_sort: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Sort);
        request.writeInt(sortType);
        // PVR_SORT_BY_CHNAME	=0,
        // PVR_SORT_BY_DATETIME	=1,
        // PVR_SORT_BUTT
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public List<PvrFileInfo> pvr_total_record_file_get(int startIndex, int total) {
        Log.d(TAG, "pvr_total_record_file_get: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Get);
        request.writeInt(startIndex);
        request.writeInt(total);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        List<PvrFileInfo> list = new ArrayList<>();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            for (int i = 0; i < total; i++) {
                PvrFileInfo fileInfo = new PvrFileInfo();
                fileInfo.channelName = reply.readString();
                fileInfo.realFileName = reply.readString();
                list.add(fileInfo);
            }
        }
        request.recycle();
        reply.recycle();

        return list;
    }

    public int pvr_check_hard_disk_open(String FilePath)//Scoty 20180827 add HDD Ready command and callback
    {
        Log.d(TAG, "pvr_check_hard_disk_open()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Open_Hard_Disk);
        request.writeString(FilePath);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_play_timeshift_stop()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvrCheckHardDiskOpen()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_PlayStop);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_record_get_live_pause_time()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvr_record_get_live_pause_time()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetLivePauseTime);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            playTime = reply.readInt();
            playTime /= 1000;
        }
        request.recycle();
        reply.recycle();

        return playTime;
    }

    public PvrFileInfo pvr_file_get_epg_info(String fullName, int epgIndex) {
        Log.d(TAG, "pvr_file_get_epg_info: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetEPGInfo);
        request.writeString(fullName);
        request.writeInt(epgIndex);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        PvrFileInfo fileInfo = new PvrFileInfo();
        int ret = reply.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            fileInfo.eventName = reply.readString();
            fileInfo.shortEvent = reply.readString();
            fileInfo.extendedText = reply.readString();
            fileInfo.languageCode = reply.readString();
            fileInfo.extendedLanguageCode = reply.readString();

            fileInfo.year = reply.readInt();
            fileInfo.month = reply.readInt();
            fileInfo.date = reply.readInt();
            fileInfo.hour = reply.readInt();
            fileInfo.minute = reply.readInt();
            fileInfo.second = reply.readInt();
            fileInfo.week = reply.readInt();
            fileInfo.yearEnd = reply.readInt();
            fileInfo.monthEnd = reply.readInt();
            fileInfo.dateEnd = reply.readInt();
            fileInfo.hourEnd = reply.readInt();
            fileInfo.monthEnd = reply.readInt();
            fileInfo.secondEnd = reply.readInt();
            fileInfo.weekEnd = reply.readInt();

            fileInfo.parentalRate = reply.readInt();
            fileInfo.eventNameCharCode = reply.readInt();
            fileInfo.shortEventCharCode = reply.readInt();
            fileInfo.recordTimeStamp = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return fileInfo;
    }

    public int pvr_get_total_rec_num() {
        Log.d(TAG, "pvr_get_total_rec_num");
        int totalFileNumber = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_Total_Rec_Num);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            totalFileNumber = reply.readInt();
        }
        Log.d(TAG, "Pvr_Get_Total_Rec_Num: ret = " + ret + " , totalFileNumber = " + totalFileNumber);

        request.recycle();
        reply.recycle();
        return totalFileNumber;
    }

    public int pvr_get_total_one_series_rec_num(String recordUniqueId) {
        Log.d(TAG, "pvr_get_total_one_series_rec_num(" + recordUniqueId + ")");
        int totalFileNumber = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_Total_One_Series_Rec_Num);
        request.writeString(recordUniqueId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            totalFileNumber = reply.readInt();
        }
        Log.d(TAG, "pvr_get_total_one_series_rec_num: ret = " + ret + " , totalFileNumber = " + totalFileNumber);

        request.recycle();
        reply.recycle();
        return totalFileNumber;
    }

    public int pvr_delete_total_records_file() {
        Log.d(TAG, "pvr_delete_total_records_file");
        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_Total_Records_File);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "pvr_delete_total_records_file: ret = " + ret);

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_delete_one_series_folder(String recordUniqueId) {
        Log.d(TAG, "pvr_delete_one_series_folder(" + recordUniqueId + ")");
        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_One_Series_Folder);
        request.writeString(recordUniqueId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "pvr_delete_one_series_folder: ret = " + ret);

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_delete_record_file_by_ch_id(int channelId) {
        Log.d(TAG, "pvr_delete_record_file_by_ch_id(" + channelId + ")");
        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_Record_File_By_Ch_Id);
        request.writeInt(channelId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "pvr_delete_record_file_by_ch_id: ret = " + ret);

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pvr_delete_record_file(String recordUniqueId) {
        Log.d(TAG, "pvr_delete_record_file(" + recordUniqueId + ")");
        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_Record_File);
        request.writeString(recordUniqueId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "pvr_delete_record_file: ret = " + ret);

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public List<PvrFileInfo> pvr_get_records_file(int startIndex, int total) {
        Log.d(TAG, "pvr_get_records_file(" + startIndex + "," + total + ")");
        List<PvrFileInfo> pvrFileInfoList = new ArrayList<>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_Records_File);
        request.writeInt(startIndex);
        request.writeInt(total);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            for (int i = 0; i < total; i++) {
                PvrFileInfo fileInfo = new PvrFileInfo();
                fileInfo.recordUniqueId = reply.readString();
                fileInfo.isSeries = (reply.readInt() == 1);
                fileInfo.episode = reply.readInt();
                fileInfo.channelNumber = reply.readInt();
                fileInfo.channelLock = reply.readInt();
                fileInfo.parentalRate = reply.readInt();
                fileInfo.recordStatus = reply.readInt();
                fileInfo.channelId = reply.readInt();
                fileInfo.year = reply.readInt();
                fileInfo.month = reply.readInt();
                fileInfo.date = reply.readInt();
                fileInfo.week = reply.readInt();
                fileInfo.hour = reply.readInt();
                fileInfo.minute = reply.readInt();
                fileInfo.second = reply.readInt();
                fileInfo.durationInMs = reply.readInt();
                fileInfo.serviceType = reply.readInt();
                fileInfo.fileSize = reply.readLong();
                fileInfo.channelName = reply.readString();
                fileInfo.eventName = reply.readString();
                fileInfo.fullNamePath = reply.readString();

                fileInfo.durationSec = fileInfo.durationInMs / 1000;
                if (fileInfo.fullNamePath != null) {
                    String[] splitName = fileInfo.fullNamePath.split("/");
                    int index = splitName.length - 1;
                    if (index >= 0)
                        fileInfo.realFileName = splitName[index];
                }
                pvrFileInfoList.add(fileInfo);
            }
        }
        Log.d(TAG, "pvr_get_records_file: ret = " + ret);

        request.recycle();
        reply.recycle();
        return pvrFileInfoList;
    }

    public List<PvrFileInfo> pvr_get_total_one_series_records_file(int startIndex, int total, String recordUniqueId) {
        Log.d(TAG, "pvr_get_total_one_series_records_file: (" + startIndex + "," + total + "," + recordUniqueId + ")");
        List<PvrFileInfo> pvrFileInfoList = new ArrayList<>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_One_Series_Records_File);
        request.writeInt(startIndex);
        request.writeInt(total);
        request.writeString(recordUniqueId);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            for (int i = 0; i < total; i++) {
                PvrFileInfo fileInfo = new PvrFileInfo();
                fileInfo.recordUniqueId = reply.readString();
                fileInfo.isSeries = (reply.readInt() == 1);
                fileInfo.episode = reply.readInt();
                fileInfo.channelNumber = reply.readInt();
                fileInfo.channelLock = reply.readInt();
                fileInfo.parentalRate = reply.readInt();
                fileInfo.recordStatus = reply.readInt();
                fileInfo.channelId = reply.readInt();
                fileInfo.year = reply.readInt();
                fileInfo.month = reply.readInt();
                fileInfo.date = reply.readInt();
                fileInfo.week = reply.readInt();
                fileInfo.hour = reply.readInt();
                fileInfo.minute = reply.readInt();
                fileInfo.second = reply.readInt();
                fileInfo.durationInMs = reply.readInt();
                fileInfo.durationSec = fileInfo.durationInMs / 1000;
                fileInfo.serviceType = reply.readInt();
                fileInfo.fileSize = reply.readLong();
                fileInfo.channelName = reply.readString();
                fileInfo.eventName = reply.readString();
                fileInfo.fullNamePath = reply.readString();
                if (fileInfo.fullNamePath != null) {
                    String[] splitName = fileInfo.fullNamePath.split("/");
                    int index = splitName.length - 1;
                    if (index >= 0)
                        fileInfo.realFileName = splitName[index];
                }
                pvrFileInfoList.add(fileInfo);
            }
        }
        Log.d(TAG, "pvr_get_total_one_series_records_file: ret = " + ret);

        request.recycle();
        reply.recycle();
        return pvrFileInfoList;
    }
}
