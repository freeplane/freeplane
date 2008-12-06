/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.map.nodelocation;

import org.freeplane.extension.IExtension;
import org.freeplane.io.IAttributeHandler;
import org.freeplane.io.IAttributeWriter;
import org.freeplane.io.ITreeWriter;
import org.freeplane.io.ReadManager;
import org.freeplane.io.WriteManager;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.NodeBuilder.NodeObject;

/**
 * @author Dimitry Polivaev
 * 06.12.2008
 */
class LocationBuilder implements IAttributeHandler,
        IAttributeWriter<IExtension> {
	public boolean parseAttribute(final Object userObject, final String tag,
	                              final String name, final String value) {
		final NodeModel node = ((NodeObject) userObject).node;
		if (name.equals("VSHIFT")) {
			node.createLocationModel().setShiftY(Integer.parseInt(value));
			return true;
		}
		else if (name.equals("VGAP")) {
			node.createLocationModel().setVGap(Integer.parseInt(value));
			return true;
		}
		else if (name.equals("HGAP")) {
			node.createLocationModel().setHGap(Integer.parseInt(value));
			return true;
		}
		return false;
	}

	void registerBy(final ReadManager readManager,
	                final WriteManager writeManager) {
		readManager.addAttributeHandler("node", this);
		writeManager.addExtensionAttributeWriter(LocationModel.class, this);
	}

	public void writeAttributes(final ITreeWriter writer,
	                            final Object userObject,
	                            final IExtension extension) {
		final LocationModel locationModel = (LocationModel) extension;
		final int vGap = locationModel.getVGap();
		if (vGap != LocationModel.VGAP) {
			writer.addAttribute("VGAP", Integer.toString(vGap));
		}
		final int hGap = locationModel.getHGap();
		if (locationModel.getHGap() != LocationModel.HGAP) {
			writer.addAttribute("HGAP", Integer.toString(hGap));
		}
		final int shiftY = locationModel.getShiftY();
		if (shiftY != 0) {
			writer.addAttribute("VSHIFT", Integer.toString(shiftY));
		}
	}
}
