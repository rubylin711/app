package com.prime.launcher.Rank;

import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.ProgramInfo;

public class Rank {
    private static final String TAG = "Rank";
    private PrimeDtv g_dtv;
    private ChannelChangeManager g_ch_change_manager;

    Rank(PrimeDtv primeDtv, ChannelChangeManager ch_change_manager) {
        g_dtv = primeDtv;
        g_ch_change_manager = ch_change_manager;
    }

    public void channel_change_by_digit(int digit) {
        g_ch_change_manager.change_channel_by_digit(digit);
    }

    public ProgramInfo get_program_by_service_id(int serviceId) {
        return g_dtv.get_program_by_service_id(serviceId);
    }
}
