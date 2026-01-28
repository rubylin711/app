package com.prime.dtv.service.subtitle;

public class SubtitleDescriptor {

    private int mSubtitlePid;

    private int mSubtitleType;

    private int[] mLanguageCode;

    private int mCompositionPageId;

    private int mAncillaryPageId;

    public int getSubtitlePid() {
        return mSubtitlePid;
    }

    public void setSubtitlePid(int subtitlePid) {
        this.mSubtitlePid = subtitlePid;
    }

    public int getSubtitleType() {
        return mSubtitleType;
    }

    public void setSubtitleType(int subtitleType) {
        this.mSubtitleType = subtitleType;
    }

    public int[] getLanguageCode() {
        return mLanguageCode;
    }

    public void setLanguageCode(int[] languageCode) {
        this.mLanguageCode = languageCode;
    }

    public int getCompositionPageId() {
        return mCompositionPageId;
    }

    public void setCompositionPageId(int compositionPageId) {
        this.mCompositionPageId = compositionPageId;
    }

    public int getAncillaryPageId() {
        return mAncillaryPageId;
    }

    public void setAncillaryPageId(int ancillaryPageId) {
        this.mAncillaryPageId = ancillaryPageId;
    }
}
