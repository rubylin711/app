PesiVoiceAssist為語音ui介面，需搭配PesiRemoteService使用

build apk:
1.apk需要系統簽名
2.apk須放置在/system/priv-app/
3.在android.mk中需加入"LOCAL_PRIVILEGED_MODULE := true"才會被放入/system/priv-app/

使用方法:
1.藍芽配對remote(RemoteG10, RemoteG20 or RemoteB009)後，系統會啟動PesiRemoteService
2.按下語音鍵，PesiRemoteService會開啟PesiVoiceAssist的activity，此時會顯示ui介面
3.按下語音鍵，左下角mic icon會有動畫，此時表示mic正在收音
4.可以按下back鍵，停止收音，或是等待收音time out。停止收音後，mic icon會停止
5.等待server回傳語音文字結果