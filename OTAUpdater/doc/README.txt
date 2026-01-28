OTA Updater 可透過 OTA Service 或 Recovery 更新系統
OTA Service 相關資料請參考 git@10.1.4.203:pesi_sw1/Launcher/OTAService.git

OTA Updater 根據 USB 隨身碟內的目錄來決定是否執行 OTA 更新，使用者可依個人需求建立下列目錄
    - 建立 Pesi_Ota 會先詢問使用者是否要執行 OTA 更新
    - 建立 Pesi_Force_Ota 會直接執行 OTA 更新

上述目錄建立完成後，OTA Updater 會根據目錄內的檔案使用對應方法進行 OTA 更新

    方法 1 : 透過 OTA Service 更新系統，必須在上述目錄置入
        - payload.bin
        - payload_properties.txt
        - metadata

    方法 2 : 透過 Recovery System 更新系統，必須在上述目錄置入
        - 更新用的 zip 檔，檔名必須為 aosp_usb.zip

上述目錄、檔案準備好後，請依照下列步驟進行更新
    1. 啟動 OTA Updater 開啟背景 Service，再關閉 OTA Updater ( 只需要啟動一次 )
    2. 插入 USB 隨身碟後，等待 OTA 更新開始，或根據 Dialog 指示進行 OTA 更新
    3. 更新完後請移除 USB 隨身碟並重新開機使系統完成更新




Ota Updater加權限(只測試了recovery update)
    步驟1. 在prime_config中加入"加權限"的資料夾:
        -BOARD_SEPOLICY_DIRS += vendor/prime/sepolicy

    步驟2. 建立system_app.te:
        -內容:
            allow system_app update_engine:binder { call transfer };
            allow system_app cache_file:lnk_file read;

    步驟2. 建立update_engine.te:
        -內容:
            allow update_engine system_app:binder call;