package com.prime.dmg.launcher.Home.Recommend.Pager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.prime.dmg.launcher.Home.Recommend.Pager.PagerManager.TransparentTransform;
import com.prime.dmg.launcher.Utils.JsonParser.AdPage;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentStateAdapter {

    String TAG = getClass().getSimpleName();
    List<PagerFragment> fragmentList = new ArrayList<>();

    public PagerAdapter(PagerManager pagerManager, List<AdPage> adPageList, TransparentTransform transform) {
        super(pagerManager.get());
        for (AdPage adPage : adPageList)
            fragmentList.add(new PagerFragment(pagerManager, adPage, transform));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        //Log.d(TAG, "createFragment: adapter create fragment " + position);
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public PagerFragment get_fragment(int pos) {
        if (pos >= fragmentList.size())
            return null;
        return fragmentList.get(pos);
    }
}
