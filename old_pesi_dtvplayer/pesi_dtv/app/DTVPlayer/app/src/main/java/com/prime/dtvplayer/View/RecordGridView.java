package com.prime.dtvplayer.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.internal.NavigationMenuItemView;
import com.prime.dtvplayer.Activity.RecordNavigatorActivity;
import com.prime.dtvplayer.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RecordGridView extends RecyclerView
{
    private static final String TAG = "RecordGridView";
    
    private static final int COLUMN_COUNT       = 5;
    private static final int ANIMATE_DURATION   = 300;
    private static final int Z_TOP              = 1;
    private static final int Z_BOTTOM           = 0;
    private static final float SCALE_UP         = 1.3f;
    private static final float SCALE_DOWN       = 1f;

    private WeakReference<RecordNavigatorActivity> mWeakReference = null;
    private List<String> mUsedPvrInfoList = new ArrayList<>();
    private List<String> mAllPvrDataList = new ArrayList<>();

    public RecordGridView(Context context)
    {
        this(context,null);
    }

    public RecordGridView(Context context, AttributeSet attrs)
    {
        this(context, attrs, -1);
    }

    public RecordGridView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setupRecord(RecordNavigatorActivity activity, List<String> pvrFileInfoList)
    {
        Log.d(TAG, "setupRecord: ");
        mWeakReference = new WeakReference<>(activity);
        mUsedPvrInfoList = pvrFileInfoList;
        mAllPvrDataList = pvrFileInfoList;

        setLayoutManager(new GridLayoutManager(activity, COLUMN_COUNT));
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        super.setAdapter(new RecordAdapter());
    }

    public void updateFilter()
    {
        Log.d(TAG, "updateFilter: ");
        mUsedPvrInfoList = mAllPvrDataList;
        getAdapter().notifyDataSetChanged();
    }

    public void updateFilter(String filterName)
    {
        Log.d(TAG, "updateFilter: filterName = " + filterName);
        List<String> newPvrFileInfoList = new ArrayList<>();

        for (String pvrInfo : mAllPvrDataList)
        {
            if (pvrInfo.contains(filterName))
            {
                newPvrFileInfoList.add(pvrInfo);
            }
        }

        mUsedPvrInfoList = newPvrFileInfoList;
        getAdapter().notifyDataSetChanged();
    }

    public int getItemCount()
    {
        Log.d(TAG, "getItemCount: ");
        return getAdapter().getItemCount();
    }

    @Nullable
    @Override
    public RecordAdapter getAdapter()
    {
        Log.d(TAG, "getAdapter: ");
        return (RecordAdapter) super.getAdapter();
    }

    public class RecordAdapter extends Adapter<RecordAdapter.ViewHolder>
    {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.record_navigate_banner_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            holder.bannerTxt.setText(mUsedPvrInfoList.get(position));
            holder.itemView.setOnFocusChangeListener(new ScaleUpBanner(position));

            if (position % COLUMN_COUNT == 0)
                holder.itemView.setOnKeyListener(new OpenDrawer());
            else
                holder.itemView.setOnKeyListener(new OpenDrawerByBack());
        }

        @Override
        public int getItemCount()
        {
            if (mUsedPvrInfoList == null)
                mUsedPvrInfoList = new ArrayList<>();
            return mUsedPvrInfoList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            ImageView bannerImg;
            TextView bannerTxt;

            public ViewHolder(View itemView)
            {
                super(itemView);
                bannerImg = itemView.findViewById(R.id.record_banner_img);
                bannerTxt = itemView.findViewById(R.id.record_banner_title);
            }
        }

        class OpenDrawer implements OnKeyListener
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BACK)
                {
                    Log.d(TAG, "onKey: left or back");
                    RecordNavigatorActivity activity = mWeakReference.get();
                    DrawerLayout drawerLayout        = activity.findViewById(R.id.record_navigate_drawer);
                    NavigationMenuItemView itemView  = activity.getMenuItem();

                    drawerLayout.openDrawer(GravityCompat.START);
                    itemView.requestFocus();
                    return true;
                }
                return false;
            }
        }

        class OpenDrawerByBack implements OnKeyListener
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    Log.d(TAG, "onKey: back");
                    RecordNavigatorActivity activity = mWeakReference.get();
                    DrawerLayout drawerLayout       = activity.findViewById(R.id.record_navigate_drawer);
                    NavigationMenuItemView itemView = activity.getMenuItem();

                    drawerLayout.openDrawer(GravityCompat.START);
                    itemView.requestFocus();
                    return true;
                }
                return false;
            }
        }

        class ScaleUpBanner implements OnFocusChangeListener
        {
            int position;

            ScaleUpBanner(int position)
            {
                this.position = position;
            }

            @Override
            public void onFocusChange(View itemView, boolean hasFocus)
            {
                Log.d(TAG, "onFocusChange: scale up banner");
                TextView bannerTxt = itemView.findViewById(R.id.record_banner_title);
                bannerTxt.setBackgroundResource(hasFocus ? R.color.Aqua : R.color.DarkTransparent);

                float offset = (SCALE_UP * itemView.getWidth() - itemView.getWidth()) / 2;

                itemView.setTranslationZ(hasFocus? Z_TOP : Z_BOTTOM);
                if (position % COLUMN_COUNT == 0)
                    itemView.animate()
                            .translationX(hasFocus ? offset : 0)
                            .scaleX(hasFocus ? SCALE_UP : SCALE_DOWN)
                            .scaleY(hasFocus ? SCALE_UP : SCALE_DOWN)
                            .setDuration(ANIMATE_DURATION);
                else if ((position + 1) % COLUMN_COUNT == 0)
                    itemView.animate()
                            .translationX(hasFocus ? -offset : 0)
                            .scaleX(hasFocus ? SCALE_UP : SCALE_DOWN)
                            .scaleY(hasFocus ? SCALE_UP : SCALE_DOWN)
                            .setDuration(ANIMATE_DURATION);
                else
                    itemView.animate()
                            .scaleX(hasFocus ? SCALE_UP : SCALE_DOWN)
                            .scaleY(hasFocus ? SCALE_UP : SCALE_DOWN)
                            .setDuration(ANIMATE_DURATION);
            }
        }
    }
}

