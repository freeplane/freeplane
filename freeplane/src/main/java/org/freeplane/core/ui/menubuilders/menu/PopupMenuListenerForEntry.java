package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

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
		fireChildEntriesWillBecomeVisible(entry);
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				fireChildEntriesHidden(entry);
			}
		});
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