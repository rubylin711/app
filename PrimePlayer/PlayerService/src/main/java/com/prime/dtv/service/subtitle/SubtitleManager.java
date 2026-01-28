package com.prime.dtv.service.subtitle;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaFormat;
import android.media.SubtitleTrack;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.PesEvent;
import android.media.tv.tuner.filter.PesSettings;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.FrameLayout;

import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.ThreadUtils;
import com.prime.datastructure.utils.ClosedCaptionUIDefine;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.service.Table.Desciptor.SubtitlingDescriptor;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.subtitle.dvb.DvbSubtitleCodec;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.dtv.service.subtitle.isdbtCC.IsdbccSubtitleCodec;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.Executor;

public class SubtitleManager {
    private static final String TAG = "SubtitleManager";
    private static SubtitleManager mSubtitlManager;
    private DataManager mDatamanager = null;

    private static final int MSG_SUBTITLE_BITMAP_INFO = 1;
    private static final int MSG_SUBTITLE_DATA_INFO = 2;
    private static final int MSG_CC_DATA_INFO = 3;

    private static final int CC_TYPE_DTVCC_PACKET_START = 3;
    private static final int CC_TYPE_DTVCC_PACKET_DATA = 2;
    private static final int DTVCC_MAX_PACKET_SIZE = 64;
    private static final int DTVCC_PACKET_SIZE_SCALE_FACTOR = 2;
    private static final int MAX_ALLOCATED_SIZE = 9600 / 8;
    private final ByteArrayBuffer mDtvCcPacket = new ByteArrayBuffer(MAX_ALLOCATED_SIZE);
    private int mDtvCcPacketCalculatedSize = 0;
    private boolean mDtvCcPacking = false;
    private SubtitleTrack.RenderingWidget mCCWidget = null;
    private Handler mHandler;
    private Executor mExecutor;
    private HandlerThread mExecutorHandlerThread = null;
    private Handler mExecutorHandlerThreadHandler = null;
    private static final int THREAD_COUNT = 1;
    private static HandlerThread mHandlerThread = null;
    private Handler mHandlerThreadHandler = null;
    private SubtitleCodec mSubtitleCodec;
    protected Filter mSubtitleFilter = null;
    private ArrayDeque<Integer> mInputBufferIndices;
    private InputBuffer mInputBuffer;
    private PesQueue pesQueue;
    private boolean isSubtitleSynced = false;
    private int subtitleSyncTimeUs = 0;
    private static TunerInterface mTuneInterface;
    private static boolean isStartSubtitle;
    private FrameLayout mLiveTvLayout;
    private SubtitleTrack cea608CaptionTrack ;
    private SubtitleTrack cea708CaptionTrack ;
    private boolean isCCMode = false;
    private boolean isCCAribStdB24 = false;
    private PesiDtvFrameworkInterfaceCallback mCallback = null;
    private Context mContext;
    private int currentSubtitlePid = 0;

    public SubtitleManager(Context context, PesiDtvFrameworkInterfaceCallback callback) {
        mTuneInterface = TunerInterface.getInstance(context);
        mDatamanager = DataManager.getDataManager(context);
        mContext = context;
        mCallback = callback;
        mExecutorHandlerThread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_AUDIO);
        mExecutorHandlerThread.start();
        mExecutorHandlerThreadHandler = new Handler(mExecutorHandlerThread.getLooper());
        mExecutor = new HandlerExecutor(mExecutorHandlerThreadHandler);

        mInputBufferIndices = new ArrayDeque<>();
        mInputBuffer = new InputBuffer();
        isStartSubtitle = false;
        mHandlerThread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_AUDIO);
        mHandlerThread.start();
        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //
                SubitleHandleMessage(msg);
            }
        };
    }

    public static SubtitleManager getSubtitleManager(Context context){
        return mSubtitlManager;
    }
    public static SubtitleManager getSubtitleManager(Context context,PesiDtvFrameworkInterfaceCallback callback) {
        if(mSubtitlManager == null){
            mSubtitlManager = new SubtitleManager(context, callback);
        }
        return mSubtitlManager;
    }
    public static boolean GetisStartSubtitle(){
        return isStartSubtitle;
    }
    private void SubitleHandleMessage(Message msg) {
        //LogUtils.d("SubitleHandleMessage msg = " + msg.what);
        switch(msg.what){
            case MSG_SUBTITLE_BITMAP_INFO:{
                LogUtils.d("SubitleHandleMessage obj = " + msg.obj);
                mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP, 0, 0, msg.obj);
            }break;
            case MSG_SUBTITLE_DATA_INFO:{
                pushPes(msg.getData().getByteArray("subtitleData"));
            }break;
            case MSG_CC_DATA_INFO: {
                getUserData(msg.getData().getByteArray("ccData"));
            }break;
        }
    }

    public void showSubtitle(Long presentationTimeUs){
        if(!mInputBufferIndices.isEmpty() && !pesQueue.isEmpty() &&
                pesQueue.getFirstTimestampUs() != -1 ){
            syncSubtitleTime(presentationTimeUs);
            if(pesQueue.getFirstTimestampUs() <= presentationTimeUs + subtitleSyncTimeUs){
                int index = mInputBufferIndices.pop();
                ByteBuffer inputBuffer;
                try {
                    inputBuffer = mSubtitleCodec.getInputBuffer(index);
                }catch(IllegalStateException exception) {
                    return;
                }
                mInputBuffer.buffer = inputBuffer;
                pesQueue.pop(mInputBuffer);
                mSubtitleCodec.queueInputBuffer(index, 0, inputBuffer.limit(), mInputBuffer.timestampUs, 0);
            }else {
                pesQueue.flushBefore(presentationTimeUs - subtitleSyncTimeUs);
            }

        }
    }
    public void syncSubtitleTime(Long presentationTimeUs){
        if(!isSubtitleSynced){
            int syncTime = (int) Math.abs(pesQueue.getFirstTimestampUs() - presentationTimeUs);
            if(syncTime <= 1000000) {
                isSubtitleSynced = true;
                subtitleSyncTimeUs = syncTime;
                Log.d(TAG, "subtitleSyncTimeUs = " +subtitleSyncTimeUs);
            }
        }
    }
    public void startSubtitle(int type, int tunerId, int Pid, int compage_id, int anycpage_id){
        Log.d(TAG,"startSubtitle type = "+type + " mSubtitleCodec = "+mSubtitleCodec);
        if(type == SubtitlingDescriptor.Subtitle.SUBTITLING_TYPE_CEA608)
        {
            startCCDataFilter(tunerId, Pid);
        }
        else if(mSubtitleCodec != null) {
            stoptSubtitle();
            ThreadUtils.sleep(100);
        }
        if(mSubtitleCodec==null){
            if(type == SubtitlingDescriptor.Subtitle.SUBTITLING_TYPE_ARIB_STD_B24) {
                isCCAribStdB24 = true;
                startSubtitlePesFilter(tunerId, Pid, SubtitlingDescriptor.Subtitle.SUBTITLING_TYPE_ARIB_STD_B24);
                mSubtitleCodec = new IsdbccSubtitleCodec();
                IsdbccSubtitleCodec isdbccSubtitleCodec = (IsdbccSubtitleCodec)mSubtitleCodec;
                isdbccSubtitleCodec.setCallback(mSubtitleCallback, mHandlerThreadHandler);
                isdbccSubtitleCodec.start();
            }
            else {
                startSubtitlePesFilter(tunerId, Pid, SubtitlingDescriptor.Subtitle.SUBTITLING_TYPE_DVB);
                mSubtitleCodec = new DvbSubtitleCodec();
                DvbSubtitleCodec dvbSubtitleCodec = (DvbSubtitleCodec)mSubtitleCodec;
                dvbSubtitleCodec.setPageIds(compage_id, anycpage_id);
                dvbSubtitleCodec.setCallback(mSubtitleCallback, mHandlerThreadHandler);
                dvbSubtitleCodec.start();
            }
        }
        currentSubtitlePid = Pid;
    }

    private void startSubtitlePesFilter(int tunerId, int pid, int type) {
        Tuner mTuner = mTuneInterface.getTuner(tunerId);
        LogUtils.d("startSubtitlePesFilter tunerId=" + tunerId + " pid=" + pid + " type=" + type);
    
        if (mTuner == null) {
            LogUtils.d("startSubtitlePesFilter Can't get mTuner:" + tunerId);
            return;
        }
        if (mSubtitleFilter != null) {
            LogUtils.d("startSubtitlePesFilter mSubtitleFilter already exists, skip creating new filter");
            return;
        }
    
        SubtitlefilterCallback subtitlefilterCallback;
        if (type == SubtitlingDescriptor.Subtitle.SUBTITLING_TYPE_ARIB_STD_B24)
            subtitlefilterCallback = new SubtitlefilterCallback(SubtitlefilterCallback.FilterType_CC_AribStdB24);
        else
            subtitlefilterCallback = new SubtitlefilterCallback(SubtitlefilterCallback.FilterType_Subtitle);
    
        LogUtils.d("openFilter(TYPE_TS, SUBTYPE_PES)...");
        mSubtitleFilter = mTuner.openFilter(
                Filter.TYPE_TS,
                Filter.SUBTYPE_PES,
                1024 * 1024,
                mExecutor,
                subtitlefilterCallback
        );
        LogUtils.d("start SubtitleFilter pid=" + pid + " mSubtitleFilter=" + mSubtitleFilter);
    
        PesSettings pesSettings = PesSettings
                .builder(Filter.TYPE_TS)
                .build();
        FilterConfiguration pesConfig = TsFilterConfiguration
                .builder()
                .setSettings(pesSettings)
                .setTpid(pid)
                .build();
        mSubtitleFilter.configure(pesConfig);
        mSubtitleFilter.start();
        pesQueue = new PesQueue();
        pesQueue.setDiscontinuityThreshold(5000000);
        isStartSubtitle = true;
        LogUtils.d("startSubtitlePesFilter end, isStartSubtitle=" + isStartSubtitle);
    }
    private void stopSubtitlePesFilter() {
        mHandlerThreadHandler.removeMessages(SubtitleManager.MSG_SUBTITLE_DATA_INFO);
        mHandlerThreadHandler.removeMessages(SubtitleManager.MSG_SUBTITLE_BITMAP_INFO);
        //if (mSubtitleFilter == null)
        //    return;
        LogUtils.d("stopPesFilter enter");
        isStartSubtitle = false;
        isSubtitleSynced = false;
        isCCAribStdB24 = false;
        subtitleSyncTimeUs = 0;
        if(mSubtitleFilter != null) {
            mSubtitleFilter.stop();
            mSubtitleFilter.close();
            mSubtitleFilter = null;
        }

        if(mSubtitleCodec != null){
            mSubtitleCodec.stop();
            mSubtitleCodec = null;
        }
        if(pesQueue != null){
            pesQueue.clear();
            pesQueue = null;
        }
        mInputBufferIndices.clear();
        mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP, 1, 0, null);
        LogUtils.d( "stopPesFilter end");
    }
    private SubtitleCodec.Callback mSubtitleCallback = new SubtitleCodec.Callback() {
        @Override
        public void onError(SubtitleCodec codec, SubtitleCodec.SubtitleException exception) {
            LogUtils.d("Subtitle SubtitleCodec error = " + exception.getMessage());
        }

        @Override
        public void onInputBufferAvailable(SubtitleCodec codec, int index) {
            LogUtils.d("Subtitle mInputBufferIndices index = " + index);
            mInputBufferIndices.add(index);
        }

        @Override
        public void onOutputBufferAvailable(SubtitleCodec codec, Bitmap bitmap, SubtitleCodec.BufferInfo info) {
            if(isStartSubtitle) {
                Message message = new Message();
                message.what = SubtitleManager.MSG_SUBTITLE_BITMAP_INFO;
                message.obj = bitmap;
                LogUtils.d("Subtitle info.timeoutSec = " + info.timeoutSec);
                mHandlerThreadHandler.sendMessage(message);
            }
        }
    };

//    protected final FilterCallback mFilterCallback = new SubtitlefilterCallback();

    public void stoptSubtitle() {
//        Log.d(TAG,"stoptSubtitle() isStartSubtitle = "+isStartSubtitle);
        if(isStartSubtitle) {
            stopCCData();
            stopSubtitlePesFilter();
            currentSubtitlePid = 0;
        }
    }

    public int getCurrentSubtitlePid() {
        return currentSubtitlePid;
    }

    public void setCurrentSubtitlePid(int currentSubtitlePid) {
        this.currentSubtitlePid = currentSubtitlePid;
    }

    private class SubtitlefilterCallback implements FilterCallback {
        private static final int FilterType_Subtitle = 0;
        private static final int FilterType_CCData = 1;
        private static final int FilterType_CC_AribStdB24 = 2;
        protected int filterType = 0;
    
        public SubtitlefilterCallback(int filterType) {
            this.filterType = filterType;
            LogUtils.d("SubtitlefilterCallback created, filterType=" + filterType);
        }
    
        @Override
        public void onFilterEvent(Filter filter, FilterEvent[] events) {
            try {
                for (FilterEvent event : events) {
                    if (event instanceof PesEvent) {
//                        LogUtils.d("subtitle onFilterEvent is PesEvent");
                        PesEvent pesEvent = (PesEvent) event;
                        int dataLength = pesEvent.getDataLength();
                        if (dataLength <= 0 )
                            continue;
                        if(!isStartSubtitle) {
                            return;
                        }
                        byte[] ccBuffer = new byte[dataLength];
                        LogUtils.d("primetif subtitle dataLength = " + dataLength);
                        filter.read(ccBuffer, 0, dataLength);
//                        LogUtils.d("subtitle read Length= " + filter.read(ccBuffer, 0, dataLength));
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        if(isStartSubtitle&& (filterType==FilterType_Subtitle || filterType==FilterType_CC_AribStdB24)){
                            message.what = SubtitleManager.MSG_SUBTITLE_DATA_INFO;
                            bundle.putByteArray("subtitleData",ccBuffer);
                        }
                        else if(isStartSubtitle&&filterType==FilterType_CCData) {
                            message.what = SubtitleManager.MSG_CC_DATA_INFO;
                            bundle.putByteArray("ccData", ccBuffer);
                        }
                        message.setData(bundle);
                        mHandlerThreadHandler.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                LogUtils.d("errors on handling filter: "+ e);
                //throw new RuntimeException(e);
            }
        }
    
        @Override
        public void onFilterStatusChanged(Filter filter, int status) {
            LogUtils.d("onFilterStatusChanged: filter=" + filter + " status=" + status);
        }
    }
    
    public void pushPes(byte[] subtitleBuffer){
        if (pesQueue ==null)
            return;
        ByteBuffer byteBuffer = ByteBuffer.wrap(subtitleBuffer);
        Pes pes = new Pes();
        pes.setByteBuffer(byteBuffer);
        pes.parseHeader();
        pesQueue.pushPes(pes);
    }

    public void startCCDataFilter(int tunerId, int pid) {
        Tuner mTuner = mTuneInterface.getTuner(tunerId);
        if(mTuner == null) {
            LogUtils.d("startCCDataFilter Can't get mTuner:"+tunerId);
            return;
        }
        int videoID = 0;
        if(mSubtitleFilter != null)
            return;
        mSubtitleFilter = mTuner.openFilter(Filter.TYPE_TS, Filter.SUBTYPE_PES, 1024 * 1024, mExecutor, new SubtitlefilterCallback(SubtitlefilterCallback.FilterType_CCData));
        Log.d(TAG, "cc data mSubtitleFilter end"+mSubtitleFilter);

        videoID = pid;
        Log.d(TAG, "vedioID =" +  videoID);
        int StreamId = (0xc000 |(videoID &0xFF));
        Log.d(TAG, "startCCDataFilter ");
        Log.d(TAG, "StreamId =" +  Integer.toHexString(StreamId));
        PesSettings ccDataSettings = PesSettings
                .builder(Filter.TYPE_TS)
                .setStreamId(StreamId)
                .build();
        FilterConfiguration ccDataConfig = TsFilterConfiguration
                .builder()
                .setSettings(ccDataSettings)
                .build();
        mSubtitleFilter.configure(ccDataConfig);
        Log.d(TAG, "configure  end");
        mSubtitleFilter.start();
        isStartSubtitle = true;
        isCCMode = true;
        Log.d(TAG, "start end");
    }

    public void stopCCData(){
        if (mSubtitleFilter != null) {
            Log.d(TAG, " cc data mSubtitleFilter.stop() start...");
            isCCMode = false ;
            mSubtitleFilter.stop();
            mSubtitleFilter.close();
            mSubtitleFilter = null;
            remove_CCDataWidget();
            cea608CaptionTrack = null;
            cea708CaptionTrack = null;
            isStartSubtitle = false;
//            if(mVideoView != null){
//                mVideoView.setSu(null);
//            }
            Log.d(TAG, " mCCDataFilter.stop() end...");
        }
    }

    public void setCCLayout(FrameLayout frameLayout){
        this.mLiveTvLayout =frameLayout;
    }

    public void getUserData(byte[] ccData){
        if(ccData[0] == 0 && ccData[1] == 0 &&ccData[2] == 1
                && String.format("%02X" ,ccData[3]).equals("B2")
                && String.format("%02X" ,ccData[4]).equals("47")
                && String.format("%02X" ,ccData[5]).equals("41")
                && String.format("%02X" ,ccData[6]).equals("39")
                && String.format("%02X" ,ccData[7]).equals("34")
                && ccData[8] == 3
                && String.format("%02X" ,ccData[ccData.length-1]).equals("FF")){
            LogUtils.d("isMpegCCData");
            // start_code = 0x000001B2
            // user_identifier = 0x47413934
            // type_code = 0x03
            //get cc_pkt
            int count = ccData[9] &0x1f;
            int cc_length = count*3 +11 ;
            LogUtils.d("count = " + count);
            LogUtils.d("ccData.length = " + ccData.length);
            if(cc_length+1 > ccData.length)
                return;
            byte[] data = new byte[count*3];
            for (int i = 11; i < cc_length; i++) {
                data[i - 11] = ccData[i];
            }
            getDTVCCPacket(data);
        }else if( String.format("%02X" ,ccData[0]).equals("B5")
                && ccData[1] == 0
                && String.format("%02X" ,ccData[2]).equals("31")
                && String.format("%02X" ,ccData[3]).equals("47")
                && String.format("%02X" ,ccData[4]).equals("41")
                && String.format("%02X" ,ccData[5]).equals("39")
                && String.format("%02X" ,ccData[6]).equals("34")
                && ccData[7] == 3
                && String.format("%02X" ,ccData[ccData.length-1]).equals("FF")){
            LogUtils.d("isH264UserData");
            int count = ccData[8] &0x1f;
            int cc_length = count*3 +10 ;
            LogUtils.d("count = " + count);
            LogUtils.d("ccData.length = " + ccData.length);
            if(cc_length+1 > ccData.length)
                return;
            byte[] data = new byte[count*3];
            for (int i = 10; i < cc_length; i++) {
                data[i - 10] = ccData[i];
            }
            getDTVCCPacket(data);
        }
    }

    private void getDTVCCPacket(byte[] data){
        StringBuilder bufferString = new StringBuilder();
        for(byte ccByte :data){
            bufferString.append(String.format("%02x", ccByte));
        }
        LogUtils.d("getDTVCCPacket ccData = " + bufferString);
        int ccCount = data.length / 3;
        byte[] bytes = new byte[3 * ccCount];
        for (int i = 0; i < 3 * ccCount; i++) {
            bytes[i] = data[i];
        }
        int pos = 0;

        for (int i = 0; i < ccCount; ++i) {
            boolean ccValid = (bytes[pos] & 0x04) != 0;
            int ccType = bytes[pos] & 0x03;
            byte[] cc_byte = new byte[3];
            if (ccValid) {
                LogUtils.d("ccType = " + ccType);
                if (isCCMode) {
                    if (ccType == 2 || ccType == 3) {
                        isCCMode = false;
                        initCea708CaptionRenderer();
                    }else {
                        if((bytes[pos+1]&0x7F) ==0 && (bytes[pos+2]&0x7F) == 0) {
                            pos += 3; // next cc_data_pkt
                            continue;
                        } else{
                            isCCMode = false;
                            initClosedCaptionRenderer();
                        }
                    }
                }
                //if ccType =0 or 1 is should be interpreted as CEA-608 Captions
                // and just get ccType == 0
                if (ccType == 0) {
                    cc_byte[0] = bytes[pos];
                    cc_byte[1] = (byte) (bytes[pos+1]&0x7F); // modified 7-bit
                    cc_byte[2] = (byte) (bytes[pos+2]&0x7F);
                    showCea608CCData(cc_byte);
                }
                if (ccType == CC_TYPE_DTVCC_PACKET_START) {
                    if (mDtvCcPacking) {
                        parseDtvCcPacket(mDtvCcPacket.buffer(),mDtvCcPacket.length());
                        mDtvCcPacket.clear();
                        mDtvCcPacketCalculatedSize = 0;
                    }
                    mDtvCcPacking = true;
                    int packetSize = bytes[pos + 1] & 0x3F; // last 6 bits
                    if (packetSize == 0) {
                        packetSize = DTVCC_MAX_PACKET_SIZE;
                    }
                    LogUtils.d("packetSize =  " +packetSize);
                    mDtvCcPacketCalculatedSize = packetSize * DTVCC_PACKET_SIZE_SCALE_FACTOR;
                    mDtvCcPacket.append(bytes[pos + 1]);
                    mDtvCcPacket.append(bytes[pos + 2]);
                } else if (mDtvCcPacking && ccType == CC_TYPE_DTVCC_PACKET_DATA) {
                    mDtvCcPacket.append(bytes[pos + 1]);
                    mDtvCcPacket.append(bytes[pos + 2]);
                }
                if ((ccType == CC_TYPE_DTVCC_PACKET_START || ccType == CC_TYPE_DTVCC_PACKET_DATA)
                        && mDtvCcPacking && mDtvCcPacket.length() == mDtvCcPacketCalculatedSize) {
                    mDtvCcPacking = false;
                    parseDtvCcPacket(mDtvCcPacket.buffer(),mDtvCcPacket.length());
                    mDtvCcPacket.clear();
                    mDtvCcPacketCalculatedSize = 0;
                }
            }
            pos += 3;
        }
    }

    private void parseDtvCcPacket(byte[] data,int length) {
        int pos = 0;
        int packetSize = data[pos] & 0x3f;
        if (packetSize == 0) {
            packetSize = DTVCC_MAX_PACKET_SIZE;
        }
        int calculatedPacketSize = packetSize * DTVCC_PACKET_SIZE_SCALE_FACTOR;
        if (length != calculatedPacketSize) {
            return;
        }
        ++pos;
        int len = pos + calculatedPacketSize;
        while (pos < len) {
            int serviceNumber = (data[pos] & 0xe0) >> 5;
            int blockSize = data[pos] & 0x1f;
            ++pos;
            LogUtils.d("serviceNumber =  " + serviceNumber);
            if (serviceNumber == 7) {
                serviceNumber = (data[pos] & 0x3f);
                ++pos;
                // Return if invalid service number
                if (serviceNumber < 7) {
                    return;
                }
            }
            if (pos + blockSize > length) {
                return;
            }
            if (blockSize == 0 ||serviceNumber != 1/*cc_1*/) {
                pos += blockSize;
                continue;
            }
            int blockLimit = pos + blockSize;
            byte[] blocksData = new byte[blockSize];
            LogUtils.d("blockSize =  " + blockSize);
            LogUtils.d("pos =  " + pos);
            System.arraycopy(data, pos, blocksData, 0, blockSize);
            showCea708CCData(blocksData);
            pos = blockLimit;
            LogUtils.d("pos =  " + pos);
        }
    }

    private void initClosedCaptionRenderer(){
        mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_CLOSED_CAPTION_ENABLE, 1, ClosedCaptionUIDefine.CEA_608, null);
        LogUtils.d("initClosedCaptionRenderer Cea608 end");
    }

    private void initCea708CaptionRenderer(){
        mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_CLOSED_CAPTION_ENABLE, 1, ClosedCaptionUIDefine.CEA_708, null);
        LogUtils.d("initCea708CaptionRenderer Cea708 end");
    }

    private void remove_CCDataWidget() {
        mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_CLOSED_CAPTION_ENABLE, 0, 0, null);
    }

    private void showCea608CCData(byte [] ccData){
        LogUtils.d("showCCData");
        if (cea608CaptionTrack == null)
            return;
        mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_CLOSED_CAPTION_DATA, 0, 0, ccData);
    }

    private void showCea708CCData(byte [] ccData){
        LogUtils.d("Cea708 showCCData ");
        if (cea708CaptionTrack == null)
            return;
        mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_CLOSED_CAPTION_DATA, 0, 0, ccData);
    }

    public boolean isStartSubtitle() {
        return isStartSubtitle;
    }
}
