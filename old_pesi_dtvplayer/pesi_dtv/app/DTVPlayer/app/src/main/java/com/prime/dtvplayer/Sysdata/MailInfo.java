package com.prime.dtvplayer.Sysdata;

public class MailInfo {
    public static final int MAILUNREAD = 0;
    public static final int MAILREAD = 1;
    public static final int FORCE = 0;
    public static final int NORMAL = 1;

    private String MailID = "";
    private String MailMsg = "";
    private int MailRead = MAILUNREAD;

    public void setMailID(String id) {
        MailID = id;
    }
    public String getMailID() {
        return MailID;
    }

    public void setMailMsg(String msg) {
        MailMsg = msg;
    }
    public String getMailMsg() {
        return MailMsg;
    }

    public void setMailRead(int read) {
        MailRead = read;
    }
    public int getMailRead() {
        return MailRead;
    }
}
