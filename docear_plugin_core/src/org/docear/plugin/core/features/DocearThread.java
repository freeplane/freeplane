package org.docear.plugin.core.features;

import org.docear.plugin.core.logging.DocearLogger;

public abstract class DocearThread extends Thread {
	private final String name;
	private boolean terminated = false;
	
	public DocearThread(String name) {	
		this.name = name;
	}
	
	public final void run() {
		DocearLogger.info(this.toString()+" starting...");
		try {
			this.execute();
		} catch (InterruptedException e) {
		}
	}
	
	public final synchronized void start() {
		super.start();
	}
	
	public final void terminate() {
		this.terminated  = true; 
	}
	
	public final boolean isTerminated() {
		return this.terminated;
	}
	
	public abstract void execute() throws InterruptedException;
	
	
	public String toString() {
		return this.name;
	}
}
