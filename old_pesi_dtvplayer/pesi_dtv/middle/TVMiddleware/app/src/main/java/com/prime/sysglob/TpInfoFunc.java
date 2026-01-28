package com.prime.sysglob;

import com.prime.sysdata.TpInfo;

import java.util.List;

/**
 * Created by ethan_lin on 2017/11/20.
 */

public interface TpInfoFunc {
    public abstract List<TpInfo> GetTpInfoList(int tuner_type);
    public abstract List<TpInfo> GetTpInfoListBySatId(int satId);
    public abstract TpInfo GetTpInfo(int tp_id);
    public abstract void Save(TpInfo pTp);
    public abstract void Save(List<TpInfo> pTps);
    public abstract void Delete(int tpId);
    public abstract void Add(TpInfo pTp);
    public abstract void Update(TpInfo pTp);
}
