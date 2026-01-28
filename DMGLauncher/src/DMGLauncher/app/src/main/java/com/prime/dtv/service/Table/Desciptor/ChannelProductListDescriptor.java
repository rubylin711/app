package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.MASK_16BITS;
import static com.prime.dtv.service.Util.Utils.MASK_24BITS;
import static com.prime.dtv.service.Util.Utils.MASK_8BITS;
import static com.prime.dtv.service.Util.Utils.getInt;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Util.DVBString;

import java.util.ArrayList;
import java.util.List;

public class ChannelProductListDescriptor extends DescBase{
    private static final String TAG = "ChannelProductListDescriptor";
    int mProductNumber; //8 bits
    public List<ChannelProductListDesc> mChannelProductListDesc = new ArrayList<ChannelProductListDesc>();

    public ChannelProductListDescriptor(byte[] data) {
        Parsing(data, data.length);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;

        mProductNumber = Length/2;
        Log.d(TAG,"Tag = " + Tag + " Length = " + Length + " mProductNumber = " + mProductNumber);
        if(toUnsignedInt(data[0]) != Descriptor.CHANNEL_PRODUCT_LIST_DESCRIPTOR)
        {
            Log.d(TAG,"unknow desciptor [" + toUnsignedInt(data[0]) + "]");
        }
        else {
            int productId,r;
            ChannelProductListDesc channelProductListDesc;

            Tag = toUnsignedInt(data[0]);
            Length = toUnsignedInt(data[1]);
            if(lens == Length+2 && Length>0)
                DataExist = true;
            r=0;
            while((Length-r) > 0){
                productId = getInt(data, r+2, 2, MASK_16BITS);
                //Log.d(TAG,"productId = " + productId);
                r=r+2;
                channelProductListDesc = new ChannelProductListDesc(productId);

                mChannelProductListDesc.add(channelProductListDesc);
            }
        }
    }

    public class ChannelProductListDesc {
        public int ProductId;

        private ChannelProductListDesc(int productId) {
            super();
            this.ProductId=productId;

        }
    }
}