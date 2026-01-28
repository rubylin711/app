package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.VMXProtectData;

abstract public class EWSDialog extends Dialog {
    private final String TAG = "EWSDialog";
    private Context mContext;
    private DTVActivity mdtv;
    private TextView msgTop[] = new TextView[4];
    private TextView msgBot[] = new TextView[4];
    private String strTop, strBot;
    private int mLevel = 0;
    public EWSDialog (DTVActivity dtvActivity, Context context, int level, int mode) {//Scoty 20181225 modify VMX EWBS rule
        super(context);
        Log.d(TAG, "EWSDialog: ");
        mdtv = dtvActivity;
        mContext = context;
        mLevel = level;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(mode == 1) {
            setContentView(R.layout.ews_dialog);

            WindowManager.LayoutParams wlp = this.getWindow().getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

            msgTop[0] = (TextView) findViewById(R.id.ewsMsg1TXV);
            msgTop[1] = (TextView) findViewById(R.id.ewsMsg2TXV);
            msgTop[2] = (TextView) findViewById(R.id.ewsMsg3TXV);
            msgTop[3] = (TextView) findViewById(R.id.ewsMsg4TXV);
            msgBot[0] = (TextView) findViewById(R.id.ewsMsg5TXV);
            msgBot[1] = (TextView) findViewById(R.id.ewsMsg6TXV);
            msgBot[2] = (TextView) findViewById(R.id.ewsMsg7TXV);
            msgBot[3] = (TextView) findViewById(R.id.ewsMsg8TXV);

            UpdateEWBSMsg();
        }
        else
        {
            setContentView(R.layout.ews_empty_dialog);
            WindowManager.LayoutParams wlp = this.getWindow().getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        }
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void onStop() {
        mdtv.VMXStopEWBS(1);//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add VMX EWBS mode
        //mdtv.MtestSetBuzzer(0);//buzzer off
    }

    @Override
    public void dismiss() {
        dialogEnd();
        super.dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        Log.d(TAG, "onKeyDown: ");
        return super.onKeyDown(keyCode, event);
    }

    public void UpdateEWBSMsg()
    {
        GetEWBSMsg(mLevel);
        //mdtv.MtestSetBuzzer(1);//buzzer on
        ShowEWSMsg(strTop , msgTop);
        ShowEWSMsg(strBot , msgBot);
    }

    public void GetEWBSMsg( int level)
    {
        VMXProtectData protectData = mdtv.GetProtectData();
        if(level == 0) {
            strTop = protectData.getEWBS0Top();
            strBot = protectData.getEWBS0Bot();
            //strTop = mContext.getResources().getString(R.string.STR_EWBS0_TOP_TEXT);
            //strBot = mContext.getResources().getString(R.string.STR_EWBS0_BOT_TEXT);
        }
        else
        {
            strTop = protectData.getEWBS1Top();
            strBot = protectData.getEWBS1Bot();
            //strTop = mContext.getResources().getString(R.string.STR_EWBS1_TOP_TEXT);
            //strBot = mContext.getResources().getString(R.string.STR_EWBS1_BOT_TEXT);
        }
        Log.d(TAG, "GetEWBSMsg: strTop =" + strTop + "     strBot = " + strBot);

    }

    public void ShowEWSMsg(String EWSstr, TextView msgView[])
    {
        int j = 0;
        String strID = mdtv.VMXGetBoxID();
        String[] msg = new String[]{"", "", "", ""};

        //Log.d(TAG, "EWSDialog:  str1.length() =" + EWSstr.length());

        int i = 0;
        while( i < EWSstr.length())
        {
            if(EWSstr.charAt(i) == '*')
            {
                j++;
                i++;
                continue;
            }
            else if(EWSstr.charAt(i) == '#')
            {
                if(i + 2 < EWSstr.length())
                {
                    if( EWSstr.charAt(i+1) == 'B' && EWSstr.charAt(i+2) == '#')
                    {
                        msgView[j].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        i=i+3;
                        continue;
                    }
                }
                if(i + 3 < EWSstr.length())
                {
                    if( EWSstr.charAt(i+1) == 'I' && EWSstr.charAt(i+2) == 'D' && EWSstr.charAt(i+3) == '#')
                    {
                        msg[j] = msg[j]+ strID;
                        i=i+4;
                        continue;
                    }
                }
            }

            msg[j] = msg[j] + EWSstr.charAt(i);
            i++;
        }

        Log.d(TAG, "ParsingEWSMsg:  msg len = " + msg.length);
        for( int a = 0; a< msgView.length; a++)
        {
            if( a < msg.length) {
                msgView[a].setText(msg[a]);
                //Log.d(TAG, "ParsingEWSMsg:  msg [" + a+"]=" + msg[a]);
            }
        }
    }

    abstract public void dialogEnd();
}