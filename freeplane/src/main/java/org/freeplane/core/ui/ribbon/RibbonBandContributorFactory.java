package org.freeplane.core.ui.ribbon;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

public class RibbonBandContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			private JRibbonBand band;
			private boolean valid = false;

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}
				band = new JRibbonBand(TextUtils.removeTranslateComment(TextUtils.getText("ribbon.band." + attributes.getProperty("name"))), null);
				// read policies and sub-contributions
				context.processChildren(context.getCurrentPath(), this);
				setResizePolicies(attributes.getProperty("resize_policies"));
				band.setFocusable(false);
				if (valid) {
					parent.addChild(band, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
				}

			}

			public void addChild(Object child, ChildProperties properties) {
				if (child instanceof AbstractCommandButton) {
					RibbonElementPriority priority = properties.get(RibbonElementPriority.class);
					if (priority == null) {
						priority = RibbonElementPriority.TOP;
					}
					band.addCommandButton((AbstractCommandButton) child, priority);
					valid = true;
				}

			}

			private void setResizePolicies(String policiesString) {
				if (policiesString != null) {
					String[] tokens = policiesString.split(",");
					List<RibbonBandResizePolicy> policyList = new ArrayList<RibbonBandResizePolicy>();
					for (String policyStr : tokens) {
						if ("none".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.None(band.getControlPanel()));
						} else if ("mirror".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Mirror(band.getControlPanel()));
						} else if ("high2low".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.High2Low(band.getControlPanel()));
						} else if ("high2mid".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.High2Mid(band.getControlPanel()));
						} else if ("mid2low".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Mid2Low(band.getControlPanel()));
						} else if ("mid2mid".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Mid2Mid(band.getControlPanel()));
						} else if ("low2mid".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Low2Mid(band.getControlPanel()));
						}
					}
					policyList.add(new IconRibbonBandResizePolicy(band.getControlPanel()));
					band.setResizePolicies(policyList);
					try {
						FlamingoUtilities.checkResizePoliciesConsistency(band);
					}
					catch (Exception ignore) {
						reorganizePolicies(band, true);
					}
				}

			}
		};
	}
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	@SuppressWarnings("unchecked")
	protected void reorganizePolicies(JRibbonBand band, boolean tolerateExceptions) {
		Insets ins = band.getInsets();
		AbstractBandControlPanel controlPanel = band.getControlPanel();
		if (controlPanel == null)
			return;
		int height = controlPanel.getPreferredSize().height
				+ band.getUI().getBandTitleHeight() + ins.top
				+ ins.bottom;
		List<RibbonBandResizePolicy> resizePolicies = band.getResizePolicies();
		FlamingoUtilities.checkResizePoliciesConsistencyBase(band);
		int index = -1;
		while((index = checkPolicies(band, height, resizePolicies)) > -1) {
			if(tolerateExceptions) {
				band.setResizePolicies(buildNewList(resizePolicies, index));
				break;
			}
			else {
				throw new IllegalStateException(getExceptionMessage(band, height, resizePolicies, index));
			}
			
		}
	}
	
	@SuppressWarnings("rawtypes")
	private List buildNewList(List<RibbonBandResizePolicy> resizePolicies, int index) {
		ArrayList<RibbonBandResizePolicy> newList = new ArrayList<RibbonBandResizePolicy>();
		for (int i = 0; i < resizePolicies.size(); i++) {
			if(i == index) {
				continue;
			}
			newList.add(resizePolicies.get(i));
		}
		return newList;
	}
	
	@SuppressWarnings("rawtypes")
	private int checkPolicies(AbstractRibbonBand ribbonBand, int height, List<RibbonBandResizePolicy> resizePolicies) {
		for (int i = 0; i < (resizePolicies.size() - 1); i++) {
			RibbonBandResizePolicy policy1 = resizePolicies.get(i);
			RibbonBandResizePolicy policy2 = resizePolicies.get(i + 1);
			int width1 = policy1.getPreferredWidth(height, 4);
			int width2 = policy2.getPreferredWidth(height, 4);
			if (width1 < width2) {
				return i+1;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("rawtypes")
	private String getExceptionMessage(AbstractRibbonBand ribbonBand, int height, List<RibbonBandResizePolicy> resizePolicies,
			int errorIndex) {
		RibbonBandResizePolicy policy1 = resizePolicies.get(errorIndex-1);
		RibbonBandResizePolicy policy2 = resizePolicies.get(errorIndex);
		int width1 = policy1.getPreferredWidth(height, 4);
		// create the trace message
		StringBuilder builder = new StringBuilder();
		builder.append("Inconsistent preferred widths\n");
		builder.append("Ribbon band '" + ribbonBand.getTitle()
				+ "' has the following resize policies\n");
		for (int j = 0; j < resizePolicies.size(); j++) {
			RibbonBandResizePolicy policy = resizePolicies.get(j);
			int width = policy.getPreferredWidth(height, 4);
			builder.append("\t" + policy.getClass().getName()
					+ " with preferred width " + width + "\n");
		}
		builder.append(policy1.getClass().getName()
				+ " with pref width " + width1
				+ " is followed by resize policy "
				+ policy2.getClass().getName()
				+ " with larger pref width\n");

		return builder.toString();
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
