package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class LocalTimeOffsetDescriptor extends DescBase{
    private static final String TAG = "LocalTimeOffsetDescriptor";
    private final List<LocalTimeOffset> mLocalTimeOffsetList = new ArrayList<>();

    public LocalTimeOffsetDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        int t=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;

        t = t + 2; // move to the start pos of LocalTimeOffsets
        while (t<Length) {
            final String countryCode = getISO8859_1String(data,t,3);
            final int countryRegionId = getInt(data, t+3, 1, 0xFC) >>2;
            final int localTimeOffsetPolarity = getInt(data, t+3, 1, 0x01);
            final int localTimeOffset = getInt(data, t+4, 2, MASK_16BITS);
            final int timeOfChange_msb = getInt(data,t+6,2, MASK_16BITS);
            final int timeOfChange_lsb = getInt(data,t+8,3, MASK_24BITS);
            final int nextTimeOffset = getInt(data, t+11,2, MASK_16BITS);

            final LocalTimeOffset s = new LocalTimeOffset(countryCode, countryRegionId, localTimeOffsetPolarity, localTimeOffset, timeOfChange_msb, timeOfChange_lsb, nextTimeOffset);
            mLocalTimeOffsetList.add(s);
            t+=13;
        }

    }

    public static class LocalTimeOffset {
        public String CountryCode;
        public int CountryRegionId;
        public int LocalTimeOffsetPolarity;
        public int LocalTimeOffset;
        public int TimeOfChangeMsb; /* 4bit MSB of total 40bit */
        public int TimeOfChangeLsb; /* 32bit LSB of total 40bit */
        public int nextTimeOffset;

        public LocalTimeOffset(final String c, final int id, final int localPolarity, int localOffset, final int timeChangeMsb, final int timeChangeLsb, final int nextOffset) {
            CountryCode = c;
            CountryRegionId = id;
            LocalTimeOffsetPolarity = localPolarity;
            LocalTimeOffset = localOffset;
            TimeOfChangeMsb = timeChangeMsb;
            TimeOfChangeLsb = timeChangeLsb;
            nextTimeOffset = nextOffset;

        }
    }

    public List<LocalTimeOffset> getOffsetList() {
        return mLocalTimeOffsetList;
    }
}
