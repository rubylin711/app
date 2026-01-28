package com.prime.dtvplayer.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityTitleView;

public class IRDInfoActivity extends DTVActivity {

    final String TAG = getClass().getSimpleName();
    private ActivityTitleView title;
    private TextView irdSwInfo;
    private TextView buidlDate;
    private TextView chipID;
    private TextView snInfo;
    private TextView caVersion;
    private TextView scNumber;
    private TextView pairingStatus;
    private TextView purse;
    private TextView groupM;
    private TextView loaction;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.irdinfo);

        irdSwInfo = (TextView) findViewById(R.id.irdSwInfoTXV);
        buidlDate = (TextView) findViewById(R.id.buidlDateInfoTXV);
        chipID = (TextView) findViewById(R.id.chipIDInfoTXV);
        snInfo = (TextView) findViewById(R.id.snInfoTXV);
        caVersion = (TextView) findViewById(R.id.caVersionInfoTXV);
        scNumber = (TextView) findViewById(R.id.scNumberInfoTXV);
        pairingStatus = (TextView) findViewById(R.id.pairingStatusInfoTXV);
        //purse = (TextView) findViewById(R.id.groupMTXV);
        groupM = (TextView) findViewById(R.id.groupMInfoTXV); // connie 20181116 for  Revisions-20170526 report
        loaction = (TextView) findViewById(R.id.loactionInfoTXV);

        InitTitle();
        showInfo();
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.TitleLayout);
        title.setTitleView(getString(R.string.STR_IRD_INFORMATION_TITLE));
    }

    private void showInfo() {
        int pair= 0;
        irdSwInfo.setText(GetApkSwVersion());
        buidlDate.setText(GetLibDate());
        chipID.setText(GetChipID());
        snInfo.setText(GetSN());
        caVersion.setText(GetCaVersion());
        scNumber.setText(GetSCNumber());

        pair = GetPairingStatus();
        if(pair == 0)
            pairingStatus.setText(getString(R.string.STR_NO));
        else
            pairingStatus.setText(getString(R.string.STR_YES));

        //purse.setText(GetPurse());
        groupM.setText(Integer.toString(GetGroupM())); // connie 20181116 for  Revisions-20170526 report
        loaction.setText("");

    }
}
