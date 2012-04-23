package org.freeplane.core.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class DoubleClickTimer{
	static final public int MAX_TIME_BETWEEN_CLICKS;
	static {
		final Object p = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
		MAX_TIME_BETWEEN_CLICKS = p instanceof Integer ? (Integer) p : 250;
	}
	private Timer timer;
	private int delay;
	public int getDelay() {
    	return delay;
    }

	public void setDelay(int delay) {
    	this.delay = delay;
    }

	public DoubleClickTimer() {
    }
	
	public void start(final Runnable runnable){
		if(runnable == null)
			return;
		cancel();
		if(delay == 0){
			runnable.run();
			return;
		}
		timer = new Timer(delay, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timer = null;
				runnable.run();
			}
		});
		timer.setRepeats(false);
		timer.start();
	}
	
	public void cancel() {
		if (timer != null){
			timer.stop();
			timer = null;
		}
	}

}
