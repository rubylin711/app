package com.prime.dmg.launcher.Settings;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Settings.fragment.DMGSettingsFragment;
import com.prime.dmg.launcher.Settings.fragment.SelectQamFragment;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Scan.Scan_utils;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.MusicInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVMessage;

import java.util.List;

public class ChannelPreScanningActivity extends BaseActivity {
    private final static String TAG = "ChannelManualScanningActivity";
    public final static String DEFAULT_CHANNEL_FREQUENCY = "303";
    public final static String DEFAULT_CHANNEL_SYMBOL_RATE = "5200";
    public final static String DEFAULT_CHANNEL_QAM_TEXT = "256";
    public final static String DEFAULT_DMG_CHANNEL_FREQUENCY = "615";
    public final static String DEFAULT_DMG_CHANNEL_SYMBOL_RATE = "5057";
    public final static String DEFAULT_DMG_CHANNEL_QAM_TEXT = "64";
    private TextView g_textv_frequency, g_textv_symbolrate, g_textv_qam;
    private TextView g_textv_channel_signal_scan, g_textv_channel_scan, g_textv_channel_freq_scan, g_textv_channel_scan_reset;
    private TextView g_textv_channel_next_step;
    private View g_view_frequency_line, g_view_symbolrate_line, g_view_qam_line;
    private RelativeLayout g_blue_hint_key1, g_blue_hint_key2, g_ok_hint_key;
    private SelectQamFragment g_fragment_select_qam;
    private boolean g_is_setup_wizard = false ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        if ( intent != null )
            g_is_setup_wizard = intent.getBooleanExtra("SetupWizard", false) ;

        if ( g_is_setup_wizard )
            setContentView(R.layout.activity_channel_pre_scanning_setupwizard);
        else
            setContentView(R.layout.activity_channel_pre_scanning);
        init();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode >= KeyEvent.KEYCODE_0  && keyCode <= KeyEvent.KEYCODE_9) {
            handle_number_key(Utils.number_code_to_string(keyCode));
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            clear_text();
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                remove_character();
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
        if (msg.getMsgType() == TVMessage.TYPE_ADD_MUSIC_CATEGORY_TO_FAV) {
            Log.d(TAG, "onMessage: TYPE_ADD_MUSIC_CATEGORY_TO_FAV");
            add_music_category_to_fav();
        }
    }

    @SuppressLint("SetTextI18n")
    private void init() {

        g_textv_frequency = findViewById(R.id.lo_channel_pre_scanning_frequency);
        g_textv_symbolrate = findViewById(R.id.lo_channel_pre_scanning_symbolrate);
        g_textv_qam = findViewById(R.id.lo_channel_pre_scanning_qam);

        TpInfo tpInfo = get_tpInfo(this);
        LogUtils.d("tpInfo = "+tpInfo);
        if (tpInfo != null) {
            g_textv_frequency.setText(Integer.toString(tpInfo.CableTp.getFreq()/1000));
            g_textv_symbolrate.setText(Integer.toString(tpInfo.CableTp.getSymbol()));
            g_textv_qam.setText(Integer.toString(tpInfo.CableTp.getQamRealValue()));
        }
        else {
            if(ACSHelper.get_ProjectId(this) == ACSHelper.PROJECT_TBC) {

                int value = ACSHelper.get_BATId(this);
                if(HomeApplication.get_prime_dtv() != null) {
                    HomeApplication.get_prime_dtv().gpos_info_update_by_key_string(GposInfo.GPOS_BAT_ID, value);
                }
                Pvcfg.setBatId(value);
                Pvcfg.setModuleType(Pvcfg.MODULE_TBC);
                LogUtils.d("TBC");
                g_textv_frequency.setText(DEFAULT_CHANNEL_FREQUENCY);
                g_textv_symbolrate.setText(DEFAULT_CHANNEL_SYMBOL_RATE);
                g_textv_qam.setText(DEFAULT_CHANNEL_QAM_TEXT);
            }
            else {
                g_textv_frequency.setText(DEFAULT_DMG_CHANNEL_FREQUENCY);
                g_textv_symbolrate.setText(DEFAULT_DMG_CHANNEL_SYMBOL_RATE);
                g_textv_qam.setText(DEFAULT_DMG_CHANNEL_QAM_TEXT);
                LogUtils.d("DMG ");
            }
        }

        g_view_frequency_line = findViewById(R.id.lo_channel_pre_scanning_frequency_line);
        g_view_symbolrate_line = findViewById(R.id.lo_channel_pre_scanning_symbolrate_line);
        g_view_qam_line = findViewById(R.id.lo_channel_pre_scanning_qam_line);

        g_blue_hint_key1 = findViewById(R.id.lo_channel_pre_scanning_delete_frame1);
        g_blue_hint_key2 = findViewById(R.id.lo_channel_pre_scanning_delete_frame2);
        g_ok_hint_key = findViewById(R.id.lo_channel_pre_scanning_qam_select_hint);

        g_textv_channel_signal_scan = findViewById(R.id.lo_channel_pre_scanning_channel_signal_scan);
        g_textv_channel_scan = findViewById(R.id.lo_channel_pre_scanning_channel_scan);
        g_textv_channel_freq_scan = findViewById(R.id.lo_channel_pre_scanning_channel_freq_scan);
        g_textv_channel_scan_reset = findViewById(R.id.lo_channel_pre_scanning_channel_scan_reset);
        g_textv_channel_next_step = findViewById(R.id.lo_channel_pre_scanning_channel_next_step);

        set_listener();
    }

    @SuppressLint("SetTextI18n")
    private void handle_number_key(String valueOfNum) {
        if (g_textv_frequency.isFocused()) {
            g_textv_frequency.setText(g_textv_frequency.getText() + valueOfNum);
        } else if (g_textv_symbolrate.isFocused()) {
            g_textv_symbolrate.setText(g_textv_symbolrate.getText() + valueOfNum);
        } else if (g_textv_qam.isFocused()) {
            g_textv_qam.callOnClick();
        }
    }

    private void clear_text() {
        if (g_textv_frequency.isFocused()) {
            g_textv_frequency.setText("");
        } else if (g_textv_symbolrate.isFocused()) {
            g_textv_symbolrate.setText("");
        }
    }

    private void remove_character() {
        if (g_textv_frequency.isFocused() && g_textv_frequency.getText().length() > 0) {
            g_textv_frequency.setText(g_textv_frequency.getText().subSequence(0, g_textv_frequency.getText().length()-1));
        } else if (g_textv_symbolrate.isFocused() && g_textv_symbolrate.getText().length() > 0) {
            g_textv_symbolrate.setText(g_textv_symbolrate.getText().subSequence(0, g_textv_symbolrate.getText().length()-1));
        }
    }

    @SuppressLint("SetTextI18n")
    private void set_listener() {
        g_textv_frequency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                g_view_frequency_line.setBackgroundColor(getColor(R.color.pvr_red_color));
                g_blue_hint_key1.setVisibility(View.VISIBLE);
                g_blue_hint_key2.setVisibility(View.GONE);
                g_ok_hint_key.setVisibility(View.GONE);
            }
            else g_view_frequency_line.setBackgroundColor(getColor(R.color.white));
        });

        g_textv_symbolrate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                g_view_symbolrate_line.setBackgroundColor(getColor(R.color.pvr_red_color));
                g_blue_hint_key1.setVisibility(View.GONE);
                g_blue_hint_key2.setVisibility(View.VISIBLE);
                g_ok_hint_key.setVisibility(View.GONE);
            }
            else g_view_symbolrate_line.setBackgroundColor(getColor(R.color.white));
        });

        g_textv_qam.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                g_view_qam_line.setBackgroundColor(getColor(R.color.pvr_red_color));
                g_blue_hint_key1.setVisibility(View.GONE);
                g_blue_hint_key2.setVisibility(View.GONE);
                g_ok_hint_key.setVisibility(View.VISIBLE);
            }
            else g_view_qam_line.setBackgroundColor(getColor(R.color.white));
        });

        g_textv_qam.setOnClickListener((v) -> {
            g_fragment_select_qam = new SelectQamFragment(this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: qam = " + ((TextView) v).getText());
                    g_textv_qam.setText(((TextView) v).getText());
                    g_fragment_select_qam.dismiss();
                }
            });
            g_fragment_select_qam.show();
        });

        g_textv_channel_signal_scan.setOnClickListener((v) -> {
            Log.i(TAG, "channel_signal_scan on click");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(DMGSettingsFragment.PACKAGE_LAUNCHER, DMGSettingsFragment.ACTIVITY_CHANNEL_SCAN));
            Bundle bundle = new Bundle();
            bundle.putBoolean(DMGSettingsFragment.SCAN_FROM_ACS, false);
            bundle.putBoolean(DMGSettingsFragment.AUTO_DETECT, true);
            bundle.putBoolean(DMGSettingsFragment.SCANNING_PAGE, true);
            bundle.putBoolean(DMGSettingsFragment.SINGLE_FREQ, false);
            bundle.putInt(DMGSettingsFragment.SCANNING_FREQ, get_frequency());
            bundle.putInt(DMGSettingsFragment.SCANNING_SR, get_symbolrate());
            bundle.putInt(DMGSettingsFragment.SCANNING_BW, 6);
            bundle.putInt(DMGSettingsFragment.SCANNING_MODULATION, get_qam());
            bundle.putString(DMGSettingsFragment.SCANNING_MODULATION_TEXT, g_textv_qam.getText().toString());
            bundle.putInt(DMGSettingsFragment.SCANNING_TUNERID, 0);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        g_textv_channel_scan.setOnClickListener((v) -> {
            Log.i(TAG, "channel_scan on click");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(DMGSettingsFragment.PACKAGE_LAUNCHER, DMGSettingsFragment.ACTIVITY_CHANNEL_SCAN));
            Bundle bundle = new Bundle();
            bundle.putBoolean(DMGSettingsFragment.SCAN_FROM_ACS, false);
            bundle.putBoolean(DMGSettingsFragment.AUTO_DETECT, true);
            bundle.putBoolean(DMGSettingsFragment.SCANNING_PAGE, false);
            bundle.putBoolean(DMGSettingsFragment.SINGLE_FREQ, false);
            bundle.putInt(DMGSettingsFragment.SCANNING_FREQ, get_frequency());
            bundle.putInt(DMGSettingsFragment.SCANNING_SR, get_symbolrate());
            bundle.putInt(DMGSettingsFragment.SCANNING_BW, 6);
            bundle.putInt(DMGSettingsFragment.SCANNING_MODULATION, get_qam());
            bundle.putString(DMGSettingsFragment.SCANNING_MODULATION_TEXT, g_textv_qam.getText().toString());
            bundle.putInt(DMGSettingsFragment.SCANNING_TUNERID, 0);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        if ( g_textv_channel_freq_scan != null )
        {
            g_textv_channel_freq_scan.setOnClickListener((v) -> {
                Log.i(TAG, "channel_freq_scan on click");
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(DMGSettingsFragment.PACKAGE_LAUNCHER, DMGSettingsFragment.ACTIVITY_CHANNEL_SCAN));
                Bundle bundle = new Bundle();
                bundle.putBoolean(DMGSettingsFragment.SCAN_FROM_ACS, false);
                bundle.putBoolean(DMGSettingsFragment.AUTO_DETECT, true);
                bundle.putBoolean(DMGSettingsFragment.SCANNING_PAGE, false);
                bundle.putBoolean(DMGSettingsFragment.SINGLE_FREQ, true);
                bundle.putInt(DMGSettingsFragment.SCANNING_FREQ, get_frequency());
                bundle.putInt(DMGSettingsFragment.SCANNING_SR, get_symbolrate());
                bundle.putInt(DMGSettingsFragment.SCANNING_BW, 6);
                bundle.putInt(DMGSettingsFragment.SCANNING_MODULATION, get_qam());
                bundle.putString(DMGSettingsFragment.SCANNING_MODULATION_TEXT, g_textv_qam.getText().toString());
                bundle.putInt(DMGSettingsFragment.SCANNING_TUNERID, 0);
                intent.putExtras(bundle);
                startActivity(intent);
            });
        }

        g_textv_channel_scan_reset.setOnClickListener((v) -> {
            Log.i(TAG, "channel_scan_reset on click");
            if(Pvcfg.getModuleType() == Pvcfg.MODULE_DMG) {
                g_textv_frequency.setText(DEFAULT_DMG_CHANNEL_FREQUENCY);
                g_textv_symbolrate.setText(DEFAULT_DMG_CHANNEL_SYMBOL_RATE);
                g_textv_qam.setText(DEFAULT_DMG_CHANNEL_QAM_TEXT);
            }
            else {
                g_textv_frequency.setText(DEFAULT_CHANNEL_FREQUENCY);
                g_textv_symbolrate.setText(DEFAULT_CHANNEL_SYMBOL_RATE);
                g_textv_qam.setText(DEFAULT_CHANNEL_QAM_TEXT);
            }
        });

        if ( g_textv_channel_next_step != null )
        {
            g_textv_channel_next_step.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "channel_next_step on click");
                    //finish();
                    finishAffinity();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            });
        }
    }

    private int get_frequency() {
        if (TextUtils.isEmpty(g_textv_frequency.getText())) {
            return -1;
        }
        return Integer.valueOf(g_textv_frequency.getText().toString());
    }

    private int get_symbolrate() {
        if (TextUtils.isEmpty(g_textv_symbolrate.getText())) {
            return -1;
        }
        return Integer.valueOf(g_textv_symbolrate.getText().toString());
    }

    private int get_qam() {
        if (TextUtils.isEmpty(g_textv_qam.getText())) {
            return -1;
        }
        int qam_in_value = Integer.valueOf(g_textv_qam.getText().toString());
        if (qam_in_value == 256)
            return TpInfo.Cable.QAM_256;
        else if (qam_in_value == 128)
            return TpInfo.Cable.QAM_128;
        else if (qam_in_value == 64)
            return TpInfo.Cable.QAM_64;
        else if (qam_in_value == 32)
            return TpInfo.Cable.QAM_32;
        else if (qam_in_value == 16)
            return TpInfo.Cable.QAM_16;
        else
            return TpInfo.Cable.QAM_AUTO;
    }

    @Override
    public void onBackPressed() {
        if ( g_is_setup_wizard ) // setup wizard channel search should not back
            return ;

        super.onBackPressed();
    }

    public static TpInfo get_tpInfo(Context context) {
        ChannelChangeManager channelChangeManager = ChannelChangeManager.get_instance(context);
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        ProgramInfo programInfo = channelChangeManager.get_cur_channel();
        if (programInfo!=null)
            return primeDtv.tp_info_get(programInfo.getTpId());
        else
            return null;
    }

    private void add_music_category_to_fav() {
        /*return;*/
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        List<ProgramInfo> programInfoList = primeDtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        List<MusicInfo> musicInfoList = MusicInfo.get_current_category(this);
        Log.d(TAG, "add_music_category_to_fav: musicInfoList size = " + musicInfoList.size());
        Log.d(TAG, "add_music_category_to_fav: programInfoList size = " + musicInfoList.size());
        if (musicInfoList.isEmpty())
            return;

        Scan_utils mScanUtils = new Scan_utils(this);
        mScanUtils.category_update_to_fav(programInfoList, musicInfoList);
    }
}
