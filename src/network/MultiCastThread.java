package network;

import gui.Form;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.TreeMap;

import xen.Main;

public class MultiCastThread extends Thread{
	
	private static MultiCastThread instance = null;
	public static MultiCastThread getInstance(){
		return instance;
	}
	public static MultiCastThread newInstance(String name) throws IOException{
		instance = new MultiCastThread();
		instance.s = new MulticastSocket(port);
		instance.name = name;
		return instance;
	}
	public static void kill() {
		instance.running = false;
		closeSocket();
		instance = null;
	}
	
	boolean running = true;
	
	static int port = 5040;
	static String group = "225.4.5.6";
	
	TreeMap<String,String> list = new TreeMap<String,String>();
	MulticastSocket s = null;
	String name = "";
	boolean closed = false;
	
	private MultiCastThread(){
		
	}
	
	public void run(){
		System.out.println("Multicast started on: " + group + ":" + port);
		byte[] buf = new byte[1024];
		String heyMess = "FastRoute, who is here?:";
		String awnserMess = "Hi, this is FastRoute:";
		String byeMess = "see ya FastRoute";
		DatagramPacket pack = null;
		
		try {
			s.joinGroup(InetAddress.getByName(group));
			pack = new DatagramPacket((heyMess + name + ":" + Main.version).getBytes(), (heyMess + name + ":" + Main.version).getBytes().length, InetAddress.getByName(group), port);
			s.send(pack);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while(running){
			try {
				pack = new DatagramPacket(buf, buf.length);
				s.receive(pack);
				if(!pack.getAddress().getHostName().equals(InetAddress.getLocalHost().getHostName()) && !pack.getAddress().getCanonicalHostName().equals(InetAddress.getLocalHost().getCanonicalHostName())){
					System.out.println("Received data from: " + pack.getAddress().toString() + 
							":" + pack.getPort() + " with length: " +
						    pack.getLength());
					System.out.write(pack.getData(),0,pack.getLength());
					System.out.println();
					
					String in = new String(pack.getData(),0,pack.getLength()).trim();

					if(in.split(":").length == 3 && Main.versionCompaire(in.split(":")[2])){
						if(in.startsWith(heyMess)){
							System.out.println("Awnsering...");
							list.put("/" + pack.getAddress().getHostAddress(), in.split(":")[1].trim());
							if(Form.getInstance() != null){
								Form.getInstance().setUserList(list);
							}
							pack = new DatagramPacket((awnserMess + name + ":" + Main.version).getBytes(), (awnserMess + name + ":" + Main.version).getBytes().length, InetAddress.getByName(group), port);
							s.send(pack);
						} else if(in.startsWith(awnserMess)){
							list.put("/" + pack.getAddress().getHostAddress(), in.split(":")[1].trim());
							if(Form.getInstance() != null){
								Form.getInstance().setUserList(list);
							}
						} else if(in.equalsIgnoreCase(byeMess)){
							list.remove("/" + pack.getAddress().getHostAddress());
							if(Form.getInstance() != null){
								Form.getInstance().setUserList(list);
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		closeSocket();
		closed = true;
	}
	
	public boolean isClosed(){
		return closed;
	}
	
	private static synchronized void closeSocket(){
		try {
			if(instance != null && instance.s != null && !instance.s.isClosed()){
				instance.s.send(new DatagramPacket("see ya FastRoute".getBytes(), "see ya FastRoute".getBytes().length, InetAddress.getByName(group), port));
				instance.s.setSoTimeout(1);
				instance.s.leaveGroup(InetAddress.getByName(group));
				instance.s.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void update(String name) throws UnknownHostException, IOException{
		this.name = name;
		list.clear();
		if(Form.getInstance() != null){
			Form.getInstance().setUserList(list);
		}
		if(s != null && !s.isClosed()){
			String mess = "FastRoute, who is here?:" + name + ":" + Main.version;
			s.send(new DatagramPacket(mess.getBytes(), mess.getBytes().length, InetAddress.getByName(group), port));
		}
	}
	
	public TreeMap<String,String> getList(){
		return list;
	}
}
