package org.freeplane.core.ui.menubuilders.ribbon;

import java.awt.Component;
import java.util.ArrayList;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.menu.ComponentProvider;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class RibbonBandComponentProvider implements ComponentProvider {

	public static final String RESIZE_POLICIES_ATTRIBUTE = "resize_policies";
	
	final private EntryAccessor entryAccessor;
	
	public RibbonBandComponentProvider(ResourceAccessor resourceAccessor) {
		entryAccessor = new EntryAccessor(resourceAccessor);
	}

	@Override
	public Component createComponent(Entry entry) {
		String title = TextUtils.removeMnemonic(entryAccessor.getText(entry));
		JRibbonBand band = new JRibbonBand(title, null);
		ArrayList<RibbonBandResizePolicy> list = new ArrayList<>();
		//simple default resize policy because band is empty on creation and it'll raise errors otherwise
		list.add(new IconRibbonBandResizePolicy(band.getControlPanel()));
		band.setResizePolicies(list);
		return band;
	}
}
