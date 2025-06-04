package main;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.crypto.SecretKey;

public class Decode {

	public static String decodeImageWithPassword(BufferedImage image, String seedPassword) throws IOException {
		
		Set<Point> usedPixels = new HashSet<>();
		
		StringBuilder encryptedBinary = new StringBuilder();
		StringBuilder encryptedText = new StringBuilder();
		
		long SECRET_SEED = Utility.generateSeed(seedPassword);
		Random random = new Random(SECRET_SEED);
		int x = 0, y = 0;
				
		while(! encryptedText.toString().contains("END")) {
			x = random.nextInt(image.getWidth());
			y = random.nextInt(image.getHeight());
			
			if(!usedPixels.contains(new Point(x,y))) {
				usedPixels.add(new Point(x,y));
				int pixel = image.getRGB(x, y);

				// Extract the LSB from each color channel
				int red = (pixel >> 16) & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int blue = pixel & 0xFF;

				// Append the least significant bit of each color channel
				encryptedBinary.append(red & 0x01);
				encryptedBinary.append(green & 0x01);
				encryptedBinary.append(blue & 0x01);
				
				if(encryptedBinary.length() >= 8) {
					String eightBits = Utility.binaryToText(encryptedBinary.toString().substring(0,8));
					encryptedText.append(eightBits);
					encryptedBinary.delete(0,8);
					if(encryptedText.toString().endsWith("END")) break;
				}
				
			}
		}
		
		boolean startsWith = encryptedText.toString().startsWith("START");
		boolean endsWith = encryptedText.toString().endsWith("END");
		
		if(startsWith && endsWith) {
			int start = encryptedText.toString().indexOf("START")+ 5; // Move past marker
			int end = encryptedText.toString().lastIndexOf("END");
			if (end == -1 || start >= end)  throw new RuntimeException("Invalid text format");			
			
			String binary = Utility.textToBinary(encryptedText.toString().substring(start,end));
			byte[] fullBytes = Utility.binaryToBytes(binary.toString());
			int payloadlength = ByteBuffer.wrap(fullBytes,0,4).getInt();
			byte[] payload = Arrays.copyOfRange(fullBytes,4,4+payloadlength);
			
			ByteArrayInputStream input = new ByteArrayInputStream(payload);
			byte[] salt = input.readNBytes(16); // 256 bytes
			byte[] encryptedData = input.readAllBytes(); // n bytes
			
			SecretKey secretKey = Encryption.getKeyFromPassword(seedPassword, salt);			
			byte[] decryptedStr = Decryption.byteDecryption(encryptedData,secretKey);			
			String decompressedStr = Compression.decompressByteData(decryptedStr);
			return decompressedStr;
		}
		
		return null;
	}
	
	public static String decodeImageWithKey(BufferedImage image, PrivateKey privateKey) throws IOException {
		
		Set<Point> usedPixels = new HashSet<>();				
		StringBuilder encryptedBinary = new StringBuilder();
		StringBuilder encryptedText = new StringBuilder();
		
		long SECRET_SEED = Utility.generateSeedFromBytes(privateKey.getEncoded());
		Random random = new Random(SECRET_SEED);
		int x = 0, y = 0;
		
		while(! encryptedText.toString().contains("END")) {
			x = random.nextInt(image.getWidth());
			y = random.nextInt(image.getHeight());
			
			if(!usedPixels.contains(new Point(x,y))) {
				usedPixels.add(new Point(x,y));
				int pixel = image.getRGB(x, y);

				// Extract the LSB from each color channel
				int red = (pixel >> 16) & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int blue = pixel & 0xFF;

				// Append the least significant bit of each color channel
				encryptedBinary.append(red & 0x01);
				encryptedBinary.append(green & 0x01);
				encryptedBinary.append(blue & 0x01);
				
				if(encryptedBinary.length() >= 8) {
					String eightBits = Utility.binaryToText(encryptedBinary.toString().substring(0,8));
					encryptedText.append(eightBits);
					encryptedBinary.delete(0,8);
					if(encryptedText.toString().endsWith("END")) break;
				}
			}
		}
	
		boolean startsWith = encryptedText.toString().startsWith("START");
		boolean endsWith = encryptedText.toString().endsWith("END");
		
		// Check if text starts and ends 
		if(startsWith && endsWith)  {			
			int start = encryptedText.toString().indexOf("START")+ 5; // Move past marker
			int end = encryptedText.toString().lastIndexOf("END");
			if (end == -1 || start >= end)  throw new RuntimeException("Invalid text format");			
			
			String binary = Utility.textToBinary(encryptedText.toString().substring(start,end));
			byte[] fullBytes = Utility.binaryToBytes(binary.toString());
			int payloadlength = ByteBuffer.wrap(fullBytes,0,4).getInt();
			byte[] payload = Arrays.copyOfRange(fullBytes,4,4+payloadlength);
			
			ByteArrayInputStream input = new ByteArrayInputStream(payload);
			byte[] encryptedRSAKey = input.readNBytes(256); // 256 bytes
			byte[] iv = input.readNBytes(16); // 16 bytes
			byte[] encryptedData = input.readAllBytes(); // n bytes
						
			// De-crypt and de-compress byte data 
			byte[] aesKeyBytes = Decryption.decryptAESKeyWithRSA(encryptedRSAKey, privateKey);
			SecretKey secretKey = Encryption.getAESKeyFromBytes(aesKeyBytes);
			byte[] decryptedBytes = Decryption.decryptAES(encryptedData,secretKey,iv);			
			String decompressedStr = Compression.decompressByteData(decryptedBytes);
			if(decompressedStr == null) return "Keys do not match! Unable to decode message!!!";
			return decompressedStr;
		}
		
		return null;
	}
}

