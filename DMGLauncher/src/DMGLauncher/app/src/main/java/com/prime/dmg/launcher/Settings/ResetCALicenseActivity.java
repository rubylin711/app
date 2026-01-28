package com.prime.dmg.launcher.Settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.CasRefreshHelper;
import com.prime.dtv.PrimeDtv;

import java.util.List;

public class ResetCALicenseActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            GuidedStepSupportFragment.addAsRoot(this, new ResetCALicenseFragment(), android.R.id.content);
        }
    }

    public static class ResetCALicenseFragment extends GuidedStepSupportFragment {

        @Override
        public int onProvideTheme() {
            return androidx.leanback.R.style.Theme_Leanback_GuidedStep;
        }

        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.tv_nagra_license_reset_title);
//            String description = getString(R.string.tv_nagra_reset_info);
            String description = getString(R.string.tv_wvcas_license_reset_info);
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
                Toast.makeText(getContext(), getString(R.string.watting_nagra_license_reset), Toast.LENGTH_LONG).show();

                // clear cas data and casData.json first because handleCasRefresh won't do it
                CasRefreshHelper casRefreshHelper = CasRefreshHelper.get_instance();
                casRefreshHelper.clear_cas_data();

                // call handleCasRefresh() with deleteFlag=1
                // to request new cas data, clear license mapping and offline licenses
                PrimeDtv prime_dtv = HomeApplication.get_prime_dtv();
                prime_dtv.handleCasRefresh(1, 0, 0); // deleteFlag = 1

                getActivity().finish();
            } else if (action.getId() == 2) {
                getActivity().finish();
            }
        }
    }
}
