package com.prime.primetvinputapp.Scan;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.prime.primetvinputapp.R;
import com.prime.primetvinputapp.Scan.Cable.ChannelPreScanningActivity;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class SelectQamFragment extends DialogFragment {
    private final static String TAG = "SelectQamFragment";

    private final WeakReference<AppCompatActivity> g_ref;
    private View.OnClickListener g_on_click_listener;

    public SelectQamFragment(AppCompatActivity activity, View.OnClickListener onClickListener) {
        g_ref = new WeakReference<>(activity);
        g_on_click_listener = onClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // full screen
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

        // without the title
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // remove background shadow
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // custom layout
        View rootView = inflater.inflate(R.layout.fragment_select_qam,
                container, false);
        init_view(rootView);
        return rootView;
    }

    public void show() {
        super.show(get().getSupportFragmentManager(), TAG);
    }

    public ChannelPreScanningActivity get() {
        return (ChannelPreScanningActivity) g_ref.get();
    }

    private void init_view(View rootView) {
        TextView qam256Txv  = rootView.findViewById(R.id.lo_select_qam_textv_256);
        TextView qam128Txv  = rootView.findViewById(R.id.lo_select_qam_textv_128);
        TextView qam64Txv  = rootView.findViewById(R.id.lo_select_qam_textv_64);
        TextView qam32Txv  = rootView.findViewById(R.id.lo_select_qam_textv_32);
        TextView qam16Txv  = rootView.findViewById(R.id.lo_select_qam_textv_16);

        qam256Txv.setOnClickListener(g_on_click_listener);
        qam128Txv.setOnClickListener(g_on_click_listener);
        qam64Txv.setOnClickListener(g_on_click_listener);
        qam32Txv.setOnClickListener(g_on_click_listener);
        qam16Txv.setOnClickListener(g_on_click_listener);
    }
}
