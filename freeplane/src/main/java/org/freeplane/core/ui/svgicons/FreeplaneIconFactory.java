package org.freeplane.core.ui.svgicons;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;

/** utility methods to access Freeplane's (builtin and user) icons. */
public class FreeplaneIconFactory {
	private static final String ANTIALIAS_SVG = "antialias_svg";
	private static SVGUniverse svgUniverse;

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

	private static boolean isSvgAntialiasEnabled() {
		return ResourceController.getResourceController().getBooleanProperty(ANTIALIAS_SVG);
	}

	public static Icon createSVGIcon(final URL url, final int heightPixels) {
		return AccessController.doPrivileged(new PrivilegedAction<Icon>() {
			@Override
			public Icon run() {
				return new SVGIconCreator(url).setHeight(heightPixels).create();
			}
		});
	}

	public static Icon createSVGIconHavingWidth(final URL url, final int widthPixels) {
		return AccessController.doPrivileged(new PrivilegedAction<Icon>() {
			@Override
			public Icon run() {
				return new SVGIconCreator(url).setWidth(widthPixels).create();
			}
		});
	}

	public static Icon createSVGIcon(final URL url) {
		return AccessController.doPrivileged(new PrivilegedAction<Icon>() {
			@Override
			public Icon run() {
				return new SVGIconCreator(url).create();
			}
		});
	}

	private static class SVGIconCreator {
		private float aspectRatio;
		private SVGIcon icon;

		SVGIconCreator(URL url) {
			if (svgUniverse == null)
				svgUniverse = new SVGUniverse();
			icon = new SVGIcon();
			URI svgUri;
			try {
				try {
					new URI(url.toString());
					svgUri = svgUniverse.loadSVG(url);
				}
				catch (URISyntaxException ex) {
					svgUri = svgUniverse.loadSVG(url.openStream(), url.getPath());
				}
				icon.setSvgUniverse(svgUniverse);
				icon.setSvgURI(svgUri);
				final SVGDiagram diagram = svgUniverse.getDiagram(svgUri);
				aspectRatio = diagram.getHeight() / diagram.getWidth();
				icon.setAutosize(SVGIcon.AUTOSIZE_STRETCH);
				icon.setAntiAlias(isSvgAntialiasEnabled());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public Icon create() {
			return new CachingIcon(icon);
		}

		SVGIconCreator setHeight(final int heightPixels) {
			icon.setPreferredSize(new Dimension((int) (heightPixels / aspectRatio), heightPixels));
			return this;
		}

		SVGIconCreator setWidth(final int widthPixels) {
			icon.setPreferredSize(new Dimension(widthPixels, (int) (widthPixels * aspectRatio)));
			return this;
		}
	}

	public static ImageIcon toImageIcon(Icon icon) {
		if(icon instanceof ImageIcon)
			return (ImageIcon) icon;
		if(icon instanceof CachingIcon)
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
