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
package org.freeplane.core.icon;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class ZoomedIcon extends UIIcon {
	final static private Map<UIIcon, Map<Float, ImageIcon>> zoomedIcons = new HashMap<UIIcon, Map<Float,ImageIcon>>();
	private final UIIcon uiIcon;
	
	private final float zoom;
	
	private ImageIcon zoomedIcon;
	
	public ZoomedIcon(UIIcon uiIcon, float zoom) {
		super(uiIcon.getName(), uiIcon.getFileName(), uiIcon.getDescription(), uiIcon.getShortcutKey());
		this.uiIcon = uiIcon;
		this.zoom   = zoom;
	}
	
	@Override
	public ImageIcon getIcon() {
		if(zoomedIcon == null) {
			Map<Float, ImageIcon> icons = zoomedIcons.get(uiIcon);
			if(icons == null){
				icons = new HashMap<Float, ImageIcon>();
				zoomedIcons.put(uiIcon, icons);
			}
			zoomedIcon = icons.get(zoom);
			if(zoomedIcon != null){
				return zoomedIcon;
			}
			final ImageIcon icon = uiIcon.getIcon();
			final Image scaledImage = uiIcon.getIcon()
					  .getImage()
					  .getScaledInstance(
							  (int)(icon.getIconWidth() * zoom), 
							  (int)(icon.getIconHeight() * zoom),
							  Image.SCALE_SMOOTH);
			zoomedIcon = new ImageIcon(scaledImage);
			icons.put(zoom, zoomedIcon);
		}
		return zoomedIcon; 
	}
	
	@Override
	public String getPath() {
		return uiIcon.getPath();
	}
	
	@Override
	public URL getUrl() {
		return uiIcon.getUrl();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && zoom == ((ZoomedIcon)obj).zoom;
	}

	@Override
	public int hashCode() {
		return 31 * super.hashCode() + Float.valueOf(zoom).hashCode();
	}
	
}
