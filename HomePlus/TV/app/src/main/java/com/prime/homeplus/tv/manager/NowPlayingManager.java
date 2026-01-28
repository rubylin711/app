package com.prime.homeplus.tv.manager;

import android.content.Context;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.tvprovider.media.tv.Channel;

import com.prime.homeplus.tv.data.GlobalState;
import com.prime.homeplus.tv.utils.ChannelUtils;

import com.prime.datastructure.sysdata.GposInfo;

public class NowPlayingManager {
    private static final String TAG = "NowPlayingManager";
    private static String globalLastChannelDisplayNumber = "";

    private Context context;
    private Channel currentChannel = null, previousChannel = null;
    TvContentRating currentContentBlockedRating = null;
    private static final long TUNE_DEBOUNCE_DELAY_MS = 100;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingTuneRunnable = null;

    public NowPlayingManager(Context context) {
        this.context = context;
    }

    public Channel getPreviousChannel() {
        return previousChannel;
    }

    public void setPreviousChannelNumber(Channel ch) {
        previousChannel = ch;
    }

    public void saveLastChannelDisplayNumber(String displayNum) {
        GlobalState.lastWatchedChannelNumber = displayNum;
        // --- Sync with CNSLauncher ---
        try {
            if (currentChannel != null) {
                String lastChannelId = currentChannel.getServiceId() + " # " + currentChannel.getTransportStreamId()
                        + " # " + currentChannel.getOriginalNetworkId();

                Log.d(TAG, "Sync to CNSLauncher: " + lastChannelId);
                GposInfo.setLastPlayChannelId(context,lastChannelId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync last channel to Settings.System", e);
        }
        // -----------------------------
    }

    public TvContentRating getCurrentContentBlockedRating() {
        return currentContentBlockedRating;
    }

    public void setCurrentContentBlockedRating(TvContentRating contentBlockedRating) {
        currentContentBlockedRating = contentBlockedRating;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void updateCurrentChannel() {
        currentChannel = ChannelUtils.getChannelFullData(context,
                TvContract.buildChannelUri(currentChannel.getId()));
    }

    public void setCurrentChannel(Channel ch) {
        Log.d(TAG, "setCurrentChannel ch:" + ch.getDisplayName());
        if (ch != null) {
            previousChannel = currentChannel;
            currentChannel = ch;
            saveLastChannelDisplayNumber(currentChannel.getDisplayNumber());
        }
    }

    public void tune(TvView tvView, Channel ch) {
		android.os.Bundle params = new android.os.Bundle();
        params.putBoolean("SIDEBAND_MODE", true);
        tune(tvView, ch, params);
    }

    public void tune(TvView tvView, Channel ch, Bundle params) {
        if (ch == null || ch.getId() == -1) {
            Log.e(TAG, "tune: Invalid channel or channel ID is -1. Aborting tune.");
            return;
        }

        setCurrentChannel(ch);

        if (pendingTuneRunnable != null) {
            handler.removeCallbacks(pendingTuneRunnable);
        }

        pendingTuneRunnable = () -> {
            String channelUrl = "content://android.media.tv/channel/" + ch.getId();
            Log.d(TAG, "Tuning to channel: " + channelUrl +
                    " ChannelUtils.INPUT_ID = " + ChannelUtils.INPUT_ID);
            currentContentBlockedRating = null;

            tvView.tune(ChannelUtils.INPUT_ID, Uri.parse(channelUrl), params);
        };
        handler.postDelayed(pendingTuneRunnable, TUNE_DEBOUNCE_DELAY_MS);
    }

    public void cancelPendingTune() {
        if (pendingTuneRunnable != null) {
            handler.removeCallbacks(pendingTuneRunnable);
            pendingTuneRunnable = null;
        }
    }
}
