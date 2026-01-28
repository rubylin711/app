package com.prime.app.control.service;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static long          UPDATE_CYCLE            = 0;
    public static String        CONFIG_URL              = null;
    public static List<String>  CONFIG_URL_LIST         = new ArrayList<>();
    public static String        OTA_PAYLOAD_BIN         = null;
    public static String        OTA_PAYLOAD_PROPERTIES  = null;
    public static String        OTA_METADATA            = null;
    public static List<String>  APP_INSTALL_PKG         = new ArrayList<>();
    public static List<String>  APP_INSTALL_URL         = new ArrayList<>();
    public static List<String>  APP_UNINSTALL_PKG       = new ArrayList<>();
    public static boolean       WHITELIST_ENABLE        = false;
    public static List<String>  WHITELIST_APPEND        = new ArrayList<>();
    public static List<String>  WHITELIST_REMOVE        = new ArrayList<>();
    public static boolean       WHITELIST_REMOVE_ALL    = false;
    public static List<String>  WHITELIST_REPLACE       = new ArrayList<>();
    public static List<String>  WHITELIST               = new ArrayList<>();
}
