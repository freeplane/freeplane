/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
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
package org.freeplane.features.mindmapmode.edge;

import java.awt.Color;
import java.util.Random;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.map.AMapChangeListenerAdapter;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Nov 28, 2010
 */

@NodeHookDescriptor(hookName = "AutomaticEdgeColor")
@ActionLocationDescriptor(locations = "/menu_bar/format/edges")
public class AutomaticEdgeColorHook extends PersistentNodeHook implements IExtension {
	private class Listener extends AMapChangeListenerAdapter{
		private Random random = new Random(); 
		@Override
	    public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
			if(!isActive(child)){
				return;
			}
			if(parent.isRoot()){
				final EdgeModel edgeModel = EdgeModel.createEdgeModel(child);
				if(null == edgeModel.getColor()){
					final MEdgeController controller = (MEdgeController) EdgeController.getController();
					controller.setColor(child, randomColor());
					controller.setWidth(child, 3);
				}
			}
			else{
				final MEdgeController controller = (MEdgeController) EdgeController.getController();
				controller.setColor(child, null);
				controller.setWidth(child, EdgeModel.DEFAULT_WIDTH);
				controller.setStyle(child, null);
			}
	    }

		private Color randomColor() {
			return new Color (random.nextInt(255), random.nextInt(255), random.nextInt(255)); 
	    }
	}

	public AutomaticEdgeColorHook() {
	    super();
		final Listener listener = new Listener();
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		mapController.addMapChangeListener(listener);
    }

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

}

