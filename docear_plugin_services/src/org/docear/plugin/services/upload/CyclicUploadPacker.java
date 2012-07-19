package org.docear.plugin.services.upload;

import org.docear.plugin.core.features.DocearThread;

public class CyclicUploadPacker extends DocearThread {

	private final Runnable target;
	private final int interval;

	/**
	 * @param target - the procedure that should be executed cyclic
	 * @param interval - sleep time in seconds 
	 */
	public CyclicUploadPacker(Runnable target, int interval) {
		super("Docear Service Upload-Packer-Thread");
		this.target = target;
		this.interval = interval*1000;
	}
	
	public void execute() throws InterruptedException {
		if(target != null) {
			while(!isTerminated()) {
				target.run();
				if(!isInterrupted()) {
					sleep(interval);
				}
			}
		}			
	}
	
}