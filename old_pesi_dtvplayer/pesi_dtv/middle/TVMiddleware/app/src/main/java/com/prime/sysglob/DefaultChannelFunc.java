package com.prime.sysglob;

import com.prime.sysdata.DefaultChannel;

/**
 * Created by scoty on 2018/1/31.
 */

public interface DefaultChannelFunc {
    public void SetDefaultChannel(int channelId, int grouptype);
    public DefaultChannel GetDefaultChannel();
}
