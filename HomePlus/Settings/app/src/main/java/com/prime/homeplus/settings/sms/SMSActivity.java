package com.prime.homeplus.settings.sms;

import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import android.app.Instrumentation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.graphics.Typeface;

import com.prime.homeplus.settings.InfoUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.sms.data.OBJQuerySNO;
import com.prime.homeplus.settings.sms.data.OBJSwapMobilePhone;
import com.prime.homeplus.settings.sms.data.OBJAuthorSTB;

public class SMSActivity extends AppCompatActivity {
    private static final String TAG = "SMSActivity";

    private ConstraintLayout clReport, clWorkOrder, clSwapMobilePhone, clAuthorSTB, clActivate, clActivateResult;
    private Button btnIVRSubmit, btnIVRCancel, btnWorkOrderConfirm, btnWorkOrderChange, btnWorkOrderCancel, btnMobilePhoneSave, btnMobilePhoneCancel, btnAutohrSTBConfirm, btnActivateResultConfirm;
    private EditText etCompany, etIVR, etSwapMobilePhone;
    private TextView textView_ReportStatus;
    private TextView textView_RetCode, textView_RetMsg, textView_CrmId, textView_CrmWorkOrder, textView_Crminstallname, textView_CrmBpname, textView_CrmWorker1, textView_Mobilephone, textView_CmMode;
    private TextView textView_CrmId_AuthorSTB, textView_StbSN, textView_CrmWorkOrder_AuthorSTB, textView_CrmWorker1_AuthorSTB, textView_Mobilephone_AuthorSTB, textView_IsHD, textView_HDSerial, textView_CmMode_AuthorSTB;
    private TextView textView_Activating, textView_result_Status, textView_result_RetCode, textView_result_RetMsg;

    private SMSState mSMSState = new SMSState();
    private Handler mHandler = new Handler();

    private OBJQuerySNO mOBJQuerySNO;
    private OBJSwapMobilePhone mOBJSwapMobilePhone;
    private OBJAuthorSTB mOBJAuthorSTB;
    private String newGroupId = "";
    private String newBid = "";
    private String newZipCode = "";
    private String newAreaCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sms);

        mSMSState.setState(SMSState.STATE_INQUIRE);

        initView();
    }

    private void initView() {
        clReport = (ConstraintLayout) findViewById(R.id.clReport);
        clWorkOrder = (ConstraintLayout) findViewById(R.id.clWorkOrder);
        clSwapMobilePhone = (ConstraintLayout) findViewById(R.id.clSwapMobilePhone);
        clAuthorSTB = (ConstraintLayout) findViewById(R.id.clAuthorSTB);
        clActivate = (ConstraintLayout) findViewById(R.id.clActivate);
        clActivateResult = (ConstraintLayout) findViewById(R.id.clActivateResult);

        btnIVRSubmit = (Button) findViewById(R.id.btnIVRSubmit);
        btnIVRCancel = (Button) findViewById(R.id.btnIVRCancel);

        btnWorkOrderConfirm = (Button) findViewById(R.id.btnWorkOrderConfirm);
        btnWorkOrderChange = (Button) findViewById(R.id.btnWorkOrderChange);
        btnWorkOrderCancel = (Button) findViewById(R.id.btnWorkOrderCancel);

        btnMobilePhoneSave = (Button) findViewById(R.id.btnMobilePhoneSave);
        btnMobilePhoneCancel = (Button) findViewById(R.id.btnMobilePhoneCancel);

        btnAutohrSTBConfirm = (Button) findViewById(R.id.btnAutohrSTBConfirm);
        btnActivateResultConfirm = (Button) findViewById(R.id.btnActivateResultConfirm);

        etCompany = (EditText) findViewById(R.id.etCompany);
        etIVR = (EditText) findViewById(R.id.etIVR);
        etSwapMobilePhone = (EditText) findViewById(R.id.etSwapMobilePhone);

        textView_ReportStatus = (TextView) findViewById(R.id.textView_ReportStatus);

        btnIVRSubmit.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnIVRCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnWorkOrderConfirm.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnWorkOrderChange.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnWorkOrderCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnMobilePhoneSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnMobilePhoneCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnAutohrSTBConfirm.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnActivateResultConfirm.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        btnIVRCancel.requestFocus();

        mHandler.removeCallbacks(initialFocusRunable);
        mHandler.postDelayed(initialFocusRunable, 250);

        String mCrmId = InfoUtils.getSoId(getApplicationContext(), true);
        if (!TextUtils.isEmpty(mCrmId)) {
            etCompany.setText(mCrmId);
        }
        etCompany.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!TextUtils.isEmpty(etCompany.getText().toString())) {
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
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return true;
                    }
                }
                return false;
            }
        });

        etCompany.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setButtonFocusStyle(v, hasFocus);
                if (hasFocus) {
                    int pos = etCompany.getText().length();
                    etCompany.setSelection(pos);
                }
            }
        });


        etIVR.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!TextUtils.isEmpty(etIVR.getText().toString())) {
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
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (!TextUtils.isEmpty(etIVR.getText().toString())) {
                            etIVR.setSelection(0);
                        }
                    }
                }
                return false;
            }
        });

        etIVR.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setButtonFocusStyle(v, hasFocus);
                if (hasFocus) {
                    int pos = etIVR.getText().length();
                    etIVR.setSelection(pos);
                }
            }
        });

        etSwapMobilePhone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!TextUtils.isEmpty(etSwapMobilePhone.getText().toString())) {
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
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return true;
                    }
                }
                return false;
            }
        });

        etSwapMobilePhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setButtonFocusStyle(v, hasFocus);
                if (hasFocus) {
                    int pos = etSwapMobilePhone.getText().length();
                    etSwapMobilePhone.setSelection(pos);
                }
            }
        });

        etCompany.addTextChangedListener(inputWatcherQuerySNO);
        etIVR.addTextChangedListener(inputWatcherQuerySNO);
        etSwapMobilePhone.addTextChangedListener(inputWatcherSwapMobilePhone);

        // WorkInfo
        textView_RetCode = (TextView) findViewById(R.id.textView_RetCode);
        textView_RetMsg = (TextView) findViewById(R.id.textView_RetMsg);
        textView_CrmId = (TextView) findViewById(R.id.textView_CrmId);
        textView_CrmWorkOrder = (TextView) findViewById(R.id.textView_CrmWorkOrder);
        textView_Crminstallname = (TextView) findViewById(R.id.textView_Crminstallname);
        textView_CrmBpname = (TextView) findViewById(R.id.textView_CrmBpname);
        textView_CrmWorker1 = (TextView) findViewById(R.id.textView_CrmWorker1);
        textView_Mobilephone = (TextView) findViewById(R.id.textView_Mobilephone);
        textView_CmMode = (TextView) findViewById(R.id.textView_CmMode);

        // AuthorSTB
        textView_CrmId_AuthorSTB = (TextView) findViewById(R.id.textView_CrmId_AuthorSTB);
        textView_StbSN = (TextView) findViewById(R.id.textView_StbSN);
        textView_CrmWorkOrder_AuthorSTB = (TextView) findViewById(R.id.textView_CrmWorkOrder_AuthorSTB);
        textView_CrmWorker1_AuthorSTB = (TextView) findViewById(R.id.textView_CrmWorker1_AuthorSTB);
        textView_Mobilephone_AuthorSTB = (TextView) findViewById(R.id.textView_Mobilephone_AuthorSTB);
        textView_IsHD = (TextView) findViewById(R.id.textView_IsHD);
        textView_HDSerial = (TextView) findViewById(R.id.textView_HDSerial);
        textView_CmMode_AuthorSTB = (TextView) findViewById(R.id.textView_CmMode_AuthorSTB);

        // Activating
        textView_Activating = (TextView) findViewById(R.id.textView_Activating);
        textView_result_Status = (TextView) findViewById(R.id.textView_result_Status);
        textView_result_RetCode = (TextView) findViewById(R.id.textView_result_RetCode);
        textView_result_RetMsg = (TextView) findViewById(R.id.textView_result_RetMsg);

        btnIVRSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mCrmId, mCrmWorkshortsno;
                String pattern = "^[0-9]{1}$";

                mCrmId = etCompany.getText().toString();
                if (mCrmId.matches(pattern)) {
                    mCrmId = "0" + mCrmId;
                }

                mCrmWorkshortsno = etIVR.getText().toString();
                mOBJQuerySNO = new OBJQuerySNO(mCrmId, mCrmWorkshortsno);

                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                // save singleI status

                mHandler.removeCallbacks(sendQuerySNORunable);
                mHandler.postDelayed(sendQuerySNORunable, 0);
            }
        });
        btnIVRSubmit.setClickable(false);
        btnIVRSubmit.setTextColor(getColor(R.color.colorWhiteOpacity30));

        btnIVRCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnWorkOrderConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAuthorSTBInfo();
                showAuthorSTB();
            }
        });

        btnWorkOrderChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwapMobilePhone();
            }
        });

        btnWorkOrderCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReport();
            }
        });

        btnMobilePhoneSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mMobilePhone;
                mMobilePhone = etSwapMobilePhone.getText().toString();
                if (mOBJQuerySNO != null) {
                    mOBJSwapMobilePhone = new OBJSwapMobilePhone(mOBJQuerySNO.getCrmId(), mOBJQuerySNO.getCrmWorkshortsno(), mMobilePhone);
                    mHandler.removeCallbacks(sendSwapMobilePhoneRunable);
                    mHandler.postDelayed(sendSwapMobilePhoneRunable, 0);
                }
            }
        });
        btnMobilePhoneSave.setClickable(false);
        btnMobilePhoneSave.setTextColor(getColor(R.color.colorWhiteOpacity30));

        btnMobilePhoneCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReport();
            }
        });

        btnAutohrSTBConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOBJAuthorSTB != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("ACTION", "sendAuthorSTB");
                    Message msg = new Message();
                    msg.setData(bundle);
                    mHandler2.sendMessage(msg);
                }
            }
        });

        btnActivateResultConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public View.OnFocusChangeListener NoAnimationOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            setButtonFocusStyle(view, hasFocus);
        }
    };

    public void setButtonFocusStyle(View view, boolean hasFocus) {
        if (hasFocus) {
            if (view instanceof Button) {
                Button btnBtn = (Button) view;
                btnBtn.setTypeface(null, Typeface.BOLD);
                btnBtn.setTextSize(17);
            } else if (view instanceof EditText) {
                EditText etText = (EditText) view;
                etText.setTypeface(null, Typeface.BOLD);
                etText.setTextSize(17);
            }
        } else {
            if (view instanceof Button) {
                Button btnBtn = (Button) view;
                btnBtn.setTypeface(null, Typeface.NORMAL);
                btnBtn.setTextSize(16);
            } else if (view instanceof EditText) {
                EditText etText = (EditText) view;
                etText.setTypeface(null, Typeface.NORMAL);
                etText.setTextSize(16);
            }
        }
    }

    private final TextWatcher inputWatcherQuerySNO = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Log.d(TAG,"beforeTextChanged-" + start + "-" + count + "-" + after);
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Log.d(TAG,"onTextChanged-" + start + "-" + before + "-" + count);
        }

        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged");
            if ((etCompany.getText().length() > 0) && (etIVR.getText().length() > 0)) {
                btnIVRSubmit.setClickable(true);
                btnIVRSubmit.setTextColor(getResources().getColorStateList(R.color.state_selected_white_normal_b3ffffff));
            } else {
                btnIVRSubmit.setClickable(false);
                btnIVRSubmit.setTextColor(getColor(R.color.colorWhiteOpacity30));
            }

            if (etCompany.getText().length() >= 2) {
                etIVR.requestFocus();
            }
        }
    };

    private final TextWatcher inputWatcherSwapMobilePhone = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Log.d(TAG,"beforeTextChanged-" + start + "-" + count + "-" + after);
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Log.d(TAG,"onTextChanged-" + start + "-" + before + "-" + count);
        }

        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged");
            if (etSwapMobilePhone.getText().length() > 0) {
                btnMobilePhoneSave.setClickable(true);
                btnMobilePhoneSave.setTextColor(getResources().getColorStateList(R.color.state_selected_white_normal_b3ffffff));
            } else {
                btnMobilePhoneSave.setClickable(false);
                btnMobilePhoneSave.setTextColor(getColor(R.color.colorWhiteOpacity30));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    // workaround: do not lunch software keyboard
    private Runnable initialFocusRunable = new Runnable() {
        public void run() {
            Log.d(TAG, "initialFocusRunable");
            etCompany.requestFocus();
        }
    };

    private Runnable sendQuerySNORunable = new Runnable() {
        public void run() {
            Log.d(TAG, "sendQuerySNORunable");

            PostDataSMS postDataSMS = new PostDataSMS(SMSActivity.this, mHandler2);
            postDataSMS.sendQuerySNO(mOBJQuerySNO);
        }
    };

    private Runnable sendSwapMobilePhoneRunable = new Runnable() {
        public void run() {
            Log.d(TAG, "sendSwapMobilePhoneRunable");

            PostDataSMS postDataSMS = new PostDataSMS(SMSActivity.this, mHandler2);
            postDataSMS.sendSwapMobilePhone(mOBJSwapMobilePhone);
        }
    };

    private Runnable sendAuthorSTBRunable = new Runnable() {
        public void run() {
            Log.d(TAG, "sendAuthorSTBRunable");

            PostDataSMS postDataSMS = new PostDataSMS(SMSActivity.this, mHandler2);
            postDataSMS.sendAuthorSTB(mOBJAuthorSTB);
        }
    };

    private Runnable sendChangeGroup = new Runnable() {
        public void run() {
            Log.d(TAG, "sendChangeGroup");

            PostDataProduct postDataProduct = new PostDataProduct(SMSActivity.this, mHandler2);
            postDataProduct.sendChangeGroup(newGroupId);
        }
    };

    private Runnable backToReportPageRunable = new Runnable() {
        public void run() {
            Log.d(TAG, "finishActivationRunable");
            showReport();
        }
    };

    private Runnable finishActivationRunable = new Runnable() {
        public void run() {
            Log.d(TAG, "finishActivationRunable");
            finish();
        }
    };

    private Handler mHandler2 = new Handler() {
        public void handleMessage(Message msg) {

            String api = msg.getData().getString("API");
            try {
                if (!TextUtils.isEmpty(api) && api.equals("QuerySNO")) {
                    // UI: RetCode, RetMsg, CrmId, CrmWorkOrder, Crminstallname, CrmBpname, CrmWorker1, mobilephone, CmMode
                    // AuthorSTB: CrmId, DeviceSNo3, CrmWorkOrder, CrmWork1, MobilePhone, IncludeHD, CustId, ReturnMode, HDSerialNo, CmMode
                    // {"CrmId":"20","DeviceSNo3":"015000831900000043","CrmWorkOrder":"A2020040000051","CrmWorker1":"OL001" ,"MobilePhone":"0987654321","IncludeHD":"1" ,"CustId":"","ReturnMode":"1","HDSerialNo":"WD-WXB1A165P6UH","CmMode":"1"}

                    String retCode = msg.getData().getString("RetCode");
                    String retMsg = msg.getData().getString("RetMsg");
                    String crmId = msg.getData().getString("CrmId");
                    String crmWorkOrder = msg.getData().getString("CrmWorkOrder");
                    String crminstallname = msg.getData().getString("Crminstallname");
                    String crmBpname = msg.getData().getString("CrmBpname");
                    String crmWorker1 = msg.getData().getString("CrmWorker1");
                    String mobilephone = msg.getData().getString("mobilephone");
                    String includeHD = "0";
                    String hdSerialNo = "";
                    String cmMode = msg.getData().getString("CmMode");

                    newBid = msg.getData().getString("BID");
                    newZipCode = msg.getData().getString("ZIPCode");
                    newAreaCode = msg.getData().getString("AreaCode");

                    if (TextUtils.isEmpty(retCode) || !retCode.equals("0")) {
                        textView_ReportStatus.setTextColor(Color.RED);
                        // TODO: error handling
                        textView_ReportStatus.setText(getString(R.string.workorder_inquiry_notexist)); // RetCode: 6
                        showReport();
                        return;
                    }

                    textView_RetCode.setText(retCode);
                    textView_RetMsg.setText(retMsg);
                    textView_CrmId.setText(crmId);
                    textView_CrmWorkOrder.setText(crmWorkOrder);
                    textView_Crminstallname.setText(crminstallname);
                    textView_CrmBpname.setText(crmBpname);
                    textView_CrmWorker1.setText(crmWorker1);
                    textView_Mobilephone.setText(mobilephone);
                    textView_CmMode.setText(cmMode);

                    showWorkOrder();

                    hdSerialNo = PrimeUtils.g_prime_dtv.get_hdd_serial();

                    mOBJAuthorSTB = new OBJAuthorSTB(crmId, android.os.Build.getSerial(), crmWorkOrder, crmWorker1, mobilephone, includeHD, "", "1", hdSerialNo, cmMode);
                    return;
                } else if (!TextUtils.isEmpty(api) && api.equals("SwapMobilePhone")) {
                    String retCode = msg.getData().getString("RetCode");
                    String retMsg = msg.getData().getString("RetMsg");
                    String crmId = msg.getData().getString("CrmId");
                    String crmWorkOrder = msg.getData().getString("CrmWorkOrder");
                    String crminstallname = msg.getData().getString("Crminstallname");
                    String crmBpname = msg.getData().getString("CrmBpname");
                    String crmWorker1 = msg.getData().getString("CrmWorker1");
                    String mobilephone = msg.getData().getString("mobilephone");
                    String includeHD = "0";
                    String hdSerialNo = "";
                    String cmMode = msg.getData().getString("CmMode");

                    newBid = msg.getData().getString("BID");
                    newZipCode = msg.getData().getString("ZIPCode");
                    newAreaCode = msg.getData().getString("AreaCode");

                    if (TextUtils.isEmpty(retCode) || !retCode.equals("0")) {
                        textView_ReportStatus.setTextColor(Color.RED);
                        // TODO: error handling
                        textView_ReportStatus.setText(getString(R.string.workorder_inquiry_notexist)); // RetCode: 6
                        showReport();
                        return;
                    }

                    textView_RetCode.setText(retCode);
                    textView_RetMsg.setText(retMsg);
                    textView_CrmId.setText(crmId);
                    textView_CrmWorkOrder.setText(crmWorkOrder);
                    textView_Crminstallname.setText(crminstallname);
                    textView_CrmBpname.setText(crmBpname);
                    textView_CrmWorker1.setText(crmWorker1);
                    textView_Mobilephone.setText(mobilephone);
                    textView_CmMode.setText(cmMode);

                    showWorkOrder();

                    hdSerialNo = PrimeUtils.g_prime_dtv.get_hdd_serial();

                    mOBJAuthorSTB = new OBJAuthorSTB(crmId, android.os.Build.getSerial(), crmWorkOrder, crmWorker1, mobilephone, includeHD, "", "1", hdSerialNo, cmMode);
                    return;
                } else if (!TextUtils.isEmpty(api) && api.equals("AuthorSTB")) {
                    String retCode = msg.getData().getString("RetCode");
                    String retMsg = msg.getData().getString("RetMsg");

                    if (!TextUtils.isEmpty(retCode) && retCode.equals("0")) {
                        textView_result_Status.setText(getString(R.string.activated));
                        textView_result_RetCode.setText("Return Code:[" + retCode + "]");
                        textView_result_RetMsg.setText(retMsg);

                        try {
                            if (!TextUtils.isEmpty(mOBJAuthorSTB.getCrmId())) {
                                // save so id
                            }

                            if (!TextUtils.isEmpty(mOBJAuthorSTB.getHDSerialNo())) {
                                Log.d(TAG, "[Provisioning finished] get valid HDD, update serial number and mount point, then wait PVR IRD command to enable or disable PVR service");
                                // save pvr info
                            } else {
                                Log.d(TAG, "[Provisioning finished] no valid HDD, reset PVR info");
                                // reset pvr info
                            }

                            if (!TextUtils.isEmpty(mOBJAuthorSTB.getCmMode())) {
                                // save cm mode

                                if (mOBJAuthorSTB.getCmMode().equals("0")) {
                                    newGroupId = "0";
                                } else if (mOBJAuthorSTB.getCmMode().equals("2")) {
                                    newGroupId = "ottproduct1";
                                } else {
                                    newGroupId = "product1";
                                }

                                mHandler.removeCallbacks(sendChangeGroup);
                                mHandler.postDelayed(sendChangeGroup, 0);
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "" + e);
                        }
                    } else {
                        textView_result_Status.setText(getString(R.string.activate_failure));
                        textView_result_RetCode.setText("Return Code:[" + retCode + "]");
                        textView_result_RetMsg.setText(retMsg);
                    }
                    showActivateResult();
                    return;
                } else if (!TextUtils.isEmpty(api) && api.equals("changeGroup/single")) {
                    String code = msg.getData().getString("code");
                    String message = msg.getData().getString("message");

                    if (TextUtils.isEmpty(code) || !code.equals("0")) {
                        Log.d(TAG, "[changeGroup/single] return error code:" + code + ", message:" + message);
                        return;
                    }

                    Log.d(TAG, "[changeGroup/single] return code:" + code + ", message:" + message);
                }
            } catch (Exception e) {
            }

            String action = msg.getData().getString("ACTION");
            try {
                if (!TextUtils.isEmpty(action) && action.equals("sendAuthorSTB")) {
                    mHandler.removeCallbacks(sendAuthorSTBRunable);
                    mHandler.postDelayed(sendAuthorSTBRunable, 100);
                    showActivating();
                } else if (!TextUtils.isEmpty(action) && action.equals("VolleyError")) {
                    String errorMsg = msg.getData().getString("ErrorMsg");
                    textView_ReportStatus.setTextColor(Color.RED);
                    // TODO: need to optimize
                    textView_ReportStatus.setText(getString(R.string.workorder_inquiry_neterror));
                    showReport();
                }
            } catch (Exception e) {
            }

        }
    };

    public void setAuthorSTBInfo() {
        if (mOBJAuthorSTB != null) {
            textView_CrmId_AuthorSTB.setText(mOBJAuthorSTB.getCrmId());
            textView_StbSN.setText(mOBJAuthorSTB.getDeviceSNo3());
            textView_CrmWorkOrder_AuthorSTB.setText(mOBJAuthorSTB.getCrmWorkOrder());
            textView_CrmWorker1_AuthorSTB.setText(mOBJAuthorSTB.getCrmWorker1());
            textView_Mobilephone_AuthorSTB.setText(mOBJAuthorSTB.getMobilePhone());
            textView_IsHD.setText(mOBJAuthorSTB.getIncludeHD());
            textView_HDSerial.setText(mOBJAuthorSTB.getHDSerialNo());
            textView_CmMode_AuthorSTB.setText(mOBJAuthorSTB.getCmMode());

        }
    }

    public void showReport() {
        clReport.setVisibility(View.VISIBLE);
        clWorkOrder.setVisibility(View.GONE);
        clSwapMobilePhone.setVisibility(View.GONE);
        clAuthorSTB.setVisibility(View.GONE);
        clActivate.setVisibility(View.GONE);
        clActivateResult.setVisibility(View.GONE);

        etCompany.requestFocus();
    }

    public void showWorkOrder() {
        clReport.setVisibility(View.GONE);
        clWorkOrder.setVisibility(View.VISIBLE);
        clSwapMobilePhone.setVisibility(View.GONE);
        clAuthorSTB.setVisibility(View.GONE);
        clActivate.setVisibility(View.GONE);
        clActivateResult.setVisibility(View.GONE);

        btnWorkOrderConfirm.requestFocus();
    }

    public void showSwapMobilePhone() {
        clReport.setVisibility(View.GONE);
        clWorkOrder.setVisibility(View.GONE);
        clSwapMobilePhone.setVisibility(View.VISIBLE);
        clAuthorSTB.setVisibility(View.GONE);
        clActivate.setVisibility(View.GONE);
        clActivateResult.setVisibility(View.GONE);

        etSwapMobilePhone.requestFocus();
    }

    public void showAuthorSTB() {
        clReport.setVisibility(View.GONE);
        clWorkOrder.setVisibility(View.GONE);
        clSwapMobilePhone.setVisibility(View.GONE);
        clAuthorSTB.setVisibility(View.VISIBLE);
        clActivate.setVisibility(View.GONE);
        clActivateResult.setVisibility(View.GONE);

        btnAutohrSTBConfirm.requestFocus();
    }

    public void showActivating() {
        clReport.setVisibility(View.GONE);
        clWorkOrder.setVisibility(View.GONE);
        clSwapMobilePhone.setVisibility(View.GONE);
        clAuthorSTB.setVisibility(View.GONE);
        clActivate.setVisibility(View.VISIBLE);
        clActivateResult.setVisibility(View.GONE);
    }

    public void showActivateResult() {
        clReport.setVisibility(View.GONE);
        clWorkOrder.setVisibility(View.GONE);
        clSwapMobilePhone.setVisibility(View.GONE);
        clAuthorSTB.setVisibility(View.GONE);
        clActivate.setVisibility(View.GONE);
        clActivateResult.setVisibility(View.VISIBLE);

        btnActivateResultConfirm.requestFocus();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return false; //handled by the next receiver
    }
}
