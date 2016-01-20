package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Container;

import javax.swing.JMenu;

import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.TextUtils;

public class EmptyMenuItemBuilder implements EntryVisitor {
	private final EntryAccessor entryAccessor;

	public EmptyMenuItemBuilder(ResourceAccessor resourceAccessor) {
		this.entryAccessor = new EntryAccessor(resourceAccessor);
	}
	@Override
	public void visit(Entry target) {
		final Container container = getMenuItemContainer(target);
		if(container.getComponentCount() == 0){
			final String text = TextUtils.getText(target.getParent().getName() + ".noActions");
			final JFreeplaneMenuItem noActionItem = new JFreeplaneMenuItem(text);
			noActionItem.setEnabled(false);
			entryAccessor.setComponent(target, noActionItem);
			container.add(noActionItem);
		}
	}
	
	private Container getMenuItemContainer(Entry target) {
		final Container ancestorComponent = (Container) entryAccessor.getAncestorComponent(target);
		if(ancestorComponent instanceof JMenu)
			return ((JMenu)ancestorComponent).getPopupMenu();
		else
			return ancestorComponent;
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}

}
