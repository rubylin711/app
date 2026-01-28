package com.prime.sysdata;

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

    private int ChannelId;
    private int ServiceId;
    private int Type; //is ALL_TV_TYPE , ALL_RADIO_TYPE
    private int DisplayNum;
    private String DisplayName;
    private int Lock;
    private int Skip;
    private int CA;
    private int TpId;
    private int SatId;
    private int AntId;
    private int Pcr;
    private int TransportStreamId;
    private int OriginalNetworkId;
    private int NetworkId;
    private int AudioLRSelected;
    private int AudioSelected; //切pid要先LR清0
    private int PmtVersion;
    private int TunerId;
    public VideoInfo pVideo = new VideoInfo();
    public List<AudioInfo> pAudios = new ArrayList<AudioInfo>();
    public List<SubtitleInfo> pSubtitle = new ArrayList<SubtitleInfo>();
    public List<TeletextInfo> pTeletext = new ArrayList<TeletextInfo>();

    public ProgramInfo(){//eric lin add

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
        this.AntId = info.getAntId();
        this.Pcr = info.getPcr();
        this.TransportStreamId = info.getTransportStreamId();
        this.OriginalNetworkId = info.getOriginalNetworkId();
        this.NetworkId = info.getNetworkId();
        this.AudioLRSelected = info.getAudioLRSelected();
        this.AudioSelected = info.getAudioSelected();
        this.PmtVersion = info.getPmtVersion();
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
                +" Lock = "+Lock+" Skip = "+Skip+" CA = "+CA+" TpId = "+TpId+" SatId = "+SatId+" AntId = "+AntId
                +" TransportStreamId = "+TransportStreamId+" OriginalNetworkId = "+OriginalNetworkId+" NetworkId = "+NetworkId
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

    public int getChannelId() {
        return ChannelId;
    }

    public void setChannelId(int channelId) {
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

    public int getAntId() {
        return AntId;
    }

    public void setAntId(int antId) {
        AntId = antId;
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

    public int getNetworkId() {
        return NetworkId;
    }

    public void setNetworkId(int networkId) {
        NetworkId = networkId;
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

    public int getPmtVersion() {
        return PmtVersion;
    }

    public void setPmtVersion(int pmtVersion) {
        PmtVersion = pmtVersion;
    }

    public int getTunerId() {
        return TunerId;
    }

    public void setTunerId(int tunerId) {
        TunerId = tunerId;
    }

    public class VideoInfo {
        private int PID;
        private int Codec;

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
    }

    public static class AudioInfo {
        private int Pid;
        private int Codec;
        private String LeftIsoLang;
        private String RightIsoLang;

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
}
