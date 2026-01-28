package com.prime.dtvplayer.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.CaStatus;
import com.prime.dtvplayer.View.ActivityTitleView;

public class CAInfoActivity extends DTVActivity {
    final String TAG = getClass().getSimpleName();
    private ActivityTitleView title;
    private TextView caStatus;
    private TextView auth;
    private TextView deauth;
    private TextView ecm;
    private TextView emm;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cainfo);

        caStatus = (TextView) findViewById(R.id.caStatusInfoTXV);
        auth = (TextView) findViewById(R.id.authInfoTXV);
        deauth = (TextView) findViewById(R.id.deauthInfoTXV);
        emm = (TextView) findViewById(R.id.emmInfoTXV);
        ecm = (TextView) findViewById(R.id.ecmInfoTXV);
        InitTitle();
        showInfo();
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.TitleLayout);
        title.setTitleView(getString(R.string.STR_CA_INFORMATION_TITLE));
    }

    private void showInfo()
    {
        CaStatus info = GetCAStatusInfo();
        if(info != null) {
            caStatus.setText(info.CA_status);
            auth.setText(info.Auth);
            deauth.setText(info.Deauth);
        }

        emm.setText(Integer.toString(GetEMMcount()));
        ecm.setText(Integer.toString(GetECMcount()));
    }

}
