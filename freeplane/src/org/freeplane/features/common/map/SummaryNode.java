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
package org.freeplane.features.common.map;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Apr 9, 2011
 */
@NodeHookDescriptor(hookName = "SummaryNode", onceForMap = false)
public class SummaryNode extends PersistentNodeHook implements IExtension{
	
	public static void install(){
		new SummaryNode();
		new FirstGroupNode();
	};
	
	static public boolean isFirstGroupNode(final NodeModel nodeModel) {
		return nodeModel.containsExtension(FirstGroupNode.class);
	}

	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		return this;
	}
	
	static public boolean isSummaryNode(final NodeModel nodeModel) {
		return nodeModel.containsExtension(SummaryNode.class);
	}
	
}

@NodeHookDescriptor(hookName = "FirstGroupNode", onceForMap = false)
class FirstGroupNode extends PersistentNodeHook implements IExtension{
	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		return this;
	}
	
}

