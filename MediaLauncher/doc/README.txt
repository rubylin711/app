介紹：
    插入 USB 後，MediaLauncher 自動讀取 USB 內的 video & image
    並自動使用 USB 內的 signature 驗證 video & image
    若驗證通過，MediaLauncher 會將檔案加入播放清單，並自動循環播放

使用方法：
    1. 必須搭配 USB disk，請在 USB 建立目錄
        - mkdir -p /storage/C03E-0A55/AUTO_PLAY/
        - mkdir -p /storage/C03E-0A55/AUTO_PLAY/sign
    2. AUTO_PLAY 用來放置 video & image
       AUTO_PLAY/sign 用來放置所有 video & image 的 signature
       MediaSignEncryption 產生的 signature
    3. 將 USB disk 插入 STB，MediaLauncher 偵測到後會自動驗證 signature
       並且只會播放通過驗證的 video & image
    4. RCU 按 <UP> <DOWN> <LEFT> <RIGHT> 啟動 Main Settings