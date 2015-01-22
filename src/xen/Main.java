package xen;

import gui.Form;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import network.UploadThread;
import network.MultiCastThread;
import network.PingThread;
import network.NetworkListener;

public class Main {
	
	public static final String version = "1.1.0";
	static Timer gc;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		Form.getInstance().setDetailLabel("");
		Form.getInstance().setLanDetailLabel("");
		
		try {
			NetworkListener.newInstance().start();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Form.getInstance(),"Could not bind to port 5048!","Error",JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			PingThread.newInstance().start();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Form.getInstance(),"Could not bind to port 5049!","Error",JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			Form.getInstance().setUserName(InetAddress.getLocalHost().getHostName());
			MultiCastThread.newInstance(InetAddress.getLocalHost().getHostName()).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	MultiCastThread.kill();
		    }
		}));
		
		gc = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if(Archiver.getInstance() != null && !Archiver.getInstance().isAlive()){
					try {
						System.out.println("killing Thread...");
						Archiver.getInstance().join();
						Archiver.kill();
						System.gc();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
				if(UploadThread.getInstance() != null && !UploadThread.getInstance().isAlive()){
					try {
						System.out.println("killing Thread...");
						UploadThread.getInstance().join();
						UploadThread.kill();
						System.gc();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
			}
		};
		gc.schedule(task, 0, 2000);
	}

	public static void startLocal(String from, String to) {
		if(new File(from).exists() && to.trim() != ""){
			Form.getInstance().setGoButtonEnabled(false);
			Archiver.newInstance(from, to).start();
		} else {
			JOptionPane.showMessageDialog(Form.getInstance(),"The location does not exist!","Wait!",JOptionPane.WARNING_MESSAGE);
		}
	}

	public static void startInternet(String ip, String from) {
		if(new File(from).exists()){
			Form.getInstance().setGoButtonEnabled(false);
			UploadThread.newInstance(ip, from).start();
		} else {
			JOptionPane.showMessageDialog(Form.getInstance(),"The location does not exist!","Wait!",JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public static boolean versionCompaire(String ver){
		if(ver.split("\\.").length == 3 && ver.split("\\.")[0].equalsIgnoreCase(version.split("\\.")[0]) && ver.split("\\.")[1].equalsIgnoreCase(version.split("\\.")[1])){
			return true;
		}
		return false;
	}

}
