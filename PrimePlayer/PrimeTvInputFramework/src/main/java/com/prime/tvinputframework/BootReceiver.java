package com.prime.tvinputframework;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.util.Log;

import com.prime.datastructure.sysdata.PvrRecFileInfo;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    public BootReceiver() {

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "tvinputframework onReceive action = " + intent.getAction());
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, PrimeTvInputService.class);
            context.startService(serviceIntent);

            // update recorded programs status
            updateRecordingRecordedProgramsToFailure(context);
        }
    }

    private static void updateRecordingRecordedProgramsToFailure(Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        String where = TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1 + " = ?";
        String[] selectionArgs = {
                String.valueOf(PvrRecFileInfo.RECORD_STATUS_RECORDING)
        };

        ContentValues values = new ContentValues();
        values.put(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1, PvrRecFileInfo.RECORD_STATUS_FAILED);

        contentResolver.update(TvContract.RecordedPrograms.CONTENT_URI, values, where, selectionArgs);
    }
}
