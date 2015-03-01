package org.freeplane.core.ui.menubuilders;

import java.awt.Component;
import java.awt.Container;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.util.TextUtils;

public class JMenuItemBuilder implements EntryVisitor{

	private EntryPopupListener popupListener;

	public JMenuItemBuilder(EntryPopupListener popupListener) {
		this.popupListener = popupListener;
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
		JMenu menu = createMenuEntry(entry);
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

	protected JMenu createMenuEntry(final Entry entry) {
		JMenu menu = new JMenu();
		String name = entry.getName();
		final String iconResource = ResourceController.getResourceController().getProperty(name + ".icon", null);
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, TextUtils.getRawText(name));
		if(iconResource != null){
			final URL url = ResourceController.getResourceController().getResource(iconResource);
			menu.setIcon(new ImageIcon(url));
		}
		return menu;
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
	public boolean shouldSkipChildren() {
		return false;
	}

}
