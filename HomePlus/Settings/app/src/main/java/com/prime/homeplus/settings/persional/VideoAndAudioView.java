package com.prime.homeplus.settings.persional;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

@SuppressLint("NewApi")
public class VideoAndAudioView extends ThirdLevelView {
    private String TAG = "HomePlus-VideoAndAudioView";
    private SettingsRecyclerView settingsRecyclerView;


    public VideoAndAudioView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_video_audio;
    }

    private Button btnVideoAudio;
    private TextView tvAudio, tvAspectRatio, tvHDMICEC, tvHDMIHDCP, tvHDMIResolution;

    private void updatePopupUI(){
        updateResolutionUI();
        updateAudioUI();
        updateAspectRatioUI();
        updateCecAndHdcpUI();
    }

    private void updateMainSummary() {
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if (gposInfo == null) return;

        // Resolution
        int resId = GposInfo.getResolution(getContext());
        String[] resItems = PrimeUtils.get_supported_resolution_list(getContext());
        // Careful with index bounds, though typically GPOS index matches the list
        if (resId >= 0 && resId < resItems.length) {
            tvHDMIResolution.setText(resItems[resId]);
        }

        // Audio
        String lang = GposInfo.getAudioLanguageSelection(getContext(), 0);
        String chi = getContext().getString(R.string.settings_av_setting_lang_chinese);
        String eng = getContext().getString(R.string.settings_av_setting_lang_english);
        if ("chi".equals(lang)) {
            tvAudio.setText(chi);
        } else {
            tvAudio.setText(eng);
        }

        // Aspect Ratio
        int ratioId = GposInfo.getScreen16x9(getContext());
        String[] ratioItems = getResources().getStringArray(R.array.aspect_ratio_list);
        if (ratioId >= 0 && ratioId < ratioItems.length) {
            tvAspectRatio.setText(ratioItems[ratioId]);
        }

        // CEC
        int cec = GposInfo.getHdmiCecOnOff(getContext());
        String hdmicec = (cec == 1) ? getContext().getString(R.string.settings_av_setting_cecon) : getContext().getString(R.string.settings_av_setting_cecoff);
        tvHDMICEC.setText(hdmicec);

        // HDCP
        int hdcp = GposInfo.getHDCPOnOff(getContext());
        String hdmihdcp;
        if(hdcp == 1){
            hdmihdcp = getContext().getString(R.string.settings_av_setting_auto);
        }else{
            hdmihdcp = "1.4";
        }
        tvHDMIHDCP.setText(hdmihdcp);
    }

    public void onFocus() {
        init_setting();
        updateMainSummary();
        btnVideoAudio.requestFocus();
    }

    public void onViewPaused() {
    }

    public void onViewResumed() {
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        btnVideoAudio = (Button) findViewById(R.id.btnVideoAudio);

        btnVideoAudio.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        btnVideoAudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopWindow();
            }
        });
        tvAudio = findViewById(R.id.tvAudio);
        tvAspectRatio = findViewById(R.id.tvAspectRatio);
        tvHDMICEC = findViewById(R.id.tvHDMICEC);
        tvHDMIHDCP = findViewById(R.id.tvHDMIHDCP);
        tvHDMIResolution = findViewById(R.id.tvHDMIResolution);
    }

    private View popupView;
    private static PopupWindow popupWindow;
    private Button btnSave, btnCancel, btnAudio, btnAspectRatio, btnResolution;
    private TextView tvAudioItem, tvAudioSelect, tvAspectRatioItem, tvAspectRatioSelect, tvResolutionItem, tvResolutionSelect;
    private RadioButton rbtnCECOn, rbtnCECOff, rbtnHDCPAuto, rbtnHDCP14;
    String[] audioItems , aspectRatioItems, ResolutionItems;
    private int audioIndex = 0, aspectRatioIndex = 0, resolutionIndex = 0;
    private int confirmedAudioIndex = 0, confirmedAspectRatioIndex = 0, confirmedResolutionIndex = 0;

    private void set_OnKeyListener(View v){
        v.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                    } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        if (v.getId() == R.id.btnAudio) {
                            confirmedAudioIndex = audioIndex;
                            updateAudioUI();
                        } else if (v.getId() == R.id.btnAspectRatio) {
                            confirmedAspectRatioIndex = aspectRatioIndex;
                            updateAspectRatioUI();
                        } else if (v.getId() == R.id.btnResolution) {
                            confirmedResolutionIndex = resolutionIndex;
                            updateResolutionUI();
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        nextItem(keyCode, v);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        nextItem(keyCode, v);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_video_and_audio, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        tvResolutionItem = popupView.findViewById(R.id.tvResolution);
        tvResolutionSelect = popupView.findViewById(R.id.tvResolutionSelect);
        btnResolution = popupView.findViewById(R.id.btnResolution);
        set_OnKeyListener(btnResolution);

        tvAudioItem = popupView.findViewById(R.id.tvAudio);
        tvAudioSelect = popupView.findViewById(R.id.tvAudioSelect);
        btnAudio = popupView.findViewById(R.id.btnAudio);
        set_OnKeyListener(btnAudio);
        audioItems = getResources().getStringArray(R.array.audio_language_list);

        tvAspectRatioItem = popupView.findViewById(R.id.tvAspectRatio);
        tvAspectRatioSelect = popupView.findViewById(R.id.tvAspectRatioSelect);
        btnAspectRatio = popupView.findViewById(R.id.btnAspectRatio);
        set_OnKeyListener(btnAspectRatio);
        aspectRatioItems = getResources().getStringArray(R.array.aspect_ratio_list);

        rbtnCECOn = popupView.findViewById(R.id.rbtnCECEnable);
        rbtnCECOff = popupView.findViewById(R.id.rbtnCECDisable);

        rbtnHDCPAuto = popupView.findViewById(R.id.rbtnHDCPAuto);
        rbtnHDCP14 = popupView.findViewById(R.id.rbtnHDCP14);

        btnSave = (Button) popupView.findViewById(R.id.btnSave);
        btnCancel = (Button) popupView.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. 備份舊的設定值 (從 GposInfo 讀取目前生效的值)
                GposInfo oldInfo = PrimeUtils.get_gpos_info();
                final int oldResolution = GposInfo.getResolution(getContext());
                final int oldAudioIndex = (GposInfo.getAudioLanguageSelection(getContext(), 0).equals("chi") ? 0 : 1);
                final int oldAspectRatio = GposInfo.getScreen16x9(getContext());
                final int oldCec = GposInfo.getHdmiCecOnOff(getContext());
                final int oldHdcp = GposInfo.getHDCPOnOff(getContext());
                // 2. 儲存並套用新設定
                saveData();
                popupWindow.dismiss(); // 關閉原本的設定選單
                // 3. 顯示確認倒數視窗
                // 比對 確認選取的解析度 與 舊的解析度
                if(oldResolution != confirmedResolutionIndex)
                    showConfirmationPopup(oldResolution, oldAudioIndex, oldAspectRatio, oldCec, oldHdcp);
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }
    private void nextItem(int keyCode, View v) {
        int idex = audioIndex;
        String[] items = audioItems;
        if (v.getId() == R.id.btnAspectRatio){
            idex = aspectRatioIndex;
            items = aspectRatioItems;
        }else if(v.getId() == R.id.btnResolution){
            idex = resolutionIndex;
            items = ResolutionItems;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            idex = idex - 1;
            if (idex == -1) {
                idex = items.length - 1;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            idex = idex + 1;
            if (idex == items.length) {
                idex = 0;
            }
        } else {

        }
        if(v.getId() == R.id.btnAudio) {
            audioIndex = idex;
            updateAudioUI();
        } else if (v.getId() == R.id.btnAspectRatio) {
            aspectRatioIndex = idex;
            updateAspectRatioUI();
        } else if (v.getId() == R.id.btnResolution){
            resolutionIndex = idex;
            updateResolutionUI();
        }

    }

    private void showPopWindow() {
        popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
        init_setting();
        updatePopupUI();
    }

    private void init_setting() {
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        confirmedAudioIndex = (GposInfo.getAudioLanguageSelection(getContext(), 0).equals("chi")?0:1);
        confirmedAspectRatioIndex = GposInfo.getScreen16x9(getContext());
        confirmedResolutionIndex = GposInfo.getResolution(getContext());
        
        // 初始時，瀏覽的 index 與 確認的 index 相同
        audioIndex = confirmedAudioIndex;
        aspectRatioIndex = confirmedAspectRatioIndex;
        resolutionIndex = confirmedResolutionIndex;

        ResolutionItems = PrimeUtils.get_supported_resolution_list(getContext());
        LogUtils.d("resolutionIndex = "+resolutionIndex);
    }

    private void saveData(){
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if(gposInfo != null){
            //Resolution - 使用 confirmedResolutionIndex
            if (GposInfo.getResolution(getContext()) != confirmedResolutionIndex) {
                GposInfo.setResolution(getContext(), resolutionIndex);
                 //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_RESOLUTION, gposInfo.getResolution());
                 PrimeUtils.set_hdmi_output_format(confirmedResolutionIndex);
            }

            //Audio Selection - 使用 confirmedAudioIndex
            String oldAudio = GposInfo.getAudioLanguageSelection(getContext(), 0);
            String newAudio = (confirmedAudioIndex == 0)?"chi":"eng";

            if (!oldAudio.equals(newAudio)) {
                GposInfo.setAudioLanguageSelection(getContext(), 0, (audioIndex == 0)?"chi":"eng");
                //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_AUDIO_LANG_SELECT_1, gposInfo.getAudioLanguageSelection(0));
            }
            //Aspect Ratio - 使用 confirmedAspectRatioIndex
            if (GposInfo.getScreen16x9(getContext()) != confirmedAspectRatioIndex) {
                GposInfo.setScreen16x9(getContext(), aspectRatioIndex);
                //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_SCREEN_16X9, gposInfo.getScreen16x9());
                PrimeUtils.set_aspect_ratio(confirmedAspectRatioIndex);
            }

            //HDMICEC
            boolean hdmicec = false;
            if(rbtnCECOn.isChecked()){
                hdmicec = true;
            }else if(rbtnCECOff.isChecked()){
                hdmicec = false;
            }
            int oldCec = GposInfo.getHdmiCecOnOff(getContext());
            int newCec = hdmicec ? 1 : 0;
            if (oldCec != newCec) {
                GposInfo.setHdmiCecOnOff(getContext(), newCec);
                //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_HDMI_CEC, gposInfo.getHdmiCecOnOff());
                PrimeUtils.set_hdmicec_enable(hdmicec);
            }

            //HDCP
            int hdcp = 0;
            if(rbtnHDCPAuto.isChecked()){
                hdcp = 1;
            }else if(rbtnHDCP14.isChecked()){
                hdcp = 2;
            }
            if (GposInfo.getHDCPOnOff(getContext()) != hdcp) {
                GposInfo.setHDCPOnOff(getContext(), hdcp);
                //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_HDCP_ONOFF, gposInfo.getHDCPOnOff());
                PrimeUtils.set_hdcp_level(hdcp);
            }
            updateMainSummary();
        }

    }

    private void updateAspectRatioUI(){
        int idex = aspectRatioIndex; // 當前瀏覽的 index

        String aspectRatio = aspectRatioItems[aspectRatioIndex];
        tvAspectRatioItem.setText(aspectRatio);
        LogUtils.d("aspectRatio = "+aspectRatio+" confirmedAspectRatioIndex = "+confirmedAspectRatioIndex+" idex = "+idex+"");
        
        // 只有當瀏覽的 index 等於確認的 index 時才顯示打勾
        if(confirmedAspectRatioIndex == idex)
            tvAspectRatioSelect.setVisibility(VISIBLE);
        else
            tvAspectRatioSelect.setVisibility(GONE);
        // Removed main view update
    }

    private void updateAudioUI(){
        String nowAudio = "";
        if(PrimeUtils.get_gpos_info() != null)
            nowAudio = GposInfo.getAudioLanguageSelection(getContext(), 0);

        String audio = audioItems[audioIndex];

        tvAudioItem.setText(audio);

        LogUtils.d("audio = "+audio+" nowAudio = "+nowAudio);
        
        // 使用 confirmedAudioIndex 來判斷是否顯示選取狀態
        if(confirmedAudioIndex == audioIndex) {
             tvAudioSelect.setVisibility(VISIBLE);
        } else {
             tvAudioSelect.setVisibility(GONE);
        }
        
        // Removed main view update
    }

    private void updateCecAndHdcpUI(){
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        int cec = 1, hdcp = 1;

        if(gposInfo != null){
            cec = GposInfo.getHdmiCecOnOff(getContext());
            hdcp = GposInfo.getHDCPOnOff(getContext());

        }
        if(cec == 1) {
            rbtnCECOn.setChecked(true);
            rbtnCECOff.setChecked(false);
        }else{
            rbtnCECOn.setChecked(false);
            rbtnCECOff.setChecked(true);
        }
        // Removed main view update

        if(hdcp == 1){
            rbtnHDCPAuto.setChecked(true);
            rbtnHDCP14.setChecked(false);
        }else{
            rbtnHDCPAuto.setChecked(false);
            rbtnHDCP14.setChecked(true);
        }
        // Removed main view update
    }

    private void updateResolutionUI() {
        int nowResolution = 0;
        if(PrimeUtils.get_gpos_info() != null)
            nowResolution = GposInfo.getResolution(getContext());

        if(ResolutionItems == null)
            ResolutionItems = PrimeUtils.get_supported_resolution_list(getContext());

        String resolution = ResolutionItems[resolutionIndex];
        LogUtils.d("nowResolution = "+nowResolution+" resolutionIndex = "+resolutionIndex);
        tvResolutionItem.setText(resolution);
        
        // 只有當瀏覽的 index 等於確認的 index 時才顯示打勾
        if(confirmedResolutionIndex == resolutionIndex)
            tvResolutionSelect.setVisibility(VISIBLE);
        else
            tvResolutionSelect.setVisibility(GONE);

        // Removed main view update
    }

    private PopupWindow confirmPopupWindow;
    private CountDownTimer confirmTimer;
    private TextView tvConfirmMessage;

    private void showConfirmationPopup(final int oldRes, final int oldAudio, final int oldAspect, final int oldCec, final int oldHdcp) {
        View confirmView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_settings, null);

        // 設定 PopupWindow
        confirmPopupWindow = new PopupWindow(confirmView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, true);
        confirmPopupWindow.setFocusable(true);
        confirmPopupWindow.setOutsideTouchable(false); // 禁止點擊外部關閉

        tvConfirmMessage = confirmView.findViewById(R.id.tvConfirmMessage_2);
        Button btnKeep = confirmView.findViewById(R.id.btnConfirmKeep);
        Button btnRevert = confirmView.findViewById(R.id.btnConfirmRevert);

        // "Keep" 按鈕邏輯
        btnKeep.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmTimer != null) confirmTimer.cancel();
                confirmPopupWindow.dismiss();
                // 設定已經在 saveData() 儲存了，這裡不需要做額外動作
            }
        });

        // "Revert" 按鈕邏輯
        btnRevert.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmTimer != null) confirmTimer.cancel();
                confirmPopupWindow.dismiss();
                revertSettings(oldRes, oldAudio, oldAspect, oldCec, oldHdcp);
            }
        });

        // 處理按鍵事件 (防止使用者按 Back 鍵直接關閉而不還原)
        confirmView.setFocusableInTouchMode(true);
        confirmView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // 按 Back 鍵視同放棄修改 -> 還原
                    if (confirmTimer != null) confirmTimer.cancel();
                    confirmPopupWindow.dismiss();
                    revertSettings(oldRes, oldAudio, oldAspect, oldCec, oldHdcp);
                    return true;
                }
                return false;
            }
        });

        // 顯示視窗 (顯示在畫面正中央)
        confirmPopupWindow.showAtLocation(settingsRecyclerView, Gravity.CENTER, 0, 0);
        btnKeep.requestFocus(); // 預設焦點在 Keep

        // 啟動 15 秒倒數計時
        startConfirmTimer(oldRes, oldAudio, oldAspect, oldCec, oldHdcp);
    }

    private void startConfirmTimer(final int oldRes, final int oldAudio, final int oldAspect, final int oldCec, final int oldHdcp) {
        confirmTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (confirmPopupWindow != null && confirmPopupWindow.isShowing()) {
                    String msg = String.format(getContext().getString(R.string.resolution_comfirm_message_2), (millisUntilFinished / 1000));
                    tvConfirmMessage.setText(msg);
                }
            }

            @Override
            public void onFinish() {
                if (confirmPopupWindow != null && confirmPopupWindow.isShowing()) {
                    confirmPopupWindow.dismiss();
                    // 時間到，自動還原
                    revertSettings(oldRes, oldAudio, oldAspect, oldCec, oldHdcp);
                }
            }
        }.start();
    }

    private void revertSettings(int oldRes, int oldAudio, int oldAspect, int oldCec, int oldHdcp) {
        // 1. 還原類別內的索引變數，以便下次打開選單時顯示正確
        resolutionIndex = oldRes;
//        audioIndex = oldAudio;
//        aspectRatioIndex = oldAspect;

        // 2. 直接操作底層 API 還原設定
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if(gposInfo != null){
            // 還原 Resolution
            GposInfo.setResolution(getContext(), oldRes);
            //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_RESOLUTION, oldRes);
            PrimeUtils.set_hdmi_output_format(oldRes);

//            // 還原 Audio
//            gposInfo.setAudioLanguageSelection(0, (oldAudio == 0)?"chi":"eng");
//            PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_AUDIO_LANG_SELECT_1, gposInfo.getAudioLanguageSelection(0));
//
//            // 還原 Aspect Ratio
//            gposInfo.setScreen16x9(oldAspect);
//            PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_SCREEN_16X9, oldAspect);
//            PrimeUtils.set_aspect_ratio(oldAspect);
//
//            // 還原 CEC
//            gposInfo.setHdmiCecOnOff(oldCec);
//            PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_HDMI_CEC, oldCec);
//            PrimeUtils.set_hdmicec_enable(oldCec == 1);
//
//            // 還原 HDCP
//            gposInfo.setHDCPOnOff(oldHdcp);
//            PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_HDCP_ONOFF, oldHdcp);
//            PrimeUtils.set_hdcp_level(oldHdcp);

            // 更新主畫面上的文字摘要
            updateMainSummary();
        }
    }
}
