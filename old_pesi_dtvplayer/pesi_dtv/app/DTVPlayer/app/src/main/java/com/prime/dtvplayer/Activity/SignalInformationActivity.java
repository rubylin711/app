package com.prime.dtvplayer.Activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * Created by edwin on 2017/11/17.
 */

public class SignalInformationActivity extends DTVActivity {
    final String TAG = getClass().getSimpleName();

    TextView chNoTxv;
    TextView symbolRateTxv;
    TextView freqTxv;
    TextView videoPidTxv;
    TextView audioPidTxv;
    TextView pcrPidTxv;
    TextView servPidTxv;
    TextView tidTxv;
    TextView lockTxv;
    TextView strengthTxv;
    TextView qualityTxv;
    TextView berTxv;

    TextView channel;
    TextView symbolRate;
    TextView frequency;
    TextView videoPid;
    TextView audioPid;
    TextView pcrPid;
    TextView serviceId;
    TextView tid;
    TextView lock;
    TextView strength;
    TextView quality;
    TextView ber;
    ProgressBar strengthPrg;
    ProgressBar qualityPrg;

    ActivityTitleView title;

    Runnable runnableProgBar;
    Handler handler = null;
    int curServNum;
    long curChannelId;
    int curServType;
    ProgramInfo programInfo;
    TpInfo tpInfo;
    private int tunerType;


    /* // Sat
        TextView antennaTxv;
        TextView polTxv;
        TextView antIdSatName;
        TextView polarization;
        SatInfo satInfo;
        AntInfo antInfo;
    */

    private static class signalInfo {
        static int channelNum       = 0;
        static String channelName   = "null";
        static int antennaId        = 0;
        static String satelliteName = "null";
        static String symbolRate    = "null";
        static String frequency     = "null";
        static String polarization  = "null";
        static String videoPid      = "null";
        static String audioPid      = "null";
        static String pcrPid        = "null";
        static String serviceId     = "null";
        static String tid           = "null";
        static String lock          = "null";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal_information);
        tunerType = GetCurTunerType();
        InitTitle();
        if(ViewHistory.getCurChannel() == null) {
            String str = getString(R.string.STR_NO_SIGNAL_INFORMATION);
            new MessageDialogView(this, str, 3000) {
                public void dialogEnd() {
                    finish();
                }
            }.show();
        }
        else {
            InitView();
            InitVar();
        }
    }

    @Override
    public void onConnected() {
        super.onConnected();

        if(handler != null) {
            UpdateSignalInfo();
            handler.post(runnableProgBar);
        }
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        if(handler != null)
          handler.removeCallbacks(runnableProgBar);
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.signalInfoTitleLayout);
        title.setTitleView(getString(R.string.STR_SIGNAL_INFORMATION_TITLE));
    }

    private void InitView() {
        chNoTxv       = (TextView) findViewById(R.id.idChNo);
        symbolRateTxv = (TextView) findViewById(R.id.idSymbolRate);
        freqTxv       = (TextView) findViewById(R.id.idFreq);
        videoPidTxv   = (TextView) findViewById(R.id.idVideoPid);
        audioPidTxv   = (TextView) findViewById(R.id.idAudioPid);
        pcrPidTxv     = (TextView) findViewById(R.id.idPcrPid);
        servPidTxv    = (TextView) findViewById(R.id.idServPid);
        tidTxv        = (TextView) findViewById(R.id.idTid);
        lockTxv       = (TextView) findViewById(R.id.idLock);
        strengthTxv   = (TextView) findViewById(R.id.idStrength);
        qualityTxv    = (TextView) findViewById(R.id.idQuality);
        berTxv        = (TextView) findViewById(R.id.idBer);

        channel      = (TextView) findViewById(R.id.idChNoValue);
        symbolRate   = (TextView) findViewById(R.id.idSymRateValue);
        frequency    = (TextView) findViewById(R.id.idFreqValue);
        videoPid     = (TextView) findViewById(R.id.idVideoPidValue);
        audioPid     = (TextView) findViewById(R.id.idAudioPidValue);
        pcrPid       = (TextView) findViewById(R.id.idPcrPidValue);
        serviceId    = (TextView) findViewById(R.id.idServIdValue);
        tid          = (TextView) findViewById(R.id.idTidValue);
        lock         = (TextView) findViewById(R.id.idLockValue);
        strength     = (TextView) findViewById(R.id.idStrengthValue);
        quality      = (TextView) findViewById(R.id.idQualityValue);
        ber          = (TextView) findViewById(R.id.idBerValue);
        strengthPrg  = (ProgressBar) findViewById(R.id.idStrengthPrgBar);
        qualityPrg   = (ProgressBar) findViewById(R.id.idQualityPrgBar);

        // Sat
        //antennaTxv  = (TextView) findViewById(R.id.idAntenna);
        //polTxv      = (TextView) findViewById(R.id.idPol);
        //antIdSatName = (TextView) findViewById(R.id.idAntValue);
        //polarization = (TextView) findViewById(R.id.idPolValue);
    }

    private void InitVar() {
        chNoTxv.setText(getString(R.string.STR_CH_NO).concat(":"));
        //antennaTxv.setText(getString(R.string.STR_ANTENNA).concat(":"));
        if(tunerType == TpInfo.ISDBT || tunerType == TpInfo.DVBT)
            symbolRateTxv.setText(getString(R.string.STR_ISDBT_BANDWIDTH).concat(":"));
        else
            symbolRateTxv.setText(getString(R.string.STR_SYMBOLRATE).concat(":"));
        freqTxv.setText(getString(R.string.STR_FREQUENCY).concat(":"));
        //polTxv.setText(getString(R.string.STR_POLARIZATION).concat(":"));
        videoPidTxv.setText(getString(R.string.STR_VIDEO_PID).concat(":"));
        audioPidTxv.setText(getString(R.string.STR_AUDIO_PID).concat(":"));
        pcrPidTxv.setText(getString(R.string.STR_PCR_PID).concat(":"));
        servPidTxv.setText(getString(R.string.STR_SERVICE_ID).concat(":"));
        tidTxv.setText(getString(R.string.STR_TID).concat(":"));
        lockTxv.setText(getString(R.string.STR_LOCK).concat(":"));
        strengthTxv.setText(getString(R.string.STR_STRENGTH).concat(":"));
        qualityTxv.setText(getString(R.string.STR_QUALITY).concat(":"));
        berTxv.setText(getString(R.string.str_signal_ber).concat(":"));

        handler = new Handler(getMainLooper());
        runnableProgBar = new Runnable() {
            @Override
            public void run() {
                UpdateProgressBar();
                handler.postDelayed(runnableProgBar, 1000);
            }
        };
    }

    private void UpdateSignalInfo() { // get data from server
        Log.d(TAG, "UpdateSignalInfo()");
        curChannelId = ViewHistory.getCurChannel().getChannelId();//GposInfoGet().getCurChannelId();
        curServType = ViewHistory.getCurGroupType();//GposInfoGet().getCurGroupType();
        Log.d(TAG, "UpdateSignalInfo: curChannelId = "+ curChannelId);
        Log.d(TAG, "UpdateSignalInfo: curServType = "+ curServType);
        programInfo = ProgramInfoGetByChannelId(curChannelId);
        tpInfo = TpInfoGet(programInfo.getTpId());
        int audioCurPos = AvControlGetAudioListInfo(ViewHistory.getPlayId()).getCurPos();

        signalInfo.channelNum = programInfo.getDisplayNum();
        signalInfo.channelName = programInfo.getDisplayName();
        if(tunerType == TpInfo.DVBC)
        {
            signalInfo.symbolRate = tpInfo.CableTp.getSymbol() + "";
            signalInfo.frequency = tpInfo.CableTp.getFreq()/1000 + ""; // Johnny 20190508 modify DVBC freq shown in UI from KHz to MHz
        }
        else if(tunerType == TpInfo.ISDBT || tunerType == TpInfo.DVBT)
        {
            switch (tpInfo.TerrTp.getBand())
            {
                case TpInfo.Terr.BAND_6MHZ:
                {
                    signalInfo.symbolRate = "6000";
                }break;
                case TpInfo.Terr.BAND_7MHZ:
                {
                    signalInfo.symbolRate = "7000";
                }break;
                case TpInfo.Terr.BAND_8MHZ:
                {
                    signalInfo.symbolRate = "8000";
                }break;
            }
            signalInfo.frequency = tpInfo.TerrTp.getFreq() + "";
        }
        else if(tunerType == TpInfo.DVBS)
        {
            signalInfo.symbolRate = tpInfo.SatTp.getSymbol() + "";
            signalInfo.frequency = tpInfo.SatTp.getFreq() + "";
        }

        signalInfo.videoPid = programInfo.pVideo.getPID() + "";
        signalInfo.audioPid = ( programInfo.pAudios.size() == 0 ) ? // Edwin 20181130 fix no audio pid
                getString(R.string.STR_NO_AUDIO_PID)
                : programInfo.pAudios.get(audioCurPos).getPid() + "";
        signalInfo.pcrPid = programInfo.getPcr() + "";
        signalInfo.serviceId = programInfo.getServiceId() + "";
        signalInfo.tid = programInfo.getTransportStreamId() + "";

        if (programInfo.getLock() == 1)
            signalInfo.lock = getString(R.string.STR_YES);
        else
            signalInfo.lock = getString(R.string.STR_NO);

        channel.setText(String.format(Locale.getDefault(), "%s %s", signalInfo.channelNum, signalInfo.channelName));
        symbolRate.setText(signalInfo.symbolRate);
        frequency.setText(signalInfo.frequency);
        videoPid.setText(signalInfo.videoPid);
        audioPid.setText(signalInfo.audioPid);
        pcrPid.setText(signalInfo.pcrPid);
        serviceId.setText(signalInfo.serviceId);
        tid.setText(signalInfo.tid);
        lock.setText(signalInfo.lock);

        // sat
        //satInfo = SatInfoGet(programInfo.getSatId());
        //antInfo = AntInfoGet(programInfo.getAntId());
        //signalInfo.antenna      = antInfo.getAntId();
        //signalInfo.satellite    = satInfo.getSatName();
        //signalInfo.polarization = tpInfo.SatTp.getPolar()+"";
        //antenna.setText(signalInfo.antenna);
        //polarization.setText(signalInfo.polarization);
    }

    private void UpdateProgressBar()
    {
        /* ProgressBar */
        int tunerId = 0;
        int strengthVal = 0;
        int qualityVal = 0;
        int lockStatus = 0;
        String strBer;

        tunerId     = tpInfo.getTuner_id();
        strengthVal = TunerGetStrength(tunerId);
        qualityVal  = TunerGetQuality(tunerId);
        lockStatus  = TunerGetLockStatus(tunerId);
        strBer      = TunerGetBER(tunerId);

        Log.d(TAG, "UpdateProgressBar: strengthVal ="+strengthVal+" , qualityVal = "+qualityVal+" , lockStatus = "+lockStatus);

        strength.setText(String.format(Locale.getDefault(), "%d %%", strengthVal));
        strengthPrg.setProgress(strengthVal);
        quality.setText(String.format(Locale.getDefault(), "%d %%", qualityVal));
        qualityPrg.setProgress(qualityVal);
        ber.setText(strBer);

        if (lockStatus == 1)
        {
            strengthPrg.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            qualityPrg.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            lock.setText(getString(R.string.STR_YES));
            /*
            strengthPrg.setProgressDrawable(getDrawable(R.drawable.progress_bar_lock_yes));
            qualityPrg.setProgressDrawable(getDrawable(R.drawable.progress_bar_lock_yes));
            */
        }
        else
        {
            lock.setText(getString(R.string.STR_NO));
            strengthPrg.setProgressTintList(ColorStateList.valueOf(Color.RED));
            qualityPrg.setProgressTintList(ColorStateList.valueOf(Color.RED));
        }
    }

}
