package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.Sysdata.BookInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestBookActivity extends DTVActivity {

    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 8;  // 8 bookinfo functions

    private List<String> mBookElements;

    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_book);

        Init();
    }

    @Override
    public void onBackPressed() {

        TestMidMain tm = new TestMidMain();
        // if all funcs are tested, set checked = true
        if ( mTestedFuncSet.size() == mTestTotalFuncCount )
        {
            int result = 0;
            tm.getTestInfoByIndex(mPosition).setChecked(true);

            // send error item to testMidMain
            for (int index : mErrorIndexSet) {
                result = tm.bitwiseLeftShift(result, index, false);    //fail item
            }

            tm.getTestInfoByIndex(mPosition).setResult(result);
        }

        super.onBackPressed();
    }

    public void BtnBookInfoGet_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 0; // it is first button

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null || simpleChannelList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "No Program, scan first!", btnIndex, store);
                return;
            }

            BookInfoDeleteAll(); // make sure book did not exist
            strInput.append("BookInfoDeleteAll()\n");
            strOutput.append("\n");

            Date now = GetLocalTime();
            Date testDate = new Date(now.getTime() + 3600000);  // testDate = now + 1hour
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(testDate);
            BookInfo bookInfoIn = new BookInfo();
            bookInfoIn.setBookId(BookInfo.MAX_NUM_OF_BOOKINFO);
            bookInfoIn.setEnable(1);
            bookInfoIn.setChannelId(simpleChannelList.get(0).getChannelId());
            bookInfoIn.setBookType(1);
            bookInfoIn.setBookCycle(2);
            bookInfoIn.setYear(calendar.get(Calendar.YEAR));
            bookInfoIn.setMonth(calendar.get(Calendar.MONTH)+1);
            bookInfoIn.setDate(calendar.get(Calendar.DATE));
            int week = calendar.get(Calendar.DAY_OF_WEEK);
            if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                week = week - 1;
                if(week == 0){
                    week = 7;
                }
            }
            bookInfoIn.setWeek(week-1); // tran 1~7 to 0~6
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            bookInfoIn.setStartTime(hour*100 + min); // pesi startTime HHmm
//            Log.d(TAG, "BtnBookInfoGet_OnClick: " + bookInfoIn.getStartTime());
            bookInfoIn.setDuration(200);    // pesi duration 200 = 2h
            bookInfoIn.setEventName("test book");
            int retBookId = BookInfoAdd(bookInfoIn);
            bookInfoIn.setBookId(retBookId);

            strInput.append("BookInfoAdd()\n");
            strOutput.append("ret ID = ").append(retBookId).append("\n");

            // get test data from DB
            BookInfo bookInfoOut = GetTaskByIdFromUIBookList(retBookId);
            strInput.append("BookInfoGet()\n\n");
            strOutput.append("ID = ").append(retBookId).append("\n\n");
            if ( bookInfoOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null BookInfo", btnIndex, store);
                return;
            }

            // set textview text
            strInput.append("Name = ").append(bookInfoIn.getEventName()).append("\n");
            strOutput.append("Name = ").append(bookInfoOut.getEventName()).append("\n");
            strInput.append("BookID = ").append(bookInfoIn.getBookId()).append("\n");
            strOutput.append("BookID = ").append(bookInfoOut.getBookId()).append("\n");
            strInput.append("ChannelID = ").append(bookInfoIn.getChannelId()).append("\n");
            strOutput.append("ChannelID = ").append(bookInfoOut.getChannelId()).append("\n");
            strInput.append("BookType = ").append(bookInfoIn.getBookType()).append("\n");
            strOutput.append("BookType = ").append(bookInfoOut.getBookType()).append("\n");
            strInput.append("BookCycle = ").append(bookInfoIn.getBookCycle()).append("\n");
            strOutput.append("BookCycle = ").append(bookInfoOut.getBookCycle()).append("\n");
            strInput.append("Year = ").append(bookInfoIn.getYear()).append("\n");
            strOutput.append("Year = ").append(bookInfoOut.getYear()).append("\n");
            strInput.append("Month = ").append(bookInfoIn.getMonth()).append("\n");
            strOutput.append("Month = ").append(bookInfoOut.getMonth()).append("\n");
            strInput.append("Date = ").append(bookInfoIn.getDate()).append("\n");
            strOutput.append("Date = ").append(bookInfoOut.getDate()).append("\n");
            strInput.append("Week = ").append(bookInfoIn.getWeek()).append("\n");
            strOutput.append("Week = ").append(bookInfoOut.getWeek()).append("\n");
            strInput.append("StartTime = ").append(bookInfoIn.getStartTime()).append("\n");
            strOutput.append("StartTime = ").append(bookInfoOut.getStartTime()).append("\n");
            strInput.append("Duration = ").append(bookInfoIn.getDuration()).append("\n");
            strOutput.append("Duration = ").append(bookInfoOut.getDuration()).append("\n");

            strInput.append(bookInfoIn.ToString()).append("\n");
            strOutput.append(bookInfoOut.ToString()).append("\n");

            // compare
            if ( !bookInfoIn.ToString().equals(bookInfoOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "BookInfoIn is different form BookInfoOut", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // get BookInfo when there is no specific ID
            bookInfoOut = BookInfoGet(BookInfo.MAX_NUM_OF_BOOKINFO);
            strInput.append("BookInfoGet(BookInfo.MAX_NUM_OF_BOOKINFO)\n");
            strOutput.append("bookInfo = ").append(bookInfoOut).append("\n");

            // we should get null
            if (bookInfoOut != null)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got BookInfo when the specific ID does not exit", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoGet() Pass!", store);
    }

    public void BtnBookInfoGetList_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 1;
        final int testSize = 3;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null || simpleChannelList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "No Program, scan first!", btnIndex, store);
                return;
            }

            BookInfoDeleteAll(); // make sure book did not exist
            strInput.append("BookInfoDeleteAll()\n");
            strOutput.append("\n");
            strInput.append("Test size = ").append(testSize).append("\n");
            strOutput.append("\n");

            Date now = GetLocalTime();
            Calendar calendar = Calendar.getInstance();
            for (int i = 0 ; i < testSize ; i++)
            {
                Date testDate = new Date(now.getTime() + 3600000*(i+1));  // testDate = now + i+1 hour
                calendar.setTime(testDate);
                BookInfo bookInfoIn = new BookInfo();
                bookInfoIn.setBookId(BookInfo.MAX_NUM_OF_BOOKINFO);
                bookInfoIn.setEnable(1);
                bookInfoIn.setChannelId(simpleChannelList.get(0).getChannelId());
                bookInfoIn.setBookType(1);
                bookInfoIn.setBookCycle(1);
                bookInfoIn.setYear(calendar.get(Calendar.YEAR));
                bookInfoIn.setMonth(calendar.get(Calendar.MONTH)+1);
                bookInfoIn.setDate(calendar.get(Calendar.DATE));
                int week = calendar.get(Calendar.DAY_OF_WEEK);
                if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                    week = week - 1;
                    if(week == 0){
                        week = 7;
                    }
                }
                bookInfoIn.setWeek(week-1); // trans 1~7 to 0~6
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int min = calendar.get(Calendar.MINUTE);
                bookInfoIn.setStartTime(hour*100 + min); // pesi startTime HHmm
//            Log.d(TAG, "BtnBookInfoGet_OnClick: " + bookInfoIn.getStartTime());
                bookInfoIn.setDuration(30);    // pesi duration 30 = 30m
                bookInfoIn.setEventName("test book" + i);
                BookInfoAdd(bookInfoIn);
            }

            List<BookInfo> bookInfoList = BookInfoGetList();
            strInput.append("BookInfoGetList()\n\n");
            strOutput.append("Size = ").append(bookInfoList == null ? 0 : bookInfoList.size()).append("\n\n");

            // check if null
            if ( bookInfoList == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null BookInfoList", btnIndex, store);
                return;
            }

            // show
            for (int i = 0 ; i < bookInfoList.size() ; i++)
            {
                strInput.append("bookInfoList[").append(i).append("] : \n");
                strOutput.append("Name = ").append(bookInfoList.get(i).getEventName()).append("\n");
                strInput.append("\n");
                strOutput.append("BookID = ").append(bookInfoList.get(i).getBookId()).append("\n");
                strInput.append("\n");
                strOutput.append("ChannelID = ").append(bookInfoList.get(i).getChannelId()).append("\n");
                strInput.append("\n");
                strOutput.append("BookType = ").append(bookInfoList.get(i).getBookType()).append("\n");
                strInput.append("\n");
                strOutput.append("BookCycle = ").append(bookInfoList.get(i).getBookCycle()).append("\n");
                strInput.append("\n");
                strOutput.append("Year = ").append(bookInfoList.get(i).getYear()).append("\n");
                strInput.append("\n");
                strOutput.append("Month = ").append(bookInfoList.get(i).getMonth()).append("\n");
                strInput.append("\n");
                strOutput.append("Date = ").append(bookInfoList.get(i).getDate()).append("\n");
                strInput.append("\n");
                strOutput.append("Week = ").append(bookInfoList.get(i).getWeek()).append("\n");
                strInput.append("\n");
                strOutput.append("StartTime = ").append(bookInfoList.get(i).getStartTime()).append("\n");
                strInput.append("\n");
                strOutput.append("Duration = ").append(bookInfoList.get(i).getDuration()).append("\n");
                strInput.append("\n");
                strOutput.append("\n");
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 - end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoGetList() Pass!", store);
    }

    public void BtnBookInfoAddAndUpdate_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 2;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 Add -start
            strInput.append("Input Test1 : Add\n");
            strOutput.append("Output Test1 : Add\n");

            List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null || simpleChannelList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "No Program, scan first!", btnIndex, store);
                return;
            }

            BookInfoDeleteAll(); // make sure book did not exist
            strInput.append("BookInfoDeleteAll()\n\n");
            strOutput.append("\n\n");

            Date now = GetLocalTime();
            Calendar calendar = Calendar.getInstance();
            Date testDate = new Date(now.getTime() + 3600000);  // testDate = now + 1 hour
            calendar.setTime(testDate);
            BookInfo bookInfoIn = new BookInfo();
            bookInfoIn.setBookId(BookInfo.MAX_NUM_OF_BOOKINFO);
            bookInfoIn.setEnable(1);
            bookInfoIn.setChannelId(simpleChannelList.get(0).getChannelId());
            bookInfoIn.setBookType(1);
            bookInfoIn.setBookCycle(2);
            bookInfoIn.setYear(calendar.get(Calendar.YEAR));
            bookInfoIn.setMonth(calendar.get(Calendar.MONTH)+1);
            bookInfoIn.setDate(calendar.get(Calendar.DATE));
            int week = calendar.get(Calendar.DAY_OF_WEEK);
            if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                week = week - 1;
                if(week == 0){
                    week = 7;
                }
            }
            bookInfoIn.setWeek(week-1); // tran 1~7 to 0~6
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            bookInfoIn.setStartTime(hour*100 + min); // pesi startTime HHmm
//            Log.d(TAG, "BtnBookInfoGet_OnClick: " + bookInfoIn.getStartTime());
            bookInfoIn.setDuration(1111);    // pesi duration 1111 = 11h11m
            bookInfoIn.setEventName("test book");
            int retBookId = BookInfoAdd(bookInfoIn);
            bookInfoIn.setBookId(retBookId);

            // get test data from DB
            BookInfo bookInfoOut = GetTaskByIdFromUIBookList(retBookId);
            if ( bookInfoOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null BookInfo", btnIndex, store);
                return;
            }

            // set textview text
            strInput.append("Name = ").append(bookInfoIn.getEventName()).append("\n");
            strOutput.append("Name = ").append(bookInfoOut.getEventName()).append("\n");
            strInput.append("BookID = ").append(bookInfoIn.getBookId()).append("\n");
            strOutput.append("BookID = ").append(bookInfoOut.getBookId()).append("\n");
            strInput.append("ChannelID = ").append(bookInfoIn.getChannelId()).append("\n");
            strOutput.append("ChannelID = ").append(bookInfoOut.getChannelId()).append("\n");
            strInput.append("BookType = ").append(bookInfoIn.getBookType()).append("\n");
            strOutput.append("BookType = ").append(bookInfoOut.getBookType()).append("\n");
            strInput.append("BookCycle = ").append(bookInfoIn.getBookCycle()).append("\n");
            strOutput.append("BookCycle = ").append(bookInfoOut.getBookCycle()).append("\n");
            strInput.append("Year = ").append(bookInfoIn.getYear()).append("\n");
            strOutput.append("Year = ").append(bookInfoOut.getYear()).append("\n");
            strInput.append("Month = ").append(bookInfoIn.getMonth()).append("\n");
            strOutput.append("Month = ").append(bookInfoOut.getMonth()).append("\n");
            strInput.append("Date = ").append(bookInfoIn.getDate()).append("\n");
            strOutput.append("Date = ").append(bookInfoOut.getDate()).append("\n");
            strInput.append("Week = ").append(bookInfoIn.getWeek()).append("\n");
            strOutput.append("Week = ").append(bookInfoOut.getWeek()).append("\n");
            strInput.append("StartTime = ").append(bookInfoIn.getStartTime()).append("\n");
            strOutput.append("StartTime = ").append(bookInfoOut.getStartTime()).append("\n");
            strInput.append("Duration = ").append(bookInfoIn.getDuration()).append("\n");
            strOutput.append("Duration = ").append(bookInfoOut.getDuration()).append("\n");

            strInput.append(bookInfoIn.ToString()).append("\n");
            strOutput.append(bookInfoOut.ToString()).append("\n");

            // compare
            if ( !bookInfoIn.ToString().equals(bookInfoOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "BookInfoIn is different form BookInfoOut", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            // test2 Update -start
            strInput.append("Input Test2 : Update()\n");
            strOutput.append("Output Test2 : Update()\n");

            bookInfoIn.setEventName("test update book");
            bookInfoIn.setBookType(0);
            bookInfoIn.setBookCycle(3);

            if (min < 50)
            {
                min += 10;
            }
            else
            {
                min -= 10;
            }

            bookInfoIn.setStartTime(hour*100 + min);
            bookInfoIn.setDuration(2359);
            BookInfoUpdate(bookInfoIn);

            // get test data from DB
            Log.d(TAG, "BtnBookInfoAddAndUpdate_OnClick: " + bookInfoIn.getBookId());
            bookInfoOut = GetTaskByIdFromUIBookList(bookInfoIn.getBookId());
            if ( bookInfoOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null BookInfo", btnIndex, store);
                return;
            }

            // set textview text
            strInput.append("Name = ").append(bookInfoIn.getEventName()).append("\n");
            strOutput.append("Name = ").append(bookInfoOut.getEventName()).append("\n");
            strInput.append("BookID = ").append(bookInfoIn.getBookId()).append("\n");
            strOutput.append("BookID = ").append(bookInfoOut.getBookId()).append("\n");
            strInput.append("ChannelID = ").append(bookInfoIn.getChannelId()).append("\n");
            strOutput.append("ChannelID = ").append(bookInfoOut.getChannelId()).append("\n");
            strInput.append("BookType = ").append(bookInfoIn.getBookType()).append("\n");
            strOutput.append("BookType = ").append(bookInfoOut.getBookType()).append("\n");
            strInput.append("BookCycle = ").append(bookInfoIn.getBookCycle()).append("\n");
            strOutput.append("BookCycle = ").append(bookInfoOut.getBookCycle()).append("\n");
            strInput.append("Year = ").append(bookInfoIn.getYear()).append("\n");
            strOutput.append("Year = ").append(bookInfoOut.getYear()).append("\n");
            strInput.append("Month = ").append(bookInfoIn.getMonth()).append("\n");
            strOutput.append("Month = ").append(bookInfoOut.getMonth()).append("\n");
            strInput.append("Date = ").append(bookInfoIn.getDate()).append("\n");
            strOutput.append("Date = ").append(bookInfoOut.getDate()).append("\n");
            strInput.append("Week = ").append(bookInfoIn.getWeek()).append("\n");
            strOutput.append("Week = ").append(bookInfoOut.getWeek()).append("\n");
            strInput.append("StartTime = ").append(bookInfoIn.getStartTime()).append("\n");
            strOutput.append("StartTime = ").append(bookInfoOut.getStartTime()).append("\n");
            strInput.append("Duration = ").append(bookInfoIn.getDuration()).append("\n");
            strOutput.append("Duration = ").append(bookInfoOut.getDuration()).append("\n");

            // compare
            if ( !bookInfoIn.ToString().equals(bookInfoOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "BookInfoIn is different form BookInfoOut", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoAdd/Update() Pass!", store);
    }

    public void BtnBookInfoUpdateList_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 3;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null || simpleChannelList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "No Program, scan first!", btnIndex, store);
                return;
            }

            int saveListSize = BookInfo.MAX_NUM_OF_BOOKINFO;
            List<BookInfo> bookInfoList = new ArrayList<>();

            Date now = GetLocalTime();
            Calendar calendar = Calendar.getInstance();
            for ( int i = 0 ; i < saveListSize ; i++ )
            {
                Date testDate = new Date(now.getTime() + 3600000*(i+1));  // testDate = now + i+1 hour
                calendar.setTime(testDate);
                BookInfo bookInfoIn = new BookInfo();
                bookInfoIn.setBookId(i);
                bookInfoIn.setEnable(1);
                bookInfoIn.setChannelId(simpleChannelList.get(0).getChannelId());
                bookInfoIn.setBookType(i%2);
                bookInfoIn.setBookCycle(i%5);
                bookInfoIn.setYear(calendar.get(Calendar.YEAR));
                bookInfoIn.setMonth(calendar.get(Calendar.MONTH)+1);
                bookInfoIn.setDate(calendar.get(Calendar.DATE));
                int week = calendar.get(Calendar.DAY_OF_WEEK);
                if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                    week = week - 1;
                    if(week == 0){
                        week = 7;
                    }
                }
                bookInfoIn.setWeek(week-1); // tran 1~7 to 0~6
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int min = calendar.get(Calendar.MINUTE);
                bookInfoIn.setStartTime(hour*100 + min); // pesi startTime HHmm
//            Log.d(TAG, "BtnBookInfoGet_OnClick: " + bookInfoIn.getStartTime());
                bookInfoIn.setDuration(15);    // pesi duration 15 = 15m
                bookInfoIn.setEventName("test book" + i);
                bookInfoList.add(bookInfoIn);
            }

            BookInfoUpdateList(bookInfoList);
            strInput.append("BookInfoSaveList() : Size = ").append(bookInfoList.size()).append("\n");
            strOutput.append("\n");

            List<BookInfo> bookInfoListOut = BookInfoGetList();

            if (bookInfoListOut == null)
            {
                GotError(view, "Got null BookInfoList", btnIndex, store);
                return;
            }

            strInput.append("BookInfoGetList()\n");
            strOutput.append("bookInfoList : Size = ").append(bookInfoListOut.size()).append("\n");

            // check size
            if (bookInfoListOut.size() != bookInfoList.size())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of BookInfoList after BookInfoSaveList", btnIndex, store);
                return;
            }

            // show content of booklist and booklistOut
            for (int i = 0 ; i < bookInfoList.size() ; i++)
            {
                // set textview text
                strInput.append("Name = ").append(bookInfoList.get(i).getEventName()).append("\n");
                strOutput.append("Name = ").append(bookInfoListOut.get(i).getEventName()).append("\n");
                strInput.append("BookID = ").append(bookInfoList.get(i).getBookId()).append("\n");
                strOutput.append("BookID = ").append(bookInfoListOut.get(i).getBookId()).append("\n");
                strInput.append("ChannelID = ").append(bookInfoList.get(i).getChannelId()).append("\n");
                strOutput.append("ChannelID = ").append(bookInfoListOut.get(i).getChannelId()).append("\n");
                strInput.append("BookType = ").append(bookInfoList.get(i).getBookType()).append("\n");
                strOutput.append("BookType = ").append(bookInfoListOut.get(i).getBookType()).append("\n");
                strInput.append("BookCycle = ").append(bookInfoList.get(i).getBookCycle()).append("\n");
                strOutput.append("BookCycle = ").append(bookInfoListOut.get(i).getBookCycle()).append("\n");
                strInput.append("Year = ").append(bookInfoList.get(i).getYear()).append("\n");
                strOutput.append("Year = ").append(bookInfoListOut.get(i).getYear()).append("\n");
                strInput.append("Month = ").append(bookInfoList.get(i).getMonth()).append("\n");
                strOutput.append("Month = ").append(bookInfoListOut.get(i).getMonth()).append("\n");
                strInput.append("Date = ").append(bookInfoList.get(i).getDate()).append("\n");
                strOutput.append("Date = ").append(bookInfoListOut.get(i).getDate()).append("\n");
                strInput.append("Week = ").append(bookInfoList.get(i).getWeek()).append("\n");
                strOutput.append("Week = ").append(bookInfoListOut.get(i).getWeek()).append("\n");
                strInput.append("StartTime = ").append(bookInfoList.get(i).getStartTime()).append("\n");
                strOutput.append("StartTime = ").append(bookInfoListOut.get(i).getStartTime()).append("\n");
                strInput.append("Duration = ").append(bookInfoList.get(i).getDuration()).append("\n");
                strOutput.append("Duration = ").append(bookInfoListOut.get(i).getDuration()).append("\n");

                strInput.append("\n");
                strOutput.append("\n");
            }

            // check content
            for (int i = 0 ; i < bookInfoList.size() ; i++)
            {
                if (!bookInfoList.get(i).ToString().equals(bookInfoListOut.get(i).ToString()))
                {
                    ShowResultOnTXV(strInput.toString(), txvInput);
                    ShowResultOnTXV(strOutput.toString(), txvOutput);
                    GotError(view, "Content does not match, pos = " + i, btnIndex, store);
                    return;
                }
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoSaveList() Pass!", store);
    }

    public void BtnBookInfoDelete_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 4;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null || simpleChannelList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "No Program, scan first!", btnIndex, store);
                return;
            }

            // make sure no book exist
            BookInfoDeleteAll();
            strInput.append("BookInfoDeleteAll()\n\n");
            strOutput.append("\n\n");

            // add a bookinfo first
            Date now = GetLocalTime();
            Calendar calendar = Calendar.getInstance();
            Date testDate = new Date(now.getTime() + 3600000);  // testDate = now + 1 hour
            calendar.setTime(testDate);
            BookInfo bookInfoIn = new BookInfo();
            bookInfoIn.setBookId(BookInfo.MAX_NUM_OF_BOOKINFO);
            bookInfoIn.setEnable(1);
            bookInfoIn.setChannelId(simpleChannelList.get(0).getChannelId());
            bookInfoIn.setBookType(0);
            bookInfoIn.setBookCycle(0);
            bookInfoIn.setYear(calendar.get(Calendar.YEAR));
            bookInfoIn.setMonth(calendar.get(Calendar.MONTH)+1);
            bookInfoIn.setDate(calendar.get(Calendar.DATE));
            int week = calendar.get(Calendar.DAY_OF_WEEK);
            if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                week = week - 1;
                if(week == 0){
                    week = 7;
                }
            }
            bookInfoIn.setWeek(week-1);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            bookInfoIn.setStartTime(hour*100 + min); // pesi startTime HHmm
//            Log.d(TAG, "BtnBookInfoGet_OnClick: " + bookInfoIn.getStartTime());
            bookInfoIn.setDuration(1111);    // pesi duration 1111 = 11h11m
            bookInfoIn.setEventName("test del book");
            int retBookId = BookInfoAdd(bookInfoIn);
            bookInfoIn.setBookId(retBookId);

            strInput.append("BookInfoAdd()\n");
            strOutput.append("ret ID = ").append(retBookId).append("\n");

            // get from DB
            BookInfo bookInfoOut = GetTaskByIdFromUIBookList(retBookId);
            if ( bookInfoOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null BookInfo after add", btnIndex, store);
                return;
            }

            // record size -> del -> record size
            List<BookInfo> bookInfoList = BookInfoGetList();
            int sizeBefore = bookInfoList == null ? 0 : bookInfoList.size();
            strInput.append("BookInfoGetList()\n");
            strOutput.append("Size = ").append(sizeBefore).append("\n");

            BookInfoDelete(retBookId);
            strInput.append("BookInfoDelete()\n");
            strOutput.append("ID = ").append(retBookId).append("\n");

            bookInfoList = BookInfoGetList();
            int sizeAfter = bookInfoList == null ? 0 : bookInfoList.size();
            strInput.append("BookInfoGetList()\n");
            strOutput.append("Size = ").append(sizeAfter).append("\n");

            // check size
            if ( sizeAfter != sizeBefore-1 )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of BookInfoList after BookInfoDelete", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoDelete() Pass!", store);
    }

    public void BtnBookInfoDeleteAll_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 5;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            BookInfoDeleteAll();
            strInput.append("BookInfoDeleteAll()\n");
            strOutput.append("\n");

            List<BookInfo> bookInfoList = BookInfoGetList();
            strInput.append("BookInfoGetList()\n");
            strOutput.append("bookInfoList = ").append(bookInfoList).append("\n");

            // bookInfoList should be null when empty
            if ( bookInfoList != null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "GetList  is not null when there is no BookInfo", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // delete all when no bookinfo exist
            // test1 had delete all once so just del again
            BookInfoDeleteAll();
            strInput.append("BookInfoDeleteAll()\n");
            strOutput.append("\n");

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoDeleteAll() Pass!", store);
    }

    public void BtnBookInfoGetComingBook_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 6;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null || simpleChannelList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "No Program, scan first!", btnIndex, store);
                return;
            }

            BookInfoDeleteAll();
            strInput.append("BookInfoDeleteAll()\n");
            strOutput.append("\n");

            // add a bookinfo first
            Date now = GetLocalTime();
            Calendar calendar = Calendar.getInstance();
            Date testDate = new Date(now.getTime() + 3600000);  // testDate = now + 1 hour
            calendar.setTime(testDate);
            BookInfo bookInfoIn = new BookInfo();
            bookInfoIn.setBookId(BookInfo.MAX_NUM_OF_BOOKINFO);
            bookInfoIn.setEnable(1);
            bookInfoIn.setChannelId(simpleChannelList.get(0).getChannelId());
            bookInfoIn.setBookType(0);
            bookInfoIn.setBookCycle(0);
            bookInfoIn.setYear(calendar.get(Calendar.YEAR));
            bookInfoIn.setMonth(calendar.get(Calendar.MONTH)+1);
            bookInfoIn.setDate(calendar.get(Calendar.DATE));
            int week = calendar.get(Calendar.DAY_OF_WEEK);
            if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                week = week - 1;
                if(week == 0){
                    week = 7;
                }
            }
            bookInfoIn.setWeek(week-1);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            bookInfoIn.setStartTime(hour*100 + min); // pesi startTime HHmm
//            Log.d(TAG, "BtnBookInfoGet_OnClick: " + bookInfoIn.getStartTime());
            bookInfoIn.setDuration(1);    // pesi duration 1 = 1m
            bookInfoIn.setEventName("test book");
            int retBookId = BookInfoAdd(bookInfoIn);
            bookInfoIn.setBookId(retBookId);

            strInput.append("BookInfoAdd()\n");

            BookInfo bookInfoOut = BookInfoGetComingBook();
            strOutput.append("BookInfoGetComingBook()\n");
            if ( bookInfoOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "BookInfoGetComingBook() got null BookInfo", btnIndex, store);
                return;
            }

            // set textview text
            strInput.append("Name = ").append(bookInfoIn.getEventName()).append("\n");
            strOutput.append("Name = ").append(bookInfoOut.getEventName()).append("\n");
            strInput.append("BookID = ").append(bookInfoIn.getBookId()).append("\n");
            strOutput.append("BookID = ").append(bookInfoOut.getBookId()).append("\n");
            strInput.append("ChannelID = ").append(bookInfoIn.getChannelId()).append("\n");
            strOutput.append("ChannelID = ").append(bookInfoOut.getChannelId()).append("\n");
            strInput.append("BookType = ").append(bookInfoIn.getBookType()).append("\n");
            strOutput.append("BookType = ").append(bookInfoOut.getBookType()).append("\n");
            strInput.append("BookCycle = ").append(bookInfoIn.getBookCycle()).append("\n");
            strOutput.append("BookCycle = ").append(bookInfoOut.getBookCycle()).append("\n");
            strInput.append("Year = ").append(bookInfoIn.getYear()).append("\n");
            strOutput.append("Year = ").append(bookInfoOut.getYear()).append("\n");
            strInput.append("Month = ").append(bookInfoIn.getMonth()).append("\n");
            strOutput.append("Month = ").append(bookInfoOut.getMonth()).append("\n");
            strInput.append("Date = ").append(bookInfoIn.getDate()).append("\n");
            strOutput.append("Date = ").append(bookInfoOut.getDate()).append("\n");
            strInput.append("Week = ").append(bookInfoIn.getWeek()).append("\n");
            strOutput.append("Week = ").append(bookInfoOut.getWeek()).append("\n");
            strInput.append("StartTime = ").append(bookInfoIn.getStartTime()).append("\n");
            strOutput.append("StartTime = ").append(bookInfoOut.getStartTime()).append("\n");
            strInput.append("Duration = ").append(bookInfoIn.getDuration()).append("\n");
            strOutput.append("Duration = ").append(bookInfoOut.getDuration()).append("\n");

            // compare
            if ( !bookInfoIn.ToString().equals(bookInfoOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "BookInfoIn is different form BookInfoOut", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoGetComingBook() Pass!", store);
    }

    public void BtnBookInfoFindConflictBooks_OnClick(View view)
    {
        List<BookInfo> store = BookInfoGetList();
        final int btnIndex = 7;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null || simpleChannelList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "No Program, scan first!", btnIndex, store);
                return;
            }

            BookInfoDeleteAll();
            strInput.append("BookInfoDeleteAll()\n");
            strOutput.append("\n");

            // add a bookinfo first
            strInput.append("BookInfoAdd()\n");
            strOutput.append("\n");
            Date now = GetLocalTime();
            Calendar calendar = Calendar.getInstance();
            Date testDate = new Date(now.getTime() + 3600000);  // testDate = now + 1 hour
            calendar.setTime(testDate);
            BookInfo bookInfoIn = new BookInfo();
            bookInfoIn.setBookId(BookInfo.MAX_NUM_OF_BOOKINFO);
            bookInfoIn.setEnable(1);
            bookInfoIn.setChannelId(simpleChannelList.get(0).getChannelId());
            bookInfoIn.setBookType(0);
            bookInfoIn.setBookCycle(0);
            bookInfoIn.setYear(calendar.get(Calendar.YEAR));
            bookInfoIn.setMonth(calendar.get(Calendar.MONTH)+1);
            bookInfoIn.setDate(calendar.get(Calendar.DATE));
            int week = calendar.get(Calendar.DAY_OF_WEEK);
            if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
                week = week - 1;
                if(week == 0){
                    week = 7;
                }
            }
            bookInfoIn.setWeek(week-1);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            bookInfoIn.setStartTime(hour*100 + min); // pesi startTime HHmm
//            Log.d(TAG, "BtnBookInfoGet_OnClick: " + bookInfoIn.getStartTime());
            bookInfoIn.setDuration(1);    // pesi duration 11 = 1m
            bookInfoIn.setEventName("test book");
            int retBookId = BookInfoAdd(bookInfoIn);
//            bookInfoIn.setBookId(retBookId);  // same id will not be considered conflict in pesi

            // get from DB
            BookInfo bookInfoOut = GetTaskByIdFromUIBookList(retBookId);
            if ( bookInfoOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null BookInfo after add", btnIndex, store);
                return;
            }

            // find conflict
            strInput.append("BookInfoFindConflictBooks()\n");
            strOutput.append("\n");
            bookInfoOut.setBookId(BookInfo.MAX_NUM_OF_BOOKINFO);
            List<BookInfo> bookInfoList = BookInfoFindConflictBooks(bookInfoOut);

            if (bookInfoList == null)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null conflict", btnIndex, store);
                return;
            }

            // set textview text show conflict
            for ( int i = 0 ; i < bookInfoList.size() ; i++ )
            {
                strInput.append("Conflict[").append(i).append("]\n");
                strOutput.append("Name = ").append(bookInfoList.get(i).getEventName()).append("\n");
                strInput.append("\n");
                strOutput.append("BookID = ").append(bookInfoList.get(i).getBookId()).append("\n");
                strInput.append("\n");
                strOutput.append("ChannelID = ").append(bookInfoList.get(i).getChannelId()).append("\n");
                strInput.append("\n");
                strOutput.append("BookType = ").append(bookInfoList.get(i).getBookType()).append("\n");
                strInput.append("\n");
                strOutput.append("BookCycle = ").append(bookInfoList.get(i).getBookCycle()).append("\n");
                strInput.append("\n");
                strOutput.append("Year = ").append(bookInfoList.get(i).getYear()).append("\n");
                strInput.append("\n");
                strOutput.append("Month = ").append(bookInfoList.get(i).getMonth()).append("\n");
                strInput.append("\n");
                strOutput.append("Date = ").append(bookInfoList.get(i).getDate()).append("\n");
                strInput.append("\n");
                strOutput.append("Week = ").append(bookInfoList.get(i).getWeek()).append("\n");
                strInput.append("\n");
                strOutput.append("StartTime = ").append(bookInfoList.get(i).getStartTime()).append("\n");
                strInput.append("\n");
                strOutput.append("Duration = ").append(bookInfoList.get(i).getDuration()).append("\n");
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "BookInfoFindConflictBooks() Pass!", store);
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTBOOK_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTBOOK_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTBOOK_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTBOOK_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestBook");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        // init bookinfo elements, for error message
        mBookElements = new ArrayList<>();
        mBookElements.add("Enable");
        mBookElements.add("BookId");
        mBookElements.add("ChNum");
        mBookElements.add("ChType");
        mBookElements.add("BookType");
        mBookElements.add("BookCycle");
        mBookElements.add("Year");
        mBookElements.add("Month");
        mBookElements.add("Date");
        mBookElements.add("Week");
        mBookElements.add("StartTime");
        mBookElements.add("Duration");
        mBookElements.add("EventName");
    }

    private void GotError(View view, String errorMsg, int btnIndex, List<BookInfo> store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());

        if ( store != null )
        {
//            BookInfoSaveList(store);
        }
    }

    private void TestPass(View view, String msg, List<BookInfo> store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());

        if ( store != null )
        {
//            BookInfoSaveList(store);
        }
    }

    // show result(string) on textview
    private void ShowResultOnTXV(String result, TextView textView)
    {
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }
}

