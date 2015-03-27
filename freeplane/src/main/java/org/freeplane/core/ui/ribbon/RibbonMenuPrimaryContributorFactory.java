package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.ActionUtils;
import org.freeplane.core.util.Compat;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class RibbonMenuPrimaryContributorFactory implements IRibbonContributorFactory {
	final private RibbonBuilder builder;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public RibbonMenuPrimaryContributorFactory(RibbonBuilder builder) {
		this.builder = builder;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static RibbonApplicationMenuEntryPrimary createMenuEntry(final AFreeplaneAction action, CommandButtonKind kind) {
		String title = ActionUtils.getActionTitle(action);
		ResizableIcon icon = ActionUtils.getActionIcon(action);

		RibbonApplicationMenuEntryPrimary entry = new RibbonApplicationMenuEntryPrimary(icon, title, new RibbonActionContributorFactory.RibbonActionListener(action), kind);
		return entry;
	}
	
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
			RibbonApplicationMenuEntryPrimary entry;			
			
			public String getKey() {
				String key = attributes.getProperty("action", null);
				if(key == null) {
					key = attributes.getProperty("name", null);
				}
				return key;
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				entry = null;
				if(context.hasChildren(context.getCurrentPath())) {					
					if(attributes.get("action") == null) {
						AFreeplaneAction action = ActionUtils.getDummyAction(getKey());
						entry = createMenuEntry(action, CommandButtonKind.POPUP_ONLY);
					}
					else {
						AFreeplaneAction action = context.getBuilder().getMode().getAction(getKey());
						if(action == null) {
							action = ActionUtils.getDummyAction(getKey());
						}
						entry = createMenuEntry(action, CommandButtonKind.ACTION_AND_POPUP_MAIN_ACTION);
					}
					
					context.processChildren(context.getCurrentPath(), this);
				}
				else {
					if(attributes.get("action") == null) {
						return;
					}
					AFreeplaneAction action = context.getBuilder().getMode().getAction(getKey());
					if(action == null) {
						return;
					}
					
					entry = createMenuEntry(action, CommandButtonKind.ACTION_ONLY);
				}
				KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(getKey());
				if(ks != null) {
					AFreeplaneAction action = context.getBuilder().getMode().getAction(getKey());
					if(action != null) {
						RichTooltip tip = RibbonActionContributorFactory.getRichTooltip(action, ks);
						if(tip != null) {
							entry.setActionRichTooltip(tip);
						}
					}
				}
				parent.addChild(entry, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
			}

			public void addChild(Object child, ChildProperties properties) {
				if(child instanceof SecondaryEntryGroup) {
					SecondaryEntryGroup group = (SecondaryEntryGroup) child;
					entry.addSecondaryMenuGroup(group.getTitle(), group.getEntries().toArray(new RibbonApplicationMenuEntrySecondary[0]));
				}
			}
		};
	}
	
	/***********************************************************************************
	 * NESTED TYPE DECLARATIONS
	 **********************************************************************************/
	
	public static class SecondaryEntryGroup {
		private final String groupTitle;
		private List<RibbonApplicationMenuEntrySecondary> entries = new ArrayList<RibbonApplicationMenuEntrySecondary>();
		
		public SecondaryEntryGroup(String title) {
			this.groupTitle = title;
		}
		
		public void addEntry(RibbonApplicationMenuEntrySecondary entry) {
			entries.add(entry);
		}
		
		public List<RibbonApplicationMenuEntrySecondary> getEntries() {
			return Collections.unmodifiableList(entries);
		}
		
		public String getTitle() {
			return groupTitle;
		}
	}
}
