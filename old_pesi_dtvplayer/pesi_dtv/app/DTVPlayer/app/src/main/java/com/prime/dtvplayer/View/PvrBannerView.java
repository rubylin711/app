package com.prime.dtvplayer.View;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolphin.dtv.EnPVRPlayStatus;
import com.dolphin.dtv.EnPVRTimeShiftStatus;
import com.dolphin.dtv.EnTrickMode;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.PvrInfo;


public class PvrBannerView extends ConstraintLayout {
    private static final String TAG = "PvrBannerView";

    private TextView curTime,totalTime, pvrType, pvrSpeed;
    private ImageView bannerbg, pvrSymbol;
    private ProgressBar pvrTimeBar;
    private int visibility = View.INVISIBLE;

    public PvrBannerView(Context context) {
        super(context);
    }
    public PvrBannerView(Context context, AttributeSet attrs) {
        this(context);
    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "onFinishInflate");
        super.onFinishInflate();

        bannerbg = (ImageView) findViewById(R.id.pvrBannerbgIGV);
        pvrType = (TextView) findViewById(R.id.pvrTypeTXV);
        pvrSpeed = (TextView) findViewById(R.id.pvrSpeedTXV);
        pvrSymbol = (ImageView) findViewById(R.id.pvrSymbolTXV);
        curTime = (TextView) findViewById(R.id.curTimeTXV);
        pvrTimeBar = (ProgressBar) findViewById(R.id.PvrTimeProgBar);
        totalTime = (TextView) findViewById(R.id.totalTimeTXV);

        int barcolor =Color.GREEN;
        pvrTimeBar.setProgressTintList(ColorStateList.valueOf(barcolor));
    }

    public void setVisibility( DTVActivity mdtv, int visible, int Duration, boolean focusPip)//Scoty 20180802 fixed cur time not update when focus on Pip
    {
        bannerbg.setVisibility(visible);
        pvrType.setVisibility(visible);
        curTime.setVisibility(visible);
        totalTime.setVisibility(visible);
        pvrTimeBar.setVisibility(visible);
        visibility = visible;
        this.setVisibility(visible);
        if(visibility == VISIBLE)
        {
            int RecTime;
            int Hour,Min, Sec;
            int Progress;
            int curPvrMode = -1;

            if(focusPip)//Scoty 20180802 fixed cur time not update when focus on Pip
                curPvrMode = mdtv.getCurrentPipPvrMode();
            else
                curPvrMode = mdtv.getCurrentPvrMode();

            if ( curPvrMode == PvrInfo.EnPVRMode.PLAY_RECORD_FILE )//if ( (mdtv.PvrGetCurrentRecMode() == NO_ACTION) && (mdtv.PvrGetCurrentPlayMode() == FILE_PLAY) )
            {

                Log.d(TAG, "PVRActivity: setVisibility: PLAY");

                pvrType.setVisibility(INVISIBLE);
                pvrSymbol.setVisibility(VISIBLE);
                pvrSpeed.setVisibility(VISIBLE);

                int playStatus = mdtv.PvrPlayGetCurrentStatus();
                int playTime = mdtv.PvrPlayGetPlayTime();
                int speed = mdtv.PvrPlayGetCurrentTrickMode().getValue() / 1024;

                switch (playStatus)
                {
                    case EnPVRPlayStatus.PLAY:
                        setTrickSymbol(EnPVRTimeShiftStatus.PLAY);
                        pvrSpeed.setVisibility(INVISIBLE); // edwin 20180626 remove 1X
                        break;

                    case EnPVRPlayStatus.PAUSE:
                        setTrickSymbol(EnPVRTimeShiftStatus.PAUSE);
                        pvrSpeed.setVisibility(INVISIBLE); // edwin 20180626 remove 1X
                        break;

                    case EnPVRPlayStatus.FAST_FORWARD:
                        setTrickSymbol(EnPVRTimeShiftStatus.FAST_FORWARD);
                        break;

                    case EnPVRPlayStatus.FAST_BACKWARD:
                        setTrickSymbol(EnPVRTimeShiftStatus.FAST_BACKWARD);
                        break;
                }

                totalTime.setText(setTime(Duration));
                curTime.setText(setTime(playTime));
                pvrTimeBar.setMax(Duration);
                pvrTimeBar.setProgress(playTime);
                pvrSpeed.setText(String.valueOf(speed).concat(mdtv.getString(R.string.STR_X))); // edwin 20180622 lack of speed
            }
            else if( curPvrMode == PvrInfo.EnPVRMode.RECORD)//else if(mdtv.PvrGetCurrentRecMode() == REC)
            {
                int recId = mdtv.PvrRecordCheck(mdtv.ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
                pvrType.setVisibility(View.VISIBLE);
                pvrSpeed.setVisibility(View.INVISIBLE);
                pvrSymbol.setVisibility(View.INVISIBLE);
                //Log.d(TAG, "setVisibility:  Duration = " + Duration);
                String TotalTime = setTime(Duration);
                totalTime.setText(TotalTime);

                RecTime = mdtv.PvrRecordGetAlreadyRecTime(mdtv.ViewHistory.getPlayId(), recId);//Scoty 20180809 modify dual pvr rule
                String CurTime = setTime(RecTime);
                curTime.setText(CurTime);
                //Log.d(TAG, "updateTimeStatus: CurTime ="+CurTime);
                pvrTimeBar.setProgress((RecTime/Duration)*100);
                //Log.d(TAG, "setVisibility:   RecTime/Duration)*100 = "  + (RecTime/Duration)*100);
            }
            else if( curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE || curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE ||
                    curPvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE) // TimeShift Mode //Scoty 20180827 add and modify TimeShift Live Mode
            {
                pvrType.setVisibility(View.INVISIBLE);
                pvrSpeed.setVisibility(View.VISIBLE);
                pvrSymbol.setVisibility(View.VISIBLE);

                if (curPvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)//TimeShift 
                {
                    //Scoty 20180827 add and modify TimeShift Live Mode -s
                    int curPlayTIme = mdtv.pvrRecordGetLivePauseTime();//mdtv.PvrTimeShiftGetPlaySecond(mdtv.ViewHistory.getPlayId());
                    if(curPlayTIme < 0)
                        curPlayTIme = 0;
                    String CurTimeShiftPlayTime = setTime(curPlayTIme),speedStr = "";//Scoty 20180622 reset speed text when start timeshift
                    curTime.setText(CurTimeShiftPlayTime);

                    RecTime = mdtv.PvrTimeShiftGetRecordTime(mdtv.ViewHistory.getPlayId());
                    String CurTimeShiftTime = setTime(RecTime);
                    totalTime.setText(CurTimeShiftTime);

                    pvrSpeed.setText(speedStr);//Scoty 20180622 reset speed text when start timeshift
                    pvrSymbol.setBackgroundResource(R.drawable.pvr_banner_pause);

                    //Set Play Progress
                    if(curPlayTIme > 0)//Scoty 20181106 fixed crash ,press pause key when recording to stop record and start Timeshift
                        Progress = curPlayTIme*100/RecTime;//eric lin 20180802 fix timeshift file mode progress bar not show,-start
                    else
                        Progress = 0;
                    if(Progress > 100)
                        Progress = 100;
                    pvrTimeBar.setProgress(Progress);//eric lin 20180802 fix timeshift file mode progress bar not show,-end
                    //Scoty 20180827 add and modify TimeShift Live Mode -e
                }
//                else if(mdtv.PvrGetCurrentPlayMode() == EnPVRPlayMode.FILE_PLAY) // edwin 20180612
//                {
//                    //Set Play Speed
//                    EnTrickMode enTrick = mdtv.PvrPlayGetCurrentTrickMode();
//                    int speed = enTrick.getValue()/1024;
//                    String speedStr = "";
//
//                    if(speed != -1 && speed != 1)
//                    {
//                        speedStr = String.valueOf(speed+"X");
//                    }
//                    pvrSpeed.setText(speedStr);
//                }
                else if(curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE)//TimeShift Play or Live Mode //Scoty 20180827 add and modify TimeShift Live Mode
                {
                    Log.d(TAG, "setVisibility: TIMESHIFT_PLAY");
                    //Set Play Symbol
                    int status = mdtv.PvrTimeShiftGetStatus(mdtv.ViewHistory.getPlayId());
                    setTrickSymbol(status);

                    //Set Play Speed
                    EnTrickMode enTrick = mdtv.PvrTimeShiftGetCurrentTrickMode(mdtv.ViewHistory.getPlayId());
                    int speed = enTrick.getValue()/1024;
                    String speedStr = "";
                    if(speed != -1 && speed != 1)
                    {
                        speedStr = String.valueOf(speed+"X");
                    }
                    pvrSpeed.setText(speedStr);

                    //Set Play Time
                    int curPlayTIme = mdtv.PvrTimeShiftGetPlaySecond(mdtv.ViewHistory.getPlayId());
                    if(curPlayTIme < 0)//Scoty 20180827 add and modify TimeShift Live Mode
                        return;
                    String CurTimeShiftPlayTime = setTime(curPlayTIme);
                    curTime.setText(CurTimeShiftPlayTime);

                    //Set REC Time
                    RecTime = mdtv.PvrTimeShiftGetRecordTime(mdtv.ViewHistory.getPlayId());
                    String CurTimeShiftTime = setTime(RecTime);
                    totalTime.setText(CurTimeShiftTime);

                    //Set Play Progress                    
                    Progress = curPlayTIme*100/RecTime;//eric lin 20180802 fix timeshift file mode progress bar not show,-start
                    if(Progress > 100)
                        Progress = 100;
                    pvrTimeBar.setProgress(Progress);//eric lin 20180802 fix timeshift file mode progress bar not show,-end
                }
                else
                    setVisibility(View.INVISIBLE);//Scoty 20180827 add and modify TimeShift Live Mode

            }
        }
    }

    public int getVisibility()
    {
        return visibility == View.VISIBLE ? View.VISIBLE:View.INVISIBLE ;
    }

    public void updateTimeStatus(DTVActivity mdtv, int totalSec, boolean focusPip)//Scoty 20180802 fixed cur time not update when focus on Pip
    {
        int RecTime;
        int Hour,Min, Sec;
        int Progress;
        String str;
        int curPvrMode = -1;

        if(focusPip)//Scoty 20180802 fixed cur time not update when focus on Pip
            curPvrMode = mdtv.getCurrentPipPvrMode();
        else
            curPvrMode = mdtv.getCurrentPvrMode();
        if(curPvrMode == PvrInfo.EnPVRMode.PLAY_RECORD_FILE)//if ( (mdtv.PvrGetCurrentRecMode() == NO_ACTION) && (mdtv.PvrGetCurrentPlayMode() == FILE_PLAY) )
        {
            Log.d(TAG, "PVRActivity: updateTimeStatus: FILE PLAY");

            pvrType.setVisibility(INVISIBLE);
            pvrSymbol.setVisibility(VISIBLE);
            pvrSpeed.setVisibility(VISIBLE);

            int playStatus = mdtv.PvrPlayGetCurrentStatus();
            int playTime = mdtv.PvrPlayGetPlayTime();
            int speed = mdtv.PvrPlayGetCurrentTrickMode().getValue() / 1024;

            switch (playStatus)
            {
                case EnPVRPlayStatus.PLAY:
                    setTrickSymbol(EnPVRTimeShiftStatus.PLAY);
                    pvrSpeed.setVisibility(INVISIBLE); // edwin 20180626 remove 1X
                    break;

                case EnPVRPlayStatus.PAUSE:
                    setTrickSymbol(EnPVRTimeShiftStatus.PAUSE);
                    pvrSpeed.setVisibility(INVISIBLE); // edwin 20180626 remove 1X
                    break;

                case EnPVRPlayStatus.FAST_FORWARD:
                    setTrickSymbol(EnPVRTimeShiftStatus.FAST_FORWARD);
                    break;

                case EnPVRPlayStatus.FAST_BACKWARD:
                    setTrickSymbol(EnPVRTimeShiftStatus.FAST_BACKWARD);
                    break;
            }

            totalTime.setText(setTime(totalSec)); //edwin 20180622 patch the lack of totalTime
            curTime.setText(setTime(playTime));
            pvrTimeBar.setProgress(playTime);
            pvrTimeBar.setMax(totalSec);
            pvrSpeed.setText(String.valueOf(speed).concat(mdtv.getString(R.string.STR_X)));
        }
        else if(curPvrMode == PvrInfo.EnPVRMode.RECORD)//else if(mdtv.PvrGetCurrentRecMode() == REC)
        {
            int recId = mdtv.PvrRecordCheck(mdtv.ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
            pvrType.setVisibility(View.VISIBLE);
            pvrSpeed.setVisibility(View.INVISIBLE);
            pvrSymbol.setVisibility(View.INVISIBLE);

            RecTime = mdtv.PvrRecordGetAlreadyRecTime(mdtv.ViewHistory.getPlayId(), recId);//Scoty 20180809 modify dual pvr rule
            String CurTime = setTime(RecTime);
            curTime.setText(CurTime);
            Progress = RecTime*100/totalSec;//eric lin 20180802 fix timeshift file mode progress bar not show,-start
            if(Progress > 100)
                Progress = 100;
            pvrTimeBar.setProgress(Progress);//eric lin 20180802 fix timeshift file mode progress bar not show,-end
            Log.d(TAG, "updateTimeStatus: ====>"+ Progress);
        }
        else if(curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE || curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE ||
                curPvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)// TimeShift Mode //Scoty 20180827 add and modify TimeShift Live Mode
        {
            pvrType.setVisibility(View.INVISIBLE);
            pvrSpeed.setVisibility(View.VISIBLE);
            pvrSymbol.setVisibility(View.VISIBLE);

            if (curPvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)//TimeShift //Scoty 20180827 add and modify TimeShift Live Mode
            {
                //Scoty 20180827 add and modify TimeShift Live Mode -s
                int curPlayTIme = mdtv.pvrRecordGetLivePauseTime();
                if(curPlayTIme < 0)
                    curPlayTIme = 0;
                String CurTimeShiftPlayTime = setTime(curPlayTIme),speedStr = "";//Scoty 20180622 reset speed text when start timeshift
                curTime.setText(CurTimeShiftPlayTime);

                RecTime = mdtv.PvrTimeShiftGetRecordTime(mdtv.ViewHistory.getPlayId());
                String CurTimeShiftTime = setTime(RecTime);//,speedStr = "";//Scoty 20180622 reset speed text when start timeshift
                pvrSpeed.setText(speedStr);//Scoty 20180622 reset speed text when start timeshift
                totalTime.setText(CurTimeShiftTime);

                if(curPlayTIme > 0)//Scoty 20181106 fixed crash ,press pause key when recording to stop record and start Timeshift
                    Progress = curPlayTIme*100/RecTime;//eric lin 20180802 fix timeshift file mode progress bar not show,-start
                else
                    Progress = 0;
                if(Progress > 100)
                    Progress = 100;
                pvrTimeBar.setProgress(Progress);//eric lin 20180802 fix timeshift file mode progress bar not show,-end
                //Scoty 20180827 add and modify TimeShift Live Mode -e
            }
            else if(curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE)//TimeShift Play or Live Mode  //Scoty 20180827 add and modify TimeShift Live Mode
            {
                //Set Play Symbol
                int status = mdtv.PvrTimeShiftGetStatus(mdtv.ViewHistory.getPlayId());
                setTrickSymbol(status);

                //Set Play Speed
                EnTrickMode enTrick = mdtv.PvrTimeShiftGetCurrentTrickMode(mdtv.ViewHistory.getPlayId());
                int speed = enTrick.getValue()/1024;
                String speedStr = "";
                if(speed != -1 && speed != 1)
                {
                    speedStr = String.valueOf(speed+"X");
                }
                pvrSpeed.setText(speedStr);

                //Set Play Time
                int curPlayTIme = mdtv.PvrTimeShiftGetPlaySecond(mdtv.ViewHistory.getPlayId());
                if(curPlayTIme < 0)//Scoty 20180827 add and modify TimeShift Live Mode
                    return;
                String CurTimeShiftPlayTime = setTime(curPlayTIme);
                curTime.setText(CurTimeShiftPlayTime);

                //Set REC Time
                RecTime = mdtv.PvrTimeShiftGetRecordTime(mdtv.ViewHistory.getPlayId());
                String CurTimeShiftTime = setTime(RecTime);
                totalTime.setText(CurTimeShiftTime);

                //Set Play Progress                
                Progress = curPlayTIme*100/RecTime;//eric lin 20180802 fix timeshift file mode progress bar not show,-start
                if(Progress > 100)
                    Progress = 100;
                pvrTimeBar.setProgress(Progress);//eric lin 20180802 fix timeshift file mode progress bar not show,-end                
            }
            else
                setVisibility(View.INVISIBLE);//Scoty 20180827 add and modify TimeShift Live Mode
        }
    }

    private String setTime(int inputTime)
    {
        int Hour = inputTime/60/60;
        int Min = (inputTime-(Hour*60*60))/60;
        int Sec = inputTime-(Hour*60*60)-(Min*60);
        String Time = String.format("%02d", Hour) + " : " + String.format("%02d", Min)
                + " : " +     String.format("%02d", Sec);

        return Time;
    }

    private void setTrickSymbol(int status)
    {
        if(status == EnPVRTimeShiftStatus.PLAY)
        {
            pvrSymbol.setBackgroundResource(R.drawable.pvr_banner_play);
        }
        else if(status == EnPVRTimeShiftStatus.PAUSE)
        {
            pvrSymbol.setBackgroundResource(R.drawable.pvr_banner_pause);
        }
        else if(status == EnPVRTimeShiftStatus.FAST_FORWARD)
        {
            pvrSymbol.setBackgroundResource(R.drawable.pvr_banner_ff);
        }
        else if(status == EnPVRTimeShiftStatus.FAST_BACKWARD)
        {
            pvrSymbol.setBackgroundResource(R.drawable.pvr_banner_fb);
        }
    }
}
