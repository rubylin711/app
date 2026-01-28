package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.sysdata.BookInfo;

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

                if (newBookInfo.check_conflict(bookInfo)) {
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
}
