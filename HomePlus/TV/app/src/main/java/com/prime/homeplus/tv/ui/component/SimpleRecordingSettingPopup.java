package com.prime.homeplus.tv.ui.component;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;
import com.prime.homeplus.tv.ui.activity.PvrActivity;
import com.prime.homeplus.tv.utils.TimeUtils;
import com.prime.homeplus.tv.utils.ViewUtils;

import java.util.List;

public class SimpleRecordingSettingPopup {
    private static final String TAG = "SimpleRecordingSettingPopup";
    private Context context;
    private PopupWindow popupWindow;
    private View parentView, popupView;
    private ScheduledProgramData newScheduledProgramData;
    private List<ScheduledProgramData> conflictScheduledProgramDataList;

    private final Runnable dismissRunnable = this::dismiss;
    private Handler handler;
    private static final long AUTO_HIDE_BOTTOM_MESSAGE_TIMEOUT_MS = 2 * 1000;

    // Menu
    private ConstraintLayout clRecordingSettingMenu;
    private Button btnRecordingSettingMenuSingleRecord, btnRecordingSettingMenuSeriesRecord,
            btnRecordingSettingMenuGoToList, btnRecordingSettingMenuCancel;

    // Conflict
    private ConstraintLayout include_clRecordingSettingConflict;
    private LinearLayout llRecordingSettingConflictOld1, llRecordingSettingConflictOld2;
    private RadioButton rbRecordingSettingConflictOld1, rbRecordingSettingConflictOld2;
    private TextView tvRecordingSettingConflictNewChannelNumber, tvRecordingSettingConflictNewChannelName,
            tvRecordingSettingConflictNewProgramName, tvRecordingSettingConflictNewProgramTime,
            tvRecordingSettingConflictOldChannelNumber1, tvRecordingSettingConflictOldChannelName1,
            tvRecordingSettingConflictOldProgramName1, tvRecordingSettingConflictOldTime1,
            tvRecordingSettingConflictOldChannelNumber2, tvRecordingSettingConflictOldChannelName2,
            tvRecordingSettingConflictOldProgramName2, tvRecordingSettingConflictOldTime2;
    private ImageView ivRecordingSettingConflictOldIcon1, ivRecordingSettingConflictOldIcon2;
    private Button btnRecordingSettingConflictSave, btnRecordingSettingConflictCancel;

    // Bottom Message
    private ConstraintLayout clRecordingSettingBottomMessage;
    private TextView tvRecordingSettingBottomMessage;

    // Bottom Bar: Start / Cancel
    private RelativeLayout rlRecordingSettingBottomBar;
    private LinearLayout llRecordingSettingBottomBarRecord, llRecordingSettingBottomBarCancelRecord;
    private Button btnRecordingSettingBottomBarRecordOne, btnRecordingSettingBottomBarSeriesRecord,
            btnRecordingSettingBottomBarCancelSingleRecord, btnRecordingSettingBottomBarCancelSeries,
            btnRecordingSettingBottomBarCancel;
    private TextView tvRecordingSettingBottomBarProgramName;

    private final PrimeDtvServiceInterface primeDtv;

    private OnRecordingSettingChangedListener recordingSettingChangedListener;

    public interface OnRecordingSettingChangedListener {
        void onRecordingSettingChanged();
    }

    public void setOnRecordingSettingChangedListener(SimpleRecordingSettingPopup.OnRecordingSettingChangedListener listener) {
        this.recordingSettingChangedListener = listener;
    }

    public SimpleRecordingSettingPopup(Context context, View parentView, Handler handler) {
        this.context = context;
        this.parentView = parentView;
        this.handler = handler;
        initPopup();

        primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
    }

    public void setScheduledProgramInfo(Program pg, Channel ch) {
        if (pg != null && ch != null) {
            newScheduledProgramData = new ScheduledProgramData(pg, ch);
        }
    }

    private void initPopup() {
        LayoutInflater inflater = LayoutInflater.from(context);
        popupView = inflater.inflate(R.layout.popup_simple_recording_setting, null);

        initMenu();
        initConflict();
        initBottomMessage();
        initBottomBar();

        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        popupWindow.setOnDismissListener(() -> {
            Log.d(TAG, "setOnDismissListener");
            btnRecordingSettingConflictSave.setEnabled(false);
            handler.removeCallbacks(null);
        });
    }
    
    private void initMenu() {
        clRecordingSettingMenu = popupView.findViewById(R.id.clRecordingSettingMenu);
        btnRecordingSettingMenuSingleRecord = popupView.findViewById(R.id.btnRecordingSettingMenuSingleRecord);
        btnRecordingSettingMenuSeriesRecord = popupView.findViewById(R.id.btnRecordingSettingMenuSeriesRecord);
        btnRecordingSettingMenuGoToList = popupView.findViewById(R.id.btnRecordingSettingMenuGoToList);
        btnRecordingSettingMenuCancel = popupView.findViewById(R.id.btnRecordingSettingMenuCancel);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingMenuSingleRecord, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingMenuSeriesRecord, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingMenuGoToList, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingMenuCancel, 17, 16, true);

        btnRecordingSettingMenuSingleRecord.setOnClickListener((v) -> {
            addSingleRecord();
        });

        btnRecordingSettingMenuSeriesRecord.setOnClickListener((v) -> {
            addSeriesRecord();
        });

        btnRecordingSettingMenuGoToList.setOnClickListener((v) -> {
            Intent intent = new Intent(context, PvrActivity.class);
            try {
                dismiss();
                context.startActivity(intent);
            } catch (Exception e) {
                Log.d(TAG, "Error: " + e.toString());
            }
        });

        btnRecordingSettingMenuCancel.setOnClickListener((v) -> dismiss());
    }

    private void initConflict() {
        include_clRecordingSettingConflict = popupView.findViewById(R.id.include_clRecordingSettingConflict);
        llRecordingSettingConflictOld1 = popupView.findViewById(R.id.llRecordingSettingConflictOld1);
        llRecordingSettingConflictOld2 = popupView.findViewById(R.id.llRecordingSettingConflictOld2);
        rbRecordingSettingConflictOld1 = popupView.findViewById(R.id.rbRecordingSettingConflictOld1);
        rbRecordingSettingConflictOld2 = popupView.findViewById(R.id.rbRecordingSettingConflictOld2);
        tvRecordingSettingConflictNewChannelNumber = popupView.findViewById(R.id.tvRecordingSettingConflictNewChannelNumber);
        tvRecordingSettingConflictNewChannelName = popupView.findViewById(R.id.tvRecordingSettingConflictNewChannelName);
        tvRecordingSettingConflictNewProgramName = popupView.findViewById(R.id.tvRecordingSettingConflictNewProgramName);
        tvRecordingSettingConflictNewProgramTime = popupView.findViewById(R.id.tvRecordingSettingConflictNewProgramTime);
        tvRecordingSettingConflictOldChannelNumber1 = popupView.findViewById(R.id.tvRecordingSettingConflictOldChannelNumber1);
        tvRecordingSettingConflictOldChannelName1 = popupView.findViewById(R.id.tvRecordingSettingConflictOldChannelName1);
        tvRecordingSettingConflictOldProgramName1 = popupView.findViewById(R.id.tvRecordingSettingConflictOldProgramName1);
        tvRecordingSettingConflictOldTime1 = popupView.findViewById(R.id.tvRecordingSettingConflictOldTime1);
        tvRecordingSettingConflictOldChannelNumber2 = popupView.findViewById(R.id.tvRecordingSettingConflictOldChannelNumber2);
        tvRecordingSettingConflictOldChannelName2 = popupView.findViewById(R.id.tvRecordingSettingConflictOldChannelName2);
        tvRecordingSettingConflictOldProgramName2 = popupView.findViewById(R.id.tvRecordingSettingConflictOldProgramName2);
        tvRecordingSettingConflictOldTime2 = popupView.findViewById(R.id.tvRecordingSettingConflictOldTime2);
        ivRecordingSettingConflictOldIcon1 = popupView.findViewById(R.id.ivRecordingSettingConflictOldIcon1);
        ivRecordingSettingConflictOldIcon2 = popupView.findViewById(R.id.ivRecordingSettingConflictOldIcon2);
        btnRecordingSettingConflictSave = popupView.findViewById(R.id.btnRecordingSettingConflictSave);
        btnRecordingSettingConflictCancel = popupView.findViewById(R.id.btnRecordingSettingConflictCancel);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingConflictSave, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingConflictCancel, 17, 16, true);

        rbRecordingSettingConflictOld1.setOnClickListener(v -> {
            if (rbRecordingSettingConflictOld2.isChecked()) {
                rbRecordingSettingConflictOld2.setChecked(false);
            }
            btnRecordingSettingConflictSave.setEnabled(true);
        });

        rbRecordingSettingConflictOld1.setOnFocusChangeListener((v, hasFocus) -> {
            setConflict1Style(hasFocus);
        });

        rbRecordingSettingConflictOld2.setOnClickListener(v -> {
            if (rbRecordingSettingConflictOld1.isChecked()) {
                rbRecordingSettingConflictOld1.setChecked(false);
            }
            btnRecordingSettingConflictSave.setEnabled(true);
        });

        rbRecordingSettingConflictOld2.setOnFocusChangeListener((v, hasFocus) -> {
            setConflict2Style(hasFocus);
        });

        btnRecordingSettingConflictSave.setOnClickListener((v) -> {
            if (conflictScheduledProgramDataList != null && conflictScheduledProgramDataList.size() >= 2) {
                long deleteScheduledProgramId = -1;
                if (rbRecordingSettingConflictOld1.isChecked()) {
                    deleteScheduledProgramId = conflictScheduledProgramDataList.get(0).getId();
                } else if (rbRecordingSettingConflictOld2.isChecked()) {
                    deleteScheduledProgramId = conflictScheduledProgramDataList.get(1).getId();
                } else {
                    // TODO: error handling
                    return;
                }
                ScheduledProgramUtils.deleteScheduledProgram(context, deleteScheduledProgramId);
                Uri uri = ScheduledProgramUtils.insertScheduledProgram(context, newScheduledProgramData);
                if (uri != null) {
                    refreshUi();
                    showBottomMessage(context.getString(R.string.recording_setting_add_recording_success));
                } else {
                    showBottomMessage(context.getString(R.string.recording_setting_add_recording_fail));
                }
            }
        });

        btnRecordingSettingConflictCancel.setOnClickListener((v) -> dismiss());
    }

    private void initBottomMessage() {
        clRecordingSettingBottomMessage = popupView.findViewById(R.id.clRecordingSettingBottomMessage);
        tvRecordingSettingBottomMessage = popupView.findViewById(R.id.tvRecordingSettingBottomMessage);
    }

    private void initBottomBar() {
        rlRecordingSettingBottomBar = popupView.findViewById(R.id.rlRecordingSettingBottomBar);
        llRecordingSettingBottomBarRecord = popupView.findViewById(R.id.llRecordingSettingBottomBarRecord);
        llRecordingSettingBottomBarCancelRecord = popupView.findViewById(R.id.llRecordingSettingBottomBarCancelRecord);
        btnRecordingSettingBottomBarRecordOne = popupView.findViewById(R.id.btnRecordingSettingBottomBarRecordOne);
        btnRecordingSettingBottomBarSeriesRecord = popupView.findViewById(R.id.btnRecordingSettingBottomBarSeriesRecord);
        btnRecordingSettingBottomBarCancelSingleRecord = popupView.findViewById(R.id.btnRecordingSettingBottomBarCancelSingleRecord);
        btnRecordingSettingBottomBarCancelSeries = popupView.findViewById(R.id.btnRecordingSettingBottomBarCancelSeries);
        btnRecordingSettingBottomBarCancel = popupView.findViewById(R.id.btnRecordingSettingBottomBarCancel);
        tvRecordingSettingBottomBarProgramName = popupView.findViewById(R.id.tvRecordingSettingBottomBarProgramName);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingBottomBarRecordOne, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingBottomBarSeriesRecord, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingBottomBarCancelSingleRecord, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingBottomBarCancelSeries, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnRecordingSettingBottomBarCancel, 17, 16, true);

        btnRecordingSettingBottomBarRecordOne.setOnClickListener((v) -> {
            addSingleRecord();
        });

        btnRecordingSettingBottomBarSeriesRecord.setOnClickListener((v) -> {
            addSeriesRecord();
        });

        btnRecordingSettingBottomBarCancelSingleRecord.setOnClickListener((v) -> {
            cancelSingleRecord();
        });

        btnRecordingSettingBottomBarCancelSeries.setOnClickListener((v) -> {
            cancelSeriesRecord();
        });

        btnRecordingSettingBottomBarCancel.setOnClickListener((v) -> dismiss());
    }

    public void showMenu() {
        Log.d(TAG, "showMenu");
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // check PVR provisioning and current disk status
        if (!Pvcfg.getPVR_PJ()) {
            showBottomMessage(context.getString(R.string.error_pvr001));
            return;
        }

        String pvrUsbMountPath = primeDtv.pvr_get_usb_mount_path();
        if (TextUtils.isEmpty(pvrUsbMountPath)) {
            showBottomMessage(context.getString(R.string.error_pvr002));
            return;
        }

        long pvrUsbTotalSizeMB = getUsbTotalSize(pvrUsbMountPath) / (1024 * 1024);
        if (pvrUsbTotalSizeMB < Pvcfg.getPvrHddTotalSizeLimit()) {
            showBottomMessage(context.getString(R.string.error_pvr014));
            return;
        }

        // TODO: check pvr 003, 004, 009, 013?

        if (newScheduledProgramData == null) {
            showBottomMessage(context.getString(R.string.no_program_info));
            return;
        }

        ScheduledProgramData scheduledProgramData = ScheduledProgramUtils.getScheduledProgram(context, newScheduledProgramData.getId());
        if (scheduledProgramData != null) {
            showBottomBar(false);
            return;
        }

        if (TextUtils.isEmpty(newScheduledProgramData.getEpisodeTitle())) {
            btnRecordingSettingMenuSeriesRecord.setVisibility(View.GONE);
        } else {
            btnRecordingSettingMenuSeriesRecord.setVisibility(View.VISIBLE);
        }

        clRecordingSettingMenu.setVisibility(View.VISIBLE);
        include_clRecordingSettingConflict.setVisibility(View.GONE);
        clRecordingSettingBottomMessage.setVisibility(View.GONE);
        rlRecordingSettingBottomBar.setVisibility(View.GONE);

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        btnRecordingSettingMenuSingleRecord.requestFocus();
    }

    public void showConflict() {
        Log.d(TAG, "showConflict");

        clRecordingSettingMenu.setVisibility(View.GONE);
        include_clRecordingSettingConflict.setVisibility(View.VISIBLE);
        clRecordingSettingBottomMessage.setVisibility(View.GONE);
        rlRecordingSettingBottomBar.setVisibility(View.GONE);

        if (conflictScheduledProgramDataList != null && conflictScheduledProgramDataList.size() >= 2) {
            tvRecordingSettingConflictNewChannelNumber.setText(newScheduledProgramData.getChannelNumber());
            tvRecordingSettingConflictNewChannelName.setText(newScheduledProgramData.getChannelName());
            tvRecordingSettingConflictNewProgramName.setText(newScheduledProgramData.getTitle());
            String newStartEndTime = context.getString(R.string.reminder_schedule_format,
                    TimeUtils.formatTimestampWithHourMinute(newScheduledProgramData.getStartTimeUtcMillis()),
                    TimeUtils.formatTimestampWithHourMinute(newScheduledProgramData.getEndTimeUtcMillis()));
            tvRecordingSettingConflictNewProgramTime.setText(newStartEndTime);

            tvRecordingSettingConflictOldChannelNumber1.setText(conflictScheduledProgramDataList.get(0).getChannelNumber());
            tvRecordingSettingConflictOldChannelName1.setText(conflictScheduledProgramDataList.get(0).getChannelName());
            tvRecordingSettingConflictOldProgramName1.setText(conflictScheduledProgramDataList.get(0).getTitle());
            String oldStartEndTime1 = context.getString(R.string.reminder_schedule_format,
                    TimeUtils.formatTimestampWithHourMinute(conflictScheduledProgramDataList.get(0).getStartTimeUtcMillis()),
                    TimeUtils.formatTimestampWithHourMinute(conflictScheduledProgramDataList.get(0).getEndTimeUtcMillis()));
            tvRecordingSettingConflictOldTime1.setText(oldStartEndTime1);


            tvRecordingSettingConflictOldChannelNumber2.setText(conflictScheduledProgramDataList.get(1).getChannelNumber());
            tvRecordingSettingConflictOldChannelName2.setText(conflictScheduledProgramDataList.get(1).getChannelName());
            tvRecordingSettingConflictOldProgramName2.setText(conflictScheduledProgramDataList.get(1).getTitle());
            String oldStartEndTime2 = context.getString(R.string.reminder_schedule_format,
                    TimeUtils.formatTimestampWithHourMinute(conflictScheduledProgramDataList.get(1).getStartTimeUtcMillis()),
                    TimeUtils.formatTimestampWithHourMinute(conflictScheduledProgramDataList.get(1).getEndTimeUtcMillis()));
            tvRecordingSettingConflictOldTime2.setText(oldStartEndTime2);
        }

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        btnRecordingSettingConflictCancel.requestFocus();
    }

    public void showBottomMessage(String msg) {
        Log.d(TAG, "showBottomMessage");

        clRecordingSettingMenu.setVisibility(View.GONE);
        include_clRecordingSettingConflict.setVisibility(View.GONE);
        clRecordingSettingBottomMessage.setVisibility(View.VISIBLE);
        rlRecordingSettingBottomBar.setVisibility(View.GONE);

        tvRecordingSettingBottomMessage.setText(msg);
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

        handler.removeCallbacks(dismissRunnable);
        handler.postDelayed(dismissRunnable, AUTO_HIDE_BOTTOM_MESSAGE_TIMEOUT_MS);
    }

    public void showBottomBar(boolean checkPvr) {
        Log.d(TAG, "showBottomBar");

        if (checkPvr) {
            if (!Pvcfg.getPVR_PJ()) {
                showBottomMessage(context.getString(R.string.error_pvr001));
                return;
            }

            String pvrUsbMountPath = primeDtv.pvr_get_usb_mount_path();
            if (TextUtils.isEmpty(pvrUsbMountPath)) {
                showBottomMessage(context.getString(R.string.error_pvr002));
                return;
            }

            long pvrUsbTotalSizeMB = getUsbTotalSize(pvrUsbMountPath) / (1024 * 1024);
            if (pvrUsbTotalSizeMB < Pvcfg.getPvrHddTotalSizeLimit()) {
                showBottomMessage(context.getString(R.string.error_pvr014));
                return;
            }

            // TODO: check pvr 003, 004, 009, 013?
        }

        if (newScheduledProgramData == null) {
            showBottomMessage(context.getString(R.string.no_program_info));
            return;
        }

        clRecordingSettingMenu.setVisibility(View.GONE);
        include_clRecordingSettingConflict.setVisibility(View.GONE);
        clRecordingSettingBottomMessage.setVisibility(View.GONE);
        rlRecordingSettingBottomBar.setVisibility(View.VISIBLE);

        tvRecordingSettingBottomBarProgramName.setText(newScheduledProgramData.getTitle());

        ScheduledProgramData scheduledProgramData = ScheduledProgramUtils.getScheduledProgram(context, newScheduledProgramData.getId());
        if (scheduledProgramData != null) {
            if (TimeUtils.isInTimeRange(scheduledProgramData.getStartTimeUtcMillis(), scheduledProgramData.getEndTimeUtcMillis())) {
                btnRecordingSettingBottomBarCancelSingleRecord.setText(context.getString(R.string.recording_setting_cancel_record));
            } else {
                if (TextUtils.isEmpty(scheduledProgramData.getEpisodeTitle())) {
                    btnRecordingSettingBottomBarCancelSeries.setVisibility(View.GONE);
                    btnRecordingSettingBottomBarCancelSingleRecord.setText(context.getString(R.string.recording_setting_cancel_single_record));
                } else {
                    btnRecordingSettingBottomBarCancelSeries.setVisibility(View.VISIBLE);
                }
            }

            llRecordingSettingBottomBarRecord.setVisibility(View.GONE);
            llRecordingSettingBottomBarCancelRecord.setVisibility(View.VISIBLE);
        } else {
            if (TextUtils.isEmpty(newScheduledProgramData.getEpisodeTitle())) {
                btnRecordingSettingBottomBarSeriesRecord.setVisibility(View.GONE);
            } else {
                btnRecordingSettingBottomBarSeriesRecord.setVisibility(View.VISIBLE);
            }

            llRecordingSettingBottomBarRecord.setVisibility(View.VISIBLE);
            llRecordingSettingBottomBarCancelRecord.setVisibility(View.GONE);
        }

        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
        btnRecordingSettingBottomBarCancel.requestFocus();
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private void addSingleRecord() {
        // set cycle to one time for single record
        newScheduledProgramData.setCycle(BookInfo.BOOK_CYCLE_ONETIME);
        // remove series related info for single record
        newScheduledProgramData.setEpisodeTitle(null);
        newScheduledProgramData.setEpisodeDisplayNumber(null);
        newScheduledProgramData.setSeriesId(null);

        conflictScheduledProgramDataList = ScheduledProgramUtils.getConflictScheduledPrograms(context, newScheduledProgramData);
        if (conflictScheduledProgramDataList != null && conflictScheduledProgramDataList.size() >= 2) {
            showConflict();
        } else {
            Uri uri = ScheduledProgramUtils.insertScheduledProgram(context, newScheduledProgramData);
            if (uri != null) {
                refreshUi();
                showBottomMessage(context.getString(R.string.recording_setting_add_recording_success));
            } else {
                showBottomMessage(context.getString(R.string.recording_setting_add_recording_fail));
            }
        }
    }

    private void addSeriesRecord() {
        // set cycle to series for series record
        newScheduledProgramData.setCycle(BookInfo.BOOK_CYCLE_SERIES);

        conflictScheduledProgramDataList = ScheduledProgramUtils.getConflictScheduledPrograms(context, newScheduledProgramData);
        if (conflictScheduledProgramDataList != null && conflictScheduledProgramDataList.size() >= 2) {
            showConflict();
        } else {
            Uri uri = ScheduledProgramUtils.insertScheduledProgram(context, newScheduledProgramData);
            if (uri != null) {
                refreshUi();
                showBottomMessage(context.getString(R.string.recording_setting_add_recording_success));
            } else {
                showBottomMessage(context.getString(R.string.recording_setting_add_recording_fail));
            }
        }
    }

    private void cancelSingleRecord() {
        ScheduledProgramUtils.deleteScheduledProgram(context, newScheduledProgramData.getId());
        refreshUi();
        dismiss();
    }

    private void cancelSeriesRecord() {
        ScheduledProgramUtils.deleteScheduledProgram(context, newScheduledProgramData.getId());
        refreshUi();
        dismiss();
    }

    private void refreshUi() {
        if (recordingSettingChangedListener != null) {
            recordingSettingChangedListener.onRecordingSettingChanged();
        }
    }

    private void setConflict1Style(boolean hasFocus) {
        if (hasFocus) {
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelNumber1, 16, R.color.white, Typeface.BOLD);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelName1, 17, R.color.white, Typeface.BOLD);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldProgramName1, 17, R.color.white, Typeface.BOLD);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldTime1, 16, R.color.white, Typeface.NORMAL);
            ivRecordingSettingConflictOldIcon1.setImageResource(R.drawable.icon_miniguide_rec_resv_f);
            llRecordingSettingConflictOld1.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
        } else {
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelNumber1, 15, R.color.colorGoldOpacity70, Typeface.NORMAL);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelName1, 16, R.color.colorWhiteOpacity70, Typeface.NORMAL);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldProgramName1, 16, R.color.colorWhiteOpacity70, Typeface.NORMAL);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldTime1, 15, R.color.colorWhiteOpacity70, Typeface.NORMAL);
            ivRecordingSettingConflictOldIcon1.setImageResource(R.drawable.icon_miniguide_rec_resv);
            llRecordingSettingConflictOld1.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setConflict2Style(boolean hasFocus) {
        if (hasFocus) {
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelNumber2, 16, R.color.white, Typeface.BOLD);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelName2, 17, R.color.white, Typeface.BOLD);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldProgramName2, 17, R.color.white, Typeface.BOLD);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldTime2, 16, R.color.white, Typeface.NORMAL);
            ivRecordingSettingConflictOldIcon2.setImageResource(R.drawable.icon_miniguide_rec_resv_f);
            llRecordingSettingConflictOld2.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
        } else {
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelNumber2, 15, R.color.colorGoldOpacity70, Typeface.NORMAL);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldChannelName2, 16, R.color.colorWhiteOpacity70, Typeface.NORMAL);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldProgramName2, 16, R.color.colorWhiteOpacity70, Typeface.NORMAL);
            ViewUtils.applyTextViewTextEffect(tvRecordingSettingConflictOldTime2, 15, R.color.colorWhiteOpacity70, Typeface.NORMAL);
            ivRecordingSettingConflictOldIcon2.setImageResource(R.drawable.icon_miniguide_rec_resv);
            llRecordingSettingConflictOld2.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private long getUsbTotalSize(String usbPath) {
        StatFs statFs = new StatFs(Uri.parse(usbPath).getPath());
        return statFs.getBlockCountLong() * statFs.getBlockSizeLong();
    }
}
