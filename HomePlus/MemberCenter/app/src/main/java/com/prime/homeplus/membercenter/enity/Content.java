package com.prime.homeplus.membercenter.enity;

public class Content {
    private String imageUrl;
    private Intent intent;
    private String text;

    public Content(String imageUrl, Intent intent, String text) {
        this.imageUrl = imageUrl;
        this.intent = intent;
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
