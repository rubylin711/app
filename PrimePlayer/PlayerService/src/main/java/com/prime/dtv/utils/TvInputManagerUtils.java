package com.prime.dtv.utils;

import android.content.Context;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;

import com.prime.android.SystemAPP.PrimeSystemApp;
import com.prime.datastructure.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TvInputManagerUtils {
    private static final String TAG = "TvInputManagerUtils";

    public static Set<TvContentRating> getRatings(Context context) {
        List<TvContentRating> ratingList = PrimeSystemApp.getRatings(context);
        Set<TvContentRating> ratings = new HashSet<>();
        for(TvContentRating r : ratingList){
            ratings.add(r);
        }
        return ratings;
    }

    public static void set_enable(Context context ,boolean enable){
        PrimeSystemApp.set_enable(context,enable);
        LogUtils.d("enable = "+enable);
    }

    public static void remove_all_rattings(Context context){
        PrimeSystemApp.remove_all_rattings(context);
        LogUtils.d(" ");
    }

    public static void add_rating(Context context, TvContentRating rating){
        PrimeSystemApp.add_rating(context, rating);
        LogUtils.d("rating = "+rating.flattenToString());
    }
}
