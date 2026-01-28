package com.prime.homeplus.settings.system;

import android.app.ActionBar;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class ResetToDefaultView extends ThirdLevelView {
    private String TAG = "HomePlus-ResetToDefaultView";
    private SettingsRecyclerView settingsRecyclerView;
    private Context appContext;

    public ResetToDefaultView(int i, Context context, SettingsRecyclerView settingsRecyclerView) {
        super(i, context, settingsRecyclerView);
        this.settingsRecyclerView = settingsRecyclerView;
        this.appContext = context;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_reset;
    }

    private Button btnReset;

    public void onFocus() {
        btnReset.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initPopWindow();

        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        settingsRecyclerView.backToList();
                    }
                }
                return false;
            }
        });

        btnReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
            }
        });
    }

    private View popupView;
    private static PopupWindow popupWindow;
    private Button btnSave, btnCancel;

    private void initPopWindow() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reset_to_default, null);
        popupWindow = new PopupWindow(popupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
        btnSave = (Button) popupView.findViewById(R.id.btnSave);
        btnCancel = (Button) popupView.findViewById(R.id.btnCancel);
        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PrimeUtils.do_factory_reset();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }
}
