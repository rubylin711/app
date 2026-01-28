package com.pesi.remote_service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class CommunicateToServer {
    private static String TAG = "CommunicateToServer" ;
    private Context mContext = null ;
    private FileUploadTask mFileUploadTask = null ;
    public CommunicateToServer(Context context, Handler uiHandler)
    {
        mContext = context ;
        mFileUploadTask = new FileUploadTask(uiHandler);
    }

    public void execute( File file, String serverUrl )
    {
        mFileUploadTask.execute(file, serverUrl, Locale.getDefault().getLanguage()) ;
    }
    private class FileUploadTask extends AsyncTask<Object, Void, String> {
        private Handler mTaskUiHandler;
        public FileUploadTask( Handler uiHandler)
        {
            mTaskUiHandler = uiHandler ;
        }
        @Override
        protected String doInBackground(Object... params) {
            String response = null;
            Log.d( TAG, "send file to server" ) ;
            try {
                File file = (File) params[0];
                String serverUrl = (String) params[1];
                String deviceLanguage = (String) params[2];
                Log.d( TAG, "deviceLanguage:" + deviceLanguage ) ;
                // 创建URL对象
                URL url = new URL(serverUrl);

                // 创建HttpURLConnection对象
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // 设置请求方法为POST
                connection.setRequestMethod("POST");

                // 允许输入输出流
                connection.setDoInput(true);
                connection.setDoOutput(true);

                // 添加设备语言到请求头
                connection.setRequestProperty("Device-Language", deviceLanguage);

                // 设置内容类型为二进制
                connection.setRequestProperty("Content-Type", "application/octet-stream");

                // 创建数据输出流
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // 读取文件并写入输出流
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                Log.d( TAG, "send file to server finish" ) ;

                // 获取服务器响应
                InputStream inputStream = connection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                int bytesReadResponse;
                while ((bytesReadResponse = inputStream.read(buffer)) != -1) {
                    stringBuilder.append(new String(buffer, 0, bytesReadResponse));
                }
                response = stringBuilder.toString();
                Log.d( TAG, "get server response " + response ) ;
                // 关闭连接
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // 处理服务器响应
            Log.d(TAG, "Server Response: " + result);
            if ( mContext != null )
            {
                Message message = new Message() ;
                message.what = RcuVoiceBluetoothService.SERVER_RESPONSE;
                message.obj = result ;
                mTaskUiHandler.sendMessage(message);
            }
        }
    }
}
