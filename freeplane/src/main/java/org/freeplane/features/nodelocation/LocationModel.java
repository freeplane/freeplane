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

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class LocationModel implements IExtension {
	public static final Quantity<LengthUnit> DEFAULT_HGAP = new Quantity<LengthUnit>(14, LengthUnit.pt);
	public static final int DEFAULT_HGAP_PX = DEFAULT_HGAP.toBaseUnitsRounded();
	public static Quantity<LengthUnit> DEFAULT_SHIFT_Y = new Quantity<LengthUnit>(0, LengthUnit.pt);
	public static Quantity<LengthUnit> DEFAULT_VGAP = new Quantity<LengthUnit>(2, LengthUnit.pt);
	public static final LocationModel NULL_LOCATION = new LocationModel() {
		@Override
		public void setHGap(final Quantity<LengthUnit> gap) {
			if (gap != getHGap()) {
				throw new NoSuchMethodError();
			}
		}

		@Override
		public void setShiftY(final Quantity<LengthUnit> shiftY) {
			if (shiftY != getShiftY()) {
				throw new NoSuchMethodError();
			}
		}

		@Override
		public void setVGap(final Quantity<LengthUnit> gap) {
			if (gap != getVGap()) {
				throw new NoSuchMethodError();
			}
		}
	};
	
	private Quantity<LengthUnit> hGap;
	private Quantity<LengthUnit> shiftY;
	private Quantity<LengthUnit> vGap;

	public LocationModel(){
		hGap = LocationModel.DEFAULT_HGAP;
		shiftY = LocationModel.DEFAULT_SHIFT_Y;
		vGap = LocationModel.DEFAULT_VGAP;
	}

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


	public Quantity<LengthUnit> getHGap() {
		return hGap;
	}

	public Quantity<LengthUnit> getShiftY() {
		return shiftY;
	}

	public Quantity<LengthUnit> getVGap() {
		return vGap;
	}

	public void setHGap(final Quantity<LengthUnit> gap) {
		assertNotNull(gap);
		hGap = gap;
	}

	private void assertNotNull(Object object) {
		if(object == null)
			throw new NullPointerException();
	}

	public void setShiftY(final Quantity<LengthUnit> shiftY) {
		assertNotNull(shiftY);
		this.shiftY = shiftY;
	}

	public void setVGap(final Quantity<LengthUnit> gap) {
		assertNotNull(gap);
		vGap = gap.toBaseUnits() >= 0 ? gap : new Quantity<LengthUnit>(0, gap.unit);
	}
}
