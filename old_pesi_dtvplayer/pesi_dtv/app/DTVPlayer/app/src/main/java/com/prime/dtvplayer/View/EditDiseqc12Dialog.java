package com.prime.dtvplayer.View;

///**
// * Created by edwin_weng on 2018/4/23.
// */


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolphin.dtv.DiSEqCLimitType;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_PROG_GREEN;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static com.prime.dtvplayer.Sysdata.SatInfo.ANGLE_E;
import static com.prime.dtvplayer.Sysdata.SatInfo.ANGLE_W;
import static com.prime.dtvplayer.Sysdata.TpInfo.DVBS;

public abstract class EditDiseqc12Dialog extends Dialog {
    private static final String TAG = "EditDiseqc12Dialog";

    private TextView resetMotorTxv,txvSetLimit;//Scoty add DiSeqC Motor rule
    private TextView strengthTxv;
    private TextView qualityTxv;
    private Spinner spSat;
    private Spinner spTp;
    private Spinner spMoveSpeed;
    private Spinner spMoveDish;
    private ProgressBar pbStrength;
    private ProgressBar pbQuality;
    private ActivityHelpView help;

    private DTVActivity mDtv;
    private Context cont;
    private int[] satPos;
    private List<SatInfo> satList;
    private List<TpInfo> tpList;
    private String[] strSatList;
    private String[] strTpList;

    private boolean isModified = false;
    private int positionIndex;
    private int orgSat;

    protected EditDiseqc12Dialog(@NonNull Context context,
                                 ActivityHelpView help,
                                 boolean clickNone,
                                 int positionIndex,
                                 int[] satPos,
                                 List<SatInfo> satList,
                                 List<TpInfo> tpList)
    {
        super(context, R.style.transparentDialog);

        if (getWindow() == null) {
            Log.d(TAG, "EditDiseqc12Dialog: getWindow() is null");
            return;
        }

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.verticalMargin = (float) 0.13;
        layoutParams.dimAmount = 0;
        getWindow().setAttributes(layoutParams);

        this.mDtv           = (DTVActivity) context;
        this.cont           = context;
        this.positionIndex  = positionIndex;
        this.satPos         = satPos;
        this.satList        = satList;
        this.tpList         = tpList;
        this.strSatList     = GetSatStringArray(this.satList);
        this.strTpList      = GetTpStringArray(this.tpList);
        this.help           = help;
        Log.d(TAG, "EditDiseqc12Dialog: positionIndex = "+positionIndex);

        if (clickNone)
        {
            this.isModified = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pos_edit_dialog);

        Log.d(TAG, "onCreate: ");

        spSat = (Spinner) findViewById(R.id.sat_spinner);
        spTp = (Spinner) findViewById(R.id.transponder_spinner);
        spMoveDish = (Spinner) findViewById(R.id.sp_move_dish);
        spMoveSpeed = (Spinner) findViewById(R.id.sp_move_speed);
        resetMotorTxv = (TextView) findViewById(R.id.txvOK_button);//Scoty add DiSeqC Motor rule
        txvSetLimit = (TextView) findViewById(R.id.txvSetLimit_button);
        pbStrength = (ProgressBar) findViewById(R.id.strength_progressbar);
        pbQuality = (ProgressBar) findViewById(R.id.quality_progressbar);
        strengthTxv = (TextView) findViewById(R.id.strength_percent_txv);
        qualityTxv = (TextView) findViewById(R.id.quality_percent_txv);
        Log.d(TAG, "onCreate  resetMotorTxv " + resetMotorTxv.getHeight());
        Log.d(TAG, "onCreate  txvSetLimit " + txvSetLimit.getHeight());
        InitTitleHelp();
        InitSpinner();
        InitResetMotor();//Scoty add DiSeqC Motor rule
        InitSetLimit();
        InitProgressBar();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {

        Log.d(TAG, "onKeyDown: keyCode = "+keyCode);

        switch (keyCode)
        {
            case KEYCODE_BACK:
                ShowConfirmDialog();
                break;

            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                ShowEditSat();
                break;

            case KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                ShowAddTp();
                break;
        }
        if (getCurrentFocus() == resetMotorTxv)
        {

            Log.d(TAG, "getCurrentFocus() ==  resetMotorTxv " + resetMotorTxv.getHeight());
        }
        else
        {
            TextView txvCenterPos = (TextView) findViewById(R.id.txvCenterPos);
            Log.d(TAG, "getCurrentFocus() ==  txvCenterPos " + txvCenterPos.getHeight());
            Log.d(TAG, "getCurrentFocus() !=  resetMotorTxv " + resetMotorTxv.getHeight());
            Log.d(TAG, "getCurrentFocus() !=  txvSetLimit " + txvSetLimit.getHeight());
        }
        return super.onKeyDown(keyCode, event);
    }

    private void ShowConfirmDialog()
    {
        Log.d(TAG, "ShowConfirmDialog: ");

        help.resetHelp(1, R.drawable.help_red,"Delete ALL");
        help.resetHelp(2, R.drawable.help_blue,"Delete");

        if (orgSat != spSat.getSelectedItemPosition())
        {
            isModified = true;
        }

        if ( ! isModified ) return;

        ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), this) {
            @Override
            public void OnClickYes() {

                Log.d(TAG, "OnClickYes: positionIndex = "+positionIndex);
                UpdateSetting(spSat.getSelectedItemPosition(), positionIndex);
            }
        };
        confirmDialog.show();
    }

    private void ShowEditSat()
    {
        Log.d(TAG, "ShowEditSat: ");

        DialogEditSat editSatDialog = new DialogEditSat(getContext(), spSat.getSelectedItemPosition(), satList)
        {

            @Override
            public void OnClickUpdate(int satIndex, String satName, String newAngle,
                                    String angleWE, int anglePos)
            {
                Log.d(TAG, "OnClickUpdate: edit SAT");

                int orgPos = spSat.getSelectedItemPosition();

                // set new sat name
                satList.get(satIndex).setSatName(satName);
                strSatList[satIndex] = satName + " " + newAngle + angleWE;

                // set new angle
                satList.get(satIndex).setAngle(Float.valueOf(newAngle));

                // set new angle W E
                satList.get(satIndex).setAngleEW((anglePos == ANGLE_E) ? ANGLE_E : ANGLE_W);

                new SelectBoxView(cont, spSat, strSatList);
                InitSpinnerSat();    // Johnny 20180515 set listener after new SelectBoxView()
                spSat.setSelection(orgPos);
                isModified = true;
            }
        };
        editSatDialog.show();
    }

    private void ShowAddTp()
    {
        Log.d(TAG, "ShowAddTp: ");

        DialogAddTp addTpDialog = new DialogAddTp(getContext()) {
            @Override
            public void OnClickOK(int freq, int sym, int pol) {
                Log.d(TAG, "OnClickOK: add TP");
                int index = spSat.getSelectedItemPosition();
                int tpNum = satList.get(index).getTpNum();

                // add TP info
                TpInfo tpInfo = new TpInfo(DVBS);
                tpInfo.setSatId(satList.get(index).getSatId());
                tpInfo.setTuner_id(0/*getTunerId()*/);
                tpInfo.SatTp.setFreq(freq);
                tpInfo.SatTp.setSymbol(sym);
                tpInfo.SatTp.setPolar(pol);
                tpList.add(tpInfo);
                mDtv.TpInfoAdd(tpInfo); // Johnny 20180815 add tp to service here because TpinfoUpdateList() used in EditDiseqc10/12Dialog can't add tp
                strTpList = GetTpStringArray(tpList);

                // set Tp Num & Tps
                satList.get(index).setTpNum(tpNum+1);
                List<Integer> tps = new ArrayList<>();
                for (int i = 0; i < tpList.size(); i++) {
                    tps.add(tpList.get(i).getTpId());
                }
                satList.get(index).setTps(tps);

                new SelectBoxView(getContext(), spTp, strTpList);
                InitSpinnerTP();    // Johnny 20180515 set listener after new SelectBoxView()
                spTp.setSelection(tpList.size()-1);
                isModified = true;
            }
        };
        addTpDialog.show();
    }

    private void InitTitleHelp()
    {
        Log.d(TAG, "InitTitleHelp: ");

        Resources res = cont.getResources();
        String strTitle = res.getString(R.string.STR_EDIT_POSITION).concat(" "+(positionIndex));

        // Title
        TextView txvTitle = (TextView) findViewById(R.id.pos_edit_title);
        txvTitle.setText(strTitle);

        // Help
        help.resetHelp(1, R.drawable.help_red, res.getString(R.string.STR_SAT_EDIT));
        help.resetHelp(2, R.drawable.help_green, res.getString(R.string.STR_DVBS_ADD_TP));

        // Johnny 20181228 for mouse control -s
        help.setHelpIconClickListener(1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowEditSat();
            }
        });
        help.setHelpIconClickListener(2, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAddTp();
            }
        });
        // Johnny 20181228 for mouse control -e
    }

    private void InitSpinner()
    {
        Log.d(TAG, "InitSpinner: ");

        Resources res = cont.getResources();
        new SelectBoxView(cont, spSat, strSatList);
        new SelectBoxView(cont, spTp, strTpList);
        new SelectBoxView(cont, spMoveDish, res.getStringArray(R.array.STR_ARY_MV_DISH));
        new SelectBoxView(cont, spMoveSpeed, res.getStringArray(R.array.STR_ARY_MV_SPEED));

        if (positionIndex != 0)
        {
            if (satPos[positionIndex] == -1)
            {
                spSat.setSelection(0);
            }
            else
            {
                spSat.setSelection(satPos[positionIndex]);
            }
        }

        InitSpinnerSat();
        InitSpinnerTP();
        InitSpinnerMoveSpeed();
        InitSpinnerMoveDish();

        orgSat = spSat.getSelectedItemPosition();
    }

    private void InitSpinnerSat() {

        spSat.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemSelected: satellite, position = "+position);

                super.onItemSelected(parent, view, position, id);

                // change Transponder list
                tpList = mDtv.TpInfoGetListBySatId(satList.get(position).getSatId());
                strTpList = GetTpStringArray(tpList);
                new SelectBoxView(getContext(), spTp, strTpList);
                InitSpinnerTP();    // Johnny 20180515 set listener after new SelectBoxView()

                spTp.setSelection(0);
                spMoveSpeed.setSelection(0);
                spMoveDish.setSelection(0);
                txvSetLimit.setText(cont.getResources().getString(R.string.STR_DISABLE));
                resetMotorTxv.setHeight(txvSetLimit.getHeight());
                // Tuner Tune
                TunerTune(spTp.getSelectedItemPosition(), tpList);
            }
        });

        Handler handler = new Handler();

        handler.post(new Runnable() { // set Sat focused
            @Override
            public void run() {
                spSat.requestFocus();
            }
        });
    }

    private void InitSpinnerTP() {

        Log.d(TAG, "InitSpinnerTP: ");

        spTp.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                super.onItemSelected(parent, view, position, id);

                Log.d(TAG, "onItemSelected: TP position = "+position);

                // Tuner Tune
                TunerTune(spTp.getSelectedItemPosition(), tpList);
            }
        });
    }

    private void InitSpinnerMoveSpeed()
    {
        Log.d(TAG, "InitSpinnerMoveSpeed: ");

        spMoveSpeed.setOnItemSelectedListener(new SelectBoxView.SelectBoxOnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                super.onItemSelected(parent, view, position, id);

                Log.d(TAG, "onItemSelected: Move Speed, position = "+position);
            }
        });
    }

    private void InitSpinnerMoveDish() {

        Log.d(TAG, "InitSpinnerMoveDish: ");

        final int STOP = 0;
        final int MOVING_E = 1;
        final int MOVING_W = 2;

        spMoveDish.setClickable(false); // don't drop down selector
        spMoveDish.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                Log.d(TAG, "InitSpinnerMoveDish: ");

                final Spinner spinner = (Spinner) v;
                boolean actionDown = event.getAction() == KeyEvent.ACTION_DOWN;
                boolean keyLeft = keyCode == KEYCODE_DPAD_LEFT;
                boolean keyRight = keyCode == KEYCODE_DPAD_RIGHT;
                //Scoty add DiSeqC Motor rule -s
                if(actionDown) {
                    if (keyRight) {
                        spinner.setSelection(MOVING_E);
                        mDtv.setDiSEqC12MoveMotor(0, 0, 1);
                        isModified = true;
                        // Tuner Tune
                        TunerTune(spTp.getSelectedItemPosition(), tpList);
                    } else if (keyLeft) {
                        spinner.setSelection(MOVING_W);
                        mDtv.setDiSEqC12MoveMotor(0, 1, 1);
                        isModified = true;

                        // Tuner Tune
                        TunerTune(spTp.getSelectedItemPosition(), tpList);
                    }
                }
                else
                {
                    if(keyRight || keyLeft) {
                        spinner.setSelection(STOP);
                        mDtv.setDiSEqC12MoveMotorStop(0);
                    }
                }
                //Scoty add DiSeqC Motor rule -e
                return false;
            }
        });
    }

    private void InitResetMotor()//Scoty add DiSeqC Motor rule
    {
        resetMotorTxv.setOnClickListener(new View.OnClickListener() { // Reset Motor
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnClick resetMotorTxv: " + resetMotorTxv.getHeight());
                mDtv.resetDiSEqC12Position(0);
            }
        });
    }

    private void InitSetLimit() {

        Log.d(TAG, "InitSetLimit: ");

        txvSetLimit.setOnClickListener(new View.OnClickListener() { // set "Set Limit" on click listener
            @Override
            public void onClick(View v) {

                Log.d(TAG, "OnClickSetLimit: ");

                (new LimitSetupDialog(cont)).show();
            }
        });
    }

    private void InitProgressBar() {

        Log.d(TAG, "InitProgressBar: ");

        final Handler handler = new Handler();

        Runnable updateProgressBar = new Runnable() {
            @Override
            public void run() {

                int strength;
                int quality;
                int lock;
                int color;

                lock        = mDtv.TunerGetLockStatus(0);
                strength    = mDtv.TunerGetStrength(0);
                quality     = mDtv.TunerGetQuality(0);
                color       = (lock == 1) ? Color.GREEN : Color.RED;

                pbStrength.setProgressTintList(ColorStateList.valueOf(color));
                pbStrength.setProgress(strength);
                pbQuality.setProgressTintList(ColorStateList.valueOf(color));
                pbQuality.setProgress(quality);
                strengthTxv.setText(String.valueOf(strength).concat(" %"));
                qualityTxv.setText(String.valueOf(quality).concat(" %"));
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(updateProgressBar, 1000);
    }

    private void TunerTune(int tpPos, List<TpInfo> tpList)
    {
        Log.d(TAG, "TunerTune: ");

        int tpId = tpList.get(tpPos).getTpId();
        int frequency = tpList.get(tpPos).SatTp.getFreq();
        int symbolRate = tpList.get(tpPos).SatTp.getSymbol();
        int polar = tpList.get(tpPos).SatTp.getPolar();
        mDtv.TunerTuneDVBS(0, tpId, frequency, symbolRate, polar);
    }

    private String[] GetSatStringArray(List<SatInfo> satList)
    {
        Log.d(TAG, "GetSatStringArray: ");

        if (satList == null)
        {
            satList = new ArrayList<>();
        }

        String[] strSatList = new String[satList.size()];

        for(int i = 0; i< satList.size(); i++)
        {
            String name = satList.get(i).getSatName();
            String angle = Float.toString(satList.get(i).getAngle());
            String angleWE = (satList.get(i).getAngleEW() == ANGLE_W) ? "W" : "E";

            strSatList[i] = (name + " " + angle + angleWE);
        }

        return strSatList;
    }

    private String[] GetTpStringArray(List<TpInfo> tpList)
    {
        Log.d(TAG, "GetTpStringArray: ");

        if(tpList == null)
        {
            tpList = new ArrayList<>();
        }

        //Log.d(TAG, "size " + tpList.size() );
        String[] strTpList = new String [tpList.size()];

        for(int i = 0; i< tpList.size(); i++) {
            String freq         = Integer.toString(tpList.get(i).SatTp.getFreq());
            String symbolRate   = Integer.toString(tpList.get(i).SatTp.getSymbol());
            String polar        = (tpList.get(i).SatTp.getPolar() == TpInfo.Sat.POLAR_H) ? " H " : " V ";

            strTpList[i] = freq + polar + symbolRate;
            //Log.d(TAG, "tp id = " + tpList.get(i).getTpId() + "     str = " + strTpList[i]);
        }

        return strTpList;
    }

    class LimitSetupDialog extends Dialog {
        Context cont;
        Resources r;
        String strMoveDish[];
        TextView txvEastLimit, txvWestLimit;
        TextView txvResetDiseqcLimit, txvLimitOnOff;

        LimitSetupDialog(@NonNull Context context) {
            super(context, R.style.transparentDialog);
            cont = context;
            r = context.getResources();
            strMoveDish = r.getStringArray(R.array.STR_ARY_MV_DISH);

            Log.d(TAG, "LimitSetupDialog: ");
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.limit_setup_dialog);

            Log.d(TAG, "onCreate: ");

            txvEastLimit        = (TextView) findViewById(R.id.txv_east_limit_btn);
            txvWestLimit        = (TextView) findViewById(R.id.txv_west_limit_btn);
            txvResetDiseqcLimit = (TextView) findViewById(R.id.txv_reset_diseqc_limit);
            txvLimitOnOff       = (TextView) findViewById(R.id.txv_disable);

            txvEastLimit.setOnClickListener(new View.OnClickListener() { // set east limit
                @Override
                public void onClick(View v) {
                    (new WouldYouLikeToSet(cont, false) {
                        @Override
                        void OnClickOk() {
                            Resources res = cont.getResources();
                            //east
                            txvEastLimit.setText(res.getString(R.string.STR_SET));
                            txvEastLimit.setBackgroundResource(R.drawable.focus_list);
                            txvEastLimit.setFocusable(false);
                            // west
                            txvWestLimit.setText(res.getString(R.string.STR_STOP));
                            txvWestLimit.setBackgroundResource(R.drawable.selectbox);
                            txvWestLimit.setFocusable(true);
                            txvWestLimit.requestFocus();
                            mDtv.setDiSEqCLimitPos(0, DiSEqCLimitType.SAT_MOTOR_LIMIT_EAST.getValue());//Scoty add DiSeqC Motor rule
                        }
                    }).show();
                }
            });

            txvWestLimit.setOnClickListener(new View.OnClickListener() { // set west limit
                @Override
                public void onClick(View v) {
                    (new WouldYouLikeToSet(cont, false) {
                        @Override
                        void OnClickOk() {
                            Resources res = cont.getResources();
                            // west
                            txvWestLimit.setText(res.getString(R.string.STR_SET));
                            txvWestLimit.setBackgroundResource(R.drawable.focus_list);
                            txvWestLimit.setFocusable(false);
                            txvResetDiseqcLimit.setFocusable(true);
                            txvResetDiseqcLimit.requestFocus();
                            mDtv.setDiSEqCLimitPos(0, DiSEqCLimitType.SAT_MOTOR_LIMIT_WEST.getValue());//Scoty add DiSeqC Motor rule
                        }
                    }).show();
                }
            });

            txvResetDiseqcLimit.setOnClickListener(new View.OnClickListener() { // reset
                @Override
                public void onClick(View v) {
                    (new WouldYouLikeToSet(cont, true) {
                        @Override
                        void OnClickOk() {
                            Resources res = cont.getResources();
                            // east
                            txvEastLimit.setText(res.getString(R.string.STR_STOP));
                            txvEastLimit.setBackgroundResource(R.drawable.selectbox);
                            txvEastLimit.setFocusable(true);
                            txvEastLimit.requestFocus();
                            // west
                            txvWestLimit.setText(res.getString(R.string.STR_NOT_SET));
                            txvWestLimit.setBackgroundResource(R.drawable.focus_list);
                            txvWestLimit.setFocusable(false);
                            txvResetDiseqcLimit.setFocusable(false);
                            mDtv.setDiSEqCLimitPos(0, DiSEqCLimitType.SAT_MOTOR_LIMIT_OFF.getValue());//Scoty add DiSeqC Motor rule
                        }
                    }).show();
                }
            });

            txvLimitOnOff.setOnClickListener(new View.OnClickListener() { // click "Disable"
                @Override
                public void onClick(View v) {
                    txvSetLimit.setText(cont.getResources().getString(R.string.STR_DISABLE));
                    mDtv.setDiSEqCLimitPos(0, DiSEqCLimitType.SAT_MOTOR_LIMIT_OFF.getValue());//Scoty add DiSeqC Motor rule
                    dismiss();
                }
            });

            View.OnKeyListener onKeyListener = new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    final Resources res = cont.getResources();
                    boolean actionDown = event.getAction() == KeyEvent.ACTION_DOWN;
                    boolean keyRight = keyCode == KEYCODE_DPAD_RIGHT;
                    boolean keyLeft = keyCode == KEYCODE_DPAD_LEFT;
                    final TextView txv = ((TextView)v);

                    //Scoty add DiSeqC Motor rule -s
                    if(actionDown) {
                        if (keyRight) {
                            txv.setText(res.getString(R.string.STR_MOVING_E));
                            mDtv.setDiSEqC12MoveMotor(0, 0, 1);
                        } else if (keyLeft) {
                            txv.setText(res.getString(R.string.STR_MOVING_W));
                            mDtv.setDiSEqC12MoveMotor(0, 1, 1);
                        }
                    }
                    else
                    {
                        if(keyRight || keyLeft) {
                            txv.setText(res.getString(R.string.STR_STOP));
                            mDtv.setDiSEqC12MoveMotorStop(0);
                        }
                    }
                    //Scoty add DiSeqC Motor rule -e
                    return false;
                }
            };

            txvEastLimit.setOnKeyListener(onKeyListener);
            txvWestLimit.setOnKeyListener(onKeyListener);
        }

        @Override
        public void onBackPressed() { // Limit Setup Dialog

            Log.d(TAG, "onBackPressed: ");

            super.onBackPressed();
            Resources res = cont.getResources();
            boolean isEnabled =
                    txvEastLimit.getText().equals(res.getString(R.string.STR_SET)) &&
                    txvWestLimit.getText().equals(res.getString(R.string.STR_SET));

            if (isEnabled)
            {
                txvSetLimit.setText(res.getString(R.string.STR_ENABLE));
                isModified = true;
            }
            else
            {
                txvSetLimit.setText(res.getString(R.string.STR_DISABLE));
            }

            // Tuner Tune
            TunerTune(spTp.getSelectedItemPosition(), tpList);
        }

        abstract class WouldYouLikeToSet extends Dialog
        {
            TextView txvOk, txvCancel;
            boolean reset;

            WouldYouLikeToSet(@NonNull Context context, boolean reset)
            {
                super(context, R.style.transparentDialog);
                this.reset = reset;

                Log.d(TAG, "WouldYouLikeToSet: ");
            }

            @Override
            protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.limit_setup_confirm_dialog);

                Log.d(TAG, "onCreate: ");

                txvOk = (TextView) findViewById(R.id.message_yes_txv);
                txvCancel = (TextView) findViewById(R.id.message_no_txv);
                TextView msg = (TextView) findViewById(R.id.message_txv);

                if (reset)
                {
                    Resources res = cont.getResources();
                    msg.setText(res.getString(R.string.STR_LIMIT_SETUP_CONFIRM_RESET));
                }

                txvOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OnClickOk();
                        dismiss();
                    }
                });

                txvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }

            abstract void OnClickOk();
        } // Would You Like To Set Dialog
    } // Limit Setup Dialog

    public abstract class ConfirmDialog extends Dialog {
        private final String TAG = "AntennaSetupActivity.ConfirmDialog";

        private TextView yes;
        private TextView no;
        private Dialog parentDialog;
        Activity act = (Activity) cont;

        ConfirmDialog(@NonNull Context context, Dialog parent) {
            super(context, R.style.transparentDialog);
            this.parentDialog = parent;

            Log.d(TAG, "ConfirmDialog: ");
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.change_lnb_confirm_dialog);

            Log.d(TAG, "onCreate: ");

            yes = (TextView) findViewById(R.id.message_yes_txv);
            no = (TextView) findViewById(R.id.message_no_txv);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "onClick: YES");

                    OnClickYes();
                    dismiss();

                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parentDialog.dismiss();
                        }
                    });
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "onClick: NO");

                    satList = mDtv.SatInfoGetList(DVBS);
                    strSatList = new String[satList.size()];

                    for(int i = 0; i< satList.size(); i++) {
                        String name = satList.get(i).getSatName();
                        String angle = String.valueOf(satList.get(i).getAngle());
                        String angleWE = (satList.get(i).getAngleEW() == ANGLE_E) ? "E" : "W";
                        strSatList[i] = (name + " " + angle + angleWE);
                    }

                    dismiss();

                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parentDialog.dismiss();
                        }
                    });
                }
            });
        }

        public abstract void OnClickYes();

    } // Confirm Dialog

    public abstract void UpdateSetting(int curPos, int positionIndex);
} // Position Edit Dialog
