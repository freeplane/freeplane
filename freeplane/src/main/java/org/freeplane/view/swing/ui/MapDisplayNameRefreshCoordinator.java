package org.freeplane.view.swing.ui;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.bookmarks.mindmapmode.MapBookmarks;
import org.freeplane.features.bookmarks.mindmapmode.NodeBookmark;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;

public class MapDisplayNameRefreshCoordinator implements IMapChangeListener, INodeChangeListener, IFreeplanePropertyListener {

    interface MapNameChoicePreferences {
        MapDisplayNameSource getDisplayNameSource();
    }

    interface MapTitlesRefresher {
        void setMapTitles();
    }

    interface PropertyListenerRegistrar {
        void addPropertyChangeListener(IFreeplanePropertyListener listener);
    }

    private static final class ResourceMapNameChoicePreferences implements MapNameChoicePreferences {
        @Override
        public MapDisplayNameSource getDisplayNameSource() {
            return ResourceController.getResourceController().getEnumProperty(
                    MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                    MapDisplayNameSource.FILE_NAME);
        }
    }

    private static final class ControllerMapTitlesRefresher implements MapTitlesRefresher {
        @Override
        public void setMapTitles() {
            Controller.getCurrentController().getMapViewManager().setMapTitles();
        }
    }

    private static final class ResourcePropertyListenerRegistrar implements PropertyListenerRegistrar {
        @Override
        public void addPropertyChangeListener(IFreeplanePropertyListener listener) {
            ResourceController.getResourceController().addPropertyChangeListener(listener);
        }
    }

    private final MapNameChoicePreferences mapNameChoicePreferences;
    private final MapTitlesRefresher mapTitlesRefresher;

    public MapDisplayNameRefreshCoordinator() {
        this(new ResourceMapNameChoicePreferences(), new ControllerMapTitlesRefresher(),
                new ResourcePropertyListenerRegistrar());
    }

    MapDisplayNameRefreshCoordinator(MapNameChoicePreferences mapNameChoicePreferences,
                                     MapTitlesRefresher mapTitlesRefresher,
                                     PropertyListenerRegistrar propertyListenerRegistrar) {
        this.mapNameChoicePreferences = mapNameChoicePreferences;
        this.mapTitlesRefresher = mapTitlesRefresher;
        if (propertyListenerRegistrar != null) {
            propertyListenerRegistrar.addPropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if (MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY.equals(propertyName)
                || MapDisplayNameResolver.MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY.equals(propertyName)) {
            refreshMapTitles();
        }
    }

    @Override
    public void mapChanged(MapChangeEvent event) {
        Object property = event.getProperty();
        if (IMapViewManager.MapChangeEventProperty.MAP_VIEW_ROOT.equals(property)
                || MapBookmarks.class.equals(property)
                || UrlManager.MAP_URL.equals(property)) {
            refreshMapTitles();
        }
    }

    @Override
    public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
        if (isAncestorDependentSource()) {
            refreshMapTitles();
        }
    }

    @Override
    public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
        if (isAncestorDependentSource()) {
            refreshMapTitles();
        }
    }

    @Override
    public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
        if (isAncestorDependentSource()) {
            refreshMapTitles();
        }
    }

    @Override
    public void nodeChanged(NodeChangeEvent event) {
        Object property = event.getProperty();
        MapDisplayNameSource source = getDisplayNameSource();
        if (source == MapDisplayNameSource.VIEW_ROOT_TEXT
                && NodeModel.NODE_TEXT.equals(property)) {
            refreshMapTitles();
        }
        else if (source == MapDisplayNameSource.VIEW_ROOT_BOOKMARK
                && NodeBookmark.class.equals(property)) {
            refreshMapTitles();
        }
    }

    private MapDisplayNameSource getDisplayNameSource() {
        return mapNameChoicePreferences.getDisplayNameSource();
    }

    private boolean isAncestorDependentSource() {
        return getDisplayNameSource() != MapDisplayNameSource.FILE_NAME;
    }

    private void refreshMapTitles() {
        mapTitlesRefresher.setMapTitles();
    }
}
