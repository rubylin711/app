package com.pesi.remote_service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ProcessAtvvAudio_V_0_4 extends ProcessAtvvAudio {
    public ProcessAtvvAudio_V_0_4(Context context, Handler uiHandler) {
        super(context, uiHandler);
        PlayRecordVoice.initRecordAndTrack(8000);
        PlayRecordVoice.startPlay();
    }

    class ADPCMHeaderInfo
    {
        /*
        52 49 46 46 // RIFF
        34 61 00 00 // SIZE
        57 41 56 45 // WAVE
        {
        66 6D 74 20 //FMT
        14 00 00 00 // FORMAT or FMT BLOCK SIZE ?
        11 00 //FMT TAG
        01 00 //channels
        40 1F 00 00 // samplerates, EX 8000
        D7 0F 00 00 // average bytes per second,  EX samplerates X channels X 16 bits (2 bytes)    8000 * 2 * 1 = 16000
        00 01 // blockalign,  EX 16 bits (2 bytes) X channels   2 * 1 = 2 bytes
        04 00 // bits per sample, EX 16 bits
        02 00 // number of extra format bytes
        F9 01
        66 61 63 74 // fact
        04 00 00 00 // size for fact data
        00 BE 00 00 // fact data, total ADPCM data block nunber
        64 61 74 61 // DATA
        }

         */
        char RIFF[] = {'R','I','F','F'};
        int  filesize;
        char WAVE[] = {'W','A','V','E'};
        char FMT[] = {'f','m','t',' '};
//        int  format = 0x0011 ; // Intel DVI ADPCM (IMA ADPCM)
        int  fmttag = 0x0011 ; // Intel DVI ADPCM (IMA ADPCM)
        int  channels=0x1;
        int  samplerate = 8000;
        int  byterate = 16000;
        int  blockalign = 0x86;
        int  bitspersample = 16;
        int  blockinfo[] = {0x0002,0x105};
//        int sbSize = 2 ;
        char DATA[] = {'d','a','t','a'};
        int  sampledatabytes;
    };

    @Override
    void processAtvvAudioAction(Message msg) {
        int caseId = msg.what;
        switch (caseId) {
            case ATVV_AUDIO_START:
                openSavedAdpcmFile();
                mRcuAudioDataByteFrameSize = msg.arg1 ;
//                        mRxAudioDataArray = new byte[mRcuAudioDataByteFrameSize] ;
                mAdpcmUnit = new AdpcmUnit();
                break;
            case ATVV_AUDIO_STOP:
                convertAdpcmToPcm() ;
                break;
            case ATVV_AUDIO_DATA:
                byte data[] = (byte[]) msg.obj;
//                        arraycopy(data, 0, mRxAudioDataArray, mRxAudioDataCount, data.length) ;
//                        mRxAudioDataCount += data.length ;
//                        Log.d( "jimtest", "mRxAudioDataCount = " + mRxAudioDataCount ) ;
//                        if ( mRxAudioDataCount >= mRcuAudioDataByteFrameSize )
//                            mRxAudioDataCount = 0 ;
                writeFile(mAdpcmFile, data);
                break;
            case ATVV_AUDIO_SYNC:
                byte sync_data[] = (byte[]) msg.obj;
                int expectSize = (((sync_data[1]<<8)&0xFF00) + (sync_data[2]&0xFF))*mRcuAudioDataByteFrameSize ;
                Log.d( "jimtest", "Sync data " + (sync_data[1]&0xFF) + " " + (sync_data[2]&0xFF) + " expectSize = " + expectSize + " File size = " + mAdpcmFile.length() ) ;
                break;
        }
    }

    private void convertAdpcmToPcm()
    {
        if ( mAdpcmFile == null )
            return;
        try {
            ///////////////////// out PCM data //////////////////
            openSavedPcmFile();
            ///////////////////////////////////////

            ///////////////////// out PCM withour header data //////////////////
            File outputADPCMwithoutHeaderFile = new File(mContext.getExternalFilesDir(null) + "/AudiotADPCMwithoutHeader.bin") ;
            if ( outputADPCMwithoutHeaderFile.exists() )
                outputADPCMwithoutHeaderFile.delete();
            outputADPCMwithoutHeaderFile.createNewFile();
            ///////////////////////////////////////

            FileInputStream fis = new FileInputStream(mAdpcmFile);
            byte[] ADPCMbyteArray = new byte[(int) mAdpcmFile.length()];
            int bytesRead = fis.read(ADPCMbyteArray);
            Log.d( "jimtest", "bytesRead = " + bytesRead ) ;

            int audioTrackWriteByteCound = 0 ;
            for( int i = 0 ; i < bytesRead ; i = i + 134 )
            {
                int seq_num = ((ADPCMbyteArray[i]<<8)&0xFF00) + (ADPCMbyteArray[i+1]&0xFF) ;
                int rcu_id = ADPCMbyteArray[i+2]&0xFF ;
                short prev_pred = (short) (((ADPCMbyteArray[i+3]<<8)&0xFF00) + (ADPCMbyteArray[i+4]&0xFF));
                int index_into_step_size_table = (ADPCMbyteArray[i+5]&0xFF) ;
                Log.d( "jimtest", "byteArray[" + i + "] seq#[" + seq_num + "] "
                        + " rcu_id[" + rcu_id + "] "
                        + " prev_pred[" + String.format("%04X", prev_pred) + "] "
                        + " index_into_step_size_table[" + index_into_step_size_table + "] ") ;
                mAdpcmUnit.setDecoderSyncData(index_into_step_size_table, prev_pred);
                byte[] PCMByteArray = new byte[128*4];
                int PCMByteArrayIndex = 0 ;
                byte[] AdpcmWithoutHeaderArray = new byte[128] ;
                int AdpcmWithoutHeaderArrayIndex = 0 ;
                for( int j = i +6 ; j < (i + 134); j++ )
                {
                    AdpcmWithoutHeaderArray[AdpcmWithoutHeaderArrayIndex] = ADPCMbyteArray[j] ;
                    AdpcmWithoutHeaderArrayIndex++ ;

                    byte highAdpcmData = (byte) ((ADPCMbyteArray[j] & 0xF0) >> 4);
                    byte lowAdpcmData = (byte) (ADPCMbyteArray[j] & 0x0F);
//                    Log.d( "jimtest", "highAdpcmData = " + String.format("%02X", highAdpcmData) + " lowAdpcmData = " + String.format("%02X", lowAdpcmData) ) ;
                    short pcmDataInt = mAdpcmUnit.ADPCM_Decode(highAdpcmData) ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) (pcmDataInt & 0xFF);
                    PCMByteArrayIndex++ ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) ((pcmDataInt >> 8) & 0xFF);
                    PCMByteArrayIndex++ ;
                    pcmDataInt = mAdpcmUnit.ADPCM_Decode(lowAdpcmData) ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) (pcmDataInt & 0xFF);
                    PCMByteArrayIndex++ ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) ((pcmDataInt >> 8) & 0xFF);
                    PCMByteArrayIndex++ ;
                }

                PlayRecordVoice.writeAudioData(PCMByteArray);
//                Log.d("jimtest", "PCMByteArray " + PCMByteArray[0]);
                writeFile(mPcmFile, PCMByteArray ) ;
                writeFile(outputADPCMwithoutHeaderFile, AdpcmWithoutHeaderArray ) ;
            }
            fis.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
