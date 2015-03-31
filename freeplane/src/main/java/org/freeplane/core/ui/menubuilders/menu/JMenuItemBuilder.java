package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Icon;
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
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class JMenuItemBuilder implements EntryVisitor{

	final private EntryPopupListener popupListener;
	final ResourceAccessor resourceAccessor;
	final private MenuSplitter menuSplitter;
	final private EntryAccessor entryAccessor;

	public JMenuItemBuilder(EntryPopupListener popupListener, ResourceAccessor resourceAccessor) {
		this.popupListener = popupListener;
		this.resourceAccessor = resourceAccessor;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
		menuSplitter = new MenuSplitter(resourceAccessor.getIntProperty(
		    MenuSplitterConfiguration.MAX_MENU_ITEM_COUNT_KEY, 10));
	}

	@Override
	public void visit(Entry entry) {
		if (entry.hasChildren() && !entryAccessor.getText(entry).isEmpty())
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
	    new EntryAccessor().setComponent(entry, component);
	    final Container container = (Container) new EntryAccessor().getAncestorComponent(entry);
		menuSplitter.addComponent(container, component);
    }

	private void addSubmenu(final Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		JMenu menu = new JMenu();
		final String rawText = entryAccessor.getText(entry);
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, rawText);
		final Icon icon = entryAccessor.getIcon(entry);
		if (icon != null) {
			menu.setIcon(icon);
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
		final AFreeplaneAction action = new EntryAccessor().getAction(entry);
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
