package com.prime.homeplus.settings.persional;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

@SuppressLint("NewApi")
public class RemoteControlView extends ThirdLevelView {
    private String TAG = "HomePlus-RemoteControlView";
    private SettingsRecyclerView settingsRecyclerView;
    private Context mContext;

    public RemoteControlView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
        mContext = context;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_remotecontrol;
    }

    private Button btnRemoteControlUpgrade, btnRemoteControl, btnRemoteControlVolumeLock;

    public void onFocus() {
        btnRemoteControlUpgrade.requestFocus();
    }

    public void onViewPaused() {
    }

    public void onViewResumed() {
    }

    @Override
    public void onViewCreated() {
        btnRemoteControl = (Button) findViewById(R.id.btnRemoteControl);
        btnRemoteControl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnRemoteControl.setTypeface(null, Typeface.BOLD);
                    btnRemoteControl.setTextSize(20);
                } else {
                    btnRemoteControl.setTypeface(null, Typeface.NORMAL);
                    btnRemoteControl.setTextSize(17);
                }
            }
        });

        btnRemoteControl.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    }
                }
                return false;
            }
        });

        btnRemoteControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, R.string.settings_personal_rcu_rcuNotSupport, Toast.LENGTH_SHORT).show();
            }
        });

        btnRemoteControlUpgrade = (Button) findViewById(R.id.btnRemoteControlUpgrade);
        btnRemoteControlUpgrade.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnRemoteControlUpgrade.setTypeface(null, Typeface.BOLD);
                    btnRemoteControlUpgrade.setTextSize(20);
                } else {
                    btnRemoteControlUpgrade.setTypeface(null, Typeface.NORMAL);
                    btnRemoteControlUpgrade.setTextSize(17);
                }
            }
        });

        btnRemoteControlUpgrade.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            settingsRecyclerView.focusUp();
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            settingsRecyclerView.focusDown();
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            settingsRecyclerView.backToList();
                        }
                    }
                }
                return false;
            }
        });

        btnRemoteControlUpgrade.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, R.string.settings_personal_rcu_rcuNotSupport, Toast.LENGTH_SHORT).show();
            }
        });

        btnRemoteControlVolumeLock = (Button) findViewById(R.id.btnRemoteControlVolumeLock);
        btnRemoteControlVolumeLock.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnRemoteControlVolumeLock.setTypeface(null, Typeface.BOLD);
                    btnRemoteControlVolumeLock.setTextSize(20);
                } else {
                    btnRemoteControlVolumeLock.setTypeface(null, Typeface.NORMAL);
                    btnRemoteControlVolumeLock.setTextSize(17);
                }
            }
        });

        btnRemoteControlVolumeLock.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            settingsRecyclerView.focusUp();
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            settingsRecyclerView.focusDown();
                        }
                    }
                }
                return false;
            }
        });

        btnRemoteControlVolumeLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, R.string.settings_personal_rcu_rcuNotSupport, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
