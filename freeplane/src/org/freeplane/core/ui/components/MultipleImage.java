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
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

public class MultipleImage implements Icon {
	final private List<Icon> mIcons = new ArrayList<Icon>();

	public MultipleImage() {
	}

	public void addImage(final Icon icon) {
		mIcons.add(icon);
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
	
	public void addOrReplaceIcon(Icon oldIcon, Icon newIcon) {
		if(newIcon == null || mIcons.indexOf(newIcon) >= 0) {
			return;
		}
		
		if(oldIcon == null) {
			mIcons.add(newIcon);
		}
		else {
    		int index = mIcons.indexOf(oldIcon);
    		if(index > -1) {
    			mIcons.remove(index);
    			mIcons.add(index, newIcon);
    		}
    		else {
    			mIcons.add(newIcon);
    		}
		}
	}
};
