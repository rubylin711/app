package com.prime.dtvplayer.Activity;

import android.app.ActivityManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;
import com.prime.dtvplayer.Sysdata.GposInfo;

import java.util.List;
import java.util.Locale;

/**
 * Created by edwin on 2017/11/17.
 */

public class LanguageSettingsActivity extends DTVActivity
{
    private final String TAG = getClass().getSimpleName();
    private Spinner osdLang;
    private Spinner firstLang;
    private Spinner secondLang;
    private Spinner subtitleDisp;
    private Spinner subtitleLang;
    private SelectBoxView selOsdLang;
    private SelectBoxView sel1stLang;
    private SelectBoxView sel2ndLang;
    private SelectBoxView selSubtDisp;
    private SelectBoxView selSubtLang;
    private Resources res;
    private Locale mCurLocale;
//    private List<LocalePicker.LocaleInfo> mSupportLanguage;


    @Override
    public void onCreate ( Bundle savedInstanceState )
    {
        Log.d( TAG, "onCreate: " );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.language_settings );

        res = getResources();

        InitTitleHelp();
        InitSelectBox();
    }

    @Override
    protected void onResume ()
    {
        Log.d( TAG, "onResume: " );
        super.onResume();
        String gposLang;
        String[] allLanguageCodes;
        String[] osdLanguageCodes;
        int gposOnOff;
        GposInfo gposInfo;

        gposInfo = GposInfoGet();
        allLanguageCodes = res.getStringArray( R.array.STR_LANGUAGE_CODES_ISO639 );
        osdLanguageCodes = res.getStringArray( R.array.STR_OSD_LANGUAGE_CODE );

        // get OSD language
        //gposLang = gposInfo.getOSDLanguage();
        //osdLang.setSelection( IndexOf( mCurLocale.toString(), osdLanguageCodes ) );
        osdLang.setSelection(0);

        // get first language
        gposLang = gposInfo.getAudioLanguageSelection( 0 );
        firstLang.setSelection( IndexOf( gposLang, allLanguageCodes ) );

        // get second language
        gposLang = gposInfo.getAudioLanguageSelection( 1 );
        secondLang.setSelection( IndexOf( gposLang, allLanguageCodes ) );

        // get subtitle display
        gposOnOff = gposInfo.getSubtitleOnOff();
        subtitleDisp.setSelection( gposOnOff );

        // get subtitle language
        gposLang = gposInfo.getSubtitleLanguageSelection( 0 );
        subtitleLang.setSelection( IndexOf( gposLang, allLanguageCodes ) );
    }

    @Override
    protected void onPause ()
    {
        Log.d( TAG, "onPause: " );
        super.onPause();
        int subtitleOnOff;
        int selectedLangIndex;
        String[] allLanguageCodes = res.getStringArray( R.array.STR_LANGUAGE_CODES_ISO639 );
        GposInfo gposInfo = GposInfoGet();

        // save selected OSD Lang
        String osdLang = (String) selOsdLang.GetSelectedItem();
        gposInfo.setOSDLanguage( osdLang );

        // save selected 1st Lang
        selectedLangIndex = sel1stLang.GetSelectedItemIndex();
        gposInfo.setAudioLanguageSelection( 0, allLanguageCodes[selectedLangIndex] );
        AvControlSetSubtitleLanguage(ViewHistory.getPlayId(), 0 , allLanguageCodes[selectedLangIndex]);//eric lin 20180918 set sub lang after change auto subtitle language

        // save selected 2nd Lang
        selectedLangIndex = sel2ndLang.GetSelectedItemIndex();
        gposInfo.setAudioLanguageSelection( 1, allLanguageCodes[selectedLangIndex] );
        AvControlSetSubtitleLanguage(ViewHistory.getPlayId(), 1, allLanguageCodes[selectedLangIndex]);//eric lin 20180828 set sub language

        // save subtitle On Off
        subtitleOnOff = selSubtDisp.GetSelectedItemIndex();
        gposInfo.setSubtitleOnOff( subtitleOnOff );

        // save selected subtitle Lang
        selectedLangIndex = selSubtLang.GetSelectedItemIndex();
        gposInfo.setSubtitleLanguageSelection( 0, allLanguageCodes[selectedLangIndex] );


        GposInfoUpdate( gposInfo );
//        LocalePicker.updateLocale( mCurLocale );


        //Log.d(TAG, "onPause: set sub lang="+allLanguageCodes[selectedLangIndex]);//eric lin test

    }

    private void InitTitleHelp ()
    {
        Log.d( TAG, "InitTitleHelp: " );
        ActivityTitleView title = (ActivityTitleView) findViewById( R.id.langTitleLayout );
        ActivityHelpView help = (ActivityHelpView) findViewById( R.id.langHelpLayout );
        String orgStr;
        //        SpannableStringBuilder colorStr;

        title.setTitleView( getString( R.string.STR_LANGUAGE_SETTINGS_TITTLE ) );
        orgStr = getString( R.string.STR_LANGUAGE_SETTINGS_HELP_INFO );
        //        int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
        //        ForegroundColorSpan yellow[] = {
        //                new ForegroundColorSpan(Color.YELLOW),
        //                new ForegroundColorSpan(Color.YELLOW),
        //                new ForegroundColorSpan(Color.YELLOW),
        //                new ForegroundColorSpan(Color.YELLOW)
        //        };

        //        colorStr = new SpannableStringBuilder(orgStr);
        //        colorStr.setSpan(yellow[0],4, 4 + 2, flag);
        //        colorStr.setSpan(yellow[1],15, 15 + 2, flag);
        //        colorStr.setSpan(yellow[2],28, 28 + 2, flag);
        //        colorStr.setSpan(yellow[3],50, 50 + 4, flag);

        help.setHelpInfoTextBySplit( orgStr );
        help.resetHelp( 1, 0, null );
        help.resetHelp( 2, 0, null );
        help.resetHelp( 3, 0, null );
        help.resetHelp( 4, 0, null );
    }

    private void InitSelectBox ()
    {
        Log.d( TAG, "InitSelectBox: " );

        InitOSDLang();

        osdLang = (Spinner) findViewById( R.id.menuLangOpt );
        firstLang = (Spinner) findViewById( R.id.firstLangOpt );
        secondLang = (Spinner) findViewById( R.id.secondLangOpt );
        subtitleDisp = (Spinner) findViewById( R.id.subtitleDisplayOpt );
        subtitleLang = (Spinner) findViewById( R.id.subtitleLangOpt );

        Resources res = getResources();
        selOsdLang = new SelectBoxView( this, osdLang, res.getStringArray( R.array.STR_MENU_LANGUAGE_OPTION ) );
        sel1stLang = new SelectBoxView( this, firstLang, res.getStringArray( R.array.STR_FIRST_LANGUAGE_OPTION ) );
        sel2ndLang = new SelectBoxView( this, secondLang, res.getStringArray( R.array.STR_SECOND_LANGUAGE_OPTION ) );
        selSubtDisp = new SelectBoxView( this, subtitleDisp, res.getStringArray( R.array.STR_SUBTITLE_DISPLAY_OPTION ) );
        selSubtLang = new SelectBoxView( this, subtitleLang, res.getStringArray( R.array.STR_SUBTITLE_LANGUAGE_OPTION ) );

        osdLang.setOnItemSelectedListener( ChangeOSDLang() );
    }

    private int IndexOf ( String thisStr, String[] allStrings )
    {
        Log.d( TAG, "IndexOf: " );
        int i = 0;
        for ( String str : allStrings )
        {
            if ( str.equalsIgnoreCase( thisStr ) )
            {
                return i;
            }
            i++;
        }
        Log.e( TAG, "IndexOf: index not found!" );
        return 0;
    }

    private void InitOSDLang()
    {
        boolean isInDeveloperMode = Settings.Global.getInt( this.getContentResolver()
                , Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0 ) != 0;
        Log.d(TAG, "InitOSDLang: isInDeveloperMode = "+isInDeveloperMode);
//        mSupportLanguage = LocalePicker.getAllAssetLocales( this, isInDeveloperMode );

//        try
//        {
//            mCurLocale = ActivityManager.getService().getConfiguration().getLocales().get( 0 );
//        }
//        catch ( RemoteException e )
//        {
//            e.printStackTrace();
//        }

//        Log.d( TAG, "currentLocale = " + mCurLocale + " country = " + mCurLocale.toLanguageTag() );
//        for ( final LocalePicker.LocaleInfo localeInfo : mSupportLanguage )
//        {
//            Log.d( TAG, "locate = " + localeInfo.getLocale() +
//                    " label = " + localeInfo.getLabel() +
//                    " country = " + localeInfo.getLocale().toLanguageTag() );
//        }
    }

    private SelectBoxView.SelectBoxOnItemSelectedListener ChangeOSDLang()
    {
        Log.d( TAG, "ChangeOSDLang: " );

        return new SelectBoxView.SelectBoxOnItemSelectedListener()
        {
            @Override
            public void onItemSelected ( AdapterView<?> parent, View view, int position, long id )
            {
                Log.d( TAG, "onItemSelected: Change OSD Lang, select = "+parent.getSelectedItem() );

                super.onItemSelected( parent, view, position, id );
                Locale locale = GetLocale( (String) parent.getSelectedItem() );

                if ( locale != null && !mCurLocale.equals( locale ) )
                {
                    mCurLocale = locale;
                }
            }
        };
    }

    private Locale GetLocale(String lang)
    {
        Log.d( TAG, "GetLocale: " );
        
        String langCode =
        lang.equalsIgnoreCase( getString( R.string.STR_ENGLISH) )    ? getString( R.string.STR_EN_US ) :
        lang.equalsIgnoreCase( getString( R.string.STR_FINNISH) )    ? getString( R.string.STR_FI_FI ) :
        lang.equalsIgnoreCase( getString( R.string.STR_FRENCH) )     ? getString( R.string.STR_FR_FR ) :
        lang.equalsIgnoreCase( getString( R.string.STR_GERMAN) )     ? getString( R.string.STR_DE_DE ) :
        lang.equalsIgnoreCase( getString( R.string.STR_GREEK) )      ? getString( R.string.STR_EL_GR ) :
        lang.equalsIgnoreCase( getString( R.string.STR_HUNGARIAN) )  ? getString( R.string.STR_HU_HU ) :
        lang.equalsIgnoreCase( getString( R.string.STR_ITALIAN) )    ? getString( R.string.STR_IT_IT ) :
        lang.equalsIgnoreCase( getString( R.string.STR_NORWEGIAN) )  ? getString( R.string.STR_NB_NO ) :
        lang.equalsIgnoreCase( getString( R.string.STR_POLISH) )     ? getString( R.string.STR_PL_PL ) :
        lang.equalsIgnoreCase( getString( R.string.STR_PORTUGUESE) ) ? getString( R.string.STR_PT_PT ) :
        lang.equalsIgnoreCase( getString( R.string.STR_ROMANIAN) )   ? getString( R.string.STR_RO_RO ) :
        lang.equalsIgnoreCase( getString( R.string.STR_RUSSIAN) )    ? getString( R.string.STR_RU_RU ) :
        lang.equalsIgnoreCase( getString( R.string.STR_SLOVENIAN) )  ? getString( R.string.STR_SL_SI ) :
        lang.equalsIgnoreCase( getString( R.string.STR_SPANISH) )    ? getString( R.string.STR_ES_ES ) :
        lang.equalsIgnoreCase( getString( R.string.STR_SWEDISH) )    ? getString( R.string.STR_SV_SE ) :
        lang.equalsIgnoreCase( getString( R.string.STR_TURKISH) )    ? getString( R.string.STR_TR_TR ) :
        lang.equalsIgnoreCase( getString( R.string.STR_ARABIC) )     ? getString( R.string.STR_AR ) :
        lang.equalsIgnoreCase( getString( R.string.STR_CHINESE) )    ? getString( R.string.STR_ZH_TW ) :
        lang.equalsIgnoreCase( getString( R.string.STR_CZECH) )      ? getString( R.string.STR_CS_CZ ) :
        lang.equalsIgnoreCase( getString( R.string.STR_DANISH) )     ? getString( R.string.STR_DA_DK ) :
        lang.equalsIgnoreCase( getString( R.string.STR_DUTCH) )      ? getString( R.string.STR_NL_NL ) :
        lang.equalsIgnoreCase( getString( R.string.STR_SLOVAK) )     ? getString( R.string.STR_SK_SK ) :
        lang.equalsIgnoreCase( getString( R.string.STR_UKRAINIAN) )  ? getString( R.string.STR_UK_UA ) :
        lang.equalsIgnoreCase( getString( R.string.STR_TATAR) )      ? getString( R.string.STR_TT ) :
        lang.equalsIgnoreCase( getString( R.string.STR_MOLDAVIAN) )  ? getString( R.string.STR_MO ) :
        lang.equalsIgnoreCase( getString( R.string.STR_BELARUSIAN) ) ? getString( R.string.STR_BE_BY ) : null;

        if ( langCode == null )
        {
            return null;
        }

//        for ( LocalePicker.LocaleInfo localeInfo : mSupportLanguage )
//        {
//            if ( localeInfo.getLocale().toString().equalsIgnoreCase( langCode ) )
//            {
//                return localeInfo.getLocale();
//            }
//        }
        return null;
    }
}
