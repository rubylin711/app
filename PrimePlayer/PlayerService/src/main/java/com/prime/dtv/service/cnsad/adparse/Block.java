package com.prime.dtv.service.cnsad.adparse;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A named block in data.xml: portal | epg | miniepg | channels */
public final class Block {
    private final BlockType name;
    private final PlayMode playMode;
    @Nullable private final Integer durationSec;
    private final List<Asset> assets;
    @Nullable private final ActionDef action;

    // ★ 新增：對應 <child name="...">
    @Nullable private final String childName;

    // 舊建構式 → 委派到新建構式（childName=null）
    public Block(BlockType name, PlayMode playMode, @Nullable Integer durationSec,
                 List<Asset> assets, @Nullable ActionDef action) {
        this(name, playMode, durationSec, assets, action, null);
    }

    // ★ 新建構式（含 childName）
    public Block(BlockType name, PlayMode playMode, @Nullable Integer durationSec,
                 List<Asset> assets, @Nullable ActionDef action, @Nullable String childName) {
        if (name == null) throw new IllegalArgumentException("block@name is null");
        this.name = name;
        this.playMode = (playMode == null) ? new PlayMode(PlayModeType.INTERVAL, null, null) : playMode;
        this.durationSec = durationSec;
        this.assets = (assets == null) ? Collections.<Asset>emptyList() : Collections.unmodifiableList(new ArrayList<>(assets));
        this.action = action;
        this.childName = childName;
    }

    public BlockType getName() { return name; }
    public PlayMode getPlayMode() { return playMode; }
    @Nullable public Integer getDurationSec() { return durationSec; }
    public List<Asset> getAssets() { return assets; }
    @Nullable public ActionDef getAction() { return action; }

    // ★ 新增 getter
    @Nullable public String getChildName() { return childName; }
}
