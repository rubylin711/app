package com.prime.dmg.launcher.PVR.Management;

public class MemoryDeviceInfo {
    private String g_usb_device;
    private String g_usb_path;
    private long g_total_space;
    private long g_free_space;
    private long g_used_space;
    private String g_type;

    public MemoryDeviceInfo() {}

    public MemoryDeviceInfo(String usbDevice, String usbPath, long totalSpace, long freeSpace, long usedSpace, String type) {
        g_usb_device = usbDevice;
        g_usb_path = usbPath;
        g_total_space = totalSpace;
        g_free_space = freeSpace;
        g_used_space = usedSpace;
        g_type = type;
    }

    public String get_usb_device() {
        return g_usb_device;
    }

    public void set_usb_device(String usbDevice) {
        g_usb_device = usbDevice;
    }

    public String get_usb_path() {
        return g_usb_path;
    }

    public void set_usb_path(String usbPath) {
        g_usb_path = usbPath;
    }

    public long get_total_space() {
        return g_total_space;
    }

    public void set_total_space(long totalSpace) {
        g_total_space = totalSpace;
    }

    public long get_free_space() {
        return g_free_space;
    }

    public void set_free_space(long freeSpace) {
        g_free_space = freeSpace;
    }

    public long get_used_space() {
        return g_used_space;
    }

    public void set_used_space( long usedSpace) {
        g_used_space = usedSpace;
    }

    public String get_type() {
        return g_type;
    }

    public void set_type(String type) {
        g_type = type;
    }
}
