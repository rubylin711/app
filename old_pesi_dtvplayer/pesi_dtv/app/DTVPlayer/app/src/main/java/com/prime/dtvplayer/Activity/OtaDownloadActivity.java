package com.prime.dtvplayer.Activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.OtaDownloadAlarmDialogView;

public class OtaDownloadActivity extends DTVActivity {
    private static final String TAG = "OtaDownloadActivity";
    //private Context mContext = null;
    //private DTVActivity mDtv;
    ActivityTitleView title;
    private Button okBTN;
    private TextView stbModelTxv;
    private TextView statusTxv;
    private TextView statusValueTxv;
    private TextView progressValueTxv;

    private String urlString = "http://10.1.4.70:8082/2.zip";//eric lin test
    //"https://www.w3schools.com/html/html_examples.asp";
    private DownloadManager downloadManager = null;
    private DownloadCompleteReceiver receiver = null;
    private long downloadId = 0;
    private DownloadManager.Request request;
    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private ProgressBar strengthBar ;
    Handler CheckStatusHandler=null;
    private int downloadState=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ota_download);

        Log.d(TAG, "onCreate: BK1");
        InitTitle();
        Log.d(TAG, "onCreate: BK2");
        ota_dialog_init();
        Log.d(TAG, "onCreate: BK3");
        initData();
    }

    private void InitTitle() {
        Log.d(TAG, "InitTitleHelp: ");
        title = (ActivityTitleView) findViewById(R.id.otaDownloadTitleLayout);
        title.setTitleView(getString(R.string.STR_OTA_DOWNLOAD_TITLE));
    }

    private void ota_dialog_init()
    {

        //Scoty 20180614 modify ota update dialog -s
        stbModelTxv = (TextView) findViewById(R.id.idStbModel);
        statusTxv = (TextView) findViewById(R.id.idStatus);
        stbModelTxv.setText(getString(R.string.STR_STB_MODEL).concat(":"));
        statusTxv.setText(getString(R.string.STR_STATUS).concat(":"));

        statusValueTxv = (TextView) findViewById(R.id.idStatusValue);
        progressValueTxv = (TextView) findViewById(R.id.idProgressValue) ;
        strengthBar = (ProgressBar)findViewById(R.id.idProgressBar) ;


//        InitListener();
//        tpidEDV.setOnClickListener(stopKeyboardOnClickListener);
//        tpidEDV.setOnKeyListener(tpIdOnKeyListener);
//        tpidEDV.setOnFocusChangeListener(tpIdOnFocusChangeListener);
//        freqEDV.setOnClickListener(stopKeyboardOnClickListener);
//        freqEDV.setOnKeyListener(freqOnKeyListener);
//        freqEDV.setOnFocusChangeListener(freqOnFocusChangeListener);
//        symbolEDV.setOnClickListener(stopKeyboardOnClickListener);
//        symbolEDV.setOnKeyListener(srOnKeyListener);
//        symbolEDV.setOnFocusChangeListener(srOnFocusChangeListener);



//        cancelBTN = (Button) window.findViewById(R.id.otadialogcancelBTN);
//        cancelBTN.setOnClickListener(new OtaUpdateDialogView.onCancelclick());
        okBTN = (Button) findViewById(R.id.idManualUpdateBtn);
        okBTN.setOnClickListener(new onOkclick());

        CheckStatusHandler = new Handler();
//        okBTN.setOnClickListener(new OtaUpdateDialogView.onOkclick());
        //Scoty 20180614 modify ota update dialog -e

        //get default value
//        mTpId = tpidEDV.getText().toString();
//        mFrequency = freqEDV.getText().toString();
//        if(mTunerType == TpInfo.DVBC || mTunerType == TpInfo.DVBS) {
//            mSymbolRate = symbolEDV.getText().toString();
//        }

    }

    class onOkclick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(downloadId != 0) {
                Log.d(TAG, "onClick: downloadId="+downloadId+", remove it");
//                downloadManager.remove(downloadId);
                return;
            }else
                CheckStatusHandler.post(CheckStatusRunnable);
            downloadId = downloadManager.enqueue(request);
            Log.d(TAG, "onClick: get downloadId="+downloadId);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(downloadId != 0) {
                    new SureDialog(OtaDownloadActivity.this) {
                        public void onSetMessage(View v) {
                            ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_DOWNLOAD));
                        }

                        public void onSetNegativeButton() {
                            Log.d(TAG, "onKeyDown: onSetNegativeButton");
                        }

                        public void onSetPositiveButton() {
                            Log.d(TAG, "onKeyDown: downloadId="+downloadId+", remove it");
                            downloadManager.remove(downloadId);
                            finish();
                        }
                    };
                }
                Log.d(TAG, "onKeyDown : KEYCODE_BACK");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获得广播的频道来进行判断是否下载完毕
            if (intent.getAction().equals(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                long loadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Log.d("jim test", "loadId   " + loadId + "   downloadId   " + downloadId);
                if (loadId == downloadId) {

                    Log.d(TAG, "onReceive: complete");


                }
            }
        }
    }

    private void initData(){
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        receiver = new DownloadCompleteReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)){
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            //imageView.setImageURI(Uri.parse(uriString));
                            Log.d(TAG, "onReceive: TOST download success");
                            Toast.makeText(OtaDownloadActivity.this, "download success", Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d(TAG, "onReceive: TOST download NOT success");
                            Toast.makeText(OtaDownloadActivity.this, "download NOT　success", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        request = new DownloadManager.Request(Uri.parse(urlString));
        //request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "1.apk");

    }


    //UI--start
    /**
     * 查询下载进度
     * @param requestId
     *
     * DownloadManager.COLUMN_TOTAL_SIZE_BYTES  下载文件的大小（总字节数）
     * DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR 当前下载文件的字节数
     */
    public void queryDownloadProgress(long requestId,DownloadManager  downloadManager){
        DownloadManager.Query query=new DownloadManager.Query();
        query.setFilterById(requestId);
        try {
            boolean isGoging=true;
            {//while (isGoging) {
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int state = cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    Log.d(TAG, "queryDownloadProgress: TOST state="+state);
                    downloadState = state;
                    switch (state) {
                        case DownloadManager.STATUS_SUCCESSFUL://下载成功
                            isGoging=false;
                            handler.obtainMessage(
                                    downloadManager.STATUS_SUCCESSFUL).sendToTarget();//发送到主线程，更新ui
                            strengthBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                            strengthBar.setProgress(100);
                            progressValueTxv.setText(getString(R.string.STR_ONE_HUNDRED_PERCENT));
                            statusValueTxv.setText("SUCCESSFUL");
                            removeCallback();
                            break;
                        case DownloadManager.STATUS_FAILED://下载失败
                            isGoging=false;
                            handler.obtainMessage(downloadManager.STATUS_FAILED).sendToTarget();
                            statusValueTxv.setText("FAILED");
                            removeCallback();
                            //发送到主线程，更新ui
                            break;

                        case DownloadManager.STATUS_RUNNING://下载中
                            /**
                             * 计算下载下载率；
                             */
                            int totalSize = cursor.getInt(
                                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                            int currentSize = cursor.getInt(  cursor.getColumnIndex(
                                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            int progress = (int) (((float) currentSize) /
                                    ((float) totalSize) * 100);
                            handler.obtainMessage(downloadManager.STATUS_RUNNING,
                                    progress).sendToTarget();//发送到主线程，更新ui
                            //Log.d(TAG, "handleMessage: tost STATUS_PENDING BK1 progress="+progress);//eric lin test
                            strengthBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                            strengthBar.setProgress(progress);
                            progressValueTxv.setText(Integer.toString(progress)+getString(R.string.STR_PERCENT));
                            statusValueTxv.setText("RUNNING");
                            break;

                        case DownloadManager.STATUS_PAUSED://下载停止
                            handler.obtainMessage(DownloadManager.STATUS_PAUSED).sendToTarget();
                            statusValueTxv.setText("PAUSED");
                            removeCallback();
                            break;

                        case DownloadManager.STATUS_PENDING://准备下载
                            handler.obtainMessage(DownloadManager.STATUS_PENDING).sendToTarget();
                            statusValueTxv.setText("PENDING");
                            break;
                    }
                }
                if(cursor!=null){
                    cursor.close();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /*handler更新ui*/
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    //downloadDialog.setProgress(100);
                    //cancleDialog();
                    Log.d(TAG, "handleMessage: tost STATUS_SUCCESSFUL");//eric lin test

                    // Edwin 20190509 fix dialog not focus -s
                    final OtaDownloadAlarmDialogView tmpBookAlarmDialog = new OtaDownloadAlarmDialogView(OtaDownloadActivity.this);
                    //tmpBookAlarmDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run () {
                            tmpBookAlarmDialog.show();
                        }
                    }, 150);
                    // Edwin 20190509 fix dialog not focus -e

                    removeCallback();
                    break;
                case DownloadManager.STATUS_RUNNING:
                    //downloadDialog.setProgress((int) msg.obj);
                    //Log.d(TAG, "handleMessage: tost STATUS_RUNNING");//eric lin test
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.d(TAG, "handleMessage: tost STATUS_FAILED");//eric lin test
                    //cancleDialog();
                    break;
                case DownloadManager.STATUS_PENDING:
                    Log.d(TAG, "handleMessage: tost STATUS_PENDING");//eric lin test
                    //showDialog();
                    break;
            }
            return false;
        }
    });

    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            Log.d(TAG, "run:     Auto  CheckStatusRunnable !!!!");
            queryDownloadProgress(downloadId,downloadManager);
            CheckStatusHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };

    private void removeCallback(){
        CheckStatusHandler.removeCallbacks(CheckStatusRunnable);
    }
    //UI-end
}
