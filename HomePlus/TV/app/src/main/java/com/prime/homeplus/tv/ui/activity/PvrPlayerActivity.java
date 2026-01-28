package com.prime.homeplus.tv.ui.activity;

import android.content.ContentUris;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.PlaybackParams;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.homeplus.tv.BuildConfig;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.RecordedProgramData;
import com.prime.homeplus.tv.ui.fragment.PvrPlayerDialogFragment;
import com.prime.homeplus.tv.utils.ProgramRatingUtils;
import com.prime.homeplus.tv.utils.RecordedProgramUtils;
import com.prime.homeplus.tv.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PvrPlayerActivity extends AppCompatActivity {
    private static final String TAG = "HOMEPLUS_PVR_PLAYER";

    public static final String EXTRA_RECORDED_PROGRAM_ID = "extra_recorded_program_id";
    private RecordedProgramData recordedProgramData;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hideDashboardRunnable = this::hideDashboard;
    private final Runnable mPerformActualSeekRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Seek to accumulated target: " + mPendingTargetPositionMs);
            pvrPlayerTvView.timeShiftSeekTo(mPendingTargetPositionMs);

            mCurrentPositionMs = mPendingTargetPositionMs;
            mAccumulatedSeekOffsetMs = 0;
            handler.postDelayed(mResetSeekRunnable, RESET_SEEK_DELAY_MS);
        }
    };
    private final Runnable mResetSeekRunnable = () -> {
        resetSeek();
        updateAndShowPlayingStatus();
    };

    private static final long AUTO_HIDE_DASHBOARD_TIMEOUT_MS = 5 * 1000;
    private static final long SEEK_STEP_MIN = 60 * 1000;

    private static final long RESET_SEEK_DELAY_MS = 2500;
    private static final long PERFORM_ACTUAL_SEEK_DELAY_MS = 500;
    private int mSeekPressCount = 0;
    private int mCurrentSeekStep = 0;
    private long mAccumulatedSeekOffsetMs = 0;
    private long mPendingTargetPositionMs = 0;
    private boolean mIsRecordedProgramNowRecording = false;
    private boolean mIsUserSeeking = false;
    private RecordedProgramObserver mRecordedProgramObserver;

    // Main
    private TvView pvrPlayerTvView;
    private ImageView ivPvrPlayerPlayStatus;
    private TextView tvPvrPlayerPlayStatus;

    // Dashboard
    private ConstraintLayout include_clPvrPlayerDashboard;
    private ProgressBar pbPvrPlayer;
    private ImageView ivPvrPlayerNowRecording;
    private TextView tvPvrPlayerStartTime, tvPvrPlayerDuration, tvPvrPlayerProgramName,
            tvPvrPlayerYellowBilingual, tvPvrPlayerRedPause;

    // Program info
    private ConstraintLayout include_clPvrPlayerProgramInfo;
    private ImageView ivPvrProgramInfoChannelRating, ivPvrProgramInfoNowRecording;
    private TextView tvPvrProgramInfoChannelNumber, tvPvrProgramInfoChannelName,
            tvPvrProgramInfoProgramName, tvPvrProgramInfoDescription;
    private Button btnPvrProgramInfoClose;

    // Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - BUILD_TIME:" + BuildConfig.BUILD_TIME);

        setContentView(R.layout.activity_pvr_player);

        initPvrPlayerMainViews();
        initPvrPlayerDashboardViews();
        initPvrPlayerProgramInfoViews();

        long recordedProgramId = -1;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_RECORDED_PROGRAM_ID)) {
            String recordingIdStr = intent.getStringExtra(EXTRA_RECORDED_PROGRAM_ID);
            if (!TextUtils.isEmpty(recordingIdStr)) {
                try {
                    recordedProgramId = Long.parseLong(recordingIdStr);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "Invalid recording ID format: " + recordingIdStr, e);
                }
            } else {
                Log.d(TAG, "Recording ID extra is empty");
            }
        } else {
            Log.d(TAG, "Intent or EXTRA_RECORDING_ID is missing");
        }

        if (recordedProgramId <= 0) {
            Log.d(TAG, "Invalid recordedProgramId: " + recordedProgramId);
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // error handling
            showPvrPlayerDialog(PvrPlayerDialogFragment.PvrPlayerDialogState.STOP_PLAYBACK);
            return;
        }

        playRecordedProgramById(recordedProgramId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");

        if (intent != null) {
            setIntent(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        pvrPlayerTvView.reset();
        pvrPlayerTvView.setTimeShiftPositionCallback(null);
        unregisterRecordedProgramObserver();
    }

    private void initPvrPlayerMainViews() {
        Log.d(TAG, "initPvrPlayerViews");
        pvrPlayerTvView = findViewById(R.id.pvrPlayerTvView);
        ivPvrPlayerPlayStatus = findViewById(R.id.ivPvrPlayerPlayStatus);
        tvPvrPlayerPlayStatus = findViewById(R.id.tvPvrPlayerPlayStatus);

        pvrPlayerTvView.setTimeShiftPositionCallback(new TvView.TimeShiftPositionCallback() {
            @Override
            public void onTimeShiftCurrentPositionChanged(String inputId, long timeMs) {
//                Log.d(TAG, "onTimeShiftCurrentPositionChanged: " + timeMs);
                mCurrentPositionMs = timeMs;

                if (!mIsUserSeeking) {
                    pbPvrPlayer.setProgress((int) (timeMs / 1000));
                    tvPvrPlayerStartTime.setText(TimeUtils.formatMillisToTime(timeMs));
                }
            }

            @Override
            public void onTimeShiftStartPositionChanged(String inputId, long timeMs) {
//                Log.d(TAG, "onTimeShiftStartPositionChanged: " + timeMs);
                super.onTimeShiftStartPositionChanged(inputId, timeMs);
            }
        });

        pvrPlayerTvView.setCallback(new TvView.TvInputCallback() {
            @Override
            public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
                Log.d(TAG, "onTracksChanged: " + tracks);
                if (mAudioTracks == null) {
                    mAudioTracks = new ArrayList<>();
                } else {
                    mAudioTracks.clear();
                }

                for (TvTrackInfo track : tracks) {
                    if (track.getType() == TvTrackInfo.TYPE_AUDIO) {
                        mAudioTracks.add(track);
                    }
                }
            }

            @Override
            public void onTrackSelected(String inputId, int type, String trackId) {
                Log.d(TAG, "onTrackSelected: " + trackId);
                if (mAudioTracks == null) {
                    return;
                }

                for (TvTrackInfo tvTrackInfo : mAudioTracks) {
                    if (tvTrackInfo.getId().equals(trackId)) {
                        String lang = tvTrackInfo.getLanguage();
                        if (lang.equals("eng")) {
                            tvPvrPlayerYellowBilingual.setText(getString(R.string.text_English));
                        } else if (lang.equals("chi")) {
                            tvPvrPlayerYellowBilingual.setText(getString(R.string.text_Chinese));
                        } else {
                            tvPvrPlayerYellowBilingual.setText(getString(R.string.none));
                        }

                        showDashboard();
                        break;
                    }
                }
            }
        });
    }

    private void initPvrPlayerDashboardViews() {
        include_clPvrPlayerDashboard = findViewById(R.id.include_clPvrPlayerDashboard);
        pbPvrPlayer = findViewById(R.id.pbPvrPlayer);

        ivPvrPlayerNowRecording = findViewById(R.id.ivPvrPlayerNowRecording);

        tvPvrPlayerStartTime = findViewById(R.id.tvPvrPlayerStartTime);
        tvPvrPlayerDuration = findViewById(R.id.tvPvrPlayerDuration);
        tvPvrPlayerProgramName = findViewById(R.id.tvPvrPlayerProgramName);
        tvPvrPlayerYellowBilingual = findViewById(R.id.tvPvrPlayerYellowBilingual);
        tvPvrPlayerRedPause = findViewById(R.id.tvPvrPlayerRedPause);
    }

    private void initPvrPlayerProgramInfoViews() {
        include_clPvrPlayerProgramInfo = findViewById(R.id.include_clPvrPlayerProgramInfo);
        ivPvrProgramInfoChannelRating = findViewById(R.id.ivPvrProgramInfoChannelRating);
        ivPvrProgramInfoNowRecording = findViewById(R.id.ivPvrProgramInfoNowRecording);

        tvPvrProgramInfoChannelNumber = findViewById(R.id.tvPvrProgramInfoChannelNumber);
        tvPvrProgramInfoChannelName = findViewById(R.id.tvPvrProgramInfoChannelName);
        tvPvrProgramInfoProgramName = findViewById(R.id.tvPvrProgramInfoProgramName);
        tvPvrProgramInfoDescription = findViewById(R.id.tvPvrProgramInfoDescription);

        btnPvrProgramInfoClose = findViewById(R.id.btnPvrProgramInfoClose);
        btnPvrProgramInfoClose.setOnClickListener((v) -> {
            showDashboard();
        });

        btnPvrProgramInfoClose.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    showDashboard();
                    return true;
                }
            }
            return false;
        });
    }

    private void playRecordedProgramById(long recordedProgramId) {
        recordedProgramData = RecordedProgramUtils.getRecordedProgram(getApplicationContext(), recordedProgramId);
        if (recordedProgramData != null) {
            mIsRecordedProgramNowRecording =
                    recordedProgramData.getPesiRecordStatus() == PvrRecFileInfo.RECORD_STATUS_RECORDING;
            updateDashboardData();
            updateProgramInfoData();
            showDashboard();
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // play recorded program
            Uri recordedProgramUri = TvContractCompat.buildRecordedProgramUri(recordedProgramId);
            pvrPlayerTvView.timeShiftPlay(recordedProgramData.getInputId(), recordedProgramUri);

            if (mIsRecordedProgramNowRecording) {
                // register current TIF recorded program observer for updating now recording info
                registerRecordedProgramObserver(recordedProgramUri, handler);
            }
        } else {
            Log.d(TAG, "No recorded program found for recordedProgramId=" + recordedProgramId);
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // error handling
            showPvrPlayerDialog(PvrPlayerDialogFragment.PvrPlayerDialogState.STOP_PLAYBACK);
        }
    }

    private void updateAndShowPlayingStatus() {
        int statusIcon;
        String redKeyMsg = getString(R.string.pvr_play);
        String speedInfo = "";

        if (mSeekPressCount > 0) {
            statusIcon = mCurrentSeekStep > 0 ?
                    R.drawable.layer_pvr_step_forward : R.drawable.layer_pvr_step_backward;
            speedInfo = getString(R.string.pvr_format_step_mins, mCurrentSeekStep);
        } else if (mCurrentSpeed != 1.0f) {
            int absSpeed = (int)Math.abs(mCurrentSpeed);
            speedInfo = String.format(Locale.getDefault(), "X %d", absSpeed);
            statusIcon = mCurrentSpeed > 0 ?
                    R.drawable.layer_pvr_fast_forward : R.drawable.layer_pvr_fast_rewind;
        } else {
            if (mIsPlaying) {
                statusIcon = R.drawable.layer_pvr_pause;
                redKeyMsg = getString(R.string.pvr_pause);
            } else {
                statusIcon = R.drawable.layer_pvr_play;
                redKeyMsg = getString(R.string.pvr_play);
            }
        }

        tvPvrPlayerRedPause.setText(redKeyMsg);
        ivPvrPlayerPlayStatus.setImageResource(statusIcon);
        tvPvrPlayerPlayStatus.setText(speedInfo);
        ivPvrPlayerPlayStatus.setVisibility(View.VISIBLE);
        tvPvrPlayerPlayStatus.setVisibility(View.VISIBLE);
    }

    private void hidePlayingStatus() {
        ivPvrPlayerPlayStatus.setVisibility(View.GONE);
        tvPvrPlayerPlayStatus.setVisibility(View.GONE);
    }

    private void updateDashboardData() {
        mPlaybackStartTimeMs = 0;
        mPlaybackEndTimeMs = recordedProgramData.getRecordingDurationMillis();

        tvPvrPlayerStartTime.setText(getString(R.string.pvr_start_time));
        tvPvrPlayerDuration.setText(TimeUtils.formatMillisToTime(recordedProgramData.getRecordingDurationMillis()));
        tvPvrPlayerProgramName.setText(recordedProgramData.getTitle());

        pbPvrPlayer.setMax((int) (mPlaybackEndTimeMs / 1000));

        if (mIsRecordedProgramNowRecording) {
            ivPvrPlayerNowRecording.setVisibility(View.VISIBLE);
        }
    }

    private void showDashboard() {
        include_clPvrPlayerDashboard.setVisibility(View.VISIBLE);
        include_clPvrPlayerProgramInfo.setVisibility(View.GONE);

        updateAndShowPlayingStatus();
        scheduleHideDashboard();
    }

    private void hideDashboard() {
        include_clPvrPlayerDashboard.setVisibility(View.GONE);

        if (mCurrentSpeed == 1.0f && mIsPlaying) {
            hidePlayingStatus();
        }
    }

    private void scheduleHideDashboard() {
        handler.removeCallbacks(hideDashboardRunnable);
        handler.postDelayed(hideDashboardRunnable, AUTO_HIDE_DASHBOARD_TIMEOUT_MS);
    }

    private void updateProgramInfoData() {
        tvPvrProgramInfoChannelNumber.setText(recordedProgramData.getChannelNumber());
        tvPvrProgramInfoChannelName.setText(recordedProgramData.getChannelName());
        tvPvrProgramInfoProgramName.setText(recordedProgramData.getTitle());
        tvPvrProgramInfoDescription.setText(recordedProgramData.getLongDescription());

        int ratingAge = ProgramRatingUtils.getStrictestDvbAge(recordedProgramData.getContentRatings());
        ivPvrProgramInfoChannelRating.setImageResource(ProgramRatingUtils.getRatingIcon(ratingAge));

        if (mIsRecordedProgramNowRecording) {
            ivPvrProgramInfoNowRecording.setVisibility(View.VISIBLE);
        }
    }

    private void showProgramInfo() {
        include_clPvrPlayerProgramInfo.setVisibility(View.VISIBLE);
        btnPvrProgramInfoClose.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d(TAG, "onKeyDown: event = " + event);
        if (mSeekPressCount > 0 &&
                (keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == KeyEvent.KEYCODE_PROG_RED ||
                keyCode == KeyEvent.KEYCODE_PROG_GREEN ||
                keyCode == KeyEvent.KEYCODE_PROG_BLUE)) {
            // cancel seek if pause/play/fastForward/rewind
            handler.removeCallbacks(mPerformActualSeekRunnable);
            handler.removeCallbacks(mResetSeekRunnable);
            resetSeek();
            updateAndShowPlayingStatus();

            Log.d(TAG, "Seek cancelled due to key event = " + event);
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_RED || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            handlePlayPause();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            handleRewind();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW ||
                keyCode == KeyEvent.KEYCODE_F1/*rcu audio key*/) {
            handleAudioSwitch();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            handleFastForward();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO) {
            showProgramInfo();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            handleSeekRelative(false);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            handleSeekRelative(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            showPvrPlayerDialog(PvrPlayerDialogFragment.PvrPlayerDialogState.STOP_PLAYBACK);
            return true;
        }

        return false;
    }

    private boolean mIsPlaying = true;
    private float mCurrentSpeed = 1.0f;
    private long mPlaybackStartTimeMs = -1;
    private long mPlaybackEndTimeMs = -1;
    private long mCurrentPositionMs = 0;

    private List<TvTrackInfo> mAudioTracks;
    private int mCurrentAudioTrackIndex = 0;
    private void handlePlayPause() {
        Log.d(TAG, "before handlePlayPause mIsPlaying:" + mIsPlaying);
        if (mIsPlaying) {
            timeShiftPause();
        } else {
            timeShiftResume();
        }
        Log.d(TAG, "after handlePlayPause mIsPlaying:" + mIsPlaying);
        showDashboard();
    }

    private void handleRewind() {
        Log.d(TAG, "before handleRewind mCurrentSpeed:" + mCurrentSpeed);
        if (mCurrentSpeed == -32.0f) { // (x-32 -> pause)
            timeShiftResume();
        } else if (mCurrentSpeed >= 0.0f) { // (normal/pause/fast forward -> x-2)
            mCurrentSpeed = -2.0f;
        } else { // (x-2 -> x-4 -> ...)
            mCurrentSpeed *= 2.0f;
        }

        if (!mIsPlaying) {
            mIsPlaying = true;
            pvrPlayerTvView.timeShiftResume();
        }

        setSpeed(mCurrentSpeed);
        Log.d(TAG, "after handleRewind mCurrentSpeed:" + mCurrentSpeed);
        showDashboard();
    }

    private void handleFastForward() {
        Log.d(TAG, "before handleFastForward mCurrentSpeed:" + mCurrentSpeed);
        if (mCurrentSpeed == 32.0f) { // (x32 -> pause)
            timeShiftResume();
        } else if (mCurrentSpeed <= 1.0f) { // (normal/pause/rewind -> x2)
            mCurrentSpeed = 2.0f;
        } else { // (x2 -> x4 -> ...)
            mCurrentSpeed *= 2.0f;
        }

        if (!mIsPlaying) {
            mIsPlaying = true;
            pvrPlayerTvView.timeShiftResume();
        }

        setSpeed(mCurrentSpeed);
        Log.d(TAG, "after handleFastForward mCurrentSpeed:" + mCurrentSpeed);
        showDashboard();
    }

    private void timeShiftPause() {
        mCurrentSpeed = 1.0f;
        setSpeed(mCurrentSpeed);
        mIsPlaying = false;

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // pause
        pvrPlayerTvView.timeShiftPause();
    }

    private void timeShiftResume() {
        mCurrentSpeed = 1.0f;
        setSpeed(mCurrentSpeed);
        mIsPlaying = true;

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // resume
        pvrPlayerTvView.timeShiftResume();
    }

    private void setSpeed(float speed) {
        Log.d(TAG, "Setting playback speed to: " + speed + "x");

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // set speed
        PlaybackParams playbackParams = new PlaybackParams();
        playbackParams.setSpeed(speed);
        pvrPlayerTvView.timeShiftSetPlaybackParams(playbackParams);
    }

    private void handleSeekRelative(long offsetMs) {
        long currentPositionMs = mCurrentPositionMs;
        long targetPositionMs = currentPositionMs + offsetMs;
        Log.d(TAG, "handleSeekRelative from " + currentPositionMs + " to " + targetPositionMs);

        if (targetPositionMs < mPlaybackStartTimeMs) {
            targetPositionMs = mPlaybackStartTimeMs;
        }
        if (targetPositionMs > mPlaybackEndTimeMs) {
            targetPositionMs = mPlaybackEndTimeMs;
        }

        Log.d(TAG, "Seeking to: " + targetPositionMs);
        mCurrentPositionMs = targetPositionMs;
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // seek
        pvrPlayerTvView.timeShiftSeekTo(targetPositionMs);

        showDashboard();
    }

    private void handleSeekRelative(boolean isForward) {
        mIsUserSeeking = true;
        handler.removeCallbacks(mPerformActualSeekRunnable);
        handler.removeCallbacks(mResetSeekRunnable);

        if (mSeekPressCount == 0) {
            mAccumulatedSeekOffsetMs = 0;
        }

        mSeekPressCount++;

        long stepMs;
        if (mSeekPressCount <= 5) {
            stepMs = SEEK_STEP_MIN; // 1 min
        } else if (mSeekPressCount <= 8) {
            stepMs = 3 * SEEK_STEP_MIN; // 3 mins
        } else {
            stepMs = 10 * SEEK_STEP_MIN; // 10 mins
        }

        if (isForward) {
            mAccumulatedSeekOffsetMs += stepMs;
        } else {
            mAccumulatedSeekOffsetMs -= stepMs;
        }

        mPendingTargetPositionMs = mCurrentPositionMs + mAccumulatedSeekOffsetMs;
        // check border
        mPendingTargetPositionMs = Math.max(
                mPlaybackStartTimeMs,
                Math.min(mPendingTargetPositionMs, mPlaybackEndTimeMs));

        // update UI
        mCurrentSeekStep = isForward ? (int)(stepMs / SEEK_STEP_MIN) : -(int)(stepMs / SEEK_STEP_MIN);
        pbPvrPlayer.setProgress((int) (mPendingTargetPositionMs / 1000));
        tvPvrPlayerStartTime.setText(TimeUtils.formatMillisToTime(mPendingTargetPositionMs));
        showDashboard();

        // delay to do seek
        handler.postDelayed(mPerformActualSeekRunnable, PERFORM_ACTUAL_SEEK_DELAY_MS);
    }

    private void resetSeek() {
        mSeekPressCount = 0;
        mCurrentSeekStep = 0;
        mAccumulatedSeekOffsetMs = 0;
        mIsUserSeeking = false;
    }

    private void handleAudioSwitch() {
        Log.d(TAG, "handleAudioSwitch");
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // get audio tracks
        if (mAudioTracks == null) { // != null means it has been updated in onTracksChanged()
            mAudioTracks = pvrPlayerTvView.getTracks(TvTrackInfo.TYPE_AUDIO);
        }

        if (mAudioTracks != null && !mAudioTracks.isEmpty()) {
            Log.d(TAG, "handleAudioSwitch - find " + mAudioTracks.size() + " audio tracks");
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // get current audio track id
            String currentTrackId = pvrPlayerTvView.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            for (int i = 0; i < mAudioTracks.size(); i++) {
                if (mAudioTracks.get(i).getId().equals(currentTrackId)) {
                    mCurrentAudioTrackIndex = i;
                    break;
                }
            }
        }

        if (mAudioTracks == null || mAudioTracks.size() <= 1) {
            Log.d(TAG, "handleAudioSwitch - no other audio");
            return;
        }

        mCurrentAudioTrackIndex = (mCurrentAudioTrackIndex + 1) % mAudioTracks.size();

        TvTrackInfo nextTrack = mAudioTracks.get(mCurrentAudioTrackIndex);
        String nextTrackId = nextTrack.getId();

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // set audio track
        pvrPlayerTvView.selectTrack(TvTrackInfo.TYPE_AUDIO, nextTrackId);

        CharSequence lang = nextTrack.getLanguage();
        if (lang == null) lang = "音軌 " + (mCurrentAudioTrackIndex + 1);
        Log.d(TAG, "handleAudioSwitch - lang:" + lang + ", mCurrentAudioTrackIndex:" + mCurrentAudioTrackIndex);
    }

    private static final String PVR_PLAYER_DIALOG_TAG = "PvrPlayerDialog";
    public void showPvrPlayerDialog(PvrPlayerDialogFragment.PvrPlayerDialogState state) {
        PvrPlayerDialogFragment dialog = new PvrPlayerDialogFragment(state);
        dialog.setPvrPlayerListener(new PvrPlayerDialogFragment.OnPvrPlayerListener() {
            @Override
            public void onPvrPlayerContinue() {
                Log.d(TAG, "PvrPlayerDialogFragment onPvrPlayerContinue");
            }

            @Override
            public void onPvrPlayerFromStart() {
                Log.d(TAG, "PvrPlayerDialogFragment onPvrPlayerFromStart");
            }

            @Override
            public void onPvrPlayerExit() {
                Log.d(TAG, "PvrPlayerDialogFragment onPvrPlayerExit");
                backToPvrMenu();
            }

            @Override
            public void onPvrPlayerReplay() {
                Log.d(TAG, "PvrPlayerDialogFragment onPvrPlayerReplay");
                pvrPlayerTvView.reset();
                mIsPlaying = true;
                mCurrentSpeed = 1.0f;
                mCurrentPositionMs = 0;
                resetSeek();
                playRecordedProgramById(recordedProgramData.getId());
                dialog.dismiss();
            }

            @Override
            public void onPvrPlayerConfirmStop() {
                Log.d(TAG, "PvrPlayerDialogFragment onPvrPlayerConfirmStop");
                backToPvrMenu();
            }

            @Override
            public void onPvrPlayerCancel() {
                Log.d(TAG, "PvrPlayerDialogFragment onPvrPlayerCancel");
                dialog.dismiss();
            }
        });

        dialog.show(getSupportFragmentManager(), PVR_PLAYER_DIALOG_TAG);
    }

    private void backToPvrMenu() {
        Log.d(TAG, "backToPvrMenu");
        Intent intent = new Intent(PvrPlayerActivity.this, PvrActivity.class);
        try {
            PvrPlayerActivity.this.startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
        }
    }

    private void handlePlayToBegin() {
        // primeDtv will resume playback itself
        // update status only
        mIsPlaying = true;
        mCurrentSpeed = 1.0f;

        // update ui
        runOnUiThread(this::showDashboard);
    }

    private void handlePlayToEnd() {
        if (mIsRecordedProgramNowRecording) {
            // keep playing if record is still recording
            // primeDtv will resume playback itself
            // update status only
            mIsPlaying = true;
            mCurrentSpeed = 1.0f;

            // update ui
            runOnUiThread(this::showDashboard);
        } else {
            showPvrPlayerDialog(PvrPlayerDialogFragment.PvrPlayerDialogState.END_PLAYBACK);
        }
    }

    public void handleTvMessage(TVMessage msg) {
//        Log.d(TAG, "handleTvMessage: " + msg.getMsgType());
        switch (msg.getMsgType()) {
            case TVMessage.TYPE_PVR_PLAY_TO_BEGIN: {
                Log.d(TAG, "handleTvMessage: TYPE_PVR_PLAY_TO_BEGIN");
                handlePlayToBegin();
            }
            break;
            case TVMessage.TYPE_PVR_PLAY_EOF: {
                Log.d(TAG, "handleTvMessage: TYPE_PVR_PLAY_EOF");
                handlePlayToEnd();
            }
            break;
        }
    }

    private class RecordedProgramObserver extends ContentObserver {

        public RecordedProgramObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "onChange: RecordedProgram uri = " + uri);
            long recordingId = recordedProgramData.getId();
            if (uri != null) {
                long changedRecordingId = ContentUris.parseId(uri);
                if (recordingId != changedRecordingId) {
                    Log.e(TAG, "onChange: recording id mismatch");
                    return;
                }
            }

            // update record duration
            mPlaybackEndTimeMs = RecordedProgramUtils.getCurrentRecordedProgramDuration(
                    PvrPlayerActivity.this, recordingId);
            recordedProgramData.setRecordingDurationMillis(mPlaybackEndTimeMs);

            tvPvrPlayerDuration.setText(TimeUtils.formatMillisToTime(mPlaybackEndTimeMs));
            pbPvrPlayer.setMax((int) (mPlaybackEndTimeMs / 1000)); // progress bar max

            // show/hide now recording image
            long recordStatus = RecordedProgramUtils.getCurrentRecordedProgramStatus(
                    PvrPlayerActivity.this, recordingId);
            recordedProgramData.setInternalProviderFlag1(recordStatus);
            if (recordStatus == PvrRecFileInfo.RECORD_STATUS_RECORDING) {
                ivPvrProgramInfoNowRecording.setVisibility(View.VISIBLE);
                ivPvrPlayerNowRecording.setVisibility(View.VISIBLE);
            } else {
                ivPvrProgramInfoNowRecording.setVisibility(View.GONE);
                ivPvrPlayerNowRecording.setVisibility(View.GONE);
                mIsRecordedProgramNowRecording = false;
            }
        }
    }

    private void registerRecordedProgramObserver(Uri uri, Handler handler) {
        if (mRecordedProgramObserver != null) return; // already registered

        mRecordedProgramObserver = new RecordedProgramObserver(handler);
        getContentResolver().registerContentObserver(uri, true, mRecordedProgramObserver);
    }

    private void unregisterRecordedProgramObserver() {
        if (mRecordedProgramObserver != null) {
            getContentResolver().unregisterContentObserver(mRecordedProgramObserver);
            mRecordedProgramObserver = null;
        }
    }
}
