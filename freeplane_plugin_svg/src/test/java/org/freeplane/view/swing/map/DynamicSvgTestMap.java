package org.freeplane.view.swing.map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.filechooser.FileFilter;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.export.mindmapmode.IExportEngine;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.link.ConnectorArrows;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.ConnectorShape;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/** Native Swing content with the same controller fixture pattern as core map-view tests. */
public final class DynamicSvgTestMap implements AutoCloseable {
    public final Controller controller = mock(Controller.class);
    public final ResourceController resources = mock(ResourceController.class);
    public final ModeController mode = mock(ModeController.class);
    public final LinkController links = mock(LinkController.class);
    public final ExportController exports = mock(ExportController.class);
    public final ViewController display = mock(ViewController.class);
    public final MapModel model;
    public final MapView view;
    private final Controller previous;
    private final List<PropertyChangeListener> previousFocusListeners;
    private final Map<NodeModel, NativeNodeView> views = new IdentityHashMap<>();

    public DynamicSvgTestMap() throws Exception {
        previous = Controller.getCurrentController();
        previousFocusListeners = Arrays.asList(KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .getPropertyChangeListeners("focusedWindow"));
        ResourceBundles bundles = mock(ResourceBundles.class);
        Properties english = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/translations/Resources_en.properties")) {
            english.load(input);
        }
        when(bundles.getResourceString(anyString())).thenAnswer(call -> english.getProperty(call.getArgument(0), call.getArgument(0)));
        when(bundles.getResourceString(anyString(), nullable(String.class))).thenAnswer(call -> english.getProperty(call.getArgument(0), call.getArgument(1)));
        when(resources.getResources()).thenReturn(bundles);
        when(resources.getProperty(anyString())).thenReturn("");
        when(resources.getProperty(anyString(), nullable(String.class))).thenAnswer(call -> call.getArgument(1));
        when(resources.getResourceStream(anyString())).thenAnswer(call -> getClass().getResourceAsStream(call.getArgument(0)));
        when(resources.getIntProperty(anyString(), anyInt())).thenAnswer(call -> call.getArgument(1));
        when(resources.getColorProperty(anyString())).thenReturn(Color.BLACK);
        when(resources.getLengthProperty(anyString())).thenReturn(10);
        when(controller.getResourceController()).thenReturn(resources);
        when(controller.getModeController()).thenReturn(mode);
        FilterController filters = mock(FilterController.class);
        when(controller.getExtension(FilterController.class)).thenReturn(filters);
        when(controller.getViewController()).thenReturn(display);
        IMapViewManager manager = mock(IMapViewManager.class);
        when(controller.getMapViewManager()).thenReturn(manager);
        when(mode.getController()).thenReturn(controller);
        when(mode.getModeName()).thenReturn("MindMap");
        when(mode.getExtension(ExportController.class)).thenReturn(exports);
        when(mode.getExtension(LinkController.class)).thenReturn(links);
        MapController maps = mock(MapController.class);
        when(mode.getMapController()).thenReturn(maps);
        when(maps.isFolded(any(NodeModel.class))).thenAnswer(call -> ((NodeModel) call.getArgument(0)).isFolded());
        TextController text = mock(TextController.class);
        when(mode.getExtension(TextController.class)).thenReturn(text);
        when(text.getPlainTransformedTextWithoutNodeNumber(any(NodeModel.class))).thenAnswer(call -> call.getArgument(0).toString());
        LayoutController layout = mock(LayoutController.class);
        when(mode.getExtension(LayoutController.class)).thenReturn(layout);
        when(layout.getEffectiveChildNodesLayout(any(NodeModel.class))).thenReturn(ChildNodesLayout.AUTO);
        IUserInputListenerFactory input = mock(IUserInputListenerFactory.class);
        when(mode.getUserInputListenerFactory()).thenReturn(input);
        when(input.getMapMouseListener()).thenReturn(mock(IMouseListener.class));
        when(input.getMapMouseWheelListener()).thenReturn(new MouseAdapter() {});
        Controller.setCurrentController(controller);
        model = new MapModel((source, target, children) -> null, null, null);
        model.setRoot(new NodeModel("root", model));
        model.getRootNode().createID();
        view = new MapView(model, mode) {
            @Override public void setMap(MapModel model) { }
        };
        view.initializeForTest(MapViewLayout.MAP, -1);
        view.attachRootViewForTest(nativeView(model.getRootNode()));
        view.setBackground(Color.WHITE);
        view.replaceSelection(new NodeView[]{nativeView(model.getRootNode())});
        JViewport viewport = new JViewport();
        viewport.setView(view);
        viewport.setSize(800, 600);
        view.setSize(1600, 1200);
        when(manager.getMapViewComponent()).thenReturn(view);
        when(exports.checkCurrentMap(model)).thenReturn(true);
        when(links.getColor(any(ConnectorModel.class))).thenReturn(Color.BLUE);
        when(links.getOpacity(any(ConnectorModel.class))).thenReturn(200);
        when(links.getWidth(any(ConnectorModel.class))).thenReturn(2);
        when(links.getShape(any(ConnectorModel.class))).thenReturn(ConnectorShape.CUBIC_CURVE);
        ConnectorArrows arrows = ConnectorArrows.BOTH;
        when(links.getArrows(any(ConnectorModel.class))).thenReturn(arrows);
    }

    public NodeModel add(NodeModel parent, String text, boolean folded) {
        NodeModel child = new NodeModel(text, model);
        child.setFolded(folded);
        child.createID();
        parent.insert(child);
        return child;
    }

    public void attach() { nativeView(model.getRootNode()).syncChildren(); }

    public NodeView node(NodeModel model) { return nativeView(model); }

    public void narrowRoot(NodeModel root) { view.setRootNode(root); }

    public void filter(Filter filter) {
        filter.calculateFilterResults(model);
        view.getMapSelection().setFilter(filter);
    }

    public void hyperlink(NodeModel node, String href) {
        NodeLinks extension = node.getExtension(NodeLinks.class);
        if (extension == null) { extension = new NodeLinks(); node.addExtension(extension); }
        extension.setHyperLink(new Hyperlink(URI.create(href)));
    }

    public ConnectorModel connect(NodeModel source, NodeModel target) {
        ConnectorModel connector = new ConnectorModel(source, target.getID());
        connector.setStartInclination(new Point(60, 20));
        connector.setEndInclination(new Point(60, -20));
        NodeLinks extension = source.getExtension(NodeLinks.class);
        if (extension == null) { extension = new NodeLinks(); source.addExtension(extension); }
        extension.addArrowlink(connector);
        return connector;
    }

    public void details(NodeModel node, String details) {
        node.addExtension(new DetailModel("plain", details, details, false));
        ZoomableLabel label = new ZoomableLabel();
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(Color.BLACK);
        label.setText(details);
        NativeNodeView nodeView = nativeView(node);
        int width = nodeView.getContent().getWidth();
        nodeView.addContent(label, NodeView.DETAIL_VIEWER_POSITION);
        JComponent content = nodeView.getContent();
        content.setSize(width, content.getPreferredSize().height);
        content.doLayout();
    }

    public void icon(NodeModel node, Icon icon) { nativeView(node).getMainView().setIcon(icon); }

    public <T> T onEdt(Callable<T> action) throws Exception {
        FutureTask<T> result = new FutureTask<>(action);
        if (EventQueue.isDispatchThread()) result.run();
        else EventQueue.invokeAndWait(result);
        return result.get();
    }

    public Map<FileFilter, IExportEngine> installExports(BundleActivator activator) throws Exception {
        Map<FileFilter, IExportEngine> engines = new LinkedHashMap<>();
        engines.put(new CaseSensitiveFileNameExtensionFilter("html", "existing HTML"), mock(IExportEngine.class));
        doAnswer(call -> { engines.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(exports).addMapExportEngine(any(), any());
        BundleContext context = mock(BundleContext.class);
        activator.start(context);
        ArgumentCaptor<Object> provider = ArgumentCaptor.forClass(Object.class);
        verify(context).registerService(eq(IModeControllerExtensionProvider.class.getName()),
                provider.capture(), any());
        ((IModeControllerExtensionProvider) provider.getValue()).installExtension(mode, null);
        return engines;
    }

    public static byte[] gentiumBytes() throws IOException {
        try (InputStream input = DynamicSvgTestMap.class.getResourceAsStream("/fonts/gentium-subset.ttf")) {
            if (input == null) throw new IOException("Missing licensed Gentium fixture");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int count; (count = input.read(buffer)) >= 0;) bytes.write(buffer, 0, count);
            return bytes.toByteArray();
        }
    }

    public void flush() throws Exception { EventQueue.invokeAndWait(() -> {}); }

    @Override
    public void close() throws Exception {
        flush();
        KeyboardFocusManager focus = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        for (PropertyChangeListener listener : focus.getPropertyChangeListeners("focusedWindow"))
            if (!previousFocusListeners.contains(listener)) focus.removePropertyChangeListener("focusedWindow", listener);
        Controller.setCurrentController(previous);
    }

    private NativeNodeView nativeView(NodeModel node) { return views.computeIfAbsent(node, NativeNodeView::new); }

    private final class NativeNodeView extends NodeView {
        NativeNodeView(NodeModel node) {
            super(node, view);
            MainView main = new MainView();
            main.setPainter(new ForkPainter(main));
            setMainView(main);
            main.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            main.setForeground(Color.BLACK);
            main.setText(node.toString());
            main.setBounds(0, 0, 280, 35);
            getContent().setBounds(0, 0, 280, 60);
            node.addViewer(this);
        }

        void syncChildren() {
            getMainView().setText(getNode().toString());
            for (NodeView child : getChildrenViews()) { remove(child); child.getNode().removeViewer(child); }
            if (!isFolded())
                for (NodeModel child : getNode().getChildren()) {
                    NativeNodeView childView = nativeView(child);
                    child.addViewer(childView);
                    add(childView);
                    childView.syncChildren();
                }
        }

        @Override void fireFoldingChanged() { syncChildren(); }
        @Override void updateIcons() { }
        @Override void resetLayoutPropertiesRecursively() { }
    }
}
