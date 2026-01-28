package com.prime.dtv.service.Test;

import android.content.Context;
import android.util.Log;

import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.Table.StreamType;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.TpInfo;

public class AvCmdTest {
    private static final String TAG = "AvCmdTest";
    private PesiDtvFrameworkInterface mPesiDtvFrameworkInterface = null;
    private Context mContext = null;

    public AvCmdTest(Context context, PesiDtvFrameworkInterface pesiDtvFrameworkInterface) {
        mContext = context;
        mPesiDtvFrameworkInterface = pesiDtvFrameworkInterface;
    }
    public void playAv() {
        // mux1 24 ABC
        DataManager dataManager = DataManager.getDataManager(mContext);
        ProgramInfo programInfo = new ProgramInfo();
        programInfo.setChannelId(111);
        programInfo.pVideo.setCodec(StreamType.STREAM_MPEG2_VIDEO);
        programInfo.pVideo.setPID(1249);
        ProgramInfo.AudioInfo audioInfo = new ProgramInfo.AudioInfo(1248, StreamType.STREAM_MPEG1_AUDIO, "zh", "zh");
        programInfo.pAudios.add(audioInfo);
        programInfo.setPcr(1249);
        TpInfo tp = new TpInfo(TpInfo.DVBC,dataManager.mTpInfoList.size(),0,1,482000,5200,TpInfo.Cable.QAM_256);
        programInfo.setTpId(tp.getTpId());
        dataManager.mTpInfoList.add(tp);
        dataManager.mProgramInfoList.add(programInfo);
        ProgramInfo pp2 = new ProgramInfo();
        pp2.setChannelId(222);
        pp2.pVideo.setCodec(StreamType.STREAM_MPEG2_VIDEO);
        pp2.pVideo.setPID(1019);
        ProgramInfo.AudioInfo audioInfo2 = new ProgramInfo.AudioInfo(1018, StreamType.STREAM_MPEG1_AUDIO, "zh", "zh");
        pp2.pAudios.add(audioInfo2);
        pp2.setPcr(1019);
        pp2.setTpId(tp.getTpId());
        dataManager.mProgramInfoList.add(pp2);
        for(int i = 0; i < dataManager.getProgramInfoList().size();i++) {
            ProgramInfo pp = dataManager.getProgramInfoList().get(i);
            Log.d(TAG,"programInfo = "+pp.ToString());
        }
//        mPesiDtvFrameworkInterface.setSurfaceHolder(this,holder);
//        Log.d(TAG,"holder.getSurface() = "+holder.getSurface());
        mPesiDtvFrameworkInterface.AvControlPlayByChannelId(0,programInfo.getChannelId(),programInfo.getType(),1);
//        mPlayer = new Player(this, holder.getSurface(), 0, mTuneInterface, programInfo, false);
    }
    public void channelUp() {
        DataManager dataManager = DataManager.getDataManager(mContext);
        ProgramInfo changeProgram = null;
//        for(int i = 0; i < dataManager.getProgramInfoList().size();i++) {
//            ProgramInfo programInfo = dataManager.getProgramInfoList().get(i);
//            Log.d(TAG,"KEYCODE_CHANNEL_UP programInfo = "+programInfo.ToString());
//        }
        for(int i = 0; i < dataManager.getProgramInfoList().size();i++) {
            ProgramInfo programInfo = dataManager.getProgramInfoList().get(i);
            if(programInfo.getChannelId() == dataManager.mGposInfo.getCurChannelId()) {
                int a = i==0? (dataManager.getProgramInfoList().size()-1) : (i-1);
                changeProgram = dataManager.getProgramInfoList().get(i==0? (dataManager.getProgramInfoList().size()-1) : (i-1));
                break;
            }
        }
        if(changeProgram != null) {
            System.out.println( "changeProgram=" + changeProgram.ToString() );
            mPesiDtvFrameworkInterface.AvControlPlayStop(0, 0, 1);
            mPesiDtvFrameworkInterface.AvControlPlayByChannelId(0,changeProgram.getChannelId(),changeProgram.getType(),1);
        }
    }
}
