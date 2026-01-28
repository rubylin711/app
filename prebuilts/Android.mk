LOCAL_PATH := $(my-dir)
include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeLogger
LOCAL_SRC_FILES := PrimeLogger.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeBtPair
LOCAL_SRC_FILES := PrimeBtPair.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := MiniLauncher
LOCAL_SRC_FILES := MiniLauncher.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
LOCAL_PROPRIETARY_MODULE := true
LOCAL_OVERRIDES_PACKAGES := Home Launcher2 Launcher3 Launcher3QuickStep
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := OTAUpdater
LOCAL_SRC_FILES := OTAUpdater.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := OTAService
LOCAL_SRC_FILES := OTAService.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := ServerControlService
LOCAL_SRC_FILES := ServerControlService.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

# include $(CLEAR_VARS)
# # Module name should match apk name to be installed.
# LOCAL_MODULE := DMG_Launcher
# LOCAL_SRC_FILES := DMG_Launcher.apk
# LOCAL_MODULE_CLASS := APPS
# LOCAL_MODULE_SUFFIX := .apk
# LOCAL_BUILT_MODULE_STEM := package.apk
# LOCAL_MODULE_TARGET_ARCH := arm
# LOCAL_CERTIFICATE := platform
# #LOCAL_PRIVILEGED_MODULE := true
# #LOCAL_PROPRIETARY_MODULE := true
# LOCAL_OVERRIDES_PACKAGES := Home Launcher2 Launcher3 Launcher3QuickStep TVLauncher
# # fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
# include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := ACSClient
LOCAL_SRC_FILES := ACSClient.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := WebBrowser
LOCAL_SRC_FILES := WebBrowser.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := SetupWizardCustomize
LOCAL_SRC_FILES := SetupWizardCustomize.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeRro
LOCAL_SRC_FILES := PrimeRro.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_DEX_PREOPT := false
LOCAL_MODULE_PATH := $(TARGET_OUT_SYSTEM_EXT)/overlay
include $(BUILD_PREBUILT)

#include $(CLEAR_VARS)
# Module name should match apk name to be installed.
#LOCAL_MODULE := PrimeGlobalKeysOverlay
#LOCAL_SRC_FILES := PrimeGlobalKeysOverlay.apk
#LOCAL_MODULE_CLASS := APPS
#LOCAL_MODULE_SUFFIX := .apk
#LOCAL_BUILT_MODULE_STEM := package.apk
#LOCAL_MODULE_TARGET_ARCH := arm
#LOCAL_CERTIFICATE := platform
#LOCAL_SYSTEM_EXT_MODULE := true
#LOCAL_MODULE_PATH := $(TARGET_OUT_SYSTEM_EXT)/overlay
#include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeGlobalKeyHandler
LOCAL_SRC_FILES := PrimeGlobalKeyHandler.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_PRIVILEGED_MODULE := true
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeSettingsProviderOverlay
LOCAL_SRC_FILES := PrimeSettingsProviderOverlay.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_DEX_PREOPT := false
LOCAL_MODULE_PATH := $(TARGET_OUT_SYSTEM_EXT)/overlay
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := SocLibControl
LOCAL_SRC_FILES := SocLibControl.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := AppDownloadManager
LOCAL_SRC_FILES := AppDownloadManager.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
include $(BUILD_PREBUILT)

# include $(CLEAR_VARS)
# # Module name should match apk name to be installed.
# LOCAL_MODULE := PrimeLauncher
# LOCAL_SRC_FILES := PrimeLauncher.apk
# LOCAL_MODULE_CLASS := APPS
# LOCAL_MODULE_SUFFIX := .apk
# LOCAL_BUILT_MODULE_STEM := package.apk
# LOCAL_MODULE_TARGET_ARCH := arm
# LOCAL_CERTIFICATE := platform
# #LOCAL_PRIVILEGED_MODULE := true
# #LOCAL_PROPRIETARY_MODULE := true
# LOCAL_OVERRIDES_PACKAGES := Home Launcher2 Launcher3 Launcher3QuickStep TVLauncher
# # fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
# include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := CNS_Launcher
LOCAL_SRC_FILES := CNS_Launcher.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
#LOCAL_PROPRIETARY_MODULE := true
LOCAL_OVERRIDES_PACKAGES := Home Launcher2 Launcher3 Launcher3QuickStep TVLauncher
# fix mismatch libraries
LOCAL_OPTIONAL_USES_LIBRARIES := org.apache.http.legacy androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeDtvService
LOCAL_SRC_FILES := PrimeDtvService.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_SYSTEM_EXT_MODULE := true
#LOCAL_PROPRIETARY_MODULE := true
# fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeTvInputFramework
LOCAL_SRC_FILES := PrimeTvInputFramework.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
#LOCAL_PROPRIETARY_MODULE := true
# fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)

# include $(CLEAR_VARS)
# # Module name should match apk name to be installed.
# LOCAL_MODULE := PrimeTvInputApp
# LOCAL_SRC_FILES := PrimeTvInputApp.apk
# LOCAL_MODULE_CLASS := APPS
# LOCAL_MODULE_SUFFIX := .apk
# LOCAL_BUILT_MODULE_STEM := package.apk
# LOCAL_MODULE_TARGET_ARCH := arm
# LOCAL_CERTIFICATE := platform
# #LOCAL_PRIVILEGED_MODULE := true
# #LOCAL_PROPRIETARY_MODULE := true
# # fix mismatch libraries
# # LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
# include $(BUILD_PREBUILT)
include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := PrimeSettings
LOCAL_SRC_FILES := PrimeSettings.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
# fix mismatch libraries
LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)
include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := HOMEPLUS_TV
LOCAL_SRC_FILES := HOMEPLUS_TV.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
#LOCAL_PROPRIETARY_MODULE := true
# fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)
include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := HOMEPLUS_SETTINGS
LOCAL_SRC_FILES := HOMEPLUS_SETTINGS.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
#LOCAL_PROPRIETARY_MODULE := true
# fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)
include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := HOMEPLUS_VBM
LOCAL_SRC_FILES := HOMEPLUS_VBM.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
#LOCAL_PROPRIETARY_MODULE := true
# fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)
include $(CLEAR_VARS)
# Module name should match apk name to be installed.
LOCAL_MODULE := HOMEPLUS_MEMBERCENTER
LOCAL_SRC_FILES := HOMEPLUS_MEMBERCENTER.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := .apk
LOCAL_BUILT_MODULE_STEM := package.apk
LOCAL_MODULE_TARGET_ARCH := arm
LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
#LOCAL_PROPRIETARY_MODULE := true
# fix mismatch libraries
# LOCAL_OPTIONAL_USES_LIBRARIES := androidx.window.extensions androidx.window.sidecar
include $(BUILD_PREBUILT)