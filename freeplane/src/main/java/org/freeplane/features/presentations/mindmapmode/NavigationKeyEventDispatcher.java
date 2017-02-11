package org.freeplane.features.presentations.mindmapmode;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

class NavigationKeyEventDispatcher implements KeyEventDispatcher {
	private final PresentationState state;
	public NavigationKeyEventDispatcher(PresentationState state) {
		super();
		this.state = state;
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if(e.getModifiers() != 0)
			return false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
				e.consume();
				if(e.getID() == KeyEvent.KEY_PRESSED)
					state.showPreviousSlide();
				return true;
		case KeyEvent.VK_DOWN:
				e.consume();
				if(e.getID() == KeyEvent.KEY_PRESSED)
					state.showNextSlide();
				return true;
		default:
			 return false;
		}
		
	}
}