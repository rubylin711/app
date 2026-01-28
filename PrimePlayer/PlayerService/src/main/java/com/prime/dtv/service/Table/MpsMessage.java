package com.prime.dtv.service.Table;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.service.Util.CryptoUtils;
import com.prime.dtv.service.Util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MpsMessage {
    private static final String TAG = "MpsMessage";
    public static final byte DIVISION_CODE_LENGTH = 10;
    public static final byte GRPBIT_MASK_LENGTH = 16;


    public static final byte REFRESH_DATA = 0;
    public static final byte CLEAN_DATA = 1;
    public static final byte ZIP_CODE_LENGTH = 6;
    public static final byte STB_MODEL_LENGTH = 4;
    public static final int COMMAND_REFRESH_CAS_DATA = 0xA001;

    public static final int TARGET_TYPE_ALL = 0;
    public static final int TARGET_TYPE_DEVICE_ID = 1;
    public static final int TARGET_TYPE_ZIPCODE = 2;
    public static final int TARGET_TYPE_GRPBIT_MASK = 3;

    private int mSignatureType;
    private int mSignatureTargetLength;
    private long mTimestamp;
    private int mMpsDataUnitCount; //1~20
    private List<MpsDataUnit> mMpsDataUnitList = new ArrayList<MpsDataUnit>();

    public int getSignatureType() {
        return mSignatureType;
    }
    public int getSignatureTargetLength() {
        return mSignatureTargetLength;
    }
    public long getTimestamp() {
        return mTimestamp;
    }


    public int getNumOfMpsDataUnit() {
        return mMpsDataUnitList.size();
    }
    public MpsDataUnit getMpsDataUnitByIndex(int i) {
        if(i >= mMpsDataUnitList.size())
            return null;
        else
            return mMpsDataUnitList.get(i);
    }

    public class MpsData{
//        public int mCommandType; //2 bytes
//        public int mCommandLength; //2 bytes
//        public byte[] mZipCode= new byte[ZIP_CODE_LENGTH]; // 6 bytes
//        public byte[] mStbModel= new byte[STB_MODEL_LENGTH]; // 4 bytes
//        public int mDelete;
//        public int mMaxDelaySec; // 2 bytes
//        public int commandType;
        public byte[] commandData;
    }

    public class DeviceId{
        public int deviceIdLen;
        public byte[] deviceId;
    }

    public class DivisionCode{
        public byte[] divisionCode;//=new byte[10];
    }

    public class GrpbitMask{
        public byte[] grpbitMask;//=new byte[16];
    }

    public class MpsDataUnit {

        public final static long CNS_IRD_CODE_CA_MAIL    =   55000;
        public final static long CNS_IRD_CODE_EMERGENCY_ALARM    =   55010;
        public final static long CNS_IRD_CODE_IRC_COMMAND    =   55020;
        public int mTargeType;
        public DeviceId mDeviceId;
        public DivisionCode mDivisionCode;
        public GrpbitMask mGrpbitMask;
        public long mIrdCode;
        public long mSequenceNumber;
        public int mEncAlgorithm;
        public int mEncDataLen;
        public MpsData mMpsData;

        public MpsDataUnit(){
            mDeviceId=null;
            mDivisionCode=null;
            mGrpbitMask=null;
            mMpsData=null;
        }
    }

    public MpsMessage(byte[] data, int lens) {
        if(lens > 8) {
            int total_len = 0, index = 0, signatureLen = 0;
            byte[] sourceData;
            byte[] signatureData;
            mSignatureType = Utils.getInt(data, index, 1, Utils.MASK_8BITS);
            mSignatureTargetLength = Utils.getInt(data, index + 1, 2, Utils.MASK_16BITS);
            //LogUtils.d("FFFFFF lens = "+lens+" mSignatureTargetLength = "+mSignatureTargetLength);
            if (mSignatureTargetLength + 3 < lens) {
                sourceData = new byte[mSignatureTargetLength];
                index = index + 3;
                System.arraycopy(data, index, sourceData, 0, mSignatureTargetLength);
                signatureLen = lens - (mSignatureTargetLength + index);
                signatureData = new byte[signatureLen];
                System.arraycopy(data, index + mSignatureTargetLength, signatureData, 0, signatureLen);
                CryptoUtils cryptoUtils = new CryptoUtils();
                if (cryptoUtils.verifyECDSASignature(sourceData, signatureData) == true) {
                    LogUtils.d("verifyECDSASignature Ok");
                    int i;
                    //LogUtils.d("LLLLLL Private Data Byte = " + Arrays.toString(sourceData));
                    //LogUtils.d("LLLLLL lens = "+lens+" mSignatureType = "+mSignatureType);
                    //LogUtils.d("LLLLLL mSignatureTargetLength = "+mSignatureTargetLength+" Private Data Byte = " + bytesToHexWithSpaces(sourceData));
                    //LogUtils.d("LLLLLL signatureLen = "+signatureLen+" Signature Data Byte = " + bytesToHexWithSpaces(signatureData));
    
                    mTimestamp = Utils.getInt(data, index, 4, Utils.MASK_32BITS);
                    //LogUtils.d("LLLLLL mTimestamp = "+mTimestamp);
                    mMpsDataUnitCount = Utils.getInt(data, index + 4, 1, Utils.MASK_8BITS);
                    //LogUtils.d("LLLLLL mMpsDataUnitCount = "+mMpsDataUnitCount);
                    index = index + 5;
                    total_len = index;
                    for (i = 0; i < mMpsDataUnitCount; i++) {
                        //LogUtils.d("LLLLLL lens = "+lens+" total_len = "+total_len);
                        if (lens > total_len) {
                            MpsDataUnit mpsDataUnit = new MpsDataUnit();
                            mpsDataUnit.mTargeType = Utils.getInt(data, index, 1, Utils.MASK_8BITS);
                            //LogUtils.d("LLLLLL mpsDataUnit.mTargeType = "+mpsDataUnit.mTargeType);
                            index = index + 1;
                            mpsDataUnit.mIrdCode = Utils.getInt(data, index, 4, Utils.MASK_32BITS);
                            //LogUtils.d("LLLLLL mpsDataUnit.mIrdCode = "+mpsDataUnit.mIrdCode);
                            index = index + 4;
                            mpsDataUnit.mSequenceNumber = Utils.getInt(data, index, 4, Utils.MASK_32BITS);
                            //LogUtils.d("LLLLLL mpsDataUnit.mSequenceNumber = "+Long.toHexString(mpsDataUnit.mSequenceNumber));
                            index = index + 4;

                            if (mpsDataUnit.mTargeType == TARGET_TYPE_DEVICE_ID) {
                                mpsDataUnit.mDeviceId = new DeviceId();
                                mpsDataUnit.mDeviceId.deviceIdLen = Utils.getInt(data, index, 1, Utils.MASK_8BITS);
                                index = index + 1;
                                mpsDataUnit.mDeviceId.deviceId = new byte[mpsDataUnit.mDeviceId.deviceIdLen];
                                System.arraycopy(data, index, mpsDataUnit.mDeviceId.deviceId, 0, mpsDataUnit.mDeviceId.deviceIdLen);
                                index = index + mpsDataUnit.mDeviceId.deviceIdLen;
                                //LogUtils.d("LLLLLL DeviceId Len = "+mpsDataUnit.mDeviceId.deviceIdLen+" DeviceId = " + bytesToHexWithSpaces(mpsDataUnit.mDeviceId.deviceId));
                            } else if (mpsDataUnit.mTargeType == TARGET_TYPE_ZIPCODE) {
                                mpsDataUnit.mDivisionCode = new DivisionCode();
                                mpsDataUnit.mDivisionCode.divisionCode = new byte[DIVISION_CODE_LENGTH];
                                System.arraycopy(data, index, mpsDataUnit.mDivisionCode.divisionCode, 0, DIVISION_CODE_LENGTH);
                                index = index + DIVISION_CODE_LENGTH;
                                //LogUtils.d("LLLLLL DivisionCode = "+ bytesToHexWithSpaces(mpsDataUnit.mDivisionCode.divisionCode));
                            } else if (mpsDataUnit.mTargeType == TARGET_TYPE_GRPBIT_MASK) {
                                mpsDataUnit.mGrpbitMask = new GrpbitMask();
                                mpsDataUnit.mGrpbitMask.grpbitMask = new byte[GRPBIT_MASK_LENGTH];
                                System.arraycopy(data, index, mpsDataUnit.mGrpbitMask.grpbitMask, 0, GRPBIT_MASK_LENGTH);
                                index = index + GRPBIT_MASK_LENGTH;
                                //LogUtils.d("LLLLLL GrpbitMask = "+ bytesToHexWithSpaces(mpsDataUnit.mGrpbitMask.grpbitMask));
                            }
                            mpsDataUnit.mEncAlgorithm = Utils.getInt(data, index, 1, Utils.MASK_8BITS);
                            //LogUtils.d("LLLLLL mpsDataUnit.mEncAlgorithm = "+ mpsDataUnit.mEncAlgorithm);
                            index = index + 1;
                            mpsDataUnit.mEncDataLen = Utils.getInt(data, index, 2, Utils.MASK_16BITS);
//                            LogUtils.d("LLLLLL mpsDataUnit.mEncDataLen = "+ mpsDataUnit.mEncDataLen);
                            index = index + 2;
                            if (mpsDataUnit.mEncAlgorithm == 1) {
                                byte[] tmp_data = new byte[mpsDataUnit.mEncDataLen];
                                byte[] tmp_data2;
                                int index_tmp=0;
                                System.arraycopy(data, index, tmp_data, 0, mpsDataUnit.mEncDataLen);
                                index = index + mpsDataUnit.mEncDataLen;
                                //LogUtils.d("LLLLLL Encrypted Data = " + bytesToHexWithSpaces(tmp_data));

/*
                                byte[] text_data;
                                byte[] text_data2;
                                byte[] text_data3;
                                text_data = new byte[mpsDataUnit.mEncDataLen];
                                System.arraycopy(tmp_data, 0, text_data, 0, mpsDataUnit.mEncDataLen);
                                LogUtils.d("LLLLLL Original Test Data = " + bytesToHexWithSpaces(text_data));
                                text_data2 = cryptoUtils.encryptAES(text_data);
                                LogUtils.d("LLLLLL Encrypt Test Data = " + bytesToHexWithSpaces(text_data2));
                                text_data3 = cryptoUtils.decryptAES(text_data2);
                                LogUtils.d("LLLLLL Decrypt Test Data = " + bytesToHexWithSpaces(text_data3));
*/

                                tmp_data2 = cryptoUtils.decryptAES(tmp_data);
                                if (tmp_data2.length == 0) {
                                    continue;
                                }
                                //LogUtils.d("LLLLLL Decrypt Data = " + bytesToHexWithSpaces(tmp_data2));
                                mpsDataUnit.mMpsData = new MpsData();
                                mpsDataUnit.mMpsData.commandData = tmp_data2;
                                //index=index+index_tmp;
                            }
                            else {
                                LogUtils.d("mps data not encrypted ! ignore !");
                            }
                            mMpsDataUnitList.add(mpsDataUnit);
                            total_len = index;
                        }
                    }
                    //printfMessage();
                } else {
                    LogUtils.d(" verifyECDSASignature Not Ok");
                }
            }
            else{
                LogUtils.d(" MpsMessage content unreasonable");
            }
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexWithSpaces(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2 + bytes.length - 1];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            if (j < bytes.length - 1) {
                //if(j%2 == 1) {
                    hexChars[j * 3 + 2] = ' '; 
                //}
            }
        }
        return new String(hexChars);
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0'); // 確保每個字節都是兩位數
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public static String convertTimestampToDate(long timestampInSeconds) {
        
        long timestampInMillis = timestampInSeconds * 1000;

        Date date = new Date(timestampInMillis);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    private void printfMessage(){
        int i;
        LogUtils.d("LLLLLL mpsMessage data start");
        LogUtils.d("LLLLLL mSignatureType = "+mSignatureType);
        LogUtils.d("LLLLLL mSignatureTargetLength = "+mSignatureTargetLength);
        LogUtils.d("LLLLLL mTimestamp = "+mTimestamp+" DataTime = "+convertTimestampToDate(mTimestamp));
        LogUtils.d("LLLLLL mMpsDataUnitCount = "+mMpsDataUnitCount);
        LogUtils.d("LLLLLL mMpsDataUnitList.size() = "+mMpsDataUnitList.size());
        for(i=0;i<mMpsDataUnitList.size();i++) {
            LogUtils.d("LLLLLL index = " + i);
            if (mMpsDataUnitList.get(i).mDeviceId != null) {
                LogUtils.d("LLLLLL deviceIdLen = " + mMpsDataUnitList.get(i).mDeviceId.deviceIdLen);
                LogUtils.d("LLLLLL deviceId = " + bytesToHexWithSpaces(mMpsDataUnitList.get(i).mDeviceId.deviceId));
            }
            if (mMpsDataUnitList.get(i).mDivisionCode != null) {
                LogUtils.d("LLLLLL divisionCode = " + bytesToHexWithSpaces(mMpsDataUnitList.get(i).mDivisionCode.divisionCode));
            }
            if (mMpsDataUnitList.get(i).mGrpbitMask != null) {
                LogUtils.d("LLLLLL divisionCode = " + bytesToHexWithSpaces(mMpsDataUnitList.get(i).mGrpbitMask.grpbitMask));
            }

            LogUtils.d("LLLLLL mIrdCode = " + mMpsDataUnitList.get(i).mIrdCode);

            LogUtils.d("LLLLLL mSequenceNumber = " + mMpsDataUnitList.get(i).mSequenceNumber);
            LogUtils.d("LLLLLL mEncAlgorithm = " + mMpsDataUnitList.get(i).mEncAlgorithm);
            LogUtils.d("LLLLLL mEncDataLen = " + mMpsDataUnitList.get(i).mEncDataLen);
            if (mMpsDataUnitList.get(i).mMpsData != null) {
                LogUtils.d("LLLLLL mMpsData.commandData = " + bytesToHexWithSpaces(mMpsDataUnitList.get(i).mMpsData.commandData));
            }
        }
    }
}
/*
MPS Message Format{ //One MPS message has N DataUnits.
    signature_type                                              1 uimsbf
    signature_target_length                                     2 uimsbf
    timestamp                                                   4 uimsbf
    mps_data_unit_count                                         1 uimsbf
    for (i = 0; i < mps_data_unit_count; i++) {
        target_type                                             1 uimsbf
        ird_code                                                4 bslbf
        sequence_number                                         4 uimsbf
        If (target_type == 0x00) {
            // all // nothing
        }
        else If (target_type == 0x01) { // device_id
            device_id_len                                       1 uimsbf
            device_id                                           device_id_len string
        }
        else if (target_type == 0x02) { // division_code
            division_code                                       10 string
        }
        else if (target_type == 0x03) { // grpbit_mask
            grpbit_mask                                         16 bslbf
        }
        enc_algorithm                                           1 uimsbf
        enc_data_len                                            1 uimsbf
        if (enc_data_len > 0) {
            (encrypted) MPS DataUnit                            enc_data_len predefined
        }
    }
    signature1                                                  variable bslbf
}

MPS DataUnit{
    data_len        1               uimsbf
    ird_data        ird_data_len    bslbf
}
*/