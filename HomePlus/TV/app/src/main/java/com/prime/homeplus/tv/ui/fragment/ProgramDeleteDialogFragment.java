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
import androidx.fragment.app.DialogFragment;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.utils.ViewUtils;

public class ProgramDeleteDialogFragment extends DialogFragment {
    private static final String TAG = "ProgramDeleteDialogFragment";

    private TextView tvProgramDeleteMsg, tvProgramDeleteChannelNumber,
            tvProgramDeleteChannelName, tvProgramDeleteProgramName,
            tvProgramDeleteDate, tvProgramDeleteSchedule;
    private Button btnProgramDelete, btnProgramDeleteAll, btnReminderCancelDelete;

    private OnProgramDeleteListener programDeleteListener;

    long programId;
    String deleteMsg, channelNumber, channelName, programName, date, startEndTime;

    public interface OnProgramDeleteListener {
        void onProgramDelete(long programId);
        void onProgramDeleteAll();
    }

    public ProgramDeleteDialogFragment(long programId, String deleteMsg, String channelNumber,
                                       String channelName, String programName, String date,
                                       String startEndTime) {
        this.programId = programId;
        this.deleteMsg = deleteMsg;
        this.channelNumber = channelNumber;
        this.channelName = channelName;
        this.programName = programName;
        this.date = date;
        this.startEndTime = startEndTime;
    }

    public void setProgramDeleteListener(OnProgramDeleteListener listener) {
        this.programDeleteListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_program_delete, container, false);

        tvProgramDeleteMsg = view.findViewById(R.id.tvProgramDeleteMsg);
        tvProgramDeleteChannelNumber = view.findViewById(R.id.tvProgramDeleteChannelNumber);
        tvProgramDeleteChannelName = view.findViewById(R.id.tvProgramDeleteChannelName);
        tvProgramDeleteProgramName = view.findViewById(R.id.tvProgramDeleteProgramName);
        tvProgramDeleteDate = view.findViewById(R.id.tvProgramDeleteDate);
        tvProgramDeleteSchedule = view.findViewById(R.id.tvProgramDeleteSchedule);

        btnProgramDelete = view.findViewById(R.id.btnProgramDelete);
        btnProgramDeleteAll = view.findViewById(R.id.btnProgramDeleteAll);
        btnReminderCancelDelete = view.findViewById(R.id.btnReminderCancelDelete);

        tvProgramDeleteMsg.setText(deleteMsg);
        tvProgramDeleteChannelNumber.setText(channelNumber);
        tvProgramDeleteChannelName.setText(channelName);
        tvProgramDeleteProgramName.setText(programName);
        tvProgramDeleteDate.setText(date);
        tvProgramDeleteSchedule.setText(startEndTime);

        ViewUtils.applyButtonFocusTextEffect(btnProgramDelete, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnProgramDeleteAll, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnReminderCancelDelete, 17, 16, true);

        btnProgramDelete.setOnClickListener((v) -> {
            if (programDeleteListener != null) {
                programDeleteListener.onProgramDelete(programId);
            }
        });

        btnProgramDelete.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                    keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                    keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                btnProgramDeleteAll.requestFocus();
                return true;
            }

            return false;
        });

        btnProgramDeleteAll.setOnClickListener((v) -> {
            if (programDeleteListener != null) {
                programDeleteListener.onProgramDeleteAll();
            }
        });

        btnProgramDeleteAll.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                    keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                btnProgramDelete.requestFocus();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                btnReminderCancelDelete.requestFocus();
                return true;
            }

            return false;
        });

        btnReminderCancelDelete.setOnClickListener((v) -> {
            dismiss();
        });

        btnReminderCancelDelete.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                    keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                    keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                btnProgramDeleteAll.requestFocus();
                return true;
            }

            return false;
        });

        return view;
    }
}