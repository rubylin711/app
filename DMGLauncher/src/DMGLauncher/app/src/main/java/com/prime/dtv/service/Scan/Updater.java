package com.prime.dtv.service.Scan;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.prime.dtv.service.Table.Table;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.utils.LogUtils;

abstract public class Updater {
    private long mChannelId;
    private int mTunerId = 0;
    private boolean mStop = false;
    protected DataManager mDataManager;
    protected TunerInterface mTuneInterface;
    protected Context mContext;
    protected static Scan_utils mScanUtils;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private static final int SLEEP_UPDATE_PROCESS = 0;
    private static final int START_UPDATE_PROCESS = 1;
    private static final int STOP_UPDATE_PROCESS = 2;
    public Updater(Context context, long channel_id){
        LogUtils.d("NEW Updater");
        mContext = context;
        mChannelId = channel_id;
        mTuneInterface = TunerInterface.getInstance(context);
        mDataManager = DataManager.getDataManager(context);
        mScanUtils = new Scan_utils(context);
        mHandlerThread = new HandlerThread(getClass().getName());
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                LogUtils.d(" handleMessage => "+msg.what);
                switch (msg.what){
                    case SLEEP_UPDATE_PROCESS:{

                    }break;
                    case START_UPDATE_PROCESS:{
                        proecee(mChannelId);
                    }break;
                    case STOP_UPDATE_PROCESS:{

                    }break;
                }
            }
        };

    }

    public long getChannelId(){
        return mChannelId;
    }
    public void setChannelId(long channelId){
        mChannelId = channelId;
    }
/*    protected void run() {
        UpdaterRunnable runnable = new UpdaterRunnable(mChannelId);
        newThread(runnable);
    }

    private void newThread(UpdaterRunnable runnable) {
        new Thread(runnable).start();
    }*/

    public boolean IsStop() {
        return mStop;
    }

/*    private class UpdaterRunnable implements Runnable{
        private long mChannelId;
        public UpdaterRunnable(long channel_id) {
            mChannelId = channel_id;
        }

        @Override
        public void run() {
            proecee(mChannelId);
        }
    }*/
    void start(){
        mStop = false;
        Message msg = mHandler.obtainMessage();
        msg.what = START_UPDATE_PROCESS;
        mHandler.sendMessage(msg);
        //run();
    }
    void stop(){
        mStop = true;
    }
    protected abstract void proecee(long channel_id);

    public int getTunerId() {
        return mTunerId;
    }

    public void setTunerId(int mTunerId) {
        this.mTunerId = mTunerId;
    }
}
