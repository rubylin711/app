package com.prime.dtvplayer.View;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by jim_huang on 2017/10/26.
 */

public class InfoBannerView extends ConstraintLayout {
    private final String TAG = getClass().getSimpleName();
    private ImageView bannerBG ;
    private TextView bannerNo ;
    private TextView bannerName ;
    private TextView bannerEvent ;
    private ImageView imageScramble ;
    private ImageView imageSubtitle ;
    private ImageView imageTeletext;
    private ImageView imageStereo ;
    private ImageView imageParentalrate ;
    private ImageView imageResolution ;
    private ImageView imageRatio ;
    private ImageView imageTvRadio;
    private ImageView bannerArrowLeft;
    private ImageView bannerArrowRight;
    private TextView bannerCurTime ;
    //private TextView dateClock ;
//    private int visibility = View.INVISIBLE ;

    public InfoBannerView(Context context) {
        super(context);
    }

    public InfoBannerView(Context context, AttributeSet attrs) {
        this(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "onFinishInflate: ");
        bannerBG = (ImageView)findViewById(R.id.bannerBgIGV) ;
        bannerNo = (TextView)findViewById(R.id.bannerNoTXV) ;
        bannerName = (TextView)findViewById(R.id.bannerNameTXV) ;
        bannerEvent = (TextView)findViewById(R.id.bannerEventTXV) ;
        imageTvRadio = (ImageView) findViewById(R.id.banner_tvradio_imageview);
        imageScramble = (ImageView)findViewById(R.id.scrambleIGV) ;
        imageSubtitle = (ImageView)findViewById(R.id.subtitleIGV) ;
        imageTeletext = (ImageView) findViewById(R.id.teletextIGV);
        imageStereo = (ImageView)findViewById(R.id.stereoIGV) ;
        imageParentalrate = (ImageView)findViewById(R.id.parentalRateIGV) ;
        imageResolution = (ImageView)findViewById(R.id.resolutionIGV) ;
        imageRatio = (ImageView)findViewById(R.id.ratioIGV) ;
        bannerCurTime = (TextView)findViewById(R.id.curTimeTXV) ;
        //dateClock = (TextView)findViewById(R.id.textClock) ;
        bannerArrowLeft = (ImageView) findViewById(R.id.banner_arrow_left);
        bannerArrowRight = (ImageView) findViewById(R.id.banner_arrow_right);

    }

    public void SetVisibility(int visible,
                              GposInfo gposInfo,
                              ProgramInfo progInfo,
                              List<EPGEvent> epgEventGetPF,
                              Date curTime,
                              int channelNumber,
                              DTVActivity mdtv) {
        Log.d(TAG, "SetVisibility: ");

        /*visibility = ( visible == VISIBLE ? VISIBLE : INVISIBLE ) ;
        bannerBG.setVisibility(visibility);
        bannerNo.setVisibility(visibility);
        bannerName.setVisibility(visibility);
        bannerEvent.setVisibility(visibility);
        imageTvRadio.setVisibility(visibility);
        imageScramble.setVisibility(visibility);
        imageSubtitle.setVisibility(visibility);
        imageTeletext.setVisibility(visibility);
        imageStereo.setVisibility(visibility);
        imageParentalrate.setVisibility(visibility);
        imageResolution.setVisibility(visibility);
        imageRatio.setVisibility(visibility);
        bannerCurTime.setVisibility(visibility);*/
        //dateClock.setVisibility(visibility);

        this.setVisibility(visible);

        bannerArrowLeft.setVisibility(INVISIBLE);
        bannerArrowRight.setVisibility(INVISIBLE);


        if( visible == VISIBLE ){
            if(mdtv != null) {
                if (mdtv.ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                    Log.d(TAG, "SetVisibility: UpdateVodBanner");
                    EPGEvent event = mdtv.ViewHistory.getCurChannel().getPresentepgEvent();
                    UpdateVodBanner(mdtv.ViewHistory.getCurChannel(), epgEventGetPF, event);
                } else if (mdtv.ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                    Log.d(TAG, "SetVisibility: UpdateYoutubeBanner");
                    EPGEvent event = mdtv.ViewHistory.getCurChannel().getPresentepgEvent();
                    UpdateYoutubeBanner(mdtv.ViewHistory.getCurChannel(), epgEventGetPF ,event);
                } else {
                    Log.d(TAG, "SetVisibility: UpdateBanner");
                    UpdateBanner(gposInfo, progInfo, epgEventGetPF, curTime, channelNumber, mdtv);
                }
            }
        }
    }

    public int GetVisibility()
    {
//        return visibility == VISIBLE ? VISIBLE : INVISIBLE ;
        return this.getVisibility();
    }

    public void UpdateBanner( GposInfo gposInfo,
                              ProgramInfo progInfo,
                              List<EPGEvent> epgEventGetPF,
                              Date curTime,
                              int channelNumber,
                              DTVActivity mdtv)
    {
        if ((gposInfo == null) || (progInfo == null)/*||epgEventGetPF == null*/)
            return;

        Log.d(TAG, "UpdateBanner:" +
                " channelId = "+ progInfo.getChannelId()+
                " channelNum = "+ progInfo.getDisplayNum()+
                " chname = "+ progInfo.getDisplayName()//+
                //" epgEventGetPF.size() = "+ epgEventGetPF.size()
        );

        int playId;
        int resolutionHeight = 0;
        int fps = 0;
        int screenRatio = 0;
        int parentalRate = -1 ;
        EnAudioTrackMode curTrackMode = null;
        SubtitleInfo subtitleInfo = null;
        boolean teletextAvailable = false;

        if(mdtv != null) {
            playId = mdtv.ViewHistory.getPlayId();
            resolutionHeight = mdtv.AvControlGetVideoResolutionHeight(playId);//gposInfo.getResolution();
            fps = mdtv.AvControlGetFPS(playId);
            screenRatio = mdtv.AvControlGetRatio(playId);
            parentalRate = -1;
            curTrackMode = mdtv.AvControlGetTrackMode(playId);
            subtitleInfo = mdtv.AvControlGetSubtitleList(playId);
            teletextAvailable = mdtv.AvControlIsTeletextAvailable(playId);
        }

        Log.d(TAG, "UpdateBanner: resolutionHeight = " + resolutionHeight);
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    HH:mm", Locale.getDefault());
        String date = sDateFormat.format(curTime);

        bannerCurTime.setText(date);
        //bannerNo.setText(String.valueOf(progInfo.getDisplayNum()));
        bannerNo.setText(String.valueOf(channelNumber));
        bannerName.setText(progInfo.getDisplayName());

        // TV or Ratio
        if (progInfo.getType() == ProgramInfo.ALL_TV_TYPE)
            imageTvRadio.setBackgroundResource(R.drawable.tv);
        else if (progInfo.getType() == ProgramInfo.ALL_RADIO_TYPE)
            imageTvRadio.setBackgroundResource(R.drawable.radio);
        else {
            imageTvRadio.setBackground(null);
            Log.d(TAG, "UpdateBanner: unknown program type");
        }

        // event name
        if(epgEventGetPF != null && epgEventGetPF.size() == 2) {
            bannerEvent.setText(epgEventGetPF.get(EPGEvent.EPG_TYPE_PRESENT).getEventName());
            bannerArrowRight.setVisibility(VISIBLE);
            bannerArrowLeft.setVisibility(INVISIBLE);
        }
        else {
            bannerEvent.setText("");
            bannerArrowRight.setVisibility(INVISIBLE);
            bannerArrowLeft.setVisibility(INVISIBLE);
        }

        // CA
        if ( progInfo.getCA() == 1 )
            imageScramble.setBackgroundResource(R.drawable.scremble);
        else
            imageScramble.setBackground(null);

        // subtitle
        if(subtitleInfo != null)
            Log.d(TAG, "UpdateBanner: count == " + subtitleInfo.getComponentCount());
        if(subtitleInfo != null && subtitleInfo.getComponentCount() > 1)
            imageSubtitle.setBackgroundResource(R.drawable.subtitle);
        else
            imageSubtitle.setBackground(null);

        if(teletextAvailable == true)
            imageTeletext.setBackgroundResource(R.drawable.start_teletext);
        else
            imageTeletext.setBackground(null);

        // audio selected
        if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT)
        {
            imageStereo.setBackgroundResource(R.drawable.stereo_left);
        }
        else if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT)
        {
            imageStereo.setBackgroundResource(R.drawable.stereo_right);
        }
        else if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO)
        {
            imageStereo.setBackgroundResource(R.drawable.stereo_dual);
        }
        else{
            imageStereo.setBackground(null);
            Log.d(TAG, "UpdateBanner: audioSelected not found");
        }

        // parental rate
        if(epgEventGetPF != null && epgEventGetPF.size() != 0) {
            parentalRate = epgEventGetPF.get(EPGEvent.EPG_TYPE_PRESENT).getParentalRate();
        }

        Log.d(TAG, "UpdateBanner: parentalRate = "+ parentalRate);
        if(parentalRate == 0 || parentalRate == -1)
            imageParentalrate.setBackground(null);
        if (parentalRate >= 6 && parentalRate < 12)
            imageParentalrate.setBackgroundResource(R.drawable.parental_06);
        else if ( parentalRate >= 12 && parentalRate < 16 )
            imageParentalrate.setBackgroundResource(R.drawable.parental_12);
        else if ( parentalRate >= 16 && parentalRate < 18 )
            imageParentalrate.setBackgroundResource(R.drawable.parental_16);
        else if ( parentalRate >= 18 && parentalRate < 99 )
            imageParentalrate.setBackgroundResource(R.drawable.parental_18);
        else if(parentalRate == 99){
            imageParentalrate.setBackgroundResource(R.drawable.parental_all_blocked);
            Log.d(TAG, "UpdateBanner: parental rate not found");
        }

        // resolution
        if ( resolutionHeight == 480 )
            imageResolution.setBackgroundResource(R.drawable.resolution480i);
        else if ( resolutionHeight == 576 )
            imageResolution.setBackgroundResource(R.drawable.resolution576p);
        else if ( resolutionHeight == 720 )
            imageResolution.setBackgroundResource(R.drawable.resolution720p);
        else if ( resolutionHeight == 1080 ) {
            if(fps == 25)
            imageResolution.setBackgroundResource(R.drawable.resolution1080i);
            else
            imageResolution.setBackgroundResource(R.drawable.resolution1080p);
        }
        else if ( resolutionHeight == 2160 ) // Edwin 20190513 if resolution height is 2160, info banner show 4k icon
        {
            imageResolution.setBackgroundResource(R.drawable.resolution_4k);
        }
        else {
            imageResolution.setBackground(null);
            Log.d(TAG, "UpdateBanner: resolution not found");
        }

        // ratio
        if ( screenRatio == 1 )
            imageRatio.setBackgroundResource(R.drawable.ratio4to3);
        else if ( screenRatio == 2 )
            imageRatio.setBackgroundResource(R.drawable.ratio16to9);
        else {
            imageRatio.setBackground(null);
            Log.d(TAG, "UpdateBanner: ratio not found");
        }
    }

    public void UpdateVodBanner(SimpleChannel channel, List<EPGEvent> epgEventGetPF, EPGEvent event)
    {
        bannerEvent.setText(event.getEventName());
        //bannerCurTime.setText(String.valueOf(event.getStartTime()));
        bannerNo.setText(String.valueOf(channel.getChannelNum()));
        bannerName.setText(channel.getChannelName());


        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    HH:mm", Locale.getDefault());
        String date = sDateFormat.format(event.getStartTime());
        bannerCurTime.setText(date);

        if(epgEventGetPF != null && epgEventGetPF.size() == 2) {
            //bannerEvent.setText(channel.getPresentepgEvent().getEventName());
            bannerEvent.setText(epgEventGetPF.get(EPGEvent.EPG_TYPE_PRESENT).getEventName());
            bannerArrowRight.setVisibility(VISIBLE);
            bannerArrowLeft.setVisibility(INVISIBLE);
        }
        else {
            bannerEvent.setText("");
            bannerArrowRight.setVisibility(INVISIBLE);
            bannerArrowLeft.setVisibility(INVISIBLE);
        }
    }

    public void UpdateYoutubeBanner(SimpleChannel channel, List<EPGEvent> epgEventGetPF, EPGEvent event)
    {
        bannerEvent.setText(event.getEventName());
        //bannerCurTime.setText(String.valueOf(event.getStartTime()));
        bannerNo.setText(String.valueOf(channel.getChannelNum()));
        bannerName.setText(channel.getChannelName());

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    HH:mm", Locale.getDefault());
        String date = sDateFormat.format(event.getStartTime());
        bannerCurTime.setText(date);

        if(epgEventGetPF != null && epgEventGetPF.size() == 2) {
            //bannerEvent.setText(channel.getPresentepgEvent().getEventName());
            bannerEvent.setText(epgEventGetPF.get(EPGEvent.EPG_TYPE_PRESENT).getEventName());
            bannerArrowRight.setVisibility(VISIBLE);
            bannerArrowLeft.setVisibility(INVISIBLE);
        }
        else {
            bannerEvent.setText("");
            bannerArrowRight.setVisibility(INVISIBLE);
            bannerArrowLeft.setVisibility(INVISIBLE);
        }
    }

    public void ChangeBannerEventTo(String eventName, int eventType) {
        if (bannerBG.getVisibility() == VISIBLE) {
            // event name
            bannerEvent.setText(eventName);
            if (eventType == EPGEvent.EPG_TYPE_FOLLOW) {
                bannerArrowRight.setVisibility(INVISIBLE);
                bannerArrowLeft.setVisibility(VISIBLE);
                Log.d(TAG, "SwitchBannerEvent: current event type is FOLLOW");
            }
            else if (eventType == EPGEvent.EPG_TYPE_PRESENT) {
                bannerArrowLeft.setVisibility(INVISIBLE);
                bannerArrowRight.setVisibility(VISIBLE);
                Log.d(TAG, "SwitchBannerEvent: current event type is PRESENT");
            }
            else {
                bannerArrowLeft.setVisibility(INVISIBLE);
                bannerArrowRight.setVisibility(INVISIBLE);
                Log.d(TAG, "SwitchBannerEvent: no event");
            }
        }
    }
}
