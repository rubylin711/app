package com.prime.datastructure.sysdata;

/**
 * Created by eric_lin on 2018/5/23.
 */

public class MasterPinCode {
    private static final int mMasterPinCode = 3141;

    public int getmMasterPinCode() {
        return mMasterPinCode;
    }

    public static boolean checkMasterPinCode(int pin_code){
        if(pin_code == mMasterPinCode)
            return true;
        else
            return false;
    }

    public static boolean checkMasterPinCode(String pin_code){
        if(pin_code.equals(String.format("%4d", mMasterPinCode)))
            return true;
        else
            return false;
    }


    public static boolean checkPinCode(String pinCode, String inputPinCode){
        if(pinCode.equals(inputPinCode) || checkMasterPinCode(inputPinCode))
            return true;
        else
            return false;
    }

    public static boolean checkPinCode(int pinCode, int inputPinCode){
        if(pinCode == inputPinCode || checkMasterPinCode(inputPinCode))
            return true;
        else
            return false;
    }
}
