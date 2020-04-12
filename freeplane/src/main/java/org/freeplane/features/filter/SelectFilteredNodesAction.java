/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.filter;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapController.Direction;
import org.freeplane.features.map.MapNavigationUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
class SelectFilteredNodesAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final FilterController filterController;


	SelectFilteredNodesAction(final FilterController filterController) {
		super("SelectFilteredNodesAction");
		this.filterController = filterController;
	}

	public void actionPerformed(final ActionEvent e) {
		if(! filterController.isFilterActive()){
			return;
		}
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final NodeModel selected = selection.getSelected();
        final NodeModel rootNode = selected.getMap().getRootNode();
		Filter filter = selection.getFilter();
        boolean nodeFound = filter.getFilterInfo(rootNode).isMatched();
		if(nodeFound){
			selection.selectAsTheOnlyOneSelected(rootNode);
		}
		NodeModel next = rootNode;
		for(;;){
			next = MapNavigationUtils.findNext(Direction.FORWARD, next, rootNode);
			if(next == null){
				break;
			}
			if(next.isHiddenSummary() || ! filter.getFilterInfo(next).isMatched())
				continue;
			mapController.displayNode(next);
			if(nodeFound){
				selection.toggleSelected(next);
			}
			else{
				selection.selectAsTheOnlyOneSelected(next);
				nodeFound = true;
			}
		}
		if(filter.getFilterInfo(selected).isMatched())
		    selection.makeTheSelected(selected);
	}
}
