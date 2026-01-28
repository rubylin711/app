LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := PrimeGlobalKeyHandler_Source
LOCAL_CERTIFICATE := platform
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_MODULE_TAGS := optional
LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PACKAGE)
