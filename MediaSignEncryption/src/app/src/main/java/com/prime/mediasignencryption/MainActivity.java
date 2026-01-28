package com.prime.mediasignencryption;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int queryReturnOk = 0;
    private static final int queryUpdateFile = 1;
    private static final int queryUpdateDir = 2;
    private Context mContext;
    private String mSelectDir = null;
    private List<String> mSelectMediaPaths = new ArrayList<>();
    // Create a Handler for the main thread
    Handler handler = new Handler(Looper.getMainLooper());

//    HandlerThread handlerThread = new HandlerThread("MyHandlerThread");

    Button mBtnUpdateLocalPath;
    Button mBtnSignAndVerify;
    TextView mInfos;
    ScrollView mScrollInfos;
    /*EditText filePath;
    Button btnCreate;
    Button btnSign;
    Button btnVerify;
    Button btnClear;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mContext = this;

        mBtnUpdateLocalPath = findViewById(R.id.btn_update_local_path);
        mBtnSignAndVerify = findViewById(R.id.btn_sign_and_verify);
        mInfos = findViewById(R.id.infos);
        mScrollInfos = findViewById(R.id.infos_scroll);
        /*filePath = findViewById(R.id.editText);
        btnCreate = findViewById(R.id.create_keys_btn);
        btnSign = findViewById(R.id.sign_btn);
        btnVerify = findViewById(R.id.verify_btn);
        btnClear = findViewById(R.id.clear_btn);

        btnCreate.setOnClickListener(createListener);
        btnSign.setOnClickListener(signListener);
        btnVerify.setOnClickListener(verifyListener);
        btnClear.setOnClickListener(clearListener);*/

        mBtnUpdateLocalPath.setOnClickListener(updateLocalPathListener);
        mBtnSignAndVerify.setOnClickListener(signAndVerifyListener);

        mBtnSignAndVerify.setEnabled(false);
        mInfos.setText(getResources().getString(R.string.instructions_for_use));

        /*handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                signAndVerify();
            }
        }, 5000);*/
    }

    /*private final View.OnClickListener createListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            List<String> keys = ReadWriteKeys.readKeys(ReadWriteKeys.KEY_TYPE_PUBLIC_AND_PRIVATE);
            if (keys.isEmpty())
            {
                keys = SignatureAlgorithm.createKeys();
                ReadWriteKeys.writeKeys(keys, ReadWriteKeys.KEY_TYPE_PUBLIC_AND_PRIVATE);
            }
            String publicKey = keys.get(0);
            String privateKey = keys.get(1);

            System.out.println("publicKey: " + publicKey + "\n");
            System.out.println("privateKey: " + privateKey + "\n");

            keyInfos.append("publicKey:" + publicKey + "\n");
            keyInfos.append("privateKey:" + privateKey + "\n");
        }
    };

    private final View.OnClickListener signListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            List<String> keys = ReadWriteKeys.readKeys(ReadWriteKeys.KEY_TYPE_PUBLIC_AND_PRIVATE);
            Log.d(TAG, "onClick: keys size:" + keys.size());
            List<String> sign = SignatureAlgorithm.getSign(filePath.getText().toString(), keys.get(1));
            ReadWriteKeys.writeKeys(sign, ReadWriteKeys.KEY_TYPE_SIGN);
            keyInfos.append("sign:" + sign.get(0) + "\n");
        }
    };

    private final View.OnClickListener verifyListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            // Run a block of code on the main thread
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //63.mp4
                    List<String> keys = ReadWriteKeys.readKeys(ReadWriteKeys.KEY_TYPE_PUBLIC_AND_PRIVATE);
                    List<String> sign = ReadWriteKeys.readKeys(ReadWriteKeys.KEY_TYPE_SIGN);
                    for (int count = 0; count < sign.size(); count++) {
                        boolean isVerified = SignatureAlgorithm.verifyFiles(filePath.getText().toString(),sign.get(count),keys.get(0));
                        keyInfos.append("isVerified: " + isVerified + "\n");
                    }
                    filePath.setText("");
                }
            });

        }
    };

    private final View.OnClickListener clearListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            keyInfos.setText("");
        }
    };*/

    private final View.OnClickListener updateLocalPathListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Intent intent0 = new Intent(mContext, FileSelector.class);
//            Activity activity = this;
            startActivityForResult(intent0, queryUpdateDir);
//            startActivity(intent0);
        }
    };

    private final View.OnClickListener signAndVerifyListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: mSelectFiles.size:" + mSelectMediaPaths.size());
            if (!mSelectMediaPaths.isEmpty()) {
                mSelectMediaPaths.clear();
                Log.d(TAG, "onClick: mSelectFiles.size:" + mSelectMediaPaths.size());
            }

            File dir = new File(mSelectDir);

            getAllFilesInDir(dir);
            for (int i = 0; i < mSelectMediaPaths.size(); i++) {
                String mediaPath = mSelectMediaPaths.get(i);
                Log.d(TAG, "onClick: media path:" + mediaPath/*.getPath()*/);
                //create sign and store to sign file
                String sign = SignatureAlgorithm.getSign(mediaPath);
                ReadWriteSignature.writeSign(sign, mediaPath);

                //verify
                String signPath = ReadWriteSignature.getDefaultSignPath(mediaPath);
                boolean resultWithOneInput = SignatureAlgorithm.isVerifies(mediaPath);
                boolean resultWithTwoInput = SignatureAlgorithm.isVerifies(mediaPath, signPath);
                String verifyResults = resultWithOneInput && resultWithTwoInput ? "verify success" : "verify fail";
                Log.d(TAG, "onClick: resultWithOneInput = " + resultWithOneInput + "\n" +
                        "resultWithTwoInput = " + resultWithTwoInput);

                mInfos.append(mSelectMediaPaths.get(i) + "\n" +
                        signPath + "\n" +
                        verifyResults + "\n\n");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollInfos.fullScroll(View.FOCUS_DOWN);
                        mBtnSignAndVerify.requestFocus();
                    }
                });
            }
        }
    };

    private void getAllFilesInDir(File dir) {
        File[] files = dir.listFiles(new FileSelector.DirAndFileFilter());
        for (int i = 0; (files != null) && (i < files.length);
             i++) {
            if (files[i].isDirectory()) {
                Log.d(TAG, "getAllFilesInDir: files[" + i + "] is Directory:" + files[i].getPath());
                getAllFilesInDir(files[i]);
            } else {
                //files.add(tempFiles[i]);
                mSelectMediaPaths.add(files[i].getPath());
                Log.d(TAG, "getAllFilesInDir: files path:" + files[i].getPath());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if ((requestCode == queryUpdateDir) &&
                    (resultCode == queryReturnOk)) {
                Bundle bundle = data.getExtras();
                String dir = bundle.getString(FileSelector.DIR);
                if (dir != null) {
                    mSelectDir = dir;
                    mInfos.append(dir + "\n");
//                    mLocalPath.setText(file.substring(file.lastIndexOf("/") + 1));
                    mBtnSignAndVerify.setEnabled(true);
                }
            }
        }
    }

    /*private void signAndVerify() {
        File file = new File("/storage/A8B7-02B0/AUTO_PLAY/");
        if (!file.exists()) return;
        String filePath1 = "/storage/A8B7-02B0/AUTO_PLAY/03_FHD_1920x1080.jpg";
        String filePath2 = "/storage/A8B7-02B0/AUTO_PLAY/40_HEVC_AAC_mp4.mp4";

        String sign1 = SignatureAlgorithm.getSign(filePath1);
        String sign2 = SignatureAlgorithm.getSign(filePath2);
        ReadWriteSignature.writeSign(sign1, filePath1);
        ReadWriteSignature.writeSign(sign2, filePath2);
        boolean result1 = SignatureAlgorithm.isVerifies(filePath1);
        Log.d(TAG, "verifyTest: isVerifies result1 = " + result1);
        boolean result2 = SignatureAlgorithm.isVerifies(filePath2);
        Log.d(TAG, "verifyTest: isVerifies result2 = " + result2);
    }*/
}

