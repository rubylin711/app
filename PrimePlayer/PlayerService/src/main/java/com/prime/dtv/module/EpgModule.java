package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.datastructure.sysdata.SeriesInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EpgModule {
    private static final String TAG = "EpgModule";

    private static final int CMD_EPG_Base = PrimeDtvMediaPlayer.CMD_Base + 0x400;

    // EPG command
    private static final int CMD_EPG_GetPresentEvent = CMD_EPG_Base + 0x01;
    private static final int CMD_EPG_GetFollowEvent = CMD_EPG_Base + 0x02;
    private static final int CMD_EPG_GetByEventId = CMD_EPG_Base + 0x03;
    private static final int CMD_EPG_GetEventByTime = CMD_EPG_Base + 0x04;
    private static final int CMD_EPG_GetShortDesc = CMD_EPG_Base + 0x05;
    private static final int CMD_EPG_GetDetailDesc = CMD_EPG_Base + 0x06;
    private static final int CMD_EPG_SetLanguageCode = CMD_EPG_Base + 0x07;
    private static final int CMD_EPG_GetLanguageCode = CMD_EPG_Base + 0x08;
    private static final int CMD_EPG_GetParentRate = CMD_EPG_Base + 0x09;
    private static final int CMD_EPG_Start = CMD_EPG_Base + 0x0A;
    private static final int CMD_EPG_SendEitRowData = CMD_EPG_Base + 0x0B;
    private static final int CMD_EPG_SetEpgDataId = CMD_EPG_Base + 0x0C;
    private static final int CMD_EPG_Stop = CMD_EPG_Base + 0x0D;
    private static final int CMD_EPG_DebugEvents = CMD_EPG_Base + 0x0E;
    private static final int CMD_EPG_GetPresentFollowEvent = CMD_EPG_Base + 0x0F;
    private static final int CMD_EPG_AddEpgDataId = CMD_EPG_Base + 0x10;
    private static final int CMD_EPG_DelEpgDataId = CMD_EPG_Base + 0x11;
    private DataManager mDataManager;

    private static class DtvDate {
        int mYear;
        int mMonth;
        int mDay;
        int mHour;
        int mMinute;
        int mSecond;

        DtvDate(int paraYear, int paraMonth, int paraDay, int paraHour, int paraMinute, int paraSecond) {
            mYear = paraYear;
            mMonth = paraMonth;
            mDay = paraDay;
            mHour = paraHour;
            mMinute = paraMinute;
            mSecond = paraSecond;
        }
    }

    private DtvDate time_calendar_to_dtv_date(Calendar calendar) {
        Log.d(TAG, "time_calendar_to_dtv_date()");
        DtvDate retDtvDate = new DtvDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        return retDtvDate;
    }

    private Calendar dtv_date_to_calendar(DtvDate dtvDate) {
        Calendar ca = null;

        PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();
        int zoneSecond = 60 * dtv.get_dtv_timezone();
        int daylightSecond = 60 * 60 * dtv.get_dtv_daylight();

        if (0 == zoneSecond) {
            ca = Calendar.getInstance();
        } else {
            ca = Calendar.getInstance(time_convert_timezone(zoneSecond + daylightSecond));
        }

        ca.set(dtvDate.mYear, dtvDate.mMonth - 1, dtvDate.mDay, dtvDate.mHour, dtvDate.mMinute, dtvDate.mSecond);
        return ca;
    }

    private TimeZone time_convert_timezone(int zoneSecond) {
        int zoneMinutes = zoneSecond / 60;
        TimeZone timeZone = null;

        String strZone = "GMT";
        if (zoneMinutes > 0) {
            strZone += "+";
            strZone += String.format("%02d:%02d", zoneMinutes / 60, zoneMinutes % 60);
        } else if (zoneMinutes < 0) {
            strZone += "-";
            strZone += String.format("%02d:%02d", (0 - zoneMinutes) / 60, (0 - zoneMinutes) % 60);
        }

        timeZone = TimeZone.getTimeZone(strZone);
        return timeZone;
    }


    private void initial_epg_event(EPGEvent event, Parcel reply) {
        long channelId = PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt());
        //event.setChannelId(channelId);
        //Log.d(TAG, "initial_epg_event:  channel ID = " + channelId);

        int eventId = reply.readInt();
        event.set_event_id(eventId);
        //Log.d(TAG, "initial_epg_event:  Event ID = " + eventId);

        DtvDate dtvDate = new DtvDate(reply.readInt(), reply.readInt(), reply.readInt(), reply.readInt(), reply.readInt(), reply.readInt());

//        Log.d(TAG, String.format("EPG = %d-%d-%d %d:%d:%d" , dtvDate.mYear, dtvDate.mMonth, dtvDate.mDay, dtvDate.mHour, dtvDate.mMinute, dtvDate.mSecond));

        // set start date
        Date startDate = new Date(dtvDate.mYear - 1900, dtvDate.mMonth - 1, dtvDate.mDay, dtvDate.mHour, dtvDate.mMinute, dtvDate.mSecond);//Date startDate = this.timeDtvDate2date(dtvDate);
        event.set_start_time(startDate.getTime());

        //int durations = reply.readInt() * 1000;
        int durations = reply.readInt();
//        if(durations % 60 != 0)
//            durations = 60 * (durations/60);
        durations = durations * 1000;

        //Log.d(TAG, "initial_epg_event:  duration = " + durations);
        event.set_duration(durations);

        long endTime = startDate.getTime() + durations;
        event.set_end_time(endTime);

        int freeCA = reply.readInt();
        //Log.d(TAG, "initial_epg_event:  freeCA = " + freeCA);
        //if (0 == freeCA)
        //{
        //    event.setFreeCA(true);
        //}
        //else
        //{
        //    event.setFreeCA(false);
        //}

        int parentLockLevel = reply.readInt();
        event.set_parental_rate(parentLockLevel);
        //Log.d(TAG, "initial_epg_event:  parentLockLevel = " + parentLockLevel);
        //event.setParentLockLevel(parentLockLevel);

        //String countryCode = reply.readString();
        //Log.d(TAG, "initial_epg_event:  CoountryCode = " + countryCode);
        //event.setParentCountryCode(countryCode);

        int contentLevel1 = reply.readInt();
        //Log.d(TAG, "initial_epg_event: contentLevel1 = " + contentLevel1);
        //event.setContentLevel1(contentLevel1);

        int contentLevel2 = reply.readInt();
        //Log.d(TAG, "initial_epg_event:  contentLevel2 = " + contentLevel2);
        //event.setContentLevel2(contentLevel2);

        int contentLevel3 = reply.readInt();
        //Log.d(TAG, "initial_epg_event:  contentLevel3 = " + contentLevel3);
        //event.setContentLevel3(contentLevel3);

        int contentLevel4 = reply.readInt();
        //Log.d(TAG, "initial_epg_event: contentLevel4 = " + contentLevel4);
        //event.setContentLevel4(contentLevel4);

        String name = reply.readString();
        Log.d(TAG, "initial_epg_event:  Name = " + name);
        if (null != name) {
            event.set_event_name(name);
        }

        byte[] series_key = new byte[SeriesInfo.Series.MAX_SERIES_KEY_LENGTH];
        try {
            reply.readByteArray(series_key);
        } catch (RuntimeException e) {
            Log.e(TAG, "initial_epg_event: read series key fail", e);
            Log.e(TAG, "initial_epg_event: check MAX_SERIES_KEY_LENGTH in dtvservice");
        }

        event.set_series_key(series_key);
        Log.d(TAG, "series key = " + Arrays.toString(series_key));

        int episode_key = reply.readInt();
        event.set_episode_key(episode_key);
        Log.d(TAG, "episode_key = " + episode_key);

        int episode_status = reply.readInt();
        event.set_episode_status(episode_status);
        Log.d(TAG, "episode_status = " + episode_status);

        int episode_last = reply.readInt();
        event.set_episode_last(episode_last);
        Log.d(TAG, "episode_last = " + episode_last);
    }

    public int debug_epg_events(long channelID) {
        Log.d(TAG, "start_epg(" + channelID + ")");
        return PrimeDtvMediaPlayer.excute_command(CMD_EPG_DebugEvents, channelID);
    }

    public int start_epg(long channelID) {
        Log.d(TAG, "start_epg(" + channelID + ")");
        return PrimeDtvMediaPlayer.excute_command(CMD_EPG_Start, channelID);
    }

//    public EPGEvent get_present_event(long channelID) {
//        //Log.d(TAG, "get_present_event(" + channelID + ")");
//        EPGEvent event = null;
//        Parcel request = Parcel.obtain();
//        Parcel reply = Parcel.obtain();
//        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
//        request.writeInt(CMD_EPG_GetPresentEvent);
//        request.writeInt((int) channelID);
//
//        PrimeDtvMediaPlayer.invokeex(request, reply);
//        int ret = reply.readInt();
//        //Log.d(TAG, "ret = " + ret);
//
//        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
//            event = new EPGEvent();
//            initial_epg_event(event, reply);
//            event.set_event_type(EPGEvent.EPG_TYPE_PRESENT);
//        }else{
//            LogUtils.d(" ret = "+ret);
//        }
//
//        request.recycle();
//        reply.recycle();
//
//        return event;
//    }
//
//    public EPGEvent get_follow_event(long channelID) {
//        Log.d(TAG, "get_follow_event(" + channelID + ")");
//        EPGEvent event = null;
//        Parcel request = Parcel.obtain();
//        Parcel reply = Parcel.obtain();
//        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
//        request.writeInt(CMD_EPG_GetFollowEvent);
//        request.writeInt((int) channelID);
//
//        PrimeDtvMediaPlayer.invokeex(request, reply);
//        int ret = reply.readInt();
//        Log.d(TAG, "ret = " + ret);
//
//        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
//            event = new EPGEvent();
//            initial_epg_event(event, reply);
//            event.set_event_type(EPGEvent.EPG_TYPE_FOLLOW);
//        }
//
//        request.recycle();
//        reply.recycle();
//
//        return event;
//    }

    public EPGEvent get_present_event(long channelID) {
        //Log.d(TAG, "get_present_event(" + channelID + ")");
        EPGEvent event = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetPresentFollowEvent);

        /*1. send channel id*/
        request.writeInt((int) channelID);

        /*2. send time*/
        LocalDateTime now = LocalDateTime.now();
        request.writeInt(now.getYear());
        request.writeInt(now.getMonthValue());
        request.writeInt(now.getDayOfMonth());
        request.writeInt(now.getHour());
        request.writeInt(now.getMinute());
        request.writeInt(now.getSecond());

        // send pf flag, 1:p 2:f 3:p&f
        request.writeInt(1);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        //Log.d(TAG, "ret = " + ret);

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int count = reply.readInt();
            if (count == 1) { // should have only one event
                event = new EPGEvent();
                initial_epg_event(event, reply);
                event.set_event_type(EPGEvent.EPG_TYPE_PRESENT);
            }
        } else{
            LogUtils.d(" ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return event;
    }

    public EPGEvent get_follow_event(long channelID) {
        //Log.d(TAG, "get_follow_event(" + channelID + ")");
        EPGEvent event = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetPresentFollowEvent);

        /*1. send channel id*/
        request.writeInt((int) channelID);

        /*2. send time*/
        LocalDateTime now = LocalDateTime.now();
        request.writeInt(now.getYear());
        request.writeInt(now.getMonthValue());
        request.writeInt(now.getDayOfMonth());
        request.writeInt(now.getHour());
        request.writeInt(now.getMinute());
        request.writeInt(now.getSecond());

        // send pf flag, 1:p 2:f 3:p&f
        request.writeInt(2);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        //Log.d(TAG, "ret = " + ret);

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int count = reply.readInt();
            if (count == 1) { // should have only one event
                event = new EPGEvent();
                initial_epg_event(event, reply);
                event.set_event_type(EPGEvent.EPG_TYPE_FOLLOW);
            }
        } else{
            LogUtils.d(" ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return event;
    }

    // if return 2 non null epg events, events[0] = present, event[1] = follow
    // if return 1 non null epg event, it may be present or follow
    public EPGEvent[] get_present_follow_event(long channelID) {
        //Log.d(TAG, "get_present_follow_event(" + channelID + ")");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetPresentFollowEvent);

        /*1. send channel id*/
        request.writeInt((int) channelID);

        /*2. send time*/
        LocalDateTime now = LocalDateTime.now();
        request.writeInt(now.getYear());
        request.writeInt(now.getMonthValue());
        request.writeInt(now.getDayOfMonth());
        request.writeInt(now.getHour());
        request.writeInt(now.getMinute());
        request.writeInt(now.getSecond());

        // send pf flag, 1:p 2:f 3:p&f
        request.writeInt(3);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        //Log.d(TAG, "ret = " + ret);

        EPGEvent[] events = new EPGEvent[2];
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int count = reply.readInt();
            for (int i = 0 ; i < count ; i++) {
                EPGEvent event = new EPGEvent();
                initial_epg_event(event, reply);
                long cur_time = now.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
                if (cur_time >= event.get_start_time() && cur_time < event.get_end_time()) {
                    event.set_event_type(EPGEvent.EPG_TYPE_PRESENT);
                } else if (cur_time < event.get_start_time()) {
                    event.set_event_type(EPGEvent.EPG_TYPE_FOLLOW);
                } else {
                    event.set_event_type(EPGEvent.EPG_TYPE_SCHEDULE); // should not happen
                }

                events[i] = event;
            }
        }else{
            LogUtils.d(" ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return events;
    }

    public EPGEvent get_epg_by_event_id(long channelID, int eventId) {
        Log.d(TAG, "get_epg_by_event_id(" + channelID + "," + eventId + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetByEventId);
        request.writeInt((int) channelID);
        request.writeInt(eventId);

        EPGEvent event = null;
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            event = new EPGEvent();
            initial_epg_event(event, reply);
        }

        request.recycle();
        reply.recycle();

        return event;
    }

    public List<EPGEvent> get_epg_events(long channelID, Date startTime, Date endTime, int pos, int reqNum, int addEmpty) {
        Log.d(TAG, "get_epg_events(EPGEventFilter," + pos + "," + reqNum + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetEventByTime);

        /*1. send channel id*/
        Log.d(TAG, "channelId = " + channelID);
        request.writeInt((int) channelID);
        //Calendar ca = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        /*2. send start time*/
        if (null != startTime) {
            //DtvDate dtvDate = this.timeDate2DtvDate(startDate);

            /*request.writeInt(dtvDate.mYear);
            request.writeInt(dtvDate.mMonth);
            request.writeInt(dtvDate.mDay);
            request.writeInt(dtvDate.mHour);
            request.writeInt(dtvDate.mMinute);
            request.writeInt(dtvDate.mSecond);*/

            request.writeInt(startTime.getYear() + 1900);
            request.writeInt(startTime.getMonth() + 1);
            request.writeInt(startTime.getDate());
            request.writeInt(startTime.getHours());
            request.writeInt(startTime.getMinutes());
            request.writeInt(startTime.getSeconds());

            Log.d(TAG, "EPGTEST get_epg_events:  startTime = " + startTime.getYear() + "/" + startTime.getMonth() + "/" + startTime.getDate() + ", " + startTime.getHours() + ":" + startTime.getMinutes() + ":" + startTime.getSeconds());//eric lin test

            Log.d(TAG, "get_epg_events:  Start Date = " + (startTime.getYear() + 1900) + "/" + (startTime.getMonth() + 1) + "/" + startTime.getDate()
                    + "     " + startTime.getHours() + ":" + startTime.getMinutes() + ":" + startTime.getSeconds());
        } else {
            Log.d(TAG, "getEPGEvents:  Start Date = NULL !!!!!");
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
        }

        /*3. send end time*/
        if (null != endTime) {
            //DtvDate dtvDate = this.timeDate2DtvDate(endDate);

            /*request.writeInt(dtvDate.mYear);
            request.writeInt(dtvDate.mMonth);
            request.writeInt(dtvDate.mDay);
            request.writeInt(dtvDate.mHour);
            request.writeInt(dtvDate.mMinute);
            request.writeInt(dtvDate.mSecond);*/

            request.writeInt(endTime.getYear() + 1900);
            request.writeInt(endTime.getMonth() + 1);
            request.writeInt(endTime.getDate());
            request.writeInt(endTime.getHours());
            request.writeInt(endTime.getMinutes());
            request.writeInt(endTime.getSeconds());

            Log.d(TAG, "EPGTEST get_epg_events:  endTime = " + endTime.getYear() + "/" + endTime.getMonth() + "/" + endTime.getDate() + ", " + endTime.getHours() + ":" + endTime.getMinutes() + ":" + endTime.getSeconds());//eric lin test
        } else {
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
        }

        /*4. send weekday index*/

        /*5. send content level*/

        request.writeInt(pos);
        request.writeInt(reqNum);
        request.writeInt(addEmpty);

        List<EPGEvent> list = null;
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            int retNum = reply.readInt();
            Log.d(TAG, "retNum = " + retNum);
            if (retNum >= 0) {
                list = new ArrayList<>();
                for (int i = 0; i < retNum; i++) {
                    EPGEvent event = new EPGEvent();
                    initial_epg_event(event, reply);
                    list.add(event);
                }
                
                // first 2 EPG events may have wrong order for a short time after dtvservice crash
                // swap them if we found first start time > second start time
                if (retNum >= 2
                        && list.get(0).get_start_time() > list.get(1).get_start_time()) {
                    Collections.swap(list, 0, 1);
                }
            }
        }

        request.recycle();
        reply.recycle();

        return list;
    }

    public String get_short_description(long channelId, int eventId) {
        Log.d(TAG, "get_short_description(" + channelId + "," + eventId + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetShortDesc);
        request.writeInt((int) channelId);
        request.writeInt(eventId);

        String desc = null;
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "get_short_description:  ret = " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            desc = reply.readString();
        }

        request.recycle();
        reply.recycle();
        return desc;
    }

    public String get_detail_description(long channelId, int eventId) {
        Log.d(TAG, "get_detail_description(" + channelId + "," + eventId + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetDetailDesc);
        request.writeInt((int) channelId);
        request.writeInt(eventId);

        String detailDescription = null;
        //LocalExtendedDescription localExtendedDescription = null;
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "get_detail_description:  ret = " + ret);
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            //localExtendedDescription = new LocalExtendedDescription();
            int itemNum = reply.readInt();
            Log.v(TAG, "itemNum = " + itemNum);
            if (itemNum > 0) {
                //HashMap<String, String> map = new LinkedHashMap<String, String>();
                for (int i = 0; i < itemNum; i++) {
                    String key = reply.readString();
                    String value = reply.readString();
                    //Log.v(TAG, "map.put(key = " + key + ",value = " + value + ")");
                    //map.put(key, value);
                }
                //localExtendedDescription.setItemsContent(map);
            }

            detailDescription = reply.readString();
            //localExtendedDescription.setDetailDescription(detailDescription);
        }
        request.recycle();
        reply.recycle();

        //return localExtendedDescription;
        return detailDescription;
    }

    public int set_event_lang(String firstEvtLang, String secondEvtLang) {
        Log.d(TAG, "set_event_lang(" + firstEvtLang + " : " + secondEvtLang + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_SetLanguageCode);

        request.writeString(firstEvtLang);
        //request.writeString(secondEvtLang);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "set_event_lang fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
    public int send_eit_rowdata(int buff_index, int lens) {
        int i;
        //Log.d(TAG, "send_eit_rowdata lens = " + lens);


        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_SendEitRowData);

        request.writeInt(buff_index);
        request.writeInt(lens);

        //request.writeByteArray(data);
        //Log.d(TAG, "data[" + (lens-3) +"] = "+ data[lens-3] + " data[" + (lens-2) +"] = "+ data[lens-2] + " data[" + (lens-1) +"] = "+ data[lens-1]);
        //LogUtils.d("[[Ethan]] invokeex data buff_index = "+buff_index+" lens = "+lens);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "set_event_lang fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
    public int send_eit_rowdata(byte[] data, int lens) {
        int i;
        //Log.d(TAG, "send_eit_rowdata lens = " + lens);


        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_SendEitRowData);

        request.writeInt(lens);

        //request.writeByteArray(data);
        //Log.d(TAG, "data[" + (lens-3) +"] = "+ data[lens-3] + " data[" + (lens-2) +"] = "+ data[lens-2] + " data[" + (lens-1) +"] = "+ data[lens-1]);
        LogUtils.d("[[Ethan]] invokeex data lens = "+lens);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "set_event_lang fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int send_epg_data_id(List<ProgramInfo> programInfo) {
        Log.d(TAG, "send_epg_trip_id");
        int i,size,ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;

        size=programInfo.size();
        if(size > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
            request.writeInt(CMD_EPG_SetEpgDataId);
            request.writeInt(size);
            for(i=0;i<size;i++) {
                request.writeInt((int) programInfo.get(i).getChannelId());
                request.writeInt(programInfo.get(i).getServiceId());
                request.writeInt(programInfo.get(i).getTransportStreamId());
                request.writeInt(programInfo.get(i).getOriginalNetworkId());
            }
            PrimeDtvMediaPlayer.invokeex(request, reply);
            ret = reply.readInt();
            if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
                Log.e(TAG, "send_epg_trip_id fail, ret = " + ret);
            }

            request.recycle();
            reply.recycle();
        }
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int add_epg_data_id(long channelId, int sid, int tid, int onid){
        Log.d(TAG, "add_epg_data_id channelId  = "+channelId+" serviceId = "+sid+" tsid = "+tid+" onid = "+onid);
        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_AddEpgDataId);
        request.writeInt((int)channelId);
        request.writeInt(sid);
        request.writeInt(tid);
        request.writeInt(onid);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "add_epg_data_id fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int delete_epg_data_id(long channelId, int sid, int tid, int onid){
        Log.d(TAG, "delete_epg_data_id channelId  = "+channelId+" serviceId = "+sid+" tsid = "+tid+" onid = "+onid);
        int ret = PrimeDtvMediaPlayer.CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_DelEpgDataId);
        request.writeInt((int)channelId);
        request.writeInt(sid);
        request.writeInt(tid);
        request.writeInt(onid);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS != ret) {
            Log.e(TAG, "delete_epg_data_id fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

}
