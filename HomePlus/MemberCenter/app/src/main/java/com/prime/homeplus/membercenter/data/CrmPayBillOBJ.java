package com.prime.homeplus.membercenter.data;

public class CrmPayBillOBJ {
    String mCrmId, mCreditNumber, mValidDate, mCrmPrepay, mCrmMediabillno, mDeviceSNo3;

    public CrmPayBillOBJ(String crmId, String creditNumber, String validDate, String crmPrepay, String crmMediabillno, String deviceSNo3) {
        mCrmId = crmId;
        mCreditNumber = creditNumber;
        mValidDate = validDate;
        mCrmPrepay = crmPrepay;
        mCrmMediabillno = crmMediabillno;
        mDeviceSNo3 = deviceSNo3;
    }

    public String getCrmId() {
        return mCrmId;
    }

    public String getCreditNumber() {
        return mCreditNumber;
    }

    public String getValidDate() {
        return mValidDate;
    }

    public String getCrmPrepay() {
        return mCrmPrepay;
    }

    public String getCrmMediabillno() {
        return mCrmMediabillno;
    }

    public String getDeviceSNo3() {
        return mDeviceSNo3;
    }

    public void setCrmId(String crmId) {
        this.mCrmId = crmId;
    }

    public void setCreditNumber(String creditNumber) {
        this.mCreditNumber = creditNumber;
    }

    public void setValidDate(String validDate) {
        this.mValidDate = validDate;
    }

    public void setCrmPrepay(String crmPrepay) {
        this.mCrmPrepay = crmPrepay;
    }

    public void setCrmMediabillno(String crmMediabillno) {
        this.mCrmMediabillno = crmMediabillno;
    }

    public void setDeviceSNo3(String deviceSNo3) {
        this.mDeviceSNo3 = deviceSNo3;
    }

}

