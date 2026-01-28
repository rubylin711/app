package com.prime.dtvplayer.View;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.R;

/**
 * Created by johnny_shih on 2018/1/9.
 */

public class DVBSTpDialogView extends DialogFragment {
    private final String TAG = getClass().getSimpleName();

    final public static int TYPE_EDIT_TP = 0;
    final public static int TYPE_ADD_TP = 1;
    final public static String KEY_TYPE = "type";
    final public static String KEY_FREQUENCY = "freq";
    final public static String KEY_SYMBOL_RATE = "symbol";
    final public static String KEY_POLAR = "polar";

    Bundle bundle;

    View DialogView;

    TextView TextTitle;
    TextView TextFreq;
    TextView TextSymbol;
    TextView TextPolar;
    EditText EditTextFreq;
    EditText EditTextSymbol;
    Spinner SpinnerPolar;
    SelectBoxView SelectPolar;
    Button BtnOk;
    Button BtnCancel;

    int type;

    public interface NoticeDialogListener {
        void onDialogEditPositiveClick(int freq, int symbol, int polar);    // pass ok(edit) event to activity
        void onDialogAddPositiveClick(int freq, int symbol, int polar);     // pass ok(add) event to activity
        void onDialogNegativeClick();                                       // pass cancel event
    }

    NoticeDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Pass null as the parent view because its going in the dialog layout
        DialogView = inflater.inflate(R.layout.dvbs_tp_dialog, null);
        // Inflate and set the layout for the dialog
        builder.setView(DialogView);

        InitItems();
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        SetLayoutParams();
    }

    private void InitItems()
    {
        String str;

        bundle = this.getArguments();    // get args from activity

        TextTitle = (TextView) DialogView.findViewById(R.id.titleTXV);
        TextFreq = (TextView) DialogView.findViewById(R.id.freqTXV);
        TextSymbol = (TextView) DialogView.findViewById(R.id.symbolTXV);
        TextPolar = (TextView) DialogView.findViewById(R.id.polarTXV);
        EditTextFreq = (EditText) DialogView.findViewById(R.id.freqEDV);
        EditTextSymbol = (EditText) DialogView.findViewById(R.id.symbolEDV);
        SpinnerPolar = (Spinner) DialogView.findViewById(R.id.polarSPINNER);
        BtnOk = (Button) DialogView.findViewById(R.id.okBTN);
        BtnCancel = (Button) DialogView.findViewById(R.id.cancelBTN);

        type = bundle.getInt(KEY_TYPE, -1);

        // Set Title
        if ( type == TYPE_EDIT_TP )
        {
            TextTitle.setText(getString(R.string.STR_DVBS_EDIT_TP));
        }
        else if ( type == TYPE_ADD_TP )
        {
            TextTitle.setText(getString(R.string.STR_DVBS_ADD_TP));
        }
        else
        {
            // Unknown type
        }

        // Set TextView
        str = getString(R.string.STR_FREQUENCY)+" : ";
        TextFreq.setText(str);
        str = getString(R.string.STR_SYMBOLRATE)+" : ";
        TextSymbol.setText(str);
        str = getString(R.string.STR_POLARIZATION)+" : ";
        TextPolar.setText(str);

        // Set EditText
        str = Integer.toString(bundle.getInt(KEY_FREQUENCY, 0));
        EditTextFreq.setText(str);
        EditTextFreq.setOnFocusChangeListener(EditTextFocusChangedListener);    // 20180810 Johnny for use EditText to modify freq & symbol rate
        EditTextFreq.setOnKeyListener(EditTextKeyListener); // 20180810 Johnny for use EditText to modify freq & symbol rate
        EditTextFreq.setShowSoftInputOnFocus(false);    // 20180810 Johnny for use EditText to modify freq & symbol rate
        str = Integer.toString(bundle.getInt(KEY_SYMBOL_RATE, 0));
        EditTextSymbol.setText(str);
        EditTextSymbol.setOnFocusChangeListener(EditTextFocusChangedListener);  // 20180810 Johnny for use EditText to modify freq & symbol rate
        EditTextSymbol.setOnKeyListener(EditTextKeyListener);   // 20180810 Johnny for use EditText to modify freq & symbol rate
        EditTextSymbol.setShowSoftInputOnFocus(false);  // 20180810 Johnny for use EditText to modify freq & symbol rate

        // Set Spinner
        String[] polarStrList = getResources().getStringArray(R.array.STR_ARRAY_POLAR);
        SelectPolar = new SelectBoxView(getContext(), SpinnerPolar, polarStrList);
        SelectPolar.SetSelectedItemIndex(bundle.getInt(KEY_POLAR, 0));

        // Set Button
        BtnCancel.setOnClickListener(CancelClickListener);
        BtnOk.setOnClickListener(OkClickListener);

        // Set NoticeDialogListener
        mListener = (NoticeDialogListener) getActivity();
    }

    private void SetLayoutParams()
    {
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(windowParams);
        }
    }

    private View.OnClickListener OkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int freq = Integer.parseInt(EditTextFreq.getText().toString());
            int symbol = Integer.parseInt(EditTextSymbol.getText().toString());
            int polar = SelectPolar.GetSelectedItemIndex();

            if ( type == TYPE_EDIT_TP)
            {
                mListener.onDialogEditPositiveClick(freq, symbol, polar);
            }
            else if ( type == TYPE_ADD_TP )
            {
                mListener.onDialogAddPositiveClick(freq, symbol, polar);
            }

            dismiss();
        }
    };

    private View.OnClickListener CancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onDialogNegativeClick();
            dismiss();
        }
    };

    // 20180810 Johnny for use EditText to modify freq & symbol rate -s
    private View.OnFocusChangeListener EditTextFocusChangedListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            EditText editText = (EditText) v;

            if (hasFocus) {
                editText.selectAll();
            }
            else
            {
                String strOriEditTextValue;
                if (editText.hashCode() == EditTextFreq.hashCode()) {
                    strOriEditTextValue = Integer.toString(bundle.getInt(KEY_FREQUENCY, 0));
                }
                else if (editText.hashCode() == EditTextSymbol.hashCode()) {
                    strOriEditTextValue = Integer.toString(bundle.getInt(KEY_SYMBOL_RATE, 0));
                }
                else {
                    strOriEditTextValue = "0";
                }

                if (TextUtils.isEmpty(editText.getText())) {    // return to init value if empty
                    editText.setText(strOriEditTextValue);
                }
            }
        }
    };

    private View.OnKeyListener EditTextKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
            if (isActionDown)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DEL:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        DeleteOneEditText((EditText) v);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private void DeleteOneEditText(EditText et) {
        int position = et.getSelectionStart();
        Editable editable = et.getText();

        if (position > 0) {
            editable.delete(position - 1, position);
        }
    }
    // 20180810 Johnny for use EditText to modify freq & symbol rate -e
}
