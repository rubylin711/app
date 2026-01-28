package com.prime.dtv.service.Player;

public class BasePlayController {
    private static final String TAG = "BasePlayController";

//    public BasePlayController(Activity mAct, Handler mHandler, int tag) {
//        this.mAct = mAct;
//        this.mHandler = mHandler;
//        this.mPath = tag;
//        mExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
//        mPlayControllerSp = RtkApplication.getApplication().getSharedPreferences(SettingSp.SP_RTK_TUNER_HAL_DEMO, Context.MODE_PRIVATE);
//        mISAfterChangeChannel=false;
//        mChangeChannelCount=0;
//        mFrameRenderedListener = new MediaCodec.OnFrameRenderedListener() {
//            @Override
//            public void onFrameRendered(@NonNull MediaCodec codec, long presentationTimeUs, long nanoTime){
//                // a customized way to consume data efficiently by using status as a hint.
//                if(mISStartPlaying ){
//                    Log.d(TAG, "onFrameRendered ......");
//                    mISStartPlaying = false;
//                }
//                if(SettingUtils.isSubtitleON())
//                    showSubtitle(presentationTimeUs);
//            }
//        };
//        mBaseChannelTimer = new BaseChannelTimer(5000,1000);
//        mInputBufferIndices = new ArrayDeque<>();
//        mInputBuffer = new InputBuffer();
//    }
}
