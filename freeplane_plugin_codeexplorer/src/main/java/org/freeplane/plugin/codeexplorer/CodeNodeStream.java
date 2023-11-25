/*
 * Created on 19 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeIterator;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class CodeNodeStream {

    static Stream<CodeNodeModel> selectedNodes(IMapSelection selection){
        return selection.getSelection().stream().map(CodeNodeModel.class::cast);
    }

    static Stream<CodeNodeModel> visibleNodes(MapView mapView){
        NodeIterator<NodeView> nodeViewIterator = NodeIterator.of(mapView.getRoot(), NodeView::getChildrenViews);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(nodeViewIterator, Spliterator.ORDERED), false)
                .filter(NodeView::isContentVisible)
                .map(NodeView::getNode)
                .map(CodeNodeModel.class::cast);
    }
}
