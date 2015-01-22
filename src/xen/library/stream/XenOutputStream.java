package xen.library.stream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Compressor;

public class XenOutputStream extends BufferedOutputStream{
	
	public XenOutputStream(OutputStream out, Cipher c, LZ4Compressor l, int bufferSize) throws IOException {
		super(new LZ4BlockOutputStream(new CipherOutputStream(new BufferedOutputStream(out), c), bufferSize, l));
	}

	public XenOutputStream(OutputStream out, XenParameters en) throws IOException {
		this(out, en.getCipher(Cipher.ENCRYPT_MODE), en.getCompressor(), en.getBufferSize());
	}
	
}
