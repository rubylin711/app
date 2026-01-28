package com.prime.launcher.OTA;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.LogUtils;

public class OtaUserCheckDialogFragment extends DialogFragment {
    private TextView g_content_textview;
    private Button g_button_more_action;
    private Button g_button_update;
    private OtaParam g_ota_param;
    private static OtaUserCheckDialogFragment g_Fragment = null ;
    public static OtaUserCheckDialogFragment newInstance() {
        return new OtaUserCheckDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_BACK )
                    return true ;
                return false ;
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_ota_notify, container, false);
        g_ota_param = OtaParam.get_instance();
        g_content_textview = view.findViewById(R.id.txt_ota_content);
        g_content_textview.setText(getString(R.string.ota_content_message) + "(" + g_ota_param.getOta_version() + ")");
        g_button_update = view.findViewById(R.id.bn_ota_update);
        g_button_update.setText(getString(R.string.ota_update_now));
        g_button_more_action = view.findViewById(R.id.bn_ota_more_option);
        g_button_more_action.setText(getString(R.string.ota_more_options));

        LogUtils.d(" "+g_ota_param.toString());
        g_button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                OtaUtils.start_Ota_Reminder(g_Fragment.getContext(),null);
                HomeApplication.get_prime_dtv().gpos_info_update_by_key_string(GposInfo.GPOS_OTA_MD5, "0");
                HomeApplication.get_prime_dtv().saveGposKeyValue(GposInfo.GPOS_OTA_MD5, "0");
                SystemProperties.set("persist.sys.prime.ota_download_status", "0");
                SystemProperties.set("persist.sys.prime.ota_last_download_version", "");
                SystemProperties.set("persist.sys.prime.zipfile.download.status", "0");
                OtaUtils.send_Start_Ota(g_Fragment.getContext());
            }
        });

        g_button_more_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Intent intent = new Intent(getActivity(), OtaUserSelectTimeActivity.class);
                HomeApplication.get_prime_dtv().gpos_info_update_by_key_string(GposInfo.GPOS_OTA_MD5, g_ota_param.getMd5_checksum());
                HomeApplication.get_prime_dtv().saveGposKeyValue(GposInfo.GPOS_OTA_MD5, g_ota_param.getMd5_checksum());
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
            layoutParams.x = (int) (20 * getResources().getDisplayMetrics().density);
            layoutParams.y = (int) (40 * getResources().getDisplayMetrics().density);
            window.setAttributes(layoutParams);
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    public static void showOtaUserCheckDialog(FragmentManager fragmentManager) {
        if ( !OtaUserSelectTimeActivity.g_user_select_time_activity_is_showing )
        {
            if (g_Fragment != null)
                g_Fragment.dismiss();
            g_Fragment = OtaUserCheckDialogFragment.newInstance();
            g_Fragment.show(fragmentManager, OtaUserCheckDialogFragment.class.getSimpleName());
        }
    }
}
