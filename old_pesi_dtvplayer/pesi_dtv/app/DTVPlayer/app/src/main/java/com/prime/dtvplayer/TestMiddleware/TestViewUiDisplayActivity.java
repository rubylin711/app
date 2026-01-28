package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestViewUiDisplayActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 10;  // 10 viewuidisplay functions

    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    StringBuilder mStrInput;
    StringBuilder mStrOutput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_view_ui_display);

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

    public void BtnUpdateEpgPF_OnClick(View view)
    {
        final int btnIndex = 0;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        long chId;
        ViewUiDisplay viewUiDisplay;
        EPGEvent epgEventPresent, epgEventFollow;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            chId = GposInfoGet().getCurChannelId();
            viewUiDisplay = GetViewUiDisplay();
            epgEventPresent = EpgEventGetPresentEvent(chId);
            epgEventFollow = EpgEventGetFollowEvent(chId);

            if ( epgEventPresent == null || epgEventFollow == null )
            {
                mStrInput.append("No EpgEvent of Gpos.curChId\n");
                mStrOutput.append("No EpgEvent of Gpos.curChId\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No EpgEvent of Gpos.curChId, invalid test", btnIndex);
                return;
            }

            viewUiDisplay.UpdateEpgPF();
            mStrInput.append("UpdateEpgPF()\n");
            mStrOutput.append("\n");

            // show EpgPreData, EpgFolData
            if ( viewUiDisplay.EpgPreData == null || viewUiDisplay.EpgFolData == null)
            {
                mStrInput.append("EpgPreData = ").append(viewUiDisplay.EpgPreData).append("\n");
                mStrOutput.append("EpgFolData = ").append(viewUiDisplay.EpgFolData).append("\n");
            }
            else
            {
                // present
                mStrInput.append("EpgPre of viewUiDisplay : \n");
                mStrOutput.append("EpgPre of Gpos.curChId : \n");
                mStrInput.append("EventId = ").append(viewUiDisplay.EpgPreData.getEventId()).append("\n");
                mStrOutput.append("EventId = ").append(epgEventPresent.getEventId()).append("\n");
                mStrInput.append("EventType = ").append(viewUiDisplay.EpgPreData.getEventType()).append("\n");
                mStrOutput.append("EventType = ").append(epgEventPresent.getEventType()).append("\n");
                mStrInput.append("TableId = ").append(viewUiDisplay.EpgPreData.getTableId()).append("\n");
                mStrOutput.append("TableId = ").append(epgEventPresent.getTableId()).append("\n");
                mStrInput.append("EventName = ").append(viewUiDisplay.EpgPreData.getEventName()).append("\n");
                mStrOutput.append("EventName = ").append(epgEventPresent.getEventName()).append("\n");

                mStrInput.append("\n");
                mStrOutput.append("\n");

                // follow
                mStrInput.append("EpgFol of viewUiDisplay : \n");
                mStrOutput.append("EpgFol of Gpos.curChId : \n");
                mStrInput.append("EventId = ").append(viewUiDisplay.EpgFolData.getEventId()).append("\n");
                mStrOutput.append("EventId = ").append(epgEventFollow.getEventId()).append("\n");
                mStrInput.append("EventType = ").append(viewUiDisplay.EpgFolData.getEventType()).append("\n");
                mStrOutput.append("EventType = ").append(epgEventFollow.getEventType()).append("\n");
                mStrInput.append("TableId = ").append(viewUiDisplay.EpgFolData.getTableId()).append("\n");
                mStrOutput.append("TableId = ").append(epgEventFollow.getTableId()).append("\n");
                mStrInput.append("EventName = ").append(viewUiDisplay.EpgFolData.getEventName()).append("\n");
                mStrOutput.append("EventName = ").append(epgEventFollow.getEventName()).append("\n");
            }

            if ( !viewUiDisplay.EpgPreData.ToString().equals(epgEventPresent.ToString())
                    || !viewUiDisplay.EpgFolData.ToString().equals(epgEventFollow.ToString()) )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "EpgPF of viewUiDisplay does not match epgPF of Gpos.curChId", btnIndex);
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
        TestPass(view, "UpdateEpgPF() Pass!");
    }



    public void BtnChangeChannelByDigi_OnClick(View view)
    {
        final int btnIndex = 1;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        GposInfo store = GposInfoGet();

        ViewUiDisplay viewUiDisplay;
        GposInfo gposInfo;
        ProgramInfo programInfo;
        List<ProgramInfo> programInfoList;
        int testDisplayNum;
        boolean changeByDigiSucess;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            viewUiDisplay = GetViewUiDisplay();
            programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);

            if ( programInfoList == null )
            {
                mStrInput.append("No  ALL_TV_TYPE ProgramInfo\n");
                mStrOutput.append("No  ALL_TV_TYPE ProgramInfo\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No  ALL_TV_TYPE ProgramInfo, invalid test", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            // set testDisplayNum to last ch of TV_TYPE
            programInfo = programInfoList.get(programInfoList.size()-1);
            testDisplayNum = programInfo.getDisplayNum();

            // change channel
            changeByDigiSucess = viewUiDisplay.ChangeChannelByDigi(testDisplayNum);
            mStrInput.append("ChangeChannelByDigi(").append(testDisplayNum).append(")\n");
            mStrOutput.append("return = ").append(changeByDigiSucess).append("\n");

            // should return true
            if ( !changeByDigiSucess )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChangeChannelByDigi return false", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            // get cur displayNum
            gposInfo = GposInfoGet();
            programInfo = ProgramInfoGetByChannelId(gposInfo.getCurChannelId());
            mStrInput.append("Target DisplayNum =  : ").append(testDisplayNum).append("\n");
            mStrOutput.append("Cur DisplayNum = ").append(programInfo.getDisplayNum()).append("\n");

            // two displayNum should be the same
            if ( testDisplayNum != programInfo.getDisplayNum() )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "Wrong curDisplayNum after ChangeChannelByDigi", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            viewUiDisplay = GetViewUiDisplay();

            // change channel to impossible
            changeByDigiSucess = viewUiDisplay.ChangeChannelByDigi(Integer.MAX_VALUE);
            mStrInput.append("ChangeChannelByDigi(").append(Integer.MAX_VALUE).append(")\n");
            mStrOutput.append("return = ").append(changeByDigiSucess).append("\n");

            // should return false
            if ( changeByDigiSucess )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChangeChannelByDigi return true when change to Integer.Max", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
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
            GposInfoUpdate(store);
            AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangeChannelUp() Pass!");
        GposInfoUpdate(store);
        AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
    }

    public void BtnChangeProgram_OnClick(View view)
    {
        final int btnIndex = 2;
        GposInfo store = GposInfoGet();

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        ViewUiDisplay viewUiDisplay;
        GposInfo gposInfo;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            // change program
            viewUiDisplay = GetViewUiDisplay();
            viewUiDisplay.ChangeProgram();
            mStrInput.append("ChangeProgram()\n");
            mStrOutput.append("\n");

            // check if ViewHistory and Gpos are not same
            gposInfo = GposInfoGet();
            if (gposInfo.getCurChannelId() != super.ViewHistory.getCurChannel().getChannelId()
                    || gposInfo.getCurGroupType() != super.ViewHistory.getCurGroupType())
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "Gpos and ViewHistory are different", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            // show gpos and ViewHistory
            mStrInput.append("Ch of Gpos : \n");
            mStrOutput.append("Ch of ViewHistory : \n");
            mStrInput.append("ChId = ").append(gposInfo.getCurChannelId()).append("\n");
            mStrOutput.append("ChId = ").append(super.ViewHistory.getCurChannel().getChannelId()).append("\n");
            mStrInput.append("GroupType = ").append(gposInfo.getCurGroupType()).append("\n");
            mStrOutput.append("GroupType = ").append(super.ViewHistory.getCurGroupType()).append("\n");

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
            GposInfoUpdate(store);
            AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangeProgram() Pass!");
        GposInfoUpdate(store);
        AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
    }

    public void BtnChangeBannerInfoUp_OnClick(View view)
    {
        final int btnIndex = 3;
        GposInfo store = GposInfoGet();

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        ViewUiDisplay viewUiDisplay;
        List<ProgramInfo> programInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            viewUiDisplay = GetViewUiDisplay();

            // set viewhistory to tv[0]
            mStrInput.append("Set ViewHistory to ALL_TV[0]\n");
            mStrOutput.append("\n");
            programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            if (programInfoList == null)
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE Program", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }
            else if (programInfoList.size() < 2)
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ALL_TV_TYPE Program less than 2, invalid test", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            AvControlPlayByChannelId(ViewHistory.getPlayId(),programInfoList.get(0).getChannelId(), programInfoList.get(0).getType(),1);

            // ChangeBannerInfoUp
            mStrInput.append("ChangeBannerInfoUp()\n");
            mStrOutput.append("\n");
            viewUiDisplay.ChangeBannerInfoUp();

            // Show ViewHistory.getCurCh and tv[1]
            mStrInput.append("Ch of ViewHistory : \n");
            mStrOutput.append("Ch of ALL_TV_TYPE[1] : \n");
            mStrInput.append("ChId = ").append(ViewHistory.getCurChannel().getChannelId()).append("\n");
            mStrOutput.append("ChId = ").append(programInfoList.get(1).getChannelId()).append("\n");

            // Check if ViewHistory.getCurChannel == tv[1]
            if (ViewHistory.getCurChannel().getChannelId() != programInfoList.get(1).getChannelId())
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChIds are different after ChangeBannerInfoUp()", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            viewUiDisplay = GetViewUiDisplay();

            // set viewhistory to tv[last]
            mStrInput.append("Set ViewHistory to ALL_TV[last]\n");
            mStrOutput.append("\n");
            programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            if (programInfoList == null)
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE Program", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            AvControlPlayByChannelId(ViewHistory.getPlayId(),programInfoList.get(programInfoList.size()-1).getChannelId(), programInfoList.get(programInfoList.size()-1).getType(),1);

            // ChangeBannerInfoUp
            mStrInput.append("ChangeBannerInfoUp()\n");
            mStrOutput.append("\n");
            viewUiDisplay.ChangeBannerInfoUp();

            // Show ViewHistory.getCurCh and tv[0]
            mStrInput.append("Ch of ViewHistory : \n");
            mStrOutput.append("Ch of ALL_TV[0] : \n");
            mStrInput.append("ChId = ").append(ViewHistory.getCurChannel().getChannelId()).append("\n");
            mStrOutput.append("ChId = ").append(programInfoList.get(0).getChannelId()).append("\n");

            // Check if ViewHistory.getCurChannel == tv[0]
            if (ViewHistory.getCurChannel().getChannelId() != programInfoList.get(0).getChannelId())
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChIds are different after ChangeBannerInfoUp()", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
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
            GposInfoUpdate(store);
            AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangeBannerInfoUp() Pass!");
        GposInfoUpdate(store);
        AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
    }

    public void BtnChangeBannerInfoDown_OnClick(View view)
    {
        final int btnIndex = 4;
        GposInfo store = GposInfoGet();

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        ViewUiDisplay viewUiDisplay;
        List<ProgramInfo> programInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            viewUiDisplay = GetViewUiDisplay();

            // set viewhistory to tv[last]
            mStrInput.append("Set ViewHistory to ALL_TV[last]\n");
            mStrOutput.append("\n");
            programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            if (programInfoList == null)
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE Program", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }
            else if (programInfoList.size() < 2)
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ALL_TV_TYPE Program less than 2, invalid test", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            int lastIndex = programInfoList.size()-1;
            AvControlPlayByChannelId(ViewHistory.getPlayId(),programInfoList.get(lastIndex).getChannelId(), programInfoList.get(lastIndex).getType(),1);

            // ChangeBannerInfoDown
            mStrInput.append("ChangeBannerInfoDown()\n");
            mStrOutput.append("\n");
            viewUiDisplay.ChangeBannerInfoDown();

            // Show ViewHistory.getCurCh and tv[last-1]
            mStrInput.append("Ch of ViewHistory : \n");
            mStrOutput.append("Ch of ALL_TV_TYPE[last-1] : \n");
            mStrInput.append("ChId = ").append(ViewHistory.getCurChannel().getChannelId()).append("\n");
            mStrOutput.append("ChId = ").append(programInfoList.get(lastIndex-1).getChannelId()).append("\n");

            // Check if ViewHistory.getCurChannel == tv[last-1]
            if (ViewHistory.getCurChannel().getChannelId() != programInfoList.get(lastIndex-1).getChannelId())
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChIds are different after ChangeBannerInfoDown()", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            viewUiDisplay = GetViewUiDisplay();

            // set viewhistory to tv[0]
            mStrInput.append("Set ViewHistory to ALL_TV[0]\n");
            mStrOutput.append("\n");
            programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            if (programInfoList == null)
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE Program", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            AvControlPlayByChannelId(ViewHistory.getPlayId(),programInfoList.get(0).getChannelId(), programInfoList.get(0).getType(),1);

            // ChangeBannerInfoDown
            mStrInput.append("ChangeBannerInfoDown()\n");
            mStrOutput.append("\n");
            viewUiDisplay.ChangeBannerInfoDown();

            // Show ViewHistory.getCurCh and tv[last]
            mStrInput.append("Ch of ViewHistory : \n");
            mStrOutput.append("Ch of ALL_TV[last] : \n");
            mStrInput.append("ChId = ").append(ViewHistory.getCurChannel().getChannelId()).append("\n");
            mStrOutput.append("ChId = ").append(programInfoList.get(lastIndex).getChannelId()).append("\n");

            // Check if ViewHistory.getCurChannel == tv[last]
            if (ViewHistory.getCurChannel().getChannelId() != programInfoList.get(lastIndex).getChannelId())
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChIds are different after ChangeBannerInfoDown()", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
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
            GposInfoUpdate(store);
            AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangeBannerInfoDown() Pass!");
        GposInfoUpdate(store);
        AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
    }

    public void BtnChangePreProgram_OnClick(View view)
    {
        final int btnIndex = 5;
        GposInfo store = GposInfoGet();

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        ViewUiDisplay viewUiDisplay;
        List<SimpleChannel> simpleChannelList;
        try {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            viewUiDisplay = GetViewUiDisplay();

            simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,0,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null) {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No ALL_TV_TYPE Program", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            // save pre/cur ch/type
            SimpleChannel preCh, curCh;
            int preGroupType, curGroupType;
            preCh = ViewHistory.getPreChannel();
            curCh = ViewHistory.getCurChannel();
            preGroupType = ViewHistory.getPreGroupType();
            curGroupType = ViewHistory.getCurGroupType();

            // show before changePreProgram()
            mStrInput.append("Before ChangePreProgram()\n");
            mStrOutput.append("\n");
            if (preCh == null)
            {
                mStrInput.append("Pre Ch : \n");
                mStrOutput.append("Cur Ch : \n");
                mStrInput.append("Ch = ").append((Object) null).append("\n");
                mStrOutput.append("ChId = ").append(curCh.getChannelId()).append("\n");
                mStrInput.append("GroupType = ").append(preGroupType).append("\n\n");
                mStrOutput.append("GroupType = ").append(curGroupType).append("\n\n");
            }
            else
            {
                mStrInput.append("Pre Ch : \n");
                mStrOutput.append("Cur Ch : \n");
                mStrInput.append("ChId = ").append(preCh.getChannelId()).append("\n");
                mStrOutput.append("ChId = ").append(ViewHistory.getCurChannel().getChannelId()).append("\n");
                mStrInput.append("GroupType = ").append(preGroupType).append("\n\n");
                mStrOutput.append("GroupType = ").append(curGroupType).append("\n\n");
            }



            // ChangePreProgram()
            mStrInput.append("ChangePreProgram()\n");
            mStrOutput.append("\n");
            viewUiDisplay.ChangePreProgram();

            // show pre and cur ch after change
            mStrInput.append("After ChangePreProgram()\n");
            mStrOutput.append("\n");
            if (ViewHistory.getPreChannel() == null)
            {
                mStrInput.append("Pre Ch : \n");
                mStrOutput.append("Cur Ch : \n");
                mStrInput.append("Ch = ").append((Object) null).append("\n");
                mStrOutput.append("ChId = ").append(ViewHistory.getCurChannel().getChannelId()).append("\n");
                mStrInput.append("GroupType = ").append(ViewHistory.getPreGroupType()).append("\n\n");
                mStrOutput.append("GroupType = ").append(ViewHistory.getCurGroupType()).append("\n\n");
            }
            else
            {
                mStrInput.append("Pre Ch : \n");
                mStrOutput.append("Cur Ch : \n");
                mStrInput.append("ChId = ").append(ViewHistory.getPreChannel().getChannelId()).append("\n");
                mStrOutput.append("ChId = ").append(ViewHistory.getCurChannel().getChannelId()).append("\n");
                mStrInput.append("GroupType = ").append(ViewHistory.getPreGroupType()).append("\n\n");
                mStrOutput.append("GroupType = ").append(ViewHistory.getCurGroupType()).append("\n\n");
            }

            // check
            if (preCh == null)  // if preCh == null, curCh of history should be the same as curCh, preCh of history should be preCh
            {
                if (ViewHistory.getCurChannel().getChannelId() != curCh.getChannelId()
                        || ViewHistory.getCurGroupType() != curGroupType
                        || ViewHistory.getPreChannel() != null
                        || ViewHistory.getPreGroupType() != preGroupType)
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "Ch is wrong  after ChangePreProgram()", btnIndex);
                    GposInfoUpdate(store);
                    AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                    return;
                }
            }
            else    // else, curCh of history should be the same as preCh, preCh of history should be curCh
            {
                if (ViewHistory.getCurChannel().getChannelId() != preCh.getChannelId()
                        || ViewHistory.getCurGroupType() != preGroupType
                        || ViewHistory.getPreChannel().getChannelId() != curCh.getChannelId()
                        || ViewHistory.getPreGroupType() != curGroupType)
                {
                    ShowResultOnTXV(mStrInput.toString(), txvInput);
                    ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                    GotError(view, "Ch is wrong  after ChangePreProgram()", btnIndex);
                    GposInfoUpdate(store);
                    AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
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
            GposInfoUpdate(store);
            AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangePreProgram() Pass!");
        GposInfoUpdate(store);
        AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
    }

    public void BtnChangeGroup_OnClick(View view)
    {
        final int btnIndex = 6;
        GposInfo store = GposInfoGet();

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        ViewUiDisplay viewUiDisplay;
        GposInfo gposInfo;
        boolean changeSuccess;
        List<ProgramInfo> programInfoList;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            viewUiDisplay = GetViewUiDisplay();

            gposInfo = GposInfoGet();
            //Scoty 20180810 change save Tv/Radio channel by channelId -s
            if ( gposInfo.getCurGroupType() == ProgramInfo.ALL_TV_TYPE )
            {
                long TvRadioChannelId = 0;
                List<SimpleChannel> channelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_RADIO_TYPE,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                if(ViewHistory.getTvChannelId() != 0)
                    TvRadioChannelId = ViewHistory.getRadioChannelId();
                else
                    TvRadioChannelId = channelList.get(0).getChannelId();
                changeSuccess = viewUiDisplay.ChangeGroup(channelList,ProgramInfo.ALL_RADIO_TYPE,TvRadioChannelId);
                mStrInput.append("ChangeGroup()\n");
                mStrOutput.append("Type = ").append(ProgramInfo.ALL_RADIO_TYPE).append("\n");

                programInfoList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
            }
            else
            {
                long TvRadioChannelId = 0;
                List<SimpleChannel> channelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_TV_TYPE,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                if(ViewHistory.getTvChannelId() != -1)
                    TvRadioChannelId = ViewHistory.getRadioChannelId();
                else
                    TvRadioChannelId = channelList.get(0).getChannelId();
                changeSuccess = viewUiDisplay.ChangeGroup(channelList,ProgramInfo.ALL_TV_TYPE,TvRadioChannelId);
                mStrInput.append("ChangeGroup()\n");
                mStrOutput.append("Type = ").append(ProgramInfo.ALL_TV_TYPE).append("\n");

                programInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            }
            //Scoty 20180810 change save Tv/Radio channel by channelId -e
            mStrInput.append("\n");
            mStrOutput.append("return = ").append(changeSuccess).append("\n");

            if ( (programInfoList == null && changeSuccess))
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChangeGroup return true when there is no program in group", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }
            else if ( (programInfoList != null && !changeSuccess) )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChangeGroup return false when program in group is not empty", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
                return;
            }

            mStrInput.append("\n");
            mStrOutput.append("\n");
            // test1 -end

            // test2 -start
            mStrInput.append("Input Test2 : \n");
            mStrOutput.append("Output Test2 : \n");

            viewUiDisplay = GetViewUiDisplay();

            List<SimpleChannel> channelList = ProgramInfoGetPlaySimpleChannelList(Integer.MAX_VALUE,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            changeSuccess = viewUiDisplay.ChangeGroup(channelList,Integer.MAX_VALUE,0);
            mStrInput.append("ChangeGroup()\n");
            mStrOutput.append("Type = ").append(Integer.MAX_VALUE).append("\n");

            mStrInput.append("\n");
            mStrOutput.append("return = ").append(changeSuccess).append("\n");

            if (changeSuccess)
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "ChangeGroup return true when change to impossible group", btnIndex);
                GposInfoUpdate(store);
                AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
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
            GposInfoUpdate(store);
            AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
            return;
        }

        ShowResultOnTXV(mStrInput.toString(), txvInput);
        ShowResultOnTXV(mStrOutput.toString(), txvOutput);
        TestPass(view, "ChangeGroup() Pass!");
        GposInfoUpdate(store);
        AvControlPlayByChannelId(ViewHistory.getPlayId(),store.getCurChannelId(), store.getCurGroupType(),1);
    }

    /*
    public void BtnGetEndTime_OnClick(View view)
    {
        final int btnIndex = 7;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        ViewUiDisplay viewUiDisplay;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            viewUiDisplay = GetViewUiDisplay();

            EPGEvent epgEvent = EpgEventGetPresentEvent(GposInfoGet().getCurChannelId());
            if ( epgEvent == null )
            {
                mStrInput.append("No Present Epg of Gpos.curChId\n");
                mStrOutput.append("No Present Epg of Gpos.curChId\n");
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "No Present Epg of Gpos.curChId\n", btnIndex);
                return;
            }
            else
            {
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

                mStrInput.append("Test Epg : \n");
                mStrOutput.append("(long)Start Time UtcM = ").append(epgEvent.getStartTimeUtcM()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("(long)Start Time UtcL = ").append(epgEvent.getStartTimeUtcL()).append("\n");
                mStrInput.append("\n");
                mStrOutput.append("(long)Duration = ").append(epgEvent.getDuration()).append("\n");

                long timeGetByGetEndTime = viewUiDisplay.GetEndTime(epgEvent);
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

    public void BtnCheckNewMail_OnClick(View view)
    {
        final int btnIndex = 8;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();


        try
        {

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
//        TestPass(view, "CheckNewMail() Pass!");
        TestPass(view, "SkipTest");
    }

    public void BtnViewUiDisplayInit_OnClick(View view)
    {
        final int btnIndex = 9;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        String beforeInit;
        String afterInit;
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            ViewUiDisplay viewUiDisplay = GetViewUiDisplay();
            beforeInit = viewUiDisplay.toString();

            //ViewUiDisplayInit();
            viewUiDisplay = GetViewUiDisplay();
            afterInit = viewUiDisplay.toString();

            mStrInput.append("Before Init : \n").append(beforeInit).append("\n");
            mStrOutput.append("After Init : \n").append(afterInit).append("\n");

            if ( beforeInit.equals(afterInit) )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "viewUiDisplay did not change after ViewUiDisplayInit()", btnIndex);
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
        TestPass(view, "ViewUiDisplayInit() Pass!");
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTVIEWUIDISPLAY_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTVIEWUIDISPLAY_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTVIEWUIDISPLAY_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTVIEWUIDISPLAY_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestViewUiDisplay");

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
