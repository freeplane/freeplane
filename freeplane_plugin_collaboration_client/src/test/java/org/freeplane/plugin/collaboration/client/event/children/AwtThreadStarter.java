package org.freeplane.plugin.collaboration.client.event.children;

import javax.swing.SwingUtilities;

public class AwtThreadStarter {
	static {
		startAwtThread();
	}
	
	private static void startAwtThread(){
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					// intentionally left blank
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void await() {
	}
}