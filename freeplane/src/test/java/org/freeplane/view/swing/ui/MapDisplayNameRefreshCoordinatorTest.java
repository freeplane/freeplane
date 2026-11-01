package org.freeplane.view.swing.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.bookmarks.mindmapmode.MapBookmarks;
import org.freeplane.features.bookmarks.mindmapmode.NodeBookmark;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.junit.Test;

public class MapDisplayNameRefreshCoordinatorTest {

    @Test
    public void shouldRefreshForRelevantPropertyChangesOnly() {
        MutablePreferences preferences = new MutablePreferences(MapDisplayNameSource.FILE_NAME);
        RecordingRefresher refresher = new RecordingRefresher();

        MapDisplayNameRefreshCoordinator coordinator = newCoordinator(preferences, refresher);

        coordinator.propertyChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY, "a", "b");
        coordinator.propertyChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY, "1", "2");
        coordinator.propertyChanged("other", "1", "2");

        assertThat(refresher.callCount).isEqualTo(2);
    }

    @Test
    public void shouldRefreshForRelevantMapChangesOnly() {
        MutablePreferences preferences = new MutablePreferences(MapDisplayNameSource.FILE_NAME);
        RecordingRefresher refresher = new RecordingRefresher();

        MapDisplayNameRefreshCoordinator coordinator = newCoordinator(preferences, refresher);

        coordinator.mapChanged(new MapChangeEvent(this, IMapViewManager.MapChangeEventProperty.MAP_VIEW_ROOT, null, null));
        coordinator.mapChanged(new MapChangeEvent(this, MapBookmarks.class, null, null));
        coordinator.mapChanged(new MapChangeEvent(this, UrlManager.MAP_URL, null, null));
        coordinator.mapChanged(new MapChangeEvent(this, "other", null, null));

        assertThat(refresher.callCount).isEqualTo(3);
    }

    @Test
    public void shouldRefreshNodeTextChangesOnlyInViewRootTextMode() {
        MutablePreferences preferences = new MutablePreferences(MapDisplayNameSource.VIEW_ROOT_TEXT);
        RecordingRefresher refresher = new RecordingRefresher();

        MapDisplayNameRefreshCoordinator coordinator = newCoordinator(preferences, refresher);
        NodeModel node = createNodeModel();

        coordinator.nodeChanged(new NodeChangeEvent(node, NodeModel.NODE_TEXT, null, null, false, false));
        coordinator.nodeChanged(new NodeChangeEvent(node, NodeBookmark.class, null, null, false, false));

        assertThat(refresher.callCount).isEqualTo(1);
    }

    @Test
    public void shouldRefreshBookmarkChangesOnlyInViewRootBookmarkMode() {
        MutablePreferences preferences = new MutablePreferences(MapDisplayNameSource.VIEW_ROOT_BOOKMARK);
        RecordingRefresher refresher = new RecordingRefresher();

        MapDisplayNameRefreshCoordinator coordinator = newCoordinator(preferences, refresher);
        NodeModel node = createNodeModel();

        coordinator.nodeChanged(new NodeChangeEvent(node, NodeBookmark.class, null, null, false, false));
        coordinator.nodeChanged(new NodeChangeEvent(node, NodeModel.NODE_TEXT, null, null, false, false));

        assertThat(refresher.callCount).isEqualTo(1);
    }

    @Test
    public void shouldRefreshStructuralNodeEventsOnlyForAncestorDependentSources() {
        NodeModel node = createNodeModel();
        NodeDeletionEvent deletionEvent = new NodeDeletionEvent(node, node, 0);
        NodeMoveEvent moveEvent = new NodeMoveEvent(node, 0, node, node, 0);

        MutablePreferences fileNamePreferences = new MutablePreferences(MapDisplayNameSource.FILE_NAME);
        RecordingRefresher fileNameRefresher = new RecordingRefresher();
        MapDisplayNameRefreshCoordinator fileNameCoordinator = newCoordinator(fileNamePreferences, fileNameRefresher);

        fileNameCoordinator.onNodeInserted(node, node, 0);
        fileNameCoordinator.onNodeMoved(moveEvent);
        fileNameCoordinator.onNodeDeleted(deletionEvent);

        MutablePreferences rootPreferences = new MutablePreferences(MapDisplayNameSource.VIEW_ROOT_TEXT);
        RecordingRefresher rootRefresher = new RecordingRefresher();
        MapDisplayNameRefreshCoordinator rootCoordinator = newCoordinator(rootPreferences, rootRefresher);

        rootCoordinator.onNodeInserted(node, node, 0);
        rootCoordinator.onNodeMoved(moveEvent);
        rootCoordinator.onNodeDeleted(deletionEvent);

        assertThat(fileNameRefresher.callCount).isZero();
        assertThat(rootRefresher.callCount).isEqualTo(3);
    }

    private MapDisplayNameRefreshCoordinator newCoordinator(MutablePreferences preferences,
                                                            RecordingRefresher refresher) {
        return new MapDisplayNameRefreshCoordinator(preferences,
                refresher,
                listener -> {
                });
    }

    private NodeModel createNodeModel() {
        return new NodeModel(new MapModel(null, null, null));
    }

    private static class MutablePreferences implements MapDisplayNameRefreshCoordinator.MapNameChoicePreferences {
        private final MapDisplayNameSource source;

        private MutablePreferences(MapDisplayNameSource source) {
            this.source = source;
        }

        @Override
        public MapDisplayNameSource getDisplayNameSource() {
            return source;
        }
    }

    private static class RecordingRefresher implements MapDisplayNameRefreshCoordinator.MapTitlesRefresher {
        private int callCount;

        @Override
        public void setMapTitles() {
            callCount++;
        }
    }

}
