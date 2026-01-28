package com.prime.dtvplayer.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.DeleteReserveDialog;
import com.prime.dtvplayer.View.DetailInfoDialog;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.SetReserveInfoDialog;
import com.prime.dtvplayer.View.SummaryInfoDialog;
import com.prime.dtvplayer.utils.TVMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.R.color.black;
import static android.R.color.holo_blue_bright;
import static android.R.color.white;
import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;
import static androidx.recyclerview.widget.RecyclerView.FOCUS_AFTER_DESCENDANTS;
import static androidx.recyclerview.widget.RecyclerView.FOCUS_BLOCK_DESCENDANTS;
import static android.view.KeyEvent.KEYCODE_CHANNEL_DOWN;
import static android.view.KeyEvent.KEYCODE_CHANNEL_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_INFO;
import static android.view.KeyEvent.KEYCODE_PROG_BLUE;
import static android.view.KeyEvent.KEYCODE_PROG_GREEN;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static android.view.KeyEvent.KEYCODE_PROG_YELLOW;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class EpgActivity extends DTVActivity {

    private static final String TAG = "EpgActivity";
    private  DTVActivity mDTVActivity = null;
    private ActivityHelpView helpView;
    private ActivityTitleView titleView;
    private TextView textviewDate;
    private TextView textviewDetail;
    private TextView textviewEventInfo;
    private TextView textviewNoInfo;
    private RecyclerView ListviewCH;
    private RecyclerView ListviewEvent;
    private long ONE_DAY_IN_MILLS = 24 * 60 *60 *1000;
    private Handler mHandler = null;// new Handler();
    private EpgUiDisplay epgUiDisplay = null;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    SimpleDateFormat CurTimeFormat = new SimpleDateFormat("MMM dd  HH : mm");
    SimpleDateFormat titleFormat=new SimpleDateFormat("dd  MMM . yyyy");
    private final LinearLayoutManager channelLayoutManager = new LinearLayoutManager(this);
    private final LinearLayoutManager eventLayoutManager = new LinearLayoutManager(this);
    private EpgChAdapter ChlistAdapter;
    private EpgEventAdapter EventlistAdapter;

    private int chindex = 0 ;
    private int eventindex = 0;
    private int curDate =0;
    private int totalDayEpg = 7 ;
    private int line_in_page = 7;
    private int CurType = 0;

    private EitUpdateThread EitThread=null;

    // === EPG RecyclerView var ===
    private boolean atEventList = false;
    private boolean updateEvent = true;
    private int visibleHeight;
    private int eventLine = -1;// event line
    private int chLine = 0; // current focus channel line
    //private int focusedPos = 0;
    private int itemHeight;
    private boolean changeChFlag = false;//Scoty 20181129 modify VMX enter Epg rule

    private final int  KEYCODE_GUIDE_PESI = 297 ;
    private BookManager bookManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.epg);
        mDTVActivity = this;
        titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        helpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);
        SetTitleHelp();
        SetHotKeyHelp();

        Bundle mainmenu =this.getIntent().getExtras();
        CurType = mainmenu != null ? mainmenu.getInt("type") : 0;
        chindex = mainmenu != null ? mainmenu.getInt("cur_channel") : 0;
        if(chindex == 0) {//gary20200619 fix enter epg focus not correct channel from pesi launcher
            if(ViewHistory.getCurChannel()!=null)
                chindex = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
        }
        if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181129 modify VMX enter Epg rule
            changeChFlag = mainmenu.getBoolean("changeCH");

        //for largest display size,--start
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);        
        int height = size.y;                
        int itemHeight =  ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));

        line_in_page = (int)(height*0.42)/itemHeight;
        Log.d(TAG, "onCreate: line_in_page = " + line_in_page);
        //for largest display size,--end

        String str;
        if(ViewHistory.getCurChannel() == null){
            if (CurType <= ProgramInfo.TV_FAV6_TYPE)
                str = getString(R.string.STR_NO_TV_CHANNEL);
            else
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

            mHandler = new Handler();
            if (EitThread == null) {
                EitThread = new EitUpdateThread();
                EitThread.start();
                Log.d(TAG, "EitUpdateThread  start !");
            } else
                Log.d(TAG, "EitUpdateThread  already exist  !");

            InitItem();
        }
    }

    public void onStart(){
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    public void onResume(){
        Log.d(TAG, "onResume: ");
        super.onResume();
        if(ViewHistory.getCurChannel() != null) {
            titleView.setCurTimeVisible();
            mHandler.post(mUpdateCurrentTime);
        }
    }
    public void onStop(){
        Log.d(TAG, "onStop: ");
        super.onStop();
        if(EitThread!=null){
            EitThread.quitLoop();
            EitThread=null;
            Log.d(TAG, "on stop : EitUpdateThread  quitLoop()!!!");
        }
        else
            Log.d(TAG, "on stop : EitUpdateThread  is NULL !!!");
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected: ");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected: ");
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();

        if (EitThread != null) {
			/*wait the thread exit*/
            EitThread.quitLoop();
            Log.d(TAG, "onDestroy");
            try {
                EitThread.join();
                Log.d(TAG, "wait for work thread exit done!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onDestroy : EitUpdateThread quitLoop() !!!");
        }
        else
            Log.d(TAG, "onDestroy : EitUpdateThread  is NULL !!!");
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        Log.d(TAG, "onMessage: tvMessage.getMsgType() = " + tvMessage.getMsgType());
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_EPG_UPDATE:
                Log.d(TAG, "onMessage: TYPE_EPG_UPDATE");
                long channelID = tvMessage.getChannelId();
                if(channelID == epgUiDisplay.programInfoList.get(chindex).getChannelId())
                {
                    Log.d(TAG, "onMessage:  TYPE_EPG_UPDATE!!!!!!!!!!" +
                            " MSG channelID = " + channelID +
                            " Cur chNum = " + epgUiDisplay.programInfoList.get(chindex).getChannelId()+
                            "chindex = " + chindex
                    );
                    EitThread.onSetupCmd(EitUpdateThread.EIT_UPDATE, chindex, curDate);
                }
                else {
                    Log.d(TAG, "onMessage:  TYPE_EPG_UPDATE!!!!!!!!!!" +
                            " MSG channelID = " + channelID +
                            " NO Channel in list !"
                    );
                }
                break;

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
            }break; // for VMX need open/close -e
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged: ");
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Log.d(TAG, "onWindowFocusChanged: ");
            int width = ListviewCH.getWidth();
            itemHeight = ((int) getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
            visibleHeight = line_in_page * itemHeight;

            LayoutParams params;
            params = new LayoutParams(width, visibleHeight);
            ListviewCH.setLayoutParams(params);

            width = ListviewEvent.getWidth();
            params = new LayoutParams(width, visibleHeight);
            ListviewEvent.setLayoutParams(params);
        }
    }

    // ====   Item  Init ================
    private void InitItem()
    {
        Log.d(TAG, "InitItem: ");
        titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        helpView = (ActivityHelpView) findViewById(R.id.HelpViewLayout);
        textviewDetail = (TextView) findViewById(R.id.detailTXV) ;
        textviewEventInfo = (TextView) findViewById(R.id.EventInfoTXV);
        textviewDate = (TextView) findViewById(R.id.curDateTXV);

        // ====  Set Title & Help =====
        titleView.setTitleView(getString(R.string.STR_CH_GUIDE_TITLE));
        helpView.setHelpInfoTextBySplit(null);

        //EpgUiDisplayInit(CurType);
        epgUiDisplay = GetEpgUiDisplay(CurType);
        bookManager = GetBookManager();

        InitChListUI();
        InitEventUI();

        if( ChlistAdapter != null)
            ChlistAdapter.notifyDataSetChanged();

        int focusPage = 0; //for focus on cur channel
        if(line_in_page < (chindex+1))
            focusPage = chindex / line_in_page;
        ListviewCH.scrollToPosition(focusPage*line_in_page);
        final int curChIndex = chindex;
        ListviewCH.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                int fistChPosition = ListviewCH.getChildAdapterPosition(ListviewCH.getChildAt(0));
                chLine = curChIndex-fistChPosition;
                View view = ListviewCH.getChildAt(chLine);
                if (view != null)
                    view.requestFocus();
            }
        }, 0);

        SetDateTitle(GetLocalTime().getTime());
        SetHotKeyHelp();

    }

    private void SetDateTitle(long date)
    {
        Log.d(TAG, "SetDateTitle: ");
        String curDate;
        curDate = titleFormat.format(date);
        textviewDate.setText(curDate);
        //Log.d(TAG, "Title Date = " + curDate);
    }

    //   ===========   Ch  List  UI  Init   ==========
    private void InitChListUI()
    {
        Log.d(TAG, "chlistUIInit: ");
        channelLayoutManager.setOrientation(VERTICAL);
        ListviewCH = (RecyclerView) findViewById(R.id.chLIV);
        ChlistAdapter = new EpgChAdapter(epgUiDisplay.programInfoList);
        //Log.d(TAG, "init   size  = " + epgUiDisplay.programInfoList.size());
        ListviewCH.setAdapter(ChlistAdapter);
        ListviewCH.setLayoutManager(channelLayoutManager);
        ListviewCH.setItemAnimator(null);

    }

    // ======  event UI  Init  =================
    private void InitEventUI()
    {
        Log.d(TAG, "eventUIInit: ");
        ListviewEvent = (RecyclerView) findViewById(R.id.eventLIV);
        textviewNoInfo = (TextView) findViewById(R.id.noInFoTXV);
        textviewNoInfo.setVisibility(INVISIBLE);

        eventLayoutManager.setOrientation(VERTICAL);
        EventlistAdapter = new EpgEventAdapter(epgUiDisplay.epgDisplayData);
        ListviewEvent.setAdapter(EventlistAdapter);
        ListviewEvent.setLayoutManager(eventLayoutManager);
        //ListviewEvent.setOnKeyListener(EventOnkeyLinstener);
        ListviewEvent.setItemAnimator(null);
    }

    // =============Event  Focus  Listener  ==================
    private AdapterView.OnFocusChangeListener EventFocusListener =  new AdapterView.OnFocusChangeListener()
    {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                Log.d(TAG, "onFocusChange: EventFocusListener detail info update");
                // ===  Set  Info & Summary  Help ===
                String help = getString(R.string.STR_EPG_INFO_HELP);
//                SpannableStringBuilder style = new SpannableStringBuilder(help);
//                style.setSpan(new ForegroundColorSpan(YELLOW), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                helpView.setHelpInfoTextBySplit(help);
                SetHotKeyHelp();

                ((TextView) v.findViewById(R.id.startTimerTXV)).setTextColor(getColor(black));
                ((TextView) v.findViewById(R.id.chnameTXV)).setTextColor(getColor(black));
                ((TextView) v.findViewById(R.id.chnameTXV)).setSelected(true);

                int position = ListviewEvent.getChildAdapterPosition(ListviewEvent.getFocusedChild());
                if (position == -1)
                    return;
                if(ListviewEvent.hasFocus())
                {
                    if(epgUiDisplay.epgDisplayData.get(position)!=null) {
                        EPGEvent curEvent = epgUiDisplay.epgDisplayData.get(position);
                        String strStartTime = formatter.format(curEvent.getStartTime());
                        String strEndTime = formatter.format(curEvent.getEndTime());
                        String EventInfo = strStartTime + " - " + strEndTime + "   " + curEvent.getEventName();
                        String Detail = epgUiDisplay.GetDetailInfo(epgUiDisplay.programInfoList.get(chindex).getChannelId(), curEvent.getEventId());
                        if(Detail == null || Detail.equals("")) // connie 20180806 fix detail show not complete
                        {
                            Detail = epgUiDisplay.GetShortEvent(epgUiDisplay.programInfoList.get(chindex).getChannelId(), curEvent.getEventId());
                            if(Detail == null)
                                Detail = "";
                        }
                        textviewEventInfo.setText(EventInfo);
                        textviewDetail.setText(Detail);
                        eventindex = position;
                    }
                    else
                        Log.d(TAG, "EventList  date  " + curDate + "   position   " + position + "is NULL!!!!!!");
                }
            }
            else
            {
                Log.d(TAG, "onFocusChange: remove event detail info");
                // === Remove Event Detail text ====
                textviewEventInfo.setText("");
                textviewDetail.setText("");

                ((TextView) v.findViewById(R.id.startTimerTXV)).setTextColor(getColor(white));
                ((TextView) v.findViewById(R.id.chnameTXV)).setTextColor(getColor(white));
                ((TextView) v.findViewById(R.id.chnameTXV)).setSelected(false);
            }
        }
    };

    // =============   EPG CH Adapter  =========================
    private class EpgChAdapter extends RecyclerView.Adapter<EpgChAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView chnum;
            TextView chname;
            ViewHolder(View itemView) {
                super(itemView);
                chnum = (TextView) itemView.findViewById(R.id.chnumTXV);
                chname = (TextView) itemView.findViewById(R.id.chnameTXV);
            }
        }

        List<SimpleChannel> programList;
        EpgChAdapter(List<SimpleChannel> programInfoList) {
            programList = programInfoList;
        }

        @Override
        public EpgChAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.epg_chlist_layout, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final EpgChAdapter.ViewHolder holder, int position) {
            if (programList == null)
                return;
            holder.chnum.setText(String.valueOf(programList.get(position).getChannelNum()));
            holder.chname.setText(programList.get(position).getChannelName());
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    int position = holder.getAdapterPosition();
                    //Log.d(TAG, "channel onFocusChange: current focus = "+ position());
                    if (hasFocus) {
                        Log.d(TAG, "onFocusChange: channel list get focus");
                        // ===  Set  OK : Summary  help ===
//                        SpannableStringBuilder style = new SpannableStringBuilder(
//                                getString(R.string.STR_EPG_OK_HELP));
//                        style.setSpan(new ForegroundColorSpan(YELLOW),
//                                0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        helpView.setHelpInfoTextBySplit( getString(R.string.STR_EPG_OK_HELP));

                        SetHotKeyHelp();

                        chindex = position;
                        eventindex = 0;
                        //curDate = 0;

                        Log.d(TAG, "onFocusChange: updateEvent = "+ updateEvent);
                        if ( updateEvent ) {
                            EitThread.onSetupCmd(EitUpdateThread.CHANGE_CH, chindex, curDate);
                        }
                        else {
                            Log.d(TAG, "onFocusChange: no change channel");
                        }

                        holder.chnum.setTextColor(getColor(black));
                        holder.chname.setTextColor(getColor(black));
                        holder.chname.setSelected(true);//for Marquee
                        //Log.d(TAG,"event get count = " +EventlistAdapter.getCount());
                    }
                    else {
                        holder.chname.setSelected(false);//for Marquee
                        if (ListviewEvent.hasFocus()) {
                            holder.chnum.setTextColor(getColor(holo_blue_bright));
                            holder.chname.setTextColor(getColor(holo_blue_bright));
                            return;
                        }
                        holder.chnum.setTextColor(getColor(white));
                        holder.chname.setTextColor(getColor(white));
                    }
                }
            });

            // Johnny 20181219 for mouse control -s
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeChannel();
                }
            });

            holder.itemView.setFocusableInTouchMode(true);
            // Johnny 20181219 for mouse control -e
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "channel getItemCount: channel = "+ programList.size());
            return programList.size();
        }
    }

    // =========   EPG  EVENT  Adapter  =================
    private class EpgEventAdapter extends RecyclerView.Adapter<EpgEventAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView startTime;
            TextView eventName;
            ViewHolder(View itemView) {
                super(itemView);
                startTime = (TextView) itemView.findViewById(R.id.startTimerTXV);
                eventName = (TextView) itemView.findViewById(R.id.chnameTXV);
            }
        }

        private List<EPGEvent> eventList;
        EpgEventAdapter(List<EPGEvent> event) {
            eventList = event;
        }

        @Override
        public EpgEventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: ");
            View convertView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.epg_eventlist_layout, parent, false);
            return new ViewHolder(convertView);
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "event getItemCount: event = " + eventList.size());
            return eventList.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) { // event onBindViewHolder
            Log.d(TAG, "onBindViewHolder: position = "+ position );
            if ( !eventList.isEmpty() ) {
                // === Set Event Name ===
                holder.eventName.setText(eventList.get(position).getEventName());

                Date startTime = new Date(epgUiDisplay.epgDisplayData.get(position).getStartTime());
                String EventStartTime = formatter.format(startTime);
                holder.startTime.setText(EventStartTime);

                // === RECYCLER VIEW SETTINGS ===
                Log.d(TAG, "onBindViewHolder: event" +
                        "\n event name = "+ holder.eventName.getText()+
                        "\n position = "+ position+
                        "\n eventLine = "+ eventLine+
                        "\n atEventList = "+ atEventList
                );
                holder.itemView.setOnFocusChangeListener(EventFocusListener);
                SetHotKeyHelp();
            } else
                Log.d(TAG, "position = " + position + "is  NULL !!!!");

            // Johnny 20181219 for mouse control -s
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // do nothing
                }
            });

            holder.itemView.setFocusableInTouchMode(true);
            // Johnny 20181219 for mouse control-e
        }
    }

    // ========  OnKeyDown ===================
    public boolean onKeyDown(int keyCode, final KeyEvent event) {
        int currentCh, lastCh;
        int currentEvent, lastEvent;
        int scrollHeight;
        currentCh = ListviewCH.getChildAdapterPosition(ListviewCH.getFocusedChild());
        currentEvent = ListviewEvent.getChildAdapterPosition(ListviewEvent.getFocusedChild());
        final RecyclerView recyclerView;

        switch (keyCode) {
            case KEYCODE_DPAD_DOWN:
                Log.d(TAG, "onKeyDown: DOWN");
                lastCh = channelLayoutManager.getItemCount()-1;
                lastEvent = eventLayoutManager.getItemCount()-1;
                updateEvent = true;

                if ((currentCh >= 0) && ListviewCH.hasFocus()) {
                    curDate = 0;
                    eventLine = -1;
                    SetDateTitle(GetFirstMillisOfCurrentDay() + curDate * ONE_DAY_IN_MILLS);

                    if (currentCh == lastCh) {
                        Log.d(TAG, "onKeyDown: channel list scroll up top");
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        chLine = 0;
                        ListviewCH.scrollToPosition(0);
                        ListviewCH.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = ListviewCH.getLayoutManager().findViewByPosition(0);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);
//                        scrollHeight = channelLayoutManager.getItemCount() * visibleHeight;
//                        ListviewCH.scrollBy(0, -scrollHeight);
//                        ListviewCH.getChildAt(0).requestFocus();
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        return true;
                    }
                    else if (chLine < (ListviewCH.getChildCount() - 1)) {
                        Log.d(TAG, "onKeyDown: channel list focus down one item");
                        chLine++;
                    }
                    else if (chLine == (ListviewCH.getChildCount() - 1)) {
                        Log.d(TAG, "onKeyDown: channel list scroll down one item");
                        ListviewCH.scrollBy(0, itemHeight);
                    }
                    else {
                        Log.d(TAG, "onKeyDown: key down else at channel focus");
                    }
                }
                else if ((currentEvent >= 0) && ListviewEvent.hasFocus()) {

                    if (currentEvent == lastEvent) {
                        Log.d(TAG, "onKeyDown: event list scroll up top");
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        scrollHeight = eventLayoutManager.getItemCount() * visibleHeight;
                        eventLine = 0;

                        ListviewEvent.scrollToPosition(0);
                        ListviewEvent.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = ListviewEvent.getLayoutManager().findViewByPosition(0);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);

//                        ListviewEvent.scrollBy(0, -scrollHeight);
//                        ListviewEvent.getChildAt(0).requestFocus();
//Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        Log.d(TAG, "onKeyDown:" +
                                " current ch = "+ currentCh+
                                " current event = "+ currentEvent);
                        return true;
                    }
                    else if (eventLine < (ListviewEvent.getChildCount() - 1)) {
                        Log.d(TAG, "onKeyDown: event list focus down one item");
                        ++eventLine;
                    }
                    else if (eventLine == (ListviewEvent.getChildCount() - 1)) {
                        Log.d(TAG, "onKeyDown: event list scroll down one item");
                        ListviewEvent.scrollBy(0, itemHeight);
                    }
                    else {
                        Log.d(TAG, "onKeyDown: key down else at event focus");
                    }
                }
                if (eventLine >= ListviewEvent.getChildCount())
                    eventLine = ListviewEvent.getChildCount() - 1;
                break;

            case KEYCODE_DPAD_UP:
                Log.d(TAG, "onKeyDown: UP");
                updateEvent = true;

                if ((currentCh >= 0) && ListviewCH.hasFocus()) {
                    curDate = 0;
                    eventLine = -1;
                    SetDateTitle(GetFirstMillisOfCurrentDay() + curDate * ONE_DAY_IN_MILLS);

                    if (currentCh == 0) {
                        Log.d(TAG, "onKeyDown: channel list scroll down bottom");
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        final int focusedPos = channelLayoutManager.getItemCount() - 1;
                        chLine = ListviewCH.getChildCount()-1;
                        ListviewCH.scrollToPosition(focusedPos);
                        ListviewCH.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = ListviewCH.getLayoutManager().findViewByPosition(focusedPos);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);

//                        scrollHeight = channelLayoutManager.getItemCount() * visibleHeight;
//                        ListviewCH.scrollBy(0, scrollHeight);
//                        ListviewCH
//                                .getChildAt(ListviewCH.getChildCount()-1)
//                                .requestFocus();
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        return true;
                    }
                    else if (chLine > 0) {
                        Log.d(TAG, "onKeyDown: channel list focus up one item");
                        --chLine;
                    }
                    else if (chLine == 0) {
                        Log.d(TAG, "onKeyDown: channel list scroll up one item");
                        ListviewCH.scrollBy(0, -itemHeight);
                    }
                    else {
                        Log.d(TAG, "onKeyDown: key up else at channel focus");
                    }
                }
                else if ((currentEvent >= 0) && ListviewEvent.hasFocus()) {
                    if (currentEvent == 0) {
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -s
                        Log.d(TAG, "onKeyDown: event list scroll down bottom");
                        eventLine = ListviewEvent.getChildCount() - 1;
                        final int focusedPos = eventLayoutManager.getItemCount() - 1;

                        ListviewEvent.scrollToPosition(focusedPos);
                        ListviewEvent.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                View view = ListviewEvent.getLayoutManager().findViewByPosition(focusedPos);
                                if (view != null) {
                                    view.requestFocus();
                                }
                            }
                        }, 0);

//                        scrollHeight = eventLayoutManager.getItemCount() * visibleHeight;
//                        ListviewEvent.scrollBy(0, scrollHeight);
//                        ListviewEvent
//                                .getChildAt(eventLine)
//                                .requestFocus();
                        //Scoty 20180529 change scrollBy to scrollToPosition to fix ANR crash when over 4,000 channels -e
                        return true;
                    }
                    else if (eventLine > 0) {
                        Log.d(TAG, "onKeyDown: event list focus up one item");
                        --eventLine;
                    }
                    else if (eventLine == 0) {
                        Log.d(TAG, "onKeyDown: event list scroll up one item");
                        ListviewEvent.scrollBy(0, -itemHeight);
                    }
                    else {
                        Log.d(TAG, "onKeyDown: key up else at event focus");
                    }
                }
                Log.d(TAG, "onKeyDown:" +
                        " chLine = "+ chLine+
                        " eventLine = "+ eventLine
                );
                break;

            case KEYCODE_DPAD_RIGHT:
                Log.d(TAG, "onKeyDown: RIGHT");

                if (ListviewEvent.getLayoutManager().getItemCount() == 0) {
                    Log.d(TAG, "onKeyDown: no event list");
                    return true;
                }
                if (eventLine == -1)
                    eventLine = 0;
                if (eventLine >= ListviewEvent.getChildCount())
                    eventLine = ListviewEvent.getChildCount() - 1;
                atEventList = true;
                ListviewCH.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                ListviewEvent.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                ListviewEvent.getChildAt(eventLine).requestFocus();
                Log.d(TAG, "onKeyDown: event focus visible index = "+ eventLine);
                break;

            case KEYCODE_DPAD_LEFT:
                Log.d(TAG, "onKeyDown: LEFT");
                if (chLine == -1)
                    chLine = 0;
                if (chLine >= ListviewCH.getChildCount())
                    chLine = ListviewCH.getChildCount()-1;
                atEventList = false;
                updateEvent = false;
                ListviewCH.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                ListviewEvent.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                ListviewCH.getChildAt(chLine).requestFocus();
                break;

            //case KEYCODE_3:
            case KEYCODE_INFO: // info key
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
                if (ListviewEvent.hasFocus()) {
                    EPGEvent Event = epgUiDisplay.epgDisplayData.get(eventindex);
                    long channelID = epgUiDisplay.programInfoList.get(chindex).getChannelId();
                    new DetailInfoDialog(this, epgUiDisplay, Event, channelID);
                }
                break;

            //case KEYCODE_4:
            case KEYCODE_PROG_RED: // Book Event
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                onProgRedClicked();     // Johnny 20181228 for mouse control
                break;

            //case KEYCODE_5:
            case KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: show summary info");
                onProgGreenClicked();   // Johnny 20181228 for mouse control
                break;

            //case KEYCODE_6:
            case KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
            { // pre date
                Log.d(TAG, "onkey Previous DATE   curdate =  " + curDate);
                onProgYellowClicked();  // Johnny 20181228 for mouse control
            }break;

            //case KEYCODE_7:
            case KEYCODE_PROG_BLUE: // Next Day
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG, "BLUE KEY");
                Log.d(TAG, "onkey Next DATE   curdate =  " + curDate);
                onProgBlueClicked();    // Johnny 20181228 for mouse control
            }break;

            case KEYCODE_CHANNEL_DOWN: // Page   Down
            {

                if ( ListviewCH.hasFocus() )
                {
                    PagePrev(ListviewCH);
                }
                else if ( ListviewEvent.hasFocus() )
                {
                    PagePrev(ListviewEvent);
                }
            }break;

            case KEYCODE_CHANNEL_UP: // Page   Down
            {
                if ( ListviewCH.hasFocus() )
                {
                    PageNext(ListviewCH);
                }
                else if ( ListviewEvent.hasFocus() )
                {
                    PageNext(ListviewEvent);
                }
            }break;

            // Johnny 20181219 for mouse control, is handled in click listener
//                Log.d(TAG, "onKeyDown: OK key");
//                if (ListviewCH.hasFocus()) {
//                    changeChannel();    // Johnny 20181219 for mouse control, use finction instead
//                }
//                break;

            case KEYCODE_GUIDE_PESI:
            case ExtKeyboardDefine.KEYCODE_GUIDE_PESI:  // Johnny 20181210 for keyboard control
                Intent it = new Intent();
                int curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
                Bundle bundle = new Bundle();
                it.setClass(this, DimensionEPG.class);
                bundle = new Bundle();
                bundle.putInt("type", ViewHistory.getCurGroupType() );
                bundle.putInt("cur_channel", curListPosition );
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181129 modify VMX enter Epg rule
                    bundle.putBoolean("changeCH",changeChFlag);
                it.putExtras(bundle);
                startActivity(it, bundle);
                finish();
                break;

            //case KEYCODE_9: // TVRADIO
            case KeyEvent.KEYCODE_TV_RADIO_SERVICE:
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

                    new MessageDialogView(EpgActivity.this, str, 3000) {
                        public void dialogEnd() {
                        }
                    }.show();
                    CurType = preType;
                }
                else
                {
                    chLine = 0;
                    eventLine = 0;
                    eventindex = 0;
                    curDate = 0;
                    chindex = 0;
                    ListviewCH.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                    ListviewEvent.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

                    ChlistAdapter = new EpgChAdapter(epgUiDisplay.programInfoList);
                    ListviewCH.setAdapter(ChlistAdapter);
                    ChlistAdapter.notifyDataSetChanged();
                    ListviewCH.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ListviewCH.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                            ListviewCH.getChildAt(chLine).requestFocus();

                            EitThread.onSetupCmd(EitUpdateThread.CHANGE_GRUOP, chindex, curDate);
                            SetDateTitle(GetFirstMillisOfCurrentDay()+curDate*ONE_DAY_IN_MILLS);
                            SetHotKeyHelp();
                        }
                    }, 0);
                }
            }break;

            case KeyEvent.KEYCODE_BACK:
            {
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) {
                    if (ViewHistory.getCurChannel().getChannelNum() != 1) {
                        if(changeChFlag) {//Scoty 20181129 modify VMX enter Epg rule
                            ProgramInfo newprogram = ProgramInfoGetByLcn(1, ProgramInfo.ALL_TV_TYPE);
                            AvControlPlayByChannelId(ViewHistory.getPlayId(), newprogram.getChannelId(), ProgramInfo.ALL_TV_TYPE, 1);
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

    // =========Event  Handler ============
    public class EventHandler extends Handler {
        EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: msg.what = "+ msg.what);
            //Log.d(TAG, "handleMessage: current focus = "+ getCurrentFocus());
            switch (msg.what) {
                case 0 : // EIT_UPDATE
                {
                    //Log.d(TAG,"------------handleMessage Update EIT List   IN---------");
                    Log.d(TAG, "handleMessage: EIT_UPDATE");
                    if (msg.arg1 == chindex) {
                        final int focusEvent = ListviewEvent.hasFocus() ? 1:0 ;
                        epgUiDisplay.EitDataDisplayUpdate();
                        if (EventlistAdapter != null) {
                            EventlistAdapter.notifyDataSetChanged();
                            Log.d(TAG, "handleMessage: notifyDataSetChanged" +
                                    "\n eventLine = "+ eventLine+
                                    "\n itemCount = "+ EventlistAdapter.getItemCount()
                            );
                        }

                        ListviewEvent.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(focusEvent == 1)
                                {
                                    if(ListviewEvent.getChildCount() > eventLine)
                                    {
                                        ListviewEvent.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                                        ListviewCH.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                                        ListviewEvent.getChildAt(eventLine).requestFocus();
                                    }
                                    else
                                    {
                                        ListviewEvent.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                                        ListviewCH.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                                        ListviewCH.getChildAt(chLine).requestFocus();
                                    }
                                }
                            }
                        }, 0);

                        if (eventLayoutManager.getItemCount() == 0)
                            textviewNoInfo.setVisibility(VISIBLE);
                        else
                            textviewNoInfo.setVisibility(INVISIBLE);
                    }
                    //Log.d(TAG,"------------handleMessage Update EIT List   OUT------------");
                }break;
                case 1: // CHANGE_CH  or  CHANGE_DATE or CHANGE_GRUOP
                {
                    //Log.d(TAG,"------------handleMessage Update EIT List   IN---------");
                    Log.d(TAG, "handleMessage: CHANGE_CH  or  CHANGE_DATE");
                    if (msg.arg1 == chindex && msg.arg2 == curDate) {
                        final int focusEvent = ListviewEvent.hasFocus() ? 1:0 ;
                        eventLayoutManager.scrollToPositionWithOffset(0, 0);
                        epgUiDisplay.EitDataDisplayUpdate();
                        if (EventlistAdapter != null)
                            EventlistAdapter.notifyDataSetChanged();

                        ListviewEvent.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(focusEvent == 1)
                                {
                                    if(EventlistAdapter.getItemCount() == 0) {
                                        ListviewEvent.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
                                        ListviewCH.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                                        ListviewCH.getChildAt(chLine).requestFocus();
                                    }
                                    else {
                                        ListviewEvent.getChildAt(0).requestFocus();
                                    }

                                }
                            }
                        }, 0);

                        if (eventLayoutManager.getItemCount() == 0)
                            //if (EventlistAdapter.getCount() == 0)
                            textviewNoInfo.setVisibility(VISIBLE);
                        else
                            textviewNoInfo.setVisibility(INVISIBLE);
                    }

                    if (EventlistAdapter.getItemCount() == 0) {
                        ListviewEvent.setVisibility(GONE);
                    }
                    else {
                        ListviewEvent.setVisibility(VISIBLE);
                    }
                    //Log.d(TAG,"------------handleMessage Update EIT List   OUT------------");
                }break;
            }
        }
    }

    // ===========EIT  Update  Thread=============
    class EitUpdateThread extends Thread {
        private Handler mHandler = null;
        private static final int EIT_UPDATE = 0;
        private static final int CHANGE_CH = 1;
        private static final int CHANGE_DATE = 2;
        private static final int CHANGE_GRUOP = 3;
        //private static final int DATA_RADIO = 4;
        //private static final int DATA_TV = 5;
        @SuppressLint("HandlerLeak")
        public void run() {
            Log.d(TAG, "run: ");
            Looper.prepare();
            mHandler = new Handler()
            {
                public void handleMessage(Message msg) {
                    Message message=new Message();
                    int channelIndex, DateIndex;
                    long StartTime, EndTime;
                    EventHandler ha =new EventHandler(Looper.getMainLooper());
                    Log.d(TAG, "handleMessage:     parma 1 =  " + msg.arg1 + "    param2 = " + msg.arg2);
                    switch (msg.what) {
                        case CHANGE_GRUOP:
                        case CHANGE_CH:
                        case CHANGE_DATE:
                        {
                            Log.d(TAG,"------------------------EitUpdateThread     CHANGE_DATE   IN---------------");
                            channelIndex = msg.arg1;
                            DateIndex = msg.arg2;
                            Log.d(TAG, "handleMessage:" +
                                    " DateIndex = "+ DateIndex +
                                    " curDate = " + curDate);
                            if(DateIndex==curDate && channelIndex == chindex) {
//                                if(DateIndex == 0)
//                                {
//                                    StartTime = GetLocalTime().getTime()/1000;
//                                    EndTime = (GetFirstMillisOfCurrentDay()/1000)+(24*60*60);
//                                }
//                                else
//                                {
//                                    StartTime = (GetFirstMillisOfCurrentDay()/1000)+(DateIndex *24*60*60);
//                                    EndTime = StartTime + 24*60*60;
//                                }
//                                Log.d(TAG, "handleMessage:    CHANGE_DATE  start time = " + StartTime + "    EndTime = " + EndTime);

                                Calendar tmpCalendar = getDateByOffset(curDate);
                                Date startDate = tmpCalendar.getTime();

                                if (0 != curDate)
                                {
                                    startDate.setHours(0);
                                    startDate.setMinutes(0);
                                    startDate.setSeconds(0);
                                }
                                /*3. set end day*/
                                tmpCalendar.add(Calendar.DAY_OF_MONTH, +1);

                                Date endDate = tmpCalendar.getTime();
                                endDate.setHours(0);
                                endDate.setMinutes(0);
                                endDate.setSeconds(0);
                                //List<EPGEvent> epgEventList = EpgEventGetEPGEventList(programInfo.getChannelId(), startDate.getTime(), endDate.getTime());

                                epgUiDisplay.EitDataUpdate(channelIndex, startDate.getTime(), endDate.getTime(), 0);
                                message.what=1;
                                message.arg1 = channelIndex;
                                message.arg2 = DateIndex;
                                ha.sendMessage(message);
                            }
                            Log.d(TAG,"------------------------EitUpdateThread     CHANGE_DATE   OUT---------------");
                        }break;
                        case EIT_UPDATE:
                        {
                            Log.d(TAG,"------------------------EitUpdateThread     EIT_UPDATE   IN---------------");
                            channelIndex = msg.arg1;
                            DateIndex = msg.arg2;
                            Log.d(TAG, "handleMessage:EIT_UPDATE!!!!!!!!  channelIndex = " + channelIndex + "      DateIndex = " + DateIndex);
                            if(channelIndex == chindex)
                            {
                                Calendar tmpCalendar = getDateByOffset(curDate);
                                Date startDate = tmpCalendar.getTime();

                                if (0 != curDate)
                                {
                                    startDate.setHours(0);
                                    startDate.setMinutes(0);
                                    startDate.setSeconds(0);
                                }
                                /*3. set end day*/
                                tmpCalendar.add(Calendar.DAY_OF_MONTH, +1);

                                Date endDate = tmpCalendar.getTime();
                                endDate.setHours(0);
                                endDate.setMinutes(0);
                                endDate.setSeconds(0);
                                epgUiDisplay.EitDataUpdate(channelIndex, startDate.getTime(), endDate.getTime(), 0);
                                message.what = 0;
                                message.arg1 = channelIndex;
                                message.arg2 = DateIndex;
                                ha.sendMessage(message);
                                Log.d(TAG,"------------------------EitUpdateThread     EIT_UPDATE   OUT---------------");
                            }
                        }break;
                        default:
                            break;
                    }
                }
            };
            Looper.loop();
            //Log.d(TAG, "work thread will now exit.");
        }

        void quitLoop() {
            Log.d(TAG, "quitLoop: ");
            if (mHandler != null && mHandler.getLooper() != null) {
                mHandler.getLooper().quit();
            }
        }

        void onSetupCmd(int cmd, int param1, int param2) {
            Log.d(TAG, "onSetupCmd: ----------------------------------------------");
            if (mHandler != null){
                mHandler.sendMessage(mHandler.obtainMessage(cmd, param1, param2));
            }
            else {
                Log.d(TAG, "onSetupCmd Fail   !!!!   mHandler is NULL  !!!");
            }
        }
    }

    private long GetFirstMillisOfCurrentDay(){
        Log.d(TAG, "GetFirstMillisOfCurrentDay: ");
        //Log.d(TAG, "get_firstmillisofcurrentday: current focus = "+ getCurrentFocus());
        Date date = GetLocalTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        date = calendar.getTime();
        return date.getTime();
    }

    private final Runnable mUpdateCurrentTime = new Runnable() {
        @Override
        public void run() {
            String strCurTime = CurTimeFormat.format(GetLocalTime());
            String curTime = titleView.getCurrentTime();
            if (curTime == null || !strCurTime.equals(curTime))
            {
                titleView.setCurrentTime(strCurTime);
            }
            mHandler.postDelayed(mUpdateCurrentTime, 1000);
        }
    };

    private void SetTitleHelp()
    {
        ActivityTitleView titleView = (ActivityTitleView) findViewById(R.id.TitleViewLayout);
        titleView.setTitleView(getString(R.string.STR_CH_GUIDE_TITLE));
        helpView.setHelpInfoTextBySplit(null);
    }

    private void SetHotKeyHelp()
    {
        Log.d(TAG, "SetHotKeyHelp: current focus = "+ getCurrentFocus());
        int iconIndex = 1;

        if(atEventList) {
            helpView.resetHelp(iconIndex, R.drawable.help_red, getString(R.string.STR_TIMER));
            helpView.setHelpIconClickListener(iconIndex, new View.OnClickListener() {   // Johnny 20181228 for mouse control
                @Override
                public void onClick(View view) {
                    onProgRedClicked();
                }
            });
            iconIndex++;
        }

        helpView.resetHelp(iconIndex, R.drawable.help_green, getString(R.string.STR_SUMMARY));
        helpView.setHelpIconClickListener(iconIndex, new View.OnClickListener() {   // Johnny 20181228 for mouse control
            @Override
            public void onClick(View view) {
                onProgGreenClicked();
            }
        });
        iconIndex++;

        if(curDate > 0) {
            helpView.resetHelp(iconIndex, R.drawable.help_yellow, getString(R.string.STR_PREVIOUS_DAY));
            helpView.setHelpIconClickListener(iconIndex, new View.OnClickListener() {   // Johnny 20181228 for mouse control
                @Override
                public void onClick(View view) {
                    onProgYellowClicked();
                }
            });
            iconIndex++;
        }

        if(curDate < totalDayEpg-1) {
            helpView.resetHelp(iconIndex, R.drawable.help_blue, getString(R.string.STR_NEXT_DAY));
            helpView.setHelpIconClickListener(iconIndex, new View.OnClickListener() {   // Johnny 20181228 for mouse control
                @Override
                public void onClick(View view) {
                    onProgBlueClicked();
                }
            });
            iconIndex++;
        }

        while(iconIndex <= 4) {
            helpView.resetHelp(iconIndex, 0, null);
            iconIndex++;
        }
    }

    private Calendar getDateByOffset(int dayOffSet)
    {
        Date date = GetLocalTime();
        if (date == null)
        {
            Log.d(TAG, "getDateByOffset:  Date is NULL !!!!!!!");
            date = new Date();
        }

        long dateOffsetMills = dayOffSet * (ONE_DAY_IN_MILLS);
        long dateMills = date.getTime() + dateOffsetMills;
        date.setTime(dateMills);
        Calendar ca = Calendar.getInstance();
        if (TmGetCurrentTimeZone() != 0)
        {
            ca.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        ca.setTime(date);

        return ca;
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
            totalHeight = list.getChildAt(0).getMeasuredHeight() * list.getAdapter().getItemCount();
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

    // Johnny 2081218 for mouse control
    public void changeChannel() {
        Log.d(TAG, "onKeyDown: ch list has focus / change program");
        View focusedChild = ListviewCH.getFocusedChild();
        int childPos = ListviewCH.getChildLayoutPosition(focusedChild);
        epgUiDisplay.ChangeProgram(childPos);
        if( Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1) { // for VMX need open/close
            AvControlShowVideo(ViewHistory.getPlayId(), false);
            AvControlSetMute(ViewHistory.getPlayId(), true);
        }
    }

    // Johnny 20181228 for mouse control -s
    private void onProgRedClicked() {
        if (ListviewEvent.hasFocus()) {
            BookInfo CheckInfo;
            EPGEvent curEvent = epgUiDisplay.epgDisplayData.get(eventindex);
            long chId = epgUiDisplay.programInfoList.get(chindex).getChannelId();
            int chType = CurType;
            long startTime = epgUiDisplay.epgDisplayData.get(eventindex).getStartTime();
            long duration = epgUiDisplay.epgDisplayData.get(eventindex).getDuration();
            CheckInfo = bookManager.CheckExist(chId, chType, startTime, duration);
            if (CheckInfo == null) {
                if (bookManager.CheckFull()) // Timer  Full !!!
                {
                    new MessageDialogView(EpgActivity.this,
                            getString(R.string.STR_TIMER_FULL), 5000) {
                        public void dialogEnd() {
                        }
                    }.show();
                } else {
                    int bookId = bookManager.GetEmptyBookId();
                    if (bookId < BookInfo.MAX_NUM_OF_BOOKINFO)
                    {
                        // Edwin 20190509 fix dialog not focus -s
                        final SetReserveInfoDialog dialog = new SetReserveInfoDialog(this, mDTVActivity, epgUiDisplay, bookManager, bookId, curEvent, chindex, CurType);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                dialog.show();
                            }
                        }, 200);
                        // Edwin 20190509 fix dialog not focus -e
                    }
                }
            } else // Delete  BookInfo
            {
                new DeleteReserveDialog(this, mDTVActivity, bookManager, CheckInfo);
            }
        }
    }

    private void onProgGreenClicked() {
        //BookManagerInit();
        bookManager = GetBookManager();

        // Edwin 20190509 fix dialog not focus -s
        final SummaryInfoDialog dialog = new SummaryInfoDialog(this, mDTVActivity, epgUiDisplay, bookManager);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                dialog.mDialog.show();
            }
        }, 200);
        // Edwin 20190509 fix dialog not focus -e
    }

    private void onProgYellowClicked() {
        if(curDate > 0) {
            curDate--;
            eventLine = 0;
            eventindex = 0;
            SetDateTitle(GetFirstMillisOfCurrentDay()+curDate*ONE_DAY_IN_MILLS);
            EitThread.onSetupCmd(EitUpdateThread.CHANGE_DATE,chindex, curDate);
            SetHotKeyHelp();
            Log.d(TAG, "set   pre date =  " + curDate);
        }
    }

    private void onProgBlueClicked() {
        if(curDate < totalDayEpg-1) {
            curDate++;
            eventLine = 0;
            eventindex = 0;
            SetDateTitle(GetFirstMillisOfCurrentDay()+curDate*ONE_DAY_IN_MILLS);
            EitThread.onSetupCmd(EitUpdateThread.CHANGE_DATE,chindex, curDate);
            SetHotKeyHelp();
        }
    }
    // Johnny 20181228 for mouse control -e
}