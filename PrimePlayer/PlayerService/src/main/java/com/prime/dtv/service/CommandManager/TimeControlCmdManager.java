package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.prime.dtv.Interface.BaseManager;

import java.util.Date;

public class TimeControlCmdManager extends BaseManager {
    private static final String TAG = "TimeControlCmdManager" ;
    public TimeControlCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, TimeControlCmdManager.class);
    }
    /*
   time zone
    */
    public int setTime(Date date) {
        return 0;
    }

    public int getDtvTimeZone() {
        return 0;
    }

    public int setDtvTimeZone(int zonesecond) {
        return 0;
    }

    public int getSettingTDTStatus() {
        return 0;
    }

    public int setSettingTDTStatus(int onoff) {
        return 0;
    }

    public int setTimeToSystem(boolean bSetTimeToSystem) {
        return 0;
    }

    public int syncTime(boolean bEnable) {
        return 0;
    }

    public int getDtvDaylight() {
        return 0;
    }

    public int setDtvDaylight(int onoff) {
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }
}
