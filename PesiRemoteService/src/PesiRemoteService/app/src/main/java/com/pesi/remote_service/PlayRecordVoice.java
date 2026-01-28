package com.pesi.remote_service;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

public class PlayRecordVoice {
    private static String TAG = "PlayRecordVoice" ;
    static AudioTrack mTrack = null;
    public static void initRecordAndTrack( int sampleRate ) {
        int bufferSize  = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        Log.d( TAG, " bufferSize = " + bufferSize + " minBufferSize = " + minBufferSize ) ;
    }

    public static void writeAudioData( byte data[] )
    {
        mTrack.write(data,0, data.length);
    }
    public static void startPlay()
    {
        mTrack.play();
    }
    public static void stopPlay()
    {
        mTrack.stop();
    }
}
