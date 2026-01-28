package com.prime.datastructure.CommuincateInterface;

import com.prime.datastructure.utils.TVMessage;

interface IPrimeDtvServiceCallback {
    void onMessage(in TVMessage msg);
}