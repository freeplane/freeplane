package org.freeplane.features.presentations.mindmapmode;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

final class UpDownKeyEventDispatcher implements KeyEventDispatcher {
	private final PresentationState state;
	public UpDownKeyEventDispatcher(PresentationState state) {
		super();
		// TODO Auto-generated constructor stub
		this.state = state;
	}
	private boolean isActive = false;
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if(! isActive ||e.getModifiers() != 0)
			return false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
				e.consume();
				state.showPreviousSlide();
				break;
		case KeyEvent.VK_DOWN:
				e.consume();
				state.showNextSlide();
				break;
		default:
			// do nothings
		}
		return true;
	}
	public void activate() {
		isActive = true;
	}
	public void deactivate() {
		isActive = false;
	}
}