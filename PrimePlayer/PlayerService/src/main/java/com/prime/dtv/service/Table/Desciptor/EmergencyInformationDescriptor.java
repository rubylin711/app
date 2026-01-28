package com.prime.dtv.service.Table.Desciptor;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class EmergencyInformationDescriptor extends DescBase{
    private static final String TAG = "EmergencyInformationDescriptor";
    public List<EmergencyInformationSid> mEmergencyInformationSidList = new ArrayList<>();

    public EmergencyInformationDescriptor(byte[] data,int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data,int lens) {
        int i=0,j=0,areaCodeLength=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        for(i=0;i<(Length-1);i+=(4+areaCodeLength)) {
            EmergencyInformationSid emergencyInformationSid = new EmergencyInformationSid();
            emergencyInformationSid.ServiceId = (toUnsignedInt(data[2+i]) << 8) + toUnsignedInt(data[2+i+1]);// getInt(data, 2+i,2,MASK_16BITS);
            emergencyInformationSid.StartEndFlag = (toUnsignedInt(data[2+i+2]) & 0x80) >> 7;//getBits(data[2+i+2],1,1);
            emergencyInformationSid.SignalLevel = (toUnsignedInt(data[2+i+2]) & 0x40) >> 6;//getBits(data[2+i+2],2,1);
            emergencyInformationSid.Reserved = toUnsignedInt(data[2+i+2]) & 0x3f;//getInt(data, 2+i+2,1,MASK_6BITS);
            emergencyInformationSid.AreaCodeLength = toUnsignedInt(data[2+i+3]);
            LogPrint(TAG,emergencyInformationSid.toString());
            for(j=0;j<(emergencyInformationSid.AreaCodeLength-1);j+=2) {
                EmergencyInformationSid.EmergencyInformationAreaCode emergencyInformationAreaCode = emergencyInformationSid.new EmergencyInformationAreaCode();
                emergencyInformationAreaCode.FirstHex = (toUnsignedInt(data[2+i+4+j]) & 0xe0) >> 5;
                emergencyInformationAreaCode.SecondHex = (toUnsignedInt(data[2+i+4+j]) & 0x1e) >> 1;
                emergencyInformationAreaCode.ThirdHex = ((toUnsignedInt(data[2+i+4+j]) & 0x01) << 4) + ((toUnsignedInt(data[2+i+4+j+1]) & 0xf0) >> 4);
                LogPrint(TAG,emergencyInformationAreaCode.toString());
                emergencyInformationAreaCode.Reserved = toUnsignedInt(data[2+i+4+j+1]) & 0x0f;
                emergencyInformationSid.mEmergencyInformationAreaCodeList.add(emergencyInformationAreaCode);
            }
            mEmergencyInformationSidList.add(emergencyInformationSid);
        }
    }

    public List<EmergencyInformationSid> getemergList() {
        return mEmergencyInformationSidList;
    }

    public class EmergencyInformationSid {
        public int ServiceId;
        public int StartEndFlag;
        public int SignalLevel;
        public int Reserved;
        public int AreaCodeLength;
        public List<EmergencyInformationAreaCode> mEmergencyInformationAreaCodeList = new ArrayList<>();

        public String toString() {
            return (getClass().getName() + " "
                    + "ServiceId = " + ServiceId + " "
                    + "StartEndFlag = " + StartEndFlag + " "
                    + "SignalLevel = " + SignalLevel + " "
                    + "Reserved = " + Reserved + " "
                    + "AreaCodeLength = " + AreaCodeLength + " ");
        }

        public class EmergencyInformationAreaCode {
            public int FirstHex;
            public int SecondHex;
            public int ThirdHex;
            public int Reserved;

            public String toString() {
                return (getClass().getName() + " "
                        + "FirstHex = " + FirstHex + " "
                        + "SecondHex = " + SecondHex + " "
                        + "ThirdHex = " + ThirdHex + " "
                        + "Reserved = " + Reserved + " ");
            }
        }
    }
}
