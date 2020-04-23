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
	private static final String GENERAL_NO_ACTIONS_TEXT = TextUtils.getText("menu.noActions");
    private final EntryAccessor entryAccessor;

	public EmptyMenuItemBuilder(ResourceAccessor resourceAccessor) {
		this.entryAccessor = new EntryAccessor(resourceAccessor);
	}
	@Override
	public void visit(Entry target) {
		final Container container = getMenuItemContainer(target);
		if(container.getComponentCount() == 0){
			String menuName = target.getParent().getName();
            final String text = getMenuItemText(menuName);
			final JFreeplaneMenuItem noActionItem = new JFreeplaneMenuItem(text);
			noActionItem.setEnabled(false);
			entryAccessor.setComponent(target, noActionItem);
			container.add(noActionItem);
		}
	}
	
    private String getMenuItemText(String menuName) {
        String menuSpecificText = TextUtils.getText(menuName + ".noActions", GENERAL_NO_ACTIONS_TEXT);
        return menuSpecificText;
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
