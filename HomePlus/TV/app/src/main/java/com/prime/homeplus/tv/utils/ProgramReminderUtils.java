package com.prime.homeplus.tv.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.prime.homeplus.tv.data.ProgramReminderData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProgramReminderUtils {
    private static final String TAG = "ProgramReminderUtils";

    private static final String PREF_NAME = "tv_reminders";
    private static final String KEY_LIST = "reminder_list";

    public static void saveReminders(Context context, ArrayList<ProgramReminderData> list) {
        Log.d(TAG, "saveReminders");
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder builder = new StringBuilder();

        for (ProgramReminderData reminder : list) {
            builder.append(reminder.toStorageString()).append("\n");
        }

        editor.putString(KEY_LIST, builder.toString().trim());
        editor.apply();
    }

    public static ArrayList<ProgramReminderData> loadReminders(Context context) {
        Log.d(TAG, "loadReminders");
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_LIST, "");

        ArrayList<ProgramReminderData> reminderList = new ArrayList<>();

        if (!saved.isEmpty()) {
            String[] lines = saved.split("\n");
            for (String line : lines) {
                ProgramReminderData reminder = ProgramReminderData.fromStorageString(line);
                if (reminder != null) {
                    reminderList.add(reminder);
                }
            }
        }

        return reminderList;
    }

    public static void deleteAllReminders(Context context) {
        Log.d(TAG, "deleteAllReminders");
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(KEY_LIST);
        editor.apply();
    }

    public static void addReminder(Context context, ProgramReminderData newReminder) {
        Log.d(TAG, "addReminder");
        ArrayList<ProgramReminderData> list = loadReminders(context);
        if (list == null) list = new ArrayList<>();

        boolean exists = false;
        for (ProgramReminderData r : list) {
            if (r.toStorageString().equals(newReminder.toStorageString())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            list.add(newReminder);
            saveReminders(context, list);
        }
    }

    public static void addOrReplaceReminderByProgramId(Context context, ProgramReminderData newReminder) {
        Log.d(TAG, "addOrReplaceReminderByProgramId");
        ArrayList<ProgramReminderData> list = loadReminders(context);
        if (list == null) {
            list = new ArrayList<>();
        }

        if (newReminder == null) {
            return;
        }

        int targetIndex = -1;
        long newProgramId = newReminder.getProgramId();

        for (int i = 0; i < list.size(); i++) {

            if (newProgramId == list.get(i).getProgramId()) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex != -1) {
            list.set(targetIndex, newReminder);
        } else {
            list.add(newReminder);
        }

        saveReminders(context, list);
    }

    public static void deleteReminder(Context context, ProgramReminderData targetReminder) {
        Log.d(TAG, "deleteReminder");
        ArrayList<ProgramReminderData> list = loadReminders(context);
        if (list == null) return;

        Iterator<ProgramReminderData> iterator = list.iterator();
        while (iterator.hasNext()) {
            ProgramReminderData r = iterator.next();
            if (r.toStorageString().equals(targetReminder.toStorageString())) {
                iterator.remove();
                break;
            }
        }

        saveReminders(context, list);
    }

    public static ProgramReminderData findFirstReminderConflictInSameMinute(Context context, ProgramReminderData newReminder) {
        Log.d(TAG, "findFirstReminderConflictInSameMinute");
        ArrayList<ProgramReminderData> list = loadReminders(context);

        if (list == null || newReminder == null) return null;

        long newStartMinute = newReminder.getStartTimeUtcMillis() / (60 * 1000);

        for (ProgramReminderData reminder : list) {
            long existingStartMinute = reminder.getStartTimeUtcMillis() / (60 * 1000);

            if (existingStartMinute == newStartMinute) {
                return reminder;
            }
        }

        return null;
    }

    public static boolean doesReminderExist(Context context, long programId) {
        Log.d(TAG, "doesReminderExist");
        ArrayList<ProgramReminderData> list = loadReminders(context);

        if (list == null || list.isEmpty()) {
            return false;
        }

        for (ProgramReminderData reminder : list) {
            if (reminder.getProgramId() == programId) {
                return true;
            }
        }

        return false;
    }
}
