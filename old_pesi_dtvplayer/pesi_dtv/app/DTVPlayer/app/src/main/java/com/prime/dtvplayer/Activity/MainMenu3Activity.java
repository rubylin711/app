package com.prime.dtvplayer.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;


import com.prime.dtvplayer.R;

public class MainMenu3Activity extends AppCompatActivity {
    private static final String TAG = "MainMenu3Activity";
    private static final int TIME_INTERVAL_BACK = 2000; // milliseconds, time between two back pressed

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private Toast mExitToast;
    private long mBackPressedTime = 0;

    @SuppressLint({"RestrictedApi", "ShowToast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu3);

        mNavigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.main_menu3_layout);

        mExitToast = Toast.makeText(this, getString(R.string.STR_PRESS_BACK_AGAIN), Toast.LENGTH_SHORT);

        setupMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // open drawer(menu) on activity resume
        // no animation if we open drawer directly in onCreate(), onStart(), onResume() without delay
        // so we set pre draw listener to open drawer(menu)
        ViewTreeObserver viewTreeObserver = mDrawerLayout.getViewTreeObserver();
        if (viewTreeObserver != null) {
            viewTreeObserver.addOnPreDrawListener(new PreDrawShowMenuListener(mDrawerLayout));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // close drawer(menu) without animation on activity pause
        // try not to block the view under us
        mDrawerLayout.closeDrawer(GravityCompat.START, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // press back again to exit
        long currentTime = System.currentTimeMillis();
        if (mBackPressedTime + TIME_INTERVAL_BACK < currentTime) {
            mExitToast.show();
            mBackPressedTime = currentTime;
        }
        else {
            mExitToast.cancel();

            // tell view to exit
            // must set before super.onBackPressed();
            setResult(ViewActivity.FINISH_APK, getIntent());
            super.onBackPressed();
        }
    }

    private void setupMenu() {
        // set the color that cover the main content when drawer(menu) is open to transparent
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        // set drawer(menu) listener
        mDrawerLayout.addDrawerListener(new MenuListener(mDrawerLayout, mNavigationView));

        // set menu item select(click) listener
        mNavigationView.setNavigationItemSelectedListener(new MenuItemClickListener(this));
    }

    private static class MenuListener implements DrawerLayout.DrawerListener {
        private final DrawerLayout drawerLayout;
        private final NavigationView navigationView;

        public MenuListener(DrawerLayout drawerLayout, NavigationView navigationView) {
            this.drawerLayout = drawerLayout;
            this.navigationView = navigationView;
        }

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            // before drawer fully opened, set menu item focus listener
            if (newState == DrawerLayout.STATE_SETTLING && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                for (int i = 0 ; i < navigationView.getMenu().size() ; i++) {
                    int itemId = navigationView.getMenu().getItem(i).getItemId();
                    NavigationMenuItemView navMenuItemView = navigationView.findViewById(itemId);
                    navMenuItemView.setOnFocusChangeListener(new MenuItemFocusChangedListener());
                }
            }
        }
    }

    private static class MenuItemClickListener implements NavigationView.OnNavigationItemSelectedListener {
        private final Context context;

        public MenuItemClickListener(Context context) {
            this.context = context;
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int itemId = item.getItemId();
            Intent intent = null;
            if (itemId == R.id.nav_live_channel) {
                intent = new Intent(context, ViewActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else if (itemId == R.id.nav_channel_search) {
                intent = new Intent(context, ChannelSearchActivity.class);
            } else if (itemId == R.id.nav_tv_channel) {
                intent = new Intent(context, TVManagerActivity.class);
                intent.putExtra("service",1);
            } else if (itemId == R.id.nav_radio_channel) {
                intent = new Intent(context, TVManagerActivity.class);
                intent.putExtra("service",0);
            } else if (itemId == R.id.nav_epg) {
                intent = new Intent(context, DimensionEPG.class);
            } else if (itemId == R.id.nav_reset_default) {
                intent = new Intent(context, ResetDefaultActivity.class);
            }

            if (intent != null) {
                context.startActivity(intent);
            }

            return false;
        }
    }

    private static class MenuItemFocusChangedListener implements View.OnFocusChangeListener {
        @SuppressLint("RestrictedApi")
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (NavigationMenuItemView.class.equals(view.getClass())) {
                // set item checked for the focus state of text and icon inside
                ((NavigationMenuItemView) view).setChecked(hasFocus);
            }
        }
    }

    private static class PreDrawShowMenuListener implements ViewTreeObserver.OnPreDrawListener {
        private final DrawerLayout drawerLayout;

        private PreDrawShowMenuListener(DrawerLayout drawerLayout) {
            this.drawerLayout= drawerLayout;
        }

        @Override
        public boolean onPreDraw() {
            if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                Log.d(TAG, "onPreDraw: openDrawer");
                drawerLayout.openDrawer(GravityCompat.START);

                ViewTreeObserver viewTreeObserver = drawerLayout.getViewTreeObserver();
                if (viewTreeObserver != null) {
                    viewTreeObserver.removeOnPreDrawListener(this);
                }
            }

            return true;
        }
    }

}