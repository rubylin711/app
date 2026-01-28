package com.prime.dtvservice.Interface;

import com.prime.dtv.PrimeDtv;

public abstract class BaseModule {
    private PrimeDtv primeDtv;

    public BaseModule(PrimeDtv dtv) {
        primeDtv = dtv;
    }

    public PrimeDtv getPrimeDtv() {
        return primeDtv;
    }
}
