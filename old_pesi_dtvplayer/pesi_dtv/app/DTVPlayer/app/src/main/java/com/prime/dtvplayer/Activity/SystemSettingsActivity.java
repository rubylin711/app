package com.prime.dtvplayer.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.SelectBoxView;

import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;

/*
  Created by edwin on 2017/11/17.
 */

public class SystemSettingsActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    //private Context mCotext;
    //private Spinner spinnerStartCh;
    //private SelectBoxView selStartChannel;
    private SelectBoxView selBannerTime;
    private SelectBoxView selOSDTransparency;
    private SelectBoxView selDeepStandby;
    private SelectBoxView selAutoStandby;
    private ActivityHelpView help;
    private String orgStr;
    private Dialog mDialog;
    private RecyclerAdapter chAdapter=null;
    private RecyclerView chlistview;
    private TextView startonchannlTXV;
    //private ArrayAdapter<String> adapter;
    private String strLastWatch,strFixedChannel;
    //private boolean chlistGoTop,chlistGoBottom;
    private int childPos = 0;
    private long startOnchChannelId;
    private List<SimpleChannel> programList = new ArrayList<>();//Scoty 20181109 modify for skip channel

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_settings);

        //mCotext = this;
        InitTitleHelp();
        InitSelectBox();
        InitStartOnChannel();
    }

    private void InitTitleHelp() {
        Log.d(TAG, "InitTitleHelp: ");
        ActivityTitleView title;
        title = (ActivityTitleView) findViewById(R.id.systemSettingsTitleLayout);
        help = (ActivityHelpView) findViewById(R.id.systemSettingsHelpLayout);

        orgStr = getString(R.string.STR_SYSTEM_SETTINGS_STARTUP_CH_MSG);

        title.setTitleView(getString(R.string.STR_SYSTEM_SETTINGS_TITLE));
        help.setHelpInfoTextBySplit(orgStr);
        help.resetHelp(1,0,null);
        help.resetHelp(2,0,null);
        help.resetHelp(3,0,null);
        help.resetHelp(4,0,null);
    }

    private void InitSelectBox() {
        Log.d(TAG, "InitSelectBox()");
        Resources res = getResources();

        Spinner spinnerBanTime;
        Spinner spinnerOSD;
        Spinner spinnerDeepStby;
        Spinner spinnerAutoStby;
        //spinnerStartCh  = (Spinner) findViewById(R.id.valStartupCh);
        spinnerBanTime  = (Spinner) findViewById(R.id.valBannerDisplayTime);
        spinnerOSD      = (Spinner) findViewById(R.id.valOsdTransparency);
        spinnerDeepStby = (Spinner) findViewById(R.id.valDeepStandbyAfter);
        spinnerAutoStby = (Spinner) findViewById(R.id.valAutoStandbyAfter);

        /*selStartChannel = new SelectBoxView(this,
                spinnerStartCh, res.getStringArray(R.array.STR_STARTUP_CHANNEL_ARY));*/
        selBannerTime = new SelectBoxView(this,
                spinnerBanTime, res.getStringArray(R.array.STR_BANNER_DISPLAY_TIME_ARY));
        selOSDTransparency = new SelectBoxView(this,
                spinnerOSD, res.getStringArray(R.array.STR_OSD_TRANSPARENCY_ARY));
        selDeepStandby = new SelectBoxView(this,
                spinnerDeepStby, res.getStringArray(R.array.STR_DEEP_STANDBY_AFTER_ARY));
        selAutoStandby = new SelectBoxView(this,
                spinnerAutoStby, res.getStringArray(R.array.STR_AUTO_STANDBY_AFTER_ARY));

        // change help info when moving to 1st spinner
        /*spinnerStartCh.setOnFocusChangeListener(new SelectBoxView.SelectBoxtOnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                super.onFocusChange(v, hasFocus);
                if (hasFocus)
                    help.setHelpInfoTextBySplit(orgStr);
                else
                    help.setHelpInfoTextBySplit(orgStr);
            }
        });*/
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        GposInfo gposInfo = GposInfoGet();

        // Load Data
        //selStartChannel.SetSelectedItemIndex(GposInfoGet().getStartOnChType());
        int bannerTime = gposInfo.getBannerTimeout();
        if(bannerTime > 0)
            bannerTime -= 1;
        selBannerTime.SetSelectedItemIndex(bannerTime);
        selOSDTransparency.SetSelectedItemIndex(gposInfo.getOSDTransparency());
        selDeepStandby.SetSelectedItemIndex(gposInfo.getDeepSleepMode());
        selAutoStandby.SetSelectedItemIndex(gposInfo.getAutoStandbyTime());
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        // save data
        GposInfo gposInfo = GposInfoGet();
        gposInfo.setStartOnChannelId(startOnchChannelId);
        if (startOnchChannelId == 0)
        {
            gposInfo.setStartOnChType(ProgramInfo.ALL_TV_TYPE);
        }
        else
        {
            int chType = ProgramInfoGetByChannelId(startOnchChannelId).getType();
            gposInfo.setStartOnChType(chType);
        }

        gposInfo.setBannerTimeout(selBannerTime.GetSelectedItemIndex()+1);
        gposInfo.setOSDTransparency(selOSDTransparency.GetSelectedItemIndex());
        gposInfo.setDeepSleepMode(selDeepStandby.GetSelectedItemIndex());
        gposInfo.setAutoStandbyTime(selAutoStandby.GetSelectedItemIndex());
        GposInfoUpdate(gposInfo);
    }

    public void InitStartOnChannel()
    {
        Log.d(TAG, "InitStartOnChannel: ");
        programList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_TV_TYPE,1);//Scoty 20181109 modify for skip channel
        startOnchChannelId = GposInfoGet().getStartOnChannelId();
        if(GetSimpleProgramByChannelIdfromTotalChannelListByGroup(ViewHistory.getCurGroupType(), startOnchChannelId)==null){//eric lin 20180802 fix after delete all tv, enter system setting crash
            startOnchChannelId = 0;
        }
        //int startOnchType = GposInfoGet().getStartOnChType();

        strFixedChannel = getString(R.string.STR_FIXED_CHANNEL_SELECT);
        strLastWatch = getString(R.string.STR_LAST_WATCHED);
        startonchannlTXV = (TextView) findViewById(R.id.startonchannlTXV);
        startonchannlTXV.requestFocus();

        Log.d(TAG, "InitStartOnChannel: startOnchChannelId == " + startOnchChannelId);
        if(startOnchChannelId == 0)
        {
            startonchannlTXV.setText(strLastWatch);
        }
        else {
            //Scoty 20181109 modify for skip channel -s
            int position = 0;
            for(int i = 0 ; i < programList.size() ;i++)
            {
                if(startOnchChannelId == programList.get(i).getChannelId())
                    position = i;
            }

            Log.d(TAG, "position = "+position);
            if(position == -1)//ethan 20180801 fixed APK crash
                position = 0;

            String string = Integer.toString(
                    programList.get(position).getChannelNum()) + " "
                    + programList.get(position).getChannelName();
            startonchannlTXV.setText(string);
            //Scoty 20181109 modify for skip channel -e
        }

        startonchannlTXV.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String startUpChannel = (String) startonchannlTXV.getText();
                if (event.getAction() == ACTION_DOWN) {
                    switch (keyCode) {
                        // Johnny 20181219 for mouse control, handled in onclick listener
//                        case KEYCODE_DPAD_CENTER:
//                            Log.d(TAG, "onKey: press OK");
//                            if (!startUpChannel.equals(strLastWatch) )
//                                DoYouWantFixedCH();//open fixed channel dialog
//                            break;
                        case KEYCODE_DPAD_RIGHT:
                            Log.d(TAG, "onKey: press right");
                            if ( startUpChannel.equals(strLastWatch) ) {
                                startonchannlTXV.setText(strFixedChannel);
                            }
                            else
                            {
                                startonchannlTXV.setText(strLastWatch);
                                startOnchChannelId = 0;
                                setDefaultOpenChannel(startOnchChannelId, ProgramInfo.ALL_TV_TYPE);
                            }
                            break;
                        case KEYCODE_DPAD_LEFT:
                            Log.d(TAG, "onKey: press left");
                            if ( startUpChannel.equals(strLastWatch) ) {
                                startonchannlTXV.setText(strFixedChannel);
                            }
                            else
                            {
                                startonchannlTXV.setText(strLastWatch);
                                startOnchChannelId = 0;
                                setDefaultOpenChannel(startOnchChannelId, ProgramInfo.ALL_TV_TYPE);
                            }
                            break;
                    }
                }
                return false;
            }
        });

        startonchannlTXV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: ");
                if (hasFocus)
                    help.setHelpInfoTextBySplit(orgStr);
                else
                    help.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
            }
        });

        // Johnny 20181219 for mouse control -s
        startonchannlTXV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String startUpChannel = (String) startonchannlTXV.getText();
                if (!startUpChannel.equals(strLastWatch)) {
                    DoYouWantFixedCH();//open fixed channel dialog
                }
            }
        });

        startonchannlTXV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String startUpChannel = (String) startonchannlTXV.getText();
                if ( startUpChannel.equals(strLastWatch) ) {
                    startonchannlTXV.setText(strFixedChannel);
                }
                else
                {
                    startonchannlTXV.setText(strLastWatch);
                    startOnchChannelId = 0;
                    setDefaultOpenChannel(startOnchChannelId, ProgramInfo.ALL_TV_TYPE);
                }
                return true;
            }
        });
        // Johnny 20181219 for mouse control -e
    }


    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private List<SimpleChannel> listItems;//Scoty 20181109 modify for skip channel

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView chnum;
            TextView chname;
            ImageView scramble;
            ImageView fav;
            ImageView chlock;

            ViewHolder(View itemView) {
                super(itemView);
                chnum = (TextView) itemView.findViewById(R.id.chnumTXV);//LCN
                chname = (TextView) itemView.findViewById(R.id.chnameTXV);//Channel name
                scramble = (ImageView) itemView.findViewById(R.id.scrambleIGV);//Scramble icon
                fav = (ImageView) itemView.findViewById(R.id.favIGV);//group icon
                chlock = (ImageView) itemView.findViewById(R.id.lockIGV);//Lock icon

            }
        }

        RecyclerAdapter(List<SimpleChannel> list) {//Scoty 20181109 modify for skip channel
            listItems = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from( parent.getContext() )
                    .inflate(R.layout.tvmanager_list_item, parent, false);

            convertView.setOnClickListener(new chlistOnClick());
            convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
//                            Log.d(TAG, "onFocusChange: channel = "+ ((TextView) v.findViewById(R.id.chnameTXV)).getText());
                        v.setBackgroundResource(R.drawable.focus_list);
                    }
                }
            });

            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            String string = Integer.toString(listItems.get(position).getChannelNum());
            holder.chnum.setText(string);
            holder.chname.setText(listItems.get(position).getChannelName());

            if(listItems.get(position).getCA() == 1)
                holder.scramble.setBackgroundResource(R.drawable.scramble);
            else
                holder.scramble.setBackgroundResource(android.R.color.transparent);

            if(listItems.get(position).getUserLock() == 1)
                holder.chlock.setBackgroundResource(R.drawable.lock);
            else
                holder.chlock.setBackgroundResource(android.R.color.transparent);


            holder.itemView.getLayoutParams().height = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

            //for Marquee,--start
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        holder.chname.setSelected(true);
                        holder.chnum.setSelected(true);
                    }
                    else {
                        holder.chname.setSelected(false);
                        holder.chnum.setSelected(false);
                    }
                }
            });
            //for Marquee,--end
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            if (listItems == null) {
                return 0;
            }
            else
            {
                return listItems.size();//list size
            }
        }
    }

    class chlistOnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            final int position = chlistview.getChildLayoutPosition(v);
            Log.d(TAG,"chlistOnClick curChannelIndex" + position);
            setSelectChannel(position);
            dismissDialog();
        }
    }

    public void DoYouWantFixedCH()//Yes : select channel ; No: cancel
    {
        Log.d(TAG," DoYouWantToFixedCH ");
        new SureDialog(SystemSettingsActivity.this){
            public void onSetMessage(View v){
                ((TextView)v).setText(getString(R.string.STR_DO_YOU_WANT_TO_DEFINE_START_CHANNEL));
            }
            public void onSetNegativeButton() {
                startonchannlTXV.setText(strFixedChannel);
                startOnchChannelId = 0;
                setDefaultOpenChannel(startOnchChannelId, ProgramInfo.ALL_TV_TYPE);
            }
            public void onSetPositiveButton(){
                //Scoty 20180622 protect startonchannel dialog crash when no channel -s
                if(programList.size() > 0)//Scoty 20181109 modify for skip channel
                    SelectCH();//show start on channel dialog
                else
                {
                    String str = getString(R.string.STR_NO_CHANNELS_FOUND);
                    new MessageDialogView(SystemSettingsActivity.this, str, 3000) {
                        public void dialogEnd() {
                        }
                    }.show();
                }
                //Scoty 20180622 protect startonchannel dialog crash when no channel -e
            }
        };

    }

    public void SelectCH()
    {
        Log.d(TAG, "SelectCH: ");
        //Dialog mdialog;
        mDialog = new Dialog(SystemSettingsActivity.this, R.style.MyDialog);
        mDialog.setOnKeyListener(new Dialog.OnKeyListener(){
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if(event.getAction() == ACTION_DOWN)
            {
                View okView = chlistview.getFocusedChild();
                int curtv_position = chlistview.getChildAdapterPosition(okView);
                int listViewHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
                int okCount = chAdapter.getItemCount();
                int okOffset = okCount * listViewHeight;
                int childCount = chlistview.getChildCount();
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DPAD_UP:
                    {
                        if (curtv_position == 0) {
                            Log.d(TAG, "onKey: focus bottom item");
                            //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                            final int position = okCount-1;
                            childPos = childCount - 1;
                            chlistview.scrollToPosition(position);
                            chlistview.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    View view = chlistview.getLayoutManager().findViewByPosition(position);
                                    if (view != null) {
                                        view.requestFocus();
                                    }
                                }
                            }, 0);
//                            chlistview.scrollBy(0, okOffset);
                            //chlistGoBottom = true;
//                            chlistview.getChildAt(childPos).requestFocus();
                            //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                            return true;
                        }
                        else {
                            if (childPos == 0) {
                                Log.d(TAG, "onKey: scroll up");
                                chlistview.scrollBy(0, -listViewHeight);
                                childPos = 0;
                                chlistview.getChildAt(childPos).requestFocus();
                                return true;
                            }
                            else if (childPos > 0) {
                                Log.d(TAG, "onKey: move up");
                                childPos--;
                            }
                        }
                    }break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    {
                        if ((curtv_position >= 0) && (curtv_position == okCount - 1)) {
                            Log.d(TAG, "onKey: focus top item");
                            //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                            childPos = 0;
                            chlistview.scrollToPosition(0);
                            chlistview.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    View view = chlistview.getLayoutManager().findViewByPosition(childPos);
                                    if (view != null) {
                                        view.requestFocus();
                                    }
                                }
                            }, 0);

//                            chlistview.scrollBy(0, (-okOffset));
//                            chlistview.getChildAt(childPos).requestFocus();
                            //chlistGoTop = true;
                            //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                            return true;
                        }
                        else {
                            if (childPos == (childCount - 1)) {
                                Log.d(TAG, "onKey: scroll down");
                                chlistview.scrollBy(0, listViewHeight);
                                chlistview.getChildAt(childPos).requestFocus();
                                childPos = childCount - 1;
                                return true;
                            }
                            else if (childPos < (childCount - 1)) {
                                Log.d(TAG, "onKey: move down");
                                childPos = childPos + 1;
                            }
                        }
                    }break;
                    case KeyEvent.KEYCODE_BACK:
                    {
                        help.setHelpInfoTextBySplit(orgStr); // edwin 20180709 add help info
                        dialog.dismiss();
                    }break;

                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        help.setHelpInfoTextBySplit(orgStr); // edwin 20180709 add help info
                        break;

                    // edwin 20180709 add page up & page down -s

                    case KeyEvent.KEYCODE_PAGE_DOWN:
                        PagePrev(chlistview);
                        break;

                    case KeyEvent.KEYCODE_PAGE_UP:
                        PageNext(chlistview);
                        break;

                    // edwin 20180709 add page up & page down -e
                }

            }
            return false;
            }

        });
        childPos = 0;
        if(mDialog == null) {
            return;
        }

        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190510 fix dialog has no focus
            @Override
            public void run () {
                mDialog.show();
            }
        }, 150);
        //mDialog.show();
        mDialog.setContentView(R.layout.timer_channel_select_dialog);

        Window window = mDialog.getWindow();
        WindowManager.LayoutParams lp = null;

        if (window != null)
            lp = window.getAttributes();

        if (lp != null)
            lp.dimAmount = 0.0f;

        if (window != null) {
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        if (lp != null)
        {
            help.setHelpInfoTextBySplit(getString(R.string.STR_FIXED_CH_HELP_INFO)); // edwin 20180709 add help info
            DialogAddCH(lp.width);//init dialog
        }
    }

    public void DialogAddCH(int width)
    {
        Log.d(TAG,"DialogAddCH: ");
        int listCount;
        int itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

        Window window = mDialog.getWindow();
        if (window != null) {
            chlistview = (RecyclerView) window.findViewById(R.id.chlisLIV);
        }
        chAdapter = new RecyclerAdapter(programList);//Scoty 20181109 modify for skip channel
        //for largest display size,--start
        int mheight = getResources().getDisplayMetrics().heightPixels;
        Guideline top = (Guideline) mDialog.getWindow().findViewById(R.id.title_guideline2);
        Guideline bottom = (Guideline) mDialog.getWindow().findViewById(R.id.bottom_guideline);
        float topPercent = ((ConstraintLayout.LayoutParams)top.getLayoutParams()).guidePercent;
        float bottomPercent = ((ConstraintLayout.LayoutParams)bottom.getLayoutParams()).guidePercent;
        float guideLineRange = bottomPercent - topPercent;
        listCount = (int)(mheight*guideLineRange)/itemHeight; //0.77-0.22
        //for largest display size,--end
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                width, itemHeight * listCount);
        chlistview.setLayoutParams(layoutParams);
        chlistview.setAdapter(chAdapter);
        chAdapter.notifyDataSetChanged();
        /*chlistview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrolled: ");
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged: ");

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (chlistGoTop) {
                        chlistGoTop = false;
                        recyclerView
                                .getChildAt(0)
                                .requestFocus();
                    }
                    else if (chlistGoBottom) {
                        chlistGoBottom = false;
                        recyclerView
                                .getChildAt(recyclerView.getChildCount()-1)
                                .requestFocus();
                    }
                }
            }
        });
        chlistGoTop = false;
        chlistGoBottom = false;*/
    }

    public void setSelectChannel(int position)
    {
        //Scoty 20181109 modify for skip channel -s
        String text = Integer.toString(
                programList.get(position).getChannelNum()) + " " +
                programList.get(position).getChannelName();
        startonchannlTXV.setText(text);

        startOnchChannelId = programList.get(position).getChannelId();
        int chType = ProgramInfoGetByChannelId(startOnchChannelId).getType();
        setDefaultOpenChannel(startOnchChannelId, chType);
        //Scoty 20181109 modify for skip channel -e
    }

    public void dismissDialog(){
        Log.d(TAG, "dismissDialog");
        if(mDialog!=null&& mDialog.isShowing()){
            mDialog.dismiss();//close dialog
        }
    }

    public boolean isShowing() {
        Log.d(TAG, "isShowing");
        //check dialog is exist
        return mDialog != null && mDialog.isShowing();
    }

    // edwin 20180709 add page up & page down -s
    private void PagePrev(RecyclerView list)
    {
        Log.d(TAG, "PagePrev: ");

        int topPos  = list.getChildAdapterPosition(list.getChildAt(0));
        int curPos  = list.getChildAdapterPosition(list.getFocusedChild());
        int samePos = curPos - topPos;
        float totalHeight;

        if ( list.getChildAt(0) != null )
        {
            totalHeight = list
                    .getChildAt(0)
                    .getMeasuredHeight() * list.getAdapter().getItemCount();
        }
        else
        {
            totalHeight = getResources()
                    .getDimension(R.dimen.LIST_VIEW_HEIGHT) * list.getAdapter().getItemCount();
        }

        if ( list.getChildAdapterPosition(list.getChildAt(0)) == 0 )
        {
            list.scrollBy(0, (int) totalHeight);
        }
        else
        {
            list.scrollBy(0, -list.getLayoutParams().height);
        }

        list.getChildAt(samePos).requestFocus();
    }

    private void PageNext(RecyclerView list)
    {
        Log.d(TAG, "PageNext: ");

        int topPos  = list.getChildAdapterPosition(list.getChildAt(0));
        int curPos  = list.getChildAdapterPosition(list.getFocusedChild());
        int samePos = curPos - topPos;
        float totalHeight;

        if ( list.getChildAt(0) != null )
        {
            totalHeight = list.getChildAt(0).getMeasuredHeight() * list.getAdapter().getItemCount();
        }
        else
        {
            totalHeight = getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT) * list.getAdapter().getItemCount();
        }

        if ( list.getChildAdapterPosition(list
                .getChildAt(list.getChildCount()-1)) == (list.getAdapter().getItemCount()-1) )
        {
            list.scrollBy(0, (int) -totalHeight);
        }
        else
        {
            list.scrollBy(0, list.getLayoutParams().height);
        }

        list.getChildAt(samePos).requestFocus();
    }
    // edwin 20180709 add page up & page down -e
}