package com.prime.homeplus.membercenter.TvMail;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.prime.homeplus.membercenter.enity.Data;

public class TvMailManager {
    private static String TAG = "TvMailManager";


    public TvMailManager() {

    }

    public static void handleTvMailInfo(final Context context, String mailId, final boolean showTitle ) {
        TvMailDbHelper tvMailDbHelper = new TvMailDbHelper(context);;
        final Data data = tvMailDbHelper.selectTvMailFromId(mailId);

        if (data.getContent().getText() != null && !data.getContent().getText().equals("")) {
            Log.d(TAG,"TvMail has text content, showTvMailDialog");
            try {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    Handler mainThread = new Handler(Looper.getMainLooper());
                    mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                            TvMailDialog tvMailDialog = new TvMailDialog(context);
                            tvMailDialog.showTvMailDialog(data, showTitle);
                            tvMailDialog = null;
                        }
                    });
                } else {
                    TvMailDialog tvMailDialog = new TvMailDialog(context);
                    tvMailDialog.showTvMailDialog(data, showTitle);
                    tvMailDialog = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Log.d(TAG,"TvMail has no text content, showTvMailPicDialog");
            try {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    Handler mainThread = new Handler(Looper.getMainLooper());
                    mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                            TvMailPicDialog tvMailPicDialog = new TvMailPicDialog(context);
                            tvMailPicDialog.showTvMailPicDialog(data, showTitle);
                            tvMailPicDialog = null;
                        }
                    });
                } else {
                    TvMailPicDialog tvMailPicDialog = new TvMailPicDialog(context);
                    tvMailPicDialog.showTvMailPicDialog(data, showTitle);
                    tvMailPicDialog = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }



}
