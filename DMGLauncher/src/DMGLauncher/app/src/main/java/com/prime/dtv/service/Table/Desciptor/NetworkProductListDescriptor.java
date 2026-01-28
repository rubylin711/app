package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static com.prime.dtv.service.Util.Utils.getInt;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Util.DVBString;

import java.util.ArrayList;
import java.util.List;

public class NetworkProductListDescriptor extends DescBase{
    private static final String TAG = "NetworkProductListDescriptor";
    int mProductIdCount; //8 bits
    public List<NetworkProductListDesc> mNetworkProductListDesc = new ArrayList<NetworkProductListDesc>();

    public NetworkProductListDescriptor(byte[] data) {
        Parsing(data, data.length);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        mProductIdCount = toUnsignedInt(data[2]);
        Log.d(TAG,"Tag = " + Tag + " Length = " + Length + " mProductIdCount = " + mProductIdCount);
        if(toUnsignedInt(data[0]) != Descriptor.NETWORK_PRODUCT_LIST_DESCRIPTOR)
        {
            Log.d(TAG,"unknow desciptor [" + toUnsignedInt(data[0]) + "]");
        }
        else {
            int productId,displayOrder,dsisplayMode,iSO639LanguageCode1,productNameEngLength,iSO639LanguageCode2,productNameChiLength,r;
            String productNameEng,productNameChi;
            int offset=3;
            NetworkProductListDesc networkProductListDesc;
            DVBString dvbString;
            Tag = toUnsignedInt(data[0]);
            Length = toUnsignedInt(data[1]);
            if(lens == Length+2 && Length>0)
                DataExist = true;
            r=0;
            while((Length-1-r) > 0){
                productId = getInt(data, r+offset, 2, MASK_16BITS);
                displayOrder = (int) ((data[r+offset+2] & 0xFC) >> 2);
                dsisplayMode = (int) (data[r+offset+2] & 0x1);

                iSO639LanguageCode1 = getInt(data, r+offset+3, 3, MASK_24BITS);
                productNameEngLength = getInt(data, r+offset+6, 1, MASK_8BITS);
                //Log.d(TAG," productNameEngLength = " + productNameEngLength);
                dvbString = new DVBString(data, r+offset+7, productNameEngLength);
                productNameEng = dvbString.toString(Pvcfg.getDefaultCharset());
                //Log.d(TAG,"productNameEng = " + productNameEng);
                iSO639LanguageCode2 = getInt(data, r+offset+7+productNameEngLength, 3, MASK_24BITS);
                productNameChiLength = getInt(data, r+offset+10+productNameEngLength, 1, MASK_8BITS);
                //Log.d(TAG,"productNameChiLength = " + productNameChiLength);
                dvbString = new DVBString(data, r+offset+11+productNameEngLength, productNameChiLength);
                productNameChi = dvbString.toString(Pvcfg.getDefaultCharset());
                //Log.d(TAG,"productNameChi = " + productNameChi);
                r=r+11+productNameEngLength+productNameChiLength;

                networkProductListDesc = new NetworkProductListDesc(productId,displayOrder,dsisplayMode,
                        iSO639LanguageCode1,productNameEngLength,productNameEng,
                        iSO639LanguageCode2,productNameChiLength,productNameChi);

                mNetworkProductListDesc.add(networkProductListDesc);
            }
        }
    }

    public class NetworkProductListDesc {
        public int ProductId;
        public int DisplayOrder;
        public int DisplayMode;
        public int ISO639LanguageCode1;
        public int ProductNameEngLength;
        public String ProductNameEng;
        public int	ISO639LanguageCode2;
        public int ProductNameChiLength;
        public String ProductNameChi;

        private NetworkProductListDesc(int productId, int displayOrder, int DisplayMode,
                                       int ISO639LanguageCode1, int ProductNameEngLength, String ProductNameEng,
                                       int ISO639LanguageCode2, int ProductNameChiLength, String ProductNameChi) {
            super();
            this.ProductId=productId;
            this.DisplayOrder=displayOrder;
            this.DisplayMode=DisplayMode;
            this.ISO639LanguageCode1=ISO639LanguageCode1;
            this.ProductNameEngLength=ProductNameEngLength;
            this.ProductNameEng=ProductNameEng;
            this.ISO639LanguageCode2=ISO639LanguageCode2;
            this.ProductNameChiLength=ProductNameChiLength;
            this.ProductNameChi=ProductNameChi;
        }
    }
}
