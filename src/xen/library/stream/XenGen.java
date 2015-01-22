package xen.library.stream;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class XenGen {
	
	public static void main(String[] args) throws Exception{
		if(args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("/?")){
			helpText("");
			return;
		}
		if(args.length == 1){
			genRandom(args[0]);
			System.out.println("random key 512 block coding file " + args[0] + " is done.");
			return;
		} else if(args.length == 2){
			gen(args[0], args[1]);
			System.out.println("defined key 512 block coding file " + args[0] + " is done.");
			return;
		} else if(args.length == 4){
			try{
				gen(args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
				System.out.println("defined key " + Integer.parseInt(args[2]) + " block coding file " + args[0] + " is done.");
				return;
			} catch(NumberFormatException e) {
				helpText("the size and/or mode parameter has to be a number");
				return;
			}
		}
		
		helpText("");
		return;
	}
	
	private static void helpText(String error) {
		if(!error.isEmpty()){
			System.out.println("Error: " + error + "\n");
		}
		
		System.out.println("Generates a crypted fileKey. \n");
		
		System.out.println("usage: java -cp " + new java.io.File(XenGen.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName() + " xen.library.stream.XenGen <dest> [<password> [<size>] [<mode>]]\n");
		System.out.println("  help | /?     Displays this page.");
		System.out.println("  <dest>        Destination of the outputed keyFile");
		System.out.println("  [<password>]  Passphrase used for the cipher (random one if not entered)");
		System.out.println("  [<size>]      The size of the compression block (default at 512KB)");
		System.out.println("  [<mode>]      The mode of the compression 0-1(Higher the slower, default at 1)");
	}

	public static void genRandom(String fileName) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		gen(fileName, 512, random(16), random(16), random(16), 1);
	}
	private static void gen(String fileName, int size, byte[] pass, byte[] salt, byte[] iv, int mode) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		Key key = XenParameters.makeKeySalt(pass, salt);
		IvParameterSpec IV = XenParameters.makeIv(iv);
		
		XenParameters x = new XenParameters(mode, size*1024, key, IV.getIV(), "AES/CBC/PKCS5Padding");
		XenParameters.save(fileName, x);
	}
	public static void gen(String fileName, String passPhrase) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		gen(fileName, 512, passPhrase.getBytes(), random(128), random(128), 1);
	}
	public static void gen(String fileName, String passPhrase, int size, int mode) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		gen(fileName, size, passPhrase.getBytes(), random(128), random(128), mode);
	}
	public static void gen(String fileName, int size, String passPhrase, int mode, String salt, String iv) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		gen(fileName, size, passPhrase.getBytes(), salt.getBytes(), iv.getBytes(), mode);
	}
	
	
	public static XenParameters genRandom() throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		return gen(512, random(16), random(16), random(16), 1);
	}
	public static XenParameters gen(int size, byte[] pass, byte[] salt, byte[] iv, int mode) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		Key key = XenParameters.makeKeySalt(pass, salt);
		IvParameterSpec IV = XenParameters.makeIv(iv);

		return gen(size, key, IV.getIV(), mode);
	}
	public static XenParameters gen(String passPhrase) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		return gen(512, passPhrase.getBytes(), random(16), random(16), 1);
	}
	public static XenParameters gen(int size, String passPhrase, int mode) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		return gen(size, passPhrase.getBytes(), random(16), random(16), mode);
	}
	public static XenParameters gen(int size, String passPhrase, int mode, String salt, String iv) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		return gen(size, passPhrase.getBytes(), salt.getBytes(), iv.getBytes(), mode);
	}
	
	public static XenParameters gen(int size, Key key, byte[] IV, int mode) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException{
		XenParameters x = new XenParameters(mode, size*1024, key, IV, "AES/CBC/PKCS5Padding");
		return x;
	}
	
	private static byte[] random(int size){
		SecureRandom random = new SecureRandom();
		byte[] out = new byte[size];
		random.nextBytes(out);
		
		return out;
	}
}
