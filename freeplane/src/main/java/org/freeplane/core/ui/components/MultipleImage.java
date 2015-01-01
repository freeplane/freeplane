/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.features.icon.UIIcon;

public class MultipleImage implements Icon {
	final private List<Icon> mIcons = new ArrayList<Icon>();
	final private List<UIIcon> mUIIcons = new ArrayList<UIIcon>();

	public MultipleImage() {
	}

	public void addIcon(final UIIcon uiIcon) {
		mIcons.add(uiIcon.getIcon());
		mUIIcons.add(uiIcon);
	}

	public void addLinkIcon(Icon icon) {
		mIcons.add(icon);
		mUIIcons.add(null);
	};

	public int getIconHeight() {
		int myY = 0;
		for (final Icon icon : mIcons) {
			final int otherHeight = icon.getIconHeight();
			if (otherHeight > myY) {
				myY = otherHeight;
			}
		}
		return myY;
	};

	public int getIconWidth() {
		int myX = 0;
		for (final Icon icon : mIcons) {
			myX += icon.getIconWidth();
		}
		return myX;
	}

	public int getImageCount() {
		return mIcons.size();
	}

	public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
		int myX = x;
		for (final Icon icon : mIcons) {
			icon.paintIcon(c, g, myX, y);
			myX += icon.getIconWidth();
		}
	}
	
	public UIIcon getUIIconAt(Point coordinate){
		if(coordinate.x < 0 || coordinate.y < 0)
			return null;
		int iconX = 0;
		for (int iconIndex = 0; iconIndex < mIcons.size(); iconIndex++)
		{
			iconX += mIcons.get(iconIndex).getIconWidth();
			if(coordinate.x <= iconX){
				return mUIIcons.get(iconIndex);
			}
		}
		return null;
	}
	
	//DOCEAR - get a rect relative to this image for a specific icon  
	public Rectangle getIconR(Icon icon) {
		int myX = 0;
		for (final Icon ico : mIcons) {
			if(ico.equals(icon)) {
				return new Rectangle(myX, 0, ico.getIconWidth(), ico.getIconHeight());
			}
			myX += ico.getIconWidth();
		}
		return null;
	}
};
