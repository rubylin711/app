package com.prime.homeplus.membercenter.enity;


import com.google.gson.JsonObject;
import com.prime.homeplus.membercenter.utilcode.EncryptUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RequestData {
    private String command;
    private String timestamp;
    private String sign;
    private JsonObject request_data;

    public RequestData(String command, String timestamp, JsonObject request_data) {
        this.command = command;
        this.timestamp = timestamp;
        this.request_data = request_data;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public JsonObject getRequest_data() {
        return request_data;
    }

    public void setRequest_data(JsonObject request_data) {
        this.request_data = request_data;
    }

    public String getRequestBody() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("command", command);
        String sign = EncryptUtils.encryptMD5ToString(timestamp + gbEncoding(request_data.toString())).toLowerCase();
        jsonObject.addProperty("sign", sign);
        jsonObject.addProperty("timestamp", timestamp);
        jsonObject.addProperty("request_data", gbEncoding(request_data.toString()));
        return jsonObject.toString();
    }

    public static String encodeUnicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append("\\u00" + Integer.toHexString(c));
        }
        return unicode.toString().toLowerCase();
    }
    public static String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }

}
