package xen.library.stream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import net.jpountz.lz4.LZ4BlockInputStream;

public class XenInputStream extends BufferedInputStream{
	
	public XenInputStream(InputStream in, Cipher c) throws IOException {
		super(new LZ4BlockInputStream(new BufferedInputStream(new CipherInputStream(new BufferedInputStream(in), c))));
	}
	
	public XenInputStream(InputStream in, XenParameters de) throws IOException {
		this(in, de.getCipher(Cipher.DECRYPT_MODE));
	}
	
}
