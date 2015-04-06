package org.freeplane.core.ui;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.pushingpixels.flamingo.api.common.AsynchronousLoadListener;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;

class RibbonHelper {
	static void setRibbonIcon(final JMenuItem item) {
		ImageIcon ico = (ImageIcon) item.getIcon();
		ImageWrapperResizableIcon newIco = ImageWrapperResizableIcon.getIcon(ico.getImage(),
		    new Dimension(ico.getIconWidth(), ico.getIconHeight()));
		newIco.setPreferredSize(new Dimension(16, 16));
		newIco.addAsynchronousLoadListener(new AsynchronousLoadListener() {
			public void completed(boolean success) {
				item.repaint();
			}
		});
		item.setIcon(newIco);
	}
}