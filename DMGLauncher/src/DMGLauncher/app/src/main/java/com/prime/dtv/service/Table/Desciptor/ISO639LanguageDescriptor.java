package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ISO639LanguageDescriptor extends DescBase{
    private static final String TAG = "ISO639LanguageDescriptor";

    private final List<Language> languageList = new ArrayList<Language>();


    public static class Language{
        /**
         *
         */
        private final String iso639LanguageCode;
        /**
         *
         */
        private final int audioType;


        public Language(final String lCode, final int audioT){
            iso639LanguageCode = lCode;
            audioType = audioT;
        }




        public int getAudioType() {
            return audioType;
        }


        public String getIso639LanguageCode() {
            return iso639LanguageCode;
        }


        @Override
        public String toString(){
            return "code:'"+iso639LanguageCode+"', audio:"+audioType;
        }


    }

    public ISO639LanguageDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        int t=0;
        while (t<Length) {
            final String languageCode=getISO8859_1String(data, t+2, 3);
            final int audio = getInt(data, t+5, 1, MASK_8BITS);
            Log.d(TAG,"languageCode = " + languageCode);
            final Language s = new Language(languageCode, audio);
            languageList.add(s);
            t+=4;
        }
    }


    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(super.toString());
        for (Language language : languageList) {
            buf.append(language.toString());
        }


        return buf.toString();
    }

    public static String getAudioTypeString(final int audio) {
        switch (audio) {
            case 0: return "Undefined";
            case 1: return "Clean effects";
            case 2: return "Hearing impaired";
            case 3: return "Visual impaired commentary";
            default:
                if ((audio >= 0x04) && (audio <= 0x7F)){
                    return "User Private";
                }
                return "Reserved";
        }
    }

    public List<Language> getLanguageList() {
        return languageList;
    }
}