package com.prime.homeplus.tv.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.tvprovider.media.tv.Channel;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.dtv.PrimeDtv;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.error.ErrorMessageResolver;
import com.prime.homeplus.tv.ui.activity.MainActivity;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.NetworkUtils;
import com.prime.homeplus.tv.utils.PrimeUtils;
import com.prime.homeplus.tv.utils.QrCodeUtils;
import com.prime.homeplus.tv.utils.ReportExporter;

public class LiveSignalInfoFragment extends Fragment {
    private static final String TAG = "LiveSignalInfoFragment";
    private static final long REFRESH_LIVE_SIGNAL_INFO_PERIOD_MS = 10 * 1000;

    private Button btnSignalInfoClose;
    private Button btnSignalInfoReport;
    private TextView tvSignalInfoCh;
    private TextView tvSignalFreq;
    private TextView tvSignalInfoSymbol;
    private TextView tvSignalQAM;
    private TextView tvSignalInfoBer;
    private TextView tvSignalInfoSnr;
    private TextView tvSignalInfoStrength;
    private TextView tvSignalStbSn;
    private TextView tvSignalInfoCardSn;
    private TextView tvSignalStbIp;
    private TextView tvSignalInfoSiInfo;
    private TextView tvSignalInfoStreamInfo;
    private ProgressBar pbSNR;
    private ProgressBar pbStrength;
    private ImageView ivSignalInfoQRCode;

    private Handler handler = new Handler();
    private final Runnable refreshLiveSignalInfoRunnable = this::refreshLiveSignalInfo;

    private static final String ARG_CHANNEL_ID = "channel_id";

    private long mChannelId = -1;
    private Channel mChannel = null;
    private int qam_value[] = { 16, 32, 64, 128, 256 };

    public static LiveSignalInfoFragment newInstance(long channelId) {
        LiveSignalInfoFragment fragment = new LiveSignalInfoFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CHANNEL_ID, channelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChannelId = getArguments().getLong(ARG_CHANNEL_ID);
        }
        if (mChannelId != -1) {
            mChannel = ChannelUtils.getChannelFullData(getContext(),
                    TvContract.buildChannelUri(mChannelId));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_live_signal_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        Log.d(TAG, "initViews");
        btnSignalInfoClose = view.findViewById(R.id.btnSignalInfoClose);
        btnSignalInfoReport = view.findViewById(R.id.btnSignalInfoReport);
        tvSignalInfoCh = view.findViewById(R.id.tvSignalInfoCh);
        tvSignalFreq = view.findViewById(R.id.tvSignalFreq);
        tvSignalInfoSymbol = view.findViewById(R.id.tvSignalInfoSymbol);
        tvSignalQAM = view.findViewById(R.id.tvSignalQAM);
        tvSignalInfoBer = view.findViewById(R.id.tvSignalInfoBer);
        tvSignalInfoSnr = view.findViewById(R.id.tvSignalInfoSnr);
        tvSignalInfoStrength = view.findViewById(R.id.tvSignalInfoStrength);
        tvSignalStbSn = view.findViewById(R.id.tvSignalStbSn);
        tvSignalInfoCardSn = view.findViewById(R.id.tvSignalInfoCardSn);
        tvSignalStbIp = view.findViewById(R.id.tvSignalStbIp);
        tvSignalInfoSiInfo = view.findViewById(R.id.tvSignalInfoSiInfo);
        tvSignalInfoStreamInfo = view.findViewById(R.id.tvSignalInfoStreamInfo);
        pbSNR = view.findViewById(R.id.pbSNR);
        pbStrength = view.findViewById(R.id.pbStrength);
        ivSignalInfoQRCode = view.findViewById(R.id.ivSignalInfoQRCode);

        btnSignalInfoClose.setOnClickListener((v) -> {
            closeFragment();
        });

        btnSignalInfoClose.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    closeFragment();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    btnSignalInfoReport.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    return true;
                }
            }
            return false;
        });

        btnSignalInfoReport.setOnClickListener((v) -> {
            Log.d(TAG, " btnSignalInfoReport click");
            // TODO 回報VBM功能?
        });

        btnSignalInfoReport.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    closeFragment();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    btnSignalInfoClose.requestFocus();
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefresh();
        if (btnSignalInfoClose != null) {
            btnSignalInfoClose.requestFocus();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefresh();
    }

    private void startRefresh() {
        resetHandlerDelay(handler, refreshLiveSignalInfoRunnable, REFRESH_LIVE_SIGNAL_INFO_PERIOD_MS,
                "refreshLiveSignalInfo");
    }

    private void stopRefresh() {
        handler.removeCallbacks(refreshLiveSignalInfoRunnable);
    }

    @SuppressLint("SetTextI18n")
    private void refreshLiveSignalInfo() {
        if (isVisible()) {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
            if (mChannel != null && primeDtv != null) {
                ProgramInfo programInfo = PrimeUtils.getProgramInfo(mChannel);
                if (programInfo != null) {
                    TpInfo tpInfo = primeDtv.tp_info_get(programInfo.getTpId());
                    tvSignalInfoCh.setText("CH" + mChannel.getDisplayNumber() + " " + mChannel.getDisplayName());
                    if (tpInfo != null) {
                        tvSignalFreq.setText(tpInfo.CableTp.getFreq() * 100 + "");
                        tvSignalInfoSymbol.setText(tpInfo.CableTp.getSymbol() + "");
                        tvSignalQAM.setText(Integer.toString(qam_value[tpInfo.CableTp.getQam()]));
                        boolean isLock = PrimeUtils.tuner_get_lock(programInfo.getTunerId());
                        Log.d(TAG, "isLock = " + isLock);
                        if (isLock) {
                            int strength = PrimeUtils.tuner_get_strength(programInfo.getTunerId());
                            pbStrength.setProgress(strength);
                            tvSignalInfoStrength.setText(Integer.toString(strength));

                            int snr = PrimeUtils.tuner_get_snr(programInfo.getTunerId());
                            pbSNR.setProgress(snr);
                            tvSignalInfoSnr.setText(Integer.toString(snr));

                            double ber = PrimeUtils.tuner_get_ber(programInfo.getTunerId());
                            tvSignalInfoBer.setText(Double.toString(ber));
                            Log.d(TAG, "TYPE_TUNER_LOCK_STATUS Lock => mStrength :" + strength + " dBuV mSNR :" + snr
                                    + " dB Ber :" + ber);

                        } else {
                            Log.d(TAG, "TYPE_TUNER_LOCK_STATUS UnLock ");
                            reset_tuner_status();
                        }
                        tvSignalStbIp.setText(NetworkUtils.getIPAddress(true));
                        tvSignalStbSn.setText(Pvcfg.get_device_sn());
                        tvSignalInfoCardSn.setText(Pvcfg.get_device_sn());
                        tvSignalInfoSiInfo.setText(
                                "ONID:" + mChannel.getOriginalNetworkId() + ",TSID:" + mChannel.getTransportStreamId() +
                                        ",SID:" + mChannel.getServiceId());
                        tvSignalInfoStreamInfo.setText(getSignalInfoStreamInfo(programInfo));
                        // QrErrorEvent
                        String qrCode = ReportExporter.getQrErrorReport(getContext(), "SignalCheck",
                                mChannel);
                        Bitmap qrErrorBitmap = QrCodeUtils.generateQRCode(qrCode, 240);
                        ivSignalInfoQRCode.setImageBitmap(qrErrorBitmap);
                    }

                }
                // TODO : QRCode要如何產生及套用?
            }

            resetHandlerDelay(handler, refreshLiveSignalInfoRunnable, REFRESH_LIVE_SIGNAL_INFO_PERIOD_MS,
                    "refreshLiveSignalInfo");
        }
    }

    private void resetHandlerDelay(Handler h, Runnable runnable, long delayMillis, String tag) {
        // Log.d(TAG, "resetHandlerDelay [" + tag + "] delay: " + delayMillis + " ms");
        h.removeCallbacks(runnable);
        h.postDelayed(runnable, delayMillis);
    }

    private void closeFragment() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideLiveSignalInfo();
        }
    }

    @SuppressLint("SetTextI18n")
    private void reset_tuner_status() {
        pbStrength.setProgress(0);
        tvSignalInfoStrength.setText(Integer.toString(0));

        pbSNR.setProgress(0);
        tvSignalInfoSnr.setText(Integer.toString(0));

        tvSignalInfoBer.setText("0");
    }

    private String getSignalInfoStreamInfo(ProgramInfo programInfo) {
        String str = "";
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        int cmMode = GposInfo.getCmMode(getContext());//Settings.System.getInt(PrimeHomeplusTvApplication.getInstance().getContentResolver(), "cm_mode", 1);
        //        int ftiCreate = Settings.System.getInt(PrimeHomeplusTvApplication.getInstance().getContentResolver(), "FTI_CREATE", 0);
        int ftiCreate = GposInfo.getFTI_CREATE(PrimeHomeplusTvApplication.getInstance(),0);
        str += "ECMPID:" + PrimeUtils.getECMPid(programInfo);
        str += ",VPID:" + PrimeUtils.getVideoPid(programInfo);
        str += ",APID:" + PrimeUtils.getAudioPid(programInfo);
        str += ",\n";
        //try {
            int autoStandbyValue = GposInfo.getSleepTimeout(getContext());//Settings.Secure.getInt(PrimeHomeplusTvApplication.getInstance().getContentResolver(), "sleep_timeout");
            if (autoStandbyValue == -1)
                str += "AutoStandby:0";
            else
                str += "AutoStandby:1";
        //} catch (Settings.SettingNotFoundException e) {
        //    Log.e(TAG, "property sleep_timeout get fail !!");
            str += "AutoStandby:0";
        //}
        int hdcp = gposInfo != null ? GposInfo.getHDCPOnOff(getContext()) : 0;
        // TODO : HDCP: ? 這些要填上
        str += ",HDCP:" + hdcp;
        str += ",\n";
        // TODO : AL: ? , LiAD: ? 這些要填上
        str += "AL:" + Pvcfg.get_area_limitation();
        str += ",開通:" + cmMode;
        str += ",教學頁:" + ftiCreate;
        str += ",LiAD:" + Pvcfg.get_LiTvAd_onoff();

        return str;
    }
}
