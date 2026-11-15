package org.freeplane.view.swing.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.IMapViewManager;
import org.junit.After;
import org.junit.Before;
import org.freeplane.view.swing.map.MapViewTitleTestSupport.TestNodeView;
import org.junit.Test;

public class MapViewRootLifecycleTest {
    private Controller previousController;
    private Controller controller;
    private ModeController modeController;

    @Before
    public void setUp() {
        previousController = Controller.getCurrentController();
        controller = mock(Controller.class);
        ResourceController resourceController = mock(ResourceController.class);
        IMapViewManager mapViewManager = mock(IMapViewManager.class);
        modeController = mock(ModeController.class);
        MapController mapController = mock(MapController.class);
        IUserInputListenerFactory userInputListenerFactory = mock(IUserInputListenerFactory.class);
        IMouseListener mapMouseListener = mock(IMouseListener.class);
        TextController textController = mock(TextController.class);

        when(controller.getResourceController()).thenReturn(resourceController);
        when(controller.getMapViewManager()).thenReturn(mapViewManager);
        when(controller.getModeController()).thenReturn(modeController);
        when(mapViewManager.getMapViewComponent()).thenReturn(null);
        when(modeController.getMapController()).thenReturn(mapController);
        when(modeController.getUserInputListenerFactory()).thenReturn(userInputListenerFactory);
        when(modeController.getExtension(TextController.class)).thenReturn(textController);
        when(modeController.canEdit(any(NodeModel.class))).thenReturn(false);
        when(mapController.isFolded(any(NodeModel.class))).thenReturn(false);
        when(userInputListenerFactory.getMapMouseListener()).thenReturn(mapMouseListener);
        when(userInputListenerFactory.getMapMouseWheelListener()).thenReturn(new MouseAdapter() {
        });
        when(resourceController.getProperty(anyString())).thenReturn("");
        when(resourceController.getBooleanProperty(anyString())).thenReturn(false);
        when(resourceController.getIntProperty(anyString(), anyInt())).thenAnswer(invocation -> invocation.getArgument(1));
        when(resourceController.getColorProperty(anyString())).thenReturn(Color.BLACK);
        when(resourceController.getLengthProperty(anyString())).thenReturn(10);
        when(textController.getNodeNumbering(any(NodeModel.class))).thenReturn(false);

        Controller.setCurrentController(controller);
    }

    @After
    public void tearDown() {
        Controller.setCurrentController(previousController);
    }

    @Test
    public void keepsDescendantViewerRegisteredWhenJumpingIntoChildOfDetachedRoot() throws Exception {
        MapModel map = new MapModel((source, targetMap, withChildren) -> null, null, null);
        NodeModel root = new NodeModel("root", map);
        map.setRoot(root);
        NodeModel detachedRootNode = new NodeModel("detached-root", map);
        root.insert(detachedRootNode);
        NodeModel descendantNode = new NodeModel("descendant", map);
        detachedRootNode.insert(descendantNode);

        MapView mapView = new MapView(map, modeController) {
            @Override
            public void setMap(final MapModel viewedMap) {
            }
        };
        setField(mapView, "siblingMaxLevel", -1);

        TestNodeView mapRootView = new TestNodeView(root, mapView);
        TestNodeView detachedRootView = new TestNodeView(detachedRootNode, mapView);
        TestNodeView descendantView = new TestNodeView(descendantNode, mapView);
        detachedRootView.add(descendantView);

        root.addViewer(mapRootView);
        detachedRootNode.addViewer(detachedRootView);
        descendantNode.addViewer(descendantView);

        setField(mapView, "mapRootView", mapRootView);
        setField(mapView, "currentRootView", detachedRootView);
        setField(mapView, "currentRootParentView", null);
        mapView.add(detachedRootView);

        mapView.setRootNode(descendantNode);

        assertSame(descendantView, mapView.getRoot());
        assertSame(detachedRootView, getField(mapView, "currentRootParentView"));
        assertNull(detachedRootView.getParent());
        assertSame(mapView, descendantView.getParent());
        assertTrue(descendantNode.getViewers().contains(descendantView));
        assertEquals(1, countView(descendantNode, descendantView));
        assertTrue(detachedRootNode.getViewers().contains(detachedRootView));

        @SuppressWarnings("unchecked")
        List<NodeModel> rootsHistory = (List<NodeModel>) getField(mapView, "rootsHistory");
        assertEquals(1, rootsHistory.size());
        assertSame(detachedRootNode, rootsHistory.get(0));
    }

    private static int countView(NodeModel node, NodeView expectedView) {
        return (int) new ArrayList<>(node.getViewers()).stream().filter(view -> view == expectedView).count();
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = MapView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = MapView.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
