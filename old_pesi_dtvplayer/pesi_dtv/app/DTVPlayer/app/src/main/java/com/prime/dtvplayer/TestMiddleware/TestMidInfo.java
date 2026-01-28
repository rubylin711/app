package com.prime.dtvplayer.TestMiddleware;

/**
 * Created by eric_lin on 2017/12/12.
 */

public class TestMidInfo {
    private boolean checked;
    private String item_str;
    private int result;

    TestMidInfo(boolean value1, String str, int value2){
        checked = value1;
        item_str=str;
        result=value2;
    }

    public boolean getChecked()
    {
        return checked;
    }
    public String getItemString()
    {
        return item_str;
    }
    public int getResult()
    {
        return result;
    }

    public void setChecked(boolean value)
    {
        checked = value;
    }

    public  void setResult(int value)
    {
        result = value;
    }
}
