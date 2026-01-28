package com.prime.homeplus.tv.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.inspur.adservice.IADCallback;
import com.inspur.adservice.IADManager;
import com.prime.dtv.service.dsmcc.DsmccService;
import com.prime.homeplus.tv.data.ADData;
import com.prime.homeplus.tv.data.MiniEpgAdBean;
import com.prime.homeplus.tv.data.ADImage;
import com.prime.homeplus.tv.event.EventEpgUpdate;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ADHelper {
    private final static String TAG = "HomePlus_ADHelper";

    private final static String AD_TOGGLE_KEY_SYSTEM_PROPERTY = "persist.sys.inspur.ca.ad";
    private final static String AD_TOGGLE_KEY_SYSTEM_PROPERTY_9642C = "persist.inspur.ca.ad";

    public static class ADHelperHolder {
        public static final ADHelper instance = new ADHelper();
    }

    private ADHelper() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ADHelper getInstance() {
        return ADHelperHolder.instance;
    }

    private Handler mHandler;
    private IADManager mIADManager;

    private Context mContext;
    private long lastAdFreshTime = 0;

    private ServiceConnection mADServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "mADServiceConnection onServiceConnected <...");
            mIADManager = IADManager.Stub.asInterface(iBinder);
            try {
                mIADManager.registerListener(iadCallback, DsmccService.KEY_MINIEPG);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // parseADData();
            freshADToggle();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "mADServiceConnection onServiceDisconnected <...");
            mIADManager = null;
        }
    };

    private IADCallback iadCallback = new IADCallback.Stub() {
        @Override
        public void dataInfrom(String data) throws RemoteException {
            parseADData();
        }
    };

    public void bindADService(Context context) {
        Log.d(TAG, "bindADService <...");
        try {
            mContext = context;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.prime.dtvservice", "com.prime.dtv.service.dsmcc.DsmccService"));
            context.bindService(intent, mADServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unbindADService(Context context) {
        Log.d(TAG, "unbindADService <...");
        try {
            if (null != mIADManager) {
                context.unbindService(mADServiceConnection);
                mIADManager = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseADData() {
        Log.d(TAG, "parseADData <... mIADManager = " + mIADManager);
        if (null != mIADManager) {
            try {
                Gson gson = new Gson();
                String epgData = mIADManager.getADJsonData(DsmccService.KEY_MINIEPG);
                Log.d(TAG, "parseADData miniEpgData==>" + epgData);

                // String portalData = mIADManager.getADJsonData("portal");
                // Log.d(TAG, "parseADData portalData==>" + portalData);

                // String channelData = mIADManager.getADJsonData("channels");
                // Log.d(TAG, "parseADData channelsData==>" + channelData);

                if (!TextUtils.isEmpty(epgData)) {
                    MiniEpgAdBean data = gson.fromJson(epgData, MiniEpgAdBean.class);
                    miniEpgAdData = new ADData();
                    miniEpgAdData.entryTime = data.getEntryTime();

                    String prefix = data.getPathPrefix();
                    if (prefix != null && !prefix.endsWith("/") && !prefix.endsWith(java.io.File.separator)) {
                        prefix += java.io.File.separator;
                    }
                    miniEpgAdData.pathPrefix = prefix;

                    for (int i = 0; i < data.getChildren().size(); i++) {
                        miniEpgAdData.playMode.add(data.getChildren().get(i).getPlayModeType());
                        miniEpgAdData.playModeSubType.add(data.getChildren().get(i).getPlayModeSubType());
                        miniEpgAdData.playModeValue.add(Integer.parseInt(data.getChildren().get(i).getPlayModeValue()));
                        miniEpgAdData.durationValue.add(Integer.parseInt(data.getChildren().get(i).getDurationValue()));
                        List<MiniEpgAdBean.ChildrenBean.AssetTypeBean.ImageBean> imageJA = data.getChildren().get(i)
                                .getAssetType().getImage();
                        if (null != imageJA && imageJA.size() > 0) {
                            List<ADImage> adImages = new ArrayList<>();
                            for (int j = 0, size = imageJA.size(); j < size; j++) {
                                ADImage adImage = new ADImage();
                                adImage.assetValue = imageJA.get(j).getAssetValue();
                                adImage.actionType = imageJA.get(j).getActionType();
                                adImage.actionValue = imageJA.get(j).getActionValue();
                                adImages.add(adImage);
                            }
                            miniEpgAdData.adImageList.add(adImages);
                        }
                    }
                    Log.d(TAG, "广告mini epg: " + gson.toJson(miniEpgAdData));
                    triggerMiniEpgAd();
                    mHandler.removeCallbacks(miniEpgAdRunnable);
                    mHandler.postDelayed(miniEpgAdRunnable, 20 * 60 * 1000);
                }

            } catch (RemoteException e) {
                Log.e(TAG, "parseADData failed: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    private Runnable miniEpgAdRunnable = () -> {
        Log.d(TAG, "miniEpgAdRunnable 每20分钟更新miniEpg轮播");
        triggerMiniEpgAd();
    };

    private void triggerMiniEpgAd() {
        if (miniEpgAdData != null && !TextUtils.isEmpty(miniEpgAdData.pathPrefix)
                && miniEpgAdData.playMode.size() > 0
                && miniEpgAdData.playModeSubType.size() > 0
                && miniEpgAdData.durationValue.size() > 0
                && miniEpgAdData.adImageList.size() > 0) {
            EventEpgUpdate eventEpgUpdate = new EventEpgUpdate();
            eventEpgUpdate.setmADData(miniEpgAdData);
            EventBus.getDefault().post(eventEpgUpdate);
        }
    }

    private ADData miniEpgAdData = null;

    public void freshADToggle() {
        boolean isOpen = isAdOpen();
        long curTime = System.currentTimeMillis();
        Log.d(TAG, "freshADToggle isOpen:" + isOpen);
        if (lastAdFreshTime != 0 && curTime - lastAdFreshTime <= 2000) {
            Log.d(TAG, "freshADToggle刚请求，短时间内不用重复");
            lastAdFreshTime = curTime;
            return;
        }
        if (isOpen) {
            parseADData();
        }
    }

    public boolean isAdOpen() {
        boolean isOpen = false;
        // Simplified check, assuming standard property or 9642C property
        String prop9642 = SystemProperties.get(AD_TOGGLE_KEY_SYSTEM_PROPERTY_9642C, "");
        if ("open".equalsIgnoreCase(prop9642)) {
            isOpen = true;
        } else {
            isOpen = SystemProperties.get(AD_TOGGLE_KEY_SYSTEM_PROPERTY, "open").equalsIgnoreCase("open");
        }
        Log.d(TAG, "isAdOpen isOpen:" + isOpen);
        return isOpen;
    }
}
