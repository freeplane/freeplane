package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

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
			component = new Separator();
		}
		else
			component = null;
	    return component;
	}
}
