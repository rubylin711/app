package com.prime.dtvplayer.TestMiddleware;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.View.FingerPrintView;

/**
 * Created by scoty on 2018/2/23.
 */

public class TestTeletextDialogView extends Dialog{
    private static final String TAG="TeletextDialogView";
    private Context mContext;
    private TeletextInfo mTeletextInfo;
    private DTVActivity mDtv;
    private int mplayid;
    private boolean mRestartSubtitle = false;
    public static final int ttxDialogTypeAV = 1;
    public static final int ttxDialogTypePVR = 2;
    private Toast mToast;

    public TestTeletextDialogView(Context context, DTVActivity dtv, int playid)
    {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mContext = context;
        mDtv = dtv;
        mplayid = playid;

//        setCancelable(false);// disable click back button  // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        show();//show dialog
        setContentView(R.layout.test_txt_dialog_view);
        Window window = getWindow();//get dialog widow size
        if (window == null) {
            Log.d(TAG, "TeletextDialogView: window = null");
            return;
        }

        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        window.setAttributes(lp);//set dialog parameter
        int width =lp.width;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mTeletextInfo = mDtv.AvControlGetCurrentTeletext(mplayid);
        if (mTeletextInfo != null)
        {
            //Subtitle and teletext use the same SurfaceView to display.
            //When quit teletext,we should restore the original display state for subtitle.
            if (mDtv.AvControlIsSubtitleVisible(mplayid))
            {
                mDtv.AvControlShowSubtitle(mplayid,false);
                mRestartSubtitle = true;
            }
            mDtv.AvControlSetTeletextLanguage(mplayid,mTeletextInfo.getLangCode());
            mDtv.AvControlShowTeletext(mplayid,true);
            Log.d(TAG, "showTTX");




        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d(TAG, "TTX:KeyEvent.ACTION_DOWN:keyCode = " + keyCode + ".");

//        if ((SystemClock.elapsedRealtime() - mEnterTime) < KEY_EVENT_INTERVAL)
//        {
//            LogTool.d(LogTool.MPLAY, "TTX:KeyEvent.ACTION_DOWN:keyCode[bak/ttx] return");
//
//            if ((KeyEvent.KEYCODE_BACK != keyCode) && (KeyEvent.KEYCODE_TV_TELETEXT != keyCode))
//            {
//                return true;
//            }
//        }


        switch (keyCode)
        {
            case KeyEvent.KEYCODE_TV_TELETEXT:
            case ExtKeyboardDefine.KEYCODE_TV_TELETEXT:   // Johnny 20181210 for keyboard control
            {
                boolean result = mDtv.AvControlIsTeletextVisible(mplayid);
                //ShowToast("1234");
                String str="is teletext visible=" + result;

                Log.d(TAG, "is teletext visible="+result);


                new FingerPrintView(mContext,str, 6000,
                        22,0,500,500,300,100,Color.argb(255,255,255,255),Color.argb(255,0,255,0)).show();
            }break;
            case KeyEvent.KEYCODE_BACK:
            {
                Log.d(TAG, "DTV_KEYVALUE_TXT: showTTX(false)");

                mDtv.AvControlShowTeletext(mplayid,false);
                if (mRestartSubtitle)
                {
                    mDtv.AvControlShowSubtitle(mplayid,true);
                    mRestartSubtitle = false;
                }
//                this.dismiss();
            }break;
            case KeyEvent.KEYCODE_0:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_0.getValue());
            }break;
            case KeyEvent.KEYCODE_1:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_1.getValue());
            }break;
            case KeyEvent.KEYCODE_2:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_2.getValue());
            }break;
            case KeyEvent.KEYCODE_3:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_3.getValue());
            }break;
            case KeyEvent.KEYCODE_4:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_4.getValue());
            }break;
            case KeyEvent.KEYCODE_5:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_5.getValue());
            }break;
            case KeyEvent.KEYCODE_6:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_6.getValue());
            }break;
            case KeyEvent.KEYCODE_7:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_7.getValue());
            }break;
            case KeyEvent.KEYCODE_8:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_8.getValue());
            }break;
            case KeyEvent.KEYCODE_9:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_9.getValue());
            }break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_CHANNEL_UP:
            case KeyEvent.KEYCODE_PAGE_UP:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_PREVIOUS_PAGE.getValue());
            }break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
            case KeyEvent.KEYCODE_PAGE_DOWN:
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_NEXT_PAGE.getValue());
            }break;
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_RED.getValue());//page + 1
            }break;
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_GREEN.getValue());//page + 2
            }break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_YELLOW.getValue());//page + 3
            }break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                mDtv.AvControlSetTeletextCommand(mplayid,TTX.TTX_KEY_CYAN.getValue());//page + 4
            }break;
            default:
            {
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public enum TTX{
        TTX_KEY_0(0),
        TTX_KEY_1(1),
        TTX_KEY_2(2),
        TTX_KEY_3(3),
        TTX_KEY_4(4),
        TTX_KEY_5(5),
        TTX_KEY_6(6),
        TTX_KEY_7(7),
        TTX_KEY_8(8),
        TTX_KEY_9(9),
        TTX_KEY_PREVIOUS_PAGE(10),
        TTX_KEY_NEXT_PAGE(11),
        TTX_KEY_PREVIOUS_SUBPAGE(12),
        TTX_KEY_NEXT_SUBPAGE(13),
        TTX_KEY_PREVIOUS_MAGAZINE(14),
        TTX_KEY_NEXT_MAGAZINE(15),
        TTX_KEY_RED(16),
        TTX_KEY_GREEN(17),
        TTX_KEY_YELLOW(18),
        TTX_KEY_CYAN(19),
        TTX_KEY_INDEX(20),
        TTX_KEY_REVEAL(21),
        TTX_KEY_HOLD(22),
        TTX_KEY_MIX(23),
        TTX_KEY_UPDATE(24),
        TTX_KEY_ZOOM(25),
        TTX_KEY_SUBPAGE(26),
        TTX_KEY_BUTT(27);

        private int value;

        private TTX(int value){
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    private void ShowToast(String string)
    {
        if (mToast == null)
        {
            Log.d(TAG, "ShowToast: BK1");
            mToast = Toast.makeText(mContext, string, Toast.LENGTH_SHORT);
            Log.d(TAG, "ShowToast: BK2");
        }
        else
        {
            mToast.setText(string);
        }

        mToast.show();
    }
}
