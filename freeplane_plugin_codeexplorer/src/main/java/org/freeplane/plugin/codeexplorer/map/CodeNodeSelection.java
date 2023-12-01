/*
 * Created on 19 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.stream.Stream;

import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.thirdparty.com.google.common.base.Supplier;

enum CodeNodeSelection implements Supplier<Stream<CodeNode>>{
    VISIBLE(CodeNodeSelection::visibleNodes),
    SELECTED(CodeNodeSelection::selectedNodes);

    static Stream<CodeNode> selectedNodes(){
        return CodeNodeStream.selectedNodes(Controller.getCurrentController().getSelection());
    }

    static Stream<CodeNode> visibleNodes(){
        MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
        return CodeNodeStream.visibleNodes(mapView);
    }

    final private Supplier<Stream<CodeNode>> nodeSupplier;

    private CodeNodeSelection(Supplier<Stream<CodeNode>> nodeSupplier) {
        this.nodeSupplier = nodeSupplier;
    }

    @Override
    public Stream<CodeNode> get() {
        return nodeSupplier.get();
    }

}