package com.prime.mediasignencryption;

 import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReadWriteSignature {
    private static final String TAG = ReadWriteSignature.class.getSimpleName();
    //public static final int KEY_TYPE_PUBLIC_AND_PRIVATE = 0;
    //public static final int KEY_TYPE_SIGN = 1;

    protected static String readSign(/*int type*/String mediaPath, String signPath) {
        String sign = null;
        /*File file = null;
        if (type == KEY_TYPE_PUBLIC_AND_PRIVATE) {
            file = new File("/storage/A8B7-02B0/public_and_private_keys.txt");
            if (!file.exists()) {
                //file not exist, create a new file
                try {
                    file.createNewFile();
                    System.out.println("create success!");
                } catch (IOException e) {
                    System.out.println("create fail = " + e.getMessage());
                }
            }
        }*/

        if (signPath == null || signPath.isEmpty()) {
            //else if (type == KEY_TYPE_SIGN) {
            // Log.d(TAG, "readSign: mediaPath:" + mediaPath + "\n");
            signPath = getDefaultSignPath(mediaPath);
        }

        // sign file not exist ?
        File signFile = new File(signPath);
        if (!signFile.exists()) {
            Log.d(TAG, "readSign: sign file not exist");
            return sign;
        }

        try {
            FileReader fileReader = new FileReader(signPath);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            if ((line = br.readLine()) != null) {
                sign = line;
                // Log.d(TAG, "readSign: signPath sign:" + sign);
            }
            br.close();
            fileReader.close();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            Log.e(TAG, "readSign: ", e);
        }


            //file not exist, create a new file
//            try {
//                file.createNewFile();
//                System.out.println("create success!");
//            } catch (IOException e) {
//                System.out.println("create fail = " + e.getMessage());
//            }
        //}

//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                sign = line;
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return sign;
    }

    protected static void writeSign(String sign, String mediaPath) {
        /*if (type == KEY_TYPE_PUBLIC_AND_PRIVATE) {
            file = new File("/storage/emulated/0/public_and_private_keys.txt");
            if (!file.exists()) {
                //file not exist, create a new file
                try {
                    file.createNewFile();
                    System.out.println("create success!");
                } catch (IOException e) {
                    System.out.println("create fail = " + e.getMessage());
                }
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                for (int count = 0; count < keys.size(); count++) {
                    bw.write(keys.get(count) + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (type == KEY_TYPE_SIGN) {*/
        String signPath = getDefaultSignPath(mediaPath);
        Log.d(TAG, "writeSign: signPath = " + signPath);

        File signFile = new File(signPath);
        File parentFile= signFile.getParentFile();
        if (parentFile == null)
            return;
//        Log.d(TAG, "writeSign: signFile.isDirectory() = " + signFile.isDirectory());
        Log.d(TAG, "writeSign: parentFile = " + parentFile);
        Log.d(TAG, "writeSign: parentFile.exists() = " + parentFile.exists());

        if (!parentFile.exists()) {
            boolean mkdirsResult = parentFile.mkdirs();
            Log.d(TAG, "writeSign: mkdir results:" + mkdirsResult);
            if (!mkdirsResult) {
                Log.d(TAG, "writeSign: mkdir fail");
                return;
            }
        }

        if (!signFile.exists()) {
            //file not exist, create a new file
            try {
                boolean result = signFile.createNewFile();
                if (result)
                    Log.d(TAG, "writeSign: !signFile.exists(): create signFile success!");
                else
                    Log.d(TAG, "writeSign: !signFile.exists(): create signFile fail!");
            } catch (IOException e) {
                Log.e(TAG, "writeSign: !signFile.exists(): create signFile fail = " + e.getMessage());
            }
            try (FileWriter writer = new FileWriter(signFile)) {
                writer.write(sign);
                writer.close();
                Log.d(TAG, "writeSign: !signFile.exists(): add signFile content success!");
            } catch (IOException e) {
                Log.e(TAG, "writeSign: !signFile.exists(): add signFile content fail:" + e.getMessage());
//                    throw new RuntimeException(e);
            }
        } else {
            String readSignString = readSign(mediaPath, signPath);
            if (!sign.equals(readSignString)) {
                try (FileWriter writer = new FileWriter(signFile)) {
                    writer.write(sign);
                    writer.close();
                    Log.d(TAG, "writeSign: signFile.exists(): cover signFile content success!");
                } catch (IOException e) {
                    Log.e(TAG, "writeSign: signFile.exists(): cover signFile content fail:" + e.getMessage());
//                    throw new RuntimeException(e);
                }
            }
        }

        /*boolean isExist = false;
//        for (int count = 0; count < signs.size(); count++) {
        if (sign.contains(key)) {
            isExist = true;
        }
//        }
        if (!isExist) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.write(key + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }*/
        //}


    }

    protected static String getDefaultSignPath(String mediaPath) {
        String signPath = null;
        File mediaFile = new File(mediaPath);//.mp4...etc
        if (!mediaFile.exists()) {
            Log.d(TAG, "getDefaultSignPath: mediafile is not exists!");
            return signPath;
        }

        // Log.d(TAG, "readSign: mediaFile.getPath: " + mediaFile.getPath());
        // Log.d(TAG, "readSign: mediaFile.getParent: " + mediaFile.getParent());
        // Log.d(TAG, "readSign: mediaFile.getName: " + mediaFile.getName());

        String mediaName = mediaFile.getName();//contain ex: .mp4 ...etc
        String signName = null;
        int point = mediaName.lastIndexOf(".");
        if (point != -1) {
            String fileExtension = mediaName.substring(point + 1);
            mediaName = mediaName.substring(0, point);
            signName = mediaName + "_" + fileExtension + ".sign";
        } else {
            signName = mediaName + ".sign";
        }
        Log.d(TAG, "getDefaultSignPath: signName:" + signName);
        signPath = mediaFile.getParent() + "/sign/" + signName;
        return signPath;
    }
}
