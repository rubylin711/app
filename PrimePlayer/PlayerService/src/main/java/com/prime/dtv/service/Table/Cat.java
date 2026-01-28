package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;

import java.util.ArrayList;
import java.util.List;


public class Cat extends Table {
    private static final String TAG = "Cat";


    public Cat(int tunerID) {
        super(tunerID, CAT_TABLE_ID);
        this.run(this.getClass().getName());
    }
    public CatData getCatData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            return (CatData) getData().get(0);
        }
    }

    protected List<Demux.DemuxChannel> getDemuxChannels() {
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();

        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        demuxFilter.setFilterData(new byte[] {tableId});
        demuxFilter.setFilterMask(new byte[] {(byte)0xFF});
        Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
        demuxChannel.setPid(CAT_PID);
        demuxChannel.setTableId(tableId);
        demuxChannel.setCrcEnable(true);
        demuxChannel.setRepeat(true);
        demuxChannel.setFilter(demuxFilter);
        demuxChannel.setTimeOut(10000);

        demuxChannel.setDemuxSectionCallback(
                new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

        demuxChannels.add(demuxChannel);

        return demuxChannels;
    }

    protected TableData parsing(byte[] data, int lens) {
        //Log.d(TAG,"Bat parsing()");
        CatData catData = getCatData(); // use old data to parse for multi section

        if (catData == null) {
            catData = new CatData();
        }

        catData.parsing(data, lens);

        return catData;
    }

    @Override
    protected void finish_table() {

    }

}
