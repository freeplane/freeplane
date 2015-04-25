package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionEnabler;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.MenuSplitterConfiguration;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
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
	final private ComponentProvider menuActionComponentProvider;

	public JMenuItemBuilder(EntryPopupListener popupListener, IAcceleratorMap accelerators,
	                        AcceleratebleActionProvider acceleratebleActionProvider, ResourceAccessor resourceAccessor) {
		this(popupListener,
		    new MenuActionComponentProvider(accelerators, acceleratebleActionProvider, resourceAccessor),
		    resourceAccessor);
	}

	public JMenuItemBuilder(EntryPopupListener popupListener, ComponentProvider menuActionComponentProvider,
	                        ResourceAccessor resourceAccessor) {
		this.popupListener = popupListener;
		this.resourceAccessor = resourceAccessor;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
		menuSplitter = new MenuSplitter(resourceAccessor.getIntProperty(
		    MenuSplitterConfiguration.MAX_MENU_ITEM_COUNT_KEY, 10));
		this.menuActionComponentProvider = menuActionComponentProvider;
	}

	@Override
	public void visit(Entry entry) {
		if ((entry.hasChildren() || entryAccessor.getAction(entry) == null) && !entryAccessor.getText(entry).isEmpty())
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

	private Component createActionComponent(Entry entry) {
	    final Component component = menuActionComponentProvider.createComponent(entry);
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		if (action != null)
			action.addPropertyChangeListener(new ActionEnabler(component));
		return component;
    }

	private void addComponent(Entry entry, final Component component) {
		entryAccessor.setComponent(entry, component);
		final Container container = (Container) entryAccessor.getAncestorComponent(entry);
		if (container != null)
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
		addPopupMenuListener(entry, menu.getPopupMenu());

	}

	protected void addPopupMenuListener(final Entry entry, final JPopupMenu popupMenu) {
	    popupMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				fireChildEntriesWillBecomeVisible(entry);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				fireChildEntriesWillBecomeInvisible(entry);
			}

			private void fireChildEntriesWillBecomeVisible(final Entry entry) {
				popupListener.childEntriesWillBecomeVisible(entry);
				for (Entry child : entry.children())
					if (!(entryAccessor.getComponent(child) instanceof JMenu))
						fireChildEntriesWillBecomeVisible(child);
			}

			private void fireChildEntriesWillBecomeInvisible(final Entry entry) {
	            popupListener.childEntriesWillBecomeInvisible(entry);
				for (Entry child : entry.children())
					if (!(entryAccessor.getComponent(child) instanceof JMenu))
						fireChildEntriesWillBecomeInvisible(child);
            }
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
    }

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
