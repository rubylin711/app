// ITVCallback.aidl
package com.prime.utils;

// Declare any non-default types here with import statements
import com.prime.utils.TVMessage;

interface ITVCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onMessage(in TVMessage message);
}
