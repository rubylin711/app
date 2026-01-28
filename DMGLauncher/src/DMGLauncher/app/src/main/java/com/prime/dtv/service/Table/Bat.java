package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;
import android.util.Log;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;

import java.util.ArrayList;
import java.util.List;

public class Bat extends Table {
    private static final String TAG = "Bat";
    private boolean mBatSearchMode;
    private int mBouquetId;

    public Bat(int tunerID, int bouquetId,boolean BatSearchMode) {
        //super(tuner, tunerId, BAT_TABLE_ID,BatSearchMode);
        super(tunerID, BAT_TABLE_ID);
        //Log.d(TAG,"Creat Bat object, BatSearchMode = "+BatSearchMode+" BouquetId = "+bouquetId);
        mBatSearchMode=BatSearchMode;
        mBouquetId=bouquetId;
        this.run(this.getClass().getName());
    }
    public BatData getBatData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            return (BatData) getData().get(0);
        }
    }

    protected List<Demux.DemuxChannel> getDemuxChannels() {
        int MutiSize,i;
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();
        DemuxSectionCallback dmxCallback;
        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        if(mBatSearchMode == true) {
            //Log.d(TAG,"mBatSearchMode == true,  tableId = "+Integer.toString(tableId,16));
            byte[] filterData = new byte[2];
            filterData[0] = (byte) ((mBouquetId & 0xFF00) >> 8);
            filterData[1] = (byte) (mBouquetId & 0x00FF);
            demuxFilter.setFilterData(new byte[]{tableId, filterData[0], filterData[1]});
            demuxFilter.setFilterMask(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        }
        else {
            //Log.d(TAG,"mBatSearchMode == false,  tableId = "+Integer.toString(tableId,16));
            byte[] filterData = new byte[2];
            filterData[0] = (byte) ((mBouquetId & 0xFF00) >> 8);
            filterData[1] = (byte) (mBouquetId & 0x00FF);
            demuxFilter.setFilterData(new byte[]{tableId, filterData[0], filterData[1]});
            demuxFilter.setFilterMask(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        }
        Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
        demuxChannel.setPid(BAT_PID);
        demuxChannel.setTableId(tableId);
        demuxChannel.setCrcEnable(true);
        demuxChannel.setRepeat(true);
        demuxChannel.setFilter(demuxFilter);
        demuxChannel.setTimeOut(10000);

        demuxChannel.setDemuxSectionCallback(
                new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

        demuxChannels.add(demuxChannel);
        dmxCallback=demuxChannel.getDemuxSectionCallback();

        return demuxChannels;
    }

    protected TableData parsing(byte[] data, int lens) {
        //Log.d(TAG,"Bat parsing()");
        BatData batData = getBatData(); // use old data to parse for multi section

        if (batData == null) {
            batData = new BatData();
        }

        batData.parsing(data, lens);

        return batData;
    }

    @Override
    protected void finish_table() {

    }

}
