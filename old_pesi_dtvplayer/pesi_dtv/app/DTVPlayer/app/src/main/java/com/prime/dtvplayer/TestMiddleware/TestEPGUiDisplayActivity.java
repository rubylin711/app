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
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class TestEPGUiDisplayActivity extends DTVActivity {

    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 7;  // 7 epguidisplay functions

    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    StringBuilder mStrInput;
    StringBuilder mStrOutput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_epgui_display);

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
    public void BtnEpgUiDisplay_OnClick(View view)
    {
        final int btnIndex = 0;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        int type;
        EpgUiDisplay epgUiDisplay;
        List<SimpleChannel> simpleChannelList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_TV_TYPE ProgramInfo\n");
            }
            else
            {
                mStrInput.append("EpgUiDisplay() : Type = ").append(type).append("\n");
                mStrOutput.append("SimpleChannelList : Size = ").append(simpleChannelList.size()).append("\n");

                // show simplechannel
                for ( int i = 0 ; i < simpleChannelList.size() ; i++ )
                {
                    SimpleChannel simpleChannel = simpleChannelList.get(i);
                    mStrInput.append("SimpleChannelList[ ").append(i).append(" ] : \n");
                    mStrOutput.append("SimpleChannel ").append(i).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChId = ").append(simpleChannel.getChannelId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChNum = ").append(simpleChannel.getChannelNum()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChName = ").append(simpleChannel.getChannelName()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("CA = ").append(simpleChannel.getCA()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("UserLock = ").append(simpleChannel.getUserLock()).append("\n");
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            type = ProgramInfo.ALL_RADIO_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_RADIO_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_RADIO_TYPE ProgramInfo\n");
            }
            else
            {
                mStrInput.append("EpgUiDisplay() : Type = ").append(type).append("\n");
                mStrOutput.append("SimpleChannelList : Size = ").append(simpleChannelList.size()).append("\n");

                // show simplechannel
                for ( int i = 0 ; i < simpleChannelList.size() ; i++ )
                {
                    SimpleChannel simpleChannel = simpleChannelList.get(i);
                    mStrInput.append("SimpleChannelList[ ").append(i).append(" ] : \n");
                    mStrOutput.append("SimpleChannel ").append(i).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChId = ").append(simpleChannel.getChannelId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChNum = ").append(simpleChannel.getChannelNum()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChName = ").append(simpleChannel.getChannelName()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("CA = ").append(simpleChannel.getCA()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("UserLock = ").append(simpleChannel.getUserLock()).append("\n");
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test2 -end

            // test3 -start
            mStrInput.append("Input Test3 : \n");
            mStrOutput.append("Output Test3 : \n");

            type = ProgramInfo.TV_FAV1_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;

            if ( simpleChannelList == null )
            {
                mStrInput.append("No TV_FAV1_TYPE ProgramInfo\n");
                mStrOutput.append("No TV_FAV1_TYPE ProgramInfo\n");
            }
            else
            {
                mStrInput.append("EpgUiDisplay() : Type = ").append(type).append("\n");
                mStrOutput.append("SimpleChannelList : Size = ").append(simpleChannelList.size()).append("\n");

                // show simplechannel
                for ( int i = 0 ; i < simpleChannelList.size() ; i++ )
                {
                    SimpleChannel simpleChannel = simpleChannelList.get(i);
                    mStrInput.append("SimpleChannelList[ ").append(i).append(" ] : \n");
                    mStrOutput.append("SimpleChannel ").append(i).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChId = ").append(simpleChannel.getChannelId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChNum = ").append(simpleChannel.getChannelNum()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("ChName = ").append(simpleChannel.getChannelName()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("CA = ").append(simpleChannel.getCA()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("UserLock = ").append(simpleChannel.getUserLock()).append("\n");
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test3 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "EpgUiDisplay() Pass!");
    }

    public void BtnEitDataUpdate_OnClick(View view)
    {
        final int btnIndex = 1;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        int type;
        EpgUiDisplay epgUiDisplay;
        List<SimpleChannel> simpleChannelList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE ProgramInfo", btnIndex);
                return;
            }
            else
            {
                // get time and update eitdata, time = impossible time
                SimpleChannel simpleChannel = simpleChannelList.get(0);
                List<EPGEvent> epgEventList = EpgEventGetEPGEventList(simpleChannel.getChannelId(), 0, 0, 0);  // Time is useless now 20180119
                if ( epgEventList == null )
                {
                    mStrInput.append("No EpgEvent of ProgramInfoList[0]\n");
                    mStrOutput.append("No EpgEvent of ProgramInfoList[0]\n");
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "No EpgEvent of ProgramInfoList[0]", btnIndex);
                    return;
                }

                long epgStartTime = 0;
                long epgEndTime = 0;
                epgUiDisplay.EitDataUpdate(0, epgStartTime, epgEndTime, 0);
                mStrInput.append("EitDataUpdate()\n");
                mStrOutput.append("Time = ").append(epgStartTime).append(" to ").append(epgEndTime).append("\n");

                // update eitdisplaydata
                int sizeBefore = epgUiDisplay.epgDisplayData.size();
                epgUiDisplay.EitDataDisplayUpdate();
                int sizeAfter = epgUiDisplay.epgDisplayData.size();
                mStrInput.append("EitDataDisplayUpdate() : \n");
                mStrInput.append("\n");
                mStrOutput.append("Before update size = ").append(sizeBefore).append("\n");
                mStrOutput.append("After update size = ").append(sizeAfter).append("\n");

                // epgUiDisplay.epgDisplayData(epgUpdateData) should be empty
                if ( !epgUiDisplay.epgDisplayData.isEmpty() )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "epgUpdateData is not empty", btnIndex);
                    return;
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE ProgramInfo", btnIndex);
                return;
            }
            else
            {
                // get time and update eitdata, time = epgUiDisplay.GetStartTime(epg[0])
                SimpleChannel simpleChannel = simpleChannelList.get(0);
                List<EPGEvent> epgEventList = EpgEventGetEPGEventList(simpleChannel.getChannelId(), 0 ,0, 0);  // Time is useless now 20180119

                if ( epgEventList == null )
                {
                    mStrInput.append("No EpgEvent of ProgramInfoList[0]\n");
                    mStrOutput.append("No EpgEvent of ProgramInfoList[0]\n");
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "No EpgEvent of ProgramInfoList[0]", btnIndex);
                    return;
                }

                long epgStartTime = epgEventList.get(0).getStartTime();
                long epgEndTime = epgEventList.get(0).getEndTime();
                epgUiDisplay.EitDataUpdate(0, epgStartTime, epgEndTime, 0);
                mStrInput.append("EitDataUpdate()\n");
                mStrOutput.append("Time = ").append(epgStartTime).append(" to ").append(epgEndTime).append("\n");

                // update eitdisplaydata
                int sizeBefore = epgUiDisplay.epgDisplayData.size();
                epgUiDisplay.EitDataDisplayUpdate();
                int sizeAfter = epgUiDisplay.epgDisplayData.size();
                mStrInput.append("EitDataDisplayUpdate() : \n");
                mStrInput.append("\n");
                mStrOutput.append("Before update size = ").append(sizeBefore).append("\n");
                mStrOutput.append("After update size = ").append(sizeAfter).append("\n");

                // epgEvenGetPF
                EPGEvent present = EpgEventGetPresentEvent(simpleChannel.getChannelId());
                EPGEvent follow = EpgEventGetFollowEvent(simpleChannel.getChannelId());
                if ( present == null || follow == null )
                {
                    mStrInput.append("No EpgEvent of ProgramInfoList[0]\n");
                    mStrOutput.append("No EpgEvent of ProgramInfoList[0]\n");
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "No EpgEvent of ProgramInfoList[0]", btnIndex);
                    return;
                }

                List<EPGEvent> eventListPF = new ArrayList<>();
                eventListPF.add(present);
                eventListPF.add(follow);

                // show EventId, Sid, TransportSId, OriNetId, EventName
                for ( int i = 0 ; i < epgUiDisplay.epgDisplayData.size() ; i++ )
                {
                    EPGEvent epgEvent = epgUiDisplay.epgDisplayData.get(i);
                    EPGEvent epgEventPF = eventListPF.get(i);
                    mStrInput.append("EpgEventPF[ ").append(i).append(" ] : \n");
                    mStrOutput.append("EpgUpdateData ").append(i).append("\n");

                    mStrInput.append("Sid = ").append(epgEventPF.getSid()).append("\n");
                    mStrOutput.append("Sid = ").append(epgEvent.getSid()).append("\n");

                    mStrInput.append("OriNetId = ").append(epgEventPF.getOriginalNetworkId()).append("\n");
                    mStrOutput.append("OriNetId = ").append(epgEvent.getOriginalNetworkId()).append("\n");

                    mStrInput.append("TransportSid = ").append(epgEventPF.getTransportStreamId()).append("\n");
                    mStrOutput.append("TransportSId = ").append(epgEvent.getTransportStreamId()).append("\n");

                    mStrInput.append("EventId = ").append(epgEventPF.getEventId()).append("\n");
                    mStrOutput.append("EventId = ").append(epgEvent.getEventId()).append("\n");

                    mStrInput.append("TableId = ").append(epgEventPF.getTableId()).append("\n");
                    mStrOutput.append("TableId = ").append(epgEvent.getTableId()).append("\n");

                    mStrInput.append("EventType = ").append(epgEventPF.getEventType()).append("\n");
                    mStrOutput.append("EventType = ").append(epgEvent.getEventType()).append("\n");

                    mStrInput.append("EventName = ").append(epgEventPF.getEventName()).append("\n");
                    mStrOutput.append("EventName = ").append(epgEvent.getEventName()).append("\n");

                    mStrInput.append("ENLangCodec = ").append(epgEventPF.getTableId()).append("\n");
                    mStrOutput.append("ENLangCodec = ").append(epgEvent.getTableId()).append("\n");

                    mStrInput.append("StartTime = ").append(epgEventPF.getStartTime()).append("\n");
                    mStrOutput.append("StartTime = ").append(epgEvent.getStartTime()).append("\n");

                    mStrInput.append("EndTime = ").append(epgEventPF.getEndTime()).append("\n");
                    mStrOutput.append("EndTime = ").append(epgEvent.getEndTime()).append("\n");

                    mStrInput.append("Duration = ").append(epgEventPF.getDuration()).append("\n");
                    mStrOutput.append("Duration = ").append(epgEvent.getDuration()).append("\n");

                    mStrInput.append("ParentalRate = ").append(epgEventPF.getParentalRate()).append("\n");
                    mStrOutput.append("ParentalRate = ").append(epgEvent.getParentalRate()).append("\n");

                    mStrInput.append("ShortEvent = ").append(epgEventPF.getShortEvent()).append("\n");
                    mStrOutput.append("ShortEvent = ").append(epgEvent.getShortEvent()).append("\n");

                    mStrInput.append("SELangCodec = ").append(epgEventPF.getShortEventLangCodec()).append("\n");
                    mStrOutput.append("SELangCodec = ").append(epgEvent.getShortEventLangCodec()).append("\n");

                    mStrInput.append("ExtEvent = ").append(epgEventPF.getExtendedEvent()).append("\n");
                    mStrOutput.append("ExtEvent = ").append(epgEvent.getExtendedEvent()).append("\n");

                    mStrInput.append("ExtELangCodec = ").append(epgEventPF.getExtendedEventLangCodec()).append("\n");
                    mStrOutput.append("ExtELangCodec = ").append(epgEvent.getExtendedEventLangCodec()).append("\n");

                }

                // compare epgUiDisplay.epgDisplayData(epgUpdateData) and eventListPF
                for ( int i = 0 ; i < eventListPF.size() ; i++ )
                {
                    EPGEvent epgEvent = epgUiDisplay.epgDisplayData.get(i);
                    EPGEvent epgEventPF = eventListPF.get(i);
                    if ( !epgEvent.ToString().equals(epgEventPF.ToString()) )
                    {
                        ShowResultOnTXV(mStrInput.toString(), txvInput);
                        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                        GotError(view, "EpgUpdateData is different from EpgEventGetPF()", btnIndex);
                        return;
                    }
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test2 -end

            // test3 -start
            mStrInput.append("Input Test3 : \n");
            mStrOutput.append("Output Test3 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE ProgramInfo", btnIndex);
                return;
            }
            else
            {
                // get time and update eitdata, time = last half of epgEventList
                SimpleChannel simpleChannel = simpleChannelList.get(0);
                List<EPGEvent> epgEventList = EpgEventGetEPGEventList(simpleChannel.getChannelId(), 0, 0, 0);  // Time is useless now 20180119

                if ( epgEventList == null )
                {
                    mStrInput.append("No EpgEvent of ProgramInfoList[0]\n");
                    mStrOutput.append("No EpgEvent of ProgramInfoList[0]\n");
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "No EpgEvent of ProgramInfoList[0]", btnIndex);
                    return;
                }

                int sizeEpgEventList = epgEventList.size();
                long epgStartTime = epgEventList.get(sizeEpgEventList/2).getStartTime();
                long epgEndTime = epgEventList.get(sizeEpgEventList-1).getEndTime();
                epgUiDisplay.EitDataUpdate(0, epgStartTime, epgEndTime, 0);
                mStrInput.append("EitDataUpdate()\n");
                mStrOutput.append("Time = ").append(epgStartTime).append(" to ").append(epgEndTime).append("\n");

                // update eitdisplaydata
                int sizeBefore = epgUiDisplay.epgDisplayData.size();
                epgUiDisplay.EitDataDisplayUpdate();
                int sizeAfter = epgUiDisplay.epgDisplayData.size();
                mStrInput.append("EitDataDisplayUpdate() : \n");
                mStrInput.append("\n");
                mStrOutput.append("Before update size = ").append(sizeBefore).append("\n");
                mStrOutput.append("After update size = ").append(sizeAfter).append("\n");

                // show EventId, Sid, TransportSId, OriNetId, EventName
                for ( int i = 0 ; i < epgUiDisplay.epgDisplayData.size() ; i++ )
                {
                    EPGEvent epgEvent = epgUiDisplay.epgDisplayData.get(i);
                    mStrInput.append("epgUpdateData[ ").append(i).append(" ] : \n");
                    mStrOutput.append("EpgEvent ").append(i).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("EventId = ").append(epgEvent.getEventId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Sid = ").append(epgEvent.getSid()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("TransportSId = ").append(epgEvent.getTransportStreamId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("OriNetId = ").append(epgEvent.getOriginalNetworkId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("EventName = ").append(epgEvent.getEventName()).append("\n");
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test3 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "EitDataUpdate() Pass!");
    }

    public void BtnEitDataDisplayUpdate_OnClick(View view)
    {
        final int btnIndex = 2;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        int type;
        EpgUiDisplay epgUiDisplay;
        List<SimpleChannel> simpleChannelList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE ProgramInfo", btnIndex);
                return;
            }
            else
            {
                // get time and update eitdata
                SimpleChannel simpleChannel = simpleChannelList.get(0);
                List<EPGEvent> epgEventList = EpgEventGetEPGEventList(simpleChannel.getChannelId(), 0, 0, 0);
                if ( epgEventList == null )
                {
                    mStrInput.append("No EpgEvent of ProgramInfoList[0]\n");
                    mStrOutput.append("No EpgEvent of ProgramInfoList[0]\n");
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "No EpgEvent of ProgramInfoList[0]", btnIndex);
                    return;
                }

                int sizeEpgEventList = epgEventList.size();
                long epgStartTime = epgEventList.get(0).getStartTime();
                long epgEndTime = epgEventList.get(sizeEpgEventList/2-1).getEndTime();
                epgUiDisplay.EitDataUpdate(0, epgStartTime, epgEndTime, 0);
                mStrInput.append("EitDataUpdate()\n");
                mStrOutput.append("Time = ").append(epgStartTime).append(" to ").append(epgEndTime).append("\n");

                // update eitdisplaydata
                int sizeBefore = epgUiDisplay.epgDisplayData.size();
                epgUiDisplay.EitDataDisplayUpdate();
                int sizeAfter = epgUiDisplay.epgDisplayData.size();
                mStrInput.append("EitDataDisplayUpdate() : \n");
                mStrInput.append("Expected Size = ").append(sizeEpgEventList/2).append("\n");
                mStrOutput.append("Before update size = ").append(sizeBefore).append("\n");
                mStrOutput.append("After update size = ").append(sizeAfter).append("\n");


                // size should be sizeEpgEventList/2
                if ( sizeAfter != sizeEpgEventList/2 )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "After update size should be " + sizeEpgEventList/2, btnIndex);
                    return;
                }

                // show EventId, Sid, TransportSId, OriNetId, EventName
                for ( int i = 0 ; i < epgUiDisplay.epgDisplayData.size() ; i++ )
                {
                    EPGEvent epgEvent = epgUiDisplay.epgDisplayData.get(i);
                    mStrInput.append("epgDisplayData[ ").append(i).append(" ] : \n");
                    mStrOutput.append("EpgEvent ").append(i).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("EventId = ").append(epgEvent.getEventId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("Sid = ").append(epgEvent.getSid()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("TransportSId = ").append(epgEvent.getTransportStreamId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("OriNetId = ").append(epgEvent.getOriginalNetworkId()).append("\n");
                    mStrInput.append("\n");
                    mStrOutput.append("EventName = ").append(epgEvent.getEventName()).append("\n");
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "EitDataDisplayUpdate() Pass!");
    }

    public void BtnChangeProgram_OnClick(View view)
    {
        final int btnIndex = 3;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();


        EpgUiDisplay epgUiDisplay;
        List<SimpleChannel> simpleChannelList;
        int type;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE ProgramInfo", btnIndex);
                return;
            }
            else if (simpleChannelList.size() < 2)
            {
                mStrInput.append("Not enough ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("Not enough ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "Not enough ALL_TV_TYPE ProgramInfo", btnIndex);
                return;
            }
            else
            {

                // set cur chid and grouptype to ALL_TV_TYPE channel[0]
                GposInfo gposInfo = GposInfoGet();
                gposInfo.setCurChannelId(simpleChannelList.get(0).getChannelId());
                gposInfo.setCurGroupType(type);
                GposInfoUpdate(gposInfo);

                mStrInput.append("ChangeProgram() to : Channel[1]\n");
                mStrOutput.append("ChId =  ").append(simpleChannelList.get(1).getChannelId()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("GroupType =  ").append(type).append("\n");

                // show chId/groupType before change
                gposInfo = GposInfoGet();
                mStrInput.append("Before change program : \n");
                mStrInput.append("ChId = ").append(gposInfo.getCurChannelId()).append("\n");
                mStrInput.append("GroupType = ").append(gposInfo.getCurGroupType()).append("\n");

                // change program to channel[1]
                epgUiDisplay.ChangeProgram(1);

                // show chId/groupType after change
                gposInfo = GposInfoGet();
                mStrOutput.append("After change program : \n");
                mStrOutput.append("ChId = ").append(gposInfo.getCurChannelId()).append("\n");
                mStrOutput.append("GroupType = ").append(gposInfo.getCurGroupType()).append("\n");

                // check if curChId/groupType match channel[1]
                if (gposInfo.getCurChannelId() != simpleChannelList.get(1).getChannelId()
                        || gposInfo.getCurGroupType() != type )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "CurChId/GroupType does not match ALL_TV_TYPE channel[1]", btnIndex);
                    return;
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangeProgram() Pass!");
//        TestPass(view, "Skip Test");
    }

    public void BtnChangeGroup_OnClick(View view)
    {
        final int btnIndex = 4;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        int type;
        EpgUiDisplay epgUiDisplay;
        List<SimpleChannel> simpleChannelList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            boolean changeSuccess = epgUiDisplay.ChangeGroup(ProgramInfo.ALL_RADIO_TYPE);
            mStrInput.append("ChangeGroup()\n");
            mStrOutput.append("Type = ").append(ProgramInfo.ALL_RADIO_TYPE).append("\n");

            simpleChannelList = epgUiDisplay.programInfoList;
            if ( simpleChannelList == null )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "programInfoList has no program, invalid test", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("return = ").append(changeSuccess).append("\n");

            if ( !changeSuccess )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChangeGroup() return false", btnIndex);
                return;
            }

            List<ProgramInfo> allRadioList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);

            for ( int i = 0 ; i < simpleChannelList.size() ; i++ )
            {
                SimpleChannel infoByProgramList = simpleChannelList.get(i);
                ProgramInfo infoByAllRadioList = allRadioList.get(i);
                mStrInput.append("RadioProgramList[0] : \n");
                mStrOutput.append("programInfoList[0] : \n");
                mStrInput.append("ChId = ").append(infoByAllRadioList.getChannelId()).append("\n");
                mStrOutput.append("ChId = ").append(infoByProgramList.getChannelId()).append("\n");
                mStrInput.append("DisplayName = ").append(infoByAllRadioList.getDisplayName()).append("\n");
                mStrOutput.append("ChName = ").append(infoByProgramList.getChannelName()).append("\n");
                mStrInput.append("DisplayNum = ").append(infoByAllRadioList.getDisplayNum()).append("\n");
                mStrOutput.append("ChNum = ").append(infoByProgramList.getChannelNum()).append("\n");
            }

            for ( int i = 0 ; i < simpleChannelList.size() ; i++ )
            {
                SimpleChannel infoByProgramList = simpleChannelList.get(i);
                ProgramInfo infoByAllRadioList = allRadioList.get(i);
                if ( infoByProgramList.getChannelId() != infoByAllRadioList.getChannelId() )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "Program did not change after ChangeGroup()", btnIndex);
                    return;
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : Change to empty\n");
            mStrOutput.append("Output Test2 : Change to empty\n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            changeSuccess = epgUiDisplay.ChangeGroup(ProgramInfo.RADIO_FAV2_TYPE);
            mStrInput.append("ChangeGroup()\n");
            mStrOutput.append("Type = ").append(ProgramInfo.RADIO_FAV2_TYPE).append("\n");

            List<FavInfo> favInfoList = FavInfoGetList(ProgramInfo.RADIO_FAV2_TYPE);
            if ( favInfoList != null )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "RADIO_FAV2_TYPE has program, invalid test", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("return = ").append(changeSuccess).append("\n");

            if ( changeSuccess )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChangeGroup() return true", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test2 -end

        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangeGroup() Pass!");
    }

    /*
    public void BtnGetEndTime_OnClick(View view)
    {
        final int btnIndex = 5;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        int type;
        EpgUiDisplay epgUiDisplay;
        List<SimpleChannel> simpleChannelList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            epgUiDisplay = new EpgUiDisplay(type);
            simpleChannelList = epgUiDisplay.programInfoList;
            mStrInput.append("new EpgUiDisplay()\n");
            mStrOutput.append("Type = ").append(type).append("\n");

            if ( simpleChannelList == null )
            {
                mStrInput.append("No ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE ProgramInfo", btnIndex);
                return;
            }
            else
            {
                // get time and update eitdata
                SimpleChannel simpleChannel = simpleChannelList.get(0);
                List<EPGEvent> epgEventList = EpgEventGetEPGEventList(simpleChannel.getChannelId(), 0, 0);
                if ( epgEventList == null )
                {
                    mStrInput.append("No EpgEvent of ProgramInfoList[0]\n");
                    mStrOutput.append("No EpgEvent of ProgramInfoList[0]\n");
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "No EpgEvent of ProgramInfoList[0]", btnIndex);
                    return;
                }

                int sizeEpgEventList = epgEventList.size();
                long epgStartTime = UtcToSec(epgEventList.get(0).getStartTimeUtcM(), epgEventList.get(0).getStartTimeUtcL());
                long epgEndTime = UtcToSec(epgEventList.get(sizeEpgEventList/2-1).getStartTimeUtcM(), epgEventList.get(sizeEpgEventList/2-1).getStartTimeUtcL())
                        + UtcLToSec(epgEventList.get(sizeEpgEventList/2-1).getDuration());
                epgUiDisplay.EitDataUpdate(0, epgStartTime, epgEndTime);
                mStrInput.append("EitDataUpdate()\n");
                mStrOutput.append("Time = ").append(epgStartTime).append(" to ").append(epgEndTime).append("\n");

                // update eitdisplaydata
                int sizeBefore = epgUiDisplay.epgDisplayData.size();
                epgUiDisplay.EitDataDisplayUpdate();
                int sizeAfter = epgUiDisplay.epgDisplayData.size();
                mStrInput.append("EitDataDisplayUpdate() : \n");
                mStrInput.append("\n");
                mStrOutput.append("Before update size = ").append(sizeBefore).append("\n");
                mStrOutput.append("After update size = ").append(sizeAfter).append("\n");


                int offsetSec;
                TimeZone defaultZone = TimeZone.getDefault();
                GposInfo gposInfo = GposInfoGet();
                if(gposInfo.getAutoRegionTimeOffset() == 1) {
                    Date da = new Date();
                    offsetSec = defaultZone.getOffset(da.getTime())/1000;
                }
                else {
                    offsetSec = (int) gposInfo.getRegionTimeOffset()*60*60;
                }

                // index = 0;
                EPGEvent epgEvent = epgUiDisplay.epgDisplayData.get(0);
                long timeGetByGetEndTime = epgUiDisplay.GetEndTime(epgEvent);
                long timeGetByCalculate = UtcToSec(epgEvent.getStartTimeUtcM(), epgEvent.getStartTimeUtcL())
                        + offsetSec
                        + UtcLToSec(epgEvent.getDuration());

                mStrInput.append("GetEndTime() = ").append(timeGetByGetEndTime).append("\n");
                mStrOutput.append("Calculated time = ").append(timeGetByCalculate).append("\n");

                // time should be same
                if ( timeGetByGetEndTime != timeGetByCalculate )
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "GetEndTime() and calculated time are different", btnIndex);
                    return;
                }
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "GetEndTime() Pass!");
    }
*/

    public void BtnEpgUiDisplayInit_OnClick(View view)
    {
        final int btnIndex = 6;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        int type;
        String beforeInit;
        String afterInit;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            type = ProgramInfo.ALL_TV_TYPE;
            EpgUiDisplay epgUiDisplay = new EpgUiDisplay(type);
            beforeInit = epgUiDisplay.toString();

            //EpgUiDisplayInit(type);
            epgUiDisplay = GetEpgUiDisplay(type);
            BookManager bookManager = GetBookManager();


            afterInit = epgUiDisplay.toString();

            mStrInput.append("Before Init : \n").append(beforeInit).append("\n");
            mStrOutput.append("After Init : \n").append(afterInit).append("\n");

            if ( beforeInit.equals(afterInit) )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "epgUiDisplay did not change after EpgUiDisplayInit()", btnIndex);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrInput.toString(), txvInput);
            ShowResultOnTXV(mStrOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "EpgUiDisplayInit() Pass!");
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTEPGUIDISPLAY_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTEPGUIDISPLAY_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTEPGUIDISPLAY_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTEPGUIDISPLAY_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestEPGUiDisplay");

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

    private void TestPass(View view, String msg)
    {
        Button button = (Button) findViewById(view.getId());

//        SpannableStringBuilder style = new SpannableStringBuilder(msg);
//        style.setSpan(new ForegroundColorSpan(Color.BLACK), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        help.setHelpInfoText( style, msg );

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());

    }

    // show result(string) on textview
    private void ShowResultOnTXV(String result, TextView textView)
    {
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }
}
