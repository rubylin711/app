
PrimeLogger 根據 USB 隨身碟內的目錄來決定存放的 STB 資訊，使用者可依個人需求建立下列目錄
   * 建立 Pesi_Logcat 會擷取 logcat 
   * 建立 Pesi_UserSystem 會擷取 dropbox、tombstone 
   * 建立 Pesi_DeviceInfo 會擷取 property、recovery log ... 
   
上述目錄建立完成後，請依照下列步驟抓取 STB 資料 
   1. 啟動 PrimeLogger 開啟背景 Service，再關閉 PrimeLogger (只需要啟動一次) 
   2. 根據需求重現問題 
   3. 插入 USB 隨身碟並等待 1 分鐘 
   4. 1 分鐘後移除 USB 隨身碟，之後便可在目錄內可根據時間找到 STB 資料