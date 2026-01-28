package com.prime.dmg.launcher.Utils;

public abstract class OnQueryDayFrequencyListener {
    public abstract void  onSuccess(int appDayFrequencyCount, int serverDayFrequencyLimit);
    public abstract void  onFail(Throwable e);
}
