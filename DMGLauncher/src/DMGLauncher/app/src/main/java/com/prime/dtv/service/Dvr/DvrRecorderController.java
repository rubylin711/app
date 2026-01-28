package com.prime.dtv.service.Dvr;

import android.content.Context;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.dvr.DvrRecorder;
import android.media.tv.tuner.dvr.DvrSettings;
import android.media.tv.tuner.dvr.OnRecordStatusChangedListener;
import android.media.tv.tuner.filter.Filter;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.service.Table.StreamType;
import com.prime.dtv.service.Util.MediaUtils;
import com.prime.dtv.sysdata.DTVMessage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;


import java.time.Instant;

public class DvrRecorderController {
    private static final String TAG = "DVR DvrRecorderController";
    public static final String PVR_FOLDER = "/DADA" ;
    public static final int RECORDERMODE_RECORD = 0;
    public static final int RECORDERMODE_TIMEShIFT = 1;
    public static final long PTS_MAX = 95443717900L;

    private PesiDtvFrameworkInterfaceCallback mPesiDtvCallback = null;

    private Tuner mTuner = null;

    private DvrRecorder mDvrRecorder=null;
    private long bufferSize;
    private ArrayList<Filter> mFilterInfos = new ArrayList<Filter>();
    private int mRecordMode = RECORDERMODE_RECORD;



    private String originalFilePath = "";
    private String filePath = "";
    private String fileInfoPath = "";
    private ParcelFileDescriptor mPfd;
    private ParcelFileDescriptor mFileInfofd;


    private long readSize = 307192; //300*1024;
    private volatile boolean dvrStop = false;
    private Lock dvrStopLock = new ReentrantLock();
    protected ExecutorService mExecutor;
    private static int mSleepTime=30;
    private static boolean mDataReady=false;

    private long mFilelength;


    private File writeFile =null;
    private RandomAccessFile InputStream =null;
    //private BufferedOutputStream InputStream = null;
    //private static final int MAX_SIZE_PER_MINUTE = 80;//100; //MB
    private static final int TIMESHIFT_SIZE_MINUTE = 180; //minute
    //private static final long BASE_FILE_SIZE = 50*1024*1024L;
    //private static final long MAX_FILE_SIZE = 50*1024*1024L;
    public static final long MAX_RECORD_FILE_SIZE = 2048L*1024L*1024L;
    //private static final int  MAX_FILE_NUM  = (((MAX_SIZE_PER_MINUTE*TIMESHIFT_SIZE_MINUTE) / (int)(MAX_FILE_SIZE/(1024*1024))) + 3);//100;
    private volatile int MAX_SIZE_PER_MINUTE;
    private volatile int MAX_FILE_NUM ;
    private volatile long BASE_FILE_SIZE;
    private volatile long MAX_FILE_SIZE;

    private long recordingSize = 0;
    private int  mCurrentFileIndex = -1;
    private long totalRecordingSize = 0;

    //private Filter mRecorderVideoFilter;
    //private Filter mRecorderAudioFilter;

    public FileWriter mfileWriter;

    public static ArrayList<Integer> mInfosFileIndexList;
    public static volatile int  mLockFileIndex = -1;
    public static Map<Integer, Long> mFileIndexMapTime;
    public static  Lock mLockFileIndexLock = new ReentrantLock();
    private long mTimeShiftrecordingFileSize = 0;

    private int mDurationSec;
    private int mTunerId;

    private Context mContext = null;

    private volatile long mStartRecTime;
    private volatile long mCurrentTime;


    private AtomicInteger mRecordSec ;

    private AtomicInteger mTimeshiftRecEndSec;
    private AtomicInteger mTimeshiftRecStartSec;
    //private AtomicInteger mTimeshiftPlaybackCurrentIndex;
    private static final int THREAD_COUNT = 1;

    public DvrRecorderController(PesiDtvFrameworkInterfaceCallback callback,Context context,int tunerId,int durationSec){
        mCurrentFileIndex = -1;
        mDurationSec = durationSec;
        mPesiDtvCallback = callback;
        mTunerId = tunerId;
        mExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
        mContext = context;
        mInfosFileIndexList = null;
        mFileIndexMapTime = null;
    }

    public int getTimeShiftBufferSec(){
        return TIMESHIFT_SIZE_MINUTE*60;
    }

    public long getStartRecTimestamp(){
        return mStartRecTime;
    }

    public long getTimeShiftrecordingFileSize(){
        return mTimeShiftrecordingFileSize;
    }

    public int getRecordTime() {
        return (mRecordSec != null) ? mRecordSec.get() : 0;
    }

    public int getTimeshiftRecStartTime() {
        return (mTimeshiftRecStartSec != null) ? mTimeshiftRecStartSec.get() : 0;
    }
    public int getTimeshiftRecEndTime() {
        return (mTimeshiftRecEndSec != null) ? mTimeshiftRecEndSec.get() : 0;
    }

    public long getTotalRecordSize() {
        return totalRecordingSize;
    }

    public ArrayList<Filter>  getAllFilterInfo() {
        return  mFilterInfos;
    }

    public void setFilePath(String path){
        originalFilePath = path;
        filePath = path;
        fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;
    }

    public void setTuner(Tuner tuner ){
        mTuner = tuner;
        //Log.d(TAG,"tuner = "+tuner);
    }

    public void setRecordMode(int mode){

        mRecordMode = mode;
        if(mRecordMode == RECORDERMODE_TIMEShIFT){
            mLockFileIndex = -1;
            mInfosFileIndexList = null;
            mFileIndexMapTime = null;
        }
    }

    public int getRecordMode(){
        return mRecordMode;
    }

    public void setAttachFilter(Filter filter){
        mFilterInfos.add(filter);
        mDvrRecorder.attachFilter(filter);
    }

    public void setdetachFilter(Filter filter){
        mDvrRecorder.detachFilter(filter);
        mFilterInfos.remove(filter);
    }

    public void initDvrRecorder(int videoCodec){
        int mStatusMask = 1;
        long mLowThreshold = 1258291;
        long mHighThreshold = 3774873;
        long mPacketSize = 188;

        Log.d(TAG,"initDvrRecorder start");
        dvrStopLock.lock();
        dvrStop = false;
        dvrStopLock. unlock();
        readSize = 10*1048476; //10M
        bufferSize = 2*20971400L;//40M
        mLowThreshold = bufferSize / 4;
        mHighThreshold =(bufferSize * 3) / 4;
        if(mRecordMode == RECORDERMODE_TIMEShIFT) {
        mLockFileIndexLock.lock();
        mLockFileIndex = -1;
        mLockFileIndexLock.unlock();
            if(videoCodec != -1) {
                switch (videoCodec) {
                    case MediaUtils.RTK_CODEC_VIDEO_MPEG1:
                        MAX_SIZE_PER_MINUTE = 100;
                        MAX_FILE_NUM = 60;
                        Log.d(TAG, "RTK_CODEC_VIDEO_MPEG1");
                        break;
                    case MediaUtils.RTK_CODEC_VIDEO_MPEG2: //0x02
                        MAX_SIZE_PER_MINUTE = 100;
                        MAX_FILE_NUM = 60;
                        Log.d(TAG, "RTK_CODEC_VIDEO_MPEG2");
                        break;
                    case MediaUtils.RTK_CODEC_VIDEO_MPEG4_PART2:
                        MAX_SIZE_PER_MINUTE = 150;
                        MAX_FILE_NUM = 60;
                        Log.d(TAG, "RTK_CODEC_VIDEO_MPEG4_PART2");
                        break;
                    case MediaUtils.RTK_CODEC_VIDEO_H264:
                        MAX_SIZE_PER_MINUTE = 150;
                        MAX_FILE_NUM = 60;
                        Log.d(TAG, "RTK_CODEC_VIDEO_H264");
                        break;
                    case MediaUtils.RTK_CODEC_VIDEO_H265:
                        MAX_SIZE_PER_MINUTE = 500;
                        //readSize = 1048476; //1M
                        //bufferSize = 20971400L;//20M
                        //mLowThreshold = bufferSize / 4;
                        //mHighThreshold = (bufferSize * 3) / 4;
                        MAX_FILE_NUM = 60;
                        Log.d(TAG, "RTK_CODEC_VIDEO_H265");
                        break;
                    default:
                        MAX_SIZE_PER_MINUTE = 150;
                        MAX_FILE_NUM = 60;
                        break;
                }
            }
            //MAX_FILE_NUM = 1600;
            BASE_FILE_SIZE = MAX_SIZE_PER_MINUTE*1024*1024L;
            MAX_FILE_SIZE = MAX_SIZE_PER_MINUTE*1024*1024L;
            //MAX_FILE_NUM = (((MAX_SIZE_PER_MINUTE * TIMESHIFT_SIZE_MINUTE) / (int) (MAX_FILE_SIZE / (1024 * 1024))) + 2);//100;
        }

        //mOnPlaybackStatusChangedListener = new OnPlaybackStatusChangedListener();
        OnRecordStatusChangedListener DvrListener = new OnRecordStatusChangedListener() {
            @Override
            public void onRecordStatusChanged(int status) {
                /*
                enum RecordStatus {
                    DATA_READY = 1,
                    LOW_WATER = 2,
                    HIGH_WATER = 4,
                    OVERFLOW = 8,
                }
                */

                // a customized way to consume data efficiently by using status as a hint.
                //Log.d(TAG,"onRecordStatusChanged status = " + status);
                mDataReady = true;
                if(status ==1)
                    mDataReady = true;
                else if(status ==2)
                    mSleepTime=30;
                else if(status >=4)
                    mSleepTime=10;
                Log.d(TAG, "RecordStatus = "+status+", mSleepTime = "+mSleepTime );
            }
        };
        //Log.d(TAG, "initDvrRecorder  openDvrRecorder" );
        mDvrRecorder = mTuner.openDvrRecorder(bufferSize, mExecutor, DvrListener);

        int mDataFormat = DvrSettings.DATA_FORMAT_TS;
        DvrSettings dvrSettings = DvrSettings.builder()
                .setStatusMask(mStatusMask).setLowThreshold(mLowThreshold).setHighThreshold(mHighThreshold).setPacketSize(mPacketSize).setDataFormat(mDataFormat).build();

        mDvrRecorder.configure(dvrSettings);
        openRecorderFile();
        if(mRecordMode == RECORDERMODE_TIMEShIFT) {
        mInfosFileIndexList.add(mCurrentFileIndex);
        mFileIndexMapTime.put(mCurrentFileIndex,System.currentTimeMillis());
    }
    }

    public void openRecorderFile(){
        if(mRecordMode == RECORDERMODE_TIMEShIFT) {
            if (mInfosFileIndexList == null) {
                mInfosFileIndexList = new ArrayList<>();
            }
            if (mFileIndexMapTime == null) {
                mFileIndexMapTime = new HashMap<Integer, Long>();
            }
        }

        if(mRecordMode == RECORDERMODE_TIMEShIFT){
            if(mCurrentFileIndex<0)
                mCurrentFileIndex = 0;
            else
                mCurrentFileIndex++;

            filePath = originalFilePath+"_INDEX_"+mCurrentFileIndex;
            fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;
        }

        if(mRecordMode == RECORDERMODE_RECORD){
            if(mCurrentFileIndex<0)
                mCurrentFileIndex = 0;
            else
                mCurrentFileIndex++;
            if(mCurrentFileIndex > 0) {
                filePath = originalFilePath + "_INDEX_" + mCurrentFileIndex;
                fileInfoPath = filePath + DvrRecordInfo.FileInfoExtension;
            }
            //Log.d(TAG, "filePath="+filePath+" fileInfoPath="+fileInfoPath);
        }

        mPfd = openFile(filePath);
        Log.d(TAG, "filePath="+filePath+" mPfd="+mPfd+" mRecordMode="+mRecordMode);
        mDvrRecorder.setFileDescriptor(mPfd);
        mFileInfofd = openFile(fileInfoPath);
        Log.d(TAG, "fileInfoPath="+fileInfoPath+" mFileInfofd="+mFileInfofd+" mRecordMode="+mRecordMode);
        try{
            mfileWriter = new FileWriter(fileInfoPath,true);// true:add write
        }catch (Exception e){
            Log.d(TAG,"FileWriter fail, e = "+e);
        }
    }

    public void firstRemoveFile(){
        //Log.d(TAG,"firstRemoveFile DvrRecorderController.mLockFileIndexLock.lock()");
        DvrRecorderController.mLockFileIndexLock.lock();
        Log.d(TAG,"MAX_FILE_NUM="+MAX_FILE_NUM+" mInfosFileIndexList.size()="+mInfosFileIndexList.size());

        if(mRecordMode == RECORDERMODE_TIMEShIFT && mInfosFileIndexList != null && mInfosFileIndexList.size()>MAX_FILE_NUM){
            String filePathTmp;
            String fileInfoPathTmp;
            int removeFileIndexTmp;
            Log.d(TAG,"mLockFileIndex="+mLockFileIndex+" mInfosFileIndexList.get(0)="+mInfosFileIndexList.get(0));
            Log.d(TAG,"mFileIndexMapTime.get(0)="+mFileIndexMapTime.get(0)+" mInfosFileIndexList.get(0)="+mInfosFileIndexList.get(0));
            if(mLockFileIndex == -1){
                mLockFileIndex = mInfosFileIndexList.get(0);
            }else if(mLockFileIndex != mInfosFileIndexList.get(0)){
                removeFileIndexTmp=mInfosFileIndexList.get(0);
                mFileIndexMapTime.remove(mInfosFileIndexList.get(0));
                mInfosFileIndexList.remove(0);
                filePathTmp = originalFilePath+"_INDEX_"+removeFileIndexTmp;
                fileInfoPathTmp = filePathTmp+DvrRecordInfo.FileInfoExtension;
                Log.d(TAG, "delete file index: "+removeFileIndexTmp+" filePathTmp:"+filePathTmp+" fileInfoPathTmp:"+fileInfoPathTmp);
                File file = new File(filePathTmp);
                file.delete();
                try{
                    mfileWriter.close();
                }catch(Exception e){
                }
                file = new File(fileInfoPathTmp);
                file.delete();
            }
        }
        DvrRecorderController.mLockFileIndexLock.unlock();
        Log.d(TAG,"firstRemoveFile DvrRecorderController.mLockFileIndexLock.unlock()");
    }

    public long FileSizeCalculator(int fileIndex){
        double incrementFactor = 1.01;
        long currentSize = BASE_FILE_SIZE;
        for (int i = 1; i < fileIndex; i++) {
            if (currentSize < MAX_FILE_SIZE) {
                currentSize = (long) (currentSize * incrementFactor);
            } else {
                currentSize = MAX_FILE_SIZE;
            }
        }
        return currentSize;
    }

    public ParcelFileDescriptor openFile(String filePath) {
        Log.d(TAG, "start play... openFile filePath =" + filePath);
        File file = new File(filePath);
        mFilelength = file.length();
        if(file.exists()){
            Log.d(TAG, "remove the old file...  filePath =" + filePath);
            file.delete();

            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "file.createNewFile() fail, e =" + e);
                e.printStackTrace();
            }
        }else{
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "createNewFile fail, e =" + e);
            }
        }
        ParcelFileDescriptor pfd;
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_CREATE|ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return pfd;
    }

    void closeFile(){
        if(mPfd != null){

            Log.d(TAG,"read file length...");
            long fileLength = -1; // 用來儲存檔案長度

            // --- 在關閉檔案前，執行檔案長度計算 ---
            FileInputStream fis = null;
            try {
                // 從 ParcelFileDescriptor 取得底層的 FileDescriptor
                FileDescriptor fd = mPfd.getFileDescriptor();

                // 建立 FileInputStream 來讀取檔案資訊（這裡只讀取長度，不讀內容）
                fis = new FileInputStream(fd);

                // 透過 FileChannel 取得檔案大小
                fileLength = fis.getChannel().size();

                Log.d(TAG, "filePath="+filePath+" fileLength="+fileLength+" bytes");

            } catch (IOException e) {
                // 處理可能的 I/O 錯誤
                Log.e(TAG, "read file length fail: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // 務必關閉 FileInputStream，避免資源洩漏
                // 即使上面發生例外，也要確保它被關閉
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close FileInputStream fail: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            try {
                Log.d(TAG,"close file...");
                mPfd.close();
                mPfd = null;
            } catch (Exception e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
        if(mfileWriter !=null){
            try {
                mfileWriter.close();
                mfileWriter = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mFileInfofd != null){
            try {
                Log.d(TAG,"close info file...");
                mFileInfofd.close();
                mFileInfofd = null;
            } catch (Exception e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
        if(InputStream !=null){
            try {
                InputStream.close();
                InputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void getFirstAndLastPts(long[] ptsData) {
        ParcelFileDescriptor fileInfofd =null;
        FileDescriptor fd = null;//;pfd.getFileDescriptor();
        BufferedReader reader = null;
        long firstPts=0,lastPts=0;

        File file = new File(fileInfoPath);

        try {
            fileInfofd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_CREATE|ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

        if(fileInfofd != null){
            fd = mFileInfofd.getFileDescriptor();
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(fd)));
                String line;
                boolean firstLineFound = false;

                while ((line = reader.readLine()) != null) {

                    int idx = line.indexOf("I-Pts:");
                    if (idx != -1) {
                        int endIdx = line.indexOf("\t", idx);
                        String ptsStr;
                        if (endIdx != -1) {
                            ptsStr = line.substring(idx + 6, endIdx);
                        } else {
                            ptsStr = line.substring(idx + 6);
                        }
                        long pts = Long.parseLong(ptsStr.trim());
                        if (!firstLineFound) {
                            firstPts = pts;
                            firstLineFound = true;
                        }
                        lastPts = pts;
                    }
                }
                ptsData[0]=firstPts;
                ptsData[1]=lastPts;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close(); 
                        fileInfofd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*
    void savePtsData(long A, long B, String ptsFilePath) {
        File file = new File(ptsFilePath);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
            String line = "Head:" + A + "\tTail:" + B + "\n";
            Log.d(TAG,"Write String="+line+" ptsFilePath="+ptsFilePath);
            writer.write(line);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    */

    public  void initFile(){
        if(writeFile==null) {
            //Log.d(TAG, "openFileWrite  WriteFile==null");
            //filePath = RtkApplication.getDVRRecorderFilePath(mRecordMode);
            writeFile = new File(filePath);
        }
        if(writeFile.exists()){
            //Log.d(TAG, "remove the old file...  filePath =" + filePath);
            writeFile.delete();

            try {
                writeFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(InputStream ==null) {
            try {
                //Log.d(TAG, "openFileRead  InputStream ==null");
                InputStream = new RandomAccessFile(writeFile,"rws");
                //InputStream =new BufferedOutputStream(new FileOutputStream(writeFile),4042754);//1024*1024*4 -（1024*1024*4）%188
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void  openFileWrite(byte b[],long Offset,int len) {
        try {
            if(InputStream!=null)
                InputStream.write(b,0,len);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startDvrRecorder(){
        //START DVR
        Log.d(TAG,"startDvrRecorder");
        mTimeShiftrecordingFileSize =0;
        mDvrRecorder.start();
        mDvrRecorder.flush();
        if(mRecordMode == RECORDERMODE_TIMEShIFT){
            mTimeshiftRecStartSec = new AtomicInteger(0);
            mTimeshiftRecEndSec = new AtomicInteger(0);
        }
        mRecordSec = new AtomicInteger(0);
        Log.d(TAG,"MAX_SIZE_PER_MINUTE="+MAX_SIZE_PER_MINUTE+" TIMESHIFT_SIZE_MINUTE="+TIMESHIFT_SIZE_MINUTE+" MAX_FILE_NUM="+MAX_FILE_NUM);
        //start write ...
        ReadThread readThread = new ReadThread();
        Thread r1 = new Thread(readThread);
        r1.start();
    }

    public void stopDVRRecorder()
    {
        Log.d(TAG, "stopDVRRecorder");
        dvrStopLock.lock();
        dvrStop =true;
        dvrStopLock. unlock();
        if(mPfd != null){
            try {
                Log.d(TAG,"stop playerStop  file...");
                mPfd.close();
            } catch (Exception e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
        mTimeShiftrecordingFileSize =0;
        if(mDvrRecorder !=null){
            mDvrRecorder.stop();
            mDvrRecorder.close();
            mDvrRecorder =null;
        }
        if(InputStream !=null){
            try {
                Log.d(TAG,"InputStream close...");
                InputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mRecordMode == RECORDERMODE_TIMEShIFT){
            DvrRecorderController.mLockFileIndexLock.lock();
            mInfosFileIndexList = null;
            mFileIndexMapTime = null;
            DvrRecorderController.mLockFileIndexLock.unlock();
            //delete file
            File originalFile = new File(originalFilePath);
            String fileFolder = originalFile.getParent();//"/data/vendor/dtvdata/Records";//RtkApplication.getDVRRecorderFolder(mRecordMode);

            File file = new File(fileFolder);
            if(file.exists()){
                deleteFolder(file);
            }
        }
        /*
        if(mRecordMode == RECORDERMODE_RECORD){
            long firstPts=0,lastPts=0;
            long[] ptsData = new long[2];
            String filePath = originalFilePath + "_PTS.test";
            getFirstAndLastPts(ptsData);
            savePtsData(ptsData[0],ptsData[1],filePath);
        }
        */
    }

    public static long gettime() {
        return Instant.now().getEpochSecond(); // 回傳當前 UTC 時間（秒）
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "ReadThread Recorder start..." );
            long  ret = -1;
            int count=0,sleepTime=0;
            mStartRecTime=gettime();
            while (!dvrStop) {
                count=count+sleepTime;
                sleepTime=mSleepTime;
                if(count >= 1000) {
                    mCurrentTime = gettime();
                    mRecordSec.set((int) (mCurrentTime - mStartRecTime));
                    Log.d(TAG,"ReadThread mTunerId="+mTunerId+"mRecordSec="+mRecordSec.get()+" mDurationSec="+mDurationSec);
                    if ((mRecordMode == RECORDERMODE_RECORD) && (mDurationSec > 0) && (mRecordSec.get() >= mDurationSec)) {
                        //mDurationSec = mDurationSec * 2; //Avoid sending callbacks all the time
                        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_RECORDING_COMPLETED, mTunerId, 0, null);
                        while (!dvrStop) {
                            Log.d(TAG, "ReadThread PESI_SVR_EVT_PVR_RECORDING_COMPLETED");
                            try {
                                Thread.currentThread().sleep(100);////40);
                            } catch (InterruptedException e) {
                                // TODO: handle exception
                            }
                        }
                        break;
                    }

                    if(mRecordMode == RECORDERMODE_TIMEShIFT) {
                        mTimeshiftRecEndSec.set(mRecordSec.get());
                        if(mTimeshiftRecEndSec.get() > (TIMESHIFT_SIZE_MINUTE*60)){
                            mTimeshiftRecStartSec.set(mTimeshiftRecEndSec.get() - (TIMESHIFT_SIZE_MINUTE*60));
                        }
                        Log.d(TAG,"ReadThread ReadThread mTimeshiftRecStartSec="+mTimeshiftRecStartSec.get()+" mTimeshiftRecEndSec="+mTimeshiftRecEndSec.get());
                    }
                    count=0;
                }
                dvrStopLock.lock();
                //Log.d(TAG," ReadThread start mDataReady =" + mDataReady+" mSleepTime =" +mSleepTime);
                //Log.d(TAG,"ReadThread, mDataReady="+mDataReady);
                if(!dvrStop && mDvrRecorder != null && mDataReady){
                    if(mRecordMode == RECORDERMODE_TIMEShIFT && mInfosFileIndexList!=null && recordingSize >= FileSizeCalculator(mInfosFileIndexList.size())){
                        try {
                            Log.d(TAG,"ReadThread mInfosFileIndexList.size()="+mInfosFileIndexList.size());
                            Log.d(TAG,"ReadThread FileSizeCalculator(mInfosFileIndexList.size())="+FileSizeCalculator(mInfosFileIndexList.size()));
                            //mTimeShiftrecordingFileSize += recordingSize;
                            Log.d(TAG,"ReadThread recordingSize="+recordingSize+" mTimeShiftrecordingFileSize"+mTimeShiftrecordingFileSize);
                            closeFile();
                            firstRemoveFile();
                            mTimeShiftrecordingFileSize += recordingSize;
                            mDvrRecorder.stop();
                            //firstRemoveFile();
                            //closeFile();
                            openRecorderFile();
                            //mTimeShiftrecordingFileSize += recordingSize;
                            mDvrRecorder.start();
                            DvrRecorderController.mLockFileIndexLock.lock();
                            mInfosFileIndexList.add(mCurrentFileIndex);
                            DvrRecorderController.mLockFileIndexLock.unlock();
                            mFileIndexMapTime.put(mCurrentFileIndex,System.currentTimeMillis());
                            Log.d(TAG,"ReadThread mCurrentFileIndex="+mCurrentFileIndex+" mInfosFileIndexList.size()="+mInfosFileIndexList.size());
                            Log.d(TAG,"ReadThread mFileIndexMapTime="+mFileIndexMapTime);
                            recordingSize = 0;
                            //Log.d(TAG, "mCurrentFileIndex="+mCurrentFileIndex);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(mRecordMode == RECORDERMODE_RECORD && recordingSize >= MAX_RECORD_FILE_SIZE){
                        try {
                            closeFile();
                            mDvrRecorder.stop();
                            //closeFile();
                            openRecorderFile();
                            mDvrRecorder.start();
                            recordingSize = 0;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ret = mDvrRecorder.write(readSize);
                    if(ret <= 0){
                        Log.w(TAG, "ReadThread mDvrRecorder.write(), ret="+ret);
                    }
                    else{
                    recordingSize += ret;
                    totalRecordingSize += ret;
                    }
                }
                dvrStopLock. unlock();
                try {
                    Thread.currentThread().sleep(sleepTime);////40);
                } catch (InterruptedException e) {
                    // TODO: handle exception
                }
            }
            Log.d(TAG,"ReadThread Recorder exit..." );
        }
    }

    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        try {
            if(folder.isFile())
                folder.delete();
        }catch(Exception e){
            Log.d(TAG, "delete fail ");
        }
    }


    public static Boolean checkRecorderPath(String fullFilePath,int mode){

        String filePath = fullFilePath;
        String fileInfoPath = null;
        String fileFolder;

        int lastSlashIndex = fullFilePath.lastIndexOf('/'); // 找到最後一個 '/'
        if (lastSlashIndex != -1) {
            fileFolder = fullFilePath.substring(0, lastSlashIndex); // 擷取到最後一個 '/' 之前的部分
            Log.d(TAG,"fileFolder = "+fileFolder);
        }
        else {
            return false;
        }
        //check dir
        File folder = new File(fileFolder);
        if(!folder.exists()){
            try {
                Log.d(TAG, "make Folder");
                folder.mkdir();
            } catch (Exception e) {
                Log.e(TAG, "Folder creat fail ");
                return false;
            }
        }else {
            if(mode == RECORDERMODE_TIMEShIFT){
                Log.d(TAG,"delete Folder");
                deleteFolder(folder);
                // file.mkdir();
            }
        }

        File file = new File(filePath);
        file.length();
        if(file.exists()){

        }else{
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        fileInfoPath =filePath+DvrRecordInfo.FileInfoExtension;
        file = new File(fileInfoPath);
        if (file.exists()) {
            file.delete();
            Log.d(TAG, "delete file");
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
