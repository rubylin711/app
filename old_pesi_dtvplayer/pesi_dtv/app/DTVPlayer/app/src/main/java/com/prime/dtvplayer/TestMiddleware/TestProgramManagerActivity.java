package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestProgramManagerActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private List<ProgramManagerImpl> ProgramManagerList = null;
    private enum Items {
        ProgramManagerInit(0),
        AddProgramToFav(1),
        MoveProgram(2),
        DelProgram(3),
        DelAllProgram(4),
        Max(5);
        //ProgramManagerInfo(0),
        //TransferToProgramInfo(1),
        //ProgramManagerImpl(2),
        //ResetProgramListChnum(3),
        //Save(8),
        //ProgramManagerSave(10),

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestProgramManagerActivity.Items testItem : TestProgramManagerActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestProgramManagerActivity.Items valueOf(int testItem) {
            return (TestProgramManagerActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private static final int[] BUTTON_IDS = {
            R.id.BtnTestProgramM1,
            R.id.BtnTestProgramM2,
            R.id.BtnTestProgramM3,
            R.id.BtnTestProgramM4,
            R.id.BtnTestProgramM5,
            //R.id.BtnTestProgramM6,
            //R.id.BtnTestProgramM7,
            //R.id.BtnTestProgramM8,
            //R.id.BtnTestProgramM9,
            //R.id.BtnTestProgramM10,
            //R.id.BtnTestProgramM11,
    };
    private TextView TvProgramManager=null;
    private static final String[] ITEM_NAME = {
            "void ProgramManagerInit(int type)\nResult:",
            "void AddProgramToFav(int GroupType,int srcIndex)\nResult:",
            "void MoveProgram(int cur, int dest)\nResult:",
            "void DelProgram(int index,int del)\nResult:",
            "void DelAllProgram(int del)\nResult:",
            //"ProgramManagerInfo(ProgramInfo programInfo)\nResult:",
            //"ProgramInfo TransferToProgramInfo()\nResult:",
            //"ProgramManagerImpl(int type)\nResult:",
            //"void ResetProgramListChnum(ProgramManagerInfo list)\nResult:",
            //"void Save()\nResult:",
            //"void ProgramManagerSave()\nResult:",

    };
    TestMidMain tm = new TestMidMain();//
    private static final int TESTITEM_NUM = TestProgramManagerActivity.Items.Max.getValue();//
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    int mTestResult=0;//
    List<ProgramManagerImpl.ProgramManagerInfo> mAllTvPmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mTvFav1PmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mTvFav2PmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mTvFav3PmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mTvFav4PmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mTvFav5PmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mTvFav6PmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mAllRadioPmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mRadioFav1PmInfoList;
    List<ProgramManagerImpl.ProgramManagerInfo> mRadioFav2PmInfoList;
    private long  mDelAllTvChId=0;
    private long  mDelTVFav1ChId=0;
    private long  mDelTVFav2ChId=0;
    private long  mDelTVFav3ChId=0;
    private long  mDelTVFav4ChId=0;
    private long  mDelTVFav5ChId=0;
    private long  mDelTVFav6ChId=0;
    private long  mDelAllRadioChId=0;
    private long  mDelRadioFav1ChId=0;
    private long  mDelRadioFav2ChId=0;


    private void resetDelChId(){
        mDelAllTvChId=0;
        mDelTVFav1ChId=0;
        mDelTVFav2ChId=0;
        mDelTVFav3ChId=0;
        mDelTVFav4ChId=0;
        mDelTVFav5ChId=0;
        mDelTVFav6ChId=0;
        mDelAllRadioChId=0;
        mDelRadioFav1ChId=0;
        mDelRadioFav2ChId=0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int i=0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_program_manager);

        Log.d(TAG, "onCreate");

        //Buttons
        buttons = new ArrayList<Button>();
        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);
            //button.setOnClickListener(this); // maybe
            buttons.add(button);
        }
        for(i = Items.ProgramManagerInit.getValue(); i< Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        TvProgramManager = (TextView)this.findViewById(R.id.TvTestProgram);
        TvProgramManager.setText("Info:");
        TvProgramManager.setMovementMethod(new ScrollingMovementMethod());
        TvProgramManager.scrollTo(0,0);

        //eric lin workaround
//        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);//ALL TV
//        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
//        if(ProgramManagerList != null) {
//            if(ProgramManagerList.get(0).ProgramManagerInfoList.size() >=0){
//                AvControlPlayByChannelId(ViewHistory.getPlayId(), ProgramManagerList.get(0).ProgramManagerInfoList.get(0).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);
//            }
//        }else {//ALL RADIO
//            //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
//              ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
//            if (ProgramManagerList != null) {
//                if(ProgramManagerList.get(0).ProgramManagerInfoList.size() >=0){
//                    AvControlPlayByChannelId(ViewHistory.getPlayId(), ProgramManagerList.get(0).ProgramManagerInfoList.get(0).getChannelId(), ProgramInfo.ALL_RADIO_TYPE,1);
//                }
//            }
//        }

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

    //void ProgramManagerInit(int type)
    //void AddProgramToFav(int favMode,int srcIndex)
    //void MoveProgram(int cur, int dest)
    //void DelProgram(int index,int del)
    //void DelAllProgram(int del)
    //ProgramManagerInfo(ProgramInfo programInfo)
    //ProgramInfo TransferToProgramInfo()
    //ProgramManagerImpl(int type).
    //void ResetProgramListChnum(List<DTVActivity.ProgramManagerImpl.ProgramManagerInfo> list)
    //void Save()
    //void ProgramManagerSave()

    public void BtnProgramManagerInit_OnClick(View view){
        //void ProgramManagerInit(int type)
        //time/tv manager call this method
        Log.d(TAG,"BtnProgramManagerInit_OnClick:");
        int itemIndex = Items.ProgramManagerInit.getValue();
        StringBuilder zText = new StringBuilder ();

        fillAllListString(zText);
        TvProgramManager.setText(zText.toString());
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }


    public void BtnAddProgramToFav_OnClick(View view){
        //void AddProgramToFav(int GroupType,int srcIndex)
        //tv list call this method
        Log.d(TAG,"BtnAddProgramToFav_OnClick:");
        int itemIndex = Items.AddProgramToFav.getValue();
        StringBuilder zText = new StringBuilder ();
        int i=0;
        int srcIndex;
        String tv_str = "\nTest...\n"
                +"AddProgramToFav(int GroupType,int srcIndex):\n"
                +"Add first/last program of TV list to tv favorite list...\n";
        String radio_str = "Add first/last program of radio list to radio favorite list...\n";
        List<ProgramManagerImpl.ProgramManagerInfo> addTvFavList = new ArrayList<>();
        List<ProgramManagerImpl.ProgramManagerInfo> addRadioFavList = new ArrayList<>();

        //show original list
        fillString(zText, "Original lists:\n");
        fillAllListString(zText);

        fillString(zText, tv_str);
        //Test: add tv program to tv favorite list
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if(ProgramManagerList != null) {
            if (ProgramManagerList.get(0).ProgramManagerInfoList != null) {
                if(ProgramManagerList.get(0).ProgramManagerInfoList.size() >=1){
                    if(ProgramManagerList.get(0).ProgramManagerInfoList.size() == 1){
                        //add first program to fav list
                        srcIndex = 0;
                        for (i = ProgramInfo.TV_FAV1_TYPE; i <= ProgramInfo.TV_FAV6_TYPE; i++) {
                            ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,i, srcIndex);
                        }
                            addTvFavList.add(ProgramManagerList.get(0).ProgramManagerInfoList.get(srcIndex));
                    }
                    else{//size >=2
                        //add first program to fav list
                        srcIndex = 0;
                        for (i = ProgramInfo.TV_FAV1_TYPE; i <= ProgramInfo.TV_FAV6_TYPE; i++) {
                            ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,i, srcIndex);
                        }
                            addTvFavList.add(ProgramManagerList.get(0).ProgramManagerInfoList.get(srcIndex));
                        //add last program to fav list
                        srcIndex = ProgramManagerList.get(0).ProgramManagerInfoList.size() - 1;
                        for (i = ProgramInfo.TV_FAV1_TYPE; i <= ProgramInfo.TV_FAV6_TYPE; i++) {
                            ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,i, srcIndex);
                        }
                            addTvFavList.add(ProgramManagerList.get(0).ProgramManagerInfoList.get(srcIndex));
                    }
                }else{
                    Log.d(TAG,"BtnAddProgramToFav_OnClick: BK1-2 all tv list is empty");
                    fillString(zText, "all tv list is empty\n");
                    addTvFavList = null;
                }
            }else {
                Log.d(TAG, "BtnAddProgramToFav_OnClick: BK1-3 all tv list is empty\n");
                fillString(zText, "all tv list is empty\n");
                addTvFavList = null;
            }
        }else {
            Log.d(TAG, "BtnAddProgramToFav_OnClick: BK1-4 tv list is empty.\n");
            fillString(zText, "tv list is empty.\n");
            addTvFavList = null;
        }
        ProgramManagerSave(ProgramManagerList);



        //Test: add radio program to radio favorite list
        fillString(zText, radio_str);
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            if (ProgramManagerList.get(0).ProgramManagerInfoList != null) {
                if(ProgramManagerList.get(0).ProgramManagerInfoList.size() >=1){
                    if(ProgramManagerList.get(0).ProgramManagerInfoList.size() ==1){
                        //add first program to fav list
                        srcIndex = 0;
                        for(i=ProgramInfo.RADIO_FAV1_TYPE; i<=ProgramInfo.RADIO_FAV2_TYPE; i++) {
                            ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,(i-ProgramInfo.ALL_RADIO_TYPE), srcIndex);
                        }
                            addRadioFavList.add(ProgramManagerList.get(0).ProgramManagerInfoList.get(srcIndex));
                    }else {//size >=2
                        //add first program to fav list
                        srcIndex = 0;
                        for (i = ProgramInfo.RADIO_FAV1_TYPE; i <= ProgramInfo.RADIO_FAV2_TYPE; i++) {
                            ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,(i - ProgramInfo.ALL_RADIO_TYPE), srcIndex);
                        }
                            addRadioFavList.add(ProgramManagerList.get(0).ProgramManagerInfoList.get(srcIndex));
                        //add last program to fav list
                        srcIndex = ProgramManagerList.get(0).ProgramManagerInfoList.size() - 1;
                        for (i = ProgramInfo.RADIO_FAV1_TYPE; i <= ProgramInfo.RADIO_FAV2_TYPE; i++) {
                            ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,(i - ProgramInfo.ALL_RADIO_TYPE), srcIndex);
                        }
                            addRadioFavList.add(ProgramManagerList.get(0).ProgramManagerInfoList.get(srcIndex));
                    }
                }else{
                    Log.d(TAG,"BtnAddProgramToFav_OnClick: BK2-8 all radio list is empty");
                    fillString(zText, "all radio list is empty");
                    addRadioFavList = null;
                }
            }else {
                Log.d(TAG, "BtnAddProgramToFav_OnClick: BK2-9: all radio list is empty");
                fillString(zText, "all radio list is empty\n");
                addRadioFavList = null;
            }
        }else {
            Log.d(TAG, "BtnAddProgramToFav_OnClick: BK2-10: 9radio list is empty.");
            fillString(zText, "radio list is empty.\n");
        }
        ProgramManagerSave(ProgramManagerList);

        //show all list after add fav
        fillString(zText, "\nAfter test, list is ....\n");
        fillAllListString(zText);
        Log.d(TAG,"BtnAddProgramToFav_OnClick: BK4");
        //show result
        TvProgramManager.setText(zText.toString());
        if(checkAddProgramToFavResult(addTvFavList, addRadioFavList)==true){
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
            setCheckedResult(itemIndex, false);
        }
    }


    private boolean checkAddProgramToFavResult(List<ProgramManagerImpl.ProgramManagerInfo> tvList, List<ProgramManagerImpl.ProgramManagerInfo> radioList){
        int i,j,k;
        boolean find=false;

        if(tvList == null && radioList == null)
            return false;
        //Check TV favorite
        if(tvList != null) {
            //Log.d(TAG, "checkAddProgramToFavResult: tvList.size()="+tvList.size());
            //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
            for (i = 0; i < tvList.size(); i++) {
                for (j = ProgramInfo.TV_FAV1_TYPE; j <= ProgramInfo.TV_FAV6_TYPE; j++) {
                    find = false;
                    for (k = 0; k < ProgramManagerList.get(j).ProgramManagerInfoList.size(); k++) {
                        if (tvList.get(i).getChannelId()  == ProgramManagerList.get(j).ProgramManagerInfoList.get(k).getChannelId()){
                            //Log.d(TAG, "checkAddProgramToFavResult(TV): i=" + i + ", j=" + j + ", find related favInfo, chName="+tvList.get(i).getChName());
                            find = true;
                            break;
                        }
                    }
                    if (find == false) {
                        Log.d(TAG, "checkAddProgramToFavResult(TV): i=" + i + ", j=" + j + ", can not find related favInfo");
                        return false;
                    }
                }
            }
        }
        //Check Radio favorite
        if(radioList != null) {
            Log.d(TAG, "checkAddProgramToFavResult: radioList.size()="+radioList.size());
            //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
            for (i = 0; i < radioList.size(); i++) {
                for (j = ProgramInfo.RADIO_FAV1_TYPE; j <= ProgramInfo.RADIO_FAV2_TYPE; j++) {
                    find = false;
                    for (k = 0; k < ProgramManagerList.get(j-ProgramInfo.ALL_RADIO_TYPE).ProgramManagerInfoList.size(); k++) {
                        if (radioList.get(i).getChannelId() == ProgramManagerList.get(j-ProgramInfo.ALL_RADIO_TYPE).ProgramManagerInfoList.get(k).getChannelId()) {
                            //Log.d(TAG, "checkAddProgramToFavResult(Radio): i=" + i + ", j=" + j + ", find related favInfo, chName="+radioList.get(i).getChName());
                            find = true;
                            break;
                        }
                    }
                    if (find == false) {
                        Log.d(TAG, "checkAddProgramToFavResult(Radio): i=" + i + ", j=" + j + ", can not find related favInfo");
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public void BtnMoveProgram_OnClick(View view){
        //void MoveProgram(int cur, int dest)
        //tv/fav list call this method
        Log.d(TAG,"BtnMoveProgram_OnClick:");
        int itemIndex = Items.MoveProgram.getValue();
        StringBuilder zText = new StringBuilder ();
        int i,j,start=0,end=0;
        List<Integer> tvStartList = new ArrayList<Integer>();
        List<Integer> tvEndList = new ArrayList<Integer>();
        List<Integer> radioStartList = new ArrayList<Integer>();
        List<Integer> radioEndList = new ArrayList<Integer>();

        String tv_str = "\nTest...\n"
                +"void MoveProgram(int cur, int dest):\n"
                +"Move first program to last program of all TV/Tv favorite list...\n";
        String radio_str = "Move first program to last program of all Radio/Radio favorite list...\n";


        mAllTvPmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mTvFav1PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mTvFav2PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mTvFav3PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mTvFav4PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mTvFav5PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mTvFav6PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mAllRadioPmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mRadioFav1PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();
        mRadioFav2PmInfoList = new ArrayList<ProgramManagerImpl.ProgramManagerInfo>();


        //show original list
        fillString(zText, "Original lists:\n");
        fillAllListString(zText);

        fillString(zText, tv_str);
        //Test: "Move first program to last program of all TV/Tv favorite list
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++) {
                for (j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {//get original tv list
                    if (i == ProgramInfo.ALL_TV_TYPE) {
                        mAllTvPmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.TV_FAV1_TYPE) {
                        mTvFav1PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.TV_FAV2_TYPE) {
                        mTvFav2PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.TV_FAV3_TYPE) {
                        mTvFav3PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.TV_FAV4_TYPE) {
                        mTvFav4PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.TV_FAV5_TYPE) {
                        mTvFav5PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.TV_FAV6_TYPE) {
                        mTvFav6PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    }
                }
            }
            for(i=0; i<ProgramManagerList.size(); i++){
                //start move
                if(ProgramManagerList.get(i).ProgramManagerInfoList.size() >=2){
                    start = 0;
                    end = ProgramManagerList.get(i).ProgramManagerInfoList.size()-1;
                    tvStartList.add(start);
                    tvEndList.add(end);
                    ProgramManagerList.get(i).MoveProgram(ProgramManagerList,start, end);
                }else{
                    tvStartList.add(-1);
                    tvEndList.add(-1);
                    Log.d(TAG, "ProgramManagerList["+i+"] tv list is empty or just have one program");//
                    fillString(zText, "ProgramManagerList["+i+"] list is empty or just have one program\n");//
                }
            }
        }else {
            Log.d(TAG, "tv list is empty.\n");
            fillString(zText, "tv list is empty.\n");
        }
        ProgramManagerSave(ProgramManagerList);

        //Test: add radio program to radio favorite list
        fillString(zText, radio_str);
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++) {
                for (j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {//get original radio list
                    if (i == ProgramInfo.ALL_RADIO_TYPE - ProgramInfo.ALL_RADIO_TYPE) {
                        mAllRadioPmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.RADIO_FAV1_TYPE - ProgramInfo.ALL_RADIO_TYPE) {
                        mRadioFav1PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    } else if (i == ProgramInfo.RADIO_FAV2_TYPE - ProgramInfo.ALL_RADIO_TYPE) {
                        mRadioFav2PmInfoList.add(ProgramManagerList.get(i).ProgramManagerInfoList.get(j));
                    }
                }
            }
            for(i=0; i<ProgramManagerList.size(); i++){
                //start move
                if(ProgramManagerList.get(i).ProgramManagerInfoList.size() >=2){
                    start = 0;
                    end = ProgramManagerList.get(i).ProgramManagerInfoList.size()-1;
                    radioStartList.add(start);
                    radioEndList.add(end);
                    ProgramManagerList.get(i).MoveProgram(ProgramManagerList,start, end);
                }else{
                    radioStartList.add(-1);
                    radioEndList.add(-1);
                    Log.d(TAG, "(radio)ProgramManagerList["+i+"] radio list is empty or just have one program");//
                    fillString(zText, "ProgramManagerList["+i+"] list is empty or just have one program\n");//
                }
            }
        }else {
            Log.d(TAG, "\nradio list is empty.");
            fillString(zText, "\nradio list is empty.");
        }
        ProgramManagerSave(ProgramManagerList);


        //show all list after add fav
        fillString(zText, "\nAfter test, list is ....\n");
        fillAllListString(zText);

        //show result
        TvProgramManager.setText(zText.toString());
        if(checkMoveProgramResult(tvStartList, tvEndList, radioStartList, radioEndList)==true){
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    private boolean compareThereIds(int type, int favMode, Integer start, Integer end, List<ProgramManagerImpl.ProgramManagerInfo> src){
        int i;
        List<ProgramManagerImpl.ProgramManagerInfo> dest=null;

        if(type == ProgramInfo.ALL_TV_TYPE) {
            if (favMode == 0)
                dest = mAllTvPmInfoList;
            else if (favMode == 1)
                dest = mTvFav1PmInfoList;
            else if (favMode == 2)
                dest = mTvFav2PmInfoList;
            else if (favMode == 3)
                dest = mTvFav3PmInfoList;
            else if (favMode == 4)
                dest = mTvFav4PmInfoList;
            else if (favMode == 5)
                dest = mTvFav5PmInfoList;
            else if (favMode == 6)
                dest = mTvFav6PmInfoList;
            else {
                Log.d(TAG, "compareThereIds: type=" + type + ", favMode=" + favMode + " not illegal tv mode");
                return false;
            }
        }else if(type == ProgramInfo.ALL_RADIO_TYPE) {
            if (favMode == 0)
                dest = mAllRadioPmInfoList;
            else if (favMode == 1)
                dest = mRadioFav1PmInfoList;
            else if (favMode == 2)
                dest = mRadioFav2PmInfoList;
            else {
                Log.d(TAG, "compareThereIds: type=" + type + ", favMode=" + favMode + " not illegal radio mode");
                return false;
            }
        }else {
            Log.d(TAG, "compareThereIds: type=" + type + ", favMode=" + favMode + " not illegal type");
            return false;
        }

        if(start == -1 || end == -1){
            Log.d(TAG, "compareThereIds: start or end = -1, return false");
            return false;
        }


        Log.d(TAG,"compareThereIds: type="+type+", favMode="+favMode+", start="+start+", end="+end);
        for(i=0; i<src.size(); i++) {
            if(start < end){//up
                if(i >= start && i < end){
                    if(src.get(i).getChannelId() == dest.get(i+1).getChannelId()){
                        Log.d(TAG, "compareChannelId(10): list["+i+"] match");
                    }else {
                        Log.d(TAG, "compareChannelId(20): list[" + i + "] not match");
                        return false;
                    }
                }else if(i == end){
                    if(src.get(i).getChannelId() == dest.get(start).getChannelId()){
                        Log.d(TAG, "compareChannelId(11): list["+i+"] match");
                    }else {
                        Log.d(TAG, "compareChannelId(21): list[" + i + "] not match");
                        return false;
                    }
                }else{
                    if(src.get(i).getChannelId() == dest.get(i).getChannelId()){
                        Log.d(TAG, "compareChannelId(12): list["+i+"] match");
                    }else {
                        Log.d(TAG, "compareChannelId(23): list[" + i + "] not match");
                        return false;
                    }
                }
            }else if(start > end){//down
                if(i <= start && i > end){
                    if(src.get(i).getChannelId() == dest.get(i-1).getChannelId()){
                        Log.d(TAG, "compareChannelId(14): list["+i+"] match");
                    }else {
                        Log.d(TAG, "compareChannelId(24): list[" + i + "] not match");
                        return false;
                    }
                }else if(i == end){
                    if(src.get(i).getChannelId() == dest.get(start).getChannelId()){
                        Log.d(TAG, "compareChannelId(11): list["+i+"] match");
                    }else {
                        Log.d(TAG, "compareChannelId(21): list[" + i + "] not match");
                        return false;
                    }
                }else{
                    if(src.get(i).getChannelId() == dest.get(i).getChannelId()){
                        Log.d(TAG, "compareChannelId(12): list["+i+"] match");
                    }else {
                        Log.d(TAG, "compareChannelId(23): list[" + i + "] not match");
                        return false;
                    }
                }
            }
        }

        return true;
    }


    private boolean checkMoveProgramResult(List<Integer> tvStartList, List<Integer> tvEndList, List<Integer> radioStartList, List<Integer> radioEndList){
        int i,j;
        List<ProgramManagerImpl.ProgramManagerInfo> src;

        //Both TV/Radio list have not more than 2 programs, return fail
        if(mAllTvPmInfoList.size() <=2
                && mAllRadioPmInfoList.size() <=2)
            return false;

        //Check TV/TV favorite list
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++){
                if(ProgramManagerList.get(i).ProgramManagerInfoList.size() >=2){
                    src = ProgramManagerList.get(i).ProgramManagerInfoList;
                    if(compareThereIds(ProgramInfo.ALL_TV_TYPE, i, tvStartList.get(i), tvEndList.get(i), src) == false)
                        return false;
                }else{
                    Log.d(TAG, "checkMoveProgramResult(TV): ["+i+"] list just have one program or empty");
                }
            }
        }else{
            Log.d(TAG, "checkMoveProgramResult(TV): tv list is empty.");
        }

        //Check Radio/Radio favorite list
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++){
                if(ProgramManagerList.get(i).ProgramManagerInfoList.size() >=2){
                    src = ProgramManagerList.get(i).ProgramManagerInfoList;
                    if(compareThereIds(ProgramInfo.ALL_RADIO_TYPE, i, radioStartList.get(i), radioEndList.get(i), src) == false)
                        return false;
                }else{
                    Log.d(TAG, "checkMoveProgramResult(Radio): ["+i+"] list just have one program  or empty");
                }
            }
        }else{
            Log.d(TAG, "checkMoveProgramResult(Radio): radio list is empty.");
        }

        return true;
    }

    private void setDelChId(int type, int favMode, long chId){
        if(type == ProgramInfo.ALL_TV_TYPE) {
            if (favMode == 0)
                mDelAllTvChId = chId;
            else if (favMode == 1)
                mDelTVFav1ChId = chId;
            else if (favMode == 2)
                mDelTVFav2ChId = chId;
            else if (favMode == 3)
                mDelTVFav3ChId = chId;
            else if (favMode == 4)
                mDelTVFav4ChId = chId;
            else if (favMode == 5)
                mDelTVFav5ChId = chId;
            else if (favMode == 6)
                mDelTVFav6ChId = chId;
        }else if(type == ProgramInfo.ALL_RADIO_TYPE) {
            if (favMode == 0)
                mDelAllRadioChId = chId;
            else if (favMode == 1)
                mDelRadioFav1ChId = chId;
            else if (favMode == 2)
                mDelRadioFav2ChId = chId;
        }
    }

    public void BtnDelProgram_OnClick(View view){
        //void DelProgram(int index,int del)
        //tv/fav list call this method
        Log.d(TAG,"BtnDelProgram_OnClick:");
        int itemIndex = Items.DelProgram.getValue();
        StringBuilder zText = new StringBuilder ();
        int i,j;
        int delIndex=0;
        int delChId=0;

        String tv_str = "\nTest...\n"
                +"void DelProgram(int index,int del):\n"
                +"Delete first program of all TV/Tv favorite list...\n";
        String radio_str = "Delete first program of all Radio/Radio favorite list...\n";

        resetDelChId();

        //show original list
        fillString(zText, "Original lists:\n");
        fillAllListString(zText);

        fillString(zText, tv_str);
        //Test: "Move first program to last program of all TV/Tv favorite list
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if(ProgramManagerList != null) {
            if (ProgramManagerList.get(0).ProgramManagerInfoList.size() >= 1) {
                ProgramManagerList.get(0).DelProgram(ProgramManagerList,delIndex, 1);
                for(i=0; i<ProgramManagerList.size(); i++)
                    setDelChId(ProgramInfo.ALL_TV_TYPE, i, ProgramManagerList.get(0).ProgramManagerInfoList.get(0).getChannelId());
            } else {
                Log.d(TAG, "BtnDelProgram_OnClick: ProgramManagerList[0] tv list is empty");
                fillString(zText, "ProgramManagerList[0] tv list is empty\n");//
            }
            ProgramManagerSave(ProgramManagerList);
        }else {
            Log.d(TAG, "BtnDelProgram_OnClick: tv list is empty.");
            fillString(zText, "tv list is empty.\n");
        }


        //Test: add radio program to radio favorite list
        fillString(zText, radio_str);
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            if (ProgramManagerList.get(0).ProgramManagerInfoList.size() >= 1) {
                ProgramManagerList.get(0).DelProgram(ProgramManagerList,delIndex, 1);
                for(i=0; i<ProgramManagerList.size(); i++)
                    setDelChId(ProgramInfo.ALL_RADIO_TYPE, i, ProgramManagerList.get(0).ProgramManagerInfoList.get(0).getChannelId());
            } else {
                Log.d(TAG, "BtnDelProgram_OnClick: ProgramManagerList[0] radio list is empty");
                fillString(zText, "ProgramManagerList[0] radio list is empty\n");//
            }
            ProgramManagerSave(ProgramManagerList);
        }else {
            Log.d(TAG, "BtnDelProgram_OnClick: radio list is empty.");
            fillString(zText, "radio list is empty.\n");
        }


        //show all list after del
        fillString(zText, "\nAfter test, list is ....\n");
        fillAllListString(zText);

        //show result
        TvProgramManager.setText(zText.toString());
        //check result
        if(checkDelProgramResult()==true){
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
            setCheckedResult(itemIndex, false);
        }

    }

    private boolean checkDelProgramResult(){
        int i,j;
        List<ProgramManagerImpl.ProgramManagerInfo> src;
        Log.d(TAG, "checkDelProgramResult: ");
        //Both Original TV/Radio list have no programs, return fail
//        if(mAllTvPmInfoList.size() == 0
//                && mAllRadioPmInfoList.size() ==0) {
//            Log.d(TAG, "checkDelProgramResult: Both Original TV/Radio list have no programs, return fail");
//            return false;
//        }

        //Check TV/TV favorite list
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if(ProgramManagerList != null) {
            //if(mAllTvPmInfoList.size() >= 1) {//original all tv list have more than one program
                for (i = 0; i < ProgramManagerList.size(); i++) {
                    if (ProgramManagerList.get(i).ProgramManagerInfoList.size() >= 0) {
                        src = ProgramManagerList.get(i).ProgramManagerInfoList;
                        if (compareDelFirstProgram(ProgramInfo.ALL_TV_TYPE, i, src) == false) {
                            Log.d(TAG, "compareDelFirstProgram: TV favMode="+i+"...fail");
                            return false;
                        }
                    } else {
                        Log.d(TAG, "checkDelProgramResult: [" + i + "] tv list is empty");
                    }
                }
            //}
        }else{
            Log.d(TAG, "checkDelProgramResult: tv list is empty.");
        }

        //Check Radio/Radio favorite list
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            //if(mAllTvPmInfoList.size() >= 1) {//original all radio list have more than one program
                for (i = 0; i < ProgramManagerList.size(); i++) {
                    if (ProgramManagerList.get(i).ProgramManagerInfoList.size() >= 0) {
                        src = ProgramManagerList.get(i).ProgramManagerInfoList;
                        if (compareDelFirstProgram(ProgramInfo.ALL_RADIO_TYPE, i, src) == false) {
                            Log.d(TAG, "compareDelFirstProgram: Radio favMode=%d"+i+"...fail");
                            return false;
                        }
                    } else {
                        Log.d(TAG, "checkDelProgramResult: [" + i + "] radio list is empty");
                    }
                }
            //
        }else{
            Log.d(TAG, "checkDelProgramResult: radio list is empty.");
        }

        return true;
    }



    private boolean compareDelFirstProgram(int type, int favMode, List<ProgramManagerImpl.ProgramManagerInfo> src){
        int i;
        List<ProgramManagerImpl.ProgramManagerInfo> dest=null;
        boolean find=false;
        long delChId=0;

        if(type == ProgramInfo.ALL_TV_TYPE) {
            if (favMode == 0) {
                delChId = mDelAllTvChId;
                dest = mAllTvPmInfoList;
            }
            else if (favMode == 1) {
                delChId = mDelTVFav1ChId;
                dest = mTvFav1PmInfoList;
            }
            else if (favMode == 2) {
                delChId = mDelTVFav2ChId;
                dest = mTvFav2PmInfoList;
            }
            else if (favMode == 3) {
                delChId = mDelTVFav3ChId;
                dest = mTvFav3PmInfoList;
            }
            else if (favMode == 4) {
                delChId = mDelTVFav4ChId;
                dest = mTvFav4PmInfoList;
            }
            else if (favMode == 5) {
                delChId = mDelTVFav5ChId;
                dest = mTvFav5PmInfoList;
            }
            else if (favMode == 6) {
                delChId = mDelTVFav6ChId;
                dest = mTvFav6PmInfoList;
            }
            else {
                Log.d(TAG, "compareDelFirstProgram: type=" + type + ", favMode=" + favMode + " not illegal tv mode");
                return false;
            }
        }else if(type == ProgramInfo.ALL_RADIO_TYPE) {
            if (favMode == 0) {
                delChId = mDelAllRadioChId;
                dest = mAllRadioPmInfoList;
            }
            else if (favMode == 1) {
                delChId = mDelRadioFav1ChId;
                dest = mRadioFav1PmInfoList;
            }
            else if (favMode == 2) {
                delChId = mDelRadioFav2ChId;
                dest = mRadioFav2PmInfoList;
            }
            else {
                Log.d(TAG, "compareDelFirstProgram: type=" + type + ", favMode=" + favMode + " not illegal radio mode");
                return false;
            }
        }else {
            Log.d(TAG, "compareDelFirstProgram: type=" + type + ", favMode=" + favMode + " not illegal type");
            return false;
        }

        Log.d(TAG,"compareDelFirstProgram: type="+type+", favMode="+favMode);


        for (i = 0; i < src.size(); i++) {
            if(src.get(i).getChannelId() == delChId) {
                Log.d(TAG, "compareDelFirstProgram: type=" + type + ", favMode=" + favMode + ", i="+i+", chId="+delChId);
                return false;
            }
        }


        return true;
    }


    public void BtnDelAllProgram_OnClick(View view){
        //void DelAllProgram(int del)
        int i;
        int itemIndex = Items.DelAllProgram.getValue();
        StringBuilder zText = new StringBuilder ();
        String tv_str = "\nTest...\n"
                +"void DelAllProgram(int del):\n"
                +"Delete all programs of all TV/Tv favorite list...\n";
        String radio_str = "Delete all programs of all Radio/Radio favorite list...\n";

        Log.d(TAG,"BtnDelAllProgram_OnClick:");
        //show original list
        fillString(zText, "Original lists:\n");
        fillAllListString(zText);
        fillString(zText, tv_str);

        //Test: Delete all programs of all TV/Tv favorite list...
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++){
                ProgramManagerList.get(i).DelAllProgram(1); //start delete all
                ProgramManagerList.get(i).Save();
            }
        }else {
            Log.d(TAG, "BtnDelAllProgram_OnClick: tv list is empty.\n");
            fillString(zText, "tv list is empty.\n");
        }

        //Test: Delete all programs of all Radio/Radio favorite list...
        fillString(zText, radio_str);
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++){
                ProgramManagerList.get(i).DelAllProgram(1); //start delete all
                ProgramManagerList.get(i).Save();
            }
        }else {
            Log.d(TAG, "\nBtnDelAllProgram_OnClick: radio list is empty.");
            fillString(zText, "\nradio list is empty.");
        }

        ProgramManagerSave(ProgramManagerList); //eric lin add, because ProgramManagerList.get(i).Save() not do ProgramManagerUpdateHistory();

        //show result
        fillString(zText, "\nAfter test, list is ....\n");
        fillAllListString(zText);
        TvProgramManager.setText(zText.toString());

        //check result
        if(checkDelAllProgramResult()==true){
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    private boolean checkDelAllProgramResult(){
        int i=0;
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++){
                if(ProgramManagerList.get(i).ProgramManagerInfoList.size() !=0){
                    Log.d(TAG, "checkDelAllProgramResult: tv list["+i+"] is not empty, size="+ProgramManagerList.get(i).ProgramManagerInfoList.size());
                    return false;
                }else{
                    Log.d(TAG, "checkDelAllProgramResult: tv list["+i+"] is empty");
                }
            }
        }else {
            Log.d(TAG, "checkDelAllProgramResult: tv/tv favorite list is empty.\n");
        }

        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++){
                if(ProgramManagerList.get(i).ProgramManagerInfoList.size() !=0){
                    Log.d(TAG, "checkDelAllProgramResult: radio list["+i+"] is not empty, size="+ProgramManagerList.get(i).ProgramManagerInfoList.size());
                    return false;
                }else{
                    Log.d(TAG, "checkDelAllProgramResult: radio list["+i+"] is empty");
                }
            }
        }else {
            Log.d(TAG, "checkDelAllProgramResult: radio/radio favorite list is empty.\n");
        }

        return true;
    }


    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }

    void fillAllListString(StringBuilder zText){
        String tv_str = "All TV/TV favorite: ProgramManagerInit(ALL_TV_TYPE):\n";
        String radio_str = "All Radio/Radio favorite: ProgramManagerInit(ALL_RADIO_TYPE):\n";
        int i,j;
        int chNum;
        String chName;

        //All TV, TV Fav1~Fav6
        fillString(zText, tv_str);
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        //Log.d(TAG, "fillAllListString: Show All TV, TV Fav1~Fav6 ...");
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++) {
                if (ProgramManagerList.get(i) != null) {
                    fillString(zText, "list["+i+"]=>");
                    //if(i==0) {//eric lin test
                    //    Log.d(TAG, "fillAllListString: ProgramManagerList.get(i).ProgramManagerInfoList.size()="+ProgramManagerList.get(i).ProgramManagerInfoList.size());
                    //}
                    for (j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {
                        chNum = ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelNum();
                        chName = ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelName();
                        //Log.d(TAG, "list["+i+"]["+chNum+"]"+chName);
                        fillString(zText, "["+chNum+"]"+chName+", ");
                    }
                    fillString(zText, "\n");
                }else {
                    //Log.d(TAG, "list["+i+"] is empty");
                    fillString(zText, "list["+i+"] is empty\n");
                }
            }
        }else
            fillString(zText, "list is empty.\n");

        //All Radio, Radio Fav1~Fav2
        //Log.d(TAG, "fillAllListString: Show All Radio, Radio Fav1~Fav2 ...");
        fillString(zText, "\n");
        fillString(zText, radio_str);
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if(ProgramManagerList != null) {
            for(i=0; i<ProgramManagerList.size(); i++) {
                if (ProgramManagerList.get(i) != null) {
                    fillString(zText, "list["+i+"]=>");
                    for (j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {
                        chNum = ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelNum();
                        chName = ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelName();
                        //Log.d(TAG, "list["+i+"]["+chNum+"]"+chName);
                        fillString(zText, "["+chNum+"]"+chName+", ");
                    }
                    fillString(zText, "\n");
                }else {
                    //Log.d(TAG, "list["+i+"] is empty");
                    fillString(zText, "list["+i+"] is empty\n");
                }
            }
        }else
            fillString(zText, "list is empty.\n");
    }

}
