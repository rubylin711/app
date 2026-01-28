package com.prime.homeplus.membercenter;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.homeplus.membercenter.TvMail.TvMailDbHelper;
import com.prime.homeplus.membercenter.TvMail.TvMailManager;
import com.prime.homeplus.membercenter.data.AllMailHead;
import com.prime.homeplus.membercenter.data.CrmPayBillOBJ;
import com.prime.homeplus.membercenter.data.CrmPayBillResponseJson;
import com.prime.homeplus.membercenter.data.GetVodPointOBJ;
import com.prime.homeplus.membercenter.data.GetVodPointResponseJson;
import com.prime.homeplus.membercenter.data.QueryCrmMyAccountOBJ;
import com.prime.homeplus.membercenter.data.QueryCrmMyAccountResponseJson;
import com.prime.homeplus.membercenter.data.QueryCrmMyAccountRetData2;
import com.prime.homeplus.membercenter.data.QueryCustInfoResponseJson;
import com.prime.homeplus.membercenter.data.QueryPrepayAmountOBJ;
import com.prime.homeplus.membercenter.data.QueryPrepayAmountResponseJson;
import com.prime.homeplus.membercenter.data.Question;
import com.prime.homeplus.membercenter.data.QueryCustInfoOBJ;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.IntentCompat;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MemberActivity extends Activity implements HttpDownloadTask.HttpDownloadResponse {
    public static String TAG = "HomePlus-MemberActivity";

    private float density;
    private HttpDownloadTask httpDownloadTask;
    private String DEFAULT_MC_DATA_URL_PREFIX = "https://cnsatv.totaltv.com.tw:8093";
    private TvMailDbHelper tvMailDbHelper;
    private CmModeContentObserver cmModeValueObserver;

    private static final int REQUEST_CODE = 999;

    private final int MEMBER_MEMBER = 0;
    private final int MEMBER_SUBSCRIPTION = 1;
    private final int MEMBER_VOD_POINT = 2;
    private final int MEMBER_CLAUSE = 3;
    private final int MEMBER_MAIL = 4;
    private final int MEMBER_QUESTION = 5;
    private final int MEMBER_FRIDAY_CROSS_SCREEN = 6;
    private final int MEMBER_FRIDAY_DOWNLOAD = 7;
    private final int MEMBER_CHANNEL_LIST = 8;

    // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
    private boolean mCableMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member);
        density = getResources().getDisplayMetrics().density;
        initLayout();
        initMember();
        initChannelList();
        initSubscription();
        initVODPoint();
        initClause();
        initMail();
        initQuestion();
        initCrossScreen();
        if (tvMailDbHelper == null) {
            tvMailDbHelper = new TvMailDbHelper(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d(TAG, "onNewIntent");
        handleIntentAction();
    }

    private ConstraintLayout clMain;

    private ListView lvMembers;
    private TextView tvDateTime;
    private ConstraintLayout clList, clListTitle;

    private ConstraintLayout memberMember, memberChannelList, memberSubscription, memberVODPoint, memberTerms,
            memberMail, memberQuestion, memberCrossScreen;

    private int listIndex = 0;
    private List<Map<String, Object>> memberCenterListMap = null;

    private void initLayout() {
        clMain = (ConstraintLayout) findViewById(R.id.clMain);

        memberMember = (ConstraintLayout) findViewById(R.id.memberMember);
        memberChannelList = (ConstraintLayout) findViewById(R.id.memberChannelList);
        memberSubscription = (ConstraintLayout) findViewById(R.id.memberSubscription);
        memberVODPoint = (ConstraintLayout) findViewById(R.id.memberVODPoint);
        memberTerms = (ConstraintLayout) findViewById(R.id.memberTerms);
        memberMail = (ConstraintLayout) findViewById(R.id.memberMail);
        memberQuestion = (ConstraintLayout) findViewById(R.id.memberQuestion);
        memberCrossScreen = (ConstraintLayout) findViewById(R.id.memberCrossScreen);

        tvDateTime = (TextView) findViewById(R.id.tvDateTime);

        clList = (ConstraintLayout) findViewById(R.id.clList);
        clListTitle = (ConstraintLayout) findViewById(R.id.clListTitle);

        lvMembers = (ListView) findViewById(R.id.lvMembers);

        lvMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 7:
                        Log.d(TAG, "friDay Cross Screen OnClick.");
                        openFridayGooglePlay();
                        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                        break;
                }

            }
        });

        lvMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listIndex = position;
                if ((memberCenterListMap != null) && (memberCenterListMap.size() > listIndex)) {
                    listIndex = (Integer) memberCenterListMap.get(position).get("item_id");
                    showMembersRight(listIndex);
                }
                if (listIndex == MEMBER_FRIDAY_DOWNLOAD) {
                    svCrossScreen.setFocusable(false);
                } else {
                    svCrossScreen.setFocusable(true);
                }

                for (int i = 0; i < lvMembers.getChildCount(); i++) {
                    View viewItem = lvMembers.getChildAt(i);
                    if (viewItem != null) {
                        TextView tv_item_name = viewItem.findViewById(R.id.item_name);
                        if (tv_item_name != null) {
                            int adapterPosition = lvMembers.getFirstVisiblePosition() + i;
                            if (position == adapterPosition) {
                                tv_item_name.setTypeface(Typeface.DEFAULT_BOLD);
                            } else {
                                tv_item_name.setTypeface(Typeface.DEFAULT);
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvAnimatorSet = new AnimatorSet();

        lvMembers.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                listViewFocus(hasFocus);
            }

        });
    }

    private void showMembersRight(int index) {
        switch (index) {
            case MEMBER_MEMBER:
                invisibleAllView();
                memberMember.setVisibility(View.VISIBLE);
                updateMember();
                break;
            case MEMBER_SUBSCRIPTION:
                invisibleAllView();
                memberSubscription.setVisibility(View.VISIBLE);
                break;
            case MEMBER_VOD_POINT:
                invisibleAllView();
                memberVODPoint.setVisibility(View.VISIBLE);
                updateVODPoint();
                break;
            case MEMBER_CLAUSE:
                invisibleAllView();
                memberTerms.setVisibility(View.VISIBLE);
                break;
            case MEMBER_MAIL:
                invisibleAllView();
                memberMail.setVisibility(View.VISIBLE);
                mailPage = 0;
                updateMail();
                break;
            case MEMBER_QUESTION:
                invisibleAllView();
                memberQuestion.setVisibility(View.VISIBLE);
                break;
            case MEMBER_FRIDAY_CROSS_SCREEN:
                invisibleAllView();
                svCrossScreen.scrollTo(0, 0);
                memberCrossScreen.setVisibility(View.VISIBLE);
                break;
            case MEMBER_FRIDAY_DOWNLOAD:
                break;
            case MEMBER_CHANNEL_LIST:
                invisibleAllView();
                memberChannelList.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void invisibleAllView() {
        memberMember.setVisibility(View.INVISIBLE);
        memberChannelList.setVisibility(View.INVISIBLE);
        memberSubscription.setVisibility(View.INVISIBLE);
        memberVODPoint.setVisibility(View.INVISIBLE);
        memberTerms.setVisibility(View.INVISIBLE);
        memberMail.setVisibility(View.INVISIBLE);
        memberQuestion.setVisibility(View.INVISIBLE);
        memberCrossScreen.setVisibility(View.INVISIBLE);
    }

    private void listViewFocus(boolean isFocus) {
        if (isFocus) {
            clListTitle.setVisibility(View.VISIBLE);

            animateTvInput((int) (250 * density), 300);

            goToList(listIndex);
        } else {
            clListTitle.setVisibility(View.INVISIBLE);

            animateTvInput((int) (55 * density), 300);

        }

        setListSpace(isFocus);

        for (int i = 0; i < lvMembers.getCount(); i++) {
            View view = lvMembers.getChildAt(i);

            if (null != view) {
                TextView tvName = (TextView) view.findViewById(R.id.item_name);

                if (isFocus) {
                    tvName.setVisibility(View.VISIBLE);
                } else {
                    tvName.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void goToList(int index) {
        int list_view_index = 0;

        if (memberCenterListMap != null) {
            for (int i = 0; i < memberCenterListMap.size(); i++) {
                if (index == (Integer) memberCenterListMap.get(i).get("item_id")) {
                    list_view_index = i;
                    break;
                }
            }
        }

        lvMembers.setFocusable(true);
        lvMembers.requestFocus();
        lvMembers.setSelection(list_view_index);
    }

    private AnimatorSet tvAnimatorSet;

    private void animateTvInput(int toWidth, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(clList.getMeasuredWidth(), toWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = clList.getLayoutParams();
                layoutParams.width = val;
                clList.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(duration);
        anim.start();
    }

    private void setListSpace(boolean isFocus) {
        ValueAnimator anim;

        if (isFocus) {
            anim = ValueAnimator.ofInt((int) (205 * density), (int) (250 * density));
        } else {
            anim = ValueAnimator.ofInt((int) (250 * density), (int) (205 * density));
        }

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = lvMembers.getLayoutParams();
                layoutParams.width = val;
                lvMembers.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(300);
        anim.start();

    }

    private Handler handler = new Handler();
    private Runnable updateTimer = new Runnable() {
        public void run() {
            String time = new SimpleDateFormat("MM.dd (E) HH:mm").format(new Date());
            tvDateTime.setText(time);

            handler.postDelayed(this, 10000);
        }
    };

    private SimpleAdapter getMemberListAdapter() {
        memberCenterListMap = getMemberLisData();
        return new SimpleAdapter(this, memberCenterListMap, R.layout.settings_list_item,
                new String[] { "item_icon", "item_name", "item_id" }, new int[] { R.id.item_icon, R.id.item_name });
    }

    private List<Map<String, Object>> getMemberLisData() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        String titleList[];
        TypedArray iconList;
        int idList[];

        if (mCableMode) {
            titleList = getResources().getStringArray(R.array.membercenter_list_cable_mode_title);
            iconList = getResources().obtainTypedArray(R.array.membercenter_list_cable_mode_drawable);
            idList = getResources().getIntArray(R.array.membercenter_list_cable_mode_id);
        } else {
            titleList = getResources().getStringArray(R.array.membercenter_list_title);
            iconList = getResources().obtainTypedArray(R.array.membercenter_list_drawable);
            idList = getResources().getIntArray(R.array.membercenter_list_id);
        }

        if (titleList.length != iconList.length()) {
            return null;
        }

        for (int i = 0; i < titleList.length; i++) {
            map = new HashMap<String, Object>();
            map.put("item_icon", iconList.getResourceId(i, 0));
            map.put("item_name", titleList[i]);
            map.put("item_id", idList[i]);
            mList.add(map);
        }

        return mList;
    }

    public class Adapter extends SimpleAdapter {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        View view;
        ImageView item_icon;
        LinearLayout item_date_ll, item_type_ll, item_id_ll;
        TextView item_read;

        public Adapter(Context context, List<? extends Map<String, Object>> data, int resource, String[] from,
                int[] to) {

            super(context, data, resource, from, to);
            mList = (List<Map<String, Object>>) data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            view = super.getView(position, convertView, parent);
            item_icon = (ImageView) view.findViewById(R.id.item_icon);
            item_date_ll = (LinearLayout) view.findViewById(R.id.item_date_ll);
            item_type_ll = (LinearLayout) view.findViewById(R.id.item_type_ll);
            item_id_ll = (LinearLayout) view.findViewById(R.id.item_id_ll);
            item_read = (TextView) view.findViewById(R.id.item_read);

            if (isFocus) {
                item_type_ll.setVisibility(View.VISIBLE);
                item_id_ll.setVisibility(View.VISIBLE);

            } else {
                item_type_ll.setVisibility(View.GONE);
                item_id_ll.setVisibility(View.GONE);
            }

            if (mList.get(position).get("item_read").equals("1")) {
                item_read.setVisibility(View.VISIBLE);
            } else {
                item_read.setVisibility(View.GONE);
            }

            Log.d(TAG, "!!!! " + mList.get(position).get("item_read"));

            return view;
        }

    }

    private LinearLayout llCustId, llSubsId;
    private TextView tvCustId, tvSubsId, tvSTBNumber, tvSmartCardNumber;

    private void initMember() {
        llCustId = (LinearLayout) findViewById(R.id.llCustId);
        llSubsId = (LinearLayout) findViewById(R.id.llSubsId);

        tvCustId = (TextView) findViewById(R.id.tvCustId);
        tvSubsId = (TextView) findViewById(R.id.tvSubsId);
        tvSTBNumber = (TextView) findViewById(R.id.tvSTBNumber);
        tvSmartCardNumber = (TextView) findViewById(R.id.tvSmartCardNumber);
    }

    private void updateMember() {
        if (!mCableMode) {
            llCustId.setVisibility(View.VISIBLE);
            llSubsId.setVisibility(View.VISIBLE);

            QueryCustInfoOBJ mQueryCustInfoOBJ;

            String mSO = Utils.getSoId(true);
            mQueryCustInfoOBJ = new QueryCustInfoOBJ(mSO, Utils.getSN(), Utils.getCardSN());

            PostDataMemberCenter postDataSMS = new PostDataMemberCenter(MemberActivity.this, mHandler2);
            postDataSMS.sendQueryCustInfo(mQueryCustInfoOBJ);
        } else {
            llCustId.setVisibility(View.GONE);
            llSubsId.setVisibility(View.GONE);
        }

        tvSTBNumber.setText(Utils.getSN());
        tvSmartCardNumber.setText(Utils.getCardSN());
    }

    private ScrollView svOnlineRemark;
    private LinearLayout llDefaultRemark;
    private TextView tvGiftPoint, tvUsePoint, tvSurplusPoint, tvQueryTime, tvDueDate, tvVodDiscount, tvOnlineRemark;

    private void initVODPoint() {
        svOnlineRemark = (ScrollView) findViewById(R.id.svOnlineRemark);

        llDefaultRemark = (LinearLayout) findViewById(R.id.llDefaultRemark);

        tvGiftPoint = (TextView) findViewById(R.id.tvGiftPoint);
        tvUsePoint = (TextView) findViewById(R.id.tvUsePoint);
        tvSurplusPoint = (TextView) findViewById(R.id.tvSurplusPoint);
        tvQueryTime = (TextView) findViewById(R.id.tvQueryTime);
        tvDueDate = (TextView) findViewById(R.id.tvDueDate);
        tvVodDiscount = (TextView) findViewById(R.id.tvVodDiscount);
        tvOnlineRemark = (TextView) findViewById(R.id.tvOnlineRemark);

        svOnlineRemark.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    svOnlineRemark.scrollTo(0, 0);
                }
            }
        });

        svOnlineRemark.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (svOnlineRemark.getScrollY() == 0) {
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        int scrollY = svOnlineRemark.getScrollY();
                        int scrollViewHeight = svOnlineRemark.getChildAt(0).getHeight();
                        int scrollViewVisibleHeight = svOnlineRemark.getHeight();
                        if (scrollY + scrollViewVisibleHeight >= scrollViewHeight) {
                            Log.d(TAG, "reached the bottom !");
                            return true;
                        }

                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_VOD_POINT);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void updateVODPoint() {
        GetVodPointOBJ mGetVodPointOBJ;

        String mSO = Utils.getSoId(true);
        mGetVodPointOBJ = new GetVodPointOBJ(mSO, Utils.getSN());

        PostDataMemberCenter postDataSMS = new PostDataMemberCenter(MemberActivity.this, mHandler2);
        postDataSMS.sendGetVodPoint(mGetVodPointOBJ);
    }

    private View popupViewMailDelete;
    private PopupWindow popupWindowMailDelete;
    private TextView tvMailDelete, tvMailDeleteAll;

    private void initMailDeletePopupWindow() {
        popupViewMailDelete = LayoutInflater.from(this).inflate(R.layout.dialog_mail_delete, null);
        popupWindowMailDelete = new PopupWindow(popupViewMailDelete, ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, true);

        tvMailDelete = (TextView) popupViewMailDelete.findViewById(R.id.tvMailDelete);
        tvMailDeleteAll = (TextView) popupViewMailDelete.findViewById(R.id.tvMailDeleteAll);

        Button btnOK = (Button) popupViewMailDelete.findViewById(R.id.btnOK);
        Button btnCancel = (Button) popupViewMailDelete.findViewById(R.id.btnCancel);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindowMailDelete.dismiss();
                if (tvMailDeleteAll.getVisibility() == View.VISIBLE) {
                    deleteMailAll();
                } else {
                    AllMailHead allMailHead = allMailHeads.get(lvMail.getSelectedItemPosition());
                    if (allMailHead.getMailType().equals("CAMAIL")) {
                        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                    } else if (allMailHead.getMailType().equals("TVMAIL")) {
                        tvMailDbHelper.deleteMail(allMailHead.getMail_id());
                    }
                }
                updateMail();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindowMailDelete.dismiss();
            }
        });
    }

    private void showDeleteMail(String status) {
        if (status.equals("All")) {
            tvMailDelete.setVisibility(View.GONE);
            tvMailDeleteAll.setVisibility(View.VISIBLE);
        } else {
            tvMailDelete.setVisibility(View.VISIBLE);
            tvMailDeleteAll.setVisibility(View.GONE);
        }
        popupWindowMailDelete.showAtLocation(clMain, Gravity.RIGHT, 0, 0);
    }

    private void deleteMailAll() {
        for (AllMailHead allMailHead : allMailHeads) {
            if (allMailHead.getNewEmail() == 0) {
                if (allMailHead.getMailType().equals("CAMAIL")) {
                    // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                } else if (allMailHead.getMailType().equals("TVMAIL")) {
                    tvMailDbHelper.deleteMail(allMailHead.getMail_id());
                }
            }
        }
    }

    private void readMailAll() {
        for (AllMailHead allMailHead : allMailHeads) {
            if (allMailHead.getMailType().equals("CAMAIL")) {
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            } else if (allMailHead.getMailType().equals("TVMAIL")) {
                tvMailDbHelper.updateIsRead(allMailHead.getMail_id());
            }
        }
        updateMail();
    }

    private Button btnChannelListForCableMode;

    private void initChannelList() {
        btnChannelListForCableMode = (Button) findViewById(R.id.btnChannelListForCableMode);

        btnChannelListForCableMode.setOnFocusChangeListener(OnFocusChangeListener);

        btnChannelListForCableMode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_CHANNEL_LIST);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnChannelListForCableMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), ChannelList.class);
                startActivity(intent);
            }
        });
    }

    private Button btnPayment, btnBillingHistory, btnChannelList;

    private ConstraintLayout memberSubscriptionPayment, memberSubscriptionBillingHistory;

    private ListView lvBillingHistory;
    private TextView tvBillingHistoryIndex, tvBillingHistoryTotal;
    List<QueryCrmMyAccountRetData2> billingHistoryList;
    private int billingHistoryPage = 1;
    private int billingHistoryTotalPage = 1;
    int billingHistoryItemSizeOfPage = 8;

    private ListView lvPayment;
    private LinearLayout llPaymentPageIndex, llPaymentColorKey;
    private TextView tv_select_all, tvPaymentIndex, tvPaymentTotal, tvPaymentSelectedPrice,
            tvPaymentAllShouldAmountPrice;
    List<QueryPrepayAmountResponseJson.QueryPrepayAmountRetData2> paymentList;
    private int paymentPage = 1;
    private int paymentTotalPage = 1;
    int paymentItemSizeOfPage = 99;
    boolean isPaymentSelectAll = false;

    private void initSubscription() {
        memberSubscriptionPayment = (ConstraintLayout) findViewById(R.id.memberSubscriptionPayment);
        memberSubscriptionBillingHistory = (ConstraintLayout) findViewById(R.id.memberSubscriptionBillingHistory);

        btnPayment = (Button) findViewById(R.id.btnPayment);
        btnBillingHistory = (Button) findViewById(R.id.btnBillingHistory);
        btnChannelList = (Button) findViewById(R.id.btnChannelList);

        btnPayment.setOnFocusChangeListener(OnFocusChangeListener);
        btnBillingHistory.setOnFocusChangeListener(OnFocusChangeListener);
        btnChannelList.setOnFocusChangeListener(OnFocusChangeListener);

        btnPayment.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
                        if ((keyCode == KeyEvent.KEYCODE_BACK)
                                && (memberSubscriptionPayment.getVisibility() == View.VISIBLE)) {
                            memberSubscriptionPayment.setVisibility(View.INVISIBLE);
                            clearPaymentFirstPage();
                            btnPayment.requestFocus();
                        } else {
                            memberSubscriptionPayment.setVisibility(View.INVISIBLE);
                            clearPaymentFirstPage();
                            goToList(MEMBER_SUBSCRIPTION);
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnBillingHistory.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
                        if ((keyCode == KeyEvent.KEYCODE_BACK)
                                && (memberSubscriptionBillingHistory.getVisibility() == View.VISIBLE)) {
                            memberSubscriptionBillingHistory.setVisibility(View.INVISIBLE);
                            clearBillingHistoryFirstPage();
                            btnBillingHistory.requestFocus();
                        } else if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                                && (memberSubscriptionBillingHistory.getVisibility() != View.VISIBLE)) {
                            btnPayment.requestFocus();
                        } else {
                            memberSubscriptionBillingHistory.setVisibility(View.INVISIBLE);
                            clearBillingHistoryFirstPage();
                            goToList(MEMBER_SUBSCRIPTION);
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnChannelList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_SUBSCRIPTION);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberSubscriptionPayment.setVisibility(View.VISIBLE);

                QueryPrepayAmountOBJ mQueryPrepayAmountOBJ;
                String mSO = Utils.getSoId(true);
                mQueryPrepayAmountOBJ = new QueryPrepayAmountOBJ(mSO, Utils.getSN(), "", "0");

                PostDataMemberCenter postDataSMS = new PostDataMemberCenter(MemberActivity.this, mHandler2);
                postDataSMS.sendQueryPrepayAmount(mQueryPrepayAmountOBJ);
            }
        });

        btnBillingHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberSubscriptionBillingHistory.setVisibility(View.VISIBLE);

                QueryCrmMyAccountOBJ mQueryCrmMyAccountOBJ;
                String mSO = Utils.getSoId(true);
                mQueryCrmMyAccountOBJ = new QueryCrmMyAccountOBJ(mSO, Utils.getSN());

                PostDataMemberCenter postDataSMS = new PostDataMemberCenter(MemberActivity.this, mHandler2);
                postDataSMS.sendQueryCrmMyAccount(mQueryCrmMyAccountOBJ);
            }
        });

        btnChannelList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), ChannelList.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        tv_select_all = (TextView) findViewById(R.id.tv_select_all);
        tvPaymentIndex = (TextView) findViewById(R.id.tvPaymentIndex);
        tvPaymentTotal = (TextView) findViewById(R.id.tvPaymentTotal);
        tvPaymentSelectedPrice = (TextView) findViewById(R.id.tvPaymentSelectedPrice);
        tvPaymentAllShouldAmountPrice = (TextView) findViewById(R.id.tvPaymentAllShouldAmountPrice);
        lvPayment = (ListView) findViewById(R.id.lvPayment);
        llPaymentPageIndex = (LinearLayout) findViewById(R.id.llPaymentPageIndex);
        llPaymentColorKey = (LinearLayout) findViewById(R.id.llPaymentColorKey);

        tvBillingHistoryIndex = (TextView) findViewById(R.id.tvBillingHistoryIndex);
        tvBillingHistoryTotal = (TextView) findViewById(R.id.tvBillingHistoryTotal);
        lvBillingHistory = (ListView) findViewById(R.id.lvBillingHistory);

        initOnlinePayment();
    }

    private View popupViewOnlinePayment;
    private PopupWindow popupWindowOnlinePayment;
    private EditText etCardNumber, etValidDateMonth, etValidDateYear;
    private TextView tvOnlinePaymentTotalPrice;
    private Button btnOnlinePaymentConfirm, btnOnlinePaymentCancel;
    private int paymentSelectedPrice = 0;
    private StringBuilder paymentSelectedMediaBillno = new StringBuilder("");

    private void initOnlinePayment() {

        popupViewOnlinePayment = LayoutInflater.from(this).inflate(R.layout.member_subscription_online_payment, null);
        popupWindowOnlinePayment = new PopupWindow(popupViewOnlinePayment, ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, true);

        btnOnlinePaymentConfirm = (Button) popupViewOnlinePayment.findViewById(R.id.btnOnlinePaymentConfirm);
        btnOnlinePaymentCancel = (Button) popupViewOnlinePayment.findViewById(R.id.btnOnlinePaymentCancel);

        btnOnlinePaymentConfirm.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);
        btnOnlinePaymentCancel.setOnFocusChangeListener(NoAnimationOnFocusChangeListener);

        tvOnlinePaymentTotalPrice = (TextView) popupViewOnlinePayment.findViewById(R.id.tvOnlinePaymentTotalPrice);

        etCardNumber = (EditText) popupViewOnlinePayment.findViewById(R.id.etCardNumber);
        etValidDateMonth = (EditText) popupViewOnlinePayment.findViewById(R.id.etValidDateMonth);
        etValidDateYear = (EditText) popupViewOnlinePayment.findViewById(R.id.etValidDateYear);

        etCardNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etCardNumber.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        etCardNumber.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {

                if (etCardNumber.getText().length() > 0 &&
                        etValidDateMonth.getText().length() > 0 &&
                        etValidDateYear.getText().length() > 0) {
                } else {
                }

                if (etCardNumber.getText().length() == 16) {
                    etValidDateMonth.requestFocus();
                }
            }
        });

        etValidDateMonth.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etValidDateMonth.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        etValidDateMonth.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {

                if (etCardNumber.getText().length() > 0 &&
                        etValidDateMonth.getText().length() > 0 &&
                        etValidDateYear.getText().length() > 0) {
                } else {
                }

                if (etValidDateMonth.getText().length() == 2) {
                    etValidDateYear.requestFocus();
                }
            }
        });

        etValidDateYear.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (!etValidDateYear.getText().toString().equals("")) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Instrumentation inst = new Instrumentation();
                                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
                                    } catch (Exception e) {
                                        Log.d(TAG, "" + e);
                                    }
                                }
                            }.start();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        etValidDateYear.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {

                if (etCardNumber.getText().length() > 0 &&
                        etValidDateMonth.getText().length() > 0 &&
                        etValidDateYear.getText().length() > 0) {
                } else {
                }

                if (etValidDateYear.getText().length() == 2) {
                    btnOnlinePaymentConfirm.requestFocus();
                }
            }
        });

        btnOnlinePaymentConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etCardNumber.getText().length() == 16) {
                    String encCardNumber = "";
                    String cardNumber = etCardNumber.getText().toString();
                    String validDateMonth = "";
                    String validDateYear = "";
                    try {
                        validDateMonth = String.format("%02d", Integer.parseInt(etValidDateMonth.getText().toString()));
                        validDateYear = String.format("%02d", Integer.parseInt(etValidDateYear.getText().toString()));
                    } catch (Exception e) {
                    }

                    String validDate = validDateMonth + "/" + validDateYear;

                    CrmPayBillOBJ mCrmPayBillOBJ;
                    String mSO = Utils.getSoId(true);
                    try {
                        encCardNumber = AesUtils.aesEncrypt(Utils.getSN() + validDateMonth + validDateYear + mSO,
                                cardNumber);
                    } catch (Exception e) {
                    }

                    if (tvOnlinePaymentTotalPrice.getText().toString().equals("" + paymentSelectedPrice)) {
                        mCrmPayBillOBJ = new CrmPayBillOBJ(mSO, encCardNumber, validDate, "" + paymentSelectedPrice,
                                paymentSelectedMediaBillno.toString(), Utils.getSN());
                        PostDataMemberCenter postDataSMS = new PostDataMemberCenter(MemberActivity.this, mHandler2);
                        postDataSMS.sendCrmPayBill(mCrmPayBillOBJ);
                    } else {
                        Toast.makeText(MemberActivity.this, "價格取得失敗，請重新點選", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MemberActivity.this, "Creditnumber 格式錯誤", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnOnlinePaymentCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindowOnlinePayment.dismiss();
            }
        });
    }

    private void resetOnlinePayment() {
        tvOnlinePaymentTotalPrice.setText("" + paymentSelectedPrice);
        etCardNumber.setText("");
        etValidDateMonth.setText("");
        etValidDateYear.setText("");
    }

    private Runnable updateSubscriptionPaymentRunnable = new Runnable() {
        public void run() {
            lvPayment.requestFocus();
            lvPayment.setSelection(0);
            ((BaseAdapter) lvPayment.getAdapter()).notifyDataSetChanged();
        }
    };

    private void subscriptionPaymentUpdateSelectedPrice() {
        paymentSelectedPrice = 0;
        paymentSelectedMediaBillno = new StringBuilder("");

        for (int i = 0; i < lvPayment.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) lvPayment.getChildAt(i);
            if (itemLayout != null) {
                CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.paymentCheckBox);

                if (cb != null && cb.isChecked()) {
                    TextView tvPrice = (TextView) itemLayout.findViewById(R.id.paymentPrice);
                    if (tvPrice != null) {
                        String price = tvPrice.getText().toString();
                        String regEx = "[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？']";
                        price = Pattern.compile(regEx).matcher(price).replaceAll("").trim();
                        try {
                            paymentSelectedPrice += Integer.parseInt(price);
                        } catch (NumberFormatException e) {

                        }
                    }

                    TextView tvMediaBillno = (TextView) itemLayout.findViewById(R.id.paymentMediabillno);
                    if (tvMediaBillno != null
                            && paymentSelectedMediaBillno.toString().indexOf(tvMediaBillno.getText().toString()) < 0) {
                        paymentSelectedMediaBillno = paymentSelectedMediaBillno
                                .append(tvMediaBillno.getText().toString());
                        paymentSelectedMediaBillno = paymentSelectedMediaBillno.append(",");
                    }
                }
            }
        }

        tvPaymentSelectedPrice.setText("$" + paymentSelectedPrice);
        Log.d(TAG, "totalSelectedPrice:" + paymentSelectedPrice);

        if (paymentSelectedMediaBillno.length() > 0) {
            paymentSelectedMediaBillno = paymentSelectedMediaBillno
                    .deleteCharAt(paymentSelectedMediaBillno.length() - 1);
        }
        Log.d(TAG, "paymentSelectedMediaBillno:" + paymentSelectedMediaBillno.toString());
    }

    private void subscriptionPaymentSelectAllCheckBox(boolean bCheck) {
        isPaymentSelectAll = bCheck;
        paymentSelectedPrice = 0;
        paymentSelectedMediaBillno = new StringBuilder("");
        tv_select_all.setText((bCheck == true) ? getString(R.string.subscription_onlinepayment_deselectall)
                : getString(R.string.subscription_onlinepayment_selectall));

        for (int i = 0; i < lvPayment.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) lvPayment.getChildAt(i);
            if (itemLayout != null) {
                CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.paymentCheckBox);
                if (cb != null) {
                    cb.setChecked(bCheck);

                    if (cb.isChecked()) {
                        TextView tvPrice = (TextView) itemLayout.findViewById(R.id.paymentPrice);
                        if (tvPrice != null) {
                            String price = tvPrice.getText().toString();
                            String regEx = "[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？']";
                            price = Pattern.compile(regEx).matcher(price).replaceAll("").trim();
                            try {
                                paymentSelectedPrice += Integer.parseInt(price);
                            } catch (NumberFormatException e) {

                            }
                        }

                        TextView tvMediaBillno = (TextView) itemLayout.findViewById(R.id.paymentMediabillno);
                        if (tvMediaBillno != null && paymentSelectedMediaBillno.toString()
                                .indexOf(tvMediaBillno.getText().toString()) < 0) {
                            paymentSelectedMediaBillno = paymentSelectedMediaBillno
                                    .append(tvMediaBillno.getText().toString());
                            paymentSelectedMediaBillno = paymentSelectedMediaBillno.append(",");
                        }
                    }
                }
            }
        }

        tvPaymentSelectedPrice.setText("$" + paymentSelectedPrice);
        Log.d(TAG, "totalSelectedPrice:" + paymentSelectedPrice);

        if (paymentSelectedMediaBillno.length() > 0) {
            paymentSelectedMediaBillno = paymentSelectedMediaBillno
                    .deleteCharAt(paymentSelectedMediaBillno.length() - 1);
        }
        Log.d(TAG, "paymentSelectedMediaBillno:" + paymentSelectedMediaBillno.toString());
    }

    private void subscriptionPaymentInverseAllCheckBox() {
        paymentSelectedPrice = 0;
        paymentSelectedMediaBillno = new StringBuilder("");

        for (int i = 0; i < lvPayment.getChildCount(); i++) {
            LinearLayout itemLayout = (LinearLayout) lvPayment.getChildAt(i);
            CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.paymentCheckBox);
            cb.setChecked(!cb.isChecked());

            if (cb.isChecked()) {
                TextView tvPrice = (TextView) itemLayout.findViewById(R.id.paymentPrice);
                String price = tvPrice.getText().toString();
                String regEx = "[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？']";
                price = Pattern.compile(regEx).matcher(price).replaceAll("").trim();
                try {
                    paymentSelectedPrice += Integer.parseInt(price);
                } catch (NumberFormatException e) {

                }

                TextView tvMediaBillno = (TextView) itemLayout.findViewById(R.id.paymentMediabillno);
                paymentSelectedMediaBillno = paymentSelectedMediaBillno.append(tvMediaBillno.getText().toString());
                paymentSelectedMediaBillno = paymentSelectedMediaBillno.append(",");
            }
        }

        tvPaymentSelectedPrice.setText("$" + paymentSelectedPrice);
        Log.d(TAG, "totalSelectedPrice:" + paymentSelectedPrice);

        if (paymentSelectedMediaBillno.length() > 0) {
            paymentSelectedMediaBillno = paymentSelectedMediaBillno
                    .deleteCharAt(paymentSelectedMediaBillno.length() - 1);
        }
        Log.d(TAG, "paymentSelectedMediaBillno:" + paymentSelectedMediaBillno.toString());
    }

    private void updateSubscriptionPayment(QueryPrepayAmountResponseJson queryPrepayAmountResponseJson) {
        paymentSelectedPrice = 0;
        paymentSelectedMediaBillno = new StringBuilder("");

        subscriptionPaymentSelectAllCheckBox(false);

        getPaymentList(queryPrepayAmountResponseJson);
        goToPaymentFirstPage();

        tvPaymentTotal.setText(String.format("%02d", lvPayment.getCount()));
        handler.postDelayed(updateSubscriptionPaymentRunnable, 0);

        lvPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (null != view) {
                    if (lvPayment.getCount() > position) {
                        for (int i = 0; i < lvPayment.getCount(); i++) {
                            View item = lvPayment.getChildAt(i - lvPayment.getFirstVisiblePosition());
                            if (item != null) {
                                TextView tvPaymentItem = item.findViewById(R.id.paymentItem);
                                TextView tvPaymentPrice = item.findViewById(R.id.paymentPrice);
                                if (i == position) {
                                    tvPaymentItem.setTextSize(17);
                                    tvPaymentItem.setTypeface(null, Typeface.BOLD);
                                    tvPaymentPrice.setTextSize(17);
                                    tvPaymentPrice.setTypeface(null, Typeface.BOLD);
                                } else {
                                    tvPaymentItem.setTextSize(16);
                                    tvPaymentItem.setTypeface(null, Typeface.NORMAL);
                                    tvPaymentPrice.setTextSize(16);
                                    tvPaymentPrice.setTypeface(null, Typeface.NORMAL);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        lvPayment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox chkItem = (CheckBox) view.findViewById(R.id.paymentCheckBox);

                TextView mediaBillnoItem = (TextView) view.findViewById(R.id.paymentMediabillno);
                boolean itemIsChecked = chkItem.isChecked();

                chkItem.setChecked(!itemIsChecked);

                for (int i = 0; i < lvPayment.getChildCount(); i++) {
                    LinearLayout itemLayout = (LinearLayout) lvPayment.getChildAt(i);
                    CheckBox cb = (CheckBox) itemLayout.findViewById(R.id.paymentCheckBox);
                    TextView tvMediaBillno = (TextView) itemLayout.findViewById(R.id.paymentMediabillno);

                    if (mediaBillnoItem.getText().toString().equals(tvMediaBillno.getText().toString())) {
                        cb.setChecked(!itemIsChecked);
                    }
                }

                subscriptionPaymentUpdateSelectedPrice();
            }
        });

        lvPayment.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (lvPayment.getSelectedItemPosition() == 0) {
                        } else {
                            tvPaymentIndex.setText(String.format("%02d", lvPayment.getSelectedItemPosition()));
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (lvPayment.getCount() == (lvPayment.getSelectedItemPosition() + 1)) {
                        } else {
                            tvPaymentIndex.setText(String.format("%02d", lvPayment.getSelectedItemPosition() + 2));
                        }
                    }
                }
                return false;
            }
        });
    }

    private void clearPaymentFirstPage() {
        paymentPage = 1;
        lvPayment.setAdapter(null);
        tvPaymentSelectedPrice.setText("$0");
        tvPaymentAllShouldAmountPrice.setText("$0");
        tvPaymentIndex.setText("01");
        tvPaymentTotal.setText("01");
    }

    private void goToPaymentFirstPage() {
        paymentPage = 1;
        lvPayment.setAdapter(getPaymentListAdapter(1));
        tvPaymentIndex.setText("01");
    }

    public List<QueryPrepayAmountResponseJson.QueryPrepayAmountRetData2> getPaymentList(
            QueryPrepayAmountResponseJson queryPrepayAmountResponseJson) {
        paymentList = new ArrayList<QueryPrepayAmountResponseJson.QueryPrepayAmountRetData2>();

        try {
            tvPaymentAllShouldAmountPrice
                    .setText("$" + queryPrepayAmountResponseJson.getRetData().getCrmTotalShouldAmount());
            paymentList = queryPrepayAmountResponseJson.getRetData().getDataRowsList();
            if (paymentList.size() > 0) {
                llPaymentPageIndex.setVisibility(View.VISIBLE);
                llPaymentColorKey.setVisibility(View.VISIBLE);
            }
            double totalPage = Math.ceil((double) paymentList.size() / (double) paymentItemSizeOfPage);
            paymentTotalPage = (int) totalPage;
        } catch (Exception unused) {
        }

        return paymentList;
    }

    private SimpleAdapter getPaymentListAdapter(int page) {
        return new SimpleAdapter(this, getPaymentListData(page), R.layout.member_subscription_payment_item,
                new String[] { "check_box", "item_title", "item_service_period", "item_price", "item_media_billno" },
                new int[] { R.id.paymentCheckBox, R.id.paymentItem, R.id.paymentServicePeriod, R.id.paymentPrice,
                        R.id.paymentMediabillno });
    }

    private List<Map<String, Object>> getPaymentListData(int page) {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        tvPaymentIndex.setText("01");

        int endIndex = (paymentItemSizeOfPage) * (page);
        if (endIndex > paymentList.size()) {
            endIndex = paymentList.size();
        }

        for (int i = paymentItemSizeOfPage * (page - 1); i < endIndex; i++) {
            QueryPrepayAmountResponseJson.QueryPrepayAmountRetData2 payment = paymentList.get(i);

            map = new HashMap<String, Object>();
            map.put("check_box", false);
            map.put("item_title", payment.getCrmCItemName());
            map.put("item_service_period", payment.getCrmRealStartDate() + " - " + payment.getCrmRealStopDate());
            map.put("item_price", "$" + payment.getCrmShouldAmount());
            map.put("item_media_billno", payment.getCrmMediabillno());
            mList.add(map);
        }

        return mList;
    }

    private Runnable updateSubscriptionBillingHistoryRunnable = new Runnable() {
        public void run() {
            lvBillingHistory.requestFocus();
            lvBillingHistory.setSelection(0);
            ((BaseAdapter) lvBillingHistory.getAdapter()).notifyDataSetChanged();
        }
    };

    private void updateSubscriptionBillingHistory(QueryCrmMyAccountResponseJson queryCrmMyAccountResponseJson) {
        getBillingHistoryList(queryCrmMyAccountResponseJson);
        goToBillingHistoryFirstPage();

        tvBillingHistoryTotal.setText(String.format("%02d", billingHistoryTotalPage));
        handler.postDelayed(updateSubscriptionBillingHistoryRunnable, 0);

        lvBillingHistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (null != view) {
                    if (lvBillingHistory.getCount() > position) {
                        for (int i = 0; i < lvBillingHistory.getCount(); i++) {
                            View item = lvBillingHistory.getChildAt(i - lvBillingHistory.getFirstVisiblePosition());
                            if (item != null) {
                                TextView tvHistoryItem = item.findViewById(R.id.historyItem);
                                TextView tvHistoryPrice = item.findViewById(R.id.historyPrice);
                                if (i == position) {
                                    tvHistoryItem.setTextSize(17);
                                    tvHistoryItem.setTypeface(null, Typeface.BOLD);
                                    tvHistoryPrice.setTextSize(17);
                                    tvHistoryPrice.setTypeface(null, Typeface.BOLD);
                                } else {
                                    tvHistoryItem.setTextSize(16);
                                    tvHistoryItem.setTypeface(null, Typeface.NORMAL);
                                    tvHistoryPrice.setTextSize(16);
                                    tvHistoryPrice.setTypeface(null, Typeface.NORMAL);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        lvBillingHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "您點選了第 " + (position + 1) + " 項@@");
            }
        });

        lvBillingHistory.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (lvBillingHistory.getSelectedItemPosition() == 0) {
                            billingHistoryPageUp();
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (lvBillingHistory.getCount() == (lvBillingHistory.getSelectedItemPosition() + 1)) {
                            billingHistoryPageDown();
                        }
                    }
                }
                return false;
            }
        });
    }

    private void clearBillingHistoryFirstPage() {
        billingHistoryPage = 1;
        lvBillingHistory.setAdapter(null);
        tvBillingHistoryIndex.setText("01");
        tvBillingHistoryTotal.setText("01");
    }

    private void goToBillingHistoryFirstPage() {
        billingHistoryPage = 1;
        lvBillingHistory.setAdapter(getBillingHistoryListAdapter(1));
        tvBillingHistoryIndex.setText("01");
    }

    private void billingHistoryPageUp() {
        if (billingHistoryPage == 1) {
            billingHistoryPage = billingHistoryTotalPage;
        } else {
            billingHistoryPage = billingHistoryPage - 1;
        }

        lvBillingHistory.setAdapter(getBillingHistoryListAdapter(billingHistoryPage));
        if (billingHistoryPage == billingHistoryTotalPage) {
            lvBillingHistory.setSelection((billingHistoryList.size() % billingHistoryItemSizeOfPage) - 1);
        } else {
            lvBillingHistory.setSelection(billingHistoryItemSizeOfPage - 1);
        }
    }

    private void billingHistoryPageDown() {
        if (billingHistoryPage == billingHistoryTotalPage) {
            billingHistoryPage = 1;
        } else {
            billingHistoryPage = billingHistoryPage + 1;
        }

        lvBillingHistory.setAdapter(getBillingHistoryListAdapter(billingHistoryPage));
    }

    public List<QueryCrmMyAccountRetData2> getBillingHistoryList(
            QueryCrmMyAccountResponseJson queryCrmMyAccountResponseJson) {
        billingHistoryList = new ArrayList<QueryCrmMyAccountRetData2>();

        try {

            billingHistoryList = queryCrmMyAccountResponseJson.getRetData().getDataRowsList();
            double totalPage = Math.ceil((double) billingHistoryList.size() / (double) billingHistoryItemSizeOfPage);
            billingHistoryTotalPage = (int) totalPage;
        } catch (Exception unused) {
        }

        return billingHistoryList;
    }

    private SimpleAdapter getBillingHistoryListAdapter(int page) {
        return new SimpleAdapter(this, getBillingHistoryListData(page),
                R.layout.member_subscription_billing_history_item,
                new String[] { "item_title", "item_period", "item_payment_date", "item_price" },
                new int[] { R.id.historyItem, R.id.historyPeriod, R.id.historyPaymentDate, R.id.historyPrice });
    }

    private List<Map<String, Object>> getBillingHistoryListData(int page) {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        tvBillingHistoryIndex.setText(String.format("%02d", page));

        int endIndex = (billingHistoryItemSizeOfPage) * (page);
        if (endIndex > billingHistoryList.size()) {
            endIndex = billingHistoryList.size();
        }

        for (int i = billingHistoryItemSizeOfPage * (page - 1); i < endIndex; i++) {
            QueryCrmMyAccountRetData2 billingHistory = billingHistoryList.get(i);

            map = new HashMap<String, Object>();
            map.put("item_title", billingHistory.getCrmCItemName());
            map.put("item_period", billingHistory.getCrmStartDate() + " - " + billingHistory.getCrmStopDate());
            map.put("item_payment_date", billingHistory.getCrmRealDate());
            map.put("item_price", "$" + billingHistory.getCrmAmount());
            mList.add(map);
        }

        return mList;
    }

    private Button btnPrivacy, btnService, btnContract;
    private ImageView ivTerms;
    private ConstraintLayout clTerms;

    private void initClause() {
        initClausePopupWindow();

        btnPrivacy = (Button) findViewById(R.id.btnPrivacy);
        btnService = (Button) findViewById(R.id.btnService);
        btnContract = (Button) findViewById(R.id.btnContract);

        ivTerms = (ImageView) findViewById(R.id.imTerms);

        clTerms = (ConstraintLayout) findViewById(R.id.clTerms);

        btnPrivacy.setOnFocusChangeListener(OnFocusChangeListener);
        btnService.setOnFocusChangeListener(OnFocusChangeListener);
        btnContract.setOnFocusChangeListener(OnFocusChangeListener);

        btnPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTitle.setText(getString(R.string.msg_privacy));
                loadPrivacy();
                popupWindowClause.showAtLocation(clMain, Gravity.RIGHT, 0, 0);
                svClauseContent.scrollTo(0, 0);
                btnClauseOK.requestFocus();
            }
        });

        btnPrivacy.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_CLAUSE);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTitle.setText(getString(R.string.msg_service));
                loadService();
                popupWindowClause.showAtLocation(clMain, Gravity.RIGHT, 0, 0);
                svClauseContent.scrollTo(0, 0);
                btnClauseOK.requestFocus();
            }
        });

        btnService.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_CLAUSE);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), Contractual.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnContract.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_CLAUSE);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        StringBuilder stringBuilder = new StringBuilder();
        String serverPrefix = DEFAULT_MC_DATA_URL_PREFIX;
        stringBuilder.append(serverPrefix + "/contract/EPG-SO");
        String mSO = Utils.getSoId(true);
        stringBuilder.append(mSO);
        stringBuilder.append("-Banner.jpg");
        Log.d(TAG, "Terms img url:" + stringBuilder.toString());
        Glide.with((Activity) this).load(stringBuilder.toString()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                    boolean isFirstResource) {
                ViewGroup.LayoutParams params = clTerms.getLayoutParams();
                params.height = (int) (180 * density);
                clTerms.setLayoutParams(params);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                    DataSource dataSource, boolean isFirstResource) {
                ViewGroup.LayoutParams params = clTerms.getLayoutParams();
                params.height = (int) (310 * density);
                clTerms.setLayoutParams(params);
                return false;
            }
        }).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivTerms);

    }

    @Override
    public void HttpDownloadResponse(Pair<String, String> result) {
        if (result.first == "privacy.txt") {
            String message = result.second;
            if (message.indexOf("Error") != -1) {

            } else {
                saveFile("privacy.txt", result.second);
            }

            downloadQuestion();
        } else if (result.first == "question.json") {
            String message = result.second;
            if (message.indexOf("Error") != -1) {

            } else {
                saveFile("question.json", result.second);
            }

            downloadService();
        } else if (result.first == "service.txt") {
            String message = result.second;
            if (message.indexOf("Error") != -1) {

            } else {
                saveFile("service.txt", result.second);
            }

            downloadCrossScreen1();
        } else if (result.first == "ottfriday_1.txt") {
            String message = result.second;
            if (message.indexOf("Error") != -1) {

            } else {
                saveFile("ottfriday_1.txt", result.second);
                setOttfriday1(result.second);
            }

            downloadCrossScreen2();
        } else if (result.first == "ottfriday_2.txt") {
            String message = result.second;
            if (message.indexOf("Error") != -1) {

            } else {
                saveFile("ottfriday_2.txt", result.second);
                setOttfriday2(result.second);
            }

            downloadCrossScreen3();
        } else if (result.first == "ottfriday_3.txt") {
            String message = result.second;
            if (message.indexOf("Error") != -1) {

            } else {
                saveFile("ottfriday_3.txt", result.second);
                setOttfriday3(result.second);
            }
        }
    }

    private void downloadPrivacy() {
        httpDownloadTask = new HttpDownloadTask(this);
        String mSO = Utils.getSoId(false);
        httpDownloadTask.execute("https://cnsatv.totaltv.com.tw:8093/channel-banner/SO" + mSO + "/privacy.txt",
                "privacy.txt");
    }

    private void loadPrivacy() {
        String message = load("privacy.txt");
        if (message.equals("")) {
            tvClauseContent.setText(getFromAssets("privacy.txt"));
        } else {
            tvClauseContent.setText(message);
        }
    }

    private void downloadQuestion() {
        httpDownloadTask = new HttpDownloadTask(this);
        String mSO = Utils.getSoId(false);
        httpDownloadTask.execute("https://cnsatv.totaltv.com.tw:8093/channel-banner/SO" + mSO + "/question.json",
                "question.json");
    }

    private String loadQuestion() {
        Log.d(TAG, "loadQuestionloadQuestionloadQuestionloadQuestionloadQuestionloadQuestionloadQuestion");
        String message = load("question.json");
        if (message.equals("")) {
            message = getFromAssets("question.json");
        } else {

        }

        return message;
    }

    private void downloadService() {
        httpDownloadTask = new HttpDownloadTask(this);
        String mSO = Utils.getSoId(false);
        httpDownloadTask.execute("https://cnsatv.totaltv.com.tw:8093/channel-banner/SO" + mSO + "/service.txt",
                "service.txt");
    }

    private void loadService() {
        String message = load("service.txt");
        if (message.equals("")) {
            tvClauseContent.setText(getFromAssets("service.txt"));
        } else {
            tvClauseContent.setText(message);
        }
    }

    private void downloadCrossScreen1() {
        httpDownloadTask = new HttpDownloadTask(this);
        String serverPrefix = DEFAULT_MC_DATA_URL_PREFIX;
        String mSO = Utils.getSoId(false);
        httpDownloadTask.execute(serverPrefix + "/channel-banner/SO" + mSO + "/ottfriday_1.txt", "ottfriday_1.txt");
    }

    private void setOttfriday1(String text) {
        if (!text.equals("") && !text.isEmpty()) {
            tvContent1.setText(text);
        }
    }

    private void downloadCrossScreen2() {
        httpDownloadTask = new HttpDownloadTask(this);
        String serverPrefix = DEFAULT_MC_DATA_URL_PREFIX;
        String mSO = Utils.getSoId(false);
        httpDownloadTask.execute(serverPrefix + "/channel-banner/SO" + mSO + "/ottfriday_2.txt", "ottfriday_2.txt");
    }

    private void setOttfriday2(String text) {
        if (!text.equals("") && !text.isEmpty()) {
            tvContent2.setText(text);
        }
    }

    private void downloadCrossScreen3() {
        httpDownloadTask = new HttpDownloadTask(this);
        String serverPrefix = DEFAULT_MC_DATA_URL_PREFIX;
        String mSO = Utils.getSoId(false);
        httpDownloadTask.execute(serverPrefix + "/channel-banner/SO" + mSO + "/ottfriday_3.txt", "ottfriday_3.txt");
    }

    private void setOttfriday3(String text) {
        if (!text.equals("") && !text.isEmpty()) {
            tvContent3.setText(text);
        }
    }

    public boolean saveFile(String fileName, String mytext) {
        Log.i(TAG, "SAVE - " + fileName);
        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(mytext);
            out.close();
            return true;
        } catch (IOException e) {
            Log.i(TAG, "Save Data Error");
            e.printStackTrace();
            return false;
        }
    }

    public String load(String fileName) {
        Log.i(TAG, "LOAD - " + fileName);
        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line = "";
            String allLine = "";
            while ((line = r.readLine()) != null) {
                allLine = allLine + line + "\n";
            }
            r.close();
            return allLine;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "LOAD Data Error");
            return "";
        }
    }

    public String getFromAssets(String str) {
        Log.d(TAG, "getFromAssets " + str);
        String str2 = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(getResources().getAssets().open(str)));
            String str3 = "";
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    return str3;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str3);
                stringBuilder.append(readLine);
                stringBuilder.append("\n");
                str3 = stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return str2;
        }
    }

    private View popupViewClause;
    private PopupWindow popupWindowClause;

    private TextView tvTitle, tvClauseContent;
    private TextView tvClauseLineTop, tvClauseLineBottom;
    private Button btnClauseOK;
    ScrollView svClauseContent;

    private void initClausePopupWindow() {
        popupViewClause = LayoutInflater.from(this).inflate(R.layout.dialog_terms, null);
        popupWindowClause = new PopupWindow(popupViewClause, ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, true);

        tvTitle = (TextView) popupViewClause.findViewById(R.id.tvTitle);
        tvClauseContent = (TextView) popupViewClause.findViewById(R.id.tvContent);

        tvClauseLineTop = (TextView) popupViewClause.findViewById(R.id.tvLineTop);
        tvClauseLineBottom = (TextView) popupViewClause.findViewById(R.id.tvLineBottom);

        btnClauseOK = (Button) popupViewClause.findViewById(R.id.btnOK);

        svClauseContent = (ScrollView) popupViewClause.findViewById(R.id.svContent);

        svClauseContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                if (hasFocus) {
                    tvClauseLineTop.setBackgroundColor(getResources().getColor(R.color.colorTabSelect));
                    tvClauseLineBottom.setBackgroundColor(getResources().getColor(R.color.colorTabSelect));
                } else {
                    tvClauseLineTop.setBackgroundColor(getResources().getColor(R.color.colorWhiteOpacity40));
                    tvClauseLineBottom.setBackgroundColor(getResources().getColor(R.color.colorWhiteOpacity40));
                }

            }

        });

        btnClauseOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTitle.setSelected(false);
                popupWindowClause.dismiss();
            }
        });
    }

    private TextView tvNotRead, tvNotReadTotal, tvMailPageIndex, tvMailPageTotal, tvNoMail, tvMailLimit;
    private ListView lvMail;
    private LinearLayout llMailMax, llID, llType, llMailPageIndex, llMailKeyBar;
    private boolean isFocus = false;
    private int mailPosition = 0;

    private void initMail() {
        initMailDeletePopupWindow();

        tvNotRead = (TextView) findViewById(R.id.tvNotRead);
        tvNotReadTotal = (TextView) findViewById(R.id.tvNotReadTotal);
        tvMailPageIndex = (TextView) findViewById(R.id.tvMailPageIndex);
        tvMailPageTotal = (TextView) findViewById(R.id.tvMailPageTotal);
        tvNoMail = (TextView) findViewById(R.id.tvNoMail);
        tvMailLimit = (TextView) findViewById(R.id.tvMailLimit);
        lvMail = (ListView) findViewById(R.id.lvMail);
        llMailMax = (LinearLayout) findViewById(R.id.llMailMax);
        llID = (LinearLayout) findViewById(R.id.llID);
        llType = (LinearLayout) findViewById(R.id.llType);
        llMailPageIndex = (LinearLayout) findViewById(R.id.llMailPageIndex);
        llMailKeyBar = (LinearLayout) findViewById(R.id.llMailKeyBar);

        llID.setVisibility(View.GONE);
        llType.setVisibility(View.GONE);
        llMailPageIndex.setVisibility(View.GONE);
        llMailKeyBar.setVisibility(View.GONE);

        lvMail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                isFocus = hasFocus;
                if (hasFocus) {
                    llMailMax.setVisibility(View.VISIBLE);
                    llID.setVisibility(View.VISIBLE);
                    llType.setVisibility(View.VISIBLE);
                    llMailPageIndex.setVisibility(View.VISIBLE);
                    llMailKeyBar.setVisibility(View.VISIBLE);

                    updateMail();
                } else {
                    llMailMax.setVisibility(View.GONE);
                    llID.setVisibility(View.GONE);
                    llType.setVisibility(View.GONE);
                    llMailPageIndex.setVisibility(View.GONE);
                    llMailKeyBar.setVisibility(View.GONE);

                    if (lvMail.getCount() > mailPosition) {
                        for (int i = 0; i < lvMail.getCount(); i++) {
                            View item = lvMail.getChildAt(i - lvMail.getFirstVisiblePosition());
                            if ((item != null) && (i == mailPosition)) {
                                TextView tvItem_title = item.findViewById(R.id.item_title);
                                TextView tvItem_importance = item.findViewById(R.id.item_importance);
                                tvItem_title.setTextSize(16);
                                tvItem_title.setTypeface(null, Typeface.NORMAL);
                                tvItem_importance.setTextSize(16);
                                tvItem_importance.setTypeface(null, Typeface.NORMAL);
                                break;
                            }
                        }
                    }
                }
            }
        });

        lvMail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mailType = allMailHeads.get(position + mailPage * 9).getMailType();
                view.findViewById(R.id.item_read).setVisibility(View.GONE);
                if (mailType.equals("CAMAIL")) {
                    // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                } else if (mailType.equals("TVMAIL")) {
                    TvMailManager.handleTvMailInfo(getApplicationContext(),
                            allMailHeads.get(position + mailPage * 9).getMail_id(), false);
                }
            }
        });

        lvMail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mailPosition = position;
                if (null != view) {
                    if (lvMail.getCount() > position) {
                        for (int i = 0; i < lvMail.getCount(); i++) {
                            View item = lvMail.getChildAt(i - lvMail.getFirstVisiblePosition());
                            if (item != null) {
                                TextView tvItem_title = item.findViewById(R.id.item_title);
                                TextView tvItem_importance = item.findViewById(R.id.item_importance);
                                if ((i == position) && lvMail.isFocused()) {
                                    tvItem_title.setTextSize(17);
                                    tvItem_title.setTypeface(null, Typeface.BOLD);
                                    tvItem_importance.setTextSize(17);
                                    tvItem_importance.setTypeface(null, Typeface.BOLD);
                                } else {
                                    tvItem_title.setTextSize(16);
                                    tvItem_title.setTypeface(null, Typeface.NORMAL);
                                    tvItem_importance.setTextSize(16);
                                    tvItem_importance.setTypeface(null, Typeface.NORMAL);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lvMail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_PROG_RED || keyCode == KeyEvent.KEYCODE_F9) {
                        showDeleteMail("");
                    } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN || keyCode == KeyEvent.KEYCODE_F10) {
                        showDeleteMail("All");
                    } else if ((keyCode == KeyEvent.KEYCODE_PROG_YELLOW) || (keyCode == KeyEvent.KEYCODE_F11)) {
                        readMailAll();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        goToList(MEMBER_MAIL);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (mailPage != 0 && 0 == mailPosition) {
                            mailPage = mailPage - 1;
                            updateMail();
                            lvMail.setSelection(lvMail.getAdapter().getCount() - 1);
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if ((mailPage + 1) != mailTotalPages && (lvMail.getAdapter().getCount() - 1) == mailPosition) {
                            mailPage = mailPage + 1;
                            updateMail();
                            lvMail.setSelection(0);
                        } else if ((mailPage + 1) == mailTotalPages
                                && (lvMail.getAdapter().getCount() - 1) == mailPosition) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    private SimpleAdapter getMailListAdapter(int page) {
        return new Adapter(this, getMailLisData(page), R.layout.member_mail_item,
                new String[] { "item_id", "item_title", "item_importance", "item_date" },
                new int[] { R.id.item_id, R.id.item_title, R.id.item_importance, R.id.item_date });
    }

    private List<AllMailHead> allMailHeads;

    private List<Map<String, Object>> getMailLisData(int page) {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        int max = allMailHeads.size();
        int first = 0 + page * 9;
        int last = 0 + (page + 1) * 9;
        if (last > max) {
            last = max;
        }

        for (int i = first; i < last; i++) {
            map = new HashMap<String, Object>();

            if (allMailHeads.get(i).getMailType().equals("TVMAIL")) {
                map.put("item_id", "");
            } else {
                map.put("item_id", allMailHeads.get(i).getMail_id());
            }
            map.put("item_read", "" + allMailHeads.get(i).getNewEmail());

            map.put("item_title", allMailHeads.get(i).getMailHead());
            String importance = "";
            if (allMailHeads.get(i).getMailType().equals("TVMAIL")) {
                if (allMailHeads.get(i).getImportance() == 3) {
                    importance = getString(R.string.msg_important);
                } else if (allMailHeads.get(i).getImportance() == 2) {
                    importance = getString(R.string.msg_medium);
                } else {
                    importance = getString(R.string.msg_normal);
                }
            } else {
                if (allMailHeads.get(i).getImportance() == 0) {
                    importance = getString(R.string.msg_normal);
                } else {
                    importance = getString(R.string.msg_important);
                }
            }

            map.put("item_importance", importance);
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(allMailHeads.get(i).getCreateTime());
            map.put("item_date", time);
            mList.add(map);
        }

        return mList;
    }

    private int mailTotalPages = 0;
    private int mailPage = 0;

    private void updateMail() {
        allMailHeads = tvMailDbHelper.mergeAndGetAllMails();
        Log.d(TAG, "TvMail, allMailHeads size: " + allMailHeads.size());

        int mailMax = 99;
        tvMailLimit.setText(" " + mailMax);
        int size = allMailHeads.size();
        Log.d(TAG, "Mail Max = " + mailMax);
        if (allMailHeads.size() > mailMax) {
            AllMailHead allMailHead = allMailHeads.get(allMailHeads.size() - 1);
            Log.d(TAG, "Mail size > " + mailMax + ", Remove Last one !!! Mail Type = " + allMailHead.getMailType());
            if (allMailHead.getMailType().equals("CAMAIL")) {
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            } else if (allMailHead.getMailType().equals("TVMAIL")) {
                tvMailDbHelper.deleteMail(allMailHead.getMail_id());
            }
        }

        mailTotalPages = getMailTotalPages(size);

        if (size != 0) {
            tvNoMail.setVisibility(View.INVISIBLE);
        } else {
            tvNoMail.setVisibility(View.VISIBLE);
        }

        lvMail.setAdapter(getMailListAdapter(mailPage));

        tvNotRead.setText(getMailUnreadSizeWithoutCommand());
        tvNotReadTotal.setText(getMailSizeWithoutCommand());

        String pageS = "";
        int page = mailPage + 1;
        if (page < 10) {
            pageS = "0" + page;
        } else {
            pageS = "" + page;
        }

        String totalPageS = "";
        int totalPage = mailTotalPages;
        if (page < 10) {
            totalPageS = "0" + totalPage;
        } else {
            totalPageS = "" + totalPage;
        }

        tvMailPageIndex.setText(pageS);
        tvMailPageTotal.setText(totalPageS);
    }

    private int getMailTotalPages(int size) {
        int pages = 0;

        if (size >= 9 && size % 9 == 0) {
            pages = size / 9;
        } else {
            pages = size / 9 + 1;
        }

        return pages;
    }

    private String getMailUnreadSizeWithoutCommand() {
        int mailSize = 0;

        for (AllMailHead allMailHead : allMailHeads) {
            if (allMailHead.getNewEmail() == 1)
                mailSize = mailSize + 1;
        }

        if (mailSize < 10) {
            return "0" + mailSize;
        } else {
            return "" + mailSize;
        }
    }

    private String getMailSizeWithoutCommand() {
        int mailSize = 0;

        for (AllMailHead allMailHead : allMailHeads) {
            mailSize = mailSize + 1;
        }

        if (mailSize < 10) {
            return "0" + mailSize;
        } else {
            return "" + mailSize;
        }
    }

    private ConstraintLayout memberQuestionQuestion;
    private ListView lvQuestion;
    private TextView tvQuestionIndex, tvQuestionTotal;
    private Button btnTutorial, btnQuestion;

    private void initQuestion() {
        memberQuestionQuestion = (ConstraintLayout) findViewById(R.id.memberQuestionQuestion);

        btnTutorial = (Button) findViewById(R.id.btnTutorial);
        btnQuestion = (Button) findViewById(R.id.btnQuestion);

        btnTutorial.setOnFocusChangeListener(OnFocusChangeListener);
        btnQuestion.setOnFocusChangeListener(OnFocusChangeListener);

        btnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), Tutorial.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnTutorial.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_QUESTION);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnQuestion.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        goToList(MEMBER_QUESTION);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return true;
                    }
                }
                return false;
            }
        });

        btnQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memberQuestionQuestion.setVisibility(View.VISIBLE);

                lvQuestion.requestFocus();
                lvQuestion.setSelection(0);
                ((BaseAdapter) lvQuestion.getAdapter()).notifyDataSetChanged();
            }
        });

        tvQuestionIndex = (TextView) findViewById(R.id.tvQuestionIndex);
        tvQuestionTotal = (TextView) findViewById(R.id.tvQuestionTotal);

        lvQuestion = (ListView) findViewById(R.id.lvQuestion);

        getQuestionList();
        goToQuestionFirstPage();

        lvQuestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvTitle.setText(questionList.get(position + itemSizeOfPage * (questionPage - 1)).getTitle());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvTitle.setSelected(true);
                    }
                }, 2000);
                tvClauseContent.setText(questionList.get(position + itemSizeOfPage * (questionPage - 1)).getContent());
                popupWindowClause.showAtLocation(clMain, Gravity.RIGHT, 0, 0);
                svClauseContent.scrollTo(0, 0);
                btnClauseOK.requestFocus();
            }
        });

        lvQuestion.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (lvQuestion.getSelectedItemPosition() == 0) {
                            questionPageUp();
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (lvQuestion.getCount() == (lvQuestion.getSelectedItemPosition() + 1)) {
                            questionPageDown();
                        }
                    }
                }
                return false;
            }
        });
    }

    private void goToQuestionFirstPage() {
        questionPage = 1;
        lvQuestion.setAdapter(getQuestionListAdapter(1));
        tvQuestionIndex.setText("01");
    }

    private int questionPage = 1;

    private void questionPageUp() {
        if (questionPage == 1) {
            questionPage = 5;
        } else {
            questionPage = questionPage - 1;
        }

        lvQuestion.setAdapter(getQuestionListAdapter(questionPage));
        lvQuestion.setSelection(itemSizeOfPage - 1);
    }

    private void questionPageDown() {
        if (questionPage == 5) {
            questionPage = 1;
        } else {
            questionPage = questionPage + 1;
        }

        lvQuestion.setAdapter(getQuestionListAdapter(questionPage));
    }

    List<Question> questionList;

    public List<Question> getQuestionList() {
        questionList = new ArrayList<Question>();

        try {
            Gson gson = new Gson();
            Question[] questions = gson.fromJson(loadQuestion(), Question[].class);
            questionList = Arrays.asList(questions);
        } catch (Exception unused) {

        }

        return questionList;
    }

    private SimpleAdapter getQuestionListAdapter(int page) {
        return new SimpleAdapter(this, getQuestionLisData(page), R.layout.member_question_item,
                new String[] { "item_title", "item_content" }, new int[] { R.id.item_title });
    }

    int itemSizeOfPage = 8;

    private List<Map<String, Object>> getQuestionLisData(int page) {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        tvQuestionIndex.setText("0" + page);

        int endIndex = (itemSizeOfPage) * (page);
        if (endIndex > questionList.size()) {
            endIndex = questionList.size();
        }

        for (int i = itemSizeOfPage * (page - 1); i < endIndex; i++) {
            Question question = questionList.get(i);
            map = new HashMap<String, Object>();
            map.put("item_title", question.getTitle());
            map.put("item_content", question.getContent());
            mList.add(map);
        }

        return mList;
    }

    private SimpleAdapter getQuestionListAdapter() {
        return new SimpleAdapter(this, getQuestionLisData(), R.layout.member_question_item,
                new String[] { "item_title", "item_content" }, new int[] { R.id.item_title });
    }

    private List<Map<String, Object>> getQuestionLisData() {
        List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        List<Question> questionList = getQuestionList();

        for (Question question : questionList) {
            map = new HashMap<String, Object>();
            map.put("item_title", question.getTitle());
            map.put("item_content", question.getContent());
            mList.add(map);
        }

        return mList;
    }

    TextView tvContent1, tvContent2, tvContent3;
    ImageView ivQRCode;
    ScrollView svCrossScreen;

    private void initCrossScreen() {

        tvContent1 = (TextView) findViewById(R.id.tvContent1);
        tvContent2 = (TextView) findViewById(R.id.tvContent2);
        tvContent3 = (TextView) findViewById(R.id.tvContent3);
        ivQRCode = (ImageView) findViewById(R.id.ivQRCode);
        svCrossScreen = (ScrollView) findViewById(R.id.svContent);

        tvContent1.setText(load("ottfriday_1.txt"));
        tvContent2.setText(load("ottfriday_2.txt"));
        tvContent3.setText(load("ottfriday_3.txt"));

        StringBuilder qrCodeStringBuilder = new StringBuilder();
        String serverPrefix = DEFAULT_MC_DATA_URL_PREFIX;
        qrCodeStringBuilder.append(serverPrefix + "/channel-banner/SO");
        String mSO = Utils.getSoId(false);
        qrCodeStringBuilder.append(mSO);
        qrCodeStringBuilder.append("/ottfriday_QR.png");
        Log.d(TAG, "CrossScreen QRCode img url:" + qrCodeStringBuilder.toString());
        Glide.with((Activity) this).load(qrCodeStringBuilder.toString()).into(ivQRCode);

        svCrossScreen.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        goToList(MEMBER_FRIDAY_CROSS_SCREEN);
                    }
                }
                return false;
            }
        });

    }

    public View.OnFocusChangeListener NoAnimationOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.BOLD);
                    rbBtn.setTextSize(17);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.BOLD);
                    btnBtn.setTextSize(17);
                } else if (view instanceof EditText) {
                    EditText etText = (EditText) view;
                    etText.setTypeface(null, Typeface.BOLD);
                    etText.setTextSize(17);
                }
            } else {
                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.NORMAL);
                    rbBtn.setTextSize(16);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.NORMAL);
                    btnBtn.setTextSize(16);
                } else if (view instanceof EditText) {
                    EditText etText = (EditText) view;
                    etText.setTypeface(null, Typeface.NORMAL);
                    etText.setTextSize(16);
                }
            }
        }
    };

    public View.OnFocusChangeListener OnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                view.bringToFront();
                ScaleAnimation am = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                am.setDuration(200);
                am.setFillAfter(true);
                view.setAnimation(am);
                am.startNow();

                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.BOLD);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.BOLD);
                }
            } else {
                view.clearAnimation();

                if (view instanceof RadioButton) {
                    RadioButton rbBtn = (RadioButton) view;
                    rbBtn.setTypeface(null, Typeface.NORMAL);
                } else if (view instanceof Button) {
                    Button btnBtn = (Button) view;
                    btnBtn.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    };

    private Handler mHandler2 = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {

            String api = msg.getData().getString("API");
            try {
                if (api.equals("QueryCustInfo")) {
                    QueryCustInfoResponseJson obj = (QueryCustInfoResponseJson) msg.obj;
                    if (!obj.getRetCode().equals("0")) {
                    }

                    tvCustId.setText(obj.getRetData().getCustId());
                    tvSubsId.setText(obj.getRetData().getSubsId());
                    return;
                } else if (api.equals("QueryPrepayAmount")) {
                    QueryPrepayAmountResponseJson obj = (QueryPrepayAmountResponseJson) msg.obj;

                    if (!obj.getRetCode().equals("0")) {
                    }
                    updateSubscriptionPayment(obj);
                } else if (api.equals("CrmPayBill")) {
                    CrmPayBillResponseJson obj = (CrmPayBillResponseJson) msg.obj;

                    if (!obj.getRetCode().equals("0")) {
                    }

                    Toast.makeText(MemberActivity.this, obj.getRetMsg(), Toast.LENGTH_LONG).show();
                    popupWindowOnlinePayment.dismiss();

                    enterPayment();
                } else if (api.equals("QueryCrmMyAccount")) {
                    QueryCrmMyAccountResponseJson obj = (QueryCrmMyAccountResponseJson) msg.obj;

                    if (!obj.getRetCode().equals("0")) {
                    }
                    updateSubscriptionBillingHistory(obj);
                } else if (api.equals("GetVodPoint")) {
                    GetVodPointResponseJson obj = (GetVodPointResponseJson) msg.obj;
                    if (!obj.getRetResult().getCode().equals("0")) {
                    }

                    String queryTime = msg.getData().getString("QUERY_TIME");
                    String vodPointDueDate = msg.getData().getString("VOD_POINT_DUE_DATE");

                    tvGiftPoint.setText(obj.getRetDataList().get(0).getGiftpoint());
                    tvUsePoint.setText(obj.getRetDataList().get(0).getUsepoint());
                    tvSurplusPoint.setText(obj.getRetDataList().get(0).getSurpluspoint());
                    tvQueryTime.setText(queryTime);

                    if (!TextUtils.isEmpty(obj.getRetDataList().get(0).getRecvTill())) {
                        tvDueDate.setText(obj.getRetDataList().get(0).getRecvTill());
                    } else {
                        tvDueDate.setText(vodPointDueDate);
                    }

                    String vodDiscount = obj.getRetDataList().get(0).getVoddiscount();
                    if (vodDiscount == null || vodDiscount.equals("")) {
                        vodDiscount = "60";
                    }
                    tvVodDiscount.setText(vodDiscount + "%");

                    if (!TextUtils.isEmpty(obj.getRetDataList().get(0).getRemark())) {
                        Spannable textLable;
                        textLable = new SpannableString(Html.fromHtml(obj.getRetDataList().get(0).getRemark()));
                        tvOnlineRemark.setText(textLable);
                        svOnlineRemark.setVisibility(View.VISIBLE);
                        llDefaultRemark.setVisibility(View.GONE);
                    } else {
                        tvOnlineRemark.setText("");
                        svOnlineRemark.setVisibility(View.GONE);
                        llDefaultRemark.setVisibility(View.VISIBLE);
                    }

                    return;
                }
            } catch (Exception e) {
            }
        }
    };

    private void enterPayment() {
        listIndex = MEMBER_SUBSCRIPTION;
        showMembersRight(MEMBER_SUBSCRIPTION);

        new Thread() {
            @Override
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
                } catch (Exception e) {
                    Log.d(TAG, "" + e);
                }
            }
        }.start();

        memberSubscriptionPayment.setVisibility(View.VISIBLE);

        QueryPrepayAmountOBJ mQueryPrepayAmountOBJ;
        String mSO = Utils.getSoId(true);
        mQueryPrepayAmountOBJ = new QueryPrepayAmountOBJ(mSO, Utils.getSN(), "", "0");

        PostDataMemberCenter postDataSMS = new PostDataMemberCenter(MemberActivity.this, mHandler2);
        postDataSMS.sendQueryPrepayAmount(mQueryPrepayAmountOBJ);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (memberSubscriptionPayment.getVisibility() == View.VISIBLE) {
                btnPayment.requestFocus();
                memberSubscriptionPayment.setVisibility(View.INVISIBLE);
                clearPaymentFirstPage();
                return true;
            }

            if (memberSubscriptionBillingHistory.getVisibility() == View.VISIBLE) {
                btnBillingHistory.requestFocus();
                memberSubscriptionBillingHistory.setVisibility(View.INVISIBLE);
                clearBillingHistoryFirstPage();
                return true;
            }

            if (memberQuestionQuestion.getVisibility() == View.VISIBLE) {
                btnQuestion.requestFocus();
                memberQuestionQuestion.setVisibility(View.INVISIBLE);

                goToQuestionFirstPage();
                return true;
            }

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (memberSubscriptionPayment.getVisibility() == View.VISIBLE) {
                memberSubscriptionPayment.setVisibility(View.INVISIBLE);
                clearPaymentFirstPage();
                goToList(MEMBER_SUBSCRIPTION);
                return true;
            }

            if (memberSubscriptionBillingHistory.getVisibility() == View.VISIBLE) {
                memberSubscriptionBillingHistory.setVisibility(View.INVISIBLE);
                clearBillingHistoryFirstPage();
                goToList(MEMBER_SUBSCRIPTION);
                return true;
            }

            if (memberQuestionQuestion.getVisibility() == View.VISIBLE) {
                memberQuestionQuestion.setVisibility(View.INVISIBLE);
                goToQuestionFirstPage();
                goToList(MEMBER_QUESTION);
                return true;
            }
        } else if ((keyCode == KeyEvent.KEYCODE_PROG_RED) || (keyCode == KeyEvent.KEYCODE_F9)) {
        } else if ((keyCode == KeyEvent.KEYCODE_PROG_GREEN) || (keyCode == KeyEvent.KEYCODE_F10)) {
        } else if ((keyCode == KeyEvent.KEYCODE_PROG_YELLOW) || (keyCode == KeyEvent.KEYCODE_F11)) {
            if (memberSubscriptionPayment.isShown()) {
                subscriptionPaymentSelectAllCheckBox(!isPaymentSelectAll);
            }
        } else if ((keyCode == KeyEvent.KEYCODE_PROG_BLUE) || (keyCode == KeyEvent.KEYCODE_F12)) {
            if (memberSubscriptionPayment.isShown()) {
                resetOnlinePayment();
                popupWindowOnlinePayment.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.RIGHT, 0, 0);
            }
        } else if (keyCode == KeyEvent.KEYCODE_1) {
        } else if (keyCode == KeyEvent.KEYCODE_2) {
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.d(TAG, "onActivityResult requestCode = "+requestCode + " resultCode = " +
        // resultCode);
        if (requestCode == 999 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String value = data.getStringExtra("Action");
                Intent newIntent = new Intent();
                newIntent.putExtra("Action", value);
                setIntent(newIntent);
            }
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        registerCmModeContentObserver(this);
        handler.postDelayed(updateTimer, 500);

        lvMembers.setAdapter(getMemberListAdapter());

        downloadPrivacy();
        handleIntentAction();
    }

    private void handleIntentAction() {
        Intent intent = getIntent();
        if (intent != null) {
            final String action = intent.getStringExtra("Action");
            Log.d(TAG, "handleIntentAction: " + action);
            if ("QuestionList".equals(action)) {
                goToList(MEMBER_QUESTION);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (btnQuestion != null) {
                            btnQuestion.performClick();
                        }
                    }
                }, 200);

                intent.removeExtra("Action");
            }
            if ("Tutorial".equals(action)) {
                goToList(MEMBER_QUESTION);
                intent.removeExtra("Action");
            }
            if (intent.getBooleanExtra("isOpenMessage", false)) {
                listIndex = MEMBER_MAIL;
                showMembersRight(MEMBER_MAIL);
                goToList(MEMBER_MAIL);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (lvMail != null) {
                            lvMail.setFocusable(true);
                            lvMail.setFocusableInTouchMode(true);
                            lvMail.requestFocus();
                            lvMail.setSelection(0);
                            // Log.d(TAG, "isOpenMessage: lvMail requestFocus executed");
                        }
                    }
                }, 500);
                intent.removeExtra("isOpenMessage");
            }
            if (intent.getBooleanExtra("isOpenPayment", false)) {
                listIndex = MEMBER_SUBSCRIPTION;
                showMembersRight(MEMBER_SUBSCRIPTION);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goToList(MEMBER_SUBSCRIPTION);
                        enterPayment();
                    }
                }, 500);
                intent.removeExtra("isOpenPayment");
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        unregisterCmModeContentObserver(this);
        handler.removeCallbacks(updateTimer);
    }

    public void openFridayGooglePlay() {
        if(Pvcfg.get_cns_overdue_payment() == 1)
            return;
        Log.d(TAG, "friDay OnClick >>> going to open google play.");
        if (!Utils.isAppInstalled(this, "net.fetnet.fetvod.tv")) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("market://details?id=net.fetnet.fetvod.tv"));
                intent.setPackage("com.android.vending");
                // intent.addFlags(268435456);
                startActivity(intent);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Intent intent2 = new Intent();
        intent2.setPackage("net.fetnet.fetvod.tv");
        intent2.setAction("android.intent.action.MAIN");
        intent2.addCategory(IntentCompat.CATEGORY_LEANBACK_LAUNCHER);
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> listQueryIntentActivities = packageManager.queryIntentActivities(intent2, 0);
        if (listQueryIntentActivities == null || listQueryIntentActivities.size() == 0) {
            Intent intent3 = new Intent();
            intent3.setPackage("net.fetnet.fetvod.tv");
            intent3.setAction("android.intent.action.MAIN");
            intent3.addCategory("android.intent.category.LAUNCHER");
            listQueryIntentActivities = packageManager.queryIntentActivities(intent3, 0);
        }
        Collections.sort(listQueryIntentActivities, new ResolveInfo.DisplayNameComparator(packageManager));
        if (listQueryIntentActivities.size() > 0) {
            ActivityInfo activityInfo = listQueryIntentActivities.get(0).activityInfo;
            Log.d("TvMailDialog", "launchApp-" + activityInfo.applicationInfo.packageName + "-" + activityInfo.name);
            ComponentName componentName = new ComponentName(activityInfo.applicationInfo.packageName,
                    activityInfo.name);
            Intent intent4 = new Intent("android.intent.action.MAIN");
            // intent4.setFlags(270532608);
            intent4.setComponent(componentName);
            try {
                startActivity(intent4);
            } catch (Exception unused) {
            }
        }
    }

    private void registerCmModeContentObserver(Context context) {
        Log.d(TAG, "registerCmModeContentObserver");
        updateCableModeFlag(this);
        ContentResolver contentResolver = context.getContentResolver();
        this.cmModeValueObserver = new CmModeContentObserver(null, context);
        contentResolver.registerContentObserver(Settings.System.getUriFor("cm_mode"), true, this.cmModeValueObserver);
    }

    private void unregisterCmModeContentObserver(Context context) {
        Log.d(TAG, "unregisterCmModeContentObserver");
        if (this.cmModeValueObserver != null) {
            context.getContentResolver().unregisterContentObserver(this.cmModeValueObserver);
        }
        this.cmModeValueObserver = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean updateCableModeFlag(Context context) {
        boolean z = GposInfo.getCmMode(this) == 0;//Settings.System.getInt(context.getContentResolver(), "cm_mode", 1) == 0;
        if (this.mCableMode == z) {
            return false;
        }
        Log.d(TAG, "updateCableModeFlag:" + z);
        this.mCableMode = z;
        return true;
    }

    private final class CmModeContentObserver extends ContentObserver {
        private Context mContext;

        public CmModeContentObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
        }

        /*
         * JADX WARN: Type inference failed for: r2v4, types:
         * [com.hwacom.homeplus.membercenter.MemberActivity$CmModeContentObserver$1]
         */
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            Log.d(MemberActivity.TAG, "onChange cmMode");
            if (MemberActivity.this.updateCableModeFlag(this.mContext)) {
                new Thread() { // from class:
                               // com.hwacom.homeplus.membercenter.MemberActivity.CmModeContentObserver.1
                    @Override // java.lang.Thread, java.lang.Runnable
                    public void run() {
                        Looper.prepare();
                        Context baseContext = MemberActivity.this.getBaseContext();
                        StringBuilder sb = new StringBuilder();
                        sb.append("模式切換: ");
                        sb.append(MemberActivity.this.mCableMode ? "單C" : "非單C");
                        Toast.makeText(baseContext, sb.toString(), 1).show();
                        Looper.loop();
                    }
                }.start();
                Intent intent = MemberActivity.this.getIntent();
                MemberActivity.this.finish();
                MemberActivity.this.startActivity(intent);
            }
        }
    }

}
