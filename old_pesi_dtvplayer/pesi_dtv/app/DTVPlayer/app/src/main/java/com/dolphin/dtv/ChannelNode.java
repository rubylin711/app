package com.dolphin.dtv;

import java.util.Arrays;

public class ChannelNode
{
    private static final int DTV_MAX_SERVICE_NUM = 32;
    public static final int TTX_SUBT_TYPE = 1;
    public static final int TTX_PICTURE_TYPE = 0;
    public long channelID = 0;
    public int deliveryID = 0;
    public int TPID = 0;
    public String OrignalServiceName = "";
    public int origNetworkID = 0;
    public int TSID = 0;
    public int serviceID = 0;
    public int serviceType = 0;
    public int serviceUserType = 0;
    public int AudPid = 0;
    public int VidPid = 0;
    public int PmtPid = 0;
    public int PcrPID = 0;
    /**
     * <0-not ca ;1-has ca <br>
     * CN:0-清流；1-加扰流
     */
    public int bCAMode = 0;

    public int tempFlag = 0;
    /**
     * If has schedule EPG<br>
     * CNcomment:是否有EPG时间信息
     */
    public int HasScheduleEPG = 0;
    /**
     * <If has PF EPG <br>
     * CNcomment:是否有PF信息
     */
    public int HasPFEPG = 0;
    public int u8Reserved = 0;

    public int LCN = 0;

    public int VidType = 0;
    public int AudType = 0;

    public int editTag = 0; // Lock skip Del Move
    public int favorTag = 0;

    /** HI_SVR_PM_PROG_EXTERN_DATA_S */

    public int AudioNum = 0;
    public int AudioIdx = 0;

    public int CANum = 0;
    public int CAIdx = 0;

    public int SubtNum = 0;
    public int SubtIdx = 0;

    public int TTX_SubtNum = 0;
    public int TTX_SubtIndex = 0;

    public int ParentalRating = 0;
    public String ParentCountryCode = "";

    /**
     * The track info<br>
     * CN: 音轨信息.
     */
    public PM_PROG_VOL_TRACK volTrack = new PM_PROG_VOL_TRACK();

    public SERVICE_ESID[] esidAudioStream = new SERVICE_ESID[DTV_MAX_SERVICE_NUM];
    public SERVICE_ESID[] esidSubtitleInfo = new SERVICE_ESID[DTV_MAX_SERVICE_NUM];
    public SERVICE_ESID[] esidTeletext = new SERVICE_ESID[DTV_MAX_SERVICE_NUM];


    public ChannelNode()
    {
        for (int i=0; i<DTV_MAX_SERVICE_NUM; i++)
        {
            esidAudioStream[i] = new SERVICE_ESID();
            esidSubtitleInfo[i] = new SERVICE_ESID();
            esidTeletext[i] = new SERVICE_ESID();
        }
    }

    public String toString()
    {
        String toStr = "";
        toStr += "channelID = " + channelID + " TPID = " + TPID  + "\n";
        toStr += "OrignalServiceName = " + OrignalServiceName + " origNetworkID = " + origNetworkID+ "\n";
        toStr += "TSID = " + TSID + " serviceID = " + serviceID + " serviceType = " + serviceType+ "\n";
        toStr += "serviceUserType = " + serviceUserType + " AudPid = " + AudPid+ "\n";
        toStr += "VidPid = " + VidPid + " PmtPid = " + PmtPid+ "\n";
        toStr += "PcrPID = " + PcrPID + " bCAMode = " + bCAMode+ "\n";
        toStr += "HasScheduleEPG = " + HasScheduleEPG + " HasPFEPG = " + HasPFEPG+ "\n";
        toStr += "u8Reserved = " + u8Reserved + " LCN = " + LCN + " VidType = " + VidType+ "\n";
        toStr += "AudType = " + AudType + " editTag = " + editTag+ "\n";
        toStr += "favorTag = " + favorTag + " AudioNum = " + AudioNum+ "\n";
        toStr += "AudioIdx = " + AudioIdx + " CANum = " + CANum+ "\n";
        toStr += "CAIdx = " + CAIdx + " SubtNum = " + SubtNum+ "\n";
        toStr += "SubtIdx = " + SubtIdx + " TTX_SubtNum = " + TTX_SubtNum+ "\n";
        toStr += "TTX_SubtIndex = " + TTX_SubtIndex + " volTrack = " + volTrack+ "\n";
        toStr += "esidAudioStream = " + Arrays.toString(esidAudioStream) +"\n" + " esidSubtitleInfo = " + Arrays.toString(esidSubtitleInfo)+ "\n";
        toStr += "esidTeletext = " + Arrays.toString(esidTeletext)+ "\n";
        toStr += "ParentalRating = " + ParentalRating + "\n";
        toStr += "ParentCountryCode = " + ParentCountryCode + "\n";
        return toStr;
    }



    public class SERVICE_ESID
    {
        /*Element stream PID
        * DVB Subtitle: subtitle PID
        * TTX:
        * 31 ~ 16     15 ~ 8          7 ~ 0
        *          MagazineNumber    PageNumber
        */
        public int Pid = 0;

        /* Element stream type */
        public int Type = 0;

        /* Three byte of language code */
        // public int[] Langcode = new int[DTV_LANG_CODE_LEN];
        public String szLangCode = "";

        public int AudioType = 0;

        public int TrackMode = 0;

        public String toString()
        {
            String toStr = "";
            toStr += "Pid = " + Pid + " Type = " + Type + "szLangCode =" + szLangCode;

            return toStr;
        }
    }

    public class PM_PROG_VOL_TRACK
    {
        /**
         * Volume <br>
         * CN:音量.
         */
        public int Volume = 0;
        /**
         * Audio Track <br>
         * CN:声道.
         */
        public int AudioChannel = 0;
        /**
         * Audio index<br>
         * CN:音频序号.
         */
        public int AudioIndex = 0;
        /**
         * Reserved <br>
         * CN:保留字段.
         */
        public int Reserved = 0;

        public String toString()
        {
            String toStr = "";
            toStr += "Volume = " + Volume + " AudioChannel = " + AudioChannel;
            toStr += "AudioIndex = " + AudioIndex + " Reserved = " + Reserved;
            return toStr;
        }
    }
}
