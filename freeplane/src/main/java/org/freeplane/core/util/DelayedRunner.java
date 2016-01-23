package org.freeplane.core.util;

import javax.swing.SwingUtilities;

public class DelayedRunner {
	private final Runnable runnable;
	private boolean runTriggered;
	public DelayedRunner(Runnable runnable) {
		super();
		this.runnable = runnable;
		runTriggered = false;
	}
	public void runLater() {
		if(! runTriggered){
			runTriggered = true;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					runTriggered = false;
					runnable.run();
				}
			});
		}
	}
}