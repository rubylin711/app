package com.prime.homeplus.settings.persional;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsItemData;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class SubtitleView extends ThirdLevelView {
    private String TAG = "HomePlus-SubtitleView";

    private SettingsRecyclerView secondDepthView;

    public SubtitleView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);

        this.secondDepthView = secondDepthView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_subtitle;
    }

    private Button btnSubtitle;

    public void onFocus() {
        btnSubtitle.requestFocus();
        getValue();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        btnSubtitle = (Button) findViewById(R.id.btnSubtitle);

        btnSubtitle.setOnKeyListener(new View.OnKeyListener() {
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


        btnSubtitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
            }
        });
    }


    private View popupView;
    private static PopupWindow popupWindow;

    private String[] subtitleItems;

    private Button btnPopupSubtitle, btnSave, btnCancel;

    private TextView tvSelectSubtitle, tvSubtitle;

    private ImageView ivArrowLeft, ivArrowRight;

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_subtitle, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        subtitleItems = getResources().getStringArray(R.array.subtitle_language_list);

        tvSelectSubtitle = (TextView) popupView.findViewById(R.id.tvSelectSubtitle);
        tvSubtitle = (TextView) popupView.findViewById(R.id.tvSubtitle);

        ivArrowLeft = (ImageView) popupView.findViewById(R.id.ivArrowLeft);
        ivArrowRight = (ImageView) popupView.findViewById(R.id.ivArrowRight);

        btnPopupSubtitle = (Button) popupView.findViewById(R.id.btnSubtitle);
        btnSave = (Button) popupView.findViewById(R.id.btnSave);
        btnCancel = (Button) popupView.findViewById(R.id.btnCancel);

        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        btnPopupSubtitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tvSelectSubtitle.setTypeface(null, Typeface.BOLD);
                    tvSelectSubtitle.setTextSize(17);
                    tvSelectSubtitle.setTextColor(getResources().getColor(R.color.white));
                    tvSubtitle.setTypeface(null, Typeface.BOLD);
                    tvSubtitle.setTextSize(17);
                    tvSubtitle.setTextColor(getResources().getColor(R.color.white));
                    ivArrowLeft.setImageResource(R.drawable.arrow_setting_spin_l_f);
                    ivArrowRight.setImageResource(R.drawable.arrow_setting_spin_r_f);
                } else {
                    tvSelectSubtitle.setTypeface(null, Typeface.NORMAL);
                    tvSelectSubtitle.setTextSize(16);
                    tvSelectSubtitle.setTextColor(getResources().getColor(R.color.colorWhiteOpacity70));
                    tvSubtitle.setTypeface(null, Typeface.NORMAL);
                    tvSubtitle.setTextSize(16);
                    tvSubtitle.setTextColor(getResources().getColor(R.color.colorWhiteOpacity70));
                    ivArrowLeft.setImageResource(R.drawable.arrow_setting_spin_l_d);
                    ivArrowRight.setImageResource(R.drawable.arrow_setting_spin_r_d);
                }
            }
        });

        btnPopupSubtitle.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                    } else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        confirmedSubtitleIndex = subtitleIndex;
                        updatePopupUI();
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        nextSubTitle(keyCode);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        nextSubTitle(keyCode);
                        return true;
                    }
                }
                return false;
            }
        });

        btnPopupSubtitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSubtitle();
                getValue();
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSubtitle();
                getValue();

                popupWindow.dismiss();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private int subtitleIndex = 0;
    private int confirmedSubtitleIndex = 0;

    private void nextSubTitle(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            subtitleIndex = subtitleIndex - 1;
            if (subtitleIndex == -1) {
                subtitleIndex = subtitleItems.length - 1;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            subtitleIndex = subtitleIndex + 1;
            if (subtitleIndex == subtitleItems.length) {
                subtitleIndex = 0;
            }
        } else {

        }
        updatePopupUI();
    }

    private void updatePopupUI() {

        String nowSubtitle = "";
        if(PrimeUtils.get_gpos_info() != null)
            nowSubtitle = GposInfo.getSubtitleLanguageSelection(getContext(), 0);

        String subtitle = subtitleItems[subtitleIndex];

        tvSubtitle.setText(subtitle);

        // Checkmark logic using confirmed index
        if (confirmedSubtitleIndex == subtitleIndex) {
            tvSelectSubtitle.setVisibility(VISIBLE);
        } else {
            tvSelectSubtitle.setVisibility(GONE);
        }
    }

    private void updateMainSummary() {
         // Update the summary text in the list (Main View)
         // This assumes secondDepthView.notifyDataSetChanged() updates the text based on SettingsItemData
         // effectively, we need to update SettingsItemData first?
         // Actually, saveSubtitle updates SettingsItemData.
         // But we need to ensure we display the CONFIRMED (System) value here if we re-enter.
         // The original code used getValue() to read system value and set text.
         
         // Let's rely on saveSubtitle updating the cached data for the list.
         // But for the initial state, onFocus calls getValue.
    }

    private void getValue() {
        String nowSubtitle = "";

        if(PrimeUtils.get_gpos_info() != null)
            nowSubtitle = GposInfo.getSubtitleLanguageSelection(getContext(), 0);

        if (nowSubtitle.equals("")) {
            confirmedSubtitleIndex = 0;
        } else if (nowSubtitle.equals("chi")) {
            confirmedSubtitleIndex = 0;
        } else {
            confirmedSubtitleIndex = 1;
        }
        // Initialize browsing index to confirmed index
        subtitleIndex = confirmedSubtitleIndex;
        
        updatePopupUI();
    }

    private void saveSubtitle() {
        // Optimization: Check if changed
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if(gposInfo != null){
            String oldSubtitle = GposInfo.getSubtitleLanguageSelection(getContext(), 0);
            String newSubtitle = (confirmedSubtitleIndex == 0)?"chi":"eng";
            
            if (!oldSubtitle.equals(newSubtitle)) {
                GposInfo.setSubtitleLanguageSelection(getContext(), 0, newSubtitle);
                //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_SUBTITLE_LANG_SELECT_1, gposInfo.getSubtitleLanguageSelection(0));
            }
        }
        
        // Update Main View Item Data
        String subtitle = subtitleItems[confirmedSubtitleIndex];
        SettingsItemData settingsItems = secondDepthView.settingsItems.get(secondDepthView.getViewIndex());
        settingsItems.setValue(subtitle);
        secondDepthView.notifyDataSetChanged();
    }
}
