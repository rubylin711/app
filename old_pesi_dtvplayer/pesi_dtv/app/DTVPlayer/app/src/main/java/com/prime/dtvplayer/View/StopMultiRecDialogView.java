package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.List;
import java.util.Locale;

public abstract class StopMultiRecDialogView extends Dialog {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private DTVActivity mdtv = null;
    private List<PvrInfo> mPvrList = null;
    private RecyclerView mPvrRecyclerView = null;
    private PvrListAdapter mPvrAdapter = null;
    private Handler CheckTimeHandler=null;
    private Dialog mParentDialog = null;

    public StopMultiRecDialogView(Context context, List<PvrInfo> pvrList, Dialog parent) {
        super(context);
        mContext = context;
        mdtv = (DTVActivity)context;
        mPvrList = pvrList;
        mParentDialog = parent;
        int default_height = 300;

//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area
        //show();//show dialog // Edwin 20190508 fix dialog has no focus
        setContentView(R.layout.stop_multi_record_dialog);


        Window window = getWindow();//get dialog widow size
        if (window == null) {
            Log.d(TAG, "StopMultiRecDialogView: window = null");
            return;
        }
        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp
        lp.dimAmount=0.0f;
        lp.height = default_height + (int)((mPvrList.size()+1)*mContext.getResources().getDimension(R.dimen.LIST_VIEW_HEIGHT));
        window.setAttributes(lp);//set dialog parameter
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.CENTER);

        mPvrRecyclerView = (RecyclerView)  findViewById(R.id.RecordRecyclerView);
        mPvrAdapter = new PvrListAdapter(mPvrList);
        mPvrRecyclerView.setAdapter(mPvrAdapter);
        mPvrAdapter.notifyDataSetChanged();
        startHandler();
        ((SimpleItemAnimator)mPvrRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void startHandler()
    {
        for(int i = 0 ; i < mPvrList.size() ; i++)
        {
            Log.d(TAG, "AAAAonBindViewHolder: i = "+ i +  " mode =>" + mPvrList.get(i).getPvrMode());
            if(mPvrList.get(i).getPvrMode() == PvrInfo.EnPVRMode.RECORD)
            {
                CheckTimeHandler = new Handler();
                CheckTimeHandler.post(GetPvrTime);
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {//key event
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP: {
                View curView = mPvrRecyclerView.getFocusedChild();
                int curPos = mPvrRecyclerView.getChildAdapterPosition(curView);
                if (curPos == 0) {
                    final int position = mPvrList.size()-1;
                    mPvrRecyclerView.scrollToPosition(position);
                    mPvrRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View view = mPvrRecyclerView.getLayoutManager().findViewByPosition(position);
                            if (view != null) {
                                view.requestFocus();
                            }
                        }
                    }, 0);
                    return true;
                }
            }break;
            case KeyEvent.KEYCODE_DPAD_DOWN:{
                View curView = mPvrRecyclerView.getFocusedChild();
                int curPos = mPvrRecyclerView.getChildAdapterPosition(curView);
                if (curPos == (mPvrList.size() - 1)) {
                    mPvrRecyclerView.scrollToPosition(0);
                    mPvrRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View view = mPvrRecyclerView.getLayoutManager().findViewByPosition(0);
                            if (view != null) {
                                view.requestFocus();
                            }
                        }
                    }, 0);
                    return true;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }

    class PvrListAdapter extends RecyclerView.Adapter<PvrListAdapter.ViewHolder> {
        private List<PvrInfo> mList;
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView chNum;
            TextView chName;
            TextView duration;
            ViewHolder(View itemView) {
                super(itemView);
                chNum = (TextView) itemView.findViewById(R.id.chnumTxv);
                chName = (TextView) itemView.findViewById(R.id.chnameTxv);
                duration = (TextView) itemView.findViewById(R.id.durationTXV);
            }
        }

        PvrListAdapter(List<PvrInfo> pvrList) {
            this.mList = pvrList;
            PvrInfo pvrCancel = new PvrInfo(0, 0, 0);//add cancel item
            mList.add(pvrCancel);
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.stop_multi_record_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final int pos = position;
            if(pos == (mList.size()-1)) {
                String str = mContext.getString(R.string.STR_HELP_FILTER_SPACE);
                holder.chNum.setText(str);
                holder.chName.setText(mContext.getString(R.string.STR_CANCEL));
                holder.chName.setGravity(Gravity.CENTER);
                holder.duration.setText(str);
            }
            else
            {
                long channelId = mList.get(pos).getChannelId();
                SimpleChannel channel = mdtv.GetSimpleProgramByChannelIdfromTotalChannelListByGroup(mdtv.ViewHistory.getCurGroupType(),channelId);

                holder.chNum.setText(String.valueOf(channel.getChannelNum()));
                holder.chName.setText(channel.getChannelName());

                Log.d(TAG, "AAAAonBindViewHolder: pos = " + pos + " mode = " + mList.get(pos).getPvrMode());
                if(mList.get(pos).getPvrMode() == PvrInfo.EnPVRMode.RECORD) {//if(pvrMode == REC) {
                    int RecTime, Hour, Min, Sec;
                    RecTime = mdtv.PvrRecordGetAlreadyRecTime(mdtv.ViewHistory.getPlayId(), mList.get(pos).getRecId());
                    Hour = RecTime / 60 / 60;
                    Min = (RecTime - (Hour * 60 * 60)) / 60;
                    Sec = RecTime - (Hour * 60 * 60) - (Min * 60);
                    String str = String.format(Locale.getDefault(),"%02d", Hour) + " : " + String.format(Locale.getDefault(),"%02d", Min) + " : " + String.format(Locale.getDefault(),"%02d", Sec);
                    holder.duration.setText(str);
                }
                else if(mList.get(pos).getPvrMode() == PvrInfo.EnPVRMode.TIMESHIFT_LIVE)
                {
                    String str = mContext.getString(R.string.STR_TIMESHIFT);
                    holder.duration.setText(str);
                }
                //holder.duration.setText();
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(pos < (mList.size()-1)) {
                        int pvrMode = mList.get(pos).getPvrMode();
                        int recId = mList.get(pos).getRecId();

                        onStopPVR(pvrMode, recId);

                        // edwin 20180725 for closing Voice Input Dialog -s
                        mdtv.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run ()
                            {
                                if ( mParentDialog != null )
                                {
                                    mParentDialog.dismiss();
                                }
                            }
                        });
                        // edwin 20180725 for closing Voice Input Dialog -e
                    }
                    dismiss();
                }
            });

            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        holder.itemView.setBackground(mContext.getDrawable(R.drawable.focus));
                        holder.chNum.setTextColor(Color.BLACK);
                        holder.chName.setTextColor(Color.BLACK);
                        holder.duration.setTextColor(Color.BLACK);
                    }
                    else
                    {
                        holder.itemView.setBackground(mContext.getDrawable(android.R.color.transparent));
                        holder.chNum.setTextColor(Color.WHITE);
                        holder.chName.setTextColor(Color.WHITE);
                        holder.duration.setTextColor(Color.WHITE);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    public void remove()
    {
        if(isShowing())
            dismiss();
        if(CheckTimeHandler != null) {
            CheckTimeHandler.removeCallbacks(GetPvrTime);
            CheckTimeHandler = null;
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(CheckTimeHandler != null) {
            CheckTimeHandler.removeCallbacks(GetPvrTime);
            CheckTimeHandler = null;
        }
        Log.d(TAG, "onStop:  CheckTimeHandler REMOVE !!!!!!");
    }

    final Runnable GetPvrTime = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            for(int i = 0 ; i < mPvrList.size() ; i++)
            {
                if(mPvrList.get(i).getPvrMode() == PvrInfo.EnPVRMode.RECORD)
                {
                    mPvrAdapter.notifyItemChanged(i);
                }
            }
            CheckTimeHandler.postDelayed(GetPvrTime, 1000);
        }
    };

    abstract public void onStopPVR(int pvrMode,int recId);
}
