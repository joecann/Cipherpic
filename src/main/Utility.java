package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class Utility {

	public static String textToBinary(String text) {
		StringBuilder binaryString = new StringBuilder();
		char[]charArray = text.toCharArray();
		for(char c: charArray) {
			String binary = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
			binaryString.append(binary);
		}
		return binaryString.toString();
	}

	public static String bytesToBinary(byte[] bytes) {
		StringBuilder binary = new StringBuilder();
		for(byte b: bytes) {
			binary.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
		}
		return binary.toString();
	}

	public static String binaryToText(String binary) {
		StringBuilder convertedBinary = new StringBuilder();
		for(int i = 0; i < binary.length(); i++) {
			if(i+8 > binary.length()) break;
			int bitIndex = i+8;
			String bits = binary.substring(i,bitIndex);
			int asciiValue = Integer.parseInt(bits,2);
			char character = (char) asciiValue;
			convertedBinary.append(character);
		}
		return convertedBinary.toString();
	}

	public static byte[] binaryToBytes(String binary) {
		int byteCount = binary.length()/8;
		byte[] bytes = new byte[byteCount];
		for(int i = 0; i < byteCount; i++) {
			bytes[i] = (byte) Integer.parseInt(binary.substring(i*8,(i+1)*8),2);
		}
		return bytes;
	}

	public static BufferedImage byteArrayToImage(byte[] bytes) throws IOException {
		try (ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes)){
			return ImageIO.read(byteArray);
		}
	}
	
	public static byte[] bufferedImagetoByteArray(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", byteArray);
		byteArray.flush();
		return byteArray.toByteArray();
		
	}
	
	public static long generateSeed(String password) {
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			return Arrays.hashCode(Arrays.copyOfRange(hash,0,8));
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static long generateSeedFromBytes(byte[] seed) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(seed);
			return Arrays.hashCode(Arrays.copyOfRange(hash,0,8));
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
}
