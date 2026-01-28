package com.prime.dtvplayer.Sysdata;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prime.dtvplayer.Service.Table.StreamType;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

    public static final int ALL_TV_TYPE = 0;
    public static final int TV_FAV1_TYPE = 1;
    public static final int TV_FAV2_TYPE = 2;
    public static final int TV_FAV3_TYPE = 3;
    public static final int TV_FAV4_TYPE = 4;
    public static final int TV_FAV5_TYPE = 5;
    public static final int TV_FAV6_TYPE = 6;
    public static final int ALL_RADIO_TYPE = 7;
    public static final int RADIO_FAV1_TYPE = 8;
    public static final int RADIO_FAV2_TYPE = 9;
    public static final int ALL_TV_RADIO_TYPE_MAX = 10;

    public static final String CHANNEL_ID = "ChannelId";
    public static final String SERVICE_ID = "ServiceId";
    public static final String TYPE = "Type";
    public static final String DISPLAY_NUM = "DisplayNum";
    public static final String DISPLAY_NAME = "DisplayName";
    public static final String LOCK = "Lock";
    public static final String SKIP = "Skip";
    public static final String CA_FLAG = "CA";
    public static final String TP_ID = "TpId";
    public static final String SAT_ID = "SatId";
    public static final String PCR = "Pcr";
    public static final String TRANSPORT_STREAM_ID = "TransportStreamId";
    public static final String ORIGINAL_NETWORK_ID = "OriginalNetworkId";
    public static final String AUDIO_LR_SELELCTED = "AudioLRSelected";
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

    private long ChannelId;
    private int ServiceId;
    private int Type; //is ALL_TV_TYPE , ALL_RADIO_TYPE
    private int DisplayNum;
    private String DisplayName;
    private int Lock;
    private int Skip;
    private int CA;
    private int TpId;
    private int SatId;
    private int Pcr;
    private int TransportStreamId;
    private int OriginalNetworkId;
    private int AudioLRSelected;
    private int AudioSelected; //切pid要先LR清0
    private int TunerId;
    private int ParentalRating;
    private String ParentCountryCode;
    public VideoInfo pVideo = new VideoInfo();
    public List<AudioInfo> pAudios = new ArrayList<AudioInfo>();
    public List<SubtitleInfo> pSubtitle = new ArrayList<SubtitleInfo>();
    public List<TeletextInfo> pTeletext = new ArrayList<TeletextInfo>();

    public ProgramInfo(){//eric lin add
        this.TunerId = 0;
    }

    public ProgramInfo(ProgramInfo info){//eric lin add
        int i=0;

        this.ChannelId = info.getChannelId();
        this.ServiceId = info.getServiceId();
        this.Type = info.getType();
        this.DisplayNum = info.getDisplayNum();
        this.DisplayName = info.getDisplayName();
        this.Lock = info.getLock();
        this.Skip = info.getSkip();
        this.CA = info.getCA();
        this.TpId = info.getTpId();
        this.SatId = info.getSatId();
        this.Pcr = info.getPcr();
        this.TransportStreamId = info.getTransportStreamId();
        this.OriginalNetworkId = info.getOriginalNetworkId();
        this.AudioLRSelected = info.getAudioLRSelected();
        this.AudioSelected = info.getAudioSelected();
        this.TunerId = info.getTunerId();
        this.pVideo.PID = info.pVideo.getPID();
        this.pVideo.Codec = info.pVideo.getCodec();
        for(i=0; i<info.pAudios.size();i++)
        {
            this.pAudios.add(new ProgramInfo.AudioInfo(info.pAudios.get(i).getPid()
                    , info.pAudios.get(i).getCodec()
                    , info.pAudios.get(i).getLeftIsoLang()
                    , info.pAudios.get(i).getRightIsoLang()));
        }
        for(i=0; i<info.pSubtitle.size();i++)
        {
            this.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                    info.pSubtitle.get(i).getPid()
                    , info.pSubtitle.get(i).getLang()
                    , info.pSubtitle.get(i).getComPageId()
                    , info.pSubtitle.get(i).getAncPageId()));
        }
        for(i=0; i<info.pTeletext.size();i++)
        {
            this.pTeletext.add(new ProgramInfo.TeletextInfo(
                    info.pTeletext.get(i).getPid()
                    , info.pTeletext.get(i).getType()
                    , info.pTeletext.get(i).getLang()
                    , info.pTeletext.get(i).getMagazineNum()
                    , info.pTeletext.get(i).getPageNum()));
        }
    }

    public String ToString(){
        String info = "ServiceId = "+ServiceId+" Type = "+Type+" DisplayNum = "+ DisplayNum +" DisplayName = "+ DisplayName
                +" Lock = "+Lock+" Skip = "+Skip+" CA = "+CA+" TpId = "+TpId+" SatId = "+SatId
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

    public long getChannelId() {
        return ChannelId;
    }

    public void setChannelId(long channelId) {
        ChannelId = channelId;
    }

    public int getServiceId() {
        return ServiceId;
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

    public void update(ProgramInfo programInfo) {
        int i;
        if(this.ChannelId == programInfo.getChannelId()) {
            this.ServiceId = programInfo.getServiceId();
            this.Type = programInfo.getType();
            this.DisplayNum = programInfo.getDisplayNum();
            this.DisplayName = programInfo.getDisplayName();
            this.Lock = programInfo.getLock();
            this.Skip = programInfo.getSkip();
            this.CA = programInfo.getCA();
            this.TpId = programInfo.getTpId();
            this.SatId = programInfo.getSatId();
            this.Pcr = programInfo.getPcr();
            this.TransportStreamId = programInfo.getTransportStreamId();
            this.OriginalNetworkId = programInfo.getOriginalNetworkId();
            this.AudioLRSelected = programInfo.getAudioLRSelected();
            this.AudioSelected = programInfo.getAudioSelected();
            this.TunerId = programInfo.getTunerId();
            this.pVideo.PID = programInfo.pVideo.getPID();
            this.pVideo.Codec = programInfo.pVideo.getCodec();
            this.pVideo.CaInfoList.clear();
            for(CaInfo tmp : programInfo.pVideo.CaInfoList) {
                this.pVideo.CaInfoList.add(tmp);
            }
            for (i = 0; i < programInfo.pAudios.size(); i++) {
                this.pAudios.clear();
                ProgramInfo.AudioInfo audioInfo = new ProgramInfo.AudioInfo(programInfo.pAudios.get(i).getPid()
                        , programInfo.pAudios.get(i).getCodec()
                        , programInfo.pAudios.get(i).getLeftIsoLang()
                        , programInfo.pAudios.get(i).getRightIsoLang());
                for(CaInfo tmp : programInfo.pAudios.get(i).CaInfoList) {
                    audioInfo.CaInfoList.add(tmp);
                }

                this.pAudios.add(audioInfo);
            }
            for (i = 0; i < programInfo.pSubtitle.size(); i++) {
                this.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        programInfo.pSubtitle.get(i).getPid()
                        , programInfo.pSubtitle.get(i).getLang()
                        , programInfo.pSubtitle.get(i).getComPageId()
                        , programInfo.pSubtitle.get(i).getAncPageId()));
            }
            for (i = 0; i < programInfo.pTeletext.size(); i++) {
                this.pTeletext.add(new ProgramInfo.TeletextInfo(
                        programInfo.pTeletext.get(i).getPid()
                        , programInfo.pTeletext.get(i).getType()
                        , programInfo.pTeletext.get(i).getLang()
                        , programInfo.pTeletext.get(i).getMagazineNum()
                        , programInfo.pTeletext.get(i).getPageNum()));
            }
//            this.PlayStreamUrl = programInfo.getPlayStreamUrl();
//            this.PlayStreamType = programInfo.getPlayStreamType();
        }
    }

    public class CaInfo {
        private int CaSystemId;
        private int EcmPid;

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
                mime = null;
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
                mime = null;
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
    }

    public static class SubtitleInfo {
        private int Pid;
        private String Lang ;
        private int ComPageId;
        private int AncPageId;


        public SubtitleInfo(int pid, String lang, int comPageId, int ancPageId) {
            Pid = pid;
            Lang = lang;
            ComPageId = comPageId;
            AncPageId = ancPageId;
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

    public String getJsonStringFromVideoInfo() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(pVideo);
        return jsonString;
    }

    public String getJsonStringFromAudioInfo() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(pAudios);
        return jsonString;
    }

    public String getJsonStringFromSubtitleInfo() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(pSubtitle);
        return jsonString;
    }

    public String getJsonStringFromTeletextInfo() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(pTeletext);
        return jsonString;
    }

    public VideoInfo getVideoInfoFromJsonString(String jsonString) {
        Gson gson = new Gson();
        VideoInfo videoInfo = gson.fromJson(jsonString, VideoInfo.class);
        return videoInfo;
    }

    public List<AudioInfo> getAudioInfoFromJsonString(String jsonString) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<AudioInfo>>() {}.getType();
        List<AudioInfo> audioInfoList = gson.fromJson(jsonString, collectionType);
        return audioInfoList;
    }

    public List<SubtitleInfo> getSubtitleInfoFromJsonString(String jsonString) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<SubtitleInfo>>() {}.getType();
        List<SubtitleInfo> subtitleInfoList = gson.fromJson(jsonString, collectionType);
        return subtitleInfoList;
    }

    public List<TeletextInfo> getTeletextInfoFromJsonString(String jsonString) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<AudioInfo>>() {}.getType();
        List<TeletextInfo> teletextInfoList = gson.fromJson(jsonString, collectionType);
        return teletextInfoList;
    }
}
