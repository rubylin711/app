package com.prime.dtvplayer.Sysdata;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.tvprovider.media.tv.ChannelLogoUtils;
import androidx.tvprovider.media.tv.PreviewChannel;
import androidx.tvprovider.media.tv.PreviewChannelHelper;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.TvContractCompat.RecordedPrograms;
import androidx.tvprovider.media.tv.WatchNextProgram;

import com.TvInput.EpgSyncService;
import com.TvInput.PesiTvInputService;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.model.RecordedProgram;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Activity.ViewActivity;
import com.prime.dtvplayer.Database.DBChannelFunc;
import com.prime.dtvplayer.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecommendChannel {
    private final String TAG = getClass().getSimpleName();
    private final String PACKAGE_NAME = "com.prime.dtvplayer";
    private final String PRIME_AUTHOR = "prime";
    private Context mContext;
    private Uri CHANNEL_LOGO_URI = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);
    private Uri PROGRAM_LOGO_URI = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);
    private Uri WATCH_NEXT_LOGO_URI = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);
    private TvProviderChannelInfo mChannelInfo = null;
    private TvProviderProgramInfo mProgramInfo = null;
    private List<TvProviderProgramInfo> mProgramInfoList = null;
    private TvProviderInfo mUriInfo = null;
    private ChannelBuilder mChannelBuilder = null;
    private ProgramBuilder mProgramBuilder = null;

    ///////////Origin
    private static final String DTV_SET_LAUNCHER_EPG = "com.prime.launcher.epgupdate";
    private static final String DTV_LAUNCHER_PACKAGE_NAME = "com.pesilauncher.pesilauncher"; // jim 2019/09/04 fix send update EPG broadcast to launcher failed

    private void BuildRecommandChannelEpg(long tvPorviderChannelID, String eventName, String shortEvent, long startTime, long endTime) {
        PreviewProgram previewProgram = new PreviewProgram.Builder()
                .setChannelId(tvPorviderChannelID)
                .setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                .setTitle(eventName)
                .setDescription(shortEvent)
                .setStartTimeUtcMillis(startTime)
                .setEndTimeUtcMillis(endTime)
                .setAuthor("prime")
                // Set more attributes...
                .build();
        Uri previewProgramUri = mContext.getContentResolver().insert(TvContractCompat.PreviewPrograms.CONTENT_URI, previewProgram.toContentValues());

        String strPR = previewProgram.toString();
        Log.d(TAG, "===================================================");
        Log.d(TAG, "BuildRecommandChannelEpg: tvPorviderChannelID =" + tvPorviderChannelID);
        Log.d(TAG, "BuildRecommandChannelEpg: strPR =" + strPR);
    }

    public void UpdateRecommandChannelEpg(DTVActivity mDTV, long service_id, long tvPorviderChannelID ) {
        Date nowtime = mDTV.getLocalTime();
        List<EPGEvent> epgEventList = mDTV.EpgEventGetEPGEventList(service_id,nowtime.getTime(),nowtime.getTime()+24*60*60*1000, 0);
        if(epgEventList != null) {
            for (int i = 0; i < epgEventList.size(); i++) {
                BuildRecommandChannelEpg( tvPorviderChannelID, epgEventList.get(i).getEventName(),epgEventList.get(i).getShortEvent(),epgEventList.get(i).getStartTime(),epgEventList.get(i).getEndTime());
            }
        }
    }

    private Uri BuildRecommandChannel(String displayName, int DisplayNumber, int OriginalNetworkId, long channelID) {
        String pesiChannelId = Long.toString(channelID);
        Channel channel = new Channel.Builder()
                .setDisplayName(displayName)
                .setDescription(displayName)
                .setDisplayNumber(""+DisplayNumber)
                .setType(TvContract.Channels.TYPE_PREVIEW)
                .setPackageName(PACKAGE_NAME)
                .setInputId(pesiChannelId)
                .setOriginalNetworkId(OriginalNetworkId)
                // Set more attributes...
                .build();
        Uri channelUri = mContext.getContentResolver().insert(TvContractCompat.Channels.CONTENT_URI, channel.toContentValues());
        String str_CH = channel.toString();
        Log.d(TAG, "BuildRecommandChannel:  pesiChannelId =" + pesiChannelId);
        Log.d(TAG, "BuildRecommandChannel:  str_CH =" + str_CH);
        return channelUri ;
    }

    public void UpdateRecommandChannelListDB(DTVActivity mDTV, ChannelHistory viewHistory) {
        // jim 2019/09/04 fix add epg data cause TIF channel channel number  continually increase -s
        String mSelectionClause = TvContractCompat.Channels.COLUMN_TYPE + " = ? AND " +
                TvContractCompat.Channels.COLUMN_PACKAGE_NAME + " = ? ";
        String[] mSelectionArgs = {TvContract.Channels.TYPE_PREVIEW,
                PACKAGE_NAME};

        int dechannelUri = mContext.getContentResolver().delete(TvContractCompat.Channels.CONTENT_URI,
                mSelectionClause, mSelectionArgs);
        if (viewHistory.getCurChannel() != null) {
            int tpid = viewHistory.getCurChannel().getTpId();

            List<SimpleChannel> recommandList = mDTV.MtestGetSimpleChannelListByTpID(tpid);
            SimpleChannel tempChannel = viewHistory.getCurChannel();
            ProgramInfo service = mDTV.ProgramInfoGetByChannelId(tempChannel.getChannelId());
            Uri channelUri = BuildRecommandChannel(tempChannel.getChannelName(), tempChannel.getChannelNum(), service.getOriginalNetworkId()
                    , tempChannel.getChannelId());
            UpdateRecommandChannelEpg(mDTV,tempChannel.getChannelId(), ContentUris.parseId(channelUri));

            for (int i = 0, j = 1; i < recommandList.size() && j < 6; i++) {
                tempChannel = recommandList.get(i);
                if ((tempChannel.getChannelId() != viewHistory.getCurChannel().getChannelId())) {//gary20190830 fix set pesi launcher recommand data count incorrect
                    service = mDTV.ProgramInfoGetByChannelId(tempChannel.getChannelId());
                    channelUri = BuildRecommandChannel(tempChannel.getChannelName(), tempChannel.getChannelNum(), service.getOriginalNetworkId()
                            , tempChannel.getChannelId());
                    UpdateRecommandChannelEpg(mDTV,tempChannel.getChannelId(), ContentUris.parseId(channelUri));
                    j++ ;
                }
            }
        }
        // jim 2019/09/04 fix add epg data cause TIF channel channel number  continually increase -e
    }

    public void UpdatePesiChannelListDB(ChannelHistory viewHistory) { //update pesi all channel to voice search db
        List<SimpleChannel> simpleChannelList = viewHistory.getCurChannelList();
        if(simpleChannelList != null && simpleChannelList.size() > 0) {
            List<DBChannelFunc.Channel> channelList = new ArrayList<DBChannelFunc.Channel>();
            for(int i = 0; i < simpleChannelList.size() ; i++) {
                DBChannelFunc.Channel channel = new DBChannelFunc.Channel();
                channel.setChannelId(simpleChannelList.get(i).getChannelId());
                channel.setChannelName(simpleChannelList.get(i).getChannelName());
                channel.setChannelNumber(simpleChannelList.get(i).getChannelNum());
                channelList.add(channel);
            }
            DBChannelFunc dbChannelFunc = new DBChannelFunc(mContext);
            dbChannelFunc.save(channelList);

            List<DBChannelFunc.Channel> testChannelList = dbChannelFunc.getChannelList();
            if(testChannelList != null) {
                Log.d(TAG,"testChannelList size = " + testChannelList.size());
                for(int i = 0; i < testChannelList.size(); i++) {
                    Log.d(TAG,"testChannelList.get(i).getChannelId() = " + testChannelList.get(i).getChannelId());
                    Log.d(TAG,"testChannelList.get(i).getChannelName() = " + testChannelList.get(i).getChannelName());
                    Log.d(TAG,"testChannelList.get(i).getChannelNumber() = " + testChannelList.get(i).getChannelNumber());
                }
            }
        }
    }

    public void SendBroadcaseToPesiLauncher() { //tell pesi launcher recommamd channel epg update done
        Intent PesiLauncherIntent = new Intent(DTV_SET_LAUNCHER_EPG);
        PesiLauncherIntent.setPackage(DTV_LAUNCHER_PACKAGE_NAME);// jim 2019/09/04 fix send update EPG broadcast to launcher failed
        mContext.sendBroadcast(PesiLauncherIntent);
        Log.d(TAG, "SetPesiLauncher: send update epg finish broadcast !!!!");
    }

    public void SetPesiLauncher(ChannelHistory viewHistory) {
        //UpdateRecommandChannelListDB(mDTV,mDTV.ViewHistory);
        //UpdatePesiChannelListDB(viewHistory);
        SendBroadcaseToPesiLauncher();
        EpgSyncService.DeletePesiTvInputChannels(mContext);
        PesiTvInputService.updateEPG(mContext,"leave DTV");//gary20190830 modify for pesilauncher TvView
    }

    public RecommendChannel(Context context) {
        mContext = context;
        mProgramInfoList = new ArrayList<>();
        mUriInfo = new TvProviderInfo();
        mChannelBuilder = new ChannelBuilder();
        mProgramBuilder = new ProgramBuilder();
    }
    ////////////////////////////////
    public ChannelBuilder getChannelBuilder(){
        return mChannelBuilder;
    }

    public ProgramBuilder getProgramBuilder(){
        return mProgramBuilder;
    }

    public List<TvProviderProgramInfo> getProgramInfoList() {
        return mProgramInfoList;
    }

    public TvProviderInfo getUriInfo() {
        return mUriInfo;
    }

    /**
     * Set UriInfo for Caller to Get Channel and Programs Uri
     * @param mUriinfo
     */
    private void setUriInfo(TvProviderInfo mUriinfo)
    {
        this.mUriInfo = mUriinfo;
    }
///////////////////////////


    public final class ChannelBuilder {
        private TvProviderChannelInfo tmpChannelInfo = null;

        public ChannelBuilder() {
            tmpChannelInfo = new TvProviderChannelInfo();
        }

        public String getType() {
            return this.tmpChannelInfo.getType();
        }

        public RecommendChannel.ChannelBuilder setType(String type) {
            this.tmpChannelInfo.setType(type);
            return this;
        }

        public String getDisplayName() {
            return this.tmpChannelInfo.getDisplayName();
        }

        public RecommendChannel.ChannelBuilder setDisplayName(String displayName) {
            this.tmpChannelInfo.setDisplayName(displayName);
            return this;
        }

        public String getDisplayNumber() {
            return this.tmpChannelInfo.getDisplayNumber();
        }

        public RecommendChannel.ChannelBuilder setDisplayNumber(String displayNumber) {
            this.tmpChannelInfo.setDisplayNumber(displayNumber);
            return this;
        }

        public String getDescription() {
            return this.tmpChannelInfo.getDescription();
        }

        public RecommendChannel.ChannelBuilder setDescription(String description) {
            this.tmpChannelInfo.setDescription(description);
            return this;
        }

        public String getPackageName() {
            return this.tmpChannelInfo.getPackageName();
        }

        public RecommendChannel.ChannelBuilder setPackageName(String packageName) {
            this.tmpChannelInfo.setPackageName(packageName);
            return this;
        }

        public String getInputId() {
            return this.tmpChannelInfo.getInputId();
        }

        public RecommendChannel.ChannelBuilder setInputId(String inputId) {
            this.tmpChannelInfo.setInputId(inputId);
            return this;
        }

        public int getOriginalNetworkId() {
            return this.tmpChannelInfo.getOriginalNetworkId();
        }

        public RecommendChannel.ChannelBuilder setOriginalNetworkId(int originalNetworkId) {
            this.tmpChannelInfo.setOriginalNetworkId(originalNetworkId);
            return this;
        }

        public String getChannelLogo() {
            return this.tmpChannelInfo.getChannelLogo();
        }

        public RecommendChannel.ChannelBuilder setChannelLogo(String channelLogo) {
            this.tmpChannelInfo.setChannelLogo(channelLogo);
            return this;
        }

        public String getInternalProviderData() {
            return this.tmpChannelInfo.getInternalProviderData();
        }

        public RecommendChannel.ChannelBuilder setInternalProviderData(String internalProviderData) {
            this.tmpChannelInfo.setInternalProviderData(internalProviderData);
            return this;
        }

        public Intent getAppIntent() {
            return this.tmpChannelInfo.getAppintent();
        }

        public RecommendChannel.ChannelBuilder setAppIntent(Intent intent) {
            this.tmpChannelInfo.setAppintent(intent);
            return this;
        }

        public boolean getSearchable() {
            return this.tmpChannelInfo.getSearchable();
        }

        public RecommendChannel.ChannelBuilder setSearchable(boolean searchable) {
            this.tmpChannelInfo.setSearchable(searchable);
            return this;
        }

        public TvProviderChannelInfo build() {
            mChannelInfo = new TvProviderChannelInfo();
            if (mChannelInfo != tmpChannelInfo) {
                mChannelInfo.type = tmpChannelInfo.type;
                mChannelInfo.displayName = tmpChannelInfo.displayName;
                mChannelInfo.displayNumber = tmpChannelInfo.displayNumber;
                mChannelInfo.Description = tmpChannelInfo.Description;
                mChannelInfo.packageName = tmpChannelInfo.packageName;
                mChannelInfo.inputId = tmpChannelInfo.inputId;
                mChannelInfo.originalNetworkId = tmpChannelInfo.originalNetworkId;
                mChannelInfo.channelLogo = tmpChannelInfo.channelLogo;
                mChannelInfo.internalProviderData = tmpChannelInfo.internalProviderData;
                mChannelInfo.intent = tmpChannelInfo.intent;
                mChannelInfo.searchable = tmpChannelInfo.searchable;
            }
            return mChannelInfo;
        }
    }


    public class TvProviderChannelInfo{
        private String channelId;
        private String type;
        private String displayName;
        private String displayNumber;
        private String Description;
        private String packageName;
        private String inputId;
        private int originalNetworkId;
        private String channelLogo;
        private String internalProviderData;
        private Intent intent;
        private boolean searchable;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayNumber() {
            return displayNumber;
        }

        public void setDisplayNumber(String displayNumber) {
            this.displayNumber = displayNumber;
        }

        public String getDescription() {
            return Description;
        }

        public void setDescription(String description) {
            Description = description;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getInputId() {
            return inputId;
        }

        public void setInputId(String inputId) {
            this.inputId = inputId;
        }

        public int getOriginalNetworkId() {
            return originalNetworkId;
        }

        public void setOriginalNetworkId(int originalNetworkId) {
            this.originalNetworkId = originalNetworkId;
        }

        public String getChannelLogo() {
            return channelLogo;
        }

        public void setChannelLogo(String channelLogo) {
            this.channelLogo = channelLogo;
        }

        public String getInternalProviderData() {
            return internalProviderData;
        }

        public void setInternalProviderData(String internalProviderData) {
            this.internalProviderData = internalProviderData;
        }


        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public Intent getAppintent() {
            return intent;
        }

        public void setAppintent(Intent intent) {
            this.intent = intent;
        }

        public boolean getSearchable() {
            return searchable;
        }

        public void setSearchable(boolean searchable) {
            this.searchable = searchable;
        }
    }


    public final class ProgramBuilder {
        private TvProviderProgramInfo tmpProgramInfo = null;

        public ProgramBuilder() {
            tmpProgramInfo = new TvProviderProgramInfo();
        }

        public int getType() {
            return this.tmpProgramInfo.getType();
        }

        public RecommendChannel.ProgramBuilder setType(int type) {
            this.tmpProgramInfo.setType(type);
            return this;
        }

        public String getTitle() {
            return this.tmpProgramInfo.getTitle();
        }

        public RecommendChannel.ProgramBuilder setTitle(String title) {
            this.tmpProgramInfo.setTitle(title);
            return this;
        }

        public String getDescription() {
            return this.tmpProgramInfo.getDescription();
        }

        public RecommendChannel.ProgramBuilder setDescription(String description) {
            this.tmpProgramInfo.setDescription(description);
            return this;
        }

        public long getStartTime() {
            return this.tmpProgramInfo.getStartTime();
        }

        public RecommendChannel.ProgramBuilder setStartTime(long startTime) {
            this.tmpProgramInfo.setStartTime(startTime);
            return this;
        }

        public long getEndTime() {
            return this.tmpProgramInfo.getEndTime();
        }

        public RecommendChannel.ProgramBuilder setEndTime(long endTime) {
            this.tmpProgramInfo.setEndTime(endTime);
            return this;
        }

        public String getAuthor() {
            return this.tmpProgramInfo.getAuthor();
        }

        public RecommendChannel.ProgramBuilder setAuthor(String author) {
            this.tmpProgramInfo.setAuthor(author);
            return this;
        }

        public android.content.Intent getIntent() {
            return this.tmpProgramInfo.getIntent();
        }

        public RecommendChannel.ProgramBuilder setIntent(android.content.Intent intent) {
            this.tmpProgramInfo.setIntent(intent);
            return this;
        }

        public String getGenre() {
            return this.tmpProgramInfo.getGenre();
        }

        public RecommendChannel.ProgramBuilder setGenre(String genre) {
            this.tmpProgramInfo.setGenre(genre);
            return this;
        }

        public Uri getPosterArtUri() {
            return this.tmpProgramInfo.getPosterArtUri();
        }

        public RecommendChannel.ProgramBuilder setPosterArtUri(Uri posterArtUri) {
            this.tmpProgramInfo.setPosterArtUri(posterArtUri);
            return this;
        }

        public String getInternalProviderData() {
            return this.tmpProgramInfo.getInternalProviderData();
        }

        public RecommendChannel.ProgramBuilder setInternalProviderData(String internalProviderData) {
            this.tmpProgramInfo.setInternalProviderData(internalProviderData);
            return this;
        }

        public long getLastEngagementTimeUtcMills() {
            return this.tmpProgramInfo.getLastEngagementTimeUtcMills();
        }

        public RecommendChannel.ProgramBuilder setLastEngagementTimeUtcMillis(long lastEnagementTimeUtcMills) {
            this.tmpProgramInfo.setLastEngagementTimeUtcMillis(lastEnagementTimeUtcMills);
            return this;
        }

        public int getLastPlaybackPositionMillis() {
            return this.tmpProgramInfo.getLastPlaybackPositionMillis();
        }

        public RecommendChannel.ProgramBuilder setLastPlaybackPositionMillis(int lastPlaybackPositionMillis) {
            this.tmpProgramInfo.setLastPlaybackPositionMillis(lastPlaybackPositionMillis);
            return this;
        }

        public long getPesiChannelId() {
            return this.tmpProgramInfo.getPesiChannelId();
        }

        public RecommendChannel.ProgramBuilder setPesiChannelId(long pesiChannelId) {
            this.tmpProgramInfo.setPesiChannelId(pesiChannelId);
            return this;
        }

        public String getPackageName() {
            return this.tmpProgramInfo.getPackageName();
        }

        public RecommendChannel.ProgramBuilder setPackageName(String packageName) {
            this.tmpProgramInfo.setPackageName(packageName);
            return this;
        }

        public int getDurationMillis() {
            return this.tmpProgramInfo.getDurationMillis();
        }

        public RecommendChannel.ProgramBuilder setDurationMillis(int durationMillis) {
            this.tmpProgramInfo.setDurationMillis(durationMillis);
            return this;
        }

        public boolean getLive() {
            return this.tmpProgramInfo.getLive();
        }

        public RecommendChannel.ProgramBuilder setLive(boolean live) {
            this.tmpProgramInfo.setLive(live);
            return this;
        }

        public String GetPosterArtUriDescription() {
            return this.tmpProgramInfo.getPosterArtUriDescription();
        }

        public RecommendChannel.ProgramBuilder setPosterArtUriDescription(String description) {
            this.tmpProgramInfo.setPosterArtUriDescription(description);
            return this;
        }

        public Uri getLogoUri() {
            return this.tmpProgramInfo.getLogoUri();
        }

        public RecommendChannel.ProgramBuilder setLogoUri(Uri logoUri) {
            this.tmpProgramInfo.logoUri = logoUri;
            return this;
        }

        public String getLogoUriDescription() {
            return this.tmpProgramInfo.getLogoUriDescription();
        }

        public RecommendChannel.ProgramBuilder setLogoUriDescription(String logoUriDescription) {
            this.tmpProgramInfo.logoUriDescription = logoUriDescription;
            return this;
        }

        public int getAvailabilityType() {
            return this.tmpProgramInfo.getAvailabilityType();
        }

        public RecommendChannel.ProgramBuilder setAvailabilityType(int availabilityType) {
            this.tmpProgramInfo.availabilityType = availabilityType;
            return this;
        }

        public String[] getCanonicalGenres() {
            return this.tmpProgramInfo.getCanonicalGenres();
        }

        public RecommendChannel.ProgramBuilder setCanonicalGenres(String[] canonicalGenres) {
            this.tmpProgramInfo.CanonicalGenres = canonicalGenres;
            return this;
        }

        public int getEpisodeNumber() {
            return this.tmpProgramInfo.getEpisodeNumber();
        }

        public RecommendChannel.ProgramBuilder setEpisodeNumber(int episodeNumber) {
            this.tmpProgramInfo.episodeNumber = episodeNumber;
            return this;
        }

        public String getEpisodeTitle() {
            return this.tmpProgramInfo.getEpisodeTitle();
        }

        public RecommendChannel.ProgramBuilder setEpisodeTitle(String episodeTitle) {
            this.tmpProgramInfo.episodeTitle = episodeTitle;
            return this;
        }

        public long getInteractionCount() {
            return this.tmpProgramInfo.getInteractionCount();
        }

        public RecommendChannel.ProgramBuilder setInteractionCount(long interactionCount) {
            this.tmpProgramInfo.interactionCount = interactionCount;
            return this;
        }

        public int getInteractionTpye() {
            return this.tmpProgramInfo.getInteractionTpye();
        }

        public RecommendChannel.ProgramBuilder setInteractionTpye(int interactionTpye) {
            this.tmpProgramInfo.interactionTpye = interactionTpye;
            return this;
        }

        public int getItemCount() {
            return this.tmpProgramInfo.getItemCount();
        }

        public RecommendChannel.ProgramBuilder setItemCount(int itemCount) {
            this.tmpProgramInfo.itemCount = itemCount;
            return this;
        }

        public String getInternalProviderId() {
            return this.tmpProgramInfo.getInternalProviderId();
        }

        public RecommendChannel.ProgramBuilder setInternalProviderId(String internalProviderId) {
            this.tmpProgramInfo.internalProviderId = internalProviderId;
            return this;
        }

        public String getOfferPrice() {
            return this.tmpProgramInfo.getOfferPrice();
        }

        public RecommendChannel.ProgramBuilder setOfferPrice(String offerPrice) {
            this.tmpProgramInfo.offerPrice = offerPrice;
            return this;
        }

        public Uri getPreviewVideoUri() {
            return this.tmpProgramInfo.getPreviewVideoUri();
        }

        public RecommendChannel.ProgramBuilder setPreviewVideoUri(Uri videoUri) {
            this.tmpProgramInfo.previewVideoUri = videoUri;
            return this;
        }

        public Uri getPreviewAudioUri() {
            return this.tmpProgramInfo.getPreviewAudioUri();
        }

        public RecommendChannel.ProgramBuilder setPreviewAudioUri(Uri audioUri) {
            this.tmpProgramInfo.previewAudioUri = audioUri;
            return this;
        }

        public String getReleaseDate() {
            return this.tmpProgramInfo.getReleaseDate();
        }

        public RecommendChannel.ProgramBuilder setReleaseDate(String releaseDate) {
            this.tmpProgramInfo.releaseDate = releaseDate;
            return this;
        }

        public String getReviewRating() {
            return this.tmpProgramInfo.getReviewRating();
        }

        public RecommendChannel.ProgramBuilder setReviewRating(String reviewRating) {
            this.tmpProgramInfo.reviewRating = reviewRating;
            return this;
        }

        public int getReviewRatingStyle() {
            return this.tmpProgramInfo.getReviewRatingStyle();
        }

        public RecommendChannel.ProgramBuilder setReviewRatingStyle(int reviewRatingStyle) {
            this.tmpProgramInfo.reviewRatingStyle = reviewRatingStyle;
            return this;
        }

        public int getSeasonNumber() {
            return this.tmpProgramInfo.getSeasonNumber();
        }

        public RecommendChannel.ProgramBuilder setSeasonNumber(int seasonNumber) {
            this.tmpProgramInfo.seasonNumber = seasonNumber;
            return this;
        }

        public String getStartingPrice() {
            return this.tmpProgramInfo.getStartingPrice();
        }

        public RecommendChannel.ProgramBuilder setStartingPrice(String startingPrice) {
            this.tmpProgramInfo.startingPrice = startingPrice;
            return this;
        }

        public int getThumbnailratio() {
            return this.tmpProgramInfo.getThumbnailratio();
        }

        public RecommendChannel.ProgramBuilder setThumbnailratio(int thumbnailratio) {
            this.tmpProgramInfo.thumbnailratio = thumbnailratio;
            return this;
        }

        public Uri getThumbnailUri() {
            return this.tmpProgramInfo.getThumbnailUri();
        }

        public RecommendChannel.ProgramBuilder setThumbnailUri(Uri thumbnailUri) {
            this.tmpProgramInfo.thumbnailUri = thumbnailUri;
            return this;
        }

        public int getVideoHeight() {
            return this.tmpProgramInfo.getVideoHeight();
        }

        public RecommendChannel.ProgramBuilder setVideoHeight(int videoHeight) {
            this.tmpProgramInfo.videoHeight = videoHeight;
            return this;
        }

        public int getVideoWidth() {
            return this.tmpProgramInfo.getVideoWidth();
        }

        public RecommendChannel.ProgramBuilder setVideoWidth(int videoWidth) {
            this.tmpProgramInfo.videoWidth = videoWidth;
            return this;
        }

        public int getWeight() {
            return this.tmpProgramInfo.getWeight();
        }

        public RecommendChannel.ProgramBuilder setWeight(int weight) {
            this.tmpProgramInfo.weight = weight;
            return this;
        }

        public int getUpdateType() {
            return this.tmpProgramInfo.getUpdateType();
        }

        public RecommendChannel.ProgramBuilder setUpdateType(int updateType) {
            this.tmpProgramInfo.updateType = updateType;
            return this;
        }

        public String getUpdateFilter() {
            return this.tmpProgramInfo.getUpdateFilter();
        }

        public RecommendChannel.ProgramBuilder setUpdateFilter(String updateFilter) {
            this.tmpProgramInfo.updateFilter = updateFilter;
            return this;
        }

        public int getPosterArtAspectRatio() {
            return this.tmpProgramInfo.getPosterArtAspectRatio();
        }

        public RecommendChannel.ProgramBuilder setPosterArtAspectRatio(int posterArtAspectRatio) {
            this.tmpProgramInfo.PosterArtAspectRatio = posterArtAspectRatio;
            return this;
        }

        public TvContentRating[] getContentRating() {
            return this.tmpProgramInfo.getContentRating();
        }

        public RecommendChannel.ProgramBuilder setContentRating(TvContentRating[] contentRating) {
            this.tmpProgramInfo.contentRating = contentRating;
            return this;
        }

        public boolean isSearchable() {
            return this.tmpProgramInfo.searchable;
        }

        public RecommendChannel.ProgramBuilder setSearchable(boolean searchable) {
            this.tmpProgramInfo.searchable = searchable;
            return this;
        }

        public List<TvProviderProgramInfo> build() {
            mProgramInfo = new TvProviderProgramInfo();
            if (mProgramInfo != tmpProgramInfo) {
                mProgramInfo.Type = tmpProgramInfo.Type;
                mProgramInfo.Title = tmpProgramInfo.Title;
                mProgramInfo.Description = tmpProgramInfo.Description;
                mProgramInfo.StartTime = tmpProgramInfo.StartTime;
                mProgramInfo.EndTime = tmpProgramInfo.EndTime;
                mProgramInfo.Author = tmpProgramInfo.Author;
                mProgramInfo.Intent = tmpProgramInfo.Intent;
                mProgramInfo.PackageName = tmpProgramInfo.PackageName;
                mProgramInfo.Genre = tmpProgramInfo.Genre;
                mProgramInfo.PosterArtUri = tmpProgramInfo.PosterArtUri;
                mProgramInfo.InternalProviderData = tmpProgramInfo.InternalProviderData;
                mProgramInfo.LastEnagementTimeUtcMills = tmpProgramInfo.LastEnagementTimeUtcMills;
                mProgramInfo.LastPlaybackPositionMillis = tmpProgramInfo.LastPlaybackPositionMillis;
                mProgramInfo.PesiChannelId = tmpProgramInfo.PesiChannelId;
                mProgramInfo.DurationMillis = tmpProgramInfo.DurationMillis;
                mProgramInfo.isLive = tmpProgramInfo.isLive;
                mProgramInfo.PosterArtUriDescription = tmpProgramInfo.PosterArtUriDescription;
                mProgramInfo.logoUri = tmpProgramInfo.logoUri;
                mProgramInfo.logoUriDescription = tmpProgramInfo.logoUriDescription;
                mProgramInfo.availabilityType = tmpProgramInfo.availabilityType;
                mProgramInfo.CanonicalGenres = tmpProgramInfo.CanonicalGenres;
                mProgramInfo.episodeNumber = tmpProgramInfo.episodeNumber;
                mProgramInfo.episodeTitle = tmpProgramInfo.episodeTitle;
                mProgramInfo.interactionCount = tmpProgramInfo.interactionCount;
                mProgramInfo.interactionTpye = tmpProgramInfo.interactionTpye;
                mProgramInfo.itemCount = tmpProgramInfo.itemCount;
                mProgramInfo.internalProviderId = tmpProgramInfo.internalProviderId;
                mProgramInfo.PosterArtAspectRatio = tmpProgramInfo.PosterArtAspectRatio;
                mProgramInfo.offerPrice = tmpProgramInfo.offerPrice;
                mProgramInfo.previewVideoUri = tmpProgramInfo.previewVideoUri;
                mProgramInfo.previewAudioUri = tmpProgramInfo.previewAudioUri;
                mProgramInfo.releaseDate = tmpProgramInfo.releaseDate;
                mProgramInfo.reviewRating = tmpProgramInfo.reviewRating;
                mProgramInfo.reviewRatingStyle = tmpProgramInfo.reviewRatingStyle;
                mProgramInfo.seasonNumber = tmpProgramInfo.seasonNumber;
                mProgramInfo.startingPrice = tmpProgramInfo.startingPrice;
                mProgramInfo.thumbnailratio = tmpProgramInfo.thumbnailratio;
                mProgramInfo.thumbnailUri = tmpProgramInfo.thumbnailUri;
                mProgramInfo.videoHeight = tmpProgramInfo.videoHeight;
                mProgramInfo.videoWidth = tmpProgramInfo.videoWidth;
                mProgramInfo.weight = tmpProgramInfo.weight;
                mProgramInfo.contentRating = tmpProgramInfo.contentRating;
                mProgramInfo.searchable = tmpProgramInfo.searchable;
                mProgramInfo.updateType = tmpProgramInfo.updateType;
                mProgramInfo.updateFilter = tmpProgramInfo.updateFilter;
                mProgramInfoList.add(mProgramInfo);
            }
            return mProgramInfoList;
        }
    }

    public class TvProviderProgramInfo{
        private int Type;
        private String Title;
        private String Description;
        private long StartTime;
        private long EndTime;
        private String Author;
        private android.content.Intent Intent;
        private String PackageName;
        private String Genre;
        private Uri PosterArtUri;
        private String PosterArtUriDescription;
        private String InternalProviderData;
        private long LastEnagementTimeUtcMills;//Watch Next Program
        private int LastPlaybackPositionMillis;//Watch Next Program
        private long PesiChannelId;
        private int DurationMillis;
        private boolean isLive;
        private Uri logoUri;
        private String logoUriDescription;
        private int availabilityType;
        private String[] CanonicalGenres ;
        private int episodeNumber;
        private String episodeTitle;
        private long interactionCount;
        private int interactionTpye;
        private int itemCount;
        private String internalProviderId;
        private String offerPrice;
        private int PosterArtAspectRatio;
        private Uri previewVideoUri;
        private Uri previewAudioUri;
        private String releaseDate;
        private String reviewRating;
        private int reviewRatingStyle;
        private int seasonNumber;
        private String startingPrice;
        private int thumbnailratio;
        private Uri thumbnailUri;
        private int videoHeight;
        private int videoWidth;
        private int weight;
        private TvContentRating[] contentRating;
        private boolean searchable;



        //Update Filter
        private int updateType = -1;
        private String updateFilter = "";

        public int getType() {
            return Type;
        }

        public void setType(int type) {
            Type = type;
        }

        public String getTitle() {
            return Title;
        }

        public void setTitle(String title) {
            Title = title;
        }

        public String getDescription() {
            return Description;
        }

        public void setDescription(String description) {
            Description = description;
        }

        public long getStartTime() {
            return StartTime;
        }

        public void setStartTime(long startTime) {
            StartTime = startTime;
        }

        public long getEndTime() {
            return EndTime;
        }

        public void setEndTime(long endTime) {
            EndTime = endTime;
        }

        public String getAuthor() {
            return Author;
        }

        public void setAuthor(String author) {
            Author = author;
        }

        public android.content.Intent getIntent() {
            return Intent;
        }

        public void setIntent(android.content.Intent intent) {
            Intent = intent;
        }

        public String getGenre() {
            return Genre;
        }

        public void setGenre(String genre) {
            Genre = genre;
        }

        public Uri getPosterArtUri() {
            return PosterArtUri;
        }

        public void setPosterArtUri(Uri posterArtUri) {
            PosterArtUri = posterArtUri;
        }

        public String getInternalProviderData() {
            return InternalProviderData;
        }

        public void setInternalProviderData(String internalProviderData) {
            InternalProviderData = internalProviderData;
        }

        public long getLastEngagementTimeUtcMills() {
            return LastEnagementTimeUtcMills;
        }

        public void setLastEngagementTimeUtcMillis(long lastEnagementTimeUtcMills) {
            LastEnagementTimeUtcMills = lastEnagementTimeUtcMills;
        }

        public int getLastPlaybackPositionMillis() {
            return LastPlaybackPositionMillis;
        }

        public void setLastPlaybackPositionMillis(int lastPlaybackPositionMillis) {
            LastPlaybackPositionMillis = lastPlaybackPositionMillis;
        }

        public long getPesiChannelId() {
            return PesiChannelId;
        }

        public void setPesiChannelId(long pesiChannelId) {
            PesiChannelId = pesiChannelId;
        }

        public String getPackageName() {
            return PackageName;
        }

        public void setPackageName(String packageName) {
            PackageName = packageName;
        }

        public int getDurationMillis() {
            return DurationMillis;
        }

        public void setDurationMillis(int durationMillis) {
            DurationMillis = durationMillis;
        }

        public String getPosterArtUriDescription() {
            return PosterArtUriDescription;
        }

        public void setPosterArtUriDescription(String posterArtUriDescription) {
            PosterArtUriDescription = posterArtUriDescription;
        }

        public void setLive(boolean live)
        {
            isLive = live;
        }

        public boolean getLive() {
            return isLive;
        }

        public Uri getLogoUri() {
            return logoUri;
        }

        public void setLogoUri(Uri logoUri) {
            this.logoUri = logoUri;
        }

        public String getLogoUriDescription() {
            return logoUriDescription;
        }

        public void setLogoUriDescription(String logoUriDescription) {
            this.logoUriDescription = logoUriDescription;
        }

        public int getAvailabilityType() {
            return availabilityType;
        }

        public void setAvailabilityType(int availabilityType) {
            this.availabilityType = availabilityType;
        }

        public String[] getCanonicalGenres() {
            return CanonicalGenres;
        }

        public void setCanonicalGenres(String[] canonicalGenres) {
            CanonicalGenres = canonicalGenres;
        }

        public int getEpisodeNumber() {
            return episodeNumber;
        }

        public void setEpisodeNumber(int episodeNumber) {
            this.episodeNumber = episodeNumber;
        }

        public String getEpisodeTitle() {
            return episodeTitle;
        }

        public void setEpisodeTitle(String episodeTitle) {
            this.episodeTitle = episodeTitle;
        }

        public long getInteractionCount() {
            return interactionCount;
        }

        public void setInteractionCount(long interactionCount) {
            this.interactionCount = interactionCount;
        }

        public int getInteractionTpye() {
            return interactionTpye;
        }

        public void setInteractionTpye(int interactionTpye) {
            this.interactionTpye = interactionTpye;
        }

        public int getItemCount() {
            return itemCount;
        }

        public void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        public String getInternalProviderId() {
            return internalProviderId;
        }

        public void setInternalProviderId(String internalProviderId) {
            this.internalProviderId = internalProviderId;
        }

        public String getOfferPrice() {
            return offerPrice;
        }

        public void setOfferPrice(String offerPrice) {
            this.offerPrice = offerPrice;
        }

        public Uri getPreviewVideoUri() {
            return previewVideoUri;
        }

        public void setPreviewVideoUri(Uri videoUri) {
            this.previewVideoUri = videoUri;
        }

        public Uri getPreviewAudioUri() {
            return previewAudioUri;
        }

        public void setPreviewAudioUri(Uri audioUri) {
            this.previewAudioUri = audioUri;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getReviewRating() {
            return reviewRating;
        }

        public void setReviewRating(String reviewRating) {
            this.reviewRating = reviewRating;
        }

        public int getReviewRatingStyle() {
            return reviewRatingStyle;
        }

        public void setReviewRatingStyle(int reviewRatingStyle) {
            this.reviewRatingStyle = reviewRatingStyle;
        }

        public int getSeasonNumber() {
            return seasonNumber;
        }

        public void setSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
        }

        public String getStartingPrice() {
            return startingPrice;
        }

        public void setStartingPrice(String startingPrice) {
            this.startingPrice = startingPrice;
        }

        public int getThumbnailratio() {
            return thumbnailratio;
        }

        public void setThumbnailratio(int thumbnailratio) {
            this.thumbnailratio = thumbnailratio;
        }

        public Uri getThumbnailUri() {
            return thumbnailUri;
        }

        public void setThumbnailUri(Uri thumbnailUri) {
            this.thumbnailUri = thumbnailUri;
        }

        public int getVideoHeight() {
            return videoHeight;
        }

        public void setVideoHeight(int videoHeight) {
            this.videoHeight = videoHeight;
        }

        public int getVideoWidth() {
            return videoWidth;
        }

        public void setVideoWidth(int videoWidth) {
            this.videoWidth = videoWidth;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getUpdateType() {
            return updateType;
        }

        public void setUpdateType(int updateType) {
            this.updateType = updateType;
        }

        public String getUpdateFilter() {
            return updateFilter;
        }

        public void setUpdateFilter(String updateFilter) {
            this.updateFilter = updateFilter;
        }

        public int getPosterArtAspectRatio() {
            return PosterArtAspectRatio;
        }

        public void setPosterArtAspectRatio(int posterArtAspectRatio) {
            PosterArtAspectRatio = posterArtAspectRatio;
        }

        public TvContentRating[] getContentRating() {
            return contentRating;
        }

        public void setContentRating(TvContentRating[] contentRating) {
            this.contentRating = contentRating;
        }

        public boolean isSearchable() {
            return searchable;
        }

        public void setSearchable(boolean searchable) {
            this.searchable = searchable;
        }

    }

    public class TvProviderInfo{
        private Uri channelUri;
        private List<Uri> programUriList;

        public Uri getChannelUri() {
            return channelUri;
        }

        private void setChannelUri(Uri channelUri) {
            this.channelUri = channelUri;
        }

        public List<Uri> getProgramUriList() {
            return programUriList;
        }

        private void setProgramUriList(List<Uri> programUriList) {
            this.programUriList = programUriList;
        }

    }

    /**
     * @param tmpUri ChannelUri
     * @param channelInfo Channel Info
     * @return New channelUri or Exist channelUri
     */
    private Uri GetProviderChannelUri(Uri tmpUri, TvProviderChannelInfo channelInfo)
    {
        Uri channelUri;
        if (tmpUri == null || tmpUri.toString().equals("null")) {//Recently Watched Channel is already exist, not need to create new one
            channelUri = BuildTvProviderChannel(channelInfo);
        }
        else {
            //If Clear Data from TvProvider, shall delete Preview Program and Create New Channel Uri
            Cursor tmpProgramCursor = GetExistProgramCursor(tmpUri);
            DeletePreviewProgramInChannelbyPackageName(tmpUri, channelInfo.getPackageName());
            if (tmpProgramCursor == null || tmpProgramCursor.getCount() == 0) {
                channelUri = BuildTvProviderChannel(channelInfo);
            } else {
                channelUri = tmpUri;//Channel Uri already exist
            }
        }

        return channelUri;
    }

    /**
     * Add New Channel
     * @param channelInfo
     * @return New Channel Uri
     */
    private Uri BuildTvProviderChannel(TvProviderChannelInfo channelInfo)
    {
        Channel channel = new Channel.Builder()
                .setDisplayName(channelInfo.getDisplayName())
                .setDescription(channelInfo.getDescription())
                .setDisplayNumber(channelInfo.getDisplayNumber())
                //.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setType(channelInfo.getType())
                .setPackageName(channelInfo.getPackageName())
                .setInputId(channelInfo.getInputId())
                .setOriginalNetworkId(channelInfo.getOriginalNetworkId())
                //.setChannelLogo(channelInfo.getChannelLogo())
                .setInternalProviderData(channelInfo.getInternalProviderData())//qeury
                .setAppLinkIntent(channelInfo.getAppintent())
                .setSearchable(channelInfo.getSearchable())
                // Set more attributes...
                .build();

        Log.d(TAG, "BuildTvProviderChannel: displayName = " + channelInfo.getDisplayName());
        Uri channelUri = mContext.getContentResolver().insert(TvContractCompat.Channels.CONTENT_URI, channel.toContentValues());

        String str_CH = channel.toString();
        Log.d(TAG, "BuildTvProviderChannel:  channelUri =" + channelUri);
        Log.d(TAG, "BuildTvProviderChannel:  str_CH =" + str_CH);
        return channelUri ;
    }

    /**
     * Set Channel Logo
     * @param channelUri
     * @param logoUri
     */
    private void BuildTvProiderChannelLogo(Uri channelUri,Uri logoUri)
    {
        //Channel Logo
        long UriId = ContentUris.parseId(channelUri);
        Bitmap bm = BitmapFactory.decodeResource( mContext.getResources(), R.drawable.cablevision);
        ChannelLogoUtils.storeChannelLogo(mContext, UriId, bm);
    }

    /**
     * Add New Programs
     * @param channelUri
     * @param programInfo
     * @return Prgogram Uri
     */
    private Uri BuildTvProviderProgram(Uri channelUri, TvProviderProgramInfo programInfo)
    {

        PreviewProgram previewProgram = SetProgramInfoValues(channelUri,programInfo);
        Uri previewProgramUri = mContext.getContentResolver().insert(TvContractCompat.PreviewPrograms.CONTENT_URI, previewProgram.toContentValues());

        String strPR = previewProgram.toString();
        Log.d(TAG, "===================================================");
        Log.d(TAG, "BuildTvProviderProgram: tvPorviderChannelID =" + ContentUris.parseId(channelUri) +" \npreviewProgramUri = " + previewProgramUri +"]");
        Log.d(TAG, "BuildTvProviderProgram: strPR =" + strPR);

        return previewProgramUri;
    }

    /**
     * Add New Channel and Programs
     * @return program size > 0 : Success
     * program size <= 0 : Fail
     */
    public int Add()
    {
        TvProviderInfo tvproviderInfo = new TvProviderInfo();
        List<Uri> ProgramUriList = new ArrayList<>();
        Uri channelUri,programUri;
        Log.d(TAG, "Add(): list size = [" + mProgramInfoList.size() +"]");
        int size = 0;

        if(mChannelInfo != null) {
            //Build New Channel Row
            channelUri = BuildTvProviderChannel(mChannelInfo);
            tvproviderInfo.setChannelUri(channelUri);

            //channel logo
            BuildTvProiderChannelLogo(channelUri, Uri.parse(mChannelInfo.getChannelLogo()));

            Log.d(TAG, "Add default Channel: Id = [" + ContentUris.parseId(channelUri) +"]");
            TvContractCompat.requestChannelBrowsable(mContext, ContentUris.parseId(channelUri));
        }else {
            Log.d(TAG, "Add(): ChannelInfo is null !");
            return size;
        }

        if (mProgramInfoList != null && mProgramInfoList.size() > 0) {
            for (int i = 0; i < mProgramInfoList.size(); i++) {
                programUri = BuildTvProviderProgram(channelUri, mProgramInfoList.get(i));
                ProgramUriList.add(programUri);
            }
            size = ProgramUriList.size();
        }

        tvproviderInfo.setProgramUriList(ProgramUriList);
        setUriInfo(tvproviderInfo);//Set UriInfo for Caller
        return size;
    }

    /**
     * Rebuild Programs by exist channel Uri
     * @param channelUri
     * @return program size > 0 : Success
     * program size < 0 :Fail
     */
    public int RebuildPrograms(Uri channelUri)
    {
        TvProviderInfo tvproviderInfo = new TvProviderInfo();
        List<Uri> ProgramUriList = new ArrayList<>();
        Uri programUri;
        Log.d(TAG, "RebuildPrograms(): list size = [" + mProgramInfoList.size() +"] uri = ["+channelUri+"]");
        int size = 0;

        if(mChannelInfo != null) {
            //Build Channel Row
            //channelUri = GetProviderChannelUri(channeluri, mChannelInfo);
            Cursor tmpProgramCursor = GetExistProgramCursor(channelUri);
            DeletePreviewProgramInChannelbyPackageName(channelUri, mChannelInfo.getPackageName());
            if (tmpProgramCursor == null || tmpProgramCursor.getCount() == 0) {
                channelUri = BuildTvProviderChannel(mChannelInfo);
            }
            tvproviderInfo.setChannelUri(channelUri);

            //channel logo
            BuildTvProiderChannelLogo(channelUri, Uri.parse(mChannelInfo.getChannelLogo()));

            Log.d(TAG, "RebuildPrograms default Channel: Id = [" + ContentUris.parseId(channelUri) +"]");
            TvContractCompat.requestChannelBrowsable(mContext, ContentUris.parseId(channelUri));
        }else {
            Log.d(TAG, "RebuildPrograms(): ChannelInfo is null !");
            return size;
        }

        if (mProgramInfoList != null && mProgramInfoList.size() > 0) {
            for (int i = 0; i < mProgramInfoList.size(); i++) {
                programUri = BuildTvProviderProgram(channelUri, mProgramInfoList.get(i));
                ProgramUriList.add(programUri);
            }
            size = ProgramUriList.size();
        }

        tvproviderInfo.setProgramUriList(ProgramUriList);
        setUriInfo(tvproviderInfo);//Set UriInfo for Caller
        return size;
    }

    /**
     * Update Channel Info
     * @param ChannelUri
     * @return
     * SUCCESS : ret > 0
     * FAIL : ret <= 0
     */
    public int UpdateChannel(Uri ChannelUri)
    {
        int ret = 0;
        //Update Channel Info
        if(mChannelInfo != null) {
            Channel channel = new Channel.Builder()
                    .setDisplayName(mChannelInfo.getDisplayName())
                    .setDescription(mChannelInfo.getDescription())
                    .setDisplayNumber(mChannelInfo.getDisplayNumber())
                    .setType(mChannelInfo.getType())
                    .setPackageName(mChannelInfo.getPackageName())
                    .setInputId(mChannelInfo.getInputId())
                    .setOriginalNetworkId(mChannelInfo.getOriginalNetworkId())
                    .setChannelLogo(mChannelInfo.getChannelLogo())
                    .setInternalProviderData(mChannelInfo.getInternalProviderData().getBytes())//qeury
                    .setSearchable(mChannelInfo.getSearchable())
                    .setAppLinkIntent(mChannelInfo.getAppintent())
                    // Set more attributes...
                    .build();

            //Log.d(TAG, "UpdateChannel: displayName = " + mChannelInfo.getDisplayName());
            ret = UpdateChannelValuesbyUri(ChannelUri, channel);
            Log.d(TAG, "UpdateChannel: ret = [" + ret +"]");
        }

        return ret;

    }

    /**
     * Update Programs by Channel Uri
     * @param ChannelUri
     * @return size > 0 : Success
     * size <= 0 : FAIL
     */
    public int UpdatePrograms(Uri ChannelUri)
    {
        int size = 0;
        Log.d(TAG, "UpdatePrograms: size = [" + mProgramInfoList.size()+"]");
        //Update Program Info
        if(mProgramInfoList != null && mProgramInfoList.size() > 0)
        {
            for(int i = 0 ; i < mProgramInfoList.size() ; i++) {
                int tmp_program_ret = UpdateProgramValuesByFilter(ChannelUri,mProgramInfoList.get(i).getUpdateType(),mProgramInfoList.get(i).getUpdateFilter(),mProgramInfoList.get(i));
                Log.d(TAG, "UpdatePrograms: " + mProgramInfoList.get(i).getPesiChannelId() + " filterType = ["
                        + mProgramInfoList.get(i).getUpdateType()
                        +"] filter = [" + mProgramInfoList.get(i).getUpdateFilter()
                        +"] tmp_program_ret = ["+ tmp_program_ret +"]");
                if(tmp_program_ret > 0)
                    size++;
            }
        }else{
            Log.d(TAG, "UpdatePrograms Please Check ProgramList exists");
        }

        Log.d(TAG, "UpdatePrograms: programs size = [" + size + "]");
        return size;
    }

    /**
     * Update Channel Info
     * @param ChannelUri
     * @param channel
     * @return ret > 0 : SUCCESS
     * ret >= 0 : FAIL
     */
    private int UpdateChannelValuesbyUri(Uri ChannelUri, Channel channel)
    {
        int ret = 0;
        if(ChannelUri.toString().equals("null"))
        {
            Log.d(TAG, "UpdateChannelValuesbyUri: no Channel Uri Exist!!");
            return ret;
        }else
        {
            Cursor chCursor = getChannelCursorbyUri(ChannelUri);
            Log.d(TAG, "UpdateChannelValuesbyUri: chCursor count = [" + chCursor.getCount() +"]");
            if(chCursor != null && chCursor.getCount() != 0)
            {
                String mSelectionClause = TvContractCompat.Channels.COLUMN_TYPE + " = ? AND " +
                        TvContractCompat.Channels.COLUMN_PACKAGE_NAME + " = ? ";
                String[] mSelectionArgs = {TvContract.Channels.TYPE_PREVIEW,
                        PACKAGE_NAME};

                chCursor.moveToNext();
                ret = mContext.getContentResolver().update(ChannelUri,channel.toContentValues(),mSelectionClause, mSelectionArgs);
                Log.d(TAG, "UpdateChannelValuesbyUri: ret = [" + ret +"]");
            }
            return ret;
        }
    }

    /**
     * @param ChannelUri
     * @param updateType 0:Update by PesiChannelId; 1: Update by Program Uri
     * @param updateFilter String
     * @param programInfo Update PrgogramInfo
     * @return Update or Add Program, size should > 0
     * ret = 0 , FAIL, No Update or Add
     * ret = 1 , Success
     */
    private int UpdateProgramValuesByFilter(Uri ChannelUri, int updateType, String updateFilter, TvProviderProgramInfo programInfo)
    {
        Log.d(TAG, "UpdateProgramValuesByFilter: updateFilter = [" + updateFilter +"] Genre = [" + programInfo.getGenre() +"] Package Name = [" + programInfo.getPackageName() +"]");
        int ret = 0;
        if(ChannelUri.toString().equals("null"))
        {
            Log.d(TAG, "UpdateProgramValuesByFilter: no Channel Uri Exist!!");
            return ret;
        }else {
            Cursor ProgramCursor = GetExistProgramCursor(ChannelUri);
            if (ProgramCursor != null && ProgramCursor.getCount() != 0) {
                Log.d(TAG, "UpdateProgramValuesByFilter: programCursor count = [" + ProgramCursor.getCount() +"]");
                boolean isUpdateProgramExist = false;
                while(ProgramCursor.moveToNext())
                {
                    Log.d(TAG, "UpdateProgramValuesByFilter: programCursor String = [" + ProgramCursor.toString() +"] id = [" + ProgramCursor.getLong(0)+"]");
                    boolean doUpdate = false;
                    PrintPreviewProgramCursor(ProgramCursor);
                    long id = ProgramCursor.getLong(0);
                    String intentString = ProgramCursor.getString(3);
                    long PesiChannelId = GetPesiChannelIdfromIntent(intentString);
                    String GenrefromCursor = ProgramCursor.getString(5);
                    String packagNamefromCursor =  ProgramCursor.getString(6);
                    Log.d(TAG, "UpdateProgramValuesByFilter: intentString = [" + intentString +"] PesiChannelId = [" + PesiChannelId +"] packagNamefromCursor = [" + packagNamefromCursor +"] GenrefromCursor = ["
                            + GenrefromCursor +"]");

                    if((updateType == 0 && (String.valueOf(PesiChannelId).equals(updateFilter)) //Update by PesiChannelId
                            || (updateType == 1 && TvContractCompat.buildPreviewProgramUri(id).equals(Uri.parse(updateFilter))) //Update by ProgramUri
                    )) {
                        doUpdate = true;
                        isUpdateProgramExist = true;
                    }

                    Log.d(TAG, "UpdateProgramValuesByFilter:  doUpdate = [" + doUpdate +"]");
                    if(doUpdate){
                        String mSelectionClause = TvContractCompat.PreviewPrograms._ID + " = ? ";
                        String[] mSelectionArgs = {String.valueOf(id)};

                        PreviewProgram previewProgram = SetProgramInfoValues(ChannelUri,programInfo);
                        ret = mContext.getContentResolver().update(TvContractCompat.buildPreviewProgramUri(id), previewProgram.toContentValues(), mSelectionClause, mSelectionArgs);

                        break;//already find the program, do next update
                    }

                    Log.d(TAG, "\n\nUpdateProgramValuesByFilter: => [" + TvContractCompat.buildPreviewProgramUri(id).toString() +"]\n\n");
                }
                //If Update Program Not Exist, Add new Program
                if(!isUpdateProgramExist)
                {
                    PreviewProgram previewProgram = SetProgramInfoValues(ChannelUri,programInfo);
                    Uri ProgramUri = mContext.getContentResolver().insert(TvContractCompat.PreviewPrograms.CONTENT_URI, previewProgram.toContentValues());
                    if(ProgramUri != null)
                        ret = 1;
                }
            }

        }
        return ret;
    }

    /**
     * @return Size
     * watchNextProgramUri is Not null return 1 : ADD SUCCESS
     * watchNextProgramUri is null return 0 : ADD FAIL
     */
    public int AddWatchNextProgram()
    {
        TvProviderProgramInfo watchNextProgramInfo = mProgramInfoList.get(0);
        //Delete Previous Watch Next Program
        DeleteWatchNextProgram();

        //Add New Watch Next Program
        WatchNextProgram.Builder builder = new WatchNextProgram.Builder();
        builder.setType(watchNextProgramInfo.getType())
                .setWatchNextType(TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE)
                .setTitle(watchNextProgramInfo.getTitle())
                .setDescription(watchNextProgramInfo.getDescription())
                .setStartTimeUtcMillis(watchNextProgramInfo.getStartTime())
                .setEndTimeUtcMillis(watchNextProgramInfo.getEndTime())
                .setAuthor(watchNextProgramInfo.getAuthor())
                .setIntent(watchNextProgramInfo.getIntent())
                .setGenre(watchNextProgramInfo.getGenre())
                .setPosterArtUri(watchNextProgramInfo.getPosterArtUri())
                .setDurationMillis(watchNextProgramInfo.getDurationMillis())
                .setLastEngagementTimeUtcMillis(watchNextProgramInfo.getLastEngagementTimeUtcMills())
                .setLastPlaybackPositionMillis(watchNextProgramInfo.getLastPlaybackPositionMillis())
                .setInternalProviderData(watchNextProgramInfo.getInternalProviderData().getBytes());

        Uri watchNextProgramUri = mContext.getContentResolver()
                .insert(TvContractCompat.WatchNextPrograms.CONTENT_URI, builder.build().toContentValues());

        Log.d(TAG, "AddWatchNextProgram: watchNextProgramUri = [" + watchNextProgramUri + "]");

        if(watchNextProgramUri != null)
            return 1;//one Watch Next Program
        else
            return 0;
        //return watchNextProgramUri;
    }

    //Print Channel Informaiton from Cursor
    private void PrintChannelCursor(Cursor channelCursor)
    {
        Log.d(TAG, "PrintChannelCursor: channelCursor \nId = [" + channelCursor.getLong(0)
                + "]\n displayName = ["
                + channelCursor.getString(1)
                + "]\n package Name = ["
                + channelCursor.getString(2)
                + "]\n intent = ["
                + channelCursor.getString(3)
                + "]\n display number = ["
                + channelCursor.getString(4)
                + "]\n input id = ["
                + channelCursor.getInt(5)
                + "]\n Description = ["
                + channelCursor.getString(6)
                + "]\n INTERNAL_PROVIDER_DATA = ["
                +  channelCursor.getBlob(7)
                + "]\n");

        if(channelCursor.getBlob(7) != null)
        {
            String string = new String(channelCursor.getBlob(7));
            Log.d(TAG, "\nPrintChannelCursor: private_data to string = [" + string + "]\n\n");
        }
    }

    //Print Preview Program Informaiton from Cursor
    private void PrintPreviewProgramCursor(Cursor programCursor)
    {
        Log.d(TAG, "PrintPreviewProgramCursor: programCursor \n" +
                " Id = [" + programCursor.getLong(0)
                + "]\n Title = ["
                + programCursor.getString(1)
                + "]\n description = ["
                + programCursor.getString(2)
                + "]\n intent = ["
                + programCursor.getString(3)
                + "]\n posture = ["
                + programCursor.getString(4)
                + "]\n Genre = ["
                + programCursor.getString(5)
                + "]\n PackageName = ["
                + programCursor.getString(6)
                + "]\n ChannelId = ["
                + programCursor.getLong(7)
                + "]\n Author = ["
                + programCursor.getString(8)
                + "]\n INTERNAL_PROVIDER_DATA = ["
                + programCursor.getBlob(9)
                + "]\n START_TIME_UTC_MILLIS = ["
                + programCursor.getLong(10)
                + "]\n END_TIME_UTC_MILLIS = ["
                + programCursor.getLong(11)
                + "]\n DURATION_MILLIS = ["
                + programCursor.getInt(12)
                + "]");


        if(programCursor.getBlob(9) != null) {
            String string = new String(programCursor.getBlob(9));
            Log.d(TAG, "\nPrintPreviewProgramCursor: private_data to string = [" + string + "]\n\n");
        }

        PreviewProgram previewProgram = PreviewProgram.fromCursor(programCursor);
        Log.d(TAG, "PrintPreviewProgramCursor: [ to String \n[" + previewProgram.toString()+ "]");
    }

    /**
     * Get Exist Program Cursor to check program info
     * @param ChannelUri
     * @return Program Cursor
     */
    private Cursor GetExistProgramCursor(Uri ChannelUri)
    {
        Cursor tmpCursor = getChannelCursorbyUri(ChannelUri);
        Cursor tmpProgramCursor = null;
        if(tmpCursor != null && tmpCursor.getCount() != 0)
        {
            tmpCursor.moveToNext();
            tmpProgramCursor = getRecommendProgram(tmpCursor.getLong(0));
        }
        return tmpProgramCursor;
    }

    /**
     * Get Channel Cursor from DataBase
     * @param channelUri
     * @return Cursor
     */
    private Cursor getChannelCursorbyUri(Uri channelUri)
    {
        String CHANNEL_SELECTION = TvContractCompat.Channels.COLUMN_TYPE + "='" +
                TvContractCompat.Channels.TYPE_PREVIEW + "'";
        // Other column names can be found in TvContractCompat.Channels
        String[] CHANNEL_PROJECTION = {
                TvContractCompat.Channels._ID,
                TvContractCompat.Channels.COLUMN_DISPLAY_NAME,
                TvContractCompat.Channels.COLUMN_PACKAGE_NAME,
                TvContractCompat.Channels.COLUMN_APP_LINK_INTENT_URI,
                TvContractCompat.Channels.COLUMN_DISPLAY_NUMBER,
                TvContractCompat.Channels.COLUMN_INPUT_ID,
                TvContractCompat.Channels.COLUMN_DESCRIPTION,
                TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_DATA,
                TvContractCompat.Channels.COLUMN_TYPE,
                TvContractCompat.Channels.COLUMN_SEARCHABLE,

        };

        Cursor channelCursor = mContext.getContentResolver().query(
                channelUri,
                CHANNEL_PROJECTION,
                CHANNEL_SELECTION,
                null, null);

        return channelCursor ;
    }

    /**
     * Query Programs by ChannelId
     * @param channelId
     * @return Program Cursor
     */
    private Cursor getRecommendProgram(long channelId)
    {
        String PROGRAMS_SELECTION =
                TvContractCompat.PreviewPrograms.COLUMN_CHANNEL_ID + "=? AND " +
                        TvContractCompat.PreviewPrograms.COLUMN_BROWSABLE + "=1";

        String[] PROGRAMS_PROJECTION = {
                TvContractCompat.PreviewPrograms._ID,
                TvContractCompat.PreviewPrograms.COLUMN_TITLE,
                TvContractCompat.PreviewPrograms.COLUMN_SHORT_DESCRIPTION,
                TvContractCompat.PreviewPrograms.COLUMN_INTENT_URI,
                TvContractCompat.PreviewPrograms.COLUMN_POSTER_ART_URI,
                TvContractCompat.PreviewPrograms.COLUMN_GENRE,
                TvContractCompat.PreviewPrograms.COLUMN_PACKAGE_NAME,
                TvContractCompat.PreviewPrograms.COLUMN_CHANNEL_ID,
                TvContractCompat.PreviewPrograms.COLUMN_AUTHOR,
                TvContractCompat.PreviewPrograms.COLUMN_INTERNAL_PROVIDER_DATA,
                TvContractCompat.PreviewPrograms.COLUMN_START_TIME_UTC_MILLIS,
                TvContractCompat.PreviewPrograms.COLUMN_END_TIME_UTC_MILLIS,
                TvContractCompat.PreviewPrograms.COLUMN_DURATION_MILLIS

        };

        String[] SELECTION_ARGS = { String.valueOf(channelId) };
        Cursor programCursor = mContext.getContentResolver().query(
                TvContractCompat.PreviewPrograms.CONTENT_URI,
                PROGRAMS_PROJECTION,
                PROGRAMS_SELECTION,
                SELECTION_ARGS,
                null);

        return programCursor ;
    }

    /**
     * @return ret = 1 : Delete Watch Next Program SUCCESS
     * ret = 0 : Delete Watch Next Program FAIL
     */
    //Delete Watch Next Continue Program
    public int DeleteWatchNextProgram()
    {
        Log.d(TAG, "DeleteWatchNextProgram: IN");
        int ret = 0;
        Cursor watchNextContinueCursor = GetWatchNextContinueCursor();
        Log.d(TAG, "DeleteWatchNextProgram: Count = [" + watchNextContinueCursor.getCount()+"]");
        if (watchNextContinueCursor != null && watchNextContinueCursor.getCount() != 0) {
            while (watchNextContinueCursor.moveToNext()) {
                long id = watchNextContinueCursor.getLong(0);
                String packageName = watchNextContinueCursor.getString(1);
                Log.d(TAG, "DeleteWatchNextProgram: id = [" + id +"] \n [" + watchNextContinueCursor.toString() +"] packageName = [" + packageName +"]");
                if(packageName.equals(PACKAGE_NAME)) {
                    ret = mContext.getContentResolver().delete(TvContractCompat.buildWatchNextProgramUri(id), null, null);
                }
            }
        }
        return ret;
    }

    /**
     * Get Watch Next Program Cursor
     * @return Watch Next Cursor
     */
    private Cursor GetWatchNextContinueCursor()
    {
        String CHANNEL_SELECTION = TvContractCompat.WatchNextPrograms.COLUMN_WATCH_NEXT_TYPE + "='" +
                TvContractCompat.WatchNextPrograms.WATCH_NEXT_TYPE_CONTINUE + "'";

        String[] CHANNEL_PROJECTION = {
                TvContractCompat.WatchNextPrograms._ID,
                TvContractCompat.WatchNextPrograms.COLUMN_PACKAGE_NAME
        };

        Cursor wachNextCursor = mContext.getContentResolver().query(
                TvContractCompat.WatchNextPrograms.CONTENT_URI,
                CHANNEL_PROJECTION,
                CHANNEL_SELECTION,
                null, null);

        return wachNextCursor ;
    }

    /**
     * Get PesiChannelId from Intent String
     * @param intentString from DataBase
     * @return PesiChanenlId
     */
    private long GetPesiChannelIdfromIntent(String intentString)
    {
        long PesiChannelId = 0;
        Log.d(TAG, "GetPesiChannelIdfromIntent: intentString = [" + intentString +"]");
        String[] channelIdSplitString;
        String[] SplitStr = intentString.split(";");
        for(int i = 0 ; i < SplitStr.length ; i++)
            Log.d(TAG, "GetPesiChannelIdfromIntent: SplitStr["+i+"] = " + SplitStr[i]);

        if(SplitStr.length >= 4)
        {
            channelIdSplitString = SplitStr[3].split("=");
            Log.d(TAG, "GetPesiChannelIdfromIntent: length = " + channelIdSplitString.length);
            if (channelIdSplitString.length > 1) {
                PesiChannelId = Long.parseLong(channelIdSplitString[1]);
                //Log.d(TAG, "GetPesiChannelIdfromIntent:  channelIdSplitString [0]" + channelIdSplitString[0]+"] [1]" + channelIdSplitString[1]);
            }
        }
        Log.d(TAG, "GetPesiChannelIdfromIntent: PesiChannelId = [" + PesiChannelId+"]");
        return PesiChannelId;
    }

    /**
     * Delete All programs by Package Name
     * @param ChannelUri
     * @param packageName
     */
    //Delete Channel Preview Program by Package Name
    private void DeletePreviewProgramInChannelbyPackageName(Uri ChannelUri, String packageName)
    {
        if(ChannelUri.toString().equals("null"))
            return;
        Cursor channelCursor = getChannelCursorbyUri(ChannelUri);
        Log.d(TAG, "DeletePreviewProgramInChannelbyPackageName: Channel count = [" + channelCursor.getCount() +"]");
        if (channelCursor.getCount() != 0)
        {
            while(channelCursor.moveToNext() && channelCursor.getString(2).equals(PACKAGE_NAME)) {
                PrintChannelCursor(channelCursor);//Print Channel Information
                Cursor programCursor = getRecommendProgram(channelCursor.getLong(0));
                Log.d(TAG, "DeletePreviewProgramInChannelbyPackageName: programCursor count = [" + channelCursor.getCount() +"]");
                if (programCursor != null && programCursor.getCount() != 0) {
                    while (programCursor.moveToNext()) {
                        PrintPreviewProgramCursor(programCursor);//Print Preview Program Information
                        long id = programCursor.getLong(0);
                        String filterString;
                        filterString = programCursor.getString(6);//Get Preview Program Package Name
                        Log.d(TAG, "DeletePreviewProgramInChannelbyPackageName: filterString = [" + filterString +"] packageName = [" + packageName +"]");
                        if(packageName.equals(filterString))//delete same Genre preview program
                            mContext.getContentResolver().delete(TvContractCompat.buildPreviewProgramUri(id), null, null);
                    }
                }
            }
        }
    }

    /**
     * @param ChannelUri
     * @return ret > 0 : Delete SUCCESS
     * ret <= 0 : Delete FAIL
     */
    public int DeleteChannelRow(Uri ChannelUri)
    {
        int ret = 0;
        if(ChannelUri.toString().equals("null")) {
            Log.d(TAG, "DeleteChannelRow: Please Check the Correct Uri");
            return ret;
        }
        ret = mContext.getContentResolver().delete(ChannelUri,null,null);
        return ret;
    }

    /**
     * @param ChannelUri
     * @param deleteType = 0: delete all, 1:delete by ChannelId, 2: delete by Program Uri
     * @param deleteFilter String
     * @return Successfully Delete Programs Size, should > 0
     */
    public List<Uri> DeletePrograms(Uri ChannelUri, int deleteType, String deleteFilter)
    {
        Cursor channelCursor = getChannelCursorbyUri(ChannelUri);
        List<Uri> deleteProgramList = new ArrayList<>();
        Log.d(TAG, "DeletePrograms: Channel count = [" + channelCursor.getCount() +"]");
        if (channelCursor.getCount() != 0)
        {
            while(channelCursor.moveToNext()) {
                if(channelCursor.getString(2).equals(PACKAGE_NAME)) {
                    PrintChannelCursor(channelCursor);//Print Channel Information
                    Cursor programCursor = getRecommendProgram(channelCursor.getLong(0));
                    Log.d(TAG, "DeletePrograms: programCursor count = [" + channelCursor.getCount() + "]");
                    if (programCursor != null && programCursor.getCount() != 0) {
                        while (programCursor.moveToNext()) {
                            PrintPreviewProgramCursor(programCursor);//Print Preview Program Information
                            long id = programCursor.getLong(0);
                            String filterString = "";
                            boolean doUpdate = false;

                            if (deleteType == 0) {
                                doUpdate = true;
                            } else if (deleteType == 1)//Delete by Pesi ChannelId
                            {
                                filterString = String.valueOf(GetPesiChannelIdfromIntent(programCursor.getString(2)));
                                if (deleteFilter.equals(filterString))
                                    doUpdate = true;
                            } else if (deleteType == 2)//Delete by Program Uri
                            {
                                if (deleteFilter.equals(TvContractCompat.buildPreviewProgramUri(id).toString()))
                                    doUpdate = true;
                            }
                            Log.d(TAG, "DeletePrograms: deleteFilter = [" + deleteFilter + "] deleteType = [" + deleteType + "]");
                            //if(Append.getDeleteFilter().equals(filterString) || doUpdate)//delete preview program
                            if (doUpdate) {
                                Uri DeleteProgramUri = TvContractCompat.buildPreviewProgramUri(id);
                                int ret = mContext.getContentResolver().delete(DeleteProgramUri, null, null);
                                if(ret > 0) {
                                    deleteProgramList.add(DeleteProgramUri);
                                }
                            }
                        }
                    }
                }
            }
        }
        return deleteProgramList;
    }

    private PreviewProgram SetProgramInfoValues(Uri channelUri, TvProviderProgramInfo programInfo)
    {
        long tvPorviderChannelID = ContentUris.parseId(channelUri);
        PreviewProgram previewProgram = new PreviewProgram.Builder()
                .setChannelId(tvPorviderChannelID)
                .setType(programInfo.getType())
                .setTitle(programInfo.getTitle())
                .setDescription(programInfo.getDescription())
                .setStartTimeUtcMillis(programInfo.getStartTime())
                .setEndTimeUtcMillis(programInfo.getEndTime())
                .setAuthor(programInfo.getAuthor())
                .setIntent(programInfo.getIntent())
                .setGenre(programInfo.getGenre())
                .setPosterArtUri(programInfo.getPosterArtUri())
                .setDurationMillis(programInfo.getDurationMillis())
                .setInternalProviderData(programInfo.getInternalProviderData().getBytes()) //query
                .setLive(programInfo.getLive())
                .setLogoUri(programInfo.getLogoUri())
                .setLogoContentDescription(programInfo.getLogoUriDescription())
                .setAvailability(programInfo.getAvailabilityType())
                .setContentRatings(programInfo.getContentRating())
                .setCanonicalGenres(programInfo.getCanonicalGenres())
                .setEpisodeNumber(programInfo.getEpisodeNumber())
                .setEpisodeTitle(programInfo.getEpisodeTitle())
                .setInteractionCount(programInfo.getInteractionCount())
                .setInteractionType(programInfo.getInteractionTpye())
                .setItemCount(programInfo.getItemCount())
                .setInternalProviderId(programInfo.getInternalProviderId())
                .setOfferPrice(programInfo.getOfferPrice())
                .setPosterArtAspectRatio(programInfo.getPosterArtAspectRatio())
                .setPreviewVideoUri(programInfo.getPreviewVideoUri())
                .setPreviewAudioUri(programInfo.getPreviewAudioUri())
                .setReleaseDate(programInfo.getReleaseDate())
                .setReviewRating(String.valueOf(programInfo.getReviewRating()))
                .setReviewRatingStyle(programInfo.getReviewRatingStyle())
                .setSeasonNumber(programInfo.getSeasonNumber())
                .setStartingPrice(programInfo.getStartingPrice())
                .setThumbnailAspectRatio(programInfo.getThumbnailratio())
                .setThumbnailUri(programInfo.getThumbnailUri())
                .setVideoHeight(programInfo.getVideoHeight())
                .setVideoWidth(programInfo.getVideoWidth())
                .setWeight(programInfo.getWeight())
                .setSearchable(programInfo.isSearchable())
                // Set more attributes...
                .build();

        return previewProgram;
    }

    private Cursor getRecommendChannelCursorbyUri(Uri channelUri)
    {
        String CHANNEL_SELECTION = TvContractCompat.Channels.COLUMN_TYPE + "='" +
                TvContractCompat.Channels.TYPE_PREVIEW + "'";
        // Other column names can be found in TvContractCompat.Channels
        String[] CHANNEL_PROJECTION = {
                TvContractCompat.Channels._ID,
                TvContractCompat.Channels.COLUMN_DISPLAY_NAME,
                TvContractCompat.Channels.COLUMN_PACKAGE_NAME,
                TvContractCompat.Channels.COLUMN_APP_LINK_INTENT_URI,
                TvContractCompat.Channels.COLUMN_DISPLAY_NUMBER,
                TvContractCompat.Channels.COLUMN_INPUT_ID,
                TvContractCompat.Channels.COLUMN_DESCRIPTION,
                TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_DATA,

        };
        Cursor channelCursor = mContext.getContentResolver().query(
                channelUri,
                CHANNEL_PROJECTION,
                CHANNEL_SELECTION,
                null, null);

        return channelCursor ;
    }

    private Cursor getRecommendProgramCursor(long channelId)
    {
        String PROGRAMS_SELECTION =
                TvContractCompat.PreviewPrograms.COLUMN_CHANNEL_ID + "=? AND " +
                        TvContractCompat.PreviewPrograms.COLUMN_BROWSABLE + "=1";

        String[] PROGRAMS_PROJECTION = {
                TvContractCompat.PreviewPrograms._ID,
                TvContractCompat.PreviewPrograms.COLUMN_CHANNEL_ID,
                TvContractCompat.PreviewPrograms.COLUMN_TYPE,
                TvContractCompat.PreviewPrograms.COLUMN_TITLE,
                TvContractCompat.PreviewPrograms.COLUMN_SHORT_DESCRIPTION,
                TvContractCompat.PreviewPrograms.COLUMN_START_TIME_UTC_MILLIS,
                TvContractCompat.PreviewPrograms.COLUMN_END_TIME_UTC_MILLIS,
                TvContractCompat.PreviewPrograms.COLUMN_AUTHOR,
                TvContractCompat.PreviewPrograms.COLUMN_INTENT_URI,
                TvContractCompat.PreviewPrograms.COLUMN_GENRE,
                TvContractCompat.PreviewPrograms.COLUMN_POSTER_ART_URI,
                TvContractCompat.PreviewPrograms.COLUMN_DURATION_MILLIS,
                TvContractCompat.PreviewPrograms.COLUMN_INTERNAL_PROVIDER_DATA,
                TvContractCompat.PreviewPrograms.COLUMN_LIVE,
                TvContractCompat.PreviewPrograms.COLUMN_LOGO_URI,
                TvContractCompat.PreviewPrograms.COLUMN_LOGO_CONTENT_DESCRIPTION,
                TvContractCompat.PreviewPrograms.COLUMN_AVAILABILITY,
                TvContractCompat.PreviewPrograms.COLUMN_CONTENT_RATING,
                TvContractCompat.PreviewPrograms.COLUMN_CANONICAL_GENRE,
                TvContractCompat.PreviewPrograms.COLUMN_EPISODE_DISPLAY_NUMBER,
                TvContractCompat.PreviewPrograms.COLUMN_EPISODE_TITLE,
                TvContractCompat.PreviewPrograms.COLUMN_INTERACTION_COUNT,
                TvContractCompat.PreviewPrograms.COLUMN_INTERACTION_TYPE,
                TvContractCompat.PreviewPrograms.COLUMN_ITEM_COUNT,
                TvContractCompat.PreviewPrograms.COLUMN_INTERNAL_PROVIDER_ID,
                TvContractCompat.PreviewPrograms.COLUMN_OFFER_PRICE,
                TvContractCompat.PreviewPrograms.COLUMN_POSTER_ART_ASPECT_RATIO,
                TvContractCompat.PreviewPrograms.COLUMN_PREVIEW_VIDEO_URI,
                TvContractCompat.PreviewPrograms.COLUMN_PREVIEW_AUDIO_URI,
                TvContractCompat.PreviewPrograms.COLUMN_RELEASE_DATE,
                TvContractCompat.PreviewPrograms.COLUMN_REVIEW_RATING,
                TvContractCompat.PreviewPrograms.COLUMN_REVIEW_RATING_STYLE,
                TvContractCompat.PreviewPrograms.COLUMN_SEASON_DISPLAY_NUMBER,
                TvContractCompat.PreviewPrograms.COLUMN_STARTING_PRICE,
                TvContractCompat.PreviewPrograms.COLUMN_THUMBNAIL_ASPECT_RATIO,
                TvContractCompat.PreviewPrograms.COLUMN_THUMBNAIL_URI,
                TvContractCompat.PreviewPrograms.COLUMN_VIDEO_HEIGHT,
                TvContractCompat.PreviewPrograms.COLUMN_VIDEO_WIDTH,
                TvContractCompat.PreviewPrograms.COLUMN_WEIGHT,
                TvContractCompat.PreviewPrograms.COLUMN_CONTENT_RATING
        };

        String[] SELECTION_ARGS = { String.valueOf(channelId) };
        Cursor programCursor = mContext.getContentResolver().query(
                TvContractCompat.PreviewPrograms.CONTENT_URI,
                PROGRAMS_PROJECTION,
                PROGRAMS_SELECTION,
                SELECTION_ARGS,
                null);

        return programCursor ;
    }

    /**
     * Get Channel Info from TvProvider DataBase
     * @param channelUri
     * @return TvProviderChannelInfo
     * no Channel Info return null
     */
    public TvProviderChannelInfo GetChannelInfobyChannelUri(Uri channelUri)
    {
        TvProviderChannelInfo tvProviderChannelInfo = null;//= new TvProviderChannelInfo();
        Cursor channelCursor = getChannelCursorbyUri(channelUri);
        Log.d(TAG, "GetChannelInfobyChannelUri: Channel count = [" + channelCursor.getCount() +"]");
        if (channelCursor.getCount() != 0)
        {
            while(channelCursor.moveToNext()) {
                tvProviderChannelInfo = new TvProviderChannelInfo();
                tvProviderChannelInfo.setChannelId(channelCursor.getString(0));
                tvProviderChannelInfo.setDisplayName(channelCursor.getString(1));
                tvProviderChannelInfo.setPackageName(channelCursor.getString(2));
                if(channelCursor.getString(3) != null) {
                    String IntentString = channelCursor.getString(3);
                    //intent:#Intent;launchFlags=0x10000000;component=com.prime.dtvplayer/.Activity.ViewActivity;end
                    Log.d(TAG, "GetProgramInfobyChannelUri: [" + IntentString+"]");
                    String[] SplitStr = IntentString.split("/");
                    String[] CompStr = SplitStr[1].split(";");
                    String componentName = CompStr[0];

                    Log.d(TAG, "GetProgramInfobyChannelUri: componentName = [" + componentName +"]");
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(mContext,componentName));
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);

                    tvProviderChannelInfo.setAppintent(intent);
                }
                tvProviderChannelInfo.setDisplayNumber(channelCursor.getString(4));
                tvProviderChannelInfo.setInputId(channelCursor.getString(5));
                tvProviderChannelInfo.setDescription(channelCursor.getString(6));

                if(channelCursor.getBlob(7) != null) {
                    String string = new String(channelCursor.getBlob(7));
                    tvProviderChannelInfo.setInternalProviderData(string);
                }

                tvProviderChannelInfo.setType(channelCursor.getString(8));
                Log.d(TAG, "GetChannelInfobyChannelUri: " + channelCursor.getInt(9));
                tvProviderChannelInfo.setSearchable(channelCursor.getInt(9) == 1);
            }
        }

        return tvProviderChannelInfo;
    }


    /**
     * Get Programs Info Data from TvProvider DataBase
     * @param channelUri
     * @return TvProviderProgramInfo List
     * no programs data return null
     */
    public List<TvProviderProgramInfo> GetProgramInfobyChannelUri(Uri channelUri)
    {
        List<TvProviderProgramInfo> programList = null; //= new ArrayList<>();

        Cursor tmpCursor = getChannelCursorbyUri(channelUri);
        Cursor tmpProgramCursor = null;
        if(tmpCursor != null && tmpCursor.getCount() != 0)
        {
            tmpCursor.moveToNext();
            tmpProgramCursor = getRecommendProgramCursor(tmpCursor.getLong(0));
        }

        if (tmpProgramCursor != null && tmpProgramCursor.getCount() != 0) {
            Log.d(TAG, "GetProgramInfobyChannelUri: programCursor count = [" + tmpProgramCursor.getCount() + "]");
            programList = new ArrayList<>();
            boolean isUpdateProgramExist = false;
            while (tmpProgramCursor.moveToNext()) {
                TvProviderProgramInfo tmpProgramInfo = new TvProviderProgramInfo();
                Log.d(TAG, "GetProgramInfobyChannelUri: programCursor String = [" + tmpProgramCursor.toString() + "] id = [" + tmpProgramCursor.getLong(0) + "]");


                tmpProgramInfo.Type = tmpProgramCursor.getInt(2);
                tmpProgramInfo.Title = tmpProgramCursor.getString(3);
                tmpProgramInfo.Description = tmpProgramCursor.getString(4);
                tmpProgramInfo.StartTime = tmpProgramCursor.getLong(5);
                tmpProgramInfo.EndTime = tmpProgramCursor.getLong(6);
                tmpProgramInfo.Author = tmpProgramCursor.getString(7);


                tmpProgramInfo.PackageName = tmpProgramCursor.getString(8);
                tmpProgramInfo.Genre = tmpProgramCursor.getString(9);
                tmpProgramInfo.PosterArtUri = Uri.parse(tmpProgramCursor.getString(10));
                tmpProgramInfo.PosterArtUriDescription = tmpProgramCursor.getString(11);
                tmpProgramInfo.InternalProviderData = new String(tmpProgramCursor.getBlob(12));
                tmpProgramInfo.PesiChannelId = GetPesiChannelIdfromIntent(tmpProgramCursor.getString(8));

                ///////////////Intent
                if(tmpProgramCursor.getString(8) != null) {
                    //intent:#Intent;launchFlags=0x10000000;component=com.prime.dtvplayer/.Activity.ViewActivity;l.channel_id=714735643;end
                    String IntentString = tmpProgramCursor.getString(8);
                    String[] SplitStr = IntentString.split(";");
                    String[] CompStr = SplitStr[2].split("/");
                    String componentName = CompStr[1];

                    Log.d(TAG, "GetProgramInfobyChannelUri: componentName = [" + componentName +"]");
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(mContext,componentName));
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(mContext.getString(R.string.STR_CHANGE_PROGRAM_CHANNEL_ID),tmpProgramInfo.PesiChannelId);

                    tmpProgramInfo.Intent = intent;
                }

                tmpProgramInfo.DurationMillis = tmpProgramCursor.getInt(11);
                tmpProgramInfo.isLive = tmpProgramCursor.getInt(13) == 1;/////need check

                if(tmpProgramCursor.getString(14) != null)
                    tmpProgramInfo.logoUri = Uri.parse(tmpProgramCursor.getString(14));////Why Error?
                tmpProgramInfo.logoUriDescription = tmpProgramCursor.getString(15);
                tmpProgramInfo.availabilityType = tmpProgramCursor.getInt(16);

                ////////////////////CanonicalGenres
                if(tmpProgramCursor.getString(18) != null) {
                    String tmpStr = tmpProgramCursor.getString(18);
                    String[] Genres = tmpStr.split(",");
                    Log.d(TAG, "GetProgramInfobyChannelUri: CanonicalGenres = [" + tmpProgramCursor.getString(18) + "]");
                    String[] canonical_genres = new String[Genres.length];
                    for(int i = 0 ; i < Genres.length ; i++) {
                        Log.d(TAG, "GetProgramInfobyChannelUri: Str[" + i + "] = [" + Genres[i] + "]");
                        canonical_genres[i] = Genres[i];
                    }
                    tmpProgramInfo.CanonicalGenres = canonical_genres;
                    Log.d(TAG, "GetProgramInfobyChannelUri: [" + tmpProgramInfo.CanonicalGenres.length + "] ");
                }

                tmpProgramInfo.episodeNumber = tmpProgramCursor.getInt(19);
                tmpProgramInfo.episodeTitle = tmpProgramCursor.getString(20);
                tmpProgramInfo.interactionCount = tmpProgramCursor.getInt(21);
                tmpProgramInfo.interactionTpye = tmpProgramCursor.getInt(22);
                tmpProgramInfo.itemCount= tmpProgramCursor.getInt(23);
                tmpProgramInfo.internalProviderId = tmpProgramCursor.getString(24);
                tmpProgramInfo.offerPrice = tmpProgramCursor.getString(25);
                tmpProgramInfo.PosterArtAspectRatio = tmpProgramCursor.getInt(26);

                if(tmpProgramCursor.getString(27) != null)
                    tmpProgramInfo.previewVideoUri = Uri.parse(tmpProgramCursor.getString(27));
                if(tmpProgramCursor.getString(28) != null)
                    tmpProgramInfo.previewAudioUri = Uri.parse(tmpProgramCursor.getString(28));
                tmpProgramInfo.releaseDate = tmpProgramCursor.getString(29);

                /////////////////////reviewRating
                if(tmpProgramCursor.getString(30) != null) {
                    Log.d(TAG, "GetProgramInfobyChannelUri: reviewRating = [" + tmpProgramCursor.getString(30) + "]");
                    tmpProgramInfo.reviewRating = tmpProgramCursor.getString(30);
                }


                tmpProgramInfo.reviewRatingStyle = tmpProgramCursor.getInt(31);
                tmpProgramInfo.seasonNumber = tmpProgramCursor.getInt(32);
                tmpProgramInfo.startingPrice = tmpProgramCursor.getString(33);
                tmpProgramInfo.thumbnailratio = tmpProgramCursor.getInt(34);
                if(tmpProgramCursor.getString(35) != null) {
                    tmpProgramInfo.thumbnailUri = Uri.parse(tmpProgramCursor.getString(35));
                }
                tmpProgramInfo.videoHeight = tmpProgramCursor.getInt(36);
                tmpProgramInfo.videoWidth = tmpProgramCursor.getInt(37);
                tmpProgramInfo.weight = tmpProgramCursor.getInt(38);


                ///////////////////////////tvContentRatings example = [com.android.tv/US_TV/US_TV_PG/US_TV_D/US_TV_L]
                if(tmpProgramCursor.getString(39) != null) {
                    String tmpStr = tmpProgramCursor.getString(39);
                    Log.d(TAG, "GetProgramInfobyChannelUri: conterntRating = [" + tmpStr +"]");
                    String[] Str = tmpStr.split("/");
                    TvContentRating tvContentRating;
                    int size = Str.length - 3;//Check subRatings Size
                    if(size > 0) {
                        String[] subRating = new String[size];
                        for (int i = 3, j = 0; i < Str.length; i++, j++) {
                            subRating[j] = Str[i];
                            Log.d(TAG, "GetProgramInfobyChannelUri: j = [" + j + "] = [" + subRating[j] + "]");
                        }
                        tvContentRating = TvContentRating.createRating(Str[0],Str[1],Str[2],subRating);
                    }
                    else
                    {
                        tvContentRating = TvContentRating.createRating(Str[0],Str[1],Str[2]);
                    }
                    TvContentRating[] contentRatings = {tvContentRating};
                    tmpProgramInfo.contentRating = contentRatings;
                    // Log.d(TAG, "GetProgramInfobyChannelUri: [" + contentRatings.toString() +"]");
                }
                programList.add(tmpProgramInfo);
            }
        }
        return programList;

    }

}
