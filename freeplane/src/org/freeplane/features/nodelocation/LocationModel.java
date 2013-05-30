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
package org.freeplane.features.nodelocation;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class LocationModel implements IExtension {
	public final static int HGAP = 20;
	public static final LocationModel NULL_LOCATION = new LocationModel() {
		@Override
		public void setHGap(final int gap) {
			if (gap != getHGap()) {
				throw new NoSuchMethodError();
			}
		}

		@Override
		public void setShiftY(final int shiftY) {
			if (shiftY != getShiftY()) {
				throw new NoSuchMethodError();
			}
		}

		@Override
		public void setVGap(final int gap) {
			if (gap != getVGap()) {
				throw new NoSuchMethodError();
			}
		}
	};
	public final static int VGAP = 3;

	public static LocationModel createLocationModel(final NodeModel node) {
		LocationModel location = (LocationModel) node.getExtension(LocationModel.class);
		if (location == null) {
			location = new LocationModel();
			node.addExtension(location);
		}
		return location;
	}

	public static LocationModel getModel(final NodeModel node) {
		final LocationModel location = (LocationModel) node.getExtension(LocationModel.class);
		return location != null ? location : LocationModel.NULL_LOCATION;
	}

	private int hGap = LocationModel.HGAP;
	private int shiftY = 0;
	private int vGap = LocationModel.VGAP;

	public int getHGap() {
		return hGap;
	}

	public int getShiftY() {
		return shiftY;
	}

	public int getVGap() {
		return vGap;
	}

	public void setHGap(final int gap) {
		hGap = gap;
	}

	public void setShiftY(final int shiftY) {
		this.shiftY = shiftY;
	}

	public void setVGap(final int gap) {
		vGap = Math.max(gap, 0);
	}
}
