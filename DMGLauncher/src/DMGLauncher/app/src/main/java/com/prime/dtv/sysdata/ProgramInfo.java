package com.prime.dtv.sysdata;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Table.StreamType;
import com.prime.dtv.service.Util.Utils;
import com.prime.dtv.utils.LogUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gary_hsu on 2017/11/9.
 */

public class ProgramInfo {
    private static final String TAG="ProgramInfo";
    public static final int PROGRAM_TV = 0;
    public static final int PROGRAM_RADIO = 1;
    public static final int MAX_NUM_OF_SERVICE = 1000;
    public static final int NUMBER_OF_AUDIO_IN_SIL = 24;

    public static final String CHANNEL_ID = "ChannelId";
    public static final String SERVICE_ID = "ServiceId";
    public static final String TYPE = "Type";
    public static final String DISPLAY_NUM = "DisplayNum";
    public static final String DISPLAY_NAME = "DisplayName";
    public static final String LOCK = "Lock";
    public static final String SKIP = "Skip";
    public static final String PVR_SKIP = "PvrSkip";
    public static final String CA_FLAG = "CA";
    public static final String TP_ID = "TpId";
    public static final String SAT_ID = "SatId";
    public static final String PCR = "Pcr";
    public static final String LOGICAL_CHANNEL_NUMBER = "LCN";
    public static final String TRANSPORT_STREAM_ID = "TransportStreamId";
    public static final String ORIGINAL_NETWORK_ID = "OriginalNetworkId";
    public static final String AUDIO_LR_SELECTED = "AudioLRSelected";
    public static final String AUDIO_SELECTED = "AudioSelected";
    public static final String TUNER_ID = "TunerId";
    public static final String PARENTAL_RATING = "ParentalRating";
    public static final String PARENTAL_COUNTRY_CODE = "ParentCountryCode";
    public static final String VIDEO_INFO_JSON = "VideoInfoJSON";
    public static final String AUDIO_INFO_JSON = "AudioInfoJSON";
    public static final String SUBTITLE_INFO_JSON = "SubtitleInfoJSON";
    public static final String TELETEXT_INFO_JSON = "TeletextInfoJSON";
    public static final String PLAY_STREAM_URL = "PlayStreamUrl";
    public static final String PLAY_STREAM_TYPE = "PlayStreamType";
    public static final String PMT_PID = "PmtPid";
    public static final String PMT_VERSION = "PmtVersion";
    public static final String DELETE_FLAG = "DeleteFlag";
    public static final String CATEGORY_TYPE = "category_type";
    public static final String TIME_LOCK_FLAG = "TimeLockFlag";

    private long ChannelId;
    private int ServiceId;
    private int Type; //is ALL_TV_TYPE , ALL_RADIO_TYPE
    private int DisplayNum;
    private String DisplayName = "";
    private String ChName_eng;
    private String ChName_chi;
    private int Lock;
    private int Skip;
    private int PvrSkip;
    private int CA;
    private int TpId;
    private int SatId;
    private int Pcr;
    private int LCN;
    private int Quality;
    private int TransportStreamId;
    private int OriginalNetworkId;
    private int AudioLRSelected;
    private int AudioSelected;
    private int TunerId;
    private int ParentalRating;
    private String ParentCountryCode;
    public VideoInfo pVideo = new VideoInfo();
    public List<AudioInfo> pAudios = new ArrayList<AudioInfo>();
    public List<SubtitleInfo> pSubtitle = new ArrayList<SubtitleInfo>();
    public List<TeletextInfo> pTeletext = new ArrayList<TeletextInfo>();
    private int pmtPid;
    private int pmtversion;
    private int DeleteFlag;
    private int TimeLockFlag;
    private int AdultFlag;

    private long category_type;
    private List<Integer> service_product = new ArrayList<Integer>();

    public ProgramInfo(){//eric lin add
        this.TunerId = 0;
        this.pmtPid = 0;
        this.pmtversion = 0;
        this.pVideo.PID = 0;
        this.pVideo.Codec = 0;
    }

    public ProgramInfo(ProgramInfo info){//eric lin add
        int i=0;

        this.pmtPid = info.getPmtPid();
        this.pmtversion = info.getPmtversion();
        this.ChannelId = info.getChannelId();
        this.ServiceId = info.getServiceId();
        this.Type = info.getType();
        this.DisplayNum = info.getDisplayNum();
        this.DisplayName = info.getDisplayName();
        this.Lock = info.getLock();
        this.Skip = info.getSkip();
        this.PvrSkip = info.getPvrSkip();
        this.CA = info.getCA();
        this.TpId = info.getTpId();
        this.SatId = info.getSatId();
        this.Pcr = info.getPcr();
        this.LCN = info.getLCN();
        this.Quality = info.getQuality();
        this.TransportStreamId = info.getTransportStreamId();
        this.OriginalNetworkId = info.getOriginalNetworkId();
        this.AudioLRSelected = info.getAudioLRSelected();
        this.AudioSelected = info.getAudioSelected();
        this.TunerId = info.getTunerId();
        this.ChName_eng = info.getChName_eng();
        this.ChName_chi = info.getChName_chi();
        this.category_type = info.getCategory_type();
        this.service_product.clear();
        this.service_product.addAll(info.service_product);
        this.pVideo.PID = info.pVideo.getPID();
        this.pVideo.Codec = info.pVideo.getCodec();
        for(i=0; i<info.pAudios.size();i++)
        {
            AudioInfo audioInfo = new AudioInfo(info.pAudios.get(i).getPid()
                    , info.pAudios.get(i).getCodec()
                    , info.pAudios.get(i).getLeftIsoLang()
                    , info.pAudios.get(i).getRightIsoLang());
            audioInfo.CaInfoList.addAll(info.pAudios.get(i).CaInfoList);

            this.pAudios.add(audioInfo);
        }
        for(i=0; i<info.pSubtitle.size();i++)
        {
            this.pSubtitle.add(new SubtitleInfo(
                    info.pSubtitle.get(i).getType()
                    , info.pSubtitle.get(i).getPid()
                    , info.pSubtitle.get(i).getLang()
                    , info.pSubtitle.get(i).getComPageId()
                    , info.pSubtitle.get(i).getAncPageId()));
        }
        for(i=0; i<info.pTeletext.size();i++)
        {
            this.pTeletext.add(new TeletextInfo(
                    info.pTeletext.get(i).getPid()
                    , info.pTeletext.get(i).getType()
                    , info.pTeletext.get(i).getLang()
                    , info.pTeletext.get(i).getMagazineNum()
                    , info.pTeletext.get(i).getPageNum()));
        }
        for (i = 0; i < 3 ; i++) {
            if (i == 0) {
                this.TimeLockFlag &= ~0x0001;
                this.TimeLockFlag |= info.getTimeLockFlag(i);

            }
            else if (i == 1) {
                this.TimeLockFlag &= ~0x0002;
                this.TimeLockFlag |= (info.getTimeLockFlag(i) << i);
            }
            else if (i == 2) {
                this.TimeLockFlag &= ~0x0004;
                this.TimeLockFlag |= (info.getTimeLockFlag(i) << i);
            }
        }

        this.pVideo.CaInfoList.addAll(info.pVideo.CaInfoList);
    }

    public String ToString(){
        String info = "ServiceId = "+ServiceId+" Type = "+Type+" LCN = "+LCN+" DisplayNum = "+ DisplayNum +" DisplayName = "+ DisplayName
                +" Lock = "+Lock+" Skip = "+Skip+" PvrSkip = "+PvrSkip+" CA = "+CA+" TpId = "+TpId+" SatId = "+SatId
                +" TransportStreamId = "+TransportStreamId+" OriginalNetworkId = "+OriginalNetworkId
                +" AudioLRSelected = "+AudioLRSelected+" AudioLRSelected = "+AudioLRSelected;
        if(pVideo != null){
            info += " Vide Pid = "+pVideo.PID+" Video Codec = "+pVideo.Codec;
        }
        if(pAudios.size() >0){
            AudioInfo pAudio;
            for(int i=0 ; i<pAudios.size() ; i++){
                pAudio = pAudios.get(i);
                info += " Audio Pid"+i+" = "+pAudio.Pid;
                info += " Audio Codec"+i+" = "+pAudio.Codec;
                info += " Audio Lang L"+i+" = "+pAudio.LeftIsoLang;
            }
        }
        return info;
    }

    public int getPmtversion() {
        return pmtversion;
    }
    public void setPmtVersion(int pmt_version) {
        pmtversion = pmt_version;
    }
    public int getPmtPid() {
        return pmtPid;
    }
    public void setPmtPid(int pmt_pid) {
        pmtPid = pmt_pid;
    }

    public long getChannelId() {
        return ChannelId;
    }

    public void setChannelId(long channelId) {
        ChannelId = channelId;
    }

    public int getServiceId() {
        return ServiceId;
    }

    public String getServiceId(int maxLength) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ServiceId);

        //for (int i = stringBuilder.length(); i < maxLength; i++)
        //    stringBuilder.insert(0, "0");

        return stringBuilder.toString();
    }

    public void setServiceId(int serviceId) {
        ServiceId = serviceId;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getDisplayNum() {
        return DisplayNum;
    }

    public String getDisplayNum(int maxLength) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DisplayNum);

        for (int i = stringBuilder.length(); i < maxLength; i++)
            stringBuilder.insert(0, "0");

        return stringBuilder.toString();
    }

    public String getDisplayNameFull() {
        return getDisplayNum(MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM) + " " + getDisplayName();
    }

    public void setDisplayNum(int displayNum) {
        DisplayNum = displayNum;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public int getLock() {
        return Lock;
    }

    public void setLock(int lock) {
        Lock = lock;
    }

    public int getSkip() {
        return Skip;
    }

    public void setSkip(int skip) {
        Skip = skip;
    }

    public int getPvrSkip() {
        return PvrSkip;
    }

    public void setPvrSkip(int skip) {
        PvrSkip = skip;
    }

    public int getCA() {
        return CA;
    }

    public void setCA(int CA) {
        this.CA = CA;
    }

    public int getTpId() {
        return TpId;
    }

    public void setTpId(int tpId) {
        TpId = tpId;
    }

    public int getSatId() {
        return SatId;
    }

    public void setSatId(int satId) {
        SatId = satId;
    }

    public int getPcr() {
        return Pcr;
    }

    public void setPcr(int pcr) {
        Pcr = pcr;
    }

    public int getLCN() {
        return LCN;
    }

    public void setLCN(int lcn) {
        LCN = lcn;
    }

    public int getTransportStreamId() {
        return TransportStreamId;
    }

    public void setTransportStreamId(int transportStreamId) {
        TransportStreamId = transportStreamId;
    }

    public int getOriginalNetworkId() {
        return OriginalNetworkId;
    }

    public void setOriginalNetworkId(int originalNetworkId) {
        OriginalNetworkId = originalNetworkId;
    }

    public int getAudioLRSelected() {
        return AudioLRSelected;
    }

    public void setAudioLRSelected(int audioLRSelected) {
        AudioLRSelected = audioLRSelected;
    }

    public int getAudioSelected() {
        return AudioSelected;
    }

    public void setAudioSelected(int audioSelected) {
        AudioSelected = audioSelected;
    }

    public int getTunerId() {
        return TunerId;
    }

    public void setTunerId(int tunerId) {
        TunerId = tunerId;
    }

    public int getParentalRating() {
        return ParentalRating;
    }

    public void setParentalRating(int parentalRating) {
        ParentalRating = parentalRating;
    }

    public String getParentCountryCode() {
        return ParentCountryCode;
    }

    public void setParentCountryCode(String parentCountryCode) {
        ParentCountryCode = parentCountryCode;
    }

    public int getQuality()
    {
        return Quality;
    }

    public void setQuality(int quality) { Quality=quality; }

    public void update(ProgramInfo programInfo) {
        int i;
        if(this.ChannelId == programInfo.getChannelId()) {
            this.ServiceId = programInfo.getServiceId();
            this.pmtversion = programInfo.getPmtversion();
            this.Type = programInfo.getType();
            this.DisplayNum = programInfo.getDisplayNum();
            this.DisplayName = programInfo.getDisplayName();
            this.Lock = programInfo.getLock();
            this.Skip = programInfo.getSkip();
            this.PvrSkip = programInfo.getPvrSkip();
            this.CA = programInfo.getCA();
            this.TpId = programInfo.getTpId();
            this.SatId = programInfo.getSatId();
            this.Pcr = programInfo.getPcr();
            this.LCN = programInfo.getLCN();
            this.Quality = programInfo.getQuality();
            this.TransportStreamId = programInfo.getTransportStreamId();
            this.OriginalNetworkId = programInfo.getOriginalNetworkId();
            this.AudioLRSelected = programInfo.getAudioLRSelected();
            this.AudioSelected = programInfo.getAudioSelected();
            this.TunerId = programInfo.getTunerId();
            this.pVideo.PID = programInfo.pVideo.getPID();
            this.pVideo.Codec = programInfo.pVideo.getCodec();
            this.pVideo.CaInfoList.clear();
            this.pVideo.CaInfoList.addAll(programInfo.pVideo.CaInfoList);
            this.ChName_eng = programInfo.getChName_eng();
            this.ChName_chi = programInfo.getChName_chi();
            this.category_type = programInfo.getCategory_type();
            this.service_product.clear();
            this.service_product.addAll(programInfo.service_product);

            this.pAudios.clear();
            for (i = 0; i < programInfo.pAudios.size(); i++) {
                AudioInfo audioInfo = new AudioInfo(programInfo.pAudios.get(i).getPid()
                        , programInfo.pAudios.get(i).getCodec()
                        , programInfo.pAudios.get(i).getLeftIsoLang()
                        , programInfo.pAudios.get(i).getRightIsoLang());
                audioInfo.CaInfoList.addAll(programInfo.pAudios.get(i).CaInfoList);

                this.pAudios.add(audioInfo);
            }

            this.pSubtitle.clear();
            for (i = 0; i < programInfo.pSubtitle.size(); i++) {
                this.pSubtitle.add(new SubtitleInfo(
                        programInfo.pSubtitle.get(i).getType()
                        , programInfo.pSubtitle.get(i).getPid()
                        , programInfo.pSubtitle.get(i).getLang()
                        , programInfo.pSubtitle.get(i).getComPageId()
                        , programInfo.pSubtitle.get(i).getAncPageId()));
            }

            this.pTeletext.clear();
            for (i = 0; i < programInfo.pTeletext.size(); i++) {
                this.pTeletext.add(new TeletextInfo(
                        programInfo.pTeletext.get(i).getPid()
                        , programInfo.pTeletext.get(i).getType()
                        , programInfo.pTeletext.get(i).getLang()
                        , programInfo.pTeletext.get(i).getMagazineNum()
                        , programInfo.pTeletext.get(i).getPageNum()));
            }
//            this.PlayStreamUrl = programInfo.getPlayStreamUrl();
//            this.PlayStreamType = programInfo.getPlayStreamType();
            for (i = 0; i < 3 ; i++) {
                if (i == 0) {
                    this.TimeLockFlag &= ~0x0001;
                    this.TimeLockFlag |= programInfo.getTimeLockFlag(i);

                }
                else if (i == 1) {
                    this.TimeLockFlag &= ~0x0002;
                    this.TimeLockFlag |= (programInfo.getTimeLockFlag(i) << i);
                }
                else if (i == 2) {
                    this.TimeLockFlag &= ~0x0004;
                    this.TimeLockFlag |= (programInfo.getTimeLockFlag(i) << i);
                }
            }
        }
    }

    public int getDeleteFlag() {
        return DeleteFlag;
    }

    public void setDeleteFlag(int deleteFlag) {
        DeleteFlag = deleteFlag;
    }

    public long getCategory_type() {
        return category_type;
    }

    public void setCategory_type(long category_type) {
        this.category_type = category_type;
    }

    public String getChName_eng() {
        return ChName_eng;
    }

    public void setChName_eng(String chName_eng) {
        ChName_eng = chName_eng;
    }

    public String getChName_chi() {
        return ChName_chi;
    }

    public void setChName_chi(String chName_chi) {
        ChName_chi = chName_chi;
    }

    public List<Integer> getService_product(){
        return service_product;
    }

    public void setService_product(int product_id){
        service_product.add(product_id);
    }

    public int getTimeLockFlag() {
        return TimeLockFlag;
    }

    public void setTimeLockFlag(int flag) {
        TimeLockFlag = flag;
    }

    public int getTimeLockFlag(int index) {
        if (index == 0)
            return TimeLockFlag & 0x0001;
        else if (index == 1)
            return (TimeLockFlag & 0x0002) >> index;
        else if (index == 2)
            return (TimeLockFlag & 0x0004) >> index;
        else if (index == 3)
            return TimeLockFlag;
        else
            return 0;
    }

    public void setTimeLockFlag(int index, int value) {
        //Log.d(TAG, "setTimeLockFlag: index = " + index + " value = " + value);
        if (index == 0) {
            TimeLockFlag &= ~0x0001;
            TimeLockFlag |= value;
        }
        else if (index == 1) {
            TimeLockFlag &= ~0x0002;
            TimeLockFlag |= (value << index);
        }
        else if (index == 2) {
            TimeLockFlag &= ~0x0004;
            TimeLockFlag |= (value << index);
        }
        else if (index == 3) {
            TimeLockFlag = value;
        }
        else Log.e(TAG, "setTimeLockFlag: index out of bound");
    }

    public int getAdultFlag() {
        if(Pvcfg.getModuleType() == Pvcfg.MODULE_DMG || Pvcfg.getModuleType() == Pvcfg.MODULE_TBC){
            if((((category_type & 0x0040)>>6) == 1)) {
                return 1;
            }
		}
        return 0;
    }

    public void setAdultFlag(int adultFlag) {
        AdultFlag = adultFlag;
    }

    public static class CaInfo {
        private int CaSystemId;
        private int EcmPid;
        private byte[] PrivateDataBytes;

        public int getCaSystemId() {
            return CaSystemId;
        }

        public void setCaSystemId(int caSystemId) {
            CaSystemId = caSystemId;
        }

        public int getEcmPid() {
            return EcmPid;
        }

        public void setEcmPid(int ecmPid) {
            EcmPid = ecmPid;
        }

        public byte[] getPrivateData() {
            return PrivateDataBytes;
        }
        public void setPrivateData(byte[] privateDataBytes) {
            PrivateDataBytes = privateDataBytes;
        }
    }
    
    public class VideoInfo {
        private int PID;
        private int Codec;
        public List<CaInfo> CaInfoList = new ArrayList<>();

        public int getPID() {
            return PID;
        }

        public void setPID(int PID) {
            this.PID = PID;
        }

        public int getCodec() {
            return Codec;
        }

        public void setCodec(int codec) {
            Codec = codec;
        }

        public String getMime() {
            String mime = "video/";
            if(Codec == StreamType.STREAM_MPEG1_VIDEO) {
                mime +="mpeg1";
            }
            else if(Codec == StreamType.STREAM_MPEG2_VIDEO) {
                mime +="mpeg2";
            }
            else if(Codec == StreamType.STREAM_MPEG4_VIDEO) {
                mime +="mp4v-es";
            }
            else if(Codec == StreamType.STREAM_MPEG4_H264_VIDEO) {
                mime +="avc";
            }
            else if(Codec == StreamType.STREAM_HEVC_VIDEO) {
                mime +="hevc";
            }
            else {
                Log.d(TAG,"Codec["+Codec+"] not support google MIME");
                mime +="mpeg1";
//                mime = null;
            }
            return mime;
        }

        public int getEcmPid(int caSystemId) {
            int ecmPid = 0;
            for(CaInfo tmp : CaInfoList) {
                if(caSystemId == tmp.getCaSystemId()) {
                    ecmPid = tmp.getEcmPid();
                    break;
                }
            }
            return ecmPid;
        }

        public int getCaSystemId(int index) {
            if (CaInfoList != null && CaInfoList.size() > index) {
                return CaInfoList.get(index).getCaSystemId();
            }
            else {
                return 0;
            }
        }

        public byte[] getPrivateData(int caSystemId) {
            byte[] privateData = new byte[0];
            for(CaInfo tmp : CaInfoList) {
                if(caSystemId == tmp.getCaSystemId()) {
                    privateData = tmp.getPrivateData();
                    break;
                }
            }

//            Log.d(TAG, "getPrivateData: privatedata = " + Arrays.toString(privateData));
            return privateData;
        }
    }

    public static class AudioInfo {
        private int Pid;
        private int Codec;
        private String LeftIsoLang;
        private String RightIsoLang;
        public List<CaInfo> CaInfoList = new ArrayList<>();

        public AudioInfo(int pid, int codec, String langL, String langR){
            Pid = pid;
            Codec = codec;
            LeftIsoLang = langL;
            RightIsoLang = langR;
        }

        public int getPid() {
            return Pid;
        }

        public void setPid(int Pid) {
            this.Pid = Pid;
        }

        public int getCodec() {
            return Codec;
        }

        public void setCodec(int codec) {
            Codec = codec;
        }

        public String getLeftIsoLang() {
            return LeftIsoLang;
        }

        public void setLeftIsoLang(String leftIsoLang) {
            LeftIsoLang = leftIsoLang;
        }

        public String getRightIsoLang() {
            return RightIsoLang;
        }

        public void setRightIsoLang(String rightIsoLang) {
            RightIsoLang = rightIsoLang;
        }

        public String getMime() {
            String mime = "audio/";
            if(Codec == StreamType.STREAM_MPEG1_AUDIO) {
                mime +="mpeg";
            }
            else if(Codec == StreamType.STREAM_MPEG2_AUDIO) {
                mime +="mpeg-L2";
            }
            else if(Codec == StreamType.STREAM_AC3_AUDIO) {
                mime +="ac3";
            }
            else if(Codec == StreamType.STREAM_DTS_AUDIO) {
                mime +="mp4v-es";
            }
            else if(Codec == StreamType.STREAM_AAC_AUDIO) {
                mime +="mp4a-latm";
            }
//            else if(Codec == StreamType.STREAM_HEAAC_AUDIO) {
//                mime +="hevc";
//            }
//            else if(Codec == StreamType.STREAM_DDPLUS_AUDIO) { //put mantis
//                mime +="ac3";
//            }
            else {
                Log.d(TAG,"Codec["+Codec+"] not support google MIME");
//                mime = null;
                mime +="mpeg";
            }
            return mime;
        }

        public int getEcmPid(int caSystemId) {
            int ecmPid = 0;
            for(CaInfo tmp : CaInfoList) {
                if(caSystemId == tmp.getCaSystemId()) {
                    ecmPid = tmp.getEcmPid();
                    break;
                }
            }
            return ecmPid;
        }

        public int getCaSystemId(int index) {
            if (CaInfoList != null && CaInfoList.size() > index) {
                return CaInfoList.get(index).getCaSystemId();
            }
            else {
                return 0;
            }
        }

        public byte[] getPrivateData(int caSystemId) {
            byte[] privateData = new byte[0];
            for(CaInfo tmp : CaInfoList) {
                if(caSystemId == tmp.getCaSystemId()) {
                    privateData = tmp.getPrivateData();
                    break;
                }
            }

//            Log.d(TAG, "getPrivateData: privatedata = " + Arrays.toString(privateData));
            return privateData;
        }
    }

    public static class SubtitleInfo {
        private int Type;
        private int Pid;
        private String Lang ;
        private int ComPageId;
        private int AncPageId;


        public SubtitleInfo(int type, int pid, String lang, int comPageId, int ancPageId) {
            Type = type;
            Pid = pid;
            Lang = lang;
            ComPageId = comPageId;
            AncPageId = ancPageId;
        }

        public int getType() {
            return Type;
        }

        public void setType(int type) {
            this.Type = type;
        }

        public int getPid() {
            return Pid;
        }

        public void setPid(int pid) {
            Pid = pid;
        }

        public String getLang() {
            return Lang;
        }

        public void setLang(String lang) {
            Lang = lang;
        }

        public int getComPageId() {
            return ComPageId;
        }

        public void setComPageId(int comPageId) {
            ComPageId = comPageId;
        }

        public int getAncPageId() {
            return AncPageId;
        }

        public void setAncPageId(int ancPageId) {
            AncPageId = ancPageId;
        }

        public String toString(){
            String info = "PID= "+getPid()+" Lang= "+getLang()+ " getComPageId=" + getComPageId() + " getAncPageId=" +getAncPageId();
            Log.d(TAG, info);
            return info;
        }
    }

    public static class TeletextInfo {
        private int Pid;
        private int Type;
        private String Lang;
        private int MagazineNum;
        private int PageNum;

        public TeletextInfo(int pid,int type, String lang, int magazineNum, int pageNum) {
            Pid = pid;
            Type = type;
            Lang = lang;
            MagazineNum = magazineNum;
            PageNum = pageNum;
        }

        public int getType() {
            return Type;
        }

        public void setType(int type) {
            Type = type;
        }

        public String getLang() {
            return Lang;
        }

        public void setLang(String lang) {
            Lang = lang;
        }

        public int getMagazineNum() {
            return MagazineNum;
        }

        public void setMagazineNum(int magazineNum) {
            MagazineNum = magazineNum;
        }

        public int getPageNum() {
            return PageNum;
        }

        public void setPageNum(int pageNum) {
            PageNum = pageNum;
        }

        public int getPid() {
            return Pid;
        }

        public void setPid(int pid) {
            Pid = pid;
        }
    }

    public static String videoInfoSerialize(VideoInfo videoInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(videoInfo.PID).append(",").append(videoInfo.Codec);

        for (CaInfo ca : videoInfo.CaInfoList) {
            sb.append("|")
                    .append(ca.CaSystemId).append(",")
                    .append(ca.EcmPid).append(",")
                    .append(Utils.bytesToHex(ca.PrivateDataBytes));
        }

        return sb.toString();
    }

    private VideoInfo videoInfoDeserialize(String data) {
        String[] parts = data.split("\\|");

        String[] head = parts[0].split(",", -1);
        VideoInfo info = new VideoInfo();
        info.setPID(Integer.parseInt(head[0]));
        info.setCodec(Integer.parseInt(head[1]));

        for (int i = 1; i < parts.length; i++) {
            String[] caParts = parts[i].split(",", -1);
            if (caParts.length >= 3) {
                CaInfo ca = new CaInfo();
                ca.CaSystemId = Integer.parseInt(caParts[0]);
                ca.EcmPid = Integer.parseInt(caParts[1]);
                ca.PrivateDataBytes = Utils.hexToBytes(caParts[2]);
                info.CaInfoList.add(ca);
            }
        }

        return info;
    }

    public String getJsonStringFromVideoInfo() {
//        Gson gson = new Gson();
        String jsonString = videoInfoSerialize(pVideo);//gson.toJson(pVideo);
        return jsonString;
    }

    public static String audioInfoSerialize(List<AudioInfo> audioInfoList) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < audioInfoList.size(); i++) {
            AudioInfo a = audioInfoList.get(i);

            // 基本欄位
            sb.append(a.getPid()).append(",")
                    .append(a.getCodec()).append(",")
                    .append(a.getLeftIsoLang() == null ? "" : a.getLeftIsoLang()).append(",")
                    .append(a.getRightIsoLang() == null ? "" : a.getRightIsoLang());

            // CaInfoList (optional)
            if (a.CaInfoList != null && !a.CaInfoList.isEmpty()) {
                sb.append("#");
                for (int j = 0; j < a.CaInfoList.size(); j++) {
                    CaInfo ca = a.CaInfoList.get(j);
                    if (j > 0) sb.append(";");
                    sb.append(ca.CaSystemId).append(",")
                            .append(ca.EcmPid).append(",")
                            .append(Utils.bytesToHex(ca.PrivateDataBytes));
                }
            }

            if (i < audioInfoList.size() - 1) sb.append("|");
        }

        return sb.toString();
    }

    private List<AudioInfo> audioInfoDeserialize(String data) {
        List<AudioInfo> result = new ArrayList<>();
        if (data == null || data.isEmpty()) return result;

        String[] entries = data.split("\\|");

        for (String entry : entries) {
            String[] parts = entry.split("#", 2); // [0] 是基本欄位, [1] 是 CaInfo 清單（可選）
            String[] main = parts[0].split(",", -1);
            if (main.length < 4) continue;

            AudioInfo a = new AudioInfo(Integer.parseInt(main[0]),Integer.parseInt(main[1]),main[2],main[3]);

            if (parts.length == 2 && !parts[1].isEmpty()) {
                String[] caItems = parts[1].split(";");
                for (String caStr : caItems) {
                    String[] caParts = caStr.split(",", -1);
                    if (caParts.length >= 3) {
                        CaInfo ca = new CaInfo();
                        ca.CaSystemId = Integer.parseInt(caParts[0]);
                        ca.EcmPid = Integer.parseInt(caParts[1]);
                        ca.PrivateDataBytes = Utils.hexToBytes(caParts[2]);
                        a.CaInfoList.add(ca);
                    }
                }
            }

            result.add(a);
        }

        return result;
    }

    public String getJsonStringFromAudioInfo() {
//        Gson gson = new Gson();
        String jsonString = audioInfoSerialize(pAudios);//gson.toJson(pAudios);
        return jsonString;
    }

    public static String subtitleListSerialize(List<SubtitleInfo> list) {
        if (list == null || list.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            SubtitleInfo s = list.get(i);
            sb.append(s.getType()).append(",");
            sb.append(s.getPid()).append(",");
            sb.append(s.getLang() == null ? "" : s.getLang()).append(",");
            sb.append(s.getComPageId()).append(",");
            sb.append(s.getAncPageId());

            if (i < list.size() - 1) sb.append("|");
        }
        return sb.toString();
    }

    public static List<SubtitleInfo> subtitleListDeserialize(String data) {
        List<SubtitleInfo> result = new ArrayList<>();
        if (data == null || data.isEmpty()) return result;

        String[] entries = data.split("\\|");
        for (String entry : entries) {
            String[] fields = entry.split(",", -1); // 保留空值
            if (fields.length >= 5) {
                SubtitleInfo s = new SubtitleInfo(Integer.parseInt(fields[0]),Integer.parseInt(fields[1]),fields[2],
                        Integer.parseInt(fields[3]),Integer.parseInt(fields[4]));
                result.add(s);
            }
        }
        return result;
    }

    public String getJsonStringFromSubtitleInfo() {
//        Gson gson = new Gson();
        String jsonString = subtitleListSerialize(pSubtitle);//gson.toJson(pSubtitle);
        return jsonString;
    }

    public static String teletextListSerialize(List<TeletextInfo> list) {
        if (list == null || list.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            TeletextInfo t = list.get(i);
            sb.append(t.getPid()).append(",");
            sb.append(t.getType()).append(",");
            sb.append(t.getLang() == null ? "" : t.getLang()).append(",");
            sb.append(t.getMagazineNum()).append(",");
            sb.append(t.getPageNum());

            if (i < list.size() - 1) sb.append("|");
        }
        return sb.toString();
    }

    public static List<TeletextInfo> teletextListDeserialize(String data) {
        List<TeletextInfo> result = new ArrayList<>();
        if (data == null || data.isEmpty()) return result;

        String[] entries = data.split("\\|");
        for (String entry : entries) {
            String[] fields = entry.split(",", -1); // -1 保留空欄位
            if (fields.length >= 5) {
                TeletextInfo t = new TeletextInfo(Integer.parseInt(fields[0]),Integer.parseInt(fields[1]),fields[2],
                        Integer.parseInt(fields[3]),Integer.parseInt(fields[4]));
                result.add(t);
            }
        }
        return result;
    }

    public String getJsonStringFromTeletextInfo() {
//        Gson gson = new Gson();
        String jsonString = teletextListSerialize(pTeletext);//gson.toJson(pTeletext);
        return jsonString;
    }

    public VideoInfo getVideoInfoFromJsonString(String jsonString) {
//        Gson gson = new Gson();
        VideoInfo videoInfo = videoInfoDeserialize(jsonString);//gson.fromJson(jsonString, VideoInfo.class);
        return videoInfo;
    }

    public List<AudioInfo> getAudioInfoFromJsonString(String jsonString) {
//        Gson gson = new Gson();
//        Type collectionType = new TypeToken<List<AudioInfo>>() {}.getType();
        List<AudioInfo> audioInfoList = audioInfoDeserialize(jsonString);//gson.fromJson(jsonString, collectionType);
        return audioInfoList;
    }

    public List<SubtitleInfo> getSubtitleInfoFromJsonString(String jsonString) {
//        Gson gson = new Gson();
//        Type collectionType = new TypeToken<List<SubtitleInfo>>() {}.getType();
        List<SubtitleInfo> subtitleInfoList = subtitleListDeserialize(jsonString);//gson.fromJson(jsonString, collectionType);
        return subtitleInfoList;
    }

    public List<TeletextInfo> getTeletextInfoFromJsonString(String jsonString) {
//        Gson gson = new Gson();
//        Type collectionType = new TypeToken<List<TeletextInfo>>() {}.getType();
        List<TeletextInfo> teletextInfoList = teletextListDeserialize(jsonString);//gson.fromJson(jsonString, collectionType);
        return teletextInfoList;
    }
}
