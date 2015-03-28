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
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.ui.menubuilders.action.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JMenuItemBuilder implements EntryVisitor{

	final private EntryPopupListener popupListener;
	final private ResourceAccessor menuEntryBuilder;

	public JMenuItemBuilder(EntryPopupListener popupListener, ResourceAccessor menuEntryBuilder) {
		this.popupListener = popupListener;
		this.menuEntryBuilder = menuEntryBuilder;
	}

	@Override
	public void visit(Entry entry) {
		if(entry.hasChildren())
			addSubmenu(entry);
		else
			addActionItem(entry);
	}

	private void addActionItem(Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		if(actionComponent != null){
			entry.setComponent(actionComponent);
			final Container container = getParentComponent(entry);
			container.add(actionComponent);
		}
	}

	private void addSubmenu(final Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		final Container container = getParentComponent(entry);
		JMenu menu = new JMenu();
		String name = entry.getName();
		final String key = name + ".icon";
		final String iconResource = menuEntryBuilder.getProperty(key);
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, menuEntryBuilder.getRawText(name));
		if(iconResource != null){
			final URL url = menuEntryBuilder.getResource(iconResource);
			menu.setIcon(new ImageIcon(url));
		}
		entry.setComponent(menu);
		container.add(menu);
		final JPopupMenu popupMenu = menu.getPopupMenu();
		if(actionComponent != null){
			popupMenu.add(actionComponent);
		}
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

	private Container getParentComponent(final Entry entry) {
		final Container ancestorComponent = (Container) entry.getAncestorComponent();
		if(ancestorComponent instanceof JMenu)
			return ((JMenu) ancestorComponent).getPopupMenu();
		else 
			return ancestorComponent;
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
