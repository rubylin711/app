package com.dolphin.dtv;

import com.prime.dtvplayer.Config.Pvcfg;

//Scoty 20181024 add for 3796/3798 usb port model

public class UsbPortNum {
    public int UsbPort1;
    public int UsbPort2;

    public UsbPortNum (){
        if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3796)
        {
            setUsbPort1(2);
            setUsbPort2(7);
        }
        else if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)
        {
            setUsbPort1(1);
            setUsbPort2(2);
        }
    }

    public int getUsbPort1()
    {
        return UsbPort1;
    }

    public void setUsbPort1(int usbPort1)
    {
        UsbPort1 = usbPort1;
    }

    public int getUsbPort2()
    {
        return UsbPort2;
    }

    public void setUsbPort2(int usbPort2)
    {
        UsbPort2 = usbPort2;
    }
}
