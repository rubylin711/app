package com.prime.homeplus.tv.ui.component;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;
import androidx.tvprovider.media.tv.TvContractUtils;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.adapter.PvrManualChannelListAdapter;
import com.prime.homeplus.tv.adapter.PvrManualGenreListAdapter;
import com.prime.homeplus.tv.data.GenreData;
import com.prime.homeplus.tv.data.GlobalState;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.manager.NowPlayingManager;
import com.prime.homeplus.tv.ui.fragment.ParentalPinDialogFragment;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.PrimeUtils;
import com.prime.homeplus.tv.utils.ProgramRatingUtils;
import com.prime.homeplus.tv.utils.ProgramUtils;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;
import com.prime.homeplus.tv.utils.TimeUtils;
import com.prime.homeplus.tv.utils.ViewUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PvrManualRecordingPopup {
    private static final String TAG = "PvrManualRecordingPopup";
    private AppCompatActivity activity;
    private Context context;
    private PopupWindow popupWindow;
    private View parentView, popupView;
    private OnJumpToScheduledPageListener jumpToScheduledPageListener;
    private OnInsertRecordingScheduleListener insertRecordingScheduleListener;

    private PvrManualGenreListAdapter pvrManualGenreListAdapter;
    private PvrManualChannelListAdapter pvrManualChannelListAdapter;
    private GenreData.GenreInfo currentGenreInfo = null;
    private List<Channel> currentChannelList = new ArrayList<>();
    private Channel currentChannel = null;

    private static final int CHANNEL_LIST_SPAN_COUNT = 2;

    // TvView
    private String lastInputId = null;
    private int lastReason = -1;
    private long lastTime = 0;
    private static final long MIN_INTERVAL_MS = 50;


    private boolean isLock = true;
    private TvView pvrManualTvView;
    private NowPlayingManager nowPlayingManager;

    // Main
    private Button btnPvrManualStep1, btnPvrManualStep2;
    private TextView tvPvrManualStep1First, tvPvrManualStep1Second, tvPvrManualStep1Third,
            tvPvrManualStep2First, tvPvrManualStep2Second, tvPvrManualStep2Third,
            tvPvrManualStep1Line, tvPvrManualStep2Line;

    private ConstraintLayout clPvrManualLiveChannel;
    private LinearLayout llPvrManualStep1Title, llPvrManualStep2Title,
            llPvrManualUnlockAll, llPvrManualShowSchedule;
    private ImageView ivPvrManualTvFrameBlock;
    private ProgressBar pbPvrManualProgram;
    private Button btnPvrManualConfirm, btnPvrManualCancel;
    private TextView tvPvrManualChannelNumber, tvPvrManualChannelName,
            tvPvrManualProgramName, tvPvrManualProgramStartEndTime;


    // Step 1
    private ConstraintLayout include_clPvrManualStep1;
    private RecyclerView rvPvrManualStep1Genre, rvPvrManualStep1ChannelList;

    // Step 2
    private LinearLayout include_llPvrManualStep2;
    private ConstraintLayout clPvrManualStep2Weekly;
    private RadioGroup rgPvrManualStep2Mode;
    private RadioButton rbtnPvrManualStep2Once, rbtnPvrManualStep2Daily, rbtnPvrManualStep2Weekly;
    private Button btnPvrManualStep2Monday, btnPvrManualStep2Tuesday, btnPvrManualStep2Wednesday,
            btnPvrManualStep2Thursday, btnPvrManualStep2Friday, btnPvrManualStep2Saturday,
            btnPvrManualStep2Sunday;
    private CheckBox cbPvrManualStep2Monday, cbPvrManualStep2Tuesday, cbPvrManualStep2Wednesday,
            cbPvrManualStep2Thursday, cbPvrManualStep2Friday, cbPvrManualStep2Saturday,
            cbPvrManualStep2Sunday;
    private EditText etPvrManualStep2StartDate, etPvrManualStep2StartHour, etPvrManualStep2StartMinute,
            etPvrManualStep2EndHour, etPvrManualStep2EndMinute;

    // Scheduled Recording Success
    private ConstraintLayout include_clPvrManualSuccess;
    private TextView tvPvrManualSuccessChannelNumber, tvPvrManualSuccessChannelName, tvPvrManualSuccessMode,
            tvPvrManualSuccessDate, tvPvrManualSuccessTime;

    private static final int RECORD_MINUTE_STEP = 10;
    private final PrimeDtvServiceInterface primeDtv;
    private String strLastValidStartHour = "00";
    private String strLastValidEndHour = "00";
    private String strLastValidStartMinute = "00";
    private String strLastValidEndMinute = "00";

    private static final long DISMISS_DELAY_MS = 5000L;
    private DatePickerDialog datePickerDialog;
    private final Handler handler;
    private final Runnable dismissRunnable = this::dismiss;

    public interface OnJumpToScheduledPageListener {
        void onJumpToScheduledPage();
    }

    public interface OnInsertRecordingScheduleListener {
        void onInsertRecordingSchedule();
    }

    public void setOnJumpToScheduledPageListener(OnJumpToScheduledPageListener listener) {
        this.jumpToScheduledPageListener = listener;
    }

    public void setOnInsertRecordingScheduleListener(OnInsertRecordingScheduleListener listener) {
        this.insertRecordingScheduleListener = listener;
    }

    public PvrManualRecordingPopup(AppCompatActivity activity, Context context, View parentView) {
        this.activity = activity;
        this.context = context;
        this.parentView = parentView;
        primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        handler = new Handler(Looper.getMainLooper());

        initChannels();
        initPopup();
    }

    private void initChannels() {
        String channelDisplayNumber = "";

        currentChannelList = getChannelList(null);

        if (currentChannelList.isEmpty()) {
            Log.d(TAG, "No default channel list");
            return;
        }

        if (!TextUtils.isEmpty(GlobalState.lastWatchedChannelNumber)) {
            channelDisplayNumber = GlobalState.lastWatchedChannelNumber;
        }

        if (TextUtils.isEmpty(channelDisplayNumber)) {
            currentChannel = currentChannelList.get(0);
        } else {
            for (Channel ch : currentChannelList) {
                if (channelDisplayNumber.equals(ch.getDisplayNumber())) {
                    currentChannel = ch;
                    break;
                }
            }
        }
    }

    private void initPopup() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        popupView = inflater.inflate(R.layout.popup_pvr_manual_recording, null);

        initTvViews();
        initMainViews();
        initStep1Views();
        initStep2Views();
        initScheduledSuccessViews();

        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        popupWindow.setOnDismissListener(() -> {
            Log.d(TAG, "setOnDismissListener");
            isLock = true;

            if (nowPlayingManager != null) {
                nowPlayingManager.cancelPendingTune();
            }

            if (pvrManualTvView != null) {
                pvrManualTvView.reset();
            }

            handler.removeCallbacksAndMessages(null);
        });

        if (nowPlayingManager == null) {
            nowPlayingManager = new NowPlayingManager(context);
        }

        if (currentChannel != null) {
            tuneChannel(currentChannel);
        }

        btnPvrManualStep1.requestFocus();
    }

    private void initTvViews() {
        pvrManualTvView = popupView.findViewById(R.id.pvrManualTvView);
        ivPvrManualTvFrameBlock = popupView.findViewById(R.id.ivPvrManualTvFrameBlock);
        pvrManualTvView.setCallback(mCallBack);
    }

    private void tuneChannel(Channel ch) {
        Log.d(TAG, "tuneChannel ch:" + ch.getDisplayNumber());
        tvPvrManualChannelNumber.setText(ch.getDisplayNumber());
        tvPvrManualChannelName.setText(ch.getDisplayName());

        Program pg = ProgramUtils.getCurrentProgram(context, ch.getId());
        if (pg != null) {
            String startEndTime = TimeUtils.formatTimestampWithHourMinute(pg.getStartTimeUtcMillis()) +
                    "-" +
                    TimeUtils.formatTimestampWithHourMinute(pg.getEndTimeUtcMillis());
            tvPvrManualProgramStartEndTime.setText(startEndTime);
            ViewUtils.setProgressWithMillisMax(pbPvrManualProgram, pg.getStartTimeUtcMillis(), pg.getEndTimeUtcMillis());

            int ratingAge = ProgramRatingUtils.getStrictestDvbAge(pg.getContentRatings());
            int blockedRatingAge = ProgramRatingUtils.getSystemContentBlockedRating(context);

            if (ratingAge >= blockedRatingAge && isLock) {
                tvPvrManualProgramName.setText(context.getString(R.string.channel_list_parental_rated_program));

                pvrManualTvView.reset();
                ivPvrManualTvFrameBlock.setVisibility(View.VISIBLE);
            } else {
                tvPvrManualProgramName.setText(pg.getTitle());

                // disable OVERLAY_VIEW
                // it will crash if TIF session create overlay view in popup window
                Bundle params = new Bundle();
                params.putBoolean("overlay_view_enable", false);
                nowPlayingManager.tune(pvrManualTvView, ch, params);

                ivPvrManualTvFrameBlock.setVisibility(View.GONE);
            }
        } else {
            tvPvrManualProgramName.setText(context.getString(R.string.no_program_info));
            tvPvrManualProgramStartEndTime.setText("00:00-00:00");

            pvrManualTvView.reset();
            ivPvrManualTvFrameBlock.setVisibility(View.VISIBLE);
        }
    }

    private TvView.TvInputCallback mCallBack = new TvView.TvInputCallback() {
        @Override
        public void onContentBlocked(String inputId, TvContentRating rating) {
            Log.d(TAG, "TvView onContentBlocked: " + inputId + ", " + (null != rating ? rating.flattenToString() : "null"));
            super.onContentBlocked(inputId, rating);
        }

        public void onContentAllowed(String inputId) {
            Log.d(TAG, "TvView onContentAllowed:" + inputId);
        }

        @Override
        public void onVideoSizeChanged(String inputId, int width, int height) {
            super.onVideoSizeChanged(inputId, width, height);
            Log.d(TAG, "TvView onVideoSizeChanged: " + width + "x" + height);
        }

        @Override
        public void onVideoUnavailable(String inputId, int reason) {
            // TODO: TBC - Prevent rapid duplicate onVideoUnavailable callback
            long now = System.currentTimeMillis();
            if (inputId.equals(lastInputId) && reason == lastReason && (now - lastTime < MIN_INTERVAL_MS)) {
                Log.d(TAG, "Duplicate onVideoUnavailable call ignored");
                return;
            }  else {
                lastInputId = inputId;
                lastReason = reason;
                lastTime = now;
            }

            super.onVideoUnavailable(inputId, reason);
            String reasonMsg = "none";
            if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING";
            } else if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL";

                ivPvrManualTvFrameBlock.setVisibility(View.VISIBLE);
            } else if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING";
            } else if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY";
            }

            Log.d(TAG, "TvView onVideoUnavailable inputId: " + inputId +
                    ", reason no: " + reason +
                    ", reason msg: " + reasonMsg);
        }

        public void onVideoAvailable(String inputId) {
            Log.d(TAG, "TvView onVideoAvailable");
        }
    };

    private final View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                    jumpToScheduledList();
                } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
                    showPvrManualParentalPinDialog();
                }
            }
            return false;
        }
    };

    private void initMainViews() {
        btnPvrManualStep1 = popupView.findViewById(R.id.btnPvrManualStep1);
        btnPvrManualStep2 = popupView.findViewById(R.id.btnPvrManualStep2);
        llPvrManualStep1Title = popupView.findViewById(R.id.llPvrManualStep1Title);
        llPvrManualStep2Title = popupView.findViewById(R.id.llPvrManualStep2Title);
        tvPvrManualStep1First = popupView.findViewById(R.id.tvPvrManualStep1First);
        tvPvrManualStep1Second = popupView.findViewById(R.id.tvPvrManualStep1Second);
        tvPvrManualStep1Third = popupView.findViewById(R.id.tvPvrManualStep1Third);
        tvPvrManualStep2First = popupView.findViewById(R.id.tvPvrManualStep2First);
        tvPvrManualStep2Second = popupView.findViewById(R.id.tvPvrManualStep2Second);
        tvPvrManualStep2Third = popupView.findViewById(R.id.tvPvrManualStep2Third);
        tvPvrManualStep1Line = popupView.findViewById(R.id.tvPvrManualStep1Line);
        tvPvrManualStep2Line = popupView.findViewById(R.id.tvPvrManualStep2Line);
        btnPvrManualStep1.setOnKeyListener(keyListener);
        btnPvrManualStep1.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "btnPvrManualStep1 hasFocus:" + hasFocus);
            setStep1TitleStyle(hasFocus);
        });

        btnPvrManualStep1.setOnClickListener((v) -> {
            showStep1();
            setStep2TitleStyle(false);
        });

        btnPvrManualStep2.setOnKeyListener(keyListener);
        btnPvrManualStep2.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "btnPvrManualStep2 hasFocus:" + hasFocus);
            setStep2TitleStyle(hasFocus);
        });

        btnPvrManualStep2.setOnClickListener((v) -> {
            showStep2();
            setStep1TitleStyle(false);
        });

        clPvrManualLiveChannel = popupView.findViewById(R.id.clPvrManualLiveChannel);
        llPvrManualUnlockAll = popupView.findViewById(R.id.llPvrManualUnlockAll);
        llPvrManualShowSchedule = popupView.findViewById(R.id.llPvrManualShowSchedule);
        pbPvrManualProgram = popupView.findViewById(R.id.pbPvrManualProgram);
        btnPvrManualConfirm = popupView.findViewById(R.id.btnPvrManualConfirm);
        btnPvrManualCancel = popupView.findViewById(R.id.btnPvrManualCancel);
        tvPvrManualChannelNumber = popupView.findViewById(R.id.tvPvrManualChannelNumber);
        tvPvrManualChannelName = popupView.findViewById(R.id.tvPvrManualChannelName);
        tvPvrManualProgramName = popupView.findViewById(R.id.tvPvrManualProgramName);
        tvPvrManualProgramStartEndTime = popupView.findViewById(R.id.tvPvrManualProgramStartEndTime);
        ViewUtils.applyButtonFocusTextEffect(btnPvrManualConfirm, 17, 16, true);
        btnPvrManualConfirm.setOnKeyListener(keyListener);
        btnPvrManualConfirm.setOnClickListener((v) -> {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // save recording schedule
            if (currentChannel != null) {
                if (validateRecordStartTime() &&
                        validateRecordDuration() &&
                        saveRecordingSchedule()) {
                    include_clPvrManualSuccess.setVisibility(View.VISIBLE);
                    tvPvrManualSuccessChannelNumber.setText(currentChannel.getDisplayNumber());
                    tvPvrManualSuccessChannelName.setText(currentChannel.getDisplayName());
                    tvPvrManualSuccessDate.setText(etPvrManualStep2StartDate.getText());
                    tvPvrManualSuccessTime.setText(getSuccessTimeString());
                    tvPvrManualSuccessMode.setText(getSuccessModeString());
                    include_clPvrManualSuccess.requestFocus();
                    handler.postDelayed(dismissRunnable, DISMISS_DELAY_MS);
                }
            } else {
                Log.d(TAG, "currentChannel is null");
                PrimeUtils.showCenterSnackBar(v, "Invalid channel");
                dismiss();
            }
        });

        ViewUtils.applyButtonFocusTextEffect(btnPvrManualCancel, 17, 16, true);
        btnPvrManualCancel.setOnKeyListener(keyListener);
        btnPvrManualCancel.setOnClickListener((v) -> {
            dismiss();
        });
    }

    private void initStep1Views() {
        include_clPvrManualStep1 = popupView.findViewById(R.id.include_clPvrManualStep1);
        rvPvrManualStep1Genre = popupView.findViewById(R.id.rvPvrManualStep1Genre);
        rvPvrManualStep1ChannelList = popupView.findViewById(R.id.rvPvrManualStep1ChannelList);

        rvPvrManualStep1Genre.setLayoutManager(new LinearLayoutManager(context));
        pvrManualGenreListAdapter = new PvrManualGenreListAdapter(rvPvrManualStep1Genre, GenreData.getAllEpgGenres());
        pvrManualGenreListAdapter.setOnRecyclerViewInteractionListener(new PvrManualGenreListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onFocus(GenreData.GenreInfo genreInfo) {
                Log.d(TAG, "pvrManualGenreListAdapter onFocus genreId:" + genreInfo.id + ", genreName:" + genreInfo.getName(context));
                if (currentGenreInfo == null || (genreInfo.id != currentGenreInfo.id)) {
                    currentGenreInfo = genreInfo;
                    currentChannelList = getChannelList(currentGenreInfo);
                    pvrManualChannelListAdapter.updateListAndSelectItem(currentChannelList, currentChannel);
                }
            }

            @Override
            public void onKeyEventReceived(KeyEvent event) {
                int keyCode = event.getKeyCode();
                Log.d(TAG, "pvrManualGenreListAdapter keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    focusRecycleViewIndex(rvPvrManualStep1ChannelList, 0);
                } else if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                    jumpToScheduledList();
                } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
                    showPvrManualParentalPinDialog();
                }
            }
        });

        rvPvrManualStep1Genre.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "rvPvrManualStep1Genre onGlobalLayout");
            }
        });

        rvPvrManualStep1Genre.setAdapter(pvrManualGenreListAdapter);

        rvPvrManualStep1ChannelList.setLayoutManager(new GridLayoutManager(context, CHANNEL_LIST_SPAN_COUNT));
        pvrManualChannelListAdapter = new PvrManualChannelListAdapter(rvPvrManualStep1ChannelList, currentChannelList, CHANNEL_LIST_SPAN_COUNT);
        pvrManualChannelListAdapter.setOnRecyclerViewInteractionListener(new PvrManualChannelListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onClick(Channel ch) {
                Log.d(TAG, "pvrManualChannelListAdapter onClick ch: " + ch.getDisplayNumber());
                currentChannel = ch;
                tuneChannel(currentChannel);
                btnPvrManualStep2.requestFocus();
                showStep2();
            }

            @Override
            public void onJumpToGenreList() {
                Log.d(TAG, "pvrManualChannelListAdapter onJumpToGenreList");
                focusRecycleViewIndex(rvPvrManualStep1Genre, pvrManualGenreListAdapter.getCurrentGenreIndex());
            }

            @Override
            public void onJumpToCancel() {
                Log.d(TAG, "pvrManualChannelListAdapter onJumpToCancel");
                btnPvrManualCancel.requestFocus();
            }

            @Override
            public void onKeyEventReceived(KeyEvent event) {
                int keyCode = event.getKeyCode();
                Log.d(TAG, "pvrManualChannelListAdapter keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                } else if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                    jumpToScheduledList();
                } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
                    showPvrManualParentalPinDialog();
                }
            }
        });

        rvPvrManualStep1ChannelList.setAdapter(pvrManualChannelListAdapter);
        pvrManualChannelListAdapter.setSelectItem(currentChannel);
    }

    private void openDatePicker() {
        if (datePickerDialog == null) {
            final Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH);
            int day = now.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog.OnDateSetListener onDateSetListener =
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                // month start from 0 so add 1
                String strDate = String.format(
                        Locale.getDefault(),
                        "%d.%02d.%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay);
                // set date string to edittext
                etPvrManualStep2StartDate.setText(strDate);
                etPvrManualStep2StartHour.requestFocus();
            };

            datePickerDialog = new DatePickerDialog(activity, onDateSetListener, year, month, day);

            // make it focus button instead of year on the top left
            datePickerDialog.setOnShowListener(dialog -> {
                Button positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.requestFocus();
            });
            datePickerDialog.setOnDismissListener(
                    dialog -> etPvrManualStep2StartDate.selectAll());

            // this will make it hard to focus day in dialog
            // so check date manually by setOnDateChangedListener
//            datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());

            datePickerDialog.getDatePicker().setOnDateChangedListener((
                    view, changedYear, changedMonth, changedDay) -> {
                Calendar selected = Calendar.getInstance();
                Button positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                selected.set(changedYear, changedMonth, changedDay);
                // warn user and block confirm button if choose before now
                if (selected.before(now)) {
                    PrimeUtils.showCenterSnackBar(
                            view,
                            R.string.pvr_manual_step2_schedule_must_after_now);
                    positiveButton.setEnabled(false);
                } else {
                    positiveButton.setEnabled(true);
                }
            });
        }

        if (!datePickerDialog.isShowing()) {
            datePickerDialog.show();
        }
    }

    private void initStep2Views() {
        include_llPvrManualStep2 = popupView.findViewById(R.id.include_llPvrManualStep2);
        clPvrManualStep2Weekly = popupView.findViewById(R.id.clPvrManualStep2Weekly);
        rgPvrManualStep2Mode = popupView.findViewById(R.id.rgPvrManualStep2Mode);
        rbtnPvrManualStep2Once = popupView.findViewById(R.id.rbtnPvrManualStep2Once);
        rbtnPvrManualStep2Daily = popupView.findViewById(R.id.rbtnPvrManualStep2Daily);
        rbtnPvrManualStep2Weekly = popupView.findViewById(R.id.rbtnPvrManualStep2Weekly);

        btnPvrManualStep2Monday = popupView.findViewById(R.id.btnPvrManualStep2Monday);
        btnPvrManualStep2Tuesday = popupView.findViewById(R.id.btnPvrManualStep2Tuesday);
        btnPvrManualStep2Wednesday = popupView.findViewById(R.id.btnPvrManualStep2Wednesday);
        btnPvrManualStep2Thursday = popupView.findViewById(R.id.btnPvrManualStep2Thursday);
        btnPvrManualStep2Friday = popupView.findViewById(R.id.btnPvrManualStep2Friday);
        btnPvrManualStep2Saturday = popupView.findViewById(R.id.btnPvrManualStep2Saturday);
        btnPvrManualStep2Sunday = popupView.findViewById(R.id.btnPvrManualStep2Sunday);
        cbPvrManualStep2Monday = popupView.findViewById(R.id.cbPvrManualStep2Monday);
        cbPvrManualStep2Tuesday = popupView.findViewById(R.id.cbPvrManualStep2Tuesday);
        cbPvrManualStep2Wednesday = popupView.findViewById(R.id.cbPvrManualStep2Wednesday);
        cbPvrManualStep2Thursday = popupView.findViewById(R.id.cbPvrManualStep2Thursday);
        cbPvrManualStep2Friday = popupView.findViewById(R.id.cbPvrManualStep2Friday);
        cbPvrManualStep2Saturday = popupView.findViewById(R.id.cbPvrManualStep2Saturday);
        cbPvrManualStep2Sunday = popupView.findViewById(R.id.cbPvrManualStep2Sunday);

        etPvrManualStep2StartDate = popupView.findViewById(R.id.etPvrManualStep2StartDate);
        etPvrManualStep2StartDate.setShowSoftInputOnFocus(false);
        // show date picker dialog when input 0~9
        etPvrManualStep2StartDate.setKeyListener(new KeyListener() {
            @Override
            public int getInputType() {
                return 0;
            }

            @Override
            public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    openDatePicker();
                    return true;
                }

                return false;
            }

            @Override
            public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyOther(View view, Editable text, KeyEvent event) {
                return false;
            }

            @Override
            public void clearMetaKeyState(View view, Editable content, int states) {

            }
        });
        // show date picker dialog when click
        etPvrManualStep2StartDate.setOnClickListener(v -> openDatePicker());

        etPvrManualStep2StartHour = popupView.findViewById(R.id.etPvrManualStep2StartHour);
        etPvrManualStep2StartHour.setShowSoftInputOnFocus(false);
        etPvrManualStep2StartHour.setOnKeyListener(createHourOnKeyListener(etPvrManualStep2StartHour));
        etPvrManualStep2StartHour.setOnFocusChangeListener(createHourOnFocusChangeListener(etPvrManualStep2StartHour));

        etPvrManualStep2StartMinute = popupView.findViewById(R.id.etPvrManualStep2StartMinute);
        etPvrManualStep2StartMinute.setShowSoftInputOnFocus(false);
        etPvrManualStep2StartMinute.setOnKeyListener(createMinuteOnKeyListener(etPvrManualStep2StartMinute));

        etPvrManualStep2EndHour = popupView.findViewById(R.id.etPvrManualStep2EndHour);
        etPvrManualStep2EndHour.setShowSoftInputOnFocus(false);
        etPvrManualStep2EndHour.setOnKeyListener(createHourOnKeyListener(etPvrManualStep2EndHour));
        etPvrManualStep2EndHour.setOnFocusChangeListener(createHourOnFocusChangeListener(etPvrManualStep2EndHour));

        etPvrManualStep2EndMinute = popupView.findViewById(R.id.etPvrManualStep2EndMinute);
        etPvrManualStep2EndMinute.setShowSoftInputOnFocus(false);
        etPvrManualStep2EndMinute.setOnKeyListener(createMinuteOnKeyListener(etPvrManualStep2EndMinute));

        // init value of recording time edit text
        setNextValidRecordingTime();

        // set watcher after init value of recording time edit text
        etPvrManualStep2StartHour.addTextChangedListener(createHourTextWatcher(etPvrManualStep2StartHour));
        etPvrManualStep2StartMinute.addTextChangedListener(createMinuteTextWatcher(etPvrManualStep2StartMinute));
        etPvrManualStep2EndHour.addTextChangedListener(createHourTextWatcher(etPvrManualStep2EndHour));
        etPvrManualStep2EndMinute.addTextChangedListener(createMinuteTextWatcher(etPvrManualStep2EndMinute));

        ViewUtils.applyRadioButtonFocusTextEffect(rbtnPvrManualStep2Once, 17, 16, true);
        rbtnPvrManualStep2Once.setOnClickListener(v -> hideStep2Weekly());

        ViewUtils.applyRadioButtonFocusTextEffect(rbtnPvrManualStep2Daily, 17, 16, true);
        rbtnPvrManualStep2Daily.setOnClickListener(v -> hideStep2Weekly());

        ViewUtils.applyRadioButtonFocusTextEffect(rbtnPvrManualStep2Weekly, 17, 16, true);
        rbtnPvrManualStep2Weekly.setOnClickListener(v -> showStep2Weekly());

        Button[] buttons = {
                btnPvrManualStep2Monday, btnPvrManualStep2Tuesday, btnPvrManualStep2Wednesday,
                btnPvrManualStep2Thursday, btnPvrManualStep2Friday, btnPvrManualStep2Saturday,
                btnPvrManualStep2Sunday
        };

        CheckBox[] checkBoxes = {
                cbPvrManualStep2Monday, cbPvrManualStep2Tuesday, cbPvrManualStep2Wednesday,
                cbPvrManualStep2Thursday, cbPvrManualStep2Friday, cbPvrManualStep2Saturday,
                cbPvrManualStep2Sunday
        };

        for (int i = 0; i < buttons.length; i++) {
            ViewUtils.applyButtonFocusTextEffect(buttons[i], 17, 16, true);
            ViewUtils.bindToggleButtonToCheckBox(buttons[i], checkBoxes[i]);
        }
    }

    private View.OnKeyListener createHourOnKeyListener(EditText editText) {
        return (v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

            Editable text = editText.getText();
            int length = text.length();

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (!validateHour(editText)) return true;

                    int pos = (keyCode == KeyEvent.KEYCODE_DPAD_UP) ? 0 : length;
                    editText.setSelection(pos);
                    return false;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    int start = editText.getSelectionStart();
                    int end = editText.getSelectionEnd();
                    boolean isAllSelected = (length > 0 && start == 0 && end == length);

                    if (!isAllSelected && length > 0) {
                        text.delete(length - 1, length);
                        return true;
                    } else {
                        if (!validateHour(editText)) return true;
                        editText.setSelection(0);
                        return false;
                    }

                default:
                    return false;
            }
        };
    }

    private View.OnKeyListener createMinuteOnKeyListener(EditText editText) {
        return (v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;

            Editable text = editText.getText();
            int length = text.length();

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    int pos = (keyCode == KeyEvent.KEYCODE_DPAD_UP) ? 0 : length;
                    editText.setSelection(pos);
                    return false;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    int start = editText.getSelectionStart();
                    int end = editText.getSelectionEnd();
                    boolean isAllSelected = (length > 0 && start == 0 && end == length);

                    if (!isAllSelected && length > 0) {
                        text.delete(length - 1, length);
                        return true;
                    } else {
                        editText.setSelection(0);
                        return false;
                    }

                default:
                    return false;
            }
        };
    }

    private View.OnFocusChangeListener createHourOnFocusChangeListener(EditText editText) {
        return (v, hasFocus) -> {
            if (!hasFocus) {
                int length = editText.length();
                if (length < 2) {
                    if (validateHour(editText)) {
                        updateConfirmButtonState();
                    } else {
                        editText.setText(getHourLastValidValue(editText.getId()));
                        editText.selectAll();
                    }
                } else {
                    updateConfirmButtonState();
                }
            }
        };
    }

    private TextWatcher createHourTextWatcher(EditText editText) {
        return new TextWatcher() {
            boolean isEditing = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing || s.length() != 2) return;

                isEditing = true;
                if (validateHour(editText)) {
                    View nextFocus = (editText.getId() == R.id.etPvrManualStep2StartHour)
                            ? etPvrManualStep2StartMinute : etPvrManualStep2EndMinute;
                    editText.post(nextFocus::requestFocus);
                } else {
                    editText.setText(getHourLastValidValue(editText.getId()));
                    editText.selectAll();
                }
                isEditing = false;
            }
        };
    }

    private TextWatcher createMinuteTextWatcher(EditText editText) {
        return new TextWatcher() {
            boolean isEditing = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing || s.length() == 0) return;

                isEditing = true;
                char c = s.toString().charAt(0);
                int minute = (c - '0') * 10;
                editText.setText(String.format(Locale.getDefault(), "%02d", minute));

                if (validateMinute(editText)) {
                    if (editText.getId() == R.id.etPvrManualStep2StartMinute) {
                        if (validateRecordStartTime()) {
                            editText.post(etPvrManualStep2EndHour::requestFocus);
                            btnPvrManualConfirm.setEnabled(validateRecordDuration());
                        } else {
                            editText.selectAll();
                            btnPvrManualConfirm.setEnabled(false);
                        }
                    } else { // EndMinuteCase
                        if (validateRecordStartTime() && validateRecordDuration()) {
                            btnPvrManualConfirm.setEnabled(true);
                            editText.post(btnPvrManualConfirm::requestFocus);
                        } else {
                            editText.selectAll();
                            btnPvrManualConfirm.setEnabled(false);
                        }
                    }
                } else {
                    editText.setText(getMinuteLastValidValue(editText.getId()));
                    editText.selectAll();
                }

                isEditing = false;
            }
        };
    }

    private String getHourLastValidValue(int viewId) {
        return (viewId == R.id.etPvrManualStep2StartHour) ? strLastValidStartHour : strLastValidEndHour;
    }

    private String getMinuteLastValidValue(int viewId) {
        return (viewId == R.id.etPvrManualStep2StartMinute) ? strLastValidStartMinute : strLastValidEndMinute;
    }

    private void updateConfirmButtonState() {
        btnPvrManualConfirm.setEnabled(validateRecordStartTime() && validateRecordDuration());
    }

    private void initScheduledSuccessViews() {
        include_clPvrManualSuccess = popupView.findViewById(R.id.include_clPvrManualSuccess);
        tvPvrManualSuccessChannelNumber = popupView.findViewById(R.id.tvPvrManualSuccessChannelNumber);
        tvPvrManualSuccessChannelName = popupView.findViewById(R.id.tvPvrManualSuccessChannelName);
        tvPvrManualSuccessMode = popupView.findViewById(R.id.tvPvrManualSuccessMode);
        tvPvrManualSuccessDate = popupView.findViewById(R.id.tvPvrManualSuccessDate);
        tvPvrManualSuccessTime = popupView.findViewById(R.id.tvPvrManualSuccessTime);
    }

    public void show() {
        showStep1();
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);
    }

    public void showStep1() {
        Log.d(TAG, "showStep1");
        include_clPvrManualStep1.setVisibility(View.VISIBLE);
        include_llPvrManualStep2.setVisibility(View.GONE);

        llPvrManualUnlockAll.setVisibility(View.VISIBLE);
        llPvrManualShowSchedule.setVisibility(View.VISIBLE);
    }


    public void showStep2() {
        Log.d(TAG, "showStep2");
        include_clPvrManualStep1.setVisibility(View.GONE);
        include_llPvrManualStep2.setVisibility(View.VISIBLE);

        llPvrManualUnlockAll.setVisibility(View.GONE);
        llPvrManualShowSchedule.setVisibility(View.GONE);

        hideStep2Weekly();
        rbtnPvrManualStep2Once.setChecked(true);
        rbtnPvrManualStep2Once.requestFocus();
    }

    public void showStep2Weekly() {
        Log.d(TAG, "showStep2Weekly");
        CheckBox[] checkBoxes = {
                cbPvrManualStep2Monday, cbPvrManualStep2Tuesday, cbPvrManualStep2Wednesday,
                cbPvrManualStep2Thursday, cbPvrManualStep2Friday, cbPvrManualStep2Saturday,
                cbPvrManualStep2Sunday
        };

        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setChecked(false);
        }

        clPvrManualStep2Weekly.setVisibility(View.VISIBLE);
    }

    public void hideStep2Weekly() {
        Log.d(TAG, "hideStep2Weekly");
        clPvrManualStep2Weekly.setVisibility(View.GONE);
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void jumpToScheduledList() {
        if (jumpToScheduledPageListener != null) {
            jumpToScheduledPageListener.onJumpToScheduledPage();
        } else {
            dismiss();
        }
    }

    private void focusRecycleViewIndex(RecyclerView rv, int index) {
        Log.d(TAG, "focusRecycleViewIndex index:" + index);
        if (rv != null && rv.getVisibility() == View.VISIBLE) {
            rv.postDelayed(() -> {
                View itemView = rv.getLayoutManager().findViewByPosition(index);
                if (itemView != null) {
                    itemView.requestFocus();
                }
            }, 0);
        }
    }

    private List<Channel> getChannelList(GenreData.GenreInfo genreInfo) {
        List<Channel> channelList = new ArrayList<>();
        if ((genreInfo == null) || (genreInfo.id == GenreData.ID_ALL_CHANNELS)) {
            channelList = ChannelUtils.getAllChannels(context);
        } else {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // get specific genre channel list
            channelList = ChannelUtils.getAllChannelsByGenre(context, genreInfo.id);
        }

        return channelList;
    }

    private static final String PVR_MANUAL_PARENTAL_PIN_DIALOG_TAG = "PvrManualParentalPinDialog";
    public void showPvrManualParentalPinDialog() {
        Log.d(TAG, "showPvrManualParentalPinDialog");
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
                isLock = false;
                tuneChannel(currentChannel);

                dialog.dismiss();
            } else {
                dialog.showErrorMessage();
            }
        });

        dialog.show(activity.getSupportFragmentManager(), PVR_MANUAL_PARENTAL_PIN_DIALOG_TAG);
    }

    private void setStep1TitleStyle(boolean hasFocus) {
        if (hasFocus) {
            llPvrManualStep1Title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            ViewUtils.applyTextViewTextEffect(tvPvrManualStep1First, -1, R.color.white, -1);
            ViewUtils.applyTextViewTextEffect(tvPvrManualStep1Second, -1, R.color.white, -1);
            ViewUtils.applyTextViewTextEffect(tvPvrManualStep1Third, 16, R.color.white, Typeface.BOLD);
        } else {
            llPvrManualStep1Title.setBackgroundColor(Color.TRANSPARENT);
            if (include_clPvrManualStep1.getVisibility() == View.VISIBLE) {
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep1First, -1, R.color.color9fb8cb, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep1Second, -1, R.color.color9fb8cb, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep1Third, 15, R.color.white, Typeface.NORMAL);
            } else {
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep1First, -1, R.color.color9fb8cbOpacity30, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep1Second, -1, R.color.color9fb8cbOpacity30, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep1Third, 15, R.color.colorWhiteOpacity30, Typeface.NORMAL);
            }
        }
    }

    private void setStep2TitleStyle(boolean hasFocus) {
        if (hasFocus) {
            llPvrManualStep2Title.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            ViewUtils.applyTextViewTextEffect(tvPvrManualStep2First, -1, R.color.white, -1);
            ViewUtils.applyTextViewTextEffect(tvPvrManualStep2Second, -1, R.color.white, -1);
            ViewUtils.applyTextViewTextEffect(tvPvrManualStep2Third, 16, R.color.white, Typeface.BOLD);
        } else {
            llPvrManualStep2Title.setBackgroundColor(Color.TRANSPARENT);
            if (include_llPvrManualStep2.getVisibility() == View.VISIBLE) {
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep2First, -1, R.color.color9fb8cb, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep2Second, -1, R.color.color9fb8cb, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep2Third, 15, R.color.white, Typeface.NORMAL);
            } else {
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep2First, -1, R.color.color9fb8cbOpacity30, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep2Second, -1, R.color.color9fb8cbOpacity30, -1);
                ViewUtils.applyTextViewTextEffect(tvPvrManualStep2Third, 15, R.color.colorWhiteOpacity30, Typeface.NORMAL);
            }
        }
    }

    private long getStep2StartTimeMillis() {
        long timeMillis = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HHmm", Locale.getDefault());
        String timeString = getFormattedStartTimeString();
        Date date = null;
        try {
            date = sdf.parse(timeString);
        } catch (ParseException e) {
            Log.e(TAG, "getStep2StartTimeMillis: parsing fail", e);
        }

        if (date != null) {
            timeMillis = date.getTime();
        }

        return timeMillis;
    }

    private long getStep2EndTimeMillis() {
        long endTimeMillis = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HHmm", Locale.getDefault());
        String timeString = getFormattedEndTimeString();
        Date date = null;
        try {
            date = sdf.parse(timeString);
        } catch (ParseException e) {
            Log.e(TAG, "getStep2EndTimeMillis: parsing fail", e);
        }

        if (date != null) {
            endTimeMillis = date.getTime();
            long startTimeMillis = getStep2StartTimeMillis();
            if (endTimeMillis <= startTimeMillis) {
                endTimeMillis += 24 * 60 * 60 * 1000L;
            }
        }

        return endTimeMillis;
    }

    private boolean saveRecordingSchedule() {
        long step2StartTimeMills = getStep2StartTimeMillis();
        long step2EndTimeMills = getStep2EndTimeMillis();
        if (step2StartTimeMills == 0 || step2EndTimeMills == 0) {
            Log.d(TAG, "saveRecordingSchedule: invalid start or end time");
            PrimeUtils.showCenterSnackBar(popupView, "Invalid start or end time");
            return false;
        }

        Program program = ProgramUtils.getProgramForDate(context, currentChannel.getId(), step2StartTimeMills);
        String contentRatings;
        if (program != null) {
            contentRatings = TvContractUtils.contentRatingsToString(program.getContentRatings());
        } else {
            contentRatings = TvContentRating.UNRATED.flattenToString();
        }

        int cycle;
        int weekMask = 0;
        if (rbtnPvrManualStep2Once.isChecked()) {
            cycle = BookInfo.BOOK_CYCLE_ONETIME;
        } else if (rbtnPvrManualStep2Daily.isChecked()) {
            cycle = BookInfo.BOOK_CYCLE_DAILY;
        } else if (rbtnPvrManualStep2Weekly.isChecked()) {
            CheckBox[] checkBoxes = {
                    cbPvrManualStep2Monday, cbPvrManualStep2Tuesday, cbPvrManualStep2Wednesday,
                    cbPvrManualStep2Thursday, cbPvrManualStep2Friday, cbPvrManualStep2Saturday,
                    cbPvrManualStep2Sunday
            };

            int[] weekMasks = {
                    BookInfo.BOOK_WEEK_DAY_MONDAY, BookInfo.BOOK_WEEK_DAY_TUESDAY,
                    BookInfo.BOOK_WEEK_DAY_WEDNESDAY, BookInfo.BOOK_WEEK_DAY_THURSDAY,
                    BookInfo.BOOK_WEEK_DAY_FRIDAY, BookInfo.BOOK_WEEK_DAY_SATURDAY,
                    BookInfo.BOOK_WEEK_DAY_SUNDAY
            };

            if (Arrays.stream(checkBoxes).allMatch(CheckBox::isChecked)) {
                cycle = BookInfo.BOOK_CYCLE_DAILY;
            } else if (Arrays.stream(checkBoxes).noneMatch(CheckBox::isChecked)) {
                cycle = BookInfo.BOOK_CYCLE_ONETIME;
            } else {
                cycle = BookInfo.BOOK_CYCLE_WEEKLY;
                for (int i = 0; i < checkBoxes.length ; i++) {
                    if (checkBoxes[i].isChecked()) {
                        weekMask += weekMasks[i];
                    }
                }
            }
        } else {
            cycle = BookInfo.BOOK_CYCLE_ONETIME;
        }

        ScheduledProgramData scheduledProgramData = new ScheduledProgramData(
                currentChannel,
                contentRatings,
                step2StartTimeMills,
                step2EndTimeMills,
                cycle,
                weekMask);
        Uri uri = ScheduledProgramUtils.insertScheduledProgram(context, scheduledProgramData);
        if (uri != null) {
            if (insertRecordingScheduleListener != null) {
                insertRecordingScheduleListener.onInsertRecordingSchedule();
            }

            return true;
        } else {
            return false;
        }
    }

    private String getSuccessTimeString() {
        return etPvrManualStep2StartHour.getText() + ":" + etPvrManualStep2StartMinute.getText() +
                "-" +
                etPvrManualStep2EndHour.getText() + ":" + etPvrManualStep2EndMinute.getText();
    }

    private String getSuccessModeString() {
        String successModeString;
        RadioButton checkedRadioButton
                = popupView.findViewById(rgPvrManualStep2Mode.getCheckedRadioButtonId());

        if (checkedRadioButton == rbtnPvrManualStep2Weekly) {
            Button[] buttons = {
                    btnPvrManualStep2Monday, btnPvrManualStep2Tuesday, btnPvrManualStep2Wednesday,
                    btnPvrManualStep2Thursday, btnPvrManualStep2Friday, btnPvrManualStep2Saturday,
                    btnPvrManualStep2Sunday
            };

            CheckBox[] checkBoxes = {
                    cbPvrManualStep2Monday, cbPvrManualStep2Tuesday, cbPvrManualStep2Wednesday,
                    cbPvrManualStep2Thursday, cbPvrManualStep2Friday, cbPvrManualStep2Saturday,
                    cbPvrManualStep2Sunday
            };

            if (Arrays.stream(checkBoxes).allMatch(CheckBox::isChecked)) {
                successModeString = rbtnPvrManualStep2Daily.getText().toString();
            } else if (Arrays.stream(checkBoxes).noneMatch(CheckBox::isChecked)) {
                successModeString = rbtnPvrManualStep2Once.getText().toString();
            } else {
                StringBuilder successModeBuilder
                        = new StringBuilder(rbtnPvrManualStep2Weekly.getText().toString());
                successModeBuilder.append("(");
                for (int i = 0, checkedCount = 0; i < checkBoxes.length ; i++) {
                    if (checkBoxes[i].isChecked()) {
                        if (checkedCount > 0) {
                            successModeBuilder.append(", ");
                        }

                        String week = buttons[i].getText().toString();
                        week = week.replace("\n","");
                        successModeBuilder.append(week);
                        checkedCount++;
                    }
                }

                successModeBuilder.append(")");
                successModeString = successModeBuilder.toString();
            }
        } else {
            successModeString = checkedRadioButton.getText().toString();
        }

        return successModeString;
    }

    private void setNextValidRecordingTime() {
        // Step in minutes, can be 5, 10, 15, etc.
        int minuteStep = RECORD_MINUTE_STEP;

        Calendar calendar = Calendar.getInstance();

        int currentMinute = calendar.get(Calendar.MINUTE);

        // Calculate the next valid start minute based on step
        int nextStartMinute = getNextValidMinute(currentMinute, minuteStep);

        // If minute wrapped, add 1 hour to the calendar
        if (nextStartMinute < currentMinute) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        // Set the calendar to the start time
        calendar.set(Calendar.MINUTE, nextStartMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Update the start date field
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        etPvrManualStep2StartDate.setText(sdf.format(calendar.getTime()));

        // Format start hour and minute for display
        String strStartHour = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String strStartMinute = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MINUTE));

        // Calculate end minute (next step)
        int nextEndMinute = (nextStartMinute + minuteStep) % 60;
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);

        // If end minute wrapped to next hour
        if (nextEndMinute <= nextStartMinute) {
            endHour = (endHour + 1) % 24;
        }

        // Format end hour and minute
        String strEndHour = String.format(Locale.getDefault(), "%02d", endHour);
        String strEndMinute = String.format(Locale.getDefault(), "%02d", nextEndMinute);

        // Update start and end hour/minute UI fields
        etPvrManualStep2StartHour.setText(strStartHour);
        etPvrManualStep2EndHour.setText(strEndHour);
        etPvrManualStep2StartMinute.setText(strStartMinute);
        etPvrManualStep2EndMinute.setText(strEndMinute);

        // Save the last valid values
        strLastValidStartHour = strStartHour;
        strLastValidEndHour = strEndHour;
        strLastValidStartMinute = strStartMinute;
        strLastValidEndMinute = strEndMinute;
    }

    private boolean validateHour(EditText editText) {
        String text = editText.getText().toString();
        int errorMsgResId = -1;

        if (text.isBlank()) {
            errorMsgResId = 0;
        } else {
            int hour = parseInt(text);
            if (hour < 0 || hour > 23) {
                errorMsgResId = R.string.pvr_manual_step2_hour_out_of_range;
            }
        }

        if (errorMsgResId != -1) {
            if (errorMsgResId != 0) {
                PrimeUtils.showCenterSnackBar(editText, errorMsgResId);
            }
            return false;
        }

        int id = editText.getId();
        if (id == R.id.etPvrManualStep2StartHour) {
            strLastValidStartHour = text;
        } else if (id == R.id.etPvrManualStep2EndHour) {
            strLastValidEndHour = text;
        }

        return true;
    }

    private boolean validateMinute(EditText editText) {
        String text = editText.getText().toString();
        int errorMsgResId = -1;

        if (text.isBlank()) {
            errorMsgResId = 0;
        } else {
            int minute = parseInt(text);
            if (minute < 0 || minute > 59) {
                errorMsgResId = R.string.pvr_manual_step2_minute_out_of_range;
            }
        }

        if (errorMsgResId != -1) {
            if (errorMsgResId != 0) {
                PrimeUtils.showCenterSnackBar(editText, errorMsgResId);
            }
            return false;
        }

        int id = editText.getId();
        if (id == R.id.etPvrManualStep2StartMinute) {
            strLastValidStartMinute = text;
        } else if (id == R.id.etPvrManualStep2EndMinute) {
            strLastValidEndMinute = text;
        }

        return true;
    }

    private int getNextValidMinute(int currentMinute, int step) {

        int next = ((currentMinute / step) + 1) * step;

        // wrap around if >= 60
        if (next >= 60) {
            next = 0;
        }

        return next;
    }

    private int parseInt(String s) {
        if (s == null || s.isEmpty()) {
            return -1;
        }

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    private boolean validateRecordDuration() {
        int startHour = parseInt(etPvrManualStep2StartHour.getText().toString());
        int endHour = parseInt(etPvrManualStep2EndHour.getText().toString());
        int startMinute = parseInt(etPvrManualStep2StartMinute.getText().toString());
        int endMinute = parseInt(etPvrManualStep2EndMinute.getText().toString());
        if (startHour < 0 || endHour < 0 || startMinute < 0 || endMinute < 0) {
            return false;
        }

        int startTotalMin = startHour * 60 + startMinute;
        int endTotalMin   = endHour * 60 + endMinute;

        // cross-day case
        if (endTotalMin <= startTotalMin) {
            endTotalMin += 24 * 60;
        }

        int durationMin = endTotalMin - startTotalMin;
        int durationLimitMin = 4 * 60;

        boolean isValid = durationMin <= durationLimitMin;
        if (!isValid) {
            PrimeUtils.showCenterSnackBar(
                    popupView,
                    R.string.pvr_manual_step2_max_duration_4_hours);
        }

        return isValid;
    }

    private boolean validateRecordStartTime() {
        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy.MM.dd HHmm", Locale.getDefault());

        String timeString = getFormattedStartTimeString();

        Date date = null;
        try {
            date = sdf.parse(timeString);
        } catch (ParseException e) {
            Log.e(TAG, "validateRecordStartTime: parsing date failed", e);
        }

        if (date == null) {
            return false;
        }

        // Start time
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(date);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        // Current time
        Calendar now = Calendar.getInstance();

        // Start time must be strictly after now
        boolean isValid = now.before(startTime);
        if (!isValid) {
            PrimeUtils.showCenterSnackBar(
                    popupView,
                    R.string.pvr_manual_step2_schedule_must_after_now);
        }

        return isValid;
    }

    private String getFormattedStartTimeString() {
        String strDate = etPvrManualStep2StartDate.getText().toString();
        int hour = parseInt(etPvrManualStep2StartHour.getText().toString());
        String strHour = String.format(Locale.getDefault(), "%02d", hour);

        int minute = parseInt(etPvrManualStep2StartMinute.getText().toString());
        String strMinute = String.format(Locale.getDefault(), "%02d", minute);

        return strDate + " " + strHour + strMinute; // yyyy.MM.dd HHmm
    }

    private String getFormattedEndTimeString() {
        String strDate = etPvrManualStep2StartDate.getText().toString();
        int hour = parseInt(etPvrManualStep2EndHour.getText().toString());
        String strHour = String.format(Locale.getDefault(), "%02d", hour);

        int minute = parseInt(etPvrManualStep2EndMinute.getText().toString());
        String strMinute = String.format(Locale.getDefault(), "%02d", minute);

        return strDate + " " + strHour + strMinute; // yyyy.MM.dd HHmm
    }
}
