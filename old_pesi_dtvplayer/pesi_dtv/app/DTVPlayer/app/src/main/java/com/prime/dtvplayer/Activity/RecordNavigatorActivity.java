package com.prime.dtvplayer.Activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.FilterListView;
import com.prime.dtvplayer.View.RecordGridView;

import java.util.ArrayList;
import java.util.List;

public class RecordNavigatorActivity extends AppCompatActivity
{
    private static final String TAG = "RecordNavigatorActivity";

    private static final int ANIMATE_DURATION = 300;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private RecordGridView mRecordGridView;
    private FilterListView mFilterListView;
    private NavigationMenuItemView mMenuItemView;
    private List<NavigationMenuItemView> mNavMenuList = new ArrayList<>();
    private List<String> mPvrFileInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_navigator);

        mDrawerLayout = findViewById(R.id.record_navigate_drawer);
        mNavigationView = findViewById(R.id.record_navigate_navigation);
        mRecordGridView = findViewById(R.id.record_navigator_grid);
        mFilterListView = findViewById(R.id.record_navigate_filter);

        mRecordGridView.setupRecord(this, mPvrFileInfoList);
        mFilterListView.setupFilter(this, mPvrFileInfoList);

        setupMenu();
    }

    @Override
    public void onBackPressed()
    {
        if (mRecordGridView.hasFocus())
        {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // close drawer(menu) without animation on activity pause
        // try not to block the view under us
        mDrawerLayout.closeDrawer(GravityCompat.START, false);
    }

    private void setupMenu()
    {
        float slideX = getResources().getDimension(R.dimen.record_navigator_menu_width);
        mRecordGridView.setTranslationX(slideX);
        mDrawerLayout.openDrawer(GravityCompat.START);

        // set onKeyListener & onFocusChangeListener
        mDrawerLayout.addOnLayoutChangeListener(new OnLayoutSetupListener());

        // set the color that cover the main content when drawer(menu) is open to transparent
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        // set drawer(menu) listener
        mDrawerLayout.addDrawerListener(new DrawerListener());

        // set menu item select(click) listener
        mNavigationView.setNavigationItemSelectedListener(new OnClickMenu());

        openFilterPanel(false);
    }

    public NavigationMenuItemView getMenuItem()
    {
        return mMenuItemView;
    }

    public void startFilterRecord(String filterName)
    {
        Log.d(TAG, "startFilterRecord: filterName = " + filterName);
        if (filterName != null)
        {
            mMenuItemView.setTag(filterName);
            mRecordGridView.updateFilter(filterName);
        }
    }

    public void showAllRecord()
    {
        Log.d(TAG, "showAllRecord: ");
        mRecordGridView.updateFilter();
    }

    public void openFilterPanel(boolean open)
    {
        float offset = getResources().getDimension(R.dimen.record_navigator_menu_width);
        int filterWidth = (int) getResources().getDimension(R.dimen.record_navigator_filter_width);

        mFilterListView.setMinimumWidth(filterWidth);
        mFilterListView.animate()
                .setListener(new AnimatorListener(open))
                .translationX(open ? offset : -offset)
                .setDuration(ANIMATE_DURATION);

        if (open)
            mFilterListView.requestFocus();
        else if (mMenuItemView != null)
            mMenuItemView.requestFocus();
        else
            mDrawerLayout.requestFocus();

        if (mMenuItemView != null)
            mMenuItemView.setSelected(open);
    }

    public void setupMenuFocusable(boolean focusable)
    {
        Log.d(TAG, "setupMenuFocusable: focusable = " + focusable);
        for (NavigationMenuItemView itemView : mNavMenuList)
            itemView.setFocusable(focusable);
    }

    private class DrawerListener implements DrawerLayout.DrawerListener
    {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset)
        {
            float slideX = drawerView.getWidth() * slideOffset;
            mRecordGridView.setTranslationX(slideX);
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    }

    private class OnLayoutSetupListener implements View.OnLayoutChangeListener
    {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
        {
            if (mMenuItemView == null)
            {
                Log.d(TAG, "onLayoutChange: setup menu item listener");
                Menu menu = mNavigationView.getMenu();

                for (int i = 0; i < menu.size(); i++)
                {
                    int itemId = menu.getItem(i).getItemId();
                    NavigationMenuItemView itemView = mNavigationView.findViewById(itemId);

                    if (itemView != null)
                    {
                        itemView.setOnFocusChangeListener(new OnFocusUpdateRecord());
                        if (itemId == R.id.all_records)
                            itemView.setOnKeyListener(new OnKeyCloseDrawer());
                        else if (itemId == R.id.filter_records)
                            itemView.setOnKeyListener(new OnKeyFilterRecord());
                        mNavMenuList.add(itemView);
                    }
                }

                mDrawerLayout.requestFocus();
            }
        }
    }

    private class OnFocusUpdateRecord implements View.OnFocusChangeListener
    {
        @SuppressLint("RestrictedApi")
        @Override
        public void onFocusChange(View view, boolean hasFocus)
        {
            NavigationMenuItemView itemView;
            if (NavigationMenuItemView.class.equals(view.getClass()))
            {
                // set item checked for the focus state of text and icon inside
                itemView = (NavigationMenuItemView) view;
                if (itemView == null)
                    return;

                if (hasFocus)
                {
                    if (mMenuItemView != null)
                    {
                        int itemId = itemView.getItemData().getItemId();
                        int lastId = mMenuItemView.getItemData().getItemId();
                        String filterName = (String) itemView.getTag();

                        if (lastId != itemId)
                        {
                            Log.d(TAG, "onFocusChange: filterName = " + filterName);
                            if (itemId == R.id.all_records)         showAllRecord();
                            else if (itemId == R.id.filter_records) startFilterRecord(filterName);
                        }
                    }

                    mMenuItemView = itemView;
                }
            }
        }
    }

    private class OnKeyCloseDrawer implements View.OnKeyListener
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (event.getAction() != KeyEvent.ACTION_DOWN)
                return false;

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
            {
                if (mRecordGridView == null || mRecordGridView.getItemCount() == 0)
                    return true;

                Log.d(TAG, "onKey: closeDrawer");
                mRecordGridView.requestFocus();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            return false;
        }
    }

    private class OnKeyFilterRecord implements View.OnKeyListener
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (event.getAction() != KeyEvent.ACTION_DOWN)
                return false;

            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
            {
                if (mFilterListView.getItemCount() != 0)
                {
                    openFilterPanel(true);
                    setupMenuFocusable(false);
                }
                return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
            {
                if (mPvrFileInfoList == null || mPvrFileInfoList.size() == 0)
                    return true;

                mRecordGridView.requestFocus();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            return false;
        }
    }

    private class OnClickMenu implements NavigationView.OnNavigationItemSelectedListener
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            int itemId = item.getItemId();

            if (itemId == R.id.all_records)
            {
                Toast.makeText(RecordNavigatorActivity.this, "all_program", Toast.LENGTH_LONG).show();
            }
            else if (itemId == R.id.filter_records)
            {
                Toast.makeText(RecordNavigatorActivity.this, "filter_program", Toast.LENGTH_LONG).show();
            }

            return false;
        }
    }

    private class AnimatorListener implements Animator.AnimatorListener
    {
        boolean start;

        AnimatorListener(boolean start)
        {
            this.start = start;
        }

        @Override
        public void onAnimationStart(Animator animation)
        {
            mFilterListView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animation)
        {
            mFilterListView.setVisibility(start ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}