package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;

import java.util.ArrayList;
import java.util.List;

public class Tdt extends Table {
    private static final String TAG = "Tdt";

    public Tdt(int tunerID) {
        super(tunerID, TDT_TABLE_ID);
        this.run(this.getClass().getName());
    }

    public TdtData getTdtData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            return (TdtData) getData().get(0);
        }
    }

    @Override
    protected List<Demux.DemuxChannel> getDemuxChannels() {
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();

        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        demuxFilter.setFilterData(new byte[] {tableId});
        demuxFilter.setFilterMask(new byte[] {(byte)0xFF});
        Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
        demuxChannel.setPid(TDT_TOT_PID);
        demuxChannel.setTableId(tableId);
        demuxChannel.setCrcEnable(false); // tdt no crc
        demuxChannel.setRepeat(false); // based on pesi service
        demuxChannel.setFilter(demuxFilter);
        demuxChannel.setTimeOut(30000); // based on pesi service

        demuxChannel.setDemuxSectionCallback(
                new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

        demuxChannels.add(demuxChannel);

        return demuxChannels;
    }

    @Override
    protected TableData parsing(byte[] data, int lens) {
        TdtData tdtData = new TdtData();
        tdtData.parsing(data, lens);

        return tdtData;
    }

    @Override
    protected void finish_table() {

    }
}
