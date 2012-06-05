package org.docear.plugin.communications.features;

import java.io.IOException;
import java.io.InputStream;

public final class DocearServiceResponse {
	
	public enum Status {
		OK, FAILURE
	}
	
	
	private final Status status;
	private final InputStream content;
	private StringBuffer buffer;
	
	public DocearServiceResponse(Status status, InputStream contentStream) {
		content = contentStream;
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public InputStream getContent() {
		return content;
	}

	public String getContentAsString() {
		buffer = new StringBuffer();
		if(buffer.length() <= 0) {
			int ch = -1;
			try {
				while((ch = content.read()) > -1) {
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
}
