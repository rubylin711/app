package com.prime.dtv.service.CommandManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.datastructure.sysdata.SeriesInfo;
import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceApplication;
import com.prime.dtv.ServiceInterface;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.datastructure.sysdata.BookInfo;

import java.util.ArrayList;
import java.util.List;

public class BookCmdManager extends BaseManager {
    private static final String TAG = "BookCmdManager" ;

    private final DataManager mDataManager;

    public BookCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, BookCmdManager.class);
        mDataManager = DataManager.getDataManager(context);
    }

    /*
    Timer
     */
    public List<BookInfo> BookInfoGetList() {
        return mDataManager.getBookInfoList();
    }

    public BookInfo BookInfoGet(int bookId) {
        return mDataManager.getBookInfo(bookId);
    }

    public int BookInfoAdd(BookInfo bookInfo) {
        mDataManager.addBookInfo(bookInfo);
        return 1;
    }

    public int BookInfoUpdate(BookInfo bookInfo) {
        mDataManager.updateBookInfo(bookInfo.getBookId(), bookInfo);
        return 1;
    }

    public int BookInfoUpdateList(List<BookInfo> bookList) {
        mDataManager.updateBookInfoList(bookList);
        return 1;
    }

    public int BookInfoDelete(int bookId) {
        mDataManager.delBookInfo(bookId);
        return 1;
    }

    public int BookInfoDeleteAll() {
        mDataManager.delAllBookInfo();
        return 1;
    }

    public BookInfo BookInfoGetComingBook() {
        return null;
    }

    public List<BookInfo> BookInfoFindConflictBooks(BookInfo newBookInfo) {
        List<BookInfo> allBookInfoList = mDataManager.getBookInfoList();

        List<BookInfo> conflicBookInfoList = new ArrayList<>();
        for (BookInfo bookInfo: allBookInfoList) {
            if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD) {
                //Log.d(TAG, "BookInfoFindConflictBooks: " + bookInfo.ToString());

                if (check_conflict(newBookInfo,bookInfo)) {
                    //Log.d(TAG, "BookInfoFindConflictBooks: conflict");
                    //Log.d(TAG, "BookInfoFindConflictBooks: new " + newBookInfo.ToString());
                    conflicBookInfoList.add(bookInfo);
                }
            }
        }

        return conflicBookInfoList;
    }

    // save one bookInfo to database
    public void BookInfoSave(BookInfo bookInfo) {
        mDataManager.DataManagerSaveBookInfo(bookInfo);
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }

    @SuppressLint("SimpleDateFormat")
    public boolean check_conflict(BookInfo newBookInfo, BookInfo oldBookInfo) {

        List<Long> startTimeList = new ArrayList<>();
        List<Long> endTimeList = new ArrayList<>();
        get_time_list(newBookInfo,startTimeList, endTimeList);
        /*for (Long startTime:startTimeList)
            Log.d(TAG, "check_conflict: startTime = " + startTime);
        for (Long endTime:endTimeList)
            Log.d(TAG, "check_conflict: endTime = " + endTime);*/

        List<Long> oldStartTimeList = new ArrayList<>();
        List<Long> oldEndTimeList = new ArrayList<>();
        get_time_list(oldBookInfo,oldStartTimeList, oldEndTimeList);

        /*for (Long startTime:oldStartTimeList)
            Log.d(TAG, "check_conflict: startTime = " + startTime);
        for (Long endTime:oldEndTimeList)
            Log.d(TAG, "check_conflict: endTime = " + endTime);*/

        for (int i = 0; i < startTimeList.size(); i++) {
            //Log.i(TAG, "check_conflict: new " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(startTimeList.get(i))) + " ~ " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(endTimeList.get(i))));

            for (int j = 0; j < oldStartTimeList.size(); j++) {
                //Log.i(TAG, "check_conflict: old start " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(oldStartTimeList.get(j))) + " ~ " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(oldEndTimeList.get(j))));
                if (startTimeList.get(i) >= oldStartTimeList.get(j) && startTimeList.get(i) < oldEndTimeList.get(j))
                    return true;
                if (endTimeList.get(i) > oldStartTimeList.get(j) && endTimeList.get(i) <= oldEndTimeList.get(j))
                    return true;
                if (oldStartTimeList.get(j) >= startTimeList.get(i) && oldStartTimeList.get(j) < endTimeList.get(i))
                    return true;
                if (oldEndTimeList.get(j) > startTimeList.get(i) && oldEndTimeList.get(j) <= endTimeList.get(i))
                    return true;
            }
        }

        Log.i(TAG, "check_conflict: false");
        return false;
        /*if ( getBookCycle() == BOOK_CYCLE_ONETIME)
            return check_conflict_onetime_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_DAILY)
            return check_conflict_daily_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_WEEKLY)
            return check_conflict_weekly_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_WEEKEND)
            return check_conflict_weekend_and_other(oldBookInfo);
        else if ( getBookCycle() == BOOK_CYCLE_WEEKDAYS)
            return check_conflict_weekdays_and_other(oldBookInfo);
        else
            return false;*/
    }

    public void get_time_list(BookInfo bookInfo, List<Long> startTimeList, List<Long> endTimeList) {
        Log.e(TAG,"this function not porting!! please check");
        Long startTimestamp = bookInfo.get_start_time_stamp();
        Long endTimestamp = bookInfo.get_end_time_stamp();

        switch (bookInfo.getBookCycle()) {
            case BookInfo.BOOK_CYCLE_ONETIME: {
                startTimeList.add(startTimestamp);
                endTimeList.add(endTimestamp);
            }break;
            case BookInfo.BOOK_CYCLE_DAILY: {
                for (int i = 0; i < BookInfo.CHECK_CONFLICT_NUM; i++) {
                    startTimeList.add(startTimestamp + (long)86400000 * i);
                    endTimeList.add(endTimestamp + (long)86400000 * i);
                }
            }break;
            case BookInfo.BOOK_CYCLE_WEEKLY: {
                for (int i = 0; i < BookInfo.CHECK_WEEKLY_CONFLICT_NUM; i++) {
                    startTimeList.add(startTimestamp + (long)86400000 * i * 7);
                    endTimeList.add(endTimestamp + (long)86400000 * i * 7);
                }
            }break;
            case BookInfo.BOOK_CYCLE_WEEKEND: {
                for (int i = 0; i < BookInfo.CHECK_CONFLICT_NUM; i++) {
                    if ((bookInfo.get_week_value() + i) % 7 == 0 || (bookInfo.get_week_value() + i) % 6 == 0) {
                        startTimeList.add(startTimestamp + (long)86400000 * i);
                        endTimeList.add(endTimestamp + (long)86400000 * i);
                    }
                }
            }break;
            case BookInfo.BOOK_CYCLE_WEEKDAYS: {
                for (int i = 0; i < BookInfo.CHECK_CONFLICT_NUM; i++) {
                    if ((bookInfo.get_week_value() + i) % 7 != 0 || (bookInfo.get_week_value() + i) % 6 != 0) {
                        startTimeList.add(startTimestamp + (long)86400000 * i);
                        endTimeList.add(endTimestamp + (long)86400000 * i);
                    }
                }
            }break;
            case BookInfo.BOOK_CYCLE_SERIES: {
                PrimeDtv primeDtv = ServiceInterface.get_prime_dtv();
                SeriesInfo.Series series = primeDtv.get_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());

                if (series == null) {
                    Log.e(TAG, "get_time_list: series == null");
                    return;
                }

                for (SeriesInfo.Episode episode : series.getEpisodeList()) {
                    //Log.d(TAG, "get_time_list: " + episode.toString());
                    if(primeDtv.pvr_CheckSeriesEpisode(bookInfo.getSeriesRecKey(), episode.getEpisodeKey()) == 0)
                        continue;
                    startTimeList.add(bookInfo.get_start_time_stamp(episode.getStartLocalDateTime()));
                    endTimeList.add(bookInfo.get_end_time_stamp(episode.getStartLocalDateTime(), episode.getDuration()));
                }
            }break;
            default:
                Log.e(TAG, "get_time_list: book type not match");
                break;
        }
    }
}
