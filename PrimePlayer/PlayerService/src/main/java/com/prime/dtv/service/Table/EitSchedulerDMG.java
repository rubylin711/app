package com.prime.dtv.service.Table;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;
import com.prime.dtv.service.Util.Utils;

import java.util.ArrayList;
import java.util.List;

public class EitSchedulerDMG extends Table{
    private static final String TAG = "EitSchedulerDMG";

    private final PrimeDtvMediaPlayer mPrimeDtvMediaPlayer=PrimeDtvMediaPlayer.get_instance();
    private HandlerThread mHandlerThread = null;
    private Handler mHandlerThreadHandler = null;
    private static final long EIT_SCHEDULE_FILTER_BUFFER_SIZE = 1*1024*1024;
    private int mServiceID;
    private List<Integer> mServiceIDs = new ArrayList<>();
    private int mTimeoutMS = 3000;
    private byte mFiterData;
    private byte mFiterMask;
    private boolean mStop = false;

    public EitSchedulerDMG(int tunerID, byte tableId, List<Integer> serviceIds, int timeout) {
        super(tunerID, tableId);
        mStop = false;
        mServiceIDs.addAll(serviceIds);
        if(timeout>0)
            mTimeoutMS = timeout;
        mHandlerThread = new HandlerThread(getClass().getName());

        mHandlerThread.start();
        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {

                int lens = msg.arg1;
                byte[] data = (byte[]) msg.obj;

                processData(data,lens);
            }
        };
        this.run("EitSchedulerDMG");
    }
    public EitSchedulerDMG(int tunerID, byte tableId, int serviceId, int timeout,byte data, byte mask) {
        super(tunerID, tableId);
        mServiceID = serviceId;
        mFiterData = data;
        mFiterMask = mask;
        if(timeout>0)
            mTimeoutMS = timeout;
        mHandlerThread = new HandlerThread(getClass().getName());

        mHandlerThread.start();
        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {

                int lens = msg.arg1;
                byte[] data = (byte[]) msg.obj;

                processData(data,lens);
            }
        };
        this.run(this.getClass().getName());
    }
    @Override
    protected void run(String name){
        EitSchedulerDMGRunable r = new EitSchedulerDMGRunable();
        new Thread(r, name).start();
    }
    @Override
    protected List<Demux.DemuxChannel> getDemuxChannels() {
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();

        for(int i=0; i<mServiceIDs.size() ; i++){
            int serviceID = mServiceIDs.get(i);
            Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
            byte[] filter_data = new byte[]{tableId, 0, 0, (byte)((serviceID&0xFF00)>>8), (byte)(serviceID&0xFF)};
            byte[] filter_mask = new byte[]{(byte) 0xCE, 0, 0, (byte) 0xFF, (byte) 0xFF};

            demuxFilter.setFilterData(filter_data);
            demuxFilter.setFilterMask(filter_mask);

            Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
            demuxChannel.setPid(EIT_PID);
            demuxChannel.setTableId(tableId);

        demuxChannel.setCrcEnable(true);
        demuxChannel.setRepeat(true);
        demuxChannel.setFilter(demuxFilter);
        //demuxChannel.setFilterBufferSize(EIT_SCHEDULE_FILTER_BUFFER_SIZE);
        demuxChannel.setTimeOut(mTimeoutMS); // based on pesi service // set 0 to monitor table

            demuxChannel.setDemuxSectionCallback(
                    new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

            demuxChannels.add(demuxChannel);
        }

        return demuxChannels;
    }


    @Override
    protected TableData parsing(byte[] data, int lens) {
        int tableId = Utils.getInt(data, 0, 1, Utils.MASK_8BITS);
        int serviceId = Utils.getInt(data, 3, 2, Utils.MASK_16BITS);
        int section_number = Utils.getInt(data, 6, 1, Utils.MASK_8BITS);
        int last_section_number = Utils.getInt(data, 7, 1, Utils.MASK_8BITS);
        if(!isEitTableId(data[0])){
            LogUtils.e("[Ethan] Not EIT table table id "+data[0]);
            return null;
        }
        //LogUtils.d("[Ethan] tableId = " + tableId + " serviceId = " + serviceId + " section_number = "+section_number+ " last_section_number = "+last_section_number+" lens = " + lens);
        Message message = new Message();
        message.arg1 = lens ;
        message.obj = data ;

        mHandlerThreadHandler.sendMessage(message);
        //processData(data, lens);
        return null;
    }

    @Override
    protected void finish_table() {
        LogUtils.d("mServiceID = "+mServiceID);
        //abort();
        //cleanup();
    }

    public static boolean isEitTableId(byte tableId) {
        return tableId == Table.EIT_PRESENT_FOLLOWING_TABLE_ID
                || tableId == Table.EIT_OTHER_PRESENT_FOLLOWING_TABLE_ID
                || (tableId >= Table.EIT_SCHEDULING_MIN_TABLE_ID && tableId <= Table.EIT_SCHEDULING_MAX_TABLE_ID)
                || (tableId >= Table.EIT_OTHER_SCHEDULING_MIN_TABLE_ID && tableId <= Table.EIT_OTHER_SCHEDULING_MAX_TABLE_ID);
    }

    private void processData(byte[] data, int lens) {
        mPrimeDtvMediaPlayer.send_eit_rowdata(data,lens);
    }
    public void cleanup() {
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;

            // remove all pending callback and msg
            mHandlerThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThreadHandler = null;
        }
        mStop = true;
    }

    protected class EitSchedulerDMGRunable implements Runnable{

        @Override
        public void run() {
            List<Demux.DemuxChannel> demuxChannels = getDemuxChannels();
            int i=0;
            while (!mStop){
                Demux.DemuxChannel demuxChannel = demuxChannels.get(i);
                process(demuxChannel);
                i++;
                if(i == demuxChannels.size())
                    i=0;
            }

        }
    }
}
