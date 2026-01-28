package com.dolphin.dtv;


public class DTVMessage
{
    //Scoty 20180831 modify new callback event id -s
////////////////////////AV play/////////////////////////////
    public static final int HI_SVR_EVT_AV_CALLBACK_START = 0x00010001 ;
    /**
     * play stop event,no parameter.<br>
     * CN:播放停止消息通知，没有参数。<br>
     */
    public static final int HI_SVR_EVT_AV_STOP = 0x00010001;

    /**
     * play success event,no parameter.<br>
     * CN:播放成功消息通知，没有参数。<br>
     */
    public static final int HI_SVR_EVT_AV_PLAY_SUCCESS = 0x00010002;

    /**
     * play failure event,first parameter is failure reason.<br>
     * 1--lock fail,2--video attribute error,4--video PID error,8--video play error,<br>
     * 16--audio attribute error,32--audio PID error,64--audio play error.<br>
     * CN:播放失败消息通知，第一个参数是失败原因。<br>
     * 1--锁频失败，2--视频属性值错误，4--视频PID错误，8--视频播放错误，<br>
     * 16--音频属性值错误，32--音频PID错误，64--音频播放错误。<br>
     */
    public static final int HI_SVR_EVT_AV_PLAY_FAILURE = 0x00010003;

    /**
     *CA status event,first parameter is CA status, 1 is scramble 0 is FTA.<br>
     * CN:CA状态消息通知，第一个参数是CA状态，1代表加扰 0代表非加扰。<br>
     */
    public static final int HI_SVR_EVT_AV_CA = 0x00010004;

    /**
     *front send stop event,no parameter.<br>
     * CN:前端放送停止，没有参数。<br>
     */
    public static final int HI_SVR_EVT_AV_FRONTEND_STOP = 0x00010005;

    /**
     *front send resume event,no parameter.<br>
     * CN:前端放送恢复，没有参数。<br>
     */
    public static final int HI_SVR_EVT_AV_FRONTEND_RESUME = 0x00010006;

    /**
     *play signal change event,first parameter is signal status, 1 is Lock 0 is UnLock.<br>
     * CN:播放信号状态变化消息通知，第一个参数是信号状态，1代表锁定 0代表未锁定。<br>
     */
    public static final int HI_SVR_EVT_AV_SIGNAL_STAUTS = 0x00010007;

    /**
     *motor move event,no parameter.<br>
     * CN:马达移动消息通知，没有参数。<br>
     */
    public static final int HI_SVR_EVT_AV_MOTOR_MOVING = 0x00010008;

    /**
     *motor stop event,no parameter.<br>
     * CN:马达停止移动消息通知，没有参数。<br>
     */
    public static final int HI_SVR_EVT_AV_MOTOR_STOP = 0x00010009;

    /**
     *video and audio channel scrambled status. first parameter is scrambled status，1 audio channel scrambled,2 video channel scrambled.<br>
     * CN:音视频通道加密状态.第一个参数是通道加密状态。1 音频通道加密, 2 视频通道加密。<br>
     */
    public static final int HI_SVR_EVT_AV_CHANNEL_SCRAMBLED = 0x0001000A;

    public static final int HI_SVR_EVT_AV_CHANNEL_LOCKED = 0x0001000B;

    public static final int HI_SVR_EVT_AV_CALLBACK_END = 0x0001000C ;

    ////////////////////////EPG/////////////////////////////
    public static final int HI_SVR_EVT_EPG_CALLBACK_START = 0x00020001 ;
    /**
     * PF event version changed of current program, first parameter is progRowID.<br>
     * CN:当前频道PF信息版本更新消息，第一个参数为当前频道RowID。<br>
     */
    public static final int HI_SVR_EVT_EPG_PF_VERSION_CHANGED = 0x00020001;

    /**
     * All PF events searched finish of current program, first parameter is progRowID.<br>
     * CN:当前频道所有PF信息搜索完成，第一个参数为当前频道RowID。<br>
     */
    public static final int HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH = 0x00020002;

    /**
     * All PF events searched finish of current frequency, first parameter is tpRowID.<br>
     * CN:当前频点下所有PF信息搜索完成消息，第一个参数为当前频点RowID。<br>
     */
    public static final int HI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH = 0x00020003;

    /**
     * All PF events of all programs searched finish,no parameter. <br>
     * CN:所有频道的PF信息搜索完成消息,没有参数。<br>
     */
    public static final int HI_SVR_EVT_EPG_PF_ALL_FINISH = 0x00020004;

    /**
     * Schedule event version changed of current program, first parameter is progRowID.<br>
     * CN:当前频道节目事件版本变更消息，第一个参数为当前频道RowID。<br>
     */
    public static final int HI_SVR_EVT_EPG_SCH_VERSION_CHANGED = 0x00020005;

    /**
     * All schedule events searched finish of current program, first parameter is progRowID.<br>
     * CN:当前频道下所有节目事件搜索完成消息，第一个参数为当前频道RowID。<br>
     */
    public static final int HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH = 0x00020006;

    /**
     * All schedule events searched finish of current frequency, first parameter is tpRowID.<br>
     * CN:当前频点下所有节目事件搜索完成消息，第一个参数为当前频点RowID。<br>
     */
    public static final int HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH = 0x00020007;

    /**
     * All schedule events searched finish,no parameter.<br>
     * CN:所有频道的节目事件搜索完成消息,没有参数.<br>
     */
    public static final int HI_SVR_EVT_EPG_SCH_ALL_FINISH = 0x00020008;

    /**
     * EPG event parental rating notification of current program, first parameter is progRowID.<br>
     * CN:当前频道EPG节目事件父母锁通知消息，第一个参数为当前频点RowID。<br>
     */
    public static final int HI_SVR_EVT_EPG_PARENTAL_RATING = 0x00020009;
    public static final int HI_SVR_EVT_EPG_CALLBACK_END = 0x0002000A ;
    //////////////////////////////search///////////////////////////////////
    public static final int HI_SVR_EVT_SRCH_CALLBACK_START = 0x00030001 ;
    /**
     * search start event,first parameter is deliver row ID.<br>
     * CN:搜索启动消息，第一个参数是传输标识ID。<br>
     */
    public static final int HI_SVR_EVT_SRCH_BEGIN = 0x00030001;

    /**
     * Start to lock the frequency;the parameter:param1 is frequency,param2 is symbol rate.<br>
     * CN:开始锁频消息。参数param1是频点，param2是符号率。<br>
     */
    public static final int HI_SVR_EVT_SRCH_LOCK_START = 0x00030002;

    /**
     * Send to lock status of tuner;the parameter:param1 is 1 mean to locked.<br>
     * CN:发送锁频状态消息，参数param1值为1时标识锁频成功。<br>
     */
    public static final int HI_SVR_EVT_SRCH_LOCK_STATUS = 0x00030003;

    /**
     * one freq's table search finish,first parameter is frequency row ID.<br>
     * CN:一个频点的表信息搜索完成,第一个参数为频点标识ID.<br>
     */
    public static final int HI_SVR_EVT_SRCH_CUR_FREQ_TABLE_FINISH = 0x00030004;

    /**
     * The info of current locked;the parameter:param1 is frequency,param2 is
     * ID of frequency(use it can get the other information).<br>
     * CN:当前频点信息。参数param1是频点，param2是频点ID(根据频点ID，可以获取其他频点信息)。<br>
     */
    public static final int HI_SVR_EVT_SRCH_CUR_FREQ_INFO = 0x00030005;

    /**
     * Send to the current process of scan;the parameter:param1 is process(0..100).<br>
     * CN:发送当前搜索进度，参数param1值为进度(范围0--100)。<br>
     */
    public static final int HI_SVR_EVT_SRCH_CUR_SCHEDULE = 0x00030006;

    /**
     * Finish the scan of one frequency;the parameter:param1 is the number of new find
     * program,param2 is number of droped.<br>
     * CN:完成一个频点搜索。参数param1新发现节目数，param2是丢弃节目数。<br>
     */
    public static final int HI_SVR_EVT_SRCH_ONE_FREQ_FINISH = 0x00030007;

    /**
     * the message that success to finish scan；parameter:param1 is total frequency number,
     *  param2 is have program frequency number.<br>
     * CN:搜台完成消息。参数param1是所有频点数，param2包含节目的频点数。<br>
     */
    public static final int HI_SVR_EVT_SRCH_FINISH = 0x00030008;

    /**
     * To search for new program.the parameter:param1 is id of frequency,param2 is id of channel. <br>
     * CN:搜索到新的节目。参数param1是频点ID，param2是频道ID。<br>
     */
    public static final int HI_SVR_EVT_SRCH_GET_PROG = 0x00030009;
    /**
     *  To search for new program.the parameter:param1 is id of frequency,param2 is id of channel. obj is ChannelNode
     */
    public static final int HI_SVR_EVT_SRCH_GET_PROG_PESI = 0x0003000A ;
    public static final int HI_SVR_EVT_SRCH_CALLBACK_END = 0x0003000B ;
    ////////////////////////Book/////////////////////////////
    public static final int HI_SVR_EVT_BOOK_CALLBACK_START = 0x00040001 ;
    /**
     * Remind time of book task has been added, first parameter is bookRowID.<br>
     * CN:预定任务定时提醒已添加，第一个参数为预定标识ID。<br>
     */
    public static final int HI_SVR_EVT_BOOK_REMIND = 0x00040001;

    /**
     * Start time of book task is arrived, first parameter is bookRowID.<br>
     * CN:预定任务启动通知，第一个参数为预定标识ID。<br>
     */
    public static final int HI_SVR_EVT_BOOK_TIME_ARRIVE = 0x00040002;

    /**
     * End time of book task is arrived, first parameter is bookRowID.<br>
     * CN:预定任务结束通知，第一个参数为预定标识ID。<br>
     */
    public static final int HI_SVR_EVT_BOOK_TIME_END = 0x00040003;

    public static final int HI_SVR_EVT_BOOK_CALLBACK_END = 0x00040004 ;

    ////////////////////////time shift and record////////////////////////
    public static final int HI_SVR_EVT_PVR_CALLBACK_START = 0x00050001 ;
    /**
     *time shift status forward to end of a file,no parameter.<br>
     * CN:时移状态播放到文件尾，没有参数。<br>
     */
    public static final int HI_SVR_EVT_PVR_PLAY_EOF = 0x00050001;

    /**
     *time shift status backward to head of a file,no parameter.<br>
     * CN:时移状态回退到文件头，没有参数。<br>
     */
    public static final int HI_SVR_EVT_PVR_PLAY_SOF = 0x00050002;

    /**
     * time shift message, play back fail,no parameter.<br>
     * CN:时移消息，播放错误，没有参数。<br>
     */
    public static final int HI_SVR_EVT_PVR_PLAY_ERROR = 0x00050003;

    /**
     *time shift message, laying speed reaches the recording speed ,no parameter.<br>
     * CN:时移消息，播放追上录制，没有参数。<br>
     */
    public static final int HI_SVR_EVT_PVR_PLAY_REACH_REC = 0x00050004;

    /**
     *record message, disk full,no parameter.<br>
     * CN:录制消息，磁盘满，没有参数。<br>
     */
    public static final int HI_SVR_EVT_PVR_REC_DISKFULL = 0x00050005;
    /**
     * Recording errors, time shift and recording total message.<br>
     * CN:录制出错，时移和录制共有消息。<br>
     */
    public static final int HI_SVR_EVT_PVR_REC_ERROR = 0x00050006;

    /**
     * time shift and recording total message.<br>
     * in recording situation,receive the message when schedule time has reached.<br>
     * in time shift situation, acyclic buffer condition,<br>
     * this message is generated when the specified time is reached or the designated space run out.<br>
     * CN:录像和时移共有消息。<br>
     * 录制情况下，当录完指定的时间后产生此消息。<br>
     * 时移情况下，非循环缓冲区条件下，在录制完指定的缓冲时间或用完指定大小的缓冲区后产生此消息。<br>
     */
    public static final int HI_SVR_EVT_PVR_REC_OVER_FIX = 0x00050007;

    /**
     * time shift message,cycle buffer condition,this message is generated when recording again to catch up on the play.<br>
     * CN:时移消息，循环缓冲区情况下，当录制再次追赶上播放时发送此消息。<br>
     */
    public static final int HI_SVR_EVT_PVR_REC_REACH_PLAY = 0x00050008;

    /**
     * the storage speed of the disk is slower than the recording speed,time shift and recording total message.<br>
     * in recording situation, this message is generated when the recorded file will run out of storage media space.<br>
     * in time shift situation,acyclic buffer condition,this message is generated when the recorded file will run out of storage media space.<br>
     * CN: 磁盘存储速度慢于录制速度 ，录像和时移功能共有消息。<br>
     * 录制情况下，在录制文件将要耗尽剩余存储空间时产生此消息。<br>
     * 时移情况下，非循环缓冲区条件，在录制文件将要耗尽剩余存储空间时产生此消息。<br>
     */
    public static final int HI_SVR_EVT_PVR_REC_DISK_SLOW = 0x00050009;
    public static final int PESI_EVT_PVR_RECORD_START = 0x0005000A;//eric lin 20180713 pvr msg,-start
    public static final int PESI_EVT_PVR_RECORD_STOP = 0x0005000B;
    public static final int PESI_EVT_PVR_TIMESHIFT_START = 0x0005000C;
    public static final int PESI_EVT_PVR_TIMESHIFT_PLAY_START = 0x0005000D;
    public static final int PESI_EVT_PVR_TIMESHIFT_STOP = 0x0005000E;
    public static final int PESI_EVT_PVR_FILE_PLAY_START = 0x0005000F;
    public static final int PESI_EVT_PVR_FILE_PLAY_STOP = 0x00050010;
    public static final int PESI_EVT_PVR_PLAY_PARENTAL_LOCK = 0x00050011;
    public static final int HI_SVR_EVT_PVR_OPEN_HD_FINISH = 0x00050012;
    public static final int HI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP = 0x00050013;
    public static final int HI_SVR_EVT_PVR_NOT_SUPPORT = 0x00050014;
    public static final int HI_SVR_EVT_PVR_CALLBACK_END = 0x00050015;//Scoty 20180827 add and modify TimeShift Live Mode//eric lin 20180713 pvr msg,-end

    public static final int HI_SVR_EVT_PU_RESCAN = 0x00060001;
    public static final int HI_SVR_EVT_PU_RESCAN_CURRENT_TP = 0x00060002;
    public static final int HI_SVR_EVT_PU_PROG_UPDATED = 0x00060003;

    public static final int HI_SVR_EVT_EWS_CALLBACK_START = 0x00070001 ;
    /**
     * EWS start message, param1 is reserved, param2 is ChannelID.<br>
     * CN:紧急警报（EWS）开始消息，第一个参数预留，第二个参数为紧急警报切换到频道的ID。<br>
     */
    public static final int HI_SVR_EVT_EWS_START = 0x00070001;

    /**
     * EWS stop message, param1 is reserved, param2 is ChannelID.<br>
     * CN:紧急警报（EWS）停止消息，第一个参数预留，第二个参数为紧急警报停止后切回频道的ID。<br>
     */
    public static final int HI_SVR_EVT_EWS_STOP = 0x00070002;
    public static final int HI_SVR_EVT_EWS_CALLBACK_END = 0x00070003 ;

    /**
     * VMX message start<br>
     */
    public static final int HI_SVR_EVT_VMX_CALLBACK_START   = 0x00080001;
    public static final int HI_SVR_EVT_VMX_SHOW_MSG         = 0x00080001;
    public static final int HI_SVR_EVT_VMX_OTA_START        = 0x00080002;
    public static final int HI_SVR_EVT_VMX_OTA_ERR          = 0x00080003;
    public static final int HI_SVR_EVT_VMX_WATERMARK        = 0x00080004;
    public static final int HI_SVR_EVT_VMX_WATERMARK_CLOSE  = 0x00080005;
    public static final int HI_SVR_EVT_VMX_PIN              = 0x00080006;
    public static final int HI_SVR_EVT_VMX_IPPV             = 0x00080007;
    public static final int HI_SVR_EVT_CARD_DETECT          = 0x00080008;
    public static final int HI_SVR_EVT_VMX_SEARCH           = 0x00080009;
    public static final int PESI_EVT_VMX_SCRAMBLED =0x0008000A;
    public static final int PESI_EVT_VMX_BCIO_NOTIFY =0x0008000B;
    public static final int HI_SVR_EVT_EWBS  = 0x0008000C;
    public static final int HI_SVR_EVT_VMX_E16  = 0x0008000D;
    public static final int HI_SVR_EVT_VMX_MAIL  = 0x0008000E;
    public static final int HI_SVR_EVT_VMX_FACTORY  = 0x0008000F;
    public static final int HI_SVR_EVT_VMX_CHBLOCK  = 0x00080010;
    public static final int HI_SVR_EVT_VMX_OTA_PROC = 0x00080011;
    public static final int HI_SVR_EVT_VMX_EWBS_STOP = 0x00080012;//Scoty 20181225 modify VMX EWBS rule
    public static final int HI_SVR_EVT_VMX_CALLBACK_END     = 0x00080013;

    public static final int HI_SVR_EVT_LOADERDTV_CALLBACK_START     = 0x00090001;
    public static final int PESI_LOADERDVT_RETURN_DSMCC_STATUS   =0x00090001;
    public static final int HI_SVR_EVT_LOADERDTV_CALLBACK_END     = 0x00090002;

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
    public static final int PESI_EVT_SYSTEM_CALLBACK_END = 0x000B0002;

    public static final int HI_SVR_EVT_TBM_CALLBACK_START = 0x00BC0010 ;
    /**
     * Section table update message, param1 is TableType see HI_SVR_TBM_TABLE_TYPE_E, param2 is sizeof TableType. <br>
     * CN:表更新消息，第一个参数为表类型，请参考HI_SVR_TBM_TABLE_TYPE_E，第二个参数为表类型的大小。<br>
     */
    public static final int HI_SVR_EVT_TBM_UPDATE = 0x00BC0010;


    /**
     * Time update message, param1 is timeinfo，see HI_TIME_INFO_S, param2 is sizeof HI_TIME_INFO_S. <br>
     * CN:时间更新消息，第一个参数为时间信息，请参考HI_TIME_INFO_S，第二个参数为HI_TIME_INFO_S的大小。<br>
     */
    public static final int HI_SVR_EVT_TIME_UPDATE = 0x00BC0011;

    /**
     * TimeZone update message, param1 is s32TimeZoneSeconds, param2 is sizeof s32TimeZoneSeconds. <br>
     * CN:时区更新消息，第一个参数为时区秒数，第二个参数为s32TimeZoneSeconds的大小。<br>
     */
    public static final int HI_SVR_EVT_TIMEZONE_UPDATE = 0x00BC0012;
    public static final int HI_SVR_EVT_TBM_CALLBACK_END = 0x00BC0013 ;

    public static final int HI_SVR_CI_EVT_CALLBACK_START = 0x008A0002 ;
    public static final int HI_SVR_CI_EVT_CARD_INSERT = 0x008A0002;
    public static final int HI_SVR_CI_EVT_CARD_REMOVE = 0x008A0003;
    public static final int HI_SVR_CI_EVT_MMI_MESSAGE = 0x008A0004;
    public static final int HI_SVR_CI_EVT_MMI_MENU = 0x008A0005;
    public static final int HI_SVR_CI_EVT_MMI_LIST= 0x008A0006;
    public static final int HI_SVR_CI_EVT_MMI_ENQ= 0x008A0007;
    public static final int HI_SVR_CI_EVT_CLOSE_MMI= 0x008A0008;
    public static final int HI_SVR_CI_EVT_CALLBACK_END = 0x008A0009 ;

    public static final int HI_SVR_EVT_SSU_UPDATE = 0x008B0002;
    public static final int HI_SVR_EVT_SSU_TIMEOUT = 0x008B0003;
    public static final int HI_SVR_EVT_SSU_DOWNLOAD_SCHEDULE = 0x008B0004;
    public static final int HI_SVR_EVT_SSU_DOWNLOAD_ERROR = 0x008B0005;
    public static final int HI_SVR_EVT_SSU_DOWNLOAD_FINISH = 0x008B0006;

    //Scoty 20180831 modify new callback event id -e
    /**
     * ATV message start value.<br>
     * CN:ATV消息起始值<br>
     */
    public static final int HI_ATV_EVT_START    = 0x00C00000;

    /**
     * ATV signal status.<br>
     * CN:ATV的信号状态。<br>
     */
    public static final int HI_ATV_EVT_SIGNAL_STATUS = 0x00C00001;

    /**
     * Select source coomplete.<br>
     * CN:切源完成消息<br>
     */
    public static final int HI_ATV_EVT_SELECT_SOURCE_COMPLETE = 0x00C00002;

    /**
     * begin scanning.<br>
     * CN:ATV开始搜索消息。<br>
     */
    public static final int HI_ATV_EVT_SCAN_BEGIN = 0x00C00003;

    /**
     * scan schedule.<br>
     * CN:ATV搜索进度消息。<br>
     */
    public static final int HI_ATV_EVT_SCAN_PROGRESS = 0x00C00004;

    /**
     * found a valid frequency.<br>
     * CN:ATV搜索发现一个有效频点消息。<br>
     */
    public static final int HI_ATV_EVT_SCAN_LOCK = 0x00C00005;

    /**
     * scan finished.<br>
     * CN:ATV搜索完成消息。<br>
     */
    public static final int HI_ATV_EVT_SCAN_FINISH = 0x00C00006;

    /**
     * timming change.<br>
     * CN:ATV定时改变消息。<br>
     */
    public static final int HI_ATV_EVT_TIMMING_CHANGED = 0x00C00007;

    /**
     * device plug.<br>
     * CN:设备插入消息。<br>
     */
    public static final int HI_ATV_EVT_PLUGIN = 0x00C00008;

    /**
     * device plug out.<br>
     * CN:设备拔出消息。<br>
     */
    public static final int HI_ATV_EVT_PLUGOUT = 0x00C00009;

    /**
     * pc_adjust_status.<br>
     * CN:PC调整状态消息。<br>
     */
    public static final int HI_ATV_EVT_PC_ADJ_STATUS = 0x00C0000A;

    /**
     * HDMI CEC.<br>
     * CN:HDMI CEC切源消息。<br>
     */
    public static final int HI_ATV_EVT_CEC_SELECT_SOURCE = 0x00C0000B;

    /**
     * LOCK ChANGE.<br>
     * CN:LOCK 改变消息。<br>
     */
    public static final int HI_ATV_EVT_PLAYER_LOCK_CHANGED = 0x00C0000C;

    /**
     * ATV message start<br>
     * CN:ATV消息结束值<br>
     */
    public static final int HI_ATV_EVT_END      = 0x00C10000;

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
    public static final int PESI_EVT_CA_END = 0x000F0002;
}
