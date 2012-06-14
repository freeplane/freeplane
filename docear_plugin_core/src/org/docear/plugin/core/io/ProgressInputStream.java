package org.docear.plugin.core.io;

import java.io.IOException;
import java.io.InputStream;
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

	@Override
	public int read() throws IOException {
		int read = this.stream.read();
		this.progress++;
		if(read >= 0) {			
			adjustProgress();
			fireProgessUpdated();
		}
		else {
			fireProgessUpdated();
		}
		return read; 
	}
	
	private void fireProgessUpdated() {
		Collection<DocearProgressObserver> observers = DocearController.getController().getProgressObservers(this.getClass());
		for(DocearProgressObserver observer : observers) {
			observer.update(url, this.progress, this.length);
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

}
