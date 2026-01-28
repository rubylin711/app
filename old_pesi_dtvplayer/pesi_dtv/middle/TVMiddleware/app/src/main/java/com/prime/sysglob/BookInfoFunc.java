package com.prime.sysglob;

import com.prime.sysdata.BookInfo;

import java.util.List;

/**
 * Created by gary_hsu on 2017/11/20.
 */

public interface BookInfoFunc {
    public abstract List<BookInfo> GetBookInfoList();
    public abstract BookInfo GetBookInfo(int bookId);
    public abstract void Save(BookInfo bookInfo);
    public abstract void Save(List<BookInfo> bookInfo);
    public abstract void Delete(int bookId);
    public abstract void DeleteAll();
    public abstract void Add(BookInfo bookInfo);
    public abstract void Update(BookInfo bookInfo);
}
