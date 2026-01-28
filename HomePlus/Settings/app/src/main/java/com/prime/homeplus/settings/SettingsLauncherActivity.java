package com.prime.homeplus.settings;

import android.app.Activity;
import android.os.Bundle;

import com.prime.homeplus.settings.persional.MiniGuideDisplayTimeView;
import com.prime.homeplus.settings.persional.ParentalControlView;
import com.prime.homeplus.settings.persional.PurchasePinView;
import com.prime.homeplus.settings.persional.SubtitleView;
import com.prime.homeplus.settings.persional.VideoAndAudioView;

import java.util.ArrayList;

public class SettingsLauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_launcher);

        initLayout();
    }

    private SettingsRecyclerView recyclerView;

    private void initLayout(){
        recyclerView = (SettingsRecyclerView) findViewById(R.id.settingsRecyclerView);

        String personalString[] = getResources().getStringArray(R.array.settings_personal);
        String personalSubtitleString[] = getResources().getStringArray(R.array.settings_personal_subtitle);

        String titleString[] = new String[5];
        String subtitleString[] = new String[5];
        titleString[0] = personalString[1];
        subtitleString[0]  = personalSubtitleString[1];
        titleString[1] = personalString[2];
        subtitleString[1]  = personalSubtitleString[2];
        titleString[2] = personalString[4];
        subtitleString[2]  = personalSubtitleString[4];
        titleString[3] = personalString[5];
        subtitleString[3]  = personalSubtitleString[5];
        titleString[4] = personalString[6];
        subtitleString[4]  = personalSubtitleString[6];

        ArrayList arrayList = new ArrayList(5);

        arrayList.add(new SettingsItemData(titleString[0], subtitleString[0],
                "", new ParentalControlView(0, this, recyclerView)));
        arrayList.add(new SettingsItemData(titleString[1], subtitleString[1],
                "", new PurchasePinView(1, this, recyclerView)));
        arrayList.add(new SettingsItemData(titleString[2], subtitleString[2],
                "", new VideoAndAudioView(2, this, recyclerView)));

        String nowSubtitle = "";
        String subtitle;
        if (nowSubtitle.equals("")) {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_chinese);
        } else if (nowSubtitle.equals("chi")) {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_chinese);
        } else {
            subtitle = getBaseContext().getString(R.string.settings_av_setting_lang_english);
        }
        arrayList.add(new SettingsItemData(titleString[3], subtitleString[3],
                subtitle, new SubtitleView(3, this, recyclerView)));

        String nowDisplayTime = "5";
        arrayList.add(new SettingsItemData(titleString[4], subtitleString[4],
                nowDisplayTime + getResources().getString(R.string.unit_of_display_time),
                new MiniGuideDisplayTimeView(4, this, recyclerView)));

        recyclerView.addAllItem(arrayList);

        recyclerView.setFocusItemListener(new FocusItemListener() {
            @Override
            public void onFocus(int index) {

            }
        });

        recyclerView.toFirstView();
    }
}
