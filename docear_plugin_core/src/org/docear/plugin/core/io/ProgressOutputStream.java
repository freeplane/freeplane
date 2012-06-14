package org.docear.plugin.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.DocearProgressObserver;

public class ProgressOutputStream extends OutputStream {

	private int length = 0;
	private int progress = 0;
	private final OutputStream stream;	
	
	public ProgressOutputStream(OutputStream stream, int length) throws IOException{
		this.stream = stream;
		this.length = length;
	}
	
	@Override
	public void write(int b) throws IOException {
		this.stream.write(b);
		progress++;
		fireProgessUpdated();
	}
	
	private void fireProgessUpdated() {
		Collection<DocearProgressObserver> observers = DocearController.getController().getProgressObservers(this.getClass());
		for(DocearProgressObserver observer : observers) {
			observer.update(this, this.progress, this.length);
		}
	}

}
