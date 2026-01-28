package com.prime.homeplus.tv.utils;

import android.content.Context;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

public class TvViewUtils {
    private static final String TAG = "TvViewUtils";

    /**
     * Returns a resolution label ("HD", "FullHD", "4K") based on video width.
     *
     * @param tvView The TvView instance
     * @return Resolution string, or "Unknown" if unavailable
     */
    public static String getVideoResolutionLabel(TvView tvView) {
        if (tvView == null) {
            Log.w(TAG, "TvView is null");
            return "Unknown";
        }

        try {
            List<TvTrackInfo> videoTracks = tvView.getTracks(TvTrackInfo.TYPE_VIDEO);
            if (videoTracks == null || videoTracks.isEmpty()) {
                return "Unknown";
            }

            for (TvTrackInfo track : videoTracks) {
                if (track != null && track.getVideoWidth() > 0) {
                    int width = track.getVideoWidth();

                    if (width >= 3840) {
                        return "4K";
                    } else if (width >= 1920) {
                        return "FullHD";
                    } else if (width >= 1280) {
                        return "HD";
                    } else {
                        return "SD";
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error determining video resolution", e);
        }

        return "Unknown";
    }

    /**
     * Returns a list of all audio track languages, allowing duplicate and ordered access.
     *
     * @param tvView The TvView instance
     * @return List of language codes, or empty list if none
     */
    public static List<String> getAudioLanguageList(TvView tvView) {
        List<String> languageList = new ArrayList<>();

        if (tvView == null) {
            Log.w(TAG, "TvView is null");
            return languageList;
        }

        try {
            List<TvTrackInfo> audioTracks = tvView.getTracks(TvTrackInfo.TYPE_AUDIO);
            if (audioTracks != null) {
                for (TvTrackInfo track : audioTracks) {
                    String lang = track.getLanguage();
                    if (lang != null && !lang.trim().isEmpty()) {
                        languageList.add(lang.trim().toLowerCase());
                    } else {
                        languageList.add("und"); // undefined
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get audio languages", e);
        }

        return languageList;
    }

    /**
     * Get list of subtitle languages from TvView.
     * @param tvView The TvView instance
     * @return List of subtitle language codes (e.g., "en", "zh"), empty if none or error
     */
    public static List<String> getSubtitleLanguages(TvView tvView) {
        List<String> subtitleLanguages = new ArrayList<>();

        if (tvView == null) {
            Log.w(TAG, "TvView is null");
            return subtitleLanguages;
        }

        try {
            List<TvTrackInfo> subtitleTracks = tvView.getTracks(TvTrackInfo.TYPE_SUBTITLE);
            if (subtitleTracks != null) {
                for (TvTrackInfo track : subtitleTracks) {
                    String lang = track.getLanguage();
                    if (lang != null && !lang.trim().isEmpty()) {
                        subtitleLanguages.add(lang.trim().toLowerCase());
                    } else {
                        subtitleLanguages.add("und");  // undefined language
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving subtitle tracks", e);
        }

        return subtitleLanguages;
    }
    public static String cycleSubtitleTrack(Context context, TvView tvView) {
        if (tvView == null) {
            Log.w(TAG, "cycleSubtitleTrack: tvView is null");
            return null;
        }

        List<TvTrackInfo> subtitleTracks = tvView.getTracks(TvTrackInfo.TYPE_SUBTITLE);
        if (subtitleTracks == null || subtitleTracks.isEmpty()) {
            Log.d(TAG, "cycleSubtitleTrack: no subtitle tracks");
            // 沒字幕可切，直接回傳 null 給上層自行處理（例如 Toast「無字幕」）
            return null;
        }

        // 目前選到的 track id
        String currentId = tvView.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);

        int nextIndex = 0;
        boolean found = false;

        if (currentId != null) {
            for (int i = 0; i < subtitleTracks.size(); i++) {
                TvTrackInfo t = subtitleTracks.get(i);
                if (t != null && currentId.equals(t.getId())) {
                    // 下一個 index（再按一次就切到下一個）
                    nextIndex = i + 1;
                    found = true;
                    break;
                }
            }
        }

        // 如果原本沒有選任何 subtitle，或沒找到對應 id，就從 0 開始
        if (!found) {
            nextIndex = 0;
        }

        // 若超過最後一個，就代表「關閉字幕」
        if (nextIndex >= subtitleTracks.size()) {
            Log.d(TAG, "cycleSubtitleTrack: turn subtitle OFF");
            tvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, null);
            return "off";
        }

        // 選擇下一個 subtitle track
        TvTrackInfo nextTrack = subtitleTracks.get(nextIndex);
        if (nextTrack == null) {
            Log.w(TAG, "cycleSubtitleTrack: nextTrack is null at index " + nextIndex);
            tvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, null);
            return "off";
        }

        String nextId = nextTrack.getId();
        tvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, nextId);

        String lang = nextTrack.getLanguage();
        if (lang == null || lang.trim().isEmpty()) {
            lang = "und"; // undefined
        } else {
            lang = lang.trim().toLowerCase();
        }

        Log.d(TAG, "cycleSubtitleTrack: switch to index=" + nextIndex
                + " id=" + nextId + " lang=" + lang);
        return lang;
    }

    public static void moveAndResizeTvView(Context context, TvView tvView, int xDp, int yDp, int widthDp, int heightDp) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvView.getLayoutParams();
        params.width = dpToPx(context, widthDp);
        params.height = dpToPx(context, heightDp);

        // Clear original constraints (only if necessary)
        params.topToTop = ConstraintLayout.LayoutParams.UNSET;
        params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        params.startToStart = ConstraintLayout.LayoutParams.UNSET;
        params.endToEnd = ConstraintLayout.LayoutParams.UNSET;

        // Set layout params (size)）
        tvView.setLayoutParams(params);

        // Set translation position (x, y)）
        tvView.setTranslationX(dpToPx(context, xDp));
        tvView.setTranslationY(dpToPx(context, yDp));
    }

    public static void resetTvViewToFullscreen(TvView tvView) {
        // Set to match_parent
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );

        // Restore constraints: align all sides with parent (fullscreen)
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        // Remove margins
        params.setMargins(0, 0, 0, 0);

        // Apply new layout params
        tvView.setLayoutParams(params);

        // Clear any translation (to avoid residual offset)
        tvView.setTranslationX(0);
        tvView.setTranslationY(0);
    }

    private static int dpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
