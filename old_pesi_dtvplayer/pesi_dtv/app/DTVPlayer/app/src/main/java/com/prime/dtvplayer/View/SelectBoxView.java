package com.prime.dtvplayer.View;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.dtvplayer.R;

import java.lang.reflect.Field;


/**
 * Created by gary_hsu on 2017/10/24.
 */

public class SelectBoxView {
    private static final String TAG = "SelectBoxView";
    private Context Context;
    private Spinner Spinner;
    private ArrayAdapter<String> Adapter;
    private int TextViewResourceId; // 選項樣式
    private int TextDropViewResourceId; // 下拉選單樣式

    public static class SelectBoxOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void SelectBoxOnItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            SelectBoxOnItemSelected(parent,view,position,id);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public static class SelectBoxtOnFocusChangeListener implements View.OnFocusChangeListener {

        public void SelectBoxOnFocusChange(View v, boolean hasFocus) {
            TextView txt = (TextView) v.findViewById(R.id.view_text1);
            if (txt == null )
            {
                return;
            }

            if (hasFocus)
                txt.setTextColor(Color.BLACK);
            else
                txt.setTextColor(Color.WHITE);
        }
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            SelectBoxOnFocusChange(v,hasFocus);
        }
    }
    /* 預設selectbox的樣式的建構子 */
    public SelectBoxView(Context context, Spinner spinner, String[] mItems) {
        Context = context;
        TextViewResourceId = R.layout.selectbox_layout;
        TextDropViewResourceId = R.layout.selectbox_dropdown_layout;
        Spinner = spinner;

        //設定可以成為焦點
        Spinner.setFocusable(true);
        Spinner.setFocusableInTouchMode(true);
        Spinner.setBackgroundResource(R.drawable.selectbox);
//        Spinner.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS); // Johnny 20181219 for mouse control
        Spinner.setPopupBackgroundResource(/*R.drawable.banner*/R.drawable.full_black_background);

        //設adapter給spinner
        NewAdapter(Context, TextViewResourceId, mItems, Spinner);
    }

    private void NewAdapter(Context context, int textViewResourceId, String[] mItems, Spinner spinner) {
        Adapter = new SelectBoxAdapter(context,textViewResourceId, mItems,spinner);
        Adapter.setDropDownViewResource(R.layout.selectbox_dropdown_adapter_view);
        Spinner.setAdapter(Adapter);
    }

    private class SelectBoxAdapter extends ArrayAdapter<String> {

        private LayoutInflater LayInf;
        private Spinner Spinner;

        SelectBoxAdapter(final Context context, int textViewResourceId, String[] mItems, final Spinner spinner) {
            super(context, textViewResourceId, mItems);
            LayInf = LayoutInflater.from(context);
            Spinner = spinner;

            Log.d(TAG, "onKey: Spinner = "+Spinner.getId());

            //設定監聽按鍵
            Spinner.setOnKeyListener(new View.OnKeyListener(){
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    Log.d(TAG, "======== onKey ========");
                    int pos = Spinner.getSelectedItemPosition();
                    TextView spinnerTxv = (TextView) Spinner
                            .getSelectedView().findViewById(R.id.view_text1);

                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        Log.d(TAG, "onKey: press key");
                        switch(keyCode) {
                            case KeyEvent.KEYCODE_DPAD_LEFT:
                                if(pos > 0)
                                    SetSelectedItemIndex(pos-1);
                                else
                                    SetSelectedItemIndex(Spinner.getCount()-1);
                                return true;
                            case KeyEvent.KEYCODE_DPAD_RIGHT:
                                if(pos >= (Spinner.getCount()-1))
                                    SetSelectedItemIndex(0);
                                else
                                    SetSelectedItemIndex(pos+1);
                                return true;
                        }
                    }

                    return false;
                }
            });

            //加這個監聽事件是因為按左右鍵的時候,元件focus狀態會跑掉,只有透過這個做到focus的時候樣式不一樣
            Spinner.setOnItemSelectedListener(new SelectBoxOnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    super.onItemSelected(adapterView,view,i,l);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.d(TAG, "onNothingSelected: ");
                }
            });

            //設定監聽焦點狀態
            Spinner.setOnFocusChangeListener(new SelectBoxtOnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.d(TAG, "onFocusChange: ");
                    super.onFocusChange(v,hasFocus);
                }
            });

            //設定下拉選單樣式
            Spinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.d(TAG, "onItemSelected: ");
                    int row = 5;
                    int spinnerHeight = (
                            (int) Context
                            .getResources()
                            .getDimension(R.dimen.VIEW_HEIGHT)
                    );
                    Log.d(TAG, "onItemSelected: spinner height = "+spinnerHeight);

                    if (Spinner.getCount() >= row) {
                        SetDropDownSize(row, spinnerHeight);
                    } else {
                        SetDropDownSize(Spinner.getCount(), spinnerHeight);
                    }

                    //  drop down background
                    Spinner.setPopupBackgroundResource(/*R.drawable.banner*/R.drawable.full_black_background);
                    Spinner.setDropDownVerticalOffset(spinnerHeight);
                    Spinner.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) //畫選項的時候
        {
            Log.d(TAG, "getView: ");
            //透過inflate從 選項 layout(XML) 拿layout view
            View v = LayInf.inflate(TextViewResourceId, parent, false);
            Log.d(TAG, "getView: v.getHeight() = "+v.getHeight());
            //Toast.makeText(getContext(), TAG + " v.getHeight()" + v.getHeight(), Toast.LENGTH_SHORT).show();
            //從layout view拿到text view
            TextView txt = (TextView) v.findViewById(R.id.view_text1);

            //從adapter position拿字串,並設到text view上
            txt.setText(getItem(position));
            Log.d(TAG, "getView: getItem = "+getItem(position));
            boolean focus = Spinner.isFocused();
            if (focus)
                txt.setTextColor(Color.BLACK);
            else
                txt.setTextColor(Color.WHITE);

            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) //畫下拉選單單一項目的時候
        {
            Log.d(TAG, "getDropDownView: ");
            //透過inflate從 下拉選單 layout(XML) 拿layout view
            View v = LayInf.inflate(TextDropViewResourceId, parent, false);
            //從layout view拿到text view
            TextView txt = (TextView) v.findViewById(R.id.dropview_text1);

            //從adapter position拿字串,並設到text view上
            txt.setText(getItem(position));
            Log.d(TAG, "getDropDownView: getItem = "+getItem(position));

            return v;
        }
    }

    public void SetSelectedItemIndex(int index) {
        Log.d(TAG, "SetSelectedItemIndex: index = "+index);
        View view = Spinner.getSelectedView();
        Spinner.setSelection(index);
    }

    public int GetSelectedItemIndex() {
        Log.d(TAG, "GetSelectedItemIndex: ");
        Log.i(TAG ,"get selection index "+ Spinner.getSelectedItemPosition());
        return Spinner.getSelectedItemPosition();
    }

    public Object GetSelectedItem() {
        Log.d(TAG, "GetSelectedItem: ");
        return Spinner.getSelectedItem();
    }

    public void SetNewAdapterItems(String[] mItems) {
        Log.d(TAG, "SetNewAdapterItems: ");
        NewAdapter(Context, TextViewResourceId, mItems, Spinner);
    }

    private void SetDropDownSize(final int row, final int spinnerHeight) {
        Log.d(TAG, "SetDropDownHeight: short drop down to "+row);

        Field popup = null;
        try {
            popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = null;
            try {
                popupWindow = (android.widget.ListPopupWindow) popup.get(Spinner);
            } catch (NoClassDefFoundError | ClassCastException | IllegalAccessException e) {
                e.printStackTrace();
            }

            //設定下拉選單高度
            if (popupWindow != null) {
                Log.d(TAG, "SetDropDownSize: row = "+row);
                Log.d(TAG, "SetDropDownSize: spinnerHeight = "+spinnerHeight);
                popupWindow.setHeight((row * spinnerHeight));
            }
            else
                Log.d(TAG, "SetDropDownSize: popup window = null");

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}