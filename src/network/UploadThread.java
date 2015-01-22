package network;

import gui.Form;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import xen.library.data.SpeedCalculator;
import xen.library.files.FileHelper;
import xen.library.number.DoubleHelper;
import xen.library.stream.FolderInputStream;
import xen.library.stream.XenOutputStream;
import xen.library.stream.XenParameters;

public class UploadThread extends Thread{
	
	private static UploadThread instance = null;
	public static UploadThread getInstance(){
		return instance;
	}
	public static UploadThread newInstance(String ip, String from){
		instance = new UploadThread();
		instance.from = from;
		instance.ip = ip;
		
		return instance;
	}
	public static void kill() {
		instance = null;
	}
	
	private boolean state = true;
	private String from;
	private String ip;
	
	private UploadThread(){
		
	}
	
	public void run(){
		SpeedCalculator rate = null;
		
		try{
			
			Form.getInstance().setLanDetailLabel("Starting...");
					
			XenParameters xen = XenParameters.load(getClass().getResourceAsStream("/ressource/random.xen"));
			
			File fIn = new File(from);
		
			byte[] buffer;
			int n;
		
			rate = new SpeedCalculator();
			long length;
		
			System.out.print("Calculating size of the input:");
			Form.getInstance().setLanDetailLabel("Calculating size of the input...");
		
			if(fIn.isFile()){
				length = fIn.length();
			} else {
				length = FileHelper.getLength(fIn);
			}
			
			System.out.println(" " + DoubleHelper.round(length/1024.0d/1024.0d, 2) + "MB");
			
			Entry<XenParameters, OutputStream> e = FileTransferUtils.OpenConnection(ip, from, length); 
			
			InputStream sIn = new BufferedInputStream(new FolderInputStream(fIn));
			OutputStream sOut = new XenOutputStream(e.getValue(), e.getKey());
		
			String archiveText = "";
			ArrayList<File> files = new ArrayList<File>();
			FileHelper.listing(fIn, files);
			archiveText = "<html>Archiving " + DoubleHelper.round(length/1024.0d/1024.0d, 2) + "MB of data, with " + files.size() + " files.";
			Form.getInstance().setLanDetailLabel(archiveText + "</html>");
			files = null;
			
		
			System.out.println("Starting");
			buffer = new byte[xen.getBufferSize()];
			long count = 0;
			double progress;
			while((n = sIn.read(buffer)) != -1){
				count += n;
				rate.update(count);
				sOut.write(buffer, 0, n);
				
				progress = DoubleHelper.round((count*100)/(length *1.0d),1);
				Form.getInstance().setProgressBar((int) progress);
				Form.getInstance().setProgressLabel(progress + "% - " + DoubleHelper.round(rate.getMByteRate(),2) + " MB/s");
				Form.getInstance().setLanDetailLabel(archiveText + "</html>");
			}
			sOut.flush();
			sOut.close();
		
			sIn.close();
			
			xen = null;
			fIn = null;
			
			if(count < length && !from.endsWith(".drk")){
				System.out.println("\ndone! @" + DoubleHelper.round(rate.getAvrageMByteRate(), 2) + "MB/s but " + DoubleHelper.round((length-count)/1024.0d/1024.0d, 4) + "MB could not be read.");
			} else {
				System.out.println("\ndone! @" + DoubleHelper.round(rate.getAvrageMByteRate(), 2) + "MB/s ");
			}
			
			JOptionPane.showMessageDialog(Form.getInstance(),"File sent at " + DoubleHelper.round(rate.getAvrageMByteRate(), 2) + "MB/s.","Done!",JOptionPane.INFORMATION_MESSAGE);
		} catch(Exception e){
			if(e.getMessage() != null && !e.getMessage().equalsIgnoreCase("Refused")){
				JOptionPane.showMessageDialog(Form.getInstance(),"Something went wrong with " + e.getLocalizedMessage(),"Oups!",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		
		if(rate != null){
			rate.close();
			rate = null;
		}
		Form.getInstance().setGoButtonEnabled(true);
		Form.getInstance().setProgressLabel("0% - 0 MB/s");
		Form.getInstance().setProgressBar(0);
		Form.getInstance().setLanDetailLabel("");
		FileTransferUtils.closeSocket();
		state = false;
	}
	
	public boolean getStateOfExec(){
		return state;
	}
}
