package com.prime.launcher.Utils;

import android.content.Context;

import com.prime.launcher.R;

public class LanguageCode {
    public String TAG = "LanguageCode";

    public static String getLanguageString(Context context, String languageCode) {
        String lang = "Unknown";
        if(languageCode == null)
            return lang;
//        if(languageCode.equalsIgnoreCase("eng"))
//            return context.getString(R.string.dvb_language_eng);
//        if(languageCode.equalsIgnoreCase("chi"))
//            return context.getString(R.string.dvb_language_chi);
//        if(languageCode.equalsIgnoreCase("por"))
//            return context.getString(R.string.dvb_language_por);
//        if(languageCode.equalsIgnoreCase("pol"))
//            return context.getString(R.string.dvb_language_pol);
//        if(languageCode.equalsIgnoreCase("fre"))
//            return context.getString(R.string.dvb_language_fre);
//        if(languageCode.equalsIgnoreCase("tur"))
//            return context.getString(R.string.dvb_language_tr);
        return languageCode;
    }


}
