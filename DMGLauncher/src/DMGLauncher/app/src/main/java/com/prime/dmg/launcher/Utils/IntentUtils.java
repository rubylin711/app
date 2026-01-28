package com.prime.dmg.launcher.Utils;

import android.content.Intent;
import android.util.Log;

import java.net.URISyntaxException;

public class IntentUtils extends Intent {

    static String TAG = "IntentUtils";

    public static Intent get_intent(String intentUri) {
        Intent intent = null;
        try {
            intent = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "get_intent: " + e);
        }
        return intent;
    }
}
