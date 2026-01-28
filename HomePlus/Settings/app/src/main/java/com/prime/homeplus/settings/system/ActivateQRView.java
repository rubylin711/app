package com.prime.homeplus.settings.system;

import android.app.ActionBar;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.homeplus.settings.InfoUtils;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PostDataActivationStatus;
import com.prime.homeplus.settings.PostDataProduct;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActivateQRView extends ThirdLevelView {
    private String TAG = "HomePlus-ActivateQRView";
    private SettingsRecyclerView settingsRecyclerView;

    private final static int CM_MODE_SINGLE_C = 0;
    private final static int CM_MODE_NORMAL = 1;
    private final static int CM_MODE_SINGLE_I = 2;

    private final static int ACTIVATION_REQUEST_INTERVAL_SEC = 10;
    private int activationRequestCountdown = ACTIVATION_REQUEST_INTERVAL_SEC;

    String mGroupId = "";
    int newCmMode = CM_MODE_NORMAL, oldCmMode = CM_MODE_NORMAL;

    public ActivateQRView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_signal;
    }

    private Button btnSignal;
    private TextView tvSignal, tvSignalHint;

    public void onFocus() {
        btnSignal.requestFocus();
    }

    public void onViewPaused() {
        handler.removeCallbacks(updateQRTimer);
        updateCmMode();
    }

    public void onViewResumed() {
        resetUI();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();
        initQRPopWindow();

        tvSignal = (TextView) findViewById(R.id.tvSignal);
        tvSignalHint = (TextView) findViewById(R.id.tvSignalHint);

        tvSignal.setText(getContext().getString(R.string.settings_qrview_activation));
        tvSignalHint.setText(getContext().getString(R.string.settings_qrview_description));

        btnSignal = (Button) findViewById(R.id.btnSignal);

        btnSignal.setText(getContext().getString(R.string.settings_qrview_activation));

        btnSignal.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        btnSignal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetUI();
                String SOID = InfoUtils.getSoId(getContext(), true);
                etCompany.setText(SOID);
                popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
            }
        });
    }

    private View popupView;
    private static PopupWindow popupWindow;
    private ConstraintLayout clQrMain, clModeConfirm;
    private EditText etCompany;
    private Button btnSubmit, btnSubmitCableOnly, btnSubmitInternetOnly, btnCancel, btnModeConfirm, btnModeCancel;
    private TextView tvModeConfirmMsg;

    private void resetUI() {
        oldCmMode = 1;
        popupWindow.dismiss();
        popupQRWindow.dismiss();
        clQrMain.setVisibility(View.VISIBLE);
        clModeConfirm.setVisibility(View.GONE);
    }

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_qr, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, true);

        clQrMain = (ConstraintLayout) popupView.findViewById(R.id.clQrMain);

        etCompany = (EditText) popupView.findViewById(R.id.etCompany);
        etCompany.setInputType(InputType.TYPE_CLASS_NUMBER);

        btnSubmit = (Button) popupView.findViewById(R.id.btnSubmit);
        btnSubmitCableOnly = (Button) popupView.findViewById(R.id.btnSubmitCableOnly);
        btnSubmitInternetOnly = (Button) popupView.findViewById(R.id.btnSubmitInternetOnly);
        btnCancel = (Button) popupView.findViewById(R.id.btnCancel);

        btnSubmit.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnSubmitCableOnly.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnSubmitInternetOnly.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        clModeConfirm = (ConstraintLayout) popupView.findViewById(R.id.clModeConfirm);

        btnModeConfirm = (Button) popupView.findViewById(R.id.btnModeConfirm);
        btnModeCancel = (Button) popupView.findViewById(R.id.btnModeCancel);
        btnModeConfirm.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnModeCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        tvModeConfirmMsg = (TextView) popupView.findViewById(R.id.tvModeConfirmMsg);

        etCompany.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        // settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        // settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etCompany.getText().toString().equals("")) {
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

        etCompany.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // alway set selection to the end of edittext if focused
                    int pos = etCompany.getText().length();
                    etCompany.setSelection(pos);
                }
            }
        });

        btnSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                PrimeUtils.tune_lock(0, 405000, 5217, TpInfo.Cable.QAM_256);
                popupQRWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
                newCmMode = CM_MODE_NORMAL;
                btnRefresh.setVisibility(View.GONE);
                mHandler.removeCallbacks(sendChangeGroup);
                mHandler.postDelayed(sendChangeGroup, 0);
                updateCmMode();
                handler.postDelayed(updateQRTimer, 0);
            }
        });

        btnSubmitCableOnly.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oldCmMode = 1;
                newCmMode = CM_MODE_SINGLE_C;
                btnRefresh.setVisibility(View.GONE);
                tvModeConfirmMsg.setText(v.getContext().getString(R.string.settings_qrview_confrim_single_c));
                clQrMain.setVisibility(View.GONE);
                clModeConfirm.setVisibility(View.VISIBLE);
                btnModeCancel.requestFocus();
            }
        });

        btnSubmitInternetOnly.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                oldCmMode = 1;
                newCmMode = CM_MODE_SINGLE_I;
                btnRefresh.setVisibility(View.VISIBLE);
                tvModeConfirmMsg.setText(v.getContext().getString(R.string.settings_qrview_confrim_single_i));
                clQrMain.setVisibility(View.GONE);
                clModeConfirm.setVisibility(View.VISIBLE);
                btnModeCancel.requestFocus();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCmMode();
                popupWindow.dismiss();
            }
        });

        btnModeConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                PrimeUtils.tune_lock(0, 405000, 5217, TpInfo.Cable.QAM_256);
                popupQRWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
                mHandler.removeCallbacks(sendChangeGroup);
                mHandler.postDelayed(sendChangeGroup, 0);

                if (newCmMode == CM_MODE_SINGLE_I) {
                    String soId = "00";
                    try {
                        soId = String.format("%02d", Integer.parseInt(etCompany.getText().toString().trim()));
                    } catch (Exception e) {
                        Log.d(TAG, "Error: " + e.toString());
                    }
                    // save status
                    queryActivationStatus();
                } else {
                    // reset status
                }

                updateCmMode();
                handler.postDelayed(updateQRTimer, 0);
            }
        });

        btnModeCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetUI();
            }
        });
    }

    private View popupQRView;
    private static PopupWindow popupQRWindow;
    // private
    private ImageView ivQR;
    private TextView tvStatus, tvQR;
    Button btnRefresh;

    private void initQRPopWindow() {
        popupQRView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_qr_code, null);
        popupQRWindow = new PopupWindow(popupQRView, ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, true);

        ivQR = (ImageView) popupQRView.findViewById(R.id.ivQR);

        tvStatus = (TextView) popupQRView.findViewById(R.id.tvStatus);
        tvQR = (TextView) popupQRView.findViewById(R.id.tvQR);

        Button btnCancel = (Button) popupQRView.findViewById(R.id.btnCancel);

        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetUI();
            }
        });

        btnRefresh = (Button) popupQRView.findViewById(R.id.btnRefresh);

        btnRefresh.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String soId = "00";
                try {
                    soId = String.format("%02d", Integer.parseInt(etCompany.getText().toString().trim()));
                } catch (Exception e) {
                    Log.d(TAG, "Error: " + e.toString());
                }
                PostDataActivationStatus postDataActivationStatus = new PostDataActivationStatus(getContext(),
                        mHandler);
                postDataActivationStatus.sendActivationStatus(soId, InfoUtils.getCardSN());
            }
        });

        popupQRWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                handler.removeCallbacks(updateQRTimer);
            }
        });
    }

    private Handler handler = new Handler();
    private Runnable updateQRTimer = new Runnable() {
        public void run() {
            if (popupQRWindow.isShowing()) {
                handler.removeCallbacks(updateQRTimer);
                updateQR();

                String single_i_activation_done = "";
                if (newCmMode == CM_MODE_SINGLE_I && single_i_activation_done.equals("false")) {
                    if (activationRequestCountdown <= 0) {
                        queryActivationStatus();
                        activationRequestCountdown = ACTIVATION_REQUEST_INTERVAL_SEC;
                    } else {
                        activationRequestCountdown--;
                    }
                }

                handler.postDelayed(this, 1000);
            }
        }
    };

    private void queryActivationStatus() {
        String soId = "00";
        try {
            soId = String.format("%02d", Integer.parseInt(etCompany.getText().toString().trim()));
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
        }
        PostDataActivationStatus postDataActivationStatus = new PostDataActivationStatus(getContext(), mHandler);
        postDataActivationStatus.sendActivationStatus(soId, InfoUtils.getCardSN());
    }

    public Bitmap generateQRCode(String content, int size) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.QR_VERSION, "9");
            hints.put(EncodeHintType.MARGIN, "0");

            return encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, size, size, hints);
        } catch (Exception e) {
            Log.d(TAG, "Error:" + e.toString());
            return null;
        }
    }

    private void updateQR() {
        String content = "SO: 20, Facisno: 12345678, Data: (0.0.1, 0, 0dB, 0dBuV), HDSerialNo: , CmMode: 1";
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // generate QR content (SO/SN/Data/HD/CmMode)
        String so = etCompany.getText().toString().trim();
        String Facisno = InfoUtils.getSN();
        String SW_version = PrimeUtils.get_system_info(getContext()).SW_version;
        Double ber = PrimeUtils.tuner_get_ber(0);
        int strength_level = PrimeUtils.tuner_get_strength(0);
        int snr = PrimeUtils.tuner_get_snr(0);
        String HDSerialNo = PrimeUtils.get_hdd_serial();
        int cm = newCmMode;
        content = String.format("SO: %s,  Facisno: %s, Data: (%s, %E, %ddB, %ddBuV), HDSerialNo: %s , CmMode: %d",
                so, Facisno, SW_version, ber, snr, strength_level, HDSerialNo, cm);

        LogUtils.d("content = " + content);

//        Bitmap bitMap = null;
        Bitmap bitMap = generateQRCode(content, 135);
        if (bitMap != null) {
            ivQR.setImageBitmap(bitMap);
        }

//        boolean isActivated = (Settings.System.getInt(getContext().getContentResolver(), "FTI_CREATE", 1) == 0);
        boolean isActivated = (GposInfo.getFTI_CREATE(getContext(),1) == 0);
        tvStatus.setText(isActivated ? getContext().getString(R.string.settings_qrview_activation_status_opened)
                : getContext().getString(R.string.settings_qrview_activation_status_unopened));

        tvStatus.setTextColor(isActivated ? Color.WHITE : getResources().getColor(R.color.colorWarning));

        tvQR.setText(content);
    }

    private void updateCmMode() {
        // update cm mode
    }

    private void saveActivationResult(String crmId, String bid, String zipCode, String areaCode, String cmMode) {
        if (!cmMode.equals("2")) {
            Log.d(TAG, "[saveActivationResult] cmMode is not 2, skipped");
            return;
        }

        // save activation result
    }

    private Runnable sendChangeGroup = new Runnable() {
        public void run() {
            Log.d(TAG, "sendChangeGroup");

            switch (newCmMode) {
                case CM_MODE_SINGLE_C:
                    mGroupId = "0";
                    break;
                case CM_MODE_SINGLE_I:
                    mGroupId = "ottproduct1";
                    break;
                case CM_MODE_NORMAL:
                default:
                    mGroupId = "product1";
            }
            PostDataProduct postDataProduct = new PostDataProduct(getContext(), mHandler);
            postDataProduct.sendChangeGroup(mGroupId);
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            String api = msg.getData().getString("API");
            try {
                if (api.equals(PostDataProduct.API_CHANGE_GROUP_SINGLE)) {
                    String code = msg.getData().getString("code");
                    String message = msg.getData().getString("message");

                    if (!code.equals("0")) {
                        Log.d(TAG, "[changeGroup/single] return error code:" + code + ", message:" + message);
                        return;
                    }

                    Log.d(TAG, "[changeGroup/single] return code:" + code + ", message:" + message);
                } else if (api.equals(PostDataActivationStatus.API_ACTIVATION_STATUS)) {
                    String code = msg.getData().getString("code");
                    String messages[] = msg.getData().getStringArray("messages");

                    if (!code.equals("0000")) {
                        Log.d(TAG, "[activation_status] return error code:" + code + ", message:"
                                + Arrays.toString(messages));
                        return;
                    }

                    Log.d(TAG, "[activation_status] return code:" + code + ", message:" + Arrays.toString(messages));
                    String crmId = msg.getData().getString("CrmId");
                    String bid = msg.getData().getString("BID");
                    String zipCode = msg.getData().getString("ZIPCode");
                    String areaCode = msg.getData().getString("AreaCode");
                    String cmMode = msg.getData().getString("CmMode");
                    Log.d(TAG, "[activation_status] crmId:" + crmId + ", bid:" + bid + ", zipCode:" + zipCode
                            + ", areaCode:" + areaCode + ", cmMode:" + cmMode);
                    saveActivationResult(crmId, bid, zipCode, areaCode, cmMode);
                }
            } catch (Exception e) {
            }
        }
    };
}
