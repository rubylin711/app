package com.prime.homeplus.settings.persional;

import android.app.LocaleManager;
import android.content.Context;
import android.os.LocaleList;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;

import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

import java.util.Locale;

public class LanguageView extends ThirdLevelView {
    private String TAG = "HomePlus-LanguageView";

    public LanguageView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_language;
    }

    private RadioButton rbtn1, rbtn2;

    public void onFocus(){
        rbtn1.requestFocus();
        getLanguage();
    }

    @Override
    public void onViewCreated() {
        rbtn1 = (RadioButton) findViewById(R.id.rbtn1);
        rbtn2 = (RadioButton) findViewById(R.id.rbtn2);

        rbtn1.setText(getContext().getString(R.string.settings_lang_setting_chinese));

        rbtn1.setOnKeyListener(new View.OnKeyListener() {
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

        rbtn1.setOnFocusChangeListener(OnFocusChangeListener);

        rbtn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rbtn1.setChecked(true);
                rbtn2.setChecked(false);

                PrimeUtils.set_system_language(getContext(), "zh");
            }
        });

        rbtn2.setText(getContext().getString(R.string.settings_lang_setting_english));

        rbtn2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        rbtn2.setOnFocusChangeListener(OnFocusChangeListener);

        rbtn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rbtn1.setChecked(false);
                rbtn2.setChecked(true);

                PrimeUtils.set_system_language(getContext(), "en");
            }
        });
    }

    private int languageIndex = 0;

    private void getLanguage(){

        String currentSystemLang = Locale.getDefault().getLanguage();
        LogUtils.d("currentSystemLang ="+currentSystemLang);
        if(currentSystemLang.equals("en"))
            languageIndex = 1;
        else
            languageIndex = 0;



        switch (languageIndex){
            case 0:
                rbtn1.setChecked(true);
                rbtn1.requestFocus();
                rbtn2.setChecked(false);
                break;
            case 1:
                rbtn1.setChecked(false);
                rbtn2.setChecked(true);
                rbtn2.requestFocus();
                break;
        }
    }

}
