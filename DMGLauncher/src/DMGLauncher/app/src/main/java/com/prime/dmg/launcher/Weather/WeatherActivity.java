package com.prime.dmg.launcher.Weather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.utils.TVMessage;
import com.rd.PageIndicatorView;

public class WeatherActivity extends BaseActivity {
    private final static String TAG = "WeatherActivity";
    private final static int KEYCODE_ASSIST = 219;

    private ViewPager g_weather_view_pager;
    private PageIndicatorView g_weather_indicator;
    private GuidePageAdapter g_guide_page_adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        g_weather_view_pager = findViewById(R.id.lo_weather_viewpager);
        g_weather_view_pager.setPageMargin(35);

        g_weather_indicator = findViewById(R.id.lo_weather_indicator);
        g_guide_page_adapter = new GuidePageAdapter();
        g_weather_view_pager.setAdapter(g_guide_page_adapter);
        g_weather_indicator.setViewPager(this.g_weather_view_pager);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    public class GuidePageAdapter extends PagerAdapter {
        private static final int PAGE_COUNT = 3;

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return object == view;
        }

        private GuidePageAdapter() {}

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int imageResource;
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.item_weather_guide, (ViewGroup) null, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.describeImage = (ImageView) relativeLayout.findViewById(R.id.lo_weather_imgv_guide_pic);
            viewHolder.actionBtn = (Button) relativeLayout.findViewById(R.id.lo_weather_btn_guide_action);
            if (position == 1) {
                imageResource = R.mipmap.weather_hint_p2;
                viewHolder.actionBtn.setVisibility(View.GONE);
                viewHolder.describeImage.setFocusable(true);
            } else if (position == 2) {
                imageResource = R.mipmap.weather_hint_p3;
                viewHolder.actionBtn.setVisibility(View.VISIBLE);
                viewHolder.describeImage.setFocusable(false);
                viewHolder.actionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.input_keycode(KEYCODE_ASSIST);
                        finish();
                    }
                });
            } else {
                imageResource = R.mipmap.weather_hint_p1;
                viewHolder.actionBtn.setVisibility(View.GONE);
                viewHolder.describeImage.setFocusable(true);
            }
            viewHolder.describeImage.setImageResource(imageResource);
            relativeLayout.setTag(viewHolder);
            container.addView(relativeLayout);
            return relativeLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private static class ViewHolder {
        public Button actionBtn;
        public ImageView describeImage;

        private ViewHolder() {
        }
    }
}
