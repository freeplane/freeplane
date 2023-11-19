/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.main.codeexplorermode;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.SelectedViewSnapshotCondition;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

@SuppressWarnings("serial")
class FilterCyclesAction extends AFreeplaneAction {
    enum Action{
        FILTER, SELECT
    }

	private final CodeNodeSelection selection;
    private final Action action;

    public FilterCyclesAction(CodeNodeSelection selection, Action action) {
	    super("code." + action + "." + selection + ".CyclesAction");
        this.selection = selection;
        this.action = action;
    }

	@Override
    public void actionPerformed(ActionEvent e) {
        MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
        LimitedJohnsonSimpleCycles<NodeModel> cycles = new LimitedJohnsonSimpleCycles<>(()  -> new EmptyNodeModel(mapView.getMap(), "limit"));
        if(selection == CodeNodeSelection.SELECTED) {
            selection.get().forEach(cycles::addNode);
            cycles.stopSearchHere();
        }
        CodeLinkController linkController = (CodeLinkController) LinkController.getController();
        CodeNodeStream.visibleNodes(mapView)
        .flatMap(node -> linkController.getLinksFrom(node, mapView).stream())
        .forEach(connector -> cycles.addEdge(connector.getSource(), connector.getTarget()));

        List<NodeModel> cycleNodes = cycles.findSimpleCycles().stream().flatMap(List::stream).collect(Collectors.toList());
        if(! cycleNodes.isEmpty()) {
            switch(action) {
            case FILTER: {
                ASelectableCondition condition = new SelectedViewSnapshotCondition(cycleNodes);
                Filter filter = new Filter(condition, false, true, false, false, null);
                FilterController filterController = FilterController.getCurrentFilterController();
                filterController.applyFilter(mapView.getMap(), false, filter);
                break;
            }
            case SELECT: {
                NodeModel[] nodesAsArray = cycleNodes.toArray(new NodeModel[cycleNodes.size()]);
                Controller.getCurrentController().getSelection().replaceSelection(nodesAsArray);
                break;
            }
            }
        }
	}
}
