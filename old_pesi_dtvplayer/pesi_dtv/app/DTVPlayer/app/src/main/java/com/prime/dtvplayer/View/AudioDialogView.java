package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;

import java.util.ArrayList;

import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

/**
  Created by edwin on 2017/12/8.
 */

public class AudioDialogView extends Dialog {
    private final String TAG = getClass().getSimpleName();
    private final int STEREO = 0;
    private final int LEFT_ONLY = 1;
    private final int RIGHT_ONLY = 2;
    private TextView audioTypeTXV;
    private RecyclerView audioListView;
    private Context mContext;
    private ImageView background;
    private ImageView bottomFrame1;
    private ImageView bottomFrame2;
    private ImageView bottomFrame3;
    private ImageView bottomFrame4;    
    private int videoPID;
    private ArrayList<String> adapterList;
    private int audioType;
    private OnAudioClickListener onAudioClickedListener;
    private AudioInfo mAudioInfo;
    private AudioAdapter audioAdapter;
    private int childPos = 0;
    private int mParent=0; //eric lin 20180720 file play audio dialog //0 means view, 1 means pvr
    public interface OnAudioClickListener {
        void AudioClicked();
    }

    public AudioDialogView(@NonNull Context context,
                    final int parent,//eric lin 20180720 file play audio dialog
                    final EnAudioTrackMode curTrackMode,
                    final AudioInfo AudioComp,
                    final OnAudioClickListener onAudioClickedListener) {
        super(context);
        setContentView(R.layout.audio_dialog_view);

        mContext    = context;
        /*progInfo    = programInfo;
        videoPID    = programInfo.pVideo.getPID();
        pAudio      = programInfo.pAudios;
        audioIndex  = programInfo.getAudioLRSelected(); //index
        audioType   = programInfo.getAudioSelected(); //0:STEREO 1:LEFT_ONLY 2:RIGHT_ONLY*/

        this.onAudioClickedListener = onAudioClickedListener;
        mAudioInfo  = AudioComp;

        mParent = parent; //eric lin 20180720 file play audio dialog,-start
        if(mParent == 0) {
            AudioInfo.AudioComponent curAudioComp = mAudioInfo.getComponent(mAudioInfo.getCurPos());
        childPos = curAudioComp.getPos();
        videoPID = curAudioComp.getPid();
        }else if(mParent == 1){            
            AudioInfo.AudioComponent curAudioComp = mAudioInfo.getComponent(mAudioInfo.getCurPos());
            childPos = curAudioComp.getPos();            
        }//eric lin 20180720 file play audio dialog,-end

        Log.d(TAG, "AudioDialogView: " + curTrackMode);
        if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT)
        {
            audioType = LEFT_ONLY;
        }
        else if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT)
        {
            audioType = RIGHT_ONLY;
        }
        else if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO)
        {
            audioType = STEREO;
        }

        FindView();
        InitVar();
        InitDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        Log.d(TAG, "onKeyDown: ");
        View subtitleView = audioListView.getFocusedChild();
        int curtv_position = audioListView.getChildAdapterPosition(subtitleView);
        int listViewHeight = ((int) mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        int okCount = audioAdapter.getItemCount();
        int okOffset = okCount * listViewHeight;
        int childCount = audioListView.getChildCount();

        switch (keyCode) {
            case KEYCODE_DPAD_LEFT:
                SwitchLeftOrRight(KEYCODE_DPAD_LEFT);
                setTrackMode();
                break;
            case KEYCODE_DPAD_RIGHT:
                SwitchLeftOrRight(KEYCODE_DPAD_RIGHT);
                setTrackMode();
                break;
            case KEYCODE_DPAD_DOWN: {                
                if ((curtv_position >= 0) && (curtv_position == okCount - 1)) {
                    Log.d(TAG, "onKey: focus top item");
                    childPos = 0;
                    audioListView.scrollToPosition(0);
                    audioListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View view = audioListView.getLayoutManager().findViewByPosition(childPos);
                            if (view != null) {
                                view.requestFocus();
                            }
                        }
                    }, 0);
                    return true;
                }
                else {
                    if (childPos == (childCount - 1)) {
                        Log.d(TAG, "onKey: scroll down");
                        audioListView.scrollBy(0, listViewHeight);
                        audioListView.getChildAt(childPos).requestFocus();
                        childPos = childCount - 1;
                        return true;
                    }
                    else if (childPos < (childCount - 1)) {
                        Log.d(TAG, "onKey: move down");
                        childPos = childPos + 1;
                    }
                }                
                Log.d(TAG, "onKeyDown: KEYCODE_DPAD_DOWN--childPos="+childPos);
            }break;
            case KEYCODE_DPAD_UP: {
                
                if (curtv_position == 0) {
                    Log.d(TAG, "onKey: focus bottom item");                    
                    final int position = okCount-1;
                    childPos = childCount - 1;
                    audioListView.scrollToPosition(position);
                    audioListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View view = audioListView.getLayoutManager().findViewByPosition(position);
                            if (view != null) {
                                view.requestFocus();
                            }
                        }
                    }, 0);
                    return true;
                }
                else {
                    if (childPos == 0) {
                        Log.d(TAG, "onKey: scroll up");
                        audioListView.scrollBy(0, -listViewHeight);
                        childPos = 0;
                        audioListView.getChildAt(childPos).requestFocus();
                        return true;
                    }
                    else if (childPos > 0) {
                        Log.d(TAG, "onKey: move up");
                        childPos--;
                    }
                }                
                Log.d(TAG, "onKeyDown: KEYCODE_DPAD_UP--childPos="+childPos);
            }break;
            default:
                Log.d(TAG, "onKeyDown: default");
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void FindView() {
        audioTypeTXV  = (TextView) findViewById(R.id.audioDialog_message);
        audioListView = (RecyclerView) findViewById(R.id.audioDialogListview);
        background    = (ImageView) findViewById(R.id.audioDialog_bg);
        bottomFrame1  = (ImageView) findViewById(R.id.audioDialog_btm1);
        bottomFrame2  = (ImageView) findViewById(R.id.audioDialog_btm2);
        bottomFrame3  = (ImageView) findViewById(R.id.audioDialog_btm3);
        bottomFrame4  = (ImageView) findViewById(R.id.audioDialog_btm4);
        background    = (ImageView) findViewById(R.id.audioDialog_bg);
    }

    private void InitVar() {
        adapterList = new ArrayList<>();
    }

    private void InitDialog() {
        WindowManager.LayoutParams wlp;
        if (getWindow() != null) {
            wlp = getWindow().getAttributes();   // change dialog attributes
            wlp.gravity = Gravity.TOP | Gravity.START;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.verticalMargin = 0.05f;
            wlp.horizontalMargin = 0.05f;
            getWindow().setBackgroundDrawableResource(android.R.color.transparent); // set transparency
        }
        /*background.setAlpha((float)0.85);
        audioTypeTXV.setAlpha((float)0.85);
        bottomFrame1.setAlpha((float)0.85);
        bottomFrame2.setAlpha((float)0.85);
        bottomFrame3.setAlpha((float)0.85);
        bottomFrame4.setAlpha((float)0.85);*/

        if (audioType == STEREO) { // set bottom message
            audioTypeTXV.setText(R.string.STR_STEREO); // stereo
        }
        else if (audioType == LEFT_ONLY) {
            audioTypeTXV.setText(R.string.STR_LEFT_ONLY); // left audio
        }
        else if (audioType == RIGHT_ONLY) {
            audioTypeTXV.setText(R.string.STR_RIGHT_ONLY); // right audio
        }
        else
            Log.d(TAG, "AudioDialogView:" +
                    " audioSelection error," +
                    " audioType = "+ audioType);

        InitListView();
    }

    private void InitListView() {
        for ( int i = 0 ; i < mAudioInfo.getComponentCount() ; i++ )
        {
            if (mAudioInfo.getComponent(i).getAudioType() == 0x81) // DTV_PSISI_STREAM_AUDIO_AC3
                adapterList.add(mAudioInfo.getComponent(i).getLangCode()+" - Dolby");
            else
                adapterList.add(mAudioInfo.getComponent(i).getLangCode());
        }
        audioAdapter = new AudioAdapter(adapterList);
        audioListView.setAdapter(audioAdapter);
        audioAdapter.notifyDataSetChanged();
    }

    private void SwitchLeftOrRight(int key) {
        Log.d(TAG, "SwitchLeftOrRight: ");
        boolean pressLeft = (key == KEYCODE_DPAD_LEFT);
        boolean pressRight = (key == KEYCODE_DPAD_RIGHT);

        if (pressLeft) {
            audioType--;
            if (audioType == -1)
                audioType = RIGHT_ONLY;
        }
        else if (pressRight) {
            audioType++;
            if (audioType == 3)
                audioType = STEREO;
        }
        else {
            Log.d(TAG, "SwitchLeftOrRight: key is not left nor right");
        }

        Log.d(TAG, "SwitchLeftOrRight: audioType = "+ audioType);
        if (audioType == STEREO) {
            audioTypeTXV.setText(R.string.STR_STEREO);
        }
        else if (audioType == LEFT_ONLY) {
            audioTypeTXV.setText(R.string.STR_LEFT_ONLY);
        }
        else if (audioType == RIGHT_ONLY) {
            audioTypeTXV.setText(R.string.STR_RIGHT_ONLY);
        }
        else {
            Log.d(TAG, "SwitchLeftOrRight: " +
                    "audioType is not stereo, left only or right only");
        }
    }

    class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView audio;
            public ViewHolder(View itemView) {
                super(itemView);
                audio = (TextView) itemView.findViewById(R.id.audio);
            }
        }

        ArrayList<String> adapterList;

        AudioAdapter(ArrayList<String> adapterList) {
            if (adapterList != null)
                this.adapterList = adapterList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.listview_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.audio.setText(adapterList.get(position));
            if (position == childPos) {
                holder.itemView.requestFocus();
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAudioInfo.setCurPos(holder.getAdapterPosition()); // set cur audio pos
                    onAudioClickedListener.AudioClicked();
                    Log.d(TAG, "AudioClicked:" +
                            " AudioSelected(position) = "+ mAudioInfo.getComponent(holder.getAdapterPosition()).getPos() +
                            " AudioLRSelected(type) = "+ mAudioInfo.getComponent(holder.getAdapterPosition()).getAudioType()
                    );
                    dismiss();
                }
            });

        }

        @Override
        public int getItemCount() {
            return adapterList.size();
        }

    }

    private void setTrackMode(){
        EnAudioTrackMode selectTrackMode;
        DTVActivity dtv = ((DTVActivity) mContext);
        if (audioType == STEREO)
        {
            selectTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;  // STEREO by pesi
        }
        else if (audioType == LEFT_ONLY)
        {
            selectTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT;  // LEFT by pesi
        }
        else if (audioType == RIGHT_ONLY)
        {
            selectTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT;  // RIGHT by pesi
        }
        else
        {
            selectTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_BUTT;    // BUTT by pesi
        }
        if(mParent == 0)//eric lin 20180720 file play audio dialog
        dtv.AvControlSetTrackMode(dtv.ViewHistory.getPlayId(), selectTrackMode);
        else if(mParent == 1)//eric lin 20180720 file play audio dialog
            dtv.PvrPlaySetTrackMode(selectTrackMode);
    }
}