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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

public class UIIconSet extends UIIcon {

	Collection<UIIcon> uiIcons;
	
	List<ImageIcon> imageIcons;
	
	private ImageIcon compounIcon;
	
	public UIIconSet(Collection<UIIcon> uiIcons) {
		super("", "");
		this.uiIcons    = uiIcons;
		this.imageIcons = new LinkedList<ImageIcon>();
		for(UIIcon uiIcon : uiIcons) {
			imageIcons.add(uiIcon.getIcon());
		}
	}
	
	@Override
	public ImageIcon getIcon() {
		if(compounIcon == null) {
			final BufferedImage outImage = new BufferedImage(getTotalWidth(), getMaxHeight(), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = outImage.createGraphics();
			double width = 0.0;
			for (final ImageIcon icon : imageIcons) {
				final AffineTransform inttrans = AffineTransform.getTranslateInstance(width, 0);
				g.drawImage(icon.getImage(), inttrans, null);
				width += icon.getIconWidth();
			}
			g.dispose();
			compounIcon = new ImageIcon(outImage);
		}
		return compounIcon;
	}
	
	private int getMaxHeight() {
		int height = 0;
		for(ImageIcon icon : imageIcons) {
			height = Math.max(height, icon.getIconHeight());
		}
		return height;
	}
	
	private int getTotalWidth() {
		int width = 0;
		for(ImageIcon icon : imageIcons) {
			width += icon.getIconWidth();
		}
		return width;
	}
	
	@Override
	public int compareTo(UIIcon uiIcon) {
		return 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		return uiIcons.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return uiIcons.hashCode();
	}
}
