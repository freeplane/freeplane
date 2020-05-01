package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.ui.components.JAutoRadioButtonMenuItem;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class MenuRadioActionComponentProvider implements ComponentProvider {

	private IAcceleratorMap accelerators;
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private EntryAccessor entryAccessor;
	private ButtonGroup buttonGroup;

	public MenuRadioActionComponentProvider(IAcceleratorMap accelerators, AcceleratebleActionProvider acceleratebleActionProvider,
	                                        ResourceAccessor resourceAccessor, ButtonGroup buttonGroup) {
		this.accelerators = accelerators;
		this.acceleratebleActionProvider = acceleratebleActionProvider;
		this.buttonGroup = buttonGroup;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
	}

	/* (non-Javadoc)
	 * @see org.freeplane.core.ui.menubuilders.menu.ComponentProvider#createComponent(org.freeplane.core.ui.menubuilders.generic.Entry)
	 */
	@Override
    public Component createComponent(Entry entry) {
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		if(action != null){
			final JMenuItem actionComponent;
			IFreeplaneAction wrappedAction = acceleratebleActionProvider.wrap(action);
			if (action.isSelectable()) {
				actionComponent = new JAutoRadioButtonMenuItem(wrappedAction, buttonGroup);
			}
			else {
				actionComponent = new JRadioButtonMenuItem(wrappedAction);
			}
			buttonGroup.add(actionComponent);
			final KeyStroke accelerator = accelerators.getAccelerator(action);
			actionComponent.setAccelerator(accelerator);
			MenuIconScaling.scaleIcon(actionComponent);
			return actionComponent;
		}
		else if(entry.builders().contains("separator")){
			return new JPopupMenu.Separator();
		}
		else
			return null;
	}
}
