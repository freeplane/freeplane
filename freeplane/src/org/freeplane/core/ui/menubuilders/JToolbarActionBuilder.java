package org.freeplane.core.ui.menubuilders;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoToggleButton;

public class JToolbarActionBuilder implements EntryVisitor {

	public JToolbarActionBuilder() {
	}

	@Override
	public void visit(Entry entry) {
		final AFreeplaneAction action = entry.getAction();
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
			entry.setComponent(component);
			final Container container = (Container) entry.getAncestorComponent();
			container.add(component);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		// TODO Auto-generated method stub
		return false;
	}
}
