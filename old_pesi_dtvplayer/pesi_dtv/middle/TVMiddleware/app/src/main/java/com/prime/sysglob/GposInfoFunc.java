package com.prime.sysglob;

import com.prime.sysdata.GposInfo;

/**
 * Created by gary_hsu on 2017/11/20.
 */

public interface GposInfoFunc {
    public abstract GposInfo GetGposInfo();
    public abstract void Save(GposInfo gPos);
}
