package org.docear.plugin.core.features;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logging.DocearLogger;

public abstract class DocearThread extends Thread {
	private final String name;
	private boolean terminated = false;
	
	public DocearThread(String name) {	
		this.name = name;
		setName(name);
	}
	
	public final void run() {
		DocearLogger.info(this.toString()+" starting...");
		String threadID = Integer.toHexString(this.hashCode())+toString();
		try {
			DocearController.getController().addWorkingThreadHandle(threadID);
			this.execute();
		} 
		catch (InterruptedException e) {
		}
		finally {
			DocearController.getController().removeWorkingThreadHandle(threadID);
		}
	}
	
	public final synchronized void start() {
		super.start();
	}
	
	public final void terminate() {
		this.terminated  = true;
		this.interrupt();
	}
	
	public final boolean isTerminated() {
		return this.terminated;
	}
	
	public abstract void execute() throws InterruptedException;
	
	
	public String toString() {
		return this.name;
	}
}
