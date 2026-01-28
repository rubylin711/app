package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Activity.ViewActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

/*
  Created by scoty on 2017/12/11.
 */

public abstract class OkListDialogView extends Dialog{
    private static final String TAG="OkListDialogView";
    public static final int MODE_NORMAL         = 0;
    public static final int MODE_DIGITAL        = 1;
    public static final int MODE_CHANNEL_NAME   = 2;
    public static final int MODE_CHANNEL_NUM    = 3;
    private final int PAGE_UP   = 0;
    private final int PAGE_DOWN = 1;
    private Context mContext;
    private RecyclerView rvOkList;
    private RecyclerAdapter okAdapter=null;
    private LinearLayoutManager layoutManager;
    private TextView txvNoFavChannel;
    private EditText okTitle;
    private CountDownTimer mTimer = null;
    private CountDownTimer mAutoChangeCH;

    private int curFavGroup;
    private int curFilterGroup;
    private static int mChannelPos[]
            = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private List<DTVActivity.OkListManagerImpl> mOkListManager;
    private static int listCount = 10;
    private DTVActivity mDtv;
    private List<FavGroupName> mAllProgramGroup;
    private boolean changeGroup = false;
    private static long downKeyTime = 0;//Scoty 20180613 scroll too fast will result in index wrong
    private static long upKeyTime = 0;//Scoty 20180613 scroll too fast will result in index wrong
    private List<PvrInfo> pvrList;//Scoty 20180801 modify when recording/timeshifting can not change group
    private boolean isRecord;
    private int dialogMode;
    private int firstFocusListFromTitle;
    private List<String> mMatchList;
    private int mCurPos = 0;
    private int mServiceType = 0;
    private boolean mAtBottom = false;
    private boolean mAtTop = false;

    //private ConstraintLayout layout = null;
//    protected OkListDialogView(Context context
//            , final List<DTVActivity.OkListManagerImpl> mOkListManagerList
//            , final List<FavGroupName> AllProgramGroup
//            , final int curServiceType
//            , int curListPosition
//            , DTVActivity mDTV
//            , int keycode)
//    {
//        super(context);
//        layout = new ConstraintLayout(context);
//
//        mContext = context;
//        curFavGroup = curServiceType;//cur service type
//        if (keycode == KEYCODE_DPAD_CENTER)
//            mChannelPos[curFavGroup] = curListPosition;
//        else
//            PressDigitalKey(keycode);
//        mOkListManager = mOkListManagerList;
//        mDtv = mDTV;
//        mAllProgramGroup = AllProgramGroup;
//
//        setCancelable(false);// disable click back button
//        setCanceledOnTouchOutside(false);// disable click home button and other area
//
//        //for largest display size,--start
//        int tmpWidth = mContext.getResources().getDisplayMetrics().widthPixels;
//        int tmpHeight = mContext.getResources().getDisplayMetrics().heightPixels;
//        int dialogWidth = (tmpWidth*2)/5;//dialog width
//        int dialogHeight = tmpHeight;//dialog height
//        int itemHeight =(int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);
//        //Log.d(TAG, "TTT onCreate: tmpWidth = " + tmpWidth + ", tmpHeight="+tmpHeight+ ", dialogWidth="+dialogWidth+ ", dialogHeight="+dialogHeight+", itemHeight="+itemHeight);
//
//        int displayedCount = (int)(dialogHeight*0.7)/itemHeight; //0.8-0.1=0.7
//        //Log.d(TAG, "TTT onCreate: displayedCount = " + displayedCount);
//        listCount = displayedCount;
//        //for largest display size,--end
//
//        show();//show dialog
//        setContentView(R.layout.oklist_dialog);
//        Window window = getWindow();//get dialog widow size
//        if (window == null) {
//            Log.d(TAG, "OkListDialogView: window = null");
//            return;
//        }
//
//        //for largest display size,--start
//        ConstraintLayout view = (ConstraintLayout) window.findViewById(R.id.oklistLayout);
//        if(view != null) {
//            view.setMinWidth(dialogWidth);
//            view.setMinHeight(dialogHeight);
//        }
//        else
//            Log.d(TAG, "TTT view =null");
//        //for largest display size,--end
//
//        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp
//        lp.dimAmount=0.0f;
//        window.setAttributes(lp);//set dialog parameter
//        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        window.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//
//        ChangeGroupTitle(mAllProgramGroup,curFavGroup,MODE_NORMAL);
//        oklist_dialog_init(keycode,dialogWidth);//for largest display size
//
//        curPvrMode = mDtv.getCurrentPvrMode();
//        isRecord = mDtv.isRecord();
//        dialogMode = MODE_NORMAL;
//        TitleTextInit();
//    }

    protected OkListDialogView(Context context
            , final List<DTVActivity.OkListManagerImpl> mOkListManagerList
            , final List<FavGroupName> AllProgramGroup
            , final int curServiceType
            , int curListPosition
            , DTVActivity dtv
            , int keycode
            , int mode
            , List<String> matchList)
    {
        super(context);
        dialogMode = mode;
        //layout = new ConstraintLayout(context);

        mContext = context;
        curFavGroup = curServiceType;//cur service type
        mServiceType = curServiceType;
        curFilterGroup = 0;
        if (keycode == KEYCODE_DPAD_CENTER || mode == MODE_NORMAL)
            mChannelPos[GetGroupIndex(dialogMode)] = curListPosition;
        mOkListManager = mOkListManagerList;
        mDtv = dtv;
        mAllProgramGroup = AllProgramGroup;
        mMatchList = matchList;
//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        //for largest display size,--start
        int tmpWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        int dialogWidth = ( tmpWidth * 2 ) / 5; //dialog width
        //Log.d(TAG, "TTT onCreate: tmpWidth = " + tmpWidth + ", tmpHeight="+tmpHeight+ ", dialogWidth="+dialogWidth+ ", dialogHeight="+dialogHeight+", itemHeight="+itemHeight);
        //Log.d(TAG, "TTT onCreate: displayedCount = " + displayedCount);

        int dialogHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        int itemHeight =(int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);
        listCount = (int) ( dialogHeight * 0.7 ) / itemHeight;
        //for largest display size,--end

        //show();//show dialog // Edwin 20190508 fix dialog not focus
        setContentView(R.layout.oklist_dialog);

        Window window = getWindow();//get dialog widow size
        if (window == null) {
            Log.d(TAG, "OkListDialogView: window = null");
            return;
        }
        txvNoFavChannel =  (TextView) window.findViewById(R.id.oklistnofavchannelTXV);

        //for largest display size,--start
        ConstraintLayout view = (ConstraintLayout) window.findViewById(R.id.oklistLayout);
        if(view != null) {
            view.setMinWidth(dialogWidth);
            view.setMinHeight(dialogHeight);
        }
        else
            Log.d(TAG, "TTT view =null");
        //for largest display size,--end

        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp
        lp.dimAmount=0.0f;
        window.setAttributes(lp);//set dialog parameter
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        window.setBackgroundDrawableResource(android.R.color.transparent);  // Johnny 20180801 add for layout alpha
        InitTitleText();
        InitOkListView( keycode, dialogWidth );//for largest display size

        if(mode == MODE_DIGITAL)
            PressDigitalKey(keycode);
        else if(mode == MODE_CHANNEL_NAME)
            FilterByName(matchList, curServiceType);
        else if(mode == MODE_CHANNEL_NUM)
            FilterByNumber(matchList, curServiceType);
        else
            ChangeGroupTitle(mAllProgramGroup.get(curFavGroup).getGroupName(),mode);

        pvrList = mDtv.PvrRecordGetAllInfo();//Scoty 20180801 modify when recording/timeshifting can not change group
        isRecord = mDtv.isRecord(pvrList);
    }

    private void InitTitleText() {
        Window window = getWindow();
        if (window == null)
            return;
        okTitle = (EditText) window.findViewById(R.id.oklisttitleTXV);

        // Edwin 20190509 disable keyboard(soft input) -s
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(okTitle, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Edwin 20190509 disable keyboard(soft input) -e

        okTitle.setShowSoftInputOnFocus(false);//Scoty 20180802 close digit num filter keyboard
        okTitle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTimer != null) {   // Johnny 20181219 for mouse control, fix crash when click oklist title
                    mTimer.cancel();    // Johnny 20180802 add to stop timer after click ok
                }
                SetDigitChannel();//Scoty 20180802 modify digit filter set channel to function
            }
        });
        okTitle.setOnKeyListener(new KeyController());
        okTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    Log.d(TAG,"onFocusChange");
                    v.setFocusable(false);
                    firstFocusListFromTitle = 1;
                }
            }
        });
        okTitle.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(dialogMode == MODE_DIGITAL) {
                    if(s.length() > 4) {
                        ResetCurChannelIndex();
                        //okTitle.setText(s.charAt(4));
                    }
                    else {
                        ResetChannelList(MiscDefine.OKListFilter.TAG_CHANNEL_NUM,curFavGroup,s.toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(dialogMode == MODE_DIGITAL) {
                    if (s.length() > 4) {
                        String str = ""+s.charAt(4);
                        s.clear();
                        s.append(str);
                    }
                }
            }
        });
    }

    private void InitOkListView(int keycode, int width)
    {
        Window window = getWindow();
        if (window == null) {
            Log.d(TAG, "InitOkListView: window = null");
            return;
        }

        TextView text = (TextView) window.findViewById(R.id.oklisttpinfoTXV);
        TextView textNoFavChannel = (TextView) window.findViewById(R.id.oklistnofavchannelTXV);

        rvOkList = (RecyclerView) window.findViewById(R.id.oklistLIV);
        okAdapter = new RecyclerAdapter(mContext, mOkListManager.get(GetGroupIndex(dialogMode)).ProgramInfoList);

        int itemHeight = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                width, itemHeight * listCount);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvOkList.addOnChildAttachStateChangeListener(new OnChildAttach());
        rvOkList.setLayoutParams(layoutParams);
        rvOkList.setLayoutManager(layoutManager);
        rvOkList.setAdapter(okAdapter);
        rvOkList.setItemAnimator(null);
        //topPos = mChannelPos[curFavGroup] - listCount + 1;//first open oklist dialog focus worng channel

        if (keycode == KEYCODE_DPAD_CENTER)//ok key
        {
            Log.d(TAG, "InitOkListView: mChannelPos == " + mChannelPos[GetGroupIndex(dialogMode)]);
            setOklistFocus(rvOkList,mChannelPos[GetGroupIndex(dialogMode)]);
            okAdapter.notifyDataSetChanged();
        }
        else if(keycode == ViewActivity.KEY_FAV)//fav key
        {
            if(okAdapter.getItemCount() <= 0) {
                textNoFavChannel.setVisibility(View.VISIBLE);
                text.setVisibility(View.INVISIBLE);
            }
            else {
                textNoFavChannel.setVisibility(View.INVISIBLE);
                text.setVisibility(View.VISIBLE);
                setOklistFocus(rvOkList,mChannelPos[GetGroupIndex(dialogMode)]);
            }
            okAdapter.notifyDataSetChanged();
        }

        //okAdapter.notifyDataSetChanged();

        TextView texthelp = (TextView) getWindow().findViewById(R.id.oklisthelpTXV);
        String string = mContext.getString(R.string.STR_OKLIST_HELP_OK_SELECT);
        SpannableStringBuilder builder = mDtv.SetSplitText(string);
        texthelp.setText(builder, TextView.BufferType.SPANNABLE);
    }

    private void InitTimer()
    {
        Log.d(TAG, "InitTimer: ");

        mTimer = new CountDownTimer(3000, 1000)
        {
            @Override
            public void onTick (long millisUntilFinished)
            {

            }

            @Override
            public void onFinish ()
            {
                SetDigitChannel();//Scoty 20180802 modify digit filter set channel to function
            }
        };
        mTimer.start();
    }

    private void AutoChangeChannel( List<SimpleChannel> channelList, final String chNum)
    {
        Log.d( TAG, "AutoChangeChannel: " );

        for ( int i = 0; i < channelList.size(); i++ )
        {
            if ( channelList.get(i).getChannelNum() == Integer.valueOf( chNum ) )
            {
                final int pos = i;
                mAutoChangeCH = new CountDownTimer(3000, 1000)
                {
                    @Override
                    public void onTick ( long millisUntilFinished ) {}

                    @Override
                    public void onFinish ()
                    {
                        dismiss();
                        onSetPositiveButton( mOkListManager, GetGroupIndex( dialogMode ), pos );
                    }
                }.start();
            }
        }
    }

    public void UpdateFilter ( List<String> matchList, int serviceType, int mode )
    {
        dialogMode = mode;
        Log.d( TAG, "UpdateFilter: mServiceType = "+mServiceType );

        if ( MODE_CHANNEL_NAME == mode )
        {
            FilterByName( matchList, serviceType );
        }
        else if ( MODE_CHANNEL_NUM == mode )
        {
            FilterByNumber( matchList, serviceType );
        }
    }

    private void FilterByNumber ( List<String> matchList, int serviceType ) //edwin 20180904 add
    {
        List<SimpleChannel> chList;
        String chNum;
        boolean channelMatch = false;
        SetGroupSize( matchList.size(), serviceType ); // by Num
        SetGroupIndex( dialogMode, 0 );
        mCurPos = 0;
        mAtTop = true;
        mAtBottom = false;
        mMatchList = matchList;
        mServiceType = serviceType;

        for ( int i = 0; i < matchList.size(); i++ )
        {
            chNum = GetNumber( matchList.get(i) );
            chList = mDtv
                    .ProgramInfoGetListByFilter(
                            MiscDefine.OKListFilter.TAG_CHANNEL_NUM,
                            serviceType,
                            chNum ,0,1);//Scoty 20181109 modify for skip channel
            if ( chList.size() != 0 )
            {
                Log.d( TAG, "FilterByNumber: Match" );
                txvNoFavChannel.setVisibility( View.INVISIBLE );
                channelMatch = true;
                ChangeGroupTitle( matchList.get(i), MODE_CHANNEL_NUM );
                mOkListManager.get(0).ProgramInfoList = chList;
                AutoChangeChannel(chList, chNum);
                break;
            }
        }

        if ( ! channelMatch ) // no match channel
        {
            Log.d( TAG, "FilterByNumber: not Match" );
            txvNoFavChannel.setVisibility( View.VISIBLE );
            ChangeGroupTitle( matchList.get(0), MODE_CHANNEL_NUM );
            mOkListManager.get(0).ProgramInfoList = mDtv
                    .ProgramInfoGetListByFilter(
                            MiscDefine.OKListFilter.TAG_CHANNEL_NUM,
                            serviceType,
                            matchList.get(0),0,1 );//Scoty 20181109 modify for skip channel
        }
        okAdapter.listItems = mOkListManager.get(GetGroupIndex(dialogMode)).ProgramInfoList;
        okAdapter.notifyDataSetChanged();
        rvOkList.scrollToPosition( 0 );
        ResetCurChannelIndex();
    }

    private void FilterByName ( List<String> matchList, int serviceType ) //edwin 20180827 find a list, at least the list's size is not 0
    {
        List<SimpleChannel> chList;
        String keyword;
        boolean channelMatch = false;
        SetGroupSize( matchList.size(), serviceType ); // by Name
        SetGroupIndex( dialogMode, 0 );
        mCurPos = 0;
        mAtTop = true;
        mAtBottom = false;
        mMatchList = matchList;
        mServiceType = serviceType;

        for ( int i = 0; i < matchList.size(); i++ )
        {
            keyword = matchList.get(i);
            chList = mDtv
                    .ProgramInfoGetListByFilter(
                            MiscDefine.OKListFilter.TAG_CHANNEL_NAME,
                            serviceType,
                            keyword,0,1);//Scoty 20181109 modify for skip channel
            if ( chList.size() != 0 )
            {
                Log.d( TAG, "FilterByName: Match" );
                txvNoFavChannel.setVisibility( View.INVISIBLE );
                channelMatch = true;
                ChangeGroupTitle( keyword, MODE_CHANNEL_NAME );
                chList.sort(new Comparator<SimpleChannel>()
                {
                    @Override
                    public int compare (SimpleChannel o1, SimpleChannel o2)
                    {
                        return Integer.compare(o1.getChannelNum(), o2.getChannelNum());
                    }
                });
                mOkListManager.get(0).ProgramInfoList = chList;
                break;
            }
        }

        if ( ! channelMatch ) // no match channel
        {
            Log.d( TAG, "FilterByName: not Match" );
            txvNoFavChannel.setVisibility( View.VISIBLE );
            keyword = matchList.get(0);
            ChangeGroupTitle( keyword, MODE_CHANNEL_NAME );
            mOkListManager.get(0).ProgramInfoList = mDtv
                    .ProgramInfoGetListByFilter(
                            MiscDefine.OKListFilter.TAG_CHANNEL_NAME,
                            serviceType,
                            keyword,0,1);//Scoty 20181109 modify for skip channel
        }
        okAdapter.listItems = mOkListManager.get(GetGroupIndex(dialogMode)).ProgramInfoList;
        okAdapter.notifyDataSetChanged();
        rvOkList.scrollToPosition( 0 );
        ResetCurChannelIndex();
    }

    private void ResetCurChannelIndex ()
    {
        for ( int i = 0; i < mChannelPos.length; i++ )
        {
            mChannelPos[i] = 0;
        }
    }

    private void MoveDown ( boolean titleHasFocus ) // edwin 20180709 add
    {
        if ( titleHasFocus )
        {
            rvOkList.getChildAt( 0 ).requestFocus();
            return;
        }

        int itemHeight = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        mAtTop = false;
        mCurPos = (mCurPos == ( okAdapter.getItemCount() - 1 ) ) ? 0 : mCurPos + 1;

        if ( mCurPos == 0 )
        {
            rvOkList.scrollBy( 0, -( okAdapter.getItemCount() * itemHeight ) );
            rvOkList.getChildAt( 0 ).requestFocus();
            mAtTop = true;
            mAtBottom = false;
        }
        else if ( mAtBottom )
        {
            rvOkList.scrollBy( 0, itemHeight );
            rvOkList.getChildAt( rvOkList.getChildCount() - 1 ).requestFocus();
        }
        else // down
        {
            int focusPos = mCurPos - rvOkList.getChildAdapterPosition( rvOkList.getChildAt( 0 ) );
            if ( focusPos == (rvOkList.getChildCount()-1) )
                mAtBottom = true;
            rvOkList.getChildAt( focusPos ).requestFocus();
        }
    }

    private void MoveUp() // edwin 20180709 add
    {
        int itemHeight = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        mAtBottom = false;
        mCurPos = ( mCurPos == 0 ) ? okAdapter.getItemCount() - 1 : mCurPos - 1;

        if ( mCurPos == ( okAdapter.getItemCount() - 1 ) )
        {
            rvOkList.scrollBy( 0, ( okAdapter.getItemCount() * itemHeight ) );
            rvOkList.getChildAt( rvOkList.getChildCount() - 1 ).requestFocus();
            mAtTop = false;
            mAtBottom = true;
        }
        else if ( mAtTop )
        {
            rvOkList.scrollBy( 0, -itemHeight );
            rvOkList.getChildAt( 0 ).requestFocus();
        }
        else // up
        {
            int focusPos = mCurPos - rvOkList.getChildAdapterPosition( rvOkList.getChildAt( 0 ) );
            if ( focusPos == 0 )
                mAtTop = true;
            rvOkList.getLayoutManager().findViewByPosition( mCurPos ).requestFocus();
        }
    }

    private String GetNumber( String input)
    {
        String keyword = input
                .replace(mContext.getString(R.string.STR_NUMBER_), "")
                .replace(mContext.getString(R.string.STR_NUMBER), "");

        List<String> numList = Arrays.asList(mContext.getResources().getStringArray(R.array.STR_ARRAY_NUM));
        String[] splitResult = keyword.split(mContext.getString(R.string.STR_HELP_FILTER_SPACE));
        String chNum = "";

        if ( splitResult.length < 1 )
        {
            Log.d(TAG, "GetNumber: output = has no number");
            return String.valueOf( 0 );
        }

        for ( String num : splitResult )
        {
            if ( num.matches( mContext.getString( R.string.STR_REGEX_DIGITAL ) ) )
            {
                chNum = chNum.concat( num );
                Log.d( TAG, "GetNumber: digital" );
            }
            else if ( numList.contains( num ) )
            {
                String index = String.valueOf( numList.indexOf( num ) );
                Log.d( TAG, "GetNumber: index = " + index );
                chNum = chNum.concat( index );
            }
            else
            {
                Log.d( TAG, "GetNumber: output = word" );
                return String.valueOf( 0 );
            }
        }
        Log.d(TAG, "GetNumber: output = "+chNum);

        return chNum;
    }

    private String GetTitleName ( int mode )
    {
        String name;
        int i = GetGroupIndex( dialogMode );

        if ( mode == MODE_NORMAL )
        {
            Log.d( TAG, "GetTitleName: MODE_NORMAL" );
            name = mAllProgramGroup.get(i).getGroupName();
        }
        else
        {
            Log.d( TAG, "GetTitleName: not MODE_NORMAL" );
            name = mMatchList.get(i);
        }
        return name;
    }

    private int GetGroupIndex ( int mode )
    {
        if ( mode == MODE_NORMAL )
        {
            //Log.d( TAG, "GetGroupIndex(" + mode + ") curFavGroup = " + curFilterGroup );
            return curFavGroup;
        }
        else
        {
            //Log.d( TAG, "GetGroupIndex(" + mode + ") curFilterGroup = " + curFilterGroup );
            return curFilterGroup;
        }
    }

    private void SetGroupIndex ( int mode, int groupIndex )
    {
        if(mode == MODE_NORMAL)
            curFavGroup = groupIndex;
        else
            curFilterGroup = groupIndex;
    }

    private void SetGroupSize( int size, int serviceType )
    {
        mOkListManager.clear();
        for ( int i = 0; i < size; i++ )
        {
            mOkListManager.add( mDtv
                    .newOkListManagerImpl( serviceType, null ) );
        }
    }

    //Scoty Add Youtube/Vod Stream -s
    private List<SimpleChannel> GetDigitalFilterChannelList(int filterTag, String KeyWords)
    {
        List<SimpleChannel> channelList = mDtv.ProgramInfoGetPlaySimpleChannelList(curFavGroup,1);
        List<SimpleChannel> filterChannelList = new ArrayList<>();

        Log.d(TAG, "exce GetDigitalFilterChannelList: size = ["+ channelList.size() +"] curFavGroup = [" +curFavGroup + "]"
                + " digitKeyWords = [" + KeyWords + "]");
        for(int i = 0 ; i < channelList.size() ; i++)
        {
            boolean isChannelExist = false;
            if((filterTag == MiscDefine.OKListFilter.TAG_CHANNEL_NAME && channelList.get(i).getChannelName().contains(KeyWords))
                    || (filterTag == MiscDefine.OKListFilter.TAG_CHANNEL_NUM && String.valueOf(channelList.get(i).getChannelNum()).contains(KeyWords))) {
                isChannelExist = true;
            }

            if(isChannelExist)
            {
                SimpleChannel simpleChannel = new SimpleChannel();
                simpleChannel.setChannelId(channelList.get(i).getChannelId());
                simpleChannel.setChannelNum(channelList.get(i).getChannelNum());
                simpleChannel.setChannelName(channelList.get(i).getChannelName());
                simpleChannel.setPlayStreamType(channelList.get(i).getPlayStreamType());
                if(channelList.get(i).getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE
                        || channelList.get(i).getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                {
                    simpleChannel.setUrl(channelList.get(i).getUrl());
                    simpleChannel.setPresentepgEvent(channelList.get(i).getPresentepgEvent());
                    simpleChannel.setFollowepgEvent(channelList.get(i).getFollowepgEvent());
                    simpleChannel.setShortEvent(channelList.get(i).getShortEvent());
                    simpleChannel.setDetailInfo(channelList.get(i).getDetailInfo());
                }
                filterChannelList.add(simpleChannel);
            }
        }
        Log.d(TAG, "exce GetDigitalFilterChannelList: size = ["+ filterChannelList.size() +"] curFavGroup = [" +curFavGroup + "]"
                + " digitKeyWords = [" + KeyWords + "]");

        return filterChannelList;
    }
    //Scoty Add Youtube/Vod Stream -e

    private void ResetChannelList(int filterTag, int serviceType, String keyWord) {
        Log.d(TAG,"ResetChannellist okTitle.getText() = "+okTitle.getText());
//        //Log.d(TAG,"ResetChannellist okAdapter.listItems.size() = "+okAdapter.listItems.size());
        okAdapter.listItems = mDtv
                .ProgramInfoGetListByFilter(filterTag, serviceType, keyWord,0,1);//Scoty 20181109 modify for skip channel
        if(dialogMode == MODE_DIGITAL) {
            mOkListManager.clear();

            //Scoty Add Youtube/Vod Stream -s
            Log.d(TAG, "ResetChannelList: enter");
            okAdapter.listItems = GetDigitalFilterChannelList(filterTag,keyWord);
            //Scoty Add Youtube/Vod Stream -e

            DTVActivity.OkListManagerImpl tmp = mDtv.newOkListManagerImpl(curFavGroup,okAdapter.listItems);
            mOkListManager.add(tmp);
        }
        okAdapter.notifyDataSetChanged();
    }

    private void PressDigitalKey ( int keyCode )
    {
        if ( ( keyCode >= KEYCODE_0 ) && ( keyCode <= KEYCODE_9 ) )
        {
            Log.d( TAG, "PressDigitalKey: oklist keyCode = " + keyCode );
            dialogMode = MODE_DIGITAL;
            ChangeGroupTitle( String.valueOf( keyCode - KEYCODE_0 ) );
            changeGroup = false;
            firstFocusListFromTitle = 0;
            ResetCurChannelIndex();
            if ( dialogMode == MODE_DIGITAL && mTimer == null )
            {
                InitTimer();
            }
        }
    }

    // Johnny 20181210 for keyboard control
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        super.onBackPressed();
        onSetNegativeButton();
    }


    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        if ( mAutoChangeCH != null )
        {
            mAutoChangeCH.cancel();
        }

        Window window = getWindow();
        TextView text = null;
        TextView textNoFavChannel = null;

        if (window != null) {
            text = (TextView) window.findViewById(R.id.oklisttpinfoTXV);
            textNoFavChannel = (TextView) window.findViewById(R.id.oklistnofavchannelTXV);
        }
        if (text == null || textNoFavChannel == null) {
            Log.d(TAG, "onKeyDown: text or textNoFavChannel = null");
            return true;
        }

        View okView = rvOkList.getFocusedChild();
        int itemHeigh = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        int okCount = okAdapter.getItemCount();

        if ( okTitle.hasFocus() )
            PressDigitalKey(keyCode);

        ResetTimer(mTimer,keyCode);

        switch (keyCode)
        {
            // Johnny 20181210 for keyboard control
//            case KEYCODE_BACK:
//                Log.d(TAG,"oklist KEYCODE_BACK");
//                dismiss();
//                onSetNegativeButton();
//                break;

            case KEYCODE_DPAD_RIGHT:
            {
                Log.d( TAG, "KEYCODE_DPAD_RIGHT" );
                //Scoty 20180613 add PvrSkip rule
                if ( (pvrList != null) && pvrList.size() > 0 && //Scoty 20180801 modify when recording/timeshifting can not change group // connie 20181101 for crash when pvr not support
                        dialogMode != MODE_CHANNEL_NAME &&  // edwin 20180906 NAME mode can change group
                        dialogMode != MODE_CHANNEL_NUM )    // edwin 20180906 NUM mode can change group
                    return true;
                if ( dialogMode == MODE_DIGITAL )
                    return true;

                mCurPos = 0;
                mAtTop = true;
                mAtBottom = false;
                SetGroupIndex(dialogMode,GetGroupIndex(dialogMode)+1);
                //Log.d(TAG,"111 KEYCODE_DPAD_RIGHT GetGroupIndex() = "+GetGroupIndex(dialogMode)
                //        + " mOkListManager.size() = "+mOkListManager.size());
                if(GetGroupIndex(dialogMode) >= mOkListManager.size()) {
                        SetGroupIndex(dialogMode,0);
                }
                //Log.d(TAG,"222 KEYCODE_DPAD_RIGHT GetGroupIndex() = "+GetGroupIndex(dialogMode)
                //        + " mOkListManager.size() = "+mOkListManager.size());
                ChangeGroupTitle( GetTitleName( dialogMode ), dialogMode );

                int i = GetGroupIndex(dialogMode);
                Log.d( TAG, "onKeyDown: mServiceType = "+mServiceType );
                if(dialogMode == MODE_CHANNEL_NAME)
                {
                    Log.d( TAG, "onKeyDown: MODE_CHANNEL_NAME" );
                    List<SimpleChannel> chList = mDtv
                            .ProgramInfoGetListByFilter(
                                    MiscDefine.OKListFilter.TAG_CHANNEL_NAME,
                                    mServiceType,
                                    mMatchList.get(i),0,1);//Scoty 20181109 modify for skip channel
                    chList.sort(new Comparator<SimpleChannel>()
                    {
                        @Override
                        public int compare (SimpleChannel o1, SimpleChannel o2)
                        {
                            return Integer.compare(o1.getChannelNum(), o2.getChannelNum());
                        }
                    });
                    mOkListManager.get(i).ProgramInfoList = chList;
                }
                else if ( dialogMode == MODE_CHANNEL_NUM )
                {
                    Log.d( TAG, "onKeyDown: MODE_CHANNEL_NUM" );
                    mOkListManager.get(i)
                            .ProgramInfoList = mDtv
                            .ProgramInfoGetListByFilter(
                                    MiscDefine.OKListFilter.TAG_CHANNEL_NUM,
                                    mServiceType,
                                    GetNumber( mMatchList.get(i) ),0,1);//Scoty 20181109 modify for skip channel
                }
                okAdapter.listItems = mOkListManager.get(i).ProgramInfoList;

                if(okAdapter.getItemCount() <= 0)
                {
                    textNoFavChannel.setVisibility(View.VISIBLE);
                    text.setVisibility(View.INVISIBLE);
                }
                else
                {
                    textNoFavChannel.setVisibility(View.INVISIBLE);
                    text.setVisibility(View.VISIBLE);
                }

                okAdapter.notifyDataSetChanged();
                setOklistFocus(rvOkList,mChannelPos[i]);
                changeGroup = true;

            }break;

            case KEYCODE_DPAD_LEFT:{

                Log.d(TAG,"oklist KEYCODE_DPAD_LEFT");
                //Scoty 20180613 add PvrSkip rule
                if((pvrList != null) && pvrList.size() > 0 && //Scoty 20180801 modify when recording/timeshifting can not change group   // connie 20181101 for crash when pvr not support
                        dialogMode != MODE_CHANNEL_NAME &&  // edwin 20180906 NAME mode can change group
                        dialogMode != MODE_CHANNEL_NUM)     // edwin 20180906 NUM mode can change group
                    return true;
                if(dialogMode == MODE_DIGITAL)
                    return true;

                mCurPos = 0;
                mAtTop = true;
                mAtBottom = false;
                SetGroupIndex(dialogMode,GetGroupIndex(dialogMode)-1);
                if(GetGroupIndex(dialogMode) < 0) {
                    if(mOkListManager.size() > 0)
                        SetGroupIndex(dialogMode,mOkListManager.size()-1);
                    else
                        SetGroupIndex(dialogMode,0);
                }

                ChangeGroupTitle(GetTitleName(dialogMode),dialogMode);

                int i = GetGroupIndex(dialogMode);
                Log.d( TAG, "onKeyDown: mServiceType = "+mServiceType );
                if(dialogMode == MODE_CHANNEL_NAME)
                {
                    Log.d( TAG, "onKeyDown: MODE_CHANNEL_NAME" );
                    List<SimpleChannel> chList = mDtv
                            .ProgramInfoGetListByFilter(
                                    MiscDefine.OKListFilter.TAG_CHANNEL_NAME,
                                    mServiceType,
                                    mMatchList.get(i),0,1);//Scoty 20181109 modify for skip channel
                    chList.sort(new Comparator<SimpleChannel>()
                    {
                        @Override
                        public int compare (SimpleChannel o1, SimpleChannel o2)
                        {
                            return Integer.compare(o1.getChannelNum(), o2.getChannelNum());
                        }
                    });
                    mOkListManager.get(i).ProgramInfoList = chList;
                }
                else if ( dialogMode == MODE_CHANNEL_NUM )
                {
                    Log.d( TAG, "onKeyDown: MODE_CHANNEL_NUM" );
                    mOkListManager.get(i).ProgramInfoList = mDtv
                            .ProgramInfoGetListByFilter(
                                    MiscDefine.OKListFilter.TAG_CHANNEL_NUM,
                                    mServiceType,
                                    GetNumber( mMatchList.get(i) ),0,1);//Scoty 20181109 modify for skip channel
                }
                okAdapter.listItems = mOkListManager.get(i).ProgramInfoList;

                if(okAdapter.getItemCount() <= 0)
                {
                    textNoFavChannel.setVisibility(View.VISIBLE);
                    text.setVisibility(View.INVISIBLE);
                }
                else
                {
                    textNoFavChannel.setVisibility(View.INVISIBLE);
                    text.setVisibility(View.VISIBLE);
                }

                okAdapter.notifyDataSetChanged();
                setOklistFocus(rvOkList,mChannelPos[i]);
                changeGroup = true;

            }break;

            case KEYCODE_DPAD_DOWN: {
                Log.d(TAG,"KEYCODE_DPAD_DOWN");
                
                if ( dialogMode == MODE_CHANNEL_NUM ||
                        dialogMode == MODE_CHANNEL_NAME ||
                        dialogMode == MODE_DIGITAL )
                {
                    if ( okAdapter.getItemCount() == 0 )
                        return true;
                    MoveDown( okTitle.hasFocus() );
                    return true;
                }

                //Scoty 20180613 add PvrSkip rule -s
                if( dialogMode != MODE_NORMAL )  {
                    if(firstFocusListFromTitle != 1)
                        return false;
                    else {
                        if(okAdapter.listItems == null || okAdapter.listItems.size() == 0)
                            return true;
                    }
                }
                else {
                    if(okAdapter.listItems == null || okAdapter.listItems.size() == 0)
                        return true;
                }
                //Scoty 20180613 scroll too fast will result in index wrong -s
                long time = System.currentTimeMillis();
                if(time - downKeyTime < 30)
                    return true;
                downKeyTime = time;
                //Scoty 20180613 scroll too fast will result in index wrong -e

                final int group = GetGroupIndex(dialogMode);
                boolean atLastItem =
                        (dialogMode == MODE_NORMAL) && (mChannelPos[group] == ( okCount - 1 ));
                boolean atLastChild =
                        okView == rvOkList.getChildAt(rvOkList.getChildCount()-1);
                int count = 0;
                int resetFlag = 0;
                final int preChannelIndex = mChannelPos[group];

                if (mChannelPos[group] == (okCount-1))
                    mChannelPos[group] = 0;
                else {
                    mChannelPos[group]++;
                }
                //Log.d(TAG,"okAdapter.listItems.size()" + okAdapter.listItems.size());
                //Log.d(TAG,"mChannelPos[GetGroupIndex(dialogMode)])" + mChannelPos[GetGroupIndex(dialogMode)]);
                while(okAdapter.listItems.get(mChannelPos[group]).getPVRSkip() == 1) {
                    count++;
                    mChannelPos[group]++;
                    if(mChannelPos[group] > (okCount-1)) {
                        resetFlag = 1;
                        mChannelPos[group] = 0;
                        count = 0;
                        break;
                    }
                }

                if(resetFlag == 1)
                {
                    while(okAdapter.listItems.get(mChannelPos[group]).getPVRSkip() == 1)
                    {
                        count++;
                        mChannelPos[group]++;
                    }
                }

                if(preChannelIndex == mChannelPos[group])
                    return true;
//                Log.d(TAG, "OkListDialogView:"
//                        +"\n curFavGroup = "+curFavGroup
//                        +"\n mChannelPos["+curFavGroup+"] = "+mChannelPos[curFavGroup]
//                );
                if(isRecord)//if(curPvrMode != PvrInfo.EnPVRMode.NO_ACTION)//if(mDtv.PvrGetCurrentRecMode() != EnPVRRecMode.NO_ACTION)
                {
                    //Scoty 20180613 fixed ok list index wrong when recording -s
                    int lastItemPosition = layoutManager.findLastVisibleItemPosition();
                    if(mChannelPos[group] > preChannelIndex) {
                        if(mChannelPos[group] > lastItemPosition )
                        {
                            //at last child line, need scroll one position or more
                            if(count > 0)
                            {
                                layoutManager.scrollToPosition(mChannelPos[group]);
                                rvOkList.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        rvOkList.getChildAt(rvOkList.getChildCount() - 1).requestFocus();
                                    }
                                }, 0);
                                return true;
                            }
                            else
                            {
                                rvOkList.scrollBy(0, itemHeigh);
                            }
                        }
                        else
                        {
                            //Same Page no need scroll
                            rvOkList.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int firstItemPositionAfterScroll = layoutManager.findFirstVisibleItemPosition();//Scoty 20180614 fixed pvrSkip channel index wrong
                                    rvOkList.getChildAt(mChannelPos[group]-firstItemPositionAfterScroll).requestFocus();//Scoty 20180614 fixed pvrSkip channel index wrong
                                }
                            }, 0);
                            return true;
                        }
                    }
                    else
                    {
                        //Reverse to the first page
                        layoutManager.scrollToPosition(mChannelPos[group]);
                        rvOkList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int firstItemPosition = layoutManager.findFirstVisibleItemPosition();//Scoty 20180614 fixed pvrSkip channel index wrong
                                if(preChannelIndex >= listCount)//Scoty 20180614 fixed pvrSkip channel index wrong
                                    rvOkList.getChildAt(mChannelPos[group] - firstItemPosition).requestFocus();//Scoty 20180614 fixed pvrSkip channel index wrong
                                else
                                    rvOkList.getChildAt(mChannelPos[group]).requestFocus();
                            }
                        }, 0);
                        return true;
                    }
                    //Scoty 20180613 fixed ok list index wrong when recording -e
                }
                else {
                    if (atLastItem) {
                        Log.d(TAG, "onKeyDown: scrollToPosition");
                        layoutManager.scrollToPosition(mChannelPos[group]);
                        rvOkList.postDelayed( new Runnable()
                        {
                            @Override
                            public void run ()
                            {
                                rvOkList.getChildAt( 0 ).requestFocus();
                            }
                        }, 0 );
                        break;
                    }

                    if (atLastChild) {
                        Log.d(TAG, "onKeyDown: scrollBy");
                        rvOkList.scrollBy(0, itemHeigh);
                        return true;//eric lin 20180801 fix some case in the first/last item of ok list, up/down key move more one item
                    }
                }
                //Scoty 20180613 add PvrSkip rule -e
            }break;

            case KEYCODE_DPAD_UP: {
                Log.d(TAG, "KEYCODE_DPAD_UP");
                
                if ( dialogMode == MODE_CHANNEL_NUM ||
                        dialogMode == MODE_CHANNEL_NAME ||
                        dialogMode == MODE_DIGITAL )
                {
                    if ( okAdapter.getItemCount() == 0 )
                        return true;
                    MoveUp();
                    return true;
                }
                
                //Scoty 20180613 add PvrSkip rule -s
                if( dialogMode != MODE_NORMAL )
                {
                    if(firstFocusListFromTitle != 1) {
                        return false;
                    }
                    else {
                        if(okAdapter.listItems == null || okAdapter.listItems.size() == 0)
                            return true;
                    }
                }
                else {
                    if(okAdapter.listItems == null || okAdapter.listItems.size() == 0)
                        return true;
                }
                //Scoty 20180613 scroll too fast will result in index wrong -s
                long uptime = System.currentTimeMillis();
                if(uptime - upKeyTime < 30)
                    return true;
                upKeyTime = uptime;
                //Scoty 20180613 scroll too fast will result in index wrong -e

                int resetFlag = 0,count = 0;
                final int group = GetGroupIndex(dialogMode);
                int preChannelIndex = mChannelPos[group];
                boolean atFirstChild = okView == rvOkList.getChildAt(0);
                boolean atFirstItem = mChannelPos[group] == 0;

                if (mChannelPos[group] == 0)
                    mChannelPos[group] = (okCount-1);
                else {
                    mChannelPos[group]--;
                }
                //Log.d(TAG,"okAdapter.listItems.size()" + okAdapter.listItems.size());
                //Log.d(TAG,"mChannelPos[group])" + mChannelPos[group]);
                while(okAdapter.listItems.get(mChannelPos[group]).getPVRSkip() == 1) {
                    count++;
                    mChannelPos[group]--;
                    if(mChannelPos[group] < 0) {//Scoty 20180614 fixed pvrSkip channel index wrong
                        resetFlag = 1;
                        mChannelPos[group] = (okCount-1);
                        count = 0;
                        break;
                    }
                }

                if(resetFlag == 1)
                {
                    while(okAdapter.listItems.get(mChannelPos[group]).getPVRSkip() == 1)
                    {
                        count++;
                    mChannelPos[group]--;
                    }
                }

                if(preChannelIndex == mChannelPos[group])
                    return true;
//                Log.d(TAG, "OkListDialogView:"
//                        +"\n curFavGroup = "+curFavGroup
//                        +"\n mChannelPos["+curFavGroup+"] = "+mChannelPos[curFavGroup]
//                );
                if(isRecord)//if(curPvrMode != PvrInfo.EnPVRMode.NO_ACTION)//if(mDtv.PvrGetCurrentRecMode() != EnPVRRecMode.NO_ACTION)
                {
                    //Scoty 20180613 fixed ok list index wrong when recording -s
                    final int firstItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if(mChannelPos[group] < preChannelIndex) {
                        if(mChannelPos[group]  < firstItemPosition )
                        {
                            //at first child line, need scroll one position or more
                            if(count > 0) {
                                layoutManager.scrollToPosition(mChannelPos[group]);
                                rvOkList.postDelayed(new Runnable() {
                                    @Override
                                        public void run() {
                                        rvOkList.getChildAt(0).requestFocus();
                                    }
                                }, 0);
                                return true;
                            }
                            else
                            {
                                rvOkList.scrollBy(0, (-itemHeigh));
                            }
                        }
                        else
                        {
                            //Same Page no need scroll
                            rvOkList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                    int firstItemPosition = layoutManager.findFirstVisibleItemPosition();//Scoty 20180614 fixed pvrSkip channel index wrong
                                    rvOkList.getChildAt(mChannelPos[group]-firstItemPosition).requestFocus();//Scoty 20180614 fixed pvrSkip channel index wrong
                                }
                            }, 0);
                            return true;
                        }
                    }
                    else
                    {
                        //Reverse to the last page
                        layoutManager.scrollToPosition(mChannelPos[group]);
                        rvOkList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int firstItemPositionAfterScroll = layoutManager.findFirstVisibleItemPosition();//Scoty 20180614 fixed pvrSkip channel index wrong
                                if(mChannelPos[group]>= listCount)//Scoty 20180614 fixed pvrSkip channel index wrong
                                    rvOkList.getChildAt(mChannelPos[group] - firstItemPositionAfterScroll).requestFocus();//Scoty 20180614 fixed pvrSkip channel index wrong
                                else
                                    rvOkList.getChildAt(mChannelPos[group]).requestFocus();
                            }
                        }, 0);
                        return true;
                    }
                    //Scoty 20180613 fixed ok list index wrong when recording -e
                }
                else {
                    if (atFirstItem) {
                        Log.d(TAG, "onKeyDown: scrollToPosition");

                        layoutManager.scrollToPosition(mChannelPos[group]);
                        rvOkList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rvOkList.getChildAt(rvOkList.getChildCount()-1).requestFocus();
                            }
                        }, 100);
                        break;
                    }

                    if (atFirstChild) {
                        Log.d(TAG, "onKeyDown: scrollBy");

                        rvOkList.scrollBy(0, -itemHeigh);
                        return true;//eric lin 20180801 fix some case in the first/last item of ok list, up/down key move more one item
                    }
                }
                //Scoty 20180613 add PvrSkip rule -e
            }break;

            // edwin 20180709 add page up & page down -s

            case KeyEvent.KEYCODE_PAGE_DOWN:
                PagePrev(rvOkList, okAdapter.listItems);
                break;

            case KeyEvent.KEYCODE_PAGE_UP:
                PageNext(rvOkList, okAdapter.listItems);
                break;

            // edwin 20180709 add page up & page down -e
        }
        return super.onKeyDown(keyCode, event);
    }

    //private int GetPassValue()
    //{
    //    //Scoty 20180613 add PvrSkip rule -s
    //    if(dialogMode != MODE_NORMAL)  {
    //        if(firstFocusListFromTitle != 1)
    //            return KEEP_ACTION;
    //        else {
    //            if(okAdapter.listItems == null || okAdapter.listItems.size() == 0)
    //                return STOP_ACTION;
    //        }
    //    }
    //    else {
    //        if(okAdapter.listItems == null || okAdapter.listItems.size() == 0)
    //            return STOP_ACTION;
    //    }
    //    //Scoty 20180613 scroll too fast will result in index wrong -s
    //    long time = System.currentTimeMillis();
    //    if(time - downKeyTime < 30)
    //        return STOP_ACTION;
    //    downKeyTime = time;
    //    //Scoty 20180613 scroll too fast will result in index wrong -e
    //    return 0;
    //}

    private void setOklistFocus(final RecyclerView oklist, final int position)
    {
        Log.d(TAG, "setOklistFocus: ");

        layoutManager.scrollToPositionWithOffset(position, 0);

        oklist.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                View view = oklist.getLayoutManager().findViewByPosition(position);

                if (view != null)
                    view.requestFocus();
            }
        }, 0);
    }

    private void ChangeGroupTitle(String groupName, int mode)
    {
        if (okTitle == null)
            return;

        okTitle.setBackgroundResource(R.drawable.selectbox);
        if((mode == MODE_NORMAL && groupName != null)
                || mode == MODE_CHANNEL_NAME
                || mode == MODE_CHANNEL_NUM)
        {
            okTitle.setText(groupName);
        }

        if ( dialogMode == MODE_NORMAL )
        {
            okTitle.setBackgroundResource( R.drawable.selectbox );
            okTitle.setFocusableInTouchMode( true );
            okTitle.setFocusable( false );
        }
        else if ( dialogMode == MODE_DIGITAL )
        {
            okTitle.setBackgroundResource( 0 );
            okTitle.setFocusableInTouchMode( true );
            okTitle.setFocusable( true );
            okTitle.requestFocus();
        }
        else if ( dialogMode == MODE_CHANNEL_NAME || dialogMode == MODE_CHANNEL_NUM )
        {
            okTitle.setBackgroundResource( R.drawable.selectbox );

            okTitle.setFocusableInTouchMode( true );
            okTitle.setFocusable( false );
        }
        okTitle.requestLayout();
    }

    private void ChangeGroupTitle(String digitalNumStr)
    {
        if (okTitle == null)
            return;
        okTitle.setBackgroundResource(0);
        okTitle.setFocusableInTouchMode(true);
        okTitle.setFocusable(true);
        okTitle.requestFocus();
        okTitle.setText(digitalNumStr);
        okTitle.setSelection(okTitle.getText().length());
    }
    // edwin 20180709 add page up & page down -s

    private void PagePrev(final RecyclerView list, List<SimpleChannel> chList)
    {
        Log.d(TAG, "PagePrev: ");

        if (chList == null || chList.isEmpty()) {   // Johnny 20180926 fix okList page up/down crash if no channel
            return;
        }

        boolean isFirstPage = list.getChildAdapterPosition(list.getChildAt(0)) == 0;
        int topPos  = list.getChildAdapterPosition(list.getChildAt(0));
        int curPos  = list.getChildAdapterPosition(list.getFocusedChild());
        int samePos = curPos - topPos;

        if ( HasPVRSkip(chList) )
        {
            PvrPageUp(list, chList);
        }
        else
        {
            if ( isFirstPage )
            {
                int newPos = list.getAdapter().getItemCount() - list.getChildCount() + samePos;

                Log.d(TAG, "PagePrev: newPos = "+newPos);
                mChannelPos[GetGroupIndex(dialogMode)] = newPos;
                list.scrollToPosition(list.getAdapter().getItemCount()-1);
                View view = list.getLayoutManager().findViewByPosition(newPos);
                if ( view != null )
                    view.requestFocus();
            }
            else
            {
                list.scrollBy(0, -list.getLayoutParams().height);
                list.getChildAt(samePos).requestFocus();
                mChannelPos[GetGroupIndex(dialogMode)] = list.getChildAdapterPosition(list.getChildAt(samePos));
            }
        }
    }

    private void PageNext(final RecyclerView list, List<SimpleChannel> chList)
    {
        Log.d(TAG, "PageNext: ");

        if (chList == null || chList.isEmpty()) {   // Johnny 20180926 fix okList page up/down crash if no channel
            return;
        }

        boolean isPageEnd = list.getChildAdapterPosition(list.getChildAt(list.getChildCount()-1))
                == (list.getAdapter().getItemCount()-1);
        int topPos  = list.getChildAdapterPosition(list.getChildAt(0));
        int curPos  = list.getChildAdapterPosition(list.getFocusedChild());
        int samePos = curPos - topPos;

        if ( HasPVRSkip(chList) )
        {
            PvrPageDown(list, chList);
        }
        else
        {
            if ( isPageEnd )
            {
                mChannelPos[GetGroupIndex(dialogMode)] = samePos;
                list.scrollToPosition(0);
                View view = list.getLayoutManager().findViewByPosition(samePos);
                if ( view != null )
                {
                    view.requestFocus();
                }
            }
            else
            {
                list.scrollBy(0, list.getLayoutParams().height);
                list.getChildAt(samePos).requestFocus();
                mChannelPos[GetGroupIndex(dialogMode)] = list.getChildAdapterPosition(list.getChildAt(samePos));
            }
        }
    }

    private void PvrPageDown(RecyclerView list, List<SimpleChannel> chList)
    {
        Log.d(TAG, "PvrPageDown: ");

        View view;
        LinearLayoutManager lm = (LinearLayoutManager) list.getLayoutManager();
        int startPos = list.getChildAdapterPosition(list.getFocusedChild()) + list.getChildCount();
        int offset = list.getFocusedChild().getTop();

        if ( startPos >= list.getAdapter().getItemCount() )
        {
            startPos = 0;
            offset = 0;
        }
        Log.d(TAG, "PvrPageDown: startPos = "+startPos);
        int newPos = GetPVRSkipPos(chList, startPos, PAGE_DOWN);
        mChannelPos[GetGroupIndex(dialogMode)] = newPos;

        if ( newPos < startPos )
        {
            offset = 0;
        }

        lm.scrollToPositionWithOffset(newPos, offset); // focus item at OnChildAttach
        view = list.getLayoutManager().findViewByPosition(newPos);
        if ( view != null )
        {
            view.requestFocus();
        }
    }

    private void PvrPageUp(RecyclerView list, List<SimpleChannel> chList)
    {
        Log.d(TAG, "PvrPageUp: ");

        View view;
        LinearLayoutManager lm = (LinearLayoutManager) list.getLayoutManager();
        int startPos = list.getChildAdapterPosition(list.getFocusedChild()) - list.getChildCount();
        int offset = list.getFocusedChild().getTop();

        if ( startPos < 0 )
        {
            startPos = list.getAdapter().getItemCount() - 1;
            offset = list.getChildAt(list.getChildCount()-1).getTop();
        }

        int newPos = GetPVRSkipPos(chList, startPos, PAGE_UP);
        mChannelPos[GetGroupIndex(dialogMode)] = newPos;

        if ( newPos > startPos )
        {
            offset = list.getChildAt(list.getChildCount()-1).getTop();
        }

        lm.scrollToPositionWithOffset(newPos, offset); // focus item at OnChildAttach
        view = list.getLayoutManager().findViewByPosition(newPos);
        if ( view != null )
        {
            view.requestFocus();
        }
    }

    private boolean HasPVRSkip(List<SimpleChannel> chList)
    {
        for ( int i = 0; i < chList.size(); i++ )
        {
            if ( chList.get(i).getPVRSkip() == 1 )
            {
                Log.d(TAG, "HasPVRSkip: false");
                return true;
            }
        }
        Log.d(TAG, "HasPVRSkip: true");
        return false;
    }

    private int GetPVRSkipPos(List<SimpleChannel> chList, int startPos, int direction)
    {
        Log.d(TAG, "GetPVRSkipPos: ");

        int pos = startPos;

        while ( chList.get(pos).getPVRSkip() == 1 )
        {
            if ( direction == PAGE_DOWN )
            {
                pos++;
                if ( pos >= chList.size() )
                {
                    pos = 0;
                }
            }
            else if ( direction == PAGE_UP )
            {
                pos--;
                if ( pos < 0 )
                {
                    pos = chList.size() - 1;
                }
            }
        }
        return pos;
    }
    // edwin 20180709 add page up & page down -e

    private void SetDigitChannel()//Scoty 20180802 modify digit filter set channel to function
    {
        int position = -1;
        int digitNum;
        //Log.d(TAG,"mTimer onFinish !!!!!!!!!!!!!!!!!!!!!!!!!!");
        if ( okTitle.hasFocus() )
        {
            //Log.d(TAG,"mTimer onFinish in okTitle !!!!!!!!!!!!!!!!!!!!!!!!!!");
            if ( okTitle.getTextSize() > 0 )
            {
                digitNum = Integer.valueOf( okTitle.getText().toString() );
                for ( int i = 0; i < mOkListManager.get( 0 ).ProgramInfoList.size(); i++ )
                {
                    if ( mOkListManager.get( 0 ).ProgramInfoList.get( i ).getChannelNum() == digitNum )
                    {
                        position = i;
                        mChannelPos[GetGroupIndex( dialogMode )] = position;
                        break;
                    }
                }
            }
        }
        else if ( rvOkList.hasFocus() )
        {
            position = rvOkList.getChildAdapterPosition( rvOkList.getFocusedChild() );
            mChannelPos[GetGroupIndex( dialogMode )] = position;
        }
        //Log.d(TAG,"mTimer counter down position = " + position);
        dismiss();
        onSetPositiveButton( mOkListManager, GetGroupIndex( dialogMode ), position );
    }

    private void ResetTimer(CountDownTimer timer, int keycode)
    {
        Log.d(TAG, "ResetTimer: ");

        if ( timer == null )
        {
            Log.d(TAG, "ResetTimer: timer == null");
            return;
        }

        switch ( keycode )
        {
            case KEYCODE_BACK:  // Johnny 20180802 add to stop timer when press back
                timer.cancel();
                mTimer = null;
                break;
            case KEYCODE_DPAD_UP:
            case KEYCODE_DPAD_DOWN:
            case KEYCODE_0:
            case KEYCODE_1:
            case KEYCODE_2:
            case KEYCODE_3:
            case KEYCODE_4:
            case KEYCODE_5:
            case KEYCODE_6:
            case KEYCODE_7:
            case KEYCODE_8:
            case KEYCODE_9:
                timer.cancel();
                timer.start();
                break;
        }
    }

    private class KeyController implements View.OnKeyListener
    {
        @Override
        public boolean onKey (View v, int keyCode, KeyEvent event)
        {
            if ( event.getAction() != KeyEvent.ACTION_DOWN )
            {
                return false;
            }

            Log.d(TAG, "onKey: KeyController, keyCode = "+keyCode);

            ResetTimer(mTimer, keyCode);
            return false;
        }
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private Context cont;
        private List<SimpleChannel> listItems;

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

        RecyclerAdapter(Context mContext,List<SimpleChannel> list) {
            cont = mContext;
            listItems = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from( parent.getContext() )
                    .inflate(R.layout.oklist_list_item, parent, false);

            convertView.setOnClickListener(new oklistOnClick());
            convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        //                      Log.d(TAG, "onFocusChange: channel = "+ ((TextView) v.findViewById(R.id.chnameTXV)).getText());
                        v.setBackgroundResource(R.drawable.focus_list);
                    }
                }
            });

            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            //Log.d(TAG, "onBindViewHolder: ===>>> IN position == " + position);
            //TextView text = (TextView) mDialog.getWindow().findViewById(R.id.oklisttpinfoTXV);
            //Scoty 20180613 add PvrSkip rule -s
            final int PvrSkipflag = listItems.get(position).getPVRSkip();//Scoty 20180614 fixed pvrSkip channel color wrong
            if(PvrSkipflag == 1) {
                holder.chnum.setTextColor(Color.GRAY);
                holder.chname.setTextColor(Color.GRAY);
            }
            else
            {
                holder.chnum.setTextColor(Color.WHITE);
                holder.chname.setTextColor(Color.WHITE);
            }
            //Scoty 20180613 add PvrSkip rule -e

            holder.chnum.setText(String.valueOf(listItems.get(position).getChannelNum()));
            holder.chname.setText(listItems.get(position).getChannelName());

            if(listItems.get(position).getCA() == 1)
                holder.scramble.setBackgroundResource(R.drawable.scramble);
            else
                holder.scramble.setBackgroundResource(android.R.color.transparent);

            if(listItems.get(position).getUserLock() == 1)
                holder.chlock.setBackgroundResource(R.drawable.lock);
            else
                holder.chlock.setBackgroundResource(android.R.color.transparent);

            if (dialogMode == MODE_NORMAL && (position == mChannelPos[GetGroupIndex(dialogMode)]) && changeGroup) {
                Log.d(TAG, "onBindViewHolder: change group to pos = "+position);
                changeGroup = false;
                holder.itemView.requestFocus();
            }

            holder.itemView.getLayoutParams().height = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        //Log.d(TAG, "onFocusChange: tpinfo update");
                        Window window = getWindow();
                        if (window == null)
                            return;

                        holder.itemView.setBackground(cont.getDrawable(R.drawable.focus));
                        holder.chnum.setTextColor(Color.BLACK);
                        holder.chname.setTextColor(Color.BLACK);
                        holder.chname.setSelected(true);//for Marquee
                    }
                    else
                    {
                        holder.itemView.setBackground(cont.getDrawable(android.R.color.transparent));
                        if(PvrSkipflag == 1) {//Scoty 20180614 fixed pvrSkip channel color wrong
                            holder.chnum.setTextColor(Color.GRAY);
                            holder.chname.setTextColor(Color.GRAY);
                        }
                        else {
                            holder.chnum.setTextColor(Color.WHITE);
                            holder.chname.setTextColor(Color.WHITE);
                        }
                        holder.chname.setSelected(false);//for Marquee
                    }
                }


            });
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

    class oklistOnClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            final int position = rvOkList.getChildLayoutPosition(v);
            Log.d(TAG,"oklistOnClick mChannelPos = " + position);

            mChannelPos[GetGroupIndex(dialogMode)] = position;
            dismiss();
            onSetPositiveButton(mOkListManager,GetGroupIndex(dialogMode), position);
        }
    }

    class OnChildAttach implements RecyclerView.OnChildAttachStateChangeListener
    {

        @Override
        public void onChildViewAttachedToWindow (View view)
        {
            if (mChannelPos[GetGroupIndex(dialogMode)] == rvOkList.getChildAdapterPosition(view) &&
                    dialogMode == MODE_NORMAL)
            {
                view.requestFocus();
            }
        }

        @Override
        public void onChildViewDetachedFromWindow (View view)
        {

        }
    }

    abstract public void onSetPositiveButton(List<DTVActivity.OkListManagerImpl> okList,int curFavGroup, int position);
    abstract public void onSetNegativeButton();
}
