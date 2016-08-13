package org.freeplane.core.util;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.MindIconFactory;
import org.freeplane.features.icon.mindmapmode.MIconController;

/** utility methods to access Freeplane's (builtin and user) icons. */
public class FreeplaneIconUtils {

	public static Icon createStandardIcon(String iconKey) {
        return MindIconFactory.create(iconKey).getIcon();
    }

	/** lists all icons that are available in the icon selection dialog. This may include user icons
	 * if there are some installed. */
	public static List<String> listStandardIconKeys() {
		// the source of this list is the property "icons.list" in freeplane.properties
		ArrayList<String> result = new ArrayList<String>();
		final MIconController mIconController = (MIconController) IconController.getController();
		for (MindIcon mindIcon : mIconController.getMindIcons())
			result.add(mindIcon.getName());
		return result;
	}

	public static ImageIcon createImageIcon(final String resourcePath) {
		final URL resourceUrl = ResourceController.getResourceController().getResource(resourcePath);
		return createImageIcon(resourceUrl);
	}
	
	public static ImageIcon createImageIconByResourceKey(final String resourceKey) {
		final ResourceController resourceController = ResourceController.getResourceController();
		final URL resourceUrl = resourceController.getResource(resourceController.getProperty(resourceKey));
		return createImageIcon(resourceUrl);
	}

	public static ImageIcon createImageIconPrivileged(final URL resourceUrl) {
		return AccessController.doPrivileged(new PrivilegedAction<ImageIcon>() {
			@Override
			public ImageIcon run() {
				return  createImageIcon(resourceUrl);
			}
		});
	}

	private static ImageIcon createImageIcon(final URL resourceUrl) {
		return new ImageIcon(resourceUrl);
	}
}
