package com.prime.dtv;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.HidlMemory;
import android.os.HidlMemoryUtil;
import android.os.HwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.system.ErrnoException;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;

import androidx.media3.exoplayer.ExoPlayer;

import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.module.AvModule;
import com.prime.dtv.module.BookModule;
import com.prime.dtv.module.CaModule;
import com.prime.dtv.module.CiModule;
import com.prime.dtv.module.CommonModule;
import com.prime.dtv.module.ConfigModule;
import com.prime.dtv.module.CsModule;
import com.prime.dtv.module.DeviceInfoModule;
import com.prime.dtv.module.EpgModule;
import com.prime.dtv.module.FrontEndModule;
import com.prime.dtv.module.LoaderDtvModule;
import com.prime.dtv.module.PioModule;
import com.prime.dtv.module.PipModule;
import com.prime.dtv.module.PmModule;
import com.prime.dtv.module.PvrModule;
import com.prime.dtv.module.SeriesModule;
import com.prime.dtv.module.SsuModule;
import com.prime.dtv.module.SubtitleModule;
import com.prime.dtv.module.TeletextModule;
import com.prime.dtv.module.TestModule;
import com.prime.dtv.module.TimeControlModule;
import com.prime.dtv.module.VmxModule;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.AudioInfo;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.CaStatus;
import com.prime.dtv.sysdata.DefaultChannel;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnAudioTrackMode;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.EnTrickMode;
import com.prime.dtv.sysdata.EnUseGroupType;
import com.prime.dtv.sysdata.FavGroupName;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.LoaderInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.NetProgramInfo;
import com.prime.dtv.sysdata.OTACableParameters;
import com.prime.dtv.sysdata.OTATerrParameters;
import com.prime.dtv.sysdata.PVREncryption;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.PvrFileInfo;
import com.prime.dtv.sysdata.PvrInfo;
import com.prime.dtv.sysdata.Resolution;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.SeriesInfo;
import com.prime.dtv.sysdata.SimpleChannel;
import com.prime.dtv.sysdata.SubtitleInfo;
import com.prime.dtv.sysdata.TeletextInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.sysdata.VMXProtectData;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVScanParams;
import com.prime.dtv.utils.TVTunerParams;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import vendor.prime.hardware.dtvservice.V1_0.HIDL_BUFFER_S;
import vendor.prime.hardware.dtvservice.V1_0.IDtvService;
import vendor.prime.hardware.dtvservice.V1_0.IDtvServiceCallback;

/**
 * Created by ethan_lin on 2017/10/26.
 */

public class PrimeDtvMediaPlayer {
    private static final String TAG = "PrimeDtvMediaPlayer";

    public static final String DTV_INTERFACE_NAME = "prime.dtv.IDtvService";

    public static final int CMD_RETURN_VALUE_FAIL = -1;
    public static final long CMD_RETURN_LONG_VALUE_FAIL = -1;
    public static final int CMD_RETURN_VALUE_SUCCESS = 99;
    public static final boolean ADD_SYSTEM_OFFSET = false;  // connie 20181106 for not add system offset

    private static int g_temp_pesi_default_channel_flag = 0;
    private static final String g_apk_sw_version = "DDN_V7.7.1.2";//centaur 20200619 fix mtest
    //Scoty Add Youtube/Vod Stream -s
    private static Context g_context;
    private static ExoPlayer g_exo_player;
    private static SurfaceView g_surface_view_exoplayer;
    private static WebView g_webview;
    //Scoty Add Youtube/Vod Stream -e
    private static PrimeDtvMediaPlayer g_prime_dtv = null;

    private static IDtvService g_server = null;
    private static DtvServiceCallback g_callback = null;
    public static byte[] g_private_data;//eric lin 20210312 set private data from service

    public static final int CMD_JAVA_Base = 0x10000;
    public static final int CMD_Base = 0x0;

    private EventHandler g_event_handler;
    private ArrayList<EventMapType> g_list_listener_map = null;
    private SurfaceView g_sub_teletext_surfaceview;
    private SurfaceHolder g_sub_surface_holder;
    private Context g_set_surface_context;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    private int g_is_channel_exist = 0;

    private final CommonModule g_common_module;
    private final AvModule g_av_module;
    private final PvrModule g_pvr_module;
    private final EpgModule g_epg_module;
    private final FrontEndModule g_frontend_module;
    private final PmModule g_pm_module;
    private final BookModule g_book_module;
    private final ConfigModule g_config_module;
    private final TimeControlModule g_time_control_module;
    private final CiModule g_ci_module;
    private final SsuModule g_ssu_module;
    private final SubtitleModule g_subtitle_module;
    private final TeletextModule g_teletext_module;
    private final TestModule g_test_module;
    private final CsModule g_cs_module;
    private final PipModule g_pip_module;
    private final VmxModule g_vmx_module;
    private final PioModule g_pio_module;
    private final LoaderDtvModule g_loader_dtv_module;
    private final DeviceInfoModule g_device_info_module;
    private final CaModule g_ca_module;
    private final SeriesModule g_series_module;
    private static DataManager mDataManager;
    private Object mutex = new Object();
    private static int writeHidl_index = 0;

    public static final int EIT_HIDL_BUFF_SIZE = 100;

    private static PesiDtvFrameworkInterface mDtvFramework = null;

    //gary20200807 fix service callback to apk data not correct-s
    public class DtvServiceCallback extends IDtvServiceCallback.Stub {
        @Override
        public void hwNotify(int i, int i1, int i2, int i3, ArrayList<Integer> arrayList) throws RemoteException {
            //Log.d(TAG, "DtvServiceCallback coming !!!!!!!!!");
            if (g_prime_dtv.g_event_handler != null) {
                int j;
                Parcel obj = Parcel.obtain();
                byte[] reply_data = new byte[arrayList.size()];
                for (j = 0; j < arrayList.size(); j++) {
//                    Log.d(TAG, "reply_arr[" + j + "] = " + String.format("0x%08X",reply_arr.get(j)));
                    reply_data[j] = (byte) arrayList.get(j).intValue();
//                    Log.d(TAG, "reply_data[" + j + "] = " + String.format("0x%08X",reply_data[j]));
                }
                obj.unmarshall(reply_data, 0, arrayList.size());
                obj.setDataPosition(0);

                Message m = g_prime_dtv.g_event_handler.obtainMessage(i, i1, i2, obj);
                g_prime_dtv.g_event_handler.sendMessage(m);
            }
        }

        @Override
        public void hwNoitfy_hidl_buffer(HIDL_BUFFER_S hidl_buffer_s) throws RemoteException {
            synchronized (mutex) {
                if (mDataManager == null)
                    mDataManager = DataManager.getDataManager(g_context);
                List<HIDL_BUFFER_S> hidl_buffer_sList = mDataManager.getmHidlBufferList();
                //LogUtils.d("[Ethan]  hidl_buffer_s: [" + hidl_buffer_s.toString() + "]");
                hidl_buffer_sList.add(hidl_buffer_s);
                //LogUtils.d("[Ethan] size = " + hidl_buffer_sList.size());
                if(hidl_buffer_sList.size() == EIT_HIDL_BUFF_SIZE){
                    //if(mDtvFramework != null)
                    //    mDtvFramework.startScheduleEit();
                }
            }
        }
    }
    //gary20200807 fix service callback to apk data not correct-e

    // edwin 20201214 add HwBinder.DeathRecipient -s
    OnServiceDiedListener g_on_service_died_listener;

    public interface OnServiceDiedListener {
        void onServiceDied(long cookie);
    }

    final DeathRecipient g_recipient = new DeathRecipient();

    final class DeathRecipient implements HwBinder.DeathRecipient {
        @Override
        public void serviceDied(long cookie) {
            Log.e(TAG, "[Ethan] DeathRecipient cookie = " + cookie);
            if(mDtvFramework != null)
                mDtvFramework.stopScheduleEit();
            if (mDataManager == null)
                mDataManager = DataManager.getDataManager(g_context);
            List<HIDL_BUFFER_S> hidl_buffer_sList = mDataManager.getmHidlBufferList();
            hidl_buffer_sList.clear();
            reset_dtv_service();
            prepareDTV();
            set_module_type(Pvcfg.getModuleType());
            setup_epg_channel();
            start_epg(0);
            if(Pvcfg.getModuleType() == Pvcfg.MODULE_TBC)
                set_dtv_timezone(8*60*60);
            g_on_service_died_listener.onServiceDied(cookie);
        }
    }
    // edwin 20201214 add HwBinder.DeathRecipient -e

    private void reset_dtv_service() // edwin 20201214 add HwBinder.DeathRecipient
    {
        Log.d(TAG, "reset_dtv_service: ");
        g_server = null;
        if (g_server == null) {
            try {
                g_server = IDtvService.getService(true);
                if (g_server != null) {
                    Log.e(TAG, "reset_dtv_service: g_server != null");
                    g_callback = new DtvServiceCallback();
                    g_server.setCallback(g_callback);
                    link_death_notify();
                }
                else Log.e(TAG, "reset_dtv_service: g_server == null");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void link_death_notify() // edwin 20201214 add HwBinder.DeathRecipient
    {
        Log.d(TAG, "link_death_notify: server = " + g_server);
        if (g_server != null) {
            try {
                g_server.linkToDeath(g_recipient, 0x0 /* cookie */);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unlink_death_notify() // edwin 20201214 add HwBinder.DeathRecipient
    {
        Log.d(TAG, "unlink_death_notify: server = " + g_server);
        if (g_server != null) {
            try {
                g_server.unlinkToDeath(g_recipient);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void set_on_service_died_listener(OnServiceDiedListener listener) {
        g_on_service_died_listener = listener;
    }

    public PrimeDtvMediaPlayer() {
        Log.e(TAG, "new PrimeDtvMediaPlayer");
        g_common_module = new CommonModule();
        g_av_module = new AvModule();
        g_pvr_module = new PvrModule();
        g_epg_module = new EpgModule();
        g_frontend_module = new FrontEndModule();
        g_pm_module = new PmModule();
        g_book_module = new BookModule();
        g_config_module = new ConfigModule();
        g_time_control_module = new TimeControlModule();
        g_ci_module = new CiModule();
        g_ssu_module = new SsuModule();
        g_subtitle_module = new SubtitleModule();
        g_teletext_module = new TeletextModule();
        g_test_module = new TestModule();
        g_cs_module = new CsModule();
        g_pip_module = new PipModule();
        g_vmx_module = new VmxModule();
        g_pio_module = new PioModule();
        g_loader_dtv_module = new LoaderDtvModule();
        g_device_info_module = new DeviceInfoModule();
        g_ca_module = new CaModule();
        g_series_module = new SeriesModule();

        if(mDataManager == null)
            mDataManager = DataManager.getDataManager(g_context);

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            g_event_handler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            g_event_handler = new EventHandler(this, looper);
        } else {
            g_event_handler = null;
        }

        g_list_listener_map = new ArrayList<EventMapType>();
        Log.d(TAG,"new dtvservice init!!!");
        if (g_server == null) {
            try {
                g_server = IDtvService.getService(true);
                if (g_server != null) {
                    g_callback = new DtvServiceCallback();
                    g_server.setCallback(g_callback);
                    link_death_notify();
                }
            } catch (RemoteException | NoSuchElementException e) {
                Log.d(TAG,"new dtvservice fail!!!");
                e.printStackTrace();
            }
        }
        prepareDTV();
        set_module_type(Pvcfg.getModuleType());
        if(Pvcfg.getModuleType() == Pvcfg.MODULE_TBC)
            set_dtv_timezone(8*60*60);
        //start_epg(0);
        //excute_command(CMD_PM_Clear, /*tableType.ordinal()*/2);
    }

    public static long get_unsigned_int(int signedInt) {
        return (signedInt & 0xFFFFFFFFL);
    }

    public void set_channel_exist(int exist) {
        g_is_channel_exist = exist;
    }

    public int get_channel_exist() {
        return g_is_channel_exist;
    }

    public void set_exo_player(ExoPlayer player) {
        g_exo_player = player;
    }

    public ExoPlayer get_exo_player() {
        return g_exo_player;
    }

    public void set_exoplayer_surfaceview(SurfaceView surfaceView) {
        g_surface_view_exoplayer = surfaceView;
    }

    public SurfaceView get_exoplayer_surfaceview() {
        return g_surface_view_exoplayer;
    }

    public void set_youtube_webview(WebView webview) {
        g_webview = webview;
    }

    public WebView get_youtube_webview() {
        return g_webview;
    }

    public int get_temp_pesi_default_channel_flag() {
        return g_temp_pesi_default_channel_flag;
    }

    public void set_temp_pesi_default_channel_flag(int flag) {
        g_temp_pesi_default_channel_flag = flag;
    }

    public String get_apk_sw_version() {
        return g_apk_sw_version;
    }

    public ArrayList<List<SimpleChannel>> get_program_manager_total_channel_list() {
        return g_pm_module.get_total_channel_list();
    }

    public ArrayList<FavGroupName> get_all_program_group() {
        return g_pm_module.get_all_program_group();
    }

//    public void SetViewUiDisplayManager(DTVActivity.ViewUiDisplay viewUiDisplay)
//    {
//        ViewUIDisplayManager = viewUiDisplay;
//    }
//
//    public DTVActivity.ViewUiDisplay GetViewUiDisplayManager()
//    {
//        return ViewUIDisplayManager;
//    }
//
//    public void SetEpgUiDisplayManager(DTVActivity.EpgUiDisplay epgUiDisplay)
//    {
//        EpgUiDisplayManager = epgUiDisplay;
//    }
//
//    public DTVActivity.EpgUiDisplay GetEpgUiDisplayManager(int type)
//    {
//        return EpgUiDisplayManager;
//    }

    public static boolean has_service() // edwin 20200513 add none server function
    {
        return (g_server != null);
    }

    public static PrimeDtvMediaPlayer get_instance(Context context) {
        g_context = context;
        Log.e(TAG, "exce getInstance");
        if (g_prime_dtv == null) {
            Log.e(TAG, "exce getInstance new PrimeDtvMediaPlayer");
            g_prime_dtv = new PrimeDtvMediaPlayer();
//            g_prime_dtv.init_net_program_database();//Init and Save NetPrograms DataBase
//            g_prime_dtv.update_cur_play_channel_list(context, 0);//Scoty 20180615 recover get simple channel list function
//            g_prime_dtv.set_usb_port();
        }
        mDtvFramework = PesiDtvFramework.getInstance(context);
        return g_prime_dtv;
    }

    public static PrimeDtvMediaPlayer get_instance() {
        Log.e(TAG, "getInstance");
        if (g_prime_dtv == null) {
            g_prime_dtv = new PrimeDtvMediaPlayer();
//            g_prime_dtv.update_cur_play_channel_list(0);//Scoty 20180615 recover get simple channel list function
//            g_prime_dtv.set_usb_port();
        }

        return g_prime_dtv;
    }

    /*
          ExcuteCommand -s
      */
    public static int excute_command(int cmd_id) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return get_return_value(ret);
    }

    public static int excute_command(int cmd_id, long arg1) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt((int) arg1);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return get_return_value(ret);
    }

    public static int excute_command(int cmd_id, long arg1, int arg2) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt((int) arg1);
        request.writeInt(arg2);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return get_return_value(ret);
    }

    public static int excute_command(int cmd_id, int arg1, int arg2, int arg3) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt(arg1);
        request.writeInt(arg2);
        request.writeInt(arg3);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return get_return_value(ret);
    }

    public static int excute_command_getII(int cmd_id) {
        int ret = CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            ret = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return ret;
    }

    public static int excute_command_getII(int cmd_id, long arg1) {
        int ret = CMD_RETURN_VALUE_FAIL;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt((int) arg1);

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            ret = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return ret;
    }

    public static int get_return_value(int cmdRetValue) {
        int ret;

        if (CMD_RETURN_VALUE_SUCCESS == cmdRetValue) {
            ret = 0;
        } else if (CMD_RETURN_VALUE_FAIL == cmdRetValue) {
            ret = -1;
        } else {
            ret = cmdRetValue;
        }
        return ret;
    }

    public static void invokeex(Parcel request, Parcel reply) {
//        Log.d(TAG, "invokeex");
        try {
            if (g_server == null) {
                Log.e(TAG, "error !! , server = null");
            } else {
                int i = 0;
//gary20200507 fix client invoke int value to service not correct-s
                request.setDataPosition(0);

                Parcel tmp = Parcel.obtain();
                tmp.appendFrom(request, request.dataPosition(), request.dataAvail());
                tmp.setDataPosition(0);
                String name = tmp.readString();
                int cmd = tmp.readInt();
//                Log.d(TAG, "name = " + name);
//                Log.d(TAG, "cmd = " + cmd);
                request.setDataPosition(0);
                byte[] parcel_rawdata = request.marshall();
//                for (i = 0; i < parcel_rawdata.length; i++) {
//                    Log.d(TAG, "parcel_rawdata[" + i + "] = " + String.format("0x%08X",parcel_rawdata[i]));
//                }
                ArrayList<Integer> arr = new ArrayList<>();
                for (i = 0; i < parcel_rawdata.length; i++) {
                    arr.add((int) parcel_rawdata[i]);
//                    Log.d(TAG, "ArrayList<Integer>[" + i + "] = " + String.format("0x%08X",arr.get(i)));
                }
//                String rawString = new String(parcel_rawdata);
//                String returnData = server.hwInvoke(rawString) ;

                ArrayList<Integer> reply_arr;
                reply_arr = g_server.hwInvoke(arr);
                byte[] reply_data = new byte[reply_arr.size()];
                for (i = 0; i < reply_arr.size(); i++) {
//                    Log.d(TAG, "reply_arr[" + i + "] = " + String.format("0x%08X",reply_arr.get(i)));
                    reply_data[i] = (byte) reply_arr.get(i).intValue();
//                    Log.d(TAG, "reply_data[" + i + "] = " + String.format("0x%08X",reply_data[i]));
                }
                reply.unmarshall(reply_data, 0, reply_arr.size());
//gary20200507 fix client invoke int value to service not correct-e
                reply.setDataPosition(0);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*
      ExcuteCommand -e
      */

    /*
      MessageCallback -s
      */
    private class EventHandler extends Handler {

        public EventHandler(PrimeDtvMediaPlayer dp, Looper looper) {
            super(looper);
//            mDTV = dp;
        }

        public void handleMessage(Message msg) {
            //try
            {
                List<EventMapType> lstEventMap = get_listener_maps(msg.what);
                for (int i = 0; null != lstEventMap && i < lstEventMap.size(); i++) {
                    EventMapType eventMapType = lstEventMap.get(i);
                    if (null != eventMapType) {
                        IDTVListener mEventListener = eventMapType.mEventListener;
                        if (null != mEventListener) {
                            Object object;
                            if (null == msg.obj) {
                                object = new Object();
                            } else {
                                object = msg.obj;
                            }
                            Log.d(TAG, "handleMessage: send notifyMessage " + msg.what);
                            mEventListener.notifyMessage(msg.what, msg.arg1, msg.arg2, object);
                        }
                    }
                }
            }
        }
    }

    private static class EventMapType {
        int mEventType;
        int mPrivateParam;
        IDTVListener mEventListener;

        EventMapType(int EventType, IDTVListener EventListener, int PrivateParam) {
            mEventType = EventType;
            mEventListener = EventListener;
            mPrivateParam = PrivateParam;
        }
    }

    private EventMapType get_listener_map(int eventID, IDTVListener eventListener) {
        if (null == g_list_listener_map || g_list_listener_map.size() <= 0) {
            return null;
        }

        for (int i = 0; i < g_list_listener_map.size(); i++) {
            EventMapType eventMapType = g_list_listener_map.get(i);
            if ((eventID == eventMapType.mEventType) && (eventListener == eventMapType.mEventListener)) {
                return eventMapType;
            }
        }

        return null;
    }

    private List<EventMapType> get_listener_maps(int eventID) {
        List<EventMapType> lstEventMap = null;
        if (null == g_list_listener_map || g_list_listener_map.size() <= 0) {
            return null;
        }

        for (int i = 0; i < g_list_listener_map.size(); i++) {
            EventMapType eventMapType = g_list_listener_map.get(i);
            if (eventID == eventMapType.mEventType) {
                if (null == lstEventMap) {
                    lstEventMap = new ArrayList<EventMapType>();
                }
                lstEventMap.add(eventMapType);
            }
        }

        return lstEventMap;
    }

    public int subscribe_event(int eventID, IDTVListener eventListener, int privateParam) {
//        Log.d(TAG, "JAVA: subScribeEvent(eventID= " + eventID + ")");

        // ignore EventMapType with same eventID and eventListener
        EventMapType eventMapType = get_listener_map(eventID, eventListener);
        if (null != eventMapType) {
            return get_return_value(CMD_RETURN_VALUE_SUCCESS);
        }

//        Log.d(TAG, "add eventMapType to mListnerMap: (eventID= " + eventID + ")");

        g_list_listener_map.add(new EventMapType(eventID, eventListener, privateParam));
        return get_return_value(CMD_RETURN_VALUE_SUCCESS);
    }


    public int unsubscribe_event(int eventID, IDTVListener eventListener) {
//        Log.d(TAG, "UnSubscribeEvent(" + eventID + ")");

        if (null == eventListener) {
            // compatible for old version
            for (int i = 0; null != g_list_listener_map && i < g_list_listener_map.size(); i++) {
                EventMapType eventMapType = g_list_listener_map.get(i);
                if (eventID == eventMapType.mEventType) {
                    g_list_listener_map.remove(eventMapType);
                    i--;
                }
            }
        } else {
            EventMapType eventMapType = get_listener_map(eventID, eventListener);
            if (null != eventMapType) {
                //Log.d(TAG, "unSubScribeEvent: " + mlstListenerMap.size());
                g_list_listener_map.remove(eventMapType);
                //Log.d(TAG, "unSubScribeEvent: " + mlstListenerMap.size());
            } else {
                Log.d(TAG, "mListnerMap == null");
            }
        }

        return get_return_value(CMD_RETURN_VALUE_SUCCESS);

    }
    /*
      MessageCallback -e
      */


    /*
      video playback -s
      */
    public void pip_mod_set_display(Context context, Surface surface, int type) // 1:TimeShift, 0:View //gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        g_pip_module.pip_mod_set_display(context, surface, type);
    }

    // jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem -s
    public void pip_mod_clear_display(Surface surface) {
        g_pip_module.pip_mod_clear_display(surface);
    }

    public int comm_get_wind_handle() {
        return g_common_module.comm_get_wind_handle();
    }

    public int comm_get_timeshift_wind_handle() // Edwin 20181123 to get time shift window handle
    {
        return g_common_module.comm_get_timeshift_wind_handle();
    }

    @SuppressWarnings("deprecation")
    private void init_surface_view(Context context, SurfaceView surfaceView)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        Log.d(TAG, "SUB:=================initSurfaceView====================");
        g_set_surface_context = context;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        g_sub_teletext_surfaceview = surfaceView;
        g_sub_teletext_surfaceview.setVisibility(View.VISIBLE);
        g_sub_surface_holder = g_sub_teletext_surfaceview.getHolder();

        g_sub_surface_holder.setFormat(PixelFormat.TRANSPARENT);
        g_sub_surface_holder.setType(100);

        Log.d(TAG, "SUB:=================mSubSurfaceHolder = " + g_sub_surface_holder);

        g_sub_surface_holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "SUB:=================surfaceChanged====================");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                Log.d(TAG, "SUB:=================surfaceCreated====================");

                g_sub_teletext_surfaceview.setVisibility(View.VISIBLE);

                Log.d(TAG, "SUB:=================begin setSurface====================");
                //setSurface(holder.getSurface()) ;
                pip_mod_set_display(g_set_surface_context, holder.getSurface(), 0);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
                g_sub_surface_holder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "SUB:=================surfaceDestroyed====================");
//                pipModClearDisplay(holder.getSurface());
                pip_mod_set_display(g_set_surface_context, null, 0);// jim 2020/12/16 add set surface to RTK_SerVideoSurfaceEx -s
            }
        });
    }

    public void set_surface_view(Context context, SurfaceView surfaceView)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        init_surface_view(context, surfaceView);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    }
    /*
      video playback -e
      */


    public void start_scan(TVScanParams sp) {
        g_cs_module.start_scan(sp);
    }

    public void stop_scan(boolean store) {
        g_cs_module.stop_scan(store);
    }

    public List<SatInfo> sat_info_get_list(int tunerType, int pos, int num) {
        return g_pm_module.sat_info_get_list(tunerType, pos, num);
    }

    public SatInfo sat_info_get(int satId) {
        return g_pm_module.sat_info_get(satId);
    }

    public int sat_info_add(SatInfo pSat) {
        return g_pm_module.sat_info_add(pSat);
    }

    public int sat_info_update(SatInfo pSat) {
        return g_pm_module.sat_info_update(pSat);
    }

    public int sat_info_update_list(List<SatInfo> pSats) {
        return g_pm_module.sat_info_update_list(pSats);
    }

    public int sat_info_delete(int satId) {
        return g_pm_module.sat_info_delete(satId);
    }

    public List<TpInfo> tp_info_get_list_by_satId(int tunerType, int satId, int pos, int num) {
        return g_pm_module.tp_info_get_list_by_sat_id(tunerType, satId, pos, num);
    }

    public TpInfo tp_info_get(int tp_id) {
        return g_pm_module.tp_info_get(tp_id);
    }

    public int tp_info_add(TpInfo pTp) {
        return g_pm_module.tp_info_add(pTp);
    }

    public int tp_info_update(TpInfo pTp) {
        return g_pm_module.tp_info_update(pTp);
    }

    public int tp_info_update_list(List<TpInfo> pTps) {
        return g_pm_module.tp_info_update_list(pTps);
    }

    public int tp_info_delete(int tpId) {
        return g_pm_module.tp_info_delete(tpId);
    }

    public int av_control_play_by_channel_id(int playId, long channelId, int groupType, int show) {
        Log.d(TAG, "AvControlPlayByChannelId: channelId=" + channelId);
        return g_av_module.av_control_start(playId, channelId, show);
    }

    public int av_control_pre_play_stop() {
        Log.d(TAG, "AvControlPrePlayStop: ");
        return g_av_module.av_control_pre_play_stop();
    }

    public int av_control_play_stop(int playId) {
        Log.d(TAG, "AvControlPlayStop: ");
        return g_av_module.av_control_stop(playId);
    }

    public int av_control_change_ratio_conversion(int playId, int ratio, int conversion) {
        Log.d(TAG, "AvControlChangeRatioConversion: ");
        return g_av_module.set_ratio_conversion(playId, ratio, conversion);
    }

    public int av_control_set_fast_change_channel(long PreChannelId, long NextChannelId)//Scoty 20180816 add fast change channel
    {
        return g_av_module.set_fast_change_channel(PreChannelId, NextChannelId);
    }

    public int av_control_change_resolution(int playId, int resolution) {
        return 0;
    }

    public int av_control_change_audio(int playId, AudioInfo.AudioComponent component) {
        Log.d(TAG, "AvControlChangeAudio: ");
        return g_av_module.select_audio(playId, component);
    }

    public int av_control_set_mute(int playId, boolean mute) {
        Log.d(TAG, "AvControlSetMute: ");
        return g_av_module.set_mute_status(playId, mute);
    }

    public int av_control_set_track_mode(int playId, EnAudioTrackMode stereo) {
        Log.d(TAG, "AvControlSetTrackMode: ");
        return g_av_module.set_track_mode(playId, stereo);
    }

    public int av_control_audio_output(int playId, int byPass) {
        return g_av_module.set_audio_output_mode(playId, byPass);
    }

    public int av_control_close(int playId) {
        Log.d(TAG, "AvControlClose: ");
        return g_av_module.av_control_close(playId, 0);
    }

    public int av_control_open(int playId) {
        Log.d(TAG, "AvControlOpen: ");
        return g_av_module.av_control_open(playId);
    }

    public int av_control_show_video(int playId, boolean show) {
        Log.d(TAG, "AvControlShowVideo: ");
        return g_av_module.show_video(playId, show);
    }

    public int av_control_freeze_video(int playId, boolean freeze) {
        Log.d(TAG, "AvControlFreezeVideo: ");
        return g_av_module.freeze_video(playId, freeze);
    }

    //return value shoulde be check
    public AudioInfo av_control_get_audio_list_info(int playId) {
        Log.d(TAG, "AvControlGetAudioListInfo: ");
        return g_av_module.get_current_audio(playId);
    }

    /* return status =  LIVEPLAY,TIMESHIFTPLAY.....etc  */
    public int av_control_get_play_status(int playId) {
        Log.d(TAG, "AvControlGetPlayStatus: ");
        return g_av_module.get_play_status(playId).getValue();
    }

    public boolean av_control_get_mute(int playId) {
        Log.d(TAG, "AvControlGetMute: ");
        return g_av_module.get_mute_status(playId);
    }

    public EnAudioTrackMode av_control_get_track_mode(int playId) {
        Log.d(TAG, "AvControlGetTrackMode: ");
        return g_av_module.get_track_mode(playId);
    }

    public int av_control_get_ratio(int playId) {
        Log.d(TAG, "AvControlGetRatio: ");
        return g_av_module.get_ratio(playId);
    }

    public int av_control_set_stop_screen(int playId, int stopType) {
        Log.d(TAG, "AvControlSetStopScreen: ");
        return g_av_module.set_stop_mode(playId, stopType);
    }

    public int av_control_get_stop_screen(int playId) {
        Log.d(TAG, "AvControlGetStopScreen: ");
        return g_av_module.av_get_stop_mode(playId);
    }

    public int av_control_get_fps(int playId) {
        Log.d(TAG, "AvControlGetFPS: ");
        return g_av_module.get_fps(playId);
    }

    public int av_control_ews_action_control(int playId, boolean enable) {
        Log.d(TAG, "AvControlEwsActionControl: ");
        return g_av_module.ews_action_control(playId, enable);
    }

    //input value shoulde be check
    public int av_control_set_window_size(int playId, Rect rect) {
        Log.d(TAG, "AvControlSetWindowSize: ");
        return g_av_module.set_window_rect(playId, rect);
    }

    //return value shoulde be check
    public Rect av_control_get_window_size(int playId) {
        Log.d(TAG, "AvControlGetWindowSize: ");
        return g_av_module.get_window_size(playId);
    }

    public int av_control_get_video_resolution_height(int playId) {
        Log.d(TAG, "AvControlGetVideoResolutionHeight: ");
        return g_av_module.get_video_resolution_height(playId);
    }

    public int av_control_get_video_resolution_width(int playId) {
        Log.d(TAG, "AvControlGetVideoResolutionWidth: ");
        return g_av_module.get_video_resolution_width(playId);
    }

    /* 0: dolby digital, 1: dolby digital plus */
    public int av_control_get_dolby_info_stream_type(int playId) {
        Log.d(TAG, "AvControlGetDolbyInfoStreamType: ");
        return g_av_module.get_dolby_info_stream_type(playId);
    }

    /**
     * get dolby acmod.<br>
     *
     * @return 0: "1+1"; 1: "1/0"; 2: "2/0"; 3: "3/0"; 4:"2/1"; 5:"3/1"; 6:"2/2"; 7:"3/2"; other：error<br>
     * CN:0: "1+1"; 1: "1/0"; 2: "2/0"; 3: "3/0"; 4:"2/1"; 5:"3/1"; 6:"2/2"; 7:"3/2";?��?，�?误�?br>
     */
    public int av_control_get_dolby_info_acmod(int playId) {
        Log.d(TAG, "AvControlGetDolbyInfoAcmod: ");
        return g_av_module.get_dolby_info_acmod(playId);
    }

    public SubtitleInfo.SubtitleComponent av_control_get_current_subtitle(int playId) {
        return g_subtitle_module.av_control_get_current_subtitle(playId);
    }

    public SubtitleInfo av_control_get_subtitle_list(int playId) {
        return g_subtitle_module.av_control_get_subtitle_list(playId);
    }

    public int av_control_select_subtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent) {
        return g_subtitle_module.av_control_select_subtitle(playId, subtitleComponent);
    }

    public int av_control_show_subtitle(int playId, boolean enable) {
        return g_subtitle_module.av_control_show_subtitle(playId, enable);
    }

    public boolean av_control_is_subtitle_visible(int playId) {
        return g_subtitle_module.av_control_is_subtitleVisible(playId);
    }

    public int av_control_set_subt_hoh_preferred(int playId, boolean on) {
        return g_subtitle_module.av_control_set_subt_hoh_preferred(playId, on);
    }

    public int av_control_set_subtitle_language(int playId, int index, String lang) {
        return g_subtitle_module.av_control_set_subtitle_language(playId, index, lang);
    }

    public TeletextInfo av_control_get_current_teletext(int playId) {
        return g_teletext_module.av_control_get_current_teletext(playId);
    }

    public List<TeletextInfo> av_control_get_teletext_list(int playId) {
        return g_teletext_module.av_control_get_teletext_list(playId);
    }

    public int av_control_show_teletext(int playId, boolean enable) {
        return g_teletext_module.av_control_show_teletext(playId, enable);
    }

    public boolean av_control_is_teletext_visible(int playId) {
        return g_teletext_module.av_control_is_teletext_visible(playId);
    }

    public boolean av_control_is_teletext_available(int playId) {
        return g_teletext_module.av_control_is_teletext_available(playId);
    }

    public int av_control_set_teletext_language(int playId, String primeLang) {
        return g_teletext_module.av_control_set_teletext_language(playId, primeLang);
    }

    public String av_control_get_teletext_language(int playId) {//eric lin 20180705 get ttx lang
        return g_teletext_module.av_control_get_teletext_language(playId);
    }

    public int av_control_set_command(int playId, int keyCode) {
        return g_teletext_module.av_control_set_command(playId, keyCode);
    }

    public int update_usb_software(String filename) {
        return g_ssu_module.update_usb_software(filename);
    }

    public int update_file_system_software(String pathAndFileName, String partitionName) {
        return g_ssu_module.update_file_system_software(pathAndFileName, partitionName);
    }

    public int update_ota_dvbc_software(int tpId, int freq, int symbol, int qam) {
        return g_ssu_module.update_ota_dvbc_software(tpId, freq, symbol, qam);
    }

    public int update_ota_dvbt_software(int tpId, int freq, int bandwith, int qam, int priority) {
        return g_ssu_module.update_ota_dvbt_software(tpId, freq, bandwith, qam, priority);
    }

    public int update_ota_dvbt2_software(int tpId, int freq, int bandwith, int qam, int channelmode) {
        return g_ssu_module.update_ota_dvbt2_software(tpId, freq, bandwith, qam, channelmode);
    }

    public int update_ota_isdbt_software(int tpId, int freq, int bandwith, int qam, int priority) {
        return g_ssu_module.update_ota_isdbt_software(tpId, freq, bandwith, qam, priority);
    }

    public int update_mtest_ota_software()//Scoty 20190410 add Mtest Trigger OTA command
    {
        return g_ssu_module.update_mtest_ota_software();
    }

    public int get_dtv_timezone() {
        return g_time_control_module.get_dtv_timezone();
    }

    public int set_dtv_timezone(int zonesecond) {
        return g_time_control_module.set_dtv_timezone(zonesecond);
    }

    public int get_dtv_daylight()//value: 0(off) or 1(on)
    {
        return g_time_control_module.get_dtv_daylight();
    }

    public int set_dtv_daylight(int onoff)//value: 0(off) or 1(on)
    {
        return g_time_control_module.set_dtv_daylight(onoff);
    }

    public int get_setting_tdt_status() {
        return g_time_control_module.get_setting_tdt_status();
    }

    public int set_setting_tdt_status(int onoff)//value: 0(off) or 1(on)
    {
        return g_time_control_module.set_setting_tdt_status(onoff);
    }

    public int set_time_to_system(boolean bSetTimeToSystem) {
        return g_time_control_module.set_time_to_system(bSetTimeToSystem);
    }

    public Date second_to_date(int isecond) {
        return g_time_control_module.second_to_date(isecond);
    }

    public int date_to_second(Date date) {
        return g_time_control_module.date_to_second(date);
    }

    public int mtest_enable_opt(boolean enable) {
        return g_ssu_module.mtest_enable_opt(enable);
    }

    public OTACableParameters dvb_get_ota_cable_paras() {
        return g_ssu_module.dvb_get_ota_cable_paras();
    }

    public OTATerrParameters dvb_get_ota_isdbt_paras() {
        return g_ssu_module.dvb_get_ota_isdbt_paras();
    }


    public OTATerrParameters dvb_get_ota_terrestrial_paras() {
        return g_ssu_module.dvb_get_ota_terrestrial_paras();
    }

    public OTATerrParameters dvb_get_ota_dvbt2_paras() {
        return g_ssu_module.dvb_get_ota_dvbt2_paras();
    }

    public List<BookInfo> init_ui_book_list() {//Init UI Book List after boot
        return g_book_module.init_ui_book_list();
    }

    public List<BookInfo> get_ui_book_list() {
        return g_book_module.get_ui_book_list();
    }

    public List<BookInfo> book_info_get_list() {
        return g_book_module.book_info_get_list();
    }

    public BookInfo book_info_get(int bookId) {
        return g_book_module.book_info_get(bookId);
    }

    public int book_info_add(BookInfo bookInfo) {
        return g_book_module.book_info_add(bookInfo);
    }

    public int book_info_update(BookInfo bookInfo) {
        return g_book_module.book_info_update(bookInfo);
    }

    public int book_info_update_list(List<BookInfo> bookList) {
        return g_book_module.book_info_update_list(bookList);
    }

    public int book_info_delete(int bookId) {
        return g_book_module.book_info_delete(bookId);
    }

    public int book_info_delete_all() {
        return g_book_module.book_info_delete_all();
    }

    public BookInfo book_info_get_coming_book() {
        return g_book_module.book_info_get_coming_book();
    }

    public List<BookInfo> book_info_find_conflict_books(BookInfo bookInfo) {
        return g_book_module.book_info_find_conflict_books(bookInfo);
    }

    public int tuner_lock(TVTunerParams tunerParams) {
        return g_frontend_module.tuner_lock(tunerParams);
    }

    public boolean get_tuner_status(int tuner_id) {
        return g_frontend_module.is_tuner_lock(tuner_id);
    }

    public int get_signal_strength(int nTunerID) {
        return g_frontend_module.get_signal_strength(nTunerID);
    }

    public int get_signal_quality(int nTunerID) {
        return g_frontend_module.get_signal_quality(nTunerID);
    }

    public int get_signal_snr(int nTunerID) {
        return g_frontend_module.get_signal_snr(nTunerID);
    }

    public String get_signal_ber(int nTunerID) {
        return g_frontend_module.get_signal_ber(nTunerID);
    }

    public int set_fake_tuner(int openFlag)//Scoty 20180809 add fake tuner command
    {
        return g_frontend_module.set_fake_tuner(openFlag);
    }

    public int tuner_set_antenna_5v(int tuner_id, int onOff) {
        return g_frontend_module.set_antenna_5v(tuner_id, onOff);
    }

    public int get_tuner_type() {
        return g_common_module.get_tuner_type();
    }


    public int save_table(EnTableType tableType) {
        return g_pm_module.save_table(tableType);
    }

    public int clear_table(EnTableType tableType) {
        return g_pm_module.clear_table(tableType);
    }

    public int restore_table(EnTableType tableType) {
        return g_pm_module.restore_table(tableType);
    }

    public int saveNetworks() {
        return save_table(EnTableType.ALL);
    }

    public int get_default_open_group_type() {
        return g_pm_module.get_default_open_group_type();
    }

    public int get_channel_count(int groupType) {
        return g_pm_module.get_channel_count(groupType);
    }

    public List<Integer> get_use_groups(EnUseGroupType useGroupType) {
        return g_pm_module.get_use_groups(useGroupType);
    }

    public int rebuild_all_group() {
        return g_pm_module.rebuild_all_group();
    }

    public String get_channel_group_name(int groupType) {
        return g_pm_module.get_channel_group_name(groupType);
    }

    public int set_channel_group_name(int groupType, String name) {
        return g_pm_module.set_channel_group_name(groupType, name);
    }

    public int del_channel_by_tag(int u32ProgTag) {
        return g_pm_module.del_channel_by_tag(u32ProgTag);
    }

    public int set_module_type(int type){
        return g_common_module.set_module_type(type);
    }
    private int prepareDTV() {
        return g_common_module.prepare_dtv();
    }

    public List<ProgramInfo> get_program_info_list(int type, int pos, int num) {
        return g_pm_module.get_program_info_list(type, pos, num);
    }

    //Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
    public List<SimpleChannel> get_simple_program_list_from_total_channel_list(int type, int IncludeSkipFlag, int IncludePVRSkipFlag) {
        return g_pm_module.get_simple_program_list_from_total_channelList(type, IncludeSkipFlag, IncludePVRSkipFlag);
    }

    public List<SimpleChannel> get_simple_program_list(int type, int IncludeSkipFlag, int IncludePVRSkipFlag) {
        return g_pm_module.get_simple_program_list(type, IncludeSkipFlag, IncludePVRSkipFlag);
    }

    public ProgramInfo get_program_by_lcn(int lcn, int type) {
        return g_pm_module.get_program_by_lcn(lcn, type);
    }

    public ProgramInfo get_program_by_ch_num(int chnum, int type) {
        return g_pm_module.get_program_by_chnum(chnum, type);
    }

    public ProgramInfo get_program_by_channel_id(long channelId) {
        return g_pm_module.get_program_by_channel_id(channelId);
    }

    public SimpleChannel get_simple_program_by_channel_id(long channelId) {
        return g_pm_module.get_simple_program_by_channel_id(channelId);
    }

    public SimpleChannel get_simple_program_by_channel_id_from_total_channel_list_by_group(int groupType, long channelId) {
        return g_pm_module.get_simple_program_by_channel_id_from_total_channel_list_by_group(groupType, channelId);
    }

    public SimpleChannel get_simple_program_by_channel_id_from_total_channel_list(long channelId) {

        return g_pm_module.get_simple_program_by_hannel_id_from_total_channel_list(channelId);

    }

    public void delete_program(long channelId) {
        g_pm_module.delete_program(channelId);
    }

    public int set_default_open_channel(long channelId, int groupType) {
        return g_pm_module.set_default_open_channel(channelId, groupType);
    }

    public DefaultChannel get_default_channel() {
        return g_pm_module.get_default_channel();
    }

    public int update_sat_list(List<SatInfo> satInfoList) {
        return g_pm_module.update_satList(satInfoList);
    }

    public int update_tp_list(List<TpInfo> tpInfoList) {
        return g_pm_module.update_tp_list(tpInfoList);
    }

    public int update_simple_channel_list(List<SimpleChannel> simpleChannelList, int type) {
        return g_pm_module.update_simple_channel_list(simpleChannelList, type);
    }

    public int update_program_info(ProgramInfo pProgram) {
        return g_pm_module.update_program_info(pProgram);
    }

    public int fav_info_update_list(int favMode, List<FavInfo> favInfo) {
        return g_pm_module.fav_info_update_list(favMode, favInfo);
    }

    public int update_book_list(List<BookInfo> bookInfoList) // Need command and implement
    {
        return g_book_module.update_book_list(bookInfoList);
    }

    // ========EPG==========

    public int start_epg(long channelID) {
        return g_epg_module.start_epg(channelID);
    }

    public EPGEvent get_present_event(long channelID) {
        return g_epg_module.get_present_event(channelID);
    }

    public EPGEvent get_follow_event(long channelID) {
        return g_epg_module.get_follow_event(channelID);
    }

    public EPGEvent[] get_present_follow_event(long channelID) {
        return g_epg_module.get_present_follow_event(channelID);
    }

    public EPGEvent get_epg_by_event_id(long channelID, int eventId) {
        return g_epg_module.get_epg_by_event_id(channelID, eventId);
    }

    public List<EPGEvent> get_epg_events(long channelID, Date startTime, Date endTime, int pos, int reqNum, int addEmpty) {
        return g_epg_module.get_epg_events(channelID, startTime, endTime, pos, reqNum, addEmpty);
    }

    public String get_short_description(long channelId, int eventId) {
        return g_epg_module.get_short_description(channelId, eventId);
    }

    public String get_detail_description(long channelId, int eventId) {
        return g_epg_module.get_detail_description(channelId, eventId);
    }

    public int set_event_lang(String firstEvtLang, String secondEvtLang) {
        return g_epg_module.set_event_lang(firstEvtLang, secondEvtLang);
    }

    private int writeDatatoHidlMemory(HidlMemory mem,byte[] data, int lens){
        int ret = 0;
        SharedMemory sharedMemory = null;

        FileDescriptor fd = null;
        ParcelFileDescriptor pfd = null;
        try {
            fd = mem.dup().getHandle().getFileDescriptor();
            pfd = ParcelFileDescriptor.dup(fd);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                sharedMemory = SharedMemory.fromFileDescriptor(pfd);
            }
            //LogUtils.d("[Ethan] fd = "+fd+" lens = "+lens);
            if (fd == null) {
                LogUtils.e("[Ethan] FileDescriptor is null");
                return ret;
            }
            // 獲取 HidlMemory 的大小
            int size = (int) mem.getSize(); // 假設 HidlMemory 有一個方法可以獲取大小
            //LogUtils.d("[Ethan] size = "+size);
            if (size <= 0) {
                LogUtils.e("[Ethan] HidlMemory size is invalid: " + size);
                return ret;
            }
            if(lens > size){
                LogUtils.e("[Ethan] data lens is invalid: " + lens);
                return ret;
            }
            //LogUtils.d("[Ethan] sharedMemory "+sharedMemory);
            if (sharedMemory != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                ByteBuffer buffer = sharedMemory.mapReadWrite();
                buffer.put(data, 0, lens);
                buffer.flip();
                //LogUtils.d("[Ethan] buffer "+buffer.toString());
                ret = 1;
            }
        } catch (IOException e) {
            LogUtils.e("[Ethan] Can't write buffer messge"+e.getMessage());
            throw new RuntimeException(e);
        } catch (ErrnoException e) {
            LogUtils.e("[Ethan] fd "+fd+" pfd= "+pfd);
            throw new RuntimeException(e);
        } catch ( BufferOverflowException e) {
            LogUtils.e("[Ethan] buffer overflow  lens = "+lens);
            throw new RuntimeException(e);
        }
        return ret;
    }
    private void readDataFromHidlMemory(HidlMemory mem,int offset, int len){
        // 確保 len 和 offset 在合理範圍內
        if (offset < 0 || len <= 0) {
            LogUtils.d("[Ethan] Invalid offset or length");
            return;
        }

        // 獲取 HidlMemory 的文件描述符
        FileDescriptor fd = mem.getHandle().getFileDescriptor();
        byte[] data = new byte[len];

        LogUtils.d("[Ethan] fd = "+fd);
        try (FileInputStream fis = new FileInputStream(fd)) {
            // 讀取數據
            int bytesRead = fis.read(data);
            LogUtils.d("[Ethan] bytesRead = "+bytesRead + " len = "+len);
            if (bytesRead <= 0) {
                LogUtils.d("[Ethan] No data read or end of stream reached");
                return;
            }

            // 確保不超出數據的範圍
            //if (offset + len > bytesRead) {
            //    LogUtils.d("[Ethan] Offset and length exceed the number of bytes read");
            //    return;
            //}

            // 日誌輸出讀取的數據
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len-offset; i++) {
                sb.append(data[offset + i]).append(" ");
            }
            LogUtils.d("[Ethan] read data: " + sb.toString());

        } catch (IOException e) {
            LogUtils.d("[Ethan] read fd fail ");
            e.printStackTrace();
        }

        // 如果需要進一步處理 HIDL 內存數據
        byte[] hdil_byte_data2 = HidlMemoryUtil.hidlMemoryToByteArray(mem);
        if (hdil_byte_data2 != null && offset + len <= hdil_byte_data2.length) {
            StringBuilder sb2 = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb2.append(hdil_byte_data2[offset + i]).append(" ");
            }
            LogUtils.d("[Ethan] hdil_byte_data2: " + sb2.toString());
        } else {
            LogUtils.d("[Ethan] Invalid access to hdil_byte_data2");
        }

    }
    public int send_eit_rowdata( byte[] data, int lens) {
        synchronized (mutex) {
            int i, offset = 1, len2 = lens + offset, ret = 0;
            if (mDataManager == null)
                mDataManager = DataManager.getDataManager(g_context);
            //if(test_send == 1)
            //    return 0;
            List<HIDL_BUFFER_S> bufferList = mDataManager.getmHidlBufferList();
            //LogUtils.d("[Ethan] data: "+data[0]+" "+data[1]+" "+data[2]+" "+data[3]+" "+data[4]+" "+data[5]+" "+data[6]+" "+data[7]+" "+data[8]+" "+data[9]+" "+data[10]);
            //LogUtils.d("[Ethan] data: "+data[lens-1]+" "+data[lens-2]+" "+data[lens-3]+" "+data[lens-4]+" "+data[lens-5]+" "+data[lens-6]+" "+data[lens-7]+" "+data[lens-8]+" "+data[lens-9]+" "+data[lens-10]+" "+data[lens-11]);
            //for (i = 0; i < bufferList.size(); i++)
            if(bufferList.size() > writeHidl_index)
            {
                HIDL_BUFFER_S hidl_buff = bufferList.get(writeHidl_index);
                //byte[] hdil_byte_data = HidlMemoryUtil.hidlMemoryToByteArray(hidl_buff.data);
                //byte[] data_1 = new byte[lens + offset];
                //LogUtils.d("[Ethan] len = "+hdil_byte_data.length);
                //int isUse = hdil_byte_data[0];//getInt(hdil_byte_data, 8, 4, MASK_16BITS);
                //LogUtils.d("[Ethan] i = "+i+" IsUse = "+isUse);
                //if (isUse == 0) {
                    //data_1[0] = 1;
                    //System.arraycopy(data, 0, data_1, offset, lens);
                    //System.arraycopy(hdil_byte_data,12, data,0, lens);
                    //LogUtils.d("[Ethan] data_1: "+data_1[offset]+" "+data_1[offset+1]+" "+data_1[offset+2]+" "+data_1[offset+3]+" "+data_1[offset+4]+" "+data_1[offset+5]+" "+data_1[offset+6]+" "+data_1[offset+7]+" "+data_1[offset+8]+" "+data_1[offset+9]+" "+data_1[offset+10]);
                    //LogUtils.d("[Ethan] data_1: "+data_1[len2-1]+" "+data_1[len2-2]+" "+data_1[len2-3]+" "+data_1[len2-4]+" "+data_1[len2-5]+" "+data_1[len2-6]+" "+data_1[len2-7]+" "+data_1[len2-8]+" "+data_1[len2-9]+" "+data_1[len2-10]+" "+data_1[len2-11]);
                    if(hidl_buff != null)
                        ret = writeDatatoHidlMemory(hidl_buff.data, data, lens);

                    //readDataFromHidlMemory(hidl_buff.data, offset, len2);
                    //hdil_byte_data[11] = 1;
                    //test_send = 1;
                    //LogUtils.d("[Ethan] send data table id "+data[0]+" index = "+writeHidl_index);
                    //break;
                    //if(ret == 1)
                    //    ret = g_epg_module.send_eit_rowdata(writeHidl_index, lens);
                    writeHidl_index ++;
                    if(writeHidl_index == EIT_HIDL_BUFF_SIZE)
                        writeHidl_index = 0;
                //}
            }
            //if (i == bufferList.size()) {
            //    LogUtils.d("[Ethan] HIDL buffer full");
            //    return 0;
            //}
            return ret;
        }
    }
    public int send_epg_data_id(List<ProgramInfo> programInfo) {
        return g_epg_module.send_epg_data_id(programInfo);
    }

    public int add_epg_data_id(long channelId, int sid, int tid, int onid){
        return g_epg_module.add_epg_data_id(channelId,sid,tid,onid);
    }

    public int delete_epg_data_id(long channelId, int sid, int tid, int onid){
        return g_epg_module.delete_epg_data_id(channelId, sid, tid, onid);
    }

    public int set_time(Date date) {
        return g_time_control_module.set_time(date);
    }

    public Date get_dtv_date() {
        return g_time_control_module.get_dtv_date();
    }

    public int sync_time(boolean bEnable) {
        return g_time_control_module.sync_time(bEnable);
    }

    public FavInfo fav_info_get(int favMode, int index) {
        return g_pm_module.fav_info_get(favMode, index);
    }

    public List<FavInfo> fav_info_get_list(int favMode) {
        return g_pm_module.fav_info_get_list(favMode);
    }

    public int fav_info_delete(int favMode, long channelId) {
        return g_pm_module.fav_info_delete(favMode, channelId);
    }

    public int fav_info_delete_all(int favMode) {
        return g_pm_module.fav_info_delete_all(favMode);
    }

    public String fav_group_name_get(int favMode) {
        return g_pm_module.fav_group_name_get(favMode);
    }

    public int fav_group_name_update(int favMode, String name) {
        return g_pm_module.fav_group_name_update(favMode, name);
    }

    public GposInfo gpos_info_get() {
        return g_config_module.gpos_info_get();
    }

    public void gpos_info_update(GposInfo gPos) {
        g_config_module.gpos_info_update(gPos);
    }


    public void gpos_info_update_by_key_string(String key, String value) {
        g_config_module.gpos_info_update_by_key_string(key, value);
    }

    public void gpos_info_update_by_key_string(String key, int value) {
        g_config_module.gpos_info_update_by_key_string(key, value);
    }

    public int reset_factory_default() {
        return g_config_module.reset_factory_default();
    }

    public String get_pesi_service_version() {
        return g_common_module.get_pesi_service_version();
    }

    public int mtest_get_gpio_status(int u32GpioNo) {
        return g_test_module.mtest_get_gpio_status(u32GpioNo);
    }

    public int mtest_set_gpio_status(int u32GpioNo, int bHighVolt) {
        return g_test_module.mtest_set_gpio_status(u32GpioNo, bHighVolt);
    }

    public int mtest_get_atr_status(int smartCardStatus) {
        return g_test_module.mtest_get_atr_status(smartCardStatus);
    }

    public int mtest_get_hdcp_status() {
        return g_test_module.mtest_get_hdcp_status();
    }

    public int mtest_get_hdmi_status() {
        return g_test_module.mtest_get_hdmi_status();
    }

    public int mtest_power_save() {
        return g_test_module.mtest_power_save();
    }

    public int mtest_seven_segment(int enable) {
        return g_test_module.mtest_seven_segment(enable);
    }

    public int mtest_set_antenna_5v(int tunerID, int tunerType, int enable) {
        return g_pio_module.set_antenna_5v(tunerID, tunerType, enable);
    }

    public int mtest_set_buzzer(int enable) {
        return g_pio_module.set_buzzer(enable);
    }

    public int mtest_set_led_red(int enable) {
        return g_pio_module.set_led_red(enable);
    }

    public int mtest_set_led_green(int enable) {
        return g_pio_module.set_led_green(enable);
    }

    public int mtest_set_led_orange(int enable) {
        return g_pio_module.set_led_orange(enable);
    }

    public int mtest_set_led_white(int enable) {
        return g_pio_module.set_led_white(enable);
    }

    public int mtest_set_led_on_off(int status) {
        return g_test_module.mtest_set_led_on_off(status);

    }

    public int mtest_get_front_key(int key) {
        return g_test_module.mtest_get_front_key(key);

    }

    public int mtest_set_usb_power(int enable) {
        return g_pio_module.set_usb_power(enable);
    }

    public int mtest_test_usb_read_write(int portNum, String path) {
        return g_test_module.mtest_test_usb_read_write(portNum, path);
    }

    // Johnny 20181221 for mtest split screen
    public int mtest_test_av_multi_play(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
        return g_test_module.mtest_test_av_multi_play(tunerNum, tunerIDs, channelIDs);
    }

    public int mtest_test_av_stop_by_tuner_id(int tunerID) {
        return g_test_module.mtest_test_av_stop_by_tuner_id(tunerID);
    }

    public int mtest_mic_set_input_gain(int value) {
        return g_av_module.av_mic_set_input_gain(value);
    }

    public int mtest_mic_set_lr_input_gain(int l_r, int value) {
        return g_av_module.av_mic_set_lr_input_gain(l_r, value);
    }

    public int mtest_mic_set_alc_gain(int value) {
        return g_av_module.av_mic_set_alc_gain(value);
    }

    public int mtest_get_error_frame_count(int tunerID) {
        return g_av_module.av_get_error_frame_count(tunerID);
    }

    public int mtest_get_frame_drop_count(int tunerID) {
        return g_av_module.av_get_frame_drop_count(tunerID);
    }

    public String mtest_get_chip_id() {
        return g_config_module.get_chip_id();
    }

    public int mtest_start_mtest(String version) {
        return g_test_module.mtest_start_mtest(version);
    }

    public int mtest_connect_pctool() {
        return g_test_module.mtest_connect_pctool();
    }

    public List<Integer> mtest_get_wifi_tx_rx_level() {//Scoty 20190417 add wifi level command
        return g_test_module.mtest_get_wifi_tx_rx_level();
    }

    public int mtest_get_wakeup_mode() {
        return g_device_info_module.get_wakeup_mode();
    }

    // Johnny 20190522 check key before OTA
    public Map<String, Integer> mtest_get_key_status_map() {
        return g_test_module.mtest_get_key_status_map();
    }

    public int test_set_tv_radio_count(int tvCount, int radioCount) {
        return g_test_module.test_set_tv_radio_count(tvCount, radioCount);
    }

    public int test_change_tuner(int tunerTpe)//Scoty 20180817 add Change Tuner Command
    {
        return g_test_module.test_change_tuner(tunerTpe);
    }

    ///////// for pesi middleware---pesimain /////////
    //PIP -start
    public int pip_open(int x, int y, int width, int height) {
        return g_pip_module.pip_open(x, y, width, height);
    }

    public int pip_close() {
        return g_pip_module.pip_close();
    }

    public int pip_start(long channelId, int show) {
        return g_pip_module.pip_start(channelId, show);
    }

    public int pip_stop() {
        return g_pip_module.pip_stop();
    }

    public int pip_set_window(int x, int y, int width, int height) {
        return g_pip_module.pip_set_window(x, y, width, height);
    }

    public int pip_exchange() {
        return g_pip_module.pip_exchange();
    }
    //PIP -end

    //Device -start
    public void set_usb_port() {
        g_device_info_module.set_usb_port();
    }

    public List<Integer> get_usb_port_list() {
        return g_device_info_module.get_usb_port_list();
    }
    //Device -end

    public List<SimpleChannel> get_cur_play_channel_list(int type, int includePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return g_pm_module.get_cur_play_channel_list(type, includePVRSkipFlag);
    }

    public int get_cur_play_channel_list_cnt(int type) {//eric lin 20180802 check program exist
        return g_pm_module.get_cur_play_channel_list_cnt(type);
    }

    public void reset_total_channel_list() {
        g_pm_module.reset_total_channel_list();
    }

    public void update_cur_play_channel_list(Context context, int includePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        g_pm_module.update_cur_play_channel_list(context, includePVRSkipFlag);
    }
    //Scoty Add ProgramInfo and NetProgramInfo Get TotalChannelList -e

    public void update_cur_play_channel_list(int includePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        g_pm_module.update_cur_play_channel_list(includePVRSkipFlag);
    }

    public int get_platform() {
        return g_common_module.get_platform();
    }

    public int get_tuner_num()//Scoty 20181113 add GetTunerNum function
    {
        return g_common_module.get_tuner_num();
    }

    //Scoty 20180809 modify dual pvr rule -s//Scoty 20180615 update TV/Radio TotalChannelList -s
    public void update_pvr_skip_list(int groupType, int includePVRSkipFlag, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList
    {
        g_pm_module.update_pvr_skip_list(groupType, includePVRSkipFlag, tpId, pvrTpList);
    }
    //Scoty 20180809 modify dual pvr rule -e//Scoty 20180615 update TV/Radio TotalChannelList -e

    // pvr -start
    public int record__start_v2_with_duration(long channelId, int durationSec, boolean doCipher, PVREncryption pvrEncryption) {
        return g_pvr_module.record_start_v2_with_duration(channelId, durationSec, doCipher, pvrEncryption);
    }

    public int record_start_v2_with_file_size(long channelId, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption) {
        return g_pvr_module.record_start_v2_with_file_size(channelId, fileSizeMB, doCipher, pvrEncryption);
    }

    public int pvr_timeshift_start_v2(int durationSec, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption) {
        return g_pvr_module.pvr_timeshift_start_v2(durationSec, fileSizeMB, doCipher, pvrEncryption);
    }

    public List<PvrFileInfo> pvr_get_records_file(int startIndex, int total) {
        return g_pvr_module.pvr_get_records_file(startIndex, total);
    }

    public List<PvrFileInfo> pvr_get_total_one_series_records_file(int startIndex, int total, String recordUniqueId) {
        return g_pvr_module.pvr_get_total_one_series_records_file(startIndex, total, recordUniqueId);
    }

    public int pvr_get_total_rec_num() {
        return g_pvr_module.pvr_get_total_rec_num();
    }

    public int pvr_get_total_one_series_rec_num(String recordUniqueId) {
        return g_pvr_module.pvr_get_total_one_series_rec_num(recordUniqueId);
    }

    public int pvr_delete_total_records_file() {
        return g_pvr_module.pvr_delete_total_records_file();
    }

    public int pvr_delete_one_series_folder(String recordUniqueId) {
        return g_pvr_module.pvr_delete_one_series_folder(recordUniqueId);
    }

    public int pvr_delete_record_file_by_ch_id(int channelId) {
        return g_pvr_module.pvr_delete_record_file_by_ch_id(channelId);
    }

    public int pvr_delete_record_file(String recordUniqueId) {
        return g_pvr_module.pvr_delete_record_file(recordUniqueId);
    }

    public int pvr_record_start(int pvrPlayerID, long channelID, String recordPath, int duration) {
        return g_pvr_module.pvr_record_start(pvrPlayerID, channelID, recordPath, duration);
    }

    public int pvr_record_start(int pvrPlayerID, long channelID, String recordPath, int duration, PVREncryption pvrEncryption) {
        return g_pvr_module.pvr_record_start(pvrPlayerID, channelID, recordPath, duration, pvrEncryption);
    }

    public int pvr_record_stop(int pvrPlayerID, int recId) {
        return g_pvr_module.pvr_record_stop(pvrPlayerID, recId);
    }

    public int pvr_record_get_already_rec_time(int pvrPlayerID, int recId) {
        return g_pvr_module.pvr_record_get_already_rec_time(pvrPlayerID, recId);
    }

    public int pvr_record_get_status(int pvrPlayerID, int recId) {
        return g_pvr_module.pvr_record_get_status(pvrPlayerID, recId);
    }

    public String pvr_record_get_file_full_path(int pvrPlayerID, int recId) {
        return g_pvr_module.pvr_record_get_file_full_path(pvrPlayerID, recId);
    }

    public int pvr_record_get_program_id(int pvrPlayerID, int recId) {
        return g_pvr_module.pvr_record_get_program_id(pvrPlayerID, recId);
    }

    public int pvr_timeshift_start(int playerID, int time, int filesize, String filePath) {
        return g_pvr_module.pvr_timeshift_start(playerID, time, filesize, filePath);
    }

    public int pvr_timeshift_stop(int playerID) {
        return g_pvr_module.pvr_timeshift_stop(playerID);
    }

    public int pvr_timeshift_play_for_live_channel(int pvrMode) //Edwin 20181022 TimeShift for Live Channel
    {
        return g_pvr_module.pvr_time_shift_play_for_live_channel(pvrMode);
    }

    public int pvr_timeshift_play(int playerID) {
        return g_pvr_module.pvr_timeshift_play(playerID);
    }

    public int pvr_timeshift_resume(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        return g_pvr_module.pvr_timeshift_resume(playerID);
    }

    public int pvr_timeshift_pause(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        return g_pvr_module.pvr_timeshift_pause(playerID);
    }

    public int pvr_timeshift_live_pause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return g_pvr_module.pvr_timeshift_live_pause(playerID);
    }

    public int pvr_timeshift_file_pause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return g_pvr_module.pvr_timeshift_file_pause(playerID);
    }

    public int pvr_timeshift_trick_play(int playerID, EnTrickMode mode) {
        return g_pvr_module.pvr_timeshift_trick_play(playerID, mode);
    }

    public int pvr_timeshift_seek_play(int playerID, int seekSec) {
        return g_pvr_module.pvr_timeshift_seek_play(playerID, seekSec);
    }

    public Date pvr_timeshift_get_played_time(int playerID) {
        return g_pvr_module.pvr_timeshift_get_played_time(playerID);
    }

    public int pvr_timeshift_get_play_second(int playerID) {
        return g_pvr_module.pvr_timeshift_get_play_second(playerID);
    }

    public Date pvr_timeshift_get_begin_time(int playerID) {
        return g_pvr_module.pvr_timeshift_get_begin_time(playerID);
    }

    public int pvr_timeshift_get_begin_second(int playerID) {
        return g_pvr_module.pvr_timeshift_get_begin_second(playerID);
    }

    public int pvr_timeshift_get_record_time(int playerID) {
        return g_pvr_module.pvr_timeshift_get_record_time(playerID);
    }

    public int pvr_timeshift_get_status(int playerID) {

        return g_pvr_module.pvr_timeshift_get_status(playerID);
    }

    public EnTrickMode pvr_timeshift_get_current_trick_mode(int playerID) {
        return g_pvr_module.pvr_timeshift_get_current_trick_mode(playerID);
    }

    public int pvr_play_start(String filePath) {
        return g_pvr_module.pvr_play_start(filePath);
    }

    public int pvr_play_start(String filePath, PVREncryption pvrEncryption) {
        return g_pvr_module.pvr_play_start(filePath, pvrEncryption);
    }

    public int pvr_play_stop() {
        return g_pvr_module.pvr_play_stop();
    }

    public int pvr_play_pause() {
        return g_pvr_module.pvr_play_pause();
    }

    public int pvr_play_resume() {
        return g_pvr_module.pvr_play_resume();
    }

    public int pvr_play_trick_play(EnTrickMode enSpeed) {
        return g_pvr_module.pvr_play_trick_play(enSpeed);
    }

    public int pvr_play_seek_to(int sec) {
        return g_pvr_module.pvr_play_seek_to(sec);
    }

    public int pvr_play_get_play_time() {
        return g_pvr_module.pvr_play_get_play_time();
    }

    public int pvr_play_get_play_time_ms()//eric lin 20181026 get play time(ms) for live channel
    {
        return g_pvr_module.pvr_play_get_play_time_ms();
    }

    public long pvr_play_get_size() {
        return g_pvr_module.pvr_play_get_size();
    }

    public int pvr_play_get_duration() {
        return g_pvr_module.pvr_play_get_duration();
    }

    public boolean pvr_play_is_radio(String fullName) {
        return g_pvr_module.pvr_play_is_radio(fullName);
    }

    public int pvr_play_get_current_status() {
        return g_pvr_module.pvr_play_get_current_status();
    }

    public EnTrickMode pvr_play_get_current_trick_mode() {
        return g_pvr_module.pvr_play_get_current_trick_mode();
    }

    public String pvr_play_get_file_full_path(int pvrPlayerID) {
        return g_pvr_module.pvr_play_get_file_full_path(pvrPlayerID);
    }

    public Resolution pvr_play_get_video_resolution() {
        return g_pvr_module.pvr_play_get_video_resolution();
    }

    public AudioInfo.AudioComponent pvr_play_get_current_audio() {
        return g_pvr_module.pvr_play_get_current_audio();
    }

    public AudioInfo pvr_play_get_audio_components() {
        return g_pvr_module.pvr_play_get_audio_components();
    }

    public int pvr_play_select_audio(AudioInfo.AudioComponent audio) {
        return g_pvr_module.pvr_play_select_audio(audio);
    }

    public int pvr_play_set_window_rect(Rect rect) {
        return g_pvr_module.pvr_play_set_window_rect(rect);
    }

    public int pvr_play_set_track_mode(EnAudioTrackMode enTrackMode) {
        return g_pvr_module.pvr_play_set_track_mode(enTrackMode);
    }

    public EnAudioTrackMode pvr_play_get_track_mode() {
        return g_pvr_module.pvr_play_get_track_mode();
    }

    public int pvr_file_remove(String filePath) {
        return g_pvr_module.pvr_file_remove(filePath);
    }

    public int pvr_file_rename(String oldName, String newName) {

        return g_pvr_module.pvr_file_rename(oldName, newName);
    }

    public int pvr_file_get_duration(String fullName) {
        return g_pvr_module.pvr_file_get_duration(fullName);
    }

    public long pvr_file_get_size(String fullName) {

        return g_pvr_module.pvr_file_get_size(fullName);
    }

    public PvrFileInfo pvr_file_get_all_info(String fullName) {
        return g_pvr_module.pvr_file_get_all_info(fullName);
    }

    public PvrFileInfo pvr_file_get_extra_info(String fullName) {
        return g_pvr_module.pvr_file_get_extra_info(fullName);
    }

    public PvrFileInfo pvr_file_get_epg_info(String fullName, int epgIndex) {
        return g_pvr_module.pvr_file_get_epg_info(fullName, epgIndex);
    }

    public int pvr_get_current_pvr_mode(long channelId) {
        return g_pvr_module.pvr_get_current_pvr_mode(channelId);
    }

    public int pvr_set_parent_lock_ok()  //connie 20180806 for pvr parentalRate
    {

        return g_pvr_module.pvr_set_parent_lock_ok();
    }

    // edwin 20180809 add PvrTotalRecordFileXXX -s
    public int pvr_total_record_file_open(String dirPath) {
        return g_pvr_module.pvr_total_record_file_open(dirPath);
    }

    public int pvr_total_record_file_close() {
        return g_pvr_module.pvr_total_record_file_close();
    }

    public int pvr_total_record_file_sort(int sortType) {
        return g_pvr_module.pvr_total_record_file_sort(sortType);
    }

    public List<PvrFileInfo> pvr_total_record_file_get(int startIndex, int total) {
        return g_pvr_module.pvr_total_record_file_get(startIndex, total);
    }

    public int pvr_check_hard_disk_open(String FilePath)//Scoty 20180827 add HDD Ready command and callback
    {
        return g_pvr_module.pvr_check_hard_disk_open(FilePath);
    }

    public int pvr_play_timeshift_stop()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return g_pvr_module.pvr_play_timeshift_stop();
    }

    public int pvr_record_get_live_pause_time()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return g_pvr_module.pvr_record_get_live_pause_time();
    }
    // edwin 20180809 add PvrTotalRecordFileXXX -e

    public int pvr_get_ratio() {
        return g_pvr_module.pvr_get_ratio();
    }
    // pvr -end

    public String get_record_path() {
        return g_config_module.get_record_path();
    }

    public void set_record_path(String path) {
        g_config_module.set_record_path(path);
    }

    public String get_default_rec_path()//Scoty 20180525 add get default record path
    {
        return g_config_module.get_default_rec_path();
    }

    public int pvr_record_check(long channelID) {
        return g_pvr_module.pvr_record_check(channelID);
    }

    public List<PvrInfo> pvr_record_get_all_info() {
        return g_pvr_module.pvr_record_get_all_info();
    }

    public int pvr_record_get_max_rec_num() {
        return g_pvr_module.pvr_record_get_max_rec_num();
    }

    public int pvr_play_file_check_last_view_point(String fullName) {
        return g_pvr_module.pvr_play_file_check_last_view_point(fullName);
    }

    public int pvr_set_start_position_flag(int startPositionFlag) {
        return g_pvr_module.pvr_set_start_position_flag(startPositionFlag);
    }

    public List<SimpleChannel> get_channel_list_by_filter(int filterTag, int serviceType, String keyword, int IncludeSkip, int IncludePvrSkip)//Scoty 20181109 modify for skip channel
    {
        return g_pm_module.get_channel_list_by_filter(filterTag, serviceType, keyword, IncludeSkip, IncludePvrSkip);
    }

    public int record_ts_start(int TunerId, String FullName) // connie 20180803 add record ts -s
    {
        return g_pvr_module.record_ts_start(TunerId, FullName);
    }

    public int record_ts_stop() {
        return g_pvr_module.record_ts_stop();
    }// connie 20180803 add record ts -e

    // Johnny 20180814 add setDiseqc1.0 port -s
    public int set_diseqc10_port_info(int nTunerId, int nPort, int n22KSwitch, int nPolarity) {
        return g_frontend_module.set_diseqc10_port_info(nTunerId, nPort, n22KSwitch, nPolarity);
    }

    //Scoty add DiSeqC Motor rule -s
    public int set_diseqc12_move_motor(int nTunerId, int Direct, int Step) {
        return g_frontend_module.set_diseqc12_move_motor(nTunerId, Direct, Step);
    }

    public int set_diseqc12_move_motor_stop(int nTunerId) {
        return g_frontend_module.set_diseqc12_move_motor_stop(nTunerId);
    }

    public int reset_diseqc12_position(int nTunerId) {
        return g_frontend_module.reset_diseqc12_position(nTunerId);
    }

    public int set_diseqc_limit_pos(int nTunerId, int limitType) {
        return g_frontend_module.set_diseqc_limit_pos(nTunerId, limitType);
    }

    //Scoty add DiSeqC Motor rule -e
    // Johnny 20180814 add setDiseqc1.0 port -e
    public int set_standby_on_off(int onOff) {
        return g_config_module.set_standby_on_off(onOff);
    }

    // for VMX need open/close -s
    public void vmx_start_scan(TVScanParams sp, int startTPID, int endTPID) // connie 20180919 add for vmx search
    {
        g_cs_module.vmx_start_scan(sp, startTPID, endTPID);
    }

    public LoaderInfo get_loader_info() // connie 20180903 for VMX -s
    {
        return g_vmx_module.get_loader_info();
    }

    public CaStatus get_ca_status_info() {
        return g_vmx_module.get_ca_status_info();
    }

    public int get_ecm_count() {
        return g_vmx_module.get_ecm_count();
    }

    public int get_emm_count() {
        return g_vmx_module.get_emm_count();
    }

    public String get_lib_date() {
        return g_vmx_module.get_lib_date();
    }

    public String get_chip_id() {
        return g_vmx_module.get_chip_id();
    }

    public String get_sn() {
        return g_vmx_module.get_sn();
    }

    public String get_ca_version() {
        return g_vmx_module.get_ca_version();
    }

    public String get_sc_number() {
        return g_vmx_module.get_sc_number();
    }

    public int get_pairing_status() {
        return g_vmx_module.get_pairing_status();
    }

    public String get_purse() {
        return g_vmx_module.get_purse();
    }

    public int get_group_m() {
        return g_vmx_module.get_group_m();
    }

    public int vmx_set_pin_code(String pincode) {
        return g_vmx_module.vmx_set_pin_code(pincode);
    }

    public String get_location() {
        return g_vmx_module.get_location();
    }

    public int set_pin_code(String pinCode, int PinIndex, int TextSelect) {
        return g_vmx_module.set_pin_code(pinCode, PinIndex, TextSelect);
    }

    public int set_pptv(String pinCode, int pinIndex) {
        return g_vmx_module.set_pptv(pinCode, pinIndex);
    }

    public void set_osm_ok() {
        g_vmx_module.set_osm_ok();
    }

    public void vmx_test(int mode) {
        g_vmx_module.vmx_test(mode);
    }

    //Scoty 20181207 modify VMX OTA rule -s
    public void test_vmx_ota(int mode) {
        g_vmx_module.test_vmx_ota(mode);
    }

    public int vmx_auto_ota(int OTAMode, int TriggerID, int TriggerNum, int TunerId, int SatId, int DsmccPid, int FreqNum, ArrayList<Integer> FreqList, ArrayList<Integer> BandwidthList) {
        return g_vmx_module.vmx_auto_ota(OTAMode, TriggerID, TriggerNum, TunerId, SatId, DsmccPid, FreqNum, FreqList, BandwidthList, get_tuner_type());
    }

    //Scoty 20181207 modify VMX OTA rule -e
    public String vmx_get_box_id() {
        return g_vmx_module.vmx_get_box_id();
    }

    public String vmx_get_virtual_number() {
        return g_vmx_module.vmx_get_virtual_number();
    }

    public void vmx_stop_ewbs(int mode)//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add stop EWBS
    {
        g_vmx_module.vmx_stop_ewbs(mode);
    }

    public void vmx_stop_emm() {
        g_vmx_module.vmx_stop_emm();
    }

    public void vmx_osm_finish(int triggerID, int triggerNum) {
        g_vmx_module.vmx_osm_finish(triggerID, triggerNum);
    }

    public VMXProtectData get_protect_data() {
        return g_config_module.get_protect_data();
    }

    public int set_protect_data(int first, int second, int third) {
        return g_config_module.set_protect_data(first, second, third);
    }
    // for VMX need open/close -e

    public int enter_view_activity(int enter) {
        return g_config_module.enter_view_activity(enter);
    }

    public int enable_mem_status_check(int enable) {
        return g_config_module.enable_mem_status_check(enable);
    }

    public int loader_dtv_get_jtag() {
        return g_loader_dtv_module.loader_dtv_get_jtag();
    }

    public int loader_dtv_set_jtag(int value) {
        return g_loader_dtv_module.loader_dtv_set_jtag(value);
    }

    public int loader_dtv_check_isdbt_service(OTATerrParameters ota) {
        return g_loader_dtv_module.loader_dtv_check_isdbt_service(ota);
    }

    public int loader_dtv_check_terrestrial_service(OTATerrParameters ota) {
        return g_loader_dtv_module.loader_dtv_check_terrestrial_service(ota);
    }

    public int loader_dtv_check_cable_service(OTACableParameters ota) {
        return g_loader_dtv_module.loader_dtv_check_cable_service(ota);
    }

    public int loader_dtv_get_stb_sn() {
        return g_loader_dtv_module.loader_dtv_get_stb_sn();
    }

    public int loader_dtv_get_chipset_id() {
        return g_loader_dtv_module.loader_dtv_get_chipset_id();
    }

    public int loader_dtv_get_sw_version() {
        return g_loader_dtv_module.Loader_dtv_get_sw_version();
    }

    public int invoke_test() {
        return g_common_module.invoke_test();
    }

    public void widevine_cas_session_id(int sessionIndex, int sessionId) {//eric lin 20210107 widevine cas
        g_ca_module.widevine_cas_session_id(sessionIndex, sessionId);
    }

    public int set_net_stream_info(int groupType, NetProgramInfo netStreamInfo) {
        return g_pm_module.set_net_stream_info(groupType, netStreamInfo);
    }

    public int reset_net_program_database() {
        return g_pm_module.reset_program_database(g_context);
    }

    public int ResetNetProgramDatabase() {
        return g_pm_module.reset_program_database(g_context);
    }


    /**
     * First Time Get netprogram.ini File and Add to DataBase
     * After Save Complete Rename to netprogram_already_set.ini
     * In order to not to save database again
     *
     * @return isSuccess : Save DataBase Results
     */
    public boolean init_net_program_database() {
        return g_pm_module.init_net_program_database(g_context, get_tuner_type());
    }

    public void setup_epg_channel(){
        List<ProgramInfo> tvChList = mDtvFramework.getProgramInfoList(
                FavGroup.ALL_TV_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);
        List<ProgramInfo> radioChList = mDtvFramework.getProgramInfoList(
                FavGroup.ALL_RADIO_TYPE,
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.NUM_ALL);

        List<ProgramInfo> allChList = new ArrayList<>();
        allChList.addAll(tvChList);
        allChList.addAll(radioChList);
        if (allChList.size() > 0) {
            send_epg_data_id(allChList);
        }
    }

    public void debug_epg_events(long channelId){
        g_epg_module.debug_epg_events(channelId);
    }

    public int add_series(long channelId, byte[] key){
        return g_series_module.add_series(channelId, key);
    }

    public int delete_serires(long channelId, byte[] key){
        return g_series_module.delete_series(channelId, key);
    }

    public SeriesInfo.Series get_series(long channelId, byte[] key){
        return g_series_module.get_series(channelId, key);
    }

    public SeriesInfo get_series_info(long channelId){
        return g_series_module.get_series_info(channelId);
    }

    public List<SeriesInfo> get_all_series_data(){
        return g_series_module.get_all_series_data();
    }

    public int save_series(){
        return g_series_module.save_series();
    }
}
