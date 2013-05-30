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
package org.freeplane.features.nodelocation;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * 06.12.2008
 */
class LocationBuilder implements IExtensionAttributeWriter {
	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "VSHIFT", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				LocationModel.createLocationModel(node).setShiftY(Integer.parseInt(value));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "VGAP", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				LocationModel.createLocationModel(node).setVGap(Integer.parseInt(value));
			}
		});
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "HGAP", new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				LocationModel.createLocationModel(node).setHGap(Integer.parseInt(value));
			}
		});
	}

	void registerBy(final ReadManager readManager, final WriteManager writeManager) {
		registerAttributeHandlers(readManager);
		writeManager.addExtensionAttributeWriter(LocationModel.class, this);
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
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
