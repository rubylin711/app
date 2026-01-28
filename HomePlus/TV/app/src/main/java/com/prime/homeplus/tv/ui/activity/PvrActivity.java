package com.prime.homeplus.tv.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.tv.BuildConfig;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.adapter.PvrRecordedListAdapter;
import com.prime.homeplus.tv.adapter.PvrMenuAdapter;
import com.prime.homeplus.tv.adapter.PvrScheduledListAdapter;
import com.prime.homeplus.tv.data.PvrMenuData;
import com.prime.homeplus.tv.data.RecordedProgramData;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.manager.ListPaginationManager;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;
import com.prime.homeplus.tv.ui.component.PvrManualRecordingPopup;
import com.prime.homeplus.tv.ui.fragment.ParentalPinDialogFragment;
import com.prime.homeplus.tv.ui.fragment.ProgramDeleteDialogFragment;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.RecordedProgramUtils;
import com.prime.homeplus.tv.utils.StringUtils;
import com.prime.homeplus.tv.utils.TimeUtils;
import com.prime.homeplus.tv.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PvrActivity extends AppCompatActivity {
    private static final String TAG = "HOMEPLUS_PVR";

    private static final int ANIMATION_DURATION_MS = 300;
    private static final int RIGHT_SIDE_PREVIEW_SIZE = 708;
    private static final int RIGHT_SIDE_LIST_PREVIEW_SIZE = 580;
    private static final int RIGHT_SIDE_FULL_SIZE = 902;
    private static final int RIGHT_SIDE_LIST_FULL_SIZE = 778;
    private final int RECORDED_LIST_ITEMS_PER_PAGE = 8;
    private final int SCHEDULED_LIST_ITEMS_PER_PAGE = 8;

    private float density;
    private Handler handler = new Handler(Looper.getMainLooper());

    private PvrMenuAdapter pvrMenuAdapter;
    private PrimeDtvServiceInterface primeDtv;

    List<Channel> allChannels;

    // Menu
    ConstraintLayout clPvrList, clPvrListTitle;
    RecyclerView rvPvrMenu;
    TextView tvPvrDateTime;

    // Recorded List
    ConstraintLayout include_clPvrRecordedList;
    RelativeLayout rlPvrRecordedListMain;
    RecyclerView rvPvrRecordedList;
    LinearLayout llPvrRecordedListPageInfo, llPvrRecordedListSeriesBlock,
            llPvrRecordedListDiskSize, llPvrRecordedListColorButtonBlock,
            llPvrRecordedListBlue;
    TextView tvPvrNoRecordedList, tvPvrRecordedListDuration,
            tvPvrRecordedListFileSize, tvPvrRecordedListCurrentPage,
            tvPvrRecordedListTotalPage, tvPvrRecordedListSeriesProgramName;

    ListPaginationManager pvrRecordedListPaginationManager;
    PvrRecordedListAdapter pvrRecordedListAdapter;

    // Scheduled List
    ConstraintLayout include_clPvrScheduledList;
    RelativeLayout rlPvrScheduledListMain;
    RecyclerView rvPvrScheduledList;
    LinearLayout llPvrScheduledListPage, llPvrScheduledListSeriesBlock,
            llPvrScheduledListDiskSize, llPvrScheduledListColorButtonBlock,
            llPvrScheduledListBlue;
    TextView tvPvrNoScheduledList, tvPvrScheduledListStartEndTime,
            tvPvrScheduledListCurrentPage, tvPvrScheduledListTotalPage,
            tvPvrScheduledListSeriesProgramName;

    ListPaginationManager pvrScheduledListPaginationManager;
    PvrScheduledListAdapter pvrScheduledListAdapter;

    // Setup List
    ConstraintLayout include_clPvrSetupList;
    Button btnPvrProgramBasedRecording, btnPvrManualRecording;

    // manual recording layout
    PvrManualRecordingPopup pvrManualRecordingPopup;

    // Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - BUILD_TIME:" + BuildConfig.BUILD_TIME);
        primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        setContentView(R.layout.activity_pvr);

        initPvrViews();
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

        handler.postDelayed(updateTimer, 500);

        allChannels = ChannelUtils.getAllChannels(this);
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

        if (pvrManualRecordingPopup != null) {
            pvrManualRecordingPopup.dismiss();
        }

        handler.removeCallbacks(updateTimer);

        finish();
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
    }

    private void initPvrViews() {
        Log.d(TAG, "initPvrViews");

        density = getResources().getDisplayMetrics().density;

        initPvrMenuViews();
        initPvrRecordedListViews();
        initPvrScheduledListViews();
        initPvrSetupListViews();
    }

    private void initPvrMenuViews() {
        clPvrList = findViewById(R.id.clPvrList);
        clPvrListTitle = findViewById(R.id.clPvrListTitle);
        rvPvrMenu = findViewById(R.id.rvPvrMenu);
        tvPvrDateTime = findViewById(R.id.tvPvrDateTime);

        List<PvrMenuData> pvrMenuList = new ArrayList<>();
        pvrMenuList.add(new PvrMenuData(PvrMenuData.PvrMenuState.RECORDED_LIST,
                R.drawable.icon_record_record_f,
                getString(R.string.recorded_list)));
        pvrMenuList.add(new PvrMenuData(PvrMenuData.PvrMenuState.SCHEDULED_LIST,
                R.drawable.icon_record_recordschedule_f,
                getString(R.string.recording_schedule)));
        pvrMenuList.add(new PvrMenuData(PvrMenuData.PvrMenuState.RECORDING_SETUP_LIST,
                R.drawable.icon_record_recording_f,
                getString(R.string.recording_title)));

        rvPvrMenu.setLayoutManager(new LinearLayoutManager(this));
        pvrMenuAdapter = new PvrMenuAdapter(rvPvrMenu, pvrMenuList);
        rvPvrMenu.setAdapter(pvrMenuAdapter);

        pvrMenuAdapter.setOnItemFocusedListener((pvrMenuState) -> {
            Log.d(TAG, "pvrMenuAdapter setOnItemFocusedListener pvrMenuState:" + pvrMenuState);
            if (pvrMenuState == PvrMenuData.PvrMenuState.RECORDED_LIST) {
                showRecordedList();
            } else if (pvrMenuState == PvrMenuData.PvrMenuState.SCHEDULED_LIST) {
                showScheduledList();
            } else if (pvrMenuState == PvrMenuData.PvrMenuState.RECORDING_SETUP_LIST) {
                showRecordingSetupList();
            }
        });

        pvrMenuAdapter.setOnItemSelectedListener(pvrMenuState -> {
            Log.d(TAG, "pvrMenuAdapter setOnItemSelectedListener pvrMenuState:" + pvrMenuState);
            if (pvrMenuState == PvrMenuData.PvrMenuState.RECORDED_LIST && !tvPvrNoRecordedList.isShown()) {
                setRecordedListExpanded(true);
                focusRecycleViewIndex(rvPvrRecordedList, 0);
            } else if (pvrMenuState == PvrMenuData.PvrMenuState.SCHEDULED_LIST && !tvPvrNoScheduledList.isShown()) {
                setScheduledListExpanded(true);
                focusRecycleViewIndex(rvPvrScheduledList, 0);
            } else if (pvrMenuState == PvrMenuData.PvrMenuState.RECORDING_SETUP_LIST) {
                setRecordingSetupListExpanded(true);
                btnPvrProgramBasedRecording.requestFocus();
            }
        });
    }

    private void initPvrRecordedListViews() {
        Log.d(TAG, "initPvrRecordedListViews");
        include_clPvrRecordedList = findViewById(R.id.include_clPvrRecordedList);
        rlPvrRecordedListMain = findViewById(R.id.rlPvrRecordedListMain);
        rvPvrRecordedList = findViewById(R.id.rvPvrRecordedList);

        llPvrRecordedListPageInfo = findViewById(R.id.llPvrRecordedListPageInfo);
        llPvrRecordedListSeriesBlock = findViewById(R.id.llPvrRecordedListSeriesBlock);
        llPvrRecordedListDiskSize = findViewById(R.id.llPvrRecordedListDiskSize);
        llPvrRecordedListColorButtonBlock = findViewById(R.id.llPvrRecordedListColorButtonBlock);
        llPvrRecordedListBlue = findViewById(R.id.llPvrRecordedListBlue);

        tvPvrNoRecordedList = findViewById(R.id.tvPvrNoRecordedList);
        tvPvrRecordedListDuration = findViewById(R.id.tvPvrRecordedListDuration);
        tvPvrRecordedListFileSize = findViewById(R.id.tvPvrRecordedListFileSize);
        tvPvrRecordedListCurrentPage = findViewById(R.id.tvPvrRecordedListCurrentPage);
        tvPvrRecordedListTotalPage = findViewById(R.id.tvPvrRecordedListTotalPage);
        tvPvrRecordedListSeriesProgramName = findViewById(R.id.tvPvrRecordedListSeriesProgramName);

        initPvrRecordedListData();
    }

    private void initPvrRecordedListData() {
        rvPvrRecordedList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        pvrRecordedListPaginationManager = new ListPaginationManager<>(new ArrayList<>(), RECORDED_LIST_ITEMS_PER_PAGE);
        pvrRecordedListAdapter = new PvrRecordedListAdapter(rvPvrRecordedList, pvrRecordedListPaginationManager.getCurrentPageData());

        pvrRecordedListAdapter.setOnRecyclerViewInteractionListener(new PvrRecordedListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onPageUp() {
                Log.d(TAG, "pvrRecordedListAdapter onPageUp");
                pvrRecordedListPaginationManager.previousPage();
                tvPvrRecordedListCurrentPage.setText(
                        StringUtils.padToNDigits(pvrRecordedListPaginationManager.getCurrentPage() + 1, 2));
                pvrRecordedListAdapter.updateList(pvrRecordedListPaginationManager.getCurrentPageData(), true);

            }

            @Override
            public void onPageDown() {
                Log.d(TAG, "pvrRecordedListAdapter onPageDown");
                pvrRecordedListPaginationManager.nextPage();
                tvPvrRecordedListCurrentPage.setText(
                        StringUtils.padToNDigits(pvrRecordedListPaginationManager.getCurrentPage() + 1, 2));
                pvrRecordedListAdapter.updateList(pvrRecordedListPaginationManager.getCurrentPageData(), false);
            }

            @Override
            public void onEnterSeriesFolder(String episodeName) {
                Log.d(TAG, "pvrRecordedListAdapter onEnterSeriesFolder");
                llPvrRecordedListSeriesBlock.setVisibility(View.VISIBLE);
                tvPvrRecordedListSeriesProgramName.setText(episodeName);
                refreshRecordedList(false, episodeName);
            }

            @Override
            public void onExitSeriesFolder(String episodeName) {
                Log.d(TAG, "pvrRecordedListAdapter onExitSeriesFolder");
                llPvrRecordedListSeriesBlock.setVisibility(View.GONE);
                refreshRecordedList(false);
            }

            @Override
            public void onPlayRecording(RecordedProgramData recordedProgramData) {
                Log.d(TAG, "pvrRecordedListAdapter onPlayRecording Name:" + recordedProgramData.getTitle());
                Intent intent = new Intent(PvrActivity.this, PvrPlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(PvrPlayerActivity.EXTRA_RECORDED_PROGRAM_ID, String.valueOf(recordedProgramData.getId()));
                intent.putExtras(bundle);
                try {
                    PvrActivity.this.startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Error: " + e.toString());
                }
            }

            @Override
            public void onDeleteRecordedProgram(RecordedProgramData recordedProgramData) {
                Log.d(TAG, "pvrRecordedListAdapter onDeleteReminder");
                showRecordedProgramDeleteDialog(recordedProgramData);
            }

            @Override
            public void onShowParentalPinDialog( ) {
                Log.d(TAG, "pvrRecordedListAdapter onShowParentalPinDialog");
                showRecordedProgramParentalPinDialog(() -> {
                        llPvrRecordedListBlue.setVisibility(View.GONE);
                        pvrRecordedListAdapter.unlockProgramName();
                        pvrRecordedListAdapter.updateList(pvrRecordedListPaginationManager.getCurrentPageData(), false);
                    });
            }

            @Override
            public void onBackToPreview( ) {
                Log.d(TAG, "pvrRecordedListAdapter onBackToPreview");
                setRecordedListExpanded(false);
            }
        });

        rvPvrRecordedList.setAdapter(pvrRecordedListAdapter);
    }

    private void initPvrScheduledListViews() {
        Log.d(TAG, "initPvrScheduledListViews");
        include_clPvrScheduledList = findViewById(R.id.include_clPvrScheduledList);
        rlPvrScheduledListMain = findViewById(R.id.rlPvrScheduledListMain);
        rvPvrScheduledList = findViewById(R.id.rvPvrScheduledList);

        llPvrScheduledListPage = findViewById(R.id.llPvrScheduledListPage);
        llPvrScheduledListSeriesBlock = findViewById(R.id.llPvrScheduledListSeriesBlock);
        llPvrScheduledListDiskSize = findViewById(R.id.llPvrScheduledListDiskSize);
        llPvrScheduledListColorButtonBlock = findViewById(R.id.llPvrScheduledListColorButtonBlock);
        llPvrScheduledListBlue = findViewById(R.id.llPvrScheduledListBlue);

        tvPvrNoScheduledList = findViewById(R.id.tvPvrNoScheduledList);
        tvPvrScheduledListStartEndTime = findViewById(R.id.tvPvrScheduledListStartEndTime);
        tvPvrScheduledListCurrentPage = findViewById(R.id.tvPvrScheduledListCurrentPage);
        tvPvrScheduledListTotalPage = findViewById(R.id.tvPvrScheduledListTotalPage);
        tvPvrScheduledListSeriesProgramName = findViewById(R.id.tvPvrScheduledListSeriesProgramName);

        initPvrScheduledListData();
    }

    private void initPvrScheduledListData() {
        rvPvrScheduledList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        pvrScheduledListPaginationManager = new ListPaginationManager<>(new ArrayList<>(), SCHEDULED_LIST_ITEMS_PER_PAGE);
        pvrScheduledListAdapter = new PvrScheduledListAdapter(rvPvrScheduledList, pvrScheduledListPaginationManager.getCurrentPageData());

        pvrScheduledListAdapter.setOnRecyclerViewInteractionListener(new PvrScheduledListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onPageUp() {
                Log.d(TAG, "pvrScheduledListAdapter onPageUp");
                pvrScheduledListPaginationManager.previousPage();
                tvPvrScheduledListCurrentPage.setText(
                        StringUtils.padToNDigits(pvrScheduledListPaginationManager.getCurrentPage() + 1, 2));
                pvrScheduledListAdapter.updateList(pvrScheduledListPaginationManager.getCurrentPageData(), true);

            }

            @Override
            public void onPageDown() {
                Log.d(TAG, "pvrScheduledListAdapter onPageDown");
                pvrScheduledListPaginationManager.nextPage();
                tvPvrScheduledListCurrentPage.setText(
                        StringUtils.padToNDigits(pvrScheduledListPaginationManager.getCurrentPage() + 1, 2));
                pvrScheduledListAdapter.updateList(pvrScheduledListPaginationManager.getCurrentPageData(), false);
            }

            @Override
            public void onEnterSeriesFolder(String episodeName) {
                Log.d(TAG, "pvrScheduledListAdapter onEnterSeriesFolder");
                llPvrScheduledListSeriesBlock.setVisibility(View.VISIBLE);
                tvPvrScheduledListSeriesProgramName.setText(episodeName);
                refreshScheduledList(false, episodeName);
            }

            @Override
            public void onExitSeriesFolder(String episodeName) {
                Log.d(TAG, "pvrScheduledListAdapter onExitSeriesFolder");
                llPvrScheduledListSeriesBlock.setVisibility(View.GONE);
                refreshScheduledList(false);
            }

            @Override
            public void onDeleteScheduledProgram(ScheduledProgramData scheduledProgramData) {
                Log.d(TAG, "pvrScheduledListAdapter onDeleteReminder");
                showScheduledProgramDeleteDialog(scheduledProgramData);
            }

            @Override
            public void onShowParentalPinDialog( ) {
                Log.d(TAG, "pvrScheduledListAdapter onShowParentalPinDialog");
                showRecordedProgramParentalPinDialog(() -> {
                        llPvrScheduledListBlue.setVisibility(View.GONE);
                        pvrScheduledListAdapter.unlockProgramName();
                        pvrScheduledListAdapter.updateList(pvrScheduledListPaginationManager.getCurrentPageData(), false);
                    });
            }

            @Override
            public void onBackToPreview( ) {
                Log.d(TAG, "pvrScheduledListAdapter onBackToPreview");
                setScheduledListExpanded(false);
            }
        });

        rvPvrScheduledList.setAdapter(pvrScheduledListAdapter);
    }

    private void initPvrSetupListViews() {
        Log.d(TAG, "initPvrSetupListViews");
        include_clPvrSetupList = findViewById(R.id.include_clPvrSetupList);

        btnPvrProgramBasedRecording = findViewById(R.id.btnPvrProgramBasedRecording);
        btnPvrManualRecording = findViewById(R.id.btnPvrManualRecording);

        ViewUtils.applyButtonFocusTextEffect(btnPvrProgramBasedRecording, 14, 14, true);
        btnPvrProgramBasedRecording.setOnClickListener((v) -> {
            try {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("Action", "EPG");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start MainActivity, error:" + e.toString());
            }
        });

        btnPvrProgramBasedRecording.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    btnPvrManualRecording.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_BACK) {
                    setRecordingSetupListExpanded(false);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                }
            }
            return false;
        });

        ViewUtils.applyButtonFocusTextEffect(btnPvrManualRecording, 14, 14, true);
        btnPvrManualRecording.setOnClickListener((v) -> {
            showManualRecordingPopup(v);
        });

        btnPvrManualRecording.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    btnPvrProgramBasedRecording.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_BACK) {
                    setRecordingSetupListExpanded(false);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                }
            }
            return false;
        });
    }

    private void showRecordedList() {
        include_clPvrRecordedList.setVisibility(View.VISIBLE);
        include_clPvrScheduledList.setVisibility(View.GONE);
        include_clPvrSetupList.setVisibility(View.GONE);

        resetRecordedList();
    }

    private void setRecordedListExpanded(boolean isExpanded) {
        if (!isExpanded) {
            pvrRecordedListAdapter.setDetailViewsVisible(false);
            pvrMenuAdapter.focusTo(PvrMenuData.PvrMenuState.RECORDED_LIST);
            expandPvrMenu();
        } else {
            pvrRecordedListAdapter.setDetailViewsVisible(true);
            collapsePvrMenu();
        }

        float rightSideWidth = isExpanded ? RIGHT_SIDE_FULL_SIZE * density : RIGHT_SIDE_PREVIEW_SIZE * density;
        float rightSideListWidth = isExpanded ? RIGHT_SIDE_LIST_FULL_SIZE * density : RIGHT_SIDE_LIST_PREVIEW_SIZE * density;
        ViewUtils.animateViewWidth(include_clPvrRecordedList,
                (int)rightSideWidth,
                ANIMATION_DURATION_MS);
        ViewUtils.animateViewWidth(rlPvrRecordedListMain,
                (int)rightSideListWidth,
                ANIMATION_DURATION_MS);

        int visibility = isExpanded ? View.VISIBLE : View.GONE;
        tvPvrRecordedListDuration.setVisibility(visibility);
        tvPvrRecordedListFileSize.setVisibility(visibility);
        llPvrRecordedListPageInfo.setVisibility(visibility);
        llPvrRecordedListDiskSize.setVisibility(visibility);
        llPvrRecordedListColorButtonBlock.setVisibility(visibility);

        llPvrRecordedListSeriesBlock.setVisibility(View.GONE);
    }

    private void resetRecordedList() {
        refreshRecordedList(true);
    }

    private void updateRecordedList() {
        refreshRecordedList(false);
    }

    private void refreshRecordedList(boolean withoutFocus) {
        refreshRecordedList(withoutFocus, null);
    }

    private void refreshRecordedList(boolean withoutFocus, String episodeName) {
        List<RecordedProgramData> allRecordedPrograms;
        if (TextUtils.isEmpty(episodeName)) {
            // get all recorded programs
            allRecordedPrograms = RecordedProgramUtils.getRecordedPrograms(getApplicationContext());
        } else {
            // get recorded programs for a specific episodeName
            allRecordedPrograms = RecordedProgramUtils.getSeriesRecordedPrograms(getApplicationContext(), episodeName);
        }

        if (allRecordedPrograms.isEmpty()) {
            tvPvrNoRecordedList.setVisibility(View.VISIBLE);
            if (!withoutFocus) {
                setRecordedListExpanded(false);
            }
        } else {
            tvPvrNoRecordedList.setVisibility(View.GONE);
        }

        pvrRecordedListPaginationManager.updateData(allRecordedPrograms);

        if (TextUtils.isEmpty(episodeName)) {
            if (withoutFocus) {
                pvrRecordedListAdapter.updateListWithoutFocus(pvrRecordedListPaginationManager.getCurrentPageData());
            } else {
                pvrRecordedListAdapter.updateList(pvrRecordedListPaginationManager.getCurrentPageData(), false, false);
            }
        } else {
            pvrRecordedListAdapter.updateList(pvrRecordedListPaginationManager.getCurrentPageData(), false, true);
        }

        tvPvrRecordedListCurrentPage.setText(
                StringUtils.padToNDigits(pvrRecordedListPaginationManager.getCurrentPage() + 1, 2));
        tvPvrRecordedListTotalPage.setText(
                StringUtils.padToNDigits(pvrRecordedListPaginationManager.getTotalPages(), 2));
    }

    private void showScheduledList() {
        include_clPvrRecordedList.setVisibility(View.GONE);
        include_clPvrScheduledList.setVisibility(View.VISIBLE);
        include_clPvrSetupList.setVisibility(View.GONE);

        resetScheduledList();
    }

    private void setScheduledListExpanded(boolean isExpanded) {
        if (!isExpanded) {
            pvrScheduledListAdapter.setDetailViewsVisible(false);
            pvrMenuAdapter.focusTo(PvrMenuData.PvrMenuState.SCHEDULED_LIST);
            expandPvrMenu();
        } else {
            pvrScheduledListAdapter.setDetailViewsVisible(true);
            collapsePvrMenu();
        }

        float rightSideWidth = isExpanded ? RIGHT_SIDE_FULL_SIZE * density : RIGHT_SIDE_PREVIEW_SIZE * density;
        float rightSideListWidth = isExpanded ? RIGHT_SIDE_LIST_FULL_SIZE * density : RIGHT_SIDE_LIST_PREVIEW_SIZE * density;
        ViewUtils.animateViewWidth(include_clPvrScheduledList,
                (int)rightSideWidth,
                ANIMATION_DURATION_MS);
        ViewUtils.animateViewWidth(rlPvrScheduledListMain,
                (int)rightSideListWidth,
                ANIMATION_DURATION_MS);

        int visibility = isExpanded ? View.VISIBLE : View.GONE;
        tvPvrScheduledListStartEndTime.setVisibility(visibility);
        llPvrScheduledListPage.setVisibility(visibility);
        llPvrScheduledListDiskSize.setVisibility(visibility);
        llPvrScheduledListColorButtonBlock.setVisibility(visibility);

        llPvrScheduledListSeriesBlock.setVisibility(View.GONE);
    }

    private void resetScheduledList() {
        refreshScheduledList(true);
    }

    private void updateScheduledList() {
        refreshScheduledList(false);
    }

    private void refreshScheduledList(boolean withoutFocus) {
        refreshScheduledList(withoutFocus, null);
    }

    private void refreshScheduledList(boolean withoutFocus, String episodeName) {
        List<ScheduledProgramData> allScheduledPrograms;
        if (TextUtils.isEmpty(episodeName)) {
            // get all scheduled programs
            allScheduledPrograms = ScheduledProgramUtils.getScheduledPrograms(getApplicationContext());
        } else {
            // get scheduled programs for a specific episodeName
            allScheduledPrograms = ScheduledProgramUtils.getSeriesScheduledPrograms(getApplicationContext(), episodeName);
        }

        if (allScheduledPrograms.isEmpty()) {
            tvPvrNoScheduledList.setVisibility(View.VISIBLE);
            if (!withoutFocus) {
                setScheduledListExpanded(false);
            }
        } else {
            tvPvrNoScheduledList.setVisibility(View.GONE);
        }

        pvrScheduledListPaginationManager.updateData(allScheduledPrograms);

        if (TextUtils.isEmpty(episodeName)) {
            if (withoutFocus) {
                pvrScheduledListAdapter.updateListWithoutFocus(pvrScheduledListPaginationManager.getCurrentPageData());
            } else {
                pvrScheduledListAdapter.updateList(pvrScheduledListPaginationManager.getCurrentPageData(), false, false);
            }
        } else {
            pvrScheduledListAdapter.updateList(pvrScheduledListPaginationManager.getCurrentPageData(), false, true);
        }

        tvPvrScheduledListCurrentPage.setText(
                StringUtils.padToNDigits(pvrScheduledListPaginationManager.getCurrentPage() + 1, 2));
        tvPvrScheduledListTotalPage.setText(
                StringUtils.padToNDigits(pvrScheduledListPaginationManager.getTotalPages(), 2));
    }

    private void showRecordingSetupList() {
        include_clPvrRecordedList.setVisibility(View.GONE);
        include_clPvrScheduledList.setVisibility(View.GONE);
        include_clPvrSetupList.setVisibility(View.VISIBLE);
    }

    private void setRecordingSetupListExpanded(boolean isExpanded) {
        if (!isExpanded) {
            pvrMenuAdapter.focusTo(PvrMenuData.PvrMenuState.RECORDING_SETUP_LIST);
            expandPvrMenu();
        } else {
            collapsePvrMenu();
        }

        float rightSideWidth = isExpanded ? RIGHT_SIDE_FULL_SIZE * density : RIGHT_SIDE_PREVIEW_SIZE * density;
        ViewUtils.animateViewWidth(include_clPvrSetupList,
                (int)rightSideWidth,
                ANIMATION_DURATION_MS);
    }

    private void collapsePvrMenu() {
        rvPvrMenu.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        clPvrListTitle.setVisibility(View.INVISIBLE);
        pvrMenuAdapter.setMenuNameGone();
    }

    private void expandPvrMenu() {
        clPvrListTitle.setVisibility(View.VISIBLE);
        pvrMenuAdapter.setMenuNameVisible();
        rvPvrMenu.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    }

    private Runnable updateTimer = new Runnable() {
        public void run() {
            String time = new SimpleDateFormat("MM.dd (E) HH:mm").format(new Date());
            tvPvrDateTime.setText(time);
            handler.postDelayed(this, 10000);
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode = " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return false;
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

    private void showManualRecordingPopup(View view) {
        pvrManualRecordingPopup = new PvrManualRecordingPopup(this, getApplicationContext(), view);
        pvrManualRecordingPopup.setOnJumpToScheduledPageListener(() -> {
            Log.d(TAG, "setOnJumpToScheduledPageListener");
            pvrManualRecordingPopup.dismiss();
            showScheduledList();
            resetScheduledList();

            // switch from SetupList to ScheduledList
            setRecordingSetupListExpanded(false);
            setScheduledListExpanded(true);
            focusRecycleViewIndex(rvPvrScheduledList, 0);
        });

        pvrManualRecordingPopup.setOnInsertRecordingScheduleListener(() -> {
            Log.d(TAG, "setOnInsertRecordingScheduleListener: ");
            resetScheduledList();
        });

        pvrManualRecordingPopup.show();
    }

    private static final String RECORDED_PROGRAM_DELETE_DIALOG_TAG = "RecordedProgramDeleteDialog";
    public void showRecordedProgramDeleteDialog(RecordedProgramData data) {
        showProgramDeleteDialog(data.getId(), getString(R.string.recorded_content_delete_msg),
                data.getChannelNumber(), data.getChannelName(),
                data.getTitle(), data.getStartTimeUtcMillis(),
                data.getRecordingDurationMillis(), RECORDED_PROGRAM_DELETE_DIALOG_TAG,
                () -> {
                    RecordedProgramUtils.deleteRecordedProgram(getApplicationContext(), data.getId());
                },
                () -> {
                    RecordedProgramUtils.deleteAllRecordedProgram(getApplicationContext());
                },
                this::updateRecordedList
        );
    }

    private static final String SCHEDULED_PROGRAM_DELETE_DIALOG_TAG = "ScheduledProgramDeleteDialog";
    public void showScheduledProgramDeleteDialog(ScheduledProgramData data) {
        showProgramDeleteDialog(data.getId(), getString(R.string.recorded_content_delete_msg),
                data.getChannelNumber(),
                data.getChannelName(),
                data.getTitle(),
                data.getStartTimeUtcMillis(),
                data.getRecordingDurationMillis(),
                SCHEDULED_PROGRAM_DELETE_DIALOG_TAG,
                () -> {
                    ScheduledProgramUtils.deleteScheduledProgram(getApplicationContext(), data.getId());
                },
                () -> {
                    ScheduledProgramUtils.deleteAllScheduledProgram(getApplicationContext());
                },
                this::updateScheduledList
        );
    }

    private void showProgramDeleteDialog(long programId, String message, String channelNumber,
            String channelName, String title, long startTimeUtcMillis,
            long recordingDurationMillis, String dialogTag, Runnable onDelete,
            Runnable onDeleteAll, Runnable onRefresh) {

        String date = TimeUtils.formatTimestampToDateYyMmDd(startTimeUtcMillis);
        String duration = TimeUtils.formatMillisToTime(recordingDurationMillis);

        ProgramDeleteDialogFragment dialog = new ProgramDeleteDialogFragment(programId, message,
                channelNumber, channelName, title, date, duration);

        dialog.setProgramDeleteListener(new ProgramDeleteDialogFragment.OnProgramDeleteListener() {
            @Override
            public void onProgramDelete(long id) {
                Log.d(TAG, "ProgramDeleteDialogFragment onProgramDelete");
                if (onDelete != null) {
                    new Thread(() -> {
                        onDelete.run();
                        if (onRefresh != null) {
                            runOnUiThread(onRefresh);
                        }
                        runOnUiThread(dialog::dismiss);
                    }).start();
                } else {
                    if (onRefresh != null) {
                        onRefresh.run();
                    }
                }
            }

            @Override
            public void onProgramDeleteAll() {
                Log.d(TAG, "ProgramDeleteDialogFragment onProgramDeleteAll");
                if (onDeleteAll != null) {
                    new Thread(() -> {
                        onDeleteAll.run();
                        if (onRefresh != null) {
                            runOnUiThread(onRefresh);
                        }
                        runOnUiThread(dialog::dismiss);
                    }).start();
                } else {
                    if (onRefresh != null) {
                        onRefresh.run();
                    }
                }
            }
        });

        dialog.show(getSupportFragmentManager(), dialogTag);
    }

    private static final String RECORDED_PROGRAM_PARENTAL_PIN_DIALOG_TAG = "RecordedProgramParentalPinDialog";
    public void showRecordedProgramParentalPinDialog(Runnable onUnlockProgram) {
        ParentalPinDialogFragment dialog = new ParentalPinDialogFragment();
        dialog.setOnPinEnteredListener(pin -> {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            String parentalPin = "0000"; // default value
            GposInfo gposInfo = primeDtv.gpos_info_get();
            if (gposInfo != null) {
                parentalPin = String.format(
                        Locale.ROOT,
                        "%04d",
                        GposInfo.getPasswordValue(this.getApplicationContext()));
            }

            if (!TextUtils.isEmpty(pin) && pin.equals(parentalPin)) {
                if (onUnlockProgram != null) {
                    onUnlockProgram.run();
                }
                dialog.dismiss();
            } else {
                dialog.showErrorMessage();
            }
        });
        dialog.show(getSupportFragmentManager(), RECORDED_PROGRAM_PARENTAL_PIN_DIALOG_TAG);
    }
}
