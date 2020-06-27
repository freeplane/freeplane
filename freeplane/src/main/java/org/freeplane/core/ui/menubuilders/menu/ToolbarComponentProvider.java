package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToolBar.Separator;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;

public class ToolbarComponentProvider implements ComponentProvider {

	/* (non-Javadoc)
	 * @see org.freeplane.core.ui.menubuilders.menu.ComponentProvider#createComponent(org.freeplane.core.ui.menubuilders.generic.Entry)
	 */
	@Override
    public Component createComponent(Entry entry) {
	    final EntryAccessor entryAccessor = new EntryAccessor();
		final Object existingComponent = entryAccessor.getComponent(entry);
		if (existingComponent != null)
			return (Component) existingComponent;
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		Component component;
		if(action != null){
		    AbstractButton actionComponent;
			if (action.isSelectable()) {
				actionComponent = new JAutoToggleButton(action);
			}
			else {
				actionComponent = new JButton(action);
			}
			IconReplacer.replaceByImageIcon(actionComponent);
			component = actionComponent;
		}
		else if(entry.builders().contains("separator")){
			component = new Separator();
		}
		else
			component = null;
	    return component;
	}
}
