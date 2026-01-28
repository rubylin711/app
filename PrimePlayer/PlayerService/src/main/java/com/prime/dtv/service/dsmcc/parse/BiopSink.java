package com.prime.dtv.service.dsmcc.parse;

import java.util.Map;

public interface BiopSink {
    
    // SRG：把 SRG 的 objectKey 一起帶出，mountHint 可為空字串
    void onServiceGateway(int carouselId, byte[] srgObjectKey, String mountHint);

    // 目錄：把「這個目錄自己的 objectKey」當 parent key 交給上層
    void onDirectory(int carouselId, byte[] directoryKey, Map<String, byte[]> nameToChildKey);

    // 檔案：上層用 objectKey 去對應先前的 name->key 來決定檔名與路徑
    void onFile(int carouselId, byte[] objectKey, byte[] content);
}
