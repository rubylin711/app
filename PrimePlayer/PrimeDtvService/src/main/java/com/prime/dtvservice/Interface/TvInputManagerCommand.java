package com.prime.dtvservice.Interface;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.media.tv.TvContentRating;
import android.os.Bundle;
import android.util.Log;

import com.prime.datastructure.CommuincateInterface.TvInputManagerModule;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtv;

import java.util.HashSet;
import java.util.Set;

public class TvInputManagerCommand {
    private static final String TAG = "TvInputManagerCommand";
    private PrimeDtv primeDtv = null;
    public Bundle executeCommand(Bundle requestBundle, Bundle replyBundle, PrimeDtv primeDtv) {
        this.primeDtv = primeDtv;
        int command_id = requestBundle.getInt(COMMAND_ID,0);
        LogUtils.d("command_id = "+command_id);
        switch(command_id) {
            case TvInputManagerModule.CMD_ServicePlayer_TvInputManager_GetRatings:{
                replyBundle = get_ratings(requestBundle,replyBundle);
            }break;
            case TvInputManagerModule.CMD_ServicePlayer_TvInputManager_SetParentalRatingEnable:{
                replyBundle = set_enable(requestBundle,replyBundle);
            }break;
            case TvInputManagerModule.CMD_ServicePlayer_TvInputManager_RemoveAllRatings:{
                replyBundle = remove_all_ratings(requestBundle,replyBundle);
            }break;
            case TvInputManagerModule.CMD_ServicePlayer_TvInputManager_AddRatings:{
                replyBundle = add_ratings(requestBundle,replyBundle);
            }break;
            default:
                LogUtils.e("Command not implement");
                replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_FAIL);
        }

        return replyBundle;
    }

    private Bundle add_ratings(Bundle requestBundle, Bundle replyBundle) {
        TvContentRating rating = TvContentRating.createRating(requestBundle.getString("Domain"),
                                                                requestBundle.getString("RatingSystem"),
                                                                requestBundle.getString("RatingValue"));
        primeDtv.add_rating(rating);
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle remove_all_ratings(Bundle requestBundle, Bundle replyBundle) {
        primeDtv.remove_all_rattings();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle set_enable(Bundle requestBundle, Bundle replyBundle) {
        primeDtv.set_parental_rating_enable(requestBundle.getBoolean("RatingEnable"));
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        return replyBundle;
    }

    private Bundle get_ratings(Bundle requestBundle, Bundle replyBundle) {
        Set<TvContentRating> ratings = primeDtv.getRatings();
        replyBundle.putInt(COMMAND_REPLY_STATUS,COMMAND_REPLY_SUCCESS);
        int num = ratings.size();
        replyBundle.putInt("RatingNum", num);
        for(TvContentRating r : ratings){
            replyBundle.putString("Domain", r.getDomain());
            replyBundle.putString("RatingSystem", r.getRatingSystem());
            replyBundle.putString("RatingValue", r.getMainRating());
        }
        return replyBundle;
    }
}
