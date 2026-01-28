package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class TeletextDescriptor extends DescBase{

    private final List<Teletext> mteletextList = new ArrayList<Teletext>();


    public static class Teletext {
        /**
         *
         */
        private final String iso639LanguageCode;
        private final int teletextType ;
        private final int teletextMagazineNumber;
        private final int teletextPageNumber;


        public Teletext(final String lCode, final int tType,final int tMagazine,final int tPage){
            iso639LanguageCode = lCode;
            teletextType = tType;
            teletextMagazineNumber = tMagazine;
            teletextPageNumber = tPage;
        }



        @Override
        public String toString(){
            return "code:'"+iso639LanguageCode;
        }


        public int getTeletextType() {
            return teletextType;
        }


        public int getTeletextMagazineNumber() {
            return teletextMagazineNumber;
        }


        public int getTeletextPageNumber() {
            return teletextPageNumber;
        }


        public String getIso639LanguageCode() {
            return iso639LanguageCode;
        }


    }

    public TeletextDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(final byte[] data, int lens) {
        int t=0;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        while ((t+4)<Length) {
            final String languageCode=getISO8859_1String(data, 2+t, 3);
            final int teletext_type = getInt(data,t+5, 1, 0xF8)>>3;
            final int teletext_magazine_number = getInt(data, t+5, 1, 0x07);
            final int teletext__page_number = getInt(data, t+6, 1, 0xFF);
            final Teletext s = new Teletext(languageCode, teletext_type,teletext_magazine_number,teletext__page_number);
            mteletextList.add(s);
            t+=5;
        }
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(super.toString());
        for (Teletext teletext : mteletextList) {
            buf.append(teletext.toString());
        }


        return buf.toString();
    }

    public static String getTeletextTypeString(final int type) {
        switch (type) {
            case 0: return "reserved for future use";
            case 1: return "initial Teletext page";
            case 2: return "Teletext subtitle page";
            case 3: return "additional information page";
            case 4: return "programme schedule page";
            case 5: return "Teletext subtitle page for hearing impaired people";
            default: return "reserved for future use";
        }
    }


    public List<Teletext> getTeletextList() {
        return mteletextList;
    }
}