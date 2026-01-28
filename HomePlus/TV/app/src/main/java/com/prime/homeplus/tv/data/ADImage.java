package com.prime.homeplus.tv.data;

public class ADImage {
    public String assetValue;
    public String actionType;
    public String actionValue;

    public String getAssetValue() {
        return assetValue;
    }

    public void setAssetValue(String assetValue) {
        this.assetValue = assetValue;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    @Override
    public String toString() {
        return "ADImage{" +
                "assetValue='" + assetValue + '\'' +
                ", actionType='" + actionType + '\'' +
                ", actionValue='" + actionValue + '\'' +
                '}';
    }
}
