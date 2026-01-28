package com.prime.dtvplayer.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.prime.dtvplayer.MessageDialog;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;
import com.prime.dtvplayer.Sysdata.MasterPinCode;
import java.lang.reflect.Method;


public class ParentalActivity extends DTVActivity {
    private final String TAG = "ParentalActivity";
    private EditText EditPinCode,EditNewPinCode,EditConfirmPinCode;
    private Spinner SpinnerChannelLock,SpinnerInstallLock,SpinnerParentRate;
    private ActivityTitleView setActivityTitleView;
    private ActivityHelpView setActivityHelpView;
    private String PinCode=null,NewPinCode=null,ConfirmPinCode=null;
    private int channellockflag,installationlockflag,parentalrate;
    private GposInfo Gpos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parental);
        ParentInit();

        // Johnny add 20180129
        TextView viewChannelLock = (TextView) findViewById(R.id.channllockTXV);
        viewChannelLock.setVisibility(TextView.GONE);
        SpinnerChannelLock.setVisibility(Spinner.GONE);
    }
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
    }

    private void ParentInit() {
        Log.d(TAG, "ParentInit");
        Gpos = GposInfoGet();//load from db
        parentalrate = Gpos.getParentalRate();//get parental rate
        //PinCode = Integer.toString(Gpos.getPasswordValue());//get Pin code
        PinCode = String.format("%04d",Gpos.getPasswordValue());
        channellockflag = Gpos.getParentalLockOnOff();//get parental lock
        installationlockflag = Gpos.getInstallLockOnOff();//get install lock

        //set editbox
        setEditBox();

        //set selectbox
        setSelectBox();

        //set Title
        setTitle();

        //set HelpInfo Text
        setHelpEditBoxInfo();

        //set Help Text
        setHelpBtn();

    }
    
    public void setNewPinCode()
    {
        Log.d(TAG, "setNewPinCode");
    }
    public void confirmPinCode()
    {
        Log.d(TAG, "confirmPinCode");
    }
    public void setItemFocusable(boolean flag)
    {
        Log.d(TAG, "setItemFocusable");
        EditNewPinCode.setFocusable(flag);
        //EditConfirmPinCode.setFocusable(flag);
        //SpinnerChannelLock.setFocusable(flag);
        SpinnerInstallLock.setFocusable(flag);
        SpinnerParentRate.setFocusable(flag);

        // Johnny 20181219 for mouse control -s
        EditNewPinCode.setEnabled(flag);
        SpinnerInstallLock.setEnabled(flag);
        SpinnerParentRate.setEnabled(flag);

        EditNewPinCode.setFocusableInTouchMode(flag);
        SpinnerInstallLock.setFocusableInTouchMode(flag);
        SpinnerParentRate.setFocusableInTouchMode(flag);
        // Johnny 20181219 for mouse control -e
    }
    public void setTitle()
    {
        Log.d(TAG, "setTitle");
        setActivityTitleView = (ActivityTitleView)findViewById(R.id.activityTitleViewLayout2);
        setActivityTitleView.setTitleView(getString(R.string.STR_PARENTAL_CONTROL));
    }
    public void setHelpEditBoxInfo()
    {
        Log.d(TAG, "setHelpEditBoxInfo");
        setActivityHelpView = (ActivityHelpView)findViewById(R.id.activityHelpViewLayout);
//        SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_NUMBER_INPUT_OK_CONFIRM_HELP));//eric lin 20180523 parental activity
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 38, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setActivityHelpView.setHelpInfoTextBySplit(getString(R.string.STR_NUMBER_INPUT_OK_CONFIRM_HELP));

    }
    public void setHelpSpinnerInfo()
    {
        Log.d(TAG, "setHelpSpinnerInfo");
        setActivityHelpView = (ActivityHelpView)findViewById(R.id.activityHelpViewLayout);
//        SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.STR_PARENT_HELP_INFO));
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 15, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 28, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        style.setSpan(new ForegroundColorSpan(Color.YELLOW), 50, 54, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setActivityHelpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));

    }
    public void setHelpBtn()
    {
        Log.d(TAG, "setHelpBtn");
        setActivityHelpView.resetHelp(1,0,null);
        setActivityHelpView.resetHelp(2,0,null);
        setActivityHelpView.resetHelp(3,0,null);
        setActivityHelpView.resetHelp(4,0,null);
    }

    private void checkPinCode()
    {
                    //Check PinCode
                    if(MasterPinCode.checkPinCode(PinCode, EditPinCode.getText().toString())){//eric lin 20180523 check pin code (include master pin code)
                        Log.d(TAG, "Enter the Right Pin Code");
                        setItemFocusable(true);
                        EditPinCode.setFocusable(false);
                        EditPinCode.setEnabled(false);  // Johnny 20181219 for mouse control

                        // Johnny modify 20180129
                        SpinnerInstallLock.requestFocus();
//                        SpinnerChannelLock.requestFocus();
                    }
                    else {
                        Log.d(TAG, "Enter the Wrong Pin Code");
                        EditPinCode.setText("");//eric lin 20180523 parental activity,-start
                        new MessageDialog(ParentalActivity.this, 0){
                            public void onSetMessage(View v){
                                ((TextView)v).setText(getString(R.string.STR_INCORRECT_PIN_CODE_ACCESS_DENIED));
                            }
                            public void onSetNegativeButton(){
                            }
                            public void onSetPositiveButton(int status){
                            }
                            public void dialogEnd(int status) {
                            }
                        };//eric lin 20180523 parental activity,-end
                    }
                }

    public void setEditBox()
    {
        Log.d(TAG, "setEditBox");
        EditPinCode = (EditText) findViewById(R.id.pincodeEDV);
        hiddenSoftInputFromWindow(ParentalActivity.this, EditPinCode);//eric lin 20180523 parental activity
        EditPinCode.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event){
                boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
                EditText etSymbolRate = (EditText) v;
                if (isActionDown)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            checkPinCode();
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        deleteEditText(EditPinCode);
                        return true;
                        default:
                            break;
                    }
                    }
                if(EditPinCode.getText().length()==4)
                    checkPinCode();
                return false;                
            }
        });
        EditPinCode.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if(hasFocus == true)
                setHelpEditBoxInfo();
            }
        });


        EditNewPinCode = (EditText) findViewById(R.id.newpincodeEDV);
        hiddenSoftInputFromWindow(ParentalActivity.this, EditNewPinCode);//eric lin 20180523 parental activity
        EditNewPinCode.setFocusable(false);
        EditNewPinCode.setEnabled(false);   // Johnny 20181219 for mouse control
        EditNewPinCode.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event){                
                if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN ){//eric lin 20180523 parental activity
                    deleteEditText(EditNewPinCode);
                    return true;
                }
                else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER && EditNewPinCode.getText().length()==4){//eric lin 20180523 parental activity
                    //Add NewPinCode
                    NewPinCode = EditNewPinCode.getText().toString();
                    EditConfirmPinCode.setFocusable(true);
                    EditConfirmPinCode.setEnabled(true);    // Johnny 20181219 for mouse control
                    EditConfirmPinCode.setFocusableInTouchMode(true);   // Johnny 20181219 for mouse control
                    EditConfirmPinCode.requestFocus();
                    return true;
                }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN ){//eric lin 20180523 parental activity
                    SpinnerParentRate.requestFocus();
                    return true;
                }
                return false;
            }
        });
        EditNewPinCode.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if(hasFocus == true) {
                    EditNewPinCode.selectAll();
                setHelpEditBoxInfo();
            }
            }
        });

        EditConfirmPinCode = (EditText) findViewById(R.id.confirmpincodeEDV);
        hiddenSoftInputFromWindow(ParentalActivity.this, EditConfirmPinCode);//eric lin 20180523 parental activity
        EditConfirmPinCode.setFocusable(false);
        EditConfirmPinCode.setEnabled(false);   // Johnny 20181219 for mouse control
        EditConfirmPinCode.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN ){//eric lin 20180523 parental activity
                    deleteEditText(EditConfirmPinCode);
                    return true;
                }                
                else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER && EditConfirmPinCode.getText().length()==4){//eric lin 20180523 parental activity
                    Log.d(TAG, "Confirm New Pin Code");
                    //Confirm NewPinCode
                    if(NewPinCode.equals(EditConfirmPinCode.getText().toString())) {
                        Log.d(TAG, "New Pin Code Changed !");
                        PinCode = NewPinCode;
                        EditNewPinCode.setText("");//eric lin 20180523 parental activity
                        EditConfirmPinCode.setText("");//eric lin 20180523 parental activity
                        EditConfirmPinCode.setFocusable(false);
                        EditConfirmPinCode.setEnabled(false);   // Johnny 20181219 for mouse control

                        // Johnny modify 20180129
                        SpinnerInstallLock.requestFocus();
//                        SpinnerChannelLock.requestFocus();

                        new MessageDialog(ParentalActivity.this, 0){//eric lin 20180523 parental activity
                            public void onSetMessage(View v){
                                ((TextView)v).setText(getString(R.string.STR_PIN_CODE_SUCCESSFULLY_CHANGED));
                            }
                            public void onSetNegativeButton(){
                            }
                            public void onSetPositiveButton(int status){
                            }
                            public void dialogEnd(int status) {
                            }
                        };
                        return true;
                    }
                    else
                    {
                        Log.d(TAG, "Not correctly confirmed !");
                        EditNewPinCode.setText("");//eric lin 20180523 parental activity
                        EditConfirmPinCode.setText("");//eric lin 20180523 parental activity
                        EditConfirmPinCode.clearFocus();//editbox need clear focus before open dialog!!!
                        new MessageDialog(ParentalActivity.this, 0){
                            public void onSetMessage(View v){
                                ((TextView)v).setText(getString(R.string.STR_NOT_CORRECTLY_CONFIRMED));
                            }
                            public void onSetNegativeButton(){
                            }
                            public void onSetPositiveButton(int status){
                            }
                            public void dialogEnd(int status) {
                            }
                        };
                    }
                }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN ){//eric lin 20180523 parental activity
                    EditNewPinCode.requestFocus();
                    return true;
                }
                return false;
            }
        });
        EditConfirmPinCode.setOnFocusChangeListener(new Spinner.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if(hasFocus == true) {
                    EditConfirmPinCode.selectAll();
                setHelpEditBoxInfo();
            }
            }
        });
    }
    public void setSelectBox()
    {
        Log.d(TAG, "setSelectBox");
        //channel lock select
        String[] str_channel_lock_list = getResources().getStringArray(R.array.STR_ARRAY_ONOFF);
//        ArrayAdapter<String> adapter_channel_lock = new ArrayAdapter<String>(
//                this, android.R.layout.simple_spinner_item, str_channel_lock_list);
//
        SpinnerChannelLock= (Spinner) findViewById(R.id.channllockSPINNER);
        final SelectBoxView ChannelLock = new SelectBoxView(this, SpinnerChannelLock, str_channel_lock_list);
        SpinnerChannelLock.setFocusable(false);
        SpinnerChannelLock.setEnabled(false);   // Johnny 20181219 for mouse control
//        SpinnerChannelLock.setOnItemSelectedListener(ChannelLockListener);
//        ChannelLock.SetSelectedItemIndex(channellockflag);

        //installation lock select
        String[] str_install_lock_list = getResources().getStringArray(R.array.STR_ARRAY_ONOFF);
        ArrayAdapter<String> adapter_install_lock = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, str_install_lock_list);

        SpinnerInstallLock= (Spinner) findViewById(R.id.installlockSPINNER);
        final SelectBoxView InstallLock = new SelectBoxView(this, SpinnerInstallLock, str_install_lock_list);
        SpinnerInstallLock.setFocusable(false);
        SpinnerInstallLock.setEnabled(false);   // Johnny 20181219 for mouse control
        SpinnerInstallLock.setOnItemSelectedListener(InstallationLockListener);
        InstallLock.SetSelectedItemIndex(installationlockflag);
        SpinnerInstallLock.setOnFocusChangeListener(new SelectBoxView.SelectBoxtOnFocusChangeListener() {//eric lin 20180523 parental activity
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                super.onFocusChange(v, hasFocus);
                if (hasFocus)
                    setHelpSpinnerInfo();
            }
        });

        //parental rate select
        String[] str_parent_lock_list = getResources().getStringArray(R.array.STR_ARRAY_PARENTAL_RATE);
        ArrayAdapter<String> adapter_parent_rate = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, str_parent_lock_list);

        SpinnerParentRate= (Spinner) findViewById(R.id.parentrateSPINNER);
        final SelectBoxView PatentRate = new SelectBoxView(this, SpinnerParentRate, str_parent_lock_list);
        SpinnerParentRate.setFocusable(false);
        SpinnerParentRate.setEnabled(false);    // Johnny 20181219 for mouse control
        SpinnerParentRate.setOnItemSelectedListener(ParentalRateListener);

        int selectParentalRateIndex = 0;
        if(parentalrate == 0)
            selectParentalRateIndex = 0;//not block
        else if(parentalrate == 6)
            selectParentalRateIndex = 1;
        else if(parentalrate == 12)
            selectParentalRateIndex = 2;
        else if(parentalrate == 16)
            selectParentalRateIndex = 3;
        else if(parentalrate == 18)
            selectParentalRateIndex = 4;
        else
            selectParentalRateIndex = 5;//all block
        PatentRate.SetSelectedItemIndex(selectParentalRateIndex); // need parentalRate define
        SpinnerParentRate.setOnFocusChangeListener(new SelectBoxView.SelectBoxtOnFocusChangeListener() {//eric lin 20180523 parental activity
        @Override
            public void onFocusChange(View v, boolean hasFocus) {
                super.onFocusChange(v, hasFocus);
                if (hasFocus)
                    setHelpSpinnerInfo();
        }
        });
        }

//    private AdapterView.OnItemSelectedListener ChannelLockListener =  new AdapterView.OnItemSelectedListener()
//    {
//        @Override
//        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
//            Log.d(TAG, "ChannelLockListener " + position);
//            // channel lock setting
//            channellockflag = position;
//        }
//        @Override
//        public void onNothingSelected(AdapterView arg0) {
//        }
//    };

    private AdapterView.OnItemSelectedListener InstallationLockListener =  new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            Log.d(TAG, "InstallationLockListener " + position);
            // installation lock setting
            installationlockflag = position;
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private AdapterView.OnItemSelectedListener ParentalRateListener =  new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            Log.d(TAG, "ParentalRateListener " + position);
            // parental rate setting
            if(position == 0)
                parentalrate = 0;//not block
            else if(position == 1)
                parentalrate = 6;
            else if(position == 2)
                parentalrate = 12;
            else if(position == 3)
                parentalrate = 16;
            else if(position == 4)
                parentalrate = 18;
            else
                parentalrate = 99;//all block
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            {
                Gpos.setParentalRate(parentalrate);//set new parental rate
                if(NewPinCode != null)
                    Gpos.setPasswordValue(Integer.valueOf(NewPinCode));//set new pin code
                Gpos.setParentalLockOnOff(channellockflag);//set parental lock
                Gpos.setInstallLockOnOff(installationlockflag);//set install lock
                GposInfoUpdate(Gpos);//store to db
            }
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean deleteEditText(EditText et) {//eric lin 20180523 parental activity
        int position = et.getSelectionStart();
        Editable editable = et.getText();

        if (position > 0) {
            editable.delete(position - 1, position);
            return true;
        } else {
            return false; //delete fail
        }
    }

   private void hiddenSoftInputFromWindow(Activity activity, EditText editText) {//eric lin 20180523 parental activity
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
}
