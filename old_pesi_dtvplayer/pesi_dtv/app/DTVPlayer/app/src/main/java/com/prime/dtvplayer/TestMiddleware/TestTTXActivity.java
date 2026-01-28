package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestTTXActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 7;    // 7 functions
    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ttx);

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
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTTTX_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTTTX_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTTTX_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTTTX_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestTTX");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);
    }

    public void BtnTTXShow_OnClick(View view)
    {

    }

    public void BtnTTXHide_OnClick(View view)
    {

    }

    public void BtnTTXGoNextPage_OnClick(View view)
    {

    }

    public void BtnTTXGoPrePage_OnClick(View view)
    {

    }

    public void BtnTTXGoPage_OnClick(View view)
    {

    }

    public void BtnTTXGoHome_OnClick(View view)
    {

    }

    public void BtnTTXGoColorLink_OnClick(View view)
    {

    }
}
