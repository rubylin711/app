package com.prime.homeplus.membercenter.enity;

import java.util.ArrayList;

public class TvMailResponse {
    private Integer code;
    private ArrayList<Data> data;
    private String message;
    private int messageHeartbeatTime;
    private Integer timeCost;
    private Integer timeStamp;

    public TvMailResponse(Integer code, ArrayList<Data> data, String message, int messageHeartbeatTime, Integer timeCost, Integer timeStamp) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.messageHeartbeatTime = messageHeartbeatTime;
        this.timeCost = timeCost;
        this.timeStamp = timeStamp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ArrayList<Data> getData() {
        return data;
    }

    public void setData(ArrayList<Data> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageHeartbeatTime() {
        return messageHeartbeatTime;
    }

    public void setMessageHeartbeatTime(int messageHeartbeatTime) {
        this.messageHeartbeatTime = messageHeartbeatTime;
    }

    public Integer getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Integer timeCost) {
        this.timeCost = timeCost;
    }

    public Integer getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Integer timeStamp) {
        this.timeStamp = timeStamp;
    }
}

