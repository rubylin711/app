package com.prime.homeplus.settings.persional;

import android.app.ActionBar;
import android.app.Instrumentation;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class PurchasePinView extends ThirdLevelView {
    private String TAG = "HomePlus-PurchasePinView";

    public PurchasePinView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_parental_control;
    }

    private TextView tvEnterPin, tvEnterPinHint;
    private EditText etParentalPin;
    private TextView tvPin1, tvPin2, tvPin3, tvPin4;

    public void onFocus() {
        etParentalPin.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        tvEnterPin = (TextView) findViewById(R.id.tvEnterPin);
        tvEnterPinHint = (TextView) findViewById(R.id.tvEnterPinHint);

        tvEnterPin.setText(getContext().getString(R.string.settings_purchase_pin));

        tvPin1 = (TextView) findViewById(R.id.tvPin1);
        tvPin2 = (TextView) findViewById(R.id.tvPin2);
        tvPin3 = (TextView) findViewById(R.id.tvPin3);
        tvPin4 = (TextView) findViewById(R.id.tvPin4);

        etParentalPin = (EditText) findViewById(R.id.etParentalPin);

        etParentalPin.setInputType(InputType.TYPE_NULL);

        etParentalPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "etParentalPin onKey " + keyCode);
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etParentalPin.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        } else {
                            settingsRecyclerView.backToList();
                        }
                    }
                }
                return false;
            }
        });

        etParentalPin.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {

                } else {
                    etParentalPin.setText("");
                }
            }
        });

        etParentalPin.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                lightPassword(tvPin1, tvPin2, tvPin3, tvPin4, etParentalPin.getText().length());

                if (etParentalPin.getText().length() == 4) {
                    String pin = "0000";
                    GposInfo gposInfo = PrimeUtils.get_gpos_info();
                    if(gposInfo != null){
                        pin = String.format("%04d", GposInfo.getPurchasePasswordValue(getContext()));
                        LogUtils.d("pin = "+pin);
                    }
                    if (etParentalPin.getText().toString().equals(pin)) {
                        popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
                        etParentalPin.setText("");

                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                        tvPinHint.setTextColor(getResources().getColor(R.color.colorWhiteOpacity40));
                        tvPinHint.setText(getContext().getString(R.string.settings_system_pindel));
                    } else {
                        tvEnterPinHint.setText(getContext().getString(R.string.settings_system_pinwrong));
                        tvEnterPinHint.setTextColor(getResources().getColor(R.color.colorWarning));
                        etParentalPin.setText("");
                    }
                } else if (etParentalPin.getText().length() == 1) {
                    tvEnterPinHint.setText(getContext().getString(R.string.settings_system_pindel));
                    tvEnterPinHint.setTextColor(getResources().getColor(R.color.colorWhiteOpacity40));
                }
            }
        });
    }

    private void lightPassword(TextView tv1, TextView tv2, TextView tv3, TextView tv4, int size) {
        switch (size) {
            case 0:
                setPasswordOn(tv1, false);
                setPasswordOn(tv2, false);
                setPasswordOn(tv3, false);
                setPasswordOn(tv4, false);
                break;
            case 1:
                setPasswordOn(tv1, true);
                setPasswordOn(tv2, false);
                setPasswordOn(tv3, false);
                setPasswordOn(tv4, false);
                break;
            case 2:
                setPasswordOn(tv1, true);
                setPasswordOn(tv2, true);
                setPasswordOn(tv3, false);
                setPasswordOn(tv4, false);
                break;
            case 3:
                setPasswordOn(tv1, true);
                setPasswordOn(tv2, true);
                setPasswordOn(tv3, true);
                setPasswordOn(tv4, false);
                break;
            case 4:
                setPasswordOn(tv1, true);
                setPasswordOn(tv2, true);
                setPasswordOn(tv3, true);
                setPasswordOn(tv4, true);
                break;
        }
    }

    private void setPasswordOn(TextView tv, boolean on) {
        if (on) {
            tv.setBackground(getResources().getDrawable(R.drawable.password_on));
        } else {
            tv.setBackground(getResources().getDrawable(R.drawable.password_off));
        }
    }

    private View popupView;
    private static PopupWindow popupWindow;

    private EditText etNewPassword, etConfirmPassword;
    private TextView tvPinHint;
    private TextView tvNPin1, tvNPin2, tvNPin3, tvNPin4;
    private TextView tvCPin1, tvCPin2, tvCPin3, tvCPin4;
    private Button btnSave, btnCancel;

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_pin, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        etNewPassword = (EditText) popupView.findViewById(R.id.etNewPassword);
        etConfirmPassword = (EditText) popupView.findViewById(R.id.etConfirmPassword);

        etNewPassword.setInputType(InputType.TYPE_NULL);
        etConfirmPassword.setInputType(InputType.TYPE_NULL);

        tvPinHint = (TextView) popupView.findViewById(R.id.tvPinHint);

        tvNPin1 = (TextView) popupView.findViewById(R.id.tvNPin1);
        tvNPin2 = (TextView) popupView.findViewById(R.id.tvNPin2);
        tvNPin3 = (TextView) popupView.findViewById(R.id.tvNPin3);
        tvNPin4 = (TextView) popupView.findViewById(R.id.tvNPin4);

        tvCPin1 = (TextView) popupView.findViewById(R.id.tvCPin1);
        tvCPin2 = (TextView) popupView.findViewById(R.id.tvCPin2);
        tvCPin3 = (TextView) popupView.findViewById(R.id.tvCPin3);
        tvCPin4 = (TextView) popupView.findViewById(R.id.tvCPin4);

        btnSave = (Button) popupView.findViewById(R.id.btnSave);
        btnCancel = (Button) popupView.findViewById(R.id.btnCancel);

        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        btnSave.setEnabled(false);

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPin = etNewPassword.getText().toString();
                String confirmPin = etConfirmPassword.getText().toString();

                if (newPin.equals(confirmPin)) {
                    saveValue(newPin);
                    popupWindow.dismiss();
                } else {
                    tvPinHint.setText("確認密碼不相符，請重新輸入。");
                    tvPinHint.setTextColor(getResources().getColor(R.color.colorWarning));
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");

                    etNewPassword.requestFocus();
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        etNewPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "etParentalPin onKey " + keyCode);
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        //settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        //settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etNewPassword.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        } else {
                            //settingsRecyclerView.backToList();
                        }
                    }
                }
                return false;
            }
        });

        etConfirmPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "etParentalPin onKey " + keyCode);
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        //settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        //settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etConfirmPassword.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        } else {
                            //settingsRecyclerView.backToList();
                        }
                    }
                }
                return false;
            }
        });

        etNewPassword.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                lightPassword(tvNPin1, tvNPin2, tvNPin3, tvNPin4, etNewPassword.getText().length());

                if (etNewPassword.getText().length() == 4 &&
                        etConfirmPassword.getText().length() == 4) {
                    btnSave.setEnabled(true);
                } else {
                    btnSave.setEnabled(false);
                }

                if (etNewPassword.getText().length() == 4) {
                    etConfirmPassword.requestFocus();
                } else if (etParentalPin.getText().length() == 1) {
                    tvPinHint.setText(getContext().getString(R.string.settings_system_pindel));
                    tvPinHint.setTextColor(getResources().getColor(R.color.colorWhiteOpacity40));
                }
            }
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                lightPassword(tvCPin1, tvCPin2, tvCPin3, tvCPin4, etConfirmPassword.getText().length());

                if (etNewPassword.getText().length() == 4 &&
                        etConfirmPassword.getText().length() == 4) {
                    btnSave.setEnabled(true);
                } else {
                    btnSave.setEnabled(false);
                }

                if (etConfirmPassword.getText().length() == 4) {
                    btnSave.requestFocus();
                }
            }
        });
    }

    private void saveValue(String value) {

        //LogUtils.d("saveValue = "+value);

        GposInfo.setPurchasePasswordValue(getContext(), Integer.parseInt(value));
        //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_PURCHASE_PASSWORD_VALUE,Integer.parseInt(value));
    }
}
