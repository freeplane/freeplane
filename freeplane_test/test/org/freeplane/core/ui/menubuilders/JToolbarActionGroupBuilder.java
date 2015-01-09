package org.freeplane.core.ui.menubuilders;

import java.awt.Container;

import javax.swing.JButton;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.FreeplaneActions;

public class JToolbarActionGroupBuilder implements Builder {

	private FreeplaneActions freeplaneActions;

	public JToolbarActionGroupBuilder(FreeplaneActions freeplaneActions) {
		this.freeplaneActions = freeplaneActions;
	}

	@Override
	public void build(Entry entry) {
		final String actionName = entry.getAttribute("action");
		final AFreeplaneAction action = freeplaneActions.getAction(actionName);
		final JButton button = new JButton(action);
		entry.setComponent(button);
		final Container container = (Container) entry.getParent().getComponent();
		container.add(button);
	}

}
