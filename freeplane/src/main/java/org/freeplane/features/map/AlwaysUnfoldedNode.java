/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
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
package org.freeplane.features.map;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Apr 9, 2011
 */
@NodeHookDescriptor(hookName = "AlwaysUnfoldedNode", onceForMap = false)
public class AlwaysUnfoldedNode extends PersistentNodeHook implements IExtension{
	
	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		return this;
	}
	
	static public boolean isConnectorNode(final NodeModel nodeModel) {
		return nodeModel.containsExtension(AlwaysUnfoldedNode.class);
	}

	@Override
    protected void add(NodeModel node, IExtension extension) {
	    super.add(node, extension);
	    node.setFolded(false);
    }
	
	
}

