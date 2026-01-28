LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call find-other-java-files, java)
LOCAL_MODULE_TAGS := optional
#LOCAL_PROPRIETARY_MODULE := true
LOCAL_MODULE := PrimeAudioTrack
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_JAVA_LIBRARY)
