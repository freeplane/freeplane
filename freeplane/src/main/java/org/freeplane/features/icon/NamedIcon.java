/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2019 dimitry
 *
 *  This file author is dimitry
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

import javax.swing.Icon;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;

/**
 * @author Dimitry Polivaev
 * Dec 25, 2019
 */
public interface NamedIcon extends Comparable<NamedIcon> {
    String getName();
    String getFile();
	Icon getIcon();
	Icon getIcon(Quantity<LengthUnits> iconHeight);
	NamedIcon zoom(float zoom);
	int getOrder();
	

	@Override
    default int compareTo(final NamedIcon uiIcon) {
		return Integer.compare(getOrder(), uiIcon.getOrder());
	}
}
