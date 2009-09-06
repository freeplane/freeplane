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
package org.freeplane.core.icon.factory;

import java.util.WeakHashMap;

import javax.swing.ImageIcon;

import org.freeplane.core.icon.UIIcon;

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
	
	private static final ImageIcon ICON_NOT_FOUND 
		= new ImageIcon(DEFAULT_IMAGE_PATH + "IconNotFound.png");

	private final WeakHashMap<String, ImageIcon> ICON_CACHE
		= new WeakHashMap<String, ImageIcon>();
	
	public static ImageIconFactory getInstance() {
		return FACTORY;
	}
	
	public ImageIcon getImageIcon(UIIcon uiIcon) {
		return getImageIcon(uiIcon.getPath());
	}
	
	public ImageIcon getImageIcon(String fileName) {
		ImageIcon result = ICON_NOT_FOUND;
		if(fileName != null) {
			if(ICON_CACHE.containsKey(fileName)) {
				result = ICON_CACHE.get(fileName);
			}
			else {
				result = new ImageIcon(fileName);
				ICON_CACHE.put(fileName, result);
			}
		}
		return result;
	}
}
