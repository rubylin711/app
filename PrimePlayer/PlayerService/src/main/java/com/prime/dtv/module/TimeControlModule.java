package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;

import java.util.Calendar;
import java.util.Date;

public class TimeControlModule {
    private static final String TAG = "TimeControlModule";
    private static final int CMD_TMCTL_Base = PrimeDtvMediaPlayer.CMD_Base + 0x900;

    // Time control command
    private static final int CMD_TMCTL_GetDateTime = CMD_TMCTL_Base + 0x01;
    private static final int CMD_TMCTL_SetDateTime = CMD_TMCTL_Base + 0x02;
    private static final int CMD_TMCTL_GetTimeZone = CMD_TMCTL_Base + 0x03;
    private static final int CMD_TMCTL_SetTimeZone = CMD_TMCTL_Base + 0x04;
    private static final int CMD_TMCTL_GetDaylightSaving = CMD_TMCTL_Base + 0x05;
    private static final int CMD_TMCTL_SetDaylightSaving = CMD_TMCTL_Base + 0x06;
    private static final int CMD_TMCTL_DateTimeToSecond = CMD_TMCTL_Base + 0x07;
    private static final int CMD_TMCTL_SecondToDateTime = CMD_TMCTL_Base + 0x08;
    private static final int CMD_TMCTL_AutoSyncTimeFromDtv = CMD_TMCTL_Base + 0x09;
    private static final int CMD_TMCTL_AutoSyncTimeZoneFromDtv = CMD_TMCTL_Base + 0x0A;
    private static final int CMD_TMCTL_GetCurSleepDuration = CMD_TMCTL_Base + 0x0B;
    private static final int CMD_TMCTL_SetWakeupInterval = CMD_TMCTL_Base + 0x0C;

    // JAVA CMD
    private static final int CMD_JAVA_Base = PrimeDtvMediaPlayer.CMD_JAVA_Base;
    private static final int CMD_TMCTL_GetCurrentSystemTime = CMD_JAVA_Base + CMD_TMCTL_Base + 0x01;//TODO
    private static final int CMD_TMCTL_GetSettingTOTStatus = CMD_JAVA_Base + CMD_TMCTL_Base + 0x02;//TODO
    private static final int CMD_TMCTL_GetSettingTDTStatus = CMD_JAVA_Base + CMD_TMCTL_Base + 0x03;//TODO
    private static final int CMD_TMCTL_SetTimeToSystem = CMD_JAVA_Base + CMD_TMCTL_Base + 0x04;//TODO
    private static final int CMD_TMCTL_SetSettingTOTStatus = CMD_JAVA_Base + CMD_TMCTL_Base + 0x05;//TODO
    private static final int CMD_TMCTL_SetSettingTDTStatus = CMD_JAVA_Base + CMD_TMCTL_Base + 0x06;//TODO

    public Date get_dtv_date() {
        //Log.d(TAG, "get_dtv_date: ");
        Date getDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_GetDateTime);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        //Log.d(TAG, "get_dtv_date:  ret = " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int min = 0;
            int second = 0;
            long offset = 0;
            year = reply.readInt();
            month = reply.readInt();
            day = reply.readInt();
            hour = reply.readInt();
            min = reply.readInt();
            second = reply.readInt();
            getDate = new Date(year - 1900, month - 1, day, hour, min, second);

            offset = 0;
            if (PrimeDtvMediaPlayer.ADD_SYSTEM_OFFSET) {  // connie 20181106 for not add system offset
                Calendar ca = Calendar.getInstance();
                int zone = ca.get(Calendar.ZONE_OFFSET);
                int dst = ca.get(Calendar.DST_OFFSET);

                offset = zone + dst;
            }
            getDate.setTime(getDate.getTime() + offset);
        }
        request.recycle();
        reply.recycle();
        return getDate;
    }

    public int set_time(Date date) {
        long offset = 0;
        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;

        if (date == null) {
            return PrimeDtvMediaPlayer.get_return_value(ret);
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_SetDateTime);

        Calendar ca = Calendar.getInstance();
        if (PrimeDtvMediaPlayer.ADD_SYSTEM_OFFSET) { // connie 20181101 for not add system offset
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);
            offset = zone + dst;
        }
        date.setTime(date.getTime() - offset);
        ca.setTime(date);

        int year = date.getYear();
        int month = date.getMonth();
        int mday = date.getDate();
        int hour = date.getHours();
        int min = date.getMinutes();
        int isecond = date.getSeconds();

        request.writeInt(year + 1900);
        request.writeInt(month + 1);
        request.writeInt(mday);
        request.writeInt(hour);
        request.writeInt(min);
        request.writeInt(isecond);
        request.writeInt(0);
        request.writeInt(0);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            Log.d(TAG, "set_time: success year=" + year + ", month=" + month + ", mday=" + mday + ", hour=" + hour + ", min=" + min);
            ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;
        } else
            Log.d(TAG, "set_time: fail year=" + year + ", month=" + month + ", mday=" + mday + ", hour=" + hour + ", min=" + min);

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int get_dtv_timezone() {
        //Log.d(TAG, "get_dtv_timezone");
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_TMCTL_GetTimeZone);
    }

    public int set_dtv_timezone(int zonesecond) {
        //Log.d(TAG, "set_dtv_timezone");
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_TMCTL_SetTimeZone, zonesecond);
    }

    public int get_dtv_daylight()//value: 0(off) or 1(on)
    {
        //Log.d(TAG, "get_dtv_daylight");
        return PrimeDtvMediaPlayer.excute_command_getII(CMD_TMCTL_GetDaylightSaving);
    }

    public int set_dtv_daylight(int onoff)//value: 0(off) or 1(on)
    {
        Log.d(TAG, "set_dtv_daylight()");
        return PrimeDtvMediaPlayer.excute_command(CMD_TMCTL_SetDaylightSaving, onoff);
    }

    public int get_setting_tdt_status() {
        //Log.d(TAG, "get_setting_tdt_status");
        return PrimeDtvMediaPlayer.excute_command(CMD_TMCTL_GetSettingTDTStatus);
    }

    public int set_setting_tdt_status(int onoff)//value: 0(off) or 1(on)
    {
        Log.d(TAG, "set_setting_tdt_status()");
        return PrimeDtvMediaPlayer.excute_command(CMD_TMCTL_SetSettingTDTStatus, onoff);
    }

    public int set_time_to_system(boolean bSetTimeToSystem) {
        //Log.d(TAG, "set_time_to_system bEnable=" + bEnable);
        return PrimeDtvMediaPlayer.excute_command(CMD_TMCTL_SetTimeToSystem, bSetTimeToSystem ? 1 : 0);
    }

    public Date second_to_date(int isecond) {
        int offset = 0;
        Date getDate = null;
        Calendar ca = Calendar.getInstance();

        if (PrimeDtvMediaPlayer.ADD_SYSTEM_OFFSET) { // connie 20181106 for not add system offset
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);

            offset = zone / 1000 + dst / 1000;
        }

        isecond += offset;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_SecondToDateTime);
        request.writeInt(isecond);
//    request.writeInt(1);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int min = 0;
            int second = 0;
            year = reply.readInt();
            month = reply.readInt();
            day = reply.readInt();
            hour = reply.readInt();
            min = reply.readInt();
            second = reply.readInt();
            reply.readInt();
            reply.readInt();
            getDate = new Date(year - 1900, month - 1, day, hour, min, second);
        }
        request.recycle();
        reply.recycle();
        return getDate;
    }

    public int date_to_second(Date date) {
        int second = 0;
        if (date == null) {
            return 0;
        }

        Calendar ca = Calendar.getInstance();
        int dst = ca.get(Calendar.DST_OFFSET);
        int zone = ca.get(Calendar.ZONE_OFFSET);
        ca.setTime(date);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_DateTimeToSecond);

        /*int year = date.getYear();
        int month = date.getMonth();
        int mday = date.getDate();
        int hour = date.getHours();
        int min = date.getMinutes();
        int isecond = date.getSeconds();*/

        // Johnny 20180223 test to replace outdated date.get...
        int year = ca.get(Calendar.YEAR);
        int month = ca.get(Calendar.MONTH) + 1; // calendar month = 0~11
        int mday = ca.get(Calendar.DATE);
        int hour = ca.get(Calendar.HOUR_OF_DAY);
        int min = ca.get(Calendar.MINUTE);
        int isecond = ca.get(Calendar.SECOND);

        request.writeInt(/*year + 1900*/year);
        request.writeInt(/*month + 1*/month);
        request.writeInt(mday);
        request.writeInt(hour);
        request.writeInt(min);
        request.writeInt(isecond);
        request.writeInt(0);
        request.writeInt(0);
//    request.writeInt(1);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            second = reply.readInt();

            if (PrimeDtvMediaPlayer.ADD_SYSTEM_OFFSET) { // connie 20181106 for not add system offset
                int offset = zone / 1000 + dst / 1000;
                second -= offset;
            }
        }
        request.recycle();
        reply.recycle();
        return second;
    }

    public int sync_time(boolean bEnable) {
        //Log.d(TAG, "sync_time bEnable=" + bEnable);
        return PrimeDtvMediaPlayer.excute_command(CMD_TMCTL_AutoSyncTimeFromDtv, bEnable ? 1 : 0);
    }

}
