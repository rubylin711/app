package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.BookInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookModule {
    private static final String TAG = "BookModule";

    private static final int CMD_BOOK_Base = PrimeDtvMediaPlayer.CMD_Base + 0x700;

    // Book command
    private static final int CMD_BOOK_GetNum = CMD_BOOK_Base + 0x01;
    private static final int CMD_BOOK_GetBookByRowID = CMD_BOOK_Base + 0x02;
    private static final int CMD_BOOK_GetBookByType = CMD_BOOK_Base + 0x03;
    private static final int CMD_BOOK_FindConflictbooks = CMD_BOOK_Base + 0x04;
    private static final int CMD_BOOK_AddBook = CMD_BOOK_Base + 0x05;
    private static final int CMD_BOOK_UpdateBook = CMD_BOOK_Base + 0x06;
    private static final int CMD_BOOK_DeleteBookByRowID = CMD_BOOK_Base + 0x07;
    private static final int CMD_BOOK_DeleteBookByProg = CMD_BOOK_Base + 0x08;
    private static final int CMD_BOOK_ClearAllBooks = CMD_BOOK_Base + 0x09;
    private static final int CMD_BOOK_GetComingBook = CMD_BOOK_Base + 0x0A;
    private static final int CMD_BOOK_GetAllBooks = CMD_BOOK_Base + 0x0B;
    private static final int CMD_BOOK_SaveBookList = CMD_BOOK_Base + 0x0C;

    private List<BookInfo> g_ui_book_list = null;

    public List<BookInfo> get_ui_book_list() {
        return g_ui_book_list;
    }

    public List<BookInfo> init_ui_book_list() {//Init UI Book List after boot
        if (g_ui_book_list == null)
            g_ui_book_list = new ArrayList<BookInfo>();
        g_ui_book_list = book_info_get_list();

        if (g_ui_book_list == null)
            g_ui_book_list = new ArrayList<BookInfo>();

        return g_ui_book_list;
    }

    private void clear_ui_book_list() {
        if (g_ui_book_list != null)
            g_ui_book_list.clear();
    }

//    public void set_ui_book_manager(DTVActivity.BookManager bookmanager)
//    {
//        UIBookManager = bookmanager;
//    }
//
//    public DTVActivity.BookManager get_ui_book_manager()
//    {
//        return UIBookManager;
//    }

    public List<BookInfo> book_info_get_list() {
        return get_all_tasks();
    }

    public BookInfo book_info_get(int bookId) {
        return get_task_by_id(bookId);
    }

    public int book_info_add(BookInfo bookInfo) {
        return add_task(bookInfo);
    }

    public int book_info_update(BookInfo bookInfo) {
        return update_task(bookInfo);
    }

    public int book_info_update_list(List<BookInfo> bookList) {
        return update_book_list(bookList);
    }

    public int book_info_delete(int bookId) {
        return delete_task(bookId);
    }

    public int book_info_delete_all() {
        return clear_all_tasks();
    }

    public BookInfo book_info_get_coming_book() {
        return get_coming_task();
    }

    public List<BookInfo> book_info_find_conflict_books(BookInfo bookInfo) {
        return find_conflict_tasks(bookInfo);
    }

    private List<BookInfo> book_get_tasks_by_type(int type) {
        Log.d(TAG, "book_get_tasks_by_type() : " + type);
        ArrayList<BookInfo> list = null;
        list = new ArrayList<BookInfo>();

        {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_BOOK_GetBookByType);
            request.writeInt(type);
            request.writeInt(0);//u32Pos
            request.writeInt(1000);//u32Num

            PrimeDtvMediaPlayer.invokeex(request, reply);
            int ret = reply.readInt();
            if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
                int num = reply.readInt();
                Log.d(TAG, "num = " + num);
                if (num > 0) {
                    for (int i = 0; i < num; i++) {
                        BookInfo bookInfo = new BookInfo();
                        initial_book_data(bookInfo, reply);
                        list.add(bookInfo);
                    }
                }
            }
            request.recycle();
            reply.recycle();
        }
        return list;
    }

    private List<BookInfo> get_all_tasks() {
        Log.d(TAG, "get_all_tasks()");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_GetAllBooks);

        ArrayList<BookInfo> list = null;
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int num = reply.readInt();
            Log.d(TAG, "num = " + num);
            if (num > 0) {
                list = new ArrayList<BookInfo>();
                for (int i = 0; i < num; i++) {
                    BookInfo bookInfo = new BookInfo();
                    initial_book_data(bookInfo, reply);
                    list.add(bookInfo);
                }
            }
        }

        request.recycle();
        reply.recycle();
        return list;
    }

//    public BookInfo get_task_by_id_from_ui_book_list(int id)
//    {
//        for(int i = 0 ; i < UIBookManager.BookList.size() ; i++) {
//            if (UIBookManager.BookList.get(i).getBookId() == id)
//            {
//                return UIBookManager.BookList.get(i);
//            }
//        }
//        return null;
//    }

    private BookInfo get_task_by_id(int id) {
        Log.d(TAG, "get_task_by_id(" + id + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_GetBookByRowID);
        request.writeInt(id);

        BookInfo bookInfo = null;
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            bookInfo = new BookInfo();
            initial_book_data(bookInfo, reply);
        }
        request.recycle();
        reply.recycle();
        return bookInfo;
    }

    private int add_task(BookInfo bookInfo) {
        Log.d(TAG, "add_task(" + bookInfo.getBookId() + ")");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_AddBook);

        write_book_data(request, bookInfo);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            ret = reply.readInt();
        }

        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    private int update_task(BookInfo bookInfo) {
        Log.d(TAG, "update_task(" + bookInfo.getBookId() + ")");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_UpdateBook);

        write_book_data(request, bookInfo);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret =" + ret);
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_book_list(List<BookInfo> bookInfoList) // Need command and implement
    {
        if (bookInfoList != null && bookInfoList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_BOOK_SaveBookList);
            request.writeInt(bookInfoList.size());
            for (int i = 0; i < bookInfoList.size(); i++) {
                write_book_data(request, bookInfoList.get(i));
            }
            PrimeDtvMediaPlayer.invokeex(request, reply);
            int ret = reply.readInt();
            Log.d(TAG, "ret =" + ret);
            request.recycle();
            reply.recycle();
            return PrimeDtvMediaPlayer.get_return_value(ret);
        }
        return 0;
    }

    private void initial_book_data(BookInfo bookInfo, Parcel reply) {
        int id = reply.readInt();
        bookInfo.setBookId(id);
        Log.d(TAG, "initial_book_data:  id =" + id);

        long channelId = PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt());
        bookInfo.setChannelId(channelId);
        Log.d(TAG, "initial_book_data: channelId = " + channelId);

        reply.readInt();// status

        reply.readInt();// progType

        int type = reply.readInt();
        bookInfo.setBookType(/*EnTaskType.valueOf(type)*/type);
        Log.d(TAG, "initial_book_data: type = " + /*EnTaskType.valueOf(type)*/type);

        int cycle = reply.readInt();
        bookInfo.setBookCycle(/*EnTaskCycle.valueOf(cycle)*/cycle);
        Log.d(TAG, "initial_book_data:cycle = " + /*EnTaskCycle.valueOf(cycle)*/cycle);
        reply.readInt();// weektype

        int eventId = reply.readInt();
//        bookInfo.setEventId(eventId); // change in the future (Book)
        Log.d(TAG, "initial_book_data: eventId = " + eventId);

        int startTime = reply.readInt();
        Calendar ca = Calendar.getInstance();
        int dst = ca.get(Calendar.DST_OFFSET);
        //int zone = ca.get(Calendar.ZONE_OFFSET);

        Date startDate;
        PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();
        if (PrimeDtvMediaPlayer.ADD_SYSTEM_OFFSET) // connie 20181106 for not add system offset
            startDate = dtv.second_to_date(startTime - dst / 1000);
        else
            startDate = dtv.second_to_date(startTime);
        ca.setTime(startDate);
        int pesiStartTime = ca.get(Calendar.HOUR_OF_DAY) * 100 + ca.get(Calendar.MINUTE);

        bookInfo.setStartTime(pesiStartTime);
        bookInfo.setDate(ca.get(Calendar.DATE));
        bookInfo.setMonth(ca.get(Calendar.MONTH) + 1);
        bookInfo.setYear(ca.get(Calendar.YEAR));
        int weekDay = ca.get(Calendar.DAY_OF_WEEK);
        if (ca.getFirstDayOfWeek() == Calendar.SUNDAY) {
            weekDay = weekDay - 1;
            if (weekDay == 0) {
                weekDay = 7;
            }
        }
        bookInfo.setWeek(weekDay - 1);    // weekDay from java = 1~7, trans to 0~6
        Log.d(TAG, "initial_book_data: startDate = " + startDate.toString());
        Log.d(TAG, "initial_book_data: startTime = " + startTime);

        int durations = reply.readInt();
        bookInfo.setDuration(/*durations*/durations / 3600 * 100 + durations % 3600 / 60);
        Log.d(TAG, "initial_book_data:durations = " + durations);

        String name = reply.readString();
        bookInfo.setEventName(name);
        Log.d(TAG, "initial_book_data:name = " + name);

        reply.readInt();//Userdata
        //bookInfo.setGroupType();  // change in the future (Book)

        //task.setEnable(true);
        bookInfo.setEnable(1);  // true = 1

        int groupType = reply.readInt();
        bookInfo.setGroupType(groupType);
        Log.d(TAG, "initial_book_data:  groupType = " + groupType);
    }

    private void write_book_data(Parcel request, BookInfo bookInfo) {
        request.writeInt(bookInfo.getBookId());
        Log.d(TAG, "write_book_data: bookInfo.getId() = " + bookInfo.getBookId());
        request.writeInt((int) bookInfo.getChannelId());
        Log.d(TAG, "write_book_data: bookInfo.getChannelId() = " + bookInfo.getChannelId());

        request.writeInt(0); // status

        //need fix
        /*if (bookInfo.getBookType() == EnTaskType.STANDBY || bookInfo.getBookType() == EnTaskType.POWERON
                || bookInfo.getBookType() == EnTaskType.HINT)
        {
            request.writeInt(3);
        }
        else*/
        {
            request.writeInt(0);
        }

        request.writeInt(/*task.getType().ordinal()*/bookInfo.getBookType());
        Log.v(TAG, "write_book_data: bookInfo.getType() = " + /*task.getType().ordinal()*/bookInfo.getBookType());

        request.writeInt(/*task.getCycle().ordinal()*/bookInfo.getBookCycle());
        Log.v(TAG, "write_book_data: bookInfo.getCycle() = " + /*task.getCycle().ordinal()*/bookInfo.getBookCycle());

        String strDate = String.format(Locale.getDefault(),
                "%d/%02d/%02d %02d:%02d:%02d",
                bookInfo.getYear(), bookInfo.getMonth(), bookInfo.getDate(), bookInfo.getStartTime() / 100, bookInfo.getStartTime() % 100, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();
        cal.setTime(date);
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        request.writeInt(1 << (weekday - 1));

        request.writeInt(/*bookInfo.getEventId()*/0);   // change in the future (Book)
        Log.v(TAG, "write_book_data: bookInfo.getEventId() = " + /*bookInfo.getEventId()*/0);    // change in the future (Book)

        request.writeInt(dtv.date_to_second(date));
        Log.v(TAG, "write_book_data: startTime = " + date);
        Log.v(TAG, "write_book_data: startTimeSecond = " + dtv.date_to_second(date));

        int secDuration = /*hour*/bookInfo.getDuration() / 100 * 3600 + /*min*/bookInfo.getDuration() % 100 * 60;
        request.writeInt(secDuration);
        Log.v(TAG, "write_book_data: bookInfo.getDuration() = " + bookInfo.getDuration());
        request.writeString(bookInfo.getEventName());
        Log.v(TAG, "write_book_data: bookInfo.getEventName() = " + bookInfo.getEventName());
        request.writeInt(0);//u32UserData

        request.writeInt(bookInfo.getGroupType());
        Log.d(TAG, "write_book_data:  bookInfo.getGroupType() = " + bookInfo.getGroupType());
    }

    private int delete_task(int bookId) {
        Log.d(TAG, "delete_task(" + bookId + ")");
        return PrimeDtvMediaPlayer.excute_command(CMD_BOOK_DeleteBookByRowID, bookId);
    }

    private int clear_all_tasks() {
        Log.d(TAG, "clear_all_tasks()");
        return PrimeDtvMediaPlayer.excute_command(CMD_BOOK_ClearAllBooks);
    }

    private BookInfo get_coming_task() {
        int u32IntervalSecond = Integer.MAX_VALUE;
        Log.d(TAG, "get_coming_task()");
        BookInfo bookInfo = null;
        for (int i = 0; i < 5; i++) {
            BookInfo tmpbookInfo = null;
            int tmpInterval = Integer.MAX_VALUE;
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_BOOK_GetComingBook);

            request.writeInt(i);
            PrimeDtvMediaPlayer.invokeex(request, reply);
            int ret = reply.readInt();
            Log.d(TAG, "ret = " + ret);
            if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
                tmpInterval = reply.readInt();
                Log.d(TAG, "tmpInterval = " + tmpInterval);
                tmpbookInfo = new BookInfo();
                initial_book_data(tmpbookInfo, reply);
                if (tmpInterval < u32IntervalSecond) {
                    Log.d(TAG, "u32IntervalSecond = " + u32IntervalSecond);
                    u32IntervalSecond = tmpInterval;
                    bookInfo = tmpbookInfo;
                }
            }
            request.recycle();
            reply.recycle();
        }
        return bookInfo;
    }

    private List<BookInfo> find_conflict_tasks(BookInfo bookInfo) {
        Log.d(TAG, "find_conflict_tasks()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_FindConflictbooks);

        write_book_data(request, bookInfo);

        ArrayList<BookInfo> list = null;
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int num = reply.readInt();
            int bookId = 0;
            Log.d(TAG, "num = " + num);
            if (num > 0) {
                list = new ArrayList<BookInfo>();
                for (int i = 0; i < num; i++) {
                    BookInfo conflictBookInfo = new BookInfo();
                    bookId = reply.readInt();
                    conflictBookInfo = get_task_by_id(bookId);
                    list.add(conflictBookInfo);
                }
            }
        }

        request.recycle();
        reply.recycle();

        return list;
    }
}
