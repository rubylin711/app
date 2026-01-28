package com.prime.sysdata;

/**
 * Created by gary_hsu on 2017/11/15.
 */

public class FavGroupName {
    public static final String TAG = "FavGroupName";
    public static final int TV_FAV_NUM = 6;
    public static final int RADIO_FAV_NUM = 2;

    private int GroupType;
    private String GroupName;

    public FavGroupName(){//eric lin add
    }

    public FavGroupName(int groupType, String groupName){//eric lin add
        GroupType = groupType;
        GroupName = groupName;
    }

    public String ToString(){
        return "[GROUP_TYPE  " + GroupType + "GroupName : " + GroupName + "]";
    }

    public int getGroupType() {
        return GroupType;
    }

    public void setGroupType(int groupType) {
        GroupType = groupType;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }
}
