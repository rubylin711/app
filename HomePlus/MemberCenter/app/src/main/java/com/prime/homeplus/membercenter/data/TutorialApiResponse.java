package com.prime.homeplus.membercenter.data;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

public class TutorialApiResponse {
    private int code;
    private Data[] data;
    private String message;
    private int timeCost;
    private String timeStamp;

    public Data[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", data=" + java.util.Arrays.toString(data) +
                ", message='" + message + '\'' +
                ", timeCost=" + timeCost +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }

    public class Data {
        private Button[] buttonList;
        private int intervalTime;
        private Pic[] picList;

        @Override
        public String toString() {
            return "Data{" +
                    "buttonList=" + java.util.Arrays.toString(buttonList) +
                    ", intervalTime=" + intervalTime +
                    ", picList=" + java.util.Arrays.toString(picList) +
                    '}';
        }

        public Button[] getButtonList() {
            return buttonList;
        }

        public int getIntervalTime() {
            return intervalTime;
        }

        public Pic[] getPicList() {
            return picList;
        }
    }

    public class Button {
        public String buttonName;
        public String intent;
        public int orderNum;

        public String getAction() {
            String action = "";
            if (!TextUtils.isEmpty(intent)) {
                try {
                    JSONObject obj = new JSONObject(intent);
                    action = obj.getString("action");
                } catch (Exception e) {
                    Log.d("HomePlus-Tutorial", "Error: " + e.toString());
                }
            }
            return action;
        }

        public int getOrderNum() {
            return orderNum;
        }

        @Override
        public String toString() {
            return "Button{" +
                    "buttonName='" + buttonName + '\'' +
                    ", intent='" + intent + '\'' +
                    ", orderNum=" + orderNum +
                    '}';
        }
    }

    public class Pic {
        public String groundingTime;
        public int isGrounding;
        public String name;
        public int orderNum;
        public String picUrl;
        public String remark;
        public String undercarriageTime;

        public int getOrderNum() {
            return orderNum;
        }

        @Override
        public String toString() {
            return "Pic{" +
                    "groundingTime='" + groundingTime + '\'' +
                    ", isGrounding=" + isGrounding +
                    ", name='" + name + '\'' +
                    ", orderNum=" + orderNum +
                    ", picUrl='" + picUrl + '\'' +
                    ", remark='" + remark + '\'' +
                    ", undercarriageTime='" + undercarriageTime + '\'' +
                    '}';
        }
    }
}
