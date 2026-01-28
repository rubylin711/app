package com.prime.homeplus.settings.persional;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;

import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsItemData;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

public class STBDataReturnView extends ThirdLevelView {
    private String TAG = "HomePlus-STBDataReturnView";
    private SettingsRecyclerView secondDepthView;
    private boolean stbdata_return = true;

    public STBDataReturnView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);

        this.secondDepthView = secondDepthView;
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_language;
    }

    private RadioButton rbtn1, rbtn2;

    public void onFocus() {
        Log.d(TAG, "onFocus() !!!");
        updateData();
        //rbtn1.requestFocus();
    }

    @Override
    public void onViewCreated() {
        rbtn1 = (RadioButton) findViewById(R.id.rbtn1);
        rbtn2 = (RadioButton) findViewById(R.id.rbtn2);

        rbtn1.setOnFocusChangeListener(OnFocusChangeListener);
        rbtn2.setOnFocusChangeListener(OnFocusChangeListener);

        rbtn1.setText(getContext().getString(R.string.settings_datareturn_enable));

        rbtn1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
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

        rbtn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rbtn2.setChecked(false);
                stbdata_return = true;
                saveData();
            }
        });

        rbtn2.setText(getContext().getString(R.string.settings_datareturn_disable));

        rbtn2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        settingsRecyclerView.focusDown();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

                    }
                }
                return false;
            }
        });

        rbtn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rbtn1.setChecked(false);
                stbdata_return = false;
                saveData();
            }
        });
    }

    private void updateData() {
        //String status = "true";
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // get data return setting value
        if (PrimeUtils.get_gpos_info() != null)
            stbdata_return = (GposInfo.getSTBDataReturn(getContext())==1)?true:false;


        if (stbdata_return) {
            rbtn1.setChecked(true);
            rbtn1.requestFocus();
            rbtn2.setChecked(false);
        } else {
            rbtn1.setChecked(false);
            rbtn2.setChecked(true);
            rbtn2.requestFocus();
        }
    }

    private void saveData() {
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // save data return setting value
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        if(gposInfo != null){
            GposInfo.setSTBDataReturn(getContext(), (stbdata_return)?1:0);
            //PrimeUtils.g_prime_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_STBDATA_RETURN, gposInfo.getSTBDataReturn());
        }
        String returnData;
        if (stbdata_return) {
            returnData = getResources().getString(R.string.open);
        } else {
            returnData = getResources().getString(R.string.close);
        }

        SettingsItemData settingsItems = secondDepthView.settingsItems.get(secondDepthView.getViewIndex());
        settingsItems.setValue(returnData);

        secondDepthView.notifyDataSetChanged();
    }
}
