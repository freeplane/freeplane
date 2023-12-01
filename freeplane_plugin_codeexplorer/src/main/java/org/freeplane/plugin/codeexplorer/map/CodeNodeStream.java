/*
 * Created on 19 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeIterator;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class CodeNodeStream {

    static Stream<CodeNode> selectedNodes(IMapSelection selection){
        return selection.getSelection().stream().map(CodeNode.class::cast);
    }

    static Stream<CodeNode> visibleNodes(MapView mapView){
        Stream<NodeView> nodeViews = nodeViews(mapView);
        return nodeViews
                .filter(NodeView::isContentVisible)
                .map(NodeView::getNode)
                .map(CodeNode.class::cast);
    }

    static Stream<NodeView> nodeViews(MapView mapView) {
        NodeIterator<NodeView> nodeViewIterator = NodeIterator.of(mapView.getRoot(), NodeView::getChildrenViews);
        Stream<NodeView> nodeViews = StreamSupport.stream(Spliterators.spliteratorUnknownSize(nodeViewIterator, Spliterator.ORDERED), false);
        return nodeViews;
    }
}
