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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.R;

import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

public abstract class DialogAddTp extends Dialog {
    private final static String TAG = "AntennaSetupActivity.DialogAddTp";

    private Context cont;
    private EditText etFreq, etSym;
    private TextView txvOk, txvCancel;
    //private ImageView imgFreq, imgSym;
    private Spinner spPol;
    //private SelectBoxView sbPol;
    //private String inputNum = "";

    DialogAddTp(@NonNull Context context) {
        super(context, R.style.transparentDialog);
        cont = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tp_dialog);

        Log.d(TAG, "onCreate: ");

        etFreq = (EditText) findViewById(R.id.freq_input_txv);
        etSym = (EditText) findViewById(R.id.symbol_input_txv);
        txvOk = (TextView) findViewById(R.id.txv_ok);
        txvCancel = (TextView) findViewById(R.id.txv_cancel);
        //imgFreq = (ImageView) findViewById(R.id.freq_dial_icon);
        //imgSym = (ImageView) findViewById(R.id.symbol_dial_icon);
        spPol = (Spinner) findViewById(R.id.pol_spinner);
        new SelectBoxView(getContext(), spPol, cont.getResources().getStringArray(R.array.STR_ARRAY_POLAR));


        HideKeyboard();
        InputStopKeyboard(etFreq);
        InputStopKeyboard(etSym);
        InputFixOnKey();
        InputRange();
        Ok();
        Cancel();
    }

    private void HideKeyboard()
    {
        Log.d(TAG, "HideKeyboard: ");

        Window window = getWindow();

        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private void InputStopKeyboard(EditText editText)
    {
        Log.d(TAG, "InputStopKeyboard: ");

        View.OnClickListener stopKeyboard = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((EditText) v).selectAll();
                Log.d(TAG, "onClick: select all");
            }
        };

        editText.setOnClickListener(stopKeyboard);
    }

    private void InputFixOnKey()
    {
        Log.d(TAG, "InputFixOnKey: ");

        View.OnKeyListener stopCursor = new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
                EditText editText = (EditText) v;

                if ( !isActionDown )
                    return false;

                Log.d(TAG, "onKey: fix input value");
                switch (keyCode) {

                    case KEYCODE_DPAD_CENTER:
                    case KEYCODE_DPAD_LEFT:
                    case KEYCODE_DPAD_RIGHT:
                        int freq = Integer.valueOf(etFreq.getText().toString());
                        int sym = Integer.valueOf(etSym.getText().toString());
                        if (freq >= 32767)  etFreq.setText(String.valueOf(32767));
                        else if (freq <= 0) etFreq.setText(String.valueOf(0));
                        if (sym >= 65535)   etSym.setText(String.valueOf(65535));
                        else if (sym <= 0)  etSym.setText(String.valueOf(0));
                        editText.selectAll();
                        return true;

                    case KEYCODE_DPAD_DOWN:
                        if (editText == etFreq)     etSym.requestFocus();
                        else if (editText == etSym) spPol.requestFocus();
                        return true;

                    case KEYCODE_DPAD_UP:
                        if (editText == etFreq)     txvOk.requestFocus();
                        else if (editText == etSym) etFreq.requestFocus();
                        return true;
                }

                return false;
            }
        };
        etFreq.setOnKeyListener(stopCursor);
        etSym.setOnKeyListener(stopCursor);
    }

    private void InputRange() {
        View.OnFocusChangeListener range_from_0_to_32767 = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) v;
                String strVal;
                int freq;

                if ( ! hasFocus ) {
                    strVal = editText.getText().toString();
                    strVal = strVal.contains(".") ? "0" : strVal;
                    freq = Integer.valueOf(strVal);

                    // Frequency
                    if (freq >= 32767) {
                        editText.setText(String.valueOf(32767));
                    }
                    else if (freq <= 0) {
                        editText.setText(String.valueOf(0));
                    }
                }
            }
        };
        View.OnFocusChangeListener range_from_0_to_65535 = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) v;
                String strVal;
                int symbolRate;

                if ( ! hasFocus ) {
                    strVal = editText.getText().toString();
                    strVal = strVal.contains(".") ? "0" : strVal;
                    symbolRate = Integer.valueOf(strVal);

                    // Symbol Rate
                    if (symbolRate >= 65535) {
                        editText.setText(String.valueOf(65535));
                    }
                    else if (symbolRate <= 0) {
                        editText.setText(String.valueOf(0));
                    }
                }
            }
        };
        etFreq.setOnFocusChangeListener(range_from_0_to_32767);
        etSym.setOnFocusChangeListener(range_from_0_to_65535);
    }

    public abstract void OnClickOK(int freq, int sym, int pol);
    private void Ok() {
        txvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");

                int freq = Integer.valueOf(etFreq.getText().toString());
                int symbol = Integer.valueOf(etSym.getText().toString());
                int polar = spPol.getSelectedItemPosition();

                OnClickOK(freq, symbol, polar);
                dismiss();
            }
        });
    }
    private void Cancel() {
        txvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
} // Add TP Dialog
