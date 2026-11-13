package org.freeplane.view.swing.ui;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.bookmarks.mindmapmode.NodeBookmark;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;

public class MapDisplayNameRefreshCoordinator implements INodeChangeListener {

    interface MapNameChoicePreferences {
        MapDisplayNameSource getDisplayNameSource();
    }

    private final MapNameChoicePreferences preferences;

    public MapDisplayNameRefreshCoordinator() {
        this(() -> ResourceController.getResourceController().getEnumProperty(
                MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY, MapDisplayNameSource.FILE_NAME));
    }

    MapDisplayNameRefreshCoordinator(MapNameChoicePreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void nodeChanged(NodeChangeEvent event) {
        Object property = event.getProperty();
        MapDisplayNameSource source = preferences.getDisplayNameSource();
        if (source == MapDisplayNameSource.VIEW_ROOT_TEXT && NodeModel.NODE_TEXT.equals(property)
                || source == MapDisplayNameSource.VIEW_ROOT_BOOKMARK && NodeBookmark.class.equals(property)) {
            event.getNode().getMap().fireMapChangeEvent(new MapChangeEvent(
                    this, event.getNode().getMap(), MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        }
    }
}
