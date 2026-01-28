package com.prime.launcher.Settings;


import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.prime.launcher.R;

import java.util.List;

public class ResetCAActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            GuidedStepSupportFragment.addAsRoot(this, new ResetCAFragment(), android.R.id.content);
        }
    }

    public static class ResetCAFragment extends GuidedStepSupportFragment {


        @Override
        public int onProvideTheme() {
            return androidx.leanback.R.style.Theme_Leanback_GuidedStep;
        }

        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.tv_nagra_reset_title);
            String description = getString(R.string.tv_nagra_reset_info);
            return new GuidanceStylist.Guidance(title, description, null, null);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            actions.add(new GuidedAction.Builder(getContext()).id(1).title(getString(R.string.tv_nagra_reset_ok)).build());
            actions.add(new GuidedAction.Builder(getContext()).id(2).title(getString(R.string.tv_nagra_reset_cancel)).build());
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == 1) {
                Toast.makeText(getContext(), getString(R.string.watting_nagra_reset), Toast.LENGTH_LONG).show();
                getActivity().finish();
            } else if (action.getId() == 2) {
                getActivity().finish();
            }
        }
    }
}
