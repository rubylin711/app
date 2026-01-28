package com.prime.launcher.EPG;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.prime.datastructure.sysdata.EPGEvent;

public abstract class EpgContentView extends RelativeLayout {
    protected OnItemChangeListener g_on_item_change_listener;

    public EpgContentView(Context context) {
        super(context);
        init();
    }

    public EpgContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EpgContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    public interface OnItemChangeListener {
        void onFocusChange(int programId, long channelId, int serviceId, long category, EPGEvent epgEvent);
    }

    public void set_on_item_change_listener(OnItemChangeListener listener) {
        this.g_on_item_change_listener = listener;
    }
}
