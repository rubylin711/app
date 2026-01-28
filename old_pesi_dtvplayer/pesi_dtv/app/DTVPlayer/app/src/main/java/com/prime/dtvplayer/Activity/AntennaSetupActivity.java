package com.prime.dtvplayer.Activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.EditDiseqc12Dialog;
import com.prime.dtvplayer.View.EditDiseqc10Dialog;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.SelectBoxView;

import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_PROG_BLUE;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.OnFocusChangeListener;
import static android.view.View.VISIBLE;
import static com.prime.dtvplayer.Sysdata.SatInfo.ANGLE_W;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_A;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_B;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_C;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_PORT_D;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_TYPE_1_0;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_TYPE_1_2;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_TYPE_NONE;
import static com.prime.dtvplayer.Sysdata.SatInfo.DISEQC_TYPE_OFF;
import static com.prime.dtvplayer.Sysdata.SatInfo.TONE_22K_AUTO;
import static com.prime.dtvplayer.View.SelectBoxView.SelectBoxOnItemSelectedListener;
import static com.prime.dtvplayer.View.SelectBoxView.SelectBoxtOnFocusChangeListener;

public class AntennaSetupActivity extends DTVActivity
{
    private final String TAG = getClass().getSimpleName();

    // View
    private ActivityHelpView help;
    private ConstraintLayout diseqc10Layout, diseqc12Layout, diseqcOffLayout;
    private Spinner sprDscType, sprPage;
    private TextView btnPortA, btnPortB, btnPortC, btnPortD;
    private TextView btnPos1, btnPos2, btnPos3, btnPos4;
    private TextView txvAngle1, txvAngle2, txvAngle3, txvAngle4;
    private TextView txvPosition1, txvPosition2, txvPosition3, txvPosition4;
    private TextView btnOFF;

    // for DTVActivity
    private List<SatInfo> satList;
    private List<TpInfo> tpList;
    private String[] strSatList;

    // for common
    private boolean stopSelect = true;
    private String strNone;

    // Diseqc Off
    private static final int DISEQC_PORT_OFF = -1;
    private int portOFFsat = 0;

    // Diseqc 1.0
    private int portAsat = -1;
    private int portBsat = -1;
    private int portCsat = -1;
    private int portDsat = -1;

    // Diseqc 1.2
    private static final int TOTAL_POSITION = 33;
    private int positionSat[] = new int[TOTAL_POSITION];
    //Scoty 20180831 modify antenna rule by antenna type -s
    private int transferPositionToDiseqcType(int pos)
    {
        if (pos == 0)
            pos = DISEQC_TYPE_OFF;
        else if (pos == 1)
            pos = DISEQC_TYPE_1_0;
        else if (pos == 2)
            pos = DISEQC_TYPE_1_2;

        return pos;
    }

    private int transferDiseqcTypeToPosition(int type)
    {
        int pos = 0;
        if (type == DISEQC_TYPE_OFF)
            pos = 0;
        else if (type == DISEQC_TYPE_1_0)
            pos = 1;
        else if (type == DISEQC_TYPE_1_2)
            pos = 2;

        return pos;
    }
    //Scoty 20180831 modify antenna rule by antenna type
    private SelectBoxOnItemSelectedListener change_Diseqc_Type = new SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {

            Log.d(TAG, "onItemSelected: Diseqc Type");

            super.onItemSelected(parent, view, position, id);

            diseqcOffLayout.setVisibility(INVISIBLE);
            diseqc10Layout.setVisibility(INVISIBLE);
            diseqc12Layout.setVisibility(INVISIBLE);
            sprPage.setVisibility(INVISIBLE);


            int pos = transferPositionToDiseqcType(position);//Scoty 20180831 modify antenna rule by antenna type
            if (pos == DISEQC_TYPE_OFF)
            {
                diseqcOffLayout.setVisibility(VISIBLE);
            }
            else if (pos == DISEQC_TYPE_1_0)//Scoty 20180831 modify antenna rule by antenna type
            {
                diseqc10Layout.setVisibility(VISIBLE);
            }
            else if (pos == DISEQC_TYPE_1_2)//Scoty 20180831 modify antenna rule by antenna type
            {
                diseqc12Layout.setVisibility(VISIBLE);
                sprPage.setVisibility(VISIBLE);
                sprPage.setSelection(0);

                int dsc12Pos1 = positionSat[1]; // update button text when switch to Diseqc 1.2
                int dsc12Pos2 = positionSat[2];
                int dsc12Pos3 = positionSat[3];
                int dsc12Pos4 = positionSat[4];
                btnPos1.setText(strNone);
                btnPos2.setText(strNone);
                btnPos3.setText(strNone);
                btnPos4.setText(strNone);
                if (dsc12Pos1 >= 0) btnPos1.setText(strSatList[dsc12Pos1]);
                if (dsc12Pos2 >= 0) btnPos2.setText(strSatList[dsc12Pos2]);
                if (dsc12Pos3 >= 0) btnPos3.setText(strSatList[dsc12Pos3]);
                if (dsc12Pos4 >= 0) btnPos4.setText(strSatList[dsc12Pos4]);
            }
            else {
                Log.d(TAG, "onItemSelected: diseqc not select");
            }
        }
    };

    private SelectBoxOnItemSelectedListener change_Page = new SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int page, long id)
        {

            Log.d(TAG, "onItemSelected: select PAGE");
            super.onItemSelected(parent, view, page, id);

            if (stopSelect)
            {
                stopSelect = false;
                return;
            }

            String posStr = getString(R.string.STR_POSITION);
            int position1 = (page * 4) + 1;
            int position2 = (page * 4) + 2;
            int position3 = (page * 4) + 3;
            int position4 = (page * 4) + 4;

            // set position string
            txvPosition1.setText(posStr.concat(String.valueOf(position1)));
            txvPosition2.setText(posStr.concat(String.valueOf(position2)));
            txvPosition3.setText(posStr.concat(String.valueOf(position3)));
            txvPosition4.setText(posStr.concat(String.valueOf(position4)));

            // set button text
            btnPos1.setText(strNone);
            btnPos2.setText(strNone);
            btnPos3.setText(strNone);
            btnPos4.setText(strNone);
            if (positionSat[position1] >= 0) btnPos1.setText(strSatList[positionSat[position1]]);
            if (positionSat[position2] >= 0) btnPos2.setText(strSatList[positionSat[position2]]);
            if (positionSat[position3] >= 0) btnPos3.setText(strSatList[positionSat[position3]]);
            if (positionSat[position4] >= 0) btnPos4.setText(strSatList[positionSat[position4]]);
        }
    };

    SelectBoxtOnFocusChangeListener change_HelpInfo = new SelectBoxtOnFocusChangeListener()
    {
        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {

            Log.d(TAG, "onFocusChange: change help info");

            super.onFocusChange(v, hasFocus);
            TextView dsc10_Box = (TextView) findViewById(R.id.antImage_center);
            dsc10_Box.setHeight(1);

            help.resetHelp(1,0,null);
            help.resetHelp(2,0,null);
            help.resetHelp(3,0,null);
            help.resetHelp(4,0,null);

            if (hasFocus)
            {
                dsc10_Box.setTextColor(Color.WHITE);
                dsc10_Box.setBackgroundResource(R.color.colorGray);
            }
            else
            {
                dsc10_Box.setTextColor(Color.BLACK);
                dsc10_Box.setBackgroundResource(android.R.color.holo_blue_dark);
            }

            if (hasFocus)
            {
                help.setHelpInfoTextBySplit(getString(R.string.STR_ANTENNA_SETUP_HELP));
            }
            else
            {
                help.setHelpInfoTextBySplit(getString(R.string.STR_ANTENNA_SETUP_HELP_SELECT_SATELLITE));

                int position = transferPositionToDiseqcType(sprDscType.getSelectedItemPosition());//Scoty 20180831 modify antenna rule by antenna type
                if (position == DISEQC_TYPE_OFF)//Scoty 20180831 modify antenna rule by antenna type
                {
                    help.resetHelp(1, R.drawable.help_blue, getString(R.string.STR_DELETE));
                    help.setHelpIconClickListener(1, new OnClickListener() {    // Johnny 20181228 for mouse control
                        @Override
                        public void onClick(View view) {
                            DeleteOne();
                        }
                    });
                }
                else
                {
                    help.resetHelp(1, R.drawable.help_red, getString(R.string.STR_DELETE_ALL));
                    help.resetHelp(2, R.drawable.help_blue, getString(R.string.STR_DELETE));

                    // Johnny 20181228 for mouse control -s
                    help.setHelpIconClickListener(1, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean isDiseqc10 = btnPortA.hasFocus() || btnPortB.hasFocus() || btnPortC.hasFocus() || btnPortD.hasFocus();
                            boolean isDiseqc12 = btnPos1.hasFocus() || btnPos2.hasFocus() || btnPos3.hasFocus() || btnPos4.hasFocus();

                            DeleteAll(isDiseqc10, isDiseqc12);
                        }
                    });
                    help.setHelpIconClickListener(2, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DeleteOne();
                        }
                    });
                    // Johnny 20181228 for mouse control -e
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antenna_setup);
        if(AvControlGetPlayStatus(ViewHistory.getPlayId()) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue()) {
            AvControlPlayStop(ViewHistory.getPlayId()); // Johnny 20180816 stop av when enter AntennaSetup
        }

        strNone = getString(R.string.STR_NONE);

        InitView();
        InitSatTp();

        if (satList.size() == 0)
        {
            TextView txvType = (TextView) findViewById(R.id.typeTxv);
            txvType.setVisibility(INVISIBLE);
            sprDscType.setVisibility(INVISIBLE);
            sprPage.setVisibility(INVISIBLE);
            help.setVisibility(INVISIBLE);
            new MessageDialogView(this, "No sat list", 3000) {
                @Override
                public void dialogEnd() {
                    finish();
                }
            }.show();
            return;
        }

        InitButton();
        InitSpinner();
        InitDiseqc();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(TAG, "onKeyDown: ");

        boolean isDiseqc10 = btnPortA.hasFocus() || btnPortB.hasFocus() || btnPortC.hasFocus() || btnPortD.hasFocus();
        boolean isDiseqc12 = btnPos1.hasFocus() || btnPos2.hasFocus() || btnPos3.hasFocus() || btnPos4.hasFocus();

        switch (keyCode) {

            case KEYCODE_PROG_RED: // DELETE ALL
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                DeleteAll(isDiseqc10, isDiseqc12);
                break;

            case KEYCODE_PROG_BLUE: // DELETE
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                DeleteOne();
                break;

            case KEYCODE_DPAD_LEFT:
                if (isDiseqc12)
                {
                    Log.d(TAG, "onKeyDown: switch to LEFT page");
                    int page = sprPage.getSelectedItemPosition();
                    int index = (page == 0) ? 7 : page - 1;
                    sprPage.setSelection(index);
                }
                break;

            case KEYCODE_DPAD_RIGHT:
                if (isDiseqc12)
                {
                    Log.d(TAG, "onKeyDown: switch to RIGHT page");
                    int page = sprPage.getSelectedItemPosition();
                    int index = (page == 7) ? 0 : page + 1;
                    sprPage.setSelection(index);
                }
                break;

            case KEYCODE_BACK:
                SaveTable(EnTableType.SATELLITE);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void DeleteOne()
    {
        Log.d(TAG, "DeleteOne: ");

        int diseqcType = sprDscType.getSelectedItemPosition();
        diseqcType = transferPositionToDiseqcType(diseqcType);//Scoty 20180831 modify antenna rule by antenna type
        switch (diseqcType)
        {
            case DISEQC_TYPE_OFF: { // Deseqc Off Delete One
                btnOFF.setText(strNone);
                portOFFsat = 0;
                return;
            }

            case DISEQC_TYPE_1_0: { // Deseqc 1.0 Delete One

                if (btnPortA.hasFocus() && (portAsat >= 0)) {
                    ResetButton(DISEQC_PORT_A);
                    SetDiseqcNotUse(portAsat, DISEQC_TYPE_1_0);
                    portAsat = -1;
                }
                else if (btnPortB.hasFocus() && (portBsat >= 0)) {
                    ResetButton(DISEQC_PORT_B);
                    SetDiseqcNotUse(portBsat, DISEQC_TYPE_1_0);
                    portBsat = -1;
                }
                else if (btnPortC.hasFocus() && (portCsat >= 0)) {
                    ResetButton(DISEQC_PORT_C);
                    SetDiseqcNotUse(portCsat, DISEQC_TYPE_1_0);
                    portCsat = -1;
                }
                else if (btnPortD.hasFocus() && (portDsat >= 0)) {
                    ResetButton(DISEQC_PORT_D);
                    SetDiseqcNotUse(portDsat, DISEQC_TYPE_1_0);
                    portDsat = -1;
                }
                return;
            }

            case DISEQC_TYPE_1_2: {
                TextView curBtn;
                int posIndex = (sprPage.getSelectedItemPosition() * 4);

                if (btnPos1.hasFocus())
                {
                    curBtn = btnPos1;
                    posIndex = posIndex + 1;
                }
                else if (btnPos2.hasFocus())
                {
                    curBtn = btnPos2;
                    posIndex = posIndex + 2;
                }
                else if (btnPos3.hasFocus())
                {
                    curBtn = btnPos3;
                    posIndex = posIndex + 3;
                }
                else if (btnPos4.hasFocus())
                {
                    curBtn = btnPos4;
                    posIndex = posIndex + 4;
                }
                else
                {
                    return;
                }

                if (positionSat[posIndex] >= 0)
                {
                    SetDiseqcNotUse(positionSat[posIndex], DISEQC_TYPE_1_2);
                    positionSat[posIndex] = -1;
                    curBtn.setText(strNone);
                }
            }
        }
    }

    private void DeleteAll(boolean isDiseqc10, boolean isDiseqc12)
    {
        Log.d(TAG, "DeleteAll: ");

        if (isDiseqc10) // Diseqc 1.0 DELETE ALL
        {
            EmptyDiseqc(DISEQC_TYPE_1_0);
        }
        else if (isDiseqc12) // Diseqc 1.2 DELETE ALL
        {
            EmptyDiseqc(DISEQC_TYPE_1_2);
        }

        UpdateSatList();
    }

    private void InitView()
    {
        Log.d(TAG, "InitView: ");

        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.title_layout);
        help = (ActivityHelpView) findViewById(R.id.help_layout);
        sprDscType = (Spinner) findViewById(R.id.diseqc_type_spinner);
        sprPage = (Spinner) findViewById(R.id.page_1_to_8_spinner);
        diseqc10Layout = (ConstraintLayout) findViewById(R.id.diseqc10_ConstraintLayout);
        diseqc12Layout = (ConstraintLayout) findViewById(R.id.diseqc12_ConstraintLayout);
        diseqcOffLayout = (ConstraintLayout) findViewById(R.id.diseqc_off_ConstraintLayout);
        btnPortA = (TextView) findViewById(R.id.lnb1_press);
        btnPortB = (TextView) findViewById(R.id.lnb2_press);
        btnPortC = (TextView) findViewById(R.id.lnb3_press);
        btnPortD = (TextView) findViewById(R.id.lnb4_press);
        txvAngle1 = (TextView) findViewById(R.id.ant_val1);
        txvAngle2 = (TextView) findViewById(R.id.ant_val2);
        txvAngle3 = (TextView) findViewById(R.id.ant_val3);
        txvAngle4 = (TextView) findViewById(R.id.ant_val4);
        txvPosition1 = (TextView) findViewById(R.id.diseqc_pos1);
        txvPosition2 = (TextView) findViewById(R.id.diseqc_pos2);
        txvPosition3 = (TextView) findViewById(R.id.diseqc_pos3);
        txvPosition4 = (TextView) findViewById(R.id.diseqc_pos4);
        btnPos1 = (TextView) findViewById(R.id.pos1_press);
        btnPos2 = (TextView) findViewById(R.id.pos2_press);
        btnPos3 = (TextView) findViewById(R.id.pos3_press);
        btnPos4 = (TextView) findViewById(R.id.pos4_press);
        btnOFF = (TextView) findViewById(R.id.diseqc_off_press);
        title.setTitleView(getString(R.string.STR_ANTENNA_SETUP_TITLE));
    }

    private void InitSatTp() // get SAT list & TP list
    {
        Log.d(TAG, "InitSatTp: ");

        // Satellite
        satList = SatInfoGetList(TpInfo.DVBS);
        strSatList = new String[satList.size()];

        for(int i = 0; i< satList.size(); i++)
        {
            String name = satList.get(i).getSatName();
            String angle = Float.toString(satList.get(i).getAngle());
            String angleWE = (satList.get(i).getAngleEW() == ANGLE_W) ? "W" : "E";

            strSatList[i] = (name + " " + angle + angleWE);
            Log.d(TAG, "InitSatTp: strSatList[i] = "+strSatList[i]);
        }

        // Transponder
        if (satList.size() != 0)
        {
            tpList = TpInfoGetListBySatId(satList.get(0).getSatId());
        }
        else
        {
            tpList = null;
        }
    }

    private void InitButton()
    {
        Log.d(TAG, "InitButton: ");

        final Context context = AntennaSetupActivity.this;

        OnClickListener onClickEditButton = new OnClickListener() { // Edit LNB when click LNB button
            @Override
            public void onClick(View view) {

                TextView curBtn = (TextView) view;
                int diseqcType  = sprDscType.getSelectedItemPosition();
                int page        = sprPage.getSelectedItemPosition();
                int positionIndex   = -1;
                int dscPort         = -1;

                diseqcType = transferPositionToDiseqcType(diseqcType);//Scoty 20180831 modify antenna rule by antenna type
                if (curBtn == btnOFF)           dscPort = -1;
                else if (curBtn == btnPortA)    dscPort = DISEQC_PORT_A;
                else if (curBtn == btnPortB)    dscPort = DISEQC_PORT_B;
                else if (curBtn == btnPortC)    dscPort = DISEQC_PORT_C;
                else if (curBtn == btnPortD)    dscPort = DISEQC_PORT_D;
                else if (curBtn == btnPos1)     positionIndex = (page*4)+1;
                else if (curBtn == btnPos2)     positionIndex = (page*4)+2;
                else if (curBtn == btnPos3)     positionIndex = (page*4)+3;
                else if (curBtn == btnPos4)     positionIndex = (page*4)+4;

                if ((diseqcType == DISEQC_TYPE_OFF) || (diseqcType == DISEQC_TYPE_1_0))
                {
                    DialogDiseqc10(context, dscPort, curBtn);
                }
                else if (diseqcType == DISEQC_TYPE_1_2)
                {
                    DialogDiseqc12(context, positionIndex, curBtn);
                }
            }
        };
        btnPortA.setOnClickListener(onClickEditButton);
        btnPortB.setOnClickListener(onClickEditButton);
        btnPortC.setOnClickListener(onClickEditButton);
        btnPortD.setOnClickListener(onClickEditButton);
        btnPos1.setOnClickListener(onClickEditButton);
        btnPos2.setOnClickListener(onClickEditButton);
        btnPos3.setOnClickListener(onClickEditButton);
        btnPos4.setOnClickListener(onClickEditButton);
        btnOFF.setOnClickListener(onClickEditButton);

        InitHighlight(); // Draw connection line when focus LNB button
    }

    private void InitHighlight()
    {
        Log.d(TAG, "InitHighlight: ");

        //final TextView diseqc10Square = (TextView) findViewById(R.id.antImage_center);
        final int focusColor = getColor(android.R.color.holo_blue_dark);
        final int unfocusColor = Color.WHITE;

        final ImageView lnbConnectMiddle;
        final ImageView lnb1Connect1, lnb1Connect2, lnb1Connect3, lnb1Connect4;
        final ImageView lnb2Connect1, lnb2Connect2, lnb2Connect3, lnb2Connect4;
        final ImageView lnb3Connect1, lnb3Connect2, lnb3Connect3, lnb3Connect4;
        final ImageView lnb4Connect1, lnb4Connect2, lnb4Connect3, lnb4Connect4;
        final ImageView diseqcOffLine;
        final ImageView posLine0, posLine1, posLine2, posLine3, posLine4,
                  posLine1_1, posLine1_2, posLine1_3, posLine1_4;


        lnbConnectMiddle = (ImageView) findViewById(R.id.antLine_base_6);
        lnb1Connect1 = (ImageView) findViewById(R.id.antLine_base_1);
        lnb1Connect2 = (ImageView) findViewById(R.id.connect_1_1);
        lnb1Connect3 = (ImageView) findViewById(R.id.connect_1_2);
        lnb1Connect4 = (ImageView) findViewById(R.id.antLine_base_8);
        lnb2Connect1 = (ImageView) findViewById(R.id.antLine_base_2);
        lnb2Connect2 = (ImageView) findViewById(R.id.connect_2_1);
        lnb2Connect3 = (ImageView) findViewById(R.id.connect_2_2);
        lnb2Connect4 = (ImageView) findViewById(R.id.antLine_base_9);
        lnb3Connect1 = (ImageView) findViewById(R.id.antLine_base_3);
        lnb3Connect2 = (ImageView) findViewById(R.id.connect_3_1);
        lnb3Connect3 = (ImageView) findViewById(R.id.connect_3_3);
        lnb3Connect4 = (ImageView) findViewById(R.id.antLine_base_10);
        lnb4Connect1 = (ImageView) findViewById(R.id.antLine_base_4);
        lnb4Connect2 = (ImageView) findViewById(R.id.connect_4_1);
        lnb4Connect3 = (ImageView) findViewById(R.id.connect_4_4);
        lnb4Connect4 = (ImageView) findViewById(R.id.antLine_base_11);
        posLine0 = (ImageView) findViewById(R.id.antLine_base_12);
        posLine1_1 = (ImageView) findViewById(R.id.antLine_base_13_1);
        posLine1_2 = (ImageView) findViewById(R.id.antLine_base_13_2);
        posLine1_3 = (ImageView) findViewById(R.id.antLine_base_13_3);
        posLine1_4 = (ImageView) findViewById(R.id.antLine_base_13_4);
        posLine1 = (ImageView) findViewById(R.id.antLine_base_14);
        posLine2 = (ImageView) findViewById(R.id.antLine_base_15);
        posLine3 = (ImageView) findViewById(R.id.antLine_base_16);
        posLine4 = (ImageView) findViewById(R.id.antLine_base_17);
        diseqcOffLine = (ImageView) findViewById(R.id.antLine_diseqc_off_base);

        OnFocusChangeListener lnb1_OnFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    /*diseqc10Square.setTextColor(Color.BLACK);
                    diseqc10Square.setBackgroundResource(R.drawable.focus);*/
                    lnbConnectMiddle.setBackgroundColor(focusColor);
                    lnb1Connect1.setBackgroundColor(focusColor);
                    lnb1Connect2.setBackgroundColor(focusColor);
                    lnb1Connect3.setBackgroundColor(focusColor);
                    lnb1Connect4.setBackgroundColor(focusColor);
                    lnb1Connect2.setVisibility(VISIBLE);
                    lnb1Connect3.setVisibility(VISIBLE);
                }
                else {
                    /*diseqc10Square.setTextColor(Color.WHITE);
                    diseqc10Square.setBackgroundResource(R.drawable.n_focus);*/
                    lnbConnectMiddle.setBackgroundColor(unfocusColor);
                    lnb1Connect1.setBackgroundColor(unfocusColor);
                    lnb1Connect2.setVisibility(INVISIBLE);
                    lnb1Connect3.setVisibility(INVISIBLE);
                    lnb1Connect4.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener lnb2_OnFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    /*diseqc10Square.setTextColor(Color.BLACK);
                    diseqc10Square.setBackgroundResource(R.drawable.focus);*/
                    lnbConnectMiddle.setBackgroundColor(focusColor);
                    lnb2Connect1.setBackgroundColor(focusColor);
                    lnb2Connect2.setBackgroundColor(focusColor);
                    lnb2Connect3.setBackgroundColor(focusColor);
                    lnb2Connect4.setBackgroundColor(focusColor);
                    lnb2Connect2.setVisibility(VISIBLE);
                    lnb2Connect3.setVisibility(VISIBLE);
                }
                else {
                    /*diseqc10Square.setTextColor(Color.WHITE);
                    diseqc10Square.setBackgroundResource(R.drawable.n_focus);*/
                    lnbConnectMiddle.setBackgroundColor(unfocusColor);
                    lnb2Connect1.setBackgroundColor(unfocusColor);
                    lnb2Connect2.setVisibility(INVISIBLE);
                    lnb2Connect3.setVisibility(INVISIBLE);
                    lnb2Connect4.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener lnb3_OnFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    /*diseqc10Square.setTextColor(Color.BLACK);
                    diseqc10Square.setBackgroundResource(R.drawable.focus);*/
                    lnbConnectMiddle.setBackgroundColor(focusColor);
                    lnb3Connect1.setBackgroundColor(focusColor);
                    lnb3Connect2.setBackgroundColor(focusColor);
                    lnb3Connect3.setBackgroundColor(focusColor);
                    lnb3Connect4.setBackgroundColor(focusColor);
                    lnb3Connect2.setVisibility(VISIBLE);
                    lnb3Connect3.setVisibility(VISIBLE);
                }
                else {
                    /*diseqc10Square.setTextColor(Color.WHITE);
                    diseqc10Square.setBackgroundResource(R.drawable.n_focus);*/
                    lnbConnectMiddle.setBackgroundColor(unfocusColor);
                    lnb3Connect1.setBackgroundColor(unfocusColor);
                    lnb3Connect2.setVisibility(INVISIBLE);
                    lnb3Connect3.setVisibility(INVISIBLE);
                    lnb3Connect4.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener lnb4_OnFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    /*diseqc10Square.setTextColor(Color.BLACK);
                    diseqc10Square.setBackgroundResource(R.drawable.focus);*/
                    lnbConnectMiddle.setBackgroundColor(focusColor);
                    lnb4Connect1.setBackgroundColor(focusColor);
                    lnb4Connect2.setBackgroundColor(focusColor);
                    lnb4Connect3.setBackgroundColor(focusColor);
                    lnb4Connect4.setBackgroundColor(focusColor);
                    lnb4Connect2.setVisibility(VISIBLE);
                    lnb4Connect3.setVisibility(VISIBLE);
                }
                else {
                    /*diseqc10Square.setTextColor(Color.WHITE);
                    diseqc10Square.setBackgroundResource(R.drawable.n_focus);*/
                    lnbConnectMiddle.setBackgroundColor(unfocusColor);
                    lnb4Connect1.setBackgroundColor(unfocusColor);
                    lnb4Connect2.setVisibility(INVISIBLE);
                    lnb4Connect3.setVisibility(INVISIBLE);
                    lnb4Connect4.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener btnPos1onFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    posLine0.setBackgroundColor(focusColor);
                    posLine1_1.setBackgroundColor(focusColor);
                    posLine1_1.setVisibility(VISIBLE);
                    posLine1.setBackgroundColor(focusColor);
                }
                else {
                    posLine0.setBackgroundColor(unfocusColor);
                    posLine1_1.setBackgroundColor(unfocusColor);
                    posLine1_1.setVisibility(INVISIBLE);
                    posLine1.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener btnPos2onFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    posLine0.setBackgroundColor(focusColor);
                    posLine1_2.setBackgroundColor(focusColor);
                    posLine1_2.setVisibility(VISIBLE);
                    posLine2.setBackgroundColor(focusColor);
                }
                else {
                    posLine0.setBackgroundColor(unfocusColor);
                    posLine1_2.setBackgroundColor(unfocusColor);
                    posLine1_2.setVisibility(INVISIBLE);
                    posLine2.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener btnPos3onFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    posLine0.setBackgroundColor(focusColor);
                    posLine1_3.setBackgroundColor(focusColor);
                    posLine1_3.setVisibility(VISIBLE);
                    posLine3.setBackgroundColor(focusColor);
                }
                else {
                    posLine0.setBackgroundColor(unfocusColor);
                    posLine1_3.setBackgroundColor(unfocusColor);
                    posLine1_3.setVisibility(INVISIBLE);
                    posLine3.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener btnPos4onFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    posLine0.setBackgroundColor(focusColor);
                    posLine1_4.setBackgroundColor(focusColor);
                    posLine1_4.setVisibility(VISIBLE);
                    posLine4.setBackgroundColor(focusColor);
                }
                else {
                    posLine0.setBackgroundColor(unfocusColor);
                    posLine1_4.setBackgroundColor(unfocusColor);
                    posLine1_4.setVisibility(INVISIBLE);
                    posLine4.setBackgroundColor(unfocusColor);
                }
            }
        };
        OnFocusChangeListener offLineOnFocus = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    diseqcOffLine.setBackgroundColor(focusColor);
                }
                else {
                    diseqcOffLine.setBackgroundColor(unfocusColor);
                }
            }
        };

        btnPortA.setOnFocusChangeListener(lnb1_OnFocus);
        btnPortB.setOnFocusChangeListener(lnb2_OnFocus);
        btnPortC.setOnFocusChangeListener(lnb3_OnFocus);
        btnPortD.setOnFocusChangeListener(lnb4_OnFocus);
        btnPos1.setOnFocusChangeListener(btnPos1onFocus);
        btnPos2.setOnFocusChangeListener(btnPos2onFocus);
        btnPos3.setOnFocusChangeListener(btnPos3onFocus);
        btnPos4.setOnFocusChangeListener(btnPos4onFocus);
        btnOFF.setOnFocusChangeListener(offLineOnFocus);
    }

    private void InitSpinner() {

        Log.d(TAG, "InitSpinner: ");

        new SelectBoxView(this, sprDscType, getResources().getStringArray(R.array.STR_DISEQC));
        new SelectBoxView(this, sprPage, getResources().getStringArray(R.array.STR_PAGE));

        sprDscType.setOnItemSelectedListener(change_Diseqc_Type);
        sprDscType.setOnFocusChangeListener(change_HelpInfo);
        sprPage.setOnItemSelectedListener(change_Page);
        sprPage.setOnFocusChangeListener(change_HelpInfo);
    }

    private void InitDiseqc()
    {
        Log.d(TAG, "InitDiseqc: ");

        for (int i = 0; i < positionSat.length; i++)
        {
            positionSat[i] = -1;
        }

        int selectedType = InitDiseqcType();
        int selectPos = transferDiseqcTypeToPosition(selectedType);//Scoty 20180831 modify antenna rule by antenna type
        Log.d(TAG, "InitDiseqc: selectedType = "+selectedType);

        if (selectedType == DISEQC_TYPE_OFF)
        {
            sprDscType.setSelection(selectPos);//Scoty 20180831 modify antenna rule by antenna type
            InitDiseqcOff();
        }
        else if (selectedType == DISEQC_TYPE_1_0)
        {
            sprDscType.setSelection(selectPos);//Scoty 20180831 modify antenna rule by antenna type
            InitDiseqc10();
        }
        else if (selectedType == DISEQC_TYPE_1_2)
        {
            sprDscType.setSelection(selectPos);//Scoty 20180831 modify antenna rule by antenna type
            InitDiseqc12();
        }
    }

    private int InitDiseqcType() {

        Log.d(TAG, "InitDiseqcType: satList.size() = "+satList.size());

        for (int i = 0; i < satList.size() ; i++)
        {
            int diseqcType = satList.get(i).Antenna.getDiseqcType();
            if(diseqcType != DISEQC_TYPE_NONE) {//Scoty 20180831 modify antenna rule by antenna type
                Log.d(TAG, "InitDiseqcType: diseqcType = " + diseqcType + " i == " + i);
                return diseqcType;
            }
        }
        return DISEQC_TYPE_OFF;
    }

    private void InitDiseqc10()
    {
        Log.d(TAG, "InitDiseqc10: ");

        for (int i = 0; i < satList.size(); i++)
        {
            SatInfo satInfo = satList.get(i);

            if (satInfo.Antenna.getDiseqcType() == DISEQC_TYPE_1_0)
            {
                //boolean dscUse  = satInfo.Antenna.getDiseqcUse() == 1;
                int     dscPort = satInfo.Antenna.getDiseqc();
                String  angle   = satInfo.getAngle()+" °";

                //if (dscUse)
                {
                    if (dscPort == DISEQC_PORT_A)
                    {
                        btnPortA.setText(strSatList[i]);
                        txvAngle1.setText(angle);
                        portAsat = i;
                    }
                    else if (dscPort == DISEQC_PORT_B)
                    {
                        btnPortB.setText(strSatList[i]);
                        txvAngle2.setText(angle);
                        portBsat = i;
                    }
                    else if (dscPort == DISEQC_PORT_C)
                    {
                        btnPortC.setText(strSatList[i]);
                        txvAngle3.setText(angle);
                        portCsat = i;
                    }
                    else if (dscPort == DISEQC_PORT_D)
                    {
                        btnPortD.setText(strSatList[i]);
                        txvAngle4.setText(angle);
                        portDsat = i;
                    }
                }
            }
        }
    }

    private void InitDiseqc12()
    {
        Log.d(TAG, "InitDiseqc12: ");

        for (int i = 0; i < satList.size(); i++)
        {
            SatInfo satInfo = satList.get(i);

            if (satInfo.Antenna.getDiseqcType() == DISEQC_TYPE_1_2)
            {
                int posIndex = satInfo.getPostionIndex();

                if (posIndex != 0) // satInfo did not use if PositionIndex is 0
                {
                    positionSat[posIndex] = i; // PositionIndex should be 1~32

                    if (posIndex == 1)      btnPos1.setText(strSatList[i]);
                    else if (posIndex == 2) btnPos2.setText(strSatList[i]);
                    else if (posIndex == 3) btnPos3.setText(strSatList[i]);
                    else if (posIndex == 4) btnPos4.setText(strSatList[i]);
                }
                else
                {
                    Log.d(TAG, "InitDiseqc12: PostionIndex 0 not use;");
                }
            }
        }
    }

    private void InitDiseqcOff()
    {
        Log.d(TAG, "InitDiseqcOff: ");

        portOFFsat = 0;
        for (int i = 0; i < satList.size() ; i++) {//Scoty 20180831 modify antenna rule by antenna type
            if(satList.get(i).Antenna.getDiseqcType() == DISEQC_TYPE_OFF) {
                Log.d(TAG, "InitDiseqcOff: ==>> i == " + i + " type " + satList.get(i).Antenna.getDiseqcType());
                btnOFF.setText(strSatList[i]);
                portOFFsat = i;
                return;
            }
        }

        btnOFF.setText(strSatList[0]);
    }

    private void DialogDiseqc10(Context context, int diseqcPort, final TextView curBtn)
    {
        Log.d(TAG, "DialogDiseqc10: ");

        int curSat
                = diseqcPort == DISEQC_PORT_OFF ?   portOFFsat
                : diseqcPort == DISEQC_PORT_A ?     portAsat
                : diseqcPort == DISEQC_PORT_B ?     portBsat
                : diseqcPort == DISEQC_PORT_C ?     portCsat
                : diseqcPort == DISEQC_PORT_D ?     portDsat
                : 0;
        if (curSat == -1)
        {
            curSat = 0;
        }

        String buttonString = curBtn.getText().toString();
        boolean clickNONE = buttonString.equals(getString(R.string.STR_NONE));

        new EditDiseqc10Dialog(context, diseqcPort, curSat, clickNONE, satList, help)
        {
            @Override
            public void UpdateSetting(int diseqcPort,
                                      int curSat,
                                      int lnbType,
                                      int lowFreq,
                                      int highFreq,
                                      int tone22k)
            {
                Log.d(TAG, "UpdateSetting: ");

                int diseqcType = (diseqcPort == DISEQC_PORT_OFF) ? DISEQC_TYPE_OFF : DISEQC_TYPE_1_0;

                // ================ Empty Diseqc Data ================
                // if Diseqc 1.0 set Diseqc Off Button "None" & index "0"
                // if Diseqc Off set Diseqc 1.0 Button "None" & index "-1"
                if (diseqcType == DISEQC_TYPE_OFF)
                {
                    // Empty Diseqc 1.0
                    EmptyDiseqc(DISEQC_TYPE_OFF);//Scoty 20180831 modify antenna rule by antenna type
                    EmptyDiseqc(DISEQC_TYPE_1_0);
                    EmptyDiseqc(DISEQC_TYPE_1_2);
                }
                else
                {
                    // Empty Diseqc Off
                    EmptyDiseqc(DISEQC_TYPE_OFF);
                    EmptyDiseqc(DISEQC_TYPE_1_2);
                    EmptyOnePort(curSat); // Reset according to same satellite with different PORT
                }

                UpdatePortSatInfo(curSat, lnbType, lowFreq, highFreq, tone22k, diseqcPort);
                UpdatePortLayout(diseqcPort, curSat, curBtn);
            }
        }.show();
    }

    private void DialogDiseqc12(Context context, int positionIndex, final TextView curBtn) {

        Log.d(TAG, "DialogDiseqc12: positionIndex = "+positionIndex);

        boolean clickNONE = curBtn.getText().equals(getString(R.string.STR_NONE));

        new EditDiseqc12Dialog(context, help, clickNONE, positionIndex, positionSat, satList, tpList)
        {
            @Override
            public void UpdateSetting(int curSatPos, int positionIndex)
            {
                Log.d(TAG, "UpdateSetting: positionIndex = "+positionIndex);

                // Remove Diseqc 1.0 & Diseqc Off Sat
                EmptyDiseqc(DISEQC_TYPE_OFF);
                EmptyDiseqc(DISEQC_TYPE_1_0);
                EmptyOnePosition(positionIndex, curSatPos); // do not use other position with same sat

                UpdatePositionSatInfo(curSatPos, positionIndex);
                curBtn.setText(strSatList[curSatPos]);
            }
        }.show();
    }

    private void EmptyDiseqc(int diseqcType)
    {
        Log.d(TAG, "EmptyDiseqc: ");

        if (diseqcType == DISEQC_TYPE_OFF)
        {
            for(int i = 0 ; i < satList.size() ; i++)//Scoty 20180831 modify antenna rule by antenna type
            {
                if(satList.get(i).Antenna.getDiseqcType() == DISEQC_TYPE_OFF) {
                    satList.get(i).Antenna.setDiseqcType(DISEQC_TYPE_NONE);
                }
            }
            btnOFF.setText(strNone);
            portOFFsat = 0;
        }
        else if (diseqcType == DISEQC_TYPE_1_0)
        {
            ResetButton(DISEQC_PORT_A);
            ResetButton(DISEQC_PORT_B);
            ResetButton(DISEQC_PORT_C);
            ResetButton(DISEQC_PORT_D);

            for (SatInfo satInfo : satList)
            {
                if (satInfo.Antenna.getDiseqcType() == DISEQC_TYPE_1_0)
                {
                    satInfo.Antenna.setDiseqcType(DISEQC_TYPE_NONE);//Scoty 20180831 modify antenna rule by antenna type
                }
            }

            portAsat = -1;
            portBsat = -1;
            portCsat = -1;
            portDsat = -1;
        }
        else if (diseqcType == DISEQC_TYPE_1_2)
        {
            btnPos1.setText(strNone);
            btnPos2.setText(strNone);
            btnPos3.setText(strNone);
            btnPos4.setText(strNone);

            for (int i = 1; i < TOTAL_POSITION; i++)
            {
                if (positionSat[i] >= 0)
                {
                    // positionSat:         sat index of position 1~32
                    // positionSat[i]:      sat index of position "i"
                    // positionSat[i] = -1; disable satellite of position "i"
                    satList.get(positionSat[i]).setPostionIndex(0);
                    satList.get(positionSat[i]).Antenna.setDiseqcType(DISEQC_TYPE_NONE);//Scoty 20180831 modify antenna rule by antenna type
                    positionSat[i] = -1;
                }
            }
        }
    }

    private void EmptyOnePort(int curSat)
    {
        Log.d(TAG, "EmptyOnePort: ");

        DeleteOne();    // Johnny 20180816 Fix pre sat not del if you change sat in same port/position

        // Johnny 20180821 fix del wrong port -s
        // do not use other port with same satellite
        if (portAsat == curSat)
        {
            portAsat = -1;
            ResetButton(DISEQC_PORT_A);
        }
        else if (portBsat == curSat)
        {
            portBsat = -1;
            ResetButton(DISEQC_PORT_B);
        }
        else if (portCsat == curSat)
        {
            portCsat = -1;
            ResetButton(DISEQC_PORT_C);
        }
        else if (portDsat == curSat)
        {
            portDsat = -1;
            ResetButton(DISEQC_PORT_D);
        }
        // Johnny 20180821 fix del wrong port -e
    }

    private void EmptyOnePosition(int positionIndex, int curSat)
    {
        Log.d(TAG, "EmptyOnePosition: ");

        // delete same Sat at pageNum
        //    before   after
        // 1: SAT_1    _____
        // 2: _____    SAT_1
        // 3: _____    _____
        // 4: _____    _____

        DeleteOne();    // Johnny 20180816 Fix pre sat not del if you change sat in same port/position
        int page = sprPage.getSelectedItemPosition();
        positionSat[positionIndex] = curSat;

        if (positionSat[(page * 4) + 1] == curSat)    btnPos1.setText(strNone);
        if (positionSat[(page * 4) + 2] == curSat)    btnPos2.setText(strNone);
        if (positionSat[(page * 4) + 3] == curSat)    btnPos3.setText(strNone);
        if (positionSat[(page * 4) + 4] == curSat)    btnPos4.setText(strNone);

        for (int i = 1; i < TOTAL_POSITION; i++)
        {
            if (i != positionIndex) // other position
            {
                // position's sat index is same with curSat
                boolean position_use_same_sat = (positionSat[i] == curSat);

                if (position_use_same_sat) // use same satInfo
                {
                    positionSat[i] = -1; // reset SatInfo index
                    satList.get(i).Antenna.setDiseqcType(DISEQC_TYPE_NONE);//Scoty 20180831 modify antenna rule by antenna type
                }
            }
        }
    }

    private void UpdatePortLayout(int diseqcPort, int curSat, TextView curBtn)
    {
        Log.d(TAG, "UpdatePortLayout: ");

        String angle = String.valueOf(satList.get(curSat).getAngle()).concat(" °");
        int diseqcType = (diseqcPort == DISEQC_PORT_OFF) ? DISEQC_TYPE_OFF : DISEQC_TYPE_1_0;

        // ================ set new Diseqc index & TextView ================
        // set button index
        if (diseqcType == DISEQC_TYPE_OFF)      portOFFsat = curSat;
        else if (diseqcPort == DISEQC_PORT_A)   portAsat = curSat;
        else if (diseqcPort == DISEQC_PORT_B)   portBsat = curSat;
        else if (diseqcPort == DISEQC_PORT_C)   portCsat = curSat;
        else if (diseqcPort == DISEQC_PORT_D)   portDsat = curSat;

        // set angle
        if (diseqcPort == DISEQC_PORT_A)        txvAngle1.setText(angle);
        else if (diseqcPort == DISEQC_PORT_B)   txvAngle2.setText(angle);
        else if (diseqcPort == DISEQC_PORT_C)   txvAngle3.setText(angle);
        else if (diseqcPort == DISEQC_PORT_D)   txvAngle4.setText(angle);

        // set button text
        curBtn.setText(strSatList[curSat]);
    }

    private void UpdatePortSatInfo(
            int curSat, int lnbType, int lowFreq, int highFreq, int tone22k, int diseqcPort)
    {
        Log.d(TAG, "UpdatePortSatInfo: ");

        int diseqcType = (diseqcPort == DISEQC_PORT_OFF) ? DISEQC_TYPE_OFF : DISEQC_TYPE_1_0;
        List<Integer> tps = GetTps();

        satList.get(curSat).setTpNum(tpList.size());
        satList.get(curSat).setTps(tps);
        satList.get(curSat).Antenna.setLnbType(lnbType);
        satList.get(curSat).Antenna.setLnb1(lowFreq);
        satList.get(curSat).Antenna.setLnb2(highFreq);
        satList.get(curSat).Antenna.setTone22kUse( (tone22k != TONE_22K_AUTO) ? 1 : 0 );
        satList.get(curSat).Antenna.setTone22k(tone22k);
        satList.get(curSat).Antenna.setDiseqcType(diseqcType);
        //gposInfo.setLnbPower(lnbPower);

        if (diseqcType == DISEQC_TYPE_1_0)
        {
            satList.get(curSat).Antenna.setDiseqc(diseqcPort);
            //satList.get(curSat).Antenna.setDiseqcUse(1);
        }
        satList.get(curSat).Antenna.setDiseqcType(diseqcType);//Scoty 20180831 modify antenna rule by antenna type

        UpdateSatList();
        TpInfoUpdateList(tpList);
        strSatList = GetSatStringArray(satList);
    }

    private void UpdatePositionSatInfo(int curSat, int positionIndex)
    {
        Log.d(TAG, "UpdatePositionSatInfo: ");

        List<Integer> Tps = GetTps();
        satList.get(curSat).setPostionIndex(positionIndex);
        satList.get(curSat).setTpNum(tpList.size());
        satList.get(curSat).setTps(Tps);
        satList.get(curSat).Antenna.setDiseqcType(DISEQC_TYPE_1_2);

        UpdateSatList();
        TpInfoUpdateList(tpList);
        strSatList = GetSatStringArray(satList);
    }

    private void ResetButton(int diseqcPort) {

        Log.d(TAG, "ButtonReset: ");

        if (DISEQC_PORT_A == diseqcPort)
        {
            btnPortA.setText(strNone);
            txvAngle1.setText("");
        }
        else if (DISEQC_PORT_B == diseqcPort)
        {
            btnPortB.setText(strNone);
            txvAngle2.setText("");
        }
        else if (DISEQC_PORT_C == diseqcPort)
        {
            btnPortC.setText(strNone);
            txvAngle3.setText("");
        }
        else if (DISEQC_PORT_D == diseqcPort)
        {
            btnPortD.setText(strNone);
            txvAngle4.setText("");
        }
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

    private List<Integer> GetTps()
    {
        Log.d(TAG, "GetTps: ");

        List<Integer> Tps = new ArrayList<>();

        for (int i = 0; i < tpList.size(); i++) // get Tp ID list
        {
            Tps.add(tpList.get(i).getTpId());
        }

        return Tps;
    }

    private void SetDiseqcNotUse(int curSat, int diseqcType)
    {
        Log.d(TAG, "SetDiseqcNotUse: ");


        satList.get(curSat).Antenna.setDiseqcType(DISEQC_TYPE_NONE);//Scoty 20180831 modify antenna rule by antenna type

//        if (diseqcType == DISEQC_TYPE_1_0)
//        {
//            //satList.get(curSat).Antenna.setDiseqcUse(0);
//        }
//        else if (diseqcType == DISEQC_TYPE_1_2)
//        {
//            satList.get(curSat).setPostionIndex(0);
//        }

        SatInfoUpdate(satList.get(curSat));
    }

    private void UpdateSatList()
    {
        Log.d(TAG, "UpdateSatList: ");

        for (SatInfo satInfo : satList)
        {
            SatInfoUpdate(satInfo); // HISI
        }

        //SatInfoUpdateList(satList); // PESI
    }

    private void LogAll()
    {
        List<SatInfo> satList = SatInfoGetList(TpInfo.DVBS);
        for(int i = 0; i< satList.size(); i++)
        {
            Log.d(TAG, " \nLogAll: SatName = "+satList.get(i).getSatName()+"\n"+
                    "        DiseqcType = "+satList.get(i).Antenna.getDiseqcType()+
                    /*" DiseqcUse = "+satList.get(i).Antenna.getDiseqcUse()+*/
                    " Diseqc = "+satList.get(i).Antenna.getDiseqc()+
                    " PostionIndex = "+satList.get(i).getPostionIndex()
            );
        }
    }
}
