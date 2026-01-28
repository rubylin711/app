package com.prime.homeplus.membercenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.membercenter.data.TutorialApiResponse;
import com.prime.homeplus.membercenter.enity.RequestData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import androidx.core.view.MarginLayoutParamsCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Tutorial extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.tutorial);

        init();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        handler2.removeCallbacks(autoJumpNextPage);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private static final String TAG = "HomePlus-Tutorial";
    private boolean use_default = true;
    private int PAGE_COUNT = 0;
    public ArrayList<TutorialView> tutorialViewList;
    private ViewPager vpTutorial;
    public ImageView[] imgIndicators;
    private Button btnTutorialSkip;
    private ImageView ivDirection;
    private TextView tvDirection;
    private static final int MESSAGE_API_CODE = 100;
    private static final int MESSAGE_API_ERROR = 101;
    private static final String ACTION_CONFIRM_POPUP_BUTTON_NOSHOW = "android.intent.action.NOSHOW";
    private static final String ACTION_CONFIRM_POPUP_BUTTON_MORE = "android.intent.action.MORE";
    private static final String ACTION_CONFIRM_POPUP_BUTTON_CLOSE = "android.intent.action.CLOSE";

    private Button btnConirmPopupFirst = null;

    private int DEFAULT_NEXT_PAGE_INTERVAL_MS = 10 * 1000;
    private int next_page_interval_ms = DEFAULT_NEXT_PAGE_INTERVAL_MS;
    private Handler handler2 = new Handler();
    private Runnable autoJumpNextPage = new Runnable() {
        public void run() {
            if ((rlTutorialConfirmPopup != null) && (rlTutorialConfirmPopup.getVisibility() == View.VISIBLE)) {
                Log.d(TAG, "rlTutorialConfirmPopup is visible, do not jump next page");
                return;
            }

            if (vpTutorial != null) {
                if ((vpTutorial.getCurrentItem() + 1) < vpTutorial.getAdapter().getCount()) {
                    vpTutorial.setCurrentItem(vpTutorial.getCurrentItem() + 1);
                }
            }
        }
    };

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void updateData(TutorialApiResponse tutorialApiResponse) {
        Log.d(TAG, "updateData()");

        use_default = true;
        PAGE_COUNT = 0;
        TutorialApiResponse.Button[] buttonList = null;
        if (tutorialApiResponse != null) {
            TutorialApiResponse.Data[] dataArray = tutorialApiResponse.getData();
            if ((dataArray != null) && (dataArray.length >= 1)) {
                TutorialApiResponse.Data data = dataArray[0];
                buttonList = data.getButtonList();
                if (buttonList != null) {
                    for (int i=0; i<buttonList.length; i++) {
                        TutorialApiResponse.Button button = buttonList[i];
                        if (button != null) {
                            Log.d(TAG, "buttonList[" + i + "]: " + button.toString());
                        }
                    }
                }

                if (data.getIntervalTime() > 0) {
                    next_page_interval_ms = data.getIntervalTime() * 1000;
                }

                TutorialApiResponse.Pic[] picList = data.getPicList();
                if (picList != null) {
                    Log.d(TAG, "picList len: " + picList.length);
                    int newSize = 0;
                    for (int i = 0; i < picList.length; i++) {
                        if (picList[i] != null) {
                            picList[newSize++] = picList[i];
                        }
                    }
                    picList = Arrays.copyOf(picList, newSize);
                    Log.d(TAG, "picList new len: " + picList.length);
                }

                if (picList != null) {
                    Comparator<TutorialApiResponse.Pic> ageComparator = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        ageComparator = Comparator.comparingInt(TutorialApiResponse.Pic::getOrderNum);
                    }
                    Arrays.sort(picList, ageComparator);
                }

                if (picList != null) {
                    tutorialViewList = new ArrayList();
                    for (int i=0; i<picList.length; i++) {
                        TutorialApiResponse.Pic pic = picList[i];
                        if (pic != null) {
                            Log.d(TAG, "picList[" + i + "]: " + pic.toString());

                            if (pic.isGrounding == 1) {
                                String groundingTimeStr = pic.groundingTime;
                                String undercarriageTimeStr = pic.undercarriageTime;
                                Log.d(TAG, "groundingTimeStr:" + groundingTimeStr + ", undercarriageTimeStr:" + undercarriageTimeStr);
                                if (groundingTimeStr.trim().equals("") && undercarriageTimeStr.trim().equals("")) {
                                    Log.e(TAG, "groundingTimeStr and undercarriageTimeStr are both empty, show pic");
                                    PAGE_COUNT++;
                                    tutorialViewList.add(new TutorialView(this, pic.name, pic.remark, pic.picUrl));
                                } else {
                                    try {
                                        long groundingTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).parse(groundingTimeStr.replace("T", " "), new ParsePosition(0)).getTime();
                                        long undercarriageTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).parse(undercarriageTimeStr.replace("T", " "), new ParsePosition(0)).getTime();
                                        long currentTime = System.currentTimeMillis();
                                        Log.d(TAG, "currentTime:" + currentTime + ", [ groundingTime:" + groundingTime + ", undercarriageTime:" + undercarriageTime + " ]");
                                        if  ((groundingTime <= currentTime) && (currentTime < undercarriageTime)) {
                                            PAGE_COUNT++;
                                            tutorialViewList.add(new TutorialView(this, pic.name, pic.remark, pic.picUrl));
                                        } else {
                                            Log.e(TAG, "Skip pic name:" + pic.name + ", the grounding time has not yet been reached, or the undercarriage time has passed.");
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Skip due to data error, Error：" + e.toString());
                                    }
                                }
                            } else {
                                PAGE_COUNT++;
                                tutorialViewList.add(new TutorialView(this, pic.name, pic.remark, pic.picUrl));
                            }
                        }
                    }
                    use_default = false;
                }
            }
        }

        Log.d(TAG, "use_default:" + use_default + ", PAGE_COUNT:" + PAGE_COUNT);
        if ((use_default == true) || (PAGE_COUNT == 0)) {
            PAGE_COUNT = 5;
            tutorialViewList = new ArrayList();
            tutorialViewList.add(new TutorialView(this, getString(R.string.tutorial_title1), getString(R.string.tutorial_description1), getDrawable(R.drawable.tutorial_img_01)));
            tutorialViewList.add(new TutorialView(this, getString(R.string.tutorial_title2), getString(R.string.tutorial_description2), getDrawable(R.drawable.tutorial_img_02)));
            tutorialViewList.add(new TutorialView(this, getString(R.string.tutorial_title3), getString(R.string.tutorial_description3), getDrawable(R.drawable.tutorial_img_03)));
            tutorialViewList.add(new TutorialView(this, getString(R.string.tutorial_title4), getString(R.string.tutorial_description4), getDrawable(R.drawable.tutorial_img_04)));
            tutorialViewList.add(new TutorialView(this, getString(R.string.tutorial_title5), getString(R.string.tutorial_description5), getDrawable(R.drawable.tutorial_img_05)));

            next_page_interval_ms = DEFAULT_NEXT_PAGE_INTERVAL_MS;
        }

        LinearLayout llIndicator = findViewById(R.id.llIndicator);
        imgIndicators = new ImageView[PAGE_COUNT];
        for (int i=0; i< PAGE_COUNT; i++) {
            imgIndicators[i] = new ImageView(this);
            imgIndicators[i].setImageResource(R.drawable.circle_normal);
            LinearLayout.LayoutParams params_imageview = new LinearLayout.LayoutParams(
                    dpToPx(6),
                    dpToPx(6)
            );
            if (i != 0) {
                MarginLayoutParamsCompat.setMarginStart(params_imageview, dpToPx(6));
            }
            imgIndicators[i].setLayoutParams(params_imageview);
            llIndicator.addView(imgIndicators[i]);
        }

        if (PAGE_COUNT == 1) {
            btnTutorialSkip.setText(getString(R.string.tutorial_confirm));
            ivDirection.setVisibility(View.INVISIBLE);
            tvDirection.setVisibility(View.INVISIBLE);
        } else {
            btnTutorialSkip.setText(getString(R.string.tutorial_skip));
            ivDirection.setVisibility(View.VISIBLE);
            tvDirection.setVisibility(View.VISIBLE);
        }

        vpTutorial.setAdapter(new TutorialPagerAdapter(tutorialViewList));
        handler2.removeCallbacks(autoJumpNextPage);
        handler2.postDelayed(autoJumpNextPage, next_page_interval_ms);

        vpTutorial.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                rlTutorialConfirmPopup.setVisibility(View.VISIBLE);
                if (btnConirmPopupFirst != null) {
                    btnConirmPopupFirst.requestFocus();
                }
            }
        });

        vpTutorial.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int i) {
            }

            public void onPageScrolled(int i, float f, int i2) {
            }

            public void onPageSelected(int i) {
                imgIndicators[i].setBackgroundResource(R.drawable.circle_focused);
                for (int i2 = 0; i2 < PAGE_COUNT; i2++) {
                    if (i != i2) {
                        imgIndicators[i2].setBackgroundResource(R.drawable.circle_normal);
                    }
                }
                tutorialViewList.get(i).updateImage();

                if (i == 0) {
                    btnTutorialSkip.setText(getString(R.string.tutorial_skip));
                    ivDirection.setImageResource(R.drawable.colorkey_direction_right);
                    tvDirection.setText(getString(R.string.tutorial_next));
                    handler2.removeCallbacks(autoJumpNextPage);
                    handler2.postDelayed(autoJumpNextPage, next_page_interval_ms);
                } else if (i == (PAGE_COUNT - 1)) {
                    btnTutorialSkip.setText(getString(R.string.tutorial_confirm));
                    ivDirection.setImageResource(R.drawable.colorkey_direction_left);
                    tvDirection.setText(getString(R.string.tutorial_previous));
                    handler2.removeCallbacks(autoJumpNextPage);
                } else {
                    btnTutorialSkip.setText(getString(R.string.tutorial_skip));
                    ivDirection.setImageResource(R.drawable.colorkey_direction_leftright);
                    tvDirection.setText(getString(R.string.tutorial_previous_next));
                    handler2.removeCallbacks(autoJumpNextPage);
                    handler2.postDelayed(autoJumpNextPage, next_page_interval_ms);
                }
            }
        });

        imgIndicators[0].setBackgroundResource(R.drawable.circle_focused);
        tutorialViewList.get(0).updateImage();

        initPopup(buttonList);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_API_CODE:
                    Bundle bundle = msg.getData();
                    String jsonString = bundle.getString("message");

                    Gson gson = new Gson();
                    TutorialApiResponse response = gson.fromJson(jsonString, TutorialApiResponse.class);
                    updateData(response);
                    break;
                case MESSAGE_API_ERROR:
                    updateData(null);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void sendMesage(int messageType, final String message) {
        Message msg = handler.obtainMessage(messageType);
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void init() {
        vpTutorial = (ViewPager) findViewById(R.id.vpTutorial);
        btnTutorialSkip = (Button) findViewById(R.id.btnTutorialSkip);
        ivDirection = (ImageView) findViewById(R.id.ivDirection);
        tvDirection = (TextView) findViewById(R.id.tvDirection);

        getTutorialPages();
    }

    private RelativeLayout rlTutorialConfirmPopup;

    private void initPopup(TutorialApiResponse.Button[] buttonList) {
        Log.d(TAG, "initPopup()");
        rlTutorialConfirmPopup = (RelativeLayout) findViewById(R.id.rlTutorialConfirmPopup);

        if (buttonList != null) {
            Log.d(TAG, "buttonList len: " + buttonList.length);
            int newSize = 0;
            for (int i = 0; i < buttonList.length; i++) {
                if (buttonList[i] != null) {
                    buttonList[newSize++] = buttonList[i];
                }
            }
            buttonList = Arrays.copyOf(buttonList, newSize);
            Log.d(TAG, "buttonList new len: " + buttonList.length);
        }

        LinearLayout llConfirmPopup = findViewById(R.id.llConfirmPopup);
        if ((use_default == false) && (buttonList != null) && (buttonList.length > 0)) {
            Comparator<TutorialApiResponse.Button> ageComparator = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                ageComparator = Comparator.comparingInt(TutorialApiResponse.Button::getOrderNum);
            }
            Arrays.sort(buttonList, ageComparator);

            for (int i=0; i<buttonList.length; i++) {
                final TutorialApiResponse.Button apiButton = buttonList[i];
                if (apiButton == null) {
                    Log.d(TAG, "buttonList[" + i + "] is null !!");
                    continue;
                }
                Log.d(TAG, "buttonList[" + i + "]: " + apiButton.toString());
                Button button = createConfirmPopupButton(apiButton.buttonName, apiButton.getAction());

                if (btnConirmPopupFirst == null) {
                    btnConirmPopupFirst = button;
                }
                llConfirmPopup.addView(button);
            }
        } else {
            Button button1 = createConfirmPopupButton(getString(R.string.tutorial_dont_show_again), ACTION_CONFIRM_POPUP_BUTTON_NOSHOW);
            llConfirmPopup.addView(button1);
            if (btnConirmPopupFirst == null) {
                btnConirmPopupFirst = button1;
            }

            Button button2 = createConfirmPopupButton(getString(R.string.tutorial_learn_more), ACTION_CONFIRM_POPUP_BUTTON_MORE);
            llConfirmPopup.addView(button2);

            Button button3 = createConfirmPopupButton(getString(R.string.tutorial_cancel), ACTION_CONFIRM_POPUP_BUTTON_CLOSE);
            llConfirmPopup.addView(button3);
        }
    }

    private Button createConfirmPopupButton(String btnName, final String btnAction) {
        Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(36));
        layoutParams.leftMargin = dpToPx(7);
        layoutParams.weight = 1;
        button.setLayoutParams(layoutParams);
        button.setPadding(dpToPx(15), 0, dpToPx(15), 0);
        button.setText(btnName);
        button.setTextSize(16);
        button.setMinimumWidth(dpToPx(98));
        button.setTextColor(getResources().getColorStateList(R.color.state_selected_white_normal_b3ffffff));
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.button_item2);
        button.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                Button btn = (Button) view;
                if (btnAction.equals(ACTION_CONFIRM_POPUP_BUTTON_NOSHOW)) {
                    Log.d(TAG, "NOSHOW onClick");
                    // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(new ComponentName("com.prime.homeplus.settings", "com.prime.homeplus.settings.SettingsActivity"));
//                    intent.addFlags(268435456);
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putString("Action", "TutorialSetting");
                        intent.putExtras(bundle);
                        startActivity(intent);
                        return;
                    } catch (ActivityNotFoundException e) {
                        Log.d(Tutorial.TAG, "error: " + e.toString());
                        return;
                    }
//                    finish();
                } else if (btnAction.equals(ACTION_CONFIRM_POPUP_BUTTON_MORE)) {
                    Log.d(TAG, "MORE onClick");
                    // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                    Log.d(Tutorial.TAG, "MORE onClick");
                    Intent intent2 = new Intent();
                    intent2.setClass(Tutorial.this.getApplication(), MemberActivity.class);
                    intent2.putExtra("Action", "QuestionList");
                    startActivity(intent2);
                    return;
//                    finish();
                } else if (btnAction.equals(ACTION_CONFIRM_POPUP_BUTTON_CLOSE)) {
                    Log.d(TAG, "CLOSE onClick");
                    Log.d(Tutorial.TAG, "CLOSE onClick");
                    setResultIntent();
                    finish();
                } else {
                    Log.d(TAG, "Not support action - " + btnAction + " !!");
                }
            }
        });

        button.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Button btn = (Button) v;
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        finish();
                        return true;
                    }
                }
                return false;
            }
        });

        button.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Button btn = (Button) v;
                if (hasFocus) {
                    btn.setTypeface(null, Typeface.BOLD);
                    btn.setTextSize(17);
                } else {
                    btn.setTypeface(null, Typeface.NORMAL);
                    btn.setTextSize(16);
                }
            }
        });

        return button;
    }

    class TutorialPagerAdapter extends PagerAdapter {
        private ArrayList<TutorialView> tutorialViewList;

        public boolean isViewFromObject(View view, Object obj) {
            return obj == view;
        }

        TutorialPagerAdapter(ArrayList<TutorialView> arrayList) {
            this.tutorialViewList = arrayList;
        }

        public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
            TutorialView tutorialView = (TutorialView) tutorialViewList.get(i);
            if (tutorialView != null) {
                viewGroup.removeView(tutorialView);
            }
        }

        public Object instantiateItem(ViewGroup viewGroup, int i) {
            TutorialView tutorialView = (TutorialView) tutorialViewList.get(i);
            viewGroup.addView(tutorialView);
            return tutorialView;
        }

        public int getCount() {
            return tutorialViewList.size();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////
    private static final String DEFAULT_TUTORIAL_DOMAIN = "https://cnsatv.totaltv.com.tw:8091/";
    private static final String DEFAULT_TUTORIAL_ROLE_ID = "1";
    private static final String api_name = "api/v1/tutorial";
    private void getTutorialPages() {
        String tutorialDomain = GposInfo.getLauncherDomain(this);//Settings.System.getString(getContentResolver(), "cns_launcher_domain");
        String token = GposInfo.getLauncherToken(this);//Settings.System.getString(getContentResolver(), "cns_launcher_token");
        String roleId = GposInfo.getCurrentRoleType(this);//Settings.System.getString(getContentResolver(), "currentRoleType");
        Log.d(TAG, "tutorialDomain: " + tutorialDomain);
        Log.d(TAG, "token: " + token);
        Log.d(TAG, "roleId: " + roleId);

        if (TextUtils.isEmpty(token)) {
            String errorCode = "Error: token is null or empty !!";
            Log.d(TAG, errorCode);
            sendMesage(MESSAGE_API_ERROR, errorCode);
            return;
        }

        if (TextUtils.isEmpty(roleId)) {
            roleId = DEFAULT_TUTORIAL_ROLE_ID;
        }

        if (TextUtils.isEmpty(tutorialDomain) || tutorialDomain.equals("")) {
            tutorialDomain = DEFAULT_TUTORIAL_DOMAIN;
        }
        String requestUrl = tutorialDomain + api_name;
        String tutorialUrl = GposInfo.getTutorialUrl(this);//Settings.System.getString(getContentResolver(), "cns_tutorial_url");
        if (!TextUtils.isEmpty(tutorialUrl)) {
            requestUrl = tutorialUrl;
        }
        Log.d(TAG, "requestUrl: " + requestUrl);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        String command = "0000";
        String timestamp = System.currentTimeMillis() + "";
        JsonObject request_data = new JsonObject();
        request_data.addProperty("token", token);
        request_data.addProperty("roleId", roleId);
        RequestData requestData = new RequestData(command, timestamp, request_data);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), requestData.getRequestBody());
        Log.d(TAG, requestData.getRequestBody());
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(body)
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                String errorCode = "";
                if (!TextUtils.isEmpty(result)) {
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(result);
                        int code = obj.getInt("code");
                        if (code == 999997) {
                            errorCode = "Error: : Token 失效";
                        } else if (code == 999998) {
                            errorCode = "Error: 消息不合法/參數錯誤";
                        } else if (code == 0) {
                            Log.d(TAG, "success.");
                            sendMesage(MESSAGE_API_CODE, result);
                            return;
                        }
                    } catch (JSONException e) {
                        errorCode = "Error: " + e.toString();
                    }
                } else {
                    errorCode = "Error: result is null";
                }
                Log.d(TAG, errorCode);
                sendMesage(MESSAGE_API_ERROR, errorCode);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Connect Fail Exception:" + e.toString());
                sendMesage(MESSAGE_API_ERROR, e.toString());
            }
        });
    }

    public void setResultIntent() {
        Intent intent = new Intent();
        intent.putExtra("Action", "Tutorial");
        setResult(-1, intent);
    }

}