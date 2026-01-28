package com.prime.dtvplayer.Activity;

/*
  Created by scoty_kuo on 2017/11/08.
 */

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.PasswordDialogView;

import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.FOCUS_AFTER_DESCENDANTS;
import static androidx.recyclerview.widget.RecyclerView.FOCUS_BLOCK_DESCENDANTS;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_PAGE_DOWN;
import static android.view.KeyEvent.KEYCODE_PAGE_UP;
import static android.view.KeyEvent.KEYCODE_PROG_BLUE;
import static android.view.KeyEvent.KEYCODE_PROG_GREEN;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static android.view.KeyEvent.KEYCODE_PROG_YELLOW;

public class TVManagerActivity extends DTVActivity {
    private static final String TAG = "TVManagerActivity";
    private static int visibleItemCount = 9;
    private RecyclerView tvlistview;
    private RecyclerView favlistview;
    private TvFavAdapter tvAdapter=null;
    private TvFavAdapter favAdapter=null;
    private int move_flag = 0, lock_flag = 0, delete_flag = 0;//set mode
    private boolean hide_flag = false;//Scoty 20181113 add Hide function UI
    private int tv_move_channel = -1;
    private int fav_move_channel = -1;
    private int curFavGroup = 1; //current Favorite group
    private int serviceType = 0,groupNum = 0;
    private ActivityTitleView setActivityTitleView;
    private int tvChildPos = 0;
    private int favChildPos = 0;
    private int tvCurPos = -1;
    private LinearLayoutManager tvLayoutManager = null;
    private LinearLayoutManager favLayoutManager = null;
    private int deleteAllSelect = 0;
    private List<ProgramManagerImpl> ProgramManagerList = null;
    private boolean isCloseFun = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tvmanager);
//R.styleable.FontFamilyFont
        //for largest display size,--start
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int itemHeight =  ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        //Log.d(TAG, "TTT onCreate:    width = " + width + "    height = " + height + ", itemHeight="+itemHeight);
        //Scoty 20180523 change to use guideline value to set visible count -s
        Guideline bottmguideline = (Guideline) findViewById(R.id.listviewbottomguideline);
        Guideline topguideline = (Guideline) findViewById(R.id.body_title_guideline);
        ConstraintLayout.LayoutParams paramsBottom = (ConstraintLayout.LayoutParams) bottmguideline.getLayoutParams();
        ConstraintLayout.LayoutParams paramsTop = (ConstraintLayout.LayoutParams) topguideline.getLayoutParams();

        visibleItemCount = (int)(height*(paramsBottom.guidePercent - paramsTop.guidePercent))/itemHeight; //80%-22%=0.58
        //Scoty 20180523 change to use guideline value to set visible count -e
        //Log.d(TAG, "TTT onCreate: displayedCount = " + displayedCount);
        //for largest display size,--end

        TVManagerInit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                tvlistview.getWidth(), itemHeight * visibleItemCount);

        tvlistview.setLayoutParams(layoutParams);
        favlistview.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        View tvView = tvlistview.getFocusedChild();
        View favView = favlistview.getFocusedChild();
        int listViewHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        int curTvPos = tvlistview.getChildAdapterPosition(tvView);
        int curFavPos = favlistview.getChildAdapterPosition(favView);
        int tvItemCount = tvAdapter.getItemCount();
        int favItemCount = favAdapter.getItemCount();
        int tvOffset = tvItemCount * listViewHeight;
        int favOffset = favItemCount * listViewHeight;

        switch (keyCode) {

            // edwin 20180709 add page up & page down -s
//            case KeyEvent.KEYCODE_MENU://Scoty 20181113 add Hide function UI
//                hide_flag = !hide_flag;
//                setHideMode();
//                break;

            case KEYCODE_PAGE_DOWN:
                if ( tvlistview.hasFocus() )
                {
                    PagePrev(tvlistview);
                }
                else if ( favlistview.hasFocus() )
                {
                    PagePrev(favlistview);
                }
                break;

            case KEYCODE_PAGE_UP:
                if ( tvlistview.hasFocus() )
                {
                    PageNext(tvlistview);
                }
                else if ( favlistview.hasFocus() )
                {
                    PageNext(favlistview);
                }
                break;

            // edwin 20180709 add page up & page down -e

            case KEYCODE_DPAD_DOWN:
                Log.d(TAG, "onKeyDown: DOWN");
                if ((curTvPos >= 0) && tvlistview.hasFocus()) {
                    if (curTvPos == tvItemCount - 1) {
                        Log.d(TAG, "onKeyDown: Tv go top");
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        tvChildPos = 0;
                        tvlistview.scrollToPosition(0);
                        tvlistview.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = tvLayoutManager.findViewByPosition(tvChildPos);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);
//                        tvlistview.scrollBy(0, (-tvOffset));
//                        tvlistview.getChildAt(0).requestFocus();
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        return true;
                    }
                    else if ((tvChildPos+1) == visibleItemCount) {
                        Log.d(TAG, "onKeyDown: Tv scroll down 1 item");
                        tvlistview.scrollBy(0, listViewHeight);
                    }
                    else {
                        Log.d(TAG, "onKeyDown: Tv not scroll");
                        tvChildPos++;
                    }
                    favChildPos = tvChildPos;
                }
                else if ((curFavPos >= 0) && favlistview.hasFocus()) {
                    if (curFavPos == (favItemCount - 1)) {
                        Log.d(TAG, "onKeyDown: Fav go top");
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        favChildPos = 0;
                        favlistview.scrollToPosition(0);
                        favlistview.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = favLayoutManager.findViewByPosition(favChildPos);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);
//                        favlistview.scrollBy(0, (-favOffset));
//                        favlistview.getChildAt(0).requestFocus();
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        return true;
                    } else if ((favChildPos + 1) == visibleItemCount) {
                        Log.d(TAG, "onKeyDown: Fav scroll down 1 item");
                        favlistview.scrollBy(0, listViewHeight);
                    } else {
                        Log.d(TAG, "onKeyDown: Fav not scroll");
                        favChildPos++;
                    }
                    tvChildPos = favChildPos;
                }
                break;
            case KEYCODE_DPAD_UP:
                Log.d(TAG, "onKeyDown: UP");
                if (tvlistview.hasFocus()) {
                    if (curTvPos == 0) {
                        Log.d(TAG, "onKeyDown: Tv go bottom");
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        final int position = tvItemCount-1;
                        tvChildPos = tvlistview.getChildCount() - 1;
                        tvlistview.scrollToPosition(position);
                        tvlistview.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = tvLayoutManager.findViewByPosition(position);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);
//                        tvlistview.scrollBy(0, tvOffset);
//                        tvlistview.getChildAt(tvChildPos).requestFocus();
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        return true;
                    } else if (tvChildPos == 0) {
                        Log.d(TAG, "onKeyDown: Tv scroll up 1 item");
                        tvlistview.scrollBy(0, -listViewHeight);
                    } else {
                        Log.d(TAG, "onKeyDown: Tv not scroll");
                        tvChildPos--;
                    }
                    favChildPos = tvChildPos;
                }
                else if (favlistview.hasFocus()) {
                    if (curFavPos == 0) {
                        Log.d(TAG, "onKeyDown: Fav go bottom");
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        final int position = favItemCount-1;
                        favChildPos = favlistview.getChildCount() - 1;
                        favlistview.scrollToPosition(position);
                        favlistview.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = favLayoutManager.findViewByPosition(position);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);
//                        favlistview.scrollBy(0, favOffset);
//                        favlistview.getChildAt(favChildPos).requestFocus();
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        return true;
                    } else if (favChildPos == 0) {
                        Log.d(TAG, "onKeyDown: Fav scroll up 1 item");
                        favlistview.scrollBy(0, -listViewHeight);
                    } else {
                        Log.d(TAG, "onKeyDown: Fav not scroll");
                        favChildPos--;
                    }
                    tvChildPos = favChildPos;
                }
                break;
            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: KEYCODE_PROG_RED set delete mode");
                onProgRedClicked();     // Johnny 20181228 for mouse control
                break;
            case KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG,"onKeyDown: KEYCODE_PROG_GREEN change FAV group");
                onProgGreenClicked();   // Johnny 20181228 for mouse control
            }break;
            case KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                Log.d(TAG,"onKeyDown: KEYCODE_PROG_YELLOW set move mode in move mode");
                onProgYellowClicked();  // Johnny 20181228 for mouse control
                break;
            case KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                Log.d(TAG,"onKeyDown: KEYCODE_PROG_BLUE set lock mode");
                onProgBlueClicked();    // Johnny 20181228 for mouse control
                break;
            case KEYCODE_BACK:
                Log.d(TAG,"onKeyDown: KEYCODE_BACK");
                if(delete_flag != 0)//check delete channel dialog
                    checkDeleteChannelDialog();
                else
                    ProgramManagerSave(ProgramManagerList);//save list
                break;
            case KEYCODE_DPAD_LEFT:
                Log.d(TAG, "onKeyDown: LEFT");
                //move,delete,lock,hide mode can not use left/right key
                if (move_flag != 0 || delete_flag != 0 || lock_flag != 0 || hide_flag) {//Scoty 20181113 add Hide function UI
                    Log.d(TAG, "onKeyDown: move, delete, lock mode cannot use left key");
                    return true;
                }
                if (favlistview.hasFocus()) {
                    Log.d(TAG, "onKeyDown: UNBLOCK TV LIST");
                    tvlistview.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                    tvChildPos = favChildPos;
                    if (tvlistview.getChildAt(tvChildPos) != null) {
                        Log.d(TAG, "onKeyDown: visible tv pos not null = "+ tvChildPos);
                        tvlistview.getChildAt(tvChildPos).requestFocus();
                    }
                    else {
                        Log.d(TAG, "onKeyDown: visible tv pos null");
                        tvlistview.getChildAt(0).requestFocus();
                        tvChildPos = 0;
                    }
                }
                else if (tvlistview.hasFocus()) {
                    tvlistview.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                    if (tvlistview.getChildAt(tvChildPos) != null)
                        tvlistview.getChildAt(tvChildPos).requestFocus();
                    else {
                        tvlistview.getChildAt(0).requestFocus();
                        tvChildPos = 0;
                    }
                    return true;
                }
                break;
            case KEYCODE_DPAD_RIGHT:
                Log.d(TAG, "onKeyDown: RIGHT");
                //move,delete,lock,hide mode can not use left/right key
                if (move_flag != 0 || delete_flag != 0 || lock_flag != 0 || hide_flag) {//Scoty 20181113 add Hide function UI
                    Log.d(TAG, "onKeyDown: move, delete, lock mode cannot use right key");
                    return true;
                }
                if (favAdapter.getItemCount() == 0)
                    return true;
                if (tvlistview.hasFocus()) {
                    Log.d(TAG, "onKeyDown: BLOCK TV LIST");
                    tvlistview.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                    favChildPos = tvChildPos;
                    if (favlistview.getChildAt(favChildPos) != null)
                        favlistview.getChildAt(favChildPos).requestFocus();
                    else {
                        favlistview.getChildAt(0).requestFocus();
                        favChildPos = 0;
                    }
                }
                break;
        }
        Log.d(TAG, "onKeyDown:"+
                "\n visible tv pos = "+ tvChildPos+
                "\n visible fav pos = "+ favChildPos+
                "\n curTvPos = "+ curTvPos+
                "\n curFavPos = "+ curFavPos
        );
        return super.onKeyDown(keyCode, event);
    }

    public void TVManagerInit()
    {
        Log.d(TAG,"TVManagerInit");
        ActivityHelpView setActivityHelpView;
        int TvFavTableMax = 6, RadioFavTableMax = 2;
        deleteAllSelect = 0;

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

//        setActivityHelpView.resetHelp(1,R.drawable.help_red,getString(R.string.STR_DELETE));
//        setActivityHelpView.resetHelp(2,R.drawable.help_green,getString(R.string.STR_FAVORITE));
//        setActivityHelpView.resetHelp(3,R.drawable.help_yellow,getString(R.string.STR_MOVE));
//        //setActivityHelpView.resetHelp(4,R.drawable.help_blue,getString(R.string.STR_LOCK));
//        setActivityHelpView.resetHelp(4,R.drawable.help_blue,null);

        setActivityHelpView.resetHelp(1, 0, null);
        setActivityHelpView.resetHelp(2, 0, null);
        setActivityHelpView.resetHelp(3, 0, null);
        setActivityHelpView.resetHelp(4, 0, null);

        // Johnny 20181228 for mouse control -s
        setActivityHelpView.setHelpIconClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgRedClicked();
            }
        });
        setActivityHelpView.setHelpIconClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgGreenClicked();
            }
        });
        setActivityHelpView.setHelpIconClickListener(3, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgYellowClicked();
            }
        });
        setActivityHelpView.setHelpIconClickListener(4, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgBlueClicked();
            }
        });
        // Johnny 20181228 for mouse control -e

        tvlistview = (RecyclerView) this.findViewById(R.id.tvlistLIV);
        favlistview = (RecyclerView)this.findViewById(R.id.favlistLIV);

        tvAdapter = new TvFavAdapter((ProgramManagerList.get(0).ProgramManagerInfoList));//TV or Radio channel
        favAdapter = new TvFavAdapter((ProgramManagerList.get(curFavGroup).ProgramManagerInfoList));//first fav group

        // setHasStableIds() will cause that incorrectly notifyItemMoved()
        //tvAdapter.setHasStableIds(true);
        //favAdapter.setHasStableIds(true);

        tvLayoutManager = new LinearLayoutManager(this);
        // this is for "avoid notifyItemRemoved() error"
        /*tvLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return tvChildPos == 0 ? false : super.supportsPredictiveItemAnimations();
            }
        };*/
        tvLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        tvlistview.setLayoutManager(tvLayoutManager);
        tvlistview.setAdapter(tvAdapter);
        tvlistview.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == KEYCODE_DPAD_LEFT || keyCode == KEYCODE_DPAD_RIGHT){
                    //move,delete,lock mode can not use left/right key
                    if(move_flag != 0 || delete_flag != 0 || lock_flag != 0)
                        return true;
                }
                return false;
            }
        });

        favLayoutManager = new LinearLayoutManager(this);
        // this is for "avoid notifyItemRemoved() error"
        /*favLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return favChildPos == 0 ? false : super.supportsPredictiveItemAnimations();
            }
        };*/
        favLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        favlistview.setLayoutManager(favLayoutManager);
        favlistview.setAdapter(favAdapter);
        favlistview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_DPAD_LEFT || keyCode == KEYCODE_DPAD_RIGHT) {
                    //move,delete,lock mode can not use left/right key
                    if(move_flag != 0 || delete_flag != 0 || lock_flag != 0)
                        return true;
                }
                return false;
            }
        });

        //for calc visibleHeight,--start
        int itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        int visibleHeight = itemHeight * visibleItemCount;

        ViewGroup.LayoutParams layoutParams = tvlistview.getLayoutParams();
        layoutParams.height = visibleHeight;
        tvlistview.setLayoutParams(layoutParams);
        favlistview.setLayoutParams(layoutParams);
        //for calc visibleHeight,--end

    }

    public void checkDeleteChannelDialog()//Yes : delete ; No: cancel
    {
        Log.d(TAG," checkDeleteChannelDialog ");
        new SureDialog(TVManagerActivity.this){
            public void onSetMessage(View v){
                ((TextView)v).setText(getString(R.string.STR_DO_YOU_WANT_TO_DELETE_CHANNEL));
            }
            public void onSetNegativeButton(){
            }
            public void onSetPositiveButton(){
                //Log.d(TAG," deleteSelectChannel "+ProgramManagerList.get(0).ProgramManagerInfoList.size());
                ProgramManagerSave(ProgramManagerList);//save list
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

    //Scoty 20180524 fixed lock symbol not clean after enter TV manager again
    private void setFavoriteUserLockOnOff(int tv_channel_position, int lock) {
        for (int i = 1; i < ProgramManagerList.size(); i++) {
            for (int j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {
                if (ProgramManagerList.get(0).ProgramManagerInfoList.get(tv_channel_position).getChannelId()
                        == ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelId()) {
                    ProgramManagerList.get(i).ProgramManagerInfoList.get(j).setUserLock(lock);
                    break;
                }
            }
        }
    }

    private void setFavoriteHideOnOff(int tv_channel_position, int hide)//Scoty 20181113 add Hide function UI
    {
        for (int i = 1; i < ProgramManagerList.size(); i++) {
            for (int j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {
                if (ProgramManagerList.get(0).ProgramManagerInfoList.get(tv_channel_position).getChannelId()
                        == ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelId()) {
                    ProgramManagerList.get(i).ProgramManagerInfoList.get(j).setChannelSkip(hide);
                    break;
                }
            }
        }
    }
    class tvlistOnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            final int position = tvlistview.getChildLayoutPosition(v);

            if(move_flag == 1)//move tv mode
            {
                if(tv_move_channel == -1) {
                    tv_move_channel = position;
                    ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setMoveIcon(1);//set move symbol
                }
                else {
                    tvAdapter.notifyItemMoved(tv_move_channel, position);
                    ProgramManagerList.get(0).MoveProgram(ProgramManagerList,tv_move_channel, position);//move channel
                    ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setMoveIcon(0);//clean move symbol

                    //set first channel focus
                    /*Log.d(TAG, "onClick:" +
                            " tvChildPos = "+ tvChildPos+
                            " position = "+ position+
                            " tv_move_channel = "+ tv_move_channel
                    );*/

                    View firstView = tvLayoutManager.findViewByPosition(0);
                    View lastView = tvLayoutManager.findViewByPosition(tvAdapter.getItemCount()-1);
                    boolean goUp = position < tv_move_channel;
                    boolean goDown = position > tv_move_channel;
                    int itemHeight = (int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);

                    if (goUp && (lastView == null)) {
                        /*Log.d(TAG, "onClick: go up");*/
                        tvlistview.smoothScrollBy(0, -itemHeight);
                    }
                    if (goDown && (firstView == null)) {
                        /*Log.d(TAG, "onClick: go down");*/
                        tvlistview.smoothScrollBy(0, itemHeight);
                    }

                    tvlistview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View view = tvLayoutManager.findViewByPosition(position);
                            if (view != null) {
                                view.requestFocus();
                            }
                        }
                    }, 0);
                    tv_move_channel = -1;
                }
                tvAdapter.notifyItemChanged(position);//update Move symbol
            }
            else if(lock_flag == 1)//lock mode
            {
                Log.d(TAG, "onClick: lock mode");
                Log.d(TAG,"onClick: lock tv channel " + position);
                int lock = 0;
                if(ProgramManagerList.get(0).ProgramManagerInfoList.get(position).getUserLock() == 0)
                    lock = 1;
                ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setUserLock(lock);
                setFavoriteUserLockOnOff(position,lock);//Scoty 20180524 fixed lock symbol not clean after enter TV manager again
                tvAdapter.notifyItemChanged(position);
                favAdapter.notifyDataSetChanged();
            }
            else if(delete_flag == 1)//delete mode
            {
                Log.d(TAG, "onClick: delete mode");
                Log.d(TAG,"onClick: delete tv channel " + position);
                int delete = 0;
                if(ProgramManagerList.get(0).ProgramManagerInfoList.get(position).getDelete() == 0)
                    delete = 1;
                //ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setDelete(delete);
                ProgramManagerList.get(0).DelProgram(ProgramManagerList,position,delete);
                tvAdapter.notifyItemChanged(position);

            }
            else if(delete_flag == 2)//delete All channel mode
            {
                Log.d(TAG, "onClick: delete All channel mode deleteAllSelect = " + deleteAllSelect);
                if(deleteAllSelect == 0) {
                    deleteAllSelect = 1;
                    ProgramManagerList.get(0).DelAllProgram(1);//set delete all program
                }
                else {
                    deleteAllSelect = 0;
                    ProgramManagerList.get(0).DelAllProgram(0);//cancel delete all program
                }

                for(int i = 1; i < ProgramManagerList.size(); i++) { // connie 20180802 fix fav channel not delete
                    for(int j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++)
                            ProgramManagerList.get(i).ProgramManagerInfoList.get(j).setDelete(deleteAllSelect);
                }


                for(int i = 0 ; i < ProgramManagerList.get(0).ProgramManagerInfoList.size(); i++)
                    tvAdapter.notifyItemChanged(i);//use notifyDataSetChanged, focus will disappear
            }
            else if(hide_flag == true)//Scoty 20181205 fixed after delete channel and then add favorite result in crash//Scoty 20181113 add Hide function UI
            {
                int hide = 0;
                if(ProgramManagerList.get(0).ProgramManagerInfoList.get(position).getChannelSkip() == 0)
                    hide = 1;
                ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setChannelSkip(hide);
                setFavoriteHideOnOff(position,hide);
                tvAdapter.notifyItemChanged(position);
                favAdapter.notifyDataSetChanged();
            }
            else {//add favorite channel
                Log.d(TAG, "onClick: add fav channel " + position);
                ProgramManagerList.get(0).AddProgramToFav(ProgramManagerList,curFavGroup,position);
                ProgramManagerList.get(0).ProgramManagerInfoList.get(position).setFavIcon(1);
                tvAdapter.notifyItemChanged(position);
                favAdapter.notifyDataSetChanged();
                if (favAdapter.getItemCount() != 0)
                    favlistview.scrollToPosition(favAdapter.getItemCount()-1);
            }

        }
    }

    //favorite list onclick
    class favlistOnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            final int position = favlistview.getChildLayoutPosition(v);
            Log.d(TAG,"onClick: favlistOnClick Delete Fav " + position);

            if(move_flag == 1)//move fav mode
            {
                if(fav_move_channel == -1) {
                    Log.d(TAG, "onClick: fav_move_channel == -1");
                    fav_move_channel = position;
                    ProgramManagerList.get(curFavGroup).ProgramManagerInfoList.get(position).setMoveIcon(1);//set move symbol
                }
                else {
                    Log.d(TAG, "onClick: fav_move_channel != -1");
                    ProgramManagerList.get(curFavGroup).ProgramManagerInfoList.get(fav_move_channel).setMoveIcon(0);//clean move symbol // connie 20180524 modify for fav group channel number show index
                    favAdapter.notifyItemMoved(fav_move_channel,position);
                    ProgramManagerList.get(curFavGroup).MoveProgram(ProgramManagerList,fav_move_channel,position);//move channel
                    int startPos = fav_move_channel, endPos = position;// connie 20180524 modify for fav group channel number show index-s
                    if(fav_move_channel > position) {
                        startPos = position;
                        endPos = fav_move_channel;
                    }
                    ProgramManagerList.get(curFavGroup).ResetFavChannelNum(ProgramManagerList,curFavGroup,startPos, endPos);
                    Log.d(TAG, "onClick:  startPos ="+ startPos + "     endPos="+endPos);
                    for( int i = startPos; i <= endPos; i++)
                        favAdapter.notifyItemChanged(i);// connie 20180524 modify for fav group channel number show index-e

                    //set first channel focus
                    /*Log.d(TAG, "onClick:" +
                            " favChildPos = "+ favChildPos+
                            " position = "+ position+
                            " fav_move_channel = "+ fav_move_channel
                    );*/

                    View firstView = favLayoutManager.findViewByPosition(0);
                    View lastView = favLayoutManager.findViewByPosition(favAdapter.getItemCount()-1);
                    boolean goUp = position < fav_move_channel;
                    boolean goDown = position > fav_move_channel;
                    int itemHeight = (int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);

                    if (goUp && (lastView == null)) {
                    /*Log.d(TAG, "onClick: go up");*/
                        favlistview.smoothScrollBy(0, -itemHeight);
                    }
                    if (goDown && (firstView == null)) {
                    /*Log.d(TAG, "onClick: go down");*/
                        favlistview.smoothScrollBy(0, itemHeight);
                    }

                    favlistview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View view = favlistview.getLayoutManager().findViewByPosition(position);
                            if (view != null) {
                                view.requestFocus();
                            }
                        }
                    }, 0);
                    fav_move_channel = -1;
                }
                favAdapter.notifyItemChanged(position);//update Move symbol
            }
            else {//delete favorite channel
                Log.d(TAG, "onClick: delete fav channel " + position);
                ProgramManagerList.get(curFavGroup).DelProgram(ProgramManagerList,position,1);
                tvAdapter.notifyDataSetChanged();

                if (favAdapter.getItemCount() == 0) {
                    Log.d(TAG, "onClick: fav no more item, focus tv item");
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onKeyDown(KEYCODE_DPAD_LEFT, new KeyEvent(ACTION_DOWN, KEYCODE_DPAD_LEFT));
                        }
                    }, 0);
                }
                else if (position == (favAdapter.getItemCount())) {
                    Log.d(TAG, "onClick: fav last item");
                    --favChildPos;
                }
                favAdapter.notifyItemRemoved(position);

                int size = favAdapter.getItemCount(); // connie 20180524 modify for fav group channel number show index-s
                if(size > 0) {
                    ProgramManagerList.get(curFavGroup).ResetFavChannelNum(ProgramManagerList,curFavGroup, position, size-1);
                    for(int i = position; i< size ; i++)
                        favAdapter.notifyItemChanged(i);
                }// connie 20180524 modify for fav group channel number show index-e
            }
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
        if(serviceType == 1) {
            tvtitle.setText(R.string.STR_ALL_TV_CHANNELS);
            favtitle.setText(GetAllProgramGroup().get(curFavGroup).getGroupName());
        }
        else {
            tvtitle.setText(R.string.STR_ALL_RADIO_CHANNELS);
            favtitle.setText(GetAllProgramGroup().get(ProgramInfo.ALL_RADIO_TYPE + curFavGroup).getGroupName());
        }
//        if(GetAllProgramGroup().get(curFavGroup).getGroupName() != null)
//        favtitle.setText(GetAllProgramGroup().get(curFavGroup).getGroupName());
    }

    public void changeFAVGroupTitle()//change favorite group title
    {
        TextView favtitle = (TextView) findViewById(R.id.favtitleTXV);
        curFavGroup++;
        if(curFavGroup > groupNum)
            curFavGroup = 1;

        if(serviceType == 1)//Tv Group Name
            favtitle.setText(GetAllProgramGroup().get(curFavGroup).getGroupName());
        else//Radio Group Name
            favtitle.setText(GetAllProgramGroup().get(ProgramInfo.ALL_RADIO_TYPE + curFavGroup).getGroupName());
    }

    public void changeFAVGroup()//change favorite group
    {
        Handler handler = new Handler();
        Runnable focusLeft = new Runnable() {
            @Override
            public void run() {
                onKeyDown(KEYCODE_DPAD_LEFT, new KeyEvent(ACTION_DOWN, KEYCODE_DPAD_LEFT));
            }
        };
        Runnable focusRight = new Runnable() {
            @Override
            public void run() {
                onKeyDown(KEYCODE_DPAD_RIGHT, new KeyEvent(ACTION_DOWN, KEYCODE_DPAD_RIGHT));
            }
        };
        changeFAVGroupTitle();
        //favAdapter = new TvFavAdapter(ProgramManagerList.get(curFavGroup).ProgramManagerInfoList);
        //favlistview.setAdapter(favAdapter);
        favAdapter.listItems = ProgramManagerList.get(curFavGroup).ProgramManagerInfoList;
        favAdapter.notifyDataSetChanged();

        favChildPos = 0;
        tvChildPos = 0;
        if (favAdapter.getItemCount() == 0) { // at right & no more item, then go left
            if (favlistview.hasFocus())
                handler.postDelayed( focusLeft, 0);
        }
        else if (favlistview.hasFocus()) { // at right & has item, then re-focus fav item
            handler.postDelayed( focusRight, 0);
        }
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
                    titleicon.setImageDrawable(getDrawable(R.drawable.tvicon));
                    if(deleteAllSelect == 1) {
                    ProgramManagerList.get(0).DelAllProgram(0);//cancel delete all program
                        deleteAllSelect = 0;
                        for (int i = 0; i < ProgramManagerList.get(0).ProgramManagerInfoList.size(); i++) {
                            tvAdapter.notifyItemChanged(i);
                        }
                    }
                    else
                    {
                        for (int i = 0; i < ProgramManagerList.get(0).ProgramManagerInfoList.size(); i++) {
                            if (ProgramManagerList.get(0).ProgramManagerInfoList.get(i).getDelete() == 1) {
                                ProgramManagerList.get(0).ProgramManagerInfoList.get(i).setDelete(0);
                                tvAdapter.notifyItemChanged(i);
                            }
                        }
                    }
                }
                break;
                case 1: {
                    tvtitle.setText(R.string.STR_DELETE_CHANNEL);
                    titleicon.setImageDrawable(getDrawable(R.drawable.delete));
                }
                break;
                case 2: {
                    tvtitle.setText(R.string.STR_DELETE_ALL_CHANNELS);
                    titleicon.setImageDrawable(getDrawable(R.drawable.delete));
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
        Handler handler = new Handler();
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
                title.setText(R.string.STR_ALL_TV_CHANNELS);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onKeyDown(KEYCODE_DPAD_LEFT, new KeyEvent(ACTION_DOWN, KEYCODE_DPAD_LEFT));
                    }
                }, 0);
            }
            else {
                cancelMoveFAVChannel();
                title.setText(GetAllProgramGroup().get(curFavGroup).getGroupName());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onKeyDown(KEYCODE_DPAD_RIGHT, new KeyEvent(ACTION_DOWN, KEYCODE_DPAD_RIGHT));
                    }
                }, 0);
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
    //Scoty 20181113 add Hide function UI
    private void setHideMode()//set hide mode
    {
        TextView title = (TextView) findViewById(R.id.tvtitleTXV);
        ImageView titleicon = (ImageView) findViewById(R.id.tviconIGV);
        Log.d(TAG," setHideMode " + hide_flag);
        if(tvlistview.hasFocus()) {
            if (hide_flag) {
                title.setText(R.string.STR_HIDE_MODE);
                titleicon.setImageDrawable(getResources().getDrawable(R.drawable.hide,null));
            } else {
                title.setText(R.string.STR_ALL_TV_CHANNELS);
                titleicon.setImageDrawable(getResources().getDrawable(R.drawable.tvicon,null));
            }
        }
    }

    public class TvFavAdapter extends RecyclerView.Adapter<TvFavAdapter.ViewHolder> {
        private List<ProgramManagerImpl.ProgramManagerInfo> listItems;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView ch_num;
            TextView ch_name;
            ImageView scramble;
            ImageView fav;
            ImageView lock;
            ImageView hide;//Scoty 20181113 add Hide function UI

            ViewHolder(View itemView) {
                super(itemView);
                ch_num = (TextView) itemView.findViewById(R.id.chnumTXV);//LCN
                ch_name = (TextView) itemView.findViewById(R.id.chnameTXV);//Channel name
                scramble = (ImageView) itemView.findViewById(R.id.scrambleIGV);//Scramble icon
                fav = (ImageView) itemView.findViewById(R.id.favIGV);//group icon
                lock = (ImageView) itemView.findViewById(R.id.lockIGV);//Lock icon
                hide = (ImageView) itemView.findViewById(R.id.hideIGV);//Hide icon//Scoty 20181113 add Hide function UI
            }
        }

        TvFavAdapter(List<ProgramManagerImpl.ProgramManagerInfo> list) {
            listItems = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from( parent.getContext() )
                    .inflate(R.layout.tvmanager_list_item, parent, false);

            if(parent.getId() == tvlistview.getId())
                convertView.setOnClickListener(new tvlistOnClick());
            else if (parent.getId() == favlistview.getId())
                convertView.setOnClickListener(new favlistOnClick());

            convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        //Log.d(TAG, "onFocusChange:
                        // channel = "+ ((TextView) v.findViewById(R.id.chnameTXV)).getText());
                        v.setBackgroundResource(R.drawable.focus_list);
                    }
                }
            });

            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (listItems.get(position).getMoveIcon() == 1) //set move symbol
                holder.ch_num.setText("↕");
            else if (listItems.get(position).getDelete() == 1) //set delete symbol
                holder.ch_num.setText("X");
            else {
                holder.ch_num.setText(
                        String.valueOf(listItems
                                .get(position)
                                .getChannelNum())
                );
            }

            holder.ch_name.setText(listItems.get(position).getChannelName());

            Log.d(TAG, "ch_num" + holder.ch_num + "ch_name" + holder.ch_name);

            if (listItems.get(position).getCA() == 1)//scramble icon
                holder.scramble.setBackgroundResource(R.drawable.scramble);
            else
                holder.scramble.setBackgroundResource(android.R.color.transparent);//clean icon


            if (listItems.get(position).getFavIcon() == 1)//fav icon
                holder.fav.setBackgroundResource(R.drawable.fav);
            else
                holder.fav.setBackgroundResource(android.R.color.transparent);//clean icon

            if (listItems.get(position).getUserLock() == 1)//lock icon
                holder.lock.setBackgroundResource(R.drawable.lock);
            else
                holder.lock.setBackgroundResource(android.R.color.transparent);//clean icon

            if(listItems.get(position).getChannelSkip() == 1)//Scoty 20181113 add Hide function UI
                holder.hide.setBackgroundResource(R.drawable.hide);
            else
                holder.hide.setBackgroundResource(android.R.color.transparent);

            holder.itemView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);

            if (tvCurPos == position) { // when exit delete mode, focus original position
                tvCurPos = -1;
                holder.itemView.requestFocus();
            }

            //for Marquee,--start
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    if (hasFocus) {
                        holder.ch_num.setSelected(true);
                        holder.ch_name.setSelected(true);
                    }
                    else {
                        holder.ch_num.setSelected(false);
                        holder.ch_name.setSelected(false);
                    }
                }
            });
            //for Marquee,--end

            holder.itemView.setFocusableInTouchMode(true);  // Johnny 20181219 for mouse control
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

    public void GetProgramData()
    {
        Log.d(TAG," GetProgramData   serviceType = " + serviceType);
        int i, j;
        if(serviceType == 1) {
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
            //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        }
        else {
            ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
            //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        }

        chechChannelExistdialog();

        for(i = 1 ; i < ProgramManagerList.size(); i++)
        {
            Log.d(TAG, "GetProgramData:  =====  Goup " + i + "=========  "+ ProgramManagerList.get(i).ProgramManagerInfoList.size() );
            for(j = 0 ; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++)//set fav icon
            {
                ProgramManagerList.get(i).ProgramManagerInfoList.get(j).setFavIcon(1);
                Log.d(TAG, "GetProgramData:  "+ j + " === > " + ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelName());
            }
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

            new MessageDialogView(this, str, 3000) {
                public void dialogEnd() {
                    finish();
                }
            }.show();
        }
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

    // Johnny 20181228 for mouse control -s
    private void onProgRedClicked() {
        //delete mode can not use move, lock mode
        if(move_flag != 0 || lock_flag != 0) {
            Log.d(TAG, "onKeyDown: cannot use other mode in DELETE mode");
            return;
        }
        setDeleteMode();
    }

    private void onProgGreenClicked() {
        // move,delete,lock mode can not change FAV group
        if(move_flag != 0 || delete_flag != 0 || lock_flag != 0) {
            Log.d(TAG, "onKeyDown: cannot change FAV group");
            return;
        }
        changeFAVGroup();
    }

    private void onProgYellowClicked() {
        // move mode can not use delete, lock mode
        if(delete_flag != 0 || lock_flag != 0) {
            Log.d(TAG, "onKeyDown: cannot use other mode in MOVE mode");
            return;
        }
        setMoveMode();
    }

    private void onProgBlueClicked() {
        if(isCloseFun)
            return;
        //move,delete,lock mode can not use left/right key
        if(move_flag != 0 || delete_flag != 0) {
            Log.d(TAG, "onKeyDown: cannot use other mode in LOCK mode");
            return;
        }
        if((tvlistview.hasFocus()))
        {
            if(lock_flag != 1) {
                new PasswordDialogView(TVManagerActivity.this, GposInfoGet().getPasswordValue(), PasswordDialogView.TYPE_PINCODE,0) {
                    public void onCheckPasswordIsRight() {
                        Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                        setLockMode();
                    }

                    public void onCheckPasswordIsFalse() {
                        Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
                        new MessageDialogView(TVManagerActivity.this,
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
    // Johnny 20181228 for mouse control -e
}
