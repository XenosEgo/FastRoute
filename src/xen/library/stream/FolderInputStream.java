package xen.library.stream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import xen.library.files.FileHelper;

public class FolderInputStream extends InputStream{
	private ArrayList<File> list = new ArrayList<File>();
	private int pos = -1;
	private BufferedInputStream current;
	private File in;
	private long pointer = 0;
	private long fileLength;
	private int headerPointer = 0;
	final private int headerLength = 4+8+4; // id + file size + string size
	private int fileNamePointer = 0;
	private int fileNameLength;
	private int archiveHeaderPointer = 0;
	final private int archiveHeaderLength = 4+4+8;
	private boolean writingArchiveHeader = true;
	private boolean writingHeader = false;
	private boolean writingFileName = false;
	private boolean writingFile = false;
	private byte[] bufferArchiveHeader;
	private byte[] bufferHeader;
	private byte[] bufferFileName;
	
	private final byte[] headerStart = new byte[] {88,69,3,4};
	
	final private int version = 1;
	
	public FolderInputStream(File f) throws IllegalArgumentException, IOException{
		if(!f.isDirectory()){
			list.add(f);
		} else {
			listing(f);
		}
		in = f;
	}
	
	private void listing(File f){
		File[] sub;
		sub = f.listFiles();
		if(sub != null){
			for(File u: sub){
				if(u.isFile()){
					list.add(u);
				} else {
					listing(u);
				}
			}
		}
	}
	
	private byte[] genHeader(File f){
		byte[] output = new byte[headerLength];
		
		String name;
		if(f.getParentFile() == null){
			name = "";
		} else {
			name = f.getPath().substring(in.getPath().length());
		}
		
		fileLength = f.length();
		fileNameLength = name.length();
		bufferFileName = name.replace("\\", "/").getBytes();
		
		byte[] lenght = ByteBuffer.allocate(8).putLong(f.length()).array();
		byte[] lenghtOfPath = ByteBuffer.allocate(4).putInt(name.length()).array();

		System.arraycopy(headerStart, 0, output, 0, headerStart.length);
		System.arraycopy(lenght, 0, output, headerStart.length, lenght.length);
		System.arraycopy(lenghtOfPath, 0, output, headerStart.length+lenght.length, lenghtOfPath.length);
		
		return output;
	}
	
	private byte[] genArchiveHeader(){
		byte[] output = new byte[archiveHeaderLength];
		
		byte[] ver = ByteBuffer.allocate(4).putInt(version).array();
		byte[] numOfFiles = ByteBuffer.allocate(4).putInt(list.size()).array();
		@SuppressWarnings("unchecked")
		byte[] lenght = ByteBuffer.allocate(8).putLong(FileHelper.getLength((ArrayList<File>)list.clone())).array();

		System.arraycopy(ver, 0, output, 0, ver.length);
		System.arraycopy(numOfFiles, 0, output, ver.length, numOfFiles.length);
		System.arraycopy(lenght, 0, output, ver.length+numOfFiles.length, lenght.length);
		
		return output;
	}
	
	@Override
	public int read() throws IOException {
		return 0;
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		if(writingArchiveHeader){
			return setArchiveHeader(b, off, len);
		} else if(writingHeader){ // wonders! we have landed on a header;		
			return setHeader(b, off, len);
		} else if(writingFileName){ // reading file name
			return setFileName(b, off, len);
		} else if(writingFile){ // reading file
			return setFile(b, off, len);
		}
		return -1;
	}
	
	private int setArchiveHeader(byte[] b, int off, int len) throws IOException{
		if(archiveHeaderPointer == 0){
			bufferArchiveHeader = genArchiveHeader();
		}
		
		byte[] B = new byte[b.length];
		
		int noff= (int) Math.min(archiveHeaderLength-archiveHeaderPointer, len);
		System.arraycopy(bufferArchiveHeader, archiveHeaderPointer, B, off, noff);
		archiveHeaderPointer += noff;
		
		transfer(b, B, off, noff);
		
		if(archiveHeaderPointer == archiveHeaderLength){
			writingArchiveHeader = false;
			
			writingHeader = true;
			headerPointer = 0;
			
			return setHeader(b, off+noff, len-noff)+noff;
		}
		return noff;
	}
	
	private int setHeader(byte[] b, int off, int len) throws IOException{
		if(headerPointer == 0){
			pos++;
			if(pos >= list.size()){
				return -1;
			}
			while(true){
				if(pos >= list.size()){
					return -1;
				}
				if(list.get(pos).exists()){
					try{
						new FileInputStream(list.get(pos));
						
						break;
					} catch (FileNotFoundException e){
						pos++;
					}
				} else {
					pos++;
				}
			}
			bufferHeader = genHeader(list.get(pos));
		}
		
		byte[] B = new byte[b.length];
		
		int noff = (int) Math.min(len, headerLength - headerPointer);
		System.arraycopy(bufferHeader, headerPointer, B, off, noff);
		headerPointer += noff;
		
		transfer(b, B, off, noff);
		
		if(headerPointer == 16){
			writingHeader = false;
			headerPointer = 0;
			writingFileName = true;
			fileNamePointer = 0;
			
			return setFileName(b, off+noff, len-noff)+noff;
		}	
		return noff;
	}
	
	private int setFileName(byte[] b, int off, int len) throws IOException{
		byte[] B = new byte[b.length];
		
		int noff= (int) Math.min(fileNameLength-fileNamePointer, len);
		System.arraycopy(bufferFileName, fileNamePointer, B, off, noff);
		fileNamePointer += noff;
		
		transfer(b, B, off, noff);
		
		if(fileNamePointer == fileNameLength){
			current = new BufferedInputStream(new FileInputStream(list.get(pos)));
			
			writingFileName = false;
			fileNamePointer = 0;
			writingFile = true;
			pointer = 0;
			
			return setFile(b, off+noff, len-noff)+noff;
		} 
		return noff;
	}
	
	private int setFile(byte[] b, int off, int len) throws IOException{
		byte[] B = new byte[b.length];
		
		int noff= (int) Math.min(fileLength-pointer, len);
		current.read(B, off, noff);
		pointer += noff;
		
		transfer(b, B, off, noff);
		
		if(pointer == fileLength){
			current.close();
			current = null;
			writingFile = false;
			pointer = 0;
			writingHeader = true;
			headerPointer = 0;
			
			return setHeader(b, off+noff, len-noff)+noff;
		}
		return noff;
		
	}
	
	private void transfer(byte[] b, byte[] in, int off, int len){
		for(int i = off; i < off+len; i++){
			b[i] = in[i];
		}
	}
	
}
