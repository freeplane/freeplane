package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.JSeparator;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionEnabler;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;

public class JRibbonActionBuilder implements EntryVisitor {

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
			
			final AFreeplaneAction action = entryAccessor.getAction(entry);
			if(action != null) 
				action.addPropertyChangeListener(new ActionEnabler(actionComponent));
		}
	}

	private void addComponent(Entry entry, final Component component) {
		if(entry.isLeaf()) {
			entryAccessor.setComponent(entry, component);
		}
		else {
			entryAccessor.setComponent(entry, addPopupCallback(entry, (JCommandButton)component));
		}
		Object parent = entryAccessor.getAncestorComponent(entry);
		if(parent instanceof JRibbonContainer) {
			final JRibbonContainer container = (JRibbonContainer) parent; 
			container.add(component);
		}
	}

	private JRibbonContainer addPopupCallback(final Entry entry, final JCommandButton button) {
	    RibbonPopupWrapper callback = new RibbonPopupWrapper(entry, popupListener, button);
		button.setPopupCallback(callback);
		return callback;
    }

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}

class RibbonPopupWrapper extends JRibbonContainer implements PopupPanelCallback {

	final private EntryPopupListener popupListener;
	final private JCommandPopupMenu popupmenu;
	final private EntryAccessor entryAccessor;
	final private Entry entry;
	final private Component parent;
	
	public RibbonPopupWrapper(Entry entry, EntryPopupListener popupListener, Component parent) {
		this.entry = entry;
		this.entryAccessor = new EntryAccessor();
		this.popupmenu = new JCommandPopupMenu();
		this.popupListener = popupListener;
		this.parent = parent;
	}

	@Override
	public void add(Component component, Object constraints, int index) {
		if(component instanceof JSeparator) {
			popupmenu.addMenuSeparator();
		}
		else {
			if(component instanceof JCommandToggleButton) {
				popupmenu.addMenuButton((JCommandToggleMenuButton)component);
			}
			else {
				popupmenu.addMenuButton((JCommandMenuButton)component);
			}
		}
		
	}

	@Override
	public JPopupPanel getPopupPanel(JCommandButton commandButton) {
		fireChildEntriesWillBecomeVisible(entry);		
		return popupmenu;
	}
	
	protected void fireChildEntriesWillBecomeVisible(final Entry entry) {
		popupListener.childEntriesWillBecomeVisible(entry);
		for (Entry child : entry.children())
			if (!(entryAccessor.getComponent(child) instanceof RibbonPopupWrapper))
				fireChildEntriesWillBecomeVisible(child);
	}

	protected void fireChildEntriesWillBecomeInvisible(final Entry entry) {
        popupListener.childEntriesWillBecomeInvisible(entry);
		for (Entry child : entry.children())
			if (!(entryAccessor.getComponent(child) instanceof RibbonPopupWrapper))
				fireChildEntriesWillBecomeInvisible(child);
    }

	@Override
	public Component getParent() {
		return this.parent;
	}
}


