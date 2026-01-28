package com.prime.homeplus.vbm;

import android.util.Log;

public class VBMData {
    private static final String TAG = "HOMEPLUS_VBM-VBMDATA";
    private String StbId = "";
    private String AgentId = "";
    private String EventType = "";
    private String EventTimeStamp = "";
    private String Value_0 = "N/A";
    private String Value_1 = "N/A";
    private String Value_2 = "N/A";
    private String Value_3 = "N/A";
    private String Value_4 = "N/A";
    private String Value_5 = "N/A";
    private String Value_6 = "N/A";
    private String Value_7 = "N/A";
    private String Value_8 = "N/A";
    private String Value_9 = "N/A";

    VBMData() {

    }

    VBMData(String[] arrayVbmdata) {
        //Log.d(TAG, "arrayVbmdata size: " + arrayVbmdata.length);
        if (arrayVbmdata.length == 14) {
            this.StbId = arrayVbmdata[0];
            this.AgentId = arrayVbmdata[1];
            this.EventType = arrayVbmdata[2];
            this.EventTimeStamp = arrayVbmdata[3];
            this.Value_0 = arrayVbmdata[4];
            this.Value_1 = arrayVbmdata[5];
            this.Value_2 = arrayVbmdata[6];
            this.Value_3 = arrayVbmdata[7];
            this.Value_4 = arrayVbmdata[8];
            this.Value_5 = arrayVbmdata[9];
            this.Value_6 = arrayVbmdata[10];
            this.Value_7 = arrayVbmdata[11];
            this.Value_8 = arrayVbmdata[12];
            this.Value_9 = arrayVbmdata[13];
        } else {
            Log.d(TAG, "arrayVbmdata.length not 14");
        }
    }

    VBMData(String StbId, String AgentId, String EventType, String EventTimeStamp, String Value_0,
            String Value_1, String Value_2, String Value_3, String Value_4, String Value_5,
            String Value_6, String Value_7, String Value_8, String Value_9) {
        this.StbId = StbId;
        this.AgentId = AgentId;
        this.EventType = EventType;
        this.EventTimeStamp = EventTimeStamp;
        this.Value_0 = Value_0;
        this.Value_1 = Value_1;
        this.Value_2 = Value_2;
        this.Value_3 = Value_3;
        this.Value_4 = Value_4;
        this.Value_5 = Value_5;
        this.Value_6 = Value_6;
        this.Value_7 = Value_7;
        this.Value_8 = Value_8;
        this.Value_9 = Value_9;
    }

    public VBMData(String StbId, String AgentId, String EventType, String EventTimeStamp, String Value_0,
                   String Value_1) {
        this.StbId = StbId;
        this.AgentId = AgentId;
        this.EventType = EventType;
        this.EventTimeStamp = EventTimeStamp;
        this.Value_0 = Value_0;
        this.Value_1 = Value_1;
    }

    public VBMData(String StbId, String AgentId, String EventType, String EventTimeStamp, String Value_0,
                   String Value_1, String Value_2) {
        this.StbId = StbId;
        this.AgentId = AgentId;
        this.EventType = EventType;
        this.EventTimeStamp = EventTimeStamp;
        this.Value_0 = Value_0;
        this.Value_1 = Value_1;
        this.Value_2 = Value_2;
    }

    public VBMData(String StbId, String AgentId, String EventType, String EventTimeStamp, String Value_0,
                   String Value_1, String Value_2, String Value_3) {
        this.StbId = StbId;
        this.AgentId = AgentId;
        this.EventType = EventType;
        this.EventTimeStamp = EventTimeStamp;
        this.Value_0 = Value_0;
        this.Value_1 = Value_1;
        this.Value_2 = Value_2;
        this.Value_3 = Value_3;
    }

    public VBMData(String StbId, String AgentId, String EventType, String EventTimeStamp, String Value_0,
                   String Value_1, String Value_2, String Value_3, String Value_4) {
        this.StbId = StbId;
        this.AgentId = AgentId;
        this.EventType = EventType;
        this.EventTimeStamp = EventTimeStamp;
        this.Value_0 = Value_0;
        this.Value_1 = Value_1;
        this.Value_2 = Value_2;
        this.Value_3 = Value_3;
        this.Value_4 = Value_4;
    }
    /**
     * [新增] 配合 MainService.addRecord 使用的建構子
     * 接收可變長度參數 (Varargs) 或 String 陣列，並依序填入 Value_0 ~ Value_9
     */
    public VBMData(String StbId, String AgentId, String EventType, String EventTimeStamp, String[] values) {
        this.StbId = StbId;
        this.AgentId = AgentId;
        this.EventType = EventType;
        this.EventTimeStamp = EventTimeStamp;

        // 依序將陣列內容填入成員變數
        // 欄位預設值已為 "N/A"，若陣列長度不足則維持預設值
        if (values != null) {
            int len = values.length;
            if (len > 0 && values[0] != null) this.Value_0 = values[0];
            if (len > 1 && values[1] != null) this.Value_1 = values[1];
            if (len > 2 && values[2] != null) this.Value_2 = values[2];
            if (len > 3 && values[3] != null) this.Value_3 = values[3];
            if (len > 4 && values[4] != null) this.Value_4 = values[4];
            if (len > 5 && values[5] != null) this.Value_5 = values[5];
            if (len > 6 && values[6] != null) this.Value_6 = values[6];
            if (len > 7 && values[7] != null) this.Value_7 = values[7];
            if (len > 8 && values[8] != null) this.Value_8 = values[8];
            if (len > 9 && values[9] != null) this.Value_9 = values[9];
        }
    }
    public String toString() {
        return this.StbId + "," + this.AgentId + "," + this.EventType + "," + this.EventTimeStamp + "," +
                this.Value_0 + "," + this.Value_1 + "," + this.Value_2 + "," + this.Value_3 + "," +
                this.Value_4 + "," + this.Value_5 + "," + this.Value_6 + "," + this.Value_7 + "," +
                this.Value_8 + "," + this.Value_9;
    }

    public String toUploadString() {
        String out = "StbId=" + this.StbId;
        out += ",AgentId=" + this.AgentId + ",EventType=" + this.EventType + ",EventTimeStamp=" + this.EventTimeStamp;

        if (this.AgentId.equals("1")) {
            out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1;
            if (this.Value_0.equals("6")) {
                out += ",Value_2=" + this.Value_2 + ",Value_3=" + this.Value_3;
            }
        } else if (this.AgentId.equals("2")) {
            out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
            out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
            out += ",Value_6=" + this.Value_6 + ",Value_7=" + this.Value_7 + ",Value_8=" + this.Value_8;
            if (this.EventType.equals("0") || this.EventType.equals("2")) {
                out += ",Value_9=" + this.Value_9;
            }
        } else if (this.AgentId.equals("3")) {
            out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
            out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
            out += ",Value_6=" + this.Value_6 + ",Value_7=" + this.Value_7;
        } else if (this.AgentId.equals("4")) {
            out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
            out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
            out += ",Value_6=" + this.Value_6 + ",Value_7=" + this.Value_7;
        } else if (this.AgentId.equals("5")) {
            out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
            out += ",Value_3=" + this.Value_3;
        } else if (this.AgentId.equals("6")) {
            out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
            out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
            if (this.EventType.equals("0")) {
                out += ",Value_6=" + this.Value_6;
            }
        } else if (this.AgentId.equals("7")) {
            if (this.EventType.equals("0")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
                out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
                out += ",Value_6=" + this.Value_6 + ",Value_7=" + this.Value_7 + ",Value_8=" + this.Value_8;
            } else if (this.EventType.equals("1")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
                out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
                out += ",Value_6=" + this.Value_6 + ",Value_7=" + this.Value_7;
            } else if (this.EventType.equals("2")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
                out += ",Value_3=" + this.Value_3;
            } else if (this.EventType.equals("3")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
                out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4;
            } else if (this.EventType.equals("4")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
                out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
            }
        } else if (this.AgentId.equals("8")) {
            out = "";
        } else if (this.AgentId.equals("9")) {
            if (this.EventType.equals("0")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
                out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
                out += ",Value_6=" + this.Value_6 + ",Value_7=" + this.Value_7 + ",Value_8=" + this.Value_8;
            } else if (this.EventType.equals("1")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
            } else if (this.EventType.equals("2")) {
                out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
                out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4;
            }
        } else if (this.AgentId.equals("99")) {
            out += ",Value_0=" + this.Value_0;
        } else if (this.AgentId.equals("10")) {
            out += ",Value_0=" + this.Value_0 + ",Value_1=" + this.Value_1 + ",Value_2=" + this.Value_2;
            out += ",Value_3=" + this.Value_3 + ",Value_4=" + this.Value_4 + ",Value_5=" + this.Value_5;
        } else {
            out = "";
        }
        return out;
    }
}
