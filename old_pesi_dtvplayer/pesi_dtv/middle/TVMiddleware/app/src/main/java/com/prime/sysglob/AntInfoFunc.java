package com.prime.sysglob;

import com.prime.sysdata.AntInfo;

import java.util.List;

/**
 * Created by ethan_lin on 2017/11/20.
 */

public interface AntInfoFunc {
    public abstract List<AntInfo> GetAntInfoList();
    public abstract AntInfo GetAntInfo(int antId);
    public abstract void Save(AntInfo pant);
    public abstract void Save(List<AntInfo> pants);
    public abstract void Delete(int antId);
}
