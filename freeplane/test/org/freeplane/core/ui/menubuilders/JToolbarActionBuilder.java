package org.freeplane.core.ui.menubuilders;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoToggleButton;

public class JToolbarActionBuilder implements Builder {

	public JToolbarActionBuilder() {
	}

	@Override
	public void build(Entry entry) {
		final AFreeplaneAction action = entry.getAction();
		if(action != null){
			Component button;
			if (action.getClass().getAnnotation(SelectableAction.class) != null) {
				button = new JAutoToggleButton(action);
			}
			else {
				button = new JButton(action);
			}
			entry.setComponent(button);
			final Container container = (Container) entry.getAncestorComponent();
			container.add(button);
		}
	}

}
