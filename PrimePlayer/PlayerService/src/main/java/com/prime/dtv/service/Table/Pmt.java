package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;

import java.util.ArrayList;
import java.util.List;

public class Pmt extends Table {
    private static final String TAG = "Pmt";

    // program map pid in PAT is 13 bits, max = 0x1FFF
    private static final int MAX_PROGRAM_MAP_PID = 0x1FFF;
    // program number in PAT is 16 bits, max = 0xFFFF
    private static final int MAX_PROGRAM_NUMBER = 0xFFFF;

    private final PatData mPatData;
    private int mTunerType;

    public Pmt(int tunerID, PatData patData, int tunerType) {
        super(tunerID, PMT_TABLE_ID);
        mPatData = patData;
        mTunerType = tunerType;
        //LogUtils.d("New PMT patProgramSize = "+mPatData.getProgramTotalNum());
        this.run(this.getClass().getName());
    }

    public List<PmtData> getPmtDataList() {
        List<TableData> tableDataList = getData();
        List<PmtData> pmtDataList = new ArrayList<>();

        for (TableData tableData : tableDataList) {
            pmtDataList.add((PmtData) tableData);
        }

        return pmtDataList;
    }

    public int getPmtDataTotalNum() {
        if(getData() != null)
            return getData().size();
        else
            return 0;
    }

    @Override
    protected List<Demux.DemuxChannel> getDemuxChannels() {
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();

        int patProgramSize = mPatData == null ? 0 : mPatData.getProgramTotalNum();
        for (int i = 0 ; i < patProgramSize ; i++) {
            int programMapPid = mPatData.getProgramMapPid(i);
            int programNumber = mPatData.getProgramNumber(i);

            if (isProgramNumberValid(programNumber) && isProgramMapPidValid(programMapPid)) {
                Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
                // filter data = table id and program number(2 bytes, convert from int to raw data byte)
                demuxFilter.setFilterData(new byte[] {tableId, 0, 0, (byte)((programNumber>>8) & 0xFF), (byte)(programNumber & 0xFF)});
                demuxFilter.setFilterMask(new byte[] {(byte)0xFF, 0, 0, (byte)0xFF, (byte)0xFF});
                Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
                demuxChannel.setPid(programMapPid);
                demuxChannel.setTableId(tableId);
                demuxChannel.setCrcEnable(true);
                demuxChannel.setRepeat(true);
                demuxChannel.setFilter(demuxFilter);
                demuxChannel.setTimeOut(5000); // 5000 = based on pesi service

                demuxChannel.setDemuxSectionCallback(
                        new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

                demuxChannels.add(demuxChannel);
            }
        }

        return demuxChannels;
    }

    @Override
    protected TableData parsing(byte[] data, int lens) {
        int i=0, programNumber;

        PmtData pmtData = new PmtData(mTunerType);
        pmtData.parsing(data, lens);
        programNumber = pmtData.getProgramMap().getProgram_number();

        while(i<mPatData.getProgramTotalNum()){
            if(mPatData.getProgramNumber(i) == programNumber) {
                pmtData.setProgrammapPid(mPatData.getProgramMapPid(i));
                break;
            }
            i++;
        }

        return pmtData;
    }

    @Override
    protected void finish_table() {

    }

    private boolean isProgramNumberValid(int programNumber) {
        // 0 is reserved for a NIT packet identifier
        return programNumber > 0 && programNumber <= MAX_PROGRAM_NUMBER;
    }

    private boolean isProgramMapPidValid(int programMapPid) {
        // 0 is for PAT
        return programMapPid > 0 && programMapPid <= MAX_PROGRAM_MAP_PID;
    }
}
