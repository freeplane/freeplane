package org.freeplane.view.swing.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JViewport;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.undo.UndoHandler;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.MapViewLayout;
import org.junit.Before;
import org.junit.Test;

public class MapViewExportTest extends MapViewTitleTestSupport {
    private MapModel map;
    private MapView view;
    private final Map<NodeModel, CaptureNodeView> nodeViews = new IdentityHashMap<>();
    private JViewport viewport;

    @Before
    public void createNativeContentFixture() throws Exception {
        LayoutController layout = mock(LayoutController.class);
        when(mode.getExtension(LayoutController.class)).thenReturn(layout);
        when(layout.getEffectiveChildNodesLayout(any(NodeModel.class))).thenReturn(ChildNodesLayout.AUTO);
        when(mapController.isFolded(any(NodeModel.class))).thenAnswer(call -> ((NodeModel) call.getArgument(0)).isFolded());
        map = newMap();
        view = new MapView(map, mode) {
            @Override
            public void setMap(MapModel ignored) {
            }
        };
        field(view, "viewedMap", map);
        view.initializeForTest(MapViewLayout.MAP, -1);
        CaptureNodeView root = nodeView(map.getRootNode());
        view.attachRootViewForTest(root);
        view.replaceSelection(new NodeView[]{root});
        viewport = new JViewport();
        viewport.setView(view);
        viewport.setSize(300, 200);
        view.setSize(1200, 900);
        viewport.setViewPosition(new Point(31, 47));
        when(viewManager.getMapViewComponent()).thenReturn(view);
    }

    @Test
    public void capturesCurrentViewRootAndFilterScope() throws Exception {
        NodeModel branch = addChild(map.getRootNode(), "branch", false);
        NodeModel ancestor = addChild(branch, "excluded ancestor", true);
        NodeModel match = addChild(ancestor, "matching leaf", false);
        addChild(ancestor, "excluded leaf", false);
        addChild(map.getRootNode(), "unrelated subtree", false);
        attachChildren();
        view.setCurrentRootViewForTest(nodeView(branch));
        Filter filter = new Filter(node -> node == match, false, false, false, false,
                Filter.FilteredElement.NODE, null);
        filter.calculateFilterResults(map);
        view.getMapSelection().setFilter(filter);

        EventQueue.invokeAndWait(() -> new MapViewExportCapture(view).capture(context -> {
            assertThat(context.root.getNode()).isSameAs(branch);
            assertThat(context.nodes).extracting(NodeView::getNode).containsExactly(branch, ancestor, match);
            assertThat(context.nodes.get(1).isContentVisible()).isFalse();
            assertThat(context.nodes.get(2).isContentVisible()).isTrue();
            return null;
        }));
    }

    @Test
    public void recordsFoldingBeforeTemporaryUnfolding() throws Exception {
        NodeModel parent = addChild(map.getRootNode(), "folded parent", true);
        NodeModel child = addChild(parent, "folded child", true);
        addChild(child, "leaf", false);
        attachChildren();
        view.setZoom(1.75f);
        assertThat(nodeViews).doesNotContainKey(child);

        EventQueue.invokeAndWait(() -> new MapViewExportCapture(view).capture(context -> {
            assertThat(context.initialFolding).containsEntry(parent, true).containsEntry(child, true);
            assertThat(context.nodes).hasSize(4).allMatch(node -> !node.isFolded());
            assertThat(view.getZoom()).isEqualTo(1f);
            assertThatThrownBy(() -> context.nodes.clear()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> context.initialFolding.clear()).isInstanceOf(UnsupportedOperationException.class);
            return null;
        }));
        assertThat(view.getZoom()).isEqualTo(1.75f);
        assertThat(nodeView(parent).isFolded()).isTrue();
    }

    @Test
    public void restoresViewStateAfterCapture() throws Exception {
        NodeModel parent = addChild(map.getRootNode(), "folded parent", true);
        addChild(parent, "leaf", false);
        NodeModel selectionOpened = addChild(map.getRootNode(), "selection opened", true);
        addChild(selectionOpened, "selection leaf", false);
        NodeModel filterOpened = addChild(map.getRootNode(), "filter opened", true);
        addChild(filterOpened, "filter leaf", false);
        attachChildren();
        view.putClientProperty(MapView.FOLDING_FOLLOWS_SELECTION, true);
        NodeViewFolder selectionFolder = (NodeViewFolder) field(view, "nodeViewFolder");
        NodeViewFolder filterFolder = (NodeViewFolder) field(view, "filterNodeViewFolder");
        selectionFolder.adjustFolding(Collections.singleton(nodeView(selectionOpened)));
        filterFolder.adjustFolding(Collections.singleton(nodeView(filterOpened)));
        view.setZoom(1.5f);
        view.scrollNodeToVisible(nodeView(parent));
        List<MapViewExportCapture.ExportContext> contexts = new ArrayList<>();
        Map<String, Object> before = snapshot();

        EventQueue.invokeAndWait(() -> new MapViewExportCapture(view).capture(context -> {
            contexts.add(context);
            assertThat(view.isPrinting()).isTrue();
            return "captured";
        }));
        EventQueue.invokeAndWait(() -> {});

        assertThat(snapshot()).isEqualTo(before);
        assertThat(nodeView(parent).isFolded()).isTrue();
        EventQueue.invokeAndWait(() -> {
            selectionFolder.adjustFolding(Collections.emptySet());
            filterFolder.adjustFolding(Collections.emptySet());
        });
        assertThat(nodeView(selectionOpened).isFolded()).isTrue();
        assertThat(nodeView(filterOpened).isFolded()).isTrue();
        Graphics2D graphics = graphics();
        try {
            EventQueue.invokeAndWait(() -> assertThatThrownBy(() -> contexts.get(0).paintNode(nodeView(parent), graphics))
                    .isInstanceOf(IllegalStateException.class));
        } finally {
            graphics.dispose();
        }
    }

    @Test
    public void restoresViewStateWhenPaintingFails() throws Exception {
        NodeModel parent = addChild(map.getRootNode(), "folded parent", true);
        addChild(parent, "leaf", false);
        attachChildren();
        RuntimeException failure = new IllegalStateException("native icon painting failed");
        nodeView(parent).getMainView().setIcon(new Icon() {
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
            public void paintIcon(Component component, Graphics graphics, int x, int y) { throw failure; }
        });
        view.setZoom(0.75f);
        Map<String, Object> before = snapshot();

        EventQueue.invokeAndWait(() -> assertThatThrownBy(() -> new MapViewExportCapture(view).capture(context -> {
            Graphics2D graphics = graphics();
            try {
                context.paintNode(nodeView(parent), graphics);
            } finally {
                graphics.dispose();
            }
            return null;
        })).isSameAs(failure));
        EventQueue.invokeAndWait(() -> {});

        assertThat(snapshot()).isEqualTo(before);
        assertThat(nodeView(parent).isFolded()).isTrue();
    }

    @Test
    public void preservesMapContentAndSaveState() throws Exception {
        NodeModel parent = addChild(map.getRootNode(), "unsaved content", true);
        addChild(parent, "leaf", false);
        attachChildren();
        UndoHandler undo = new UndoHandler(map);
        map.addExtension(IUndoHandler.class, undo);
        IActor undoEntry = mock(IActor.class), redoEntry = mock(IActor.class);
        undo.addActor(undoEntry);
        undo.forceNewTransaction();
        undo.addActor(redoEntry);
        undo.undo();
        clearInvocations(undoEntry, redoEntry);
        List<Object> history = undoHistory(undo);
        int position = ((ListIterator<?>) field(undo, "actorIterator")).nextIndex();
        assertThat(undo.canUndo()).isTrue();
        assertThat(undo.canRedo()).isTrue();
        for (boolean saved : Arrays.asList(true, false)) {
            map.setSaved(saved);
            int changes = map.getNumberOfChangesSinceLastSave();
            List<Object> content = Arrays.asList(parent.getUserObject(), parent.isFolded(),
                    new ArrayList<>(parent.getChildren()), new ArrayList<>(parent.getViewers()));

            EventQueue.invokeAndWait(() -> new MapViewExportCapture(view).capture(context -> {
                assertThat(context.nodes).extracting(NodeView::getNode).contains(parent);
                return null;
            }));
            EventQueue.invokeAndWait(() -> {});

            assertThat(map.getNumberOfChangesSinceLastSave()).isEqualTo(changes);
            assertThat(map.getExtension(IUndoHandler.class)).isSameAs(undo);
            assertThat(undoHistory(undo)).isEqualTo(history);
            assertThat(((ListIterator<?>) field(undo, "actorIterator")).nextIndex()).isEqualTo(position);
            assertThat(undo.canUndo()).isTrue();
            assertThat(undo.canRedo()).isTrue();
            verifyNoInteractions(undoEntry, redoEntry);
            assertThat(Arrays.asList(parent.getUserObject(), parent.isFolded(),
                    new ArrayList<>(parent.getChildren()), new ArrayList<>(parent.getViewers()))).isEqualTo(content);
        }
    }

    @Test
    public void rejectsCaptureOutsideEventDispatchThread() {
        assertThatThrownBy(() -> new MapViewExportCapture(view).capture(context -> null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Export capture requires the Swing event dispatch thread");
    }

    private List<Object> undoHistory(UndoHandler undo) throws Exception {
        List<Object> history = new ArrayList<>();
        for (Object compound : (List<?>) field(undo, "actorList"))
            history.add(new ArrayList<>((List<?>) field(compound, "actors")));
        return history;
    }

    private Graphics2D graphics() {
        return new BufferedImage(400, 120, BufferedImage.TYPE_INT_ARGB).createGraphics();
    }

    private Map<String, Object> snapshot() throws Exception {
        Map<String, Object> state = new LinkedHashMap<>();
        for (String name : Arrays.asList("zoom", "paintingPurpose", "paintingMode", "isPreparedForPrinting",
                "fitMap", "boundingRectangle", "scrollsViewAfterLayout", "selectionDrivenFoldingState",
                "currentRootView", "currentRootParentView", "rootsHistory", "filter", "selectedsValid"))
            state.put(name, copy(field(view, name)));
        state.put("selection", new ArrayList<>(view.getSelection()));
        state.put("focus", KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
        state.put("viewport", viewport.getViewPosition());
        state.put("bounds", view.getBounds());
        for (String name : Arrays.asList("selectionUnfoldNodeModels", "pendingSelectionUnfoldNodeModels"))
            state.put(name, new ArrayList<>((Set<?>) field(view, name)));
        for (String folder : Arrays.asList("nodeViewFolder", "filterNodeViewFolder"))
            state.put(folder, new ArrayList<>((Set<?>) field(field(view, folder), "unfoldedNodeViews")));
        Object scroller = field(view, "mapScroller");
        for (String name : Arrays.asList("anchor", "anchorContentLocation", "anchorHorizontalPoint",
                "anchorVerticalPoint", "scrolledNode", "scrollingDirective", "showsSelectedAfterScroll",
                "slowScroll", "extraWidth", "scrollsNodeTreeToVisible"))
            state.put("scroll." + name, copy(field(scroller, name)));
        return state;
    }

    private Object copy(Object value) {
        if (value instanceof Point) return new Point((Point) value);
        if (value instanceof Rectangle) return new Rectangle((Rectangle) value);
        if (value instanceof List) return new ArrayList<>((List<?>) value);
        return value;
    }

    private NodeModel addChild(NodeModel parent, String text, boolean folded) {
        NodeModel child = new NodeModel(text, map);
        child.setFolded(folded);
        parent.insert(child);
        return child;
    }

    private void attachChildren() {
        nodeView(map.getRootNode()).syncChildren();
    }

    private CaptureNodeView nodeView(NodeModel node) {
        return nodeViews.computeIfAbsent(node, CaptureNodeView::new);
    }

    private class CaptureNodeView extends NodeView {
        CaptureNodeView(NodeModel node) {
            super(node, view);
            MainView main = new MainView();
            main.setPainter(new ForkPainter(main));
            setMainView(main);
            main.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            main.setForeground(Color.BLACK);
            main.setText(node.toString());
            main.setBounds(0, 0, 300, 50);
            getContent().setBounds(0, 0, 300, 50);
            node.addViewer(this);
        }

        void syncChildren() {
            for (NodeView child : getChildrenViews()) {
                remove(child);
                child.getNode().removeViewer(child);
            }
            if (!isFolded())
                for (NodeModel child : getNode().getChildren()) {
                    CaptureNodeView childView = nodeView(child);
                    child.addViewer(childView);
                    add(childView);
                    childView.syncChildren();
                }
        }

        @Override
        void fireFoldingChanged() { syncChildren(); }

        @Override
        void updateIcons() { }

        @Override
        void resetLayoutPropertiesRecursively() { }
    }
}
