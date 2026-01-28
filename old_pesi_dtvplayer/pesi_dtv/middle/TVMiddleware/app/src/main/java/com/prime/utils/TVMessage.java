package com.prime.utils;

import android.app.Service;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ethan_lin on 2017/10/30.
 */

public class TVMessage implements Parcelable {
    private static final String TAG = "TVMessage";

    private int type;
    private int flag;

    public static final int FLAG_SCAN = 1;
    public static final int TYPE_SCAN_BEGIN = 1000;
    public static final int TYPE_SCAN_PROCESS = 1001;
    public static final int TYPE_SCAN_END = 1002;

    public static final int FLAG_EPG = 2;
    public static final int TYPE_EPG_UPDATE = 2001;

    //Scan process data
    private int serviceId;
    private int serviceLCN;
    private int serviceType;
    private String serviceName;
    private int CAFlag;
    private int AlreadyScanedTpNum;
    private int serviceCHNum;
    //Scan result data
    private int total_tv;
    private int total_ratio;


    protected TVMessage(Parcel in) {
        readFromParecl(in);
    }

    public static final Creator<TVMessage> CREATOR = new Creator<TVMessage>() {
        @Override
        public TVMessage createFromParcel(Parcel in) {
            return new TVMessage(in);
        }

        @Override
        public TVMessage[] newArray(int size) {
            return new TVMessage[size];
        }
    };

    public TVMessage(int Flag, int Type) {
        this.type = Type;
        this.flag = Flag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(flag);
        switch(flag){
            case FLAG_SCAN:{
                if(type == TYPE_SCAN_PROCESS){
                    dest.writeInt(serviceId);
                    dest.writeInt(serviceType);
                    dest.writeInt(serviceLCN);
                    dest.writeString(serviceName);
                    dest.writeInt(CAFlag);
                    dest.writeInt(AlreadyScanedTpNum);
                }
                if(type == TYPE_SCAN_END){
                    dest.writeInt(total_tv);
                    dest.writeInt(total_ratio);
                }
            }break;
            case FLAG_EPG:{
                if(type == TYPE_EPG_UPDATE){
                    dest.writeInt(serviceType);
                    dest.writeInt(serviceCHNum);
                }
            }
        }

    }

    public void readFromParecl(Parcel in){
        type = in.readInt();
        flag = in.readInt();
        switch (flag){
            case FLAG_SCAN:{
                if(type == TYPE_SCAN_PROCESS){
                    serviceId = in.readInt();
                    serviceType = in.readInt();
                    serviceLCN = in.readInt();
                    serviceName = in.readString();
                    CAFlag = in.readInt();
                    AlreadyScanedTpNum = in.readInt();
                }
                if(type == TYPE_SCAN_END){
                    total_tv = in.readInt();
                    total_ratio = in.readInt();
                }
            }break;
            case FLAG_EPG:{
                if(type == TYPE_EPG_UPDATE) {
                    serviceType = in.readInt();
                    serviceCHNum = in.readInt();
                }
            }break;
        }
    }

    public static TVMessage SetScanBegin(){
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_BEGIN);
        return msg;
    }
    public static TVMessage SetScanResultUpdate(int ServiceId,int serviceType,int lcn , String ServiceName, int CA_Flag, int TpCount){
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_PROCESS);

        msg.serviceId = ServiceId;
        msg.serviceType = serviceType;
        msg.serviceLCN = lcn;
        msg.serviceName = ServiceName;
        msg.CAFlag = CA_Flag;
        msg.AlreadyScanedTpNum = TpCount;

        return msg;
    }
    public static TVMessage SetScanEnd(int total_tv, int total_ratio){
        TVMessage msg = new TVMessage(FLAG_SCAN, TYPE_SCAN_END);
        msg.total_tv = total_tv;
        msg.total_ratio = total_ratio;
        return msg;
    }

    public static TVMessage SetEPGUpdate(int serviceType, int serviceCHNum){
        TVMessage msg = new TVMessage(FLAG_EPG, TYPE_EPG_UPDATE);
        msg.serviceType = serviceType;
        msg.serviceCHNum = serviceCHNum;
        return msg;
    }

    public int getMsgType(){
        return type;
    }
    public int getMsgFlag(){
        return flag;
    }

    public int getServiceId(){
        return serviceId;
    }
    public int getchannelLCN(){
        return serviceLCN;
    }
    public String getChannelName(){
        return serviceName;
    }
    public int getCAFlag(){
        return CAFlag;
    }
    public int getTotalTVNumber(){
        return total_tv;
    }
    public int getTotalRadioNumber(){
        return total_ratio;
    }
    public int getserviceType(){
        return serviceType;
    }
    public int getAlreadyScanedTpNum(){
        return AlreadyScanedTpNum;
    }

    public int getServiceCHNum() {
        return serviceCHNum;
    }
}
