package org.freeplane.core.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Timer;



public class DelayedMouseListener implements IMouseListener {
	final private IMouseListener delegate;
	public void mouseDragged(MouseEvent e) {
	    delegate.mouseDragged(e);
    }

	public void mouseMoved(MouseEvent e) {
	    delegate.mouseMoved(e);
    }

	final private int button;
	private int clickCounter = 0;
	private Timer timer = null;
	static final private int MAX_TIME_BETWEEN_CLICKS = 
		(Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
	final private int maxClickNumber;
	

	public void mouseClicked(final MouseEvent me) {
		if(me.getButton() != button){
			delegate.mouseClicked(me);
			return;
		}
		if(timer != null){
			timer.stop();
			timer = null;
			clickCounter++;
		}
		else{
			clickCounter = 1;
		}
		if(clickCounter == maxClickNumber){
			delegate.mouseClicked(me);
			delegate.mouseReleased(me);
			return;
		}
		timer = new Timer(MAX_TIME_BETWEEN_CLICKS, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final MouseEvent newMouseEvent = new MouseEvent(me.getComponent(),me.getID(), e.getWhen(), me.getModifiers(), me.getX(), me.getY(), clickCounter, me.isPopupTrigger(), button);
				delegate.mouseClicked(newMouseEvent);
				delegate.mouseReleased(newMouseEvent);
				timer = null;
			}
		});
		timer.setRepeats(false);
		timer.start();
//		me.consume();
    }

	public void mouseEntered(MouseEvent e) {
	    delegate.mouseEntered(e);
    }

	public void mouseExited(MouseEvent e) {
	    delegate.mouseExited(e);
    }

	public void mousePressed(MouseEvent e) {
	    delegate.mousePressed(e);
    }

	public void mouseReleased(MouseEvent me) {
		if(me.getButton() != button){
			delegate.mouseReleased(me);
			return;
		}
		me.consume();
    }

	public DelayedMouseListener(IMouseListener delegate, int maxClickNumber, int button) {
	    super();
	    this.delegate = delegate;
	    this.maxClickNumber = maxClickNumber;
	    this.button = button;
    }
}
