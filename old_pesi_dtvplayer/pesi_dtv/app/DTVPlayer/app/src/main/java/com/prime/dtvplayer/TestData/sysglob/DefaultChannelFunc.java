package com.prime.dtvplayer.TestData.sysglob;

import com.prime.dtvplayer.Sysdata.DefaultChannel;

/**
 * Created by scoty on 2018/1/31.
 */

public interface DefaultChannelFunc {
    public void SetDefaultChannel(long channelId, int grouptype);
    public DefaultChannel GetDefaultChannel();
}
