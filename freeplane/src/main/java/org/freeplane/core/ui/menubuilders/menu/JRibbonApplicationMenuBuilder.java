package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;

public class JRibbonApplicationMenuBuilder implements EntryVisitor {
	final ResourceAccessor resourceAccessor;
	
	public JRibbonApplicationMenuBuilder(ResourceAccessor resourceAccessor) {
		super();
		this.resourceAccessor = resourceAccessor;
	}
	@Override
	public void visit(Entry entry) {
		new EntryAccessor().setComponent(entry, initApplicationMenu(entry));
	}
	
	private RibbonApplicationMenuContainerImpl initApplicationMenu(Entry entry) {
		JRibbon ribbon = ((JRibbon)new EntryAccessor().getAncestorComponent(entry));
		final RibbonApplicationMenuContainerImpl container = new RibbonApplicationMenuContainerImpl(ribbon);
		//TODO - replace with resourceAccessor?
		String appName = ResourceController.getResourceController().getProperty("ApplicationName", "Freeplane");
		URL location = ResourceController.getResourceController().getResource("/images/"+appName.trim()+"_app_menu_128.png");
		if (location != null) {
			ResizableIcon icon = ImageWrapperResizableIcon.getIcon(location, new Dimension(32, 32));
			ribbon.setApplicationIcon(icon);
		}
		return container;
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}

interface RibbonApplicationMenuContainer {
	public void add(RibbonApplicationMenuEntryPrimary comp);
	
	public void add(SecondaryEntryGroup comp);
	
	public void add(RibbonApplicationMenuEntryFooter comp);
}

class RibbonApplicationMenuContainerImpl implements RibbonApplicationMenuContainer {

	private final JRibbon ribbon;

	public RibbonApplicationMenuContainerImpl(JRibbon ribbon) {
		this.ribbon = ribbon;
	}

	public void add(RibbonApplicationMenuEntryPrimary comp) {
		RibbonApplicationMenu appMenu = cloneMenu();
		appMenu.addMenuEntry(comp);
		ribbon.setApplicationMenu(appMenu);
	}

	public void add(SecondaryEntryGroup comp) {
		throw new RuntimeException("not supported!");
	}
	
	public void add(RibbonApplicationMenuEntryFooter comp) {
		System.out.println("add Footer: " + comp);
	}
	
	private RibbonApplicationMenu cloneMenu() {
		RibbonApplicationMenu appMenu = new RibbonApplicationMenu();
		RibbonApplicationMenu oldMenu = ribbon.getApplicationMenu();
		if(oldMenu != null) {
			for(RibbonApplicationMenuEntryFooter footer : oldMenu.getFooterEntries()) {
				appMenu.addFooterEntry(footer);
			}
			for(RibbonApplicationMenuEntryPrimary entry : oldMenu.getPrimaryEntries().get(oldMenu.getPrimaryEntries().size() - 1)) {
				appMenu.addMenuEntry(entry);
			}
		}
		return appMenu;
	}
}
