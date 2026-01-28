package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.datastructure.sysdata.IrdetoInfo;

public class IrdetoModule {
    private static final String TAG = "IrdetoModule";
    private static final int CMX_IRDETO_BASE = PrimeDtvMediaPlayer.CMD_Base + 0x1800;
    //IRDETO
    private static final int CMD_IRDETO_PARSE_TABLE = CMX_IRDETO_BASE + 0x01;
    private static final int CMD_IRDETO_GET_EMM_STATUS = CMX_IRDETO_BASE + 0x02;
    private static final int CMD_IRDETO_GET_ECM_STATUS = CMX_IRDETO_BASE + 0x03;
    private static final int CMD_IRDETO_GET_LOADER_STATUS = CMX_IRDETO_BASE + 0x04;
    private static final int CMD_IRDETO_GET_PRODUCT_STATUS = CMX_IRDETO_BASE + 0x05;
    private static final int CMD_IRDETO_GET_CLIENT_STATUS = CMX_IRDETO_BASE + 0x06;
    private static final int CMD_IRDETO_SET_OTA_USER_REPLY = CMX_IRDETO_BASE + 0x07;
    private static final int CMD_IRDETO_SET_MONITOR_SWITCH = CMX_IRDETO_BASE + 0x08;
    private static final int CMD_IRDETO_GET_EXTENDED_SERVICES_STATUS = CMX_IRDETO_BASE + 0x09;
    private static final int CMD_IRDETO_GET_STREAM_STATUS = CMX_IRDETO_BASE + 0x0A;
    private static final int CMD_IRDETO_GET_SERVICES_STATUS = CMX_IRDETO_BASE + 0x0B;
    private static final int CMD_IRDETO_GET_PRODUCT_LIST = CMX_IRDETO_BASE + 0x0C;
    private static final int CMD_IRDETO_ALL_SERVICE_STOP = CMX_IRDETO_BASE + 0x0D;
    private static final int CMD_IRDETO_GET_CA_SYSTEM_ID = CMX_IRDETO_BASE + 0x0E;
    private static final int CMD_IRDETO_GET_CDSN = CMX_IRDETO_BASE + 0x0F;
    private static final int CMD_IRDETO_GET_NATIONALITY = CMX_IRDETO_BASE + 0x10;
    private static final int CMD_IRDETO_GET_CSSN = CMX_IRDETO_BASE + 0x11;
    private static final int CMD_IRDETO_GET_ECM_EMM_COUNT = CMX_IRDETO_BASE + 0x12;
    private static final int CMD_IRDETO_GET_FLEXI_FLSH_STATUS = CMX_IRDETO_BASE + 0x13;
    private static final int CMD_IRDETO_GET_FLEXI_CORE_STATUS = CMX_IRDETO_BASE + 0x14;
    private static final int CMD_IRDETO_GET_SERIAL_NUMBER = CMX_IRDETO_BASE + 0x15;
    private static final int CMD_IRDETO_GET_TMS_DATA = CMX_IRDETO_BASE + 0x16;

    private static final int CMD_IRDETO_GET_BUILD_INFO = CMX_IRDETO_BASE + 0x17;
    private static final int CMD_IRDETO_GET_SECURE_TYPE = CMX_IRDETO_BASE + 0x18;
    private static final int CMD_IRDETO_GET_LOCK_ID = CMX_IRDETO_BASE + 0x19;
    private static final int CMD_IRDETO_GET_VERSION = CMX_IRDETO_BASE + 0x1A;
    private static final int CMD_IRDETO_GET_CAPABILITY = CMX_IRDETO_BASE + 0x1B;

    private static final int CMD_IRDETO_GET_MAIL_BY_UNID = CMX_IRDETO_BASE + 0x1C;
    private static final int CMD_IRDETO_GET_MAIL_TOTAL_NUMBER = CMX_IRDETO_BASE + 0x1D;
    private static final int CMD_IRDETO_GET_MAILES = CMX_IRDETO_BASE + 0x1E;
    private static final int CMD_IRDETO_SET_MAIL_TO_READ_BY_UNID = CMX_IRDETO_BASE + 0x1F;
    private static final int CMD_IRDETO_DELETE_MAIL_BY_UNID = CMX_IRDETO_BASE + 0x20;
    private static final int CMD_IRDETO_GET_ATTRIB_BY_UNID = CMX_IRDETO_BASE + 0x21;
    private static final int CMD_IRDETO_GET_ATTRIB_TOTAL_NUMBER = CMX_IRDETO_BASE + 0x22;
    private static final int CMD_IRDETO_GET_ATTRIBUTES = CMX_IRDETO_BASE + 0x23;
    private static final int CMD_IRDETO_SET_ATTRIB_TO_READ_BY_INDEX = CMX_IRDETO_BASE + 0x24;
    private static final int CMD_IRDETO_DELETE_ATTRIB_BY_INDEX = CMX_IRDETO_BASE + 0x25;
    private static final int CMD_IRDETO_DELETE_ALL_MAILS = CMX_IRDETO_BASE + 0x26;
    private static final int CMD_IRDETO_DELETE_ALL_ATTRIBUTES = CMX_IRDETO_BASE + 0x27;

    private static final int CMD_IRDETO_GET_LOADER_INFO = CMX_IRDETO_BASE + 0x28;
    private static final int CMD_IRDETO_SET_TOKEN = CMX_IRDETO_BASE + 0x29;


    public static final int CAT_TABLE_TAG = 0;
    public static final int PMT_TABLE_TAG = 1;
    public static final int FORCE_PMT_TABLE_TAG = 2;

    public static final int EMM_SWITCH_TAG = 0;
    public static final int ECM_SWITCH_TAG = 1;

    public static final long IRDETO_KEY_TOKEN = 100;

    public int send_table_rowdata(long channelId,int serviceId,int transportStreamId,int originalNetworkId,int type,byte[] data, int lens) {
        if(((type == CAT_TABLE_TAG) || (type == PMT_TABLE_TAG) || (type == FORCE_PMT_TABLE_TAG)) == false){
            Log.e(TAG, "table is not cat or pmt");
            return -1;
        }
        String strType = "PMT_TABLE_TAG";
        switch(type){
            case CAT_TABLE_TAG:
                strType = "CAT_TABLE_TAG";
                break;
            case PMT_TABLE_TAG:
                strType = "PMT_TABLE_TAG";
                break;
            case FORCE_PMT_TABLE_TAG:
                strType = "FORCE_PMT_TABLE_TAG";
                break;
        }
        Log.d(TAG,"type = "+strType+" ,TpId="+(channelId>>16)+",ChannelId="+channelId);
        Log.d(TAG,"ServiceId="+serviceId+",TransportStreamId="+transportStreamId+",OriginalNetworkId="+originalNetworkId);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_PARSE_TABLE);
        request.writeInt((int)channelId);

        request.writeInt((int)serviceId);
        request.writeInt((int)transportStreamId);
        request.writeInt((int)originalNetworkId);

        request.writeInt(type);
        request.writeInt(lens);
        request.writeByteArray(data);


        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "send_table_rowdata fail, ret = " + ret);
        }
        else{
            Log.d(TAG, "send_table_rowdata success");
        }

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int get_emm_status() // connie 20180903 for VMX -s
    {
        Log.d(TAG, "get_emm_status: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_EMM_STATUS);
        request.writeInt(CMX_IRDETO_BASE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_emm_status ok");
        } else
            Log.e(TAG, "get_emm_status failed, ret: " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int get_ecm_status() // connie 20180903 for VMX -s
    {
        Log.d(TAG, "get_ecm_status: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_ECM_STATUS);
        request.writeInt(CMX_IRDETO_BASE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_ecm_status ok");
        }
        else
            Log.e(TAG, "get_ecm_status failed, ret: " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int get_loader_status() // connie 20180903 for VMX -s
    {
        Log.d(TAG, "get_loader_status: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_LOADER_STATUS);
        request.writeInt(CMX_IRDETO_BASE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_loader_status ok");
        } else
            Log.e(TAG, "get_loader_status failed, ret: " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int get_product_status() // connie 20180903 for VMX -s
    {
        Log.d(TAG, "get_product_status: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_PRODUCT_STATUS);
        request.writeInt(CMX_IRDETO_BASE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_product_status ok");
        } else
            Log.e(TAG, "get_product_status failed, ret: " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int get_client_status() // connie 20180903 for VMX -s
    {
        Log.d(TAG, "get_client_status: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_CLIENT_STATUS);
        request.writeInt(CMX_IRDETO_BASE);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_client_status ok");
        } else
            Log.e(TAG, "get_client_status failed, ret: " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_ota_user(int s32DataTmp) // connie 20180903 for VMX -s
    {
        Log.d(TAG, "set_ota_user: ");
        int s32Data=9999;
        if(s32DataTmp == 0)
            s32Data=9999;
        else
            s32Data=s32DataTmp;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_SET_OTA_USER_REPLY);
        request.writeInt(s32Data);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "set_ota_user ok");
        } else
            Log.d(TAG, "set_ota_user failed, ret: " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int cca_monitor_switch(int type,int enable) {
        if(((type == EMM_SWITCH_TAG) || (type == ECM_SWITCH_TAG)) == false){
            Log.e(TAG, "Wrong parameter type");
            return -1;
        }

        Log.d(TAG,"type="+type+",enable="+enable);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_SET_MONITOR_SWITCH);
        if(type == EMM_SWITCH_TAG)
            request.writeInt(1);
        else
            request.writeInt(0);
        if(enable == 1)
            request.writeInt(1);
        else
            request.writeInt(0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "set_ca_monitor_switch fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "set_ca_monitor_switch success");
        }

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public IrdetoInfo.UcServiceStatus cca_extended_get_service_status(int type) {
        IrdetoInfo irdetoInfo=null;
        if(((type == EMM_SWITCH_TAG) || (type == ECM_SWITCH_TAG)) == false){
            Log.e(TAG, "Wrong parameter type");
            return null;
        }

        Log.d(TAG,"type="+type);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_EXTENDED_SERVICES_STATUS);
        if(type == EMM_SWITCH_TAG)
            request.writeInt(1);
        else
            request.writeInt(0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_extended_get_service_status fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_extended_get_service_status success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            int i,j,len1,len2;

            String statusMessage;
            int serviceHandle;
            int nonPVREnableFlag;
            int isShareable;
            int isSpeRemainingTimeValid;
            int isSecureMediaPipelineUsed;
            byte[] errorCodeForSMP;//=new byte[UC_SIZE_ERRORCODE_FOR_SMP];
            byte[] remainingTimeStatusProductId;//=new byte[UC_SIZE_PRODUCT_ID];
            int remainingTimeStatusRemainingTime;

            int extendIsValid;
            int extendOperatorCnt;
            int[] extendCaSystemID;// = new int[UC_MAX_OPERATOR_COUNT];
            String[] extendOperatorStatus;// = new String[UC_MAX_OPERATOR_COUNT];
            int[] extendIsSecureMediaPipelineUsed;// = new int[UC_MAX_OPERATOR_COUNT];
            byte[][] extendErrorCodeForSMP;// = new byte[UC_MAX_OPERATOR_COUNT][UC_SIZE_ERRORCODE_FOR_SMP];

            statusMessage = reply.readString();
            serviceHandle = reply.readInt();
            nonPVREnableFlag = reply.readInt();

            extendIsValid = reply.readInt();
            extendOperatorCnt = reply.readInt();
            len1 = reply.readInt();
            extendCaSystemID = new int[len1];
            for(i=0;i<len1;i++)
                extendCaSystemID[i] = reply.readInt();

            len1 = reply.readInt();
            extendOperatorStatus = new String[len1];
            for(i=0;i<len1;i++)
                extendOperatorStatus[i] = reply.readString();

            len1 = reply.readInt();
            extendIsSecureMediaPipelineUsed = new int[len1];
            for(i=0;i<len1;i++)
                extendIsSecureMediaPipelineUsed[i] = reply.readInt();

            len1 = reply.readInt();
            len2 = reply.readInt();
            extendErrorCodeForSMP = new byte[len1][len2];
            for(i=0;i<len1;i++)
                for(j=0;j<len2;j++)
                    extendErrorCodeForSMP[i][j] = (byte)reply.readInt();

            isShareable = reply.readInt();
            isSpeRemainingTimeValid = reply.readInt();

            len1 = reply.readInt();
            remainingTimeStatusProductId = new byte[len1];
            for(i=0;i<len1;i++)
                remainingTimeStatusProductId[i] = (byte)reply.readInt();

            remainingTimeStatusRemainingTime = reply.readInt();
            isSecureMediaPipelineUsed = reply.readInt();
            len1 = reply.readInt();
            errorCodeForSMP = new byte[len1];
            for(i=0;i<len1;i++)
                errorCodeForSMP[i] = (byte)reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_EXTENDED_SERVICE_STATUS);

            irdetoInfo.mUcServiceStatus.setUcServiceStatus(statusMessage,serviceHandle,nonPVREnableFlag,isShareable,isSpeRemainingTimeValid,
            isSecureMediaPipelineUsed,errorCodeForSMP,remainingTimeStatusProductId,remainingTimeStatusRemainingTime);
            irdetoInfo.mUcServiceStatus.mUcExrendedServiceStatus.setUcExrendedServiceStatus(extendIsValid,extendOperatorCnt,extendCaSystemID,
                    extendOperatorStatus,extendIsSecureMediaPipelineUsed,extendErrorCodeForSMP);
        }

        request.recycle();
        reply.recycle();
        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcServiceStatus();
    }

    public IrdetoInfo.UcStreamStatus cca_get_stream_status(int type) {
        IrdetoInfo irdetoInfo=null;
        if(((type == EMM_SWITCH_TAG) || (type == ECM_SWITCH_TAG)) == false){
            Log.e(TAG, "Wrong parameter type");
            return null;
        }

        Log.d(TAG,"type="+type);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_STREAM_STATUS);
        if(type == EMM_SWITCH_TAG)
            request.writeInt(1);
        else
            request.writeInt(0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_stream_status fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_stream_status success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            int i,j,len1,len2;
            int StreamCount;
            int isEcm,count,protocolType,pid,webServiceType,lengthOfURL,canRecord,componentCount,caSystemID;
            String streamStatusMessage,addressOfURL;
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_STREAM_STATUS);



            StreamCount = reply.readInt();

            irdetoInfo.mUcStreamStatus.setUcStreamStatusUnitCnt(StreamCount);

            for(i=0;i<StreamCount;i++)
            {
                isEcm = reply.readInt();
                count = reply.readInt();
                streamStatusMessage = reply.readString();

                protocolType = reply.readInt();
                pid = reply.readInt();
                webServiceType = reply.readInt();
                lengthOfURL = reply.readInt();
                addressOfURL =  reply.readString();

                canRecord = reply.readInt();
                caSystemID = reply.readInt();
                componentCount = reply.readInt();
                irdetoInfo.mUcStreamStatus.addUcServiceStatus(componentCount,isEcm,count,streamStatusMessage,protocolType,pid,
                        webServiceType,lengthOfURL,addressOfURL,canRecord,caSystemID);
                for(j=0;j<componentCount;j++)
                {
                    int componentProtocolType,componentpid;
                    componentProtocolType = reply.readInt();
                    componentpid = reply.readInt();
                    irdetoInfo.mUcStreamStatus.mUcStreamStatusUnit.get(i).setComponentByIdx(j,componentProtocolType,componentpid);
                }
            }
        }

        request.recycle();
        reply.recycle();
        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcStreamStatus();
    }

    public byte[] cca_get_service_status(int type) {
        byte[] serviceStatus = null;//new byte[0];
        if(((type == EMM_SWITCH_TAG) || (type == ECM_SWITCH_TAG)) == false){
            Log.e(TAG, "Wrong parameter type");
            return serviceStatus;
        }

        Log.d(TAG,"type="+type);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_SERVICES_STATUS);
        if(type == EMM_SWITCH_TAG)
            request.writeInt(1);
        else
            request.writeInt(0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_service_status fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_service_status success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            int len = reply.readInt();
            serviceStatus = new byte[len];
            reply.readByteArray(serviceStatus);
        }

        request.recycle();
        reply.recycle();

        return serviceStatus;
    }

    public IrdetoInfo.UcProductStatus cca_get_product_status() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_PRODUCT_LIST);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_product_status fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_product_status success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int i, j, len;
            int ProductCount;
            int[] product_id;
            int sector_number, entitled, startingDate, duration, startingTimeInSeconds, durationInSeconds, productType, caSystemID, isPurchaseToOwn, assetId, sourceType;

            ProductCount = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_PRODUCT_STATUS);

            irdetoInfo.mUcProductStatus.setUcProductStatusUnitCnt(ProductCount);

            for (i = 0; i < ProductCount; i++) {
                sector_number = reply.readInt();
                entitled = reply.readInt();
                len = reply.readInt();
                product_id = new int[len];
                for (j = 0; j < len; j++)
                    product_id[j] = reply.readInt();
                startingDate = reply.readInt();
                duration = reply.readInt();
                startingTimeInSeconds = reply.readInt();
                durationInSeconds = reply.readInt();
                productType = reply.readInt();
                caSystemID = reply.readInt();
                isPurchaseToOwn = reply.readInt();
                assetId = reply.readInt();
                sourceType = reply.readInt();
                irdetoInfo.mUcProductStatus.addUcProductStatus(sector_number,entitled,startingDate,duration,startingTimeInSeconds,
                        durationInSeconds,productType,caSystemID,isPurchaseToOwn,assetId,sourceType,product_id);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcProductStatus();
    }

    public int cca_stop_service() {

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_ALL_SERVICE_STOP);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_stop_service fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_stop_service success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public IrdetoInfo.UcOperatorInfo cca_get_operator_info() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_CA_SYSTEM_ID);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_operator_info fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_operator_info success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int i, j, len;
            int OperatorCount;
            int active, activeCaSystemID, caSystemIDStart, caSystemIDEnd;
            String operatorName;

            OperatorCount = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_OPERATOR_INFO);

            irdetoInfo.mUcOperatorInfo.setUcOperatorInfoUnitCnt(OperatorCount);

            for (i = 0; i < OperatorCount; i++) {
                active = reply.readInt();
                activeCaSystemID = reply.readInt();
                caSystemIDStart = reply.readInt();
                caSystemIDEnd = reply.readInt();
                operatorName = reply.readString();
                irdetoInfo.mUcOperatorInfo.addUcOperatorInfo(active, activeCaSystemID, caSystemIDStart, caSystemIDEnd, operatorName);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcOperatorInfo();
    }

    public IrdetoInfo.UcCDSN cca_get_cdsn() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_CDSN);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_cdsn fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_cdsn success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            long clientId;
            int checkNum;
            int tmp;

            tmp = reply.readInt();
            clientId = tmp;
            tmp = reply.readInt();
            clientId = (clientId << 32) | tmp;
            checkNum = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_CDSN);
            irdetoInfo.mUcCDSN.setUcCDSN(clientId,checkNum);
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcCDSN();
    }

    public IrdetoInfo.UcNationality cca_get_nationality() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_NATIONALITY);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_nationality fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_nationality success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int i,len;
            int OperatorCount;
            int caSystemID;
            byte[] nationalityData = new byte[0];

            OperatorCount = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_NATIONALITY);

            irdetoInfo.mUcNationality.setUcNationalityUnitCnt(OperatorCount);

            for (i = 0; i < OperatorCount; i++) {
                caSystemID = reply.readInt();
                len = reply.readInt();
                nationalityData = new byte[len];
                reply.readByteArray(nationalityData);

                irdetoInfo.mUcNationality.addUcNationality(caSystemID, nationalityData);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcNationality();
    }

    public int cca_get_cssn() {
        int cssn=-1;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_CSSN);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_cssn fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_cssn success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            cssn = reply.readInt();
        }

        request.recycle();
        reply.recycle();

        return cssn;
    }

    public IrdetoInfo.UcEcmEmmCount cca_get_ecm_emm_count() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_ECM_EMM_COUNT);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_ecm_emm_count fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_ecm_emm_count success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int i;
            int OperatorCount;
            int caSystemID;
            int ecmCount;
            int emmCount;

            OperatorCount = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_ECM_EMM_COUNT);

            irdetoInfo.mUcEcmEmmCount.setUcEcmEmmCountUnitCnt(OperatorCount);

            for (i = 0; i < OperatorCount; i++) {
                caSystemID = reply.readInt();
                ecmCount = reply.readInt();
                emmCount = reply.readInt();

                irdetoInfo.mUcEcmEmmCount.addUcEcmEmmCount(caSystemID, ecmCount, emmCount);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcEcmEmmCount();
    }

    public IrdetoInfo.UcFlexiFlshStatus cca_get_flexi_flash_data() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_FLEXI_FLSH_STATUS);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_flexi_flash_data fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_flexi_flash_data success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            String secureCoreListStatus;
            String packagesDownloadProgressInfo;

            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_FLEXI_FLSH_STATUS);
            secureCoreListStatus = reply.readString();
            packagesDownloadProgressInfo = reply.readString();

            irdetoInfo.mUcFlexiFlshStatus.setUcFlexiFlshStatus(secureCoreListStatus,packagesDownloadProgressInfo);

        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getFlexiFlshStatus();
    }

    public IrdetoInfo.UcFlexiCoreStatus cca_get_flexi_core_data() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_FLEXI_CORE_STATUS);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_flexi_core_data fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_flexi_core_data success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            String imageStatus;
            String packagesDownloadProgressInfo;

            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_FLEXI_CORE_STATUS);
            imageStatus = reply.readString();
            packagesDownloadProgressInfo = reply.readString();
            irdetoInfo.mUcFlexiCoreStatus.setUcFlexiCoreStatus(imageStatus,packagesDownloadProgressInfo); //fixed crash

        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getFlexiCoreStatus();
    }

    public IrdetoInfo.UcSerialNumber cca_get_serial_number() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_SERIAL_NUMBER);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_serial_number fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_serial_number success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int i,len;
            int OperatorCount;
            int caSystemID;
            byte[] serialNumberBytes = new byte[0];

            OperatorCount = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_SERIAL_NUMBER);

            irdetoInfo.mUcSerialNumber.setUcSerialNumberUnitCnt(OperatorCount);

            for (i = 0; i < OperatorCount; i++) {
                caSystemID = reply.readInt();
                len = reply.readInt();
                serialNumberBytes = new byte[len];
                reply.readByteArray(serialNumberBytes);
                irdetoInfo.mUcSerialNumber.addUcSerialNumber(caSystemID, serialNumberBytes);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcSerialNumber();
    }

    public IrdetoInfo.UcTmsData cca_get_tms_data() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_TMS_DATA);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_tms_data fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_tms_data success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int i,len;
            int OperatorCount;
            int caSystemID;
            byte[] tmsData = new byte[0];

            OperatorCount = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_TMS_DATA);

            irdetoInfo.mUcTmsData.setUcTsDataUnitCnt(OperatorCount);

            for (i = 0; i < OperatorCount; i++) {
                caSystemID = reply.readInt();
                len = reply.readInt();
                tmsData = new byte[len];
                reply.readByteArray(tmsData);
                irdetoInfo.mUcTmsData.addUcTmsData(caSystemID, tmsData);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcTmsData();
    }

    public IrdetoInfo.UcBuildInfo cca_get_build_info() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_BUILD_INFO);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_build_info fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_build_info success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int len;
            byte[] buildInfo = new byte[0];

            len = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_BUILD_INFO);
            if(len > 0){
                buildInfo = new byte[len];
                reply.readByteArray(buildInfo);
            }
            irdetoInfo.mUcBuildInfo.addUcBuildInfo(len, buildInfo);

        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcBuildInfo();
    }

    public int cca_get_secure_type() {
        int SecureType=-1;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_SECURE_TYPE);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_secure_type fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_secure_type success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            SecureType = reply.readInt();
        }

        request.recycle();
        reply.recycle();

        return SecureType;
    }

    public int cca_get_lock_id() {
        int LockId=-1;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_LOCK_ID);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_lock_id fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_lock_id success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            LockId = reply.readInt();
        }

        request.recycle();
        reply.recycle();

        return LockId;
    }

    public IrdetoInfo.UcVersion cca_get_version() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_VERSION);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_version fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_version success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int len;
            byte[] buildInfo = new byte[0];

            len = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_VERSION);
            if(len > 0){
                buildInfo = new byte[len];
                reply.readByteArray(buildInfo);
            }
            irdetoInfo.mUcVersion.addUcVersion(len, buildInfo);

        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcVersion();
    }

    public IrdetoInfo.UcCapability cca_get_capability() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_CAPABILITY);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_capability fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_capability success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int len;
            byte[] buildInfo = new byte[0];

            len = reply.readInt();
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_CAPABILITY);
            if(len > 0){
                buildInfo = new byte[len];
                reply.readByteArray(buildInfo);
            }
            irdetoInfo.mUcCapability.addUcCapability(len, buildInfo);

        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcCapability();
    }

    public IrdetoInfo.UcMailList cca_get_mail_by_uni_id(long uniId) {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_MAIL_BY_UNID);
        request.writeInt((int)uniId);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_mail_by_uni_id fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_mail_by_uni_id success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int index,kind,type,msg_watched,year,month,date,hour,minute,second,week;
            String text = null;
            index = reply.readInt();
            kind = reply.readInt();
            type = reply.readInt();
            msg_watched = reply.readInt();
            text = reply.readString();

            year = reply.readInt();
            month = reply.readInt();
            date = reply.readInt();
            hour = reply.readInt();
            minute = reply.readInt();
            second = reply.readInt();
            week = reply.readInt();

            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_MAIL_BY_UNID);
            irdetoInfo.getUcMailList().addUcMail(index,kind,type,msg_watched,text,year,month,date,hour,minute,second,week);

        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcMailList();
    }

    public int cca_get_mail_total_count() {
        int total_count=-1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_MAIL_TOTAL_NUMBER);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_mail_total_count fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_mail_total_count success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {

            total_count = reply.readInt();
        }
        request.recycle();
        reply.recycle();

        return total_count;
    }

    public IrdetoInfo.UcMailList cca_get_mails() {
        int count,total;
        IrdetoInfo irdetoInfo=null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_MAILES);
        //request.writeInt(startIndex);
        //request.writeInt(getNumber);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_mails fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_mails success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int unid,kind,type,msg_watched,year,month,date,hour,minute,second,week;
            String text = null;
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_MAILS);
            total = reply.readInt();
            for(count=0;count<total;count++){
                unid = reply.readInt();
                kind = reply.readInt();
                type = reply.readInt();
                msg_watched = reply.readInt();
                text = reply.readString();

                year = reply.readInt();
                month = reply.readInt();
                date = reply.readInt();
                hour = reply.readInt();
                minute = reply.readInt();
                second = reply.readInt();
                week = reply.readInt();

                //irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_MAIL_BY_UNID);
                irdetoInfo.getUcMailList().addUcMail(unid,kind,type,msg_watched,text,year,month,date,hour,minute,second,week);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcMailList();
    }

    public int cca_set_mail_status_to_read(int unid) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_SET_MAIL_TO_READ_BY_UNID);
        request.writeInt((int)unid);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_set_mail_status_to_read fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_set_mail_status_to_read success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int cca_delete_mail_by_unid(int unid) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_DELETE_MAIL_BY_UNID);
        request.writeInt((int)unid);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_delete_mail_by_index fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_delete_mail_by_index success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public IrdetoInfo.UcAttributeList cca_get_attribute_by_uni_id(long uniId) {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_ATTRIB_BY_UNID);
        request.writeInt((int)uniId);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_attribute_by_uni_id fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_attribute_by_uni_id success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int index,msg_watched,year,month,date,hour,minute,second,week;
            String text = null;

            index = reply.readInt();
            msg_watched = reply.readInt();
            text = reply.readString();

            year = reply.readInt();
            month = reply.readInt();
            date = reply.readInt();
            hour = reply.readInt();
            minute = reply.readInt();
            second = reply.readInt();
            week = reply.readInt();

            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_Attribute_BY_UNID);
            irdetoInfo.getUcAttributeList().addUcAttribute(index,msg_watched,text,year,month,date,hour,minute,second,week);

        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcAttributeList();
    }

    public int cca_get_attribute_total_count() {
        int total_count=-1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_ATTRIB_TOTAL_NUMBER);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_attribute_total_count fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_attribute_total_count success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {

            total_count = reply.readInt();
        }
        request.recycle();
        reply.recycle();

        return total_count;
    }

    public IrdetoInfo.UcAttributeList cca_get_attributes(int startIndex,int getNumber) {
        int count;
        IrdetoInfo irdetoInfo=null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_ATTRIBUTES);
        request.writeInt(startIndex);
        request.writeInt(getNumber);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_attributes fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_attributes success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int index,msg_watched,year,month,date,hour,minute,second,week;
            String text = null;
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_Attributes);
            for(count=0;count<getNumber;count++){
                index = reply.readInt();
                msg_watched = reply.readInt();
                text = reply.readString();

                year = reply.readInt();
                month = reply.readInt();
                date = reply.readInt();
                hour = reply.readInt();
                minute = reply.readInt();
                second = reply.readInt();
                week = reply.readInt();

                irdetoInfo.getUcAttributeList().addUcAttribute(index,msg_watched,text,year,month,date,hour,minute,second,week);
            }
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getUcAttributeList();
    }

    public int cca_set_attribute_status_to_read(int index) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_SET_ATTRIB_TO_READ_BY_INDEX);
        request.writeInt((int)index);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_set_attribute_status_to_read fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_set_attribute_status_to_read success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int cca_delete_attribute_by_index(int index) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_DELETE_ATTRIB_BY_INDEX);
        request.writeInt((int)index);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_delete_attribute_by_index fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_delete_attribute_by_index success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int cca_delete_all_mails() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_DELETE_ALL_MAILS);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_delete_all_mails fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_delete_all_mails success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int cca_delete_all_attributes() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_DELETE_ALL_ATTRIBUTES);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_delete_all_attributes fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_delete_all_attributes success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public IrdetoInfo.UcLoaderInfo cca_get_irdeto_loader_info() {
        IrdetoInfo irdetoInfo=null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_GET_LOADER_INFO);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_get_irdeto_loader_info fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_get_irdeto_loader_info success");
        }
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            int ManufacturerId, HardwareVersion, Variant, SystemId, KeyVersion, SignatureVersion, DownloadSequenceNumber;
            irdetoInfo = new IrdetoInfo(IrdetoInfo.UC_Loader_Info);

            ManufacturerId = reply.readInt();
            HardwareVersion = reply.readInt();
            Variant = reply.readInt();
            SystemId = reply.readInt();
            KeyVersion = reply.readInt();
            SignatureVersion = reply.readInt();
            DownloadSequenceNumber = reply.readInt();

            irdetoInfo.getLoaderInfo().setUcLoaderInfo(ManufacturerId,HardwareVersion,Variant,SystemId,KeyVersion,SignatureVersion,DownloadSequenceNumber);
        }
        request.recycle();
        reply.recycle();

        if(irdetoInfo == null)
            return null;
        else
            return irdetoInfo.getLoaderInfo();
    }

    public int cca_set_token(long token) {

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_IRDETO_SET_TOKEN);

        request.writeInt((int)token);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "cca_set_token fail, ret = " + ret);
        }
        else {
            Log.d(TAG, "cca_set_token success");
        }
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
}
