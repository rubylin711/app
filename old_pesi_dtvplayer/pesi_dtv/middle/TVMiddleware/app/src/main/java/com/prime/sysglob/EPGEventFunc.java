package com.prime.sysglob;

import com.prime.sysdata.EPGEvent;
import java.util.List;

/**
 * Created by gary_hsu on 2017/11/22.
 */

public interface EPGEventFunc {
    public abstract List<EPGEvent> getEventPF(int sid, int transportStreamId, int originalNetworkId);
    public abstract List<EPGEvent> getEventSchedule(int sid,int transportStreamId,int originalNetworkId);
}
