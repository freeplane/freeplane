package org.docear.plugin.communications.features;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class DocearServiceResponse {
	
	public enum Status {
		OK, FAILURE
	}
	
	
	private final Status status;
	private byte[] content;
	
	private final StringBuffer buffer;
	private int length;
	
	public DocearServiceResponse(Status status, InputStream contentStream) {
		buffer = new StringBuffer();
		content = fill(contentStream);
		this.status = status;
	}

	

	public Status getStatus() {
		return status;
	}

	public InputStream getContent() {
		return new ByteArrayInputStream(content,0, length);
	}

	public String getContentAsString() {
		if(buffer.length() <= 0) {
			InputStream is = getContent();
			int ch = -1;
			try {
				while((ch = is.read()) > -1) {
					buffer.append((char)ch);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}
	
	public String toString() {
		return status+" "+getContentAsString();
	}
	
	private byte[] fill(InputStream contentStream) {
		int max = 8192;
		byte[] buf = new byte[max];
		int b = -1;
		int pos = 0;
		try {		
			while((b = contentStream.read()) > -1) {
				buf[pos++] = (byte) b;
				if(pos >= max) {
					max = pos * 2;
					byte nbuf[] = new byte[max];
					System.arraycopy(buf, 0, nbuf, 0, pos);
					buf = nbuf;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		length = pos;
		return buf;
	}
}
