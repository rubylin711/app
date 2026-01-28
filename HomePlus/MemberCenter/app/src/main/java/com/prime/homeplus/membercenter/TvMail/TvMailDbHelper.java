package com.prime.homeplus.membercenter.TvMail;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.os.Build;
import android.os.Trace;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.annotations.SerializedName;
import com.prime.homeplus.membercenter.data.AllMailHead;
import com.prime.homeplus.membercenter.data.TvMailHead;
import com.prime.homeplus.membercenter.enity.Content;
import com.prime.homeplus.membercenter.enity.Data;
import com.prime.homeplus.membercenter.enity.Extra;
import com.prime.homeplus.membercenter.enity.Intent;

import java.io.DataInput;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TvMailDbHelper extends SQLiteOpenHelper {
    private String TAG = "HomePlus-TvMailDbHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_tv_mail";

    public static final String TABLE_DATA = "tvmail_data";
    public static final String COLUMN_LAUMESSAGEPUBLISHID = "lauMessagePublishId";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_SCHEDULETIME = "scheduleTime";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_IMAGEURL = "imageUrl";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_ACTION = "tvmail_action";
    public static final String COLUMN_CONTENTURI = "contentUri";
    public static final String COLUMN_PACKAGENAME = "packageName";
    public static final String COLUMN_UPDATETIME = "updateTime";

    public static final String TABLE_EXTRAS = "data_extras";
    public static final String COLUMN_KEY = "tvmail_key";
    public static final String COLUMN_VALUE = "value";

    public static final String TABLE_TOTAL = "total_mails";
    public static final String COLUMN_MAILID = "mail_id";
    public static final String COLUMN_IMPORTANCE = "importance";
    public static final String COLUMN_MAILHEAD = "mailHead";
    public static final String COLUMN_NEWMAIL = "newEmail";
    public static final String COLUMN_MAILTYPE = "mailType";


    private static final String CREATE_DATA_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_DATA + "("
                    + COLUMN_LAUMESSAGEPUBLISHID + " TEXT NOT NULL PRIMARY KEY , "
                    + COLUMN_LEVEL + " INTEGER NOT NULL DEFAULT 1 , "
                    + COLUMN_SCHEDULETIME + " INTEGER NOT NULL DEFAULT '' , "
                    + COLUMN_TITLE + " TEXT NOT NULL DEFAULT '' , "
                    + COLUMN_IMAGEURL + " TEXT , "
                    + COLUMN_TEXT + " TEXT , "
                    + COLUMN_ACTION + " TEXT , "
                    + COLUMN_CONTENTURI + " TEXT , "
                    + COLUMN_PACKAGENAME + " TEXT , "
                    + COLUMN_NEWMAIL + " INTEGER NOT NULL DEFAULT 1 , "
                    + COLUMN_UPDATETIME + " TEXT "
                    + " )";

    private static final String CREATE_EXTRAS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EXTRAS + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_LAUMESSAGEPUBLISHID + " TEXT NOT NULL , "
                    + COLUMN_KEY + " TEXT , "
                    + COLUMN_VALUE + " TEXT"
                    + " )";

    private static final String CREATE_TOTALMAIL_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TOTAL + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_MAILID + " TEXT NOT NULL , "
                    + COLUMN_SCHEDULETIME + " INTEGER , "
                    + COLUMN_IMPORTANCE + " INTEGER , "
                    + COLUMN_MAILHEAD + " TEXT , "
                    + COLUMN_NEWMAIL + " INTEGER , "
                    + COLUMN_MAILTYPE + " TEXT"
                    + " )";


    public TvMailDbHelper(Context context) {
        super(context, DATABASE_NAME, new SQLiteDatabase.CursorFactory() {
            @Override
            public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
                                    String editTable, SQLiteQuery query) {
                return new SQLiteCursor(masterQuery, editTable, query);
            }
        }, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATA_TABLE);
        db.execSQL(CREATE_EXTRAS_TABLE);
        db.execSQL(CREATE_TOTALMAIL_TABLE);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DO NOT USE CONSTANTS FOR DB UPGRADE STEPS, USE ONLY LITERAL SQL STRINGS!
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        Trace.beginSection("getWritableDatabase");
        try {
            return super.getWritableDatabase();
        } finally {
            Trace.endSection();
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        Trace.beginSection("getReadableDatabase");
        try {
            return super.getReadableDatabase();
        } finally {
            Trace.endSection();
        }
    }

    public void insertTvMailData(Data data) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LAUMESSAGEPUBLISHID, data.getLauMessagePublishId());
        values.put(COLUMN_LEVEL, data.getLevel());
        values.put(COLUMN_SCHEDULETIME, data.getScheduleTime());
        values.put(COLUMN_TITLE, data.getTitle());
        values.put(COLUMN_IMAGEURL, data.getContent().getImageUrl());
        values.put(COLUMN_TEXT, data.getContent().getText());
        values.put(COLUMN_ACTION, data.getContent().getIntent().getAction());
        values.put(COLUMN_CONTENTURI, data.getContent().getIntent().getContentUri());
        values.put(COLUMN_PACKAGENAME, data.getContent().getIntent().getPackageName());
        values.put(COLUMN_NEWMAIL, 1);
        db.insert(TABLE_DATA, null, values);

        insertExtras(data.getLauMessagePublishId().toString(), data.getContent().getIntent().getExtras());

        String countQuery = "SELECT  * FROM " + TABLE_DATA;
        SQLiteDatabase dbCount = this.getReadableDatabase();
        Cursor cursor = dbCount.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        Log.d(TAG, "tvmail count: " + count);
    }

    public void insertExtras(String dataID, ArrayList<Extra> extras) {
        SQLiteDatabase db = getWritableDatabase();
        if ((extras != null) && (extras.size() > 0)) {
            for (Extra extra : extras) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_LAUMESSAGEPUBLISHID, dataID);
                values.put(COLUMN_KEY, extra.getKey());
                values.put(COLUMN_VALUE, extra.getValue());
                db.insert(TABLE_EXTRAS, null, values);
            }
        }
        db.close();
    }


    public void deleteMail(String mailId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATA, COLUMN_LAUMESSAGEPUBLISHID + " = " + mailId, null);
        db.close();
    }

    public void updateIsRead(String mailId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NEWMAIL, 0);

        db.update(TABLE_DATA, values, COLUMN_LAUMESSAGEPUBLISHID + " = " + mailId, null);
        db.close();
    }

    public Data selectTvMailFromId(String mailId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DATA + " WHERE " + COLUMN_LAUMESSAGEPUBLISHID + " = '" + mailId + "'", null);
        Data data = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String lauMessagePublishId = cursor.getString(cursor.getColumnIndex(COLUMN_LAUMESSAGEPUBLISHID));
                int level = cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL));
                int scheduleTime = cursor.getInt(cursor.getColumnIndex(COLUMN_SCHEDULETIME));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String imageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGEURL));
                String text = cursor.getString(cursor.getColumnIndex(COLUMN_TEXT));
                String tvmail_action = cursor.getString(cursor.getColumnIndex(COLUMN_ACTION));
                String contentUri = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENTURI));
                String packageName = cursor.getString(cursor.getColumnIndex(COLUMN_PACKAGENAME));
                int newMail = cursor.getInt(cursor.getColumnIndex(COLUMN_NEWMAIL));

                ArrayList<Extra> extras = selectExtrasFromId(lauMessagePublishId);
                Intent intent = new Intent(tvmail_action, contentUri, extras, packageName);
                Content content = new Content(imageUrl, intent, text);
                data = new Data(content, Long.parseLong(lauMessagePublishId), level, scheduleTime, title, newMail);
            } while (cursor.moveToNext());
        }
        db.close();
        return data;

    }

    private ArrayList<Extra> selectExtrasFromId(String mailId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXTRAS + " WHERE " + COLUMN_LAUMESSAGEPUBLISHID + " = '" + mailId + "'", null);
        ArrayList<Extra> extras = new ArrayList<Extra>();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String key = cursor.getString(cursor.getColumnIndex(COLUMN_KEY));
                String value = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));

                Extra extra = new Extra(key, value);
                extras.add(extra);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return extras;
    }

    public List<TvMailHead> selectTvMailHeads() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DATA, null);
        List<TvMailHead> TvMailHeadList = new ArrayList<TvMailHead>();

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String actionId = cursor.getString(cursor.getColumnIndex(COLUMN_LAUMESSAGEPUBLISHID));
                int importance = cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL));
                String mailHead = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                int newEmail = cursor.getInt(cursor.getColumnIndex(COLUMN_NEWMAIL));
                int createTime = cursor.getInt(cursor.getColumnIndex(COLUMN_SCHEDULETIME));
                TvMailHead tvMailHead = new TvMailHead(actionId, createTime, importance, mailHead, newEmail);
                TvMailHeadList.add(tvMailHead);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return TvMailHeadList;
    }

    public <T> List<AllMailHead> mergeAndGetAllMails() {
        List<TvMailHead> tvMailHeads = selectTvMailHeads();
        List<AllMailHead> allMailHeads = new ArrayList<AllMailHead>();
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("delete from " + TABLE_TOTAL);

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // Add CA MAIL into

        if ((tvMailHeads != null) && (tvMailHeads.size() > 0)) {
            for (TvMailHead tvMailHead : tvMailHeads) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_MAILID, tvMailHead.getActionId());
                values.put(COLUMN_SCHEDULETIME, tvMailHead.getCreateTime());
                values.put(COLUMN_IMPORTANCE, tvMailHead.getImportance());
                values.put(COLUMN_MAILHEAD, tvMailHead.getMailHead());
                values.put(COLUMN_NEWMAIL, tvMailHead.getNewEmail());
                values.put(COLUMN_MAILTYPE, "TVMAIL");
                db.insert(TABLE_TOTAL, null, values);
            }
        }

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TOTAL + " ORDER BY " + COLUMN_SCHEDULETIME + " DESC ", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String mail_id = cursor.getString(cursor.getColumnIndex(COLUMN_MAILID));
                int createTime = cursor.getInt(cursor.getColumnIndex(COLUMN_SCHEDULETIME));
                int importance = cursor.getInt(cursor.getColumnIndex(COLUMN_IMPORTANCE));
                String mailHead = cursor.getString(cursor.getColumnIndex(COLUMN_MAILHEAD));
                int newEmail = cursor.getInt(cursor.getColumnIndex(COLUMN_NEWMAIL));
                String mailType = cursor.getString(cursor.getColumnIndex(COLUMN_MAILTYPE));


                AllMailHead allMailHead = new AllMailHead(mail_id, createTime, importance, mailHead, newEmail, mailType);
                allMailHeads.add(allMailHead);


            } while (cursor.moveToNext());
        }
        db.close();
        return allMailHeads;
    }
}
