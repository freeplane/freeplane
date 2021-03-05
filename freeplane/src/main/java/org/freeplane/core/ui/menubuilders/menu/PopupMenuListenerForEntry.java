package org.freeplane.core.ui.menubuilders.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.RecursiveMenuStructureProcessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

class PopupMenuListenerForEntry implements PopupMenuListener{

	@SuppressWarnings("serial")
	static private class PopupTimer extends Timer {
		private static final int DELAY =  20;
		public PopupTimer(ActionListener listener) {
			super(DELAY, listener);
		}};
	private final Entry entry;
	private final EntryPopupListener popupListener;
	final EntryAccessor entryAccessor;

	PopupMenuListenerForEntry(Entry entry, EntryPopupListener popupListener, ResourceAccessor resourceAccessor) {
		this.entry = entry;
		this.popupListener = popupListener;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		PopupTimer popupTimer = entry.getAttribute(PopupTimer.class);
		if(popupTimer != null) {
			popupTimer.stop();
			entry.removeAttribute(PopupTimer.class);
			fireChildEntriesHidden(entry);
		}
		fireChildEntriesWillBecomeVisible(entry);
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		PopupTimer popupTimer = new PopupTimer(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				entry.removeAttribute(PopupTimer.class);
				fireChildEntriesHidden(entry);
			}
		});
		entry.setAttribute(PopupTimer.class, popupTimer);
		popupTimer.setRepeats(false);
		popupTimer.start();
	}

	private void fireChildEntriesWillBecomeVisible(final Entry entry) {
		popupListener.childEntriesWillBecomeVisible(entry);
		for (Entry child : entry.children())
			if (!RecursiveMenuStructureProcessor.shouldProcessUiOnEvent(child))
				fireChildEntriesWillBecomeVisible(child);
	}

	private void fireChildEntriesHidden(final Entry entry) {
	    popupListener.childEntriesHidden(entry);
		for (Entry child : entry.children())
			if (!RecursiveMenuStructureProcessor.shouldProcessUiOnEvent(child))
				fireChildEntriesHidden(child);
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}
}