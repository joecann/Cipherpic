package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.javalin.Javalin;
import io.javalin.http.UploadedFile;

public class Server {
	 	
 	public static ByteArrayOutputStream encodeWithPasskey(UploadedFile img, String data) throws IOException, NoSuchAlgorithmException {
 		ByteArrayOutputStream imageStream =  new ByteArrayOutputStream();
 		try(InputStream in = img.content()){
			byte[] bytes = in.readAllBytes(); 
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
			outputStream.write(bytes);
			byte[] imageBytes = outputStream.toByteArray();
			BufferedImage image = Utility.byteArrayToImage(imageBytes);
			
			KeyPair keypair = KeyUtility.generateKeyPair();
			PrivateKey privateKey = KeyUtility.generatePrivateKey(keypair);
			PublicKey publicKey = KeyUtility.generatePublicKey(keypair);				
			BufferedImage encodedImage = Encode.encodeImageWithKey(image, data, publicKey, privateKey);
							
			byte[] encodedImageBytes = Utility.bufferedImagetoByteArray(encodedImage);
							
			try(ZipOutputStream zip = new ZipOutputStream(imageStream)){
				zip.putNextEntry(new ZipEntry("encodedimage.png"));
				zip.write(encodedImageBytes);
				zip.closeEntry();
				
				zip.putNextEntry(new ZipEntry("private_key.der"));
				zip.write(privateKey.getEncoded());
				zip.closeEntry();
			}
 		}
 		return imageStream;
 	}
 	
 	public static String decodeWithPasskey(UploadedFile img, UploadedFile key) throws IOException {	 		
 		try {
 			// Get image bytes
 			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
 			InputStream imageStream = img.content();
			byte[] imageBytes = imageStream.readAllBytes(); 
			outputStream.write(imageBytes);
			byte[] image = outputStream.toByteArray();
			BufferedImage bufferedImage = Utility.byteArrayToImage(image);
			outputStream.flush();
			
 			// Get private key bytes
			InputStream keyStream = key.content();
			byte[] keyBytes = keyStream.readAllBytes(); 
			PrivateKey privateKey = KeyUtility.bytesToPrivatekey(keyBytes);
			
			return Decode.decodeImageWithKey(bufferedImage, privateKey);
 		}
 		
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
	
 	public static byte[] encodeWithPassword(UploadedFile img, String data, String seed) throws IOException {
 		
 		try(InputStream in = img.content()){
			byte[] bytes = in.readAllBytes(); 
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
			outputStream.write(bytes);
			byte[] imageBytes = outputStream.toByteArray();
			BufferedImage image = Utility.byteArrayToImage(imageBytes);
			
			BufferedImage encodedImage = Encode.encodeImageWithPassword(image, data, seed);
			byte[] encodedBytes = Utility.bufferedImagetoByteArray(encodedImage);
			return encodedBytes;
		}				
 	}
 	
 	public static String decodeWithPassword(UploadedFile img, String seed) throws IOException {
 		byte[] imageBytes = null;
		
		try(InputStream imageStream = img.content()){
			byte[] bytes = imageStream.readAllBytes(); 
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
			outputStream.write(bytes);
			imageBytes = outputStream.toByteArray();
			BufferedImage image = Utility.byteArrayToImage(imageBytes);
			String decodedMessage = Decode.decodeImageWithPassword(image,seed);
			return decodedMessage;
		} 		
 	}
 	
	public static void main(String[] args) {

		var app = Javalin.create().start(7070);
		Set<String> allowOrigins = Set.of("https://cipherpic.com","https://www.cipherpic.com");

		app.before(ctx -> {
			String origin = ctx.header("Origin");
			if(allowOrigins.contains(origin)) {
				ctx.header("Access-Control-Allow-Origin",origin);
			}
			ctx.header("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
			ctx.header("Access-Control-Allow-Headers","Content-Type");
			//ctx.header("Access-Control-Allow-Credentials","true");
		});

		app.options("/*", ctx -> {
			String origin = ctx.header("Origin");
			if(allowOrigins.contains(origin)) {
				ctx.header("Access-Control-Allow-Origin",origin);
			}
			ctx.header("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
			ctx.header("Access-Control-Allow-Headers","Content-Type");
			//ctx.header("Access-Control-Allow-Credentials","true");
			ctx.status(200);
		});

		app.get("/health", ctx ->{
			ctx.status(200).result("Server is up and running");
		});
		
		app.post("/encodeWithPassword", ctx ->{
			UploadedFile img = ctx.uploadedFile("imagefile");
			String data = ctx.formParam("data");
			String seed = ctx.formParam("seed");
			
			System.out.println(data + " " + seed);
	 		
	 		if(data == null || seed == null) {
	 			ctx.status(500).result("Error");	
	 		}
	 		
	 		
			byte[] byteImage = Server.encodeWithPassword(img, data, seed);		
			ctx.contentType("image/png");
			ctx.status(200).result(byteImage);	
			
		});
		
		app.post("/decodeWithPassword", ctx ->{
			UploadedFile img = ctx.uploadedFile("imagefile");
			String seed = ctx.formParam("auth");
			String decodedMessage =  Server.decodeWithPassword(img, seed);			
			ctx.status(200).result(decodedMessage);				
		});
		
		app.post("/encodeWithPasskey", ctx ->{
			UploadedFile img = ctx.uploadedFile("imagefile");
			String data = ctx.formParam("data");		
			ByteArrayOutputStream output = Server.encodeWithPasskey(img, data);				
			ctx.contentType("application/zip");
			ctx.status(200).result(output.toByteArray());		
		});
		
		app.post("/decodeWithPasskey", ctx ->{
			UploadedFile img = ctx.uploadedFile("imagefile");
			UploadedFile key = ctx.uploadedFile("auth");
			String decodedMessage = Server.decodeWithPasskey(img,key);	
			ctx.status(200).result(decodedMessage);				
		});		
	}
}
