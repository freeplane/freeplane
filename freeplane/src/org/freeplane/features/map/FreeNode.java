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
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Apr 9, 2011
 */
@NodeHookDescriptor(hookName = "FreeNode", onceForMap = false)
public class FreeNode extends PersistentNodeHook implements IExtension{
	
	public static void install(){
		new FreeNode();
	};
	

	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		return this;
	}
	
	@Override
	public void undoableToggleHook(NodeModel node, IExtension extension) {
		if(node.isRoot())
				return;
		final NodeModel[] selecteds = getSelectedNodes();
		((MLocationController)LocationController.getController()).moveNodePosition(node, -1, LocationModel.HGAP, 0);
		super.undoableToggleHook(node, extension);
		if(isFreeNode(node)){
			MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
			mapController.moveNode(node, 0);
		}
		Controller.getCurrentController().getSelection().replaceSelection(selecteds);
	}


	static public boolean isFreeNode(final NodeModel nodeModel) {
		return nodeModel.containsExtension(FreeNode.class);
	}
}

