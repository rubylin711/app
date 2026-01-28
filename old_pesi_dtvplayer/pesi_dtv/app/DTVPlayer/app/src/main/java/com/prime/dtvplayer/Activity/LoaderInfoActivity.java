package com.prime.dtvplayer.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.prime.dtvplayer.Sysdata.LoaderInfo;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityTitleView;

public class LoaderInfoActivity extends DTVActivity {
    final String TAG = getClass().getSimpleName();

    private ActivityTitleView title;
    private TextView hardware;
    private TextView software;
    private TextView sequenceNum;
    private TextView buildDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader_info);

        InitTitle();

        hardware = (TextView) findViewById(R.id.hwInfoTXV);
        software = (TextView) findViewById(R.id.swInfoTXV);
        sequenceNum = (TextView) findViewById(R.id.sequenceNumInfoTXV);
        buildDate = (TextView) findViewById(R.id.buildtimeInfoTXV);

        LoaderInfo info = GetLoaderInfo();
        if(info!=null) {
            hardware.setText(info.Hardware);
            software.setText(info.Software);
            sequenceNum.setText(info.SequenceNumber);
            buildDate.setText(info.BuildDate);
        }
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.TitleLayout);
        title.setTitleView(getString(R.string.STR_LOADER_INFORMATION_TITLE));
    }
}
