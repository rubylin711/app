package com.prime.dtvplayer.View;

// Created by edwin_weng on 2018/4/20.

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.SatInfo;

import java.util.List;

import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

public abstract class DialogEditSat extends Dialog {
    private static final String TAG = "AntennaSetupActivity.SatEditDialog";

    private Context cont;
    private List<SatInfo> satList;
    private int satIndex;
    private Spinner spAngleWE;
    private EditText satNameEt, angle1Et, angle2Et;
    private TextView saveTxv, cancelTxv;

    DialogEditSat(@NonNull Context context, int satIndex, List<SatInfo> satList) {
        super(context, R.style.transparentDialog);
        this.satIndex = satIndex;
        cont = context;
        this.satList = satList;

        Log.d(TAG, "DialogEditSat: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_sat_dialog);

        Log.d(TAG, "onCreate: ");

        satNameEt = (EditText) findViewById(R.id.sat_name_edt);
        spAngleWE = (Spinner) findViewById(R.id.ew_spinner);
        angle1Et = (EditText) findViewById(R.id.pos_val1_txv);
        angle2Et = (EditText) findViewById(R.id.pos_val2_txv);
        saveTxv = (TextView) findViewById(R.id.save_txv);
        cancelTxv = (TextView) findViewById(R.id.cancel_txv);

        // init Spinner
        new SelectBoxView(getContext(), spAngleWE, cont.getResources().getStringArray(R.array.STR_EW));
        spAngleWE.setSelection(satList.get(satIndex).getAngleEW());

        // set sat name
        satNameEt.setText(satList.get(satIndex).getSatName());
        satNameEt.setSelection(satNameEt.length());
        HideKeyboard();
        SatNameOnClick();

        // set Angle
        String angle = String.valueOf( satList.get(satIndex).getAngle() );
        angle1Et.setText(angle.substring(0, angle.indexOf(".")));
        angle2Et.setText(angle.substring(angle.indexOf(".")+1, angle.length()));
        AngleOnFocus();
        AngleOnKey();
        InputStopKeyboard(angle1Et);
        InputStopKeyboard(angle2Et);

        InitUpdate();   // set SAVE button
        InitCancel();   // set CANCEL button
    }

    private void InitUpdate() {

        Log.d(TAG, "InitUpdate: ");

        saveTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Save Button");
                String satName = satNameEt.getText().toString();
                String newAngle = angle1Et.getText().toString()+"."+angle2Et.getText().toString();
                String angleWE = (String) spAngleWE.getSelectedItem();
                int angleWEPos = spAngleWE.getSelectedItemPosition();

//                // set new sat name
//                satList.get(satIndex).setSatName(satName);
//                strSatList[satIndex] = satName + " " + newAngle + spAngleWE.getSelectedItem();
//
//                // set new angle
//                satList.get(satIndex).setAngle(Float.valueOf(newAngle));
//
//                // set new angle W E
//                int anglePos = spAngleWE.getSelectedItemPosition();
//                satList.get(satIndex).setAngleEW( (anglePos == ANGLE_E) ? ANGLE_E : ANGLE_W );
//
//                dtv.SatInfoUpdate(satList.get(satIndex));

                OnClickUpdate(satIndex, satName, newAngle, angleWE, angleWEPos);
                dismiss();
            }
        });
    }

    private void InitCancel() {

        Log.d(TAG, "InitCancel: ");

        cancelTxv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Cancel Button");
                dismiss();
            }
        });
    }

    private void HideKeyboard() {
        Window window = getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private void InputStopKeyboard(EditText editText) {

        Log.d(TAG, "InputStopKeyboard: ");

        View.OnClickListener stopKeyboard = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) v).selectAll();
                Log.d(TAG, "onClick: select all");
            }
        };

        editText.setOnClickListener(stopKeyboard);
    }

    private void SatNameOnClick() {

        Log.d(TAG, "SatNameOnClick: ");

        satNameEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: ");

                InputMethodManager mgr = (InputMethodManager) cont.getSystemService(Context.INPUT_METHOD_SERVICE);

                if (mgr != null) {
                    mgr.showSoftInput(satNameEt, InputMethodManager.SHOW_FORCED);
                }
            }
        });
    }

    private void AngleOnFocus() {

        Log.d(TAG, "AngleOnFocus: ");

        View.OnFocusChangeListener angleOnFocus = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                EditText etAngle = (EditText) v;
                String strAngle;
                int angle;

                if (hasFocus)
                {
                    return;
                }

                Log.d(TAG, "onFocusChange: ");

                strAngle = etAngle.getText().toString();
                strAngle = strAngle.contains(".") ? "0" : strAngle;
                angle = Integer.valueOf(strAngle);

                if (etAngle == angle1Et)
                {
                    // angle 1
                    if (angle >= 180) {
                        etAngle.setText(String.valueOf(179));
                    }
                    else if (angle <= 0) {
                        etAngle.setText(String.valueOf(0));
                    }
                }

                if (etAngle == angle2Et)
                {
                    // angle 2
                    etAngle.setText(String.valueOf(angle));
                }
            }
        };

        angle1Et.setOnFocusChangeListener(angleOnFocus);
        angle2Et.setOnFocusChangeListener(angleOnFocus);
    }

    private void AngleOnKey() {

        Log.d(TAG, "AngleOnKey: ");

        angle1Et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == ACTION_UP)
                {
                    return false;
                }

                Log.d(TAG, "onKey: ");

                switch (keyCode) {

                    case KEYCODE_DPAD_RIGHT:
                        angle2Et.requestFocus();
                        return true;

                    case KEYCODE_DPAD_UP:
                        satNameEt.requestFocus();
                        return true;

                    case KEYCODE_DPAD_DOWN:
                        saveTxv.requestFocus();
                        return true;

                    case KEYCODE_DPAD_LEFT:
                    case KEYCODE_DPAD_CENTER:
                        int angle = Integer.valueOf(angle1Et.getText().toString());
                        if (angle >= 180)       angle1Et.setText(String.valueOf(179));
                        else if (angle <= 0)    angle1Et.setText(String.valueOf(0));
                        angle1Et.selectAll();
                        return true;
                }

                return false;
            }
        });

        angle2Et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == ACTION_UP)
                {
                    return false;
                }

                Log.d(TAG, "onKey: ");

                switch (keyCode) {

                    case KEYCODE_DPAD_RIGHT:
                        spAngleWE.requestFocus();
                        return true;

                    case KEYCODE_DPAD_LEFT:
                        angle1Et.requestFocus();
                        return true;

                    case KEYCODE_DPAD_UP:
                        satNameEt.requestFocus();
                        return true;

                    case KEYCODE_DPAD_DOWN:
                        saveTxv.requestFocus();
                        return true;

                    case KEYCODE_DPAD_CENTER:
                        angle2Et.selectAll();
                        return true;
                }
                return false;
            }
        });
    }

    public abstract void OnClickUpdate(int satIndex, String satName, String newAngle, String angleWE, int angleWEPos);

} // SAT Edit Dialog
