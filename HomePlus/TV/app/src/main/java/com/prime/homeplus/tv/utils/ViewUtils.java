package com.prime.homeplus.tv.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.media.tv.TvContentRating;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.prime.homeplus.tv.R;

public class ViewUtils {
    private static final String TAG = "ViewUtils";

    public static void bindToggleButtonToCheckBox(Button button, CheckBox checkBox) {
        button.setOnClickListener(v -> checkBox.toggle());
    }

    public static void applyButtonFocusTextEffect(@NonNull final Button button,
                                            float focusSizeSp,
                                            float unfocusSizeSp,
                                            boolean boldOnFocus) {
        if (button == null) {
            return;
        }

        try {
            button.setOnFocusChangeListener((v, hasFocus) -> {
                try {
                    float size = hasFocus ? focusSizeSp : unfocusSizeSp;
                    int style = (hasFocus && boldOnFocus) ? Typeface.BOLD : Typeface.NORMAL;

                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                    button.setTypeface(null, style);
                } catch (Exception e) {
                    Log.e(TAG, "applyFocusTextEffect failed inside listener", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "applyFocusTextEffect failed", e);
        }
    }

    public static void applyRadioButtonFocusTextEffect(@NonNull final RadioButton rButton,
                                                  float focusSizeSp,
                                                  float unfocusSizeSp,
                                                  boolean boldOnFocus) {
        if (rButton == null) {
            return;
        }

        try {
            rButton.setOnFocusChangeListener((v, hasFocus) -> {
                try {
                    float size = hasFocus ? focusSizeSp : unfocusSizeSp;
                    int style = (hasFocus && boldOnFocus) ? Typeface.BOLD : Typeface.NORMAL;

                    rButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                    rButton.setTypeface(null, style);
                } catch (Exception e) {
                    Log.e(TAG, "applyFocusTextEffect failed inside listener", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "applyFocusTextEffect failed", e);
        }
    }

    public static void applyTextViewTextEffect(@NonNull final TextView textView,
                                                  float sizeSp,
                                                  int color,
                                                  int typeface) {
        if (textView == null) {
            return;
        }

        try {
            if (sizeSp != -1) {
                textView.setTextSize(sizeSp);
            }

            if (color != -1) {
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), color));
            }

            if (typeface != -1) {
                textView.setTypeface(null, typeface);
            }
        } catch (Exception e) {
            Log.e(TAG, "applyFocusTextEffect failed", e);
        }
    }

    // TODO: XXX
    public static void setProgressWithMillisMax(ProgressBar pb, long startUtc, long endUtc) {
        if (pb == null || startUtc <= 0 || endUtc <= startUtc) {
            if (pb != null) {
                pb.setMax(0);
                pb.setProgress(0);
            }
            return;
        }

        long duration = endUtc - startUtc;
        if (duration > Integer.MAX_VALUE) {
            pb.setMax(100);
            pb.setProgress(0);
            return;
        }

        int max = (int) duration;
        long currentUtc = System.currentTimeMillis();

        long played = currentUtc - startUtc;
        if (played < 0) played = 0;
        if (played > duration) played = duration;

        int progress = (int) played;

        pb.setMax(max);
        pb.setProgress(progress);
    }

    public static int calculateProgress(long startMs, long endMs, long currentMs, int max) {
        if (startMs < 0 || endMs <= startMs || max <= 0) {
            return 0;
        }

        long duration = endMs - startMs;
        long played = currentMs - startMs;

        if (played < 0) played = 0;
        if (played > duration) played = duration;

        float ratio = (float) played / (float) duration;
        return (int) (ratio * max);
    }

    public static void setQualityIcon(ImageView iv, String resolutionLabel) {
        if ("FullHD".equals(resolutionLabel)) {
            iv.setImageResource(R.drawable.icon_miniguide_hd);
            iv.setVisibility(View.VISIBLE);
        } else if ("4K".equals(resolutionLabel)) {
            iv.setImageResource(R.drawable.icon_miniguide_4_k);
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.GONE);
        }
    }

    public static void setRatingIcon(ImageView iv, TvContentRating[] programRatingSet) {
        int ratingAge = ProgramRatingUtils.getNowRating(programRatingSet);
        if (ratingAge == 0) {
            iv.setVisibility(View.GONE);
        } else {
            iv.setImageResource(ProgramRatingUtils.getRatingIcon(ratingAge));
            iv.setVisibility(View.VISIBLE);
        }
    }

    public static void showToast(Context context, int duration_type, String msg) {
        Toast.makeText(context, msg, duration_type).show();
    }

    public static void dismissDialogIfExists(FragmentManager fm, String tag) {
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment instanceof DialogFragment) {
            DialogFragment dialog = (DialogFragment) fragment;
            if (dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                dialog.dismissAllowingStateLoss();
            }
        }
    }

    public static void animateViewWidth(View view, int toWidth, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredWidth(), toWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = val;
                view.setLayoutParams(layoutParams);
            }
        });

        anim.setDuration(duration);
        anim.start();
    }
}
