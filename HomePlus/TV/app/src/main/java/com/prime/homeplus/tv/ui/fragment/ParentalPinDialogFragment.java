package com.prime.homeplus.tv.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.utils.ViewUtils;

public class ParentalPinDialogFragment extends DialogFragment {
    private static final String TAG = "ParentalPinDialogFragment";

    TextView tvParentalPinHint;
    TextView[] pinAsterisks;
    EditText etParentalPin;
    Button btnParentalPinSave, btnParentalPinCancel;

    private OnPinEnteredListener pinListener;

    public interface OnPinEnteredListener {
        void onPinEntered(String pin);
    }

    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        this.pinListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_parental_pin, container, false);
        tvParentalPinHint = view.findViewById(R.id.tvParentalPinHint);
        etParentalPin = view.findViewById(R.id.etParentalPin);
        btnParentalPinSave = view.findViewById(R.id.btnParentalPinSave);
        btnParentalPinCancel = view.findViewById(R.id.btnParentalPinCancel);
        pinAsterisks = new TextView[]{
                view.findViewById(R.id.tvParentalPinAsterisk1),
                view.findViewById(R.id.tvParentalPinAsterisk2),
                view.findViewById(R.id.tvParentalPinAsterisk3),
                view.findViewById(R.id.tvParentalPinAsterisk4)
        };

        etParentalPin.setInputType(InputType.TYPE_NULL);
        etParentalPin.setFocusable(true);
        etParentalPin.setFocusableInTouchMode(true);
        etParentalPin.requestFocus();

        etParentalPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        Editable text = etParentalPin.getText();
                        int length = text.length();
                        if (length > 0) {
                            text.delete(length - 1, length);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        etParentalPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                for (int i = 0; i < 4; i++) {
                    if (i < length) {
                        pinAsterisks[i].setBackgroundResource(R.drawable.password_on);
                    } else {
                        pinAsterisks[i].setBackgroundResource(R.drawable.password_off);
                    }
                }

                if (length != 0) {
                    showHint();
                }

                if (length == 4) {
                    btnParentalPinSave.requestFocus();
                }
            }
        });

        ViewUtils.applyButtonFocusTextEffect(btnParentalPinSave, 17, 16, true);
        btnParentalPinSave.setOnClickListener(v -> {
            String pin = etParentalPin.getText().toString();
            if (pin.length() == 4) {
                if (pinListener != null) {
                    pinListener.onPinEntered(pin);
                }
            } else {
                showErrorMessage();
                etParentalPin.setText("");
                etParentalPin.requestFocus();
            }
        });

        ViewUtils.applyButtonFocusTextEffect(btnParentalPinCancel, 17, 16, true);
        btnParentalPinCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    public void showHint() {
        if (tvParentalPinHint != null) {
            tvParentalPinHint.setText(getContext().getString(R.string.parental_lock_authorization_press_left_to_delete));
            tvParentalPinHint.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhiteOpacity50));
        }
    }

    public void showErrorMessage() {
        if (tvParentalPinHint != null) {
            tvParentalPinHint.setText(getContext().getString(R.string.parental_lock_authorization_incorrect_pin));
            tvParentalPinHint.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRedWarning));
        }
    }
}