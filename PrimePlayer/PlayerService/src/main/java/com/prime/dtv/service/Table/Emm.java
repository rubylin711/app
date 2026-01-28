package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;
import android.util.Log;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;

import java.util.ArrayList;
import java.util.List;

public class Emm extends Table{
    private static final String TAG = "Emm";
    private final int mEmmPid;

    public Emm(int tunerID,int emmPid) {

        super(tunerID, EMM_EMERGENCY_TABLE_ID);
        mEmmPid=emmPid;
        //Log.d(TAG, "BBBBB this.run(), emmPid  = "+emmPid);
        this.run(this.getClass().getName());

    }
    public PrivateSectionData getEmmData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            return (PrivateSectionData) getData().get(0);
        }
    }

    public void cleanEmmData(){
        if(getData() != null){
            getData().clear();
        }
    }

    protected List<Demux.DemuxChannel> getDemuxChannels() {
        byte tableId = getTableId();
        long temp=(long)tableId;
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();
        //Log.d(TAG, "tableId = "+temp);
        //Log.d(TAG, "EmmPid = "+mEmmPid);
        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        demuxFilter.setFilterData(new byte[] {tableId,0,0,1});
        demuxFilter.setFilterMask(new byte[] {(byte)0xFE,(byte)0,(byte)0,(byte)0xFF});
        Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
        demuxChannel.setPid(mEmmPid);
        demuxChannel.setTableId(tableId);
        demuxChannel.setCrcEnable(true);
        demuxChannel.setRepeat(true);
        demuxChannel.setFilter(demuxFilter);
        //demuxChannel.setTimeOut(10000);
        demuxChannel.setTimeOut(0);

        demuxChannel.setDemuxSectionCallback(
                new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

        demuxChannels.add(demuxChannel);

        return demuxChannels;
    }

    protected TableData parsing(byte[] data, int lens) {
        //Log.d(TAG,"Bat parsing()");
        PrivateSectionData privateSectionData = getEmmData(); // use old data to parse for multi section
        try {
            if (privateSectionData == null) {
                //Log.d(TAG, "BBBBB new PrivateSectionData() = ");
                privateSectionData = new PrivateSectionData();
            }
            //Log.d(TAG, "BBBBB privateSectionData.parsing");
            privateSectionData.parsing(data, lens);
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }

        return privateSectionData;
    }

    @Override
    protected void finish_table() {

    }

}
