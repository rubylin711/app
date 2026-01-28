package com.prime.dtvplayer.TestData.sysglob;

import com.prime.dtvplayer.Sysdata.GposInfo;

/**
 * Created by gary_hsu on 2017/11/20.
 */

public interface GposInfoFunc {
    public abstract GposInfo GetGposInfo();
    public abstract void Save(GposInfo gPos);
}
