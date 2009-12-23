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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.ui.components.MultipleImage;

public class UIIconSet extends UIIcon {

	final Collection<UIIcon> uiIcons;
	final float zoom; 
	
	public Collection<UIIcon> getIcons() {
		return uiIcons;
	}

	List<Icon> imageIcons;
	
	private MultipleImage compoundIcon;
	
	public UIIconSet(final Collection<UIIcon> uiIcons, float zoom) {
		super("", "");
		this.zoom = zoom;
		this.uiIcons    = Collections.unmodifiableCollection(uiIcons);
		this.imageIcons = new LinkedList<Icon>();
		for(UIIcon uiIcon : uiIcons) {
			final Icon icon;
			if(zoom == 1f) {
				icon = uiIcon.getIcon();
			}
			else{
				icon = new ZoomedIcon(uiIcon, zoom).getIcon();
			}
			imageIcons.add(icon);
		}
	}
	
	@Override
	public Icon getIcon() {
		if(compoundIcon == null) {
			compoundIcon = new MultipleImage();
			for (final Icon icon : imageIcons) {
				compoundIcon.addImage(icon);
			}
		}
		return compoundIcon;
	}
	
	@Override
	public int compareTo(UIIcon uiIcon) {
		return 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		UIIconSet uiIconSet = (UIIconSet)obj;
		return zoom == uiIconSet.zoom && uiIcons.equals(uiIconSet.uiIcons);
	}
	
	@Override
	public int hashCode() {
		return 31 * uiIcons.hashCode();
	}
}
