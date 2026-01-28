package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TestBookManagerActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 10;  // 10 bookmanager functions

    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    StringBuilder mStrInput;
    StringBuilder mStrOutput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_book_manager);

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

    public void BtnBookManager_OnClick( View view )
    {
        final int btnIndex = 0;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        List<BookInfo> bookInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            bookManager = new BookManager();
            bookInfoList = bookManager.BookList;

            if ( bookInfoList == null )
            {
                mStrInput.append("No BookInfo\n");
                mStrOutput.append("No BookInfo\n");
            }
            else
            {
                mStrInput.append("new BookManager() : ").append("\n");
                mStrOutput.append("BookList : Size = ").append(bookInfoList.size()).append("\n");

                // show all
                for ( int i = 0 ; i < bookInfoList.size() ; i++ )
                {
                    BookInfo bookInfo = bookInfoList.get(i);
                    mStrInput.append("BookList[ ").append(i).append(" ] : \n");
                    mStrOutput.append("BookInfo").append(i).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("BookId = ").append(bookInfo.getBookId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChId = ").append(bookInfo.getChannelId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("GroupType = ").append(bookInfo.getGroupType()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("EventName = ").append(bookInfo.getEventName()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("BookType = ").append(bookInfo.getBookType()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("BookCycle = ").append(bookInfo.getBookCycle()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Year = ").append(bookInfo.getYear()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Month = ").append(bookInfo.getMonth()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Date = ").append(bookInfo.getDate()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Week = ").append(bookInfo.getWeek()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("StartTime = ").append(bookInfo.getStartTime()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Duration = ").append(bookInfo.getDuration()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Enable = ").append(bookInfo.getEnable()).append("\n");

                    mStrInput.append("\n");
                    mStrOutput.append("\n");
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "BookManager() Pass!");
    }

    public void BtnCheckExist_OnClick( View view )
    {
        final int btnIndex = 1;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        List<BookInfo> bookInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            bookInfoList = BookInfoGetList();

            if ( bookInfoList == null )
            {
                mStrInput.append("No BookInfo\n");
                mStrOutput.append("No BookInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No BookInfo", btnIndex);
                return;
            }
            else
            {
                bookManager = new BookManager();

                mStrInput.append("new BookManager() : ").append("\n");
                mStrOutput.append("BookList : Size = ").append(bookInfoList.size()).append("\n");

                BookInfo bookInfo = bookInfoList.get(0);
                long startTime, duration;
                String strStartTime
                        = bookInfo.getYear()
                        + "-"
                        + String.format(Locale.US, "%02d", bookInfo.getMonth())
                        + "-"
                        + String.format(Locale.US, "%02d", bookInfo.getDate())
                        + " "
                        + String.format(Locale.US, "%02d",bookInfo.getStartTime()/100)
                        + ":"
                        + String.format(Locale.US, "%02d",bookInfo.getStartTime()%100)
                        + ":"
                        + "00";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date = sdf.parse(strStartTime);

                startTime = date.getTime();
                duration = bookInfo.getDuration()/100*60*60*1000 + bookInfo.getDuration()%100*60*1000;
                mStrInput.append("Test StartTime = ").append(startTime).append("\n");
                mStrOutput.append("\n");
                mStrInput.append("Test Duration = ").append(duration).append("\n\n");
                mStrOutput.append("\n\n");

                BookInfo existBookinfo = bookManager.CheckExist(bookInfo.getChannelId(), bookInfo.getGroupType(), startTime, duration);

                if ( existBookinfo == null )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "Got null when exist", btnIndex);
                    return;
                }

                mStrInput.append("BookInfoGetList()[ 0 ] : ").append("\n");
                mStrOutput.append("CheckExist() BookInfo : ").append("\n");
                mStrInput.append("ChId = ").append(bookInfo.getChannelId()).append("\n");
                mStrOutput.append("ChId = ").append(existBookinfo.getChannelId()).append("\n");
                mStrInput.append("GroupType = ").append(bookInfo.getGroupType()).append("\n");
                mStrOutput.append("GroupType = ").append(existBookinfo.getGroupType()).append("\n");
                mStrInput.append("Year = ").append(bookInfo.getYear()).append("\n");
                mStrOutput.append("Year = ").append(existBookinfo.getYear()).append("\n");
                mStrInput.append("Month = ").append(bookInfo.getMonth()).append("\n");
                mStrOutput.append("Month = ").append(existBookinfo.getMonth()).append("\n");
                mStrInput.append("Date = ").append(bookInfo.getDate()).append("\n");
                mStrOutput.append("Date = ").append(existBookinfo.getDate()).append("\n");
                mStrInput.append("StartTime = ").append(bookInfo.getStartTime()).append("\n");
                mStrOutput.append("StartTime = ").append(existBookinfo.getStartTime()).append("\n");
                mStrInput.append("Duration = ").append(bookInfo.getDuration()).append("\n");
                mStrOutput.append("Duration = ").append(existBookinfo.getDuration()).append("\n");
            }


            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");
            bookInfoList = BookInfoGetList();

            if ( bookInfoList == null )
            {
                mStrInput.append("No BookInfo\n");
                mStrOutput.append("No BookInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No BookInfo", btnIndex);
                return;
            }
            else
            {
                bookManager = new BookManager();

                mStrInput.append("new BookManager() : ").append("\n");
                mStrOutput.append("BookList : Size = ").append(bookInfoList.size()).append("\n");

                long startTime = 0;
                long duration = 0;
                mStrInput.append("Test StartTime = ").append(startTime).append("\n");
                mStrOutput.append("\n");
                mStrInput.append("Test Duration = ").append(duration).append("\n\n");
                mStrOutput.append("\n\n");

                BookInfo existBookinfo = bookManager.CheckExist(1, ProgramInfo.ALL_TV_TYPE, startTime, duration);

                mStrInput.append("\n");
                mStrOutput.append("CheckExist() BookInfo = ").append(existBookinfo).append("\n");

                if ( existBookinfo != null )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "BookManager.CheckExist() return non null", btnIndex);
                    return;
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "CheckExist() Pass!");
    }

    public void BtnCheckFull_OnClick( View view )
    {
        final int btnIndex = 2;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        List<BookInfo> bookInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            bookManager = new BookManager();
            bookInfoList = bookManager.BookList;
            bookInfoList.clear();
            mStrInput.append("BookList.clear() : ").append("\n");
            mStrOutput.append("BookList : Size = ").append(bookInfoList.size()).append("\n");

            mStrInput.append("CheckFull() : ").append("\n\n");
            mStrOutput.append("Return = ").append(bookManager.CheckFull()).append("\n\n");
            if ( bookManager.CheckFull() )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "CheckFull() = true when empty", btnIndex);
                return;
            }

            // add MAX_NUM_OF_BOOKINFO bookinfo
            for ( int i = 0 ; i < BookInfo.MAX_NUM_OF_BOOKINFO ; i++ )
            {
                BookInfo bookInfo = new BookInfo();
                bookInfo.setBookId(i);
                bookInfo.setEnable(1);
                bookInfoList.add(bookInfo);
            }

            mStrInput.append("Add MAX_NUM bookInfo : ").append("\n");
            mStrOutput.append("BookList : Size = ").append(bookInfoList.size()).append("\n");

            mStrInput.append("CheckFull() : ").append("\n");
            mStrOutput.append("Return = ").append(bookManager.CheckFull()).append("\n");
            if ( !bookManager.CheckFull() )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "CheckFull() = false when full", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "CheckFull() Pass!");
    }

    public void BtnGetEmptyBookId_OnClick( View view )
    {
        final int btnIndex = 3;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        List<BookInfo> bookInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            bookManager = new BookManager();
            bookInfoList = bookManager.BookList;
            bookInfoList.clear();
            mStrInput.append("BookList.clear() : ").append("\n");
            mStrOutput.append("BookList : Size = ").append(bookInfoList.size()).append("\n");

            int emptyId = bookManager.GetEmptyBookId();
            mStrInput.append("GetEmptyBookId() : ").append("\n\n");
            mStrOutput.append("Return = ").append(emptyId).append("\n\n");
            if ( emptyId != 0 )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "GetEmptyBookId() return non 0 when empty", btnIndex);
                return;
            }

            // add MAX_NUM_OF_BOOKINFO bookinfo
            for ( int i = 0 ; i < BookInfo.MAX_NUM_OF_BOOKINFO ; i++ )
            {
                BookInfo bookInfo = new BookInfo();
                bookInfo.setBookId(i);
                bookInfo.setEnable(1);
                bookInfoList.add(bookInfo);
            }

            mStrInput.append("Add MAX_NUM bookInfo : ").append("\n");
            mStrOutput.append("BookList : Size = ").append(bookInfoList.size()).append("\n");

            emptyId = bookManager.GetEmptyBookId();
            mStrInput.append("GetEmptyBookId() : ").append("\n\n");
            mStrOutput.append("Return = ").append(emptyId).append("\n\n");
            if ( emptyId != BookInfo.MAX_NUM_OF_BOOKINFO )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "GetEmptyBookId() return non MAX_NUM_OF_BOOKINFO when full", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "GetEmptyBookId() Pass!");
    }

    public void BtnAddBookInfo_OnClick( View view )
    {
        final int btnIndex = 4;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        List<BookInfo> bookInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");



            bookManager = new BookManager();
            bookInfoList = bookManager.BookList;

            int sizeBefore = bookInfoList.size();
            mStrInput.append("new BookManager() : ").append("\n");
            mStrOutput.append("BookList : Size = ").append(sizeBefore).append("\n");

            boolean isFullWhenAdd;
            Calendar calendar = Calendar.getInstance();
            BookInfo bookInfo = new BookInfo();
            bookInfo.setEnable(1);
            bookInfo.setBookId(bookManager.GetEmptyBookId());
            bookInfo.setChannelId(1);
            bookInfo.setGroupType(2);
            bookInfo.setYear(calendar.get(Calendar.YEAR));
            bookInfo.setMonth(calendar.get(Calendar.MONTH) + 1);  // calendar.month = 0~11
            bookInfo.setDate(calendar.get(Calendar.DAY_OF_MONTH));
            bookInfo.setWeek(calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
            bookInfo.setStartTime(calendar.get(Calendar.HOUR_OF_DAY)*100+calendar.get(Calendar.MINUTE));
            bookInfo.setDuration(1234);

            isFullWhenAdd = bookManager.CheckFull();
            bookManager.AddBookInfo(bookInfo);
            int sizeAfter = bookManager.BookList.size();
            mStrInput.append("AddBookInfo() : ").append("\n\n");
            mStrOutput.append("BookList : Size = ").append(sizeAfter).append("\n\n");

            if ( sizeAfter != sizeBefore+1 && !isFullWhenAdd )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "Size did not change after AddBookInfo()", btnIndex);
                return;
            }

            BookInfo bookListLast = bookManager.BookList.get(sizeAfter-1);
            mStrInput.append("Added BookInfo : ").append("\n");
            mStrOutput.append("BookList.Last : ").append("\n");
            mStrInput.append("ChId = ").append(bookInfo.getChannelId()).append("\n");
            mStrOutput.append("ChId = ").append(bookListLast.getChannelId()).append("\n");
            mStrInput.append("GroupType = ").append(bookInfo.getGroupType()).append("\n");
            mStrOutput.append("GroupType = ").append(bookListLast.getGroupType()).append("\n");
            mStrInput.append("Year = ").append(bookInfo.getYear()).append("\n");
            mStrOutput.append("Year = ").append(bookListLast.getYear()).append("\n");
            mStrInput.append("Month = ").append(bookInfo.getMonth()).append("\n");
            mStrOutput.append("Month = ").append(bookListLast.getMonth()).append("\n");
            mStrInput.append("Date = ").append(bookInfo.getDate()).append("\n");
            mStrOutput.append("Date = ").append(bookListLast.getDate()).append("\n");
            mStrInput.append("Week = ").append(bookInfo.getWeek()).append("\n");
            mStrOutput.append("Week = ").append(bookListLast.getWeek()).append("\n");
            mStrInput.append("StartTime = ").append(bookInfo.getStartTime()).append("\n");
            mStrOutput.append("StartTime = ").append(bookListLast.getStartTime()).append("\n");
            mStrInput.append("Duration = ").append(bookInfo.getDuration()).append("\n");
            mStrOutput.append("Duration = ").append(bookListLast.getDuration()).append("\n");

            if ( !bookInfo.ToString().equals(bookListLast.ToString()) )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "Added bookInfo did not match BookList.lastBookInfo", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "AddBookInfo() Pass!");
    }

    public void BtnDelBookInfo_OnClick( View view )
    {
        final int btnIndex = 5;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        List<BookInfo> bookInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            bookManager = new BookManager();
            bookInfoList = bookManager.BookList;
            int sizeBefore = bookManager.BookList.size();
            if ( sizeBefore == 0 )
            {
                Calendar calendar = Calendar.getInstance();
                BookInfo bookInfo = new BookInfo();
                bookInfo.setEnable(1);
                bookInfo.setBookId(0);
                bookInfo.setEventName("Added Test BookInfo");
                bookInfo.setChannelId(1);
                bookInfo.setGroupType(2);
                bookInfo.setYear(calendar.get(Calendar.YEAR));
                bookInfo.setMonth(calendar.get(Calendar.MONTH) + 1);  // calendar.month = 0~11
                bookInfo.setDate(calendar.get(Calendar.DAY_OF_MONTH));
                bookInfo.setWeek(calendar.get(Calendar.DAY_OF_WEEK));
                bookInfo.setStartTime(calendar.get(Calendar.HOUR_OF_DAY)*100+calendar.get(Calendar.MINUTE));
                bookInfo.setDuration(1234);
            }

            mStrInput.append("new BookManager() : ").append("\n");
            mStrOutput.append("BookList : Size = ").append(sizeBefore).append("\n");

            BookInfo delBookInfo = bookInfoList.get(0);
            bookManager.DelBookInfo(delBookInfo);

            int sizeAfter = bookManager.BookList.size();
            mStrInput.append("DelBookInfo() : Del[0]").append("\n\n");
            mStrOutput.append("BookList : Size = ").append(sizeAfter).append("\n\n");

            if ( sizeAfter != sizeBefore-1 )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "Size did not change after DelBookInfo()", btnIndex);
                return;
            }

            bookInfoList = bookManager.BookList;
            for ( int i = 0 ; i < bookInfoList.size() ; i++ )
            {
                BookInfo bookInfo = bookInfoList.get(i);
                if ( delBookInfo.ToString().equals(bookInfo.ToString()) )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "Deleted bookInfo still exist after DelBookInfo()", btnIndex);
                    return;
                }
            }

            // show all
            for ( int i = 0 ; i < bookInfoList.size() ; i++ )
            {
                BookInfo bookInfo = bookInfoList.get(i);
                mStrInput.append("BookList[ ").append(i).append(" ] : \n");
                mStrOutput.append("BookInfo").append(i).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("BookId = ").append(bookInfo.getBookId()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("ChId = ").append(bookInfo.getChannelId()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("GroupType = ").append(bookInfo.getGroupType()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("EventName = ").append(bookInfo.getEventName()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("BookType = ").append(bookInfo.getBookType()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("BookCycle = ").append(bookInfo.getBookCycle()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("Year = ").append(bookInfo.getYear()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("Month = ").append(bookInfo.getMonth()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("Date = ").append(bookInfo.getDate()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("Week = ").append(bookInfo.getWeek()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("StartTime = ").append(bookInfo.getStartTime()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("Duration = ").append(bookInfo.getDuration()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("Enable = ").append(bookInfo.getEnable()).append("\n");

                mStrInput.append("\n");
                mStrOutput.append("\n");
            }

            for ( int i = 0 ; i < bookInfoList.size() ; i++ )
            {
                BookInfo bookInfo = bookInfoList.get(i);
                if ( bookInfo.getBookId() != i )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "BookList.Id did not arrange after DelBookInfo()", btnIndex);
                    return;
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "DelBookInfo() Pass!");
    }

    public void BtnGetEndTime_OnClick( View view )
    {
        final int btnIndex = 6;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        BookInfo bookInfo;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");
            bookManager = new BookManager();

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // calendar.month = 0~11
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int sec = 0;
            int duration = 1234;

            bookInfo = new BookInfo();
            bookInfo.setYear(year);
            bookInfo.setMonth(month);
            bookInfo.setDate(day);
            bookInfo.setStartTime(hour*100 + min);
            bookInfo.setDuration(duration);

            String strDate = String.format(Locale.US, "%d/%02d/%02d %02d:%02d:%02d", year, month, day, hour, min, sec);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            Date date = sdf.parse(strDate);

            mStrInput.append("Date = ").append(strDate).append("\n");
            mStrOutput.append("(long)Date = ").append(date.getTime()).append("\n");
            mStrInput.append("Duration = ").append(duration/100).append(":").append(duration%100).append("\n\n");
            mStrOutput.append("(long)Duration = ").append(duration/100*60*60*1000 + duration%100*60*1000).append("\n\n");

            long funcEndTime = bookManager.GetEndTime(bookInfo).getTime();
            long calcEndTime = date.getTime() + duration/100*60*60*1000 + duration%100*60*1000;

            mStrInput.append("GetEndTime() = ").append(funcEndTime).append("\n");
            mStrOutput.append("CalculateEndTime = ").append(calcEndTime).append("\n");

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "GetEndTime() Pass!");
    }

    public void BtnCheckBookAfterNow_OnClick( View view )
    {
        final int btnIndex = 7;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        BookInfo bookInfo;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // calendar.month = 0~11
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int sec = 0;

            bookInfo = new BookInfo();
            bookInfo.setYear(year);
            bookInfo.setMonth(month);
            bookInfo.setDate(day);
            bookInfo.setStartTime(hour*100 + min - 1);  // time before now

            String strDateNow = String.format(Locale.US, "%d/%02d/%02d %02d:%02d:%02d", year, month, day, hour, min, sec);
            String strDateBefore = String.format
                    (
                            Locale.US, "%d/%02d/%02d %02d:%02d:%02d",
                            bookInfo.getYear(),
                            bookInfo.getMonth(),
                            bookInfo.getDate(),
                            bookInfo.getStartTime()/100,
                            bookInfo.getStartTime()%100,
                            sec
                    );
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            Date dateNow = sdf.parse(strDateNow);
            Date dateBefore = sdf.parse(strDateBefore);

            mStrInput.append("Date now = ").append(strDateNow).append("\n");
            mStrOutput.append("(long)Date = ").append(dateNow.getTime()).append("\n");
            mStrInput.append("Date tested = ").append(strDateBefore).append("\n");
            mStrOutput.append("(long)Date = ").append(dateBefore.getTime()).append("\n");

            bookManager = new BookManager();
            mStrInput.append("CheckBookAfterNow() : ").append("\n");
            mStrOutput.append("Return = ").append(bookManager.CheckBookAfterNow(bookInfo)).append("\n");
            if (bookManager.CheckBookAfterNow(bookInfo))
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "CheckBookAfterNow() return true when bookInfo is before now", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1; // calendar.month = 0~11
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            min = calendar.get(Calendar.MINUTE);
            sec = 0;

            bookInfo = new BookInfo();
            bookInfo.setYear(year);
            bookInfo.setMonth(month);
            bookInfo.setDate(day);
            bookInfo.setStartTime(hour*100 + min + 1);  // time after now

            strDateNow = String.format(Locale.US, "%d/%02d/%02d %02d:%02d:%02d", year, month, day, hour, min, sec);
            String strDateAfter = String.format
                    (
                            Locale.US, "%d/%02d/%02d %02d:%02d:%02d",
                            bookInfo.getYear(),
                            bookInfo.getMonth(),
                            bookInfo.getDate(),
                            bookInfo.getStartTime()/100,
                            bookInfo.getStartTime()%100,
                            sec
                    );
            sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            dateNow = sdf.parse(strDateNow);
            Date dateAfter = sdf.parse(strDateAfter);

            mStrInput.append("Date now = ").append(strDateNow).append("\n");
            mStrOutput.append("(long)Date = ").append(dateNow.getTime()).append("\n");
            mStrInput.append("Date tested = ").append(strDateAfter).append("\n");
            mStrOutput.append("(long)Date = ").append(dateAfter.getTime()).append("\n");

            bookManager = new BookManager();
            mStrInput.append("CheckBookAfterNow() : ").append("\n");
            mStrOutput.append("Return = ").append(bookManager.CheckBookAfterNow(bookInfo)).append("\n");
            if (!bookManager.CheckBookAfterNow(bookInfo))
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "CheckBookAfterNow() return false when bookInfo is after now", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "CheckBookAfterNow() Pass!");
    }

    public void BtnSave_OnClick( View view )
    {
        final int btnIndex = 8;
        List<BookInfo> store = BookInfoGetList();
        if ( store == null )
        {
            store = new ArrayList<>();
        }

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        List<BookInfo> bookInfoList;
        BookInfo bookInfo;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            bookManager = new BookManager();
            bookInfoList = bookManager.BookList;
            mStrInput.append("new BookManager() : \n");
            mStrOutput.append("Size =  ").append(bookInfoList.size()).append("\n");

            bookInfoList.clear();
            mStrInput.append("BookList.clear() : \n");
            mStrOutput.append("Size =  ").append(bookInfoList.size()).append("\n");

            bookManager.Save();
            mStrInput.append("Save()\n");
            mStrOutput.append("BookInfoGetList() = ").append(BookInfoGetList()).append("\n");

            if ( BookInfoGetList() != null )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "BookInfoGetList() got non null when Save() empty", btnIndex);
                BookInfoUpdateList(store);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            bookManager = new BookManager();
            bookInfoList = bookManager.BookList;
            mStrInput.append("new BookManager() : \n");
            mStrOutput.append("Size =  ").append(bookInfoList.size()).append("\n");

            for ( int i = bookInfoList.size() ; i < BookInfo.MAX_NUM_OF_BOOKINFO ; i++ )
            {
                bookInfo = new BookInfo();
                bookInfo.setBookId(i);
                bookInfo.setEnable(1);
                bookInfo.setEventName("Added BookInfo " + i);
                bookInfoList.add(bookInfo);
            }

            mStrInput.append("Add MAX_NUM BookInfo : \n");
            mStrOutput.append("BookList.size() = ").append(bookInfoList.size()).append("\n");

            bookManager.Save();
            List<BookInfo> dbBookInfoList = BookInfoGetList();
            int size;
            if ( dbBookInfoList == null )
            {
                size = 0;
            }
            else
            {
                size = dbBookInfoList.size();
            }

            mStrInput.append("Save() : \n");
            mStrOutput.append("BookInfoGetList().size() = ").append(size).append("\n");

            if ( BookInfoGetList() == null || size < BookInfo.MAX_NUM_OF_BOOKINFO )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "Wrong size after Save()", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "Save() Pass!");
        BookInfoUpdateList(store);
    }

    public void BtnBookManagerInit_OnClick( View view )
    {
        final int btnIndex = 9;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        BookManager bookManager;
        String beforeInit;
        String afterInit;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            bookManager = new BookManager();
            beforeInit = bookManager.toString();

            //BookManagerInit();
            bookManager = GetBookManager();
            afterInit = bookManager.toString();

            mStrInput.append("Before Init : \n").append(beforeInit).append("\n");
            mStrOutput.append("After Init : \n").append(afterInit).append("\n");

            if ( beforeInit.equals(afterInit) )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "bookManager did not change after BookManagerInit();", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "BookManagerInit() Pass!");
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTBOOKMANAGER_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTBOOKMANAGER_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTBOOKMANAGER_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTBOOKMANAGER_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestBookManager");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);
    }

    private void GotError(View view, String errorMsg, int btnIndex)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());

    }

    private void TestPass(View view, String msg)
    {
        Button button = (Button) findViewById(view.getId());

//        SpannableStringBuilder style = new SpannableStringBuilder(msg);
//        style.setSpan(new ForegroundColorSpan(Color.BLACK), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        help.setHelpInfoText( style, msg );

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());

    }

    // show result(string) on textview
    private void ShowResultOnTXV(String result, TextView textView)
    {
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }

}
