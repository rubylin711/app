package com.prime.launcher;

import static com.prime.datastructure.CommuincateInterface.ScanModule.CMD_ServicePlayer_SCAN_StartScan;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.datastructure.CommuincateInterface.IPrimeDtvServiceCallback;
import com.prime.datastructure.CommuincateInterface.IPrimeDtvService;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.CommuincateInterface.ParcelableClass;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.launcher.R;

public class TestServiceActivity extends AppCompatActivity {
    private static String TAG = "TestServiceActivity";
    private Context mContext ;
    private static final String AIDL_SERVICE_PKGNAME = "com.prime.dtv";
    private static final String AIDL_PLAYER_SERVICE_ACTION = "com.prime.dtv.DtvAidlService.action";
    private final AIDLServiceConnection gPlayerServiceConn = new AIDLServiceConnection(AIDL_SERVICE_PKGNAME, AIDL_PLAYER_SERVICE_ACTION);
    private IPrimeDtvService gPlayerService = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this ;
        setContentView(R.layout.test_service_activty);
        Log.d(TAG, "onCreate");
        bindService(gPlayerServiceConn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mContext.unbindService(gPlayerServiceConn);
    }

    private void testSendTpInfo()
    {
        if ( gPlayerService != null )
        {
            TpInfo tp = new TpInfo();
            tp.setTunerType(TpInfo.DVBT);
            tp.setTpId(10);
            tp.setSatId(20);
            tp.setNetwork_id(1);
            tp.setTransport_id(2);
            tp.setOrignal_network_id(3);
            tp.setTuner_id(1);
            tp.setSdt_version(50);
            tp.TerrTp.setChannel(1);
            tp.TerrTp.setFreq(2);
            tp.TerrTp.setBand(3);
            tp.TerrTp.setFft(4);
            tp.TerrTp.setGuard(5);
            tp.TerrTp.setConst(6);
            tp.TerrTp.setHierarchy(7);
            tp.TerrTp.setNetWork(8);
            tp.TerrTp.setNitSearchIndex(9);
            tp.TerrTp.setCodeRate(10);
            tp.TerrTp.setSearchOrNot(11);

            tp.CableTp.setChannel(1);
            tp.CableTp.setFreq(2);
            tp.CableTp.setSymbol(3);
            tp.CableTp.setQam(4);

            tp.SatTp.setFreq(1);
            tp.SatTp.setSymbol(2);
            tp.SatTp.setFec(3);
            tp.SatTp.setPolar(4);
            tp.SatTp.setDrot(5);
            tp.SatTp.setSpect(6);
            tp.SatTp.setNetWork(7);
            tp.SatTp.setNitSearchIndex(8);
            tp.SatTp.setSearchOrNot(9);

            Log.d(TAG, "testSendTpInfo tp:" + tp.ToString());
//            ParcelableClass param = new ParcelableClass( tp);
//            try {
//                ParcelableClass result = gPlayerService.invoke(1111, param);
//                ProgramInfo programInfo = result.getData(ProgramInfo.class);
//                Log.d(TAG, "testSendTpInfo result:" + programInfo.ToString());
//            } catch (RemoteException e) {
//                throw new RuntimeException(e);
//            }
        }
    }

    private void testStartScan()
    {
        if ( gPlayerService != null )
        {
            TpInfo tpInfo = new TpInfo(TpInfo.DVBC);
            tpInfo.setTpId(0);
            tpInfo.CableTp.setFreq(303000);
            tpInfo.CableTp.setSymbol(5200);
            tpInfo.CableTp.setQam(TpInfo.Cable.QAM_256);
            Log.d(TAG, "testStartScan tpInfo:" + tpInfo.ToString());

            TVScanParams scanParams = new TVScanParams(0, tpInfo, 0, 1, 0, 0, 0, 0);
//            try {
//                gPlayerService.invoke(CMD_ServicePlayer_SCAN_StartScan, new ParcelableClass(scanParams));
//            } catch (RemoteException e) {
//                throw new RuntimeException(e);
//            }
        }
    }
    public class AIDLServiceConnection implements ServiceConnection {
        private final String pkg;
        private final String action;

        public AIDLServiceConnection(String pkg, String action) {
            this.pkg = pkg;
            this.action = action;
        }

        public String getPkg()
        {
            return pkg;
        }

        public String getAction()
        {
            return action;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "onServiceConnected [" + pkg + "] action [" + action + "] Connected to " + componentName);
            if ( action.equals(AIDL_PLAYER_SERVICE_ACTION) )
            {
                gPlayerService = IPrimeDtvService.Stub.asInterface(service);
                try {
                    gPlayerService.registerCallback(new IPrimeDtvServiceCallback.Stub() {
                        @Override
                        public void onMessage(TVMessage msg) throws RemoteException {
                            Log.d(TAG, "onMessage msg = " + msg.getMessage() + " type " + msg.getMsgType() + " flag " + msg.getMsgFlag() );
                        }
                    },null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
//                testSendTpInfo();
                testStartScan();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected [" + pkg + "] action [" + action + "] Connected to " + componentName);
            if (action.equals(AIDL_PLAYER_SERVICE_ACTION)) {
                gPlayerService = null;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "Rebinding service: " + action);
                bindService(this);
            }, 1000);
        }
    }
    private void bindService(AIDLServiceConnection serviceConnection) {
        Log.d(TAG, "bindService pkg:" + serviceConnection.getPkg() + " action:" + serviceConnection.getAction() );
        Intent intent = new Intent();
        intent.setPackage(serviceConnection.getPkg());
        intent.setAction(serviceConnection.getAction());
        mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}
