package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class FrequencyListDescriptor extends DescBase{
    private static final String TAG = "FrequencyListDescriptor";

    public int CodingType = 0;
    public List<CentreFrequency> mfrequencyList = new ArrayList<>();

    public FrequencyListDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        int t=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        while(t<(Length-3)){
            final long freq = getLong(data, 3 + t, 4, MASK_32BITS);
            final CentreFrequency cent_freq= new CentreFrequency(freq);
            mfrequencyList.add(cent_freq);
            t+=4;
        }

    }

    public class CentreFrequency {
        private final long centure_frequency;

        public CentreFrequency(final long centurefrequency) {
            super();
            this.centure_frequency = centurefrequency;
        }
    }
}
