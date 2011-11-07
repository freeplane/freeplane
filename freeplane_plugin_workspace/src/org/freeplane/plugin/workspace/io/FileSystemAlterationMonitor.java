/**
 * author: Marcel Genzmehr
 * 03.11.2011
 */
package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * 
 */
public class FileSystemAlterationMonitor {
	private final FileAlterationMonitor monitor;
	private boolean isRunning;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public FileSystemAlterationMonitor(long interval) {
		monitor = new FileAlterationMonitor(interval);
		start();
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public final void addFileSystemListener(File file, FileAlterationListener listener) {
		Iterator<FileAlterationObserver> observers = monitor.getObservers().iterator();
		while(observers.hasNext()) {
			FileAlterationObserver observer = observers.next();
			if(observer.getDirectory().getPath().equals(file.getPath())) {
				observer.removeListener(listener);
				observer.addListener(listener);
				return;
			}
		}
		if(file.exists() && file.isDirectory()) {
			FileAlterationObserver observer = new FileAlterationObserver(file);
			observer.addListener(listener);
			monitor.addObserver(observer);
		}
	}
	
	public final void removeFileSystemListener(File file, FileAlterationListener listener) {
		Iterator<FileAlterationObserver> observers = monitor.getObservers().iterator();
		while(observers.hasNext()) {
			FileAlterationObserver observer = observers.next();
			if(observer.getDirectory().getPath().equals(file.getPath())) {
				Iterator<FileAlterationListener> listeners = observer.getListeners().iterator();
				while(listeners.hasNext()) {
					if(listeners.next().equals(listener)) {
						listeners.remove();
					}
				}
			}
		}
	}
	
	public final boolean isRunning() {
		return isRunning;
	}
	
	public final void stop() {
		if(isRunning()) {	
			try {
				monitor.stop();
			}
			catch (Exception e) {
				e.printStackTrace();			
			}
		}
		isRunning = false;
	}
	
	public final void start() {
		if(isRunning()) {
			return;
		}		
		try {
			monitor.start();
			isRunning = true;
		}
		catch (Exception e) {
			isRunning = false;
			e.printStackTrace();			
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
