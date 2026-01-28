package com.prime.dtvplayer.TestData.sysglob.TestDataImpl;

import android.content.Context;
import android.util.Log;

import com.prime.dtvplayer.TestData.TestData.TestData;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.TestData.sysglob.EPGEventFunc;
import com.prime.dtvplayer.TestData.tvclient.TestDataTVClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnny_shih on 2017/11/29.
 */

public class TestDataEPGEventFuncImpl implements EPGEventFunc {

    private static final String TAG="TestDataEPGEventFuncImpl";
    private Context context;
    private TestData testData = null;

    public TestDataEPGEventFuncImpl(Context context)
    {
        this.context = context;
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public List<EPGEvent> getEventPF(int sid, int transportStreamId, int originalNetworkId) {
        return queryPF(sid, transportStreamId, originalNetworkId);
    }

    @Override
    public List<EPGEvent> getEventSchedule(int sid, int transportStreamId, int originalNetworkId) {
        return querySchedule(sid, transportStreamId, originalNetworkId);
    }

    //@Override
    public List<EPGEvent> getEventSchedule(int sid, int transportStreamId, int originalNetworkId, long startTime, long endTime, int addEmpty) {
        return querySchedule(sid, transportStreamId, originalNetworkId, startTime, endTime, addEmpty);
    }

    private List<EPGEvent> queryPF(int sid, int transportStreamId, int originalNetworkId) {

        List<EPGEvent> epgEventList_PF = testData.GetTestDatEpgEventList_PF();
        if(epgEventList_PF == null || epgEventList_PF.isEmpty()) {
            return null;
        }

        List<EPGEvent> epgEventList = new ArrayList<>();
        EPGEvent EpgEvent;
        for ( int i = 0 ; i < epgEventList_PF.size() ; i++ )
        {
            EPGEvent curEPGEvent = epgEventList_PF.get(i);
            if (curEPGEvent.getSid() == sid
                    && curEPGEvent.getOriginalNetworkId() == originalNetworkId
                    && curEPGEvent.getTransportStreamId() == transportStreamId )
            {
                EpgEvent = ParseCursor(curEPGEvent);
                epgEventList.add(EpgEvent);
            }
        }

        if ( epgEventList.isEmpty() )
        {
            return null;
        }
        else
        {
            return epgEventList;
        }
    }

    private  EPGEvent addEmptyEvent(int sid, int transportStreamId, int originalNetworkId, long startTime, long endTime){
        EPGEvent epgEvent;
        epgEvent = new EPGEvent();
        epgEvent.setSid(sid);
        epgEvent.setOriginalNetworkId(originalNetworkId);
        epgEvent.setTransportStreamId(transportStreamId);
        epgEvent.setEventId(1);  // eventId = preEventId++
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        //epgEvent.setStartTimeUtcM( Long.decode(strTimeUtcM) );  // HexStr("0xXXXX")->Long
//        epgEvent.setStartTimeUtcM( timeUtcM );
//        epgEvent.setStartTimeUtcL( GetLongByHMS(timeUtcLHour, timeUtcLMin, timeUtcLSec) );
//        epgEvent.setDuration( GetLongByHMS(durationH, durationM, durationS) );
        epgEvent.setStartTime(startTime);
        epgEvent.setDuration(endTime-startTime);
        epgEvent.setEndTime(endTime);
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        return epgEvent;
    }
    private List<EPGEvent> querySchedule(int sid, int transportStreamId, int originalNetworkId, long startTime, long endTime, int addEmpty) {
        List<EPGEvent> epgEventList_Schedule = testData.GetTestDatEpgEventList_Schedule();
        long curEventStartTime =0;
        long curEventEndTime =0;

        if(epgEventList_Schedule == null || epgEventList_Schedule.isEmpty()) {
            Log.d(TAG, "querySchedule: null or Empty");//eric lin test
            return null;
        }

        List<EPGEvent> EpgEventList = new ArrayList<>();
        EPGEvent EpgEvent;
        for ( int i = 0 ; i < epgEventList_Schedule.size() ; i++ )
        {
            EPGEvent curEPGEvent = epgEventList_Schedule.get(i);
            curEventStartTime = curEPGEvent.getStartTime();
            curEventEndTime = curEPGEvent.getEndTime();
            if (curEPGEvent.getSid() == sid
                    && curEPGEvent.getOriginalNetworkId() == originalNetworkId
                    && curEPGEvent.getTransportStreamId() == transportStreamId
                    && ((curEventStartTime >= startTime && curEventStartTime <= endTime) || (curEventEndTime >= startTime && curEventEndTime <= endTime))
                    )
            {
                EpgEvent = ParseCursor(curEPGEvent);
                EpgEventList.add(EpgEvent);
            }
        }

        if ( EpgEventList.isEmpty() )
        {            
            //add EmptyEvent
            EpgEventList.add(addEmptyEvent(sid, transportStreamId, originalNetworkId, startTime, endTime));
            return EpgEventList;

            //return null;
        }
        else
        {            
            return EpgEventList;
        }
    }
    private List<EPGEvent> querySchedule(int sid, int transportStreamId, int originalNetworkId) {
        List<EPGEvent> epgEventList_Schedule = testData.GetTestDatEpgEventList_Schedule();

        if(epgEventList_Schedule == null || epgEventList_Schedule.isEmpty()) {            
            return null;
        }

        List<EPGEvent> EpgEventList = new ArrayList<>();
        EPGEvent EpgEvent;
        for ( int i = 0 ; i < epgEventList_Schedule.size() ; i++ )
        {
            EPGEvent curEPGEvent = epgEventList_Schedule.get(i);
            if (curEPGEvent.getSid() == sid
                    && curEPGEvent.getOriginalNetworkId() == originalNetworkId
                    && curEPGEvent.getTransportStreamId() == transportStreamId)
            {
                EpgEvent = ParseCursor(curEPGEvent);
                EpgEventList.add(EpgEvent);
            }
        }

        if ( EpgEventList.isEmpty() )
        {
            return null;
        }
        else
        {
            return EpgEventList;
        }
    }

    private EPGEvent ParseCursor(EPGEvent curEPGEvent){
        EPGEvent EpgEvent = new EPGEvent();
        EpgEvent.setSid(curEPGEvent.getSid());
        EpgEvent.setOriginalNetworkId(curEPGEvent.getOriginalNetworkId());
        EpgEvent.setTransportStreamId(curEPGEvent.getTransportStreamId());
        EpgEvent.setEventId(curEPGEvent.getEventId());
        EpgEvent.setTableId(curEPGEvent.getTableId());
        EpgEvent.setEventType((curEPGEvent.getEventType()));
        EpgEvent.setEventName(curEPGEvent.getEventName());
        EpgEvent.setEventNameLangCodec(curEPGEvent.getEventNameLangCodec());
        EpgEvent.setStartTime(curEPGEvent.getStartTime());
        EpgEvent.setEndTime(curEPGEvent.getEndTime());
        EpgEvent.setDuration(curEPGEvent.getDuration());
        EpgEvent.setParentalRate(curEPGEvent.getParentalRate());
        EpgEvent.setShortEvent(curEPGEvent.getShortEvent());
        EpgEvent.setShortEventLangCodec(curEPGEvent.getShortEventLangCodec());
        EpgEvent.setExtendedEvent(curEPGEvent.getExtendedEvent());
        EpgEvent.setExtendedEventLangCodec(curEPGEvent.getExtendedEventLangCodec());

        //Log.i(TAG, "ParseCursor EpgEvent : "+EpgEvent.ToString());
        return EpgEvent;
    }
}
