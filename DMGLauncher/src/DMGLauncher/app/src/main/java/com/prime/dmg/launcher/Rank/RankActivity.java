package com.prime.dmg.launcher.Rank;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.Player.AvCmdMiddle;
import com.prime.dtv.service.Util.ErrorCodeUtil;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.TVMessage;

public class RankActivity extends BaseActivity implements PrimeDtv.DTVCallback{
    private static final String TAG = "RankActivity";
    private static int g_current_focus_channel = -1;

    private RankingView g_view_ranking;
    private SurfaceView g_surface_view;
    private SurfaceView g_surface_view_2;
    private SurfaceView g_surface_view_3;
    private TextView g_live_tv_message;

    private PrimeDtv g_dtv;
    public Rank g_rank;
    private ChannelChangeManager g_ch_change_manager;
    private int checkAvFrameCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        init_view();
        init();

        //g_dtv.register_callbacks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //g_dtv.unregister_callbacks();
    }

    private void init_view() {
        g_view_ranking = findViewById(R.id.lo_rank_view);
        g_surface_view = findViewById(R.id.lo_rank_live_tv);
        g_surface_view.setVisibility(View.INVISIBLE);
        g_surface_view_2 = findViewById(R.id.lo_rank_live_tv_2);
        g_surface_view_2.setVisibility(View.INVISIBLE);
        g_surface_view_3 = findViewById(R.id.lo_rank_live_tv_3);
        g_surface_view_3.setVisibility(View.INVISIBLE);
        g_live_tv_message = findViewById(R.id.lo_rank_live_tv_message);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            g_surface_view.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
            g_surface_view_2.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
            g_surface_view_3.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
        }
    }

    private void init() {
        g_dtv = HomeApplication.get_prime_dtv();
        g_ch_change_manager = ChannelChangeManager.get_instance(getApplicationContext());
        g_rank = new Rank(g_dtv, g_ch_change_manager);
        g_view_ranking.init(g_rank);
        g_dtv.set_surface_view(this, g_surface_view, 0);
        g_dtv.set_surface_view(this, g_surface_view_2, 1);
        g_dtv.set_surface_view(this, g_surface_view_3, 2);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case 0:
                long channelID = tvMessage.getChannelId();
                break;
            case TVMessage.TYPE_AV_FRAME_PLAY_STATUS:
                if(tvMessage.getAvFrameChannelId() != g_ch_change_manager.get_cur_ch_id())
                    return;
                if (tvMessage.getAvFrameStatus() == 0) { // av ok
                    if (is_ca_message_visible()) {
                        close_ca_message();
                        if(g_dtv.av_control_get_play_status(0) != AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)
                            checkAvFrameCnt++;
                        else
                            checkAvFrameCnt = 0;
                        if(checkAvFrameCnt > 3) {
                            g_ch_change_manager.change_channel_by_id(g_ch_change_manager.get_cur_ch_id());
                            checkAvFrameCnt = 0;
                        }
                    }
                }
                else { // av not ok
                    ProgramInfo cur_channel = g_dtv.get_program_by_channel_id(tvMessage.getAvFrameChannelId());
                    checkAvFrameCnt = 0;
                    if(tvMessage.getAvFrameStatus() == 1) {
//                        if(g_dtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)
//                            g_ch_change_manager.change_channel_stop(0, 0);
                        if(!is_ca_message_visible())
                            showErrorMessage(ErrorCodeUtil.ERROR_E213, null);
                    }
                    else if (cur_channel != null && !g_dtv.get_tuner_status(cur_channel.getTunerId())
                            && !is_ca_message_visible()) {
                        // tuner not lock, show e200
                        show_ca_message(getString(R.string.error_e200));
                    }
                }
                break;
            case TVMessage.TYPE_SYSTEM_SHOW_ERROR_MESSAGE:
                Log.d(TAG, "onMessage: TYPE_SYSTEM_SHOW_ERROR_MESSAGE");
                g_ch_change_manager.change_channel_stop(0, 0);
                showErrorMessage(tvMessage.getErrCode(), tvMessage.getMessage());
                break;
        }
    }


    private void show_ca_message(String string) {
        g_live_tv_message.setText(string);
        g_live_tv_message.setVisibility(View.VISIBLE);
    }

    private void close_ca_message() {
        g_live_tv_message.setVisibility(View.GONE);
    }

    private boolean is_ca_message_visible() {
        return g_live_tv_message.getVisibility() == View.VISIBLE;
    }

    public static void set_current_channel_number(int channelNumber) {
        g_current_focus_channel = channelNumber;
    }

    public static int get_current_channel_number() {
        return g_current_focus_channel;
    }

    private void showErrorMessage(int errCode, String message) {
        String errorMessage = ErrorCodeUtil.getErrorMessage(this, errCode, message);
        runOnUiThread(() -> show_ca_message(errorMessage));
    }
}
