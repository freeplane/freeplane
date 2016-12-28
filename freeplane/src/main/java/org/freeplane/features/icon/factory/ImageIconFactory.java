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

import static org.freeplane.core.ui.LengthUnits.pt;

import java.net.URL;
import java.util.Locale;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.icon.UIIcon;

/**
 * 
 * Factory for swing icons used in the GUI.
 * 
 * @author Tamas Eppel
 *
 */
public final class ImageIconFactory {
	private static final Quantity<LengthUnits> DEFAULT_ICON_HEIGHT = new Quantity<LengthUnits>(48, pt);
	private static final ImageIconFactory FACTORY = new ImageIconFactory();
	private static final String DEFAULT_IMAGE_PATH = "/images/";
	private static final ImageIcon ICON_NOT_FOUND = FACTORY.getImageIcon(ResourceController.getResourceController()
	    .getResource(DEFAULT_IMAGE_PATH + "IconNotFound.png"));
	private final WeakValueCache<String, ImageIcon> ICON_CACHE = new WeakValueCache<String, ImageIcon>();
	private final WeakHashMap<ImageIcon, URL> ICON_URLS = new WeakHashMap<ImageIcon, URL>();

	public static ImageIconFactory getInstance() {
		return FACTORY;
	}

	public ImageIcon getImageIcon(final UIIcon uiIcon) {
		return getImageIcon(uiIcon.getUrl(), DEFAULT_ICON_HEIGHT);
	}

	public ImageIcon getImageIcon(final URL url) {
		return getImageIcon(url, DEFAULT_ICON_HEIGHT);
	}

	private String createCacheKey(final URL url, final int heightPixels) {
		return url.toString() + "#" + heightPixels;
	}


	public Icon getImageIcon(UIIcon uiIcon, Quantity<LengthUnits> iconHeight) {
		return getImageIcon(uiIcon, iconHeight);
	}
	
	public ImageIcon getImageIcon(final URL url, Quantity<LengthUnits> iconHeight) {
		ImageIcon result = ICON_NOT_FOUND;
		if (url != null) {
			final int heightPixels = iconHeight.toBaseUnitsRounded();
			final String cacheKey = createCacheKey(url, heightPixels);
			if (ICON_CACHE.containsKey(cacheKey)) {
				result = ICON_CACHE.get(cacheKey);
			}
			else {
				if (url.getPath().toLowerCase(Locale.ENGLISH).endsWith(".svg")) {
					result = FreeplaneIconUtils.createSVGIconPrivileged(url, heightPixels);
				} else {
					result = FreeplaneIconUtils.createImageIconPrivileged(url);
				}
				ICON_CACHE.put(cacheKey, result);
				ICON_URLS.put(result, url);
			}
		}
		return result;
	}

	public boolean canScaleIcon(final Icon icon) {
		return ICON_URLS.containsKey(icon);
		
	}
	public Icon getScaledIcon(final Icon icon, Quantity<LengthUnits> iconHeight) {
		if(iconHeight.toBaseUnitsRounded() == icon.getIconHeight())
			return icon;
		final URL iconUrl = ICON_URLS.get(icon);
		if (iconUrl != null)
			return getImageIcon(iconUrl, iconHeight);
		else
			throw new IllegalArgumentException("unknown icon");
	}
}
