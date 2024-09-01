/*
 * Created on 24 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeStream;
import org.freeplane.features.mode.Controller;

class AncestorsHider {

    static void hideAncestors() {
        IMapSelection selection = Controller.getCurrentController().getSelection();
        CodeNode node = (CodeNode) selection.getSelected();
        Filter filter = selection.getFilter();
        FilterController filterController = FilterController.getCurrentFilterController();
        NodeStream.of(selection.getSelectionRoot())
        .filter(n -> n.isFolded() && filter.accepts(n))
        .forEach(n -> n.setFolded(false));
        boolean isAnyLeafDescendantSelected = NodeStream.of(node)
        .filter(n -> n.isLeaf() && filter.accepts(n) && selection.isSelected(n))
        .findAny().isPresent();
        if (! isAnyLeafDescendantSelected) {
            NodeStream.of(node)
            .filter(n -> n.isLeaf() && filter.accepts(n))
            .findFirst()
            .ifPresent(n -> selection.toggleSelected(n));
        }
        Filter leafFilter = new Filter(filter.getCondition(), false, false, filter.areDescendantsShown(), false,
                filter.getFilteredElement(), null);
        filterController.applyFilter(node.getMap(), false, leafFilter);
    }

}
