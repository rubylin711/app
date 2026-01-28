package com.prime.dtvplayer.TestMiddleware;

import androidx.appcompat.app.AppCompatActivity;
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

public class TestTunerISDBTActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private enum Items {
        Tune(0),
        TuneByExistTp(1),
        Antenna5V(2),
        Max(3);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestTunerISDBTActivity.Items testItem : TestTunerISDBTActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestTunerISDBTActivity.Items valueOf(int testItem) {
            return (TestTunerISDBTActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    private enum Textviews {
        TpId(0),
        Frequency(1),
        Bandwidth(2),
        LockStatus(3),
        Strength(4),
        Quality(5),
        BER(6),
        SNR(7),
        Max(8);
        private int value;
        private static Map map = new HashMap<>();
        private Textviews(int value) {
            this.value = value;
        }
        static {
            for (TestTunerISDBTActivity.Textviews tv : TestTunerISDBTActivity.Textviews.values()) {
                map.put(tv.value, tv);
            }
        }
        public static TestTunerISDBTActivity.Textviews valueOf(int tv) {
            return (TestTunerISDBTActivity.Textviews)map.get(tv);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private List<TextView> testviews;
    private static final int[] BUTTON_IDS = {
            R.id.buttonIsdbTTune,
            R.id.buttonIsdbTTuneByExistTp,
            R.id.buttonIsdbTTunerSetAntenna5V,
    };

    private static final int[] TEXTVIEW_IDS = {
            R.id.textViewTestIsdbTTpId,
            R.id.textViewTestIsdbTFrequency,
            R.id.textViewTestIsdbTBandwidth,
            //R.id.textViewTestIsdbTQam,
            R.id.textViewTestIsdbTLockStatus,
            R.id.textViewTestIsdbTStrength,
            R.id.textViewTestIsdbTQuality,
            R.id.textViewTestIsdbTBER,
            R.id.textViewTestIsdbTSNR,
    };
    private static final String[] TEXTVIEW_STRING = {
            "TpId: ",
            "Frequency: ",
            "Bandwidth: ",            
            "LockStatus: ",
            "Strength: ",
            "Quality: ",
            "BER: ",
            "SNR: ",
    };
    private static final String[] ITEM_NAME = {
            "TunerTuneDVBT(int tunerId, int tpId, int frequency, int bandwith)\nResult:",
            "TunerTuneByExistTp(int tunerId,int tpId)\nResult:",
            "TunerSetAntenna5V\nResult:",
    };
    int mTestResult=0;//
    TestMidMain tm = new TestMidMain();//
    private TextView TvFavInfo=null;
    private static final int TESTITEM_NUM = TestTunerISDBTActivity.Items.Max.getValue();
    private static final int TEXTVIEW_NUM = TestTunerISDBTActivity.Textviews.Max.getValue();
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    private List<TpInfo> mTpList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_tuner_isdbt);

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
        for(int i = TestTunerISDBTActivity.Items.Tune.getValue(); i< TestTunerISDBTActivity.Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        for(int i = TestTunerISDBTActivity.Textviews.TpId.getValue(); i< TestTunerISDBTActivity.Textviews.Max.getValue(); i++){
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

    public void BtnTunerTuneISDBT_OnClick(View view){
        //TunerTuneDVBT(int tunerId, int tpId, int frequency, int bandwith)
        Log.d(TAG,"BtnTunerTuneISDBT_OnClick:");
        int itemIndex = TestTunerISDBTActivity.Items.Tune.getValue();
        int TestTpId = 10;
        int Frequency = 0;
        int Bandwidth = 0;        
        int tpID = 0;
        int Quality=0;
        int Strength =0;
        int lock = 0;
        String ber;
        int snr = 0;
        String[] str_bandwidth_list = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);

        if(mTpList==null)
            mTpList = new ArrayList<TpInfo>();
        mTpList = TpInfoGetList(TpInfo.ISDBT);

        //show
        Frequency = mTpList.get(TestTpId).TerrTp.getFreq();
        Bandwidth = mTpList.get(TestTpId).TerrTp.getBand();

        tpID = mTpList.get(TestTpId).getTpId();
        Log.d(TAG,"BtnTunerTuneISDBT_OnClick: tpID="+tpID+", Frequency="+Frequency+", Bandwidth="+Bandwidth);
        //TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
        testviews.get(TestTunerISDBTActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.TpId.getValue()]+tpID);
        testviews.get(TestTunerISDBTActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Frequency.getValue()]+Frequency);
        testviews.get(TestTunerISDBTActivity.Textviews.Bandwidth.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Bandwidth.getValue()]+str_bandwidth_list[Bandwidth]);        

        //Tune        
        TunerTuneISDBT( 0, tpID, Frequency,Bandwidth);

        //show
        lock = TunerGetLockStatus(0);
        Strength = TunerGetStrength(0);
        Quality = TunerGetQuality(0);
        ber = TunerGetBER(0);
        snr = TunerGetSNR(0);

        testviews.get(TestTunerISDBTActivity.Textviews.LockStatus.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.LockStatus.getValue()]+lock);
        testviews.get(TestTunerISDBTActivity.Textviews.Strength.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Strength.getValue()]+Strength);
        testviews.get(TestTunerISDBTActivity.Textviews.Quality.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Quality.getValue()]+Quality);
        testviews.get(TestTunerISDBTActivity.Textviews.BER.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.BER.getValue()]+ber);
        testviews.get(TestTunerISDBTActivity.Textviews.SNR.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.SNR.getValue()]+snr);

        Log.d(TAG,"BtnTunerTuneISDBT_OnClick: BK_end");
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }

    public void BtnTunerTuneByExistTpISDBT_OnClick(View view){
        //TunerTuneByExistTp(int tunerId,int tpId)
        Log.d(TAG,"BtnTunerTuneByExistTpISDBT_OnClick:");
        int itemIndex = TestTunerISDBTActivity.Items.TuneByExistTp.getValue();
        int TestTpId = 11;
        int Frequency = 0;
        int Bandwidth = 0;        
        int tpID = 0;
        int Quality=0;
        int Strength =0;
        int lock = 0;
        String ber;
        int snr = 0;
        String[] str_bandwidth_list = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);

        if(mTpList==null)
            mTpList = new ArrayList<TpInfo>();
        mTpList = TpInfoGetList(TpInfo.ISDBT);

        //show
        Frequency = mTpList.get(TestTpId).TerrTp.getFreq();
        Bandwidth = mTpList.get(TestTpId).TerrTp.getBand();

        tpID = mTpList.get(TestTpId).getTpId();
        Log.d(TAG,"BtnTunerTuneByExistTpISDBT_OnClick: tpID="+tpID+", Frequency="+Frequency+", Bandwidth="+Bandwidth);
        //TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
        testviews.get(TestTunerISDBTActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.TpId.getValue()]+tpID);
        testviews.get(TestTunerISDBTActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Frequency.getValue()]+Frequency);
        testviews.get(TestTunerISDBTActivity.Textviews.Bandwidth.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Bandwidth.getValue()]+str_bandwidth_list[Bandwidth]);
        //testviews.get(TestTunerISDBTActivity.Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.QAM.getValue()]+str_qam_list[Qam]);

        //Tune
        TunerTuneByExistTp(0, tpID);

        //show
        lock = TunerGetLockStatus(0);
        Strength = TunerGetStrength(0);
        Quality = TunerGetQuality(0);
        ber = TunerGetBER(0);
        snr = TunerGetSNR(0);


        testviews.get(TestTunerISDBTActivity.Textviews.LockStatus.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.LockStatus.getValue()]+lock);
        testviews.get(TestTunerISDBTActivity.Textviews.Strength.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Strength.getValue()]+Strength);
        testviews.get(TestTunerISDBTActivity.Textviews.Quality.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.Quality.getValue()]+Quality);
        testviews.get(TestTunerISDBTActivity.Textviews.BER.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.BER.getValue()]+ber);
        testviews.get(TestTunerISDBTActivity.Textviews.SNR.getValue()).setText(TEXTVIEW_STRING[TestTunerISDBTActivity.Textviews.SNR.getValue()]+snr);

        Log.d(TAG,"BtnTunerTuneByExistTpISDBT_OnClick: BK_end");
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }

    static int Antenna5V_OnOff = 0;
    public void BtnTunerSetAntenna5V_OnClick(View view){
        Log.d(TAG,"BtnTunerSetAntenna5V_OnClick:");

        int itemIndex = TestTunerISDBTActivity.Items.Antenna5V.getValue();
        String OnOff ="";
        if(Antenna5V_OnOff == 0) // off
        {
            OnOff = "ON";
            Antenna5V_OnOff = 1; // on
        }
        else {
            OnOff = "OFF";
            Antenna5V_OnOff = 0;
        }

        int ret = TunerSetAntenna5V(0, Antenna5V_OnOff);

        if(ret != -1) {
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex] + "Pass     setting : " + Antenna5V_OnOff + "  " + OnOff);
            setCheckedResult(itemIndex, true);
        }
        else {
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex] + "Fail     setting : " + Antenna5V_OnOff + "   " + OnOff);
            setCheckedResult(itemIndex, false);
        }

    }
}
