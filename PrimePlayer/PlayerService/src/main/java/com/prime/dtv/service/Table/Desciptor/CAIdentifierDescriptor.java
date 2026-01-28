package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class CAIdentifierDescriptor extends DescBase{
    private static final String TAG = "CAIdentifierDescriptor";
    private final List<CASystemId> mCA_system_id = new ArrayList<CASystemId>();
    //public int CASystemId;

    public static class CASystemId {
        private final int ca_system_id;

        public CASystemId(final int ca_system_id) {
            super();
            this.ca_system_id = ca_system_id;
        }
    }
    public CAIdentifierDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        int t=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        while(t<Length){
            final int caSystemID = getInt(data, 2 + t, 2, MASK_16BITS);
            final CASystemId caID= new CASystemId(caSystemID);
            mCA_system_id.add(caID);
            t+=2;
        }
    }
}
