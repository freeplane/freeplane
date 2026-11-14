package org.freeplane.view.swing.map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.MockedStatic;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.ui.IMapViewManager.MapChangeEventProperty;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.view.swing.ui.MapDisplayNameResolver;
import java.util.Arrays;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.features.map.MapModel;
import org.junit.Test;

public class MapViewControllerTitleTest extends MapViewTitleTestSupport {
    @Test
    public void frameTitleUsesResolvedLabelWithExistingContext() throws Exception {
        MapViewController manager = newViewController();
        MapModel map = newMap();
        MapView view = newView(map, "map");
        views(manager).add(view);
        field(manager, "selectedMapView", view);
        manager.propertyChanged("workspaceTitle", "", "");
        verify(display).setTitle("map - Mind map");
        setCustomName(view, "custom & *");
        map.setSaved(false);
        manager.propertyChanged("workspaceTitle", "", "");
        verify(display).setTitle("custom & * * - Mind map");
        map.setReadOnly(true);
        manager.propertyChanged("workspaceTitle", "", "");
        verify(display).setTitle("custom & * - Mind map (read_only)");
        when(mode.isEditingLocked()).thenReturn(true);
        manager.propertyChanged("workspaceTitle", "", "");
        verify(display).setTitle("custom & * - Mind map (OptionPanel.view_mode.true)");
        File file = new File("/tmp/日本語/map.mm");
        map.setURL(file.toURI().toURL());
        when(resources.getProperty("workspaceTitle")).thenReturn("Workspace");
        manager.propertyChanged("workspaceTitle", "", "");
        verify(display).setTitle(TextWritingDirection.LEFT_TO_RIGHT.isolatePathSeparators(
                "Workspace - custom & * - Mind map (OptionPanel.view_mode.true) " + file.getAbsolutePath()));
        field(manager, "selectedMapView", null);
        manager.propertyChanged("workspaceTitle", "", "");
        verify(display).setTitle("Workspace - Mind map");
        when(controller.getModeController()).thenReturn(null);
        manager.propertyChanged("workspaceTitle", "", "");
        verify(display).setTitle("");
    }

    @Test
    public void stringSelectionKeepsExistingKeysAndErrors() throws Exception {
        MapViewController manager = newViewController();
        MapView first = newView(newMap(), "same");
        MapView second = newView(newMap(), "same");
        MapView third = newView(newMap(), "same");
        addView(manager, first);
        addView(manager, second);
        addView(manager, third);
        assertEquals(Arrays.asList("same", "same<2>", "same<3>"), manager.getMapKeys());
        assertTrue(manager.changeToMapView("same<2>"));
        assertSame(second, manager.getMapViewComponent());
        assertFalse(manager.tryToChangeToMapView("not-a-key"));
        assertThrows(IllegalArgumentException.class, () -> manager.changeToMapView("not-a-key"));
        assertThrows(UnsupportedOperationException.class, () -> manager.getMapKeys().add("other"));
        assertThrows(UnsupportedOperationException.class, () -> manager.getMaps().clear());
        List<String> keys = manager.getMapKeys();
        java.util.Map<String, MapModel> maps = manager.getMaps();
        second.setName("script name");
        assertTrue(manager.changeToMapView("script name"));
        assertSame(second, manager.getMapViewComponent());
        assertSame(second.getMap(), manager.getMaps().get("script name"));
        assertEquals(Arrays.asList("same", "same<2>", "same<3>"), keys);
        assertSame(second.getMap(), maps.get("same<2>"));
        setCustomName(second, "display label");
        assertFalse(manager.tryToChangeToMapView("display label"));
        assertThrows(IllegalArgumentException.class, () -> manager.changeToMapView("display label"));
    }
    @Test
    public void onlyTheSelectedViewCanUpdateTheFrameTitle() throws Exception {
        MapViewController manager = newViewController();
        MapModel map = newMap();
        MapView selected = newView(map, "one");
        MapView sameMap = newView(map, "two");
        MapView unrelated = newView(newMap(), "other");
        addView(manager, selected);
        addView(manager, sameMap);
        addView(manager, unrelated);
        manager.changeToMapView(selected);
        clearInvocations(display, manager);
        map.setSaved(false);
        setCustomName(sameMap, "background");
        unrelated.refreshTitle();
        verifyNoInteractions(display);
        setCustomName(selected, "selected &");
        verify(display).setTitle("selected & * - Mind map");
        verify(manager, never()).getMapViews();
        verify(manager, never()).getViews(any(MapModel.class));
    }

    @Test
    public void selectionChangesMoveTheFrameSubscriptionOnce() throws Exception {
        MapViewController manager = newViewController();
        MapView first = newView(newMap(), "one");
        MapView second = newView(newMap(), "two");
        addView(manager, first);
        addView(manager, second);
        List<PropertyChangeEvent> firstTitles = recordTitleRequests(first);
        List<PropertyChangeEvent> secondTitles = recordTitleRequests(second);
        manager.changeToMapView(first);
        manager.changeToMapView(second);
        manager.afterViewChange(second, second);
        assertEquals(0, Arrays.stream(first.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY))
                .filter(listener -> listener == manager).count());
        assertEquals(1, Arrays.stream(second.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY))
                .filter(listener -> listener == manager).count());
        assertTrue(firstTitles.isEmpty());
        assertTrue(secondTitles.isEmpty());
        verify(input, never()).updateMapList();
        verify(display, atLeastOnce()).setTitle("two - Mind map");
    }

    @Test
    public void selectionNotifiesOnlyTheOldAndNewViews() throws Exception {
        MapViewController manager = newViewController();
        MapView first = newView(newMap(), "one");
        MapView second = newView(newMap(), "two");
        MapView third = newView(newMap(), "three");
        addView(manager, first);
        addView(manager, second);
        addView(manager, third);
        manager.changeToMapView(first);
        List<PropertyChangeEvent> firstSelection = new ArrayList<>();
        List<PropertyChangeEvent> secondSelection = new ArrayList<>();
        List<PropertyChangeEvent> thirdSelection = new ArrayList<>();
        first.addPropertyChangeListener(SelectableAction.SELECTION_PROPERTY, firstSelection::add);
        second.addPropertyChangeListener(SelectableAction.SELECTION_PROPERTY, secondSelection::add);
        third.addPropertyChangeListener(SelectableAction.SELECTION_PROPERTY, thirdSelection::add);
        clearInvocations(manager);
        manager.changeToMapView(second);
        manager.changeToMapView(second);
        assertEquals(1, firstSelection.size());
        assertEquals(Boolean.FALSE, firstSelection.get(0).getNewValue());
        assertNull(firstSelection.get(0).getOldValue());
        assertEquals(1, secondSelection.size());
        assertEquals(Boolean.TRUE, secondSelection.get(0).getNewValue());
        assertTrue(thirdSelection.isEmpty());
        verify(manager, never()).getMapViews();
        verify(manager, never()).getViews(any(MapModel.class));
    }

    @Test
    public void mapReplacementKeepsViewAndRebindsNotifications() throws Exception {
        MapViewController manager = newViewController();
        MapModel oldMap = newMap();
        MapModel replacement = newMap();
        replacement.setURL(new File("/tmp/replacement.mm").toURI().toURL());
        MapView view = newView(oldMap, "old");
        MapView unrelated = newView(newMap(), "other");
        view.setCustomizedTabNameSupplier(() -> "owner name");
        addView(manager, view);
        addView(manager, unrelated);
        manager.changeToMapView(view);
        List<String> order = new ArrayList<>();
        manager.addMapSelectionListener(new IMapSelectionListener() {
            @Override
            public void afterMapChange(MapModel old, MapModel current) {
                order.add("replacement");
            }
        });
        view.addPropertyChangeListener(MapView.TITLE_REFRESH_PROPERTY, event -> order.add("title"));
        List<PropertyChangeEvent> unrelatedEvents = recordTitleRequests(unrelated);
        try (MockedStatic<MapStyleModel> styles = mockStatic(MapStyleModel.class);
                MockedStatic<NodeViewFactory> nodes = mockStatic(NodeViewFactory.class)) {
            prepareReplacementRendering(view, replacement, styles, nodes);
            manager.setMap(view, replacement);
        }
        assertSame(view, manager.getMapViewComponent());
        assertSame(replacement, view.getMap());
        assertEquals("owner name", view.getCustomizedTabName());
        assertEquals(Arrays.asList("replacement", "title"), order);
        oldMap.fireMapChangeEvent(new MapChangeEvent(this, oldMap, MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        assertEquals(2, order.size());
        replacement.fireMapChangeEvent(new MapChangeEvent(this, replacement, MapChangeEventProperty.MAP_VIEW_TITLE, null, null, false));
        assertEquals(3, order.size());
        preferenceChanged(MapDisplayNameResolver.MAP_DISPLAY_NAME_MAX_LENGTH_PROPERTY);
        assertEquals(4, order.size());
        assertEquals(1, unrelatedEvents.size());
    }

    @Test
    public void mapReplacementRebuildsMenuOnlyWhenItsKeyChanges() throws Exception {
        MapViewController manager = newViewController();
        MapModel original = newMap();
        original.setURL(new File("/tmp/map.mm").toURI().toURL());
        MapView view = newView(original, "script key");
        MapView other = newView(original, "script key");
        addView(manager, view);
        addView(manager, other);
        assertEquals("script key<2>", other.getName());
        for (String path : Arrays.asList("/tmp/map.mm", "/tmp/renamed.mm")) {
            MapModel replacement = newMap();
            replacement.setURL(new File(path).toURI().toURL());
            clearInvocations(input);
            try (MockedStatic<MapStyleModel> styles = mockStatic(MapStyleModel.class);
                    MockedStatic<NodeViewFactory> nodes = mockStatic(NodeViewFactory.class)) {
                prepareReplacementRendering(view, replacement, styles, nodes);
                manager.setMap(view, replacement);
            }
            verify(input, times(path.equals("/tmp/map.mm") ? 0 : 1)).updateMapList();
            assertEquals(path.equals("/tmp/map.mm") ? "script key" : replacement.getTitle(), view.getName());
            assertEquals("script key<2>", other.getName());
        }
    }

    @Test
    public void viewNameUpdateDoesNotRefreshOtherDockingTitles() throws Exception {
        MapViewController manager = newViewController();
        MapModel map = newMap();
        map.setURL(new File("/tmp/renamed.mm").toURI().toURL());
        MapView view = newView(map, "old");
        MapView other = newView(map, "other");
        addView(manager, view);
        addView(manager, other);
        manager.changeToMapView(view);
        List<PropertyChangeEvent> sourceEvents = recordTitleRequests(view);
        List<PropertyChangeEvent> otherEvents = recordTitleRequests(other);
        manager.updateMapViewName();
        assertEquals(map.getTitle(), view.getName());
        assertEquals(1, sourceEvents.size());
        assertTrue(otherEvents.isEmpty());
        verify(input).updateMapList();
    }

    @Test
    public void closedViewRequestsDoNotChangeDisplays() throws Exception {
        MapViewController manager = newViewController();
        MapModel map = newMap();
        MapView view = newView(map, "closing");
        addView(manager, view);
        manager.changeToMapView(view);
        doAnswer(call -> { manager.onRemove(map); return null; }).when(mapController).closeWithoutSaving(map);
        assertTrue(manager.closeWithoutSaving(view));
        clearInvocations(display);
        setCustomName(view, "after close");
        view.refreshTitle();
        verifyNoInteractions(display);
        assertFalse(Arrays.asList(view.getPropertyChangeListeners(MapView.TITLE_REFRESH_PROPERTY)).contains(manager));
    }

    @Test
    public void urlSelectionUsesTheMatchingViewReference() throws Exception {
        MapViewController manager = newViewController();
        MapModel file = newMap();
        file.setURL(new File("/tmp/map.mm").toURI().toURL());
        MapModel remote = newMap();
        remote.setURL(new java.net.URL("https://example.org/map.mm"));
        MapView first = newView(file, "file key");
        MapView second = newView(remote, "remote key");
        addView(manager, first);
        addView(manager, second);
        assertTrue(manager.tryToChangeToMapView(file.getURL()));
        assertSame(first, manager.getMapViewComponent());
        assertTrue(manager.tryToChangeToMapView(file.getURL()));
        assertEquals("file key", manager.checkIfFileIsAlreadyOpened(file.getURL()));
        assertTrue(manager.tryToChangeToMapView(remote.getURL()));
        assertSame(second, manager.getMapViewComponent());
        assertFalse(manager.tryToChangeToMapView(new java.net.URL("https://example.org/missing.mm")));
        verify(manager, never()).changeToMapView(anyString());
        verify(manager, never()).tryToChangeToMapView(anyString());
    }


    @Test
    public void mapReplacementDoesNotFocusUnselectedViews() throws Exception {
        selectsReplacementRoot = true;
        MapView view = spy(newView(newMap(), "view"));
        MainView oldMain = mock(MainView.class);
        when(view.getRoot().getMainView()).thenReturn(oldMain);
        when(oldMain.getLocationOnScreen()).thenReturn(new java.awt.Point());
        doReturn(true).when(view).isShowing();
        field(view, "mapScroller", mock(MapScroller.class));
        doCallRealMethod().when(view).selectAsTheOnlyOneSelected(any(NodeView.class), anyBoolean());
        try (MockedStatic<MapStyleModel> styles = mockStatic(MapStyleModel.class);
                MockedStatic<NodeViewFactory> nodes = mockStatic(NodeViewFactory.class);
                MockedStatic<javax.swing.SwingUtilities> swing =
                        mockStatic(javax.swing.SwingUtilities.class, CALLS_REAL_METHODS)) {
            swing.when(() -> javax.swing.SwingUtilities.invokeLater(any(Runnable.class))).thenAnswer(call -> null);
            for (boolean selected : new boolean[]{false, true}) {
                when(viewManager.getMapViewComponent()).thenReturn(selected ? view : null);
                MapModel replacement = newMap();
                prepareReplacementRendering(view, replacement, styles, nodes);
                view.setMap(replacement);
                NodeView root = view.getRoot();
                verify(view).selectAsTheOnlyOneSelected(root, selected);
                verify(root, times(selected ? 1 : 0)).requestFocusInWindow();
                assertSame(root, view.getSelected());
                when(root.getMainView()).thenReturn(oldMain);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void mapReplacementKeepsExistingViewStateResets() throws Exception {
        selectsReplacementRoot = true;
        MapModel original = newMap();
        org.freeplane.features.map.NodeModel child = new org.freeplane.features.map.NodeModel("child", original);
        original.getRootNode().insert(child);
        MapView view = spy(newView(original, "view"));
        installNodeViews(view);
        changeViewRoot(view, child);
        java.util.List<org.freeplane.features.map.NodeModel> history =
                (java.util.List<org.freeplane.features.map.NodeModel>) field(view, "rootsHistory");
        history.add(child);
        field(view, "zoom", 3f);
        org.freeplane.features.filter.Filter filter = mock(org.freeplane.features.filter.Filter.class);
        field(view, "filter", filter);
        field(view, "mapScroller", mock(MapScroller.class));
        doCallRealMethod().when(view).selectAsTheOnlyOneSelected(any(NodeView.class), anyBoolean());
        MapModel replacement = newMap();
        try (MockedStatic<MapStyleModel> styles = mockStatic(MapStyleModel.class);
                MockedStatic<NodeViewFactory> nodes = mockStatic(NodeViewFactory.class)) {
            prepareReplacementRendering(view, replacement, styles, nodes);
            view.setMap(replacement);
        }
        assertSame(replacement.getRootNode(), view.getRoot().getNode());
        assertSame(view.getRoot(), view.getSelected());
        assertNotSame(filter, view.getFilter());
        assertNull(view.getFilter().getCondition());
        assertTrue(history.isEmpty());
        assertEquals(1f, view.getZoom(), 0f);
        assertNotSame(original.getRootNode(), view.getSelected().getNode());
    }
}
