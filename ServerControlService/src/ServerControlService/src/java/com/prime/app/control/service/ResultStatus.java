package com.prime.server.control.service;

public class ResultStatus {
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
