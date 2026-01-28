package com.prime.homeplus.tv.utils;


import android.content.Context;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.tvprovider.media.tv.Program;
import androidx.tvprovider.media.tv.TvContractUtils;

import com.prime.homeplus.tv.R;

public class ProgramRatingUtils {
    private static final String TAG = "GuideUtils";
    private static final String DVB_AGE_PREFIX = "com.android.tv/DVB/DVB_";

    public static int getRatingIcon(int index) {
        switch (index) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return R.drawable.icon_rating_all;
            case 9:
            case 10:
            case 11:
                return R.drawable.icon_rating_6;

            case 12:
            case 13:
            case 14:
                return R.drawable.icon_rating_12;
            case 15:
            case 16:
                return R.drawable.icon_rating_15;
            case 17:
            case 18:
                return R.drawable.icon_rating_18;
        }

        return 0;
    }

    // checkParentalRating
    public static boolean isParentalProgramRatingLocked (Context context, Program program) {
        if (null == program) {
            return false;
        } else {
            int programRatingAge = getNowRating(program.getContentRatings());
            int systemRatingAge = getSystemContentBlockedRating(context);
//            Log.d(TAG,"isParentalProgramRatingLocked programRatingAge = "+programRatingAge+" systemRatingAge = "+systemRatingAge);
            return programRatingAge >= systemRatingAge;
        }
    }

    public static int getSystemContentBlockedRating(Context context) {
        int nowAge = 18;

        Set<TvContentRating> blockedRatings = getContentBlockedRatings(context);
        if (blockedRatings == null || blockedRatings.isEmpty()) {
            return nowAge;
        }

        for (TvContentRating rating : blockedRatings) {
            if (rating == null) continue;

            String mainRating = rating.getMainRating();
            if (mainRating == null || !mainRating.startsWith("DVB_")) continue;

            try {
                int age = Integer.parseInt(mainRating.substring(4)); // Extract number after "DVB_"
                nowAge = Math.min(nowAge, age);
            } catch (NumberFormatException e) {
                Log.d(TAG, "getSystemContentBlockedRating Error: " + e.toString());
            }
        }

        return nowAge;
    }

    public static Set<TvContentRating> getContentBlockedRatings(Context context) {
        try {
            TvInputManager tvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
            if (tvInputManager != null) {
                return new HashSet<>(tvInputManager.getBlockedRatings());
            }
        } catch (Exception e) {
            Log.e(TAG, "getContentBlockedRatings Error: " + e.toString());
        }
        return Collections.emptySet();
    }

    public static int getNowRating(TvContentRating[] tvContentRatingSet) {
        if (tvContentRatingSet == null || tvContentRatingSet.length == 0) {
            return 0;
        }

        int nowAge = 18; // Default max age

        for (TvContentRating rating : tvContentRatingSet) {
            if (rating == null) continue;

            String mainRating = rating.getMainRating();
            if (mainRating == null || !mainRating.startsWith("DVB_")) {
                continue;
            }

            try {
                int age = Integer.parseInt(mainRating.substring(4)); // Extract number after "DVB_"
//                Log.d(TAG,"age = "+age+" nowAge = "+nowAge+" mainRating = "+mainRating);
                nowAge = Math.min(nowAge, age);
            } catch (NumberFormatException e) {
                Log.d(TAG, "getNowRating Error: " + e.toString());
            }
        }

        return nowAge;
    }

    public static String getStrictestDvbRating(TvContentRating[] tvContentRatings) {
        return getStrictestDvbRating(TvContractUtils.contentRatingsToString(tvContentRatings));
    }

    public static String getStrictestDvbRating(String contentRatingString) {

        if (TextUtils.isEmpty(contentRatingString)) {
            return null;
        }

        String[] ratings = contentRatingString.split(",");

        int maxAgeFound = -1;
        String strictestRating = null;

        for (String rating : ratings) {
            String trimmedRating = rating.trim();

            if (trimmedRating.startsWith(DVB_AGE_PREFIX)) {
                try {
                    String ageString = trimmedRating.substring(DVB_AGE_PREFIX.length());
                    int currentAge = Integer.parseInt(ageString);

                    if (currentAge > maxAgeFound) {
                        maxAgeFound = currentAge;
                        strictestRating = trimmedRating;
                    }
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Found non-age DVB rating, skipping: " + trimmedRating);
                }
            }
        }

        return strictestRating;
    }

    public static int getStrictestDvbAge(TvContentRating[] tvContentRatings) {
        return getStrictestDvbAge(TvContractUtils.contentRatingsToString(tvContentRatings));
    }

    public static int getStrictestDvbAge(String contentRatingString) {
        String strictestRating = getStrictestDvbRating(contentRatingString);

        if (strictestRating != null) {
            try {
                String ageString = strictestRating.substring(DVB_AGE_PREFIX.length());
                return Integer.parseInt(ageString);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }
}
