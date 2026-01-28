package com.prime.dtv.service.Table;

import static java.lang.Byte.toUnsignedInt;

import android.media.tv.tuner.Tuner;
import android.util.Log;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback.FilterCompleteCallback;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public abstract class Table {
    private static final String TAG = "Table";

    public static final byte PAT_TABLE_ID = 0x00;
    public static final byte CAT_TABLE_ID = 0x01;
    public static final byte PMT_TABLE_ID = 0x02;
    public static final byte NIT_ACTUAL_NETWORK_TABLE_ID = 0x40;
    public static final byte NIT_OTHER_NETWORK_TABLE_ID = 0x41;
    public static final byte SDT_ACTUAL_TS_TABLE_ID = 0x42;
    public static final byte SDT_OTHER_TS_TABLE_ID = 0x46;
    public static final byte BAT_TABLE_ID = 0x4A;
    public static final byte EIT_PRESENT_FOLLOWING_TABLE_ID = 0x4E;
    public static final byte EIT_SCHEDULING_MIN_TABLE_ID = 0x50;
    public static final byte EIT_SCHEDULING_MAX_TABLE_ID = 0x5F;
    public static final byte EIT_OTHER_PRESENT_FOLLOWING_TABLE_ID = 0x4F;
    public static final byte EIT_OTHER_SCHEDULING_MIN_TABLE_ID = 0x60;
    public static final byte EIT_OTHER_SCHEDULING_MAX_TABLE_ID = 0x6F;
    public static final byte TDT_TABLE_ID = 0x70;
    public static final byte TOT_TABLE_ID = 0x73;

    public static final byte EMM_EMERGENCY_TABLE_ID = (byte) 0x90;
    public static final byte EMM_NORMAL_TABLE_ID = (byte) 0x91;

    public static final byte PAT_PID = 0x0000;
    public static final byte CAT_PID = 0x0001;
    public static final byte NIT_PID = 0x0010;
    public static final byte BAT_PID = 0x0011;
    public static final byte SDT_PID = 0x0011;
    public static final byte EIT_PID = 0x0012;
    public static final byte TDT_TOT_PID = 0x0014;

    private FilterCompleteCallback mFilterCompleteCallback;
    private final Demux mDemux;
    private final int mTunerId;
    private final Semaphore mSemaphore;
    private final List<TableData> mDataList; // only one data in list except PMT

    private final byte mTableId;

    private final boolean mMultiTable;
    private Thread mthread;

    public Table(int tunerId, byte tableId) {

        mTunerId = tunerId;
        mTableId = tableId;

        mDemux = new Demux();
        mSemaphore = new Semaphore(1);
        mDataList = new ArrayList<>();

        mMultiTable = false;
        createCallback();
    }

    public Table(int tunerId, byte tableId ,boolean MultiTable) {
        mTunerId = tunerId;
        mTableId = tableId;

        mDemux = new Demux();
        mSemaphore = new Semaphore(1);
        mDataList = new ArrayList<>();

        mMultiTable = MultiTable;

        createCallback();
        //if(!(tableId == SDT_OTHER_TS_TABLE_ID))
        //if(MultiTable == false)
        //    run();
    }


    public void processWait() {
        synchronized(mDataList) {
            try {
                mDataList.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processNotify() {
        synchronized(mDataList) {
            mDataList.notify();
        }
    }

    protected List<TableData> getData() {
        return mDataList;
    }

    private void createCallback() {
        mFilterCompleteCallback = new FilterCompleteCallback() {
            @Override
            public void onPesiFilterEvent(byte[] data, int dataLen) {
                if (mDemux.getAbortFlag() == 1) {
                    Log.d(TAG, "onPesiFilterEvent Abort ");
                    return;
                }

                TableData tableData = parsing(data, dataLen);

                if(tableData!=null){
                    if (mTableId != PMT_TABLE_ID) {
                        if (mMultiTable == true && (mTableId == SDT_OTHER_TS_TABLE_ID || mTableId == SDT_ACTUAL_TS_TABLE_ID)) {
                            int TsId, TsId2,i;
                            TsId = ((SdtData) tableData).getTransportStreamIdByIndex(0);
                            for (i = 0; i < mDataList.size(); i++) {
                                TsId2 = ((SdtData) mDataList.get(i)).getTransportStreamIdByIndex(0);
                                if (TsId2 == TsId) {
                                    mDataList.remove(i);
                                    break;
                                }
                            }
                        }
                        else {
                            mDataList.clear(); // only one data in list except PMT
                        }
                    }
                    mDataList.add(tableData);
                }
            }

            @Override
            public void onPesiFilterStatusChanged(int status) {
                LogUtils.d("[Ethan] Filter status = "+status);
            }
        };
    }

    protected void run(String name) {
        TableRunnable runnable = new TableRunnable(name);
        newThread(runnable);
    }

    protected void process(Demux.DemuxChannel demuxChannel) {
        try {
            mSemaphore.acquire();
            if(mDemux.getAbortFlag() == 0) {
                if (demuxChannel.getTimeOut() == 0) {
                    mDemux.monitorTable(demuxChannel, mTunerId);
                }
                else {
                    mDemux.getTable(demuxChannel, mTunerId);
                    //finish_table();
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            mSemaphore.release();
        }
    }

    protected class TableRunnable implements Runnable {
        private String mTableName;

        public TableRunnable(String table_name) {
            mTableName = table_name;
        }

        public String getTalbeName(){
            return mTableName;
        }
        @Override
        public void run() {
            List<Demux.DemuxChannel> demuxChannels = getDemuxChannels();

            for (Demux.DemuxChannel demuxChannel : demuxChannels) {
                process(demuxChannel);
            }

            processNotify();
        }
    }

    private void newThread(TableRunnable runnable) {
        //new Thread(runnable, runnable.getTalbeName()).start();
        mthread = new Thread(runnable,runnable.getTalbeName());
        mthread.start();
    }

    public void abort() {
        mDemux.setAbortFlag(1);
        processNotify();
        if(mthread != null) {
            try {
                mthread.join();
            } catch (InterruptedException e) {
                mthread = null;
                e.printStackTrace();
            }
            mthread = null;
        }
    }

    protected byte getTableId() {
        return mTableId;
    }

    protected FilterCompleteCallback getFilterCompleteCallback() {
        return mFilterCompleteCallback;
    }

    protected abstract List<Demux.DemuxChannel> getDemuxChannels();
    protected abstract TableData parsing(byte[] data, int lens);
    protected abstract void finish_table();
}
