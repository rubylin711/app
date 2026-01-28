package com.prime.dtv.service.datamanager;

public class CNSSITableData {
    public static final String TAG = "CNSSITableData";

    public static final String CNS_TICKER_SERVICE_NAME = "CNS-TICKER";
    public static final String CNS_AD_SERVICE_NAME = "CNS-AD";

    private int TickerServiceId = 0;
    private int ADServiceId = 0;
    private int TickerPmtPid = 0;
    private int ADPmtPid = 0;
    private int TickerDSMCCPid = 0;
    private int ADDSMCCPid = 0;


    public int getTickerServiceId() {
        return TickerServiceId;
    }

    public void setTickerServiceId(int tickerServiceId) {
        TickerServiceId = tickerServiceId;
    }

    public int getADServiceId() {
        return ADServiceId;
    }

    public void setADServiceId(int ADServiceId) {
        this.ADServiceId = ADServiceId;
    }

    public int getTickerPmtPid() {
        return TickerPmtPid;
    }

    public void setTickerPmtPid(int tickerPmtPid) {
        TickerPmtPid = tickerPmtPid;
    }

    public int getADPmtPid() {
        return ADPmtPid;
    }

    public void setADPmtPid(int ADPmtPid) {
        this.ADPmtPid = ADPmtPid;
    }

    public int getTickerDSMCCPid() {
        return TickerDSMCCPid;
    }

    public void setTickerDSMCCPid(int tickerDSMCCPid) {
        TickerDSMCCPid = tickerDSMCCPid;
    }

    public int getADDSMCCPid() {
        return ADDSMCCPid;
    }

    public void setADDSMCCPid(int ADDSMCCPid) {
        this.ADDSMCCPid = ADDSMCCPid;
    }
}
