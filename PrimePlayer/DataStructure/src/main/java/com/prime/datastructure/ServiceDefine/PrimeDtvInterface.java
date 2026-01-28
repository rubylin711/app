package com.prime.datastructure.ServiceDefine;

import com.prime.datastructure.utils.TVMessage;

public class PrimeDtvInterface {
    public interface DTVCallback {
        void onMessage(TVMessage msg);
    }
}
