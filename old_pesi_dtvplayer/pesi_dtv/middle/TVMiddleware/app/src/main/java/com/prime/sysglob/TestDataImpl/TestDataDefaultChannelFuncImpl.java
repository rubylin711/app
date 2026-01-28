package com.prime.sysglob.TestDataImpl;

import android.content.Context;

import com.prime.TestData.TestData;
import com.prime.sysdata.DefaultChannel;
import com.prime.sysglob.DefaultChannelFunc;
import com.prime.tvclient.TestDataTVClient;
/**
 * Created by scoty on 2018/1/31.
 */

public class TestDataDefaultChannelFuncImpl implements DefaultChannelFunc{
    private static final String TAG="TestDataDefaultChannelFuncImpl";
    private Context context;
    TestData testData = null;

    public TestDataDefaultChannelFuncImpl(Context context){
        this.context = context;
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public void SetDefaultChannel(int channelId, int grouptype)
    {
        DefaultChannel defaultChannel = testData.GetTestDataDefaultChannel();
        defaultChannel.setChanneId(channelId);
        defaultChannel.setGroupType(grouptype);
    }

    @Override
    public DefaultChannel GetDefaultChannel()
    {
        DefaultChannel defaultChannel = testData.GetTestDataDefaultChannel();
        return defaultChannel;
    }
}
