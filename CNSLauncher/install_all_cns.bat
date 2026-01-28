@echo off
chcp 65001 >nul
title CNS APK 自動安裝工具 (搜尋 *.apk)
color 0F

:: 2. 檢查裝置
echo [狀態] 偵測裝置中...
adb devices
echo.

call :check_and_install "..\PrimePlayer\PrimeDtvService\build\outputs\apk\debug"
call :check_and_install "..\PrimePlayer\PrimeTvInputFramework\build\outputs\apk\debug"
call :check_and_install "..\HomePlus\MemberCenter\app\build\outputs\apk\debug"
call :check_and_install "..\HomePlus\TV\app\build\outputs\apk\debug"
call :check_and_install "..\HomePlus\VBM\app\build\outputs\apk\debug"
call :check_and_install "..\HomePlus\Settings\app\build\outputs\apk\debug"
call :check_and_install "src\CNSLauncher\app\build\outputs\apk\cns\debug"

echo.
echo ========================================================
echo                  所有任務執行完畢
echo ========================================================
echo.

PAUSE
exit /b

:check_and_install
set "debug_path=%~1"
set "release_path=%debug_path:debug=release%"

if exist "%debug_path%\*.apk" (
    call "install_apk.bat" "%debug_path%"
) else (
    echo [提示] Debug 路徑未找到 APK: "%debug_path%"
    echo [提示] 嘗試 Release 路徑: "%release_path%"
    if exist "%release_path%\*.apk" (
        call "install_apk.bat" "%release_path%"
    ) else (
        echo [警告] Debug 和 Release 路徑均未找到 APK
    )
)
exit /b