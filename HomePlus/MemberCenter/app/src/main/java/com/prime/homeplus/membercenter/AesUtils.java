package com.prime.homeplus.membercenter;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesUtils {
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/ECB/NoPadding";

	@SuppressLint("NewApi")
	public static String aesEncrypt(String strKey, String strValue) throws Exception {
		String result = "";
		SecretKeySpec mSecretKeySpec = null;
		Cipher mCipher = null;
		int keyLength = strKey.length();
		byte[] mArrayByte = Arrays.copyOf(strKey.getBytes(StandardCharsets.ISO_8859_1), keyLength);
		try {
			mSecretKeySpec = new SecretKeySpec(mArrayByte, ALGORITHM);
			mCipher = Cipher.getInstance(TRANSFORMATION);
			mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec);
			byte[] mArrayByteTmp = mCipher.doFinal(strValue.getBytes(StandardCharsets.UTF_8));
			result = Base64.getEncoder().encodeToString(mArrayByteTmp);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String byteArr2HexStr(byte[] arrB) throws Exception {
		int iLen = arrB.length;
		StringBuffer sb = new StringBuffer(iLen * 2);

		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			if (intTmp < 16) {
				sb.append("0");
			}

			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	private static Key getKey(byte[] arrBTmp) throws Exception {
		byte[] arrB = new byte[8];
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}

		Key key = new javax.crypto.spec.SecretKeySpec(arrB, "AES");
		return key;
	}
}
