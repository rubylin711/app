@echo off
chcp 65001 >nul
title Copy Release APKs to Prebuilts

set PREBUILTS_DIR=..\prebuilts

echo [Info] Copying APKs to %PREBUILTS_DIR%...

:: HomePlus MemberCenter
for %%f in ("..\HomePlus\MemberCenter\app\build\outputs\apk\release\*.apk") do (
    echo [Copying] %%~nxf to HOMEPLUS_MEMBERCENTER.apk
    copy /Y "%%f" "%PREBUILTS_DIR%\HOMEPLUS_MEMBERCENTER.apk"
)

:: HomePlus TV
for %%f in ("..\HomePlus\TV\app\build\outputs\apk\release\*.apk") do (
    echo [Copying] %%~nxf to HOMEPLUS_TV.apk
    copy /Y "%%f" "%PREBUILTS_DIR%\HOMEPLUS_TV.apk"
)

:: HomePlus VBM
for %%f in ("..\HomePlus\VBM\app\build\outputs\apk\release\*.apk") do (
    echo [Copying] %%~nxf to HOMEPLUS_VBM.apk
    copy /Y "%%f" "%PREBUILTS_DIR%\HOMEPLUS_VBM.apk"
)

:: HomePlus Settings
for %%f in ("..\HomePlus\Settings\app\build\outputs\apk\release\*.apk") do (
    echo [Copying] %%~nxf to HOMEPLUS_SETTINGS.apk
    copy /Y "%%f" "%PREBUILTS_DIR%\HOMEPLUS_SETTINGS.apk"
)

:: CNS Launcher
for %%f in ("src\CNSLauncher\app\build\outputs\apk\cns\release\*.apk") do (
    echo [Copying] %%~nxf to CNS_Launcher.apk
    copy /Y "%%f" "%PREBUILTS_DIR%\CNS_Launcher.apk"
)

:: PrimeDtvService
for %%f in ("..\PrimePlayer\PrimeDtvService\build\outputs\apk\release\*.apk") do (
    echo [Copying] %%~nxf to PrimeDtvService.apk
    copy /Y "%%f" "%PREBUILTS_DIR%\PrimeDtvService.apk"
)

:: PrimeTvInputFramework
for %%f in ("..\PrimePlayer\PrimeTvInputFramework\build\outputs\apk\release\*.apk") do (
    echo [Copying] %%~nxf to PrimeTvInputFramework.apk
    copy /Y "%%f" "%PREBUILTS_DIR%\PrimeTvInputFramework.apk"
)

echo.
echo [Info] Done.
pause