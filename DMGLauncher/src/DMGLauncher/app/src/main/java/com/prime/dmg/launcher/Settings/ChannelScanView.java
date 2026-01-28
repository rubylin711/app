package com.prime.dmg.launcher.Settings;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.prime.dmg.launcher.R;

import java.util.ArrayList;

public class ChannelScanView extends RelativeLayout {
    private final static String TAG = "ChannelScanView";
    private Button g_button;
    private int g_fake_progress;
    private go_back_listener g_go_back_listener;
    private LinearLayout g_scan_bar;
    private int g_scan_count_down_time;
    private TextView g_scan_num;
    private ProgressBar g_scan_progress;
    private TextView g_scanning_hint;
    private TextView g_scanning_hint_dot;
    private TextView g_scanning_sub_hint;
    private TextView g_signal_parameter;
    private int g_total_freq = 303000;
    private String g_dot = "";
    private Runnable g_dot_runnable = new Runnable() {
        @Override
        public void run() {
            ChannelScanView.this.g_dot += ".";
            if (ChannelScanView.this.g_dot.length() >= 4) {
                ChannelScanView.this.g_dot = ".";
            }
            ChannelScanView.this.g_scanning_hint_dot.setText(ChannelScanView.this.g_dot);
            ChannelScanView.this.postDelayed(this, 800L);
        }
    };
    private Runnable g_update_progress = new Runnable() {
        @Override
        public void run() {
            ChannelScanView.this.g_fake_progress += 10;
            if (ChannelScanView.this.g_fake_progress >= 90) {
                ChannelScanView.this.g_fake_progress = 90;
            }
            ChannelScanView.this.g_scan_num.setText(ChannelScanView.this.g_fake_progress + "%");
            ChannelScanView.this.g_scan_progress.setProgress(ChannelScanView.this.g_fake_progress);
            ChannelScanView.this.postDelayed(this, 1000L);
        }
    };
    private Runnable g_update_button_hint = new Runnable() {
        @Override
        public void run() {
            ChannelScanView.this.g_button.setText(
                    ((Object) ChannelScanView.this.getContext().getResources().getText(R.string.settings_channel_scanning_back)) +
                    "(" + ChannelScanView.this.g_scan_count_down_time + ")");
            if (ChannelScanView.this.g_scan_count_down_time <= 0) {
                if (ChannelScanView.this.g_go_back_listener != null) {
                    ChannelScanView.this.g_go_back_listener.go_back();
                }
            } else {
                ChannelScanView.this.postDelayed(this, 1000L);
            }
            //ChannelScanView.access$1510(ChannelScanView.this);//access$xxx is count down timer's function
            int curScanCountDownTime = ChannelScanView.this.g_scan_count_down_time;
            ChannelScanView.this.g_scan_count_down_time = curScanCountDownTime - 1;
        }
    };

    public interface go_back_listener {
        void go_back();
    }

    public ChannelScanView(Context context) {
        super(context);
        init();
    }

    public ChannelScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChannelScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this.g_dot_runnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.g_dot_runnable);
    }

    private void init() {
        inflate(getContext(), R.layout.view_channel_scan, this);
        this.g_scanning_hint     = (TextView) findViewById(R.id.lo_cs_textv_scanning_hint);
        this.g_scanning_sub_hint = (TextView) findViewById(R.id.lo_cs_textv_scanning_sub_hint);
        this.g_scanning_hint_dot = (TextView) findViewById(R.id.lo_cs_textv_scanning_hint_dot);
        this.g_scan_progress     = (ProgressBar) findViewById(R.id.lo_cs_pgsb_scan_progress);
        this.g_scan_num          = (TextView) findViewById(R.id.lo_cs_textv_scan_num);
        this.g_signal_parameter  = (TextView) findViewById(R.id.lo_cs_textv_signal_parameter);
        this.g_scan_bar          = (LinearLayout) findViewById(R.id.lo_cs_lnrl_scan_frame);
        Button button            = (Button) findViewById(R.id.lo_cs_btn_go_back);
        this.g_button = button;
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelScanView channelScanView = ChannelScanView.this;
                channelScanView.removeCallbacks(channelScanView.g_update_button_hint);
                if (ChannelScanView.this.g_go_back_listener != null) {
                    ChannelScanView.this.g_go_back_listener.go_back();
                }
            }
        });
        update_scan_progress(0);
        //updateProgressWhenReady(); //channel scan show display with fake data
    }

    public void update_scan_params(int freq, int symbol, String qamText) {
        this.g_signal_parameter.setText(((Object) getContext().getText(R.string.settings_channel_frequency)) +
                ":" + freq + "\n" + ((Object) getContext().getText(R.string.settings_channel_symbol_rate)) +
                ":" + symbol + "\n" + ((Object) getContext().getText(R.string.settings_channel_qam)) +
                ":" + qamText + "\n");
    }

    public void update_channel_count(final int size) {
        post(new Runnable() {
            @Override
            public void run() {
                String valueOf = String.valueOf(size);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(valueOf);
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ChannelScanView.this.getContext().getColor(R.color.pvr_red_color)), 0, valueOf.length(), 33);
                SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(ChannelScanView.this.getResources().getText(R.string.settings_channel_scanning_sub_hint_video));
                spannableStringBuilder.append((CharSequence) " ");
                spannableStringBuilder.append((CharSequence) spannableStringBuilder2);
                ChannelScanView.this.g_scanning_sub_hint.setText(spannableStringBuilder);
            }
        });
    }

    public void set_total_freq(int size) {
        this.g_total_freq = size;
    }

    public void remove_update_progress_callback() {
        removeCallbacks(this.g_update_progress);
    }

    public void update_progress_when_ready() {
        remove_update_progress_callback();
        this.g_fake_progress = 0;
        postDelayed(this.g_update_progress, 1000L);
    }

    public void update_scan_progress(final int num) {
        post(new Runnable() {
            @Override
            public void run() {
                int ceil = (int) Math.ceil((num / ChannelScanView.this.g_total_freq) * 100.0d);
                if (ceil >= 100) {
                    ceil = 100;
                }
                ChannelScanView.this.g_scan_num.setText(ceil + "%");
                ChannelScanView.this.g_scan_progress.setProgress(ceil);
            }
        });
    }

    public void update_scan_progress_percent(final int percent) {
        post(new Runnable() {
            @Override
            public void run() {
                ChannelScanView.this.g_scan_num.setText(percent + "%");
                ChannelScanView.this.g_scan_progress.setProgress(percent);
            }
        });
    }

    public void update_scan_completed(final int size) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                ChannelScanView.this.g_scan_bar.setVisibility(GONE);
                ChannelScanView.this.g_scanning_hint_dot.setVisibility(GONE);
                ChannelScanView channelScanView = ChannelScanView.this;
                channelScanView.removeCallbacks(channelScanView.g_dot_runnable);
                ChannelScanView.this.g_scanning_hint.setText(R.string.settings_channel_scanning_completed);
                int channels = ChannelScanView.this.get_channels();
                int radios = ChannelScanView.this.get_radios();
                String valueOf = String.valueOf(channels);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(valueOf);
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ChannelScanView.this.getContext().getColor(R.color.pvr_red_color)), 0, valueOf.length(), 33);
                SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(ChannelScanView.this.getResources().getText(R.string.settings_channel_scanning_sub_hint_video));
                spannableStringBuilder.append((CharSequence) " ");
                spannableStringBuilder.append((CharSequence) spannableStringBuilder2);
                spannableStringBuilder.append((CharSequence) ", ");
                String valueOf2 = String.valueOf(radios);
                SpannableStringBuilder spannableStringBuilder3 = new SpannableStringBuilder(valueOf2);
                spannableStringBuilder3.setSpan(new ForegroundColorSpan(ChannelScanView.this.getContext().getColor(R.color.pvr_red_color)), 0, valueOf2.length(), 33);
                SpannableStringBuilder spannableStringBuilder4 = new SpannableStringBuilder(ChannelScanView.this.getResources().getText(R.string.settings_channel_scanning_sub_hint_audio));
                spannableStringBuilder.append((CharSequence) spannableStringBuilder3);
                spannableStringBuilder.append((CharSequence) " ");
                spannableStringBuilder.append((CharSequence) spannableStringBuilder4);
                ChannelScanView.this.g_scanning_sub_hint.setText(spannableStringBuilder);
                if (channels == 0 && radios == 0) {
                    ChannelScanView.this.g_scanning_hint.setText(R.string.settings_channel_scan_failed);
                }
                ChannelScanView.this.update_hint_button();
            }
        }, 2000L);
    }

    public void update_scan_completed(final int tv_count, final int radio_count) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                ChannelScanView.this.g_scan_bar.setVisibility(GONE);
                ChannelScanView.this.g_scanning_hint_dot.setVisibility(GONE);
                ChannelScanView channelScanView = ChannelScanView.this;
                channelScanView.removeCallbacks(channelScanView.g_dot_runnable);
                ChannelScanView.this.g_scanning_hint.setText(R.string.settings_channel_scanning_completed);
                int channels = tv_count;
                int radios = radio_count;
                String valueOf = String.valueOf(channels);
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(valueOf);
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ChannelScanView.this.getContext().getColor(R.color.pvr_red_color)), 0, valueOf.length(), 33);
                SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder(ChannelScanView.this.getResources().getText(R.string.settings_channel_scanning_sub_hint_video));
                spannableStringBuilder.append((CharSequence) " ");
                spannableStringBuilder.append((CharSequence) spannableStringBuilder2);
                spannableStringBuilder.append((CharSequence) ", ");
                String valueOf2 = String.valueOf(radios);
                SpannableStringBuilder spannableStringBuilder3 = new SpannableStringBuilder(valueOf2);
                spannableStringBuilder3.setSpan(new ForegroundColorSpan(ChannelScanView.this.getContext().getColor(R.color.pvr_red_color)), 0, valueOf2.length(), 33);
                SpannableStringBuilder spannableStringBuilder4 = new SpannableStringBuilder(ChannelScanView.this.getResources().getText(R.string.settings_channel_scanning_sub_hint_audio));
                spannableStringBuilder.append((CharSequence) spannableStringBuilder3);
                spannableStringBuilder.append((CharSequence) " ");
                spannableStringBuilder.append((CharSequence) spannableStringBuilder4);
                ChannelScanView.this.g_scanning_sub_hint.setText(spannableStringBuilder);
                if (channels == 0 && radios == 0) {
                    ChannelScanView.this.g_scanning_hint.setText(R.string.settings_channel_scan_failed);
                }
                ChannelScanView.this.update_hint_button();
            }
        }, 2000L);
    }

    public void update_scan_timeout() {
        post(new Runnable() {
            @Override
            public void run() {
                ChannelScanView.this.g_scan_bar.setVisibility(GONE);
                ChannelScanView.this.g_scanning_hint_dot.setVisibility(GONE);
                ChannelScanView channelScanView = ChannelScanView.this;
                channelScanView.removeCallbacks(channelScanView.g_dot_runnable);
                ChannelScanView.this.g_scanning_hint.setText(R.string.settings_channel_scan_timeout);
                ChannelScanView.this.update_hint_button();
            }
        });
    }

    public void update_scan_no_signal() {
        post(new Runnable() {
            @Override
            public void run() {
                ChannelScanView.this.g_scan_bar.setVisibility(GONE);
                ChannelScanView.this.g_scanning_hint_dot.setVisibility(GONE);
                ChannelScanView channelScanView = ChannelScanView.this;
                channelScanView.removeCallbacks(channelScanView.g_dot_runnable);
                ChannelScanView.this.g_scanning_hint.setText(R.string.settings_channel_scan_no_signal);
                ChannelScanView.this.update_hint_button();
            }
        });
    }

    public int get_channels() {
        ArrayList<String> allData = null/*TvChannelManager.getAllData(getContext())*/;
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < allData.size(); i++) {
            /*if (allData.get(i).getType().equals("SERVICE_TYPE_AUDIO_VIDEO")) {
                arrayList.add(Integer.valueOf(i));
            }*/
        }
        return arrayList.size();
    }

    public int get_radios() {
        ArrayList<String> allData = null /*TvChannelManager.getAllData(getContext())*/;
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < allData.size(); i++) {
            /*if (allData.get(i).getType().equals("SERVICE_TYPE_AUDIO")) {
                arrayList.add(Integer.valueOf(i));
            }*/
        }
        return arrayList.size();
    }

    public void update_hint_button() {
        this.g_button.setVisibility(VISIBLE);
        this.g_button.requestFocus();
        this.g_scan_count_down_time = 3;
        removeCallbacks(this.g_update_button_hint);
        post(this.g_update_button_hint);
    }

    public void set_on_back_listener(go_back_listener listener) {
        this.g_go_back_listener = listener;
    }
}
