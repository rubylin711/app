package com.prime.dmg.launcher.Rank;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.dmg.launcher.EPG.MyLinearLayoutManager;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.JsonParser.JsonParser;
import com.prime.dmg.launcher.Utils.JsonParser.RankInfo;

import java.util.List;

public class RankingView extends RelativeLayout {
    private static final String TAG = "RankingView";
    private Rank g_rank;
    private int g_current_focus_channel;
    private List<RankInfo> g_program_item = null;
    private int g_enter_channel_num;
    private boolean g_is_blue_key_skip;
    private RankDetailView g_view_rank_detail = null;
    private MiddleFocusRecyclerView g_rcv_ranking_list = null;
    private MyLinearLayoutManager g_lnrl_manager = null;
    private RankingAdapter g_adpt_ranking = null;
    private Runnable g_tune_channel_runnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "run: g_current_focus_channel = " + g_current_focus_channel);
            RankActivity.set_current_channel_number(g_current_focus_channel);
            g_rank.channel_change_by_digit(g_current_focus_channel);
        }
    };

    public RankingView(Context context) {
        super(context);
        init_view();
    }

    public RankingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_view();
    }

    public RankingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_view();
    }

    public void init(Rank rank) {
        g_rank = rank;
    }

    private void init_view() {
        inflate(getContext(), R.layout.view_ranking, this);
        g_rcv_ranking_list = findViewById(R.id.lo_rank_rcv_list);
        g_view_rank_detail = findViewById(R.id.lo_rank_view_channel_detail);

        g_rcv_ranking_list.setHasFixedSize(true);
        g_lnrl_manager = new MyLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        g_rcv_ranking_list.setLayoutManager(g_lnrl_manager);
        g_rcv_ranking_list.addItemDecoration(new RankingAdapter.MyItemDecoration(getContext()));
        g_adpt_ranking = new RankingAdapter(getContext(), get_program_item_list(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g_is_blue_key_skip = true;
                Intent intent = new Intent(getContext(), HomeActivity.class);
                intent.putExtra(HomeActivity.EXTRA_SCREEN_TYPE, HomeActivity.SCREEN_TYPE_LIVE_TV);
                getContext().startActivity(intent);
            }
        }, new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Log.d(TAG, "onFocusChange hasFocus = " + hasFocus);
                if (!hasFocus || g_program_item == null) {
                    return;
                }
                set_program(Utils.get_tag_position((String) v.getTag()), Utils.get_sec_tag_id((String) v.getTag()),false);
            }
        });
        g_rcv_ranking_list.setAdapter(g_adpt_ranking);
    }

    private List<RankInfo> get_program_item_list() {
        String jsonRankList = ACSDataProviderHelper.get_acs_provider_data(getContext(), "rank_list");
        g_program_item = JsonParser.parse_ranking_info(jsonRankList);
        return g_program_item;
    }

    public void set_program(int position, int channelNum, boolean tuneRightNow) {
        if (g_program_item == null )
            return;
        if (g_program_item.size() == 0)
            return;

        RankInfo rankProgramItem = g_program_item.get(position);
        g_view_rank_detail.set_program(rankProgramItem, channelNum);
        if (RankActivity.get_current_channel_number() == channelNum) {
            return;
        }
        tune_channel(channelNum, tuneRightNow);
    }

    private void tune_channel(final int channelNum, boolean tuneRightNow) {
        removeCallbacks(g_tune_channel_runnable);
        g_current_focus_channel = channelNum;
        if (channelNum != RankActivity.get_current_channel_number()) {
            if (tuneRightNow) {
                post(g_tune_channel_runnable);
            } else {
                postDelayed(g_tune_channel_runnable, 500L);
            }
        }
    }
}
