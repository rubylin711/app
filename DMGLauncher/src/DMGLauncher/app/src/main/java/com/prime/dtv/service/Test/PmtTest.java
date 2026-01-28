package com.prime.dtv.service.Test;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dtv.service.Table.PmtData;
import com.prime.dtv.utils.TVMessage;

import java.util.ArrayList;
import java.util.List;

public class PmtTest extends BaseActivity {
    private static final String TAG = "PmtTest";

	private List<PmtData> mpmtdatalist = new ArrayList<PmtData>();

	public void PmtTestFuntion() {
        int i, j, totalNum;
        byte[] pmtTableData1 = new byte[]
                {       (byte)0x02, (byte)0xb0, (byte)0x28, (byte)0x00, (byte)0x17, (byte)0xc5,
                        (byte)0x00, (byte)0x00, (byte)0xf8, (byte)0x57, (byte)0xf0, (byte)0x00,
                        (byte)0x1b, (byte)0xf8, (byte)0x57, (byte)0xf0, (byte)0x00, (byte)0x03,
                        (byte)0xf8, (byte)0x58, (byte)0xf0, (byte)0x06, (byte)0x0a, (byte)0x04,
                        (byte)0x65, (byte)0x6e, (byte)0x67, (byte)0x00, (byte)0x03, (byte)0xf8,
                        (byte)0x59, (byte)0xf0, (byte)0x06, (byte)0x0a, (byte)0x04, (byte)0x63,
                        (byte)0x68, (byte)0x69, (byte)0x00, (byte)0x49, (byte)0xe2, (byte)0xa4,
                        (byte)0xda, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff
                        , (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff
                        , (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
                        , (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff
                        , (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
                        ,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
                };
        byte[] pmtTableData2 = new byte[]
                {(byte)0x02 ,(byte)0xb0 ,(byte)0x28 ,(byte)0x00 ,(byte)0x13 ,(byte)0xc5 ,(byte)0x00 ,
                        (byte)0x00 ,(byte)0xe0 ,(byte)0x22 ,(byte)0xf0 ,(byte)0x00 ,(byte)0x04 ,(byte)0xe0 ,(byte)0x20 ,
                        (byte)0xf0 ,(byte)0x06 ,(byte)0x0a ,(byte)0x04 ,(byte)0x63 ,(byte)0x68 ,(byte)0x69 ,(byte)0x00 ,
                        (byte)0x04 ,(byte)0xe0

                        ,(byte)0x21 ,(byte)0xf0 ,(byte)0x06 ,(byte)0x0a ,(byte)0x04 ,(byte)0x65 ,
                        (byte)0x6e ,(byte)0x67 ,(byte)0x00 ,(byte)0x1b ,(byte)0xe0 ,(byte)0x22 ,(byte)0xf0 ,(byte)0x00 ,
                        (byte)0x68 ,(byte)0x6c ,(byte)0xb2 ,(byte)0xf9 ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff

                        ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff

                        ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff

                        ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff

                        ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff

                        ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,
                        (byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff ,(byte)0xff
                };
        Log.d(TAG, "PmtParsing PmtTableData1 Start @@@@@@@@@@@@@@@@");
        PmtData pmtData1 = new PmtData();
        pmtData1.parsing(pmtTableData1, pmtTableData1.length);
        mpmtdatalist.add(pmtData1);
        Log.d(TAG, "PmtParsing PmtTableData1 End @@@@@@@@@@@@@@@@");

        Log.d(TAG, "PmtParsing PmtTableData2 Start @@@@@@@@@@@@@@@@");
        PmtData pmtData2 = new PmtData();
        pmtData2.parsing(pmtTableData2, pmtTableData2.length);
        mpmtdatalist.add(pmtData2);
        Log.d(TAG, "PmtParsing PmtTableData2 End @@@@@@@@@@@@@@@@");

        totalNum = mpmtdatalist.size();
        Log.d(TAG,"Get Pmt Data TotalNumber = " + totalNum);
        if(totalNum > 0)
        {
            i = 0;
            for(PmtData temp_pmt : mpmtdatalist) {
                Log.d(TAG, "=======================================");
                Log.d(TAG, "Get Pmt Data " + (i + 1));
                int program_number = temp_pmt.getProgramMap().getProgram_number();
                Log.d(TAG, "program_number = " + program_number);
                int pcr_pid = temp_pmt.getProgramMap().getPcr_pid();
                Log.d(TAG, "pcr_pid = " + pcr_pid);
                int video_pid = temp_pmt.getProgramMap().getVideo_pid(0);
                Log.d(TAG, "video_pid = " + video_pid);
                int video_type = temp_pmt.getProgramMap().getVideo_stream_type(0);
                Log.d(TAG, "video_type = " + video_type);
                int audio_pid = temp_pmt.getProgramMap().getAudio_pid(0);
                Log.d(TAG, "audio_pid = " + audio_pid);
                int audio_type = temp_pmt.getProgramMap().getAudio_stream_type(0);
                Log.d(TAG, "audio_type = " + audio_type);
                String languageCode=temp_pmt.getProgramMap().getIso639LanguageCode1(0);
                Log.d(TAG,"languageCode = " + languageCode);
                int ttxt_desc_num = temp_pmt.getProgramMap().getTtxt_desc_number();
                Log.d(TAG, "ttxt_desc_num = " + ttxt_desc_num);
                i++;
            }
        }
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {

    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }
}