package com.prime.dtvplayer.TestData.sysglob;

import com.prime.dtvplayer.Sysdata.SatInfo;

import java.util.List;

/**
 * Created by ethan_lin on 2017/11/20.
 */

public interface SatInfoFunc {
    public abstract List<SatInfo> GetSatinfoList();
    public abstract SatInfo GetSatInfo(int satId);
    public abstract void Save(SatInfo pSat);
    public abstract void Save(List<SatInfo> pSats);
    public abstract void Delete(int satId);
    public abstract void Add(SatInfo pSat);
    public abstract void Update(SatInfo pSat);
}
