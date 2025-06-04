package main;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Decryption {

	/**
	 * Has five steps:
	 * 1. Decode base64 encrypted data
	 * 2. Extract IV (first 16 bytes)
	 * 3. Extract encrypted data
	 * 4. Initialize Cipher for decryption
	 * 5. Decrypt and return string
	 * @param strToDecrypt
	 * @param secretKey
	 * @param salt
	 * @return
	 */
	public static String strDecryption(String strToDecrypt, SecretKey secretKey, byte[]salt) {
		byte[] decryptedText = null;
		try {
			byte[] encryptedData = Base64.getDecoder().decode(strToDecrypt);
			byte[] vector = Arrays.copyOfRange(encryptedData,0,16);
			IvParameterSpec ivspec = new IvParameterSpec(vector);
			byte[] cipherText = Arrays.copyOfRange(encryptedData,16,encryptedData.length);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE,secretKey,ivspec);
			decryptedText = cipher.doFinal(cipherText);
			return new String(decryptedText,StandardCharsets.UTF_8);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] byteDecryption(byte[] byteToDecrypt, SecretKey secretKey) {
		try {
			byte[] vector = Arrays.copyOfRange(byteToDecrypt,0,16);
			IvParameterSpec ivspec = new IvParameterSpec(vector);
			byte[] cipherText = Arrays.copyOfRange(byteToDecrypt,16,byteToDecrypt.length);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE,secretKey,ivspec);
			byte[] decryptedText = cipher.doFinal(cipherText);
			return decryptedText;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] decryptAES(byte[] data, SecretKey aesKey, byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec vectorSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE,aesKey,vectorSpec);
			return cipher.doFinal(data);
		} 
		
		catch (Exception e) {	
			e.printStackTrace();
		} 
		return null;
	}
	
	public static byte[] decryptAESKeyWithRSA(byte[] encryptedAESKey, PrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE,privateKey);
			return cipher.doFinal(encryptedAESKey);
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
