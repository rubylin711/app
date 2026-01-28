package com.prime.dtv.assistant;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class OnDeviceSearchProvider extends ContentProvider {
    private static final String TAG = "OnDeviceSearchProvider";
    private static final boolean DEBUG = true;

    private static final String SUGGEST_COLUMN_PROGRESS_BAR_PERCENTAGE = "progress_bar_percentage";

    private static final String[] SEARCHABLE_COLUMNS =
            new String[]{
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2,
                    SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE,
                    SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                    SearchManager.SUGGEST_COLUMN_CONTENT_TYPE,
                    SearchManager.SUGGEST_COLUMN_IS_LIVE,
                    SearchManager.SUGGEST_COLUMN_VIDEO_WIDTH,
                    SearchManager.SUGGEST_COLUMN_VIDEO_HEIGHT,
                    SearchManager.SUGGEST_COLUMN_DURATION,
                    SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR,
                    SUGGEST_COLUMN_PROGRESS_BAR_PERCENTAGE
            };

    static final String SUGGEST_PARAMETER_ACTION = "action";
    static final int ACTION_TYPE_SWITCH_CHANNEL = 2;
    static final int ACTION_TYPE_SWITCH_INPUT = 3;

    @Override
    public boolean onCreate() {
        return true;
    }


    /*
    assistant comes here
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (DEBUG) {
            Log.d(
                    TAG,
                    "query("
                            + uri
                            + ", "
                            + Arrays.toString(projection)
                            + ", "
                            + selection
                            + ", "
                            + Arrays.toString(selectionArgs)
                            + ", "
                            + sortOrder
                            + ")");
        }

        String action = uri.getQueryParameter(SUGGEST_PARAMETER_ACTION);

        if (selectionArgs == null) {
            throw new IllegalArgumentException(
                    "selectionArgs must be provided for the Uri: " + uri);
        }

        if (action == null) {
            Log.d(TAG, "Generic Search Request = " + selectionArgs[0]);
        } else if (Integer.parseInt(action) == ACTION_TYPE_SWITCH_CHANNEL) {
            Log.d(TAG, "Request to change Channel = " + selectionArgs[0]);
            return createChannel(selectionArgs[0]);
        } else if (Integer.parseInt(action) == ACTION_TYPE_SWITCH_INPUT) {
            Log.d(TAG, "Request to change input = " + selectionArgs[0]);
        } else {
            Log.d(TAG, "Unsupported Type = " + selectionArgs[0]);
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private Cursor createChannel(String query) {
        MatrixCursor cursor = new MatrixCursor(SEARCHABLE_COLUMNS, 1);
        List<String> row = new ArrayList<>(SEARCHABLE_COLUMNS.length);

        ProgramInfo resultChannel = searchChannel(query);
        if (resultChannel != null) {
            if (DEBUG) {
                Log.d(TAG,
                        "Found channel = "
                                + resultChannel.getDisplayNameFull()
                                + ", "
                                + resultChannel.getChannelId());
            }

            row.add(resultChannel.getDisplayName()); //channel name
            row.add("Description for " + resultChannel.getDisplayName()); //description
            row.add(LiveTvManager.get_channel_icon_url(getContext()
                    , resultChannel.getServiceId(MiniEPG.MAX_LENGTH_OF_SERVICE_ID))); //image
            row.add(Intent.ACTION_VIEW); //intent action
            row.add(TvContract.buildChannelUri(resultChannel.getChannelId()).toString()); //intent data
            row.add(TvContract.Channels.CONTENT_ITEM_TYPE); //content type
            row.add("1"); //live or not_live
            row.add("0"); //width
            row.add("0"); //height
            row.add("0"); //duration
            row.add("0"); //production year
            row.add("0"); //progress bar percentage
            cursor.addRow(row);
        }

        return cursor;
    }

    private ProgramInfo searchChannel(String query) {
        ProgramInfo resultChannel;
        if (TextUtils.isDigitsOnly(query)) { // query string is digit
            // find channel by ch number in tv
            resultChannel = searchChannelByChNum(Integer.parseInt(query), FavGroup.ALL_TV_TYPE);

            if (resultChannel == null) {
                // find channel in radio if not exist in tv
                resultChannel
                        = searchChannelByChNum(Integer.parseInt(query), FavGroup.ALL_RADIO_TYPE);
            }
        } else { // query string is name
            // find channel by ch name in tv
            resultChannel = searchChannelByPartialChName(query, FavGroup.ALL_TV_TYPE);

            if (resultChannel == null) {
                // find channel in radio if not exist in tv
                resultChannel = searchChannelByPartialChName(query, FavGroup.ALL_RADIO_TYPE);
            }
        }

        return resultChannel;
    }

    private ProgramInfo searchChannelByChNum(int chNum, int chType) {
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        return primeDtv.get_program_by_ch_num(chNum, chType);
    }

    private ProgramInfo searchChannelByPartialChName(String partialChName, int chType) {
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        List <ProgramInfo> programInfos = primeDtv.get_program_info_list(
                chType,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        programInfos.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));

        for (ProgramInfo programInfo : programInfos) {
            if (programInfo.getDisplayName().toLowerCase()
                    .contains(partialChName.toLowerCase())) { // partially equal
                return programInfo;
            }
        }

        return null;
    }
}
