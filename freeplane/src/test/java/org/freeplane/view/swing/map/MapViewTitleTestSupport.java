package org.freeplane.view.swing.map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Font;
import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.mockito.MockedStatic;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.util.Compat;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.view.swing.ui.MapDisplayNameResolver;
import org.freeplane.view.swing.ui.MapDisplayNameSource;
import org.junit.After;
import org.junit.Before;

public class MapViewTitleTestSupport {
    protected Controller controller;
    protected ResourceController resources;
    protected ModeController mode;
    protected MapController mapController;
    protected IMapViewManager viewManager;
    protected ViewController display;
    protected IUserInputListenerFactory input;
    protected TextController text;
    protected boolean selectsReplacementRoot;
    protected final List<IFreeplanePropertyListener> propertyListeners = new ArrayList<>();
    private Controller previousController;
    private List<PropertyChangeListener> previousFocusListeners;

    @Before
    public void setUpTitleServices() throws Exception {
        Compat.setIsApplet(false);
        previousController = Controller.getCurrentController();
        previousFocusListeners = Arrays.asList(KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .getPropertyChangeListeners("focusedWindow"));
        controller = mock(Controller.class);
        resources = mock(ResourceController.class);
        mode = mock(ModeController.class);
        mapController = mock(MapController.class);
        viewManager = mock(IMapViewManager.class);
        display = mock(ViewController.class);
        input = mock(IUserInputListenerFactory.class);
        text = mock(TextController.class);
        ResourceBundles bundles = mock(ResourceBundles.class);
        when(bundles.getResourceString(anyString())).thenAnswer(call -> call.getArgument(0));
        when(bundles.getResourceString(anyString(), nullable(String.class))).thenAnswer(call -> call.getArgument(1));
        when(resources.getText(anyString(), nullable(String.class))).thenAnswer(call -> call.getArgument(1));
        when(bundles.getResourceString("mode_title")).thenReturn("{0}");
        when(bundles.getResourceString("mode_MindMap")).thenReturn("Mind map");
        when(resources.getResources()).thenReturn(bundles);
        when(resources.getProperty(anyString())).thenReturn("");
        when(resources.getProperty(anyString(), nullable(String.class))).thenAnswer(call -> call.getArgument(1));
        when(resources.getProperty("icons.state")).thenReturn("knotes.svg;note_black_and_transp.svg");
        when(resources.getResourceStream(anyString()))
                .thenAnswer(call -> MapViewTitleTestSupport.class.getResourceAsStream(call.getArgument(0)));
        when(resources.getIntProperty(anyString(), anyInt())).thenAnswer(call -> call.getArgument(1));
        when(resources.getArrayProperty(anyString(), anyString())).thenReturn(new String[0]);
        when(resources.getArrayProperty("predefined_zoom_levels", ", *"))
                .thenReturn(new String[]{"50%", "100%", "200%"});
        when(resources.getColorProperty(anyString())).thenReturn(Color.BLACK);
        when(resources.getLengthProperty(anyString())).thenReturn(10);
        when(resources.getEnumProperty(MapDisplayNameResolver.MAP_DISPLAY_NAME_SOURCE_PROPERTY,
                MapDisplayNameSource.FILE_NAME)).thenReturn(MapDisplayNameSource.FILE_NAME);
        doAnswer(call -> { propertyListeners.add(call.getArgument(0)); return null; })
                .when(resources).addPropertyChangeListener(any(IFreeplanePropertyListener.class));
        doAnswer(call -> { propertyListeners.remove(call.getArgument(0)); return null; })
                .when(resources).removePropertyChangeListener(any(IFreeplanePropertyListener.class));
        when(controller.getResourceController()).thenReturn(resources);
        when(controller.getModeController()).thenReturn(mode);
        when(controller.getMapViewManager()).thenAnswer(call -> viewManager);
        when(controller.getViewController()).thenReturn(display);
        when(mode.getController()).thenReturn(controller);
        when(mode.getModeName()).thenReturn("MindMap");
        when(mode.getMapController()).thenReturn(mapController);
        when(mode.getUserInputListenerFactory()).thenReturn(input);
        when(mode.getExtension(TextController.class)).thenReturn(text);
        MapStyle style = mock(MapStyle.class);
        when(mode.getExtension(MapStyle.class)).thenReturn(style);
        when(style.getZoom(any(MapModel.class))).thenReturn(1f);
        when(style.getBackground(any(MapModel.class))).thenReturn(Color.BLACK);
        when(input.getMapMouseListener()).thenReturn(mock(IMouseListener.class));
        when(input.getMapMouseWheelListener()).thenReturn(new MouseAdapter() {});
        doAnswer(call -> {
            MapChangeEvent event = call.getArgument(0);
            event.getMap().fireMapChangeEvent(event);
            return null;
        }).when(mapController).fireMapChanged(any(MapChangeEvent.class));
        Controller.setCurrentController(controller);
    }

    @After
    public void restoreTitleServices() {
        KeyboardFocusManager focus = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        for (PropertyChangeListener listener : focus.getPropertyChangeListeners("focusedWindow"))
            if (!previousFocusListeners.contains(listener))
                focus.removePropertyChangeListener("focusedWindow", listener);
        Controller.setCurrentController(previousController);
    }

    protected List<MapChangeEvent> recordMapEvents(MapModel map) {
        List<MapChangeEvent> events = new ArrayList<>();
        map.addMapChangeListener(new IMapChangeListener() {
            @Override
            public void mapChanged(MapChangeEvent event) {
                events.add(event);
            }
        });
        return events;
    }

    protected List<PropertyChangeEvent> recordTitleRequests(MapView view) {
        List<PropertyChangeEvent> events = new ArrayList<>();
        view.addPropertyChangeListener(MapView.TITLE_REFRESH_PROPERTY, events::add);
        return events;
    }

    protected MapModel newMap() {
        MapModel map = new MapModel((source, target, children) -> null, null, null);
        map.setRoot(new NodeModel("root", map));
        map.setSaved(true);
        return map;
    }

    protected MapView newView(MapModel map, String name) {
        MapView view = new MapView(map, mode) {
            private boolean initialized;

            @Override
            public void setMap(MapModel map) {
                if (initialized)
                    super.setMap(map);
                initialized = true;
            }

            @Override
            public void selectAsTheOnlyOneSelected(NodeView node) {
                // Node focus/scrolling is not part of title notifications.
            }

            @Override
            public void selectAsTheOnlyOneSelected(NodeView node, boolean requestFocus) {
                if (selectsReplacementRoot)
                    super.selectAsTheOnlyOneSelected(node, requestFocus);
            }
        };
        NodeView root = mock(NodeView.class);
        when(root.getNode()).thenReturn(map.getRootNode());
        try {
            field(view, "currentRootView", root);
            field(view, "mapRootView", root);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        view.setName(name);
        map.addMapChangeListener(view);
        resources.addPropertyChangeListener(view);
        return view;
    }

    protected AutoCloseable prepareReplacementRendering(MapViewController manager) {
        MockedStatic<MapStyleModel> styles = mockStatic(MapStyleModel.class);
        MockedStatic<NodeViewFactory> nodes = mockStatic(NodeViewFactory.class);
        doAnswer(call -> {
            prepareReplacementRendering(call.getArgument(0), call.getArgument(1), styles, nodes);
            return call.callRealMethod();
        }).when(manager).setMap(any(java.awt.Component.class), any(MapModel.class));
        return () -> {
            nodes.close();
            styles.close();
        };
    }

    protected void prepareReplacementRendering(MapView view, MapModel map,
            MockedStatic<MapStyleModel> styles, MockedStatic<NodeViewFactory> nodes) {
        MapStyleModel styleModel = mock(MapStyleModel.class);
        styles.when(() -> MapStyleModel.getExtension(map)).thenReturn(styleModel);
        when(styleModel.getStyleNodeSafe(any())).thenReturn(map.getRootNode());
        when(styleModel.getZoom()).thenReturn(1f);
        when(styleModel.getMapViewLayout()).thenReturn(MapViewLayout.MAP);
        NodeStyleController nodeStyle = mock(NodeStyleController.class);
        when(mode.getExtension(NodeStyleController.class)).thenReturn(nodeStyle);
        when(nodeStyle.getFont(any(), any())).thenReturn(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        when(nodeStyle.getHorizontalTextAlignment(any(), any())).thenReturn(HorizontalTextAlignment.DEFAULT);
        when(resources.getProperty(MapView.RESOURCES_SELECTED_NODE_RECTANGLE_COLOR)).thenReturn("#000000");
        NodeViewFactory factory = mock(NodeViewFactory.class);
        nodes.when(NodeViewFactory::getInstance).thenReturn(factory);
        NodeView root = mock(NodeView.class);
        when(root.getNode()).thenReturn(map.getRootNode());
        when(factory.newNodeView(eq(map.getRootNode()), eq(view), eq(view), anyInt())).thenReturn(root);
    }


    protected void setCustomName(MapView view, String name) {
        view.setCustomizedTabNameSupplier(() -> name);
        view.refreshTitle();
    }

    protected void installNodeViews(MapView view) throws Exception {
        TestNodeView root = nodeViews(view.getMap().getRootNode(), view);
        field(view, "mapRootView", root);
        field(view, "currentRootView", root);
        field(view, "siblingMaxLevel", -1);
        view.add(root);
    }

    protected void changeViewRoot(MapView view, NodeModel node) {
        view.setRootNode(node);
    }

    private TestNodeView nodeViews(NodeModel node, MapView mapView) {
        TestNodeView view = new TestNodeView(node, mapView);
        node.addViewer(view);
        for (NodeModel child : node.getChildren())
            view.add(nodeViews(child, mapView));
        return view;
    }

    protected MapViewController newViewController() {
        MapViewController original = new MapViewController(controller);
        MapViewController manager = spy(original);
        manager.removeMapViewChangeListener(original);
        manager.addMapViewChangeListener(manager);
        resources.removePropertyChangeListener(original);
        resources.addPropertyChangeListener(manager);
        doNothing().when(manager).obtainFocusForSelected();
        viewManager = manager;
        return manager;
    }

    protected void addView(MapViewController manager, MapView view) throws Exception {
        Method method = MapViewController.class.getDeclaredMethod("addToOrChangeInMapViews", String.class, MapView.class);
        method.setAccessible(true);
        method.invoke(manager, view.getName(), view);
    }

    @SuppressWarnings("unchecked")
    protected Vector<MapView> views(MapViewController manager) throws Exception {
        return (Vector<MapView>) field(manager, "mapViewVector");
    }

    public static Object field(Object target, String name) throws Exception {
        return declaredField(target, name).get(target);
    }

    public static void field(Object target, String name, Object value) throws Exception {
        declaredField(target, name).set(target, value);
    }

    private static Field declaredField(Object target, String name) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    public static class TestNodeView extends NodeView {
        public TestNodeView(NodeModel viewedNode, MapView map) {
            super(viewedNode, map);
            setMainView(new MainView());
        }

        @Override
        public boolean isContentVisible() {
            return true;
        }

        @Override
        void updateIcons() {
        }

        @Override
        void fireFoldingChanged() {
        }

        @Override
        void resetLayoutPropertiesRecursively() {
        }
    }

    protected void preferenceChanged(String property) {
        for (IFreeplanePropertyListener listener : new ArrayList<>(propertyListeners))
            listener.propertyChanged(property, "", "");
    }
}
