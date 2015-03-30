package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.MenuSplitterConfiguration;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.ui.menubuilders.action.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JMenuItemBuilder implements EntryVisitor{

	final private EntryPopupListener popupListener;
	final private ResourceAccessor resourceAccessor;
	final private MenuSplitter menuSplitter;

	public JMenuItemBuilder(EntryPopupListener popupListener, ResourceAccessor resourceAccessor) {
		this.popupListener = popupListener;
		this.resourceAccessor = resourceAccessor;
		menuSplitter = new MenuSplitter(resourceAccessor.getIntProperty(
		    MenuSplitterConfiguration.MAX_MENU_ITEM_COUNT_KEY, 10));
	}

	@Override
	public void visit(Entry entry) {
		if (entry.hasChildren() && !entry.getName().isEmpty())
			addSubmenu(entry);
		else
			addActionItem(entry);
	}

	private void addActionItem(Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		if(actionComponent != null){
			addComponent(entry, actionComponent);
		}
	}

	private void addComponent(Entry entry, final Component component) {
	    entry.setComponent(component);
	    final Container container = (Container) entry.getAncestorComponent();
	    if (container instanceof JMenu)
	    	menuSplitter.addMenuComponent((JMenu) container, component);
	    else
	    	container.add(component);
    }

	private void addSubmenu(final Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		JMenu menu = new JMenu();
		String name = entry.getName();
		final String key = name + ".icon";
		final String iconResource = resourceAccessor.getProperty(key);
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, resourceAccessor.getRawText(name));
		if(iconResource != null){
			final URL url = resourceAccessor.getResource(iconResource);
			menu.setIcon(new ImageIcon(url));
		}
		addComponent(entry, menu);
		if(actionComponent != null){
			menuSplitter.addMenuComponent(menu, actionComponent);
		}
		final JPopupMenu popupMenu = menu.getPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				popupListener.childEntriesWillBecomeVisible(entry);
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				popupListener.childEntriesWillBecomeInvisible(entry);
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	private Component createActionComponent(Entry entry) {
		final AFreeplaneAction action = entry.getAction();
		final Component actionComponent;
		if(action != null){
			if (action.isSelectable()) {
				actionComponent = new JAutoCheckBoxMenuItem(action);
			}
			else {
				actionComponent = new JFreeplaneMenuItem(action);
			}
		}
		else if(entry.builders().contains("separator")){
			actionComponent = new JPopupMenu.Separator();
		}
		else
			actionComponent = null;
		return actionComponent;
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
