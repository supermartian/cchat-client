package com.vt.chatroom;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.util.Base64;

@SuppressLint("NewApi")
public class KeyProcessor {
	private BigInteger secretval;
	private BigInteger primeval;
	private int radix;
	private static KeyProcessor instance;
	
	public static KeyProcessor getInstance() {
		if (instance == null) {
			instance = new KeyProcessor("0", "0", "0", 16);
		}
		
		return instance;
	}
	
	private KeyProcessor(String sec, String pub, String prime, int radix) {
		secretval = new BigInteger(sec, radix);
		primeval = new BigInteger(prime, radix);
		this.radix = radix;
	}
	
	public String computeSecret(String pub) {
		String ret;
		BigInteger pubInt = new BigInteger(pub, radix);
		BigInteger retInt = pubInt.modPow(secretval, primeval);
		ret = retInt.toString(radix);
		
		return ret;
	}
	
	public void setSecret(String sec) {
		secretval = new BigInteger(sec, radix);
	}
	
	public void setPrime(String prime) {
		primeval = new BigInteger(prime, radix);
	}
	
	public BigInteger getSecret() {
		return secretval;
	}
	
    public static String encryptMessage(String message, String key)
    {
    	byte[] msg = message.getBytes();
    	try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			byte[] keyb = key.getBytes("UTF-8");
			keyb = Arrays.copyOf(keyb, 16);
			SecretKeySpec secretKey = new SecretKeySpec(keyb, "AES");
			c.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.encodeToString(c.doFinal(msg), Base64.DEFAULT);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    }
    
    public static String decryptMessage(String message, String key)
    {
    	byte[] msg = Base64.decode(message, Base64.DEFAULT);
    	try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			byte[] keyb = key.getBytes("UTF-8");
			keyb = Arrays.copyOf(keyb, 16);
			SecretKeySpec secretKey = new SecretKeySpec(keyb, "AES");
			c.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(c.doFinal(msg));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return "";
    }
}
