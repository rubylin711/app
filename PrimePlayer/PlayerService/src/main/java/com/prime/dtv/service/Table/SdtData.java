package com.prime.dtv.service.Table;

import static com.prime.dtv.service.Table.Desciptor.Descriptor.SERVICE_DESC;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;
import com.prime.dtv.service.Table.Desciptor.ServiceDescriptor;

import java.util.ArrayList;
import java.util.List;


public class SdtData extends TableData {
    private static final String TAG = "SdtData";
    public class ServiceData {
        private int mTransportStreamId;
        private int mVersionNumber;
        private int mOriginalNetworkId;
        private int mServiceId;
        private int mEitScheduleFlag;
        private int mEitPresentFollowingFlag;
        private int mRunningStatus;
        private int mFreeCaMode;
        private Descriptor mDescriptor = new Descriptor();

        public ServiceData(byte[] data, int descriptorsLoopLength) {
            int i;
            //Log.d(TAG,"SDT Data descriptorsLoopLength = " + descriptorsLoopLength);
            for(i=0;i<descriptorsLoopLength;){
                int descriptorLength,descriptorTag;
                descriptorTag=toUnsignedInt(data[i]);
                descriptorLength=toUnsignedInt(data[i+1]);
               // Log.d(TAG,"SDT Data descriptorTag = " + descriptorTag);
               // Log.d(TAG,"SDT Data descriptorLength = " + descriptorLength);
                byte[] tableData = new byte[descriptorLength+2];
                System.arraycopy(data, i, tableData, 0, descriptorLength+2);
                mDescriptor.ParsingDescriptor(tableData, descriptorLength+2);
                i=i+2+descriptorLength;
            }
        }

        public Descriptor getDescriptor() {
            return mDescriptor;
        }

        public int getTransportStreamId() {
            return mTransportStreamId;
        }

        private void setTransportStreamId(int transportStreamId) {
            this.mTransportStreamId = transportStreamId;
        }

        public int getVersionNumber() {
            return mVersionNumber;
        }

        private void setVersionNumber(int versionNumber) {
            this.mVersionNumber = versionNumber;
        }

        public int getOriginalNetworkId() {
            return mOriginalNetworkId;
        }

        private void setOriginalNetworkId(int originalNetworkId) {
            this.mOriginalNetworkId = originalNetworkId;
        }

        public int getServiceId() {
            return mServiceId;
        }

        private void setServiceId(int serviceId) {
            this.mServiceId = serviceId;
        }

        public int getEitScheduleFlag() {
            return mEitScheduleFlag;
        }

        private void setEitScheduleFlag(int eitScheduleFlag) {
            this.mEitScheduleFlag = eitScheduleFlag;
        }

        public int getEitPresentFollowingFlag() {
            return mEitPresentFollowingFlag;
        }

        private void setEitPresentFollowingFlag(int eitPresentFollowingFlag) {
            this.mEitPresentFollowingFlag = eitPresentFollowingFlag;
        }

        public int getRunningStatus() {
            return mRunningStatus;
        }

        private void setRunningStatus(int runningStatus) {
            this.mRunningStatus = runningStatus;
        }

        public int getFreeCaMode() {
            return mFreeCaMode;
        }

        private void setFreeCaMode(int freeCaMode) {
            this.mFreeCaMode = freeCaMode;
        }

        public int getServiceType() {
            //Log.d(TAG,"SDT Data getServiceType");
            List<DescBase> descBase = mDescriptor.getDescriptorList(SERVICE_DESC);
            //Log.d(TAG,"SDT Data descBase.size() = " + descBase.size());
            for(int i = 0; i < descBase.size(); i++) {
                ServiceDescriptor serviceDescriptor = (ServiceDescriptor) descBase.get(i);
                return serviceDescriptor.ServiceType ;
            }
            return -1;
        }
        public String getServiceProviderName() {
            List<DescBase> descBase = mDescriptor.getDescriptorList(SERVICE_DESC);
            for(int i = 0; i < descBase.size(); i++) {
                ServiceDescriptor serviceDescriptor = (ServiceDescriptor) descBase.get(i);
                return serviceDescriptor.ServiceProviderName ;
            }
            return null;
        }
        public String getServiceName() {
            List<DescBase> descBase = mDescriptor.getDescriptorList(SERVICE_DESC);
            for(int i = 0; i < descBase.size(); i++) {
                ServiceDescriptor serviceDescriptor = (ServiceDescriptor) descBase.get(i);
                return serviceDescriptor.ServiceName ;
            }
            return null;
        }
    }

    private List<ServiceData> mServiceData = new ArrayList<>();;

    public List<ServiceData> geServiceData() {
        return mServiceData;
    }

    public int getServiceDataTotalNum() {
        return mServiceData.size();
    }

    public int getTransportStreamIdByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getTransportStreamId();
        }
        return -1;
    }

    public int getVersionNumberByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getVersionNumber();
        }
        return -1;
    }

    public int getOriginalNetworkIdByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getOriginalNetworkId();
        }
        return -1;
    }

    public int getServiceIdByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getServiceId();
        }
        return -1;
    }

    public int getEitPresentFollowingFlagByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getEitPresentFollowingFlag();
        }
        return -1;
    }

    public int getEitScheduleFlagByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getEitScheduleFlag();
        }
        return -1;
    }

    public int getRunningStatusByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getRunningStatus();
        }
        return -1;
    }

    public int getFreeCaModeByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getFreeCaMode();
        }
        return -1;
    }

    public String getServiceProviderNameByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getServiceProviderName();
        }
        return null;
    }

    public String getServiceNameByIndex(int index) {
        if((mServiceData.size() > 0) && (index < mServiceData.size())) {
            return mServiceData.get(index).getServiceName();
        }
        return null;
    }

    public ServiceData getServiceData(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
           if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i);
            }
        }
        return null;
    }

    public int getEitScheduleFlag(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
            if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i).getEitScheduleFlag();
            }
        }
        return -1;
    }

    public int getEitPresentFollowingFlag(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
            if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i).getEitPresentFollowingFlag();
            }
        }
        return -1;
    }

    public int getRunningStatus(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
            if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i).getRunningStatus();
            }
        }
        return -1;
    }

    public int getFreeCaMode(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
            if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i).getFreeCaMode();
            }
        }
        return -1;
    }

    public int getServiceType(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
            if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i).getServiceType();
            }
        }
        return -1;
    }

    public String getServiceProviderName(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
            if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i).getServiceProviderName();
            }
        }
        return null;
    }

    public String getServiceName(final int transportStreamId,final int serviceId) {
        for(int i=0;i<mServiceData.size();i++) {
            if((mServiceData.get(i).getTransportStreamId()==transportStreamId) &&
                    (mServiceData.get(i).getServiceId()==serviceId)) {
                return mServiceData.get(i).getServiceName();
            }
        }
        return null;
    }

    private void buildServiceDataList(byte[] data, int offset, int sdtLength) {
        //private List<SdtData.ServiceData> mServiceData;
        //final ArrayList<ServiceData> r = new ArrayList<>();
        try{
            int i = 0,j=0;
            int headerOffset=11;
            int loopLen = sdtLength - headerOffset - 4; //Header + CRC
            int tableId = toUnsignedInt(data[0]);
            int transportStreamId=(toUnsignedInt(data[3])<<8)+toUnsignedInt(data[4]);
            int version = (toUnsignedInt(data[5]) & 0x3e)>>1;
            int originalNetworkId=(toUnsignedInt(data[8])<<8) +toUnsignedInt(data[9]);

            //Log.d(TAG,"SDT Data sdtLength = " + sdtLength);
            //Log.d(TAG,"SDT Data tableId = " + tableId);
            //Log.d(TAG,"SDT Data transportStreamId = " + transportStreamId);
            //Log.d(TAG,"SDT Data originalNetworkId = " + originalNetworkId);
            //Log.d(TAG,"SDT Data loopLen = " + loopLen);
            //Log.d(TAG,"SDT Data headerOffset = " + headerOffset);

            for (i = headerOffset; i < loopLen; )
            {
                int serviceId=(toUnsignedInt(data[i]) << 8) + toUnsignedInt(data[i+1]);
                int eitScheduleFlag=(toUnsignedInt(data[i+2]) & 0x2) >> 1;
                int eitPresentFollowingFlag=toUnsignedInt(data[i+2]) & 0x1;
                int runningStatus=(toUnsignedInt(data[i+3]) & 0xe0) >> 5;
                int freeCaMode=(toUnsignedInt(data[i+3]) & 0x10) >> 4;
                int descriptorsLoopLength = ((toUnsignedInt(data[i+3]) & 0xf) << 8) + toUnsignedInt(data[i+4]);

                //Log.d(TAG, "SDT Data i = " + i);
                //Log.d(TAG, "SDT Data serviceId = " + serviceId);
                //Log.d(TAG, "SDT Data eitScheduleFlag = " + eitScheduleFlag);
                //Log.d(TAG, "SDT Data eitPresentFollowingFlag = " + eitPresentFollowingFlag);
                //Log.d(TAG, "SDT Data runningStatus = " + runningStatus);
                //Log.d(TAG, "SDT Data freeCaMode = " + freeCaMode);
                //Log.d(TAG, "SDT Data descriptorsLoopLength = " + descriptorsLoopLength);

                byte[] tableData = new byte[descriptorsLoopLength];
                System.arraycopy(data, i + 5, tableData, 0, descriptorsLoopLength);
                ServiceData serviceData = new ServiceData(tableData, descriptorsLoopLength);

                serviceData.setTransportStreamId(transportStreamId);
                serviceData.setVersionNumber(version);
                serviceData.setOriginalNetworkId(originalNetworkId);
                serviceData.setServiceId(serviceId);
                serviceData.setEitScheduleFlag(eitScheduleFlag);
                serviceData.setEitPresentFollowingFlag(eitPresentFollowingFlag);
                serviceData.setRunningStatus(runningStatus);
                serviceData.setFreeCaMode(freeCaMode);
                mServiceData.add(serviceData);

                i = i + 5 + descriptorsLoopLength; // 5 = ServiceData header
            }
        } catch (Exception e) {
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
        //return r;
    }

    @Override
    public void parsing(byte[] data, int lens) {
        buildServiceDataList(data, 0, lens);
    }

    public void SdtTestFuntion()
    {
        //SdtData sdtData;
        //sdtData=new SdtData();
        int i,j,totalNum;
        //The test data is SDT Actual from mux1.trp
        byte[] sdtTableData=new byte[]
        {
                (byte)0x42, (byte)0xf1, (byte)0x36, (byte)0x03, (byte)0xfd, (byte)0xc3, (byte)0x00, (byte)0x00, (byte)0x22, (byte)0xf1, (byte)0xff, (byte)0x13, (byte)0xc4, (byte)0xfd, (byte)0x80, (byte)0x2f, (byte)0x48, (byte)0x22,
                (byte)0x01, (byte)0x13, (byte)0x53, (byte)0x76, (byte)0x65, (byte)0x72, (byte)0x69, (byte)0x67, (byte)0x65, (byte)0x73, (byte)0x20, (byte)0x54, (byte)0x65, (byte)0x6c, (byte)0x65, (byte)0x76, (byte)0x69, (byte)0x73,
                (byte)0x69, (byte)0x6f, (byte)0x6e, (byte)0x0c, (byte)0x53, (byte)0x56, (byte)0x54, (byte)0x32, (byte)0x20, (byte)0x6d, (byte)0x65, (byte)0x64, (byte)0x20, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x5f, (byte)0x04,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x14, (byte)0xf1, (byte)0x03, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0x14, (byte)0x32, (byte)0xfd, (byte)0x80, (byte)0x29, (byte)0x48, (byte)0x1c, (byte)0x01, (byte)0x13,
                (byte)0x53, (byte)0x76, (byte)0x65, (byte)0x72, (byte)0x69, (byte)0x67, (byte)0x65, (byte)0x73, (byte)0x20, (byte)0x54, (byte)0x65, (byte)0x6c, (byte)0x65, (byte)0x76, (byte)0x69, (byte)0x73, (byte)0x69, (byte)0x6f,
                (byte)0x6e, (byte)0x06, (byte)0x32, (byte)0x34, (byte)0x20, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x5f, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x14, (byte)0xf1, (byte)0x03, (byte)0x00, (byte)0x63,
                (byte)0x00, (byte)0x11, (byte)0xc6, (byte)0xfc, (byte)0x80, (byte)0x30, (byte)0x48, (byte)0x23, (byte)0x82, (byte)0x12, (byte)0x42, (byte)0x6f, (byte)0x78, (byte)0x65, (byte)0x72, (byte)0x20, (byte)0x54, (byte)0x56,
                (byte)0x20, (byte)0x41, (byte)0x63, (byte)0x63, (byte)0x65, (byte)0x73, (byte)0x73, (byte)0x20, (byte)0x41, (byte)0x42, (byte)0x0e, (byte)0x4e, (byte)0x6f, (byte)0x6b, (byte)0x69, (byte)0x61, (byte)0x5f, (byte)0x32,
                (byte)0x31, (byte)0x32, (byte)0x54, (byte)0x5f, (byte)0x73, (byte)0x77, (byte)0x31, (byte)0x5f, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x14, (byte)0xf1, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x01,
                (byte)0x03, (byte)0x70, (byte)0xfd, (byte)0x80, (byte)0x2f, (byte)0x48, (byte)0x22, (byte)0x01, (byte)0x16, (byte)0x53, (byte)0x76, (byte)0x65, (byte)0x72, (byte)0x69, (byte)0x67, (byte)0x65, (byte)0x73, (byte)0x20,
                (byte)0x54, (byte)0x65, (byte)0x6c, (byte)0x65, (byte)0x76, (byte)0x69, (byte)0x73, (byte)0x69, (byte)0x6f, (byte)0x6e, (byte)0x20, (byte)0x41, (byte)0x42, (byte)0x09, (byte)0x53, (byte)0x56, (byte)0x54, (byte)0x20,
                (byte)0x45, (byte)0x78, (byte)0x74, (byte)0x72, (byte)0x61, (byte)0x5f, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x14, (byte)0xf1, (byte)0x03, (byte)0x00, (byte)0x61, (byte)0x00, (byte)0x03, (byte)0xf2,
                (byte)0xfd, (byte)0x80, (byte)0x27, (byte)0x48, (byte)0x1a, (byte)0x01, (byte)0x13, (byte)0x53, (byte)0x76, (byte)0x65, (byte)0x72, (byte)0x69, (byte)0x67, (byte)0x65, (byte)0x73, (byte)0x20, (byte)0x54, (byte)0x65,
                (byte)0x6c, (byte)0x65, (byte)0x76, (byte)0x69, (byte)0x73, (byte)0x69, (byte)0x6f, (byte)0x6e, (byte)0x04, (byte)0x53, (byte)0x56, (byte)0x54, (byte)0x31, (byte)0x5f, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x14, (byte)0xf1, (byte)0x03, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x03, (byte)0x66, (byte)0xfd, (byte)0x80, (byte)0x2e, (byte)0x48, (byte)0x21, (byte)0x01, (byte)0x13, (byte)0x53, (byte)0x76, (byte)0x65,
                (byte)0x72, (byte)0x69, (byte)0x67, (byte)0x65, (byte)0x73, (byte)0x20, (byte)0x54, (byte)0x65, (byte)0x6c, (byte)0x65, (byte)0x76, (byte)0x69, (byte)0x73, (byte)0x69, (byte)0x6f, (byte)0x6e, (byte)0x0b, (byte)0x42,
                (byte)0x61, (byte)0x72, (byte)0x6e, (byte)0x6b, (byte)0x61, (byte)0x6e, (byte)0x61, (byte)0x6c, (byte)0x65, (byte)0x6e, (byte)0x5f, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x14, (byte)0xf1, (byte)0x03,
                (byte)0x00, (byte)0x62, (byte)0x00, (byte)0x94, (byte)0x48, (byte)0x1d, (byte)0x98
        };

        byte[] sdtMultiTableData_1=new byte[]
        {
                (byte)0x42 ,(byte)0xf3 ,(byte)0xf1 ,(byte)0x00 ,(byte)0x01 ,(byte)0xc5 ,(byte)0x00 ,
                (byte)0x01 ,(byte)0x00 ,(byte)0x01 ,(byte)0xff ,(byte)0x00 ,(byte)0x01 ,(byte)0xfd ,(byte)0x80 ,
                (byte)0x19 ,(byte)0x48 ,(byte)0x17 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,
                (byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0c ,(byte)0x49 ,(byte)0x6e ,
                (byte)0x66 ,(byte)0x6f ,(byte)0x20 ,(byte)0x43 ,(byte)0x68 ,(byte)0x61 ,(byte)0x6e ,(byte)0x6e ,
                (byte)0x65 ,(byte)0x6c ,(byte)0x00 ,(byte)0x64 ,(byte)0xfd ,(byte)0x90 ,(byte)0x1b ,(byte)0x48 ,
                (byte)0x13 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,
                (byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x08 ,(byte)0x46 ,(byte)0x65 ,(byte)0x73 ,(byte)0x74 ,
                (byte)0x69 ,(byte)0x76 ,(byte)0x61 ,(byte)0x6c ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,
                (byte)0x44 ,(byte)0x4e ,(byte)0x00 ,(byte)0x96 ,(byte)0xfd ,(byte)0x90 ,(byte)0x25 ,(byte)0x48 ,
                (byte)0x1d ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,
                (byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x12 ,(byte)0x46 ,(byte)0x4f ,(byte)0x58 ,(byte)0x20 ,
                (byte)0x4d ,(byte)0x6f ,(byte)0x76 ,(byte)0x69 ,(byte)0x65 ,(byte)0x73 ,(byte)0x20 ,(byte)0x50 ,
                (byte)0x72 ,(byte)0x65 ,(byte)0x6d ,(byte)0x69 ,(byte)0x75 ,(byte)0x6d ,(byte)0x49 ,(byte)0x04 ,
                (byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x00 ,(byte)0x9b ,(byte)0xfd ,(byte)0x90 ,
                (byte)0x21 ,(byte)0x48 ,(byte)0x19 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,
                (byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0e ,(byte)0x46 ,(byte)0x4f ,
                (byte)0x58 ,(byte)0x20 ,(byte)0x46 ,(byte)0x61 ,(byte)0x6d ,(byte)0x20 ,(byte)0x4d ,(byte)0x6f ,
                (byte)0x76 ,(byte)0x69 ,(byte)0x65 ,(byte)0x73 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,
                (byte)0x44 ,(byte)0x4e ,(byte)0x00 ,(byte)0x9c ,(byte)0xfd ,(byte)0x90 ,(byte)0x19 ,(byte)0x48 ,
                (byte)0x17 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,
                (byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0c ,(byte)0x43 ,(byte)0x69 ,(byte)0x6e ,(byte)0x65 ,
                (byte)0x6d ,(byte)0x61 ,(byte)0x20 ,(byte)0x57 ,(byte)0x6f ,(byte)0x72 ,(byte)0x6c ,(byte)0x64 ,
                (byte)0x00 ,(byte)0x9e ,(byte)0xfd ,(byte)0x90 ,(byte)0x1e ,(byte)0x48 ,(byte)0x1c ,(byte)0x01 ,
                (byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,
                (byte)0x56 ,(byte)0x11 ,(byte)0x46 ,(byte)0x6f ,(byte)0x78 ,(byte)0x20 ,(byte)0x41 ,(byte)0x63 ,
                (byte)0x74 ,(byte)0x69 ,(byte)0x6f ,(byte)0x6e ,(byte)0x20 ,(byte)0x4d ,(byte)0x6f ,(byte)0x76 ,
                (byte)0x69 ,(byte)0x65 ,(byte)0x73 ,(byte)0x00 ,(byte)0x9f ,(byte)0xfd ,(byte)0x90 ,(byte)0x23 ,
                (byte)0x48 ,(byte)0x1b ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,
                (byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x10 ,(byte)0x53 ,(byte)0x74 ,(byte)0x61 ,
                (byte)0x72 ,(byte)0x20 ,(byte)0x43 ,(byte)0x68 ,(byte)0x6e ,(byte)0x20 ,(byte)0x4d ,(byte)0x6f ,
                (byte)0x76 ,(byte)0x20 ,(byte)0x53 ,(byte)0x45 ,(byte)0x41 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,
                (byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x00 ,(byte)0xc9 ,(byte)0xfd ,(byte)0x90 ,(byte)0x1d ,
                (byte)0x48 ,(byte)0x15 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,
                (byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0a ,(byte)0x53 ,(byte)0x74 ,(byte)0x61 ,
                (byte)0x72 ,(byte)0x20 ,(byte)0x57 ,(byte)0x6f ,(byte)0x72 ,(byte)0x6c ,(byte)0x64 ,(byte)0x49 ,
                (byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x00 ,(byte)0xca ,(byte)0xfd ,
                (byte)0x90 ,(byte)0x16 ,(byte)0x48 ,(byte)0x0e ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,
                (byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x03 ,(byte)0x46 ,
                (byte)0x4f ,(byte)0x58 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,
                (byte)0x00 ,(byte)0xcb ,(byte)0xfd ,(byte)0x90 ,(byte)0x1b ,(byte)0x48 ,(byte)0x13 ,(byte)0x01 ,
                (byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,
                (byte)0x56 ,(byte)0x08 ,(byte)0x46 ,(byte)0x4f ,(byte)0x58 ,(byte)0x43 ,(byte)0x52 ,(byte)0x49 ,
                (byte)0x4d ,(byte)0x45 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,
                (byte)0x00 ,(byte)0xcc ,(byte)0xfd ,(byte)0x90 ,(byte)0x15 ,(byte)0x48 ,(byte)0x0d ,(byte)0x01 ,
                (byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,
                (byte)0x56 ,(byte)0x02 ,(byte)0x46 ,(byte)0x58 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,
                (byte)0x44 ,(byte)0x4e ,(byte)0x00 ,(byte)0xcd ,(byte)0xfd ,(byte)0x90 ,(byte)0x11 ,(byte)0x48 ,
                (byte)0x0f ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,
                (byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x04 ,(byte)0x44 ,(byte)0x49 ,(byte)0x56 ,(byte)0x41 ,
                (byte)0x00 ,(byte)0xce ,(byte)0xfd ,(byte)0x90 ,(byte)0x1c ,(byte)0x48 ,(byte)0x14 ,(byte)0x01 ,
                (byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,
                (byte)0x56 ,(byte)0x09 ,(byte)0x55 ,(byte)0x6e ,(byte)0x69 ,(byte)0x76 ,(byte)0x65 ,(byte)0x72 ,
                (byte)0x73 ,(byte)0x61 ,(byte)0x6c ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,
                (byte)0x4e ,(byte)0x00 ,(byte)0xcf ,(byte)0xfd ,(byte)0x90 ,(byte)0x17 ,(byte)0x48 ,(byte)0x0f ,
                (byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,
                (byte)0x54 ,(byte)0x56 ,(byte)0x04 ,(byte)0x53 ,(byte)0x79 ,(byte)0x66 ,(byte)0x79 ,(byte)0x49 ,
                (byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x01 ,(byte)0x02 ,(byte)0xfd ,
                (byte)0x90 ,(byte)0x14 ,(byte)0x48 ,(byte)0x12 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,
                (byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x07 ,(byte)0x43 ,
                (byte)0x20 ,(byte)0x4d ,(byte)0x55 ,(byte)0x53 ,(byte)0x49 ,(byte)0x43 ,(byte)0x01 ,(byte)0x2d ,
                (byte)0xfd ,(byte)0x90 ,(byte)0x20 ,(byte)0x48 ,(byte)0x18 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,
                (byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0d ,
                (byte)0x44 ,(byte)0x69 ,(byte)0x73 ,(byte)0x6e ,(byte)0x65 ,(byte)0x79 ,(byte)0x20 ,(byte)0x4a ,
                (byte)0x75 ,(byte)0x6e ,(byte)0x69 ,(byte)0x6f ,(byte)0x72 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,
                (byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x01 ,(byte)0x2e ,(byte)0xfd ,(byte)0x90 ,(byte)0x21 ,
                (byte)0x48 ,(byte)0x19 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,
                (byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0e ,(byte)0x44 ,(byte)0x69 ,(byte)0x73 ,
                (byte)0x6e ,(byte)0x65 ,(byte)0x79 ,(byte)0x20 ,(byte)0x43 ,(byte)0x68 ,(byte)0x61 ,(byte)0x6e ,
                (byte)0x6e ,(byte)0x65 ,(byte)0x6c ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,
                (byte)0x4e ,(byte)0x01 ,(byte)0x2f ,(byte)0xfd ,(byte)0x90 ,(byte)0x1d ,(byte)0x48 ,(byte)0x15 ,
                (byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,
                (byte)0x54 ,(byte)0x56 ,(byte)0x0a ,(byte)0x42 ,(byte)0x61 ,(byte)0x62 ,(byte)0x79 ,(byte)0x20 ,
                (byte)0x46 ,(byte)0x69 ,(byte)0x72 ,(byte)0x73 ,(byte)0x74 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,
                (byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x01 ,(byte)0x31 ,(byte)0xfd ,(byte)0x90 ,(byte)0x21 ,
                (byte)0x48 ,(byte)0x19 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,
                (byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0e ,(byte)0x4e ,(byte)0x69 ,(byte)0x63 ,
                (byte)0x6b ,(byte)0x65 ,(byte)0x6c ,(byte)0x6f ,(byte)0x64 ,(byte)0x65 ,(byte)0x6f ,(byte)0x6e ,
                (byte)0x20 ,(byte)0x4a ,(byte)0x72 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,
                (byte)0x4e ,(byte)0x01 ,(byte)0xf7 ,(byte)0xfd ,(byte)0x90 ,(byte)0x19 ,(byte)0x48 ,(byte)0x11 ,
                (byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,
                (byte)0x54 ,(byte)0x56 ,(byte)0x06 ,(byte)0x45 ,(byte)0x21 ,(byte)0x20 ,(byte)0x45 ,(byte)0x6e ,
                (byte)0x74 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x01 ,
                (byte)0xf8 ,(byte)0xfd ,(byte)0x90 ,(byte)0x18 ,(byte)0x48 ,(byte)0x16 ,(byte)0x01 ,(byte)0x08 ,
                (byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,
                (byte)0x0b ,(byte)0x46 ,(byte)0x61 ,(byte)0x73 ,(byte)0x68 ,(byte)0x69 ,(byte)0x6f ,(byte)0x6e ,
                (byte)0x20 ,(byte)0x4f ,(byte)0x6e ,(byte)0x65 ,(byte)0x02 ,(byte)0x5d ,(byte)0xfd ,(byte)0x90 ,
                (byte)0x19 ,(byte)0x48 ,(byte)0x17 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,
                (byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0c ,(byte)0x46 ,(byte)0x6f ,
                (byte)0x78 ,(byte)0x20 ,(byte)0x53 ,(byte)0x70 ,(byte)0x6f ,(byte)0x72 ,(byte)0x74 ,(byte)0x73 ,
                (byte)0x20 ,(byte)0x33 ,(byte)0x02 ,(byte)0x9c ,(byte)0xfd ,(byte)0x90 ,(byte)0x18 ,(byte)0x48 ,
                (byte)0x16 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,
                (byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0b ,(byte)0x51 ,(byte)0x75 ,(byte)0x61 ,(byte)0x64 ,
                (byte)0x20 ,(byte)0x53 ,(byte)0x70 ,(byte)0x6f ,(byte)0x72 ,(byte)0x74 ,(byte)0x73 ,(byte)0x03 ,
                (byte)0x21 ,(byte)0xfd ,(byte)0x90 ,(byte)0x21 ,(byte)0x48 ,(byte)0x19 ,(byte)0x01 ,(byte)0x08 ,
                (byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,
                (byte)0x0e ,(byte)0x4e ,(byte)0x61 ,(byte)0x74 ,(byte)0x47 ,(byte)0x65 ,(byte)0x6f ,(byte)0x20 ,
                (byte)0x43 ,(byte)0x68 ,(byte)0x61 ,(byte)0x6e ,(byte)0x6e ,(byte)0x65 ,(byte)0x6c ,(byte)0x49 ,
                (byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x22 ,(byte)0xfd ,
                (byte)0x90 ,(byte)0x20 ,(byte)0x48 ,(byte)0x18 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,
                (byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0d ,(byte)0x4e ,
                (byte)0x61 ,(byte)0x74 ,(byte)0x47 ,(byte)0x65 ,(byte)0x6f ,(byte)0x20 ,(byte)0x50 ,(byte)0x65 ,
                (byte)0x6f ,(byte)0x70 ,(byte)0x6c ,(byte)0x65 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,
                (byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x23 ,(byte)0xfd ,(byte)0x90 ,(byte)0x1e ,(byte)0x48 ,
                (byte)0x16 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,
                (byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x0b ,(byte)0x4e ,(byte)0x61 ,(byte)0x74 ,(byte)0x47 ,
                (byte)0x65 ,(byte)0x6f ,(byte)0x20 ,(byte)0x57 ,(byte)0x69 ,(byte)0x6c ,(byte)0x64 ,(byte)0x49 ,
                (byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x85 ,(byte)0xfd ,
                (byte)0x90 ,(byte)0x17 ,(byte)0x48 ,(byte)0x0f ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,
                (byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x04 ,(byte)0x54 ,
                (byte)0x56 ,(byte)0x52 ,(byte)0x49 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,
                (byte)0x4e ,(byte)0x03 ,(byte)0x86 ,(byte)0xfd ,(byte)0x90 ,(byte)0x17 ,(byte)0x48 ,(byte)0x0f ,
                (byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,
                (byte)0x54 ,(byte)0x56 ,(byte)0x04 ,(byte)0x53 ,(byte)0x43 ,(byte)0x54 ,(byte)0x56 ,(byte)0x49 ,
                (byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x87 ,(byte)0xfd ,
                (byte)0x90 ,(byte)0x18 ,(byte)0x48 ,(byte)0x10 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,
                (byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x05 ,(byte)0x4c ,
                (byte)0x45 ,(byte)0x4a ,(byte)0x45 ,(byte)0x4c ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,
                (byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x88 ,(byte)0xfd ,(byte)0x90 ,(byte)0x1a ,(byte)0x48 ,
                (byte)0x12 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,
                (byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x07 ,(byte)0x54 ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,
                (byte)0x73 ,(byte)0x20 ,(byte)0x37 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,
                (byte)0x4e ,(byte)0x03 ,(byte)0x89 ,(byte)0xfd ,(byte)0x90 ,(byte)0x1b ,(byte)0x48 ,(byte)0x13 ,
                (byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,
                (byte)0x54 ,(byte)0x56 ,(byte)0x08 ,(byte)0x49 ,(byte)0x6e ,(byte)0x64 ,(byte)0x6f ,(byte)0x73 ,
                (byte)0x69 ,(byte)0x61 ,(byte)0x72 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,
                (byte)0x4e ,(byte)0xf8 ,(byte)0x4b ,(byte)0x91 ,(byte)0x6f
        };

        byte[] sdtMultiTableData_2=new byte[]
        {
                (byte)0x42 ,(byte)0xf0 ,(byte)0xda ,(byte)0x00 ,(byte)0x01 ,(byte)0xc5 ,(byte)0x01 ,
                (byte)0x01 ,(byte)0x00 ,(byte)0x01 ,(byte)0xff ,(byte)0x03 ,(byte)0x8a ,(byte)0xfd ,(byte)0x90 ,
                (byte)0x17 ,(byte)0x48 ,(byte)0x0f ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,
                (byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x04 ,(byte)0x41 ,(byte)0x4e ,
                (byte)0x54 ,(byte)0x56 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,
                (byte)0x03 ,(byte)0x8b ,(byte)0xfd ,(byte)0x90 ,(byte)0x19 ,(byte)0x48 ,(byte)0x11 ,(byte)0x01 ,
                (byte)0x08 ,(byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,
                (byte)0x56 ,(byte)0x06 ,(byte)0x54 ,(byte)0x56 ,(byte)0x20 ,(byte)0x4f ,(byte)0x6e ,(byte)0x65 ,
                (byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x8c ,
                (byte)0xfd ,(byte)0x90 ,(byte)0x1b ,(byte)0x48 ,(byte)0x13 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,
                (byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x08 ,
                (byte)0x4d ,(byte)0x65 ,(byte)0x74 ,(byte)0x72 ,(byte)0x6f ,(byte)0x20 ,(byte)0x54 ,(byte)0x56 ,
                (byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x8e ,
                (byte)0xfd ,(byte)0x90 ,(byte)0x1c ,(byte)0x48 ,(byte)0x14 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,
                (byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x09 ,
                (byte)0x4b ,(byte)0x6f ,(byte)0x6d ,(byte)0x70 ,(byte)0x61 ,(byte)0x73 ,(byte)0x20 ,(byte)0x54 ,
                (byte)0x56 ,(byte)0x49 ,(byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x03 ,
                (byte)0x8f ,(byte)0xfd ,(byte)0x90 ,(byte)0x19 ,(byte)0x48 ,(byte)0x11 ,(byte)0x01 ,(byte)0x08 ,
                (byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,
                (byte)0x06 ,(byte)0x4e ,(byte)0x65 ,(byte)0x74 ,(byte)0x20 ,(byte)0x54 ,(byte)0x56 ,(byte)0x49 ,
                (byte)0x04 ,(byte)0xff ,(byte)0x49 ,(byte)0x44 ,(byte)0x4e ,(byte)0x03 ,(byte)0x91 ,(byte)0xfd ,
                (byte)0x90 ,(byte)0x15 ,(byte)0x48 ,(byte)0x13 ,(byte)0x01 ,(byte)0x08 ,(byte)0x4f ,(byte)0x72 ,
                (byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,(byte)0x08 ,(byte)0x54 ,
                (byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x73 ,(byte)0x20 ,(byte)0x54 ,(byte)0x56 ,(byte)0x03 ,
                (byte)0x97 ,(byte)0xfd ,(byte)0x90 ,(byte)0x16 ,(byte)0x48 ,(byte)0x14 ,(byte)0x01 ,(byte)0x08 ,
                (byte)0x4f ,(byte)0x72 ,(byte)0x61 ,(byte)0x6e ,(byte)0x67 ,(byte)0x65 ,(byte)0x54 ,(byte)0x56 ,
                (byte)0x09 ,(byte)0x4f ,(byte)0x20 ,(byte)0x43 ,(byte)0x68 ,(byte)0x61 ,(byte)0x6e ,(byte)0x6e ,
                (byte)0x65 ,(byte)0x6c ,(byte)0x5b ,(byte)0x16 ,(byte)0x99 ,(byte)0xa8
        };
        Log.d(TAG,"SdtParsing sdtTableData Start @@@@@@@@@@@@@@@@");
        this.parsing(sdtTableData,sdtTableData.length);
        Log.d(TAG,"SdtParsing sdtTableData End @@@@@@@@@@@@@@@@");
        //this.parsing(sdtTableData,sdtTableData.length);
        Log.d(TAG,"SdtParsing sdtMultiTableData_1 Start @@@@@@@@@@@@@@@@");
        this.parsing(sdtMultiTableData_1,sdtMultiTableData_1.length);
        Log.d(TAG,"SdtParsing sdtMultiTableData_1 End @@@@@@@@@@@@@@@@");
        Log.d(TAG,"SdtParsing sdtMultiTableData_2 Start @@@@@@@@@@@@@@@@");
        this.parsing(sdtMultiTableData_2,sdtMultiTableData_2.length);
        Log.d(TAG,"SdtParsing sdtMultiTableData_2 End @@@@@@@@@@@@@@@@");

        totalNum=this.getServiceDataTotalNum();
        Log.d(TAG,"Get Sdt Data TotalNumber = " + totalNum);
        if(totalNum > 0)
        {
            for(i=0;i<totalNum;i++)
            {
                Log.d(TAG,"=======================================");
                Log.d(TAG,"Get Sdt Data " + (i+1));
                int transportStreamId=this.getTransportStreamIdByIndex(i);
                Log.d(TAG,"Get Sdt Data TransportStreamId = " + transportStreamId);
                int originalNetworkId=this.getOriginalNetworkIdByIndex(i);
                Log.d(TAG,"Get Sdt Data OriginalNetworkId = " + originalNetworkId);
                int serviceId=this.getServiceIdByIndex(i);
                Log.d(TAG,"Get Sdt Data ServiceId = " + serviceId);
                int eitScheduleFlag=this.getEitScheduleFlag(transportStreamId,serviceId);
                Log.d(TAG,"Get Sdt Data EitScheduleFlag = " + eitScheduleFlag);
                int eitPresentFollowingFlag=this.getEitPresentFollowingFlag(transportStreamId,serviceId);
                Log.d(TAG,"Get Sdt Data EitPresentFollowingFlag = " + eitPresentFollowingFlag);
                int runningStatus=this.getRunningStatus(transportStreamId,serviceId);
                Log.d(TAG,"Get Sdt Data RunningStatus = " + runningStatus);
                int freeCaMode=this.getFreeCaMode(transportStreamId,serviceId);
                Log.d(TAG,"Get Sdt Data FreeCaMode = " + freeCaMode);
                int serviceType=this.getServiceType(transportStreamId,serviceId);
                Log.d(TAG,"Get Sdt Data ServiceType = " + serviceType);
                String serviceProviderName=this.getServiceProviderName(transportStreamId,serviceId);
                Log.d(TAG,"Get Sdt Data ServiceProviderName = " + serviceProviderName);
                String serviceName=this.getServiceName(transportStreamId,serviceId);
                Log.d(TAG,"Get Sdt Data ServiceName = " + serviceName);
                Log.d(TAG,"=======================================");
            }
        }
    }
}
