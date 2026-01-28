package com.prime.mediasignencryption;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class SignatureAlgorithm {
    private static final String TAG = SignatureAlgorithm.class.getSimpleName();
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhE7TP981ulqkS0TosHRfzgX5F75su7H7cNHp53W8pTrPc3OQlATGvMfbhuoXS+zqvHz+ekpNqbQ7F8VFUSCv+NpArauqxxXPP2raQzczqZRuT+3dcYEdRjZu4mTzXPPR1tj0XtQ7g9PdinqyQ6QR0LtM+uJb+npBUiD63Pu4oz4BN0/DwUbyGI8LTysCQwHlbl6yWnWMh3oBZoVLc+cUShS7Qv0EYlP18WVdUKOVEy8fcLwJOJ+WSYBheKp7x1qycJHoIQQvmg3YygHa/J/qoLu1SWDf2MwN5c8Kz/V40eIYtl8WXJ5Qfz5tAJI3WJ7bRExnqc0MWRm3LbmgRFSQIwIDAQAB";
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCETtM/3zW6WqRLROiwdF/OBfkXvmy7sftw0enndbylOs9zc5CUBMa8x9uG6hdL7Oq8fP56Sk2ptDsXxUVRIK/42kCtq6rHFc8/atpDNzOplG5P7d1xgR1GNm7iZPNc89HW2PRe1DuD092KerJDpBHQu0z64lv6ekFSIPrc+7ijPgE3T8PBRvIYjwtPKwJDAeVuXrJadYyHegFmhUtz5xRKFLtC/QRiU/XxZV1Qo5UTLx9wvAk4n5ZJgGF4qnvHWrJwkeghBC+aDdjKAdr8n+qgu7VJYN/YzA3lzwrP9XjR4hi2XxZcnlB/Pm0AkjdYnttETGepzQxZGbctuaBEVJAjAgMBAAECggEAHVh+coRHnnXMSwWVts2ZElEw1ptd8qOfaX6lqrjqhptst+bCApKsoeKSRnJViHmb9wKiTu18HE/la3hNz5i4bzG5dsvErTOhLCe/EXmE8/AAJ6pSm4JPw+r+3SgW8l8CA+1LBl18ddjgIn7kWyxg5zMj9+GNFRlYs6as/aa3ZT1h0TPuTE4ThCLOlDkr2LMRucOwHyWYc5xG6UzieYl8eY4eDKp923kHTkoaK/aXcIgdQ1A1VRczueUdmMArS9N9NRlwfQy8tY9ef8J4ryIAFNfcXtLZNVKJ4c4egiGq9Q14BOCEBY8EBLu6j05uQv1wmQRETCk1ofNoyZaM0k8bgQKBgQC62D1QFgOkMhsAsk81Q1su9rdjuHvlcE885nQT4h8MMi5N94W0JLIXtK+5d3XzfJNMl5/zMvN4VazX0YhO6KxnBoeswdFwua6/WLq2CL5UVJ+JK8X57+MEt5/80diYPEUyiqgupClEJoMTtn1zW83Qu+hS2VIooE7q4SlN3xXhWQKBgQC1RykA1pSaFWZTwKZCwETkLApSGuZ/np8ZgCQrwyogjg//NZ5LxKBE9YjUET1Z49M7nG4S1EfJrhH2LU03gdrMUdSYPvugQzSembJa4sk6CJ++Yl+5GnAqNqMk0AnEbbuqQCJjvTGsnX3Kp1V6RrguWK8VOsGV5N91/ERoyDjx2wKBgQC1KOqx1tZrAmXqE/j2pLspWu9PEdWXDYDWhH9xs+H0mkcJM4CAvL+senWS9v/3OAc90Rm13OXx++fdTXzDPZfDQLKVz0q56F9Fictmqi2YaIBSqI2JG41ayoulvvUqDe3t5kDREDwupsSafWs/SXjR9Q96HNorJ9u8otnAdhep6QKBgAkcgIUYQ/PSq4f+IN0uCMhnJkWmMHutn1WKyUah0pY8TbWIHmK0iVGt/aibDThdaRqP7EBqBwmuBB0Zfc4SasrmOgdE8yTE+/oUw5Ap4e9Wy6CoFSOanpSglmadRkBnO419F71nnv2MFOyAqJ0yvMMnlfU9NMmx7UFi4pVZqvMNAoGAP5ZkkxHrAVX5MIuBIZlr6zYiIq21p2ZYXapATVUo1b7cT8qZzAvkAzq5rvKA0J+aeZOWEow7uRkwXnJmt5WJJok3Zt1avSrS3qWOARcMlHpMwcLBrKZfBI1vswpJ8JXBoxbLZAwhjk/PdTpF0iT06jOU2NHjF1lcyWa2rqYUczA=";

    /**
     * create public and private keys
     *
     * @return 0. public key & 1. private key
     */
    protected static List<String> createKeys() {
        List<String> keys = new ArrayList<>();
        try {
            KeyPairGenerator keyPairGenerator = null;
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            // Get the public and private keys
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            Base64.Encoder encoder = Base64.getEncoder();
            keys.add(encoder.encodeToString(publicKey.getEncoded()));
            keys.add(encoder.encodeToString(privateKey.getEncoded()));
        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
            Log.e(TAG, "createKeys: NoSuchAlgorithmException", e);
        }
        return keys;
    }

    /**
     * return file sign
     *
     * @param mediaPath   pre-signed file
     * @return sign
     */
    protected static String getSign(String mediaPath) {
        String encodedSignedMessage = null;
        try (FileInputStream fis = new FileInputStream(mediaPath)) {
            long fileSize = fis.getChannel().size();
            //get real file size and then fix to byte type
            byte[] fileSizeByte = String.valueOf(fileSize).getBytes(StandardCharsets.UTF_8);
            long dataSizeMax = 100 * 1024;
            if (fileSize > dataSizeMax)
                fileSize = dataSizeMax;
            byte[] data = new byte[(int)fileSize];
            int numRead = fis.read(data);
//            System.out.println("numRead = " + numRead);
            Log.d(TAG, "getSign: numRead:" + numRead);
            fis.close();
            Log.d(TAG, "getSign: data length:" + data.length + ", bytesFileSize:" + fileSizeByte.length);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(data);
            baos.write(fileSizeByte);
            data = baos.toByteArray();
            baos.close();
            Log.d(TAG, "getSign: new data length:" + data.length);
            //byte[] content = Files.readAllBytes(Paths.get(mediaPath));
            PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(Base64.getDecoder()
                    .decode(PRIVATE_KEY.getBytes(StandardCharsets.UTF_8)));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pk = kf.generatePrivate(pkcs8);

            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(pk);
            signature.update(data);
            byte[] signedMessage = signature.sign();
            Log.d(TAG, "getSign: signedMessage = " + Arrays.toString(signedMessage));
            // Encode the signed message in Base64
            encodedSignedMessage = Base64.getEncoder().encodeToString(signedMessage);
            Log.d(TAG, "getSign: encodedSignedMessage = " + encodedSignedMessage);
            return encodedSignedMessage;
        } catch (Exception e) {
//            throw new RuntimeException(e);
            Log.e(TAG, "getSign: ", e);
            encodedSignedMessage = null;
        }
        return encodedSignedMessage;
    }

    /**
     * verify file sign success or fail
     *
     * @param mediaPath   file
     * @param sign      sign
     * @return Is this file signed with the corresponding private key
     */
    private static boolean verifyFiles(String mediaPath, String sign) {
        boolean isVerified = false;
        // Create a FileInputStream object
        try (FileInputStream fis = new FileInputStream(mediaPath)){
            long fileSize = fis.getChannel().size();
            byte[] fileSizeByte = String.valueOf(fileSize).getBytes(StandardCharsets.UTF_8);
            long dataSizeMax = 100 * 1024;
            if (fileSize > dataSizeMax)
                fileSize = dataSizeMax;
            byte[] data = new byte[(int)fileSize];
            int numRead = fis.read(data);
            Log.d(TAG, "verifyFiles: numRead:" + numRead);
            fis.close();
            Log.d(TAG, "verifyFiles: data length:" + data.length + ", bytesFileSize:" + fileSizeByte.length);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(data);
            baos.write(fileSizeByte);
            data = baos.toByteArray();
            baos.close();
            /*System.out.println("new data length:" + data.length);
            System.arraycopy(fileSizeByte, 0, data, numRead, fileSizeByte.length);
            System.out.println("numRead = " + numRead);*/

            X509EncodedKeySpec x509 = new X509EncodedKeySpec(Base64.getDecoder()
                    .decode(PUBLIC_KEY.getBytes(StandardCharsets.UTF_8)));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pk = kf.generatePublic(x509);

            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(pk);
            signature.update(data);
            isVerified = signature.verify(Base64.getDecoder().decode(sign.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            Log.e(TAG, "isVerifies: " + e);
            isVerified = false;
        }
        Log.d(TAG, "verifyFiles: result:" + isVerified);
        return isVerified;
    }

    /**
     * get sign using mediaPath
     *
     * @param mediaPath   file
     * @return get sign
     */
    public static boolean isVerifies (String mediaPath) {
        boolean result = false;
        String sign = ReadWriteSignature.readSign(mediaPath, null);
        if (sign == null || sign.isEmpty()) {
             Log.d(TAG, "isVerifies: sign is null or sign is Empty");
            return result;
        }
        result = verifyFiles(mediaPath, sign);
        return result;
    }

    /**
     * get sign using mediaPath & signPath
     *
     * @param mediaPath   file
     * @return get sign
     */
    public static boolean isVerifies (String mediaPath, String signPath) {
        boolean result = false;
        String sign = ReadWriteSignature.readSign(mediaPath, signPath);
        if (sign == null || sign.isEmpty()) {
            Log.d(TAG, "isVerifies: sign is null or sign is Empty");
            return result;
        }
        result = verifyFiles(mediaPath, sign);
        return result;
    }
}
