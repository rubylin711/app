package com.prime.dtv.service.Util;

import static com.prime.dtv.service.Util.Utils.MASK_8BITS;
import static com.prime.dtv.service.Util.Utils.getCharDecodedStringWithControls;
import static com.prime.dtv.service.Util.Utils.getInt;
import static com.prime.dtv.service.Util.Utils.getString;
import static java.lang.Byte.toUnsignedInt;

import java.nio.charset.Charset;

public class DVBString {
    private final byte[]data;
    private final int offset;
    private static final String TAG = "DVBString";
    public DVBString(final byte[] data, final int offset) {
        super();
        this.data = data;
        this.offset = offset;
    }

    /**
     * Create a DVBString where the length is explicitly specified. Most of the usage first byte of data array islength,
     * but sometimes (at end of descriptor) this is not needed because it can be infered differently.
     * In those cases use this constructor
     *
     * @param dataIn
     * @param offset
     * @param len
     */
    public DVBString(final byte[] dataIn, final int offset, int len) {
        super();
        if(len>255) {
            throw new RuntimeException("DVB String can not be longer than 255 chars:" + len);
        }
        this.data = new byte[len+1];
        this.data[0] = (byte) len;
        this.offset = 0;
        System.arraycopy(dataIn, offset, this.data, 1, len);

    }


    /**
     * return plain text string representation where control chars have been removed
     */
    @Override
    public String toString(){
        return getString(data,this.getOffset()+1, this.getLength());
    }


    /**
     * @return string representation where control chars are present
     */
    public String toRawString() {
        return getCharDecodedStringWithControls(data,this.getOffset()+1, this.getLength());
    }

    /**
     * If DVBString has no explicit charset defined in first byte(s), use parameter charSet as encoding.
     *
     * @param defaultCharSet when null use normal "default (ISO 6937, latin)" encoding
     */
    public String toString(Charset defaultCharSet) {
        if((getCharSet()!=null) || (defaultCharSet == null)){
            return toString();
        }
        return new String(data, this.getOffset() + 1, this.getLength(), defaultCharSet);
    }

    public Charset getCharSet(){
        return Utils.getCharSet(data, this.getOffset()+1, this.getLength());
    }

    public String getEncodingString(){

        // empty string has no encoding
        if(getLength()==0){
            return "-";
        }

        final int fb = toUnsignedInt(data[offset+1]);
        if(0x20<=fb)
        {
            return "default (ISO 6937, latin)";
        }else if((0x01<=fb)&&(fb<=0x1F)){
            switch (fb) {
                case 0x01:
                    return "ISO/IEC 8859-5";
                case 0x02:
                    return "ISO/IEC 8859-6";
                case 0x03:
                    return "ISO/IEC 8859-7";
                case 0x04:
                    return "ISO/IEC 8859-8";
                case 0x05:
                    return "ISO/IEC 8859-9";
                case 0x06:
                    return "ISO/IEC 8859-10";
                case 0x07:
                    return "ISO/IEC 8859-11";
                case 0x08:
                    return "ISO/IEC 8859-12";
                case 0x09:
                    return "ISO/IEC 8859-13";
                case 0x0A:
                    return "ISO/IEC 8859-14";
                case 0x0B:
                    return "ISO/IEC 8859-15";
                case 0x10:
                    if(data[offset+2]==0x0){
                        return "ISO/IEC 8859-"+data[offset+3];

                    }
                    return "Illegal value";
                case 0x11:
                    return "ISO/IEC 10646-1";
                case 0x12:
                    return "KSX1001-2004";
                case 0x13:
                    return "GB-2312-1980";
                case 0x14:
                    return "Big5 subset of ISO/IEC 10646-1";
                case 0x15:
                    return "UTF-8 encoding of ISO/IEC 10646-1";
                case 0x1F:
                    return "Described by encoding_type_id;"+data[offset+2];
                default:
                    return "reserved for future use";
            }

        }
        return "illegal value";
    }




    public int getLength() {
        return getInt(data, offset, 1, MASK_8BITS);
    }



    public int getOffset() {
        return offset;
    }


    public byte[] getData() {
        return data;
    }
}
