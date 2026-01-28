package com.TvInput;

import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import androidx.tvprovider.media.tv.TvContractCompat;
import android.util.Log;

import com.dolphin.dtv.HiDtvMediaPlayer;
//import com.google.android.exoplayer.util.Util;
import com.google.android.media.tv.companionlibrary.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * EpgSyncJobService that periodically runs to update channels and programs.
 */
public class EpgSyncService extends EpgSyncJobService
{
    private static final String TAG = "EpgSyncService";
    private static final boolean DEBUG = true;
    private static final String CHANNEL_LOGO = "https://storage.googleapis.com/android-tv/images/mpeg_dash.png";
    private static final String TEARS_OF_STEEL_ART = "https://storage.googleapis.com/gtv-videos-bucket/sample/images/tears.jpg";
    private static final String PESI_PACKAGE_NAME = "com.prime.dtvplayer";//gary20190905 fix input service always new channel not use exist channel

    public static final String KEY_CHANNEL_ID = "Channel ID";
    public static final long DEFAULT_EPG_DURATION = 8 * 24 * 3600 * 1000;
    private static int EPG_SYNC_PERIOD = 60 * 1000;

    private HiDtvMediaPlayer mHiDtv = HiDtvMediaPlayer.getInstance();

    private boolean hasEvent = false;
    private int mChannelSize = 0;
    private int mChannelCount = 0;

    public static int getEpgSyncPeriod () { // Edwin 20181115 for update Period
        return EPG_SYNC_PERIOD;
    }
//gary20190905 fix input service always new channel not use exist channel-s
    private TIFChannelData queryChannelExist(long ChannelId) {
        TvInputInfo InputInfo = getPesiTvInputInfo(this);
        TIFChannelData channelData = new TIFChannelData();
        if(InputInfo != null) {
            Uri uri = TvContractCompat.buildChannelsUriForInput(InputInfo.getId());
            String mSelectionClause = TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_DATA + " LIKE ?";
            InternalProviderData internalProviderData = new InternalProviderData();
            try {
                internalProviderData.setRepeatable(false);
                internalProviderData.put(KEY_CHANNEL_ID, ChannelId);
            } catch (InternalProviderData.ParseException e) {
                e.printStackTrace();
            }
            String[] mSelectionArgs = {"" + internalProviderData.toString()};
            try (Cursor cursor = this.getContentResolver().query(
                    uri, TIFChannelData.projection, mSelectionClause, mSelectionArgs, null)) {
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToNext();
                    channelData.setChannelId(cursor.getLong(0));
                    channelData.setInputId(cursor.getString(1));
                    channelData.setDisplayNumber(cursor.getString(2));
                    channelData.setDisplayName(cursor.getString(3));
                    channelData.setDescprition(cursor.getString(4));
                    channelData.setServiceId(cursor.getLong(5));
                    channelData.setOnId(cursor.getLong(6));
                    channelData.setTsId(cursor.getLong(7));
                    channelData.setServiceType(cursor.getString(8));
                    channelData.setType(cursor.getString(9));
                    byte[] bypeProviderData = cursor.getBlob(10);
                    if(bypeProviderData.length > 0) {
                        try {
                            InternalProviderData providerData = new InternalProviderData(bypeProviderData);
                            channelData.setInternalProviderData(providerData);
                        } catch (InternalProviderData.ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    Uri channelUri = TvContractCompat.buildChannelUri(channelData.getChannelId());
                    if (DEBUG == true) {
                        Log.d(TAG, "queryChannel===============================" + cursor.getPosition() + "===============================");
                        Log.d(TAG, "channelId = " + channelData.getChannelId());
                        Log.d(TAG, "channelUri = " + channelUri);
                        Log.d(TAG, "inputId = " + channelData.getInputId());
                        Log.d(TAG, "displayNumber = " + channelData.getDisplayNumber());
                        Log.d(TAG, "displayName = " + channelData.getDisplayName());
                        Log.d(TAG, "descprition = " + channelData.getDescprition());
                        Log.d(TAG, "serviceId = " + channelData.getServiceId());
                        Log.d(TAG, "onId = " + channelData.getOnId());
                        Log.d(TAG, "tsId = " + channelData.getTsId());
                        Log.d(TAG, "serviceType = " + channelData.getServiceType());
                        Log.d(TAG, "type = " + channelData.getType());
                        try {
                            Log.d(TAG, "internalProviderData.get(KEY_CHANNEL_ID)  = " + internalProviderData.get(KEY_CHANNEL_ID));
                        } catch (InternalProviderData.ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    return channelData;
                }
//                else {
//                    try (Cursor cursor2 = this.getContentResolver().query(
//                            uri, TIFChannelData.projection, null, null, null)) {
//                        while (cursor2 != null && cursor2.moveToNext()) {
//                            channelData.setChannelId(cursor2.getLong(0));
//                            channelData.setInputId(cursor2.getString(1));
//                            channelData.setDisplayNumber(cursor2.getString(2));
//                            channelData.setDisplayName(cursor2.getString(3));
//                            channelData.setDescprition(cursor2.getString(4));
//                            channelData.setServiceId(cursor2.getLong(5));
//                            channelData.setOnId(cursor2.getLong(6));
//                            channelData.setTsId(cursor2.getLong(7));
//                            channelData.setServiceType(cursor2.getString(8));
//                            channelData.setType(cursor2.getString(9));
//                            byte[] bypeProviderData = cursor2.getBlob(10);
//                            Log.d(TAG, "55555555555555555555555555555");
//                            if(bypeProviderData.length > 0) {
//                                try {
//                                    InternalProviderData providerData = new InternalProviderData(bypeProviderData);
//                                    channelData.setInternalProviderData(providerData);
//                                } catch (InternalProviderData.ParseException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                    Uri channelUri = TvContractCompat.buildChannelUri(channelData.getChannelId());
//                    if (DEBUG == true) {
//                        Log.d(TAG, "all queryChannel===============================" + cursor.getPosition() + "===============================");
//                        Log.d(TAG, "channelId = " + channelData.getChannelId());
//                        Log.d(TAG, "channelUri = " + channelUri);
//                        Log.d(TAG, "inputId = " + channelData.getInputId());
//                        Log.d(TAG, "displayNumber = " + channelData.getDisplayNumber());
//                        Log.d(TAG, "displayName = " + channelData.getDisplayName());
//                        Log.d(TAG, "descprition = " + channelData.getDescprition());
//                        Log.d(TAG, "serviceId = " + channelData.getServiceId());
//                        Log.d(TAG, "onId = " + channelData.getOnId());
//                        Log.d(TAG, "tsId = " + channelData.getTsId());
//                        Log.d(TAG, "serviceType = " + channelData.getServiceType());
//                        Log.d(TAG, "type = " + channelData.getType());
//                        try {
//                            Log.d(TAG, "internalProviderData.get(KEY_CHANNEL_ID)  = " + internalProviderData.get(KEY_CHANNEL_ID));
//                        } catch (InternalProviderData.ParseException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
            }
        }
        return null;
    }

    private static TvInputInfo getPesiTvInputInfo(Context context) {
        TvInputInfo tvInputInfo = null;
        TvInputManager tvInputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
        for (TvInputInfo info : tvInputManager.getTvInputList()) {
            Log.d( TAG, "info.getServiceInfo().name = " + info.getServiceInfo().name ) ;
            if (info.getServiceInfo().name.contains("com.TvInput.PesiTvInputService")) {
                tvInputInfo = info;
                Log.d( TAG, "tvInputInfo.getId()  =  " + tvInputInfo.getId());
                break;
            }
        }
        return tvInputInfo;
    }
    public static void DeletePesiTvInputChannels(Context context) {
        TvInputInfo InputInfo = getPesiTvInputInfo(context);
        TIFChannelData channelData = new TIFChannelData();
        if(InputInfo != null) {
            Uri uri = TvContractCompat.buildChannelsUriForInput(InputInfo.getId());
            String mSelectionClause = TvContractCompat.Channels.COLUMN_PACKAGE_NAME + " LIKE ?";
            String[] mSelectionArgs = {PESI_PACKAGE_NAME};
            try (Cursor cursor = context.getContentResolver().query(
                    uri, TIFChannelData.projection, mSelectionClause, mSelectionArgs, null)) {
                while (cursor != null && cursor.moveToNext()) {
                    long channelId = cursor.getLong(0);
                    String inputId = cursor.getString(1);
                    String displayNumber = cursor.getString(2);
                    String displayName = cursor.getString(3);
                    if (DEBUG == true) {
                        Log.d(TAG, "delete Channel===============================" + cursor.getPosition() + "===============================");
                        Log.d(TAG, "channelId = " + channelId);
                        Log.d(TAG, "inputId = " + inputId);
                        Log.d(TAG, "displayNumber = " + displayNumber);
                        Log.d(TAG, "displayName = " + displayName);
                    }
                    context.getContentResolver().delete(
                            TvContractCompat.buildChannelUri(channelId), null, null);
                }
            }
        }
    }

    private Channel getChannelByPesiChannelId(long channelId) {
        Channel channel = null;
        TIFChannelData channelData = null;
        channelData = queryChannelExist(channelId);
        if(channelData != null) {
            String[] projection = new String[] {
                    TvContractCompat.Channels._ID,
                    TvContractCompat.Channels.COLUMN_DESCRIPTION,
                    TvContractCompat.Channels.COLUMN_DISPLAY_NAME,
                    TvContractCompat.Channels.COLUMN_DISPLAY_NUMBER,
                    TvContractCompat.Channels.COLUMN_INPUT_ID,
                    TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_DATA,
                    TvContractCompat.Channels.COLUMN_NETWORK_AFFILIATION,
                    TvContractCompat.Channels.COLUMN_ORIGINAL_NETWORK_ID,
                    TvContractCompat.Channels.COLUMN_PACKAGE_NAME,
                    TvContractCompat.Channels.COLUMN_SEARCHABLE,
                    TvContractCompat.Channels.COLUMN_SERVICE_ID,
                    TvContractCompat.Channels.COLUMN_SERVICE_TYPE,
                    TvContractCompat.Channels.COLUMN_TRANSPORT_STREAM_ID,
                    TvContractCompat.Channels.COLUMN_TYPE,
                    TvContractCompat.Channels.COLUMN_VIDEO_FORMAT,
                    TvContractCompat.Channels.COLUMN_BROWSABLE,
                    TvContractCompat.Channels.COLUMN_LOCKED,
            };
            Uri channelUri = TvContractCompat.buildChannelUri(channelData.getChannelId());
            try (Cursor cursor = getContentResolver().query(channelUri, projection, null, null, null)) {
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToNext();
                    if (DEBUG == true) {
                        int index = 0;
                        if (!cursor.isNull(index)) {
                            Log.d(TAG, "getId = " + (cursor.getLong(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getDescription = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getDisplayName = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getDisplayNumber = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getInputId = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getInternalProviderData = " + (cursor.getBlob(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getNetworkAffiliation = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getOriginalNetworkId = " + (cursor.getInt(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getPackageName = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getSearchable = " + (cursor.getInt(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getServiceId = " + (cursor.getInt(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getServiceType = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getTransportStreamId= " + (cursor.getInt(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getType = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getVideoFormat = " + (cursor.getString(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getBrowasble = " + (cursor.getInt(index)));
                        }
                        if (!cursor.isNull(++index)) {
                            Log.d(TAG, "getLocked = " + (cursor.getInt(index)));
                        }
                    }
                    channel = Channel.fromCursor(cursor);
                }
            }
        }
        return channel;
    }
//gary20190905 fix input service always new channel not use exist channel-e
    @Override
    public List<Channel> getChannels ()
    {
        List<Channel> channelList = new ArrayList<Channel>();
        List<ProgramInfo> programInfoList = mHiDtv.GetProgramInfoList(ProgramInfo.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);

        if ( programInfoList == null || programInfoList.size() == 0 )
        {
            return channelList;
        }

        for ( ProgramInfo programInfo : programInfoList )
        {
            InternalProviderData internalProviderData = new InternalProviderData();
            try {
                internalProviderData.setRepeatable(false);
                internalProviderData.put(KEY_CHANNEL_ID, programInfo.getChannelId());
            }
            catch ( InternalProviderData.ParseException e )
            {
                e.printStackTrace();
            }

            Log.d(TAG, "getChannels: channel Num = "+programInfo.getDisplayNum()+"channel Name = "+programInfo.getDisplayName());
//gary20190905 fix input service always new channel not use exist channel-s
            Channel channel = null;
            channel = getChannelByPesiChannelId(programInfo.getChannelId());
            if(channel == null) {
                channel = new Channel.Builder()
                        .setPackageName(PESI_PACKAGE_NAME)
                        .setDisplayName(programInfo.getDisplayName())
                        .setDisplayNumber(String.valueOf(programInfo.getDisplayNum()))
                        //.setChannelLogo(CHANNEL_LOGO) // Edwin 20181113 remove channel logo
                        .setOriginalNetworkId((int) programInfo.getChannelId())   // network id is wrong now, tmp use pesi channel id instead
                        .setServiceId(programInfo.getServiceId())
                        .setTransportStreamId(programInfo.getTransportStreamId())
                        .setServiceType(TvContractCompat.Channels.SERVICE_TYPE_AUDIO_VIDEO)
                        .setType(TvContractCompat.Channels.TYPE_DVB_C)
                        .setInternalProviderData(internalProviderData)
                        .build();
            }
            if(channel != null)
                channelList.add(channel);
//gary20190905 fix input service always new channel not use exist channel-e
        }
        mChannelSize = channelList.size();
        EPG_SYNC_PERIOD = channelList.size() * 5000; // Edwin 20181115 update Period
        return channelList;
    }

    @Override
    public List<Program> getProgramsForChannel( Uri channelUri, Channel channel, long startMs, long endMs )
    {
        TvInputManager mManager = (TvInputManager) this.getSystemService(Context.TV_INPUT_SERVICE);//gary20190830 modify for pesilauncher TvView
//        mManager.requestChannelBrowsable(channelUri);//gary20190830 modify for pesilauncher TvView // @hide, find another method
        long startTime = mHiDtv.getDtvDate().getTime();
        long endTime = startTime + DEFAULT_EPG_DURATION;
        long tmpTime = startTime;
        long channelId = 0;

        try {
            channelId = Long.parseLong(channel
                    .getInternalProviderData()
                    .get(KEY_CHANNEL_ID)
                    .toString());
        } catch ( InternalProviderData.ParseException e ) {
            e.printStackTrace();
        }
        List<Program> programsList = new ArrayList<>();
        List<EPGEvent> eventList = mHiDtv.getEPGEvents(channelId,
                new Date(startTime), new Date(endTime), 0, 1000, 1);//eric lin 20181026 add empty event for live channel, last param 0 to 1

        if ( eventList != null && eventList.isEmpty() ) {//eric lin 20181102 avoid crash, add eventList != null
            Log.d(TAG, "getProgramsForChannel: No event");
        }
        else if ( eventList != null )
        {
            Log.d(TAG, "getProgramsForChannel: eventList.size() = "+eventList.size());
            for ( EPGEvent event : eventList )
            {
                // Edwin 20181107 fix empty event put at start time 0 -s
                if ( event.getStartTime() == event.getEndTime() )
                {
                    // Edwin 20181108 add this to check wrong time
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd HH:mm");
                    Log.d(TAG, "getProgramsForChannel:\n event name = " + event.getEventName() +
                            "\n start time = " + sdf.format(event.getStartTime()) +" end time = " + sdf.format(event.getEndTime()) +
                            "\n start time = " + event.getStartTime() + " end time = " + event.getEndTime()+" duration = "+event.getDuration());
                    continue;
                }
                if(event.getEventName() != null) {//eric lin 20181102 avoid crash, add this condition
                    if (event.getEventName().equals("")) {//eric lin 20181026 add empty event for live channel
                        event.setEventName(channel.getDisplayName());
                        event.setStartTime(tmpTime);
                        event.setEndTime(tmpTime + event.getDuration());
                        // Edwin 20181112 fix wrong time of empty event -s
                        if ( event.getStartTime() == event.getEndTime() ) {
                            event.setEndTime(endTime);
                        }
                        if ( event.getStartTime() >= endTime ) {
                            continue;
                        }
                        // Edwin 20181112 fix wrong time of empty event -e
                        Log.d(TAG, "getProgramsForChannel: start time = "+event.getStartTime()+" end time = "+event.getEndTime());
                    } else if (event.getEventName().isEmpty()) {
                        Log.d(TAG, "getProgramsForChannel: BK2");
                    } else if (event.getEventName().equals(null)) {
                        Log.d(TAG, "getProgramsForChannel: BK3");
                    } else if (event.getEventName().equals(" ")) {
                        Log.d(TAG, "getProgramsForChannel: BK4");
                    } else {
                        hasEvent = true; // Edwin 20181115 for update Period
                    }
                }
                tmpTime = event.getEndTime();
                // Edwin 20181107 fix empty event put at start time 0 -e

                InternalProviderData internalProviderData = new InternalProviderData();
                internalProviderData.setVideoType(/*Util.TYPE_DASH*/0); // edwin 20200429 for playing stream
                String description = mHiDtv.getDetailDescription(channelId, event.getEventId());

                //eric lin 20181108 live channel parental rate,-start
                //event.setParentalRate(7); //it is for test
                if(event.getParentalRate() >= 4){
                    int pr = event.getParentalRate();
                    Log.d(TAG, "getProgramsForChannel: pr="+pr);
                    String prStr;
                    switch(pr){
                        case 4:
                            prStr = "DVB_4";
                            break;
                        case 5:
                            prStr = "DVB_5";
                            break;
                        case 6:
                            prStr = "DVB_6";
                            break;
                        case 7:
                            prStr = "DVB_7";
                            break;
                        case 8:
                            prStr = "DVB_8";
                            break;
                        case 9:
                            prStr = "DVB_9";
                            break;
                        case 10:
                            prStr = "DVB_10";
                            break;
                        case 11:
                            prStr = "DVB_11";
                            break;
                        case 12:
                            prStr = "DVB_12";
                            break;
                        case 13:
                            prStr = "DVB_13";
                            break;
                        case 14:
                            prStr = "DVB_14";
                            break;
                        case 15:
                            prStr = "DVB_15";
                            break;
                        case 16:
                            prStr = "DVB_16";

                            break;
                        case 17:
                            prStr = "DVB_17";
                            break;
                        case 18:
                            prStr = "DVB_18";
                            break;
                        default:
                            prStr = "DVB_18";
                            break;
                    }
                    TvContentRating contentRatings = TvContentRating.createRating("com.android.tv", "DVB", prStr, (String)null);
                    TvContentRating[] TvCrArray = new TvContentRating[1];
                    TvCrArray[0] = contentRatings;

                    programsList.add(new Program.Builder()
                            .setContentRatings(TvCrArray)
                            .setChannelId(channelId)
                            .setTitle(event.getEventName())
                            .setDescription(description)
                            .setStartTimeUtcMillis(event.getStartTime())    // Edwin 20181112 fix wrong start time
                            .setEndTimeUtcMillis(event.getEndTime())        // Edwin 20181112 fix wrong end time
                            .setCanonicalGenres(new String[]{TvContractCompat.Programs.Genres.MOVIES})
                            .setPosterArtUri(TEARS_OF_STEEL_ART)
                            .setThumbnailUri(TEARS_OF_STEEL_ART)
                            .setInternalProviderData(internalProviderData)
                            .build());
                }else{//eric lin 20181108 live channel parental rate,-start,-end
                    programsList.add(new Program.Builder()
                            .setChannelId(channelId)
                            .setTitle(event.getEventName())
                            .setDescription(description)
                            .setStartTimeUtcMillis(event.getStartTime())    // Edwin 20181112 fix wrong start time
                            .setEndTimeUtcMillis(event.getEndTime())        // Edwin 20181112 fix wrong end time
                            .setCanonicalGenres(new String[]{TvContractCompat.Programs.Genres.MOVIES})
                            .setPosterArtUri(TEARS_OF_STEEL_ART)
                            .setThumbnailUri(TEARS_OF_STEEL_ART)
                            .setInternalProviderData(internalProviderData)
                            .build());
                }
            }
            mChannelCount++;
            Log.d(TAG, "getProgramsForChannel: mChannelSize = "+mChannelSize+" mChannelCount = "+mChannelCount+" hasEvent = "+hasEvent);
            if ( hasEvent && mChannelCount == mChannelSize ) {
                EPG_SYNC_PERIOD = 3600 * 1000; // Edwin 20181115 update Period
            }
        }

        return programsList;
    }
}
