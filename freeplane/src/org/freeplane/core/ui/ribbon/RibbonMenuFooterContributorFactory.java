package org.freeplane.core.ui.ribbon;

import java.awt.event.ActionListener;
import java.util.Properties;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.Compat;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;

public class RibbonMenuFooterContributorFactory implements IRibbonContributorFactory {
	final private RibbonBuilder builder;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public RibbonMenuFooterContributorFactory(RibbonBuilder builder) {
		this.builder = builder;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public ARibbonContributor getContributor(final Properties attributes) {
		String accel = attributes.getProperty("accelerator", null);
		final String actionKey = attributes.getProperty("action");		
		
		if (actionKey != null) {
    		if (accel != null) {
    			if (Compat.isMacOsX()) {
    				accel = accel.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
    			}			
    			builder.getAcceleratorManager().setDefaultAccelerator(actionKey, accel);
    		}
		}
		
		return new ARibbonContributor() {
			public String getKey() {
				return attributes.getProperty("action");
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				final String key = attributes.getProperty("action");
				if(key != null) {
					AFreeplaneAction action = context.getBuilder().getMode().getAction(key);
					if(action != null) {
						String title = ActionUtils.getActionTitle(action);
						ResizableIcon icon = ActionUtils.getActionIcon(action);
						ActionListener listener = new RibbonActionContributorFactory.RibbonActionListener(action);
						final RibbonApplicationMenuEntryFooter entry = new RibbonApplicationMenuEntryFooter(icon, title, listener);
						parent.addChild(entry, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
					}
				}
			}

			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}
}
