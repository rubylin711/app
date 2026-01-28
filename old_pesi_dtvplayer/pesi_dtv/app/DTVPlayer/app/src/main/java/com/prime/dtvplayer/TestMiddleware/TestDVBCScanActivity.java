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
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.utils.TVMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDVBCScanActivity extends DTVActivity {
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
            for (TestDVBCScanActivity.Items testItem : TestDVBCScanActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestDVBCScanActivity.Items valueOf(int testItem) {
            return (TestDVBCScanActivity.Items)map.get(testItem);
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
        TvTotalCnt(7),
        RadioTotalCnt(8),
        BER(9),
        SNR(10),
        Max(11);
        private int value;
        private static Map map = new HashMap<>();
        private Textviews(int value) {
            this.value = value;
        }
        static {
            for (TestDVBCScanActivity.Textviews tv : TestDVBCScanActivity.Textviews.values()) {
                map.put(tv.value, tv);
            }
        }
        public static TestDVBCScanActivity.Textviews valueOf(int tv) {
            return (TestDVBCScanActivity.Textviews)map.get(tv);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private List<TextView> testviews;
    private static final int[] BUTTON_IDS = {
            R.id.buttonTestDvbcScanStartScanManual,
            R.id.buttonTestDvbcScanStartScanAuto,
            //R.id.buttonTestStopScan,
            R.id.buttonTestDvbcScanComplete,
            R.id.buttonTestDvbcScanCancel,
    };
    private static final int[] TEXTVIEW_IDS = {
            R.id.textViewTestDvbcScanTpId,
            R.id.textViewTestDvbcScanFrequency,
            R.id.textViewTestDvbcScanSymbolRate,
            R.id.textViewTestDvbcScanQam,
            R.id.textViewTestDvbcScanLockStatus,
            R.id.textViewTestDvbcScanStrength,
            R.id.textViewTestDvbcScanQuality,
            R.id.textViewTestDvbcScanTotalTvCnt,
            R.id.textViewTestDvbcScanTotalRadioCnt,
            R.id.textViewTestDvbcScanBER,
            R.id.textViewTestDvbcScanSNR,
    };
    private static final String[] TEXTVIEW_STRING = {
            "TpId: ",
            "Frequency: ",
            "SymbolRate: ",
            "QAM: ",
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
    private static final int TESTITEM_NUM = TestDVBCScanActivity.Items.Max.getValue();//
    private static final int TEXTVIEW_NUM = TestDVBCScanActivity.Textviews.Max.getValue();
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    int mTestResult=0;//
    private TextView TvScanTvResult=null, TvScanRadioResult=null;
    Handler CheckSignalHandler;
    private List<TpInfo> tpList = new ArrayList<>();
    int Frequency=0;
    int Symbolrate =0;
    int Qam=0;
    int tpID=0;
    int scanMode=0;
    int searchOptionCaFta = 0;
    int searchOptionTVRadio = 0;
    int SearchCount = -1;
    private ArrayList<serviceInfo> tvlist=null;
    private ArrayList<serviceInfo> radiolist=null;
    private Button ButtonCancel;
    private Button ButtonComplete;
    int mTotalTvNumber;
    int mTotalRadioNumber;
    int mItemIndex;
    private TpInfo mTpInfo = null;
    private SatInfo mSatInfo = null;
    private int mTpId = 0;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dvbcscan);

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
        for(int i = TestDVBCScanActivity.Items.StartScanManual.getValue(); i< TestDVBCScanActivity.Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        for(int i = TestDVBCScanActivity.Textviews.TpId.getValue(); i< TestDVBCScanActivity.Textviews.Max.getValue(); i++){
            TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
            testviews.add(tv);
            testviews.get(i).setText(TEXTVIEW_STRING[i]);
        }

        //TextView
        TvScanTvResult = (TextView)this.findViewById(R.id.textViewTestDvbcScanTvResult);
        TvScanTvResult.setText("Tv:");
        TvScanTvResult.setMovementMethod(new ScrollingMovementMethod());
        TvScanTvResult.scrollTo(0,0);

        //TextView
        TvScanRadioResult = (TextView)this.findViewById(R.id.textViewTestDvbcScanRadioResult);
        TvScanRadioResult.setText("Radio:");
        TvScanRadioResult.setMovementMethod(new ScrollingMovementMethod());
        TvScanRadioResult.scrollTo(0,0);

        if(tpList==null)
            tpList = new ArrayList<TpInfo>();
        tpList = TpInfoGetList(TpInfo.DVBC);

        if(tvlist==null)
            tvlist = new ArrayList<serviceInfo>();
        if(radiolist==null)
            radiolist = new ArrayList<serviceInfo>();

        // cancel & View Button
        Log.d(TAG, "onCreate: BK1");
        ButtonCancel = (Button)this.findViewById(R.id.buttonTestDvbcScanCancel);
        Log.d(TAG, "onCreate: BK2");
        ButtonComplete = (Button)this.findViewById(R.id.buttonTestDvbcScanComplete);
        Log.d(TAG, "onCreate: BK3");

        ButtonCancel.setOnClickListener(CancelListener);
        Log.d(TAG, "onCreate: BK4");
        ButtonComplete.setOnClickListener(CompleteListener);
        Log.d(TAG, "onCreate: BK5");
        ButtonComplete.setVisibility(View.INVISIBLE);
        Log.d(TAG, "onCreate: BK_end");


        mTpInfo = TpInfoGet(tpList.get(0).getTpId());
        mSatInfo = SatInfoGet(mTpInfo.getSatId());
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
        Log.d(TAG, "onMessage: type="+tvMessage.getMsgType());//eric lin test
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_SCAN_BEGIN :
            {
                Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_BEGIN!");
            }break;
            case TVMessage.TYPE_SCAN_SERCHTP:
            {
                Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_SERCHTP!");
                UpdateProgress(tvMessage);//eric lin test, mark
            }break;
            case TVMessage.TYPE_SCAN_PROCESS:
            {
                Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_PROCESS!");
                UpdateStatus(tvMessage);
            }break;
            case TVMessage.TYPE_SCAN_SCHEDULE:
            {
                Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_SCHEDULE!");
                //UpdateProgressBar(tvMessage);//eric lin test, mark
            }break;
            case TVMessage.TYPE_SCAN_END:
            {
                Log.d(TAG, "onMessage:     TVMessage.TYPE_SCAN_END");
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
        String ber;
        int snr = 0;

        lock = TunerGetLockStatus(0);
        Quality = TunerGetQuality(0);
        Strength = TunerGetStrength(0);
        ber = TunerGetBER(0);
        snr = TunerGetSNR(0);

        testviews.get(TestDVBCScanActivity.Textviews.LockStatus.getValue()).setText(TEXTVIEW_STRING[TestDVBCScanActivity.Textviews.LockStatus.getValue()]+lock);
        testviews.get(TestDVBCScanActivity.Textviews.Strength.getValue()).setText(TEXTVIEW_STRING[TestDVBCScanActivity.Textviews.Strength.getValue()]+Strength);
        testviews.get(TestDVBCScanActivity.Textviews.Quality.getValue()).setText(TEXTVIEW_STRING[TestDVBCScanActivity.Textviews.Quality.getValue()]+Quality);
        testviews.get(TestDVBCScanActivity.Textviews.BER.getValue()).setText(TEXTVIEW_STRING[TestDVBCScanActivity.Textviews.BER.getValue()]+ber);
        testviews.get(TestDVBCScanActivity.Textviews.SNR.getValue()).setText(TEXTVIEW_STRING[TestDVBCScanActivity.Textviews.SNR.getValue()]+snr);
    }

    private View.OnClickListener CancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "CancelListener: BK1");
            int itemIndex = TestDVBCScanActivity.Items.Cancel.getValue();
            //mItemIndex = itemIndex;

            //before stop scan            
            List<ProgramInfo> tvProgramList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            List<ProgramInfo> radioProgramList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
            int preTotalChCount=0, totalChCount=0;
            if(tvProgramList!=null)
                preTotalChCount += tvProgramList.size();
            if(radioProgramList!=null)
                preTotalChCount += radioProgramList.size();

            //stop scan
            ScanParamsStopScan(false);

            //after stop scan 
            tvProgramList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            radioProgramList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);

            if(tvProgramList!=null)
                totalChCount += tvProgramList.size();
            if(radioProgramList!=null)
                totalChCount += radioProgramList.size();

            Log.d(TAG, "TestDVBCScan-CancelListener: preTotalChCount="+preTotalChCount+", totalChCount="+totalChCount);
            if(preTotalChCount == totalChCount){
                buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
                setCheckedResult(itemIndex, true);
            }else{
                buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
                setCheckedResult(itemIndex, false);
            }

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
            int itemIndex = TestDVBCScanActivity.Items.Complete.getValue();
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
            if(ScanResultSetChannel() == 1)//there are some programs
                SetChannelExist(1);//1 : can go to normal view ; 0 : can't go to normal view
            else {//there is no program
                SetChannelExist(0);
            }

            //--start
            if(GetChannelExist()==1) {

                //ViewUiDisplayInit();
                ViewUiDisplay viewUiDisplay = GetViewUiDisplay();
                AvControlOpen(ViewHistory.getPlayId());

                // Johnny add 20180524 for setting ratio conversion, need to be called after AvControlOpen
                AvControlChangeRatioConversion(ViewHistory.getPlayId(), GetRatioByIndex(GposInfoGet().getScreen16x9()), GposInfoGet().getConversion());

                viewUiDisplay.ChangeProgram();
            }
            //--end
            //SaveTable(EnTableType.ALL);//eric lin test
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
        String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        Log.d(TAG, "UpdateStatus:     tvMessage.getAlreadyScanedTpNum() = " + tvMessage.getAlreadyScanedTpNum());
        //Log.d(TAG, "UpdateStatus:   TVMessage.TYPE_SCAN_PROCESS!   service id = " + tvMessage.getServiceId());

//eric lin, mark
//        if(scanMode == 0 && SearchCount != tvMessage.getAlreadyScanedTpNum()) {
//            SearchCount = tvMessage.getAlreadyScanedTpNum();
//            // SearchCount  : 1 ~
//            if(SearchCount <= tpList.size() &&
//                    ( tpList.get(SearchCount - 1).CableTp.getFreq() != Frequency) )
//            {
//                Log.d(TAG, "UpdateStatus:   Frequency = " + Frequency + "      table freq = " +  tpList.get(SearchCount - 1).CableTp.getFreq() );
//                Frequency = tpList.get(SearchCount - 1).CableTp.getFreq();
//                Symbolrate = tpList.get(SearchCount - 1).CableTp.getSymbol();
//                //str = getString(R.string.STR_FREQUENCY) + " : " + Frequency + getString(R.string.STR_KHZ) + "     Symbolrate : " + Symbolrate + getString(R.string.STR_KSPS);
//                //TextviewInfo.setText(str);
//                Qam = tpList.get(SearchCount - 1).CableTp.getQam();
//                testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()]+(SearchCount - 1));
//                testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()]+Frequency);
//                testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()]+Symbolrate);
//                testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()]+str_qam_list[Qam]);
//            }
//        }

        if(tvMessage.getServiceId()!=0) {
            TestDVBCScanActivity.serviceInfo ch = new TestDVBCScanActivity.serviceInfo();
            ch.setName(tvMessage.getChannelName());
            ch.setChLCN(tvMessage.getchannelLCN());
            ch.setCAflag(tvMessage.getCAFlag());
            ch.setServiceType(tvMessage.getserviceType());
            //if(scanMode == 0) {//auto
            ch.setTpId(mTpId);//ch.setTpId(tvMessage.getTpId());//eric lin 20180117 modify
                Log.d(TAG, "UpdateStatus: scan:auto--setTpId="+ch.getTpId()+", tvM.tpId="+tvMessage.getTpId()+", mTpId="+mTpId);
           // }

            //Log.d(TAG, "UpdateStatus:   Name = " + ch.getName() + "   CA Flag = " + ch.getCAflag());
            if(tvMessage.getserviceType() == ProgramInfo.ALL_TV_TYPE)
                Log.d(TAG, "UpdateStatus: TV lcn="+tvMessage.getchannelLCN()+", name="+tvMessage.getChannelName());
            else //(tvMessage.getserviceType() == ProgramInfo.ALL_RADIO_TYPE)
                Log.d(TAG, "UpdateStatus: radio lcn="+tvMessage.getchannelLCN());

            if (ch.getServiceType() == ProgramInfo.ALL_TV_TYPE) {
                tvlist.add(ch);
                //mTvListAdapter.notifyDataSetChanged();
                //ListviewTV.setSelection(tvlist.size() -1);
                showRealtimeScanResult(ch.getTpId(), ProgramInfo.ALL_TV_TYPE); //showScanResult(ProgramInfo.ALL_TV_TYPE);
            }
            else {
                radiolist.add(ch);
                //mRadioListAdapter.notifyDataSetChanged();
                //ListviewRadio.setSelection(radiolist.size() -1);
                showRealtimeScanResult(ch.getTpId(), ProgramInfo.ALL_RADIO_TYPE); //showScanResult(ProgramInfo.ALL_RADIO_TYPE);
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
            Log.d(TAG, "ScanEnd:   BK1");
            if(scanMode == 0) {//auto
                buttons.get(TestDVBCScanActivity.Items.StartScanAuto.getValue()).setText(ITEM_NAME[TestDVBCScanActivity.Items.StartScanAuto.getValue()] + "Pass");
                setCheckedResult(TestDVBCScanActivity.Items.StartScanAuto.getValue(), true);
            }else if(scanMode == 1) {//manual
                buttons.get(TestDVBCScanActivity.Items.StartScanManual.getValue()).setText(ITEM_NAME[TestDVBCScanActivity.Items.StartScanManual.getValue()] + "Pass");
                setCheckedResult(TestDVBCScanActivity.Items.StartScanManual.getValue(), true);
            }

            if(mItemIndex == TestDVBCScanActivity.Items.Complete.getValue()
                    || mItemIndex == TestDVBCScanActivity.Items.StartScanManual.getValue()
                    ||mItemIndex == TestDVBCScanActivity.Items.StartScanAuto.getValue()){
            buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex] + "Pass");
            setCheckedResult(mItemIndex, true);
            }

            //show Tv/Radio count
            tv = (TextView)this.findViewById(TEXTVIEW_IDS[Textviews.TvTotalCnt.getValue()]);
            testviews.get(Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[Textviews.TvTotalCnt.getValue()]+mTotalTvNumber);
            tv = (TextView)this.findViewById(TEXTVIEW_IDS[Textviews.RadioTotalCnt.getValue()]);
            testviews.get(Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[Textviews.RadioTotalCnt.getValue()]+mTotalRadioNumber);

        } else {
            if(scanMode == 0) {//auto
                buttons.get(Items.StartScanAuto.getValue()).setText(ITEM_NAME[Items.StartScanAuto.getValue()] + "Fail");
                setCheckedResult(Items.StartScanAuto.getValue(), false);
            }else if(scanMode == 1) {//manual
                buttons.get(Items.StartScanManual.getValue()).setText(ITEM_NAME[Items.StartScanManual.getValue()] + "Fail");
                setCheckedResult(Items.StartScanManual.getValue(), false);
            }

            if(mItemIndex == TestDVBCScanActivity.Items.Complete.getValue()
                    || mItemIndex == TestDVBCScanActivity.Items.StartScanManual.getValue()
                    ||mItemIndex == TestDVBCScanActivity.Items.StartScanAuto.getValue()){
            buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex] + "Fail");
            setCheckedResult(mItemIndex, false);
        }
        }

        Log.d(TAG, "ScanEnd:   BK2");
        ButtonComplete.setVisibility(View.VISIBLE);
        Log.d(TAG, "ScanEnd:   BK3");
        ButtonComplete.requestFocus();
        Log.d(TAG, "ScanEnd:   BK4");
        showAllScanResult();
        Log.d(TAG, "ScanEnd:   BK5");



        if(tvMessage.getTotalRadioNumber() == 0 && tvMessage.getTotalTVNumber() == 0)
        {
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
        ArrayList<serviceInfo> tmplist=null;
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
        int tpId = -1;



        List<TpInfo> tpList = TpInfoGetList(getTunerType());


        //Show Tv result
        fillString(zText, "Tv:\n");
        if (tvlist != null) {
            for (i = 0; i < tvlist.size(); i++) {
//                TpIndex = tvlist.get(i).getTpId();//eric lin mark
//                TpId = tpList.get(TpIndex).getTpId();//eric lin mark
                tpId = tvlist.get(i).getTpId();
                //Log.d(TAG, "showAllScanResult: TpId="+TpId);//eric lin test
                if(scanMode == 0) {//auto
                    if(preTpId != tpId) {
                        if(preTpId != -1)
                        fillString(zText, "\n");
                        fillString(zText, String.format("TpId:" + tpId + ", Frequency:" + TpInfoGet(tpId).CableTp.getFreq() +"\n=>"));//eric lin test, mark
                        //fillString(zText, String.format("TpId:" + TpId + ", Frequency:" + TpInfoGet(TpId).CableTp.getFreq() +"\n=>"));//eric lin test, mark
                        preTpId = tpId;
                }
                }
                if(tvlist.get(i).getCAflag()==1)
                    fillString(zText, String.format("$[" + tvlist.get(i).getChNum() + "]" + tvlist.get(i).getName() + ", "));
                else
                    fillString(zText, String.format("  [" + tvlist.get(i).getChNum() + "]" + tvlist.get(i).getName() + ", "));//"\n"));
            }
        }
        TvScanTvResult.setText(zText);

        zText.setLength(0);//clear
        preTpId = -1;
        //Show Radio result
        fillString(zText, "Radio:\n");
        if (radiolist != null) {
            for (i = 0; i < radiolist.size(); i++) {
//                TpIndex = radiolist.get(i).getTpId();
//                TpId = tpList.get(TpIndex).getTpId();
                tpId = radiolist.get(i).getTpId();
                if(scanMode == 0) {//auto
                    if(preTpId != tpId) {
                        if(preTpId != -1)
                        fillString(zText, "\n");
                    fillString(zText, String.format("TpId:" + tpId + ", Frequency:" + TpInfoGet(tpId).CableTp.getFreq() +"\n=>"));
                        preTpId = tpId;
                }
                }
                if(radiolist.get(i).getCAflag()==1)
                    fillString(zText, String.format("$[" + radiolist.get(i).getChNum() + "]" + radiolist.get(i).getName() + ", "));//"\n"));
                else
                    fillString(zText, String.format("  [" + radiolist.get(i).getChNum() + "]" + radiolist.get(i).getName() + ", "));//"\n"));
            }
        }
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

    public void BtnTestStartScanManual_OnClick(View view){
        //ScanParamsStartScan(int tunerId, int tpId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta)
        Log.d(TAG,"BtnTestStartScan_OnClick:");
        int itemIndex = Items.StartScanManual.getValue();
        mItemIndex = itemIndex;
        //StringBuilder zText = new StringBuilder ();
        int TestTpIndex = 50;
        int TestTpId = 10;
        scanMode=1; // 0 : auto   1 : manual
        searchOptionCaFta = 0; //0:All; 1:Scramble; 2:FTA
        searchOptionTVRadio = 0; //0:All; 1:TV; 2:Radio
        String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        TextView tv;


        mTotalTvNumber = 0;
        mTotalRadioNumber = 0;
        ButtonComplete.setVisibility(View.INVISIBLE);

        //eric lin test
        List<TpInfo> tpList = TpInfoGetList(TpInfo.DVBC);
        /*
        if(tpList !=null){
            for(int i=0; i<tpList.size(); i++) {
                Log.d(TAG, " BtnTestStartScan: i="+i+", tpId="+tpList.get(i).getTpId()+", freq="+tpList.get(i).CableTp.getFreq());
            }
        }else
            Log.d(TAG, " BtnTestStartScan: tpList is null");
        */
        TestTpId = tpList.get(TestTpIndex).getTpId();


        //show
        if(scanMode == 0) {//auto
            testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()]);
            testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()]);
            testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()]);
            testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()]);
        }
        else if(scanMode == 1) {//manual
            tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
            Frequency = TpInfoGet(TestTpId).CableTp.getFreq(); //tpList.get(TestTpId).CableTp.getFreq();
            Symbolrate = TpInfoGet(TestTpId).CableTp.getSymbol(); //tpList.get(TestTpId).CableTp.getSymbol();
            Qam = TpInfoGet(TestTpId).CableTp.getQam(); //tpList.get(TestTpId).CableTp.getQam();
            Log.d(TAG, "BtnTunerTuneDVBC_OnClick: tpID=" + tpID + ", Frequency=" + Frequency + ", Symbolrate=" + Symbolrate + ", Qam=" + Qam);
            //TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
            testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()] + tpID);
            testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()] + Frequency);
            testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()] + Symbolrate);
            testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()] + str_qam_list[Qam]);
        }
        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[Textviews.TvTotalCnt.getValue()]);
        testviews.get(Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[Textviews.RadioTotalCnt.getValue()]);
        testviews.get(Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        CheckSignalHandler = new Handler();
        CheckSignalHandler.post(CheckStatusRunnable);
        Log.d(TAG, "BtnTestStartScan_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);
    }

    public void BtnTestStartScanAuto_OnClick(View view){
        //ScanParamsStartScan(int tunerId, int tpId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta)
        Log.d(TAG,"BtnTestStartScan_OnClick:");
        int itemIndex = Items.StartScanAuto.getValue();
        mItemIndex = itemIndex;
        //StringBuilder zText = new StringBuilder ();
        int TestTpIndex=0;
        int TestTpId = 10;
        scanMode=0; // 0 : auto   1 : manual
        searchOptionCaFta = 0; //0:All; 1:Scramble; 2:FTA
        searchOptionTVRadio = 0; //0:All; 1:TV; 2:Radio
        String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        TextView tv;


        mTotalTvNumber = 0;
        mTotalRadioNumber = 0;
        ButtonComplete.setVisibility(View.INVISIBLE);

        List<TpInfo> tpList = TpInfoGetList(TpInfo.DVBC);
        TestTpId = tpList.get(TestTpIndex).getTpId();

        //show
        if(scanMode == 0) {//auto
            tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
            Log.d(TAG, "BtnTestStartScanAuto_OnClick: tpID="+tpID);
            testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()]);
            testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()]);
            testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()]);
            testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()]);
        }
        else if(scanMode == 1) {//manual
            tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
            Frequency = TpInfoGet(TestTpId).CableTp.getFreq(); //tpList.get(TestTpId).CableTp.getFreq();
            Symbolrate = TpInfoGet(TestTpId).CableTp.getSymbol(); //tpList.get(TestTpId).CableTp.getSymbol();
            Qam = TpInfoGet(TestTpId).CableTp.getQam(); //tpList.get(TestTpId).CableTp.getQam();
            Log.d(TAG, "BtnTunerTuneDVBC_OnClick: tpID=" + tpID + ", Frequency=" + Frequency + ", Symbolrate=" + Symbolrate + ", Qam=" + Qam);
            //TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
            testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()] + tpID);
            testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()] + Frequency);
            testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()] + Symbolrate);
            testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()] + str_qam_list[Qam]);
        }
        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[Textviews.TvTotalCnt.getValue()]);
        testviews.get(Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[Textviews.RadioTotalCnt.getValue()]);
        testviews.get(Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        CheckSignalHandler = new Handler();
        CheckSignalHandler.post(CheckStatusRunnable);
        Log.d(TAG, "BtnTestStartScan_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);

    }

    private void UpdateProgress(TVMessage tvMessage) {
//        int searchCount = tvMessage.getAlreadyScanedTpNum();
        String[] str_qam_list = getResources().getStringArray(R.array.STR_ARRAY_QAMLIST);
        int tpID = tvMessage.getTpId();
        mTpId = tpID;

        // cur searching tp & sat
        TpInfo tmpTpInfo = TpInfoGet(tpID);
        if (tmpTpInfo != null)
        {
            mTpInfo = tmpTpInfo;
    }

        mSatInfo = SatInfoGet(mTpInfo.getSatId());

        // set display tp & sat message
        //String str = GetDisplayMsg(mTpInfo, mSatInfo);
        //mTextviewInfo.setText(str);

        //---start
        Frequency = mTpInfo.CableTp.getFreq();
        Symbolrate = mTpInfo.CableTp.getSymbol();
        //str = getString(R.string.STR_FREQUENCY) + " : " + Frequency + getString(R.string.STR_KHZ) + "     Symbolrate : " + Symbolrate + getString(R.string.STR_KSPS);
        //TextviewInfo.setText(str);
        Qam = mTpInfo.CableTp.getQam();
        testviews.get(Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[Textviews.TpId.getValue()]+tpID);
        testviews.get(Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[Textviews.Frequency.getValue()]+Frequency);
        testviews.get(Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[Textviews.SymbolRate.getValue()]+Symbolrate);
        testviews.get(Textviews.QAM.getValue()).setText(TEXTVIEW_STRING[Textviews.QAM.getValue()]+str_qam_list[Qam]);
        //---end
    }

//    private String GetDisplayMsg(TpInfo tpInfo, SatInfo satInfo)
//    {
//        String str;
//
//        if(GetCurTunerType() == TpInfo.DVBC)
//        {
//            int freq = tpInfo.CableTp.getFreq();
//            int symbol = tpInfo.CableTp.getSymbol();
//
//            String strFreq = getString(R.string.STR_FREQUENCY) + " : " + freq + getString(R.string.STR_KHZ);
//            String strSymbol = getString(R.string.STR_SYMBOLRATE) + " : " + symbol + getString(R.string.STR_MSPS);
//
//            str = strFreq + "     " + strSymbol;
//        }
//        else if(GetCurTunerType() == TpInfo.ISDBT)
//        {
//            int freq = tpInfo.TerrTp.getFreq();
//            String band = mStrBandList[tpInfo.TerrTp.getBand()];
//
//            String strFreq = getString(R.string.STR_FREQUENCY) + " : " + freq + getString(R.string.STR_KHZ);
//            String strBand = getString(R.string.STR_ISDBT_BANDWIDTH) + " : " + band + getString(R.string.STR_KHZ);
//
//            str = strFreq + "     " + strBand;
//        }
//        else if(GetCurTunerType() == TpInfo.DVBT)//eric lin 20170108 add dvb-t channel search UI
//        {
//            int channel = tpInfo.TerrTp.getChannel();
//            int freq = tpInfo.TerrTp.getFreq();
//
//            String strChannel = getString(R.string.STR_CHANNEL) + " : " + channel;
//            String strFreq = freq + getString(R.string.STR_MHZ);
//
//            str = strChannel + "     " + strFreq;
//        }
//        else if(GetCurTunerType() == TpInfo.DVBS)
//        {
//            String name = satInfo.getSatName();
//            String angle = Float.toString(satInfo.getAngle());
//            String angleEW = satInfo.getAngleEW() == SatInfo.ANGLE_E ? "E" : "W";
//            String strSat = getString(R.string.STR_DVBS_SATELLITE) + " : " + name + " " + angle + angleEW;
//
//            String freq = Integer.toString(tpInfo.SatTp.getFreq());
//            String symbol = Integer.toString(tpInfo.SatTp.getSymbol());
//            String polar = tpInfo.SatTp.getPolar() == TpInfo.Sat.POLAR_H ? "H" : "V";
//            String strTp = getString(R.string.STR_DVBS_TP) + " : " + freq + " " + polar + " " + symbol;
//
//            str = strSat + "     " + strTp;
//        }
//        else
//        {
//            str = " Unknown Tuner Type";
//        }
//
//        return str;
//    }

    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }
}
