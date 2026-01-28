// IVbmBridge.aidl
package com.inspur.cnsvbm;

// 不再需要 import 任何 Record，因為我們只傳 String
interface IVbmBridge {
    /**
     * 接收任何類型的 VBM 資料
     * @param jsonPayload 包含 agentId, values 等資訊的 JSON 字串
     */
    void sendVbmJson(String jsonPayload);
}