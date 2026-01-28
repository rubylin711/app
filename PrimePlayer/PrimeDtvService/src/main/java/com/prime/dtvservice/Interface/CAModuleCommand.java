package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.os.Bundle;

import com.prime.datastructure.CommuincateInterface.BookModule;
import com.prime.datastructure.CommuincateInterface.CaModule;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;

import java.util.List;

public class CAModuleCommand {
    public static final String TAG = "CAModuleCommand";
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle, Bundle replyBundle, PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = " + command_id);
        switch(command_id) {
            case CaModule.CMD_ServicePlayer_CA_GetEntitlementList:
                replyBundle = get_entitlement_list(requestBundle,replyBundle);
                break;
            case CaModule.CMD_ServicePlayer_CA_GetPromotionList:
                replyBundle = get_promotion_list(requestBundle,replyBundle);
                break;
            case CaModule.CMD_ServicePlayer_CA_GetCasData:
                replyBundle = get_cas_data(requestBundle,replyBundle);
                break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }
        return replyBundle;
    }

    private Bundle get_entitlement_list(Bundle requestBundle, Bundle replyBundle) {
        CasData casData = primeDtv.get_cas_data();
        List<String> entitlement_list = casData.getEntitledChannelIds();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt("NumOfIds", entitlement_list.size());
        for(int i = 0; i < entitlement_list.size(); i++) {
            replyBundle.putString("EntitlementId" + i, entitlement_list.get(i));
        }
        return replyBundle;
    }

    private Bundle get_promotion_list(Bundle requestBundle, Bundle replyBundle) {
        CasData casData = primeDtv.get_cas_data();
        List<String> promotion_list = casData.getPromotionChannelIds();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        replyBundle.putInt("NumOfIds", promotion_list.size());
        for(int i = 0; i < promotion_list.size(); i++) {
            replyBundle.putString("PromotionId" + i, promotion_list.get(i));
        }
        return replyBundle;
    }

    private Bundle get_cas_data(Bundle requestBundle, Bundle replyBundle) {
        CasData casData = primeDtv.get_cas_data();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        LogUtils.d("get_cas_data "+casData.getRawJsonString());
        replyBundle.putString("mRawJsonString", casData.getRawJsonString());
        return replyBundle;
    }
}
