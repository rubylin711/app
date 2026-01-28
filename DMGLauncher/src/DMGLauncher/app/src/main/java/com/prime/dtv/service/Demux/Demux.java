package com.prime.dtv.service.Demux;

import static java.lang.Thread.sleep;

import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.SectionSettingsWithSectionBits;
import android.media.tv.tuner.filter.Settings;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.utils.LogUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demux {
    private static final String TAG = "Demux";
    private static final long DEFAULT_FILTER_BUFFER_SIZE = 32*1024;//32 * 1024; // 32kb from realtek demo code
    private int mAbortFlag = 0;
    private Filter mFilter = null;
    private DemuxChannel mDemuxChannel = null;
    private Executor mExecutor;
    private HandlerThread mExecutorHandlerThread = null;
    private Handler mExecutorHandlerThreadHandler = null;
    private static final int THREAD_COUNT = 1;
    private TunerInterface mTunerInterface = null;
    private boolean isClosing = false;

    public static class DemuxFilter {
        private byte[] mFilterData;
        private byte[] mFilterMask;

        public byte[] getFilterData() {
            return mFilterData;
        }

        public void setFilterData(byte[] filterData) {
            this.mFilterData = filterData;
        }

        public byte[] getFilterMask() {
            return mFilterMask;
        }

        public void setFilterMask(byte[] filterMask) {
            this.mFilterMask = filterMask;
        }
    }

    public static class DemuxChannel {
        private int mPid;
        private int mTableId;
        private boolean mCrcEnable;
        private boolean mRepeat;
        private int mTimeOut;
        private DemuxFilter mFilter;
        private long mFilterBufferSize = DEFAULT_FILTER_BUFFER_SIZE;
        private DemuxSectionCallback mDemuxSectionCallback = null;


        public int getPid() {
            return mPid;
        }

        public void setPid(int pid) {
            this.mPid = pid;
        }

        public int getTableId() {
            return mTableId;
        }

        public void setTableId(int tableId) {
            this.mTableId = tableId;
        }

        public boolean getCrcEnable() {
            return mCrcEnable;
        }

        public void setCrcEnable(boolean crcEnable) {
            this.mCrcEnable = crcEnable;
        }

        public boolean getRepeat() {
            return mRepeat;
        }

        public void setRepeat(boolean repeat) {
            this.mRepeat = repeat;
        }

        public DemuxSectionCallback getDemuxSectionCallback() {
            return mDemuxSectionCallback;
        }

        public void setDemuxSectionCallback(DemuxSectionCallback demuxSectionCallback) {
            this.mDemuxSectionCallback = demuxSectionCallback;
        }

        public int getTimeOut() {
            return mTimeOut;
        }

        public void setTimeOut(int timeOut) {
            this.mTimeOut = timeOut;
        }

        public DemuxFilter getFilter() {
            return mFilter;
        }

        public void setFilter(DemuxFilter filter) {
            this.mFilter = filter;
        }

        public long getFilterBufferSize() {
            return mFilterBufferSize;
        }

        public void setFilterBufferSize(long mFilterBufferSize) {
            this.mFilterBufferSize = mFilterBufferSize;
        }
    }

    private Executor getExecutor() {
        return Runnable::run;
    }

    private void startFilter(DemuxChannel channel, Tuner tuner) {
        mDemuxChannel = channel;
        mDemuxChannel.getDemuxSectionCallback().setFlagReceived(1);
//        if(mDemuxChannel.getTableId() == 0x50){
//            //LogUtils.d("[Ethan] 1111111111111111");
//            mExecutorHandlerThread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_FOREGROUND);
//            mExecutorHandlerThread.start();
//            mExecutorHandlerThreadHandler = new Handler(mExecutorHandlerThread.getLooper());
//            mExecutor = new HandlerExecutor(mExecutorHandlerThreadHandler);
//        }
//        else{
            mExecutor = getExecutor();
//        }
        LogUtils.d("[Ethan] openFilter table id = "+mDemuxChannel.getTableId()+" Filter buffer size = "+mDemuxChannel.getFilterBufferSize());
        if(mFilter == null) {
            mFilter = tuner.openFilter(
                    Filter.TYPE_TS,
                    Filter.SUBTYPE_SECTION,
                    mDemuxChannel.getFilterBufferSize(),
                    mExecutor,
                    mDemuxChannel.getDemuxSectionCallback().getFilterCallback());
        }
        else{
            mFilter.stop();
        }
        if(mFilter == null){
            LogUtils.e("Fail to open filter table id ="+mDemuxChannel.getTableId());
            abort();
            return;
        }

        int filterLength = mDemuxChannel.getFilter().getFilterData().length;
        byte[] filterMode = new byte[filterLength];
        for (int i = 0; i < filterLength; i++) {
            filterMode[i] = 0;
        }
        Settings settings = SectionSettingsWithSectionBits
                .builder(Filter.TYPE_TS)
                .setFilter(mDemuxChannel.getFilter().getFilterData())
                .setMask(mDemuxChannel.getFilter().getFilterMask())
                .setMode(filterMode)
//                .setVersion(1)
                .setCrcEnabled(mDemuxChannel.getCrcEnable())
                .setRaw(false)
                .setRepeat(mDemuxChannel.getRepeat())
                .build();
        FilterConfiguration config = TsFilterConfiguration
                .builder()
                .setTpid(mDemuxChannel.getPid())
                .setSettings(settings)
                .build();
        try {
            mFilter.configure(config);
            mFilter.start();
            LogUtils.d("openFilter filterid = "+mFilter.getId());
        }
        catch (Exception e){
            mFilter = null;
            abort();
            LogUtils.d("IllegalStateException "+ e);
        }
    }

    public void getTable(DemuxChannel channel, int tunerId) {
        if(mTunerInterface == null){
            mTunerInterface = TunerInterface.getInstance();
            if(mTunerInterface == null){
                LogUtils.e("mTunerInterface is NULL !!!!!!!!!");
                return;
            }
        }
        Tuner tuner = mTunerInterface.getTuner(tunerId);
        int count = 0;
        if (mAbortFlag == 1) {
            return;
        }

        startFilter(channel, tuner);

        while (mDemuxChannel.getDemuxSectionCallback().getFlagReceived() == 1) {
            try {
                sleep(10);
                count++;
                //if(count%100==0)
                //  printf("count [%d]\n", count);
                if(count>((mDemuxChannel.getTimeOut()+10)/10)) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        abort();
/*        try {
            if (mFilter != null) {
                LogUtils.d("[Ethan] Close Filter table id = "+mDemuxChannel.getTableId()+" filterid = "+mFilter.getId());
                mFilter.stop();
                mFilter.close(); // close after complete or timeout
                mFilter = null;
            }
        }
        catch (Exception e){
            mFilter = null;
            LogUtils.e("Exception !!!!!!");
            abort();
        }*/
    }

    public void monitorTable(DemuxChannel channel, int tunerId) {
        if(mTunerInterface == null){
            mTunerInterface = TunerInterface.getInstance();
            if(mTunerInterface == null){
                LogUtils.e("mTunerInterface is NULL !!!!!!!!!");
                return;
            }
        }
        Tuner tuner = mTunerInterface.getTuner(tunerId);
        if (mAbortFlag == 1) {
            return;
        }

        startFilter(channel, tuner);

        while(mAbortFlag != 1) {
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        abort();
    }

    public void abort() {
        try {
            if (mFilter != null && isClosing == false) {
                isClosing = true;
                LogUtils.d("[Ethan] Close Filter table id = "+mDemuxChannel.getTableId()+" filterid = "+mFilter.getId());
                mFilter.stop();
                mFilter.close();
                mFilter = null;
                isClosing = false;
            }
        }
        catch (Exception e){
            mFilter = null;
            LogUtils.e("StateException !!!!!!");
        }
        if (mExecutorHandlerThread != null) {
            mExecutorHandlerThread.quitSafely();
            mExecutorHandlerThread = null;
        }
        if (mExecutorHandlerThreadHandler != null) {
            mExecutorHandlerThreadHandler.removeCallbacksAndMessages(null);
            mExecutorHandlerThreadHandler = null;
        }
        mExecutor = null;
    }

    public int getAbortFlag() {
        return mAbortFlag;
    }

    public void setAbortFlag(int abort) {
        mAbortFlag = abort;
        if (mDemuxChannel != null) {
            mDemuxChannel.mDemuxSectionCallback.setAbortFlag(abort);
        }
        abort();
    }
}
