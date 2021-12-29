package net.jkdev.rushhour.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class IOUtil{
	
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	
	public static byte[] readResource(String name) throws IOException{
		return readFully(ClassLoader.getSystemResourceAsStream(name));
	}
	
	public static String readResourceUTF8(String name) throws IOException{
		return new String(readResource(name), StandardCharsets.UTF_8);
	}
	
	public static int copy(InputStream in, OutputStream out, byte[] buffer, int offset, int length) throws IOException{
		int read;
		int len = 0;
		while((read = in.read(buffer, offset, length)) != -1){
			out.write(buffer, offset, read);
			len += read;
		}
		return len;
	}
	
	public static void copyFast(InputStream in, OutputStream out, byte[] buffer, int offset, int length) throws IOException{
		int read;
		while((read = in.read(buffer, offset, length)) != -1){
			out.write(buffer, offset, read);
		}
	}
	
	public static int copy(InputStream in, OutputStream out) throws IOException{
		int size = DEFAULT_BUFFER_SIZE;
		return copy(in, out, new byte[size], 0, size);
	}
	
	public static void copyFast(InputStream in, OutputStream out) throws IOException{
		int size = DEFAULT_BUFFER_SIZE;
		copyFast(in, out, new byte[size], 0, size);
	}
	
	public static byte[] readFully(InputStream in, int bufferSize) throws IOException{
		int read;
		int offset = 0;
		int remaining = bufferSize;
		byte[] buffer = new byte[bufferSize];
		while((read = in.read(buffer, offset, remaining)) != -1){
			offset += read;
			if((remaining -= read) == 0){
				byte[] temp = new byte[(remaining = buffer.length) * 2];
				System.arraycopy(buffer, 0, temp, 0, remaining);
				buffer = temp;
			}
		}
		byte[] result = new byte[offset];
		System.arraycopy(buffer, 0, result, 0, result.length);
		return result;
	}
	
	public static byte[] readFully(InputStream in) throws IOException{
		return readFully(in, DEFAULT_BUFFER_SIZE);
	}
	
	public static ByteBuffer readFully(ReadableByteChannel ch, int bufferSize) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		while(ch.read(buffer) != -1){
			if(buffer.remaining() == 0){
				buffer.flip();
				List<ByteBuffer> list = new ArrayList<>();
				list.add(buffer);
				buffer = ByteBuffer.allocateDirect(bufferSize);
				while(ch.read(buffer) != -1){
					if(buffer.remaining() == 0){
						buffer.flip();
						list.add(buffer);
						buffer = ByteBuffer.allocateDirect(bufferSize);
					}
				}
				buffer.flip();
				buffer = ByteBuffer.allocateDirect(bufferSize * list.size() + buffer.remaining());
				for(ByteBuffer chunk : list){
					buffer.put(chunk);
				}
				buffer.position(0);
				return buffer;
			}
		}
		buffer.flip();
		return buffer.slice();
	}

	public static ByteBuffer readFully(ReadableByteChannel ch) throws InterruptedException, IOException{
		return readFully(ch, DEFAULT_BUFFER_SIZE);
	}
	
	public static OutputStream getMultiOutputStream(OutputStream...outputs){
		return new OutputStream(){
			@Override
			public void write(int b) throws IOException{
				for(OutputStream output : outputs){
					output.write(b);
				}
			}
			
			@Override
			public void write(byte[] b, int off, int len) throws IOException{
				for(OutputStream output : outputs){
					output.write(b, off, len);
				}
			}
			
			@Override
			public void flush() throws IOException{
				flush(0, outputs.length);
			}
			
			@Override
			public void close() throws IOException{
				close(0, outputs.length);
			}
			
			private void flush(int index, int length) throws IOException{
				try{
					outputs[index].flush();
				}finally{
					if(++index < length){
						flush(index, length);
					}
				}
			}
			
			private void close(int index, int length) throws IOException{
				try{
					outputs[index].close();
				}finally{
					if(++index < length){
						close(index, length);
					}
				}
			}
		};
	}
	
	public static void tunnelFast(InputStream input, OutputStream tunnel, int bufferSize) throws IOException{
		byte[] buffer = new byte[bufferSize];
		int read;
		while((read = input.read(buffer, 0, bufferSize)) != -1){
			tunnel.write(buffer, 0, read);
		}
	}
	
	public static int tunnel(InputStream input, OutputStream tunnel, int bufferSize) throws IOException{
		byte[] buffer = new byte[bufferSize];
		int read;
		int c = 0;
		while((read = input.read(buffer, 0, bufferSize)) != -1){
			tunnel.write(buffer, 0, read);
			c += read;
		}
		return c;
	}
	
	public static void tunnelGzipFast(InputStream input, OutputStream tunnel, int bufferSize) throws IOException{
		try(GZIPOutputStream gzipOut = new GZIPOutputStream(tunnel, bufferSize)){
			byte[] buffer = new byte[bufferSize];
			int read;
			while((read = input.read(buffer, 0, bufferSize)) != -1){
				gzipOut.write(buffer, 0, read);
			}
			gzipOut.flush();
		}
	}
	
	public static void tunnelGzipFast(InputStream input, OutputStream tunnel) throws IOException{
		tunnelGzipFast(input, tunnel, DEFAULT_BUFFER_SIZE);
	}
	
	public static int tunnelGzip(InputStream input, OutputStream tunnel, int bufferSize) throws IOException{
		try(GZIPOutputStream gzipOut = new GZIPOutputStream(tunnel, bufferSize)){
			byte[] buffer = new byte[bufferSize];
			int read;
			int c = 0;
			while((read = input.read(buffer, 0, bufferSize)) != -1){
				gzipOut.write(buffer, 0, read);
				c += read;
			}
			gzipOut.flush();
			return c;
		}
	}
	
	public static int tunnelGzip(InputStream input, OutputStream tunnel) throws IOException{
		return tunnelGzip(input, tunnel, DEFAULT_BUFFER_SIZE);
	}
	
	public static Map<String, List<String>> getParameters(String url){
		if(url == null || url.isEmpty()){
			return Collections.emptyMap();
		}
		int paramStart = url.indexOf('?');
		if(paramStart == -1){
			return Collections.emptyMap();
		}
		try{
			url = URLDecoder.decode(url, "UTF-8");
		}catch(UnsupportedEncodingException e){
			System.err.println("Could not decode given url: " + url);
			throw new IllegalStateException(e);
		}
		return Arrays.stream(url.substring(paramStart + 1).split("&")).map(IOUtil::getParam).collect(Collectors.groupingBy(
				SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}
	
	public static SimpleImmutableEntry<String, String> getParam(String param){
		final int idx = param.indexOf("=");
		final String key = idx > 0 ? param.substring(0, idx) : param;
		final String value = idx > 0 && param.length() > idx + 1 ? param.substring(idx + 1) : null;
		return new SimpleImmutableEntry<>(key, value);
	}
}
