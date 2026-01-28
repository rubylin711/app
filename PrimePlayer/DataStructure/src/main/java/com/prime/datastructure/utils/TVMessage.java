package com.prime.datastructure.utils;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.TpInfo;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Created by ethan_lin on 2017/10/30.
 */

@ParcelablePlease
public class TVMessage implements Parcelable {
    protected static final String TAG = "TVMessage";
    public Bitmap subtitle_bitmap;

    protected int type;
    protected int flag;

    public static final int FLAG_SCAN = 1;
    public static final int TYPE_SCAN_BEGIN = 1000;
    public static final int TYPE_SCAN_SERCHTP = 1001;
    public static final int TYPE_SCAN_PROCESS = 1002;
    public static final int TYPE_SCAN_SCHEDULE = 1003;
    public static final int TYPE_SCAN_END = 1004;

    public static final int FLAG_EPG = 2;
    public static final int TYPE_EPG_UPDATE = 2001;
    public static final int TYPE_EPG_PF_VERSION_CHANGED = 2002;

    public static final int FLAG_CA = 3;
    public static final int TYPE_CA_MSG_UPDATE = 3001;
    public static final int TYPE_CA_MV_MSG = 3002;//eric lin 20210107 widevine cas
    public static final int TYPE_CA_REFRESS_CAS_SET_CHANNEL = 3003;

    public static final int FLAG_TUNER_LOCK = 4;
    public static final int TYPE_TUNER_LOCK_STATUS = 4001;

    public static final int FLAG_AV = 5;
    public static final int TYPE_AV_LOCK_STATUS = 5001;
    public static final int TYPE_AV_PLAY_STATUS = 5002;
	public static final int TYPE_AV_FRAME_PLAY_STATUS = 5003;
    public static final int TYPE_AV_SUBTITLE_BIMAP = 5004;
    public static final int TYPE_AV_DECODER_ERROR = 5005;
    public static final int TYPE_AV_CLOSED_CAPTION_DATA = 5006;
    public static final int TYPE_AV_CLOSED_CAPTION_ENABLE = 5007;
    public static final int TYPE_AV_FirstFrame = 5008;  //tifcheck
    public static final int TYPE_AV_VideoUnavailable = 5009;  //tifcheck
    public static final int TYPE_AV_PmtAvChange = 5010;  //tifcheck
    public static final int TYPE_AV_DvbSubtileChange = 5011;  //tifcheck
    public static final int TYPE_AV_FCC_VISIBLE = 5012;

    public static final int FLAG_PVR = 6;
    public static final int TYPE_PVR_REC_OVER_FIX = 6001;
    public static final int TYPE_PVR_REC_DISK_FULL = 6002;
    public static final int TYPE_PVR_PLAY_TO_BEGIN = 6003;
    public static final int TYPE_PVR_PLAY_REACH_REC = 6004;
    public static final int TYPE_PVR_PLAY_EOF = 6005;

    public static final int TYPE_PVR_RECORD_START = 6006;//eric lin 20180713 pvr msg,-start
    public static final int TYPE_PVR_RECORD_STOP = 6007;
    public static final int TYPE_PVR_TIMESHIFT_START = 6008;
    public static final int TYPE_PVR_TIMESHIFT_PLAY_START = 6009;
    public static final int TYPE_PVR_TIMESHIFT_STOP = 6010;
    public static final int TYPE_PVR_FILE_PLAY_START = 6011;
    public static final int TYPE_PVR_FILE_PLAY_STOP = 6012;//eric lin 20180713 pvr msg,-end
    public static final int TYPE_PVR_PLAY_PARENTAL_LOCK = 6013; //connie 20180806 for pvr parentalRate
    public static final int TYPE_PVR_HDD_READY = 6014;//Scoty 20180827 add HDD Ready command and callback
    public static final int TYPE_PVR_NOT_SUPPORT = 6015;//Scoty 20180827 add HDD Ready command and callback
    public static final int TYPE_PVR_SUCCESS = 6016;
    public static final int TYPE_PVR_RECORDING_START_ERROR = 6017;
    public static final int TYPE_PVR_RECORDING_START_SUCCESS = 6018;
    public static final int TYPE_PVR_RECORDING_STOP_SUCCESS = 6019;
    public static final int TYPE_PVR_PLAY_ERROR = 6020;
    public static final int TYPE_PVR_PLAYBACK_SUCCESS = 6021;
    public static final int TYPE_PVR_PLAYBACK_STOP_SUCCESS = 6022;
    public static final int TYPE_PVR_PLAY_RESUME_ERROR = 6023;
    public static final int TYPE_PVR_PLAY_RESUME_SUCCESS = 6024;
    public static final int TYPE_PVR_PLAY_PAUSE_ERROR = 6025;
    public static final int TYPE_PVR_PLAY_PAUSE_SUCCESS = 6026;
    public static final int TYPE_PVR_PLAY_FF_ERROR = 6027;
    public static final int TYPE_PVR_PLAY_FF_SUCCESS = 6028;
    public static final int TYPE_PVR_PLAY_RW_ERROR = 6029;
    public static final int TYPE_PVR_PLAY_RW_SUCCESS = 6030;
    public static final int TYPE_PVR_PLAY_SEEK_ERROR = 6031;
    public static final int TYPE_PVR_PLAY_SEEK_SUCCESS = 6032;
    public static final int TYPE_PVR_TIMESHIFT_SUCCESS = 6033;
    public static final int TYPE_PVR_TIMESHIFT_ERROR = 6034;
    public static final int TYPE_PVR_TIMESHIFT_STOP_ERROR = 6035;
    public static final int TYPE_PVR_TIMESHIFT_STOP_SUCCESS = 6036;
    public static final int TYPE_PVR_TIMESHIFT_PLAY_ERROR = 6037;
    public static final int TYPE_PVR_TIMESHIFT_RESUME_ERROR = 6038;
    public static final int TYPE_PVR_TIMESHIFT_RESUME_SUCCESS = 6039;
    public static final int TYPE_PVR_TIMESHIFT_PAUSE_ERROR = 6040;
    public static final int TYPE_PVR_TIMESHIFT_PAUSE_SUCCESS = 6041;
    public static final int TYPE_PVR_TIMESHIFT_FF_ERROR = 6042;
    public static final int TYPE_PVR_TIMESHIFT_FF_SUCCESS = 6043;
    public static final int TYPE_PVR_TIMESHIFT_RW_ERROR = 6044;
    public static final int TYPE_PVR_TIMESHIFT_RW_SUCCESS = 6045;
    public static final int TYPE_PVR_TIMESHIFT_SEEK_ERROR = 6046;
    public static final int TYPE_PVR_TIMESHIFT_SEEK_SUCCESS = 6047;
    public static final int TYPE_PVR_RECORDING_STOP_ERROR = 6048;
    public static final int TYPE_PVR_PLAYBACK_STOP_ERROR = 6049;
    public static final int TYPE_PVR_RECORDING_COMPLETED = 6050;
    public static final int TYPE_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR = 6051;
    public static final int TYPE_PVR_PLAY_CHANGE_AUDIO_TRACK_SUCCESS = 6052;
    public static final int TYPE_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR = 6053;
    public static final int TYPE_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_SUCCESS = 6054;


    public static final int FLAG_VMX = 7;
    public static final int TYPE_VMX_SHOW_MSG = 7001;
    public static final int TYPE_VMX_OTA_START = 7002;
    public static final int TYPE_VMX_OTA_ERR = 7003;
    public static final int TYPE_VMX_WATERMARK = 7004;
    public static final int TYPE_VMX_WATERMARK_CLOSE = 7005;
    public static final int TYPE_VMX_SET_PIN = 7006;
    public static final int TYPE_VMX_IPPV = 7007;
    public static final int TYPE_VMX_CARD_DETECT = 7008;
    public static final int TYPE_VMX_SEARCH = 7009;
    public static final int TYPE_VMX_BCIO_NOTIFY = 7010;

    public static final int FLAG_LOADERDTV = 8;
    public static final int TYPE_LOADERDTV_SERVICE_STATUS = 8001;

    public static final int TYPE_REMOVE_USB_STOP_REC = 9000;//Scoty 20180802 add message when recording and then remove usb not clean rec icon

    public static final int FLAG_PIO = 10;
    public static final int TYPE_PIO_FRONT_PANEL_KEY_CH_DOWN = 10001;
    public static final int TYPE_PIO_FRONT_PANEL_KEY_POWER = 10002;
    public static final int TYPE_PIO_FRONT_PANEL_KEY_CH_UP = 10003;
    public static final int TYPE_PIO_USB_OVERLOAD = 10004;
    public static final int TYPE_PIO_ANTENNA_OVERLOAD = 10005;


    public static final int FLAG_SYSTEM = 11;
    public static final int TYPE_SYSTEM_DQA_AUTO_TEST = 11001;
    public static final int TYPE_SYSTEM_SHOW_ERROR_MESSAGE = 11002;
    public static final int TYPE_SYSTEM_SI_UPDATE_CHANNEL_LIST = 11003;
    public static final int TYPE_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL = 11004;
    public static final int TYPE_SYSTEM_SI_UPDATE_SET_CHANNEL = 11005;
    public static final int TYPE_SYSTEM_TIME_SYNC = 11006;
    public static final int TYPE_SYSTEM_INIT = 11007;
    public static final int TYPE_SYSTEM_CNS_TICKER_READY = 11008;

    public static final int FLAG_MTEST = 12;//Scoty 20190410 add Mtest Pc Tool callback
    public static final int TYPE_MTEST_PCTOOL = 12001;
    public static final int TYPE_MTEST_FRONT_PANEL_KEY_RESET = 12002;
    public static final int TYPE_MTEST_FRONT_PANEL_KEY_WPS = 12003;
    public static final int TYPE_MTEST_SERVICE_DIED = 12004; // edwin 20201214 add HwBinder.DeathRecipient

    public static final int FLAG_TEST = 13;
    public static final int TYPE_CALLBACK_TEST = 13001;

    public static final int FLAG_OTHER = 14;
    public static final int TYPE_ADD_MUSIC_CATEGORY_TO_FAV = 14005;

    public static final int FLAG_BOOK = 15;
    public static final int TYPE_BOOK_RUN_AD = 15001;

    public static final int FLAG_SERIES = 16;
    public static final int TYPE_SERIES_UPDATE = 16001;

    public static final int FLAG_HDD = 17;
    public static final int TYPE_HDD_NO_SPACE = 17001;

    public static final int FLAG_CLOAK = 18;
    public static final int TYPE_CLOAK_ECM_STATUS = 18001;
    public static final int TYPE_CLOAK_SERVICE_STATUS = 18002;
    public static final int TYPE_CLOAK_EMM_STATUS = 18003;
    public static final int TYPE_CLOAK_EXTENDED_PRODUCT_LIST = 18004;
    public static final int TYPE_CLOAK_MONITOR_ECM = 18005;
    public static final int TYPE_CLOAK_MONITOR_EMM = 18006;
    public static final int TYPE_CLOAK_ATTRIB_MSG = 18007;

    public static final int TYPE_CLOAK_MAIL = 18008;
    public static final int TYPE_CLOAK_ATTRIB = 18009;
    public static final int TYPE_CLOAK_FINGERPRINT = 18010;
    public static final int TYPE_CLOAK_SLIDE = 18011;
    public static final int TYPE_CLOAK_EMM_DOWNLOAD = 18012;
    public static final int TYPE_CLOAK_EMM_CHANGEPIN = 18013;
    public static final int TYPE_CLOAK_RESET_IRD = 18014;
    public static final int TYPE_CLOAK_RE_SCAN_SI_RESET_ORDER = 18015;
    public static final int TYPE_CLOAK_RECONNECT_TO_SERVICE = 18016;
    public static final int TYPE_CLOAK_ERASE_SI_RESET = 18017;
    public static final int TYPE_CLOAK_FORCE_TUNE = 18018;
    public static final int TYPE_CLOAK_STATUS_TMS_DATA_CHANGED = 18019;
    public static final int TYPE_CLOAK_STATUS_NATIONALITY_CHANGED = 18020;
    public static final int TYPE_CLOAK_STATUS_FLEXIFLASH_MESSAGE = 18021;
    public static final int TYPE_CLOAK_STATUS_IFCP_IMAGE_MESSAGE = 18022;

    public static final int TYPE_CUSTOMER_CARD_ONOFF = 18023;
    public static final int TYPE_CUSTOMER_DISPLAY_PID = 18024;
    public static final int TYPE_CUSTOMER_SET_INFO_CHANNEL = 18025;
    public static final int TYPE_CUSTOMER_SET_LCN = 18026;

    public static final int FLAG_ACS = 19;
    public static final int TYPE_ACS_SET_NIT_ID_TO_ASC = 19000;

    // --- VBM ---
    public static final int FLAG_VBM = 20;
    public static final int TYPE_VBM_RECORD = 20001;

    // --- OTA ---
    public static final int FLAG_OTA = 21;
    public static final int TYPE_OTA_SHOW_COUNT_DOWN_DIALOG = 21001;
    public static final int TYPE_OTA_SHOW_UPDATE_DIALOG = 21002;

    //Scan process data
    protected int serviceId;
    protected int serviceLCN;
    protected int serviceType;
    protected long channelId;
    protected String serviceName;
    protected int CAFlag;
    protected int AlreadyScanedTpNum;
    protected int serviceCHNum;
    protected int tpId;
    protected int percent;
    //Scan result data
    protected int total_tv;
    protected int total_radio;
    //CA Message
    protected String Error_Code;
    protected String CA_Msg_String;
    //Tuner Lock Status
    protected int TunerLock;
    //AV Lock Status
    protected int AVlockStatus;
    protected int Rating;
    protected int Finish;
    protected int AvFrameStatus;
    protected long AvFrameChannelId;//eric lin 20180803 no video signal
    protected int AvTunerId;
    //PVR
    protected int recId = -1;//eric lin 20180713 pvr msg
    protected int recChannelId;
    protected int pvrMode;
    protected PvrInfo.EnPlaySpeed pvrSpeed;
    protected String pvrCombinedRecIdxStr;

    //VMX
    protected int CAMsgMode;
    protected String CAmsg;
    protected int CAMsgDuration;

    protected int CAWaterMarkMode;
    protected String CAWaterMarkMsg;
    protected int CAWaterMarkDur;

    protected int OTAMode;
    protected int VMXPinOnOff;
    protected long VMXPinChannelID;
    protected int VMXPinIndex;
    protected int VMXPinTextSelector;

    protected long IPPV_ChannelID;
    protected int IPPV_OnOff;
    protected String IPPV_CurToken;
    protected String IPPV_cost;
    protected int IPPV_PinIndex;

    protected int VMX_BcioNotify_type;

    protected int CardDetect;

    protected int VMXSearchMode;
    protected int VMXSearchFreq;
    protected int VMXSearchsym;

    protected int VMXTriggerID;
    protected int VMXTriggerNum;

    //LoaderDtv
    protected int loaderdtv_dLStatus; // 0 is no lock , 1 is lock not service, 2 is service found

    //PIO
    protected int pioUsbOverloadPort;

    //DQA auto test
    protected int dqaAutoTestFlag; // 1 = in auto test, 0 = not in auto test

    //Mtest
    protected int cmdId;//Scoty 20190410 add Mtest Pc Tool callback
    protected int errCode;//Scoty 20190410 add Mtest Pc Tool callback

    //test
    protected int testParam1;
    protected int testParam2;
    protected String testName;

    //widevine 
    //eric lin 20210107 widevine cas, -start
    protected int WvEventId;
    protected byte[] WvMsg;
    protected int sessionMode;
    protected int wv_index;
    protected int es_pid;
    protected int ecm_pid;
    protected widevineMsg WvMsgData;
    //eric lin 20210107 widevine cas, -end
    protected String message;

    //Irdeto Cloak
    protected CloakEcmStatus cloakEcmStatus;
    protected CloakServiceStatus cloakServiceStatus;
    protected CloakEmmStatus cloakEmmStatus;
    protected CloakMonitorEcm cloakMonitorEcm;
    protected CloakMonitorEmm cloakMonitorEmm;
    protected CloakAttribMsg  cloakAttribMsg;
    protected long u32UniIdMail;
    protected long u32UniIdAttrib;
    protected CloakFingerprint cloakFingerprint;
    protected CloakSlide cloakSlide;
    protected CloakEMMDownload EMMDownloadData;
    protected long u32EMMPinCode;
    protected CloakForceTune ForceTuneData;
    protected CloakCustomer_cardon Customer_cardon;
    protected int Customer_display_pid;
    protected int Customer_landing_service;
    protected int Customer_landing_LCN;
    protected int CaSystem;
    protected byte[] ClosedCaptionData;
    protected int ClosedCaptionEnable;
    protected int ClosedCaptionType;

    protected int nit_id;

    //ota
    protected String otaLastVersion;

    public TVMessage()
    {
        type = 0;
        flag = 0;
    }
    public TVMessage(int Flag, int Type) {
        this.type = Type;
        this.flag = Flag;
    }
    /*
        protected TVMessage(Parcel in) {
            readFromParecl(in);
        }

        public static final Creator<TVMessage> CREATOR = new Creator<TVMessage>() {
            @Override
            public TVMessage createFromParcel(Parcel in) {
                return new TVMessage(in);
            }

            @Override
            public TVMessage[] newArray(int size) {
                return new TVMessage[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeInt(flag);
            switch(flag){
                case FLAG_SCAN:{
                    if(type == TYPE_SCAN_PROCESS){
                        dest.writeInt(serviceId);
                        dest.writeInt(serviceType);
                        dest.writeInt(serviceLCN);
                        dest.writeString(serviceName);
                        dest.writeInt(CAFlag);
                        dest.writeInt(AlreadyScanedTpNum);
                    }
                    if(type == TYPE_SCAN_END){
                        dest.writeInt(total_tv);
                        dest.writeInt(total_ratio);
                    }
                }break;
                case FLAG_EPG:{
                    if(type == TYPE_EPG_UPDATE){
                        dest.writeInt(serviceType);
                        dest.writeInt(serviceCHNum);
                    }
                }
            }

        }

        public void readFromParecl(Parcel in){
            type = in.readInt();
            flag = in.readInt();
            switch (flag){
                case FLAG_SCAN:{
                    if(type == TYPE_SCAN_PROCESS){
                        serviceId = in.readInt();
                        serviceType = in.readInt();
                        serviceLCN = in.readInt();
                        serviceName = in.readString();
                        CAFlag = in.readInt();
                        AlreadyScanedTpNum = in.readInt();
                    }
                    if(type == TYPE_SCAN_END){
                        total_tv = in.readInt();
                        total_ratio = in.readInt();
                    }
                }break;
                case FLAG_EPG:{
                    if(type == TYPE_EPG_UPDATE) {
                        serviceType = in.readInt();
                        serviceCHNum = in.readInt();
                    }
                }break;
            }
        }
    */
    public static TVMessage SetVbmRecord(String jsonPayload) {
        TVMessage msg = new TVMessage(FLAG_VBM, TYPE_VBM_RECORD);
        msg.message = (jsonPayload == null) ? "" : jsonPayload;
        return msg;
    }
    public static TVMessage SetScanBegin(){
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_BEGIN);
        return msg;
    }
    public static TVMessage SetScanTP(int TpCount, int TpId){
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_SERCHTP);
        msg.AlreadyScanedTpNum = TpCount;
        msg.tpId = TpId;
        return msg;
    }
    public static TVMessage SetScanResultUpdate(int ServiceId,int serviceType,int lcn , String ServiceName, int CA_Flag){
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_PROCESS);

        msg.serviceId = ServiceId;
        msg.serviceType = serviceType;
        msg.serviceLCN = lcn;
        msg.serviceName = ServiceName;
        msg.CAFlag = CA_Flag;

        return msg;
    }
    public static TVMessage SetScanScheduleUpdate(int percent) {
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_SCHEDULE);
        msg.percent = percent;

        return msg;
    }
    public static TVMessage SetScanEnd(int total_tv, int total_radio){
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_END);
        msg.total_tv = total_tv;
        msg.total_radio = total_radio;
        return msg;
    }

    public static TVMessage SetAddMusicCategoryToFav(){
        return new TVMessage(FLAG_OTHER, TYPE_ADD_MUSIC_CATEGORY_TO_FAV);
    }

    public static TVMessage SetEPGUpdate(long channelId){
        TVMessage msg = new TVMessage(FLAG_EPG, TYPE_EPG_UPDATE);
        //msg.serviceType = serviceType;
        //msg.serviceCHNum = serviceCHNum;
        msg.channelId = channelId;
        return msg;
    }

    public static TVMessage SetEpgPFVersionChanged(long channelId){
        TVMessage msg = new TVMessage(FLAG_EPG, TYPE_EPG_PF_VERSION_CHANGED);
        msg.channelId = channelId;
        return msg;
    }

    public static TVMessage SetCAMsg(int ca_flag , String errorCode , String CA_Msg_String) {
        TVMessage msg = new TVMessage(FLAG_CA, TYPE_CA_MSG_UPDATE);
        msg.CAFlag = ca_flag;
        msg.Error_Code = errorCode;
        msg.CA_Msg_String = CA_Msg_String;
        return msg;
    }

    public static TVMessage SetTunerLockStatus(int lock){
        TVMessage msg = new TVMessage(FLAG_TUNER_LOCK, TYPE_TUNER_LOCK_STATUS);
        msg.TunerLock = lock;
        return msg;
    }

    public static TVMessage SetAVLockStatus(int avLockStatus, long channelId , int rating){
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_LOCK_STATUS);
        Log.d(TAG, "SetAVLockStatus: notifyMessage  ");
        msg.AVlockStatus = avLockStatus;
        msg.channelId = channelId;
        msg.Rating = rating;
        return msg;
    }

    public static TVMessage SetPlayMsg(int finish){
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_PLAY_STATUS);
        msg.Finish = finish;
        return msg;
    }

    public static TVMessage SetDecoderError(){
        TVMessage msg= new TVMessage(FLAG_AV, TYPE_AV_DECODER_ERROR);
        return msg;
    }
    public static TVMessage SetPmtAvChange(){ //tifcheck
        TVMessage msg= new TVMessage(FLAG_AV, TYPE_AV_PmtAvChange);
        return msg;
    }
    public static TVMessage SetPmtDvbSubtileChange(){ //tifcheck
        TVMessage msg= new TVMessage(FLAG_AV, TYPE_AV_DvbSubtileChange);
        return msg;
    }
    public static TVMessage SetFirstFrame(){ //tifcheck
        TVMessage msg= new TVMessage(FLAG_AV, TYPE_AV_FirstFrame);
        return msg;
    }
    public static TVMessage SetFirstFrame(int tunerId, long channelId){ //tifcheck
        TVMessage msg= new TVMessage(FLAG_AV, TYPE_AV_FirstFrame);
        msg.AvTunerId = tunerId;
        msg.channelId = channelId;
        return msg;
    }
    public static TVMessage SetVideoUnavailable(int status){ //tifcheck
        TVMessage msg= new TVMessage(FLAG_AV, TYPE_AV_VideoUnavailable);
        msg.AvFrameStatus=status;
        return msg;
    }
    public static TVMessage SetSubtitleBitmap(Bitmap bitmap){
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_SUBTITLE_BIMAP);
        msg.subtitle_bitmap = bitmap;
        return msg;
    }

    public static TVMessage SetClosedCaptionEnable(int enable, int type){
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_CLOSED_CAPTION_ENABLE);
        msg.ClosedCaptionEnable = enable;
        msg.ClosedCaptionType = type;
        return msg;
    }

    public static TVMessage SetClosedCaptionData(byte[] data){
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_CLOSED_CAPTION_DATA);
        msg.ClosedCaptionData = data;
        return msg;
    }

    public static TVMessage SetVideoStatus(int avFrameStatus, long channelId){//eric lin 20180803 no video signal
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_FRAME_PLAY_STATUS);
        msg.AvFrameStatus = avFrameStatus;
        msg.AvFrameChannelId = channelId;//eric lin 20180803 no video signal
        return msg;
    }
    public static TVMessage SetFCCVisible(int index) {
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_FCC_VISIBLE);
        msg.AvTunerId = index;
        return msg;
    }

    public static TVMessage SetPvrOverFix(int recId,int pvrMode){//Scoty 20180720 add PESI_SVR_EVT_PVR_REC_OVER_FIX send recId
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_REC_OVER_FIX);
        msg.recId = recId;
        msg.pvrMode = pvrMode;
        return msg;
    }

    public static TVMessage SetPvrDISKFULL(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_REC_DISK_FULL);
        return msg;
    }

    public static TVMessage SetPvrPlaytoBegin(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_TO_BEGIN);
        return msg;
    }

    public static TVMessage SetPvrPlayReachRec(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_REACH_REC);
        return msg;
    }

    public static TVMessage SetPvrEOF() {
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_EOF);
        return  msg;
    }

    public static TVMessage SetPvrRecordStart(int param1, int param2, int param3, String recIdxStr){//eric lin 20180713 pvr msg,-start
        /*
        Log.d(TAG, "SetPvrRecordStart: param1="+param1+", param2="+param2);
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORD_START);
        if(param1 == 0)
            msg.recId = param2;
        else
            msg.recId = -1;
        */
        Log.d(TAG, "SetPvrRecordStart: param1="+param1+" param2="+param2);
        TVMessage msg;
        if(param1 == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORDING_START_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORDING_START_ERROR);

        msg.recId=param2;
        msg.recChannelId=param3;
        msg.pvrCombinedRecIdxStr = recIdxStr;

        return msg;
    }

    public static TVMessage SetPvrRecordStop(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORD_STOP);
        return msg;
    }

    public static TVMessage SetPvrRecordStop(int param1,int param2,int param3){
        Log.d(TAG, "SetPvrRecordStop: param1="+param1+" param2="+param2+" param3="+param3);
        TVMessage msg;
        if(param1 == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORDING_STOP_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORDING_STOP_ERROR);
        msg.recId=param2;
        msg.recChannelId=param3;
        return msg;
    }
    public static TVMessage SetPvrRecordCompleted(int recId){
        Log.d(TAG, "SetPvrRecordCompleted: recId="+recId);
        TVMessage msg;
        msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORDING_COMPLETED);
        msg.recId = recId;
        return msg;
    }

    public static TVMessage SetPvrTimeshiftStart(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_START);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftStart(int param){
        Log.d(TAG, "SetPvrTimeshiftStart: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_SUCCESS);
        else if(param == 1)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_PLAY_ERROR);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_ERROR);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftPlayStart(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_PLAY_START);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftStop(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_STOP);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftStop(int param){
        Log.d(TAG, "SetPvrTimeshiftStop: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_STOP_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_STOP_ERROR);
        return msg;
    }

    public static TVMessage SetPvrFilePlayStart(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_FILE_PLAY_START);
        return msg;
    }

    public static TVMessage SetPvrFilePlayStart(int param){
        Log.d(TAG, "SetPvrFilePlayStart: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAYBACK_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_ERROR);
        return msg;
    }

    public static TVMessage SetPvrFilePlayStop(){
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_FILE_PLAY_STOP);
        return msg;
    }

    public static TVMessage SetPvrFilePlayStop(int param){
        Log.d(TAG, "SetPvrFilePlayStop: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAYBACK_STOP_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAYBACK_STOP_ERROR);
        return msg;
    }

    public static TVMessage SetPvrFilePlayPause(int param){
        Log.d(TAG, "SetPvrFilePlayPause: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_PAUSE_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_PAUSE_ERROR);
        return msg;
    }

    public static TVMessage SetPvrFilePlayResume(int param){
        Log.d(TAG, "SetPvrFilePlayResume: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_RESUME_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_RESUME_ERROR);
        return msg;
    }

    public static TVMessage SetPvrFilePlayFastForward(int param,int speed){
        Log.d(TAG, "SetPvrFilePlayFastForward: param="+param);
        TVMessage msg;
        if(param == 0) {
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_FF_SUCCESS);
            msg.pvrSpeed = PvrInfo.EnPlaySpeed.convertSpeed(speed);
        }
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_FF_ERROR);
        return msg;
    }

    public static TVMessage SetPvrFilePlayRewind (int param,int speed){
        Log.d(TAG, "SetPvrFilePlayRewind: param="+param);
        TVMessage msg;
        if(param == 0) {
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_RW_SUCCESS);
            msg.pvrSpeed = PvrInfo.EnPlaySpeed.convertSpeed(speed);
        }
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_RW_ERROR);
        return msg;
    }

    public static TVMessage SetPvrFileSeek (int param){
        Log.d(TAG, "SetPvrFileSeek: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_SEEK_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_SEEK_ERROR);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftPlayPause(int param){
        Log.d(TAG, "SetPvrTimeshiftPlayPause: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_PAUSE_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_PAUSE_ERROR);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftPlayResume(int param){
        Log.d(TAG, "SetPvrTimeshiftPlayResume: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_RESUME_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_RESUME_ERROR);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftPlayFastForward(int param,int speed){
        Log.d(TAG, "SetPvrTimeshiftPlayFastForward: param="+param);
        TVMessage msg;
        if(param == 0) {
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_FF_SUCCESS);
            msg.pvrSpeed = PvrInfo.EnPlaySpeed.convertSpeed(speed);
        }
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_FF_ERROR);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftPlayRewind (int param,int speed){
        Log.d(TAG, "SetPvrTimeshiftPlayRewind: param="+param);
        TVMessage msg;
        if(param == 0) {
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_RW_SUCCESS);
            msg.pvrSpeed = PvrInfo.EnPlaySpeed.convertSpeed(speed);
        }
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_RW_ERROR);
        return msg;
    }

    public static TVMessage SetPvrTimeshiftPlaySeek (int param){
        Log.d(TAG, "SetPvrTimeshiftPlaySeek: param="+param);
        TVMessage msg;
        if(param == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_SEEK_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_SEEK_ERROR);
        return msg;
    }

    public static TVMessage SetPvrChangeAudioTrack(int param1,int param2){
        Log.d(TAG, "SetPvrTimeshiftPlaySeek: param1="+param1+" param2"+param2);
        TVMessage msg;
        if(param1 == 0) {
            if(param2 == 0)
                msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_CHANGE_AUDIO_TRACK_SUCCESS);
            else
                msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR);
        }
        else {
            if(param2 == 0)
                msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_SUCCESS);
            else
                msg = new TVMessage(FLAG_PVR, TYPE_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR);
        }
        return msg;
    }


    //eric lin 20180713 pvr msg,-end

    public static TVMessage SetPvrPlayParanentLock(){  //connie 20180806 for pvr parentalRate
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_PLAY_PARENTAL_LOCK);
        return msg;
    }

    public static TVMessage SetPvrHardDiskReady(){//Scoty 20180827 add HDD Ready command and callback
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_HDD_READY);
        return msg;
    }

    public static TVMessage SetPvrNotSupport(){//Scoty 20180827 add HDD Ready command and callback
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_NOT_SUPPORT);
        return msg;
    }

    public static TVMessage SetRemoveUsbStopRec(){//Scoty 20180802 add message when recording and then remove usb not clean rec icon
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_REMOVE_USB_STOP_REC);
        return msg;
    }

    // VMX
    public static TVMessage SetVMXShowMsg(int mode, String CAmsg, int duration){
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_SHOW_MSG);
        msg.CAMsgMode = mode;
        msg.CAmsg = CAmsg;
        msg.CAMsgDuration = duration;
        return msg;
    }

    public static TVMessage SetVMXWaterMark(int mode, String CAmsg, int duration){
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_WATERMARK);
        msg.CAWaterMarkMode = mode;
        msg.CAWaterMarkMsg = CAmsg;
        msg.CAWaterMarkDur = duration;
        return msg;
    }

    public static TVMessage VMXWaterMarkClose(){
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_WATERMARK_CLOSE);
        return msg;
    }

    public static TVMessage VMXOTAStart(int mode){
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_OTA_START);
        msg.OTAMode = mode;
        return msg;
    }

    public static TVMessage VMXOTAError(){
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_OTA_ERR);
        return msg;
    }

    public static TVMessage VMXSetPin(int enable, long channelID, int pinIndex, int textSelector){
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_SET_PIN);
        msg.VMXPinOnOff = enable;
        msg.VMXPinChannelID = channelID;
        msg.VMXPinIndex = pinIndex;
        msg.VMXPinTextSelector = textSelector;
        return msg;
    }

    public static TVMessage VMXIPPV(int enable, long channelID, int pinIndex ,String curToken, String cost){
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_IPPV);
        msg.IPPV_OnOff = enable;
        msg.IPPV_ChannelID = channelID;
        msg.IPPV_PinIndex = pinIndex;
        msg.IPPV_CurToken = curToken;
        msg.IPPV_cost = cost;
        return msg;
    }

    public static TVMessage VMXBcioNotify(int type){ // connie 20180925 add for ippv/pin bcio notify
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_BCIO_NOTIFY);
        msg.VMX_BcioNotify_type = type;
        return msg;
    }

    public static TVMessage VMXCardDetect(int status)
    {
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_CARD_DETECT);
        msg.CardDetect = status;
        return msg;
    }

    public static TVMessage VMXSearch(int mode, int freq, int symbol)
    {
        TVMessage msg = new TVMessage(FLAG_VMX, TYPE_VMX_SEARCH);
        msg.VMXSearchMode = mode;
        msg.VMXSearchFreq = freq;
        msg.VMXSearchsym = symbol;
        return msg;
    }

    public static TVMessage SetDownloadServiceStatus(int is_locked, int dsi_size)
    {
        TVMessage msg = new TVMessage(FLAG_LOADERDTV, TYPE_LOADERDTV_SERVICE_STATUS);
        if(is_locked == 1)
        {
            if(dsi_size != 0)
                msg.loaderdtv_dLStatus = 2;
            else
                msg.loaderdtv_dLStatus = 1;
        }
        else
            msg.loaderdtv_dLStatus =  0;
        Log.d(TAG, "SetDownloadServiceStatus: loaderdtv_dLStatus "+ msg.loaderdtv_dLStatus);
        return msg;
    }

    public static TVMessage SetPioUsbOverload(int port) {
        TVMessage msg = new TVMessage(FLAG_PIO, TYPE_PIO_USB_OVERLOAD);
        msg.pioUsbOverloadPort = port;
        return msg;
    }

    public static TVMessage SetDqaAutoTest(int flag) {
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_DQA_AUTO_TEST);
        msg.dqaAutoTestFlag = flag;
        return msg;
    }

    public static TVMessage SetMtestPcTool(int cmdId, int errCode){//Scoty 20190410 add Mtest Pc Tool callback
        TVMessage msg = new TVMessage(FLAG_MTEST, TYPE_MTEST_PCTOOL);
        msg.cmdId = cmdId;
        msg.errCode = errCode;
        return msg;
    }

    public static TVMessage SetMtestServiceDied() { // edwin 20201214 add HwBinder.DeathRecipient
        return new TVMessage(FLAG_MTEST, TYPE_MTEST_SERVICE_DIED);
    }

    public static TVMessage SetTestCallbackTest(int testParam1, int testParam2, String interfaceName){
        TVMessage msg = new TVMessage(FLAG_TEST, TYPE_CALLBACK_TEST);
        msg.testParam1 = testParam1;
        msg.testParam2 = testParam2;
        msg.testName = interfaceName;
        return msg;
    }

    public static TVMessage SetCaWVMsg(int eventId, byte[] val){//eric lin 20210107 widevine cas
        TVMessage msg = new TVMessage(FLAG_CA, TYPE_CA_MV_MSG);
        widevineMsg wvMsg = new widevineMsg(val);
        msg.WvEventId = eventId;
        msg.WvMsgData = wvMsg;
        return msg;
    }

    public static TVMessage SetCaWVMsg(int eventId, byte[] val, int sessionIndex){//eric lin 20210107 widevine cas
        TVMessage msg = new TVMessage(FLAG_CA, TYPE_CA_MV_MSG);
        widevineMsg wvMsg = new widevineMsg(val, sessionIndex);
        msg.WvEventId = eventId;
        msg.WvMsgData = wvMsg;
        //msg.WvMsg = val;
        //msg.wv_index = sessionIndex;
        Log.d(TAG, "notifyMessage: CALLBACK_EVENT_ECM ZZZ sessionIndex="+sessionIndex);
        return msg;
    }

    public static TVMessage SetCaWVMsg(int eventId, int sessionMode, int wv_index, int es_pid, int ecm_pid)//eric lin 20210107 widevine cas
    {
        TVMessage msg = new TVMessage(FLAG_CA, TYPE_CA_MV_MSG);
        widevineMsg wvMsg = new widevineMsg(sessionMode, wv_index, es_pid, ecm_pid);
        msg.WvEventId = eventId;
        msg.WvMsgData = wvMsg;
        /*
        msg.sessionMode = sessionMode;
        msg.wv_index = wv_index;
        msg.es_pid = es_pid;
        msg.ecm_pid = ecm_pid;
         */
        return msg;
    }

    public static TVMessage SetCaWVMsg(int eventId, widevineMsg wvMsg)//eric lin 20210107 widevine cas
    {
        TVMessage msg = new TVMessage(FLAG_CA, TYPE_CA_MV_MSG);
        msg.WvEventId = eventId;
        msg.WvMsgData = wvMsg;
        return msg;
    }

    public static TVMessage SetShowErrorMsg(int errorCode, String message, int caSystem) {
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_SHOW_ERROR_MESSAGE);
        msg.errCode = errorCode;
        msg.message = message;
        msg.CaSystem = caSystem;
        return msg;
    }

    public static TVMessage UpdateChannelList(){
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_SI_UPDATE_CHANNEL_LIST);

        return msg;
    }
    public static TVMessage SetChannel(ProgramInfo p){
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_SI_UPDATE_SET_CHANNEL);
        msg.channelId = p.getChannelId();
        return msg;
    }
    public static TVMessage Set1stChannel(){
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL);

        return msg;
    }
    public static TVMessage RunAD(){
        TVMessage msg = new TVMessage(FLAG_BOOK, TYPE_BOOK_RUN_AD);

        return msg;
    }
    public static TVMessage SetChannel(long channelId){
        TVMessage msg = new TVMessage(FLAG_CA, TYPE_CA_REFRESS_CAS_SET_CHANNEL);
        msg.channelId = channelId;
        return msg;
    }


    public static TVMessage SetSeriesUpdateMsg() {
        return new TVMessage(FLAG_SERIES, TYPE_SERIES_UPDATE);
	}
	
    public static TVMessage NoHDDSpace(){
        TVMessage msg = new TVMessage(FLAG_HDD, TYPE_HDD_NO_SPACE);
        return msg;
    }

    public static TVMessage ShowOTACountDownDialog(String lastVersion) {
        TVMessage msg = new TVMessage(FLAG_OTA, TYPE_OTA_SHOW_COUNT_DOWN_DIALOG);
        msg.otaLastVersion = lastVersion;
        return msg;
    }

    public static TVMessage ShowOTAUpdateDialog(String lastVersion) {
        TVMessage msg = new TVMessage(FLAG_OTA, TYPE_OTA_SHOW_UPDATE_DIALOG);
        msg.otaLastVersion = lastVersion;
        return msg;
    }

    public int getMsgType(){
        return type;
    }
    public int getMsgFlag(){
        return flag;
    }

    public int getServiceId(){
        return serviceId;
    }
    public int getchannelLCN(){
        return serviceLCN;
    }
    public String getChannelLCNString() {
        int tunerType = Pvcfg.getTunerType();
        if(tunerType == TpInfo.ISDBT)
        {
            double value = serviceLCN / 100.0;
            String raw = String.format(Locale.US, "%.2f", value);
//            String raw = String.format("%.2f", value);  // 例：0.01 或 10.00
            String[] parts = raw.split("\\.");
            String intPartRaw = parts[0];

            // 最多保留兩位整數（左邊補 0，超過兩位就保留最後兩位）
            String intPart;
            if (intPartRaw.length() >= 2) {
                intPart = intPartRaw.substring(intPartRaw.length() - 2); // 取最後兩位
            } else {
                intPart = String.format("%02d", Integer.parseInt(intPartRaw)); // 補 0
            }

            return intPart + "." + parts[1];
        }else
        {
            return String.valueOf(serviceLCN);
        }
    }

    public String getChannelName(){
        return serviceName;
    }
    public int getCAFlag(){
        return CAFlag;
    }
    public int getTotalTVNumber(){
        return total_tv;
    }
    public int getTotalRadioNumber(){
        return total_radio;
    }
    public int getserviceType(){
        return serviceType;
    }
    public int getAlreadyScanedTpNum(){
        return AlreadyScanedTpNum;
    }

    public int getServiceCHNum() {
        return serviceCHNum;
    }

    public int getTpId() {
        return tpId;
    }
    public int getPercent() {
        return percent;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getCA_ErrorCode()
    {
        return Error_Code;
    }
    public String getCA_Msg_String(){
        return CA_Msg_String;
    }

    public int getTunerLockStatus()
    {
        return TunerLock;
    }

    public int getAvLockStatus()
    {
        return AVlockStatus;
    }

    public int getRating()
    {
        return Rating;
    }

    public int getAvFinish()
    {
        return Finish;
    }

    public int getAvFrameStatus()
    {
        return AvFrameStatus;
    }
    public long getAvFrameChannelId()//eric lin 20180803 no video signal
    {
        return AvFrameChannelId;
    }
    public int getAvTunerId()
    {
        return AvTunerId;
    }

    public int getRecId()//eric lin 20180713 pvr msg
    {
        return recId;
    }

    public long getRecChannelId() {
        return recChannelId;
    }

    public int getPvrMode()//eric lin 20180713 pvr msg
    {
        return pvrMode;
    }

    public PvrRecIdx getPvrRecIdx() {
        return PvrRecIdx.fromCombinedString(pvrCombinedRecIdxStr);
    }

    //VMX    // connie 20180903 for VMX -s
    public int getCAMsgMode()
    {
        return CAMsgMode;
    }

    public String getCAMsg()
    {
        return CAmsg;
    }

    public int getCAMsgDuration()
    {
        return CAMsgDuration;
    }

    public int GetWaterMarkMode()
    {
        return CAWaterMarkMode;
    }

    public String GetWaterMarkMsg()
    {
        return CAWaterMarkMsg;
    }

    public int GetWaterMarkDur()
    {
        return CAWaterMarkDur;
    }

    public int GetOTAMode() {
        return OTAMode;
    }

    public int GetVMXPinOnOff() {
        return VMXPinOnOff;
    }

    public long GetVMXPinChannelID() {
        return VMXPinChannelID;
    }

    public int GetVMXPinIndex() {
        return VMXPinIndex;
    }

    public int GetVMXPinTextSelector() {
        return VMXPinTextSelector;
    }


    public long GetIPPV_ChannelID() {
        return IPPV_ChannelID;
    }

    public int GetIPPV_PinIndex() {
        return IPPV_PinIndex;
    }



    public int GetIPPV_OnOff() {
        return IPPV_OnOff;
    }

    public String GetIPPV_CurToken() {
        return IPPV_CurToken;
    }

    public String GetIPPV_cost() {
        return IPPV_cost;
    }

    public int GetPCIONotify_type() // connie 20180925 add for ippv/pin bcio notify
    {
        return VMX_BcioNotify_type;
    }

    public int GetCardDetect() {
        return CardDetect;
    }

    public int GetVMXSearchMode() {
        return VMXSearchMode;
    }
    public int GetVMXSearchFreq() {
        return VMXSearchFreq;
    }
    public int GetVMXSearchsym() {
        return VMXSearchsym;
    }   // connie 20180903 for VMX-e

    public int GetVMXTriggerID()
    {
        return VMXTriggerID;
    }

    public int GetVMXTriggerNum()
    {
        return VMXTriggerNum;
    }

    public int GetLoaderDtvDLStatus() {return loaderdtv_dLStatus; }

    public int GetPioUsbOverloadPort() {
        return pioUsbOverloadPort;
    }

    public int GetDqaAutoTestFlag() {
        return dqaAutoTestFlag;
    }

    public int GetMtestCmdId()//Scoty 20190410 add Mtest Pc Tool callback
    {
        return cmdId;
    }

    public int GetMtestErrCode()//Scoty 20190410 add Mtest Pc Tool callback
    {
        return errCode;
    }

    public int getTestParam1() {
        return testParam1;
    }

    public int getTestParam2() {
        return testParam2;
    }

    public String getTestName() {
        return testName;
    }

    //eric lin 20210107 widevine cas, -start    
    public int getWvEventId() {return WvEventId;}

    public byte[] getWvMsg() {return WvMsg;}
    public widevineMsg getWvMsgData() {return WvMsgData;}

    public int getCaSystem() {
        return CaSystem;
    }

    public void setCaSystem(int caSystem) {
        CaSystem = caSystem;
    }

    public byte[] getClosedCaptionData() {return ClosedCaptionData;}

    public int getClosedCaptionEnable() {
        return ClosedCaptionEnable;
    }

    public int getClosedCaptionType() {
        return ClosedCaptionType;
    }

    public int getNit_id() {
        return nit_id;
    }

    public String getOtaLastVersion() {
        return otaLastVersion;
    }

    @ParcelablePlease
    public static class widevineMsg implements Parcelable {
        public int sessionMode;
        public int wv_index;
        public int es_pid;
        public int ecm_pid;
        public int ecmDataLen;
        public byte[] ecmData;
        public widevineMsg()
        {

        }
        public widevineMsg(byte[] data)
        {
            ecmData = data;
        }
        widevineMsg(byte[] data, int wv_index)
        {
            this.wv_index = wv_index;
            ecmData = data;
        }
        widevineMsg(int sessionMode, int wv_index, int es_pid, int ecm_pid)
        {
            this.sessionMode = sessionMode;
            this.wv_index = wv_index;
            this.es_pid = es_pid;
            this.ecm_pid = ecm_pid;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            TVMessage$widevineMsgParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<widevineMsg> CREATOR = new Creator<widevineMsg>() {
            public widevineMsg createFromParcel(Parcel source) {
                widevineMsg target = new widevineMsg();
                TVMessage$widevineMsgParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public widevineMsg[] newArray(int size) {
                return new widevineMsg[size];
            }
        };
    }
    //eric lin 20210107 widevine cas, -end

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TVMessageParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TVMessage> CREATOR = new Creator<TVMessage>() {
        public TVMessage createFromParcel(Parcel source) {
            TVMessage target = new TVMessage();
            TVMessageParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TVMessage[] newArray(int size) {
            return new TVMessage[size];
        }
    };

    //Irdeto Cloak start
    public static TVMessage InitCloakEcmStatus(int serviceHandle,String statusMessage,int ucStreamProtocolType,int UcStreamPid,
                                               int UcStreamWebAddressType,int UcStreamWebAddressLength,byte[] UcStreamWebAddressURL) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_ECM_STATUS);
        msg.cloakEcmStatus = new CloakEcmStatus(serviceHandle,statusMessage,ucStreamProtocolType,UcStreamPid,
                UcStreamWebAddressType,UcStreamWebAddressLength,UcStreamWebAddressURL);
        return  msg;
    }

    @ParcelablePlease
    public static class CloakEcmStatus implements Parcelable {
        public int serviceHandle;
        public String statusMessage;
        public int ucStreamProtocolType; //UC_STREAM_DVB=0,UC_STREAM_IP=1
        public int UcStreamPid;
        public int UcStreamWebAddressType; //UC_CLIENT_REGISTRATION_WEB_SERVICE=1,UC_PULL_EMM_WEB_SERVICE=2
        public int UcStreamWebAddressLength;
        public byte[] UcStreamWebAddressURL=new byte[0];

        public CloakEcmStatus()
        {

        }
        public CloakEcmStatus(int serviceHandle,String statusMessage,int ucStreamProtocolType,int UcStreamPid,int UcStreamWebAddressType,
                              int UcStreamWebAddressLength,byte[] UcStreamWebAddressURL) {
            this.serviceHandle=serviceHandle;
            this.statusMessage=statusMessage;
            this.ucStreamProtocolType=ucStreamProtocolType;
            this.UcStreamPid=UcStreamPid;
            this.UcStreamWebAddressType=UcStreamWebAddressType;
            this.UcStreamWebAddressLength=UcStreamWebAddressLength;
            if(UcStreamWebAddressLength > 0)
                this.UcStreamWebAddressURL=UcStreamWebAddressURL;
        }
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            TVMessage$CloakEcmStatusParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<CloakEcmStatus> CREATOR = new Creator<CloakEcmStatus>() {
            public CloakEcmStatus createFromParcel(Parcel source) {
                CloakEcmStatus target = new CloakEcmStatus();
                TVMessage$CloakEcmStatusParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public CloakEcmStatus[] newArray(int size) {
                return new CloakEcmStatus[size];
            }
        };
    }
    public CloakEcmStatus getCloakEcmStatus() {return cloakEcmStatus;}

    public void printCloakEcmStatus() {
        int i,j;
        String url = new String(cloakEcmStatus.UcStreamWebAddressURL, StandardCharsets.UTF_8);
        Log.d(TAG, "@@@### cloakEcmStatus.serviceHandle="+cloakEcmStatus.serviceHandle);
        Log.d(TAG, "@@@### cloakEcmStatus.statusMessage="+cloakEcmStatus.statusMessage);
        Log.d(TAG, "@@@### cloakEcmStatus.ucStreamProtocolType="+cloakEcmStatus.ucStreamProtocolType);
        Log.d(TAG, "@@@### cloakEcmStatus.UcStreamPid="+cloakEcmStatus.UcStreamPid);
        Log.d(TAG, "@@@### cloakEcmStatus.UcStreamWebAddressType="+cloakEcmStatus.UcStreamWebAddressType);
        Log.d(TAG, "@@@### cloakEcmStatus.UcStreamWebAddressLength="+cloakEcmStatus.UcStreamWebAddressLength);
        Log.d(TAG, "@@@### cloakEcmStatus.UcStreamWebAddressURL="+cloakEcmStatus.UcStreamWebAddressURL);
        Log.d(TAG, "@@@### cloakEcmStatus.UcStreamWebAddressURL="+url);
    }

    public static TVMessage InitCloakServiceStatus(String statusMessage,int serviceHandle,int nonPVREnableFlag,int isShareable,
                                                   int isSpeRemainingTimeValid, int speRemainingTime_remainingTime,int isSecureMediaPipelineUsed,
                                                   int[] productId,int[] errorCodeForSMP) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_SERVICE_STATUS);
        msg.cloakServiceStatus = new CloakServiceStatus(statusMessage,serviceHandle,nonPVREnableFlag,isShareable,
                isSpeRemainingTimeValid, speRemainingTime_remainingTime,isSecureMediaPipelineUsed,
                productId,errorCodeForSMP);
        return  msg;
    }

    public static class CloakServiceStatus implements Parcelable {
        public String statusMessage;
        public int serviceHandle;
        public int nonPVREnableFlag;
        public ExtendedStatus extendedStatus;
        public int isShareable;
        public int isSpeRemainingTimeValid;
        public int[] speRemainingTime_productId;
        public int speRemainingTime_remainingTime;
        public int isSecureMediaPipelineUsed;
        public int[] errorCodeForSMP;

        public CloakServiceStatus(String statusMessage, int serviceHandle, int nonPVREnableFlag, int isShareable,
                                  int isSpeRemainingTimeValid, int speRemainingTime_remainingTime,
                                  int isSecureMediaPipelineUsed, int[] productId, int[] errorCodeForSMP) {
            this.statusMessage = statusMessage;
            this.serviceHandle = serviceHandle;
            this.nonPVREnableFlag = nonPVREnableFlag;
            this.isShareable = isShareable;
            this.isSpeRemainingTimeValid = isSpeRemainingTimeValid;
            this.speRemainingTime_remainingTime = speRemainingTime_remainingTime;
            this.isSecureMediaPipelineUsed = isSecureMediaPipelineUsed;
            this.speRemainingTime_productId = productId != null ? productId.clone() : new int[0];
            this.errorCodeForSMP = errorCodeForSMP != null ? errorCodeForSMP.clone() : new int[0];
            this.extendedStatus = new ExtendedStatus();
        }

        protected CloakServiceStatus(Parcel in) {
            statusMessage = in.readString();
            serviceHandle = in.readInt();
            nonPVREnableFlag = in.readInt();
            isShareable = in.readInt();
            isSpeRemainingTimeValid = in.readInt();
            speRemainingTime_remainingTime = in.readInt();
            isSecureMediaPipelineUsed = in.readInt();
            speRemainingTime_productId = in.createIntArray();
            errorCodeForSMP = in.createIntArray();
            extendedStatus = in.readParcelable(ExtendedStatus.class.getClassLoader(), CloakServiceStatus.ExtendedStatus.class);
        }

        public static final Creator<CloakServiceStatus> CREATOR = new Creator<CloakServiceStatus>() {
            @Override
            public CloakServiceStatus createFromParcel(Parcel in) {
                return new CloakServiceStatus(in);
            }

            @Override
            public CloakServiceStatus[] newArray(int size) {
                return new CloakServiceStatus[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(statusMessage);
            dest.writeInt(serviceHandle);
            dest.writeInt(nonPVREnableFlag);
            dest.writeInt(isShareable);
            dest.writeInt(isSpeRemainingTimeValid);
            dest.writeInt(speRemainingTime_remainingTime);
            dest.writeInt(isSecureMediaPipelineUsed);
            dest.writeIntArray(speRemainingTime_productId);
            dest.writeIntArray(errorCodeForSMP);
            dest.writeParcelable(extendedStatus, flags);
        }

        public static class ExtendedStatus implements Parcelable {
            public int isValid;
            public int operatorCnt;
            public int[] caSystemID;
            public String[] operatorStatus;
            public int[] isSecureMediaPipelineUsed;
            public int[][] errorCodeForSMP;

            public ExtendedStatus() {
                this.caSystemID = new int[0];
                this.operatorStatus = new String[0];
                this.isSecureMediaPipelineUsed = new int[0];
                this.errorCodeForSMP = new int[0][0];
            }

            public void setExtendedStatus(int isValid, int operatorCnt, int[] caSystemID,
                                          int[] isSecureMediaPipelineUsed, String[] operatorStatus, int[][] errorCodeForSMP) {
                this.isValid = isValid;
                this.operatorCnt = operatorCnt;
                this.caSystemID = caSystemID != null ? caSystemID.clone() : new int[0];
                this.isSecureMediaPipelineUsed = isSecureMediaPipelineUsed != null ? isSecureMediaPipelineUsed.clone() : new int[0];
                this.operatorStatus = operatorStatus != null ? operatorStatus.clone() : new String[0];
                if (errorCodeForSMP != null && errorCodeForSMP.length > 0 && errorCodeForSMP[0].length > 0) {
                    this.errorCodeForSMP = new int[errorCodeForSMP.length][];
                    for (int i = 0; i < errorCodeForSMP.length; i++) {
                        this.errorCodeForSMP[i] = errorCodeForSMP[i] != null ? errorCodeForSMP[i].clone() : new int[0];
                    }
                } else {
                    this.errorCodeForSMP = new int[0][0];
                }
            }

            protected ExtendedStatus(Parcel in) {
                isValid = in.readInt();
                operatorCnt = in.readInt();
                caSystemID = in.createIntArray();
                operatorStatus = in.createStringArray();
                isSecureMediaPipelineUsed = in.createIntArray();
                int outerLen = in.readInt();
                if (outerLen > 0) {
                    errorCodeForSMP = new int[outerLen][];
                    for (int i = 0; i < outerLen; i++) {
                        errorCodeForSMP[i] = in.createIntArray();
                    }
                } else {
                    errorCodeForSMP = new int[0][0];
                }
            }

            public static final Creator<ExtendedStatus> CREATOR = new Creator<ExtendedStatus>() {
                @Override
                public ExtendedStatus createFromParcel(Parcel in) {
                    return new ExtendedStatus(in);
                }

                @Override
                public ExtendedStatus[] newArray(int size) {
                    return new ExtendedStatus[size];
                }
            };

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(isValid);
                dest.writeInt(operatorCnt);
                dest.writeIntArray(caSystemID);
                dest.writeStringArray(operatorStatus);
                dest.writeIntArray(isSecureMediaPipelineUsed);
                dest.writeInt(errorCodeForSMP.length);
                for (int[] array : errorCodeForSMP) {
                    dest.writeIntArray(array);
                }
            }
        }

        public ExtendedStatus getExtendedStatus() {
            return extendedStatus;
        }

        public void setExtendedStatus(int isValid, int operatorCnt, int[] caSystemID,
                                      int[] isSecureMediaPipelineUsed, String[] operatorStatus, int[][] errorCodeForSMP) {
            extendedStatus.setExtendedStatus(isValid, operatorCnt, caSystemID, isSecureMediaPipelineUsed, operatorStatus, errorCodeForSMP);
        }
    }
    public CloakServiceStatus getCloakServiceStatus() {return cloakServiceStatus;}

    public void printCloakServiceStatus() {
        int i,j;
        Log.d(TAG, "@@@### cloakServiceStatus.statusMessage="+cloakServiceStatus.statusMessage);
        Log.d(TAG, "@@@### cloakServiceStatus.serviceHandle="+cloakServiceStatus.serviceHandle);
        Log.d(TAG, "@@@### cloakServiceStatus.nonPVREnableFlag="+cloakServiceStatus.nonPVREnableFlag);
        Log.d(TAG, "@@@### cloakServiceStatus.extendedStatus.isValid="+cloakServiceStatus.extendedStatus.isValid);
        Log.d(TAG, "@@@### cloakServiceStatus.extendedStatus.operatorCnt="+cloakServiceStatus.extendedStatus.operatorCnt);
        for(i=0;i<cloakServiceStatus.extendedStatus.caSystemID.length;i++)
            Log.d(TAG, "@@@### cloakServiceStatus.extendedStatus.caSystemID["+i+"]="+cloakServiceStatus.extendedStatus.caSystemID[i]);
        for(i=0;i<cloakServiceStatus.extendedStatus.operatorStatus.length;i++)
            Log.d(TAG, "@@@### cloakServiceStatus.extendedStatus.operatorStatus["+i+"]="+cloakServiceStatus.extendedStatus.operatorStatus[i]);
        for(i=0;i<cloakServiceStatus.extendedStatus.isSecureMediaPipelineUsed.length;i++)
            Log.d(TAG, "@@@### cloakServiceStatus.extendedStatus.isSecureMediaPipelineUsed["+i+"]="+cloakServiceStatus.extendedStatus.isSecureMediaPipelineUsed[i]);
        for (i = 0; i < cloakServiceStatus.extendedStatus.errorCodeForSMP.length; i++) {
            for (j = 0; j < cloakServiceStatus.extendedStatus.errorCodeForSMP[i].length; j++) {
                Log.d(TAG, "@@@### cloakServiceStatus.extendedStatus.errorCodeForSMP["+i+"]["+j+"]="+cloakServiceStatus.extendedStatus.errorCodeForSMP[i][j]);
            }
        }
        Log.d(TAG, "@@@### cloakServiceStatus.isShareable="+cloakServiceStatus.isShareable);
        Log.d(TAG, "@@@### cloakServiceStatus.isSpeRemainingTimeValid="+cloakServiceStatus.isSpeRemainingTimeValid);
        for(i=0;i<cloakServiceStatus.speRemainingTime_productId.length;i++)
            Log.d(TAG, "@@@### cloakServiceStatus.speRemainingTime_productId["+i+"]="+cloakServiceStatus.speRemainingTime_productId[i]);
        Log.d(TAG, "@@@### cloakServiceStatus.speRemainingTime_remainingTime="+cloakServiceStatus.speRemainingTime_remainingTime);
        Log.d(TAG, "@@@### cloakServiceStatus.isSecureMediaPipelineUsed="+cloakServiceStatus.isSecureMediaPipelineUsed);
        for(i=0;i<cloakServiceStatus.errorCodeForSMP.length;i++)
            Log.d(TAG, "@@@### cloakServiceStatus.errorCodeForSMP["+i+"]="+cloakServiceStatus.errorCodeForSMP[i]);
    }

    public static TVMessage InitCloakEmmStatus(int serviceHandle,String statusMessage,int ucStreamProtocolType,int UcStreamPid,
                                               int UcStreamWebAddressType,int UcStreamWebAddressLength,byte[] UcStreamWebAddressURL) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_EMM_STATUS);
        msg.cloakEmmStatus = new CloakEmmStatus(serviceHandle,statusMessage,ucStreamProtocolType,UcStreamPid,
                UcStreamWebAddressType,UcStreamWebAddressLength,UcStreamWebAddressURL);
        return  msg;
    }

    public static class CloakEmmStatus implements Parcelable {
        public int serviceHandle;
        public String statusMessage;
        public int ucStreamProtocolType; //UC_STREAM_DVB=0,UC_STREAM_IP=1
        public int UcStreamPid;
        public int UcStreamWebAddressType; //UC_CLIENT_REGISTRATION_WEB_SERVICE=1,UC_PULL_EMM_WEB_SERVICE=2
        public int UcStreamWebAddressLength;
        public byte[] UcStreamWebAddressURL=new byte[0];

        public CloakEmmStatus(int serviceHandle,String statusMessage,int ucStreamProtocolType,int UcStreamPid,int UcStreamWebAddressType,
                              int UcStreamWebAddressLength,byte[] UcStreamWebAddressURL) {
            this.serviceHandle=serviceHandle;
            this.statusMessage=statusMessage;
            this.ucStreamProtocolType=ucStreamProtocolType;
            this.UcStreamPid=UcStreamPid;
            this.UcStreamWebAddressType=UcStreamWebAddressType;
            this.UcStreamWebAddressLength=UcStreamWebAddressLength;
            if(UcStreamWebAddressLength > 0)
                this.UcStreamWebAddressURL=UcStreamWebAddressURL;
        }

        protected CloakEmmStatus(Parcel in) {
            serviceHandle = in.readInt();
            statusMessage = in.readString();
            ucStreamProtocolType = in.readInt();
            UcStreamPid = in.readInt();
            UcStreamWebAddressType = in.readInt();
            UcStreamWebAddressLength = in.readInt();
            UcStreamWebAddressURL = in.readBlob();
        }

        public static final Creator<CloakEmmStatus> CREATOR = new Creator<CloakEmmStatus>() {
            @Override
            public CloakEmmStatus createFromParcel(Parcel in) {
                return new CloakEmmStatus(in);
            }

            @Override
            public CloakEmmStatus[] newArray(int size) {
                return new CloakEmmStatus[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(serviceHandle);
            dest.writeString(statusMessage);
            dest.writeInt(ucStreamProtocolType);
            dest.writeInt(UcStreamPid);
            dest.writeInt(UcStreamWebAddressType);
            dest.writeInt(UcStreamWebAddressLength);
            dest.writeBlob(UcStreamWebAddressURL);
        }
    }
    public CloakEmmStatus getCloakEmmStatus() {return cloakEmmStatus;}

    public void printCloakEmmStatus() {
        int i,j;
        String url = new String(cloakEmmStatus.UcStreamWebAddressURL, StandardCharsets.UTF_8);
        Log.d(TAG, "@@@### cloakEmmStatus.serviceHandle="+cloakEmmStatus.serviceHandle);
        Log.d(TAG, "@@@### cloakEmmStatus.statusMessage="+cloakEmmStatus.statusMessage);
        Log.d(TAG, "@@@### cloakEmmStatus.ucStreamProtocolType="+cloakEmmStatus.ucStreamProtocolType);
        Log.d(TAG, "@@@### cloakEmmStatus.UcStreamPid="+cloakEmmStatus.UcStreamPid);
        Log.d(TAG, "@@@### cloakEmmStatus.UcStreamWebAddressType="+cloakEmmStatus.UcStreamWebAddressType);
        Log.d(TAG, "@@@### cloakEmmStatus.UcStreamWebAddressLength="+cloakEmmStatus.UcStreamWebAddressLength);
        Log.d(TAG, "@@@### cloakEmmStatus.UcStreamWebAddressURL="+cloakEmmStatus.UcStreamWebAddressURL);
        Log.d(TAG, "@@@### cloakEmmStatus.UcStreamWebAddressURL="+url);
    }

    public static TVMessage InitExtendedProductList(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_EXTENDED_PRODUCT_LIST);
        return msg;
    }

    public static TVMessage InitCloakMonitorEcm(int serviceHandle,String statusMessage,int caSystemID) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_MONITOR_ECM);
        msg.cloakMonitorEcm = new CloakMonitorEcm(serviceHandle,statusMessage,caSystemID);
        return  msg;
    }

    public static class CloakMonitorEcm implements Parcelable {
        public int serviceHandle;
        public String statusMessage;
        public int caSystemID; //UC_STREAM_DVB=0,UC_STREAM_IP=1

        public CloakMonitorEcm(int serviceHandle,String statusMessage,int caSystemID) {
            this.serviceHandle=serviceHandle;
            this.statusMessage=statusMessage;
            this.caSystemID=caSystemID;
        }

        protected CloakMonitorEcm(Parcel in) {
            serviceHandle = in.readInt();
            statusMessage = in.readString();
            caSystemID = in.readInt();
        }

        public static final Creator<CloakMonitorEcm> CREATOR = new Creator<CloakMonitorEcm>() {
            @Override
            public CloakMonitorEcm createFromParcel(Parcel in) {
                return new CloakMonitorEcm(in);
            }

            @Override
            public CloakMonitorEcm[] newArray(int size) {
                return new CloakMonitorEcm[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(serviceHandle);
            dest.writeString(statusMessage);
            dest.writeInt(caSystemID);
        }
    }

    public CloakMonitorEcm getCloakMonitorEcm() {return cloakMonitorEcm;}

    public String getCloakMonitorEcmStatusMessage() { return cloakMonitorEcm.statusMessage; }

    public int getCloakMonitorEcmCaSystemID() { return cloakMonitorEcm.caSystemID; }


    public void printCloakMonitorEcm() {
        Log.d(TAG, "@@@### cloakMonitorEcm.serviceHandle="+cloakMonitorEcm.serviceHandle);
        Log.d(TAG, "@@@### cloakMonitorEcm.statusMessage="+cloakMonitorEcm.statusMessage);
        Log.d(TAG, "@@@### cloakMonitorEcm.caSystemID="+cloakMonitorEcm.caSystemID);
    }

    public static TVMessage InitCloakMonitorEmm(int serviceHandle,String statusMessage,int caSystemID) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_MONITOR_EMM);
        msg.cloakMonitorEmm = new CloakMonitorEmm(serviceHandle,statusMessage,caSystemID);
        return  msg;
    }

    public static class CloakMonitorEmm implements Parcelable{
        public int serviceHandle;
        public String statusMessage;
        public int caSystemID; //UC_STREAM_DVB=0,UC_STREAM_IP=1

        public CloakMonitorEmm(int serviceHandle,String statusMessage,int caSystemID) {
            this.serviceHandle=serviceHandle;
            this.statusMessage=statusMessage;
            this.caSystemID=caSystemID;
        }

        protected CloakMonitorEmm(Parcel in) {
            serviceHandle = in.readInt();
            statusMessage = in.readString();
            caSystemID = in.readInt();
        }

        public static final Creator<CloakMonitorEmm> CREATOR = new Creator<CloakMonitorEmm>() {
            @Override
            public CloakMonitorEmm createFromParcel(Parcel in) {
                return new CloakMonitorEmm(in);
            }

            @Override
            public CloakMonitorEmm[] newArray(int size) {
                return new CloakMonitorEmm[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(serviceHandle);
            dest.writeString(statusMessage);
            dest.writeInt(caSystemID);
        }
    }

    public CloakMonitorEmm getCloakMonitorEmm() {return cloakMonitorEmm;}

    public String getCloakMonitorEmmStatusMessage() { return cloakMonitorEmm.statusMessage; }

    public int getCloakMonitorEmmCaSystemID() { return cloakMonitorEmm.caSystemID; }

    public void printCloakMonitorEmm() {
        Log.d(TAG, "@@@### cloakMonitorEmm.serviceHandle="+cloakMonitorEmm.serviceHandle);
        Log.d(TAG, "@@@### cloakMonitorEmm.statusMessage="+cloakMonitorEmm.statusMessage);
        Log.d(TAG, "@@@### cloakMonitorEmm.caSystemID="+cloakMonitorEmm.caSystemID);
    }

    public static TVMessage InitCloakAttribMsg(int bIndex,int wEmmHandle,int wServiceHandle,int wMsgLength,byte[] bMessageData,int bType,
                                               int bDisplayMethod,int wDuration,int wCRC16,int bFingerprintType,int bRFU,int[][] covertedDot,int ShowFP) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_ATTRIB_MSG);
        msg.cloakAttribMsg = new CloakAttribMsg(bIndex,wEmmHandle,wServiceHandle,wMsgLength,bMessageData,bType,bDisplayMethod,wDuration,wCRC16,
                bFingerprintType,bRFU,covertedDot,ShowFP);
        return  msg;
    }

    public static class CloakAttribMsg implements Parcelable{
        public int bIndex;
        public int wEmmHandle;
        public int wServiceHandle;
        public int wMsgLength;
        public byte[] bMessageData = new byte[0];
        public int bType;
        public int bDisplayMethod;
        public int wDuration;
        public int wCRC16;
        public int bFingerprintType;
        public int bRFU;
        public int[][] covertedDot = new int[0][0];
        public int ShowFP;

        public int time_year;
        public int time_month;
        public int time_date;
        public int time_hour;
        public int time_minute;
        public int time_second;
        public int time_week;

        public int FpRandomParam_enable;
        public int FpRandomParam_x;
        public int FpRandomParam_y;
        public int FpRandomParam_fcol;
        public int FpRandomParam_bcol;

        public CloakAttribMsg(int bIndex,int wEmmHandle,int wServiceHandle,int wMsgLength,byte[] bMessageData,int bType, int bDisplayMethod,
                              int wDuration,int wCRC16,int bFingerprintType,int bRFU, int[][] covertedDot,int ShowFP) {
            int i,j;
            this.bIndex = bIndex;
            this.wEmmHandle = wEmmHandle;
            this.wServiceHandle = wServiceHandle;
            this.wMsgLength = wMsgLength;
            this.bType = bType;
            this.bDisplayMethod = bDisplayMethod;
            this.wDuration = wDuration;
            this.wCRC16 = wCRC16;
            this.bFingerprintType = bFingerprintType;
            this.bRFU = bRFU;
            this.ShowFP = ShowFP;
            this.bMessageData = bMessageData != null ? bMessageData.clone() : new byte[0];
            this.covertedDot = covertedDot != null ? covertedDot.clone() : new int[0][0];
        }

        protected CloakAttribMsg(Parcel in) {
            bIndex = in.readInt();
            wEmmHandle = in.readInt();
            wServiceHandle = in.readInt();
            wMsgLength = in.readInt();
            bMessageData = in.readBlob();
            bType = in.readInt();
            bDisplayMethod = in.readInt();
            wDuration = in.readInt();
            wCRC16 = in.readInt();
            bFingerprintType = in.readInt();
            bRFU = in.readInt();
            int outerLen = in.readInt();
            if (outerLen > 0) {
                covertedDot = new int[outerLen][];
                for (int i = 0; i < outerLen; i++) {
                    covertedDot[i] = in.createIntArray(); // 讀取每個內層數組
                }
            } else {
                covertedDot = new int[0][0];
            }
            ShowFP = in.readInt();
            time_year = in.readInt();
            time_month = in.readInt();
            time_date = in.readInt();
            time_hour = in.readInt();
            time_minute = in.readInt();
            time_second = in.readInt();
            time_week = in.readInt();
            FpRandomParam_enable = in.readInt();
            FpRandomParam_x = in.readInt();
            FpRandomParam_y = in.readInt();
            FpRandomParam_fcol = in.readInt();
            FpRandomParam_bcol = in.readInt();
        }

        public static final Creator<CloakAttribMsg> CREATOR = new Creator<CloakAttribMsg>() {
            @Override
            public CloakAttribMsg createFromParcel(Parcel in) {
                return new CloakAttribMsg(in);
            }

            @Override
            public CloakAttribMsg[] newArray(int size) {
                return new CloakAttribMsg[size];
            }
        };

        public void SetCloakAttribMsg_Time(int year, int month, int date, int hour, int minute, int second, int week) {
            this.time_year=year;
            this.time_month=month;
            this.time_date=date;
            this.time_hour=hour;
            this.time_minute=minute;
            this.time_second=second;
            this.time_week=week;
        }
        public void SetCloakAttribMsg_FpRandom(int enable,int x,int y,int fcol,int bcol) {
            this.FpRandomParam_enable=enable;
            this.FpRandomParam_x=x;
            this.FpRandomParam_y=y;
            this.FpRandomParam_fcol=fcol;
            this.FpRandomParam_bcol=bcol;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(bIndex);
            dest.writeInt(wEmmHandle);
            dest.writeInt(wServiceHandle);
            dest.writeInt(wMsgLength);
            dest.writeBlob(bMessageData);
            dest.writeInt(bType);
            dest.writeInt(bDisplayMethod);
            dest.writeInt(wDuration);
            dest.writeInt(wCRC16);
            dest.writeInt(bFingerprintType);
            dest.writeInt(bRFU);
            if (covertedDot != null && covertedDot.length > 0 && covertedDot[0] != null) {
                dest.writeInt(covertedDot.length); // 寫入外層數組長度
                for (int[] array : covertedDot) {
                    dest.writeIntArray(array != null ? array : new int[0]); // 寫入每個內層數組
                }
            } else {
                dest.writeInt(0); // 空數組
            }
            dest.writeInt(ShowFP);
            dest.writeInt(time_year);
            dest.writeInt(time_month);
            dest.writeInt(time_date);
            dest.writeInt(time_hour);
            dest.writeInt(time_minute);
            dest.writeInt(time_second);
            dest.writeInt(time_week);
            dest.writeInt(FpRandomParam_enable);
            dest.writeInt(FpRandomParam_x);
            dest.writeInt(FpRandomParam_y);
            dest.writeInt(FpRandomParam_fcol);
            dest.writeInt(FpRandomParam_bcol);
        }
    }
    public CloakAttribMsg getCloakAttribMsg() {return cloakAttribMsg;}
    public void printCloakAttribMsg() {
        int i,j;
        Log.d(TAG, "@@@### cloakAttribMsg.bIndex="+cloakAttribMsg.bIndex);
        Log.d(TAG, "@@@### cloakAttribMsg.wEmmHandle="+cloakAttribMsg.wEmmHandle);
        Log.d(TAG, "@@@### cloakAttribMsg.wServiceHandle="+cloakAttribMsg.wServiceHandle);
        Log.d(TAG, "@@@### cloakAttribMsg.wMsgLength="+cloakAttribMsg.wMsgLength);
        for(i=0;i<cloakAttribMsg.bMessageData.length;i++)
            Log.d(TAG, "@@@### cloakAttribMsg.bMessageData["+i+"]="+cloakAttribMsg.bMessageData[i]);

        Log.d(TAG, "@@@### cloakAttribMsg.bType="+cloakAttribMsg.bType);
        Log.d(TAG, "@@@### cloakAttribMsg.bDisplayMethod="+cloakAttribMsg.bDisplayMethod);
        Log.d(TAG, "@@@### cloakAttribMsg.wDuration="+cloakAttribMsg.wDuration);
        Log.d(TAG, "@@@### cloakAttribMsg.wCRC16="+cloakAttribMsg.wCRC16);
        Log.d(TAG, "@@@### cloakAttribMsg.bFingerprintType="+cloakAttribMsg.bFingerprintType);
        Log.d(TAG, "@@@### cloakAttribMsg.bRFU="+cloakAttribMsg.bRFU);
        for (i = 0; i < cloakAttribMsg.covertedDot.length; i++) {
            for (j = 0; j < cloakAttribMsg.covertedDot[i].length; j++) {
                Log.d(TAG, "@@@### cloakAttribMsg.covertedDot["+i+"]["+j+"]="+cloakAttribMsg.covertedDot[i][j]);
            }
        }
        Log.d(TAG, "@@@### cloakAttribMsg.ShowFP="+cloakAttribMsg.ShowFP);

        Log.d(TAG, "@@@### cloakAttribMsg.time_year="+cloakAttribMsg.time_year);
        Log.d(TAG, "@@@### cloakAttribMsg.time_month="+cloakAttribMsg.time_month);
        Log.d(TAG, "@@@### cloakAttribMsg.time_date="+cloakAttribMsg.time_date);
        Log.d(TAG, "@@@### cloakAttribMsg.time_hour="+cloakAttribMsg.time_hour);
        Log.d(TAG, "@@@### cloakAttribMsg.time_minute="+cloakAttribMsg.time_minute);
        Log.d(TAG, "@@@### cloakAttribMsg.time_second="+cloakAttribMsg.time_second);
        Log.d(TAG, "@@@### cloakAttribMsg.time_week="+cloakAttribMsg.time_week);

        Log.d(TAG, "@@@### cloakAttribMsg.FpRandomParam_enable="+cloakAttribMsg.FpRandomParam_enable);
        Log.d(TAG, "@@@### cloakAttribMsg.FpRandomParam_x="+cloakAttribMsg.FpRandomParam_x);
        Log.d(TAG, "@@@### cloakAttribMsg.FpRandomParam_y="+cloakAttribMsg.FpRandomParam_y);
        Log.d(TAG, "@@@### cloakAttribMsg.FpRandomParam_fcol="+cloakAttribMsg.FpRandomParam_fcol);
        Log.d(TAG, "@@@### cloakAttribMsg.FpRandomParam_bcol="+cloakAttribMsg.FpRandomParam_bcol);
    }

    public static TVMessage InitCloakMailUniId(long uniId){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_MAIL);
        msg.u32UniIdMail=uniId;
        return msg;
    }
    public long getCloakMailUniId() {return u32UniIdMail;}

    public static TVMessage InitCloakAttribUniId(long uniId){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_ATTRIB);
        msg.u32UniIdAttrib=uniId;
        return msg;
    }
    public long getCloakAttribUniId() {return u32UniIdAttrib;}

    public static TVMessage InitCloakFingerprint(long fcol,long bcol,int Type,int flash,int duration,String fingerprint) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_FINGERPRINT);
        msg.cloakFingerprint = new CloakFingerprint(fcol,bcol,Type,flash,duration,fingerprint);
        return  msg;
    }

    public static class CloakFingerprint implements Parcelable{
        public static final int TYPE_OVERT = 0;
        public static final int TYPE_COVERT = 1;
        public int rect_x;
        public int rect_y;
        public int rect_dx;
        public int rect_dy;

        public long fcol;
        public long bcol;

        public int type; // 0:overt 1:covert
        public int flash;
        public int duration;

        public int row;
        public int column;

        //type = 0(overt) ==> Use the content not the covert
        //type = 1(covert) ==> Use the covert not the content
        public byte[][] covert = new byte[0][0];
        public String content = null;

        public CloakFingerprint(long fcol,long bcol,int type,int flash,int duration,String fingerprint){
            this.fcol = fcol;
            this.bcol = bcol;
            this.type = type;
            this.flash = flash;
            this.duration = duration;
            this.content = fingerprint;
        }

        protected CloakFingerprint(Parcel in) {
            rect_x = in.readInt();
            rect_y = in.readInt();
            rect_dx = in.readInt();
            rect_dy = in.readInt();
            fcol = in.readLong();
            bcol = in.readLong();
            type = in.readInt();
            flash = in.readInt();
            duration = in.readInt();
            row = in.readInt();
            column = in.readInt();
            int outerLen = in.readInt();
            if (outerLen > 0) {
                covert = new byte[outerLen][];
                for (int i = 0; i < outerLen; i++) {
                    covert[i] = in.createByteArray(); // 讀取每個內層數組
                }
            } else {
                covert = new byte[0][0];
            }
            content = in.readString();
        }

        public static final Creator<CloakFingerprint> CREATOR = new Creator<CloakFingerprint>() {
            @Override
            public CloakFingerprint createFromParcel(Parcel in) {
                return new CloakFingerprint(in);
            }

            @Override
            public CloakFingerprint[] newArray(int size) {
                return new CloakFingerprint[size];
            }
        };

        public void setCloakRect(int x, int y, int dx, int dy){
            this.rect_x = x;
            this.rect_y = y;
            this.rect_dx = dx;
            this.rect_dy = dy;
        }

        public void setCloakCovert(int x,int y,byte[][] covert){
            this.covert = new byte[x][y];
            this.covert = covert;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(rect_x);
            dest.writeInt(rect_y);
            dest.writeInt(rect_dx);
            dest.writeInt(rect_dy);
            dest.writeLong(fcol);
            dest.writeLong(bcol);
            dest.writeInt(type);
            dest.writeInt(flash);
            dest.writeInt(duration);
            dest.writeInt(row);
            dest.writeInt(column);
            if (covert != null && covert.length > 0 && covert[0] != null) {
                dest.writeInt(covert.length); // 寫入外層數組長度
                for (byte[] array : covert) {
                    dest.writeByteArray(array != null ? array : new byte[0]); // 寫入每個內層數組
                }
            } else {
                dest.writeInt(0); // 空數組
            }
            dest.writeString(content);
        }
    }

    public CloakFingerprint getCloakFingerprint() {return cloakFingerprint;}

    public static TVMessage InitCloakSlide(long bcol,int flash,int duration,String slide) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_SLIDE);
        msg.cloakSlide = new CloakSlide(bcol,flash,duration,slide);
        return  msg;
    }

    public static class CloakSlide implements Parcelable{
        public long bcol;// background color
        public int flash;// flash marquee
        public int duration;
        public String content = null;

        public CloakSlide(long bcol,int flash,int duration,String slide){
            this.bcol = bcol;
            this.flash = flash;
            this.duration = duration;
            this.content = slide;
        }

        protected CloakSlide(Parcel in) {
            bcol = in.readLong();
            flash = in.readInt();
            duration = in.readInt();
            content = in.readString();
        }

        public static final Creator<CloakSlide> CREATOR = new Creator<CloakSlide>() {
            @Override
            public CloakSlide createFromParcel(Parcel in) {
                return new CloakSlide(in);
            }

            @Override
            public CloakSlide[] newArray(int size) {
                return new CloakSlide[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeLong(bcol);
            dest.writeInt(flash);
            dest.writeInt(duration);
            dest.writeString(content);
        }
    }
    public static TVMessage InitCloakEMMDownload(int emm_handle,int dl_allowed,int ForcedDownload,int ProfdecForcedDownload){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_EMM_DOWNLOAD);
        msg.EMMDownloadData = new CloakEMMDownload(emm_handle,dl_allowed,ForcedDownload,ProfdecForcedDownload);
        return msg;
    }

    public static class CloakEMMDownload implements Parcelable{
        public int emm_handle;
        public int dl_allowed;
        public int ForcedDownload;
        public int ProfdecForcedDownload;
        public CloakEMMDownload(int emm_handle,int dl_allowed,int ForcedDownload,int ProfdecForcedDownload){
            this.emm_handle = emm_handle;
            this.dl_allowed = dl_allowed;
            this.ForcedDownload = ForcedDownload;
            this.ProfdecForcedDownload = ProfdecForcedDownload;
        }

        protected CloakEMMDownload(Parcel in) {
            emm_handle = in.readInt();
            dl_allowed = in.readInt();
            ForcedDownload = in.readInt();
            ProfdecForcedDownload = in.readInt();
        }

        public static final Creator<CloakEMMDownload> CREATOR = new Creator<CloakEMMDownload>() {
            @Override
            public CloakEMMDownload createFromParcel(Parcel in) {
                return new CloakEMMDownload(in);
            }

            @Override
            public CloakEMMDownload[] newArray(int size) {
                return new CloakEMMDownload[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(emm_handle);
            dest.writeInt(dl_allowed);
            dest.writeInt(ForcedDownload);
            dest.writeInt(ProfdecForcedDownload);
        }
    }

    public static TVMessage InitCloakEMMChangePin(int pin_code){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_EMM_CHANGEPIN);
        msg.u32EMMPinCode = pin_code;
        return msg;
    }

    public static TVMessage InitCloakRetIRD(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_RESET_IRD);
        return msg;
    }
    public static TVMessage InitCloakRescan(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_RE_SCAN_SI_RESET_ORDER);
        return msg;
    }
    public static TVMessage InitCloakEraseSI(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_ERASE_SI_RESET);
        return msg;
    }

    public static TVMessage InitCloakReconnectService(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_RECONNECT_TO_SERVICE);
        return msg;
    }

    public static TVMessage InitCloakForceTune(int serviceId,int original_network_id,int transport_stream_id) {
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_FORCE_TUNE);
        msg.ForceTuneData = new CloakForceTune(serviceId,original_network_id,transport_stream_id);
        return  msg;
    }
    public static class CloakForceTune implements Parcelable{

        public int serviceId;
        public int original_network_id;
        public int transport_stream_id;

        public CloakForceTune(int serviceId,int original_network_id,int transport_stream_id){
            this.serviceId = serviceId;
            this.original_network_id = original_network_id;
            this.transport_stream_id = transport_stream_id;
        }

        protected CloakForceTune(Parcel in) {
            serviceId = in.readInt();
            original_network_id = in.readInt();
            transport_stream_id = in.readInt();
        }

        public static final Creator<CloakForceTune> CREATOR = new Creator<CloakForceTune>() {
            @Override
            public CloakForceTune createFromParcel(Parcel in) {
                return new CloakForceTune(in);
            }

            @Override
            public CloakForceTune[] newArray(int size) {
                return new CloakForceTune[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(serviceId);
            dest.writeInt(original_network_id);
            dest.writeInt(transport_stream_id);
        }
    }

    public static TVMessage InitCloakCustomerCardon(int type,int page_number,String text){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CUSTOMER_CARD_ONOFF);
        msg.Customer_cardon = new CloakCustomer_cardon(type,page_number,text);
        return msg;
    }
    public static class CloakCustomer_cardon implements Parcelable{

        public int type;
        public int page_number;
        public String content = null;

        public CloakCustomer_cardon(int type,int page_number,String text){
            this.type = type;
            this.page_number = page_number;
            this.content = text;
        }

        protected CloakCustomer_cardon(Parcel in) {
            type = in.readInt();
            page_number = in.readInt();
            content = in.readString();
        }

        public static final Creator<CloakCustomer_cardon> CREATOR = new Creator<CloakCustomer_cardon>() {
            @Override
            public CloakCustomer_cardon createFromParcel(Parcel in) {
                return new CloakCustomer_cardon(in);
            }

            @Override
            public CloakCustomer_cardon[] newArray(int size) {
                return new CloakCustomer_cardon[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeInt(page_number);
            dest.writeString(content);
        }
    }
    public static TVMessage InitCustomerDisplayPID(int type){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CUSTOMER_DISPLAY_PID);
        msg.Customer_display_pid=type;
        return msg;
    }

    public static TVMessage InitCustomerInfoChannel(int landing_service){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CUSTOMER_SET_INFO_CHANNEL);
        msg.Customer_landing_service=landing_service;
        return msg;
    }

    public static TVMessage InitCustomerSetLCN(int lcn){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CUSTOMER_SET_LCN);
        msg.Customer_landing_LCN=lcn;
        return msg;
    }

    public CloakSlide getCloakSlide() {return cloakSlide;}

    public static TVMessage InitTmsDataChanged(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_STATUS_TMS_DATA_CHANGED);
        return msg;
    }

    public static TVMessage InitNationalityChanged(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_STATUS_NATIONALITY_CHANGED);
        return msg;
    }

    public static TVMessage InitFlexiflashMessage(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_STATUS_FLEXIFLASH_MESSAGE);
        return msg;
    }

    public static TVMessage InitIfcpImageMessage(){
        TVMessage msg = new TVMessage(FLAG_CLOAK, TYPE_CLOAK_STATUS_IFCP_IMAGE_MESSAGE);
        return msg;
    }
    //Irdeto Cloak end

    public int getErrCode()
    {
        return errCode;
    }
    public String getMessage() {
        return message;
    }

    public static TVMessage StartTimeSync() {
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_TIME_SYNC);
        return msg;
    }

    public static TVMessage setNitIdToACS(int nit_id) {
        TVMessage msg = new TVMessage(FLAG_ACS, TYPE_ACS_SET_NIT_ID_TO_ASC);
        msg.nit_id = nit_id;
        return msg;
    }

    public static TVMessage SetTickerReady() {
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_CNS_TICKER_READY);
        return msg;
    }
}
