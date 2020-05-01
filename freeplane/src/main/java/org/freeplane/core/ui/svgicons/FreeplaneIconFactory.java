package org.freeplane.core.ui.svgicons;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;

/** utility methods to access Freeplane's (builtin and user) icons. */
public class FreeplaneIconFactory {
	private static final String ANTIALIAS_SVG = "antialias_svg";

	public static Icon createIcon(final String resourcePath) {
		final URL resourceUrl = ResourceController.getResourceController().getResource(resourcePath);
		return createIcon(resourceUrl);
	}

	public static Icon createIconPrivileged(final URL resourceUrl) {
		return AccessController.doPrivileged(new PrivilegedAction<Icon>() {
			@Override
			public Icon run() {
				return createIcon(resourceUrl);
			}
		});
	}

	private static Icon createIcon(final URL resourceUrl) {
		return new ImageIcon(resourceUrl);
	}

	static boolean isSvgAntialiasEnabled() {
		return ResourceController.getResourceController().getBooleanProperty(ANTIALIAS_SVG);
	}

	public static Icon createSVGIcon(final URL url, final int heightPixels) {
		return AccessController.doPrivileged(new PrivilegedAction<Icon>() {
			@Override
			public Icon run() {
				return new SVGIconCreator(url).setHeight(heightPixels).createIcon();
			}
		});
	}

	public static Icon createSVGIconHavingWidth(final URL url, final int widthPixels) {
		return AccessController.doPrivileged(new PrivilegedAction<Icon>() {
			@Override
			public Icon run() {
				return new SVGIconCreator(url).setWidth(widthPixels).createIcon();
			}
		});
	}

	public static Icon createSVGIcon(final URL url) {
		return AccessController.doPrivileged(new PrivilegedAction<Icon>() {
			@Override
			public Icon run() {
				return new SVGIconCreator(url).createIcon();
			}
		});
	}

	public static ImageIcon toImageIcon(Icon icon) {
		if(icon == null)
			return null;
		else if(icon instanceof ImageIcon)
			return (ImageIcon) icon;
		else if(icon instanceof CachingIcon)
			return ((CachingIcon)icon).getImageIcon();
		else {
	      int width = icon.getIconWidth();
	      int height = icon.getIconHeight();
	      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	      Graphics g = image.getGraphics();
	      icon.paintIcon(null, g, 0, 0);
	      return new ImageIcon(image);
		}
	}
}
