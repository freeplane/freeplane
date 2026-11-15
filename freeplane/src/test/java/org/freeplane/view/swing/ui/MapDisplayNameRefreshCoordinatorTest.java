package org.freeplane.view.swing.ui;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.freeplane.features.bookmarks.mindmapmode.NodeBookmark;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;
import org.freeplane.view.swing.map.MapViewTitleTestSupport;
import org.junit.Test;

public class MapDisplayNameRefreshCoordinatorTest extends MapViewTitleTestSupport {
    @Test
    public void shouldRefreshNodeTextChangesOnlyInViewRootTextMode() {
        for (MapDisplayNameSource source : MapDisplayNameSource.values()) {
            MapModel map = newMap();
            List<MapChangeEvent> events = recordMapEvents(map);
            List<MapChangeEvent> unrelatedEvents = recordMapEvents(newMap());
            MapDisplayNameRefreshCoordinator coordinator = new MapDisplayNameRefreshCoordinator(() -> source);

            coordinator.nodeChanged(new NodeChangeEvent(
                    map.getRootNode(), NodeModel.NODE_TEXT, null, null, false, false));

            assertEquals(source == MapDisplayNameSource.VIEW_ROOT_TEXT ? 1 : 0, events.size());
            if (source == MapDisplayNameSource.VIEW_ROOT_TEXT)
                assertTitleRequest(map, coordinator, events.get(0));
            assertTrue(unrelatedEvents.isEmpty());
        }
        verifyNoInteractions(viewManager);
    }

    @Test
    public void shouldRefreshBookmarkChangesOnlyInViewRootBookmarkMode() {
        for (MapDisplayNameSource source : MapDisplayNameSource.values()) {
            MapModel map = newMap();
            List<MapChangeEvent> events = recordMapEvents(map);
            List<MapChangeEvent> unrelatedEvents = recordMapEvents(newMap());
            MapDisplayNameRefreshCoordinator coordinator = new MapDisplayNameRefreshCoordinator(() -> source);

            coordinator.nodeChanged(new NodeChangeEvent(
                    map.getRootNode(), NodeBookmark.class, null, null, false, false));

            assertEquals(source == MapDisplayNameSource.VIEW_ROOT_BOOKMARK ? 1 : 0, events.size());
            if (source == MapDisplayNameSource.VIEW_ROOT_BOOKMARK)
                assertTitleRequest(map, coordinator, events.get(0));
            assertTrue(unrelatedEvents.isEmpty());
        }
        verifyNoInteractions(viewManager);
    }

    @Test
    public void irrelevantNodeChangesDoNotPublishTitleRequests() {
        for (MapDisplayNameSource source : MapDisplayNameSource.values()) {
            MapModel map = newMap();
            List<MapChangeEvent> events = recordMapEvents(map);

            new MapDisplayNameRefreshCoordinator(() -> source).nodeChanged(
                    new NodeChangeEvent(map.getRootNode(), "unrelated", null, null, false, false));

            assertTrue(events.isEmpty());
        }
        verifyNoInteractions(viewManager);
    }

    private void assertTitleRequest(MapModel map, MapDisplayNameRefreshCoordinator coordinator, MapChangeEvent event) {
        assertSame(map, event.getMap());
        assertSame(coordinator, event.getSource());
        assertSame(MapChangeEventProperty.MAP_VIEW_TITLE, event.getProperty());
        assertNull(event.getOldValue());
        assertNull(event.getNewValue());
        assertFalse(event.setsDirtyFlag());
        assertTrue(map.isSaved());
    }
}
