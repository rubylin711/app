package com.prime.dtv.service.cnsad.adparse;

import androidx.annotation.Nullable;

/** UI action mapping (mainly for EPG OK/BLUE). */
public final class ActionDef {
    @Nullable private final String type;
    @Nullable private final String code;
    @Nullable private final String value;
    @Nullable private final String parameter;

    public ActionDef(@Nullable String type, @Nullable String code, @Nullable String value, @Nullable String parameter) {
        this.type = type; this.code = code; this.value = value; this.parameter = parameter;
    }

    @Nullable public String getType() { return type; }
    @Nullable public String getCode() { return code; }
    @Nullable public String getValue() { return value; }
    @Nullable public String getParameter() { return parameter; }
}
