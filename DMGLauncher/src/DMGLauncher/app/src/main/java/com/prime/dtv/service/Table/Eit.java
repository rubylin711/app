package com.prime.dtv.service.Table;

import android.graphics.Bitmap;
import android.media.tv.tuner.Tuner;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.os.Process;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;
import com.prime.dtv.service.Util.Utils;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.subtitle.SubtitleManager;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vendor.prime.hardware.dtvservice.V1_0.HIDL_BUFFER_S;

public class Eit extends Table {
    private static final String TAG = "Eit";
    private static final boolean mUseParser=false;
    private final EitData.EitListener mListener;
    private final PrimeDtvMediaPlayer mPrimeDtvMediaPlayer=PrimeDtvMediaPlayer.get_instance();
    private HandlerThread mHandlerThread = null;
    private Handler mHandlerThreadHandler = null;
    private static final long EIT_SCHEDULE_FILTER_BUFFER_SIZE = 1*1024*1024;
    private static final long EIT_PF_FILTER_BUFFER_SIZE = 32*1024;
    private int mSepareEIT = 0;
    private int count=0;
    private int count2=0;
    //private byte[] data_tmp = new byte[1024*400];
    public Eit(int tunerID, byte eitTableId, EitData.EitListener eitListener, int separeEIT) {
        super(tunerID, eitTableId);
        mListener = eitListener;
        mSepareEIT = separeEIT;
        //Random random = new Random();
        //random.nextBytes(data_tmp);

        mHandlerThread = new HandlerThread(getClass().getName()/*,Process.THREAD_PRIORITY_AUDIO*/);
//        if(eitTableId == EIT_SCHEDULING_MIN_TABLE_ID)
//            mHandlerThread.setPriority(Thread.MAX_PRIORITY);
        mHandlerThread.start();
        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //Bundle bundle = msg.getData();
                int lens = msg.arg1;//bundle.getInt("RawDataLength");
                byte[] data = (byte[]) msg.obj;//bundle.getByteArray("RawDataLengthData");
                //byte[] data_1 = new byte[lens];
                //System.arraycopy(data, 0, data_1, 0, lens);
                /*
                if(count2 == 100) {
                    //Log.e(TAG,"processData , lens = " + lens);
                    count2=0;
                    //processData(data,lens);

                    //byte[] data_tmp = new byte[1024*4];
                   // Random random = new Random();
                    //random.nextBytes(data_tmp);
                    //processData(data_tmp,lens);
                }
                else{
                    count2++;
                }
                */
                //LogUtils.d("[Ethan] processData");
                processData(data,lens);
            }
        };
        this.run(this.getClass().getName());
    }

    private void processData(byte[] data, int lens) {
        mPrimeDtvMediaPlayer.send_eit_rowdata(data,lens);
    }

    public EitData getEitData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            return (EitData) getData().get(0);
        }
    }

    @Override
    protected List<Demux.DemuxChannel> getDemuxChannels() {
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();

        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        demuxFilter.setFilterData(new byte[] {tableId});
        demuxFilter.setFilterMask(new byte[]{(byte) 0xFE});
        Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
        demuxChannel.setPid(EIT_PID);
        demuxChannel.setTableId(tableId);
//        if(tableId == EIT_SCHEDULING_MIN_TABLE_ID)
//            demuxChannel.setCrcEnable(false);
//        else
            demuxChannel.setCrcEnable(true);
        demuxChannel.setRepeat(true);
        demuxChannel.setFilter(demuxFilter);
        if(tableId == EIT_SCHEDULING_MIN_TABLE_ID)
            demuxChannel.setFilterBufferSize(EIT_SCHEDULE_FILTER_BUFFER_SIZE);
        else
            demuxChannel.setFilterBufferSize(EIT_PF_FILTER_BUFFER_SIZE);
        demuxChannel.setTimeOut(0); // based on pesi service // set 0 to monitor table

        demuxChannel.setDemuxSectionCallback(
                new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

        demuxChannels.add(demuxChannel);

        return demuxChannels;
    }

    @Override
    protected TableData parsing(byte[] data, int lens) {

        if(mUseParser == true) {
            EitData eitData = getEitData();
            if (eitData == null) {
                eitData = new EitData(mListener);
            }
            eitData.parsing(data, lens);
            return eitData;
        }
        else{
            /*
            if(count == 100) {
                int tableId = Utils.getInt(data, 0, 1, Utils.MASK_8BITS);
                int serviceId = Utils.getInt(data, 3, 2, Utils.MASK_16BITS);
                //Log.d(TAG, "tableId = " + tableId + " serviceId = " + serviceId + " lens = " + lens);
            }
            */
            int tableId = Utils.getInt(data, 0, 1, Utils.MASK_8BITS);
            int serviceId = Utils.getInt(data, 3, 2, Utils.MASK_16BITS);
            if(!isEitTableId(data[0])){
                LogUtils.e("[Ethan] Not EIT table table id "+data[0]);
                return null;
            }
            //Log.d(TAG, "[Ethan] tableId = " + tableId + " serviceId = " + serviceId + " lens = " + lens);
            Message message = new Message();
            message.arg1 = lens ;
            message.obj = data ;

            mHandlerThreadHandler.sendMessage(message);
            //processData(data, lens);
            return null;
        }
    }

    @Override
    protected void finish_table() {

    }

    public static boolean isEitTableId(byte tableId) {
        return tableId == Table.EIT_PRESENT_FOLLOWING_TABLE_ID
                || tableId == Table.EIT_OTHER_PRESENT_FOLLOWING_TABLE_ID
                || (tableId >= Table.EIT_SCHEDULING_MIN_TABLE_ID && tableId <= Table.EIT_SCHEDULING_MAX_TABLE_ID)
                || (tableId >= Table.EIT_OTHER_SCHEDULING_MIN_TABLE_ID && tableId <= Table.EIT_OTHER_SCHEDULING_MAX_TABLE_ID);
    }

    public void cleanup() {
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;

            // remove all pending callback and msg
            mHandlerThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThreadHandler = null;
        }
    }
}
