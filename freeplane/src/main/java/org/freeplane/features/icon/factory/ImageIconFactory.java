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

import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.features.icon.UIIcon;

/**
 * 
 * Factory for swing icons used in the GUI.
 * 
 * @author Tamas Eppel
 *
 */
public final class ImageIconFactory {
	private static final ImageIconFactory FACTORY = new ImageIconFactory();
	private static final String DEFAULT_IMAGE_PATH = "/images/";
	private static final ImageIcon ICON_NOT_FOUND = new ImageIcon(ResourceController.getResourceController()
	    .getResource(DEFAULT_IMAGE_PATH + "IconNotFound.png"));
	private final WeakHashMap<String, ImageIcon> ICON_CACHE = new WeakHashMap<String, ImageIcon>();

	public static ImageIconFactory getInstance() {
		return FACTORY;
	}

	public ImageIcon getImageIcon(final UIIcon uiIcon) {
		return getImageIcon(uiIcon.getUrl(), 16, 16);
	}

	public ImageIcon getImageIcon(final UIIcon uiIcon, final int widthPixels, final int heightPixels) {
		return getImageIcon(uiIcon.getUrl(), widthPixels, heightPixels);
	}

	public ImageIcon getImageIcon(final URL url) {
		return getImageIcon(url, 64, 64);
	}

	private String createCacheKey(final URL url, final int widthPixels, final int heightPixels) {
		return url.toString() + "-" + widthPixels + "x" + heightPixels;
	}

	public ImageIcon getImageIcon(final URL url, final int widthPixels, final int heightPixels) {
		ImageIcon result = ICON_NOT_FOUND;
		if (url != null) {
			final String cacheKey = createCacheKey(url, widthPixels, heightPixels);
			if (ICON_CACHE.containsKey(cacheKey)) {
				result = ICON_CACHE.get(cacheKey);
			}
			else {
				if (url.getPath().toLowerCase(Locale.ENGLISH).endsWith(".svg")) {
					result = FreeplaneIconUtils.createSVGIconPrivileged(url, widthPixels, heightPixels);
				} else {
					result = FreeplaneIconUtils.createImageIconPrivileged(url);
				}
				ICON_CACHE.put(cacheKey, result);
			}
		}
		return result;
	}
}
