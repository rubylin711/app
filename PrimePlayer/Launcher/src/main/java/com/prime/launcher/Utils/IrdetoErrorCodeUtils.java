package com.prime.launcher.Utils;

import com.prime.datastructure.Ca.IrdetoErrorCode;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.prime.datastructure.Ca.IrdetoErrorCode;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.launcher.CasInfo.CloakMessages;
import com.prime.launcher.ChannelChangeManager;

import java.util.HashMap;
import java.util.Map;

public class IrdetoErrorCodeUtils {
    public static final String TAG = "IrdetoErrorCodeUtils";

    private Map<Integer, String> ErrorCodePriorityMAP = new HashMap<>();
    private int current_error = IrdetoErrorCode.ErrorCodePriority.ERR_MAX.getValue();
    private int e48_or_e52;
    private HandlerThread e48_52_handler_thread;
    private Handler e48_52_handler;

    public static IrdetoErrorCodeUtils.Holder ErrocCodeUtilHolder;

    public interface Callback {
        void on_e52_change();
    }

    private Callback g_callback = null;

    public static class Holder{
        public IrdetoErrorCodeUtils ErrorCodeUtilData;

        public Holder(){
            ErrorCodeUtilData = new IrdetoErrorCodeUtils();
        }
    }

    public static IrdetoErrorCodeUtils getInstance() {
        if(ErrocCodeUtilHolder == null)
            ErrocCodeUtilHolder = new IrdetoErrorCodeUtils.Holder();
        return ErrocCodeUtilHolder.ErrorCodeUtilData;
    }

    public IrdetoErrorCodeUtils() {
        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E48_52.getValue(),"");
        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E38.getValue(),"");
        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_CA.getValue(),"");
        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E44.getValue(),"");
        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_PESI_DEFINE.getValue(),"");
        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E42.getValue(),"");
        e48_or_e52 = 0;
        e48_52_handler_thread = new HandlerThread("E48_52_check");
        e48_52_handler_thread.start();
        e48_52_handler = new Handler(e48_52_handler_thread.getLooper());
    }

    public void check_to_e52() {
        e48_52_handler.removeCallbacksAndMessages(null);
        e48_52_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(g_callback != null)
                    g_callback.on_e52_change();
            }
        },30*1000);
    }

    public synchronized boolean IrdetoErrorMessagePriorityManager(Context context, int error_flag, String error_code) {
//        for(int i = 0 ; i < ErrorCodePriority.ERR_MAX.getValue(); i++) {
//            Log.d(TAG,"ErrorCodePriorityMAP.get("+i+") = "+ErrorCodePriorityMAP.get(i));
//        }
//        Log.d(TAG,"IrdetoErrorMessagePriorityManager error_flag = "+error_flag+" error_code = "+error_code);
        if(!CloakMessages.is_general_banner_msg(error_flag,error_code))
            return false;
        if(error_code != null && !error_code.isEmpty()) {
            char firstChar = error_code.charAt(0);
//            Log.d(TAG,"firstChar = " + firstChar );
            if(firstChar == 'E' || firstChar == 'e') {
                switch (error_flag) {
                    case IrdetoErrorCode.SET_E52:
                        e48_or_e52 = IrdetoErrorCode.SET_E52;
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E48_52.getValue(), error_code);
                        break;
                    case IrdetoErrorCode.SET_E48:
                        e48_or_e52 = IrdetoErrorCode.SET_E48;
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E48_52.getValue(), error_code);
                        break;
                    case IrdetoErrorCode.SET_E38:
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E38.getValue(), error_code);
                        break;
                    case IrdetoErrorCode.SET_E42:
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E42.getValue(), error_code);
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E44.getValue(), "");
                        break;
                    case IrdetoErrorCode.SET_E44:
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E44.getValue(), error_code);
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E42.getValue(), "");
                        break;
                    case IrdetoErrorCode.SET_PESI_DEFINE:
//                    ErrorCodePriorityMAP.put(ErrorCodePriority.ERR_PESI_DEFINE.getValue(),error_code);
                        break;
                    case 0:
                        ChannelChangeManager channel_change_manager = ChannelChangeManager.get_instance(context);
                        ProgramInfo programInfo = channel_change_manager.get_default_channel();

                        if (programInfo != null){
//                            Log.d(TAG,"programInfo["+programInfo.getDisplayNum()+"] ["+programInfo.getDisplayName()+"] getCa() = "+programInfo.getCA());
                            if(programInfo.getCA() == 0)
                                return false;
                        }
                        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_CA.getValue(), error_code);
                        Log.d(TAG,"IrdetoErrorMessagePriorityManager set ca error "+error_code);
                        break;
                }
            }
            else if(firstChar == 'I' || firstChar == 'i')
            {
                Log.d(TAG,"IrdetoErrorMessagePriorityManager info messages do not display in error banner!!!");
            }
            else if(firstChar == 'D' || firstChar == 'd')
            {
                ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_CA.getValue(), "");
                Log.d(TAG,"IrdetoErrorMessagePriorityManager clean ca error");
            }
        }
        else
        {
            switch(error_flag)
            {
                case IrdetoErrorCode.CLEAN_E48_52:
                    e48_or_e52 = 0;
                    e48_52_handler.removeCallbacksAndMessages(null);
                    ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E48_52.getValue(), "");
                    break;
                case IrdetoErrorCode.CLEAN_E38:
                    ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E38.getValue(), "");
                    break;
                case IrdetoErrorCode.CLEAN_E42:
                    ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E42.getValue(), "");
                    break;
                case IrdetoErrorCode.CLEAN_E44:
                    ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_E44.getValue(), "");
                    break;
                case IrdetoErrorCode.CLEAN_PESI_DEFINE://gary20120228 add no video signal and bad signal message
//                    ErrorCodePriorityMAP.put(ErrorCodePriority.ERR_PESI_DEFINE.getValue(), "");
                    break;
            }
        }
//        for(int i = 0 ; i < ErrorCodePriority.ERR_MAX.getValue(); i++) {
//            Log.d(TAG,"IrdetoErrorMessagePriorityManager ErrorCodePriorityMAP.get("+i+") = "+ErrorCodePriorityMAP.get(i));
//        }
        set_current_error();
        return true;
    }

    private void set_current_error() {
        current_error = IrdetoErrorCode.ErrorCodePriority.ERR_MAX.getValue();
        for(int i = 0; i < IrdetoErrorCode.ErrorCodePriority.ERR_MAX.getValue(); i++) {
            if(!ErrorCodePriorityMAP.get(i).isEmpty()) {
                current_error = i;
                Log.d(TAG,"IrdetoErrorMessagePriorityManager current error["+i+"] = "+ErrorCodePriorityMAP.get(i));
                break;
            }
        }
    }

    public int getE48_or_e52() {
        return e48_or_e52;
    }

    public String getIrdetoErrorMsg(Context context) {
        if(current_error >= IrdetoErrorCode.ErrorCodePriority.ERR_MAX.getValue())
            return "";
//        Log.d(TAG,"current_error = "+current_error);
//        Log.d(TAG,"ErrorCodePriorityMAP.get(current_error) = "+ErrorCodePriorityMAP.get(current_error));
        return CloakMessages.getMessage(context,ErrorCodePriorityMAP.get(current_error));
    }

    public void register_e52_callback(IrdetoErrorCodeUtils.Callback callback) {
        g_callback = callback;
    }

    public void unregister_e52_callback() {
        g_callback = null;
    }

    public void cleanCaError() {
        ErrorCodePriorityMAP.put(IrdetoErrorCode.ErrorCodePriority.ERR_CA.getValue(), "");
    }
}
