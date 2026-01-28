package com.prime.homeplus.membercenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TutorialView extends ConstraintLayout {
    public TutorialView(@NonNull Context context) {
        super(context);
    }

    public TutorialView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TutorialView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TutorialView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Context context;
    private Drawable img;
    private String subtitle;
    private String title;
    private String imgUrl;

    public TutorialView(Context context, String str, String str2, Drawable drawable) {
        super(context);

        this.context = context;

        this.title = str;
        this.subtitle = str2;
        this.img = drawable;

        init(context);
    }

    public TutorialView(Context context, String str, String str2, String imgUrl) {
        super(context);

        this.context = context;

        this.title = str;
        this.subtitle = str2;
        this.imgUrl = imgUrl;

        init(context);
    }


    public void init(Context context) {
        View.inflate(context, R.layout.tutorial_item, this);
            TextView item_title = findViewById(R.id.item_title);
        item_title.setText(this.title);
        TextView item_subtitle = findViewById(R.id.item_subtitle);
        item_subtitle.setText(this.subtitle);
        ImageView item_img = findViewById(R.id.item_img);
        if (this.img != null) {
            item_img.setImageDrawable(this.img);
        }
    }

    public void updateImage() {
        ImageView item_img = findViewById(R.id.item_img);
        if (this.imgUrl != null) {
            Log.d("HomePlus-Tutorial", "updateImage(): imgUrl - " + imgUrl);
            Glide.with(this).load(imgUrl).into(item_img);
        }
    }
}