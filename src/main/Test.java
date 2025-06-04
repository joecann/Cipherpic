package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.imageio.ImageIO;

public class Test {

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		BufferedImage img = ImageIO.read(new File("/home/joe-root/Pictures/dog.jpeg")); // 
		
		KeyPair keyPair = KeyUtility.generateKeyPair();
		PrivateKey privateKey = KeyUtility.generatePrivateKey(keyPair);
		PublicKey publicKey = KeyUtility.generatePublicKey(keyPair);
		BufferedImage encodeWithKey = Encode.encodeImageWithKey(img, "Hello world", publicKey, privateKey); //
		String messageFromKey = Decode.decodeImageWithKey(encodeWithKey,privateKey);
		System.out.println("KEY: " + messageFromKey);
		
		BufferedImage encodeWithPassword = Encode.encodeImageWithPassword(img, "Hello world","password");
		String messageFromPassword = Decode.decodeImageWithPassword(encodeWithPassword, "password");
		System.out.println("PW: " + messageFromPassword);
	}

}
