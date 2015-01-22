package xen.library.stream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

public class XenParameters implements Serializable{
	private static final long serialVersionUID = -5556365627724410797L;
	
	transient private LZ4Compressor l;
	transient private Cipher c;
	private int size;
	private String algo;
	private Key key;
	private byte[] IV;
	private int mode;
	
	public XenParameters(int mode, int size, Key key, byte[] IV, String algo) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
		l = LZ4Gen(mode);
		this.mode = mode;
		c = Cipher.getInstance(algo);
		this.algo = algo;
		this.size = size;
		this.key = key;
		this.IV = IV;
	}
	
	public Cipher getCipher(int mode){
		try {
			c.init(mode, key, new IvParameterSpec(IV));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return c;
	}
	public LZ4Compressor getCompressor(){
		return l;
	}
	public int getBufferSize(){
		return size;
	}
	
	public static SecretKeySpec makeKey(String passphrase) throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance("SHA");
		digest.update(passphrase.getBytes());
		return new SecretKeySpec(digest.digest(), 0, 16, "AES");
	}
	
	public static IvParameterSpec makeIv(byte[] iv) throws NoSuchAlgorithmException{
		MessageDigest digest = MessageDigest.getInstance("SHA");
		digest.update(iv);
		return new IvParameterSpec(digest.digest(), 0, 16);
	}
	
	public static SecretKeySpec makeKeySalt(String bs, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException{
		return makeKeySalt(bs.getBytes(), salt);
	}
	
	public static SecretKeySpec makeKeySalt(byte[] bs, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException{
		int iterations = 10000;
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		SecretKey tmp = factory.generateSecret(new PBEKeySpec((new String(bs)).toCharArray(), salt, iterations, 128));
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}
	
	public static void save(String location, XenParameters obj) throws IOException{
		FileOutputStream fos = new FileOutputStream(location);
	    ObjectOutputStream out = new ObjectOutputStream(fos);
	    out.writeObject(obj);
	    out.flush();
	    out.close();
	}

	public static XenParameters load(String location) throws IOException{
		FileInputStream fis = new FileInputStream(location);
        ObjectInputStream in = new ObjectInputStream(fis);
        XenParameters obj;
		try {
			obj = (XenParameters) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			in.close();
			return null;
		}
        in.close();
        try {
			obj.regenKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	public static XenParameters load(InputStream fis) throws IOException{
        ObjectInputStream in = new ObjectInputStream(fis);
        XenParameters obj;
		try {
			obj = (XenParameters) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			in.close();
			return null;
		}
        in.close();
        try {
			obj.regenKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	private void regenKey() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		c = Cipher.getInstance(algo);
		l = LZ4Gen(mode);
	}
	
	private LZ4Compressor LZ4Gen(int mode){
		if(mode == 0){
			return LZ4Factory.fastestJavaInstance().fastCompressor();
		} else {
			return LZ4Factory.fastestJavaInstance().highCompressor();
		}
	}

}
