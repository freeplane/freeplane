package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.JPopupMenu;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class PopupBuilder implements EntryVisitor {
	final private EntryPopupListener popupListener;
	private final JPopupMenu nodePopupMenu;

	public PopupBuilder(final JPopupMenu nodePopupMenu, final EntryPopupListener popupListener) {
		super();
		this.popupListener =popupListener;
		this.nodePopupMenu = nodePopupMenu;
	}

	@Override
	public void visit(Entry target) {
		nodePopupMenu.addPopupMenuListener(new PopupMenuListenerForEntry(target, popupListener));
		new EntryAccessor().setComponent(target, nodePopupMenu);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
