package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class TestEPGEventActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;

    final int mTestTotalFuncCount = 8;    // 8 EPG functions
    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_epgevent);

        Init();
    }

    @Override
    public void onBackPressed() {

        TestMidMain tm = new TestMidMain();
        // if all funcs are tested, set checked = true
        if ( mTestedFuncSet.size() == mTestTotalFuncCount )
        {
            int result = 0;
            tm.getTestInfoByIndex(mPosition).setChecked(true);

            // send error item to testMidMain
            for (int index : mErrorIndexSet) {
                result = tm.bitwiseLeftShift(result, index, false);    //fail item
            }

            tm.getTestInfoByIndex(mPosition).setResult(result);
        }

        super.onBackPressed();
    }


    public void BtnEPGEventGetPresentEvent_OnClick(View view)
    {
        final int btnIndex = 0; // it is first button
        int epgSize;
        try
        {
            List<ProgramInfo> programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);

            if ( programInfoList == null || programInfoList.isEmpty() )
            {
                GotError(view, "No ALL_TV_TYPE Program!", btnIndex);
                return;
            }

            ProgramInfo programInfo = programInfoList.get(0);
            StringBuilder result = new StringBuilder();
            EPGEvent presentEpg = EpgEventGetPresentEvent(programInfo.getChannelId());
            result.append("ChannelId = ").append(programInfo.getChannelId()).append("\n");
            result.append("Channel Name =").append(programInfo.getDisplayName()).append("\n\n");

            if ( presentEpg == null ) // null = get nothing = pass
            {
                TestPass(view, "EPGEventGetPresentEvent() Pass!", 0);
                return;
            }

            // show content of epg
            result.append("ServiceID : ").append(presentEpg.getSid()).append("\n");
            result.append("OriNetID : ").append(presentEpg.getOriginalNetworkId()).append("\n");
            result.append("TStreamID : ").append(presentEpg.getTransportStreamId()).append("\n");
            result.append("EventID : ").append(presentEpg.getEventId()).append("\n");
            result.append("TableID : ").append(presentEpg.getTableId()).append("\n");
            result.append("EventType : ").append(presentEpg.getEventType()).append("\n");
            result.append("EventName : ").append(presentEpg.getEventName()).append("\n");
            result.append("EventNameCodec : ").append(presentEpg.getEventNameLangCodec()).append("\n");
            result.append("Start Time : ").append( new Date(presentEpg.getStartTime()).toString()).append("\n");
            result.append("End Time : ").append(new Date(presentEpg.getEndTime()).toString()).append("\n");
            result.append("Duration : ").append(presentEpg.getDuration()).append("\n");
            result.append("ParentalRate : ").append(presentEpg.getParentalRate()).append("\n");
            result.append("ShortEvent : ").append(presentEpg.getShortEvent()).append("\n");
            result.append("ShortEventCodec : ").append(presentEpg.getShortEventLangCodec()).append("\n");
            result.append("ExtEvent : ").append(presentEpg.getExtendedEvent()).append("\n");
            result.append("ExtEventCodec : ").append(presentEpg.getExtendedEventLangCodec()).append("\n\n");

            ShowResult(result.toString());
            epgSize = 1;
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }

        TestPass(view, "EPGEventGetPresentEvent() Pass!", epgSize);
    }

    public void BtnEPGEventGetFollowEvent_OnClick(View view)
    {
        final int btnIndex = 1;
        int epgSize;
        try
        {
            List<ProgramInfo> programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);

            if ( programInfoList == null || programInfoList.isEmpty() )
            {
                GotError(view, "No ALL_TV_TYPE Program!", btnIndex);
                return;
            }

            ProgramInfo programInfo = programInfoList.get(0);
            StringBuilder result = new StringBuilder();
            EPGEvent followEpg = EpgEventGetFollowEvent(programInfo.getChannelId());
            result.append("ChannelId = ").append(programInfo.getChannelId()).append("\n");
            result.append("Channel Name =").append(programInfo.getDisplayName()).append("\n\n");

            if ( followEpg == null ) // null = get nothing = pass
            {
                TestPass(view, "EPGEventGetFollowEvent() Pass!", 0);
                return;
            }

            // show content of epg
            result.append("ServiceID : ").append(followEpg.getSid()).append("\n");
            result.append("OriNetID : ").append(followEpg.getOriginalNetworkId()).append("\n");
            result.append("TStreamID : ").append(followEpg.getTransportStreamId()).append("\n");
            result.append("EventID : ").append(followEpg.getEventId()).append("\n");
            result.append("TableID : ").append(followEpg.getTableId()).append("\n");
            result.append("EventType : ").append(followEpg.getEventType()).append("\n");
            result.append("EventName : ").append(followEpg.getEventName()).append("\n");
            result.append("EventNameCodec : ").append(followEpg.getEventNameLangCodec()).append("\n");
            result.append("StartTime : ").append( new Date(followEpg.getStartTime()).toString()).append("\n");
            result.append("EndTime : ").append(new Date(followEpg.getEndTime()).toString()).append("\n");
            result.append("Duration : ").append(followEpg.getDuration()).append("\n");
            result.append("ParentalRate : ").append(followEpg.getParentalRate()).append("\n");
            result.append("ShortEvent : ").append(followEpg.getShortEvent()).append("\n");
            result.append("ShortEventCodec : ").append(followEpg.getShortEventLangCodec()).append("\n");
            result.append("ExtEvent : ").append(followEpg.getExtendedEvent()).append("\n");
            result.append("ExtEventCodec : ").append(followEpg.getExtendedEventLangCodec()).append("\n\n");

            ShowResult(result.toString());
            epgSize = 1;
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }

        TestPass(view, "EPGEventGetFollowEvent() Pass!", epgSize);
    }

    public void BtnEpgEventGetEPGByEventId_OnClick(View view)
    {
        final int btnIndex = 2;
        int epgSize;
        try
        {
            List<ProgramInfo> programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);

            if ( programInfoList == null || programInfoList.isEmpty() )
            {
                GotError(view, "No ALL_TV_TYPE Program!", btnIndex);
                return;
            }

            ProgramInfo programInfo = programInfoList.get(0);
            EPGEvent presentEpg = EpgEventGetPresentEvent(programInfo.getChannelId());
            if ( presentEpg == null ) // null = get nothing = pass
            {
                TestPass(view, "Test Channel No Present EPG !", 0);
                return;
            }
            StringBuilder result = new StringBuilder();
            EPGEvent epg = EpgEventGetEPGByEventId(programInfo.getChannelId(), presentEpg.getEventId());
            result.append("ChannelId = ").append(programInfo.getChannelId()).append("\n");
            result.append("Channel Name = ").append(programInfo.getDisplayName()).append("\n");
            result.append("EventId = ").append(presentEpg.getEventId()).append("\n\n");

            if ( epg == null ) // null = get nothing = pass
            {
                TestPass(view, "EpgEventGetEPGByEventId() Pass!", 0);
                return;
            }

            // show content of epg
            result.append("ServiceID : ").append(epg.getSid()).append("\n");
            result.append("OriNetID : ").append(epg.getOriginalNetworkId()).append("\n");
            result.append("TStreamID : ").append(epg.getTransportStreamId()).append("\n");
            result.append("EventID : ").append(epg.getEventId()).append("\n");
            result.append("TableID : ").append(epg.getTableId()).append("\n");
            result.append("EventType : ").append(epg.getEventType()).append("\n");
            result.append("EventName : ").append(epg.getEventName()).append("\n");
            result.append("EventNameCodec : ").append(epg.getEventNameLangCodec()).append("\n");
            result.append("Start Time : ").append( new Date(epg.getStartTime()).toString()).append("\n");
            result.append("End Time : ").append(new Date(epg.getEndTime()).toString()).append("\n");
            result.append("Duration : ").append(epg.getDuration()).append("\n");
            result.append("ParentalRate : ").append(epg.getParentalRate()).append("\n");
            result.append("ShortEvent : ").append(epg.getShortEvent()).append("\n");
            result.append("ShortEventCodec : ").append(epg.getShortEventLangCodec()).append("\n");
            result.append("ExtEvent : ").append(epg.getExtendedEvent()).append("\n");
            result.append("ExtEventCodec : ").append(epg.getExtendedEventLangCodec()).append("\n\n");

            ShowResult(result.toString());
            epgSize = 1;
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }

        TestPass(view, "EpgEventGetEPGByEventId() Pass!", epgSize);
    }

    public void BtnEpgEventGetEPGEventList_OnClick(View view)
    {
        final int btnIndex = 3;
        final long startTime;
        final long endTime;

        int epgSize;

        try
        {
            List<ProgramInfo> programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);

            if ( programInfoList == null || programInfoList.isEmpty() )
            {
                GotError(view, "No ALL_TV_TYPE Program!", btnIndex);
                return;
            }

            ProgramInfo programInfo = programInfoList.get(0);
            StringBuilder result = new StringBuilder();
            Date date = GetLocalTime();
            startTime = date.getTime();
            endTime = startTime + 24 * 60 * 60 * 1000; // endTime = startTime + 1day
            int dayOffSet = 0;
            Calendar tmpCalendar = getDateByOffset(dayOffSet);
                /*2. set start day*/
            Date startDate = tmpCalendar.getTime();

            if (0 != dayOffSet)
            {
                startDate.setHours(0);
                startDate.setMinutes(0);
                startDate.setSeconds(0);
            }
                /*3. set end day*/
            tmpCalendar.add(Calendar.DAY_OF_MONTH, +1);

            Date endDate = tmpCalendar.getTime();
            endDate.setHours(0);
            endDate.setMinutes(0);
            endDate.setSeconds(0);
            List<EPGEvent> epgEventList = EpgEventGetEPGEventList(programInfo.getChannelId(), startDate.getTime(), endDate.getTime(), 0);
            result.append("ChannelId = ").append(programInfo.getChannelId()).append("\n");
            result.append("Channel Name =").append(programInfo.getDisplayName()).append("\n\n");
            result.append("StartTime = ").append(startTime).append(" ").append(date.toString()).append("\n");
            result.append("EndTime = ").append(endTime).append(" ").append(new Date(endTime).toString()).append("\n\n");

            if ( epgEventList == null ) // null = get nothing = pass
            {
                TestPass(view, "EpgEventGetEPGEventList() Pass!", 0);
                return;
            }

            for ( int i = 0 ; i < epgEventList.size() ; i++ )
            {
                EPGEvent epgEvent = epgEventList.get(i);
                //result.append(epgEvent.ToString()).append("\n\n");
                result.append("ServiceID : ").append(epgEvent.getSid()).append("\n");
                result.append("OriNetID : ").append(epgEvent.getOriginalNetworkId()).append("\n");
                result.append("TStreamID : ").append(epgEvent.getTransportStreamId()).append("\n");
                result.append("EventID : ").append(epgEvent.getEventId()).append("\n");
                result.append("TableID : ").append(epgEvent.getTableId()).append("\n");
                result.append("EventType : ").append(epgEvent.getEventType()).append("\n");
                result.append("EventName : ").append(epgEvent.getEventName()).append("\n");
                result.append("EventNameCodec : ").append(epgEvent.getEventNameLangCodec()).append("\n");
                result.append("Start Time : ").append( new Date(epgEvent.getStartTime()).toString()).append("\n");
                result.append("End Time : ").append(new Date(epgEvent.getEndTime()).toString()).append("\n");
                result.append("Duration : ").append(epgEvent.getDuration()).append("\n");
                result.append("ParentalRate : ").append(epgEvent.getParentalRate()).append("\n");
                result.append("ShortEvent : ").append(epgEvent.getShortEvent()).append("\n");
                result.append("ShortEventCodec : ").append(epgEvent.getShortEventLangCodec()).append("\n");
                result.append("ExtEvent : ").append(epgEvent.getExtendedEvent()).append("\n");
                result.append("ExtEventCodec : ").append(epgEvent.getExtendedEventLangCodec()).append("\n\n");
            }

            ShowResult(result.toString());
            epgSize = epgEventList.size();
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }

        TestPass(view, "EpgEventGetEPGEventList() Pass!", epgSize);
    }

    public void BtnEpgEventGetShortDescription_OnClick(View view)
    {
        final int btnIndex = 4;
        int epgSize;
        try
        {
            List<ProgramInfo> programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);

            if ( programInfoList == null || programInfoList.isEmpty() )
            {
                GotError(view, "No ALL_TV_TYPE Program!", btnIndex);
                return;
            }

            ProgramInfo programInfo = programInfoList.get(0);
            EPGEvent presentEpg = EpgEventGetPresentEvent(programInfo.getChannelId());
            if ( presentEpg == null ) // null = get nothing = pass
            {
                TestPass(view, "Test Channel No Present EPG !", 0);
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append("Channel Name =").append(programInfo.getDisplayName()).append("\n");
            result.append("event ID =").append(presentEpg.getEventId()).append("\n");
            String description = EpgEventGetShortDescription(programInfo.getChannelId(), presentEpg.getEventId());
            result.append("ShortEvent : ").append(description).append("\n");
            ShowResult(result.toString());

            ShowResult(result.toString());
            epgSize = 1;
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }


//        TestPass(view, "Skip Test!", epgSize);
        TestPass(view, "EpgEventGetShortDescription() Pass!", epgSize);
    }

    public void BtnEpgEventGetDetailDescription_OnClick(View view)
    {
        final int btnIndex = 5;
        int epgSize = 0;
        try
        {
            List<ProgramInfo> programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);

            if ( programInfoList == null || programInfoList.isEmpty() )
            {
                GotError(view, "No ALL_TV_TYPE Program!", btnIndex);
                return;
            }

            ProgramInfo programInfo = programInfoList.get(0);
            EPGEvent presentEpg = EpgEventGetPresentEvent(programInfo.getChannelId());
            if ( presentEpg == null ) // null = get nothing = pass
            {
                TestPass(view, "Test Channel No Present EPG !", 0);
                return;
            }

            StringBuilder result = new StringBuilder();

            result.append("Channel Name =").append(programInfo.getDisplayName()).append("\n");
            result.append("event ID =").append(presentEpg.getEventId()).append("\n");
            String description = EpgEventGetDetailDescription(programInfo.getChannelId(), presentEpg.getEventId());
            result.append("DetailDescription : ").append(description).append("\n");

            ShowResult(result.toString());
            epgSize = 1;
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }

//        TestPass(view, "Skip Test!", epgSize);
        TestPass(view, "EpgEventGetDetailDescription() Pass!", epgSize);
    }

    public void BtnEpgEventSetLanguageCode_OnClick(View view)
    {
        final int btnIndex = 6;

        try
        {
            StringBuilder result = new StringBuilder();
            ShowResult(result.toString());
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }

        TestPass(view, "Skip Test!", 0);
//        TestPass(view, "EpgEventSetLanguageCode() Pass!", epgSize);
    }

    public void BtnEpgEventStartEPG_OnClick(View view)
    {
        final int btnIndex = 7;

        try
        {
            StringBuilder result = new StringBuilder();
            ShowResult(result.toString());
        }
        catch (Exception e)
        {
            GotError(view, e.toString(), btnIndex);
            return;
        }

        TestPass(view, "Skip Test!", 0);
//        TestPass(view, "EpgEventStartEPG() Pass!", epgSize);
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTEPGEVENT_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTEPGEVENT_LAYOUT_HELP);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestEPG");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

    }

    private void GotError(View view, String errorMsg, int btnIndex)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());
    }

    private void TestPass(View view, String msg, int epgSize)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg + "\nEPGEventSize : " + epgSize );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());
    }

    // show result(string) on textview
    private void ShowResult(String result)
    {
        TextView textView = (TextView) findViewById(R.id.ID_TESTEPGEVENT_TXV_RESULT);
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }

    private Calendar getDateByOffset(int dayOffSet)
    {
        Date date = GetLocalTime();
        if (date == null)
        {
            date = new Date();
        }

        long dateOffsetMills = dayOffSet * (24*60*60*1000);
        long dateMills = date.getTime() + dateOffsetMills;
        date.setTime(dateMills);
        Calendar ca = Calendar.getInstance();
        if (TmGetCurrentTimeZone() != 0)
        {
            ca.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        ca.setTime(date);

        return ca;
    }
}
