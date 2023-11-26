package org.freeplane.core.ui.components;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AccelerateableAction;

public class JFreeplaneMenuItem extends JMenuItem implements IKeyBindingManager {
	public JFreeplaneMenuItem() {
		super();
	}

	public JFreeplaneMenuItem(final Action a) {
		super(a);
	}

	public JFreeplaneMenuItem(final Icon icon) {
		super(icon);
	}

	public JFreeplaneMenuItem(final String text, final Icon icon) {
		super(text, icon);
	}

	public JFreeplaneMenuItem(final String text, final int mnemonic) {
		super(text, mnemonic);
	}

	public JFreeplaneMenuItem(final String text) {
		super(text);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean isKeyBindingProcessed = false;

	@Override
	protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed) {
		try {
			isKeyBindingProcessed = true;
			return super.processKeyBinding(ks, e, condition, pressed);
		}
		finally {
			isKeyBindingProcessed = false;
		}
	}

	public boolean isKeyBindingProcessed() {
		return isKeyBindingProcessed;
	}

	protected void processMouseEvent(MouseEvent e){
		if (e.getID() == MouseEvent.MOUSE_RELEASED && contains(e.getPoint()) && AccelerateableAction.isNewNodeLinkedToMenuItemEnabled() ) {
			doClick();
		} else {
			super.processMouseEvent(e);
		}
	}

}
