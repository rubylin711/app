package com.prime.dtv.service.cnsad.adparse;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Root model for data.xml */
public final class AdModel {
    private final long entryEpochSec;    // <entry time="...">
    @Nullable private final String readFolder;     // <readfolder value="..."/>
    private final List<Block> blocks;

    public AdModel(long entryEpochSec, @Nullable String readFolder, List<Block> blocks) {
        if (entryEpochSec <= 0) throw new IllegalArgumentException("entry@time missing or invalid");
        this.entryEpochSec = entryEpochSec;
        this.readFolder = readFolder;
        this.blocks = (blocks == null) ? Collections.<Block>emptyList() : Collections.unmodifiableList(new ArrayList<>(blocks));
    }

    public long getEntryEpochSec() { return entryEpochSec; }
    @Nullable public String getReadFolder() { return readFolder; }
    public List<Block> getBlocks() { return blocks; }
}
