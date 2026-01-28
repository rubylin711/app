package com.prime.homeplus.tv.ui.component;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.adapter.ProgramReminderListAdapter;
import com.prime.homeplus.tv.data.ProgramReminderData;
import com.prime.homeplus.tv.manager.ListPaginationManager;
import com.prime.homeplus.tv.utils.ProgramReminderUtils;
import com.prime.homeplus.tv.ui.fragment.ParentalPinDialogFragment;
import com.prime.homeplus.tv.ui.fragment.ProgramDeleteDialogFragment;
import com.prime.homeplus.tv.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Locale;

public class ProgramReminderSettingPopup {
    private static final String TAG = "ReminderSettingPopup";
    private Fragment hostFragment;
    private Context context;
    private PopupWindow popupWindow;
    private View parentView, popupView;
    private OnRefreshEpgReminderListener refreshReminderListener;
    private boolean needRefreshEpgReminder = false;

    // Menu
    private ConstraintLayout clReminderMenu;
    private Button btnReminderMenuAddReminder, btnReminderMenuGoToList, btnReminderMenuCancel;

    // Conflict
    private ConstraintLayout clReminderConflict;
    private TextView tvReminderConflictNewChannelNumber, tvReminderConflictNewChannelName,
            tvReminderConflictNewProgramName, tvReminderConflictNewTime,
            tvReminderConflictExistingChannelNumber, tvReminderConflictExistingChannelName,
            tvReminderConflictExistingProgramName, tvReminderConflictExistingTime;
    private Button btnReminderConflictSave, btnReminderConflictCancel;


    // Main List
    private ConstraintLayout clReminderMain;
    private RecyclerView rvProgramReminderList;
    private TextView tvReminderNoWatchList, tvReminderBlueKey, tvReminderRedKey;
    private ListPaginationManager programReminderListPaginationManager;
    private ProgramReminderListAdapter programReminderListAdapter;
    private final int PROGRAM_REMINDER_ITEMS_PER_PAGE = 10;

    private ProgramReminderData newProgramReminderData, deleteProgramReminderData, conflictProgramReminderData;
    private final PrimeDtvServiceInterface primeDtv;

    public interface OnRefreshEpgReminderListener {
        void onRefreshEpgReminder();
    }

    public void setOnRefreshEpgReminderListener(OnRefreshEpgReminderListener listener) {
        this.refreshReminderListener = listener;
    }

    public ProgramReminderSettingPopup(Fragment fragment, Context context, View parentView) {
        hostFragment = fragment;
        this.context = fragment.getContext();
        this.parentView = parentView;
        primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        initPopup();
    }

    public void setReminderInfo(Program pg, Channel ch) {
        newProgramReminderData = new ProgramReminderData(pg, ch);
    }

    private void initPopup() {
        LayoutInflater inflater = LayoutInflater.from(context);
        popupView = inflater.inflate(R.layout.popup_reminder_setting, null);

        initMenu();
        initConflict();
        initMainList();

        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        popupWindow.setOnDismissListener(() -> {
            Log.d(TAG, "setOnDismissListener");
            if (needRefreshEpgReminder) {
                if (refreshReminderListener != null) {
                    refreshReminderListener.onRefreshEpgReminder();
                }
            }

            needRefreshEpgReminder = false;
            newProgramReminderData = null;
            deleteProgramReminderData = null;
            conflictProgramReminderData = null;
        });
    }

    private void initMenu() {
        clReminderMenu = popupView.findViewById(R.id.clReminderMenu);
        btnReminderMenuAddReminder = popupView.findViewById(R.id.btnReminderMenuAddReminder);
        btnReminderMenuGoToList = popupView.findViewById(R.id.btnReminderMenuGoToList);
        btnReminderMenuCancel = popupView.findViewById(R.id.btnReminderMenuCancel);

        btnReminderMenuAddReminder.setOnClickListener((v) -> {
            if (conflictProgramReminderData == null) {
                // No existing reminder conflict, add new reminder
                ProgramReminderUtils.addOrReplaceReminderByProgramId(context, newProgramReminderData);
                needRefreshEpgReminder = true;
                dismiss();
            } else if (conflictProgramReminderData.getProgramId() == newProgramReminderData.getProgramId()) {
                // Reminder for the same program already exists, cancel it
                ProgramReminderUtils.deleteReminder(context, newProgramReminderData);
                needRefreshEpgReminder = true;
                dismiss();
            } else {
                // Conflict with another program's reminder, show conflict dialog
                showConflict();
            }
        });

        btnReminderMenuGoToList.setOnClickListener((v) -> {
            tvReminderBlueKey.setVisibility(View.VISIBLE);
            programReminderListAdapter.lockProgramName();
            showMainList();
        });

        btnReminderMenuCancel.setOnClickListener((v) -> {
            dismiss();
        });
    }

    private void initConflict() {
        clReminderConflict = popupView.findViewById(R.id.clReminderConflict);

        tvReminderConflictNewChannelNumber = popupView.findViewById(R.id.tvReminderConflictNewChannelNumber);
        tvReminderConflictNewChannelName = popupView.findViewById(R.id.tvReminderConflictNewChannelName);
        tvReminderConflictNewProgramName = popupView.findViewById(R.id.tvReminderConflictNewProgramName);
        tvReminderConflictNewTime = popupView.findViewById(R.id.tvReminderConflictNewTime);
        tvReminderConflictExistingChannelNumber = popupView.findViewById(R.id.tvReminderConflictExistingChannelNumber);
        tvReminderConflictExistingChannelName = popupView.findViewById(R.id.tvReminderConflictExistingChannelName);
        tvReminderConflictExistingProgramName = popupView.findViewById(R.id.tvReminderConflictExistingProgramName);
        tvReminderConflictExistingTime = popupView.findViewById(R.id.tvReminderConflictExistingTime);

        btnReminderConflictSave = popupView.findViewById(R.id.btnReminderConflictSave);
        btnReminderConflictCancel = popupView.findViewById(R.id.btnReminderConflictCancel);

        btnReminderConflictSave.setOnClickListener((v) -> {
            ProgramReminderUtils.deleteReminder(context, conflictProgramReminderData);
            ProgramReminderUtils.addOrReplaceReminderByProgramId(context, newProgramReminderData);
            needRefreshEpgReminder = true;
            dismiss();
        });

        btnReminderConflictCancel.setOnClickListener((v) -> {
            dismiss();
        });
    }

    private void initMainList() {
        clReminderMain = popupView.findViewById(R.id.clReminderMain);

        rvProgramReminderList = popupView.findViewById(R.id.rvProgramReminderList);

        tvReminderNoWatchList = popupView.findViewById(R.id.tvReminderNoWatchList);
        tvReminderBlueKey = popupView.findViewById(R.id.tvReminderBlueKey);
        tvReminderRedKey = popupView.findViewById(R.id.tvReminderRedKey);

        initMainListData();
    }

    public void showMenu() {
        Log.d(TAG, "showMenu");
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        if (newProgramReminderData == null) {
            return;
        }

        conflictProgramReminderData = ProgramReminderUtils.findFirstReminderConflictInSameMinute(context, newProgramReminderData);
        if ((conflictProgramReminderData != null) &&
                (conflictProgramReminderData.getProgramId() == newProgramReminderData.getProgramId())) {
            btnReminderMenuAddReminder.setText(context.getString(R.string.reminder_cancel));
        } else {
            btnReminderMenuAddReminder.setText(context.getString(R.string.reminder_set));
        }

        clReminderMenu.setVisibility(View.VISIBLE);
        clReminderConflict.setVisibility(View.GONE);
        clReminderMain.setVisibility(View.GONE);
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        btnReminderMenuAddReminder.requestFocus();
    }

    public void showConflict() {
        Log.d(TAG, "showConflict");
        if (conflictProgramReminderData == null) {
            return;
        }

        tvReminderConflictNewChannelNumber.setText(newProgramReminderData.getChannelNumber());
        tvReminderConflictNewChannelName.setText(newProgramReminderData.getChannelName());
        tvReminderConflictNewProgramName.setText(newProgramReminderData.getProgramName());
        String new_start_end_time = context.getString(R.string.reminder_schedule_format,
                TimeUtils.formatTimestampWithHourMinute(newProgramReminderData.getStartTimeUtcMillis()),
                TimeUtils.formatTimestampWithHourMinute(newProgramReminderData.getEndTimeUtcMillis()));
        tvReminderConflictNewTime.setText(new_start_end_time);

        tvReminderConflictExistingChannelNumber.setText(conflictProgramReminderData.getChannelNumber());
        tvReminderConflictExistingChannelName.setText(conflictProgramReminderData.getChannelName());
        tvReminderConflictExistingProgramName.setText(conflictProgramReminderData.getProgramName());
        String conflict_start_end_time = context.getString(R.string.reminder_schedule_format,
                TimeUtils.formatTimestampWithHourMinute(conflictProgramReminderData.getStartTimeUtcMillis()),
                TimeUtils.formatTimestampWithHourMinute(conflictProgramReminderData.getEndTimeUtcMillis()));
        tvReminderConflictExistingTime.setText(conflict_start_end_time);

        clReminderMenu.setVisibility(View.GONE);
        clReminderConflict.setVisibility(View.VISIBLE);
        clReminderMain.setVisibility(View.GONE);
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        btnReminderConflictSave.requestFocus();
    }


    public void showMainList() {
        Log.d(TAG, "showMainList");
        updateMainListData();

        clReminderMenu.setVisibility(View.GONE);
        clReminderConflict.setVisibility(View.GONE);
        clReminderMain.setVisibility(View.VISIBLE);
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
    }

    public void initMainListData() {
        rvProgramReminderList.setLayoutManager(new LinearLayoutManager(context));
        programReminderListPaginationManager = new ListPaginationManager<>(new ArrayList<>(), PROGRAM_REMINDER_ITEMS_PER_PAGE);
        programReminderListAdapter = new ProgramReminderListAdapter(rvProgramReminderList, programReminderListPaginationManager.getCurrentPageData());
        programReminderListAdapter.setOnRecyclerViewInteractionListener(new ProgramReminderListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onPageUp() {
                Log.d(TAG, "programReminderListAdapter onPageUp");
                programReminderListPaginationManager.previousPage();
                programReminderListAdapter.updateList(programReminderListPaginationManager.getCurrentPageData(), true);
            }

            @Override
            public void onPageDown() {
                Log.d(TAG, "programReminderListAdapter onPageDown");
                programReminderListPaginationManager.nextPage();
                programReminderListAdapter.updateList(programReminderListPaginationManager.getCurrentPageData(), false);
            }

            @Override
            public void onDeleteReminder(ProgramReminderData programReminderData) {
                Log.d(TAG, "programReminderListAdapter onDeleteReminder");
                if (programReminderData != null) {
                    showProgramReminderProgramDeleteDialog(programReminderData);
                }
            }

            @Override
            public void onShowParentalPinDialog( ) {
                Log.d(TAG, "programReminderListAdapter onShowParentalPinDialog");
                showProgramReminderParentalPinDialog();
            }
        });

        rvProgramReminderList.setAdapter(programReminderListAdapter);
    }

    public void updateMainListData() {
        ArrayList<ProgramReminderData> allReminders = ProgramReminderUtils.loadReminders(context);
        if (allReminders.isEmpty()) {
            tvReminderNoWatchList.setVisibility(View.VISIBLE);
        } else {
            tvReminderNoWatchList.setVisibility(View.GONE);
        }
        programReminderListPaginationManager.updateData(allReminders);
        programReminderListAdapter.updateList(programReminderListPaginationManager.getCurrentPageData(), false);
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private static final String PROGRAM_REMINDER_PROGRAM_DELETE_DIALOG_TAG = "ProgramReminderProgramDeleteDialog";
    public void showProgramReminderProgramDeleteDialog(ProgramReminderData programReminderData) {
        deleteProgramReminderData = programReminderData;

        String date = TimeUtils.formatTimestampToDateYyMmDd(deleteProgramReminderData.getStartTimeUtcMillis());
        String startEndTime = context.getString(R.string.reminder_schedule_format,
                TimeUtils.formatTimestampWithHourMinute(deleteProgramReminderData.getStartTimeUtcMillis()),
                TimeUtils.formatTimestampWithHourMinute(deleteProgramReminderData.getEndTimeUtcMillis()));

        ProgramDeleteDialogFragment dialog = new ProgramDeleteDialogFragment(
                deleteProgramReminderData.getProgramId(),
                context.getString(R.string.reminder_delete_msg),
                deleteProgramReminderData.getChannelNumber(),
                deleteProgramReminderData.getChannelName(),
                deleteProgramReminderData.getProgramName(),
                date,
                startEndTime);

        dialog.setProgramDeleteListener(new ProgramDeleteDialogFragment.OnProgramDeleteListener() {
            @Override
            public void onProgramDelete(long programId) {
                Log.d(TAG, "ProgramDeleteDialogFragment onProgramDelete");
                if (programId == deleteProgramReminderData.getProgramId()) {
                    ProgramReminderUtils.deleteReminder(context, deleteProgramReminderData);
                }
                needRefreshEpgReminder = true;
                showMainList();

                dialog.dismiss();
            }

            @Override
            public void onProgramDeleteAll() {
                Log.d(TAG, "ProgramDeleteDialogFragment onProgramDeleteAll");
                ProgramReminderUtils.deleteAllReminders(context);
                needRefreshEpgReminder = true;
                showMainList();

                dialog.dismiss();
            }
        });
        dialog.show(hostFragment.getParentFragmentManager(), PROGRAM_REMINDER_PROGRAM_DELETE_DIALOG_TAG);
    }

    private static final String PROGRAM_REMINDER_PARENTAL_PIN_DIALOG_TAG = "ProgramReminderParentalPinDialog";
    public void showProgramReminderParentalPinDialog() {
        ParentalPinDialogFragment dialog = new ParentalPinDialogFragment();
        dialog.setOnPinEnteredListener(pin -> {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            String parentalPin = "0000"; // default value
            GposInfo gposInfo = primeDtv.gpos_info_get();
            if (gposInfo != null) {
                parentalPin = String.format(
                        Locale.ROOT,
                        "%04d",
                        GposInfo.getPasswordValue(context));
            }

            if (!TextUtils.isEmpty(pin) && pin.equals(parentalPin)) {
                tvReminderBlueKey.setVisibility(View.GONE);
                programReminderListAdapter.unlockProgramName();
                programReminderListAdapter.updateList(programReminderListPaginationManager.getCurrentPageData(), false);
                dialog.dismiss();
            } else {
                dialog.showErrorMessage();
            }
        });
        dialog.show(hostFragment.getParentFragmentManager(), PROGRAM_REMINDER_PARENTAL_PIN_DIALOG_TAG);
    }
}
