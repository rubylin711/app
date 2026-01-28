package com.prime.datastructure.sysdata;

public class MailSetting {

    private boolean g_coupon;
    private boolean g_news;
    private boolean g_popular;
    private boolean g_service;
    private boolean g_shopping;

    public MailSetting(boolean shopping, boolean news, boolean popular, boolean coupon, boolean service) {
        this.g_shopping = shopping;
        this.g_news = news;
        this.g_popular = popular;
        this.g_coupon = coupon;
        this.g_service = service;
    }

    public void set_shopping(boolean status) {
        this.g_shopping = status;
    }

    public boolean get_shopping() {
        return this.g_shopping;
    }

    public void set_news(boolean status) {
        this.g_news = status;
    }

    public boolean get_news() {
        return this.g_news;
    }

    public void set_popular(boolean status) {
        this.g_popular = status;
    }

    public boolean get_popular() {
        return this.g_popular;
    }

    public void set_coupon(boolean status) {
        this.g_coupon = status;
    }

    public boolean get_coupon() {
        return this.g_coupon;
    }

    public void set_service(boolean status) {
        this.g_service = status;
    }

    public boolean get_service() {
        return this.g_service;
    }

    public String to_string() {
        StringBuilder sb = new StringBuilder();
        sb.append("shopping:" + get_shopping());
        sb.append(",news:" + get_news());
        sb.append(",popular:" + get_popular());
        sb.append(",coupon:" + get_coupon());
        sb.append(",service:" + get_service());
        return sb.toString();
    }
}
