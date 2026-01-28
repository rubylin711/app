package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TestUtcActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 2;  // 2 utc functions

    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    StringBuilder mStrInput;
    StringBuilder mStrOutput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_utc);

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

    /*
    public void BtnUtcMToSec_OnClick(View view)
    {
        final int btnIndex = 0;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        Date now = new Date();
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            Date beginDate = sdf.parse("2017/03/29");
            Date nowDate =  sdf.parse(sdf.format(now));

            long utcM = 0xe1f1;
            int daysAfter = (int) ((nowDate.getTime()-beginDate.getTime()) /1000/60/60/24);
            utcM += daysAfter;

            // set textview text - date and utcm
            mStrInput.append("Date = ").append(sdf.format(now)).append("\n");
            mStrOutput.append("UtcM = ").append(utcM).append(" (0x").append(Long.toHexString(utcM)).append(")").append("\n");

            long secFunc = UtcMToSec(utcM);
            long secNow = nowDate.getTime() / 1000;

            // set textview text - sec got from func and sec got from date.getTime()
            mStrInput.append("UtcMToSec = ").append(secFunc).append("\n");
            mStrOutput.append("Date.getTime/1000 = ").append(secNow).append("\n");

            if ( secFunc != secNow )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "UtcMToSec return wrong time(sec)", btnIndex);
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
        TestPass(view, "UtcMToSec() Pass!");
    }

    public void BtnUtcLToSec_OnClick(View view)
    {
        final int btnIndex = 1;

        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        mStrInput = new StringBuilder();
        mStrOutput = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        try
        {
            // test1 -start
            mStrInput.append("Input Test1 : \n");
            mStrOutput.append("Output Test1 : \n");

            calendar.setTime(now);
            Date hmsDate =  sdf.parse(sdf.format(now));

            String s = String.format(Locale.US,"%02d%02d%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            long utcL = Long.decode("0x" + s);

            // set textview text - date and utcl
            mStrInput.append("Date(HMS) = ").append(sdf.format(now)).append("\n");
            mStrOutput.append("UtcL = ").append(utcL).append(" (0x").append(Long.toHexString(utcL)).append(")").append("\n");

            long secFunc = UtcLToSec(utcL);
            long secNow = hmsDate.getTime() / 1000;

            // set textview text - sec got from func and sec got from date.getTime()
            mStrInput.append("UtcLToSec = ").append(secFunc).append("\n");
            mStrOutput.append("Date.getTime/1000 = ").append(secNow).append("\n");

            if ( secFunc != secNow )
            {
                ShowResultOnTXV(mStrInput.toString(), txvInput);
                ShowResultOnTXV(mStrOutput.toString(), txvOutput);
                GotError(view, "", btnIndex);
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
        TestPass(view, "UtcLToSec() Pass!");
    }
*/
    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTUTC_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTUTC_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTUTC_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTUTC_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestUtc");

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
