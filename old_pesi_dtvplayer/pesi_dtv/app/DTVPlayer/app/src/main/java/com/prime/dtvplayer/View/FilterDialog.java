package com.prime.dtvplayer.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

//import static android.view.KeyEvent.KEYCODE_0;
//import static android.view.KeyEvent.KEYCODE_1;
//import static android.view.KeyEvent.KEYCODE_2;
//import static android.view.KeyEvent.KEYCODE_3;
//import static android.view.KeyEvent.KEYCODE_4;
//import static android.view.KeyEvent.KEYCODE_5;
//import static android.view.KeyEvent.KEYCODE_6;
//import static android.view.KeyEvent.KEYCODE_7;
//import static android.view.KeyEvent.KEYCODE_8;
//import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

abstract public class FilterDialog extends Dialog
{
    private final String TAG = "FilterDialog";
    public final static int TAG_CHANNEL_NUM = 0;
    public final static int TAG_CHANNEL_NAME = 1;
    private final static int TYPE_TV = 0;
    //private final static int TYPE_RADIO = 0;
    private final int MAX_LENGTH = 25;

    private Context mCont;
    private String mKeyword;
    private String mChNum;
    private List<String> mMatchList;
    private CountDownTimer mTimer;
    private boolean isInit = true;
    private int keyWordPos = 0;
    private int mTag;

    private FilterList rvChannelList;
    private EditText edtKeyWord;
    private TextView txvChannelNotFound;
    private TextView txvTitle;
    //private ImageView imgLine;
    private ImageView imgKeywordLine;

    FilterDialog ( @NonNull Context context, int filterTag, List<String> matches )
    {
        super(context, R.style.transparentDialog);

        Log.d(TAG, "FilterDialog: ");

        mCont = context;
        mMatchList = matches;
        mTag = filterTag;

        if ( mTag == TAG_CHANNEL_NUM )
        {
            mKeyword = matches.get(0).replace(mCont.getString(R.string.STR_NUMBER_), "");
            mChNum = GetNumber(matches.get(0));
        }
        else if ( mTag == TAG_CHANNEL_NAME )
        {
            mKeyword = matches.get(0);
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_dialog);

        Log.d(TAG, "onCreate: ");
        txvTitle            = (TextView) findViewById(R.id.voice_input_title);
        imgKeywordLine      = (ImageView) findViewById(R.id.key_word_line);
        rvChannelList       = (FilterList) findViewById(R.id.key_word_list);
        edtKeyWord          = (EditText) findViewById(R.id.key_word);
        txvChannelNotFound  = (TextView) findViewById(R.id.key_word_has_no_channel);

        InitTimer(mChNum);
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        if ( ! hasFocus )
        {
            Log.d(TAG, "onWindowFocusChanged: has no focus");
            return;
        }

        Log.d(TAG, "onWindowFocusChanged: has focus");

        if ( isInit )
        {
            InitKeyboard();
            InitDialog();
            InitKeyWord(mKeyword);
            InitChannelList(mKeyword, mChNum, mTimer);
            isInit = false;
        }
    }

    @Override
    public boolean onKeyDown (int keyCode, @NonNull KeyEvent event)
    {
        Log.d(TAG, "onKeyDown: keyCode = "+keyCode);

        if ( mTimer != null )
        {
            mTimer.cancel();
        }

        switch ( keyCode )
        {
            case KEYCODE_DPAD_UP:
                if ( rvChannelList.hasFocus() && rvChannelList.getCurPos() == 0 )
                {
                    rvChannelList.setLoop(false); // edwin 20180726 fix focus
                    edtKeyWord.requestFocus();
                    return true;
                }
                else if ( rvChannelList.hasFocus() )
                {
                    return rvChannelList.moveUp();
                }
                break;

            case KEYCODE_DPAD_DOWN:
                if ( rvChannelList.hasFocus() )
                {
                    return rvChannelList.moveDown();
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void InitTimer(final String chNum)
    {
        Log.d(TAG, "InitTimer: chNum = "+chNum);

        mTimer = new CountDownTimer(3000, 1000)
        {
            @Override
            public void onTick (long millisUntilFinished)
            {

            }

            @Override
            public void onFinish ()
            {
                ChangeChannel(chNum);
                dismiss();
            }
        };
    }

    private void InitDialog()
    {
        Log.d(TAG, "InitDialog: ");

        if ( getWindow() == null )
        {
            return;
        }

        Display display = ((Activity) mCont).getWindowManager().getDefaultDisplay();
        Point screen = new Point();
        display.getSize(screen);

        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        getWindow().setGravity(Gravity.TOP|Gravity.START);
        wlp.x = 100;
        wlp.y = 100;
        wlp.width = (int) (screen.x * 0.35);
        wlp.height = screen.y - (2 * wlp.y);
        getWindow().setAttributes(wlp);
    }

    private void InitKeyWord (String initKeyword)
    {
        Log.d(TAG, "InitKeyWord: initKeyword = "+initKeyword);

        String keyword = initKeyword;
        if ( keyword.length() > MAX_LENGTH )
        {
            keyword = keyword.substring(0, MAX_LENGTH);
        }
        edtKeyWord.setText(keyword);
        edtKeyWord.setSelection(keyword.length());

        if ( mTag == TAG_CHANNEL_NAME )
        {
            String title = txvTitle.getText().toString().concat(mCont.getString(R.string.STR_CHANNEL_NAME));
            edtKeyWord.setOnClickListener(new ShowKeyboard());
            edtKeyWord.setOnKeyListener(new KeyController());
            txvTitle.setText(title);
            imgKeywordLine.setVisibility(View.GONE);
        }
        else if ( mTag == TAG_CHANNEL_NUM )
        {
            String title = txvTitle.getText().toString().concat(mCont.getString(R.string.STR_CHANNEL_NUMBER));
            edtKeyWord.setOnClickListener(null);
            edtKeyWord.setOnKeyListener(new KeyController());
            edtKeyWord.setInputType(InputType.TYPE_NULL);
            edtKeyWord.setTextIsSelectable(true);
            txvTitle.setText(title);
            txvChannelNotFound.setText(mCont.getString(R.string.STR_NUMBER_NOT_FOUND));
            imgKeywordLine.setVisibility(View.GONE);
        }

        edtKeyWord.addTextChangedListener(new UpdateChannel());
    }

    private void InitChannelList (String keyWord, String chNum, CountDownTimer timer)
    {
        Log.d(TAG, "InitChannelList: keyWord = "+keyWord);

        List<SimpleChannel> channelList = GetChannelList(keyWord, chNum);

        // init channel list
        rvChannelList.setAdapter(new ChannelListAdapter(channelList));
        rvChannelList.setListNum(GetListNum());

        if ( mTag == TAG_CHANNEL_NUM )
        {
            rvChannelList.post(FocusByChannelNum(channelList, chNum, timer));
        }
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
            case KEYCODE_DPAD_UP:
            case KEYCODE_DPAD_DOWN:
            case KEYCODE_DPAD_CENTER:   // Johnny 20180802 add
            case KEYCODE_BACK:  // Johnny 20180802 add
                timer.cancel();
                mTimer = null;
                break;

//            case KEYCODE_0:
//            case KEYCODE_1:
//            case KEYCODE_2:
//            case KEYCODE_3:
//            case KEYCODE_4:
//            case KEYCODE_5:
//            case KEYCODE_6:
//            case KEYCODE_7:
//            case KEYCODE_8:
//            case KEYCODE_9:
//                timer.cancel();
//                timer.start();
//                break;
        }
    }

    public void UpdateMatchList(List<String> matches)
    {
        Log.d(TAG, "UpdateMatchList: ");

        mMatchList = matches;
        mKeyword = matches.get(0).replace("number ", "");

        String keyword = mKeyword;
        if ( mKeyword.length() > MAX_LENGTH )
        {
            keyword = keyword.substring(0, MAX_LENGTH);
        }
        edtKeyWord.setText(keyword);
        edtKeyWord.setSelection( keyword.length() );
    }

    private void UpdateChannelList (String keyWord, String chNum)
    {
        Log.d(TAG, "UpdateChannelList: keyWord = "+keyWord);

        List<SimpleChannel> channelList = GetChannelList(keyWord, chNum);

        // update channel list
        ChannelListAdapter adapter = (ChannelListAdapter) rvChannelList.getAdapter();
        adapter.setChannelList(channelList);
        adapter.notifyDataSetChanged();
    }

    private Runnable FocusByChannelNum( final List<SimpleChannel> channelList,
            final String chNum,
            final CountDownTimer timer )
    {
        Log.d(TAG, "FocusByChannelNum: chNum = "+chNum);

        return new Runnable()
        {
            @Override
            public void run ()
            {
                if ( chNum != null && !chNum.isEmpty() && mTag == TAG_CHANNEL_NUM )
                {
                    for ( int i = 0; i < channelList.size(); i++ )
                    {
                        if ( channelList.get(i).getChannelNum() == Integer.valueOf(chNum) )
                        {
                            rvChannelList.setSelection(i);
                            timer.start();
                            break;
                        }
                    }
                }
            }
        };
    }

    private List<SimpleChannel> GetChannelList(String keyWord, String chNum)
    {
        Log.d(TAG, "GetChannelList: keyWord = "+keyWord+" chNum = "+chNum);

        LinearLayout channelListLayout = (LinearLayout) findViewById(R.id.key_word_list_layout); // edwin 20180726 fix focus
        List<SimpleChannel> channelList = new ArrayList<>();
        DTVActivity dtv = (DTVActivity) mCont;

        if ( mTag == TAG_CHANNEL_NAME )
        {
            channelList = dtv.ProgramInfoGetListByFilter(TAG_CHANNEL_NAME, TYPE_TV, keyWord,0,1);//Scoty 20181109 modify for skip channel
        }
        else if ( mTag == TAG_CHANNEL_NUM )
        {
            channelList = dtv.ProgramInfoGetListByFilter(TAG_CHANNEL_NUM, TYPE_TV, chNum,0,1);//Scoty 20181109 modify for skip channel
        }

        if ( channelList.size() == 0 )
        {
            channelListLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); // edwin 20180726 fix focus
            txvChannelNotFound.setVisibility(View.VISIBLE);
            SimpleChannel empty = new SimpleChannel();
            empty.setChannelName("");
            empty.setChannelNum(-1);
            for ( int i = 0; i < 10; i++ )
            {
                channelList.add(empty);
            }
        }
        else
        {
            channelListLayout.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS); // edwin 20180726 fix focus
            txvChannelNotFound.setVisibility(View.INVISIBLE);
            channelList.sort(new Comparator<SimpleChannel>()
            {
                @Override
                public int compare (SimpleChannel o1, SimpleChannel o2)
                {
                    return Integer.compare(o1.getChannelNum(), o2.getChannelNum());
                }
            });
        }

        return channelList;
    }

    private int GetListNum()
    {
        Log.d(TAG, "GetListNum: ");

        if ( getWindow() == null )
        {
            Log.d(TAG, "GetListNum: getWindow() == null");
            return 0;
        }
        
        float itemHeight = (int) mCont.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);
        float dialogHeight = getWindow().getAttributes().height;

        return (int) (dialogHeight / itemHeight) - 2;
    }

    private String GetNumber(String result)
    {
        Log.d(TAG, "GetNumber: input = "+result);

        List<String> numList = Arrays.asList(mCont.getResources().getStringArray(R.array.STR_ARRAY_NUM));
        String[] splitResult = result.split(mCont.getString(R.string.STR_HELP_FILTER_SPACE));
        String chNum = "";

        if ( splitResult.length < 1 )
        {
            Log.d(TAG, "GetNumber: output = has no number");
            return "0";
        }

        for ( int i = 1; i < splitResult.length; i++ )
        {
            if ( splitResult[i].matches("\\d+(?:\\.\\d+)?") )
            {
                chNum = chNum.concat(splitResult[i]);
            }
            else if ( numList.contains(splitResult[i]) )
            {
                String index = String.valueOf(numList.indexOf(splitResult[i]));
                Log.d(TAG, "GetNumber: index = "+index);
                chNum = chNum.concat(index);
            }
            else
            {
                Log.d(TAG, "GetNumber: output = word");
                return "0";
            }
        }
        Log.d(TAG, "GetNumber: output = "+chNum);

        return chNum;
    }

    private void InitKeyboard()
    {
        Log.d(TAG, "InitKeyboard: ");

        Window window = getWindow();

        if (window != null)
        {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private void SwitchKeyWord(int keyCode, List<String> matchList)
    {
        Log.d(TAG, "SwitchKeyWord: ");

        if ( matchList == null )
        {
            return;
        }

        if ( keyCode == KEYCODE_DPAD_LEFT )
        {
            if ( keyWordPos == 0 )
            {
                keyWordPos = matchList.size() - 1;
            }
            else
            {
                keyWordPos--;
            }
        }
        else if ( keyCode == KEYCODE_DPAD_RIGHT )
        {
            if ( keyWordPos == (matchList.size() - 1) )
            {
                keyWordPos = 0;
            }
            else
            {
                keyWordPos++;
            }
        }

        if ( mTag == TAG_CHANNEL_NAME )
        {
            String chName = matchList.get(keyWordPos);
            if ( chName.length() > MAX_LENGTH )
            {
                chName = chName.substring(0, MAX_LENGTH);
            }
            edtKeyWord.setText(chName);
            edtKeyWord.setSelection(chName.length());
        }
        else if ( mTag == TAG_CHANNEL_NUM )
        {
            String chNum = matchList.get(keyWordPos).replace(mCont.getString(R.string.STR_NUMBER_), "");
            if ( chNum.length() > MAX_LENGTH )
            {
                chNum = chNum.substring(0, MAX_LENGTH);
            }
            edtKeyWord.setText(chNum);
        }
    }

    abstract void ChangeChannel ( String chNum );

//    private void ChangeChannel(final int pos, int chId)
//    {
//        Log.d(TAG, "ChangeChannel: ");
//
//        final DTVActivity dtv = ((DTVActivity) cont);
//        final ViewActivity act = ((ViewActivity) cont);
//        int pvrMode = dtv.getCurrentPvrMode();
//        int curTpId = dtv.ViewHistory.getCurChannel().getTpId();
//        int newTpId = dtv.ProgramInfoGetByChannelId(chId).getTpId();
//        final List<PvrInfo> pvrList = dtv.PvrRecordGetAllInfo();
//        final List<SimpleChannel> channelList = ((ChannelListAdapter) rvChannelList.getAdapter()).getChannelList();
//
//        Log.d(TAG, "ChangeChannel: pvrMode = "+pvrMode);
//        Log.d(TAG, "ChangeChannel: curTpId = "+curTpId);
//        Log.d(TAG, "ChangeChannel: newTpId = "+newTpId);
//        Log.d(TAG, "ChangeChannel: pvrList.size() = "+pvrList.size());
//        if ( (pvrMode != PvrInfo.EnPVRMode.NO_ACTION)
//                && (curTpId != newTpId)
//                && (pvrList.size() > 0)
//                && (pvrList != null) )
//        {
//            Log.d(TAG, "ChangeChannel: pvrMode != PvrInfo.EnPVRMode.NO_ACTION");
//            new StopMultiRecDialogView(cont, pvrList, this)
//            {
//                @Override
//                public void onStopPVR(int pvrMode, int recId)
//                {
//                    Log.d(TAG, "onStopPVR: stop pvr, change channel");
//                    dtv.mRecID = recId;
//                    act.stopPVRMode(pvrMode, 1);
//                    dtv.AvControlPlayByChannelId(
//                            dtv.ViewHistory.getPlayId(),
//                            channelList.get(pos).getChannelId(),
//                            dtv.ViewHistory.getCurGroupType(),1);
//                    act.isShown_FilterDialog = false;
//                }
//            };
//        }
//        else
//        {
//            Log.d(TAG, "ChangeChannel: pvrMode == PvrInfo.EnPVRMode.NO_ACTION");
//            dtv.AvControlPlayByChannelId(
//                    dtv.ViewHistory.getPlayId(),
//                    channelList.get(pos).getChannelId(),
//                    dtv.ViewHistory.getCurGroupType(),1);
//            act.isShown_FilterDialog = false;
//            dismiss();
//        }
//    }



//    private void SetListNum(final VoiceInputRecyclerView targetView, final View topView, final View bottomView)
//    {
//        Log.d(TAG, "SetListNum: ");
//
//        int locBottom[] = new int[2];
//        int locTop[] = new int[2];
//        int rvItemHeight = (int) cont.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT);
//
//        if (targetView.getChildAt(0) != null) // edwin 20180704 avoid null item view
//        {
//            rvItemHeight = targetView.getChildAt(0).getMeasuredHeight();
//        }
//
//        bottomView.getLocationOnScreen(locBottom);
//        topView.getLocationOnScreen(locTop);
//
//        int rvHeight = locBottom[1] - locTop[1] - topView.getMeasuredHeight();
//
//        //Log.d(TAG, "run: rvHeight = "+rvHeight+
//        //        ", rvItemHeight = "+rvItemHeight+
//        //        ", rvHeight/rvItemHeight = "+rvHeight/rvItemHeight);
//
//        targetView.setListNum(rvHeight/rvItemHeight);
//    }

//    private class ResetText implements View.OnFocusChangeListener
//    {
//        @Override
//        public void onFocusChange (View v, boolean hasFocus)
//        {
//            Log.d(TAG, "onFocusChange: EditText CleanText");
//            if ( ! hasFocus || isInit )
//            {
//                return;
//            }
//
//            edtKeyWord.setText("");
//        }
//    }

    private class UpdateChannel implements TextWatcher
    {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count)
        {
            Log.d(TAG, "onTextChanged: s.toString() = "+s.toString());

            String newKeyWord = s.toString();
            String chNum = GetNumber(mCont.getString(R.string.STR_NUMBER_)+s.toString());
            UpdateChannelList(newKeyWord, chNum);
        }

        @Override
        public void afterTextChanged (Editable s)
        {

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

            switch ( keyCode )
            {
                case KEYCODE_DPAD_LEFT:
                case KEYCODE_DPAD_RIGHT:
                    SwitchKeyWord(keyCode, mMatchList); //switch key word
                    return true;

                case KEYCODE_DPAD_UP:
                    rvChannelList.setSelection(rvChannelList.getItemCount()-1);
                    return true;
            }

            return false;
        }
    }

    private class ShowKeyboard implements View.OnClickListener
    {
        @Override
        public void onClick (View v)
        {
            Log.d(TAG, "onClick: ShowSoftInput");

            InputMethodManager mgr = (InputMethodManager) mCont.getSystemService(Context.INPUT_METHOD_SERVICE);

            if ( mgr != null )
            {
                mgr.showSoftInput(edtKeyWord, InputMethodManager.SHOW_FORCED);
            }
        }
    }

    private class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder>
    {
        private List<SimpleChannel> channelList;

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView txvChannel;
            TextView txvChannelNo;

            public ViewHolder (View itemView)
            {
                super(itemView);
                txvChannel = (TextView) itemView.findViewById(R.id.voice_input_channel);
                txvChannelNo = (TextView) itemView.findViewById(R.id.voice_input_channel_num);
            }
        }

        ChannelListAdapter (List<SimpleChannel> list)
        {
            Log.d(TAG, "ChannelListAdapter: ");

            channelList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.filter_channel_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder (final ViewHolder holder, int position)
        {
            String chNum    = String.valueOf(channelList.get(position).getChannelNum());
            String chName   = channelList.get(position).getChannelName();
            //final int chId  = channelList.get(position).getChannelId();
            
            if ( chNum.equals(String.valueOf(-1)) )
            {
                chNum = null;
            }

            holder.txvChannelNo.setText(chNum);
            holder.txvChannel.setText(chName);
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange (View v, boolean hasFocus)
                {
                    Log.d(TAG, "onFocusChange: set Marquee (Scrolling Text)");

                    holder.txvChannelNo.setSelected(hasFocus);
                    holder.txvChannel.setSelected(hasFocus);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick (View v)
                {
                    Log.d(TAG, "onClick: Change Channel");

                    ChangeChannel((String) holder.txvChannelNo.getText()); //ChangeChannel(holder.getAdapterPosition(), chId);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount ()
        {
            return channelList.size();
        }

        public List<SimpleChannel> getChannelList ()
        {
            return channelList;
        }

        public void setChannelList (List<SimpleChannel> channelList)
        {
            this.channelList = channelList;
        }
    }
}
