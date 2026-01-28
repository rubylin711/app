package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.List;

public class SubtitlingDescriptor extends DescBase{

    private final List<Subtitle> msubtitleList = new ArrayList<Subtitle>();
    private int subt_lang_num;


    public static class Subtitle{
        /* subtitling type
        0x00	保留 (Reserved)，未使用
        0x01	標準字幕 (Normal DVB Subtitles)，適用於 4:3 視訊
        0x02	標準字幕 (Normal DVB Subtitles)，適用於 16:9 視訊
        0x03	標準字幕 (Normal DVB Subtitles)，適用於 2.21:1 視訊
        0x04	高對比字幕 (High-contrast DVB Subtitles)，適用於 4:3 視訊
        0x05	高對比字幕 (High-contrast DVB Subtitles)，適用於 16:9 視訊
        0x06	高對比字幕 (High-contrast DVB Subtitles)，適用於 2.21:1 視訊
        0x07	聽障字幕 (For the hard-of-hearing, normal DVB Subtitles)，適用於 4:3 視訊
        0x08	聽障字幕 (For the hard-of-hearing, normal DVB Subtitles)，適用於 16:9 視訊
        0x09	聽障字幕 (For the hard-of-hearing, normal DVB Subtitles)，適用於 2.21:1 視訊
        0x0A	聽障字幕 (For the hard-of-hearing, high-contrast DVB Subtitles)，適用於 4:3 視訊
        0x0B	聽障字幕 (For the hard-of-hearing, high-contrast DVB Subtitles)，適用於 16:9 視訊
        0x0C	聽障字幕 (For the hard-of-hearing, high-contrast DVB Subtitles)，適用於 2.21:1 視訊
        0x0D - 0xFF	保留 (Reserved for future use)
        we use 0xFE for CEA-608 (close caption)
        */
        public static final int SUBTITLING_TYPE_DVB = 0xFC;
        public static final int SUBTITLING_TYPE_ARIB_STD_B24 = 0xFD;
        public static final int SUBTITLING_TYPE_CEA608 = 0xFE;
        private final String iso639LanguageCode;
        private final int subtitlingType ;
        private final int compositionPageId;
        private final int ancillaryPageId;


        public Subtitle(final String lCode, final int sType,final int sCompositionPageDd,final int aPageId){
            iso639LanguageCode = lCode;
            subtitlingType = sType;
            compositionPageId = sCompositionPageDd;
            ancillaryPageId = aPageId;
        }


        @Override
        public String toString(){
            return "code:'"+iso639LanguageCode;
        }


        public String getIso639LanguageCode() {
            return iso639LanguageCode;
        }


        public int getSubtitlingType() {
            return subtitlingType;
        }


        public int getCompositionPageId() {
            return compositionPageId;
        }


        public int getAncillaryPageId() {
            return ancillaryPageId;
        }


    }

    public SubtitlingDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        if((Length+2) == lens)
            DataExist = true;
        else
            DataExist = false;
        int t=0;
        while (t<Length) {
            final String languageCode=getISO8859_1String(data, 2+t, 3);
            final int subtitling_type = getInt(data, 5+t, 1, MASK_8BITS);
            final int composition_page_id = getInt(data, 6+t, 2, MASK_16BITS);
            final int ancillary_page_id = getInt(data, 8+t, 2, MASK_16BITS);
            final Subtitle s = new Subtitle(languageCode, subtitling_type,composition_page_id,ancillary_page_id);
            msubtitleList.add(s);
            t+=8;
        }
        subt_lang_num = msubtitleList.size();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(super.toString());
        for (Subtitle subtitle : msubtitleList) {
            buf.append(subtitle.toString());
        }


        return buf.toString();
    }

    public int getNoSubtitle(){
        return msubtitleList.size();
    }

    public List<Subtitle> getSubtitleList() {
        return msubtitleList;
    }
}