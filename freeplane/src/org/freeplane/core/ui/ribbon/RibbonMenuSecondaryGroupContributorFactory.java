package org.freeplane.core.ui.ribbon;

import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory.RibbonActionListener;
import org.freeplane.core.ui.ribbon.RibbonMenuPrimaryContributorFactory.SecondaryEntryGroup;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;

public class RibbonMenuSecondaryGroupContributorFactory implements IRibbonContributorFactory {

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
			SecondaryEntryGroup group;
			public String getKey() {
				return attributes.getProperty("name", null);
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				group = new SecondaryEntryGroup(TextUtils.removeTranslateComment(TextUtils.getRawText("ribbon.menu.group."+getKey())));
				context.processChildren(context.getCurrentPath(), this);
				parent.addChild(group, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
			}
			
			public void addChild(Object child, ChildProperties properties) {
				if(child instanceof RibbonApplicationMenuEntrySecondary) {
					group.addEntry((RibbonApplicationMenuEntrySecondary) child);
				}
				else if(child instanceof AbstractCommandButton) {
					group.addEntry(wrapButton((AbstractCommandButton) child));
				}
			}

			private RibbonApplicationMenuEntrySecondary wrapButton(AbstractCommandButton button) {
				ActionListener listener = null;
				PopupPanelCallback callback = null;
				CommandButtonKind kind = CommandButtonKind.ACTION_ONLY;
				if(button instanceof JCommandButton) {
					if(((JCommandButton) button).getPopupCallback() != null) {
						kind = (((JCommandButton) button).getCommandButtonKind());
						callback = ((JCommandButton) button).getPopupCallback();
					}
				}
				for (ActionListener l : button.getListeners(ActionListener.class)) {
					if(l instanceof RibbonActionListener) {
						listener = l;
						break;
					}
				}
				RibbonApplicationMenuEntrySecondary entry = new RibbonApplicationMenuEntrySecondary(button.getIcon(), button.getText(), listener, kind);
				if(callback != null) {
					entry.setPopupCallback(callback);
				}
				KeyStroke ks = (KeyStroke) button.getClientProperty(RibbonActionContributorFactory.ACTION_ACCELERATOR);	
				if(ks != null) {
					AFreeplaneAction action = (AFreeplaneAction) button.getClientProperty(RibbonActionContributorFactory.ACTION);
					if(action != null) {
						RichTooltip tip = RibbonActionContributorFactory.getRichTooltip(action, ks);
						if(tip != null) {
							entry.setActionRichTooltip(tip);
						}
					}
				}
				return entry;
			}
		};
	}
	
}
