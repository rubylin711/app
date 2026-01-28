package com.prime.dtv.service.subtitle;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.PesEvent;
import android.media.tv.tuner.filter.PesSettings;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;

import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.subtitle.dvb.DvbSubtitleCodec;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVMessage;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubtitleManager {
    private static final String TAG = "SubtitleManager";
    private static SubtitleManager mSubtitlManager;
    private DataManager mDatamanager = null;

    private static final int MSG_SUBTITLE_BITMAP_INFO = 1;
    private static final int MSG_SUBTITLE_DATA_INFO = 2;
    private Handler mHandler;
    private Executor mExecutor;
    private HandlerThread mExecutorHandlerThread = null;
    private Handler mExecutorHandlerThreadHandler = null;
    private static final int THREAD_COUNT = 1;
    private static HandlerThread mHandlerThread = null;
    private Handler mHandlerThreadHandler = null;    
    private DvbSubtitleCodec mDvbSubtitleCodec;
    protected Filter mSubtitleFilter = null;
    private ArrayDeque<Integer> mInputBufferIndices;
    private InputBuffer mInputBuffer;
    private PesQueue pesQueue;
    private boolean isSubtitleSynced = false;
    private int subtitleSyncTimeUs = 0;
    private static TunerInterface mTuneInterface;
    private static boolean isStartSubtitle;
    private PesiDtvFrameworkInterfaceCallback mCallback = null;

    public SubtitleManager(Context context, PesiDtvFrameworkInterfaceCallback callback) {
        mTuneInterface = TunerInterface.getInstance(context);
        mDatamanager = DataManager.getDataManager(context);
        mCallback = callback;
        mExecutorHandlerThread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_AUDIO);
        mExecutorHandlerThread.start();
        mExecutorHandlerThreadHandler = new Handler(mExecutorHandlerThread.getLooper());
        mExecutor = new HandlerExecutor(mExecutorHandlerThreadHandler);

        mInputBufferIndices = new ArrayDeque<>();
        mInputBuffer = new InputBuffer();
        isStartSubtitle = false;
        mHandlerThread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_AUDIO);
        mHandlerThread.start();
        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //
                SubitleHandleMessage(msg);
            }
        };
    }

    public static SubtitleManager getSubtitleManager(Context context){
        return mSubtitlManager;
    }
    public static SubtitleManager getSubtitleManager(Context context,PesiDtvFrameworkInterfaceCallback callback) {
        if(mSubtitlManager == null){
            mSubtitlManager = new SubtitleManager(context, callback);
        }
        return mSubtitlManager;
    }
    public static boolean GetisStartSubtitle(){
        return isStartSubtitle;
    }
    private void SubitleHandleMessage(Message msg) {
        //LogUtils.d("SubitleHandleMessage msg = " + msg.what);
        switch(msg.what){
            case MSG_SUBTITLE_BITMAP_INFO:{
                mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP, 0, 0, (Bitmap) msg.obj);
            }break;
            case MSG_SUBTITLE_DATA_INFO:{
                pushPes(msg.getData().getByteArray("subtitleData"));
            }break;

        }
    }

    public void showSubtitle(Long presentationTimeUs){
        if(!mInputBufferIndices.isEmpty() && !pesQueue.isEmpty() &&
                pesQueue.getFirstTimestampUs() != -1 ){
            syncSubtitleTime(presentationTimeUs);
            if(pesQueue.getFirstTimestampUs() <= presentationTimeUs + subtitleSyncTimeUs){
                int index = mInputBufferIndices.pop();
                ByteBuffer inputBuffer;
                try {
                    inputBuffer = mDvbSubtitleCodec.getInputBuffer(index);
                }catch(IllegalStateException exception) {
                    return;
                }
                mInputBuffer.buffer = inputBuffer;
                pesQueue.pop(mInputBuffer);
                mDvbSubtitleCodec.queueInputBuffer(index, 0, inputBuffer.limit(), mInputBuffer.timestampUs, 0);
            }else {
                pesQueue.flushBefore(presentationTimeUs - subtitleSyncTimeUs);
            }

        }
    }
    public void syncSubtitleTime(Long presentationTimeUs){
        if(!isSubtitleSynced){
            int syncTime = (int) Math.abs(pesQueue.getFirstTimestampUs() - presentationTimeUs);
            if(syncTime <= 1000000) {
                isSubtitleSynced = true;
                subtitleSyncTimeUs = syncTime;
                Log.d(TAG, "subtitleSyncTimeUs = " +subtitleSyncTimeUs);
            }
        }
    }
    public void startSubtitle(int tunerId, int Pid, int compage_id, int anycpage_id){
        if(mDvbSubtitleCodec==null){
            startSubtitlePesFilter(tunerId, Pid);
            mDvbSubtitleCodec = new DvbSubtitleCodec();
            mDvbSubtitleCodec.setPageIds(compage_id, anycpage_id);
            mDvbSubtitleCodec.setCallback(mSubtitleCallback, mHandlerThreadHandler);
            mDvbSubtitleCodec.start();
        }
    }

    private void startSubtitlePesFilter(int tunerId, int pid) {
        Tuner mTuner = mTuneInterface.getTuner(tunerId);
        if(mTuner == null) {
            LogUtils.d("startSubtitlePesFilter Can't get mTuner:"+tunerId);
            return;
        }
        if(mSubtitleFilter != null)
            return;
        mSubtitleFilter = mTuner.openFilter(Filter.TYPE_TS, Filter.SUBTYPE_PES, 1024 * 1024, mExecutor, mFilterCallback);
        LogUtils.d("start SubtitleFilter pid:"+pid);
        PesSettings pesSettings = PesSettings
                .builder(Filter.TYPE_TS)
                .build();
        FilterConfiguration pesConfig = TsFilterConfiguration
                .builder()
                .setSettings(pesSettings)
                .setTpid(pid)
                .build();
        mSubtitleFilter.configure(pesConfig);
        mSubtitleFilter.start();
        pesQueue = new PesQueue();
        pesQueue.setDiscontinuityThreshold(5000000);
        isStartSubtitle = true;
        LogUtils.d("start end");
    }
    private void stopSubtitlePesFilter() {
        mHandlerThreadHandler.removeMessages(SubtitleManager.MSG_SUBTITLE_DATA_INFO);
        mHandlerThreadHandler.removeMessages(SubtitleManager.MSG_SUBTITLE_BITMAP_INFO);
        //if (mSubtitleFilter == null)
        //    return;
        LogUtils.d("stopPesFilter enter");
        isStartSubtitle = false;
        isSubtitleSynced = false;
        subtitleSyncTimeUs = 0;
        if(mSubtitleFilter != null) {
            mSubtitleFilter.stop();
            mSubtitleFilter.close();
            mSubtitleFilter = null;
        }

        if(mDvbSubtitleCodec != null){
            mDvbSubtitleCodec.stop();
            mDvbSubtitleCodec = null;
        }
        if(pesQueue != null){
            pesQueue.clear();
            pesQueue = null;
        }
        mInputBufferIndices.clear();
        LogUtils.d( "stopPesFilter end");
    }
    private SubtitleCodec.Callback mSubtitleCallback = new SubtitleCodec.Callback() {
        @Override
        public void onError(SubtitleCodec codec, SubtitleCodec.SubtitleException exception) {
            LogUtils.d("SubtitleCodec error = " + exception.getMessage());
        }

        @Override
        public void onInputBufferAvailable(SubtitleCodec codec, int index) {
            LogUtils.d("mInputBufferIndices index = " + index);
            mInputBufferIndices.add(index);
        }

        @Override
        public void onOutputBufferAvailable(SubtitleCodec codec, Bitmap bitmap, SubtitleCodec.BufferInfo info) {
            if(isStartSubtitle) {
                Message message = new Message();
                message.what = SubtitleManager.MSG_SUBTITLE_BITMAP_INFO;
                message.obj = bitmap;
                LogUtils.d("info.timeoutSec = " + info.timeoutSec);
                mHandlerThreadHandler.sendMessage(message);
            }
        }
    };

    protected final FilterCallback mFilterCallback = new SubtitlefilterCallback();

    public void stoptSubtitle() {
        if(isStartSubtitle)
            stopSubtitlePesFilter();
    }

    private class SubtitlefilterCallback implements FilterCallback {
        @Override
        public void onFilterEvent(Filter filter, FilterEvent[] events) {
            try {
                for (FilterEvent event : events) {
                    if (event instanceof PesEvent) {
                        //LogUtils.d("onFilterEvent is PesEvent");
                        PesEvent pesEvent = (PesEvent) event;
                        int dataLength = pesEvent.getDataLength();
                        if (dataLength <= 0 )
                            continue;
                        if(!isStartSubtitle) {
                            return;
                        }
                        byte[] ccBuffer = new byte[dataLength];
                        //LogUtils.d("dataLength = " + dataLength);
                        filter.read(ccBuffer, 0, dataLength);
                        //LogUtils.d("read Length= " + filter.read(ccBuffer, 0, dataLength));
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        if(isStartSubtitle){
                            message.what = SubtitleManager.MSG_SUBTITLE_DATA_INFO;
                            bundle.putByteArray("subtitleData",ccBuffer);
                        }
                        message.setData(bundle);
                        mHandlerThreadHandler.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                LogUtils.d("errors on handling filter: "+ e);
                //throw new RuntimeException(e);
            }
        }

        @Override
        public void onFilterStatusChanged(Filter filter, int status) {

        }
    }

    public void pushPes(byte[] subtitleBuffer){
        ByteBuffer byteBuffer = ByteBuffer.wrap(subtitleBuffer);
        Pes pes = new Pes();
        pes.setByteBuffer(byteBuffer);
        pes.parseHeader();
        if (pesQueue ==null)
            return;
        pesQueue.pushPes(pes);
    }
}
