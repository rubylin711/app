LOCAL_PATH := $(my-dir)
##############################
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_PACKAGE_NAME := ServerControlService_source
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_USE_AAPT2 := true
LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.legacy_legacy-support-v4 \
	androidx-constraintlayout_constraintlayout \
	androidx.appcompat_appcompat \
	androidx.leanback_leanback

#LOCAL_STATIC_JAVA_LIBRARIES := vendor.prime.hardware.misc-V1.0-java


include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
