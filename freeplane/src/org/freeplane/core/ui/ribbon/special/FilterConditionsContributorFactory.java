package org.freeplane.core.ui.ribbon.special;

import java.util.Properties;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandToggleMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class FilterConditionsContributorFactory implements IRibbonContributorFactory {	

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}
		

			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}				
				
				JCommandButton button = new JCommandButton(TextUtils.removeTranslateComment(TextUtils.getText("FilterCondition")));
				button.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				button.setPopupCallback(new PopupPanelCallback() {
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
    					
    					AFreeplaneAction action = context.getBuilder().getMode().getAction("ApplyToVisibleAction");
    					JCommandToggleMenuButton toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
    					action.setSelected();
    					toggleButton.getActionModel().setSelected(action.isSelected());
    					popupmenu.addMenuButton(toggleButton);
    					
    					action = context.getBuilder().getMode().getAction("ShowAncestorsAction");
    					toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
    					action.setSelected();
    					toggleButton.getActionModel().setSelected(action.isSelected());
    					popupmenu.addMenuButton(toggleButton);
    					
    					action = context.getBuilder().getMode().getAction("ShowDescendantsAction");
    					toggleButton = RibbonActionContributorFactory.createCommandToggleMenuButton(action);
    					action.setSelected();
    					toggleButton.getActionModel().setSelected(action.isSelected());
    					popupmenu.addMenuButton(toggleButton);    					
    					
						return popupmenu;
					}
				});
				ChildProperties props = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
				props.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
				parent.addChild(button, props);
			}
					
			public void addChild(Object child, ChildProperties properties) {
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
