package com.prime.dtv.service.Table;

import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PatData extends TableData {
    public class Program {
        private int mProgramNumber;
        private int mProgramMapPid;

        public Program(final int program_number, final int program_map_PID) {
            super();
            this.mProgramNumber = program_number;
            this.mProgramMapPid = program_map_PID;
        }
    }
    public PatData(){

    }
    public PatData(int program_number, int program_map_PID){
        final ArrayList<Program> r = new ArrayList<>();
        final Program c = new Program(program_number, program_map_PID);
        r.add(c);
        programs = r;
    }
    private List<Program> programs;

    public List<Program> getPrograms() {
        return programs;
    }

    public int getProgramTotalNum() {
        return programs.size();
    }
    public int getProgramNumber(int index) {
        return programs.get(index).mProgramNumber;
    }

    public void setProgramNumber(int programNumber) {
        //this.mprogramNumber = programNumber;
    }

    public int getProgramMapPid(int index) {
        return programs.get(index).mProgramMapPid;
    }

    public void setProgramMapPid(int programMapPid) {
        //this.mProgramMapPid = programMapPid;
    }
    private List<Program> buildProgramList(byte[] data, int offset, int programInfoLength) {
        final ArrayList<Program> r = new ArrayList<>();
        int i = 0;
        while (i < programInfoLength) {
            final Program c = new Program((toUnsignedInt(data[offset+i]) << 8) + toUnsignedInt(data[offset + i + 1]),
                    ((toUnsignedInt(data[offset + i + 2]) & 0x1f) << 8) + toUnsignedInt(data[offset + i + 3]));
            i += 4;
            r.add(c);
        }
        return r;
    }

    @Override
    public void parsing(byte[] data, int lens) {
        try {
            programs = buildProgramList(data, 8, lens - 3 - 9);
        } catch (Exception e) {
            //Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }
}
