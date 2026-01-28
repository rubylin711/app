package com.prime.tvinputframework.tunetest;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class TuneTestActivity extends Activity {
    private static final String TAG = "TuneTestActivity";
	
    private TvView tvView;

    private String currentInputId;
    private Uri currentChannelUri;

    // ★ Audio track 測試用
    private final List<TvTrackInfo> mAudioTracks = new ArrayList<>();
    private int mAudioIndex = 0;

    // ★ Subtitle track 測試用
    private final List<TvTrackInfo> mSubtitleTracks = new ArrayList<>();
    // -1 = OFF（關閉字幕），0..N-1 = 第幾條字幕
    private int mSubtitleIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvView = new TvView(this);
        setContentView(tvView);

        tvView.setCallback(new TvView.TvInputCallback() {
            @Override
            public void onVideoAvailable(String inputId) {
                Log.d(TAG, "onVideoAvailable: " + inputId);
            }

            @Override
            public void onVideoUnavailable(String inputId, int reason) {
                Log.d(TAG, "onVideoUnavailable: " + inputId + ", reason=" + reason);
            }

            @Override
            public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
                Log.d(TAG, "onTracksChanged, inputId=" + inputId
                        + " trackCount=" + (tracks == null ? 0 : tracks.size()));

                mAudioTracks.clear();
                mSubtitleTracks.clear();

                if (tracks != null) {
                    for (TvTrackInfo t : tracks) {
                        if (t.getType() == TvTrackInfo.TYPE_AUDIO) {
                            mAudioTracks.add(t);
                            Log.d(TAG, "  audio track: id=" + t.getId()
                                    + " lang=" + t.getLanguage());
                        } else if (t.getType() == TvTrackInfo.TYPE_SUBTITLE) {
                            mSubtitleTracks.add(t);
                            Log.d(TAG, "  subtitle track: id=" + t.getId()
                                    + " lang=" + t.getLanguage());
                        }
                    }
                }

                Log.d(TAG, "audio track count = " + mAudioTracks.size());
                Log.d(TAG, "subtitle track count = " + mSubtitleTracks.size());
                
                // ★ 同步目前選中的 audio track index（可有可無，主要是讓第一次切就真的「切到下一條」）
                String selectedAudioId = tvView.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
                mAudioIndex = 0;
                if (selectedAudioId != null) {
                    for (int i = 0; i < mAudioTracks.size(); i++) {
                        if (selectedAudioId.equals(mAudioTracks.get(i).getId())) {
                            mAudioIndex = i;
                            break;
                        }
                    }
                }
                Log.d(TAG, "current selected audio index = " + mAudioIndex);

                // ★ 同步目前選中的 subtitle track index
                String selectedSubId = tvView.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
                mSubtitleIndex = -1; // 先當作 OFF
                if (selectedSubId != null) {
                    for (int i = 0; i < mSubtitleTracks.size(); i++) {
                        if (selectedSubId.equals(mSubtitleTracks.get(i).getId())) {
                            mSubtitleIndex = i;
                            break;
                        }
                    }
                }
                Log.d(TAG, "current selected subtitle index = " + mSubtitleIndex);
            }

            @Override
            public void onTrackSelected(String inputId, int type, String trackId) {
                Log.d(TAG, "onTrackSelected: inputId=" + inputId
                        + " type=" + type + " trackId=" + trackId);
            }
        });

        handleIntent();
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(); // singleTop：新指令走這裡，不重建 Activity
    }

    private void handleIntent() {
        // 讀取傳進來的資料
        currentChannelUri = getIntent().getData(); // content://android.media.tv/channel/<id>
        currentInputId = getIntent().getStringExtra("inputId"); // com.prime.tvinputframework/.PrimeTvInputService
        float volume = getIntent().getFloatExtra("volume", 1.0f);

        long channelId = getIntent().getLongExtra("channelId", -1);
        if (currentChannelUri == null && channelId > 0) {
            currentChannelUri = ContentUris.withAppendedId(
                    TvContract.Channels.CONTENT_URI, channelId);
        }

        // 若沒提供 inputId，嘗試從該 channel 讀
        if (currentInputId == null && currentChannelUri != null) {
            currentInputId = queryInputId(currentChannelUri);
        }

        Log.d(TAG, "tune: inputId=" + currentInputId
                + ", uri=" + currentChannelUri
                + ", volume=" + volume);

        if (currentInputId != null && currentChannelUri != null) {
            tvView.setStreamVolume(volume);
            tvView.tune(currentInputId, currentChannelUri);
        } else {
            Log.e(TAG, "缺少 inputId 或 channelUri，無法 tune");
            finish();
        }
    }

    private String queryInputId(Uri channelUri) {
        String[] proj = { TvContract.Channels.COLUMN_INPUT_ID };
        try (Cursor c = getContentResolver().query(channelUri, proj, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        }
        return null;
    }

    // =============== 測試 onSelectTrack：用遙控器切音軌 / 字幕 ===============

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 你可以改成你習慣的鍵：例如 PROG_BLUE / MEDIA_AUDIO_TRACK / YELLOW / BLUE...
        Log.d(TAG, "keyCode:"+keyCode);
        if (keyCode == 131) {     // 你的 audio key
            switchAudioTrack();
            return true;
        }
        if (keyCode == 185) {     // 你的 subtitle key
            switchSubtitleTrack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void switchAudioTrack() {
        if (mAudioTracks.isEmpty()) {
            Log.d(TAG, "switchAudioTrack: no audio tracks");
            return;
        }

        mAudioIndex = (mAudioIndex + 1) % mAudioTracks.size();
        TvTrackInfo t = mAudioTracks.get(mAudioIndex);
        String trackId = t.getId();

        Log.d(TAG, "switchAudioTrack: index=" + mAudioIndex
                + " id=" + trackId
                + " size=" + mAudioTracks.size()
                + " lang=" + t.getLanguage());

        // ★ 這裡會觸發 PrimeTvInputServiceSession.onSelectTrack(.../TYPE_AUDIO...)
        tvView.selectTrack(TvTrackInfo.TYPE_AUDIO, trackId);
    }

    private void switchSubtitleTrack() {
        if (mSubtitleTracks.isEmpty()) {
            Log.d(TAG, "switchSubtitleTrack: no subtitle tracks");
            tvView.setCaptionEnabled(false);
            tvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, null);
            mSubtitleIndex = -1;
            return;
        }

        int count = mSubtitleTracks.size();

        // 狀態輪詢：
        // OFF(-1) → 0 → 1 → ... → (count-1) → OFF(-1) → ...
        if (mSubtitleIndex < 0) {
            // 目前是 OFF，切到第一條字幕
            mSubtitleIndex = 0;
        } else if (mSubtitleIndex < count - 1) {
            // 還沒到最後一條，往下一條
            mSubtitleIndex++;
        } else {
            // 已經是最後一條，再按一次就 OFF
            mSubtitleIndex = -1;
        }

        if (mSubtitleIndex == -1) {
            // 關閉字幕
            Log.d(TAG, "switchSubtitleTrack: OFF (disable subtitles)");
            tvView.setCaptionEnabled(false);
            // 通知 TIF 不選任何 subtitle track
            tvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, null);
            return;
        }

        TvTrackInfo t = mSubtitleTracks.get(mSubtitleIndex);
        String trackId = t.getId();

        Log.d(TAG, "switchSubtitleTrack: index=" + mSubtitleIndex
                + " id=" + trackId
                + " size=" + mSubtitleTracks.size()
                + " lang=" + t.getLanguage());

        // 開啟系統層 CC，並選定這條 subtitle track
        tvView.setCaptionEnabled(true);
        // ★ 這裡會觸發 PrimeTvInputServiceSession.onSelectTrack(.../TYPE_SUBTITLE...)
        tvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, trackId);
    }
}
