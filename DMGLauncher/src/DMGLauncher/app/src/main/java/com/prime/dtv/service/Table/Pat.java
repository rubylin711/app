package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;

import java.util.ArrayList;
import java.util.List;


public class Pat extends Table {
    private static final String TAG = "Pat";

    public Pat(int tunerID) {
        super(tunerID, PAT_TABLE_ID);
        this.run(this.getClass().getName());
    }

    public PatData getPatData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            return (PatData) getData().get(0);
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
        demuxChannel.setPid(PAT_PID);
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
        PatData patData = getPatData(); // use old data to parse for multi section

        if (patData == null) {
            patData = new PatData();
        }

        patData.parsing(data, lens);

        return patData;
    }

    @Override
    protected void finish_table() {

    }
}

