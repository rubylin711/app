package com.prime.launcher.teletextservice.decoder;

import android.content.Context;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.PesEvent;
import android.media.tv.tuner.filter.PesSettings;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.util.Log;

import com.prime.dtv.service.Tuner.TunerInterface;

import java.util.concurrent.Executor;
public class TeletextManager {
    private static final String TAG = "TeletextManager";

    private static TeletextManager sInstance;
    private Tuner mTuner;
    private Filter mTeletextFilter;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private Executor mExecutor;
    private final Object lock = new Object();
    private boolean isRunning = false; // 旗標
    private int data_in = 0;
    private volatile boolean isFilterValid = false; // ⭐️ 防止 Filter close race
    private Context mContext;
    private TeletextDecoder mDecoder;

    public static TeletextManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TeletextManager(context);
        }
        return sInstance;
    }

    private TeletextManager(Context context) {
        mContext = context;
        mHandlerThread = new HandlerThread("TeletextDecoderThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mExecutor = new HandlerExecutor(mHandler);
        mDecoder = new TeletextDecoder(context, mHandler);
    }
    
    
    public void startTeletext(int tunerId, int pid) {
        synchronized (lock) {

            if (isRunning || mTeletextFilter != null) {
                Log.w(TAG, "Teletext already started");
                return;
            }

            mTuner = TunerInterface.getInstance(mContext).getTuner(tunerId);
            if (mTuner == null) {
                Log.e(TAG, "Tuner not available");
                return;
            }

            mTeletextFilter = mTuner.openFilter(
                    Filter.TYPE_TS,
                    Filter.SUBTYPE_PES,
                    1024 * 512,
                    mExecutor,
                    new FilterCallback() {
                        @Override
                        public void onFilterEvent(Filter filter, FilterEvent[] events) {
                            if (!isRunning || !isFilterValid) {
                                Log.w(TAG, "⚠️ FilterEvent received but already stopped");
                                return;
                            }
                            for (FilterEvent event : events) {
                                if (event instanceof PesEvent) {
                                    PesEvent pesEvent = (PesEvent) event;
                                    int length = pesEvent.getDataLength();
                                    if (length <= 0) return;

                                    byte[] buffer = new byte[length];
                                    try {
                                        int read = filter.read(buffer, 0, length);
                                        if (read > 0) {
                                            data_in++;
                                            if (data_in>0&& data_in <4)
                                                Log.w(TAG, "data in : "+data_in+"len:" + length);
                                            mDecoder.feedTeletextPes(buffer);
                                        }
                                    } catch (IllegalStateException e) {
                                        Log.w(TAG, "⚠️ Filter already closed when reading: " + e.getMessage());
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFilterStatusChanged(Filter filter, int status) {
                            // Optional: handle filter status change
                        }
                    });

            PesSettings pesSettings = PesSettings.builder(Filter.TYPE_TS).build();
            TsFilterConfiguration config = TsFilterConfiguration.builder()
                    .setSettings(pesSettings)
                    .setTpid(pid)
                    .build();

            mTeletextFilter.configure(config);
            isRunning = true;
            isFilterValid = true;
            mDecoder.start();
            mTeletextFilter.start();
            Log.i(TAG, "Teletext started on pid " + pid);
        }
    }

    public void stopTeletext() {
        if (!isRunning) return;

        isRunning = false;
        isFilterValid = false; // ⭐️ 這樣 onFilterEvent 會自動 skip
        if (mTeletextFilter != null) {
            mTeletextFilter.stop();
            mTeletextFilter.close();
            mTeletextFilter = null;
        }

        if (mDecoder != null) {
            mDecoder.stop();
        }
        data_in=0;
        Log.i(TAG, "Teletext stopped");
    }

    public boolean isRunning() {
        return isRunning;
    }
}
