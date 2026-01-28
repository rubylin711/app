package com.prime.homeplus.tv.utils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.Program;

import com.prime.homeplus.tv.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProgramUtils {
    private static final String TAG = "ProgramUtils";

    public static boolean isNowPlaying(Program program) {
        if (program == null) {
            return false;
        }

        return TimeUtils.isInTimeRange(program.getStartTimeUtcMillis(), program.getEndTimeUtcMillis());
    }

    public static Program getCurrentProgram(Context context, long channelId) {
        Log.d(TAG, "getCurrentProgram ChannelId:" + channelId);
        long now = System.currentTimeMillis();
        String selection = TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " <= ? AND " +
                TvContractCompat.Programs.COLUMN_END_TIME_UTC_MILLIS + " >= ?";
        String[] selectionArgs = { String.valueOf(now), String.valueOf(now) };

        List<Program> programs = queryPrograms(context, channelId, selection, selectionArgs, null);
        if (programs == null || programs.isEmpty()) {
            Log.w(TAG, "No current program found for channelId: " + channelId);
            return null;
        }
        removeOverlappingPrograms(programs);
        return programs.get(0);
    }

    public static Program getNextProgram(Context context, long channelId) {
        Log.d(TAG, "getNextProgram ChannelId:" + channelId);
        long now = System.currentTimeMillis();
        String selection = TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " > ?";
        String[] selectionArgs = { String.valueOf(now) };
        String sortOrder = TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " ASC";

        List<Program> programs = queryPrograms(context, channelId, selection, selectionArgs, sortOrder);
        if (programs == null || programs.isEmpty()) {
            Log.w(TAG, "No next program found for channelId: " + channelId);
            return null;
        }
        removeOverlappingPrograms(programs);
        return programs.get(0);
    }

    public static List<Program> getRemainingProgramsForDate(Context context, long channelId, long fromTimestampMillis) {
        Log.d(TAG, "getRemainingProgramsForDate ChannelId:" + channelId + ", FromTimestamp:" + fromTimestampMillis);

        // Create a Calendar instance in the local time zone and set it to fromTimestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(fromTimestampMillis);

        // Set the time to the end of the current day: 23:59:59.999
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endOfDayTimestamp = calendar.getTimeInMillis();

        // Query condition: start_time <= endOfDay AND end_time >= fromTimestamp (i.e., now)
        String selection = TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " <= ? AND " +
                TvContractCompat.Programs.COLUMN_END_TIME_UTC_MILLIS + " >= ?";

        String[] selectionArgs = {
                String.valueOf(endOfDayTimestamp),
                String.valueOf(fromTimestampMillis)
        };

        // Query programs from "fromTimestamp" to end of day
        List<Program> programList = queryPrograms(context, channelId, selection, selectionArgs,
                TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " ASC");
        removeOverlappingPrograms(programList);
        return programList;
    }

    public static List<Program> getUpcomingProgramsForDate(Context context, long channelId, long fromTimestampMillis) {
        Log.d(TAG, "getUpcomingProgramsForDate ChannelId:" + channelId + ", FromTimestamp:" + fromTimestampMillis);

        // Create a Calendar instance in the local time zone and set it to fromTimestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(fromTimestampMillis);

        // Set the time to the end of the current day: 23:59:59.999
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endOfDayTimestamp = calendar.getTimeInMillis();

        // Selection criteria for the query
        String selection = TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " >= ? AND " +
                TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " <= ?";
        String[] selectionArgs = {
                String.valueOf(fromTimestampMillis),
                String.valueOf(endOfDayTimestamp)
        };

        // Query programs from "fromTimestamp" to end of day
        List<Program> programList = queryPrograms(context, channelId, selection, selectionArgs,
                TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " ASC");
        removeOverlappingPrograms(programList);
        return programList;

    }

    public static Program getProgramForDate(Context context, long channelId, long fromTimestampMillis) {
        Log.d(TAG, "getProgramForDate ChannelId:" + channelId + ", FromTimestamp:" + fromTimestampMillis);

        Program program = null;
        // Selection criteria for the query
        String selection = TvContractCompat.Programs.COLUMN_CHANNEL_ID + " = ? AND " +
                TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " <= ? AND " +
                TvContractCompat.Programs.COLUMN_END_TIME_UTC_MILLIS + " > ?";

        String[] selectionArgs = {
                String.valueOf(channelId),
                String.valueOf(fromTimestampMillis),
                String.valueOf(fromTimestampMillis)
        };

        String sortOrder = TvContractCompat.Programs.COLUMN_START_TIME_UTC_MILLIS + " ASC LIMIT 1";

        try {
            try (Cursor cursor = context.getContentResolver().query(
                    TvContractCompat.Programs.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    sortOrder)) {

                if (cursor != null && cursor.moveToFirst()) {
                    program = Program.fromCursor(cursor);
                }
            }
        } catch (SecurityException se) {
            Log.e(TAG, "getProgramForDate: SecurityException", se);
        } catch (Exception e) {
            Log.e(TAG, "getProgramForDate: Exception", e);
        }

        return program;
    }

    private static List<Program> queryPrograms(Context context, long channelId,
                                               String selection, String[] selectionArgs,
                                               String sortOrder) {
        List<Program> programs = new ArrayList<>();

        if (context == null) {
            return programs;
        }
        if (channelId <= 0) {
            return programs;
        }

        String baseSelection = TvContractCompat.Programs.COLUMN_CHANNEL_ID + " = ?";
        String[] baseArgs = new String[]{String.valueOf(channelId)};

        String finalSelection = baseSelection;
        String[] finalSelectionArgs = baseArgs;

        if (selection != null && !selection.isEmpty()) {
            finalSelection += " AND " + selection;

            if (selectionArgs != null && selectionArgs.length > 0) {
                finalSelectionArgs = new String[baseArgs.length + selectionArgs.length];
                System.arraycopy(baseArgs, 0, finalSelectionArgs, 0, baseArgs.length);
                System.arraycopy(selectionArgs, 0, finalSelectionArgs, baseArgs.length, selectionArgs.length);
            }
        }

        try {
            try (Cursor cursor = context.getContentResolver().query(
                    TvContractCompat.Programs.CONTENT_URI,
                    null,
                    finalSelection,
                    finalSelectionArgs,
                    sortOrder)) {

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        programs.add(Program.fromCursor(cursor));
                    } while (cursor.moveToNext());
                }
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return programs;
    }

    public static Program createEmptyProgram(Context context) {
        return new Program.Builder()
                .setTitle(context.getString(R.string.no_program_info))
                .setChannelId(-1)
                .build();
    }

    public static boolean isEmptyProgram(Context context, Program program) {
        String emptyProgramTitle = context.getString(R.string.no_program_info);
        return program.getChannelId() == -1 && emptyProgramTitle.equals(program.getTitle());
    }

    private static void removeOverlappingPrograms(List<Program> pgList ) {
        if (pgList == null || pgList.size() <= 1) return;

        for (int i = 0; i < pgList.size() - 1; ) {
            Program current = pgList.get(i);
            Program next = pgList.get(i + 1);
            long currentEnd = current.getEndTimeUtcMillis();
            long nextStart = next.getStartTimeUtcMillis();

            if (currentEnd > nextStart) {
                // Overlap detected, keep the entry with the larger _id
                if (current.getId() < next.getId()) {
                    // Remove current (older entry)
                    pgList.remove(i);
                    // Do not increment i because next has been shifted to index i
                } else {
                    // Remove next (older entry)
                    pgList.remove(i + 1);
                    // No need to change i, as we will compare current with the new next entry in the next iteration
                }
            } else {
                // No overlap, move to the next pair for comparison
                i++;
            }
        }
    }

    public static Program getProgramByEventId(Context context, long channelId, int eventId) {
        Log.d(TAG, "getProgramByEventId: channelId = " + channelId + " eventId = " + eventId);

        Program program = null;
        // Selection criteria for the query
        String selection = TvContractCompat.Programs.COLUMN_CHANNEL_ID + " = ? AND " +
                TvContractCompat.Programs.COLUMN_EVENT_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(channelId),
                String.valueOf(eventId)
        };

        String sortOrder = "1 LIMIT 1"; // get only one

        try {
            try (Cursor cursor = context.getContentResolver().query(
                    TvContractCompat.Programs.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    sortOrder)) {

                if (cursor != null && cursor.moveToFirst()) {
                    program = Program.fromCursor(cursor);
                } else {
                    Log.w(TAG, "getProgramByEventId: program not found");
                }
            }
        } catch (SecurityException se) {
            Log.e(TAG, "getProgramByEventId: SecurityException", se);
        } catch (Exception e) {
            Log.e(TAG, "getProgramByEventId: Exception", e);
        }

        return program;
    }
}
