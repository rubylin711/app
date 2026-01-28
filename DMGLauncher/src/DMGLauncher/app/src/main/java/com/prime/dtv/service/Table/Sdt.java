package com.prime.dtv.service.Table;

import static java.lang.Byte.toUnsignedInt;


import android.media.tv.tuner.Tuner;
import android.util.Log;

import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Demux.DemuxSectionCallback;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class Sdt extends Table {
    private static final String TAG = "Sdt";
    private static boolean mIsSdtOther;
    private int mTransportStreamId;
    private boolean mSdtSearchMode;
    private List<Integer> mMutiTransportStreamId = new ArrayList<>();;
    public Sdt(int tunerID, boolean isActual,boolean SdtSearchMode, NitData nitData) {
        super(tunerID, isActual ? SDT_ACTUAL_TS_TABLE_ID : SDT_OTHER_TS_TABLE_ID,SdtSearchMode);
        //Log.d(TAG,"Creat Sdt object, isActual = "+isActual);
        mTransportStreamId=0;
        mSdtSearchMode=SdtSearchMode;
        mIsSdtOther = true;//!isActual;
        if(SdtSearchMode == true && (nitData != null)){
            List<NitData.TransportStream> transportStreamList=nitData.getTransportStreamList();
            for(NitData.TransportStream temp : transportStreamList) {
                setMultiTransportStreamId(temp.getTransportStreamID());
            }
            LogUtils.d("Run SDT Mutlti-Table (including SDT Other)");
        }
        else{
            LogUtils.d("Run Normal SDT Table");
        }
        this.run(this.getClass().getName());
    }

    public List<SdtData> getSdtData() {
        if (getData().isEmpty()) {
            return null;
        }
        else {
            List<TableData> tableDataList = getData();
            List<SdtData> sdtDataList = new ArrayList<>();
            for (TableData tableData : tableDataList) {
                sdtDataList.add((SdtData) tableData);
            }
            return sdtDataList;
        }
    }

    public void setSdtTransportStreamId(int TransportStreamId) {
        mTransportStreamId=TransportStreamId;
        }

    public void setMultiTransportStreamId(int TransportStreamId) {
        mMutiTransportStreamId.add(TransportStreamId);
    }

    @Override
    protected List<Demux.DemuxChannel> getDemuxChannels() {
        int MutiSize,i;
        byte tableId = getTableId();
        List<Demux.DemuxChannel> demuxChannels = new ArrayList<>();
        DemuxSectionCallback dmxCallback;
        Demux.DemuxFilter demuxFilter = new Demux.DemuxFilter();
        if(mSdtSearchMode == true) {
            //Log.d(TAG,"mSdtSearchMode == true, Mask=0xFB,  tableId = "+Integer.toString(tableId,16));
            demuxFilter.setFilterData(new byte[]{tableId});
            demuxFilter.setFilterMask(new byte[]{(byte) 0xFB});
        }
        else {
            if (tableId == SDT_OTHER_TS_TABLE_ID) {
                if (mTransportStreamId != 0) {
                    //Log.d(TAG,"mSdtSearchMode == false, Mask=0xFF 0xFF 0xFF");
                    byte[] filterData = new byte[2];
                    filterData[0] = (byte) ((mTransportStreamId & 0xFF00) >> 8);
                    filterData[1] = (byte) (mTransportStreamId & 0x00FF);
                    demuxFilter.setFilterData(new byte[]{tableId, filterData[0], filterData[1]});
                    demuxFilter.setFilterMask(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                }
                else {
                    //Log.d(TAG,"mSdtSearchMode == false, Mask=0xFF,  tableId = "+Integer.toString(tableId,16));
                    demuxFilter.setFilterData(new byte[] {tableId});
                    demuxFilter.setFilterMask(new byte[] {(byte)0xFF});
                }
            }
            else {
                //Log.d(TAG,"mSdtSearchMode == false, Mask=0xFF,  tableId = "+Integer.toString(tableId,16));
                demuxFilter.setFilterData(new byte[] {tableId});
                demuxFilter.setFilterMask(new byte[] {(byte)0xFF});
            }
        }
        Demux.DemuxChannel demuxChannel = new Demux.DemuxChannel();
        demuxChannel.setPid(SDT_PID);
        demuxChannel.setTableId(tableId);
        demuxChannel.setCrcEnable(true);
        demuxChannel.setRepeat(true);
        demuxChannel.setFilter(demuxFilter);
        demuxChannel.setTimeOut(10000);

        demuxChannel.setDemuxSectionCallback(
        new DemuxSectionCallback(getFilterCompleteCallback(), tableId));

        demuxChannels.add(demuxChannel);
        dmxCallback=demuxChannel.getDemuxSectionCallback();
        //Log.d(TAG,"mSdtSearchMode = " + mSdtSearchMode);
        if(mSdtSearchMode == true) {
            MutiSize = mMutiTransportStreamId.size();
                if (MutiSize > 0) {
                    for (i = 0; i < MutiSize; i++) {
                        dmxCallback.setMultiTransportStreamId(mMutiTransportStreamId.get(i));
                }
            }
        }
        return demuxChannels;
    }

    @Override
    protected TableData parsing(byte[] data, int lens) {
        SdtData sdtData = null;
        int transportStreamId;

        if (getData().isEmpty()) /*if (getSdtData() == null)*/{
            sdtData = new SdtData();
        }
        else{
            int i,size,find;
            //size=getSdtData().size();
            size=getData().size();
            if(mIsSdtOther == false) {
                sdtData = getSdtData().get(0);
            }
            else{
                transportStreamId=(toUnsignedInt(data[3])<<8)+toUnsignedInt(data[4]);
                find=0;
                for(i=0;i<size;i++){
                    //sdtData = getSdtData().get(i);
                    sdtData = (SdtData)getData().get(i);
                    if(sdtData.getTransportStreamIdByIndex(0) == transportStreamId){
                        find=1;
                        break;
                    }
                }
                if(find == 0){
                    sdtData = new SdtData();
                }
            }

        }
        if (sdtData == null) {
            Log.d(TAG, "sdtData is NULL, something error");
        }
        sdtData.parsing(data, lens);

        return sdtData;
    }

    @Override
    protected void finish_table() {

    }

}
