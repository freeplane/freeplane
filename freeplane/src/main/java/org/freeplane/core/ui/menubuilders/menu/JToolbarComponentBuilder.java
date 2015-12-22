package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionEnabler;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JToolbarComponentBuilder implements EntryVisitor {

	private final ComponentProvider componentProvider;

	public JToolbarComponentBuilder(ComponentProvider componentProvider) {
		super();
		this.componentProvider = componentProvider;
	}

	public JToolbarComponentBuilder() {
		this(new ToolbarComponentProvider());
	}


	@Override
	public void visit(Entry entry) {
		Component component = componentProvider.createComponent(entry);
		if(component != null){
			final EntryAccessor entryAccessor = new EntryAccessor();
			entryAccessor.setComponent(entry, component);
			final AFreeplaneAction action = entryAccessor.getAction(entry);
			if (action != null) {
				final ActionEnabler actionEnabler = new ActionEnabler(component);
				action.addPropertyChangeListener(actionEnabler);
				entry.setAttribute(actionEnabler.getClass(), actionEnabler);
			}
			final Container container = (Container) new EntryAccessor().getAncestorComponent(entry);
			if (container instanceof JToolBar)
				container.add(component);
			else
				SwingUtilities.getAncestorOfClass(JToolBar.class, container).add(component);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}
