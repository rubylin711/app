package com.prime.dtvplayer.Activity;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.DeleteReserveDialog;
import com.prime.dtvplayer.View.DetailInfoDialog;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.SetReserveInfoDialog;
import com.prime.dtvplayer.View.SummaryInfoDialog;
import com.prime.dtvplayer.View.guide.GuideUtils;
import com.prime.dtvplayer.View.guide.ProgramItemView;
import com.prime.dtvplayer.View.guide.ProgramRow;
import com.prime.dtvplayer.View.guide.ProgramTableAdapter;
import com.prime.dtvplayer.View.guide.TimeListAdapter;
import com.prime.dtvplayer.View.guide.TimelineRow;
import com.prime.dtvplayer.utils.TVMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.view.KeyEvent.KEYCODE_CHANNEL_DOWN;
import static android.view.KeyEvent.KEYCODE_CHANNEL_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_INFO;
import static android.view.KeyEvent.KEYCODE_PROG_BLUE;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static android.view.KeyEvent.KEYCODE_PROG_YELLOW;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS;
import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;

//import android.widget.VideoView;

public class DimensionEPG extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private  DTVActivity mDTVActivity = null;
    private TimeListAdapter mTimeListAdapter;
    private TimelineRow mTimelineRow;
    private ImageView curLine;
    private TextView mshowDate;
    private TextView channelName;
    private TextView eventName;
    private TextView eventInfo;
    private TextView curTimeText;
    public RecyclerView mGrid;
    private TextView endLine;
    private SurfaceView video;

    private int CurType = 0; // Current Group
    private int CurChannelIndex=0; // Focus Channel Index
    private EPGEvent CurFocusEvent ;// Focus Event
    private long curTime = 0 ; // Record Current Move Time

    private int mWidthPerHour = 0;
    private long mStartTime = 0; // Start Time in 7 day
    private long timeRangeStart = 0; // Start Time in Show Range in UI
    private long timeRangeEnd = 0; // End Time in Show Range in UI
    public int scrollOffset = 0 ;

    private int displayedCount = 6; // total line in one page default
    int itemHeight = 0; // item height

    private int mCurrentTimeIndicatorWidth = 0; // current Time line width
    private Handler mHandler = null;// new Handler();
    private Handler EnterEPG = null;;
    private long currentTime =0; //  Record getLocalTime()

    private long oldTime = 0;
    private boolean changeChFlag = false;//Scoty 20181129 modify VMX enter Epg rule

    private ProgramTableAdapter programTableAdapter=null;

    private static final long HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);
    private static final long SEVEN_DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(7);
    private static final long HALF_HOUR_IN_MILLIS = HOUR_IN_MILLIS / 2;
    private static final int TIME_UNIT_PAGE=4; // time range unit

    SimpleDateFormat CurTimeformatter = new SimpleDateFormat("MMM dd  HH : mm", Locale.getDefault());
    private SimpleDateFormat curDateFormatter= new SimpleDateFormat("dd MMM", Locale.getDefault()); // Use for Show Current Date on UI
    String strCurTime="";

    private final int  KEYCODE_GUIDE_PESI = 297 ;
    SetReserveInfoDialog mReserveDialog;
    boolean isShowReserveDialog = false;

    //========   Key test !!!!!!!!  =========
    private static Handler KeyTest_handler;
    private Thread t =null ;
    private boolean TEST_EPG = false;

    private EpgUiDisplay epgUiDisplay = null;
    private BookManager bookManager = null;

    private boolean isCloseFun = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mDTVActivity = this;
        setContentView(R.layout.activity_dimension_epg);

        setTitleHelp();
        Bundle mainmenu =this.getIntent().getExtras();
        CurType = mainmenu != null ? mainmenu.getInt("type") : 0;
        CurChannelIndex = mainmenu != null ? mainmenu.getInt("cur_channel") : 0;
        if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181129 modify VMX enter Epg rule
            changeChFlag = mainmenu.getBoolean("changeCH");


        String str;
        if(ViewHistory.getCurChannel() == null){
            if (CurType <= ProgramInfo.TV_FAV6_TYPE) // TV Group
                str = getString(R.string.STR_NO_TV_CHANNEL);
            else // Radio Gruop
                str = getString(R.string.STR_NO_RADIO_CHANNEL);

            new MessageDialogView(this, str, 3000) {
                public void dialogEnd() {
                    finish();
                }
            }.show();
        }
        else {
            //EpgUiDisplayInit(CurType);
            epgUiDisplay = GetEpgUiDisplay(CurType);
            bookManager = GetBookManager();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            //Log.d(TAG, "onStart:    width = " + width + "    height = " + height );

            int chnumWidth = ((int) getResources().getDimension(R.dimen.DIMENSIONEPG_TABLE_CHANNEL_NUM_WIDTH));
            int chnameWidth =  ((int) getResources().getDimension(R.dimen.DIMENSIONEPG_TABLE_CHANNEL_NAME_WIDTH));
            int itemHeight =  ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
            mWidthPerHour = ((int)(width*0.9)- chnumWidth- chnameWidth)/2 ;
            displayedCount = ((int)(height*0.5) - itemHeight)/itemHeight;
            Log.d(TAG, "onStart: mWidthPerHour = " + mWidthPerHour);
            GuideUtils.setWidthPerHour(mWidthPerHour);
            mHandler = new Handler();
            itemInit();
            //mHandler.post(mUpdateTimeIndicator);

            //======= Key Test =========
            createMessageHandleThread();
        }
    }

    // ======== KEY Test ============
    private void createMessageHandleThread(){
        //need start a thread to raise looper, otherwise it will be blocked
        t = new Thread() {
            public void run() {
                Log.i( TAG,"Creating handler ..." );
                Looper.prepare();
                KeyTest_handler = new Handler(){
                    public void handleMessage(Message msg) {
                        //process incoming messages here
                    }
                };
                Looper.loop();
                Log.i( TAG, "Looper thread ends" );
            }
        };
        t.start();
    }
    private final Runnable Key_Test_Run = new Runnable() {
        @Override
        public void run() {
            Instrumentation inst=new Instrumentation();

            //int num = (int)((Math.random())*100);
            int num = 3;
            Log.d(TAG, "Auto Test: num =: " + num);
            if(num % 10 == 0) {
                Log.d(TAG, "Auto Test:  send UP");
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
            }
            else if(num % 10 == 1 || num % 10 == 6) {
                Log.d(TAG, "Auto Test:  send DOWN");
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            }
            else if(num % 10 == 2) {
                Log.d(TAG, "Auto Test:  send LEFT");
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
            }
            else if(num % 10 == 3 || num % 10 == 7 ) {
                Log.d(TAG, "Auto Test:  send RIGHT");
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
            }
            else if(num % 10 == 4) {
                Log.d(TAG, "Auto Test:  send YELLOW");
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_PROG_YELLOW);
            }
            else if(num % 10 == 5 || num % 10 == 8) {
                Log.d(TAG, "Auto Test:  send BLUE");
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_PROG_BLUE);
            }
            else if(num % 10 == 9) {
                Log.d(TAG, "Auto Test:  send KEYCODE_DPAD_CENTER");
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            }
            KeyTest_handler.postDelayed(Key_Test_Run, 1000);
        }
    };

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if(ViewHistory.getCurChannel() != null){
            updateStartTime();
            mHandler.post(mUpdateTimeIndicator);

            //======= Key Test  =============
            if(TEST_EPG)
            KeyTest_handler.post(Key_Test_Run);
            //========================
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if(mHandler != null)
            mHandler.removeCallbacks(mUpdateTimeIndicator);

        if(EnterEPG != null)
            EnterEPG.removeCallbacks(EnterEPGRunnable);

        if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)
        {
            Rect rect = new Rect();
            Display parent = getWindowManager().getDefaultDisplay();
            rect.left = 0;
            rect.top = 0;
            rect.right = parent.getWidth();
            rect.bottom = parent.getHeight();
            AvControlSetWindowSize(0, new Rect(rect));
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop: ");
        if(mHandler != null)
        mHandler.removeCallbacks(mUpdateTimeIndicator);
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_EPG_UPDATE: {
                //int chnum = tvMessage.getServiceCHNum();
                //int type = tvMessage.getserviceType();
                long channelID = tvMessage.getChannelId();
                Log.d(TAG, "onMessage: channelID = " + channelID);
                int updateIndex = -1;
                if (programTableAdapter == null)
                    break;

                ProgramRow focusRow;
                int findLine=-1;
                int channelIndex = -1;
                for (int i = 0; i < mGrid.getChildCount(); i++) {
                    focusRow = (ProgramRow) mGrid.getChildAt(i).findViewById(R.id.row);
                    channelIndex = focusRow.getChannelIndex();
                    if (epgUiDisplay.programInfoList.get(channelIndex).getChannelId() == channelID) {
                        findLine = i;
                        updateIndex = channelIndex;
                        break;
                    }
                }
                if (updateIndex != -1) {
                    int focusIndex = ChannelListGetFocusedChildIndex();
                    if(focusIndex == findLine && findLine != -1)
                        mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                    Log.d(TAG, "onMessage: updateCHEvent !");
                    programTableAdapter.updateCHEvent(updateIndex);
                }

            }break;

            case TVMessage.TYPE_VMX_SET_PIN: // for VMX need open/close -s
            {
                int vmxPinOnOff = tvMessage.GetVMXPinOnOff();
                long vmxPinChannelID = tvMessage.GetVMXPinChannelID();
                int vmxPinIndex = tvMessage.GetVMXPinIndex();
                int vmxTextSelector = tvMessage.GetVMXPinTextSelector();

                Log.d(TAG, "onMessage: vmxPinOnOff = " + vmxPinOnOff + "     vmxPinChannelID = " + vmxPinChannelID);
                VMXCheckPin(this, 1, vmxPinOnOff, vmxPinChannelID, "", vmxPinIndex, vmxTextSelector);

            }break;

            case TVMessage.TYPE_VMX_IPPV:
            {
                int vmxIPPVOnOff = tvMessage.GetIPPV_OnOff();
                long vmxIPPVChannelID = tvMessage.GetIPPV_ChannelID();
                String vmxIPPVcurToken = tvMessage.GetIPPV_CurToken();
                String vmxIPPVCost = tvMessage.GetIPPV_cost();
                int vmxPinIndex = tvMessage.GetIPPV_PinIndex();

                Log.d(TAG, "onMessage:  vmxIPPVOnOff =  " + vmxIPPVOnOff + "  vmxIPPVChannelID ="+ vmxIPPVChannelID+ "   vmxIPPVcurToken = " + vmxIPPVcurToken + "    vmxIPPVCost = "+vmxIPPVCost);
                String ippvMsg = getString(R.string.STR_AVAILABLE_CREDIT) + (vmxIPPVcurToken) + "\n"
                        +  getString(R.string.STR_COST_OF_EVENT) + (vmxIPPVCost);
                VMXCheckPin(this, 2, vmxIPPVOnOff, vmxIPPVChannelID, ippvMsg, vmxPinIndex, 0);
            }break;
            case TVMessage.TYPE_VMX_BCIO_NOTIFY:// connie 20180925 add for ippv/pin bcio notify
            {
                int vmxIPPVOnOff = tvMessage.GetPCIONotify_type();
                VMXBcioNotify(vmxIPPVOnOff);
            }break;// for VMX need open/close -e
        }
    }

    @Override
    public void onConnected()
    {
        super.onConnected();
    }

    @Override
    public void onDisconnected()
    {
        super.onDisconnected();
    }

    private void setUILayoutParam()
    {
        int displayedHeight;// total line in UI
        // ===== set mGrid Layout Params =====
        itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        ViewGroup.LayoutParams layoutParams;
        if(epgUiDisplay.programInfoList.size()< displayedCount)
            displayedHeight = epgUiDisplay.programInfoList.size() * itemHeight;
        else
            displayedHeight = displayedCount * itemHeight;
        layoutParams = mGrid.getLayoutParams();
        int chnumWidth = ((int) getResources().getDimension(R.dimen.DIMENSIONEPG_TABLE_CHANNEL_NUM_WIDTH));
        int chnameWidth =  ((int) getResources().getDimension(R.dimen.DIMENSIONEPG_TABLE_CHANNEL_NAME_WIDTH));
        layoutParams.width = mWidthPerHour*2 + chnumWidth+chnameWidth ;
        layoutParams.height = displayedHeight;
        mGrid.setLayoutParams(layoutParams);
        mGrid.setBackgroundColor(Color.WHITE);

        // ======Set Time Row Layout Params ======
        layoutParams = mTimelineRow.getLayoutParams() ;
        layoutParams.width = mWidthPerHour*2;
        mTimelineRow.setLayoutParams(layoutParams);

        // ======Set cur Line Layout Params ======
        layoutParams = curLine.getLayoutParams() ;
        layoutParams.height = displayedHeight+itemHeight;
        curLine.setLayoutParams(layoutParams);

        endLine.setVisibility(VISIBLE);
    }

    public void setTitleHelp()
    {
        ActivityTitleView titleView;
        ActivityHelpView helpView;
        // ======Title  &  Help ======
        titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        helpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);

        titleView.setTitleView(getString(R.string.STR_CH_GUIDE_TITLE));
        helpView.setHelpInfoTextBySplit(null);

        helpView.resetHelp(1, R.drawable.help_red, null);
        helpView.resetHelp(2,  R.drawable.help_green, null);
//        helpView.resetHelp(1, R.drawable.help_yellow, "- 6:00");
//        helpView.resetHelp(2, R.drawable.help_blue, "+ 6:00");
        helpView.resetHelp(3, R.drawable.help_yellow, null);
        helpView.resetHelp(4, R.drawable.help_blue, null);
//        helpView.resetHelp(1, R.drawable.help_red, getString(R.string.STR_TIMER));
//        helpView.resetHelp(2,  R.drawable.help_green, getString(R.string.STR_SUMMARY));
//        helpView.resetHelp(3, R.drawable.help_yellow, null);
//        helpView.resetHelp(4, R.drawable.help_blue, null);

        // Johnny 20181228 for mouse control -s
        helpView.setHelpIconClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgRedClicked();
            }
        });
        helpView.setHelpIconClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgGreenClicked();
            }
        });
        helpView.setHelpIconClickListener(3, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgYellowClicked();
            }
        });
        helpView.setHelpIconClickListener(4, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProgBlueClicked();
            }
        });
        // Johnny 20181228 for mouse control -e

        // ===  Set  Info & Summary  Help ===
        //String help = getString(R.string.STR_EPG_INFO_HELP);
//        SpannableStringBuilder style = new SpannableStringBuilder(help);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //helpView.setHelpInfoTextBySplit(help);
        //========================
    }

    public void itemInit()
    {
        Date showDate ;
        int focusPage = 0;
        channelName = (TextView)findViewById(R.id.channelNameTXV);
        eventName = (TextView)findViewById(R.id.eventNameTXV);
        eventInfo = (TextView)findViewById(R.id.eventInfoTXV);
        mshowDate = (TextView) findViewById(R.id.clock);
        mGrid = (RecyclerView) findViewById(R.id.grid);
        mTimelineRow = (TimelineRow) findViewById(R.id.time_row);
        curLine = (ImageView) findViewById(R.id.current_time_indicator);
        curTimeText = (TextView)findViewById(R.id.curTimeTXV);
        video = (SurfaceView)findViewById(R.id.videoView2);
        endLine =(TextView)findViewById(R.id.endLineTXV);

        currentTime = GetLocalTime().getTime();
        mStartTime = floorTime(GetLocalTime().getTime() ,HALF_HOUR_IN_MILLIS);
        timeRangeStart = mStartTime;
        timeRangeEnd = timeRangeStart + HALF_HOUR_IN_MILLIS * TIME_UNIT_PAGE;

        // ===== Show Current Date ======
        showDate = new Date(timeRangeStart);
        mshowDate.setText(curDateFormatter.format(showDate));
        mshowDate.setBackground(getResources().getDrawable(R.drawable.epgtimedate));

        setUILayoutParam();
        // =====Set Time List Adapter ======
        mTimeListAdapter = new TimeListAdapter(mStartTime);
        mTimelineRow.getRecycledViewPool().setMaxRecycledViews(R.layout.program_guide_table_header_row_item,10);
        mTimelineRow.setAdapter(mTimeListAdapter);

        // ======Set Channel List Adapter ======
        programTableAdapter = new ProgramTableAdapter(DimensionEPG.this ,epgUiDisplay);
        mGrid.getRecycledViewPool().setMaxRecycledViews(R.layout.program_guide_table_row,30);
        mGrid.setItemViewCacheSize(0);
        mGrid.setAdapter(programTableAdapter);
        mGrid.setItemAnimator(null);
        if(displayedCount < (CurChannelIndex+1)) // focus on cur channel
            focusPage = CurChannelIndex / displayedCount;
        mGrid.scrollToPosition(focusPage*displayedCount);
        curTime = timeRangeStart;
        mTimelineRow.addOnScrollListener(onScrollListener);

        if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) {
            video.setVisibility(VISIBLE);//Scoty 20181127 modify 3798 model need show small screen
            EnterEPG = new Handler();
            EnterEPG.postDelayed(EnterEPGRunnable, 1000);
        }
    }

    final Runnable EnterEPGRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            Log.d(TAG, "EnterEPGRunnable");
            Rect rect = new Rect();
            int location[] = new int[2];
            video.getHolder().setFormat(PixelFormat.TRANSPARENT);
            video.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + video.getWidth();
            rect.bottom = rect.top + video.getHeight();
            AvControlSetWindowSize(0, new Rect(rect));
        }
    };

    public static long floorTime(long timeMs, long timeUnit) {
        return timeMs - (timeMs % timeUnit);
    }
    public long getStartTime()
    {
        return mStartTime;
    }
    public long getEndTime()
    {
        return mStartTime+ SEVEN_DAY_IN_MILLIS;
    }
    public long getTimeRangeStart()
    {
        return timeRangeStart;
    }
    public long getTimeRangeEnd()
    {
        return timeRangeEnd;
    }

    public void shiftTime(long timeMillisToScroll) {
        long newStart = timeRangeStart + timeMillisToScroll;
        long newEnd = timeRangeEnd + timeMillisToScroll;
        if (newStart < getStartTime() || newEnd > getEndTime()) {
            newStart = mStartTime;
            newEnd = mStartTime + HALF_HOUR_IN_MILLIS * TIME_UNIT_PAGE;
        }
        //Log.d(TAG, "shiftTime:  timeMillisToScroll = " + timeMillisToScroll);
        //Log.d(TAG, "shiftTime:  newStart = " + newStart + "    newEnd=" + newEnd);

        setTimeRange(newStart, newEnd);
        scrollOffset = (int) (mWidthPerHour * getShiftedTime()/ HOUR_IN_MILLIS);
        mTimelineRow.scrollTo(scrollOffset, false);
    }

    private void setTimeRange(long newStartTime, long newEndTime) {
        //Log.d(TAG, "setTimeRange: newStartTime = " + newStartTime + "   newEndTime = " + newEndTime);
        //Log.d(TAG, "setTimeRange:  timeRangeStart = " +timeRangeStart + "    timeRangeEnd = " + timeRangeEnd);
        if (newStartTime != timeRangeStart || newEndTime != timeRangeEnd) {
            timeRangeStart = newStartTime;
            timeRangeEnd = newEndTime;
            Date showDate = new Date(timeRangeStart);
            if(mshowDate != null)
                mshowDate.setText(curDateFormatter.format(showDate));
        }
    }

    public void updateDetail( EPGEvent event)
    {
        if( event == null)
            return;
        String str;
        CurFocusEvent = event ;
        String shortEvent;
        int ChannelIndex = getChannelIndex();
        Log.d(TAG, "updateDetail: Focus channel ===> ChannelIndex = " + ChannelIndex);

        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date EventTime = new Date(CurFocusEvent.getStartTime());
        String strStartTime = formatter.format(EventTime);
        EventTime = new Date(CurFocusEvent.getEndTime());
        String strEndTime = formatter.format(EventTime);

        // ======  Update  Detail   Info =======
        str = strStartTime + " ~ " + strEndTime;
        channelName.setText(epgUiDisplay.programInfoList.get(ChannelIndex).getChannelName());
        if(CurFocusEvent.getEventId()!=0)
            str = str + "   " +CurFocusEvent.getEventName();
        else
            str = str + "   " + getString(R.string.STR_NO_INFO_AVAILABLE);
        eventName.setText( str );
        //Log.d(TAG, "updateDetail:  event Name = "+ CurFocusEvent.getEventName());
        shortEvent = epgUiDisplay.GetDetailInfo(epgUiDisplay.programInfoList.get(ChannelIndex).getChannelId(), event.getEventId());
        if(shortEvent == null || shortEvent.equals("")) // connie 20180806 fix detail show not complete
        {
            shortEvent = epgUiDisplay.GetShortEvent(epgUiDisplay.programInfoList.get(ChannelIndex).getChannelId(), event.getEventId());
            if(shortEvent == null)
                shortEvent = "";
        }
        str = shortEvent;
        eventInfo.setText(str);
    }


    public void setCurTime( long value)
    {
        if(value < getStartTime())
            value = getStartTime();
        curTime = value ;

        SimpleDateFormat formatter= new SimpleDateFormat("yyyy  MMM dd  ... HH : mm", Locale.getDefault());
        Date EventTime = new Date(curTime);
        String strStartTime = formatter.format(EventTime);
        Log.d(TAG, "setCurTime:  current Time = " + strStartTime);
    }
    public long getCurTime()
    {
        return curTime;
    }

    public void setChannelIndex(int channelIndex)
    {
        Log.d(TAG, "setChannelIndex: channelIndex =" + channelIndex);
        CurChannelIndex = channelIndex;
    }

    public int getChannelIndex()
    {
        return CurChannelIndex ;
    }


    long getShiftedTime() {
        //Log.d(TAG, "getShiftedTime: timeRangeStart =  " + timeRangeStart);
        //Log.d(TAG, "getShiftedTime: mStartTime =  " + mStartTime);
        return timeRangeStart - mStartTime;
    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            positionCurrentTimeIndicator(currentTime); // connie  20180604 modify for get LocalTime Too often 
            onHorizontalScrolled(dx);
        }
    };

    private void onHorizontalScrolled(int dx) {
        for (int i = 0; i < mGrid.getChildCount(); i++) {
            mGrid.getChildAt(i).findViewById(R.id.row).scrollBy(dx, 0);
        }
    }

    public int getTimelineRowScrollOffset() {
        return mTimelineRow.getScrollOffset();
    }

    private final Runnable mUpdateTimeIndicator = new Runnable() {
        @Override
        public void run() {
            long curTime = GetLocalTime().getTime(); // connie  20180604 modify for get LocalTime Too often 
            if((mStartTime + HOUR_IN_MILLIS*2) <= curTime) { // connie  20180604 modify for get LocalTime Too often 
                //Log.d(TAG, "run:  UPDATE!!!!!!!!!!!!!!!!");
                updateStartTime();
                mHandler.postDelayed(mUpdateTimeIndicator, 1000);
            }
            else {
                //Log.d(TAG, "run:  refresh time Line !!!!!!!");
                String strStartTime = CurTimeformatter.format(curTime); // connie  20180604 modify for get LocalTime Too often 
                if (!strCurTime.equals(strStartTime)) {
                    curTimeText.setText(strStartTime);
                    strCurTime = strStartTime;
                }

                positionCurrentTimeIndicator(curTime); // connie  20180604 modify for get LocalTime Too often 
                mHandler.postDelayed(mUpdateTimeIndicator, 1000);
            }
        }
    };

    private void updateStartTime()
    {
        long newRangeStart = timeRangeStart;
        long newRangeEnd;
        ProgramRow focusRow;
        CurFocusEvent = null;
        mStartTime = floorTime(GetLocalTime().getTime() ,HALF_HOUR_IN_MILLIS);
        if(curTime < mStartTime)
            curTime = mStartTime;
        if(timeRangeStart < mStartTime)
            newRangeStart = mStartTime;

        newRangeEnd = newRangeStart + HOUR_IN_MILLIS*2; // End Time in Show Range in UI
        scrollOffset = 0 ;

        mTimeListAdapter = null ;
        mTimeListAdapter = new TimeListAdapter(mStartTime);
        mTimelineRow.setAdapter(mTimeListAdapter);

        setTimeRange(newRangeStart, newRangeEnd);
        scrollOffset = (int) (mWidthPerHour * getShiftedTime()/ HOUR_IN_MILLIS);
        programTableAdapter.updateStartTime();

        mTimelineRow.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTimelineRow.scrollTo(scrollOffset, false);
            }
        }, 0);

        mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        if(mGrid.getChildAt(0)!=null)
        {
            focusRow = (ProgramRow) mGrid.getChildAt(0).findViewById(R.id.row);
            int firstCh = focusRow.getChannelIndex();
            for( int i = 0; i <  mGrid.getChildCount(); i++) {
                programTableAdapter.notifyItemChanged(firstCh + i);
            }
        }
    }

    private void positionCurrentTimeIndicator(long curTime) { // connie  20180604 modify for get LocalTime Too often 
        //testTime+=5000;
        currentTime = curTime ;//GetLocalTime().getTime(); // connie  20180604 modify for get LocalTime Too often 
        int offset = GuideUtils.convertMillisToPixel(mStartTime, currentTime)
                - mTimelineRow.getScrollOffset();
        Log.d(TAG, "positionCurrentTimeIndicator: offset =" + offset);

        if (offset < 0) {
            curLine.setVisibility(View.GONE);
        }
        else {
            if (mCurrentTimeIndicatorWidth == 0) {
                curLine.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                mCurrentTimeIndicatorWidth = curLine.getMeasuredWidth();
                Log.d(TAG, "positionCurrentTimeIndicator:  mCurrentTimeIndicatorWidth = "+mCurrentTimeIndicatorWidth);
            }
            curLine.setPaddingRelative(
                    offset - mCurrentTimeIndicatorWidth / 2, 0, 0, 0);
            curLine.bringToFront();
            curLine.setVisibility(VISIBLE);
        }
    }

    public void ResetTimeRange() // At 7Day end ---> right Key ----> move to head
    {
        final int focusIndex = ChannelListGetFocusedChildIndex();
        mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        shiftTime(-(getTimeRangeStart()-getStartTime()));
        setCurTime(getStartTime());
        mGrid.postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgramRow focusRow;
                focusRow = (ProgramRow) mGrid.getChildAt(focusIndex).findViewById(R.id.row);
                setRowFocus(focusRow);
            }
        }, 0);
    }

    private void setRowFocus( ProgramRow FocusRow)
    {
        // Find Focus Event on FocusRow
        ProgramItemView eventView;
        EPGEvent findEvent;
        //Log.d(TAG, "onKeyDown:  focusRow.getChildCount() = " + FocusRow.getChildCount());

        for(int i = 0 ; i < FocusRow.getChildCount(); i++) {
            eventView = (ProgramItemView) FocusRow.getChildAt(i);
            findEvent = eventView.getItemEvent();
            Log.d(TAG, "onKeyDown : setRowFocus: channel " + eventView.getItemChannel());

            if (findEvent.getStartTime() <= curTime && findEvent.getEndTime() > curTime) {
                Log.d(TAG, "onKeyDown:  FIND !!!!!!!!!    i = " + i);
                mGrid.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                FocusRow.getChildAt(i).requestFocus();
                break;
            }
        }
    }

    private int ChannelListGetFocusedChildIndex()
    {
        for (int i = 0; i < mGrid.getChildCount(); ++i) {
            if (mGrid.getChildAt(i).hasFocus()) {
                return i;
            }
        }
        return -1;
    }

    // Johnny 20181219 for mouse control
    public void changeChannel() {
        Log.d(TAG, "onKeyDown:   Change Channel ===>" + epgUiDisplay.programInfoList.get(getChannelIndex()).getChannelName());

//Scoty Add Youtube/Vod Stream -s
        SimpleExoPlayer mExoPlayer = GetExoPlayer();
        SurfaceView mSurfaceViewExoplayer = GetExoplayerSurfaceView();
        WebView mWebview = GetYoutubeWebview();
        SimpleChannel toChannel = epgUiDisplay.programInfoList.get(getChannelIndex());
        Log.d(TAG, "changeChannel: Stream Type = [" + toChannel.getPlayStreamType() +"] channelId = [" + toChannel.getChannelId() +"]");
        Log.d(TAG, "changeChannel: Cur Watch Stream Type = [" + ViewHistory.getCurChannel().getPlayStreamType() +"] channelId = [" + ViewHistory.getCurChannel().getChannelId() +"]");
        if(toChannel.getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {//Play Webview Youtube
            //AvControlPlayStop(0);
            if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                mExoPlayer.stop(true);
                mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                mSurfaceViewExoplayer.setVisibility(INVISIBLE);
            }else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                StopPlayYoutubeVideo(mWebview);
            }
            else{
                AvControlPrePlayStop();
                AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
            }
            //PlayExoPlayerVideo();
            mSurfaceViewExoplayer.setVisibility(INVISIBLE);
            StartPlayYoutubeVideo(mWebview,toChannel);
            ViewHistory.SetCurChannel(toChannel,epgUiDisplay.programInfoList,CurType);

        }else if(toChannel.getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) //Play HLS
        {
            if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                StopPlayYoutubeVideo(mWebview);
            }else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                mExoPlayer.stop(true);
                mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                mSurfaceViewExoplayer.setVisibility(INVISIBLE);
            }
            else{
                AvControlPrePlayStop();
                AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
            }
            PlayExoPlayerVideo(mExoPlayer,mSurfaceViewExoplayer,toChannel);
            ViewHistory.SetCurChannel(toChannel,epgUiDisplay.programInfoList,CurType);

        }
        else
        {
            if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                mExoPlayer.stop(true);
                mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                mSurfaceViewExoplayer.setVisibility(INVISIBLE);
            }else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                StopPlayYoutubeVideo(mWebview);
            }
        }
        //Scoty Add Youtube/Vod Stream -e

        if(Pvcfg.getUIModel() != Pvcfg.UIMODEL_3798) {//Scoty 20181127 modify 3798 change Epg Channel rule
            epgUiDisplay.ChangeProgram(getChannelIndex());
        }

        if( Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1) { // for VMX need open/close
            AvControlShowVideo(ViewHistory.getPlayId(), false);
            AvControlSetMute(ViewHistory.getPlayId(), true);
        }
    }

    private void StartPlayYoutubeVideo(WebView mWebView, SimpleChannel channel)
    {
        String mimeType = "text/html";
        String encoding = "UTF-8";//"base64";

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

//        String frameVideo = "<iframe width=\"" + width +"\""
//               +"height=\"" + height +"\" " +
//                "src=\"https://www.youtube.com/embed/lu_BJKxqGnk?&autoplay=1\" frameborder=\"0\"" + "border-style =\"none\""+
//                " allow=\"autoplay;\" allowfullscreen></iframe>";

        String frameVideo = channel.getUrl();
        Log.d(TAG, "exce webview StartPlayYoutubeVideo: Width = [" + width + "] height = [" + height +"]"
                + "x = [" + displaymetrics.xdpi +"] y = [" + displaymetrics.ydpi +"]");
        if(mWebView.getVisibility() == INVISIBLE) {
            Log.d(TAG, "exce webview Start StartPlayYoutubeVideo: ");
            mWebView.setVisibility(VISIBLE);

            mWebView.loadUrl(frameVideo);
            mWebView.loadDataWithBaseURL("", frameVideo, mimeType, encoding, "");
            mWebView.onResume();
        }
        setDefaultOpenChannel(channel.getChannelId(),CurType);
    }

    private void StopPlayYoutubeVideo(WebView mWebView)
    {
        Log.d(TAG, "exce webview StopPlayYoutubeVideo: ");
        mWebView.setVisibility(INVISIBLE);
        mWebView.stopLoading();
        mWebView.onPause();


    }

    private void PlayExoPlayerVideo(SimpleExoPlayer mExoPlayer, SurfaceView mSurfaceViewExoplayer, SimpleChannel channel)
    {
        Log.d(TAG, "exce PlayExoPlayerVideo: ");
        //String VideoPath = ViewHistory.getCurChannel().getUrl();//VOD_VIDEO_URL;
        String VideoPath = channel.getUrl();
        Log.d(TAG, "exce11 PlayExoPlayerVideo: VideoPath = [" + VideoPath + "]");
        Uri videoUri = Uri.parse(VideoPath);
        Log.d(TAG, "exce PlayExoPlayerVideo: VideoPath = [" + VideoPath + "]");

        mSurfaceViewExoplayer.setVisibility(VISIBLE);
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(getApplicationContext(), "ViewActivity"));
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(videoUri);
        mExoPlayer.setRepeatMode(REPEAT_MODE_ONE);
        mExoPlayer.setVideoSurfaceView(mSurfaceViewExoplayer);
        mExoPlayer.prepare(mediaSource);

        mExoPlayer.setPlayWhenReady(true);
        setDefaultOpenChannel(channel.getChannelId(),CurType);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: ");
        final int keyDownLatency = 50;
        long newTime;
        newTime = System.currentTimeMillis();

        if (isShowReserveDialog) // Edwin 20190509 fix dialog not focus
        {
            return true;
        }
        if ((newTime - oldTime) < keyDownLatency && keyCode != KeyEvent.KEYCODE_BACK) {   // Johnny 20181210 for keyboard control
            return true;
        }

        oldTime = newTime;

        switch (keyCode) {
            case KEYCODE_INFO:
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
            {
                int ChannelIndex = getChannelIndex();
                long ChannelID = epgUiDisplay.programInfoList.get(ChannelIndex).getChannelId();
                new DetailInfoDialog(this, epgUiDisplay, CurFocusEvent, ChannelID);
            }
            break;

            // Johnny 20181219 for mouse control, is handled in ProgramItemView ON_CLICKED
            case KeyEvent.KEYCODE_DPAD_CENTER:
            {
                changeChannel();
            }break;

            case KeyEvent.KEYCODE_PROG_GREEN: // Summary
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
            {
                onProgGreenClicked();   // Johnny 20181228 for mouse control
            }break;

            case KEYCODE_PROG_RED: // Book Event
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
            {
                isShowReserveDialog = true; // Edwin 20190509 fix dialog not focus
                onProgRedClicked();     // Johnny 20181228 for mouse control
            }break;

            case KEYCODE_GUIDE_PESI:
            case ExtKeyboardDefine.KEYCODE_GUIDE_PESI:  // Johnny 20181210 for keyboard control
            {
                int curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
                Intent it = new Intent();
                Bundle bundle = new Bundle();
                it.setClass(this, EpgActivity.class);
                bundle.putInt("type", ViewHistory.getCurGroupType());
                bundle.putInt("cur_channel", curListPosition );
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181129 modify VMX enter Epg rule
                    bundle.putBoolean("changeCH",changeChFlag);
                it.putExtras(bundle);
                startActivity(it, bundle);
                finish();
            }break;

            case KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                onProgBlueClicked();    // Johnny 20181228 for mouse control
                return true;
            }

            case KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
            {
                onProgYellowClicked();  // Johnny 20181228 for mouse control
                return true;

            }

            case KEYCODE_DPAD_DOWN:
            {
                int focusIndex = ChannelListGetFocusedChildIndex();
                int newFocusIndex;
                Log.d(TAG, "onKeyDown:  focusIndex  ="+focusIndex + "    child count =" +mGrid.getChildCount());

                if(focusIndex == -1)
                    return true;

                if(getChannelIndex() == mGrid.getAdapter().getItemCount()-1) // last channel
                {
                    Log.d(TAG, "onKeyDown: Last Channel !!");
                    mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                    mGrid.scrollToPosition(0);
                    setChannelIndex(0);
                    mGrid.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mGrid.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                            ProgramRow focusRow;
                            focusRow = (ProgramRow) mGrid.getChildAt(0).findViewById(R.id.row);
                            setRowFocus(focusRow);
                        }
                    }, 0);

                    if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181127 modify 3798 change Epg Channel rule
                        epgUiDisplay.ChangeProgram(getChannelIndex());

                    return true;
                }
                else
                {
                    ProgramRow focusRow;
                    if(focusIndex == mGrid.getChildCount()-1)// Last Line In Page
                    {
                        mGrid.scrollBy(0, itemHeight);
                        newFocusIndex = focusIndex;
                    }
                    else
                        newFocusIndex = focusIndex + 1;

                    Log.d(TAG, "onkeydown :  newFocusIndex = " + newFocusIndex);
                    focusRow = (ProgramRow) mGrid.getChildAt(newFocusIndex).findViewById(R.id.row);
                    setRowFocus(focusRow);

                    if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181127 modify 3798 change Epg Channel rule
                        epgUiDisplay.ChangeProgram(getChannelIndex());

                    return true;
                }
            }
            case KEYCODE_DPAD_UP:
            {
                int focusIndex = ChannelListGetFocusedChildIndex();
                int newFocusIndex;
                Log.d(TAG, "onKeyDown:   focusIndex = " + focusIndex + "     child count =" +mGrid.getChildCount());
                if(focusIndex == -1)
                    return true;

                if( focusIndex == 0 && getChannelIndex() == 0) // First Channel !!
                {
                    Log.d(TAG, "onKeyDown: First Channel !!");
                    mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                    mGrid.scrollToPosition(mGrid.getAdapter().getItemCount()-1);
                    setChannelIndex(mGrid.getAdapter().getItemCount()-1);

                    mGrid.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mGrid.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                            ProgramRow focusRow;
                            focusRow = (ProgramRow) mGrid.getChildAt(mGrid.getChildCount()-1).findViewById(R.id.row);
                            setRowFocus(focusRow);
                        }
                    }, 0);

                    if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181127 modify 3798 change Epg Channel rule
                        epgUiDisplay.ChangeProgram(getChannelIndex());

                    return true;
                }
                else
                {
                    ProgramRow focusRow;
                    if(focusIndex == 0)// First Channel In Page
                    {
                        mGrid.scrollBy(0, -itemHeight);
                        newFocusIndex = focusIndex;
                    }
                    else
                        newFocusIndex=focusIndex-1;

                    focusRow = (ProgramRow) mGrid.getChildAt(newFocusIndex).findViewById(R.id.row);
                    //Log.d(TAG, "onKeyDown:   test.getChildCount(); = " +focusRow.getChildCount() + "     focusIndex = " + focusIndex);
                    setRowFocus(focusRow);

                    if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181127 modify 3798 change Epg Channel rule
                        epgUiDisplay.ChangeProgram(getChannelIndex());

                    return true;
                }
            }

            case KEYCODE_CHANNEL_DOWN: // Page   Down
            {
                final int focusIndex = ChannelListGetFocusedChildIndex();
                Log.d(TAG, "onKeyDown:  focusIndex  ="+focusIndex);
                if(focusIndex == -1)
                    return true;

                PagePrev(focusIndex);
            }break;

            case KEYCODE_CHANNEL_UP: // Page   Down
            {
                final int focusIndex = ChannelListGetFocusedChildIndex();
                Log.d(TAG, "onKeyDown:  focusIndex  ="+focusIndex );
                if(focusIndex == -1)
                    return true;

                PageNext(focusIndex);
            }break;

            case KeyEvent.KEYCODE_TV_RADIO_SERVICE:// TVRADIO
            case ExtKeyboardDefine.KEYCODE_TV_RADIO_SERVICE:   // Johnny 20181210 for keyboard control
            {
                int preType = CurType;
                String str;
                if(CurType <= ProgramInfo.TV_FAV6_TYPE)
                    CurType = ProgramInfo.ALL_RADIO_TYPE;
                else
                    CurType = ProgramInfo.ALL_TV_TYPE;

                if(!epgUiDisplay.ChangeGroup(CurType))
                {
                    if (CurType <= ProgramInfo.TV_FAV6_TYPE)
                        str = getString(R.string.STR_NO_TV_CHANNEL);
                    else
                        str = getString(R.string.STR_NO_RADIO_CHANNEL);

                    new MessageDialogView(this, str, 3000) {
                        public void dialogEnd() {
                        }
                    }.show();
                    CurType = preType;
                }
                else
                {
                    CurChannelIndex=0;
                    mStartTime = floorTime(GetLocalTime().getTime() ,HALF_HOUR_IN_MILLIS);
                    CurFocusEvent = null;// Focus Event
                    mGrid.scrollToPosition(0);
                    curTime = mStartTime;
                    setTimeRange(mStartTime, mStartTime+HOUR_IN_MILLIS*2);
                    scrollOffset = 0 ;

                    mTimeListAdapter = null ;
                    mTimeListAdapter = new TimeListAdapter(mStartTime);
                    mTimelineRow.setAdapter(mTimeListAdapter);

                    scrollOffset = (int) (mWidthPerHour * getShiftedTime()/ HOUR_IN_MILLIS);
                    mTimelineRow.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTimelineRow.scrollTo(scrollOffset, false);
                        }
                    }, 0);

                    setUILayoutParam();
                    programTableAdapter.changeGroup();
                }
            }break;

            case KeyEvent.KEYCODE_BACK:
            {
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) {
                    if (ViewHistory.getCurChannel().getChannelNum() != 1) {
                        if ( changeChFlag ) //Scoty 20181129 modify VMX enter Epg rule
                        {
                            // Edwin 20181204 change to first channel
                            long channelId = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE).get(0).getChannelId();
                            //ProgramInfo newprogram = ProgramInfoGetByLcn(1, ProgramInfo.ALL_TV_TYPE);
                            AvControlPlayByChannelId(ViewHistory.getPlayId(), channelId, ProgramInfo.ALL_TV_TYPE, 1);
                        }
//                        if (Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1) { // for VMX need open/close
//                            AvControlShowVideo(ViewHistory.getPlayId(), false);
//                            AvControlSetMute(ViewHistory.getPlayId(), true);
//                        }
                    }
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }

    // edwin 20180709 add page up & page down -s
    private void PagePrev( final int focusIndex)
    {
        Log.d(TAG, "PagePrev:   focusIndex = " + focusIndex);
        float totalHeight;

        if ( mGrid.getChildAt(0) != null )
        {
            totalHeight = mGrid.getChildAt(0).getMeasuredHeight() * mGrid.getAdapter().getItemCount();
        }
        else
        {
            totalHeight = getResources()
                    .getDimension(R.dimen.LIST_VIEW_HEIGHT) * mGrid.getAdapter().getItemCount();
        }

        mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        getCurrentFocus().clearFocus();
        if ( mGrid.getChildAdapterPosition(mGrid.getChildAt(0)) == 0 )
        {
            mGrid.scrollBy(0, (int) totalHeight);
        }
        else
        {
            mGrid.scrollBy(0, -mGrid.getLayoutParams().height);
        }

        mGrid.postDelayed(new Runnable() // connie 20181012 for setfocus on new event
        {
            @Override
            public void run()
            {
                ProgramRow focusRow = (ProgramRow) mGrid.getChildAt(focusIndex).findViewById(R.id.row); // focusIndex
                setRowFocus(focusRow);
            }
        }, 0);
    }

    private void PageNext( final int focusIndex)
    {
        Log.d(TAG, "PageNext: ");

        float totalHeight;

        if ( mGrid.getChildAt(0) != null )
        {
            totalHeight = mGrid.getChildAt(0).getMeasuredHeight() * mGrid.getAdapter().getItemCount();
        }
        else
        {
            totalHeight = getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT) * mGrid.getAdapter().getItemCount();
        }

        mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        getCurrentFocus().clearFocus();
        if ( mGrid.getChildAdapterPosition(mGrid
                .getChildAt(mGrid.getChildCount()-1)) == (mGrid.getAdapter().getItemCount()-1) )
        {
            mGrid.scrollBy(0, (int) -totalHeight);
        }
        else
        {
            mGrid.scrollBy(0, mGrid.getLayoutParams().height);
        }

        mGrid.postDelayed(new Runnable() // connie 20181012 for setfocus on new event
        {
            @Override
            public void run()
            {
                Log.d(TAG, "run: focusIndex = " + focusIndex);
                ProgramRow focusRow = (ProgramRow) mGrid.getChildAt(focusIndex).findViewById(R.id.row); // focusIndex
                setRowFocus(focusRow);
            }
        }, 0);
    }
    // edwin 20180709 add page up & page down -e

    // Johnny 20181228 for mouse control -s
    private void onProgRedClicked() {
        if(isCloseFun)
            return;
        if (CurFocusEvent.getEventId() == 0)
            return;

        EPGEvent curEvent = CurFocusEvent;
        BookInfo CheckInfo = bookManager.CheckExist(epgUiDisplay.programInfoList.get(CurChannelIndex).getChannelId(),
                CurType, curEvent.getStartTime(), curEvent.getDuration());
        if (CheckInfo == null) {
            if (bookManager.CheckFull()) // Timer  Full !!!
            {
                new MessageDialogView(this, getString(R.string.STR_TIMER_FULL), 3000) {
                    public void dialogEnd() {
                    }
                }.show();
            }
            else
            {
                // Edwin 20190508 fix dialog not focus -s
                int bookId = bookManager.GetEmptyBookId();
                Handler fixDialogNotShow = new Handler();

                if (bookId < BookInfo.MAX_NUM_OF_BOOKINFO)
                {
                    mReserveDialog = new SetReserveInfoDialog(this, mDTVActivity, epgUiDisplay, bookManager, bookId, CurFocusEvent, CurChannelIndex, CurType);

                    fixDialogNotShow.postDelayed(new Runnable() {
                        @Override
                        public void run () {
                            mReserveDialog.show();
                            isShowReserveDialog = false; // Edwin 20190509 fix dialog not focus
                        }
                    }, 200);
                }
                // Edwin 20190508 fix dialog not focus -e
            }
        } else // Delete  BookInfo
        {
            new DeleteReserveDialog(this, mDTVActivity, bookManager, CheckInfo);
        }
    }

    private void onProgGreenClicked()
    {
        if(isCloseFun)
            return;
        final SummaryInfoDialog dialog = new SummaryInfoDialog(this, mDTVActivity, epgUiDisplay, bookManager);
        Handler fixDialogNotShow = new Handler();

        //BookManagerInit();
        bookManager = GetBookManager();

        // Edwin 20190508 fix dialog not focus -s
        fixDialogNotShow.postDelayed(new Runnable() {
            @Override
            public void run () {
                dialog.mDialog.show();
            }
        }, 200);
        // Edwin 20190508 fix dialog not focus -e
    }

    private void onProgYellowClicked() {
        ProgramRow focusRow;
        int focusIndex = ChannelListGetFocusedChildIndex();
        Log.d(TAG, "onKeyDown:  focusIndex =" + focusIndex);
        if( focusIndex == -1)
            return;

        long time = curTime ;
        if(getTimeRangeStart() - (6*HOUR_IN_MILLIS) < getStartTime())
            time = getStartTime();
        else
            time = time - 6*HOUR_IN_MILLIS;

        setCurTime(time);
        mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        shiftTime(-6*HOUR_IN_MILLIS);
        focusRow = (ProgramRow) mGrid.getChildAt(focusIndex).findViewById(R.id.row);
        setRowFocus(focusRow);
    }

    private void onProgBlueClicked() {
        int focusIndex = ChannelListGetFocusedChildIndex();
        ProgramRow focusRow;
        Log.d(TAG, "onKeyDown:   focusIndex  =" + focusIndex);

        if( focusIndex == -1)
            return;

        long time = curTime ;
        if(getTimeRangeEnd() + (6*HOUR_IN_MILLIS) > getEndTime())
            time = getStartTime();
        else
            time = time + 6*HOUR_IN_MILLIS;

        setCurTime(time);
        mGrid.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        shiftTime(6*HOUR_IN_MILLIS);

        focusRow = (ProgramRow) mGrid.getChildAt(focusIndex).findViewById(R.id.row);
        setRowFocus(focusRow);
    }
    // Johnny 20181228 for mouse control -e
}
