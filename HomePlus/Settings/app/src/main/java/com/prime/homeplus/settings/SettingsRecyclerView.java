package com.prime.homeplus.settings;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsRecyclerView extends RecyclerView {
    private String TAG = "HomePlus-SettingsRecyclerView";

    private ArrayList<SettingsItemData> settingsAllItems = new ArrayList();

    public ArrayList<SettingsItemData> settingsItems = new ArrayList();
    private SettingsRecyclerViewAdapter viewAdapter;

    private LinearLayoutManager layoutManager;

    public SettingsRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }

    public SettingsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.layoutManager = new LinearLayoutManager(getContext()) {
            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView recyclerView, @NonNull View view, @NonNull Rect rect, boolean z) {
                return false;
            }

            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView recyclerView, @NonNull View view, @NonNull Rect rect, boolean z, boolean z2) {
                return false;
            }
        };
        setLayoutManager(this.layoutManager);

        viewAdapter = new SettingsRecyclerViewAdapter(this, settingsItems);
        setAdapter(viewAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.recycler_divider));
        addItemDecoration(divider);


    }

    public void addAllItem(ArrayList<SettingsItemData> settingsItems) {
        Log.d("HomePlus-", "addAllItem !!! ");

        this.settingsAllItems.clear();
        this.settingsAllItems.addAll(settingsItems);

        this.settingsItems.clear();
        this.settingsItems.addAll(settingsItems);
        Log.d("HomePlus-", "settingsItems size  " + settingsItems.size());
        notifyDataSetChanged();
    }

    public void focusItem(int i) {
        closeItemInList();

        settingsItems.get(i).setExpand(true);
        Log.d("HomePlus", settingsItems.get(i).getTitle());
        notifyDataSetChanged();

        ThirdLevelView view = (ThirdLevelView) settingsItems.get(i).getView();
        view.onFocus();
    }

    public void updatefocusItem(int i) {
        ThirdLevelView view = (ThirdLevelView) settingsItems.get(i).getView();
        view.onFocus();
    }

    public void handleAction(int i, String action) {
        ThirdLevelView view = (ThirdLevelView) settingsItems.get(i).getView();
        view.onHandleAction(action);
    }

    public void viewPaused(int i) {
        ThirdLevelView view = (ThirdLevelView) settingsItems.get(i).getView();
        view.onViewPaused();
    }

    public void viewResumed(int i) {
        ThirdLevelView view = (ThirdLevelView) settingsItems.get(i).getView();
        view.onViewResumed();
    }

    public void closeAllItem() {
        this.settingsItems.clear();
        this.settingsItems.addAll(settingsAllItems);

        for (int i = 0; i < settingsItems.size(); i++) {
            if (settingsItems.get(i).isExpand())
                settingsItems.get(i).setExpand(false);
        }
    }

    private void closeItemInList() {
        for (int i = 0; i < settingsItems.size(); i++) {
            if (settingsItems.get(i).isExpand())
                settingsItems.get(i).setExpand(false);
        }
    }

    public void notifyDataSetChanged() {
        viewAdapter.notifyDataSetChanged();
    }

    public void onItemSwitch(int i) {
        Log.d(TAG, "onItemSwitch " + i);

        requestFocus();
        this.focusItem(i);
    }

    private int viewIndex = 0;

    public int getViewIndex() {
        return viewIndex;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "RecyclerView keyCode " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            focusUp();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            focusDown();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (viewIndex == 7) {
                backToList();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void focusUp() {
        if (viewIndex == 0) {
            if (settingsAllItems.size() == settingsItems.size()) {
                return;
            }

            turnPage();
            viewIndex = settingsItems.size() - 1;
            onItemSwitch(viewIndex);
            return;
        }

        viewIndex = viewIndex - 1;
        if (viewIndex < 0) {
            viewIndex = 0;
        }
        onItemSwitch(viewIndex);
    }

    public void focusDown() {
        if (viewIndex == settingsItems.size() - 1) {
            if (settingsAllItems.size() == settingsItems.size()) {
                return;
            }

            turnPage();
            viewIndex = 0;
            onItemSwitch(viewIndex);
            return;
        }

        viewIndex = viewIndex + 1;
        if (viewIndex >= settingsItems.size()) {
            viewIndex = settingsItems.size() - 1;
        }
        onItemSwitch(viewIndex);
    }

    private int pageIndex = 0;

    private void turnPage() {
        if (pageIndex == 0) {
            pageIndex = 1;

            this.settingsItems.clear();
            for (int i = 6; i < settingsAllItems.size(); i++) {
                settingsItems.add(settingsAllItems.get(i));
            }


        } else {
            pageIndex = 0;

            this.settingsItems.clear();
            for (int i = 0; i < 6; i++) {
                settingsItems.add(settingsAllItems.get(i));
            }
        }

        focusItemListener.onFocus(pageIndex + 1);
    }

    public void backToList() {
        if (backToListListener == null) { //not call setBackToListListener(), e.g. SettingsLauncherActivity
            return;
        }
        backToListListener.onBack();
        viewIndex = 0;
        //viewAdapter.get

    }

    public void toView() {
        viewIndex = 0;
        pageIndex = 1;
        turnPage();
        onItemSwitch(0);
        //viewAdapter.get

    }

    public void toFirstView() {
        viewIndex = 0;
        pageIndex = 0;
        onItemSwitch(0);
        //viewAdapter.get

    }

    private BackToListListener backToListListener;

    public void setBackToListListener(BackToListListener backToListListener) {
        this.backToListListener = backToListListener;
    }

    private FocusItemListener focusItemListener;

    public void setFocusItemListener(FocusItemListener focusItemListener) {
        this.focusItemListener = focusItemListener;
    }

    private boolean mCableMode = true;
    public void setCableMode(boolean flag) {
        mCableMode = flag;
    }

    public boolean getCableMode() {
        return mCableMode;
    }

    public void focusItemByName(String itemName) {
        Log.d(this.TAG, "focusItemByName:" + itemName);
        int index = -1;
        int i = 0;
        while (true) {
            if (i > this.settingsAllItems.size()) {
                break;
            }
            if (!this.settingsAllItems.get(i).getTitle().equals(itemName)) {
                i++;
            } else {
                index = i;
                break;
            }
        }
        if (index != -1) {
            if (index >= this.settingsItems.size()) {
                this.viewIndex = index - this.settingsItems.size();
                turnPage();
                onItemSwitch(this.viewIndex);
                return;
            } else {
                this.viewIndex = index;
                onItemSwitch(this.viewIndex);
                return;
            }
        }
        Log.d(this.TAG, "item not found - " + itemName);
    }

}
