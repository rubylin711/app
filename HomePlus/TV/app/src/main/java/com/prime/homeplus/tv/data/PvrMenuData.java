package com.prime.homeplus.tv.data;

public class PvrMenuData {
    public enum PvrMenuState {
        RECORDED_LIST,
        SCHEDULED_LIST,
        RECORDING_SETUP_LIST
    }

    private PvrMenuState pvrMenuState;
    private int iconResId;
    private String menuName;

    public PvrMenuData(PvrMenuState pvrMenuState, int iconResId, String menuName) {
        this.pvrMenuState = pvrMenuState;
        this.iconResId = iconResId;
        this.menuName = menuName;
    }

    public PvrMenuState getPvrMenuState() {
        return pvrMenuState;
    }

    public void setPvrMenuState(PvrMenuState pvrMenuState) {
        this.pvrMenuState = pvrMenuState;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    @Override
    public String toString() {
        return "PvrMenuData{" +
                "iconResId='" + iconResId + '\'' +
                ", menuName='" + menuName + '\'' +
                '}';
    }
}


