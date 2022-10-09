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
package org.freeplane.features.layout;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.LayoutOrientation;
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
class LayoutBuilder implements IExtensionAttributeWriter {
	private void registerAttributeHandlers(final ReadManager reader) {
		final IAttributeHandler layoutOrientationHandler = new IAttributeHandler() {
			@Override
            public void setAttribute(final Object userObject, final String value) {
				final NodeModel node = (NodeModel) userObject;
				LayoutModel.createLayoutModel(node).setLayoutOrientation(LayoutOrientation.valueOf(value));
			}
		};
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "LAYOUT_ORIENTATION", layoutOrientationHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "LAYOUT_ORIENTATION", layoutOrientationHandler);
		final IAttributeHandler childrenSidesHandler = new IAttributeHandler() {
            @Override
            public void setAttribute(final Object userObject, final String value) {
                final NodeModel node = (NodeModel) userObject;
                LayoutModel.createLayoutModel(node).setChildrenSides(ChildrenSides.valueOf(value));
            }
        };
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "CHILDREN_SIDES", childrenSidesHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "CHILDREN_SIDES", childrenSidesHandler);
        final IAttributeHandler verticalAlignmentHandler = new IAttributeHandler() {
            public void setAttribute(final Object userObject, final String value) {
                final NodeModel node = (NodeModel) userObject;
                LayoutModel.createLayoutModel(node).setChildNodesAlignment(ChildNodesAlignment.valueOf(value));
            }
        };
        reader.addAttributeHandler(NodeBuilder.XML_NODE, "CHILD_NODES_ALIGNMENT", verticalAlignmentHandler);
        reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "CHILD_NODES_ALIGNMENT", verticalAlignmentHandler);
	}

	void registerBy(final ReadManager readManager, final WriteManager writeManager) {
		registerAttributeHandlers(readManager);
		writeManager.addExtensionAttributeWriter(LayoutModel.class, this);
	}

	@Override
    public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
		final LayoutModel layoutModel = (LayoutModel) extension;
		LayoutOrientation layoutOrientation = layoutModel.getLayoutOrientation();
		if (layoutOrientation != LayoutOrientation.NOT_SET) {
			writer.addAttribute("LAYOUT_ORIENTATION", layoutOrientation.name());
		}
		ChildrenSides childrenSides = layoutModel.getChildrenSides();
        if (childrenSides != ChildrenSides.NOT_SET) {
            writer.addAttribute("CHILDREN_SIDES", layoutOrientation.name());
        }
        final ChildNodesAlignment alignment = layoutModel.getChildNodesAlignment();
        if (alignment != LayoutModel.DEFAULT_CHILD_NODES_ALIGNMENT) {
            writer.addAttribute("CHILD_NODES_ALIGNMENT", alignment.name());
        }

	}
}
