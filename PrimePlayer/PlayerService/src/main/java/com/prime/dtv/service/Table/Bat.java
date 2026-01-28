package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;

import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;
import com.prime.datastructure.utils.Utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Bat extends Table {
    private static final String TAG = "Bat";
    private boolean mBatSearchMode;
    private int mBouquetId;
    private boolean mSdtSearchMode;
    private int max_multi_table_num;
    public Bat(int tunerID, int bouquetId,boolean BatSearchMode) {
        super(tunerID, BAT_TABLE_ID,BatSearchMode);
        //super(tuner, BAT_TABLE_ID);
        Log.d(TAG,"Creat Bat object, BatSearchMode = "+BatSearchMode+" BouquetId = "+bouquetId);
        mBatSearchMode=BatSearchMode;
        mBouquetId=bouquetId;
        max_multi_table_num=50;
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

    public List<BatData> getBatDataList() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            List<TableData> tableDataList = getData();
            List<BatData> batDataList = new ArrayList<>();
            for (TableData tableData : tableDataList) {
                batDataList.add((BatData) tableData);
            }
            return batDataList;
        }
    }

    protected List<Demux.DemuxChannel> getDemuxChannels() {
        int MutiSize,i;
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();
        DemuxSectionCallback dmxCallback;
        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        byte[] filterData = new byte[2];
        if(mBatSearchMode == true) {
            //Log.d(TAG,"allen_test mBatSearchMode == true,  tableId = "+Integer.toString(tableId,16));
            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_SUND)){
                filterData[0] = (byte) ((mBouquetId & 0xFF00) >> 8);
                demuxFilter.setFilterData(new byte[]{tableId, filterData[0]});
                demuxFilter.setFilterMask(new byte[]{(byte) 0xFF, (byte) 0xF0});
            }
            else{
            filterData[0] = (byte) ((mBouquetId & 0xFF00) >> 8);
            filterData[1] = (byte) (mBouquetId & 0x00FF);
            demuxFilter.setFilterData(new byte[]{tableId, filterData[0], filterData[1]});
            demuxFilter.setFilterMask(new byte[]{(byte) 0xFF, (byte) 0xF0, (byte) 0xFF});
        }
        }
        else {
            //Log.d(TAG,"allen_test mBatSearchMode == false,  tableId = "+Integer.toString(tableId,16));
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
        if(mBatSearchMode == true) {
            dmxCallback.setMultiBouquetId(max_multi_table_num);
        }
        return demuxChannels;
    }

    protected TableData parsing(byte[] data, int lens) {
        Log.d(TAG,"Bat parsing()");
        BatData batData=null;
        if(mBatSearchMode == true){
            int bouquetId,size,find,i;
            bouquetId = Utils.getInt(data, 3, 2, Utils.MASK_16BITS);
            size=getData().size();
            find=0;
            for(i=0;i<size;i++){
                batData = (BatData)getData().get(i);
                if(batData.getBouquetId() == bouquetId){
                    find=1;
                    break;
                }
            }
            if(find == 0){
                batData = new BatData();
            }
        }
        else {
            batData = getBatData(); // use old data to parse for multi section

        if (batData == null) {
            batData = new BatData();
        }
        }

        batData.parsing(data, lens);

        return batData;
    }

    @Override
    protected void finish_table() {

    }
}
