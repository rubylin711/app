package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ChannelHistory;
import com.prime.dtvplayer.utils.TVMessage;

import java.util.HashMap;
import java.util.Map;

public class TestRestToFactoryActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private enum Items {
        Invoke(0),
        Callback(1),
        ResetToFactory(2),
        Max(3);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestRestToFactoryActivity.Items testItem : TestRestToFactoryActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestRestToFactoryActivity.Items valueOf(int testItem) {
            return (TestRestToFactoryActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    TestMidMain tm = new TestMidMain();//
    private static final int TESTITEM_NUM = TestRestToFactoryActivity.Items.Max.getValue();//
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    int mTestResult=0;//
    Button btnResetToFacotry;
    TextView txvResult;
    TextView invokeTestResult;
    TextView callbackTestResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_rest_to_factory);

        btnResetToFacotry = (Button)this.findViewById(R.id.buttonTestRestToFactory);
        txvResult = (TextView)this.findViewById(R.id.textResult);
        invokeTestResult = (TextView)this.findViewById(R.id.invoketest_txt);
        callbackTestResult = (TextView)this.findViewById(R.id.callbacktest_txt);
    }
    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);

        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_CALLBACK_TEST:{//Scoty 20190410 add Mtest Pc Tool callback
                int param1 = tvMessage.getTestParam1(), param2 = tvMessage.getTestParam2();
                String testName = tvMessage.getTestName();
                Log.d(TAG,"TYPE_CALLBACK_TEST param1 = " + param1 + " param2 = " + param1
                    + " testName = "+testName);
                //callback test data right :  param1 = 1, param2 = 1, testName = PESI_EVT_PT_DEBUG_CALLBACK_TEST
                // connect pc tool
                int itemIndex = Items.Callback.getValue();
                if(param1 == 1 && param2 == 1 && testName.equals("PESI_EVT_PT_DEBUG_CALLBACK_TEST")) {
                    callbackTestResult.setText("callback Test : OK");
                    setCheckedResult(itemIndex, true);
                }
                else {
                    callbackTestResult.setText("callback Test : Fail");
                    setCheckedResult(itemIndex, false);
                }

            }break;
            default:
                break;
        }
    }
    private void setCheckedResult(int ItemIndex, boolean testResult){
        int allchecked=0;
        Bundle bundle =this.getIntent().getExtras();//need

        SubItemChecked[ItemIndex] = true;

        mTestResult = tm.bitwiseLeftShift(mTestResult, ItemIndex, testResult);
        for(int i=0; i< TESTITEM_NUM; i++){
            if(SubItemChecked[i]==true)
                allchecked++;
        }
        if(allchecked == TESTITEM_NUM) {
            tm.getTestInfoByIndex(bundle.getInt("position")).setChecked(true);
            tm.getTestInfoByIndex(bundle.getInt("position")).setResult(mTestResult);
        }
    }

    public void BtnInvokeTest_OnClick(View view){
        //void ProgramManagerInit(int type)
        //time/tv manager call this method
        Log.d(TAG,"BtnInvokeTest_OnClick:");
        int itemIndex = Items.Invoke.getValue();
        StringBuilder zText = new StringBuilder ();

        //copy from ResetDefaultActivity.java
        if(InvokeTest() == 0) {
            invokeTestResult.setText("Invoke Test : OK");
            setCheckedResult(itemIndex, true);
        }
        else {
            invokeTestResult.setText("Invoke Test : Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    public void BtnResetToFactory_OnClick(View view){
        //void ProgramManagerInit(int type)
        //time/tv manager call this method
        Log.d(TAG,"BtnResetToFactory_OnClick:");
        int itemIndex = Items.ResetToFactory.getValue();
        StringBuilder zText = new StringBuilder ();

        //copy from ResetDefaultActivity.java
        ResetDefault();
        AvControlPlayStop(ViewHistory.getPlayId());
        AvControlClose(ViewHistory.getPlayId());
        SetChannelExist(0);
        ChannelHistory.Reset();
        ResetTotalChannelList();

        txvResult.setText("Status: OK");
        setCheckedResult(itemIndex, true);
    }
}
