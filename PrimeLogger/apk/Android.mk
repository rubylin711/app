LOCAL_PATH := $(my-dir)
include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeLogger
LOCAL_SRC_FILES := PrimeLogger_v1.0.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)
