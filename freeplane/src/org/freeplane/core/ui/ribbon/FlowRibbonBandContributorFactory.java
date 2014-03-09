package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;

import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.ribbon.JFlowRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class FlowRibbonBandContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			private JFlowRibbonBand band;

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}
				band = new JFlowRibbonBand(TextUtils.removeTranslateComment(TextUtils.getText("ribbon.band." + attributes.getProperty("name"))), null);
				RibbonBandResizePolicy policy = band.getCurrentResizePolicy();
				band.setCurrentResizePolicy(policy);
				// read policies and sub-contributions
				context.processChildren(context.getCurrentPath(), this);
				
				parent.addChild(band, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
				
				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();				
				policies.add(new CoreRibbonResizePolicies.FlowThreeRows(band.getControlPanel()));
				policies.add(new IconRibbonBandResizePolicy(band.getControlPanel()));
				band.setResizePolicies(policies);
			}

			public void addChild(Object child, ChildProperties properties) {
				if (child instanceof JComponent) {					
					band.addFlowComponent((JComponent) child);
				}
			}

		};
	}
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
