package com.prime.sysglob;

import com.prime.sysdata.FavInfo;

import java.util.List;

/**
 * Created by gary_hsu on 2017/11/21.
 */

public interface FavInfoFunc {
    public abstract List<FavInfo> GetFavInfoList(int favMode);
    public abstract FavInfo GetFavInfo(int favMode,int favNum);
    public abstract void Save(FavInfo favInfo);
    public abstract void Save(List<FavInfo> favInfo);
    public abstract void Delete(int favMode, int favNum);
    public abstract void DeleteAll(int favMode);
}
