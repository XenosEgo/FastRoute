package xen.library.files;

import java.io.File;
import java.util.ArrayList;

public class FileHelper {
	
	public static long getLength(ArrayList<File> list){
		long total = 0;
		
		for(File f:list){
			total += f.length();
		}
		
		return total;
	}
	
	public static long getLength(File f){
		long total = 0;
		
		ArrayList<File> list = new ArrayList<File>();
		
		listing(f, list);
		
		for(File u:list){
			total += u.length();
		}
		
		return total;
	}
	
	public static void listing(File f, ArrayList<File> list){
		if(f.isFile()){
			list.add(f);
			return;
		}
		File[] sub;
		sub = f.listFiles();
		if(sub != null){
			for(File u: sub){
				if(u.isFile()){
					list.add(u);
				} else {
					listing(u,list);
				}
			}
		}
	}
	
}
