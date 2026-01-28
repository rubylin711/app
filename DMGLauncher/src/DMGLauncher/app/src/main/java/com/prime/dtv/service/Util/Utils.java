package com.prime.dtv.service.Util;

import static java.lang.Byte.toUnsignedInt;

import android.text.format.DateUtils;
import android.util.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.JulianFields;
import java.util.Calendar;
import java.util.Date;

public final class Utils {
    private static final String TAG = "Utils";

    public static final int MASK_1BIT=0x01;
    public static final int MASK_2BITS=0x03;
    public static final int MASK_3BITS=0x07;
    public static final int MASK_4BITS=0x0F;
    public static final int MASK_5BITS=0x1F;
    public static final int MASK_6BITS=0x3F;
    public static final int MASK_7BITS=0x7F;
    public static final int MASK_8BITS=0xFF;
    public static final int MASK_9BITS=0x01FF;
    public static final int MASK_10BITS=0x03FF;
    public static final int MASK_12BITS=0x0FFF;
    public static final int MASK_13BITS=0x1FFF;
    public static final int MASK_14BITS=0x3FFF;
    public static final int MASK_15BITS=0x7FFF;
    public static final int MASK_16BITS=0xFFFF;
    public static final int MASK_18BITS=0x3_FFFF;
    public static final int MASK_20BITS=0xF_FFFF;
    public static final int MASK_22BITS=0x3F_FFFF;
    public static final int MASK_24BITS=0xFF_FFFF;
    public static final int MASK_31BITS=0x7FF_FFFFF;

    public static final int MASK_32BITS=0xFFFF_FFFF;
    public static final long MASK_33BITS=0x1_FFFF_FFFFL;
    public static final long MASK_40BITS=0xFF_FFFF_FFFFL;
    public static final long MASK_48BITS=0xDDFF_FFFF_FFFFL;
    public static final long MASK_64BITS=0xFFFF_FFFF_FFFF_FFFFL;
    /**
     *
     * Parse an array of bytes into a java String, according to ETSI EN 300 468 V1.11.1 Annex A (normative): Coding of text characters
     * Control chars (0x80.. 0x9f) are removed
     *
     * @param b array of source bytes
     * @param off offset where relevant data starts in array b
     * @param len number of bytes to be parsed
     * @return a java String, according to ETSI EN 300 468 V1.11.1 Annex A,
     */
    public static String getString(final byte[] b, final int off, final int len) {
        String decoded = getCharDecodedStringWithControls(b, off, len);
        return removeControlChars(decoded);

    }


    /**
     * remove formatting, like newlines and character emphasis
     * see table A.1 ETSI EN 300 468 V1.16.1 (2019-08)
     * @param decoded
     * @return
     */
    private static String removeControlChars(String decoded) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < decoded.length(); i++) {
            final char c = decoded.charAt(i);
            if(!isControlCharacter(c)) {
                result.append(c);
            }
        }

        return result.toString();
    }


    private static boolean isControlCharacter(final char c) {
        return (c >= 0x80) && (c <= 0x9f);
    }


    public static String getCharDecodedStringWithControls(final byte[] b, final int off, final int len) {
        int length = len;
        int offset = off;
        if(length<=0){
            return "";
        }
        final Charset charset = getCharSet(b, offset, length);
        final int charSetLen = getCharSetLen(b, offset);

        length -= charSetLen;
        offset += charSetLen;

        String decoded;

        if(charset==null){
            decoded = Iso6937ToUnicode.convert(b, offset, length); //default for DVB
        }else{
            decoded = new String(b, offset, length,charset);
        }
        return decoded;
    }


    public static Charset getCharSet(final byte[] b, final int offset, final int length){
        Charset charset = null;
        if((length>0)&&(b[offset]<0x20)&&(b[offset]>=0)){ //Selection of character table
            final int selectorByte=b[offset];
            try {
                if((selectorByte>0)&&(selectorByte<=0x0b)){
                    charset = Charset.forName("ISO-8859-"+(selectorByte+4));
                }else if((selectorByte==0x10)){
                    if(b[offset+1]==0x0){
                        charset = Charset.forName("ISO-8859-"+b[offset+2]);
                    } // else == reserved for future use, so not implemented
                }else if((selectorByte==0x11 )){ // ISO/IEC 10646
                    charset = StandardCharsets.UTF_16;
                }else if((selectorByte==0x14 )){ // Big5 subset of ISO/IEC 10646
                    charset = Charset.forName("Big5");
                }else if((selectorByte==0x15 )){ // UTF-8 encoding of ISO/IEC 10646
                    charset = StandardCharsets.UTF_8;
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG,"IllegalArgumentException in getCharSet:"+e);
                charset = StandardCharsets.ISO_8859_1;
            }
            if(charset==null){
                charset = StandardCharsets.ISO_8859_1;
            }
        }
        return charset;
    }

    private static int getCharSetLen(final byte[] b, final int offset){
        int charsetLen = 0;
        if((b[offset]<0x20)&&(b[offset]>=0)){ //Selection of character table
            final int selectorByte=b[offset];
            if((selectorByte>0)&&(selectorByte<=0x0b)){
                charsetLen = 1;
            }else if((selectorByte==0x10)){
                if(b[offset+1]==0x0){
                    charsetLen = 3;
                }
            }else if((selectorByte==0x11 )){ // ISO/IEC 10646
                charsetLen = 1;
            }else if((selectorByte==0x14 )){ // Big5 subset of ISO/IEC 10646
                charsetLen = 1;
            }else if((selectorByte==0x15 )){ // UTF-8 encoding of ISO/IEC 10646
                charsetLen = 1;
            }else if((selectorByte==0x1F )){ // described by encoding_type_id
                charsetLen = 2;
            }
        }
        return charsetLen;
    }


    public static String getISO8859_1String(final byte[] b, final int offset, final int length) {
        if(length<=0){
            return "";
        }
        return new String(b, offset, length, StandardCharsets.ISO_8859_1);
    }

    /**
     * convert integer between 0 and 255 (inclusive) into byte (byte is interpreted as unsigned, even though that does not exist in java)
     *
     * @param b
     * @return
     */

    public static byte getInt2UnsignedByte(final int b){
        if(b<=127){
            return (byte)b;
        }
        return (byte) (b-256);
    }


    /**
     * Get an positive integer from array of bytes
     *
     * @param bytes
     * @param offset starting position
     * @param len number of bytes to interpret
     * @param mask bitmask to select which bits to use
     * @return
     */
    public static int getInt(final byte[] bytes, final int offset, final int len, final int mask){
        int r=0;
        for (int i = 0; i < len; i++) {
            r = (r<<8) | toUnsignedInt(bytes[offset+i]);
        }
        return (r&mask);
    }

    /**
     * @param bytes
     * @param offset in bytes array where to start
     * @param len length in bytes (ec 2 bytes if you need 12 bits)
     * @param mask used to remove unwanted bits
     * @return
     */
    public static long getLong(final byte[] bytes, final int offset, final int len, final long mask){
        long r=0;
        for (int i = 0; i < len; i++) {
            r = (r<<8) | toUnsignedInt(bytes[offset+i]);
        }
        return (r&mask);
    }

    /**
     * Get single bit from a byte
     * Numbering starts from high order bit, starts at 1.
     *
     * @param b single byte
     * @param i position of bit in byte, start from 1 up to 8
     * @return 0 or 1 bit value
     */
    public static int getBit(final byte b, final int i) {
        return (( b & (0x80 >> (i-1))));
    }

    /**
     * Get sequence of bits from a byte
     * @param b
     * @param i position of the starting bit
     * @param len number of bits to get starting from i
     * @return sequence of bits as int
     * @example To get bit 3 and 4 call as getBits(b, 3, 2)
     *
     */
    public static int getBits(final byte b, final int i, final int len) {
        int mask = 0x00;

        for(int pos = i; pos < (i+len); ++pos)
        {
            mask |= 0x80 >> (pos-1);
        }

        return (b & mask) >> (9 - i - len);
    }
    /**
     * create a copy of a part of a byte[], use Arrays.copyOfRange instead
     * @param original
     * @param from
     * @param to
     * @return
     */
    @Deprecated
    public static byte[] copyOfRange(final byte[] original, final int from, final int to) {
        final int newLength = to - from;
        if (newLength < 0) {
            throw new IllegalArgumentException(from + " > " + to);
        }
        final byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * returns a copy of bytes from b, starting at offset with total length len. returns empy [] when len == 0
     * @param b
     * @param offset
     * @param len
     * @return
     */
    public static byte[] getBytes(final byte[] b, final int offset, final int len) {
        if(len==0){
            return new byte[0];
        }
        return Utils.copyOfRange(b, offset, offset+len);
    }

    /**
     * Returns a java8 LocalDateTime from rawDateTimeBytes.
     * @param rawDateTimeBytes 5 bytes = 40bits,
     *                         date = 16 bits MJD,
     *                         time = 24bits, 6 digits in 4-bit BCD (hour, min, sec)
     * @return LocalDateTime parsed from rawDateTimeBytes
     */
    public static LocalDateTime getLocalDateTimeFromRawBytes(byte[] rawDateTimeBytes) {
        if (rawDateTimeBytes == null || rawDateTimeBytes.length != 5) {
            Log.e(TAG, "getLocalDateTimeFromRawBytes: rawDateTimeBytes error!");
            return LocalDateTime.now();
        }

        // EXAMPLE: 93/10/13 12:45:00 is coded as "0xC079124500".

        // 16 bits giving the 16 LSBs of MJD
        int modifiedJulianDate = Utils.getInt(rawDateTimeBytes, 0, 2, Utils.MASK_16BITS);
        Log.d(TAG, "getLocalDateTimeFromRawBytes: modifiedJulianDate = " + modifiedJulianDate);

        // 24 bits coded as 6 digits in 4-bit BCD (hour, min, sec)
        int hour = bcdToDec(rawDateTimeBytes[2]);
        int minute = bcdToDec(rawDateTimeBytes[3]);
        int second = bcdToDec(rawDateTimeBytes[4]);
        Log.d(TAG, "getLocalDateTimeFromRawBytes: hour = " + hour + ", min = " + minute + ", sec = " + second);

        // to java8 LocalDateTime
        LocalDate localDate = LocalDate.MIN.with(JulianFields.MODIFIED_JULIAN_DAY, modifiedJulianDate);
        LocalTime localTime = LocalTime.of(hour, minute, second);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        Log.d(TAG, "getLocalDateTimeFromRawBytes: localDateTime = " + localDateTime);
        return localDateTime;
    }

    /**
     * Returns time in millis from the epoch
     * @param rawDateTimeBytes 5 bytes = 40bits,
     *                         date = 16 bits MJD,
     *                         time = 24bits, 6 digits in 4-bit BCD (hour, min, sec)
     * @return time in millis parsed from rawDateTimeBytes
     */
    // refer to:
    // https://cs.android.com/android/platform/superproject/+/master:packages/apps/TV/tuner/src/com/android/tv/tuner/data/SectionParser.java;l=1137;drc=master
    public static long getTimeMillisFromRawBytes(byte[] rawDateTimeBytes) {
        if (rawDateTimeBytes == null || rawDateTimeBytes.length != 5) {
            Log.e(TAG, "getTimeMillisFromRawBytes: rawDateTimeBytes error!");
            return 0;
        }

        // EXAMPLE: 93/10/13 12:45:00 is coded as "0xC079124500".

        // 16 bits giving the 16 LSBs of MJD
        float modifiedJulianDate = Utils.getInt(rawDateTimeBytes, 0, 2, Utils.MASK_16BITS);
        Log.d(TAG, "getTimeMillisFromRawBytes: modifiedJulianDate = " + modifiedJulianDate);

        int year = (int) ((modifiedJulianDate - 15078.2f) / 365.25f);
        int mjdMonth =
                (int) ((modifiedJulianDate
                        - 14956.1f
                        - (int) (year * 365.25f)) / 30.6001f);
        int day =
                (int) modifiedJulianDate
                        - 14956
                        - (int) (year * 365.25f)
                        - (int) (mjdMonth * 30.6001f);
        int month = mjdMonth - 1;
        if (mjdMonth == 14 || mjdMonth == 15) {
            year += 1;
            month -= 12;
        }
        Log.d(TAG, "getTimeMillisFromRawBytes: year = " + year + ", month = " + month + ", day = " + day);

        // 24 bits coded as 6 digits in 4-bit BCD (hour, min, sec)
        int hour = bcdToDec(rawDateTimeBytes[2]);
        int minute = bcdToDec(rawDateTimeBytes[3]);
        int second = bcdToDec(rawDateTimeBytes[4]);
        Log.d(TAG, "getTimeMillisFromRawBytes: hour = " + hour + ", min = " + minute + ", sec = " + second);

        // get time in seconds from a calender
        Calendar calendar = Calendar.getInstance();
        year += 1900;
        // month - 1 here because
        // month value in Calendar.set() is 0-based. e.g., 0 for January.
        calendar.set(year, month-1, day, hour, minute, second);
        long startTime = calendar.getTimeInMillis();
        Log.d(TAG, "getTimeMillisFromRawBytes: startTime = " + startTime);

        return startTime;
    }

    /**
     * Returns a java8 Duration from rawDurationBytes.
     *
     * @param rawDurationBytes 3 bytes, 24 bits coded as 6 digits in 4-bit BCD (hour, min, sec).
     *                         e.g. 01:45:30 is coded as "0x014530".
     * @return Duration parsed from rawDurationBytes
     */
    public static Duration getDurationFromRawBytes(byte[] rawDurationBytes) {
        if (rawDurationBytes == null || rawDurationBytes.length != 3) {
            Log.e(TAG, "getDurationFromRawBytes: rawDurationBytes error!");
            return Duration.ZERO;
        }

        // 24 bits coded as 6 digits in 4-bit BCD (hour, min, sec)
        int hour = bcdToDec(rawDurationBytes[0]);
        int minute = bcdToDec(rawDurationBytes[1]);
        int second = bcdToDec(rawDurationBytes[2]);
        Log.d(TAG, "getDurationFromRawBytes: hour = " + hour + ", min = " + minute + ", sec = " + second);

        // to java8 Duration
        Duration duration = Duration.ofHours(hour).plusMinutes(minute).plusSeconds(second);
        Log.d(TAG, "getDurationFromRawBytes: duration = " + duration);
        return duration;
    }

    /**
     * Returns duration in millis from the epoch
     * @param rawDurationBytes 3 bytes, 24 bits coded as 6 digits in 4-bit BCD (hour, min, sec).
     *                         e.g. 01:45:30 is coded as "0x014530".
     * @return duration in millis parsed from rawDurationBytes
     */
    public static long getDurationMillisFromRawBytes(byte[] rawDurationBytes) {
        if (rawDurationBytes == null || rawDurationBytes.length != 3) {
            Log.e(TAG, "getDurationMillisFromRawBytes: rawDurationBytes error!");
            return 0;
        }

        // 24 bits coded as 6 digits in 4-bit BCD (hour, min, sec)
        int hour = bcdToDec(rawDurationBytes[0]);
        int minute = bcdToDec(rawDurationBytes[1]);
        int second = bcdToDec(rawDurationBytes[2]);
        Log.d(TAG, "getDurationMillisFromRawBytes: hour = " + hour + ", min = " + minute + ", sec = " + second);

        // to seconds
        long duration = (hour*3600 + minute*60 + second) * DateUtils.SECOND_IN_MILLIS;
        Log.d(TAG, "getDurationMillisFromRawBytes: duration = " + duration);
        return duration;
    }

    /**
     * Convert from bcd to decimal
     * @param bcdVal bcd byte
     * @return decimal int
     */
    private static int bcdToDec(byte bcdVal) {
        return (bcdVal / 16 * 10) + (bcdVal % 16);
    }

    /**
     * Returns a formatted string from LocalDateTime
     *
     * @param localDateTime
     * @return time string formatted as "yyyy/MM/dd HH:mm:ss"
     */
    public static String getUtcFormattedTimeString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }

    /**
     * Returns Date from LocalDateTime
     * @param localDateTime
     * @return Date at UTC ZoneOffset
     */
    public static Date getDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) return new byte[0];
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }
}
