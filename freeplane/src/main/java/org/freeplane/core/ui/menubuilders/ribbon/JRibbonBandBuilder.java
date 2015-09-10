package org.freeplane.core.ui.menubuilders.ribbon;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.menubuilders.menu.ComponentProvider;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.MutableRibbonTask;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;
import org.pushingpixels.flamingo.internal.ui.ribbon.AbstractBandControlPanel;

public class JRibbonBandBuilder implements EntryVisitor {

	private EntryAccessor accessor;
	private ComponentProvider provider;
	
	public JRibbonBandBuilder(ResourceAccessor resourceAccessor) {
		this(resourceAccessor, new RibbonBandComponentProvider(resourceAccessor));
	}
	
	public JRibbonBandBuilder(ResourceAccessor resourceAccessor, ComponentProvider componentProvider) {
		accessor = new EntryAccessor(resourceAccessor);
		this.provider = componentProvider;
	}

	@Override
	public void visit(Entry target) {
		Object attr = target.getAttribute("");
		RibbonBandContainer container = new RibbonBandContainer((JRibbonBand)provider.createComponent(target), attr == null ? null : String.valueOf(attr));
		accessor.setComponent(target, container);
		((MutableRibbonTask) accessor.getAncestorComponent(target)).addBand(container.getBand());
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}
}

class RibbonBandContainer extends JRibbonContainer {
	final private JRibbonBand band;
	final private String policies;

	public RibbonBandContainer(JRibbonBand band, String policies) {
		if(band == null) throw new IllegalArgumentException("band is NULL!");
		this.band = band;
		this.policies = policies;
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		AbstractCommandButton button = (AbstractCommandButton) comp;
		RibbonElementPriority priority = (RibbonElementPriority)button.getClientProperty(RibbonActionComponentProvider.PRIORITY_PROPERTY);
		band.addCommandButton(button, priority != null ? priority : RibbonElementPriority.MEDIUM);
		updateResizePolicies(band, policies);
	}

	public JRibbonBand getBand() {
		return band;
	}

	@Override
	public Component getParent() {
		return band;
	}
	
	public void updateResizePolicies(JRibbonBand band, String policiesAttr) {		
		List<RibbonBandResizePolicy> policyList = new ArrayList<RibbonBandResizePolicy>();
		if (policiesAttr != null && !"".equals(String.valueOf(policiesAttr))) {
			String[] tokens = String.valueOf(policiesAttr).split(",");
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
		} else {
			policyList = CoreRibbonResizePolicies.getCorePoliciesRestrictive(band);
		}		
		
		if(checkPolicies(band, policyList) > -1) {
			policyList = sortList(policyList, getBandHeight(band));
		}
		
		band.setResizePolicies(policyList);
	}
		
	private List<RibbonBandResizePolicy> sortList(List<RibbonBandResizePolicy> resizePolicies, int height) {
		ArrayList<RibbonBandResizePolicy> newList = new ArrayList<RibbonBandResizePolicy>();
		int size = resizePolicies.size();
		int [] widths = new int[size];
		int [] index = new int[size];
		RibbonBandResizePolicy policy;
		for (int i = 0; i < resizePolicies.size(); i++) {
			policy = resizePolicies.get(i);
			widths[i] = policy.getPreferredWidth(height, 4);
			index[i] = i;
		}
		int max = (resizePolicies.get(size-1) instanceof IconRibbonBandResizePolicy) ? size - 1 : size;
		for (int i = 0; i < (max-1); i++) {
			
			for(int j = i; j < max; j++ ) {
				if(widths[index[i]] < widths[index[j]]) {
					int temp = index[i];
					index[i] = index[j];
					index[j] = temp;
				}
			}
		}
		for (int i = 0; i < size; i++) {
			if(widths[index[i]] > 0 && widths[index[i]] >= widths[index[size-1]]) 
			{
				policy = resizePolicies.get(index[i]);
				newList.add(policy);
			}
		}
		
		return newList;
	}
	
	@SuppressWarnings("rawtypes")
	private int checkPolicies(AbstractRibbonBand ribbonBand, List<RibbonBandResizePolicy> resizePolicies) {
		int height = getBandHeight(ribbonBand);
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
	private int getBandHeight(AbstractRibbonBand ribbonBand) {
		Insets ins = ribbonBand.getInsets();
		AbstractBandControlPanel controlPanel = ribbonBand.getControlPanel();
		if (controlPanel == null)
			return 0;
		int height = controlPanel.getPreferredSize().height
				+ ribbonBand.getUI().getBandTitleHeight() + ins.top
				+ ins.bottom;
		return height;
	}
}
