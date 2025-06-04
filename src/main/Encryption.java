package main;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

	public static byte[] generateSalt() {
		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		return salt;
	}

	public static SecretKey getKeyFromPassword(String strToEncrypt, byte[] salt) {
		SecretKey secretKey = null;
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(strToEncrypt.toCharArray(), salt, 65535, 256);
			secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		}

		catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				e.printStackTrace();
		}
		return secretKey;

	}
	
	public static String strEncryption(String str, SecretKey secretKey) {
		StringBuilder encryptedStr = new StringBuilder();
		byte[] encryptedWithInitializationVector = null;

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// Generate a random 16-byte initialization Vector
			byte[] vector = new byte[16];
			new SecureRandom().nextBytes(vector);
			IvParameterSpec vectorSpec = new IvParameterSpec(vector);
			// Initialization AES cipher in ENCRYPT mode
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, vectorSpec);
			// Encrypted string
			byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
			// Combine initialization vector + encrypted data and encode it to Base64
			encryptedWithInitializationVector = new byte[vector.length + encrypted.length];
			System.arraycopy(vector, 0, encryptedWithInitializationVector, 0, vector.length);
			System.arraycopy(encrypted, 0, encryptedWithInitializationVector, vector.length, encrypted.length);
			encryptedStr.append(Base64.getEncoder().encodeToString(encryptedWithInitializationVector));
			return encryptedStr.toString();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static byte[] byteEncryption(byte[] bytes, SecretKey secretKey) {
	
		byte[] encryptedWithInitializationVector = null;

		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// Generate a random 16-byte initialization Vector
			byte[] vector = new byte[16];
			new SecureRandom().nextBytes(vector);
			IvParameterSpec vectorSpec = new IvParameterSpec(vector);
			// Initialization AES cipher in ENCRYPT mode
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, vectorSpec);
			// Encrypted string
			byte[] encrypted = cipher.doFinal(bytes);
			// Combine initialization vector + encrypted data and encode it to Base64
			encryptedWithInitializationVector = new byte[vector.length + encrypted.length];
			System.arraycopy(vector, 0, encryptedWithInitializationVector, 0, vector.length);
			System.arraycopy(encrypted, 0, encryptedWithInitializationVector, vector.length, encrypted.length);
			return encryptedWithInitializationVector;
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] generateAESKey() {
		byte[] key = new byte[32];
		new SecureRandom().nextBytes(key);
		return key;
	}
	
	public static SecretKey getAESKeyFromBytes(byte[] bytes) {
		return new SecretKeySpec(bytes,"AES");
	}
	
	
	public static byte[] encryptionAES(byte[] str, SecretKey secretKey, byte[] ivOut) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] iv = new byte[16];
			new SecureRandom().nextBytes(iv);
			System.arraycopy(iv, 0, ivOut, 0, 16);
		
			IvParameterSpec vectorSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, vectorSpec);
			return cipher.doFinal(str);
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] encryptAESKeyWithRSA(byte[] aesKey, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE,publicKey);
			return cipher.doFinal(aesKey);
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
}
