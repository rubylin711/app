package com.prime.dtvplayer.TestMiddleware;


import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dolphin.dtv.EnTableType;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.DefaultChannel;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.Sysdata.GposInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TestGposActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 4;    // 2 Gpos functions
    private int mPosition;  // position of testMidMain

    private List<String> mGposElements;

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private StringBuilder mStrBuilderOutput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_gpos);

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

    // button GposInfoGet onclick
    public void BtnGposInfoGet_OnClick(View view)
    {
        GposInfo store = GposInfoGet();
        //Toast.makeText(this, "Get", Toast.LENGTH_SHORT).show();
        final int btnIndex = 0;

        mStrBuilderOutput = new StringBuilder();
        ShowResultOnTXV("", txvOutput);

        ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);

        try
        {
            // test1 -start
            mStrBuilderOutput.append("Output Test1 : \n");

            GposInfo gposInfo = GposInfoGet();
            AppendGpos2TXV(gposInfo);

            mStrBuilderOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
        TestPass(view, "GposInfoGet() Pass!", store);
    }

    // button GposInfoSave onclick
    public void BtnGposInfoSave_OnClick(View view)
    {
        GposInfo store = GposInfoGet();
        //Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
        final int btnIndex = 1;

        mStrBuilderOutput = new StringBuilder();
        ShowResultOnTXV("", txvOutput);

        try
        {
            // test1 -start
            mStrBuilderOutput.append("Test1 : \n");

            GposInfo gposInfo = GposInfoGet();
            mStrBuilderOutput.append("Pre : \n");
            AppendGpos2TXV(gposInfo);

            gposInfo.setDBVersion("1234");
            gposInfo.setCurChannelId(1);
            gposInfo.setCurGroupType(2);
            gposInfo.setPasswordValue(3);
            gposInfo.setParentalRate(4);
            gposInfo.setParentalLockOnOff(5);
            gposInfo.setInstallLockOnOff(6);
            gposInfo.setBoxPowerStatus(7);
            gposInfo.setStartOnChannelId(8);
            gposInfo.setStartOnChType(9);
            gposInfo.setVolume(10);
            gposInfo.setAudioTrackMode(11);
            gposInfo.setAutoRegionTimeOffset(12);
            gposInfo.setRegionTimeOffset((float) 13);
            gposInfo.setRegionSummerTime(14);
            gposInfo.setLnbPower(15);
            gposInfo.setScreen16x9(16);
            gposInfo.setConversion(17);
            gposInfo.setResolution(18);
            gposInfo.setOSDLanguage("ger");
            gposInfo.setSearchProgramType(19);
            gposInfo.setSearchMode(20);
            gposInfo.setAudioLanguageSelection(0,"eng");
            gposInfo.setAudioLanguageSelection(1,"ger");
            gposInfo.setSubtitleLanguageSelection(0,"eng");
            gposInfo.setSubtitleLanguageSelection(1,"ger");
            gposInfo.setSortByLcn(21);
            gposInfo.setOSDTransparency(22);
            gposInfo.setBannerTimeout(23);
            gposInfo.setHardHearing(24);
            gposInfo.setAutoStandbyTime(25);
            gposInfo.setDolbyMode(26);
            gposInfo.setHDCPOnOff(27);
            //gposInfo.setDeepSleepMode(28); // connie 20181017 modify for update fail because there is no this member in service
            gposInfo.setSubtitleOnOff(29);
            gposInfo.setAvStopMode(30);

            GposInfoUpdate(gposInfo);

            GposInfo gposInfoCur = GposInfoGet();
            mStrBuilderOutput.append("Cur \n");
            AppendGpos2TXV(gposInfoCur);

            mStrBuilderOutput.append("\n");

            if (!gposInfo.ToString().equals(gposInfoCur.ToString()))
            {
                ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
                String msg = "GposInfoSave Fail";
                GotError(view, msg, btnIndex, store);
                return;
            }

            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
        TestPass(view, "GposInfoUpdate() Pass!", store);

    }

    public void getDefaultOpenChannel_OnClick(View view)
    {
        final int btnIndex = 2;

        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            DefaultChannel channel = getDefaultOpenChannel();
            if(channel != null)
                result = "getDefaultOpenChannel() :\n" + "Channel ID =" + channel.getChanneId() + "\nGroupType = " + channel.getGroupType();
            else
                result = "DefaultChannel = NULL !!!!" ;

            ShowResultOnTXV(result, txvOutput);
            TestPass(view, "getDefaultOpenChannel() Pass!", null);
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, null);
            return;
        }
    }

    public void setDefaultOpenChannel_OnClick(View view)
    {
        final int btnIndex = 3;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            List<ProgramInfo> pList = new ArrayList<>();
            pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            if(pList == null || pList.size() == 0)
            {
                result = "setDefaultOpenChannel  Fail !!!! No Channel Now..." ;
                ShowResultOnTXV(result, txvOutput);
                return;
            }
            int toatal = pList.size();
            ProgramInfo setCh = pList.get(toatal-1);
            int ret = setDefaultOpenChannel(setCh.getChannelId(), ProgramInfo.ALL_TV_TYPE);
            if(ret != -1)
            {
                result = "setDefaultOpenChannel() : Channel ID = " + setCh.getChannelId() + "     Channel Name =" + setCh.getDisplayName();
                TestPass(view, "setDefaultOpenChannel() Pass !", null);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                result = "setDefaultOpenChannel  Fail !!!!" ;
                TestPass(view, "setDefaultOpenChannel() Fail !", null);
                ShowResultOnTXV(result, txvOutput);
            }
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, null);
            return;
        }
    }

    public void SaveTable_OnClick(View view)
    {
        final int btnIndex = 4;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {

            GposInfo gposInfo = GposInfoGet();

            Log.d(TAG, "SaveTable_OnClick:  Password = " + gposInfo.getPasswordValue());
            Log.d(TAG, "SaveTable_OnClick:  ParentalRate = " + gposInfo.getParentalRate());
            Log.d(TAG, "SaveTable_OnClick:  ParentalLockOnOff = " + gposInfo.getParentalLockOnOff());

            gposInfo.setPasswordValue(3);
            gposInfo.setParentalRate(4);
            gposInfo.setParentalLockOnOff(5);
            GposInfoUpdate(gposInfo);

            int ret = SaveTable(EnTableType.ALL);
            Log.d(TAG, "SaveTable_OnClick:  ret = " + ret);
            if(ret != -1)
            {
                TestPass(view, "SaveTable() Pass !", null);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                TestPass(view, "SaveTable() Fail !", null);
                ShowResultOnTXV(result, txvOutput);
            }
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, null);
            return;
        }
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTGPOS_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTGPOS_LAYOUT_HELP);
        txvOutput = (TextView) findViewById(R.id.ID_TESTGPOS_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestGpos");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        // init gpos elements, for error message
        mGposElements = new ArrayList<>();
        mGposElements.add("DBVersion");
        mGposElements.add("CurChannelId");
        mGposElements.add("CurGroupType");
        mGposElements.add("PasswordValue");
        mGposElements.add("ParentalRate");
        mGposElements.add("ParentalLockOnOff");
        mGposElements.add("InstallLockOnOff");
        mGposElements.add("BoxPowerStatus");
        mGposElements.add("StartOnChannelId");
        mGposElements.add("StartOnChType");
        mGposElements.add("Volume");
        mGposElements.add("AudioTrackMode");
        mGposElements.add("AutoRegionTimeOffset");
        mGposElements.add("RegionTimeOffset");
        mGposElements.add("RegionSummerTime");
        mGposElements.add("LnbPower");
        mGposElements.add("Screen16x9");
        mGposElements.add("Conversion");
        mGposElements.add("Resolution");
        mGposElements.add("OSDLanguage");
        mGposElements.add("SearchProgramType");
        mGposElements.add("SearchMode");
        mGposElements.add("AudioLanguageSelection[0]");
        mGposElements.add("AudioLanguageSelection[1]");
        mGposElements.add("SubtitleLanguageSelection[0]");
        mGposElements.add("SubtitleLanguageSelection[1]");
        mGposElements.add("SortByLcn");
        mGposElements.add("OSDTransparency");
        mGposElements.add("BannerTimeout");
        mGposElements.add("HardHearing");
        mGposElements.add("AutoStandbyTime");
        mGposElements.add("DolbyMode");
        mGposElements.add("HDCPOnOff");
        mGposElements.add("DeepSleepMode");
        mGposElements.add("SubtitleOnOff");
        mGposElements.add("AvStopMode");
    }

    private void GotError(View view, String errorMsg, int btnIndex, GposInfo store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());

//        GposInfoSave(store);
    }

    private void TestPass(View view, String msg, GposInfo store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());

//        GposInfoSave(store);
    }

    // show result(string) on textview
    private void ShowResultOnTXV(String result, TextView textView)
    {
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }

    private void AppendGpos2TXV(GposInfo gposInfo)
    {
        // set textview text
        mStrBuilderOutput.append(mGposElements.get(0)).append(" = ").append(gposInfo.getDBVersion()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(1)).append(" = ").append(gposInfo.getCurChannelId()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(2)).append(" = ").append(gposInfo.getCurGroupType()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(3)).append(" = ").append(gposInfo.getPasswordValue()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(4)).append(" = ").append(gposInfo.getParentalRate()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(5)).append(" = ").append(gposInfo.getParentalLockOnOff()).append("\n");

        mStrBuilderOutput.append(mGposElements.get(6)).append(" = ").append(gposInfo.getInstallLockOnOff()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(7)).append(" = ").append(gposInfo.getBoxPowerStatus()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(8)).append(" = ").append(gposInfo.getStartOnChannelId()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(9)).append(" = ").append(gposInfo.getStartOnChType()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(10)).append(" = ").append(gposInfo.getVolume()).append("\n");

        mStrBuilderOutput.append(mGposElements.get(11)).append(" = ").append(gposInfo.getAudioTrackMode()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(12)).append(" = ").append(gposInfo.getAutoRegionTimeOffset()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(13)).append(" = ").append(gposInfo.getRegionTimeOffset()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(14)).append(" = ").append(gposInfo.getRegionSummerTime()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(15)).append(" = ").append(gposInfo.getLnbPower()).append("\n");

        mStrBuilderOutput.append(mGposElements.get(16)).append(" = ").append(gposInfo.getScreen16x9()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(17)).append(" = ").append(gposInfo.getConversion()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(18)).append(" = ").append(gposInfo.getResolution()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(19)).append(" = ").append(gposInfo.getOSDLanguage()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(20)).append(" = ").append(gposInfo.getSearchProgramType()).append("\n");

        mStrBuilderOutput.append(mGposElements.get(21)).append(" = ").append(gposInfo.getSearchMode()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(22)).append(" = ").append(gposInfo.getAudioLanguageSelection(0)).append("\n");
        mStrBuilderOutput.append(mGposElements.get(23)).append(" = ").append(gposInfo.getAudioLanguageSelection(1)).append("\n");
        mStrBuilderOutput.append(mGposElements.get(24)).append(" = ").append(gposInfo.getSubtitleLanguageSelection(0)).append("\n");
        mStrBuilderOutput.append(mGposElements.get(25)).append(" = ").append(gposInfo.getSubtitleLanguageSelection(1)).append("\n");

        mStrBuilderOutput.append(mGposElements.get(26)).append(" = ").append(gposInfo.getSortByLcn()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(27)).append(" = ").append(gposInfo.getOSDTransparency()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(28)).append(" = ").append(gposInfo.getBannerTimeout()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(29)).append(" = ").append(gposInfo.getHardHearing()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(30)).append(" = ").append(gposInfo.getAutoStandbyTime()).append("\n");

        mStrBuilderOutput.append(mGposElements.get(31)).append(" = ").append(gposInfo.getDolbyMode()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(32)).append(" = ").append(gposInfo.getHDCPOnOff()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(33)).append(" = ").append(gposInfo.getDeepSleepMode()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(34)).append(" = ").append(gposInfo.getSubtitleOnOff()).append("\n");
        mStrBuilderOutput.append(mGposElements.get(35)).append(" = ").append(gposInfo.getAvStopMode()).append("\n");

        mStrBuilderOutput.append("\n");
    }
}
