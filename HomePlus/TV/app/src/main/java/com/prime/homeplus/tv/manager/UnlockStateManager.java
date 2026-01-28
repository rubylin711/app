package com.prime.homeplus.tv.manager;

import android.util.Log;

public class UnlockStateManager {
    private static final String TAG = "UnlockStateManager";

    private int unlockedFlags = LockManager.LOCK_NONE;

    public void unlock(int lockType) {
        switch (lockType) {
            case LockManager.LOCK_ADULT_CHANNEL:
                unlockedFlags |= LockManager.LOCK_ADULT_CHANNEL
                        | LockManager.LOCK_PARENTAL_CHANNEL
                        | LockManager.LOCK_PARENTAL_PROGRAM
                        | LockManager.LOCK_WORK_HOUR
                        | LockManager.LOCK_EPG_PROGRAM_INFO;
                break;

            case LockManager.LOCK_PARENTAL_CHANNEL:
                unlockedFlags |= LockManager.LOCK_PARENTAL_CHANNEL
                        | LockManager.LOCK_PARENTAL_PROGRAM
                        | LockManager.LOCK_WORK_HOUR
                        | LockManager.LOCK_EPG_PROGRAM_INFO;
                break;

            case LockManager.LOCK_PARENTAL_PROGRAM:
                unlockedFlags |= LockManager.LOCK_PARENTAL_PROGRAM
                        | LockManager.LOCK_WORK_HOUR
                        | LockManager.LOCK_EPG_PROGRAM_INFO;
                break;

            case LockManager.LOCK_WORK_HOUR:
                unlockedFlags |= LockManager.LOCK_WORK_HOUR
                        | LockManager.LOCK_EPG_PROGRAM_INFO;
                ;
                break;

            case LockManager.LOCK_EPG_PROGRAM_INFO:
                unlockedFlags |= LockManager.LOCK_EPG_PROGRAM_INFO;
                break;
        }
    }

    public void reset() {
        unlockedFlags = LockManager.LOCK_NONE;
    }

    public boolean isUnlocked(int lockType) {
        return (unlockedFlags & lockType) != 0;
    }

    public void relockAdultChannelIfNeeded(int currentChannelLockFlag) {
        if (currentChannelLockFlag != LockManager.LOCK_ADULT_CHANNEL &&
                (unlockedFlags & LockManager.LOCK_ADULT_CHANNEL) != 0) {
            Log.d(TAG, "Re-locking adult channel, clearing the unlocked flag");
            unlockedFlags = unlockedFlags & ~LockManager.LOCK_ADULT_CHANNEL;
        }
    }

    public void relockParentalProgramIfNeeded(int currentChannelLockFlag) {
        if (currentChannelLockFlag == LockManager.LOCK_NONE &&
                (unlockedFlags & LockManager.LOCK_PARENTAL_PROGRAM) != 0) {
            Log.d(TAG, "Re-locking parental program, clearing the unlocked flag");
            unlockedFlags = unlockedFlags & ~LockManager.LOCK_PARENTAL_PROGRAM;
        }
    }
}
