package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class JRibbonActionBuilder implements EntryVisitor{

	final private EntryPopupListener popupListener;
	final ResourceAccessor resourceAccessor;
	final private EntryAccessor entryAccessor;
	final private ComponentProvider actionComponentProvider;

	public JRibbonActionBuilder(EntryPopupListener popupListener, IAcceleratorMap accelerators,
	                        AcceleratebleActionProvider acceleratebleActionProvider, ResourceAccessor resourceAccessor) {
		this(popupListener,
		    new RibbonActionComponentProvider(accelerators, acceleratebleActionProvider, resourceAccessor), 
		    resourceAccessor);
	}

	public JRibbonActionBuilder(EntryPopupListener popupListener, ComponentProvider actionComponentProvider,
	                        ResourceAccessor resourceAccessor) {
		this.popupListener = popupListener;
		this.resourceAccessor = resourceAccessor;
		this.entryAccessor = new EntryAccessor(resourceAccessor);		
		this.actionComponentProvider = actionComponentProvider;
	}

	@Override
	public void visit(Entry entry) {
		final Component actionComponent = actionComponentProvider.createComponent(entry);
		if(actionComponent != null){
			addComponent(entry, actionComponent);
		}
	}

	private void addComponent(Entry entry, final Component component) {
		entryAccessor.setComponent(entry, component);
		Object parent = entryAccessor.getAncestorComponent(entry);
		if(parent instanceof JRibbonContainer) {
			final JRibbonContainer container = (JRibbonContainer) parent; 
			container.add(component);
		}
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
