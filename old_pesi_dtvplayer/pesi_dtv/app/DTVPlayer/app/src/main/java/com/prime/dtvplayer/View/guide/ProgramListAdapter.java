package com.prime.dtvplayer.View.guide;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Activity.DimensionEPG;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;

import java.util.List;

import static android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS;

class ProgramListAdapter extends RecyclerView.Adapter<ProgramListAdapter.ProgramItemViewHolder> {
    private final String TAG = getClass().getSimpleName();

    private List<EPGEvent> meventList=null;
    private DimensionEPG mDimensionEPG=null;
    private DTVActivity.EpgUiDisplay mepgUiDisplay=null;
    private int mChannelIndex;

    ProgramListAdapter(DimensionEPG context, DTVActivity.EpgUiDisplay epgUiDisplay, List<EPGEvent> eventList, int channelIndex) {
        mDimensionEPG = context;
        mepgUiDisplay = epgUiDisplay;
        mChannelIndex = channelIndex;
        meventList = eventList;
        //Log.d(TAG, "ProgramListAdapter:channelIndex =  "+channelIndex);
        //Log.d(TAG, "ProgramListAdapter: meventList.size = "+meventList.size());
    }

    @Override
    public int getItemCount() {
        //Log.d(TAG, "ProgramListAdapter======>getItemCount: eventList = "+ meventList.size());
        return meventList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.program_guide_table_item;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<EPGEvent> getEventList()
    {
        return meventList;
    }


    @Override
    public void onBindViewHolder(final ProgramItemViewHolder holder, int position) {

        holder.onBind(meventList.get(position));
        long curTime = mDimensionEPG.getCurTime();

        if( mChannelIndex == mDimensionEPG.getChannelIndex() &&
                (meventList.get(position).getStartTime() <= curTime
                        && meventList.get(position).getEndTime() > curTime))
        {
            holder.itemView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDimensionEPG.mGrid.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
                    holder.itemView.requestFocus();
                }
            }, 0);
        }
    }

    @Override
    public ProgramItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "ProgramListAdapter : onCreateViewHolder: ");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ProgramItemViewHolder(itemView);
    }

    @Override
    public void onViewRecycled(ProgramItemViewHolder holder) {
        holder.onUnbind();
    }

    class ProgramItemViewHolder extends RecyclerView.ViewHolder {
        // Should be called from main thread.
        ProgramItemViewHolder(View itemView) {
            super(itemView);
        }

        void onBind(EPGEvent event) {
            Log.d(TAG, "ProgramListAdapter===>connie test ==>onBind: ");
            Log.d(TAG, "onBind: event Name = "+ event.getEventName());
            //Scoty Add Youtube/Vod Stream -s
            if(mepgUiDisplay.programInfoList.get(mChannelIndex).getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE
                    || mepgUiDisplay.programInfoList.get(mChannelIndex).getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                event =  mepgUiDisplay.programInfoList.get(mChannelIndex).getPresentepgEvent();
            //Scoty Add Youtube/Vod Stream -e
            ((ProgramItemView) itemView).setValues(mDimensionEPG, mChannelIndex, event, event.getStartTime(), event.getEndTime());
        }
        void onUnbind() {
            ((ProgramItemView) itemView).clearValues();
        }

    }
}
