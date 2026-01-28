package com.prime.dtvplayer.TestMiddleware;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.prime.dtvplayer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestTunerActivity extends AppCompatActivity {
    private String TAG=getClass().getSimpleName();
    private enum Items {
        TunerDVBT(0),
        TunerDVBS(1),
        TunerDVBC(2),
        TunerISDBT(3),
        Max(4);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestTunerActivity.Items testItem : TestTunerActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestTunerActivity.Items valueOf(int testItem) {
            return (TestTunerActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private static final int[] BUTTON_IDS = {
            R.id.BtnTestTuner1,
            R.id.BtnTestTuner2,
            R.id.BtnTestTuner3,
            R.id.BtnTestTuner4,
    };
    private static final String[] ITEM_NAME = {
            "DVB-T Tuner",
            "DVB-S Tuner",
            "DVB-C Tuner",
            "ISDB-T Tuner",
    };
    int mTestResult=0;//
    TestMidMain tm = new TestMidMain();//


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_tuner);
        Bundle bundle =this.getIntent().getExtras();//need

        //Buttons
        buttons = new ArrayList<Button>();
        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);
            //button.setOnClickListener(this); // maybe
            buttons.add(button);
        }
        for(int i=Items.TunerDVBT.getValue(); i< Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }
    }

    public void BtnTunerDVBT_OnClick(View view){
        Log.d(TAG,"BtnTunerDVBT_OnClick:");
        int itemIndex = Items.TunerDVBT.getValue();
    }
    public void BtnTunerDVBS_OnClick(View view){
        Log.d(TAG,"BtnTunerDVBS_OnClick:");
        int itemIndex = Items.TunerDVBS.getValue();
    }
    public void BtnTunerDVBC_OnClick(View view){
        Log.d(TAG,"BtnTunerDVBC_OnClick:");
        int itemIndex = Items.TunerDVBC.getValue();
        Intent intent = new Intent();//need
        Bundle bundle =this.getIntent().getExtras();//need

        intent.setClass(TestTunerActivity.this,TestTunerDVBCActivity.class);
        intent.putExtra("position",bundle.getInt("position"));//need

        startActivity(intent);

        //intent.putExtra("position",position+LIST_MAX_ITEM);//need
    }

    public void BtnTunerISDBT_OnClick(View view){
        Log.d(TAG,"BtnTunerISDBT_OnClick:");
        int itemIndex = Items.TunerISDBT.getValue();
        Intent intent = new Intent();//need
        Bundle bundle =this.getIntent().getExtras();//need

        //intent.setClass(TestTunerActivity.this,TestTunerDVBCActivity.class); //DVB-C
        intent.setClass(TestTunerActivity.this,TestTunerISDBTActivity.class);
        intent.putExtra("position",bundle.getInt("position"));//need

        startActivity(intent);

        //intent.putExtra("position",position+LIST_MAX_ITEM);//need
    }

}
