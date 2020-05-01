package org.freeplane.core.ui.menubuilders.menu;

import javax.swing.JPopupMenu;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class PopupBuilder implements EntryVisitor {
	private final EntryPopupListener popupListener;
	private final JPopupMenu popupMenu;
    private final ResourceAccessor resourceAccessor;

	public PopupBuilder(final JPopupMenu nodePopupMenu, final EntryPopupListener popupListener, 
	        ResourceAccessor resourceAccessor) {
		super();
		this.popupListener =popupListener;
		this.popupMenu = nodePopupMenu;
        this.resourceAccessor = resourceAccessor;
	}

	@Override
	public void visit(Entry target) {
		popupMenu.addPopupMenuListener(new PopupMenuListenerForEntry(target, popupListener, resourceAccessor));
		EntryAccessor entryAccessor = new EntryAccessor();
		entryAccessor.setText(target, "popup");
        entryAccessor.setComponent(target, popupMenu);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
