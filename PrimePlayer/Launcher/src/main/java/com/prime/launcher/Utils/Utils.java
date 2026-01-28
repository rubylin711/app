package com.prime.launcher.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.launcher.Receiver.PrimeUsbReceiver;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Utils {
    private static final String TAG = "Utils";
    private static String MOUNTED_USB_PATH = null;

    private static class Notification {
        public static final List<String> MsgQueue = new ArrayList<>();
        public static boolean IS_SHOW = false;
    }

    public static int get_tag_position(String tag) {
        return Integer.valueOf(tag.split(",")[0]).intValue();
    }

    public static int get_tag_id(String tag) {
        return Integer.valueOf(tag.split(",")[1]).intValue();
    }

    public static int get_sec_tag_id(String tag) {
        return Integer.valueOf(tag.split(",")[2]).intValue();
    }

    public static int get_third_tag_id(String tag) {
        return Integer.valueOf(tag.split(",")[3]).intValue();
    }

    public static int get_fourth_tag_id(String tag) {
        return Integer.valueOf(tag.split(",")[4]).intValue();
    }

    public static String get_fourth_tag(String tag) {
        return tag.split(",")[4];
    }

    public static void input_keycode(int value) {
        try {
            new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("input keyevent " + value).getInputStream())).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get_leading_zero_number_at(int number) {
        return String.format("%03d", Integer.valueOf(number));
    }

    public static String number_code_to_string(int keyCode) {
        Log.d(TAG, "number_code_to_string: keyCode = " + keyCode);
        return String.valueOf(keyCode - 7);
    }

    public static String generate_url(Context context, String serviceName) {
        String bat_id = ACSDataProviderHelper.get_acs_provider_data(context, "bat_id");
        String zipcode = ACSDataProviderHelper.get_acs_provider_data(context, "zipcode");
        String STB_CA_SN = SystemProperties.get("ro.boot.cstmsnno");

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority("mwps.tbc.net.tw").appendPath(serviceName).
                appendPath("0").
                appendPath("0").
                appendPath("0").
                appendPath(Build.VERSION.INCREMENTAL).
                appendPath(STB_CA_SN).
                appendPath(STB_CA_SN).
                appendPath(STB_CA_SN);
        if (!TextUtils.isEmpty(bat_id)) {
            builder.appendPath(bat_id);
        } else {
            builder.appendPath("null");
        }
        if (!TextUtils.isEmpty(zipcode)) {
            builder.appendPath(zipcode);
        } else {
            builder.appendPath("null");
        }
        builder.appendPath("null");
        return builder.build().toString();
    }

    public static void unmount_usb_path() {
        MOUNTED_USB_PATH = null;
        PrimeUsbReceiver.MOUNTED = false;
    }

    public static void set_mount_usb_path(String usbPath) {
        LogUtils.d("set_mount_usb_path: "+ usbPath);
        MOUNTED_USB_PATH = usbPath;
        PrimeUsbReceiver.MOUNTED = MOUNTED_USB_PATH != null && !MOUNTED_USB_PATH.isEmpty();
    }

    public static String get_usb_uuid() {
        if (null == MOUNTED_USB_PATH || MOUNTED_USB_PATH.isEmpty()) {
            Log.e(TAG, "get_usb_name: usb path == null or empty");
            return null;
        }
        String[] stringArray = MOUNTED_USB_PATH.split("/");
        return stringArray[stringArray.length-1];
    }

    public static String get_usb_uuid(String usbPath) {
        if (null == usbPath || usbPath.isEmpty()) {
            Log.e(TAG, "get_usb_name: usb path == null or empty");
            return null;
        }
        String[] stringArray = usbPath.split("/");
        return stringArray[stringArray.length-1];
    }

    public static String get_usb_name(Context context, String mountUUID) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            @SuppressLint("SoonBlockedPrivateApi")
            Method mGetRecs = StorageManager.class.getDeclaredMethod("getVolumeRecords");
            mGetRecs.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> recs = (List<Object>) mGetRecs.invoke(sm);

            Class<?> clsRec = Class.forName("android.os.storage.VolumeRecord");
            @SuppressLint("SoonBlockedPrivateApi") Method getFsUuid   = clsRec.getDeclaredMethod("getFsUuid");
            @SuppressLint("SoonBlockedPrivateApi") Method getNickname = clsRec.getDeclaredMethod("getNickname");

            String targetNickName = "null";
            for (Object r : recs) {
                String uuid = (String) getFsUuid.invoke(r);     // 1234-5678
                String nick = (String) getNickname.invoke(r);   // MovieUSB
                Log.d(TAG, uuid + " ??" + nick);

                if (mountUUID.equals(uuid)) {
                    if (nick == null)
                        targetNickName = uuid;
                    else
                        targetNickName = nick;
                    break;
                }
            }
            return targetNickName;
        } catch (Exception e) {
            Log.e(TAG, "get_usb_name: get usb name fail " + e );
        }

        return "null";
    }

    public static String get_mount_usb_path() {
        return MOUNTED_USB_PATH;
    }

    public static boolean check_usb_size() {
        //Log.d(TAG, "check_usb_size:");
        if (MOUNTED_USB_PATH == null || MOUNTED_USB_PATH.isEmpty()) {
            Log.d(TAG, "check_usb_size: 0");
            return false;
        }

        List<Long> usbSize = get_usb_space_info();
        long totalSize = usbSize.get(0);
        long availableSize = usbSize.get(1);

        if (totalSize < 0) {
            Log.w(TAG, "check_usb_size: no usb disk");
            return false;
        }

        if (totalSize < 10000) {
            Log.w(TAG, "check_usb_size: size less than "+10000/1000+"g");
            return false;
        }

        return true;
    }

    public static List<Long> get_usb_space_info() {
        long totalSize = -1;
        long availableSize = 0;

        if(MOUNTED_USB_PATH != null) {
            StatFs statFs = new StatFs(Uri.parse(MOUNTED_USB_PATH).getPath());
            totalSize = statFs.getBlockCountLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            availableSize = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() / (1024 * 1024);
            //Log.d(TAG, "check_usb_size: Total Size: " + totalSize + " MB");
            //Log.d(TAG, "check_usb_size: Available Size: " + availableSize + " MB");
        }
        else{
            Log.w(TAG, "check_usb_size: mStatFs is null ! MOUNTED_USB_PATH is "+MOUNTED_USB_PATH);
        }

        List<Long> usbSize = new ArrayList<>();
        usbSize.add(totalSize);
        usbSize.add(availableSize);
        return usbSize;
    }
    @SuppressLint("DiscouragedPrivateApi")
    public static boolean eject_usb_device(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        try {
            // make getVolumes method by reflection
            //noinspection JavaReflectionMemberAccess
            Method getVolumesMethod = StorageManager.class.getDeclaredMethod("getVolumes");
            List<?> volumes = (List<?>) getVolumesMethod.invoke(storageManager);

            if (null == volumes || volumes.isEmpty())
                return false;

            for (Object volume : volumes) {
                Method getDiskMethod = volume.getClass().getMethod("getDisk");
                Object disk = getDiskMethod.invoke(volume);

                if (disk != null) { // ensure this is a USB storage device
                    Method isUsbMethod = disk.getClass().getMethod("isUsb");
                    //noinspection DataFlowIssue
                    boolean isUsb = (boolean) isUsbMethod.invoke(disk);

                    if (isUsb) {
                        // unmount USB Volume
                        //noinspection JavaReflectionMemberAccess
                        Method unmountMethod = StorageManager.class.getDeclaredMethod("unmount", String.class);
                        Method getIdMethod = volume.getClass().getMethod("getId");
                        String volumeId = (String) getIdMethod.invoke(volume);
                        unmountMethod.invoke(storageManager, volumeId);
                        Log.d(TAG, "eject_usb_device: eject usb storage success");
                        return true;
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "eject_usb_device: eject usb storage fail", e);
        }
        return false;
    }

    @SuppressLint("DiscouragedPrivateApi")
    public static void eject_usb_device(Context context, String mountPoint) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        if (mountPoint.equals(MOUNTED_USB_PATH)) {
            PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
            primeDtv.pvr_deinit();
            primeDtv.hdd_monitor_stop();
        }

        try {
            // make getVolumes method by reflection
            //noinspection JavaReflectionMemberAccess
            Method getVolumesMethod = StorageManager.class.getDeclaredMethod("getVolumes");
            List<?> volumes = (List<?>) getVolumesMethod.invoke(storageManager);

            if (null == volumes || volumes.isEmpty())
                return;

            for (Object volume : volumes) {
                Method getDiskMethod = volume.getClass().getMethod("getDisk");
                Object disk = getDiskMethod.invoke(volume);

                if (disk != null) { // ensure this is a USB storage device
                    Method isUsbMethod = disk.getClass().getMethod("isUsb");
                    //noinspection DataFlowIssue
                    boolean isUsb = (boolean) isUsbMethod.invoke(disk);

                    if (isUsb) {
                        // unmount USB Volume
                        //noinspection JavaReflectionMemberAccess
                        Method getPathMethod = volume.getClass().getMethod("getPath");  // ?��??�本?�能??getDirectory()
                        Object pathObject = getPathMethod.invoke(volume);
                        String mountPath = ((File)pathObject).getPath().toString();
                        Log.d(TAG, "eject_usb_device: mountPath = " + mountPath );

                        if (!mountPoint.equals(mountPath))
                            continue;

                        Method unmountMethod = StorageManager.class.getDeclaredMethod("unmount", String.class);
                        Method getIdMethod = volume.getClass().getMethod("getId");
                        String volumeId = (String) getIdMethod.invoke(volume);
                        Log.d(TAG, "eject_usb_device: id = " + volumeId);
                        unmountMethod.invoke(storageManager, volumeId);
                        Log.d(TAG, "eject_usb_device: eject usb storage success");
                        return;
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "eject_usb_device: eject usb storage fail", e);
        }
    }

    public static boolean format_usb_device(Context context) {

        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            // 1. ?��??�到 getDisks()
            Method mGetDisks = StorageManager.class.getDeclaredMethod("getDisks");
            mGetDisks.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> disks = (List<Object>) mGetDisks.invoke(sm);

            // 2. ?��? DiskInfo 欄�?
            Class<?> clsDiskInfo = Class.forName("android.os.storage.DiskInfo");
            Method isUsb = clsDiskInfo.getDeclaredMethod("isUsb");
            Method getId = clsDiskInfo.getDeclaredMethod("getId");
            Method isAdoptable = clsDiskInfo.getDeclaredMethod("isAdoptable");

            Object targetDisk = null;
            for (Object d : disks) {
                if ((boolean) isUsb.invoke(d) && (boolean) isAdoptable.invoke(d)) {
                    targetDisk = d;
                    break;
                }
            }
            if (targetDisk == null) {
                Log.e(TAG, "no usb disk");
                return false;
            }
            String diskId = (String) getId.invoke(targetDisk);

            // 3. ?��? StorageManager.partitionPublic()
            @SuppressLint("SoonBlockedPrivateApi")
            Method partPublic = StorageManager.class.getDeclaredMethod("partitionPublic", String.class);
            partPublic.setAccessible(true);
            partPublic.invoke(sm, diskId);
        }
        catch (Exception e) {
            Log.e(TAG, "format_usb_device: format usb device fail", e);
            return false;
        }
        return true;
    }

    public static void modify_usb_name(Context context, String mountPoint, String newName) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (newName == null) {
            Log.e(TAG, "modify_usb_name: newName == null");
            return;
        }

        try {
            // ?��? getVolumes() ?�出?�??VolumeInfo（@hide�?
            @SuppressLint("DiscouragedPrivateApi")
            Method mGetVolumes = StorageManager.class.getDeclaredMethod("getVolumes");
            mGetVolumes.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> vols = (List<Object>) mGetVolumes.invoke(sm);

            Class<?> clsVol = Class.forName("android.os.storage.VolumeInfo");
            Field fFsUuid = clsVol.getDeclaredField("fsUuid");
            Method getPath   = clsVol.getDeclaredMethod("getPath");

            String targetUuid = null;
            for (Object v : vols) {
                if (mountPoint.equals(getPath.invoke(v).toString())) {
                    targetUuid = (String) fFsUuid.get(v);
                }
            }

            Log.d(TAG, "modify_usb_name: targetUuid = " + targetUuid);

            @SuppressLint("SoonBlockedPrivateApi") Method mSetNick = StorageManager.class.getDeclaredMethod(
                    "setVolumeNickname", String.class, String.class);
            mSetNick.setAccessible(true);
            mSetNick.invoke(sm, targetUuid, newName);        // <- ?��?顯示?��?�?
        } catch (Exception e) {
            Log.e(TAG, "modify_usb_name: modify usb name fail", e);
        }
    }

    public static List<ProgramInfo> get_all_tv_program_info_list(PrimeDtv primeDtv) {
        List<ProgramInfo> allChannels, TypeTvChannels, TypeRadioChannels;

        TypeTvChannels = new ArrayList<>();
        TypeRadioChannels = new ArrayList<>();

        if (primeDtv != null) {
            TypeTvChannels = primeDtv.get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
            //LogUtils.d("TV number: "+ TypeTvChannels.size());
            TypeRadioChannels = primeDtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
            //LogUtils.d("Radio number: "+ TypeRadioChannels.size());
        }

        allChannels = new ArrayList<>();
        allChannels.addAll(TypeTvChannels);
        allChannels.addAll(TypeRadioChannels);
        allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));

        return allChannels;
    }

    @SuppressLint("InflateParams")
    public static void show_notification(Context context, String message) {
        if (message != null)
            Notification.MsgQueue.add(message);

        if (Notification.IS_SHOW)
            return;

        Notification.IS_SHOW = true;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // need <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        View systemBannerView = LayoutInflater.from(context).inflate(R.layout.system_banner, null);
        TextView bannerTxv = systemBannerView.findViewById(R.id.bannerText);

        bannerTxv.setText(Notification.MsgQueue.get(0));
        systemBannerView.setAlpha(0f);
        systemBannerView.setTranslationX(500f);
        params.gravity = Gravity.TOP | Gravity.END;
        windowManager.addView(systemBannerView, params);

        // popup animation
        systemBannerView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // auto disappear
        new CountDownTimer(5000, 1000) {
            @Override public void onTick(long millisUntilFinished) {}
            @Override public void onFinish() {
                systemBannerView.animate()
                        .alpha(0f)
                        .translationX(500f)
                        .setDuration(250)
                        .withEndAction(() -> {
                            systemBannerView.setVisibility(View.INVISIBLE);
                            windowManager.removeView(systemBannerView);
                            Notification.IS_SHOW = false;
                            Notification.MsgQueue.remove(0);
                            if (!Notification.MsgQueue.isEmpty())
                                show_notification(context, null);
                        })
                        .start();
            }
        }.start();
    }

    public static void keep_screen_on(AppCompatActivity activity, boolean keepOn) {
        //Log.d(TAG, "keep_screen_on: [keepOn] " + keepOn);
        activity.runOnUiThread(() -> {
            if (keepOn)
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });
    }

    public static void print_call_stack() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace())
            Log.d("StackTrace", element.toString());
    }

    public static boolean is_context_valid_for_glide(Context context) {
        if (context == null) {
            return false;
        }

        if (context instanceof Activity activity) {
            // for Glide: You cannot start a load for a destroyed activity
            return !activity.isDestroyed() && !activity.isFinishing();
        }

        return true;
    }
}
