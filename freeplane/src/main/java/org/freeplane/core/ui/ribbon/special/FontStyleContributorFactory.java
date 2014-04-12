package org.freeplane.core.ui.ribbon.special;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.event.TreeSelectionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.CurrentState;
import org.freeplane.core.ui.ribbon.IChangeObserver;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory.ActionChangeListener;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.ui.ribbon.RibbonBuilder;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.ribbon.JFlowRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonComponent;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class FontStyleContributorFactory implements IRibbonContributorFactory {
	
	private void setAccelerator(RibbonBuilder builder, String accel, String actionKey) {
    	if (accel != null) {
    		if (Compat.isMacOsX()) {
    			accel = accel.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
    		}
    		builder.getAcceleratorManager().setDefaultAccelerator(actionKey, accel);
    	}
	}

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				setAccelerator(context.getBuilder(), "control B", "BoldAction");
				setAccelerator(context.getBuilder(), "control I", "ItalicAction");
				setAccelerator(context.getBuilder(), "control PLUS", "IncreaseNodeFontAction");
				setAccelerator(context.getBuilder(), "control MINUS", "DecreaseNodeFontAction");
				setAccelerator(context.getBuilder(), "alt shift F", "NodeColorAction");
				setAccelerator(context.getBuilder(), "alt shift P", "UsePlainTextAction");
				
				if (parent == null) {
					return;
				}

				// RIBBONS expandlistener and icon
				JFlowRibbonBand band = new JFlowRibbonBand(TextUtils.removeTranslateComment(TextUtils.getText("ribbon.band.font")), null, null);
				band.setExpandButtonKeyTip("FN");
				band.setCollapsedStateKeyTip("ZF");

				MUIFactory uiFactory = Controller.getCurrentModeController().getExtension(MUIFactory.class);

				final Container fontBox = uiFactory.createFontBox();
				JRibbonComponent fontComboWrapper = new JRibbonComponent((JComponent) fontBox);
				fontComboWrapper.setKeyTip("SF");
				addDefaultToggleHandler(context, fontComboWrapper);
				band.addFlowComponent(fontComboWrapper);

				final Container sizeBox = uiFactory.createSizeBox();
				JRibbonComponent sizeComboWrapper = new JRibbonComponent((JComponent) sizeBox);
				sizeComboWrapper.setKeyTip("SS");
				addDefaultToggleHandler(context, sizeComboWrapper);
				band.addFlowComponent(sizeComboWrapper);

				final Container styleBox = uiFactory.createStyleBox();
				JRibbonComponent styleComboWrapper = new JRibbonComponent((JComponent) styleBox);
				styleComboWrapper.setKeyTip("SD");
				addDefaultToggleHandler(context, styleComboWrapper);
				band.addFlowComponent(styleComboWrapper);

				JCommandButtonStrip styleStrip = new JCommandButtonStrip();

				AFreeplaneAction action = context.getBuilder().getMode().getAction("BoldAction");
				final JCommandToggleButton boldButton = RibbonActionContributorFactory.createCommandToggleButton(action);
				addDefaultToggleHandler(context, action, boldButton);				
				styleStrip.add(boldButton);

				action = context.getBuilder().getMode().getAction("ItalicAction");
				final JCommandToggleButton italicButton = RibbonActionContributorFactory.createCommandToggleButton(action);
				addDefaultToggleHandler(context, action, italicButton);
				styleStrip.add(italicButton);
				
				action = context.getBuilder().getMode().getAction("NodeColorAction");
				JCommandButton button = RibbonActionContributorFactory.createCommandButton(action);
				addDefaultToggleHandler(context, action, button);
				styleStrip.add(button);
				
				action = context.getBuilder().getMode().getAction("NodeBackgroundColorAction");
				button = RibbonActionContributorFactory.createCommandButton(action);
				addDefaultToggleHandler(context, action, button);
				styleStrip.add(button);
				
				action = context.getBuilder().getMode().getAction("NodeColorBlendAction");
				button = RibbonActionContributorFactory.createCommandButton(action);
				addDefaultToggleHandler(context, action, button);
				styleStrip.add(button);
				
				action = context.getBuilder().getMode().getAction("BlinkingNodeHookAction");
				button = RibbonActionContributorFactory.createCommandButton(action);
				addDefaultToggleHandler(context, action, button);
				styleStrip.add(button);
				
				action = context.getBuilder().getMode().getAction("MapBackgroundColorAction");
				button = RibbonActionContributorFactory.createCommandButton(action);
				addDefaultToggleHandler(context, action, button);
				styleStrip.add(button);
								
				band.addFlowComponent(styleStrip);
				
				action = context.getBuilder().getMode().getAction("RemoveFormatAction");				
				button = RibbonActionContributorFactory.createCommandButton(action);
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);				
				addDefaultToggleHandler(context, action, button);
				band.addFlowComponent(button);
				
				action = context.getBuilder().getMode().getAction("UsePlainTextAction");
				button = RibbonActionContributorFactory.createCommandButton(action);
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);
				addDefaultToggleHandler(context, action, button);
				band.addFlowComponent(button);
				
				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();				
				policies.add(new CoreRibbonResizePolicies.FlowThreeRows(band.getControlPanel()));
				policies.add(new IconRibbonBandResizePolicy(band.getControlPanel()));
				band.setResizePolicies(policies);	

				parent.addChild(band, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));

			}

			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}
	
	private void addDefaultToggleHandler(final RibbonBuildContext context, final Component component) {
		context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
			public void updateState(CurrentState state) {
				if(state.isNodeChangeEvent()) {					
				}
				else if(state.allMapsClosed()) {					
					component.setEnabled(false);
				}
				else if (state.get(TreeSelectionEvent.class) == null) {
					component.setEnabled(true);
				}
			}
		});
	}

	private void addDefaultToggleHandler(final RibbonBuildContext context, final AFreeplaneAction action, final AbstractCommandButton button) {
		context.getBuilder().getMapChangeAdapter().addListener(new ActionChangeListener(action, button));
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
