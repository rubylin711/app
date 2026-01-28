package com.prime.dtvplayer.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.core.content.res.ResourcesCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.OtaUpdateDialogView;
import com.prime.dtvplayer.View.VMXLocationDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainMenu2Activity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private static int pageNum;
    private static int itemNum;
    private static int totalPageNum;
    private static boolean menuIsAvailable;
    private static MainMenu2Activity.SubMenuFunction subMenuFunction ;
    private final int FINISH_APK = 0xffff;
    
    private ImageView menu1IGV;
    private ImageView menu2IGV;
    private ImageView menu3IGV;
    private ImageView menu4IGV;
    private ImageView menu5IGV;
    private ImageView leftArrow;
    private ImageView rightArrow;
    private TextView menu1TXV;
    private TextView menu2TXV;
    private TextView menu3TXV;
    private TextView menu4TXV;
    private TextView menu5TXV;
    private TextView pageTXV;
    private TextView subMenuTitleTXV;
    private LinearLayout submenuLinearLayout;
    private ConstraintLayout selectedMainItemLinearLayout;
    private LinearLayout mainMenuLinearLayout;
    private String USBUpdateName = "DDN82_usb.bin";
    private String usbDevicePath1="",usbDevicePath2="";

    private int[] mainImg;
    private int[] focusmainImg;
    private int[] mainItemStr;
    private Integer[] itemINST;
    private Integer[] itemCH;
    private Integer[] itemSET;
    private Integer[] itemSERV;
    private Integer[] itemMEDIA;

    private static int test = 1;

    private static final boolean mIsFillet = true;//eric lin 20180912 mainmenu2 fillet

    // Johnny 20181219 for mouse control -s
    private View.OnClickListener mMenuIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            itemNum = 0;
            ImageView preSelectIcon = getMenuIconImage(pageNum);    // selected icon before click
            setViewZoomOut(preSelectIcon);   // zoom out pre icon

            pageNum = getMenuIconPageNum(view);  // set pageNum by clicked icon
            setViewZoomIn(view);       // zoom in clicked icon
            ShowSubmenu();
        }
    };

    // first click = select
    // click selected = intent to other activity
    private View.OnClickListener mSubMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.isSelected() && menuIsAvailable) {
                ActivateSelectedItem();
            }
            else {
                FixSubmenuTxv((TextView) submenuLinearLayout.getChildAt(itemNum), false);   // deselect pre submenu item

                itemNum = submenuLinearLayout.indexOfChild(view);   // get itemNum by clicked submenu item
                FixSubmenuTxv((TextView) view, true);   // select clicked item
            }
        }
    };
    // Johnny 20181219 for mouse control -e

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mIsFillet == true)//eric lin 20180912 mainmenu2 fillet
            setContentView(R.layout.main_menu2_fillet);
        else
            setContentView(R.layout.main_menu2);

        menu1IGV   = (ImageView)findViewById(R.id.menu1Img);
        menu2IGV   = (ImageView)findViewById(R.id.menu2Img);
        menu3IGV   = (ImageView)findViewById(R.id.menu3Img);
        menu4IGV   = (ImageView)findViewById(R.id.menu4Img);
        menu5IGV   = (ImageView)findViewById(R.id.menu5Img);
        //leftArrow  = (ImageView)findViewById(R.id.left);
        //rightArrow = (ImageView)findViewById(R.id.right);
        subMenuTitleTXV = (TextView)findViewById(R.id.subMenuTitle);
        menu1TXV   = (TextView)findViewById(R.id.menu1Txv);
        menu2TXV   = (TextView)findViewById(R.id.menu2Txv);
        menu3TXV   = (TextView)findViewById(R.id.menu3Txv);
        menu4TXV   = (TextView)findViewById(R.id.menu4Txv);
        //menu5TXV   = (TextView)findViewById(R.id.menu5Txv);
        //pageTXV    = (TextView)findViewById(R.id.page);
        mainMenuLinearLayout         = (LinearLayout) findViewById(R.id.main_menu_layout);
        submenuLinearLayout          = (LinearLayout) findViewById(R.id.submenuLayout);
        selectedMainItemLinearLayout = (ConstraintLayout) findViewById(R.id.selectedLayout);

        // Johnny 20181219 for mouse control
        menu1IGV.setOnClickListener(mMenuIconClickListener);
        menu2IGV.setOnClickListener(mMenuIconClickListener);
        menu3IGV.setOnClickListener(mMenuIconClickListener);
        menu4IGV.setOnClickListener(mMenuIconClickListener);
        menu5IGV.setOnClickListener(mMenuIconClickListener);

        Init();
    }

    private void Init () {
        Log.d(TAG, "Init()");
        //        Toast.makeText(this, TAG + " started", Toast.LENGTH_SHORT).show();

        InitMenu();

        subMenuFunction = new MainMenu2Activity.SubMenuFunction();
        pageNum = 1;
        itemNum = 0;//fix crash
        totalPageNum = mainItemStr.length;
        //pageTXV.setText(
        //         String.valueOf(pageNum)
        //                 .concat("/")
        //                 .concat(String.valueOf(totalPageNum))
        // ); // init Page Number

        //GradientDrawable border = new GradientDrawable(); // show yellow frame
        //border.setShape(GradientDrawable.RECTANGLE);
        //border.setStroke(3, Color.YELLOW);
        //border.setCornerRadius(3);
        //selectedMainItemLinearLayout.setBackground(border);

        ShowMainImage();
        ShowSubmenu();
        ImageView focusIcon = getMenuIconImage(pageNum);
        //focusIcon.setImageResource( focusmainImg[pageNum-1] );
        setViewZoomIn(focusIcon);

        //mainMenuLinearLayout.setVisibility(LinearLayout.GONE); // hide main  menu //eric lin 20180116 show first main menu, mark
        //submenuLinearLayout.setVisibility(LinearLayout.GONE); //eric lin 20180116 show first main menu, mark
        menuIsAvailable = true;//eric lin 20180116 show first main menu, modify false to true
    }

    private void InitMenu()
    {
        mainImg = new int[] {
                R.drawable.installation_menu2_nfocus,
                R.drawable.channel_menu2_nfocus,
                R.drawable.settings_menu2_nfocus,
                R.drawable.tools_menu2_nfocus,
                R.drawable.hdd_menu2_nfocus
        };
        focusmainImg = new int[] {
                R.drawable.installation_menu2_focus,
                R.drawable.channel_menu2_focus,
                R.drawable.settings_menu2_focus,
                R.drawable.tools_menu2_focus,
                R.drawable.hdd_menu2_focus
        };
        mainItemStr = new int[] {
                R.string.STR_INSTALLATION,
                R.string.STR_CHANNEL,
                R.string.STR_SETTING,
                R.string.STR_SERVICE,
                R.string.STR_MEDIA_CENTER
        };

        // Edwin 20181214 add item of CA_VMX -s
        if (GetCurTunerType() == TpInfo.DVBS)
        {
            List<Integer> instList = new ArrayList<>();
            instList.add(R.string.STR_CHANNEL_SEARCH);
            instList.add(R.string.STR_ANTENNA_SETUP);
            instList.add(R.string.STR_RECEIVER_INFORMATION);
            instList.add(R.string.STR_SIGNAL_INFORMATION);
            if ( Pvcfg.getCAType() == Pvcfg.CA_VMX )
            {
                instList.add(R.string.STR_CA_INFORMATION);
                instList.add(R.string.STR_MAIL);
            }
            itemINST = instList.toArray(new Integer[0]);
            //itemINST = new int[] {
            //        R.string.STR_CHANNEL_SEARCH,
            //        R.string.STR_ANTENNA_SETUP,
            //        R.string.STR_RECEIVER_INFORMATION,
            //        R.string.STR_SIGNAL_INFORMATION,
            //        //R.string.STR_CA_INFORMATION, // for VMX need open/close
            //        //R.string.STR_MAIL // for VMX need open/close
            //};
        }
        else    // dvbc, isdbt, dvbt
        {
            List<Integer> instList = new ArrayList<>();
            instList.add(R.string.STR_CHANNEL_SEARCH);
            instList.add(R.string.STR_RECEIVER_INFORMATION);
            instList.add(R.string.STR_SIGNAL_INFORMATION);
            if ( Pvcfg.getCAType() == Pvcfg.CA_VMX )
            {
                instList.add(R.string.STR_CA_INFORMATION);
                instList.add(R.string.STR_MAIL);
            }
            itemINST = instList.toArray(new Integer[0]);
            //itemINST = new int[] {
            //        R.string.STR_CHANNEL_SEARCH,
            //        R.string.STR_RECEIVER_INFORMATION,
            //        R.string.STR_SIGNAL_INFORMATION,
            //        //R.string.STR_CA_INFORMATION, // for VMX need open/close
            //        //R.string.STR_MAIL, // for VMX need open/close
            //};
        }
        // Edwin 20181214 add item of CA_VMX -e

        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            itemCH = new Integer[]{
                    R.string.STR_TV_CHANNEL,
                    R.string.STR_RADIO_CHANNEL,
                    R.string.STR_TV_PROGRAMME_GUIDE,
                    //R.string.STR_TV_DIMENSION_EPG
            };
        }else{
            itemCH = new Integer[]{
                    R.string.STR_TV_CHANNEL,
                    R.string.STR_RADIO_CHANNEL,
                    R.string.STR_TV_PROGRAMME_GUIDE,
                    //R.string.STR_TV_DIMENSION_EPG
            };
        }
        itemSET = new Integer[] {
                R.string.STR_LANGUAGE_SETTINGS,
//                R.string.STR_OUTPUT_SETTINGS,
                R.string.STR_SYSTEM_SETTINGS,
                R.string.STR_PARENTAL_CONTROL,
                R.string.STR_RESET_FACTORY_DEFAULT
        };

        // Edwin 20181214 add item of CA_VMX -s
        List<Integer> servList = new ArrayList<>();
        servList.add(R.string.STR_TIMER);
        servList.add(R.string.STR_TIME_ADJUST);
        servList.add(R.string.STR_USB_UPDATE);
        servList.add(R.string.STR_OTA_UPDATE);
        if ( Pvcfg.getCAType() == Pvcfg.CA_VMX )
        {
            servList.add(R.string.STR_LOCATION_INFO);
        }
        itemSERV = servList.toArray(new Integer[0]);

        //itemSERV = new Integer[] {
        //        R.string.STR_TIMER,
        //        R.string.STR_TIME_ADJUST,
        //        R.string.STR_USB_UPDATE,
        //        R.string.STR_OTA_UPDATE,
        //        //R.string.STR_LOCATION_INFO, // for VMX need open/close
        //};
        // Edwin 20181214 add item of CA_VMX -e

        if(Pvcfg.getPVR_PJ()==true) {
            itemMEDIA = new Integer[]{
                    R.string.STR_RECORDS_LIST,
                    R.string.STR_STORAGE_SETTINGS,
                    R.string.STR_RECORD_SETTINGS
            };
        }
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
            case KeyEvent.KEYCODE_PROG_RED: // connie 20190805 for add recommand channel test
            {
                Log.d(TAG, "onKeyDown: KEYCODE_PROG_RED...");
                String mSelectionClause = TvContractCompat.Channels.COLUMN_PACKAGE_NAME + " LIKE ?";
                String[] mSelectionArgs = {"com.prime.dtvplayer"};
                int dechannelUri = getContentResolver().delete(TvContractCompat.Channels.CONTENT_URI,
                        mSelectionClause,mSelectionArgs);
                for(int i=0;i<6;i++) {
                    Channel channel = new Channel.Builder()
                            .setDisplayName("推薦影片")
                            .setDescription("DTV "+i)
                            .setDisplayNumber(""+i)
                            .setType(TvContract.Channels.TYPE_PREVIEW)
                            .setPackageName("com.prime.dtvplayer")
                            .setOriginalNetworkId(1)
                            // Set more attributes...
                            .build();
                    Log.d(TAG, "onKeyDown:  channel.ID =" + channel.getId());
                    Uri channelUri = getContentResolver().insert(TvContractCompat.Channels.CONTENT_URI, channel.toContentValues());
                    String str_CH = channel.toString();
                    Log.d(TAG, "onKeyDown:  str_CH =" + str_CH);
//                    PreviewProgram previewProgram = new PreviewProgram.Builder()
//                            .setChannelId(channel.getId())
//                            .setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
//                            .setTitle("DTV "+i+" Program Title")
//                            .setDescription("Program Description")
//                            .setStartTimeUtcMillis(System.currentTimeMillis())
//                            .setEndTimeUtcMillis(System.currentTimeMillis()+60*60*1000)
//                            // Set more attributes...
//                            .build();
//                    Uri previewProgramUri = getContentResolver().insert(TvContractCompat.PreviewPrograms.CONTENT_URI,
//                            previewProgram.toContentValues());
                }
                Log.d(TAG, "onKeyDown: KEYCODE_PROG_RED...done");
//                Cursor channelCursor = getChannelCursor() ;
//                if (channelCursor != null) {
//                    while (channelCursor.moveToNext()) {
//                        long channelId = channelCursor.getLong(0);
//                        String channelDisplayName = channelCursor.getString(1);
//                        String packageName = channelCursor.getString(2);
//                        String channelIntent = channelCursor.getString(3);
//                        Log.d(TAG, "onKeyDown: =======================================================================");
//                        Log.d(TAG, "onKeyDown: channdlID = " + channelId
//                                + " channelDisplayName = " + channelDisplayName
//                                + " packageName = " + packageName
//                                + " channelIntent = " + channelIntent);
//
//                        if (!(packageName.equals("com.prime.dtvplayer") && channelDisplayName.equals("推薦影片")))
//                            //if ( ! (packageName.equals("com.google.android.youtube.tv") && channelDisplayName.equals("Recommended")) )
//                            //if ( ! (packageName.equals( "com.qiyi.tv.tw") && channelDisplayName.equals("愛奇藝專屬推薦")) )
//                            continue;
//
//                        Cursor programCursor = getProgramCursor(channelId);
//                        if (programCursor != null) {
//                            Intent intent = null;
//
//                            while (programCursor.moveToNext()) {
//                                long programID = programCursor.getLong(0);
//                                String programTitle = programCursor.getString(1);
//                                String programDescription = programCursor.getString(2);
//                                String programIntent = programCursor.getString(3);
//                                String posterArtUri = programCursor.getString(4);
//                                Log.d(TAG, "onKeyDown: programID = " + programID
//                                        + " programTitle = " + programTitle
//                                        + " programDescription = " + programDescription
//                                        + " programIntent = " + programIntent
//                                        + " posterArtUri = " + posterArtUri);
//
//                            }
//                        }
//                    }
//                }
            }break;

            case KeyEvent.KEYCODE_PROG_GREEN: { // connie 20190805 for add recommand channel test
                Cursor channelCursor = getChannelCursor();
                Channel channel;

                if (channelCursor != null) {
                    while (channelCursor.moveToNext()) {
                        long channelId = channelCursor.getLong(0);
                        String channelDisplayName = channelCursor.getString(1);
                        String packageName = channelCursor.getString(2);
                        String channelIntent = channelCursor.getString(3);
                        Log.d(TAG, "onKeyDown: =======================================================================");
                        Log.d(TAG, "onKeyDown: channdlID = " + channelId
                                + " channelDisplayName = " + channelDisplayName
                                + " packageName = " + packageName
                                + " channelIntent = " + channelIntent);
                        //=========================================================

                       // channel = Channel.fromCursor(channelCursor);
                        Log.d(TAG, "onKeyDown:  channel => " + channelId);
                        if (packageName.equals("com.prime.dtvplayer") && channelDisplayName.equals("推薦影片"))
                        {
                            String title = "Test Program " + test;
                            Log.d(TAG, "onKeyDown:  title =" + title);
                            PreviewProgram previewProgram = new PreviewProgram.Builder()
                                    .setChannelId(channelId)
                                    .setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                                    .setTitle(title)
                                    .setDescription(title + " Description")
                                    .setStartTimeUtcMillis(System.currentTimeMillis())
                                    .setEndTimeUtcMillis(System.currentTimeMillis()+60*60*1000)
                                    // Set more attributes...
                                    .build();
                                    Uri previewProgramUri = getContentResolver().insert(TvContractCompat.PreviewPrograms.CONTENT_URI, previewProgram.toContentValues());

                            String strPR = previewProgram.toString();
                            Log.d(TAG, "onKeyDown: strPR =" + strPR);
                            test++;
                            Cursor programCursor = getProgramCursor(channelId);
                            if (programCursor != null) {
                                Intent intent = null;

                                while (programCursor.moveToNext()) {
                                    long programID = programCursor.getLong(0);
                                    String programTitle = programCursor.getString(1);
                                    String programDescription = programCursor.getString(2);
                                    String programIntent = programCursor.getString(3);
                                    String posterArtUri = programCursor.getString(4);
                                    Log.d(TAG, "onKeyDown: programID = " + programID
                                            + " programTitle = " + programTitle
                                            + " programDescription = " + programDescription
                                            + " programIntent = " + programIntent
                                            + " posterArtUri = " + posterArtUri);

                                }
                            }
                        }
                        else
                            continue;
                    }
                }
            }break;

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
            MainMenu2Activity.this.finish();
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
            case 5:
                itemLength = itemMEDIA.length;
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
            case 5:
                itemLength = itemMEDIA.length;
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
        ImageView focusIcon = null;
        focusIcon = getMenuIconImage(pageNum);
        //focusIcon.setImageResource( mainImg[pageNum-1] );
        setViewZoomOut(focusIcon);
        if ( --pageNum < 1 ) 
          pageNum += totalPageNum; // update page number
        focusIcon = getMenuIconImage(pageNum);
        //focusIcon.setImageResource( focusmainImg[pageNum-1] );
        setViewZoomIn(focusIcon);
        //pageTXV.setText(
        //        String.valueOf(pageNum)
        //                .concat("/")
        //                .concat(String.valueOf(totalPageNum))
        //);

/*
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
*/
        ShowSubmenu();
    }

    private void MoveRight() {
        Log.d(TAG, "MoveRight()");
        int i = pageNum - 1;
        int indexLimit = totalPageNum - 1;
        itemNum = 0;
        ImageView focusIcon = null; // connie test-s
        Log.d(TAG, "MoveRight: 111 pageNum = " + pageNum);
        focusIcon = getMenuIconImage(pageNum);
        //focusIcon.setImageResource( mainImg[pageNum-1] );
        setViewZoomOut(focusIcon);
        if( ++pageNum > totalPageNum )
          pageNum -= totalPageNum; // update page number

        focusIcon = getMenuIconImage(pageNum);
        //focusIcon.setImageResource( focusmainImg[pageNum-1] );
        setViewZoomIn(focusIcon);
        Log.d(TAG, "MoveRight: 222 pageNum = " + pageNum);
        //pageTXV.setText(
        //        String.valueOf(pageNum)
        //                .concat("/")
        //                .concat(String.valueOf(totalPageNum))
        //);

        /*// connie test-e
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
*/
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
                        case R.string.STR_CA_INFORMATION: // for VMX need open/close
                            subMenuFunction.CAInfo();
                            break;
                        case R.string.STR_MAIL: // for VMX need open/close
                            subMenuFunction.MailInfo();
                            break;
                        default:
                            Log.d(TAG, "ActivateSelectedItem(): no such sub item in page 1");
                            break;
                    }
                    finish();
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
                        case R.string.STR_TV_DIMENSION_EPG:
                            subMenuFunction.Dimension_EPG();
                            break;
                        default:
                            Log.d(TAG, "ActivateSelectedItem(): no such sub item in page 2");
                            break;
                    }
                    finish();
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
                        case R.string.STR_USB_UPDATE:
                            subMenuFunction.USBUpdate();
                            break;
                        case R.string.STR_OTA_UPDATE:
                            subMenuFunction.OTAUpdate();
                            break;
                        case R.string.STR_LOCATION_INFO:
                            subMenuFunction.LocationInfo();
                            break;
                        default:
                            Log.d(TAG, "ActivateSelectedItem(): no such sub item in page 4");
                            break;
                    }
                    break;
                case 5:
                    switch (itemMEDIA[itemNum]) {
                        case R.string.STR_RECORDS_LIST:
                            subMenuFunction.RecordList();
                            break;
                        case R.string.STR_STORAGE_SETTINGS:
                            subMenuFunction.StorageSettings();
                            break;
                        case R.string.STR_RECORD_SETTINGS:
                            subMenuFunction.RecordSettings();
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
        menu1IGV.setImageResource(mainImg[0]);
        menu2IGV.setImageResource(mainImg[1]);
        menu3IGV.setImageResource(mainImg[2]);
        menu4IGV.setImageResource(mainImg[3]);
        menu5IGV.setImageResource(mainImg[4]);
        //leftArrow.setImageResource(R.drawable.ic_left);
        //rightArrow.setImageResource(R.drawable.ic_right);
    }

    private void ShowSubmenu () {
        Log.d(TAG, "ShowSubmenu()");
        submenuLinearLayout.removeAllViews();
        subMenuTitleTXV.setText(getText(mainItemStr[pageNum-1]));//eric lin test, add
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
            case 5:
                ShowMenuMEDIA();
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

    private void ShowMenuMEDIA() {
        Log.d(TAG, "ShowMenuMEDIA()");
        for (int i = 0; i < itemMEDIA.length; i++) {
            if (i == 0)
                AddViewToSubmenu(getString(itemMEDIA[i]), true);
            else
                AddViewToSubmenu(getString(itemMEDIA[i]), false);
        }
    }

    private void AddViewToSubmenu(String str, boolean focus) {
        Log.d(TAG, "AddViewToSubmenu() str = "+ str);
        TextView functionTXV = new TextView(this);

        functionTXV.setOnClickListener(mSubMenuClickListener);  // Johnny 20181219 for mouse control
        functionTXV.setFocusable(false);    // Johnny 20181219 for mouse control

        FixSubmenuTxv(functionTXV, focus);
        functionTXV.setText(str);

        submenuLinearLayout.addView(functionTXV);//eric lin test
    }
    
    private void FixSubmenuTxv ( TextView txv, boolean focus ) {
        if ( mIsFillet == true ) //eric lin 20180912 mainmenu2 fillet
        {
            Log.d(TAG, "FixSubmenuTxv: focus=" + focus);//eric lin test
            int txvHeight = (int) getResources().getDimension(R.dimen.VIEW_SUBMENUITEM_FILLET_HEIGHT);
            int txvWidth = (int) getResources().getDimension(R.dimen.SUBMENUITEM_WIDTH);
            //int margin = (int) getResources().getDimension(R.dimen.MARGIN_10DP_SIZE);

            if ( focus )
            {
                //txv.setBackground(getDrawable(R.drawable.focus));//txv.setBackgroundColor(getResources().getColor(R.color.LightBlue));
                //txv.setBackgroundColor(getResources().getColor(R.color.LightBlue));
                txv.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_rectangle_frameline_focusitem, null));
                txv.animate().scaleX(1.3f).scaleY(1.26f).setDuration(300).start();
                //txv.setTextSize();
            }
            else
            {
                txv.setBackgroundColor(Color.TRANSPARENT);//txv.setBackground(getDrawable(R.drawable.n_focus));
                //txv.setTextScaleX(1.0f);
                txv.animate().scaleX(1).scaleY(1).setDuration(300).start();
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(txvWidth, txvHeight); //MATCH_PARENT, txvHeight); //eric lin test
            //layoutParams.setMargins(margin,0,margin, (int) getResources().getDimension(R.dimen.MARGIN_SIZE));
            txv.setLayoutParams(layoutParams);
        }
        else
        {
            int txvHeight = (int) getResources().getDimension(R.dimen.VIEW_SUBMENUITEM_HEIGHT);

            if ( focus )
            {
                //txv.setBackground(getDrawable(R.drawable.focus));//txv.setBackgroundColor(getResources().getColor(R.color.LightBlue));
                txv.setBackgroundColor(getResources().getColor(R.color.LightBlue));
                txv.animate().scaleX(1.3f).scaleY(1.3f).setDuration(300).start();
            }
            else
            {
                txv.setBackgroundColor(Color.TRANSPARENT);//txv.setBackground(getDrawable(R.drawable.n_focus));
                txv.animate().scaleX(1).scaleY(1).setDuration(300).start();
            }

            txv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, txvHeight));
            //        ViewGroup.LayoutParams layoutParams = txv.getLayoutParams();
            //        Log.d(TAG, "FixSubmenuTxv: TOST width="+layoutParams.width);
        }

        //        layoutParams.width = dpToPx(300);
        //        txv.setLayoutParams(layoutParams);

        txv.setSelected(focus);
        txv.setGravity(Gravity.CENTER);
        txv.setTextColor(getResources().getColorStateList(R.color.text_color, getTheme()));
        txv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.MAINMENU2_SUBMENU_TEXT));

    }

    public void onResume() {
        super.onResume();

        /*ConstraintLayout allLayout = findViewById(R.id.main_layout);
        Log.d(TAG, "onResume: allLayout = " + allLayout);
        if (allLayout != null)
            allLayout.animate().alpha(0.88f).setDuration(300).start();*/

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

                if ( UsbCount() == 0 )
                {
                    new MessageDialogView( MainMenu2Activity.this
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

                Intent it = new Intent();
                it.setClass( MainMenu2Activity.this, RecordListActivity.class );
                startActivity( it );
            }
            // edwin 20180802 block startActivity when there is no USB device -e
        }

        private void AntennaSetup() {
            Log.d(TAG, "AntennaSetup()");
            Intent it = new Intent();
            it.setClass(MainMenu2Activity.this, AntennaSetupActivity.class);
            startActivity(it);
        }

        private void ChannelSearch() {
            Log.d(TAG, "ChannelSearch() CurTunerType = " + GetCurTunerType());
            Intent it = new Intent();
            if(GetCurTunerType() == TpInfo.DVBC) {
                it.setClass(MainMenu2Activity.this, ChannelSearchActivity.class);
            }
            else if(GetCurTunerType() == TpInfo.ISDBT) {
                it.setClass(MainMenu2Activity.this, ChannelSearchISDBTActivity.class);
            }
            else if(GetCurTunerType() == TpInfo.DVBT) {
                it.setClass(MainMenu2Activity.this, ChannelSearchDVBTActivity.class);
            }
            else if(GetCurTunerType() == TpInfo.DVBS) {
                it.setClass(MainMenu2Activity.this, ChannelSearchDVBSActivity.class);
            }
            else
                return;
            Bundle bundle = new Bundle();
            bundle.putString("parent", "MainMenu");
            it.putExtras(bundle);
            CheckMenuLockAndStartActivity(MainMenu2Activity.this, it); // Edwin 20181214 live tv cannot scan channel
        }

        private void ReceiverInfo() {
            Log.d(TAG, "ReceiverInfo()");
            Intent it = new Intent();
            it.setClass(MainMenu2Activity.this, ReceiverInformationActivity.class);
            startActivity(it);
        }

        private void SignalInfo() {
            Log.d(TAG, "SignalInfo()");
            Intent it = new Intent();
            it.setClass(MainMenu2Activity.this, SignalInformationActivity.class);
            startActivity(it);
        }

        private void USBUpdate()
        {
            final List<String> USBMountPath = new ArrayList<>();
            StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            PesiStorageHelper storageHelper = new PesiStorageHelper(storageManager);

            for (Object vol : getVolumes())
            {
                if (storageHelper.isUsb(vol))
                {
                    String path = storageHelper.getPath(vol);
                    Log.d(TAG, "USBUpdate: path = "+path);
                    USBMountPath.add(path);
                }
            }

            if (USBMountPath.size() > 0 /*deviceCount > 0*/)
            {
                new SureDialog(MainMenu2Activity.this)
                {
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
                                        new MessageDialogView(MainMenu2Activity.this, str
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
                            new MessageDialogView(MainMenu2Activity.this, str
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
                new MessageDialogView(MainMenu2Activity.this, str
                        , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                    public void dialogEnd() {
                    }
                }.show();
            }

        }

        private void OTAUpdate() {
            Log.d(TAG, "OTAUpdate() CurTunerType = " + GetCurTunerType());
            //Scoty 20181207 modify VMX OTA rule -s
            if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)
            {
                Intent intent = new Intent();
                intent.setClass(MainMenu2Activity.this,VMXOTAActivity.class);
                startActivity(intent);
            }
            else {
                final OtaUpdateDialogView dialog = new OtaUpdateDialogView(MainMenu2Activity.this, GetCurTunerType()) {
                    public void onSetPositiveButton(int tpid, int freq, int symbol, int qam, int bandwidth) {
                        if (GetCurTunerType() == TpInfo.DVBC) {
                            Log.d(TAG, "OTAUpdate: DVBC = " + GetCurTunerType());
                            int ret = UpdateOTADVBCSoftWare(tpid, freq, symbol, qam);
                            if (ret != 0) {
                                String str = getString(R.string.STR_OTA_UPDATE_FAIL);
                                new MessageDialogView(MainMenu2Activity.this, str, getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
                                    public void dialogEnd() {
                                    }
                                }.show();
                            }

                        } else if (GetCurTunerType() == TpInfo.ISDBT || GetCurTunerType() == TpInfo.DVBT) {
                            Log.d(TAG, "OTAUpdate: ISDBT/DVBT = " + GetCurTunerType());
                            int ret = UpdateOTADVBTSoftWare(tpid, freq, bandwidth, 0, 0);
                            if (ret != 0) {
                                String str = getString(R.string.STR_OTA_UPDATE_FAIL);
                                new MessageDialogView(MainMenu2Activity.this, str, getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
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
            //Scoty 20181207 modify VMX OTA rule -e
        }

        private void LocationInfo() {
            Log.d(TAG, "LocationInfo()");
            Intent it = new Intent();
            it.setClass(MainMenu2Activity.this, LocationActivity.class);
            startActivity(it);
        }

        private void TvChannel() {
            Log.d(TAG, "TvChannel()");
            Intent intentTV = new Intent();
            intentTV.setClass(MainMenu2Activity.this,TVManagerActivity.class);
            intentTV .putExtra("service",1);
            CheckMenuLockAndStartActivity(MainMenu2Activity.this, intentTV); // Edwin 20181214 live tv cannot scan channel
        }

        private void RadioChannel() {
            Log.d(TAG, "RadioChannel()");
            Intent intentRADIO = new Intent();
            intentRADIO.setClass(MainMenu2Activity.this,TVManagerActivity.class);
            intentRADIO .putExtra("service",0);
            CheckMenuLockAndStartActivity(MainMenu2Activity.this, intentRADIO); // Edwin 20181214 live tv cannot scan channel
        }

        private void EPG() {
            Log.d(TAG, "EPG()");
            int curListPosition = 0;
            if(ViewHistory.getCurChannel()!=null)
                curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
            Intent it = new Intent();
            it.setClass(MainMenu2Activity.this, EpgActivity.class);
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
            it.setClass(MainMenu2Activity.this, DimensionEPG.class);
            Bundle bundle = new Bundle();
            if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) {
                if (Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1)//Scoty 20181129 modify VMX enter Epg rule
                {
                    new MessageDialogView(MainMenu2Activity.this,getString(R.string.STR_VMX_CHBLOCK_MSG),3000)
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
            it.setClass(MainMenu2Activity.this, LanguageSettingsActivity.class);
            startActivity(it);
        }

        private void SystemSettings() {
            Log.d(TAG, "SystemSettings()");
            Intent it = new Intent();
            it.setClass(MainMenu2Activity.this, SystemSettingsActivity.class);
            startActivity(it);
        }

        private void OutputSettings() {
            Log.d(TAG, "OutputSettings()");
            Intent it = new Intent();
            it.setClass(MainMenu2Activity.this, OutputSettingsActivity.class);
            startActivity(it);
        }

        private void ParentalControl() {
            Log.d(TAG, "ParentalControl()");
            Intent intentParent = new Intent();
            intentParent.setClass(MainMenu2Activity.this,ParentalActivity.class);
            startActivity(intentParent);
        }

        private void ResetFactoryDefault() {
            Log.d(TAG, "ResetFactoryDefault()");
            Intent intentReset = new Intent();
            intentReset.setClass(MainMenu2Activity.this,ResetDefaultActivity.class);
            CheckMenuLockAndStartActivity(MainMenu2Activity.this, intentReset); // Edwin 20181214 live tv cannot scan channel
        }

        private void CAInfo() {// for VMX need open/close
            Log.d(TAG, "CAInfo()");
            Intent intentReset = new Intent();
            intentReset.setClass(MainMenu2Activity.this,CAMenuActivity.class);
            CheckMenuLockAndStartActivity(MainMenu2Activity.this, intentReset); // Edwin 20181214 live tv cannot scan channel
        }

        private void MailInfo() { // connie 20181116 for vmx mail
            Log.d(TAG, "MailInfo()");
            Intent intentReset = new Intent();
            intentReset.setClass(MainMenu2Activity.this,MailActivity.class);
            CheckMenuLockAndStartActivity(MainMenu2Activity.this, intentReset); // Edwin 20181214 live tv cannot scan channel
        }


        private void Timer() {
            Log.d(TAG, "Timer()");
            Intent intentTimer = new Intent();
            intentTimer.setClass(MainMenu2Activity.this,TimerListActivity.class);
            startActivity(intentTimer);
        }

        private void TimeAdjust() {
            Log.d(TAG, "TimeAdjust()");
            Intent intent = new Intent();
            intent.setClass(MainMenu2Activity.this,TimeAdjustActivity.class);
            startActivity(intent);
        }

        private void StorageSettings(){
            Log.d(TAG, "StorageSettings()");

            // edwin 20180802 block startActivity when there is no USB device -s
            if ( UsbCount() == 0 )
            {
                new MessageDialogView( MainMenu2Activity.this
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
            intent.setClass(MainMenu2Activity.this,Storage_device_setup.class);
            startActivity(intent);
        }

        private void RecordSettings() {
            Log.d(TAG, "RecordSettings()");
            Intent intent = new Intent();
            intent.setClass(MainMenu2Activity.this, RecordSettingsActivity.class);
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

    // Edwin 20181214 live tv cannot scan channel -s
    // Johnny add 20180125
//    private void CheckMenuLockAndStartActivity(final Intent it)
//    {
//        if (GposInfoGet().getInstallLockOnOff() == 0)
//        {
//            startActivity(it);
//        }
//        else
//        {
//            new PasswordDialogView(MainMenu2Activity.this, GposInfoGet().getPasswordValue(), PasswordDialogView.TYPE_PINCODE,0) {
//                public void onCheckPasswordIsRight() {
//                    Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
//                    startActivity(it);
//                }
//
//                public void onCheckPasswordIsFalse() {
//                    Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
//
//                    new MessageDialogView(MainMenu2Activity.this
//                            , getString(R.string.STR_INVALID_PASSWORD)
//                            , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
//                        public void dialogEnd() {
//                        }
//                    }.show();
//                }
//                public boolean onDealUpDownKey() {
//                    return false;
//                }
//            };
//        }
//    }
    // Edwin 20181214 live tv cannot scan channel -e

    // edwin 20180802 add USB device count -s
    private int UsbCount()
    {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper helper = new PesiStorageHelper(storageManager);
        int usbCount = 0;

        for ( Object vol : getVolumes() )
        {
            if (helper.isUsb(vol))
            {
                usbCount++;
            }
        }
        Log.d(TAG, "UsbCount: usbCount = " + usbCount);
        return usbCount;
    }
    // edwin 20180802 add USB device count -e

    private void setViewZoomIn(View v)
    {
        AnimationSet animationSet = new AnimationSet((true));
        ScaleAnimation animation;
        if(pageNum==1)
            animation = new ScaleAnimation(1.0f,1.4f,1.0f,1.4f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,1.0f);
        else if(pageNum==4)
            animation = new ScaleAnimation(1.0f,1.4f,1.0f,1.4f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,1.0f);
        else if(pageNum==5)
            animation = new ScaleAnimation(1.0f,1.7f,1.0f,1.7f,
                              Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.7f);
        else
            animation = new ScaleAnimation(1.0f,1.5f,1.0f,1.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,1.0f);
        animation.setDuration(200);
        animation.setFillAfter(true);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animationSet);
    }

    private void setViewZoomOut(View v)
    {
        AnimationSet animationSet = new AnimationSet((true));
        ScaleAnimation animation = new ScaleAnimation(1.3f,1.0f,1.3f,1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,1f);
        animation.setDuration(200);
        animation.setFillAfter(true);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animationSet);
    }

    private ImageView getMenuIconImage(int index)
    {
        Log.d(TAG, "getMenuIconImage:  index = " + index);
        ImageView icon;
        if(index == 1)
            return menu1IGV;
        else if(index == 2)
            return menu2IGV;
        else if(index == 3)
            return menu3IGV;
        else if(index == 4)
            return menu4IGV;
        else
            return menu5IGV;
    }

    // Johnny 20181219 for mouse control
    private int getMenuIconPageNum(View menuIGV) {
        int num = 1;

        if (menuIGV.getId() == menu1IGV.getId()) {
            num = 1;
        }
        else if (menuIGV.getId() == menu2IGV.getId()) {
            num = 2;
        }
        else if (menuIGV.getId() == menu3IGV.getId()) {
            num = 3;
        }
        else if (menuIGV.getId() == menu4IGV.getId()) {
            num = 4;
        }
        else if (menuIGV.getId() == menu5IGV.getId()) {
            num = 5;
        }

        return num;
    }


    private Cursor getChannelCursor() // connie 20190805 for add recommand channel test
    {
        String CHANNEL_SELECTION = TvContractCompat.Channels.COLUMN_TYPE + "='" +
                TvContractCompat.Channels.TYPE_PREVIEW + "'";
// Other column names can be found in TvContractCompat.Channels
        String[] CHANNEL_PROJECTION = {
                TvContractCompat.Channels._ID,
                TvContractCompat.Channels.COLUMN_DISPLAY_NAME,
                TvContractCompat.Channels.COLUMN_PACKAGE_NAME,
                TvContractCompat.Channels.COLUMN_APP_LINK_INTENT_URI,
        };
        Cursor channelCursor = this.getContentResolver().query(
                TvContractCompat.Channels.CONTENT_URI,
                CHANNEL_PROJECTION,
                CHANNEL_SELECTION,
                null, null);

        return channelCursor ;
    }

    private Cursor getProgramCursor( long channelId ) // connie 20190805 for add recommand channel test
    {
        String PROGRAMS_SELECTION =
                TvContractCompat.PreviewPrograms.COLUMN_CHANNEL_ID + "=? AND " +
                        TvContractCompat.PreviewPrograms.COLUMN_BROWSABLE + "=1";

        String[] PROGRAMS_PROJECTION = {
                TvContractCompat.PreviewPrograms._ID,
                TvContractCompat.PreviewPrograms.COLUMN_TITLE,
                TvContractCompat.PreviewPrograms.COLUMN_SHORT_DESCRIPTION,
                TvContractCompat.PreviewPrograms.COLUMN_INTENT_URI,
                TvContractCompat.PreviewPrograms.COLUMN_POSTER_ART_URI
        };
        String[] SELECTION_ARGS = { String.valueOf(channelId) };
        Cursor programCursor = this.getContentResolver().query(
                TvContractCompat.PreviewPrograms.CONTENT_URI,
                PROGRAMS_PROJECTION,
                PROGRAMS_SELECTION,
                SELECTION_ARGS,
                null);

        return programCursor ;
    }
}
