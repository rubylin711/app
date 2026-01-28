package com.prime.dtvplayer.TestMiddleware;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

public class TestFavGroupActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    TestMidMain tm = new TestMidMain();//need
    Button BtGroupNameGet=null;
    Button BtGroupNameSave=null;
    TextView TvGroupNameGetResult=null;
    TextView TvGroupNameSaveResult=null;
    TextView TvInfo=null;
    private static final int TESTITEM_NUM = 2;
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];
    int mTestResult=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fav_group);

        BtGroupNameGet=(Button)this.findViewById(R.id.TestFavGroupBt1);
        BtGroupNameSave=(Button)this.findViewById(R.id.TestFavGroupBt2);
        TvGroupNameGetResult=(TextView)this.findViewById(R.id.TestFavGroupTv1Result);
        TvGroupNameSaveResult=(TextView)this.findViewById(R.id.TestFavGroupTv2Result);
        TvInfo=(TextView)this.findViewById(R.id.TestFavGroupTv3Info);

        //BtGroupNameGet.setText("GroupNameGet(int GroupType)");
        //BtGroupNameSave.setText("GroupNameSave(int GroupType, String name)");
        TvGroupNameGetResult.setText("Result");
        TvGroupNameSaveResult.setText("Result");

        BtGroupNameGet.setOnClickListener(GroupNameGetListener);
        BtGroupNameSave.setOnClickListener(GroupNameSaveListener);





    }

    private void setCheckedResult(int ItemIndex, boolean testResult){
        int allchecked=0;
        int itemresult=0;
        Bundle bundle =this.getIntent().getExtras();//need

        SubItemChecked[ItemIndex] = true;
        //if(test_ok==false)
        mTestResult = tm.bitwiseLeftShift(mTestResult, ItemIndex, testResult);
        for(int i=0; i< TESTITEM_NUM; i++)
        {
            if(SubItemChecked[i]==true)
                allchecked++;
        }
        if(allchecked == TESTITEM_NUM) {
            tm.getTestInfoByIndex(bundle.getInt("position")).setChecked(true);
            tm.getTestInfoByIndex(bundle.getInt("position")).setResult(mTestResult);
        }
    }

    private View.OnClickListener GroupNameGetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "GroupNameGetListener--onClick");
            TvInfo.setText("Info:"
                    +"\n(1)GroupNameGet(ProgramInfo.ALL_TV_TYPE):"+ GroupNameGet(ProgramInfo.ALL_TV_TYPE)
                    +"\n(2)GroupNameGet(ProgramInfo.TV_FAV1_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV1_TYPE)
                    +"\n(3)GroupNameGet(ProgramInfo.TV_FAV2_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV2_TYPE)
                    +"\n(4)GroupNameGet(ProgramInfo.TV_FAV3_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV3_TYPE)
                    +"\n(5)GroupNameGet(ProgramInfo.TV_FAV4_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV4_TYPE)
                    +"\n(6)GroupNameGet(ProgramInfo.TV_FAV5_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV5_TYPE)
                    +"\n(7)GroupNameGet(ProgramInfo.TV_FAV6_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV6_TYPE)
                    +"\n(8)GroupNameGet(ProgramInfo.ALL_RADIO_TYPE):"+ GroupNameGet(ProgramInfo.ALL_RADIO_TYPE)
                    +"\n(9)GroupNameGet(ProgramInfo.RADIO_FAV1_TYPE):"+ GroupNameGet(ProgramInfo.RADIO_FAV1_TYPE)
                    +"\n(10)GroupNameGet(ProgramInfo.RADIO_FAV2_TYPE):"+ GroupNameGet(ProgramInfo.RADIO_FAV2_TYPE)
                    +"\n\nInvalid parameter:"
                    +"\n(1)GroupNameGet(ProgramInfo.ALL_TV_RADIO_TYPE_MAX):"+ GroupNameGet(ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
                    +"\n(2)GroupNameGet(-1):"+ GroupNameGet(-1)
                );
            TvGroupNameGetResult.setText("Result: Pass");

            setCheckedResult(0, true);
        }
    };


    private View.OnClickListener GroupNameSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "GroupNameSaveListener--onClick");
            String all_tv_radio_max_str = "MAX MAX MAX MAX MAX MAX MAX MAX";
            String minus_one_str = "-1-1-1-1-1-1-1-1-1-1-1-1-1-1-1-1-1";
            String all_tv_str = "All TV All TV All TV All TV";
            String tv_fav1_str = "TV_FAV1 TV_FAV1 TV_FAV1";
            String tv_fav2_str = "TV_FAV2 TV_FAV2 TV_FAV2";
            String tv_fav3_str = "TV_FAV3 TV_FAV3 TV_FAV3";
            String tv_fav4_str = "TV_FAV4 TV_FAV4 TV_FAV4";
            String tv_fav5_str = "TV_FAV5 TV_FAV5 TV_FAV5";
            String tv_fav6_str = "TV_FAV6 TV_FAV6 TV_FAV6";
            String all_radio_str = "All Radio All Radio All Radio";
            String radio_fav1_str = "Radio FA1 Radio FA1 Radio FA1";
            String radio_fav2_str = "Radio FA2 Radio FA2 Radio FA2";

            GroupNameUpdate(ProgramInfo.ALL_TV_RADIO_TYPE_MAX, all_tv_radio_max_str);
            GroupNameUpdate(-1, minus_one_str);
            GroupNameUpdate(ProgramInfo.ALL_TV_TYPE, all_tv_str);
            GroupNameUpdate(ProgramInfo.TV_FAV1_TYPE, tv_fav1_str);
            GroupNameUpdate(ProgramInfo.TV_FAV2_TYPE, tv_fav2_str);
            GroupNameUpdate(ProgramInfo.TV_FAV3_TYPE, tv_fav3_str);
            GroupNameUpdate(ProgramInfo.TV_FAV4_TYPE, tv_fav4_str);
            GroupNameUpdate(ProgramInfo.TV_FAV5_TYPE, tv_fav5_str);
            GroupNameUpdate(ProgramInfo.TV_FAV6_TYPE, tv_fav6_str);
            GroupNameUpdate(ProgramInfo.ALL_RADIO_TYPE, all_radio_str);
            GroupNameUpdate(ProgramInfo.RADIO_FAV1_TYPE, radio_fav1_str);
            GroupNameUpdate(ProgramInfo.RADIO_FAV2_TYPE, radio_fav2_str);


            TvInfo.setText("Info:"
                    +"\n(1)GroupNameGet(ProgramInfo.ALL_TV_TYPE):"+ GroupNameGet(ProgramInfo.ALL_TV_TYPE)
                    +"\n(2)GroupNameGet(ProgramInfo.TV_FAV1_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV1_TYPE)
                    +"\n(3)GroupNameGet(ProgramInfo.TV_FAV2_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV2_TYPE)
                    +"\n(4)GroupNameGet(ProgramInfo.TV_FAV3_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV3_TYPE)
                    +"\n(5)GroupNameGet(ProgramInfo.TV_FAV4_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV4_TYPE)
                    +"\n(6)GroupNameGet(ProgramInfo.TV_FAV5_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV5_TYPE)
                    +"\n(7)GroupNameGet(ProgramInfo.TV_FAV6_TYPE):"+ GroupNameGet(ProgramInfo.TV_FAV6_TYPE)
                    +"\n(8)GroupNameGet(ProgramInfo.ALL_RADIO_TYPE):"+ GroupNameGet(ProgramInfo.ALL_RADIO_TYPE)
                    +"\n(9)GroupNameGet(ProgramInfo.RADIO_FAV1_TYPE):"+ GroupNameGet(ProgramInfo.RADIO_FAV1_TYPE)
                    +"\n(10)GroupNameGet(ProgramInfo.RADIO_FAV2_TYPE):"+ GroupNameGet(ProgramInfo.RADIO_FAV2_TYPE)
                    +"\n\nInvalid parameter:"
                    +"\n(1)GroupNameGet(ProgramInfo.ALL_TV_RADIO_TYPE_MAX):"+ GroupNameGet(ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
                    +"\n(2)GroupNameGet(-1):"+ GroupNameGet(-1)
            );

            if(//all_tv_radio_max_str.equals(GroupNameGet(ProgramInfo.ALL_TV_RADIO_TYPE_MAX))
               //     && minus_one_str.equals(GroupNameGet(-1))
                    all_tv_str.equals(GroupNameGet(ProgramInfo.ALL_TV_TYPE))
                    && tv_fav1_str.equals(GroupNameGet(ProgramInfo.TV_FAV1_TYPE))
                    && tv_fav2_str.equals(GroupNameGet(ProgramInfo.TV_FAV2_TYPE))
                    && tv_fav3_str.equals(GroupNameGet(ProgramInfo.TV_FAV3_TYPE))
                    && tv_fav4_str.equals(GroupNameGet(ProgramInfo.TV_FAV4_TYPE))
                    && tv_fav5_str.equals(GroupNameGet(ProgramInfo.TV_FAV5_TYPE))
                    && tv_fav6_str.equals(GroupNameGet(ProgramInfo.TV_FAV6_TYPE))
                    && all_radio_str.equals(GroupNameGet(ProgramInfo.ALL_RADIO_TYPE))
                    && radio_fav1_str.equals(GroupNameGet(ProgramInfo.RADIO_FAV1_TYPE))
                    && radio_fav2_str.equals(GroupNameGet(ProgramInfo.RADIO_FAV2_TYPE))) {
                TvGroupNameSaveResult.setText("Result: Pass");
                setCheckedResult(1, true);
            }
            else{
                TvGroupNameSaveResult.setText("Result: Fail");
                setCheckedResult(1, false);
            }
        }
    };
}
