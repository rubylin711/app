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
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OKListManagerActivity extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private List<OkListManagerImpl> OkList = new ArrayList<OkListManagerImpl>();

    private enum Items {
        OkListInit(0),
        FilterByChannelName(1),
        FilterByChannelNum(2),
        Max(3);

        private int value;
        private static Map map = new HashMap<>();
        private Items(int value) {
            this.value = value;
        }
        static {
            for (OKListManagerActivity.Items testItem : OKListManagerActivity.Items.values()) {
                map.put(testItem.value, testItem);
            }
        }
        public static OKListManagerActivity.Items valueOf(int testItem) {
            return (OKListManagerActivity.Items)map.get(testItem);
        }
        public int getValue() {
            return value;
        }
    }
    private List<Button> buttons;
    private static final int[] BUTTON_IDS = {
            R.id.BtnTestOkList1,
            R.id.BtnTestOkList2,
            R.id.BtnTestOkList3
    };
    private TextView TvOkList=null;
    private static final String[] ITEM_NAME = {
            "void OkListInit()\nResult:",
            "void ProgramInfoGetListByFilter()\nFilter by Channel Name\nResult:",
            "void ProgramInfoGetListByFilter()\nFilter by Channel Num\nResult:"
    };
    TestMidMain tm = new TestMidMain();//
    private static final int TESTITEM_NUM = OKListManagerActivity.Items.Max.getValue();//
    boolean [] SubItemChecked = new boolean[TESTITEM_NUM];//
    int mTestResult=0;//
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oklist_manager);

        Log.d(TAG,"onCreate:");
        //Buttons
        buttons = new ArrayList<Button>();
        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);
            //button.setOnClickListener(this); // maybe
            buttons.add(button);
        }
        Log.d(TAG,"onCreate: BK1");
        for(int i = Items.OkListInit.getValue(); i< Items.Max.getValue(); i++){
            Button button = (Button)findViewById(BUTTON_IDS[i]);
            buttons.add(button);
            buttons.get(i).setText(ITEM_NAME[i]);
        }

        Log.d(TAG,"onCreate: BK2");
        //TextView
        TvOkList = (TextView)this.findViewById(R.id.TvTestOkList);
        TvOkList.setText("Info:");
        TvOkList.setMovementMethod(new ScrollingMovementMethod());
        TvOkList.scrollTo(0,0);
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

    public void BtnOkListInit_OnClick(View view){
        //void ProgramManagerInit(int type)
        //time/tv manager call this method
        Log.d(TAG,"BtnProgramManagerInit_OnClick:");
        int itemIndex = Items.OkListInit.getValue();
        StringBuilder zText = new StringBuilder ();

        Log.d(TAG,"BtnProgramManagerInit_OnClick: BK1");
        fillAllListString(zText);
        Log.d(TAG,"BtnProgramManagerInit_OnClick: BK2");

        TvOkList.setText(zText.toString());
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }

    public void BtnOkList_ProgramInfoGetListByFilter_OnClick(View view){
        Log.d(TAG,"BtnOkList_ProgramInfoGetListByFilter_OnClick:");
        int itemIndex = 0, searchMode = 0;
        List<SimpleChannel> ProgramInfoList = OkList.get(0).ProgramInfoList;
        List<SimpleChannel> List = new ArrayList<>();
        if(ProgramInfoList == null || ProgramInfoList.size() == 0) {
            TvOkList.setText("No Channel !!!!");
            return ;
        }

        Log.d(TAG, "BtnOkList_ProgramInfoGetListByFilter_OnClick:  Fist Channel Name = " + ProgramInfoList.get(0).getChannelName());
        String serchStr = "";
        if(view == findViewById(R.id.BtnTestOkList2)) {
            Items.FilterByChannelName.getValue();
            searchMode = MiscDefine.OKListFilter.TAG_CHANNEL_NAME;
            serchStr = ProgramInfoList.get(0).getChannelName().substring(0, 2);
        }
        else if(view == findViewById(R.id.BtnTestOkList3)) {
            Items.FilterByChannelNum.getValue();
            searchMode = MiscDefine.OKListFilter.TAG_CHANNEL_NUM;
            serchStr = "9";
        }

        List = ProgramInfoGetListByFilter( searchMode, 0, serchStr ,0,1);//Scoty 20181109 modify for skip channel
        String result = "Search String = " +serchStr +"\n\nList[0] :\n";

        for(int i = 0; i<ProgramInfoList.size();i++)
            result = result + "[" + ProgramInfoList.get(i).getChannelNum() + "] " + ProgramInfoList.get(i).getChannelName() + "\n";

        if(searchMode == MiscDefine.OKListFilter.TAG_CHANNEL_NUM)
            result = result + "\n\nFilter By Channel Name:\n" ;
        else
            result = result + "\n\nFilter By Channel Num:\n" ;

        if(List != null )
        {
            for(int i  = 0; i < List.size(); i++)
                result = result + "["+ ProgramInfoList.get(i).getChannelNum() + "] " + List.get(i).getChannelName() + "\n";
        }

        TvOkList.setText(result);
        buttons.get(itemIndex).setText(ITEM_NAME[itemIndex]+"Pass");
        setCheckedResult(itemIndex, true);
    }


    void fillAllListString(StringBuilder zText){
        String str = "OKList: \n";
        String radio_str = "All Radio/Radio favorite: ProgramManagerInit(ALL_RADIO_TYPE):\n";
        int i,j;
        int chNum;
        String chName;

        //All TV, TV Fav1~Fav6
        fillString(zText, str);
        OkList = GetOkList();
        //OkListInit();

        if(OkList != null) {
            for(i=0; i<OkList.size(); i++) {
                if(OkList.get(i).ProgramInfoList != null) {
                    fillString(zText, "list[" + i + "]=>");
                    for (j = 0; j < OkList.get(i).ProgramInfoList.size(); j++) {
                        chNum = OkList.get(i).ProgramInfoList.get(j).getChannelNum();
                        chName = OkList.get(i).ProgramInfoList.get(j).getChannelName();
                        Log.d(TAG, "list[" + i + "][" + chNum + "]" + chName);
                        fillString(zText, "[" + chNum + "]" + chName + ", ");
                    }
                    fillString(zText, "\n");
                }
                else{
                    Log.d(TAG, "OkListManagerList.get(i) = null");
                    fillString(zText, "list[" + i + "] is empty\n");
                }
            }
        }else
            fillString(zText, "list is empty.\n");
    }
    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }
}
