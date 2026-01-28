package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class TvMailHead {

    private String actionId;
    private int createTime;
    private int importance;
    private String mailHead;
    private int newEmail;

    public TvMailHead(String actionId, int createTime, int importance, String mailHead, int newEmail) {
        this.actionId = actionId;
        this.createTime = createTime;
        this.importance = importance;
        this.mailHead = mailHead;
        this.newEmail = newEmail;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public String getMailHead() {
        return mailHead;
    }

    public void setMailHead(String mailHead) {
        this.mailHead = mailHead;
    }

    public int getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(int newEmail) {
        this.newEmail = newEmail;
    }
}
