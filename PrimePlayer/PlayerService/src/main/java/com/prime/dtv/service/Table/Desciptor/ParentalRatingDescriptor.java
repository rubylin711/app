package com.prime.dtv.service.Table.Desciptor;

import android.util.Log;

import com.prime.dtv.service.Util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ParentalRatingDescriptor extends DescBase {
    private static final String TAG = "ParentalRatingDescriptor";

    private static final int RATING_DATA_LENGTH = 4;

    List<ParentalRating> mParentalRatingList;

    public ParentalRatingDescriptor(byte[] data) {
        mParentalRatingList = new ArrayList<>();
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

        int ratingCount = Length / RATING_DATA_LENGTH; // Length = desc length
        for (int i = 0 ; i < ratingCount ; i++) {
            int ratingDataPos = MIN_DESCRIPTOR_LENGTH + i*RATING_DATA_LENGTH;
            // ISO_639_language_code = 3 byte after desc_tag&desc_length and pre rating data
            String countryCode = Utils.getISO8859_1String(data, ratingDataPos, 3);
            // rating = 1 byte after ISO_639_language_code
            int rating = Byte.toUnsignedInt(data[ratingDataPos+3]);

            ParentalRating parentalRating = new ParentalRating(countryCode, rating);
            mParentalRatingList.add(parentalRating);
        }
    }

    public List<ParentalRating> getParentalRatingList() {
        return mParentalRatingList;
    }

    public ParentalRating getParentalRating(String countryCode) {
        for (ParentalRating parentalRating : mParentalRatingList) {
            if (parentalRating.getCountryCode().equalsIgnoreCase(countryCode)) {
                return parentalRating;
            }
        }

        return null;
    }

    public static class ParentalRating {
        private static final String TAG = "ParentalRating";

        private final String mCountryCode;
        private final int mRating;


        public ParentalRating(String countryCode, int rating) {
            mCountryCode = countryCode;
            mRating = rating;
        }

        public String getCountryCode() {
            return mCountryCode;
        }

        public int getRating() {
            return mRating;
        }

        public int getMinAge() {
            return mRating+3;
        }
    }
}
