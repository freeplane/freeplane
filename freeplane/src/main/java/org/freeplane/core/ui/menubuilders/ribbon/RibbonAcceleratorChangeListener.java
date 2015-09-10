package org.freeplane.core.ui.menubuilders.ribbon;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IAcceleratorChangeListener;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;

public class RibbonAcceleratorChangeListener implements IAcceleratorChangeListener {

	private EntriesForAction entries;
	private EntryAccessor entryAccessor;
	private RibbonComponentDecorator decorator;

	public RibbonAcceleratorChangeListener(ResourceAccessor resourceAccessor, IAcceleratorMap acceleratorMap, EntriesForAction entries) {
		this.entries = entries;
		this.entryAccessor = new EntryAccessor();
		this.decorator = new RibbonComponentDecorator(resourceAccessor, acceleratorMap);
	}

	@Override
	public void acceleratorChanged(AFreeplaneAction action, KeyStroke oldStroke, KeyStroke newStroke) {
		for(Entry entry : entries.entries(action)) {
			Object comp = entryAccessor.getComponent(entry);
			if(comp instanceof AbstractCommandButton) {
				decorator.updateRichTooltip(comp, action, newStroke);
			} 
			else if (comp instanceof JRibbonContainer) {
				comp = ((JRibbonContainer) comp).getParent();
				if(comp instanceof AbstractCommandButton) {
					decorator.updateRichTooltip(comp, action, newStroke);
				}
			}
		}
	}
}
