package com.prime.homeplus.tv.ui.fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.utils.ViewUtils;

public class PvrPlayerDialogFragment extends DialogFragment {
    private static final String TAG = "PvrPlayerDialogFragment";

    public enum PvrPlayerDialogState {
        LAST_POSITION,
        END_PLAYBACK,
        STOP_PLAYBACK
    }

    private PvrPlayerDialogState pvrPlayerDialogState;
    private ConstraintLayout clPvrPlayerLastPosition;
    private Button btnPvrPlayerContinue, btnPvrPlayerFromStart;

    private ConstraintLayout clPvrPlayerEndPlayback;
    private Button btnPvrPlayerExit, btnPvrPlayerReplay;

    private ConstraintLayout clPvrPlayerStopPlayback;
    private Button btnPvrPlayerConfirmStop, btnPvrPlayerCancel;

    private OnPvrPlayerListener pvrPlayerListener;

    public interface OnPvrPlayerListener {
        void onPvrPlayerContinue();
        void onPvrPlayerFromStart();
        void onPvrPlayerExit();
        void onPvrPlayerReplay();
        void onPvrPlayerConfirmStop();
        void onPvrPlayerCancel();
    }

    public PvrPlayerDialogFragment(PvrPlayerDialogState state) {
        pvrPlayerDialogState = state;
    }

    public void setPvrPlayerListener(OnPvrPlayerListener listener) {
        this.pvrPlayerListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_pvr_player, container, false);

        switch (pvrPlayerDialogState) {
            case LAST_POSITION:
                initLastPositionViews(view);
                clPvrPlayerLastPosition.setVisibility(View.VISIBLE);
                break;
            case END_PLAYBACK:
                initEndPlaybackViews(view);
                clPvrPlayerEndPlayback.setVisibility(View.VISIBLE);
                break;
            case STOP_PLAYBACK:
                initStopPlaybackViews(view);
                clPvrPlayerStopPlayback.setVisibility(View.VISIBLE);
                break;
        }

        return view;
    }

    private void initLastPositionViews(View view) {
        clPvrPlayerLastPosition = view.findViewById(R.id.clPvrPlayerLastPosition);
        btnPvrPlayerContinue = view.findViewById(R.id.btnPvrPlayerContinue);
        btnPvrPlayerFromStart = view.findViewById(R.id.btnPvrPlayerFromStart);
        ViewUtils.applyButtonFocusTextEffect(btnPvrPlayerContinue, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnPvrPlayerFromStart, 17, 16, true);
        btnPvrPlayerContinue.setOnClickListener((v) -> {
            if (pvrPlayerListener != null) {
                pvrPlayerListener.onPvrPlayerContinue();
            }
        });

        btnPvrPlayerContinue.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    btnPvrPlayerFromStart.requestFocus();
                    return true;
                }
            }
            return false;
        });

        btnPvrPlayerFromStart.setOnClickListener((v) -> {
            if (pvrPlayerListener != null) {
                pvrPlayerListener.onPvrPlayerFromStart();
            }
        });

        btnPvrPlayerFromStart.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    btnPvrPlayerContinue.requestFocus();
                    return true;
                }
            }
            return false;
        });
    }

    private void initEndPlaybackViews(View view) {
        clPvrPlayerEndPlayback = view.findViewById(R.id.clPvrPlayerEndPlayback);
        btnPvrPlayerExit = view.findViewById(R.id.btnPvrPlayerExit);
        btnPvrPlayerReplay = view.findViewById(R.id.btnPvrPlayerReplay);
        ViewUtils.applyButtonFocusTextEffect(btnPvrPlayerExit, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnPvrPlayerReplay, 17, 16, true);
        btnPvrPlayerExit.setOnClickListener((v) -> {
            if (pvrPlayerListener != null) {
                pvrPlayerListener.onPvrPlayerExit();
            }
        });

        btnPvrPlayerExit.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    btnPvrPlayerReplay.requestFocus();
                    return true;
                }
            }
            return false;
        });

        btnPvrPlayerReplay.setOnClickListener((v) -> {
            if (pvrPlayerListener != null) {
                pvrPlayerListener.onPvrPlayerReplay();
            }
        });

        btnPvrPlayerReplay.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    btnPvrPlayerExit.requestFocus();
                    return true;
                }
            }
            return false;
        });

    }

    private void initStopPlaybackViews(View view) {
        clPvrPlayerStopPlayback = view.findViewById(R.id.clPvrPlayerStopPlayback);
        btnPvrPlayerConfirmStop = view.findViewById(R.id.btnPvrPlayerConfirmStop);
        btnPvrPlayerCancel = view.findViewById(R.id.btnPvrPlayerCancel);
        ViewUtils.applyButtonFocusTextEffect(btnPvrPlayerConfirmStop, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnPvrPlayerCancel, 17, 16, true);
        btnPvrPlayerConfirmStop.setOnClickListener((v) -> {
            if (pvrPlayerListener != null) {
                pvrPlayerListener.onPvrPlayerConfirmStop();
            }
        });

        btnPvrPlayerConfirmStop.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    btnPvrPlayerCancel.requestFocus();
                    return true;
                }
            }
            return false;
        });

        btnPvrPlayerCancel.setOnClickListener((v) -> {
            if (pvrPlayerListener != null) {
                pvrPlayerListener.onPvrPlayerCancel();
            }
        });

        btnPvrPlayerCancel.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    btnPvrPlayerConfirmStop.requestFocus();
                    return true;
                }
            }
            return false;
        });
    }
}