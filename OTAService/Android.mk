# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := OTAService_source

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_PRIVATE_PLATFORM_APIS := false
# LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_CERTIFICATE := vendor/prime/signedkeys/platform
LOCAL_PRIVILEGED_MODULE := false
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_MODULE_TAGS := optional
LOCAL_PRODUCT_MODULE := false
LOCAL_USE_AAPT2 := true
LOCAL_DEX_PREOPT := false
LOCAL_STATIC_ANDROID_LIBRARIES := \
  android-support-v4 \
	android-support-v7-appcompat \
	android-support-constraint-layout 
	
LOCAL_STATIC_JAVA_LIBRARIES := vendor.prime.hardware.misc-V1.0-java 


include $(BUILD_PACKAGE)

# include $(CLEAR_VARS)
# LOCAL_MODULE := com.prime.android.tv.otaservice.xml
# LOCAL_MODULE_CLASS := ETC
# LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
# LOCAL_SRC_FILES := $(LOCAL_MODULE)
# include $(BUILD_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
