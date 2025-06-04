package main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtility {
	
	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return generator.generateKeyPair();
	}
	
	public static PublicKey generatePublicKeyWithFile(KeyPair keypair, String path) {
		PublicKey publicKey = keypair.getPublic();
		try {	
			String pem = "-----BEGIN PUBLIC KEY-----\n" + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "\n-----END PUBLIC KEY-----";
			Files.write(Paths.get(path),pem.getBytes());
			System.out.println("KEY saved: " + path);
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}	
		return publicKey;
	}
	
	public static PublicKey generatePublicKey(KeyPair keypair) {
		PublicKey publicKey = keypair.getPublic();
		return publicKey;
		
	}
	
	public static PrivateKey generatePrivateKeyWithFile(KeyPair keypair, String path) {
		PrivateKey privateKey = keypair.getPrivate();
		try {
			String pem = "-----BEGIN PRIVATE KEY-----\n" + Base64.getEncoder().encodeToString(privateKey.getEncoded()) + "\n-----END PRIVATE KEY-----";
			Files.write(Paths.get(path),pem.getBytes());
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
		return privateKey;
	}
	
	public static PrivateKey generatePrivateKey(KeyPair keypair) {
		return keypair.getPrivate();
	}
	
	
	public static PublicKey loadPublicKeyFile(String path) {
		try {
			String pem = new String(Files.readAllBytes(Paths.get(path)))
					.replace("-----BEGIN PUBLIC KEY-----","")
					.replace("-----END PUBLIC KEY-----","")
					.replaceAll("\\s+","");
			byte[] decoded = Base64.getDecoder().decode(pem);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			return KeyFactory.getInstance("RSA").generatePublic(spec);
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	public static PrivateKey loadPrivateKeyFile(String path) {
		try {
			String pem = new String(Files.readAllBytes(Paths.get(path)))
					.replace("-----BEGIN PRIVATE KEY-----","")
					.replace("-----END PRIVATE KEY-----","")
					.replaceAll("\\s+","");
			byte[] decoded = Base64.getDecoder().decode(pem);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			return KeyFactory.getInstance("RSA").generatePrivate(spec);
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public static PrivateKey bytesToPrivatekey(byte[] bytes) {
		try {
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
			return KeyFactory.getInstance("RSA").generatePrivate(spec);
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	

}
