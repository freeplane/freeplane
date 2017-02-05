package org.freeplane.features.presentations.mindmapmode;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

class EscapeKeyEventDispatcher implements KeyEventDispatcher {
	private final PresentationState state;
	public EscapeKeyEventDispatcher(PresentationState state) {
		super();
		this.state = state;
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if(e.getModifiers() != 0)
			return false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
				e.consume();
				if(e.getID() == KeyEvent.KEY_PRESSED)
					state.stopPresentation();
				return true;
		default:
			return false;
		}
	}
}