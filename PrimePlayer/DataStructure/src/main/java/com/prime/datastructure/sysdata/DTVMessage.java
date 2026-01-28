package com.prime.datastructure.sysdata;


public class DTVMessage
{
    //Scoty 20180831 modify new callback event id -s
////////////////////////AV play/////////////////////////////
    public static final int PESI_SVR_EVT_AV_CALLBACK_START = 0x00010001 ;
    /**
     * play stop event,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_STOP = 0x00010001;

    /**
     * play success event,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_PLAY_SUCCESS = 0x00010002;

    /**
     * play failure event,first parameter is failure reason.<br>
     * 1--lock fail,2--video attribute error,4--video PID error,8--video play error,<br>
     * 16--audio attribute error,32--audio PID error,64--audio play error.<br>
     */
    public static final int PESI_SVR_EVT_AV_PLAY_FAILURE = 0x00010003;

    /**
     *CA status event,first parameter is CA status, 1 is scramble 0 is FTA.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_CA = 0x00010004;

    /**
     *front send stop event,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_FRONTEND_STOP = 0x00010005;

    /**
     *front send resume event,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_FRONTEND_RESUME = 0x00010006;

    /**
     *play signal change event,first parameter is signal status, 1 is Lock 0 is UnLock.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_SIGNAL_STAUTS = 0x00010007;

    /**
     *motor move event,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_MOTOR_MOVING = 0x00010008;

    /**
     *motor stop event,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_MOTOR_STOP = 0x00010009;

    /**
     *video and audio channel scrambled status. first parameter is scrambled status，1 audio channel scrambled,2 video channel scrambled.<br>
     *
     */
    public static final int PESI_SVR_EVT_AV_CHANNEL_SCRAMBLED = 0x0001000A;

    public static final int PESI_SVR_EVT_AV_CHANNEL_LOCKED = 0x0001000B;

    public static final int PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP = 0x0001000C;
    public static final int PESI_SVR_EVT_AV_DECODER_ERROR = 0x0001000D;
    public static final int PESI_SVR_EVT_AV_CLOSED_CAPTION_ENABLE = 0x0001000E;
    public static final int PESI_SVR_EVT_AV_CLOSED_CAPTION_DATA = 0x0001000F;

    public static final int PESI_SVR_EVT_FIRST_FRAME = 0x00010010 ; //tifcheck
    public static final int PESI_SVR_EVT_VideoUnavailable = 0x00010011 ;
    public static final int PESI_SVR_EVT_AV_CALLBACK_END = 0x00010012 ;
    public static final int PESI_SVR_EVT_AV_FCC_VISIBLE = 0x00010013 ;
    ////////////////////////EPG/////////////////////////////
    public static final int PESI_SVR_EVT_EPG_CALLBACK_START = 0x00020001 ;
    /**
     * PF event version changed of current program, first parameter is progRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_PF_VERSION_CHANGED = 0x00020001;

    /**
     * All PF events searched finish of current program, first parameter is progRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH = 0x00020002;

    /**
     * All PF events searched finish of current frequency, first parameter is tpRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH = 0x00020003;

    /**
     * All PF events of all programs searched finish,no parameter. <br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_PF_ALL_FINISH = 0x00020004;

    /**
     * Schedule event version changed of current program, first parameter is progRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_SCH_VERSION_CHANGED = 0x00020005;

    /**
     * All schedule events searched finish of current program, first parameter is progRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH = 0x00020006;

    /**
     * All schedule events searched finish of current frequency, first parameter is tpRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH = 0x00020007;

    /**
     * All schedule events searched finish,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_SCH_ALL_FINISH = 0x00020008;

    /**
     * EPG event parental rating notification of current program, first parameter is progRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EPG_PARENTAL_RATING = 0x00020009;
    public static final int PESI_SVR_EVT_EPG_CALLBACK_END = 0x0002000A ;
    //////////////////////////////search///////////////////////////////////
    public static final int PESI_SVR_EVT_SRCH_CALLBACK_START = 0x00030001 ;
    /**
     * search start event,first parameter is deliver row ID.<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_BEGIN = 0x00030001;

    /**
     * Start to lock the frequency;the parameter:param1 is frequency,param2 is symbol rate.<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_LOCK_START = 0x00030002;

    /**
     * Send to lock status of tuner;the parameter:param1 is 1 mean to locked.<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_LOCK_STATUS = 0x00030003;

    /**
     * one freq's table search finish,first parameter is frequency row ID.<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_CUR_FREQ_TABLE_FINISH = 0x00030004;

    /**
     * The info of current locked;the parameter:param1 is frequency,param2 is
     * ID of frequency(use it can get the other information).<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_CUR_FREQ_INFO = 0x00030005;

    /**
     * Send to the current process of scan;the parameter:param1 is process(0..100).<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_CUR_SCHEDULE = 0x00030006;

    /**
     * Finish the scan of one frequency;the parameter:param1 is the number of new find
     * program,param2 is number of droped.<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_ONE_FREQ_FINISH = 0x00030007;

    /**
     * the message that success to finish scan；parameter:param1 is total frequency number,
     *  param2 is have program frequency number.<br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_FINISH = 0x00030008;

    /**
     * To search for new program.the parameter:param1 is id of frequency,param2 is id of channel. <br>
     *
     */
    public static final int PESI_SVR_EVT_SRCH_GET_PROG = 0x00030009;
    /**
     *  To search for new program.the parameter:param1 is id of frequency,param2 is id of channel. obj is ChannelNode
     */
    public static final int PESI_SVR_EVT_SRCH_GET_PROG_PESI = 0x0003000A ;
    public static final int PESI_SVR_EVT_SRCH_CALLBACK_END = 0x0003000B ;
    ////////////////////////Book/////////////////////////////
    public static final int PESI_SVR_EVT_BOOK_CALLBACK_START = 0x00040001 ;
    /**
     * Remind time of book task has been added, first parameter is bookRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_BOOK_REMIND = 0x00040001;

    /**
     * Start time of book task is arrived, first parameter is bookRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_BOOK_TIME_ARRIVE = 0x00040002;

    /**
     * End time of book task is arrived, first parameter is bookRowID.<br>
     *
     */
    public static final int PESI_SVR_EVT_BOOK_TIME_END = 0x00040003;

    public static final int PESI_SVR_EVT_BOOK_RUN_AD = 0x00040004;

    public static final int PESI_SVR_EVT_BOOK_CALLBACK_END = 0x00040005 ;

    ////////////////////////time shift and record////////////////////////
    public static final int PESI_SVR_EVT_PVR_CALLBACK_START = 0x00050001 ;
    /**
     *time shift status forward to end of a file,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_PVR_PLAY_EOF = 0x00050001;

    /**
     *time shift status backward to head of a file,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_PVR_PLAY_SOF = 0x00050002;

    /**
     * time shift message, play back fail,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_PVR_PLAY_ERROR = 0x00050003;

    /**
     *time shift message, laying speed reaches the recording speed ,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_PVR_PLAY_REACH_REC = 0x00050004;

    /**
     *record message, disk full,no parameter.<br>
     *
     */
    public static final int PESI_SVR_EVT_PVR_REC_DISKFULL = 0x00050005;
    /**
     * Recording errors, time shift and recording total message.<br>
     *
     */
    public static final int PESI_SVR_EVT_PVR_REC_ERROR = 0x00050006;

    /**
     * time shift and recording total message.<br>
     * in recording situation,receive the message when schedule time has reached.<br>
     * in time shift situation, acyclic buffer condition,<br>
     * this message is generated when the specified time is reached or the designated space run out.<br>
     */
    public static final int PESI_SVR_EVT_PVR_REC_OVER_FIX = 0x00050007;

    /**
     * time shift message,cycle buffer condition,this message is generated when recording again to catch up on the play.<br>
     *
     */
    public static final int PESI_SVR_EVT_PVR_REC_REACH_PLAY = 0x00050008;

    /**
     * the storage speed of the disk is slower than the recording speed,time shift and recording total message.<br>
     * in recording situation, this message is generated when the recorded file will run out of storage media space.<br>
     * in time shift situation,acyclic buffer condition,this message is generated when the recorded file will run out of storage media space.<br>
     */
    public static final int PESI_SVR_EVT_PVR_REC_DISK_SLOW = 0x00050009;
    public static final int PESI_EVT_PVR_RECORD_START = 0x0005000A;//eric lin 20180713 pvr msg,-start
    public static final int PESI_EVT_PVR_RECORD_STOP = 0x0005000B;
    public static final int PESI_EVT_PVR_TIMESHIFT_START = 0x0005000C;
    public static final int PESI_EVT_PVR_TIMESHIFT_PLAY_START = 0x0005000D;
    public static final int PESI_EVT_PVR_TIMESHIFT_STOP = 0x0005000E;
    public static final int PESI_EVT_PVR_FILE_PLAY_START = 0x0005000F;
    public static final int PESI_EVT_PVR_FILE_PLAY_STOP = 0x00050010;
    public static final int PESI_EVT_PVR_PLAY_PARENTAL_LOCK = 0x00050011;
    public static final int PESI_SVR_EVT_PVR_OPEN_HD_FINISH = 0x00050012;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP = 0x00050013;
    public static final int PESI_SVR_EVT_PVR_NOT_SUPPORT = 0x00050014;
    public static final int PESI_SVR_EVT_PVR_SUCCESS = 0x00050015;
    public static final int PESI_SVR_EVT_PVR_RECORDING_SUCCESS = 0x00050016;
    public static final int PESI_SVR_EVT_PVR_RECORDING_STOP_SUCCESS = 0x00050017;
    public static final int PESI_SVR_EVT_PVR_PLAYBACK_SUCCESS = 0x00050018;
    public static final int PESI_SVR_EVT_PVR_PLAYBACK_STOP_SUCCESS = 0x00050019;
    public static final int PESI_SVR_EVT_PVR_RECORDING_STOP_ERROR = 0x0005001A;
    public static final int PESI_SVR_EVT_PVR_PLAYBACK_STOP_ERROR = 0x0005001B;
    public static final int PESI_SVR_EVT_PVR_PLAY_RESUME_ERROR = 0x0005001C;
    public static final int PESI_SVR_EVT_PVR_PLAY_RESUME_SUCCESS = 0x0005001D;
    public static final int PESI_SVR_EVT_PVR_PLAY_PAUSE_ERROR = 0x0005001E;
    public static final int PESI_SVR_EVT_PVR_PLAY_PAUSE_SUCCESS = 0x0005001F;
    public static final int PESI_SVR_EVT_PVR_PLAY_FF_ERROR = 0x00050020;
    public static final int PESI_SVR_EVT_PVR_PLAY_FF_SUCCESS = 0x00050021;
    public static final int PESI_SVR_EVT_PVR_PLAY_RW_ERROR = 0x00050022;
    public static final int PESI_SVR_EVT_PVR_PLAY_RW_SUCCESS = 0x00050023;
    public static final int PESI_SVR_EVT_PVR_PLAY_SEEK_ERROR = 0x00050024;
    public static final int PESI_SVR_EVT_PVR_PLAY_SEEK_SUCCESS = 0x00050025;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_SUCCESS = 0x00050026;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_ERROR = 0x00050027;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_STOP_SUCCESS = 0x00050028;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_STOP_ERROR = 0x00050029;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_SUCCESS = 0x0005002A;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_ERROR = 0x0005002B;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_ERROR = 0x0005002C;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_SUCCESS = 0x0005002D;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_ERROR = 0x0005002E;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_SUCCESS = 0x0005002F;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_FF_ERROR = 0x00050030;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_FF_SUCCESS = 0x00050031;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_RW_ERROR = 0x00050032;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_RW_SUCCESS = 0x00050033;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_ERROR = 0x00050034;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_SUCCESS = 0x00050035;
    public static final int PESI_SVR_EVT_PVR_RECORDING_COMPLETED = 0x00050036;
    public static final int PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR = 0x00050037;
    public static final int PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_SUCCESS = 0x00050038;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR = 0x00050039;
    public static final int PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_SUCCESS = 0x00050040;
    public static final int PESI_SVR_EVT_PVR_PLAY_SET_SPEED_ERROR = 0x00050041;
    public static final int PESI_SVR_EVT_PVR_PLAY_SET_SPEED_SUCCESS = 0x00050042;
    public static final int PESI_SVR_EVT_PVR_CALLBACK_END = 0x00050043;//Scoty 20180827 add and modify TimeShift Live Mode//eric lin 20180713 pvr msg,-end

    public static final int PESI_SVR_EVT_PU_RESCAN = 0x00060001;
    public static final int PESI_SVR_EVT_PU_RESCAN_CURRENT_TP = 0x00060002;
    public static final int PESI_SVR_EVT_PU_PROG_UPDATED = 0x00060003;

    public static final int PESI_SVR_EVT_EWS_CALLBACK_START = 0x00070001 ;
    /**
     * EWS start message, param1 is reserved, param2 is ChannelID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EWS_START = 0x00070001;

    /**
     * EWS stop message, param1 is reserved, param2 is ChannelID.<br>
     *
     */
    public static final int PESI_SVR_EVT_EWS_STOP = 0x00070002;
    public static final int PESI_SVR_EVT_EWS_CALLBACK_END = 0x00070003 ;

    /**
     * VMX message start<br>
     */
    public static final int PESI_SVR_EVT_VMX_CALLBACK_START   = 0x00080001;
    public static final int PESI_SVR_EVT_VMX_SHOW_MSG         = 0x00080001;
    public static final int PESI_SVR_EVT_VMX_OTA_START        = 0x00080002;
    public static final int PESI_SVR_EVT_VMX_OTA_ERR          = 0x00080003;
    public static final int PESI_SVR_EVT_VMX_WATERMARK        = 0x00080004;
    public static final int PESI_SVR_EVT_VMX_WATERMARK_CLOSE  = 0x00080005;
    public static final int PESI_SVR_EVT_VMX_PIN              = 0x00080006;
    public static final int PESI_SVR_EVT_VMX_IPPV             = 0x00080007;
    public static final int PESI_SVR_EVT_CARD_DETECT          = 0x00080008;
    public static final int PESI_SVR_EVT_VMX_SEARCH           = 0x00080009;
    public static final int PESI_EVT_VMX_SCRAMBLED =0x0008000A;
    public static final int PESI_EVT_VMX_BCIO_NOTIFY =0x0008000B;
    public static final int PESI_SVR_EVT_EWBS  = 0x0008000C;
    public static final int PESI_SVR_EVT_VMX_E16  = 0x0008000D;
    public static final int PESI_SVR_EVT_VMX_MAIL  = 0x0008000E;
    public static final int PESI_SVR_EVT_VMX_FACTORY  = 0x0008000F;
    public static final int PESI_SVR_EVT_VMX_CHBLOCK  = 0x00080010;
    public static final int PESI_SVR_EVT_VMX_OTA_PROC = 0x00080011;
    public static final int PESI_SVR_EVT_VMX_EWBS_STOP = 0x00080012;//Scoty 20181225 modify VMX EWBS rule
    public static final int PESI_SVR_EVT_VMX_CALLBACK_END     = 0x00080013;

    public static final int PESI_SVR_EVT_CLOAK_CALLBACK_START = 0x00081001;
    public static final int PESI_SVR_EVT_CA_IRDETO_MSG = 0x00081001;
    public static final int PESI_SVR_EVT_IRDETO_NOTIFY_CURRENT_ERROR = 0x00000001;
    public static final int PESI_SVR_EVT_IRDETO_SERVICE_GROUP_INFO = 0x00000002;
    public static final int PESI_SVR_EVT_IRDETO_EMM_SERVICE_INFO = 0x00000003;
    public static final int PESI_SVR_EVT_IRDETO_MONITOR_ECM = 0x00000004;
    public static final int PESI_SVR_EVT_IRDETO_MONITOR_EMM = 0x00000005;
    public static final int PESI_SVR_EVT_IRDETO_CLIENT_STATUS = 0x00000006;
    public static final int PESI_SVR_EVT_IRDETO_EXTENDED_PRODUCT_LIST = 0x00000007;
    public static final int PESI_SVR_EVT_IRDETO_REGION_STATUS_LIST = 0x00000008;
    public static final int PESI_SVR_EVT_IRDETO_FSU_MSG = 0x00000009;
    public static final int PESI_SVR_EVT_CA_QC_EMM_DOWNLOAD = 0x0000000A;
    public static final int PESI_SVR_EVT_CA_QC_EMM_CHANGEPIN = 0x0000000B;
    public static final int PESI_SVR_EVT_IRDETO_ATTRIB_MSG = 0x0000000C;
    public static final int PESI_SVR_EVT_IRDETO_CLOAK_MAIL = 0x0000000D;
    public static final int PESI_SVR_EVT_IRDETO_CLOAK_ATTRIB = 0x0000000E;
    public static final int PESI_SVR_EVT_IRDETO_CLOAK_FINGERPRINT = 0x0000000F;
    public static final int PESI_SVR_EVT_IRDETO_CLOAK_SLIDE = 0x00000010;
    public static final int PESI_SVR_EVT_IRDETO_CLIENT_STATUS_TMS_DATA_CHANGED = 0x00000011;
    public static final int PESI_SVR_EVT_IRDETO_CLIENT_STATUS_NATIONALITY_CHANGED = 0x00000012;
    public static final int PESI_SVR_EVT_IRDETO_CLIENT_STATUS_FLEXIFLASH_MESSAGE = 0x00000013;
    public static final int PESI_SVR_EVT_IRDETO_CLIENT_STATUS_IFCP_IMAGE_MESSAGE = 0x00000014;

    
    public static final int PESI_SVR_EVT_CA_IRDETO_IRD = 0x00081002;
    public static final int PESI_SVR_EVT_RESET_IRD = 0x00000001;
    public static final int PESI_SVR_EVT_RE_SCAN_SI_RESET_ORDER = 0x00000002;
    public static final int PESI_SVR_EVT_RECONNECT_TO_SERVICE = 0x00000003;
    public static final int PESI_SVR_EVT_FORCE_TUNE_TO_SERVICE = 0x00000004;
    public static final int PESI_EVT_ERASE_SI_RESET_RETUNE = 0x00000005;
    public static final int PESI_EVT_CUSTOMER_CARD_ONOFF = 0x00000006;
    public static final int PESI_EVT_CUSTOMER_DISPLAY_PID = 0x00000007;
    public static final int PESI_EVT_CUSTOMER_SET_INFO_CHANNEL = 0x00000008;
    public static final int PESI_EVT_CUSTOMER_SET_LCN = 0x00000009;
 
    public static final int PESI_SVR_EVT_CLOAK_CALLBACK_END = 0x00081003;


    public static final int PESI_SVR_EVT_LOADERDTV_CALLBACK_START     = 0x00090001;
    public static final int PESI_LOADERDVT_RETURN_DSMCC_STATUS   =0x00090001;
    public static final int PESI_SVR_EVT_LOADERDTV_CALLBACK_END     = 0x00090002;

    /**
     * PIO message start
     */
    public static final int PESI_EVT_PIO_CALLBACK_START = 0x000A0001;
    public static final int PESI_EVT_PIO_FRONT_PANEL_KEY_CH_DOWN = 0x000A0001;
    public static final int PESI_EVT_PIO_FRONT_PANEL_KEY_POWER = 0x000A0002;
    public static final int PESI_EVT_PIO_FRONT_PANEL_KEY_CH_UP = 0x000A0003;
    public static final int PESI_EVT_PIO_USB_OVERLOAD = 0x000A0004;
    public static final int PESI_EVT_PIO_ANTENNA_OVERLOAD = 0x000A0005;
    public static final int PESI_EVT_PIO_CALLBACK_END = 0x000A0006;

    /**
     * System message start
     */
    public static final int PESI_EVT_SYSTEM_CALLBACK_START = 0x000B0001;
    public static final int PESI_EVT_SYSTEM_DQA_AUTO_TEST = 0x000B0001;
    public static final int PESI_EVT_SYSTEM_PMT_UPDATE_VERSION = 0x000B0002;
    public static final int PESI_EVT_SYSTEM_PMT_UPDATE_AV = 0x000B0003;
    public static final int PESI_EVT_SYSTEM_PMT_UPDATE_SUBTITLE = 0x000B0004;
    public static final int PESI_EVT_SYSTEM_START_MONITOR_TABLE = 0x000B0005;
    public static final int PESI_EVT_SYSTEM_STOP_MONITOR_TABLE = 0x000B0006;
    public static final int PESI_EVT_SYSTEM_SI_UPDATE_CHANNEL = 0x000B0007;
    public static final int PESI_EVT_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL = 0x000B0008;
    public static final int PESI_EVT_SYSTEM_SI_UPDATE_SET_CHANNEL = 0x000B0009;
    public static final int PESI_EVT_SYSTEM_SI_UPDATE_WVCAS_URL = 0x000B000A;
    public static final int PESI_EVT_SYSTEM_SI_UPDATE_TBC_BAT_WHITE_LIST_FAIL = 0x000B000B;
    public static final int PESI_EVT_SYSTEM_PMT_GET_RAW_DATA = 0x000B000C;
    public static final int PESI_EVT_SYSTEM_CAT_UPDATE_VERSION = 0x000B000D;
    public static final int PESI_EVT_SYSTEM_START_TIME_SYNC = 0x000B000E;
    public static final int PESI_EVT_SYSTEM_PMT_UPDATE_TELETEXT = 0x000B000F;
    public static final int PESI_EVT_SYSTEM_SET_NIT_ID_TO_ACS = 0x000B0010;
    public static final int PESI_EVT_SYSTEM_CNS_GET_TICKER_DATA = 0x000B0011;
    public static final int PESI_EVT_SYSTEM_CNS_GET_AD_DATA = 0x000B0012;
    public static final int PESI_EVT_SYSTEM_CALLBACK_END = 0x000B0013;

    public static final int PESI_SVR_EVT_TBM_CALLBACK_START = 0x00BC0013 ;
    /**
     * Section table update message, param1 is TableType see PESI_SVR_TBM_TABLE_TYPE_E, param2 is sizeof TableType. <br>
     *
     */
    public static final int PESI_SVR_EVT_TBM_UPDATE = PESI_SVR_EVT_TBM_CALLBACK_START+1;


    /**
     * Time update message, param1 is timeinfo，see PESI_TIME_INFO_S, param2 is sizeof PESI_TIME_INFO_S. <br>
     *
     */
    public static final int PESI_SVR_EVT_TIME_UPDATE = PESI_SVR_EVT_TBM_CALLBACK_START+2;

    /**
     * TimeZone update message, param1 is s32TimeZoneSeconds, param2 is sizeof s32TimeZoneSeconds. <br>
     *
     */
    public static final int PESI_SVR_EVT_TIMEZONE_UPDATE = PESI_SVR_EVT_TBM_CALLBACK_START+3;
    public static final int PESI_SVR_EVT_TBM_CALLBACK_END = PESI_SVR_EVT_TBM_CALLBACK_START+4 ;

    public static final int PESI_SVR_CI_EVT_CALLBACK_START = 0x008A0002 ;
    public static final int PESI_SVR_CI_EVT_CARD_INSERT = 0x008A0002;
    public static final int PESI_SVR_CI_EVT_CARD_REMOVE = 0x008A0003;
    public static final int PESI_SVR_CI_EVT_MMI_MESSAGE = 0x008A0004;
    public static final int PESI_SVR_CI_EVT_MMI_MENU = 0x008A0005;
    public static final int PESI_SVR_CI_EVT_MMI_LIST= 0x008A0006;
    public static final int PESI_SVR_CI_EVT_MMI_ENQ= 0x008A0007;
    public static final int PESI_SVR_CI_EVT_CLOSE_MMI= 0x008A0008;
    public static final int PESI_SVR_CI_EVT_CALLBACK_END = 0x008A0009 ;

    public static final int PESI_SVR_EVT_SSU_UPDATE = 0x008B0002;
    public static final int PESI_SVR_EVT_SSU_TIMEOUT = 0x008B0003;
    public static final int PESI_SVR_EVT_SSU_DOWNLOAD_SCHEDULE = 0x008B0004;
    public static final int PESI_SVR_EVT_SSU_DOWNLOAD_ERROR = 0x008B0005;
    public static final int PESI_SVR_EVT_SSU_DOWNLOAD_FINISH = 0x008B0006;

    //Scoty 20180831 modify new callback event id -e
    /**
     * ATV message start value.<br>
     *
     */
    public static final int PESI_ATV_EVT_START    = 0x00C00000;

    /**
     * ATV signal status.<br>
     *
     */
    public static final int PESI_ATV_EVT_SIGNAL_STATUS = 0x00C00001;

    /**
     * Select source coomplete.<br>
     *
     */
    public static final int PESI_ATV_EVT_SELECT_SOURCE_COMPLETE = 0x00C00002;

    /**
     * begin scanning.<br>
     *
     */
    public static final int PESI_ATV_EVT_SCAN_BEGIN = 0x00C00003;

    /**
     * scan schedule.<br>
     *
     */
    public static final int PESI_ATV_EVT_SCAN_PROGRESS = 0x00C00004;

    /**
     * found a valid frequency.<br>
     *
     */
    public static final int PESI_ATV_EVT_SCAN_LOCK = 0x00C00005;

    /**
     * scan finished.<br>
     *
     */
    public static final int PESI_ATV_EVT_SCAN_FINISH = 0x00C00006;

    /**
     * timming change.<br>
     *
     */
    public static final int PESI_ATV_EVT_TIMMING_CHANGED = 0x00C00007;

    /**
     * device plug.<br>
     *
     */
    public static final int PESI_ATV_EVT_PLUGIN = 0x00C00008;

    /**
     * device plug out.<br>
     *
     */
    public static final int PESI_ATV_EVT_PLUGOUT = 0x00C00009;

    /**
     * pc_adjust_status.<br>
     *
     */
    public static final int PESI_ATV_EVT_PC_ADJ_STATUS = 0x00C0000A;

    /**
     * HDMI CEC.<br>
     *
     */
    public static final int PESI_ATV_EVT_CEC_SELECT_SOURCE = 0x00C0000B;

    /**
     * LOCK ChANGE.<br>
     *
     */
    public static final int PESI_ATV_EVT_PLAYER_LOCK_CHANGED = 0x00C0000C;

    /**
     * ATV message start<br>
     *
     */
    public static final int PESI_ATV_EVT_END      = 0x00C10000;

    /*
     * Mtest Callback Messages
     * */
    public static final int PESI_EVT_MTEST_START = 0x000D0001;
    public static final int PESI_EVT_MTEST_PCTOOL = 0x000D0001;//Scoty 20190410 add Mtest Pc Tool callback\
    public static final int PESI_EVT_MTEST_FPKEY_RESET = 0x000D0002;
    public static final int PESI_EVT_MTEST_FPKEY_WPS = 0x000D0003;
    public static final int PESI_EVT_MTEST_END = 0x000D0004;

    public static final int PESI_EVT_PT_DEBUG_CALLBACK_TEST = 0x000E0001;
    public static final int PESI_EVT_PT_DEBUG_END = 0x000E0002;

    public static final int PESI_EVT_CA_WIDEVINE = 0x000F0001;

    public static final int PESI_EVT_CA_WIDEVINE_REFRESH_CAS_DATA = 0x000F0002;
    public static final int PESI_EVT_CA_WIDEVINE_ERROR = 0x000F0003;
    public static final int PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE = 0x000F0004;
    public static final int PESI_EVT_CA_WIDEVINE_CA_MAIL = 0x000F0005;
    public static final int PESI_EVT_CA_WIDEVINE_EMERGENCY_ALARM = 0x000F0006;
    public static final int PESI_EVT_CA_WIDEVINE_IRD_COMMAND = 0x000F0007;
    public static final int PESI_EVT_CA_END = 0x000F0008;
    public static final int PESI_EVT_HIDL_MEMORY = 0x00100001;
    public static final int PESI_EVT_SERIES_UPDATE = 0x00110001;
    public static final int PESI_EVT_SERIES_END = 0x00110002;

    /**
     * OTA message start<br>
     *
     */

    public static final int PESI_EVT_OTA_START = 0x00120001;
    public static final int PESI_EVT_OTA_SHOW_UPDATE_COUNT_DOWN_DIALOG = 0x00120002;
    public static final int PESI_EVT_OTA_SHOW_UPDATE_DIALOG = 0x00120003;
    public static final int PESI_EVT_OTA_END = 0x00120004;
}
