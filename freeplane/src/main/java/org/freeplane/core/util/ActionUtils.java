package org.freeplane.core.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

public abstract class ActionUtils {

	public static ResizableIcon getActionIcon(final AFreeplaneAction action) {
		ResizableIcon icon = null;
		ImageIcon ico = (ImageIcon) action.getValue(Action.SMALL_ICON);
		if(ico != null) {
			icon = ImageWrapperResizableIcon.getIcon(ico.getImage(), new Dimension(ico.getIconWidth(), ico.getIconHeight()));
		}
		else {
			String resource = ResourceController.getResourceController().getProperty(action.getIconKey(), null);
			if (resource != null) {
				URL location = ResourceController.getResourceController().getResource(resource);
				icon = ImageWrapperResizableIcon.getIcon(location, new Dimension(16, 16));
			}
		}
		if(icon == null) {
			icon = RibbonActionContributorFactory.BLANK_ACTION_ICON;
		}
		return icon;
	}

	public static String getActionTitle(final AFreeplaneAction action) {
		String title = (String)action.getValue(Action.NAME);
		
		if(title == null || title.isEmpty()) {
			title = TextUtils.getText(action.getTextKey());
		}
		if(title == null || title.isEmpty()) {
			title = action.getTextKey();
		}
		return TextUtils.removeTranslateComment(title);
	}

	public static AFreeplaneAction getDummyAction(final String key) {
		return new AFreeplaneAction(key) {
			private static final long serialVersionUID = -5405032373977903024L;
	
			public String getTextKey() {
				return getKey();
			}
			
			public void actionPerformed(ActionEvent e) {
				//do nothing
			}
		};
	}
	
	

}
