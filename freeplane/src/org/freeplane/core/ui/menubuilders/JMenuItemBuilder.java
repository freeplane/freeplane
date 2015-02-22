package org.freeplane.core.ui.menubuilders;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;

public class JMenuItemBuilder implements EntryVisitor{

	@Override
	public void visit(Entry entry) {
		final AFreeplaneAction action = entry.getAction();
		Component component;
		if(action != null){
			if (action.isSelectable()) {
				component = new JAutoCheckBoxMenuItem(action);
			}
			else {
				component = new JFreeplaneMenuItem(action);
			}
		}
		else if(entry.builders().contains("separator")){
			component = new JPopupMenu.Separator();
		}
		else
			component = null;
		if(component != null){
			entry.setComponent(component);
			final JMenu container = ((JMenu) entry.getAncestorComponent());
			container.getPopupMenu().add(component);
		}
	}

	@Override
	public boolean shouldSkipChildren() {
		// TODO Auto-generated method stub
		return false;
	}

}
