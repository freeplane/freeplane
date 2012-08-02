package org.docear.plugin.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearProgressObserver;


public class ProgressInputStream extends InputStream {
	
	private final InputStream stream;
	private final URL url;
	private int length = 0;
	private int progress = 0;
	private boolean closed = false;
		
	public ProgressInputStream(URLConnection connection) throws IOException {
		this.length = connection.getContentLength();		
		this.stream = connection.getInputStream();
		this.url = connection.getURL();
	}
	
	public ProgressInputStream(InputStream stream, URL url) throws IOException {
		this.stream = stream;
		this.length = stream.available();
		this.url = url;
	}

	public ProgressInputStream(InputStream stream, URL url, int length) {
		this.stream = stream;
		this.length = length;
		this.url = url;
	}

	@Override
	public int read() throws IOException {
		if(closed) {
			fireProgessFinished();
			throw new InterruptedIOException();
		}
		int read = this.stream.read();
		this.progress++;
		if(read >= 0) {			
			adjustProgress();
			fireProgessUpdated();
		}
		else {
			fireProgessFinished();
		}
		return read; 
	}
	
	private void fireProgessUpdated() {
		Collection<DocearProgressObserver> observers = DocearController.getController().getProgressObservers(this.getClass());
		for(DocearProgressObserver observer : observers) {
			observer.update(this, this.progress, this.length, url.toString());
		}
	}
	
	private void fireProgessFinished() {
		Collection<DocearProgressObserver> observers = DocearController.getController().getProgressObservers(this.getClass());
		for(DocearProgressObserver observer : observers) {
			observer.finished(this, url.toString());
		}
	}

	public int available() throws IOException {
		return this.stream.available();		
	}
	
	private void adjustProgress() throws IOException {
		int streamLength = this.progress+available(); 
		if(this.length < streamLength) {
			this.length = streamLength+1;
		}
	}
	
	public void close() throws IOException {
		this.stream.close();
		closed = true;
	}

}
