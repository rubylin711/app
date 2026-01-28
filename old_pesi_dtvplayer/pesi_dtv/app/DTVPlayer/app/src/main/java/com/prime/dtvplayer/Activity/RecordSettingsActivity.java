package com.prime.dtvplayer.Activity;


import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;

import java.util.ArrayList;
import java.util.List;

public class RecordSettingsActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityTitleView mTitle;
    private ActivityHelpView mHelp;
    SelectBoxView mSelTimeShiftDuration;
    SelectBoxView mSelRecordIconOnOff;

    GposInfo mGpos;
    List<Integer> mTimeShiftDurationList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_settings);

        InitTitleHelp();
        InitSelectBox();
        InitTimeShiftDurationList();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        mGpos = GposInfoGet();

        // set mSelTimeShiftInterval
        int duration = mGpos.getTimeshiftDuration();
        int index = GetTimeShiftDurationIndex(duration);
        mSelTimeShiftDuration.SetSelectedItemIndex(index);

        // set mSelRecordIconOnOff
        int recordIconOnOff = mGpos.getRecordIconOnOff();    // Johnny 20180730 set gpos recordIcon
        mSelRecordIconOnOff.SetSelectedItemIndex(recordIconOnOff);    // Johnny 20180730 set gpos recordIcon
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();

        // set gpos.timeShiftDuration
        int index = mSelTimeShiftDuration.GetSelectedItemIndex();
        int duration = GetTimeShiftDuration(index);
        mGpos.setTimeshiftDuration(duration);

        // set gpos.recordIconInOff
        int recordIconOnOff = mSelRecordIconOnOff.GetSelectedItemIndex();    // Johnny 20180730 set gpos recordIcon
        mGpos.setRecordIconOnOff(recordIconOnOff);  // Johnny 20180730 set gpos recordIcon

        GposInfoUpdate(mGpos);
    }

    private void InitTitleHelp() {
        Log.d(TAG, "InitTitleHelp: ");
        mTitle = (ActivityTitleView) findViewById(R.id.recordSettingsTitleLayout);
        mHelp = (ActivityHelpView) findViewById(R.id.recordSettingsHelpLayout);

        mTitle.setTitleView(getString(R.string.STR_RECORD_SETTINGS_TITLE));
        mHelp.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
        mHelp.resetHelp(1,0,null);
        mHelp.resetHelp(2,0,null);
        mHelp.resetHelp(3,0,null);
        mHelp.resetHelp(4,0,null);
    }

    private void InitSelectBox() {
        Log.d(TAG, "InitSelectBox()");
        Resources res = getResources();

        Spinner spinTimeShiftDuration = (Spinner) findViewById(R.id.spinnerTimeShiftInterval);
        Spinner spinRecordIcon = (Spinner) findViewById(R.id.spinnerRecordIcon);


        mSelTimeShiftDuration = new SelectBoxView(this,
                spinTimeShiftDuration, res.getStringArray(R.array.STR_ARRAY_TIMESHIFT_INTERVAL));
        mSelRecordIconOnOff = new SelectBoxView(this,
                spinRecordIcon, res.getStringArray(R.array.STR_ARRAY_RECORD_ICON));
    }

    private void InitTimeShiftDurationList()
    {
        mTimeShiftDurationList = new ArrayList<>();

        // add duration according to STR_ARRAY_TIMESHIFT_INTERVAL
        mTimeShiftDurationList.add(15*60);  // 15min
        mTimeShiftDurationList.add(30*60);  // 30min
        mTimeShiftDurationList.add(60*60);  // 1h
        mTimeShiftDurationList.add(90*60);  // 1h 30min
        mTimeShiftDurationList.add(120*60); // 2h
    }

    private int GetTimeShiftDurationIndex(int duration)
    {
        for (int i = 0 ; i < mTimeShiftDurationList.size() ; i++)
        {
            if (mTimeShiftDurationList.get(i) == duration)
            {
                return i;
            }
        }

        return 0;
    }

    private int GetTimeShiftDuration(int index)
    {
        return mTimeShiftDurationList.get(index);
    }
}
