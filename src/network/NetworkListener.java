package network;

import gui.Form;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import xen.library.number.DoubleHelper;
import xen.library.stream.XenGen;
import xen.library.stream.XenParameters;

public class NetworkListener extends Thread{
	
	private static NetworkListener instance = null;
	public static NetworkListener getInstance(){
		return instance;
	}
	public static NetworkListener newInstance() throws IOException{
		instance = new NetworkListener();
		instance.s = new ServerSocket(instance.port);
		return instance;
	}
	public static void kill() {
		instance.running = false;
		instance = null;
	}
	
	boolean running = true;
	
	int port = 5048;
	
	ServerSocket s = null;
	String to = "";
	
	
	private NetworkListener(){
		
	}
	
	public void run(){
		final JFileChooser fc = new JFileChooser();
		
		Socket sock = null;
		InputStream in = null;
		
		while(running){
			try {
				System.out.println("Lisening...");
				sock = s.accept();
				System.out.println("Client Connected.");
				
				byte[] buffer = new byte[2];
				sock.getInputStream().read(buffer, 0, 2);
				int n = ByteBuffer.wrap(buffer).getShort();
				System.out.println("incomming message of " + n + " bytes long.");
				byte[] message = new byte[n];
				sock.getInputStream().read(message);
				int pos = 0;
				int count = 0;
				for(byte b:message){
					
					if(count >= 3){
						break;
					}
					if(b == ":".toCharArray()[0]){
						count++;
					}
					pos++;
				}
				System.out.write(message, 0, pos);
				System.out.println();
				String[] text = new String(message, 0, pos).split(":"); // 0: who? 1: what? 2: how much? 3: public key
				
				byte[] pubKeyRaw = new byte[94];
				for(int i = 0; i < 94; i++){
					pubKeyRaw[i] = message[pos+i];
				}
				X509EncodedKeySpec ks = new X509EncodedKeySpec(pubKeyRaw);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				PublicKey pubKey = kf.generatePublic(ks);
				
				double size = DoubleHelper.round(Long.valueOf(text[2].trim())/1024.d/1024.d, 2);
				
				Form.getInstance().setTabIndex(0);
								
				int result = JOptionPane.showConfirmDialog(Form.getInstance().getParent(), text[0] + " wants to send " + text[1] + " of " + size + "MB, do you accepte?", "Incoming file.", JOptionPane.YES_NO_OPTION);
				
				if(result == JOptionPane.NO_OPTION){
					sock.getOutputStream().write("refused".getBytes());
				} else {
					fc.setMultiSelectionEnabled(false);
					fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					fc.setCurrentDirectory(new File(System.getProperty("user.home")));
					
					int returnVal = fc.showSaveDialog(Form.getInstance().getFocusOwner());
					
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						if(fc.getSelectedFile().isFile()){
		                	to = fc.getSelectedFile().getParentFile().getAbsolutePath() + File.separator;
		                } else {
		                	to = fc.getSelectedFile().getAbsolutePath() + File.separator;
		                }
						SecureRandom rand = new SecureRandom();
						byte[] key = new byte[16];
						byte[] salt = new byte[16];
						byte[] iv = new byte[16];
						rand.nextBytes(key);
						rand.nextBytes(salt);
						rand.nextBytes(iv);
						/*System.out.println(Arrays.toString(key));
						System.out.println(Arrays.toString(salt));
						System.out.println(Arrays.toString(iv));*/
						ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
						Key pass = XenParameters.makeKeySalt(key, salt);
						IvParameterSpec IV = XenParameters.makeIv(iv);
						byteArray.write(pass.getEncoded());
						byteArray.write(IV.getIV());
						Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						c.init(Cipher.ENCRYPT_MODE, pubKey);
						sock.getOutputStream().write(c.doFinal(byteArray.toByteArray()));
						System.out.println("Ready to receive:");
						in = new BufferedInputStream(sock.getInputStream(), 8 * 1024 * 1024);
						Form.getInstance().setGoButtonEnabled(false);
						DownloadThread.run(in, to + text[1], Long.valueOf(text[2].trim()), XenGen.gen(512, pass, IV.getIV(), 1));
						System.out.println("Done!");
						System.gc();
					} else {
						sock.getOutputStream().write("refused".getBytes());
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
