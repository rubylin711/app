package com.prime.homeplus.settings.sms.msg;

// TODO
public class ErrorMsg {
    public static String getErrorMsg(int errorCode) {
        String _errorMsg = "";

        switch (errorCode) {
            default:
                _errorMsg = "None";
        }

        return _errorMsg;
    }
}
