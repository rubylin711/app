package com.prime.homeplus.tv.manager;

import android.content.Context;
import android.media.tv.TvContentRating;

import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.PrimeUtils;
import com.prime.homeplus.tv.utils.ProgramRatingUtils;

import java.util.HashSet;
import java.util.Set;

public class LockManager {
    private static final String TAG = "LockManager";

    public static final int LOCK_NONE             = 0x00;
    public static final int LOCK_ADULT_CHANNEL    = 0x01;
    public static final int LOCK_PARENTAL_CHANNEL = 0x02;
    public static final int LOCK_PARENTAL_PROGRAM = 0x04;
    public static final int LOCK_WORK_HOUR        = 0x08;
    public static final int LOCK_EPG_PROGRAM_INFO = 0x10;

    public enum LockMode {
        ALL,
        HIGHEST_ONLY
    }
    private final Context applicationContext;

    public LockManager(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public int getHighestPriorityLockFlag(Channel channel, Program program, TvContentRating currentContentBlockedRating) {
        return resolveLockFlags(channel, program, currentContentBlockedRating, LockMode.HIGHEST_ONLY);
    }

    public int resolveLockFlags(Channel channel, Program program, TvContentRating currentContentBlockedRating, LockMode mode) {
        int requiredLocks = LOCK_NONE;

        // adult channel
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        Boolean isAdultChannelLock = ChannelUtils.isAdult(applicationContext,channel);
        if (isAdultChannelLock) {
            if (mode == LockMode.HIGHEST_ONLY) {
                return LOCK_ADULT_CHANNEL;
            }
            requiredLocks |= LOCK_ADULT_CHANNEL;
        }
        // parental channel
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor , same as prime channel lock
        Boolean isParentalChannelLocked = ChannelUtils.isChannelLocked(applicationContext, channel);
        if (isParentalChannelLocked) {
            if (mode == LockMode.HIGHEST_ONLY) {
                return LOCK_PARENTAL_CHANNEL;
            }
            requiredLocks |= LOCK_PARENTAL_CHANNEL;
        }

        // parental program
        Boolean isParentalProgramRatingLocked = (currentContentBlockedRating != null) ||
                ProgramRatingUtils.isParentalProgramRatingLocked(applicationContext, program);
        if (isParentalProgramRatingLocked) {
            if (mode == LockMode.HIGHEST_ONLY) {
                return LOCK_PARENTAL_PROGRAM;
            }
            requiredLocks |= LOCK_PARENTAL_PROGRAM;
        }

        // work hours
        Boolean isWorkHoursLocked = false;
        GposInfo gposInfo = PrimeUtils.g_prime_dtv.gpos_info_get();
        if (gposInfo != null) {
            int startRes = GposInfo.getTimeLockPeriodStart(applicationContext, 0);
            int endRes = GposInfo.getTimeLockPeriodEnd(applicationContext, 0);

            if (startRes != -1 && endRes != -1) {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(java.util.Calendar.MINUTE);
                int currentTime = currentHour * 100 + currentMinute;

                if (startRes <= endRes) {
                    if (currentTime >= startRes && currentTime < endRes) {
                        isWorkHoursLocked = true;
                    }
                } else {
                    // Cross midnight, e.g. 2300 to 0600
                    if (currentTime >= startRes || currentTime < endRes) {
                        isWorkHoursLocked = true;
                    }
                }
            }
        }

        if (isWorkHoursLocked) {
             if (mode == LockMode.HIGHEST_ONLY) {
                return LOCK_WORK_HOUR;
            }
            requiredLocks |= LOCK_WORK_HOUR;
        }

        return requiredLocks;
    }
}
