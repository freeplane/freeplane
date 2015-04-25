package org.freeplane.core.ui;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

class DelegatingPopupMenuListener implements PopupMenuListener {
	final private PopupMenuListener listener;
	final private Object source;

	public DelegatingPopupMenuListener(final PopupMenuListener listener, final Object source) {
		super();
		this.listener = listener;
		this.source = source;
	}

	public Object getSource() {
		return source;
	}

	private PopupMenuEvent newEvent() {
		return new PopupMenuEvent(source);
	}

	public void popupMenuCanceled(final PopupMenuEvent e) {
		listener.popupMenuCanceled(newEvent());
	}

	public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
		listener.popupMenuWillBecomeInvisible(newEvent());
	}

	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		listener.popupMenuWillBecomeVisible(newEvent());
	}
}