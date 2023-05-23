/*
 * Created on 23 May 2023
 *
 * author dimitry
 */
package org.freeplane.features.styles.mindmapmode;

import java.util.function.Consumer;

import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;

public class SelectedNodeChangeListener{
    public static void onSelectedNodeChange(Consumer<NodeModel> callback) {
        final ModeController modeController = Controller.getCurrentModeController();
        final MapController mapController = modeController.getMapController();
        final Controller controller = Controller.getCurrentController();
        mapController.addNodeSelectionListener(new INodeSelectionListener() {
            @Override
            public void onSelect(final NodeModel node) {
                final IMapSelection selection = controller.getSelection();
                if (selection == null) {
                    return;
                }
                if (selection.size() >= 1) {
                    callback.accept(selection.getSelected());
                }
            }
        });
        mapController.addUINodeChangeListener(new INodeChangeListener() {
            @Override
            public void nodeChanged(final NodeChangeEvent event) {
                final IMapSelection selection = controller.getSelection();
                if (selection == null) {
                    return;
                }
                final NodeModel node = event.getNode();
                if (selection.getSelected().equals(node)) {
                    callback.accept(selection.getSelected());
                }
            }
        });
        mapController.addUIMapChangeListener(new IMapChangeListener() {

            @Override
            public void mapChanged(MapChangeEvent event) {
                Object property = event.getProperty();
                if(! MapStyle.MAP_STYLES.equals(property) && ! MapStyle.MAP_LAYOUT.equals(property)
                        && MapChangeEventProperty.MAP_VIEW_ROOT != property)
                    return;
                final IMapSelection selection = controller.getSelection();
                if (selection == null) {
                    return;
                }
                callback.accept(selection.getSelected());
            }

        });
        IMapSelectionListener mapSelectionListener = new IMapSelectionListener() {

            @Override
            public void afterMapChange(MapModel oldMap, MapModel newMap) {
                if(newMap == null)
                    callback.accept(null);
            }

        };
        controller.getMapViewManager().addMapSelectionListener(mapSelectionListener);

    }
}