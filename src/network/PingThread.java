package network;

import gui.Form;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import xen.Main;

public class PingThread extends Thread{

		private static PingThread instance = null;
		public static PingThread getInstance(){
			return instance;
		}
		public static PingThread newInstance() throws IOException{
			instance = new PingThread();
			instance.s = new ServerSocket(PingThread.port);
			return instance;
		}
		public static void kill() {
			instance.running = false;
			instance = null;
		}
		
		boolean running = true;
		
		static int port = 5049;
		
		ServerSocket s = null;
		static Socket sOut = null;
		
		public static String sendPing(String ip, String name){
			try {
				byte[] buffer = new byte[1024];
				
				InetAddress address = InetAddress.getByName(ip);
				
				sOut = new Socket(address, port);
				
				String message = "ping:" + Form.getInstance().getUserName() + ":" + Main.version;
				sOut.getOutputStream().write(message.getBytes());
				int n = sOut.getInputStream().read(buffer);
				if(!new String(buffer).equalsIgnoreCase("WrongVersion")){
					return new String(buffer, 5 , n-5);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				sOut.close();
			} catch (IOException e) {	}
			return null;
		}
		
		
		private PingThread(){
			
		}
		
		public void run(){
			Socket sock = null;
			
			while(running){
				try {
					sock = s.accept();
					
					String messageString = "pong:" + Form.getInstance().getUserName();
					
					byte[] buffer = new byte[1024];
					int n = sock.getInputStream().read(buffer);
					System.out.write(buffer, 0, n);
					System.out.println();
					String inText = new String(buffer, 5, n-5);
					String ver = inText.split(":")[inText.split(":").length - 1];
					if(Main.versionCompaire(ver)){
						Form.getInstance().addUserList("/" + sock.getInetAddress().getHostAddress(), new String(buffer, 5, n-5));
						sock.getOutputStream().write(messageString.getBytes());
					} else {
						System.out.println("Wrong version, from a ping");
						sock.getOutputStream().write("WrongVersion".getBytes());
					}
					sock.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

}
