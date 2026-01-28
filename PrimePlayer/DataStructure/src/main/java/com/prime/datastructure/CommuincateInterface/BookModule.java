package com.prime.datastructure.CommuincateInterface;

import static com.prime.datastructure.CommuincateInterface.CommandBase.CMD_ServicePlayer_BOOK_Base;

public class BookModule {
    public static final int CMD_ServicePlayer_BOOK_BookInfoGetList = CMD_ServicePlayer_BOOK_Base+0x01;
    public static final int CMD_ServicePlayer_BOOK_BookInfoGet = CMD_ServicePlayer_BOOK_Base+0x02;
    public static final int CMD_ServicePlayer_BOOK_BookInfoAdd = CMD_ServicePlayer_BOOK_Base+0x03;
    public static final int CMD_ServicePlayer_BOOK_BookInfoUpdate = CMD_ServicePlayer_BOOK_Base+0x04;
    public static final int CMD_ServicePlayer_BOOK_BookInfoUpdateList = CMD_ServicePlayer_BOOK_Base+0x05;
    public static final int CMD_ServicePlayer_BOOK_BookInfoDelete = CMD_ServicePlayer_BOOK_Base+0x06;
    public static final int CMD_ServicePlayer_BOOK_BookInfoDeleteAll = CMD_ServicePlayer_BOOK_Base+0x07;
    public static final int CMD_ServicePlayer_BOOK_BookInfoGetComingBook = CMD_ServicePlayer_BOOK_Base+0x08;
    public static final int CMD_ServicePlayer_BOOK_BookInfoFindConflictBooks = CMD_ServicePlayer_BOOK_Base+0x09;
    public static final int CMD_ServicePlayer_BOOK_BookInfoSave = CMD_ServicePlayer_BOOK_Base+0x0A;
    public static final int CMD_ServicePlayer_BOOK_SetAlarm = CMD_ServicePlayer_BOOK_Base+0x0B;
    public static final int CMD_ServicePlayer_BOOK_CancelAlarm = CMD_ServicePlayer_BOOK_Base+0x0C;
    public static final int CMD_ServicePlayer_BOOK_ScheduleNextTimer = CMD_ServicePlayer_BOOK_Base+0x0D;

    public static final String KEY_BOOK_INTENT = "BookIntent";
    public static final String ACTION_START_RECORD = "com.prime.action.START_RECORD";
    public static final String ACTION_STOP_RECORD = "com.prime.action.STOP_RECORD";
    public static final String ACTION_STOP_ALL_RECORD = "com.prime.action.STOP_ALL_RECORD";
}
