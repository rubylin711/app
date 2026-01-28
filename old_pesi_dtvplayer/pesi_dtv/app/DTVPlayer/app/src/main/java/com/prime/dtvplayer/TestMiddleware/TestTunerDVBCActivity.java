package com.prime.dtvplayer.TestMiddleware;

import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestTunerDVBCActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private enum Items {
        Tune(0),
        TuneByExistTp(1),
        Max(2);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestTunerDVBCActivity.Items testItem : TestTunerDVBCActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestTunerDVBCActivity.Items valueOf(int testItem) {
            return (TestTunerDVBCActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    private enum Textviews {
        TpId(0),
        Frequency(1),
        SymbolRate(2),
        QAM(3),
        LockStatus(4),
        Strength(5),
        Quality(6),
        BER(7),
        SNR(8),
        Max(9);
        private int value;
        private static Map map = new HashMap<>();
        private Textviews(int value) {
            this.value = value;
        }
        static {
            for (TestTunerDVBCActivity.Textviews tv : TestTunerDVBCActivity.Textviews.values()) {
                map.put(tv.value, tv);
            }
        }
        public static TestTunerDVBCActivity.Textviews valueOf(int tv) {
            return (TestTunerDVBCActivity.Textviews)map.get(tv);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private List<TextView> testviews;
    private static final int[] BUTTON_IDS = {
            R.id.buttonDVBCTune,
            R.id.buttonDVBCTuneByExistTp,
    };

    private static final int[] TEXTVIEW_IDS = {
            R.id.textViewTestDvbCTpId,
            R.id.textViewTestDvbCFrequency,
            R.id.textViewTestDvbCSymbolRate,
            R.id.textViewTestDvbCQam,
            R.id.textViewTestDvbCLockStatus,
            R.id.textViewTestDvbCStrength,
            R.id.textViewTestDvbCQuality,
            R.id.textViewTestDvbCBER,
            R.id.textViewTestDvbCSNR,
    };
    private static final String[] TEXTVIEW_STRING = {
            "TpId: ",
            "Frequency: ",
            "SymbolRate: ",
            "QAM: ",
            "LockStatus: ",
            "Strength: ",
            "Quality: ",
            "BER: ",
            "SNR: ",
    };
    private static final String[] ITEM_NAME = {
            "TunerTuneDVBC(int tunerId, int tpId, int frequency, int symbolRate, int Qam)\nResult:",
            "TunerTuneByExistTp(int tunerId,int tpId)\nResult:",
    };
    int mTestResult=0;//
    TestMidMain tm = new TestMidMain();//
    private TextView TvFavInfo=null;
    private static final int TESTITEM_NUM = TestTunerDVBCActivity.Items.Max.getValue();
    private static final int TEXTVIEW_NUM = TestTunerDVBCActivity.Textviews.Max.getValue();
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    //private List<TpInfo> mTpList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_tuner_dvbc);

        //Buttons
        buttons = new ArrayList<Button>();
        testviews = new ArrayList<TextView>();
        /*
        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);
            //button.setOnClickListener(this); // maybe
            buttons.add(button);
        }
        */
        for(int i = TestTunerDVBCActivity.Items.Tune.getValue(); i< TestTunerDVBCActivity.Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        for(int i = TestTunerDVBCActivity.Textviews.TpId.getValue(); i< TestTunerDVBCActivity.Textviews.Max.getValue(); i++){
            TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
            testviews.add(tv);
            testviews.get(i).setText(TEXTVIEW_STRING[i]);
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

    public void BtnTunerTuneDVBC_OnClick(View view){
        //void TunerTuneDVBC(int tunerId, int tpId, int frequency, int symbolRate, int Qam)
        Log.d(TAG,"BtnTunerTuneDVBC_OnClick:");
        int itemIndex = Items.Tune.getValue();
        int TestTpIndex = 50;
        int TestTpId = 10;
        int Frequency = 698000;
        int Symbolrate = 6875000;
        int Qam = TpInfo.Cable.QAM_64;
        int tpID = 0;

        String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);

        //if(mTpList==null)
        //    mTpList = new ArrayList<TpInfo>();
        //mTpList = TpInfoGetList(TpInfo.DVBC);

        List<TpInfo> tpList = TpInfoGetList(getTunerType());
        TestTpId = tpList.get(TestTpIndex).getTpId();

        //show
        Frequency = TpInfoGet(TestTpId).CableTp.getFreq(); //mTpList.get(TestTpId).CableTp.getFreq();
        Symbolrate = TpInfoGet(TestTpId).CableTp.getSymbol(); //mTpList.get(TestTpId).CableTp.getSymbol();
        Qam = TpInfoGet(TestTpId).CableTp.getQam(); //mTpList.get(TestTpId).CableTp.getQam();
        tpID = TpInfoGet(TestTpId).getTpId(); //mTpList.get(TestTpId).getTpId();
        Log.d(TAG,"BtnTunerTuneDVBC_OnClick: tpID="+tpID+", Frequency="+Frequency+", Symbolrate="+Symbolrate+", Qam="+Qam);
        //TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
        testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()]+tpID);
        testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()]+Frequency);
        testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()]+Symbolrate);
        testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()]+str_qam_list[Qam]);

        //Tune
        TunerTuneDVBC( 0, tpID, Frequency,Symbolrate, Qam);

        // must delay when trying to get lock, strength, quality ...etc after TunerTuneDVBC
        Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int Quality=0;
                int Strength =0;
                int lock = 0;
                String ber;
                int snr = 0;

                lock = TunerGetLockStatus(0);
                Strength = TunerGetStrength(0);
                Quality = TunerGetQuality(0);
                ber = TunerGetBER(0);
                snr = TunerGetSNR(0);

                //show, use string.format because "string"+"string" has problem
                testviews.get(Textviews.LockStatus.getValue()).setText(String.format("%s%s",TEXTVIEW_STRING[Textviews.LockStatus.getValue()], lock));
                testviews.get(Textviews.Strength.getValue()).setText(String.format("%s%s",TEXTVIEW_STRING[Textviews.Strength.getValue()], Strength));
                testviews.get(Textviews.Quality.getValue()).setText(String.format("%s%s",TEXTVIEW_STRING[Textviews.Quality.getValue()], Quality));
                testviews.get(Textviews.BER.getValue()).setText(String.format("%s%s",TEXTVIEW_STRING[Textviews.BER.getValue()], ber));
                testviews.get(Textviews.SNR.getValue()).setText(String.format("%s%s",TEXTVIEW_STRING[Textviews.SNR.getValue()], snr));
            }
        };

        // 650ms is the least required delay, use 700 for safety
        handler.postDelayed(runnable, 700);

        Log.d(TAG,"BtnTunerTuneDVBC_OnClick: BK_end");
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }

    public void BtnTunerTuneByExistTp_OnClick(View view){
        //TunerTuneByExistTp(int tunerId,int tpId)
        Log.d(TAG,"BtnTunerTuneByExistTp_OnClick:");
        int itemIndex = Items.TuneByExistTp.getValue();
        int TestTpIndex=51;
        int TestTpId = 11;
        int Frequency = 0;
        int Symbolrate = 0;
        int Qam = 0;
        int tpID = 0;
        int Quality=0;
        int Strength =0;
        int lock = 0;
        String ber;
        int snr = 0;
        String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);

        //if(mTpList==null)
        //    mTpList = new ArrayList<TpInfo>();
        //mTpList = TpInfoGetList(TpInfo.DVBC);

        List<TpInfo> tpList = TpInfoGetList(getTunerType());
        TestTpId = tpList.get(TestTpIndex).getTpId();

        //show
        Frequency = TpInfoGet(TestTpId).CableTp.getFreq(); //mTpList.get(TestTpId).CableTp.getFreq();
        Symbolrate = TpInfoGet(TestTpId).CableTp.getSymbol();//mTpList.get(TestTpId).CableTp.getSymbol();
        Qam = TpInfoGet(TestTpId).CableTp.getQam();//mTpList.get(TestTpId).CableTp.getQam();
        tpID = TpInfoGet(TestTpId).getTpId();//mTpList.get(TestTpId).getTpId();
        Log.d(TAG,"BtnTunerTuneByExistTp_OnClick: tpID="+tpID+", Frequency="+Frequency+", Symbolrate="+Symbolrate+", Qam="+Qam);
        //TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
        testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()]+tpID);
        testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()]+Frequency);
        testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()]+Symbolrate);
        testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()]+str_qam_list[Qam]);

        //Tune
        TunerTuneByExistTp(0, tpID);

        //show
        lock = TunerGetLockStatus(0);
        Strength = TunerGetStrength(0);
        Quality = TunerGetQuality(0);
        ber = TunerGetBER(0);
        snr = TunerGetSNR(0);


        testviews.get(Textviews.LockStatus.getValue()).setText(TEXTVIEW_STRING[Textviews.LockStatus.getValue()]+lock);
        testviews.get(Textviews.Strength.getValue()).setText(TEXTVIEW_STRING[Textviews.Strength.getValue()]+Strength);
        testviews.get(Textviews.Quality.getValue()).setText(TEXTVIEW_STRING[Textviews.Quality.getValue()]+Quality);
        testviews.get(Textviews.BER.getValue()).setText(TEXTVIEW_STRING[Textviews.BER.getValue()]+ber);
        testviews.get(Textviews.SNR.getValue()).setText(TEXTVIEW_STRING[Textviews.SNR.getValue()]+snr);

        Log.d(TAG,"BtnTunerTuneByExistTp_OnClick: BK_end");
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }
}
