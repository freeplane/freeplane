package org.freeplane.view.swing.map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeGeometryModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleShape;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.junit.Test;
import org.mockito.MockedStatic;

public class MapViewRootChangeTest {

    @Test
    public void setRootNodeUnfoldsNodeInModel() {
        withMapView((view, target) -> {
            target.setFolded(true);

            view.setRootNode(target);

            assertThat(target.isFolded(), equalTo(false));
        });
    }

    @Test
    public void setRootNodeKeepsAlreadyUnfoldedNode() {
        withMapView((view, target) -> {
            target.setFolded(false);

            view.setRootNode(target);

            assertThat(target.isFolded(), equalTo(false));
        });
    }

    @Test
    public void setRootNodeUnfoldsFoldedAncestors() {
        withMapView((view, target) -> {
            final NodeModel bbb = target.getParentNode();
            final NodeModel aaa = bbb.getParentNode();
            target.setFolded(true);
            bbb.setFolded(true);
            aaa.setFolded(true);

            view.setRootNode(target);

            assertThat(bbb.isFolded(), equalTo(false));
            assertThat(aaa.isFolded(), equalTo(false));
        });
    }

    private interface MapViewScenario {
        void run(MapView view, NodeModel target);
    }

    private void withMapView(MapViewScenario scenario) {
        Compat.setIsApplet(false);
        ResourceController resourceController = mock(ResourceController.class);
        when(resourceController.getProperty(anyString())).thenAnswer(invocation ->
                ((String) invocation.getArgument(0)).toLowerCase().contains("color") ? "#ff000000" : "false");
        when(resourceController.getProperty(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(resourceController.getBooleanProperty(anyString())).thenReturn(false);
        when(resourceController.getIntProperty(anyString(), anyInt())).thenAnswer(invocation -> invocation.getArgument(1));
        when(resourceController.getIntProperty(anyString())).thenReturn(0);
        when(resourceController.getColorProperty(anyString())).thenReturn(Color.BLACK);
        when(resourceController.getLengthProperty(anyString())).thenReturn(0);
        when(resourceController.getDoubleProperty(anyString(), anyInt())).thenAnswer(invocation -> (double) (int) invocation.getArgument(1));
        when(resourceController.getArrayProperty(anyString(), anyString())).thenReturn(new String[0]);
        when(resourceController.getFreeplaneUserDirectory()).thenReturn(System.getProperty("java.io.tmpdir"));
        when(resourceController.getLengthQuantityProperty(anyString()))
                .thenReturn(new org.freeplane.api.Quantity<>(16, org.freeplane.api.LengthUnit.px));
        when(resourceController.getResource(anyString()))
                .thenAnswer(invocation -> MapViewRootChangeTest.class.getResource(invocation.getArgument(0)));
        try {
            when(resourceController.getResourceStream(anyString()))
                    .thenAnswer(invocation -> MapViewRootChangeTest.class.getResourceAsStream(invocation.getArgument(0)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        ResourceBundles resourceBundles = mock(ResourceBundles.class);
        when(resourceBundles.getResourceString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(resourceBundles.getResourceString(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(resourceController.getResources()).thenReturn(resourceBundles);

        try (MockedStatic<ResourceController> resourceControllers = mockStatic(ResourceController.class)) {
            resourceControllers.when(ResourceController::getResourceController).thenReturn(resourceController);

            ModeController modeController = mock(ModeController.class, RETURNS_DEEP_STUBS);
            Controller controller = mock(Controller.class, RETURNS_DEEP_STUBS);
            when(controller.getModeController()).thenReturn(modeController);
            Controller.setCurrentController(controller);

            Map<Class<?>, Object> extensions = new HashMap<>();
            NodeStyleController nodeStyleController = mock(NodeStyleController.class);
            when(nodeStyleController.getFont(any(), any())).thenReturn(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            when(nodeStyleController.getHorizontalTextAlignment(any(), any())).thenReturn(HorizontalTextAlignment.DEFAULT);
            when(nodeStyleController.getShapeConfiguration(any(), any())).thenReturn(NodeGeometryModel.FORK);
            when(nodeStyleController.getShape(any(), any())).thenReturn(NodeStyleShape.fork);
            extensions.put(NodeStyleController.class, nodeStyleController);
            MapStyle mapStyle = mock(MapStyle.class);
            when(mapStyle.getBackground(any())).thenReturn(Color.WHITE);
            extensions.put(MapStyle.class, mapStyle);
            when(modeController.getExtension(org.mockito.ArgumentMatchers.<Class<IExtension>>any()))
                    .thenAnswer(invocation -> extensions.computeIfAbsent(invocation.getArgument(0), c -> mock((Class<?>) c)));

            MapController mapController = modeController.getMapController();
            doAnswer(invocation -> {
                NodeModel node = invocation.getArgument(0);
                node.setFolded(invocation.getArgument(1));
                return null;
            }).when(mapController).setFolded(any(), anyBoolean(), any());

            MapFake mapFake = new MapFake();
            NodeModel aaa = mapFake.createNode("aaa");
            NodeModel bbb = mapFake.createNode("bbb");
            NodeModel target = mapFake.createNode("target");
            mapFake.getRoot().insert(aaa);
            aaa.insert(bbb);
            bbb.insert(target);
            MapModel map = mapFake.getRoot().getMap();
            MapStyleModel mapStyleModel = mock(MapStyleModel.class, RETURNS_DEEP_STUBS);
            when(mapStyleModel.getZoom()).thenReturn(1f);
            when(mapStyleModel.getMapViewLayout()).thenReturn(MapViewLayout.MAP);
            when(map.getExtension(MapStyleModel.class)).thenReturn(mapStyleModel);

            MapView view = new MapView(map, modeController);

            scenario.run(view, target);
        }
        finally {
            Controller.setCurrentController(null);
        }
    }
}
