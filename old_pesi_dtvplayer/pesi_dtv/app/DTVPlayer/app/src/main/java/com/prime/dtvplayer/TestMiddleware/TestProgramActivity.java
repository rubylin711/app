package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestProgramActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private enum Items {
        GetList(0),
        GetSimpleChannelList(1),
        GetByChannelId(2),
        GetSimpleChannelByChannelId(3),
        SaveSimpleList(4),
        DeleteNotReSortChnumByChId(5),
        Max(6);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestProgramActivity.Items testItem : TestProgramActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestProgramActivity.Items valueOf(int testItem) {
            return (TestProgramActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private static final int[] BUTTON_IDS = {
            R.id.BtnTestProgram1,
            R.id.BtnTestProgram2,
            R.id.BtnTestProgram3,
            R.id.BtnTestProgram4,
            R.id.BtnTestProgram5,
            R.id.BtnTestProgram6,
    };
    private TextView TvProgramInfo=null;
    private static final String[] ITEM_NAME = {
            "ProgramInfoGetList(int type)\nResult:",
            "ProgramInfoGetSimpleChannelList(int type)\nResult:",
            "ProgramInfoGetByChannelId(long channelId)\nResult:",
            "ProgramInfoGetSimpleChannelByChannelId(long channelId)\nResult:",
            "ProgramInfoSaveList(List<SimpleChannel> programList,int type)\nResult:",
            "ProgramInfoDeleteNotReSortChnum(long channelId)\nResult:",
    };
    TestMidMain tm = new TestMidMain();//
    private static final int TESTITEM_NUM = Items.Max.getValue();//
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    int mTestResult=0;//
    static int mcnt=0;
    static int mSidcnt=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_program);

        //Buttons
        buttons = new ArrayList<Button>();
        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);
            //button.setOnClickListener(this); // maybe
            buttons.add(button);
        }
        for(int i = Items.GetList.getValue(); i< Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        TvProgramInfo = (TextView)this.findViewById(R.id.TvTestProgram);
        TvProgramInfo.setText("Info:");
        TvProgramInfo.setMovementMethod(new ScrollingMovementMethod());
        TvProgramInfo.scrollTo(0,0);
    }

    private void setCheckedResult(int ItemIndex, boolean testResult){
        int allchecked=0;
        int itemresult=0;
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

    public void BtnProgramInfoGetList_OnClick(View view){
        //List<ProgramInfo> ProgramInfoGetList(int type)
        Log.d(TAG,"BtnProgramInfoGetList_OnClick:");
        int itemIndex = Items.GetList.getValue();
        StringBuilder zText = new StringBuilder ();

        String tv_str = "(1)ProgramInfoGetList(ALL_TV_TYPE):\n";
        String radio_str = "(2)ProgramInfoGetList(ALL_RADIO_TYPE):\n";
        List<ProgramInfo> pList = new ArrayList<>();

        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        fillString(zText, "Info:\n");
        fillString(zText, tv_str);
        if(pList == null){
            fillString(zText, "=>list is empty.\n");
        }else{
            fillString(zText, "=>");
            for(int i=0; i< pList.size(); i++)
                fillString(zText, String.format("[" + pList.get(i).getDisplayNum() + "]" + pList.get(i).getDisplayName() + ", "));
        }

        if(pList != null)
            pList.clear();
        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
        fillString(zText, "\n\n"+radio_str);
        if(pList == null){
            fillString(zText, "=>list is empty.\n");
        }else{
            fillString(zText, "=>");
            for(int i=0; i< pList.size(); i++)
                fillString(zText, String.format("["+pList.get(i).getDisplayNum()+"]"+pList.get(i).getDisplayName()+", "));
        }
        //Log.d(TAG, "zText="+zText.toString());
        TvProgramInfo.setText(zText.toString());

        //showInfo(itemIndex);
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }

    public void BtnProgramInfoGetSimpleChannelList_OnClick(View view){
        //List<SimpleChannel> ProgramInfoGetSimpleChannelList(int type)
        Log.d(TAG,"BtnProgramInfoGetSimpleChannelList_OnClick:");
        int itemIndex = Items.GetSimpleChannelList.getValue();
        StringBuilder zText = new StringBuilder ();
        int i=0,j=0;

        String str_function[]={
                "(1)ProgramInfoGetSimpleChannelList(ALL_TV_TYPE):\n",
                "(2)ProgramInfoGetSimpleChannelList(TV_FAV1_TYPE):\n",
                "(3)ProgramInfoGetSimpleChannelList(TV_FAV1_TYPE):\n",
                "(4)ProgramInfoGetSimpleChannelList(TV_FAV1_TYPE):\n",
                "(5)ProgramInfoGetSimpleChannelList(TV_FAV1_TYPE):\n",
                "(6)ProgramInfoGetSimpleChannelList(TV_FAV1_TYPE):\n",
                "(7)ProgramInfoGetSimpleChannelList(TV_FAV1_TYPE):\n",
                "(8)ProgramInfoGetList(ALL_RADIO_TYPE):\n",
                "(9)ProgramInfoGetList(RADIO_FAV1_TYPE):\n",
                "(10)ProgramInfoGetList(RADIO_FAV2_TYPE):\n",
        };
        List<SimpleChannel> pList = new ArrayList<>();

        fillString(zText, "Info:\n");
        for(i=0; i< ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++){
            if(pList != null)
                pList.clear();
            pList = ProgramInfoGetSimpleChannelList(i,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            fillString(zText, str_function[i]);
            if(pList == null){
                fillString(zText, "=>list is empty.\n");
            }else{
                fillString(zText, "=>");
                for(j=0; j< pList.size(); j++)
                    fillString(zText, String.format("["+pList.get(j).getChannelNum()+"]"+pList.get(j).getChannelName()+", "));
                fillString(zText, "\n");
            }
            fillString(zText, "\n");
        }

        TvProgramInfo.setText(zText.toString());

        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }

//    public void BtnProgramInfoGetByLcn_OnClick(View view){
//        //ProgramInfo ProgramInfoGetByLcn(int lcn, int type)
//        Log.d(TAG,"BtnProgramInfoGetByLcn_OnClick:");
//        int itemIndex = Items.GetByLcn.getValue();
//        String tv_str = "(1)ProgramInfoGetByLcn(int lcn, ALL_TV_TYPE):\n"
//                      + "Get LCN by chNum=1\n=>";
//        String radio_str = "(2)ProgramInfoGetByLcn(int lcn, ALL_RADIO_TYPE):\n"
//                            + "Get LCN by chNum=1\n=>";
//        StringBuilder zText = new StringBuilder ();
//        ProgramInfo tmp = null;
//        ProgramInfo program = null;
//        //show chnum=1's tv/radio program
//        //TV
//        fillString(zText, "Info:\n");
//        fillString(zText, tv_str);
//        tmp = ProgramInfoGetByChnum(1, ProgramInfo.ALL_TV_TYPE);
//        if(tmp == null){
//            fillString(zText, "There is not chnum=1's TV program.\n");
//        }else{
//            program = ProgramInfoGetByLcn(tmp.getDisplayNum(), ProgramInfo.ALL_TV_TYPE);
//            if(program != null)
//                fillString(zText, program.getDisplayName()+" ,LCN="+program.getDisplayNum());
//            fillString(zText, "\n\n");
//        }
//
//        //Radio
//        fillString(zText, radio_str);
//        tmp = ProgramInfoGetByChnum(1, ProgramInfo.ALL_RADIO_TYPE);
//        if(tmp == null){
//            fillString(zText, "There is not chnum=1's Radio program.\n");
//        }else{
//            program = ProgramInfoGetByLcn(tmp.getDisplayNum(), ProgramInfo.ALL_RADIO_TYPE);
//            if(program != null)
//                fillString(zText, program.getDisplayName()+" ,LCN="+program.getDisplayNum());
//        }
//        //Log.d(TAG, "out_str="+zText.toString());
//        TvProgramInfo.setText(zText.toString());
//
//
//        //showInfo(itemIndex);
//        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
//        setCheckedResult(itemIndex, true);
//    }

//    public void BtnProgramInfoGetByChnum_OnClick(View view){
//        //ProgramInfo ProgramInfoGetByChnum(int chnum,int type)
//        Log.d(TAG,"BtnProgramInfoGetByChnum_OnClick:");
//        int itemIndex = Items.GetByChnum.getValue();
//        //String tv_str = "ProgramInfoGetByChnum(2, ALL_TV_TYPE):\n"
//        //        + "Get tv program by chNum=2\n=>";
//        //String radio_str = "ProgramInfoGetByChnum(2, ALL_RADIO_TYPE):\n"
//        //        + "Get radio program by chNum=2\n=>";
//        String str_function[]={
//                "(1)ProgramInfoGetByChnum(2, ALL_TV_TYPE):\nGet program by chNum=2\n=>",
//                "(2)ProgramInfoGetByChnum(2, TV_FAV1_TYPE):\nGet program by chNum=2\n=>",
//                "(3)ProgramInfoGetByChnum(TV_FAV1_TYPE):\nGet program by chNum=2\n=>",
//                "(4)ProgramInfoGetByChnum(TV_FAV1_TYPE):\nGet program by chNum=2\n=>",
//                "(5)ProgramInfoGetByChnum(TV_FAV1_TYPE):\nGet program by chNum=2\n=>",
//                "(6)ProgramInfoGetByChnum(TV_FAV1_TYPE):\nGet program by chNum=2\n=>",
//                "(7)ProgramInfoGetByChnum(TV_FAV1_TYPE):\nGet program by chNum=2\n=>",
//                "(8)ProgramInfoGetByChnum(ALL_RADIO_TYPE):\nGet program by chNum=2\n=>",
//                "(9)ProgramInfoGetByChnum(RADIO_FAV1_TYPE):\nGet program by chNum=2\n=>",
//                "(10)ProgramInfoGetByChnum(RADIO_FAV2_TYPE):\nGet program by chNum=2\n=>",
//        };
//        StringBuilder zText = new StringBuilder ();
//
//        //show chnum=2's tv/radio program
//        for(int i = ProgramInfo.ALL_TV_TYPE; i< ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++){
//            ProgramInfo program = ProgramInfoGetByChnum(2, i);
//            fillString(zText, str_function[i]);
//            if(program == null){
//                fillString(zText, "There is no chnum=2's program.\n");
//                fillString(zText, "\n");
//            }else{
//                fillString(zText, program.getDisplayName()+" ,LCN="+program.getDisplayNum());
//                fillString(zText, "\n\n");
//            }
//        }
//        Log.d(TAG, "out_str="+zText.toString());
//        TvProgramInfo.setText(zText.toString());
//
//
//        //showInfo(itemIndex);
//        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
//        setCheckedResult(itemIndex, true);
//    }

    public void BtnProgramInfoGetByChannelId_OnClick(View view){
        //ProgramInfo ProgramInfoGetByChannelId(long channelId)
        Log.d(TAG,"BtnProgramInfoGetByChannelId_OnClick:");
        int itemIndex = Items.GetByChannelId.getValue();
        long TestChannelId = -1;//4981746;   //-1: find first program of tv list
        String str = String.format("ProgramInfoGetByChannelId(long channelId):\n");
        StringBuilder zText = new StringBuilder ();
        ProgramInfo program;
        Boolean result=false;
        List<ProgramInfo> pList = new ArrayList<>();

        if(TestChannelId == -1) {
            pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            if (pList != null)
                TestChannelId = pList.get(0).getChannelId();//find first program of tv list
        }
        program = ProgramInfoGetByChannelId(TestChannelId);

        fillString(zText, "Info:\n");
        fillString(zText, str);
        fillString(zText, "ChannelId="+TestChannelId+"\n=>");
        if(program == null){
            fillString(zText, "There is no channelId="+TestChannelId+" program!!!");
        }else{
            fillString(zText, String.format("["+program.getDisplayNum()+"]"+program.getDisplayName()));
            result = true;
        }
        TvProgramInfo.setText(zText.toString());

        //set test result
        if(result == true) {
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex] + "Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex] + "Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    public void BtnProgramInfoGetSimpleChannelByChannelId_OnClick(View view){
        //SimpleChannel ProgramInfoGetSimpleChannelByChannelId(long channelId)
        Log.d(TAG,"BtnProgramInfoGetSimpleChannelByChannelId_OnClick:");
        int itemIndex = Items.GetSimpleChannelByChannelId.getValue();
        long TestChannelId = -1;//4981746;   //-1: find first program of tv list
        String str = String.format("ProgramInfoGetSimpleChannelByChannelId(long channelId):\n");
        StringBuilder zText = new StringBuilder ();
        SimpleChannel program;
        Boolean result=false;
        List<ProgramInfo> pList = new ArrayList<>();

        if(TestChannelId == -1) {
            pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
            if (pList != null)
                TestChannelId = pList.get(0).getChannelId();//find first program of tv list
        }
        program = GetSimpleProgramByChannelIdfromTotalChannelListByGroup(ViewHistory.getCurGroupType(), TestChannelId);

        fillString(zText, "Info:\n");
        fillString(zText, str);
        fillString(zText, "ChannelId="+TestChannelId+"\n=>");
        if(program == null){
            fillString(zText, "There is no channelId="+TestChannelId+" program!!!");
        }else{
            fillString(zText, String.format("["+program.getChannelNum()+"]"+program.getChannelName()));
            result = true;
        }
        TvProgramInfo.setText(zText.toString());

        //set test result
        if(result == true) {
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex] + "Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex] + "Fail");
            setCheckedResult(itemIndex, false);
        }
    }



//    public void BtnProgramInfoSave_OnClick(View view){
//        //ProgramInfoSave(ProgramInfo program)
//        Log.d(TAG,"BtnProgramInfoSave_OnClick:");
//        int itemIndex = Items.Save.getValue();
//        int tv_cnt=0;
//        int radio_cnt=0;
//
//        List<ProgramInfo> pList = new ArrayList<>();
//        ProgramInfo newTvProgram = new ProgramInfo();
//        ProgramInfo newRadioProgram = new ProgramInfo();
//        String newTvName=String.format("TV-%d", mcnt);
//        String newRadioName=String.format("Radio-%d", mcnt);
//        String tv_str = "(1)TV Test: ProgramInfoSave(ProgramInfo program):\n"
//                + "Save one tv program(chName:"+newTvName+") to end of tv list\n=>";
//        String radio_str = "(2)Radio Test: ProgramInfoSave(ProgramInfo program):\n"
//                + "Save one radio program(chName:"+newRadioName+") to end of radio list\n=>";
//        StringBuilder zText = new StringBuilder ();
//        fillString(zText, "Info:\n");
//
//        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
//        if(pList == null){
//            tv_cnt = 0;
//        }else{
//            tv_cnt = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE).size();
//        }
//        newTvProgram.setType(ProgramInfo.ALL_TV_TYPE);
//        newTvProgram.setDisplayName(newTvName);
//        newTvProgram.setDisplayNum(tv_cnt+1);
//        newTvProgram.setTransportStreamId(7788);
//        newTvProgram.setOriginalNetworkId(8877);
//        newTvProgram.setServiceId(mSidcnt);
//        newTvProgram.setTpId(0);
//        newTvProgram.setChannelId((newTvProgram.getTpId() << 16) | newTvProgram.getServiceId());
//        mSidcnt++;
//
//        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
//        if(pList == null){
//            radio_cnt = 0;
//        }else{
//            radio_cnt = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE).size();
//        }
//        newRadioProgram.setType(ProgramInfo.ALL_RADIO_TYPE);
//        newRadioProgram.setDisplayName(newRadioName);
//        newRadioProgram.setDisplayNum(radio_cnt+1);
//        newRadioProgram.setTransportStreamId(7788);
//        newRadioProgram.setOriginalNetworkId(8877);
//        newRadioProgram.setServiceId(mSidcnt);
//        newTvProgram.setTpId(0);
//        newTvProgram.setChannelId((newTvProgram.getTpId() << 16) | newTvProgram.getServiceId());
//        mSidcnt++;
//
//        //Test
//        ProgramInfoSave(newTvProgram);
//        ProgramInfoSave(newRadioProgram);
//
//        if(pList != null)
//            pList.clear();
//        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
//        fillString(zText, tv_str);
//        if(pList == null){
//            fillString(zText, "list is empty.\n");
//        }else{
//            for(int i=0; i< pList.size(); i++)
//                fillString(zText, String.format("["+pList.get(i).getDisplayNum()+"]"+pList.get(i).getDisplayName()+", "));
//        }
//
//        if(pList != null) {
//            pList.clear();
//        }
//        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
//        fillString(zText, "\n\n"+radio_str);
//        if(pList == null){
//            fillString(zText, "list is empty.\n");
//        }else{
//            for(int i=0; i< pList.size(); i++)
//                fillString(zText, String.format("["+pList.get(i).getDisplayNum()+"]"+pList.get(i).getDisplayName()+", "));
//        }
//        Log.d(TAG, "out_str="+zText.toString());
//        TvProgramInfo.setText(zText.toString());
//
//
//        if(checkSaveResult(tv_cnt, radio_cnt, newTvName, newRadioName)==true){
//            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
//            setCheckedResult(itemIndex, true);
//        }else{
//            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
//            setCheckedResult(itemIndex, false);
//        }
//        mcnt++;
//    }

//    private boolean checkSaveResult(int tv_cnt, int radio_cnt, String newTvName, String newRadioName){
//        List<ProgramInfo> pList = new ArrayList<>();
//
//        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
//        if(pList != null) {
//            if (pList.size() != tv_cnt + 1) {//check size
//                Log.d(TAG, "checkSaveResult: BK1 pList.size()=" + pList.size() + ", tv_cnt=" + tv_cnt);
//                return false;
//            }
//            if (pList.get(pList.size() - 1).getDisplayName().equals(newTvName) == false) {//check chName
//                Log.d(TAG, "checkSaveResult: BK2 pList.name=" + pList.get(pList.size() - 1).getDisplayName() + ", newTvName=" + newTvName);
//                return false;
//            }
//        }else{
//            Log.d(TAG, "checkSaveResult: BK2-1 pList=null");
//            return false;
//        }
//
//        if(pList != null)
//        pList.clear();
//        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
//        if(pList != null) {
//            if (pList.size() != radio_cnt + 1) {//check size
//                Log.d(TAG, "checkSaveResult: BK3 pList.size()=" + pList.size() + ", radio_cnt=" + radio_cnt);
//                return false;
//            }
//            if (pList.get(pList.size() - 1).getDisplayName().equals(newRadioName) == false) {//check chName
//                Log.d(TAG, "checkSaveResult: BK4 pList.name=" + pList.get(pList.size() - 1).getDisplayName() + ", newTvName=" + newRadioName);
//                return false;
//            }
//        }else{
//            Log.d(TAG, "checkSaveResult: BK4-1 pList=null");
//            return false;
//        }
//        return true;
//    }

//    public void BtnProgramInfoSaveList_OnClick(View view){
//        //ProgramInfoSaveList(List<ProgramInfo> programList)
//        Log.d(TAG,"BtnProgramInfoSaveList_OnClick:");
//        int itemIndex = Items.SaveList.getValue();
//        int TEST_CNT=100;
//        //int tv_cnt=0;
//        //int radio_cnt=0;
//        List<ProgramInfo> pTvList = new ArrayList<>();
//        List<ProgramInfo> pRadioList = new ArrayList<>();
//        List<ProgramInfo> ptmpList = new ArrayList<>();
//        ProgramInfo newTvProgram = null;
//        ProgramInfo newRadioProgram = null;
//        String newTvName=null;
//        String newRadioName=null;
//        String tv_str = "(1)TV Test: ProgramInfoSaveList(program list):\n"
//                + "Save 100 programs to tv list\n=>";
//        String radio_str = "(2)Radio Test: ProgramInfoSaveList(program list):\n"
//                + "Save 100 programs to radio list\n=>";
//        StringBuilder zText = new StringBuilder ();
//
//        //tv_cnt = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE).size();
//        //radio_cnt = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE).size();
//        if(pTvList != null)
//            pTvList.clear();
//        if(pRadioList != null)
//            pRadioList.clear();
//
//        mSidcnt=1;
//        for(int i=1; i<=TEST_CNT; i++) {
//            newTvProgram = new ProgramInfo();
//            newRadioProgram = new ProgramInfo();
//
//            newTvName=String.format("LTV-%d", i);
//            newTvProgram.setType(ProgramInfo.ALL_TV_TYPE);
//            newTvProgram.setDisplayName(newTvName);
//            newTvProgram.setDisplayNum(i);
//            //newTvProgram.setLCN(i);
//            newTvProgram.setTransportStreamId(7788);
//            newTvProgram.setOriginalNetworkId(8877);
//            newTvProgram.setServiceId(mSidcnt);
//            newTvProgram.setTpId(1);
//            newTvProgram.setChannelId((newTvProgram.getTpId() << 16) | newTvProgram.getServiceId());
//            mSidcnt++;
//            pTvList.add(newTvProgram);
//
//            newRadioName=String.format("LRadio-%d", i);
//            newRadioProgram.setType(ProgramInfo.ALL_RADIO_TYPE);
//            newRadioProgram.setDisplayName(newRadioName);
//            newRadioProgram.setDisplayNum(i);
//            //newRadioProgram.setLCN(600+i);
//            newRadioProgram.setTransportStreamId(7788);
//            newRadioProgram.setOriginalNetworkId(8877);
//            newRadioProgram.setServiceId(mSidcnt);
//            newRadioProgram.setTpId(1);
//            newRadioProgram.setChannelId((newRadioProgram.getTpId() << 16) | newRadioProgram.getServiceId());
//            mSidcnt++;
//            pRadioList.add(newRadioProgram);
//        }
//
//        //Test
//        ProgramInfoSaveList(pTvList);
//        ProgramInfoSaveList(pRadioList);
//
//        ptmpList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
//        fillString(zText, "Info:\n");
//        fillString(zText, tv_str);
//        if(ptmpList == null){
//            fillString(zText, "list is empty.\n");
//        }else{
//            for(int i=0; i< ptmpList.size(); i++) {
//                fillString(zText, "[" + ptmpList.get(i).getDisplayNum() + "]" + ptmpList.get(i).getDisplayName() + ", ");
//                //Log.d(TAG, "[" + ptmpList.get(i).getChNum() + "]" + ptmpList.get(i).getChName() + " ,");
//            }
//        }
//
//        if(ptmpList != null)
//            ptmpList.clear();
//        ptmpList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
//        fillString(zText, "\n\n"+radio_str);
//        if(ptmpList == null){
//            fillString(zText, "list is empty.\n");
//        }else{
//            for(int i=0; i< ptmpList.size(); i++)
//                fillString(zText, String.format("["+ptmpList.get(i).getDisplayNum()+"]"+ptmpList.get(i).getDisplayName()+", "));
//        }
//        TvProgramInfo.setText(zText.toString());
//        //Log.d(TAG, "out_str="+out_str);
//
//        if(checkSaveListResult(TEST_CNT, pTvList, pRadioList)==true){
//            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
//            setCheckedResult(itemIndex, true);
//        }else{
//            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
//            setCheckedResult(itemIndex, false);
//        }
//    }

//    private boolean checkSaveListResult(int test_cnt, List<ProgramInfo> tvList, List<ProgramInfo> radioList){
//        List<ProgramInfo> pList = new ArrayList<>();
//
//        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
//        if(pList != null) {
//            if (pList.size() != test_cnt) {//check size
//                Log.d(TAG, "checkSaveListResult: BK1 pList.size()=" + pList.size() + ", test_cnt=" + test_cnt);
//                return false;
//            } else {
//                for (int i = 0; i < test_cnt; i++) {
//                    if (pList.get(i).getDisplayName().equals(tvList.get(i).getDisplayName()) == false) {//check chName
//                        Log.d(TAG, "checkSaveListResult: BK2 pList.name=" + pList.get(i).getDisplayName() + ", newTvName=" + tvList.get(i).getDisplayName());
//                        return false;
//                    }
//                }
//            }
//        }else{
//            Log.d(TAG, "checkSaveListResult: BK2-1 pList=null");
//            return false;
//        }
//
//        if(pList != null)
//        pList.clear();
//        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
//        if(pList != null) {
//            if (pList.size() != test_cnt) {//check size
//                Log.d(TAG, "checkSaveListResult: BK3 pList.size()=" + pList.size() + ", test_cnt=" + test_cnt);
//                return false;
//            } else {
//                for (int i = 0; i < test_cnt; i++) {
//                    if (pList.get(i).getDisplayName().equals(radioList.get(i).getDisplayName()) == false) {//check chName
//                        Log.d(TAG, "checkSaveListResult: BK4 pList.name=" + pList.get(i).getDisplayName() + ", newRadioName=" + radioList.get(i).getDisplayName());
//                        return false;
//                    }
//                }
//            }
//        }else{
//            Log.d(TAG, "checkSaveListResult: BK4-1 pList=null");
//            return false;
//        }
//
//        return true;
//    }

    public void BtnProgramInfoSaveSimpleList_OnClick(View view){
        //ProgramInfoSaveList(List<SimpleChannel> programList,int type)
        Log.d(TAG,"BtnProgramInfoSaveSimpleList_OnClick:");
        int itemIndex = Items.SaveSimpleList.getValue();
        List<SimpleChannel> pTvList = new ArrayList<>();
        List<SimpleChannel> pRadioList = new ArrayList<>();
        List<SimpleChannel> ptmpTvList = new ArrayList<>();
        List<SimpleChannel> ptmpRadioList = new ArrayList<>();
        String tv_str = "(1)TV Test: ProgramInfoSaveList(List<SimpleChannel> programList,int type):\n"
                + "modify ch name to SList... of all tv list\n=>";
        String radio_str = "(2)Radio Test: ProgramInfoSaveList(List<SimpleChannel> programList,int type):\n"
                + "modify ch name to SList... of all radio list\n=>";
        StringBuilder zText = new StringBuilder ();
        int i=0;

        //TV
        pTvList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if(pTvList != null ){
            for(i=0; i< pTvList.size(); i++){
                pTvList.get(i).setChannelName(String.format("SListTV-%d", (i+1)));//change ch name
            }
        }
        //Radio
        pRadioList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_RADIO_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if(pRadioList != null ){
            for(i=0; i< pRadioList.size(); i++){
                pRadioList.get(i).setChannelName(String.format("SListRadio-%d", (i+1)));//change ch name
            }
        }
        ProgramInfoUpdateList(pTvList, ProgramInfo.ALL_TV_TYPE);
        ProgramInfoUpdateList(pRadioList, ProgramInfo.ALL_RADIO_TYPE);

        //show result
        fillString(zText, "Info:\n");
        fillString(zText, tv_str);
        //TV
        ptmpTvList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if(ptmpTvList != null ){
            for(i=0; i< ptmpTvList.size(); i++){
                fillString(zText, "[" + ptmpTvList.get(i).getChannelNum() + "]" + ptmpTvList.get(i).getChannelName() + ", ");
            }
        }else
            fillString(zText, "list is empty.\n");
        //Radio
        ptmpRadioList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_RADIO_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        fillString(zText, "\n\n");
        fillString(zText, radio_str);
        if(ptmpRadioList != null ){
            for(i=0; i< ptmpRadioList.size(); i++){
                fillString(zText, "[" + ptmpRadioList.get(i).getChannelNum() + "]" + ptmpRadioList.get(i).getChannelName() + ", ");
            }
        }else
            fillString(zText, "list is empty.\n");
        TvProgramInfo.setText(zText.toString());

        //check result
        if(checkSaveSimpleListResult(pTvList, pRadioList)==true){
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    private boolean checkSaveSimpleListResult(List<SimpleChannel> tvList, List<SimpleChannel> radioList){
        List<SimpleChannel> tmpList = new ArrayList<>();
        List<SimpleChannel> comList = new ArrayList<>();
        int i=0,j=0;
        int tv_cnt=0, radio_cnt=0;

        Log.d(TAG, "BBK4");//eric lin test
        for(i=0; i<2; i++) {
            if(i==0) {
                tmpList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                comList = tvList;
            }
            else if(i==1) {
                tmpList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_RADIO_TYPE,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                comList = radioList;
            }
            if (tmpList != null) {
                if(i==0)
                    tv_cnt = comList.size();
                else if(i==1)
                    radio_cnt = comList.size();
                if (tmpList.size() != comList.size()) {//check size
                    Log.d(TAG, "checkSaveSimpleListResult: BK1 i="+i);
                    Log.d(TAG, "checkSaveSimpleListResult: BK1 tmpList.size()=" + tmpList.size() + ", comList.size()=" + comList.size());
                    return false;
                } else {
                    for (j = 0; j < tmpList.size(); j++) {
                        if (tmpList.get(j).getChannelName().equals(comList.get(j).getChannelName()) == false) {//check chName
                            Log.d(TAG, "checkSaveSimpleListResult: BK2 i="+i+", j="+j);
                            Log.d(TAG, "checkSaveSimpleListResult: BK2 tmpList.name=" + tmpList.get(j).getChannelName() + ", comList.name=" + comList.get(j).getChannelName());
                            return false;
                        }
                    }
                }
            } else {
                Log.d(TAG, "checkSaveSimpleListResult: BK2-1 pList=null, i="+i);
                if(i==0)
                    tv_cnt = 0;
                else if(i==1)
                    radio_cnt = 0;
            }
        }

        if(tv_cnt==0 && radio_cnt==0) {
            return false;
        }

        return true;
    }


    public void BtnDeleteNotReSortChnumByChId_OnClick(View view){
        //int ProgramInfoDeleteNotReSortChannelId(long channelId)
        Log.d(TAG,"BtnDeleteNotReSortChnumByChId_OnClick:");
        int itemIndex = Items.DeleteNotReSortChnumByChId.getValue();
        int tv_cnt=0;
        int radio_cnt=0;
        List<ProgramInfo> pList = new ArrayList<>();
        List<ProgramInfo> pTvList = new ArrayList<>();
        List<ProgramInfo> pRadioList = new ArrayList<>();
        ProgramInfo deletedTvProgram = null;
        ProgramInfo deletedRadioProgram = null;
        String tv_str = "(1)TV Test: ProgramInfoDeleteNotReSortChnum(long channelId):"
                + "\nDelete first tv program\n=>";
        String radio_str = "(2)Radio Test: ProgramInfoDeleteNotReSortChnum(long channelId):"
                + "\nDelete first radio program\n=>";
        StringBuilder zText = new StringBuilder ();


        pTvList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        if(pTvList != null){
            tv_cnt = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE).size();
            deletedTvProgram = pTvList.get(0);
        }else{
            tv_cnt = 0;
        }
        pRadioList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
        if(pRadioList != null){
            radio_cnt = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE).size();
            deletedRadioProgram = pRadioList.get(0);
        }else{
            radio_cnt = 0;
        }

        //Test
        if(tv_cnt >0) {
            Log.d(TAG, "BBK1 chId="+deletedTvProgram.getChannelId());
            ProgramInfoDeleteNotReSortChannelId(deletedTvProgram.getChannelId());
            //ProgramInfoDeleteNotReSortChnum(deletedTvProgram.getServiceId(), deletedTvProgram.getTransportStreamId(), deletedTvProgram.getOriginalNetworkId());
        }
        if(radio_cnt >0) {
            Log.d(TAG, "BBK2 chId="+deletedRadioProgram.getChannelId());
            ProgramInfoDeleteNotReSortChannelId(deletedRadioProgram.getChannelId());
            //ProgramInfoDeleteNotReSortChnum(deletedRadioProgram.getServiceId(), deletedRadioProgram.getTransportStreamId(), deletedRadioProgram.getOriginalNetworkId());
        }

        //show
        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        fillString(zText, tv_str);
        if(pList == null){
            fillString(zText, "list is empty.\n");
        }else{
            for(int i=0; i< pList.size(); i++)
                fillString(zText, String.format("["+pList.get(i).getDisplayNum()+"]"+pList.get(i).getDisplayName()+", "));
        }

        if(pList != null)
            pList.clear();
        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
        fillString(zText, "\n\n"+radio_str);
        if(pList == null){
            fillString(zText, "list is empty.\n");
        }else{
            for(int i=0; i< pList.size(); i++)
                fillString(zText, String.format("["+pList.get(i).getDisplayNum()+"]"+pList.get(i).getDisplayName()+", "));
        }
        Log.d(TAG, "out_str="+zText.toString());
        TvProgramInfo.setText(zText.toString());

        if(checkDeleteNotReSortChnumByChIdResult(tv_cnt, radio_cnt, deletedTvProgram, deletedRadioProgram)==true){
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    private boolean checkDeleteNotReSortChnumByChIdResult(int tv_cnt, int radio_cnt, ProgramInfo tv_program, ProgramInfo radio_program){
        List<ProgramInfo> pList = new ArrayList<>();

        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        if(pList != null) {
            if (pList.size() != tv_cnt - 1) {//check size
                Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK1 pList.size()=" + pList.size() + ", tv_cnt=" + tv_cnt);
                return false;
            }
            for (int i = 0; i < pList.size(); i++) {
                if (pList.get(i).getChannelId() == tv_program.getChannelId()) {
                    Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK2 s_id=" + pList.get(i).getOriginalNetworkId());
                    return false;
                }
            }
        }else{
            if(tv_cnt == 1 ||tv_cnt == 0){
                Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK2-1 pList=null, but tv_cnt= 0 or 1");
            }else {
                Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK2-2 pList=null");
                return false;
            }
        }

        if(pList != null)
            pList.clear();
        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
        if(pList != null) {
            if (pList.size() != radio_cnt - 1) {//check size
                Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK3 pList.size()=" + pList.size() + ", radio_cnt=" + radio_cnt);
                return false;
            }
            for (int i = 0; i < pList.size(); i++) {
                if (pList.get(i).getChannelId() == radio_program.getChannelId() ) {
                    Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK4 s_id=" + pList.get(i).getOriginalNetworkId());
                    return false;
                }
            }
        }else{
            if(radio_cnt == 1 || radio_cnt == 0){
                Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK4-1 pList=null, but radio_cnt=0 or 1");
            }else {
                Log.d(TAG, "checkDeleteNotReSortChnumByChIdResult: BK4-1 pList=null");
                return false;
            }
        }
        return true;
    }

    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }


    //below is mark part


    /*
    public void BtnProgramInfoDeleteNotReSortChnum_OnClick(View view){
        //ProgramInfoDeleteNotReSortChnum(int sid, int tsid, int onid)
        Log.d(TAG,"BtnProgramInfoDeleteNotReSortChnum_OnClick:");
        int itemIndex = Items.DeleteNotReSortChnum.getValue();
        int tv_cnt=0;
        int radio_cnt=0;
        List<ProgramInfo> pList = new ArrayList<>();
        List<ProgramInfo> pTvList = new ArrayList<>();
        List<ProgramInfo> pRadioList = new ArrayList<>();
        ProgramInfo deletedTvProgram = null;
        ProgramInfo deletedRadioProgram = null;
        String newTvName=String.format("TV-%d", mcnt);
        String newRadioName=String.format("Radio-%d", mcnt);
        String tv_str = "\nTV Test: ProgramInfoDeleteNotReSortChnum(int sid, int tsid, int onid):"
                + "\nDelete first tv program\n=>";
        String radio_str = "\n\nRadio Test: ProgramInfoDeleteNotReSortChnum(int sid, int tsid, int onid):"
                + "\nDelete first radio program\n=>";
        StringBuilder zText = new StringBuilder ();


        pTvList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        if(pTvList != null){
            tv_cnt = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE).size();
            deletedTvProgram = pTvList.get(0);
        }else{
            tv_cnt = 0;
        }
        pRadioList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
        if(pRadioList != null){
            radio_cnt = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE).size();
            deletedRadioProgram = pRadioList.get(0);
        }else{
            radio_cnt = 0;
        }

        //Test
        if(tv_cnt >0) {
            ProgramInfoDeleteNotReSortChannelId(deletedTvProgram.getChannelId());
            //ProgramInfoDeleteNotReSortChnum(deletedTvProgram.getServiceId(), deletedTvProgram.getTransportStreamId(), deletedTvProgram.getOriginalNetworkId());
        }
        if(radio_cnt >0) {
            ProgramInfoDeleteNotReSortChannelId(deletedRadioProgram.getChannelId());
            //ProgramInfoDeleteNotReSortChnum(deletedRadioProgram.getServiceId(), deletedRadioProgram.getTransportStreamId(), deletedRadioProgram.getOriginalNetworkId());
        }

        //show
        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        fillString(zText, tv_str);
        if(pList == null){
            fillString(zText, "list is empty.\n");
        }else{
            for(int i=0; i< pList.size(); i++)
                fillString(zText, String.format("["+pList.get(i).getDisplayNum()+"]"+pList.get(i).getDisplayName()+" ,"));
        }

        if(pList != null)
            pList.clear();
        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
        fillString(zText, "\n"+radio_str);
        if(pList == null){
            fillString(zText, "list is empty.\n");
        }else{
            for(int i=0; i< pList.size(); i++)
                fillString(zText, String.format("["+pList.get(i).getDisplayNum()+"]"+pList.get(i).getDisplayName()+" ,"));
        }
        Log.d(TAG, "out_str="+zText.toString());
        TvProgramInfo.setText(zText.toString());

        if(checkProgramInfoDeleteNotReSortChnumResult(tv_cnt, radio_cnt, deletedTvProgram, deletedRadioProgram)==true){
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    private boolean checkProgramInfoDeleteNotReSortChnumResult(int tv_cnt, int radio_cnt, ProgramInfo tv_program, ProgramInfo radio_program){
        List<ProgramInfo> pList = new ArrayList<>();

        pList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        if(pList != null) {
            if (pList.size() != tv_cnt - 1) {//check size
                Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK1 pList.size()=" + pList.size() + ", tv_cnt=" + tv_cnt);
                return false;
            }
            for (int i = 0; i < pList.size(); i++) {
                if (pList.get(i).getServiceId() == tv_program.getServiceId()
                        && pList.get(i).getTransportStreamId() == tv_program.getTransportStreamId()
                        && pList.get(i).getOriginalNetworkId() == tv_program.getOriginalNetworkId()) {
                    Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK2 s_id=" + pList.get(i).getOriginalNetworkId());
                    return false;
                }
            }
        }else{
            if(tv_cnt == 1 ||tv_cnt == 0){
                Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK2-1 pList=null, but tv_cnt= 0 or 1");
            }else {
                Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK2-2 pList=null");
                return false;
            }
        }

        if(pList != null)
            pList.clear();
        pList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);
        if(pList != null) {
            if (pList.size() != radio_cnt - 1) {//check size
                Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK3 pList.size()=" + pList.size() + ", radio_cnt=" + radio_cnt);
                return false;
            }
            for (int i = 0; i < pList.size(); i++) {
                if (pList.get(i).getServiceId() == radio_program.getServiceId()
                        && pList.get(i).getTransportStreamId() == radio_program.getTransportStreamId()
                        && pList.get(i).getOriginalNetworkId() == radio_program.getOriginalNetworkId()) {
                    Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK4 s_id=" + pList.get(i).getOriginalNetworkId());
                    return false;
                }
            }
        }else{
            if(radio_cnt == 1 || radio_cnt == 0){
                Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK4-1 pList=null, but radio_cnt=0 or 1");
            }else {
                Log.d(TAG, "checkProgramInfoDeleteNotReSortChnumResult: BK4-1 pList=null");
                return false;
            }
        }
        return true;
    }
    */
}
