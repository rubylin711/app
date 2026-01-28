package com.prime.homeplus.settings.persional;

import android.app.ActionBar;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.tv.TvContentRating;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.settings.ChannelControlManager;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.ParentalControlManager;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;
import com.prime.homeplus.settings.data.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.RequiresApi;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ParentalControlView extends ThirdLevelView {
    private String TAG = "HomePlus-ParentalControlView";

    public ParentalControlView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_parental_control;
    }

    public void onFocus() {
        updateParental();
        if (llParentalControlUnlock.getVisibility() == VISIBLE) {
            btn1.requestFocus();
        } else {
            etParentalPin.requestFocus();
        }
    }

    private EditText etParentalPin;
    private TextView tvPin1, tvPin2, tvPin3, tvPin4;
    private LinearLayout llParentalControlLocked, llParentalControlUnlock;
    private TextView tvEnterPinHint;
    private Button btn1, btn2, btn3, btn4, btn5;

    @Override
    public void onViewCreated() {
        initRatingPopWindow();
        initChannelLockPopWindow();
        initWorkTimePopWindow();
        initPopWindow();
        initAdultShowPopWindow();

        tvEnterPinHint = (TextView) findViewById(R.id.tvEnterPinHint);

        llParentalControlLocked = (LinearLayout) findViewById(R.id.llParentalControlLocked);
        llParentalControlUnlock = (LinearLayout) findViewById(R.id.llParentalControlUnlock);

        etParentalPin = (EditText) findViewById(R.id.etParentalPin);

        tvPin1 = (TextView) findViewById(R.id.tvPin1);
        tvPin2 = (TextView) findViewById(R.id.tvPin2);
        tvPin3 = (TextView) findViewById(R.id.tvPin3);
        tvPin4 = (TextView) findViewById(R.id.tvPin4);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);

        btn1.setOnFocusChangeListener(OnFocusChangeListener);

        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupRatingWindow.showAtLocation(settingsRecyclerView, Gravity.CENTER, 0, 0);
                getRating();
            }
        });

        btn1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        btn5.requestFocus();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        llParentalControlLocked.setVisibility(VISIBLE);
                        llParentalControlUnlock.setVisibility(GONE);

                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        btn2.setOnFocusChangeListener(OnFocusChangeListener);

        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                llSaving.setVisibility(GONE);
                popupChannelLockWindow.showAtLocation(settingsRecyclerView, Gravity.CENTER, 0, 0);
                initChannelList();
            }
        });

        btn2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        btn5.requestFocus();
                    }
                }
                return false;
            }
        });

        btn3.setOnFocusChangeListener(OnFocusChangeListener);

        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tvWorkTimeSubtitle.setText(getContext().getString(R.string.settings_parental_worktime_sub));
                tvWorkTimeSubtitle.setTextColor(Color.parseColor("#ff8da4b9"));

                popupWorkTimeWindow.showAtLocation(settingsRecyclerView, Gravity.CENTER, 0, 0);

                updateWorkTime();
            }
        });

        btn3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        btn5.requestFocus();
                    }
                }
                return false;
            }
        });

        btn4.setOnFocusChangeListener(OnFocusChangeListener);

        btn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
                etParentalPin.setText("");

                etNewPassword.setText("");
                etConfirmPassword.setText("");
                tvPinHint.setTextColor(getResources().getColor(R.color.colorWhiteOpacity40));
                tvPinHint.setText(getContext().getString(R.string.settings_system_pindel));
            }
        });

        btn4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        btn5.requestFocus();
                    }
                }
                return false;
            }
        });

        btn5.setOnFocusChangeListener(OnFocusChangeListener);

        btn5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupAdultShowWindow.showAtLocation(settingsRecyclerView, Gravity.CENTER, 0, 0);
                updateAdultShow();
            }
        });

        btn5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        btn1.requestFocus();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        llParentalControlLocked.setVisibility(VISIBLE);
                        llParentalControlUnlock.setVisibility(GONE);

                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        etParentalPin.setInputType(InputType.TYPE_NULL);

        etParentalPin.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
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
                        pin = String.format("%04d", GposInfo.getPasswordValue(getContext()));
                        LogUtils.d("pin = "+pin);
                    }
                    if (etParentalPin.getText().toString().equals(pin)) {
                        llParentalControlLocked.setVisibility(GONE);
                        llParentalControlUnlock.setVisibility(VISIBLE);

                        btn1.requestFocus();
                        updateParental();
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

    private void updateParental() {
        btn1.setText(Html.fromHtml("<b>" + getContext().getString(R.string.settings_parental_rating) + "</b>" + "<br />" +
                "<small>" + getRatingString() + "</small>" + "<br />"));
        btn2.setText(Html.fromHtml("<b>" + getContext().getString(R.string.settings_parental_channel) + "</b>" + "<br />" +
                "<small>" + getLockSelectedNumber() + "</small>" + "<br />"));
        btn3.setText(Html.fromHtml("<b>" + getContext().getString(R.string.settings_parental_worktime) + "</b>" + "<br />" +
                "<small>" + getWorkTime() + "</small>" + "<br />"));
        btn4.setText(Html.fromHtml("<b>" + getContext().getString(R.string.settings_parental_changepin) + "</b>" + "<br />" +
                "<small>" + "⦁ ⦁ ⦁ ⦁" + "</small>" + "<br />"));
        btn5.setText(Html.fromHtml("<b>" + getContext().getString(R.string.settings_parental_adult_show) + "</b>" + "<br />" +
                "<small>" + getAdultShow() + "</small>" + "<br />"));
    }

    private String getRatingString() {
        String rating = "";
        switch (parentalControlManager.getNowRating()) {
            case 0:

            case 4:

            case 9:
                rating = getContext().getString(R.string.settings_parental_control_pg6);
                break;
            case 12:
                rating = getContext().getString(R.string.settings_parental_control_pg12);
                break;
            case 15:
                rating = getContext().getString(R.string.settings_parental_control_pg15);
                break;
            case 17:
                rating = getContext().getString(R.string.settings_parental_control_pg18);
                break;
            default:
                rating = getContext().getString(R.string.settings_parental_control_pg18);
                break;
        }
        return rating;
    }

    private String getLockSelectedNumber() {
        String lockSelectedString = "" + getChannelLockSize();
        if (lockSelectedString.length() == 1) {
            lockSelectedString = "00" + lockSelectedString;
        } else if (lockSelectedString.length() == 2) {
            lockSelectedString = "0" + lockSelectedString;
        }

        return lockSelectedString;
    }

    private int getChannelLockSize() {
        int count = 0;
        if (channelList != null) {
            for (Channel channel : channelList) {
                if (channel.isLocked()) {
                    count++;
                }
            }
        }
        return count;
    }

    private String getWorkTime() {
        String status = getContext().getString(R.string.settings_off);

        boolean isWorkTimeEnable = false;
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if(gposInfo != null){
            if(GposInfo.getTimeLockPeriodStart(getContext(), 0)!= -1){
                isWorkTimeEnable = true;
            }
        }

        if (isWorkTimeEnable) {
            status = getContext().getString(R.string.settings_on);
            String startH = String.format("%02d", GposInfo.getTimeLockPeriodStart(getContext(), 0)/100);
            String startM = String.format("%02d", GposInfo.getTimeLockPeriodStart(getContext(), 0)%100);
            String endH = String.format("%02d", GposInfo.getTimeLockPeriodEnd(getContext(), 0)/100);
            String endM = String.format("%02d", GposInfo.getTimeLockPeriodEnd(getContext(), 0)%100);

            status = status + "|" + startH + ":" + startM + "-" + endH + ":" + endM;
        }

        return status;
    }

    private boolean checkTimeAvailable(int startH, int startM, int endH, int endM){
        if(startH > 23 || startM > 60 || endH > 23 || endM >60){
            return false;
        }
        if(startH*100+startM >= endH*100+endM)
            return false;

        return true;
    }

    private View popupView;
    private static PopupWindow popupWindow;

    private EditText etNewPassword, etConfirmPassword;
    private TextView tvTitle, tvSubtitle, tvPinHint;
    private TextView tvNPin1, tvNPin2, tvNPin3, tvNPin4;
    private TextView tvCPin1, tvCPin2, tvCPin3, tvCPin4;
    private Button btnSave, btnCancel;

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_pin, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        tvTitle = (TextView) popupView.findViewById(R.id.tvTitle);
        tvSubtitle = (TextView) popupView.findViewById(R.id.tvSubtitle);

        tvTitle.setText(getContext().getString(R.string.settings_parental_changepin_title));
        tvSubtitle.setText(getContext().getString(R.string.settings_parental_changepin_sub));

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
        LogUtils.d("saveValue = "+value);

        GposInfo.setPasswordValue(getContext(), Integer.parseInt(value));
        //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_PASSWORD_VALUE, Integer.valueOf(value));
    }

    private View popupRatingView;
    private static PopupWindow popupRatingWindow;
    private RadioButton rbtn18, rbtn15, rbtn12, rbtn6;

    private ParentalControlManager parentalControlManager;


    private void initRatingPopWindow() {
        popupRatingView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_parental_control, null);
        popupRatingWindow = new PopupWindow(popupRatingView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        rbtn18 = (RadioButton) popupRatingView.findViewById(R.id.rbtn1);
        rbtn15 = (RadioButton) popupRatingView.findViewById(R.id.rbtn2);
        rbtn12 = (RadioButton) popupRatingView.findViewById(R.id.rbtn3);
        rbtn6 = (RadioButton) popupRatingView.findViewById(R.id.rbtn4);
        rbtn18.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        rbtn15.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        rbtn12.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        rbtn6.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        Button btnSave = (Button) popupRatingView.findViewById(R.id.btnSave);
        Button btnCancel = (Button) popupRatingView.findViewById(R.id.btnCancel);

        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String rating = "R18";
                if (rbtn18.isChecked()) {
                    setParentalRating(17);
                    rating = "R18";
                } else if (rbtn15.isChecked()) {
                    setParentalRating(15);
                    rating = "PG15";
                } else if (rbtn12.isChecked()) {
                    setParentalRating(12);
                    rating = "PG12";
                } else if (rbtn6.isChecked()) {
                    setParentalRating(9);
                    rating = "P6";
                }
                LogUtils.d("rating = "+rating);
                //Settings.System.putString(getContext().getContentResolver(), "cns_parental_rating", rating);
                popupRatingWindow.dismiss();
                updateParental();
            }
        });

        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupRatingWindow.dismiss();
                updateParental();
            }
        });

        parentalControlManager = new ParentalControlManager(getContext());
    }

    private void getRating() {
        int index = 0;
        switch (parentalControlManager.getNowRating()) {
            case 0:

            case 4:

            case 9:
                rbtn6.setChecked(true);
                rbtn6.requestFocus();
                break;
            case 12:
                rbtn12.setChecked(true);
                rbtn12.requestFocus();
                break;
            case 15:
                rbtn15.setChecked(true);
                rbtn15.requestFocus();
                break;
            case 17:
                rbtn18.setChecked(true);
                rbtn18.requestFocus();
                break;
        }
    }

    private void setParentalRating(int age) {
        parentalControlManager.setEnable(true);

        parentalControlManager.removeAllRatings();

        for (int i = age; i < 19; i++) {
            TvContentRating rating = TvContentRating.createRating(
                    "com.android.tv",
                    "DVB",
                    "DVB_" + i);
            parentalControlManager.addRatings(rating);
        }
        GposInfo.setParentalRate(getContext(), age);
        //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_PARENTAL_RATE, age);
    }

    private View popupChannelLockView;
    private static PopupWindow popupChannelLockWindow;
    private GridView gvChannelList;
    private TextView tvSelected, tvTotal;
    private Button btnSelectAll;
    private boolean selectAll = true;
    private LinearLayout llSaving;

    private void initChannelLockPopWindow() {
        popupChannelLockView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_parental_control_channel, null);
        popupChannelLockWindow = new PopupWindow(popupChannelLockView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        gvChannelList = (GridView) popupChannelLockView.findViewById(R.id.gvChannelList);

        btnSelectAll = (Button) popupChannelLockView.findViewById(R.id.btnSelectAll);
        selectAll = true;
        btnSelectAll.setText(getContext().getString(R.string.settings_parental_channel_selectall));
        Button btnSave = (Button) popupChannelLockView.findViewById(R.id.btnSave);
        Button btnCancel = (Button) popupChannelLockView.findViewById(R.id.btnCancel);

        tvSelected = (TextView) popupChannelLockView.findViewById(R.id.tvSelected);
        tvTotal = (TextView) popupChannelLockView.findViewById(R.id.tvTotal);

        llSaving = (LinearLayout) popupChannelLockView.findViewById(R.id.llSaving);

        llSaving.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        });

        gvChannelList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (null != view) {
                    view.setBackgroundColor(Color.parseColor("#484d56"));
                    channelLockPosition = position;

                    if (gvChannelList.getCount() > position) {
                        for (int i = 0; i < gvChannelList.getCount(); i++) {
                            View item = gvChannelList.getChildAt(i - gvChannelList.getFirstVisiblePosition());
                            if (item != null) {
                                CheckBox cbLock = item.findViewById(R.id.item_lock);
                                TextView tvNumber = item.findViewById(R.id.item_number);
                                TextView tvName = item.findViewById(R.id.item_name);
                                if (i == position) {
                                    cbLock.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                                    tvNumber.setTextSize(17);
                                    tvName.setTextSize(17);
                                    tvName.setTypeface(null, Typeface.BOLD);
                                } else {
                                    cbLock.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhiteOpacity50)));
                                    tvNumber.setTextSize(16);
                                    tvName.setTextSize(16);
                                    tvName.setTypeface(null, Typeface.NORMAL);
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                channelLockPosition = -1;
                adapter.notifyDataSetChanged();
            }
        });

        gvChannelList.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    channelLockPosition = channelLockPositionLast;
                    adapter.notifyDataSetChanged();
                } else {
                    for (int i = 0; i < gvChannelList.getCount(); i++) {
                        View item = gvChannelList.getChildAt(i - gvChannelList.getFirstVisiblePosition());
                        if (item != null) {
                            CheckBox cbLock = item.findViewById(R.id.item_lock);
                            TextView tvNumber = item.findViewById(R.id.item_number);
                            TextView tvName = item.findViewById(R.id.item_name);

                            cbLock.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhiteOpacity50)));
                            tvNumber.setTextSize(16);
                            tvName.setTextSize(16);
                            tvName.setTypeface(null, Typeface.NORMAL);
                        }
                    }

                    channelLockPositionLast = channelLockPosition;
                    channelLockPosition = -1;
                    adapter.notifyDataSetChanged();
                }
            }
        });

        gvChannelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox item_lock = (CheckBox) view.findViewById(R.id.item_lock);
                TextView item_number = (TextView) view.findViewById(R.id.item_number);

                if (item_lock.isChecked()) {
                    item_lock.setChecked(false);
                } else {
                    item_lock.setChecked(true);
                }

                saveTempList(item_number.getText().toString(), item_lock.isChecked());
                updateChannelList();
            }
        });

        btnSelectAll.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnSelectAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < gvChannelList.getChildCount(); i++) {
                    View convertView = gvChannelList.getChildAt(i);
                    CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.item_lock);
                    checkBox.setChecked(selectAll);
                }

                for (Channel channel : channelList) {
                    saveTempList(channel.StringGetChannelNumber(), selectAll);
                }

                //update button text
                if (selectAll) {
                    btnSelectAll.setText(getContext().getString(R.string.settings_parental_channel_delselectall));
                } else {
                    btnSelectAll.setText(getContext().getString(R.string.settings_parental_channel_selectall));
                }
                selectAll = !selectAll;

                updateChannelList();
            }
        });

        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                llSaving.setVisibility(VISIBLE);

                llSaving.setFocusable(true);
                llSaving.requestFocus();

                new Thread() {
                    @Override
                    public void run() {
                        setLock();

                        Message message = new Message();
                        message.what = 0;
                        messageHandler.sendMessage(message);
                    }
                }.start();
            }
        });

        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupChannelLockWindow.dismiss();
            }
        });

        messageHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        updateParental();
                        llSaving.setVisibility(GONE);
                        popupChannelLockWindow.dismiss();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private Handler messageHandler;

    ArrayList<String> channelTempList;

    private void saveTempList(String channelNumber, boolean save) {
        if (save) {
            if (!channelTempList.contains(channelNumber)) {
                channelTempList.add(channelNumber);
            }
        } else {
            channelTempList.remove(channelNumber);
        }
    }

    private boolean isSelectedAll(){
        return channelTempList.size() != 0 && channelTempList.size() == channelList.size();
    }

    ChannelControlManager channelControlManager;
    ArrayList<Channel> channelList;
    List<Map<String, String>> channelLisData;

    private void initChannelList() {
        channelControlManager = new ChannelControlManager(getContext());

        channelLisData = getChannelLisData();
        channelList = channelControlManager.getChannelList();

        channelTempList = new ArrayList<String>();
        channelTempList.removeAll(channelTempList);

        adapter = new Adapter();
        gvChannelList.setAdapter(adapter);

        handler.postDelayed(initChannelLockRunnable, 500);
    }


    private void setChannelSelectItem() {
        View convertView;

        for (int i = 0; i < gvChannelList.getCount(); i++) {
            convertView = gvChannelList.getChildAt(i);


            if (null != convertView) {
                TextView item_number = (TextView) convertView.findViewById(R.id.item_number);

                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.item_lock);
                boolean isLock = isChannelLock(item_number.getText().toString());
                checkBox.setChecked(isLock);

                if (isLock) {
                    saveTempList(item_number.getText().toString(), true);
                }
            }
        }

        //update button of "Select All/Deselect All"
        if (isSelectedAll() == true) {
            selectAll = false;
            btnSelectAll.setText(getContext().getString(R.string.settings_parental_channel_delselectall));
        } else {
            selectAll = true;
            btnSelectAll.setText(getContext().getString(R.string.settings_parental_channel_selectall));
        }

        updateChannelList();

        if (gvChannelList.getCount() != 0) {

            gvChannelList.requestFocus();

            channelLockPosition = 0;
            gvChannelList.setSelection(0);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateChannelList() {
        String lockSelectedString = "" + getLockSelected();
        if (lockSelectedString.length() == 1) {
            lockSelectedString = "00" + lockSelectedString;
        } else if (lockSelectedString.length() == 2) {
            lockSelectedString = "0" + lockSelectedString;
        }

        tvSelected.setText(lockSelectedString);

        String total = "" + channelList.size();
        if (total.length() == 1) {
            total = "00" + total;
        } else if (total.length() == 2) {
            total = "0" + total;
        }

        tvTotal.setText(total);
    }

    private int getLockSelected() {
        return channelTempList.size();
    }


    private void setLock() {
        if (channelList.size() != 0) {
            saveChannelLock();
            updateParental();
        }
    }

    private void saveChannelLock() {
        if (channelControlManager != null && channelList != null) {
             for (Channel channel : channelList) {
                 boolean inTempList = isInChannelTempList(channel.StringGetChannelNumber());
                 if (channel.isLocked() != inTempList) {
                     channelControlManager.updateChannelLockStatus(channel, inTempList);
                 }
             }
        }
    }

    private Handler handler = new Handler();
    private Runnable initChannelLockRunnable = new Runnable() {
        public void run() {
            setChannelSelectItem();
        }
    };

    Adapter adapter;
    private int channelLockPosition = 0;
    private int channelLockPositionLast = 0;

    private List<Map<String, String>> getChannelLisData() {
        List<Map<String, String>> mList = new ArrayList<Map<String, String>>();
        channelList = channelControlManager.getChannelList();

        Map<String, String> map = new HashMap<String, String>();

        for (Channel channel : channelList) {
            map = new HashMap<String, String>();
            map.put("item_block", "" + getChannelLockStatus(channel.getChannelId()));
            map.put("item_num", "" + channel.StringGetChannelNumber());
            map.put("item_name", channel.getChannelName());
            Log.d(TAG, channel.toString());
            mList.add(map);
        }

        return mList;
    }

    private boolean getChannelLockStatus(long channelId) {
        if (channelList != null) {
            for (Channel channel : channelList) {
                if (channel.getChannelId() == channelId) {
                    return channel.isLocked();
                }
            }
        }
        return false;
    }


    public class Adapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public Adapter() {
            mInflater = LayoutInflater.from(getContext());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.settings_channel_lock_item, null);
                holder.item_bg = (LinearLayout) convertView.findViewById(R.id.item_bg);
                holder.item_lock = (CheckBox) convertView.findViewById(R.id.item_lock);
                holder.item_number = (TextView) convertView.findViewById(R.id.item_number);
                holder.item_name = (TextView) convertView.findViewById(R.id.item_name);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (channelLockPosition == position) {
                holder.item_bg.setBackgroundColor(getResources().getColor(R.color.colorTabSelect));
                holder.item_lock.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                holder.item_number.setTextSize(17);
                holder.item_name.setTextSize(17);
                holder.item_name.setTypeface(null, Typeface.BOLD);
            } else {
                convertView.setBackgroundColor(Color.parseColor("#484d56"));
            }

            Map<String, String> map = channelLisData.get(position);
            holder.item_number.setText(map.get("item_num"));
            holder.item_name.setText(map.get("item_name"));

            holder.item_lock.setChecked(isInChannelTempList(map.get("item_num")));


            return convertView;
        }

        public final int getCount() {
            return channelLisData.size();
        }

        public final Object getItem(int position) {
            return channelLisData.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            CheckBox item_lock;
            LinearLayout item_bg;
            TextView item_number;
            TextView item_name;
        }
    }

    private boolean isChannelLock(String channelNumber) {
        Channel selectedChannel = null;
        for (Channel channel : channelList) {
            if (channel.StringGetChannelNumber().equals(channelNumber)) {
                selectedChannel = channel;
                break;
            }
        }

        if (null != selectedChannel) {
            return getChannelLockStatus(selectedChannel.getChannelId());
        }

        return false;
    }

    private boolean isInChannelTempList(String channelNumber) {
        return channelTempList.contains(channelNumber);
    }

    private View popupWorkTimeView;
    private static PopupWindow popupWorkTimeWindow;

    private TextView tvWorkTimeSubtitle;
    private RadioButton rbtnWorkTimeEnable, rbtnWorkTimeDisable;
    private EditText etStartHour, etStartMinute, etEndHour, etEndMinute;
    private Button btnWorkTimeSave, btnWorkTimeCancel;


    private void initWorkTimePopWindow() {
        popupWorkTimeView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_work_time, null);
        popupWorkTimeWindow = new PopupWindow(popupWorkTimeView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        tvWorkTimeSubtitle = (TextView) popupWorkTimeView.findViewById(R.id.tvSubtitle);

        rbtnWorkTimeEnable = (RadioButton) popupWorkTimeView.findViewById(R.id.rbtn1);
        rbtnWorkTimeDisable = (RadioButton) popupWorkTimeView.findViewById(R.id.rbtn2);

        etStartHour = (EditText) popupWorkTimeView.findViewById(R.id.etStartHour);
        etStartMinute = (EditText) popupWorkTimeView.findViewById(R.id.etStartMinute);
        etEndHour = (EditText) popupWorkTimeView.findViewById(R.id.etEndHour);
        etEndMinute = (EditText) popupWorkTimeView.findViewById(R.id.etEndMinute);

        etStartHour.setInputType(InputType.TYPE_NULL);
        etStartMinute.setInputType(InputType.TYPE_NULL);
        etEndHour.setInputType(InputType.TYPE_NULL);
        etEndMinute.setInputType(InputType.TYPE_NULL);

        etStartHour.setSelectAllOnFocus(true);
        etStartMinute.setSelectAllOnFocus(true);
        etEndHour.setSelectAllOnFocus(true);
        etEndMinute.setSelectAllOnFocus(true);

        btnWorkTimeSave = (Button) popupWorkTimeView.findViewById(R.id.btnSave);
        btnWorkTimeCancel = (Button) popupWorkTimeView.findViewById(R.id.btnCancel);

        rbtnWorkTimeEnable.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        rbtnWorkTimeDisable.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etStartHour.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etStartMinute.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etEndHour.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        etEndMinute.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnWorkTimeSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnWorkTimeCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        etStartHour.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etStartHour.getText().toString().equals("")) {
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
                        }
                    }
                }
                return false;
            }
        });

        etStartMinute.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etStartMinute.getText().toString().equals("")) {
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
                        }
                    }
                }
                return false;
            }
        });

        etEndHour.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etEndHour.getText().toString().equals("")) {
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
                        }
                    }
                }
                return false;
            }
        });

        etEndMinute.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etEndMinute.getText().toString().equals("")) {
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
                        }
                    }
                }
                return false;
            }
        });

        rbtnWorkTimeEnable.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setWorkTimeEnable(true);
            }
        });

        rbtnWorkTimeDisable.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setWorkTimeEnable(false);
            }
        });

        btnWorkTimeSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enable = rbtnWorkTimeEnable.isChecked();
                if (enable) {
                    int start_hour = Integer.valueOf(etStartHour.getText().toString());
                    int start_minute = Integer.valueOf(etStartMinute.getText().toString());
                    int end_hour = Integer.valueOf(etEndHour.getText().toString());
                    int end_minute = Integer.valueOf(etEndMinute.getText().toString());
                    LogUtils.e(" "+start_hour+":"+start_minute+" "+end_hour+":"+end_minute );
                    if(checkTimeAvailable(start_hour, start_minute, end_hour, end_minute)){
                        LogUtils.e("settings_work_time_error "+start_hour+":"+start_minute+" "+end_hour+":"+end_minute );
                    }else{
                        //GposInfo gposInfo = PrimeUtils.get_gpos_info();
                        GposInfo.setTimeLockPeriodStart(getContext(), 0, start_hour*100+start_minute);
                        GposInfo.setTimeLockPeriodEnd(getContext(), 0, end_hour*100+end_minute);
                        //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_TIME_LOCK_PERIOD_1_START, gposInfo.getTimeLockPeriodStart(0));
                        //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_TIME_LOCK_PERIOD_1_END, gposInfo.getTimeLockPeriodEnd(0));
                    }
                } else {
                    //GposInfo gposInfo = PrimeUtils.get_gpos_info();
                    GposInfo.setTimeLockPeriodStart(getContext(), 0, -1);
                    GposInfo.setTimeLockPeriodEnd(getContext(), 0, -1);
                    //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_TIME_LOCK_PERIOD_1_START, gposInfo.getTimeLockPeriodStart(0));
                    //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_TIME_LOCK_PERIOD_1_END, gposInfo.getTimeLockPeriodEnd(0));
                }
                popupWorkTimeWindow.dismiss();
                updateParental();
            }
        });

        btnWorkTimeCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                updateParental();
                popupWorkTimeWindow.dismiss();
            }
        });
    }

    private void updateWorkTime() {
        boolean isWorkTimeEnable = false;
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if(gposInfo != null){
            isWorkTimeEnable = (GposInfo.getTimeLockPeriodStart(getContext(), 0) != -1)?true:false;
        }

        if (isWorkTimeEnable) {
            rbtnWorkTimeEnable.setChecked(true);
            rbtnWorkTimeDisable.setChecked(false);

            setWorkTimeEnable(true);
        } else {
            rbtnWorkTimeEnable.setChecked(false);
            rbtnWorkTimeDisable.setChecked(true);

            setWorkTimeEnable(false);
        }

        String startH = "00";
        String startM = "00";
        String endH = "00";
        String endM = "00";
        if(gposInfo != null) {
            if(GposInfo.getTimeLockPeriodStart(getContext(), 0) != -1){
                startH = String.format("%02d", GposInfo.getTimeLockPeriodStart(getContext(), 0)/100);
                startM = String.format("%02d", GposInfo.getTimeLockPeriodStart(getContext(), 0)%100);
            }
            if(GposInfo.getTimeLockPeriodEnd(getContext(), 0) != -1){
                endH = String.format("%02d", GposInfo.getTimeLockPeriodEnd(getContext(), 0)/100);
                endM = String.format("%02d", GposInfo.getTimeLockPeriodEnd(getContext(), 0)%100);
            }
        }

        etStartHour.setText(startH);
        etStartMinute.setText(startM);
        etEndHour.setText(endH);
        etEndMinute.setText(endM);
    }

    private void setWorkTimeEnable(boolean enable) {
        etStartHour.setFocusable(enable);
        etStartMinute.setFocusable(enable);
        etEndHour.setFocusable(enable);
        etEndMinute.setFocusable(enable);

        etStartHour.setEnabled(enable);
        etStartMinute.setEnabled(enable);
        etEndHour.setEnabled(enable);
        etEndMinute.setEnabled(enable);
    }

    private View popupAdultShowView;
    private static PopupWindow popupAdultShowWindow;

    private TextView tvAdultShowSubtitle;
    private RadioButton rbtnAdultShowEnable, rbtnAdultShowDisable;
    private Button btnAdultShowSave, btnAdultShowCancel;


    private void initAdultShowPopWindow() {
        popupAdultShowView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_adult_show, null);
        popupAdultShowWindow = new PopupWindow(popupAdultShowView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        tvAdultShowSubtitle = (TextView) popupAdultShowView.findViewById(R.id.tvSubtitle);

        rbtnAdultShowEnable = (RadioButton) popupAdultShowView.findViewById(R.id.rbtn1);
        rbtnAdultShowDisable = (RadioButton) popupAdultShowView.findViewById(R.id.rbtn2);

        btnAdultShowSave = (Button) popupAdultShowView.findViewById(R.id.btnSave);
        btnAdultShowCancel = (Button) popupAdultShowView.findViewById(R.id.btnCancel);

        rbtnAdultShowEnable.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        rbtnAdultShowDisable.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnAdultShowSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnAdultShowCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        btnAdultShowSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enable = rbtnAdultShowEnable.isChecked();
                GposInfo.setAdultShow(getContext(), enable?1:0);//Settings.System.putInt(getContext().getContentResolver(), "cns_adult_show", enable?1:0);
                updateParental();
                popupAdultShowWindow.dismiss();
            }
        });

        btnAdultShowCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                updateParental();
                popupAdultShowWindow.dismiss();
            }
        });
    }

    private void updateAdultShow() {
        int current_adult_show = 1;

        current_adult_show = GposInfo.getAdultShow(getContext());//Settings.System.getInt(getContext().getContentResolver(), "cns_adult_show", 1);
        if (current_adult_show == 0) {
            rbtnAdultShowEnable.setChecked(false);
            rbtnAdultShowDisable.setChecked(true);
        } else {
            rbtnAdultShowEnable.setChecked(true);
            rbtnAdultShowDisable.setChecked(false);
        }
    }

    private String getAdultShow() {
        // adult show value default is enable
        String status = getContext().getString(R.string.settings_on);
        int current_adult_show = 1;

        current_adult_show = GposInfo.getAdultShow(getContext());//Settings.System.getInt(getContext().getContentResolver(), "cns_adult_show", 1);


        if (current_adult_show == 0) {
            status = getContext().getString(R.string.settings_off);
        }

        return status;
    }
}
