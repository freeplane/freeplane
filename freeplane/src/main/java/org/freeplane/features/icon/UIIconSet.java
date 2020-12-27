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
package org.freeplane.features.icon;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.ui.components.MultipleImage;

public class UIIconSet extends UIIcon {
	private static final int ORDER = Integer.MAX_VALUE;
    final Collection<NamedIcon> uiIcons;
	final float zoom;

	public Collection<NamedIcon> getIcons() {
		return uiIcons;
	}

	List<NamedIcon> imageIcons;
	private MultipleImage compoundIcon;

	public UIIconSet(final Collection<NamedIcon> uiIcons, final float zoom) {
		super("", "", ORDER);
		this.zoom = zoom;
		this.uiIcons = Collections.unmodifiableCollection(uiIcons);
		imageIcons = new LinkedList<NamedIcon>();
		for (final NamedIcon uiIcon : uiIcons) {
			final NamedIcon icon;
			if (zoom == 1f) {
				icon = uiIcon;
			}
			else {
				icon = uiIcon.zoom(zoom);
			}
			imageIcons.add(icon);
		}
	}

	@Override
	public Icon getIcon() {
		throw new RuntimeException(new NoSuchMethodException());
	}
	
	@Override
	public Icon getIcon(Quantity<LengthUnit> iconHeight) {
		if (compoundIcon == null) {
			compoundIcon = new MultipleImage();
			for (final NamedIcon icon : imageIcons) {
				compoundIcon.addIcon(icon, iconHeight);
			}
		}
		return compoundIcon;
	}

	@Override
	public int compareTo(final NamedIcon uiIcon) {
		return 1;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final UIIconSet uiIconSet = (UIIconSet) obj;
		return zoom == uiIconSet.zoom && uiIcons.equals(uiIconSet.uiIcons);
	}

	@Override
	public int hashCode() {
		return 31 * uiIcons.hashCode();
	}
}
