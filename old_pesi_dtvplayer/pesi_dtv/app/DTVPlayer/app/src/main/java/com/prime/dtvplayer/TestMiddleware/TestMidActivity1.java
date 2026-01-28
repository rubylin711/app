package com.prime.dtvplayer.TestMiddleware;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.prime.dtvplayer.R;

import java.util.ArrayList;
import java.util.List;

public class TestMidActivity1 extends AppCompatActivity {
    private String TAG=getClass().getSimpleName();
    private List<TestMidInfo> mInfoLeft=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TestMidMain tm = new TestMidMain();//need
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mid1);




        Bundle bundle =this.getIntent().getExtras();//need
        Log.d(TAG, "onCreate--position="+bundle.getInt("position"));

        //test start
        int result=0;
        result = tm.bitwiseLeftShift(result, 0, false);//fail item
        result = tm.bitwiseLeftShift(result, 2,false);//fail item
        result = tm.bitwiseLeftShift(result, 3, false);//fail item
        result = tm.bitwiseLeftShift(result, 8, false);//fail item
        tm.getTestInfoByIndex(bundle.getInt("position")).setResult(result);
        //test end

        tm.getTestInfoByIndex(bundle.getInt("position")).setChecked(true);//need









    }
}
