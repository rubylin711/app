package com.prime.homeplus.membercenter.enity;

public class Data {
    private Content content;
    private Long lauMessagePublishId;
    private Integer level;
    private int scheduleTime;
    private String title;
    private int newMail;

    public Data(Content content, Long lauMessagePublishId, Integer level, int scheduleTime, String title, int newMail) {
        this.content = content;
        this.lauMessagePublishId = lauMessagePublishId;
        this.level = level;
        this.scheduleTime = scheduleTime;
        this.title = title;
        this.newMail = newMail;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Long getLauMessagePublishId() {
        return lauMessagePublishId;
    }

    public void setLauMessagePublishId(Long lauMessagePublishId) {
        this.lauMessagePublishId = lauMessagePublishId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public int getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(int scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNewMail() {
        return newMail;
    }

    public void setNewMail(int newMail) {
        this.newMail = newMail;
    }
}
