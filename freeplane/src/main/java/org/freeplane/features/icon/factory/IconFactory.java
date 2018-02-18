/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Tamas Eppel
 *
 *  This file author is Tamas Eppel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.icon.factory;

import java.net.URL;
import java.util.Locale;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.icon.UIIcon;

/**
 *
 * Factory for swing icons used in the GUI.
 *
 * @author Tamas Eppel
 *
 */
public final class IconFactory {
	public static final Quantity<LengthUnits> DEFAULT_UI_ICON_HEIGHT = ResourceController.getResourceController()
	    .getLengthQuantityProperty("toolbar_icon_height");
	private static final IconFactory FACTORY = new IconFactory();
	private static final String DEFAULT_IMAGE_PATH = "/images/";
	private static final Icon ICON_NOT_FOUND = FACTORY.getIcon(ResourceController.getResourceController()
	    .getResource(DEFAULT_IMAGE_PATH + "IconNotFound.png"));
	private static final String USE_SVG_ICONS = "use_svg_icons";
	private final WeakValueCache<String, Icon> ICON_CACHE = new WeakValueCache<String, Icon>();
	private final WeakHashMap<Icon, URL> ICON_URLS = new WeakHashMap<Icon, URL>();

	public static IconFactory getInstance() {
		return FACTORY;
	}

	public Icon getIcon(final UIIcon uiIcon) {
		return getIcon(uiIcon.getUrl(), DEFAULT_UI_ICON_HEIGHT);
	}

	public Icon getIcon(final URL url) {
		return getIcon(url, DEFAULT_UI_ICON_HEIGHT);
	}

	private String createCacheKey(final URL url, final int heightPixels) {
		return url.toString() + "#" + heightPixels;
	}

	public Icon getIcon(UIIcon uiIcon, Quantity<LengthUnits> iconHeight) {
		return getIcon(uiIcon.getUrl(), iconHeight);
	}

	public Icon getIcon(final URL url, Quantity<LengthUnits> iconHeight) {
		Icon result = ICON_NOT_FOUND;
		if (url != null) {
			final int heightPixels = iconHeight.toBaseUnitsRounded();
			final String cacheKey = createCacheKey(url, heightPixels);
			if (ICON_CACHE.containsKey(cacheKey)) {
				result = ICON_CACHE.get(cacheKey);
			}
			else {
				if (url.getPath().toLowerCase(Locale.ENGLISH).endsWith(".svg")) {
					result = FreeplaneIconFactory.createSVGIcon(url, heightPixels);
					ICON_URLS.put(FreeplaneIconFactory.toImageIcon(result), url);
				}
				else {
					result = FreeplaneIconFactory.createIconPrivileged(url);
				}
				ICON_CACHE.put(cacheKey, result);
				ICON_URLS.put(result, url);
				ICON_URLS.put(result, url);
			}
		}
		return result;
	}

	public boolean canScaleIcon(final Icon icon) {
		return ICON_URLS.containsKey(icon);
	}

	public Icon getScaledIcon(final Icon icon, Quantity<LengthUnits> iconHeight) {
		if (iconHeight.toBaseUnitsRounded() == icon.getIconHeight())
			return icon;
		final URL iconUrl = ICON_URLS.get(icon);
		if (iconUrl != null)
			return getIcon(iconUrl, iconHeight);
		else
			throw new IllegalArgumentException("unknown icon");
	}

	static public boolean isSvgIconsEnabled() {
		return ResourceController.getResourceController().getBooleanProperty(IconFactory.USE_SVG_ICONS);
	}

	public static String[] getAlternativePaths(final String resourcePath) {
		final String pngSuffix = ".png";
		if (isSvgIconsEnabled() && resourcePath.endsWith(pngSuffix)) {
			final String svgPath = resourcePath.substring(0, resourcePath.length() - pngSuffix.length()) + ".svg";
			return new String[] { svgPath, resourcePath };
		}
		else
			return new String[] { resourcePath };
	}
}
