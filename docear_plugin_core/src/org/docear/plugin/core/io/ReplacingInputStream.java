package org.docear.plugin.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class ReplacingInputStream extends InputStream {

	private final InputStream innerStream;
	private Map<String, String> mapping;
	private StringBuffer buffer = new StringBuffer();
	private Set<String> keys; 
	
	public ReplacingInputStream(Map<String, String> replaceMap, InputStream is) {
		this.innerStream = is;
		this.mapping = replaceMap;
		this.keys = mapping.keySet();
	}
	
	public int available() throws IOException{
		return innerStream.available()+buffer.length();
	}

	public int read() throws IOException {
		if(buffer.length() > 0) {
			try {
				return buffer.charAt(0);
			}
			finally {
				buffer.deleteCharAt(0);
			}
		}
		int c = innerStream.read();
		if(c > -1) {
			buffer.append((char)c);
			boolean try2Replace = false;
			while(keyStartsWith(buffer.toString())) {
				c = innerStream.read();
				buffer.append((char)c);
				if(keys.contains(buffer.toString())) {
					try2Replace = true;
					break;
				}
			}
			
			if(try2Replace) {
				tryToReplace();
			}
			
			try {
				return buffer.charAt(0);
			}
			finally {
				buffer.deleteCharAt(0);
			}
		}
		return c;
	}
	
	private void tryToReplace() {
		String replacement = mapping.get(buffer.toString());
		if(replacement != null) {
			buffer.delete(0, buffer.length());
			buffer.append(replacement);		
		}
	}

	public boolean keyStartsWith(String prefix) {
		for(String key : keys) {
			if(key.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
	
	public void close() throws IOException {
		innerStream.close();
	}

}
