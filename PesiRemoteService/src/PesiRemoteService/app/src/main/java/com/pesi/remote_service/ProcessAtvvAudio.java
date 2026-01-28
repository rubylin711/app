package com.pesi.remote_service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public abstract class  ProcessAtvvAudio extends Thread {
    private String TAG = "ProcessAtvvAudio" ;
    private Handler mHandler;
    private Handler mUiHandler;
    public Context mContext ;
    public static final int ATVV_AUDIO_START = 0x200 ;
    public static final int ATVV_AUDIO_STOP = 0x201 ;
    public static final int ATVV_AUDIO_DATA = 0x202 ;
    public static final int ATVV_AUDIO_SYNC = 0x203 ;
    File mAdpcmFile ;
    File mPcmFile ;
    public int mRcuAudioDataByteFrameSize = 0 ;
    public AdpcmUnit mAdpcmUnit ;
    AUDIO_SYNC_DATA mAUDIO_SYNC_DATA = null;
    class AUDIO_SYNC_DATA
    {
        ArrayList<Integer> codedec_usage = new ArrayList<>();
        ArrayList<Integer> frame_number = new ArrayList<>();
        ArrayList<Short> pred_value = new ArrayList<>();
        ArrayList<Integer> step_index = new ArrayList<>();
        public void add( int codec_usage, int frame_number, short pred_value, int step_index )
        {
            mAUDIO_SYNC_DATA.codedec_usage.add(codec_usage) ;
            mAUDIO_SYNC_DATA.frame_number.add(frame_number) ;
            mAUDIO_SYNC_DATA.pred_value.add(pred_value) ;
            mAUDIO_SYNC_DATA.step_index.add(step_index) ;
        }
    }

    @SuppressLint("HandlerLeak")
    public ProcessAtvvAudio(Context context, Handler uiHandler ) {
        mContext = context ;
        mUiHandler = uiHandler ;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                processAtvvAudioAction(msg);
            }
        };
    }

    public void openSavedAdpcmFile()
    {
        mAdpcmFile = new File(mContext.getExternalFilesDir(null) + "/AudioADPCM.bin") ;
        Log.d( TAG, "mAudioOutputFile[" + mAdpcmFile.getPath() + "]" ) ;
        try {
            mAdpcmFile.delete() ;
            mAdpcmFile.createNewFile() ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void openSavedPcmFile()
    {
        mPcmFile = new File(mContext.getExternalFilesDir(null) + "/AudioPCM.bin") ;
        Log.d( TAG, "mAudioOutputFile[" + mPcmFile.getPath() + "]" ) ;
        try {
            mPcmFile.delete();
            mPcmFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeFile(File file, byte[] data) {
        try {
//            Log.d( TAG, "ADPCM[" + data[0] + "] to PCM[" + ADPCM_Decode(data[0]) + "]");
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件写入失败：" + e.getMessage());
        }
    }

    public void sendMessage(int caseId, byte data[]) {
        sendMessage(caseId, data, 0 ) ;
    }
    public void sendMessage(int caseId, byte data[], int arg1 ) {
        Message message = mHandler.obtainMessage(caseId);
        message.obj = data ;
        message.arg1 = arg1 ;
        message.sendToTarget();
    }

    public void audioProcessFinish()
    {
        CommunicateToServer mCommunicateToServer = new CommunicateToServer(mContext, mUiHandler) ;
        mCommunicateToServer.execute(mPcmFile, "http://10.1.4.20:8080");
    }

    abstract void processAtvvAudioAction( Message msg ) ;
}
