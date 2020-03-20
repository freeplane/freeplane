package org.freeplane.core.ui.menubuilders.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.Compat;

class PopupMenuListenerForEntry implements PopupMenuListener{
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
		if(Compat.isMacOsX()) {
			Timer macOsMenuTimer = entry.getAttribute(Timer.class);
			if(macOsMenuTimer != null) {
				macOsMenuTimer.stop();
				entry.removeAttribute(Timer.class);
				fireChildEntriesHidden(entry);
			}
		}
		fireChildEntriesWillBecomeVisible(entry);
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		if(! Compat.isMacOsX()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireChildEntriesHidden(entry);
				}
			});
		}
		else {
			Timer macOsMenuTimer = new Timer(20, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					entry.removeAttribute(Timer.class);
					fireChildEntriesHidden(entry);
				}
			});
			entry.setAttribute(Timer.class, macOsMenuTimer);
			macOsMenuTimer.setRepeats(false);
			macOsMenuTimer.start();
		}
	}

	private boolean containsSubmenu(Entry entry) {
	    if (entry.hasChildren() || entryAccessor.getAction(entry) == null) {
            String text = entryAccessor.getText(entry);
            return text != null && !text.isEmpty();
	    }
	    return false;
	}

	private void fireChildEntriesWillBecomeVisible(final Entry entry) {
		popupListener.childEntriesWillBecomeVisible(entry);
		for (Entry child : entry.children())
			if (!containsSubmenu(child))
				fireChildEntriesWillBecomeVisible(child);
	}

	private void fireChildEntriesHidden(final Entry entry) {
	    popupListener.childEntriesHidden(entry);
		for (Entry child : entry.children())
			if (!containsSubmenu(child))
				fireChildEntriesHidden(child);
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}
}