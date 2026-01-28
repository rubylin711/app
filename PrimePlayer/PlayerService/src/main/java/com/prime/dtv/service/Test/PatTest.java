package com.prime.dtv.service.Test;


import android.util.Log;

import com.prime.dtv.service.Table.PatData;


public class PatTest {
    private static final String TAG = "PatTest";

	public void PatTestFuntion() {
        //SdtData sdtData;
        //sdtData=new SdtData();
        int i, j, totalNum;
        //The test data is SDT Actual from mux1.trp
        byte[] patTableData1 = new byte[]
                {       (byte)0x00 ,(byte)0xb0 ,(byte)0xa5 ,(byte)0x00 ,(byte)0x01 ,(byte)0xd5 ,(byte)0x00 ,
						(byte)0x00 ,(byte)0x00 ,(byte)0x00 ,(byte)0xe0 ,(byte)0x10 ,(byte)0x00 ,(byte)0x01 ,(byte)0xe5 ,
						(byte)0x6e ,(byte)0x00 ,(byte)0x64 ,(byte)0xe5 ,(byte)0x64 ,(byte)0x00 ,(byte)0x96 ,(byte)0xe4 ,
						(byte)0xd8 ,(byte)0x00 ,(byte)0x9b ,(byte)0xe4 ,(byte)0xb0 ,(byte)0x00 ,(byte)0x9c ,(byte)0xe4 ,
						(byte)0x24 ,(byte)0x00 ,(byte)0x9e ,(byte)0xe4 ,(byte)0x74 ,(byte)0x00 ,(byte)0x9f ,(byte)0xe4 ,
						(byte)0xce ,(byte)0x00 ,(byte)0xc9 ,(byte)0xe4 ,(byte)0x38 ,(byte)0x00 ,(byte)0xca ,(byte)0xe4 ,
						(byte)0x42 ,(byte)0x00 ,(byte)0xcb ,(byte)0xe4 ,(byte)0x1a ,(byte)0x00 ,(byte)0xcc ,(byte)0xe4 ,
						(byte)0x2e ,(byte)0x00 ,(byte)0xcd ,(byte)0xe4 ,(byte)0x6a ,(byte)0x00 ,(byte)0xce ,(byte)0xe4 ,
						(byte)0xc4 ,(byte)0x00 ,(byte)0xcf ,(byte)0xe4 ,(byte)0xba ,(byte)0x01 ,(byte)0x02 ,(byte)0xe4 ,
						(byte)0x88 ,(byte)0x01 ,(byte)0x2d ,(byte)0xe3 ,(byte)0xfc ,(byte)0x01 ,(byte)0x2e ,(byte)0xe3 ,
						(byte)0xf2 ,(byte)0x01 ,(byte)0x2f ,(byte)0xe4 ,(byte)0x56 ,(byte)0x01 ,(byte)0x31 ,(byte)0xe4 ,
						(byte)0x10 ,(byte)0x01 ,(byte)0xf7 ,(byte)0xe4 ,(byte)0x60 ,(byte)0x01 ,(byte)0xf8 ,(byte)0xe5 ,
						(byte)0x46 ,(byte)0x02 ,(byte)0x5d ,(byte)0xe5 ,(byte)0x3c ,(byte)0x02 ,(byte)0x9c ,(byte)0xe5 ,
						(byte)0x78 ,(byte)0x03 ,(byte)0x21 ,(byte)0xe4 ,(byte)0x4c ,(byte)0x03 ,(byte)0x22 ,(byte)0xe4 ,
						(byte)0xa6 ,(byte)0x03 ,(byte)0x23 ,(byte)0xe4 ,(byte)0x9c ,(byte)0x03 ,(byte)0x85 ,(byte)0xe5 ,
						(byte)0x0a ,(byte)0x03 ,(byte)0x86 ,(byte)0xe4 ,(byte)0xe2 ,(byte)0x03 ,(byte)0x87 ,(byte)0xe4 ,
						(byte)0x7e ,(byte)0x03 ,(byte)0x88 ,(byte)0xe5 ,(byte)0x28 ,(byte)0x03 ,(byte)0x89 ,(byte)0xe5 ,
						(byte)0x14 ,(byte)0x03 ,(byte)0x8a ,(byte)0xe4 ,(byte)0xec ,(byte)0x03 ,(byte)0x8b ,(byte)0xe4 ,
						(byte)0xf6 ,(byte)0x03 ,(byte)0x8c ,(byte)0xe5 ,(byte)0x00 ,(byte)0x03 ,(byte)0x8e ,(byte)0xe5 ,
						(byte)0x50 ,(byte)0x03 ,(byte)0x8f ,(byte)0xe5 ,(byte)0x5a ,(byte)0x03 ,(byte)0x91 ,(byte)0xe5 ,
						(byte)0x1e ,(byte)0x03 ,(byte)0x97 ,(byte)0xe4 ,(byte)0x06 ,(byte)0x0f ,(byte)0xd1 ,(byte)0x3e ,
						(byte)0xd8 ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
						(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff
                };
        Log.d(TAG, "PmtParsing PatTableData1 Start @@@@@@@@@@@@@@@@");
        PatData patData1 = new PatData();
        patData1.parsing(patTableData1, patTableData1.length);
        Log.d(TAG, "PmtParsing PatTableData1 End @@@@@@@@@@@@@@@@");


        totalNum = patData1.getPrograms().size();
        Log.d(TAG,"Get Pmt Data TotalNumber = " + totalNum);
        if(totalNum > 0)
        {
            i = 0;
            for(i=0;i<totalNum;i++) {
                Log.d(TAG, "=======================================");
                Log.d(TAG, "Get Pat Data " + (i + 1));
                int program_number = patData1.getProgramNumber(i);
                Log.d(TAG, "program_number = " + program_number);
                int program_map_PID = patData1.getProgramMapPid(i);
                Log.d(TAG, "program_map_PID = " + program_map_PID);
            }
        }
    }
}