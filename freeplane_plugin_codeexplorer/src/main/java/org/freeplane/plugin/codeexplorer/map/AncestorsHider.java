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
        .filter(n -> n.isFolded() && n.isVisible(filter))
        .forEach(n -> n.setFolded(false));
        boolean isAnyLeafDescendantSelected = NodeStream.of(node)
        .filter(n -> n.isLeaf() && n.isVisible(filter) && selection.isSelected(n))
        .findAny().isPresent();
        if (! isAnyLeafDescendantSelected) {
            NodeStream.of(node)
            .filter(n -> n.isLeaf() && n.isVisible(filter))
            .findFirst()
            .ifPresent(n -> selection.toggleSelected(n));
        }
        Filter leafFilter = new Filter(filter.getCondition(), false, false, filter.areDescendantsShown(), false, null);
        filterController.applyFilter(node.getMap(), false, leafFilter);
    }

}
