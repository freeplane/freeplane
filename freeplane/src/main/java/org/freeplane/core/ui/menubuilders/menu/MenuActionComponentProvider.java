package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class MenuActionComponentProvider implements ComponentProvider {

    private static final Icon SELECTABLE_ICON = ResourceController.getResourceController().getIcon("/images/selectable.svg");
    private IAcceleratorMap accelerators;
	private AcceleratebleActionProvider acceleratebleActionProvider;
	private EntryAccessor entryAccessor;

	public MenuActionComponentProvider(IAcceleratorMap accelerators, AcceleratebleActionProvider acceleratebleActionProvider,
	                                   ResourceAccessor resourceAccessor) {
		this.accelerators = accelerators;
		this.acceleratebleActionProvider = acceleratebleActionProvider;
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
			boolean isActionSelectable = action.isSelectable();
            if (isActionSelectable) {
				actionComponent = new JAutoCheckBoxMenuItem(wrappedAction);
			}
			else {
				actionComponent = new JFreeplaneMenuItem(wrappedAction);
			}
			final KeyStroke accelerator = accelerators.getAccelerator(action);
			actionComponent.setAccelerator(accelerator);
			boolean shouldRemoveMenuIcon = entryAccessor.shouldRemoveMenuIcon(entry);
            if(shouldRemoveMenuIcon && ! isActionSelectable)
			    actionComponent.setIcon(null);
            else if(isActionSelectable && (shouldRemoveMenuIcon || actionComponent.getIcon() == null)) {
			    actionComponent.setIcon(SELECTABLE_ICON);
			}
			IconReplacer.replaceByScaledImageIcon(actionComponent);
			return actionComponent;
		}
		else if(entry.builders().contains("separator")){
			return new JPopupMenu.Separator();
		}
		else
			return null;
	}
}
