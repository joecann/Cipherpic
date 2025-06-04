package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

public class Compression {
	
	// Much too slow
	public static BufferedImage zopfli(BufferedImage imageToCompress) {
		try {
			File tempFile = File.createTempFile("image","png");
			File outputFile = File.createTempFile("image","png");
			ImageIO.write(imageToCompress,"png", tempFile);
			
			ProcessBuilder process = new ProcessBuilder("zopflipng","--lossy_transparent","--iterations=15","--filters=01234mepb",tempFile.getAbsolutePath(),outputFile.getAbsolutePath());
			process.inheritIO();
			Process p = process.start();
			
			if(p.waitFor() != 0) throw new RuntimeException("ZopfliPNG compression failed");
			BufferedImage result = ImageIO.read(tempFile);
			tempFile.delete();
			outputFile.delete();
			return result;
		} 
		
		catch (IOException | InterruptedException e) {		
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	// Not adding any compression benefit
	public static BufferedImage optipng(BufferedImage imageToCompress) {
		try {
			File tempFile = File.createTempFile("image","png");
			ImageIO.write(imageToCompress,"png", tempFile);
			// Tool, optimization level (-o0 - -o7, file path 
			ProcessBuilder process = new ProcessBuilder("optipng","-o7",tempFile.getAbsolutePath());
			process.inheritIO();
			Process p = process.start();
			
			if(p.waitFor() != 0) throw new RuntimeException("OptiPNG compression failed");
			BufferedImage result = ImageIO.read(tempFile);
			tempFile.delete();
			return result;
		} 
		
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	
		return null;
	}
	
	public static byte[] compressStringData(String string) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try(GZIPOutputStream gzip = new GZIPOutputStream(output)){
			byte[] buffer = string.getBytes();
			gzip.write(buffer,0,buffer.length);
			gzip.close();
			return output.toByteArray();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
		
	public static String decompressByteData(byte [] compressedData) {
		ByteArrayInputStream input = new ByteArrayInputStream(compressedData);
		try(GZIPInputStream gzip = new GZIPInputStream(input)){
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while((bytesRead = gzip.read(buffer)) > 0) {
				output.write(buffer,0,bytesRead);
			}
			output.close();
			gzip.close();
			return output.toString(StandardCharsets.UTF_8);
		}
		
		catch (IOException e) {
			e.printStackTrace();
		};
		return null;
	}
	
	
}
