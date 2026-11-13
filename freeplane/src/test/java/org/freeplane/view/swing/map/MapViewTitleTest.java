package org.freeplane.view.swing.map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.beans.PropertyChangeEvent;
import java.util.List;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.bookmarks.mindmapmode.MapBookmarks;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.ui.MapDisplayNameResolver;
import org.freeplane.view.swing.ui.MapDisplayNameSource;
import org.junit.Test;

public class MapViewTitleTest extends MapViewTitleTestSupport {
    @Test
    public void titleRequestIdentifiesItsViewWithoutCopiedDisplayState() {
        MapView view = newView(newMap(), "map");
        List<PropertyChangeEvent> requests = recordTitleRequests(view);
        view.refreshTitle();
        assertEquals(1, requests.size());
        PropertyChangeEvent event = requests.get(0);
        assertSame(view, event.getSource());
        assertEquals("mapViewTitleRefresh", event.getPropertyName());
        assertNull(event.getOldValue());
        assertNull(event.getNewValue());
    }

    @Test
    public void viewRootChangeNotifiesOnlyItsSourceView() throws Exception {
        MapModel map = newMap();
        NodeModel detached = new NodeModel("detached", map);
        map.getRootNode().insert(detached);
        NodeModel child = new NodeModel("child", map);
        detached.insert(child);
        MapView source = newView(map, "source");
        MapView other = newView(map, "same map");
        MapView unrelated = newView(newMap(), "other map");
        TestNodeView rootView = new TestNodeView(map.getRootNode(), source);
        TestNodeView detachedView = new TestNodeView(detached, source);
        TestNodeView childView = new TestNodeView(child, source);
        detachedView.add(childView);
        map.getRootNode().addViewer(rootView);
        detached.addViewer(detachedView);
        child.addViewer(childView);
        field(source, "mapRootView", rootView);
        field(source, "currentRootView", detachedView);
        field(source, "siblingMaxLevel", -1);
        source.add(detachedView);
        List<PropertyChangeEvent> sourceEvents = recordTitleRequests(source);
        List<PropertyChangeEvent> otherEvents = recordTitleRequests(other);
        List<PropertyChangeEvent> unrelatedEvents = recordTitleRequests(unrelated);

        source.setRootNode(child);

        assertSame(childView, source.getRoot());
        assertEquals(1, sourceEvents.size());
        assertTrue(otherEvents.isEmpty());
        assertTrue(unrelatedEvents.isEmpty());
        assertFalse(source.isSelected());
    }

    @Test
    public void sharedMapChangesNotifyOnlySubscribedViews() {
        MapModel map = newMap();
        MapView first = newView(map, "one");
        MapView second = newView(map, "two");
        MapView unrelated = newView(newMap(), "unrelated");
        List<PropertyChangeEvent> firstEvents = recordTitleRequests(first);
        List<PropertyChangeEvent> secondEvents = recordTitleRequests(second);
        List<PropertyChangeEvent> unrelatedEvents = recordTitleRequests(unrelated);
        for (Object property : new Object[]{UrlManager.MAP_URL, MapBookmarks.class, MapChangeEventProperty.MAP_VIEW_TITLE})
            map.fireMapChangeEvent(new MapChangeEvent(this, map, property, null, null, false));
        assertEquals(3, firstEvents.size());
        assertEquals(3, secondEvents.size());
        assertTrue(unrelatedEvents.isEmpty());
        verify(viewManager, never()).getViews(any(MapModel.class));
        verify(viewManager, never()).getMapViews();
    }

    @Test
    public void labelPreferencesNotifyRegisteredViewsDirectly() throws Exception {
        MapView first = newView(newMap(), "one");
        MapView second = newView(newMap(), "two");
        when(viewManager.getMapViewComponent()).thenReturn(first);
        IFreeplanePropertyListener shared = (IFreeplanePropertyListener) field(first, "propertyChangeListener");
        if (!propertyListeners.contains(shared))
            propertyListeners.add(shared);
        List<PropertyChangeEvent> firstEvents = recordTitleRequests(first);
        List<PropertyChangeEvent> secondEvents = recordTitleRequests(second);
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY);
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY);
        assertEquals(2, firstEvents.size());
        assertEquals(2, secondEvents.size());
        verify(viewManager, never()).getViews(any(MapModel.class));
        verify(viewManager, never()).getMapViews();
        preferenceChanged("unrelated");
        assertEquals(2, firstEvents.size());
        assertEquals(2, secondEvents.size());
    }

    @Test
    public void customNameChangeNotifiesOnlyItsView() {
        MapModel map = newMap();
        MapView first = newView(map, "one");
        MapView second = newView(map, "two");
        List<PropertyChangeEvent> firstEvents = recordTitleRequests(first);
        List<PropertyChangeEvent> secondEvents = recordTitleRequests(second);
        first.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, "custom");
        first.putClientProperty(MapDisplayNameResolver.CUSTOMIZED_TAB_NAME_PROPERTY, new String("custom"));
        assertEquals(1, firstEvents.size());
        assertTrue(secondEvents.isEmpty());
    }

    @Test
    public void structuralChangesNotifyOnlyViewsUsingAncestorSources() {
        for (MapDisplayNameSource source : MapDisplayNameSource.values()) {
            when(resources.getEnumProperty(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                    MapDisplayNameSource.FILE_NAME)).thenReturn(source);
            MapModel map = newMap();
            MapView first = newView(map, "one");
            MapView second = newView(map, "two");
            MapView unrelated = newView(newMap(), "other");
            NodeModel parent = map.getRootNode();
            NodeModel child = new NodeModel("child", map);
            parent.insert(child);
            List<PropertyChangeEvent> firstEvents = recordTitleRequests(first);
            List<PropertyChangeEvent> secondEvents = recordTitleRequests(second);
            List<PropertyChangeEvent> otherEvents = recordTitleRequests(unrelated);
            map.fireNodeInsertionEvent(parent, child, 0);
            parent.remove(0);
            map.fireNodeDeletionEvent(new NodeDeletionEvent(parent, child, 0));
            assertNull(child.getParentNode());
            map.fireNodeMovedEvent(new NodeMoveEvent(parent, 0, child, parent, 0));
            int expected = source == MapDisplayNameSource.FILE_NAME ? 0 : 3;
            assertEquals(expected, firstEvents.size());
            assertEquals(expected, secondEvents.size());
            assertTrue(otherEvents.isEmpty());
        }
        verify(viewManager, never()).getViews(any(MapModel.class));
        verify(viewManager, never()).getMapViews();
    }

    @Test
    public void closingViewsRemovesMapAndPreferenceSubscriptions() throws Exception {
        MapViewController manager = newViewController();
        MapModel map = newMap();
        MapView first = newView(map, "one");
        MapView second = newView(map, "two");
        addView(manager, first);
        addView(manager, second);
        List<PropertyChangeEvent> firstEvents = recordTitleRequests(first);
        List<PropertyChangeEvent> secondEvents = recordTitleRequests(second);
        assertTrue(manager.closeWithoutSaving(first));
        map.fireMapChangeEvent(new MapChangeEvent(this, map, MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY);
        assertTrue(firstEvents.isEmpty());
        assertEquals(2, secondEvents.size());
        assertFalse(propertyListeners.contains(first));
        doAnswer(call -> { manager.onRemove(map); return null; }).when(mapController).closeWithoutSaving(map);
        assertTrue(manager.closeWithoutSaving(second));
        map.fireMapChangeEvent(new MapChangeEvent(this, map, MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY);
        assertEquals(2, secondEvents.size());
        assertFalse(propertyListeners.contains(second));
        assertTrue(manager.getMapViews().isEmpty());
    }
}
