package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JSeparator;
import javax.swing.JToolBar;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionEnabler;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class JToolbarComponentBuilder implements EntryVisitor {
	private final ComponentProvider componentProvider;

	public JToolbarComponentBuilder(ComponentProvider componentProvider) {
		super();
		this.componentProvider = componentProvider;
	}

	public JToolbarComponentBuilder(ResourceAccessor resourceAccessor) {
		this(new ToolbarComponentProvider(resourceAccessor));
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
			final JToolBar container = (JToolBar) new EntryAccessor().getAncestorComponent(entry);
			GridBagConstraints constraints = layoutConstraintsForEntry(entry, component);
			container.add(component, constraints);
		}
	}

	public static GridBagConstraints layoutConstraintsForEntry(Entry entry, Component component) {
		final int gridWidth;
		final int gridHeight;
		String rowSpec = (String) entry.getAttribute("row");
		if(rowSpec == null)
			rowSpec = (String) entry.getParent().getAttribute("row");
		final int row;
		if ("2".equals(rowSpec))
			row = 1;
		else
			row = 0;
		String widthSpec =(String)entry.getAttribute("width");
		if(widthSpec != null)
			gridWidth = Integer.valueOf(widthSpec);
		else
			gridWidth = 1;
		if(rowSpec == null || component instanceof JSeparator) {
			gridHeight = 2;
		} else {
			gridHeight = 1;
		}
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = GridBagConstraints.RELATIVE;
		constraints.gridy = row;
		constraints.gridwidth = gridWidth;
		constraints.gridheight = gridHeight;
		String weightSpec = (String) entry.getAttribute("weight");
		constraints.weightx = weightSpec != null ? Integer.parseInt(weightSpec) : 0;
		constraints.fill = gridHeight == 1 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.BOTH;
		constraints.anchor = entry.builders().contains("dropdownMenu") ? GridBagConstraints.SOUTH : GridBagConstraints.NORTHEAST;
		return constraints;
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		final EntryAccessor entryAccessor = new EntryAccessor();
		return entryAccessor.getAction(entry) != null || entry.builders().contains("dropdownMenu");
	}
}
