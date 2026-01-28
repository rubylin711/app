package com.prime.dtvplayer.GridViewActivity;

/*
  Created by scoty_kuo on 2017/11/08.
 */

import android.content.Context;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.PasswordDialogView;

import java.util.List;

public class GridTVManagerActivity extends DTVActivity {
    private static final String TAG = "TVManagerActivity";
    private GridView tvlistview;
    private GridView favlistview;
    private ListAdapter tvAdapter=null;
    private ListAdapter favAdapter=null;
    private int move_flag = 0,lock_flag = 0,delete_flag = 0;//set mode
    private int tv_move_channel = -1,
            fav_move_channel = -1;
//            tv_delete_channel = -1,
//            fav_delete_channel = -1;
    private int curFavGroup = 1;//current Favorite group
//    private ProgramInfo favInfo = new ProgramInfo();
    private int serviceType = 0,groupNum = 0;
    private ActivityTitleView setActivityTitleView;
    private int listCount = 9;
    private boolean tvGoTop,
                    tvGoBottom,
                    favGoTop,
                    favGoBottom;
    //private TextView favtitle;
    private List<ProgramManagerImpl> ProgramManagerList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tvmanager);
        TVManagerInit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        ConstraintLayout.LayoutParams tvlayoutParams = new ConstraintLayout.LayoutParams(
                        tvlistview.getWidth(), itemHeight * listCount);

        tvlistview.setTranslationX(tvlistview.getX());
        tvlistview.setTranslationY(tvlistview.getY());
        tvlistview.setLayoutParams(tvlayoutParams);


        ConstraintLayout.LayoutParams favlayoutParams = new ConstraintLayout.LayoutParams(
                favlistview.getWidth(), itemHeight * listCount);

        favlistview.setTranslationX(favlistview.getX());
        favlistview.setTranslationY(favlistview.getY());
        favlistview.setLayoutParams(favlayoutParams);

        //for(int i = 0 ; i < ProgramManagerList.get(0).ProgramManagerInfoList.size() ; i++)
        //{

        //}

    }

    public void TVManagerInit()
    {
        Log.d(TAG,"TVManagerInit");
        ActivityHelpView setActivityHelpView;
        int TvFavTableMax = 6, RadioFavTableMax = 2;

        serviceType = getIntent().getIntExtra("service",0);

        if(serviceType == 1)
            groupNum = TvFavTableMax;
        else
            groupNum = RadioFavTableMax;

        //program data
        GetProgramData();

        setActivityTitleView = (ActivityTitleView)findViewById(R.id.tvmanagerTitleViewLayout);
        changeProgramTitle();
        changeListTitle();

        setActivityHelpView = (ActivityHelpView)findViewById(R.id.tvmanagerHelpViewLayout);
//        SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_TVMANAGER_HELP_INFO));
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 42, 44, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 63, 67, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setActivityHelpView.setHelpInfoTextBySplit(getString(R.string.STR_TVMANAGER_HELP_INFO));

        setActivityHelpView.resetHelp(1,R.drawable.help_red,getString(R.string.STR_DELETE));
        setActivityHelpView.resetHelp(2,R.drawable.help_green,getString(R.string.STR_FAVORITE));
        setActivityHelpView.resetHelp(3,R.drawable.help_yellow,getString(R.string.STR_MOVE));
        setActivityHelpView.resetHelp(4,R.drawable.help_blue,getString(R.string.STR_LOCK));

        // Johnny 20181228 for mouse control -s
        setActivityHelpView.setHelpIconClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDeleteMode();
            }
        });
        setActivityHelpView.setHelpIconClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFAVGroup();
            }
        });
        setActivityHelpView.setHelpIconClickListener(3, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMoveMode();
            }
        });
        setActivityHelpView.setHelpIconClickListener(4, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((tvlistview.hasFocus()))
                {
                    if(lock_flag != 1) {
                        new PasswordDialogView(GridTVManagerActivity.this,GposInfoGet().getPasswordValue(), PasswordDialogView.TYPE_PINCODE,0) {
                            public void onCheckPasswordIsRight() {
                                Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                                setLockMode();
                            }

                            public void onCheckPasswordIsFalse() {
                                Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
                                new MessageDialogView(GridTVManagerActivity.this,
                                        getString(R.string.STR_INVALID_PASSWORD), 3000) {
                                    public void dialogEnd() {
                                    }
                                }.show();
                            }
                            public boolean onDealUpDownKey() {
                                return false;
                            }
                        };
                    }
                    else
                        setLockMode();
                }
            }
        });
        // Johnny 20181228 for mouse control -e

        tvlistview = (GridView) this.findViewById(R.id.tvlistLIV);
        favlistview = (GridView)this.findViewById(R.id.favlistLIV);

        tvAdapter = new ListAdapter(GridTVManagerActivity.this,(ProgramManagerList.get(0).ProgramManagerInfoList));//TV or Radio channel
        favAdapter = new ListAdapter(GridTVManagerActivity.this,(ProgramManagerList.get(curFavGroup).ProgramManagerInfoList));//first fav group

        tvlistview.setNumColumns(1);
        tvlistview.setAdapter(tvAdapter);
        tvlistview.setOnItemClickListener(new tvlistOnItemClick());
        tvlistview.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event){
                Log.d(TAG, "onKey: ===>>> AAAAA");
                if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    //move,delete,lock mode can not use left/right key
                    if(move_flag != 0 || delete_flag != 0 || lock_flag != 0)
                        return true;
                    else
                        favlistview.requestFocus();
                }
                return false;
            }
        });
        //tvlistview.setSelection(40);

        favlistview.setNumColumns(1);
        favlistview.setAdapter(favAdapter);
        favlistview.setOnItemClickListener(new favlistOnItemClick());
        favlistview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT //|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                        ){
                    //move,delete,lock mode can not use left/right key
                    if(move_flag != 0 || delete_flag != 0 || lock_flag != 0)
                        return true;
                    else
                        tvlistview.requestFocus();
                }
                return false;
            }
        });
        favlistview.deferNotifyDataSetChanged();
    }

    public void checkDeleteChannelDialog()//Yes : delete ; No: cancel
    {
        Log.d(TAG," checkDeleteChannelDialog ");
        new SureDialog(GridTVManagerActivity.this){
            public void onSetMessage(View v){
                ((TextView)v).setText(getString(R.string.STR_DO_YOU_WANT_TO_DELETE_CHANNEL));
            }
            public void onSetNegativeButton(){
            }
            public void onSetPositiveButton(){
                //Log.d(TAG," deleteSelectChannel "+ProgramManagerList.get(0).ProgramManagerInfoList.size());
                channelMangerSave();//save list
                finish();//back to main menu
            }
        };

    }

    public void cancelMoveTVChannel()//cancel mark tv move channel
    {
        if(tv_move_channel != -1) {
            ProgramManagerList.get(0).ProgramManagerInfoList.get(tv_move_channel).setMoveIcon(0);
            tv_move_channel = -1;
            tvAdapter.notifyDataSetChanged();
        }
    }

    public void cancelMoveFAVChannel()//cancel mark fav move channel
    {
        if(fav_move_channel != -1) {
            ProgramManagerList.get(0).ProgramManagerInfoList.get(fav_move_channel).setMoveIcon(0);
            fav_move_channel = -1;
            favAdapter.notifyDataSetChanged();
        }
    }

//    public void setListviewInVisible()//set TV listview invisible
//    {
//        Log.d(TAG,"setListviewInVisible");
//        if(tvlistview.getVisibility() == View.VISIBLE)
//            tvlistview.setVisibility(View.GONE);
//
//    }
//
//    public void setListviewVisible()//set FAV listview invisible
//    {
//        Log.d(TAG,"setListviewVisible");
//        if(tvlistview.getVisibility() == View.GONE)
//            tvlistview.setVisibility(View.VISIBLE);
//
//    }

    static int deleteAllSelect = 0;

    class tvlistOnItemClick implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            Log.d(TAG,"tvlistOnItemClick " + position);

            if(move_flag == 1)//move tv mode
            {
                Log.d(TAG,"move tv channel " + position);
                if(tv_move_channel == -1) {
                    tv_move_channel = position;
                    ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setMoveIcon(1);//set move symbol
                }
                else {
                    ProgramManagerList.get(0).MoveProgram(ProgramManagerList,tv_move_channel,position);//move channel
                    ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setMoveIcon(0);//clean move symbol
                    tv_move_channel = -1;

                }
            }
            else if(lock_flag == 1)//lock mode
            {
                Log.d(TAG,"lock tv channel " + position);
                int lock = 0;
                if(ProgramManagerList.get(0).ProgramManagerInfoList.get(position).getUserLock() == 0)
                    lock = 1;
                ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setUserLock(lock);
                //tvAdapter.notifyItemChanged(position);
                favAdapter.notifyDataSetChanged();
            }
            else if(delete_flag == 1)//delete mode
            {
                Log.d(TAG,"delete tv channel " + position);
                int delete = 0;
                if(ProgramManagerList.get(0).ProgramManagerInfoList.get(position).getDelete() == 0)
                    delete = 1;
                ProgramManagerList.get(0).DelProgram(ProgramManagerList,position,delete);

            }
            else if(delete_flag == 2)//delete All channel mode
            {
                if(deleteAllSelect == 0) {
                    deleteAllSelect = 1;
                    ProgramManagerList.get(0).DelAllProgram(1);//set delete all program
                }
                else {
                    deleteAllSelect = 0;
                    ProgramManagerList.get(0).DelAllProgram(0);//cancel delete all program
                }
            }
            else {//add favorite channel
                Log.d(TAG, "add fav channel " + position);
                ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,curFavGroup,position);
                ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setFavIcon(1);
                favAdapter.notifyDataSetChanged();
            }
            tvAdapter.notifyDataSetChanged();

        }
    }

    //favorite list onclick
    class favlistOnItemClick implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            //final int position = favlistview.getChildLayoutPosition(v);
            Log.d(TAG,"favlistOnItemClick Delete Fav " + position);

            if(move_flag == 1)//move fav mode
            {
                if(fav_move_channel == -1) {
                    fav_move_channel = position;
                    ProgramManagerList.get(curFavGroup).ProgramManagerInfoList.get(position).setMoveIcon(1);//set move symbol
                }
                else {
                    //favAdapter.notifyItemMoved(fav_move_channel,position);
                    ProgramManagerList.get(curFavGroup).MoveProgram(ProgramManagerList,fav_move_channel,position);//move channel
                    ProgramManagerList.get(curFavGroup).ProgramManagerInfoList.get(position).setMoveIcon(0);//clean move symbol
                    fav_move_channel = -1;
                }
            }
            else {//delete favorite channel
                Log.d(TAG,"delete fav channel " + position);
                ProgramManagerList.get(curFavGroup).DelProgram(ProgramManagerList,position,1);
                tvAdapter.notifyDataSetChanged();
            }
                favAdapter.notifyDataSetChanged();
        }
    }

    public void changeProgramTitle()//change tv channel title
    {
        if(serviceType == 1)
            setActivityTitleView.setTitleView(getString(R.string.STR_CHANNEL) + " > " + getString(R.string.STR_ALL_TV_CHANNELS));
        else
            setActivityTitleView.setTitleView(getString(R.string.STR_CHANNEL) + " > " + getString(R.string.STR_ALL_RADIO_CHANNELS));
    }

    public void changeListTitle()
    {
        TextView tvtitle,favtitle;
        tvtitle = (TextView) findViewById(R.id.tvtitleTXV);
        favtitle = (TextView) findViewById(R.id.favtitleTXV);
        if(serviceType == 1)
            tvtitle.setText(R.string.STR_ALL_TV_CHANNELS);
        else
            tvtitle.setText(R.string.STR_ALL_RADIO_CHANNELS);
        favtitle.setText(GetAllProgramGroup().get(curFavGroup).getGroupName());
    }

    public void changeFAVGroupTitle()//change favorite group title
    {
        TextView favtitle = (TextView) findViewById(R.id.favtitleTXV);
        curFavGroup++;
        if(curFavGroup > groupNum)
            curFavGroup = 1;

        favtitle.setText(GetAllProgramGroup().get(curFavGroup).getGroupName());
    }

    public void changeFAVGroup()//change favorite group
    {
        changeFAVGroupTitle();
        favAdapter = new ListAdapter(GridTVManagerActivity.this,ProgramManagerList.get(curFavGroup).ProgramManagerInfoList);
        favlistview.setAdapter(favAdapter);
        favAdapter.notifyDataSetChanged();
    }

    public void channelMangerSave()
    {
        for(int i = 0 ; i < ProgramManagerList.size() ; i++)
            ProgramManagerList.get(i).Save();
    }

    public void setDeleteMode()//set delete mode
    {
        Log.d(TAG," setDeleteMode " + delete_flag);
        if(tvlistview.hasFocus()) {
            delete_flag++;
            TextView tvtitle = (TextView) findViewById(R.id.tvtitleTXV);
            ImageView titleicon = (ImageView) findViewById(R.id.tviconIGV);
            if (delete_flag > 2)
                delete_flag = 0;
            switch (delete_flag) {
                case 0: {
                    if(serviceType == 1)
                        tvtitle.setText(R.string.STR_ALL_TV_CHANNELS);
                    else
                        tvtitle.setText(R.string.STR_ALL_RADIO_CHANNELS);
                    titleicon.setImageDrawable(getResources().getDrawable(R.drawable.tvicon,null));
                    ProgramManagerList.get(0).DelAllProgram(0);//cancel delete all program
                    tvAdapter.notifyDataSetChanged();
                }
                break;
                case 1: {
                    tvtitle.setText(R.string.STR_DELETE_CHANNEL);
                    titleicon.setImageDrawable(getResources().getDrawable(R.drawable.delete,null));
                }
                break;
                case 2: {
                    tvtitle.setText(R.string.STR_DELETE_ALL_CHANNELS);
                    titleicon.setImageDrawable(getResources().getDrawable(R.drawable.delete,null));
                }
                break;
                default:
                    break;
            }
        }
    }

    public void setMoveMode()//set move mode
    {
        TextView title;
        ImageView titleicon;
        move_flag++;
        Log.d(TAG," setMoveMode " + move_flag);
        if(move_flag > 1)
            move_flag = 0;
        if(tvlistview.hasFocus()) {
            title = (TextView) findViewById(R.id.tvtitleTXV);
            titleicon = (ImageView) findViewById(R.id.tviconIGV);
        }
        else {
            title = (TextView)findViewById(R.id.favtitleTXV);
            titleicon = (ImageView) findViewById(R.id.faviconIGV);
        }
        if(move_flag == 1) {
            title.setText(R.string.STR_MOVE_MODE);
            titleicon.setImageDrawable(getResources().getDrawable( R.drawable.move ,null));
        }
        else {
            titleicon.setImageDrawable(getResources().getDrawable( R.drawable.tvicon,null));
            if(tvlistview.hasFocus()) {
                cancelMoveTVChannel();
                //changeProgramTitle();
                title.setText(R.string.STR_ALL_TV_CHANNELS);
            }
            else {
                cancelMoveFAVChannel();
                title.setText(GetAllProgramGroup().get(curFavGroup).getGroupName());
            }
        }
    }

    public void setLockMode()//set lock mode
    {
        TextView title = (TextView) findViewById(R.id.tvtitleTXV);
        ImageView titleicon = (ImageView) findViewById(R.id.tviconIGV);
        Log.d(TAG," setLockMode " + lock_flag);
        if(tvlistview.hasFocus()) {
            lock_flag++;
            if (lock_flag > 1)
                lock_flag = 0;

            if (lock_flag == 1) {
                title.setText(R.string.STR_LOCK_MODE);
                titleicon.setImageDrawable(getResources().getDrawable(R.drawable.lock,null));
            } else {
                title.setText(R.string.STR_ALL_TV_CHANNELS);
                titleicon.setImageDrawable(getResources().getDrawable(R.drawable.tvicon,null));
            }
        }
    }

    private class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context cont;
        private List<ProgramManagerImpl.ProgramManagerInfo> listItems;
        private int selectItem;

        class ViewHolder {
            TextView ch_num;//LCN
            TextView ch_name;//Channel name
            ImageView scramble;//Scramble icon
            ImageView fav;//group icon
            ImageView lock;//Lock icon
            ImageView dividerLine;
        }

        public ListAdapter(Context context, List<ProgramManagerImpl.ProgramManagerInfo> list) {
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
            Log.d(TAG," getView === > IN ");
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.tvmanager_list_item, null);

                holder = new ViewHolder();
                holder.ch_num = (TextView) convertView.findViewById(R.id.chnumTXV);
                holder.ch_name = (TextView) convertView.findViewById(R.id.chnameTXV);
                holder.scramble = (ImageView) convertView.findViewById(R.id.scrambleIGV);
                holder.fav = (ImageView) convertView.findViewById(R.id.favIGV);
                holder.lock = (ImageView) convertView.findViewById(R.id.lockIGV);
                //holder.dividerLine = (ImageView) convertView.findViewById(R.id.dividerlineIGV);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            TextView chnum = (TextView) convertView.findViewById(R.id.chnumTXV);
            if (((ProgramManagerImpl.ProgramManagerInfo) listItems.get(position)).getMoveIcon() == 1) {//set move symbol
                holder.ch_num.setText("↕");
            } else if (((ProgramManagerImpl.ProgramManagerInfo) listItems.get(position)).getDelete() == 1)//set delete symbol
            {
                holder.ch_num.setText("X");
            } else
                holder.ch_num.setText(Integer.toString(((ProgramManagerImpl.ProgramManagerInfo) listItems.get(position)).getChannelNum()));

            TextView chname = (TextView) convertView.findViewById(R.id.chnameTXV);
            holder.ch_name.setText(((ProgramManagerImpl.ProgramManagerInfo) listItems.get(position)).getChannelName());

            Log.d(TAG, "ch_num" + holder.ch_num + "ch_name" + holder.ch_name);

            if (((ProgramManagerImpl.ProgramManagerInfo) listItems.get(position)).getCA() == 1)//scramble icon
                holder.scramble.setBackgroundResource(R.drawable.scramble);
            else
                holder.scramble.setBackgroundResource(android.R.color.transparent);//clean icon


            if (((ProgramManagerImpl.ProgramManagerInfo) listItems.get(position)).getFavIcon() == 1)//fav icon
                holder.fav.setBackgroundResource(R.drawable.fav);
            else
                holder.fav.setBackgroundResource(android.R.color.transparent);//clean icon

            if (((ProgramManagerImpl.ProgramManagerInfo) listItems.get(position)).getUserLock() == 1)//lock icon
                holder.lock.setBackgroundResource(R.drawable.lock);
            else
                holder.lock.setBackgroundResource(android.R.color.transparent);//clean icon

           /* if(position == (listItems.size()-1))
                holder.dividerLine.setVisibility(View.INVISIBLE);
            else
                holder.dividerLine.setVisibility(View.VISIBLE);
            */
            return convertView;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                Log.d(TAG," KEYCODE_DPAD_DOWN");
                GridView listView;
                int cur_position;
                if(tvlistview.hasFocus()) {
                    cur_position = tvlistview.getSelectedItemPosition();
                    listView = tvlistview;
                }
                else {
                    cur_position = favlistview.getSelectedItemPosition();
                    listView = favlistview;
                }
                if (cur_position == listView.getCount() - 1)
                    listView.setSelection(0);



                //int curtv_postiiton = tvlistview.getSelectedItemPosition();
                //int curfav_postiiton = favlistview.getSelectedItemPosition();
                //Log.d(TAG, "count " + tvlistview.getCount() + "cur_postiiton " + curtv_postiiton);
                //if (curtv_postiiton == tvlistview.getCount() - 1)
               //     tvlistview.setSelection(0);
                //if (curfav_postiiton == favlistview.getCount() - 1)
               //     favlistview.setSelection(0);
            }break;
            case KeyEvent.KEYCODE_DPAD_UP: {
                Log.d(TAG, "onKeyDown: UP");
                GridView listView;
                int cur_position;
                if(tvlistview.hasFocus()) {
                    cur_position = tvlistview.getSelectedItemPosition();
                    listView = tvlistview;
                }
                else {
                    cur_position = favlistview.getSelectedItemPosition();
                    listView = favlistview;
                }
                if (cur_position == 0)
                    listView.setSelection(listView.getCount() - 1);
                //int curtv_postiiton = tvlistview.getSelectedItemPosition();
                //int curfav_postiiton = favlistview.getSelectedItemPosition();
               // Log.d(TAG, "count " + tvlistview.getCount() + "cur_postiiton " + cur_position);

                //if (curtv_postiiton == 0)
                //    tvlistview.setSelection(tvlistview.getCount() - 1);
                //if (curfav_postiiton == 0)
                 //   favlistview.setSelection(favlistview.getCount() - 1);
            }
            break;
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG," KEYCODE_PROG_RED set delete mode ");
                setDeleteMode();
            }break;
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG," KEYCODE_PROG_GREEN change FAV group ");
                changeFAVGroup();
            }break;
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG," KEYCODE_PROG_YELLOW set move mode ");
                setMoveMode();
            }break;
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG," KEYCODE_PROG_BLUE set lock mode ");
                if((tvlistview.hasFocus()))
                {
                    if(lock_flag != 1) {
                        new PasswordDialogView(GridTVManagerActivity.this,GposInfoGet().getPasswordValue(), PasswordDialogView.TYPE_PINCODE,0) {
                            public void onCheckPasswordIsRight() {
                                Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                                setLockMode();
                            }

                            public void onCheckPasswordIsFalse() {
                                Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
                                new MessageDialogView(GridTVManagerActivity.this,
                                        getString(R.string.STR_INVALID_PASSWORD), 3000) {
                                    public void dialogEnd() {
                                    }
                                }.show();
                            }
                            public boolean onDealUpDownKey() {
                                return false;
                            }
                        };
                    }
                    else
                        setLockMode();
                }
            }break;
            case KeyEvent.KEYCODE_4:{
                //page up
                if(tvlistview.hasFocus())
                    setListPageUp(tvlistview);
                else
                    setListPageUp(favlistview);

            }break;
            case KeyEvent.KEYCODE_5:{
                //page down
                if(tvlistview.hasFocus())
                    setListPageDown(tvlistview);
                else
                    setListPageDown(favlistview);

            }break;
            case KeyEvent.KEYCODE_BACK:{
                Log.d(TAG," KEYCODE_BACK");
                if(delete_flag != 0)//check delete channel dialog
                    checkDeleteChannelDialog();
                else
                    channelMangerSave();//save list
            }break;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void setListPageUp(GridView listview)
    {
        int position = listview.getSelectedItemPosition();
        position -= listCount;
        if(position < 0)
            listview.setSelection((listview.getCount())+(position));
        else
            listview.setSelection(position);
    }

    public void setListPageDown(GridView listview)
    {
        int position = listview.getSelectedItemPosition();
        position += listCount;
        if(position > (listview.getCount() - 1)) {
            position -= (listview.getCount());
            listview.setSelection(position);
        }
        else
            listview.setSelection(position);
    }

    public void GetProgramData()
    {
        Log.d(TAG," GetProgramData");
        //int i, j;
        if(serviceType == 1) {
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
            //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        }
        else {
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
            //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        }

        chechChannelExistdialog();
        for (int i = 1; i < ProgramManagerList.size(); i++)
        {
            for (int j = 0; j < ProgramManagerList.get(0).ProgramManagerInfoList.size(); j++)
            {
                ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,i,j);
            }
            ProgramManagerList.get(i).Save();
        }

        for(int i = 1 ; i < ProgramManagerList.size(); i++)
        {
            for(int j = 0 ; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++)//set fav icon
                ProgramManagerList.get(i).ProgramManagerInfoList.get(j).setFavIcon(1);
        }
    }

    public void chechChannelExistdialog()
    {
        Log.d(TAG," chechChannelExistdialog");
        String str;
        if(ProgramManagerList.get(0).ProgramManagerInfoList.size() ==0) {
            if (serviceType == 1)
                str =getString(R.string.STR_NO_TV_CHANNEL);
            else
                str =getString(R.string.STR_NO_RADIO_CHANNEL);

            new MessageDialogView(GridTVManagerActivity.this, str, 3000) {
                public void dialogEnd() {
                    finish();
                }
            }.show();
        }
    }
}
