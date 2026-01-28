package com.loader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loader.structure.OTATerrParameters;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.SelectBoxView;
import com.prime.dtvplayer.utils.TVMessage;

import java.lang.reflect.Method;

public class LoaderTerrestrialParameters extends DTVActivity{

    private final String TAG = getClass().getSimpleName();
    private EditText efreq_value;
    private EditText epid_value;
    private TextView tStatus;
    private Spinner sbandWidth_value;
    private Spinner sqam_value;
    private Button bconfirm;
    public OTATerrParameters parameters;
    private SelectBoxView select_qamMode;
    private SelectBoxView select_bandwidthMode;
    private int ret;
    public int service_is_availabled;
    private boolean is_searched;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader_terrestrialparameters);
        parameters = new OTATerrParameters();

        //Init parameters value
        Bundle b=this.getIntent().getExtras();
        service_is_availabled = b.getInt("service", 0);;
        parameters.pid = b.getInt("pid", 0);
        parameters.frequency = b.getInt("frequency", 0);
        parameters.bandWidth = b.getInt("bandWidth", 0);
        parameters.modulation = b.getInt("modulation", 0);
        parameters.enDVBTPrio = b.getInt("dvbprio", 0);
        parameters.enChannelMode = b.getInt("channelMode", 0);
        is_searched = false;
        Log.d(TAG, "onCreate: pid = " + parameters.pid + " frequency = " + parameters.frequency
                + " bandwidth = " + parameters.bandWidth + " modulation = " + parameters.modulation );

        //set frequency value
        efreq_value = (EditText)findViewById(R.id.e_terrestrial_freq);
        hiddenSoftInputFromWindow(LoaderTerrestrialParameters.this, efreq_value); // hide android keypad
        efreq_value.setText(Integer.toString(parameters.frequency));
        efreq_value.setOnKeyListener(freq_key_listen);
        efreq_value.setOnFocusChangeListener(frequency_change_focus);

        //set pid value
        epid_value = (EditText)findViewById(R.id.e_terrestrial_pid);
        hiddenSoftInputFromWindow(LoaderTerrestrialParameters.this, epid_value);// hide android keypad
        epid_value.setText(Integer.toString(parameters.pid));
        epid_value.setOnKeyListener(pid_key_listen);
        epid_value.setOnFocusChangeListener(pid_change_focus);

        //set bandwidth value
        sbandWidth_value = (Spinner)findViewById(R.id.e_terrestrial_bandwidth);
        String[] bandwidthList = getResources().getStringArray(R.array.STR_LOADER_DTV_BANDWIDTH_ARY);
        select_bandwidthMode = new SelectBoxView(this, sbandWidth_value, bandwidthList);
        select_bandwidthMode.SetSelectedItemIndex(parameters.bandWidth-6);

        //set qam value
        sqam_value = (Spinner)findViewById(R.id.e_terrestrial_qam);
        String[] qamList = getResources().getStringArray(R.array.STR_LOADER_DTV_QAM_ARY);
        select_qamMode = new SelectBoxView(this, sqam_value, qamList);
        select_qamMode.SetSelectedItemIndex(parameters.modulation);

        //set confirm button
        bconfirm = (Button)findViewById(R.id.b_terrestrial_confirm);
        bconfirm.setOnClickListener(mConfirmListener);
        bconfirm.setOnKeyListener(confirm_key_listen);

        //set status text
        tStatus = (TextView)findViewById(R.id.t_terrestrial_status);
        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_EDIT_HINT));
    }

    private View.OnKeyListener freq_key_listen = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            boolean isFreqActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);

            //Log.d(TAG, "efreq_value isActionDown" + isFreqActionDown);
            if (isFreqActionDown) {
                Log.d(TAG, "efreq_value keyCode: " + keyCode);
                switch (keyCode) {
                    case KeyEvent.KEYCODE_0:
                        boolean isStartEqualEnd = efreq_value.getSelectionStart() == efreq_value.getSelectionEnd() ? true : false;
                        if (!isStartEqualEnd || efreq_value.getText().toString().isEmpty())
                            return true;
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (efreq_value.getText().length() == 3) {
                            sbandWidth_value.requestFocus();
                            tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_SELECT_HINT));
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        sbandWidth_value.requestFocus();
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_SELECT_HINT));
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        bconfirm.requestFocus();
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_SEARCH_HINT));
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        deleteEditText(efreq_value);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private View.OnFocusChangeListener frequency_change_focus = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                // Set Help
                efreq_value.selectAll();
                if(is_searched == false)
                    tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_EDIT_HINT));
            }
            else {
                String freqStr = efreq_value.getText().toString();
                int NewFrequency;
                if(freqStr.length() == 3) {
                    NewFrequency = Integer.parseInt(freqStr);
                    if(NewFrequency >= 100 && NewFrequency <=900) {
                        if (NewFrequency != parameters.frequency) {
                            parameters.frequency = NewFrequency;
                            Log.d(TAG, "onFocusChange:     frequency change ! new frequency  = " + NewFrequency);
                        }
                    }
                    else {
                        efreq_value.setText(String.valueOf(parameters.frequency));
                        show_err_toast(getResources().getString(R.string.STR_LOADER_DTV_FREQ_WRONG));

                    }
                }
                else {
                    efreq_value.setText(String.valueOf(parameters.frequency));
                    show_err_toast(getResources().getString(R.string.STR_LOADER_DTV_FREQ_WRONG));
                }
            }
        }
    };

    private View.OnKeyListener pid_key_listen = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            boolean isPidActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);

            //Log.d(TAG, "epid_value isActionDown" + isPidActionDown);
            if (isPidActionDown) {
                String pid_text = epid_value.getText().toString();
                Log.d(TAG, "epid_value keyCode: " + keyCode);
                switch (keyCode) {
                    case KeyEvent.KEYCODE_0:
                        boolean isStartEqualEnd = epid_value.getSelectionStart() == epid_value.getSelectionEnd() ? true : false;
                        if (!isStartEqualEnd || pid_text.isEmpty())
                            return true;
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (pid_text.isEmpty() || pid_text.length() < 2 )
                            return true;
                        bconfirm.requestFocus();
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_SEARCH_HINT));
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        bconfirm.requestFocus();
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_SEARCH_HINT));
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        sqam_value.requestFocus();
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_SELECT_HINT));
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        deleteEditText(epid_value);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private View.OnFocusChangeListener pid_change_focus = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                // Set Help
                epid_value.selectAll();
                if(is_searched == false)
                    tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_EDIT_HINT));
            }
            else {
                String pidStr = epid_value.getText().toString();

                if(pidStr.length() <2 ) {
                    epid_value.setText(String.valueOf(parameters.pid));
                    show_err_toast(getResources().getString(R.string.STR_LOADER_DTV_PID_WRONG));
                }
                else {
                    int NewPid = Integer.parseInt(pidStr);
                    if (NewPid >= 32 && NewPid <= 8190) {
                        if (NewPid != parameters.pid) {
                            parameters.pid = NewPid;
                            Log.d(TAG, "onFocusChange:     pid change ! new pid= " + NewPid);
                        }
                    } else {
                        epid_value.setText(String.valueOf(parameters.pid));
                        show_err_toast(getResources().getString(R.string.STR_LOADER_DTV_PID_WRONG));
                    }
                }
            }
        }
    };

    private View.OnKeyListener confirm_key_listen = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            boolean isconfirmActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);

            if (isconfirmActionDown) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        efreq_value.requestFocus();
                        return true;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        epid_value.requestFocus();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private View.OnClickListener mConfirmListener =
            new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(is_searched == false) {
                        parameters.bandWidth = select_bandwidthMode.GetSelectedItemIndex() + 6;
                        parameters.frequency = Integer.valueOf(efreq_value.getText().toString());
                        parameters.pid = Integer.valueOf(epid_value.getText().toString());
                        parameters.modulation = select_qamMode.GetSelectedItemIndex();
                        Log.d(TAG, "onClick: pid" + parameters.pid);
                        ret = LoaderDtvCheckTerrestrialService(parameters);
                        if (ret == 0) {
                            tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_WAIT));
                            bconfirm.setText(getResources().getString(R.string.STR_LOADER_DTV_SEARCHING));
                            service_is_availabled = -1;
                            is_searched = true;
                            setOtherItemsFocus(false);
                        } else {
                            Log.d(TAG,"LoaderDtvCheckTerrestrialService failed");
                        }
                    }
                };
            };

    private boolean deleteEditText(EditText et) {//eric lin 20180606 disable symbol rate's virtual keyboard
        int position = et.getSelectionStart();
        Editable editable = et.getText();

        if (position > 0) {
            editable.delete(position - 1, position);
            return true;
        } else {
            return false; //delete fail
        }
    }

    private void hiddenSoftInputFromWindow(Activity activity, EditText editText) {//eric lin 20180606 disable symbol rate's virtual keyboard
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            activity.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(editText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setOtherItemsFocus(boolean focus){

        int color;
        int sel_color;

        if(focus){
            color = getColor(R.color.colorWhite);
            sel_color = R.drawable.selectbox;
        }
        else {
            color = getColor(R.color.colorGray);
            sel_color = R.drawable.selectboxgrayarrow;
        }

        sqam_value.setFocusable(focus);
        setSinnerTextColor(sqam_value.getSelectedView(), color);
        sqam_value.setBackgroundResource(sel_color);//eric lin test

        sbandWidth_value.setFocusable(focus);
        setSinnerTextColor(sbandWidth_value.getSelectedView(), color);
        sbandWidth_value.setBackgroundResource(sel_color);//eric lin test

        bconfirm.setFocusable(focus);
        bconfirm.setTextColor(color);

        efreq_value.setFocusable(focus);
        efreq_value.setTextColor(color);

        epid_value.setFocusable(focus);
        epid_value.setTextColor(color);

    }

    private void setSinnerTextColor(View v, int color) {
        TextView txt = (TextView) v.findViewById(R.id.view_text1);
        txt.setTextColor(color);
    }

    private void show_err_toast(String msg)
    {
        TextView title = (TextView)findViewById(R.id.t_terrestrial_title);
        Toast err = Toast.makeText(LoaderTerrestrialParameters.this, msg, Toast.LENGTH_SHORT);
        err.setGravity(Gravity.TOP, 0, title.getTop());
        err.show();
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_LOADERDTV_SERVICE_STATUS:
            {
                switch(tvMessage.GetLoaderDtvDLStatus())
                {
                    case 0:
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_NO_LOCK));
                        break;
                    case 1:
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_NOT_FOUND));
                        break;
                    case 2:
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_SERVICE_FOUND));
                        service_is_availabled = 0;
                        break;
                    default:
                        tStatus.setText(getResources().getString(R.string.STR_LOADER_DTV_NO_LOCK));
                        break;
                }
            }
        }
        bconfirm.setText(getResources().getString(R.string.STR_LOADER_DTV_SEARCH));
        setOtherItemsFocus(true);
        bconfirm.requestFocus();
        is_searched = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d("LoaderterrestrialParameters", "onKeyDown: IN");
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            {
                Intent intentParams = new Intent();
                Log.d(TAG, "LoaderterrestrialParameters KEYCODE_BACK ..." + service_is_availabled + " " + parameters.pid);

                Bundle b = new Bundle();
                b.putInt("service", service_is_availabled);
                b.putInt("pid", parameters.pid);
                b.putInt("frequency", parameters.frequency);
                b.putInt("bandWidth", parameters.bandWidth);
                b.putInt("modulation", parameters.modulation);
                b.putInt("dvbprio", parameters.enDVBTPrio);
                b.putInt("channelMode", parameters.enChannelMode);
                intentParams.putExtras(b);
                setResult(RESULT_OK, intentParams);
                finish();
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }


}
