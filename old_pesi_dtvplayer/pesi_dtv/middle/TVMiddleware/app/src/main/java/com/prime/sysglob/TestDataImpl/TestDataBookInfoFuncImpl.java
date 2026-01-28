package com.prime.sysglob.TestDataImpl;

import android.content.Context;
import android.util.Log;

import com.prime.TestData.TestData;
import com.prime.sysdata.BookInfo;
import com.prime.sysglob.BookInfoFunc;
import com.prime.tvclient.TestDataTVClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnny_shih on 2017/11/23.
 */

public class TestDataBookInfoFuncImpl implements BookInfoFunc {
    private static final String TAG = "TestDataBookInfoFuncImpl";

    private Context context;
    private TestData testData = null;

    public TestDataBookInfoFuncImpl(Context context) {
        this.context = context;
        //this.testData = new TestData( TpInfo.DVBC );
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public List<BookInfo> GetBookInfoList() {
        Log.d(TAG, "GetBookInfoList");
        return query();
    }

    @Override
    public BookInfo GetBookInfo(int bookId) {
        Log.d(TAG, "GetBookInfo");
        return query(bookId);
    }

    @Override
    public void Save(BookInfo bookInfo) {
        if(bookInfo.getEnable() == 0) {
            remove(bookInfo);
            List<BookInfo> bookInfoList = query();
            if ( bookInfoList == null )
            {
                return;
            }

            for(int i = 0; i < bookInfoList.size(); i++) {
                bookInfoList.get(i).setBookId(i);
            }

            updateAll(bookInfoList);
        }
        else {
            BookInfo DBBookInfo = query(bookInfo.getBookId());
            if(DBBookInfo == null) {
                add(bookInfo);
            }
            else {
                update(bookInfo);
            }
        }
    }

    @Override
    public void Save(List<BookInfo> bookInfo) {
        updateAll(bookInfo);
    }

    @Override
    public void Delete(int bookId) {
        List<BookInfo> BookList = query();
        if ( BookList == null )
        {
            return;
        }

        for(int i = 0; i < BookList.size(); i++) {
            if(BookList.get(i).getBookId() == bookId) {
                remove(BookList.get(i)); // remove real data
                BookList.remove(i);      // remove query data
                break;
            }
        }

        for(int i = 0; i < BookList.size(); i++) {
            BookList.get(i).setBookId(i);
        }

        Save(BookList);
    }

    @Override
    public void DeleteAll() {
        testData.GetTestDatBookInfoList().clear();
    }

    @Override
    public void Add(BookInfo bookInfo) {
        if(bookInfo.getEnable() == 1) {
            add(bookInfo);
        }
    }

    @Override
    public void Update(BookInfo bookInfo) {
        if(bookInfo.getEnable() == 0) {
            remove(bookInfo);
            List<BookInfo> bookInfoList = query();
            if ( bookInfoList == null )
            {
                return;
            }

            for(int i = 0; i < bookInfoList.size(); i++) {
                bookInfoList.get(i).setBookId(i);
            }
            updateAll(bookInfoList);
        }
        else {
            update(bookInfo);
        }
    }

    private void add(BookInfo bookInfo) {
        BookInfo curBookInfo = new BookInfo();
        curBookInfo.setBookId(bookInfo.getBookId());
        curBookInfo.setChannelId(bookInfo.getChannelId());
        curBookInfo.setGroupType(bookInfo.getGroupType());
        curBookInfo.setEventName(bookInfo.getEventName());
        curBookInfo.setBookType(bookInfo.getBookType());
        curBookInfo.setBookCycle(bookInfo.getBookCycle());
        curBookInfo.setYear(bookInfo.getYear());
        curBookInfo.setMonth(bookInfo.getMonth());
        curBookInfo.setDate(bookInfo.getDate());
        curBookInfo.setWeek(bookInfo.getWeek());
        curBookInfo.setStartTime(bookInfo.getStartTime());
        curBookInfo.setDuration(bookInfo.getDuration());
        curBookInfo.setEnable(bookInfo.getEnable());

        Log.d(TAG, "add "+ bookInfo.ToString());
        testData.GetTestDatBookInfoList().add(curBookInfo);
    }

    private void update(BookInfo bookInfo) {
        int count = 0;
        BookInfo curBookInfo = new BookInfo();
        curBookInfo.setBookId(bookInfo.getBookId());
        curBookInfo.setChannelId(bookInfo.getChannelId());
        curBookInfo.setGroupType(bookInfo.getGroupType());
        curBookInfo.setEventName(bookInfo.getEventName());
        curBookInfo.setBookType(bookInfo.getBookType());
        curBookInfo.setBookCycle(bookInfo.getBookCycle());
        curBookInfo.setYear(bookInfo.getYear());
        curBookInfo.setMonth(bookInfo.getMonth());
        curBookInfo.setDate(bookInfo.getDate());
        curBookInfo.setWeek(bookInfo.getWeek());
        curBookInfo.setStartTime(bookInfo.getStartTime());
        curBookInfo.setDuration(bookInfo.getDuration());
        curBookInfo.setEnable(bookInfo.getEnable());

        Log.d(TAG, "update " + bookInfo.ToString());
        List<BookInfo> bookInfoList = testData.GetTestDatBookInfoList();
        for ( int i = 0 ; i < bookInfoList.size() ; i++)
        {
            if (curBookInfo.getBookId() == bookInfoList.get(i).getBookId())
            {
                bookInfoList.set(i, curBookInfo);
                count++;
            }
        }
    }

    private void updateAll(List<BookInfo> bookInfoList) {
        if(bookInfoList.size() > 0) {
            testData.GetTestDatBookInfoList().clear();
            for (int i = 0; i < bookInfoList.size(); i++) {
                BookInfo bookInfo = bookInfoList.get(i);
                add(bookInfo);
            }
        }
    }

    private BookInfo query(int bookID) {
        List<BookInfo> bookInfoList = testData.GetTestDatBookInfoList();
        if(bookInfoList == null || bookInfoList.isEmpty()) {
            return null;
        }

        for ( int i = 0 ; i < bookInfoList.size() ; i++ )
        {
            BookInfo curBookInfo = bookInfoList.get(i);
            if ( curBookInfo.getBookId() == bookID )
            {
                return ParseCursor( curBookInfo );
            }
        }

        return null;
    }

    private List<BookInfo> query() {
        List<BookInfo> bookInfoList = testData.GetTestDatBookInfoList();
        if(bookInfoList == null || bookInfoList.isEmpty()) {
            return null;
        }

        List<BookInfo> pBooks = new ArrayList<>();
        BookInfo pBook;
        for ( int i = 0 ; i < bookInfoList.size() ; i++ )
        {
            BookInfo curBookInfo = bookInfoList.get(i);
            pBook = ParseCursor( curBookInfo );
            pBooks.add( pBook );
        }

        return pBooks;
    }

    private void remove(BookInfo bookInfo) {
        int count = 0;
        List<BookInfo> bookInfoList = testData.GetTestDatBookInfoList();
        for ( int i = 0 ; i < bookInfoList.size() ; i++ )
        {
            if ( bookInfoList.get(i).getBookId() == bookInfo.getBookId() )
            {
                Log.d(TAG, "remove : where = "+ bookInfoList.get(i).getBookId() + "=" + bookInfo.getBookId());
                bookInfoList.remove(i);
                i--;
                count++;
            }
        }
    }

    private BookInfo ParseCursor(BookInfo bookInfo)
    {
        BookInfo resultBookInfo = new BookInfo();
        resultBookInfo.setBookId(bookInfo.getBookId());
        resultBookInfo.setChannelId(bookInfo.getChannelId());
        resultBookInfo.setGroupType(bookInfo.getGroupType());
        resultBookInfo.setEventName(bookInfo.getEventName());
        resultBookInfo.setBookType(bookInfo.getBookType());
        resultBookInfo.setBookCycle(bookInfo.getBookCycle());
        resultBookInfo.setYear(bookInfo.getYear());
        resultBookInfo.setMonth(bookInfo.getMonth());
        resultBookInfo.setDate(bookInfo.getDate());
        resultBookInfo.setWeek(bookInfo.getWeek());
        resultBookInfo.setStartTime(bookInfo.getStartTime());
        resultBookInfo.setDuration(bookInfo.getDuration());
        resultBookInfo.setEnable(bookInfo.getEnable());

        return resultBookInfo;
    }
}
