package com.prime.sysglob;

import com.prime.sysdata.ProgramInfo;
import com.prime.sysdata.SimpleChannel;

import java.util.List;

/**
 * Created by ethan_lin on 2017/11/20.
 */

public interface ProgramInfoFunc {

    public abstract List<ProgramInfo> GetProgramInfoList(int type);
    public abstract List<ProgramInfo> GetProgramInfoList(int type, String sortBy);
    public abstract ProgramInfo GetProgramByChnum(int chnum, int type);
    public abstract ProgramInfo GetProgramByLcn(int lcn, int type);
    public abstract ProgramInfo GetProgramByTripletId(int s_id, int ts_id, int on_id);
    public abstract ProgramInfo GetProgramByChannelId(int channelId);
    public abstract SimpleChannel GetSimpleProgramByChannelId(int channelId);
    public abstract List<SimpleChannel> GetSimpleProgramList(int type);
    public abstract void Save(ProgramInfo pProgram);
    public abstract void Save(List<SimpleChannel> pPrograms,int type);
    public abstract void Save(List<ProgramInfo> pPrograms);
    public abstract void Delete(int sid, int tsid, int onid);
    public abstract void Delete(int channelId);
    public abstract void DeleteAll(int type);
}
