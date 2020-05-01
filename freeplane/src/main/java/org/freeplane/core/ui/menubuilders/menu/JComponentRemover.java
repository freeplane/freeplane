package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.AbstractButton;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionEnabler;
import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;

public class JComponentRemover implements EntryVisitor{
	public static JComponentRemover INSTANCE = new JComponentRemover();
	private JComponentRemover(){/**/}
	final private MenuSplitter menuSplitter = new MenuSplitter(0);

	@Override
	public void visit(Entry target) {
		final EntryAccessor entryAccessor = new EntryAccessor();
		final Component component = (Component) entryAccessor.removeComponent(target);
		if (component != null) {
			if(component instanceof AbstractButton)
				((AbstractButton)component).setAction(null);
			removeMenuComponent(component);
			ActionEnabler actionEnabler = target.removeAttribute(ActionEnabler.class);
			if(actionEnabler != null){
				final AFreeplaneAction action = entryAccessor.getAction(target);
				action.removePropertyChangeListener(actionEnabler);
			}
		}
	}

	private void removeMenuComponent(final Component component) {
		menuSplitter.removeMenuComponent(component);
    }

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
	
}