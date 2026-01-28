package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;

import java.util.ArrayList;
import java.util.List;

public class Nit extends Table {
    private static final String TAG = "Nit";

    public Nit(int tunerID, boolean isActual) {
        super(tunerID, isActual ? NIT_ACTUAL_NETWORK_TABLE_ID : NIT_OTHER_NETWORK_TABLE_ID);
        this.run(this.getClass().getName());
    }

    public NitData getNitData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            return (NitData) getData().get(0);
        }
    }

    @Override
    protected List<Demux.DemuxChannel> getDemuxChannels() {
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();

        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        demuxFilter.setFilterData(new byte[] {tableId});
        demuxFilter.setFilterMask(new byte[] {(byte)0xff});
        Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
        demuxChannel.setPid(NIT_PID);
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

    @Override
    protected TableData parsing(byte[] data, int lens) {
        NitData nitData = getNitData(); // use old data to parse for multi section

        if (nitData == null) {
            nitData = new NitData();
        }

        nitData.parsing(data, lens);

        return nitData;
    }

    @Override
    protected void finish_table() {

    }
}
