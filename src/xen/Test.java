package xen;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class Test {

	public static void main(String[] args) throws Exception {
		File f = new File("bob.sprk");
		
		RandomAccessFile r = new RandomAccessFile(f,"rw");
		FileChannel c = r.getChannel();
		c.position(c.size());
		c.transferTo(0, 5, c);
		
		c.close();
		r.close();
	}

}
