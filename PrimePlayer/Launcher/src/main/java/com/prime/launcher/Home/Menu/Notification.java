package com.prime.launcher.Home.Menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;

import java.lang.ref.WeakReference;

public class Notification {
    private static final String TAG = "Notification";
    private static final boolean DEBUG = true;
    private static final int NOTIFI_COUNT_LOADER_ID = 1;
    private static final Uri NOTIFI_COUNT_URI = Uri.parse(
            "content://com.android.tv.notifications.NotificationContentProvider/" +
                    "notifications/count");
    private LoaderManager mLoaderMgr;
    private WeakReference<Context> mContRef = null;
    private int g_cur_notifi_count = 0;

    public Notification(Context context)
    {
        mContRef = new WeakReference<>(context);
        mLoaderMgr = LoaderManager.getInstance((HomeActivity) mContRef.get());
        init_loader();
    }

    public void init_loader()
    {
        if (mLoaderMgr == null)
            return;
        mLoaderMgr.initLoader(
                NOTIFI_COUNT_LOADER_ID,
                null,
                new NotifiLoaderCallback());
    }

    public void destroyLoader()
    {
        if (mLoaderMgr == null)
            return;
        mLoaderMgr.destroyLoader(NOTIFI_COUNT_LOADER_ID);
    }

    @SuppressLint("Range")
    public int get_notification_count(Cursor data)
    {
        String COLUMN_COUNT = "count";
        if (data != null && data.moveToFirst())
            return data.getInt(data.getColumnIndex(COLUMN_COUNT));
        return 0;
    }

    private void update_notification_count(Cursor data) {
        int updateNotifiCount = get_notification_count(data);
        Log.d(TAG, "update_notification_count: updateNotifiCount = " + updateNotifiCount);
        HomeMenuView homeMenuView = ((HomeActivity) mContRef.get()).findViewById(R.id.lo_menu_rltvl_home_menu);
        if (g_cur_notifi_count < updateNotifiCount)
            homeMenuView.update_notification_count(updateNotifiCount, true);
        else if (g_cur_notifi_count > updateNotifiCount)
            homeMenuView.update_notification_count(updateNotifiCount);
        g_cur_notifi_count = updateNotifiCount;
    }

    private class NotifiLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor>
    {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args)
        {
            if (DEBUG)
                Log.d(TAG, "onCreateLoader: id = " + id);
            return new CursorLoader(mContRef.get(), NOTIFI_COUNT_URI,
                    null, null, null, null);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data)
        {
            if (DEBUG)
                Log.d(TAG, "onLoadFinished: loader.getId  = " + loader.getId());
            refreshNotifi(data);
        }

        @Override
        public void onLoaderReset(@NonNull Loader loader)
        {
            if (DEBUG)
                Log.d(TAG, "onLoaderReset: loader.getId = " + loader.getId());
            refreshNotifi(null);
        }

        private void refreshNotifi(Cursor data)
        {
            ((HomeActivity) mContRef.get()).runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    update_notification_count(data);
                }
            });
        }
    }
}
