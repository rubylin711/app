package com.prime.dtvplayer.TestMiddleware;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dolphin.dtv.EnNetworkType;
import com.prime.dtvplayer.R;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Sysdata.LoaderInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class TestCommonActivity extends DTVActivity {

    private final String TAG = getClass().getSimpleName();
    private ActivityHelpView help;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 4;
    private int mPosition;  // position of testMidMain

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private StringBuilder mStrBuilderOutput;
    private int TunerTpe = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_common);

        Init();

//        public static final int DVBT = EnNetworkType.TERRESTRIAL.getValue();
//        public static final int DVBS = EnNetworkType.SATELLITE.getValue();
//        public static final int DVBC = EnNetworkType.CABLE.getValue();
//        public static final int ISDBT = EnNetworkType.ISDB_TER.getValue(); // Johnny add for ISDBT channel search 20180103
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

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_LAYOUT_HELP);
        txvOutput = (TextView) findViewById(R.id.ID_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > Common");

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

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());
    }

    private void ShowResultOnTXV(String result, TextView textView)
    {
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }


    static int standbyTest = 0;
    public void SetStandbyOnOff_OnClick(View view)
    {
        final int btnIndex = 0;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            if(standbyTest == 0)
                standbyTest = 1;
            else
                standbyTest = 0;
            int ret = SetStandbyOnOff(standbyTest);
            if(ret != -1)
            {
                TestPass(view, "SetStandbyOnOff() Pass !  Set " + standbyTest);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "SetStandbyOnOff() Fail !   Set " + standbyTest, btnIndex);
                ShowResultOnTXV(result, txvOutput);
            }
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }
    }

    public void GetPesiServiceVersion_OnClick(View view)
    {
        final int btnIndex = 1;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            if(standbyTest == 0)
                standbyTest = 1;
            else
                standbyTest = 0;
            String version = GetPesiServiceVersion();
            if(version != null)
            {
                TestPass(view, "GetPesiServiceVersion() Pass !  Version : " + version);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "GetPesiServiceVersion() Fail !", btnIndex);
                ShowResultOnTXV(result, txvOutput);
            }
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }
    }

    public void TestChangeTuner_OnClick(View view)
    {
        final int btnIndex = 2;
        String result = "";
        ShowResultOnTXV("", txvOutput);
        try {
            int ret = 0;
            if(TunerTpe == EnNetworkType.CABLE.getValue())
                ret = TestChangeTuner(TpInfo.DVBS);
            else
                ret = TestChangeTuner(TpInfo.DVBC);

            if (ret != -1) {
                if(TunerTpe == EnNetworkType.CABLE.getValue())
                    TestPass(view, "TestChangeTuner() Pass ! Current Tuner Type = " + TunerTpe + " Change Tuner DVBS   ret : " + ret);
                else
                    TestPass(view, "TestChangeTuner() Pass !  Current Tuner Type = " + TunerTpe + " Change Tuner DVBC   ret : " + ret);
                ShowResultOnTXV(result, txvOutput);
            }
            else {
                GotError(view, "TestChangeTuner() Fail ! Change Tuner DVBS  ret :" + ret, btnIndex);
                ShowResultOnTXV(result, txvOutput);
            }
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }


    }

    public void GetLoaderInfo_OnClick(View view)
    {
        final int btnIndex = 3;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try {
            LoaderInfo info = GetLoaderInfo();
            if (info != null) {
                String loader = "Hardware = " + info.Hardware + "    Software = " + info.Software + "   SequenceNumber :" + info.SequenceNumber + "   BuildDate = " + info.BuildDate;
                TestPass(view, "GetPesiServiceVersion() Pass !  loader info : " + loader);
                ShowResultOnTXV(result, txvOutput);
            }
            else {
                GotError(view, "GetPesiServiceVersion() Fail !", btnIndex);
                ShowResultOnTXV(result, txvOutput);
            }
        }
        catch (Exception e)
        {
            ShowResultOnTXV(mStrBuilderOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
            return;
        }
    }

}
