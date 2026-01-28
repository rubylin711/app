package com.prime.tvinputframework;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class EpgSyncJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        // 模擬從 DVB SI/PSI 表（例如 EIT）解析 EPG
        ContentValues programValues = new ContentValues();
        programValues.put(TvContract.Programs.COLUMN_CHANNEL_ID, 1L);
        programValues.put(TvContract.Programs.COLUMN_TITLE, "Sample Program");
        programValues.put(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, System.currentTimeMillis());
        programValues.put(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, System.currentTimeMillis() + 3600000);

        getContentResolver().insert(TvContract.Programs.CONTENT_URI, programValues);
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
