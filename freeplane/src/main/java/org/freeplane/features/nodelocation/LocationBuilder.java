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

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
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
		final IAttributeHandler vShiftHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				LocationModel.createLocationModel(node).setShiftY(Quantity.fromString(value, LengthUnit.px));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "VSHIFT", vShiftHandler);
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "VSHIFT_QUANTITY", vShiftHandler);
		final IAttributeHandler vgapHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				LocationModel.createLocationModel(node).setVGap(Quantity.fromString(value, LengthUnit.px));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "VGAP", vgapHandler);
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "VGAP_QUANTITY", vgapHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "VGAP", vgapHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "VGAP_QUANTITY", vgapHandler);
		final IAttributeHandler hgapHandler = new IAttributeHandler() {
			public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				LocationModel.createLocationModel(node).setHGap(Quantity.fromString(value, LengthUnit.px));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "HGAP_QUANTITY", hgapHandler);
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "HGAP", hgapHandler);
	}

	void registerBy(final ReadManager readManager, final WriteManager writeManager) {
		registerAttributeHandlers(readManager);
		writeManager.addExtensionAttributeWriter(LocationModel.class, this);
	}

	public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
		final LocationModel locationModel = (LocationModel) extension;
		final Quantity<LengthUnit> vGap = locationModel.getVGap();
		if (vGap != LocationModel.DEFAULT_VGAP) {
			writer.addAttribute("VGAP_QUANTITY", vGap.toString());
		}
		final Quantity<LengthUnit> hGap = locationModel.getHGap();
		if (locationModel.getHGap() != LocationModel.DEFAULT_HGAP) {
			writer.addAttribute("HGAP_QUANTITY", hGap.toString());
		}
		final Quantity<LengthUnit> shiftY = locationModel.getShiftY();
		if (shiftY != LocationModel.DEFAULT_SHIFT_Y) {
			writer.addAttribute("VSHIFT_QUANTITY", shiftY.toString());
		}
	}
}
