package com.prime.dtv.service.cnsad.adparse;

import androidx.annotation.Nullable;

/** Asset item node. */
public final class Asset {
    private final AssetType type;
    private final String value;
    @Nullable private final Integer durationSec;
    @Nullable private final String footage;

    // ★ 新增：資產層級 action（可為 null）
    @Nullable private ActionDef action;

    public Asset(AssetType type, String value, @Nullable Integer durationSec, @Nullable String footage) {
        if (value == null) throw new IllegalArgumentException("asset@value is null");
        this.type = (type == null) ? AssetType.IMAGE : type;
        this.value = value;
        this.durationSec = durationSec;
        this.footage = footage;
    }

    public AssetType getType() { return type; }
    public String getValue() { return value; }
    @Nullable public Integer getDurationSec() { return durationSec; }
    @Nullable public String getFootage() { return footage; }

    // ★ 新增 getter / setter
    @Nullable public ActionDef getAction() { return action; }
    public void setAction(@Nullable ActionDef action) { this.action = action; }
}
