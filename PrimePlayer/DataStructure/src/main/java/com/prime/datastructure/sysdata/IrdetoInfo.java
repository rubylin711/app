package com.prime.datastructure.sysdata;

import static com.prime.datastructure.sysdata.MailData.TAG;

import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IrdetoInfo {
    public static final int UC_SIZE_ERRORCODE_FOR_SMP = 2;
    public static final int UC_SIZE_PRODUCT_ID = 2;
    public static final int UC_MAX_OPERATOR_COUNT = 8;
    public static final int UC_STATUS_LENGTH = 9;

    public static final int UC_EXTENDED_SERVICE_STATUS = 1;
    public static final int UC_STREAM_STATUS = 2;
    public static final int UC_PRODUCT_STATUS = 3;
    public static final int UC_OPERATOR_INFO = 4;
    public static final int UC_CDSN = 5;
    public static final int UC_NATIONALITY = 6;
    public static final int UC_ECM_EMM_COUNT = 7;
    public static final int UC_FLEXI_FLSH_STATUS = 8;
    public static final int UC_FLEXI_CORE_STATUS = 9;
    public static final int UC_SERIAL_NUMBER = 10;
    public static final int UC_TMS_DATA = 11;
    public static final int UC_BUILD_INFO = 12;
    public static final int UC_VERSION = 13;
    public static final int UC_CAPABILITY = 14;
    public static final int UC_MAIL_BY_UNID = 15;
    public static final int UC_MAILS = 16;
    public static final int UC_Attribute_BY_UNID = 17;
    public static final int UC_Attributes = 18;
    public static final int UC_Loader_Info = 19;

    public int mTag;
    public UcServiceStatus mUcServiceStatus = null;
    public UcStreamStatus mUcStreamStatus = null;
    public UcProductStatus mUcProductStatus = null;
    public UcOperatorInfo mUcOperatorInfo = null;
    public UcCDSN mUcCDSN = null;
    public UcNationality mUcNationality = null;
    public UcEcmEmmCount mUcEcmEmmCount = null;
    public UcFlexiFlshStatus mUcFlexiFlshStatus = null;
    public UcFlexiCoreStatus mUcFlexiCoreStatus = null;
    public UcSerialNumber mUcSerialNumber = null;
    public UcTmsData mUcTmsData = null;
    public UcBuildInfo mUcBuildInfo = null;
    public UcVersion mUcVersion = null;
    public UcCapability mUcCapability = null;
    public UcMailList mUcMailList = null;
    public UcAttributeList mUcAttributeList = null;
    public UcLoaderInfo mUcLoaderInfo = null;


    public IrdetoInfo(int i){
        switch(i) {
            case UC_EXTENDED_SERVICE_STATUS:
                mUcServiceStatus = new UcServiceStatus();
                mTag=i;
                break;
            case UC_STREAM_STATUS:
                mUcStreamStatus = new UcStreamStatus();
                mTag=i;
                break;
            case UC_PRODUCT_STATUS:
                mUcProductStatus = new UcProductStatus();
                mTag=i;
                break;
            case UC_OPERATOR_INFO:
                mUcOperatorInfo = new UcOperatorInfo();
                mTag=i;
                break;
            case UC_CDSN:
                mUcCDSN = new UcCDSN();
                mTag=i;
                break;
            case UC_NATIONALITY:
                mUcNationality = new UcNationality();
                mTag=i;
                break;
            case UC_ECM_EMM_COUNT:
                mUcEcmEmmCount = new UcEcmEmmCount();
                mTag=i;
                break;
            case UC_FLEXI_FLSH_STATUS:
                mUcFlexiFlshStatus = new UcFlexiFlshStatus();
                mTag=i;
                break;
            case UC_FLEXI_CORE_STATUS:
                mUcFlexiCoreStatus = new UcFlexiCoreStatus();
                mTag=i;
                break;
            case UC_SERIAL_NUMBER:
                mUcSerialNumber = new UcSerialNumber();
                mTag=i;
                break;
            case UC_TMS_DATA:
                mUcTmsData = new UcTmsData();
                mTag=i;
                break;
            case UC_BUILD_INFO:
                mUcBuildInfo = new UcBuildInfo();
                mTag=i;
                break;
            case UC_VERSION:
                mUcVersion = new UcVersion();
                mTag=i;
                break;
            case UC_CAPABILITY:
                mUcCapability = new UcCapability();
                mTag=i;
                break;
            case UC_MAIL_BY_UNID:
                mUcMailList = new UcMailList();
                mTag=i;
                break;
            case UC_MAILS:
                mUcMailList = new UcMailList();
                mTag=i;
                break;
            case UC_Attribute_BY_UNID:
                mUcAttributeList = new UcAttributeList();
                mTag=i;
                break;
            case UC_Attributes:
                mUcAttributeList = new UcAttributeList();
                mTag=i;
                break;
            case UC_Loader_Info:
                mUcLoaderInfo = new UcLoaderInfo();
                mTag=i;
                break;
        }
    }

    public class UcServiceStatus{
        public String mStatusMessage;
        public int mServiceHandle;
        public int mNonPVREnableFlag;
        public int mIsShareable;
        public int mIsSpeRemainingTimeValid;
        public int mIsSecureMediaPipelineUsed;
        public byte[] mErrorCodeForSMP=new byte[UC_SIZE_ERRORCODE_FOR_SMP];
        public byte[] mRemainingTimeStatusProductId=new byte[UC_SIZE_PRODUCT_ID];
        public int mRemainingTimeStatusRemainingTime;
        public UcExrendedServiceStatus mUcExrendedServiceStatus;

        public UcServiceStatus(){
            mUcExrendedServiceStatus = new UcExrendedServiceStatus();
        }
        public UcExrendedServiceStatus getUcExrendedServiceStatus(){return mUcExrendedServiceStatus;}

        public void setUcServiceStatus(String statusMessage,int serviceHandle,int nonPVREnableFlag,int isShareable,int isSpeRemainingTimeValid,
                        int isSecureMediaPipelineUsed,byte[] errorCodeForSMP,byte[] remainingTimeStatusProductId,int remainingTimeStatusRemainingTime){
            int i,limit;
            mStatusMessage = statusMessage;
            mServiceHandle = serviceHandle;
            mNonPVREnableFlag = nonPVREnableFlag;
            mIsShareable = isShareable;
            mIsSpeRemainingTimeValid = isSpeRemainingTimeValid;
            mIsSecureMediaPipelineUsed = isSecureMediaPipelineUsed;
            limit = errorCodeForSMP.length;
            if(limit > UC_SIZE_ERRORCODE_FOR_SMP)
                limit = UC_SIZE_ERRORCODE_FOR_SMP;
            for(i=0;i<limit;i++)
                mErrorCodeForSMP[i] = errorCodeForSMP[i];
            limit = remainingTimeStatusProductId.length;
            if(limit > UC_SIZE_PRODUCT_ID)
                limit = UC_SIZE_PRODUCT_ID;
            for(i=0;i<limit;i++)
                mRemainingTimeStatusProductId[i] = remainingTimeStatusProductId[i];
            mRemainingTimeStatusRemainingTime = remainingTimeStatusRemainingTime;
        }
    }
    public class UcExrendedServiceStatus{
        public int mIsValid;
        public int mOperatorCnt;
        public int[] mCaSystemID = new int[UC_MAX_OPERATOR_COUNT];
        public String[] mOperatorStatus = new String[UC_MAX_OPERATOR_COUNT];
        public int[] mIsSecureMediaPipelineUsed = new int[UC_MAX_OPERATOR_COUNT];
        byte[][] mErrorCodeForSMP = new byte[UC_MAX_OPERATOR_COUNT][UC_SIZE_ERRORCODE_FOR_SMP];
        public void setUcExrendedServiceStatus(int isValid,int operatorCnt,int[] caSystemID,String[] operatorStatus,
                                               int[] isSecureMediaPipelineUsed,byte[][] errorCodeForSMP){
            int i,j,limit1,limit2;
            mIsValid = isValid;
            mOperatorCnt = operatorCnt;
            limit1 = caSystemID.length;
            if(limit1 > UC_MAX_OPERATOR_COUNT)
                limit1 = UC_MAX_OPERATOR_COUNT;
            for(i=0;i<limit1;i++)
                mCaSystemID[i] = caSystemID[i];

            limit1 = operatorStatus.length;
            if(limit1 > UC_MAX_OPERATOR_COUNT)
                limit1 = UC_MAX_OPERATOR_COUNT;
            for(i=0;i<limit1;i++)
                mOperatorStatus[i] = operatorStatus[i];

            limit1 = isSecureMediaPipelineUsed.length;
            if(limit1 > UC_MAX_OPERATOR_COUNT)
                limit1 = UC_MAX_OPERATOR_COUNT;
            for(i=0;i<limit1;i++)
                mIsSecureMediaPipelineUsed[i] = isSecureMediaPipelineUsed[i];

            limit1=errorCodeForSMP.length;
            if(limit1 > UC_MAX_OPERATOR_COUNT)
                limit1 = UC_MAX_OPERATOR_COUNT;
            for(i=0;i<limit1;i++){
                limit2=errorCodeForSMP[i].length;
                if(limit2 > UC_SIZE_ERRORCODE_FOR_SMP)
                    limit2 = UC_SIZE_ERRORCODE_FOR_SMP;
                for(j=0;j<limit2;j++){
                    mErrorCodeForSMP[i][j] = errorCodeForSMP[i][j];
                }
            }
        }
    }
    public UcServiceStatus getUcServiceStatus(){return mUcServiceStatus;}

    public class UcStreamStatus{
        public int mStreamCount;
        public List<UcStreamStatusUnit> mUcStreamStatusUnit = new ArrayList<UcStreamStatusUnit>();


        public void setUcStreamStatusUnitCnt(int count){
            mStreamCount = count;
        }

        public void addUcServiceStatus(int componentCount,int isEcm,int count,String streamStatusMessage,int caStreamProtocolType,int caStreamProtocolPid,
                                            int caStreamstreamWebServiceAddressWebServiceType,int caStreamstreamWebServiceAddressLengthOfURL,
                                            String caStreamstreamWebServiceAddressAddressOfURL,int canRecord,int caSystemID){

            UcStreamStatusUnit ucStreamStatusUnit = new UcStreamStatusUnit();
            ucStreamStatusUnit.setComponentCount(componentCount);

            ucStreamStatusUnit.setComponentContent(isEcm,count,streamStatusMessage,caStreamProtocolType,caStreamProtocolPid,
                    caStreamstreamWebServiceAddressWebServiceType,caStreamstreamWebServiceAddressLengthOfURL,
                    caStreamstreamWebServiceAddressAddressOfURL,canRecord,caSystemID);

            mUcStreamStatusUnit.add(ucStreamStatusUnit);
        }
    }
    public class UcStreamStatusUnit{
        public int mIsEcm;
        public int mCount;
        public String mStreamStatusMessage;
        public int mCaStreamProtocolType;
        public int mCaStreamProtocolPid;
        public int mCaStreamstreamWebServiceAddressWebServiceType;
        public int mCaStreamstreamWebServiceAddressLengthOfURL;
        public String mCaStreamstreamWebServiceAddressAddressOfURL;
        public int mCanRecord;
        public int mCaSystemID;
        public int mComponentCount;
        public int[] mComponentCountProtocolType = new int[0];
        public int[] mComponentCountPid = new int[0];

        public void setComponentCount(int count){
            mComponentCount = count;
            mComponentCountProtocolType = new int[count];
            mComponentCountPid = new int[count];
        }
        public void setComponentContent(int isEcm,int count,String streamStatusMessage,int caStreamProtocolType,int caStreamProtocolPid,
                                        int caStreamstreamWebServiceAddressWebServiceType,int caStreamstreamWebServiceAddressLengthOfURL,
                                        String caStreamstreamWebServiceAddressAddressOfURL,int canRecord,int caSystemID){
            mIsEcm = isEcm;
            mCount = count;
            mStreamStatusMessage = streamStatusMessage;
            mCaStreamProtocolType = caStreamProtocolType;
            mCaStreamProtocolPid = caStreamProtocolPid;
            mCaStreamstreamWebServiceAddressWebServiceType = caStreamstreamWebServiceAddressWebServiceType;
            mCaStreamstreamWebServiceAddressLengthOfURL = caStreamstreamWebServiceAddressLengthOfURL;
            mCaStreamstreamWebServiceAddressAddressOfURL = caStreamstreamWebServiceAddressAddressOfURL;
            mCanRecord = canRecord;
            mCaSystemID = caSystemID;
        }
        public void setComponentByIdx(int index, int type, int pid){
            if(index < mComponentCount) {
                mComponentCountProtocolType[index] = type;
                mComponentCountPid[index] = pid;
            }
        }
    }
    public UcStreamStatus getUcStreamStatus(){return mUcStreamStatus;}

    public class UcProductStatus{
        public int mProductCount;
        public List<UcProductStatusUnit> mUcProductStatusUnit = new ArrayList<UcProductStatusUnit>();


        public void setUcProductStatusUnitCnt(int count){
            mProductCount = count;
        }

        public void addUcProductStatus(int sector_number,int entitled,int startingDate,int duration,int startingTimeInSeconds,int durationInSeconds,
                                       int productType,int caSystemID,int isPurchaseToOwn,int assetId,int sourceType,int[] productId){

            UcProductStatusUnit ucProductStatusUnit = new UcProductStatusUnit();

            ucProductStatusUnit.setComponentContent(sector_number,entitled,startingDate,duration,startingTimeInSeconds,durationInSeconds,
                    productType,caSystemID,isPurchaseToOwn,assetId,sourceType,productId);

            mUcProductStatusUnit.add(ucProductStatusUnit);
        }
    }
    public class UcProductStatusUnit{
        public int mSector_number;
        public int mEntitled;
        public int[] mProductId = new int[0];
        public int mStartingDate;
        public int mDuration;
        public int mStartingTimeInSeconds;
        public int mDurationInSeconds;
        public int mProductType;
        public int mCaSystemID;
        public int mIsPurchaseToOwn;
        public int mAssetId;
        public int mSourceType;

        public void setComponentContent(int sector_number,int entitled,int startingDate,int duration,int startingTimeInSeconds,
                                        int durationInSeconds,int productType,
                                        int caSystemID,int isPurchaseToOwn,int assetId,int sourceType,int[] productId){
            int i,len;
            mSector_number = sector_number;
            mEntitled = entitled;
            mStartingDate = startingDate;
            mDuration = duration;
            mStartingTimeInSeconds = startingTimeInSeconds;
            mDurationInSeconds = durationInSeconds;
            mProductType = productType;
            mCaSystemID = caSystemID;
            mIsPurchaseToOwn = isPurchaseToOwn;
            mAssetId = assetId;
            mSourceType = sourceType;
            len = productId.length;
            //Log.e("Allen test", "len" + len);

            mProductId = new int[len];
            for(i=0;i<len;i++) {
                mProductId[i] = productId[i];
                //Log.e("Allen test", "mProductId [" + i+ "]="+mProductId[i]);
            }
        }
    }
    public UcProductStatus getUcProductStatus(){return mUcProductStatus;}

    public class UcOperatorInfo{
        public int mOperatorCount;
        public List<UcOperatorInfoUnit> mUcOperatorInfoUnit = new ArrayList<UcOperatorInfoUnit>();


        public void setUcOperatorInfoUnitCnt(int count){
            mOperatorCount = count;
        }

        public void addUcOperatorInfo(int active,int activeCaSystemID,int caSystemIDStart,int caSystemIDEnd,String operatorName){

            UcOperatorInfoUnit ucOperatorInfoUnit = new UcOperatorInfoUnit();

            ucOperatorInfoUnit.setComponentContent(active,activeCaSystemID,caSystemIDStart,caSystemIDEnd,operatorName);

            mUcOperatorInfoUnit.add(ucOperatorInfoUnit);
        }
    }
    public class UcOperatorInfoUnit{
        public int mActive;
        public int mActiveCaSystemID;
        public int mCaSystemIDStart;
        public int mCaSystemIDEnd;
        public String mOperatorName;

        public void setComponentContent(int active,int activeCaSystemID,int caSystemIDStart,int caSystemIDEnd,String operatorName){
            mActive = active;
            mActiveCaSystemID = activeCaSystemID;
            mCaSystemIDStart = caSystemIDStart;
            mCaSystemIDEnd = caSystemIDEnd;
            mOperatorName = operatorName;

        }
    }
    public UcOperatorInfo getUcOperatorInfo(){return mUcOperatorInfo;}

    public class UcCDSN{
        public long mClientId;
        public int mCheckNum;

        public void setUcCDSN(long clientId,int checkNum){
            mClientId = clientId;
            mCheckNum = checkNum;
        }
    }
    public UcCDSN getUcCDSN(){return mUcCDSN;}

    public class UcNationality{
        public int mOperatorCount;
        public List<UcNationalityUnit> mUcNationalityUnit = new ArrayList<UcNationalityUnit>();


        public void setUcNationalityUnitCnt(int count){
            mOperatorCount = count;
        }

        public void addUcNationality(int caSystemID,byte[] nationalityData){

            UcNationalityUnit ucNationalityUnit = new UcNationalityUnit();

            ucNationalityUnit.setComponentContent(caSystemID,nationalityData);

            mUcNationalityUnit.add(ucNationalityUnit);
        }
    }
    public class UcNationalityUnit{
        public int mCaSystemID;
        public byte[] mNationalityData = new byte[0];


        public void setComponentContent(int caSystemID,byte[] nationalityData){
            mCaSystemID = caSystemID;
            mNationalityData = nationalityData;
        }
    }
    public UcNationality getUcNationality(){return mUcNationality;}

    public class UcEcmEmmCount{
        public int mOperatorCount;
        public List<UcEcmEmmCountUnit> mUcEcmEmmCountUnit = new ArrayList<UcEcmEmmCountUnit>();

        public void setUcEcmEmmCountUnitCnt(int count){
            mOperatorCount = count;
        }

        public void addUcEcmEmmCount(int caSystemID,int ecmCount,int emmCount){

            UcEcmEmmCountUnit ucEcmEmmCountUnit = new UcEcmEmmCountUnit();

            ucEcmEmmCountUnit.setComponentContent(caSystemID,ecmCount,emmCount);

            mUcEcmEmmCountUnit.add(ucEcmEmmCountUnit);
        }
    }
    public class UcEcmEmmCountUnit{
        public int mCaSystemID;
        public int mEcmCount;
        public int mEmmCount;

        public void setComponentContent(int caSystemID,int ecmCount,int emmCount){
            mCaSystemID = caSystemID;
            mEcmCount = ecmCount;
            mEmmCount = emmCount;
        }
    }
    public UcEcmEmmCount getUcEcmEmmCount(){return mUcEcmEmmCount;}

    public class UcFlexiFlshStatus{
        public String mSecureCoreListStatus;
        public String mPackagesDownloadProgressInfo;


        public void setUcFlexiFlshStatus(String secureCoreListStatus,String packagesDownloadProgressInfo) {
            mSecureCoreListStatus = secureCoreListStatus;
            mPackagesDownloadProgressInfo = packagesDownloadProgressInfo;
        }
    }
    public UcFlexiFlshStatus getFlexiFlshStatus(){return mUcFlexiFlshStatus;}

    public class UcFlexiCoreStatus{
        public String mImageStatus;
        public String mPackagesDownloadProgressInfo;


        public void setUcFlexiCoreStatus(String imageStatus,String packagesDownloadProgressInfo) {
            mImageStatus = imageStatus;
            mPackagesDownloadProgressInfo = packagesDownloadProgressInfo;
        }
    }
    public UcFlexiCoreStatus getFlexiCoreStatus(){return mUcFlexiCoreStatus;}

    public class UcSerialNumber{
        public int mOperatorCount;
        public List<UcSerialNumberUnit> mUcSerialNumberUnit = new ArrayList<UcSerialNumberUnit>();

        public void setUcSerialNumberUnitCnt(int count){
            mOperatorCount = count;
        }

        public void addUcSerialNumber(int caSystemID,byte[] serialNumberBytes){

            UcSerialNumberUnit ucSerialNumberUnit = new UcSerialNumberUnit();

            ucSerialNumberUnit.setComponentContent(caSystemID,serialNumberBytes);

            mUcSerialNumberUnit.add(ucSerialNumberUnit);
        }
    }
    public class UcSerialNumberUnit{
        public int mCaSystemID;
        public byte[] mSerialNumberBytes = new byte[0];

        public void setComponentContent(int caSystemID,byte[] serialNumberBytes){
            mCaSystemID = caSystemID;
            mSerialNumberBytes = serialNumberBytes;
        }
    }
    public UcSerialNumber getUcSerialNumber(){return mUcSerialNumber;}

    public class UcTmsData{
        public int mOperatorCount;
        public List<UcTmsDataUnit> mUcTmsDataUnit = new ArrayList<UcTmsDataUnit>();

        public void setUcTsDataUnitCnt(int count){
            mOperatorCount = count;
        }

        public void addUcTmsData(int caSystemID,byte[] tmsData){

            UcTmsDataUnit ucTmsDataUnit = new UcTmsDataUnit();

            ucTmsDataUnit.setComponentContent(caSystemID,tmsData);

            mUcTmsDataUnit.add(ucTmsDataUnit);
        }
    }
    public class UcTmsDataUnit{
        public int mCaSystemID;
        public byte[] mTmsData = new byte[0];

        public void setComponentContent(int caSystemID,byte[] tmsData){
            mCaSystemID = caSystemID;
            mTmsData = tmsData;
        }
    }
    public UcTmsData getUcTmsData(){return mUcTmsData;}


    public class UcBuildInfo{
       public List<UcBufferUnit>  mBuildInfoUnit = new ArrayList<UcBufferUnit>();

        public void addUcBuildInfo(int length,byte[] bytes){

            UcBufferUnit ucBufferUnit = new UcBufferUnit();

            ucBufferUnit.setComponentContent(length,bytes);

            mBuildInfoUnit.add(ucBufferUnit);
        }
    }
    public UcBuildInfo getUcBuildInfo(){return mUcBuildInfo;}

    public class UcVersion{
        public List<UcBufferUnit> mVersionUnit = new ArrayList<UcBufferUnit>();

        public void addUcVersion(int length,byte[] bytes){

            UcBufferUnit ucBufferUnit = new UcBufferUnit();

            ucBufferUnit.setComponentContent(length,bytes);

            mVersionUnit.add(ucBufferUnit);
        }
    }
    public UcVersion getUcVersion(){return mUcVersion;}

    public class UcCapability{
        public List<UcBufferUnit> mUcCapabilityUnit = new ArrayList<UcBufferUnit>();

        public void addUcCapability(int length,byte[] bytes){

            UcBufferUnit ucBufferUnit = new UcBufferUnit();

            ucBufferUnit.setComponentContent(length,bytes);

            mUcCapabilityUnit.add(ucBufferUnit);
        }
    }
    public UcCapability getUcCapability(){return mUcCapability;}

    public class UcBufferUnit{
        public int mLength;
        public byte[] mBytes = new byte[0];

        public void setComponentContent(int length,byte[] bytes){
            mLength = length;
            mBytes = bytes;
        }
    }

    public class UcMailList{
        public int mMailCount = 0;
        public List<UcMail> mMailList = new ArrayList<UcMail>();

        public void addUcMail(int unid,int kind,int type,int msgWatched,String text,int year,int month,int date,int hour,int minute,int second,int week){

            UcMail ucMail = new UcMail();

            ucMail.setUcMail(unid,kind,type,msgWatched,text,year,month,date,hour,minute,second,week);

            mMailList.add(ucMail);
            mMailCount=mMailCount+1;
        }
    }
    public class UcMail{
        public int mUniId;
        public int mKind;
        public int mType;
        public int mMsgWatched;
        public String mText;
        public int mYear;
        public int mMonth;
        public int mDate;
        public int mHour;
        public int mMinute;
        public int mSecond;
        public int mWeek;

        public void setUcMail(int unid,int kind,int type,int msgWatched,String text,int year,int month,int date,int hour,int minute,int second,int week) {
            mUniId = unid;
            mKind = kind;
            mType = type;
            mMsgWatched = msgWatched;
            mText = text;
            mYear = year;
            mMonth = month;
            mDate = date;
            mHour = hour;
            mMinute = minute;
            mSecond = second;
            mWeek = week;
        }
    }
    public UcMailList getUcMailList(){return mUcMailList;}

    public class UcAttributeList{
        public int mAttributeCount = 0;
        public List<UcAttribute> mAttributeList = new ArrayList<UcAttribute>();

        public void addUcAttribute(int index,int msgWatched,String text,int year,int month,int date,int hour,int minute,int second,int week){

            UcAttribute ucAttribute = new UcAttribute();

            ucAttribute.setUcAttribute(index,msgWatched,text,year,month,date,hour,minute,second,week);

            mAttributeList.add(ucAttribute);
            mAttributeCount=mAttributeCount+1;
        }
    }
    public class UcAttribute{
        public int mIndex;
        public int mMsgWatched;
        public String mText;
        public int mYear;
        public int mMonth;
        public int mDate;
        public int mHour;
        public int mMinute;
        public int mSecond;
        public int mWeek;

        public void setUcAttribute(int index,int msgWatched,String text,int year,int month,int date,int hour,int minute,int second,int week) {
            mIndex = index;
            mMsgWatched = msgWatched;
            mText = text;
            mYear = year;
            mMonth = month;
            mDate = date;
            mHour = hour;
            mMinute = minute;
            mSecond = second;
            mWeek = week;
        }
    }
    public UcAttributeList getUcAttributeList(){return mUcAttributeList;}

    public class UcLoaderInfo{
        public int mManufacturerId; //unsigned short
        public int mHardwareVersion;//unsigned short
        public int mVariant;//unsigned short
        public int mSystemId;//unsigned short
        public int mKeyVersion;//unsigned short
        public int mSignatureVersion;//unsigned short
        public int mDownloadSequenceNumber;//unsigned short

        public void setUcLoaderInfo(int ManufacturerId,int HardwareVersion,int Variant,int SystemId,int KeyVersion,int SignatureVersion,int DownloadSequenceNumber) {
            mManufacturerId = ManufacturerId;
            mHardwareVersion = HardwareVersion;
            mVariant = Variant;
            mSystemId = SystemId;
            mKeyVersion = KeyVersion;
            mSignatureVersion = SignatureVersion;
            mDownloadSequenceNumber = DownloadSequenceNumber;
        }
    }
    public UcLoaderInfo getLoaderInfo(){return mUcLoaderInfo;}


    public static String getListUcBufferUnitAsString(List<UcBufferUnit> list) {
        StringBuilder result = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            for (UcBufferUnit unit : list) {
                // 這裡假設 mBytes 內容是UTF-8 編碼的字串
                String str = new String(unit.mBytes, StandardCharsets.UTF_8);
                result.append(str);
            }
        }
        result.append("\0");
        return result.toString();
    }

    public static String getListUcNationalityUnitAsString(List<UcNationalityUnit> list, int count) {
        StringBuilder result = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < min(list.size(), count); i++) {
                // 這裡假設 mBytes 內容是UTF-8 編碼的字串
                UcNationalityUnit unit = list.get(i);
                String str = new String(unit.mNationalityData, StandardCharsets.UTF_8);
                result.append(str);
            }
        }
        result.append("\0");
        return result.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String getListUcSerialNumberUnitAsString(List<UcSerialNumberUnit> list, int count) {
        StringBuilder result = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < count; i++) {
                UcSerialNumberUnit unit = list.get(i);
                if (i != 0)
                    result.append("\n");
                for (byte b : unit.mSerialNumberBytes) {
                    result.append(String.format("%d", Byte.toUnsignedInt(b)));
                }
            }
        }
        result.append("\0");
        return result.toString();
    }
    public static String getListUcTmsDataUnitAsString(List<UcTmsDataUnit> list, int count) {
        StringBuilder result = new StringBuilder();
        if (list != null && !list.isEmpty()) {
            Log.d(TAG, "getListUcTmsDataUnitAsString list size = " + list.size() + ", count = " + count);
            for (int i = 0; i < count; i++) {
                UcTmsDataUnit unit = list.get(i);
                if (i != 0)
                    result.append("\n");
                for (byte b : unit.mTmsData) {
                    result.append(String.format("%02x ", b));
                }
            }
        }
        result.append("\0");
        return result.toString();
    }

}
