package com.prime.datastructure.CommuincateInterface;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class PrimeDtvServiceConnection implements ServiceConnection {
    public static String TAG = "PrimeDtvServiceConnection";
    public static final String PRIME_DTV_SERVICE_PKGNAME = "com.prime.dtvservice";
    public static final String PRIME_DTV_SERVICE_ACTION = "com.prime.dtvservice.PrimeDtvService.action";
    private String connectedFrom;
    private String pkg;
    private String action;
    private PrimeDtvServiceInterface serviceInterface = null;
    private PrimeDtvServiceInterface.onMessageListener listener = null;
    private Context context;

    public interface PrimeDtvServiceConnectionCallback {
        void onServiceConnected();
        void onServiceDisconnected();
    }

    public PrimeDtvServiceConnection(Context context, PrimeDtvServiceInterface serviceInterface,
            PrimeDtvServiceInterface.onMessageListener listener, String pkg, String action, String connectedFrom) {
        this.context = context;
        this.pkg = pkg;
        this.action = action;
        this.serviceInterface = serviceInterface;
        this.listener = listener;
        this.connectedFrom = connectedFrom;
        Log.d(TAG, "new PrimeDtvServiceConnection [" + pkg + "] action [" + action + "] connectedFrom from "
                + connectedFrom);
    }

    public String getPkg() {
        return pkg;
    }

    public String getAction() {
        return action;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Log.d(TAG, "onServiceConnected [" + pkg + "] action [" + action + "] Connected to " + componentName);
        if (action.equals(PRIME_DTV_SERVICE_ACTION)) {
            final IPrimeDtvService primeDtvService = IPrimeDtvService.Stub.asInterface(service);
            serviceInterface.SetPrimeDtvService(primeDtvService, listener, connectedFrom);
            serviceInterface.onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceDisconnected [" + pkg + "] action [" + action + "] DisConnected to " + componentName);
        if (action.equals(PRIME_DTV_SERVICE_ACTION)) {
            serviceInterface.SetPrimeDtvService(null, null, null);
            serviceInterface.onServiceDisconnected();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Rebinding service: " + action);
            try {
                context.unbindService(this);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Unbind failed (maybe not bound): " + e.getMessage());
            }
            bindService(this);
        }, 1000);
    }

    @Override
    public void onBindingDied(ComponentName name) {
        Log.e(TAG, "onBindingDied: Service binding died! " + name);
        onServiceDisconnected(name);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        Log.e(TAG, "onNullBinding: Service returned null from onBind! " + name);
    }

    private void bindService(PrimeDtvServiceConnection serviceConnection) {
        Log.d(TAG, "bindService pkg:" + serviceConnection.getPkg() + " action:" + serviceConnection.getAction());
        Intent intent = new Intent();
        intent.setPackage(serviceConnection.getPkg());
        intent.setAction(serviceConnection.getAction());
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
