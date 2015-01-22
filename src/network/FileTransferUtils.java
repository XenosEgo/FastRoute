package network;

import gui.Form;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

import xen.library.stream.XenGen;
import xen.library.stream.XenParameters;

public class FileTransferUtils {
	
	static int port = 5048;
	static Socket s = null;

	public static Entry<XenParameters, OutputStream> OpenConnection(String ip, String from, long length) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException{
		InetAddress address = InetAddress.getByName(ip.substring(1));
		
		System.out.println("Connecting to " + address + ":" + port + "...");
		
		s = new Socket(address, port);
		System.out.println("Connected!");
		InputStream in = s.getInputStream();
		
		String message = Form.getInstance().getUserName() + ":" + from.substring(from.lastIndexOf(File.separator)+1) + ":" + length + ":";
		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512, new SecureRandom());
		KeyPair pair = keyGen.generateKeyPair();
		byte[] keyData = pair.getPublic().getEncoded();
		short n = (short) (message.getBytes().length + keyData.length + 1);
		byte[] ret = new byte[] {
				(byte) (n >>> 8),
				(byte) n};
		s.getOutputStream().write(ret);
		s.getOutputStream().write(message.getBytes());
		s.getOutputStream().write(keyData);
		c.init(Cipher.DECRYPT_MODE, pair.getPrivate());
		byte[] buffer = new byte[64];
		in.read(buffer);
		//System.out.println(new String(buffer));
		if(!new String(buffer).trim().equalsIgnoreCase("refused") && !new String(buffer).trim().equalsIgnoreCase("0")){
			byte[] keys = c.doFinal(buffer);
			System.out.println("File accepted!");
			/*System.out.println(Arrays.toString(Arrays.copyOfRange(keys, 0, 16)));
			System.out.println(Arrays.toString(Arrays.copyOfRange(keys, 16, 32)));
			System.out.println(Arrays.toString(Arrays.copyOfRange(keys, 32, 48)));*/
			XenParameters xen = XenGen.gen(512, new SecretKeySpec(Arrays.copyOfRange(keys, 0, 16), "AES"), Arrays.copyOfRange(keys, 16, 32), 1);
			return new AbstractMap.SimpleEntry<XenParameters, OutputStream>(xen, new BufferedOutputStream(s.getOutputStream(), 512 * 1024));
		}
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("File refused!");
		JOptionPane.showMessageDialog(Form.getInstance(),"File was refused by the other client.","Refused",JOptionPane.INFORMATION_MESSAGE);
		throw new IOException("Refused");
	}
	
	public static void closeSocket(){
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
