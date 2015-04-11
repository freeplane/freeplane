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

public class JRibbonApplicationMenuBuilder implements EntryVisitor {
	final private RibbonApplicationMenuContainer container;
	final ResourceAccessor resourceAccessor;
	
	public JRibbonApplicationMenuBuilder(ResourceAccessor resourceAccessor) {
		super();
		this.container = new RibbonApplicationMenuContainer(new RibbonApplicationMenu());
		this.resourceAccessor = resourceAccessor;
	}
	@Override
	public void visit(Entry entry) {
		new EntryAccessor().setComponent(entry, this.container);
		initApplicationMenu(entry);
	}
	
	private void initApplicationMenu(Entry entry) {
		JRibbon ribbon = ((JRibbon)new EntryAccessor().getAncestorComponent(entry));
		ribbon.setApplicationMenu(container.getApplicationMenu());
		//TODO - replace with resourceAccessor?
		String appName = ResourceController.getResourceController().getProperty("ApplicationName", "Freeplane");
		URL location = ResourceController.getResourceController().getResource("/images/"+appName.trim()+"_app_menu_128.png");
		if (location != null) {
			ResizableIcon icon = ImageWrapperResizableIcon.getIcon(location, new Dimension(32, 32));
			ribbon.setApplicationIcon(icon);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}

class RibbonApplicationMenuContainer extends JRibbonContainer {
	final private RibbonApplicationMenu appMenu;

	public RibbonApplicationMenuContainer(RibbonApplicationMenu menu) {
		this.appMenu = menu;
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		System.out.println("add to AppMenu: " + comp);
	}

	public RibbonApplicationMenu getApplicationMenu() {
		return appMenu;
	}	
}
