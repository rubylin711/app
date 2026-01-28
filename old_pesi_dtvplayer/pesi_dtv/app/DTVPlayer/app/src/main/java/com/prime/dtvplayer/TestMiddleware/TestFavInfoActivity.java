package com.prime.dtvplayer.TestMiddleware;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestFavInfoActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private List<ProgramManagerImpl> ProgramManagerList = null;
    private enum Items {
        //GetProgram(0),
        //GetProgramList(1),
        //Save(1),
        //SaveListTwoParam(2),
        SaveListOneParam(0),
        //Delete(1),
        DeleteAll(1),
        Max(2);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (TestFavInfoActivity.Items testItem : TestFavInfoActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static TestFavInfoActivity.Items valueOf(int testItem) {
            return (TestFavInfoActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private static final int[] BUTTON_IDS = {
            R.id.BtnTestFavInfo1,
            R.id.BtnTestFavInfo2,
            //R.id.BtnTestFavInfo3,
            //R.id.BtnTestFavInfo4,
            //R.id.BtnTestFavInfo5,
            //R.id.BtnTestFavInfo6,
            //R.id.BtnTestFavInfo7,
    };
    private static final String[] ITEM_NAME = {
            //"FavInfoGetProgram(int favMode,int index)\nResult:",
            //"FavInfoGetProgramList(int favMode)\nResult:",
            //"FavInfoSave(ProgramInfo program, int favMode, int index)\nResult:",
            //"FavInfoSaveList(List <ProgramInfo> programList, int favMode)\nResult:",
            "FavInfoSaveList(List <FavInfo> favList)\nResult:",
            //"FavInfoDelete(int favMode, int index)\nResult:",
            "FavInfoDeleteAll(int favMode)\nResult:",
    };
    int mTestResult=0;//
    TestMidMain tm = new TestMidMain();//
    private TextView TvFavInfo=null;
    private static final int TESTITEM_NUM = Items.Max.getValue(); //7;//
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    private List<ProgramInfo> mTvList = new ArrayList<>();
    private List<ProgramInfo> mRadioList = new ArrayList<>();
    private List<ProgramInfo> mTestTvList = new ArrayList<>();
    private List<ProgramInfo> mTestRadioList = new ArrayList<>();
    private static int mAllTvCnt=0;
    private static int mAllRadioCnt=0;
    int mtestTvCnt=2;
    int mtestRadioCnt=2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fav_info);

        //Buttons
        buttons = new ArrayList<Button>();
        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);
            //button.setOnClickListener(this); // maybe
            buttons.add(button);
        }
        for(int i=Items.SaveListOneParam.getValue(); i< Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        //TextView
        TvFavInfo = (TextView)this.findViewById(R.id.TvTestFavInfo);
        TvFavInfo.setText("Info:");
        TvFavInfo.setMovementMethod(new ScrollingMovementMethod());
        TvFavInfo.scrollTo(0,0);

        //Get TV/Radio list and size
        getTvRadioList();

    }

    private void getTvRadioList(){
        mTvList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        mRadioList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);

        if(mTvList == null)
            Log.d(TAG, "getTvRadioList: TvList=null");
        else {
            Log.d(TAG, "getTvRadioList: TvList cnt=" + mTvList.size());
            mAllTvCnt = mTvList.size();
        }
        if(mRadioList == null)
            Log.d(TAG, "getTvRadioList: RadioList=null");
        else {
            Log.d(TAG, "getTvRadioList: RadioList cnt=" + mRadioList.size());
            mAllRadioCnt = mRadioList.size();
        }
    }

    private void setCheckedResult(int ItemIndex, boolean testResult){
        int allchecked=0;
        int itemresult=0;
        Bundle bundle =this.getIntent().getExtras();//need

        SubItemChecked[ItemIndex] = true;
        //if(test_ok==false)
        mTestResult = tm.bitwiseLeftShift(mTestResult, ItemIndex, testResult);
        for(int i=0; i< TESTITEM_NUM; i++)
        {
            if(SubItemChecked[i]==true)
                allchecked++;
        }
        if(allchecked == TESTITEM_NUM) {
            tm.getTestInfoByIndex(bundle.getInt("position")).setChecked(true);
            tm.getTestInfoByIndex(bundle.getInt("position")).setResult(mTestResult);
        }

    }

    /*
    public void BtnFavInfoGetProgram_OnClick(View view){
        //ProgramInfo FavInfoGetProgram(int favMode,int index)
        Log.d(TAG,"BtnFavInfoGetProgram_OnClick:");
        int itemIndex = Items.GetProgram.getValue();

        showInfo(itemIndex);
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }

    public void BtnFavInfoGetProgramList_OnClick(View view){
        //List<ProgramInfo> FavInfoGetProgramList(int favMode)
        Log.d(TAG,"BtnFavInfoGetProgramList_OnClick:");
        int itemIndex = Items.GetProgramList.getValue();

        //showInfo(itemIndex);
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }
    */


    /*
    public void BtnFavInfoSave_OnClick(View view){
        //void FavInfoSave(SimpleChannel channel, int GroupType, int position)
        Log.d(TAG,"BtnFavInfoSave_OnClick:");
        int itemIndex = Items.Save.getValue();
        int testTvChnum=1;
        int testRadioChnum=1;
        int testFavPosition=1;

        //start test
        if(mAllTvCnt != 0) {
            for (int i = ProgramInfo.TV_FAV1_TYPE; i <= ProgramInfo.TV_FAV6_TYPE; i++)
            {
                Log.d(TAG,"BtnFavInfoSave_OnClick: BK2");
                FavInfoSave(ProgramInfoGetSimpleChannelByChannelId(mTvList.get(testTvChnum).getChannelId()), i, testFavPosition);
                //FavInfoSave(ProgramInfoGetByChnum(testTvChnum, ProgramInfo.ALL_TV_TYPE), i, testFavIndex); //FavInfoSave(TvList.get(0), i, 0);
            }
        }

        if(mAllRadioCnt != 0) {
            for (int i = ProgramInfo.RADIO_FAV1_TYPE; i <= ProgramInfo.RADIO_FAV2_TYPE; i++)
            {
                Log.d(TAG,"BtnFavInfoSave_OnClick: BK3");
                FavInfoSave(ProgramInfoGetSimpleChannelByChannelId(mTvList.get(testRadioChnum).getChannelId()), i, testFavPosition);
                //FavInfoSave(ProgramInfoGetByChnum(testRadioChnum, ProgramInfo.ALL_RADIO_TYPE), i, testFavIndex);//FavInfoSave(RadioList.get(0), i, 0);
            }
        }

        Log.d(TAG,"BtnFavInfoSave_OnClick: BK4");
        //showInfo(itemIndex);
        if(checkSaveResult(testTvChnum, testRadioChnum, testFavPosition)==true){
            Log.d(TAG,"BtnFavInfoSave_OnClick: BK5 OK");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");//BtGetProgram.setText(str_GetProgram +"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            Log.d(TAG,"BtnFavInfoSave_OnClick: BK5 Fail");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");//BtSave.setText(str_GetProgram +"Fail");
            setCheckedResult(itemIndex, false);
        }

    }
    private boolean checkSaveResult(int TvChnum, int RadioChnum, int favIndex){
        ProgramInfo program;
        SimpleChannel tmp = null;

        if(mAllTvCnt==0 && mAllRadioCnt==0){
            Log.d(TAG,"checkSaveResult: mAllTvCnt and mAllRadioCnt both be 0");
            return false;
        }
        if(mAllTvCnt != 0) {
            program = ProgramInfoGetByChnum(TvChnum, ProgramInfo.ALL_TV_TYPE);
            if(program != null) {
                for(int i= ProgramInfo.TV_FAV1_TYPE; i<= ProgramInfo.TV_FAV6_TYPE; i++)
                {
                    tmp=FavInfoGetProgram(i, favIndex);
                    if(tmp==null)
                        return false;
                    if(tmp.getChannelId() != program.getChannelId())
                           // || tmp.getOriginalNetworkId() != program.getOriginalNetworkId()
                           // || tmp.getOriginalNetworkId() != program.getOriginalNetworkId())
                        return false;
                }
            }
            else
                return false;
        }
        if(mAllRadioCnt != 0) {
            program = ProgramInfoGetByChnum(RadioChnum, ProgramInfo.ALL_RADIO_TYPE);
            if(program != null) {
                for(int i= ProgramInfo.RADIO_FAV1_TYPE; i<= ProgramInfo.RADIO_FAV2_TYPE; i++)
                {
                    tmp=FavInfoGetProgram(i, favIndex);
                    if(tmp==null)
                        return false;
                    if(tmp.getChannelId() != program.getChannelId())
                            //|| tmp.getOriginalNetworkId() != program.getOriginalNetworkId()
                           // || tmp.getOriginalNetworkId() != program.getOriginalNetworkId())
                        return false;
                }
            }
            else
                return false;
        }
        return true;
    }
    */

    /*
    public void BtnFavInfoSaveListTwoParam_OnClick(View view){
        //void FavInfoSaveList(List <ProgramInfo> programList, int favMode)
        Log.d(TAG,"BtnFavInfoSaveListTwoParam_OnClick:");
        int itemIndex = Items.SaveListTwoParam.getValue();

        //List<ProgramInfo> programList  = new ArrayList<>();
        //int testFavIndex=0;
        if(mTestTvList != null)
            mTestTvList.clear();
        if(mTestRadioList != null)
            mTestRadioList.clear();

        //start test
        if(mAllTvCnt != 0) {
            for(int i=1; i<=mAllTvCnt; i++){//prepare program list
                mTestTvList.add(ProgramInfoGetByChnum(i, ProgramInfo.ALL_TV_TYPE));
                if(i>=mtestTvCnt)//Just add two program
                    break;
            }
            for (int i = ProgramInfo.TV_FAV1_TYPE; i <= ProgramInfo.TV_FAV6_TYPE; i++)
            {
                Log.d(TAG,"BtnFavInfoSaveListTwoParam_OnClick: BK2");
                //FavInfoSaveList(mTestTvList, i);
            }
        }

        if(mAllRadioCnt != 0) {
            for(int i=1; i<=mAllRadioCnt; i++){//prepare program list
                mTestRadioList.add(ProgramInfoGetByChnum(i, ProgramInfo.ALL_RADIO_TYPE));
                if(i>=mtestRadioCnt)//Just add two program
                    break;
            }
            for (int i = ProgramInfo.RADIO_FAV1_TYPE; i <= ProgramInfo.RADIO_FAV2_TYPE; i++)
            {
                Log.d(TAG,"BtnFavInfoSaveListTwoParam_OnClick: BK3");
               // FavInfoSaveList(mTestRadioList, i);
            }
        }

        showInfo(itemIndex);
        if(checkSaveListResult()==true){
            Log.d(TAG,"BtnFavInfoSaveListTwoParam_OnClick: BK5 OK");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");//BtGetProgram.setText(str_GetProgram +"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            Log.d(TAG,"BtnFavInfoSaveListTwoParam_OnClick: BK5 Fail");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");//BtSave.setText(str_GetProgram +"Fail");
            setCheckedResult(itemIndex, false);
        }
    }
    */

    public void BtnFavInfoSaveListOneParam_OnClick(View view){
        //void FavInfoSaveList(List <FavInfo> favList)
        Log.d(TAG,"BtnFavInfoSaveListOneParam_OnClick:");
        int itemIndex = Items.SaveListOneParam.getValue();
        int i,j;

        if(mTestTvList != null)
            mTestTvList.clear();
        if(mTestRadioList != null)
            mTestRadioList.clear();

        //start test, save all programs to fav list
        if(mAllTvCnt != 0) {
            mTestTvList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);//prepare program list
            for (i = ProgramInfo.TV_FAV1_TYPE; i <= ProgramInfo.TV_FAV6_TYPE; i++)
            {
                Log.d(TAG,"BtnFavInfoSaveListOneParam_OnClick: BK2");
                List<FavInfo> favList = new ArrayList<FavInfo>();
                for (j = 0; j < mTestTvList.size(); j++) {//eric lin a
                    FavInfo fav = new FavInfo();
                    fav.setFavNum(j+1);
                    fav.setFavMode(i);
                    fav.setChannelId(mTestTvList.get(j).getChannelId());
                    favList.add(fav);
                }
                //FavInfoSaveList(favList);
            }
        }

        if(mAllRadioCnt != 0) {
            mTestRadioList = ProgramInfoGetList(ProgramInfo.ALL_RADIO_TYPE);//prepare program list
            for (i = ProgramInfo.RADIO_FAV1_TYPE; i <= ProgramInfo.RADIO_FAV2_TYPE; i++)
            {
                Log.d(TAG,"BtnFavInfoSaveListOneParam_OnClick: BK3");
                List<FavInfo> favList = new ArrayList<FavInfo>();
                for (j = 0; j < mTestRadioList.size(); j++) {//eric lin a
                    FavInfo fav = new FavInfo();
                    fav.setFavNum(j+1);
                    fav.setFavMode(i);
                    fav.setChannelId(mTestRadioList.get(j).getChannelId());
                    favList.add(fav);
                }
                //FavInfoSaveList(favList);
            }
        }

        showInfo(itemIndex);
        if(checkSaveListResult()==true){
            Log.d(TAG,"BtnFavInfoSaveListTwoParam_OnClick: BK5 OK");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");//BtGetProgram.setText(str_GetProgram +"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            Log.d(TAG,"BtnFavInfoSaveListTwoParam_OnClick: BK5 Fail");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");//BtSave.setText(str_GetProgram +"Fail");
            setCheckedResult(itemIndex, false);
        }

    }
    private boolean checkSaveListResult(){
        List<FavInfo> tmpList  = new ArrayList<>();
        boolean find=false;
        //List<FavInfo>  tmpFavList = new ArrayList<>();

        if(mAllTvCnt != 0) {
            if(mTestTvList != null) {
                for(int i= ProgramInfo.TV_FAV1_TYPE; i<= ProgramInfo.TV_FAV6_TYPE; i++){
                    tmpList = super.FavInfoGetList(i);
                    if(tmpList==null || tmpList.size() != mTestTvList.size()) {
                        Log.d(TAG, "checkSaveListResult: BK1");
                        return false;
                    }
                    Log.d(TAG, "checkSaveListResult: BK1_1 groupType="+i);
                    for(int j=0; j<mTestTvList.size(); j++){
                        find=false;
                        Log.d(TAG, "checkSaveListResult: BK2 mTestTvList=" + mTestTvList.get(j).getChannelId());
                        for(int k=0; k<tmpList.size(); k++) {
                            Log.d(TAG, "checkSaveListResult: BK2_1 mTestTvList=" + mTestTvList.get(j).getChannelId() + ", tmpList=" + tmpList.get(k).getChannelId());
                            if ( mTestTvList.get(j).getChannelId() == tmpList.get(k).getChannelId()){ //if(!tmpList.get(j).equals(mTestTvList.get(j)))
                                find = true;
                                break;
                            }
                        }
                        if(find == false)
                            return false;
                    }
                }
            }
            else
                return false;
        }
        if(mAllRadioCnt != 0) {
            if(mTestRadioList != null) {
                for(int i= ProgramInfo.RADIO_FAV1_TYPE; i<= ProgramInfo.RADIO_FAV2_TYPE; i++){
                    tmpList = super.FavInfoGetList(i);
                    if(tmpList==null || tmpList.size() != mTestRadioList.size())
                        return false;
                    for(int j=0; j<mTestRadioList.size(); j++)
                    {
                        find=false;
                        Log.d(TAG, "checkSaveListResult: BK3 mTestRadioList=" + mTestRadioList.get(j).getChannelId());
                        for(int k=0; k<tmpList.size(); k++) {if ( mTestRadioList.get(j).getChannelId() == tmpList.get(k).getChannelId() ){ //if(!tmpList.get(j).equals(mTestTvList.get(j)))
                                find = true;
                                break;
                            }
                        }
                        if(find == false)
                            return false;
                    }
                }
            }
            else
                return false;
        }
        return true;

    }

    /*
    public void BtnFavInfoDelete_OnClick(View view){
        //void FavInfoDelete(int favMode, int index)
        int [] oriFavListCnt = new int [ProgramInfo.ALL_TV_RADIO_TYPE_MAX];
        Log.d(TAG,"BtnFavInfoDelete_OnClick:");
        int itemIndex = Items.Delete.getValue();
        List<ProgramInfo> programList = new ArrayList<>();
        ProgramInfo deletedTvProgram=null;
        ProgramInfo deletedRadioProgram=null;


        //getTvRadioList(); //reget TV/Radio list
        //start test, save all programs to fav list
        for (int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++)
        {
            if(i == ProgramInfo.ALL_TV_TYPE || i== ProgramInfo.ALL_RADIO_TYPE)
                continue;
            if(mTvList == null){
                if(i >= ProgramInfo.TV_FAV1_TYPE && i <= ProgramInfo.TV_FAV6_TYPE)
                    continue;
            }
            if(mRadioList == null){
                Log.d(TAG,"BtnFavInfoDelete_OnClick: BK1 favMode="+i);
                if(i >= ProgramInfo.RADIO_FAV1_TYPE && i <= ProgramInfo.RADIO_FAV2_TYPE)
                    continue;
            }
            programList = FavInfoGetProgramList(i);
            //Log.d(TAG,"BtnFavInfoDelete_OnClick: BK2 favMode="+i);
            if(programList == null){
                Log.d(TAG,"BtnFavInfoDelete_OnClick: BK3 favMode="+i);
                TvFavInfo.setText("\nFavInfoDelete(int favMode, int index) :"
                        +"\nDelete favId=1 for all favList..."
                        +"\nError: favList["+i+"] no fav info.");
                buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");//BtSave.setText(str_GetProgram +"Fail");
                setCheckedResult(itemIndex, false);
                return;
            }
            if(programList.size() != 0)
                oriFavListCnt[i] = programList.size();
            else{
                Log.d(TAG,"Error: fav list != null but size=0");
                buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");//BtSave.setText(str_GetProgram +"Fail");
                setCheckedResult(itemIndex, false);
                return;
            }
        }

        //Start delete favId=1 for all fav list
        for (int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++)
        {
            if(i == ProgramInfo.ALL_TV_TYPE || i== ProgramInfo.ALL_RADIO_TYPE)
                continue;
            if(mTvList == null){
                if(i >= ProgramInfo.TV_FAV1_TYPE && i <= ProgramInfo.TV_FAV6_TYPE)
                    continue;
            }
            if(mRadioList == null){
                if(i >= ProgramInfo.RADIO_FAV1_TYPE && i <= ProgramInfo.RADIO_FAV2_TYPE)
                    continue;
            }
           // if(i >= ProgramInfo.TV_FAV1_TYPE && i <= ProgramInfo.TV_FAV6_TYPE)
            //    deletedTvProgram = FavInfoGetProgram(i,1);
            //else if(i >= ProgramInfo.RADIO_FAV1_TYPE && i <= ProgramInfo.RADIO_FAV2_TYPE)
           //     deletedRadioProgram = FavInfoGetProgram(i,1);

            FavInfoDelete(i, 1);
        }

        showInfo(itemIndex);
        if(checkDeleteResult(oriFavListCnt, deletedTvProgram, deletedRadioProgram)==true){
            Log.d(TAG,"BtnFavInfoDelete_OnClick: BK5 OK");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");//BtGetProgram.setText(str_GetProgram +"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            Log.d(TAG,"BtnFavInfoDelete_OnClick: BK5 Fail");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");//BtSave.setText(str_GetProgram +"Fail");
            setCheckedResult(itemIndex, false);
        }
    }

    private boolean checkDeleteResult(int [] oriFavCnt, ProgramInfo deletedTvProgram, ProgramInfo deletedRadioProgram){
        List<ProgramInfo> tmpList  = new ArrayList<>();
        for (int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++)
        {
            if(i == ProgramInfo.ALL_TV_TYPE || i== ProgramInfo.ALL_RADIO_TYPE)
                continue;
            if(mTvList == null){
                if(i >= ProgramInfo.TV_FAV1_TYPE && i <= ProgramInfo.TV_FAV6_TYPE)
                    continue;
            }
            if(mRadioList == null){
                if(i >= ProgramInfo.RADIO_FAV1_TYPE && i <= ProgramInfo.RADIO_FAV2_TYPE)
                    continue;
            }
            tmpList = FavInfoGetSimpleChannelList(i);
            //tmpList = FavInfoGetProgramList(i);
            if(tmpList != null){
                if(tmpList.size() != oriFavCnt[i]-1) {
                    Log.d(TAG, "checkDeleteResult: (Error) BK2 favMode=" + i + ", tmpList.size()" + tmpList.size() + ", oriFavCnt[i].size()" + oriFavCnt[i]);
                    return false;
                }else{
                    for (int j = 0; j < tmpList.size(); j++) {
                        if (i >= ProgramInfo.TV_FAV1_TYPE && i <= ProgramInfo.TV_FAV6_TYPE) {
                            if (deletedTvProgram != null) {
                                if (tmpList.get(j).getServiceId() == deletedTvProgram.getServiceId()
                                        && tmpList.get(j).getOriginalNetworkId() == deletedTvProgram.getOriginalNetworkId()
                                        && tmpList.get(j).getTransportStreamId() == deletedTvProgram.getTransportStreamId()) {
                                    Log.d(TAG, "checkDeleteResult: (Error) BK90 favMode=" + i);
                                    return false;
                                }
                            }
                        } else if (i >= ProgramInfo.RADIO_FAV1_TYPE && i <= ProgramInfo.RADIO_FAV2_TYPE) {
                            if (deletedRadioProgram != null) {
                                if (tmpList.get(j).getServiceId() == deletedRadioProgram.getServiceId()
                                        && tmpList.get(j).getOriginalNetworkId() == deletedRadioProgram.getOriginalNetworkId()
                                        && tmpList.get(j).getTransportStreamId() == deletedRadioProgram.getTransportStreamId()) {
                                    Log.d(TAG, "checkDeleteResult: (Error) BK91 favMode=" + i);
                                    return false;
                                }
                            }
                        }
                }
                    Log.d(TAG, "checkDeleteResult: BK92 favMode=" + i + ", pass");
                }                
                }
            else {
                if(oriFavCnt[i] == 1)//ori count=1, after delete, get list is null is OK!!!
                    continue;
                Log.d(TAG,"checkDeleteResult: (Error) BK2 favMode="+i+", tmpList = null");
                return false;
            }
        }
        return true;
    }
    */


    public void BtnFavInfoDeleteAll_OnClick(View view){
        //void FavInfoDeleteAll(int favMode)
        Log.d(TAG,"BtnFavInfoDeleteAll_OnClick:");
        int itemIndex = Items.DeleteAll.getValue();

        //start test, delete all fav list
        for (int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++)
        {
            FavInfoDeleteAll(i);
        }

        showInfo(itemIndex);
        if(checkDeleteAllResult()==true){
            Log.d(TAG,"BtnFavInfoDeleteAll_OnClick: BK5 OK");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");//BtGetProgram.setText(str_GetProgram +"Pass");
            setCheckedResult(itemIndex, true);
        }else{
            Log.d(TAG,"BtnFavInfoDeleteAll_OnClick: BK5 Fail");
            buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Fail");//BtSave.setText(str_GetProgram +"Fail");
            setCheckedResult(itemIndex, false);
        }
    }
    private boolean checkDeleteAllResult(){
        List<FavInfo> tmpList  = new ArrayList<>();
        for (int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++)
        {
            tmpList = FavInfoGetList(i);  //tmpList = FavInfoGetProgramList(i);
            if(tmpList != null){
                Log.d(TAG,"checkDeleteAllResult: favMode="+i+", not null");
                return false;
            }else {
                Log.d(TAG,"checkDeleteAllResult: favMode="+i+", is null");
            }
        }
        return true;
    }


    void showInfo(int index){//void showInfo(boolean showProgram, String first_str){
        //String str=null;
        StringBuilder zText = new StringBuilder ();
        boolean showProgram=false;//true: get program, false: get program list

        switch(Items.valueOf(index))
        {
            /*
            case GetProgram: {
                fillString(zText,
                        "\nFavInfoGetProgram(int favMode,int index):"
                                +"\nGet program of fav position=1 of all fav list"
                        );
                showProgram=true;
            }break;
            case GetProgramList: {
                fillString(zText,
                        "\nFavInfoGetProgramList(int favMode):"
                                +"\nGet program list of all fav list"
                        );
                showProgram=false;
            }break;
            case Save: {
                fillString(zText,
                        "\nFavInfoSave(ProgramInfo program, int favMode, int index):"
                                +"\nSave chnum=1 of TV/Radio program to all fav list."
                        );
                showProgram=true;
            }break;
            case SaveListTwoParam: {
                fillString(zText,
                        "\nFavInfoSaveList(List <ProgramInfo> programList, int favMode):"
                        +"\nSave "+mtestTvCnt+" TV programs to TV FavList, "+"Save "+mtestRadioCnt+" Radio programs to Radio FavList,"
                                //+"\nyou can select "+ITEM_NAME[Items.GetProgramList.getValue()]+" button to double check result."
                        );
                showProgram=false;
            }break;
            */
            case SaveListOneParam: {
                fillString(zText,
                        "\nFavInfoSaveList(List <FavInfo> favList) :"
                        +"\nSave all programs to fav list."+"TV("+mAllTvCnt+"), Radio("+mAllRadioCnt+")."
                                //+"\nyou can select "+ITEM_NAME[Items.GetProgramList.getValue()]+" button to double check result."
                        );
                showProgram=false;
            }break;
            /*
            case Delete: {
                fillString(zText,
                        "\nFavInfoDelete(int favMode, int index) :"
                        +"\nDelete favId=1 for all fav list..."
                                //+"\nyou can select "+ITEM_NAME[Items.GetProgramList.getValue()]+" button to double check result."
                        );
                showProgram=false;
            }break;
            */
            case DeleteAll: {
                fillString(zText,
                        "\nFavInfoDeleteAll(int favMode)   :"
                        +"\nDelete all fav list..."
                                //+"\nyou can select "+ITEM_NAME[Items.GetProgramList.getValue()]+" button to double check result."
                        );
                showProgram=false;
            }break;
            default:
                fillString(zText, "Error:");
                break;
        }

        if(showProgram == true)     {//show program
            fillString(zText,
                    "\n\n(1)FavInfoGetProgram(TV_FAV1_TYPE, 1):"+ outputGetProgram(ProgramInfo.TV_FAV1_TYPE,1)
                    +"\n(2)FavInfoGetProgram(TV_FAV2_TYPE, 1):"+ outputGetProgram(ProgramInfo.TV_FAV2_TYPE, 1)
                    +"\n(3)FavInfoGetProgram(TV_FAV3_TYPE, 1):"+ outputGetProgram(ProgramInfo.TV_FAV3_TYPE, 1)
                    +"\n(4)FavInfoGetProgram(TV_FAV4_TYPE, 1):"+ outputGetProgram(ProgramInfo.TV_FAV4_TYPE, 1)
                    +"\n(5)FavInfoGetProgram(TV_FAV5_TYPE, 1):"+ outputGetProgram(ProgramInfo.TV_FAV5_TYPE, 1)
                    +"\n(6)FavInfoGetProgram(TV_FAV6_TYPE, 1):"+ outputGetProgram(ProgramInfo.TV_FAV6_TYPE, 1)
                    +"\n(7)FavInfoGetProgram(RADIO_FAV1_TYPE, 1):"+ outputGetProgram(ProgramInfo.RADIO_FAV1_TYPE, 1)
                    +"\n(8)FavInfoGetProgram(RADIO_FAV2_TYPE, 1):"+ outputGetProgram(ProgramInfo.RADIO_FAV2_TYPE, 1)
                    +"\n\ninvalid parameter:"
                    +"\n(1)FavInfoGetProgram(ALL_TV_TYPE, 1):"+ outputGetProgram(ProgramInfo.ALL_TV_TYPE, 1)
                    +"\n(2)FavInfoGetProgram(ALL_RADIO_TYPE, 1):"+ outputGetProgram(ProgramInfo.ALL_RADIO_TYPE, 1)
                    +"\n(3)FavInfoGetProgram(ALL_TV_RADIO_TYPE_MAX, 1):"+ outputGetProgram(ProgramInfo.ALL_TV_RADIO_TYPE_MAX, 1)
                    +"\n(4)FavInfoGetProgram(-1, 1):"+ outputGetProgram(-1, 1)
            );
            TvFavInfo.setText(zText.toString());

        }else{//show program list
            fillString(zText,
                    "\n\n(1)FavInfoGetProgramList(TV_FAV1_TYPE):" + outputGetProgramList(ProgramInfo.TV_FAV1_TYPE)
                    + "\n(2)FavInfoGetProgramList(TV_FAV2_TYPE):" + outputGetProgramList(ProgramInfo.TV_FAV2_TYPE)
                    + "\n(3)FavInfoGetProgramList(TV_FAV3_TYPE):" + outputGetProgramList(ProgramInfo.TV_FAV3_TYPE)
                    + "\n(4)FavInfoGetProgramList(TV_FAV4_TYPE):" + outputGetProgramList(ProgramInfo.TV_FAV4_TYPE)
                    + "\n(5)FavInfoGetProgramList(TV_FAV5_TYPE):" + outputGetProgramList(ProgramInfo.TV_FAV5_TYPE)
                    + "\n(6)FavInfoGetProgramList(TV_FAV6_TYPE):" + outputGetProgramList(ProgramInfo.TV_FAV6_TYPE)
                    + "\n(7)FavInfoGetProgramList(RADIO_FAV1_TYPE):" + outputGetProgramList(ProgramInfo.RADIO_FAV1_TYPE)
                    + "\n(8)FavInfoGetProgramList(RADIO_FAV2_TYPE):" + outputGetProgramList(ProgramInfo.RADIO_FAV2_TYPE)
                    + "\n\ninvalid parameter:"
                    + "\n(1)FavInfoGetProgramList(ALL_TV_TYPE):" + outputGetProgramList(ProgramInfo.ALL_TV_TYPE)
                    + "\n(2)FavInfoGetProgramList(ALL_RADIO_TYPE):" + outputGetProgramList(ProgramInfo.ALL_RADIO_TYPE)
                    + "\n(3)FavInfoGetProgramList(ALL_TV_RADIO_TYPE_MAX):" + outputGetProgramList(ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
                    + "\n(4)FavInfoGetProgramList(-1):" + outputGetProgramList(-1)
            );
            TvFavInfo.setText(zText.toString());
        }
    }
    String outputGetProgram(int groupType,int position){
        SimpleChannel program=null;//FavInfoGetProgram(groupType, position);
        if(program==null) {
            return "null";
        } else {
            return String.format("["+program.getChannelNum()+"]"+program.getChannelName()+" ,"); //return String.format("chName: %s", program.getChName());
        }
    }
    String outputGetProgramList(int favMode){
        List<ProgramInfo> programList = new ArrayList<>();
        List<FavInfo> favList = new ArrayList<>();
        String str="";
        int i=0;
        int tmpFavMode=0;

        favList = FavInfoGetList(favMode);
        if(favList != null) {
            //Log.d(TAG, "outputGetProgramList BK0 favMode=" + favMode + ", cnt=" + favList.size());
            str = String.format(" total cnt:%d", favList.size());
        }
        else {
            //Log.d(TAG, "outputGetProgramList BK0 favMode=" + favMode + ", is null");
            str = "null";
        }
        /*
        if(favMode < ProgramInfo.ALL_RADIO_TYPE) {
            //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
            tmpFavMode = favMode;
        }
        else if(favMode >= ProgramInfo.ALL_RADIO_TYPE && favMode < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
            //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
            tmpFavMode = favMode - ProgramInfo.ALL_RADIO_TYPE;
        }
        Log.d(TAG, "outputGetProgramList BK1");
        if(ProgramManagerList != null) {
            Log.d(TAG, "outputGetProgramList BK2 ProgramManagerList.size()="+ProgramManagerList.size());
            for(i=0; i<ProgramManagerList.size(); i++) {
                Log.d(TAG, "outputGetProgramList BK3 i="+i+", favMode="+favMode);
                if(i == tmpFavMode && ProgramManagerList.get(i) != null){
                    str = String.format(" total cnt:%d", ProgramManagerList.get(i).ProgramManagerInfoList.size());
                    //str ="";
                    break;
                }else
                    str = "null";
            }
        }else
            str = "null";
        */

        /*
        programList = FavInfoGetProgramList(favMode);
        if(programList == null) {
            Log.d(TAG,"outputGetProgramList: str=null");
            str = "null";
        }
        else {
            str = String.format("total cnt:%d", programList.size());
            str="";
        }*/

        //Log.d(TAG,"outputGetProgramList: result str="+str);
        return str;
    }
    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }

}
