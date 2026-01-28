package com.prime.homeplus.settings.sms;

public class SMSState {
    public static final int STATE_INQUIRE = 0;
    public static final int STATE_WORK_ORDER = 1;
    public static final int STATE_PHONE_CHANGE = 2;
    public static final int STATE_ACTIVATING = 3;

    public int mState = STATE_INQUIRE;

    SMSState() {

    }

    public synchronized void setState(int state) {
        mState = state;
    }

    public synchronized int getState() {
        return mState;
    }
}

