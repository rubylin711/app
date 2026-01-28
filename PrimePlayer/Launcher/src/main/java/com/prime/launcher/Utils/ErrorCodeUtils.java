package com.prime.launcher.Utils;

import com.prime.launcher.R;

import android.content.Context;

public class ErrorCodeUtils {
    public static String getErrorMessage(Context context, int errorCode) {
        String message;
        switch (errorCode) {
            case com.prime.datastructure.utils.ErrorCode.ERROR_E010:
                message = context.getString(R.string.error_e010);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E200:
                message = context.getString(R.string.error_e200);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E213:
                message = context.getString(R.string.error_e213);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E301:
                message = context.getString(R.string.error_e301);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E501:
                message = context.getString(R.string.error_e501);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E502:
                message = context.getString(R.string.error_e502);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E503:
                message = context.getString(R.string.error_e503);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E504:
                message = context.getString(R.string.error_e504);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E505:
                message = context.getString(R.string.error_e505);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E506:
                message = context.getString(R.string.error_e506);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E507:
                message = context.getString(R.string.error_e507);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E508:
                message = context.getString(R.string.error_e508);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E510:
                message = context.getString(R.string.error_e510);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E511:
                message = context.getString(R.string.error_e511);
                break;
            case com.prime.datastructure.utils.ErrorCode.ERROR_E512:
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
