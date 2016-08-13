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
	private final WeakHashMap<URL, ImageIcon> ICON_CACHE = new WeakHashMap<URL, ImageIcon>();

	public static ImageIconFactory getInstance() {
		return FACTORY;
	}

	public ImageIcon getImageIcon(final UIIcon uiIcon) {
		return getImageIcon(uiIcon.getUrl());
	}

	public ImageIcon getImageIcon(final URL url) {
		ImageIcon result = ICON_NOT_FOUND;
		if (url != null) {
			if (ICON_CACHE.containsKey(url)) {
				result = ICON_CACHE.get(url);
			}
			else {
				result = FreeplaneIconUtils.createImageIconPrivileged(url);
				ICON_CACHE.put(url, result);
			}
		}
		return result;
	}
}
