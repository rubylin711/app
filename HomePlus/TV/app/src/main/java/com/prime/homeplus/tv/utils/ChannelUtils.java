package com.prime.homeplus.tv.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;

import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.TvContractCompat;
import android.os.SystemProperties;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.data.GenreData;

import java.util.ArrayList;
import java.util.List;


public class ChannelUtils {
    private static final String TAG = "ChannelUtils";

    // TODO: use other INPUT_ID
    public static String PRIME_TVINPUT_ID = "com.prime.tvinputframework/.PrimeTvInputService";
    public static String INPUT_ID = PRIME_TVINPUT_ID;//"com.google.android.tv.dtvinput/.DtvInputService";

    public static void setFavorite(Context context, Channel channel, int favorite) {
        ContentResolver cr = context.getContentResolver();
        ContentValues cv = new ContentValues();
        String channelUrl = "content://android.media.tv/channel/" + channel.getId();
        cv.put(TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_FLAG4, favorite);
        int rows = cr.update(Uri.parse(channelUrl), cv, null, null);
        Log.d(TAG, "update favorite (" + favorite + ") rows=" + rows
                + " uri=" + Uri.parse(channelUrl));
    }

    public static boolean isChannelFavorite(Context context, Channel channel) {
        String channelUrl = "content://android.media.tv/channel/" + channel.getId();
        Channel ch = ChannelUtils.getChannelFullData(context, Uri.parse(channelUrl));
        return ch != null && ch.getInternalProviderFlag4() != null && ch.getInternalProviderFlag4() == 1;
    }

    public static Channel getChannelFullData(Context context, Uri channelUri) {
        // 1. 定義要查詢的欄位 (Projection)
        // 傳入 null 代表查詢 "所有欄位" (Select *)
        String[] projection = null;

        // 2. 執行查詢
        try (Cursor cursor = context.getContentResolver().query(
                channelUri,
                projection,
                null, // selection (因為 Uri 已指定 ID，這裡可為 null)
                null, // selectionArgs
                null  // sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                return Channel.fromCursor(cursor);

            } else {
                Log.w("ChannelData", "找不到此 Uri 的資料: " + channelUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Channel> getAllChannelsByGenre(Context context,int GenreId) {
        Log.d(TAG, "getAllChannels GenreId = "+GenreId + " GenreName = "+GenreData.getGenreName(context,GenreId));
        List<Channel> all = new ArrayList<>();
        List<ProgramInfo> programInfoList = PrimeHomeplusTvApplication.get_prime_dtv_service().get_program_info_list(GenreData.getPrimeGenre(GenreId), MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);
        for(ProgramInfo p : programInfoList) {
            Channel ch = getChannelFullData(context, p.getTvInputChannelUri());
            if(ch != null)
                all.add(ch);
        }
        return all;
    }
    public static List<Channel> getAllChannelsByProgramInfo(Context context, List<ProgramInfo> programInfoList) {
        List<Channel> all = new ArrayList<>();
        for(ProgramInfo p : programInfoList) {
            Channel ch = getChannelFullData(context, p.getTvInputChannelUri());
            if(ch != null)
                all.add(ch);
        }
        return all;
    }

    public static List<ProgramInfo> getProgramInfosByGenre(Context context,int GenreId) {
        Log.d(TAG, "getProgramInfos GenreId = "+GenreId + " GenreName = "+GenreData.getGenreName(context,GenreId));
        return PrimeHomeplusTvApplication.get_prime_dtv_service().get_program_info_list(GenreData.getPrimeGenre(GenreId), MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);
    }

    public static ProgramInfo getProgramInfoByChannel(Channel channel) {
        int type;
        if (TvContract.Channels.SERVICE_TYPE_AUDIO_VIDEO.equals(channel.getServiceType()))
            type = 0;
        else
            type = 17;

        return PrimeHomeplusTvApplication.get_prime_dtv_service().get_program_by_ch_num(Integer.parseInt(channel.getDisplayNumber()), type);
    }

    public static boolean supportDolby(ProgramInfo programInfo) {
        if (programInfo == null)
            return false;

        for(ProgramInfo.AudioInfo audioInfo: programInfo.pAudios) {
            if (audioInfo.getMime().equals("ac3"))
                return true;
        }
        return false;
    }

    public static List<Channel> getAllFavoriteChannels(Context context) {
        Log.d(TAG, "getAllChannels");

        String selection = TvContract.Channels.COLUMN_INPUT_ID + "=? AND CAST(" + TvContract.Channels.COLUMN_DISPLAY_NUMBER + " AS INTEGER) < ? AND " +
                TvContract.Channels.COLUMN_SERVICE_TYPE + " != ?" + " AND " +
                TvContract.Channels.COLUMN_INTERNAL_PROVIDER_FLAG4 + " == 1";

        List<Channel> all = queryChannels(
                context,
                selection,
                new String[]{INPUT_ID, Pvcfg.MAX_DISPLAY_NUMBER+"", TvContractCompat.Channels.SERVICE_TYPE_OTHER},
                "CAST(" + TvContract.Channels.COLUMN_DISPLAY_NUMBER + " AS INTEGER)"
        );

        return all;
    }

    public static List<Channel> getAllChannels(Context context) {
        Log.d(TAG, "getAllChannels");

        String selection = TvContract.Channels.COLUMN_INPUT_ID + "=? AND CAST(" + TvContract.Channels.COLUMN_DISPLAY_NUMBER + " AS INTEGER) < ? AND " +
                TvContract.Channels.COLUMN_SERVICE_TYPE + " != ?";

        List<Channel> all = queryChannels(
                context,
                selection,
                new String[]{INPUT_ID, Pvcfg.MAX_DISPLAY_NUMBER+"", TvContractCompat.Channels.SERVICE_TYPE_OTHER},
                "CAST(" + TvContract.Channels.COLUMN_DISPLAY_NUMBER + " AS INTEGER)"
        );

        return all;
    }

    private static List<Channel> queryChannels(Context context, String selection, String[] selectionArgs, String sortOrder) {
        List<Channel> channels = new ArrayList<>();
        Cursor cursor = null;

        if (context == null) {
            Log.e(TAG, "Context is null. Cannot query channels.");
            return channels;
        }

        try {
            cursor = context.getContentResolver().query(
                    TvContract.Channels.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    sortOrder
            );

            if (cursor == null) {
                Log.w(TAG, "Cursor is null. No results returned.");
                return channels;
            }

            if (cursor.moveToFirst()) {
                do {
                    try {
                        Channel ch = Channel.fromCursor(cursor);
                        if (ch != null) {
                            channels.add(ch);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to parse a channel from cursor.", e);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No channels matched the selection criteria.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during channel query.", e);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception closeEx) {
                Log.w(TAG, "Failed to close cursor.", closeEx);
            }
        }

        return channels;
    }

    public static boolean isAdult(Context context, Channel channel) {
        if (channel == null) {
            return false;
        }

        return channel.getInternalProviderFlag3() != null && channel.getInternalProviderFlag3() == 1;
    }
	
    public static boolean isChannelLocked(Context context, Channel channel) {
        if (channel == null) {
            return false;
        }

        byte[] internalProviderData = channel.getInternalProviderDataByteArray();
        if (internalProviderData != null) {
            try {
                org.json.JSONObject jsonObject = new org.json.JSONObject(new String(internalProviderData));
                if (jsonObject.has("channel_lock")) {
                    return "1".equals(jsonObject.getString("channel_lock"));
                }
            } catch (Exception e) {
                Log.e(TAG, "isChannelLocked parse error", e);
            }
        }
        return false;
    }
}
