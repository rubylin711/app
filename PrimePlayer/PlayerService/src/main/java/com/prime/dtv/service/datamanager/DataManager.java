package com.prime.dtv.service.datamanager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.TvContractCompat.Channels;

import com.prime.datastructure.TIF.TIFChannelData;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.MailData;
import com.prime.datastructure.sysdata.CNSMailData;
import com.prime.datastructure.sysdata.CNSEasData;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.ServiceInterface;
import com.prime.dtv.service.database.DVBDatabase;
import com.prime.dtv.service.database.dvbdatabasetable.BookInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.CNSMailDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.CNSEasDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.FavGroupNameDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.FavInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.GposDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.MailDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.ProgramDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.SatInfoDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.TpInfoDatabaseTable;
import com.prime.dtv.service.database.netstreamdatabasetable.NetProgramDatabaseTable;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.DefaultChannel;
import com.prime.datastructure.sysdata.FavGroupName;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.datastructure.sysdata.NetProgramInfo;
import com.prime.datastructure.sysdata.ProgramPlayStreamType;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.SimpleChannel;
import com.prime.datastructure.sysdata.TpInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import vendor.prime.hardware.dtvservice.V1_0.HIDL_BUFFER_S;

public class DataManager {
    private static final String TAG = "DataManager";
    public static final int SV_GPOS = 0x01;
    public static final int SV_PROGRAM_INFO = 0x02;
    // public static final int SV_FAV_GROUP_NAME = 0x04;
    public static final int SV_FAV_INFO = 0x04;
    public static final int SV_SAT_INFO = 0x08;
    public static final int SV_TP_INFO = 0x10;
    public static final int SV_BOOK_INFO = 0x20;
    public static final int SV_ALL = 0x40;
    public static final int SV_RESET_DEFAULT = 0x80;
    // private final Semaphore mSemaphore;

    // public static final int CaSystemId = 0;
    public static final int TUNR_TYPE = TpInfo.DVBC;
    public static final int MAX_GROUP_CH_NUM = 500;
    public int LAST_LCN = 1;
    public String mPvrRecPath;
    public List<SatInfo> mSatInfoList;
    public List<TpInfo> mTpInfoList;
    public GposInfo mGposInfo;
    public List<ProgramInfo> mProgramInfoList = new ArrayList<>();
    public List<FavGroup> mFavGroupList = new ArrayList<>();
    private static final Object mFavGroupListLock = new Object();
    public List<BookInfo> mBookInfoList = new ArrayList<>();
    public List<HIDL_BUFFER_S> mHidlBufferList = new ArrayList<>();
    private Context mContext;
    Thread mSaveDataThread;
    // SaveDataRunnable mSaveDataRunnable = null;
    private HandlerThread mHandlerThread = null;
    private Handler mHandlerThreadHandler = null;

    public static final int TYPE_SAVE_TABLE = 1;
    public static final int TYPE_SAVE_GPOS_CURSOR_LONG = 2;
    public static final int TYPE_SAVE_GPOS_CURSOR_STRING = 3;
    public static final int TYPE_SAVE_GPOS_CURSOR_INT = 4;
    public static final int TYPE_SAVE_PROGRAM_CURSOR = 5;
    public static final int TYPE_DEL_PROGRAM_CURSOR = 6;
    public static final int TYPE_SAVE_FAVINFO_CURSOR = 7;
    public static final int TYPE_DEL_FAVINFO_CURSOR = 8;
    public static final int TYPE_SAVE_BOOKINFO_CURSOR = 9;
    public static final int TYPE_DEL_BOOKINFO_CURSOR = 10;
    public static final int TYPE_SAVE_TPINFO_CURSOR = 11;
    public static final int TYPE_BACKUP_DB = 12;
    public static final int TYPE_SAVE_MAILDATA_CURSOR = 13;
    public static final int TYPE_DEL_MAILDATA_CURSOR = 14;
    public static final int TYPE_SAVE_CNSMAILDATA_CURSOR = 15;
    public static final int TYPE_DEL_CNSMAILDATA_CURSOR = 16;
    public static final int TYPE_SAVE_CNSEASDATA_CURSOR = 17;
    public static final int TYPE_DEL_CNSEASDATA_CURSOR = 18;

    private static DataManager mDataManager;
    private ProgramDatabase mProgramDatabase = null;
    private NetProgramDatabase mNetProgramDatabase = null;

    private static final Object mProgramListLock = new Object();
    private final Semaphore mProgramListSemphore = new Semaphore(1);
    private boolean DEBUG_SEMAPHORE = false;
    private static final Object mCNSSITableDataLock = new Object();
    private CNSSITableData mCNSSITableData = null;

    private String mTvInputId = Pvcfg.getTvInputId();

    public static DataManager getDataManager() {
        return mDataManager;
    }

    private void DataManagerResetDefault() {
        mGposInfo = null;
        mSatInfoList.clear();
        mTpInfoList.clear();
        mProgramInfoList.clear();
        mFavGroupList.clear();
        mBookInfoList.clear();
        DefaultValue defaultValue = new DefaultValue(mContext, Pvcfg.getTunerType());
        mGposInfo = defaultValue.gposInfo;
        mSatInfoList = defaultValue.satInfoList;
        mTpInfoList = defaultValue.tpInfoList;
        for (FavGroupName favGroupName : defaultValue.favGroupNameList) {
            mFavGroupList.add(new FavGroup(favGroupName));
        }
        mCNSSITableData = null;
    }

    private void DataManagerCleanAllDataBase() {
        // mGposInfo don't need remove
        new SatInfoDatabaseTable(mContext).removeAll();
        new TpInfoDatabaseTable(mContext).removeAll();
        if (Pvcfg.isPrimeTvInputEnable())
            TIFChannelData.deleteTIFChannelDataByTvInputId(mContext, Pvcfg.getTvInputId());
        new ProgramDatabaseTable(mContext).removeAll();
        new BookInfoDatabaseTable(mContext).removeAll();
        new FavInfoDatabaseTable(mContext).removeAll();
        new FavGroupNameDatabaseTable(mContext).removeAll();
    }

    public final static DataManager getDataManager(Context context) {
        if (mDataManager == null) {
            mDataManager = new DataManager(context);
        } else {
            mDataManager.mContext = context;
        }
        return mDataManager;
    }

    public TpInfo getTpInfoByFreq(int i) {
        TpInfo tp = null;
        for (TpInfo tpInfo : mTpInfoList) {
            if (tpInfo.CableTp.getFreq() == i) {
                tp = tpInfo;
            }
        }
        return tp;
    }

    public CNSSITableData getCNSSITableData() {
        synchronized (mCNSSITableDataLock) {
            return mCNSSITableData;
        }
    }

    public void setCNSSITableData(CNSSITableData data) {
        synchronized (mCNSSITableDataLock) {
            this.mCNSSITableData = data;
        }
    }

    public class Gpos_Cusror {
        private String key;
        private long long_value;
        private String string_value;
        private int int_value;
        private int type; // 0: String 1:long 2:int

        public static final int CURSOR_TYPE_STRING = 0;
        public static final int CURSOR_TYPE_LONG = 1;
        public static final int CURSOR_TYPE_INT = 2;

        public Gpos_Cusror(String key, long value) {
            this.type = CURSOR_TYPE_LONG;
            this.key = key;
            this.long_value = value;
        }

        public Gpos_Cusror(String key, String value) {
            this.type = CURSOR_TYPE_STRING;
            this.key = key;
            this.string_value = value;
        }

        public Gpos_Cusror(String key, int value) {
            this.type = CURSOR_TYPE_INT;
            this.key = key;
            this.int_value = value;
        }

        public int getType() {
            return this.type;
        }

        public int getInt_value() {
            return int_value;
        }

        public String getString_value() {
            return string_value;
        }

        public long getLong_value() {
            return long_value;
        }

        public String getKey() {
            return key;
        }
    }

    // public class SaveDataRunnable implements Runnable {
    // private int saveFlag;
    // public SaveDataRunnable(){
    // saveFlag = 0;
    // }
    // public SaveDataRunnable(int flag) {
    // saveFlag = flag;
    // }
    // public void setSaveFlag(int flag){
    // saveFlag |= flag;
    // }
    //
    // @Override
    // public void run() {
    // while(true) {
    // try {
    // Log.d(TAG, "SaveDataRunnable ..01");
    // //mSemaphore.acquire();
    // Log.d(TAG, "SaveDataRunnable ..02 saveFlag = "+saveFlag);
    // if(saveFlag != 0) {
    // DataManagerSaveDataToDataBase(saveFlag);
    // saveFlag = 0;
    // }
    // Thread.sleep(1000);
    // Log.d(TAG, "SaveDataRunnable ..03");
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // }
    private void resetTpListSaveStatus() {
        for (TpInfo tpInfo : mTpInfoList) {
            tpInfo.setStatus(MiscDefine.SaveStatus.STATUS_NONE);
        }
    }

    private void resetFavListSaveStatus() {
        for (FavGroup favGroup : mFavGroupList) {
            for (FavInfo favInfo : favGroup.getFavInfoList()) {
                favInfo.setStatus(MiscDefine.SaveStatus.STATUS_NONE);
            }
        }
    }

    private void resetBookInfoListSaveStatus() {
        for (BookInfo bookInfo : mBookInfoList) {
            bookInfo.setStatus(MiscDefine.SaveStatus.STATUS_NONE);
        }
    }

    private void DataManagerSaveDataToDataBase(int flag) {
        if ((flag & SV_RESET_DEFAULT) == SV_RESET_DEFAULT) {
            Log.d(TAG, "SV_RESET_DEFAULT");
            // do DataManagerResetDefault() first
            // prevent UI can still get program right after they call ResetDefault()
            DataManagerResetDefault();
            DataManagerCleanAllDataBase();
            if (mGposInfo != null)
                new GposDatabaseTable(mContext).save(mGposInfo);
            if (mSatInfoList.size() != 0)
                new SatInfoDatabaseTable(mContext).save(mSatInfoList);
            if (mTpInfoList.size() != 0)
                new TpInfoDatabaseTable(mContext).save(mTpInfoList);
            if (mProgramInfoList.size() != 0)
                new ProgramDatabaseTable(mContext).save(mProgramInfoList);
            if (mBookInfoList.size() != 0)
                new BookInfoDatabaseTable(mContext).save(mBookInfoList);
            if (mFavGroupList.size() != 0) {
                List<FavGroupName> favGroupNameList = new ArrayList<>();
                for (int i = 0; i < mFavGroupList.size(); i++) {
                    favGroupNameList.add(mFavGroupList.get(i).getFavGroupName());
                }
                new FavGroupNameDatabaseTable(mContext).save(favGroupNameList);
            }
            resetTpListSaveStatus();
            resetFavListSaveStatus();
            mCNSSITableData = new CNSSITableData();
        } else {
            if (((flag & SV_GPOS) == SV_GPOS) || ((flag & SV_ALL) == SV_ALL)) {
                Log.d(TAG, "SV_GPOS || SV_ALL");
                if (mGposInfo != null)
                    new GposDatabaseTable(mContext).save(mGposInfo);
            }
            if (((flag & SV_PROGRAM_INFO) == SV_PROGRAM_INFO) || ((flag & SV_ALL) == SV_ALL)) {
                Log.d(TAG, "SV_PROGRAM_INFO || SV_ALL");
                if (mProgramInfoList.size() != 0)
                    new ProgramDatabaseTable(mContext).save(mProgramInfoList);
            }
            if (((flag & SV_FAV_INFO) == SV_FAV_INFO) || ((flag & SV_ALL) == SV_ALL)) {
                Log.d(TAG, "SV_FAV_INFO || SV_ALL");
                if (mFavGroupList.size() != 0) {
                    List<FavGroupName> favGroupNameList = new ArrayList<>();
                    for (int i = 0; i < mFavGroupList.size(); i++) {
                        favGroupNameList.add(mFavGroupList.get(i).getFavGroupName());
                    }
                    new FavGroupNameDatabaseTable(mContext).save(favGroupNameList);
                    FavInfoDatabaseTable favtable = new FavInfoDatabaseTable(mContext);
                    List<FavInfo> favInfoList = new ArrayList<>();
                    // favtable.removeAll();
                    for (int i = 0; i < mFavGroupList.size(); i++) {
                        favInfoList.addAll(mFavGroupList.get(i).getFavInfoList());
                    }

                    favtable.save(favInfoList);
                    resetFavListSaveStatus();
                }
            }
            if (((flag & SV_SAT_INFO) == SV_SAT_INFO) || ((flag & SV_ALL) == SV_ALL)) {
                Log.d(TAG, "SV_SAT_INFO || SV_ALL");
                if (mSatInfoList.size() != 0)
                    new SatInfoDatabaseTable(mContext).save(mSatInfoList);
            }
            if (((flag & SV_TP_INFO) == SV_TP_INFO) || ((flag & SV_ALL) == SV_ALL)) {
                Log.d(TAG, "SV_TP_INFO || SV_ALL");
                if (mTpInfoList.size() != 0) {
                    new TpInfoDatabaseTable(mContext).save(mTpInfoList);
                    resetTpListSaveStatus();
                }
            }
            if (((flag & SV_BOOK_INFO) == SV_BOOK_INFO) || ((flag & SV_ALL) == SV_ALL)) {
                Log.d(TAG, "SV_BOOK_INFO || SV_ALL");
                // mBookInfoList should be saved to database even if it is empty
                new BookInfoDatabaseTable(mContext).save(mBookInfoList);
                resetBookInfoListSaveStatus();
            }
        }
    }

    public void DataManagerDeleteProgramData(long channelId) {
        ProgramInfo p = getProgramInfo(channelId);
        if (p != null) {
            Message message = new Message();
            message.what = TYPE_DEL_PROGRAM_CURSOR;
            message.obj = p;
            mHandlerThreadHandler.sendMessage(message);
        }
        // if(p != null){
        // LogUtils.d("DataManagerDeleteProgramData 111");
        // new ProgramDatabaseTable(mContext).remove(p);
        // LogUtils.d("DataManagerDeleteProgramData 222");
        // }
    }

    public void DataManagerSaveProgramData(ProgramInfo p) {
        if (p != null) {
            LogUtils.d(" CH " + p.getDisplayNum() + " " + p.getDisplayName());
            Message message = new Message();
            message.what = TYPE_SAVE_PROGRAM_CURSOR;
            message.obj = p;
            mHandlerThreadHandler.sendMessage(message);
        }
    }

    public void DataManagerSaveGposData(String key, int value) {
        Message message = new Message();
        message.what = TYPE_SAVE_GPOS_CURSOR_INT;
        message.obj = new Gpos_Cusror(key, value);
        mHandlerThreadHandler.sendMessage(message);
    }

    public void DataManagerSaveGposData(String key, String value) {
        Message message = new Message();
        message.what = TYPE_SAVE_GPOS_CURSOR_STRING;
        message.obj = new Gpos_Cusror(key, value);
        mHandlerThreadHandler.sendMessage(message);
        // if(mGposInfo != null)
        // new GposDatabaseTable(mContext).save(key, value);
        // else{
        // LogUtils.e("Save Gpos Data Fail");
        // }
    }

    public void DataManagerSaveGposData(String key, long value) {
        Message message = new Message();
        message.what = TYPE_SAVE_GPOS_CURSOR_LONG;
        message.obj = new Gpos_Cusror(key, value);
        mHandlerThreadHandler.sendMessage(message);
        // LogUtils.d("DataManagerSaveGposData key["+key+"] value["+value+"]");
        // if(mGposInfo != null)
        // new GposDatabaseTable(mContext).save(key, value);
        // else{
        // LogUtils.e("Save Gpos Data Fail");
        // }
    }

    public void DataManagerSaveFavInfo(FavInfo favInfo) {
        Message message = new Message();
        message.what = TYPE_SAVE_FAVINFO_CURSOR;
        message.obj = favInfo;
        mHandlerThreadHandler.sendMessage(message);
        // LogUtils.d("DataManagerSaveFavInfo favInfo = "+favInfo.ToString());
        // if(bookInfo != null) {
        // new BookInfoDatabaseTable(mContext).save(bookInfo);
        // }
    }

    public void DataManagerDeleteFavInfo(FavInfo favInfo) {
        Message message = new Message();
        message.what = TYPE_DEL_FAVINFO_CURSOR;
        message.obj = favInfo;
        mHandlerThreadHandler.sendMessage(message);
        // LogUtils.d("DataManagerDeleteFavInfo favInfo = "+favInfo.ToString());
        // if(bookInfo != null) {
        // new BookInfoDatabaseTable(mContext).save(bookInfo);
        // }
    }

    // save one bookInfo to database
    public void DataManagerSaveBookInfo(BookInfo bookInfo) {
        Message message = new Message();
        message.what = TYPE_SAVE_BOOKINFO_CURSOR;
        message.obj = bookInfo;
        mHandlerThreadHandler.sendMessage(message);
        // if(bookInfo != null) {
        // new BookInfoDatabaseTable(mContext).save(bookInfo);
        // }
    }

    public void DataManagerDeleteBookInfo(BookInfo bookInfo) {
        Message message = new Message();
        message.what = TYPE_DEL_BOOKINFO_CURSOR;
        message.obj = bookInfo;
        mHandlerThreadHandler.sendMessage(message);
        // if(bookInfo != null) {
        // new BookInfoDatabaseTable(mContext).save(bookInfo);
        // }
    }

    public void DataManagerSaveTpInfo(TpInfo tpInfo) {
        Message message = new Message();
        message.what = TYPE_SAVE_TPINFO_CURSOR;
        message.obj = tpInfo;
        mHandlerThreadHandler.sendMessage(message);
        // if(bookInfo != null) {
        // new BookInfoDatabaseTable(mContext).save(bookInfo);
        // }
    }

    public void DataManagerSaveData(int flag) {
        // mSaveDataRunnable.setSaveFlag(flag);
        LogUtils.d("SV_flag " + flag);
        // mSaveDataThread = new Thread(new
        // SaveDataRunnable(flag),"DataManagerSaveData");
        // mSaveDataThread.start();
        Message message = new Message();
        message.what = TYPE_SAVE_TABLE;
        message.arg1 = flag;
        mHandlerThreadHandler.sendMessage(message);
    }

    public void DataManagerSaveMailData(MailData mailData) {
        // mSaveDataRunnable.setSaveFlag(flag);
        // mSaveDataThread = new Thread(new
        // SaveDataRunnable(flag),"DataManagerSaveData");
        // mSaveDataThread.start();
        Message message = new Message();
        message.what = TYPE_SAVE_MAILDATA_CURSOR;
        message.obj = mailData;
        mHandlerThreadHandler.sendMessage(message);
    }

    public void DataManagerDelMailData(int mail_id) {
        // mSaveDataRunnable.setSaveFlag(flag);
        // mSaveDataThread = new Thread(new
        // SaveDataRunnable(flag),"DataManagerSaveData");
        // mSaveDataThread.start();
        Message message = new Message();
        message.what = TYPE_DEL_MAILDATA_CURSOR;
        message.arg1 = mail_id;
        mHandlerThreadHandler.sendMessage(message);
    }

    public void DataManagerBackupDatabase(boolean force) {
        Message message = new Message();
        message.what = TYPE_BACKUP_DB;
        message.obj = force;
        mHandlerThreadHandler.sendMessage(message);
    }

    public DataManager(Context context) {
        // mSemaphore = new Semaphore(1);
        // mSaveDataRunnable = new SaveDataRunnable();
        // mSaveDataThread = new Thread(mSaveDataRunnable, "DataManagerSaveData");
        // mSaveDataThread.start();
        mHandlerThread = new HandlerThread(getClass().getName()/* ,Process.THREAD_PRIORITY_AUDIO */);
        mHandlerThread.start();
        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case TYPE_SAVE_TABLE: {
                        // LogUtils.d("SV_TYPE_SAVE_TABLE 111 "+msg.arg1);
                        DataManagerSaveDataToDataBase(msg.arg1);
                        // LogUtils.d("SV_TYPE_SAVE_TABLE 222 "+msg.arg1);
                    }
                        break;
                    case TYPE_SAVE_GPOS_CURSOR_LONG: {
                        // LogUtils.d("SV_TYPE_SAVE_GPOS_CURSOR_LONG 111");
                        Gpos_Cusror c = (Gpos_Cusror) msg.obj;
                        new GposDatabaseTable(mContext).save(c.getKey(), c.getLong_value());
                        // LogUtils.d("SV_TYPE_SAVE_GPOS_CURSOR_LONG 222");
                    }
                        break;
                    case TYPE_SAVE_GPOS_CURSOR_STRING: {
                        // LogUtils.d("SV_TYPE_SAVE_GPOS_CURSOR_STRING 111");
                        Gpos_Cusror c = (Gpos_Cusror) msg.obj;
                        new GposDatabaseTable(mContext).save(c.getKey(), c.getString_value());
                        // LogUtils.d("SV_TYPE_SAVE_GPOS_CURSOR_STRING 222");
                    }
                        break;
                    case TYPE_SAVE_GPOS_CURSOR_INT: {
                        // LogUtils.d("SV_TYPE_SAVE_GPOS_CURSOR_INT 111");
                        Gpos_Cusror c = (Gpos_Cusror) msg.obj;
                        new GposDatabaseTable(mContext).save(c.getKey(), c.getInt_value());
                        // LogUtils.d("SV_TYPE_SAVE_GPOS_CURSOR_INT 222");
                    }
                        break;
                    case TYPE_SAVE_PROGRAM_CURSOR: {
                        // LogUtils.d("SV_TYPE_SAVE_PROGRAM_CURSOR 111");
                        ProgramInfo programInfo = (ProgramInfo) msg.obj;
                        if (Pvcfg.isPrimeTvInputEnable())
                            TIFChannelData.UpdateOrInsertChannels(mContext, programInfo, Pvcfg.getTvInputId(),
                                    Pvcfg.getTunerType());
                        new ProgramDatabaseTable(mContext).save(programInfo);
                        // LogUtils.d("SV_TYPE_SAVE_PROGRAM_CURSOR 2222");
                    }
                        break;
                    case TYPE_DEL_PROGRAM_CURSOR: {
                        // LogUtils.d("SV_TYPE_DEL_PROGRAM_CURSOR 111");
                        ProgramInfo programInfo = (ProgramInfo) msg.obj;
                        if (Pvcfg.isPrimeTvInputEnable())
                            TIFChannelData.deleteTIFChannelDataByChannelUri(mContext,
                                    programInfo.getTvInputChannelUri());
                        new ProgramDatabaseTable(mContext).remove(programInfo);
                        // LogUtils.d("SV_TYPE_DEL_PROGRAM_CURSOR 222");
                    }
                        break;
                    case TYPE_SAVE_FAVINFO_CURSOR: {
                        // LogUtils.d("SV_SAVE_FAVINFO_CURSOR 111");
                        new FavInfoDatabaseTable(mContext).save((FavInfo) msg.obj);
                        // LogUtils.d("SV_SAVE_FAVINFO_CURSOR 222");
                    }
                        break;
                    case TYPE_DEL_FAVINFO_CURSOR: {
                        // LogUtils.d("SV_DEL_FAVINFO_CURSOR 111");
                        new FavInfoDatabaseTable(mContext).remove((FavInfo) msg.obj);
                        // LogUtils.d("SV_DEL_FAVINFO_CURSOR 222");
                    }
                        break;
                    case TYPE_SAVE_BOOKINFO_CURSOR: {
                        // LogUtils.d("SV_SAVE_BOOKINFO_CURSOR 111");
                        new BookInfoDatabaseTable(mContext).save((BookInfo) msg.obj);
                        // LogUtils.d("SV_SAVE_BOOKINFO_CURSOR 222");
                    }
                        break;
                    case TYPE_DEL_BOOKINFO_CURSOR: {
                        // LogUtils.d("SV_DEL_BOOKINFO_CURSOR 111");
                        new BookInfoDatabaseTable(mContext).remove((BookInfo) msg.obj);
                        // LogUtils.d("SV_DEL_BOOKINFO_CURSOR 222");
                    }
                        break;
                    case TYPE_SAVE_TPINFO_CURSOR: {
                        // LogUtils.d("gggg SV_SAVE_TPINFO_CURSOR 111");
                        new TpInfoDatabaseTable(mContext).save((TpInfo) msg.obj);
                        // LogUtils.d("gggg SV_SAVE_TPINFO_CURSOR 222");
                    }
                        break;
                    case TYPE_BACKUP_DB: {
                        LogUtils.d("backup db");
                        DVBDatabase.backupDatabase((boolean) msg.obj);
                    }
                        break;
                    case TYPE_SAVE_MAILDATA_CURSOR: {
                        LogUtils.d("save maildata");
                        new MailDatabaseTable(mContext).save((MailData) msg.obj);
                    }
                        break;
                    case TYPE_DEL_MAILDATA_CURSOR: {
                        LogUtils.d("delete maildata");
                        int mail_id = msg.arg1;
                        new MailDatabaseTable(mContext).remove(mail_id);
                    }
                        break;
                    case TYPE_SAVE_CNSMAILDATA_CURSOR: {//eric lin 20260106 CNS mail
                        LogUtils.d("save CNSmaildata");
                        new CNSMailDatabaseTable(mContext).save((CNSMailData) msg.obj);
                    }
                    break;
                    case TYPE_DEL_CNSMAILDATA_CURSOR: {//eric lin 20260106 CNS mail
                        LogUtils.d("delete CNSmaildata");
                        int mail_id = msg.arg1;
                        new CNSMailDatabaseTable(mContext).remove(mail_id);
                    }
                    break;
                    case TYPE_SAVE_CNSEASDATA_CURSOR: {
                        LogUtils.d("save CNSeasdata");
                        new CNSEasDatabaseTable(mContext).save((CNSEasData) msg.obj);
                    }
                    break;
                    case TYPE_DEL_CNSEASDATA_CURSOR: {
                        LogUtils.d("delete CNSeasdata");
                        int eas_id = msg.arg1;
                        new CNSEasDatabaseTable(mContext).remove(eas_id);
                    }
                    break;
                }

            }
        };
        mContext = context;

        // only for test
        // DataManagerCleanAllDataBase();
        //
        // try {
        // sleep(2000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        DefaultValue defaultValue = new DefaultValue(mContext, Pvcfg.getTunerType());
        GposInfo gposInfo = new GposDatabaseTable(mContext).load();
        if (GposInfo.getDBVersion(mContext).equals("null")) {
            // mSatInfoList = defaultValue.satInfoList;
            // mTpInfoList = defaultValue.tpInfoList;
            mGposInfo = defaultValue.gposInfo;
            // for(FavGroupName tmp : defaultValue.favGroupNameList) {
            // mFavGroupList.add(new FavGroup(tmp));
        } else {
            mGposInfo = gposInfo;
        }

        Pvcfg.setBatId(GposInfo.getBatId(mContext));

        mSatInfoList = new SatInfoDatabaseTable(mContext).load();
        // if(mSatInfoList.equals(null)) {
        if (mSatInfoList == null || mSatInfoList.isEmpty()) {
            mSatInfoList = defaultValue.satInfoList;
            Log.d(TAG, "mSatInfoList is null or empty");
        } else {
            Log.d(TAG, "mSatInfoList is not null or empty");
        }

        mTpInfoList = new TpInfoDatabaseTable(mContext).load();
        // if(mSatInfoList.equals(null)) {
        if (mTpInfoList == null || mTpInfoList.isEmpty()) {
            mTpInfoList = defaultValue.tpInfoList;
            Log.d(TAG, "mTpInfoList is null or empty");
        } else {
            Log.d(TAG, "mTpInfoList is not null or empty");
        }

        mProgramInfoList = new ProgramDatabaseTable(mContext).load();
        // if(mSatInfoList.equals(null)) {
        if (mProgramInfoList == null || mProgramInfoList.isEmpty()) {
            Log.d(TAG, "mProgramInfoList is null or empty");
        } else {
            Log.d(TAG, "mProgramInfoList is not null or empty");
        }

        Log.d(TAG, "BookInfoDatabaseTable start");
        mBookInfoList = new BookInfoDatabaseTable(mContext).load();
        Log.d(TAG, "BookInfoDatabaseTable end");
        // if(mSatInfoList.equals(null)) {
        if (mBookInfoList == null || mBookInfoList.isEmpty()) {
            Log.d(TAG, "mBookInfoList is null or empty");
        } else {
            Log.d(TAG, "mBookInfoList is not null or empty");
        }

        rebuildFavGroup(defaultValue);

        if (mProgramDatabase == null)
            mProgramDatabase = new ProgramDatabase();
        if (mNetProgramDatabase == null)
            mNetProgramDatabase = new NetProgramDatabase();
        mHidlBufferList.clear();
        mCNSSITableData = new CNSSITableData();
    }

    public String getPvrRecPath() {
        return mPvrRecPath;
    }

    public void setPvrRecPath(String pvrRecPath) {
        mPvrRecPath = pvrRecPath;
    }

    public List<SatInfo> getSatInfoList() {
        return mSatInfoList;
    }

    public SatInfo getSatInfo(int satId) {
        for (SatInfo tmp : mSatInfoList) {
            if (tmp.getSatId() == satId)
                return tmp;
        }
        return null;
    }

    public void updateSatInfo(SatInfo updateSatInfo) {
        int updateSatID = updateSatInfo.getSatId();
        for (SatInfo satInfo : mSatInfoList) {
            if (satInfo.getSatId() == updateSatID) {
                satInfo.update(updateSatInfo);
                break;
            }
        }
    }

    public void updateSatInfoList(List<SatInfo> satInfoList) {
        if (satInfoList == null) {
            Log.e(TAG, "updateSatInfoList: null satInfoList");
            return;
        }

        if (satInfoList.size() >= SatInfo.MAX_NUM_OF_SAT) {
            Log.e(TAG, "updateSatInfoList: satInfoList size >= MAX_NUM_OF_SAT");
            return;
        }

        // clear
        mSatInfoList.clear();

        // re add and set tp id list in sat
        mSatInfoList.addAll(satInfoList);
        for (SatInfo satInfo : mSatInfoList) {
            List<Integer> tpIDList = getTpIDListBySatID(satInfo.getSatId());
            satInfo.setTps(tpIDList);
            satInfo.setTpNum(tpIDList.size());
        }
    }

    // return sat id
    public int addSatInfo(SatInfo satInfo) {
        if (satInfo == null) {
            return -1;
        }

        if (mSatInfoList.size() >= SatInfo.MAX_NUM_OF_SAT) {
            Log.e(TAG, "addSatInfo: reach MAX_NUM_OF_SAT");
            return -1;
        }

        // use last sat id + 1 for new sat id
        // TODO: find better way to get sat id
        int satID = mSatInfoList.isEmpty() ? 0 : mSatInfoList.get(mSatInfoList.size() - 1).getSatId() + 1;
        satInfo.setSatId(satID);

        // set tp id list in sat
        List<Integer> tpIDList = getTpIDListBySatID(satID);
        satInfo.setTps(tpIDList);
        satInfo.setTpNum(tpIDList.size());

        mSatInfoList.add(satInfo);
        return satID;
    }

    private List<Integer> getTpIDListBySatID(int satID) {
        List<TpInfo> tpInfoListBySatID = getTpInfoListBySatID(satID);
        List<Integer> tpIDList = new ArrayList<>();
        for (TpInfo tpInfo : tpInfoListBySatID) {
            tpIDList.add(tpInfo.getTpId());
        }

        return tpIDList;
    }

    public void delSatInfo(int satID) {
        for (SatInfo satInfo : mSatInfoList) {
            if (satInfo.getSatId() == satID) {
                // remove sat
                mSatInfoList.remove(satInfo);
                // remove all tps that belong to this sat
                delAllTpBySatID(satID);
                break;
            }
        }
    }

    private void delAllTpBySatID(int satID) {
        // use iterator to remove during iteration
        Iterator<TpInfo> iterator = mTpInfoList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getSatId() == satID) {
                iterator.remove();
            }
        }
    }

    public List<TpInfo> getTpInfoList() {
        return mTpInfoList;
    }

    public List<TpInfo> getTpInfoListBySatID(int satID) {
        List<TpInfo> tpInfoList = new ArrayList<>();
        for (TpInfo tpInfo : mTpInfoList) {
            if (tpInfo.getSatId() == satID) {
                tpInfoList.add(tpInfo);
            }
        }

        return tpInfoList;
    }

    public TpInfo getTpInfo(int tpId) {
        for (TpInfo tmp : mTpInfoList) {
            if (tmp.getTpId() == tpId)
                return tmp;
        }
        return null;
    }

    public TpInfo getTpInfobyFreq(int freq) {
        for (TpInfo tmp : mTpInfoList) {
            if (tmp.CableTp.getFreq() == freq)
                return tmp;
        }
        return null;
    }

    public void updateTpInfo(TpInfo updateTpInfo) {
        int updateTpID = updateTpInfo.getTpId();
        for (TpInfo tpInfo : mTpInfoList) {
            if (tpInfo.getTpId() == updateTpID) {
                tpInfo.update(updateTpInfo);
                break;
            }
        }
    }

    // clear and re add
    public void updateTpInfoList(List<TpInfo> tpInfoList) {
        if (tpInfoList == null) {
            Log.e(TAG, "updateTpInfoList: null tpInfoList");
            return;
        }

        // clear tp list and tp id list in sat
        mTpInfoList.clear();
        for (SatInfo satInfo : mSatInfoList) {
            satInfo.setTps(new ArrayList<>());
            satInfo.setTpNum(0);
        }

        // re add and set tp id list in sat
        mTpInfoList.addAll(tpInfoList);
        for (SatInfo satInfo : mSatInfoList) {
            List<Integer> tpIDList = getTpIDListBySatID(satInfo.getSatId());
            satInfo.setTps(tpIDList);
            satInfo.setTpNum(tpIDList.size());
        }
    }

    public void delTpInfo(int tpID) {
        for (TpInfo tpInfo : mTpInfoList) {
            if (tpInfo.getTpId() == tpID) {
                delTpIDInSat(tpID, tpInfo.getSatId());
                mTpInfoList.remove(tpInfo);
                delProgramInfoByTpId(tpID);
                break;
            }
        }
    }

    private void delTpIDInSat(int tpID, int satID) {
        SatInfo satInfo = getSatInfo(satID);

        if (satInfo == null) {
            Log.e(TAG, "delTpIDInSat: can't find sat, sat id = " + satID);
            return;
        }

        List<Integer> tpsList = satInfo.getTps();
        tpsList.remove(Integer.valueOf(tpID));
        satInfo.setTps(tpsList);
        satInfo.setTpNum(tpsList.size());
    }

    public int find_empty_tp_id() {
        // return mTpInfoList.isEmpty() ? 0 :
        // mTpInfoList.get(mTpInfoList.size()-1).getTpId() + 1;
        int tpId = -1;
        boolean find_tp_id = false;
        Log.d(TAG, "find_empty_tp_id mTpInfoList.size() = " + mTpInfoList.size());
        int n = mTpInfoList.size();
        int[] arr = mTpInfoList.stream().mapToInt(TpInfo::getTpId).toArray();

        // 原地调整数字到对应的位置
        for (int i = 0; i < n; i++) {
            while (arr[i] > 0 && arr[i] <= n && arr[arr[i] - 1] != arr[i]) {
                // 交换数字到正确的位置
                int temp = arr[arr[i] - 1];
                arr[arr[i] - 1] = arr[i];
                arr[i] = temp;
            }
        }

        // 找到第一个数字不匹配的位置
        for (int i = 0; i < n; i++) {
            if (arr[i] != i + 1) {
                tpId = i + 1;
                find_tp_id = true;
                break;
                // return i + 1;
            }
        }

        // 如果所有数字都匹配，则返回 n + 1
        if (!find_tp_id)
            tpId = n + 1;
        Log.d(TAG, "find_empty_tp_id tpId = " + tpId);
        return tpId;
    }

    // return tp id
    public int addTpInfo(TpInfo tpInfo) {
        if (tpInfo == null) {
            return -1;
        }

        // use last tp id + 1 for new tp id
        // TODO: find better way to get tp id
        int tpID = find_empty_tp_id();
        tpInfo.setTpId(tpID);
        mTpInfoList.add(tpInfo);

        addTpIDToSat(tpID, tpInfo.getSatId());
        return tpID;
    }

    private void addTpIDToSat(int tpID, int satID) {
        SatInfo satInfo = getSatInfo(satID);

        if (satInfo == null) {
            Log.e(TAG, "addTpToSat: can't find sat, sat id = " + satID);
            return;
        }

        List<Integer> tpsList = satInfo.getTps();
        tpsList.add(tpID);
        satInfo.setTps(tpsList);
        satInfo.setTpNum(tpsList.size());
        updateSatInfo(satInfo);
    }

    public List<ProgramInfo> getProgramInfoList() {
        // synchronized(mProgramListLock) {
        return mProgramInfoList;
        // }
    }

    // TO UI
    public List<ProgramInfo> getProgramInfoList(int type, int pos, int num) {
        // synchronized(mProgramListLock) {
        if ((type < FavGroup.ALL_TV_TYPE || type >= FavGroup.ALL_TV_RADIO_TYPE_MAX)
                || (pos < 0 && pos != MiscDefine.ProgramInfo.POS_ALL)
                || (num < 0 && num != MiscDefine.ProgramInfo.NUM_ALL)
                || (pos == MiscDefine.ProgramInfo.POS_ALL && num != MiscDefine.ProgramInfo.NUM_ALL)
                || (num == MiscDefine.ProgramInfo.NUM_ALL && pos != MiscDefine.ProgramInfo.POS_ALL)) {
            Log.e(TAG, "getProgramInfoList: invalid input param");
            return new ArrayList<>();
        }

        List<ProgramInfo> foundListByType = new ArrayList<>();
        synchronized (mFavGroupListLock) {
            FavGroup favGroup = mFavGroupList.get(type);
            LogUtils.d("fav info[" + type + "] num = " + favGroup.getFavInfoList().size());
            for (FavInfo favInfo : favGroup.getFavInfoList()) {
                ProgramInfo programInfo = getProgramInfo(favInfo.getChannelId());
                if (programInfo != null) {
                    foundListByType.add(programInfo);
                }
            }
        }
        List<ProgramInfo> result;
        int size = foundListByType.size();
        // return all found program if pos = POS_ALL and num = NUM_ALL
        // In fact, if pos == MiscDefine.ProgramInfo.POS_ALL,
        // condition 'num == MiscDefine.ProgramInfo.NUM_ALL' is always 'true' here
        if (pos == MiscDefine.ProgramInfo.POS_ALL && num == MiscDefine.ProgramInfo.NUM_ALL) {
            result = new ArrayList<>(foundListByType);
        }
        // return empty list if no program found or pos is out of bond
        else if (foundListByType.isEmpty() || pos >= size) {
            result = new ArrayList<>();
        }
        // return #num program from pos
        else {
            int toIndex = Math.min(pos + num, size); // avoid toIndex out of bond
            result = new ArrayList<>(foundListByType.subList(pos, toIndex));
        }

        return result;
        // }
    }

    public ProgramInfo getProgramInfo(long channelId) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222");
            // synchronized (mProgramListLock) {
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                ProgramInfo tmp = iterator.next();
                if (tmp.getChannelId() == channelId)
                    return new ProgramInfo(tmp);
            }
            // for (ProgramInfo tmp : mProgramInfoList) {
            // if (tmp.getChannelId() == channelId)
            // return new ProgramInfo(tmp);
            // }
            return null;
            // }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("33333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("44444444444444");
        }
    }

    public ProgramInfo getProgramInfo(int service_id) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("2222222222222");
            // synchronized(mProgramListLock) {
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                ProgramInfo tmp = iterator.next();
                if (tmp.getServiceId() == service_id)
                    return new ProgramInfo(tmp);
            }
            // for (ProgramInfo tmp : mProgramInfoList) {
            // if (tmp.getServiceId() == service_id)
            // return new ProgramInfo(tmp);
            // }
            return null;
            // }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444");
        }
    }

    public ProgramInfo getProgramInfo(int service_id, int transportstream_id) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("2222222222222");
            // synchronized (mProgramListLock) {
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                ProgramInfo tmp = iterator.next();
                if (tmp.getServiceId() == service_id &&
                        tmp.getTransportStreamId() == transportstream_id)
                    return new ProgramInfo(tmp);
            }
            // for (ProgramInfo tmp : mProgramInfoList) {
            // if (tmp.getServiceId() == service_id &&
            // tmp.getTransportStreamId() == transportstream_id)
            // return new ProgramInfo(tmp);
            // }
            return null;
            // }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444");
        }
    }

    public ProgramInfo getProgramInfo(int service_id, int transportstream_id, int original_network_id) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            // LogUtils.d("primetif input: sid=" +service_id +"tsid=" +transportstream_id +"
            // onid=" + original_network_id);
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("2222222222222");
            // synchronized (mProgramListLock) {
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                ProgramInfo tmp = iterator.next();
                /*
                 * LogUtils.d("primetif check ProgramInfo: sid=" + tmp.getServiceId() +
                 * " tsid=" + tmp.getTransportStreamId() +
                 * " onid=" + tmp.getOriginalNetworkId()
                 * );
                 */
                if (tmp.getServiceId() == service_id &&
                        tmp.getTransportStreamId() == transportstream_id &&
                        tmp.getOriginalNetworkId() == original_network_id)
                    return new ProgramInfo(tmp);
            }
            return null;
            // }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("33333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("4444444444444");
        }
    }

    public ProgramInfo getProgramInfo(List<ProgramInfo> programInfoList, int service_id, int transportstream_id,
            int original_network_id) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("2222222222222");
            for (ProgramInfo tmp : programInfoList) {
                if (tmp.getServiceId() == service_id &&
                        (transportstream_id == -1 || tmp.getTransportStreamId() == transportstream_id) &&
                        (original_network_id == -1 || tmp.getOriginalNetworkId() == original_network_id)) {
                    return tmp;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444444");
        }
    }

    public void resetLast_LCN(List<ProgramInfo> programInfoList) {
        try {
            LAST_LCN = 0;
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222");
            // synchronized (mProgramListLock) {
            List<ProgramInfo> temp = programInfoList;
            if (temp == null)
                temp = mProgramInfoList;
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).getLCN() > LAST_LCN)
                    LAST_LCN = temp.get(i).getLCN();
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444444");
        }
    }

    public void SetNewLcnIfNoLcn(List<ProgramInfo> programInfoList) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222");
            // synchronized (mProgramListLock) {
            List<ProgramInfo> temp = programInfoList;
            if (temp == null)
                temp = mProgramInfoList;
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).getLCN() == 0) {
                    if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS)) {
                        temp.get(i).setLCN(temp.get(i).getServiceId());
                        temp.get(i).setDisplayNum(temp.get(i).getServiceId());
                    }
                    else {
                        LAST_LCN += 1;
                        temp.get(i).setLCN(LAST_LCN);
                        temp.get(i).setDisplayNum(LAST_LCN);
                    }
                }
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444444");
        }
    }

    public void addProgramInfo(ProgramInfo programInfo) {
        ProgramInfo foundProgramInfo = getProgramInfo(programInfo.getChannelId());
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("2222222222222");
            // not found in program list, add
            if (foundProgramInfo == null) {
                mProgramInfoList.add(programInfo);
            }
            // found in program list, update
            else {
                for (ProgramInfo p : mProgramInfoList) {
                    if (p.getChannelId() == programInfo.getChannelId()) {
                        programInfo.setTvInputChannelUri(programInfo.getTvInputChannelUri());
                        if(programInfo.getLCN() == 0) {
                            programInfo.setLCN(foundProgramInfo.getLCN());
                            programInfo.setDisplayNum(foundProgramInfo.getDisplayNum());
                        }
                        p.update(programInfo);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("44444444444444");
        }
    }

    public void addProgramInfoList(List<ProgramInfo> programInfoList) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("222222222222222");
            // synchronized (mProgramListLock) {
            // clear pre scan channel -s
            mProgramInfoList.clear();
            mProgramInfoList.addAll(programInfoList);
            // clear pre scan channel -e

            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("44444444444444");
        }
    }

    public void updateProgramInfoListToTIF(int tunerType) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222");

            Context context = ServiceInterface.getContext();
            String inputId = mDataManager.GetTvInputId();

            // 1. Collect keys from mProgramInfoList
            Set<String> newChannelKeys = new HashSet<>();
            for (ProgramInfo info : mProgramInfoList) {
                String key = info.getOriginalNetworkId() + "-" + info.getTransportStreamId() + "-"
                        + info.getServiceId();
                newChannelKeys.add(key);
            }

            // 2. Query TIF channels
            Uri channelsUri = TvContractCompat.buildChannelsUriForInput(inputId);
            String[] projection = {
                    Channels._ID,
                    Channels.COLUMN_ORIGINAL_NETWORK_ID,
                    Channels.COLUMN_TRANSPORT_STREAM_ID,
                    Channels.COLUMN_SERVICE_ID
            };

            try (Cursor cursor = context.getContentResolver().query(channelsUri, projection, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(Channels._ID));
                        int onId = cursor.getInt(cursor.getColumnIndex(Channels.COLUMN_ORIGINAL_NETWORK_ID));
                        int tsId = cursor.getInt(cursor.getColumnIndex(Channels.COLUMN_TRANSPORT_STREAM_ID));
                        int serviceId = cursor.getInt(cursor.getColumnIndex(Channels.COLUMN_SERVICE_ID));

                        String key = onId + "-" + tsId + "-" + serviceId;

                        if (!newChannelKeys.contains(key)) {
                            // Delete this channel
                            Uri deleteUri = TvContractCompat.buildChannelUri(id);
                            context.getContentResolver().delete(deleteUri, null, null);
                            LogUtils.d("Deleted channel from TIF: " + key + " id=" + id);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtils.e("Error syncing channels with TIF: " + e.getMessage());
                e.printStackTrace();
            }

            TIFChannelData.insertChannelList(ServiceInterface.getContext(), mProgramInfoList,
                    mDataManager.GetTvInputId(), tunerType);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444444");
        }
    }

    public void delProgramInfoByTpId(int tpId) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("2222222222");
            // synchronized (mProgramListLock) {
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                ProgramInfo programInfo = iterator.next();
                if (programInfo.getTpId() == tpId) {
                    iterator.remove();
                    break;
                }
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("44444444444444");
        }
    }

    public void delProgramInfo(long channelId) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("2222222222");
            // synchronized (mProgramListLock) {
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                ProgramInfo programInfo = iterator.next();
                if (programInfo.getChannelId() == channelId) {
                    iterator.remove();
                    break;
                }
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("44444444444444");
        }
    }

    public void delProgramInfoList(List<ProgramInfo> programInfoList) {
        for (ProgramInfo programInfo : programInfoList) {
            delProgramInfo(programInfo.getChannelId());
        }
    }

    public void updateProgramInfo(ProgramInfo updateProgramInfo) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222");
            // synchronized (mProgramListLock) {
            long updateChannelID = updateProgramInfo.getChannelId();
            for (ProgramInfo programInfo : mProgramInfoList) {
                if (programInfo.getChannelId() == updateChannelID) {
                    programInfo.update(updateProgramInfo);
                    break;
                }
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444444");
        }
    }

    /**
     * Update programs by given simpleChannelList and type. Programs not exist
     * in simpleChannelList will be deleted. Won't add new program because we can't
     * construct program from simple channel.
     * 
     * @param simpleChannelList given simple channels for updating
     * @param type              the program type to update
     */
    public void updateProgramInfoList(List<SimpleChannel> simpleChannelList, int type) {
        // 1. update and del programs by input simpleChannelList
        // use iterator to remove during iteration
        Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
        while (iterator.hasNext()) {
            ProgramInfo programInfo = iterator.next();

            // ignore program with different type
            if (programInfo.getType() != type) {
                continue;
            }

            boolean updated = false;
            for (SimpleChannel simpleChannel : simpleChannelList) {
                if (programInfo.getChannelId() == simpleChannel.get_channel_id()) {
                    programInfo.setDisplayName(simpleChannel.get_channel_name());
                    if (Pvcfg.channelNumberUseLcn()) { // displayNum or LCN by config
                        programInfo.setLCN(simpleChannel.get_channel_num());
                    } else {
                        programInfo.setDisplayNum(simpleChannel.get_channel_num());
                    }
                    programInfo.setLock(simpleChannel.get_user_lock());
                    programInfo.setSkip(simpleChannel.get_channel_skip());
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                // this programInfo does not exist in simpleChannelList
                // delete it
                iterator.remove();
            }
        }

        // 2. re order the programs by input simpleChannelList
        // del and re add program to meet the order
        List<ProgramInfo> newProgramInfoList = new ArrayList<>();
        for (SimpleChannel simpleChannel : simpleChannelList) {
            ProgramInfo programInfo = getProgramInfo(simpleChannel.get_channel_id());
            if (programInfo != null) {
                newProgramInfoList.add(getProgramInfo(simpleChannel.get_channel_id()));
            }
        }

        delAllProgramInfo(type);
        mProgramInfoList.addAll(newProgramInfoList);
    }

    public void delAllProgramInfo() {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222222");
            // synchronized (mProgramListLock) {
            mProgramInfoList.clear();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444444");
        }
    }

    public void delAllProgramInfo(int tvOrRadioType) {
        if (tvOrRadioType != FavGroup.ALL_TV_TYPE && tvOrRadioType != FavGroup.ALL_RADIO_TYPE) {
            return;
        }
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222");
            // synchronized (mProgramListLock) {
            // use iterator to remove during iteration
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getType() == tvOrRadioType) {
                    iterator.remove();
                }
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("44444444444444");
        }
    }

    public GposInfo getGposInfo() {
        return mGposInfo;
    }

    public void updateGposInfo(GposInfo gposInfo) {
        mGposInfo = gposInfo;
    }

    public FavInfo getFavInfo(int groupIndex, int favIndex) {
        synchronized (mFavGroupListLock) {
            return mFavGroupList.get(groupIndex).getFavInfo(favIndex);
        }
    }

    public void setFavGroupList(List<FavGroup> favGroupList) {
        synchronized (mFavGroupListLock) {
            mFavGroupList = favGroupList;
        }
    }

    public int getFavGroupUsed(int groupIndex) {
        synchronized (mFavGroupListLock) {
            return mFavGroupList.get(groupIndex).getFavGroupUsed();
        }
    }

    public FavGroupName getFavGroupName(int groupIndex) {
        synchronized (mFavGroupListLock) {
            return mFavGroupList.get(groupIndex).getFavGroupName();
        }
    }

    public List<FavGroup> getFavGroupList() {
        synchronized (mFavGroupListLock) {
            return mFavGroupList;
        }
    }

    public List<FavGroup> getMusicFavGroupList() {
        synchronized (mFavGroupListLock) {
            List<FavGroup> tmp = new ArrayList<>();
            tmp.add(mFavGroupList.get(FavGroup.GROUP_MANDARIN));
            tmp.add(mFavGroupList.get(FavGroup.GROUP_WESTERN));
            tmp.add(mFavGroupList.get(FavGroup.GROUP_JPOP));
            tmp.add(mFavGroupList.get(FavGroup.GROUP_LOUNGE));
            tmp.add(mFavGroupList.get(FavGroup.GROUP_CLASSICAL));
            tmp.add(mFavGroupList.get(FavGroup.GROUP_ELSE));
            return tmp;
        }
    }

    public List<FavInfo> getFavInfoList(int groupIndex) {
        synchronized (mFavGroupListLock) {
            if (groupIndex >= mFavGroupList.size())
                return new ArrayList<>();
            return new ArrayList<>(mFavGroupList.get(groupIndex).getFavInfoList());
        }
    }

    public void setFavGroupName(int groupIndex, String name) {
        synchronized (mFavGroupListLock) {
            mFavGroupList.get(groupIndex).setFavGroupName(name);
        }
    }

    public void addFavInfo(int groupIndex, FavInfo favInfo) {
        synchronized (mFavGroupListLock) {
            mFavGroupList.get(groupIndex).addFavInfo(favInfo);
        }
    }

    public void addFavInfoList(int groupIndex, List<FavInfo> favInfoList) {
        synchronized (mFavGroupListLock) {
            mFavGroupList.get(groupIndex).addFavInfoList(favInfoList);
        }
    }

    public void delFavInfo(int groupIndex, int favNum) {
        synchronized (mFavGroupListLock) {
            mFavGroupList.get(groupIndex).delFavInfo(favNum);
        }
    }

    public void delFavInfo(int groupIndex, FavInfo favInfo) {
        synchronized (mFavGroupListLock) {
            mFavGroupList.get(groupIndex).delFavInfo(favInfo);
        }
    }

    public void updateFavInfo(int groupIndex, FavInfo favInfo) {
        synchronized (mFavGroupListLock) {
            mFavGroupList.get(groupIndex).updateFavInfo(favInfo);
        }
    }

    public void printFavGroup(String tag) {
        for (FavGroup favGroup : mFavGroupList) {
            LogUtils.d(tag + " FavGroupName = " + favGroup.getFavGroupName().getGroupName());
            for (FavInfo favInfo : favGroup.getFavInfoList()) {
                LogUtils.d(tag + " FavNum = " + favInfo.getFavNum() + " ChannelID = " + favInfo.getChannelId());
            }
        }
    }

    private void ensureSizeByIndex(List<?> list, int needSize) {
        while (list.size() < needSize) {
            list.add(null);
        }
    }

    public void rebuildFavGroup(DefaultValue defaultValue) {
        synchronized (mFavGroupListLock) {

            List<FavGroupName> dbGroupList = new FavGroupNameDatabaseTable(mContext).load();

            mFavGroupList.clear();

            if (dbGroupList == null || dbGroupList.isEmpty()) {
                if (defaultValue != null && defaultValue.favGroupNameList != null) {
                    for (FavGroupName def : defaultValue.favGroupNameList) {
                        if (def == null)
                            continue;
                        int idx = def.getGroupType();
                        if (idx < 0)
                            continue;
                        ensureSizeByIndex(mFavGroupList, idx + 1);
                        mFavGroupList.set(idx, new FavGroup(def));
                    }
                }
            } else {
                if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                        || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                        || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS)) {
                    if (defaultValue != null && defaultValue.favGroupNameList != null) {
                        for (FavGroupName def : defaultValue.favGroupNameList) {
                            if (def == null)
                                continue;
                            int idx = def.getGroupType();
                            if (idx < 0)
                                continue;
                            ensureSizeByIndex(mFavGroupList, idx + 1);
                            mFavGroupList.set(idx, new FavGroup(def));
                        }
                    }

                    for (FavGroupName dbName : dbGroupList) {
                        if (dbName == null)
                            continue;
                        int idx = dbName.getGroupType();
                        if (idx < 0)
                            continue;
                        ensureSizeByIndex(mFavGroupList, idx + 1);
                        if (mFavGroupList.get(idx) == null) {
                            mFavGroupList.set(idx, new FavGroup(dbName));
                        }
                    }

                } else {
                    for (FavGroupName dbName : dbGroupList) {
                        if (dbName == null)
                            continue;
                        int idx = dbName.getGroupType();
                        if (idx < 0)
                            continue;
                        ensureSizeByIndex(mFavGroupList, idx + 1);
                        mFavGroupList.set(idx, new FavGroup(dbName));
                    }
                }
                for (FavGroup tmp : mFavGroupList) {
                    List<FavInfo> favInfoList = new FavInfoDatabaseTable(mContext)
                            .loadByGroupType(tmp.getFavGroupName().getGroupType());
                    if (favInfoList != null)
                        mFavGroupList.get(tmp.getFavGroupName().getGroupType()).addFavInfoList(favInfoList);
                }
            }
        }
    }

    public void delAllFavInfo(int groupIndex) {
        synchronized (mFavGroupListLock) {
            mFavGroupList.get(groupIndex).delAllFavInfo();
        }
    }

    public void clearFavGroupList() {
        synchronized (mFavGroupListLock) {
            mFavGroupList.clear();
        }
    }

    private int add_to_fav(List<FavGroup> favGroupList, ProgramInfo programInfo, int type) {
        synchronized (mFavGroupListLock) {
            List<FavInfo> favInfoList = favGroupList.get(type).getFavInfoList();
            for (FavInfo fav : favInfoList) {
                if (fav.getChannelId() == programInfo.getChannelId())
                    return 0;
            }
            Log.d(TAG, "service id = " + programInfo.getServiceId() + "type = " + type);
            favInfoList.add(new FavInfo(programInfo.getLCN(), programInfo.getChannelId(), type));
            Log.d(TAG, "add_to_fav[" + type + "] " + "lcn[" + programInfo.getLCN() + "] " + programInfo.getDisplayName()
                    + " adult = " + programInfo.getAdultFlag());
            return 1;
        }
    }

    public void category_add_to_fav_default(List<ProgramInfo> ProgramInfoList, List<FavGroup> favGroupList) {
        int tv = 0, radio = 0;
        for (ProgramInfo programInfo : ProgramInfoList) {
            if (programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                tv += add_to_fav(favGroupList, programInfo, FavGroup.ALL_TV_TYPE);
            } else if (programInfo.getType() == ProgramInfo.PROGRAM_RADIO) {
                radio += add_to_fav(favGroupList, programInfo, FavGroup.ALL_RADIO_TYPE);
            }
        }
        LogUtils.d("TV = " + tv);
        LogUtils.d("Radio = " + radio);
    }

    public void category_add_to_fav_sund(List<ProgramInfo> ProgramInfoList, List<FavGroup> favGroupList) {
        int kid = 0, education = 0, news = 0, movies = 0, variety = 0, music = 0, adult = 0, sports = 0, hd = 0, tv = 0,
                radio = 0, region = 0, shopping = 0, uhd = 0;
        for (ProgramInfo programInfo : ProgramInfoList) {
            if (programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                tv += add_to_fav(favGroupList, programInfo, FavGroup.ALL_TV_TYPE);
            } else if (programInfo.getType() == ProgramInfo.PROGRAM_RADIO) {
                radio += add_to_fav(favGroupList, programInfo, FavGroup.ALL_RADIO_TYPE);
            }
            if ((programInfo.getCategory_type() & 0x0001) == 1) {// Kids
                kid += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_KIDS);
            }
            if ((((programInfo.getCategory_type() & 0x0002) >> 1) == 1)) {// education
                education += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_EDUCATION);
            }
            if ((((programInfo.getCategory_type() & 0x0004) >> 2) == 1)) {// news
                news += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_NEWS);
            }
            if ((((programInfo.getCategory_type() & 0x0008) >> 3) == 1)) {// movies
                movies += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_MOVIES);
            }
            if ((((programInfo.getCategory_type() & 0x0010) >> 4) == 1)) {// variety
                variety += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_VARIETY);
            }
            if ((((programInfo.getCategory_type() & 0x0020) >> 5) == 1)) {// music
                music += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_MUSIC);
            }
            if ((((programInfo.getCategory_type() & 0x0040) >> 6) == 1)) {// adult
                adult += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_ADULT);
            }
            if ((((programInfo.getCategory_type() & 0x0080) >> 7) == 1)) {// sports
                sports += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_SPORTS);
            }
            if ((((programInfo.getCategory_type() & 0x0100) >> 8) == 1)) {// hd
                hd += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_HD);
            }
            if ((((programInfo.getCategory_type() & 0x0200) >> 9) == 1)) {// Religion
                region += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_RELIGION);
            }
            if ((((programInfo.getCategory_type() & 0x80000) >> 19) == 1)) {// shopping
                shopping += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_SHOPPING);
            }
            if ((((programInfo.getCategory_type() & 0x0800) >> 11) == 1)) {// uhd
                uhd += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_UHD);
            }
        }
        LogUtils.d("TV = " + tv);
        LogUtils.d("Radio = " + radio);
        LogUtils.d("Kid = " + kid);
        LogUtils.d("Education = " + education);
        LogUtils.d("NEWS = " + news);
        LogUtils.d("Movies = " + movies);
        LogUtils.d("Variety = " + variety);
        LogUtils.d("Music = " + music);
        LogUtils.d("Adult = " + adult);
        LogUtils.d("Sports = " + sports);
        LogUtils.d("HD = " + hd);
        LogUtils.d("Region = " + region);
        LogUtils.d("Shopping = " + shopping);
        LogUtils.d("UHD = " + uhd);
    }

    // public void category_add_to_fav(List<ProgramInfo> ProgramInfoList) {
    //
    // }

    public void category_add_to_fav_dmg(List<ProgramInfo> ProgramInfoList, List<FavGroup> favGroupList) {
        int kid = 0, education = 0, news = 0, movies = 0, variety = 0, music = 0, adult = 0, sports = 0, hd = 0, tv = 0,
                radio = 0, region = 0, shopping = 0, uhd = 0;
        if (favGroupList.size() != FavGroup.ALL_TV_RADIO_TYPE_MAX) {
            LogUtils.d("FAV Group number is not Match !!!!!!!! favGroupList.size() = " + favGroupList.size());
            LogUtils.d("Rebuild FAV Group");
            DefaultValue defaultValue = new DefaultValue(mContext, TpInfo.DVBC);
            // favGroupList.clear();
            clearFavGroupList();
            rebuildFavGroup(defaultValue);
        }

        // clear previous favInfos. will be added again below
        for (FavGroup favGroup : favGroupList) {
            favGroup.delAllFavInfo();
        }

        synchronized (mFavGroupListLock) {
            for (ProgramInfo programInfo : ProgramInfoList) {
                if (programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                    tv += add_to_fav(favGroupList, programInfo, FavGroup.ALL_TV_TYPE);
                } else if (programInfo.getType() == ProgramInfo.PROGRAM_RADIO) {
                    radio += add_to_fav(favGroupList, programInfo, FavGroup.ALL_RADIO_TYPE);
                }
                if ((programInfo.getCategory_type() & 0x0001) == 1) {// Kids
                    kid += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_KIDS);
                }
                if ((((programInfo.getCategory_type() & 0x0002) >> 1) == 1)) {// education
                    education += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_EDUCATION);
                }
                if ((((programInfo.getCategory_type() & 0x0004) >> 2) == 1)) {// news
                    news += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_NEWS);
                }
                if ((((programInfo.getCategory_type() & 0x0008) >> 3) == 1)) {// movies
                    movies += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_MOVIES);
                }
                if ((((programInfo.getCategory_type() & 0x0010) >> 4) == 1)) {// variety
                    variety += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_VARIETY);
                }
                if ((((programInfo.getCategory_type() & 0x0020) >> 5) == 1)) {// music
                    music += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_MUSIC);
                }
                if ((((programInfo.getCategory_type() & 0x0040) >> 6) == 1)) {// adult
                    adult += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_ADULT);
                }
                if ((((programInfo.getCategory_type() & 0x0080) >> 7) == 1)) {// sports
                    sports += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_SPORTS);
                }
                if ((((programInfo.getCategory_type() & 0x0100) >> 8) == 1)) {// hd
                    hd += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_HD);
                }
                if ((((programInfo.getCategory_type() & 0x0200) >> 9) == 1)) {// Religion
                    region += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_RELIGION);
                }
                if ((((programInfo.getCategory_type() & 0x80000) >> 19) == 1)) {// shopping
                    shopping += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_SHOPPING);
                }
                if ((((programInfo.getCategory_type() & 0x0800) >> 11) == 1)) {// uhd
                    uhd += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_UHD);
                }
            }
            /*
             * LogUtils.d("TV = " + tv);
             * LogUtils.d("Radio = " + radio);
             * LogUtils.d("Kid = " + kid);
             * LogUtils.d("Education = " + education);
             * LogUtils.d("NEWS = " + news);
             * LogUtils.d("Movies = " + movies);
             * LogUtils.d("Variety = " + variety);
             * LogUtils.d("Music = " + music);
             * LogUtils.d("Adult = " + adult);
             * LogUtils.d("Sports = " + sports);
             * LogUtils.d("HD = " + hd);
             * LogUtils.d("Region = " + region);
             * LogUtils.d("Shopping = " + shopping);
             * LogUtils.d("UHD = " + uhd);
             * mDataManager.printFavGroup();
             */
        }
    }

    public List<FavGroup> category_update_to_fav_cns(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList) {
        List<FavGroup> newFavGroupList;
        DefaultValue defaultValue = new DefaultValue(mContext, TpInfo.DVBC);
        int kid = 0, education = 0, news = 0, movies = 0, variety = 0, music = 0, adult = 0, sports = 0,
                hd = 0, tv = 0, radio = 0, shopping = 0, religion = 0, leisure = 0;
        newFavGroupList = defaultValue.buildDefaultFavGroup();
        // clear previous favInfos. will be added again below
        // for (FavGroup favGroup : favGroupList) {
        // favGroup.delAllFavInfo();
        // }
        synchronized (mFavGroupListLock) {
            for (ProgramInfo programInfo : ProgramInfoList) {
                if (programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                    tv = checkNewFavListWithOld(newFavGroupList, tv, FavGroup.ALL_TV_TYPE, programInfo);
                } else if (programInfo.getType() == ProgramInfo.PROGRAM_RADIO) {
                    radio = checkNewFavListWithOld(newFavGroupList, radio, FavGroup.ALL_RADIO_TYPE, programInfo);
                }
                if ((programInfo.getCategory_type() & 0x0001) == 1) {// religion
                    religion = checkNewFavListWithOld(newFavGroupList, religion,
                            FavGroup.GROUP_CNS_PUBLIC_WELFARE_RRLIGION, programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0002) >> 1) == 1) {// music
                    music = checkNewFavListWithOld(newFavGroupList, music, FavGroup.GROUP_CNS_DRAMA_MUSIC, programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0004) >> 2) == 1) {// news
                    news = checkNewFavListWithOld(newFavGroupList, news, FavGroup.GROUP_CNS_NEWS_FINANCE, programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0008) >> 3) == 1) {// leisure
                    leisure = checkNewFavListWithOld(newFavGroupList, leisure, FavGroup.GROUP_CNS_LEISURE_KNOWLEDGE,
                            programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0010) >> 4) == 1) {// children
                    kid = checkNewFavListWithOld(newFavGroupList, kid, FavGroup.GROUP_CNS_CHILDREN_ANIMATION,
                            programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0020) >> 5) == 1) {// films
                    movies = checkNewFavListWithOld(newFavGroupList, movies, FavGroup.GROUP_CNS_FILMS_SERIES,
                            programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0040) >> 6) == 1) {// variety
                    variety = checkNewFavListWithOld(newFavGroupList, variety, FavGroup.GROUP_CNS_VARIETY, programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0080) >> 7) == 1) {// shopping
                    shopping = checkNewFavListWithOld(newFavGroupList, shopping, FavGroup.GROUP_CNS_HOME_SHOPPING,
                            programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0100) >> 8) == 1) {// learning
                    education = checkNewFavListWithOld(newFavGroupList, education,
                            FavGroup.GROUP_CNS_FOREIGN_LANGUAGE_LEARNING, programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0200) >> 9) == 1) {// hd
                    hd = checkNewFavListWithOld(newFavGroupList, hd, FavGroup.GROUP_CNS_HD, programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0400) >> 10) == 1) {// sports
                    sports = checkNewFavListWithOld(newFavGroupList, sports, FavGroup.GROUP_CNS_SPORTS_OTHERS,
                            programInfo);
                }
                if (((programInfo.getCategory_type() & 0x0800) >> 11) == 1) {// adult
                    adult = checkNewFavListWithOld(newFavGroupList, adult, FavGroup.GROUP_CNS_ADULT, programInfo);
                }
            }

//             LogUtils.d("TV = " + tv);
//             LogUtils.d("Radio = " + radio);
//             LogUtils.d("religion = " + religion);
//             LogUtils.d("Music = " + music);
//             LogUtils.d("NEWS = " + news);
//             LogUtils.d("leisure = " + leisure);
//             LogUtils.d("Kid = " + kid);
//             LogUtils.d("Movies = " + movies);
//             LogUtils.d("Variety = " + variety);
//             LogUtils.d("Shopping = " + shopping);
//             LogUtils.d("Education = " + education);
//             LogUtils.d("HD = " + hd);
//             LogUtils.d("Sports = " + sports);
//             LogUtils.d("Adult = " + adult);
//             mDataManager.printFavGroup("category_update");

        }
        return newFavGroupList;
    }

    public void category_add_to_fav_cns(List<ProgramInfo> ProgramInfoList, List<FavGroup> favGroupList) {
        int leisure = 0, religion = 0, kid = 0, education = 0, news = 0, movies = 0, variety = 0, music = 0, adult = 0,
                sports = 0, hd = 0, tv = 0, radio = 0, region = 0, shopping = 0, uhd = 0;
        if (favGroupList.size() != FavGroup.ALL_TV_RADIO_TYPE_MAX) {
            LogUtils.d("FAV Group number is not Match !!!!!!!! favGroupList.size() = " + favGroupList.size());
            LogUtils.d("Rebuild FAV Group");
            DefaultValue defaultValue = new DefaultValue(mContext, TpInfo.DVBC);
            // favGroupList.clear();
            clearFavGroupList();
            rebuildFavGroup(defaultValue);
        }

        // clear previous favInfos. will be added again below
        for (FavGroup favGroup : favGroupList) {
            favGroup.delAllFavInfo();
        }

        synchronized (mFavGroupListLock) {
            for (ProgramInfo programInfo : ProgramInfoList) {
                if (programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                    tv += add_to_fav(favGroupList, programInfo, FavGroup.ALL_TV_TYPE);
                } else if (programInfo.getType() == ProgramInfo.PROGRAM_RADIO) {
                    radio += add_to_fav(favGroupList, programInfo, FavGroup.ALL_RADIO_TYPE);
                }
                if ((programInfo.getCategory_type() & 0x0001) == 1) {// religion
                    religion += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_PUBLIC_WELFARE_RRLIGION);
                }
                if (((programInfo.getCategory_type() & 0x0002) >> 1) == 1) {// music
                    music += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_DRAMA_MUSIC);
                }
                if (((programInfo.getCategory_type() & 0x0004) >> 2) == 1) {// news
                    news += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_NEWS_FINANCE);
                }
                if (((programInfo.getCategory_type() & 0x0008) >> 3) == 1) {// leisure
                    leisure += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_LEISURE_KNOWLEDGE);
                }
                if (((programInfo.getCategory_type() & 0x0010) >> 4) == 1) {// children
                    kid += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_CHILDREN_ANIMATION);
                }
                if (((programInfo.getCategory_type() & 0x0020) >> 5) == 1) {// films
                    movies += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_FILMS_SERIES);
                }
                if (((programInfo.getCategory_type() & 0x0040) >> 6) == 1) {// variety
                    variety += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_VARIETY);
                }
                if (((programInfo.getCategory_type() & 0x0080) >> 7) == 1) {// shopping
                    shopping += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_HOME_SHOPPING);
                }
                if (((programInfo.getCategory_type() & 0x0100) >> 8) == 1) {// learning
                    education += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_FOREIGN_LANGUAGE_LEARNING);
                }
                if (((programInfo.getCategory_type() & 0x0200) >> 9) == 1) {// hd
                    hd += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_HD);
                }
                if (((programInfo.getCategory_type() & 0x0400) >> 10) == 1) {// sports
                    sports += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_SPORTS_OTHERS);
                }
                if (((programInfo.getCategory_type() & 0x0800) >> 11) == 1) {// adult
                    adult += add_to_fav(favGroupList, programInfo, FavGroup.GROUP_CNS_ADULT);
                }

            }
            /*
             * LogUtils.d("TV = " + tv);
             * LogUtils.d("Radio = " + radio);
             * LogUtils.d("religion = " + religion);
             * LogUtils.d("Music = " + music);
             * LogUtils.d("NEWS = " + news);
             * LogUtils.d("leisure = " + leisure);
             * LogUtils.d("Kid = " + kid);
             * LogUtils.d("Movies = " + movies);
             * LogUtils.d("Variety = " + variety);
             * LogUtils.d("Shopping = " + shopping);
             * LogUtils.d("Education = " + education);
             * LogUtils.d("HD = " + hd);
             * LogUtils.d("Sports = " + sports);
             * LogUtils.d("Adult = " + adult);
             * mDataManager.printFavGroup();
             */
        }
    }

    public void category_add_to_fav(List<ProgramInfo> ProgramInfoList, boolean del_all_fav) {
        List<FavGroup> favGroupList = getFavGroupList();

        if (favGroupList.size() != FavGroup.ALL_TV_RADIO_TYPE_MAX) {
            LogUtils.d("FAV Group number is not Match !!!!!!!! favGroupList.size() = " + favGroupList.size());
            LogUtils.d("Rebuild FAV Group");
            // DefaultValue defaultValue = new
            // DefaultValue(mContext,TpInfo.ISDBT);//casper20250206 test for isdbt
            DefaultValue defaultValue = new DefaultValue(mContext, Pvcfg.getTunerType());
            // favGroupList.clear();
            clearFavGroupList();
            rebuildFavGroup(defaultValue);
        }

        // clear previous favInfos. will be added again below
        if (del_all_fav) {
            for (FavGroup favGroup : favGroupList) {
                favGroup.delAllFavInfo();
            }
        }

        if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_SUND))
            category_add_to_fav_sund(ProgramInfoList, favGroupList);
        else if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC))
            category_add_to_fav_dmg(ProgramInfoList, favGroupList);
        else if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            category_add_to_fav_cns(ProgramInfoList, favGroupList);
        else
            category_add_to_fav_default(ProgramInfoList, favGroupList);

        mDataManager.printFavGroup("category_add");
    }

    private int checkNewFavListWithOld(List<FavGroup> newFavGroupList, int groupCnt, int groupType,
            ProgramInfo programInfo) {
        List<FavInfo> favInfoList = newFavGroupList.get(groupType).getFavInfoList();
        FavInfo favInfo = new FavInfo(groupCnt, programInfo.getChannelId(), groupType);
        FavInfo oldFavInfo = getFavInfo(groupType, groupCnt);
        if (oldFavInfo == null) {
            favInfo.setStatus(MiscDefine.SaveStatus.STATUS_ADD);
//             ProgramInfo p = getProgramInfo(favInfo.getChannelId());
//             Log.d(TAG,"add fav["+favInfo.getFavMode()+"] channel : ["+p.getDisplayNum()+"] "+p.getDisplayName());
        } else {
            favInfo.setStatus(MiscDefine.SaveStatus.STATUS_UPDATE);
            oldFavInfo.setStatus(MiscDefine.SaveStatus.STATUS_NOT_DELETE);
//             ProgramInfo p = getProgramInfo(favInfo.getChannelId());
//             Log.d(TAG,"update fav["+favInfo.getFavMode()+"] channel : ["+p.getDisplayNum()+"] "+p.getDisplayName());
        }

        favInfoList.add(favInfo);
        groupCnt++;
        return groupCnt;
    }

    public List<FavGroup> category_update_to_fav(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList) {
        if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC))
            return category_update_to_fav_dmg(ProgramInfoList, musicInfoList);
        else if (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            return category_update_to_fav_cns(ProgramInfoList, musicInfoList);
        return new ArrayList<>();
    }

    public List<FavGroup> category_update_to_fav_dmg(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList) {
        List<FavGroup> newFavGroupList;
        DefaultValue defaultValue = new DefaultValue(mContext, TpInfo.DVBC);
        int kid = 0, education = 0, news = 0, movies = 0, variety = 0, music = 0, adult = 0, sports = 0, hd = 0, tv = 0,
                radio = 0, region = 0, shopping = 0, uhd = 0, MANDARIN = 0, WESTERN = 0, JPOP = 0, LOUNGE = 0,
                CLASSICAL = 0, ELSE = 0;
        newFavGroupList = defaultValue.buildDefaultFavGroup();
        // clear previous favInfos. will be added again below
        // for (FavGroup favGroup : favGroupList) {
        // favGroup.delAllFavInfo();
        // }
        synchronized (mFavGroupListLock) {
            for (ProgramInfo programInfo : ProgramInfoList) {
                if (programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                    tv = checkNewFavListWithOld(newFavGroupList, tv, FavGroup.ALL_TV_TYPE, programInfo);
                } else if (programInfo.getType() == ProgramInfo.PROGRAM_RADIO) {
                    radio = checkNewFavListWithOld(newFavGroupList, radio, FavGroup.ALL_RADIO_TYPE, programInfo);

                    if (!musicInfoList.isEmpty()) {
                        int serviceId = programInfo.getServiceId();
                        for (int i = 0; i < musicInfoList.size(); i++) {
                            MusicInfo musicInfo = musicInfoList.get(i);
                            if (musicInfo.get_service_id_list().contains(serviceId)) {
                                if (i == 0) {
                                    MANDARIN = checkNewFavListWithOld(newFavGroupList, MANDARIN,
                                            FavGroup.GROUP_MANDARIN, programInfo);
                                } else if (i == 1) {
                                    WESTERN = checkNewFavListWithOld(newFavGroupList, WESTERN, FavGroup.GROUP_WESTERN,
                                            programInfo);
                                } else if (i == 2) {
                                    JPOP = checkNewFavListWithOld(newFavGroupList, JPOP, FavGroup.GROUP_JPOP,
                                            programInfo);
                                } else if (i == 3) {
                                    LOUNGE = checkNewFavListWithOld(newFavGroupList, LOUNGE, FavGroup.GROUP_LOUNGE,
                                            programInfo);
                                } else if (i == 4) {
                                    CLASSICAL = checkNewFavListWithOld(newFavGroupList, CLASSICAL,
                                            FavGroup.GROUP_CLASSICAL, programInfo);
                                } else if (i == 5) {
                                    ELSE = checkNewFavListWithOld(newFavGroupList, ELSE, FavGroup.GROUP_ELSE,
                                            programInfo);
                                }
                            }
                        }
                    }
                }
                if ((programInfo.getCategory_type() & 0x0001) == 1) {// Kids
                    kid = checkNewFavListWithOld(newFavGroupList, kid, FavGroup.GROUP_KIDS, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0002) >> 1) == 1)) {// education
                    education = checkNewFavListWithOld(newFavGroupList, education, FavGroup.GROUP_EDUCATION,
                            programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0004) >> 2) == 1)) {// news
                    news = checkNewFavListWithOld(newFavGroupList, news, FavGroup.GROUP_NEWS, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0008) >> 3) == 1)) {// movies
                    movies = checkNewFavListWithOld(newFavGroupList, movies, FavGroup.GROUP_MOVIES, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0010) >> 4) == 1)) {// variety
                    variety = checkNewFavListWithOld(newFavGroupList, variety, FavGroup.GROUP_VARIETY, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0020) >> 5) == 1)) {// music
                    music = checkNewFavListWithOld(newFavGroupList, music, FavGroup.GROUP_MUSIC, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0040) >> 6) == 1)) {// adult
                    adult = checkNewFavListWithOld(newFavGroupList, adult, FavGroup.GROUP_ADULT, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0080) >> 7) == 1)) {// sports
                    sports = checkNewFavListWithOld(newFavGroupList, sports, FavGroup.GROUP_SPORTS, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0100) >> 8) == 1)) {// hd
                    hd = checkNewFavListWithOld(newFavGroupList, hd, FavGroup.GROUP_HD, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0200) >> 9) == 1)) {// Religion
                    region = checkNewFavListWithOld(newFavGroupList, region, FavGroup.GROUP_RELIGION, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x80000) >> 19) == 1)) {// shopping
                    shopping = checkNewFavListWithOld(newFavGroupList, shopping, FavGroup.GROUP_SHOPPING, programInfo);
                }
                if ((((programInfo.getCategory_type() & 0x0800) >> 11) == 1)) {// uhd
                    uhd = checkNewFavListWithOld(newFavGroupList, uhd, FavGroup.GROUP_UHD, programInfo);
                }
            }
            LogUtils.d("TV = " + tv);
            LogUtils.d("Radio = " + radio);
            LogUtils.d("Kid = " + kid);
            LogUtils.d("Education = " + education);
            LogUtils.d("NEWS = " + news);
            LogUtils.d("Movies = " + movies);
            LogUtils.d("Variety = " + variety);
            LogUtils.d("Music = " + music);
            LogUtils.d("Adult = " + adult);
            LogUtils.d("Sports = " + sports);
            LogUtils.d("HD = " + hd);
            LogUtils.d("Region = " + region);
            LogUtils.d("Shopping = " + shopping);
            LogUtils.d("UHD = " + uhd);
            LogUtils.d("MANDARIN = " + MANDARIN);
            LogUtils.d("WESTERN = " + WESTERN);
            LogUtils.d("JPOP = " + JPOP);
            LogUtils.d("LOUNGE = " + LOUNGE);
            LogUtils.d("CLASSICAL = " + CLASSICAL);
            LogUtils.d("ELSE = " + ELSE);
            // mDataManager.printFavGroup();
        }
        return newFavGroupList;
    }

    public List<BookInfo> getBookInfoList() {
        return new ArrayList<>(mBookInfoList);
    }

    public void updateBookInfoList(List<BookInfo> bookInfoList) {
        for (BookInfo bookInfo : mBookInfoList) {
            for (BookInfo newBookInfo : bookInfoList) {
                bookInfo.setStatus(MiscDefine.SaveStatus.STATUS_NONE);
                if (newBookInfo.getBookId() == bookInfo.getBookId()) { // same
                    bookInfo.setStatus(MiscDefine.SaveStatus.STATUS_NOT_DELETE);
                }
            }
        }
        for (BookInfo bookInfo : mBookInfoList) {
            if (bookInfo.getStatus() != MiscDefine.SaveStatus.STATUS_NOT_DELETE)
                DataManagerDeleteBookInfo(bookInfo);
            bookInfo.setStatus(MiscDefine.SaveStatus.STATUS_NONE);
        }
        mBookInfoList = bookInfoList;
        for (BookInfo bookInfo : mBookInfoList) {
            DataManagerSaveBookInfo(bookInfo);
        }
    }

    public void addBookInfo(BookInfo bookInfo) {
        int findBookInfo = 0, i;
        if (mBookInfoList.size() < BookInfo.MAX_NUM_OF_BOOKINFO) {
            for (i = 0; i < mBookInfoList.size(); i++) {
                if (mBookInfoList.get(i).getBookId() == bookInfo.getBookId()) {
                    findBookInfo = 1;
                    break;
                }
            }
            if (findBookInfo == 1)
                mBookInfoList.get(i).update(bookInfo);
            else
                mBookInfoList.add(bookInfo);
            DataManagerSaveBookInfo(bookInfo);
        }
    }

    public void updateBookInfo(int bookId, BookInfo bookInfo) {
        for (int i = 0; i < mBookInfoList.size(); i++) {
            if (mBookInfoList.get(i).getBookId() == bookId) {
                ProgramInfo p = getProgramInfo(bookInfo.getChannelId());
                if (p == null)
                    continue;
                mBookInfoList.get(i).update(bookInfo);
                DataManagerSaveBookInfo(bookInfo);
            }
        }
    }

    public BookInfo getBookInfo(int bookId) {
        for (int i = 0; i < mBookInfoList.size(); i++) {
            if (mBookInfoList.get(i).getBookId() == bookId) {
                return mBookInfoList.get(i);
            }
        }
        return null;
    }

    public void delBookInfo(int bookId) {
        Iterator<BookInfo> iterator = mBookInfoList.iterator();
        while (iterator.hasNext()) {
            BookInfo bookInfoTmp = iterator.next();
            if (bookInfoTmp.getBookId() == bookId) {
                DataManagerDeleteBookInfo(bookInfoTmp);
                iterator.remove(); // ✅ 用 iterator 移除是安全的
            }
        }
    }

    public void delAllBookInfo() {
        mBookInfoList.clear();
    }

    public List<HIDL_BUFFER_S> getmHidlBufferList() {
        return mHidlBufferList;
    }

    // sysglob.c
    public int skip_service_by_SkipPvrSkip(ProgramInfo programInfo, int includeSkipFlag, int includePvrSkipFlag) {
        int SkipFlag, PvrSkipFlag;
        SkipFlag = programInfo.getSkip();
        PvrSkipFlag = programInfo.getPvrSkip();
        if (includeSkipFlag == 1 && includePvrSkipFlag == 1) {
            return 0;
        } else if ((includeSkipFlag == 0) && (includePvrSkipFlag == 0)) {
            if (SkipFlag == 1 || PvrSkipFlag == 1)
                return 1;
            else
                return 0;
        } else {
            if (includeSkipFlag == 1) {
                if (PvrSkipFlag == 1)
                    return 1;
                else
                    return 0;
            } else {
                if (SkipFlag == 1)
                    return 1;
                else
                    return 0;
            }
        }
    }

    public int get_number_of_service_by_tv_radio(int TvRadioType, int includeSkipFlag, int includePvrSkipFlag) {
        int service_number = 0;
        if (mProgramInfoList.size() == 0) {
            Log.d(TAG, "Input parameter error");
            return -1;
        }
        if (!((TvRadioType == FavGroup.ALL_TV_TYPE) || (TvRadioType == FavGroup.ALL_RADIO_TYPE))) {
            Log.d(TAG, "Input parameter error");
            return -1;
        }
        for (ProgramInfo tmp : mProgramInfoList) {
            if (TvRadioType == tmp.getType()) {
                if (skip_service_by_SkipPvrSkip(tmp, includeSkipFlag, includePvrSkipFlag) == 0) {
                    service_number++;
                }
            }
        }
        return service_number;
    }

    public int get_number_of_service_by_groupType(int groupType, int includeSkipFlag, int includePvrSkipFlag) {
        ProgramInfo programInfo = null;
        int index = 0;
        int service_number = 0;
        if (groupType >= FavGroup.ALL_TV_RADIO_TYPE_MAX) {
            Log.d(TAG, "[Input parameter error\n");
            return -1;
        }
        if ((groupType == FavGroup.ALL_TV_TYPE) || (groupType == FavGroup.ALL_RADIO_TYPE))
            service_number = get_number_of_service_by_tv_radio(groupType, includeSkipFlag, includePvrSkipFlag);
        else {
            while (index < MAX_GROUP_CH_NUM) {
                programInfo = get_service_by_group_index(groupType, index);
                if (programInfo == null)
                    break;
                if (skip_service_by_SkipPvrSkip(programInfo, includeSkipFlag, includePvrSkipFlag) == 0)
                    service_number++;
                index++;
            }
        }
        return service_number;
    }

    public ProgramInfo get_service_by_group_index(int groupType, int index) {
        long channelId;
        if (groupType == FavGroup.ALL_TV_TYPE || groupType == FavGroup.ALL_RADIO_TYPE) {
            Log.d(TAG, "Can not find service by groupType and index");
            return null;
        }
        channelId = mFavGroupList.get(groupType).getFavInfoList().get(index).getChannelId();
        if (channelId == 0) {
            Log.d(TAG, "Can not find service by groupType and index");
            return null;
        }
        return get_service_by_channelId(channelId);
    }

    public ProgramInfo get_service_by_channelId(long channelId) {
        // synchronized(mProgramListLock) {

        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222");
            Iterator<ProgramInfo> iterator = mProgramInfoList.iterator();
            while (iterator.hasNext()) {
                ProgramInfo tmp = iterator.next();
                if (tmp.getChannelId() == channelId)
                    return new ProgramInfo(tmp);
            }
            Log.d(TAG, "Can not find service by channelId[" + channelId + "]");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("44444444444444");
        }
        // }
    }

    public int get_totalnum_of_group(int groupType) {
        synchronized (mFavGroupListLock) {
            if (groupType < FavGroup.ALL_TV_TYPE || groupType > FavGroup.ALL_TV_RADIO_TYPE_MAX) {
                Log.d(TAG, "groupType " + groupType + " error");
                return 0;
            }
            return mFavGroupList.get(groupType).getFavInfoList().size();
        }
    }

    public void set_cur_channel(int groupType, long channelId) {
        // mGposInfo.setCurGroupType(groupType);
        GposInfo.setCurChannelId(mContext, channelId);
    }

    public void setStartOnChannel(long channelId, int groupType) {
        GposInfo.setStartOnChannelId(mContext, channelId);
        GposInfo.setStartOnChType(mContext, groupType);
    }

    ////// Scoty Add SimpleChannel of PesiCmd
    // public void SetNetProgramDatabase(NetProgramDatabase netProgramDatabase)
    // {
    // mNetProgramDatabase = netProgramDatabase;
    // }
    public NetProgramDatabase GetNetProgramDatabase() {
        return mNetProgramDatabase;
    }

    public class NetProgramDatabase {
        public DefaultChannel GetDefaultChannel(Context context) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            List<ProgramInfo> tmpProgramInfoList = mProgramDatabaseTable.load();
            DefaultChannel defaultChannel = new DefaultChannel();
            if (tmpProgramInfoList != null) {
                if (tmpProgramInfoList.size() == 0)
                    return null;
                defaultChannel.setChanneId(tmpProgramInfoList.get(0).getChannelId());
                defaultChannel.setGroupType(tmpProgramInfoList.get(0).getType());
                return defaultChannel;
            } else
                return null;

        }

        public void SetDefaultChannel(Context context, int channelId) {

        }

        public void SaveSimpleChannelList(Context context, List<SimpleChannel> channelList, int type,
                List<ProgramInfo> programInfoList) {
            for (int i = 0; i < channelList.size(); i++) {
                for (int j = 0; j < programInfoList.size(); j++) {
                    SimpleChannel channel = channelList.get(i);
                    if (programInfoList.get(j).getChannelId() == channelList.get(i).get_channel_id()) {
                        programInfoList.get(j).setChannelId(channel.get_channel_id());
                        programInfoList.get(j).setDisplayName(channel.get_channel_name());
                        programInfoList.get(j).setDisplayNum(channel.get_channel_num());
                        programInfoList.get(j).setLock(channel.get_user_lock());
                        programInfoList.get(j).setCA(channel.get_ca());
                        programInfoList.get(j).setSkip(channel.get_channel_skip());
                        Log.d(TAG, "exce SaveSimpleChannelList: tpId = [" + programInfoList.get(j).getTpId() + "]");
                    }
                }
            }

            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            mProgramDatabaseTable.save(programInfoList);

        }

        public List<SimpleChannel> GetSimpleChannelList(Context context) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            List<SimpleChannel> simpleChannelList = new ArrayList<>();
            List<ProgramInfo> tmpProgramInfoList = mProgramDatabaseTable.load();
            for (int i = 0; i < tmpProgramInfoList.size(); i++) {
                SimpleChannel channel = new SimpleChannel();
                channel.set_channel_id(tmpProgramInfoList.get(i).getChannelId());
                channel.set_type(tmpProgramInfoList.get(i).getType());
                channel.set_channel_name(tmpProgramInfoList.get(i).getDisplayName());
                channel.set_channel_num(tmpProgramInfoList.get(i).getDisplayNum());
                channel.set_user_lock(tmpProgramInfoList.get(i).getLock());
                channel.set_ca(tmpProgramInfoList.get(i).getCA());
                channel.set_channel_skip(tmpProgramInfoList.get(i).getSkip());
                channel.set_tp_id(tmpProgramInfoList.get(i).getTpId());
                channel.set_play_stream_type(ProgramPlayStreamType.DVB_TYPE);

                simpleChannelList.add(channel);
            }

            return simpleChannelList;
        }

        public void SaveNetProgramList(Context context, List<SimpleChannel> NetChannelList) {
            List<NetProgramInfo> mNetProgramInfoList = new ArrayList<>();

            for (int i = 0; i < NetChannelList.size(); i++) {
                NetProgramInfo mNetProgramInfo = new NetProgramInfo();
                mNetProgramInfo.setChannelId(NetChannelList.get(i).get_channel_id());
                mNetProgramInfo.setPlayStreamType(NetChannelList.get(i).get_play_stream_type());
                mNetProgramInfo.setGroupType(NetChannelList.get(i).get_type());
                mNetProgramInfo.setChannelNum(NetChannelList.get(i).get_channel_num());
                mNetProgramInfo.setChannelName(NetChannelList.get(i).get_channel_name());
                mNetProgramInfo.setUserLock(NetChannelList.get(i).get_user_lock());
                mNetProgramInfo.setSkip(NetChannelList.get(i).get_channel_skip());
                mNetProgramInfo.setVideoUrl(NetChannelList.get(i).get_url());

                mNetProgramInfoList.add(mNetProgramInfo);
            }

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.save(mNetProgramInfoList);

            /// Scoty Test Broadcast
            // StartNotify(context);

        }

        public void AddNetProgramList(Context context, List<SimpleChannel> NetChannelList) {
            List<NetProgramInfo> mNetProgramInfoList = new ArrayList<>();

            for (int i = 0; i < NetChannelList.size(); i++) {
                NetProgramInfo mNetProgramInfo = new NetProgramInfo();
                mNetProgramInfo.setChannelId(NetChannelList.get(i).get_channel_id());
                mNetProgramInfo.setPlayStreamType(NetChannelList.get(i).get_play_stream_type());
                mNetProgramInfo.setGroupType(NetChannelList.get(i).get_type());
                mNetProgramInfo.setChannelNum(NetChannelList.get(i).get_channel_num());
                mNetProgramInfo.setChannelName(NetChannelList.get(i).get_channel_name());
                mNetProgramInfo.setUserLock(NetChannelList.get(i).get_user_lock());
                mNetProgramInfo.setSkip(NetChannelList.get(i).get_channel_skip());
                mNetProgramInfo.setVideoUrl(NetChannelList.get(i).get_url());

                mNetProgramInfoList.add(mNetProgramInfo);
            }

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.add(mNetProgramInfoList);

        }

        public void UpdateNetProgramList(Context context, List<SimpleChannel> NetChannelList) {
            List<NetProgramInfo> mNetProgramInfoList = new ArrayList<>();

            for (int i = 0; i < NetChannelList.size(); i++) {
                NetProgramInfo mNetProgramInfo = new NetProgramInfo();
                mNetProgramInfo.setChannelId(NetChannelList.get(i).get_channel_id());
                mNetProgramInfo.setPlayStreamType(NetChannelList.get(i).get_play_stream_type());
                mNetProgramInfo.setGroupType(NetChannelList.get(i).get_type());
                mNetProgramInfo.setChannelNum(NetChannelList.get(i).get_channel_num());
                mNetProgramInfo.setChannelName(NetChannelList.get(i).get_channel_name());
                mNetProgramInfo.setUserLock(NetChannelList.get(i).get_user_lock());
                mNetProgramInfo.setSkip(NetChannelList.get(i).get_channel_skip());
                mNetProgramInfo.setVideoUrl(NetChannelList.get(i).get_url());

                mNetProgramInfoList.add(mNetProgramInfo);
            }

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.update(mNetProgramInfoList);

        }

        public void UpdateNetProgramInfo(Context context, SimpleChannel NetChannel) {
            NetProgramInfo mNetProgramInfo = new NetProgramInfo();
            mNetProgramInfo.setChannelId(NetChannel.get_channel_id());
            mNetProgramInfo.setPlayStreamType(NetChannel.get_play_stream_type());
            mNetProgramInfo.setGroupType(NetChannel.get_type());
            mNetProgramInfo.setChannelNum(NetChannel.get_channel_num());
            mNetProgramInfo.setChannelName(NetChannel.get_channel_name());
            mNetProgramInfo.setUserLock(NetChannel.get_user_lock());
            mNetProgramInfo.setSkip(NetChannel.get_channel_skip());
            mNetProgramInfo.setVideoUrl(NetChannel.get_url());

            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            netProgramDatabaseTable.updateNetProgramInfo(mNetProgramInfo);

            /// Scoty Test Broadcast
            StartNotify(context);

        }

        public boolean SaveNetProgramListDatabase(Context context, List<NetProgramInfo> NetChannelList) {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            boolean isSuccess = netProgramDatabaseTable.save(NetChannelList);
            Log.e(TAG, "SaveNetProgramListDatabase: save isSuccess = [" + isSuccess + "]");
            return isSuccess;
        }

        public List<NetProgramInfo> GetNetProgramList(Context context) {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            return netProgramDatabaseTable.load();
        }

        public void StartNotify(Context context) {
            Intent SearchIntent = new Intent();
            SearchIntent.setAction("com.prime.netprogram.database.update");
            context.sendBroadcast(SearchIntent, "android.permission.NETPROGRAM_BROADCAST");
        }

        public List<SimpleChannel> GetNetSimpleChannelList(Context context) {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            List<NetProgramInfo> tmpNetProgramInfoList = new ArrayList<>();
            // if(netProgramDatabaseTable != null)
            // tmpNetProgramInfoList = netProgramDatabaseTable.load();
            List<SimpleChannel> simpleChannelList = new ArrayList<>();

            for (int i = 0; i < tmpNetProgramInfoList.size(); i++) {
                SimpleChannel channel = new SimpleChannel();
                channel.set_channel_id(tmpNetProgramInfoList.get(i).getChannelId());
                channel.set_type(tmpNetProgramInfoList.get(i).getGroupType());
                channel.set_channel_name(tmpNetProgramInfoList.get(i).getChannelName());
                channel.set_channel_num(tmpNetProgramInfoList.get(i).getChannelNum());
                channel.set_user_lock(tmpNetProgramInfoList.get(i).getUserLock());
                channel.set_channel_skip(tmpNetProgramInfoList.get(i).getSkip());
                channel.set_play_stream_type(tmpNetProgramInfoList.get(i).getPlayStreamType());
                channel.set_url(tmpNetProgramInfoList.get(i).getVideoUrl());

                simpleChannelList.add(channel);
            }

            return simpleChannelList;
        }

        public int ResetDatabase(Context context) {
            NetProgramDatabaseTable netProgramDatabaseTable = new NetProgramDatabaseTable(context);
            int count = netProgramDatabaseTable.removeAll();

            return count;
        }

    }

    //////
    public ProgramDatabase GetProgramDatabase() {
        return mProgramDatabase;
    }

    public class ProgramDatabase {
        public void SaveSimpleChannelList(Context context, List<SimpleChannel> channelList, int type,
                List<ProgramInfo> programInfoList) {
            for (int i = 0; i < channelList.size(); i++) {
                for (int j = 0; j < programInfoList.size(); j++) {
                    SimpleChannel channel = channelList.get(i);
                    if (programInfoList.get(j).getChannelId() == channelList.get(i).get_channel_id()) {
                        programInfoList.get(j).setChannelId(channel.get_channel_id());
                        programInfoList.get(j).setDisplayName(channel.get_channel_name());
                        programInfoList.get(j).setDisplayNum(channel.get_channel_num());
                        programInfoList.get(j).setLock(channel.get_user_lock());
                        programInfoList.get(j).setCA(channel.get_ca());
                        programInfoList.get(j).setSkip(channel.get_channel_skip());
                        Log.d(TAG, "exce SaveSimpleChannelList: tpId = [" + programInfoList.get(j).getTpId() + "]");
                    }
                }
            }

            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            mProgramDatabaseTable.save(programInfoList);
        }

        public void SaveProgramInfolList(Context context, int type, List<ProgramInfo> programInfoList) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            mProgramDatabaseTable.save(programInfoList);
        }

        public List<ProgramInfo> GetProgramInfoList(Context context) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            return mProgramDatabaseTable.load();
        }

        public List<SimpleChannel> GetSimpleChannelList(Context context) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            List<SimpleChannel> simpleChannelList = new ArrayList<>();
            List<ProgramInfo> tmpProgramInfoList = mProgramDatabaseTable.load();
            for (int i = 0; i < tmpProgramInfoList.size(); i++) {
                SimpleChannel channel = new SimpleChannel();
                channel.set_channel_id(tmpProgramInfoList.get(i).getChannelId());
                channel.set_type(tmpProgramInfoList.get(i).getType());
                channel.set_channel_name(tmpProgramInfoList.get(i).getDisplayName());
                channel.set_channel_num(tmpProgramInfoList.get(i).getDisplayNum());
                channel.set_user_lock(tmpProgramInfoList.get(i).getLock());
                channel.set_ca(tmpProgramInfoList.get(i).getCA());
                channel.set_channel_skip(tmpProgramInfoList.get(i).getSkip());
                channel.set_tp_id(tmpProgramInfoList.get(i).getTpId());
                channel.set_play_stream_type(ProgramPlayStreamType.DVB_TYPE);

                simpleChannelList.add(channel);
            }

            return simpleChannelList;
        }

        public int ResetDatabase(Context context) {
            ProgramDatabaseTable mProgramDatabaseTable = new ProgramDatabaseTable(context);
            if (Pvcfg.isPrimeTvInputEnable())
                TIFChannelData.deleteTIFChannelDataByTvInputId(mContext, Pvcfg.getTvInputId());
            int count = mProgramDatabaseTable.removeAll();

            return count;
        }
    }

    public static Object getProgramListLock() {
        return mProgramListLock;
    }

    public void ProtectProgramList() throws InterruptedException {
        if (GetDebugSemaphore())
            LogUtils.d("11111111111111");
        mProgramListSemphore.acquire();
        if (GetDebugSemaphore())
            LogUtils.d("22222222222222");
    }

    public void unProtectProgrameList() {
        if (GetDebugSemaphore())
            LogUtils.d("333333333333333333");
        mProgramListSemphore.release();
        if (GetDebugSemaphore())
            LogUtils.d("44444444444");
    }

    private boolean GetDebugSemaphore() {
        boolean value = SystemProperties.getBoolean("persist.sys.prime.debug.programe_semaphore", DEBUG_SEMAPHORE);
        return value;
    }

    public void delete_maildata_of_db(int mail_id) {
        DataManagerDelMailData(mail_id);
        Log.d(TAG, "delete mail id : " + mail_id);
    }

    public void save_maildata_to_db(MailData mailData) {
        DataManagerSaveMailData(mailData);
    }

    public MailData get_mail_from_db(int id) {
        MailDatabaseTable mailDatabaseTable = new MailDatabaseTable(mContext);
        MailData mailData = mailDatabaseTable.query(id);
        return mailData;
    }

    public List<MailData> get_mail_list_from_db() { // for ui mail page to show
        List<MailData> mailDataList = new MailDatabaseTable(mContext).load();
        return mailDataList;
    }
    public CNSMailData get_cns_mail_from_db(int id) {//eric lin 20260106 CNS mail
        CNSMailDatabaseTable mailDatabaseTable = new CNSMailDatabaseTable(mContext);
        CNSMailData mailData = mailDatabaseTable.query(id);
        return mailData;
    }
    public List<CNSMailData> get_cns_mail_list_from_db() { //eric lin 20260106 CNS mail
        List<CNSMailData> mailDataList = new CNSMailDatabaseTable(mContext).load();
        return mailDataList;
    }
    public CNSEasData get_cns_eas_from_db(int id) {
        CNSEasDatabaseTable easDatabaseTable = new CNSEasDatabaseTable(mContext);
        CNSEasData easData = easDatabaseTable.query(id);
        return easData;
    }
    public List<CNSEasData> get_cns_eas_list_from_db() {
        List<CNSEasData> easDataList = new CNSEasDatabaseTable(mContext).load();
        return easDataList;
    }
    public int SetTvInputId(String id) {
        // Log.d(TAG,"set_tv_input_id inputId = "+id);
        mTvInputId = id;
        return 0;
    }

    public String GetTvInputId() {
        return mTvInputId;
    }

    public void resetAllProgramAudioDefaultLanguage(String lang) {
        try {
            if (GetDebugSemaphore())
                LogUtils.d("11111111111111");
            mProgramListSemphore.acquire();
            if (GetDebugSemaphore())
                LogUtils.d("22222222222222");
            // synchronized (mProgramListLock) {
            for (ProgramInfo programInfo : mProgramInfoList) {
                if(!programInfo.pAudios.isEmpty()) {
                    for ( int i = 0; i < programInfo.pAudios.size(); i++ ) {
                        String audioLang = programInfo.pAudios.get(i).getLeftIsoLang();
                        if(audioLang.equalsIgnoreCase("zho"))
                            audioLang = "chi";
                        if(audioLang.equals(lang)) {
                            programInfo.setAudioSelected(i);
                            DataManagerSaveProgramData(programInfo);
                            break;
                        }
                    }
                }
            }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (GetDebugSemaphore())
                LogUtils.d("3333333333333333");
            mProgramListSemphore.release();
            if (GetDebugSemaphore())
                LogUtils.d("444444444444444");
        }
    }
}
