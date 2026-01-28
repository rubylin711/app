ServerControlService 功能

	透過遠端設定檔 server_control_setting.json 執行 送broadcast給otaupdater & App Download Manager 功能(暫時先移除)
	確保網路正常，才可以運作

	OTA Service 功能:
		OTA 升級

	App Download Manager 功能:
		安裝 APP
		移除 APP
		白名單: ON/OFF
		白名單: 添加 Package
		白名單: 取代整個白名單
		白名單: 移除 Package
		白名單: 移除全部 Package

	其他功能:
		更新週期: 設定多久時間去讀取設定檔 URL & 執行 ServerControlService 功能
		更新 URL: 設定 "設定檔 URL" array

	對應版本:
		只要 broadcast 參數不變，ServerControlService 不需要對應特定版本
		目前 ServerControlService 對應版本 OTA Service 2.1.2 ＆ AppDownloadManager 1.1

--

Server Control Service 參數更新方式

	1. SDK 預設 property "persist.sys.prime.server_control_config"，內容為預設設定檔 URL
	2. service 第 1 次透過 property URL 更新參數
	3. 更新參數後，service 得到 [URL array 1, property URL]，URL array 內包含多個設定檔 URL
	4. service 第 2 次透過 [URL array 1, property URL] 更新參數
	5. 更新參數後，service 得到 [URL array 1, URL array 2, property URL]
	6. service 第 N 次更新得到 Total URL Array [URL array 1, URL array 2, ..., URL array N, property URL]
	7. 依序以 [URL array 1, URL array 2, ..., URL array N, property URL] 更新參數
	7. 當 Total URL Array 內的設定檔 URL 更新成功後停止更新
	8. 當 Total URL Array 內的設定檔 URL 更新失敗，service 再透過最後一個 property URL 更新參數

--

Server Control Service 預設 URL

	預設設定檔 URL 可透過下列方式修改 URL
	setprop persist.sys.prime.server_control_config http://www.your_server.com/your_config.json
	或是修改vendor/prime/system.prop內的persist.sys.prime.server_control_config

--

Server Control Service 設定檔範例 & 說明
{
    "Configuration Download Config Cycle": 30,       // 每 30 秒下載一次設定檔
    "Configuration Remote URL": [           // 第 1 次下載 property 指定的設定檔
        "http://10.1.4.180/0000.json"       // 第 2 次下載 0000.json 設定檔
        ,"http://10.1.4.180/1111.json"      // 若失敗，第 3 次下載 1111.json 設定檔
        ,"http://10.1.4.180/2222.json"      // 若失敗，第 4 次下載 2222.json 設定檔
                                            // 若失敗，下載 property 指定的設定檔
	],
    "OTA File": {                                                          // OTA Updater 執行 OTA 更新用到的檔案
        "payload":            "http://10.1.4.180/payload.bin",             // OTA 更新用到的 payload.bin
        "payload properties": "http://10.1.4.180/payload_properties.txt",  // OTA 更新用到的 payload_properties.txt
        "metadata":           "http://10.1.4.180/metadata"                 // 檢查 timestamp 用到的 metadata
	},
    "App Manager": {                               // AppDownloadManager 負責處理 apk 安裝移除 & 白名單功能
        "Whitelist": {                             
            "Enable": true,                        // 白名單開關
            "Append": ["aaa.aaa.aaa"               // 白名單新增 array 內的 package name
			           , "bbb.bbb.bbb"             // package name 在白名單內將允許安裝 apk
					   , "com.test.read.url"
					   , "com.prime.testmount"
					   , "com.xiaodianshi.tv.yst"],
            "Replace": ["xxx.xxx.xxx"              // 將 array 內的 package name 取代整個白名單
			            , "com.xiaodianshi.tv.yst"
						, "com.prime.testmount"],
            "Remove": ["xxx.xxx.xxx"],             // 白名單移除 array 指定的 package name
			"RemoveAll": false                     // 指定是否移除整個白名單內的 package name
        },
        "Install": [                               // 安裝兩個 apk，package name 為 com.prime.testmount & com.xiaodianshi.tv.yst
			["com.prime.testmount", "http://10.1.4.180/testMount.apk"],
			["com.xiaodianshi.tv.yst", "https://dl.hdslb.com/mobile/latest/android_tv_yst/iBiliTV-master.apk?t=20231025&spm_id_from=333.47.b_646f776e6c6f61642d6c696e6b.5"]
		],
        "Uninstall": [                             // 移除兩個 apk，package name 為 com.prime.testmount & com.xiaodianshi.tv.yst
			"com.prime.testmount", "com.xiaodianshi.tv.yst"
		]
    }
}


	
	
	
	
	
	
	
	
	
	
	
	
	
	