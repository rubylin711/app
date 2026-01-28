package com.prime.dtvplayer.View;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.prime.dtvplayer.R;

import java.lang.reflect.Method;
import java.util.Locale;


public abstract class RecDurationSettingView extends Dialog {
    private static final String TAG="RecDurationSettingView";
    private Context mContext = null;
    private Button /*duration,*/ okBtn;
    private EditText duration;
    private int durationHour, durationMin;
    private int durationValue = 1*60*60;//1 Hour

    @SuppressLint("ClickableViewAccessibility")
    protected RecDurationSettingView(Context context)
    {
        super(context);
        mContext = context;

//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        //show();//show dialog // Edwin 20190508 fix dialog not focus
        setContentView(R.layout.rec_duration_setting_dialog);
        Window window = getWindow();//get dialog widow size
        if (window == null) {
            Log.d(TAG, "RecDurationSettingView: window = null");
            return;
        }
        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp
        lp.dimAmount=0.0f;
        window.setAttributes(lp);//set dialog parameter
        int width =lp.width;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.CENTER);

        okBtn = (Button) window.findViewById(R.id.okBTN);
        duration = (EditText) window.findViewById(R.id.durationEDV);

        // Edwin 20190509 disable keyboard(soft input) -s
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
                    boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(duration, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Edwin 20190509 disable keyboard(soft input) -e

        duration.setText("01:00");
        durationHour = 1;
        durationMin = 0;

        duration.setOnKeyListener(new View.OnKeyListener(){ // connie 20180731 for use EditText to modify start time & duration-s
            public boolean onKey(View v, int keyCode, KeyEvent event){
                boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
                if (isActionDown)
                {
                    Log.d(TAG, "onKey:  keyCode = " + keyCode);
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            if(duration.getText().length()==5)
                                okBtn.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            okBtn.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_DEL:
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            deleteEditText(duration);
                            return true;

                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            if(duration.getText().length() < 3)
                                return true;
                            break;

                        case KeyEvent.KEYCODE_0:
                        case KeyEvent.KEYCODE_1:
                        case KeyEvent.KEYCODE_2:
                        case KeyEvent.KEYCODE_3:
                        case KeyEvent.KEYCODE_4:
                        case KeyEvent.KEYCODE_5:
                        case KeyEvent.KEYCODE_6:
                        case KeyEvent.KEYCODE_7:
                        case KeyEvent.KEYCODE_8:
                        case KeyEvent.KEYCODE_9:
                            Log.d(TAG, "onKey:  aaa" + duration.getSelectionStart() + "     " + duration.getSelectionEnd());
                            if(duration.getSelectionStart() == 0 && duration.getSelectionEnd() == 5) // select all
                                duration.setText(":");
                            String input = Integer.toString(keyCode-7);
                            insertEditText(duration, input);

                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        duration.setOnFocusChangeListener(duration_change_focus);// connie 20180731 for use EditText to modify start time & duration-e
        /*
        duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(mContext, R.style.TimePickerDialogStyle, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //Log.d(TAG,"new TimePickerDialog: selectedHour="+selectedHour+", selectedMinute="+selectedMinute );
                        durationHour = selectedHour;
                        durationMin = selectedMinute;
                        if(durationHour==0&&durationMin==0)
                            durationMin=1;
                        else
                            durationValue = durationHour*60*60+durationMin*60;
                        String durTime = String.format(Locale.ENGLISH, "%02d:%02d",durationHour,durationMin);
                        duration.setText(durTime);
                    }
                }, durationHour, durationMin, true);
                mTimePicker.show();
            }
        });
        */

        //Scoty 20180918 replace setOnClickListener to setOnKeyListener in order to skip audio_hal: pcm_open card: -s
        okBtn.setOnKeyListener(new Button.OnKeyListener() {
               @Override
               public boolean onKey(View v, int keyCode, KeyEvent event) {
                   if(event.getAction() == KeyEvent.ACTION_DOWN) {
                       switch (keyCode) {
                           case KeyEvent.KEYCODE_DPAD_CENTER:
                               if (v.hasFocus()) {
                                   dismiss();
                                   onSetPositiveButton(durationValue);
                               }
                               break;
                       }
                   }
                   return false;
               }
           }
        );

//        okBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//                onSetPositiveButton(durationValue);
//            }
//        });
        //Scoty 20180918 replace setOnClickListener to setOnKeyListener in order to skip audio_hal: pcm_open card: -e

        // Johnny 20181219 for mouse control -s
        okBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    dismiss();
                    onSetPositiveButton(durationValue);
                }

                return false;
            }
        });
        // Johnny 20181219 for mouse control -e
    }

    private View.OnFocusChangeListener duration_change_focus = new View.OnFocusChangeListener() {// connie 20180731 for use EditText to modify start time & duration-s
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                duration.selectAll();
            }
            else {
                String NewStr = duration.getText().toString();
                Log.d(TAG, "onFocusChange:  NewStr = " + NewStr);
                int hour, min;
                if(NewStr.length() == 5 && NewStr.indexOf(":") == 2) {  // Johnny 20181219 for mouse control, ":" must be the 3rd char
                    hour = Integer.valueOf(NewStr.substring(0,2));
                    min = Integer.valueOf(NewStr.substring(3,5));
                    if(hour <= 23 && min <= 59) {
                        durationHour = hour;
                        durationMin = min;
                        durationValue = durationHour*60*60+durationMin*60;
                        Log.d(TAG, "onFocusChange: durationHour = "+durationHour + "    durationMin"+durationMin);
                        return;
                    }
                }

                NewStr = String.format(Locale.getDefault(), "%02d:%02d",durationHour,durationMin);
                Log.d(TAG, "onFocusChange: durationHour ="+durationHour + "    durationMin = "+durationMin);
                duration.setText(NewStr);
            }
        }
    };
    private boolean deleteEditText(EditText et) {
        int position = et.getSelectionStart();
        String curStr = String.valueOf(et.getText());
        Log.d(TAG, "deleteEditText: curStr =" + curStr + "    position="+position);
        Editable editable = et.getText();

        if (position > 0 && curStr.length() > 1 ) {
            Log.d(TAG, "deleteEditText:  curStr.charAt( " + (position-1)+") =" + curStr.charAt(position-1));
            if( curStr.charAt(position-1)== ':') {
                et.setSelection(position - 1);
                position = et.getSelectionStart();
            }
            editable.delete(position - 1, position);
            return true;
        }
        else {
            return false; //delete fail
        }
    }
    private boolean insertEditText(EditText et, String input) {
        int position = et.getSelectionStart();
        String curStr = String.valueOf(et.getText());
        Log.d(TAG, "insertEditText: curStr =" + curStr + "    position="+position);
        Editable editable = et.getText();

        if (position >= 0  && curStr.length() < 6 ) {
            if( curStr.length() == 2 ) {
                editable.insert(position, input);
                et.setSelection(3);
                Log.d(TAG, "insertEditText:  getSelection = " + et.getSelectionStart());
            }
            else
                editable.insert(position, input);

            return true;
        }
        else {
            return false; //delete fail
        }
    }// connie 20180731 for use EditText to modify start time & duration-e

    abstract public void onSetPositiveButton(int durationValue);
}
