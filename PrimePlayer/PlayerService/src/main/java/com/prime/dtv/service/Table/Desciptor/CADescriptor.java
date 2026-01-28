package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;


public class CADescriptor extends DescBase{

    private int caSystemID;
    private int caPID;
    private byte[] privateDataByte;

    public CADescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        caSystemID = getInt(data,2,2,0xFFFF);
        caPID = getInt(data,4,2,0x1FFF);
        // Arrays.copyOfRange(src, startIndex, endIndex) also works
        //privateDataByte = Arrays.copyOfRange(data, 6, Length+2);
        if((Length - 4) > 0){
            privateDataByte = new byte[Length - 4];
            System.arraycopy(data, 6, privateDataByte, 0, Length-4);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "caSystemID="+caSystemID;
    }


    public int getCaPID() {
        return caPID;
    }

    public int getCaSystemID() {
        return caSystemID;
    }

    public byte[] getPrivateDataByte() {
        if (privateDataByte == null) {
            return new byte[0];
        }
        else {
            return privateDataByte;
        }
    }

}