package com.prime.dtvplayer.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.TvInput.ScanActivity;
import com.TvInput.TvInputActivity;
import com.dolphin.dtv.EnTableType;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.MessageDialog;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class ScanResultActivity extends DTVActivity {
    private final String TAG = "ScanResultActivity";
    private RecyclerView mListviewTV;
    private RecyclerView mListviewRadio;
    private Button mButtonComplete;
    private ProgressBar mProgressSearch;
    private ProgressBar mProgressStrength;
    private ProgressBar mProgressQuality;
    private TextView mTextviewInfo;
    private TextView mProgressValue;
    private TextView mBerValue;
    private ScanResultAdapter mTvListAdapter=null;
    private ScanResultAdapter mRadioListAdapter=null;
    private ArrayList<Object> mTvlist=null;
    private ArrayList<Object> mRadiolist=null;
    private TpInfo mTpInfo = null;
    private SatInfo mSatInfo = null;
    int mTpID=0;
    int mScanModeCaFta = 0;
    int mChannelType = 0;
    int mSearchMode=0;

    // for VMX need open/close -s
    int mStartTPID = 0; // connie 20180919 add for vmx search
    int mEndTPID = 0;
    int mTriggerID = 0;
    int mTriggerNum = 0;
    // for VMX need open/close -e

    // Edwin 20181129 add search fail message
    public static final int RESULT_SEARCH_FAIL = -1;

    // Johnny add for ISDBT 20180103
    int mNitSearch = 0;  // 0 = off, 1 = on
    int mSegChannel = 0; // 0 = off, 1 = on
    String[] mStrBandList = null;

    int mItemHeight;
    boolean mIsScanBegin = false;

    Handler mCheckSignalHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_result_layout);

        Bundle search_setting = this.getIntent().getExtras();
        assert search_setting != null;

        if(GetCurTunerType() == TpInfo.DVBC)
        {
            mTpID = search_setting.getInt(getString(R.string.STR_EXTRAS_TPID));
            mScanModeCaFta = search_setting.getInt(getString(R.string.STR_EXTRAS_SCANMODE));
            mChannelType = search_setting.getInt(getString(R.string.STR_EXTRAS_CHANNELTYPE));
            mSearchMode = search_setting.getInt(getString(R.string.STR_EXTRAS_SEARCHMODE));
            if(mSearchMode == TVScanParams.SCAN_MODE_NETWORK) {
                mNitSearch = 1;
                mSearchMode = TVScanParams.SCAN_MODE_MANUAL;
            }
            if(Pvcfg.getCAType() == Pvcfg.CA_VMX && mSearchMode == TVScanParams.SCAN_VMX_SEARCH) { // for VMX need open/close
                mStartTPID = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_START_TPID));
                mEndTPID = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_END_TPID));
                mTriggerID = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_ID));
                mTriggerNum = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_NUM));
            }
            mTpInfo = TpInfoGet(mTpID);
            mSatInfo = SatInfoGet(mTpInfo.getSatId());

            Log.d(TAG, "onCreate: freq = "+mTpInfo.CableTp.getFreq() + " symbol = " + mTpInfo.CableTp.getSymbol()+" qam = "+mTpInfo.CableTp.getQam());
            Log.d(TAG, "onCreate: ScanModeCaFta = "+ mScanModeCaFta + " Channel Type = " + mChannelType + " Search Mode = " + mSearchMode);
        }
        else if(GetCurTunerType() == TpInfo.ISDBT)
        {
            mTpID = search_setting.getInt("tp_id");
            mScanModeCaFta = search_setting.getInt("scan_mode");
            mNitSearch = search_setting.getInt("nit_search");
            mSegChannel = search_setting.getInt("1seg_channel");
            mSearchMode = search_setting.getInt("search_mode");

            if( Pvcfg.getCAType() == Pvcfg.CA_VMX && mSearchMode == TVScanParams.SCAN_VMX_SEARCH) { // for VMX need open/close
                mStartTPID = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_START_TPID));
                mEndTPID = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_END_TPID));
                mTriggerID = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_ID));
                mTriggerNum = search_setting.getInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_NUM));
            }

            mTpInfo = TpInfoGet(mTpID);
            mSatInfo = SatInfoGet(mTpInfo.getSatId());
            mStrBandList = getResources().getStringArray(R.array.STR_ISDBT_ARRAY_BANDWIDTH);

            Log.d(TAG, "onCreate: freq = "+mTpInfo.TerrTp.getFreq() + " band = " + mTpInfo.TerrTp.getBand());
            Log.d(TAG, "onCreate: NitSearch = " + mNitSearch + " 1SegChannel = " + mSegChannel);
            Log.d(TAG, "onCreate: ScanModeCaFta = "+ mScanModeCaFta + " Search Mode = " + mSearchMode);
        }
        else if(GetCurTunerType() == TpInfo.DVBT)//eric lin 20170108 add dvb-t channel search UI
        {
            mTpID = search_setting.getInt("tp_id");
            mScanModeCaFta = search_setting.getInt("scan_mode");
            mChannelType = search_setting.getInt("channel_type");
            mSearchMode = search_setting.getInt("search_mode");

            mTpInfo = TpInfoGet(mTpID);
            mSatInfo = SatInfoGet(mTpInfo.getSatId());

            Log.d(TAG, "onCreate: freq= "+ mTpInfo.TerrTp.getFreq() + " band = " + mTpInfo.TerrTp.getBand());
            Log.d(TAG, "onCreate: ScanModeCaFta = "+ mScanModeCaFta + " Channel Type = " + mChannelType + " Search Mode = " + mSearchMode);
        }
        else if(GetCurTunerType() == TpInfo.DVBS)
        {
            mTpID = search_setting.getInt("tp_id");
            mScanModeCaFta = search_setting.getInt("scan_mode");
            mChannelType = search_setting.getInt("channel_type");
            mSearchMode = search_setting.getInt("search_mode");

            mTpInfo = TpInfoGet(mTpID);
            mSatInfo = SatInfoGet(mTpInfo.getSatId());

            Log.d(TAG, "onCreate: SatID= "+ mSatInfo.getSatId() + " TpID = " + mTpInfo.getTpId());
            Log.d(TAG, "onCreate: freq= "+ mTpInfo.SatTp.getFreq() + " symbol = " + mTpInfo.SatTp.getSymbol() + " polar = " + mTpInfo.SatTp.getPolar());
            Log.d(TAG, "onCreate: ScanModeCaFta = "+ mScanModeCaFta + " Channel Type = " + mChannelType + " Search Mode = " + mSearchMode);
        }

        item_init();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mItemHeight = mListviewTV.getHeight();
        Log.d(TAG, "item_init: itemHeight = "+ mItemHeight);

        int itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

        int maxItemCount = calMaxItemCount();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                mListviewTV.getWidth(), itemHeight * maxItemCount);

        mListviewTV.setLayoutParams(layoutParams);
        mListviewRadio.setLayoutParams(layoutParams);
    }

    @Override
    public void onConnected() {
        super.onConnected();
        mCheckSignalHandler = new Handler();
        if(GetCurTunerType() == TpInfo.DVBC || GetCurTunerType() == TpInfo.DVBT)
        {

            if(Pvcfg.getCAType() == Pvcfg.CA_VMX && mSearchMode == TVScanParams.SCAN_VMX_SEARCH)// for VMX need open/close
                ScanParamsVMXStartScan(0, mStartTPID, 0, mSearchMode, mChannelType, mScanModeCaFta, 0, 0, mStartTPID, mEndTPID);
            else
                ScanParamsStartScan(0, mTpID, 0, mSearchMode, mChannelType, mScanModeCaFta, mNitSearch, 0);
        }
        else if(GetCurTunerType() == TpInfo.ISDBT)
        {
            if(Pvcfg.getCAType() == Pvcfg.CA_VMX && mSearchMode == TVScanParams.SCAN_VMX_SEARCH) // for VMX need open/close
                ScanParamsVMXStartScan(0, mStartTPID, 0, mSearchMode, mChannelType, mScanModeCaFta, 0, 0, mStartTPID, mEndTPID);
            else
                ScanParamsStartScan(0, mTpID, 0, mSearchMode, TVScanParams.SEARCH_OPTION_ALL, mScanModeCaFta, mNitSearch, mSegChannel);
        }
        else if(GetCurTunerType() == TpInfo.DVBS)
        {
            // will do network search if mSearchMode == SCAN_MODE_NETWORK, nitSearch flag no use here
            ScanParamsStartScan(0, mTpID, mSatInfo.getSatId(), mSearchMode, mChannelType, mScanModeCaFta, 0, 0);
        }
        mCheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        //Log.d(TAG, "onConnected: CheckSignalHandler post runnable" );
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        mCheckSignalHandler.removeCallbacks(CheckStatusRunnable);
        Log.d(TAG, "onDisconnected: CheckSignalHandler remove Callback" );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume" );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsScanBegin == true) // Stop scan when VMX send Search callback // connie 20180903 for VMX -s
            ScanParamsStopScan(false);
        if(Pvcfg.getCAType() == Pvcfg.CA_VMX && mSearchMode == TVScanParams.SCAN_VMX_SEARCH) // for VMX need open/close
            VMXOsmFinish(mTriggerID, mTriggerNum);
    }

    @Override 
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);

        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_SCAN_BEGIN :
            {
                Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_BEGIN!");
                mIsScanBegin = true;
                if(mSearchMode == TVScanParams.SCAN_MODE_AUTO){//eric lin 20180614 show first tp info in auto search
                    List<TpInfo> tpList = TpInfoGetList(GetCurTunerType());
                    if(tpList != null) {
                        //Log.d(TAG, "TTT onMessage:    TVMessage.TYPE_SCAN_BEGIN! first mTpID="+tpList.get(0).getTpId());
                        TVMessage.SetScanTP(0, tpList.get(0).getTpId());//fake tvMessage, just for show first tp info
                        UpdateProgress(tvMessage);
                    }
                }
            }break;
            case TVMessage.TYPE_SCAN_SERCHTP:
            {
                //Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_SERCHTP!");
                if(mIsScanBegin) {
                    UpdateProgress(tvMessage);
                }
            }break;
            case TVMessage.TYPE_SCAN_PROCESS:
            {
                //Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_PROCESS!");
                if(mIsScanBegin) {
                    UpdateStatus(tvMessage);
                }
            }break;
            case TVMessage.TYPE_SCAN_SCHEDULE:
            {
                //Log.d(TAG, "onMessage:    TVMessage.TYPE_SCAN_SCHEDULE!");
                if(mIsScanBegin) {
                    UpdateProgressBar(tvMessage);
                }
            }break;
            case TVMessage.TYPE_SCAN_END:
            {
                //Log.d(TAG, "onMessage:     TVMessage.TYPE_SCAN_END");
                if(mIsScanBegin) {
                    ScanEnd(tvMessage);
                }
            }break;
            default:
                break;
        }
    }

    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            UpdateSignalLevel();
            mCheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };


    private void item_init(){
        ActivityTitleView mTitleView;
        Button mButtonCancel;
        mTitleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);

        //progress bar
        mProgressValue = (TextView) this.findViewById(R.id.progressValueTxv);
        mProgressSearch = (ProgressBar) this.findViewById(R.id.progressPROG);
        mProgressStrength = (ProgressBar) this.findViewById(R.id.strengthPROG);
        mProgressQuality = (ProgressBar) this.findViewById(R.id.qualityPROG);
        mBerValue = (TextView) this.findViewById(R.id.berValueTxv);

        if(GetCurTunerType() == TpInfo.DVBC || GetCurTunerType() == TpInfo.DVBT || GetCurTunerType() == TpInfo.ISDBT)
        {
            if(mSearchMode == 0)
                mTitleView.setTitleView(getString(R.string.STR_TITLE_AUTO_SEARCH));
            else {
                if( Pvcfg.getCAType() == Pvcfg.CA_VMX && mSearchMode == TVScanParams.SCAN_VMX_SEARCH )  // for VMX need open/close
                    mTitleView.setTitleView(getString(R.string.STR_TITLE_VMX_SEARCH));
                else {
                    if(mNitSearch == 1)
                        mTitleView.setTitleView(getString(R.string.STR_TITLE_NETWORK_SEARCH));
                    else
                        mTitleView.setTitleView(getString(R.string.STR_TITLE_MANUAL_SEARCH));
                }
                TextView progressTxv = (TextView) findViewById(R.id.progressTXV);
                progressTxv.setVisibility(GONE);
                mProgressSearch.setVisibility(GONE);
                mProgressValue.setVisibility(GONE);
            }
        }
        else if(GetCurTunerType() == TpInfo.DVBS)
        {
            String[] strSearchBtns = getResources().getStringArray(R.array.STR_DVBS_ARRAY_SEARCH_BUTTON);
            String strSearchBtn = getString(R.string.STR_INSTALLATION) + " > " + strSearchBtns[mSearchMode];
            mTitleView.setTitleView(strSearchBtn);
        }

        // setting tv & radio adapter
        final LinearLayoutManager linearLayoutManagerTV = new LinearLayoutManager(this);
        final LinearLayoutManager linearLayoutManagerRadio = new LinearLayoutManager(this);
        mListviewTV = (RecyclerView) this.findViewById(R.id.tvlistLIV);
        mListviewTV.setItemAnimator(null);
        mListviewTV.setLayoutManager(linearLayoutManagerTV);
        mListviewRadio = (RecyclerView) this.findViewById(R.id.radioLIV);
        mListviewRadio.setItemAnimator(null);
        mListviewRadio.setLayoutManager(linearLayoutManagerRadio);

        if(mTvlist==null)
            mTvlist = new ArrayList<>();
        if(mRadiolist==null)
            mRadiolist = new ArrayList<>();

        mTvListAdapter = new ScanResultAdapter(this,mTvlist);
        mRadioListAdapter = new ScanResultAdapter(this,mRadiolist);

        mListviewTV.setAdapter(mTvListAdapter);
        mListviewRadio.setAdapter(mRadioListAdapter);

        // cancel & View Button
        mButtonCancel = (Button)this.findViewById(R.id.cancelBTN);
        mButtonComplete = (Button)this.findViewById(R.id.completeBTN);

        mButtonCancel.setOnClickListener(CancelListener);
        mButtonComplete.setOnClickListener(CompleteListener);
        mButtonComplete.setVisibility(INVISIBLE);

        mTextviewInfo =(TextView)this.findViewById(R.id.searchInfoTXV);

        // Get Tp info for Display if manual scan
        if (mSearchMode == TVScanParams.SCAN_MODE_MANUAL)
        {
            String str;
            str = GetSearchInfoMsg(mTpInfo, mSatInfo);
            mTextviewInfo.setText(str) ;
        }
    }

    private class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {
        private ArrayList<Object> listItems;
        private Context context;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView chnum;
            TextView chname;
            ImageView scramble;
            ViewHolder(View itemView) {
                super(itemView);
                chnum = (TextView) itemView.findViewById(R.id.chNumScanTXV);
                chname = (TextView) itemView.findViewById(R.id.chNameScanTXV);
                scramble = (ImageView) itemView.findViewById(R.id.scrambleScanIGV);
            }
        }

        ScanResultAdapter(Context cont, ArrayList<Object> list) {
            Log.d(TAG, "ScanResultAdapter: ");
            context = cont;
            listItems = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: ");
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.scan_result_list_layout, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: pos = "+ position);
            serviceInfo servInfo = (serviceInfo) listItems.get(position);
            holder.chnum.setText(String.valueOf(servInfo.getChNum()));
            holder.chname.setText(servInfo.getName());            
            if (servInfo.getCAflag() == 1) {
                holder.scramble.setVisibility(VISIBLE);
            }else
                holder.scramble.setVisibility(INVISIBLE);
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount: "+ listItems.size());
            return listItems.size();
        }

        @Override
        public long getItemId(int position) {
            //return super.getItemId(position);
            return position;
        }
    }

    private void UpdateSignalLevel()
    {
        int Quality;
        int Strength;
        int lock;
        int barcolor;
        String ber;

        lock = TunerGetLockStatus(0);
        Quality = TunerGetQuality(0);
        Strength = TunerGetStrength(0);
        ber = TunerGetBER(0);

        if(lock == 1 ) {
            barcolor = Color.GREEN;
        }
        else {
            barcolor = Color.RED;
            Strength = 0;
            Quality = 0;
        }        
        mProgressQuality.setProgressTintList(ColorStateList.valueOf(barcolor));
        mProgressQuality.setProgress(Quality);
        mProgressStrength.setProgressTintList(ColorStateList.valueOf(barcolor));
        mProgressStrength.setProgress(Strength);

        //mBerValue.setText(ber);
    }

    private OnClickListener CancelListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsScanBegin = false;
            ScanParamsStopScan(false);
            setResult(RESULT_SEARCH_FAIL); // Edwin 20181129 add search fail message
            finish();
        }
    };

    private OnClickListener CompleteListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsScanBegin = false;
            ScanParamsStopScan(true);
            if(ScanResultSetChannel() == 1)//there are some programs
                SetChannelExist(1);//1 : can go to normal view ; 0 : can't go to normal view
            else {//there is no program
                SetChannelExist(0);
            }

            // Edwin 20181214 live tv cannot scan channel -s
            Intent intent = new Intent();
            if ( TvInputActivity.isTvInputOpened() )
            {
                TvInputActivity.disableTvInput();
                SaveTable(EnTableType.PROGRAME);
                intent.setClass(ScanResultActivity.this, ScanActivity.class);
            }
            else
            {
                intent.setClass(ScanResultActivity.this, ViewActivity.class);
            }
            startActivity(intent);
            finish();
            // Edwin 20181214 live tv cannot scan channel -e
        }
    };

    private class serviceInfo {
        private String ChName;
        private int ChNum;
        private int ServiceType;
        private int CaFlag;

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
            return ChNum;
        }
        private void setChNum(int num) {
            this.ChNum = num;
        }
        private int getCAflag() {
            return CaFlag;
    }
        private void setCAflag(int num) {
            this.CaFlag = num;
    }
    }

    private void UpdateStatus(TVMessage tvMessage)
    {
        //Log.d(TAG, "UpdateStatus:     tvMessage.getAlreadyScanedTpNum() = " + tvMessage.getAlreadyScanedTpNum());
        //Log.d(TAG, "UpdateStatus:   TVMessage.TYPE_SCAN_PROCESS!   service id = " + tvMessage.getServiceId());
        if(tvMessage.getServiceId()!=0) {
            serviceInfo ch = new serviceInfo();
            ch.setName(tvMessage.getChannelName());
            ch.setChNum(tvMessage.getchannelLCN());
            ch.setCAflag(tvMessage.getCAFlag());
            ch.setServiceType(tvMessage.getserviceType());
            if (ch.getServiceType() == ProgramInfo.ALL_TV_TYPE) {
                mTvlist.add(ch);
                mTvListAdapter.notifyDataSetChanged();               
                mListviewTV.scrollToPosition(mListviewTV.getLayoutManager().getItemCount()-1);
                //mListviewTV.scrollBy(0, mItemHeight*mListviewTV.getLayoutManager().getItemCount());
            }
            else {
                mRadiolist.add(ch);
                mRadioListAdapter.notifyDataSetChanged();
                mListviewRadio.scrollToPosition(mListviewRadio.getLayoutManager().getItemCount()-1);
                //mListviewRadio.scrollBy(0, mItemHeight*mListviewRadio.getLayoutManager().getItemCount());
            }
        }
    }

    private void UpdateProgress(TVMessage tvMessage) {
//        int searchCount = tvMessage.getAlreadyScanedTpNum();
        int tpID = tvMessage.getTpId();

        // cur searching tp & sat
        TpInfo tmpTpInfo = TpInfoGet(tpID);
        if (tmpTpInfo != null)
        {
            mTpInfo = tmpTpInfo;
        }

        mSatInfo = SatInfoGet(mTpInfo.getSatId());

        // set display tp & sat message
        String str = GetSearchInfoMsg(mTpInfo, mSatInfo);
        mTextviewInfo.setText(str);
    }

    private void UpdateProgressBar(TVMessage tvMessage) {
        int percent = tvMessage.getPercent();

        // set progressbar
        mProgressSearch.setProgress(percent);
        mProgressValue.setText(String.valueOf(percent).concat(" %"));
    }

    private void ScanEnd(TVMessage tvMessage)
    {
        Log.d(TAG, "ScanEnd:   getTotalTVNumber = " + tvMessage.getTotalTVNumber());
        Log.d(TAG, "ScanEnd:   getTotalRadioNumber = " + tvMessage.getTotalRadioNumber());

        mButtonComplete.setVisibility(VISIBLE);
        mButtonComplete.requestFocus();
//        UpdateProgress(tpList.size());

        if(tvMessage.getTotalRadioNumber() == 0 && tvMessage.getTotalTVNumber() == 0)
        {
            new MessageDialog(this,0) {
                public void onSetMessage(View v) {
                    ((TextView) v).setText(getString(R.string.STR_NO_CHANNELS_FOUND));
                }

                public void onSetNegativeButton() {
                }

                public void onSetPositiveButton(int status) {
                }

                public void dialogEnd(int status){

                }
            };
        }

    }

    private String GetSearchInfoMsg(TpInfo tpInfo, SatInfo satInfo)
    {
        String str;

        if(GetCurTunerType() == TpInfo.DVBC)
        {
            int freq = tpInfo.CableTp.getFreq() / 1000;   // Johnny 20190508 modify DVBC freq shown in UI from KHz to MHz
            int symbol = tpInfo.CableTp.getSymbol();

            String strFreq = getString(R.string.STR_FREQUENCY) + " : " + freq + getString(R.string.STR_MHZ);   // Johnny 20190508 modify DVBC freq shown in UI from KHz to MHz
            String strSymbol = getString(R.string.STR_SYMBOLRATE) + " : " + symbol + getString(R.string.STR_MSPS);

            str = strFreq + "     " + strSymbol;
        }
        else if(GetCurTunerType() == TpInfo.ISDBT)
        {
            int freq = tpInfo.TerrTp.getFreq();
            String band = mStrBandList[tpInfo.TerrTp.getBand()];

            String strFreq = getString(R.string.STR_FREQUENCY) + " : " + freq + getString(R.string.STR_KHZ);
            String strBand = getString(R.string.STR_ISDBT_BANDWIDTH) + " : " + band + getString(R.string.STR_KHZ);

            str = strFreq + "     " + strBand;
        }
        else if(GetCurTunerType() == TpInfo.DVBT)//eric lin 20170108 add dvb-t channel search UI
        {
            int channel = tpInfo.TerrTp.getChannel();
            int freq = tpInfo.TerrTp.getFreq();

            String strChannel = getString(R.string.STR_CHANNEL) + " : " + channel;
            String strFreq = freq + getString(R.string.STR_MHZ);

            str = strChannel + "     " + strFreq;
        }
        else if(GetCurTunerType() == TpInfo.DVBS)
        {
            String name = satInfo.getSatName();
            String angle = Float.toString(satInfo.getAngle());
            String angleEW = satInfo.getAngleEW() == SatInfo.ANGLE_E ? "E" : "W";
            String strSat = getString(R.string.STR_DVBS_SATELLITE) + " : " + name + " " + angle + angleEW;

            String freq = Integer.toString(tpInfo.SatTp.getFreq());
            String symbol = Integer.toString(tpInfo.SatTp.getSymbol());
            String polar = tpInfo.SatTp.getPolar() == TpInfo.Sat.POLAR_H ? "H" : "V";
            String strTp = getString(R.string.STR_DVBS_TP) + " : " + freq + " " + polar + " " + symbol;

            str = strSat + "     " + strTp;
        }
        else
        {
            str = " Unknown Tuner Type";
        }

        return str;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                mIsScanBegin = false;
                ScanParamsStopScan(false);
                setResult(RESULT_SEARCH_FAIL); // Edwin 20181129 add search fail message
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private int calMaxItemCount(){
        int visibleItemCount;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int tmpHeight = (int) (height*0.9);//because titleGuideline = 0.1, so 1.0-0.1=0.9
        int itemHeight =  ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        Log.d(TAG, "calMaxItemCount: height=" + height + ", itemHeight=" + height + ", itemHeight="+itemHeight);

        Guideline bottmguideline = (Guideline) findViewById(R.id.guidelineCenterH);
        Guideline topguideline = (Guideline) findViewById(R.id.listTopGuideline);
        ConstraintLayout.LayoutParams paramsBottom = (ConstraintLayout.LayoutParams) bottmguideline.getLayoutParams();
        ConstraintLayout.LayoutParams paramsTop = (ConstraintLayout.LayoutParams) topguideline.getLayoutParams();

        visibleItemCount = (int)(tmpHeight*(paramsBottom.guidePercent - paramsTop.guidePercent))/itemHeight;
        if(visibleItemCount < 1)//for protect
            visibleItemCount = 1;

        Log.d(TAG, "calMaxItemCount: visibleItemCount=" + visibleItemCount + ", bottom.percent=" + paramsBottom.guidePercent + ", top.percent="+paramsTop.guidePercent);

        return visibleItemCount;
    }
}
