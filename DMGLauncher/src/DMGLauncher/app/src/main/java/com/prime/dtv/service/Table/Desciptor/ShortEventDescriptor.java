package com.prime.dtv.service.Table.Desciptor;


import android.util.Log;

import com.prime.dtv.service.Util.Utils;

public class ShortEventDescriptor extends DescBase {
    private static final String TAG = "ShorEventDescriptor";

    private String mLanguageCode; // ISO_639_language_code
    private String mEventName;
    private String mEventText;


    public ShortEventDescriptor(byte[] data) {
        Parsing(data, data.length);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        if (data.length < MIN_DESCRIPTOR_LENGTH) {
            Log.e(TAG, "Parsing: length error!");
            return;
        }

        Tag = Byte.toUnsignedInt(data[0]);
        Length = Byte.toUnsignedInt(data[1]); // descriptor_length
        if(lens == Length+MIN_DESCRIPTOR_LENGTH && Length>0)
            DataExist = true;

        if (data.length != MIN_DESCRIPTOR_LENGTH + Length) {
            Log.e(TAG, "Parsing: broken data!");
            return;
        }

        // ISO_639_language_code = 3rd to 5th byte
        mLanguageCode = Utils.getISO8859_1String(data, 2, 3);

        // event_name_length = 6th byte
        int eventNameLength = Byte.toUnsignedInt(data[5]);
        // event_name_char start from 7th byte
        mEventName = Utils.getISO8859_1String(data, 6, eventNameLength);

        // text_length = (6+event_name_length)th byte
        int textLengthStartPos = 6 + eventNameLength;
        int eventTextLength = Byte.toUnsignedInt(data[textLengthStartPos]);
        // text_char start from (textLengthStartPos+1))th byte
        mEventText = Utils.getISO8859_1String(data, textLengthStartPos+1, eventTextLength);
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public String getEventName() {
        return mEventName;
    }

    public String getEventText() {
        return mEventText;
    }
}
