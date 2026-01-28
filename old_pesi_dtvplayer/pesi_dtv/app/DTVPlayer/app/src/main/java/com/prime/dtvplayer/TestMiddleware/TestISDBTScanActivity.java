package com.prime.dtvplayer.TestMiddleware;

import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.MessageDialog;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.utils.TVMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestISDBTScanActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private enum Items {
        StartScanManual(0),
        StartScanAuto(1),
        Complete(2),
        Cancel(3),
        Max(4);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestISDBTScanActivity.Items testItem : TestISDBTScanActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestISDBTScanActivity.Items valueOf(int testItem) {
            return (TestISDBTScanActivity.Items)map.get(testItem);
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
        TvTotalCnt(6),
        RadioTotalCnt(7),
        BER(8),
        SNR(9),
        Max(10);
        private int value;
        private static Map map = new HashMap<>();
        private Textviews(int value) {
            this.value = value;
        }
        static {
            for (TestISDBTScanActivity.Textviews tv : TestISDBTScanActivity.Textviews.values()) {
                map.put(tv.value, tv);
            }
        }
        public static TestISDBTScanActivity.Textviews valueOf(int tv) {
            return (TestISDBTScanActivity.Textviews)map.get(tv);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private List<TextView> testviews;
    private static final int[] BUTTON_IDS = {
            R.id.buttonTestIsdbTScanStartScanManual,
            R.id.buttonTestIsdbTScanStartScanAuto,
            //R.id.buttonTestStopScan,
            R.id.buttonTestIsdbTScanComplete,
            R.id.buttonTestIsdbTScanCancel,
    };
    private static final int[] TEXTVIEW_IDS = {
            R.id.textViewTestIsdbTScanTpId,
            R.id.textViewTestIsdbTScanFrequency,
            R.id.textViewTestIsdbTScanBandwidth,
            R.id.textViewTestIsdbTScanLockStatus,
            R.id.textViewTestIsdbTScanStrength,
            R.id.textViewTestIsdbTScanQuality,
            R.id.textViewTestIsdbTScanTotalTvCnt,
            R.id.textViewTestIsdbTScanTotalRadioCnt,
            R.id.textViewTestIsdbTScanBER,
            R.id.textViewTestIsdbTScanSNR,
    };
    private static final String[] TEXTVIEW_STRING = {
            "TpId: ",
            "Frequency: ",
            "Bandwidth: ",
            //"QAM: ",
            "LockStatus: ",
            "Strength: ",
            "Quality: ",
            "Tv count: ",
            "Radio count: ",
            "BER: ",
            "SNR: ",
    };
    private static final String[] ITEM_NAME = {
            "Manual Search\nScanParamsStartScan()\nResult:",
            "Auto Search\nScanParamsStartScan()\nResult:",
            "Complete\nScanParamsStopScan(true)\nResult:",
            "Cancel\nScanParamsStopScan(false)\nResult:",
    };
    TestMidMain tm = new TestMidMain();//
    private static final int TESTITEM_NUM = TestISDBTScanActivity.Items.Max.getValue();//    
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    int mTestResult=0;//
    private TextView TvScanTvResult=null, TvScanRadioResult=null;
    Handler CheckSignalHandler;
    private List<TpInfo> tpList = new ArrayList<>();
    int Frequency=0;
    int Bandwidth =0;

    int tpID=0;

    int scanMode=0;
    int searchOptionCaFta = 0;
    int searchOptionTVRadio = 0;
    int SearchCount = -1;
    private ArrayList<TestISDBTScanActivity.serviceInfo> tvlist=null;
    private ArrayList<TestISDBTScanActivity.serviceInfo> radiolist=null;
    private Button ButtonCancel;
    private Button ButtonComplete;
    int mTotalTvNumber;
    int mTotalRadioNumber;
    int mItemIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_isdbtscan);

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
        for(int i = TestISDBTScanActivity.Items.StartScanManual.getValue(); i< TestISDBTScanActivity.Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        for(int i = TestISDBTScanActivity.Textviews.TpId.getValue(); i< TestISDBTScanActivity.Textviews.Max.getValue(); i++){
            TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
            testviews.add(tv);
            testviews.get(i).setText(TEXTVIEW_STRING[i]);
        }

        //TextView
        TvScanTvResult = (TextView)this.findViewById(R.id.textViewTestIsdbTScanTvResult);
        TvScanTvResult.setText("Tv:");
        TvScanTvResult.setMovementMethod(new ScrollingMovementMethod());
        TvScanTvResult.scrollTo(0,0);

        //TextView
        TvScanRadioResult = (TextView)this.findViewById(R.id.textViewTestIsdbTScanRadioResult);
        TvScanRadioResult.setText("Radio:");
        TvScanRadioResult.setMovementMethod(new ScrollingMovementMethod());
        TvScanRadioResult.scrollTo(0,0);

        if(tpList==null)
            tpList = new ArrayList<TpInfo>();
        tpList = TpInfoGetList(TpInfo.ISDBT);

        if(tvlist==null)
            tvlist = new ArrayList<TestISDBTScanActivity.serviceInfo>();
        if(radiolist==null)
            radiolist = new ArrayList<TestISDBTScanActivity.serviceInfo>();

        // cancel & View Button
        Log.d(TAG, "onCreate: BK1");
        ButtonCancel = (Button)this.findViewById(R.id.buttonTestIsdbTScanCancel);
        Log.d(TAG, "onCreate: BK2");
        ButtonComplete = (Button)this.findViewById(R.id.buttonTestIsdbTScanComplete);
        Log.d(TAG, "onCreate: BK3");

        ButtonCancel.setOnClickListener(CancelListener);
        Log.d(TAG, "onCreate: BK4");
        ButtonComplete.setOnClickListener(CompleteListener);
        Log.d(TAG, "onCreate: BK5");
        ButtonComplete.setVisibility(View.INVISIBLE);
        Log.d(TAG, "onCreate: BK_end");
    }

    private void setCheckedResult(int ItemIndex, boolean testResult){
        int allchecked=0;
        Bundle bundle =this.getIntent().getExtras();//need

        SubItemChecked[ItemIndex] = true;
        //if(test_ok==false)
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

    @Override
    public void onConnected() {
        super.onConnected();
        Log.d(TAG, "TestTunerScanActivity:   onConnected");
        /*
        Log.d(TAG, "onConnected  freq="+Frequency + "symbol =" + Symbolrate+"qam="+Qam );
        CheckSignalHandler = new Handler();
        CheckSignalHandler.post(CheckStatusRunnable);
        Log.d(TAG, "onConnected: CheckSignalHandler post runnable" );
        ScanParamsStartScan(0, tpID,search_mode,ChannelType,ScanMode);
        */
    }
    @Override
    public void onDisconnected() {
        super.onDisconnected();
        CheckSignalHandler.removeCallbacks(CheckStatusRunnable);
        Log.d(TAG, "onDisconnected: CheckSignalHandler remove Callback" );
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        //Log.d(TAG, "onMessage: type="+tvMessage.getMsgType());//eric lin test
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_SCAN_BEGIN :
            {
                //Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_BEGIN!");
            }break;
            case TVMessage.TYPE_SCAN_PROCESS:
            {
                //Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_PROCESS!");
                UpdateStatus(tvMessage);
            }break;
            case TVMessage.TYPE_SCAN_END:
            {
                //Log.d(TAG, "onMessage:     TVMessage.TYPE_SCAN_END");
                ScanEnd(tvMessage);
            }break;
            default:
                break;
        }
    }

    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            UpdateSignalLevel(); //eric lin mark
            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };

    private void UpdateSignalLevel()
    {
        int Quality=0;
        int Strength =0;
        int lock = 0;
        int ber=0;
        int snr=0;
        //int barcolor = 0;
        lock = TunerGetLockStatus(0);
        Quality = TunerGetQuality(0);
        Strength = TunerGetStrength(0);

        testviews.get(TestISDBTScanActivity.Textviews.LockStatus.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.LockStatus.getValue()]+lock);
        testviews.get(TestISDBTScanActivity.Textviews.Strength.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Strength.getValue()]+Strength);
        testviews.get(TestISDBTScanActivity.Textviews.Quality.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Quality.getValue()]+Quality);
        testviews.get(TestISDBTScanActivity.Textviews.BER.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.BER.getValue()]+ber);
        testviews.get(TestISDBTScanActivity.Textviews.SNR.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.SNR.getValue()]+snr);
    }

    private View.OnClickListener CancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "CancelListener: BK1");
            int itemIndex = TestISDBTScanActivity.Items.Cancel.getValue();
            mItemIndex = itemIndex;

            ScanParamsStopScan(false);
            //FinishScanResult(false);
            /*
            if(mTotalTvNumber!=0 || mTotalRadioNumber!=0){//eric lin add
                Log.d(TAG, "CancelListener: BK2");
                buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex]+"Pass");
                setCheckedResult(mItemIndex, true);
            }else{
                Log.d(TAG, "CancelListener: BK3");
                buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex]+"Fail");
                setCheckedResult(mItemIndex, false);
            }
            */
        }
    };

    private View.OnClickListener CompleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "CompleteListener: BK1");
            int itemIndex = TestISDBTScanActivity.Items.Complete.getValue();
            mItemIndex = itemIndex;

            ScanParamsStopScan(true);
            //FinishScanResult(true);
            if(mTotalTvNumber!=0 || mTotalRadioNumber!=0){
                buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex]+"Pass");
                setCheckedResult(mItemIndex, true);
            }else{
                buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex]+"Fail");
                setCheckedResult(mItemIndex, false);
            }

        }
    };
    private class serviceInfo {
        private String ChName;
        private int ChLCN;
        private int ServiceType;
        private int CaFlag;
        private int tpId;//eric lin add

        private int getServiceType() {
            return ServiceType;
        }
        private void setServiceType(int serviceType) {
            this.ServiceType = serviceType;
        }
        private String getName() {
            return ChName;
        }
        private void setName(String name) {
            this.ChName = name;
        }
        private int getChNum() {
            return ChLCN;
        }
        private int getTpId() {
            return tpId;
        }
        private void setChLCN(int num) {
            this.ChLCN = num;
        }
        private int getCAflag() {
            return CaFlag;
        }
        private void setCAflag(int num) {
            this.CaFlag = num;
        }
        private void setTpId(int num) {
            this.tpId = num;
        }
    }

    private void UpdateStatus(TVMessage tvMessage)
    {
        String str;
        String[] str_bandwidth_list = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);
        //Log.d(TAG, "UpdateStatus:     tvMessage.getAlreadyScanedTpNum() = " + tvMessage.getAlreadyScanedTpNum());
        //Log.d(TAG, "UpdateStatus:   TVMessage.TYPE_SCAN_PROCESS!   service id = " + tvMessage.getServiceId());
        if(scanMode == 0 && SearchCount != tvMessage.getAlreadyScanedTpNum()) {
            SearchCount = tvMessage.getAlreadyScanedTpNum();
            // SearchCount  : 1 ~
            if(SearchCount <= tpList.size() &&
                    ( tpList.get(SearchCount - 1).TerrTp.getFreq() != Frequency) )
            {
                Log.d(TAG, "UpdateStatus:   Frequency = " + Frequency + "      table freq = " +  tpList.get(SearchCount - 1).TerrTp.getFreq() );
                Frequency = tpList.get(SearchCount - 1).TerrTp.getFreq();
                Bandwidth = tpList.get(SearchCount - 1).TerrTp.getBand();
                //channel = tpList.get(SearchCount - 1).TerrTp.getChannel();
                //str = getString(R.string.STR_FREQUENCY) + " : " + Frequency + getString(R.string.STR_KHZ) + "     Bandwidth : " + BandwidthBandwidth + getString(R.string.STR_KSPS);
                //TextviewInfo.setText(str);
                //Qam = tpList.get(SearchCount - 1).CableTp.getQam();
                testviews.get(TestISDBTScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TpId.getValue()]+(SearchCount - 1));
                testviews.get(TestISDBTScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Frequency.getValue()]+Frequency);
                testviews.get(TestISDBTScanActivity.Textviews.Bandwidth.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Bandwidth.getValue()]+str_bandwidth_list[Bandwidth]);
                //testviews.get(TestISDBTScanActivity.Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.QAM.getValue()]+str_qam_list[Qam]);
            }
        }

        if(tvMessage.getServiceId()!=0) {
            TestISDBTScanActivity.serviceInfo ch = new TestISDBTScanActivity.serviceInfo();
            ch.setName(tvMessage.getChannelName());
            ch.setChLCN(tvMessage.getchannelLCN());
            ch.setCAflag(tvMessage.getCAFlag());
            ch.setServiceType(tvMessage.getserviceType());
            if(scanMode == 0)//auto
                ch.setTpId(SearchCount-1);//eric lin 20180117 modify

            //Log.d(TAG, "UpdateStatus:   Name = " + ch.getName() + "   CA Flag = " + ch.getCAflag());
            if(tvMessage.getserviceType() == ProgramInfo.ALL_TV_TYPE)
                Log.d(TAG, "Update: TV lcn="+tvMessage.getchannelLCN());
            else if(tvMessage.getserviceType() == ProgramInfo.ALL_RADIO_TYPE)
                Log.d(TAG, "Update: radio lcn="+tvMessage.getchannelLCN());
            else
                Log.d(TAG, "Update: ??? type="+tvMessage.getserviceType());
            if (ch.getServiceType() == ProgramInfo.ALL_TV_TYPE) {
                tvlist.add(ch);
                //mTvListAdapter.notifyDataSetChanged();
                //ListviewTV.setSelection(tvlist.size() -1);
                showRealtimeScanResult(SearchCount-1, ProgramInfo.ALL_TV_TYPE); //showScanResult(ProgramInfo.ALL_TV_TYPE);
            }
            else if (ch.getServiceType() == ProgramInfo.ALL_RADIO_TYPE){
                radiolist.add(ch);
                //mRadioListAdapter.notifyDataSetChanged();
                //ListviewRadio.setSelection(radiolist.size() -1);
                showRealtimeScanResult(SearchCount-1, ProgramInfo.ALL_RADIO_TYPE); //showScanResult(ProgramInfo.ALL_RADIO_TYPE);
            }
        }else
            clearUITvRadioList();
    }
    private void ScanEnd(TVMessage tvMessage)
    {
        Log.d(TAG, "ScanEnd:   getTotalTVNumber = " + tvMessage.getTotalTVNumber());
        Log.d(TAG, "ScanEnd:   getTotalRadioNumber = " + tvMessage.getTotalRadioNumber());
        TextView tv;

        mTotalTvNumber = tvMessage.getTotalTVNumber();
        mTotalRadioNumber = tvMessage.getTotalRadioNumber();
        if (mTotalTvNumber != 0 || mTotalRadioNumber != 0) {
            if(scanMode == 0) {//auto
            buttons.get(TestISDBTScanActivity.Items.StartScanAuto.getValue()).setText(ITEM_NAME[TestISDBTScanActivity.Items.StartScanAuto.getValue()] + "Pass");
            setCheckedResult(TestISDBTScanActivity.Items.StartScanAuto.getValue(), true);
            }else if(scanMode == 1) {//manual
            buttons.get(TestISDBTScanActivity.Items.StartScanManual.getValue()).setText(ITEM_NAME[                      TestISDBTScanActivity.Items.StartScanManual.getValue()] + "Pass");
            setCheckedResult(TestISDBTScanActivity.Items.StartScanManual.getValue(), true);
            }

            buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex] + "Pass");
            setCheckedResult(mItemIndex, true);

            //show Tv/Radio count
            tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()]);
            testviews.get(TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()]+mTotalTvNumber);
            tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()]);
            testviews.get(TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()]+mTotalRadioNumber);

        } else {
            if(scanMode == 0) {//auto
            buttons.get(TestISDBTScanActivity.Items.StartScanAuto.getValue()).setText(ITEM_NAME[TestISDBTScanActivity.Items.StartScanAuto.getValue()] + "Fail");
            setCheckedResult(TestISDBTScanActivity.Items.StartScanAuto.getValue(), false);
            }else if(scanMode == 1) {//manual
            buttons.get(TestISDBTScanActivity.Items.StartScanManual.getValue()).setText(ITEM_NAME[    
            TestISDBTScanActivity.Items.StartScanManual.getValue()] + "Fail");
            setCheckedResult(TestISDBTScanActivity.Items.StartScanManual.getValue(), false);
            }

            buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex] + "Fail");
            setCheckedResult(mItemIndex, false);
        }

        ButtonComplete.setVisibility(View.VISIBLE);
        ButtonComplete.requestFocus();
        Log.d(TAG, "ScanEnd: BK1");//eric lin test
        showAllScanResult();
        Log.d(TAG, "ScanEnd: BK2");//eric lin test


        if(tvMessage.getTotalRadioNumber() == 0 && tvMessage.getTotalTVNumber() == 0)
        {
            Log.d(TAG, "ScanEnd: BK3");//eric lin test
            new MessageDialog(this, 0) {
                public void onSetMessage(View v) {
                    ((TextView) v).setText(getString(R.string.STR_NO_CHANNELS_FOUND));
                }

                public void onSetNegativeButton() {
                }

                public void onSetPositiveButton(int status) {
                }
                public void dialogEnd(int status) {
                }
            };
        }
    }
    private void FinishScanResult(boolean SearchOK)
    {
        Intent intent = new Intent();
        Bundle bundleBack = new Bundle();
        if(SearchOK)
            bundleBack.putString("searchresult", "searchOK");
        else
            bundleBack.putString("searchresult", "searchFAIL");

        intent.putExtras(bundleBack);
        setResult(RESULT_OK, intent);
        //ScanResultActivity.this.finish();
    }

    public void showRealtimeScanResult(int tpId, int serviceType){
        int i=0;
        ArrayList<TestISDBTScanActivity.serviceInfo> tmplist=null;
        StringBuilder zText = new StringBuilder ();

        if(serviceType == ProgramInfo.ALL_TV_TYPE) {
            tmplist = tvlist;
            fillString(zText, "Tv:\n");
        }
        else if(serviceType == ProgramInfo.ALL_RADIO_TYPE) {
            tmplist = radiolist;
            fillString(zText, "Radio:\n");
        }
        else
            return;

        if (tmplist != null) {
            for (i = 0; i < tmplist.size(); i++) {
                if(tpId == tmplist.get(i).getTpId()) {
                    if (tmplist.get(i).getCAflag() == 1)
                        fillString(zText, String.format("$[" + tmplist.get(i).getChNum() + "]" + tmplist.get(i).getName() + "\n"));
                    else
                        fillString(zText, String.format("  [" + tmplist.get(i).getChNum() + "]" + tmplist.get(i).getName() + "\n"));
                }
            }
        }
        if(serviceType == ProgramInfo.ALL_TV_TYPE)
            TvScanTvResult.setText(zText);
        else if(serviceType == ProgramInfo.ALL_RADIO_TYPE)
            TvScanRadioResult.setText(zText);
    }

    public void showAllScanResult(){
        int i=0;
        StringBuilder zText = new StringBuilder ();
        //StringBuilder zText = new StringBuilder ();
        int preTpId = -1;
        int TpId = -1;

        //Log.d(TAG, "showAllScanResult: BK1");//eric lin test
        //Show Tv result
        Log.d(TAG, "showAllScanResult: BK0 tpList.size()"+tpList.size());//eric lin test
        fillString(zText, "Tv:\n");
        if (tvlist != null) {
            for (i = 0; i < tvlist.size(); i++) {
                TpId = tvlist.get(i).getTpId();
                Log.d(TAG, "showAllScanResult: BK1 TpId="+TpId);//eric lin test
                if(scanMode == 0) {//auto
                    if (preTpId != TpId) {
                        if (preTpId != -1)
                            fillString(zText, "\n");
                        Log.d(TAG, "showAllScanResult: BK1_1");//eric lin test
                        Log.d(TAG, "showAllScanResult: BK1_1 i=" + i + "TpId:" + TpId);//eric lin test
                        Log.d(TAG, "showAllScanResult: BK1_1 i=" + i + "Frequency:" + TpInfoGet(TpId).TerrTp.getFreq());//eric lin test

                        fillString(zText, String.format("TpId:" + TpId + ", Frequency:" + TpInfoGet(TpId).TerrTp.getFreq() + "\n=>"));
                        //fillString(zText, String.format("TpId:" + tvlist.get(i).getTpId() + ", Frequency:" + tpList.get(TpId).TerrTp.getFreq() +"\n=>"));
                        Log.d(TAG, "showAllScanResult: BK1_2");//eric lin test
                        preTpId = TpId;
                    }
                }
                if(tvlist.get(i).getCAflag()==1) {
                    Log.d(TAG, "showAllScanResult: BK1_3");//eric lin test
                    fillString(zText, String.format("$[" + tvlist.get(i).getChNum() + "]" + tvlist.get(i).getName() + ", "));
                    Log.d(TAG, "showAllScanResult: BK1_4");//eric lin test
                }
                else {
                    Log.d(TAG, "showAllScanResult: BK1_5");//eric lin test
                    fillString(zText, String.format("  [" + tvlist.get(i).getChNum() + "]" + tvlist.get(i).getName() + ", "));//"\n"));
                    Log.d(TAG, "showAllScanResult: BK1_6");//eric lin test
                }
            }
        }
        TvScanTvResult.setText(zText);

        Log.d(TAG, "showAllScanResult: BK2");//eric lin test
        zText.setLength(0);//clear
        preTpId = -1;
        //Show Radio result
        fillString(zText, "Radio:\n");
        if (radiolist != null) {
            for (i = 0; i < radiolist.size(); i++) {
                TpId = radiolist.get(i).getTpId();
                if(scanMode == 0) {//auto
                    if (preTpId != TpId) {
                        if (preTpId != -1)
                            fillString(zText, "\n");
                        fillString(zText, String.format("TpId:" + TpId + ", Frequency:" + TpInfoGet(TpId).TerrTp.getFreq() + "\n=>"));
                        preTpId = TpId;
                    }
                }
                if(radiolist.get(i).getCAflag()==1)
                    fillString(zText, String.format("$[" + radiolist.get(i).getChNum() + "]" + radiolist.get(i).getName() + ", "));//"\n"));
                else
                    fillString(zText, String.format("  [" + radiolist.get(i).getChNum() + "]" + radiolist.get(i).getName() + ", "));//"\n"));
            }
        }
        Log.d(TAG, "showAllScanResult: BK3");//eric lin test
        TvScanRadioResult.setText(zText);

    }

    public void clearScanResult(){
        if(tvlist != null)
            tvlist.clear();
        if(radiolist != null)
            radiolist.clear();
        TvScanTvResult.setText("Tv:\n");
        TvScanRadioResult.setText("Radio:\n");
    }

    public void clearUITvRadioList(){
        TvScanTvResult.setText("Tv:\n");
        TvScanRadioResult.setText("Radio:\n");
    }

    public void BtnTestISDBTStartScanManual_OnClick(View view){
        //ScanParamsStartScan(int tunerId, int tpId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta)
        Log.d(TAG,"BtnTestISDBTStartScan_OnClick:");
        int itemIndex = TestISDBTScanActivity.Items.StartScanManual.getValue();
        mItemIndex = itemIndex;
        //StringBuilder zText = new StringBuilder ();
        int TestTpId = 19;
        scanMode=1; // 0 : auto   1 : manual
        searchOptionCaFta = 0; //0:All; 1:Scramble; 2:FTA
        searchOptionTVRadio = 0; //0:All; 1:TV; 2:Radio
        //String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        String[] str_bandwidth_list = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);
        TextView tv;


        mTotalTvNumber = 0;
        mTotalRadioNumber = 0;
        ButtonComplete.setVisibility(View.INVISIBLE);


        //show
        if(scanMode == 0) {//auto
            testviews.get(TestISDBTScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TpId.getValue()]);
            testviews.get(TestISDBTScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Frequency.getValue()]);
            testviews.get(TestISDBTScanActivity.Textviews.Bandwidth.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Bandwidth.getValue()]);
        }
        else if(scanMode == 1) {//manual
            tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
            Frequency = TpInfoGet(TestTpId).TerrTp.getFreq(); //tpList.get(TestTpId).TerrTp.getFreq();
            Bandwidth = TpInfoGet(TestTpId).TerrTp.getBand(); //tpList.get(TestTpId).TerrTp.getBand();
            Log.d(TAG, "BtnTestISDBTStartScan_OnClick: tpID=" + tpID + ", Frequency=" + Frequency + ", Bandwidth=" + Bandwidth);
            testviews.get(TestISDBTScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TpId.getValue()] + tpID);
            testviews.get(TestISDBTScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Frequency.getValue()] + Frequency);
            testviews.get(TestISDBTScanActivity.Textviews.Bandwidth.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Bandwidth.getValue()] + str_bandwidth_list[Bandwidth]);

        }
        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()]);
        testviews.get(TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()]);
        testviews.get(TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        CheckSignalHandler = new Handler();
        CheckSignalHandler.post(CheckStatusRunnable);
        Log.d(TAG, "BtnTestISDBTStartScan_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);
    }

    public void BtnTestISDBTStartScanAuto_OnClick(View view){
        //ScanParamsStartScan(int tunerId, int tpId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta)
        Log.d(TAG,"BtnTestISDBTStartScan_OnClick:");
        int itemIndex = TestISDBTScanActivity.Items.StartScanAuto.getValue();
        mItemIndex = itemIndex;
        //StringBuilder zText = new StringBuilder ();
        int TestTpId = 19;
        scanMode=0; // 0 : auto   1 : manual
        searchOptionCaFta = 0; //0:All; 1:Scramble; 2:FTA
        searchOptionTVRadio = 0; //0:All; 1:TV; 2:Radio
        //String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        String[] str_bandwidth_list = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);
        TextView tv;


        mTotalTvNumber = 0;
        mTotalRadioNumber = 0;
        ButtonComplete.setVisibility(View.INVISIBLE);


        //show
        if(scanMode == 0) {//auto
            testviews.get(TestISDBTScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TpId.getValue()]);
            testviews.get(TestISDBTScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Frequency.getValue()]);
            testviews.get(TestISDBTScanActivity.Textviews.Bandwidth.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Bandwidth.getValue()]);
        }
        else if(scanMode == 1) {//manual
            tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
            Frequency = TpInfoGet(TestTpId).TerrTp.getFreq(); //tpList.get(TestTpId).TerrTp.getFreq();
            Bandwidth = TpInfoGet(TestTpId).TerrTp.getBand(); //tpList.get(TestTpId).TerrTp.getBand();
            Log.d(TAG, "BtnTestISDBTStartScan_OnClick: tpID=" + tpID + ", Frequency=" + Frequency + ", Bandwidth=" + Bandwidth);
            testviews.get(TestISDBTScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TpId.getValue()] + tpID);
            testviews.get(TestISDBTScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Frequency.getValue()] + Frequency);
            testviews.get(TestISDBTScanActivity.Textviews.Bandwidth.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.Bandwidth.getValue()] + str_bandwidth_list[Bandwidth]);

        }
        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()]);
        testviews.get(TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()]);
        testviews.get(TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestISDBTScanActivity.Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        CheckSignalHandler = new Handler();
        CheckSignalHandler.post(CheckStatusRunnable);
        Log.d(TAG, "BtnTestISDBTStartScan_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);
    }




    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }
}
