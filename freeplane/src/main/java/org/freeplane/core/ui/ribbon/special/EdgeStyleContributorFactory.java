package org.freeplane.core.ui.ribbon.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ButtonGroup;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.CurrentState;
import org.freeplane.core.ui.ribbon.IChangeObserver;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class EdgeStyleContributorFactory implements IRibbonContributorFactory {
	public ARibbonContributor getContributor(final Properties attributes) {		
		return new ARibbonContributor() {			

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}

				JRibbonBand band = new JRibbonBand(TextUtils.removeTranslateComment(TextUtils.getText("ribbon.band.edgeStyles")), null, null);
				band.setExpandButtonKeyTip("ES");
				band.setCollapsedStateKeyTip("ZE");

				JCommandButton styleGroupButton = new JCommandButton(TextUtils.removeTranslateComment(TextUtils.getText("edgeStyleGroupAction.text")));				
				styleGroupButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				AFreeplaneAction action = context.getBuilder().getMode().getAction("EdgeStyleAsParentAction");
				final JCommandToggleMenuButton styleAsParent = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, styleAsParent);
				action = context.getBuilder().getMode().getAction("EdgeStyleAction.linear");
				final JCommandToggleMenuButton styleLinear = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, styleLinear);
				action = context.getBuilder().getMode().getAction("EdgeStyleAction.bezier");
				final JCommandToggleMenuButton styleBezier = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, styleBezier);
				action = context.getBuilder().getMode().getAction("EdgeStyleAction.sharp_linear");
				final JCommandToggleMenuButton styleSharpLinear = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, styleSharpLinear);
				action = context.getBuilder().getMode().getAction("EdgeStyleAction.sharp_bezier");
				final JCommandToggleMenuButton styleSharpBezier = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, styleSharpBezier);
				action = context.getBuilder().getMode().getAction("EdgeStyleAction.horizontal");
				final JCommandToggleMenuButton styleHorizontal = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, styleHorizontal);
				action = context.getBuilder().getMode().getAction("EdgeStyleAction.hide_edge");
				final JCommandToggleMenuButton styleHideEdge = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, styleHideEdge);

				ButtonGroup group = new ButtonGroup();
				styleAsParent.getActionModel().setGroup(group);
				styleLinear.getActionModel().setGroup(group);
				styleAsParent.getActionModel().setGroup(group);
				styleBezier.getActionModel().setGroup(group);
				styleAsParent.getActionModel().setGroup(group);
				styleSharpLinear.getActionModel().setGroup(group);
				styleAsParent.getActionModel().setGroup(group);
				styleSharpBezier.getActionModel().setGroup(group);
				styleAsParent.getActionModel().setGroup(group);
				styleHorizontal.getActionModel().setGroup(group);
				styleAsParent.getActionModel().setGroup(group);
				styleHideEdge.getActionModel().setGroup(group);
				styleAsParent.getActionModel().setGroup(group);

				styleGroupButton.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						popupmenu.addMenuButton(styleAsParent);
						popupmenu.addMenuButton(styleLinear);
						popupmenu.addMenuButton(styleBezier);
						popupmenu.addMenuButton(styleSharpLinear);
						popupmenu.addMenuButton(styleSharpBezier);
						popupmenu.addMenuButton(styleHorizontal);
						popupmenu.addMenuButton(styleHideEdge);
						return popupmenu;
					}
				});

				band.addCommandButton(styleGroupButton, RibbonElementPriority.MEDIUM);

				JCommandButton lineWidthGroupButton = new JCommandButton(TextUtils.removeTranslateComment(TextUtils.getText("edgeLineWidthGroupAction.text")));
				lineWidthGroupButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);				
				action = context.getBuilder().getMode().getAction("EdgeWidthAction_width_parent");
				final JCommandToggleMenuButton widthParent = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, widthParent);
				action = context.getBuilder().getMode().getAction("EdgeWidthAction_width_thin");
				final JCommandToggleMenuButton widthThin = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, widthThin);
				action = context.getBuilder().getMode().getAction("EdgeWidthAction_1");
				final JCommandToggleMenuButton width1 = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, width1);
				action = context.getBuilder().getMode().getAction("EdgeWidthAction_2");
				final JCommandToggleMenuButton width2 = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, width2);
				action = context.getBuilder().getMode().getAction("EdgeWidthAction_4");
				final JCommandToggleMenuButton width4 = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, width4);
				action = context.getBuilder().getMode().getAction("EdgeWidthAction_8");
				final JCommandToggleMenuButton width8 = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
				addDefaultToggleHandler(context, action, width8);

				lineWidthGroupButton.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						popupmenu.addMenuButton(widthParent);
						popupmenu.addMenuButton(widthThin);
						popupmenu.addMenuButton(width1);
						popupmenu.addMenuButton(width2);
						popupmenu.addMenuButton(width4);
						popupmenu.addMenuButton(width8);
						return popupmenu;
					}
				});

				band.addCommandButton(lineWidthGroupButton, RibbonElementPriority.MEDIUM);

				action = context.getBuilder().getMode().getAction("EdgeColorAction");
				final JCommandButton edgeColorButton = RibbonActionContributorFactory.createCommandButton(action);
				band.addCommandButton(edgeColorButton, RibbonElementPriority.MEDIUM);

				action = context.getBuilder().getMode().getAction("AutomaticEdgeColorHookAction");
				//RIBBONS
//				KeyStroke ks =RibbonAcceleratorManager.parseKeyStroke("j2ef2");
//				context.getBuilder().getAcceleratorManager().setAccelerator(action, ks);
//				RibbonActionContributorFactory.updateRichTooltip(button, action, ks);
				final JCommandButton automaticColorButton = RibbonActionContributorFactory.createCommandButton(action);
				band.addCommandButton(automaticColorButton, RibbonElementPriority.MEDIUM);

				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();
				policies.add(new CoreRibbonResizePolicies.Mirror(band.getControlPanel()));
				policies.add(new CoreRibbonResizePolicies.High2Mid(band.getControlPanel()));
				band.setResizePolicies(policies);
				parent.addChild(band, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));

			}

			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}

	private void addDefaultToggleHandler(final RibbonBuildContext context, final AFreeplaneAction action, final JCommandToggleButton button) {
		context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
			public void updateState(CurrentState state) {
				if (AFreeplaneAction.checkSelectionOnChange(action)) {
					action.setSelected();
					button.getActionModel().setSelected(action.isSelected());
				}
			}
		});
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
