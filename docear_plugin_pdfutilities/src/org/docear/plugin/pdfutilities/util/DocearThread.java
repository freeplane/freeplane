package org.docear.plugin.pdfutilities.util;

import java.beans.PropertyChangeListener;

import javax.swing.event.SwingPropertyChangeSupport;

import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.freeplane.core.util.LogUtils;


public abstract class DocearThread< T, V > extends Thread{
	
	private T result = null;	
	private int progress;
	protected SwingPropertyChangeSupport changeSupport;
	private boolean canceled = false;
	
	public DocearThread(){
		super();
		this.setName("DocearThread"); //$NON-NLS-1$
	}
	
	public void run(){
		try {
			result = this.doInBackground();			
		} catch (Exception e) {
			LogUtils.severe("Exception in Docear Thread: " + e.getMessage()); //$NON-NLS-1$
		} finally {
			this.done();
		}
		
	}
	
	public T get(){
		return result;
	}
	
	public boolean isCancelled(){
		return canceled;
	}
	
	protected abstract T doInBackground() throws Exception;
	
	protected abstract void done();

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {		
		this.firePropertyChange(SwingWorkerDialog.PROGRESS, this.progress, progress);
		this.progress = progress;
	}

	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null) {
	    changeSupport = new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }
	
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (changeSupport == null || 
	    (oldValue != null && newValue != null && oldValue.equals(newValue))) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

	public synchronized void cancel(boolean interrupt) {
		this.canceled = true;
		if(interrupt){
			this.interrupt();
		}		
	}

}
