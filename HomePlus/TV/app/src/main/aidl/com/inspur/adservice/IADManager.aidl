// IADManager.aidl
package com.inspur.adservice;
import com.inspur.adservice.IADCallback;
// Declare any non-default types here with import statements

interface IADManager {
    //
   String getADJsonData(String key);
   void registerListener(IADCallback callBack,String type);
   void unregisterListener(IADCallback callBack);
}
