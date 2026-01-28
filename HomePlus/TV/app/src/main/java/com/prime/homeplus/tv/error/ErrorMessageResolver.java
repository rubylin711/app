package com.prime.homeplus.tv.error;

import android.content.Context;

import androidx.annotation.StringRes;

import com.prime.homeplus.tv.R;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessageResolver {

    // Map error codes to string resource IDs
    private static final Map<String, Integer> errorCodeMap = new HashMap<>();

    static {
        errorCodeMap.put("E000", R.string.error_e000);
        errorCodeMap.put("E002", R.string.error_e002);
        errorCodeMap.put("E005", R.string.error_e005);
        errorCodeMap.put("E006", R.string.error_e006);
        errorCodeMap.put("E007", R.string.error_e007);
        errorCodeMap.put("E009", R.string.error_e009);
        errorCodeMap.put("E010", R.string.error_e010);
        errorCodeMap.put("E014", R.string.error_e014);
        errorCodeMap.put("E015", R.string.error_e015);
        errorCodeMap.put("E016", R.string.error_e016);
        errorCodeMap.put("E017", R.string.error_e017);
        errorCodeMap.put("E018", R.string.error_e018);
        errorCodeMap.put("E019", R.string.error_e019);
        errorCodeMap.put("E020", R.string.error_e020);
        errorCodeMap.put("E021", R.string.error_e021);
        errorCodeMap.put("E022", R.string.error_e022);
        errorCodeMap.put("E023", R.string.error_e023);
        errorCodeMap.put("E024", R.string.error_e024);
        errorCodeMap.put("E025", R.string.error_e025);
        errorCodeMap.put("E026", R.string.error_e026);
        errorCodeMap.put("E027", R.string.error_e027);
        errorCodeMap.put("E028", R.string.error_e028);
        errorCodeMap.put("E029", R.string.error_e029);
        errorCodeMap.put("E030", R.string.error_e030);
        errorCodeMap.put("E200", R.string.error_e200);
        errorCodeMap.put("E201", R.string.error_e201);
        errorCodeMap.put("E202", R.string.error_e202);
        errorCodeMap.put("E300", R.string.error_e300);
        errorCodeMap.put("E911", R.string.error_e911);
        errorCodeMap.put("E999", R.string.error_e999);
    }

    /**
     * Returns the localized error message for the given error code.
     * If the error code does not exist, returns a default unknown error message.
     *
     * @param context   Context to access resources
     * @param errorCode Error code string (e.g. "E000")
     * @return Corresponding error message string
     */
    public static String getErrorMessage(Context context, String errorCode) {
        @StringRes Integer resId = errorCodeMap.get(errorCode);
        if (resId != null) {
            return context.getString(resId);
        } else {
            return context.getString(R.string.error_unknown, errorCode);
        }
    }
}

