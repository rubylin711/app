package com.prime.dtvplayer.View.guide;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Activity.DimensionEPG;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.List;


public class ProgramTableAdapter extends RecyclerView.Adapter<ProgramTableAdapter.ProgramRowViewHolder>
{
    private final String TAG = getClass().getSimpleName();
    private DimensionEPG dimensionEpg=null;
    private List<SimpleChannel> ProgramList=null;
    private DTVActivity.EpgUiDisplay mepgUiDisplay=null;
    private long mStartTime=0, mEndTime=0;

    public ProgramTableAdapter(DimensionEPG context, DTVActivity.EpgUiDisplay epgUiDisplay){
        dimensionEpg = context;
        mepgUiDisplay = epgUiDisplay;
        ProgramList = epgUiDisplay.programInfoList;
        mStartTime = dimensionEpg.getStartTime();
        mEndTime = dimensionEpg.getEndTime();
    }

    public void updateCHEvent(int channelIndex)
    {
        notifyItemChanged(channelIndex);
    }

    public void updateStartTime()
    {
        mStartTime = dimensionEpg.getStartTime();
        mEndTime = dimensionEpg.getEndTime();
    }

    public void changeGroup()
    {
        ProgramList = mepgUiDisplay.programInfoList;
        mStartTime = dimensionEpg.getStartTime();
        mEndTime = dimensionEpg.getEndTime();
        notifyDataSetChanged();
    }

//    public List<EPGEvent> GetEventData()
//    {
//        long CurEndTime = 0, nextStartTime = 0;
//        long duration = 0, totalDuration = 0;
//        List<EPGEvent> eventList = new ArrayList<>();
//        Date newDate=null;
//        EPGEvent newEvent = new EPGEvent();
//        SimpleDateFormat formatter= new SimpleDateFormat("yyyy  MMM dd  ... HH : mm",Locale.getDefault());
//        int realEventNum = 0;
//        if(mepgUiDisplay.epgUpdateData != null)
//            realEventNum = mepgUiDisplay.epgUpdateData.size();
//
//        // NO EVENT OR CUR TIME <  FIRST EVENT
//        if(mepgUiDisplay.epgUpdateData == null || mepgUiDisplay.epgUpdateData.size() == 0 ||
//                mepgUiDisplay.epgUpdateData.get(0).getStartTime() > mStartTime)
//        {
//            if (mepgUiDisplay.epgUpdateData == null ||mepgUiDisplay.epgUpdateData.size() == 0) {
//                CurEndTime = mStartTime;
//                nextStartTime = mEndTime;
//            }
//            else {
//                CurEndTime = mStartTime;
//                nextStartTime = mepgUiDisplay.epgUpdateData.get(0).getStartTime();
//            }
//            newEvent = SetEmptyEvent( CurEndTime, nextStartTime);
//            eventList.add(newEvent);
//        }
//
//        for (int j = 0; j < realEventNum; j++)
//        {
//            //Log.d(TAG, "update:  EventName = " + mepgUiDisplay.epgUpdateData.get(j).getEventName());
//            if (j + 1 < mepgUiDisplay.epgUpdateData.size()) {
//                CurEndTime = mepgUiDisplay.epgUpdateData.get(j).getEndTime();
//                nextStartTime = mepgUiDisplay.epgUpdateData.get(j + 1).getStartTime();
//            }
//            else // LAST EVENT!!!!!
//            {
//                CurEndTime = mepgUiDisplay.epgUpdateData.get(j).getEndTime();
//                nextStartTime = mEndTime;
//            }
//
//            eventList.add(mepgUiDisplay.epgUpdateData.get(j));
//            if(nextStartTime-CurEndTime!= 0)
//            {
//                newEvent = SetEmptyEvent( CurEndTime, nextStartTime);
//                eventList.add(newEvent);
//            }
//        }
//        return eventList;
//    }

    private ProgramListAdapter update_channel(int position)
    {
        mepgUiDisplay.EitDataUpdate(position, mStartTime, mEndTime, 1);
        List<EPGEvent> eventList = new ArrayList<>();
        //eventList = GetEventData(); // UI add Empty event
        // Pesi Service add Empty Event

        ProgramListAdapter listAdapter = null;
        if(mepgUiDisplay.epgUpdateData != null)
        {
            eventList.addAll(mepgUiDisplay.epgUpdateData);
            listAdapter = new ProgramListAdapter(dimensionEpg, mepgUiDisplay, eventList, position);

//        SimpleDateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd  HH : mm");
//        Log.d(TAG, "update_channel:  ======" + ProgramList.get(position).getChannelName() + "============");
//        for(int i = 0; i < eventList.size(); i++)
//        {
//            String strStartTime = formatter.format(eventList.get(i).getStartTime());
//            String strEndTime = formatter.format(eventList.get(i).getEndTime());
//            Log.d(TAG, "update_channel:  Name = " + eventList.get(i).getEventName() + "   [" + strStartTime + " ~ " + strEndTime +"]" + "  " + eventList.get(i).getStartTime() + "~"+eventList.get(i).getEndTime());
//        }
        }
        return listAdapter;
    }

//    public EPGEvent SetEmptyEvent(long CurEndTime, long nextStartTime)
//    {
//        SimpleDateFormat formatter= new SimpleDateFormat("yyyy  MMM dd  ... HH : mm");
//        EPGEvent emptyEvent = new EPGEvent();
//        emptyEvent.setEventName("No Information" );
//        emptyEvent.setEventId(-1);
//        emptyEvent.setStartTime(CurEndTime);
//        emptyEvent.setEndTime(nextStartTime);
//        emptyEvent.setDuration(nextStartTime-CurEndTime);
//        return emptyEvent;
//    }


    @Override
    public int getItemCount() {
        return mepgUiDisplay.programInfoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.program_guide_table_row;
    }

    @Override
    public void onBindViewHolder(ProgramRowViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder : position =" + position);
        holder.onBind(position);
    }

    @Override
    public ProgramRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(dimensionEpg).inflate(viewType, parent, false);
        ProgramRow programRow = (ProgramRow) itemView.findViewById(R.id.row);
        programRow.setItemAnimator(null);
        programRow.getRecycledViewPool().setMaxRecycledViews(R.layout.program_guide_table_item,0);
        return new ProgramRowViewHolder(itemView);
    }

    @Override
    public void onViewRecycled(ProgramRowViewHolder holder) {
        holder.onUnbind();
    }

    class ProgramRowViewHolder extends RecyclerView.ViewHolder {

        private final ViewGroup mContainer;
        private ProgramRow mProgramRow;
        private TextView mChannelName;
        private TextView mChannelNum;

        private ProgramRowViewHolder(View itemView) {
            super(itemView);
            mContainer = (ViewGroup) itemView;
            mProgramRow = (ProgramRow) mContainer.findViewById(R.id.row);
            mChannelNum = (TextView) mContainer.findViewById(R.id.chNumTXV);
            mChannelName = (TextView) mContainer.findViewById(R.id.chNameTXV);
        }

        private void onBind(int position)
        {
            String str = Integer.toString(ProgramList.get(position).getChannelNum());
            mChannelNum.setText(str);
            mChannelName.setText(ProgramList.get(position).getChannelName());
            ProgramListAdapter newEventList = update_channel(position);
            if (newEventList != null)
            {
                mProgramRow.swapAdapter(newEventList, true);
                mProgramRow.setProgramGuide(dimensionEpg, mepgUiDisplay, position, ProgramList.get(position).getChannelNum());
                mProgramRow.resetScroll(dimensionEpg.getTimelineRowScrollOffset(), newEventList.getEventList());
            }
        }
        private void onUnbind() {
            ProgramListAdapter adapter = (ProgramListAdapter)this.mProgramRow.getAdapter();
            adapter.getEventList().clear();
        }
    }
}
