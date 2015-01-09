package org.freeplane.core.ui.menubuilders;

import java.awt.Container;

import javax.swing.JButton;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;

public class JToolbarActionGroupBuilder implements Builder {

	public JToolbarActionGroupBuilder() {
	}

	@Override
	public void build(Entry entry) {
		final AFreeplaneAction action = entry.getAction();
		if(action != null){
			final JButton button = new JButton(action);
			entry.setComponent(button);
			final Container container = (Container) entry.getParent().getComponent();
			container.add(button);
		}
	}

}
