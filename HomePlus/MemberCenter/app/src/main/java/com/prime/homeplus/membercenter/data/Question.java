package com.prime.homeplus.membercenter.data;

import com.google.gson.annotations.SerializedName;

public class Question {
    @SerializedName(value = "title")
    private String title;

    @SerializedName(value = "content")
    private String content;

    public String getTitle(){
        return title;
    }

    public String getContent(){
        return content;
    }

}
