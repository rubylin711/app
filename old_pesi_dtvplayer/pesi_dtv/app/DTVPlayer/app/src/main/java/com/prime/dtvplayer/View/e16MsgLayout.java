package com.prime.dtvplayer.View;

import android.content.Context;
import android.graphics.Typeface;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.VMXProtectData;


public class e16MsgLayout extends ConstraintLayout {
    private final String TAG = getClass().getSimpleName();
    private TextView msgTop[] = new TextView[4];
    private TextView msgBot[] = new TextView[4];
    private ImageView imgBG;
    private DTVActivity mDTV;
    String strTop, strBot;
    private Context mContext;

    public e16MsgLayout(Context context) {
        super(context);
    }
    public e16MsgLayout(Context context, AttributeSet attrs) {
        this(context);
        mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        msgTop[0] = (TextView) findViewById(R.id.topMsg1TXV);
        msgTop[1] = (TextView) findViewById(R.id.topMsg2TXV);
        msgTop[2] = (TextView) findViewById(R.id.topMsg3TXV);
        msgTop[3] = (TextView) findViewById(R.id.topMsg4TXV);
        msgBot[0] = (TextView) findViewById(R.id.botMsg1TXV);
        msgBot[1] = (TextView) findViewById(R.id.botMsg2TXV);
        msgBot[2] = (TextView) findViewById(R.id.botMsg3TXV);
        msgBot[3] = (TextView) findViewById(R.id.botMsg4TXV);
        imgBG = (ImageView) findViewById(R.id.bgIGV);

    }

    public void SetVisibility( DTVActivity dtv, int visible)
    {
        Log.d(TAG, "ViewCAMsg: visible =" + visible );
        if (visible == View.VISIBLE) {
            //strTop = mContext.getResources().getString(R.string.STR_E16_TOP_TEXT);
            //strBot = mContext.getResources().getString(R.string.STR_E16_BOT_TEXT);
            VMXProtectData data = dtv.GetProtectData();
            strTop = data.getE16Top();
            strBot = data.getE16Bot();
            ShowE16Msg(dtv, strTop , msgTop);
            ShowE16Msg(dtv, strBot , msgBot);
        }
        else {

        }
        this.setVisibility(visible);
    }

    public void ShowE16Msg(DTVActivity dtv, String EWSstr, TextView msgView[])
    {
        int j = 0;
        String strID = dtv.VMXGetBoxID();
        String[] msg = new String[]{"", "", "", ""};

        Log.d(TAG, "EWSDialog:  str1.length() =" + EWSstr.length());

        int i = 0;
        while( i < EWSstr.length())
        {
            //Log.d(TAG, "ShowE16Msg:  i  = " + i);
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

        Log.d(TAG, "ShowE16Msg:  msg len = " + msg.length);
        for( int a = 0; a< msgView.length; a++)
        {
            if( a < msg.length) {
                msgView[a].setText(msg[a]);
                //Log.d(TAG, "ShowE16Msg:  msg [" + a+"]=" + msg[a]);
            }
        }
    }
}
