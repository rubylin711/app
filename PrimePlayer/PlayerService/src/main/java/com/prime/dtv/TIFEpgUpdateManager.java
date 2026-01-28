package com.prime.dtv;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.tv.TvContract;
import android.util.Log;

import com.prime.datastructure.TIF.TIFEpgData;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.dtv.service.Table.PatData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TIFEpgUpdateManager {

    public static final String TAG = "TIFEpgUpdateManager";
    public int DAY_OF_EPG   =   7;
    private Context mContext;
    private BlockingQueue<Long> mChannelQueuePF,mChannelQueueSCH;
    private ExecutorService mExecutorServicePF,mExecutorServiceSCH;
    private volatile boolean isRunning;
    private PrimeDtv mPrimeDtv = null;


    public TIFEpgUpdateManager(Context context,PrimeDtv primeDtv) {
        mContext = context.getApplicationContext();
        mChannelQueuePF = new LinkedBlockingQueue<>();
        mChannelQueueSCH = new LinkedBlockingQueue<>();
        mExecutorServicePF = Executors.newSingleThreadExecutor();
        //mExecutorServiceSCH = Executors.newFixedThreadPool(2); // 改為 2 執行緒
        mExecutorServiceSCH = Executors.newSingleThreadExecutor(); //tifcheck one thread
        isRunning = true;
        mPrimeDtv = primeDtv;
        startQueueProcessorPF();
        startQueueProcessorSCH();
    }

    private void startQueueProcessorPF() {
        mExecutorServicePF.submit(() -> {
            while (isRunning) {
                try {
                    Long channelId = mChannelQueuePF.take(); // 阻塞等待
                    updateEpgForChannelPF(channelId);
//                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Queue processor interrupted", e);
                    Thread.currentThread().interrupt();
                }
                catch (Exception e) {
                    Log.e(TAG, "startQueueProcessorPF Unexpected error in queue processor", e);
                    // 繼續迴圈，避免執行緒終止
                }
            }
        });
    }

    // 啟動佇列處理執行緒（2 個）
    private void startQueueProcessorSCH() {
        //for (int i = 0; i < 2; i++) { // 啟動 2 個消費者執行緒
            mExecutorServiceSCH.submit(() -> {
                while (isRunning) {
                    try {
                        Long channelId = mChannelQueueSCH.take(); // 阻塞等待
                        updateEpgForChannelSCH(channelId);
//                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Queue processor interrupted", e);
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        Log.e(TAG, "startQueueProcessorSCH Unexpected error in queue processor", e);
                        // 繼續迴圈，避免執行緒終止
                    }
                }
            });
        //}
        
    }

    public void onTIFEpgUpdate(long channelId, boolean isPF) {
        try {
//            Log.d(TAG, "Received callback for channelId: " + channelId);
            if(isPF)
                mChannelQueuePF.put(channelId);
            else
                mChannelQueueSCH.put(channelId); // 放入佇列
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to queue channelId: " + channelId, e);
            Thread.currentThread().interrupt();
        }
    }

    private void updateEpgForChannelPF(long channelId) {
//        Log.d(TAG, "updateEpgForChannelPF EPG update for channelId: " + channelId);
        ProgramInfo programInfo = mPrimeDtv.get_program_by_channel_id(channelId);
        List<EPGEvent> events = new ArrayList<>();
        EPGEvent present = mPrimeDtv.get_present_event(channelId);

        if(present != null) { //tifcheck
            //Log.d(TAG, "present " + present.get_event_id());
//            present.set_short_event(mPrimeDtv.get_short_description(channelId, present.get_event_id()));
            present.set_extended_event(mPrimeDtv.get_detail_description(channelId, present.get_event_id()));
            events.add(present);
        }
        EPGEvent follow = mPrimeDtv.get_follow_event(channelId);

        if(follow != null) { 
            //Log.d(TAG, "follow " + follow.get_event_id());
//            follow.set_short_event(mPrimeDtv.get_short_description(channelId, follow.get_event_id()));
            follow.set_extended_event(mPrimeDtv.get_detail_description(channelId, follow.get_event_id()));
            events.add(follow);
        }
        TIFEpgData.insertProgramData(mContext,programInfo, events);
//        Log.d(TAG, "updateEpgForChannelPF EPG update for channelId: " + channelId + " done");
    }

    public Date get_date(long ms) {
        return new Date(ms);
    }

    private void updateEpgForChannelSCH(long channelId) {
//        Log.d(TAG, "updateEpgForChannelSCH EPG update for channelId: " + channelId);
        ProgramInfo programInfo = mPrimeDtv.get_program_by_channel_id(channelId);
        List<EPGEvent> events;
        Date startDate = get_date(System.currentTimeMillis());
        Date endDate = get_date(System.currentTimeMillis()+DAY_OF_EPG*24*60*60*1000);
        events = mPrimeDtv.get_epg_events(channelId, startDate, endDate, 0, 1000, 0);
        for (EPGEvent epgEvent : events) {
//            epgEvent.set_short_event(mPrimeDtv.get_short_description(channelId, epgEvent.get_event_id()));
            epgEvent.set_extended_event(mPrimeDtv.get_detail_description(channelId, epgEvent.get_event_id()));
        }
        //Log.d(TAG, "SCH eventd: " + events.size()); //tifcheck
        TIFEpgData.insertProgramData(mContext,programInfo, events);
    }

    // 清理資源
    public void shutdown() {
        isRunning = false;
        mExecutorServicePF.shutdown();
        mExecutorServiceSCH.shutdown();
        try {
            if (!mExecutorServicePF.awaitTermination(5, TimeUnit.SECONDS)) {
                mExecutorServicePF.shutdownNow();
            }
            if (!mExecutorServiceSCH.awaitTermination(5, TimeUnit.SECONDS)) {
                mExecutorServiceSCH.shutdownNow();
            }
        } catch (InterruptedException e) {
            mExecutorServicePF.shutdownNow();
            mExecutorServiceSCH.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}