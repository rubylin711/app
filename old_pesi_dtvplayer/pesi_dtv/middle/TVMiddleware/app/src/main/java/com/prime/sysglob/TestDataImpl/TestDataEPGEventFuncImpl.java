package com.prime.sysglob.TestDataImpl;

import android.content.Context;
import android.util.Log;

import com.prime.TestData.TestData;
import com.prime.sysdata.EPGEvent;
import com.prime.sysglob.EPGEventFunc;
import com.prime.tvclient.TestDataTVClient;

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
                    && curEPGEvent.getTransportStreamId() == transportStreamId )
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
        EpgEvent.setStartTimeUtcM(curEPGEvent.getStartTimeUtcM());
        EpgEvent.setStartTimeUtcL(curEPGEvent.getStartTimeUtcL());
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
