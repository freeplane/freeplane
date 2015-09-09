package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

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
		setResizePolicies(band, String.valueOf(entry.getAttribute(RESIZE_POLICIES_ATTRIBUTE)));
		return band;
	}
	
	private void setResizePolicies(JRibbonBand band, String policiesString) {
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

}
