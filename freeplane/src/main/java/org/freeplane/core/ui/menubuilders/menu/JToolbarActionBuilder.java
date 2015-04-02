package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JToolbarActionBuilder implements EntryVisitor {

	public JToolbarActionBuilder() {
	}

	@Override
	public void visit(Entry entry) {
		final AFreeplaneAction action = new EntryAccessor().getAction(entry);
		Component component;
		if(action != null){
			if (action.isSelectable()) {
				component = new JAutoToggleButton(action);
			}
			else {
				component = new JButton(action);
			}
		}
		else if(entry.builders().contains("separator")){
			component = new JToolBar.Separator();
		}
		else
			component = null;
		if(component != null){
			new EntryAccessor().setComponent(entry, component);
			final Container container = (Container) new EntryAccessor().getAncestorComponent(entry);
			if (container instanceof JToolBar)
				container.add(component);
			else
				SwingUtilities.getAncestorOfClass(JToolBar.class, container).add(component);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}
}
