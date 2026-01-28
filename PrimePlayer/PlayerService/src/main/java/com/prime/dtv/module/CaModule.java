package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;

public class CaModule {
    private static final String TAG = "CaModule";

    private static final int CMD_CA_Base = PrimeDtvMediaPlayer.CMD_Base + 0x1700;//eric lin 20210107 widevine cas , add by gary

    //CA //eric lin 20210107 widevine cas , add by gary
    private static final int CMD_CA_WIDEVINE_CAS = CMD_CA_Base + 0x01;
    //CA small command  //eric lin 20210107 widevine cas , add by gary

    private static final int INVOKE_CA_CMD_READ_SESSION = 0x01;

    public void widevine_cas_session_id(int sessionIndex, int sessionId) {//eric lin 20210107 widevine cas
        //int result = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CA_WIDEVINE_CAS);
        request.writeInt(INVOKE_CA_CMD_READ_SESSION); //cmd
        request.writeInt(sessionIndex); //need fix
        request.writeInt(sessionId); //data
        PrimeDtvMediaPlayer.invokeex(request, reply);
        Log.d(TAG, "WidevineCasSessionId() sessionIndex=" + sessionIndex + ", session id=" + sessionId);//eric lin test
        /*
        int ret = reply.readInt();
        Log.d(TAG, "WidevineCasSessionId() session id ="+sessionId);//eric lin test
        if(ret == 0) {
            Log.d(TAG, "WidevineCasSessionId() set session id success!!!");
            result = 0;
        }else
            Log.d(TAG, "WidevineCasSessionId() set session id fail!!!");
         */
        request.recycle();
        reply.recycle();
        //return result;
    }
}
