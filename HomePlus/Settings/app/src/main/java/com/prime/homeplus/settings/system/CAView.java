package com.prime.homeplus.settings.system;

import android.app.ActionBar;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.SystemInfo;
import com.prime.homeplus.settings.InfoUtils;
import com.prime.homeplus.settings.LogUtils;
import com.prime.homeplus.settings.PrimeUtils;
import com.prime.homeplus.settings.R;
import com.prime.homeplus.settings.SettingsRecyclerView;
import com.prime.homeplus.settings.ThirdLevelView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CAView extends ThirdLevelView {
    private String TAG = "HomePlus-CAView";



    private String selectOperatorId = "0";

    public CAView(int i, Context context, SettingsRecyclerView secondDepthView) {
        super(i, context, secondDepthView);
    }

    @Override
    public int loadLayoutResId() {
        return R.layout.settings_ca;
    }

    private Button btn3, btn4;

    public void onFocus() {
        btn3.requestFocus();
    }

    @Override
    public void onViewCreated() {
        initOperatorInfo();
        initInfo();

        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);

        btn3.setOnFocusChangeListener(OnFocusChangeListener);
        btn4.setOnFocusChangeListener(OnFocusChangeListener);

        btn3.setOnKeyListener(new OnKeyListener() {
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

        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showOperatorInfo();
            }
        });

        btn4.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
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

        btn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getCAInfo();
                popupWindowInfo.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
            }
        });
    }

    private void initPopWindow() {
        initOperatorInfo();
        initInfo();
    }





    private View popupViewOperatorInfo;
    private PopupWindow popupWindowOperatorInfo;

    private ListView lvOperatorInfo;

    private Button btnAuthorize, btnAC;
    private LinearLayout llAuthorize, llAC;
    private int operatorBtnIndex = 0;
    private CasData mCasData;

    private void initOperatorInfo() {
        popupViewOperatorInfo = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ca_operator_info, null);
        popupWindowOperatorInfo = new PopupWindow(popupViewOperatorInfo, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        lvOperatorInfo = (ListView) popupViewOperatorInfo.findViewById(R.id.lvOperatorInfo);

        btnAuthorize = (Button) popupViewOperatorInfo.findViewById(R.id.btnAuthorize);
        btnAC = (Button) popupViewOperatorInfo.findViewById(R.id.btnAC);

        btnAuthorize.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnAC.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        llAuthorize = (LinearLayout) popupViewOperatorInfo.findViewById(R.id.llAuthorize);
        llAC = (LinearLayout) popupViewOperatorInfo.findViewById(R.id.llAC);

        lvOperatorInfo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (lvOperatorInfo.getSelectedItemPosition() == 0) {
                            switch (operatorBtnIndex) {
                                case 0:
                                    btnAuthorize.requestFocus();
                                    break;
                                case 1:
                                    btnAC.requestFocus();
                                    break;
                            }
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        //v.setBackgroundColor(getResources().getColor(R.color.colorTabSelect));
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (operatorBtnIndex == 0) {
                            operatorBtnIndex = operatorBtnIndex + 1;
                        }

                        switch (operatorBtnIndex) {
                            case 0:
                                btnAuthorize.requestFocus();
                                break;
                            case 1:
                                btnAC.requestFocus();
                                break;
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (operatorBtnIndex == 1) {
                            operatorBtnIndex = operatorBtnIndex - 1;
                        }

                        switch (operatorBtnIndex) {
                            case 0:
                                btnAuthorize.requestFocus();
                                break;
                            case 1:
                                btnAC.requestFocus();
                                break;
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        btnAuthorize.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if (hasFocus) {
                    btnAuthorize.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_item2));
                    btnAC.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_item2));

                    operatorBtnIndex = 0;
                    llAuthorize.setVisibility(VISIBLE);
                    llAC.setVisibility(GONE);
                    lvOperatorInfo.setAdapter(getOperatorInfoListAdapter());
                }
            }
        });

        btnAuthorize.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        //settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        v.setBackgroundColor(getResources().getColor(R.color.colorTabSelect));
                    }
                }
                return false;
            }
        });



        btnAC.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if (hasFocus) {
                    btnAuthorize.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_item2));
                    btnAC.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_item2));

                    operatorBtnIndex = 1;

                    llAuthorize.setVisibility(GONE);
                    llAC.setVisibility(VISIBLE);

                    lvOperatorInfo.setAdapter(getOperatorACListAdapter());
                }
            }
        });

        btnAC.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        //settingsRecyclerView.focusUp();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        v.setBackgroundColor(getResources().getColor(R.color.colorTabSelect));
                    }
                }
                return false;
            }
        });
    }

    private void showOperatorInfo() {
        lvOperatorInfo.setAdapter(getOperatorInfoListAdapter());
        popupWindowOperatorInfo.showAtLocation(settingsRecyclerView, Gravity.RIGHT, 0, 0);
    }

    private SimpleAdapter getOperatorInfoListAdapter() {
        return new SimpleAdapter(getContext(), getOperatorEntitleLisData(), R.layout.dialog_ca_entitle_list_item,
                new String[]{"item_id", "item_tape"}, new int[]{R.id.item_id, R.id.item_tape});
    }

    private List<Map<String, Object>> getOperatorEntitleLisData() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        if (mCasData == null) {
            mCasData = PrimeUtils.get_Widevine_CasData();
        }

        if (mCasData == null) {
            return mList;
        }

        Set<String> allIds = new HashSet<>();
        if (mCasData.getEntitledChannelIds() != null) {
            allIds.addAll(mCasData.getEntitledChannelIds());
        }
        if (mCasData.getPromotionChannelIds() != null) {
            allIds.addAll(mCasData.getPromotionChannelIds());
        }

        List<String> sortedIds = new ArrayList<>(allIds);
        Collections.sort(sortedIds);

        for (String id : sortedIds) {
            Map<String, Object> map = new HashMap<String, Object>();
            boolean isEntitled = mCasData.getEntitledChannelIds() != null && mCasData.getEntitledChannelIds().contains(id);
            boolean isPromo = mCasData.getPromotionChannelIds() != null && mCasData.getPromotionChannelIds().contains(id);

            String val = (isEntitled ? "1" : "0") + "," + (isPromo ? "1" : "0");

            map.put("item_id", id);
            map.put("item_tape", val);
            mList.add(map);
        }

        return mList;
    }



    private SimpleAdapter getOperatorACListAdapter() {
        return new SimpleAdapter(getContext(), getOperatorACLisData(), R.layout.dialog_ca_ac_list_item,
                new String[]{"item_id", "item_value"}, new int[]{R.id.item_id, R.id.item_value});
    }

    private List<Map<String, Object>> getOperatorACLisData() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();

        if (mCasData == null) {
            mCasData = PrimeUtils.get_Widevine_CasData();
        }

        if (mCasData == null) {
            LogUtils.e("mCasData is null");
            return mList;
        }
        LogUtils.d("mCasData => "+mCasData.toString());

        // mSuspended
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("item_id", "mSuspended");
        map.put("item_value", String.valueOf(mCasData.getSuspended()));
        mList.add(map);

        // ac1
        map = new HashMap<String, Object>();
        map.put("item_id", "AreaCode");
        map.put("item_value", mCasData.getAreaCode());
        mList.add(map);

        // ac2
        map = new HashMap<String, Object>();
        map.put("item_id", "Bouquet ID");
        map.put("item_value", mCasData.getBouquetId());
        mList.add(map);

        // ac3
        map = new HashMap<String, Object>();
        map.put("item_id", "ZIPCode");
        map.put("item_value", mCasData.getZipCode());
        mList.add(map);

        // ac4
        map = new HashMap<String, Object>();
        map.put("item_id", "AC4");
        map.put("item_value", mCasData.getAc4());
        mList.add(map);

        // mDivisionCode
        map = new HashMap<String, Object>();
        map.put("item_id", "DivisionCode");
        map.put("item_value", mCasData.getDivisionCode());
        mList.add(map);

        // mGrpBits
        map = new HashMap<String, Object>();
        map.put("item_id", "GrpBits");
        map.put("item_value", mCasData.getGrpBits());
        mList.add(map);

        // mAttrExt
        map = new HashMap<String, Object>();
        map.put("item_id", "AttrExt");
        map.put("item_value", mCasData.getAttrExt());
        mList.add(map);

        return mList;
    }

    private View popupViewInfo;
    private PopupWindow popupWindowInfo;

    private TextView tvWVSystemID, tvWVPlugInStatus, tvProxyURL, tvProxyEntitlement, tvEntitledTime;
    private Button btnSave;


    private void initInfo() {
        popupViewInfo = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ca_info, null);
        popupWindowInfo = new PopupWindow(popupViewInfo, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);

        tvWVSystemID = (TextView) popupViewInfo.findViewById(R.id.tvWVSystemID);
        tvWVPlugInStatus = (TextView) popupViewInfo.findViewById(R.id.tvWVPlugInStatus);
        tvProxyURL = (TextView) popupViewInfo.findViewById(R.id.tvProxyURL);
        tvProxyEntitlement = (TextView) popupViewInfo.findViewById(R.id.tvProxyEntitlement);
        tvEntitledTime = (TextView) popupViewInfo.findViewById(R.id.tvEntitledTime);

        btnSave = (Button) popupViewInfo.findViewById(R.id.btnSave);

        btnSave.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindowInfo.dismiss();
            }
        });
        mCasData = PrimeUtils.get_Widevine_CasData();
    }


    private void getCAInfo() {
        SystemInfo systemInfo = PrimeUtils.get_system_info(getContext());
        GposInfo gposInfo = PrimeUtils.get_gpos_info();
        tvWVSystemID.setText("39805");
        tvWVPlugInStatus.setText("OK");
        tvProxyURL.setText("");
        tvProxyEntitlement.setText("OK");
        tvEntitledTime.setText("");
        if(systemInfo != null){
            tvWVSystemID.setText(systemInfo.WV_id);
        }
        if(gposInfo != null){
            tvProxyURL.setText(GposInfo.getWVCasLicenseURL(getContext()));
            long time = GposInfo.getCasUpdateTime(getContext());
            com.prime.datastructure.utils.LogUtils.d("GPOS_CAS_UPDATE_TIME "+time);
            if(time > 0){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                tvEntitledTime.setText(sdf.format(new Date(time * 1000)));
            }
        }
        if(mCasData != null){
            tvProxyEntitlement.setText("OK");

        }
    }


}
