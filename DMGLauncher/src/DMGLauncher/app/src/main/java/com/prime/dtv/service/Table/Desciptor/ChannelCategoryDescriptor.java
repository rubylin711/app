package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelCategoryDescriptor extends DescBase{
    private static final String TAG = "ChannelCategoryDescriptor";

    public List<ChannelCategoryDesc> mChannelCategoryDesc = new ArrayList<ChannelCategoryDesc>();

    public ChannelCategoryDescriptor(byte[] data) {
        Parsing(data, data.length);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if (lens == Length + 2 && Length > 0)
            DataExist = true;
        //Log.d(TAG,"allen_test Tag = " + Tag + " Length = " + Length+" data = "+ Arrays.toString(data));
        if (toUnsignedInt(data[0]) != Descriptor.CHANNEL_CATEGORY_DESCRIPTOR) {
            Log.d(TAG, "unknow desciptor [" + toUnsignedInt(data[0]) + "]");
        }
        else {
            long categoryType;
            ChannelCategoryDesc channelCategoryDesc;
            Tag = toUnsignedInt(data[0]);
            Length = toUnsignedInt(data[1]);
            if (lens == Length + 2 && Length > 0)
                DataExist = true;
            //LogUtils.d("DataExist = "+DataExist);
            if(DataExist) {
                categoryType = getInt(data, 6, 4, MASK_32BITS);
                channelCategoryDesc = new ChannelCategoryDesc(categoryType);
                mChannelCategoryDesc.add(channelCategoryDesc);
            }
        }
    }

    public class ChannelCategoryDesc {
        public long CategoryType;

        private ChannelCategoryDesc(long categoryType) {
            super();
            this.CategoryType = categoryType;
        }
    }
}


