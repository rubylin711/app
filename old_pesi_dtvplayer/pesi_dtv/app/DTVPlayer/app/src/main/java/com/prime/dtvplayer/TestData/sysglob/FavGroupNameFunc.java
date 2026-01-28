package com.prime.dtvplayer.TestData.sysglob;

/**
 * Created by gary_hsu on 2017/11/22.
 */

public interface FavGroupNameFunc {
    public abstract String GetFavGroupName(int favMode);
    public abstract void Save(int favMode, String name);
}
