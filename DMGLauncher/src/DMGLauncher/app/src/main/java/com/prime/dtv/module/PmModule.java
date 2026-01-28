package com.prime.dtv.module;


import android.content.Context;
import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.DefaultChannel;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnNetworkType;
import com.prime.dtv.sysdata.EnTVRadioFilter;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.EnUseGroupType;
import com.prime.dtv.sysdata.FavGroupName;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.NetProgramInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.ProgramPlayStreamType;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.SimpleChannel;
import com.prime.dtv.sysdata.TpInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PmModule {
    private static final String TAG = "PmModule";

    private static final int CMD_PM_Base = PrimeDtvMediaPlayer.CMD_Base + 0x600;

    private static final int CMD_PM_GetLastChannelID = CMD_PM_Base + 0x01;
    private static final int CMD_PM_GetPreviousChannelID = CMD_PM_Base + 0x02;
    private static final int CMD_PM_GetChannelUpID = CMD_PM_Base + 0x03;
    private static final int CMD_PM_GetChannelDownID = CMD_PM_Base + 0x04;
    private static final int CMD_PM_GetChannelByID = CMD_PM_Base + 0x05;
    private static final int CMD_PM_GetChannelByLCN = CMD_PM_Base + 0x06;

    // Channel edit CMD
    private static final int CMD_PM_SetChannel = CMD_PM_Base + 0x10;
    private static final int CMD_PM_DelChannelByID = CMD_PM_Base + 0x11;
    private static final int CMD_PM_DelAllChannel = CMD_PM_Base + 0x12;
    private static final int CMD_PM_ClearAllChannelsInAllAntenna = CMD_PM_Base + 0x13;
    private static final int CMD_PM_LockChannel = CMD_PM_Base + 0x14;
    private static final int CMD_PM_IsChannelLocked = CMD_PM_Base + 0x15;
    private static final int CMD_PM_SkipChannel = CMD_PM_Base + 0x16;
    private static final int CMD_PM_IsChannelSkipped = CMD_PM_Base + 0x17;
    private static final int CMD_PM_ChannelRename = CMD_PM_Base + 0x18;
    private static final int CMD_PM_ChannelMoveTo = CMD_PM_Base + 0x19;
    private static final int CMD_PM_ChannelSwap = CMD_PM_Base + 0x1A;
    private static final int CMD_PM_ChannelSort = CMD_PM_Base + 0x1B;

    // Group CMD
    private static final int CMD_PM_AddChannelToFavList = CMD_PM_Base + 0x20;
    private static final int CMD_PM_RemoveChannelFromFavList = CMD_PM_Base + 0x21;
    private static final int CMD_PM_SetServiceMode = CMD_PM_Base + 0x22;
    private static final int CMD_PM_GetServiceMode = CMD_PM_Base + 0x23;
    private static final int CMD_PM_GetChannelNum = CMD_PM_Base + 0x24;
    private static final int CMD_PM_GetChannelList = CMD_PM_Base + 0x25;
    private static final int CMD_PM_GetChannelIndexInList = CMD_PM_Base + 0x26;

    // Store CMD
    private static final int CMD_PM_SaveToFlash = CMD_PM_Base + 0x30;
    private static final int CMD_PM_RecoverFromFlash = CMD_PM_Base + 0x31;
    private static final int CMD_PM_GetPresetFrequencyNum = CMD_PM_Base + 0x32;
    private static final int CMD_PM_GetPresetFrequency = CMD_PM_Base + 0x33;

    // Satellite TP CMD
    private static final int CMD_PM_AddSatellite = CMD_PM_Base + 0x40;
    private static final int CMD_PM_DelSatellite = CMD_PM_Base + 0x41;
    private static final int CMD_PM_DelAllSatellite = CMD_PM_Base + 0x42;
    private static final int CMD_PM_SatelliteRename = CMD_PM_Base + 0x43;
    private static final int CMD_PM_GetSatellite = CMD_PM_Base + 0x44;
    private static final int CMD_PM_GetSatelliteNum = CMD_PM_Base + 0x45;
    private static final int CMD_PM_GetSatelliteList = CMD_PM_Base + 0x46;
    private static final int CMD_PM_GetSatelliteAntennaInfo = CMD_PM_Base + 0x47;
    private static final int CMD_PM_SetSatelliteAntennaInfo = CMD_PM_Base + 0x48;
    private static final int CMD_PM_SetSatelliteLongitude = CMD_PM_Base + 0x49;
    private static final int CMD_PM_SatelliteAddTp = CMD_PM_Base + 0x4A;
    private static final int CMD_PM_GetSatelliteTpNum = CMD_PM_Base + 0x4B;
    private static final int CMD_PM_GetSatelliteTpList = CMD_PM_Base + 0x4C;
    private static final int CMD_PM_GetSatelliteTpByID = CMD_PM_Base + 0x4D;
    private static final int CMD_PM_SetSatelliteTp = CMD_PM_Base + 0x4E;
    private static final int CMD_PM_DelSatelliteTp = CMD_PM_Base + 0x4F;
    private static final int CMD_PM_DelSatelliteAllTp = CMD_PM_Base + 0x50;
    private static final int CMD_PM_GetFreqInfoByChannel = CMD_PM_Base + 0x51;

    private static final int CMD_PM_SaveSampleChannelList = CMD_PM_Base + 209;
    private static final int CMD_PM_SaveChannel = CMD_PM_Base + 210;
    private static final int CMD_PM_SaveGroupList = CMD_PM_Base + 211;
    private static final int CMD_PM_SaveTpList = CMD_PM_Base + 212;
    private static final int CMD_PM_SaveSatList = CMD_PM_Base + 213;
    private static final int CMD_PM_GetGroupChannel = CMD_PM_Base + 214;
    private static final int CMD_PM_GetGroupChannelList = CMD_PM_Base + 215;
    private static final int CMD_PM_DelGroupChannel = CMD_PM_Base + 216;
    private static final int CMD_PM_DelGroupAll = CMD_PM_Base + 217;
    private static final int CMD_PM_GetSimpleChannelList = CMD_PM_Base + 218;
    private static final int CMD_PM_GetSimpleChannel = CMD_PM_Base + 219;
    private static final int CMD_PM_SaveGroupName = CMD_PM_Base + 220;
    private static final int CMD_PM_GetGroupName = CMD_PM_Base + 221;
    private static final int CMD_PM_GetChannelFilter = CMD_PM_Base + 222;

    // JAVA CMD
    // JAVA PM
    private static final int CMD_JAVA_Base = PrimeDtvMediaPlayer.CMD_JAVA_Base;
    private static final int CMD_PM_CreateChannel = CMD_JAVA_Base + CMD_PM_Base + 0x01;
    private static final int CMD_PM_SetDefaultOpenChannel = CMD_JAVA_Base + CMD_PM_Base + 0x02;
    private static final int CMD_PM_GetDefaultOpenChannel = CMD_JAVA_Base + CMD_PM_Base + 0x03;
    private static final int CMD_PM_GetChannelNO = CMD_JAVA_Base + CMD_PM_Base + 0x04;
    private static final int CMD_PM_GetChannelExternByID = CMD_JAVA_Base + CMD_PM_Base + 0x05;
    private static final int CMD_PM_ChannelSortExtern = CMD_JAVA_Base + CMD_PM_Base + 0x06;
    private static final int CMD_PM_DelChannelByTag = CMD_JAVA_Base + CMD_PM_Base + 0x07;
    private static final int CMD_PM_DelChannelByNetWorkID = CMD_JAVA_Base + CMD_PM_Base + 0x08;
    private static final int CMD_PM_DelChannelBySignalType = CMD_JAVA_Base + CMD_PM_Base + 0x09;
    private static final int CMD_PM_DelChannelByTpID = CMD_JAVA_Base + CMD_PM_Base + 0x0A;

    private static final int CMD_PM_GetChannelGroupName = CMD_JAVA_Base + CMD_PM_Base + 0x10;
    private static final int CMD_PM_SetChannelGroupName = CMD_JAVA_Base + CMD_PM_Base + 0x11;
    private static final int CMD_PM_RebuildGroup = CMD_JAVA_Base + CMD_PM_Base + 0x12;
    private static final int CMD_PM_GetGroupFilter = CMD_JAVA_Base + CMD_PM_Base + 0x13;
    private static final int CMD_PM_RebuildAllGroup = CMD_JAVA_Base + CMD_PM_Base + 0x14;
    private static final int CMD_PM_GetDefaultOpenGroupType = CMD_JAVA_Base + CMD_PM_Base + 0x15;
    private static final int CMD_PM_GetChannelGroup = CMD_JAVA_Base + CMD_PM_Base + 0x16;
    private static final int CMD_PM_GetUseGroups = CMD_JAVA_Base + CMD_PM_Base + 0x17;
    private static final int CMD_PM_GetChannelListExtern = CMD_JAVA_Base + CMD_PM_Base + 0x18;

    private static final int CMD_PM_ExportDBToFile = CMD_JAVA_Base + CMD_PM_Base + 0x20;
    private static final int CMD_PM_ImportDBFromFile = CMD_JAVA_Base + CMD_PM_Base + 0x21;
    private static final int CMD_PM_ImportDBFromIniFile = CMD_JAVA_Base + CMD_PM_Base + 0x22;
    private static final int CMD_PM_ClearTable = CMD_JAVA_Base + CMD_PM_Base + 0x23;
    private static final int CMD_PM_SaveTable = CMD_JAVA_Base + CMD_PM_Base + 0x24;
    private static final int CMD_PM_RestoreTable = CMD_JAVA_Base + CMD_PM_Base + 0x25;

    private static final int CMD_PM_GetDeliveryIDByTpID = CMD_JAVA_Base + CMD_PM_Base + 0x30;
    private static final int CMD_PM_GetDeliveryByID = CMD_JAVA_Base + CMD_PM_Base + 0x31;
    private static final int CMD_PM_GetDeliverSystemCount = CMD_JAVA_Base + CMD_PM_Base + 0x32;
    private static final int CMD_PM_GetDeliverSystemList = CMD_JAVA_Base + CMD_PM_Base + 0x33;
    private static final int CMD_PM_GetTpByID = CMD_JAVA_Base + CMD_PM_Base + 0x34;
    private static final int CMD_PM_GetTpIDByChannelID = CMD_JAVA_Base + CMD_PM_Base + 0x35;
    private static final int CMD_PM_SetDeliveryExtern = CMD_JAVA_Base + CMD_PM_Base + 0x36;
    private static final int CMD_PM_SetTp = CMD_JAVA_Base + CMD_PM_Base + 0x37;


    private ArrayList<List<SimpleChannel>> g_total_channel_list = new ArrayList<>();
    private ArrayList<FavGroupName> g_all_program_group = null;

    public ArrayList<List<SimpleChannel>> get_total_channel_list() {
        return g_total_channel_list;
    }

    public ArrayList<FavGroupName> get_all_program_group() {
        if (g_all_program_group == null) g_all_program_group = new ArrayList<FavGroupName>();

        return g_all_program_group;
    }

    private int calc_longitude_from_angle(float angle, int angleEW) {
        int longitude = (int) (angle * SatInfo.LONGITUDE_VALUE_RATE);

        if (angleEW == SatInfo.ANGLE_W) {
            longitude = SatInfo.LONGITUDE_VALUE_MAX - longitude;
        }

        return longitude;
    }

    private void pm_get_program_by_reply(ProgramInfo program, Parcel reply) {
        //Log.d(TAG, "pm_get_program_by_reply");
        int i = 0, tmp = 0;
        String tmpString;
        int Lock = 0;
        int Skip = 0;
        int Delete = 0;
        int Move = 0;

        reply.readInt();
        //ChannelId (chNode.channelID)
        program.setChannelId(PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt()));

        //DisplayName (chNode.OrignalServiceName)
        program.setDisplayName(reply.readString());

        //TransportStreamId (chNode.TSID)
        program.setTransportStreamId(reply.readInt());

        //CA (chNode.bCAMode)
        program.setCA(reply.readInt());

        //DisplayNum (chNode.LCN)
        program.setDisplayNum(reply.readInt());

        //Lock
        program.setLock(reply.readInt());
        //Skip
        program.setSkip(reply.readInt());
        Delete = reply.readInt();//delete
        Move = reply.readInt();//move
        tmp = reply.readInt();//chNode.tempFlag = reply.readInt();
        reply.readInt();//bUsrModifyFlag
        reply.readInt();//u16HDFlag
        reply.readInt();//bPUTempFlag

        tmp = reply.readInt();//chNode.favorTag = reply.readInt();

        //OriginalNetworkId (chNode.origNetworkID)
        program.setOriginalNetworkId(reply.readInt());

        //ServiceId (chNode.serviceID)
        program.setServiceId(reply.readInt());

        //Type (chNode.serviceType)
        tmp = reply.readInt();
        program.setType(tmp);

        //pesi not use
        tmp = reply.readInt();//(chNode.HasScheduleEPG)

        //pesi not use
        tmp = reply.readInt();//(chNode.HasPFEPG)

        //pesi not use
        tmp = reply.readInt();//(chNode.AudPid)

        //pVideo (chNode.VidPid)
        program.pVideo.setPID(reply.readInt());

        //pesi not use
        tmp = reply.readInt();//(chNode.PmtPid)

        //Pcr (chNode.PcrPID)
        program.setPcr(reply.readInt());

        //pVideo (chNode.VidType)
        program.pVideo.setCodec(reply.readInt());

        //pesi not use
        tmp = reply.readInt();//(chNode.AudType)

//Log.d(TAG, "pm_get_program_by_reply  program name :" + chNode.OrignalServiceName + " fav :" + chNode.favorTag + " servicetype:" + chNode.serviceType);
    }


    private void pm_get_program_extern_by_reply(ProgramInfo program, Parcel reply) {
        Log.d(TAG, "pm_get_program_extern_by_reply");
        int i = 0, tmp = 0;

        //SatId (chNode.deliveryID)
        program.setSatId(reply.readInt());

        //TpId (chNode.TpId)
        program.setTpId(reply.readInt());

        //pesi not use
        tmp = reply.readInt();//(chNode.volTrack.Volume)

        //AudioLRSelected (chNode.volTrack.AudioChannel)
        program.setAudioLRSelected(reply.readInt());

        //AudioSelected (chNode.volTrack.AudioIndex)
        program.setAudioSelected(reply.readInt());

        //pAudios
        tmp = reply.readInt(); //(chNode.AudioNum)
        for (i = 0; i < tmp; i++) {
            int Type, Pid, AudioType, TrackMode;
            String szLangCode;
            Type = reply.readInt();
            Pid = reply.readInt();
            AudioType = reply.readInt();
            TrackMode = reply.readInt();
            szLangCode = reply.readString();
            program.pAudios.add(new ProgramInfo.AudioInfo(Pid, AudioType, szLangCode, szLangCode));
        }

        //pSubtitle
        tmp = reply.readInt();   //(chNode.SubtNum)
        for (i = 0; i < tmp; i++) {
            int Type, Pid;
            String szLangCode;

            Type = reply.readInt();
            Pid = reply.readInt();
            szLangCode = reply.readString();
            program.pSubtitle.add(new ProgramInfo.SubtitleInfo(Type,Pid, szLangCode, 0, 0));
        }

        //pTeletext
        tmp = reply.readInt(); //(chNode.TTX_SubtNum)
        for (i = 0; i < tmp; i++) {
            int Type, Pid, magazineNumber, pageNumber;
            String szLangCode;

            Type = reply.readInt();
            Pid = reply.readInt();
            szLangCode = reply.readString();
            magazineNumber = (Pid & (0x0000FF00)) >> 8;
            pageNumber = Pid & 0xFF;
            program.pTeletext.add(new ProgramInfo.TeletextInfo(Pid, Type, szLangCode, magazineNumber, pageNumber));
        }
        //Log.d(TAG, "pm_get_program_extern_by_reply ChannelNode name :" + chNode.OrignalServiceName + " fav :" + chNode.favorTag + " servicetype:" + chNode.serviceType);
    }

    private int conv_to_server_pos(int groupType) {
        if ((0 == groupType) || (1 == groupType) || (2 == groupType) || (3 == groupType)) {
            return 0;
        }

        if ((groupType >= 19) && (groupType <= 35)) {
            return (groupType - 19);
        }

        if ((groupType >= 36) && (groupType <= 335)) {
            return (groupType - 36);
        }

        if ((groupType >= 336) && (groupType <= 367)) {
            return (groupType - 336);
        }

        if ((groupType >= 368) && (groupType <= 375)) {
            return (groupType - 368);
        }

        return 0;
    }

    public DefaultChannel get_default_channel() {
        DefaultChannel defChannal = null;
        Log.d(TAG, "getDefaultChannel : ");
        long defOpenChannelId = PrimeDtvMediaPlayer.excute_command_getII(CMD_PM_GetLastChannelID);
        if (defOpenChannelId <= -1)//Scoty 20180529 fixed default open channel get last channel less than -1,UI will get wrong channelId
            defOpenChannelId = 0;

        Log.d(TAG, "get_default_channel: defOpenChannelId=" + defOpenChannelId);

        if (defOpenChannelId != 0) {
            defChannal = new DefaultChannel();
            defChannal.setChanneId(defOpenChannelId);
            defChannal.setGroupType(get_default_open_group_type());
        }

        return defChannal;
    }

    public ProgramInfo get_program_by_channel_id(long channelId) {
        //Log.d(TAG, "GetProgramByChannelId " + channelId);
        ProgramInfo program = null;
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PM_GetChannelByID);
        request1.writeInt((int) channelId);
        PrimeDtvMediaPlayer.invokeex(request1, reply1);
        int ret = reply1.readInt();
        //Log.d(TAG, "get_program_by_channel_id ret = " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            program = new ProgramInfo();
            pm_get_program_by_reply(program, reply1);
        }

        request1.recycle();
        reply1.recycle();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Parcel request2 = Parcel.obtain();
            Parcel reply2 = Parcel.obtain();

            request2.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request2.writeInt(CMD_PM_GetChannelExternByID);
            request2.writeInt((int) channelId);

            PrimeDtvMediaPlayer.invokeex(request2, reply2);
            ret = reply2.readInt();
            Log.d(TAG, "CMD_PM_GetChannelExternByID ret = " + ret);
            if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
                pm_get_program_extern_by_reply(program, reply2);
            }

            request2.recycle();
            reply2.recycle();
        }

        return program;
    }

    public void delete_program(long channelId) {
        PrimeDtvMediaPlayer.excute_command(CMD_PM_DelChannelByID, channelId);
        //saveTable(EnTableType.PROGRAME);
    }

    private int set_service_mode(int mode) {
        Log.d(TAG, "set_service_mode = " + mode);
        return PrimeDtvMediaPlayer.excute_command(CMD_PM_SetServiceMode, mode);
    }

    public int update_satList(List<SatInfo> satInfoList) {
        Log.d(TAG, "update_satList");
        int ret = 0;
        if (satInfoList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveSatList);
            request.writeInt(satInfoList.size());
            for (int i = 0; i < satInfoList.size(); i++) {
                request.writeInt(satInfoList.get(i).getSatId());
                request.writeString(satInfoList.get(i).getSatName());
                request.writeInt(satInfoList.get(i).getTunerType());
                request.writeInt(satInfoList.get(i).getTpNum());
                request.writeInt(calc_longitude_from_angle(satInfoList.get(i).getAngle(), satInfoList.get(i).getAngleEW()));
                request.writeInt(satInfoList.get(i).getLocation());
                request.writeInt(satInfoList.get(i).getPostionIndex());
                request.writeInt(satInfoList.get(i).Antenna.getLnb1());
                request.writeInt(satInfoList.get(i).Antenna.getLnb2());
                request.writeInt(satInfoList.get(i).Antenna.getLnbType());
                request.writeInt(satInfoList.get(i).Antenna.getDiseqcType());
                //request.writeInt(satInfoList.get(i).Antenna.getDiseqcUse());
                request.writeInt(satInfoList.get(i).Antenna.getDiseqc());
                request.writeInt(satInfoList.get(i).Antenna.getTone22kUse());
                request.writeInt(satInfoList.get(i).Antenna.getTone22k());
                request.writeInt(satInfoList.get(i).Antenna.getV1418Use());
                request.writeInt(satInfoList.get(i).Antenna.getV1418());
                request.writeInt(satInfoList.get(i).Antenna.getCku());
                for (int j = 0; j < satInfoList.get(i).getTpNum(); j++) {
                    request.writeInt(satInfoList.get(i).getTps().get(j));
                }
            }
            PrimeDtvMediaPlayer.invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }


    public int update_tp_list(List<TpInfo> tpInfoList) {
        Log.d(TAG, "update_tp_list");
        int ret = 0;
        if (tpInfoList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveTpList);
            request.writeInt(tpInfoList.size());
            for (int i = 0; i < tpInfoList.size(); i++) {
                request.writeInt(tpInfoList.get(i).getTpId());
                request.writeInt(tpInfoList.get(i).getSatId());
                request.writeInt(tpInfoList.get(i).getTunerType());
                if (tpInfoList.get(i).getTunerType() == TpInfo.DVBT || tpInfoList.get(i).getTunerType() == TpInfo.ISDBT) {
                    //dvb t
                    request.writeInt(tpInfoList.get(i).TerrTp.getChannel());
                    request.writeInt(tpInfoList.get(i).TerrTp.getFreq());
                    request.writeInt(tpInfoList.get(i).TerrTp.getBand());
                } else if (tpInfoList.get(i).getTunerType() == TpInfo.DVBS) {
                    //dvb s
                    request.writeInt(tpInfoList.get(i).SatTp.getFreq());
                    request.writeInt(tpInfoList.get(i).SatTp.getSymbol());
                    request.writeInt(tpInfoList.get(i).SatTp.getPolar());
                } else if (tpInfoList.get(i).getTunerType() == TpInfo.DVBC) {
                    //dvb c
                    request.writeInt(tpInfoList.get(i).CableTp.getChannel());
                    request.writeInt(tpInfoList.get(i).CableTp.getFreq());
                    request.writeInt(tpInfoList.get(i).CableTp.getSymbol());
                    request.writeInt(tpInfoList.get(i).CableTp.getQam());
                } else {
                    continue;
                }
            }
            PrimeDtvMediaPlayer.invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int update_total_channel_list(List<SimpleChannel> simpleChannelList, int type) {
        int ret = 0;
        if (simpleChannelList.size() > 0) {
            for (int i = 0; i < simpleChannelList.size(); i++) {
                g_total_channel_list.get(type).add(simpleChannelList.get(i));
            }
        } else {
            ret = 1;
        }
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_simple_channel_list(List<SimpleChannel> simpleChannelList, int type) {
        Log.d(TAG, "update_simple_channel_list");
        int ret = 0;
        update_total_channel_list(simpleChannelList, type);
        if (simpleChannelList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveSampleChannelList);
            request.writeInt(type);
            request.writeInt(simpleChannelList.size());
            if (type == FavGroup.ALL_TV_TYPE || type == FavGroup.ALL_RADIO_TYPE) {
                for (int i = 0; i < simpleChannelList.size(); i++) {
                    request.writeInt((int) simpleChannelList.get(i).get_channel_id());
                    request.writeInt(simpleChannelList.get(i).get_channel_num());
                    request.writeString(simpleChannelList.get(i).get_channel_name());
                    request.writeInt(simpleChannelList.get(i).get_user_lock());
                    request.writeInt(simpleChannelList.get(i).get_channel_skip());//Scoty 20181109 modify for skip channel
                }
            } else {
                for (int i = 0; i < simpleChannelList.size(); i++) {
                    request.writeInt((int) simpleChannelList.get(i).get_channel_id());
                    request.writeInt(simpleChannelList.get(i).get_channel_num());
//gary20200619 fix save fav list not work,only save first channel
//                    request.writeString(simpleChannelList.get(i).getChannelName());
//                    request.writeInt(simpleChannelList.get(i).getUserLock());
//                    request.writeInt(simpleChannelList.get(i).getChannelSkip()); // Edwin 20181129 add these to match CMD
                }
            }
            PrimeDtvMediaPlayer.invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_program_info(ProgramInfo pProgram) {
        int ret = -1;
        Log.d(TAG, "update_program_info some param - only AudioSelected,AudioLRSelected.");
        if (pProgram != null) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveChannel);
            request.writeInt((int) pProgram.getChannelId());
            request.writeInt(pProgram.getAudioSelected());
            request.writeInt(pProgram.getAudioLRSelected());
            PrimeDtvMediaPlayer.invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int fav_info_update_list(int favMode, List<FavInfo> favInfo) {
        Log.d(TAG, "FavInfoUpdateList");
        int ret = 0;
        if (favInfo.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveGroupList);
            request.writeInt(favInfo.size());
            for (int i = 0; i < favInfo.size(); i++) {
                request.writeInt(favInfo.get(i).getFavNum());
                request.writeInt((int) favInfo.get(i).getChannelId());
                request.writeInt(favInfo.get(i).getFavMode());
            }
            PrimeDtvMediaPlayer.invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }



    /*public int editChannel(ChannelNode channelNode)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_EditChannel);
        request.writeInt(channelNode.channelID);
        //request.writeInt(channelNode.AudPid);
        //request.writeInt(channelNode.AudType);
        //request.writeInt(channelNode.VidPid);
        //request.writeInt(channelNode.VidType);
        //request.writeInt(channelNode.PcrPID);
        request.writeInt(channelNode.favorTag);
        request.writeInt(channelNode.editTag);
        request.writeString(channelNode.OrignalServiceName);
        request.writeInt(channelNode.TPID);
        //request.writeInt(channelNode.volTrack.Volume);
        //request.writeInt(channelNode.volTrack.AudioChannel);
        request.writeInt(channelNode.LCN);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "editChannel ret = " + ret);
        return PrimeDtvMediaPlayer.getReturnValue(ret);
    }*/

    public int set_default_open_channel(long channelId, int groupType) {
        Log.d(TAG, "set_default_open_channel groupType = " + groupType + ", channelId=" + channelId);
        return PrimeDtvMediaPlayer.excute_command(CMD_PM_SetDefaultOpenChannel, channelId, groupType);
    }

    public List<ProgramInfo> get_program_info_list(int type, int pos, int num) {
        List<ProgramInfo> programList = null;
        ProgramInfo tmp = null;
        int chCount;

        int defaultOpenGroupType = get_default_open_group_type();
        if (type == FavGroup.ALL_TV_TYPE) {
            set_service_mode(EnTVRadioFilter.TV.ordinal());
        } else if (type == FavGroup.ALL_RADIO_TYPE) {
            set_service_mode(EnTVRadioFilter.RADIO.ordinal());
        }

        if (pos == MiscDefine.ProgramInfo.POS_ALL && num == MiscDefine.ProgramInfo.NUM_ALL) {
            //Log.d(TAG, "defaultOpenGroupType: "+defaultOpenGroupType);
            //chCount = getChannelCount(defaultOpenGroupType);
            chCount = get_channel_count(type);
            if (chCount > 0) {
                //programList = getProgramList(defaultOpenGroupType, 0, chCount);
                programList = get_program_list(type, 0, chCount);
                if (programList != null && !programList.isEmpty()) {
                    return programList;
                }
            }
            Log.d(TAG, "GetProgramInfoList: return null");
        }
        return null;
    }

    public List<ProgramInfo> get_program_list(int groupType, int pos, int num) {
        //Log.d(TAG, "getProgramList groupType: " + groupType);
        List<ProgramInfo> programList = new ArrayList<ProgramInfo>();
        //if (pos < 0 || num <= 0 || num > 100) //eric lin 20180209 remove get 100 channal limit
        //{
        //    return channelList;
        //}

        int i = 0;
        int ret = 0;
        int retNum = 0;

        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PM_GetChannelList);
        //request1.writeInt(groupType);

        request1.writeInt(groupType);
        request1.writeInt(conv_to_server_pos(groupType));
        request1.writeInt(pos);
        request1.writeInt(num);
        //request1.writeString(mLang);

        PrimeDtvMediaPlayer.invokeex(request1, reply1);
        ret = reply1.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            retNum = reply1.readInt();
            Log.d(TAG, "Channel Num =" + retNum);
            for (i = 0; i < retNum; i++) {
                ProgramInfo program = new ProgramInfo();
                pm_get_program_by_reply(program, reply1);
                programList.add(program);
            }
            Log.d(TAG, "getProgramList end");
        }

        request1.recycle();
        reply1.recycle();

        Parcel request2 = Parcel.obtain();
        Parcel reply2 = Parcel.obtain();

        request2.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request2.writeInt(CMD_PM_GetChannelListExtern);
        request2.writeInt(groupType);
        request2.writeInt(pos);
        request2.writeInt(num);

        PrimeDtvMediaPlayer.invokeex(request2, reply2);

        ret = reply2.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            retNum = reply2.readInt();

            for (i = 0; i < retNum; i++) {
                ProgramInfo program = programList.get(i);
                Log.d(TAG, "CMD_PM_GetChannelExternByID ret = " + ret);
                if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
                    pm_get_program_extern_by_reply(program, reply2);
                }
            }
        }

        request2.recycle();
        reply2.recycle();

        return programList;
    }


    //Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
    public List<SimpleChannel> get_simple_program_list_from_total_channelList(int type, int IncludeSkipFlag, int IncludePVRSkipFlag) {
        List<SimpleChannel> simpleList = new ArrayList<SimpleChannel>();

        for (int i = 0; i < g_total_channel_list.get(type).size(); i++) {
            if (IncludeSkipFlag == 1 && IncludePVRSkipFlag == 1)
                simpleList.add(g_total_channel_list.get(type).get(i));
            else if (IncludeSkipFlag == 0 && IncludePVRSkipFlag == 1) {
                if (g_total_channel_list.get(type).get(i).get_channel_skip() != 1)
                    simpleList.add(g_total_channel_list.get(type).get(i));
            } else if (IncludeSkipFlag == 1 && IncludePVRSkipFlag == 0) {
                if (g_total_channel_list.get(type).get(i).get_pvr_skip() != 1)
                    simpleList.add(g_total_channel_list.get(type).get(i));
            } else {
                if (g_total_channel_list.get(type).get(i).get_pvr_skip() != 1)
                    simpleList.add(g_total_channel_list.get(type).get(i));
                else if (g_total_channel_list.get(type).get(i).get_channel_skip() != 1)
                    simpleList.add(g_total_channel_list.get(type).get(i));
            }

        }

        return simpleList;
    }

    public List<SimpleChannel> get_simple_program_list(int type, int IncludeSkipFlag, int IncludePVRSkipFlag) {
        List<SimpleChannel> ChannelList = null;
        int ret = -1;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSimpleChannelList);
        request.writeInt(type);
        request.writeInt(IncludeSkipFlag);
        request.writeInt(IncludePVRSkipFlag);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            ChannelList = new ArrayList<>();
            int NumOfChannel = reply.readInt();
            for (int i = 0; i < NumOfChannel; i++) {
                //Scoty 20180613 change get simplechannel list for PvrSkip rule -s
                SimpleChannel channel = new SimpleChannel();
                channel.set_channel_id(PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt()));
                channel.set_channel_num(reply.readInt());
                channel.set_channel_name(reply.readString());
                channel.set_user_lock(reply.readInt());
                channel.set_ca(reply.readInt());
                channel.set_tp_id(reply.readInt());
                channel.set_channel_skip(reply.readInt());//Scoty 20181109 modify for skip channel
                channel.set_play_stream_type(ProgramPlayStreamType.DVB_TYPE);//Scoty Add Youtube/Vod Stream
                ChannelList.add(channel);
                //Scoty 20180613 change get simplechannel list for PvrSkip rule -e
            }

            Log.d(TAG, "GetSimpleProgramListForPesi: " + ChannelList.size());
        }
        request.recycle();
        reply.recycle();
        return ChannelList;
    }

    public ProgramInfo get_program_by_lcn(int lcn, int type) {
        List<ProgramInfo> allProgramInfo = null;
        Log.d(TAG, "GetProgramByLcn: lcn,=" + lcn + ", type=" + type);

        allProgramInfo = get_program_info_list(type, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);
        if (allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                Log.d(TAG, "GetProgramByLcn: name=" + allProgramInfo.get(i).getDisplayName() + ", num=" + allProgramInfo.get(i).getDisplayNum() + ", type=" + allProgramInfo.get(i).getType());
                if (allProgramInfo.get(i).getDisplayNum() == lcn && allProgramInfo.get(i).getType() == type)
                    return allProgramInfo.get(i);
            }
        }
        return null;
    }

    public ProgramInfo get_program_by_chnum(int chnum, int type) {
        List<ProgramInfo> allProgramInfo = null;

        allProgramInfo = get_program_info_list(type, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);

        if (allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                if (allProgramInfo.get(i).getDisplayNum() == chnum && allProgramInfo.get(i).getType() == type)
                    return allProgramInfo.get(i);
            }
        }
        return null;
    }


    public SimpleChannel get_simple_program_by_channel_id(long channelId) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSimpleChannel);
        request.writeInt((int) channelId);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        SimpleChannel channel = null;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            channel = new SimpleChannel();
            channel.set_channel_id(PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt()));
            channel.set_channel_num(reply.readInt());
            channel.set_channel_name(reply.readString());
            channel.set_user_lock(reply.readInt());
            channel.set_ca(reply.readInt());
            channel.set_tp_id(reply.readInt());
            channel.set_channel_skip(reply.readInt());//Scoty 20181109 modify for skip channel
        }
        request.recycle();
        reply.recycle();
        return channel;
    }

    public SimpleChannel get_simple_program_by_channel_id_from_total_channel_list_by_group(int groupType, long channelId) {
        for (int i = 0; i < g_total_channel_list.get(groupType).size(); i++) {
            if (channelId == g_total_channel_list.get(groupType).get(i).get_channel_id()) {
                Log.d(TAG, "get_simple_program_by_channel_id_from_total_channel_list_by_group: Name = [" + g_total_channel_list.get(groupType).get(i).get_channel_name() + "] channelId = [" + channelId + "] groupType = [" + groupType + "]");
                return g_total_channel_list.get(groupType).get(i);
            }
        }
        return null;

    }

    public SimpleChannel get_simple_program_by_hannel_id_from_total_channel_list(long channelId) {

        for (int i = 0; i < g_total_channel_list.size(); i++) {
            for (int j = 0; j < g_total_channel_list.get(i).size(); j++) {
                if (channelId == g_total_channel_list.get(i).get(j).get_channel_id()) {
                    Log.d(TAG, "get_simple_program_by_hannel_id_from_total_channel_list: Name = [" + g_total_channel_list.get(i).get(j).get_channel_name() + "] channelId = [" + channelId + "]");
                    return g_total_channel_list.get(i).get(j);
                }
            }
        }
        return null;

    }

    public List<SimpleChannel> get_cur_play_channel_list(int type, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if (g_total_channel_list.get(type) != null) return g_total_channel_list.get(type);
        else return null;
    }

    public int get_cur_play_channel_list_cnt(int type) {//eric lin 20180802 check program exist
        int cnt = 0;
        if (type >= FavGroup.ALL_TV_TYPE && type < FavGroup.ALL_TV_RADIO_TYPE_MAX) {
            cnt = g_total_channel_list.get(type).size();
            //Log.d(TAG, "GetCurPlayChannelListCnt: type=" + type + "   size=" + cnt);
        }
        return cnt;
    }

    public void reset_total_channel_list() {
        if (g_total_channel_list.size() != 0) {
            for (int i = 0; i < g_total_channel_list.size(); i++) {//eric lin 20180802 check program exist
                g_total_channel_list.get(i).clear();
            }
        }
    }

    private SimpleChannel update_net_program_epg_data(int index, SimpleChannel netProgramInfo) {
        PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();
        if (netProgramInfo.get_play_stream_type() == ProgramPlayStreamType.VOD_TYPE) {//VOD
            EPGEvent presentepgEvent = new EPGEvent();
            presentepgEvent.set_event_id(5597 + index);
            presentepgEvent.set_event_name("Vod - " + index);
            presentepgEvent.set_event_type(EPGEvent.EPG_TYPE_PRESENT);

            //Set Current Time
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();

            long tmpCurTime = dtv.get_dtv_date().getTime() - 60000;// date.getTime();
            presentepgEvent.set_start_time(tmpCurTime);
            calendar.add(Calendar.DATE, 7);
            date = calendar.getTime();
            presentepgEvent.set_end_time(date.getTime());
            presentepgEvent.set_duration(date.getTime() - tmpCurTime);

            netProgramInfo.set_present_epg_event(presentepgEvent);
            netProgramInfo.set_short_event("Vod - " + index);
            netProgramInfo.set_detail_info("Vod - " + index);

            EPGEvent followepgEvent = new EPGEvent();
            followepgEvent.set_event_id(5598 + index);
            followepgEvent.set_event_name("Vod - " + index);
            followepgEvent.set_event_type(EPGEvent.EPG_TYPE_FOLLOW);

            netProgramInfo.set_follow_epg_event(followepgEvent);
            netProgramInfo.set_short_event("Vod - " + index);
            netProgramInfo.set_detail_info("Vod - " + index);

        } else//YOUTUBE
        {
            EPGEvent presentepgEvent = new EPGEvent();
            presentepgEvent.set_event_id(5599 + index);
            presentepgEvent.set_event_name("Youtube - " + index);
            presentepgEvent.set_event_type(EPGEvent.EPG_TYPE_PRESENT);

            //Set Current Time
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            long tmpCurTime = dtv.get_dtv_date().getTime();//date.getTime();
            presentepgEvent.set_start_time(tmpCurTime);
            calendar.add(Calendar.DATE, 7);
            date = calendar.getTime();
            presentepgEvent.set_end_time(date.getTime());
            presentepgEvent.set_duration(date.getTime() - tmpCurTime);

            netProgramInfo.set_present_epg_event(presentepgEvent);
            netProgramInfo.set_short_event("Youtube" + index);
            netProgramInfo.set_detail_info("Youtube" + index);

            EPGEvent followepgEvent = new EPGEvent();
            followepgEvent.set_event_id(5600 + index);
            followepgEvent.set_event_name("Youtube - " + index);
            followepgEvent.set_event_type(EPGEvent.EPG_TYPE_FOLLOW);
            netProgramInfo.set_follow_epg_event(followepgEvent);
            netProgramInfo.set_short_event("Youtube - " + index);
            netProgramInfo.set_detail_info("Youtube - " + index);
        }

        return netProgramInfo;
    }

    public void update_cur_play_channel_list(Context context, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        Log.e(TAG, "exce update_cur_play_channel_list Net first");
        if (g_total_channel_list.size() != 0) g_total_channel_list.clear();

        DataManager mDataManager = DataManager.getDataManager(context);
//        DataManager.ProgramDatabase mProgramDatabase = mDataManager.GetProgramDatabase();
        DataManager.NetProgramDatabase mNetProgramDatabase = mDataManager.GetNetProgramDatabase();

        for (int i = 0; i < FavGroup.ALL_TV_RADIO_TYPE_MAX; i++) {
            ////need change to get simple channel list form service to compatible with now version
            List<SimpleChannel> simpleChannelList = get_simple_program_list(i, 0, IncludePVRSkipFlag);//Get ProgramInfo List from Server
//            List<SimpleChannel> simpleChannelList = mProgramDatabase.GetSimpleChannelList(context); //Get ProgramInfo List from ProgramInfo Database
            List<SimpleChannel> netSimpleChannelList = mNetProgramDatabase.GetNetSimpleChannelList(context);
            if (simpleChannelList.size() > 0 && i < FavGroup.ALL_RADIO_TYPE) {//No Considering Radio Channel
                Log.d(TAG, "exce update_cur_play_channel_list: netprogram.Size = [" + netSimpleChannelList.size() + "]");
                for (int j = 0; j < netSimpleChannelList.size(); j++) {
                    simpleChannelList.add(update_net_program_epg_data(j, netSimpleChannelList.get(j)));//add vod/youtube netprogram after program
                }
            }
            g_total_channel_list.add(simpleChannelList);
        }

        for (int ii = 0; ii < g_total_channel_list.size(); ii++)
            for (int j = 0; j < g_total_channel_list.get(ii).size(); j++)
                Log.d(TAG, "exce UpdateCurPlayChannelList: i = [" + ii + "] " + "= [" + g_total_channel_list.get(ii).get(j).get_channel_name() + "] = [" + g_total_channel_list.get(ii).get(j).get_channel_id() + "] = [" + g_total_channel_list.get(ii).get(j).get_play_stream_type() + "] = [" + g_total_channel_list.get(ii).get(j).get_channel_num() + "]");

    }
    //Scoty Add ProgramInfo and NetProgramInfo Get TotalChannelList -e

    public void update_cur_play_channel_list(int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        Log.e(TAG, "update_cur_play_channel_list first");
        if (g_total_channel_list.size() != 0) g_total_channel_list.clear();
        for (int i = 0; i < FavGroup.ALL_TV_RADIO_TYPE_MAX; i++) {
            List<SimpleChannel> simpleChannelList = get_simple_program_list(i, 0, IncludePVRSkipFlag);
            if (simpleChannelList != null) {
                Log.d(TAG, "update_cur_play_channel_list:    TotalChannelList  group " + i + "    size = " + simpleChannelList.size());
                g_total_channel_list.add(simpleChannelList);//Scoty 20180615 recover get simple channel list function
            } else {
                Log.d(TAG, "update_cur_play_channel_list:    TotalChannelList  group " + i + " is NULL !!!!!!!!!!");
                g_total_channel_list.add(new ArrayList<SimpleChannel>()); // Johnny 20210414 add empty list to prevent null pointer exception
            }
        }
    }

    //Scoty 20180809 modify dual pvr rule -s//Scoty 20180615 update TV/Radio TotalChannelList -s
    public void update_pvr_skip_list(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList
    {
        for (int i = 0; i < g_total_channel_list.get(groupType).size(); i++) {
            if ((IncludePVRSkipFlag == 0) || (pvrTpList != null && check_same_tp(groupType, g_total_channel_list.get(groupType).get(i).get_tp_id(), pvrTpList)))//Scoty 20181113 add for dual tuner pvrList
            {
                g_total_channel_list.get(groupType).get(i).set_pvr_skip(0);
            } else {
                g_total_channel_list.get(groupType).get(i).set_pvr_skip(1);
            }
        }
    }

    private SimpleChannel get_play_simple_channel_by_channel_id(int groupType, long channelId) {
        int size = g_total_channel_list.get(groupType).size();
        for (int i = 0; i < size; i++) {
            if (g_total_channel_list.get(groupType).get(i).get_channel_id() == channelId)
                return g_total_channel_list.get(groupType).get(i);
        }
        return null;
    }

    private boolean check_same_tp(int groupType, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList
    {
        if (pvrTpList == null || pvrTpList.size() == 0) return false;

        for (int i = 0; i < pvrTpList.size(); i++) {
            if (tpId == pvrTpList.get(i)) return true;
        }

        return false;
    }
    //Scoty 20180809 modify dual pvr rule -e//Scoty 20180615 update TV/Radio TotalChannelList -e

    public int set_net_stream_info(int GroupType, NetProgramInfo netStreamInfo) {
        for (int i = 0; i < g_total_channel_list.get(GroupType).size(); i++) {
            if (g_total_channel_list.get(GroupType).get(i).get_channel_id() == netStreamInfo.getChannelId()) {
                return 0;
            }
        }

        SimpleChannel tmpNetSimpleChannel = new SimpleChannel();
        tmpNetSimpleChannel.set_channel_id(netStreamInfo.getChannelId());
        tmpNetSimpleChannel.set_channel_name(netStreamInfo.getChannelName());
        tmpNetSimpleChannel.set_channel_num(netStreamInfo.getChannelNum());
        tmpNetSimpleChannel.set_url(netStreamInfo.getVideoUrl());
        tmpNetSimpleChannel.set_user_lock(netStreamInfo.getUserLock());
        tmpNetSimpleChannel.set_pvr_skip(0);
        tmpNetSimpleChannel.set_ca(0);
        tmpNetSimpleChannel.set_play_stream_type(netStreamInfo.getPlayStreamType());//0 : DVB; 1 : VOD; 2: Youtube
        tmpNetSimpleChannel.set_present_epg_event(netStreamInfo.getPresentepgEvent());
        tmpNetSimpleChannel.set_follow_epg_event(netStreamInfo.getFollowepgEvent());
        tmpNetSimpleChannel.set_short_event(netStreamInfo.getShortEvent());
        tmpNetSimpleChannel.set_detail_info(netStreamInfo.getDetailInfo());
        g_total_channel_list.get(GroupType).add(tmpNetSimpleChannel);
        return 1;
    }

    public int reset_program_database(Context context) {
        int ret = 0;
        DataManager mDataManager = DataManager.getDataManager(context);
        DataManager.ProgramDatabase mProgramDatabase = mDataManager.GetProgramDatabase();

        mProgramDatabase.ResetDatabase(context);

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int reset_net_program_database(Context context) {
        int ret = 0;
        DataManager mDataManager = DataManager.getDataManager(context);
        DataManager.NetProgramDatabase mNetProgramDatabase = mDataManager.GetNetProgramDatabase();

        mNetProgramDatabase.ResetDatabase(context);

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }


    /**
     * First Time Get netprogram.ini File and Add to DataBase
     * After Save Complete Rename to netprogram_already_set.ini
     * In order to not to save database again
     *
     * @return isSuccess : Save DataBase Results
     */
    public boolean init_net_program_database(Context context, int tunerType) {
        Log.d(TAG, "exce init_net_program_database: IN");
        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager(context).GetNetProgramDatabase();
        if (mNetProgramDatabase == null)
            Log.e(TAG, "init_net_program_database: mNetProgramDatabase is NULL");

        String VOD_INI_TITLE = "[VOD]", YOUTUBE_INI_TITLE = "[YOUTUBE]", EANABLE_NETPROGRAMS = "[ENABLE_NETPROGRAMS]";
        String VOD_ENABLE = "ENABLE", VOD_TRUE = "TRUE", VOD_PARAMS_FILTER = "==";
        String INI_READ_PATH = "/vendor/etc/dtv_settings/netprogram.ini";
        File readFile = new File(INI_READ_PATH);
        if (!readFile.exists()) {
            Log.d(TAG, "init_net_program_database: netprogram.ini Not Exist!");
            Pvcfg.SetEnableNetworkPrograms(false);
            return false;
        }

        String line;
        List<NetProgramInfo> netProgramInfoList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(readFile);
            BufferedReader breader = new BufferedReader(fileReader);
            while ((line = breader.readLine()) != null) {
                NetProgramInfo tmpNetProgramInfo = new NetProgramInfo();
                Log.d(TAG, "init_net_program_database: get NetProgram Title [" + line + "]");
                if (line.equals(VOD_INI_TITLE) || line.equals(YOUTUBE_INI_TITLE)) {
                    tmpNetProgramInfo = set_net_program_from_ini_file(breader, tunerType);
                    netProgramInfoList.add(tmpNetProgramInfo);
                } else if (line.equals(EANABLE_NETPROGRAMS))//Check Add YOUTUBE & VOD or not
                {
                    line = breader.readLine();
                    String[] EnableNetProgramText = line.split(VOD_PARAMS_FILTER);
                    if (EnableNetProgramText[0].toUpperCase().equals(VOD_ENABLE)) {
                        // Enable or Disable Netprograms
                        Pvcfg.SetEnableNetworkPrograms(EnableNetProgramText[1].toUpperCase().equals(VOD_TRUE));
                    }

                    if (!Pvcfg.IsEnableNetworkPrograms())// No Need to Save Database if false
                        return false;

                }
            }
            breader.close();

        } catch (IOException e) {
            return false;
        }

        //Check NetPrograms are already saved in dB or not, if exist no need to save again (Reboot to open DTVPlayer)
        if (mNetProgramDatabase != null) {
            if (mNetProgramDatabase.GetNetProgramList(context).size() > 0) {
                Log.e(TAG, "init_net_program_database: NetPrograms are already saved, no need to save again");
                return false;
            }
        }

        boolean isSuccess = save_net_program_list(netProgramInfoList, context);
        Log.d(TAG, "init_net_program_database: Save Database isSuccess = [" + isSuccess + "]");

        return isSuccess;

    }

    /**
     * Read netprogram.ini File and Save Params to NetProgramInfo Database
     *
     * @param reader Read netprogram.ini
     * @return NetprogramInfo
     */
    private NetProgramInfo set_net_program_from_ini_file(BufferedReader reader, int tunerType) {
        Log.d(TAG, "exce set_net_program_from_ini_file: ");
        String iniFileInfo;
        String PARAMS_END = "[END]";
        String NET_SERVICE_ID_TAG = "ServiceId";
        String NET_PLAY_STREAM_TYPE_TAG = "PlayStreamType";
        String NET_VIDEO_URL_TAG = "videoUrl";
        String NET_CHANNEL_NAME_TAG = "ChannelName";
        String NET_CHANNEL_NUM_TAG = "ChannelNum";
        NetProgramInfo netStreamInfo = new NetProgramInfo();
        try {
            while (!(iniFileInfo = reader.readLine()).equals(PARAMS_END)) {
                Log.d(TAG, "exce setVodProgramDatabase: get = [" + iniFileInfo + "]");
                String[] textsplit = iniFileInfo.split("==");
                if (textsplit[0].equals(NET_SERVICE_ID_TAG)) {
                    int serviceId = Integer.parseInt(textsplit[1]);
//                    int tpSize = TpInfoGetList(getTunerType()).size();
                    int tpSize = get_tp_list(tunerType, MiscDefine.TpInfo.NONE_SAT_ID, MiscDefine.TpInfo.POS_ALL, MiscDefine.TpInfo.NUM_ALL).size();
                    int channelId = (serviceId << 16) + (tpSize + 1);
                    netStreamInfo.setChannelId(channelId);
                } else if (textsplit[0].equals(NET_PLAY_STREAM_TYPE_TAG)) {
                    netStreamInfo.setPlayStreamType(Integer.parseInt(textsplit[1]));
                } else if (textsplit[0].equals(NET_VIDEO_URL_TAG)) {
                    netStreamInfo.setVideoUrl(textsplit[1]);
                } else if (textsplit[0].equals(NET_CHANNEL_NAME_TAG)) {
                    netStreamInfo.setChannelName(textsplit[1]);
                } else if (textsplit[0].equals(NET_CHANNEL_NUM_TAG)) {
                    netStreamInfo.setChannelNum(Integer.parseInt(textsplit[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return netStreamInfo;
    }

    /**
     * netProgramlist from netprogram.ini
     *
     * @param netProgramlist Saved NetPrograms List
     * @return isSuccess Save DataBase Results
     */
    private boolean save_net_program_list(List<NetProgramInfo> netProgramlist, Context context) {
//        for(int i = 0 ; i < netProgramlist.size() ; i++)
//            Log.d(TAG, "exce SaveNetProgramList: " + netProgramlist.get(i).ToString());

        //Save NetProgramInfo Database
        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager(context).GetNetProgramDatabase();
        boolean isSuccess = mNetProgramDatabase.SaveNetProgramListDatabase(context, netProgramlist); //Clear and Add
        Log.d(TAG, "save_net_program_list: isSuccess = [" + isSuccess + "]");

        return isSuccess;
    }

//////////////////////////////////////////////////////////////////////////

    /**
     * //     * After Search complete, add VOD & YOUTUBE Programs to TotalChannelList
     * //     * @param groupType
     * //
     */
//    private void AddNetProgramToTotalChannelList(int groupType)
//    {
//        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager().GetNetProgramDatabase();
//        List<NetProgramInfo> netprogramList = mNetProgramDatabase.GetNetProgramList(mContext);
//        for(int i = 0 ; i < netprogramList.size() ; i++)
//            SetNetStreamInfo(groupType, netprogramList.get(i));
//    }
    public int save_table(EnTableType tableType) {
        Log.d(TAG, "save_table:" + tableType.ordinal());
        return PrimeDtvMediaPlayer.excute_command(CMD_PM_SaveTable, tableType.ordinal(), 0);
    }

    public int clear_table(EnTableType tableType) {
        Log.d(TAG, "clear_table : " + tableType.ordinal());
        return PrimeDtvMediaPlayer.excute_command(CMD_PM_ClearTable, tableType.ordinal());
    }

    public int restore_table(EnTableType tableType) {
        Log.d(TAG, "restore_table :" + tableType.ordinal());
        return PrimeDtvMediaPlayer.excute_command(CMD_PM_RestoreTable, tableType.ordinal());
    }

    public int save_networks() {
        return save_table(EnTableType.ALL);
    }

    public int get_default_open_group_type() {
        int ret = PrimeDtvMediaPlayer.excute_command_getII(CMD_PM_GetDefaultOpenGroupType);
        Log.d(TAG, "get_default_open_group_type ret = " + ret);

        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL) {
            return 1;
        } else {
            return PrimeDtvMediaPlayer.get_return_value(ret);
        }
    }

    private int get_channel_num_in_group(int groupType) {
        Log.d(TAG, "get_channel_num_in_group groupType :" + groupType);

        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetChannelNum);
        request.writeInt(groupType);
        request.writeInt(conv_to_server_pos(groupType));

        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            ret = reply.readInt();
            Log.d(TAG, "get_channel_num_in_group groupType :" + groupType + "num : " + ret);
        }
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int get_channel_count(int groupType) {
        final int ATV_GROUP = 372;
        int channelCount = get_channel_num_in_group(/*mGroupType*/groupType);
        Log.v(TAG, "getTestChannelCount() = " + channelCount);
        return channelCount;
    }

    public List<Integer> get_use_groups(EnUseGroupType useGroupType) {
        Log.d(TAG, "get_use_groups = " + useGroupType);

        List<Integer> groupTypes = new ArrayList<Integer>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetUseGroups);
        request.writeInt(useGroupType.ordinal());
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "getUseGroups ret= " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int groupCount = reply.readInt();
            Log.d(TAG, "getUseGroups Count= " + groupCount);
            for (int i = 0; i < groupCount; i++) {
                int type = reply.readInt();
                groupTypes.add(type);
            }
        }

        request.recycle();
        reply.recycle();
        return groupTypes;
    }

    public int rebuild_all_group() {
        Log.v(TAG, "rebuild_all_group");
        return PrimeDtvMediaPlayer.excute_command(CMD_PM_RebuildAllGroup);
    }

    public String get_channel_group_name(int groupType) {
        Log.d(TAG, "get_channel_group_name groupType = " + groupType);
        String groupName = "";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetChannelGroupName);
        request.writeInt(groupType);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            groupName = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return groupName;
    }

    public int set_channel_group_name(int groupType, String name) {
        Log.d(TAG, "set_channel_group_name groupType = " + groupType + "name = " + name);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetChannelGroupName);
        request.writeInt(groupType);
        request.writeString(name);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int del_channel_by_tag(int u32ProgTag) {
        Log.d(TAG, "del_channel_by_tag");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_DelChannelByTag);
        request.writeInt(u32ProgTag);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        if (ret != PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            return PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        }

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public List<SatInfo> sat_info_get_list(int tunerType, int pos, int num) {
        Log.d(TAG, "sat_info_get_list: ");
        return get_deliver_system_list(tunerType, pos, num);
    }

    public SatInfo sat_info_get(int satId) {
        Log.d(TAG, "sat_info_get: ");
        SatInfo Sat = get_deliver_system_by_id(satId);
        return Sat;
    }

    public int sat_info_add(SatInfo pSat) {
        Log.d(TAG, "sat_info_add: ");
        return add_satellite(pSat);
    }

    public int sat_info_update(SatInfo pSat) {
        Log.d(TAG, "sat_info_update: ");
        return edit_satellite(pSat, false);   // change in the future (Sat)
    }

    public int sat_info_update_list(List<SatInfo> pSats) {
        Log.d(TAG, "sat_info_update_list: ");
        return update_satList(pSats);
    }

    public int sat_info_delete(int satId) {
        Log.d(TAG, "sat_info_delete: ");
        return remove_deliver_system(satId);
    }

    private int add_satellite(SatInfo satellite) {
        Log.d(TAG, "add_satellite");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_AddSatellite);
        request.writeInt(0);
        request.writeString(satellite.getSatName());

        request.writeInt(satellite.Antenna.getLnbType());
        request.writeInt(satellite.Antenna.getLnb1());
        request.writeInt(satellite.Antenna.getLnb2());
        request.writeInt(satellite.Antenna.getCku());
        request.writeInt(0);

        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(satellite.Antenna.getTone22k());
        request.writeInt(satellite.Antenna.getDiseqcType());

        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(calc_longitude_from_angle(satellite.getAngle(), satellite.getAngleEW()));
        request.writeInt(satellite.getPostionIndex());


        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            satellite.setSatId(reply.readInt());
            Log.d(TAG, "add_satellite satID = " + satellite.getSatId());

            // edit ant when add sat
            int editAntRet = edit_antenna(satellite);
            Log.d(TAG, "add_satellite: editAntRet = " + editAntRet);
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int edit_satellite(SatInfo satInfo, boolean isSelected) {
        Log.d(TAG, "edit_satellite");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetDeliveryExtern);
        request.writeInt(EnNetworkType.SATELLITE.getValue());
        request.writeInt(satInfo.getSatId());
        request.writeInt(calc_longitude_from_angle(satInfo.getAngle(), satInfo.getAngleEW()));
        request.writeInt(/*isSelected ? 1 : 0*/satInfo.Antenna.getDiseqcType());
        request.writeInt(satInfo.getSatName() == null ? 0 : satInfo.getSatName().length());
        request.writeString(satInfo.getSatName());
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        // edit ant when edit sat
        int editAntRet = edit_antenna(satInfo);
        Log.d(TAG, "edit_satellite: editAntRet = " + editAntRet);

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int remove_deliver_system(int satID) {
        Log.d(TAG, "remove_deliver_system");
        return PrimeDtvMediaPlayer.excute_command(CMD_PM_DelSatellite, satID);
    }

    private int get_angle_ew_from_longitude(int longitude) {
        if (longitude > SatInfo.LONGITUDE_VALUE_MAX / 2) {
            return SatInfo.ANGLE_W;
        } else {
            return SatInfo.ANGLE_E;
        }
    }

    private float calc_angle_from_longitude(int longitude) {
        // if longitude > 1800, angle = (3600 - longitude) / 10;
        // else angle = longitude / 10;

        float angle;
        int tmpLongitude = longitude;
        if (tmpLongitude > SatInfo.LONGITUDE_VALUE_MAX / 2) {
            // angle_W
            tmpLongitude = SatInfo.LONGITUDE_VALUE_MAX - tmpLongitude;
        }

        angle = (float) tmpLongitude / SatInfo.LONGITUDE_VALUE_RATE;

        Log.d(TAG, "calc_angle_from_longitude: " + angle);
        return angle;
    }

    // AntInfo is under SatInfo
    private int get_antenna_data(SatInfo satInfo) {
        Log.d(TAG, "get_antenna_data");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSatelliteAntennaInfo);
        request.writeInt(satInfo.getSatId());
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int nRet = reply.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == nRet) {
            Log.d(TAG, "get_antenna_data: success");
            satInfo.Antenna.setLnbType(reply.readInt());
            satInfo.Antenna.setLnb1(reply.readInt());
            satInfo.Antenna.setLnb2(reply.readInt());
            satInfo.Antenna.setCku(reply.readInt());

            // TODO:For sat
            reply.readInt();//stAntennaInfo.stLNBCfg.u32UNIC_SCRNO
            reply.readInt();//stAntennaInfo.stLNBCfg.u32UNICIFFreqMHz
            reply.readInt();//stAntennaInfo.stLNBCfg.enSatPosn

            // TODO : Set Gpos.LnbPower
            reply.readInt();    // change in the future (Ant)

            satInfo.Antenna.setTone22k(reply.readInt());

//            Antenna.EnToneBurstSwitchType swTone = Antenna.EnToneBurstSwitchType.AUTO;
//            Antenna.EnSwitchType sw12V = Antenna.EnSwitchType.NONE;

            int diseqcType = reply.readInt();
            satInfo.Antenna.setDiseqcType(diseqcType);

            // If diseqctype == 1.0, setDiseqc to this
            if (diseqcType == SatInfo.DISEQC_TYPE_1_0) {
                satInfo.Antenna.setDiseqc(reply.readInt());
            } else {
                reply.readInt();
            }

            // If diseqctype == 1.1, setDiseqc to this
            // No diseqctype1.1 now
            reply.readInt();

//            Motor.EnMotorType nMotorType = Motor.EnMotorType.valueOf(reply.readInt());
            reply.readInt();    // change in the future (Ant)

//            satInfo.setAngle(calcAngleFromLongitude(reply.readInt()));
            reply.readInt();    // angle is already set

            satInfo.setPostionIndex(reply.readInt());

            satInfo.Antenna.setTone22kUse(satInfo.Antenna.getTone22k() == 0 ? 0 : 1);
//            satInfo.Antenna.setDiseqcUse(satInfo.Antenna.getDiseqcType() == 0 ? 0 : 1);
        }

        request.recycle();
        reply.recycle();
        return nRet;
    }

    // AntInfo is under SatInfo
    private int edit_antenna(SatInfo satInfo) {
        Log.d(TAG, "editAntenna 22");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteAntennaInfo);
        request.writeInt(satInfo.getSatId());

        request.writeInt(/*lnbData.getLNBTYPE().ordinal()*/satInfo.Antenna.getLnbType());
        request.writeInt(/*lnbData.getLowLO()*/satInfo.Antenna.getLnb1());
        request.writeInt(/*lnbData.getHighLO()*/satInfo.Antenna.getLnb2());
        request.writeInt(/*lnbData.getLNBBAND().ordinal()*/satInfo.Antenna.getCku());

        // TODO:For sat
        request.writeInt(0);//stAntennaInfo.stLNBCfg.u32UNIC_SCRNO
        request.writeInt(0);//stAntennaInfo.stLNBCfg.u32UNICIFFreqMHz
        request.writeInt(0);//stAntennaInfo.stLNBCfg.enSatPosn

        // TODO: Get Gpos.LNBPower
        PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();
        request.writeInt(/*antenna.getLNBPower().ordinal()*/dtv.gpos_info_get().getLnbPower()); // Gpos.LNBPower(will be removed here), change in the future (Ant)

        request.writeInt(/*antenna.get22KSwitch().ordinal()*/satInfo.Antenna.getTone22k());
//        request.writeInt(/*antenna.getToneBurstSwtich().ordinal()*/0);
//        request.writeInt(/*antenna.get12VSwitch().ordinal()*/0);

        request.writeInt(/*antenna.getDiSEqCType().ordinal()*/satInfo.Antenna.getDiseqcType());
        request.writeInt(/*antenna.getDiSEqC10Port().ordinal()*/satInfo.Antenna.getDiseqc());
        request.writeInt(/*antenna.getDiSEqC11Port().ordinal()*/0); // no diseqctype1.1 now
        request.writeInt(/*antenna.getMotorType().ordinal()*/0);    // change in the future (Ant)
        request.writeInt(/*antenna.getLongitude()*/calc_longitude_from_angle(satInfo.getAngle(), satInfo.getAngleEW()));
        request.writeInt(/*antenna.getMotorPositionID()*/satInfo.getPostionIndex());

        /*Log.e(TAG, "getPolarity=" + antenna.getPolarity());
        Log.e(TAG, "getDiSEqC10Port=" + antenna.getDiSEqC10Port());
        Log.e(TAG, "getMotorType=" + antenna.getMotorType());*/

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "editAntenna: Ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private List<SatInfo> get_deliver_system_list(int tunerType, int nPos, int nNum) {
        return get_deliver_system_list(tunerType, nPos, nNum, null);
    }

    private List<SatInfo> get_deliver_system_list(int tunerType, int nPos, int nNum, String networkName) {
        Log.d(TAG, "getDeliverSystemList tunerType= " + tunerType + " nPos=" + nPos + ",nNum=" + nNum);

        List<SatInfo> lstRet = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetDeliverSystemList);
        request.writeInt(/*networkType.getValue()*/tunerType);
        request.writeInt(/*nPos*/0);
        request.writeInt(/*nNum*/SatInfo.MAX_NUM_OF_SAT);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            lstRet = new ArrayList<SatInfo>();
            int nCount = reply.readInt();
            Log.d(TAG, "getDeliverSystemList nCount=" + nCount);
            for (int i = 0; i < nCount; i++) {
                int nID = reply.readInt();
                int ntunerID = reply.readInt();
                int diseqcType = reply.readInt();
                String strName = reply.readString();

                if ((null != networkName) && (!networkName.equalsIgnoreCase(strName))) {
                    continue;
                }

                if (TpInfo.DVBS == tunerType) {
                    int nlongitude = reply.readInt();

                    SatInfo satInfo = new SatInfo();
                    satInfo.setSatId(nID);
                    satInfo.setSatName(strName);
//                    satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                    satInfo.setLocation(0); // change in the future (Sat)
                    satInfo.setAngle(calc_angle_from_longitude(nlongitude));
                    satInfo.setAngleEW(get_angle_ew_from_longitude(nlongitude));
                    satInfo.setTunerType(tunerType);
                    satInfo.Antenna.setDiseqcType(diseqcType);
                    lstRet.add(satInfo);

                    int getAntRet = get_antenna_data(satInfo);    // set Ant, also set Gpos.LnbPower and Sat.PosIndex
                    Log.d(TAG, "getDeliverSystemList: getAntRet = " + getAntRet);
                } else {
                    SatInfo satInfo = new SatInfo();
                    satInfo.setSatId(nID);
                    satInfo.setSatName(strName);
//                    satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                    satInfo.setLocation(0); // change in the future (Sat)
                    satInfo.setAngle(0);    // change in the future (Sat)
                    satInfo.setTunerType(tunerType);
                    lstRet.add(satInfo);

                    int getAntRet = get_antenna_data(satInfo);    // set Ant, also set Sat.PosIndex
                    Log.d(TAG, "getDeliverSystemList: getAntRet = " + getAntRet);
                }
            }
        }
        request.recycle();
        reply.recycle();

        return lstRet;
    }

    private SatInfo get_deliver_system_by_id(int satID) {
        Log.d(TAG, "get_deliver_system_by_id: satID = " + satID);
        SatInfo satInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetDeliveryByID);
        request.writeInt(/*deliverID*/satID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int signalType = reply.readInt();
            int nID = reply.readInt();
            int diseqcType = reply.readInt();
            String strName = reply.readString();

            if (EnNetworkType.SATELLITE == EnNetworkType.valueOf(signalType)) {
                int nlongitude = reply.readInt();
                Log.d(TAG, "get_deliver_system_by_id: nlongitude = " + nlongitude);

                satInfo = new SatInfo();
                satInfo.setSatId(nID);
                satInfo.setSatName(strName);
//                satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                satInfo.setLocation(0); // change in the future (Sat)
                satInfo.setAngle(calc_angle_from_longitude(nlongitude));
                satInfo.setAngleEW(get_angle_ew_from_longitude(nlongitude));
                satInfo.setTunerType(EnNetworkType.SATELLITE.getValue());
                satInfo.Antenna.setDiseqcType(diseqcType);
                get_antenna_data(satInfo);    // set Ant, also set Gpos.LnbPower and Sat.PosIndex
                Log.d(TAG, "get_deliver_system_by_id: SATELLITE ");
            } else {
                satInfo = new SatInfo();
                satInfo.setSatId(nID);
                satInfo.setSatName(strName);
//                satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                satInfo.setLocation(0); // change in the future (Sat)
                satInfo.setAngle(0);    // change in the future (Sat)
                satInfo.setTunerType(EnNetworkType.valueOf(signalType).getValue());
                get_antenna_data(satInfo);    // set Ant, also set Gpos.LnbPower and Sat.PosIndex
                Log.d(TAG, "get_deliver_system_by_id: OTHER ");
            }
        } else {
            Log.e(TAG, "get_deliver_system_by_id fail ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return satInfo;
    }

    public List<TpInfo> tp_info_get_list_by_sat_id(int tunerType, int satId, int pos, int num) {
        Log.d(TAG, "tp_info_get_list_by_sat_id: ");
        return get_tp_list(tunerType, satId, pos, num);
    }

    public TpInfo tp_info_get(int tp_id) {
        Log.d(TAG, "tp_info_get: ");
        return get_tp_by_id(tp_id);
    }

    public int tp_info_add(TpInfo pTp) {
        Log.d(TAG, "tp_info_add: ");
        return add_tp(pTp);
    }

    public int tp_info_update(TpInfo pTp) {
        Log.d(TAG, "tp_info_update: ");
        int ret = -1;
        if (pTp.getTunerType() == TpInfo.DVBC) {
            ret = edit_cable_tp(pTp);
        } else if (pTp.getTunerType() == TpInfo.DVBS) {
            ret = edit_satellite_tp(pTp);
        } else if (pTp.getTunerType() == TpInfo.DVBT) {
            ret = edit_terrestrial_tp(pTp);
        } else if (pTp.getTunerType() == TpInfo.ISDBT) {
            ret = edit_isdbt_tp(pTp);
        }

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int tp_info_update_list(List<TpInfo> pTps) {
        Log.d(TAG, "tp_info_update_list: ");
        return update_tp_list(pTps);
    }

    public int tp_info_delete(int tpId) {
        Log.d(TAG, "tp_info_delete: ");
        return remove_tp_by_id(tpId);
    }

    private List<TpInfo> get_tp_list(int tunerType, int satId, int pos, int num) {
        Log.d(TAG, "get_tp_list");
        List<TpInfo> lstRet = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSatelliteTpList);
//        request.writeInt(networkType.getValue()CABLE);
        request.writeInt(/*tarNetwork.getID()*/satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);
        request.writeInt(/*nPos*/0);
        request.writeInt(/*nNum*/SatInfo.MAX_TP_NUM_IN_ONE_SAT);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            lstRet = new ArrayList<TpInfo>();
            int nCount = reply.readInt();
            Log.d(TAG, "get_tp_list nCount=" + nCount);
            for (int i = 0; i < nCount; i++) {
                int nTPID = reply.readInt();
                int nType = reply.readInt();
                int nFreq = reply.readInt();
                Log.d(TAG, "get_tp_list: " + nType);
                TpInfo tp;
                if (nType == EnNetworkType.SATELLITE.getValue()) {
                    int nRate = reply.readInt();
                    int nPolar = reply.readInt();
                    reply.readInt();

                    tp = new TpInfo(TpInfo.DVBS);
                    tp.setTpId(nTPID);
                    tp.setSatId(satId);
                    tp.setTuner_id(0);
                    tp.SatTp.setPolar(nPolar);
                    tp.SatTp.setSymbol(nRate);
                    tp.SatTp.setFreq(nFreq);
//                    tp.SatTp.setDrot();
//                    tp.SatTp.setFec();
//                    tp.SatTp.setSpect();
//                    tp.SatTp.setOtherData();
                } else if (nType == EnNetworkType.TERRESTRIAL.getValue()) {
                    int nBandWidth = reply.readInt();
                    int nVersion = reply.readInt();

                    tp = new TpInfo(TpInfo.DVBT);
                    tp.setTpId(nTPID);

                    tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);

                    tp.setTuner_id(0);
                    tp.TerrTp.setFreq(nFreq);
                    tp.TerrTp.setBand(nBandWidth);
                } else if (nType == EnNetworkType.ISDB_TER.getValue()) {
                    int nBandWidth = reply.readInt();
                    int nMod = reply.readInt();
                    int nVersion = reply.readInt();

                    tp = new TpInfo(TpInfo.ISDBT);
                    tp.setTpId(nTPID);

                    tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);

                    tp.setTuner_id(0);
                    tp.TerrTp.setFreq(nFreq);
                    tp.TerrTp.setBand(nBandWidth);
                } else    // CABLE
                {
                    int nRate = reply.readInt();
                    int nMod = reply.readInt();
//                    EnVersionType version = EnVersionType.Version_1;

                    tp = new TpInfo(TpInfo.DVBC);
                    tp.setTpId(nTPID);

                    tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);

                    tp.setTuner_id(0);
                    tp.CableTp.setFreq(nFreq);
                    tp.CableTp.setSymbol(nRate);
                    tp.CableTp.setQam(nMod);
//                    tp.CableTp.setChannel();
//                    tp.CableTp.setOtherData();
                }

                lstRet.add(tp);
            }
        }

        Log.d(TAG, "get_tp_list read");
        request.recycle();
        reply.recycle();
        return lstRet;
    }

    private TpInfo get_tp_by_id(int tpID) {
        Log.d(TAG, "get_tp_by_id: tpID = " + tpID);
        TpInfo tp = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetTpByID);
        request.writeInt(tpID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int nSignalType = reply.readInt();
            int nTPID = reply.readInt();
            int nSID = reply.readInt();
            int nFreq = reply.readInt();
            int nRate = reply.readInt();
            int polar = reply.readInt();
            int version = reply.readInt();
            int nBandWidth = reply.readInt();
            int enQAMValue = reply.readInt();

            if (EnNetworkType.SATELLITE == EnNetworkType.valueOf(nSignalType)) {
                tp = new TpInfo(TpInfo.DVBS);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.SatTp.setPolar(polar);
                tp.SatTp.setSymbol(nRate);
                tp.SatTp.setFreq(nFreq);
            } else if (EnNetworkType.TERRESTRIAL == EnNetworkType.valueOf(nSignalType)) {
                tp = new TpInfo(TpInfo.DVBT);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.TerrTp.setFreq(nFreq);
                tp.TerrTp.setBand(nBandWidth);
            } else if (EnNetworkType.ISDB_TER == EnNetworkType.valueOf(nSignalType)) {
                tp = new TpInfo(TpInfo.ISDBT);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.TerrTp.setFreq(nFreq);
                tp.TerrTp.setBand(nBandWidth);
            } else    // CABLE
            {
                tp = new TpInfo(TpInfo.DVBC);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.CableTp.setFreq(nFreq);
                tp.CableTp.setSymbol(nRate);
                tp.CableTp.setQam(enQAMValue);
//                    tp.CableTp.setChannel();
//                    tp.CableTp.setOtherData();
            }
        } else {
            Log.e(TAG, "get_tp_by_id fail nRet = " + ret);
        }

        request.recycle();
        reply.recycle();
        return tp;
    }

    private int add_tp(TpInfo tpInfo) {
        Log.d(TAG, "add_tp");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SatelliteAddTp);

        request.writeInt(/*nDeliverID*/tpInfo.getSatId());
        request.writeInt(/*networkType.getValue()*/tpInfo.getTunerType());

        int tunerType = tpInfo.getTunerType();

        if (TpInfo.DVBT == tunerType) {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.TerrTp.getFreq());
            request.writeInt(/*tempTP.getBandWidth()*/tpInfo.TerrTp.getBand());
            request.writeInt(/*tempTP.getModulation().ordinal()*/0);    // ignore
            request.writeInt(/*tempTP.getVersion().ordinal()*/0);
        } else if (TpInfo.ISDBT == tunerType) {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.TerrTp.getFreq());
            request.writeInt(/*tempTP.getBandWidth()*/tpInfo.TerrTp.getBand());
            request.writeInt(/*tempTP.getModulation().ordinal()*/0);    // ignore
            request.writeInt(/*tempTP.getVersion().ordinal()*/0);
        } else if (TpInfo.DVBS == tunerType) {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.SatTp.getFreq());
            request.writeInt(/*tempTP.getSymbolRate()*/tpInfo.SatTp.getSymbol());
            request.writeInt(/*tempTP.getPolarity().ordinal()*/tpInfo.SatTp.getPolar());
            request.writeInt(0);
        } else    // CABLE
        {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.CableTp.getFreq());
            request.writeInt(/*tempTP.getSymbolRate()*/tpInfo.CableTp.getSymbol());
            request.writeInt(/*tempTP.getModulation().ordinal()*/tpInfo.CableTp.getQam());
            request.writeInt(0);
        }

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            tpInfo.setTpId(reply.readInt());
            Log.d(TAG, "add_tp: tpid = " + tpInfo.getTpId());
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int edit_cable_tp(TpInfo tpInfo) {
        Log.d(TAG, "edit_cable_tp");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(/*EnNetworkType.CABLE.getValue()*/EnNetworkType.CABLE.getValue());
        request.writeInt(/*nFreq*/tpInfo.CableTp.getFreq());
        request.writeInt(/*nRate*/tpInfo.CableTp.getSymbol());
        request.writeInt(/*nMod*/tpInfo.CableTp.getQam());
        request.writeInt(0);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "edit_cable_tp: ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int edit_isdbt_tp(TpInfo tpInfo) {
        Log.d(TAG, "edit_isdbt_tp");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(EnNetworkType.ISDB_TER.getValue());
        request.writeInt(/*nFreq*/tpInfo.TerrTp.getFreq());
        request.writeInt(/*nBandWidth*/tpInfo.TerrTp.getBand());
        request.writeInt(/*nMod*/0);    // ignore
        request.writeInt(/*nVer*/0);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int edit_satellite_tp(TpInfo tpInfo) {
        Log.d(TAG, "edit_satellite_tp");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*nFreq*/tpInfo.SatTp.getFreq());
        request.writeInt(/*nRate*/tpInfo.SatTp.getSymbol());
        request.writeInt(/*nPol*/tpInfo.SatTp.getPolar());
        request.writeInt(0);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int edit_terrestrial_tp(TpInfo tpInfo) {
        Log.d(TAG, "edit_terrestrial_tp");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(EnNetworkType.TERRESTRIAL.getValue());
        request.writeInt(/*nFreq*/tpInfo.TerrTp.getFreq());
        request.writeInt(/*nBandWidth*/tpInfo.TerrTp.getBand());
        request.writeInt(/*nMod*/0);    // ignore
        request.writeInt(/*nVer*/0);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int remove_tp_by_id(int tpID) {
        Log.d(TAG, "remove_tp_by_id");
        int ret = PrimeDtvMediaPlayer.excute_command(CMD_PM_DelSatelliteTp, tpID);

        Log.d(TAG, "remove_tp_by_id: ret = " + ret);
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public FavInfo fav_info_get(int favMode, int index) {
        Log.d(TAG, "fav_info_get favMode : " + favMode + " index : " + index);
        int ret;
        FavInfo favInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetGroupChannel);
        request.writeInt(favMode);
        request.writeInt(index);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int favNum = reply.readInt();
            long channelID = PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt());
            favInfo = new FavInfo(favNum, channelID, favMode);
        }
        request.recycle();
        reply.recycle();
        return favInfo;
    }

    public List<FavInfo> fav_info_get_list(int favMode) {
        Log.d(TAG, "fav_info_get_list favMode : " + favMode);
        int ret;
        List<FavInfo> favInfoList = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetGroupChannelList);
        request.writeInt(favMode);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            favInfoList = new ArrayList<>();
            int favMaxNum = reply.readInt();
            if (favMaxNum > 0) {
                for (int i = 0; i < favMaxNum; i++) {
                    int favNum = reply.readInt();
                    long channelID = PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt());
                    FavInfo favInfo = new FavInfo(favNum, channelID, favMode);
                    favInfoList.add(favInfo);
                }
            }
        }
        request.recycle();
        reply.recycle();
        return favInfoList;
    }

    public int fav_info_delete(int favMode, long channelId) {
        Log.d(TAG, "fav_info_delete favMode : " + favMode + " channelId : " + channelId);
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_DelGroupChannel);
        request.writeInt(favMode);
        request.writeInt((int) channelId);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int fav_info_delete_all(int favMode) {
        Log.d(TAG, "fav_info_delete_all favMode : " + favMode);
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_DelGroupAll);
        request.writeInt(favMode);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public String fav_group_name_get(int favMode) {
        int ret;
        String strName = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetGroupName);
        request.writeInt(favMode);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            strName = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return strName;
    }

    public int fav_group_name_update(int favMode, String name) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SaveGroupName);
        request.writeInt(favMode);
        request.writeString(name);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public List<SimpleChannel> get_channel_list_by_filter(int filterTag, int serviceType, String keyword, int IncludeSkip, int IncludePvrSkip)//Scoty 20181109 modify for skip channel
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetChannelFilter);
        request.writeInt(filterTag);
        request.writeInt(serviceType);
        request.writeString(keyword);
        request.writeInt(IncludeSkip);
        request.writeInt(IncludePvrSkip);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        List<SimpleChannel> channelList = new ArrayList<>();
        SimpleChannel simpleChannel;

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int count = reply.readInt();
            for (int i = 0; i < count; i++) {
                simpleChannel = new SimpleChannel();
                simpleChannel.set_channel_id(reply.readInt());
                simpleChannel.set_channel_num(reply.readInt());
                simpleChannel.set_channel_name(reply.readString());
                channelList.add(simpleChannel);
            }
        }

        request.recycle();
        reply.recycle();

        return channelList;
    }
}
