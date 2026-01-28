package com.prime.dtv.service.Util;


import com.prime.dtv.utils.LogUtils;

import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.*;

public class CryptoUtils {
    private static final String TAG = "CryptoUtils";
    //private static final String keyHex = "717dca12eedeb53e9935931f03c66dcf";
    private static final byte[] mKeyHex = {(byte)0x71, (byte)0x7d, (byte)0xca, (byte)0x12, (byte)0xee, (byte)0xde, (byte)0xb5, (byte)0x3e,
                                            (byte)0x99, (byte)0x35, (byte)0x93, (byte)0x1f, (byte)0x03, (byte)0xc6, (byte)0x6d, (byte)0xcf};
    //private static final String ivHex = "aa4d389054c53b21f404df372e5e90d9";
    private static final byte[] mIvHex = {(byte)0xaa, (byte)0x4d, (byte)0x38, (byte)0x90, (byte)0x54,(byte)0xc5, (byte)0x3b, (byte)0x21,
                                            (byte)0xf4, (byte)0x04, (byte)0xdf, (byte)0x37, (byte)0x2e, (byte)0x5e, (byte)0x90, (byte)0xd9};

    private static final String mPublicKeyStr= "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIvly5L5yG679FGe9rDnoWXQcIwJja3hOZoPk7JeB7LnYIqJRUrFesABnUKgrBpq9bjf0KXeI11UQ0sEXoPXCvA==";
    // (1) Verify ECDSA signature
    public boolean verifyECDSASignature(byte[] message, byte[] signature) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(mPublicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(message);
            return ecdsaVerify.verify(signature);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // (2) AES-128-CBC encrypt
    public byte[] encryptAES(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(mKeyHex, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(mIvHex);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] encryptedBytes = cipher.doFinal(data);
            return encryptedBytes;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // (3) AES-128-CBC decrypt
    public byte[] decryptAES(byte[] encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(mKeyHex, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(mIvHex);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            return decryptedBytes;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    public static void main(String[] args) throws Exception {
        // (1) 验证 ECDSA 签名的示例
        String publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIvly5L5yG679FGe9rDnoWXQcIwJja3hOZoPk7JeB7LnYIqJRUrFesABnUKgrBpq9bjf0KXeI11UQ0sEXoPXCvA==";
        byte[] message = "這是一段測試訊息".getBytes("UTF-8"); // 假設這是需要驗證的訊息
        byte[] signature = Base64.getDecoder().decode("Base64簽名資料");

        boolean isVerified = verifyECDSASignature(publicKeyBase64, message, signature);
        System.out.println("簽名驗證結果: " + isVerified);

        // (2) AES-128-CBC 解密的示例
        String keyHex = "717dca12eedeb53e9935931f03c66dcf"; // 16字节 (128位) 密钥
        String ivHex = "aa4d389054c53b21f404df372e5e90d9"; // 16字节 IV
        byte[] encryptedData = Base64.getDecoder().decode("Base64加密資料");

        byte[] decryptedData = decryptAES(keyHex, ivHex, encryptedData);
        System.out.println("解密結果: " + new String(decryptedData, "UTF-8"));
    }
    */

}
