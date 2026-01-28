package com.prime.dtv.service.Util;

import android.content.Context;

import com.prime.dmg.launcher.R;

public class ErrorCodeUtil {
    public static final int ERROR_E010 = 10;
    public static final int ERROR_E200 = 200;
    public static final int ERROR_E213 = 213;
    public static final int ERROR_E301 = 301;
    public static final int ERROR_E501 = 501;
    public static final int ERROR_E502 = 502;
    public static final int ERROR_E503 = 503;
    public static final int ERROR_E504 = 504;
    public static final int ERROR_E505 = 505;
    public static final int ERROR_E506 = 506;
    public static final int ERROR_E507 = 507;
    public static final int ERROR_E508 = 508;
    public static final int ERROR_E510 = 510;
    public static final int ERROR_E511 = 511;
    public static final int ERROR_E512 = 512;


    public static String getErrorMessage(Context context, int errorCode) {
        String message;
        switch (errorCode) {
            case ERROR_E010:
                message = context.getString(R.string.error_e010);
                break;
            case ERROR_E200:
                message = context.getString(R.string.error_e200);
                break;
            case ERROR_E213:
                message = context.getString(R.string.error_e213);
                break;
            case ERROR_E301:
                message = context.getString(R.string.error_e301);
                break;
            case ERROR_E501:
                message = context.getString(R.string.error_e501);
                break;
            case ERROR_E502:
                message = context.getString(R.string.error_e502);
                break;
            case ERROR_E503:
                message = context.getString(R.string.error_e503);
                break;
            case ERROR_E504:
                message = context.getString(R.string.error_e504);
                break;
            case ERROR_E505:
                message = context.getString(R.string.error_e505);
                break;
            case ERROR_E506:
                message = context.getString(R.string.error_e506);
                break;
            case ERROR_E507:
                message = context.getString(R.string.error_e507);
                break;
            case ERROR_E508:
                message = context.getString(R.string.error_e508);
                break;
            case ERROR_E510:
                message = context.getString(R.string.error_e510);
                break;
            case ERROR_E511:
                message = context.getString(R.string.error_e511);
                break;
            case ERROR_E512:
                message = context.getString(R.string.error_e512);
                break;
            default:
                message = "";
                break;
        }

        return message;
    }

    public static String getErrorMessage(Context context, int errorCode, String defaultMessage) {
        String message = getErrorMessage(context, errorCode);
        if (message.isEmpty()) {
            message = defaultMessage;
        }

        return message;
    }
}
