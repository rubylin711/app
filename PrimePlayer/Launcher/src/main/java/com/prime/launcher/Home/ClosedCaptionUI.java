package com.prime.launcher.Home;

import android.app.Activity;
import android.content.Context;
import android.media.MediaFormat;
import android.media.SubtitleTrack;
import android.view.View;
import android.widget.FrameLayout;


import androidx.appcompat.app.AppCompatActivity;

import com.prime.datastructure.utils.ClosedCaptionUIDefine;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.HomeApplication;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.service.subtitle.Cea708CaptionRenderer;
import com.prime.dtv.service.subtitle.ClosedCaptionRenderer;

import java.lang.ref.WeakReference;

public class ClosedCaptionUI {

    protected FrameLayout mClosedCaptionLayout = null;

    private static ClosedCaptionUI mClosedCaptionUI;
    private SubtitleTrack cea608CaptionTrack ;
    private SubtitleTrack cea708CaptionTrack ;
    private SubtitleTrack.RenderingWidget mCCWidget = null;
    private WeakReference<Context> mRefContext = null;

    public static ClosedCaptionUI getInstance() {
        if(mClosedCaptionUI == null)
            mClosedCaptionUI = new ClosedCaptionUI();
        return mClosedCaptionUI;
    }

    public void setClosedCaptionLayout(Context context, FrameLayout frameLayout) {
        mRefContext = new WeakReference<>(context);
        mClosedCaptionLayout = frameLayout;
    }


    public void DoClosedCaptionRenderer(int enable, int type) {
        if(enable == 1) {
            if (type == ClosedCaptionUIDefine.CEA_608)
                initCea608CaptionRenderer();
            if (type == ClosedCaptionUIDefine.CEA_708)
                initCea708CaptionRenderer();
        }
        else {
            remove_CCDataWidget();
        }
    }

    public void initCea608CaptionRenderer(){
        Activity activity = HomeApplication.get_current_activity();
        if(mClosedCaptionLayout != null) {
            if (activity instanceof HomeActivity) {
                AppCompatActivity Home_activity = (AppCompatActivity) activity;
                ClosedCaptionRenderer captionRenderer = new ClosedCaptionRenderer(activity);
                cea608CaptionTrack = captionRenderer.createTrack(MediaFormat.createAudioFormat(
                        MediaFormat.MIMETYPE_TEXT_CEA_608,0,0 ));

                mCCWidget = cea608CaptionTrack.getRenderingWidget();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                Home_activity.runOnUiThread(()->{
                    if(mClosedCaptionLayout != null)
                        mClosedCaptionLayout.addView((View) mCCWidget, layoutParams);
                });
            }
//            videoView.setSubtitleWidget(widget);
        }else{
            LogUtils.d("not have view");
        }
        LogUtils.d("setSubtitleWidget Cea608 end");
    }

    public void initCea708CaptionRenderer(){
        Activity activity = HomeApplication.get_current_activity();
        if(mClosedCaptionLayout != null) {
            if (activity instanceof HomeActivity) {
                AppCompatActivity Home_activity = (AppCompatActivity) activity;
                Cea708CaptionRenderer captionRenderer = new Cea708CaptionRenderer(mRefContext.get());
                cea708CaptionTrack = captionRenderer.createTrack(MediaFormat.createAudioFormat(
                        MediaFormat.MIMETYPE_TEXT_CEA_708, 0, 0));

                mCCWidget = cea708CaptionTrack.getRenderingWidget();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                Home_activity.runOnUiThread(()->{
                    if(mClosedCaptionLayout != null)
                        mClosedCaptionLayout.addView((View) mCCWidget, layoutParams);
                });
            }
        }
        LogUtils.d("setSubtitleWidget Cea708 end");
    }

    public void remove_CCDataWidget() {
        Activity activity = HomeApplication.get_current_activity();
        if (activity instanceof HomeActivity) {
            AppCompatActivity Home_activity = (AppCompatActivity) activity;
            Home_activity.runOnUiThread(()-> {
                if (mCCWidget != null && mClosedCaptionLayout != null) {
                    View view = (View) mCCWidget;
                    view.setVisibility(View.INVISIBLE);
                    mClosedCaptionLayout.removeView(view);
                }
            });
        }
    }

    public void showCea608CCData(byte [] ccData){
        LogUtils.d("showCCData");
        if (cea608CaptionTrack == null)
            return;
        Activity activity = HomeApplication.get_current_activity();
        if(mClosedCaptionLayout != null) {
            if (activity instanceof HomeActivity) {
                AppCompatActivity Home_activity = (AppCompatActivity) activity;
                Home_activity.runOnUiThread(()->{
                    if(cea608CaptionTrack != null)
                        cea608CaptionTrack.onData(ccData,true,0);
                });
            }
//            videoView.setSubtitleWidget(widget);
        }else{
            LogUtils.d("not have view");
        }

    }

    public void showCea708CCData(byte [] ccData){
        LogUtils.d("Cea708 showCCData ");
        if (cea708CaptionTrack == null)
            return;
        Activity activity = HomeApplication.get_current_activity();
        if(mClosedCaptionLayout != null) {
            if (activity instanceof HomeActivity) {
                AppCompatActivity Home_activity = (AppCompatActivity) activity;
                Home_activity.runOnUiThread(()->{
                    if(cea708CaptionTrack != null)
                        cea708CaptionTrack.onData(ccData,true,0);
                });
            }
//            videoView.setSubtitleWidget(widget);
        }else{
            LogUtils.d("not have view");
        }
    }
}
