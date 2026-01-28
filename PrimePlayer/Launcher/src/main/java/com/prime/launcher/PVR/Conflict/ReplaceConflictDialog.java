package com.prime.launcher.PVR.Conflict;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.BaseDialog;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection FieldCanBeLocal*/
public class ReplaceConflictDialog extends BaseDialog {
    private static final String TAG = ReplaceConflictDialog.class.getSimpleName();

    private final WeakReference<AppCompatActivity> RefAct;
    private final WeakReference<ConflictDialog> RefDlg;
    private final PrimeDtv PrimeDtv;
    private final BookInfo NewBook;
    private final List<BookInfo> SelectedList;

    public ReplaceConflictDialog(@NonNull AppCompatActivity activity, ConflictDialog dialog, BookInfo newBook, List<BookInfo> selectedList) {
        super(activity, R.style.Theme_Launcher_DialogFullScreen);
        RefAct = new WeakReference<>(activity);
        RefDlg = new WeakReference<>(dialog);
        PrimeDtv = HomeApplication.get_prime_dtv();
        NewBook = newBook;
        SelectedList = new ArrayList<>(selectedList);
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.drawable.semi_transparent);
            getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
        }
        setContentView(R.layout.dialog_replace_conflict);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // display selected list
        SelectedListView selectedListView = findViewById(R.id.SelectedListView);
        selectedListView.setup_view(this, NewBook, SelectedList);

        // set Go Back button
        TextView goBackButton = findViewById(R.id.GoBackButton);
        goBackButton.setOnClickListener(this::on_click_go_back);

        // set Confirm button
        TextView confirmButton = findViewById(R.id.ConfirmButton);
        confirmButton.setOnClickListener(this::on_click_confirm);
    }

    public void on_click_go_back(View view) {
        Log.d(TAG, "on_click_go_back: close dialog");
        dismiss();
    }

    public void on_click_confirm(View view) {
        int radioBoxPosition = get_radio_box_position();

        if (radioBoxPosition == -1)
            Log.e(TAG, "on_click_confirm: radio box position is -1");
        else {
            ConflictDialog conflictDialog = get_dlg_conflict();
            BookInfo oldBook = SelectedList.get(radioBoxPosition);
            if (conflictDialog != null)
                conflictDialog.on_replace_book(oldBook, NewBook);
        }
        dismiss();
    }

    public ConflictDialog get_dlg_conflict() {
        return RefDlg.get();
    }

    public int get_radio_box_position() {
        SelectedListView selectedListView = findViewById(R.id.SelectedListView);
        SelectedListAdapter adapter = selectedListView.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "get_radio_box_position: adapter is null");
            return -1;
        }
        return adapter.getRadioBoxPosition();
    }
}
