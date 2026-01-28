package com.prime.dtvplayer.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.OtaUpdateDialogView;
import com.prime.dtvplayer.View.PasswordDialogView;
import com.prime.dtvplayer.View.VMXLocationDialog;
import com.prime.dtvplayer.utils.TVMessage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by edwin on 2017/11/17.
 */

public class MainMenuActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private static int pageNum;
    private static int itemNum;
    private static int totalPageNum;
    private static boolean menuIsAvailable;
    private static SubMenuFunction subMenuFunction ;
    private final int FINISH_APK = 0xffff;

    private ImageView menu1IGV;
    private ImageView menu2IGV;
    private ImageView menu3IGV;
    private ImageView menu4IGV;
    private ImageView leftArrow;
    private ImageView rightArrow;
    private TextView menu1TXV;
    private TextView menu2TXV;
    private TextView menu3TXV;
    private TextView menu4TXV;
    private TextView pageTXV;
    private LinearLayout submenuLinearLayout;
    private LinearLayout selectedMainItemLinearLayout;
    private LinearLayout mainMenuLinearLayout;
    private String USBUpdateName = "DDN82_usb.bin";
    private String usbDevicePath1="",usbDevicePath2="";

    private int[] mainImg;
    private int[] mainItemStr;
    private int[] itemINST;
    private int[] itemCH;
    private int[] itemSET;
    private int[] itemSERV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        menu1IGV   = (ImageView)findViewById(R.id.menu1Img);
        menu2IGV   = (ImageView)findViewById(R.id.menu2Img);
        menu3IGV   = (ImageView)findViewById(R.id.menu3Img);
        menu4IGV   = (ImageView)findViewById(R.id.menu4Img);
        leftArrow  = (ImageView)findViewById(R.id.left);
        rightArrow = (ImageView)findViewById(R.id.right);
        menu1TXV   = (TextView)findViewById(R.id.menu1Txv);
        menu2TXV   = (TextView)findViewById(R.id.menu2Txv);
        menu3TXV   = (TextView)findViewById(R.id.menu3Txv);
        menu4TXV   = (TextView)findViewById(R.id.menu4Txv);
        pageTXV    = (TextView)findViewById(R.id.page);
        mainMenuLinearLayout         = (LinearLayout) findViewById(R.id.main_menu_layout);
        submenuLinearLayout          = (LinearLayout) findViewById(R.id.submenuLayout);
        selectedMainItemLinearLayout = (LinearLayout) findViewById(R.id.selectedLayout);

        Init();
    }

    private void Init() {
        Log.d(TAG, "Init()");
//        Toast.makeText(this, TAG + " started", Toast.LENGTH_SHORT).show();

        InitMenu();

        subMenuFunction = new SubMenuFunction();
        pageNum = 1;
        itemNum = 0;//fix crash
        totalPageNum = mainItemStr.length;
        pageTXV.setText(
                String.valueOf(pageNum)
                        .concat("/")
                        .concat(String.valueOf(totalPageNum))
        ); // init Page Number

        GradientDrawable border = new GradientDrawable(); // show yellow frame
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(3, Color.YELLOW);
        border.setCornerRadius(3);
        selectedMainItemLinearLayout.setBackground(border);

        ShowMainImage();
        ShowSubmenu();

        //mainMenuLinearLayout.setVisibility(LinearLayout.GONE); // hide main  menu //eric lin 20180116 show first main menu, mark
        //submenuLinearLayout.setVisibility(LinearLayout.GONE); //eric lin 20180116 show first main menu, mark
        menuIsAvailable = true;//eric lin 20180116 show first main menu, modify false to true
    }

    private void InitMenu()
    {
        mainImg = new int[] {
                R.drawable.installation,
                R.drawable.channel,
                R.drawable.setting,
                R.drawable.mail_time
        };
        mainItemStr = new int[] {
                R.string.STR_INSTALLATION,
                R.string.STR_CHANNEL,
                R.string.STR_SETTING,
                R.string.STR_SERVICE
        };

        if (GetCurTunerType() == TpInfo.DVBS)
        {
            itemINST = new int[] {
                    R.string.STR_CHANNEL_SEARCH,
                    R.string.STR_ANTENNA_SETUP,
                    R.string.STR_RECEIVER_INFORMATION,
                    R.string.STR_SIGNAL_INFORMATION,
                    R.string.STR_USB_UPDATE,
                    //R.string.STR_OTA_UPDATE   // Johnny 20180816 mark ota update in dvbs
                    //R.string.STR_CA_INFORMATION// for VMX need open/close
                    //R.string.STR_MAIL // for VMX need open/close
                    //R.string.STR_LOCATION_INFO // for VMX need open/close
            };
        }
        else    // dvbc, isdbt, dvbt
        {
            itemINST = new int[] {
                    R.string.STR_CHANNEL_SEARCH,
                    R.string.STR_RECEIVER_INFORMATION,
                    R.string.STR_SIGNAL_INFORMATION,
                    R.string.STR_USB_UPDATE,
                    //R.string.STR_OTA_UPDATE
                    //R.string.STR_CA_INFORMATION // for VMX need open/close
                    //R.string.STR_MAIL  // for VMX need open/close
                    //R.string.STR_LOCATION_INFO // for VMX need open/close
            };
        }

        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            itemCH = new int[]{
                R.string.STR_TV_CHANNEL,
                R.string.STR_RADIO_CHANNEL,
                R.string.STR_TV_PROGRAMME_GUIDE,
                R.string.STR_RECORDS_LIST
                //R.string.STR_TV_DIMENSION_EPG
        };
        }else{
            itemCH = new int[]{
                    R.string.STR_TV_CHANNEL,
                    R.string.STR_RADIO_CHANNEL,
                    R.string.STR_TV_PROGRAMME_GUIDE,
                    //R.string.STR_TV_DIMENSION_EPG
            };
        }
        itemSET = new int[] {
                R.string.STR_LANGUAGE_SETTINGS,
                R.string.STR_OUTPUT_SETTINGS,
                R.string.STR_SYSTEM_SETTINGS,
                R.string.STR_PARENTAL_CONTROL,
                R.string.STR_RESET_FACTORY_DEFAULT
        };
        itemSERV = new int[] {
                R.string.STR_TIMER,
                R.string.STR_TIME_ADJUST,
                R.string.STR_STORAGE_SETTINGS,
                R.string.STR_RECORD_SETTINGS
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown(): key code = " + keyCode);

        switch (keyCode) {
            case KeyEvent.KEYCODE_M:
            case KeyEvent.KEYCODE_MENU:
                menuIsAvailable = SwitchMenu();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if(menuIsAvailable)
                    MoveUp();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(menuIsAvailable)
                    MoveDown();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(menuIsAvailable)
                    MoveLeft();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(menuIsAvailable)
                    MoveRight();
                break;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if(menuIsAvailable)
                    ActivateSelectedItem();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: menuIsAvailable = " + menuIsAvailable);
        if(GetChannelExist() == 0)
        {
            Log.d(TAG, "onBackPressed: FINISH_APK = " + FINISH_APK);
            setResult(FINISH_APK, getIntent());
            MainMenuActivity.this.finish();
        }
        else {
            if (menuIsAvailable)
                menuIsAvailable = SwitchMenu();
            else {
                super.onBackPressed();
                Log.d(TAG, "onActivityResult: GetChannelExist() == " + GetChannelExist());

            }
        }
    }
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus)
//        Log.d(TAG, "onWindowFocusChanged: "
//                //+"focus = "+ hasFocus +"\n\t"
//                +"\n    gpos 1st lang = "+ GposInfoGet().getAudioLanguageSelection(0));
//
//    }


    public boolean SwitchMenu() {
        Log.d(TAG, "SwitchMenu()");
        if(GetChannelExist() != 1)
            return true;
        if (mainMenuLinearLayout.getVisibility() == View.GONE) { // turn on menu
            mainMenuLinearLayout.setVisibility(View.VISIBLE);
            submenuLinearLayout.setVisibility(View.VISIBLE);
            return true;
        }
        else if (mainMenuLinearLayout.getVisibility() == View.VISIBLE) { // turn off menu
            mainMenuLinearLayout.setVisibility(View.GONE);
            submenuLinearLayout.setVisibility(View.GONE);
            super.onBackPressed();
            return false;
        }
        return false;
    }

    private void MoveUp() {
        Log.d(TAG, "MoveUp()");

        TextView subItemTXV;
        int itemLength = -1;

        switch (pageNum) {
            case 1:
                itemLength = itemINST.length;
                break;
            case 2:
                itemLength = itemCH.length;
                break;
            case 3:
                itemLength = itemSET.length;
                break;
            case 4:
                itemLength = itemSERV.length;
                break;
        }

        if (itemLength == -1) {
            Log.d(TAG, "MoveUp(): wrong itemNum!");
            return;
        }

        for (int i = 0; i < itemLength; i++) {
            subItemTXV = (TextView) submenuLinearLayout.getChildAt(i);
            if (subItemTXV.isSelected()) {
                // set current item to non-focused
                FixSubmenuTxv(subItemTXV, false);

                // set next item to focused
                if (i == 0)
                    subItemTXV = (TextView) submenuLinearLayout.getChildAt(itemLength - 1);
                else
                    subItemTXV = (TextView) submenuLinearLayout.getChildAt(--i);

                itemNum = submenuLinearLayout.indexOfChild(subItemTXV);
                FixSubmenuTxv(subItemTXV, true);
                return;
            }
        }
    }

    private void MoveDown() {
        Log.d(TAG, "MoveDown()");

        TextView subItemTXV;
        int itemLength = -1;

        switch (pageNum) {
            case 1:
                itemLength = itemINST.length;
                break;
            case 2:
                itemLength = itemCH.length;
                break;
            case 3:
                itemLength = itemSET.length;
                break;
            case 4:
                itemLength = itemSERV.length;
                break;
        }

        if (itemLength == -1) {
            Log.d(TAG, "MoveDown(): wrong itemNum!");
            return;
        }

        for (int i = 0; i < itemLength; i++) {
            subItemTXV = (TextView) submenuLinearLayout.getChildAt(i);
            if (subItemTXV.isSelected()) {
                // set current non-focused
                FixSubmenuTxv(subItemTXV, false);

                // set next focused
                if (i == (itemLength - 1))
                    subItemTXV = (TextView) submenuLinearLayout.getChildAt(0);
                else
                    subItemTXV = (TextView) submenuLinearLayout.getChildAt(++i);

                itemNum = submenuLinearLayout.indexOfChild(subItemTXV);
                FixSubmenuTxv(subItemTXV, true);
                return;
            }
        }
    }

    private void MoveLeft() {
        Log.d(TAG, "MoveLeft()");
        int i = pageNum - 1;
        int indexLimit = totalPageNum - 1;
        itemNum = 0;

        if ( --pageNum < 1 ) pageNum += totalPageNum; // update page number
        pageTXV.setText(
                String.valueOf(pageNum)
                        .concat("/")
                        .concat(String.valueOf(totalPageNum))
        );

        menu2IGV.setImageResource( mainImg[i] ); // move image & text to right
        menu2TXV.setText( getText(mainItemStr[i]) );

        if ( ++i > indexLimit ) i -= totalPageNum;
        menu3IGV.setImageResource( mainImg[i] ); // move image & text to right
        menu3TXV.setText( getText(mainItemStr[i]) );

        if ( ++i > indexLimit ) i -= totalPageNum;
        menu4IGV.setImageResource( mainImg[i] ); // move image & text to right
        menu4TXV.setText( getText(mainItemStr[i]) );

        if ( ++i > indexLimit ) i -= totalPageNum;
        menu1IGV.setImageResource( mainImg[i] ); // move image & text to right
        menu1TXV.setText( getText(mainItemStr[i]) );

        ShowSubmenu();
    }

    private void MoveRight() {
        Log.d(TAG, "MoveRight()");
        int i = pageNum - 1;
        int indexLimit = totalPageNum - 1;
        itemNum = 0;

        if( ++pageNum > totalPageNum ) pageNum -= totalPageNum; // update page number
        pageTXV.setText(
                String.valueOf(pageNum)
                        .concat("/")
                        .concat(String.valueOf(totalPageNum))
        );

        if ( ++i > indexLimit ) i -= totalPageNum;
        menu1IGV.setImageResource( mainImg[i] ); // move image & text to left
        menu1TXV.setText( getText(mainItemStr[i]) );

        if ( ++i > indexLimit ) i -= totalPageNum;
        menu2IGV.setImageResource( mainImg[i] ); // move image & text to left
        menu2TXV.setText( getText(mainItemStr[i]) );

        if ( ++i > indexLimit ) i -= totalPageNum;
        menu3IGV.setImageResource( mainImg[i] ); // move image & text to left
        menu3TXV.setText( getText(mainItemStr[i]) );

        if ( ++i > indexLimit ) i -= totalPageNum;
        menu4IGV.setImageResource( mainImg[i] ); // move image & text to left
        menu4TXV.setText( getText(mainItemStr[i]) );

        ShowSubmenu();
    }

    private void ActivateSelectedItem() {
        Log.d(TAG, "ActivateSelectedItem()");

        TextView itemTXV = (TextView) submenuLinearLayout.getChildAt(itemNum);
        if (itemTXV.isSelected()) {
            switch (pageNum) {
                case 1:
                    switch ( itemINST[itemNum] ) {
                        case R.string.STR_ANTENNA_SETUP:
                            subMenuFunction.AntennaSetup();
                            break;
                        case R.string.STR_CHANNEL_SEARCH:
                            subMenuFunction.ChannelSearch();
                            break;
                        case R.string.STR_RECEIVER_INFORMATION:
                            subMenuFunction.ReceiverInfo();
                            break;
                        case R.string.STR_SIGNAL_INFORMATION:
                            subMenuFunction.SignalInfo();
                            break;
                        case R.string.STR_USB_UPDATE:
                            subMenuFunction.USBUpdate();
                            break;
                        case R.string.STR_OTA_UPDATE:
                            subMenuFunction.OTAUpdate();
                            break;
                        case R.string.STR_CA_INFORMATION:// for VMX need open/close
                            subMenuFunction.CAInfo();
                            break;
                        case R.string.STR_MAIL: // for VMX need open/close
                            subMenuFunction.MailInfo();
                            break;

                        case R.string.STR_LOCATION_INFO:
                            subMenuFunction.LocationInfo();
                            break;
                        default:
                            Log.d(TAG, "ActivateSelectedItem(): no such sub item in page 1");
                            break;
                    }
                    break;
                case 2:
                    switch (itemCH[itemNum]) {
                        case R.string.STR_TV_CHANNEL:
                            if(checkProgramExist(1)==false) {//eric lin 20180802 check program exist
                                //Log.d(TAG, "onKeyDown: checkProgramExist(0)==false");
                                String str = getString(R.string.STR_NO_TV_CHANNEL);
                                new MessageDialogView(this, str, 3000) {
                                    public void dialogEnd() {
                                    }
                                }.show();
                            }else
                            subMenuFunction.TvChannel();
                            break;
                        case R.string.STR_RADIO_CHANNEL:
                            if(checkProgramExist(2)==false) {//eric lin 20180802 check program exist
                                //Log.d(TAG, "onKeyDown: checkProgramExist(0)==false");
                                String str = getString(R.string.STR_NO_RADIO_CHANNEL);
                                new MessageDialogView(this, str, 3000) {
                                    public void dialogEnd() {
                                    }
                                }.show();
                            }else
                            subMenuFunction.RadioChannel();
                            break;
                        case R.string.STR_TV_PROGRAMME_GUIDE:
                            if(checkProgramExist(0)==false) {//eric lin 20180802 check program exist
                                //Log.d(TAG, "onKeyDown: checkProgramExist(0)==false");
                                String str = getString(R.string.STR_NO_TVRADIO_CHANNEL);
                                new MessageDialogView(this, str, 3000) {
                                    public void dialogEnd() {
                                    }
                                }.show();
                            }else
                            subMenuFunction.Dimension_EPG();
                            break;
                        case R.string.STR_RECORDS_LIST:
                            //subMenuFunction.RecordGrid();
                            subMenuFunction.RecordList();
                            break;
                        case R.string.STR_TV_DIMENSION_EPG:
                            subMenuFunction.Dimension_EPG();
                            break;
                        default:
                            Log.d(TAG, "ActivateSelectedItem(): no such sub item in page 2");
                            break;
                    }
                    break;
                case 3:
                    switch (itemSET[itemNum]) {
                        case R.string.STR_LANGUAGE_SETTINGS:
                            subMenuFunction.LanguageSettings();
                            break;
                        case R.string.STR_SYSTEM_SETTINGS:
                            subMenuFunction.SystemSettings();
                            break;
                        case R.string.STR_OUTPUT_SETTINGS:
                            subMenuFunction.OutputSettings();
                            break;
                        case R.string.STR_PARENTAL_CONTROL:
                            subMenuFunction.ParentalControl();
                            break;
                        case R.string.STR_RESET_FACTORY_DEFAULT:
                            subMenuFunction.ResetFactoryDefault();
                            break;
                        default:
                            Log.d(TAG, "ActivateSelectedItem(): no such sub item in page 3");
                            break;
                    }
                    break;
                case 4:
                    switch (itemSERV[itemNum]) {
                        case R.string.STR_TIMER:
                            if(checkProgramExist(0)==false) {//eric lin 20180802 check program exist                                
                                String str = getString(R.string.STR_NO_TVRADIO_CHANNEL);
                                new MessageDialogView(this, str, 3000) {
                                    public void dialogEnd() {
                                    }
                                }.show();
                            }else
                            subMenuFunction.Timer();
                            break;
                        case R.string.STR_TIME_ADJUST:
                            subMenuFunction.TimeAdjust();
                            break;
                        case R.string.STR_STORAGE_SETTINGS:
                            subMenuFunction.StorageSettings();
                            break;
                        case R.string.STR_RECORD_SETTINGS:
                            subMenuFunction.RecordSettings();
                            break;

                        default:
                            Log.d(TAG, "ActivateSelectedItem(): no such sub item in page 4");
                            break;
                    }
                    break;
                default:
                    Log.d(TAG, "ActivateSelectedItem(): no such page");
                    break;
            }

            /*Toast.makeText(this,
                    "start "+itemTXV.getText(),
                    Toast.LENGTH_SHORT)
                    .show();*/
        } else {
            Log.d(TAG, "ActivateSubitem(): item is not select");
        }
    }

    private void ShowMainImage() {
        Log.d(TAG, "ShowMainImage: ");
        menu1IGV.setImageResource(R.drawable.installation);
        menu2IGV.setImageResource(R.drawable.channel);
        menu3IGV.setImageResource(R.drawable.setting);
        menu4IGV.setImageResource(R.drawable.mail_time);
        leftArrow.setImageResource(R.drawable.ic_left);
        rightArrow.setImageResource(R.drawable.ic_right);
    }

    private void ShowSubmenu () {
        Log.d(TAG, "ShowSubmenu()");
        submenuLinearLayout.removeAllViews();
        switch (pageNum) {
            case 1:
                ShowMenuINST();
                break;
            case 2:
                ShowMenuCH();
                break;
            case 3:
                ShowMenuSET();
                break;
            case 4:
                ShowMenuSERV();
                break;
        }
    }

    private void ShowMenuINST() {
        Log.d(TAG, "ShowMenuINST()");
        for (int i = 0; i < itemINST.length; i++) {
            if (i == 0)
                AddViewToSubmenu(getString(itemINST[i]), true);
            else
                AddViewToSubmenu(getString(itemINST[i]), false);
        }
    }

    private void ShowMenuCH() {
        Log.d(TAG, "ShowMenuCH()");
        for (int i = 0; i < itemCH.length; i++) {
            if (i == 0)
                AddViewToSubmenu(getString(itemCH[i]), true);
            else
                AddViewToSubmenu(getString(itemCH[i]), false);
        }
    }

    private void ShowMenuSET() {
        Log.d(TAG, "ShowMenuSET()");
        for (int i = 0; i < itemSET.length; i++) {
            if (i == 0)
                AddViewToSubmenu(getString(itemSET[i]), true);
            else
                AddViewToSubmenu(getString(itemSET[i]), false);
        }
    }

    private void ShowMenuSERV() {
        Log.d(TAG, "ShowMenuSERV()");
        for (int i = 0; i < itemSERV.length; i++) {
            if (i == 0)
                AddViewToSubmenu(getString(itemSERV[i]), true);
            else
                AddViewToSubmenu(getString(itemSERV[i]), false);
        }
    }

    private void AddViewToSubmenu(String str, boolean focus) {
        Log.d(TAG, "AddViewToSubmenu() str = "+ str);
        TextView functionTXV = new TextView(this);
        FixSubmenuTxv(functionTXV, focus);
        functionTXV.setText(str);

        submenuLinearLayout.addView(functionTXV);
    }

    private void FixSubmenuTxv(TextView txv, boolean focus) {
        int txvHeight = (int) getResources().getDimension(R.dimen.VIEW_HEIGHT);

        if (focus)
            txv.setBackground(getDrawable(R.drawable.focus));
        else
            txv.setBackground(getDrawable(R.drawable.n_focus));

        txv.setLayoutParams(new LinearLayout.LayoutParams(
                MATCH_PARENT, txvHeight));
        txv.setSelected(focus);
        txv.setGravity(Gravity.CENTER);
        txv.setTextColor(getResources().getColorStateList(R.color.text_color, getTheme()));
        txv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.TEXT_SIZE)
        );
    }

    public void onResume() {
        super.onResume();
        if(GetChannelExist() == 1) {
            ViewUiDisplay viewUiDisplay = GetViewUiDisplay();
            //ViewUiDisplayInit();
            int playId = ViewHistory.getPlayId();
            int playStatus = AvControlGetPlayStatus(playId);
            if(playStatus == HiDtvMediaPlayer.EnPlayStatus.IDLE.getValue()
                    || playStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue()
                    || playStatus == HiDtvMediaPlayer.EnPlayStatus.RELEASEPLAYRESOURCE.getValue()) {
                AvControlOpen(playId);

                // Johnny add 20180524 for setting ratio conversion, need to be called after AvControlOpen
                GposInfo gposInfo = GposInfoGet();
                AvControlChangeRatioConversion(playId, GetRatioByIndex(gposInfo.getScreen16x9()), gposInfo.getConversion());

                viewUiDisplay.ChangeProgram();
            }
        }
        if(Pvcfg.getCAType() == Pvcfg.CA_VMX && NeedLocationSetting()) { // for VMX need open/close
            //SetVMXLocationFlag(0);//Scoty 20181218 modify VMX location rule
            new VMXLocationDialog(this, this);
        }
    }

    private class SubMenuFunction {

        private void RecordList() {
            // edwin 20180802 block startActivity when there is no USB device -s
            if(Pvcfg.getPVR_PJ() == true ) //eric lin 20180703 add pvcfg
            {
                Log.d( TAG, "RecordList()" );

                if ( DevCount() == 0 )
                {
                    new MessageDialogView( MainMenuActivity.this
                            , getString( R.string.STR_STORAGE_DEVICE_IS_NOT_AVAILABLE )
                            , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY ) )
                    {
                        @Override
                        public void dialogEnd ()
                        {

                        }
                    }.show();
                    return;
                }
                pvrCheckHardDiskOpen(GetRecordPath()+ getString(R.string.STR_RECORD_DIR));//Scoty 20180827 add HDD Ready command and callback
            }
            // edwin 20180802 block startActivity when there is no USB device -e
        }

        private void RecordGrid() {
            // edwin 20180802 block startActivity when there is no USB device -s
            if(Pvcfg.getPVR_PJ() == true ) //eric lin 20180703 add pvcfg
            {
                Log.d( TAG, "RecordList()" );

                if ( DevCount() == 0 )
                {
                    new MessageDialogView( MainMenuActivity.this
                            , getString( R.string.STR_STORAGE_DEVICE_IS_NOT_AVAILABLE )
                            , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY ) )
                    {
                        @Override
                        public void dialogEnd ()
                        {

                        }
                    }.show();
                    return;
                }
                pvrCheckHardDiskOpen(GetRecordPath()+ getString(R.string.STR_RECORD_DIR));//Scoty 20180827 add HDD Ready command and callback
            }
            // edwin 20180802 block startActivity when there is no USB device -e
        }

        private void AntennaSetup() {
            Log.d(TAG, "AntennaSetup()");
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, AntennaSetupActivity.class);
            startActivity(it);
        }

        private void ChannelSearch() {
            Log.d(TAG, "ChannelSearch() CurTunerType = " + GetCurTunerType());
            Intent it = new Intent();
            if(GetCurTunerType() == TpInfo.DVBC) {
                it.setClass(MainMenuActivity.this, ChannelSearchActivity.class);
            }
            else if(GetCurTunerType() == TpInfo.ISDBT) {
                it.setClass(MainMenuActivity.this, ChannelSearchISDBTActivity.class);
            }
            else if(GetCurTunerType() == TpInfo.DVBT) {
                it.setClass(MainMenuActivity.this, ChannelSearchDVBTActivity.class);
            }
            else if(GetCurTunerType() == TpInfo.DVBS) {
                it.setClass(MainMenuActivity.this, ChannelSearchDVBSActivity.class);
            }
            Bundle bundle = new Bundle();
            bundle.putString("parent", "MainMenu");
            it.putExtras(bundle);
            CheckMenuLockAndStartActivity(MainMenuActivity.this, it); // Edwin 20181214 live tv cannot scan channel
        }

        private void ReceiverInfo() {
            Log.d(TAG, "ReceiverInfo()");
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, ReceiverInformationActivity.class);
            startActivity(it);
        }

        private void SignalInfo() {
            Log.d(TAG, "SignalInfo()");
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, SignalInformationActivity.class);
            startActivity(it);
        }


        private void LocationInfo() { // for VMX need open/close
            Log.d(TAG, "LocationInfo()");
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, LocationActivity.class);
            startActivity(it);
        }

        private void USBUpdate(){
            Log.d(TAG, "USBUpdate()");
            //int deviceCount = 0;
            //StorageManager mStorageManager ;
            //mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            List<Object> storageVolumes = getVolumes(); // Edwin 20181214 simplify get volumes
            StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
            final List<String> USBMountPath = new ArrayList<String>();
            for (Object loopVolume : storageVolumes) {
                if ( pesiStorageHelper.getDevType(loopVolume) != null ) {
                    System.out.println("jim test " + loopVolume);
                    USBMountPath.add(pesiStorageHelper.getPath(loopVolume));
                }
            }

            if(USBMountPath != null && USBMountPath.size() > 0 /*deviceCount > 0*/)
            {
                new SureDialog(MainMenuActivity.this) {
                    public void onSetMessage(View v) {
                        ((TextView) v).setText(getString(R.string.STR_USB_UPDATE));
                    }

                    public void onSetNegativeButton() {
                    }

                    public void onSetPositiveButton() {
                        int hasUpdateFile = 0;
                        int success = 0;
                        for(int i = 0 ; i < USBMountPath.size() ; i++)
                        {
                            File[] fileList = CheckFilePath(USBMountPath.get(i));
                            if(fileList != null && fileList.length > 0) {
                                hasUpdateFile = CheckUsbUpdateContent(fileList);
                                Log.d(TAG, "onSetPositiveButton: hasUpdateFile = " + hasUpdateFile);
                                if (hasUpdateFile == 1) {//has UpdateFile
                                    success = UpdateUsbSoftWare(USBUpdateName);
                                    if (success != 0) {
                                        String str = getString(R.string.STR_PLEASE_CHECK_USB_UPDATE_FILE);
                                        new MessageDialogView(MainMenuActivity.this, str
                                                , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                                            public void dialogEnd() {
                                            }
                                        }.show();
                                    } else
                                        break;
                                }
                            }
                        }

                        if(hasUpdateFile == 0)
                        {
                            String str = getString(R.string.STR_NO_UPDATE_FILES);
                            new MessageDialogView(MainMenuActivity.this, str
                                    , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                                public void dialogEnd() {
                                }
                            }.show();
                        }
                    }
                };
            }
            else
            {
                String str = getString(R.string.STR_USB_DISK_NOT_READY);
                new MessageDialogView(MainMenuActivity.this, str
                        , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                    public void dialogEnd() {
                    }
                }.show();
            }

        }

        private void OTAUpdate() {
            Log.d(TAG, "OTAUpdate() CurTunerType = " + GetCurTunerType());
            final OtaUpdateDialogView dialog = new OtaUpdateDialogView(MainMenuActivity.this, GetCurTunerType()) {
                public void onSetPositiveButton(int tpid, int freq, int symbol, int qam, int bandwidth) {
                    if (GetCurTunerType() == TpInfo.DVBC) {
                        Log.d(TAG, "OTAUpdate: DVBC = " + GetCurTunerType());
                        int ret = UpdateOTADVBCSoftWare(tpid, freq, symbol, qam);
                        if (ret != 0) {
                            String str = getString(R.string.STR_OTA_UPDATE_FAIL);
                            new MessageDialogView(MainMenuActivity.this, str, getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                                public void dialogEnd() {
                                }
                            }.show();
                        }

                    } else if (GetCurTunerType() == TpInfo.ISDBT || GetCurTunerType() == TpInfo.DVBT) {
                        Log.d(TAG, "OTAUpdate: ISDBT/DVBT = " + GetCurTunerType());
                        int ret = UpdateOTADVBTSoftWare(tpid, freq, bandwidth, 0, 0);
                        if (ret != 0) {
                            String str = getString(R.string.STR_OTA_UPDATE_FAIL);
                            new MessageDialogView(MainMenuActivity.this, str, getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                                public void dialogEnd() {
                                }
                            }.show();
                        }
                    } else if (GetCurTunerType() == TpInfo.DVBS) {
                        Log.d(TAG, "OTAUpdate: DVBS = " + GetCurTunerType());
                    }

                }

            };
            new Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
                @Override
                public void run () {
                    dialog.show();
                }
            }, 150);
        }

        private void TvChannel() {
            Log.d(TAG, "TvChannel()");
            Intent intentTV = new Intent();
            intentTV.setClass(MainMenuActivity.this,TVManagerActivity.class);
            intentTV .putExtra("service",1);

            CheckMenuLockAndStartActivity(MainMenuActivity.this, intentTV); // Edwin 20181214 live tv cannot scan channel
        }

        private void RadioChannel() {
            Log.d(TAG, "RadioChannel()");
            Intent intentRADIO = new Intent();
            intentRADIO.setClass(MainMenuActivity.this,TVManagerActivity.class);
            intentRADIO .putExtra("service",0);
            CheckMenuLockAndStartActivity(MainMenuActivity.this, intentRADIO); // Edwin 20181214 live tv cannot scan channel
        }

        private void EPG() {
            Log.d(TAG, "EPG()");
            int curListPosition = 0;
            if(ViewHistory.getCurChannel()!=null)
                curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, EpgActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("type", ViewHistory.getCurGroupType() );
            bundle.putInt("cur_channel", curListPosition );
            it.putExtras(bundle);
            startActivity(it, bundle);
        }

        private void Dimension_EPG() {
            Log.d(TAG, "Dimension_EPG()");
            int curListPosition = 0;
            if(ViewHistory.getCurChannel()!=null)
                curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, DimensionEPG.class);
            Bundle bundle = new Bundle();
            if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) {
                if (Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1)//Scoty 20181129 modify VMX enter Epg rule
                {
                    new MessageDialogView(MainMenuActivity.this,getString(R.string.STR_VMX_CHBLOCK_MSG),3000)
                    {
                        public void dialogEnd() {
                        }
                    }.show();
                    return;
                }

                if(ViewHistory.getCurGroupType() >= ProgramInfo.ALL_TV_TYPE && ViewHistory.getCurGroupType() < ProgramInfo.ALL_RADIO_TYPE)
                    bundle.putInt("type", ProgramInfo.ALL_TV_TYPE);
                else
                    bundle.putInt("type", ProgramInfo.ALL_RADIO_TYPE);
            }
            else
            {
                bundle.putInt("type", ViewHistory.getCurGroupType());
            }
            bundle.putInt("cur_channel", curListPosition );
            if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181129 modify VMX
                bundle.putBoolean("changeCH",false);
            it.putExtras(bundle);
            startActivity(it, bundle);
        }

        private void LanguageSettings() {
            Log.d(TAG, "LanguageSettings()");
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, LanguageSettingsActivity.class);
            startActivity(it);
        }

        private void SystemSettings() {
            Log.d(TAG, "SystemSettings()");
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, SystemSettingsActivity.class);
            startActivity(it);
        }

        private void OutputSettings() {
            Log.d(TAG, "OutputSettings()");
            Intent it = new Intent();
            it.setClass(MainMenuActivity.this, OutputSettingsActivity.class);
            startActivity(it);
        }

        private void ParentalControl() {
            Log.d(TAG, "ParentalControl()");
            Intent intentParent = new Intent();
            intentParent.setClass(MainMenuActivity.this,ParentalActivity.class);
            startActivity(intentParent);
        }

        private void ResetFactoryDefault() {
            Log.d(TAG, "ResetFactoryDefault()");
            Intent intentReset = new Intent();
            intentReset.setClass(MainMenuActivity.this,ResetDefaultActivity.class);
            CheckMenuLockAndStartActivity(MainMenuActivity.this, intentReset); // Edwin 20181214 live tv cannot scan channel
        }

        private void CAInfo() { // for VMX need open/close
            Log.d(TAG, "CAInfo()");
            Intent intentReset = new Intent();
            intentReset.setClass(MainMenuActivity.this,CAMenuActivity.class);
            CheckMenuLockAndStartActivity(MainMenuActivity.this, intentReset); // Edwin 20181214 live tv cannot scan channel
        }


        private void MailInfo() { // connie 20181116 for vmx mail
            Log.d(TAG, "MailInfo()");
            Intent intentReset = new Intent();
            intentReset.setClass(MainMenuActivity.this,MailActivity.class);
            CheckMenuLockAndStartActivity(MainMenuActivity.this, intentReset); // Edwin 20181214 live tv cannot scan channel
        }

        private void Timer() {
            Log.d(TAG, "Timer()");
            Intent intentTimer = new Intent();
            intentTimer.setClass(MainMenuActivity.this,TimerListActivity.class);
            startActivity(intentTimer);
        }

        private void TimeAdjust() {
            Log.d(TAG, "TimeAdjust()");
            Intent intent = new Intent();
            intent.setClass(MainMenuActivity.this,TimeAdjustActivity.class);
            startActivity(intent);
        }

        private void StorageSettings(){
            Log.d(TAG, "StorageSettings()");

            // edwin 20180802 block startActivity when there is no USB device -s
            if ( DevCount() == 0 )
            {
                new MessageDialogView( MainMenuActivity.this
                        , getString( R.string.STR_STORAGE_DEVICE_IS_NOT_AVAILABLE )
                        , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY ) )
                {
                    @Override
                    public void dialogEnd ()
                    {

                    }
                }.show();
                return;
            }
            // edwin 20180802 block startActivity when there is no USB device -e

            Intent intent = new Intent();
            intent.setClass(MainMenuActivity.this,Storage_device_setup.class);
            startActivity(intent);
        }

        private void RecordSettings() {
            Log.d(TAG, "RecordSettings()");
            Intent intent = new Intent();
            intent.setClass(MainMenuActivity.this, RecordSettingsActivity.class);
            startActivity(intent);
        }
    }

    private File[] CheckFilePath(String filepath)
    {
        try {
            File filePath = new File(filepath);
            File[] fileList = filePath.listFiles();
            return fileList;
        }
        catch(Exception e){
            return null;
        }
    }

    private int CheckUsbUpdateContent(File[] fileList)
    {
        int hasUpdateBinFile = 0;
        FilenameFilter namefilter =new FilenameFilter(){
            private String[] filter={
                    "bin"
            };
            @Override
            public boolean accept(File dir, String filename){
                for(int i=0;i<filter.length;i++){
                    if(filename.indexOf(filter[i])!=-1)
                        return true;
                }
                return false;
            }
        };
        try{
            //File filePath=new File("/storage/sda1/");
            //File[] fileList=filePath.listFiles();
            CharSequence[] list =new CharSequence[fileList.length];
            for(int i=0;i<list.length;i++){
                if(fileList[i].getName().equals(USBUpdateName)) {
                    hasUpdateBinFile = 1;
                    break;
                }
            }
        }catch(Exception e){

        }

        return hasUpdateBinFile;
    }

    // Johnny add 20180125
    private void CheckMenuLockAndStartActivity(final Intent it)
    {
        if (GposInfoGet().getInstallLockOnOff() == 0)
        {
            startActivity(it);
        }
        else
        {
            new PasswordDialogView(MainMenuActivity.this, GposInfoGet().getPasswordValue(), PasswordDialogView.TYPE_PINCODE,0) {
                public void onCheckPasswordIsRight() {
                    Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                    startActivity(it);
                }

                public void onCheckPasswordIsFalse() {
                    Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");

                    new MessageDialogView(MainMenuActivity.this
                            , getString(R.string.STR_INVALID_PASSWORD)
                            , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                        public void dialogEnd() {
                        }
                    }.show();
                }
                public boolean onDealUpDownKey() {
                    return false;
                }
            };
        }
    }

    // edwin 20180802 add USB device count -s
    private int DevCount()
    {
        Log.d( TAG, "DevCount: " );

        //StorageManager mStorageManager ;
        //mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        List<Object> storageVolumes = getVolumes(); // Edwin 20181214 simplify simplify get volumes
        int devCount = 0;

        for ( Object vol : storageVolumes )
        {
            String devType = pesiStorageHelper.getDevType(vol);
            String internalPath = pesiStorageHelper.getInternalPath(vol);
            if ( (devType != null) && (!devType.equals("")) && (internalPath != null))
            {
                devCount++;
            }
        }
        return devCount;
    }
    // edwin 20180802 add USB device count -e
//Scoty 20180827 add HDD Ready command and callback -s
    @Override
    public void onMessage(TVMessage tvMessage) {
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_PVR_HDD_READY: {
                Intent it = new Intent();
                it.setClass(MainMenuActivity.this, RecordListActivity.class);
                startActivity(it);
            }break;
        }
    }
//Scoty 20180827 add HDD Ready command and callback -e
}
