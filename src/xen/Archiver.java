package xen;

import gui.Form;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import xen.library.data.SpeedCalculator;
import xen.library.files.FileHelper;
import xen.library.number.DoubleHelper;
import xen.library.stream.FolderInputStream;
import xen.library.stream.FolderOutputStream;
import xen.library.stream.XenInputStream;
import xen.library.stream.XenOutputStream;
import xen.library.stream.XenParameters;

public class Archiver extends Thread{
	
	private static Archiver instance = null;
	public static Archiver getInstance(){
		return instance;
	}
	public static Archiver newInstance(String from, String to){
		instance = new Archiver();
		instance.from = from;
		instance.to = to;
		
		return instance;
	}
	public static void kill() {
		instance = null;
	}
	
	private boolean state = true;
	private String from;
	private String to;
	
	private Archiver(){
		
	}
	
	public void run(){
		SpeedCalculator rate = null;
		
		try{
			
			Form.getInstance().setDetailLabel("Starting...");
					
			XenParameters xen = XenParameters.load(getClass().getResourceAsStream("/ressource/random.xen"));
			
			File fIn = null;
			File fOut = null;
		
			if(from.endsWith(".drk")){
				fIn = new File(from);
				fOut = new File(to + from.substring(from.lastIndexOf(File.separator)+1,from.lastIndexOf(".drk")));
				if(fOut.exists()){
					fOut = new File(to + from.substring(from.lastIndexOf(File.separator)+1,from.lastIndexOf(".drk")) + "(2)");
				}
			} else {
				fIn = new File(from);
				fOut = new File(to + from.substring(from.lastIndexOf(File.separator)+1) + ".drk");
			}
		
			InputStream sIn;
			OutputStream sOut;
		
			if(from.endsWith(".drk")){
				sIn = new XenInputStream(new FileInputStream(fIn), xen);
				sOut = new FolderOutputStream(fOut);
			} else {
				sIn = new FolderInputStream(fIn);
				sOut = new XenOutputStream(new FileOutputStream(fOut), xen);
			}
		
			byte[] buffer;
			int n;
		
			rate = new SpeedCalculator();
			long length;
		
			System.out.print("Calculating size of the input:");
			Form.getInstance().setDetailLabel("Calculating size of the input...");
		
			if(fIn.isFile()){
				length = fIn.length();
			} else {
				length = FileHelper.getLength(fIn);
			}
		
			System.out.println(" " + DoubleHelper.round(length/1024.0d/1024.0d, 2) + "MB");
			String archiveText = "";
			if(from.endsWith(".drk")){
				Form.getInstance().setDetailLabel("<html>Restoring from " + from + " of " + DoubleHelper.round(length/1024.0d/1024.0d, 2) + "MB.</html>");
			} else {
				ArrayList<File> files = new ArrayList<File>();
				FileHelper.listing(fIn, files);
				archiveText = "<html>Archiving " + DoubleHelper.round(length/1024.0d/1024.0d, 2) + "MB of data, with " + files.size() + " files.";
				Form.getInstance().setDetailLabel(archiveText + "</html>");
				files = null;
			}
		
			System.out.println("Starting");
			buffer = new byte[xen.getBufferSize()];
			long count = 0;
			double progress;
			while((n = sIn.read(buffer)) != -1){
				count += n;
				rate.update(count);
				sOut.write(buffer, 0, n);
				for(int i = 0; i < 50000; i++){
					if(i == 0){
						if(from.endsWith(".drk")){
							progress = DoubleHelper.round((count*100)/(((FolderOutputStream)sOut).getLength()*1.0d),1);
							Form.getInstance().setProgressBar((int) progress);
							Form.getInstance().setProgressLabel(progress + "% - " + DoubleHelper.round(rate.getMByteRate(),2) + " MB/s");
						} else {
							progress = DoubleHelper.round((count*100)/(length *1.0d),1);
							Form.getInstance().setProgressBar((int) progress);
							Form.getInstance().setProgressLabel(progress + "% - " + DoubleHelper.round(rate.getMByteRate(),2) + " MB/s");
							Form.getInstance().setDetailLabel(archiveText + " With " + DoubleHelper.round((fOut.length() *100.0d) /count, 1) + "% current Ratio." + "</html>");
						}
					}
				}
			}
			sOut.flush();
			sOut.close();
		
			sIn.close();
			
			xen = null;
			
			if(count < length && !from.endsWith(".drk")){
				System.out.println("\ndone! @" + DoubleHelper.round(rate.getAvrageMByteRate(), 2) + "MB/s but " + DoubleHelper.round((length-count)/1024.0d/1024.0d, 2) + "MB could not be read.");
			} else {
				System.out.println("\ndone! @" + DoubleHelper.round(rate.getAvrageMByteRate(), 2) + "MB/s ");
			}
			
			if(!from.endsWith(".drk")){
				double per = (fOut.length() *1.0d) / length;
				System.out.println(DoubleHelper.round(per*100.0d, 2) + "% compression acheved.");
				JOptionPane.showMessageDialog(null,DoubleHelper.round(per*100.0d, 2) + "% compression acheved.","Done!",JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null,"Decompression complete.","Done!",JOptionPane.INFORMATION_MESSAGE);
			}
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,"Something went wrong with " + e.getMessage(),"Oups!",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		if(rate != null){
			rate.close();
			rate = null;
		}
		Form.getInstance().setGoButtonEnabled(true);
		Form.getInstance().setProgressLabel("0% - 0 MB/s");
		Form.getInstance().setProgressBar(0);
		Form.getInstance().setDetailLabel("");
		state = false;
	}
	
	public boolean getStateOfExec(){
		return state;
	}
}
