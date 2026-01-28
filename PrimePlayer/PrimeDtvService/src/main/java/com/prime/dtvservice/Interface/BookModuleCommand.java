package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.content.Intent;
import android.os.Bundle;

import com.prime.datastructure.CommuincateInterface.BookModule;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;

import java.util.ArrayList;
import java.util.List;

public class BookModuleCommand {
    public static final String TAG = "BookModuleCommand";
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle, Bundle replyBundle, PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = " + command_id);
        switch(command_id) {
            case BookModule.CMD_ServicePlayer_BOOK_BookInfoGetList:
                replyBundle = book_info_get_list(requestBundle,replyBundle);
                break;
            case BookModule.CMD_ServicePlayer_BOOK_BookInfoGet:
                replyBundle = book_info_get(requestBundle,replyBundle);
                break;
            case BookModule.CMD_ServicePlayer_BOOK_BookInfoAdd:
                replyBundle = book_info_add(requestBundle,replyBundle);
                break;
            case BookModule.CMD_ServicePlayer_BOOK_BookInfoDelete:
                replyBundle = book_info_delete(requestBundle,replyBundle);
                break;
            case BookModule.CMD_ServicePlayer_BOOK_BookInfoDeleteAll:
                replyBundle = book_info_delete_all(requestBundle,replyBundle);
                break;
            case BookModule.CMD_ServicePlayer_BOOK_SetAlarm:
                replyBundle = set_alarm(requestBundle,replyBundle);
                break;
            case BookModule.CMD_ServicePlayer_BOOK_CancelAlarm:
                replyBundle = cancel_alarm(requestBundle,replyBundle);
                break;
            case BookModule.CMD_ServicePlayer_BOOK_ScheduleNextTimer:
                replyBundle = schedule_next_timer(requestBundle,replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    private Bundle book_info_get_list(Bundle requestBundle, Bundle replyBundle) {
        List<BookInfo> bookInfoList = primeDtv.book_info_get_list();
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        replyBundle.putParcelableArrayList(COMMAND_REPLY_OBJ, new ArrayList<>(bookInfoList));

        return replyBundle;
    }

    private Bundle book_info_get(Bundle requestBundle, Bundle replyBundle) {
        int bookId = requestBundle.getInt(BookInfo.BOOK_ID, -1);
        if (bookId == -1) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        BookInfo bookInfo = primeDtv.book_info_get(bookId);
        if (bookInfo != null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
            replyBundle.putParcelable(BookInfo.TAG, bookInfo);
        } else {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    private Bundle book_info_add(Bundle requestBundle, Bundle replyBundle) {
        BookInfo bookInfo = requestBundle.getParcelable(BookInfo.TAG, BookInfo.class);
        if (bookInfo == null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        primeDtv.book_info_add(bookInfo);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle book_info_delete(Bundle requestBundle, Bundle replyBundle) {
        int bookId = requestBundle.getInt(BookInfo.BOOK_ID, -1);
        if (bookId == -1) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        primeDtv.book_info_delete(bookId);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle book_info_delete_all(Bundle requestBundle, Bundle replyBundle) {

        primeDtv.book_info_delete_all();
        primeDtv.save_table(EnTableType.TIMER);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle set_alarm(Bundle requestBundle, Bundle replyBundle) {
        int bookId = requestBundle.getInt(BookInfo.BOOK_ID, -1);
        long triggerTimeMs = requestBundle.getLong(BookInfo.START_TIME_MS, -1);
        Intent intent = requestBundle.getParcelable(BookModule.KEY_BOOK_INTENT, Intent.class);
        if (bookId == -1 || triggerTimeMs == -1 || intent == null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        primeDtv.set_alarm(bookId, intent, triggerTimeMs);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle cancel_alarm(Bundle requestBundle, Bundle replyBundle) {
        int bookId = requestBundle.getInt(BookInfo.BOOK_ID, -1);
        Intent intent = requestBundle.getParcelable(BookModule.KEY_BOOK_INTENT, Intent.class);
        if (bookId == -1 || intent == null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        primeDtv.cancel_alarm(bookId, intent);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle schedule_next_timer(Bundle requestBundle, Bundle replyBundle) {
        BookInfo bookInfo = requestBundle.getParcelable(BookInfo.TAG, BookInfo.class);
        if (bookInfo == null) {
            replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
            return replyBundle;
        }

        primeDtv.schedule_next_timer(bookInfo);
        replyBundle.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }
}
