package com.prime.dtvplayer.TestMiddleware;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dolphin.dtv.EnTableType;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMidMain extends DTVActivity {
    private String TAG=getClass().getSimpleName();
    private ListView ListViewLeft;
    private ListView ListViewRight;
    private TestMidMainListAdapter LeftAdapter;
    private TestMidMainListAdapter RightAdapter;
    static private List<TestMidInfo> mTestInfo=null;
    static private List<TestMidInfo> mLeftTestInfo=null;
    static private List<TestMidInfo> mRightTestInfo=null;
    private int LIST_MAX_ITEM=11;
    ViewUiDisplay viewUiDisplay = null;

    private enum TestItems {
        Reset(0),
        Scan(1),
        EPGEvent(2),
        BookInfo(3),
        ProgramInfo(4),
//        FavInfo(6),
        ProgramManager(5),
//        EpgUiDisplay(9),
//        BookManager(10),
        OKListManager(6),
        Gpos(7),
        SatInfo(8),
        TpInfo(9),
        FavGroup(10),
        AvControl(11),
//        Subtitle(13),
        Tuner(12),

//        TTX(16),
//        Utc(17),

//        ViewUiDisplay(19),
        PVR_Record(13),
        PVR_Play(14),
        PVR_File(15),
        PVR_TimeShift(16),
        PVR_dual(17),
        Pip(18),
        AV_Burn(19),
        TMCTL(20),
        Common(21);

        private int value;
        private static Map map = new HashMap<>();

        private TestItems(int value) {
            this.value = value;
        }

        static {
            for (TestItems testItem : TestItems.values()) {
                map.put(testItem.value, testItem);
            }
        }

        public static TestItems valueOf(int testItem) {
            return (TestItems)map.get(testItem);
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        if(mTestInfo==null) {
            mTestInfo=new ArrayList<>();
            Log.d(TAG, "onCreate, mLeftTestInfo==null");//eric lin test
            mTestInfo.add(new TestMidInfo(false,"Reset", -1));
            mTestInfo.add(new TestMidInfo(false,"Scan", -1));
            mTestInfo.add(new TestMidInfo(false, "EPGEvent", -1));
            mTestInfo.add(new TestMidInfo(false, "BookInfo", -1));
            mTestInfo.add(new TestMidInfo(false,"ProgramInfo", -1));
            mTestInfo.add(new TestMidInfo(false, "ProgramManager", -1));
            mTestInfo.add(new TestMidInfo(false, "OKListManager", -1));
            mTestInfo.add(new TestMidInfo(false, "Gpos", -1));
            mTestInfo.add(new TestMidInfo(false,"SatInfo", -1));
            mTestInfo.add(new TestMidInfo(false,"TpInfo", -1));
//            mTestInfo.add(new TestMidInfo(false,"FavInfo", -1));
            mTestInfo.add(new TestMidInfo(false, "FavGroup", -1));
//            mTestInfo.add(new TestMidInfo(false,"EpgUiDisplay", -1));
//            mTestInfo.add(new TestMidInfo(false,"BookManager", -1));
            mTestInfo.add(new TestMidInfo(false, "AvControl", -1));
//            mTestInfo.add(new TestMidInfo(false,"Subtitle", -1));
            mTestInfo.add(new TestMidInfo(false,"Tuner", -1));
//            mTestInfo.add(new TestMidInfo(false,"TTX", -1));
//            mTestInfo.add(new TestMidInfo(false,"Utc", -1));

//            mTestInfo.add(new TestMidInfo(false,"ViewUiDisplay", -1));
            mTestInfo.add(new TestMidInfo(false,"PVR_Record", -1));
            mTestInfo.add(new TestMidInfo(false,"PVR_Play", -1));
            mTestInfo.add(new TestMidInfo(false,"PVR_File", -1));
            mTestInfo.add(new TestMidInfo(false,"PVR_TimeShift", -1));
            mTestInfo.add(new TestMidInfo(false,"PVR_dual", -1));
            mTestInfo.add(new TestMidInfo(false,"Pip", -1));
            mTestInfo.add(new TestMidInfo(false,"AV_Burn", -1));
            mTestInfo.add(new TestMidInfo(false,"TMCTL", -1));
            mTestInfo.add(new TestMidInfo(false,"Common", -1));
            sortListInfo();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mid_main);

        //Left
        ListViewLeft = (ListView)this.findViewById(R.id.TestMidViewLeft);
        LeftAdapter = new TestMidMainListAdapter(TestMidMain.this,(mLeftTestInfo));
        ListViewLeft.setAdapter(LeftAdapter);
        LeftAdapter.notifyDataSetChanged();
        //Right
        ListViewRight = (ListView)this.findViewById(R.id.TestMidViewRight);
        RightAdapter = new TestMidMainListAdapter(TestMidMain.this,(mRightTestInfo));
        ListViewRight.setAdapter(RightAdapter);
        RightAdapter.notifyDataSetChanged();
        TestMidInit();
        //ViewUiDisplayInit();//eric lin, add
        viewUiDisplay = GetViewUiDisplay();
    }



    public void TestMidInit() {
        Log.d(TAG, "TestMidInit");//eric lin test
        ListViewLeft.setOnItemClickListener(new leftlistOnItemClick());
        ListViewRight.setOnItemClickListener(new rightlistOnItemClick());//eric lin 20171226 fix right list view
    }

    private class TestMidMainListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context cont;
        private List<TestMidInfo> listItems;
        private int selectItem;

        class ViewHolder {
            ImageView iv;
            TextView item;
            TextView result;
        }

        public TestMidMainListAdapter(Context context, List<TestMidInfo> list) {
            super();
            cont = context;
            listItems = list;
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            if (listItems == null) {
                return 0;
            }
            else
            {
                return listItems.size();//list size
            }
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public void setSelectItem(int position){
            this.selectItem = position;
        }

        public int getSelectItem(){
            return this.selectItem;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG," getView === > position="+position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.testmid_listviewitem, null);
                holder = new ViewHolder();
                holder.iv = (ImageView) convertView.findViewById(R.id.imageViewCboxBrd);
                holder.item = (TextView) convertView.findViewById(R.id.TestMidTestViewItem);
                holder.result = (TextView) convertView.findViewById(R.id.TestMidTestViewResult);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            //Log.d(TAG," getView === > item="+listItems.get(position).getItemString()+", result="+listItems.get(position).getResult());
            holder.iv.setVisibility(View.VISIBLE);
            if(listItems.get(position).getChecked()==false){
                holder.iv.setBackgroundResource(android.R.drawable.checkbox_off_background);
            }else if(listItems.get(position).getChecked()==true){
                holder.iv.setBackgroundResource(android.R.drawable.checkbox_on_background);
            }
            holder.item.setText(listItems.get(position).getItemString()) ;
            holder.result.setText(parseResult(listItems.get(position).getResult()));

            return convertView;
        }
    }

    class leftlistOnItemClick implements AdapterView.OnItemClickListener
    {
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
        {
            Log.d(TAG,"leftlistOnItemClick " + position);
            Intent intent = new Intent();//need


            switch (TestItems.valueOf(position))
            {
                case Gpos:
                {
                    intent.setClass(TestMidMain.this,TestGposActivity.class);
                } break;
                case SatInfo:
                {
                    intent.setClass(TestMidMain.this,TestSatActivity.class);
                }break;
                case TpInfo:
                {
                    intent.setClass(TestMidMain.this,TestTpActivity.class);
                }break;
                case EPGEvent:
                {
                    intent.setClass(TestMidMain.this,TestEPGEventActivity.class);
                }break;
                case BookInfo:
                {
                    intent.setClass(TestMidMain.this,TestBookActivity.class);
                }break;
                case ProgramInfo:
                {
                    intent.setClass(TestMidMain.this,TestProgramActivity.class);
                }break;
//                case FavInfo:
//                {
//                    intent.setClass(TestMidMain.this,TestFavInfoActivity.class);
//                }break;
                case FavGroup:
                {
                    intent.setClass(TestMidMain.this,TestFavGroupActivity.class);
                }break;
                case ProgramManager:
                {
                    intent.setClass(TestMidMain.this,TestProgramManagerActivity.class);
                }break;
//                case EpgUiDisplay:
//                {
//                    intent.setClass(TestMidMain.this,TestEPGUiDisplayActivity.class);
//                }break;
//                case BookManager:
//                {
//                    intent.setClass(TestMidMain.this,TestBookManagerActivity.class);
//                }break;
                case OKListManager:
                {
                    intent.setClass(TestMidMain.this,OKListManagerActivity.class);
                }break;

//                case Subtitle:
//                {
//                    intent.setClass(TestMidMain.this,TestSubtitleActivity.class);
//                }break;

                case Scan:
                {
                    intent.setClass(TestMidMain.this,TestDVBCScanActivity.class);
                    //intent.setClass(TestMidMain.this,TestDVBSScanActivity.class);
//                    intent.setClass(TestMidMain.this,TestISDBTScanActivity.class);
                }break;
//                case TTX:
//                {
//                    intent.setClass(TestMidMain.this,TestTTXActivity.class);
//                }break;
                case Reset:
                {
                    intent.setClass(TestMidMain.this,TestRestToFactoryActivity.class);
                    //ResetFactoryDefault();
                }break;

                default:
                {
                    intent.setClass(TestMidMain.this,TestMidActivity1.class);//need
                }

            }

            intent.putExtra("position",position);//need
            startActivity(intent);//need
        }
    }

    class rightlistOnItemClick implements AdapterView.OnItemClickListener //eric lin 20171226 fix right list view
    {
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
        {
            Log.d(TAG,"rightlistOnItemClick " + position + ", id="+id);
            Intent intent = new Intent();//need


            switch (TestItems.valueOf(position+LIST_MAX_ITEM))
            {
//                case Utc:
//                {
//                    intent.setClass(TestMidMain.this,TestUtcActivity.class);
//                }break;
//                case ViewUiDisplay:
//                {
//                    intent.setClass(TestMidMain.this,TestViewUiDisplayActivity.class);
//                }break;
                case AvControl:
                {
                    intent.setClass(TestMidMain.this,TestAvControlActivity.class);
                }break;
                case Tuner:
                {
                    intent.setClass(TestMidMain.this,TestTunerActivity.class);
                }break;
                case PVR_Record:
                {
                    intent.setClass(TestMidMain.this,TestPVRRecordActivity.class);
                }break;
                case PVR_Play:
                {
                    intent.setClass(TestMidMain.this,TestPVRPlayActivity.class);
                }break;
                case PVR_File:
                {
                    intent.setClass(TestMidMain.this,TestPVRFileActivity.class);
                }break;
                case PVR_TimeShift:
                {
                    intent.setClass(TestMidMain.this,TestPVRTimeShiftActivity.class);
                }break;
                case PVR_dual:
                {
                    intent.setClass(TestMidMain.this,TestPVRDualActivity.class);
                }break;
                case Pip:
                {
                    intent.setClass(TestMidMain.this,TestPipActivity.class);
                }break;
                case AV_Burn:
                {
                    intent.setClass(TestMidMain.this,TestAVBurnActivity.class);
                }break;
                case TMCTL:
                {
                    intent.setClass(TestMidMain.this,TestTmctlActivity.class);
                } break;
                case Common:
                {
                    intent.setClass(TestMidMain.this,TestCommonActivity.class);
                } break;
                default:
                {
                    intent.setClass(TestMidMain.this,TestMidActivity1.class);//need
                }
            }

            intent.putExtra("position",position+LIST_MAX_ITEM);//need
            startActivity(intent);//need
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onCreate(null);

        SaveTable(EnTableType.ALL);//eric lin add
    }

    public List<TestMidInfo> getLeftTestInfo(){
        if(mLeftTestInfo != null) {
            return mLeftTestInfo;
        }
        else
            return null;
    }

    public List<TestMidInfo> getRightTestInfo(){
        if(mRightTestInfo != null) {
            return mRightTestInfo;
        }
        else
            return null;
    }


    public TestMidInfo getTestInfoByIndex(int index){
        if(mTestInfo != null) {
            if(index > mTestInfo.size())
                return null;
            else {
                if (index < LIST_MAX_ITEM)
                    return mLeftTestInfo.get(index);
                else if(index >= LIST_MAX_ITEM)
                    return mRightTestInfo.get(index-LIST_MAX_ITEM);
            }
        }else
            return null;
        return null;
    }


    void sortListInfo(){
        if(mTestInfo != null) {
            mLeftTestInfo=new ArrayList<>();
            for(int i=0; i< mTestInfo.size(); i++){
                if(i < LIST_MAX_ITEM){
                    mLeftTestInfo.add(mTestInfo.get(i));
                }else{
                    if(i==LIST_MAX_ITEM)
                        mRightTestInfo=new ArrayList<>();
                    mRightTestInfo.add(mTestInfo.get(i));
                }
            }
        }
    }

    int bitwiseLeftShift(int value, int index, boolean testResult)
    {
        int tmp=0;
        Log.d(TAG, "bitwiseLeftShift: value="+value+", index="+index+", testResult="+testResult+", result="+(value|1<<index));
        if(testResult == false)
            tmp = value|1<<index;
        else if(testResult == true) {
            Log.d(TAG, "bitwiseLeftShift:BK1 value="+value+", index="+index+", testResult="+testResult);
            tmp = value & (~(1 << index));
            Log.d(TAG, "bitwiseLeftShift:BK2 tmp="+tmp);
        }
        //Log.d(TAG, "bitwiseLeftShift: tmp="+tmp);
        return tmp;
    }

    int bitwiseRightShiftModulus2(int value, int index)
    {
        //Log.d(TAG, "bitwiseRightShiftModulus2: value="+value
        //        +", index="+index+", result="+(value >> index)%2);
        return (value >> index)%2;
    }

    String parseResult(int value){
        String str=null;

        if(value==0)
            str="Pass";
        else if(value==-1){
            str="Not be tested";
        }else{
            str="Error:";
            for(int i=0; i<31; i++)
            {
                //bitwiseRightShiftModulus2(result, i);
                Log.d(TAG, "parseResult: i="+i+", value="+bitwiseRightShiftModulus2(value, i));
                if(bitwiseRightShiftModulus2(value, i)==1) {
                    str = str + String.format("%d,", (i + 1));
                }
            }
        }
        return str;
    }
}
