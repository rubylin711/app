package com.prime.launcher.Utils.JsonParser;

import java.util.List;

public class LauncherInfo {

    private String btMac;
    private boolean dlna;
    private boolean miracast;
    private int otaSyncInterval;
    private int reportInterval;
    private String bPlusTrigger;
    private String hdmiOutControl;
    private String hdmiOutControlStatus;
    private boolean hdmiPatch;
    private String projectName;
    private String modelName;
    private String androidVersion;
    private String condition1;
    private String condition2;
    private String condition3;
    private String condition4;
    private String condition5;
    private long otaConfigTime;
    private long appConfigTime;
    private String otaStarted;
    private String otaDrivenBy;
    private int otaAfterStandby;
    private int otaAfterBoot;
    private int otaAfterImmediately;
    private String otaAfterTime;
    private int deviceLastFwVersionId;
    private int videoRecommendationId;
    private int scheduleProfileId;
    private int appProfileId;
    private int recommendationProfileId;
    private int hotVideoProfileId;
    private int networkSwitch;
    private String ubaUploadMin;
    private String logSize;
    private String ubaForceUploadHour;
    private String ledStyle;
    private int report_interval;
    private boolean enable_log;
    private boolean auto_log;
    private String group_config_version;
    private boolean UBARecommendation;
    private boolean UBARemoteButton;
    private boolean UBALaunchPoint;
    private boolean UBADirectionKey;
    private boolean UBACollect;
    private String sw_group_name;
    private int sw_sync_interval;
    private String app_group_name;
    private int app_sync_interval;
    private int launcher_sync_interval;
    private String HDMIOutControl;
    private String BplusTrigger;

    private List<LauncherProfile> launcher_profile;

    public String get_bt_mac() {
        return btMac;
    }

    public boolean is_dlna() {
        return dlna;
    }

    public boolean is_miracast() {
        return miracast;
    }

    public int get_ota_sync_interval() {
        return otaSyncInterval;
    }

    public int get_report_interval() {
        return reportInterval;
    }

    public String get_bplus_trigger() {
        return bPlusTrigger;
    }

    public String get_hdmi_out_control() {
        return hdmiOutControl;
    }

    public String get_hdmi_out_control_status() {
        return hdmiOutControlStatus;
    }

    public boolean is_hdmi_patch() {
        return hdmiPatch;
    }

    public String get_project_name() {
        return projectName;
    }

    public String get_model_name() {
        return modelName;
    }

    public String get_android_version() {
        return androidVersion;
    }

    public String get_condition_1() {
        return condition1;
    }

    public String get_condition_2() {
        return condition2;
    }

    public String get_condition_3() {
        return condition3;
    }

    public String get_condition_4() {
        return condition4;
    }

    public String get_condition_5() {
        return condition5;
    }

    public long get_ota_config_time() {
        return otaConfigTime;
    }

    public long get_app_config_time() {
        return appConfigTime;
    }

    public String get_ota_started() {
        return otaStarted;
    }

    public String get_ota_driven_by() {
        return otaDrivenBy;
    }

    public int get_ota_after_standby() {
        return otaAfterStandby;
    }

    public int get_ota_after_boot() {
        return otaAfterBoot;
    }

    public int get_ota_after_immediately() {
        return otaAfterImmediately;
    }

    public String get_ota_after_time() {
        return otaAfterTime;
    }

    public int get_device_last_fw_version_id() {
        return deviceLastFwVersionId;
    }

    public int get_video_recommendation_id() {
        return videoRecommendationId;
    }

    public int get_schedule_profile_id() {
        return scheduleProfileId;
    }

    public int get_app_profile_id() {
        return appProfileId;
    }

    public int get_recommendation_profile_id() {
        return recommendationProfileId;
    }

    public int get_hot_video_profile_id() {
        return hotVideoProfileId;
    }

    public int get_network_switch() {
        return networkSwitch;
    }

    public String get_uba_upload_min() {
        return ubaUploadMin;
    }

    public String get_log_size() {
        return logSize;
    }

    public String get_uba_force_upload_hour() {
        return ubaForceUploadHour;
    }

    public String get_led_style() {
        return ledStyle;
    }

    public int getReport_interval() {
        return report_interval;
    }

    public boolean is_enable_log() {
        return enable_log;
    }

    public boolean is_auto_log() {
        return auto_log;
    }

    public String get_group_config_version() {
        return group_config_version;
    }

    public boolean is_uba_recommendation() {
        return UBARecommendation;
    }

    public boolean is_uba_remote_button() {
        return UBARemoteButton;
    }

    public boolean is_uba_launch_point() {
        return UBALaunchPoint;
    }

    public boolean is_uba_direction_key() {
        return UBADirectionKey;
    }

    public boolean is_uba_collect() {
        return UBACollect;
    }

    public String get_sw_group_name() {
        return sw_group_name;
    }

    public int get_sw_sync_interval() {
        return sw_sync_interval;
    }

    public String get_app_group_name() {
        return app_group_name;
    }

    public int get_app_sync_interval() {
        return app_sync_interval;
    }

    public int get_launcher_sync_interval() {
        return launcher_sync_interval;
    }

    public String get_HDMI_out_control() {
        return HDMIOutControl;
    }

    public String get_Bplus_trigger() {
        return BplusTrigger;
    }

    public List<LauncherProfile> get_launcher_profiles() {
        return launcher_profile;
    }

    public void set_bt_mac(String btMac) {
        this.btMac = btMac;
    }

    public void set_dlna(boolean dlna) {
        this.dlna = dlna;
    }

    public void set_miracast(boolean miracast) {
        this.miracast = miracast;
    }

    public void set_ota_sync_interval(int otaSyncInterval) {
        this.otaSyncInterval = otaSyncInterval;
    }

    public void set_report_interval(int reportInterval) {
        this.reportInterval = reportInterval;
    }

    public void set_bPlus_trigger(String bPlusTrigger) {
        this.bPlusTrigger = bPlusTrigger;
    }

    public void set_hdmi_out_control(String hdmiOutControl) {
        this.hdmiOutControl = hdmiOutControl;
    }

    public void set_hdmi_out_control_status(String hdmiOutControlStatus) {
        this.hdmiOutControlStatus = hdmiOutControlStatus;
    }

    public void set_hdmi_patch(boolean hdmiPatch) {
        this.hdmiPatch = hdmiPatch;
    }

    public void set_project_name(String projectName) {
        this.projectName = projectName;
    }

    public void set_model_name(String modelName) {
        this.modelName = modelName;
    }

    public void set_android_version(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public void set_condition_1(String condition1) {
        this.condition1 = condition1;
    }

    public void set_condition_2(String condition2) {
        this.condition2 = condition2;
    }

    public void set_condition_3(String condition3) {
        this.condition3 = condition3;
    }

    public void set_condition_4(String condition4) {
        this.condition4 = condition4;
    }

    public void set_condition_5(String condition5) {
        this.condition5 = condition5;
    }

    public void set_ota_config_time(long otaConfigTime) {
        this.otaConfigTime = otaConfigTime;
    }

    public void set_app_config_time(long appConfigTime) {
        this.appConfigTime = appConfigTime;
    }

    public void set_ota_started(String otaStarted) {
        this.otaStarted = otaStarted;
    }

    public void set_ota_driven_by(String otaDrivenBy) {
        this.otaDrivenBy = otaDrivenBy;
    }

    public void set_ota_after_standby(int otaAfterStandby) {
        this.otaAfterStandby = otaAfterStandby;
    }

    public void set_ota_after_boot(int otaAfterBoot) {
        this.otaAfterBoot = otaAfterBoot;
    }

    public void set_ota_after_immediately(int otaAfterImmediately) {
        this.otaAfterImmediately = otaAfterImmediately;
    }

    public void set_ota_after_time(String otaAfterTime) {
        this.otaAfterTime = otaAfterTime;
    }

    public void set_device_last_fw_version_id(int deviceLastFwVersionId) {
        this.deviceLastFwVersionId = deviceLastFwVersionId;
    }

    public void set_video_recommendation_id(int videoRecommendationId) {
        this.videoRecommendationId = videoRecommendationId;
    }

    public void set_schedule_profile_id(int scheduleProfileId) {
        this.scheduleProfileId = scheduleProfileId;
    }

    public void set_app_profile_id(int appProfileId) {
        this.appProfileId = appProfileId;
    }

    public void set_recommendation_profile_id(int recommendationProfileId) {
        this.recommendationProfileId = recommendationProfileId;
    }

    public void set_hot_video_profile_id(int hotVideoProfileId) {
        this.hotVideoProfileId = hotVideoProfileId;
    }

    public void set_network_switch(int networkSwitch) {
        this.networkSwitch = networkSwitch;
    }

    public void set_uba_upload_min(String ubaUploadMin) {
        this.ubaUploadMin = ubaUploadMin;
    }

    public void set_log_size(String logSize) {
        this.logSize = logSize;
    }

    public void set_uba_force_upload_hour(String ubaForceUploadHour) {
        this.ubaForceUploadHour = ubaForceUploadHour;
    }

    public void set_led_style(String ledStyle) {
        this.ledStyle = ledStyle;
    }

    public void setReport_interval(int report_interval) {
        this.report_interval = report_interval;
    }

    public void set_enable_log(boolean enable_log) {
        this.enable_log = enable_log;
    }

    public void set_auto_log(boolean auto_log) {
        this.auto_log = auto_log;
    }

    public void set_group_config_version(String group_config_version) {
        this.group_config_version = group_config_version;
    }

    public void set_uba_recommendation(boolean UBARecommendation) {
        this.UBARecommendation = UBARecommendation;
    }

    public void set_uba_remote_button(boolean UBARemoteButton) {
        this.UBARemoteButton = UBARemoteButton;
    }

    public void set_uba_launch_point(boolean UBALaunchPoint) {
        this.UBALaunchPoint = UBALaunchPoint;
    }

    public void set_uba_direction_key(boolean UBADirectionKey) {
        this.UBADirectionKey = UBADirectionKey;
    }

    public void set_uba_collect(boolean UBACollect) {
        this.UBACollect = UBACollect;
    }

    public void set_sw_group_name(String sw_group_name) {
        this.sw_group_name = sw_group_name;
    }

    public void set_sw_sync_interval(int sw_sync_interval) {
        this.sw_sync_interval = sw_sync_interval;
    }

    public void set_app_group_name(String app_group_name) {
        this.app_group_name = app_group_name;
    }

    public void set_app_sync_interval(int app_sync_interval) {
        this.app_sync_interval = app_sync_interval;
    }

    public void set_launcher_sync_interval(int launcher_sync_interval) {
        this.launcher_sync_interval = launcher_sync_interval;
    }

    public void set_HDMI_out_control(String HDMIOutControl) {
        this.HDMIOutControl = HDMIOutControl;
    }

    public void set_Bplus_trigger(String bplusTrigger) {
        BplusTrigger = bplusTrigger;
    }

    public void set_launcher_profile(List<LauncherProfile> launcher_profile) {
        this.launcher_profile = launcher_profile;
    }
}
