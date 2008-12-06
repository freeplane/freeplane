/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.modes.mindmapmode.actions.instance;

public class MoveNodeActionInstance extends NodeActionInstance {
	private int hGap;
	private int shiftY;
	private int vGap;

	public int getHGap() {
		return hGap;
	}

	public int getShiftY() {
		return shiftY;
	}

	public int getVGap() {
		return vGap;
	}

	public void setHGap(final int hGap) {
		this.hGap = hGap;
	}

	public void setShiftY(final int shiftY) {
		this.shiftY = shiftY;
	}

	public void setVGap(final int vGap) {
		this.vGap = vGap;
	}
}
