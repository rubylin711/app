package com.prime.dtv.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.PvrInfo;

/**
 * Created by ethan_lin on 2017/10/30.
 */

public class TVMessage/* implements Parcelable */{
    private static final String TAG = "TVMessage";
    public Bitmap subtitle_bitmap;

    private int type;
    private int flag;

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


    //Scan process data
    private int serviceId;
    private int serviceLCN;
    private int serviceType;
    private long channelId;
    private String serviceName;
    private int CAFlag;
    private int AlreadyScanedTpNum;
    private int serviceCHNum;
    private int tpId;
    private int percent;
    //Scan result data
    private int total_tv;
    private int total_radio;
    //CA Message
    private String Error_Code;
    private String CA_Msg_String;
    //Tuner Lock Status
    private int TunerLock;
    //AV Lock Status
    private int AVlockStatus;
    private int Rating;
    private int Finish;
    private int AvFrameStatus;
    private long AvFrameChannelId;//eric lin 20180803 no video signal
    //PVR
    private int recId;//eric lin 20180713 pvr msg
    private int recChannelId;
    private int pvrMode;
    private PvrInfo.EnPlaySpeed pvrSpeed;

    //VMX
    private int CAMsgMode;
    private String CAmsg;
    private int CAMsgDuration;

    private int CAWaterMarkMode;
    private String CAWaterMarkMsg;
    private int CAWaterMarkDur;

    private int OTAMode;
    private int VMXPinOnOff;
    private long VMXPinChannelID;
    private int VMXPinIndex;
    private int VMXPinTextSelector;

    private long IPPV_ChannelID;
    private int IPPV_OnOff;
    private String IPPV_CurToken;
    private String IPPV_cost;
    private int IPPV_PinIndex;

    private int VMX_BcioNotify_type;

    private int CardDetect;

    private int VMXSearchMode;
    private int VMXSearchFreq;
    private int VMXSearchsym;

    private int VMXTriggerID;
    private int VMXTriggerNum;

    //LoaderDtv
    private int loaderdtv_dLStatus; // 0 is no lock , 1 is lock not service, 2 is service found

    //PIO
    private int pioUsbOverloadPort;

    //DQA auto test
    private int dqaAutoTestFlag; // 1 = in auto test, 0 = not in auto test

    //Mtest
    private int cmdId;//Scoty 20190410 add Mtest Pc Tool callback
    private int errCode;//Scoty 20190410 add Mtest Pc Tool callback

    //test
    private int testParam1;
    private int testParam2;
    private String testName;

    //widevine 
    //eric lin 20210107 widevine cas, -start
    private int WvEventId;
    private byte[] WvMsg;
    private int sessionMode;
    private int wv_index;
    private int es_pid;
    private int ecm_pid;
    private widevineMsg WvMsgData;
    //eric lin 20210107 widevine cas, -end
    private String message;


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
    public static TVMessage SetSubtitleBitmap(Bitmap bitmap){
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_SUBTITLE_BIMAP);
        msg.subtitle_bitmap = bitmap;
        return msg;
    }
    public static TVMessage SetVideoStatus(int avFrameStatus, long channelId){//eric lin 20180803 no video signal
        TVMessage msg = new TVMessage(FLAG_AV, TYPE_AV_FRAME_PLAY_STATUS);
        msg.AvFrameStatus = avFrameStatus;
        msg.AvFrameChannelId = channelId;//eric lin 20180803 no video signal
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

    public static TVMessage SetPvrRecordStart(int param1, int param2,int param3){//eric lin 20180713 pvr msg,-start
        /*
        Log.d(TAG, "SetPvrRecordStart: param1="+param1+", param2="+param2);
        TVMessage msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORD_START);
        if(param1 == 0)
            msg.recId = param2;
        else
            msg.recId = -1;
        */
        Log.d(TAG, "SetPvrRecordStart: param1="+param1+" param2"+param2);
        TVMessage msg;
        if(param1 == 0)
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORDING_START_SUCCESS);
        else
            msg = new TVMessage(FLAG_PVR, TYPE_PVR_RECORDING_START_ERROR);

        msg.recId=param2;
        msg.recChannelId=param3;
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
            PvrInfo.EnPlaySpeed enPlaySpeed=PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
            msg.pvrSpeed = enPlaySpeed.convertSpeed(speed);
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
            PvrInfo.EnPlaySpeed enPlaySpeed=PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
            msg.pvrSpeed = enPlaySpeed.convertSpeed(speed);
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
            PvrInfo.EnPlaySpeed enPlaySpeed=PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
            msg.pvrSpeed = enPlaySpeed.convertSpeed(speed);
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
            PvrInfo.EnPlaySpeed enPlaySpeed=PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
            msg.pvrSpeed = enPlaySpeed.convertSpeed(speed);
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

    public static TVMessage SetShowErrorMsg(int errorCode, String message) {
        TVMessage msg = new TVMessage(FLAG_SYSTEM, TYPE_SYSTEM_SHOW_ERROR_MESSAGE);
        msg.errCode = errorCode;
        msg.message = message;
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

    public static class widevineMsg {
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
    }
    //eric lin 20210107 widevine cas, -end

    public int getErrCode()
    {
        return errCode;
    }
    public String getMessage() {
        return message;
    }
}
