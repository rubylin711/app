package com.prime.dtvplayer.TestMiddleware;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.R;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TestTmctlActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityHelpView help;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 8;
    private int mPosition;  // position of testMidMain

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private StringBuilder mStrBuilderOutput;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmctl);
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
        title.setTitleView("TestMiddlewareMain > TMCL");

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


    public void getDtvTimeZone_OnClick(View view)
    {
        final int btnIndex = 0;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            int ret = getDtvTimeZone();
            if(ret != -1)
            {
                TestPass(view, "getDtvTimeZone() Pass !    TimeZone = " + ret + "   secs");
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "getDtvTimeZone() Fail !    time =" + ret, btnIndex);
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

    public void setDtvTimeZone_OnClick(View view)
    {
        final int btnIndex = 1;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            int ret = setDtvTimeZone(-7200);
            if(ret != -1)
            {
                TestPass(view, "setDtvTimeZone() Pass !    Set TimeZone -7200  secs");
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "setDtvTimeZone() Fail !   Set TimeZone -7200  secs", btnIndex);
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

    public void getSettingTDTStatus_OnClick(View view)
    {
        final int btnIndex = 2;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            int ret = getSettingTDTStatus();
            if(ret != -1)
            {
                TestPass(view, "getSettingTDTStatus() Pass !   ret =" + ret);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "getSettingTDTStatus() Fail !  ret =" + ret, btnIndex);
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

    public void setSettingTDTStatus_OnClick(View view)
    {
        final int btnIndex = 3;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            int ret = setSettingTDTStatus(0);
            if(ret != -1)
            {
                TestPass(view, "setSettingTDTStatus() Pass !   Set  0");
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "setSettingTDTStatus() Fail !  ", btnIndex);
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

    public void getLocalTime_OnClick(View view)
    {
        final int btnIndex = 4;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            Date local = getLocalTime();
            if(local != null)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String localTime = sdf.format(local);

                TestPass(view, "getLocalTime() Pass !    " + localTime);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "getLocalTime() Fail ! ", btnIndex);
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

    public void TmSetDateTime_OnClick(View view)
    {
        final int btnIndex = 5;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            String dateString = "2018-02-23 12:30:50";
            Date newDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateString);
            newDate = new Date(2018 - 1900, 2, 23, 12, 30, 50);

            int ret = TmSetDateTime(newDate);
            if(ret != -1)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String Time = sdf.format(newDate);

                TestPass(view, "TmSetDateTime() Pass !    Set  = " + Time);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "TmSetDateTime() Fail !", btnIndex);
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

    public void getDtvDaylight_OnClick_OnClick(View view)
    {
        final int btnIndex = 6;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {

            int summer = getDtvDaylight();
            if(summer != -1)
            {
                TestPass(view, "getDtvDaylight() Pass !    Summer Time  = " + summer);
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "TmSetDateTime() Fail !   Summer Time  = \" + summer", btnIndex);
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

    public void setDtvDaylight_OnClick_OnClick(View view)
    {
        final int btnIndex = 7;
        String result = "";
        ShowResultOnTXV("", txvOutput);

        try
        {
            int ret = setDtvDaylight(0);
            if(ret != -1)
            {
                TestPass(view, "setDtvDaylight() Pass !   setDtvDaylight : 0" );
                ShowResultOnTXV(result, txvOutput);
            }
            else
            {
                GotError(view, "setDtvDaylight() Fail !", btnIndex);
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
