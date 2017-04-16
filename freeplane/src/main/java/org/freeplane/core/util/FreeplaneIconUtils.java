package org.freeplane.core.util;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
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

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;

/** utility methods to access Freeplane's (builtin and user) icons. */
public class FreeplaneIconUtils {

	private static final String ANTIALIAS_SVG = "antialias_svg";
	private static SVGUniverse svgUniverse;

	public static Icon createStandardIcon(String iconKey) {
        return MindIconFactory.createIcon(iconKey).getIcon();
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

	private static boolean isSvgAntialiasEnabled() {
		return ResourceController.getResourceController().getBooleanProperty(ANTIALIAS_SVG);
	}

	public static SVGIcon createSVGIcon(final URL url, final int heightPixels) {
		return AccessController.doPrivileged(new PrivilegedAction<SVGIcon>() {
			@Override
			public SVGIcon run() {
				return new SVGIconCreator(url).setHeight(heightPixels).create();
			}
		});
	}

	public static SVGIcon createSVGIconHavingWidth(final URL url, final int widthPixels) {
		return AccessController.doPrivileged(new PrivilegedAction<SVGIcon>() {
			@Override
			public SVGIcon run() {
				return new SVGIconCreator(url).setWidth(widthPixels).create();
			}
		});
	}


	public static SVGIcon createSVGIcon(final URL url) {
		return AccessController.doPrivileged(new PrivilegedAction<SVGIcon>() {
			@Override
			public SVGIcon run() {
				return new SVGIconCreator(url).create();
			}
		});
	}
	
	private static class SVGIconCreator {
		private float aspectRatio;
		private SVGIcon icon;
		
		SVGIconCreator (URL url) {
			if (svgUniverse == null)
				svgUniverse = new SVGUniverse();

			icon = new SVGIcon();
			URI svgUri;
			try {
				try {
					new URI(url.toString());
					svgUri = svgUniverse.loadSVG(url);
				}
				catch (URISyntaxException ex){
					svgUri = svgUniverse.loadSVG(url.openStream(), url.getPath());
				}
				icon.setSvgUniverse(svgUniverse);
				icon.setSvgURI(svgUri);
				final SVGDiagram diagram = svgUniverse.getDiagram(svgUri);
				aspectRatio = diagram.getHeight()/diagram.getWidth();
				icon.setAutosize(SVGIcon.AUTOSIZE_STRETCH);
				icon.setAntiAlias(isSvgAntialiasEnabled());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public SVGIcon create() {
			return icon;
		}
		
		SVGIconCreator setHeight(final int heightPixels) {
			icon.setPreferredSize(new Dimension((int)(heightPixels / aspectRatio), heightPixels));
			return this;
		}
		
		SVGIconCreator setWidth(final int widthPixels) {
			icon.setPreferredSize(new Dimension(widthPixels, (int)(widthPixels * aspectRatio)));
			return this;
		}
	}
}
