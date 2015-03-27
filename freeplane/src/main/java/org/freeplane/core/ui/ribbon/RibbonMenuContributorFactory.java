package org.freeplane.core.ui.ribbon;

import java.util.Properties;

import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;

public class RibbonMenuContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			private RibbonApplicationMenu menu;

			public String getKey() {
				return "app_menu";
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				menu = new RibbonApplicationMenu();
				context.processChildren(context.getCurrentPath(), this);
				parent.addChild(menu, null);
			}

			public void addChild(Object child, ChildProperties properties) {
				if(child instanceof RibbonApplicationMenuEntryFooter) {
					menu.addFooterEntry((RibbonApplicationMenuEntryFooter) child);
				}
				else if(child instanceof RibbonApplicationMenuEntryPrimary) {
					menu.addMenuEntry((RibbonApplicationMenuEntryPrimary) child);
				}
			}
		};
	}

}
