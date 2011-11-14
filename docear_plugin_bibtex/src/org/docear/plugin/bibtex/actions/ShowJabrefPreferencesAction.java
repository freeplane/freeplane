package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;

import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;

public class ShowJabrefPreferencesAction extends AFreeplaneAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ShowJabrefPreferencesAction(String key) {
		super(key);
//		final String iconResource = ResourceController.getResourceController().getProperty(, null);
//		if (iconResource != null) {
//			final URL url = ResourceController.getResourceController().getResource(iconResource);
//			if (url == null) {
//				LogUtils.severe("can not load icon '" + iconResource + "'");
//			}
//			else {
//				final ImageIcon icon = new ImageIcon(url);
//				putValue(SMALL_ICON, icon);
//			}
//		}
	}
	
	

	public void actionPerformed(ActionEvent e) {
		ReferencesController.getController().getJabrefWrapper().getJabrefFrame().preferences();
	}
	
}
