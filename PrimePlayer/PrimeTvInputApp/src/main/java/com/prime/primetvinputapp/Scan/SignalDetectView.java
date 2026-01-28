package com.prime.primetvinputapp.Scan;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prime.primetvinputapp.R;

import java.text.DecimalFormat;

public class SignalDetectView extends RelativeLayout {
    private final static String TAG = "SignalDetectView";
    private String g_ber;
    private DecimalFormat g_ber_format;
    private DecimalFormat g_decimal_format;
    private String g_dot;
    private String g_mer;
    private String g_modulation;
    private String g_param_freq;
    private String g_power;
    private TextView g_scanning_hint;
    private TextView g_scanning_hint_dot;
    private TextView g_scanning_sub_hint;
    private TextView g_signal_parameter;
    private TextView g_signal_q_num;
    private ProgressBar g_signal_q_progress;
    private TextView g_signal_s_num;
    private ProgressBar g_signal_s_progress;
    private String g_symbol_rate;
    private boolean g_upload_info_to_acs;
    private Runnable g_upload_to_acs = new Runnable() {
        @Override
        public void run() {
            SignalDetectView signalDetectView = SignalDetectView.this;
            signalDetectView.send_signal_detect(g_symbol_rate, g_param_freq, g_modulation, g_mer, g_ber, g_power);
        }
    };

    public SignalDetectView(Context context) {
        super(context);
        init();
    }

    public SignalDetectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignalDetectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_signal_detect, this);
        this.g_scanning_hint     = (TextView) findViewById(R.id.lo_sd_textv_detecting_hint);
        this.g_scanning_sub_hint = (TextView) findViewById(R.id.lo_sd_textv_detecting_sub_hint);
        this.g_scanning_hint_dot = (TextView) findViewById(R.id.lo_sd_textv_detecting_hint_dot);
        this.g_signal_q_progress = (ProgressBar) findViewById(R.id.lo_sd_pgsb_signal_q_progress);
        this.g_signal_q_num      = (TextView) findViewById(R.id.lo_sd_textv_signal_q_num);
        this.g_signal_s_progress = (ProgressBar) findViewById(R.id.lo_sd_pgsb_signal_s_progress);
        this.g_signal_s_num      = (TextView) findViewById(R.id.lo_sd_textv_signal_s_num);
        this.g_signal_parameter  = (TextView) findViewById(R.id.lo_sd_textv_signal_parameter);
        this.g_decimal_format    = new DecimalFormat("0.0");
        this.g_ber_format        = new DecimalFormat("0.0E+00");
    }

    public void update_signal_params(int freq, int symbol, String qamText) {
        this.g_param_freq         = String.valueOf(freq);
        this.g_symbol_rate        = String.valueOf(symbol);
        this.g_modulation         = qamText;
        this.g_upload_info_to_acs = true;
        this.g_signal_parameter.setText(((Object) getContext().getText(R.string.settings_channel_frequency)) +
                ":" + freq + "\n" + ((Object) getContext().getText(R.string.settings_channel_symbol_rate)) +
                ":" + symbol + "\n" + ((Object) getContext().getText(R.string.settings_channel_qam)) +
                ":" + qamText + "\n");
    }

    public void update_signal_value(boolean lock, int strength, int quality, int snr, double ber) {
        if (lock) {
            this.g_scanning_hint.setText(getContext().getString(R.string.settings_signal_detecting_hint_detecting));
            this.g_signal_q_num.setText(this.g_decimal_format.format(snr) + " dB");
            this.g_signal_s_num.setText(this.g_decimal_format.format(strength) + " dBmV");

            this.g_signal_q_progress.setProgress(quality);
            this.g_signal_s_progress.setProgress(strength); // -25~45
            if (ber == 0) {
                ber = 1.0E-07; // according to our TBC linux STB (MTK)
            }
            this.g_scanning_sub_hint.setText("BER: " + this.g_ber_format.format(ber));
        } else {
            this.g_scanning_hint.setText(getContext().getString(R.string.settings_signal_detecting_hint_fail));
            this.g_signal_q_num.setText("N/A");
            this.g_signal_s_num.setText("N/A");

            this.g_signal_q_progress.setProgress(g_signal_q_progress.getMin());
            this.g_signal_s_progress.setProgress(g_signal_s_progress.getMin());
            this.g_scanning_sub_hint.setText("BER: N/A");
        }

        this.g_scanning_hint_dot.setText(get_moving_dot());
    }

    private String get_moving_dot() {
        String str = this.g_dot + ".";
        this.g_dot = str;
        if (str.length() >= 4) {
            this.g_dot = ".";
        }
        return this.g_dot;
    }

    public void remove_callback() {
        removeCallbacks(this.g_upload_to_acs);
    }

    public void send_signal_detect(String symbol, String freq, String qam, String mer, String ber, String strength) {
        /*
            AcsHelper.finishSignalDetection(getContext(), signalJson, false)
        */
    }
}
