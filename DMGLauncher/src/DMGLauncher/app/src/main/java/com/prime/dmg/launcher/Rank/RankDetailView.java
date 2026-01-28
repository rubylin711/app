package com.prime.dmg.launcher.Rank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.JsonParser.RankInfo;

public class RankDetailView  extends RelativeLayout {
    private static final String TAG = "RankDetailView";
    private ImageView g_imgv_channel_image;
    private int g_channel_num;
    private TextView g_textv_detail;
    private TextView g_textv_program_name;


    public RankDetailView(Context context) {
        super(context);
        init_view();
    }

    public RankDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_view();
    }

    public RankDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_view();
    }

    private void init_view() {
        inflate(getContext(), R.layout.view_rank_detail, this);
        g_imgv_channel_image = findViewById(R.id.lo_rank_detail_sub_imgv_channel_Icon);
        g_textv_detail = findViewById(R.id.lo_rank_detail_textv_detail);
        g_textv_program_name = findViewById(R.id.lo_rank_detail_sub_textv_program_name);
    }

    @SuppressLint("SetTextI18n")
    public void set_program(RankInfo rankProgramItem, int channelNum) {
        g_channel_num = rankProgramItem.get_channel_id();
        //缺少載圖
        g_imgv_channel_image.setImageDrawable(ResourcesCompat.getDrawable(getContext().getResources(),R.mipmap.ch051, null));

        g_textv_program_name.setText(rankProgramItem.get_channel_name());
        g_textv_program_name.setSelected(false);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                g_textv_program_name.setSelected(true);
            }
        }, 500L);

        g_textv_detail.setText(Utils.get_leading_zero_number_at(channelNum) + "  " + rankProgramItem.get_channel_name() + " - " + rankProgramItem.get_tv_name());
    }

    public int get_channel_num() {
        return this.g_channel_num;
    }
}
