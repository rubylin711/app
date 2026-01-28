package com.prime.dmg.launcher.CustomView;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.prime.dmg.launcher.R;

public class Snakebar {

    public static final int LENGTH_SHORT        = Snackbar.LENGTH_SHORT;
    public static final int LENGTH_LONG         = Snackbar.LENGTH_LONG;
    public static final int LENGTH_INDEFINITE   = Snackbar.LENGTH_INDEFINITE;

    public static void show(Context context, int resId, int duration) {
        show(context, context.getString(resId), duration);
    }

    public static void show(Context context, String message, int duration) {
        View rootView = ((Activity) context).getWindow().getDecorView().getRootView();
        show(rootView, message, duration);
    }

    public static void show(AppCompatActivity activity, String message, int duration) {
        View rootView = activity.getWindow().getDecorView().getRootView();
        show(rootView, message, duration);
    }

    public static void show(View view, int resId, int duration) {
        Context context = view.getContext();
        View rootView = ((Activity) context).getWindow().getDecorView().getRootView();
        show(rootView, context.getString(resId), duration);
    }

    public static void show(View rootView, String message, int duration) {

        if (rootView == null)
            return;

        Snackbar snackbar = Snackbar.make(rootView, message, duration);
        View snackbarView = snackbar.getView();
        Context context = rootView.getContext();

        // background
        snackbarView.setBackground(ContextCompat.getDrawable(context, R.drawable.custom_snackbar_background));

        // center text
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // center layout
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dp_to_px(context, 24);
        snackbarView.setLayoutParams(params);

        // show Snackbar
        snackbar.show();
    }

    /** @noinspection SameParameterValue*/
    private static int dp_to_px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
