package com.prime.homeplus.tv.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class VoiceSearchManager {

    private static final String TAG = "VoiceHelper";
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isListening = false;

    // 定義一個介面回傳結果給 Activity
    public interface VoiceCallback {
        void onResult(String text);
        void onError(String errorMsg);
        void onListeningStart();
        void onListeningEnd();
    }

    private VoiceCallback callback;

    public VoiceSearchManager(Context context, VoiceCallback callback) {
        this.context = context;
        this.callback = callback;
        initSpeechRecognizer();
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "準備好接收語音...");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "檢測到聲音輸入...");
                    callback.onListeningStart();
                }

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "說話結束，正在分析...");
                    isListening = false;
                    callback.onListeningEnd();
                }

                @Override
                public void onError(int error) {
                    String msg = getErrorText(error);
                    Log.e(TAG, "辨識錯誤: " + msg);
                    isListening = false;
                    // 有些錯誤(如 No Match)可以忽略，視需求決定是否 callback
                    callback.onError(msg);
                }

                @Override
                public void onResults(Bundle results) {
                    isListening = false;
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0); // 取信心度最高的第一個結果
                        Log.d(TAG, "辨識成功: " + text);
                        callback.onResult(text);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {}

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });

            // 設定 Intent 參數
            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            // speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-TW"); // 若需要強制指定中文
        } else {
            Log.e(TAG, "此裝置不支援 SpeechRecognizer Service");
        }
    }

    public void startListening() {
        if (speechRecognizer == null) return;

        // 確保在主執行緒呼叫
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!isListening) {
                try {
                    speechRecognizer.startListening(speechIntent);
                    isListening = true;
                    Log.d(TAG, "Start Listening Called");
                } catch (Exception e) {
                    Log.e(TAG, "啟動失敗", e);
                }
            }
        });
    }

    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER: return "error from server";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input";
            default: return "Unknown error";
        }
    }
}