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

import org.freeplane.api.ChildNodesLayout;
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
		final IAttributeHandler childNodesLayoutHandler = new IAttributeHandler() {
            @Override
            public void setAttribute(final Object userObject, final String value) {
                final NodeModel node = (NodeModel) userObject;
                LayoutModel.createLayoutModel(node).setChildNodesLayout(ChildNodesLayout.valueOf(value));
            }
        };
		reader.addAttributeHandler(NodeBuilder.XML_NODE, "CHILD_NODES_LAYOUT", childNodesLayoutHandler);
		reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "CHILD_NODES_LAYOUT", childNodesLayoutHandler);
        final IAttributeHandler verticalAlignmentHandler = new IAttributeHandler() {
            public void setAttribute(final Object userObject, final String value) {
                final NodeModel node = (NodeModel) userObject;
                LayoutModel.createLayoutModel(node).setChildNodesLayout(layoutForAlignment(value));
            }
        };
        reader.addAttributeHandler(NodeBuilder.XML_NODE, "CHILD_NODES_ALIGNMENT", verticalAlignmentHandler);
        reader.addAttributeHandler(NodeBuilder.XML_STYLENODE, "CHILD_NODES_ALIGNMENT", verticalAlignmentHandler);
	}

	protected ChildNodesLayout layoutForAlignment(String alignment) {
	    if("AS_PARENT".equals(alignment) || "AUTO".equals(alignment)) {
	        return ChildNodesLayout.AUTO;
	    } else if ("BY_FIRST_NODE".equals(alignment)) {
	        return ChildNodesLayout.AUTO_FIRST;
	    } else if ("BY_CENTER".equals(alignment)) {
	        return ChildNodesLayout.AUTO_CENTERED;
	    } else if ("BY_LAST_NODE".equals(alignment)) {
	        return ChildNodesLayout.AUTO_LAST;
	    } else {
	        return ChildNodesLayout.NOT_SET;
	    }

    }

    void registerBy(final ReadManager readManager, final WriteManager writeManager) {
		registerAttributeHandlers(readManager);
		writeManager.addExtensionAttributeWriter(LayoutModel.class, this);
	}

	@Override
    public void writeAttributes(final ITreeWriter writer, final Object userObject, final IExtension extension) {
		final LayoutModel layoutModel = (LayoutModel) extension;
		ChildNodesLayout childrenNodesLayout = layoutModel.getChildNodesLayout();
        if (childrenNodesLayout != ChildNodesLayout.NOT_SET) {
            writer.addAttribute("CHILD_NODES_LAYOUT", childrenNodesLayout.name());
        }
 	}
}
