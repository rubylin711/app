package com.prime.homeplus.tv.ui.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.adapter.EpgChannelListAdapter;
import com.prime.homeplus.tv.adapter.EpgDateListAdapter;
import com.prime.homeplus.tv.adapter.EpgGenreListAdapter;
import com.prime.homeplus.tv.adapter.EpgProgramListAdapter;
import com.prime.homeplus.tv.data.GenreData;
import com.prime.homeplus.tv.data.GlobalState;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.manager.CurrentChannelListManager;
import com.prime.homeplus.tv.manager.ListPaginationManager;
import com.prime.homeplus.tv.manager.LockManager;
import com.prime.homeplus.tv.manager.UnlockStateManager;
import com.prime.homeplus.tv.utils.ProgramReminderUtils;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;
import com.prime.homeplus.tv.ui.component.ProgramReminderSettingPopup;
import com.prime.homeplus.tv.ui.component.SimpleRecordingSettingPopup;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.ProgramUtils;
import com.prime.homeplus.tv.utils.StringUtils;
import com.prime.homeplus.tv.utils.TimeUtils;
import com.prime.homeplus.tv.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EpgFragment extends Fragment {
    private static final String TAG = "EpgFragment";

    private static final String ARG_CHANNEL_ID = "channel_id";
    private final int EPG_MAX_DAYS = 7;
    private final int EPG_ITEMS_PER_PAGE = 5;
    private static final long UPDATE_TIMER_PERIOD_MS = 10 * 1000;
    private static final int MAX_EPG_ENTER_CHANNEL_NUMBER_LENGTH = 3;
    private static final long EPG_ENTER_CHANNEL_NUMBER_TIMEOUT_MS = 3 * 1000;

    private String epgEnterChannelNumber = "";
    private String channelId;
    private MutableLiveData<List<String>> epgLiveData = new MutableLiveData<>();
    private CurrentChannelListManager currentEpgChannelListManager;
    private EpgPlaybackManager epgPlaybackManager;
    private EpgGenreListAdapter epgGenreListAdapter;
    private EpgDateListAdapter epgDateListAdapter;
    private EpgChannelListAdapter epgChannelListAdapter;
    private EpgProgramListAdapter epgProgramListAdapter;
    private ListPaginationManager dateListPaginationManager, channelListPaginationManager, programListPaginationManager;
    private View mView;
    private final Handler epgHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateTimerRunnable = this::updateTimer;
    private final Runnable epgEnterChannelNumberTimeoutRunnable = this::epgEnterChannelNumberTimeout;

    // top-half
    private TextView tvEpgCurrentGenreName, tvEpgDate, tvEpgChannelNumber, tvEpgChannelName, tvEpgProgramName,
            tvEpgProgramStart, tvEpgProgramEnd, tvEpgSubtitle, tvEpgProgramDescription;
    private ImageView ivEpgChannelPadlock, ivEpgChannelMusic, ivEpgChannelFavorite, ivEpgChannelResolution,
            ivEpgChannelDolby, ivEpgProgramRating, ivEpgProgramBilingual, ivEpgProgramReminder,
            ivEpgProgramNowRecording, ivEpgProgramScheduleRecord;
    private ProgressBar pbEpgProgram;
    private ScrollView svEpgProgramDescription;

    // bottom-half
    private LinearLayout llEpgChannelPageInfo, llEpgProgramPageInfo, llEpgProgramMask,
            llEpgBottomBarDirection, llEpgBottomBarRed, llEpgBottomBarGreen,
            llEpgBottomBarYellow, llEpgBottomBarBlue, llEpgBottomBarOk, llEpgBottomBarBack;
    private TextView tvEpgChannelCurrentPageIndex, tvEpgChannelTotalPageIndex, tvEpgProgramCurrentPageIndex,
            tvEpgProgramTotalPageIndex, tvEpgEnterChannelNumber, tvEpgBottomBarGreen, tvEpgBottomBarYellow;
    private RecyclerView rvEpgDateList, rvEpgChannelList, rvEpgProgramList;
    private ConstraintLayout clEpgEnterChannelNumber;

    // genre list
    private View popupViewGenreList;
    private static PopupWindow popupWindowGenreList;
    private RecyclerView rvEpgGenreList;

    // recording setting layout
    SimpleRecordingSettingPopup recordingSettingPopup;

    // reminder layout
    ProgramReminderSettingPopup reminderSettingPopup;

    public interface EpgPlaybackManager {
        void tuneEpgChannel(Channel ch);
        List<String> getCurrentAudioLanguageList();
        List<String> getCurrentSubtitleLanguages();
        String getCurrentVideoResolutionLabel();
        UnlockStateManager getCurrentUnlockStateManager();
        void backToLiveFromEpg();
        void showParentalPinDialog();
    }

    public EpgFragment() {
        // Required empty public constructor
    }

    public static EpgFragment newInstance(String channelId) {
        Log.d(TAG, "newInstance");
        EpgFragment fragment = new EpgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHANNEL_ID, channelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof EpgPlaybackManager) {
            epgPlaybackManager = (EpgPlaybackManager) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEpgActionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            channelId = getArguments().getString(ARG_CHANNEL_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_epg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mView = view;

        initTopHalfLayout();
        initBottomHalfLayout();
        initRecordingSettingPopup();
        initReminderPopup();
        initGenreListLayout();
        initGenreData();
        initEmptyDateData();
        initEmptyChannelData();
        initEmptyProgramData();

        epgHandler.postDelayed(updateTimerRunnable, 100);

        epgLiveData.observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> epgList) {
                Log.d(TAG, "epgLiveData onChanged");
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            Log.d(TAG, "EpgFragment is now hidden");
            hideRecordingSettingPopup();
            hideReminderPopup();
            hideGenreList();
            epgHandler.removeCallbacks(null);
        } else {
            Log.d(TAG, "EpgFragment is now visible again");
            epgHandler.postDelayed(updateTimerRunnable, 100);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (rvEpgGenreList != null) {
            resetGenreList();
        }

        if (rvEpgChannelList != null && epgGenreListAdapter != null) {
            refreshEpgByGenre(epgGenreListAdapter.getCurrentGenreInfo());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        epgPlaybackManager = null;
    }

    private void updateTimer() {
        String time = new SimpleDateFormat("MM/dd(E)  HH:mm",
                Locale.getDefault()).format(new Date())
                .replace("週", "");

        tvEpgDate.setText(time);

        boolean isToday = TimeUtils.isToday((Long) dateListPaginationManager.getFullData().get(0));
        if (!isToday) {
            Log.d(TAG, "update EPG data when crossing into a new day");
            List<Long> dates = TimeUtils.generateTimestampsFromNowThenMidnights(EPG_MAX_DAYS);
            dateListPaginationManager.updateData(dates);
            epgDateListAdapter.updateListWithoutFocus(dateListPaginationManager.getCurrentPageData());
            updateEpgChannelAndProgram();
        } else if (dateListPaginationManager.getCurrentPage() == 0 &&
                dateListPaginationManager.getCurrentFocusIndex() == 0) {
            //Log.d(TAG, "check if the first program's end time on the current channel exceeds the current time");
            if (programListPaginationManager != null && programListPaginationManager.getFullData().size() > 1) {
                Program pg = (Program)programListPaginationManager.getFullData().get(0);
                long nowUnixTime = System.currentTimeMillis();
                if (pg != null && nowUnixTime > pg.getEndTimeUtcMillis()) {
                    Log.d(TAG, "refresh dateList and programList");
                    // update date list
                    List<Long> dateList = dateListPaginationManager.getFullData();
                    if (!dateList.isEmpty()) {
                        // do not fire adapter notifyItemChanged(), just update date array list
                        dateList.set(0, nowUnixTime);
                        dateListPaginationManager.updateData(dateList);
                    }

                    // update program list
                    List<Program> programList = programListPaginationManager.getFullData();
                    programList.remove(0);
                    programListPaginationManager.updateData(programList);
                    epgProgramListAdapter.updateList(programListPaginationManager.getCurrentPageData(), false);
                }
            }
        }

        epgHandler.postDelayed(updateTimerRunnable, UPDATE_TIMER_PERIOD_MS);
    }

    private void initTopHalfLayout() {
        tvEpgCurrentGenreName = mView.findViewById(R.id.tvEpgCurrentGenreName);
        tvEpgDate = mView.findViewById(R.id.tvEpgDate);
        tvEpgChannelNumber = mView.findViewById(R.id.tvEpgChannelNumber);
        tvEpgChannelName = mView.findViewById(R.id.tvEpgChannelName);
        tvEpgProgramName = mView.findViewById(R.id.tvEpgProgramName);
        tvEpgProgramStart = mView.findViewById(R.id.tvEpgProgramStart);
        tvEpgProgramEnd = mView.findViewById(R.id.tvEpgProgramEnd);
        tvEpgSubtitle = mView.findViewById(R.id.tvEpgSubtitle);
        tvEpgProgramDescription = mView.findViewById(R.id.tvEpgProgramDescription);

        ivEpgChannelPadlock = mView.findViewById(R.id.ivEpgChannelPadlock);
        ivEpgChannelMusic = mView.findViewById(R.id.ivEpgChannelMusic);
        ivEpgChannelFavorite = mView.findViewById(R.id.ivEpgChannelFavorite);
        ivEpgChannelResolution = mView.findViewById(R.id.ivEpgChannelResolution);
        ivEpgChannelDolby = mView.findViewById(R.id.ivEpgChannelDolby);
        ivEpgProgramRating = mView.findViewById(R.id.ivEpgProgramRating);
        ivEpgProgramBilingual = mView.findViewById(R.id.ivEpgProgramBilingual);
        ivEpgProgramReminder = mView.findViewById(R.id.ivEpgProgramReminder);
        ivEpgProgramNowRecording = mView.findViewById(R.id.ivEpgProgramNowRecording);
        ivEpgProgramScheduleRecord = mView.findViewById(R.id.ivEpgProgramScheduleRecord);

        pbEpgProgram = mView.findViewById(R.id.pbEpgProgram);
        svEpgProgramDescription = mView.findViewById(R.id.svEpgProgramDescription);
    }

    private void initBottomHalfLayout() {
        llEpgChannelPageInfo = mView.findViewById(R.id.llEpgChannelPageInfo);
        llEpgProgramPageInfo = mView.findViewById(R.id.llEpgProgramPageInfo);
        llEpgProgramMask = mView.findViewById(R.id.llEpgProgramMask);
        tvEpgChannelCurrentPageIndex = mView.findViewById(R.id.tvEpgChannelCurrentPageIndex);
        tvEpgChannelTotalPageIndex = mView.findViewById(R.id.tvEpgChannelTotalPageIndex);
        tvEpgProgramCurrentPageIndex = mView.findViewById(R.id.tvEpgProgramCurrentPageIndex);
        tvEpgProgramTotalPageIndex = mView.findViewById(R.id.tvEpgProgramTotalPageIndex);

        rvEpgDateList = mView.findViewById(R.id.rvEpgDateList);
        rvEpgChannelList = mView.findViewById(R.id.rvEpgChannelList);
        rvEpgProgramList = mView.findViewById(R.id.rvEpgProgramList);

        clEpgEnterChannelNumber = mView.findViewById(R.id.clEpgEnterChannelNumber);
        tvEpgEnterChannelNumber = mView.findViewById(R.id.tvEpgEnterChannelNumber);

        llEpgBottomBarDirection = mView.findViewById(R.id.llEpgBottomBarDirection);
        llEpgBottomBarRed = mView.findViewById(R.id.llEpgBottomBarRed);
        llEpgBottomBarGreen = mView.findViewById(R.id.llEpgBottomBarGreen);
        llEpgBottomBarYellow = mView.findViewById(R.id.llEpgBottomBarYellow);
        llEpgBottomBarBlue = mView.findViewById(R.id.llEpgBottomBarBlue);
        llEpgBottomBarOk = mView.findViewById(R.id.llEpgBottomBarOk);
        llEpgBottomBarBack = mView.findViewById(R.id.llEpgBottomBarBack);
        tvEpgBottomBarGreen = mView.findViewById(R.id.tvEpgBottomBarGreen);
        tvEpgBottomBarYellow = mView.findViewById(R.id.tvEpgBottomBarYellow);

        bottomBarItems = Arrays.asList(
                llEpgBottomBarDirection, llEpgBottomBarRed,
                llEpgBottomBarGreen, llEpgBottomBarYellow,
                llEpgBottomBarBlue, llEpgBottomBarOk,
                llEpgBottomBarBack
        );
    }

    private void initRecordingSettingPopup() {
        recordingSettingPopup = new SimpleRecordingSettingPopup(requireContext(), mView, epgHandler);
        recordingSettingPopup.setOnRecordingSettingChangedListener(() -> {
            Log.d(TAG, "setOnRecordingSettingChangedListener");
            epgProgramListAdapter.updateList(programListPaginationManager.getCurrentPageData(), false);
        });
    }

    public void hideRecordingSettingPopup() {
        if (recordingSettingPopup != null) {
            recordingSettingPopup.dismiss();
        }
    }

    private void initReminderPopup() {
        reminderSettingPopup = new ProgramReminderSettingPopup(this, requireContext(), mView);
        reminderSettingPopup.setOnRefreshEpgReminderListener(() -> {
            Log.d(TAG, "setOnRefreshEpgReminderListener");
            epgProgramListAdapter.updateList(programListPaginationManager.getCurrentPageData(), false);
        });
    }

    public void hideReminderPopup() {
        if (reminderSettingPopup != null) {
            reminderSettingPopup.dismiss();
        }
    }

    private void initGenreListLayout() {
        popupViewGenreList = LayoutInflater.from(getContext()).inflate(R.layout.popup_epg_genre_list, null);
        popupWindowGenreList = new PopupWindow(popupViewGenreList,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                true);

        rvEpgGenreList = popupViewGenreList.findViewById(R.id.rvEpgGenreList);
    }

    private void initGenreData() {
        Log.d(TAG, "initGenreData");
        rvEpgGenreList.setLayoutManager(new LinearLayoutManager(requireContext()));
        epgGenreListAdapter = new EpgGenreListAdapter(rvEpgGenreList, GenreData.getAllEpgGenres());
        epgGenreListAdapter.setOnRecyclerViewInteractionListener(new EpgGenreListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onClick(GenreData.GenreInfo genreInfo) {
                Log.d(TAG, "epgGenreListAdapter onClick genreId:" + genreInfo.id + ", genreName:" + genreInfo.getName(requireContext()));
                hideGenreList();
                refreshEpgByGenre(genreInfo);
            }

            @Override
            public void onKeyEventReceived(KeyEvent event) {
                int keyCode = event.getKeyCode();
                Log.d(TAG, "epgGenreListAdapter keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    hideGenreList();
                    focusRecycleViewIndex(rvEpgDateList, dateListPaginationManager.getCurrentFocusIndex());
                }
            }
        });

        rvEpgGenreList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "rvEpgGenreList onGlobalLayout");
            }
        });

        rvEpgGenreList.setAdapter(epgGenreListAdapter);
        Log.d(TAG, "initGenreData End");
    }

    private void resetGenreList() {
        epgGenreListAdapter.resetIndex();
    }

    public void showGenreList() {
        popupWindowGenreList.showAtLocation(mView, Gravity.CENTER, 0, 0);
        epgGenreListAdapter.focusCurrentGenreIndex();
    }

    public void hideGenreList() {
        if (epgGenreListAdapter != null) {
            epgGenreListAdapter.resetRadioButtonStyle();
        }

        if (popupWindowGenreList != null) {
            popupWindowGenreList.dismiss();
        }
    }

    private void initEmptyDateData() {
        rvEpgDateList.setLayoutManager(new LinearLayoutManager(requireContext()));
        dateListPaginationManager = new ListPaginationManager<>(new ArrayList<>(), EPG_ITEMS_PER_PAGE);
        epgDateListAdapter = new EpgDateListAdapter(rvEpgDateList, dateListPaginationManager.getCurrentPageData());
        epgDateListAdapter.setOnRecyclerViewInteractionListener(new EpgDateListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onPageUp() {
                Log.d(TAG, "epgDateListAdapter onPageUp");
                dateListPaginationManager.nextPage();
                epgDateListAdapter.updateList(dateListPaginationManager.getCurrentPageData(), true);
            }

            @Override
            public void onPageDown() {
                Log.d(TAG, "epgDateListAdapter onPageDown");
                dateListPaginationManager.previousPage();
                epgDateListAdapter.updateList(dateListPaginationManager.getCurrentPageData(), false);
            }

            @Override
            public void onKeyEventReceived(KeyEvent event) {
                int keyCode = event.getKeyCode();
                Log.d(TAG, "epgDateListAdapter keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    showGenreList();
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    focusRecycleViewIndex(rvEpgChannelList, channelListPaginationManager.getCurrentFocusIndex());
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (isVisible(clEpgEnterChannelNumber)) {
                        epgEnterChannelNumberTimeout();
                    } else {
                        focusRecycleViewIndex(rvEpgChannelList, channelListPaginationManager.getCurrentFocusIndex());
                    }
                }
            }

            @Override
            public void onFocus(boolean hasFocus) {
                Log.d(TAG, "epgDateListAdapter onFocus hasFocus:" + hasFocus);
                if (hasFocus) {
                    epgChannelListAdapter.drawSelectItem(channelListPaginationManager.getCurrentFocusIndex(), (Channel)channelListPaginationManager.getCurrentFocusItem());
                    epgProgramListAdapter.drawSelectItem(programListPaginationManager.getCurrentFocusIndex());

                    llEpgChannelPageInfo.setVisibility(View.INVISIBLE);
                    llEpgProgramPageInfo.setVisibility(View.INVISIBLE);
                    updateBottomBar(BottomBarMode.FOCUS_DATE);
                }
            }

            @Override
            public void onFocusDateChanged(int index, long searchStartTime) {
                Log.d(TAG, "epgDateListAdapter onFocusDateChanged searchStartTime:" + searchStartTime);
                dateListPaginationManager.setCurrentFocusIndex(index);
                updateProgramData();
            }
        });

        rvEpgDateList.setAdapter(epgDateListAdapter);
    }

    private void updateDateData() {
        List<Long> dates = TimeUtils.generateTimestampsFromNowThenMidnights(EPG_MAX_DAYS);
        dateListPaginationManager = new ListPaginationManager<>(dates, EPG_ITEMS_PER_PAGE);
        epgDateListAdapter.updateListWithoutFocus(dateListPaginationManager.getCurrentPageData());
    }

    private void initEmptyChannelData() {
        rvEpgChannelList.setLayoutManager(new LinearLayoutManager(requireContext()));
        channelListPaginationManager = new ListPaginationManager<>(new ArrayList<>(), EPG_ITEMS_PER_PAGE);
        epgChannelListAdapter = new EpgChannelListAdapter(rvEpgChannelList, channelListPaginationManager.getCurrentPageData());
        epgChannelListAdapter.setOnRecyclerViewInteractionListener(new EpgChannelListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onPageUp() {
                Log.d(TAG, "epgChannelListAdapter onPageUp");
                channelListPaginationManager.previousPage();
                tvEpgChannelCurrentPageIndex.setText(String.valueOf(channelListPaginationManager.getCurrentPage() + 1));
                epgChannelListAdapter.updateListAndSetFocus(channelListPaginationManager.getCurrentPageData(), EPG_ITEMS_PER_PAGE - 1);
            }

            @Override
            public void onPageDown() {
                Log.d(TAG, "epgChannelListAdapter onPageDown");
                channelListPaginationManager.nextPage();
                tvEpgChannelCurrentPageIndex.setText(String.valueOf(channelListPaginationManager.getCurrentPage() + 1));
                epgChannelListAdapter.updateListAndSetFocus(channelListPaginationManager.getCurrentPageData(), 0);
            }

            @Override
            public void onKeyEventReceived(KeyEvent event) {
                int keyCode = event.getKeyCode();
                Log.d(TAG, "epgChannelListAdapter keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    focusRecycleViewIndex(rvEpgDateList, dateListPaginationManager.getCurrentFocusIndex());
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    focusRecycleViewIndex(rvEpgProgramList, programListPaginationManager.getCurrentFocusIndex());
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (isVisible(clEpgEnterChannelNumber)) {
                        epgEnterChannelNumberTimeout();
                    } else {
                        if (epgPlaybackManager != null) {
                            epgPlaybackManager.backToLiveFromEpg();
                        }
                    }
                } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
                    // TODO(STB_Vendor): Implement the custom feature for STB_Vendor

                    Channel channel = epgChannelListAdapter.getLastFocusedChannel();
                    if (channel == null)
                        return;

                    boolean isFavorite = ChannelUtils.isChannelFavorite(getContext(), channel);
                    ivEpgChannelFavorite.setVisibility(isFavorite ? View.VISIBLE : View.GONE);
                    tvEpgBottomBarYellow.setText(isFavorite ? getContext().getString(R.string.delete_favourite) : getContext().getString(R.string.add_favourite));
                }
            }

            @Override
            public void onFocus(boolean hasFocus) {
                Log.d(TAG, "epgChannelListAdapter onFocus hasFocus:" + hasFocus);
                if (hasFocus) {
                    epgDateListAdapter.drawSelectItem(dateListPaginationManager.getCurrentFocusIndex());
                    epgProgramListAdapter.drawSelectItem(programListPaginationManager.getCurrentFocusIndex());
                }

                if (channelListPaginationManager != null && hasFocus) {
                    llEpgProgramPageInfo.setVisibility(View.INVISIBLE);

                    tvEpgChannelCurrentPageIndex.setText(String.valueOf(channelListPaginationManager.getCurrentPage() + 1));
                    tvEpgChannelTotalPageIndex.setText(String.valueOf(channelListPaginationManager.getTotalPages()));
                    llEpgChannelPageInfo.setVisibility(View.VISIBLE);

                    updateBottomBar(BottomBarMode.FOCUS_CHANNEL);
                }
            }

            @Override
            public void onFocusChannelChanged(int index, Channel ch) {
                Log.d(TAG, "epgChannelListAdapter onFocusChannelChanged:" + ch.getDisplayName());
                channelListPaginationManager.setCurrentFocusIndex(index);
                if (epgPlaybackManager != null) {
                    epgPlaybackManager.tuneEpgChannel(ch);
                }

                LockManager lockManager = new LockManager(requireContext().getApplicationContext());
                int lockFlag = lockManager.getHighestPriorityLockFlag(ch, null, null);
                boolean isUnlock = false;
                if (epgPlaybackManager != null) {
                    isUnlock = epgPlaybackManager.getCurrentUnlockStateManager().isUnlocked(LockManager.LOCK_EPG_PROGRAM_INFO);
                }

                if (lockFlag == LockManager.LOCK_NONE ||
                        lockFlag == LockManager.LOCK_WORK_HOUR ||
                        lockFlag == LockManager.LOCK_PARENTAL_PROGRAM ||
                        isUnlock) {
                    updateProgramData();
                    llEpgProgramMask.setVisibility(View.GONE);
                    rvEpgProgramList.setVisibility(View.VISIBLE);
                } else {
                    updateProgramData();
                    llEpgProgramMask.setVisibility(View.VISIBLE);
                    rvEpgProgramList.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean isUnlocked(int lockFlag) {
                Log.d(TAG, "epgChannelListAdapter isUnlocked lockFlag:" + lockFlag);
                boolean isUnlocked = false;
                if (epgPlaybackManager != null) {
                    isUnlocked = epgPlaybackManager.getCurrentUnlockStateManager().isUnlocked(lockFlag);
                }
                return isUnlocked;
            }
        });

        rvEpgChannelList.setAdapter(epgChannelListAdapter);
    }

    private void updateChannelData(GenreData.GenreInfo genreInfo) {
        List<ProgramInfo> programInfoList = ChannelUtils.getProgramInfosByGenre(requireContext(), genreInfo.id);

        if ((genreInfo == null) || (genreInfo.id == GenreData.ID_ALL_CHANNELS)) {
            currentEpgChannelListManager = new CurrentChannelListManager(GenreData.getAllEpgGenres().get(0).getName(requireContext()), ChannelUtils.getAllChannels(requireContext()));
            currentEpgChannelListManager.setProgramInfoList(programInfoList);
        } else {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // get specific genre channel list
            List<Channel> genreList = ChannelUtils.getAllChannelsByProgramInfo(requireContext(), programInfoList);
            String genreName = genreInfo.getName(requireContext());
            if (genreList.isEmpty()) {
                String msg = genreInfo.getName(requireContext()) + " - " + getString(R.string.no_channel_list);
                ViewUtils.showToast(requireContext(), Toast.LENGTH_LONG, msg);

                genreName = GenreData.getAllEpgGenres().get(0).getName(requireContext());
                genreList = ChannelUtils.getAllChannels(requireContext());
            }
            currentEpgChannelListManager = new CurrentChannelListManager(genreName, genreList);
            currentEpgChannelListManager.setProgramInfoList(programInfoList);
        }
        tvEpgCurrentGenreName.setText(currentEpgChannelListManager.getCurrentGenre());

        if (currentEpgChannelListManager == null || currentEpgChannelListManager.getCurrentChannelList().isEmpty()) {
            currentEpgChannelListManager.setCurrentChannelList(new ArrayList<>());
            currentEpgChannelListManager.setProgramInfoList(new ArrayList<>());
        }

        List<Channel> chList = currentEpgChannelListManager.getCurrentChannelList();
        channelListPaginationManager.updateData(chList);
        int lastChannelIndex = findLastChannelIndex(chList);
        channelListPaginationManager.setPageAndFocusByFullListIndex((lastChannelIndex == -1) ? 0 : lastChannelIndex);

        epgChannelListAdapter.updateListAndSetFocus(channelListPaginationManager.getCurrentPageData(), channelListPaginationManager.getCurrentFocusIndex());
    }

    private void initEmptyProgramData() {
        rvEpgProgramList.setLayoutManager(new LinearLayoutManager(requireContext()));
        programListPaginationManager = new ListPaginationManager<>(new ArrayList<>(), EPG_ITEMS_PER_PAGE);
        epgProgramListAdapter = new EpgProgramListAdapter(rvEpgProgramList, programListPaginationManager.getCurrentPageData());
        epgProgramListAdapter.setOnRecyclerViewInteractionListener(new EpgProgramListAdapter.OnRecyclerViewInteractionListener() {
            @Override
            public void onPageUp() {
                Log.d(TAG, "epgProgramListAdapter onPageUp");
                if (programListPaginationManager.getCurrentPage() == 0) {
                    int index = dateListPaginationManager.getCurrentFocusIndex() - 1;
                    if (index < 0) {
                        if (dateListPaginationManager.getCurrentPage() == 0) {
                            return;
                        }
                        dateListPaginationManager.previousPage();
                        dateListPaginationManager.setCurrentFocusIndex(dateListPaginationManager.getCurrentPageData().size() - 1);
                        epgDateListAdapter.updateListWithoutFocus(dateListPaginationManager.getCurrentPageData());
                        epgDateListAdapter.drawSelectItem(dateListPaginationManager.getCurrentFocusIndex());
                    } else {
                        dateListPaginationManager.setCurrentFocusIndex(index);
                        epgDateListAdapter.drawSelectItem(dateListPaginationManager.getCurrentFocusIndex());
                    }
                    updateProgramDataAndFocusLastItem(true);
                } else {
                    programListPaginationManager.previousPage();
                    tvEpgProgramCurrentPageIndex.setText(String.valueOf(programListPaginationManager.getCurrentPage() + 1));
                    epgProgramListAdapter.updateList(programListPaginationManager.getCurrentPageData(), true);
                }
            }

            @Override
            public void onPageDown() {
                Log.d(TAG, "epgProgramListAdapter onPageDown");
                if (programListPaginationManager.getCurrentPage() == programListPaginationManager.getTotalPages() - 1) {
                    int index = dateListPaginationManager.getCurrentFocusIndex() + 1;
                    if (index >= dateListPaginationManager.getCurrentPageData().size()) {
                        dateListPaginationManager.nextPage();
                        dateListPaginationManager.setCurrentFocusIndex(0);
                        epgDateListAdapter.updateListWithoutFocus(dateListPaginationManager.getCurrentPageData());
                        epgDateListAdapter.drawSelectItem(dateListPaginationManager.getCurrentFocusIndex());
                    } else {
                        dateListPaginationManager.setCurrentFocusIndex(index);
                        epgDateListAdapter.drawSelectItem(dateListPaginationManager.getCurrentFocusIndex());
                    }
                    updateProgramDataAndFocusLastItem(false);
                } else {
                    programListPaginationManager.nextPage();
                    tvEpgProgramCurrentPageIndex.setText(String.valueOf(programListPaginationManager.getCurrentPage() + 1));
                    epgProgramListAdapter.updateList(programListPaginationManager.getCurrentPageData(), false);
                }
            }

            @Override
            public void onKeyEventReceived(KeyEvent event) {
                int keyCode = event.getKeyCode();
                Log.d(TAG, "epgProgramListAdapter keyCode:" + keyCode);
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    focusRecycleViewIndex(rvEpgChannelList, channelListPaginationManager.getCurrentFocusIndex());
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (isVisible(clEpgEnterChannelNumber)) {
                        epgEnterChannelNumberTimeout();
                    } else {
                        if (epgPlaybackManager != null) {
                            epgPlaybackManager.backToLiveFromEpg();
                        }
                    }
                }
            }

            @Override
            public void onFocus(boolean hasFocus) {
                Log.d(TAG, "epgProgramListAdapter onFocus hasFocus:" + hasFocus);
                if (hasFocus) {
                    epgDateListAdapter.drawSelectItem(dateListPaginationManager.getCurrentFocusIndex());
                    epgChannelListAdapter.drawSelectItem(channelListPaginationManager.getCurrentFocusIndex(), (Channel)channelListPaginationManager.getCurrentFocusItem());
                }

                if (programListPaginationManager != null && hasFocus) {
                    llEpgChannelPageInfo.setVisibility(View.INVISIBLE);

                    tvEpgProgramCurrentPageIndex.setText(String.valueOf(programListPaginationManager.getCurrentPage() + 1));
                    tvEpgProgramTotalPageIndex.setText(String.valueOf(programListPaginationManager.getTotalPages()));
                    llEpgProgramPageInfo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFocusProgram(int index, Program pg) {
                Log.d(TAG, "epgProgramListAdapter onFocusProgram:" + pg.getTitle());
                programListPaginationManager.setCurrentFocusIndex(index);

                if (dateListPaginationManager.getCurrentPage() == 0 &&
                        dateListPaginationManager.getCurrentFocusIndex() == 0 &&
                        programListPaginationManager.getCurrentPage() == 0 &&
                        programListPaginationManager.getCurrentFocusIndex() == 0) {
                    updateBottomBar(BottomBarMode.FOCUS_CURRENT_PROGRAM);
                } else {
                    updateBottomBar(BottomBarMode.FOCUS_PROGRAM);
                }

                LockManager lockManager = new LockManager(requireContext().getApplicationContext());
                int lock_flag = lockManager.getHighestPriorityLockFlag(null, pg, null);
                updateEpgProgramInfo(pg, lock_flag);
            }

            @Override
            public void onShowRecordingSetting(Program pg) {
                Log.d(TAG, "epgProgramListAdapter onShowRecordingSetting:" + pg.getTitle());
                if (llEpgBottomBarRed.isShown()) {
                    Channel ch = (Channel)channelListPaginationManager.getCurrentFocusItem();
                    recordingSettingPopup.setScheduledProgramInfo(pg, ch);
                    recordingSettingPopup.showMenu();
                }
            }

            @Override
            public void onShowReminderMenu(Program pg) {
                Log.d(TAG, "epgProgramListAdapter onShowReminderMenu:" + pg.getTitle());
                if (llEpgBottomBarGreen.isShown()) {
                    Channel ch = (Channel)channelListPaginationManager.getCurrentFocusItem();
                    reminderSettingPopup.setReminderInfo(pg, ch);
                    reminderSettingPopup.showMenu();
                }
            }

            @Override
            public boolean isUnlocked(int lockFlag) {
                Log.d(TAG, "epgProgramListAdapter isUnlocked lockFlag:" + lockFlag);
                boolean isUnlocked = false;
                if (epgPlaybackManager != null) {
                    isUnlocked = epgPlaybackManager.getCurrentUnlockStateManager().isUnlocked(lockFlag);
                }
                return isUnlocked;
            }
        });

        rvEpgProgramList.setAdapter(epgProgramListAdapter);
    }

    private void updateProgramData() {
        List<Program> programList = new ArrayList<>();
        if (dateListPaginationManager != null && channelListPaginationManager != null) {
            Channel ch = (Channel)channelListPaginationManager.getCurrentFocusItem();
            Long searchTime = (Long)dateListPaginationManager.getCurrentFocusItem();
            if (ch instanceof Channel && searchTime instanceof Long) {
                if (TimeUtils.isStartOfLocalDay(searchTime)) {
                    programList = ProgramUtils.getUpcomingProgramsForDate(requireContext(), ch.getId(), (long)searchTime);
                } else {
                    programList = ProgramUtils.getRemainingProgramsForDate(requireContext(), ch.getId(), (long)searchTime);
                }
            }
        }

        if (programList.isEmpty()) {
            programList.add(ProgramUtils.createEmptyProgram(requireContext()));
        }

        programListPaginationManager.updateData(programList);
        epgProgramListAdapter.updateListWithoutFocus(programListPaginationManager.getCurrentPageData());
    }

    private void updateProgramDataAndFocusLastItem(boolean isFocusLastItem) {
        List<Program> programList = new ArrayList<>();
        if (dateListPaginationManager != null && channelListPaginationManager != null) {
            Channel ch = (Channel)channelListPaginationManager.getCurrentFocusItem();
            Long searchTime = (Long)dateListPaginationManager.getCurrentFocusItem();
            if (ch instanceof Channel && searchTime instanceof Long) {
                if (TimeUtils.isStartOfLocalDay(searchTime)) {
                    programList = ProgramUtils.getUpcomingProgramsForDate(requireContext(), ch.getId(), (long)searchTime);
                } else {
                    programList = ProgramUtils.getRemainingProgramsForDate(requireContext(), ch.getId(), (long)searchTime);
                }
            }
        }

        if (programList.isEmpty()) {
            programList.add(ProgramUtils.createEmptyProgram(requireContext()));
        }

        programListPaginationManager.updateData(programList);
        if (isFocusLastItem) {
            programListPaginationManager.setPageAndFocusByFullListIndex(programListPaginationManager.getFullData().size() - 1);
        }
        epgProgramListAdapter.updateList(programListPaginationManager.getCurrentPageData(), isFocusLastItem);
    }

    public void refreshEpgByGenre(GenreData.GenreInfo genreInfo) {
        updateDateData();
        updateChannelData(genreInfo);
        rvEpgChannelList.post(() -> rvEpgChannelList.requestFocus());
        if (channelListPaginationManager != null) {
            Channel ch = (Channel)channelListPaginationManager.getCurrentFocusItem();
            Log.d(TAG, "refreshEpgByGenre ch:" + ch.getDisplayName());
            if (epgPlaybackManager != null) {
                epgPlaybackManager.tuneEpgChannel(ch);
            }
        }
    }

    public void updateEpgChannelAndProgram() {
        epgChannelListAdapter.updateListAndSetFocus(channelListPaginationManager.getCurrentPageData(), channelListPaginationManager.getCurrentFocusIndex());
        updateProgramData();
    }

    public void updateEpgChannelInfo(Channel ch, int lockFlag) {
        if (ch == null) {
            Log.d(TAG, "updateEpgChannelInfo channel is null, return !");
            return;
        }

        String msg = "updateEpgChannelInfo channelId:" + ch.getId() + ", displayNum:" + ch.getDisplayNumber() + ", displayName:" + ch.getDisplayName();
        Log.d(TAG, msg);

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        boolean isFavorite = ChannelUtils.isChannelFavorite(getContext(), ch);
        tvEpgBottomBarYellow.setText(isFavorite ? getContext().getString(R.string.delete_favourite) : getContext().getString(R.string.add_favourite));
        boolean isUnlocked = false;
        if (epgPlaybackManager != null) {
            isUnlocked = epgPlaybackManager.getCurrentUnlockStateManager().isUnlocked(lockFlag);
            Log.d(TAG, "updateEpgChannelInfo highest priority lock_flag:" + lockFlag);
            Log.d(TAG, "updateEpgChannelInfo isUnlocked:" + isUnlocked);
        }

        tvEpgChannelNumber.setText(ch.getDisplayNumber());
        tvEpgChannelName.setText(ch.getDisplayName());

        if ((lockFlag == LockManager.LOCK_NONE) ||
                (lockFlag == LockManager.LOCK_WORK_HOUR && isUnlocked)) {
            ivEpgChannelPadlock.setVisibility(View.GONE);
        } else {
            ivEpgChannelPadlock.setVisibility(View.VISIBLE);
            if (isUnlocked) {
                // except LOCK_WORK_HOUR
                ivEpgChannelPadlock.setImageResource(R.drawable.icon_ch_unlock);
            } else {
                ivEpgChannelPadlock.setImageResource(R.drawable.icon_ch_lock);
            }
        }

        if("SERVICE_TYPE_AUDIO".equals(ch.getServiceType())) {
            ivEpgChannelMusic.setVisibility(View.VISIBLE);
        } else {
            ivEpgChannelMusic.setVisibility(View.GONE);
        }

        ivEpgChannelFavorite.setVisibility(isFavorite ? View.VISIBLE : View.GONE);

        if (epgPlaybackManager != null) {
            ViewUtils.setQualityIcon(ivEpgChannelResolution, epgPlaybackManager.getCurrentVideoResolutionLabel());
        } else {
            ivEpgChannelResolution.setVisibility(View.GONE);
        }

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        List<ProgramInfo> programInfoList = currentEpgChannelListManager.getProgramInfoList();
        ProgramInfo programInfo;
        if (programInfoList == null || programInfoList.isEmpty())
            programInfo = ChannelUtils.getProgramInfoByChannel(ch);
        else
            programInfo = programInfoList.get(dateListPaginationManager.getCurrentFocusIndex());

        boolean isDolby = ChannelUtils.supportDolby(programInfo);
        ivEpgChannelDolby.setVisibility(isDolby ? View.VISIBLE : View.GONE);
    }

    public void updateEpgProgramInfo(Program pg, int lockFlag) {
        Log.d(TAG, "updateEpgProgramInfo program:" + ((pg != null) ? pg.getTitle() : "null") + ", lockFlag:" + lockFlag);
        boolean isUnlocked = false;
        if (epgPlaybackManager != null) {
            // TODO: [workaround] force LOCK_EPG_PROGRAM_INFO for unlock
            isUnlocked = epgPlaybackManager.getCurrentUnlockStateManager().isUnlocked(LockManager.LOCK_EPG_PROGRAM_INFO);
        }

        String programTitle = requireContext().getString(R.string.channel_list_parental_rated_program);
        String programDescription = requireContext().getString(R.string.channel_list_parental_rated_program);
        if (lockFlag == LockManager.LOCK_NONE ||
                lockFlag == LockManager.LOCK_WORK_HOUR ||
                isUnlocked) {
            llEpgProgramMask.setVisibility(View.GONE);
            rvEpgProgramList.setVisibility(View.VISIBLE);
            if (pg != null) {
                programTitle = pg.getTitle();
                programDescription = pg.getLongDescription();
            } else {
                programTitle = getString(R.string.no_program_info);
                programDescription = getString(R.string.none);
            }
        } else if (lockFlag == LockManager.LOCK_PARENTAL_PROGRAM) {
            // hide record and reminder
            llEpgBottomBarRed.setVisibility(View.GONE);
            llEpgBottomBarGreen.setVisibility(View.GONE);

            llEpgProgramMask.setVisibility(View.GONE);
            rvEpgProgramList.setVisibility(View.VISIBLE);
        } else {
            llEpgProgramMask.setVisibility(View.VISIBLE);
            rvEpgProgramList.setVisibility(View.GONE);
        }

        if (pg != null) {
            tvEpgProgramName.setText(programTitle);
            tvEpgProgramStart.setText(TimeUtils.formatToLocalTime(pg.getStartTimeUtcMillis(), "HH:mm"));
            ViewUtils.setProgressWithMillisMax(pbEpgProgram, pg.getStartTimeUtcMillis(), pg.getEndTimeUtcMillis());
            tvEpgProgramEnd.setText(TimeUtils.formatToLocalTime(pg.getEndTimeUtcMillis(), "HH:mm"));
            ViewUtils.setRatingIcon(ivEpgProgramRating, pg.getContentRatings());

            List<String> audioLanguageList = new ArrayList<>();
            if (epgPlaybackManager != null) {
                audioLanguageList = epgPlaybackManager.getCurrentAudioLanguageList();
            }
            ivEpgProgramBilingual.setVisibility((audioLanguageList.size() > 1) ? View.VISIBLE : View.GONE);

            List<String> subtitleLanguageList = new ArrayList<>();
            if (epgPlaybackManager != null) {
                subtitleLanguageList = epgPlaybackManager.getCurrentSubtitleLanguages();
            }
            tvEpgSubtitle.setVisibility((subtitleLanguageList.size() > 1) ? View.VISIBLE : View.GONE);

            if(!Pvcfg.get_hideLauncherPvr()) {//eric lin 20251229 hide reminder
                if (ProgramReminderUtils.doesReminderExist(requireContext(), pg.getId())) {
                    ivEpgProgramReminder.setVisibility(View.VISIBLE);
                    tvEpgBottomBarGreen.setText(requireContext().getString(R.string.reminder_epg_cancel));
                } else {
                    ivEpgProgramReminder.setVisibility(View.GONE);
                    tvEpgBottomBarGreen.setText(requireContext().getString(R.string.reminder_epg_watch));
                }
            }

            boolean isRecording = false, isScheduledRecord = false;
            ScheduledProgramData scheduledProgramData = ScheduledProgramUtils.getScheduledProgram(requireContext(), pg.getId());
            if (scheduledProgramData != null) {
                if (TimeUtils.isInTimeRange(scheduledProgramData.getStartTimeUtcMillis(),
                        scheduledProgramData.getEndTimeUtcMillis())) {
                    isRecording = true;
                } else {
                    isScheduledRecord = true;
                    // TODO: check series record
                    if (false) {
                        ivEpgProgramScheduleRecord.setImageResource(R.drawable.icon_miniguide_rec_resv_sereis);
                    } else {
                        ivEpgProgramScheduleRecord.setImageResource(R.drawable.icon_miniguide_rec_resv);
                    }
                }
            }
            ivEpgProgramNowRecording.setVisibility(isRecording ? View.VISIBLE : View.GONE);
            ivEpgProgramScheduleRecord.setVisibility(isScheduledRecord ? View.VISIBLE : View.GONE);

            tvEpgProgramDescription.setText(programDescription);
        } else {
            tvEpgProgramName.setText(programTitle);
            tvEpgProgramStart.setText("00:00");
            tvEpgProgramEnd.setText("00:00");

            ivEpgProgramRating.setVisibility(View.GONE);
            tvEpgSubtitle.setVisibility(View.GONE);
            ivEpgProgramBilingual.setVisibility(View.GONE);
            ivEpgProgramReminder.setVisibility(View.GONE);
            ivEpgProgramNowRecording.setVisibility(View.GONE);
            ivEpgProgramScheduleRecord.setVisibility(View.GONE);

            tvEpgProgramDescription.setText(programDescription);
        }
    }

    private List<LinearLayout> bottomBarItems;
    enum BottomBarMode {
        FOCUS_DATE, FOCUS_CHANNEL, FOCUS_CURRENT_PROGRAM, FOCUS_PROGRAM
    }
    private void updateBottomBar(BottomBarMode mode) {
        List<LinearLayout> visibleItems = new ArrayList<>();
        visibleItems.add(llEpgBottomBarDirection);
        visibleItems.add(llEpgBottomBarBack);
        switch (mode) {
            case FOCUS_DATE:
                visibleItems.add(llEpgBottomBarBlue);
                break;
            case FOCUS_CHANNEL:
                visibleItems.add(llEpgBottomBarYellow);
                visibleItems.add(llEpgBottomBarBlue);
                visibleItems.add(llEpgBottomBarOk);
                break;
            case FOCUS_CURRENT_PROGRAM:
                if (!Pvcfg.get_hideLauncherPvr() && hasValidProgram()) {//eric lin 20251224 hide launcher pvr
                    visibleItems.add(llEpgBottomBarRed);
                }
                visibleItems.add(llEpgBottomBarBlue);
                visibleItems.add(llEpgBottomBarOk);
                break;
            case FOCUS_PROGRAM:
                //eric lin 20251224 hide launcher pvr //eric lin 20251229 hide reminder
                if (!Pvcfg.get_hideLauncherPvr() && hasValidProgram()) {
                    visibleItems.add(llEpgBottomBarRed);
                    visibleItems.add(llEpgBottomBarGreen);
                }

                visibleItems.add(llEpgBottomBarYellow);
                visibleItems.add(llEpgBottomBarBlue);
                visibleItems.add(llEpgBottomBarOk);
                visibleItems.add(llEpgBottomBarBack);
            default:
                break;
        }

        for (int i = 0; i < bottomBarItems.size(); i++) {
            LinearLayout item = bottomBarItems.get(i);
            if (visibleItems.contains(item)) {
                item.setVisibility(View.VISIBLE);
            } else {
                item.setVisibility(View.GONE);
            }
        }
    }

    public boolean handleKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            Log.d(TAG, "handleKeyEvent keyCode:" + keyCode);
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (popupWindowGenreList != null && popupWindowGenreList.isShowing()) {
                    hideGenreList();
                } else {
                    if (epgPlaybackManager != null) {
                        epgPlaybackManager.backToLiveFromEpg();
                    }
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
                // check lock
                if (epgPlaybackManager != null) {
                    epgPlaybackManager.showParentalPinDialog();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (isVisible(clEpgEnterChannelNumber)) {
                    epgEnterChannelNumberTimeout();
                    return true;
                }
            } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                handleEpgEnterChannelNumber(keyCode - KeyEvent.KEYCODE_0);
                return true;
            }
        }
        return false;
    }

    private void epgEnterChannelNumberTimeout() {
        epgHandler.removeCallbacks(epgEnterChannelNumberTimeoutRunnable);
        List<Channel> channelList = channelListPaginationManager.getFullData();
        for (int i = 0; i < channelList.size(); i++) {
            if (StringUtils.normalizeInputNumber(epgEnterChannelNumber).equals(channelList.get(i).getDisplayNumber())) {
                clearEpgEnterChannelNumber();
                channelListPaginationManager.setPageAndFocusByFullListIndex(i);
                epgChannelListAdapter.updateListAndSetFocus(channelListPaginationManager.getCurrentPageData(), channelListPaginationManager.getCurrentFocusIndex());

                epgDateListAdapter.drawSelectItem(dateListPaginationManager.getCurrentFocusIndex());
                return;
            }
        }

        clearEpgEnterChannelNumber();
    }

    private void clearEpgEnterChannelNumber() {
        Log.d(TAG, "clearEpgEnterChannelNumber");
        epgHandler.removeCallbacks(epgEnterChannelNumberTimeoutRunnable);
        clEpgEnterChannelNumber.setVisibility(View.GONE);
        epgEnterChannelNumber = "";
        tvEpgEnterChannelNumber.setText("");
    }

    private void handleEpgEnterChannelNumber(int number) {
        clEpgEnterChannelNumber.setVisibility(View.VISIBLE);

        epgEnterChannelNumber += number;
        tvEpgEnterChannelNumber.setText(StringUtils.padToNDigits(epgEnterChannelNumber, 3));
        if (epgEnterChannelNumber.length() >= MAX_EPG_ENTER_CHANNEL_NUMBER_LENGTH) {
            // jump channel directly
            List<Channel> channelList = channelListPaginationManager.getFullData();
            for (int i = 0; i < channelList.size(); i++) {
                if (StringUtils.normalizeInputNumber(epgEnterChannelNumber).equals(channelList.get(i).getDisplayNumber())) {
                    clearEpgEnterChannelNumber();
                    channelListPaginationManager.setPageAndFocusByFullListIndex(i);
                    epgChannelListAdapter.updateListAndSetFocus(channelListPaginationManager.getCurrentPageData(), channelListPaginationManager.getCurrentFocusIndex());
                    return;
                }
            }
        } else {
            resetHandlerDelay(epgHandler, epgEnterChannelNumberTimeoutRunnable, EPG_ENTER_CHANNEL_NUMBER_TIMEOUT_MS, "epgEnterChannelNumberTimeout");
            return;
        }

        if (!StringUtils.isOnlyZeros(epgEnterChannelNumber)) {
            // TODO: channel not found message in EPG
            //ViewUtils.showToast(requireContext(), Toast.LENGTH_LONG, getString(R.string.format_toast_no_such_channel, epgEnterChannelNumber));
        }
        clearEpgEnterChannelNumber();
    }

    private void resetHandlerDelay(Handler h, Runnable runnable, long delayMillis, String tag) {
        Log.d(TAG, "resetHandlerDelay [" + tag + "] delay: " + delayMillis + " ms");
        h.removeCallbacks(runnable);
        h.postDelayed(runnable, delayMillis);
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

    private int findLastChannelIndex(List<Channel> chList) {
        if (chList == null || chList.isEmpty()) {
            return -1;
        }

        String channelDisplayNumber = "0";
        if (!TextUtils.isEmpty(GlobalState.lastWatchedChannelNumber)) {
            channelDisplayNumber = GlobalState.lastWatchedChannelNumber;
        }

        return findChannelIndexByDisplayNumber(chList, channelDisplayNumber);
    }

    private int findChannelIndexByDisplayNumber(List<Channel> chList, String displayNumber) {
        if (chList == null || chList.isEmpty() || TextUtils.isEmpty(displayNumber)) {
            return -1;
        }

        for (int i = 0; i < chList.size(); i++) {
            Channel ch = chList.get(i);
            if (displayNumber.equals(ch.getDisplayNumber())) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    private boolean hasValidProgram() {
        Context context = getContext();
        if (programListPaginationManager == null || context == null) {
            return false;
        }

        List<Program> programList = programListPaginationManager.getFullData();
        if (programList.isEmpty()) {
            return false;
        }

        if (programList.size() == 1) {
            return !ProgramUtils.isEmptyProgram(context, programList.get(0));
        }

        return true;
    }
}