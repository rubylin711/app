package com.prime.btpair;

import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.bluetooth.BluetoothClass;
import android.os.SystemProperties;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.bluetooth.BluetoothAdapter;
import java.util.Set;
import java.util.HashSet;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.graphics.drawable.Drawable;

// Animation
import android.animation.AnimatorInflater;
import android.animation.ValueAnimator;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;

public class HookBeginActivity extends Activity implements BluetoothDevicePairer.EventListener {
    private static final String TAG = "HookBeginActivity";
    private static final boolean DEBUG = true;

    private static final String ACTION_CONNECT_INPUT =
            "com.google.android.intent.action.CONNECT_INPUT";

    private static final String ACTION_GLOBAL_BUTTON_DMG = 
            "com.prime.dmg.launcher.GLOBAL_BUTTON";

    private static final String INTENT_EXTRA_NO_INPUT_MODE = "no_input_mode";

    private static final String SAVED_STATE_PREFERENCE_FRAGMENT =
            "AddAccessoryActivity.PREFERENCE_FRAGMENT";
    private static final String SAVED_STATE_CONTENT_FRAGMENT =
            "AddAccessoryActivity.CONTENT_FRAGMENT";
    private static final String SAVED_STATE_BLUETOOTH_DEVICES =
            "AddAccessoryActivity.BLUETOOTH_DEVICES";

    private static final String ADDRESS_NONE = "NONE";

    private static final int AUTOPAIR_COUNT = 10;

    private static final int MSG_UPDATE_VIEW = 1;
    private static final int MSG_REMOVE_CANCELED = 2;
    private static final int MSG_PAIRING_COMPLETE = 3;
    private static final int MSG_OP_TIMEOUT = 4;
    private static final int MSG_RESTART = 5;
    private static final int MSG_TRIGGER_SELECT_DOWN = 6;
    private static final int MSG_TRIGGER_SELECT_UP = 7;
    private static final int MSG_AUTOPAIR_TICK = 8;
    private static final int MSG_START_AUTOPAIR_COUNTDOWN = 9;

    private static final int CANCEL_MESSAGE_TIMEOUT = 3000;
    private static final int DONE_MESSAGE_TIMEOUT = 1000;
    private static final int PAIR_OPERATION_TIMEOUT = 120000;
    private static final int CONNECT_OPERATION_TIMEOUT = 15000;
    private static final int RESTART_DELAY = 3000;
    private static final int LONG_PRESS_DURATION = 3000;
    private static final int KEY_DOWN_TIME = 150;
    private static final int TIME_TO_START_AUTOPAIR_COUNT = 5000;
    private static final int EXIT_TIMEOUT_MILLIS = 90 * 1000;

    // members related to Bluetooth pairing
    private BluetoothDevicePairer mBluetoothPairer;
    private int mPreviousStatus = BluetoothDevicePairer.STATUS_NONE;
    private boolean mPairingSuccess = false;
    private boolean mPairingBluetooth = false;
    private List<BluetoothDevice> mBluetoothDevices;
    private String mCancelledAddress = ADDRESS_NONE;
    private String mCurrentTargetAddress = ADDRESS_NONE;
    private String mCurrentTargetStatus = "";
    private boolean mPairingInBackground = false;
    private String mParingAddress;

    private boolean mDone = false;

    private boolean mHwKeyDown;
    private boolean mHwKeyDidSelect;
    private boolean mNoInputMode;

    // Animation
    ImageView rcuGreenLight, rcuPressHint, rcuDotView;
    View rcuDotConnecting;
    Handler handler;
    boolean isAnimationGreenLight;
    boolean isAnimationDotConnectting;

    protected Button skipButton;
    protected TextView message;
    protected TextView deviceName;
    protected TextView deviceStatus;
    private boolean mSetResult = false;
	private int cancel_pair = 0;
    private int show_info_flag = 0;
    private StbInfoDialog g_stbInfoDialog = null;
    // Internal message handler
    private final MessageHandler mMsgHandler = new MessageHandler();
    private static final String PROPERTY_PAIR_COMPLETE = "persist.vendor.rtk.setup.pairdone";
    public static HookBeginActivity myinstance;
	
	private static final String URL = "content://com.google.android.tungsten.setupwraith.locales/localeprefs";
    private static final Uri CONTENT_URI = Uri.parse(URL);
    private static final String LOCALE_COLUMN_NAME = "locale";
    private static final String RANK_COLUMN_NAME = "rank";
	private static final String LOCALE_WILDCARD_CHAR = "*";
    private boolean showOtherLocales;
    private ArrayList<String> preferredLocalesList;

    private static class MessageHandler extends Handler {

        private WeakReference<HookBeginActivity> mActivityRef = new WeakReference<>(null);

        public void setActivity(HookBeginActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"handleMessage msg="+msg.what);
            final HookBeginActivity activity = mActivityRef.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MSG_UPDATE_VIEW:
                    activity.updateView();
                    break;
                case MSG_REMOVE_CANCELED:
                    activity.mCancelledAddress = ADDRESS_NONE;
                    activity.updateView();
                    break;
                case MSG_PAIRING_COMPLETE:
                    activity.onAboutToFinish();
                    activity.finish();
                    break;
                case MSG_OP_TIMEOUT:
                    activity.handlePairingTimeout();
                    break;
                case MSG_RESTART:
                    if (activity.mBluetoothPairer != null) {
                        activity.mBluetoothPairer.start();
                        activity.mBluetoothPairer.cancelPairing();
                    }
                    break;
                case MSG_TRIGGER_SELECT_DOWN:
                    activity.sendKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, true);
                    activity.mHwKeyDidSelect = true;
                    sendEmptyMessageDelayed(MSG_TRIGGER_SELECT_UP, KEY_DOWN_TIME);
                    activity.cancelPairingCountdown();
                    break;
                case MSG_TRIGGER_SELECT_UP:
                    activity.sendKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER, false);
                    break;
                case MSG_START_AUTOPAIR_COUNTDOWN:
                    sendMessageDelayed(obtainMessage(MSG_AUTOPAIR_TICK,
                            AUTOPAIR_COUNT, 0, null), 1000);
                    break;
                case MSG_AUTOPAIR_TICK:
                    int countToAutoPair = msg.arg1 - 1;
                    if (countToAutoPair <= 0) {
                        // AutoPair
                        activity.startAutoPairing();
                    } else {
                        sendMessageDelayed(obtainMessage(MSG_AUTOPAIR_TICK,
                                countToAutoPair, 0, null), 1000);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Handler mAutoExitHandler = new Handler();

    private final Runnable mAutoExitRunnable = this::finish;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        myinstance = this;

		createPreferredLocalesList();
        insertPreferredLocalesIntoContentProvider();
        //if ("1".equals(SystemProperties.get(PROPERTY_PAIR_COMPLETE,"0"))) {
        //    Log.d(TAG,"Bluetooth device is already paired, so skip the configuration.");
        //    onAboutToFinish();
        //    finish();
        //}
        new Thread(new Runnable() {
            @Override
            public void run() {
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				if (adapter != null) {
					Log.d(TAG,"initBlueTooth state="+adapter.getState());
					if (!adapter.isEnabled()) {
						Log.d(TAG,"initBlueTooth isEnabled="+adapter.isEnabled());
						adapter.enable(); //sleep one second ,avoid do not discovery
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					int i = 0;
					while(adapter.getState() != 12 && i <= 5) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						i++;
					}
					Set<BluetoothDevice> devices = adapter.getBondedDevices();
					//if (devices.size() <= 0) {
					//	Log.d(TAG, "no paired BT RCU.");
					//	return;
					//}
					//for (BluetoothDevice bluetoothDevice : devices) {
					//	if (bluetoothDevice == null || bluetoothDevice.getName() == null) {
					//		Log.e(TAG, "device is null or device.getName is null");
					//	} else {
					//		Log.d(TAG, "remove device: "+bluetoothDevice.getName());
					//		bluetoothDevice.removeBond();
					//	}
					//}
                    for (BluetoothDevice bluetoothDevice : devices) {
                        Log.d(TAG, "initBlueTooth "+bluetoothDevice.getName()+" "+bluetoothDevice.getAddress());
                        if (new InputDeviceCriteria().isInputDevice(bluetoothDevice.getBluetoothClass())) {
                            Log.d(TAG,"Bluetooth device is already paired, so skip the configuration.");
                            onAboutToFinish();
                            finish();
                            break;
                        }
					}
				}else{
					Log.d(TAG,"initBlueTooth no bluetooth");
				}
            }
        }).start();
        setContentView(R.layout.activity_base_layout);
        skipButton = (Button)findViewById(R.id.skip);
        message = (TextView)findViewById(R.id.message);
        // message.setText(Html.fromHtml(descString(), getImage(), null));
        deviceName = (TextView)findViewById(R.id.devicename);
        deviceStatus = (TextView)findViewById(R.id.devicestatus);

        skipButton.setVisibility(View.INVISIBLE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG,"onClick do nothing");
                onAboutToFinish();
                finish();
            }
        });
        mMsgHandler.setActivity(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mNoInputMode = getIntent().getBooleanExtra(INTENT_EXTRA_NO_INPUT_MODE, false);
        mHwKeyDown = false;

        if (savedInstanceState == null) {
            mBluetoothDevices = new ArrayList<>();
        } else {
            mBluetoothDevices =
                    savedInstanceState.getParcelableArrayList(SAVED_STATE_BLUETOOTH_DEVICES);
        }

        initViewForAnimation();
        startRcuPressHintAnimation();
    }

	private void unpair_all_devices(){
		BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
		if (defaultAdapter != null) {
			Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
			if (bondedDevices.size() <= 0) {
				Log.d(TAG, "no paired BT RCU.");
				return;
			}
			for (BluetoothDevice bluetoothDevice : bondedDevices) {
				if (bluetoothDevice == null || bluetoothDevice.getName() == null) {
					Log.e(TAG, "device is null or device.getName is null");
				} else {
					Log.d(TAG, "remove device: "+bluetoothDevice.getName());
					bluetoothDevice.removeBond();
				}
			}
		}
	}
    private ImageGetter getImage() {
        ImageGetter imageGetter=new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                int id=Integer.parseInt(source);
                Drawable drawable=getResources().getDrawable(id);
                if (drawable != null) {
                	drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth()), (int) (drawable.getIntrinsicHeight()));
                }
                return drawable;
            }
        };
         return imageGetter;
    }
    private String descString() {
        return "Press \"OK\" key and\"" + "<img src='" + R.drawable.ic_apps_white+ "'/>" + "\" key to \n Pair Remote Device.";
    }

    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receiver action: " + intent.getAction());
            if (ACTION_GLOBAL_BUTTON_DMG.equals(intent.getAction())) {
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null) {
                    Log.d(TAG, "Received key from broadcast: " + event);
                    handleKeyEvent(event);
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        if (DEBUG) {
            Log.d(TAG, "onStart() mPairingInBackground = " + mPairingInBackground);
        }

        // Only do the following if we are not coming back to this activity from
        // the Secure Pairing activity.
        if (!mPairingInBackground) {
            if(BluetoothAdapter.getDefaultAdapter().isLeEnabled())
            	startBluetoothPairer();
        }

        mPairingInBackground = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        show_info_flag = 0;
        IntentFilter filter = new IntentFilter(ACTION_GLOBAL_BUTTON_DMG);
        registerReceiver(keyReceiver, filter);
        if (mNoInputMode) {
            // Start timer count down for exiting activity.
            if (DEBUG) Log.d(TAG, "starting auto-exit timer");
            mAutoExitHandler.postDelayed(mAutoExitRunnable, EXIT_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(keyReceiver);
        if (DEBUG) Log.d(TAG, "stopping auto-exit timer");
        if (!mSetResult) {
            Log.d(TAG, "onPause onAboutToFinish");
            onAboutToFinish();
        }
        mAutoExitHandler.removeCallbacks(mAutoExitRunnable);
    }


    @Override
    public void onStop() {
        if (DEBUG) {
            Log.d(TAG, "onStop()");
        }
        myinstance = null;
        if (!mPairingBluetooth) {
            stopBluetoothPairer();
            mMsgHandler.removeCallbacksAndMessages(null);
        } else {
            // allow activity to remain in the background while we perform the
            // BT Secure pairing.
            mPairingInBackground = true;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        stopBluetoothPairer();
        mMsgHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        int keyCode = event.getKeyCode();
	    if(keyCode == KeyEvent.KEYCODE_PROG_RED)
	   		cancel_pair += 1;
		Log.d(TAG, "cancel_pair = "+ cancel_pair);
		if(cancel_pair == 4){
			skipButton.setVisibility(View.VISIBLE);
		}
		Log.d(TAG, "dispatchKeyEvent "+keyCode);
		if(cancel_pair > 4)
			return super.dispatchKeyEvent(event);
		else
			return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            if (mPairingBluetooth && !mDone) {
                cancelBtPairing();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (ACTION_CONNECT_INPUT.equals(intent.getAction()) &&
                (intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) == 0) {

            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_PAIRING) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    onHwKeyEvent(false);
                } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    onHwKeyEvent(true);
                }
            }
        } else {
            setIntent(intent);
        }
    }

    public void onActionClicked(String address) {
        Log.d(TAG,"onActionClicked");
        cancelPairingCountdown();
        if (!mDone) {
            btDeviceClicked(address);
        }
    }

    public void reset_show_info_flag()
    {
        show_info_flag = 0;
    }

    private void handleKeyEvent(KeyEvent event) {
        Log.d("HookBeginActivity", "Handling forwarded KeyEvent: " + event.getKeyCode() + "show_info_flag: "+show_info_flag);
        if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_6) {
            show_info_flag += 1;
        }
        if(show_info_flag == 2)
            showStbInfoDialog();
    }

    private void showStbInfoDialog()
    {
        if(g_stbInfoDialog == null)
            g_stbInfoDialog = new StbInfoDialog(this);
        g_stbInfoDialog.show();
    }

    // Events related to a device HW key
    private void onHwKeyEvent(boolean keyDown) {
        if (!mHwKeyDown) {
            // HW key was in UP state before
            if (keyDown) {
                // Back key pressed down
                mHwKeyDown = true;
                mHwKeyDidSelect = false;
                mMsgHandler.sendEmptyMessageDelayed(MSG_TRIGGER_SELECT_DOWN, LONG_PRESS_DURATION);
            }
        } else {
            // HW key was in DOWN state before
            if (!keyDown) {
                // HW key released
                mHwKeyDown = false;
                mMsgHandler.removeMessages(MSG_TRIGGER_SELECT_DOWN);
                if (!mHwKeyDidSelect) {
                    // key wasn't pressed long enough for selection, move selection
                    // to next item.
                    //mPreferenceFragment.advanceSelection();
                }
                mHwKeyDidSelect = false;
            }
        }
    }

    private void sendKeyEvent(int keyCode, boolean down) {
        InputManager iMgr = (InputManager) getSystemService(INPUT_SERVICE);
        if (iMgr != null) {
            long time = SystemClock.uptimeMillis();
            KeyEvent evt = new KeyEvent(time, time,
                    down ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP,
                    keyCode, 0);
            iMgr.injectInputEvent(evt, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        }
    }

    protected void updateView() {
        Log.d(TAG,"updateView");
        for (BluetoothDevice bt : mBluetoothDevices) {
            if (bt.getName() != null && bt.getAddress().equals(mCurrentTargetAddress)) {
                if (DEBUG) Log.d(TAG,"updateView name="+bt.getName()+" address="+bt.getAddress());
                deviceName.setText(bt.getName());
                deviceStatus.setText(mCurrentTargetStatus);
                break;
            }
        }
        return;
    }
	

    private void cancelPairingCountdown() {
        // Cancel countdown
        mMsgHandler.removeMessages(MSG_AUTOPAIR_TICK);
        mMsgHandler.removeMessages(MSG_START_AUTOPAIR_COUNTDOWN);
    }

    private void setTimeout(int timeout) {
        cancelTimeout();
        mMsgHandler.sendEmptyMessageDelayed(MSG_OP_TIMEOUT, timeout);
    }

    private void cancelTimeout() {
        mMsgHandler.removeMessages(MSG_OP_TIMEOUT);
    }

    protected void startAutoPairing() {
        if (DEBUG) Log.d(TAG,"startAutoPairing");
        if (mBluetoothDevices.size() > 0) {
            onActionClicked(mBluetoothDevices.get(0).getAddress());
        }
    }

    private void btDeviceClicked(String clickedAddress) {
        if (DEBUG) Log.d(TAG,"btDeviceClicked clickedAddress="+clickedAddress);
        if (mBluetoothPairer != null && !mBluetoothPairer.isInProgress()) {
            if (mBluetoothPairer.getStatus() == BluetoothDevicePairer.STATUS_WAITING_TO_PAIR &&
                    mBluetoothPairer.getTargetDevice() != null) {
                cancelBtPairing();
            } else {
                if (DEBUG) {
                    Log.d(TAG, "Looking for " + clickedAddress +
                            " in available devices to start pairing");
                }
                for (BluetoothDevice target : mBluetoothDevices) {
                    if (target.getAddress().equalsIgnoreCase(clickedAddress)) {
                        if (DEBUG) {
                            Log.d(TAG, "Found it!");
                        }
                        mCancelledAddress = ADDRESS_NONE;
                        setPairingBluetooth(true);
                        mBluetoothPairer.startPairing(target);
                        break;
                    }
                }
            }
        }
    }

    private void cancelBtPairing() {
        if (DEBUG) Log.d(TAG,"cancelBtPairing");
        // cancel current request to pair
        if (mBluetoothPairer != null) {
            if (mBluetoothPairer.getTargetDevice() != null) {
                mCancelledAddress = mBluetoothPairer.getTargetDevice().getAddress();
            } else {
                mCancelledAddress = ADDRESS_NONE;
            }
            mBluetoothPairer.cancelPairing();
        }
        mPairingSuccess = false;
        setPairingBluetooth(false);
        mMsgHandler.sendEmptyMessageDelayed(MSG_REMOVE_CANCELED,
                CANCEL_MESSAGE_TIMEOUT);
    }

    private void setPairingBluetooth(boolean pairing) {
        if (DEBUG) Log.d(TAG,"setPairingBluetooth");
        if (mPairingBluetooth != pairing) {
            mPairingBluetooth = pairing;
        }
    }

    private void startBluetoothPairer() {
        if (DEBUG) Log.d(TAG,"startBluetoothPairer");
        stopBluetoothPairer();
        mBluetoothPairer = new BluetoothDevicePairer(this, this);
        mBluetoothPairer.start();

        mBluetoothPairer.disableAutoPairing();

        mPairingSuccess = false;
        statusChanged();
    }

    private void stopBluetoothPairer() {
        if (DEBUG) Log.d(TAG,"stopBluetoothPairer");
        if (mBluetoothPairer != null) {
            mBluetoothPairer.setListener(null);
            mBluetoothPairer.dispose();
            mBluetoothPairer = null;
        }
    }

    private String getMessageForStatus(int status) {
        final int msgId;
        String msg;

        switch (status) {
            case BluetoothDevicePairer.STATUS_WAITING_TO_PAIR:
            case BluetoothDevicePairer.STATUS_PAIRING:
                msgId = R.string.accessory_state_pairing;
                break;
            case BluetoothDevicePairer.STATUS_CONNECTING:
                msgId = R.string.accessory_state_connecting;
                break;
            case BluetoothDevicePairer.STATUS_ERROR:
                msgId = R.string.accessory_state_error;
                break;
            default:
                return "";
        }

        msg = getString(msgId);

        return msg;
    }

    @Override
    public void statusChanged() {
        Log.d(TAG,"statusChanged");
        if (mBluetoothPairer == null) return;

        //int numDevices = mBluetoothPairer.getAvailableDevices().size();
        int status = mBluetoothPairer.getStatus();
        int oldStatus = mPreviousStatus;
        mPreviousStatus = status;

        String address = mBluetoothPairer.getTargetDevice() == null ? ADDRESS_NONE :
                mBluetoothPairer.getTargetDevice().getAddress();

        if (DEBUG) {
            String state = "?";
            switch (status) {
                case BluetoothDevicePairer.STATUS_NONE:
                    state = "BluetoothDevicePairer.STATUS_NONE";
                    break;
                case BluetoothDevicePairer.STATUS_SCANNING:
                    state = "BluetoothDevicePairer.STATUS_SCANNING";
                    break;
                case BluetoothDevicePairer.STATUS_WAITING_TO_PAIR:
                    state = "BluetoothDevicePairer.STATUS_WAITING_TO_PAIR";
                    break;
                case BluetoothDevicePairer.STATUS_PAIRING:
                    state = "BluetoothDevicePairer.STATUS_PAIRING";
                    break;
                case BluetoothDevicePairer.STATUS_CONNECTING:
                    state = "BluetoothDevicePairer.STATUS_CONNECTING";
                    break;
                case BluetoothDevicePairer.STATUS_ERROR:
                    state = "BluetoothDevicePairer.STATUS_ERROR";
                    break;
            }
            long time = mBluetoothPairer.getNextStageTime() - SystemClock.elapsedRealtime();
            int numDevices = mBluetoothPairer.getAvailableDevices().size();
            Log.d(TAG, "Update received, number of devices:" + numDevices + " state: " +
                    state + " target device: " + address + " time to next event: " + time);
        }
		if(mBluetoothDevices == null)
			mBluetoothDevices = new ArrayList<>();
			
		if(mBluetoothDevices != null){
			mBluetoothDevices.clear();
			mBluetoothDevices.addAll(mBluetoothPairer.getAvailableDevices());
		}

        cancelTimeout();

        switch (status) {
            case BluetoothDevicePairer.STATUS_NONE:
            Log.d(TAG,"statusChanged STATUS_NONE");
                // if we just connected to something or just tried to connect
                // to something, restart scanning just in case the user wants
                // to pair another device.
                if (oldStatus == BluetoothDevicePairer.STATUS_CONNECTING) {
                    if (mPairingSuccess) {
                        Log.d(TAG,"statusChanged STATUS_NONE STATUS_CONNECTING mPairingSuccess");
                        // Pairing complete
                        mCurrentTargetStatus = getString(R.string.accessory_state_paired);
                        mMsgHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
                        mMsgHandler.sendEmptyMessageDelayed(MSG_PAIRING_COMPLETE,
                                DONE_MESSAGE_TIMEOUT);
                        mDone = true;
                        //SystemProperties.set(PROPERTY_PAIR_COMPLETE, "1");
                        // Done, return here and just wait for the message
                        // to close the activity
                        return;
                    }
                    if (DEBUG) {
                        Log.d(TAG, "Invalidating and restarting.");
                    }

                    mBluetoothPairer.invalidateDevice(mBluetoothPairer.getTargetDevice());
                    mBluetoothPairer.start();
                    mBluetoothPairer.cancelPairing();
                    setPairingBluetooth(false);

                    // if this looks like a successful connection run, reflect
                    // this in the UI, otherwise use the default message
                    if (!mPairingSuccess && BluetoothDevicePairer.hasValidInputDevice(this)) {
                        mPairingSuccess = true;
                    }
                }
                break;
            case BluetoothDevicePairer.STATUS_SCANNING:
                mPairingSuccess = false;
                show_bt_pair();
                break;
            case BluetoothDevicePairer.STATUS_WAITING_TO_PAIR:
                mParingAddress = address;
                startRcuPairingAnimation();
                break;
            case BluetoothDevicePairer.STATUS_PAIRING:
                // reset the pairing success value since this is now a new
                // pairing run
                mPairingSuccess = true;
                show_bt_pairing();
                setTimeout(PAIR_OPERATION_TIMEOUT);
                break;
            case BluetoothDevicePairer.STATUS_CONNECTING:
                completeRcuPairing();
                show_bt_pair_success();
                mParingAddress = "";
                setTimeout(CONNECT_OPERATION_TIMEOUT);
                break;
            case BluetoothDevicePairer.STATUS_ERROR:
                mPairingSuccess = false;
                setPairingBluetooth(false);
                if (mNoInputMode) {
                    clearDeviceList();
                }
                initViewForAnimation();

                if (mParingAddress.equals(address)) {
                    show_bt_pair_fail();
                } 
                break;
        }

        mCurrentTargetAddress = address;
        mCurrentTargetStatus = getMessageForStatus(status);
        if (status == BluetoothDevicePairer.STATUS_SCANNING) {
            List<BluetoothScanner.Device> mBluetoothScannerDevices = mBluetoothPairer.getAvailableScannerDevices();
            for (BluetoothScanner.Device bt : mBluetoothScannerDevices) {
                //Log.d(TAG,"statusChanged name="+bt.btDevice.getName()+" address="+bt.btDevice.getAddress()+" isinput="+
                //new InputDeviceCriteria().isInputDevice(bt.btDevice.getBluetoothClass())+" type="+bt.btDevice.getType()+
                //" mRssi="+bt.mRssi);
                if (new InputDeviceCriteria().isInputDevice(bt.btDevice.getBluetoothClass())
                    && BluetoothDevice.DEVICE_TYPE_LE == bt.btDevice.getType()) {
                    Log.d(TAG,"statusChanged name="+bt.btDevice.getName()+" address="+bt.btDevice.getAddress()+" start pair");
                    onActionClicked(bt.btDevice.getAddress());
                    break;
                }
            }
        }
        mMsgHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
    }

    private void clearDeviceList() {
        mBluetoothDevices.clear();
        mBluetoothPairer.clearDeviceList();
    }

    private void handlePairingTimeout() {
        if (DEBUG) Log.d(TAG,"handlePairingTimeout");
        if (mPairingInBackground) {
            finish();
        } else {
            // Either Pairing or Connecting timeout out.
            // Display error message and post delayed message to the scanning process.
            mPairingSuccess = false;
            if (mBluetoothPairer != null) {
                mBluetoothPairer.cancelPairing();
            }
            mCurrentTargetStatus = getString(R.string.accessory_state_error);
            mMsgHandler.sendEmptyMessage(MSG_UPDATE_VIEW);
            mMsgHandler.sendEmptyMessageDelayed(MSG_RESTART, RESTART_DELAY);
        }
    }

    List<BluetoothDevice> getBluetoothDevices() {
        return mBluetoothDevices;
    }

    String getCurrentTargetAddress() {
        return mCurrentTargetAddress;
    }

    String getCurrentTargetStatus() {
        return mCurrentTargetStatus;
    }

    String getCancelledAddress() {
        return mCancelledAddress;
    }

    protected void onAboutToFinish() {
        Log.d(TAG,"onAboutToFinish");
        mSetResult = true;
        setResult(Activity.RESULT_OK);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        /*
            in BluetoothScanner.java
            every time come in startListening while mClients.size() > 1,
            listener always same one, but mClients.get(ptr).listener always the new one
            while user press global key, we call stopNow to stop scan,
            but next time call up BtPair will not regist ACTION_FOUND ACTION_DISCOVERY_FINISHED ( size > 1 ),
            to make sure clean data, use force clear app stack when press global key
        */
        Log.d(TAG,"onUserLeaveHint : user press global key");
        // finish whole App
        finishAffinity();
    }
    // Animation: init views for animation
    void initViewForAnimation() {
        Log.d(TAG, "initViewForAnimation");
        rcuPressHint = findViewById(R.id.bt_controller_highlight);
        rcuGreenLight = findViewById(R.id.bt_controller_light);
        rcuDotConnecting = findViewById(R.id.dot_connecting);

        rcuPressHint.setStateListAnimator(AnimatorInflater.loadStateListAnimator(HookBeginActivity.this, R.animator.rcu_highlight_fade_in_out));
        rcuPressHint.setSelected(false);
        rcuGreenLight.setStateListAnimator(AnimatorInflater.loadStateListAnimator(HookBeginActivity.this, R.animator.rcu_light_fade_in_out));
        rcuGreenLight.setSelected(false);

        handler = new Handler();

        if (!isTvSetupComplete()) {
            Log.d(TAG, "initViewForAnimation: init text");
            TextView pairHintText = findViewById(R.id.hint_pair_description);
            TextView dotConnectedText = findViewById(R.id.dot_connecting_text);
            TextView connectedText = findViewById(R.id.status_connected_text);
            pairHintText.setText(R.string.init_before_pair_hint_1);
            dotConnectedText.setText(R.string.init_before_pair_hint_2);
            connectedText.setText(R.string.init_rcu_pair_complete);
            //SystemProperties.get()
        }
    }

    boolean isTvSetupComplete() {
        return Settings.Secure.getInt(
            getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
    }

    // Animaton: hint press buttons
    void startRcuPressHintAnimation() {

        isAnimationGreenLight = false;
        isAnimationDotConnectting = false;

        handler.post(new Runnable() {
            boolean fadeIn = true;
            int count = 0;
            @Override
            public void run() {
                startGlowAnimation(rcuPressHint);
                if (fadeIn) {
                    rcuPressHint.setVisibility(View.VISIBLE);
                    rcuPressHint.setStateListAnimator(AnimatorInflater.loadStateListAnimator(HookBeginActivity.this, R.animator.rcu_highlight_fade_in_out));
                    rcuPressHint.setSelected(true);
                    fadeIn = false;
                }
                else {
                    rcuPressHint.setSelected(false);
                    fadeIn = true;
                    if (++count == 4) {
                        View hintPairDescription = findViewById(R.id.hint_pair_description);
                        hintPairDescription.setVisibility(View.INVISIBLE);
                        startRcuGreenLightAnimation(400, false);
                        startDotConnectAnimation(200, false);
                        return;
                    }
                }
                handler.postDelayed(this, 500);
            }
        });
    }

    // Animaton: green light
    void startRcuGreenLightAnimation(int duration, boolean isPairing) {

        if (isAnimationGreenLight)
            return;
        isAnimationGreenLight = true;

        handler.post(new Runnable() {
            boolean fadeIn = true;
            int count = 0;
            @Override
            public void run() {
                startGlowAnimation(rcuGreenLight);
                if (fadeIn) {
                    rcuGreenLight.setVisibility(View.VISIBLE);
                    rcuGreenLight.setStateListAnimator(AnimatorInflater.loadStateListAnimator(HookBeginActivity.this, R.animator.rcu_light_fade_in_out));
                    rcuGreenLight.setSelected(true);
                    fadeIn = false;
                }
                else {
                    rcuGreenLight.setSelected(false);
                    fadeIn = true;
                    if (!isPairing && ++count == 6) {
                        rcuGreenLight.setVisibility(View.INVISIBLE);
                        return;
                    }
                }
                handler.postDelayed(this, duration);
            }
        });
    }

    // Animaton: dot connecting
    void startDotConnectAnimation(int duration, boolean isPairing) {

        if (isAnimationDotConnectting)
            return;
        isAnimationDotConnectting = true;

        handler.post(new Runnable() {
            final View hintPairDescription = findViewById(R.id.hint_pair_description);
            int index = 0;
            int count = 0;
            @Override
            public void run() {
                int resId = R.id.dot_connecting_1;

                if (rcuDotView != null) {
                    rcuDotView.setSelected(false);
                    rcuDotView = null;
                    if (!isPairing && ++count == 12) {
                        rcuDotConnecting.setVisibility(View.INVISIBLE);
                        hintPairDescription.setVisibility(View.VISIBLE);
                        startRcuPressHintAnimation();
                        return;
                    }
                }
                else {
                    rcuDotConnecting.setVisibility(View.VISIBLE);
                    index %= 6;
                    if (index == 0) resId = R.id.dot_connecting_1;
                    if (index == 1) resId = R.id.dot_connecting_2;
                    if (index == 2) resId = R.id.dot_connecting_3;
                    if (index == 3) resId = R.id.dot_connecting_4;
                    if (index == 4) resId = R.id.dot_connecting_5;
                    if (index == 5) resId = R.id.dot_connecting_6;
                    index++;
                    rcuDotView = findViewById(resId);
                    startGlowAnimation(rcuDotView);
                    rcuDotView.setStateListAnimator(AnimatorInflater.loadStateListAnimator(HookBeginActivity.this, R.animator.circle_animator));
                    rcuDotView.setSelected(true);
                }
                handler.postDelayed(this, duration);
            }
        });
    }

    // Animaton: in pairing & not complete
    void startRcuPairingAnimation() {
        ImageView rcu50cm = findViewById(R.id.rcu_50cm);
        TextView rcuPairDescription = findViewById(R.id.hint_pair_description);
        TextView dotConnectText = findViewById(R.id.dot_connecting_text);

        rcuPressHint.setVisibility(View.INVISIBLE);
        rcu50cm.setVisibility(View.INVISIBLE);
        rcuPairDescription.setVisibility(View.INVISIBLE);
        rcuDotConnecting.setVisibility(View.VISIBLE);
        dotConnectText.setVisibility(View.VISIBLE);
        dotConnectText.setText(R.string.pairing_please_wait);

        if (!isTvSetupComplete()) {
            dotConnectText.setText(R.string.init_pairing_please_wait);
        }

        if (rcuGreenLight != null)
            rcuGreenLight.setSelected(false);
        if (rcuDotView != null)
            rcuDotView.setSelected(false);

        handler.removeCallbacksAndMessages(null);

        isAnimationGreenLight = false;
        isAnimationDotConnectting = false;

        startRcuGreenLightAnimation(200, true);
        startDotConnectAnimation(100, true);
    }

    // Animaton: complete pairing & connected
    void completeRcuPairing() {
        ImageView rcu = findViewById(R.id.bt_controller);
        View connectedRoot = findViewById(R.id.status_connected_root);
        ImageView connectedImage = findViewById(R.id.status_connected_image);

        handler.removeCallbacksAndMessages(null);

        rcu.setImageResource(R.drawable.bt_controller_focus);
        rcuGreenLight.setVisibility(View.INVISIBLE);
        rcuDotConnecting.setVisibility(View.INVISIBLE);
        connectedRoot.setVisibility(View.VISIBLE);
        connectedImage.setVisibility(View.VISIBLE);
        connectedImage.setStateListAnimator(AnimatorInflater.loadStateListAnimator(this, R.animator.rcu_highlight_fade_in_out));
        connectedImage.setSelected(true);
    }

    // Animaton: show light glow
    private void startGlowAnimation(ImageView imageView) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2000); // 持续时间2秒
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            int glowColor = adjustAlpha(0xFFFFD700, animatedValue); // 金色发光

            // 创建ColorFilter
            ColorFilter colorFilter = new LightingColorFilter(glowColor, 1);
            imageView.setColorFilter(colorFilter);
        });

        animator.start();
    }

    // Animaton: for light glow
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(255 * factor);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

        private void show_bt_pair() {
        ImageView btPair = findViewById(R.id.bt_pair);
        ImageView btPairing = findViewById(R.id.bt_pairing);
        ImageView btPairSuccess = findViewById(R.id.bt_pair_success);
        ImageView btPairFail = findViewById(R.id.bt_pair_fail);

        btPair.setVisibility(View.VISIBLE);
        btPairing.setVisibility(View.INVISIBLE);
        btPairSuccess.setVisibility(View.INVISIBLE);
        btPairFail.setVisibility(View.INVISIBLE);
    }

    private void show_bt_pairing() {
        ImageView btPair = findViewById(R.id.bt_pair);
        ImageView btPairing = findViewById(R.id.bt_pairing);
        ImageView btPairSuccess = findViewById(R.id.bt_pair_success);
        ImageView btPairFail = findViewById(R.id.bt_pair_fail);

        btPair.setVisibility(View.INVISIBLE);
        btPairing.setVisibility(View.VISIBLE);
        btPairSuccess.setVisibility(View.INVISIBLE);
        btPairFail.setVisibility(View.INVISIBLE);
    }

    private void show_bt_pair_success() {
        ImageView btPair = findViewById(R.id.bt_pair);
        ImageView btPairing = findViewById(R.id.bt_pairing);
        ImageView btPairSuccess = findViewById(R.id.bt_pair_success);
        ImageView btPairFail = findViewById(R.id.bt_pair_fail);

        btPair.setVisibility(View.INVISIBLE);
        btPairing.setVisibility(View.INVISIBLE);
        btPairSuccess.setVisibility(View.VISIBLE);
        btPairFail.setVisibility(View.INVISIBLE);
    }

    private void show_bt_pair_fail() {
        ImageView btPair = findViewById(R.id.bt_pair);
        ImageView btPairing = findViewById(R.id.bt_pairing);
        ImageView btPairSuccess = findViewById(R.id.bt_pair_success);
        ImageView btPairFail = findViewById(R.id.bt_pair_fail);

        btPair.setVisibility(View.INVISIBLE);
        btPairing.setVisibility(View.INVISIBLE);
        btPairSuccess.setVisibility(View.INVISIBLE);
        btPairFail.setVisibility(View.VISIBLE);
    }

    private void createPreferredLocalesList() {
        preferredLocalesList = new ArrayList<>();
        preferredLocalesList.add("zh-TW");
        preferredLocalesList.add("en-US");

        // Option to show/hide other supported locales that are not included in the preferred locale list.
        showOtherLocales = false;
    }	

    private void insertPreferredLocalesIntoContentProvider() {
        for (int rank = 0; rank < preferredLocalesList.size(); rank++) {
            String localeTag = preferredLocalesList.get(rank);

            ContentValues values = new ContentValues();
            values.put(LOCALE_COLUMN_NAME, localeTag);
            values.put(RANK_COLUMN_NAME, rank);

            getContentResolver().insert(CONTENT_URI, values);
        }

        // Add the wildcard value in order to show/hide the remaining device-supported locales below the preferred locales.
        ContentValues values = new ContentValues();
        values.put(LOCALE_COLUMN_NAME, LOCALE_WILDCARD_CHAR);
        values.put(RANK_COLUMN_NAME, showOtherLocales ? 1 : 0);
        getContentResolver().insert(CONTENT_URI, values);
    }	
}