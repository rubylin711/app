package com.prime.dtvplayer.Sysdata;

import android.content.Context;
import android.util.Log;

import com.prime.dtvplayer.Activity.DTVActivity;

import java.util.List;

/**
 * Created by gary_hsu on 2018/1/8.
 */

public class ChannelHistory {
    private static final String TAG = "ChannelHistory";
    private int PlayId = 0;
    private int CurGroupType = -1;
    private int PreGroupType = -1;
    private int PreChannelIndex = -1;
    private List<SimpleChannel> PreChannelList = null;
    private int CurChannelIndex = -1;
    private List<SimpleChannel> CurChannelList = null;
    private long TvChannelId = 0;//Scoty 20180810 change save Tv/Radio channel by channelId//for set tv/radio mode
    private long RadioChannelId = 0;//Scoty 20180810 change save Tv/Radio channel by channelId//for set tv/radio mode
    private int PipChannelIndex = -1;//Scoty 20180712 for pip

    private static ChannelHistory History = new ChannelHistory();

    //public ChannelHistory() {
    //    Log.d("ChannelHistory", "ChannelHistory");
    //}

    public static ChannelHistory GetInstance() {
        return History;
    }

    public void UpdateCurChanelList(List<SimpleChannel> pChannelList)//Scoty 20180613 change get simplechannel list for PvrSkip rule
    {
        CurChannelList = pChannelList;
    }

    public static void Reset(){
        History.PlayId = 0;
        History.CurGroupType = -1;
        History.PreGroupType = -1;
        History.PreChannelIndex = -1;
        History.PreChannelList = null;
        History.CurChannelIndex = -1;
        History.CurChannelList = null;
        History.TvChannelId = 0;//Scoty 20180810 change save Tv/Radio channel by channelId
        History.RadioChannelId = 0;//Scoty 20180810 change save Tv/Radio channel by channelId
        History.PipChannelIndex = -1;//Scoty 20180712 for pip
    }

    public void SetCurChannel(SimpleChannel pChannel,List<SimpleChannel> pChannelList,int pCurGroupType) {
        int ChannelIndex = -1;
//        if(pChannel != null)
//            Log.d(TAG, "SetCurChannel pChannel is not null channelId = " + pChannel.getChannelId());

        if(pChannelList != null ) {
//            Log.d(TAG, "SetCurChannel pChannelList != null");
//            Log.d(TAG, "SetCurChannel pChannelList.size() = " + pChannelList.size());
            if(pChannel == null) {
                pChannel = getCurChannel();
            }
//            Log.d(TAG, "SetCurChannel: PreGroupType = " + PreGroupType +" CurGroupType = " + CurGroupType);
//            Log.d(TAG, "SetCurChannel: CurGroupType = " + CurGroupType +" pCurGroupType = " + pCurGroupType);
            PreGroupType = CurGroupType;
            CurGroupType = pCurGroupType;
            PreChannelList = CurChannelList;
            CurChannelList = pChannelList;
        }
        else {
            PreGroupType = CurGroupType;
            PreChannelList = CurChannelList;

            if(pCurGroupType == -1) {//delete all program should reset CurChannelList
                CurChannelList = pChannelList;
                CurChannelIndex = ChannelIndex;
                CurGroupType = -1;
            }
        }
        if(pChannel != null && CurChannelList != null) {
//            Log.d(TAG, "SetCurChannel pChannel != null");
//            Log.d(TAG, "SetCurChannel pChannel name = " + pChannel.getChannelName());
            for (int i = 0; i < CurChannelList.size(); i++) {
//                Log.d(TAG, "SetCurChannel: i = " + i + " CurChannelList " + CurChannelList.get(i).getChannelId() + " pChannel " + pChannel.getChannelId());
                if (CurChannelList.get(i).getChannelId() == pChannel.getChannelId()) {
                    ChannelIndex = i;
                }
            }
        }

        if(pChannel != null && CurChannelList != null) {
            if(ChannelIndex == -1)
                ChannelIndex = 0;
            PreChannelIndex = CurChannelIndex;
            CurChannelIndex = ChannelIndex;

            if(pCurGroupType >= ProgramInfo.ALL_TV_TYPE && pCurGroupType < ProgramInfo.ALL_RADIO_TYPE) {
                //Log.d(TAG, "SetCurChannel: TV ChannelIndex = " + ChannelIndex);
                TvChannelId = CurChannelList.get(ChannelIndex).getChannelId();//Scoty 20180810 change save Tv/Radio channel by channelId
            }
            else if(pCurGroupType >= ProgramInfo.ALL_RADIO_TYPE && pCurGroupType < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
                //Log.d(TAG, "SetCurChannel: RADIO ChannelIndex = " + ChannelIndex);
                RadioChannelId = CurChannelList.get(ChannelIndex).getChannelId();//Scoty 20180810 change save Tv/Radio channel by channelId
            }

        }
        //Log.d("DD", "AvControlPlayByChannelId SetCurChannel: ChannelIndex == " + ChannelIndex);
    }
    public long getTvChannelId()//Scoty 20180810 change save Tv/Radio channel by channelId
    {
        return TvChannelId;
    }

    public long getRadioChannelId()//Scoty 20180810 change save Tv/Radio channel by channelId
    {
        return RadioChannelId;
    }

    public void SetPreChannel(SimpleChannel pChannel,List<SimpleChannel> pChannelList,int pCurGroupType) {
        if(pChannel != null) {
            PreChannelList = pChannelList;
            if (PreChannelList != null) {
                for (int i = 0; i < PreChannelList.size(); i++) {
                    if (pChannel != null && pChannel.getChannelId() == PreChannelList.get(i).getChannelId()) {
                        PreChannelIndex = i;
                    }
                }
            }
        }
        else {
            PreChannelList = null;
            PreChannelIndex = -1;
        }
    }

    public List<SimpleChannel> getCurChannelList() {
        return CurChannelList;
    }

    public SimpleChannel getCurChannel() {
        if(CurChannelIndex < 0 || CurChannelList == null || CurChannelList.size() <= CurChannelIndex) {
            return null;
        }
        else {
            return CurChannelList.get(CurChannelIndex);
        }
    }
    //Scoty 20180712 for pip
    public void setCurPipChannel(Context context)
    {
        //Scoty 20180724 modify open pip rule first: record channel, second: previous watched channel, third: next channel -s
        DTVActivity mDtv = (DTVActivity)context;
        int tmp_record_ch = -1, tmp_next_ch = -1;
        boolean resetFlag = true,FindRecordChFlag = false;

        for (int i = CurChannelIndex + 1; i < CurChannelList.size(); i++) {
            if (mDtv.PvrRecordCheck(CurChannelList.get(i).getChannelId()) != -1) {//set Record channel Index
                if (CurChannelList.get(i).getTpId() == CurChannelList.get(CurChannelIndex).getTpId()) {
                    tmp_record_ch = i;
                    FindRecordChFlag = true;
                    break;
                }
            }
            else
            {
                //set Next channel Index
                if ((CurChannelList.get(i).getTpId() == CurChannelList.get(CurChannelIndex).getTpId()) && resetFlag) {
                    tmp_next_ch = i;
                    resetFlag = false;
                }
            }
        }

        if(!FindRecordChFlag){
            for (int i = 0; i < CurChannelIndex + 1; i++){
                if ((mDtv.PvrRecordCheck(CurChannelList.get(i).getChannelId()) != -1)&&(i != CurChannelIndex)){
                    if (CurChannelList.get(i).getTpId() == CurChannelList.get(CurChannelIndex).getTpId()) {
                        tmp_record_ch = i;
                        break;
                    }
                }
                else
                {
                    if (resetFlag)
                    {
                        if (CurChannelList.get(i).getTpId() == CurChannelList.get(CurChannelIndex).getTpId()) {
                            tmp_next_ch = i;
                            resetFlag = false;
                        }
                    }
                }
            }
        }

        if(tmp_record_ch != -1)//set Record channel Index
            PipChannelIndex = tmp_record_ch;
        else
        {
            //set Pre channel Index
            if((PreChannelIndex != -1) && (CurChannelList.get(CurChannelIndex).getTpId() == CurChannelList.get(PreChannelIndex).getTpId()) &&
                    (CurChannelIndex != PreChannelIndex))//Scoty 20180801 fixed can not open PIP
                PipChannelIndex = PreChannelIndex;
            else
                PipChannelIndex = tmp_next_ch;//set next channel Index
        }
        //Scoty 20180724 modify open pip rule first: record channel, second: previous watched channel, third: next channel -e
    }

    public void setCurPipIndex(int index)
    {
        PipChannelIndex = index;
    }

    public SimpleChannel getCurPipChannel(){
        if(PipChannelIndex == -1) {
            return null;
        }
        else {
            return CurChannelList.get(PipChannelIndex);
        }
    }
    //Scoty 20180712 for pip -e
    public List<SimpleChannel> getPreChannelList() {
        return PreChannelList;
    }

    public SimpleChannel getPreChannel() {
        //Log.d("ProgramManagerUpdateHistory", "getPreChannel: ==>> PreChannelIndex == " + PreChannelIndex);
        if(PreChannelIndex != -1)
            return PreChannelList.get(PreChannelIndex);
        else
            return null;
    }

    public int getPreGroupType() {
        return PreGroupType;
    }

    public int getCurGroupType() {
        return CurGroupType;
    }

    public void setCurGroupType(int groupType)
    {
        CurGroupType = groupType;
    }
    //Scoty 20180712 for pip -s
    public void setAvPipExchange()
    {
        int pipIndex = PipChannelIndex;
        PipChannelIndex = CurChannelIndex;
        CurChannelIndex = pipIndex;
    }

    public void setPipChannelUp()
    {
        boolean resetFlag = true;
        long CurChannelChannelId = CurChannelList.get(CurChannelIndex).getChannelId();
        int CurChannelTpId = CurChannelList.get(CurChannelIndex).getTpId();
        if(PipChannelIndex == -1)
            PipChannelIndex = 0;

        if(PipChannelIndex >= (CurChannelList.size()-1))
        {
            PipChannelIndex = 0;
            for (int i = 0; i < CurChannelList.size(); i++)
            {
                if (CurChannelList.get(i).getTpId() == CurChannelTpId &&
                        CurChannelList.get(i).getChannelId() != CurChannelChannelId) {
                    PipChannelIndex = i;
                    break;
                }
            }
        }
        else {
            if((PipChannelIndex + 1) >= CurChannelList.size())
                PipChannelIndex = 0;
            else
                PipChannelIndex += 1;

            for (int i = PipChannelIndex; i < CurChannelList.size(); i++) {
                if (CurChannelList.get(i).getTpId() == CurChannelTpId &&
                        CurChannelList.get(i).getChannelId() != CurChannelChannelId) {
                    PipChannelIndex = i;
                    resetFlag = false;
                    break;
                }
            }
            if(resetFlag) {
                for (int i = 0; i < CurChannelList.size(); i++) {
                    if (CurChannelList.get(i).getTpId() == CurChannelTpId &&
                            CurChannelList.get(i).getChannelId() != CurChannelChannelId) {
                        PipChannelIndex = i;
                        break;
                    }
                }
            }
        }
    }

    public void setPipChannelDown()
    {
        boolean resetFlag = true;
        long CurChannelChannelId = CurChannelList.get(CurChannelIndex).getChannelId();
        int CurChannelTpId = CurChannelList.get(CurChannelIndex).getTpId();
        if(PipChannelIndex == -1)
            PipChannelIndex = 0;

        if(PipChannelIndex <= 0)
        {
            PipChannelIndex = CurChannelList.size() - 1;
            for (int i = (CurChannelList.size()-1); i >= 0; i--)
            {
                if (CurChannelList.get(i).getTpId() == CurChannelTpId &&
                        CurChannelList.get(i).getChannelId() != CurChannelChannelId) {
                    PipChannelIndex = i;
                    break;
                }
            }
        }
        else
        {
            if((PipChannelIndex - 1) < 0)
                PipChannelIndex = CurChannelList.size() - 1;
            else
                PipChannelIndex -= 1;

            for (int i = PipChannelIndex; i >= 0; i--) {
                if (CurChannelList.get(i).getTpId() == CurChannelTpId &&
                        CurChannelList.get(i).getChannelId() != CurChannelChannelId) {
                    PipChannelIndex = i;
                    resetFlag = false;
                    break;
                }
            }
            if(resetFlag) {
                for (int i = (CurChannelList.size()-1); i >= 0; i--)
                {
                    if (CurChannelList.get(i).getTpId() == CurChannelTpId &&
                            CurChannelList.get(i).getChannelId() != CurChannelChannelId) {
                        PipChannelIndex = i;
                        break;
                    }
                }
            }
        }
    }
    //Scoty 20180712 for pip -e
    public void setChannelUp() {
        //Scoty 20180613 add PvrSkip rule -s
        int resetFlag = 0;
        PreChannelIndex = CurChannelIndex;
        if(CurChannelIndex >= (CurChannelList.size()-1)) {
            CurChannelIndex = 0;
            while(CurChannelList.get(CurChannelIndex).getPVRSkip() == 1)
                CurChannelIndex++;
            //Scoty 20180810 change save Tv/Radio channel by channelId -s
            if(CurGroupType >= ProgramInfo.ALL_TV_TYPE && CurGroupType < ProgramInfo.ALL_RADIO_TYPE) {
                TvChannelId = CurChannelList.get(0).getChannelId();
            } else if (CurGroupType >= ProgramInfo.ALL_RADIO_TYPE && CurGroupType < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
                RadioChannelId = CurChannelList.get(0).getChannelId();
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -e
        } else {
            CurChannelIndex++;
            while(CurChannelList.get(CurChannelIndex).getPVRSkip() == 1) {
                CurChannelIndex++;
                if(CurChannelIndex >= (CurChannelList.size()-1))
                {
                    resetFlag = 1;
                    CurChannelIndex = 0;
                    break;
                }
        }
            if(resetFlag == 1) {
                while (CurChannelList.get(CurChannelIndex).getPVRSkip() == 1) {
            CurChannelIndex++;
                }
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -s
            if(CurGroupType >= ProgramInfo.ALL_TV_TYPE && CurGroupType < ProgramInfo.ALL_RADIO_TYPE) {
                TvChannelId = CurChannelList.get(CurChannelIndex).getChannelId();
            } else if (CurGroupType >= ProgramInfo.ALL_RADIO_TYPE && CurGroupType < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
                RadioChannelId = CurChannelList.get(CurChannelIndex).getChannelId();
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -e
        }
        //Scoty 20180613 add PvrSkip rule -e
    }

    public void setChannelDown() {
        //Scoty 20180613 add PvrSkip rule -s
        int resetFlag = 0;
        PreChannelIndex = CurChannelIndex;
        if(CurChannelIndex <= 0) {
            CurChannelIndex = CurChannelList.size() - 1;
            while(CurChannelList.get(CurChannelIndex).getPVRSkip() == 1) {
                CurChannelIndex--;
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -s
            if(CurGroupType >= ProgramInfo.ALL_TV_TYPE && CurGroupType < ProgramInfo.ALL_RADIO_TYPE) {
                TvChannelId = CurChannelList.get(0).getChannelId();
            } else if (CurGroupType >= ProgramInfo.ALL_RADIO_TYPE && CurGroupType < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
                RadioChannelId = CurChannelList.get(0).getChannelId();
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -e
        } else {
            CurChannelIndex--;
            while(CurChannelList.get(CurChannelIndex).getPVRSkip() == 1) {
                CurChannelIndex--;
                if(CurChannelIndex <= 0)
                {
                    resetFlag = 1;
                    CurChannelIndex = CurChannelList.size() - 1;
                    break;
        }
            }
            if(resetFlag == 1) {
                while (CurChannelList.get(CurChannelIndex).getPVRSkip() == 1) {
            CurChannelIndex--;
                }
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -s
            if(CurGroupType >= ProgramInfo.ALL_TV_TYPE && CurGroupType < ProgramInfo.ALL_RADIO_TYPE) {
                TvChannelId = CurChannelList.get(CurChannelIndex).getChannelId();
            } else if (CurGroupType >= ProgramInfo.ALL_RADIO_TYPE && CurGroupType < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
                RadioChannelId = CurChannelList.get(CurChannelIndex).getChannelId();
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -e
        }
        //Scoty 20180613 add PvrSkip rule -e
    }

    public int getCurListPos(long channelId) {
        if(CurChannelList != null) {
            Log.d(TAG, "exce getCurListPos: size = [" + CurChannelList.size() +"]");
            for (int i = 0; i < CurChannelList.size(); i++) {
                if(CurChannelList.get(i).getChannelId() == channelId)
                    return i;
            }
        }
        return -1;
    }
    //Scoty 20180816 add fast change channel -s
    public long getFastPreChId()
    {
        int index = CurChannelIndex;
        boolean resetFlag = false;

        if(CurGroupType >= ProgramInfo.ALL_TV_TYPE && CurGroupType < ProgramInfo.ALL_RADIO_TYPE) {
            if (index <= 0) {
                index = CurChannelList.size() - 1;
                while (CurChannelList.get(index).getPVRSkip() == 1) {
                    index--;
                }
            } else {
                index--;
                while (CurChannelList.get(index).getPVRSkip() == 1) {
                    index--;
                    if (index <= 0) {
                        resetFlag = true;
                        index = CurChannelList.size() - 1;
                        break;
                    }
                }
                if (resetFlag) {
                    while (CurChannelList.get(index).getPVRSkip() == 1) {
                        index--;
                    }
                }
            }
            return CurChannelList.get(index).getChannelId();
        }
        else {//radio
            return 0;
        }
    }

    public long getFastNextChId()
    {
        int index = CurChannelIndex;
        boolean resetFlag = false;
        if(CurGroupType >= ProgramInfo.ALL_TV_TYPE && CurGroupType < ProgramInfo.ALL_RADIO_TYPE) {
            if (index >= (CurChannelList.size() - 1)) {
                index = 0;
                while (CurChannelList.get(index).getPVRSkip() == 1)
                    index++;
            } else {
                index++;
                while (CurChannelList.get(index).getPVRSkip() == 1) {
                    index++;
                    if (index >= (CurChannelList.size() - 1)) {
                        resetFlag = true;
                        index = 0;
                        break;
                    }
                }
                if (resetFlag) {
                    while (CurChannelList.get(index).getPVRSkip() == 1) {
                        index++;
                    }
                }
            }
            return CurChannelList.get(index).getChannelId();
        }
        else{//radio
            return 0;
        }
    }
    //Scoty 20180816 add fast change channel -e
    public int getPlayId() {
        return PlayId;
    }

    public void setPlayId(int playId) {
        PlayId = playId;
    }
}
