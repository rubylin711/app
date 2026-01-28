package com.prime.appDownloadManager.ServerCommunicate;

public class InterfaceString
{
    public static final String BROADCAST_ACTION = "pesi.broadcast.action.device.manager";

    public class INSTRUCTION_LIST
    {
        public final static String KEY_NAME_INSTRUCTION_ID = "INSTRUCTION_ID";
        public final static int INSTRUCTION_ID_DEFAULT_VALUE = 0;

        public class UI_Management
        {
            public class INSTALL_APK
            {
                public final static int ID = 201;
                public final static String KEY_NAME_INSTALL_PKGNAME = "INSTALL_PKGNAME";
                public final static String KEY_NAME_INSTALL_PKG_DOWNLOAD_PATH = "INSTALL_PKG_DOWNLOAD_PATH";
            }
            public class UNINSTALL_APK
            {
                public final static int ID = 202;
                public final static String KEY_NAME_UNINSTALL_PKGNAME = "UNINSTALL_PKGNAME";
            }
            public class SET_WHITELIST_ONOFF
            {
                public final static int ID = 203;
                public final static String KEY_NAME_WHITELIST_ONOFF = "WHITELIST_ONOFF";
            }

            public class SET_WHITELIST
            {
                public final static int ID = 204;
                public final static String KEY_NAME_SET_WHITELIST = "SET_WHITELIST";
                public final static String KEY_NAME_APPEND = "APPEND";
            }

            public class DELETE_WHITELIST
            {
                public final static int ID = 205;
                public final static String KEY_NAME_DELETE_WHITELIST = "DELETE_WHITELIST";
            }

            public class DELETE_ALL_WHITELIST
            {
                public final static int ID = 206;
            }

            public class GET_WHITELIST
            {
                public final static int ID = 207;
            }

            public class SET_RETURN_TARGET
            {
                public final static int ID = 208;
                public final static String KEY_NAME_RETURN_TARGET_ACTION = "RETURN_TARGET_ACTION";
                public final static String KEY_NAME_RETURN_TARGET_PKGNAME = "RETURN_TARGET_PKGNAME";
            }

            public class CMD_RESULT
            {
                public final static int ID = 209;
                public final static String KEY_NAME_EXECUTE_INSTRUCTION_ID = "EXECUTE_INSTRUCTION_ID";
                public final static String KEY_NAME_RESULT_STATUS = "RESULT_STATUS";
                public final static String KEY_NAME_RESULT_DETAIL = "RESULT_DETAIL";
                public final static String KEY_NAME_DOWNLOAD_ID = "DOWNLOAD_ID";
                public final static String KEY_NAME_DOWNLOAD_FILE_PATH = "DOWNLOAD_FILE_PATH";
                public final static String KEY_NAME_CURRENT_WHITELIST = "CURRENT_WHITELIST";
                public final static String KEY_NAME_APPEND_SAME_PKGNAME_LIST = "APPEND_SAME_PKGNAME_LIST";
                public final static String KEY_NAME_DELETED_WHITELIST = "DELETED_WHITELIST";
                public final static String KEY_NAME_PKGNAMES_NOT_FOUND_LIST = "PKGNAMES_NOT_FOUND_LIST";
                public final static int EXECUTE_INSTRUCTION_ID_DEFAULT_VALUE = 0;
                public final static int DOWNLOAD_ID_DEFAULT_VALUE = 0;
            }

            public class RESULT_STATUS
            {
                public final static int DOWNLOAD_RESULT_STATUS_PENDING = 100;
                public final static int DOWNLOAD_RESULT_STATUS_RUNNING = 101;
                public final static int DOWNLOAD_RESULT_STATUS_PAUSED = 102;
                public final static int DOWNLOAD_RESULT_STATUS_SUCCESSFUL = 103;
                public final static int DOWNLOAD_RESULT_STATUS_FAILED = 104;

                public final static int DOWNLOAD_PROGRESS = 200;

                public final static int INSTALL_UNINSTALL_RESULT_STATUS_PENDING_STREAMING = 300;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_PENDING_USER_ACTION = 301;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_SUCCESS = 302;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_FAILURE = 303;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_BLOCKED = 304;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_ABORTED = 305;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_INVALID = 306;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_CONFLICT = 307;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_STORAGE = 308;
                public final static int INSTALL_UNINSTALL_RESULT_STATUS_FAILURE_INCOMPATIBLE = 309;

                public final static int RESULT_STATUS_SUCCESS = 400;
                public final static int RESULT_STATUS_FAILURE = 401;

                public final static int STATUS_PENDING_STREAMING = -2;//PackageInstaller.java hide
            }

            public class RESULT_DETAIL
            {
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_UNKNOWN = "ERROR_UNKNOWN";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_FILE_ERROR = "ERROR_FILE_ERROR";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_UNHANDLED_HTTP_CODE = "ERROR_UNHANDLED_HTTP_CODE";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_HTTP_DATA_ERROR = "ERROR_HTTP_DATA_ERROR";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_TOO_MANY_REDIRECTS = "ERROR_TOO_MANY_REDIRECTS";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_INSUFFICIENT_SPACE = "ERROR_INSUFFICIENT_SPACE";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_DEVICE_NOT_FOUND = "ERROR_DEVICE_NOT_FOUND";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_CANNOT_RESUME = "ERROR_CANNOT_RESUME";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_FILE_ALREADY_EXISTS = "ERROR_FILE_ALREADY_EXISTS";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_BLOCKED = "ERROR_BLOCKED";
                public final static String DOWNLOAD_RESULT_DETAIL_ERROR_NO_NETWORK = "ERROR_NO_NETWORK";
                public final static String DOWNLOAD_RESULT_DETAIL_PAUSED_WAITING_TO_RETRY = "PAUSED_WAITING_TO_RETRY";
                public final static String DOWNLOAD_RESULT_DETAIL_PAUSED_WAITING_FOR_NETWORK = "PAUSED_WAITING_FOR_NETWORK";
                public final static String DOWNLOAD_RESULT_DETAIL_PAUSED_QUEUED_FOR_WIFI = "PAUSED_QUEUED_FOR_WIFI";
                public final static String DOWNLOAD_RESULT_DETAIL_PAUSED_UNKNOWN = "PAUSED_UNKNOWN";

                public final static int ERROR_BLOCKED = 1010;//DownloadManager.java hide

                public final static String UNINSTALL_RESULT_DETAIL_ERROR_APP_NOT_EXIST = "APP_NOT_EXIST";

                public final static String SET_WHITELIST_RESULT_DETAIL_APPEND_SUCCESS = "APPEND_SUCCESS";
                public final static String SET_WHITELIST_RESULT_DETAIL_COVER_SUCCESS = "COVER_SUCCESS";
                public final static String SET_WHITELIST_RESULT_DETAIL_APPEND_AUTO_MERGE = "APPEND_AUTO_MERGE";
                public final static String SET_WHITELIST_RESULT_DETAIL_APPEND_EMPTY = "APPEND_EMPTY";

                public final static String DELETE_WHITELIST_RESULT_DETAIL_PKGNAME_NOT_FOUND = "PKGNAME_NOT_FOUND";
                public final static String DELETE_WHITELIST_RESULT_DETAIL_DELETED_EMPTY = "DELETED_EMPTY";

                public final static String GET_WHITELIST_RESULT_DETAIL_EMPTY_WHITELIST = "EMPTY_WHITELIST";

                public final static String RESULT_DETAIL_SAVE_FAILURE = "SAVE_FAILURE";
            }
        }
    }
}
