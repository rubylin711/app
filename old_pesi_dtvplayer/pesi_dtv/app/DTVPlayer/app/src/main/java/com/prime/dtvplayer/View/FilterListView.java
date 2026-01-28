package com.prime.dtvplayer.View;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dtvplayer.Activity.RecordNavigatorActivity;
import com.prime.dtvplayer.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FilterListView extends RecyclerView
{
    private static final String TAG = "FilterListView";

    private WeakReference<RecordNavigatorActivity> mWeakReference = null;
    private List<String> mFilterInfoList = new ArrayList<>();

    public FilterListView(Context context)
    {
        this(context,null);
    }

    public FilterListView(Context context, AttributeSet attrs)
    {
        this(context, attrs, -1);
    }

    public FilterListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setupFilter(RecordNavigatorActivity activity, List<String> pvrFileInfoList)
    {
        mWeakReference = new WeakReference<>(activity);

        for (String info : pvrFileInfoList)
        {
            if (!mFilterInfoList.contains(info))
                mFilterInfoList.add(info);
        }

        setLayoutManager(new LinearLayoutManager(activity, RecyclerView.VERTICAL, false));
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        super.setAdapter(new FilterAdapter());
    }

    public int getItemCount()
    {
        return getAdapter().getItemCount();
    }

    @Nullable
    @Override
    public FilterAdapter getAdapter()
    {
        return (FilterAdapter) super.getAdapter();
    }

    class FilterAdapter extends Adapter<FilterAdapter.ViewHolder>
    {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.record_navigator_filter_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            holder.filterName.setText(mFilterInfoList.get(position));
            holder.itemView.setOnFocusChangeListener(new OnFocusChangeColor(position));
            holder.itemView.setOnKeyListener(new OnKeyExitFilter());
            holder.itemView.setOnClickListener(new OnClickStartFilter());
        }

        @Override
        public int getItemCount()
        {
            if (mFilterInfoList == null)
                mFilterInfoList = new ArrayList<>();
            return mFilterInfoList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView filterName;

            public ViewHolder(View itemView)
            {
                super(itemView);
                filterName = itemView.findViewById(R.id.record_navigator_filter_name);
            }
        }

        class OnKeyExitFilter implements OnKeyListener
        {
            @Override
            public boolean onKey(View itemView, int keyCode, KeyEvent event)
            {
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;

                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    Log.d(TAG, "onKey: close filter panel");
                    mWeakReference.get().openFilterPanel(false);
                    return true;
                }

                return false;
            }
        }

        class OnClickStartFilter implements OnClickListener
        {
            @Override
            public void onClick(View itemView)
            {
                RecordNavigatorActivity activity = mWeakReference.get();
                TextView filterName = itemView.findViewById(R.id.record_navigator_filter_name);
                Log.d(TAG, "onClick: filter name = " + filterName.getText());

                activity.setupMenuFocusable(true);
                activity.openFilterPanel(false);
                activity.startFilterRecord((String) filterName.getText());
            }
        }

        class OnFocusChangeColor implements OnFocusChangeListener
        {
            int position;

            OnFocusChangeColor(int position)
            {
                this.position = position;
            }

            @Override
            public void onFocusChange(View itemView, boolean hasFocus)
            {
                int colorAqua = mWeakReference.get().getColor(R.color.Aqua);
                CardView cardView = itemView.findViewById(R.id.record_navigator_filter_card);
                cardView.setCardBackgroundColor(hasFocus ? colorAqua : Color.TRANSPARENT);
            }
        }
    }
}

