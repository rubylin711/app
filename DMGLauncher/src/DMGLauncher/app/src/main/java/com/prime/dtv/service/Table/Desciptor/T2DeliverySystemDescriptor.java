package com.prime.dtv.service.Table.Desciptor;


import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import com.prime.dtv.service.Util.BitSource;

import java.util.ArrayList;
import java.util.List;

public class T2DeliverySystemDescriptor extends DescBase{
    private static final String TAG = "T2DeliverySystemDescriptor";
    public int plp_id;
    public int t2_system_id;
    public int siso_miso;
    public int bandwidth;
    public int reserved_future_use;
    public int guard_interval;
    public int transmission_mode;
    public int other_frequency_flag;
    public int tfs_flag;
    public List<CellInfo> mcellInfoList = new ArrayList<>();

    public T2DeliverySystemDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }
    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        plp_id = getInt(data, 3, 1, MASK_8BITS);
        t2_system_id = getInt(data, 4, 2, MASK_16BITS);
        if (Length > 4) {
            siso_miso = getInt(data, 6, 1, 0b1100_0000) >> 6; // 2 bslbf
            bandwidth = getInt(data, 6, 1, 0b0011_1100) >> 2; // 4 bslbf
            reserved_future_use = getInt(data, 6, 1, 0b0000_0011); // 2 bslbf
            guard_interval = getInt(data, 7, 1, 0b1110_0000) >> 5; // 3 bslbf
            transmission_mode = getInt(data, 7, 1, 0b0001_1100) >> 2; // 3 bslbf
            other_frequency_flag = getInt(data, 7, 1, 0b0000_0010) >> 1; // 1 bslbf
            tfs_flag = getInt(data, 7, 1, 0b0000_0001); // 1 bslbf
            BitSource bs = new BitSource(data, 8, Length + 2);
            while (bs.available() > 0) {
                CellInfo ci = new CellInfo(bs);
                mcellInfoList.add(ci);
            }
        }

    }

    private class CellInfo {

        public class CentreFrequency {
            private final int centre_frequency;

            public CentreFrequency(final int centre_frequency){
                this.centre_frequency = centre_frequency;
            }

        }

        public class SubCellInfo {

            int cell_id_extension ;
            int transposer_frequency ;


            public SubCellInfo(int cell_id_extension,	int transposer_frequency) {
                this.cell_id_extension = cell_id_extension;
                this.transposer_frequency = transposer_frequency;
            }

        }


        public int cell_id;
        public int frequency_loop_length;
        public List<CentreFrequency> mcentreFrequencyList = new ArrayList<>();
        public int centre_frequency;
        public int subcell_info_loop_length;
        public List<SubCellInfo> msubCellInfoList = new ArrayList<>();

        public CellInfo(BitSource bs) {

            cell_id = bs.readBits(16);
            if (tfs_flag == 1){
                frequency_loop_length = bs.readBits(8);
                for (int j=0;j<(frequency_loop_length/4);j++){
                    int centre_frequency = bs.readBits(32);
                    mcentreFrequencyList.add(new CentreFrequency(centre_frequency));
                }
            }else{
                centre_frequency = bs.readBits(32);
            }
            subcell_info_loop_length = bs.readBits(8);
            for (int k=0;k<(subcell_info_loop_length/5);k++)
            {
                int cell_id_extension = bs.readBits(8);
                int transposer_frequency = bs.readBits(32);
                msubCellInfoList.add(new SubCellInfo(cell_id_extension, transposer_frequency));
            }
        }

    }
}
