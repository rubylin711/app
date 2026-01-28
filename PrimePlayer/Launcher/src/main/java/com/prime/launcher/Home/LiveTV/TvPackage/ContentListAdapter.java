package com.prime.launcher.Home.LiveTV.TvPackage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.prime.launcher.Home.LiveTV.LiveTvManager;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.Utils.JsonParser.RecommendContent;
import com.prime.launcher.Utils.JsonParser.RecommendPackage;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class ContentListAdapter extends RecyclerView.Adapter<ContentListAdapter.ViewHolder> {

    String TAG = getClass().getSimpleName();

    static final int MAX_CHANNEL_NUM_LENGTH  = 3;

    WeakReference<AppCompatActivity> g_ref;
    TvPackageDialog g_tvPackageDialog;
    List<RecommendContent> g_recommendContents;
    List<ProgramInfo> g_channels;

    public ContentListAdapter(TvPackageDialog tvPackageDialog, List<RecommendContent> recommendChannels) {
        g_ref = new WeakReference<>(tvPackageDialog.get());
        g_tvPackageDialog = tvPackageDialog;
        g_recommendContents = recommendChannels;
        g_channels = get().g_liveTvMgr.get_channels();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_recommend_package_content, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (g_tvPackageDialog.interrupt_bind_view())
            return;
        if (position == 0)
            set_content_selected(holder.itemView);
        else
            set_content_unselected(holder.itemView);
        set_channel_num(holder, position);
        set_channel_name(holder, position);
        set_channel_icon(holder, position);
        set_channel_poster(holder, position);
        set_channel_program(holder, position);
        on_click_channel(holder, position);
        holder.itemView.setTag(position);
    }

    void on_click_channel(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener((v) -> {
            List<RecommendPackage> packageList = g_tvPackageDialog.get_recommend_packages();
            RecommendPackage onePackage = packageList.get(get_type_position());

            Log.d(TAG, "on_click_channel: package " + onePackage.get_package_id() + " " + onePackage.get_package_name());
            open_purchase_page(onePackage);
            g_tvPackageDialog.dismiss();
        });
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_recommendContents.size();
    }

    public String get_service_id(int position) {
        RecommendContent content = get_content(position);
        if (content != null) {
            return get_content(position).get_service_id();
        }
        Log.e(TAG, "get_service_id: null serviceId");
        return null;
    }

    public RecommendContent get_content(int position) {
        if (position < g_recommendContents.size()) {
            return g_recommendContents.get(position);
        }
        Log.e(TAG, "get_content: position out of RecommendContents.size()");
        return null;
    }

    public ProgramInfo get_current_channel(int position) {
        String serviceId = get_service_id(position);
        return get().g_liveTvMgr.get_channel(serviceId);
    }

    public void set_channel_num(ViewHolder holder, int position) {
        ProgramInfo channel = get_current_channel(position);
        String channelNum = "";

        if (null != channel)
            channelNum = channel.getDisplayNum(MAX_CHANNEL_NUM_LENGTH);

        holder.numberView().setText(channelNum);

        if (channelNum.isEmpty())
            holder.numberView().setVisibility(View.GONE);
        else
            holder.numberView().setVisibility(View.VISIBLE);
    }

    public int get_type_position() {
        TypeListView typeListView = g_tvPackageDialog.findViewById(R.id.lo_tv_package_type_list);
        return typeListView.get_position();
    }

    public void set_channel_name(ViewHolder holder, int position) {
        ProgramInfo channel = get_current_channel(position);
        String channelName = "";

        if (channel != null)
            channelName = channel.getDisplayName();

        holder.titleView().setText(channelName);

        if (channelName.isEmpty())
            holder.titleView().setVisibility(View.GONE);
        else
            holder.titleView().setVisibility(View.VISIBLE);
    }

    public void set_channel_icon(ViewHolder holder, int position) {
        String serviceId = get_service_id(position);
        String iconUrl = LiveTvManager.get_channel_icon_url(get(), serviceId);
        int iconResId = LiveTvManager.get_channel_icon_res_id(get(), serviceId);
        Log.d(TAG, "set_channel_icon: " + iconUrl);
        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get()).load(iconUrl)
                    .error(iconResId)
                    .placeholder(iconResId)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            holder.iconView().setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            holder.iconView().setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(holder.iconView());
    }

    public void set_channel_poster(ViewHolder holder, int position) {
        RecommendContent content = get_content(position);
        String serviceId = get_service_id(position);
        String iconUrl = LiveTvManager.get_channel_icon_url(get(), serviceId);
        int iconResId = LiveTvManager.get_channel_icon_res_id(get(), serviceId);

        /*GlideUrl glideUrl = new GlideUrl(content.get_program_poster(),
                new LazyHeaders.Builder()
                        .addHeader("User-Agent", "Mozilla/5.0")
                        .build());*/

        if (!get().isFinishing() && !get().isDestroyed()) {
            RequestBuilder<Drawable> fallbackRequest = Glide.with(get()).load(iconUrl).error(iconResId);

            Glide.with(get()).load(content.get_program_poster())
                    .placeholder(iconResId)
                    .error(fallbackRequest)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            if (e != null)
                                Log.e(TAG, "onLoadFailed: " + e.getMessage());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    //.error(R.drawable.internet_error)
                    //.placeholder(R.drawable.default_photo)
                    .into(holder.posterView());
        }
    }

    public void set_channel_program(ViewHolder holder, int position) {
        RecommendContent content = get_content(position);
        holder.programView().setText(content.get_program_name());
    }

    public void set_content_selected(View itemView) {
        ContentListView contentListView = g_tvPackageDialog.findViewById(R.id.lo_tv_package_content_list);
        contentListView.set_content_selected(itemView);
    }

    public void set_content_unselected(View itemView) {
        int normalWidth = get().getResources().getDimensionPixelSize(R.dimen.content_item_layer_width);
        if (itemView.getWidth() > normalWidth) {
            ContentListView contentListView = g_tvPackageDialog.findViewById(R.id.lo_tv_package_content_list);
            contentListView.set_content_unselected(itemView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update_content_list(List<RecommendContent> contentList) {
        g_recommendContents = contentList;
        notifyDataSetChanged();
    }

    public void open_purchase_page(RecommendPackage onePackage) {
        Intent intent = new Intent(get(), PurchaseActivity.class);
        intent.putExtra(PurchaseActivity.KEY_PACKAGE_ID, onePackage.get_package_id());
        intent.putExtra(PurchaseActivity.KEY_PACKAGE_NAME, onePackage.get_package_name());
        //intent.setFlags(RtkMediaPlayer.FATALERR_AUDIO);
        get().startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public View itemFocus() {
            return itemView.findViewById(R.id.lo_item_layer_focus);
        }

        public ImageView posterView() {
            return itemView.findViewById(R.id.lo_item_layer_poster);
        }

        public TextView numberView() {
            return itemView.findViewById(R.id.lo_content_layer_number);
        }

        public ImageView iconView() {
            return itemView.findViewById(R.id.lo_content_layer_icon);
        }

        public TextView titleView() {
            return itemView.findViewById(R.id.lo_content_layer_title);
        }

        public TextView programView() {
            return itemView.findViewById(R.id.lo_content_layer_program);
        }
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            int distance = parent.getContext().getResources().getDimensionPixelOffset(R.dimen.content_list_adapter_out_rect_right);
            int distanceLast = parent.getContext().getResources().getDimensionPixelOffset(R.dimen.content_list_adapter_out_rect_right_last);
            int position = parent.getChildAdapterPosition(view);

            if (null == parent.getAdapter())
                return;

            int itemCount = parent.getAdapter().getItemCount();

            outRect.right = distance;
            if (position == itemCount - 1) {
                outRect.right = distanceLast;
            }
        }
    }
}
