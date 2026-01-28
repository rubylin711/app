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

public class TestSubtitleActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 4;    // 4 functions
    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_subtitle);

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
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTSUBTITLE_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTSUBTITLE_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTSUBTITLE_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTSUBTITLE_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestSubtitle");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);
    }

    public void BtnSubtitleStart_OnClick(View view)
    {

    }

    public void BtnSubtitleStop_OnClick(View view)
    {

    }

    public void BtnSubtitleShow_OnClick(View view)
    {

    }

    public void BtnSubtitleHide_OnClick(View view)
    {

    }
}
