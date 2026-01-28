package com.prime.dmg.launcher.Rank;

import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.sysdata.ProgramInfo;

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
