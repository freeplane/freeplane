package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.MutableRibbonTask;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class JRibbonBandBuilder implements EntryVisitor {

	private EntryAccessor accessor;
	private ComponentProvider provider;
	
	public JRibbonBandBuilder(ResourceAccessor resourceAccessor) {
		this(resourceAccessor, new RibbonBandComponentProvider(resourceAccessor));
	}
	
	public JRibbonBandBuilder(ResourceAccessor resourceAccessor, ComponentProvider componentProvider) {
		accessor = new EntryAccessor(resourceAccessor);
		this.provider = componentProvider;
	}

	@Override
	public void visit(Entry target) {
		RibbonBandContainer container = new RibbonBandContainer((JRibbonBand)provider.createComponent(target));
		accessor.setComponent(target, container);
		((MutableRibbonTask) accessor.getAncestorComponent(target)).addBand(container.getBand());
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}

class RibbonBandContainer extends JRibbonContainer {
	final private JRibbonBand band;

	public RibbonBandContainer(JRibbonBand band) {
		if(band == null) throw new IllegalArgumentException("band is NULL!");
		this.band = band;
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		AbstractCommandButton button = (AbstractCommandButton) comp;
		RibbonElementPriority priority = (RibbonElementPriority)button.getClientProperty(RibbonActionComponentProvider.PRIORITY_PROPERTY);
		band.addCommandButton(button, priority != null ? priority : RibbonElementPriority.MEDIUM);
	}

	public JRibbonBand getBand() {
		return band;
	}

	@Override
	public Component getParent() {
		return band;
	}	
}
