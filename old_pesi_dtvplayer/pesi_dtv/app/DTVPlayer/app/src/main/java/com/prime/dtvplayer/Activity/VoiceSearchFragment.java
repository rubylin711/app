package com.prime.dtvplayer.Activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.leanback.app.SearchFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.Presenter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.prime.dtvplayer.Activity.VoiceSearchActivity.ChannelDatabase;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.List;

public class VoiceSearchFragment extends SearchFragment implements SearchFragment.SearchResultProvider {
    private static final String TAG = "VoiceSearchFragment";
    private ArrayObjectAdapter mRowsAdapter;
    private Handler mHandler;
    private QueryRunnable mQueryRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mHandler = new Handler();
        mQueryRunnable = new QueryRunnable();
        setSearchResultProvider(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        /*View root = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout searchFrame = root.findViewById(R.id.lb_search_frame);
        FrameLayout resultsFrame = (FrameLayout) root.findViewById(R.id.lb_results_frame);
        SearchBar searchBar = searchFrame.findViewById(R.id.lb_search_bar);
        SpeechOrbView speechOrbView = searchBar.findViewById(R.id.lb_search_bar_speech_orb);
        ImageView imageView = searchBar.findViewById(R.id.lb_search_bar_badge);
        SearchEditText searchEditText = searchBar.findViewById(R.id.lb_search_text_editor);*/
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //mChannelList = VoiceSearchActivity.getChannelList();
        this.getContext();
    }

    @Override
    public ObjectAdapter getResultsAdapter()
    {
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String keyword)
    {
        Log.d(TAG, "onQueryTextChange: ");
        loadQuery(keyword);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String keyword)
    {
        Log.d(TAG, "onQueryTextSubmit: ");
        loadQuery(keyword);
        return true;
    }

    private void loadQuery(String keyword)
    {
        Log.d(TAG, "loadQuery: keyword = "+keyword);
        mRowsAdapter.clear();
        mHandler.removeCallbacks(mQueryRunnable);
        if (!TextUtils.isEmpty(keyword))
        {
            mQueryRunnable.setSearchText(keyword);
            mHandler.postDelayed(mQueryRunnable, 300);
        }
    }

    public static List<SimpleChannel> getResultList (Context context, String queryTxt)
    {
        long time = System.currentTimeMillis();
        List<SimpleChannel> resultList = new ArrayList<>();
        ChannelDatabase db = ChannelDatabase.getDatabase(context);
        Cursor c = db.getByFuzzy(queryTxt);

        while (c.moveToNext())
        {
            SimpleChannel channel = new SimpleChannel();
            channel.setChannelId(c.getLong(c.getColumnIndex(ChannelDatabase.KEY_CHANNEL_ID)));
            channel.setChannelNum(c.getInt(c.getColumnIndex(ChannelDatabase.KEY_CHANNEL_NUM)));
            channel.setChannelName(c.getString(c.getColumnIndex(ChannelDatabase.KEY_CHANNEL_NAME)));
            resultList.add(channel);
        }
        //Log.d(TAG, "getResultList: time="+((System.currentTimeMillis()-time)));
        //Log.d(TAG, "getResultList: time="+((System.currentTimeMillis()-time)/1000));
        return resultList;
    }

    private void queryText(String keyword)
    {
        Log.d(TAG, "queryText: keyword = "+keyword);
        List<SimpleChannel> resultList = getResultList(getContext(), keyword);

        ArrayObjectAdapter listAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (SimpleChannel channel : resultList)
        {
            Log.d(TAG, "queryText: channel = " + channel.getChannelName());
            listAdapter.add(channel);
        }

        if (listAdapter.size() > 0)
        {
            HeaderItem header = new HeaderItem(0, "Search Result" + " '" + keyword + "'");
            mRowsAdapter.add(new ListRow(header, listAdapter));
        }
    }

    private class QueryRunnable implements java.lang.Runnable {
        private String searchText;

        @Override
        public void run()
        {
            queryText(searchText);
        }

        void setSearchText (String text)
        {
            searchText = text;
        }
    }

    private static class CardPresenter extends Presenter {
        int CARD_WIDTH = 313;
        int CARD_HEIGHT = 176;
        int sSelectedBackgroundColor;
        int sDefaultBackgroundColor;
        Drawable mDefaultCardImage;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            //Log.d(TAG, "onCreateViewHolder: ");
            int infoColor = parent.getResources().getColor(android.R.color.transparent, null);
            sDefaultBackgroundColor = parent.getResources().getColor(R.color.colorBlack);
            sSelectedBackgroundColor = parent.getResources().getColor(R.color.colorPrimaryDark);
            mDefaultCardImage = parent.getResources().getDrawable(R.drawable.banner);
            ImageCardView cardView = new ImageCardView(parent.getContext()) {
                @Override
                public void setSelected(boolean selected) {
                    //updateCardBackgroundColor(this, selected);
                    super.setSelected(selected);
                }
            };
            cardView.findViewById(R.id.info_field).setBackgroundColor(infoColor);
            cardView.setCardType(BaseCardView.CARD_TYPE_INFO_OVER);
            cardView.setFocusable(true);
            cardView.setFocusableInTouchMode(true);
            //updateCardBackgroundColor(cardView, false);
            return new ViewHolder(cardView);
        }

        private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
            //Log.d(TAG, "updateCardBackgroundColor: ");
            int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
            view.setBackgroundColor(color);
            view.findViewById(R.id.info_field).setBackgroundColor(color);
        }

        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
            //Log.d(TAG, "onBindViewHolder: ");
            SimpleChannel channel = (SimpleChannel) item;
            ImageCardView cardView = (ImageCardView) viewHolder.view;

            if (channel.getChannelName() != null) {
                cardView.setTitleText(String.valueOf(channel.getChannelNum()));
                cardView.setContentText(channel.getChannelName());
                cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
                Glide.with(viewHolder.view.getContext())
                        .load(mDefaultCardImage)
                        .into(cardView.getMainImageView());
            }
        }

        @Override
        public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
            ImageCardView cardView = (ImageCardView) viewHolder.view;
            cardView.setBadgeImage(null);
            cardView.setMainImage(null);
        }
    }
}