package com.prime.dtvplayer.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityTitleView;

public class CAMenuActivity extends DTVActivity {
    final String TAG = getClass().getSimpleName();
    private ActivityTitleView title;
    private Button LoaderBTN;
    private Button IRDBTN;
    private Button CABTN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camenu);

        InitTitle();

        LoaderBTN = (Button) findViewById(R.id.loaderBTN);
        IRDBTN = (Button) findViewById(R.id.irdBTN);
        CABTN = (Button) findViewById(R.id.caBTN);
        LoaderBTN.setOnClickListener(LoaderClickListener);
        IRDBTN.setOnClickListener(IRDClickListener);
        CABTN.setOnClickListener(CAClickListener);
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.TitleLayout);
        title.setTitleView(getString(R.string.STR_CA_MENU_TITLE));
    }

    private View.OnClickListener LoaderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent it = new Intent();
            it.setClass( CAMenuActivity.this, LoaderInfoActivity.class );
            startActivity( it );
        }
    };

    private View.OnClickListener IRDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent it = new Intent();
            it.setClass( CAMenuActivity.this, IRDInfoActivity.class );
            startActivity( it );
        }
    };

    private View.OnClickListener CAClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent it = new Intent();
            it.setClass( CAMenuActivity.this, CAInfoActivity.class );
            startActivity( it );
        }
    };
}
