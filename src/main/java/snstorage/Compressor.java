package snstorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public abstract class Compressor {
	
	protected static final int COMPRESSION_LEVEL = 4;
	
	public abstract byte[] getBytes() throws UnsupportedEncodingException;
	
	public abstract void parseBytes(byte[] b) throws UnsupportedEncodingException;
	
	public byte[] getCompressedBytes() {
		
		byte[] serialization;
		try {
			serialization = this.getBytes();
			Deflater deflater = new Deflater();
			deflater.setLevel(COMPRESSION_LEVEL);
			deflater.setInput(serialization);	
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			deflater.finish();
			while (!deflater.finished()) {
				int count = deflater.deflate(buffer);
				stream.write(buffer, 0, count);
			}
			
			stream.close();
			byte[] result = stream.toByteArray();
			return result;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	
	}
	
	public void parseCompressedBytes(byte[] array) {
		Inflater inflater = new Inflater();
		inflater.setInput(array);	
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = 0;
			try {
				count = inflater.inflate(buffer);
			} catch (DataFormatException e) {
				e.printStackTrace();
			}
			stream.write(buffer, 0, count);
		}
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] decompressed = stream.toByteArray();	
		try {
			this.parseBytes(decompressed);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	
	}	

}
