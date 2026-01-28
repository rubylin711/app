@echo off
chcp 65001 >nul
title APK 自動安裝工具 (搜尋 *.apk)
color 0F

set "TARGET_DIR=%~1"
if not exist "%TARGET_DIR%" (
    color 0C
    echo.
    echo [錯誤] 找不到目標路徑：
    echo "%TARGET_DIR%"
    echo.
    pause
    exit /b
)
set "ORIGINAL_DIR=%CD%"
cd /d "%TARGET_DIR%"
echo [路徑] 目前位置: "%CD%"
echo.

:: 1. 檢查 ADB
where adb >nul 2>nul
if %errorlevel% neq 0 (
    color 0C
    echo [錯誤] 找不到 adb 指令，請檢查環境變數或將 adb.exe 放入此目錄。
    pause
    exit /b
)

:: 設定變數來判斷是否找到檔案
set "found=0"

:: 3. 開始搜尋並安裝所有 .apk 檔案
:: %%i 代表找到的檔案名稱
for %%i in (*.apk) do (
    set "found=1"
    echo --------------------------------------------------------
    echo [發現檔案] %%i
    echo [狀態] 正在安裝... 請稍候
    
    :: 執行安裝指令 (包含 -r 覆蓋, -t 測試)
    adb install -r -t "%%i"
    
    if errorlevel 0 (
        echo [結果] %%i 安裝成功!
    ) else (
        color 0C
        echo [結果] %%i 安裝失敗!
    )
)

:: 4. 如果完全沒找到 apk
if "%found%"=="0" (
    color 0C
    echo.
    echo [錯誤] 此資料夾內找不到任何 .apk 檔案。
    echo 請將 apk 檔跟此腳本放在一起。
)
cd /d "%ORIGINAL_DIR%"

echo.