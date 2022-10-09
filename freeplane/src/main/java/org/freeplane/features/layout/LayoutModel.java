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
package org.freeplane.features.layout;

import java.util.Objects;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class LayoutModel implements IExtension {
    private static final LayoutModel NOT_SET = new LayoutModel();
    public static final ChildNodesAlignment DEFAULT_CHILD_NODES_ALIGNMENT = ChildNodesAlignment.UNDEFINED;

    public static LayoutModel createLayoutModel(final NodeModel node) {
        LayoutModel layoutModel = node.getExtension(LayoutModel.class);
        if (layoutModel == null) {
            layoutModel = new LayoutModel();
            node.addExtension(layoutModel);
        }
        return layoutModel;
    }
    public static LayoutModel getModel(final NodeModel node) {
        final LayoutModel location = node.getExtension(LayoutModel.class);
        return location != null ? location : LayoutModel.NOT_SET;
    }

    private LayoutOrientation layoutOrientation = LayoutOrientation.NOT_SET;
    private ChildrenSides childrenSides = ChildrenSides.NOT_SET;
    private ChildNodesAlignment childNodesAlignment = DEFAULT_CHILD_NODES_ALIGNMENT;

    public LayoutOrientation getLayoutOrientation() {
        return layoutOrientation;
    }
    
    public void setLayoutOrientation(LayoutOrientation layoutOrientation) {
        this.layoutOrientation = layoutOrientation;
    }
    
    public ChildrenSides getChildrenSides() {
        return childrenSides;
    }
    
    public void setChildrenSides(ChildrenSides childrenSides) {
        this.childrenSides = childrenSides;
    }
    

    public ChildNodesAlignment getChildNodesAlignment() {
        return childNodesAlignment;
    }

    public void setChildNodesAlignment(ChildNodesAlignment verticalAlignment) {
        Objects.requireNonNull(verticalAlignment);
        this.childNodesAlignment = verticalAlignment;
    }
    
}