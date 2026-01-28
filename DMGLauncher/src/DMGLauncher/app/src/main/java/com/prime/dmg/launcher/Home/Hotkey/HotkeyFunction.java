package com.prime.dmg.launcher.Home.Hotkey;

import static com.prime.dmg.launcher.Home.LiveTV.LiveTvManager.*;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.PVR.Management.RecordProgramsActivity;
import com.prime.dmg.launcher.R;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.PrimeHomeReceiver;
import com.prime.dtv.sysdata.AudioInfo;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.PvrRecFileInfo;
import com.prime.dtv.sysdata.SubtitleInfo;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HotkeyFunction extends DialogFragment implements PrimeHomeReceiver.Callback {

    String TAG = HotkeyFunction.class.getSimpleName();

    static boolean PVR_PLAYBACK_MODE = false;
    static boolean PVR_TIMESHIFT_MODE = false;

    WeakReference<AppCompatActivity> g_ref;
    Handler gHandler;
    Runnable g_destroyAction;
    PrimeDtv g_dtv;

    String TAG_FUNCTION;
    String TAG_AUDIO;
    String TAG_CAPTION;
    int[] FUNC_ITEMS  = {R.string.dialog_item_audio, R.string.dialog_item_caption};

    long gChannelId;
    HashMap<Long, Integer> gAudioFocusMap = new HashMap<>();
    HashMap<Long, Integer> gSubtitleFocusMap = new HashMap<>();
    int g_focusIndex = 0;
    int gIntroTime;

    // receiver
    PrimeHomeReceiver g_homeReceiver;

    public HotkeyFunction(AppCompatActivity activity) {
        g_ref        = new WeakReference<>(activity);
        gHandler     = new Handler(Looper.getMainLooper());
        TAG_FUNCTION = get().getString(R.string.dialog_function_menu);
        TAG_AUDIO    = get().getString(R.string.dialog_item_audio);
        TAG_CAPTION  = get().getString(R.string.dialog_item_caption);
        if (activity instanceof HomeActivity)
            g_dtv = ((HomeActivity) activity).g_dtv;
        if (activity instanceof RecordProgramsActivity)
            g_dtv = ((RecordProgramsActivity) activity).get_prime_dtv();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // full screen
        setStyle(DialogFragment.STYLE_NORMAL,
                 android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

        // without the title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // remove background shadow
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // custom layout
        View rootView = inflater.inflate(R.layout.live_tv_hotkey_function_dialog,
                                         container, false);

        if (PVR_PLAYBACK_MODE || PVR_TIMESHIFT_MODE)
            hide_hint(rootView);

        rootView.setTag(TAG_FUNCTION);
        init_menu(rootView);
        register_home_receiver();
        set_timeout_dismiss();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (g_destroyAction != null) {
            g_destroyAction.run();
            g_destroyAction = null;
        }
    }

    public void on_click(TextView clickedView, View rootView, int focusIndex) {
        clickedView.setOnClickListener(v -> {
            String clickedText = (String) clickedView.getText();
            Log.d(TAG, "on_click: " + clickedText + ", focus " + focusIndex);

            if (is_function(rootView))      show_selections(clickedView, rootView, focusIndex);
            else if (is_audio(rootView))    change_audio(focusIndex);
            else if (is_subtitle(rootView)) change_subtitle(focusIndex);

            set_timeout_dismiss();
        });
    }

    public void on_key(TextView clickedView, View root) {
        clickedView.setOnKeyListener((v, keyCode, event) -> {

            set_timeout_dismiss();

            if (is_function(root))
                return false;

            if (KeyEvent.KEYCODE_BACK == keyCode &&
                KeyEvent.ACTION_DOWN  == event.getAction()) {
                Log.d(TAG, "on_key: " + root.getTag());
                root.setTag(TAG_FUNCTION);
                init_menu(root);
                return true;
            }
            return false;
        });
    }

    @Override
    public void on_press_home() {
        Log.d(TAG, "on_home_pressed: close dialog");
        get().unregisterReceiver(g_homeReceiver);
        dismiss();
    }

    public void init_menu(View rootView) {
        TextView     titleTxv  = rootView.findViewById(R.id.lo_dialog_func_title);
        LinearLayout listView  = rootView.findViewById(R.id.lo_dialog_func_list_root);
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(gChannelId);
        List<String> nameList   = get_name_list(rootView);
        int          focusIndex = 0;

        titleTxv.setText((String) rootView.getTag());
        listView.removeAllViews();

        for (String name : nameList) {
            TextView clickedView = get_clicked_view(name);
            on_click(clickedView, rootView, focusIndex);
            on_key(clickedView, rootView);
            listView.addView(clickedView);
            focusIndex++;
        }
        listView.requestFocus();

        if (is_function(rootView)) {
            change_focus(listView, g_focusIndex);
            Log.d(TAG, "init_menu: focus " + g_focusIndex);
        }
        else if (is_audio(rootView)) {
            int focusPos = 0;
            if(PVR_PLAYBACK_MODE)
                gAudioFocusMap.put(gChannelId, g_dtv.pvr_PlayGetCurrentAudioIndex(DEFAULT_TUNER_ID));
            else
                gAudioFocusMap.put(gChannelId, programInfo.getAudioSelected());
            focusPos = get_focus_position(gAudioFocusMap);
            change_focus(listView, focusPos);
            Log.d(TAG, "init_menu: audio focus " + focusPos);
        }
        else if (is_subtitle(rootView)) {
            int focusPos = get_focus_position(gSubtitleFocusMap);
            change_focus(listView, focusPos);
            Log.d(TAG, "init_menu: subtitle focus " + focusPos);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void register_home_receiver() {
        if (g_homeReceiver == null)
            g_homeReceiver = new PrimeHomeReceiver();
        g_homeReceiver.register_callback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        get().registerReceiver(g_homeReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    public HotkeyFunction enable_playback_mode() {
        PVR_PLAYBACK_MODE = true;
        return this;
    }

    public HotkeyFunction enable_timeshift_mode() {
        PVR_TIMESHIFT_MODE = true;
        return this;
    }

    public void set_channel_id(long channelId) {
        gChannelId = channelId;
    }

    public void set_intro_time(int introTime) {
        gIntroTime = introTime;
    }

    public void set_timeout_dismiss() {
        gHandler.removeCallbacksAndMessages(null);
        gHandler.postDelayed(this::dismiss, gIntroTime);
    }

    public void set_destroy_action(Runnable destroyAction) {
        g_destroyAction = destroyAction;
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    public TextView get_clicked_view(String itemName) {
        TextView itemTxv = new TextView(get());
        itemTxv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                get().getResources().getDimensionPixelSize(R.dimen.live_tv_dialog_item_height)
        ));
        itemTxv.setText(itemName);

        int textSize = (int) (get().getResources().getDimensionPixelSize(R.dimen.live_tv_dialog_item_text_size) / get().getResources().getDisplayMetrics().density);
        itemTxv.setTextSize(textSize);
        itemTxv.setFocusable(true);
        itemTxv.setGravity(Gravity.CENTER);
        itemTxv.setOnFocusChangeListener((v, hasFocus) -> {
            itemTxv.setTextColor(ContextCompat.getColor(get(),
                    hasFocus ? R.color.black : R.color.white));
            itemTxv.setBackgroundColor(ContextCompat.getColor(get(),
                    hasFocus ? R.color.pvr_red_color : R.color.trans));
            itemTxv.setTypeface(null,
                    hasFocus ? Typeface.BOLD : Typeface.NORMAL);
        });

        return itemTxv;
    }

    public List<String> get_name_list(View rootView) {
        List<String> nameList = new ArrayList<>();

        if (is_function(rootView)) {
            for (int resId : FUNC_ITEMS)
                nameList.add(getString(resId));
            return nameList;
        }

        if (is_audio(rootView)) {
            AudioInfo audioInfo = PVR_PLAYBACK_MODE
                    ? get_playback_audio_info()
                    : g_dtv.av_control_get_audio_list_info(DEFAULT_TUNER_ID);

            if (audioInfo == null) {
                nameList.add(getString(R.string.dialog_lang_none));
                return nameList;
            }

            if (audioInfo.getComponentCount() <= 0) {
                nameList.add(getString(R.string.dialog_lang_none));
                return nameList;
            }

            if (audioInfo.getComponentCount() == 1) {
                nameList.add(getString(R.string.dialog_audio_pri));
                return nameList;
            }

            if (audioInfo.getComponentCount() > 1) {
                for (AudioInfo.AudioComponent component : audioInfo.ComponentList) {
                    String langCode = component.getLangCode();
                    Log.d(TAG, "get_name_list: langCode = " + langCode);
                    if (null == langCode)
                        nameList.add(getString(R.string.dialog_lang_none));
                    else if (langCode.equalsIgnoreCase("pri") ||
                             langCode.equalsIgnoreCase("chi") ||
                             langCode.isEmpty())
                        nameList.add(getString(R.string.dialog_audio_pri));
                    else if (langCode.equalsIgnoreCase("sec"))
                        nameList.add(getString(R.string.dialog_audio_sec));
                    else
                        nameList.add(getString(R.string.dialog_und));
                }
                return nameList;
            }
        }

        if (is_subtitle(rootView)) {
            SubtitleInfo subtitleInfo = PVR_PLAYBACK_MODE
                    ? get_playback_subtitle_info()
                    : g_dtv.av_control_get_subtitle_list(DEFAULT_TUNER_ID);

            if (null == subtitleInfo) {
                nameList.add(getString(R.string.dialog_lang_none));
                return nameList;
            }

            if (subtitleInfo.getComponentCount() <= 0) {
                nameList.add(getString(R.string.dialog_lang_none));
                return nameList;
            }

            if (subtitleInfo.getComponentCount() > 0) {
                for (SubtitleInfo.SubtitleComponent component : subtitleInfo.Component) {
                    if (null == component.getLangCode())
                        nameList.add(getString(R.string.dialog_lang_none));
                    else
                        nameList.add(SubtitleInfo.SubtitleComponent.getLangString(getContext(),component.getLangCode()));
                }
                return nameList;
            }
        }

        nameList.add(getString(R.string.dialog_lang_none));
        return nameList;
    }

    public PvrRecFileInfo get_current_record() {
        if (get() instanceof RecordProgramsActivity)
            return ((RecordProgramsActivity) get()).get_current_record();
        else
            return null;
    }

    public AudioInfo get_playback_audio_info() {
        PvrRecFileInfo fileInfo = get_current_record();
        if (!PVR_PLAYBACK_MODE || null == fileInfo)
            return null;

        List<ProgramInfo.AudioInfo> audioInfoList = fileInfo.getAudiosInfoList();
        if (audioInfoList == null)
            return null;

        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setCurPos(g_dtv.pvr_PlayGetCurrentAudioIndex(DEFAULT_TUNER_ID));
        for (ProgramInfo.AudioInfo info : audioInfoList) {
            AudioInfo.AudioComponent audioComponent = new AudioInfo.AudioComponent();
            audioComponent.setPid(info.getPid());
            audioComponent.setAudioType(info.getCodec());
            audioComponent.setAdType(0);
            audioComponent.setTrackMode(-1);
            audioComponent.setLangCode(info.getLeftIsoLang());
            audioComponent.setPos(audioInfoList.indexOf(info));
            audioInfo.ComponentList.add(audioComponent);
        }

        return audioInfo;
    }

    public SubtitleInfo get_playback_subtitle_info() {
        PvrRecFileInfo fileInfo = get_current_record();
        if (!PVR_PLAYBACK_MODE || null == fileInfo)
            return null;

        List<ProgramInfo.SubtitleInfo> subtitleInfoList = fileInfo.getSubtitleInfo();
        if (subtitleInfoList == null)
            return null;

        SubtitleInfo subtitleInfo = new SubtitleInfo();
        subtitleInfo.setCurPos(0);
        for (ProgramInfo.SubtitleInfo info : subtitleInfoList) {
            SubtitleInfo.SubtitleComponent subtitleComponent = new SubtitleInfo.SubtitleComponent();
            subtitleComponent.setPid(info.getPid());
            subtitleComponent.setLangCode(info.getLang());
            subtitleComponent.setComPageId(info.getComPageId());
            subtitleComponent.setAncPageId(info.getAncPageId());
            subtitleInfo.Component.add(subtitleComponent);
        }
        return subtitleInfo;
    }

    private int get_focus_position(HashMap<Long, Integer> map) {
        if (0 == gChannelId) {
            Log.e(TAG, "get_focus_position: null event");
            return 0;
        }
        Integer focusPos = map.get(gChannelId);
        focusPos = focusPos == null ? 0 : focusPos;
        return focusPos;
    }

    public boolean is_function(View rootView) {
        return TAG_FUNCTION.equals(rootView.getTag());
    }

    public boolean is_audio(View rootView) {
        return TAG_AUDIO.equals(rootView.getTag());
    }
    
    public boolean is_subtitle(View rootView) {
        return TAG_CAPTION.equals(rootView.getTag());
    }

    public void show() {
        super.show(get().getSupportFragmentManager(), TAG);
    }

    public void show_selections(TextView clickedView, View rootView, int focusIndex) {
        g_focusIndex = focusIndex;
        rootView.setTag(clickedView.getText());
        init_menu(rootView);
    }

    public void hide_hint(View rootView) {
        if (rootView != null) {
            View bottomHint = rootView.findViewById(R.id.lo_dialog_hotkey_hint);
            if (bottomHint != null)
                bottomHint.setVisibility(View.GONE);
        }
    }

    public void change_audio(int focusIndex) {

        AudioInfo audioInfo = PVR_PLAYBACK_MODE
                ? get_playback_audio_info()
                : g_dtv.av_control_get_audio_list_info(DEFAULT_TUNER_ID);

        //AudioInfo audioInfo = g_dtv.av_control_get_audio_list_info(DEFAULT_TUNER_ID);
        if (null == audioInfo) {
            Log.w(TAG, "change_audio: null info");
            dismiss();
            return;
        }
        if (null == audioInfo.getComponent(focusIndex)) {
            Log.w(TAG, "change_audio: null component");
            dismiss();
            return;
        }

        if (PVR_PLAYBACK_MODE)
            g_dtv.pvr_PlayChangeAudioTrack(DEFAULT_TUNER_ID, audioInfo.getComponent(focusIndex));
        else if (PVR_TIMESHIFT_MODE)
            g_dtv.pvr_TimeShiftPlayChangeAudioTrack(DEFAULT_TUNER_ID, audioInfo.getComponent(focusIndex));
        else {
            g_dtv.av_control_change_audio(DEFAULT_TUNER_ID, audioInfo.getComponent(focusIndex));
            if (0 == gChannelId) {
                Log.e(TAG, "change_audio: null event");
                dismiss();
                return;
            }
            gAudioFocusMap.put(gChannelId, focusIndex);
        }

        dismiss();
    }

    public void change_subtitle(int focusIndex) {
        SubtitleInfo subtitleInfo = g_dtv.av_control_get_subtitle_list(DEFAULT_TUNER_ID);
        if (null == subtitleInfo) {
            Log.e(TAG, "change_subtitle: null info");
            dismiss();
            return;
        }
        if (null == subtitleInfo.getComponent(focusIndex)) {
            Log.e(TAG, "change_subtitle: null component");
            dismiss();
            return;
        }

        // TODO: change subtitle
        if (PVR_PLAYBACK_MODE)
            Log.e(TAG, "change_subtitle: playback change subtitle");
        else if (PVR_TIMESHIFT_MODE)
            Log.e(TAG, "change_subtitle: timeshift change subtitle");
        else {
            g_dtv.av_control_select_subtitle(DEFAULT_TUNER_ID, subtitleInfo.getComponent(focusIndex));
            if ( 0 == gChannelId) {
                Log.e(TAG, "change_subtitle: null event");
                dismiss();
                return;
            }
            gSubtitleFocusMap.put(gChannelId, focusIndex);
        }
        dismiss();
    }

    private void change_focus(LinearLayout listView, int focusPos) {
        View focusView = listView.getChildAt(focusPos);
        focusView = focusView == null ? listView.getChildAt(0) : focusView;
        focusView.requestFocus();
    }

    @Override
    public void dismiss() {
        if (g_destroyAction != null) {
            g_destroyAction.run();
            g_destroyAction = null;
        }
        super.dismiss();
    }
}
