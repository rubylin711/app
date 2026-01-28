package com.prime.soclibcontrol;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import android.util.Log;
import java.nio.ByteOrder;

public class HdmiEdidParser {
    private static final String TAG = "HdmiEdidParser" ;
    private static final int EDID_BLOCK_SIZE = 128;
    private static final int TV_NAME_DESCRIPTOR_TAG = 0xFC;
    private static final int PRODUCT_CODE_OFFSET = 10; // The Product Code offset within the EDID data

    public static void parseEdid(byte[] edidData) {
        if (edidData.length < EDID_BLOCK_SIZE) {
            Log.d( TAG, "Invalid EDID data.");
            return;
        }

        // Extract Manufacturer ID
        String manufactureName = extractManufacturerId(edidData);
        Log.d( TAG, "Manufacturer ID: " + manufactureName);

        // Extract Product Code
        int productCode = extractProductCode(edidData);
        Log.d( TAG, "Product Code: " + Integer.toHexString(productCode));

        // Extract TV Name
        String tvName = extractTvName(edidData);
        if (tvName != null) {
            Log.d( TAG, "TV Name: " + tvName);
        } else {
            Log.d( TAG, "TV Name not found.");
        }
    }

    public static String extractManufacturerId(byte[] edidData) {
        // Extract 2 bytes for the Manufacturer ID (from offset 0x08 and 0x09)
        int manufacturerId = ((edidData[8] & 0xFF) << 8) | (edidData[9] & 0xFF);
        
        // Extract each letter from the 5-bit fields
        char firstLetter = (char) ('A' + ((manufacturerId >> 10) & 0x1F) - 1);
        char secondLetter = (char) ('A' + ((manufacturerId >> 5) & 0x1F) - 1);
        char thirdLetter = (char) ('A' + (manufacturerId & 0x1F) - 1);
        
        // Concatenate the three letters to form the Manufacturer ID
        return "" + firstLetter + secondLetter + thirdLetter;
    }

    public static int extractProductCode(byte[] edidData) {
        // Extract 2 bytes for the Product Code (from offset 0x0A and 0x0B)
        ByteBuffer buffer = ByteBuffer.wrap(edidData, PRODUCT_CODE_OFFSET, 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Specify that the byte order is little-endian
        return buffer.getShort() & 0xFFFF; // Unsigned short value
    }

    public static String extractTvName(byte[] edidData) {
        // Search for the descriptor block that contains the TV Name (0xFC tag)
        for (int i = 0x36; i < EDID_BLOCK_SIZE - 1; i += 19) {
            if ((edidData[i] & 0xFF) == TV_NAME_DESCRIPTOR_TAG) {
                // Found TV Name tag, extract the string from next bytes
                return new String(Arrays.copyOfRange(edidData, i + 1, i + 18), StandardCharsets.US_ASCII).trim();
            }
        }
        return null;
    }

    public static void printEdidRawData(byte[] edidData) {
        if (edidData == null || edidData.length < EDID_BLOCK_SIZE) {
            Log.d( TAG, "Invalid EDID data.");
            return;
        }

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < edidData.length; i++) {
            // Append each byte as 2-digit hexadecimal to the string
            hexString.append(String.format("%02X ", edidData[i]));

            // Append a new line every 16 bytes
            if ((i + 1) % 16 == 0) {
                hexString.append("\n");
            }
        }

        Log.d( TAG, hexString.toString());
    }
}
