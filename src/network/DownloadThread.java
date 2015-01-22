package network;

import gui.Form;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;

import xen.library.data.SpeedCalculator;
import xen.library.number.DoubleHelper;
import xen.library.stream.FolderOutputStream;
import xen.library.stream.XenInputStream;
import xen.library.stream.XenParameters;

public class DownloadThread {
	
	private DownloadThread(){
		
	}
	
	public static void run(InputStream in, String to, long size) throws IOException{
		run(in, to, size, XenParameters.load(Class.class.getResourceAsStream("/ressource/random.xen")));
	}
	
	public static void run(InputStream in, String to, long size, XenParameters xen){
		SpeedCalculator rate = null;
		
		try{
			
			Form.getInstance().setLanDetailLabel("Starting...");
			
			File fOut = null;
			
			fOut = new File(to);
			if(fOut.exists()){
				fOut = new File(to + "(2)");
			}
			
			InputStream sIn;
			OutputStream sOut;
			FolderOutputStream sFOut = new FolderOutputStream(fOut);

			sIn = new XenInputStream(in, xen);
			sOut = new BufferedOutputStream(sFOut);
			
			byte[] buffer;
			int n;
		
			rate = new SpeedCalculator();
			long length = size;
		
			System.out.print("Calculating size of the input:");
			Form.getInstance().setLanDetailLabel("Calculating size of the input...");
		
			System.out.println(" " + DoubleHelper.round(length/1024.0d/1024.0d, 2) + "MB");
			Form.getInstance().setLanDetailLabel("<html>Reciving " + DoubleHelper.round(length/1024.0d/1024.0d, 2) + "MB of data.</html>");
		
			System.out.println("Starting");
			buffer = new byte[xen.getBufferSize()];
			long count = 0;
			double progress;
			while((n = sIn.read(buffer)) != -1){
				count += n;
				rate.update(count);
				sOut.write(buffer, 0, n);
				
				progress = DoubleHelper.round((count*100)/((sFOut).getLength()*1.0d),1);
				Form.getInstance().setProgressBar((int) progress);
				Form.getInstance().setProgressLabel(progress + "% - " + DoubleHelper.round(rate.getMByteRate(),2) + " MB/s");
			}
			sOut.flush();
			sOut.close();
			
			sIn.close();
			
			xen = null;
			sOut = null;
			fOut = null;
			
			
			System.out.println("\ndone! @" + DoubleHelper.round(rate.getAvrageMByteRate(), 2) + "MB/s ");
						
			JOptionPane.showMessageDialog(Form.getInstance(),"File recived at " + DoubleHelper.round(rate.getAvrageMByteRate(), 2) + "MB/s.","Done!",JOptionPane.INFORMATION_MESSAGE);
		} catch(Exception e){
			JOptionPane.showMessageDialog(Form.getInstance(),"Something went wrong with " + e.getMessage(),"Oups!",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		if(rate != null){
			rate.close();
			rate = null;
		}
		Form.getInstance().setGoButtonEnabled(true);
		Form.getInstance().setProgressLabel("0% - 0 MB/s");
		Form.getInstance().setProgressBar(0);
		Form.getInstance().setLanDetailLabel("");
		
	}
}
