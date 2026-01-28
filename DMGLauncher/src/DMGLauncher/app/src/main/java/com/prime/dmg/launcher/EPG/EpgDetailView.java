package com.prime.dmg.launcher.EPG;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.URLUtils;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.ProgramInfo;

public class EpgDetailView extends RelativeLayout {
    private final String TAG = "EpgDetailView";
    public static final int EPG_NO_DATA     = -1;
    public static final int EPG_LOCK_DATA   = -2;
    public static final int EPG_NO_EXPIRED  = -3;

    private TextView g_program_title;
    private TextView g_program_desc;
    private ImageView g_program_poster;
    private ImageView g_program_quality;
    private ImageView g_program_grading;
    private ScrollView g_sclv_content;

    private Epg g_epg;

    public EpgDetailView(Context context) {
        super(context);
        init_view();
    }

    public EpgDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_view();
    }

    public EpgDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_view();
    }

    public void init( Epg epg) {
        g_epg = epg;
    }

    private void init_view() {
        inflate(getContext(), R.layout.view_epg_detail, this);
        g_program_title = (TextView) findViewById(R.id.lo_epg_detail_textv_program_title);
        g_program_desc = (TextView) findViewById(R.id.lo_epg_detail_textv_program_desc);
        g_program_poster = (ImageView) findViewById(R.id.lo_epg_detail_imgv_program_poster);
        g_program_quality = (ImageView) findViewById(R.id.lo_epg_detail_imgv_program_quality);
        g_program_grading = (ImageView) findViewById(R.id.lo_epg_detail_imgv_program_grading);
        g_sclv_content = (ScrollView) findViewById(R.id.lo_epg_detail_sclv_content);
        g_sclv_content.setClickable(false);
        g_sclv_content.setFocusable(false);
    }

    public void update_program(int programId, long channelId, EPGEvent epgEvent, long category) {
        get_activity().runOnUiThread(() -> {
        if (programId == EPG_NO_DATA) {
            g_program_title.setText(getContext().getText(R.string.epg_no_data));
            g_program_quality.setImageResource(0);
            g_program_grading.setImageResource(0);
            g_program_poster.setImageResource(0);
            g_program_desc.setText("");

        }
        else if (programId == EPG_LOCK_DATA) {
            g_program_title.setText(EpgDetailView.this.getContext().getText(R.string.epg_lock_data));
            g_program_quality.setImageResource(0);
            g_program_grading.setImageResource(0);
            g_program_poster.setImageResource(0);
            g_program_desc.setText("");
        }
        else if (programId == EPG_NO_EXPIRED) {
            g_program_title.setText(getContext().getText(R.string.epg_expired));
            g_program_quality.setImageResource(0);
            g_program_grading.setImageResource(0);
            g_program_poster.setImageResource(0);
            g_program_desc.setText("");
        }
        else {
            String desc = g_epg.get_detail_description(channelId, programId);
            if (desc == null || desc.isEmpty())
                g_program_desc.setText(getContext().getText(R.string.epg_no_data));
            else
                g_program_desc.setText(desc);

            String eventName = null;
            if (epgEvent == null)
                g_program_title.setText(R.string.epg_no_data);
            else {
                eventName = epgEvent.get_event_name();
                if (eventName == null || eventName.isEmpty())
                    g_program_title.setText(R.string.epg_no_data);
                else
                    g_program_title.setText(eventName);
            }

            g_program_quality.setVisibility(VISIBLE);

            ProgramInfo programInfo = g_epg.get_program_by_channel_id(channelId);
            if (programInfo != null && programInfo.getAdultFlag() == 1 && epgEvent != null)
                epgEvent.set_parental_rate(18);

            set_program_quality(category);
            set_program_grading(epgEvent, programInfo);
            load_poster(programInfo, eventName);
        }
        });
    }

    private void set_program_grading(EPGEvent epgEvent, ProgramInfo channel) {
        g_program_grading.setImageResource(MiniEPG.get_grading_res_id(epgEvent, channel));
        /*if(parentalRate == 0 || parentalRate == -1)
            g_program_grading.setImageResource(0);
        if (parentalRate < 6)
            g_program_grading.setImageResource(R.mipmap.rating_level_g);
        if (parentalRate >= 6 && parentalRate < 12)
            g_program_grading.setImageResource(R.mipmap.rating_level_p);
        else if ( parentalRate >= 12 && parentalRate < 15 )
            g_program_grading.setImageResource(R.mipmap.rating_level_pg12);
        else if ( parentalRate >= 15 && parentalRate < 18 )
            g_program_grading.setImageResource(R.mipmap.rating_level_pg15);
        else if ( parentalRate >= 18 && parentalRate < 99 )
            g_program_grading.setImageResource(R.mipmap.rating_level_r);
        else {
            g_program_grading.setImageResource(R.mipmap.rating_level_g);
            Log.d(TAG, "UpdateBanner: parental rate not found");
        }*/
    }

    private void set_program_quality(long category) {
        if (is_4k(category))
            g_program_quality.setImageResource(R.drawable.icon_4k);
        else if (is_hd(category))
            g_program_quality.setImageResource(R.drawable.icon_tv_hd);
        else {
            g_program_quality.setVisibility(GONE);
            g_program_quality.setImageResource(0);
        }

    }

    private EpgActivity get_activity() {
        return (EpgActivity) getContext();
    }

    private boolean is_hd(long category) {
        return ((category & 0x0100)>>8) == 1;
    }

    private boolean is_4k(long category) {
        return ((category & 0x0800)>>11) == 1;
    }

    private void load_poster(ProgramInfo programInfo, String eventName){
        String url = "";

        if (programInfo == null) {
            Log.e(TAG, "load_poster: programInfo is null");
            return;
        }
        try {
            url = URLUtils.generate_poster_url(getContext(), programInfo.getServiceId(), eventName);
        } catch (Exception e) {
            Log.d(TAG, "load_poster: exception = " + e);
        }
        Log.d(TAG, "load_poster: url = " + url);

        Context context = getContext();
        if (Utils.is_context_valid_for_glide(context)) {
            String finalUrl = url;
            get_activity().runOnUiThread(() -> {
                if (!get_activity().isFinishing() && !get_activity().isDestroyed()) {
                    Glide.with(get_activity())
                            //.load("https://epgstore.tbc.net.tw/acs-api//program/ch4507/pt%E7%99%BE%E8%90%AC%E6%94%BB%E9%A0%82%E5%A4%A7%E6%8C%91%E6%88%B0%E7%AC%AC1%E5%AD%A3%3A2.png")
                            .load(finalUrl)
                            .skipMemoryCache(false)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(g_program_poster);
                }
            });
        }
    }
}
