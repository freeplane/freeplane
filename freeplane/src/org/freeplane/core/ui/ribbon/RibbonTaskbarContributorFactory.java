package org.freeplane.core.ui.ribbon;

import java.awt.Component;
import java.util.Properties;

import javax.swing.JSeparator;

import org.freeplane.core.ui.ribbon.RibbonSeparatorContributorFactory.RibbonSeparator;

public class RibbonTaskbarContributorFactory implements IRibbonContributorFactory {

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			private ARibbonContributor delegator = null;
			
			public String getKey() {
				return "taskbar";
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				delegator = parent;
				context.processChildren(context.getCurrentPath(), this);
			}
			
			public void addChild(Object child, ChildProperties properties) {
				if(child instanceof RibbonSeparator) {
					if(delegator != null) {
						delegator.addChild(new RibbonTaskBarComponent(new JSeparator(JSeparator.VERTICAL)), properties);
					}
				}
				if(child instanceof Component) {
					if(delegator != null) {
						delegator.addChild(new RibbonTaskBarComponent((Component) child), properties);
					}
				}
				
			}
		};
	}
}
