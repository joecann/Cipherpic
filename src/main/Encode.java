package main;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.crypto.SecretKey;

public class Encode {


	/**
	 *
	 * @param image
	 * @param text
	 * @param fileURL
	 * @param seedPassword
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage encodeImageWithPassword(BufferedImage image, String text, String seedPassword) throws IOException{
		
		Set<Point> usedPixels = new HashSet<>();
		// Create salt
		byte[]salt = Encryption.generateSalt();
		SecretKey secretKey = Encryption.getKeyFromPassword(seedPassword, salt);
		
		// Encrypting the string data THEN compress
		byte[] strBytes = Compression.compressStringData(text);
		byte[] encryptedBytes = Encryption.byteEncryption(strBytes, secretKey);
		
		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		payload.write(salt); // 16 bytes
		payload.write(encryptedBytes); //n bytes
		byte[] payloadData =payload.toByteArray();
		byte[] buffer = ByteBuffer.allocate(4 + payloadData.length).putInt(payloadData.length).put(payloadData).array();
		
		String binary = Utility.textToBinary("START") + Utility.bytesToBinary(buffer) + Utility.textToBinary("END");
		
		// Random placement of string using a secret seed
		long SECRET_SEED = Utility.generateSeed(seedPassword);
		Random random = new Random(SECRET_SEED);
		int x = 0, y = 0;

		int strIndex = 0;
		while(strIndex < binary.length()) {
			x = random.nextInt(image.getWidth());
			y = random.nextInt(image.getHeight());
			if(! usedPixels.contains(new Point(x,y))) {
				usedPixels.add(new Point(x,y));
				int pixel = image.getRGB(x,y);
				// Extract each color and apply bitwise with 0xFF
				int red = (pixel >> 16) & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int blue = pixel & 0xFF;
				//Modify each color channels LSB
				char currentBit = binary.charAt(strIndex);
				red = setLeastSignificantBit(red,currentBit);
				strIndex++;

				if(strIndex < binary.length()) {
					currentBit = binary.charAt(strIndex);
					green = setLeastSignificantBit(green,currentBit);
					strIndex++;
				}

				if(strIndex < binary.length()) {
					currentBit = binary.charAt(strIndex);
					blue = setLeastSignificantBit(blue,currentBit);
					strIndex++;
				}

				int newPixel = (red << 16) | (green << 8) | blue;
				image.setRGB(x,y,newPixel);
			}		
		}
		return image;
	}
	
	public static BufferedImage encodeImageWithKey(BufferedImage image, String text, PublicKey publicKey, PrivateKey privateKey) throws IOException{
		
		Set<Point> usedPixels = new HashSet<>();
						
		byte[] aesKey = Encryption.generateAESKey();
		SecretKey secretkey = Encryption.getAESKeyFromBytes(aesKey);
		byte[]iv = new byte[16];
		byte[] compressedStr = Compression.compressStringData(text);
		byte[] encryptedData = Encryption.encryptionAES(compressedStr, secretkey, iv);
		byte[] encryptedWithRSA = Encryption.encryptAESKeyWithRSA(aesKey,publicKey);
				
		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		//payload.write(encryptedWithRSA.length); // 1 byte
		payload.write(encryptedWithRSA); // 256 bytes
		payload.write(iv); // 16 bytes
		payload.write(encryptedData); // n bytes
		byte[] payloadData = payload.toByteArray();
		
		byte[] buffer = ByteBuffer.allocate(4 + payloadData.length).putInt(payloadData.length).put(payloadData).array();
		String binary = Utility.textToBinary("START") + Utility.bytesToBinary(buffer) + Utility.textToBinary("END");
		
		// Random placement of string using a secret seed
		long SECRET_SEED = Utility.generateSeedFromBytes(privateKey.getEncoded());
		Random random = new Random(SECRET_SEED);
		int x = 0, y = 0;

		int index = 0;
		while(index < binary.length()) {
			x = random.nextInt(image.getWidth());
			y = random.nextInt(image.getHeight());
			if(! usedPixels.contains(new Point(x,y))) {
				usedPixels.add(new Point(x,y));
				int pixel = image.getRGB(x,y);
				// Extract each color and apply bitwise with 0xFF
				int red = (pixel >> 16) & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int blue = pixel & 0xFF;
				
				//Modify each color channels LSB
				char currentBit = binary.charAt(index);
				red = setLeastSignificantBit(red,currentBit);
				index++;

				if(index < binary.length()) {
					currentBit = binary.charAt(index);
					green = setLeastSignificantBit(green,currentBit);
					index++;
				}

				if(index < binary.length()) {
					currentBit = binary.charAt(index);
					blue = setLeastSignificantBit(blue,currentBit);
					index++;
				}

				int newPixel = (red << 16) | (green << 8) | blue;
				image.setRGB(x,y,newPixel);
			}		
		}
		return image;
	}

	/**
	 * Clear the LSB using bitwise AND with 0xFE
	 * THEN set the LSB based on the bit value ('0' or '1')
	 * @param colorChannel
	 * @param bit
	 * @return
	 */
	private static int setLeastSignificantBit(int colorChannel, char bit) {
		colorChannel = colorChannel & 0xFE;
		if(bit == '1') {
			colorChannel = colorChannel | 0x01;
		}
		return colorChannel;
	}

	/**
	 * Checks if the image has enough pixels to store the binary data
	 * @return
	 */
	public static boolean countPixels(BufferedImage image, int length) {
		int totalPixels = image.getWidth() + image.getHeight();
		if(totalPixels * 3 < length) {
			return false;
		}
		return true;
	}

}
