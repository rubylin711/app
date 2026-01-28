package com.prime.dtv.service.Table.Desciptor;

import android.util.Log;

import com.prime.dtv.service.Util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ExtendedEventDescriptor extends DescBase {
    private static final String TAG = "ExtendedEventDescriptor";

    private int mDescNumber;
    private int mLastDescNumber;
    private String mLanguageCode; // ISO_639_language_code
    private final List<Item> mItemList;
    private String mText; // non itemized extended text

    public ExtendedEventDescriptor(byte[] data) {
        mItemList = new ArrayList<>();
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

        // descriptor_number = 4 left bits(mask = 0xF0 = 11110000) of 3rd byte and right shift 4
        mDescNumber = Utils.getInt(data, 2, 1, 0xF0) >> 4;

        // last_descriptor_number = 4 right bits of 3rd byte
        mLastDescNumber = Utils.getInt(data, 2, 1, Utils.MASK_4BITS);

        // ISO_639_language_code = 4th to 6th byte
        mLanguageCode = Utils.getISO8859_1String(data, 3, 3);

        // length_of_items = 7th byte
        int itemsLength = Byte.toUnsignedInt(data[6]);

        // items
        int itemsPos = 7; // 7 = bytes before items
        int itemsEnd = 7 + itemsLength; // items end pos
        while (itemsPos < itemsEnd) {
            int descriptionLength = Byte.toUnsignedInt(data[itemsPos]);
            itemsPos += 1;
            if (itemsPos + descriptionLength > itemsEnd) {
                Log.e(TAG, "Parsing: broken data!");
                return;
            }
            String itemDescription
                    = Utils.getISO8859_1String(data, itemsPos, descriptionLength);
            itemsPos += descriptionLength;

            int itemTextLength = Byte.toUnsignedInt(data[itemsPos]);
            itemsPos += 1;
            if (itemsPos + itemTextLength > itemsEnd) {
                Log.e(TAG, "Parsing: broken data!");
                return;
            }
            String itemText = Utils.getISO8859_1String(data, itemsPos, itemTextLength);
            itemsPos += itemTextLength;

            Item item = new Item(itemDescription, itemText);
            mItemList.add(item);
        }

        // non itemized extended text
        int textLength = Byte.toUnsignedInt(data[itemsEnd]);
        mText = Utils.getISO8859_1String(data, itemsEnd+1, textLength);

    }

    public int getDescriptorNumber() {
        return mDescNumber;
    }

    public int getLastDescriptorNumber() {
        return mLastDescNumber;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public List<Item> getItemList() {
        return mItemList;
    }

    public String getText() {
        return mText;
    }

    public static class Item {
        private static final String TAG = "Item";

        private final String mItemDescription;
        private final String mItem;


        public Item(String itemDescription, String itemText) {
            mItemDescription = itemDescription;
            mItem = itemText;
        }

        public String getItemDescription() {
            return mItemDescription;
        }

        public String getItem() {
            return mItem;
        }
    }
}
