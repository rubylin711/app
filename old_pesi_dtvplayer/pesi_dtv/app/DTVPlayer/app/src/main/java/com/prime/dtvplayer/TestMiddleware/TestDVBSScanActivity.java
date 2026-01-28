package com.prime.dtvplayer.TestMiddleware;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class TestDVBSScanActivity extends DTVActivity {

    private String TAG=getClass().getSimpleName();
    private enum Items {
        StartScanManual(0),
        StartScanAuto(1),
        Complete(2),
        Cancel(3),
        StartScanAllSat(4),
        StartScanNetWork(5),
        Max(6);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestDVBSScanActivity.Items testItem : TestDVBSScanActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestDVBSScanActivity.Items valueOf(int testItem) {
            return (TestDVBSScanActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }

    private enum Textviews {
        TpId(0),
        Frequency(1),
        SymbolRate(2),
        DROT(3),
        LockStatus(4),
        Strength(5),
        Quality(6),
        TvTotalCnt(7),
        RadioTotalCnt(8),
        BER(9),
        SNR(10),
        SAT(11),
        FEC(12),
        POLAR(13),
        SPECT(14),
        Max(15);
        private int value;
        private static Map map = new HashMap<>();
        private Textviews(int value) {
            this.value = value;
        }
        static {
            for (TestDVBSScanActivity.Textviews tv : TestDVBSScanActivity.Textviews.values()) {
                map.put(tv.value, tv);
            }
        }
        public static TestDVBSScanActivity.Textviews valueOf(int tv) {
            return (TestDVBSScanActivity.Textviews)map.get(tv);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private List<TextView> testviews;
    private static final int[] BUTTON_IDS = {
            R.id.buttonTestSatScanStartScanManual,
            R.id.buttonTestSatScanStartScanAuto,
            R.id.buttonTestSatScanComplete,
            R.id.buttonTestSatScanCancel,
            R.id.buttonTestSatScanStartScanAllSat,
            R.id.buttonTestSatScanStartScanNetwork
    };
    private static final int[] TEXTVIEW_IDS = {
            R.id.textViewTestSatScanTpId,
            R.id.textViewTestSatScanFrequency,
            R.id.textViewTestSatScanSymbolRate,
            R.id.textViewTestSatScanDrot,
            R.id.textViewTestSatScanLockStatus,
            R.id.textViewTestSatScanStrength,
            R.id.textViewTestSatScanQuality,
            R.id.textViewTestSatScanTotalTvCnt,
            R.id.textViewTestSatScanTotalRadioCnt,
            R.id.textViewTestSatScanBER,
            R.id.textViewTestSatScanSNR,
            R.id.textViewTestSatScanSAT,
            R.id.textViewTestSatScanFEC,
            R.id.textViewTestSatScanPOLAR,
            R.id.textViewTestSatScanSpect,
    };
    private static final String[] TEXTVIEW_STRING = {
            "TpId: ",
            "Frequency: ",
            "SymbolRate: ",
            "Drot: ",
            "LockStatus: ",
            "Strength: ",
            "Quality: ",
            "Tv count: ",
            "Radio count: ",
            "BER: ",
            "SNR: ",
            "SAT: ",
            "FEC: ",
            "POLAR: ",
            "SPECT: "
    };
    private static final String[] ITEM_NAME = {
            "Manual Search\nScanParamsStartScan()\nResult:",
            "Auto Search\nScanParamsStartScan()\nResult:",
            "Complete\nScanParamsStopScan(true)\nResult:",
            "Cancel\nScanParamsStopScan(false)\nResult:",
            "All Sat Search\nScanParamsStartScan()\nResult:",
            "Network Search\nScanParamsStartScan()\nResult:",
    };

    TestMidMain tm = new TestMidMain();//
    private static final int TESTITEM_NUM = TestDVBSScanActivity.Items.Max.getValue();//
    private static final int TEXTVIEW_NUM = TestDVBSScanActivity.Textviews.Max.getValue();
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    int mTestResult=0;//
    private TextView TvScanTvResult=null, TvScanRadioResult=null;
    Handler CheckSignalHandler = null;

    private List<SatInfo> satList = null;
    private List<TpInfo> tpList = new ArrayList<>();
    int Frequency=0;
    int Symbolrate =0;
    int Fec = 0;
    int Polar = 0;
    int Drot = 0;
    int Spect = 0;
    String SatName;


    int tpID=0;
    int scanMode=0;
    int searchOptionCaFta = 0;
    int searchOptionTVRadio = 0;
    int SearchCount = -1;
    private ArrayList<TestDVBSScanActivity.serviceInfo> tvlist=null;
    private ArrayList<TestDVBSScanActivity.serviceInfo> radiolist=null;
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
        setContentView(R.layout.activity_test_sat_scan);

        //Buttons
        buttons = new ArrayList<Button>();
        testviews = new ArrayList<TextView>();

        for(int i = TestDVBSScanActivity.Items.StartScanManual.getValue(); i< TestDVBSScanActivity.Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        for(int i = TestDVBSScanActivity.Textviews.TpId.getValue(); i< TestDVBSScanActivity.Textviews.Max.getValue(); i++){
            TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
            testviews.add(tv);
            testviews.get(i).setText(TEXTVIEW_STRING[i]);
        }

        //TextView
        TvScanTvResult = (TextView)this.findViewById(R.id.textViewTestSatScanTvResult);
        TvScanTvResult.setText("Tv:");
        TvScanTvResult.setMovementMethod(new ScrollingMovementMethod());
        TvScanTvResult.scrollTo(0,0);

        //TextView
        TvScanRadioResult = (TextView)this.findViewById(R.id.textViewTestSatScanRadioResult);
        TvScanRadioResult.setText("Radio:");
        TvScanRadioResult.setMovementMethod(new ScrollingMovementMethod());
        TvScanRadioResult.scrollTo(0,0);


        satList = SatInfoGetList(TpInfo.DVBS);

        if(tpList==null)
            tpList = new ArrayList<TpInfo>();
        tpList = satList.isEmpty() ? null : TpInfoGetListBySatId(satList.get(0).getSatId());

        if(tvlist==null)
            tvlist = new ArrayList<TestDVBSScanActivity.serviceInfo>();
        if(radiolist==null)
            radiolist = new ArrayList<TestDVBSScanActivity.serviceInfo>();

        // cancel & View Button
        Log.d(TAG, "onCreate: BK1");
        ButtonCancel = (Button)this.findViewById(R.id.buttonTestSatScanCancel);
        Log.d(TAG, "onCreate: BK2");
        ButtonComplete = (Button)this.findViewById(R.id.buttonTestSatScanComplete);
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
        if(CheckSignalHandler != null)
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
        String SatName;

        lock = TunerGetLockStatus(0);
        Quality = TunerGetQuality(0);
        Strength = TunerGetStrength(0);
        ber = TunerGetBER(0);
        snr = TunerGetSNR(0);

        testviews.get(TestDVBSScanActivity.Textviews.LockStatus.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.LockStatus.getValue()]+lock);
        testviews.get(TestDVBSScanActivity.Textviews.Strength.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.Strength.getValue()]+Strength);
        testviews.get(TestDVBSScanActivity.Textviews.Quality.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.Quality.getValue()]+Quality);
        testviews.get(TestDVBSScanActivity.Textviews.BER.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.BER.getValue()]+ber);
        testviews.get(TestDVBSScanActivity.Textviews.SNR.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SNR.getValue()]+snr);
    }

    private View.OnClickListener CancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "CancelListener: BK1");
            int itemIndex = TestDVBSScanActivity.Items.Cancel.getValue();
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

            Log.d(TAG, "TestSatScan-CancelListener: preTotalChCount="+preTotalChCount+", totalChCount="+totalChCount);
            if(preTotalChCount == totalChCount){
                buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
                setCheckedResult(itemIndex, true);
            }else{
                buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
                setCheckedResult(itemIndex, false);
            }
        }
    };

    private View.OnClickListener CompleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "CompleteListener: BK1");
            int itemIndex = TestDVBSScanActivity.Items.Complete.getValue();
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

        if(tvMessage.getServiceId()!=0) {
            TestDVBSScanActivity.serviceInfo ch = new TestDVBSScanActivity.serviceInfo();
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
                buttons.get(TestDVBSScanActivity.Items.StartScanAuto.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanAuto.getValue()] + "Pass");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanAuto.getValue(), true);
            }else if(scanMode == 1) {//manual
                buttons.get(TestDVBSScanActivity.Items.StartScanManual.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanManual.getValue()] + "Pass");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanManual.getValue(), true);
            }
            else if(scanMode == 2) {// Network
                buttons.get(TestDVBSScanActivity.Items.StartScanNetWork.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanNetWork.getValue()] + "Pass");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanNetWork.getValue(), true);
            }
            else if(scanMode == 3) {// All Sat
                buttons.get(TestDVBSScanActivity.Items.StartScanAllSat.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanAllSat.getValue()] + "Pass");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanAllSat.getValue(), true);
            }

            if(mItemIndex == TestDVBSScanActivity.Items.Complete.getValue()
                    || mItemIndex == TestDVBSScanActivity.Items.StartScanManual.getValue()
                    ||mItemIndex == TestDVBSScanActivity.Items.StartScanAuto.getValue()){
                buttons.get(mItemIndex).setText(ITEM_NAME[mItemIndex] + "Pass");
                setCheckedResult(mItemIndex, true);
            }

            //show Tv/Radio count
            tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
            testviews.get(TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]+mTotalTvNumber);
            tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);
            testviews.get(TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]+mTotalRadioNumber);

        }
        else {
            if(scanMode == 0) {//auto
                buttons.get(TestDVBSScanActivity.Items.StartScanAuto.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanAuto.getValue()] + "Fail");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanAuto.getValue(), false);
            }else if(scanMode == 1) {//manual
                buttons.get(TestDVBSScanActivity.Items.StartScanManual.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanManual.getValue()] + "Fail");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanManual.getValue(), false);
            }
            else if(scanMode == 2) {// Network
                buttons.get(TestDVBSScanActivity.Items.StartScanNetWork.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanNetWork.getValue()] + "Fail");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanNetWork.getValue(), false);
            }
            else if(scanMode == 3) {// All Sat
                buttons.get(TestDVBSScanActivity.Items.StartScanAllSat.getValue()).setText(ITEM_NAME[TestDVBSScanActivity.Items.StartScanAllSat.getValue()] + "Fail");
                setCheckedResult(TestDVBSScanActivity.Items.StartScanAllSat.getValue(), false);
            }

            if(mItemIndex == TestDVBSScanActivity.Items.Complete.getValue()
                    || mItemIndex == TestDVBSScanActivity.Items.StartScanManual.getValue()
                    ||mItemIndex == TestDVBSScanActivity.Items.StartScanAuto.getValue()){
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
        ArrayList<TestDVBSScanActivity.serviceInfo> tmplist=null;
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
                        fillString(zText, String.format("TpId:" + tpId + ", Frequency:" + TpInfoGet(tpId).SatTp.getFreq() +"\n=>"));//eric lin test, mark
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
                        fillString(zText, String.format("TpId:" + tpId + ", Frequency:" + TpInfoGet(tpId).SatTp.getFreq() +"\n=>"));
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

    public void BtnTestDVBSStartScanManual_OnClick(View view){
        //ScanParamsStartScan(int tunerId, int tpId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta)
        Log.d(TAG,"BtnTestStartScan_OnClick:");
        int itemIndex = TestDVBSScanActivity.Items.StartScanManual.getValue();
        mItemIndex = itemIndex;
        int TestTpIndex = 0;
        int TestTpId = 10;
        scanMode=1; // 0 : auto   1 : manual
        searchOptionCaFta = 0; //0:All; 1:Scramble; 2:FTA
        searchOptionTVRadio = 0; //0:All; 1:TV; 2:Radio
        TextView tv;


        mTotalTvNumber = 0;
        mTotalRadioNumber = 0;
        ButtonComplete.setVisibility(View.INVISIBLE);

        List<TpInfo> tpList = TpInfoGetList(TpInfo.DVBS);
        TestTpId = tpList.get(TestTpIndex).getTpId();

        //show
        //manual
        tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
        mSatInfo = SatInfoGet(mTpInfo.getSatId());

        SatName = mSatInfo.getSatName();
        Frequency = TpInfoGet(TestTpId).SatTp.getFreq();
        Symbolrate = TpInfoGet(TestTpId).SatTp.getSymbol();
        Fec = TpInfoGet(TestTpId).SatTp.getFec();
        Polar = TpInfoGet(TestTpId).SatTp.getPolar();
        Drot =TpInfoGet(TestTpId).SatTp.getDrot();
        Spect = TpInfoGet(TestTpId).SatTp.getSpect();


        Log.d(TAG, "BtnTunerTuneDVBS_OnClick: tpID=" + tpID + ", Frequency=" + Frequency + ", Symbolrate=" + Symbolrate + ",Fec="+Fec + ",Polar="+Polar+",Drot="+Drot+",Spect="+Spect);
        //TextView tv = (TextView)this.findViewById(TEXTVIEW_IDS[i]);
        testviews.get(TestDVBSScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TpId.getValue()] + tpID);
        testviews.get(TestDVBSScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.Frequency.getValue()] + Frequency);
        testviews.get(TestDVBSScanActivity.Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SymbolRate.getValue()] + Symbolrate);

        testviews.get(TestDVBSScanActivity.Textviews.SAT.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SAT.getValue()] + SatName);
        testviews.get(TestDVBSScanActivity.Textviews.DROT.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.DROT.getValue()] + Drot);
        testviews.get(TestDVBSScanActivity.Textviews.FEC.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.FEC.getValue()] + Fec);
        testviews.get(TestDVBSScanActivity.Textviews.POLAR.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.POLAR.getValue()] + Polar);
        testviews.get(TestDVBSScanActivity.Textviews.SPECT.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SPECT.getValue()] + Spect);

        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        if(CheckSignalHandler == null) {
            CheckSignalHandler = new Handler();
            CheckSignalHandler.post(CheckStatusRunnable);
        }
        Log.d(TAG, "BtnTestStartScan_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);
    }

    public void BtnTestDVBSStartScanAuto_OnClick(View view){
        //ScanParamsStartScan(int tunerId, int tpId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta)
        Log.d(TAG,"BtnTestStartScan_OnClick:");
        int itemIndex = TestDVBSScanActivity.Items.StartScanAuto.getValue();
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

        List<TpInfo> tpList = TpInfoGetList(TpInfo.DVBS);
        TestTpId = tpList.get(TestTpIndex).getTpId();

        //show
        //auto
        tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
        Log.d(TAG, "BtnTestStartScanAuto_OnClick: tpID="+tpID);
        testviews.get(TestDVBSScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TpId.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.Frequency.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SymbolRate.getValue()]);

        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        if(CheckSignalHandler == null) {
            CheckSignalHandler = new Handler();
            CheckSignalHandler.post(CheckStatusRunnable);
        }
        Log.d(TAG, "BtnTestStartScan_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);
    }

    private void UpdateProgress(TVMessage tvMessage) {
//        int searchCount = tvMessage.getAlreadyScanedTpNum();
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
        Frequency = mTpInfo.SatTp.getFreq();
        Symbolrate = mTpInfo.SatTp.getSymbol();

        SatName = mSatInfo.getSatName();
        Frequency = TpInfoGet(mTpId).SatTp.getFreq();
        Symbolrate = TpInfoGet(mTpId).SatTp.getSymbol();
        Fec = TpInfoGet(mTpId).SatTp.getFec();
        Polar = TpInfoGet(mTpId).SatTp.getPolar();
        Drot =TpInfoGet(mTpId).SatTp.getDrot();
        Spect = TpInfoGet(mTpId).SatTp.getSpect();

        Log.d(TAG, "UpdateProgress: SatName = " + SatName +",tpID=" + tpID + ", Frequency=" + Frequency + ", Symbolrate=" + Symbolrate + ",Fec="+Fec + ",Polar="+Polar+",Drot="+Drot+",Spect="+Spect);


        //str = getString(R.string.STR_FREQUENCY) + " : " + Frequency + getString(R.string.STR_KHZ) + "     Symbolrate : " + Symbolrate + getString(R.string.STR_KSPS);
        //TextviewInfo.setText(str);
        testviews.get(TestDVBSScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TpId.getValue()]+tpID);
        testviews.get(TestDVBSScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.Frequency.getValue()]+Frequency);
        testviews.get(TestDVBSScanActivity.Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SymbolRate.getValue()]+Symbolrate);

        testviews.get(TestDVBSScanActivity.Textviews.SAT.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SAT.getValue()] + SatName);
        testviews.get(TestDVBSScanActivity.Textviews.DROT.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.DROT.getValue()] + Drot);
        testviews.get(TestDVBSScanActivity.Textviews.FEC.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.FEC.getValue()] + Fec);
        testviews.get(TestDVBSScanActivity.Textviews.POLAR.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.POLAR.getValue()] + Polar);
        testviews.get(TestDVBSScanActivity.Textviews.SPECT.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SPECT.getValue()] + Spect);
        //---end
    }

    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }

    public void BtnTestDVBSStartScanNetwok_OnClick(View view){
        Log.d(TAG,"BtnTestStartScan_OnClick:");
        int itemIndex = TestDVBSScanActivity.Items.StartScanAuto.getValue();
        mItemIndex = itemIndex;
        //StringBuilder zText = new StringBuilder ();
        int TestTpIndex=0;
        int TestTpId = 10;
        scanMode=2; // 0 : auto   1 : manual  2:network
        searchOptionCaFta = 0; //0:All; 1:Scramble; 2:FTA
        searchOptionTVRadio = 0; //0:All; 1:TV; 2:Radio
        TextView tv;


        mTotalTvNumber = 0;
        mTotalRadioNumber = 0;
        ButtonComplete.setVisibility(View.INVISIBLE);

        List<TpInfo> tpList = TpInfoGetList(TpInfo.DVBS);
        TestTpId = tpList.get(TestTpIndex).getTpId();

        //show
        //auto
        tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
        Log.d(TAG, "BtnTestDVBSStartScanNetwok_OnClick: tpID="+tpID);
        testviews.get(TestDVBSScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TpId.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.Frequency.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SymbolRate.getValue()]);

        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        if(CheckSignalHandler == null) {
            CheckSignalHandler = new Handler();
            CheckSignalHandler.post(CheckStatusRunnable);
        }
        Log.d(TAG, "BtnTestDVBSStartScanNetwok_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);
    }

    public void BtnTestDVBSStartScanAllSat_OnClick(View view){
        Log.d(TAG,"BtnTestStartScan_OnClick:");
        int itemIndex = TestDVBSScanActivity.Items.StartScanAuto.getValue();
        mItemIndex = itemIndex;
        //StringBuilder zText = new StringBuilder ();
        int TestTpIndex=0;
        int TestTpId = 10;
        scanMode=3; // 0 : auto   1 : manual  2:network 3: all Sat
        searchOptionCaFta = 0; //0:All; 1:Scramble; 2:FTA
        searchOptionTVRadio = 0; //0:All; 1:TV; 2:Radio
        TextView tv;


        mTotalTvNumber = 0;
        mTotalRadioNumber = 0;
        ButtonComplete.setVisibility(View.INVISIBLE);

        List<TpInfo> tpList = TpInfoGetList(TpInfo.DVBS);
        TestTpId = tpList.get(TestTpIndex).getTpId();

        //show
        //auto
        tpID = TpInfoGet(TestTpId).getTpId(); //tpList.get(TestTpId).getTpId();
        Log.d(TAG, "BtnTestDVBSStartScanNetwok_OnClick: tpID="+tpID);
        testviews.get(TestDVBSScanActivity.Textviews.TpId.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TpId.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.Frequency.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.Frequency.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.SymbolRate.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.SymbolRate.getValue()]);

        //clear Tv/Radio count
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.TvTotalCnt.getValue()]);
        tv = (TextView)this.findViewById(TEXTVIEW_IDS[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);
        testviews.get(TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()).setText(TEXTVIEW_STRING[TestDVBSScanActivity.Textviews.RadioTotalCnt.getValue()]);

        //monitor lock status/strength/quality
        if(CheckSignalHandler == null) {
            CheckSignalHandler = new Handler();
            CheckSignalHandler.post(CheckStatusRunnable);
        }
        Log.d(TAG, "BtnTestDVBSStartScanNetwok_OnClick: CheckSignalHandler post runnable" );

        clearScanResult();
        ScanParamsStartScan(0, tpID, 0, scanMode, searchOptionTVRadio, searchOptionCaFta, 0, 0);
    }
}
