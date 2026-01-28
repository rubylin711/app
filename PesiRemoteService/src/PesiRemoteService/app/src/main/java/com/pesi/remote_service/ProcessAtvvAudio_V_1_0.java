package com.pesi.remote_service;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ProcessAtvvAudio_V_1_0 extends ProcessAtvvAudio {
    private String TAG = "ProcessAtvvAudio_V_1_0" ;
    private boolean DEBUG = false ;
    static class WavPCMHeaderInfo
    {
        /*
        52 49 46 46 // RIFF
        34 61 00 00 // SIZE // total file size - 8 // 4 + ( fmt chunk 8(header+size) + size ) + ( data chunk( 8(header+size) + size )
        57 41 56 45 // WAVE
        class FMT // 24 bytes for PCM
        {
            66 6D 74 20 //FMT
            10 00 00 00 // FMT chunk SIZE // 16 for PCM.
            01 00 //FMT TAG // 0x0001	Microsoft PCM (uncompressed)
            01 00 //NumChannels
            80 3E 00 00 // SampleRate, EX 16000
            00 7D 00 00 // average bytes per second,  EX SampleRate * NumChannels * BitsPerSample/8
            02 00 // blockalign,  EX NumChannels * BitsPerSample/8
            10 00 // BitsPerSample    , EX 8 bits = 8, 16 bits = 16, etc.
        }
        class Data
        {
            64 61 74 61 // DATA
            00 00 00 00 // DATA chunk size // NumSamples * NumChannels * BitsPerSample/8
            // actual data
        }
         */
        static char RIFF[] = {'R','I','F','F'}; // 4 bytes RIFF
        static int filesize = 0; // 4 bytes // SIZE // total file size - 8 // 4 + ( fmt chunk 8(header+size) + size ) + ( data chunk( 8(header+size) + size )
        static char WAVE[] = {'W', 'A', 'V', 'E'}; // 4 bytes WAVE
        static class FMT { // 24 bytes
            static char FMT[] = {'f', 'm', 't', ' '}; // 4 bytes FMT
            static int  fmtChunkSize = 16 ;  // 4 bytes FMT chunk SIZE // 16 for PCM.
            static short fmttag = 0x0001; // 2 bytes 0x0001 Microsoft PCM (uncompressed)
            static short numChannels = 0x1; // 2 bytes NumChannels
            static int sampleRate = 16000; // 4 bytes SampleRate, EX 16000
            static int byterate = 32000;  // 4 bytes average bytes per second,  EX SampleRate * NumChannels * BitsPerSample/8
            static short blockalign = 2; // 2 bytes blockalign,  EX NumChannels * BitsPerSample/8
            static short bitsPerSample = 16; // 2 bytes BitsPerSample    , EX 8 bits = 8, 16 bits = 16, etc.
        }
        static class DATA { // 8 + N bytes
            static char DATA[] = {'d', 'a', 't', 'a'};
            static int dataChunkSize = 0 ;
            static char actualData[] ;
        }
    };

    public ProcessAtvvAudio_V_1_0(Context context, Handler uiHandler) {
        super(context, uiHandler);
    }

    @Override
    void processAtvvAudioAction(Message msg) {
        int caseId = msg.what;
        switch (caseId) {
            case ATVV_AUDIO_START:
                openSavedAdpcmFile();
                mRcuAudioDataByteFrameSize = msg.arg1 ;
                Log.d( TAG, "mRcuAudioDataByteFrameSize : " + mRcuAudioDataByteFrameSize ) ;
                mAdpcmUnit = new AdpcmUnit() ;
                break;
            case ATVV_AUDIO_STOP:
                convertAdpcmToPcm();
                mAUDIO_SYNC_DATA = null ;
                break;
            case ATVV_AUDIO_DATA:
                byte data[] = (byte[]) msg.obj;
                writeFile(mAdpcmFile, data);
                break;
            case ATVV_AUDIO_SYNC:
                byte sync_data[] = (byte[]) msg.obj;
//                Log.d(TAG, "AUDIO_SYNC sync_data.length " + sync_data.length ) ;
                if ( sync_data.length >= 7 )
                {
                    if ( mAUDIO_SYNC_DATA == null )
                    {
                        mAUDIO_SYNC_DATA = new AUDIO_SYNC_DATA() ;
                        mAUDIO_SYNC_DATA.add( 0, 0, (short) 0, 0 ) ;
                    }

                    int codec_usage = sync_data[1]; // 0x01: [0000_0001B] ADPCM, 8kHz/16bit; 0x02: [0000_0010B] ADPCM 16kHz/16bit.
                    int frame_number = (((sync_data[2] << 8) & 0xFF00) + (sync_data[3] & 0xFF));
                    short pred_value = (short) (((sync_data[4] << 8) & 0xFF00) + (sync_data[5] & 0xFF)); // Predicted ADPCM value
                    int step_index = sync_data[6]; // Index in ADPCM step size table.
//
                    mAUDIO_SYNC_DATA.add(codec_usage, frame_number, pred_value, step_index);
                    if ( DEBUG )
                    {
                        Log.d(TAG, "AUDIO_SYNC codec_usage " + codec_usage
                                + " frame_number " + frame_number
                                + " pred_value " + String.format("%04X", pred_value)
                                + " step_index " + String.format("%04X", step_index));
                    }
                }
                break;
        }
    }

    private void convertAdpcmToPcm()
    {
        if ( mAdpcmFile == null )
            return ;
        openSavedPcmFile() ;
        try {
            FileInputStream fis = new FileInputStream(mAdpcmFile);
            byte[] ADPCMbyteArray = new byte[(int) mAdpcmFile.length()];
            int adpcmBytesRead = fis.read(ADPCMbyteArray);
            byte pcmHeader[] = preparePcmFileHeader(adpcmBytesRead) ;
            writeFile(mPcmFile, pcmHeader ) ;
            Log.d( TAG, "adpcmBytesRead = " + adpcmBytesRead ) ;

            for( int i = 0, frame_num = 0 ; i < adpcmBytesRead ; i = i + mRcuAudioDataByteFrameSize, frame_num++ )
            {
                if ( frame_num % 20 == 0 ) // received ATVV sync data every 20 frames
                {
                    if (DEBUG)
                    {
                        Log.d(TAG, " calculate frame_num = " + frame_num
                                + " Pred_value = " + String.format("%04X", mAdpcmUnit.getPred_value())
                                + " prevStepSizeIndex = " + String.format("%04X", mAdpcmUnit.getPrevStepSizeIndex()));
                    }
                    int frame_num_correspond_index = frame_num / 20;
                    if ( mAUDIO_SYNC_DATA != null )
                    {
                        short pred_value = mAUDIO_SYNC_DATA.pred_value.get(frame_num_correspond_index);
                        int prevStepSizeIndex = mAUDIO_SYNC_DATA.step_index.get(frame_num_correspond_index);
                        mAdpcmUnit.setDecoderSyncData(prevStepSizeIndex, pred_value);
                        if (DEBUG)
                        {
                            Log.d(TAG, " sync frame_num = " + mAUDIO_SYNC_DATA.frame_number.get(frame_num_correspond_index)
                                    + " Pred_value = " + String.format("%04X", mAdpcmUnit.getPred_value())
                                    + " prevStepSizeIndex = " + String.format("%04X", mAdpcmUnit.getPrevStepSizeIndex()));
                        }
                    }
                }

                byte[] PCMByteArray = new byte[mRcuAudioDataByteFrameSize*4];
                int PCMByteArrayIndex = 0 ;
                for( int j = i ; j < (i + mRcuAudioDataByteFrameSize); j++ )
                {
                    // Top nibble (bits 4-7) is decoded first, followed by bottom nibble (bits 0-3).
                    byte topAdpcmNibble = (byte) ((ADPCMbyteArray[j] & 0xF0) >> 4);
                    byte bottomAdpcmNibble = (byte) (ADPCMbyteArray[j] & 0x0F);

                    short pcmDataShort = mAdpcmUnit.ADPCM_Decode(topAdpcmNibble) ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) (pcmDataShort & 0xFF);
                    PCMByteArrayIndex++ ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) ((pcmDataShort >> 8) & 0xFF);
                    PCMByteArrayIndex++ ;
                    pcmDataShort = mAdpcmUnit.ADPCM_Decode(bottomAdpcmNibble) ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) (pcmDataShort & 0xFF);
                    PCMByteArrayIndex++ ;
                    PCMByteArray[PCMByteArrayIndex] = (byte) ((pcmDataShort >> 8) & 0xFF);
                    PCMByteArrayIndex++ ;
                }

                writeFile(mPcmFile, PCMByteArray ) ;
            }

            fis.close();
            audioProcessFinish();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] preparePcmFileHeader( int adpcmBytes )
    {
        int pcmDataSize = adpcmBytes*4 ;
        WavPCMHeaderInfo.DATA.dataChunkSize = pcmDataSize ;
        WavPCMHeaderInfo.filesize = 4 + 24 + 8 + pcmDataSize ;
        byte pcmHeader[] = new byte[44] ;
        // RIFF
        pcmHeader[0] = (byte) WavPCMHeaderInfo.RIFF[0];
        pcmHeader[1] = (byte) WavPCMHeaderInfo.RIFF[1];
        pcmHeader[2] = (byte) WavPCMHeaderInfo.RIFF[2];
        pcmHeader[3] = (byte) WavPCMHeaderInfo.RIFF[3];

        // Total file size
        pcmHeader[4] = (byte) (WavPCMHeaderInfo.filesize & 0xFF);
        pcmHeader[5] = (byte) ((WavPCMHeaderInfo.filesize >> 8) & 0xFF);
        pcmHeader[6] = (byte) ((WavPCMHeaderInfo.filesize >> 16) & 0xFF);
        pcmHeader[7] = (byte) ((WavPCMHeaderInfo.filesize >> 24) & 0xFF);

        // WAVE
        pcmHeader[8] = (byte) WavPCMHeaderInfo.WAVE[0];
        pcmHeader[9] = (byte) WavPCMHeaderInfo.WAVE[1];
        pcmHeader[10] = (byte) WavPCMHeaderInfo.WAVE[2];
        pcmHeader[11] = (byte) WavPCMHeaderInfo.WAVE[3];

        {
            // FMT CHUNK
            pcmHeader[12] = (byte) WavPCMHeaderInfo.FMT.FMT[0];
            pcmHeader[13] = (byte) WavPCMHeaderInfo.FMT.FMT[1];
            pcmHeader[14] = (byte) WavPCMHeaderInfo.FMT.FMT[2];
            pcmHeader[15] = (byte) WavPCMHeaderInfo.FMT.FMT[3];

            // FMT chunk SIZE
            pcmHeader[16] = (byte) (WavPCMHeaderInfo.FMT.fmtChunkSize & 0xFF);
            pcmHeader[17] = (byte) ((WavPCMHeaderInfo.FMT.fmtChunkSize >> 8) & 0xFF);
            pcmHeader[18] = (byte) ((WavPCMHeaderInfo.FMT.fmtChunkSize >> 16) & 0xFF);
            pcmHeader[19] = (byte) ((WavPCMHeaderInfo.FMT.fmtChunkSize >> 24) & 0xFF);

            // AUDIO FORMAT
            pcmHeader[20] = (byte) (WavPCMHeaderInfo.FMT.fmttag & 0xFF);
            pcmHeader[21] = (byte) ((WavPCMHeaderInfo.FMT.fmttag >> 8) & 0xFF);

            // Num of channels
            pcmHeader[22] = (byte) (WavPCMHeaderInfo.FMT.numChannels & 0xFF);
            pcmHeader[23] = (byte) ((WavPCMHeaderInfo.FMT.numChannels >> 8) & 0xFF);

            // SampleRate
            pcmHeader[24] = (byte) (WavPCMHeaderInfo.FMT.sampleRate & 0xFF);
            pcmHeader[25] = (byte) ((WavPCMHeaderInfo.FMT.sampleRate >> 8) & 0xFF);
            pcmHeader[26] = (byte) ((WavPCMHeaderInfo.FMT.sampleRate >> 16) & 0xFF);
            pcmHeader[27] = (byte) ((WavPCMHeaderInfo.FMT.sampleRate >> 24) & 0xFF);

            // average bytes per second
            pcmHeader[28] = (byte) (WavPCMHeaderInfo.FMT.byterate & 0xFF);
            pcmHeader[29] = (byte) ((WavPCMHeaderInfo.FMT.byterate >> 8) & 0xFF);
            pcmHeader[30] = (byte) ((WavPCMHeaderInfo.FMT.byterate >> 16) & 0xFF);
            pcmHeader[31] = (byte) ((WavPCMHeaderInfo.FMT.byterate >> 24) & 0xFF);

            // block align
            pcmHeader[32] = (byte) (WavPCMHeaderInfo.FMT.blockalign & 0xFF);
            pcmHeader[33] = (byte) ((WavPCMHeaderInfo.FMT.blockalign >> 8) & 0xFF);

            // Bits Per Sample
            pcmHeader[34] = (byte) (WavPCMHeaderInfo.FMT.bitsPerSample & 0xFF);
            pcmHeader[35] = (byte) ((WavPCMHeaderInfo.FMT.bitsPerSample >> 8) & 0xFF);
        }

        {
            // DATA
            pcmHeader[36] = (byte) (WavPCMHeaderInfo.DATA.DATA[0]) ;
            pcmHeader[37] = (byte) (WavPCMHeaderInfo.DATA.DATA[1]) ;
            pcmHeader[38] = (byte) (WavPCMHeaderInfo.DATA.DATA[2]) ;
            pcmHeader[39] = (byte) (WavPCMHeaderInfo.DATA.DATA[3]) ;

            // pcm data size
            pcmHeader[40] = (byte) (pcmDataSize & 0xFF);
            pcmHeader[41] = (byte) ((pcmDataSize >> 8) & 0xFF);
            pcmHeader[42] = (byte) ((pcmDataSize >> 16) & 0xFF);
            pcmHeader[43] = (byte) ((pcmDataSize >> 24) & 0xFF);
        }

        return pcmHeader ;
    }
}
