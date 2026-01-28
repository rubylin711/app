package com.prime.dtv.service.subtitle;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

public class Subtitler implements View.OnLayoutChangeListener {
    private Handler mBitmapHandler;
    private BitmapPainter mBitmapPainter;

    private class BitmapPainter
            implements Runnable {
        private ImageView mView;
        private Bitmap mBitmap;
        private float mX;
        private float mY;
        private float mScaleX;
        private float mScaleY;

        public BitmapPainter(ImageView imageView) {
            mView = imageView;

            mScaleX = 1.0f;
            mScaleY = 1.0f;

            mX = 0;
            mY = 0;

            mView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        public void setScale(float scaleX,
                             float scaleY) {
            mScaleX = scaleX;
            mScaleY = scaleY;
        }

        public void setPosition(float x,
                                float y) {
            mX = x;
            mY = y;
        }

        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        @Override
        public void run() {
            mView.setImageBitmap(mBitmap);
        }
    }

    public Subtitler(Looper looper,
                     ImageView imageView) {
        mBitmapPainter = new BitmapPainter(imageView);
        mBitmapHandler = new Handler(looper);
    }

    public Subtitler(Looper looper) {
        mBitmapHandler = new Handler(looper);
    }

    public void setImageView(ImageView image){
        mBitmapPainter = new BitmapPainter(image);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmapPainter.setBitmap(bitmap);
        mBitmapHandler.post(mBitmapPainter);
    }

    @Override
    public void onLayoutChange(View v,
                               int left,
                               int top,
                               int right,
                               int bottom,
                               int oldLeft,
                               int oldTop,
                               int oldRight,
                               int oldBottom) {
        mBitmapPainter.setScale((right - left) / 1920.0f,
                (bottom - top) / 1080.0f);
        mBitmapHandler.post(mBitmapPainter);
    }
}
