package com.prime.dtv.sysdata;

public class VMXProtectData {
    private int block_all_channel;
    private int location_first;
    private int location_second;
    private int location_third;
    private int location_version;

    private int group_m;
    private int group_id;
    private String E16_top;
    private String E16_bot;
    private String EWBS0_top;
    private String EWBS0_bot;
    private String EWBS1_top;
    private String EWBS1_bot;
    private String VirtualNum;//Scoty 20181225 add virtual num

    public int getBlockAllChannel()
    {
        return block_all_channel;
    }
    public void setBlockAllChannel( int value)
    {
        block_all_channel = value;
    }
    public int getLocationFirst()
    {
        return location_first;
    }
    public void setLocationFirst( int value)
    {
        location_first = value;
    }
    public int getLocationSecond()
    {
        return location_second;
    }
    public void setLocationSecond( int value)
    {
        location_second = value;
    }
    public int getLocationThird()
    {
        return location_third;
    }
    public void setLocationThird( int value)
    {
        location_third = value;
    }
    public int getLocationVersion()
    {
        return location_version;
    }
    public void setLocationVersion( int value)
    {
        location_version = value;
    }
    public int getGroupM()
    {
        return group_m;
    }
    public void setGroupM( int value)
    {
        group_m = value;
    }
    public int getGroupID()
    {
        return group_id;
    }
    public void setGroupID( int value)
    {
        group_id = value;
    }
    public String getE16Top()
    {
        return E16_top;
    }
    public void SetE16Top(String msg)
    {
        E16_top = msg;
    }
    public String getE16Bot()
    {
        return E16_bot;
    }
    public void SetE16Bot(String msg)
    {
        E16_bot = msg;
    }
    public String getEWBS0Top()
    {
        return EWBS0_top;
    }
    public void SetEWBS0Top(String msg)
    {
        EWBS0_top = msg;
    }
    public String getEWBS0Bot()
    {
        return EWBS0_bot;
    }
    public void SetEWBS0Bot(String msg)
    {
        EWBS0_bot = msg;
    }
    public String getEWBS1Top()
    {
        return EWBS1_top;
    }
    public void SetEWBS1Top(String msg)
    {
        EWBS1_top = msg;
    }
    public String getEWBS1Bot()
    {
        return EWBS1_bot;
    }
    public void SetEWBS1Bot(String msg)
    {
        EWBS1_bot = msg;
    }

    public void SetVirtualNum(String virtualnum)//Scoty 20181225 add virtual num
    {
        VirtualNum = virtualnum;
    }

    public String GetVirtualNum()//Scoty 20181225 add virtual num
    {
        return VirtualNum;
    }

}