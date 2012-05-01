package org.freeplane.core.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.Timer;

public class AutoHide {
	private static final int DELAY = 500;
	final Timer timer;
	final JComponent popup;
	
	public static void start(JComponent popup){
		new AutoHide(popup);
	}
	
	private AutoHide(JComponent popup) {
	    super();
	    this.popup = popup;
	    this.timer = new Timer(DELAY, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tryToClosePopup();
			}
		});
	    timer.setRepeats(true);
	    popup.addMouseListener(new MouseAdapter() {
			@Override
            public void mouseEntered(MouseEvent e) {
				AutoHide.this.popup.removeMouseListener(this);
			    timer.start();
            }
	    	
		});
    }
	protected void tryToClosePopup() {
		if(popup.isVisible()) {
	        if (popup.getMousePosition(true) == null) {
	        	popup.setVisible(false);
	        	timer.stop();
	        }
        }
		else
			timer.stop();
    }
	
}
