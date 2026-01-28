package com.prime.homeplus.settings;

import android.content.Context;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.RequiresApi;

import com.prime.datastructure.sysdata.GposInfo;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ParentalControlManager {
    private String TAG ="HomePlus-ParentalControlManager";
    private Context context;

    public ParentalControlManager(Context context) {
        this.context = context;
    }


    public boolean isEnable(){
        TvInputManager mTvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
        boolean enable = mTvInputManager.isParentalControlsEnabled();
        LogUtils.d("enable = "+enable);
        return enable;
    }

    public Set<TvContentRating> getRatings() {
        Set<TvContentRating> mRatings = new HashSet<TvContentRating>();

        mRatings.addAll(PrimeUtils.getRatings());
        LogUtils.d("mRatings = "+mRatings);
        return mRatings;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setEnable(boolean enable) {
        GposInfo.setParentalLockOnOff(context, enable ? 1 : 0);
        //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_PARENTAL_LOCK_ONOFF, enable ? 1 : 0);
        PrimeUtils.g_prime_dtv.tv_input_manager_set_parental_rating_enable(enable);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void removeAllRatings() {

        PrimeUtils.g_prime_dtv.tv_input_manager_remove_all_ratings();

    }

    public void addRatings(TvContentRating inputContentRating) {
        LogUtils.d("inputContentRating = "+inputContentRating);
        PrimeUtils.g_prime_dtv.tv_input_manager_add_ratings(inputContentRating);
    }

    public int getNowRating() {
        int nowAge = 18;

        if(PrimeUtils.get_gpos_info() != null){
            nowAge = GposInfo.getParentalRate(context);
        }

        if (!isEnable()) {
            nowAge = 0;
        } else {
            Set<TvContentRating> TvContentRatingSet = getRatings();
            for (TvContentRating TvContentRating : TvContentRatingSet) {
                String ageRating = TvContentRating.getMainRating();
                int age = Integer.parseInt(ageRating.substring(4, ageRating.length()));
                if (age < nowAge) {
                    nowAge = age;
                }
            }
        }
        LogUtils.d( "Ratings = "+nowAge);

        return nowAge;
    }
}
