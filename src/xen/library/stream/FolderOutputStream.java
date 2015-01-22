package xen.library.stream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class FolderOutputStream extends OutputStream{
	private File dest;
	private BufferedOutputStream current;
	private long pointer = 0;
	private long fileLength;
	protected int headerPointer = 0;
	final private int headerLength = 4+8+4; // id + file size + string size
	protected int fileNamePointer = 0;
	protected int fileNameLength;
	private int archiveHeaderPointer = 0;
	final private int archiveHeaderLength = 4+4+8;
	private boolean readingArchiveHeader = true;
	protected boolean readingHeader = false;
	protected boolean readingFileName = false;
	private boolean readingFile = false;
	private byte[] bufferArchiveHeader;
	private byte[] bufferHeader;
	protected byte[] bufferFileName;
	
	private final byte[] headerStart = new byte[] {88,69,3,4};
	
	private long totalLength = 0;
	
	final private int version = 1;
	
	public FolderOutputStream(File f){
		dest = f;
	}
	
	public long getLength(){
		return totalLength;
	}
	
	@Override
	public void write(int arg0) throws IOException {
		
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		if(readingArchiveHeader){
			getArchiveHeader(b, off, len);
		} else if(readingHeader){ // wonders! we have landed on a header;		
			getHeader(b, off, len);
		} else if(readingFileName){ // reading file name
			getFileName(b, off, len);
		} else if(readingFile){ // reading file
			getFile(b, off, len);
		}
		
	}
	
	protected void getArchiveHeader(byte[] b, int off, int len) throws IOException{
		if(archiveHeaderPointer == 0){
			bufferArchiveHeader = new byte[archiveHeaderLength];
		}
		
		int noff= (int) Math.min(archiveHeaderLength-archiveHeaderPointer, len);
		System.arraycopy(b, off, bufferArchiveHeader, archiveHeaderPointer, noff);
		archiveHeaderPointer += noff;
		
		if(archiveHeaderPointer == archiveHeaderLength){
			byte[] verBuffer = new byte[4];
			System.arraycopy(bufferArchiveHeader, 0, verBuffer, 0, 4);
			if(ByteBuffer.wrap(verBuffer).getInt() > version){
				throw new IOException("File not decomposible (obsolete FolderStream)");
			}
			
			byte[] lengthBuffer = new byte[8];
			System.arraycopy(bufferArchiveHeader, 8, lengthBuffer, 0, 8);
			totalLength = ByteBuffer.wrap(lengthBuffer).getLong();
			
			readingArchiveHeader = false;
			
			readingHeader = true;
			headerPointer = 0;
			
			getHeader(b, off+noff, len-noff);
		}
	}
	
	protected void getHeader(byte[] b, int off, int len) throws IOException{
		if(headerPointer == 0){
			bufferHeader = new byte[headerLength];
		}
		
		int noff = (int) Math.min(len, headerLength - headerPointer);
		System.arraycopy(b, off, bufferHeader, headerPointer, noff);
		headerPointer += noff;
		
		if(headerPointer == 16){
			
			int found = -1;
			for(int i = 0; i < 16; i ++){
				if(bufferHeader[i] == headerStart[0]){
					found = i;
					break;
				}
			}
			
			if(found == 0){
			
				fileNamePointer = 0;
				
				int notHeader = -1;
				byte[] idBuffer = new byte[4];
				System.arraycopy(bufferHeader, 0, idBuffer, 0, 4);
				for(int i = 0; i < 4; i++){
					if(idBuffer[i] != headerStart[i]){
						notHeader = i;
						break;
					}
				}
			
				if(notHeader == -1){
					byte[] lengthBuffer = new byte[8];
					System.arraycopy(bufferHeader, 4, lengthBuffer, 0, 8);
					fileLength = ByteBuffer.wrap(lengthBuffer).getLong();
			
					byte[] nameBuffer = new byte[4];
					System.arraycopy(bufferHeader, 12, nameBuffer, 0, 4);
					fileNameLength = ByteBuffer.wrap(nameBuffer).getInt();
					bufferFileName = new byte[fileNameLength];
				
					readingHeader = false;
					headerPointer = 0;
					readingFileName = true;
					fileNamePointer = 0;
				
					getFileName(b, off+noff, len-noff);
				} else {
					headerPointer = 16 - notHeader;
					byte[] newBufferHeader = new byte[16];
					System.arraycopy(bufferHeader, notHeader, newBufferHeader, 0, headerPointer);
				}
			} else if(found == -1){
				headerPointer = 0;
			} else {
				headerPointer = 16 - found;
				byte[] newBufferHeader = new byte[16];
				System.arraycopy(bufferHeader, found, newBufferHeader, 0, headerPointer);
			}
		}	
	}
	
	protected void getFileName(byte[] b, int off, int len) throws IOException{
		int noff= (int) Math.min(fileNameLength-fileNamePointer, len);
		System.arraycopy(b, off, bufferFileName, fileNamePointer, noff);
		fileNamePointer += noff;
		
		if(fileNamePointer == fileNameLength){
			File f = new File(dest.getPath() + new String(bufferFileName));
			if(f.exists()){
				f.delete();
			}
			
			File fDir = f.getParentFile();
			if(fDir != null && !fDir.exists()){
				fDir.mkdirs();
			}
			fDir = null;
			
			f.createNewFile();
			current = new BufferedOutputStream(new FileOutputStream(f));
			f = null;
			pointer = 0;
			readingFileName = false;
			readingFile = true;
			getFile(b, off+noff, len-noff);
		} 
	}
	
	protected void getFile(byte[] b, int off, int len) throws IOException{
		int noff= (int) Math.min(fileLength-pointer, len);
		current.write(b, off, noff);
		pointer += noff;
		if(pointer == fileLength){
			current.flush();
			current.close();
			current = null;
			readingFile = false;
			readingHeader = true;
			getHeader(b, off+noff, len-noff);
		}
		
	}

}
