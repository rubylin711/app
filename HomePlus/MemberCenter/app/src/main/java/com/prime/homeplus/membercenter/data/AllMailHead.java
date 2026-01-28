package com.prime.homeplus.membercenter.data;

import java.util.Date;

public class AllMailHead {

    private String mail_id;
    private int createTime;
    private int importance;
    private String mailHead;
    private int newEmail;
    private String mailType;

    public AllMailHead(String mail_id, int createTime, int importance, String mailHead, int newEmail, String mailType) {
        this.mail_id = mail_id;
        this.createTime = createTime;
        this.importance = importance;
        this.mailHead = mailHead;
        this.newEmail = newEmail;
        this.mailType = mailType;
    }

    public String getMail_id() {
        return mail_id;
    }

    public void setMail_id(String mail_id) {
        this.mail_id = mail_id;
    }

    public long getCreateTime() {
        Date date = new Date(createTime * 1000l);
        return date.getTime();
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

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }
}
